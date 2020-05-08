 package pl.mobilization.speakermeter;
 
 import pl.mobilization.speakermeter.speakers.SpeakerListActivity;
 import roboguice.activity.RoboSplashActivity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class SplashScreenActivity extends RoboSplashActivity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    setContentView(R.layout.splash);
	
	    Intent intent = new Intent(this, SpeakerListActivity.class);
		startActivity(intent);
 	}

 	@Override
 	protected void startNextActivity() {
 		startActivity(new Intent(this, SpeakerListActivity.class));
 	}
 
 }
