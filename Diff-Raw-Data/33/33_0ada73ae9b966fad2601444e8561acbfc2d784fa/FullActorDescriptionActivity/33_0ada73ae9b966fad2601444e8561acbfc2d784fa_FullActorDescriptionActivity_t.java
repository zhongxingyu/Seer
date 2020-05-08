 package com.watchlistapp.fullmoviedescription;
 
 import android.os.Bundle;
 import android.support.v7.app.ActionBarActivity;
 import android.widget.TextView;
 
 import com.makeramen.RoundedImageView;
 import com.textjustifylibrary.TextViewEx;
 import com.watchlistapp.R;
 
 public class FullActorDescriptionActivity extends ActionBarActivity {
 
     private RoundedImageView actorAvatarImageView;
     private TextView actorNameTextView;
     private TextView actorBirthdayTextView;
     private TextView actorPlaceOfBirthTextView;
     private TextView actorHomePageTextView;
     private TextViewEx actorBiographyTextView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_full_actor_description);
 
        String actorName = getIntent().getStringExtra("actorName");
        getSupportActionBar().setTitle(actorName);

         actorAvatarImageView = (RoundedImageView)findViewById(R.id.full_actor_description_avatar);
         actorNameTextView = (TextView)findViewById(R.id.full_actor_description_name);
         actorBirthdayTextView = (TextView)findViewById(R.id.full_actor_description_birthday);
         actorPlaceOfBirthTextView = (TextView)findViewById(R.id.full_actor_description_place_of_birth);
         actorHomePageTextView = (TextView)findViewById(R.id.full_actor_description_homepage);
         actorBiographyTextView = (TextViewEx)findViewById(R.id.full_actor_description_biography);
         String actorId = getIntent().getStringExtra("actorId");
 
         FullActorDescriptionLoader fullActorDescriptionLoader = new FullActorDescriptionLoader(actorId, actorAvatarImageView, actorNameTextView, actorBirthdayTextView,
                 actorPlaceOfBirthTextView, actorHomePageTextView, actorBiographyTextView);
         fullActorDescriptionLoader.execute();
     }
 }
