 import android.media.MediaPlayer;
 
 import com.mandala.Exception.MediaPlayerProxyException;
 
 import java.util.ArrayList;
 import java.util.List;
 
/**
  * 播放语音代理类
  * 理论上这个类可以做下载，先获得文件后，再播放本地文件，减少流量使用
  * 不做了
  * Created by W_Q on 13-10-14.
  */
 public class MediaPlayerProxy {
     private MediaPlayer player;
     private static MediaPlayerProxy proxy = new MediaPlayerProxy();
     private List<PlayState> list; //单纯的播放动画集合
     private PlayState mState; //正在播放的
     private String mPlayPath;
     private String TAG = getClass().getSimpleName();
     private PlayLengthListener playLengthListener; //播放时长监听器
     private boolean isNew; //用于注销时长监听
     private Handler handler = new Handler(){
         @Override
         public void handleMessage(Message msg) {
             if (MediaPlayerProxy.this.playLengthListener != null) {
                 MediaPlayerProxy.this.playLengthListener.currentLength(player.getCurrentPosition());
                 sendEmptyMessageDelayed(0,1000);
             }else{
                 return;
             }
 
         }
     };
 
     private MediaPlayerProxy(){
         player = new MediaPlayer();
         list = new ArrayList<PlayState>();
         player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
             @Override
             public void onPrepared(MediaPlayer mediaPlayer) {
                 player.start(); //准备结束后，开始播放
                 if (mState!=null){
                     mState.play();
                     MLog.e(TAG,"mState.play()");
                 }
                 if (playLengthListener!=null){
                     handler.sendEmptyMessageDelayed(0,1000);
                 }
             }
         });
 
         player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mediaPlayer) {
                 player.stop();//播放结束
                 player.reset();
                 mPlayPath = null;
                 playLengthListener = null;
                 if (mState!=null){
                     mState.stop();
                     mState.setState(PlayState.NORMAL);
                 }
             }
         });
 
     }
 
     /**
      * 获得播放代理
      * @return
      */
     public static MediaPlayerProxy getProxy(){
         return proxy;
     }
 
     /**
      * 仅仅在布局复用的时候调用
      * @param mState
      * @param listener
      */
     public void setPlayState(PlayState mState,PlayLengthListener listener){
         this.mState = mState;
         this.playLengthListener = listener;
     }
 
     /**
      * 带有时长监听的播放
      * @param state
      * @param listener
      * @param path
      * @return
      */
     public boolean play(PlayState state,PlayLengthListener listener,String path){
         this.playLengthListener = listener;
         isNew = true;
         return play(state, path);
     }
 
 
     /**
      * 播放(停止)语音
      * @param state 播放对象接口
      * @param path 播放路径
      */
     public boolean play(PlayState state,String path){
         this.mState = state;
         mPlayPath = path;
         PlayState current = null;
         if (isNew){
             isNew = false;
         }else {
             playLengthListener = null;
         }
         for (PlayState s:list){
             if (s.getState() == PlayState.PLAY){
                 current = s;
                 break;
             }
         }
 
         if (current!=null){
             stop();
             current.stop();
             current.setState(PlayState.NORMAL);//停止后，再
             if (current.equals(state))//再次点击还是自己，说明是想停止播放
                 return true;
         }
 
         boolean err = false;
         try {
             play(path); //实际播放
         } catch (MediaPlayerProxyException e) {
             e.printStackTrace();
             err = true;
         }finally {
             if (err){
                 //播放失败，调用停止播放的接口
                 state.setState(PlayState.NORMAL);
                 state.stop();
                 return false;
             }else{
                 //设置播放状态
 //                list.add(state);
                 state.setState(PlayState.PLAY);
                 return true;
             }
         }
     }
 
     /**
      * 实际播放的方法
      * @param path
      */
     private void play(String path) throws MediaPlayerProxyException {
         boolean err = false;
         try {
             if (path == null||path.equals("")){
                 throw new MediaPlayerProxyException("路径有问题");
             }
             MLog.i(TAG,"想要播放的音频路径"+path);
             player.setDataSource(path);
         } catch (Exception e) {
             err = true;
             //e.printStackTrace();
         }finally {
             if (err){
                 player.reset();
                 try {
                     player.setDataSource(path);
                 } catch (Exception e) {
                     //e.printStackTrace();
                     throw new MediaPlayerProxyException("路径有问题");
                 }
             }
         }
         player.prepareAsync(); //异步的准备
     }
 
     /**
      * 这个路径是否正在播放
      * @param path
      * @return
      */
     public boolean isPlay(String path){
         if (mState == null || mPlayPath == null || !mPlayPath.equals(path+"")){
             return false;
         }
         if (mPlayPath.equals(path+"")){
 //            return mState.getState() == PlayState.PLAY;
             return true;
         }
         return false;
     }
 
     /**
      * 实际的停止方法
      */
     private void stop(){
         player.reset();
     }
 
     /**
      * 强制停止方法，用于界面跳转，或特殊场景
      */
     public void forceStop(){
         PlayState mState = null;
         for (PlayState s:list){
             if (s.getState() == PlayState.PLAY){
                 mState = s;
                 break;
             }
         }
 
         if (mState!=null){
 //            强制停止实现
             stop();
             mState.stop();
             mPlayPath = null;
             playLengthListener = null;
             mState.setState(PlayState.NORMAL);
         }
     }
 
     /**
      * 添加后才可以播放,需要避免重复添加
      * @param state
      */
     public void add(PlayState state){
         if (!list.contains(state))
             list.add(state);
     }
 
     public void clear(){
         list.clear();
     }
 
     public interface PlayState{
         /**
          * 播放
          */
         public static final int PLAY = 1;
         /**
          * 未播放
          */
         public static final int NORMAL = 0;
 
         /**
          * 这个方法为了停止动画
          */
         void stop();
 
         /**
          * 这个方法为了开启动画
          */
         void play();
 
         /**
          * 获取当前item的播放状态
          * @return
          */
         int getState();
 
         /**
          * 设置播放状态
          * @param state
          */
         void setState(int state);
     }
 
     public interface PlayLengthListener{
         /**
          * 当前播放到的时长
          * @param time
          */
         void currentLength(int time);
     }
     
     public List<PlayState> getList(){
     	return this.list;
     }
 }
