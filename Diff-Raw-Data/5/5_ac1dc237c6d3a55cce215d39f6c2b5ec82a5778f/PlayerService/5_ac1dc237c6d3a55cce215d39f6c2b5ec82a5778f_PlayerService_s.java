 package jp.dip.ysato.onsenplayer;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 public class PlayerService extends Service {
 	public static final String Notify = ".NOTIFY";
 	public static final String WaitStream = "WaitStream";
 	public static final String Resume = "Resume";
 	public static final String RemotePause = "Pause";
 	public static final String RemotePlay = "Play";
 	private MediaPlayer mediaPlayer;
 	private NotificationManager notificationManager;
 	public int length = 0;
 	private GetStream getStream;
 	private BroadcastReceiver connectivityActionReceiver;
 	protected RandomAccessFile cachefile;
 	protected RandomAccessFile streamfile;
 	private ScheduledExecutorService playermonitor;
 	protected int prefetch;
 	private Bundle bundle;
 	private boolean manualPause;
 	public boolean waitstream;
 	private ComponentName eventReceiver;
 	class GetStream extends Thread {
 		private boolean initialized;
 		private String url;
 		private Handler handler;
 		private String title;
 		public GetStream(Handler handler) {
 			String program[] = bundle.getStringArray("program");
 			this.url = program[1];
 			this.title = program[3];
 			this.handler = handler;
 		}
 		public void run() {
 			InputStream in = null;
 			FileOutputStream out = null;
 			HttpURLConnection http = null;
 			try {
 				URL url = new URL(this.url);
 				String f[] = this.url.split("/");
 				String filename = f[f.length - 1];
 				File file = new File (getApplicationContext().getCacheDir(), filename);
 				if (file.exists())
 					file.delete();
 				file.deleteOnExit();
 				cachefile = new RandomAccessFile(file, "rw");
 				do {
 					http = (HttpURLConnection) url.openConnection();
 					http.setRequestMethod("GET");
 					if (length > 0)
 						http.setRequestProperty("Range", String.format("%d-", cachefile.getFilePointer()));
 					http.connect();
 					length = Integer.parseInt(http.getHeaderField("Content-Length"));
 					in = http.getInputStream();
 					cachefile.setLength(length);
 					streamfile = new RandomAccessFile(new File(PlayerService.this.getCacheDir(), filename), "r");
 					byte buf[] = new byte[128 * 1024];
 					while(length > 0) {
 						try {
 							int receive = in.read(buf);
 							cachefile.write(buf,0, receive);
 							length -= receive;
 							prefetch += receive;
 							if (prefetch < 65536)
 								continue;
 							handler.post(new Runnable() {
 								@Override
 								public void run() {
 									// TODO Auto-generated method stub
 									if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
 										try {
 											if (!initialized) {
 													mediaPlayer.setDataSource(streamfile.getFD());
 													mediaPlayer.setDisplay(null);
 													mediaPlayer.prepare();
													Notification n = new Notification(R.drawable.onsenimg, 
 															PlayerService.this.getString(R.string.playNotification, title), 
 															System.currentTimeMillis());
 													n.flags = Notification.FLAG_ONGOING_EVENT;
 													Intent intent = new Intent(PlayerService.this, PlayActivity.class);
 													intent.putExtra("program", bundle);
 													PendingIntent ci = PendingIntent.getActivity(PlayerService.this, 0, intent, 0);
 													n.setLatestEventInfo(PlayerService.this, PlayerService.this.getString(R.string.app_name), title, ci);
 													notificationManager.notify(R.string.app_name, n);
 													mediaPlayer.seekTo(0);
 													initialized = true;
 											}
 											mediaPlayer.start();
 										} catch (IllegalArgumentException e) {
 											// 	TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (IllegalStateException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (IOException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										}
 									}
 								}
 							});
 						} catch (IOException e) {
 							try {
 								waitforconnection();
 							} catch (InterruptedException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
 						}
 					}
 				} while(length > 0);
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				try {
 					if (in != null)
 						in.close();
 					if (out != null)
 						out.close();
 					if (http != null)
 						http.disconnect();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			// TODO Auto-generated method stub
 		}
 		private synchronized void waitforconnection() throws InterruptedException {
 			// TODO Auto-generated method stub
 			waitstream = true;
 			wait();
 			if (!manualPause)
 				mediaPlayer.start();
 		}
 		public synchronized void resumeconnection() {
 			waitstream = false;
 			notify();
 		}
 	}
 	
 	class PlayerBinder extends Binder {
 		public PlayerService getService() {
 			return PlayerService.this;
 		}
 
 		public Bundle getBundle() {
 			// TODO Auto-generated method stub
 			return PlayerService.this.bundle;
 		}
 	}
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mediaPlayer = new MediaPlayer();
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
 			@Override
 			public void onCompletion(MediaPlayer arg0) {
 				// TODO Auto-generated method stub
 				if (length <= 0)
 					PlayerService.this.stopSelf();
 			}
 		});
 		class MusicIntentReceiver extends BroadcastReceiver {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				// TODO Auto-generated method stub
 				if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
 					mediaPlayer.pause();
 					sendIntent(RemotePause);
 					sendBroadcast(intent);
 				} else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
 					KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
 					if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
 						return ;
 					switch(keyEvent.getKeyCode()) {
 					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
 						if (mediaPlayer.isPlaying()) {
 							mediaPlayer.pause();
 							sendIntent(RemotePause);
 						} else {
 							mediaPlayer.start();
 							sendIntent(RemotePlay);
 						}
 					}
 				}
 			}
 
 			private void sendIntent(String message) {
 				// TODO Auto-generated method stub
 				Intent i = new Intent(PlayerService.this, PlayActivity.class);
 				i.putExtra("message", message);
 				i.setAction(Notify);
 			}
 			
 		}
 		eventReceiver = new ComponentName(this, MusicIntentReceiver.class);
 		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		audioManager.registerMediaButtonEventReceiver(eventReceiver);
 	}
 	@Override
 	public void onDestroy()
 	{
 		notificationManager.cancelAll();
 		getStream.stop();
 		mediaPlayer.release();
 		mediaPlayer = null;
 		unregisterReceiver(connectivityActionReceiver);
 	}
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		bundle = intent.getBundleExtra("program");
 		getStream = new GetStream(new Handler());
 		connectivityActionReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				// TODO Auto-generated method stub
 				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 				NetworkInfo ni = cm.getActiveNetworkInfo();
 				if ((ni != null) && ni.isConnected() && (getStream.getState() == Thread.State.WAITING)) {
 					getStream.resumeconnection();
 				}
 			}
 		};
 		registerReceiver(connectivityActionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
 		getStream.start();
 		playermonitor = Executors.newScheduledThreadPool(1);
 		playermonitor.scheduleWithFixedDelay(new Runnable() {
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				if (mediaPlayer != null && cachefile != null && streamfile != null) {
 					try {
 						if ((cachefile.getFilePointer() - streamfile.getFilePointer()) < 4096) {
 							mediaPlayer.pause();
 							prefetch = 0;
 							Intent intent = new Intent(PlayerService.this, PlayActivity.class);
 							intent.putExtra("message", WaitStream);
 							intent.setAction(Notify);
 							sendBroadcast(intent);
 							Toast.makeText(PlayerService.this, R.string.getStream, Toast.LENGTH_SHORT);
 						} else {
 							if (!mediaPlayer.isPlaying()) {
 								mediaPlayer.start();
 								Intent intent = new Intent(PlayerService.this, PlayActivity.class);
 								intent.putExtra("message", Resume);
 								intent.setAction(Notify);
 								sendBroadcast(intent);
 							}
 						}
 						if (cachefile.getFilePointer() >= length)
 							playermonitor.shutdown();
 					} catch (IllegalStateException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}, 0, 1, TimeUnit.SECONDS);
 
 	}
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return new PlayerBinder();
 	}
 	public MediaPlayer player() {
 		return mediaPlayer;
 	}
 	public void pause(boolean b) {
 		// TODO Auto-generated method stub
 		manualPause = b;
 		if (b && mediaPlayer.isPlaying())
 			mediaPlayer.pause();
 		else
 			if (!waitstream)
 				mediaPlayer.start();
 	}
 	public int getPosition() {
 		// TODO Auto-generated method stub
 		if (mediaPlayer != null)
 			return mediaPlayer.getCurrentPosition() / 1000;
 		else
 			return 0;
 	}
 	public boolean isPlaying() throws IOException {
 		// TODO Auto-generated method stub
 		if (mediaPlayer != null)
 			return mediaPlayer.isPlaying() || (!manualPause);
 		else
 			throw new IOException();
 	}
 }
