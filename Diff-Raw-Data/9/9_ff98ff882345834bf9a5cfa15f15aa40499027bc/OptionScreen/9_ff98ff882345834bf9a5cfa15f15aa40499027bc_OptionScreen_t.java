 package edu.chl.asciicam.activity;
 
 //Copyright 2012 Robin Braaf, Ossian Madisson, Martin Thrnesson, Fredrik Hansson and Jonas strm.
 //
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 import java.util.Arrays;
 import java.util.List;
 
 import edu.chl.asciicam.util.SettingsController;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * 
  * @author Martin
  *
  */
 public class OptionScreen extends Activity {
 
 	private Button back_btn, reset_btn, apply_btn;
 	private SeekBar brightnessBar, densityBar;
 	private Spinner filterSpinner, bgSpinner, charSpinner;
 	private List<String> filterList, colorList;
 	private String[] colorStrings, filterStrings;
 	private CharSequence promptArray[];
 	private ArrayAdapter<String> filterAdapter, colorAdapter;
 	private float brightness;
 	private TextView bg_head, char_head, colors_head, brightness_value, density_value;
 	private int bgPos, textPos, filterPos, density;
 	protected static SettingsController settings = new SettingsController();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		//Set fullscreen, must be before setContent!
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);	
 
 		setContentView(R.layout.activity_option_screen);
 		
 		//Views
 		bg_head = (TextView) findViewById(R.id.background_head);
 		char_head = (TextView) findViewById(R.id.character_head);
 		colors_head = (TextView) findViewById(R.id.colors_head);
 		brightness_value = (TextView) findViewById(R.id.brightness_value);
 		density_value = (TextView) findViewById(R.id.density_value);	
 
 		//Widgets
 		back_btn = (Button) findViewById(R.id.back);
 		reset_btn = (Button) findViewById(R.id.reset);
 		apply_btn = (Button) findViewById(R.id.apply);
 
 		filterSpinner = (Spinner) findViewById(R.id.filter_spinner);
 		bgSpinner = (Spinner) findViewById(R.id.background_spinner);
 		charSpinner = (Spinner) findViewById(R.id.character_spinner);
 
 		//Initiating objects in OptionScreen
 		initiateViews();
 		initiateButtons();
 		initiateSpinners();
 		initiateSeekBars();
 
 	}
 
 	// This is called to initiate the views that adjusted by java
 	private void initiateViews(){
 		//Default brightness and density values
 		brightness_value.setText(""+settings.getBrightness());
 		density_value.setText(""+settings.getCompression());
 	}
 
 	//	This is called to initiate the buttons and add functionality
 	private void initiateButtons() {
 		back_btn.setOnClickListener(new View.OnClickListener() {
 
 			/* Using the finish() to go back to the last activity */
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		
 		reset_btn.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				settings.resetToDefault();
 				initiateViews();
 				initiateSpinners();
 				initiateSeekBars();
 			}
 		});
 		
 		apply_btn.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				applySettings();
 				finish();
 			}
 		});
 
 	}	
 
 	//This is called to initiate the Seekbars
 	private void initiateSeekBars(){
 		
 		//Give brightness and density the current values, even if the bar is not changed
 		brightness = settings.getBrightness();
 		density = settings.getCompression();
 		
 		// nitiating Brightnessbar		
 		brightnessBar = (SeekBar)findViewById(R.id.brightness_bar);
 		brightnessBar.setMax(200);
 		//Set the brightnessbar according to the current brightnessvalue, default is 100 (middle of the bar)
 		brightnessBar.setProgress((int) brightness+100);
 		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			//Not used
 			public void onStopTrackingTouch(SeekBar arg0) {
 			}
 			//Not used
 			public void onStartTrackingTouch(SeekBar arg0) {
 			}		  
 
 			//Listener for brightnessBar
 			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
 				//Brightnessvalue can vary between -100 and 100
 				brightness = progress-100;
 				brightness_value.setText(""+brightness);
 			}
 		});
 
 		//Initiating the Densitybar
 		densityBar = (SeekBar)findViewById(R.id.density_bar);
 		densityBar.setMax(15);
 		//Set the densitybar according to the current densityvalue, default is 6
 		densityBar.setProgress(density-5); 
 		densityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			//Not used
 			public void onStopTrackingTouch(SeekBar arg0) {
 			}
 			//Not used
 			public void onStartTrackingTouch(SeekBar arg0) {
 			}		  
 
 			//Listener for densityBar
 			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
 				//densityvalue can vary between 5 and 20
 				
 				density = progress+5;				
 				density_value.setText(""+density);
 			}
 		});
 	}
 
 	//This is called to initiate the Spinners
 	private void initiateSpinners(){
 
 		//Setting up an array for Spinner Prompts
 		promptArray = new String[3];
 		promptArray[0] = "Choose Filter";
 		promptArray[1] = "Choose Background Color";
 		promptArray[2] = "Choose Character Color";
 
 		filterSpinner.setPrompt(promptArray[0]);
 		bgSpinner.setPrompt(promptArray[1]);
 		charSpinner.setPrompt(promptArray[2]);
 
 		//Creating lists of Spinner Entries
 		filterStrings = new String[] {"AsciiFilter","GrayscaleFilter"};
 		colorStrings = new String[] {"BLACK","WHITE","GRAY","CYAN","RED","BLUE","GREEN","MAGENTA","YELLOW"};
 		filterList = Arrays.asList(filterStrings);
 		colorList = Arrays.asList(colorStrings);
 
 		//Creating layout for spinner entries
 		filterAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, filterList);
 		filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		colorAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, colorList);
 		colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
 
 		//Setting up Entries
 		filterSpinner.setAdapter(filterAdapter);		
 		bgSpinner.setAdapter(colorAdapter);
 		charSpinner.setAdapter(colorAdapter);
 
 		//Defining value for default entry to each spinner
 		//And set the last settings used if returning to optionscreen again
 		filterSpinner.setSelection(settings.getFilterPos());
 		bgSpinner.setSelection(settings.getBgPos());
 		charSpinner.setSelection(settings.getTextPos());
 
 		//Defining visibility for spinners and textviews
 		bgSpinner.setVisibility(View.VISIBLE);
 		bg_head.setVisibility(View.VISIBLE);
 		charSpinner.setVisibility(View.VISIBLE);
 		char_head.setVisibility(View.VISIBLE);
 		colors_head.setVisibility(View.VISIBLE);		
 
 		//Listener for filterSpinner
 		filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			//Called when choosing an item from filterSpinner
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				filterPos = filterSpinner.getSelectedItemPosition();
 				String filter = filterList.get(filterPos);
 				
 
 				if(filter.equals("GrayscaleFilter")){
 					bgSpinner.setVisibility(View.INVISIBLE);
 					bg_head.setVisibility(View.INVISIBLE);
 					charSpinner.setVisibility(View.INVISIBLE);
 					char_head.setVisibility(View.INVISIBLE);
 					colors_head.setVisibility(View.INVISIBLE);
 				}	
 				else{
 					bgSpinner.setVisibility(View.VISIBLE);
 					bg_head.setVisibility(View.VISIBLE);
 					charSpinner.setVisibility(View.VISIBLE);
 					char_head.setVisibility(View.VISIBLE);
 					colors_head.setVisibility(View.VISIBLE);
 				}
 
 			}
 
 			//Not used
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 
 		//Listener for backgroundSpinner
 		bgSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			//Called when choosing an item from bgSpinner
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				bgPos = bgSpinner.getSelectedItemPosition();
 			}
 
 			//Not used
 			public void onNothingSelected(AdapterView<?> arg0) {
 				
 			}
 
 		});
 
 		//Listener for characterSpinner
 		charSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			//Called when choosing an item from charSpinner
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				textPos = charSpinner.getSelectedItemPosition();				
 			}
 
 			//Not used
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 
 		});
 	}
 
 	//Called to apply the changes chosen with the widgets to the settingscontroller
 	private void applySettings(){
 		settings.setBgColor(bgPos);
 		settings.setTextColor(textPos);
 		settings.setFilter(filterPos); 
 		settings.setBrightness(brightness);
 		settings.setCompression(density);
 	}
 	
 	/**
 	 * Retrieving settings from OptionScreen
 	 * @return the current settings
 	 */
 	public static SettingsController getSettings(){
 		return settings;
 	}
 	
 }
