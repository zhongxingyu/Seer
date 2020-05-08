 package com.github.tommywalsh.mcotp.android;
 
 import com.github.tommywalsh.mcotp.shared.*;
 
 import android.app.Activity;
 import android.os.Bundle;
 import java.util.Vector;
 import android.widget.TextView;
 import android.widget.Button;
 import android.view.View;
 
 public class AndroidUI extends Activity
 {
     private AndroidPlayer m_player;
     private SongProvider m_songProvider;
     private TextView m_bandText;
     private TextView m_albumText;
     private TextView m_songText;
     private Button m_playPauseButton;
     private Button m_bandLockButton;
     private Button m_albumLockButton;
     private Button m_shuffleButton;
     private Engine m_engine;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 	setContentView(R.layout.main);
 	snagUIElements();
 
 	initializeInternals();
 
 	startPlaying();
     }
 
     private void snagUIElements() {
 	m_songText  = (TextView) findViewById(R.id.songText);
 	m_bandText  = (TextView) findViewById(R.id.bandText);
 	m_albumText = (TextView) findViewById(R.id.albumText);
 
 	m_playPauseButton = (Button) findViewById(R.id.playPauseButton);
 	m_playPauseButton.setOnClickListener(new View.OnClickListener() {
 		public void onClick(View v) {
 		    m_engine.togglePlayPause();
 		    if (m_engine.isPlaying()) {
 			m_playPauseButton.setText(R.string.pause);
 		    } else {
 			m_playPauseButton.setText(R.string.play);
 		    }
 		}
 	    });
 
 	Button skipButton = (Button) findViewById(R.id.skipButton);
 	skipButton.setOnClickListener(new View.OnClickListener() {
 		public void onClick(View v) {
 		    m_engine.nextSong();
 		}
 	    });
 	
 	m_bandLockButton = (Button) findViewById(R.id.bandLockButton);
 	m_bandLockButton.setText(R.string.lock);
 	m_bandLockButton.setOnClickListener(new View.OnClickListener() {
 		public void onClick(View v) {
		    if (m_bandLockButton.getText().equals(R.string.lock)) {
			m_engine.setClamp(null, m_engine.getSong().bandName(), null);
			m_bandLockButton.setText(R.string.unlock);
			m_albumLockButton.setText(R.string.lock);
		    } else {
 			m_engine.setClamp(null, null, null);
 			m_bandLockButton.setText(R.string.lock);
 			m_albumLockButton.setText(R.string.lock);
 		    }
 		}
 	    });
 	
 	m_albumLockButton = (Button) findViewById(R.id.albumLockButton);
 	m_albumLockButton.setText(R.string.lock);
 	m_albumLockButton.setOnClickListener(new View.OnClickListener() {
 		public void onClick(View v) {
		    if (m_albumLockButton.getText().equals(R.string.lock)) {
 			m_engine.setClamp(null, m_engine.getSong().bandName(), 
 					  m_engine.getSong().albumName());
 			m_bandLockButton.setText(R.string.lock);
 			m_albumLockButton.setText(R.string.unlock);
		    } else {
			m_engine.setClamp(null, null, null);
			m_bandLockButton.setText(R.string.lock);
			m_albumLockButton.setText(R.string.lock);
 		    }
 		}
 	    });
 	
 
 	m_shuffleButton = (Button) findViewById(R.id.shuffleButton);
 	m_shuffleButton.setText(R.string.sequential);
 	m_shuffleButton.setOnClickListener(new View.OnClickListener() {
 		public void onClick(View v) {
 		    m_engine.toggleRandom();
 		    if (m_engine.isRandom()) {
 			m_shuffleButton.setText(R.string.sequential);
 		    } else {
 			m_shuffleButton.setText(R.string.random);
 		    }
 		}
 	    });
 	
 
     }
     
     private void initializeInternals() {
 	PosixStorageProvider psp = new PosixStorageProvider("/sdcard/music");
 	m_songProvider = new SongProvider(psp);
 	m_songProvider.constructLibrary();
 
 	m_player = new AndroidPlayer(psp);
 	m_engine = new Engine(m_songProvider, m_player);
 
 	m_engine.addListener(new Engine.UpdateListener() {
 		public void onSongChanged(Song newSong) {
 		    updateSongDisplay(newSong);
 		}
 	    });
     }
 
     // should instead restore state from previous session!
     private void startPlaying() {
 	// should be controlled by UI
 	m_engine.toggleRandom();
 	m_engine.nextSong();
 	
 	m_engine.togglePlayPause();
 
 	updateSongDisplay(m_engine.getSong());
     }
 
     private void updateSongDisplay(Song song) {
 	m_bandText.setText(song.bandName());
 	m_albumText.setText(song.albumName());
 	m_songText.setText(song.songName());
     }
 }
