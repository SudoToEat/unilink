package com.teamxod.unilink;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.progresviews.ProgressWheel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamxod.unilink.user.User;

import java.util.ArrayList;
import java.util.Calendar;

public class RoommatePostActivity extends AppCompatActivity {

    private String userUID;
    private String myUID;
    private User user;

    private DatabaseReference userReference;
    private DatabaseReference preferenceReference;

    private final static int MARGIN = 15;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roommate_post);

        Intent intent = getIntent();

        if(intent.hasExtra("uid")){
            userUID = intent.getExtras().getString("uid");
        }else{
            Toast.makeText(this, "User does not exist", Toast.LENGTH_LONG).show();
            finish();
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        userReference = database.child("Users").child(userUID);
        preferenceReference = database.child("Preference");
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadData();

    }

    private void loadData() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        preferenceReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                preference myPreference = dataSnapshot.child(myUID).getValue(preference.class);
                preference userPreference = dataSnapshot.child(userUID).getValue(preference.class);
                ArrayList<String> tags;
                double score;
                if(myPreference != null && userPreference != null) {
                    Recommendation recommendation = new Recommendation(myPreference, userPreference);
                    tags = recommendation.getTagList();
                    score = recommendation.getScore();
                } else {
                    tags = new ArrayList<>();
                    score = 0;
                }
                setScoreAndTags(score,tags);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void setScoreAndTags(double score, ArrayList<String> tags){

        TextView ScoreTextView = (TextView) findViewById(R.id.roommate_score);

        ProgressWheel pw = (ProgressWheel) findViewById(R.id.roommate_progress);

        pw.setPercentage((int)(score * 36));

        TagViewGroup tagGroup = findViewById(R.id.tag);

        if(score != 0)
            ScoreTextView.setText(String.valueOf(score));
        else
            ScoreTextView.setText("?");

        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = MARGIN;
        lp.rightMargin = MARGIN;
        lp.topMargin = MARGIN;
        lp.bottomMargin = MARGIN;
        for(int i = 0; i < tags.size(); i++){
            TextView tagView = new TextView(this);
            tagView.setText(tags.get(i));
            tagView.setTextAppearance(R.style.tag_text);

            tagView.setBackgroundResource(R.drawable.roommate_tag_layout);
            tagGroup.addView(tagView,lp);
        }
    }

    private void updateUI() {
        TextView name = (TextView) findViewById(R.id.roommate_name);
        TextView gender = (TextView) findViewById(R.id.roommate_gender);
        TextView schoolYear = (TextView) findViewById(R.id.school_year);
        TextView graduateYear = (TextView) findViewById(R.id.graduate_year);
        TextView description = (TextView) findViewById(R.id.roommate_description);
        ImageView picture = (ImageView) findViewById(R.id.poster_picture);

        name.setText(user.getName());
        gender.setText(user.getGender());
        int enterYear = Integer.parseInt(user.getYearGraduate()) - 4;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR) - enterYear;
        String temp;
        if(currentYear > 5)
            temp = "Alumni";
        else
            temp = currentYear + "th Year";
        schoolYear.setText(temp);
        temp = currentYear + "th Year";
        graduateYear.setText(temp);
        description.setText(user.getDescription());
        Glide.with(this)
                .load(user.getPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(picture);
    }
}
