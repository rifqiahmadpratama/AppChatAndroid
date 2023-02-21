package com.rifqi.dude2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VidioChatActivity extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener
{

    private static String API_Key = "47113424";
    private static String SESSION_ID = "1_MX40NzExMzQyNH5-MTYxMjc0Mjg4MTIyNH4xT1plUDVuV2dJZ3dRRnBhaERPYXEwMnh-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NzExMzQyNCZzaWc9MWQ3NDE5ODMxYTI0OWY0NjlmMjkxZjMzNTNlNzhmMWQxYTk1MzJkODpzZXNzaW9uX2lkPTFfTVg0ME56RXhNelF5Tkg1LU1UWXhNamMwTWpnNE1USXlOSDR4VDFwbFVEVnVWMmRKWjNkUlJuQmhhRVJQWVhFd01uaC1mZyZjcmVhdGVfdGltZT0xNjEyNzQyOTM0Jm5vbmNlPTAuNTU3NTI3MDE3MDkyNDM2MiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjE1MzM0OTM0JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG =  VidioChatActivity.class.getSimpleName();
    private static final int RC_VIDIO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscrirbeViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView closeVidioChatBtn;
    private DatabaseReference userRef;
    private String senderUserId="";
    private String receiverUserId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vidio_chat);
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Nelpon");

        closeVidioChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVidioChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(senderUserId).child(receiverUserId).hasChild("Ringing"))
                        {
                            userRef.child(senderUserId).child(receiverUserId).child("Ringing").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mPublisher.destroy();
                            }

                            startActivity(new Intent(VidioChatActivity.this,MainActivity.class));
                            finish();
                        }

                        if (snapshot.child(senderUserId).child(receiverUserId).hasChild("Calling"))
                        {
                            userRef.child(senderUserId).child(receiverUserId).child("Calling").removeValue();
                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mPublisher.destroy();
                            }
                            startActivity(new Intent(VidioChatActivity.this,MainActivity.class));
                            finish();
                        }
                        else
                        {
                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mPublisher.destroy();
                            }
                            //  startActivity(new Intent(VidioChatActivity.this,MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VidioChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDIO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this,perms))
        {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscrirbeViewController = findViewById(R.id.subscribe_container);

            //1. initialize and connect to the Session
            mSession = new Session.Builder(this,API_Key, SESSION_ID).build();
            mSession.setSessionListener(VidioChatActivity.this);
            mSession.connect(TOKEN);
        }

        else
        {
            EasyPermissions.requestPermissions(this,"Hey this app needs Mic and camera, Please allow.",RC_VIDIO_APP_PERM,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }


    // 2. Publishing a stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VidioChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
    }

    //3. Subscribe to the streams

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Received");

        if (mSubscriber  == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscrirbeViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");

        if (mSubscriber != null)
        {
            mSubscriber = null;
            mSubscrirbeViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}