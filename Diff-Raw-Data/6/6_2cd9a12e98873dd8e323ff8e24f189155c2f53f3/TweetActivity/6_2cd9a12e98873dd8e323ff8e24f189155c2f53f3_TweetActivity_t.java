 package com.twitter.yamba;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
import com.marakana.android.yamba.clientlib.YambaClient;

 public class TweetActivity extends Activity {
     private Button tweetButton;
     private EditText tweetText;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tweet);
 
         tweetButton = (Button)findViewById(R.id.tweet_button);
         tweetText = (EditText)findViewById(R.id.tweet_text);
 
         tweetButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                YambaClient twitterService = new YambaClient("student","password");

                 Log.d("TweetActivity", "onClicked");
             }
         });
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.tweet, menu);
         return true;
     }
     
 }
