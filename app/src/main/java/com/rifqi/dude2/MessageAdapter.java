package com.rifqi.dude2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static androidx.core.content.ContextCompat.getSystemService;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> implements OnMapReadyCallback {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Cipher decipher,cipher;
    private SecretKeySpec secretKeySpec, secretKeySpec1;
    private String privKeypengirim;
    private byte[] decodeData;
    private byte[] encryptionKey = {10, 120, 56, 91, 110, 9, -26, -18, -63, 93, 22, 25, 8, -100, 124, -48};
    private GoogleMap mMap;
    private PlayerView messageSenderVideo, messageReceiverVideo;
    SimpleExoPlayer exoPlayer;

    List<Location> savedLocations;


    public MessageAdapter (List<Messages> userMessagesList, String privateKeyPengirim){
        this.privKeypengirim = privateKeyPengirim;
        this.userMessagesList = userMessagesList;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng lastLocationPlaced = sydney;
        for (Location location: savedLocations)
        {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Lat:" + location.getLatitude()+"Lon:" + location.getLongitude());
            mMap.addMarker(markerOptions);

            lastLocationPlaced = latLng;
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationPlaced,12.0f));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //lest count the number of times the pin is clicked.

                Integer clicks = (Integer) marker.getTag();
                if (clicks == null)
                {
                    clicks = 0;
                }
                clicks++;
                marker.setTag(clicks);
                // Toast.makeText(MessageAdapter.this, "Marker" + marker.getTitle() + "was Clicked " + marker.getTag() + "Times.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    public class MessageViewHolder extends  RecyclerView.ViewHolder{
        private static final String AES1 = "kunci.text";
        public TextView senderMessageText, receiverMessageText;
        private Linkify linkify;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture,image_download;
        public Button Mapssender,Mapsreceiver;
        public PlayerView messageSenderVideo, messageReceiverVideo;
        public RelativeLayout relative_video_sender,relative_video_receiver;
        private VoicePlayerView voicePlayerView_receiver, voicePlayerView_sender;
        private AlarmManager alarmManager;
        private PendingIntent pendingIntent;
        private SimpleExoPlayer  simpleExoPlayer;
        private ImageButton select_video_sender, select_video_receiver;
        private OutputStream outputStream;;
        private ClipboardManager clipboard;
        private ClipData clipData;
        private String aes1 = "",aes = "";
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            Mapsreceiver = itemView.findViewById(R.id.maps_receiver);
            Mapssender = itemView.findViewById(R.id.maps_sender);
            messageSenderVideo = itemView.findViewById(R.id.video_sender);
            select_video_sender = itemView.findViewById(R.id.select_video_sender);
            select_video_receiver = itemView.findViewById(R.id.select_video_receiver);
            relative_video_sender = itemView.findViewById(R.id.relative_video_sender);
            relative_video_receiver = itemView.findViewById(R.id.relative_video_receiver);
            messageReceiverVideo = itemView.findViewById(R.id.video_receiver);
            voicePlayerView_receiver = itemView.findViewById(R.id.voicePlayerView_receiver);
            voicePlayerView_sender = itemView.findViewById(R.id.voicePlayerView_sender);
            image_download = itemView.findViewById(R.id.image_download);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.Mapsreceiver.setVisibility(View.GONE);
        messageViewHolder.Mapssender.setVisibility(View.GONE);
        messageViewHolder.messageReceiverVideo.setVisibility(View.GONE);
        messageViewHolder.messageSenderVideo.setVisibility(View.GONE);
        messageViewHolder.select_video_sender.setVisibility(View.GONE);
        messageViewHolder.select_video_receiver.setVisibility(View.GONE);
        messageViewHolder.relative_video_sender.setVisibility(View.GONE);
        messageViewHolder.relative_video_receiver.setVisibility(View.GONE);
        messageViewHolder.voicePlayerView_sender.setVisibility(View.GONE);
        messageViewHolder.voicePlayerView_receiver.setVisibility(View.GONE);
        messageViewHolder.image_download.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "foxandroidReminderChannel";
            String description = "Channel For Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("foxandroid",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(messageViewHolder.itemView.getContext(),NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        if (fromMessageType.equals("text")){

            Log.d("test","Kunci Private yang sudah paling akhir pengirim = " + privKeypengirim);
            String aesc = messages.getAes();
            try {
                byte[] datadecode = Base64.decode(aesc.getBytes(),Base64.DEFAULT);
                decodeData = rsa.decryptByPrivateKey(datadecode,privKeypengirim);
                String decodeStr = Arrays.toString(decodeData);
                Log.d("test","Hasil dekripsi RSA di pengirim dari firebase = " + decodeStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                cipher = Cipher.getInstance("AES");
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            secretKeySpec = new SecretKeySpec(decodeData, "AES");
            byte[] base64 = Base64.decode(messages.getMessage(),Base64.DEFAULT);
            String aesDecode = null;
            try {
                aesDecode = (AESDecryptionmethod(new String(base64)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            try {
                cipher = Cipher.getInstance("AES");
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            secretKeySpec1 = new SecretKeySpec(encryptionKey, "AES");
            byte[] base641 = Base64.decode(messages.getMessage(),Base64.DEFAULT);
            String aesDecode1 = null;
            try {
                aesDecode1 = (AESDecryptionmethod1(new String(base641)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.senderMessageText.setText(aesDecode1 + "\n \n" + messages.getTime() + " - " + messages.getDate());
                messageViewHolder.senderMessageText.setTextColor(R.color.defaultContentColor);
                messageViewHolder.linkify.addLinks(messageViewHolder.senderMessageText,Linkify.ALL);
                messageViewHolder.senderMessageText.setLinkTextColor(R.color.link_color);
                messageViewHolder.aes1 = aesDecode1;
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(aesDecode + "\n \n" + messages.getTime() + " - " + messages.getDate());
                messageViewHolder.linkify.addLinks(messageViewHolder.receiverMessageText,Linkify.ALL);
                messageViewHolder.receiverMessageText.setLinkTextColor(R.color.link_color);
                messageViewHolder.aes =aesDecode;

            }
        }

        else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                messageViewHolder.image_download = messageViewHolder.messageSenderPicture;
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
                messageViewHolder.image_download = messageViewHolder.messageReceiverPicture;

            }
        }

        else if (fromMessageType.equals("record")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.voicePlayerView_sender.setVisibility(View.VISIBLE);

                messageViewHolder.voicePlayerView_sender.setAudio(messages.getMessage());

            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.voicePlayerView_receiver.setVisibility(View.VISIBLE);

                messageViewHolder.voicePlayerView_receiver.setAudio(messages.getMessage());
            }
        }

        else if (fromMessageType.equals("maps")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.Mapssender.setVisibility(View.VISIBLE);
                messageViewHolder.Mapssender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("tes","longitude dalam" +messages.getLongitude());
                        Log.d("tes","latitude dalam" +messages.getLatitude());
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), RetrieveMapActivity.class);
                        intent.putExtra("longitude", userMessagesList.get(position).getLongitude());
                        intent.putExtra("latitude", userMessagesList.get(position).getLatitude());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
            else {
                String longitude = messages.getLongitude();
                Log.d("tes","longitude = " + longitude);
                String latitude = messages.getLatitude();
                Log.d("tes","latitude = " + latitude);
                double mlongitude = Double.parseDouble(longitude);
                double mlatitude = Double.parseDouble(latitude);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                messageViewHolder.Mapsreceiver.setVisibility(View.VISIBLE);
                messageViewHolder.Mapsreceiver.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("tes","longitude dalam" +messages.getLongitude());
                        Log.d("tes","latitude dalam" +messages.getLatitude());
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), RetrieveMapActivity.class);
                        intent.putExtra("longitude", userMessagesList.get(position).getLongitude());
                        intent.putExtra("latitude", userMessagesList.get(position).getLatitude());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        else if (fromMessageType.equals("video")){
            if (fromUserID.equals(messageSenderId)){

                messageViewHolder.simpleExoPlayer = new SimpleExoPlayer.Builder(messageViewHolder.itemView.getContext()).build();
                messageViewHolder.messageSenderVideo.setVisibility(View.VISIBLE);
                messageViewHolder.select_video_sender.setVisibility(View.VISIBLE);
                messageViewHolder.relative_video_sender.setVisibility(View.VISIBLE);
                messageViewHolder.messageSenderVideo.setPlayer(messageViewHolder.simpleExoPlayer);
                Uri video = Uri.parse(messages.getMessage());
                MediaItem mediaItem = MediaItem.fromUri(video);
                messageViewHolder.simpleExoPlayer.addMediaItem(mediaItem);
                messageViewHolder.simpleExoPlayer.prepare();

                messageViewHolder.select_video_sender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "View Video",
                                "Download This Video",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), VideoViewerActivity.class);
                                    intent.putExtra("videoview", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messages.getMessage()));
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                            DownloadManager.Request.NETWORK_MOBILE);
                                    request.setTitle("Download");
                                    request.setDescription("Downloading File...");
                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("/Dude/Video/",""+System.currentTimeMillis()+".MPEG-4");

                                    DownloadManager manager = (DownloadManager)messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                }
                                else if (which == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                });

            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.simpleExoPlayer = new SimpleExoPlayer.Builder(messageViewHolder.itemView.getContext()).build();
                messageViewHolder.messageReceiverVideo.setVisibility(View.VISIBLE);
                messageViewHolder.relative_video_receiver.setVisibility(View.VISIBLE);
                messageViewHolder.select_video_receiver.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverVideo.setPlayer(messageViewHolder.simpleExoPlayer);
                Uri video = Uri.parse(messages.getMessage());
                MediaItem mediaItem = MediaItem.fromUri(video);
                messageViewHolder.simpleExoPlayer.addMediaItem(mediaItem);
                messageViewHolder.simpleExoPlayer.prepare();
                messageViewHolder.select_video_receiver.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "View Video",
                                "Download This Video",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), VideoViewerActivity.class);
                                    intent.putExtra("videoview", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messages.getMessage()));
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                            DownloadManager.Request.NETWORK_MOBILE);
                                    request.setTitle("Download");
                                    request.setDescription("Downloading File...");

                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("/Dude/Video/",""+System.currentTimeMillis()+".MPEG-4");

                                    DownloadManager manager = (DownloadManager)messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                }
                                else if (which == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }
        }

        if (fromUserID.equals(messageSenderId)){
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View This Document",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){

                                }
                                else if (which == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for Everyone",
                                "Copy"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 2){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3){
                                    messageViewHolder.clipboard = (ClipboardManager) messageViewHolder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    messageViewHolder.clipData = ClipData.newPlainText("text", messageViewHolder.aes1);
                                    messageViewHolder.clipboard.setPrimaryClip(messageViewHolder.clipData);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Delete for Everyone",
                                "Download"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 4){
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messages.getMessage()));
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                            DownloadManager.Request.NETWORK_MOBILE);
                                    request.setTitle("Download");
                                    request.setDescription("Downloading File...");

                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("/Dude/Image/",""+System.currentTimeMillis()+".jpg");

                                    DownloadManager manager = (DownloadManager)messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("maps")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }
        else {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View This Document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Copy"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 2){
                                    messageViewHolder.clipboard = (ClipboardManager) messageViewHolder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    messageViewHolder.clipData = ClipData.newPlainText("text", messageViewHolder.aes);
                                    messageViewHolder.clipboard.setPrimaryClip(messageViewHolder.clipData);
                                }

                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("maps")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Donwload"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3){
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messages.getMessage()));
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                            DownloadManager.Request.NETWORK_MOBILE);
                                    request.setTitle("Download");
                                    request.setDescription("Downloading File...");

                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("/Dude/Image/",""+System.currentTimeMillis()+".jpg");

                                    DownloadManager manager = (DownloadManager)messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                }
                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String AESDecryptionmethod(String string) throws UnsupportedEncodingException {
        byte[] EncrytedByte = string.getBytes("ISO-8859-1");
        String decrytedString = string;

        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncrytedByte);
            decrytedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decrytedString;
    }

    private String AESDecryptionmethod1(String string) throws UnsupportedEncodingException {
        byte[] EncrytedByte = string.getBytes("ISO-8859-1");
        String decrytedString = string;

        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec1);
            decryption = decipher.doFinal(EncrytedByte);
            decrytedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decrytedString;
    }

}
