 package com.group15.djhero;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class PlaySongPage extends Activity implements OnSeekBarChangeListener {
 	ImageButton imageButton;
 	private SeekBar bar; // declare seekbar object variable
 	Timer pb_timer = new Timer();
 	UpdateSongProgress pb_task;
 	MyApplication myApp;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play_song_page);
 		
 		 myApp = (MyApplication) PlaySongPage.this.getApplication();
 		 myApp.imageView = (ImageView) findViewById(R.id.imageView1);
 		 myApp.textViewforSongPosition = (TextView) findViewById(R.id.songName);
 		 myApp.textViewforSongArtist = (TextView) findViewById(R.id.artistName);
 		 imageButton = (ImageButton) findViewById(R.id.imageButton1);
 		 bar = (SeekBar) findViewById(R.id.seekBar0); // make seekbar object
 		 myApp.songProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
 		 myApp.timeLeft = (TextView) findViewById(R.id.textView1);
 		 
 		String songName = getIntent().getStringExtra("songName");
 		myApp.textViewforSongPosition.setText(songName);
 		
 		if(!myApp.timeFlag){
 			myApp.positionOfSong = getIntent().getIntExtra("position", 0);
 			SendMessage.sendMessage("p " + myApp.songlist.Songs.get(myApp.positionOfSong).id, myApp.sock);
 		}
 		if(!myApp.playButton) 
 			imageButton.setImageResource((R.drawable.pause));
 		else 
 			imageButton.setImageResource((R.drawable.play));
 		
 		
 		if(myApp.positionOfSong != getIntent().getIntExtra("position", 0))
 		{
 			myApp.positionOfSong = getIntent().getIntExtra("position", 0);
 			SendMessage.sendMessage("p " + myApp.songlist.Songs.get(myApp.positionOfSong).id, myApp.sock);
 			imageButton.setImageResource((R.drawable.pause));
 			myApp.playButton = false;
 			myApp.progressTracker = 0;
 		}
 		myApp.lengthOfCurrentSong = (myApp.songlist.Songs.get(myApp.positionOfSong).Length) / 1000;
		myApp.textViewforSongPosition.setText(myApp.songlist.Songs.get(myApp.positionOfSong).artist);
 		
 		if (myApp.images[myApp.positionOfSong] != null) {
 			myApp.imageView.setImageBitmap(myApp.images[myApp.positionOfSong]);
 		}
 		
 		bar.setOnSeekBarChangeListener(this); // set Seekbar listener.
 		bar.setProgress(myApp.Global_progress); // default value for volume Seekbar
 
 		myApp.songProgressBar.setProgress(myApp.progressTracker);
 		myApp.songProgressBar.setMax(myApp.lengthOfCurrentSong);
 		
 		myApp.timeLeft.setTextColor(Color.WHITE);
 		myApp.timeLeft.setText(String.valueOf((myApp.lengthOfCurrentSong - myApp.progressTracker+1)/60)+":"+String.format("%02d", Integer.valueOf(((myApp.lengthOfCurrentSong - myApp.progressTracker+1)%60))));
 
 		// Set up a timer task. We will use the timer to check the
 		// to update the song progress bar
 		if(!myApp.timeFlag){
 			pb_task = new UpdateSongProgress();
 			pb_timer.schedule(pb_task, 1750, 1000);
 			myApp.timeFlag = true;
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.play_song_page, menu);
 		return true;
 	}
 
 	public void PausePlay(View view) {
 		MyApplication myApp = (MyApplication) PlaySongPage.this
 				.getApplication();
 		imageButton = (ImageButton) findViewById(R.id.imageButton1);
 
 		if (myApp.playButton == false) {
 			SendMessage.sendMessage("x ", myApp.sock);
 			imageButton.setImageResource((R.drawable.play));
 			myApp.playButton = true;
 		} else {
 			SendMessage.sendMessage("r ", myApp.sock);
 			imageButton.setImageResource((R.drawable.pause));
 			myApp.playButton = false;
 		}
 	}
 
 	public void songNext(View view) {
 		MyApplication myApp = (MyApplication) PlaySongPage.this
 				.getApplication();
 		imageButton.setImageResource((R.drawable.pause));
 		myApp.positionOfSong = (++myApp.positionOfSong) % myApp.songlist.Songs.size();
 		Log.i("position", Integer.toString(myApp.positionOfSong));
 		Song thisSong = myApp.songlist.Songs.get(myApp.positionOfSong);
 
 		TextView textViewforSongPosition = (TextView) findViewById(R.id.songName);
 		textViewforSongPosition.setText(thisSong.Title);
		myApp.textViewforSongPosition.setText(myApp.songlist.Songs.get(myApp.positionOfSong).artist);
 
 		myApp.progressTracker = 0;
 		myApp.lengthOfCurrentSong = (myApp.songlist.Songs.get(myApp.positionOfSong).Length) / 1000;
 		
 		try {
 			myApp.imageView.setImageBitmap(myApp.images[myApp.positionOfSong]);
 		} catch (NullPointerException e) {
 			myApp.imageView.setImageResource(R.drawable.defaultsong);
 		} catch (IndexOutOfBoundsException e) {
 		}
 		SendMessage.sendMessage("p "+ String.valueOf((myApp.songlist.Songs
 								.get(myApp.positionOfSong).id)), myApp.sock);
 
 	}
 
 	public void songPrevious(View view) {
 		MyApplication myApp = (MyApplication) PlaySongPage.this
 				.getApplication();
 		imageButton.setImageResource((R.drawable.pause));
 		Log.i("positionBefore", Integer.toString(myApp.positionOfSong));
 		if (myApp.positionOfSong > 0)
 			myApp.positionOfSong = myApp.positionOfSong - 1;
 		Log.i("positionAfter", Integer.toString(myApp.positionOfSong));
 
 		Song thisSong = myApp.songlist.Songs.get(myApp.positionOfSong);
		myApp.textViewforSongPosition.setText(myApp.songlist.Songs.get(myApp.positionOfSong).artist);
 
 		TextView textViewforSongPosition = (TextView) findViewById(R.id.songName);
 		textViewforSongPosition.setText(thisSong.Title);
 
 		myApp.progressTracker = 0;
 		myApp.lengthOfCurrentSong = (myApp.songlist.Songs.get(myApp.positionOfSong).Length) / 1000;
 
 		try {
 			myApp.imageView.setImageBitmap(myApp.images[myApp.positionOfSong]);
 		} catch (NullPointerException e) {
 			myApp.imageView.setImageResource(R.drawable.defaultsong);
 		} catch (IndexOutOfBoundsException e) {
 		}
 		SendMessage.sendMessage("p "+ String.valueOf((myApp.songlist.Songs.get(myApp.positionOfSong).id)), myApp.sock);
 	}
 
 	@Override
 	// Update the volume progress as the user changes it
 	public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
 		if ((progress % 10) < 5) {
 			progress = (progress / 10) * 10;
 		} else {
 			progress = ((progress / 10) * 10) + 10;
 		}
 		myApp.Global_progress = progress;
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar arg0) {
 		MyApplication myApp = (MyApplication) PlaySongPage.this.getApplication();
 
 		// Send desired volume to the DE2
 		SendMessage.sendMessage("volume " + Integer.toString((myApp.Global_progress / 10) * 10),myApp.sock);
 		Log.i("VolumeTag", Integer.toString((myApp.Global_progress / 10) * 10));
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 	}
 
 	public class UpdateSongProgress extends TimerTask {
 		public void run() {
 			System.out.println("in timer");
 			if (myApp.lengthOfCurrentSong +1 >= myApp.progressTracker) {
 				if (!myApp.playButton){
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {	
 							myApp.timeLeft.setText(String.valueOf((myApp.lengthOfCurrentSong - myApp.progressTracker+1)/60)+":"+String.format("%02d", Integer.valueOf(((myApp.lengthOfCurrentSong - myApp.progressTracker+2)%60))));
 							
 						}
 					});
 					myApp.songProgressBar.setProgress(myApp.progressTracker++);
 				}
 				
 				
 			} else {
 				myApp.positionOfSong = (++myApp.positionOfSong)% myApp.songlist.Songs.size();
 				SendMessage.sendMessage("p "+ String.valueOf((myApp.songlist.Songs.get(myApp.positionOfSong).id)), myApp.sock);
 				
 				try {
 					Thread.sleep(1750);
 				} catch (InterruptedException e) {}
 		
 				myApp.progressTracker = 0;
 				myApp.songProgressBar.setProgress(myApp.progressTracker);
 				myApp.lengthOfCurrentSong = (myApp.songlist.Songs.get(myApp.positionOfSong).Length) / 1000;
 				myApp.songProgressBar.setMax(myApp.lengthOfCurrentSong);
 				
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						
 						myApp.textViewforSongPosition.setText(myApp.songlist.Songs.get(myApp.positionOfSong).Title);
 						myApp.imageView.setImageBitmap(myApp.images[myApp.positionOfSong]);
 					}
 				});
 				
 			}
 
 		}
 	}	
 }
