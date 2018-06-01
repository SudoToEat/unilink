package com.teamxod.unilink;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyFavoriteFragment extends Fragment {

    private DatabaseReference mDatabase;

    private ArrayList<String> postList;

    private FavoriteAdapter favoriteListAdapter;

    private RecyclerView houseListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_my_favorite, container, false);

        setupButton(layout);

        loadData(layout);

        return layout;
    }

    private void setupButton(View layout) {
        ImageView mBackButton = layout.findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Fragment fragment = getFragmentManager().findFragmentByTag("favorite");
                getFragmentManager()
                        .beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        });
    }

    private void loadData(View layout) {

        houseListView = (RecyclerView) layout.findViewById(R.id.my_favorite_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        houseListView.setLayoutManager(layoutManager);
        houseListView.setHasFixedSize(true);
        houseListView.addItemDecoration(new DividerItemDecoration(houseListView.getContext(), DividerItemDecoration.VERTICAL));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference favoriteReference = mDatabase.child("Users").child(uid).child("favorite_houses");
        favoriteReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postID : dataSnapshot.getChildren()) {
                    postList.add(postID.getValue(String.class));
                }
                favoriteListAdapter = new FavoriteAdapter(getActivity(), postList);
                houseListView.setAdapter(favoriteListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode()); }
        });
    }
}
