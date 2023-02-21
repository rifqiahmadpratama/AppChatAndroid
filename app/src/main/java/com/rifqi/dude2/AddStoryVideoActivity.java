package com.rifqi.dude2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private Uri videoUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story_video);

        storageReference = FirebaseStorage.getInstance().getReference("story");

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_VIDEO_REQUEST);

    }

    private void Uploadvideo() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (videoUri != null){
            final StorageReference videoRefrence = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(videoUri));

            storageTask = videoRefrence.putFile(videoUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return videoRefrence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myid);

                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis()+86400000; //1 day

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("videourl", myUrl);
                        hashMap.put("imageurl", "");
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend", timeend);
                        hashMap.put("storyid", storyid);
                        hashMap.put("userid", myid);

                        reference.child(storyid).setValue(hashMap);
                        pd.dismiss();

                        finish();
                    } else {
                        Toast.makeText(AddStoryVideoActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryVideoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Video selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            videoUri = data.getData();

            Uploadvideo();
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryVideoActivity.this, MainActivity.class));
            finish();
        }
    }
}