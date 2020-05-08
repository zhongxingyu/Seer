 package com.coredroid.core;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Window;
 import android.widget.ImageView;
import android.widget.LinearLayout;
 
 import com.coredroid.R;
 import com.coredroid.ui.CoreActivity;
 import com.coredroid.util.BackgroundTask;
 import com.coredroid.util.LogIt;
 
 /**
  * Show a splash image for a brief time while the application is initialized.
  * <br>
  * The application should extend this activity and make it the launch/main intent.
  * It is also important to add this to the activity tag in AndroidManifest.xml
  * <code>android:configChanges="keyboard|orientation"</code> 
  */
 public abstract class AppLauncher extends CoreActivity {
 
 	private static final int DEFAULT_MIN_DISPLAY_TIME = 1000;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setContentView(getLayout());
 		
		((LinearLayout)findViewById(R.id.splash)).setBackgroundColor(getBackgroundColor());
 
 		updateSplashImage();
 
 		new BackgroundTask() {
 			
 			@Override
 			public void work() {
 				
 				long start = System.currentTimeMillis();
 
 				// If app is restarting, clear out old state
 		        CoreApplication.getState().clear();
 				
 				// App specific initialization
 				try {
 					init();
 				} catch (Throwable t) {
 					LogIt.e(this, t, "Failure to init");
 					fail("There was a problem starting the application.  Please contact the developer: " + t);
 				}
 
 				// Ensure the screen stays up long enough
 				long duration = System.currentTimeMillis() - start;
 				long delay = getMinimumDisplayTime() - duration;
 				if (delay > 0) {
 					try {
 						synchronized(this) {
 							Thread.sleep(delay);
 						}
 					} catch (Exception e){/*don't care*/}
 				}
 			}
 			
 			@Override
 			public void done() {
 				Intent intent = onInitComplete();
 				if (intent != null) {
 					startActivity(intent);
 					finish();
 				} else {
 					fail("No activity specified");
 				}
 			}
 		};
 	}
 	
 	/** 
 	 * The resource ID of the layout xml for the splash.  the outside container _must_ contain a 'splash' id and a 'splashImage' id
 	 */
 	protected int getLayout() {
 		return R.layout.splash;
 	}
 	
 	protected int getMinimumDisplayTime() {
 		return DEFAULT_MIN_DISPLAY_TIME;
 	}
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         updateSplashImage();
     }
 
 	protected void updateSplashImage() {
 		Display display = getWindowManager().getDefaultDisplay();
 		int id = display.getHeight() > display.getWidth() ? getPortraitSplashResource() : getLandscapeSplashResource(); 
 		((ImageView)findViewById(R.id.splashImage)).setImageResource(id);
 	}
 	
 	/**
 	 * Allow specifying background color image if the splash image doesn't fill the entire screen
 	 */
 	protected int getBackgroundColor() {
 		return Color.WHITE;
 	}
 	
 	/**
 	 * Resource ID of the splash image to show when in portrait mode
 	 */
 	protected int getPortraitSplashResource() {
 		return R.drawable.splash;
 	}
 	
 	/**
 	 * Resource ID of the splash image to show when in landscape mode (defaults to portrait resource)
 	 * @return
 	 */
 	protected int getLandscapeSplashResource() {
 		return getPortraitSplashResource();
 	}
 	
 	/**
 	 * Called after init has completed.  Will pass control to the supplied intent
 	 */
 	protected abstract Intent onInitComplete();
 
 	/**
 	 * Called after the splash has appeared and runs in the background.
 	 */
 	protected void init(){}
 	
 }
