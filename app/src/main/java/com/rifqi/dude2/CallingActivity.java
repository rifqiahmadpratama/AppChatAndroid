package com.rifqi.dude2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, acceptCallBtn;

    private String receiverUserId="",receiverUserImage="",receiverUserName="";
    private String senderUserId="",senderUserImage="",senderUserName="", checker="";
    private String callingID="", ringingID="";
    private DatabaseReference userRef;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Nelpon");

        mediaPlayer = MediaPlayer.create(this,R.raw.ringing);

        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker = "clicked";

                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                mediaPlayer.stop();
                final HashMap<String, Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked","picked");

                userRef.child(senderUserId).child(receiverUserId).child("Ringing")
                        .updateChildren(callingPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    mediaPlayer.stop();
                                    Intent callingIntent = new Intent(CallingActivity.this, VidioChatActivity.class);
                                    callingIntent.putExtra("visit_user_id", receiverUserId);
                                    startActivity(callingIntent);
                                }
                            }
                        });
            }
        });

        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.child(receiverUserId).child(senderUserId).exists())
                {
                    receiverUserImage = snapshot.child(receiverUserId).child(senderUserId).child("image").getValue().toString();
                    receiverUserName = snapshot.child(receiverUserId).child(senderUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }
                if (snapshot.child(senderUserId).child(receiverUserId).exists())
                {
                    senderUserImage = snapshot.child(senderUserId).child(receiverUserId).child("image").getValue().toString();
                    senderUserName = snapshot.child(senderUserId).child(receiverUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mediaPlayer.start();
        userRef.child(receiverUserId).child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!checker.equals("clicked") && !snapshot.hasChild("Calling")  && !snapshot.hasChild("Ringing"))
                        {
                            final HashMap<String, Object> callingInfo = new HashMap<>();

                            callingInfo.put("calling", receiverUserId);

                            userRef.child(senderUserId).child(receiverUserId)
                                    .child("Calling")
                                    .updateChildren(callingInfo)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                final HashMap<String, Object> ringingInfo = new HashMap<>();
                                                ringingInfo.put("ringing", senderUserId);

                                                userRef.child(receiverUserId).child(senderUserId)
                                                        .child("Ringing")
                                                        .updateChildren(ringingInfo);
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserId).child(receiverUserId).hasChild("Ringing") && !snapshot.child(senderUserId).child(receiverUserId).hasChild("Calling"))
                {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }

                if (snapshot.child(receiverUserId).child(senderUserId).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent callingIntent = new Intent(CallingActivity.this, VidioChatActivity.class);
                    callingIntent.putExtra("visit_user_id", receiverUserId);
                    startActivity(callingIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void cancelCallingUser()
    {
        //from sender side
        userRef.child(senderUserId).child(receiverUserId)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists() && snapshot.hasChild("calling"))
                        {
                            callingID = snapshot.child("calling").getValue().toString();

                            userRef.child(callingID)
                                    .child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                userRef.child(senderUserId).child(receiverUserId)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                startActivity(new Intent(CallingActivity.this, MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }

                        else
                        {
                            //startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //from receiver side

        userRef.child(senderUserId).child(receiverUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists() && snapshot.hasChild("ringing"))
                        {
                            ringingID = snapshot.child("ringing").getValue().toString();

                            userRef.child(ringingID)
                                    .child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                userRef.child(senderUserId).child(receiverUserId)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                startActivity(new Intent(CallingActivity.this, MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }

                        else
                        {
                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}