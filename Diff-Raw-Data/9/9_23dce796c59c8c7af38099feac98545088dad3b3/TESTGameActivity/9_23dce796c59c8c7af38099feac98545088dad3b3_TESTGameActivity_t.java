 package edu.pugetsound.vichar;
 
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.support.v4.app.Fragment;
 //Import Fragment dependencies
 import android.support.v4.app.FragmentActivity;
 
 /**
  * The in-game Activity
  * Extends FragmentActivity to ensure support for Android 2.x
  */
 public class TESTGameActivity extends FragmentActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("GameAct", "About to set content view");
         setContentView(R.layout.activity_game_test);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         //set default twitter prompt string on Tweet fragment
         TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.tweet_fragment);
         tweetFrag.setPrompt(getString(R.string.default_twitter_prompt));
     }
     
     /**
      * Sends tweet
      * @param view
      */
     public void sendTweet(View view)
     {
     	TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.tweet_fragment);
         tweetFrag.sendTweet(view);	
     }
 
 }
