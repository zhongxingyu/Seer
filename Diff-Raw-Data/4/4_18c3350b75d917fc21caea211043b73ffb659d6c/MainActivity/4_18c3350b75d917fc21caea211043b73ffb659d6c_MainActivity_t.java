 package jp.gr.java_conf.neko_daisuki.anaudioplayer;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.media.MediaMetadataRetriever;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Interpolator;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import jp.gr.java_conf.neko_daisuki.android.widget.RotatingUzumakiSlider;
 import jp.gr.java_conf.neko_daisuki.android.widget.UzumakiHead;
 import jp.gr.java_conf.neko_daisuki.android.widget.UzumakiSlider;
 
 public class MainActivity extends Activity
 {
     private static class ActivityHolder {
 
         protected MainActivity activity;
 
         public ActivityHolder(MainActivity activity) {
             this.activity = activity;
         }
     }
 
     private abstract class Adapter extends ArrayAdapter<String> {
 
         protected LayoutInflater inflater;
         protected MainActivity activity;
 
         public Adapter(MainActivity activity, List<String> objects) {
             super(activity, 0, objects);
             this.initialize(activity);
         }
 
         public Adapter(MainActivity activity, String[] objects) {
             super(activity, 0, objects);
             this.initialize(activity);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             return convertView == null ? this.getView(position, this.makeConvertView(parent), parent) : this.makeView(position, convertView);
         }
 
         protected abstract View makeConvertView(ViewGroup parent);
         protected abstract View makeView(int position, View convertView);
 
         private void initialize(MainActivity activity) {
             this.activity = activity;
             String service = Context.LAYOUT_INFLATER_SERVICE;
             this.inflater = (LayoutInflater)activity.getSystemService(service);
         }
     }
 
     private class FileAdapter extends Adapter {
 
         private class Row {
             ImageView playingIcon;
             TextView name;
         }
 
         public FileAdapter(MainActivity activity, String[] objects) {
             super(activity, objects);
         }
 
         protected View makeView(int position, View convertView) {
             Row row = (Row)convertView.getTag();
             String name = this.getItem(position);
             String selectedFile = this.activity.getSelectedFile();
             int src = name != selectedFile ? R.drawable.ic_blank : R.drawable.ic_playing;
             row.playingIcon.setImageResource(src);
             row.name.setText(name);
             return convertView;
         }
 
         protected View makeConvertView(ViewGroup parent) {
             View view = this.inflater.inflate(R.layout.file_row, parent, false);
             Row row = new Row();
             row.playingIcon = (ImageView)view.findViewById(R.id.playing_icon);
             row.name = (TextView)view.findViewById(R.id.name);
             view.setTag(row);
             return view;
         }
     }
 
     private class DirectoryAdapter extends Adapter {
 
         private class Row {
             ImageView playingIcon;
             TextView path;
         }
 
         public DirectoryAdapter(MainActivity activity, List<String> objects) {
             super(activity, objects);
         }
 
         protected View makeView(int position, View convertView) {
             Row row = (Row)convertView.getTag();
             String path = this.getItem(position);
             int src = path != this.activity.selectedDir ? R.drawable.ic_blank : R.drawable.ic_playing;
             row.playingIcon.setImageResource(src);
             row.path.setText(path);
             return convertView;
         }
 
         protected View makeConvertView(ViewGroup parent) {
             View view = this.inflater.inflate(R.layout.dir_row, parent, false);
             Row row = new Row();
             row.playingIcon = (ImageView)view.findViewById(R.id.playing_icon);
             row.path = (TextView)view.findViewById(R.id.path);
             view.setTag(row);
             return view;
         }
     }
 
     private class PlayProcedureOnConnected extends ActivityHolder implements Runnable {
 
         public PlayProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.sendPlay();
             this.activity.startTimer();
         }
     }
 
     private class ResumeProcedureOnConnected extends ActivityHolder implements Runnable {
 
         public ResumeProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.startTimer();
         }
     }
 
     private interface ServiceUnbinder {
 
         public void unbind();
     }
 
     private class TrueServiceUnbinder extends ActivityHolder implements ServiceUnbinder {
 
         public TrueServiceUnbinder(MainActivity activity) {
             super(activity);
         }
 
         public void unbind() {
             this.activity.unbindService(this.activity.connection);
         }
     }
 
     private class FakeServiceUnbinder implements ServiceUnbinder {
 
         public void unbind() {
         }
     }
 
     private interface ServiceStarter {
 
         public void start();
     }
 
     private class TrueServiceStarter extends ActivityHolder implements ServiceStarter {
 
         public TrueServiceStarter(MainActivity activity) {
             super(activity);
         }
 
         public void start() {
             Intent intent = new Intent(this.activity, AudioService.class);
             this.activity.startService(intent);
         }
     }
 
     private class FakeServiceStarter implements ServiceStarter {
 
         public void start() {
         }
     }
 
     private interface ServiceStopper {
 
         public void stop();
     }
 
     private class TrueServiceStopper extends ActivityHolder implements ServiceStopper {
 
         public TrueServiceStopper(MainActivity activity) {
             super(activity);
         }
 
         public void stop() {
             this.activity.unbindAudioService();
             Intent intent = new Intent(this.activity, AudioService.class);
             this.activity.stopService(intent);
         }
     }
 
     private class FakeServiceStopper implements ServiceStopper {
 
         public void stop() {
         }
     }
 
     private class Connection extends ActivityHolder implements ServiceConnection {
 
         private Runnable procedureOnConnected;
 
         public Connection(MainActivity activity, Runnable procedureOnConnected) {
             super(activity);
             this.procedureOnConnected = procedureOnConnected;
         }
 
         public void onServiceConnected(ComponentName className, IBinder service) {
             this.activity.outgoingMessenger = new Messenger(service);
             this.procedureOnConnected.run();
             Log.i(LOG_TAG, "MainActivity conneted to AudioService.");
         }
 
         public void onServiceDisconnected(ComponentName className) {
             Log.i(LOG_TAG, "MainActivity disconnected from AudioService.");
         }
     }
 
     private class Mp3Comparator implements Comparator<String> {
 
         private String dir;
 
         public Mp3Comparator(String dir) {
             this.dir = dir;
         }
 
         public int compare(String name1, String name2) {
             String path1 = this.dir + File.separator + name1;
             String path2 = this.dir + File.separator + name2;
             int trackNo1 = this.extractTrackNumber(path1);
             int trackNo2 = this.extractTrackNumber(path2);
             return trackNo1 != trackNo2 ?  trackNo1 - trackNo2 : path1.compareTo(path2);
         }
 
         private int extractTrackNumber(String path) {
             MediaMetadataRetriever meta = new MediaMetadataRetriever();
             meta.setDataSource(path);
             int key = MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER;
             String datum = meta.extractMetadata(key);
             return datum != null ? this.getTrackNumber(datum) : 0;
         }
 
         private int getTrackNumber(String datum) {
             // Track number is stored in format of "Num/Total".
             int pos = datum.indexOf('/');
             String s = pos < 0 ? datum : datum.substring(0, pos);
             try {
                 return Integer.parseInt(s);
             }
             catch (NumberFormatException e) {
                 e.printStackTrace();
             }
             return 0;
         }
     }
 
     private class OnStartRotatingListener extends ActivityHolder implements RotatingUzumakiSlider.OnStartRotatingListener {
 
         public OnStartRotatingListener(MainActivity activity) {
             super(activity);
         }
 
         public void onStartRotating(RotatingUzumakiSlider slider) {
             this.activity.onStartSliding();
         }
     }
 
     private class OnStopRotatingListener extends ActivityHolder implements RotatingUzumakiSlider.OnStopRotatingListener {
 
         public OnStopRotatingListener(MainActivity activity) {
             super(activity);
         }
 
         public void onStopRotating(RotatingUzumakiSlider slider) {
             this.activity.procAfterSeeking.run();
         }
     }
 
     private class PlayAfterSeeking extends ActivityHolder implements Runnable {
 
         public PlayAfterSeeking(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.startTimer();
             this.activity.sendPlay();
         }
     }
 
     private class StayAfterSeeking implements Runnable {
 
         public void run() {
         }
     }
 
     private class SliderLogger implements UzumakiSlider.Logger {
 
         private MainActivity activity;
 
         public SliderLogger(MainActivity activity) {
             this.activity = activity;
         }
 
         public void log(String msg) {
             this.activity.log(msg);
         }
     }
 
     private class OnStartHeadMovingListener implements UzumakiSlider.OnStartHeadMovingListener {
 
         private MainActivity activity;
 
         public OnStartHeadMovingListener(MainActivity activity) {
             this.activity = activity;
         }
 
         public void onStartHeadMoving(UzumakiSlider slider, UzumakiHead head) {
             this.activity.onStartSliding();
         }
     }
 
     private class OnStopHeadMovingListener extends ActivityHolder implements UzumakiSlider.OnStopHeadMovingListener {
 
         public OnStopHeadMovingListener(MainActivity activity) {
             super(activity);
         }
 
         public void onStopHeadMoving(UzumakiSlider slider, UzumakiHead head) {
             this.activity.procAfterSeeking.run();
         }
     }
 
     private abstract class MenuDispatcher {
 
         protected MainActivity activity;
 
         public MenuDispatcher(MainActivity activity) {
             this.activity = activity;
         }
 
         public boolean dispatch() {
             this.callback();
             return true;
         }
 
         protected abstract void callback();
     }
 
     private class AboutDispatcher extends MenuDispatcher {
 
         public AboutDispatcher(MainActivity activity) {
             super(activity);
         }
 
         protected void callback() {
             this.activity.showAbout();
         }
     }
 
     private static class IncomingHandler extends Handler {
 
         private abstract class MessageHandler extends ActivityHolder {
 
             public MessageHandler(MainActivity activity) {
                 super(activity);
             }
 
             public abstract void handle(Message msg);
         }
 
         private class WhatTimeHandler extends MessageHandler {
 
             public WhatTimeHandler(MainActivity activity) {
                 super(activity);
             }
 
             public void handle(Message msg) {
                 this.activity.updateCurrentTime(msg.arg1);
             }
         }
 
         private SparseArray<MessageHandler> handlers;
 
         public IncomingHandler(MainActivity activity) {
             this.handlers = new SparseArray<MessageHandler>();
             this.handlers.put(AudioService.MSG_WHAT_TIME, new WhatTimeHandler(activity));
         }
 
         @Override
         public void handleMessage(Message msg) {
             this.handlers.get(msg.what).handle(msg);
         }
     }
 
     public static final String LOG_TAG = "anaudioplayer";
     private static final int NO_FILES_SELECTED = -1;
 
     private ViewFlipper flipper;
     /*
      * pageIndex is needed for save/restore instance state. I tried some ways
      * to get current page index on runtime, but I did not find the way useful
      * in any cases. Counting manually is easiest.
      */
     private int pageIndex;
 
     private ListView dirList;
     private ImageButton nextButton0;
 
     private View prevButton1;
     private ListView fileList;
     private ImageButton nextButton1;
 
     private View prevButton2;
     private ImageButton playButton;
     private RotatingUzumakiSlider slider;
     private TextView title;
     private TextView currentTime;
     private TextView totalTime;
 
     private File mediaDir;
     private List<String> dirs = null;
     private String selectedDir = null;
     private String[] files = new String[0];
     private int filePosition = NO_FILES_SELECTED;
 
     private Animation leftInAnimation;
     private Animation leftOutAnimation;
     private Animation rightInAnimation;
     private Animation rightOutAnimation;
 
     private View.OnClickListener pauseListener;
     private View.OnClickListener playListener;
     private TimerInterface timer;
     private FakeTimer fakeTimer;
     private Runnable procAfterSeeking;
 
     private SparseArray<MenuDispatcher> menuDispatchers = new SparseArray<MenuDispatcher>();
 
     private ServiceStarter serviceStarter;
     private ServiceStopper serviceStopper;
     private ServiceUnbinder serviceUnbinder;
     private ServiceConnection connection;
     private Messenger outgoingMessenger;
     private Messenger incomingMessenger;
 
     @Override
     public Object onRetainNonConfigurationInstance() {
         return this.connection;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.main);
 
        this.mediaDir = Environment.getExternalStorageDirectory();

         this.findViews();
         this.initializeFlipButtonListener();
         this.initializeDirList();
         this.initializeFileList();
         this.initializeAnimation();
         this.initializePlayButton();
         this.initializeTimer();
         this.initializeSlider();
         this.initializeMenu();
 
         this.pageIndex = 0;
         this.incomingMessenger = new Messenger(new IncomingHandler(this));
 
         Log.i(LOG_TAG, "Created.");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.action_bar, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         MenuDispatcher dispatcher = this.menuDispatchers.get(item.getItemId());
         return dispatcher != null ? dispatcher.dispatch() : super.onOptionsItemSelected(item);
     }
 
     private void showAbout() {
         Intent i = new Intent(this, AboutActivity.class);
         this.startActivity(i);
     }
 
     private void initializeMenu() {
         this.menuDispatchers.put(R.id.about, new AboutDispatcher(this));
     }
 
     private void initializeSlider() {
         this.slider.addOnStartHeadMovingListener(new OnStartHeadMovingListener(this));
         this.slider.addOnStopHeadMovingListener(new OnStopHeadMovingListener(this));
         this.slider.addOnStartRotatingListener(new OnStartRotatingListener(this));
         this.slider.addOnStopRotatingListener(new OnStopRotatingListener(this));
         this.slider.setLogger(new SliderLogger(this));
     }
 
     private void initializeTimer() {
         this.timer = this.fakeTimer = new FakeTimer();
     }
 
     private void findViews() {
         this.flipper = (ViewFlipper)this.findViewById(R.id.flipper);
 
         this.dirList = (ListView)this.findViewById(R.id.dir_list);
         this.nextButton0 = (ImageButton)this.findViewById(R.id.next0);
 
         this.prevButton1 = (View)this.findViewById(R.id.prev1);
         this.fileList = (ListView)this.findViewById(R.id.file_list);
         this.nextButton1 = (ImageButton)this.findViewById(R.id.next1);
 
         this.prevButton2 = (View)this.findViewById(R.id.prev2);
         this.playButton = (ImageButton)this.findViewById(R.id.play);
         this.slider = (RotatingUzumakiSlider)this.findViewById(R.id.slider);
 
         this.title = (TextView)this.findViewById(R.id.title);
         this.currentTime = (TextView)this.findViewById(R.id.current_time);
         this.totalTime = (TextView)this.findViewById(R.id.total_time);
     }
 
     private void initializePlayButton() {
         this.pauseListener = new PauseButtonListener(this);
         this.playButton.setOnClickListener(this.pauseListener);
         this.playListener = new PlayButtonListener(this);
     }
 
     private class PauseButtonListener extends ActivityHolder implements View.OnClickListener {
 
         public PauseButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.procAfterSeeking = new StayAfterSeeking();
             this.activity.pause();
         }
     }
 
     private class PlayButtonListener extends ActivityHolder implements View.OnClickListener {
 
         public PlayButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.procAfterSeeking = new PlayAfterSeeking(this.activity);
             this.activity.play();
         }
     }
 
     private static final long ANIMATION_DURATION = 250;
     //private static final int INTERPOLATOR = android.R.anim.decelerate_interpolator;
     private static final int INTERPOLATOR = android.R.anim.linear_interpolator;
 
     private Animation loadAnimation(int id, Interpolator interp) {
         Animation anim = AnimationUtils.loadAnimation(this, id);
         anim.setDuration(ANIMATION_DURATION);
         anim.setInterpolator(interp);
         return anim;
     }
 
     private void initializeAnimation() {
         Interpolator interp = AnimationUtils.loadInterpolator(this, INTERPOLATOR);
         this.leftInAnimation = this.loadAnimation(R.anim.anim_left_in, interp);
         this.leftOutAnimation = this.loadAnimation(R.anim.anim_left_out, interp);
         this.rightInAnimation = this.loadAnimation(R.anim.anim_right_in, interp);
         this.rightOutAnimation = this.loadAnimation(R.anim.anim_right_out, interp);
     }
 
     private void initializeDirList() {
         List<String> dirs = this.listMp3Dir(this.mediaDir);
         Collections.sort(dirs);
         this.showDirectories(dirs);
 
         this.dirList.setOnItemClickListener(new DirectoryListListener(this));
     }
 
     private void showDirectories(List<String> dirs) {
         this.dirs = dirs;
         this.dirList.setAdapter(new DirectoryAdapter(this, dirs));
     }
 
     private File[] listFiles(File dir, FilenameFilter filter) {
         File[] files;
         try {
             files = dir.listFiles(filter);
         }
         catch (SecurityException _) {
             files = null;
         }
         return files != null ? files : (new File[0]);
     }
 
     private List<String> listMp3Dir(File dir) {
         List<String> list = new ArrayList<String>();
 
         for (File d: this.listFiles(dir, new DirectoryFilter())) {
             list.addAll(this.listMp3Dir(d));
         }
         if (0 < this.listFiles(dir, new Mp3Filter()).length) {
             try {
                 list.add(dir.getCanonicalPath());
             }
             catch (IOException _) {
             }
         }
 
         return list;
     }
 
     private class Mp3Filter implements FilenameFilter {
 
         public boolean accept(File dir, String name) {
             return name.endsWith(".mp3");
         }
     }
 
     private class DirectoryFilter implements FilenameFilter {
 
         public boolean accept(File dir, String name) {
             String path;
             try {
                 path = dir.getCanonicalPath() + File.separator + name;
             }
             catch (IOException _) {
                 return false;
             }
             return (new File(path)).isDirectory();
         }
     }
 
     private void initializeFlipButtonListener() {
         ImageButton[] nextButtons = { this.nextButton0, this.nextButton1 };
         this.setClickListener(nextButtons, new NextButtonListener(this));
         for (ImageButton button: nextButtons) {
             this.enableButton(button, false);
         }
 
         View[] previousButtons = { this.prevButton1, this.prevButton2 };
         this.setClickListener(previousButtons, new PreviousButtonListener(this));
     }
 
     private void selectDir(int position) {
         this.selectedDir = this.dirs.get(position);
         File dir = new File(this.selectedDir);
         String dirPath;
         try {
             dirPath = dir.getCanonicalPath();
         }
         catch (IOException _) {
             dirPath = "";
         }
         String[] files = dir.list(new Mp3Filter());
         Arrays.sort(files, new Mp3Comparator(dirPath));
         this.showFiles(files);
         this.filePosition = NO_FILES_SELECTED;
 
         this.dirList.invalidateViews();
         this.enableButton(this.nextButton0, true);
         this.showNext();
     }
 
     private void showFiles(String[] files) {
         this.files = files;
         this.fileList.setAdapter(new FileAdapter(this, files));
     }
 
     private void stopTimer() {
         this.timer.cancel();
         this.timer = this.fakeTimer;
     }
 
     private void pause() {
         this.stopTimer();
         this.stopAudioService();
 
         this.playButton.setOnClickListener(this.playListener);
         this.playButton.setImageResource(R.drawable.ic_play);
     }
 
     private class PlayerTask extends TimerTask {
 
         public PlayerTask(MainActivity activity) {
             this.handler = new Handler();
             this.proc = new Proc(activity);
         }
 
         private class Proc extends ActivityHolder implements Runnable {
 
             public Proc(MainActivity activity) {
                 super(activity);
             }
 
             public void run() {
                 this.activity.sendMessage(AudioService.MSG_WHAT_TIME);
             }
         }
 
         @Override
         public void run() {
             this.handler.post(this.proc);
         }
 
         private Handler handler;
         private Runnable proc;
     }
 
     private void updateCurrentTime(int position) {
         this.slider.setProgress(position);
         this.showTime(this.currentTime, position);
     }
 
     private void startTimer() {
         this.timer = new TrueTimer();
         // Each Timer requests new TimerTask object (Timers cannot share one task).
         this.timer.scheduleAtFixedRate(new PlayerTask(this), 0, 10);
     }
 
     private void play() {
         this.stopTimer();
         this.startAudioService();
         this.bindAudioService(new PlayProcedureOnConnected(this));
 
         this.playButton.setOnClickListener(this.pauseListener);
         this.playButton.setImageResource(R.drawable.ic_pause);
     }
 
     private void sendPlay() {
         String path = this.getSelectedPath();
         int offset = this.slider.getProgress();
         Object a = AudioService.makePlayArgument(path, offset);
         this.sendMessage(AudioService.MSG_PLAY, a);
     }
 
     private String getSelectedFile() {
         int pos = this.filePosition;
         // Returning "" must be harmless.
         return pos == NO_FILES_SELECTED ? "" : this.files[pos];
     }
 
     private String getSelectedPath() {
         return this.selectedDir + File.separator + this.getSelectedFile();
     }
 
     private int getDuration(String path) {
         // MediaMetadataRetriever is not reusable.
         MediaMetadataRetriever meta = new MediaMetadataRetriever();
         meta.setDataSource(path);
         int key = MediaMetadataRetriever.METADATA_KEY_DURATION;
         String datum;
         try {
             datum = meta.extractMetadata(key);
         }
         finally {
             meta.release();
         }
         return Integer.parseInt(datum);
     }
 
     private void showTime(TextView view, int time_msec) {
         int time_sec = time_msec / 1000;
         int min = (time_sec / 60) % 100;
         int sec = time_sec % 60;
         view.setText(String.format("%02d:%02d", min, sec));
     }
 
     private void enableButton(ImageButton button, boolean enabled) {
         /*
          * Why did not I use style?
          * ========================
          *
          * I tryed to give the following settings only in *.xml, but it did not
          * work. I do not know why still. So I give the settings at here.
          */
         button.setClickable(enabled);
         int resourceId = enabled ? R.drawable.nav_right : R.drawable.ic_blank;
         button.setImageResource(resourceId);
     }
 
     private void selectFile(int position) {
         this.pause();
 
         this.filePosition = position;
         this.procAfterSeeking = new PlayAfterSeeking(this);
 
         this.fileList.invalidateViews();
         this.enableButton(this.nextButton1, true);
         String path = this.getSelectedPath();
         int duration = this.getDuration(path);
         this.slider.setMax(duration);
         this.slider.setProgress(0);
         this.showPlayingFile();
         this.showNext();
 
         this.play();
     }
 
     private void showPlayingFile() {
         this.title.setText(this.getSelectedFile());
         this.showTime(this.currentTime, this.slider.getProgress());
         this.showTime(this.totalTime, this.slider.getMax());
     }
 
     private void showPrevious() {
         this.flipper.setInAnimation(this.leftInAnimation);
         this.flipper.setOutAnimation(this.rightOutAnimation);
         this.flipper.showPrevious();
         this.pageIndex -= 1;
     }
 
     private void showNext() {
         this.flipper.setInAnimation(this.rightInAnimation);
         this.flipper.setOutAnimation(this.leftOutAnimation);
         this.flipper.showNext();
         this.pageIndex += 1;
     }
 
     private abstract class ListListener implements AdapterView.OnItemClickListener {
 
         public ListListener(MainActivity activity) {
             this.activity = activity;
         }
 
         protected MainActivity activity;
     }
 
     private class DirectoryListListener extends ListListener {
 
         public DirectoryListListener(MainActivity activity) {
             super(activity);
         }
 
         public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
             this.activity.selectDir(position);
         }
     }
 
     private class FileListListener extends ListListener {
 
         public FileListListener(MainActivity activity) {
             super(activity);
         }
 
         public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
             this.activity.selectFile(position);
         }
     }
 
     private void setClickListener(View[] buttons, View.OnClickListener listener) {
         for (View button: buttons) {
             button.setOnClickListener(listener);
         }
     }
 
     private abstract class FlipButtonListener implements View.OnClickListener {
 
         public FlipButtonListener(MainActivity activity) {
             this.activity = activity;
         }
 
         protected MainActivity activity;
     }
 
     private class NextButtonListener extends FlipButtonListener {
 
         public NextButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.showNext();
         }
     }
 
     private class PreviousButtonListener extends FlipButtonListener {
 
         public PreviousButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.showPrevious();
         }
     }
 
     private interface TimerInterface {
 
         public void scheduleAtFixedRate(TimerTask task, long deley, long period);
         public void cancel();
     }
 
     private class TrueTimer implements TimerInterface {
 
         public TrueTimer() {
             this.timer = new Timer(true);
         }
 
         public void scheduleAtFixedRate(TimerTask task, long deley, long period) {
             this.timer.scheduleAtFixedRate(task, deley, period);
         }
 
         public void cancel() {
             this.timer.cancel();
         }
 
         private Timer timer;
     }
 
     private class FakeTimer implements TimerInterface {
 
         public void scheduleAtFixedRate(TimerTask task, long deley, long period) {
         }
 
         public void cancel() {
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         Intent intent = this.makeAudioServiceIntent();
         Connection conn = new Connection(this, new ResumeProcedureOnConnected(this));
         if (this.bindService(intent, conn, 0)) {
             this.serviceStarter = new FakeServiceStarter();
             this.serviceStopper = new TrueServiceStopper(this);
             this.serviceUnbinder = new TrueServiceUnbinder(this);
             this.connection = conn;
         }
         else {
             this.serviceStarter = new TrueServiceStarter(this);
             this.serviceStopper = new FakeServiceStopper();
             this.serviceUnbinder = new FakeServiceUnbinder();
             this.connection = null;
         }
 
         Log.i(LOG_TAG, "Resumed.");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         this.stopTimer();
         this.unbindAudioService();
 
         Log.i(LOG_TAG, "Paused.");
     }
 
     private enum Key {
         PAGE_INDEX,
         NEXT_BUTTON0_ENABLED,
         NEXT_BUTTON1_ENABLED,
         SELECTED_DIR,
         FILES,
         FILE_POSITION,
         PROGRESS,
         DURATION;
 
         public String getKey() {
             return this.name();
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         outState.putInt(Key.PAGE_INDEX.getKey(), this.pageIndex);
         this.saveButton(outState, Key.NEXT_BUTTON0_ENABLED, this.nextButton0);
         this.saveButton(outState, Key.NEXT_BUTTON1_ENABLED, this.nextButton1);
         outState.putString(Key.SELECTED_DIR.getKey(), this.selectedDir);
         outState.putStringArray(Key.FILES.getKey(), this.files);
         outState.putInt(Key.FILE_POSITION.getKey(), this.filePosition);
         outState.putInt(Key.PROGRESS.getKey(), this.slider.getProgress());
         outState.putInt(Key.DURATION.getKey(), this.slider.getMax());
 
         Log.i(LOG_TAG, "Instance state was saved.");
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
 
         this.pageIndex = savedInstanceState.getInt(Key.PAGE_INDEX.getKey());
         for (int i = 0; i < this.pageIndex; i++) {
             this.flipper.showNext();
         }
         this.restoreButton(savedInstanceState, Key.NEXT_BUTTON0_ENABLED, this.nextButton0);
         this.restoreButton(savedInstanceState, Key.NEXT_BUTTON1_ENABLED, this.nextButton1);
         this.selectedDir = savedInstanceState.getString(Key.SELECTED_DIR.getKey());
         this.showFiles(savedInstanceState.getStringArray(Key.FILES.getKey()));
         this.filePosition = savedInstanceState.getInt(Key.FILE_POSITION.getKey());
         this.slider.setProgress(savedInstanceState.getInt(Key.PROGRESS.getKey()));
         this.slider.setMax(savedInstanceState.getInt(Key.DURATION.getKey()));
         this.showPlayingFile();
 
         Log.i(LOG_TAG, "Instance state was restored.");
     }
 
     private void restoreButton(Bundle savedInstanceState, Key key, ImageButton button) {
         boolean enabled = savedInstanceState.getBoolean(key.getKey());
         this.enableButton(button, enabled);
     }
 
     private void saveButton(Bundle outState, Key key, ImageButton button) {
         outState.putBoolean(key.getKey(), button.isClickable());
     }
 
     private void sendMessage(int what, Object o) {
         Message msg = Message.obtain(null, what, 0, 0, o);
         msg.replyTo = this.incomingMessenger;
         try {
             this.outgoingMessenger.send(msg);
         }
         catch (RemoteException e) {
             // TODO: MainActivity must show error to users.
             e.printStackTrace();
         }
     }
 
     private void sendMessage(int what) {
         this.sendMessage(what, null);
     }
 
     private void log(String msg) {
         this.title.setText(msg);
     }
 
     private void startAudioService() {
         this.serviceStarter.start();
         this.serviceStarter = new FakeServiceStarter();
         this.serviceStopper = new TrueServiceStopper(this);
     }
 
     private void stopAudioService() {
         this.serviceStopper.stop();
         this.serviceStarter = new TrueServiceStarter(this);
         this.serviceStopper = new FakeServiceStopper();
     }
 
     private void unbindAudioService() {
         this.serviceUnbinder.unbind();
         this.serviceUnbinder = new FakeServiceUnbinder();
     }
 
     private Intent makeAudioServiceIntent() {
         return new Intent(this, AudioService.class);
     }
 
     private void bindAudioService(Runnable procedureOnConnected) {
         Intent intent = this.makeAudioServiceIntent();
         this.connection = new Connection(this, procedureOnConnected);
         this.bindService(intent, this.connection, 0);
         this.serviceUnbinder = new TrueServiceUnbinder(this);
     }
 
     private void onStartSliding() {
         this.stopTimer();
         this.sendMessage(AudioService.MSG_PAUSE);
     }
 
     private void initializeFileList() {
         this.fileList.setOnItemClickListener(new FileListListener(this));
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
