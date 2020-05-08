 package de.lukeslog.alarmclock;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import de.jaetzold.philips.hue.ColorHelper;
 import de.jaetzold.philips.hue.HueBridge;
 import de.jaetzold.philips.hue.HueLightBulb;
 import de.lastfm.stuff.LastFMConstants;
 import de.lukeslog.alarmclock.R;
 import de.umass.lastfm.Authenticator;
 import de.umass.lastfm.Caller;
 import de.umass.lastfm.Session;
 import de.umass.lastfm.Track;
 import de.umass.lastfm.scrobble.ScrobbleResult;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 public class ClockService extends Service implements Runnable, OnPreparedListener
 {
     public static final String PREFS_NAME = "TwentyEightClock";
     public static String BRIDGEUSERNAME = "552627b33010930f275b72ab1c7be258";
     public static boolean RADIO = true;
     public static boolean RINGTONE = true;
     public static int SNOOZETIME = 300;
     public static String RADIOSTATION="";
     public static boolean FADE_IN=false;
     public static String SONG_NAME="";
     public static int timesincewakeup =1000;
     private Thread runner;
     int[] mediaarray = {R.raw.swag};//, R.raw.ilrr, R.raw.idan, R.raw.hb, R.raw.du, R.raw.cm, R.raw.htbs, R.raw.g};
     //int[] mediaarray = {R.raw.tetris};
 
     MediaPlayer mp = new MediaPlayer();
     MediaPlayer mp2 = new MediaPlayer();
     int randomsongnumber;
     int snoozetime=-1;
     boolean playmusic=true;
     private Handler handler = new Handler();
     List<HueBridge> bridges;
     Collection<HueLightBulb> lights;
     boolean lightshow=true;
     
     public class LocalBinder extends Binder 
     {
     	ClockService getService() 
     	{
             // Return this instance of LocalService so clients can call public methods
             return ClockService.this;
         }
     }
     
     private final IBinder mBinder = new LocalBinder();
     
     
 	@Override
 	public IBinder onBind(Intent intent) 
 	{
 		runner = new Thread(this);
 		runner.start();
 		return mBinder;
 	}
 	
 	@Override
     public int onStartCommand(Intent intent, int flags, int startId) 
     {
 		//Start foreground is another way to make sure the app does not stop...
 		return START_STICKY;
     }
 	
 	@Override
 	public void onCreate() 
 	{
 		super.onCreate();
 		int icon = R.drawable.clock; 
 		 Notification note=new Notification(icon, "Clock running", System.currentTimeMillis());
 		 Intent i=new Intent(this, AlarmClockActivity.class);
 
 		 i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
 				 Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
 		PendingIntent pi=PendingIntent.getActivity(this, 0,
                i, 0);
 
 		note.setLatestEventInfo(this, "Alarm Clock",
 				"...running",
 				pi);
 		note.flags|=Notification.FLAG_AUTO_CANCEL;
 		
 		startForeground(1337, note);
 		new Thread(new Runnable() 
     	{
     	    public void run() 
     	    {
 				bridges = HueBridge.discover();
 				for(HueBridge bridge : bridges) 
 			    {
 					bridge.setUsername(BRIDGEUSERNAME);
 					if(bridge.authenticate(true)) 
 		            {
 		            	Log.d("HUE", "Access granted. username: " + bridge.getUsername());
 		    			lights = (Collection<HueLightBulb>) bridge.getLights();
 		    			Log.d("HUE", "Available LightBulbs: "+lights.size());
 		    			for (HueLightBulb bulb : lights) 
 		    			{
 		    				Log.d("HUE", bulb.toString());
 		    				//identifiy(bulb);
 		    			}
 		    			System.out.println("");
 		            } 
 		            else 
 		            {
 		            	Log.d("HUE", "Authentication failed.");
 		            }
 			    }
     	    }
     	}).start();
 		
 
 
 	}
 	
 	@Override
     public void onDestroy() 
     {
 		Log.i("clock", "onDestroy!");
         super.onDestroy();
         mp.release();
         stopForeground(true);
     }
 	
 
 	public boolean alarmSet() 
 	{
 	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	    boolean active = settings.getBoolean("active", false);
 	    return active;
 	}
 	
 	private void goplay()
 	{
 		randomsongnumber = (int) (Math.random() * (mediaarray.length-1));
 	    new Thread(new Runnable() 
     	{
     	    public void run() 
     	    {
     		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 /**    		    String lastfmusername = settings.getString("lastfmusername", "");
     		    String lastfmpassword = settings.getString("lastfmpassword", "");
 				if(!lastfmusername.equals(""))
 				{
 					Session session=null;
 					try
 					{
 						Caller.getInstance().setCache(null);
 						session = Authenticator.getMobileSession(lastfmusername, lastfmpassword, LastFMConstants.key, LastFMConstants.secret);
 					}
 					catch(Exception e)
 					{
 						Log.e("clock", e.getMessage());
 					}
 					if(session!=null)
 					{
 						int now = (int) (System.currentTimeMillis() / 1000);
 						if(mediaarray[randomsongnumber]==R.raw.surf)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Surf Rider", "The Lively Ones", session);
 							result = Track.scrobble("Surf Rider", "The Lively Ones", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.who)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Michael Jackson", "Who is it", session);
 							result = Track.scrobble("Michael Jackson", "Who is it", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.world)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("R.E.M.", "World Leader Pretend", session);
 							result = Track.scrobble("R.E.M.", "World Leader Pretend", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.mull)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Wings", "Mull Of Kintyre", session);
 							result = Track.scrobble("Wings", "Mull Of Kintyre", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.robot)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Ryksopp", "The Girl And The Robot", session);
 							result = Track.scrobble("Ryksopp", "The Girl And The Robot", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.sloop)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("The Beach Boys", "Sloop John B", session);
 							result = Track.scrobble("The Beach Boys", "Sloop John B", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.songs)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Weezer", "Heart Songs", session);
 							result = Track.scrobble("Weezer", "Heart Songs", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.barra)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Rachid Taha", "Barra Barra", session);
 							result = Track.scrobble("Rachid Taha", "Barra Barra", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.changes)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("2Pack", "Changes", session);
 							result = Track.scrobble("2Pack", "Changes", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.battery)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Metallica", "Battery", session);
 							result = Track.scrobble("Metallica", "Battery", now, session);
 						}
 						if(mediaarray[randomsongnumber]==R.raw.ready)
 						{
 							ScrobbleResult result = Track.updateNowPlaying("Fugees", "Ready Or Not", session);
 							result = Track.scrobble("Fugees", "Ready Or Not", now, session);
 						}
 					}
 				}**/
     	    }
     	}).start();
 		mp = new MediaPlayer();
 		mp.setScreenOnWhilePlaying(true);
 		mp = MediaPlayer.create(this, mediaarray[randomsongnumber]);
 		mp.setLooping(false);
 		mp.setOnPreparedListener(this);
 		mp.setOnCompletionListener(new OnCompletionListener(){
 
 			@Override
 			public void onCompletion(MediaPlayer mp) 
 			{
 				goplay();
 			}
 			
 		});
 		mp.start();
 	}
 	
 	public void wake()
 	{
 		Log.i("clock", "wake");
 		Intent alarm = new Intent(this, Alarm.class);
 		alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		alarm.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		//Turn on my Coffe machine
 		//http://192.168.1.242/control?cmd=set_state_actuator&number=3&function=1&page=control.html	
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 			        URL oracle = new URL("http://192.168.1.242/control?cmd=set_state_actuator&number=3&function=1&page=control.html");
 			        URLConnection yc = oracle.openConnection();
 			        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
 					System.out.println("command->heat");
 				}
 				catch(Exception e)
 				{
 					Log.e("HUE", "there was an error when setting the lightbulb");
 				}
 			}
 	 	}).start();
 		startActivity(alarm);
 		//Find an mp3
 		if(RINGTONE)
 		{
 			goplay();
 			//startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=Hxy8BZGQ5Jo")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
 		}
 		else
 		{
 			boolean mExternalStorageAvailable = false;
 			boolean mExternalStorageWriteable = false;
 			String state = Environment.getExternalStorageState();
 
 			if (Environment.MEDIA_MOUNTED.equals(state)) 
 			{
 			    // We can read and write the media
 			    mExternalStorageAvailable = mExternalStorageWriteable = true;
 			} 
 			else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			    // We can only read the media
 			    mExternalStorageAvailable = true;
 			    mExternalStorageWriteable = false;
 			} 
 			else 
 			{
 			    // Something else is wrong. It may be one of many other states, but all we need
 			    //  to know is we can neither read nor write
 			    mExternalStorageAvailable = mExternalStorageWriteable = false;
 			}
 			if( mExternalStorageAvailable)
 			{
 				Log.i("clock", Environment.getExternalStorageState());
 				File filesystem = Environment.getExternalStorageDirectory();
 				String path = filesystem.getAbsolutePath();
 				File[] filelist = filesystem.listFiles();
 				Log.i("clock", SONG_NAME);
 				for(int i=0; i<filelist.length; i++)
 				{
 					//Log.i("clock", filelist[i].getName());
 					if(filelist[i].getName().equals("Music"))
 					{
 						File[] filelist2 = filelist[i].listFiles();
 						for(int j=0; j<filelist2.length; j++)
 						{
 							//Log.i("clock", filelist2[j].getName());
 							if(filelist2[j].getName().equals(SONG_NAME))
 							{
 								Log.i("clock", "got the song");
 								String musicpath = filelist2[j].getAbsolutePath();
 								try 
 								{
 									mp = new MediaPlayer();
 									mp.setScreenOnWhilePlaying(true);
 									mp.setDataSource(musicpath);
 									//TODO get id3 tag info and scrobble this mofo.
 							        mp.setVolume(0.99f, 0.99f);
 							        mp.setOnPreparedListener(this);
 							        mp.prepareAsync();
 								} 
 								catch (IllegalArgumentException e) 
 								{
 									e.printStackTrace();
 								} 
 								catch (IllegalStateException e) 
 								{
 									e.printStackTrace();
 								} 
 								catch (IOException e) 
 								{
 									e.printStackTrace();
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		boolean radio = settings.getBoolean("radio", true);
 		if(radio)
 		{
 			
 		}
 	}
 	
 	void turnOnRadio()
 	{
 		Log.i("clock", "turnOnRadio");
 		try
 		{
 			mp2 = new MediaPlayer();
 	    	mp2.setScreenOnWhilePlaying(true);
 	        mp2.setAudioStreamType(AudioManager.STREAM_MUSIC);
 	        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	        int station = settings.getInt("radiostation", 0);
 	        Log.d("clock", "Station---------------------"+station);
 	        try
 	        {
 	        if(station==0)
 	        {
 	        	mp2.setDataSource("http://dradio-ogg-dlf-l.akacast.akamaistream.net/7/629/135496/v1/gnl.akacast.akamaistream.net/dradio_ogg_dlf_l");
 	        }
 	        if(station==1)
 	        {
 	        	mp2.setDataSource("http://87.118.106.79:11006/ltop100.ogg");
 	        }
 	        if(station==2)
 	        {
 	        	mp2.setDataSource("http://revolutionradio.ru/live.ogg");
 	        }
 	        if(station==3)
 	        {
 	        	//http://sc2.3wk.com/3wk-u-ogg-lo
 	        	mp2.setDataSource("http://sc2.3wk.com/3wk-u-ogg-lo");
 	        }
 	        }
 	        catch(Exception e)
 	        {
 	        	Log.d("clock", "default radio");
 	        	try
 	        	{
 	        	mp2.setDataSource("http://dradio-ogg-dlf-l.akacast.akamaistream.net/7/629/135496/v1/gnl.akacast.akamaistream.net/dradio_ogg_dlf_l");
 	        	}
 	        	catch(Exception ex)
 	        	{
 	        		Log.d("clock", "fuck this");
 	        	}
 	        }
 	    	mp2.setVolume(0.99f, 0.99f);
 	        mp2.setOnPreparedListener(this);
 	        mp2.prepareAsync();
 
 		}
 	    catch (Exception ee) 
 	    {
 	      	Log.e("Error", "No Stream");
 	    }
 	}
 	
 	public void awake() 
 	{
 		try
 		{
 			timesincewakeup=0;
 			lightshow=false;
     		new Thread(new Runnable()
     		{
     			public void run()
     			{
     				try
     				{
    			        URL oracle = new URL("http://192.168.1.242/control?cmd=set_state_actuator&number=1&function=1&page=control.html");
     			        URLConnection yc = oracle.openConnection();
     			        BufferedReader in = new BufferedReader(new InputStreamReader(
     			                                yc.getInputStream()));
     					Log.d("clock", "command->heat");
     				}
     				catch(Exception e)
     				{
     					Log.e("HUE", "there was an error when setting the lightbulb");
     				}
     			}
     	 	}).start();
 			mp.stop();
 			mp.release();
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 	
 	public void snooze()
 	{
 		snoozetime=SNOOZETIME;
 		try
 		{
 			lightshow=false;
 			new Thread(new Runnable() 
 	    	{
 	    	    public void run() 
 	    	    {
 	    	    	try
 	    	    	{
 	    	    		bridges = HueBridge.discover();
 	    	    	}
 	    	    	catch(Exception e)
 	    	    	{
 	    	    		Log.e("clock", e.getMessage());
 	    	    	}
 	    			for(HueBridge bridge : bridges) 
 	    		    {
 	    				bridge.setUsername(BRIDGEUSERNAME);
 	    				if(bridge.authenticate(true)) 
 	    	            {
 	    	            	Log.d("HUE", "Access granted. username: " + bridge.getUsername());
 	    	            	try
 	    	            	{
 	    	            		lights = (Collection<HueLightBulb>) bridge.getLights();
 	    	            	}
 	    	            	catch(Exception e)
 	    	            	{
 	    	            		Log.e("clock", e.getMessage());
 	    	            	}
 	    	    			Log.d("HUE", "Available LightBulbs : "+lights.size());
 	    	    			for (HueLightBulb bulb : lights) 
 	    	    			{
 	    	    				try
 	    	    				{
 		    	    				Log.d("HUE", bulb.toString());
 		    	    				bulb.setOn(false);
 		    	    				//bulb.setTransitionTime(i*10);
 		    	    				//setHueColor(bulb, 255.0, 255.0, 255.0);
 	    	    				}
 	    	    				catch(Exception e)
 	    	    				{
 	    	    					Log.e("clock", e.getMessage());
 	    	    				}
 	    	    			}
 	    	    			//System.out.println("");
 	    	            } 
 	    	            else 
 	    	            {
 	    	            	Log.d("HUE", "Authentication failed.");
 	    	            }
 	    		    }
 	    	    }
 	    	}).start();
 			mp.stop();
 			mp.release();
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 	
 	@Override
 	public void onPrepared(MediaPlayer mpx) 
 	{
 		mpx.start();
 	}
 	
 	
 	@Override
 	public void run() 
 	{
 		boolean oktorun=true;
 		boolean newalert=true;
 		while(oktorun)
 		{
 			try
 	    	{
 	    		  Thread.currentThread().sleep(1000);
 	    	}
 	    	catch(Exception ie)
 	    	{
 
 	    	}
 		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		    boolean ringtone = settings.getBoolean("ringtone", true);
 		    timesincewakeup++;
 		    
 		    RINGTONE = ringtone;
 		    boolean active = settings.getBoolean("active", true);
 		    boolean fadein = settings.getBoolean("fadein", false);
 		    String song = settings.getString("song", "");
 		    boolean reminder = settings.getBoolean("reminder", false);
 		    int remindersubtract = settings.getInt("remindersubtract", 8);
 		    String remindertext = settings.getString("remindertext", "");
 		    SONG_NAME=song;
 		    FADE_IN = fadein;
 		    Log.i("clock", "? "+timesincewakeup);
 		    if(snoozetime>0)
 		    {
 		    	snoozetime--;
 		    	Log.d("clock", "snoozetime============="+snoozetime);
 		    }
 		    if(active)
 		    {
 		    	if((getAlarmHour()==(getHour()+2) && getAlarmMinute()==getMinute()))
 		    	{
 		    		//Turn on the heat
 		    		new Thread(new Runnable()
 		    		{
 		    			public void run()
 		    			{
 		    				try
 		    				{
						        URL oracle = new URL("http://192.168.1.242/control?cmd=set_state_actuator&number=1&function=4&page=control.html");
 						        URLConnection yc = oracle.openConnection();
 						        BufferedReader in = new BufferedReader(new InputStreamReader(
 						                                yc.getInputStream()));
 								Log.d("clock", "command->heat");
 		    				}
 		    				catch(Exception e)
 		    				{
 		    					Log.e("HUE", "there was an error when setting the lightbulb");
 		    				}
 		    			}
 		    	 	}).start();
 		    	}
 		    	if((getAlarmHour()==getHour() && getAlarmMinute()==getMinute() && newalert) || snoozetime==0)
 		    	{
 		    		snoozetime=-1;
 		    		lightshow=true;
 		    		Log.i("clock", "alarm time");
 		    		if(FADE_IN)
 		    		{
 		    			fadein();
 		    			try
 		    			{
 		    				lights(10);
 		    			}
 		    			catch(Exception e)
 		    			{
 		    				
 		    			}
 		    		}
 		    		else
 		    		{
 		    			try
 		    			{
 		    				lights(0);
 		    			}
 		    			catch(Exception e)
 		    			{
 		    				
 		    			}
 		    			AudioManager audio;
 		    			audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		    			audio.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_VIBRATE);
 		    		}
 		    		wake();
 		    		newalert=false;
 		    	}
 		    	if((!(getAlarmHour()==getHour()) || !(getAlarmMinute()==getMinute())) && !newalert)
 		    	{
 		    		newalert=true;
 		    	}
 		    }
 		    Log.i("clock", "reminder is"+reminder);
 		    if(reminder)
 		    {
 		    	int rtime = getAlarmHour()-remindersubtract;
 		    	if(rtime<0)
 		    	{
 		    		rtime=24+rtime;
 		    		Log.i("clock", "rtime="+rtime);
 		    	}
 		    	Log.i("clock", "rtime="+rtime);
 		    	Log.i("clock", "getHour()"+getHour());
 		    	Log.i("clock", "alarm,Minute()="+getAlarmMinute());
 		    	
 		    	if(getMinute()==getAlarmMinute() && rtime==getHour() && reminder)
 		    	{
 		    		Date d = new Date();
 		    		if(d.getSeconds()<5)
 		    		{
 				    	int icon = R.drawable.clock; 
 		    	    	final NotificationManager mNotMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		    			final Notification notfication = new Notification(
 								icon, "Reminder",
 		    					System.currentTimeMillis());
 		    			// on can puit the countdown here!
 		    			Intent settingsIntent = new Intent(this, AlarmClockActivity.class);
 		    			final PendingIntent pIntent = PendingIntent.getActivity(this, 0,
 		    					settingsIntent, 0);
 		    			notfication.setLatestEventInfo(getApplicationContext(),
 		    					"Reminder:", remindertext, pIntent);
 		    			notfication.flags |= Notification.FLAG_AUTO_CANCEL;
 
 		    			notfication.defaults |= Notification.DEFAULT_VIBRATE;
 		    		    notfication.vibrate = new long[]{500,500,500,500};
 	    		    	notfication.defaults |= Notification.DEFAULT_SOUND;
 		    			mNotMan.notify(555, notfication);
 		    			//TODO. sendemail.
 		    			String gmailaccString= settings.getString("gmailacc", "");
 					    String gmailpswString= settings.getString("gmailpsw", "");
 					    Log.i("clock", "gmailacc="+gmailaccString);
 					    Log.i("clock", "newmail");
 						final de.lukeslog.mail.BackgroundMail m = new de.lukeslog.mail.BackgroundMail(gmailaccString, gmailpswString);
 						Log.i("clock", "setTo");
 						String t[] = new String[1];
 						t[0]= settings.getString("tumblrmail", "")+"@tumblr.com";
 						m.setTo(t);
 						Log.i("clock", "Set From");
 						m.setFrom(gmailaccString);
 						Log.i("clock", "setSubject");
 						String header="Reminder: Go To Bed";
 						Log.i("clock", "Sending with herder="+header);
 						m.setSubject(header);
 						Log.i("clock", "setBody");
 						String body=remindertext+"\n \n Sincearly, \n your alarm clock.";
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
 									// TODO Auto-generated catch block
 									Log.i("clock", "cc"+e);
 									e.printStackTrace();
 								}	
 							}
 							
 						});
 						tt.start();
 		    		}
 		    	}
 		    }
 		}
 	}
 	
 	public int getAlarmHour()
 	{
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		int hour = settings.getInt("hour", 0);
 		//Log.i("clock", "alarm hour "+hour);
 		return hour;
 	}
 	
 	public int getAlarmMinute()
 	{
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		int hour = settings.getInt("minute", 0);
 		//Log.i("clock", "alarm minute "+hour);
 		return hour;
 	}
 	
 	public int getHour()
 	{
 		Date d = new Date();
 		//Log.i("clock", "actualhour "+d.getHours());
 		return d.getHours();
 	}
 	
 	public int getMinute()
 	{
 		Date d = new Date();
 		return d.getMinutes();
 	}
 	
 	private void fadein()
 	{
 		final AudioManager audio;
 		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		new Thread(new Runnable() 
     	{
     	    public void run() 
     	    {
     			for(int x=0; x<16; x++)
     			{
     				audio.setStreamVolume(AudioManager.STREAM_MUSIC, x, AudioManager.FLAG_VIBRATE);
     				try
     		    	{
     		    		  Thread.currentThread().sleep(1500);
     		    	}
     		    	catch(Exception ie)
     		    	{
 
     		    	}
     			}
     	    }
     	}).start();
 	}
 	
 	private void lights(final int i)
 	{
 		new Thread(new Runnable() 
     	{
     	    public void run() 
     	    {
     	    	try
     	    	{
     	    		bridges = HueBridge.discover();
     	    	}
     	    	catch(Exception e)
     	    	{
     	    		Log.e("clock", e.getMessage());
     	    	}
     			for(HueBridge bridge : bridges) 
     		    {
     				bridge.setUsername(BRIDGEUSERNAME);
     				if(bridge.authenticate(true)) 
     	            {
     	            	Log.d("HUE", "Access granted. username: " + bridge.getUsername());
     	            	try
     	            	{
     	            		lights = (Collection<HueLightBulb>) bridge.getLights();
     	            	}
     	            	catch(Exception e)
     	            	{
     	            		Log.e("clock", e.getMessage());
     	            	}
     	    			Log.d("HUE", "Available LightBulbs : "+lights.size());
     	    			for (HueLightBulb bulb : lights) 
     	    			{
     	    				try
     	    				{
 	    	    				Log.d("HUE", bulb.toString());
 	    	    				bulb.setOn(true);
 	    	    				bulb.setTransitionTime(i*10);
 	    	    				setHueColor(bulb, 255.0, 255.0, 255.0);
     	    				}
     	    				catch(Exception e)
     	    				{
     	    					Log.e("clock", e.getMessage());
     	    				}
     	    			}
     	    			//System.out.println("");
     	            } 
     	            else 
     	            {
     	            	Log.d("HUE", "Authentication failed.");
     	            }
     		    }
     	    }
     	}).start();
 	}
 	
 	
 	public static void identifiy(final HueLightBulb bulb)
 	{
 		new Thread(new Runnable()
 	 	{
 	 		public void run()
 	 		{
 	 			try
 	 			{
     				Log.d("HUE", bulb.toString());
     				boolean originalyon=false;
     				if(bulb.getOn())
     				{
     					originalyon=true;
     				}
     				Integer bri = null;
     				Integer hu = null;
     				Integer sa = null;
     				double cix = 0;
     				double ciy = 0;
     				int ct = 0;
     				if(originalyon)
     				{
 	    				bri = bulb.getBrightness();
 	    				hu = bulb.getHue();
 	    				sa = bulb.getSaturation();
 	    				cix = bulb.getCiex();
 	    				ciy = bulb.getCiey();
 	    				ct = bulb.getColorTemperature();
 	    				bulb.setOn(false);
     				}
     				try 
     				{
 						Thread.sleep(250);
 					} 
     				catch (InterruptedException e) 
     				{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
     				bulb.setOn(true);
     				bulb.setBrightness(ColorHelper.convertRGB2Hue("255255255").get("bri"));
     				bulb.setHue(ColorHelper.convertRGB2Hue("255255255").get("hue"));
     				bulb.setSaturation(ColorHelper.convertRGB2Hue("255255255").get("sat"));
     				try 
     				{
 						Thread.sleep(500);
 					} 
     				catch (InterruptedException e) 
     				{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
     				bulb.setOn(false);
     				try 
     				{
 						Thread.sleep(250);
 					} 
     				catch (InterruptedException e) 
     				{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
     				if(originalyon)
     				{
 	    				bulb.setOn(true);
 	    				bulb.setBrightness(bri);
 	    				bulb.setHue(hu);
 	    				bulb.setSaturation(sa);		 
 	    				bulb.setCieXY(cix, ciy);
 	    				bulb.setColorTemperature(ct);
     				}
 	 			}
 	 			catch(Exception e)
 	 			{
 	 				Log.e("HUE", "error while setting lights 2");
 	 			}
 			}
 	 	}).start();
 	}
 
 	public static void setHueColor(final HueLightBulb bulb, double r, double g, double b)
 	{
 	 	//method from http://www.everyhue.com/vanilla/discussion/166/hue-rgb-to-hsv-algorithm/p1
 		//r = (float(rInt) / 255)
 		r=r/255.0;
 		//g = (float(gInt) / 255)
 		g=g/255.0;
 		//b = (float(bInt) / 255)
 		b=b/255.0;
 
 		if (r > 0.04045)
 		{
 			r = Math.pow(((r + 0.055) / 1.055), 2.4);
 		}
 		else
 		{
 			r = r / 12.92;
 		}
 		if (g > 0.04045)
 		{
 			g = Math.pow(((g + 0.055) / 1.055), 2.4);
 		}
 		else
 		{
 			g = g / 12.92;
 		}
 		if (b > 0.04045)
 		{
 			b = Math.pow(((b + 0.055) / 1.055), 2.4);
 		}
 		else
 		{
 			b = b / 12.92;
 		}
 
 		r = r * 100;
 		g = g * 100;
 		b = b * 100;
 
 		//Observer = 2deg, Illuminant = D65
 		//These are tristimulus values
 		//X from 0 to 95.047
 		//Y from 0 to 100.000
 		//Z from 0 to 108.883
 		double X = r * 0.4124 + g * 0.3576 + b * 0.1805;
 		double Y = r * 0.2126 + g * 0.7152 + b * 0.0722;
 		double Z = r * 0.0193 + g * 0.1192 + b * 0.9505;
 
 		//Compute xyY
 		double sum = X + Y + Z;
 		double chroma_x = 0;
 		double chroma_y = 0;
 		if (sum > 0)
 		{
 			chroma_x = X / (X + Y + Z); //x
 			chroma_y = Y / (X + Y + Z); //y
 		}
 		final double ch_x =chroma_x;
 		final double ch_y = chroma_y;
 		int brightness = (int)(Math.floor(Y / 100 *254)); //luminosity, Y
 		boolean isBulbOn = true;
 		if (brightness == 0)
 		{
 			isBulbOn = false; //bri:0 and the hue bulbs are still on
 		}
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					bulb.setOn(true);
 					bulb.setCieXY(ch_x , ch_y);
 				}
 				catch(Exception e)
 				{
 					Log.e("HUE", "there was an error when setting the lightbulb");
 				}
 			}
 	 	}).start();
 	}
 
 
 	public void radioOff() 
 	{
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
					 URL oracle = new URL("http://192.168.1.242/control?cmd=set_state_actuator&number=1&function=1&page=control.html");
 				     URLConnection yc = oracle.openConnection();
 				     BufferedReader in = new BufferedReader(new InputStreamReader(
 				                                yc.getInputStream()));
 					Log.d("clock", "command->heat");
 				}
 				catch(Exception e)
 				{
 					Log.e("HUE", "there was an error when setting the lightbulb");
 				}
 			}
 	 	}).start();
 		try
 		{
 			mp2.stop();
 			mp2.release();
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 }
