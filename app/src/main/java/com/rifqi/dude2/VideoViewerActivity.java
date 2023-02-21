package com.rifqi.dude2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoViewerActivity extends AppCompatActivity {

    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView video_viewer;
    private String videoview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        simpleExoPlayer = new SimpleExoPlayer.Builder(VideoViewerActivity.this).build();
        video_viewer = findViewById(R.id.video_viewer);

        video_viewer.setPlayer(simpleExoPlayer);
        videoview = getIntent().getExtras().get("videoview").toString();
        Uri video = Uri.parse(videoview);
        MediaItem mediaItem = MediaItem.fromUri(video);
        simpleExoPlayer.addMediaItem(mediaItem);
        simpleExoPlayer.prepare();
        simpleExoPlayer.play();

    }
}