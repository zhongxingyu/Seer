 package edu.voicelabs.vst;
 
import java.io.File;

 import android.app.Activity;
import android.net.Uri;
 import android.os.Bundle;
import android.widget.VideoView;
 
 public class TutorialVideo extends Activity {
 	
	private VideoView videoView;
	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.tutorial_movie);
 		
		videoView = (VideoView) findViewById(R.id.tutorialVideo);		

        // This is the name of the video WITHOUT the file extension.
        // In this example, the name of the video is 'test.mp4'
        String videoName = "tutorial_intro";

        // You build the URI to your video here
        StringBuilder uriPathBuilder = new StringBuilder ();
        uriPathBuilder.append ("android.resource://");
        uriPathBuilder.append (this.getPackageName ());
        uriPathBuilder.append (File.separator);
        uriPathBuilder.append ("raw");
        uriPathBuilder.append (File.separator);
        uriPathBuilder.append (videoName);
        Uri uri = Uri.parse (uriPathBuilder.toString ());

        videoView.setVideoURI(uri);
        videoView.start();
        
		
//		MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.tutorial_intro);
//		mediaPlayer.start();
 	}
 }
