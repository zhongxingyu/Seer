 package org.geekhub.vkPlayer;
 
 import android.app.Application;
 import android.app.Service;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.IBinder;
 import android.util.Log;
 import com.perm.kate.api.Audio;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 public class PlayerService extends Service {
 
     final String LOG_T = "PlayerService";
     public final static String ACTION_TAG = "Action";
     public final static int ACTION_IDLE = 0;
     public final static int ACTION_PLAY = 1;
     public final static int ACTION_PAUSE = 2;
     public final static int ACTION_STOP = 3;
     
     final String LOG_TAG = "myLogs";
 
     public static PlayerService INSTANCE;
 
     private MediaPlayer player = new MediaPlayer();
    private int currentSong;
     private ArrayList<Audio> playlist;
 
     public void onCreate() {
     	Log.d(LOG_TAG, "--- PlayerService - onCreate() --- ");
         super.onCreate();
         INSTANCE = this;
         player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mediaPlayer) {
                 if(playlist != null){
                     next();
                 }
             }
         });
         Log.d(LOG_T, "PLayer created");
         
     }
 
     public int onStartCommand(Intent intent, int flags, int startId) {
     	Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- flags = " + flags + "startId" + startId);
 
     	Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- intent = " + intent);
     	
     	
     	int action = intent.getIntExtra(ACTION_TAG, 0);
         Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- action = " + action);
 
         switch (action){
             case ACTION_IDLE : {
                 break;
             }
             case ACTION_PLAY : {
             	Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- action = ACTION_PLAY");
                 play(0);
                 break;
             }
             case ACTION_PAUSE : {
             	Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- action = ACTION_PAUSE");
                 pause();
                 break;
             }
             case ACTION_STOP : {
             	Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- action = ACTION_STOP");
                 stop();
                 break;
             }
             default: {
 
             }
         }
         Log.d(LOG_TAG, "--- PlayerService - onStartCommand() --- return -- flags = " + flags + "-- startId = " + startId);
         return super.onStartCommand(intent, flags, startId);
     }
 
     public void onDestroy() {
     	Log.d(LOG_TAG, "--- PlayerService - onDestroy() --- ");
         INSTANCE = null;
         player.release();
         super.onDestroy();
         Log.d(LOG_TAG, "--- PlayerService - onDestroy() --- Player destroyed ");
         Log.d(LOG_T, "Player destroyed");
     }
 
     public IBinder onBind(Intent intent) {
     	Log.d(LOG_TAG, "--- PlayerService - onBind(intent) --- return NULL");
         return null;
     }
 
     public void loadPlaylist(ArrayList<com.perm.kate.api.Audio> collection){
         playlist = new ArrayList<Audio>(collection);
     }
 
     public void play(){
 
         if(player.isPlaying()){
             player.reset();
         }
 
         try{
             if(playlist != null){
                 Log.d(LOG_TAG, "--- PlayerService - play(a) --- (currentSong != null)");
                 player.setDataSource(new org.geekhub.vkPlayer.utils.Audio(playlist.get(currentSong)).getDataSource(getApplicationContext()));
             } else {
                 Log.d(LOG_TAG, "--- PlayerService - play(a) --- (audio != null) - else - RETURN");
                 return;
             }
 
             player.prepare();
             player.start();
         } catch (IOException e){
             Log.d(LOG_TAG, "--- PlayerService - play(a) --- (audio != null) - (IOException e)");
             Log.e(LOG_T, "PLayer IOException");
         }
 
     }
 
     public void play(int i){
     	Log.d(LOG_TAG, "--- PlayerService - play(a) --- ");
 
         if(player.isPlaying()){
         	Log.d(LOG_TAG, "--- PlayerService - play(a) --- (player.isPlaying())");
             player.reset();
         }
 
         Audio audio = null;
         if(playlist != null){
             audio = playlist.get(i);
             currentSong = i;
         }
 
         try{
             if(audio != null){
             	Log.d(LOG_TAG, "--- PlayerService - play(a) --- (audio != null)");
                 player.setDataSource(new org.geekhub.vkPlayer.utils.Audio(audio).getDataSource(getApplicationContext()));
             } else if( playlist != null ){
             	Log.d(LOG_TAG, "--- PlayerService - play(a) --- (currentSong != null)");
                 player.setDataSource(new org.geekhub.vkPlayer.utils.Audio(playlist.get(currentSong)).getDataSource(getApplicationContext()));
             } else {
             	Log.d(LOG_TAG, "--- PlayerService - play(a) --- (audio != null) - else - RETURN");
                 return;
             }
 
             player.prepare();
             player.start();
         } catch (IOException e){
         	Log.d(LOG_TAG, "--- PlayerService - play(a) --- (audio != null) - (IOException e)");
             Log.e(LOG_T, "PLayer IOException");
         }
 
 
     }
 
     public void next(){
         currentSong = (currentSong+1)%playlist.size();
     }
 
     public void prev(){
         currentSong--;
         if(currentSong < 0){
             currentSong = playlist.size()-1;
         }
     }
 
     public void pause(){
     	Log.d(LOG_TAG, "--- PlayerService - pause() --- ");
         if(player.isPlaying()){
         	Log.d(LOG_TAG, "--- PlayerService - pause() ---(player.isPlaying()) ");
             player.pause();
         }
     }
 
     public void stop(){
     	Log.d(LOG_TAG, "--- PlayerService - stop() ---");
         player.stop();
     }
 }
