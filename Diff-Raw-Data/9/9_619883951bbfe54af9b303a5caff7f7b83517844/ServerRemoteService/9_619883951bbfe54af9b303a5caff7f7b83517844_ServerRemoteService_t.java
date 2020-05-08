 package net.evendanan.android.thumbremote.service;
 
 import java.util.ArrayList;
 
 import net.evendanan.android.thumbremote.MediaStateListener;
 import net.evendanan.android.thumbremote.R;
 import net.evendanan.android.thumbremote.RemoteApplication;
 import net.evendanan.android.thumbremote.ServerAddress;
 import net.evendanan.android.thumbremote.ServerConnector;
 import net.evendanan.android.thumbremote.ServerState;
 import net.evendanan.android.thumbremote.ServerStatePoller;
 import net.evendanan.android.thumbremote.UiView;
 import net.evendanan.android.thumbremote.boxee.BoxeeConnector;
 import net.evendanan.android.thumbremote.boxee.BoxeeDiscovererThread;
 import net.evendanan.android.thumbremote.network.HttpRequest;
 import net.evendanan.android.thumbremote.service.DoServerRemoteAction.DoServerRemoteActionListener;
 import net.evendanan.android.thumbremote.ui.RemoteUiActivity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class ServerRemoteService extends Service implements BoxeeDiscovererThread.Receiver, DoServerRemoteActionListener {
 	
 	public class LocalBinder extends Binder {
 		public ServerRemoteService getService() {
             return ServerRemoteService.this;
         }
     }
 	
 	private static final String TAG = "ServerRemoteService";
     
 	//public static final String KEY_DATA_TITLE = "KEY_DATA_TITLE";
 	
 	private static final int NOTIFICATION_PLAYING_ID = 1;
 
 	private NotificationManager mNotificationManager;
 
 	private IBinder mBinder;
 	
 	private final BroadcastReceiver mCallReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
        	if (intent != null)
        	{
        		String newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        		if (newState != null && newState.equals(TelephonyManager.EXTRA_STATE_RINGING))
        		{
        			onPhone();
        		}
        	}
         }
     };
     
     private final BroadcastReceiver mNetworkChangedReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             onNetworkChanged();
         }
     };
     
     private Handler mHandler;
 	
 	private UiView mUi;
 	private ServerConnector mRemote;
 	private BoxeeDiscovererThread mServerDiscoverer;
 	private ServerAddress mServerAddress = null;
 	private ServerStatePoller mStatePoller = null;
 
 	private State mState;
 	/*
 	private final ServiceConnection mKeepAliveConnection = new ServiceConnection() {
 		@Override
 		public void onServiceDisconnected(ComponentName name) {				
 		}			
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {				
 		}
 	};
 */
 	private final Runnable mCheckServerStateASAP = new Runnable() {
 		@Override
 		public void run() {
 			mStatePoller.checkStateNow();
 		}
 	};
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mHandler = new Handler();
 		mState = State.DISCOVERYING;
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mBinder = new LocalBinder();
 		mRemote = new BoxeeConnector();
 		mRemote.setUiView(new MediaStateListener() {
 			
 			@Override
 			public void onMediaPlayingStateChanged(ServerState serverState) {
 				if (serverState.isMediaActive())
 				{
 					showPlayingNotification(serverState.getMediaTitle(), serverState.isMediaPlaying());
 				}
 				else
 				{
 					stopServiceIfMediaIsNotActive();
 					cancelPlayingNotification();
 				}
 				if (mUi != null) mUi.onMediaPlayingStateChanged(serverState);
 			}
 			
 			@Override
 			public void onMediaPlayingProgressChanged(ServerState serverState) {
 				if (mUi != null) mUi.onMediaPlayingProgressChanged(serverState);
 			}
 			
 			@Override
 			public void onMediaMetadataChanged(ServerState serverState) {
 				if (serverState.isMediaActive())
 				{
 					showPlayingNotification(serverState.getMediaTitle(), serverState.isMediaPlaying());
 				}
 				if (mUi != null) mUi.onMediaMetadataChanged(serverState);
 			}
 			
 			@Override
 			public void onKeyboardStateChanged(ServerState serverState) {
 				if (mUi != null) mUi.onKeyboardStateChanged(serverState);
 			}
 		});
 		
 		mStatePoller = new ServerStatePoller(mRemote, getApplicationContext());
 		mStatePoller.poll();
 		
 		IntentFilter callFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
         registerReceiver(mCallReceiver, callFilter);
         IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
         registerReceiver(mNetworkChangedReceiver, networkFilter);
         
         setServer();
         /*
         //and I want my own reference.
         bindService(new Intent(this, ServerRemoteService.class), mKeepAliveConnection, Context.BIND_AUTO_CREATE);
         */
 	}
 /*
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
     	//We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;    	
     }
   */  
     private void stopServiceIfMediaIsNotActive()
     {
     	mHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (mUi == null && !mRemote.isMediaActive())
 		    	{
 		    		Log.d(TAG,"stopServiceIfNothingIsPlaying determined that there is no running media. Killing self.");
 		    		/*
 		    		try
 		    		{
 		    			unbindService(mKeepAliveConnection);
 		    		}
 		    		catch(Exception e)
 		    		{
 		    			Log.w(TAG, "Caught an exception while unbinding from self. nm."+e.getMessage());
 		    		}
 		    		*/
 		    		stopSelf();
 		    	}
 			}
 		}, 500);
     }
 	
 	@Override
 	public void onDestroy() {
 		mStatePoller.stop();
 		
 		cancelPlayingNotification();
 		unregisterReceiver(mCallReceiver);
 		unregisterReceiver(mNetworkChangedReceiver);
 		
 		setServiceState(State.DEAD);
 		
 		super.onDestroy();		
 	}
 	
 	public State getServiceState() {return mState;}
 	
 	private void onPhone() {
 		if (RemoteApplication.getConfig().getPauseOnCall())
 		{
 			Log.i(TAG, "Got a phone call! Pausing if running...");
 			if (mRemote.isMediaPlaying())
 			{
 				Log.d(TAG, "Pausing media, since it is runing.");
 				remoteFlipPlayPause();
 			}
 		}
 		else
 		{
 			Log.i(TAG, "Got a phone call! But auto-pause is disabled.");
 		}
 	}
 	
 	private void onNetworkChanged()
 	{
 		Log.i(TAG, "Got network! Trying to reconnect...");
 		setServer();
 	}
 	
 	private void setServer() {
 		new AsyncTask<Void, Void, Void>()
 		{
 			@Override
 			protected Void doInBackground(Void... params) {
 				if (RemoteApplication.getConfig().isManuallySetServer()) {
 					mServerAddress = RemoteApplication.getConfig().constructServer();
 					mRemote.setServer(mServerAddress);
 					setServiceState(State.IDLE);
 				}
 				else {
 					if (mServerDiscoverer != null)
 						mServerDiscoverer.setReceiver(null);
 					
 					setServiceState(State.DISCOVERYING);
 					mServerDiscoverer = new BoxeeDiscovererThread(ServerRemoteService.this, ServerRemoteService.this);
 					mServerDiscoverer.start();
 				}
 				return null;
 			}
 		}.execute();
 	}
 
 	private void showPlayingNotification(String title, boolean isPlaying)
 	{
 		Notification notification = new Notification(
 				isPlaying? R.drawable.notification_playing : R.drawable.notification_paused, 
 				getString(isPlaying? R.string.server_is_playing:R.string.server_is_paused, title), 
 				System.currentTimeMillis());
 
 		Intent notificationIntent = new Intent(this, RemoteUiActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
 		notification.setLatestEventInfo(this,
 				getText(R.string.app_name), 
 				getString(isPlaying? R.string.server_is_playing:R.string.server_is_paused, title),
 				contentIntent);
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 		//notification.defaults = 0;// no sound, vibrate, etc.
 		// notifying
 		mNotificationManager.notify(NOTIFICATION_PLAYING_ID, notification);
 	}
 	
 	private void cancelPlayingNotification()
 	{
 		mNotificationManager.cancel(NOTIFICATION_PLAYING_ID);
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	public void setUiView(UiView ui) {
 		mUi = ui;
 		setServiceState(getServiceState());
 		if (ui != null)
 		{
 			mStatePoller.comeBackToForeground();
 			mUi.hello(mRemote);
 		}
 		else
 		{
 			mStatePoller.moveToBackground();
 			stopServiceIfMediaIsNotActive();
 		}
 	}
 	/*
 	@Override
 	public boolean onUnbind(Intent intent) {
 		Log.d(TAG, "onUnbind");
 		super.onUnbind(intent);
 		
 		return false; 
 	}
 	*/
 	public String getServerName()
 	{
 		return mServerAddress!=null? mServerAddress.name() : null;
 	}
 	
 	@Override
 	public void addAnnouncedServers(ArrayList<ServerAddress> servers) {
 		// This condition shouldn't ever be true.
 		if (RemoteApplication.getConfig().isManuallySetServer()) {
 			Log.d(TAG, "Skipping announced servers. Set manually");
 			setServiceState(State.IDLE);
 			return;
 		}
 
 		String preferred = RemoteApplication.getConfig().getServerName();
 
 		for (int k = 0; k < servers.size(); ++k) {
 			final ServerAddress server = servers.get(k);
 			if (server.name().equals(preferred) || TextUtils.isEmpty(preferred)) {
 				if (!server.valid()) {
 					if (mUi != null) mUi.showMessage(String.format("Found '%s' but looks broken", server.name()), 3000);
 					continue;
 				} else {
 					mServerAddress = server;
 					mRemote.setServer(mServerAddress);
 					
 					setServiceState(State.IDLE);
 					
 					if (server.authRequired())
 					{
 						if (!HttpRequest.hasCredentials())
 						{
 							setServiceState(State.ERROR_NO_PASSWORD);
 						}
 					}
 					return;
 				}
 			}
 		}
 
 		mServerAddress = null;
 		
 		setServiceState(State.ERROR_NO_SERVER);
 	}
 
 	void setServiceState(State newState) {
 		mState = newState;
 		if (mUi != null) mUi.onServerConnectionStateChanged(mState);
 	}
 
 	public void forceStop() {
 		Log.i(TAG, "Force Stop was called!");
 		//unbindService(mKeepAliveConnection);
 		stopSelf();		
 	}
 	
 	
 	@Override
 	public void onRemoteActionError(String userMessage, int errorCode, boolean longDelayMessage) {
 		if (errorCode == 401)
 		{
 			setServiceState(State.ERROR_NO_PASSWORD);
 		}
 		else if (!TextUtils.isEmpty(userMessage) && mUi != null) mUi.showMessage(userMessage, longDelayMessage? 2500 : 1250);
 	}
 	
 	@Override
 	public void onRemoteActionSuccess(String successMessage, boolean longDelayMessage) {
 		if (!TextUtils.isEmpty(successMessage) && mUi != null) mUi.showMessage(successMessage, longDelayMessage? 2500 : 500);
 		
 		mHandler.removeCallbacks(mCheckServerStateASAP);
 		mHandler.postDelayed(mCheckServerStateASAP, 100);
 	}
 
 	public static int hmsToSeconds(String hms) {
 		if (TextUtils.isEmpty(hms))
 			return 0;
 
 		try
 		{
 			int seconds = 0;
 			String[] times = hms.split(":");
 	
 			// seconds
 			seconds += Integer.parseInt(times[times.length - 1]);
 	
 			// minutes
 			if (times.length >= 2)
 				seconds += Integer.parseInt(times[times.length - 2]) * 60;
 	
 			// hours
 			if (times.length >= 3)
 				seconds += Integer.parseInt(times[times.length - 3]) * 3600;
 	
 			return seconds;
 		}
 		catch(Exception e)
 		{
 			return 0;
 		}
 	}
 	/*CONTROLLER interface*/
 
 	public void remoteFlipPlayPause() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.flipPlayPause();
 			}
 		}.execute();
 	}
 
 	public void remoteBack() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.back();
 			}
 		}.execute();
 	}
 
 	public void remoteSeekRelative(final double newSeekPosition) {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.seekRelative(newSeekPosition);
 			}
 		}.execute();
 	}
 	
 	public void remoteSeekTo(final double newSeekPosition) {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.seekTo(newSeekPosition);
 			}
 		}.execute();
 	}
 	
 	public void remoteSeekOffset(final double seekOffset)
 	{
 		final int duration = hmsToSeconds(mRemote.getMediaTotalTime());
 		if (duration > 0)
 		{
 			final double newSeekPosition = seekOffset * 100f / duration;
 			remoteSeekRelative(newSeekPosition);
 		}
 	}
 
 	public void remoteStop() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.stop();
 			}
 		}.execute();
 	}
 
 	public void remoteLeft() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.left();
 			}
 		}.execute();
 	}
 
 	public void remoteRight() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.right();
 			}
 		}.execute();
 	}
 
 	public void remoteUp() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.up();
 			}
 		}.execute();
 	}
 
 	public void remoteDown() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.down();
 			}
 		}.execute();
 	}
 
 	public void remoteSelect() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.select();
 			}
 		}.execute();
 	}
 
 	public void remoteKeypress(final char unicodeChar) {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.keypress(unicodeChar);
 			}
 		}.execute();
 	}
 
 	public void remoteVolumeOffset(final int volumeOffset) {
 		new DoServerRemoteAction(this, false) {
 			private int mNewVolume = 0;
 		@Override
 		protected void callRemoteFunction() throws Exception {
 			int volume = mRemote.getVolume();
 			mNewVolume = Math.max(0, Math.min(100, volume + volumeOffset));
 			mRemote.setVolume(mNewVolume);
 		}
 		
 		protected String getSuccessMessage() {
 			return getString(R.string.new_volume_toast, mNewVolume);
 		}
 	}.execute();
 	}
 
 	public void remoteRescanForServers() {
 		setServer();
 	}
 
 }
