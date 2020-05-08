 package cm.aptoide.pt;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Start extends Activity {
 
 	/**
 	 * The thread to process splash screen events
 	 */
	private Thread mSplashThread;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		SetAptoideTheme.setAptoideTheme(this);
 		super.onCreate(savedInstanceState);
 
 		if(ApplicationAptoide.PARTNERID != null){

 			// Splash screen view
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			setContentView(R.layout.splash);
 
			final Start sPlashScreen = this;
 
 			// The thread to wait for splash screen events
 			mSplashThread =  new Thread(){
 				@Override
 				public void run(){
 					try {
 						synchronized(this){
 							// Wait given period of time or exit on touch
 							wait(3000);
 						}
 					}
					catch(InterruptedException ex){
 					}
 
 					finish();
 
 					// Run next activity
 					Intent intent = new Intent();
 					intent.setClass(sPlashScreen, MainActivity.class);
 					startActivity(intent);
					//                stop();
 				}
 			};
 
			mSplashThread.start();
 		}else{
            finish();
 			Intent intent = new Intent();
 			intent.setClass(Start.this, MainActivity.class);
			startActivity(intent);

 		}
 	}
 
 	/**
 	 * Processes splash screen touch events
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent evt)
 	{
 		if(ApplicationAptoide.PARTNERID != null){
 			if(evt.getAction() == MotionEvent.ACTION_DOWN)
 			{
 				synchronized(mSplashThread){
 					mSplashThread.notifyAll();
 				}
 			}
 		}
 		return true;
 	}
}
