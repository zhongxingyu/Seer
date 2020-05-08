 package ru.rutube.RutubePlayer.ctrl;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 import android.util.SparseIntArray;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.HttpClientStack;
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.Volley;
 
 import java.util.List;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.HttpTransport;
 import ru.rutube.RutubeAPI.RutubeApp;
 import ru.rutube.RutubeAPI.models.Constants;
 import ru.rutube.RutubeAPI.models.TrackInfo;
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubeAPI.requests.RequestListener;
 import ru.rutube.RutubeAPI.requests.Requests;
 import ru.rutube.RutubePlayer.R;
 
 /**
  * Created by tumbler on 27.07.13.
  */
 public class PlayerController implements Parcelable, RequestListener {
 
     /**
      * Интерфейс для представления плеера
      */
     public interface PlayerView{
         /**
          * Задает Uri видеопотока видеоэлементу
          * @param uri Uri видеопотока
          */
         public void setStreamUri(Uri uri);
 
         public void setVideoTitle(String title);
         public void setThumbnailUri(Uri uri);
 
         public void showError(String error);
 
         public void startPlayback();
 
         /**
          * Обрабатывает завершение показа видео
          */
         public void onComplete();
 
         /**
          * Получить текущее смещение видео
          * @return смещение от старта в миллисекундах
          */
         public int getCurrentOffset();
 
         public void stopPlayback();
 
         public void pauseVideo();
 
         public void seekTo(int millis);
 
         public void setLoading();
         public void setLoadingCompleted();
         public void toggleThumbnail(boolean visible);
     }
 
     public static final int STATE_NEW = 0;
     public static final int STATE_STARTING = 1;
     public static final int STATE_PLAYING = 2;
     public static final int STATE_COMPLETED = 3;
     public static final int STATE_ERROR = 4;
 
     private static final String LOG_TAG = PlayerController.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
     private static final int TOTAL_REQUESTS_NEEDED = 4;
 
     protected RequestQueue mRequestQueue;
     protected ImageLoader mImageLoader;
 
     private Uri mVideoUri;
     private Uri mStreamUri;
     private Video mVideo;
     private TrackInfo mTrackInfo;
     private int mState;
     private int mVideoOffset;
     private SparseIntArray mOptionsErrorMap;
     private SparseIntArray mTrackInfoErrorMap;
     private Boolean mPlaybackAllowed = null;
 
     private volatile int mPlayRequestStage;
     private boolean mAttached;
     private PlayerView mView;
     private Context mContext;
     private Uri mThumbnailUri;
 
     //
     // Реализация интерфейса RequestListener
     //
 
     /**
      * Обработка результатов запросов к API.
      * Ждет выполнения запросов TRACK_INFO и PLAY_OPTIONS, после завершения обоих запросов
      * начинает проигрывание видео.
      * @param tag тег запроса
      * @param result данные
      *
      */
     @Override
     public void onResult(int tag, Bundle result) {
 
         if (tag == Requests.TRACK_INFO) {
             if (D) Log.d(LOG_TAG, "Got Trackinfo");
             mTrackInfo = result.getParcelable(Constants.Result.TRACKINFO);
             assert mView != null;
             if (mTrackInfo == null) {
                 Integer errCode = result.getInt(Constants.Result.TRACKINFO_ERROR);
                 if (D) Log.e(LOG_TAG, "Track info error " + String.valueOf(errCode));
                 mRequestQueue.cancelAll(Requests.PLAY_OPTIONS);
                 if (mState == STATE_ERROR)
                     return;
                 setState(STATE_ERROR);
                 mView.showError(mContext.getResources().getString(
                             mTrackInfoErrorMap.get(errCode, R.string.video_deleted)));
                 return;
             }
             mView.setVideoTitle(mTrackInfo.getTitle());
             if (mPlaybackAllowed == null || mPlaybackAllowed){
             //    mView.setStreamUri(mTrackInfo.getBalancerUrl());
                 JsonObjectRequest request = mTrackInfo.getMP4UrlRequest(mContext, this);
                 mRequestQueue.add(request);
             }
             mPlayRequestStage++;
         }
 
         if (tag == Requests.PLAY_OPTIONS) {
             if (D) Log.d(LOG_TAG, "Got PlayOptions");
             mPlaybackAllowed = result.getBoolean(Constants.Result.ACL_ALLOWED, false);
             Integer errCode = result.getInt(Constants.Result.ACL_ERRCODE, 0);
             if (!mPlaybackAllowed) {
                 if (D) Log.w(LOG_TAG, "Playback not allowed");
                 mRequestQueue.cancelAll(Requests.TRACK_INFO);
                 mRequestQueue.cancelAll(Requests.BALANCER_JSON);
                 if (mState == STATE_ERROR)
                     return;
                 setState(STATE_ERROR);
                 Integer error_resource;
                 if (errCode == 0){
                     errCode = result.getInt(Constants.Result.TRACKINFO_ERROR, 0);
                     error_resource = mTrackInfoErrorMap.get(errCode, R.string.video_deleted);
                 } else {
                     error_resource = mOptionsErrorMap.get(errCode, R.string.failed_to_load_data);
                 }
                 mView.showError(mContext.getResources().getString(error_resource));
                 return;
             } else {
 //                if (mTrackInfo != null) {
 //                    mView.setStreamUri(mTrackInfo.getBalancerUrl());
 //                }
                 if (mStreamUri != null) {
                     mView.setStreamUri(mStreamUri);
                 }
                 if (mThumbnailUri == null){
                     Uri thumbnailUri = result.getParcelable(Constants.Result.PLAY_THUMBNAIL);
                     mView.setThumbnailUri(thumbnailUri);
                 }
             }
             mPlayRequestStage++;
         }
 
         if (tag == Requests.BALANCER_JSON) {
             if (D) Log.d(LOG_TAG, "Got Balancer Result");
             String mp4url = result.getString(Constants.Result.MP4_URL);
             if (D) Log.d(LOG_TAG, "Got mp4 uri: " + mp4url);
             if (mp4url != null) {
                 mStreamUri = Uri.parse(mp4url);
                 if (mPlaybackAllowed != null && mPlaybackAllowed)
                     mView.setStreamUri(mStreamUri);
             }
             mPlayRequestStage++;
         }
 
         checkReadyToPlay();
     }
 
     @Override
     public void onVolleyError(VolleyError error) {
         if (D) Log.e(LOG_TAG, error.toString());
         mView.showError(mContext.getResources().getString(R.string.failed_to_load_data));
     }
 
     @Override
     public void onRequestError(int tag, RequestError error) {
         if (D) Log.e(LOG_TAG, error.toString());
         mView.showError(mContext.getResources().getString(R.string.failed_to_load_data));
     }
 
     //
     // Конструкторы
     //
 
     public PlayerController(Uri videoUri, Uri thumbnailUri) {
         mContext = null;
         mView = null;
         mVideoUri = videoUri;
         mState = STATE_NEW;
         mThumbnailUri = thumbnailUri;
         mVideoOffset = 0;
         initErrorMap();
     }
 
     private void initErrorMap() {
         mOptionsErrorMap = new SparseIntArray();
         mOptionsErrorMap.put(1, R.string.video_not_allowed);
         mOptionsErrorMap.put(2, R.string.region_not_allowed);
         mOptionsErrorMap.put(3, R.string.mobile_not_allowed);
         mOptionsErrorMap.put(4, R.string.anonymous_not_allowed);
         mTrackInfoErrorMap = new SparseIntArray();
         mTrackInfoErrorMap.put(3, R.string.video_deleted_by_user);
         mTrackInfoErrorMap.put(4, R.string.video_deleted_by_admin);
         mTrackInfoErrorMap.put(7, R.string.video_deleted_inappropriate);
         mTrackInfoErrorMap.put(8, R.string.video_deleted_by_rightholder);
         mTrackInfoErrorMap.put(11, R.string.video_doesnt_exist);
         mTrackInfoErrorMap.put(12, R.string.video_is_hidden);
         mTrackInfoErrorMap.put(13, R.string.mobile_not_allowed);
 
     }
 
     protected PlayerController(Uri videoUri, Uri thumbnailUri, int state, int offset, TrackInfo trackInfo) {
         this(videoUri, thumbnailUri);
         mState = state;
         mVideoOffset = offset;
         mTrackInfo = trackInfo;
     }
 
     // Реализация Parcelable
 
     public static PlayerController fromParcel(Parcel in) {
         // Странно, но иногда на строке чтения Uri возникает ClassNotFoundException,
         // поэтому вместо Uri храним в парселе строку
         Uri videoUri = Uri.parse(in.readString());
         Uri thumbnailUri = Uri.parse(in.readString());
         TrackInfo trackInfo = in.readParcelable(TrackInfo.class.getClassLoader());
         int state = in.readInt();
         int videoOffset = in.readInt();
         return new PlayerController(videoUri, thumbnailUri, state, videoOffset, trackInfo);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int i) {
         if (mVideoUri == null)
             parcel.writeString(null);
         else
             parcel.writeString(mVideoUri.toString());
         if (mThumbnailUri == null)
             parcel.writeString(null);
         else
             parcel.writeString(mThumbnailUri.toString());
         parcel.writeParcelable(mTrackInfo, i);
         parcel.writeInt(mState);
         parcel.writeInt(mVideoOffset);
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public static final Parcelable.Creator<PlayerController> CREATOR
             = new Parcelable.Creator<PlayerController>() {
         public PlayerController createFromParcel(Parcel in) {
             return PlayerController.fromParcel(in);
         }
 
         public PlayerController[] newArray(int size) {
             return new PlayerController[size];
         }
     };
 
     //
     // Собственные публичные методы
     //
 
     /**
      * Начинает воспроизведение заново.
      *
      * Выключает показ тамнейла, инициализирует видеопоток, обновляет название ролика,
      * стартует воспроизведение
      */
     public void replay() {
         // если проигрывание уже началось, не инициируем его снова.
         if (mState == STATE_STARTING)
             return;
         if (mState!= STATE_COMPLETED)
             throw new IllegalStateException(
                     String.format("Can't change state to Starting from %d", mState));
         setState(STATE_STARTING);
         mVideoOffset = 0;
         mView.toggleThumbnail(false);
         mView.setStreamUri(mStreamUri);
         mView.setVideoTitle(mTrackInfo.getTitle());
         mPlayRequestStage = TOTAL_REQUESTS_NEEDED - 1;
     }
 
     /**
      * Обработка события Fragment.onPause
      *
      * Запоминает текущую секунду видео, останавливает воспроизведение,
      * деинициализирует VideoView
      */
     public void onPause() {
         mVideoOffset = mView.getCurrentOffset();
         if (D) Log.d(LOG_TAG, "onPause: offset = " + String.valueOf(mVideoOffset));
         mView.stopPlayback();
         mView.setStreamUri(null);
     }
 
     /**
      * Обработка события Fragment.onResume
      *
      * Восстанавливает URL видеопотока, название ролика, текущую секунду вопроизведения,
      * запускает воспроизведение.
      */
     public void onResume() {
         if (D) Log.d(LOG_TAG, "onResume: state=" + String.valueOf(mState));
         if (mState == STATE_PLAYING){
             mView.setStreamUri(mStreamUri);
             mView.setVideoTitle(mTrackInfo.getTitle());
             mPlayRequestStage = TOTAL_REQUESTS_NEEDED - 1;
             setState(STATE_STARTING);
         } else {
             // Если проигрывание не готово, необходимо заново начать подготовку к проигрыванию видео
             mView.setLoading();
             mView.toggleThumbnail(true);
             if (mState != STATE_COMPLETED)
                 checkReadyToPlay();
         }
     }
 
     /**
      * Аксессор для загрузчика картинок
      *
      * @return загрузчик картинок, завязанный на локальную очередь запросов
      */
     public ImageLoader getImageLoader() {
         return mImageLoader;
     }
 
     /**
      * Обрабатывает событие окончания воспроизведения видео
      *
      * Включает тамнейл, вызывает у фрагмента обрабочтик onComplete
      */
     public void onCompletion() {
         if (mState!= STATE_PLAYING)
             throw new IllegalStateException(
                     String.format("Can't change state to Completed from %d", mState));
         setState(STATE_COMPLETED);
         if (mView != null) {
             mView.toggleThumbnail(true);
             mView.onComplete();
         }
     }
 
     /**
      * Обрабатывает событие ошибки воспроизведения видео
      */
     public void onPlaybackError() {
         setState(STATE_ERROR);
         if (mView != null) {
             mView.showError(mContext.getString(R.string.video_playback_error));
             mView.toggleThumbnail(true);
         }
     }
 
 
     /**
      * Обработка события инициализации VideoView
      */
     public void onViewReady() {
         if (D) Log.d(LOG_TAG, "Got ViewReady");
         mPlayRequestStage++;
         checkReadyToPlay();
     }
 
     /**
      * Присоединяется к контексту и пользовательскому интерфейсу,
      * инициализирует объекты, зависящие от активити.
      * @param context экземпляр активити
      * @param view фрагмент или активити, реализующие пользовательский интерфейс
      */
     public void attach(Context context, PlayerView view) {
         assert mContext == null;
         assert mView == null;
         mContext = context;
         mView = view;
         mRequestQueue = Volley.newRequestQueue(context,
                 new HttpClientStack(HttpTransport.getHttpClient()));
         mImageLoader = new ImageLoader(mRequestQueue, RutubeApp.getBitmapCache());
         if (mThumbnailUri != null) {
             mView.setThumbnailUri(mThumbnailUri);
         }
         mAttached = true;
         if (mState != STATE_NEW)
             restoreFromState();
     }
 
     /**
      * Отсоединяется от останавливаемой активити
      *
      * Останавливает очередь запросов, зануляет все ссылки на объекты Android.
      */
     public void detach() {
         mAttached = false;
         mRequestQueue.cancelAll(Requests.TRACK_INFO);
         mRequestQueue.cancelAll(Requests.PLAY_OPTIONS);
         mRequestQueue.cancelAll(Requests.YAST_VIEWED);
         mRequestQueue.stop();
         mRequestQueue = null;
         mContext = null;
         mView = null;
     }
 
     /**
      * Разбирает Uri видео, получает ID video и запускает цепочку запросов к API,
      * необходимых для начала проигрывания
      */
     public void requestStream() {
         if (!mAttached)
             throw new NullPointerException("Not attached");
         if (D) Log.d(LOG_TAG, "requestStream() for: " + String.valueOf(mVideoUri));
         mVideo = null;
         if (mVideoUri != null) {
             parseVideoUri();
         }
         if (mVideo != null)
             startPlayRequests(mVideo);
 
     }
 
     /**
      * Возвращает текущее состояние контроллера
      * @return PlayerController.STATE_*
      */
     public int getState() {
         return mState;
     }
 
     /**
      * Осуществляет разбор ссылки на видео с целью получить ID видео
      * и подпись для приватного видео.
      */
     private void parseVideoUri() {
         final List<String> segments = mVideoUri.getPathSegments();
         assert segments != null;
         if (segments.size() == 2 && segments.get(1).matches("[a-f\\d]{32}")) {
             String videoId = segments.get(1);
             mVideo = new Video(videoId);
         } else if (segments.size() == 3 &&
                 (segments.get(1).equals("private") || segments.get(1).equals("embed")) &&
                 segments.get(2).matches("[a-f\\d]{32}")) {
             String videoId = segments.get(2);
             String signature = mVideoUri.getQueryParameter("p");
             mVideo = new Video(videoId, signature);
         } else {
             if (D) Log.d(LOG_TAG, "Uncaught url from intent-filter, starting browser.");
             String packageName = "com.android.browser";
             String className = "com.android.browser.BrowserActivity";
             Intent internetIntent = new Intent(Intent.ACTION_VIEW);
             internetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
             internetIntent.setClassName(packageName, className);
             internetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             internetIntent.setData(mVideoUri);
             mContext.startActivity(internetIntent);
             ((Activity)mContext).finish();
         }
     }
 
     /**
      * Восстанавливает пользовательский интерфейс плеера, в зависимости
      * от состояния контроллера на момент сохранения
      */
     private void restoreFromState() {
         if (D) Log.d(LOG_TAG, "Restoring from state " + String.valueOf(mState));
         switch(mState) {
             case STATE_STARTING:
             case STATE_ERROR:
                 // на момент сохранения запросы еще не были обработаны, запускаем их заново
                 mState = STATE_NEW;
                 requestStream();
                 break;
             case STATE_PLAYING:
                 // На момент сохранения воспроизводилось видео, и есть корректные данные для того,
                 // чтобы восстановить процесс просмотра.
                 // Восстанавливаем название ролика, Uri видеопотока, состояние элементов управления
                 // и текущую секунду воспроизведения.
                 // Запускается показ видео без отправки статистики.
                 mState = STATE_STARTING;
                 mView.setVideoTitle(mTrackInfo.getTitle());
                 mPlayRequestStage = TOTAL_REQUESTS_NEEDED - 1;
                 mView.setStreamUri(mStreamUri);
                 mView.setLoadingCompleted();
                 break;
             case STATE_COMPLETED:
                 // На момент сохранения был показан эндскрин.
                 // Делаем так, чтобы плеер не начал в фоне воспроизводить видео, восстанавливаем
                 // состояние элементов управления,
                 mView.setStreamUri(null);
                 mView.toggleThumbnail(true);
                 mView.stopPlayback();
                 mView.setLoadingCompleted();
                 mView.onComplete();
                 break;
             default:
                 break;
         }
     }
 
     /**
      * Проверяет необходимые условия начала просмотра
      */
     private void checkReadyToPlay() {
         // Для начала воспроизведения необходимо дождаться завершения 3 запросов
         // и вызова onViewReady() - всего 4 стадии.
         if (D) Log.d(LOG_TAG, "Current stage: " + String.valueOf(mPlayRequestStage));
         if (mPlayRequestStage == TOTAL_REQUESTS_NEEDED) {
             startPlayback(true);
         } else
             if (D) Log.d(LOG_TAG, "Not ready yet");
     }
 
     /**
      * Обрабатывает процесс старта воспроизведения: создает запрос к yast.rutube.ru
      * и командует плееру начать просмотр
      */
     private void startPlayback(boolean sendViewed) {
         if (mState!= STATE_STARTING && mState != STATE_ERROR)
             throw new IllegalStateException(String.format("Can't change state to Playing from %d", mState));
         setState(STATE_PLAYING);
        if (!mAttached){
            if(D) Log.d(LOG_TAG, "Oops, not attached");
            setState(STATE_COMPLETED);
            return;
        }
         mView.setLoadingCompleted();
         mView.toggleThumbnail(false);
         if (mVideoOffset > 0)
             mView.seekTo(mVideoOffset);
         mView.startPlayback();
         if (sendViewed) {
             JsonObjectRequest request = mVideo.getYastRequest(mContext);
             mRequestQueue.add(request);
         }
     }
 
     /**
      * Обновляет логическое состояние контроллера с записью в лог
      * @param state STATE_NEW, STATE_STARTING, STATE_PLAYING, STATE_COMPLETED
      */
     private void setState(int state) {
         if (D) Log.d(LOG_TAG, String.format("Changing state: %d to %d", mState, state));
         mState = state;
     }
 
     /**
      * Выполняет цепочку запросов к API rutube необходимых для проигрывания видео.
      *
      * Рассчитывает на то, что на момент вызова метода событие MediaPlayer.onPrepared
      * не было возбуждено.
      * @param video объект видео, которое надо воспроизвести.
      */
     private void startPlayRequests(Video video) {
         if (mState != STATE_NEW && mState != STATE_ERROR)
             throw new IllegalStateException(
                     String.format("can't change state to STARTING from %d", mState));
         mPlayRequestStage = 0;
         mPlaybackAllowed = null;
         mTrackInfo = null;
         JsonObjectRequest request = video.getTrackInfoRequest(mContext, this);
         mRequestQueue.add(request);
         request = video.getPlayOptionsRequest(mContext, this);
         mRequestQueue.add(request);
         setState(STATE_STARTING);
 
     }
 
 
 }
