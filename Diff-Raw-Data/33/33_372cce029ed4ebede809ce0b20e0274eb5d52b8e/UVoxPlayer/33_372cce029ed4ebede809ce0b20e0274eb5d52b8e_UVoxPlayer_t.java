 package com.ultivox.uvoxplayer;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.*;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.ultivox.uvoxplayer.visualizer.VisualizerView;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class UVoxPlayer extends Activity {
 
     public static final String PARAM_PLAYER = "vizual";
     public static Activity activity;
 
 	public final static String INIT_UMS_NB = "UMS000007";
 	public final static String INIT_HOME_URL = "http://www.ultivox.ru/netplayer/netplayer/";
 	public final static String INIT_CONFIG_URL = "http://www.ultivox.ru/netplayer/interface/";
 	public final static String INIT_TEST_CONN_URL = "test000001/setup.txt";
 	public final static String INIT_APK_FILE = "UVoxPlayer.apk";
 	public final static String INIT_STORAGE = "/Music/netplayer";
 	public final static String INIT_PLAYLISTS = "/playlists";
 	public final static String INIT_MESSAGES = "/message";
 	public final static String INIT_DBCONNECT = "rqstplayer.php?player=";
 	public final static String INIT_COMCONNECT = "rqstplayer_com.php?player=";
 	public final static String INIT_LOG_RECIEVER = "php/file_reciever.php?player=";
 	public final static String INIT_LOGCAT_DIR = "/logcatmonitor";
 	public final static String INIT_TIME_CONNECTION = "";
 	public final static long INIT_INTERVAL_CONNECTION = 0;
 	public final static long INTERVAL_CONNECTION_STAT = 60*60*1000; // 60*60*1000
 	public static final String INIT_LOGCAT_SERVER = "http://www.ultivox.ru/netplayer/interface/php/logcat_reciever.php";
 	public static final String INIT_LOGPLAY_SERVER = "http://www.ultivox.ru/netplayer/interface/php/logplay_reciever.php";
 	public static final String INIT_SETTINGS_SERVER = "http://www.ultivox.ru/netplayer/interface/php/settings_reciever.php";
 	public final static String INIT_LOGCAT_ON = "off";
 	public final static String INIT_LOGPLAY_ON = "off";
 	public final static String INIT_UPGRADE_ON = "off";
 	public final static String INIT_DOWNLOAD_ON = "off";
 	public final static String MESS_EXT = ".mp3";
 
 	public static boolean PREF_EXIST = false;
     public static boolean VOL_EXIST = false;
     public static boolean LAUNCHER_SETS = false;
 	public static String UMS_NB;
 	public static String HOME_URL;
 	public static String CONFIG_URL;
 	public static String TEST_CONN_URL;
 	public static String APK_FILE;
 	public static String STORAGE;
 	public static String PLAYLISTS;
 	public static String MESSAGES;
 	public static String DBCONNECT;
 	public static String COMCONNECT;
 	public static String LOG_RECIEVER;
 	public static String LOGCAT_DIR;
 	public static String TIME_CONNECTION = null;
 	public static long INTERVAL_CONNECTION = 0;
 	public static String LOGCAT_SERVER;
 	public static String LOGPLAY_SERVER;
 	public static String SETTINGS_SERVER;
 	public static String LOGCAT_ON;
 	public static String LOGPLAY_ON;
 	public static String UPGRADE_ON;
 	public static String DOWNLOAD_ON;
 	public static String ID;
 
 	public final static String MAIN_PREF = "UVoxPref";
     public final static String VOL_PREF = "VolumeUVoxPref";
     public final static String LAUNCH_PREF = "LaunchUVoxPref";
 	public final static String LOG_COMMAND_MUSIC = "music";
 	public final static String LOG_COMMAND_MESS = "mess";
 	public final static String BROADCAST_PLAYINFO = "com.ultivox.uvoxplayer.play";
 	public final static String BROADCAST_FINISH = "com.ultivox.uvoxplayer.finish";
 	public final static String BROADCAST_MUSICINFO = "com.ultivox.uvoxplayer.music";
     public final static String BROADCAST_MESSINFO = "com.ultivox.uvoxplayer.message";
     public final static String BROADCAST_VOLUME = "com.ultivox.uvoxplayer.volume";
 
     public final static String reInstallUri = "com.ultivox.reinstall";
     public final static String reBootUri = "com.ultivox.rebootapp";
     public final static String launcherUri = "com.android.launcher";
     public final static String reMoveFile = "Launcher2.apk";
 
 	public final static String BROADCAST_SHOW = "com.ultivox.uvoxplayer.show";
 	public final static String PARAM_TEXT = "text";
 	private BroadcastReceiver brStatus;
 
 	// public final static String Download_ID = "DOWNLOAD_ID";
 	public final static String PLAYINFO = "info";
 	public static float volumeMusic = 1;
 	public static float volumeMessage = 1;
 	public static String[] playInfo;
 	public static TextView textInfo;
 	public static SharedPreferences settings;
 	public static SharedPreferences volumes;
     public static SharedPreferences launchers;
     private SeekBar musicSeekBar = null;
 	private SeekBar messageSeekBar = null;
 	TextView serviceInfo;
 	final static String LOG_TAG = "UVoxPlayerLogs";
 	String playTitle[] = { "Album:", "Artist:", "Song title:", "Genre:",
 			"Duration:", "Bitrate:" };
 	public static NetDbHelper netDbHelper;
 	public static SQLiteDatabase currDb;
 
 	private LogCatTask taskLogCat = null;
 	private LogPlayTask taskLogPlay = null;
 	private SettingsTask taskSettings = null;
 	private TestConnectionTask tcTask = null;
 	private DownloadTask dlTask = null;
 	private UpgradeTask ugTask = null;
     private EnviromentSetupTask enviromentTask = null;
     private RemoveLauncherTask launcherTask = null;
 
     private BroadcastReceiver br;
     private BroadcastReceiver brFinish;
     private BroadcastReceiver brVolume;
 
 	private Intent intentMainService;
 
 	private static ContentResolver contRes;
 
     private VisualizerView mVisualizerView;
     private static int playSessionId = 0;
     private static boolean launcherMainOff = true;
 
     public final static String BROADCAST_ACT_SCH = "com.ultivox.uvoxplayer.main";
     public final static String BROADCAST_ACT_SERVER = "com.ultivox.uvoxplayer.server";
 
     public final static int STATUS_MESSAGE = 100;
     public final static int STATUS_RELAUNCH = 200;
     public final static int STATUS_MESSAGE_STOP = 300;
     public final static int STATUS_PLAY_STOP = 400;
     public final static int STATUS_FADEOUT = 500;
     public final static int STATUS_FADEIN = 600;
     public final static int STATUS_SCHEDUL_END = 700;
     public final static int STATUS_PLAYLIST_EMPTY = 800;
 
     public final static int SERVER_START = 1000;
     public final static int SERVER_CONTINUE = 2000;
     public final static int SERVER_STOP = 3000;
     public final static int SERVER_ERROR = 4000;
     public final static int SERVER_RELOAD = 5000;
 
     public final static String PARAM_RESULT = "result";
     public final static String PARAM_NAME = "name";
 
     PlayMusicService playService;
     ServiceConnection sConn;
     boolean boundPlay = false;
     boolean fade = false;
     boolean playlistEmpty = false;
     boolean playDelay = false;
     Intent intentPlay;
     Intent intentSchedule;
     Intent intentMessage;
 
     public static long lastConnection = 0;
     private static boolean isPlay = false;
     BroadcastReceiver brMain, brServer;
     int mess_name = 0;
     Queue<AsyncTask<String, String, String>> taskQueue = new LinkedList<AsyncTask<String, String, String>>();
     TimeLogInfo tLogCat = null;
     protected boolean isConnectionSession = false;
     protected boolean reLoad = false;
 
     @Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(LOG_TAG, "onCreate");
 		activity = this;
         String mountState = Environment.getExternalStorageState();
         int tries = 60;
         do {
             if (!mountState.equals(Environment.MEDIA_MOUNTED)) {
                 try {
                     Thread.sleep(1000); // sleep for a second
                 } catch (InterruptedException e) {
                     Log.w(LOG_TAG, "Interrupted!");
                     break;
                 }
                 mountState = Environment.getExternalStorageState();
             } else {
                 Log.i(LOG_TAG, "External media mounted");
                 break;
             }
         } while (--tries > 0);
         if (tries == 0) {
             Log.d(LOG_TAG, "Storage not mounted ---- System shutdown!");
             System.exit(0);
         }
 		contRes = getContentResolver();
 		setContentView(R.layout.activity_uvox_player);
 		textInfo = (TextView) findViewById(R.id.textInfo);
 		serviceInfo = (TextView) findViewById(R.id.textService);
 		settings = getSharedPreferences(MAIN_PREF, MODE_PRIVATE);
 		initGlobals(settings);
 		volumes = getSharedPreferences(VOL_PREF, MODE_PRIVATE);
 		initVolumes(volumes);
         launchers = getSharedPreferences(LAUNCH_PREF, MODE_PRIVATE);
         initLaunchers(launchers);
 		createAppDirs();
 
         intentPlay = new Intent(this, PlayMusicService.class);
         intentSchedule = new Intent(this, SchedulerService.class);
         intentMessage = new Intent(this, PlayMessageService.class);
         sConn = new ServiceConnection() {
 
             public void onServiceConnected(ComponentName name, IBinder binder) {
                 Log.d(LOG_TAG, "onServiceConnected");
                 playService = ((PlayMusicService.PlayBinder) binder).getService();
                 boundPlay = true;
             }
 
             public void onServiceDisconnected(ComponentName name) {
                 Log.d(LOG_TAG, "onServiceDisconnected");
                 boundPlay = false;
             }
         };
         brMain = new BroadcastReceiver() {
             // �������� ��� ��������� ���������
 
             @Override
             public void onReceive(Context context, Intent intent) {
 
                 // Analyze extras of broadcast intend received
 
                 int result = intent.getIntExtra(PARAM_RESULT, 0);
                 switch (result) {
                     case STATUS_MESSAGE: {
                         // 1st - make fade-out, if message file exists.
                         mess_name = intent.getIntExtra(PARAM_NAME, 0);
                         pushAlltoQueue(mess_name);
                         if (!fade) {
                             fade = true;
                             Log.d(LOG_TAG, "It's time for message. Id of message:"
                                     + mess_name);
                             playService.fadeOut();
                         }
                         break;
                     }
                     case STATUS_FADEOUT: {
                         // play message
                         Log.d(LOG_TAG, "FadeOut made. File of message:" + mess_name);
                         startService(new Intent(context, PlayMessageService.class)
                                 .putExtra(PARAM_NAME, mess_name));
                         break;
                     }
                     case STATUS_FADEIN: {
                         // the end of fade-out fade-in procedure
                         if (playDelay) { // Reload PlayMusicService if playDelay
                             // triggered on.
                             startService(intentPlay);
                             bindService(intentPlay, sConn, 0);
                             playDelay = false;
                         }
                         fade = false;
                         break;
                     }
                     case STATUS_RELAUNCH: {
                         // re-launch Scheduler Message Service
                         Log.d(LOG_TAG, "Now reloading Message Scheduler");
                         startService(intentSchedule);
                         if (playlistEmpty) {
                             Log.d(LOG_TAG, "Reload PlayMisicService");
                             startService(intentPlay);
                             bindService(intentPlay, sConn, 0);
                             playlistEmpty = false;
                         }
                         break;
                     }
                     case STATUS_PLAYLIST_EMPTY: {
                         // re-launch Scheduler Message Service
                         Log.d(LOG_TAG, "There's no playlist for current hour");
                         playlistEmpty = true;
                         if (boundPlay) {
                             unbindService(sConn);
                             boundPlay = false;
                         }
                         stopService(intentPlay);
                         break;
                     }
                     case STATUS_MESSAGE_STOP: {
                         // message ended, make fade-in
                         Log.d(LOG_TAG, "Message ended");
                         playService.fadeIn();
                         break;
                     }
                     case STATUS_PLAY_STOP: {
                         // message ended, let's play music again
                         Log.d(LOG_TAG,
                                 "Play new track"
                                         + String.format("   boundPlay=%b",
                                         boundPlay));
                         if (boundPlay) {
                             unbindService(sConn);
                             boundPlay = false;
                         }
                         stopService(intentPlay);
                        try {
                            mVisualizerView.clearRenderers();
                            mVisualizerView.setEnabled(false);
                        } catch (IllegalStateException	 e) {
                            e.printStackTrace();
                        }
                         if (fade) { // end of track in time with fade-out fade-in
                             // procedure - stop delayPlayMusicService, but
                             // delay it restart until this procedure will
                             // ended
                             playDelay = true;
                         } else {
                             startService(intentPlay);
                             bindService(intentPlay, sConn, 0);
                         }
                         break;
                     }
                     case STATUS_SCHEDUL_END: {
                         stopService(intentSchedule);
                         break;
                     }
                 }
             }
 
         };
         IntentFilter intFilt = new IntentFilter(BROADCAST_ACT_SCH);
         registerReceiver(brMain, intFilt);
 
         brServer = new BroadcastReceiver() {
 
             @Override
             public void onReceive(Context context, Intent intent) {
 
                 int result = intent.getIntExtra(PARAM_RESULT, 0);
                 Log.d(LOG_TAG,
                         String.format("Connect intent recieved #%d", result));
                 AlarmManager alarmConnectServer = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                 switch (result) {
                     case SERVER_START: {
                         if (UVoxPlayer.LOGCAT_ON.equals("on")) {
                             tLogCat = new TimeLogInfo(context);
                             taskQueue.offer(new LogCatTask(context));
                         } else {
                            String logDirPath = Environment.getExternalStorageDirectory()
                                     .toString() + UVoxPlayer.LOGCAT_DIR;
                             File logDir = new File(logDirPath);
                             if (logDir.exists()) {
                                 String[] logFiles = logDir.list();
                                 for (int i=0; i<logFiles.length; i++ ) {
                                     File f = new File(logDirPath + File.separator + logFiles[i]);
                                     if (f.exists()) {
                                         f.delete();
                                     }
                                 }
                             }
                         }
                         if (UVoxPlayer.LOGPLAY_ON.equals("on")) {
                             taskQueue.offer(new LogPlayTask(context));
                         }
                         if (UVoxPlayer.UPGRADE_ON.equals("on")) {
                             taskQueue.offer(new UpgradeTask(context));
                         }
                         if (UVoxPlayer.DOWNLOAD_ON.equals("on")) {
                             taskQueue.offer(new DownloadTask(context));
                         }
                         taskQueue.offer(new SettingsTask(context));
                         isConnectionSession = true;
                         LogPlay.write("system", "Server connection", "start");
                         SysInfo sys = new SysInfo();
                         LogPlay.write("system", sys.getMainMemFree(), "info");
                         LogPlay.write("system", sys.getIntMemFree(), "info");
                         LogPlay.write("system", sys.getExtMemFree(), "info");
                         Intent mesServ = new Intent(BROADCAST_ACT_SERVER);
                         mesServ.putExtra(PARAM_RESULT, SERVER_CONTINUE);
                         sendBroadcast(mesServ);
                         break;
                     }
                     case SERVER_CONTINUE: {
                         if (!isConnectionSession) {
                             Intent mesServ = new Intent(BROADCAST_ACT_SERVER);
                             mesServ.putExtra(PARAM_RESULT, SERVER_STOP);
                             sendBroadcast(mesServ);
                             break;
                         }
                         AsyncTask<String, String, String> currentTask = taskQueue
                                 .poll();
                         if (currentTask != null) {
                             if (tLogCat != null) {
                                 currentTask.execute(tLogCat.getLogFile());
                             } else {
                                 currentTask.execute();
                             }
                         } else {
                             Intent mesServ = new Intent(BROADCAST_ACT_SERVER);
                             mesServ.putExtra(PARAM_RESULT, SERVER_STOP);
                             sendBroadcast(mesServ);
                         }
                         break;
                     }
                     case SERVER_STOP: {
                         if (reLoad) {
                             Log.d(LOG_TAG, "Now process will be reloded!");
                             // NOTsilent!!!
                             // Intent intent = new Intent(Intent.ACTION_VIEW);
                             // intent.setDataAndType(Uri.fromFile(new
                             // File("/mnt/sdcard/Download/UVoxPlayer.apk")),
                             // "application/vnd.android.package-archive");
                             // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
                             // without this flag android returned a intent error!
                             // this.startActivity(intent);
                             // Silent
                             Intent mesFinish = new Intent(UVoxPlayer.BROADCAST_FINISH);
                             sendBroadcast(mesFinish);
                             reLoad = false;
                             return;
                         }
                         LogPlay.write("system", "Server connection", "stop");
                         Intent intStartServer = new Intent(BROADCAST_ACT_SERVER);
                         intStartServer.putExtra(PARAM_RESULT, SERVER_START);
                         PendingIntent pendIntentStartServer = PendingIntent
                                 .getBroadcast(context, 0, intStartServer, 0);
                         long nextTimeConnect = 0;
                         if (UVoxPlayer.INTERVAL_CONNECTION > 0) {
                             nextTimeConnect = Calendar.getInstance()
                                     .getTimeInMillis()
                                     + UVoxPlayer.INTERVAL_CONNECTION;
                         } else if (!UVoxPlayer.TIME_CONNECTION.equals("")) {
                             try {
                                 nextTimeConnect = dateParseTimeRegExp(UVoxPlayer.TIME_CONNECTION);
                             } catch (IllegalArgumentException e) {
                                 nextTimeConnect = Calendar.getInstance()
                                         .getTimeInMillis()
                                         + UVoxPlayer.INTERVAL_CONNECTION_STAT;
                                 e.printStackTrace();
                             }
                         } else {
                             nextTimeConnect = Calendar.getInstance()
                                     .getTimeInMillis()
                                     + UVoxPlayer.INTERVAL_CONNECTION_STAT;
                         }
                         alarmConnectServer.set(AlarmManager.RTC_WAKEUP,
                                 nextTimeConnect, pendIntentStartServer);
                         isConnectionSession = false;
                         lastConnection = Calendar.getInstance().getTimeInMillis();
                         Log.d(LOG_TAG,
                                 String.format(
                                         "Set alarm to next server connection on %d sec delay",
                                         (nextTimeConnect - Calendar.getInstance()
                                                 .getTimeInMillis()) / 1000));
                         break;
                     }
                     case SERVER_ERROR: {
                         LogPlay.write("system", "Server connection", "error");
                         if (!isConnectionSession) {
                             break;
                         }
                         Intent intStartServer = new Intent(BROADCAST_ACT_SERVER);
                         intStartServer.putExtra(PARAM_RESULT, SERVER_START);
                         PendingIntent pendIntentStartServer = PendingIntent
                                 .getBroadcast(context, 0, intStartServer, 0);
                         long nextTimeConnect = 0;
                         nextTimeConnect = Calendar.getInstance().getTimeInMillis()
                                 + UVoxPlayer.INTERVAL_CONNECTION_STAT;
                         alarmConnectServer.set(AlarmManager.RTC_WAKEUP,
                                 nextTimeConnect, pendIntentStartServer);
                         isConnectionSession = false;
                         Log.d(LOG_TAG,
                                 String.format(
                                         "Task queue ERROR! Set alarm to next server connection on %d (hour delay)",
                                         (nextTimeConnect - Calendar.getInstance()
                                                 .getTimeInMillis()) / 1000));
                         break;
                     }
                     case SERVER_RELOAD: {
                         reLoad = true;
                         Intent mesServ = new Intent(BROADCAST_ACT_SERVER);
                         mesServ.putExtra(PARAM_RESULT, SERVER_CONTINUE);
                         sendBroadcast(mesServ);
                         break;
                     }
                 }
 
             }
         };
         // Set filter for server BroadcastReceiver
         IntentFilter intFiltServer = new IntentFilter(BROADCAST_ACT_SERVER);
         // turn-on server BroadcastReceiver
         registerReceiver(brServer, intFiltServer);
 
 		brStatus = new BroadcastReceiver() {
 			// �������� ��� ��������� ���������
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 
 				String infoService = intent.getStringExtra(PARAM_TEXT);
 				serviceInfo.setText(infoService);
 			}
 		};
 		IntentFilter intFStatus = new IntentFilter(BROADCAST_SHOW);
 		registerReceiver(brStatus, intFStatus);
 
 		// Setup music volume control
 		musicSeekBar = (SeekBar) findViewById(R.id.seekBarMusic);
 		musicSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				volumeMusic = (float) progress / 100;
 				serviceInfo.setText(String.format("Volume level:%d %%",
 						progress));
 				Intent mesMusic = new Intent(BROADCAST_MUSICINFO);
 				sendBroadcast(mesMusic);
 
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				SharedPreferences.Editor editor = volumes.edit();
 				editor.putFloat("MUSIC", volumeMusic);
 				editor.commit();
 				serviceInfo.setText("");
 
 			}
 		});
 
 		// Setup message volume control
 		messageSeekBar = (SeekBar) findViewById(R.id.seekBarMess);
 		messageSeekBar
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						volumeMessage = (float) progress / 100;
 						serviceInfo.setText(String.format(
 								"Message level:%d %%", progress));
 						Intent mesMess = new Intent(BROADCAST_MESSINFO);
 						sendBroadcast(mesMess);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 
 					}
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						SharedPreferences.Editor editor = volumes.edit();
 						editor.putFloat("MESSAGE", volumeMessage);
 						editor.commit();
 						serviceInfo.setText("");
 
 					}
 				});
 		br = new BroadcastReceiver() {
 			// �������� ��� ��������� ���������
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 
 				playInfo = intent.getStringArrayExtra(PLAYINFO);
 				textInfo.setText("");
 				for (int i = 0; i < playTitle.length; i++) {
 					textInfo.append(playTitle[i] + playInfo[i] + "\n");
 				}
 			}
 		};
 		// ������� ������ ��� BroadcastReceiver
 		IntentFilter intFiltPlay = new IntentFilter(BROADCAST_PLAYINFO);
 		// ������������ (��������) BroadcastReceiver
 		registerReceiver(br, intFiltPlay);
 
 		brFinish = new BroadcastReceiver() {
 			// �������� ��� ��������� ���������
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 
 				Log.d(LOG_TAG, "Finish Activity");
 				Log.d(LOG_TAG, "REBOOOT!");
 				Intent inst = new Intent();
 				inst.setClassName("com.ultivox.reinstall", "com.ultivox.reinstall.MainActivity");
 				inst.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startActivity(inst);  
 				finish();
 			}
 		};
 
 		IntentFilter intFinish = new IntentFilter(BROADCAST_FINISH);
 		registerReceiver(brFinish, intFinish);
 
         brVolume = new BroadcastReceiver() {
 
             @Override
             public void onReceive(Context context, Intent intent) {
 
                 int result = intent.getIntExtra(PARAM_PLAYER, 0);
                 Log.d(LOG_TAG, String.format("Receive visulizer brodcast with %d extra",result));
                 try {
                     if ((result==0)&&(playSessionId!=0)) {
                         // song stop to play
                         playSessionId = result;
                         mVisualizerView.setVisibility (View.INVISIBLE);
                         mVisualizerView.setOff();
                         mVisualizerView.release();
                     }
                     if ((result!=0)&&(playSessionId==0)) {
                         // song start to play
                         playSessionId = result;
                         mVisualizerView.link(playSessionId);
                         mVisualizerView.setVisibility (View.VISIBLE);
                     }
                 } catch (IllegalStateException	 e) {
                     e.printStackTrace();
                 }
 
             }
         };
         IntentFilter intVolume = new IntentFilter(BROADCAST_VOLUME);
         registerReceiver(brVolume, intVolume);
 
 		Intent intStartServer = new Intent(BROADCAST_ACT_SERVER);
 		intStartServer.putExtra(PARAM_RESULT, SERVER_START);
 		PendingIntent pendIntentStartServer = PendingIntent.getBroadcast(this,
 				0, intStartServer, 0);
 		long nextTimeConnect = 0;
 		AlarmManager alarmConnectServer = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		if (INTERVAL_CONNECTION > 0) {
 			nextTimeConnect = Calendar.getInstance().getTimeInMillis()
 					+ INTERVAL_CONNECTION;
 		} else if (!TIME_CONNECTION.equals("")) {
 			try {
 				nextTimeConnect =dateParseTimeRegExp(TIME_CONNECTION);
 			} catch (IllegalArgumentException e) {
 				nextTimeConnect = Calendar.getInstance().getTimeInMillis()
 						+ INTERVAL_CONNECTION_STAT;
 				e.printStackTrace();
 			}
 		} else {
 			nextTimeConnect = Calendar.getInstance().getTimeInMillis()
 					+ UVoxPlayer.INTERVAL_CONNECTION_STAT;
 		}
 		alarmConnectServer.set(AlarmManager.RTC_WAKEUP, nextTimeConnect,
 				pendIntentStartServer);
 		Log.d(LOG_TAG, String
 				.format("Set alarm to next server connection after %d sec",
 						(nextTimeConnect - Calendar.getInstance()
 								.getTimeInMillis()) / 1000));
         // Start all services
         Log.d(LOG_TAG, "Start all services");
         startService(intentSchedule);
         startService(intentPlay);
         bindService(intentPlay, sConn, 0);
         isPlay = true;
 
 //        if (!appInstalledOrNot(reBootUri) || !appInstalledOrNot(reInstallUri)) {
 //            if (enviromentTask == null) {
 //                enviromentTask = new EnviromentSetupTask(this);
 //                enviromentTask.runTask(null);
 //            }
 //        }
 //        if ((appInstalledOrNot(launcherUri))&&launcherMainOff) {
 //            if (launcherTask == null) {
 //                launcherTask = new RemoveLauncherTask(this);
 //                launcherTask.runTask("remove");
 //            }
 //        }
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		netDbHelper = new NetDbHelper(this, null, null, 1);
 		currDb = netDbHelper.getWritableDatabase();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(LOG_TAG, "onResume");
 		textInfo.setText("");
 		if (playInfo != null)
 			for (int i = 0; i < playInfo.length; i++) {
 				textInfo.append(playTitle[i] + playInfo[i] + "\n");
 			}
 		musicSeekBar.setProgress((int) (volumeMusic * 100));
 		messageSeekBar.setProgress((int) (volumeMessage * 100));
         mVisualizerView = (VisualizerView) findViewById(R.id.viewVolume);
         serviceInfo.setText("");
 	}
 
 	@Override
 	public void onBackPressed() {
 		return;
 	}
 
 	public void onClickStart(View v) {
 		LogPlay.write("button", "Start", "press");
         if (!isPlay) {
             startService(intentSchedule);
             startService(intentPlay);
             bindService(intentPlay, sConn, 0);
             isPlay = true;
         }
     }
 
 	public void onClickStop(View v) {
         try {
             mVisualizerView.clearRenderers();
             mVisualizerView.setEnabled(false);
         } catch (IllegalStateException	 e) {
             e.printStackTrace();
         }
         LogPlay.write("button", "Stop", "press");
 		if (taskLogCat != null
 				&& taskLogCat.getStatus() != AsyncTask.Status.FINISHED) {
 			taskLogCat.cancel(true);
 			taskLogCat = null;
 		}
 		if (taskLogPlay != null
 				&& taskLogPlay.getStatus() != AsyncTask.Status.FINISHED) {
 			taskLogPlay.cancel(true);
 			taskLogPlay = null;
 		}
 		if (taskSettings != null
 				&& taskSettings.getStatus() != AsyncTask.Status.FINISHED) {
 			taskSettings.cancel(true);
 			taskSettings = null;
 		}
 		if (tcTask != null && tcTask.getStatus() != AsyncTask.Status.FINISHED) {
 			tcTask.cancel(true);
 			tcTask = null;
 		}
 		if (dlTask != null && dlTask.getStatus() != AsyncTask.Status.FINISHED) {
 			dlTask.cancel(true);
 			dlTask = null;
 		}
 		if (ugTask != null && ugTask.getStatus() != AsyncTask.Status.FINISHED) {
 			ugTask.cancel(true);
 			ugTask = null;
 		}
         if (boundPlay) {
             unbindService(sConn);
             boundPlay = false;
         }
         isPlay = false;
         stopService(intentSchedule);
         stopService(intentPlay);
         stopService(intentMessage);
     }
 
 	public void onClickNetset(View v) {
 		LogPlay.write("button", "Net setting", "press");
 		Intent i = new Intent(
 				android.provider.Settings.ACTION_WIRELESS_SETTINGS);
 		i.putExtra(":android:show_fragment",
 				"com.android.settings.WirelessSettings");
 		i.putExtra(":android:no_headers", true);
 		startActivityForResult(i, 0);
 	}
 
 	public void onClickTimeset(View v) {
 		LogPlay.write("button", "Time setting", "press");
 		Intent i = new Intent(android.provider.Settings.ACTION_DATE_SETTINGS);
 		i.putExtra(":android:show_fragment",
 				"com.android.settings.DateTimeSettings");
 		i.putExtra(":android:no_headers", true);
 		startActivityForResult(i, 0);
 	}
 
 	public void onClickTest(View v) {
 		LogPlay.write("button", "Test connection", "press");
 		if (tcTask != null) {
 			AsyncTask.Status tcStatus = tcTask.getStatus();
 			if (tcStatus != AsyncTask.Status.FINISHED) {
 				Log.v(LOG_TAG, "... no need to start new test connection task");
 				return;
 			}
 		}
 		tcTask = new TestConnectionTask(this);
 		tcTask.runTask(null);
 	}
 
 	public void onClickDownload(View v) {
 		LogPlay.write("button", "Download", "press");
 		if (dlTask != null) {
 			AsyncTask.Status dlStatus = dlTask.getStatus();
 			if (dlStatus != AsyncTask.Status.FINISHED) {
 				Log.v(LOG_TAG, "... no need to start new download task");
 				return;
 			}
 		}
 		dlTask = new DownloadTask(this);
 		dlTask.runTask(null);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.uvox_player, menu);
 		return true;
 	}
 
 	// Menu handlers
 
 	public void onUpgrade(MenuItem item) {
 		LogPlay.write("button", "Upgrade software", "press");
 		if (ugTask != null) {
 			AsyncTask.Status ugStatus = ugTask.getStatus();
 			if (ugStatus != AsyncTask.Status.FINISHED) {
 				Log.v(LOG_TAG, "... no need to start new task");
 				return;
 			}
 		}
 		ugTask = new UpgradeTask(this);
 		ugTask.runTask(null);
 	}
 
 	public void onTerminal(MenuItem item) {
 		LogPlay.write("button", "Terminal option", "press");
 		PackageManager pm = getPackageManager();
 		try {
 			pm.getPackageInfo("jackpal.androidterm",
 					PackageManager.GET_META_DATA);
 		} catch (NameNotFoundException e) {
 			Log.v(LOG_TAG, "jackpal.androidterm package not installed");
 			return;
 		}
 		Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(
 				"jackpal.androidterm");
 		startActivity(LaunchIntent);
 	}
 
 	public void onLogCat(MenuItem item) {
 
 		LogPlay.write("button", "LogCat option", "press");
 		TimeLogInfo newLog = new TimeLogInfo(this);
 		if (newLog.getLogFile() != null) {
 			if (taskLogCat != null) {
 				AsyncTask.Status dlStatus = taskLogCat.getStatus();
 				if (dlStatus != AsyncTask.Status.FINISHED) {
 					Log.v(LOG_TAG, " client-server task allready run");
 					return;
 				}
 			}
 			taskLogCat = new LogCatTask(this);
 			taskLogCat.runTask(newLog.getLogFile());
 		}
 	}
 
 	public void onLogPlay(MenuItem item) {
 
 		LogPlay.write("button", "LogPlay option", "press");
 		if (taskLogPlay != null) {
 			AsyncTask.Status dlStatus = taskLogPlay.getStatus();
 			if (dlStatus != AsyncTask.Status.FINISHED) {
 				Log.v(LOG_TAG, " client-server task allready run");
 				return;
 			}
 		}
 		taskLogPlay = new LogPlayTask(this);
 		taskLogPlay.runTask(null);
 	}
 
 	public void onSettings(MenuItem item) {
 
 		LogPlay.write("button", "Settings option", "press");
 		if (taskSettings != null) {
 			AsyncTask.Status dlStatus = taskSettings.getStatus();
 			if (dlStatus != AsyncTask.Status.FINISHED) {
 				Log.v(LOG_TAG, " client-server task allready run");
 				return;
 			}
 		}
 		taskSettings = new SettingsTask(this);
 		taskSettings.runTask(null);
 	}
 
     public void onExit(MenuItem item) throws ClassNotFoundException,
             NoSuchMethodException {
         LogPlay.write("button", "About option", "press");
         System.exit(0);
     }
 
     public void onRemove(MenuItem item)  {
 
         LogPlay.write("button", "Remove launcher", "press");
         launcherMainOff = true;
         SharedPreferences.Editor editor = launchers.edit();
         editor.putBoolean("LAUNCHER_MAIN_OFF", launcherMainOff);
         editor.commit();
         if (appInstalledOrNot(launcherUri)) {
             Log.d(LOG_TAG, "Now main launcher will removed");
             launcherTask = new RemoveLauncherTask(this);
             launcherTask.runTask("remove");
         }
     }
 
     public void onShowVal(MenuItem item)  {
 
         LogPlay.write("button", "Show values", "press");
         String toastLine = "Serial number: "+ UMS_NB;
         try {
             PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
             toastLine = toastLine + String.format("\n Version code: %d", pInfo.versionCode);
 
         } catch (Exception e) {
             Log.e(LOG_TAG, "Check version error! " + e.getMessage());
         }
         Toast.makeText(getApplicationContext(),
                 toastLine, Toast.LENGTH_LONG).show();
 
     }
 
 	public void onRestore(MenuItem item) {
 
 		LogPlay.write("button", "Restore launcher", "press");
         if (!appInstalledOrNot(launcherUri)) {
             Log.d(LOG_TAG, "Now main launcher will restored");
             launcherTask = new RemoveLauncherTask(this);
             launcherTask.runTask("install");
         }
         launcherMainOff = false;
         SharedPreferences.Editor editor = launchers.edit();
         editor.putBoolean("LAUNCHER_MAIN_OFF", launcherMainOff);
         editor.commit();
 	}
 
 
     public void onAbout(MenuItem item)  {
 
     }
 
 	@Override
 	public void onPause() {
 		super.onPause();
 //		currDb.close();
 		Log.d(LOG_TAG, "onPause");
 	}
 
 	@Override
 	public void onDestroy() {
 
 		super.onDestroy();
 		currDb.close();
 		unregisterReceiver(br);
 		unregisterReceiver(brFinish);
         unregisterReceiver(brStatus);
         unregisterReceiver(brServer);
         unregisterReceiver(brMain);
 		Log.d(LOG_TAG, "Player stoped");
 	}
 
 	public static void initGlobals(SharedPreferences set) {
 
 		Log.d(LOG_TAG, "Init Prepererences");
 		if (set.getBoolean("PREF_EXIST", false)) {
 			PREF_EXIST = set.getBoolean("PREF_EXIST", PREF_EXIST);
 			UMS_NB = set.getString("UMS_NB", UMS_NB);
 			HOME_URL = set.getString("HOME_URL", HOME_URL);
 			CONFIG_URL = set.getString("CONFIG_URL", CONFIG_URL);
 			TEST_CONN_URL = set.getString("TEST_CONN_URL", TEST_CONN_URL);
 			APK_FILE = set.getString("APK_FILE", APK_FILE);
 			STORAGE = set.getString("STORAGE", STORAGE);
 			PLAYLISTS = set.getString("PLAYLISTS", PLAYLISTS);
 			MESSAGES = set.getString("MESSAGES", MESSAGES);
 			DBCONNECT = set.getString("DBCONNECT", DBCONNECT);
 			COMCONNECT = set.getString("COMCONNECT", COMCONNECT);
 			LOG_RECIEVER = set.getString("LOG_RECIEVER", LOG_RECIEVER);
 			LOGCAT_DIR = set.getString("LOGCAT_DIR", LOGCAT_DIR);
 			TIME_CONNECTION = set.getString("TIME_CONNECTION", TIME_CONNECTION);
 			INTERVAL_CONNECTION = set.getLong("INTERVAL_CONNECTION",
 					INTERVAL_CONNECTION);
 			LOGCAT_SERVER = set.getString("LOGCAT_SERVER", LOGCAT_SERVER);
 			LOGPLAY_SERVER = set.getString("LOGPLAY_SERVER", LOGPLAY_SERVER);
 			SETTINGS_SERVER = set.getString("SETTINGS_SERVER", SETTINGS_SERVER);
 			LOGCAT_ON = set.getString("LOGCAT_ON", LOGCAT_ON);
 			LOGPLAY_ON = set.getString("LOGPLAY_ON", LOGPLAY_ON);
 			UPGRADE_ON = set.getString("UPGRADE_ON", UPGRADE_ON);
 			DOWNLOAD_ON = set.getString("DOWNLOAD_ON", DOWNLOAD_ON);
 			ID = set.getString("ID", ID);
 
 		} else {
 			SharedPreferences.Editor editor = set.edit();
 			PREF_EXIST = true;
 			editor.putBoolean("PREF_EXIST", PREF_EXIST);
 			UMS_NB = INIT_UMS_NB;
 			editor.putString("UMS_NB", UMS_NB);
 			HOME_URL = INIT_HOME_URL;
 			editor.putString("HOME_URL", HOME_URL);
 			CONFIG_URL = INIT_CONFIG_URL;
 			editor.putString("CONFIG_URL", CONFIG_URL);
 			TEST_CONN_URL = INIT_TEST_CONN_URL;
 			editor.putString("TEST_CONN_URL", TEST_CONN_URL);
 			APK_FILE = INIT_APK_FILE;
 			editor.putString("APK_FILE", APK_FILE);
 			STORAGE = INIT_STORAGE;
 			editor.putString("STORAGE", STORAGE);
 			PLAYLISTS = INIT_PLAYLISTS;
 			editor.putString("PLAYLISTS", PLAYLISTS);
 			MESSAGES = INIT_MESSAGES;
 			editor.putString("MESSAGES", MESSAGES);
 			DBCONNECT = INIT_DBCONNECT;
 			editor.putString("DBCONNECT", DBCONNECT);
 			COMCONNECT = INIT_COMCONNECT;
 			editor.putString("COMCONNECT", COMCONNECT);
 			LOG_RECIEVER = INIT_LOG_RECIEVER;
 			editor.putString("LOG_RECIEVER", LOG_RECIEVER);
 			LOGCAT_DIR = INIT_LOGCAT_DIR;
 			editor.putString("LOGCAT_DIR", LOGCAT_DIR);
 			TIME_CONNECTION = INIT_TIME_CONNECTION;
 			editor.putString("TIME_CONNECTION", TIME_CONNECTION);
 			INTERVAL_CONNECTION = INIT_INTERVAL_CONNECTION;
 			editor.putLong("INTERVAL_CONNECTION", INTERVAL_CONNECTION);
 			LOGCAT_SERVER = INIT_LOGCAT_SERVER;
 			editor.putString("LOGCAT_SERVER", LOGCAT_SERVER);
 			LOGPLAY_SERVER = INIT_LOGPLAY_SERVER;
 			editor.putString("LOGPLAY_SERVER", LOGPLAY_SERVER);
 			SETTINGS_SERVER = INIT_SETTINGS_SERVER;
 			editor.putString("SETTINGS_SERVER", SETTINGS_SERVER);
 			LOGCAT_ON = INIT_LOGCAT_ON;
 			editor.putString("LOGCAT_ON", LOGCAT_ON);
 			LOGPLAY_ON = INIT_LOGPLAY_ON;
 			editor.putString("LOGPLAY_ON", LOGPLAY_ON);
 			UPGRADE_ON = INIT_UPGRADE_ON;
 			editor.putString("UPGRADE_ON", UPGRADE_ON);
 			DOWNLOAD_ON = INIT_DOWNLOAD_ON;
 			editor.putString("DOWNLOAD_ON", DOWNLOAD_ON);
 
 			ID = Settings.Secure.getString(contRes, Settings.Secure.ANDROID_ID);
 			editor.putString("ID", ID);
 			editor.commit();
 		}
 	}
 
 	public void initLaunchers(SharedPreferences set) {
 
         Log.d(LOG_TAG, "Set launchers");
         if (set.getBoolean("LAUNCHER_SETS", false)) {
             LAUNCHER_SETS = set.getBoolean("LAUNCHER_SETS", LAUNCHER_SETS);
             launcherMainOff = set.getBoolean("LAUNCHER_MAIN_OFF", true);
         } else {
             SharedPreferences.Editor editor = set.edit();
             LAUNCHER_SETS = true;
             editor.putBoolean("LAUNCHER_SETS", LAUNCHER_SETS);
             editor.putBoolean("LAUNCHER_MAIN_OFF", launcherMainOff);
             editor.commit();
         }
     }
 
     public void initVolumes(SharedPreferences set) {
 
         Log.d(LOG_TAG, "Adjust volumes");
         if (set.getBoolean("VOL_EXIST", false)) {
             VOL_EXIST = set.getBoolean("VOL_EXIST", VOL_EXIST);
             volumeMusic = set.getFloat("MUSIC", 1);
             volumeMessage = set.getFloat("MESSAGE", 1);
         } else {
             SharedPreferences.Editor editor = set.edit();
             VOL_EXIST = true;
             editor.putBoolean("VOL_EXIST", VOL_EXIST);
             editor.putFloat("MUSIC", 1);
             editor.putFloat("MESSAGE", 1);
             editor.commit();
         }
     }
 	private void createAppDirs() {
 
 		String base = UVoxEnvironment.getExternalStorageDirectory().toString()
 				+ INIT_STORAGE;
 		Log.d(LOG_TAG, "Restore filesystem"+base);
 		File dir = new File(base);
 		if (!dir.exists()) {
 			Log.d(LOG_TAG,base);
 			dir.mkdir();
 		}
 		dir = new File(base + PLAYLISTS);
 		if (!dir.exists()) {
 			Log.d(LOG_TAG,base + PLAYLISTS);
 			dir.mkdir();
 		}
 		dir = new File(base + MESSAGES);
 		if (!dir.exists()) {
 			Log.d(LOG_TAG,base + MESSAGES);
 			dir.mkdir();
 		}
 	}
 
     private boolean appInstalledOrNot(String uri)
     {
         PackageManager pm = getPackageManager();
         boolean app_installed = false;
         try
         {
             PackageInfo pi = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
             Log.d(LOG_TAG, pi.packageName);
             app_installed = true;
         }
         catch (PackageManager.NameNotFoundException e)
         {
             app_installed = false;
         }
         return app_installed ;
     }
 
     private void pushAlltoQueue(int messSetId) {
 
         final String TAG = "pushAlltoQueue";
         String[] columns = new String[] { NetDbHelper.MESS_SETS_MESS_ID,
                 NetDbHelper.MESS_SETS_FILE, NetDbHelper.MESS_SETS_ORDER,
                 NetDbHelper.MESS_SETS_DURATION };
         String selection = NetDbHelper.MESS_SETS_MESS_ID + " = ? ";
         String[] selectionArgs = null;
         String orderBy = NetDbHelper.MESS_SETS_ORDER + " ASC";
 
         Cursor curMessage;
 
         Log.d(TAG, String.format("Recieve intent %d", messSetId));
         selectionArgs = new String[] { String.format("%d", messSetId) };
         curMessage = UVoxPlayer.currDb.query(NetDbHelper.TABLE_MESS_SETS,
                 columns, selection, selectionArgs, null, null, orderBy);
         if (curMessage.moveToFirst()) {
             Log.d(TAG, String.format("Files number: %d", curMessage.getCount()));
             do {
                 PlayMessageService.messQueue.add(curMessage
                         .getString(curMessage
                                 .getColumnIndex(NetDbHelper.MESS_SETS_FILE)));
                 Log.d(TAG, String.format("File %s order %d", curMessage
                         .getString(curMessage
                                 .getColumnIndex(NetDbHelper.MESS_SETS_FILE)),
                         curMessage.getInt(curMessage
                                 .getColumnIndex(NetDbHelper.MESS_SETS_ORDER))));
             } while (curMessage.moveToNext());
         }
     }
 
     public static long dateParseTimeRegExp(String period) {
 
         Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
         Calendar m = Calendar.getInstance(); // midnight
         m.set(Calendar.HOUR_OF_DAY, 0);
         m.set(Calendar.MINUTE, 0);
         m.set(Calendar.SECOND, 0);
         m.set(Calendar.MILLISECOND, 0);
         long now = Calendar.getInstance().getTimeInMillis();
         long tillMidnight = now - m.getTimeInMillis();
         long event = 0L;
         Matcher matcher = pattern.matcher(period);
         if (matcher.matches()) {
             event = Long.parseLong(matcher.group(1)) * 3600000L
                     + Long.parseLong(matcher.group(2)) * 60000
                     + Long.parseLong(matcher.group(3)) * 1000;
         } else {
             throw new IllegalArgumentException("Invalid format " + period);
         }
         if (event > tillMidnight) {
             return m.getTimeInMillis() + event;
         } else {
             m.add(Calendar.DAY_OF_MONTH, 1);
             return m.getTimeInMillis() + event;
         }
     }
 }
