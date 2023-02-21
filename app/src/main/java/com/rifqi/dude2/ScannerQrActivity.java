package com.rifqi.dude2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScannerQrActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler,MessageDialogFragment.MessageDialogListener{

    private Activity mActivity;
    private Context mContext;
    private ZBarScannerView zBarScannerView;
    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private ViewGroup contentFrame;
    private Button back, btnRegLogin;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;
    private String KunciPengirim;
    private String BukaKunciPengirim;
    private String bukaKunciPengirim="";
    private String kunciPengirim = "";
    private static String PENGIRIM = null;
    private String receiverUserID,senderUserID, Current_State;
    private String nilai;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_qr);
        btnRegLogin = findViewById(R.id.btnRegLogin);
        back = findViewById(R.id.back);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        senderUserID = mAuth.getCurrentUser().getUid();

        Current_State = "new";

        Log.d("tes","Pesan luar method = "+nilai);

        btnRegLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callingIntent = new Intent(ScannerQrActivity.this, QrCodeActivity.class);
                startActivity(callingIntent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScannerQrActivity.this.finish();
            }
        });
        mActivity = this;

        zBarScannerView = new ZBarScannerView(mActivity);
        setupFormats();

        contentFrame = findViewById(R.id.content_frame);
    //    zBarScannerView = new ZBarScannerView(mActivity);
        if (savedInstanceState != null){
            mFlash = savedInstanceState.getBoolean(FLASH_STATE,false);
            mAutoFocus = savedInstanceState.getBoolean(AUTO_FOCUS_STATE,true);
            mSelectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_FORMATS);
            mCameraId = savedInstanceState.getInt(CAMERA_ID,-1);
        } else {
            mFlash = false;
            mAutoFocus = true;
            mSelectedIndices = null;
            mCameraId = -1;
        }
        setupFormats();
        contentFrame.addView(zBarScannerView);

        try {
            Map<String, Object> keyMapPengirim = rsa.initKey();
            kunciPengirim = rsa.getPublicKey(keyMapPengirim);
            KunciPengirim = kunciPengirim;
            Log.d("test","Kunci Publik pengirim Asli = " + KunciPengirim);
            bukaKunciPengirim = rsa.getPrivateKey(keyMapPengirim);
            BukaKunciPengirim = bukaKunciPengirim;
            Log.d("test","Kunci private pengirim Asli = " + BukaKunciPengirim);
        } catch (Exception e) {
            e.printStackTrace();
        }
        receiverUserID = nilai;
        if (receiverUserID != null){
            RetrieveUserInfo();
        }



    }

    @Override
    public void onResume() {
        super.onResume();
        zBarScannerView.setResultHandler((ZBarScannerView.ResultHandler) this); //Register ourselves a handler for scan results.
        zBarScannerView.startCamera(); // Start camera on resume
        zBarScannerView.setFlash(mFlash);
        zBarScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE,mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE,mAutoFocus);
        outState.putIntegerArrayList(SELECTED_FORMATS,mSelectedIndices);
        outState.putInt(CAMERA_ID,mCameraId);
    }

    @Override
    public void onPause() {
        super.onPause();
        zBarScannerView.stopCamera(); //Stop Camera on Stop
        closeMessageDialog();

    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ManageChatRequest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)){
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                Current_State = "request_sent";

                            }
                            else if (request_type.equals("received")){
                                Current_State = "request_received";

                            }
                        }
                        else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)){
                                                Current_State = "friends";
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(receiverUserID)){
            if (receiverUserID != null){
                if (Current_State.equals("new")){
                     sendFirebase();
                }
            }
        }
    }

    public void setupFormats(){
        List<BarcodeFormat> formats = new ArrayList<>();
        if (mSelectedIndices == null || mSelectedIndices.isEmpty()){
            mSelectedIndices = new ArrayList<Integer>();
            for (int i = 0; i< BarcodeFormat.ALL_FORMATS.size(); i++){
                mSelectedIndices.add(i);
            }
        }

        for (int index : mSelectedIndices){
            formats.add(BarcodeFormat.ALL_FORMATS.get(index));
        }
        if (zBarScannerView != null){
            zBarScannerView.setFormats(formats);
        }
    }

    @Override
    public void handleResult(Result result) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(this.getApplicationContext(),notification);
            r.play();
        } catch (Exception e){
        }
        showMessageDialog(result.getContents());

    }

    public void sendFirebase(){
        privatepengirim();
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("rsa").setValue(KunciPengirim);
                            Log.d("test","Nilai Kunci publik penerima sebelum dikirim = " +KunciPengirim);
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");
                                                chatNotificationMap.put("hahahah", nilai);
                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Current_State = "request_sent";
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public void showMessageDialog(String message){
        DialogFragment fragment = MessageDialogFragment.newInstance("Scan Results",message,this);
        fragment.show(this.getSupportFragmentManager(),"scan_results");

        pesan(message);
    }

    public void pesan(String message){
        nilai = message;
        Log.d("tes","Pesan dalam method = "+nilai);
        receiverUserID = nilai;
        privatepengirim();
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("rsa").setValue(KunciPengirim);
                            Log.d("test","Nilai Kunci publik penerima sebelum dikirim = " +KunciPengirim);
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");
                                                chatNotificationMap.put("hahahah", nilai);
                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Current_State = "request_sent";
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public void closeMessageDialog(){
        closeDialog("scan_results");
    }
    public void closeDialog(String dialogName){
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(dialogName);
        if (fragment != null){
            fragment.dismiss();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Resume the camera
        zBarScannerView.resumeCameraPreview(this);
    }

    public void privatepengirim(){
        String text1 = BukaKunciPengirim;
        Log.d("test","Kunci Private pengirim sebelum di simpan = " + text1);
        FileOutputStream fos1 = null;
        try {
            fos1 = openFileOutput(PENGIRIM = receiverUserID,MODE_PRIVATE);
            fos1.write(text1.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos1 != null){
                try {
                    fos1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos1 = openFileOutput(PENGIRIM = senderUserID,MODE_PRIVATE);
            fos1.write(text1.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos1 != null){
                try {
                    fos1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}