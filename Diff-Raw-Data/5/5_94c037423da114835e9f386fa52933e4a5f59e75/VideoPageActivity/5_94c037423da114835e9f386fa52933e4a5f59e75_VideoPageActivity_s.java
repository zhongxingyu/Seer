 package ru.rutube.RutubePlayer.ui;
 
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.database.ContentObserver;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Html;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.OrientationEventListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.models.Author;
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubePlayer.R;
 import ru.rutube.RutubePlayer.ctrl.VideoPageController;
 
 
 /**
  * Активити фулскрин-плеера.
  * Возможен старт по intent-action: ru.rutube.player.play c Uri видео вида:
  * http://rutube.ru/video/<video_id>/
  */
 public class VideoPageActivity extends SherlockFragmentActivity
         implements PlayerFragment.PlayerEventsListener,
         EndscreenFragment.ReplayListener,
         VideoPageController.VideoPageView {
     protected static final boolean D = BuildConfig.DEBUG;
     protected static int mLayoutResId = R.layout.player_activity;
     private boolean mIsAutorotateEnabled;
 
     protected static class ViewHolder {
         public PlayerFragment playerFragment;
         public EndscreenFragment endscreenFragment;
         public ViewGroup videoInfoContainer;
         public TextView description;
         public TextView hits;
         public TextView duration;
         public TextView author;
         public TextView title;
     }
 
     private static final String CONTROLLER = "controller";
     private static final String FULLSCREEN = "fullscreen";
     private static final String LOG_TAG = VideoPageActivity.class.getName();
 
     protected boolean mIsTablet;
     protected boolean mIsFullscreen;
     protected boolean mIsLandscape;
     protected ViewHolder mViewHolder;
     protected VideoPageController mController;
     private OrientationEventListener mOrientationListener;
 
     /**
      * Отложенная задача, включающая обработку событий от датчика ориентации.
      */
     protected Runnable mEnableOrientationEventListenerTask = new Runnable() {
         public void run() {
             if (D) Log.d(LOG_TAG, "Release Orientation");
             mOrientationListener.enable();
         }
     };
 
     /**
      * Обработчик изменения настроек автоповорота
      */
     protected ContentObserver mRotationObserver = new ContentObserver(new Handler()) {
 
         @Override
         public void onChange(boolean selfChange) {
             // При изменении настроек автоповорота включаем и выключаем обработчик событий
             // от датчика ориентации.
             checkAutoOrientation();
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         if (D) Log.d(LOG_TAG, "onCreate");
         super.onCreate(savedInstanceState);
         checkOrientation();
         mIsFullscreen = (savedInstanceState == null) ?
                 getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                 savedInstanceState.getBoolean(FULLSCREEN);
         initController(savedInstanceState);
         initWindow();
         setContentView(mLayoutResId);
         init();
         toggleFullscreen(mIsFullscreen, false);
         transformLayout(mIsLandscape);
     }
 
     @Override
     public void onBackPressed() {
         mController.onBackPressed();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Обработчик нажатий кнопок громкости
         switch (keyCode) {
             case KeyEvent.KEYCODE_VOLUME_DOWN:
             case KeyEvent.KEYCODE_VOLUME_UP:
                 return mViewHolder.playerFragment.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
             default:
                 return super.onKeyDown(keyCode, event);
         }
     }
 
     @Override
     public void closeVideoPage() {
         finish();
     }
 
     @Override
     public void onComplete() {
         if (D) Log.d(LOG_TAG, "onComplete");
         toggleEndscreen(true);
     }
 
     @Override
     public boolean isFullscreen() {
         return mIsFullscreen;
     }
 
     @Override
     public void replay() {
         mViewHolder.playerFragment.replay();
     }
 
     @Override
     public void onPlay() {
         toggleEndscreen(false);
     }
 
     @Override
     public void onFail() {
         finish();
     }
 
     @Override
     public void setVideoInfo(Video video) {
         bindTitle(video);
         bindAuthor(video);
         bindDuration(video);
         bindHits(video);
         bindDescription(video);
     }
 
     @Override
     public void onDoubleTap() {
         mController.onDoubleTap();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mController.attach(this, this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mController.detach();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putParcelable(CONTROLLER, mController);
         outState.putBoolean(FULLSCREEN, mIsFullscreen);
     }
 
     @Override
     public void toggleFullscreen(boolean isFullscreen, boolean rotate) {
         mIsFullscreen = isFullscreen;
         initWindow(isFullscreen);
         if (!isFullscreen) {
             if (!mIsTablet && rotate) {
                 // Для телефонов выход из полного экрана - это переход в портретную ориентацию
                 setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
             }
             // Для планшетов - смена layout
             if (mViewHolder.videoInfoContainer != null)
                 mViewHolder.videoInfoContainer.setVisibility(View.VISIBLE);
         } else {
             if (mViewHolder.videoInfoContainer != null)
                 mViewHolder.videoInfoContainer.setVisibility(View.GONE);
             if (rotate)
                 setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
     }
 
     @Override
     public int getScreenOrientation() {
         Display getOrient = getWindowManager().getDefaultDisplay();
         int orientation;
         if (getOrient.getWidth() == getOrient.getHeight()) {
             orientation = Configuration.ORIENTATION_SQUARE;
         } else {
             if (getOrient.getWidth() < getOrient.getHeight()) {
                 orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
             } else {
                 orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
             }
         }
         return orientation;
     }
 
     /**
      * Задает ориентацию устройства
      * @param orientation значение ориентации, соответствующее ActivityInfo.SCREEN_ORIENTATION_*
      */
     @Override
     public void setScreenOrientation(int orientation) {
         if (D) Log.d(LOG_TAG, String.format("setScreenOrientation: %d", orientation));
         // временно выключаем обработку событий датчика ориентации
         mOrientationListener.disable();
         // изменяем ориентацию
         setRequestedOrientation(orientation);
         // добавляем отложенный вызов включения обработчика собитый датчика ориентации
         Handler handler = new Handler();
         handler.postDelayed(mEnableOrientationEventListenerTask, 500);
     }
 
     protected void checkAutoOrientation() {
         mIsAutorotateEnabled = Settings.System.getInt(
                 getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
         if (!mIsAutorotateEnabled) {
             if (D) Log.d(LOG_TAG, "Autorotate is off, skip orientation handling");
             mOrientationListener.disable();
         } else {
             if (D) Log.d(LOG_TAG, "Autorotate is on, enable orientation handling");
             mOrientationListener.enable();
         }
     }
 
 
     protected void transformLayout(boolean isLandscape) {}
 
     protected void checkOrientation() {
         mIsLandscape = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
     }
 
     protected void bindDescription(Video video) {
         String description = video.getDescription();
        mViewHolder.description.setText(Html.fromHtml(description));
     }
 
     protected void bindHits(Video video) {
         String hits = video.getHitsText(this);
         mViewHolder.hits.setText(hits);
     }
 
     protected void bindDuration(Video video) {
         int duration = video.getDuration();
         mViewHolder.duration.setText(DateUtils.formatElapsedTime(duration / 1000));
     }
 
     protected void bindAuthor(Video video) {
         Author author = video.getAuthor();
         if (author != null) {
             TextView authorName = mViewHolder.author;
             authorName.setText(author.getName());
             authorName.setTag(author.getFeedUrl());
         }
     }
 
     protected void bindTitle(Video video) {
         mViewHolder.title.setText(video.getTitle());
     }
 
     protected ViewHolder getHolder() {
         return new ViewHolder();
     }
 
     protected void initHolder(ViewHolder holder) {
         FragmentManager fm = getSupportFragmentManager();
         holder.playerFragment = (PlayerFragment) fm.findFragmentById(R.id.player_fragment);
         holder.endscreenFragment = (EndscreenFragment) fm.findFragmentById(R.id.endscreen_fragment);
         holder.videoInfoContainer = (ViewGroup)findViewById(R.id.video_info_container);
         holder.description = ((TextView) findViewById(R.id.description));
         holder.hits = ((TextView) findViewById(R.id.hits));
         holder.duration = ((TextView) findViewById(R.id.duration));
         holder.author = (TextView) findViewById(R.id.author_name);
         holder.title = ((TextView) findViewById(R.id.video_title));
         mViewHolder = holder;
     }
 
     protected void initWindow() {
         initWindow(mIsFullscreen);
     }
 
     /**
      * Инициализирует параметры окна приложения
      */
     protected void initWindow(boolean isFullscreen) {
         if (D) Log.d(LOG_TAG, "initWindow fullscreen:" + String.valueOf(isFullscreen));
         WindowManager.LayoutParams attrs = getWindow().getAttributes();
         if (isFullscreen) {
             attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
         } else {
             attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
         }
         getWindow().setAttributes(attrs);
     }
 
     /**
      * Возвращает обработчик событий от датчика ориентации
      * @return OrientationEventListener
      */
     protected OrientationEventListener getOrientationEventListener() {
 
         return new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
 
             /**
              * Обрабатывает события от датчика ориентации
              * Заменяет явно заданную ориентацию экрана на "Не указано", когда пользователь
              * поворачивает устройтво так, чтобы реальная ориентация совпала с указанной
              * через setRequestedOrientation
              @param degree угол поворота устройства
              */
             @Override
             public void onOrientationChanged(int degree) {
                 if (!mIsAutorotateEnabled)
                     return;
                 if (D) Log.d(LOG_TAG, String.format("Orientation changed! %d", degree));
                 // Переводим угол поворота в константы ориентации устройства
                 degree = ((degree + 45) / 90) % 4;
                 int orientation = (degree == 0 || degree == 2)?
                         ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                         ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                 if (D) Log.d(LOG_TAG, String.format("Orient: %d %d", orientation,
                         getScreenOrientation()));
                 // Если реальная ориентация совпала с указанной, сбрасываем ориентацию экрана.
                 if (orientation == getScreenOrientation()) {
                     if (D) Log.d(LOG_TAG, "Orient: Rotating screen");
                     setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                 }
             }
         };
     }
 
     protected void init() {
 
         mOrientationListener = getOrientationEventListener();
         checkAutoOrientation();
         getContentResolver().registerContentObserver(Settings.System.getUriFor
                 (Settings.System.ACCELEROMETER_ROTATION), true, mRotationObserver);
 
 
         ViewHolder holder = getHolder();
         initHolder(holder);
 
         mViewHolder.playerFragment.setPlayerStateListener(this);
         mViewHolder.endscreenFragment.setReplayListener(this);
 
         toggleEndscreen(false);
 
         mIsTablet = getResources().getString(R.string.device_type, "phone").equals("tablet");
 
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
     }
 
     private void toggleEndscreen(boolean visible) {
         if (D) Log.d(LOG_TAG, "toggleEndscreen: " + String.valueOf(visible));
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         if (visible)
             ft.show(mViewHolder.endscreenFragment);
         else
             ft.hide(mViewHolder.endscreenFragment);
         try {
             // Вызывает ISE после saveInstanceState()
             ft.commit();
         } catch (IllegalStateException ignored) {}
     }
 
     private void initController(Bundle savedInstanceState) {
         if (savedInstanceState != null)
             mController = savedInstanceState.getParcelable(CONTROLLER);
         else
             mController = new VideoPageController(getIntent().getData());
     }
 
 }
