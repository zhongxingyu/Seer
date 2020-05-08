 package com.sonrlabs.test.sonr;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.acra.ErrorReporter;
 
 import android.app.ListActivity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.AudioManager;
 import android.media.AudioRecord;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.InflateException;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.flurry.android.FlurryAgent;
 import com.sonrlabs.test.sonr.common.Common;
 import com.sonrlabs.test.sonr.common.DialogCommon;
 import com.sonrlabs.test.sonr.signal.AudioProcessor;
 
 // This is what happens when you open the SONR app on the home screen.
 public class SONR
       extends ListActivity {
 
    public static final int SONR_ID = 1;
    public static final String DOCK_NOT_FOUND = "DOCK NOT FOUND";
    public static final String DOCK_FOUND = "DOCK FOUND";
    public static final String DEFAULT_PLAYER_SELECTED = "DEFAULT_PLAYER_SELECTED";
    public static final String APP_PACKAGE_NAME = "APP_PACKAGE_NAME";
    public static final String DISCONNECT_ACTION = "android.intent.action.DISCONNECT_DOCK";
    public static final String SHARED_PREFERENCES = "SONRSharedPreferences";
 
    private static final String SAMPLE_URI = "\\";
    private static final String AUDIO_MIME_TYPE = "audio/*";
    private static final String DOCK_NOT_FOUND_TRY_AGAIN = "Dock not detected, check connections and try again";
    private static final String OK_TXT = "Ok";
    private static final String SELECT_PLAYER = "Please select a music player";
    private static final String APP_FULL_NAME = "APP_FULL_NAME";
    private static final String PLAYER_SELECTED = "PLAYER_SELECTED";
    private static final String FIRST_LAUNCH = "FIRST_LAUNCH";
    private static final String TAG = SONR.class.getSimpleName();
    private static final Map<String, String> flurryParams = new HashMap<String, String>();
 
    private static SONR instance;
    
    /* XXX:  Dubious that these should be static. */
    private static boolean on = false;
    private static boolean mainScreen = false;
    
    private List<ApplicationInfo> infos = null;
    private int currentlySelectedApplicationInfoIndex;
    private SONRClient client;
    private AudioRecord audio;
    private final BroadcastReceiver stopReceiver = new StopReceiver();
    private boolean isRegistered = false;
    private PowerManager.WakeLock mWakeLock;
 
    private View.OnClickListener noneButtonListener = new View.OnClickListener() {
       @Override
       public void onClick(View v) {
          new CheckDockOnNoneSelection(v).start();
       }
    };
 
    public SONR() {
       instance = this;
    }
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       try {
          Common.save(this, PLAYER_SELECTED, false);
          mainScreen = true;
          currentlySelectedApplicationInfoIndex = -1;
          setContentView(R.layout.music_select_main);
          if (isFirstLaunch()) {
             // Show Intro screen
             Common.save(this, FIRST_LAUNCH, false);
             Intent intent = new Intent(this, IntroScreen.class);
             startActivity(intent);
          }
 
          if (!isRegistered) {
             registerReceiver(stopReceiver, new IntentFilter(DISCONNECT_ACTION));
             isRegistered = true;
          }
 
          infos = convert(this, findActivities(this));
          ListAdapter adapter = new AppInfoAdapter(this, infos);
          setListAdapter(adapter);
 
          AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
          int savedNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
          SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, 0);
          sharedPreferences.edit().putInt("savedNotificationVolume", savedNotificationVolume).commit();
 
          if (!ToggleSONR.SERVICE_ON) {
             Intent i = new Intent(this, ToggleSONR.class);
             startService(i);
          }
 
          audio = AudioProcessor.findAudioRecord();
          client = new SONRClient(this, audio, audioManager);
          client.onCreate();
 
          Button noneButton = (Button) findViewById(R.id.noneButton);
          noneButton.setOnClickListener(noneButtonListener);
 
          final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
          mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
          mWakeLock.acquire();
 
          // Intent intent;
          if (Common.get(this, DEFAULT_PLAYER_SELECTED, false)) {
 
             if (client != null) {
                client.onDestroy();
             }
 
             if (audio != null) {
                audio.release();
             }
             // intent = new Intent(this, DefaultAppScreen.class);
          } else {
             // intent = new Intent(this, IntroScreen.class);
          }
          // startActivity(intent);
       } catch (RuntimeException e) {
          e.printStackTrace();
          ErrorReporter.getInstance().handleException(e);
       }
    }
 
    @Override
    protected void onStart() {
       super.onStart();
       FlurryAgent.onStartSession(this, "NNCR41GZ52ZYBXPZPTGT");
    }
 
    @Override
    protected void onStop() {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
 
    @Override
    protected void onDestroy() {
       try {
          unregisterReceiver(stopReceiver);
          super.onDestroy();
          if (audio != null) {
             audio.release();
          }
 
          if (audio != null) {
             audio.release();
          }
          if (client != null) {
             client.onDestroy();
          }
 
          mainScreen = false;
          on = false;
          String ns = Context.NOTIFICATION_SERVICE;
          NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
          mNotificationManager.cancel(SONR_ID);
          mWakeLock.release();
       } catch (RuntimeException e) {
          e.printStackTrace();
          ErrorReporter.getInstance().handleException(e);
       }
    }
 
    @Override
    protected void onPause() {
       super.onPause();
       // LogFile.MakeLog("SONR paused");
       // try {
       // unregisterReceiver(StopReceiver);
       // isRegistered = false;
       // } catch(Exception e) {}
    }
 
    @Override
    protected void onResume() {
       super.onResume();
 
       AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
       audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, AudioManager.FLAG_VIBRATE);
 
       if (audio == null) {
          audio = AudioProcessor.findAudioRecord();
          if (client == null) {
             client = new SONRClient(this, audio, audioManager);
             client.onCreate();
          }
       }
    }
 
    @Override
    protected void onRestart() {
       super.onRestart();
       // LogFile.MakeLog("SONR restarted");
       if (!isRegistered) {
          registerReceiver(stopReceiver, new IntentFilter(DISCONNECT_ACTION));
          isRegistered = true;
       }
    }
 
    @Override
    protected void onListItemClick(ListView listView, View clickedView, int position, long id) {
       super.onListItemClick(listView, clickedView, position, id);
    
       ApplicationInfo ai;
       ai = infos.get(position);
       List<ResolveInfo> rinfos = findActivitiesForPackage(this, ai.packageName);
       ResolveInfo ri = rinfos.get(0);
    
       Common.save(this, APP_PACKAGE_NAME, ri.activityInfo.packageName);
       Common.save(this, APP_FULL_NAME, ri.activityInfo.name);
       Common.save(this, PLAYER_SELECTED, true);
    
       currentlySelectedApplicationInfoIndex = position;
       listView.invalidateViews();
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
       if (resultCode == 0) {
          finish();
       }
    }
 
    @Override
    public void onBackPressed() {
       // finish(); Do Nothing?
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       boolean showMenu = super.onCreateOptionsMenu(menu);
       try {
          getMenuInflater().inflate(R.menu.main, menu);
       } catch (InflateException e) {
          Log.d(TAG, "Unable to inflate menu: " + e);
          showMenu = false;
       }
       return showMenu;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       boolean consumeResult = super.onOptionsItemSelected(item);
       switch (item.getItemId()) {
          case R.id.quitOption:
             stopService(new Intent(this, ToggleSONR.class));
 
             statusBarNotification(this, false);
 
            if (client == null) {
                client.destroy();
             }
 
             finish();
             consumeResult = true;
             break;
          default:
             consumeResult = false;
             break;
       }
       return consumeResult;
    }
 
    /**
     * Public because it's invoked reflectively.
     */
    public void buttonOK(View view) {
       try {
          if (!Common.get(this, PLAYER_SELECTED, false) && !Common.get(this, DEFAULT_PLAYER_SELECTED, false)) {
             DialogCommon.quickPopoutDialog(this, false, SELECT_PLAYER, OK_TXT);
          } else {
             new CheckDockOnPlayerSelection(view).start();
          }
       } catch (RuntimeException e) {
          e.printStackTrace();
          ErrorReporter.getInstance().handleException(e);
       }
    }
 
    private boolean isFirstLaunch() {
       // Restore preferences
       return Common.get(this, FIRST_LAUNCH, true);
    }
 
    private void statusBarNotification(Context ctx) {
       statusBarNotification(ctx, true);
    }
 
    private void statusBarNotification(Context ctx, boolean show) {
       NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    
       if (show) {
          int icon = R.drawable.sonr_icon;
          long when = System.currentTimeMillis();
    
          Notification notification = new Notification(icon, ctx.getString(R.string.tickerText), when);
          notification.flags |= Notification.FLAG_NO_CLEAR;
    
          Intent notificationIntent = new Intent(ctx, SONR.class);
          notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    
          PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
    
          notification.setLatestEventInfo(ctx, TAG, ctx.getString(R.string.notificationText), contentIntent);
          notificationManager.notify(SONR_ID, notification);
       } else {
          notificationManager.cancel(SONR_ID);
       }
    }
 
    private void doStart(Context context, boolean defaultplayer) {
       Common.save(context, DEFAULT_PLAYER_SELECTED, defaultplayer);
 
       on = true;
       statusBarNotification(context);
 
       if (Common.get(context, PLAYER_SELECTED, false)) {
          flurryParams.put("MediaPlayer", APP_FULL_NAME);
          FlurryAgent.logEvent("APP_FULL_NAME", flurryParams);
          Intent mediaApp = new Intent();
          mediaApp.setClassName(Common.get(context, APP_PACKAGE_NAME, Common.N_A),
                                Common.get(context, APP_FULL_NAME, Common.N_A));
          mediaApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(mediaApp);
       }
    }
 
    private List<ApplicationInfo> convert(Context c, Collection<ResolveInfo> activities) {
       final List<ApplicationInfo> result = new ArrayList<ApplicationInfo>();
 
       final Set<ApplicationInfo> apps = new HashSet<ApplicationInfo>();
       for (ResolveInfo resolveInfo : activities) {
 
          if (!Common.get(c, DEFAULT_PLAYER_SELECTED, false)) {
 
             String[] appNames = c.getResources().getStringArray(R.array.mediaAppsNames);
 
             for (String appName : appNames) {
                String pkgName = resolveInfo.activityInfo.packageName.toLowerCase();
                String actvName = resolveInfo.activityInfo.name.toLowerCase();
 
                if (pkgName.contains(appName) || actvName.contains(appName)) {
                   apps.add(resolveInfo.activityInfo.applicationInfo);
                }
             }
          } else {
             String packageName = Common.get(c, APP_PACKAGE_NAME, Common.N_A);
             String appName = Common.get(c, APP_FULL_NAME, Common.N_A);
 
             if (packageName.equals(resolveInfo.activityInfo.packageName) && appName.equals(resolveInfo.activityInfo.name)) {
                result.add(resolveInfo.activityInfo.applicationInfo);
             }
             return result;
          }
       }
 
       result.addAll(apps);
 
       return result;
    }
 
    private List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
       final PackageManager packageManager = context.getPackageManager();
    
       final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
       mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    
       final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
       final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();
    
       if (apps != null) {
          // Find all activities that match the packageName
          for (ResolveInfo info : apps) {
             ActivityInfo activityInfo = info.activityInfo;
             if (packageName.equals(activityInfo.packageName)) {
                matches.add(info);
             }
          }
       }
    
       return matches;
    }
 
    private Collection<ResolveInfo> findActivities(Context context) {
       Map<String, ResolveInfo> finalMap = new HashMap<String, ResolveInfo>();
 
       final PackageManager packageManager = context.getPackageManager();
 
       final Intent musicPlayerIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER, null);
       final Intent musicPlayFromSearchIntent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH, null);
       final Intent mediaSearchIntent = new Intent(MediaStore.INTENT_ACTION_MEDIA_SEARCH, null);
       final Intent viewIntent = new Intent(Intent.ACTION_VIEW, null);
       viewIntent.setDataAndType(Uri.parse(SAMPLE_URI), AUDIO_MIME_TYPE);
 
       final Intent allLauncherAppsIntent = new Intent(Intent.ACTION_MAIN, null);
       allLauncherAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 
       List<ResolveInfo> queryResults = new ArrayList<ResolveInfo>();
       queryResults.addAll(packageManager.queryIntentActivities(musicPlayerIntent, 0));
       queryResults.addAll(packageManager.queryIntentActivities(musicPlayFromSearchIntent, 0));
       queryResults.addAll(packageManager.queryIntentActivities(mediaSearchIntent, 0));
       queryResults.addAll(packageManager.queryIntentActivities(viewIntent, 0));
       queryResults.addAll(packageManager.queryIntentActivities(allLauncherAppsIntent, 0));
 
       for (ResolveInfo resolveInfo : queryResults) {
          if (!finalMap.containsKey(resolveInfo.activityInfo.packageName)) {
             finalMap.put(resolveInfo.activityInfo.packageName, resolveInfo);
          }
       }
 
       return finalMap.values();
    }
 
    private final class StopReceiver
          extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
          // Handle reciever
          String mAction = intent.getAction();
 
          if (DISCONNECT_ACTION.equals(mAction)) {
             finish();
          }
       }
    }
 
    /**
     * Run the signal search in its own thread to avoid blocking the main thread.
     */
    private abstract class DockChecker
          extends Thread {
 
       private final View view;
 
       DockChecker(View view) {
          setDaemon(true);
          this.view = view;
       }
 
       @Override
       public void run() {
          if (client != null && !client.foundDock()) {
             client.searchSignal();
          }
          /*
           * Only the search runs in this thread. The response runs in the main
           * thread.
           */
          Runnable task;
          if (client != null && client.foundDock()) {
             client.startListener();
             task = new Runnable() {
                @Override
                public void run() {
                   dockFound();
                }
             };
             view.post(task);
          } else {
             task = new Runnable() {
                @Override
                public void run() {
                   dockNotFound();
                }
             };
          }
          view.post(task);
       }
 
       void dockFound() {
          Toast.makeText(getApplicationContext(), DOCK_FOUND, Toast.LENGTH_SHORT).show();
       }
 
       void dockNotFound() {
          Toast.makeText(getApplicationContext(), DOCK_NOT_FOUND, Toast.LENGTH_SHORT).show();
          DialogCommon.quickPopoutDialog(SONR.this, false, DOCK_NOT_FOUND_TRY_AGAIN, OK_TXT);
       }
    }
 
    private class CheckDockOnPlayerSelection
          extends DockChecker {
 
       CheckDockOnPlayerSelection(View view) {
          super(view);
       }
 
       @Override
       void dockFound() {
          super.dockFound();
          final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_default_player);
          doStart(SONR.this, checkBox.isChecked());
       }
    }
 
    private class CheckDockOnNoneSelection
          extends DockChecker {
 
       CheckDockOnNoneSelection(View view) {
          super(view);
       }
 
       @Override
       void dockFound() {
          super.dockFound();
          on = true;
          statusBarNotification(getApplicationContext());
 
          Intent startMain = new Intent(Intent.ACTION_MAIN);
          startMain.addCategory(Intent.CATEGORY_HOME);
          startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(startMain);
       }
 
       @Override
       void dockNotFound() {
          // do nothing
       }
 
    }
 
    private class AppInfoAdapter
          extends BaseAdapter {
 
       public final List<ApplicationInfo> appInformation = new ArrayList<ApplicationInfo>();
       private final LayoutInflater mInflater;
       private final PackageManager pm;
 
       public AppInfoAdapter(Context c) {
          mInflater = (LayoutInflater) c.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          pm = c.getPackageManager();
       }
 
       public AppInfoAdapter(Context c, Collection<? extends ApplicationInfo> applicationInfos) {
          this(c);
          appInformation.addAll(applicationInfos);
       }
 
       @Override
       public int getCount() {
          return appInformation.size();
       }
 
       @Override
       public Object getItem(int position) {
          return appInformation.get(position);
       }
 
       @Override
       public long getItemId(int position) {
          return appInformation.get(position).hashCode();
       }
 
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
          ApplicationInfo info = appInformation.get(position);
 
          if (convertView == null) {
             convertView = mInflater.inflate(R.layout.manage_applications_item, null);
          }
 
          TextView name = (TextView) convertView.findViewById(R.id.app_name);
          name.setText(info.loadLabel(pm));
 
          ImageView icon = (ImageView) convertView.findViewById(R.id.app_icon);
          icon.setImageDrawable(info.loadIcon(pm));
          TextView description = (TextView) convertView.findViewById(R.id.app_size);
          description.setText(info.loadDescription(pm));
 
          convertView.setBackgroundColor(currentlySelectedApplicationInfoIndex == position ? 0xFF666666 : 0xFF444444);
 
          return convertView;
       }
 
    }
 
 
    /*
     * XXX Highly dubious access required by ToggleSONR.
     * 
     * Indicates the need for a refactoring.
     */
    static void setOn(boolean status) {
       on = status;
    }
 
    static boolean isOn() {
       return on;
    }
 
    static boolean neverStarted() {
       return !mainScreen && !on;
    }
 
    /*
     * Sometimes the context is a SONR instance, sometimes it's a ToggleSONR
     * instance. Can that really be right?
     */
    static void startSonr(Context context, boolean defaultplayer) {
       instance.doStart(context, defaultplayer);
    }
 
 }
