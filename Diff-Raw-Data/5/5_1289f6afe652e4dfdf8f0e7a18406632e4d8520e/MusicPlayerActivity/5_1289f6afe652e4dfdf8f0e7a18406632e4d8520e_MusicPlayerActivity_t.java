 package com.szilard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.Toast;
 
 public class MusicPlayerActivity extends Activity implements OnClickListener {
 
 	private Button playButton;
 	private Button stopButton;
 	private Button nextButton;
 	private Button prevButton;
 	
 	//some constants:
 	private final int SELECTION_REQUEST = 1;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.main);
 		
 		//setting the private Button variables
 		playButton = (Button) findViewById(R.id.play);
 		stopButton = (Button) findViewById(R.id.stop);
 		prevButton = (Button) findViewById(R.id.prev);
 		nextButton = (Button) findViewById(R.id.next);
 		
 		//setting the OnClickListener to each of them by letting the class implement 
 		//this interface
 		playButton.setOnClickListener(this);
 		stopButton.setOnClickListener(this);
 		nextButton.setOnClickListener(this);
 		prevButton.setOnClickListener(this);
 
 		
 	}
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {		
 		
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.main_menu, menu);
 		
 		return true;
 		
 	}
 
 
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		
 		switch(featureId)
 		{
 		case R.id.list: 
 		//I have to start a new activity where the user will select the song which 
 		//is to be played
		startListActivity();
 		break;
 		
 		case R.id.exit:
 		break;
 			
 		
 		}
 		
 		return true;
 	}
 
 
 
	private void startListActivity() {
 		
 		Intent listIntent;
 		
 		listIntent = new Intent();
 		listIntent.setClass(this, MusicSelection.class);
 		
 		startActivityForResult(listIntent, SELECTION_REQUEST);
 	}
 	
 	
 
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 	}
 
 
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 }
