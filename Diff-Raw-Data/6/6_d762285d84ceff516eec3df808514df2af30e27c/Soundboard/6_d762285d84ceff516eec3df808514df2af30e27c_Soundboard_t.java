 package com.soundboard.dmx;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 
 public class Soundboard extends Activity {
 
 	MediaPlayer mp = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_soundboard);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// getMenuInflater().inflate(R.menu.activity_soundboard, menu);
 		return true;
 	}
 
 	public void playSound(View v) {
 		// determine mp3 to play and play it
		if(mp != null && mp.isPlaying()){
			return;
		}
 		switch (v.getId()) {
 		case (R.id.allUpDog): {
 			mp = MediaPlayer.create(this, R.raw.all_up_dog);
 			break;
 		}
 		case (R.id.dmx): {
 			mp = MediaPlayer.create(this, R.raw.dmx);
 			break;
 		}
 		case (R.id.dog): {
 			mp = MediaPlayer.create(this, R.raw.dog);
 			break;
 		}
 		case (R.id.hereWeGoAgain): {
 			mp = MediaPlayer.create(this, R.raw.here_we_go_again);
 			break;
 		}
 		case (R.id.heyYo): {
 			mp = MediaPlayer.create(this, R.raw.hey_yo);
 			break;
 		}
 		case (R.id.mmm): {
 			mp = MediaPlayer.create(this, R.raw.mmm);
 			break;
 		}
 		case (R.id.not_a_game): {
 			mp = MediaPlayer.create(this, R.raw.not_a_game);
 			break;
 		}
 		case (R.id.ride_or_die): {
 			mp = MediaPlayer.create(this, R.raw.ride_or_die);
 			break;
 		}
 		case (R.id.ruff_ryders_you_know): {
 			mp = MediaPlayer.create(this, R.raw.ruff_ryders_you_know);
 			break;
 		}
 		case (R.id.stop_talking_quick): {
 			mp = MediaPlayer.create(this, R.raw.stop_talking_quick);
 			break;
 		}
 		case (R.id.streets): {
 			mp = MediaPlayer.create(this, R.raw.streets);
 			break;
 		}
 		case (R.id.throw_it_up): {
 			mp = MediaPlayer.create(this, R.raw.throw_it_up);
 			break;
 
 		}
 		case (R.id.what_we_do_baby): {
 			mp = MediaPlayer.create(this, R.raw.what_we_do_baby);
 			break;
 
 		}
 		case (R.id.yeah): {
 			mp = MediaPlayer.create(this, R.raw.yeah);
 			break;
 		}
 		default:
 			break;
 		}
 
 		mp.start();
 	}
 }
