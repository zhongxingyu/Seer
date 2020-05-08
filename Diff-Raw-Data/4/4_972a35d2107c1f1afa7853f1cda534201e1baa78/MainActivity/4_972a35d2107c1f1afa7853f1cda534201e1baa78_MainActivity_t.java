 package com.september.tableroids;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.SurfaceView;
 import android.view.Window;
 import android.view.WindowManager;
 
 import com.google.ads.AdView;
 import com.september.tableroids.statics.GameBuilder;
 import com.september.tableroids.statics.Scorer;
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
 	
 	private AdView adView;
 	
 	
     public AdView getAdView() {
 		return adView;
 	}
 
 
 	public void setAdView(AdView adView) {
 		this.adView = adView;
 	}
 	
 	@Override
 	  public void onDestroy() {
 	    if (adView != null) {
 	      adView.destroy();
 	    }
 	    super.onDestroy();
 	  }
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
     protected void onStop() 
     {
       
         Scorer.reset();
 		GameBuilder.setReady(false);
 		Scorer.setReadyToPlay(false);
		super.onStop();
         //this.finish();
         //Log.d(tag, "MYonStop is called");
         // insert here your instructions
     }
 
 
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // requesting to turn the title OFF
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         // making it full screen
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         // set our MainGamePanel as the View
         //setContentView(new MainGamePanel(this));
         setContentView(R.layout.gamelayout);
 
       
     }
 	
 	
 	@Override
 	public void onBackPressed() {
 //		Scorer.reset();
 //		GameBuilder.setReady(false);
 //		Scorer.setReadyToPlay(false);
 //		Intent intent = new Intent(this,MainActivity.class);
 //		startActivity(intent);
 	}
  
 }
