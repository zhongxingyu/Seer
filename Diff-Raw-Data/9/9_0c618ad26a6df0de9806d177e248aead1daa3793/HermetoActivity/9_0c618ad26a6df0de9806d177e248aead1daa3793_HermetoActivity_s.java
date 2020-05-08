 package net.thiagoalz.hermeto;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 
 public class HermetoActivity extends Activity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 		configureScreen();
 		setContentView(R.layout.presentation);
		
		ImageButton play = (ImageButton) findViewById(R.id.play);
		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				
			}
		});
     }
     
     private void configureScreen() {
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 }
