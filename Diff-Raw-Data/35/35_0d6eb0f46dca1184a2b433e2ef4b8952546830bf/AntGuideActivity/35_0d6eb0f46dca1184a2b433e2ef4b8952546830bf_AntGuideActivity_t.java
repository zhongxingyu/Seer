 package com.howfun.android.antguide;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.AnimationUtils;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.howfun.android.antguide.entity.Score;
 import com.howfun.android.antguide.game.GameStatus;
 import com.howfun.android.antguide.game.TimeManager;
 import com.howfun.android.antguide.utils.DBAdapter;
 import com.howfun.android.antguide.utils.Utils;
 import com.howfun.android.antguide.view.AntView;
 import com.howfun.android.HF2D.Pos;
 
 public class AntGuideActivity extends Activity implements OnTouchListener {
 
    private static final String TAG = "AntGuide";
 
    private static final String PREF = "ant pref";
    private static final String GAME_STATE_PREF = "ant state pref";// Paused is
                                                                   // true
 
    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;
 
    private boolean mIsBackPressed;
 
    private static final int SOUND_EFFECT_COLLISION = 0;
    private static final int SOUND_EFFECT_FOOD = 1;
    private static final int SOUND_EFFECT_VICTORY = 2;
    private static final int SOUND_EFFECT_LOST = 3;
 
    private static final int MAX_SCORE = 999;
 
    private static final int[] nums = { R.drawable.num_0, R.drawable.num_1,
          R.drawable.num_2, R.drawable.num_3, R.drawable.num_4,
          R.drawable.num_5, R.drawable.num_6, R.drawable.num_7,
          R.drawable.num_8, R.drawable.num_9 };
 
    private SoundPool mSoundPool;
    private int[] mSounds;
    private int[] mSoundIds;
 
    Intent mIntentService = null;
    Intent mIntentReceiver = null;
 
    // private TextView gameScore;
    private ImageView gamePause;
    private ImageView gamePlay;
    private AntView antView;
    private FrameLayout mGameInfo;
    private TextView mGameInfoText;
 
    private ImageView mTimeMin0;
    private ImageView mTimeMin1;
    private ImageView mTimeSec0;
    private ImageView mTimeSec1;
 
    private ImageView mScore0;
    private ImageView mScore1;
    private ImageView mScore2;
 
    private int mScore = 0;
 
    private GameStatus mGameStatus;
    private TimeManager mTimeManager;
 
    private SharedPreferences mSettings;
    private boolean mBackMusicOff;
    private boolean mSoundEffectOff;
 
    private Handler mHandler = new Handler() {
 
       public void handleMessage(Message msg) {
          switch (msg.what) {
          case Utils.MSG_ANT_HOME:
             playSoundEffect(SOUND_EFFECT_VICTORY);
             stopGame(Utils.ANT_HOME);
             break;
          case Utils.MSG_ANT_LOST:
             playSoundEffect(SOUND_EFFECT_LOST);
             stopGame(Utils.ANT_LOST);
             break;
          case Utils.MSG_ANT_COLLISION:
             playSoundEffect(SOUND_EFFECT_COLLISION);
             break;
 
          case Utils.MSG_ANT_FOOD:
             playSoundEffect(SOUND_EFFECT_FOOD);
             updateScore();
             break;
          case Utils.MSG_ANT_TIMEOUT:
             // TODO: game timeout
             stopGame(Utils.ANT_TIMEOUT);
             break;
 
          case Utils.MSG_SCORE_BOARD:
             Utils.log(TAG, "show scroe board,score====" + msg.arg1);
             showScoreBoard(msg.arg1);
             break;
          case Utils.MSG_TIME_UPDATED:
             String time = (String) msg.obj;
             updateTimeBoard(time);
             break;
          default:
             break;
          }
       }
 
    };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Utils.log(TAG, "onCreate()");
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
       DisplayMetrics dm = new DisplayMetrics();
       this.getWindowManager().getDefaultDisplay().getMetrics(dm);
       // get device pixels
       DEVICE_WIDTH = dm.widthPixels;
       DEVICE_HEIGHT = dm.heightPixels;
 
       setContentView(R.layout.game_view);
 
       findViews();
       setupListeners();
       loadSoundEffects();
       initScoreBoard();
 //      init();
 
    }
 
    @Override
    protected void onResume() {
       super.onResume();
       Utils.log(TAG, "onresume..");
       init();
       mBackMusicOff = mSettings.getBoolean(Utils.PREF_SETTINGS_BACK_MUSIC_OFF,
             false);
       mSoundEffectOff = mSettings.getBoolean(
             Utils.PREF_SETTINGS_SOUND_EFFECT_OFF, false);
 
       if (!mBackMusicOff) {
          sendBroadcast(mIntentReceiver);
       }
 
       antView.init(mHandler);
       SharedPreferences sp = this.getSharedPreferences(PREF, MODE_PRIVATE);
       int gameStatus = sp.getInt(GAME_STATE_PREF, GameStatus.GAME_INIT);
 
       // int gameStatus = mGameStatus.getStatus();
 
       if (gameStatus == GameStatus.GAME_INIT) {
          playGame();
       } else if (gameStatus == GameStatus.GAME_STOPPED) {
          playGame();
       } else if (gameStatus == GameStatus.GAME_PAUSED) {
          restorePausedGame();
       } else {
          // TODO stop
       }
 
    }
 
    @Override
    protected void onPause() {
       Utils.log(TAG, "onPause..");
       // if (!mIsBackPressed) {
       // TODO pause bug
       if (this.isFinishing()) {
          Utils.log(TAG, "this isfinishing!");
          resetState();
       } else {
          Utils.log(TAG, "not this isfinishing!");
          pauseGame();
          saveState();
       }
       if (!mBackMusicOff) {
          stopService(mIntentService);
       }
       super.onPause();
 
    }
 
    @Override
    protected void onStop() {
       super.onStop();
       Utils.log(TAG, "onStop..");
       mTimeManager.kill();
    }
 
    private void findViews() {
       gamePause = (ImageView) findViewById(R.id.game_pause);
       gamePlay = (ImageView) findViewById(R.id.game_play);
       antView = (AntView) findViewById(R.id.ant_view);
       mGameInfo = (FrameLayout) findViewById(R.id.game_view_info);
 
       mGameInfoText = (TextView) findViewById(R.id.game_view_info_text);
 
       mTimeMin0 = (ImageView) findViewById(R.id.time_min0);
       mTimeMin1 = (ImageView) findViewById(R.id.time_min1);
       mTimeSec0 = (ImageView) findViewById(R.id.time_sec0);
       mTimeSec1 = (ImageView) findViewById(R.id.time_sec1);
 
       mScore0 = (ImageView) findViewById(R.id.score_0);
       mScore1 = (ImageView) findViewById(R.id.score_1);
       mScore2 = (ImageView) findViewById(R.id.score_2);
    }
 
    private void setupListeners() {
 
       if (gamePause != null) {
          gamePause.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                pauseGame();
             }
          });
       }
 
       if (gamePlay != null) {
          gamePlay.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                if (mGameStatus.getStatus() == GameStatus.GAME_PAUSED) {
                   resumeGame();
                } else if (mGameStatus.getStatus() == GameStatus.GAME_STOPPED) {
                   playGame();
                }
             }
          });
       }
 
       if (antView != null) {
          antView.setOnTouchListener(new OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                   antView.setDownPos(x, y);
 
                   break;
                case MotionEvent.ACTION_MOVE:
                   break;
                case MotionEvent.ACTION_UP:
                   antView.setUpPos(x, y);
                   antView.showBlockLine();
                   break;
                }
                return true;
             }
 
          });
       }
    }
 
    private void loadSoundEffects() {
       mSounds = new int[] { R.raw.collision, R.raw.food, R.raw.victory,
             R.raw.lost };
       mSoundPool = new SoundPool(mSounds.length, AudioManager.STREAM_MUSIC, 100);
       mSoundIds = new int[] {
             mSoundPool.load(this, mSounds[SOUND_EFFECT_COLLISION], 1),
             mSoundPool.load(this, mSounds[SOUND_EFFECT_FOOD], 1),
             mSoundPool.load(this, mSounds[SOUND_EFFECT_VICTORY], 1),
             mSoundPool.load(this, mSounds[SOUND_EFFECT_LOST], 1) };
    }
 
    private void playSoundEffect(int id) {
       if (mSoundEffectOff)
          return;
       if (mSoundPool != null) {
          mSoundPool.play(mSoundIds[id], 13, 15, 1, 0, 1f);
       }
    }
 
    private void init() {
       mIntentService = new Intent("com.howfun.android.antguide.MusicService");
       mIntentReceiver = new Intent("com.howfun.android.antguide.MusicReceiver");
 
       mGameStatus = new GameStatus();
       mTimeManager = new TimeManager(mHandler);
 
       mSettings = getSharedPreferences(Utils.PREF_SETTINGS, 0);
       mIsBackPressed = false;
    }
 
    private void playGame() {
       Utils.log(TAG, "playGame..");
       mGameStatus.setStaus(GameStatus.GAME_RUNNING);
       antView.playGame();
 
       showGamePause();
 
       hideGameInfo();
 
       // TODO timing starts
       mTimeManager.reset();
       mTimeManager.start();
 
    }
 
    private void resumeGame() {
       mGameStatus.setStaus(GameStatus.GAME_RUNNING);
 
       antView.resumeGame();
 
       showGamePause();
       hideGameInfo();
       // TODO timing resume
       mTimeManager.resume();
    }
 
    /**
     * if pause btn is clicked or activity runs onPause(),u should call this func
     */
    private void pauseGame() {
       Utils.log(TAG, "pauseGame..");
       antView.pauseGame();
       mGameStatus.setStaus(GameStatus.GAME_PAUSED);
       hideGamePause();
       showGameInfo("game paused");
       // TODO timing pause
       mTimeManager.pause();
    }
 
    /**
     * stop the game if ant gets home or lost
     * 
     * @param why
     *           why stops the game ,the reasons may be ant gets home or ant gets
     *           disappeared
     */
    private void stopGame(int why) {
       mGameStatus.setStaus(GameStatus.GAME_STOPPED);
       antView.stopGame();
       hideGamePause();
 
       String info = "";
       if (why == Utils.ANT_HOME) {
          info = "Great,Ant got home!";
       } else if (why == Utils.ANT_LOST) {
          info = "Opps,Ant got lost!!!";
       } else if (why == Utils.ANT_TIMEOUT) {
          info = "Oh,Time Out....";
       } else {
          info = "Ehhhhhhhhhhhhh!";
       }
       showGameInfo(info);
       // TODO timing clear
       mTimeManager.stop();
 
       // if (isHighScore(mScore)) {
       // Message msg = new Message();
       // msg.what = Utils.MSG_SCORE_BOARD;
       // msg.arg1 = mScore;
       // mHandler.sendMessage(msg);
       // }
    }
 
    private void showGamePause() {
       gamePause.setVisibility(View.VISIBLE);
       gamePlay.setVisibility(View.INVISIBLE);
    }
 
    private void hideGamePause() {
       gamePause.setVisibility(View.INVISIBLE);
       gamePlay.setVisibility(View.VISIBLE);
    }
 
    private void hideGameInfo() {
       mGameInfo.setVisibility(View.GONE);
    }
 
    /**
     * 
     * @param info
     *           pause or game over
     */
    private void showGameInfo(String info) {
       mGameInfo.setVisibility(View.VISIBLE);
       mGameInfoText.setText(info);
    }
 
    @Override
    public boolean onTouch(View v, MotionEvent event) {
       return false;
    }
 
    private int getUnit(int score) {
       int unit;
       if (score < 10) {
          unit = score;
       } else if (score < 100) {
          unit = score % 10;
       } else {
          int temp = score % 100;
          unit = temp % 10;
       }
       return unit;
    }
 
    private int getTen(int score) {
       int ten;
       if (score < 10) {
          ten = 0;
       } else if (score < 100) {
          ten = score / 10;
       } else {
          int temp = score % 100;
          ten = temp / 10;
       }
       return ten;
    }
 
    private int getHundred(int score) {
       int hundred;
       if (score < 100) {
          hundred = 0;
       } else {
          hundred = score / 100;
       }
       return hundred;
    }
 
    private void initScoreBoard() {
       int score0 = getHundred(mScore);
       int score1 = getTen(mScore);
       int score2 = getUnit(mScore);
       mScore0.setBackgroundResource(nums[score0]);
       mScore1.setBackgroundResource(nums[score1]);
       mScore2.setBackgroundResource(nums[score2]);
    }
 
    private void updateScore() {
       int score = mScore;
       mScore++;
       if (mScore > MAX_SCORE)
          return;
       int score0 = getHundred(mScore);
       int score1 = getTen(mScore);
       int score2 = getUnit(mScore);
 
       int score0t = getHundred(score);
       int score1t = getTen(score);
       boolean tenChange = false;
       boolean hundredChange = false;
       if (score0 != score0t)
          hundredChange = true;
       if (score1 != score1t)
          tenChange = true;
 
       mScore2.setAnimation(AnimationUtils
             .loadAnimation(this, R.anim.push_up_in));
       mScore2.setBackgroundResource(nums[score2]);
       mScore2.setAnimation(AnimationUtils.loadAnimation(this,
             R.anim.push_up_out));
       if (tenChange) {
          mScore1.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_in));
          mScore1.setBackgroundResource(nums[score1]);
          mScore1.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_out));
       }
       if (hundredChange) {
          mScore0.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_in));
          mScore0.setBackgroundResource(nums[score0]);
          mScore0.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_out));
       }
 
    }
 
    private void showScoreBoard(long score) {
       Intent intent = new Intent(this, BigNameActivity.class);
       intent.putExtra(Utils.SCORE, score);
       startActivity(intent);
    }
 
    private void updateTimeBoard(String time) {
       int min0 = Integer.parseInt(String.valueOf(time.charAt(0)));
       int min1 = Integer.parseInt(String.valueOf(time.charAt(1)));
       int sec0 = Integer.parseInt(String.valueOf(time.charAt(2)));
       int sec1 = Integer.parseInt(String.valueOf(time.charAt(3)));
       mTimeMin0.setBackgroundResource(nums[min0]);
       mTimeMin1.setBackgroundResource(nums[min1]);
       mTimeSec0.setBackgroundResource(nums[sec0]);
       mTimeSec1.setBackgroundResource(nums[sec1]);
    }
 
    private boolean isHighScore(long score) {
       boolean flag = false;
       if (score == 0) {
          return false;
       }
       List<Score> l = new ArrayList<Score>();
       DBAdapter db = new DBAdapter(this);
       db.open();
       l = db.getHighScores(Utils.TOP_SCORE_COUNT);
       db.close();
       if (l.size() < Utils.TOP_SCORE_COUNT) {
          flag = true;
       } else {
          long scoreT = l.get(l.size() - 1).getScore();
          if (score >= scoreT) {
             flag = true;
          }
       }
       return flag;
    }
 
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       Utils.log(TAG, "onKeyDown");
       switch (keyCode) {
       case KeyEvent.KEYCODE_BACK:
          mIsBackPressed = true;
          antView.pauseGame();
          mGameStatus.setStaus(GameStatus.GAME_PAUSED);
          break;
 
       default:
          break;
 
       }
       return super.onKeyDown(keyCode, event);
    }
 
    @Override
    protected void onSaveInstanceState(Bundle outState) {
       Utils.log(TAG, "onSaveInstanceState");
    }
 
    @Override
    protected void onRestoreInstanceState(Bundle outState) {
       Utils.log(TAG, "onRestoreInstanceState");
       SharedPreferences sp = this.getSharedPreferences(PREF, MODE_PRIVATE);
       int status = sp.getInt(GAME_STATE_PREF, GameStatus.GAME_INIT);
       if (status == GameStatus.GAME_PAUSED) {
          restorePausedGame();
       }
 
    }
 
    private void resetState() {
       SharedPreferences sp = this.getSharedPreferences(PREF, MODE_PRIVATE);
       sp.edit().putInt(GAME_STATE_PREF, GameStatus.GAME_INIT).commit();
 
    }
 
    private void saveState() {
       Utils.log(TAG, "saveState");
 
       antView.getGameStatus(mGameStatus);
 
       SharedPreferences sp = this.getSharedPreferences(PREF, MODE_PRIVATE);
       sp.edit().putInt(GAME_STATE_PREF, GameStatus.GAME_PAUSED).commit();
 
       float x = mGameStatus.getAntPos().x;
       float y = mGameStatus.getAntPos().y;
       float angle = mGameStatus.getAntAngle();
       sp.edit().putFloat("x", x).commit();
       sp.edit().putFloat("y", y).commit();
       sp.edit().putFloat("angle", angle).commit();
      sp.edit().putInt("time", this.mTimeManager.getTime()).commit();
 
    }
 
    private void restorePausedGame() {
 
       SharedPreferences sp = this.getSharedPreferences(PREF, MODE_PRIVATE);
       float x = sp.getFloat("x", 0);
       float y = sp.getFloat("y", 0);
       float angle = sp.getFloat("angle", 0);
      int time = sp.getInt("time", 0);
       
       if (mTimeManager !=null) {
         mTimeManager.restoreTime(time);
          mTimeManager.pause();
          
       } else {
          Utils.log(TAG, "lllllllllllllllltime is null");
       }
 
       mGameStatus.setStaus(GameStatus.GAME_PAUSED);
       mGameStatus.setAntAngle(angle);
       mGameStatus.setAntPos(new Pos(x, y));
       if (antView != null) {
          antView.setRestoredState(mGameStatus);
       }
    }
 
 }
