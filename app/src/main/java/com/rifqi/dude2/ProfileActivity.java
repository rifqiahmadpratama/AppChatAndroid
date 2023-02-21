package com.rifqi.dude2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static String PENGIRIM = null;
    private String receiverUserID, senderUserID, Current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMassageRequestButton;

    private String kunciPengirim = "";
    private String bukaKunciPengirim = "";

    private String KunciPengirim;
    private String BukaKunciPengirim;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMassageRequestButton = (Button) findViewById(R.id.decline_message_request_button);
        Current_State = "new";

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

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
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
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received")){
                                Current_State = "request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMassageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMassageRequestButton.setEnabled(true);

                                DeclineMassageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)){
                                                Current_State = "friends";
                                                SendMessageRequestButton.setText("Remove this Contact");
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
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new")){
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        deleteMessageForEveryone();
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");



                                                DeclineMassageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMassageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        privatepengirim();
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("rsa").setValue(KunciPengirim);
                            Log.d("test","Nilai Kunci publik pengirim sebelum di kirim = " +KunciPengirim);
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    SendMessageRequestButton.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    SendMessageRequestButton.setText("Remove this Contact");

                                                                                    DeclineMassageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMassageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
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

    private void CancelChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue();
        ContactsRef.child(receiverUserID).child(senderUserID)
                .removeValue();
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMassageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMassageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
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

                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
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

    private void deleteMessageForEveryone(){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(senderUserID)
                .child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(receiverUserID)
                            .child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                            }
                        }
                    });
                }
                else {
                }
            }
        });
    }
}
