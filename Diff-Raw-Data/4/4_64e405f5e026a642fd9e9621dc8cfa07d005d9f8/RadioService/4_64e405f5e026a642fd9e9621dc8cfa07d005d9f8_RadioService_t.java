 package es.aguasnegras.rneradio.services;
 
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import es.aguasnegras.rneradio.R;
import es.aguasnegras.rneradio.activities.MainActivity;
 import es.aguasnegras.rneradio.services.binders.RadioServiceBinder;
 import es.aguasnegras.rneradio.utils.CallManager;
 import es.aguasnegras.rneradio.utils.Logger;
 import es.aguasnegras.rneradio.utils.MediaStatus;
 
 public class RadioService extends Service {
 
 	private final MediaPlayer radioPlayer = new MediaPlayer();
 	private final RadioServiceBinder radioServiceBinder = new RadioServiceBinder(
 			this);
 	private MediaStatus status = MediaStatus.STOPPED;
 	private String url;
 	private NotificationManager nm;
 	private static final int NOTIFY_ID = R.layout.activity_main;
 	private CallManager callManager;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		this.registerCallManager();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		this.radioPlayer.stop();
 		this.radioPlayer.release();
 		this.unregisterCallManager();
 	}
 
 	@Override
 	public IBinder onBind(Intent bindingIntent) {
 		return this.radioServiceBinder;
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		return super.onUnbind(intent);
 	}
 
 	public MediaStatus play(String url) {
 		try {
 			if (!this.status.equals(MediaStatus.PAUSED)) {
 				if (!url.equals(this.url)) {
 					this.url = url;
 					if (this.radioPlayer.isPlaying()) {
 						this.radioPlayer.stop();
 					}
 					this.radioPlayer.reset();
 					this.radioPlayer.setDataSource(url);
 					this.radioPlayer.prepare();
 				}
 			}
 			this.radioPlayer.start();
 			this.radioPlayer.setOnErrorListener(new OnErrorListener() {
 				@Override
 				public boolean onError(MediaPlayer mp, int what, int extra) {
 					Logger.e(this, "Error on mediaplayer instance: " + what
 							+ " " + extra);
 					return true;
 				}
 			});
 			this.status = MediaStatus.PLAYING;
 		} catch (IllegalArgumentException e) {
 			Logger.e(this, "Error on play(): " + e.getLocalizedMessage());
 			this.status = MediaStatus.ERROR;
 		} catch (IllegalStateException e) {
 			Logger.e(this, "Error on play(): " + e.getLocalizedMessage());
 			this.status = MediaStatus.ERROR;
 		} catch (IOException e) {
 			Logger.e(this, "Error on play(): " + e.getLocalizedMessage());
 			this.status = MediaStatus.ERROR;
 		}
 		this.updateNotification();
 		return this.status;
 	}
 
 	public MediaStatus stop() {
 		if (this.status.equals(MediaStatus.PLAYING)
 				|| this.status.equals(MediaStatus.PAUSED)) {
 			this.radioPlayer.stop();
 			this.status = MediaStatus.STOPPED;
 		}
 		this.updateNotification();
 		return this.status;
 	}
 
 	public MediaStatus getStatus() {
 		return this.status;
 	}
 
 	public MediaStatus pause() {
 		if (this.status.equals(MediaStatus.PLAYING)) {
 			this.radioPlayer.pause();
 			this.status = MediaStatus.PAUSED;
 		}
 		this.updateNotification();
 		return this.status;
 	}
 
 	public MediaStatus play() {
 		if (this.status.equals(MediaStatus.PAUSED)) {
 			this.radioPlayer.start();
 			this.status = MediaStatus.PLAYING;
 		} else if (this.status.equals(MediaStatus.ERROR)) {
 			this.play(this.url);
 		}
 		this.updateNotification();
 		return this.status;
 	}
 
 	private void registerCallManager() {
 		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		if (mgr != null) {
 			this.callManager = new CallManager(this);
 			mgr.listen(this.callManager, PhoneStateListener.LISTEN_CALL_STATE);
 		}
 	}
 
 	private void unregisterCallManager() {
 		if (this.callManager != null) {
 			TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 			if (mgr != null) {
 				mgr.listen(this.callManager, PhoneStateListener.LISTEN_NONE);
 				this.callManager = null;
 			}
 		}
 	}
 
 	public void updateNotification() {
 		Notification notification = null;
 		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, MainActivity.class);
 		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				notificationIntent, 0);
 		switch (status) {
 		case PLAYING:
 			notification = new Notification(R.drawable.ic_stat_notify_play,
 					this.getText(R.string.playing), System.currentTimeMillis());
 			notification.setLatestEventInfo(context,
 					this.getText(R.string.playing), "", contentIntent);
 			this.startForeground(NOTIFY_ID, notification);
 			break;
 		case STOPPED:
 			notification = new Notification(R.drawable.ic_stat_notify_stop,
 					this.getText(R.string.stopped), System.currentTimeMillis());
 			notification.setLatestEventInfo(context,
 					this.getText(R.string.stopped), "", contentIntent);
 			this.stopForeground(true);
 			break;
 		case PAUSED:
 			notification = new Notification(R.drawable.ic_stat_notify_pause,
 					this.getText(R.string.paused), System.currentTimeMillis());
 			notification.setLatestEventInfo(context,
 					this.getText(R.string.paused), "", contentIntent);
 			this.stopForeground(true);
 			break;
 		case ERROR:
 			notification = new Notification(R.drawable.ic_stat_notify_error,
 					this.getText(R.string.error), System.currentTimeMillis());
 			notification.setLatestEventInfo(context,
 					this.getText(R.string.error), "", contentIntent);
 			this.stopForeground(true);
 			break;
 		}
 		nm.notify(NOTIFY_ID, notification);
 	}
 
 	public void clearNotifications() {
 		this.nm.cancel(NOTIFY_ID);
 	}
 }
