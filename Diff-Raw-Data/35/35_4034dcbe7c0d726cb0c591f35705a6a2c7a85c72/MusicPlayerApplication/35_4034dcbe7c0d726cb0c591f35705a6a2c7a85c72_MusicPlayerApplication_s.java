 package com.example.musicplayer;
 
 import android.app.Application;
 import android.content.Intent;
 import com.example.musicplayer.db.MusicPlayerDAO;
 import com.example.musicplayer.db.MusicPlayerDBHelper;
 import com.example.musicplayer.handler.MainHandler;
 import com.example.musicplayer.message.MessagePump;
 import com.example.musicplayer.pojo.Song;
 import com.example.musicplayer.service.MusicPlayerService;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: neevek
  * Date: 7/20/13
  * Time: 1:20 PM
  */
 public class MusicPlayerApplication extends Application {
     private static MusicPlayerApplication mInstance;
 
     private MainHandler mMainHandler;
     private MusicPlayerDAO mMusicPlayerDAO;
     private MessagePump mMessagePump;
 
     public final static String SHARED_PREF = MusicPlayerApplication.class.getSimpleName();
 
     public final static String PREF_KEY_LAST_PLAYED_SONG_ID = "last_played_song_id";
     public final static String PREF_KEY_LAST_PLAYED_SONG_PROGRESS = "last_played_song_progress";
 
     private List<Song> mCachedSongList;
     private List<Song> mCurrentPlayList;
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         mInstance = this;
 
         mMainHandler = new MainHandler();
 
         mMusicPlayerDAO = new MusicPlayerDAO(new MusicPlayerDBHelper(this));
 
         mMessagePump = new MessagePump();
     }
 
     public static MusicPlayerApplication getInstance () {
         return mInstance;
     }
 
     public MainHandler getMainHandler () {
         return mMainHandler;
     }
 
     public MusicPlayerDAO getMusicPlayerDAO () {
         return mMusicPlayerDAO;
     }
 
     public MessagePump getMessagePump () {
         return mMessagePump;
     }
 
     public void setCurrentPlayList (List<Song> songList) {
         mCurrentPlayList = songList;
     }
 
     public List<Song> getCurrentPlayList () {
         return mCurrentPlayList;
     }
 
     public final static Object INIT_CACHED_SONG_LIST_SYNC = new Object();
     public List<Song> getCachedAllMusicSongList(boolean init) {
         if (init) {
             synchronized (INIT_CACHED_SONG_LIST_SYNC) {
                 if (mCachedSongList == null) {
                     mCachedSongList = mMusicPlayerDAO.getAllSongs();
                 }
             }
         }
 
         return mCachedSongList;
     }
 
     public void startPlayingSong (int songId, int progress) {
         Intent intent = new Intent(this, MusicPlayerService.class);
         intent.putExtra("songId", songId);
         intent.putExtra("progress", progress);
         startService(intent);
     }
 
     public void runOnUiThread (Runnable action) {
         mMainHandler.sendAction(action);
     }
 }
