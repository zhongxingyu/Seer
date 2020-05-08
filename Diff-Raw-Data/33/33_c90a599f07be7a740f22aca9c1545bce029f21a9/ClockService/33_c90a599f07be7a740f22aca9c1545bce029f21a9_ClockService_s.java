 package de.lukeslog.alarmclock.main;
 
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
 
 import org.farng.mp3.MP3File;
 import org.farng.mp3.TagException;
 import org.farng.mp3.id3.ID3v1;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session.AccessType;
 
 import de.jaetzold.philips.hue.ColorHelper;
 import de.jaetzold.philips.hue.HueBridge;
 import de.jaetzold.philips.hue.HueLightBulb;
 import de.lukeslog.alarmclock.R;
 import de.lukeslog.alarmclock.dropbox.DropBox;
 import de.lukeslog.alarmclock.dropbox.DropBoxConstants;
 import de.lukeslog.alarmclock.lastfm.LastFMConstants;
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
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
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
     public static int SNOOZETIME = 300;
     public static String RADIOSTATION="";
     public static boolean FADE_IN=false;
     public static String SONG_NAME="";
     public static int timesincewakeup =1000;
     private Thread runner;
     
     private static final int LIVINGROOM = 1;
     private static final int BATHROOM=2;
     
     private static final int HEAT_LOW=1;
     private static final int HEAT_MEDIUM=2;
     private static final int HEAT_HIGH=3;
     private static final int HEAT_VERRY_HIGH=4;
     
     public static final String ADDR_DRADIO = "http://dradio-ogg-dlf-l.akacast.akamaistream.net/7/629/135496/v1/gnl.akacast.akamaistream.net/dradio_ogg_dlf_l";
 
     MediaPlayer mp = new MediaPlayer();
     MediaPlayer mp2 = new MediaPlayer();
     int randomsongnumber;
     int snoozetime=-1;
     boolean playmusic=true;
     private Handler handler = new Handler();
     List<HueBridge> bridges;
     Collection<HueLightBulb> lights;
     boolean lightshow=true;
     
     public static String ezcontrolIP ="";
 
     // In the class declaration section:
     public static DropboxAPI<AndroidAuthSession> mDBApi;
     final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
     
     private static String dropboxfoldername="";
 
     
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
 		
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		getDropboxAPI();
 		
 		DropBox.ListAllFolders();
 		//DropBox.getFiles2(settings);
 		
 		ezcontrolIP = settings.getString("ezcontrolIP", "");
 		if(ezcontrolIP.equals(""))
 		{
 			ezcontrolIP="192.168.1.242"; //Default IP for ezControl Servers in a Home Network
 		}
 
 		 
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
 		            	Log.d("clock", "Access granted. username: " + bridge.getUsername());
 		    			lights = (Collection<HueLightBulb>) bridge.getLights();
 		    			Log.d("clock", "Available LightBulbs: "+lights.size());
 		    			for (HueLightBulb bulb : lights) 
 		    			{
 		    				Log.d("clock", bulb.toString());
 		    				//identifiy(bulb);
 		    			}
 		    			Log.d("clock", "");
 		            } 
 		            else 
 		            {
 		            	Log.d("clock", "Authentication failed.");
 		            }
 			    }
     	    }
     	}).start();
 		
 
 
 	}
 	
 	private DropboxAPI <AndroidAuthSession> getDropboxAPI()
 	{
 		//AppKeyPair appKeys = new AppKeyPair(DropBoxConstants.appKey, DropBoxConstants.appSecret);
 		//AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
 		//mDBApi = new DropboxAPI<AndroidAuthSession>(session);	
 	
 		
 	    AppKeyPair appKeys = new AppKeyPair(DropBoxConstants.appKey, DropBoxConstants.appSecret);
 	    AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
 	    mDBApi = new DropboxAPI<AndroidAuthSession>(session);
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		String dbkey = settings.getString("DB_KEY", "");
 		String dbsecret= settings.getString("DB_SECRET", "");
 	    if (dbkey.equals("")) return null;
 	    AccessTokenPair access = new AccessTokenPair(dbkey, dbsecret);
 	    mDBApi.getSession().setAccessTokenPair(access);
 	    return mDBApi;
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
 	
 	private void scrobble(final String artist, final String song)
 	{
 	    new Thread(new Runnable() 
     	{
     	    public void run() 
     	    {
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			    String lastfmusername = settings.getString("lastfmusername", "");
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
 						ScrobbleResult result = Track.updateNowPlaying(artist, song, session);
 						result = Track.scrobble(artist, song, now, session);
 					}
 				}
     	    }
     	}).start();
 	}
 	
 	private void goplaymp3()
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
				Log.i("clock", filelist[i].getName());
 				if(filelist[i].getName().equals("WakeUpSongs"))
 				{
 					File[] filelist2 = filelist[i].listFiles();
 					randomsongnumber = (int) (Math.random() * (filelist2.length-1));
 					String musicpath = filelist2[randomsongnumber].getAbsolutePath();
 					File f = new File(musicpath);
 					String artist="";
 					String song="";
 					try 
 					{
 						MP3File mp3 = new MP3File(f);
 						ID3v1 id3 = mp3.getID3v1Tag();
 						artist = id3.getArtist();
 						Log.d("clock", "----------->ARTIST:"+artist);
 						song = id3.getSongTitle();
 						Log.d("clock", "----------->SONG:"+song);
 						scrobble(artist, song);
 					} 
 					catch (IOException e1) 
 					{
 						e1.printStackTrace();
 					} 
 					catch (TagException e1) 
 					{
 						e1.printStackTrace();
 					}
 					catch(Exception ex)
 					{
 						Log.e("clock", "There has been an exception while extracting ID3 Tag Information from the MP3");
 					}
 					try 
 					{
 						mp = new MediaPlayer();
 						mp.setScreenOnWhilePlaying(true);
 						mp.setDataSource(musicpath);
 						mp.setLooping(false);
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
 	
 	public void wake()
 	{
 		Log.i("clock", "wake");
 		try
 		{
 			Intent alarm = new Intent(this, Alarm.class);
 			alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			alarm.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			startActivity(alarm);
 		}
 		catch(Exception e)
 		{
 			Log.e("clock", "no luck starting the alarm class");
 			Log.e("clock", e.getMessage());
 			sendMail("ERROR", "no luck starting the alarm class\n"+e.getMessage());
 		}
 		try
 		{
 			goplaymp3();
 		}
 		catch(Exception e)
 		{
 			Log.e("clock", "the mp3 playing is the problem");
 			Log.e("clock", e.getMessage());
 			sendMail("ERROR", "the mp3 playing is the problem\n"+e.getMessage());
 		}
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		boolean radio = settings.getBoolean("radio", true);
 		if(radio)
 		{
 			turnOnRadio();
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
 		        	mp2.setDataSource(ADDR_DRADIO);
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
 		        	mp2.setDataSource(ADDR_DRADIO);
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
     		heatControl(LIVINGROOM, HEAT_LOW);
     		coffeMachine(false);
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
 		mpx.setOnCompletionListener(new OnCompletionListener(){
 
 			@Override
 			public void onCompletion(MediaPlayer mpx) 
 			{
 				goplaymp3();
 			}
 			
 		});
 		mpx.start();
 	}
 	
 	
 	@Override
 	public void run() 
 	{
 		boolean oktorun=true;
 		boolean newalert=true;
 		long runningcounter=0;
 		boolean coffeealarmsend=false;
 		
 		while(oktorun)
 		{
 			runningcounter++;
 		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			//Log.d("clock", "run1");
 			try
 	    	{
 	    		  Thread.currentThread().sleep(1000);
 	    	}
 	    	catch(Exception ie)
 	    	{
 
 	    	}
 			//Check if the name of the Dropboxfolder for the music has changed
 			String dropfolderstring = settings.getString("dropboxfolder", "");
 			if(!dropboxfoldername.equals("") && !dropboxfoldername.equals(dropfolderstring))
 			{
 				//TODO: The folder to be watched has changed!
 				Log.d("clock", "folder change");
 				
 			}
 			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 			if (mWifi.isConnected()) 
 			{
 				if(runningcounter%120==1)
 				{
 					DropBox.syncFiles(settings);
 				}
 			}
 			//Log.d("clock", "run2");
 		    timesincewakeup++;
 		    boolean active = settings.getBoolean("active", true);
 		    boolean fadein = settings.getBoolean("fadein", false);
 		    String song = settings.getString("song", "");
 		    boolean reminder = settings.getBoolean("reminder", false);
 		    int remindersubtract = settings.getInt("remindersubtract", 8);
 		    String remindertext = settings.getString("remindertext", "");
 		    SONG_NAME=song;
 		    FADE_IN = fadein;
 		    //Log.i("clock", "? "+timesincewakeup);
 		    if(snoozetime>0)
 		    {
 		    	snoozetime--;
 		    	Log.d("clock", "snoozetime============="+snoozetime);
 		    }
 		    if(active)
 		    {
 		    	boolean turnoncoffee=false;
 		    	if(getAlarmMinute()>5)
 		    	{
 		    		if((getAlarmHour()==getHour() && (getAlarmMinute())==(getMinute()+5)))
 			    	{
 		    			Log.d("clock", "TURN THAT MACHINE ON 1");
 		    			turnoncoffee=true;
 			    	}
 		    	}
 		    	else
 		    	{
 		    		if((getAlarmHour()==(getHour()+1) && (getAlarmMinute()+55)==getMinute()))
 			    	{
 		    			Log.d("clock", "TURN THAT MACHINE ON");
 		    			turnoncoffee=true;
 			    	}
 		    	}
 		    	if(turnoncoffee && ! coffeealarmsend)
 		    	{
 		    		coffeMachine(true);
 		    		coffeealarmsend=true;
 		    	}
 		    	if((getAlarmHour()==(getHour()+2) && getAlarmMinute()==getMinute()))
 		    	{
 		    		//Turn on the heat
 		    		heatControl(LIVINGROOM, HEAT_VERRY_HIGH);
 		    	}
 		    	if((getAlarmHour()==getHour() && getAlarmMinute()==getMinute() && newalert) || snoozetime==0)
 		    	{
 		    		snoozetime=-1;
 		    		lightshow=true;
 		    		//Log.i("clock", "alarm time");
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
 		    		coffeealarmsend=false;
 		    	}
 		    }
 		    //Log.i("clock", "reminder is"+reminder);
 		    if(reminder)
 		    {
 		    	int rtime = getAlarmHour()-remindersubtract;
 		    	if(rtime<0)
 		    	{
 		    		rtime=24+rtime;
 		    		//Log.i("clock", "rtime="+rtime);
 		    	}
 		    	//Log.i("clock", "rtime="+rtime);
 		    	//Log.i("clock", "getHour()"+getHour());
 		    	//Log.i("clock", "alarm,Minute()="+getAlarmMinute());
 		    	
 		    	if(getMinute()==getAlarmMinute() && rtime==getHour() && reminder)
 		    	{
 		    		//TURN OFF COFFE MACHINE
 		    		coffeMachine(false);
 		    		coffeealarmsend=false;
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
 		    			if(d.getSeconds()>0 && d.getSeconds()<=1)
 		    			{
 		    				sendMail("reminder, go to bed", remindertext);
 		    			}
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
 	
 	public int getSecond()
 	{
 		Date d = new Date();
 		return d.getSeconds();
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
     	    			Log.d("clock", "Available LightBulbs : "+lights.size());
     	    			for (HueLightBulb bulb : lights) 
     	    			{
     	    				try
     	    				{
 	    	    				Log.d("clock", bulb.toString());
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
     	            	Log.d("clock", "Authentication failed.");
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
     				Log.d("clock", bulb.toString());
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
 						
 						e.printStackTrace();
 					}
     				bulb.setOn(false);
     				try 
     				{
 						Thread.sleep(250);
 					} 
     				catch (InterruptedException e) 
     				{
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
 	 				Log.e("clock", "error while setting lights 2");
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
 					Log.e("clock", "there was an error when setting the lightbulb");
 				}
 			}
 	 	}).start();
 	}
 
 
 	public void radioOff() 
 	{
 		heatControl(LIVINGROOM,HEAT_LOW);
 		try
 		{
 			mp2.stop();
 			mp2.release();
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 	
 	private void coffeMachine(final boolean x)
 	{
 		//TODO this method assumes that the machine has the number 3. It should not... but this will all be better, when I have a good server for the ezcontrol
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		int number = settings.getInt("CoffeMachine", 1);
 			if(number>0)
 			{
 			//Turn on my Coffe machine
 			//http://192.168.1.242/control?cmd=set_state_actuator&number=3&function=1&page=control.html	
 			new Thread(new Runnable()
 			{
 				public void run()
 				{
 					int function=1;
 					if(!x)
 					{
 						function=2;
 					}
 					try
 					{
 				        URL oracle = new URL("http://"+ezcontrolIP+"/control?cmd=set_state_actuator&number=3&function="+function+"&page=control.html");
 				        URLConnection yc = oracle.openConnection();
 				        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
 						Log.d("clock", "command->coffee");
 					}
 					catch(Exception e)
 					{
 						Log.e("clock", "there was an error while setting the coffe machine");
 					}
 				}
 		 	}).start();
 		}
 	}
 	
 	private void heatControl(final int device, final int function)
 	{
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					URL oracle = new URL("http://"+ezcontrolIP+"/control?cmd=set_state_actuator&number="+device+"&function="+function+"&page=control.html");
 				    URLConnection yc = oracle.openConnection();
 				    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
 					Log.d("clock", "command->heat");
 				}
 				catch(Exception e)
 				{
 					Log.e("clock", "there was an error when setting the heat");
 				}
 			}
 		}).start();
 	}
 	
 	private void sendMail(String subject, String text)
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
 		String header=subject;
 		Log.i("clock", "Sending with herder="+header);
 		m.setSubject(header);
 		Log.i("clock", "setBody");
 		String body=text+"\n \n Sincearly, \n your alarm clock.";
 		Log.i("tag", "body"+body);
 		m.setBody(body);
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
 
 
