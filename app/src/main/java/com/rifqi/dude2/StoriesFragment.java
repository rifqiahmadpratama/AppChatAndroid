package com.rifqi.dude2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    private DatabaseReference ChatsRef;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private List<String> followingList;

//    public StoriesFragment() {
//        // Required empty public constructor
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stories, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView.setAdapter(storyAdapter);
        followingList = new ArrayList<>();

        checkFollowing();
        return view;
    }

    private void checkFollowing(){
        followingList = new ArrayList<>();


        ChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    followingList.add(snapshot1.getKey());
                }

                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("","",0,0,"",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));
                for (String id : followingList){
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot snapshot1 : snapshot.child(id).getChildren()){
                        story = snapshot1.getValue(Story.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                            Log.d("tes","Waktu awal = " +story.getTimestart());
                            countStory++;
                        }
                    }
                    if (countStory > 0 ){
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}