 package com.example.gameapp;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class OptionsMenu extends Activity {
 
 	private String difficulty;
 	private boolean musicState;
 	private TextView diff;
 	private TextView musica;
 	private MediaPlayer optionsMusic;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_options_menu);
 		
 		diff = (TextView) findViewById(R.id.difficulty);
 		musica = (TextView) findViewById(R.id.music);
 		Button easy = (Button) findViewById(R.id.easy_button);
 		Button normal = (Button) findViewById(R.id.normal_button);
 		Button hard = (Button) findViewById(R.id.hard_button);
 		Button save = (Button) findViewById(R.id.save_button);
 		Button on = (Button) findViewById(R.id.on_button);
 		Button off = (Button) findViewById(R.id.off_button);
 
 		save.setOnClickListener(optionsButtonListener);
 		easy.setOnClickListener(optionsButtonListener);
 		normal.setOnClickListener(optionsButtonListener);
 		hard.setOnClickListener(optionsButtonListener);
 		on.setOnClickListener(optionsButtonListener);
 		off.setOnClickListener(optionsButtonListener);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			difficulty = extras.getString("difficulty");
 			musicState = extras.getBoolean("music");
 		}
 
 		diff.setText(getResources().getString(R.string.difficulty_options) + difficulty);
 		musica.setText(getResources().getString(R.string.music_options) + " " + musicState);
 
 	}
 
 	@Override
 	protected void onStart(){
 		super.onStart();
 		startMedia();
 	}
 
 	@Override
 	protected void onStop(){
 		optionsMusic.release();
 		optionsMusic = null;
 		super.onStop();
 	}
 
 	@Override
 	public void finish(){
 		Intent passData = new Intent(OptionsMenu.this, Menu.class);
 		passData.putExtra("difficulty", difficulty);
 		passData.putExtra("music", musicState);
 		setResult(RESULT_OK, passData);
 		super.finish();
 	}
 
 	private OnClickListener optionsButtonListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			switch(v.getId()){
 			case R.id.easy_button:
				difficulty = "Easy";
 				diff.setText(getResources().getString(R.string.difficulty_options) + difficulty);
 				break;
 			case R.id.normal_button:
				difficulty = "Normal";
 				diff.setText(getResources().getString(R.string.difficulty_options) + difficulty);
 				break;
 			case R.id.hard_button:
				difficulty = "Hard";
 				diff.setText(getResources().getString(R.string.difficulty_options) + difficulty);
 				break;
 			case R.id.off_button:
 				if (musicState == true){
 					musicState = false;
 					changeMedia();
 					musica.setText(getResources().getString(R.string.music_options) + " " + musicState);
 				}
 				break;
 			case R.id.on_button:
 				if (musicState == false){
 					musicState = true;
 					changeMedia();
 					musica.setText(getResources().getString(R.string.music_options) + " " + musicState);
 				}
 				break;
 			case R.id.save_button:
 				finish();
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	public void changeMedia(){
 		if (musicState){
 			openMedia();
 		} else {
 			optionsMusic.pause();
 		}
 	}
 
 	public void openMedia(){
 		optionsMusic = MediaPlayer.create(getApplicationContext(), R.raw.options_music);
 		optionsMusic.start();
 		optionsMusic.setLooping(true);
 	}
 
 	public void closeMedia(){
 		optionsMusic.release();
 		optionsMusic = null;
 	}
 
 	public void startMedia(){
 		if (musicState){
 			openMedia();
 		} 
 	}
 
 }
