package com.rifqi.dude2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WallpaperActivity extends AppCompatActivity {

    private String messageReceiverID;
    private ImageButton btnred,btnorange,btngreen,btnselect;
    private String SHARED_PREFS = "codeTheme";
    private Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        btnred = findViewById(R.id.btnred);
        btnorange = findViewById(R.id.btnorange);
        btngreen = findViewById(R.id.btngreen);
        btnselect = findViewById(R.id.btnselect);

        btnorange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save to local storage
                String themeku = "";
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(themeku,"orange");
                editor.apply();

                Intent a =  new Intent( WallpaperActivity.this,MainActivity.class);
                startActivity(a);

            }
        });

        btnred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save to local storage
                String themeku = "";
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(themeku,"red");
                editor.apply();

                Intent a =  new Intent( WallpaperActivity.this,MainActivity.class);
                startActivity(a);

            }
        });
        btngreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save to local storage
                String themeku = "";
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(themeku,"green");
                editor.apply();

                Intent a =  new Intent( WallpaperActivity.this,MainActivity.class);
                startActivity(a);

            }
        });

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select Image"), 438);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            fileUri = data.getData();


                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String imageString = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                Log.d("test","Cek String Gambar = "+ imageString);
                String themeku = "";
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(themeku,imageString);
                editor.apply();

                Intent a =  new Intent( WallpaperActivity.this,MainActivity.class);
                startActivity(a);

        }

    }
}