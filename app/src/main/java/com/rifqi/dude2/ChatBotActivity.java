package com.rifqi.dude2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity {
    private RecyclerView chatsRV;
    private EditText userMsgEdt;
    private ImageButton sendMsgFAB;
    private final String BOT_KEY = "bot";
    private final String USER_KEY = "user";
    private ArrayList<ChatModal> chatModalArrayList;
    private ChatRVAdapter chatRVAdapter;
    private Toolbar ChatToolBar;
    private ImageButton back_home_bot;
    private CircleImageView userImage;
    private TextView userName, userLastSeen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        chatsRV = findViewById(R.id.idRVChats);
        userMsgEdt = findViewById(R.id.idEdtMessage);
        sendMsgFAB = findViewById(R.id.idFABSend);
        chatModalArrayList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatModalArrayList,this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(chatRVAdapter);
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar_bot);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar_bot, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image_bot);
        userName = (TextView) findViewById(R.id.custom_profile_name_bot);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen_bot);

        userLastSeen.setText("Online");
        userName.setText("Bot");
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/dude-baru-f609a.appspot.com/o/Profile%20Images%2Fchatbot.png?alt=media&token=48934982-2eaf-48f9-b86c-f2ac79c2fcb2").
                into(userImage);
        sendMsgFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMsgEdt.getText().toString().isEmpty()){
                    Toast.makeText(ChatBotActivity.this, "Please Enter your message", Toast.LENGTH_SHORT).show();
                    return;
                }
                getResponse(userMsgEdt.getText().toString());
                userMsgEdt.setText("");
            }
        });
        back_home_bot = findViewById(R.id.back_home_bot);
        back_home_bot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatBotActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void getResponse(String message){
        chatModalArrayList.add(new ChatModal(message,USER_KEY));
        chatRVAdapter.notifyDataSetChanged();
        String url = "http://api.brainshop.ai/get?bid=159376&key=SXnTcDwFKQjjvsGa&uid=[uid]&msg="+message;
        String BASE_URL = "http://brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModal> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModal>() {
            @Override
            public void onResponse(Call<MsgModal> call, Response<MsgModal> response) {
                if (response.isSuccessful()){
                    MsgModal modal = response.body();
                    chatModalArrayList.add(new ChatModal(modal.getCnt(),BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<MsgModal> call, Throwable t) {
                chatModalArrayList.add(new ChatModal("plase revert your question", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
            }
        });
    }

}