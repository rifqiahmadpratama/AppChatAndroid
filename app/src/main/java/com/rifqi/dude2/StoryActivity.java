package com.rifqi.dude2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;

//    VideoView playerView;
//    SimpleExoPlayer exoPlayer;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> images;
    List<String> videos;
    List<String> storyids;
    String userid;
    //   MediaController mediaController;

    Uri uri = (Uri) videos;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);


        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        story_photo = findViewById(R.id.story_photo);
        story_username = findViewById(R.id.story_username);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        userid = getIntent().getStringExtra("visit_user_id");

        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        getStories(userid);
        userInfo(userid);


        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryActivity.this, ViewActivity.class);
                intent.putExtra("id", userid);
                intent.putExtra("storyid", storyids.get(counter));
                intent.putExtra("title", "views");
                startActivity(intent);
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                        .child(userid).child(storyids.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(StoryActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onNext() {

        if (images != null){
            Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);
//            image.setVisibility(View.VISIBLE);
//            playerView.setVisibility(View.GONE);
        }
        if (uri != null){
            Log.d("tes","Vidio 1 = " + videos);
//            image.setVisibility(View.GONE);
//            playerView.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()).asBitmap().load(videos.get(++counter)).into(image);

        }


        addView(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        if (images != null){
            Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);
//            image.setVisibility(View.VISIBLE);
//            playerView.setVisibility(View.GONE);
        }
        if (uri != null) {
            Log.d("tes","Vidio 2 = " + videos);
//            image.setVisibility(View.GONE);
//            playerView.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()).asBitmap().load(videos.get(--counter)).into(image);
        }
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String userid){
        images = new ArrayList<>();
        storyids = new ArrayList<>();
        videos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                storyids.clear();
                videos.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Story story = snapshot1.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        images.add(story.getImageurl());
                        Log.d("Cek gambar" ,"baru "+ images);
                        storyids.add(story.getStoryid());
                        videos.add(story.getVideourl());
                        Log.d("Cek Vidio" ,"lama" + videos);
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoriesCount(videos.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                if (images != null){
                    Glide.with(getApplicationContext()).load(images.get(counter))
                            .into(image);
//                    image.setVisibility(View.VISIBLE);
//                    playerView.setVisibility(View.GONE);
                }
                if (uri != null) {
                    Log.d("tes","Vidio 3 = " + videos);
//                    image.setVisibility(View.GONE);
//                    playerView.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext()).asBitmap().load(videos.get(counter)).into(image);

//                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("video");
//                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//                    MediaSource mediaSource = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//                    playerView.setPlayer(exoPlayer);
//                    exoPlayer.prepare(mediaSource);
//                    exoPlayer.setPlayWhenReady(false);
                }


                addView(storyids.get(counter));
                seenNumber(storyids.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Contacts user = snapshot.getValue(Contacts.class);
                Glide.with(getApplicationContext()).load(user.getImage()).into(story_photo);
                story_username.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addView(String storyid){
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

    }
    private void seenNumber(String storyid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyid).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen_number.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}