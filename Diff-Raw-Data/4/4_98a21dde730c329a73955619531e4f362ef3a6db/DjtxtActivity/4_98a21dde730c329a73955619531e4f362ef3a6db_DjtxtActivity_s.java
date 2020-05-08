 package com.milone.djtxtme;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.TextView;
 
 public class DjtxtActivity extends Activity implements OnClickListener {
 	ArrayAdapter<String> spinnerArrayAdapter;
 	TextView text;
 	String selected;
 	SharedPreferences prefs;
 
 	String songstring = "";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// XML Variables
 		Spinner list = (Spinner) findViewById(R.id.spinner1);
 		Button bSend = (Button) findViewById(R.id.button1);
 		text = (TextView) findViewById(R.id.textView1);
 
 		// Add basic commands at top of list
 		ArrayList<String> songs = new ArrayList<String>();
 		songs.add("[blank]");
 		songs.add("[skip]");
 		songs.add("[undo]");
 
 		// Load text file and add line by line to the arraylist
 		try {
 			File sdcard = Environment.getExternalStorageDirectory();
 			File file = new File(sdcard, "djtxt.me.txt");
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line;
 			while ((line = br.readLine()) != null) {
 				songs.add(line);
 			}
 			br.close();
 		} catch (IOException e) {
 		}
 
 		// Add the clear log option at the bottom
 		songs.add("[clear log text below]");
 
 		// Put the arraylist into the Spinner
 		spinnerArrayAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_dropdown_item, songs);
 		list.setAdapter(spinnerArrayAdapter);
 		list.setOnItemSelectedListener(new MyOnItemSelectedListenerSongs());
 
 		// Allow teh user to click the button
 		bSend.setOnClickListener(this);
 	}
 
 	// String variable selected contains the choosen song or command
 	public class MyOnItemSelectedListenerSongs implements
 			OnItemSelectedListener {
 
 		@Override
 		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			selected = spinnerArrayAdapter.getItem((arg0
 					.getSelectedItemPosition()));
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		// Get the current Time, add a zero if minutes is single digit
 		// which is why minutes is a string
 		String minutes;
 		Calendar rightNow = Calendar.getInstance();
 		int hours = rightNow.get(Calendar.HOUR);
 		int int_minutes = rightNow.get(Calendar.MINUTE);
 		if (int_minutes < 10)
 			minutes = "0" + String.valueOf(int_minutes);
 		else
 			minutes = String.valueOf(int_minutes);
 
 		// Start an email intent
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "dj@djtxt.me");
 		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 				"DJtxt-AndroidApp");
 		emailIntent.setType("plain/text");
 
 		// If Undo is selected, make the body of the email "oops" command
 		if (selected == "[undo]")
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "oops");
 
 		// If Skip is selected, make the body of the email "skip" command
 		// Also Skip will get added to the log and also mark the time
 		// you can use Skip again, as it's once every 20 minutes per djtxt
 		// settings
 		else if (selected == "[skip]") {
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "skip");
 		
 			// Adding 20 minutes to the current time.
 			int int_nextskipmin = int_minutes + 20;
 			int nextskiphour = hours;
 			String nextskipmin;
 			if (int_nextskipmin >= 60) {
 				int_nextskipmin -= 60;
 				nextskiphour++;
 			}
 			if (int_nextskipmin < 10)
 				nextskipmin = "0" + String.valueOf(int_nextskipmin);
 			else
 				nextskipmin = String.valueOf(int_nextskipmin);
 
 			// songstring string is the log
 			songstring = "\n" + hours + ":" + minutes
 					+ ") SKIP sent [next avail  " + nextskiphour + ":"
 					+ nextskipmin + "]" + songstring;
 		
 			
 		// Load an email with a blank body if blank was choosen, the other
 		// fields will be filled out
 		} else if (selected == "[blank]")
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
 		
 		// if clear was called, wipe out the log, overwrite the saved log too
 		else if (selected == "[clear log text below]") {
 			songstring = "";
 			prefs = getPreferences(MODE_PRIVATE);
 			SharedPreferences.Editor editor = prefs.edit();
 			editor.putString("songs", songstring);
 			editor.commit();
 		}
 
 		// If none of the other options were called, then the user picked a song
 		// from their text file. selected is that song, add it to the body/log
 		else {
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, selected);
 			songstring = "\n" + hours + ":" + minutes + ") " + selected
 					+ songstring;
 		}
 		// Update the display with the log of this current button click
 		text.setText(songstring);
 
 		// Only call email if the command sent wasn't a clear
 		if (selected != "[clear log text below]")
 			startActivity(emailIntent);
 
 	}
 
 	// OnResume and OnPause are used to save and reload the log.
 	@Override
 	public void onResume() {
 		super.onResume();
 		prefs = getPreferences(MODE_PRIVATE);
 		songstring = prefs.getString("songs", "");
 		text.setText(songstring);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		prefs = getPreferences(MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString("songs", songstring);
 		editor.commit();
 	}
 
 }
