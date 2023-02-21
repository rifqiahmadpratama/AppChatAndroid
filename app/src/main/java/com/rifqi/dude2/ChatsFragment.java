package com.rifqi.dude2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef, NotifRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Dialog dialog;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        dialog = new Dialog(getActivity());
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        NotifRef = FirebaseDatabase.getInstance().getReference().child("Notification");
        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("image")){
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);

                                    if (dataSnapshot.child("userState").hasChild("state")){
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            holder.userStatus.setText("online");
                                            NotifRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.child(currentUserID).hasChild("message")){
                                                        String state = snapshot.child(currentUserID).child("message").getValue().toString();

                                                        if (state.equals("ketik")){
                                                            holder.userStatus.setText("Sedang Mengetik...");
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
                                        else if (state.equals("offline")){
                                            holder.userStatus.setText("Last Seen: " + date + " " + time);

                                            NotifRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.child(currentUserID).hasChild("message")){
                                                        String state = snapshot.child(currentUserID).child("message").getValue().toString();

                                                        if (state.equals("ketik")){
                                                            holder.userStatus.setText("Sedang Mengetik...");
                                                        }
                                                        else if (state.equals("tidak")){
                                                            UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.child("userState").hasChild("status")){
                                                                        String status = snapshot.child("userState").child("status").getValue().toString();
                                                                        if (status.equals("online")){
                                                                            holder.userStatus.setText("online");
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
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
                                        holder.userStatus.setText("offline");
                                    }

                                    if (retName.equals("Bot")){
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @SuppressLint("ResourceAsColor")
                                            @Override
                                            public void onClick(View v) {
                                                Intent chat = new Intent(holder.itemView.getContext(), ChatBotActivity.class);
                                                holder.itemView.getContext().startActivity(chat);

                                                holder.itemView.setBackgroundColor(R.color.bot_back_color);
                                            }
                                        });
                                    }
                                    else {
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @SuppressLint("ResourceAsColor")
                                            @Override
                                            public void onClick(View v) {
                                                Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                                chatIntent.putExtra("visit_user_id", usersIDs);
                                                chatIntent.putExtra("visit_user_name", retName);
                                                chatIntent.putExtra("visit_image", retImage[0]);
                                                holder.itemView.getContext().startActivity(chatIntent);

                                                holder.itemView.setBackgroundColor(R.color.bot_back_color);
                                            }
                                        });
                                    }

                                    if (!retName.equals("Bot")){
                                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //  Toast.makeText(getActivity(), "cek cek", Toast.LENGTH_SHORT).show();
                                                dialog.setContentView(R.layout.chat_layout_dialog);
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                ImageView profileImage1 = dialog.findViewById(R.id.image_view_chat_main_activity);
                                                TextView name = dialog.findViewById(R.id.name_main_activity);
                                                name.setText(retName);
                                                Picasso.get().load(retImage[0]).into(profileImage1);
                                                dialog.show();

                                                ImageButton chat_open_main_activity = dialog.findViewById(R.id.chat_open_main_activity);
                                                chat_open_main_activity.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id", usersIDs);
                                                        chatIntent.putExtra("visit_user_name", retName);
                                                        chatIntent.putExtra("visit_image", retImage[0]);
                                                        holder.itemView.getContext().startActivity(chatIntent);
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userStatus, userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);

        }
    }
}
