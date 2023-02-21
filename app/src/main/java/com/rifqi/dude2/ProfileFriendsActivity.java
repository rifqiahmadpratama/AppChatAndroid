package com.rifqi.dude2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ProfileFriendsActivity extends AppCompatActivity {

    private ImageButton back_home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friends);
        back_home  = findViewById(R.id.back_home);
        back_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFriendsActivity.this.finish();
            }
        });
    }
}