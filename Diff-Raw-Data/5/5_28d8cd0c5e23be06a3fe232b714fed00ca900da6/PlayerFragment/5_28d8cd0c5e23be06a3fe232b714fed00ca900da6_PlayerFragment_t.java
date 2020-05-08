 package ru.rutube.RutubePlayer.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.MotionEventCompat;
 import android.util.Log;
 import android.view.Display;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.ProgressBar;
 
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 
 import java.io.IOException;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.models.Constants;
 import ru.rutube.RutubePlayer.R;
 import ru.rutube.RutubePlayer.ctrl.PlayerController;
 import ru.rutube.RutubePlayer.views.VideoFrameLayout;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 03.05.13
  * Time: 20:14
  * To change this template use File | Settings | File Templates.
  */
 public class PlayerFragment extends Fragment implements PlayerController.PlayerView {
 
     public boolean onKeyDown(int keyCode) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_VOLUME_DOWN:
                 mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                 animateVolume(false);
                 return true;
             case KeyEvent.KEYCODE_VOLUME_UP:
                 mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                 animateVolume(true);
                 return true;
             default:
                 return false;
         }
     }
 
     protected void animateVolume(boolean up) {
         mVolumeImageView.setImageResource(up? R.drawable.volume_up : R.drawable.volume_down);
         AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
         aa.setDuration(500);
         aa.setFillAfter(true);
         aa.setAnimationListener(mVolumeAnimationListener);
         mVolumeImageView.startAnimation(aa);
     }
 
     /**
      * Интерфейс общения с активити, в которое встроен фрагмент с плеером
      */
     public interface PlayerEventsListener {
 
         /**
          * Событие начала воспроизведения
          */
         public void onPlay();
 
         /**
          * Событие окончания воспроизведения
          */
         public void onComplete();
 
         /**
          * Событие невозможности воспроизведения
          */
         public void onFail();
 
         /**
          * Двойной клик по плееру
          */
         public void onDoubleTap();
     }
     private static final String CONTROLLER = "controller";
     private static final String LOG_TAG = PlayerFragment.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
 
     protected PlayerController mController;
     protected SurfaceView mVideoView;
     protected ImageView mVolumeImageView;
     protected MediaPlayer mPlayer;
     protected Uri mStreamUri;
     protected ProgressBar mLoadProgressBar;
     protected PlayerEventsListener mPlayerEventsListener;
     protected RutubeMediaController mMediaController;
     protected int mBufferingPercent = 0;
     protected boolean mPrepared = false;
     protected GestureDetector mDetector;
     protected AudioManager mAudioManager;
 
     protected PowerManager.WakeLock mWakeLock;
     private Dialog mDialog;
 
     protected RutubeMediaController.ToggleFullscreenListener mToggleFullscreenListener = new RutubeMediaController.ToggleFullscreenListener() {
         @Override
         public void toggleFullscreen() {
             if (mPlayerEventsListener == null)
                 return;
             mPlayerEventsListener.onDoubleTap();
         }
     };
 
     protected Animation.AnimationListener mVolumeAnimationListener = new Animation.AnimationListener() {
         @Override
         public void onAnimationStart(Animation animation) {
             mVolumeImageView.setVisibility(View.VISIBLE);
         }
 
         @Override
         public void onAnimationEnd(Animation animation) {
             mVolumeImageView.setVisibility(View.GONE);
         }
 
         @Override
         public void onAnimationRepeat(Animation animation) {
 
         }
     };
 
     protected GestureDetector.OnGestureListener mGestureEventListener = new GestureDetector.OnGestureListener() {
         @Override
         public boolean onDown(MotionEvent motionEvent) {
             return false;
         }
 
         @Override
         public void onShowPress(MotionEvent motionEvent) {
 
         }
 
         @Override
         public boolean onSingleTapUp(MotionEvent motionEvent) {
             return false;
         }
 
         @Override
         public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
             return false;
         }
 
         @Override
         public void onLongPress(MotionEvent motionEvent) {
 
         }
 
         @Override
         public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
             return false;
         }
     };
 
 
     protected GestureDetector.OnDoubleTapListener mOnDoubleTabListener = new GestureDetector.OnDoubleTapListener() {
         @Override
         public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
             return false;
         }
 
         @Override
         public boolean onDoubleTap(MotionEvent motionEvent) {
             if (D) Log.d(LOG_TAG, "onDoubleTap");
             if (mPlayerEventsListener != null)
                 mPlayerEventsListener.onDoubleTap();
             return false;
         }
 
         @Override
         public boolean onDoubleTapEvent(MotionEvent motionEvent) {
             return false;
         }
     };
 
     /**
      * Обработчик закрытия диалога сообщения об ошибке
      */
     protected DialogInterface.OnDismissListener mErrorListener = new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialogInterface) {
             if (mPlayerEventsListener != null)
                 mPlayerEventsListener.onFail();
         }
     };
 
 
     /**
      * При изменении размеров контейнера, содержащего SurfaceView, изменяет размеры самого
      * SurfaceView, так как автоматически этого почему-то не происходит.
      */
     protected VideoFrameLayout.OnSizeChangedListener mOnSizeChangedListener = new VideoFrameLayout.OnSizeChangedListener() {
         @Override
         public void onSizeChanged(int width, int height) {
             if (D) Log.d(LOG_TAG, String.format("onSizeChanged: %dx%d", width, height));
             if (mPlayer == null)
                 return;
             int vw = mPlayer.getVideoWidth();
             int vh = mPlayer.getVideoHeight();
             if (vw * vh == 0)
                 return;
             boolean fullscreen = isFullscreen();
             // когда плеер не фулскрин, убираем черные поля, растягивая видео на всё доступное
             // пространство
             float scale = (fullscreen)?Math.min(((float)height)/vh, ((float)width)/vw):
                     Math.max(((float) height) / vh, ((float) width) / vw);
             int w = (int)(vw * scale);
             int h = (int)(vh * scale);
             if (D) Log.d(LOG_TAG, String.format("video size: %dx%d", vw, vh));
             if (D) Log.d(LOG_TAG, String.format("fit to size: %dx%d", w, h));
 
             ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
             lp.height = h;
             lp.width = w;
             // На Lenovo ThinkPad почему-то только после "дергания" контролов видео начинает
             // корректно апскейлиться до полного экрана.
             mMediaController.show(1);
         }
     };
 
 
     /**
      * Обработчики различных интерфейсов, необходимые, чтобы заставить MediaPlayer показывать на
      * SurfaceView видео под управлением MediaController
      */
 
     protected SurfaceHolder.Callback mSurfaceCallbackListener = new SurfaceHolder.Callback() {
         @Override
         public void surfaceCreated(SurfaceHolder surfaceHolder) {
             if (D) Log.d(LOG_TAG, "surfaceCreated");
             if (mPlayer == null)
                 initMediaPlayer();
             mPlayer.setDisplay(surfaceHolder);
         }
 
         @Override
         public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
             if (D) Log.d(LOG_TAG, "surfaceChanged");
             if (mPlayer == null)
                 initMediaPlayer();
             mPlayer.setDisplay(surfaceHolder);
         }
 
         @Override
         public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
             if (D) Log.d(LOG_TAG, "surfaceDestroyed");
         }
     };
 
     protected MediaController.MediaPlayerControl mMediaPlayerControl = new MediaController.MediaPlayerControl() {
 
         @Override
         public int getAudioSessionId() {
             return 0;
         }
 
         @Override
         public void start() {
             mPlayer.start();
         }
 
         @Override
         public void pause() {
             mPlayer.pause();
         }
 
         @Override
         public int getDuration() {
             return mPlayer.getDuration();
         }
 
         @Override
         public int getCurrentPosition() {
             return mPlayer.getCurrentPosition();
         }
 
         @Override
         public void seekTo(int millis) {
             mPlayer.seekTo(millis);
         }
 
         @Override
         public boolean isPlaying() {
             return mPlayer.isPlaying();
         }
 
         @Override
         public int getBufferPercentage() {
             return mBufferingPercent;
         }
 
         @Override
         public boolean canPause() {
             return true ;
         }
 
         @Override
         public boolean canSeekBackward() {
             return true;
         }
 
         @Override
         public boolean canSeekForward() {
             return true;
         }
 
     };
 
     protected MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
 
         @Override
         public void onCompletion(MediaPlayer mediaPlayer) {
             if (D) Log.d(LOG_TAG, "OnCompletion");
             mController.onCompletion();
             onComplete();
         }
     };
 
     protected MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
         @Override
         public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
             if (D) Log.d(LOG_TAG, "onError");
             mController.onPlaybackError();
             return true;
         }
     };
 
 
     protected MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
         @Override
         public void onPrepared(MediaPlayer mediaPlayer) {
             if (D) Log.d(LOG_TAG, "OnPrepared");
             mController.onViewReady();
             mMediaController.setMediaPlayer(mMediaPlayerControl);
             if (D) Log.d(LOG_TAG, "Prepared!");
             mPrepared = true;
         }
     };
 
     protected MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
 
         @Override
         public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
             mBufferingPercent = percent;
 
         }
     };
 
     protected View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
 
         @Override
         public boolean onTouch(View view, MotionEvent motionEvent) {
             mDetector.onTouchEvent(motionEvent);
             int action = MotionEventCompat.getActionMasked(motionEvent);
             if (D) Log.d(LOG_TAG, "onTouch: " + String.valueOf(action));
             switch(action) {
                 case MotionEvent.ACTION_DOWN:
                     if (!mPrepared) {
                         if (D) Log.d(LOG_TAG, "preparing, don't show media controller");
                         return true;
                     }
                     if (D) Log.d(LOG_TAG, "MP is prepared");
                     mMediaController.show();
                     return true;
                 default:
                     return false;
             }
         }
     };
 
     protected MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChanged = new MediaPlayer.OnVideoSizeChangedListener() {
 
         @Override
         public void onVideoSizeChanged(MediaPlayer mediaPlayer, int w, int h) {
             int surfaceView_Width = mVideoView.getWidth();
             int surfaceView_Height = mVideoView.getHeight();
 
             float ratio_width = surfaceView_Width/ (float) w;
             float ratio_height = surfaceView_Height/ (float) h;
             float aspectratio = (float) w / (float) h;
 
             boolean fullscreen = isFullscreen();
 
 
             ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
             assert layoutParams != null;
             if ((ratio_width > ratio_height) == fullscreen){
                 layoutParams.width = (int) (surfaceView_Height * aspectratio);
                 layoutParams.height = surfaceView_Height;
             }else{
                 layoutParams.width = surfaceView_Width;
                 layoutParams.height = (int) (surfaceView_Width / aspectratio);
             }
             if (D) Log.d(LOG_TAG, "size: onVideoSizeChanged");
             mVideoView.setLayoutParams(layoutParams);
 
         }
     };
 
     protected boolean isFullscreen() {
        FragmentActivity activity = getActivity();
        return activity != null && (activity.getWindow().getAttributes().flags
                 & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
     }
 
     //
     // переопределенные методы из Fragment
     //
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         if (D) Log.d(LOG_TAG, "onActivityCreated");
         init(savedInstanceState);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         if (D) Log.d(LOG_TAG, "onCreateView");
         View view = inflater.inflate(R.layout.player_fragment, container, false);
         assert view != null;
         view.setOnTouchListener(mOnTouchListener);
         return view;
     }
 
     @Override
     public void onResume() {
         if (D) Log.d(LOG_TAG, "onResume");
         super.onResume();
         // Плеер может быть уже проинициализирован в onCreateView
         // и точно уничтожается в onPause
         if (mPlayer == null)
             initMediaPlayer();
 
         // при возобновлении уже проигрываемого видео элементы управления показываются еще на
         // стадии подготовки MediaPlayer, т.е. потенциальные проблемы.
         mMediaController.hide();
 
         mController.onResume();
         mMediaController.setMediaPlayer(mMediaPlayerControl);
         mWakeLock.acquire();
     }
 
     private void initMediaPlayer() {
         if (D) Log.d(LOG_TAG, "initMediaPlayer");
         mPrepared = false;
         if (mPlayer != null)
             mPlayer.release();
         mPlayer = new MediaPlayer();
         // когда вызывается initMediaPlayer, SurfaceView еще может быть недоступна.
         // Поэтому setDisplay вызывается в соответствующем callack-е SurfaceView
         // mPlayer.setDisplay(mVideoView.getHolder());
         mPlayer.setOnPreparedListener(mOnPreparedListener);
         mPlayer.setOnCompletionListener(mOnCompletionListener);
         mPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
         mPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChanged);
         mPlayer.setOnErrorListener(mOnErrorListener);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         if (D) Log.d(LOG_TAG, "onPause");
         mController.onPause();
         mPlayer.reset();
         // невыполнение release загоняет плеер в ошибочное состояние, которое выливается в ошибку
         // Error (1, -110), После release плеер необходимо инициализировать заново.
         mPlayer.release();
         mPlayer = null;
         // messageHandler после детача получает очередное сообщение о прогрессе и пытается вызвать
         // у деинициализированного плеера getDuration. Результат - ISE/NPE.
         mMediaController.setMediaPlayer(null);
         if (mWakeLock.isHeld())
             mWakeLock.release();
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         if (D) Log.d(LOG_TAG, "Controller detached");
         mController.detach();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (D) Log.d(LOG_TAG, "onSaveInstanceState");
         outState.putParcelable(CONTROLLER, mController);
     }
 
     //
     // Реализация интерфейса PlayerController.PlayerView
     //
 
     @Override
     public int getCurrentOffset() {
         return mPlayer.getCurrentPosition();
     }
 
     @Override
     public void stopPlayback() {
         if (mPlayer != null && mPlayer.isPlaying())
             mPlayer.stop();
     }
 
     @Override
     public void seekTo(int millis) {
         if (D) Log.d(LOG_TAG, "Seek To: " + String.valueOf(millis / 1000));
         mPlayer.seekTo(millis);
     }
 
     @Override
     public void pauseVideo() {
         mPlayer.pause();
     }
 
     @Override
     public void onComplete() {
         if (mPlayerEventsListener != null) {
             if (D) Log.d(LOG_TAG, "onComplete");
             mPlayerEventsListener.onComplete();
         }
         stopPlayback();
         toggleMediaController(false);
     }
 
     @Override
     public void setStreamUri(Uri uri) {
         if (D) Log.d(LOG_TAG, "setStreamUri " + String.valueOf(uri));
         setVideoUri(uri);
         mStreamUri = uri;
     }
 
     @Override
     public void showError(String error) {
         Activity activity = getActivity();
         if (activity == null)
             return;
         AlertDialog.Builder builder = new AlertDialog.Builder(activity);
         mDialog = builder.
                 setTitle(android.R.string.dialog_alert_title).
                 setMessage(error).
                 create();
         mDialog.setOnDismissListener(mErrorListener);
         mDialog.show();
     }
 
     @Override
     public void startPlayback() {
         if (D) Log.d(LOG_TAG, "StartPlayback");
         if (mPlayerEventsListener != null)
             mPlayerEventsListener.onPlay();
         startVideoPlayback();
     }
     @Override
     public void setLoading() {
         mLoadProgressBar.setVisibility(View.VISIBLE);
     }
 
     @Override
     public void setLoadingCompleted() {
         mLoadProgressBar.setVisibility(View.GONE);
     }
 
     @Override
     public void toggleThumbnail(boolean visible) {
         int visibility = (visible)? View.VISIBLE : View.INVISIBLE;
         View view = getView();
         assert view != null;
         View thumbnail = view.findViewById(R.id.thumbnail);
         thumbnail.setVisibility(visibility);
     }
 
     @Override
     public void setVideoTitle(String title) {
         mMediaController.setVideoTitle(title);
     }
 
     @Override
     public void setThumbnailUri(Uri uri) {
         View view = getView();
         assert view != null;
         NetworkImageView netImgView = (NetworkImageView) view.findViewById(R.id.thumbnail);
         ImageLoader imageLoader = mController.getImageLoader();
         if (imageLoader == null)
             throw new NullPointerException("no image loader");
         netImgView.setImageUrl(uri.toString(), imageLoader);
     }
 
     //
     // Собственные публичные методы
     //
 
     public PlayerController getController() {
         return mController;
     }
 
     public Dialog getDialog() {
         return mDialog;
     }
 
     /**
      * Повторное воспроизведение видео
      */
     public void replay() {
         mController.replay();
     }
 
 
     /**
      * Инициализирует обрабочтик событий PlayerEventsListener
      * @param playerEventsListener контейнер фрагмента, обрабатывающий события
      */
     public void setPlayerStateListener(PlayerEventsListener playerEventsListener) {
         mPlayerEventsListener = playerEventsListener;
     }
 
     /**
      * Меняет видимость элементов управления плеером
      * @param visible true если необходимо сделать элементы управления видимыми
      */
     protected void toggleMediaController(boolean visible) {
         if (visible)
             mMediaController.show();
         else
             mMediaController.hide();
     }
 
     /**
      * Инициализация видеоэлемента
      */
     protected void initVideoView() {
         View view = getView();
         assert view != null;
 
         VideoFrameLayout container = ((VideoFrameLayout)view.findViewById(R.id.center_video_view));
         container.setOnSizeChangedListener(mOnSizeChangedListener);
 
         mVideoView = (SurfaceView) view.findViewById(R.id.video_view);
         SurfaceHolder holder = mVideoView.getHolder();
         assert holder != null;
         holder.addCallback(mSurfaceCallbackListener);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         if (mPlayer == null)
             initMediaPlayer();
     }
 
     private void initProgressBar() {
         View view = getView();
         assert view != null;
         mLoadProgressBar = (ProgressBar) view.findViewById(R.id.load);
         return;
     }
 
     private void initMediaController() {
         View view = getView();
         assert view != null;
         mMediaController = new RutubeMediaController(getActivity());
         mMediaController.setAnchorView((ViewGroup) view.findViewById(R.id.center_video_view));
         mMediaController.setOnTouchListener(mOnTouchListener);
         mMediaController.setToggleFullscreenListener(mToggleFullscreenListener);
         return;
     }
 
     /**
      * Задание Uri видеопотока для видеоэлемента
      * @param uri Uri видеопотока
      */
     protected void setVideoUri(Uri uri) {
         if (uri != null)
             try {
                 if (mPlayer == null)
                     initMediaPlayer();
                 mPlayer.reset();
                 mPrepared = false;
                 if (D) Log.d(LOG_TAG, "Preparing!");
                 mPlayer.setDataSource(getActivity(), uri);
                 mPlayer.prepareAsync();
                 mBufferingPercent = 0;
             } catch (IOException e) {
                 e.printStackTrace();
             }
 //            mVideoView.setVideoURI(uri);
     }
 
     /**
      * Начинает воспроизведение видео
      */
     protected void startVideoPlayback() {
         if (D) Log.d(LOG_TAG, "startVideoPlayback");
         //mVideoiew.setVideoURI(mStreamUri);
         mPlayer.start();
 //        mVideoView.start();
     }
 
     /**
      * Инициализация логики плеера
      * @param savedInstanceState сохраненное состояние активити
      */
     private void init(Bundle savedInstanceState) {
         mStreamUri = null;
         Activity activity = getActivity();
         assert activity != null;
 
         PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
         mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "VideoPlayer");
 
         mDetector = new GestureDetector(activity, mGestureEventListener);
         mDetector.setOnDoubleTapListener(mOnDoubleTabListener);
 
         Intent intent = activity.getIntent();
         Uri videoUri = intent.getData();
         Uri thumbnailUri = intent.getParcelableExtra(Constants.Params.THUMBNAIL_URI);
 
         initMediaController();
         initProgressBar();
 
         mAudioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
 
 
         mVolumeImageView = (ImageView)getView().findViewById(R.id.volumeImageView);
 
         mController = null;
         if (savedInstanceState != null) {
             mController = savedInstanceState.getParcelable(CONTROLLER);
         }
         if (mController == null) {
             mController = new PlayerController(videoUri, thumbnailUri);
         }
         mController.attach(activity, this);
 
         initVideoView();
 
         if (savedInstanceState == null)
             mController.requestStream();
     }
 }
