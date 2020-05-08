 package com.zhideel.tapathon.ui;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.*;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.sec.android.allshare.ServiceConnector;
 import com.sec.android.allshare.ServiceProvider;
 import com.sec.android.allshare.screen.ScreenCastManager;
 import com.squareup.otto.Bus;
 import com.squareup.otto.Subscribe;
 import com.zhideel.tapathon.R;
 import com.zhideel.tapathon.chord.BusEvent;
 import com.zhideel.tapathon.chord.ClientGameChord;
 import com.zhideel.tapathon.chord.GameChord;
 import com.zhideel.tapathon.chord.ServerGameChord;
 import com.zhideel.tapathon.logic.CommunicationBus;
 import com.zhideel.tapathon.logic.GameLogicController;
 import com.zhideel.tapathon.logic.Model;
 import com.zhideel.tapathon.utils.BitmapCache;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class GamePadActivity extends Activity implements CommunicationBus.BusManager {
 
     public static final String GAME_NAME = "TAPATHON";
     public static final String CLIENT = "CLIENT";
     public static final String SERVER_NAME = "SERVER_NAME";
     private Bus mBus;
     private GameChord mGameChord;
     private GameLogicController mLogicController;
     private List<CommunicationBus.BusManager> mManagers;
     private boolean mIsClient;
     private ServiceProvider mServiceProvider;
     private ScreenCastManager mManager;
     private BitmapCache mMemoryCache;
     private Vibrator mVibrator;
     private boolean mAllShareEnabled;
     private Dialog mAllShareDialog;
     private Dialog mNoAllShareCastDialog;
     private boolean continueMusic;
     private GameBoardView gameBoardView;
     private StatsView statsView;
     private ImageView gameEndView;
     private Button btnStart;
     private TextView tvWaiting;
     private boolean allShareShownBefore = false;
 
     public static Context mContext;
 
     private final BroadcastReceiver mWiFiBroadcastReceiver = new BroadcastReceiver() {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             final WifiInfo info = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
             if (info == null) {
                 finish();
                 Toast.makeText(GamePadActivity.this, getString(R.string.wifi_disconnected), Toast.LENGTH_LONG).show();

                 //TODO pause everyone?
             }
         }
 
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_game_pad);
         mContext = this;
         btnStart = (Button) findViewById(R.id.btn_start);
         gameEndView = (ImageView) findViewById(R.id.game_end_view);
         tvWaiting = (TextView) findViewById(R.id.tv_waiting);
         btnStart.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startGame();
                 btnStart.setVisibility(View.GONE);
             }
         });
 
         mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         registerWifiStateReceiver();
 
         final int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
         final int cacheSize = 1024 * 1024 * memClass / 8;
         mMemoryCache = new BitmapCache(cacheSize, getAssets());
 
         final String userName = getSharedPreferences(GameMenuActivity.TAPATHON_PREFERENCES, MODE_PRIVATE).getString(
                 GameMenuActivity.USER_NAME_KEY, "");
 
         mBus = CommunicationBus.getInstance();
         mManagers = new LinkedList<CommunicationBus.BusManager>();
         mManagers.add(this);
 
         final Model model = new Model(!mIsClient);
 
         final Intent intent = getIntent();
         mIsClient = intent.getBooleanExtra(CLIENT, true);
 
         final String roomName;
 
         mAllShareDialog = new AlertDialog.Builder(GamePadActivity.this).setMessage(R.string.all_share_dialog_message)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         mManager.activateManagerUI();
                     }
                 }).setNegativeButton(R.string.all_share_dialog_exit, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         mAllShareDialog.dismiss();
                     }
                 }).setCancelable(false).create();
 
         mNoAllShareCastDialog = new AlertDialog.Builder(GamePadActivity.this)
                 .setMessage(R.string.no_all_share_cast_dialog).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         mNoAllShareCastDialog.dismiss();
                     }
                 }).setCancelable(false).create();
 
         if (mIsClient) {
             btnStart.setVisibility(View.GONE);
             roomName = getIntent().getStringExtra(SERVER_NAME);
             mGameChord = new ClientGameChord(this, roomName, GAME_NAME, userName);
         } else {
             tvWaiting.setVisibility(View.GONE);
             roomName = getIntent().getStringExtra(SERVER_NAME);
             mGameChord = new ServerGameChord(this, roomName, GAME_NAME, userName);
 
             //for host to setup AllShare
             ServiceConnector.createServiceProvider(this, new ServiceConnector.IServiceConnectEventListener() {
 
                 @Override
                 public void onCreated(ServiceProvider sprovider, ServiceConnector.ServiceState state) {
                     mServiceProvider = sprovider;
                     mManager = sprovider.getScreenCastManager();
                     if (mManager != null) {
                         mNoAllShareCastDialog.dismiss();
                         mAllShareDialog.show();
                         mManager.setScreenCastEventListener(new ScreenCastManager.IScreenCastEventListener() {
 
                             @Override
                             public void onStopped(ScreenCastManager screencastmanager) {
                                 mAllShareEnabled = false;
                                 mAllShareDialog.show();
                             }
 
                             @Override
                             public void onStarted(ScreenCastManager screencastmanager) {
                                 mAllShareEnabled = true;
                                 mAllShareDialog.dismiss();
                                 screencastmanager.setMode(ScreenCastManager.ScreenMode.DUAL);
                             }
                         });
                     }
                 }
 
                 @Override
                 public void onDeleted(ServiceProvider sprovider) {
                 }
             });
 
             mLogicController = new GameLogicController(model, getResources());
             mManagers.add(mLogicController);
         }
 
         mManagers.add(model);
         mManagers.add(mGameChord);
 
         for (CommunicationBus.BusManager manager : mManagers) {
             manager.startBus();
         }
     }
 
 
     @Override
     public void onBackPressed() {
         new AlertDialog.Builder(this)
                 .setMessage(R.string.exit)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         GamePadActivity.this.finish();
                     }
                 })
                 .setNegativeButton(R.string.cancel, null)
                 .show();
     }
 
     @Override
     protected void onDestroy() {
 
         for (CommunicationBus.BusManager manager : mManagers) {
             manager.stopBus();
         }
 
         if (gameBoardView != null) {
             gameBoardView.stopBus();
         }
 
         mGameChord.stopChord();
         if (mManager != null) {
             mManager.stop();
         }
 
         if (mServiceProvider != null) {
             ServiceConnector.deleteServiceProvider(mServiceProvider);
         }
 
         unregisterReceiver(mWiFiBroadcastReceiver);
 
 
         mAllShareDialog.dismiss();
         mNoAllShareCastDialog.dismiss();
 
         super.onDestroy();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
     }
 
     public void startGame() {
         mBus.post(GameLogicController.StartGameEvent.INSTANCE);
     }
 
     public GameBoardView getGameBoard() {
         return gameBoardView;
     }
 
     public StatsView getStatsView() {
         return statsView;
     }
 
     private void showGameDisplay() {
         MultiTouchView.GameLevel level = null;
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             level = (MultiTouchView.GameLevel) extras.getSerializable("level");
             if (level == null) {
                 level = MultiTouchView.GameLevel.EASY;
             }
         }
 
 
         gameBoardView = new GameBoardView(this, level, (ViewGroup) findViewById(R.id.gameboard_container));
         statsView = new StatsView(this, (ViewGroup) findViewById(R.id.statsboard_container));
     }
 
 
     @Override
     protected void onPause() {
         super.onPause();
         if (!continueMusic) {
             MusicManager.pause();
         }
         if (statsView != null) statsView.setPaused(true);
         if (gameBoardView != null) gameBoardView.pauseBoard(true);
         mNoAllShareCastDialog.dismiss();
         mAllShareDialog.dismiss();
         super.onPause();
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         continueMusic = false;
         MusicManager.start(this, MusicManager.MUSIC_MENU);
         if (statsView != null) {
             statsView.setPaused(false);
         }
         if (gameBoardView != null) {
             gameBoardView.pauseBoard(false);
         }
         if (!mIsClient && !allShareShownBefore) {
             if (mManager == null) {
                 allShareShownBefore = true;
                 mNoAllShareCastDialog.show();
             } else if (!mAllShareEnabled) {
                 allShareShownBefore = true;
                 mAllShareDialog.show();
             }
         }
     }
 
     private void registerWifiStateReceiver() {
         final IntentFilter filter = new IntentFilter();
         filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         registerReceiver(mWiFiBroadcastReceiver, filter);
     }
 
     public void showGameEndView() {
         gameEndView.setVisibility(View.VISIBLE);
     }
 
     @Override
     public void startBus() {
         mBus.register(this);
     }
 
     @Override
     public void stopBus() {
         mBus.unregister(this);
     }
 
     @Subscribe
     public void handleGameEnd(GameActivityEvent.GameEndEvent gameEndEvent) {
         mLogicController.gameResult.getWinners();
         //TODO display the scoreboard
     }
 
     @Subscribe
     public void handleGameStart(GameActivityEvent.GameStartEvent gameStartEvent) {
         this.showGameDisplay();
     }
 
     public static class GameActivityEvent extends BusEvent {
 
         private static final long serialVersionUID = 20130326L;
 
         GameActivityEvent() {
             super();
         }
 
         public static class GameStartEvent extends GameActivityEvent {
 
             private static final long serialVersionUID = 20130327L;
 
             public GameStartEvent() {
                 super();
             }
 
         }
 
         public static class GameEndEvent extends GameActivityEvent {
 
             private static final long serialVersionUID = 20130326L;
 
             public GameEndEvent() {
                 super();
             }
 
         }
     }
 
 }
