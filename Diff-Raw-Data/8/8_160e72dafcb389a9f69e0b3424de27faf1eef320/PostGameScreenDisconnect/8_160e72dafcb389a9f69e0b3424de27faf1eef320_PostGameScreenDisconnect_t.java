 package com.example.zootypers.ui;
 
 import android.annotation.SuppressLint;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.example.zootypers.R;
 
 /**
  * Post game activity / UI for a multiplayer game, when the opponent has disconneted.
  */
 public class PostGameScreenDisconnect extends PostGameScreenMulti {
 
   @SuppressLint("NewApi")
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     // Get & display background
     setContentView(R.layout.activity_pregame_selection_multi);
     Drawable background = ((ImageButton)
         findViewById(getIntent().getIntExtra("bg", 0))).getDrawable();
 
    setContentView(R.layout.activity_post_game_screen_disconnect);
     findViewById(R.id.postgame_layout).setBackground(background);
 
     // Get and display score
     score = getIntent().getIntExtra("score", 0);
     TextView finalScore = (TextView) findViewById(R.id.final_score);
     finalScore.setText(score.toString());
 
     // Get and store the username
     username = getIntent().getStringExtra("username");
   }
 
   @Override
   protected void opponentDisplay() {
     // Do nothing
   }
 
 }
