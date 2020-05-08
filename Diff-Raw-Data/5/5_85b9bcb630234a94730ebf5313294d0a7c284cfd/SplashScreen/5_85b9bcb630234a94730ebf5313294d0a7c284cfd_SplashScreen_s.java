 /*
  * 
  */
 package cz.android.monet.restfulclient;
 
 import cz.android.monet.restfulclient.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class SplashScreen.
  *
  * @author krajcovic
  */
 /**
  * @author krajcovic
  *
  */
 public class SplashScreen extends Activity {
 
 	// how long until we go to the next activity
 	/** The splash time. */
	protected int splashTime = 200;
 	
 	/** The splash thread. */
 	private Thread splashThread;
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.splash);
 		
 		final SplashScreen splashScreen = this;
 		
 		 // thread for displaying the SplashScreen
 		splashThread = new Thread() {
 			 @Override
              public void run() {
 				 try {
 					 synchronized (this) {
 						wait(splashTime);
 					}
 				 }
 				 catch (InterruptedException e) {
 				}
 				 finally {
 					 finish();
 					 
 					 Intent i = new Intent();
 					 i.setClass(splashScreen, RESTExampleActivity.class);
 					 startActivity(i);
 					 
					 stop();
 				 }
 			 }
 		};
 		
 		splashThread.start();
 	}
 	
 		
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
 	 */
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		if(event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			synchronized (splashThread) {
 				splashThread.notifyAll();
 			}
 		}
 		
 		return true;
 		
 	}
 
 }
