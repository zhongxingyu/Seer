 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.video;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.entities.Video;
 
 /**
  *
  * @author johan
  */
 public class VideoActivity extends Activity {
 
     //Global variables
     String videoID;
     String videoName;
     String videoDescription;
     String videoURL;
     
     
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //Set layout
         setContentView(R.layout.video_detail);
         
                 
         //Get extras from previous activity
         //Gets data based on a key represented by a string
         Bundle extras = getIntent().getExtras();
         videoID = extras.getString("videoID");
         videoName = extras.getString("videoName");
         videoDescription = extras.getString("itemDescription");
         videoURL = extras.getString("videoURL");
         
         //Print some stuff based on extras from previous activity
         String SrcPath = "http://daily3gp.com/vids/747.3gp";
         VideoView myVideoView = (VideoView)findViewById(R.id.myvideoview);
         myVideoView.setVideoURI(Uri.parse(SrcPath));
         myVideoView.setMediaController(new MediaController(this));
         myVideoView.requestFocus();
         myVideoView.start();
         
         //Create button for youtube
         Button youtubeButton = (Button) findViewById(R.id.youtubeapp);
         youtubeButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
                 
                 startYoutubeApp("test");
                 
             }
         });       
         
         //Video ID
         TextView textVideoID = (TextView) findViewById(R.id.videoID);
         textVideoID.setText(videoID);
         
         
         //Video Title
         TextView textVideoName = (TextView) findViewById(R.id.videotitle);
         textVideoName.setText(videoName);
         
         //Video Description
         TextView textVideoDescription = (TextView) findViewById(R.id.videodescription);
         textVideoDescription.setText(videoDescription);
                 
         
         
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
     
     /**
      * This is called once the options menu is first displayed.
      * This is an overridden method.
      * 
      * @param menu The Menu where items are placed.
      * @return 
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.video_activity, menu);
         return true;
     }
     
     /**
      * This method is called whenever an item in the options menu is selected.
      * 
      * This is an overridden method.
      * 
      * @param item The item that was selected.
      * @return 
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_download:
                 
                 Toast.makeText(this, "Downloading video...", Toast.LENGTH_SHORT).show();
                 
                 
                 //NYI
                 return true;
                 
             case R.id.menu_favourite:
                 
                 Toast.makeText(this, "Video added to favourites!", Toast.LENGTH_SHORT).show();
                 
                 //NYI
                 return true;
             case R.id.menu_settings:
                 
                 Toast.makeText(this, "Menu clicked.", Toast.LENGTH_SHORT).show();
                 
                 //NYI
                 return true;    
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * This function sets an onClickListener for a button resource, where the supplied
      * action is a class to be started as an intent.
      * 
      * @param buttonid the integer value of the buttons ID
      * @param action the class to be started as an intent
      * @param uri The string value of the URI 
      */
     private void createButton(int buttonid, final Class action, final String uri) {
         Button button = (Button) findViewById(buttonid);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 Intent myIntent = new Intent(view.getContext(), action);
                 myIntent.putExtra("uri", uri);
                 startActivityForResult(myIntent, 0);
             }
         });
     }
     
     
     
     public void startYoutubeApp(String URI){
         
         
         
         System.out.println(videoURL);
         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoURL)));
         
         
     }
     
     /**
      * This method returns a Video based on the string value of it's ID.
      * 
      * 
      * @param videoID
      * @return Video
      */
     public Video getVideoData(String videoID) {
         
         //NYI
         return new Video(videoID, videoID, videoID, videoID, videoID, null, videoID);
     }
 
 }
