 package jp.ddo.neko_daisuki.anaudioplayer;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.media.MediaMetadataRetriever;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Interpolator;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ViewFlipper;
 
 import jp.ddo.neko_daisuki.android.widget.UzumakiSlider;
 
 public class MainActivity extends Activity
 {
     private class Player {
 
         private MediaPlayer mp;
 
         public Player() {
             this.mp = new MediaPlayer();
         }
 
         public void setup(String path) throws IOException {
             this.mp.reset();
             this.mp.setDataSource(path);
             this.mp.prepare();
         }
 
        public int getCurrentPosition() {
            return this.mp.getCurrentPosition();
        }

         public void play() {
             this.mp.start();
         }
 
         public void pause() {
             this.mp.pause();
         }
 
         public void release() {
             this.mp.release();
         }
     }
 
     private static final String LOG_TAG = "An Audio Player";
 
     private ViewFlipper flipper;
 
     private ListView dirList;
     private Button nextButton0;
 
     private Button prevButton1;
     private ListView fileList;
     private Button nextButton1;
 
     private Button prevButton2;
     private Button playButton;
     private UzumakiSlider slider;
 
     private List<String> dirs = null;
     private String selectedDir = null;
     private String[] files = new String[0];
     private int filePosition;
     private Player player = new Player();
 
     private Animation leftInAnimation;
     private Animation leftOutAnimation;
     private Animation rightInAnimation;
     private Animation rightOutAnimation;
 
     private View.OnClickListener pauseListener;
     private View.OnClickListener playListener;
     private TimerInterface timer;
     private FakeTimer fakeTimer;
     private MediaMetadataRetriever meta = new MediaMetadataRetriever();
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.main);
 
         this.findViews();
         this.initializeFlipButtonListener();
         this.initializeDirList();
         this.initializeAnimation();
         this.initializePlayButton();
         this.initializeTimer();
     }
 
     private void initializeTimer() {
         this.timer = this.fakeTimer = new FakeTimer();
     }
 
     private void findViews() {
         this.flipper = (ViewFlipper)this.findViewById(R.id.flipper);
 
         this.dirList = (ListView)this.findViewById(R.id.dir_list);
         this.nextButton0 = (Button)this.findViewById(R.id.next0);
 
         this.prevButton1 = (Button)this.findViewById(R.id.prev1);
         this.fileList = (ListView)this.findViewById(R.id.file_list);
         this.nextButton1 = (Button)this.findViewById(R.id.next1);
 
         this.prevButton2 = (Button)this.findViewById(R.id.prev2);
         this.playButton = (Button)this.findViewById(R.id.play);
         this.slider = (UzumakiSlider)this.findViewById(R.id.slider);
     }
 
     private void initializePlayButton() {
         this.pauseListener = new PauseButtonListener(this);
         this.playButton.setOnClickListener(this.pauseListener);
         this.playListener = new PlayButtonListener(this);
     }
 
     private class ActivityListener {
 
         public ActivityListener(MainActivity activity) {
             this.activity = activity;
         }
 
         protected MainActivity activity;
     }
 
     private class PauseButtonListener extends ActivityListener implements View.OnClickListener {
 
         public PauseButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.pause();
         }
     }
 
     private class PlayButtonListener extends ActivityListener implements View.OnClickListener {
 
         public PlayButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
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
 
     private static final String MEDIA_PATH = "/sdcard/u1";
 
     private void initializeDirList() {
         int layout = android.R.layout.simple_list_item_1;
         this.dirs = this.listMp3Dir(new File(MEDIA_PATH));
         this.dirList.setAdapter(new ArrayAdapter<String>(this, layout, this.dirs));
         this.dirList.setOnItemClickListener(new DirectoryListListener(this));
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
         Button[] next_buttons = { this.nextButton0, this.nextButton1 };
         this.setClickListener(next_buttons, new NextButtonListener(this));
         Button[] previous_buttons = { this.prevButton1, this.prevButton2 };
         this.setClickListener(previous_buttons, new PreviousButtonListener(this));
     }
 
     private void selectDir(int position) {
         int layout = android.R.layout.simple_list_item_1;
         this.selectedDir = this.dirs.get(position);
         this.files = (new File(this.selectedDir)).list(new Mp3Filter());
 
         this.fileList.setAdapter(new ArrayAdapter<String>(this, layout, this.files));
         this.fileList.setOnItemClickListener(new FileListListener(this));
 
         this.nextButton0.setEnabled(true);
         this.showNext();
     }
 
     private void pause() {
         this.timer.cancel();
         this.timer = this.fakeTimer;
 
         this.player.pause();
 
         this.playButton.setOnClickListener(this.playListener);
         this.playButton.setText(">");
     }
 
     private class PlayerTask extends TimerTask {
 
         public PlayerTask(MainActivity activity) {
             this.handler = new Handler();
             this.proc = new Proc(activity);
         }
 
         public class Proc implements Runnable {
 
             public Proc(MainActivity activity) {
                 this.activity = activity;
             }
 
             public void run() {
                 this.activity.updateSlider();
             }
 
             private MainActivity activity;
         }
 
         @Override
         public void run() {
             this.handler.post(this.proc);
         }
 
         private Handler handler;
         private Runnable proc;
     }
 
     private void updateSlider() {
        this.slider.setProgress(this.player.getCurrentPosition());
     }
 
     private void play() {
         this.timer.cancel();
         this.timer = new TrueTimer();
         // Each Timer requests new TimerTask object (Timers cannot share one task).
         this.timer.scheduleAtFixedRate(new PlayerTask(this), 0, 10);
 
         this.player.play();
 
         this.playButton.setOnClickListener(this.pauseListener);
         this.playButton.setText("II");
     }
 
     private String getSelectedPath() {
         return this.selectedDir + File.separator + this.files[this.filePosition];
     }
 
     private int getDuration(String path) {
         int key = MediaMetadataRetriever.METADATA_KEY_DURATION;
         String datum;
         meta.setDataSource(path);
         try {
             datum = meta.extractMetadata(key);
         }
         finally {
             meta.release();
         }
         return Integer.parseInt(datum);
     }
 
     private void selectFile(int position) {
         this.filePosition = position;
         String path = this.getSelectedPath();
         try {
             this.player.setup(path);
         }
         catch (IOException e) {
             Log.e(LOG_TAG, e.toString());
             return;
         }
 
         this.slider.setMax(this.getDuration(path));
         this.slider.setProgress(0);
 
         this.nextButton1.setEnabled(true);
         this.showNext();
 
         this.play();
     }
 
     private void showPrevious() {
         this.flipper.setInAnimation(this.leftInAnimation);
         this.flipper.setOutAnimation(this.rightOutAnimation);
         this.flipper.showPrevious();
     }
 
     private void showNext() {
         this.flipper.setInAnimation(this.rightInAnimation);
         this.flipper.setOutAnimation(this.leftOutAnimation);
         this.flipper.showNext();
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
 
     private void setClickListener(Button[] buttons, View.OnClickListener listener) {
         for (Button button: buttons) {
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
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
