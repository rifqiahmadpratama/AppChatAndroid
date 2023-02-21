package com.rifqi.dude2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.AllPermission;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Permissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID, audioPath;

    private byte[] encryptionKey = {10, 120, 56, 91, 110, 9, -26, -18, -63, 93, 22, 25, 8, -100, 124, -48};
    Cipher cipher;
    SecretKeySpec secretKeySpec;
    private static final int PICK_VIDEO = 1;
    private Uri videoUri,audioUri;
    private ImageView btEmoji;
    private static String PENGIRIM = null;
    private String pengirim;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private Calendar calendar;

    private ImageButton SendMessageButton, SendFilesButton, callBtn, back_home;
    private RecordButton recordButton;
    private RecordView recordView;
    private EditText MessageInputText;
    private byte[] encodeData;

    //   VideoView videoView;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String getThemeku;
    private String themeku = "";
    private View messageLayout;
    private RelativeLayout relativeLayout_utama;
    private String saveCurrentTime, saveCurrentDate;
    private String checker="", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    private String aesc;
    private String SHARED_PREFS = "codeTheme";
    private String calledBy="";
    private int RECORDING_REQUEST_CODE = 3000;
    private MediaRecorder mediaRecorder;

    private FirebaseUser currentUser;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private static final int REQUEST_IMAGE_CAPTURE = 11;
    private static final int REQUEST_VIDEO_CAPTURE = 40000;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private SwipeRefreshLayout mRefreshLayout;
    private int mCurrentPage = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        currentUser = mAuth.getCurrentUser();
        RootRef.child("Notification").child(messageReceiverID).child(messageSenderID).child("request_type").setValue("tidak");
        RootRef.child("Notification").child(messageReceiverID).child(messageSenderID).child("message").setValue("tidak");
        getPengirim();
        IntializeControllers();
        initView();
        final EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.relativeLayout_utama)
        ).build(MessageInputText);

        btEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.toggle();
            }
        });
        back_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.finish();
            }
        });

//        relativeLayout_profile.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("ResourceAsColor")
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ChatActivity.this, ProfileFriendsActivity.class);
//                startActivity(intent);
//              //  relativeLayout_profile.setBackgroundColor(R.color.bot_back_color);
//            }
//        });

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.ndefaprof).into(userImage);

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callingIntent = new Intent(ChatActivity.this, CallingActivity.class);
                callingIntent.putExtra("visit_user_id", messageReceiverID);
                startActivity(callingIntent);
            }
        });

        MessageInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {

                    updateTypingStatus("false");
                    SendMessageButton.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                    SendFilesButton.setVisibility(View.VISIBLE);


                    updateUserStatus("tidak");

                } else {
                    updateTypingStatus(messageReceiverID);
                    recordButton.setVisibility(View.GONE);
                    SendMessageButton.setVisibility(View.VISIBLE);
                    SendFilesButton.setVisibility(View.GONE);
                    updateUserStatus("ketik");

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        DisplayLastSeen();
        updateMessage();
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ItemManis [] items = {
                        new ItemManis("Images", R.drawable.ic_image),
                        new ItemManis("Maps", R.drawable.ic_map),
                        new ItemManis("Video", R.drawable.ic_video),
                        new ItemManis("Audio", R.drawable.ic_audio),
                        new ItemManis("Ambil Foto", R.drawable.ic_camera),
                        new ItemManis("Ambil Video", R.drawable.ic_video_camp)
                };
                ListAdapter adapter = new ArrayAdapter<ItemManis>(ChatActivity.this,
                        android.R.layout.select_dialog_item, android.R.id.text1, items) {
                    public View getView(int position, View counterView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, counterView, parent);
                        TextView tv = v.findViewById(android.R.id.text1);
                        tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon,0,0,0);
                        int dp5 = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
                        tv.setCompoundDrawablePadding(dp5);
                        return v;
                    }};
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
              //  AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setIcon(R.mipmap.dude);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if (which == 1){
                            Intent chatIntent = new Intent(ChatActivity.this, MapsActivity.class);
                            chatIntent.putExtra("visit_receiver", messageReceiverID);
                            chatIntent.putExtra("visit_sender", messageSenderID);
                            startActivity(chatIntent);
                        }
                        if (which == 2){
                            checker = "video";
                            Intent intent = new Intent();
                            intent.setType("video/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent,PICK_VIDEO);
                        }
                        if (which == 3){
                            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                            i.setType("audio/*");
                            startActivityForResult(i,101);
                        }
                        if (which == 4){
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null){
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                        if (which == 5){
                            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            takeVideoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            takeVideoIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            if (takeVideoIntent.resolveActivity(getPackageManager()) != null){
                                    takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                                    takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage = mCurrentPage+6;

               messagesList.clear();

                updateMessage();
            }
        });
    }

    public void relativeLayout_profile(View view){
        Intent intent = new Intent(ChatActivity.this,ProfileFriendsActivity.class);
        startActivity(intent);
    }

    private void updateMessage(){

        DatabaseReference messageRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID);
        Query messageQuery = messageRef.limitToLast(mCurrentPage + TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);


                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.scrollToPosition(messagesList.size() - 1);

                        mRefreshLayout.setRefreshing(false);

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());



                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateTypingStatus(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(messageReceiverID);
        Map<String, Object> map = new HashMap<>();
        map.put("typing", status);
        databaseReference.updateChildren(map);
    }
    private void updateUserStatus(String state){
        RootRef.child("Notification").child(messageSenderID).child(messageReceiverID).child("message")
                .setValue(state);
    }

    private void updateUserStatus3(String state){
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("status", state);

        RootRef.child("Users").child(messageSenderID).child("userState")
                .updateChildren(onlineStateMap);
    }

    private void IntializeControllers() {

        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        btEmoji = (ImageButton) findViewById(R.id.bt_emoji);
        messageLayout = findViewById(R.id.messageLayout);
        relativeLayout_utama = (RelativeLayout) findViewById(R.id.relativeLayout_utama);
        //relativeLayout_profile = (RelativeLayout) findViewById(R.id.relativeLayout_profile);
        back_home = (ImageButton) findViewById(R.id.back_home);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        recordButton = (RecordButton) findViewById(R.id.recordButton);
        recordView = (RecordView) findViewById(R.id.recordView);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        callBtn = (ImageButton) findViewById(R.id.video_call);
        messageAdapter = new MessageAdapter(messagesList, getPengirim());
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        getThemeku = sharedPreferences.getString(themeku,"");



        if (getThemeku.equals("red")){

            relativeLayout_utama.setBackgroundResource(R.drawable.bgred);


        }
        else if (getThemeku.equals("green")){
            relativeLayout_utama.setBackgroundResource(R.drawable.bggreen);

        }
        else if (getThemeku.equals("orange")){
            relativeLayout_utama.setBackgroundResource(R.drawable.bgorange);

        }
        else {
            byte[] imageBytes = android.util.Base64.decode(getThemeku, android.util.Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            BitmapDrawable background = new BitmapDrawable(decodedImage);
            relativeLayout_utama.setBackgroundDrawable(background);
        }

    }
    private String getExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void initView() {

        recordButton.setRecordView(recordView);
        recordButton.setListenForRecord(false);

        recordButton.setOnClickListener(view -> {

            if (isRecordOk(ChatActivity.this))
                    recordButton.setListenForRecord(true);
            else requestRecording(ChatActivity.this);
        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");

                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                messageLayout.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                recordView.setVisibility(View.GONE);
                messageLayout.setVisibility(View.VISIBLE);


            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                Log.d("RecordView", "onFinish");

                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                recordView.setVisibility(View.GONE);
                messageLayout.setVisibility(View.VISIBLE);

                sendRecodingMessage(audioPath);


            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                recordView.setVisibility(View.GONE);
            }
        });
    }

    private void sendRecodingMessage(String audioPath) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files");

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        final String messagePushID = userMessageKeyRef.getKey();

        final StorageReference filePath = storageReference.child(messagePushID + "." + ".3gp");

        fileUri = Uri.fromFile(new File(audioPath));

        uploadTask = filePath.putFile(fileUri);

        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }

                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUrl = task.getResult();
                    myUrl = downloadUrl.toString();

                    Map messageImageBody = new HashMap();
                    messageImageBody.put("message", myUrl);
                    messageImageBody.put("name", fileUri.getLastPathSegment());
                    messageImageBody.put("type", "record");
                    messageImageBody.put("from", messageSenderID);
                    messageImageBody.put("to", messageReceiverID);
                    messageImageBody.put("messageID", messagePushID);
                    messageImageBody.put("time", saveCurrentTime);
                    messageImageBody.put("date", saveCurrentDate);


                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Message Sent...", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            MessageInputText.setText("");
                        }
                    });
                }
            }
        });
    }


    private void setUpRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ChatMe/Media/Recording");

        if (!file.exists())
            file.mkdirs();
        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(audioPath);
    }

    public boolean isRecordOk(Context context){
        return ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    public void requestRecording(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, RECORDING_REQUEST_CODE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu_chat, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.remove_chat){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title dialog
            alertDialogBuilder.setTitle("Menghapus semua pesan?");

            // set pesan dari dialog
            alertDialogBuilder
                    .setMessage("Klik Ya untuk menghapus!")
                    .setIcon(R.mipmap.dude)
                    .setCancelable(false)
                    .setPositiveButton("Ya",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // jika tombol diklik, maka akan menutup activity ini
                            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).removeValue();
                            ChatActivity.this.finish();
                        }
                    })
                    .setNegativeButton("Tidak",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // jika tombol ini diklik, akan menutup dialog
                            // dan tidak terjadi apa2
                            dialog.cancel();
                        }
                    });

            // membuat alert dialog dari builder
            AlertDialog alertDialog = alertDialogBuilder.create();

            // menampilkan alert dialog
            alertDialog.show();

        }
        if (item.getItemId() == R.id.wallpaper){
            wallpaper();
        }


        return true;
    }

    private void wallpaper() {
        Intent wallpaper = new Intent(ChatActivity.this, WallpaperActivity.class);
        wallpaper.putExtra("visit_user_id", messageReceiverID);
        startActivity(wallpaper);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK &&
                data != null && data.getData() != null){

            loadingBar.setTitle("Sending");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            videoUri = data.getData();

            //fileUri = data.getData();

            if (!checker.equals("video")){

            }
            else if (checker.equals("video")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Video Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + getExt(videoUri));

                uploadTask = filePath.putFile(videoUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", videoUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Video Sent...", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            }
            else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            loadingBar.setTitle("Sending");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")){

            }
            else if (checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Image Sent...", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null){
            loadingBar.setTitle("Sending");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            audioUri = data.getData();

            if (audioUri != null){
             //   Toast.makeText(this, "uploads please wait!", Toast.LENGTH_SHORT).show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + ".3gp");


                uploadTask = filePath.putFile(audioUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", audioUri.getLastPathSegment());
                            messageImageBody.put("type", "record");
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Audio Sent...", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            loadingBar.setTitle("Sending");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),imageBitmap,"val",null);
            fileUri = Uri.parse(path);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

            final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            final String messagePushID = userMessageKeyRef.getKey();

            final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

            uploadTask = filePath.putFile(fileUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        Map messageImageBody = new HashMap();
                        messageImageBody.put("message", myUrl);
                        messageImageBody.put("name", fileUri.getLastPathSegment());
                        messageImageBody.put("type", "image");
                        messageImageBody.put("from", messageSenderID);
                        messageImageBody.put("to", messageReceiverID);
                        messageImageBody.put("messageID", messagePushID);
                        messageImageBody.put("time", saveCurrentTime);
                        messageImageBody.put("date", saveCurrentDate);


                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this, "Image Sent...", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                                MessageInputText.setText("");
                            }
                        });
                    }
                }
            });
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            loadingBar.setTitle("Sending");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            videoUri = data.getData();

            new VideoCompressAsyncTask(this.getApplicationContext()).execute("false", videoUri.toString());

            videoUri = Uri.parse(videoUri.toString());
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Video Files");

            final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            final String messagePushID = userMessageKeyRef.getKey();

            final StorageReference filePath = storageReference.child(messagePushID + "." + getExt(videoUri));

            uploadTask = filePath.putFile(videoUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        Map messageImageBody = new HashMap();
                        messageImageBody.put("message", myUrl);
                        messageImageBody.put("name", videoUri.getLastPathSegment());
                        messageImageBody.put("type", "video");
                        messageImageBody.put("from", messageSenderID);
                        messageImageBody.put("to", messageReceiverID);
                        messageImageBody.put("messageID", messagePushID);
                        messageImageBody.put("time", saveCurrentTime);
                        messageImageBody.put("date", saveCurrentDate);


                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this, "Video Sent...", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                                MessageInputText.setText("");
                            }
                        });
                    }
                }
            });
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "foxandroidReminderChannel";
            String description = "Channel For Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("foxandroid",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")){
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();


                            if (state.equals("online")){
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline")){
                                userLastSeen.setText(time); RootRef.child("Users").child(messageReceiverID)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.child("userState").hasChild("status")){
                                                    String status = snapshot.child("userState").child("status").getValue().toString();
                                                    if (status.equals("online")){
                                                        userLastSeen.setText("online");
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }

                        }

                        else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        RootRef.child("Notification").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(messageSenderID).hasChild("message")){

                            String state = dataSnapshot.child(messageSenderID).child("message").getValue().toString();

                            if (state.equals("ketik")){
                                userLastSeen.setText("Sedang Mengetik...");

                            }
                            else if (state.equals("tidak")){
                                RootRef.child("Users").child(messageReceiverID)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.child("userState").hasChild("state")){
                                                    String state = snapshot.child("userState").child("state").getValue().toString();
                                                    if (state.equals("online")){
                                                        userLastSeen.setText("online");
                                                    }
                                                    else if (state.equals("tidak")){

                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }

                        else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        RootRef.child("Notification").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(messageSenderID)){
                            String request_type = dataSnapshot.child(messageSenderID).child("request_type").getValue().toString();
                            if (request_type.equals("berhasil")){
                                createNotificationChannel();
                                Intent i = new Intent(ChatActivity.this,MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(ChatActivity.this,0,i,0);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatActivity.this,"foxandroid")
                                    .setSmallIcon(R.mipmap.dude)
                                    .setContentTitle("DUDE")
                                    .setContentText("Pesan dari " + messageReceiverName)
                                    .setAutoCancel(true)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pendingIntent);
                                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ChatActivity.this);
                                notificationManagerCompat.notify(123,builder.build());

                                RootRef.child("Notification").child(messageReceiverID).child(messageSenderID).child("message").setValue("tidak");
                                RootRef.child("Notification").child(messageReceiverID).child(messageSenderID).child("request_type").setValue("tidak");
                                userLastSeen.setText("online");

                        }
                            else if (request_type.equals("tidak")){

                            }
                        }

                        else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            updateUserStatus3("online");
        }

        getPengirim();
        checkForReceivingCall();
        RootRef.child("Contacts").child(messageSenderID).child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String rsaPubKey = dataSnapshot.child("rsa").getValue().toString();
                Log.d("test ","Kunci Publik RSA :"+ rsaPubKey);
                String aesKey = new String(encryptionKey);
                Log.d("test","Pesan = "+ aesKey);
                Log.d("test","Kunci AES sebelum di kirim dengan RSA = " + Arrays.toString(encryptionKey));

                try {
                    encodeData = rsa.encryptByPublicKey(encryptionKey, rsaPubKey);
                    byte[] baseencRSA = Base64.encode(encodeData,Base64.DEFAULT);
                    aesc = new String(baseencRSA);
                    Log.d("test","Setelah di enkrip = " + aesc);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    protected void onStop() {
        if (currentUser != null)
        {
            updateUserStatus3("offline");
        }
        super.onStop();
        updateUserStatus("tidak");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser != null){
            updateUserStatus3("offline");
        }
        updateUserStatus("tidak");

    }
    private String AESEncryptionmethod (String string){
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            returnString = new String(encryptedByte,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return returnString;
    }


    private void SendMessage(){
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else {
            String AESAJA =  (AESEncryptionmethod(messageText));
            byte[] baseencAES = Base64.encode(AESAJA.getBytes(),Base64.DEFAULT);
            String message = new String(baseencAES);
            Log.d("Test", "Encode = " + message);
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("aes", aesc);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent...", Toast.LENGTH_SHORT).show();
                        RootRef.child("Notification").child(messageSenderID).child(messageReceiverID).child("request_type")
                                .setValue("berhasil")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            RootRef.child("Notification").child(messageReceiverID).child(messageSenderID)
                                                    .child("request_type")
                                                    .setValue("tidak");

                                        }
                                    }
                                });
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }

    public String getPengirim(){
        FileInputStream fis = null;
        try {
            fis = openFileInput(PENGIRIM=messageSenderID);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }
            pengirim = (sb.toString());
            Log.d("test","Kunci Private pengirim = " + pengirim);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            fis = openFileInput(PENGIRIM=messageReceiverID);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }

            pengirim = (sb.toString());
            Log.d("test","Kunci Private pengirim = " + pengirim);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pengirim;
    }

    private void checkForReceivingCall()
    {
        RootRef.child("Nelpon").child(messageSenderID).child(messageReceiverID)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("ringing"))
                        {
                            calledBy = snapshot.child("ringing").getValue().toString();

                            Intent callingIntent = new Intent(ChatActivity.this, CallingActivity.class);
                            callingIntent.putExtra("visit_user_id", calledBy);
                            startActivity(callingIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;
        public VideoCompressAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {

                //This bellow is just a temporary solution to test that method call works
                boolean b = Boolean.parseBoolean(paths[0]);
                if (b) {
                    filePath = SiliCompressor.with(mContext).compressVideo(paths[1], paths[2]);
                } else {
                    Uri videoContentUri = Uri.parse(paths[1]);
                    // Example using the bitrate and video size parameters
                    filePath = SiliCompressor.with(mContext).compressVideo(
                            videoContentUri,
                            paths[1],
                            640,
                            480,
                            15000);
                    filePath = SiliCompressor.with(mContext).compressVideo(
                            videoContentUri,
                            paths[2],
                            640,
                            480,
                            15000);
                }


            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return filePath;
        }
    }
}