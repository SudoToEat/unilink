package com.teamxod.unilink.user;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamxod.unilink.R;
import com.teamxod.unilink.house.House;
import com.teamxod.unilink.house.HousePostActivity;

import java.util.ArrayList;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.MyPostViewHolder> {
    private final ArrayList<String> postList;

    private final Context context;

    private final DatabaseReference myPostReference;

    private final DatabaseReference housePostReference;

    private RecyclerView mRecyclerView;

    private boolean isChecked;

    MyPostAdapter(Context context, ArrayList<String> postList, boolean isChecked) {
        this.postList = postList;
        this.context = context;
        this.isChecked = isChecked;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myPostReference = database.child("Users").child(uid).child("my_house_posts");
        housePostReference = database.child("House_post");
    }

    public void setFlag(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public MyPostAdapter.MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.house_list_item_mypost, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                String postID = postList.get(itemPosition);
                Intent myIntent = new Intent(context, HousePostActivity.class);
                myIntent.putExtra("postID", postID);
                context.startActivity(myIntent);
            }
        });

        return new MyPostAdapter.MyPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyPostViewHolder holder, final int position) {

        String postID = postList.get(position);

        if (isChecked) {
            holder.delete_btn.setVisibility(View.VISIBLE);
            holder.post_btn.setVisibility(View.VISIBLE);
        } else {
            holder.delete_btn.setVisibility(View.GONE);
            holder.post_btn.setVisibility(View.GONE);
        }

        housePostReference.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                House house = dataSnapshot.getValue(House.class);
                if (house == null) return;
                String price = "$ " + house.getRooms().get(0).getPrice() + "/Mo From " + house.getStartDate();

                Glide.with(context)
                        .load(house.getPictures().get(0))
                        .into(holder.housePictureView);
                holder.titleTextView.setText(house.getTitle());
                holder.priceTextView.setText(price);
                holder.addressTextView.setText(house.getLocation());
                holder.post_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String postID = postList.get(holder.getAdapterPosition());
                        Intent myIntent = new Intent(context, EditPostActivity.class);
                        myIntent.putExtra("postID", postID);
                        context.startActivity(myIntent);
                    }
                });
                holder.delete_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String postID = postList.get(holder.getAdapterPosition());
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Are you sure you want to delete this post? The action cannot be reversed")
                                .setTitle("Delete Post");
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                housePostReference.child(postID).removeValue();
                                postList.remove(postID);
                                notifyItemRemoved(holder.getAdapterPosition());
                                myPostReference.setValue(postList);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyPostViewHolder extends RecyclerView.ViewHolder {
        final Button delete_btn;
        final Button post_btn;
        final ImageView housePictureView;
        final TextView titleTextView;
        final TextView priceTextView;
        final TextView addressTextView;

        MyPostViewHolder(View itemView) {
            super(itemView);
            post_btn = itemView.findViewById(R.id.post_btn);
            delete_btn = itemView.findViewById(R.id.delete_btn);
            titleTextView = itemView.findViewById(R.id.post_title);
            priceTextView = itemView.findViewById(R.id.post_price);
            addressTextView = itemView.findViewById(R.id.post_address);
            housePictureView = itemView.findViewById(R.id.post_image);
        }
    }
}
