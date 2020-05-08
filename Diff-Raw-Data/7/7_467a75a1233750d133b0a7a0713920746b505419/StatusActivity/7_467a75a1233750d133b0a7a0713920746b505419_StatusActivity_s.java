 package com.lohika.yambla;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 
 public class StatusActivity extends Activity implements View.OnClickListener {
 
    Twitter twitter;
     private EditText editText;
 
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState If the activity is being re-initialized after
      *                           previously being shut down then this Bundle contains the data it most
      *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.status);
 
         editText = (EditText) findViewById(R.id.status_edit);
 
         Button updateButton = (Button) findViewById(R.id.status_button);
         updateButton.setOnClickListener(this);
 
         twitter = new Twitter("askme", "123456");
         twitter.setAPIRootUrl("http://yamba.marakana.com/api");
     }
 
     @Override
     public void onClick(View view) {
         String status = editText.getText().toString();
         new PostToTwitter().execute(status);
     }
 
    class PostToTwitter extends AsyncTask<String, Integer, String> {
 
         @Override
         protected String doInBackground(String... statuses) {
             try {
                 winterwell.jtwitter.Status status = twitter.updateStatus(statuses[0]);
                 return status.text;
 
             } catch (TwitterException e) {
                 e.printStackTrace();
                 return "Failed to post!";
             }
         }
 
         @Override
         protected void onPostExecute(String result) {
             Toast.makeText(StatusActivity.this, result, Toast.LENGTH_SHORT).show();
         }
     }
 }
 
