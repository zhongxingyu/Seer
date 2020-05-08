 package com.halcyonwaves.apps.meinemediathek.services;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.halcyonwaves.apps.meinemediathek.threads.DownloadThreadExecutorThread;
 
 public class BackgroundDownloadService extends Service {
 
 	private static final String TAG = "BackgroundDownloadService";
 
 	@Override
 	public IBinder onBind( final Intent intent ) {
 		return null;
 	}
 
 	@Override
 	public int onStartCommand( final Intent intent, final int flags, final int startid ) {
 
		//
		if( null != intent ) {

			// get the extras into a local variable
			final Bundle suppliedExtras = intent.getExtras();

			if( null != suppliedExtras ) {
				// get some required information to starting the download
				final String episodeTitle = suppliedExtras.getString( "movieTitle" );
				final String downlaodURL = suppliedExtras.getString( "downloadLink" );
 
				// just log the event that we received a download request
				Log.d( BackgroundDownloadService.TAG, "Service started with request to download following URL: " + downlaodURL );
 
				// we have to check if the download was already started and skip the rest if it was
				// TODO: this
 
				// start the thread which starts the executor for starting the download process with a given timeout
				new DownloadThreadExecutorThread( this.getApplicationContext(), downlaodURL, episodeTitle ).start();
			}
 		}
 
 		//
 		return Service.START_STICKY;
 	}
 
 }
