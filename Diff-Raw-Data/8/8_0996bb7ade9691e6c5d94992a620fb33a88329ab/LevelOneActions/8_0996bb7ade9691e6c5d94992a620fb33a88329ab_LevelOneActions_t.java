 package com.redcarddev.kickshot;
 
 import java.util.Random;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Created by otternq on 10/27/13.
  */
 public class LevelOneActions extends Activity {
 
     String LOGTAG = this.getClass().getName();
 
     static Random r = new Random();
 
     final static int COMPUTER_SCORED = 1;
     final static int COMPUTER_BLOCKED = 3;
     final static int COMPUTER_SHOT = 5;
     final static int COMPUTER_INTERCEPT = 7;
     final static int COMPUTER_TURNOVER = 9;
 
     final static int PLAYER_SCORED = 0;
     final static int PLAYER_BLOCKED = 2;
     final static int PLAYER_SHOT = 4;
     final static int PLAYER_INTERCEPT = 6;
     final static int PLAYER_TURNOVER = 8;
 
     protected int state = -1;
     protected int whichSide;
 
     protected TextView actionText;
     protected ImageView actionImage;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.level_one_actions);
 
         Intent mIntent = getIntent();
         this.state = mIntent.getIntExtra("state", -1);
         this.whichSide = r.nextInt(100);
 
         this.actionText = (TextView)findViewById(R.id.actionText);
         this.actionImage = (ImageView)findViewById(R.id.actionImage);
 
         this.setActionView();
 
     }
 
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.finish();
        return true;
    }

     protected boolean setActionView() {
 
         String action = "";
         // every time this is called randomize if it's a shot to the left or right
         switch (this.state) {
             case LevelOneActions.COMPUTER_SCORED:
                 action = "They scored!";
                 break;
             case LevelOneActions.COMPUTER_BLOCKED:
                 action = "They blocked your shot!";
                 if(this.whichSide >= 50){
                     this.actionImage.setImageResource(R.drawable.away_block_left);
                 }
                 else{
                     this.actionImage.setImageResource(R.drawable.away_block_right);
                 }
                 break;
             case LevelOneActions.COMPUTER_SHOT:
                 action = "They are shooting!";
                 if(this.whichSide >= 50){
                     this.actionImage.setImageResource(R.drawable.away_shot_left);
                 }
                 else{
                     this.actionImage.setImageResource(R.drawable.away_shot_left);
                 }
                 break;
             case LevelOneActions.COMPUTER_INTERCEPT:
                 action = "They intercepted the ball!";
                 this.actionImage.setImageResource(R.drawable.away_intercept);
                 break;
             case LevelOneActions.COMPUTER_TURNOVER:
                 action = "They turned over the ball!";
                 this.actionImage.setImageResource(R.drawable.home_intercept);
                 break;
             case LevelOneActions.PLAYER_SCORED:
                 action = "You scored!";
                 break;
             case LevelOneActions.PLAYER_BLOCKED:
                 action = "You blocked their shot!";
                 if(this.whichSide >= 50){
                     this.actionImage.setImageResource(R.drawable.home_block_left);
                 }
                 else{
                     this.actionImage.setImageResource(R.drawable.home_block_right);
                 }
                 break;
             case LevelOneActions.PLAYER_SHOT:
                 action = "You are shooting!";
                 this.actionImage.setImageResource(R.drawable.home_shot_left);
                 if(this.whichSide >= 50){
                     this.actionImage.setImageResource(R.drawable.home_shot_left);
                 }
                 else{
                     this.actionImage.setImageResource(R.drawable.home_shot_right);
                 }
                 break;
             case LevelOneActions.PLAYER_INTERCEPT:
                 action = "You intercepted the ball!";
                 this.actionImage.setImageResource(R.drawable.home_intercept);
                 break;
             case LevelOneActions.PLAYER_TURNOVER:
                 action = "You turned the ball over";
                 this.actionImage.setImageResource(R.drawable.away_intercept);
                 break;
             default:
                 return false;
         }
 
         this.setActionText(action);
         //this.setActionImage(image);
 
         return true;
 
     }
 
     protected boolean setActionImage(String image) {
         return false;
     }
 
     protected boolean setActionText(String action) {
 
         this.actionText.setText(action);
 
         return true;
 
     }
 
 
 
 }
