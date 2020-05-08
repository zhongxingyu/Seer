 package com.melchor629.musicote;
 
 import java.io.IOException;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Build;
 import android.os.IBinder;
 import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Reproductor del Musicote 0.1
  * TODO Mejorar con nuevas cosas el servicio inlcuyendo una interfaz gráfica y Last.FM
  * TODO Averiguar si en versiones antiguas funciona la notificación
  * TODO Cambiar el icono por uno mejor y con tamaños para que el Lint no se queje xD
  * @author melchor
  * http://developer.android.com/guide/topics/media/mediaplayer.html
  */
 public class Reproductor extends Service implements MediaPlayer.OnPreparedListener {
 
 	MediaPlayer reproductor = new MediaPlayer();
 	
 	public int onStartCommand (Intent intent, int flags, int StartID){
 		Toast.makeText(this, "Reproductor de musicote abierto", Toast.LENGTH_LONG).show();
 		String url = intent.getStringExtra("archivo");
 		String tit = intent.getStringExtra("titulo");
 		String art = intent.getStringExtra("artista");
 		initMediaPlayer(url, tit, art);
 		return START_STICKY;
 	}
 	
 	@SuppressWarnings("deprecation")
 	@SuppressLint("NewApi")
 	public void initMediaPlayer(String url, String titulo, String artista){
         reproductor = new MediaPlayer(); // initialize it here
         reproductor.setAudioStreamType(AudioManager.STREAM_MUSIC);
         try {
 			reproductor.setDataSource(url);
 		} catch (IllegalArgumentException e) {
 			Log.e("Reproductor.Descarga","Error: "+ e.toString());
 		} catch (SecurityException e) {
 			Log.e("Reproductor.Descarga","Error: "+ e.toString());
 		} catch (IllegalStateException e) {
 			Log.e("Reproductor.Descarga","Error: "+ e.toString());
 		} catch (IOException e) {
 			Log.e("Reproductor.Descarga","Error: "+ e.toString());
 		}
         reproductor.setOnPreparedListener(this);
         reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
         reproductor.prepareAsync(); // prepare async to not block main thread
         PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                 new Intent(getApplicationContext(), MainActivity.class),
                 PendingIntent.FLAG_UPDATE_CURRENT);
         Notification notification = null;
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
         	notification = new Notification();
         	notification.tickerText = titulo+" - "+artista;
         	notification.icon = R.drawable.altavoz;
         	notification.flags |= Notification.FLAG_ONGOING_EVENT;
         	notification.setLatestEventInfo(getApplicationContext(), "Musicote",
                 "Playing: " + titulo+" - "+artista, pi);
         }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	notification = new NotificationCompat.Builder(this)
     			.setContentTitle("Musicote")
     			.setContentText("Reproduciendo: "+titulo+" - "+artista)
     			.setSmallIcon(R.drawable.altavoz)
     			.build();
         }
     	startForeground(1, notification);
 	}
 	
     /**
      * Se llama cuando el reproductor está listo
      */
     public void onPrepared(MediaPlayer player) {
         player.start();
     }
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
    	@Override
    	public void onDestroy() {
    		if (reproductor != null) reproductor.release();
    		Toast.makeText(this, "Reproductor de musicote cerrado", Toast.LENGTH_LONG).show();
     }
 }
