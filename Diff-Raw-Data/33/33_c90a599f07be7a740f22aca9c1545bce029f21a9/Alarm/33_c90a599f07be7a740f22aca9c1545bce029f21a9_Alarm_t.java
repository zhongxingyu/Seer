 package de.lukeslog.alarmclock.main;
 
 import de.lukeslog.alarmclock.main.ClockService.LocalBinder;
 import de.lukeslog.alarmclock.R;
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.text.Editable;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class Alarm extends Activity
 {
     ClockService mService;
     boolean mBound = false;
     public static final String PREFS_NAME = "TwentyEightClock";
     static boolean pureradio = false;
     WakeLock wakeLock;
     
     /** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.alarm);
 		try
 		{
 			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
 			final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
 	
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
 			        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
 			wakeLock.acquire();
 			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);	
 			kl.disableKeyguard();
 		}
 		catch(Exception e)
 		{
 			Log.e("clock", "The Problem seems to be to turn on or unlock the screen");
 			String errortext = "The Problem seems to be to turn on or unlock the screen\n"+e.getMessage();
 			sendMail(errortext);
 		}
 		try
 		{
 			getApplicationContext().startService(new Intent(this, ClockService.class));
 	        Intent intent = new Intent(this, ClockService.class);
 	        
 	        getApplicationContext().bindService(intent, mConnection,Context.BIND_AUTO_CREATE);
 		}
 		catch(Exception e)
 		{
 			Log.e("clock", "there is a service being creted and bound.");
 			String errortext = "there is a service being creted and bound.\n"+e.getMessage();
 			sendMail(errortext);
 		}
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         String websiteaddress = settings.getString("websiteaddress", "");
         boolean showSnooze = settings.getBoolean("showsnooze", true);
 		try
 		{
         WebView webView = (WebView) findViewById(R.id.webView1);
         webView.getSettings().setJavaScriptEnabled(true);
         if(websiteaddress.equals(""))
         {
         	webView.loadUrl("http://www.tagesschau.de");
         }
         else
         {
         	webView.loadUrl(websiteaddress);
         }
 		}
 		catch(Exception e)
 		{
 			Log.e("clock", "the problem is displaying the website");
 			String errortext = "the problem is displaying the website\n"+e.getMessage();
 			sendMail(errortext);
 			Log.e("clock", e.getMessage());
 		}
         final Button button = (Button) findViewById(R.id.button1);
         if(!showSnooze)
         {
         	button.setVisibility(View.GONE);
         }
         button.setOnClickListener(new View.OnClickListener() 
         {
             public void onClick(View v)
             {
             	mService.snooze();
             	Alarm.this.finish();
             }
         });
         
         final Button button2 = (Button) findViewById(R.id.button2);
         button2.setOnClickListener(new View.OnClickListener() 
         {
             public void onClick(View v)
             {
             	mService.awake();
             	mService.radioOff();
            	pureradio=false;
             	Alarm.this.finish();
             }
         });
         
         final Button button3 = (Button) findViewById(R.id.button3);
         button3.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
            	Log.d("clock", "BUTTON");
            	Log.d("clock", "pureradio="+pureradio);
             	if(!pureradio)
             	{
            		Log.d("clock", "pureradio="+pureradio);
             		pureradio=true;
            		Log.d("clock", "pureradio="+pureradio);
             		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
             		boolean radio = settings.getBoolean("radio", true);
            		Log.d("clock", "radio="+radio);
             		if(!radio)
             		{
            			Log.d("clock", "turn on now.");
             			mService.turnOnRadio();
             		}
             		mService.awake();
            		button3.setText("Turn Radio off");
            		
             	}
             	else
             	{
             		mService.radioOff();
            		pureradio=false;
                 	Alarm.this.finish();
            		//button3.setText("Turn Radio off");
             	}
             }
         });
  }
 
 	/** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() 
     {
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) 
         {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
         	LocalBinder binder = (LocalBinder) service;
             mService = binder.getService();
             mBound = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) 
         {
         	mService=null;
             mBound = false;
         }
     };
     
   
   public void onPause()
   {
 	  super.onPause();
 	  try
 	  {
 		  onDestroy();
 	  }
 	  catch(Exception e)
 	  {
 		  
 	  }
 	  
   }
   
   public void  onDestroy()
   {
 	  super.onDestroy();
 	  try
 	  {
 		  mService.awake();
 		  mService.radioOff();
 		  Alarm.this.finish();
 	  }
 	  catch(Exception e)
 	  {
 		  
 	  }
   }
 
   private void sendMail(String errortext)
   {
 	   SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	  String gmailaccString= settings.getString("gmailacc", "");
 	    String gmailpswString= settings.getString("gmailpsw", "");
 	    Log.i("clock", "gmailacc="+gmailaccString);
 	    Log.i("clock", "newmail");
 		final de.lukeslog.mail.BackgroundMail m = new de.lukeslog.mail.BackgroundMail(gmailaccString, gmailpswString);
 		Log.i("clock", "setTo");
 		String t[] = new String[1];
 		t[0]= gmailaccString;
 		m.setTo(t);
 		Log.i("clock", "Set From");
 		m.setFrom(gmailaccString);
 		Log.i("clock", "setSubject");
 		String header="ERROR";
 		Log.i("clock", "Sending with herder="+header);
 		m.setSubject(header);
 		Log.i("clock", "setBody");
 		String body=errortext+"\n \n Sincearly, \n your alarm clock.";
 		Log.i("tag", "body"+body);
 		m.setBody(body);
 		try 
 		{
 			Log.i("clock", "add Atachment");
 			//m.addAttachment(image);
 		} 
 		catch (Exception e) 
 		{
       	Log.e("clock", e.getMessage());
 			e.printStackTrace();
 		}
 		Thread tt = new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				try 
 				{
 					Log.i("clock", "send?");
 					m.send();
 					
 				} 
 				catch (Exception e) 
 				{
 					Log.i("clock", "cc"+e);
 					e.printStackTrace();
 				}	
 			}
 			
 		});
 		tt.start();
   }
 }
