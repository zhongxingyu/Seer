 // Copyright 2012 Michael Marner (michael@20papercups.net) all rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification, are
 // permitted provided that the following conditions are met:
 //
 //    1. Redistributions of source code must retain the above copyright notice, this list of
 //       conditions and the following disclaimer.
 //
 //    2. Redistributions in binary form must reproduce the above copyright notice, this list
 //       of conditions and the following disclaimer in the documentation and/or other materials
 //       provided with the distribution.
 //
 // THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY EXPRESS OR IMPLIED
 // WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 // FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 // CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 // ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 // NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 // The views and conclusions contained in the software and documentation are those of the
 // authors and should not be interpreted as representing official policies, either expressed
 // or implied, of the authors.
 
 
 package com.threedradio.player;
 
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 
 /**
  * The main activity for the Three D application, shows the user interface, etc.
  * 
  * @author Michael Marner (michael@20papercups.net)
  *
  */
 public class MainActivity extends Activity {
 
 	/**
 	 * Our service that is responsible for actually playing the music.
 	 */
 	ThreeDService service;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         /*
          * Because this activity could have been stopped and recreated, while
          * the service playing the music continues to do it's thing, we will
          * attempt to reconnect to the service when this activity is created,
          * creating the service if it does not exist.
          */
 		Intent i = new Intent(this, ThreeDService.class);
 		i.setAction(ThreeDService.ACTION_PLAY);
 		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
     }
 
     
     /**
      * Eventually, we'll have an options menu. For now, this doesn't do anything!
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     
     /**
      * The service connection handles reconnecting and disconnecting 
      * from the playback service.
      */
     private ServiceConnection mConnection = new ServiceConnection() {
 		
     	/**
     	 * If we disconnect from the service, we simply invalidate our
     	 * service handle.
     	 */
 		public void onServiceDisconnected(ComponentName name) {
 			service = null;
 		}
 		
 		/**
 		 * On connecting to the service, we check the playback state, and set
 		 * the icon on the play/pause button accordingly.
 		 */
 		public void onServiceConnected(ComponentName name, IBinder s) {
 			service = ((ThreeDService.LocalBinder)s).getService();
 			if (service.isPlaying()) {
 				((ImageButton) findViewById(R.id.playpause)).setImageResource(android.R.drawable.ic_media_pause);
 			}
 			Log.d("ThreeD", "Connected!");
 		}
 	};
     
 	
 	/**
 	 * Handler for the Play/pause button.
 	 * Toggles the music stream and sets the icon on the button accordingly.
 	 * 
 	 * @param v The button that triggered this event. Should certainly by the play/pause button!
 	 */
 	public void playButtonClick(View v) {
 		Log.v("ThreeD", "PlayButtonClicked");
 		
 		if (service.isPlaying()) {
 			service.stop();
 			((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
 		}
 		else {
 			Intent i = new Intent(MainActivity.this, ThreeDService.class);
 			i.setAction(ThreeDService.ACTION_PLAY);
 			startService(i);
 			((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
 		}
 	}
 }
