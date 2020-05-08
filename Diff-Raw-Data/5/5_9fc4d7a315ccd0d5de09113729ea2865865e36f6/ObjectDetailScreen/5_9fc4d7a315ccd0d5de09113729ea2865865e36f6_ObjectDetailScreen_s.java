 /*
  * Copyright (c) 2012 Joe Rowley
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.mobileobservinglog;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Set;
 import java.util.TreeMap;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Typeface;
 import android.location.Location;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.MarginLayoutParams;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.mobileobservinglog.SearchScreen.IndividualFilter;
 import com.mobileobservinglog.SearchScreen.IndividualFilterWrapper;
 import com.mobileobservinglog.softkeyboard.SoftKeyboard;
 import com.mobileobservinglog.softkeyboard.SoftKeyboard.TargetInputType;
 import com.mobileobservinglog.strategies.DatePicker;
 import com.mobileobservinglog.strategies.NumberPickerDriver;
 import com.mobileobservinglog.strategies.TimePicker;
 import com.mobileobservinglog.support.GpsUtility;
 import com.mobileobservinglog.support.SettingsContainer;
 import com.mobileobservinglog.support.SettingsContainer.SessionMode;
 import com.mobileobservinglog.support.database.CatalogsDAO;
 import com.mobileobservinglog.support.database.EquipmentDAO;
 import com.mobileobservinglog.support.database.LocationsDAO;
 import com.mobileobservinglog.support.database.ObservableObjectDAO;
 import com.mobileobservinglog.support.database.TargetListsDAO;
 
 public class ObjectDetailScreen extends ActivityBase{
 
 	//gather resources
 	FrameLayout body;
 	
 	FrameLayout keyboardRoot;
 	SoftKeyboard keyboardDriver;
 	
 	int firstFocus; //used to control the keyboard showing on first load
 	int firstClick;
 	
 	boolean intentEdit;
 	
 	//ObjectData
 	int id;
 	String objectName;
 	String commonName;
 	String type;
 	String magnitude;
 	String size;
 	String distance;
 	String constellation;
 	String season;
 	String rightAscension;
 	String declination;
 	String catalogDescription;
 	String catalog;
 	String otherCats;
 	String imagePath;
 	String nightImagePath;
 	boolean logged;
 	String logDate;
 	String logTime;
 	String logLocation;
 	String equipment;
 	int seeing;
 	int transparency;
 	boolean favorite;
 	//String findingMethod;
 	String viewingNotes;
 	
 	TreeMap<String, Integer> telescopes;
 	TreeMap<String, Integer> eyepieces;
 	int newEyepieceId;
 	int newTelescopeId;
 	String selectedLocation;
 	
 	boolean imageZoomed;
 	
 	//LayoutElements
 	TextView headerText;
 	TextView commonNameDisplay;
 	TextView otherCatalogsDisplay;
 	ImageView chart;
 	ImageView favoriteStar;
 	TextView rightAscDisplay;
 	TextView decDisplay;
 	TextView magDisplay;
 	TextView sizeDisplay;
 	TextView typeDisplay;
 	TextView distanceDisplay;
 	TextView constellationDisplay;
 	TextView seasonDisplay;
 	LinearLayout descriptionLayout;
 	TextView descriptionDisplay;
 	
 	TextView dateDisplay;
 	EditText dateInput;
 	TextView timeDisplay;
 	EditText timeInput;
 	TextView locationDisplay;
 	EditText locationInput;
 	TextView equipmentDisplay;
 	EditText equipmentInput;
 	TextView seeingDisplay;
 	EditText seeingInput;
 	TextView transDisplay;
 	EditText transInput;
 	TextView notesDisplay;
 	EditText notesInput;
 	
 	Button addToList;
 	Button saveButton;
 	Button clearButton;
 	
 	TextView modalHeader;
 	LinearLayout modalButtonContainer;
 	Button modalSave;
 	Button modalCancel;
 	Button modalClear;
 	LinearLayout modalSelectorsLayout;
 	LinearLayout modalSelectorSetOne;
 	LinearLayout modalSelectorSetTwo;
 	LinearLayout modalSelectorSetThree;
 	Button modalSelectorOneUpButton;
 	Button modalSelectorOneDownButton;
 	Button modalSelectorTwoUpButton;
 	Button modalSelectorTwoDownButton;
 	Button modalSelectorThreeUpButton;
 	Button modalSelectorThreeDownButton;
 	TextView modalSelectorTextOne;
 	TextView modalSelectorTextTwo;
 	TextView modalSelectorTextThree;
 	LinearLayout modalListOneContainer;
 	ListView modalListOne;
 	TextView modalListHeaderOne;
 	LinearLayout modalListTwoContainer;
 	ListView modalListTwo;
 	TextView modalListHeaderTwo;
 	
 	DatePicker datePicker;
 	TimePicker timePicker;
 	NumberPickerDriver numPickerOne;
 	NumberPickerDriver numPickerTwo;
 	NumberPickerDriver numPickerThree;
 	
 	GpsUtility gpsHelper;
 	Location locationFromDevice;
 	
 	TreeMap<String, Boolean> targetLists; 
 	ArrayList<String> originalLists;
 	
 	@Override
     public void onCreate(Bundle icicle) {
 		Log.d("JoeDebug", "ObjectDetails onCreate. Current session mode is " + settingsRef.getSessionMode());
         super.onCreate(icicle);
 
         customizeBrightness.setDimButtons(settingsRef.getButtonBrightness());
         gpsHelper = new GpsUtility(getApplicationContext());
 		
 		firstFocus = -1;
         firstClick = 1;
 		
         //setup the layout
         setContentView(settingsRef.getObjectDetailLayout());
         body = (FrameLayout)findViewById(R.id.object_detail_root); 
 	}
 	
 	@Override
     public void onPause() {
         super.onPause();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     //When we resume, we need to make sure we have the right layout set, in case the user has changed the session mode.
     @Override
     public void onResume() {
 		Log.d("JoeDebug", "ObjectDetails onResume. Current session mode is " + settingsRef.getSessionMode());
         super.onResume();
         firstFocus = -1;
         firstClick = 1;
         setLayout();
     }
 	
   //Used by the Toggle Mode menu item method in ActivityBase. Reset the layout and force the redraw
 	@Override
 	public void setLayout(){
 		setContentView(settingsRef.getObjectDetailLayout());
 		super.setLayout();
 		
 		gpsHelper.setUpLocationService();
         
         setObjectData();
         findDisplayElements();
         setUpListButtonAndFavorite();
         populateInfoDisplayElements();
         
         if(!logged || intentEdit) {
         	setEditableMode();
         } else {
         	setDisplayMode();
         }
          
         findModalElements();
         imageZoomed = false;
 
 		setMargins_noKeyboard();
 		body.postInvalidate();
 	}
 	
 	private void setObjectData() {
 		//Gather data on object
         objectName = getIntent().getStringExtra("com.mobileobservationlog.objectName");
         
         String intentEditString = getIntent().getStringExtra("com.mobileobservationlog.editIntent");
         if(intentEditString != null && intentEditString.equals("true")) {
         	intentEdit = true;
         } else {
         	intentEdit = false;
         }
         
         ObservableObjectDAO db = new ObservableObjectDAO(this);
         Cursor objectInfo = db.getObjectData(objectName);
         objectInfo.moveToFirst();
 
     	id = objectInfo.getInt(0);
     	commonName = objectInfo.getString(2);
     	type = objectInfo.getString(3);
     	magnitude = objectInfo.getString(4);
     	size = objectInfo.getString(5);
     	distance = objectInfo.getString(6);
     	constellation = objectInfo.getString(7);
     	season = objectInfo.getString(8);
     	rightAscension = objectInfo.getString(9);
     	declination = objectInfo.getString(10);
     	catalogDescription = objectInfo.getString(11);
     	catalog = objectInfo.getString(12);
     	otherCats = objectInfo.getString(13);
     	imagePath = objectInfo.getString(14);
     	nightImagePath = objectInfo.getString(15);
     	String loggedString = objectInfo.getString(16);
     	if(loggedString != null) {
     		logged = loggedString.toLowerCase().equals("true");
     	} else {
     		logged = false;
     	}
     	logDate = objectInfo.getString(17);
     	logTime = objectInfo.getString(18);
     	logLocation = objectInfo.getString(19);
     	equipment = objectInfo.getString(20);
     	seeing = objectInfo.getInt(21);
     	transparency = objectInfo.getInt(22);
     	String favoriteString = objectInfo.getString(23);
     	if(favoriteString != null) {
     		favorite = favoriteString.toLowerCase().equals("true");
     	} else {
     		favorite = false;
     	}
     	//findingMethod = objectInfo.getString(24);
     	viewingNotes = objectInfo.getString(25);
     	
     	objectInfo.close();
     	db.close();
 	}
 	
 	private String formatEquipmentString(int telescopeId, int eyepieceId) {
 		String retVal = "";
 		EquipmentDAO db = new EquipmentDAO(this);
 		Cursor equip = db.getSavedTelescope(telescopeId);
 		String telescopeType = "";
 		String telescopeDiam = "";
 		String telescopeRatio = "";
 		String telescopeLength = "";
 		if(equip.getCount() > 0) {
 			equip.moveToFirst();
 			telescopeType = equip.getString(1);
 			telescopeDiam = equip.getString(2);
 			telescopeRatio = equip.getString(3);
 			telescopeLength = equip.getString(4);
 		}
 		equip.close();
 
 		String eyepieceLength = "";
 		String eyepieceType = "";
 		equip = db.getSavedEyepiece(eyepieceId);
 		if(equip.getCount() > 0) {
 			equip.moveToFirst();
 			eyepieceLength = equip.getString(1);
 			eyepieceType = equip.getString(2);
 		}
 		equip.close();
 		db.close();
 			
 		float telLengthInMm = 0;
 		float eyeLengthInMm = 0;
 		int magnification = 0;
 		if(telescopeLength != null && eyepieceLength != null) {
 			try {
 				if(telescopeLength.contains("in")) {
 					telLengthInMm = Float.parseFloat(telescopeLength.split(" in")[0]) * 25.4f;
 				} else if(telescopeLength.contains("mm")) {
 					telLengthInMm = Float.parseFloat(telescopeLength.split(" mm")[0]);
 				}
 				if(eyepieceLength.contains("in")) {
 					eyeLengthInMm = Float.parseFloat(eyepieceLength.split(" in")[0]) * 25.4f;
 				} else if(eyepieceLength.contains("mm")) {
 					eyeLengthInMm = Float.parseFloat(eyepieceLength.split(" mm")[0]);
 				}
 			} catch (NumberFormatException e) {
 				//Do nothing with it. We'll just have some garbage data
 			} catch (NullPointerException e) {
 				
 			} catch (IndexOutOfBoundsException e) {
 				
 			}
 				
 			if(telLengthInMm > 0 && eyeLengthInMm > 0) {
 				magnification = (int) (telLengthInMm / eyeLengthInMm);
 			}
 			
 			String telescopeDescription = formatTelescopeDescription(telescopeType, telescopeRatio, telescopeDiam, telescopeLength);
 			String eyepieceDescription = formatEyepieceDescription(eyepieceType, eyepieceLength);
 			
 			if(!telescopeDescription.equals("(No Telescope Selected)") && !eyepieceDescription.equals("(No Eyepiece Selected)")) {
 				retVal = String.format("%s Telescope with %s Eyepiece", telescopeDescription, eyepieceDescription);
 				if(magnification > 0) {
 					retVal = retVal.concat(String.format(" - %dx Magnification", magnification));
 				}
 			}
 		}
 		return retVal;
 	}
 	
 	private String formatTelescopeDescription(String type, String ratio, String diameter, String length) {
 		String retVal = "";
 		if(diameter != null && diameter.length() > 0) {
 			if(diameter.split(" ")[0].length() > 0) {
 				retVal = retVal.concat(diameter);
 			}
 		}
 		if(ratio != null && ratio.length() > 0) {
 			if(retVal.length() > 0){
 				retVal = retVal.concat(" ");
 			}
 			retVal = retVal.concat("f/" + ratio);
 		}
 		if(length != null && length.length() > 0) {
 			if(length.split(" ")[0].length() > 0) {
 				if(retVal.length() > 0){
 					retVal = retVal.concat(" ");
 				}
 				retVal = retVal.concat("FL: " + length);
 			}
 		}
 		if(type != null && type.length() > 0) {
 			if(retVal.length() > 0){
 				retVal = retVal.concat(" ");
 			}
 			retVal = retVal.concat(type);
 		}
 		if(retVal.length() < 1) {
 			retVal = "(No Telescope Selected)";
 		}
 		return retVal;
 	}
 	
 	private String formatEyepieceDescription(String type, String length) {
 		String retVal = "";
 		if(length != null && length.length() > 0) {
 			if(length.split(" ")[0].length() > 0) {
 				retVal = retVal.concat(length);
 			}
 		}
 		if(type != null && type.length() > 0) {
 			if(retVal.length() > 0) {
 				retVal = retVal.concat(" ");
 			}
 			retVal = retVal.concat(type);
 		}
 		if(retVal.length() < 1) {
 			retVal = "(No Eyepiece Selected)";
 		}
 		return retVal;
 	}
 	
 	//finds all the display text views, the input edit texts, the images and the regular layout buttons. It also adds a listener to the Add To List 
 	//button. It does not add listeners to the save and clear buttons because their behavior will change based on state. These listeners are handled
 	//in the methods to set "mode" on this screen, along with the buttons' text.
 	//Also, modal elements are not handled here
 	private void findDisplayElements() {
 		headerText = (TextView)findViewById(R.id.object_detail_header);
 		commonNameDisplay = (TextView)findViewById(R.id.common_name_data);
 		otherCatalogsDisplay = (TextView)findViewById(R.id.other_catalogs_data);
 		chart = (ImageView)findViewById(R.id.star_chart);
 		favoriteStar = (ImageView)findViewById(R.id.fav_star);
 		rightAscDisplay = (TextView)findViewById(R.id.ra_data);
 		decDisplay = (TextView)findViewById(R.id.dec_data);
 		magDisplay = (TextView)findViewById(R.id.mag_data);
 		sizeDisplay = (TextView)findViewById(R.id.size_data);
 		typeDisplay = (TextView)findViewById(R.id.type_data);
 		distanceDisplay = (TextView)findViewById(R.id.dist_data);
 		constellationDisplay = (TextView)findViewById(R.id.const_data);
 		seasonDisplay = (TextView)findViewById(R.id.season_data);
 		descriptionLayout = (LinearLayout)findViewById(R.id.object_description_layout);
 		descriptionDisplay = (TextView)findViewById(R.id.desc_data);
 		
 		dateDisplay = (TextView)findViewById(R.id.date_data);
 		dateInput = (EditText)findViewById(R.id.date_input);
 		timeDisplay = (TextView)findViewById(R.id.time_data);
 		timeInput = (EditText)findViewById(R.id.time_input);
 		locationDisplay = (TextView)findViewById(R.id.location_data);
 		locationInput = (EditText)findViewById(R.id.location_input);
 		equipmentDisplay = (TextView)findViewById(R.id.equipment_data);
 		equipmentInput = (EditText)findViewById(R.id.equipment_input);
 		seeingDisplay = (TextView)findViewById(R.id.seeing_data);
 		seeingInput = (EditText)findViewById(R.id.seeing_input);
 		transDisplay = (TextView)findViewById(R.id.trans_data);
 		transInput = (EditText)findViewById(R.id.trans_input);
 		notesDisplay = (TextView)findViewById(R.id.notes_data);
 		notesInput = (EditText)findViewById(R.id.notes_input);
 	}
 	
 	private void setUpLogEditElements() {
 		dateInput.setInputType(InputType.TYPE_NULL);
 		dateInput.setOnClickListener(dateModal);
 		
 		timeInput.setInputType(InputType.TYPE_NULL);
 		timeInput.setOnClickListener(timeModal);
 		
 		locationInput.setInputType(InputType.TYPE_NULL);
 		locationInput.setOnClickListener(locationModal);
 		locationInput.setOnFocusChangeListener(showLetters_focus_LoseFocusOnly);
 		
 		equipmentInput.setInputType(InputType.TYPE_NULL);
 		equipmentInput.setOnClickListener(equipmentModal);
 		equipmentInput.setOnFocusChangeListener(showLetters_focus_LoseFocusOnly);
 		
 		seeingInput.setInputType(InputType.TYPE_NULL);
 		seeingInput.setOnClickListener(seeingModal);
 		
 		transInput.setInputType(InputType.TYPE_NULL);
 		transInput.setOnClickListener(transpModal);
 		
 		notesInput.setInputType(InputType.TYPE_NULL);
 		notesInput.setOnFocusChangeListener(showLetters_focus);
 		notesInput.setOnClickListener(showLetters_click);
 	}
 	
 	private void setUpListButtonAndFavorite() {
 		chart.setOnClickListener(zoomOnChart);
 		
 		addToList = (Button)findViewById(R.id.add_to_list_button);
 		addToList.setOnClickListener(listsModal);
 		
 		favoriteStar.setOnClickListener(changeFavorite);
 	}
 	
 	private void setUpSaveCancelButtonsEditable() {
 		saveButton = (Button)findViewById(R.id.save_edit_log_button);
 		saveButton.setText(R.string.save_log);
 		saveButton.setOnClickListener(saveLog);
 		
 		clearButton = (Button)findViewById(R.id.cancel_button);
 		clearButton.setText(R.string.clear_log);
 		clearButton.setOnClickListener(clearLogData);
 	}
 	
 	private void setUpSaveCancelButtonsDisplay() {
 		saveButton = (Button)findViewById(R.id.save_edit_log_button);
 		saveButton.setText(R.string.edit_log);
 		saveButton.setOnClickListener(editLog);
 		
 		clearButton = (Button)findViewById(R.id.cancel_button);
 		clearButton.setText(R.string.delete_log);
 		clearButton.setOnClickListener(deleteLogData);
 	}
 	
 	private void populateInfoDisplayElements() {
 		if(headerText != null && !headerText.equals("NULL")) {
 			headerText.setText(String.format("%s: %s", catalog, objectName));
 		}
 		
 		if(commonName != null && !commonName.equals("NULL")) {
 			commonNameDisplay.setText(commonName);
 			commonNameDisplay.setVisibility(View.VISIBLE);
 			TextView commonNameLabel = (TextView)findViewById(R.id.common_name_label);
 			commonNameLabel.setVisibility(View.VISIBLE);
 		}
 		if(otherCats != null && !otherCats.equals("NULL")) {
 			otherCatalogsDisplay.setText(otherCats);
 			otherCatalogsDisplay.setVisibility(View.VISIBLE);
 			TextView otherCatalogsLabel = (TextView)findViewById(R.id.other_catalogs_label);
 			otherCatalogsLabel.setVisibility(View.VISIBLE);
 		}
 		
 		setStarChartImage();
 		
 		if(favorite) {
 			favoriteStar.setImageResource(settingsRef.getFavoriteStar());
 		} else {
 			favoriteStar.setImageResource(settingsRef.getNotFavoriteStar());
 		}
 		
 		
 		if(rightAscension != null && !rightAscension.equals("NULL")) {
 			rightAscDisplay.setText(rightAscension);
 		}
 		if(declination != null && !declination.equals("NULL")) {
 			decDisplay.setText(declination);
 		}
 		if(magnitude != null && !magnitude.equals("NULL")) {
 			magDisplay.setText(magnitude);
 		}
 		if(size != null && !size.equals("NULL")) {
 			sizeDisplay.setText(size);
 		}
 		if(type != null && !type.equals("NULL")) {
 			typeDisplay.setText(type);
 		}
 		if(distance != null && !distance.equals("NULL")) {
 			distanceDisplay.setText(distance);
 		}
 		if(constellation != null && !constellation.equals("NULL")) {
 			constellationDisplay.setText(constellation);
 		}
 		if(season != null && !season.equals("NULL")) {
 			seasonDisplay.setText(season);
 		}
 		if(catalogDescription != null && !catalogDescription.equals("NULL")) {
 			descriptionDisplay.setText(catalogDescription);
 		}
 	}
 	
 	private void setStarChartImage() {
 		String fileLocationString = settingsRef.getPersistentSetting(SettingsContainer.STAR_CHART_DIRECTORY, ObjectDetailScreen.this);
 		Log.i("SetChart", "File Location String: " + fileLocationString);
 		File starChartRoot = null;
 		
 		//Now actually get the file location
 		if (fileLocationString.equals(SettingsContainer.EXTERNAL)){
 			starChartRoot = getExternalFilesDir(null);
 		}
 		else{
 			starChartRoot = getFilesDir();
 		}
 		
 		//Build up the path to the actual file
 		File chartPath = null;
 		if(settingsRef.getSessionMode().equals(SettingsContainer.SessionMode.normal)){
 			chartPath = new File(starChartRoot.toString() + imagePath);
 		} else {
 			chartPath = new File(starChartRoot.toString() + nightImagePath);
 		}
 		
 		Bitmap image = null;
 		if (chartPath.exists()){
 			image = BitmapFactory.decodeFile(chartPath.toString());
 		}
 		if(image != null) {
 			chart.setImageBitmap(image);
 		} else {
 			chart.setImageResource(settingsRef.getDefaultChartImage());
 		}
 	}
 	
 	private void populateLogDisplayElements() {
 		if(logDate != null && !logDate.equals("NULL")) {
 			dateDisplay.setText(logDate);
 		}
 		if(logTime != null && !logTime.equals("NULL")) {
 			timeDisplay.setText(logTime);
 		}
 		if(logLocation != null && !logLocation.equals("NULL")) {
 			locationDisplay.setText(logLocation);
 		}
 		if(equipment != null && equipment.length() > 0) {
 			equipmentDisplay.setText(equipment);
 		}
 		if(seeing > 0) {
 			seeingDisplay.setText(String.format("%d/%d", seeing, 5));
 		}
 		if(transparency > 0) {
 			transDisplay.setText(String.format("%d/%d", transparency, 5));
 		}
 		if(viewingNotes != null && !viewingNotes.equals("NULL")) {
 			notesDisplay.setText(viewingNotes);
 		}
 	}
 	
 	private void populateLogEditElements() {
 		if(logDate != null && !logDate.equals("NULL")) {
 			dateInput.setText(logDate);
 		} else {
 			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
 			dateInput.setText(sdf.format(Calendar.getInstance().getTime()));
 		}
 		if(logTime != null && !logTime.equals("NULL")) {
 			timeInput.setText(logTime);
 		} else {
 			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
 			timeInput.setText(sdf.format(Calendar.getInstance().getTime()));
 		}
 		if(logLocation != null && !logLocation.equals("NULL") && !logLocation.equals("")) {
 			locationInput.setText(logLocation);
 		} else {
 			if(settingsRef.getPersistentSetting(SettingsContainer.USE_GPS, this).equals(getString(R.string.use_gps_yes))) {
 				locationFromDevice = gpsHelper.getLocation();
 				if(locationFromDevice != null) {
 					locationInput.setText(gpsHelper.getString(locationFromDevice));
 				}
 			} else {
 				locationInput.setText("");
 			}
 		}
 		if(equipment != null && equipment.length() > 0) {
 			equipmentInput.setText(equipment);
 		} else {
 			equipmentInput.setText("");
 		}
 		if(seeing > 0) {
 			seeingInput.setText(String.format("%d/%d", seeing, 5));
 		} else {
 			seeingInput.setText("");
 		}
 		if(transparency > 0) {
 			transInput.setText(String.format("%d/%d", transparency, 5));
 		} else {
 			transInput.setText("");
 		}
 		if(viewingNotes != null && !viewingNotes.equals("NULL")) {
 			notesInput.setText(viewingNotes);
 		} else {
 			notesInput.setText("");
 		}
 	}
 	
 	private void logDisplayElementsGone() {
 		dateDisplay.setVisibility(View.GONE);
 		timeDisplay.setVisibility(View.GONE);
 		locationDisplay.setVisibility(View.GONE);
 		equipmentDisplay.setVisibility(View.GONE);
 		seeingDisplay.setVisibility(View.GONE);
 		transDisplay.setVisibility(View.GONE);
 		notesDisplay.setVisibility(View.GONE);
 		
 		dateInput.setVisibility(View.VISIBLE);
 		timeInput.setVisibility(View.VISIBLE);
 		locationInput.setVisibility(View.VISIBLE);
 		equipmentInput.setVisibility(View.VISIBLE);
 		seeingInput.setVisibility(View.VISIBLE);
 		transInput.setVisibility(View.VISIBLE);
 		notesInput.setVisibility(View.VISIBLE);
 	}
 	
 	private void logEditElementsGone() {
 		dateInput.setVisibility(View.GONE);
 		timeInput.setVisibility(View.GONE);
 		locationInput.setVisibility(View.GONE);
 		equipmentInput.setVisibility(View.GONE);
 		seeingInput.setVisibility(View.GONE);
 		transInput.setVisibility(View.GONE);
 		notesInput.setVisibility(View.GONE);
 
 		dateDisplay.setVisibility(View.VISIBLE);
 		timeDisplay.setVisibility(View.VISIBLE);
 		locationDisplay.setVisibility(View.VISIBLE);
 		equipmentDisplay.setVisibility(View.VISIBLE);
 		seeingDisplay.setVisibility(View.VISIBLE);
 		transDisplay.setVisibility(View.VISIBLE);
 		notesDisplay.setVisibility(View.VISIBLE);
 	}
 	
 	private void setEditableMode() {
 		setUpLogEditElements();
 		populateLogEditElements();
 		setUpSaveCancelButtonsEditable();
 		logDisplayElementsGone();
 	}
 	
 	private void setDisplayMode() {
 		populateLogDisplayElements();
 		setUpSaveCancelButtonsDisplay();
 		logEditElementsGone();
 	}
 	
 	protected final View.OnClickListener zoomOnChart = new View.OnClickListener() {
 		public void onClick(View view) {
 			if(!imageZoomed) {
 				chart.setScaleType(ScaleType.CENTER);
 				imageZoomed = true;
 			} else {
 				chart.setScaleType(ScaleType.FIT_CENTER);
 				imageZoomed = false;
 			}
 		}
 	};
 	
 	protected final View.OnClickListener changeFavorite = new View.OnClickListener() {
 		public void onClick(View arg0) {
 			favorite = !favorite;
 			ObservableObjectDAO db = new ObservableObjectDAO(ObjectDetailScreen.this);
 			boolean success = db.setFavorite(id, favorite);
 			if(favorite && success) {
 				favoriteStar.setImageResource(settingsRef.getFavoriteStar());
 			} else if(!favorite && success) { //Only update the image if we successfully hit the db
 				favoriteStar.setImageResource(settingsRef.getNotFavoriteStar());
 			}
 			db.close();
 		}
 	};
 	
 	protected final Button.OnClickListener editLog = new Button.OnClickListener() {
 		public void onClick(View arg0) {
 			Intent intent = new Intent(ObjectDetailScreen.this, ObjectDetailScreen.class);
 			intent.putExtra("com.mobileobservationlog.objectName", objectName);
 			intent.putExtra("com.mobileobservationlog.editIntent", "true");
 			startActivity(intent);
 			ObjectDetailScreen.this.finish();
 		}
 	};
 	
 	protected final Button.OnClickListener saveLog = new Button.OnClickListener() {
 		public void onClick(View arg0) {
			if(otherCats.length() > 0) {
 				String[] otherCatsSplit = otherCats.split(",");
 				String otherInstalled = "";
 				ObservableObjectDAO db = new ObservableObjectDAO(getApplicationContext());
 				for(String otherDesignation : otherCatsSplit) {
 					Cursor result = db.getObjectData(otherDesignation);
 					if(result.getCount() > 0) {
 						if(otherInstalled.length() > 0) {
 							otherInstalled = otherInstalled.concat(", ");
 						}
 						otherInstalled = otherInstalled.concat(otherDesignation.trim());
 					}
 					result.close();
 				}
 				db.close();
 				
				if(otherInstalled.length() > 0) {
 					prepForModal();
 					modalHeader.setText(String.format("Update log info for this object in other catalogs as well: %s?", otherInstalled));
 					modalCancel.setText("No");
 					modalCancel.setVisibility(View.VISIBLE);
 					modalSave.setText(ObjectDetailScreen.this.getString(R.string.ok));
 					modalSave.setVisibility(View.VISIBLE);
 					modalClear.setVisibility(View.GONE);
 					modalSelectorsLayout.setVisibility(View.GONE);
 					modalListOneContainer.setVisibility(View.GONE);
 					modalListTwoContainer.setVisibility(View.GONE);
 					modalCancel.setOnClickListener(saveThisOneOnly);
 					modalSave.setOnClickListener(saveOthersAlso);
 				} else {
 					doUpdate(false);
 				}
 			} else {
 				doUpdate(false);
 			}
 		}
 	};
 	
 	protected final Button.OnClickListener saveOthersAlso = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			doUpdate(true);
 		}
 	};
 	
 	protected final Button.OnClickListener saveThisOneOnly = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			doUpdate(false);
 		}
 	};
 	
 	protected final Button.OnClickListener deleteLogData = new Button.OnClickListener() {
 		public void onClick(View view) {
 			prepForModal();
 			modalHeader.setText("Are you sure you want to delete the log data?");
 			modalCancel.setText(R.string.cancel);
 			modalCancel.setVisibility(View.VISIBLE);
 			modalSave.setText(R.string.ok);
 			modalSave.setVisibility(View.VISIBLE);
 			modalClear.setVisibility(View.GONE);
 			modalSelectorsLayout.setVisibility(View.GONE);
 			modalListOneContainer.setVisibility(View.GONE);
 			modalListTwoContainer.setVisibility(View.GONE);
 			modalCancel.setOnClickListener(dismissModal);
 			modalSave.setOnClickListener(confirmDeleteLog);
 		}
 	};
 	
 	protected final Button.OnClickListener confirmDeleteLog = new Button.OnClickListener() {
 		public void onClick(View view) {
 			ObservableObjectDAO db = new ObservableObjectDAO(ObjectDetailScreen.this);
 			db.clearLogData(id);
 			tearDownModal();
 			Intent restartActivity = new Intent(ObjectDetailScreen.this, ObjectDetailScreen.class);
 			restartActivity.putExtra("com.mobileobservationlog.objectName", objectName);
 			restartActivity.putExtra("com.mobileobservationlog.editIntent", "true");
 			startActivity(restartActivity);
 			ObjectDetailScreen.this.finish();
 		}
 	};
 	
 	protected final Button.OnClickListener clearLogData = new Button.OnClickListener() {
 		public void onClick(View view) {
 			populateLogEditElements();
 		}
 	};
 	
 	protected final Button.OnClickListener dismissModal = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 		}
 	};
 	
 	protected final View.OnClickListener listsModal = new View.OnClickListener() {
 		public void onClick(View view) {
 			prepForModal();
 			
 			targetLists = new TreeMap<String, Boolean>();
 			ArrayList<IndividualItem> options = new ArrayList<IndividualItem>();
 			
 			TargetListsDAO db = new TargetListsDAO(ObjectDetailScreen.this);
 			Cursor lists = db.getAllTargetLists();
 			Cursor alreadySet = db.getListsByObject(objectName);
 			alreadySet.moveToFirst();
 			originalLists = new ArrayList<String>();
 			if(alreadySet.getCount() > 0) {
 				do {
 					originalLists.add(alreadySet.getString(0));
 				} 
 				while(alreadySet.moveToNext());
 			}
 			int count = lists.getCount();
 			if(count > 0) {
 				lists.moveToFirst();
 				for(int i = 0; i < count; i++) {
 					String name = lists.getString(1);
 					boolean set = originalLists.contains(name);
 					options.add(new IndividualItem(name, set));
 					targetLists.put(name, set);
 					lists.moveToNext();
 				}
 			}
 			lists.close();
 			alreadySet.close();
 			db.close();
 			
 			if(targetLists.size() > 0) {
 				modalHeader.setText("Target Lists");
 				modalListOneContainer.setVisibility(View.VISIBLE);
 				modalListHeaderOne.setVisibility(View.GONE);
 		        modalListOne.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), options));
 		        modalListOne.setOnItemClickListener(listSelected);
 				
 				modalSelectorsLayout.setVisibility(View.GONE);
 				modalListTwoContainer.setVisibility(View.GONE);
 				
 				modalSave.setOnClickListener(saveToList);
 				modalSave.setVisibility(View.VISIBLE);
 				modalCancel.setOnClickListener(dismissModal);
 				modalCancel.setVisibility(View.VISIBLE);
 				modalClear.setVisibility(View.GONE);
 				
 				Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 				int windowHeight = display.getHeight(); 
 				RelativeLayout.LayoutParams listOneParams = (RelativeLayout.LayoutParams)modalListOneContainer.getLayoutParams();
 				if(listOneParams.height > (int) windowHeight * 0.7f) {
 					listOneParams.height = (int) (windowHeight * 0.7f);
 					modalListOneContainer.setLayoutParams(listOneParams);
 				}
 			} else {
 				modalHeader.setText("There are no target lists created yet. Target lists may be managed through the home screen");
 				modalListOneContainer.setVisibility(View.GONE);
 				modalSelectorsLayout.setVisibility(View.GONE);
 				modalListTwoContainer.setVisibility(View.GONE);
 				
 				modalSave.setOnClickListener(dismissModal);
 				modalSave.setVisibility(View.VISIBLE);
 				modalCancel.setVisibility(View.GONE);
 				modalClear.setVisibility(View.GONE);
 			}
 		}
 	};
 	
 	protected final AdapterView.OnItemClickListener listSelected = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 			IndividualItem option = (IndividualItem)adapter.getItemAtPosition(position);
 			boolean newValue = !option.getSelected();
 			String name = option.getName();
 			targetLists.put(name, newValue);
 			
 			ImageView checked = (ImageView) view.findViewById(R.id.checkbox);
 			if(newValue) {
 				checked.setImageResource(settingsRef.getCheckbox_Selected());
 			} else {
 				checked.setImageResource(settingsRef.getCheckbox_Unselected());
 			}
 		}
 	};
 	
 	private final Button.OnClickListener saveToList = new Button.OnClickListener() {
 		public void onClick(View view) {
 			TargetListsDAO db = new TargetListsDAO(getApplicationContext());
 			boolean success = true;
 			for(String listName : targetLists.keySet()) {
 				if(targetLists.get(listName) && !originalLists.contains(listName)) {
 					if(!db.addItemToList(listName, objectName)) {
 						success = false;
 					}
 				} else if(!targetLists.get(listName) && originalLists.contains(listName)) {
 					if(!db.removeObjectFromList(listName, objectName)) {
 						success = false;
 					}
 				}
 			}
 			if(!success) {
 				modalHeader.setText("There was an error saving the object to the selected list(s)");
 				modalSave.setVisibility(View.GONE);
 				modalCancel.setOnClickListener(dismissModal);
 				modalCancel.setVisibility(View.VISIBLE);
 				modalClear.setVisibility(View.GONE);
 				modalListOneContainer.setVisibility(View.GONE);
 				modalListTwoContainer.setVisibility(View.GONE);
 			} else {
 				tearDownModal();
 			}
 		}
 	};
 	
 	protected final View.OnClickListener dateModal = new View.OnClickListener() {
 		public void onClick(View view) {
 			//Date will be in format MMM dd, yyyy
 			String fullDate = dateInput.getText().toString();
 			String[] split = fullDate.split(" ");
 			String month = split[0];
 			String day = split[1].split(",")[0];
 			String year = split[2];
 			
 			prepForModal();
 			datePicker = new DatePicker(month, day, year, ObjectDetailScreen.this);
 			
 			numPickerOne = datePicker.month;
 			numPickerTwo = datePicker.day;
 			numPickerThree = datePicker.year;
 			
 			modalHeader.setText("Object Log Date");
 			modalSelectorTextOne.setText(numPickerOne.getCurrentValue());
 			modalSelectorTextTwo.setText(numPickerTwo.getCurrentValue());
 			modalSelectorTextThree.setText(numPickerThree.getCurrentValue());
 			
 			modalSelectorOneUpButton.setOnClickListener(incrementButtonOne);
 			modalSelectorOneDownButton.setOnClickListener(decrementButtonOne);
 			modalSelectorTwoUpButton.setOnClickListener(incrementButtonTwo);
 			modalSelectorTwoDownButton.setOnClickListener(decrementButtonTwo);
 			modalSelectorThreeUpButton.setOnClickListener(incrementButtonThree);
 			modalSelectorThreeDownButton.setOnClickListener(decrementButtonThree);
 			
 			modalSelectorsLayout.setVisibility(View.VISIBLE);
 			modalSelectorSetOne.setVisibility(View.VISIBLE);
 			modalSelectorSetTwo.setVisibility(View.VISIBLE);
 			modalSelectorSetThree.setVisibility(View.VISIBLE);
 			modalListOneContainer.setVisibility(View.GONE);
 			modalListTwoContainer.setVisibility(View.GONE);
 			
 			modalSave.setOnClickListener(saveDate);
 			modalSave.setVisibility(View.VISIBLE);
 			modalCancel.setOnClickListener(dismissModal);
 			modalCancel.setVisibility(View.VISIBLE);
 			modalClear.setVisibility(View.GONE);
 		}
 	};
 	
 	protected final View.OnClickListener timeModal = new View.OnClickListener() {
 		public void onClick(View v) {
 			//Time will be in format h:mm a
 			String fullTime = timeInput.getText().toString();
 			String[] firstSplit = fullTime.split(":");
 			String[] secondSplit = firstSplit[1].split(" ");
 			String hour = firstSplit[0];
 			String minute = secondSplit[0];
 			String amPm = secondSplit[1];
 			
 			prepForModal();
 			timePicker = new TimePicker(hour, minute, amPm, ObjectDetailScreen.this);
 			
 			numPickerOne = timePicker.hourPicker;
 			numPickerTwo = timePicker.minutePicker;
 			numPickerThree = timePicker.amPmPicker;
 			
 			modalHeader.setText("Object Log Time");
 			modalSelectorTextOne.setText(numPickerOne.getCurrentValue());
 			modalSelectorTextTwo.setText(numPickerTwo.getCurrentValue());
 			modalSelectorTextThree.setText(numPickerThree.getCurrentValue());
 			
 			modalSelectorOneUpButton.setOnClickListener(incrementButtonOne);
 			modalSelectorOneDownButton.setOnClickListener(decrementButtonOne);
 			modalSelectorTwoUpButton.setOnClickListener(incrementButtonTwo);
 			modalSelectorTwoDownButton.setOnClickListener(decrementButtonTwo);
 			modalSelectorThreeUpButton.setOnClickListener(incrementButtonThree);
 			modalSelectorThreeDownButton.setOnClickListener(decrementButtonThree);
 			
 			modalSelectorsLayout.setVisibility(View.VISIBLE);
 			modalSelectorSetOne.setVisibility(View.VISIBLE);
 			modalSelectorSetTwo.setVisibility(View.VISIBLE);
 			modalSelectorSetThree.setVisibility(View.VISIBLE);
 			modalListOneContainer.setVisibility(View.GONE);
 			modalListTwoContainer.setVisibility(View.GONE);
 			
 			modalSave.setOnClickListener(saveTime);
 			modalSave.setVisibility(View.VISIBLE);
 			modalCancel.setOnClickListener(dismissModal);
 			modalCancel.setVisibility(View.VISIBLE);
 			modalClear.setVisibility(View.GONE);
 		}
 	};
 	
 	protected final View.OnClickListener locationModal = new View.OnClickListener() {
 		public void onClick(View view) {
 			prepForModal();
 			
 			ArrayList<IndividualItem> options = new ArrayList<IndividualItem>();
 			
 			LocationsDAO db = new LocationsDAO(ObjectDetailScreen.this);
 			Cursor locations = db.getSavedLocations();
 			int count = locations.getCount();
 			if(count > 0) {
 				locations.moveToFirst();
 				for(int i = 0; i < count; i++) {
 					String name = locations.getString(1);
 					String coords = locations.getString(2);
 					String compiled = "";
 					if(name != null) {
 						compiled = compiled.concat(name);
 					}
 					if(coords != null && coords.length() > 0) {
 						if(compiled.length() > 0) {
 							compiled = compiled.concat(" - ");
 						}
 						compiled = compiled.concat(coords);
 					}
 					options.add(new IndividualItem(compiled, false));
 					locations.moveToNext();
 				}
 			}
 			locations.close();
 			db.close();
 
 			String currentValue = locationInput.getText().toString();
 			if(currentValue.length() > 0) {
 				boolean found = false;
 				int index = -1;
 				for(int i = 0; i < options.size(); i++) {
 					if(options.get(i).getName().equals(currentValue)) {
 						found = true;
 						index = i;
 						break;
 					}
 				}
 				if(found) {
 					options.set(index, new IndividualItem(currentValue, true));
 				}
 			}
 			
 			if(options.size() > 0) {
 				modalHeader.setText("Observing Location");
 				modalListOneContainer.setVisibility(View.VISIBLE);
 				modalListHeaderOne.setVisibility(View.GONE);
 		        modalListOne.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), options));
 		        modalListOne.setOnItemClickListener(locationSelected);
 				
 				modalSelectorsLayout.setVisibility(View.GONE);
 				modalListTwoContainer.setVisibility(View.GONE);
 				
 				modalSave.setOnClickListener(saveLocation);
 				modalSave.setVisibility(View.VISIBLE);
 				modalCancel.setOnClickListener(dismissModal);
 				modalCancel.setVisibility(View.VISIBLE);
 				modalClear.setOnClickListener(locationTypeManually);
 				modalClear.setText(R.string.type_manually);
 				modalClear.setVisibility(View.VISIBLE);
 				
 				Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 				int windowHeight = display.getHeight(); 
 				RelativeLayout.LayoutParams listOneParams = (RelativeLayout.LayoutParams)modalListOneContainer.getLayoutParams();
 				if(listOneParams.height > (int) windowHeight * 0.7f) {
 					listOneParams.height = (int) (windowHeight * 0.7f);
 					modalListOneContainer.setLayoutParams(listOneParams);
 				}
 			} else {
 				modalHeader.setText("There are no saved locations. Observing Locations can be managed trough the settings screen");
 				modalListOneContainer.setVisibility(View.GONE);
 				modalSelectorsLayout.setVisibility(View.GONE);
 				modalListTwoContainer.setVisibility(View.GONE);
 				
 				modalSave.setOnClickListener(dismissModal);
 				modalSave.setVisibility(View.VISIBLE);
 				modalCancel.setVisibility(View.GONE);
 				modalClear.setVisibility(View.GONE);
 			}
 		}
 	};
 	
 	protected final View.OnClickListener equipmentModal = new View.OnClickListener() {
 		public void onClick(View view) {
 			prepForModal();
 			
 			ArrayList<IndividualItem> eyepieceOptions = new ArrayList<IndividualItem>();
 			ArrayList<IndividualItem> telescopeOptions = new ArrayList<IndividualItem>();
 			eyepieces = new TreeMap<String, Integer>();
 			telescopes = new TreeMap<String, Integer>();
 			
 			EquipmentDAO db = new EquipmentDAO(ObjectDetailScreen.this);
 			Cursor eyepiecesCursor = db.getSavedEyepieces();
 			int count = eyepiecesCursor.getCount();
 			if(count > 0) {
 				eyepiecesCursor.moveToFirst();
 				for(int i = 0; i < count; i++) {
 					int id = eyepiecesCursor.getInt(0);
 					String focalLength = eyepiecesCursor.getString(1);
 					String type = eyepiecesCursor.getString(2);
 					String compiled = formatEyepieceDescription(type, focalLength);
 					eyepieces.put(compiled, id);
 					eyepieceOptions.add(new IndividualItem(compiled, false));
 					eyepiecesCursor.moveToNext();
 				}
 			}
 			eyepiecesCursor.close();
 			
 			Cursor telescopeCursor = db.getSavedTelescopes();
 			count = telescopeCursor.getCount();
 			if(count > 0) {
 				telescopeCursor.moveToFirst();
 				for(int i = 0; i < count; i++) {
 					int id = telescopeCursor.getInt(0);
 					String type = telescopeCursor.getString(1);
 					String diameter = telescopeCursor.getString(2);
 					String ratio = telescopeCursor.getString(3);
 					String length = telescopeCursor.getString(4);
 					String compiled = formatTelescopeDescription(type, ratio, diameter, length);
 					telescopes.put(compiled, id);
 					telescopeOptions.add(new IndividualItem(compiled, false));
 					telescopeCursor.moveToNext();
 				}
 			}
 			telescopeCursor.close();
 			db.close();
 			
 			if(eyepieceOptions.size() > 0 || telescopeOptions.size() > 0) {
 				modalHeader.setText("Observing Equipment");
 				
 				if(telescopeOptions.size() > 0) {
 					modalListOneContainer.setVisibility(View.VISIBLE);
 					modalListHeaderOne.setText("Telescopes");
 					modalListHeaderOne.setVisibility(View.VISIBLE);
 			        modalListOne.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), telescopeOptions));
 			        modalListOne.setOnItemClickListener(telescopeSelected);
 				} else {
 					modalListOneContainer.setVisibility(View.VISIBLE);
 					modalListHeaderOne.setText("There are no saved telescopes. Equipment can be managed through the settings screen");
 					modalListOne.setVisibility(View.GONE);
 				}
 	
 				if(eyepieceOptions.size() > 0) {
 					modalListTwoContainer.setVisibility(View.VISIBLE);
 					modalListHeaderTwo.setText("Eyepieces");
 					modalListTwo.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), eyepieceOptions));
 					modalListTwo.setOnItemClickListener(eyepieceSelected);
 				} else {
 					modalListTwoContainer.setVisibility(View.VISIBLE);
 					modalListHeaderTwo.setText("There are no saved eyepieces. Equipment can be managed through the settings screen");
 					modalListTwo.setVisibility(View.GONE);
 				}
 				
 				modalSelectorSetOne.setVisibility(View.GONE);
 				modalSelectorSetTwo.setVisibility(View.GONE);
 				modalSelectorSetThree.setVisibility(View.GONE);
 				
 				modalSave.setOnClickListener(saveEquipment);
 				modalSave.setVisibility(View.VISIBLE);
 				modalCancel.setOnClickListener(dismissModal);
 				modalCancel.setVisibility(View.VISIBLE);
 				modalClear.setOnClickListener(equipmentTypeManually);
 				modalClear.setText(R.string.type_manually);
 				modalClear.setVisibility(View.VISIBLE);
 			} else {
 				modalHeader.setText("There are no saved telescope or eyepiece options. Equipment can be managed through the settings screen");
 				modalListOneContainer.setVisibility(View.GONE);
 				modalSelectorsLayout.setVisibility(View.GONE);
 				modalListTwoContainer.setVisibility(View.GONE);
 				
 				modalSave.setOnClickListener(dismissModal);
 				modalSave.setVisibility(View.VISIBLE);
 				modalCancel.setVisibility(View.GONE);
 				modalClear.setVisibility(View.GONE);
 			}
 			Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 			int windowHeight = display.getHeight(); 
 			RelativeLayout.LayoutParams listOneParams = (RelativeLayout.LayoutParams)modalListOneContainer.getLayoutParams();
 			listOneParams.height = (int) (windowHeight * 0.4f);
 			modalListOneContainer.setLayoutParams(listOneParams);
 			RelativeLayout.LayoutParams listTwoParams = (RelativeLayout.LayoutParams)modalListTwoContainer.getLayoutParams();
 			listTwoParams.height = (int) (windowHeight * 0.4f);
 			modalListTwoContainer.setLayoutParams(listTwoParams);
 		}
 	};
 	
 	protected final View.OnClickListener seeingModal = new View.OnClickListener() {
 		public void onClick(View view) {
 			prepForModal();
 			
 			ArrayList<String> options = new ArrayList<String>();
 			options.add("1");
 			options.add("2");
 			options.add("3");
 			options.add("4");
 			options.add("5");
 			
 			String currentValue = seeingInput.getText().toString();
 			if(currentValue == null || currentValue.equals("")) {
 				currentValue = "1";
 			} else {
 				if(currentValue.contains("/")) {
 					currentValue = currentValue.split("/")[0];
 				}
 			}
 			numPickerOne = new NumberPickerDriver(options, currentValue, ObjectDetailScreen.this) {
 				@Override
 				public boolean save() {
 					return false;
 				}
 			};
 			
 			modalHeader.setText("Seeing Conditions (1 - 5)");
 			modalSelectorTextOne.setText(numPickerOne.getCurrentValue());
 			
 			modalSelectorOneUpButton.setOnClickListener(incrementButtonOne);
 			modalSelectorOneDownButton.setOnClickListener(decrementButtonOne);
 			
 			modalSelectorsLayout.setVisibility(View.VISIBLE);
 			modalSelectorSetOne.setVisibility(View.VISIBLE);
 			modalSelectorSetTwo.setVisibility(View.GONE);
 			modalSelectorSetThree.setVisibility(View.GONE);
 			modalListOneContainer.setVisibility(View.GONE);
 			modalListTwoContainer.setVisibility(View.GONE);
 			
 			modalSave.setOnClickListener(saveSeeing);
 			modalSave.setVisibility(View.VISIBLE);
 			modalCancel.setOnClickListener(dismissModal);
 			modalCancel.setVisibility(View.VISIBLE);
 			modalClear.setVisibility(View.GONE);
 		}
 	};
 	
 	protected final View.OnClickListener transpModal = new View.OnClickListener() {
 		public void onClick(View view) {
 			prepForModal();
 			
 			ArrayList<String> options = new ArrayList<String>();
 			options.add("1");
 			options.add("2");
 			options.add("3");
 			options.add("4");
 			options.add("5");
 			
 			String currentValue = transInput.getText().toString();
 			if(currentValue == null || currentValue.equals("")) {
 				currentValue = "1";
 			} else {
 				if(currentValue.contains("/")) {
 					currentValue = currentValue.split("/")[0];
 				}
 			}
 			numPickerOne = new NumberPickerDriver(options, currentValue, ObjectDetailScreen.this) {
 				@Override
 				public boolean save() {
 					return false;
 				}
 			};
 			
 			modalHeader.setText("Transparency Conditions (1 - 5)");
 			modalSelectorTextOne.setText(numPickerOne.getCurrentValue());
 			
 			modalSelectorOneUpButton.setOnClickListener(incrementButtonOne);
 			modalSelectorOneDownButton.setOnClickListener(decrementButtonOne);
 			
 			modalSelectorsLayout.setVisibility(View.VISIBLE);
 			modalSelectorSetOne.setVisibility(View.VISIBLE);
 			modalSelectorSetTwo.setVisibility(View.GONE);
 			modalSelectorSetThree.setVisibility(View.GONE);
 			modalListOneContainer.setVisibility(View.GONE);
 			modalListTwoContainer.setVisibility(View.GONE);
 			
 			modalSave.setOnClickListener(saveTrans);
 			modalSave.setVisibility(View.VISIBLE);
 			modalCancel.setOnClickListener(dismissModal);
 			modalCancel.setVisibility(View.VISIBLE);
 			modalClear.setVisibility(View.GONE);
 		}
 	};
 	
 	protected final Button.OnClickListener incrementButtonOne = new Button.OnClickListener() {
 		public void onClick(View view) {
 			numPickerOne.upButton();
 			modalSelectorTextOne.setText(numPickerOne.getCurrentValue());
 		}
 	};
 	
 	protected final Button.OnClickListener decrementButtonOne = new Button.OnClickListener() {
 		public void onClick(View view) {
 			numPickerOne.downButton();
 			modalSelectorTextOne.setText(numPickerOne.getCurrentValue());
 		}
 	};
 	
 	protected final Button.OnClickListener incrementButtonTwo = new Button.OnClickListener() {
 		public void onClick(View view) {
 			numPickerTwo.upButton();
 			modalSelectorTextTwo.setText(numPickerTwo.getCurrentValue());
 		}
 	};
 	
 	protected final Button.OnClickListener decrementButtonTwo = new Button.OnClickListener() {
 		public void onClick(View view) {
 			numPickerTwo.downButton();
 			modalSelectorTextTwo.setText(numPickerTwo.getCurrentValue());
 		}
 	};
 	
 	protected final Button.OnClickListener incrementButtonThree = new Button.OnClickListener() {
 		public void onClick(View view) {
 			numPickerThree.upButton();
 			modalSelectorTextThree.setText(numPickerThree.getCurrentValue());
 		}
 	};
 	
 	protected final Button.OnClickListener decrementButtonThree = new Button.OnClickListener() {
 		public void onClick(View view) {
 			numPickerThree.downButton();
 			modalSelectorTextThree.setText(numPickerThree.getCurrentValue());
 		}
 	};
 	
 	protected final Button.OnClickListener saveDate = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			String date = String.format("%s %s, %s", numPickerOne.getCurrentValue(), numPickerTwo.getCurrentValue(), numPickerThree.getCurrentValue());
 			dateInput.setText(date);
 		}
 	};
 	
 	protected final Button.OnClickListener saveTime = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			String time = String.format("%s:%s %s", numPickerOne.getCurrentValue(), numPickerTwo.getCurrentValue(), numPickerThree.getCurrentValue());
 			timeInput.setText(time);
 		}
 	};
 	
 	protected final AdapterView.OnItemClickListener locationSelected = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 			IndividualItem option = (IndividualItem)adapter.getItemAtPosition(position);
 			ArrayList<IndividualItem> options = new ArrayList<IndividualItem>();
 			boolean newValue = !option.getSelected();
 			//reset all the true items to false (Should only be one)
 			selectedLocation = ""; //Reset it in case the user has deselected all, leaving the location blank;
 			for(int i = 0; i < adapter.getCount(); i++) {
 				IndividualItem currentOption = (IndividualItem)adapter.getItemAtPosition(i);
 				if(!currentOption.getName().equals(option.getName())) {
 					options.add(new IndividualItem(currentOption.getName(), false));
 				} else {
 					options.add(new IndividualItem(currentOption.getName(), newValue));
 				}
 			}
 	        modalListOne.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), options));
 	        if(newValue) {
 	        	selectedLocation = option.getName();
 	        }
 		}
 	};
 	
 	protected final  Button.OnClickListener locationTypeManually = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			locationInput.setText("");
 			showKeyboardLetters(locationInput);
 		}
 	};
 	
 	protected final Button.OnClickListener saveLocation = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			locationInput.setText(selectedLocation);
 		}
 	};
 	
 	protected final AdapterView.OnItemClickListener telescopeSelected = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 			IndividualItem option = (IndividualItem)adapter.getItemAtPosition(position);
 			ArrayList<IndividualItem> options = new ArrayList<IndividualItem>();
 			boolean newValue = !option.getSelected();
 			//reset all the true items to false (Should only be one)
 			newTelescopeId = 0; //Reset it in case the user has deselected all, leaving the location blank;
 			for(int i = 0; i < adapter.getCount(); i++) {
 				IndividualItem currentOption = (IndividualItem)adapter.getItemAtPosition(i);
 				if(!currentOption.getName().equals(option.getName())) {
 					options.add(new IndividualItem(currentOption.getName(), false));
 				} else {
 					options.add(new IndividualItem(currentOption.getName(), newValue));
 				}
 			}
 	        modalListOne.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), options));
 	        if(newValue) {
 	        	newTelescopeId = telescopes.get(option.getName());
 	        }
 		}
 	};
 	
 	protected final AdapterView.OnItemClickListener eyepieceSelected = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 			IndividualItem option = (IndividualItem)adapter.getItemAtPosition(position);
 			ArrayList<IndividualItem> options = new ArrayList<IndividualItem>();
 			boolean newValue = !option.getSelected();
 			//reset all the true items to false (Should only be one)
 			newEyepieceId = 0; //Reset it in case the user has deselected all, leaving the location blank;
 			for(int i = 0; i < adapter.getCount(); i++) {
 				IndividualItem currentOption = (IndividualItem)adapter.getItemAtPosition(i);
 				if(!currentOption.getName().equals(option.getName())) {
 					options.add(new IndividualItem(currentOption.getName(), false));
 				} else {
 					options.add(new IndividualItem(currentOption.getName(), newValue));
 				}
 			}
 	        modalListTwo.setAdapter(new IndividualItemAdapter(ObjectDetailScreen.this, settingsRef.getSearchModalListLayout(), options));
 	        if(newValue) {
 	        	newEyepieceId = eyepieces.get(option.getName());
 	        }
 		}
 	};
 	
 	protected final  Button.OnClickListener equipmentTypeManually = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			equipmentInput.setText("");
 			showKeyboardLetters(equipmentInput);
 		}
 	};
 	
 	protected final Button.OnClickListener saveEquipment = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			equipmentInput.setText(formatEquipmentString(newTelescopeId, newEyepieceId));
 		}
 	};
 	
 	protected final Button.OnClickListener saveSeeing = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			seeingInput.setText(numPickerOne.getCurrentValue() + "/5");
 		}
 	};
 	
 	protected final Button.OnClickListener saveTrans = new Button.OnClickListener() {
 		public void onClick(View view) {
 			tearDownModal();
 			transInput.setText(numPickerOne.getCurrentValue() + "/5");
 		}
 	};
 	
 	protected final Button.OnClickListener showLetters_click = new Button.OnClickListener(){
     	public void onClick(View view){
     		if(firstClick > 0){
     			showKeyboardLetters(view);
     		}
     		firstClick = -1;
     	}
     };
     
     protected final EditText.OnFocusChangeListener showLetters_focus = new EditText.OnFocusChangeListener(){
     	public void onFocusChange(View view, boolean hasFocus) {
 			if(hasFocus){
 				showKeyboardLetters(view);
 			}
 			else{
 				tearDownKeyboard();
 			}
 			firstFocus = 1;
 		}
     };
     
     protected final EditText.OnFocusChangeListener showLetters_focus_LoseFocusOnly = new EditText.OnFocusChangeListener(){
     	public void onFocusChange(View view, boolean hasFocus) {
 			if(!hasFocus){
 				tearDownKeyboard();
 			}
 		}
     };
     
     protected final Button.OnClickListener showNumbers_click = new Button.OnClickListener(){
     	public void onClick(View view){
     		if(firstClick > 0){
     			showKeyboardNumbers(view);
     		}
     		firstClick = -1;
     	}
     };
     
     protected final EditText.OnFocusChangeListener showNumbers_focus = new EditText.OnFocusChangeListener(){
     	public void onFocusChange(View view, boolean hasFocus) {
 			if(hasFocus){
 				showKeyboardNumbers(view);
 			}
 			else{
 				tearDownKeyboard();
 			}
 			firstFocus = 1;
 		}
     };
 
 	private void showKeyboardLetters(View view) {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
 		keyboardRoot = (FrameLayout)findViewById(R.id.keyboard_root);
 		if(keyboardDriver != null)
 			keyboardDriver = null;
 		keyboardRoot.setVisibility(View.VISIBLE);
 		keyboardDriver = new SoftKeyboard(this, (EditText) view, TargetInputType.LETTERS);
 		setMargins_keyboard();
 	}
 
 	private void showKeyboardNumbers(View view) {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
 		keyboardRoot = (FrameLayout)findViewById(R.id.keyboard_root);
 		if(keyboardDriver != null)
 			keyboardDriver = null;
 		keyboardRoot.setVisibility(View.VISIBLE);
 		keyboardDriver = new SoftKeyboard(this, (EditText) view, TargetInputType.NUMBER_DECIMAL);
 		setMargins_keyboard();
 	}
     
     private void tearDownKeyboard(){
     	if(keyboardDriver != null){
     		keyboardDriver.hideAll();
 	    	keyboardRoot.setVisibility(View.INVISIBLE);
 	    	keyboardDriver = null;
 			setMargins_noKeyboard();
     	}
     }
 
     //We killed the ManageEquipment screen when launching this activity, otherwise we would go back to it in a stale state. We need to kill this activity and relaunch
     //that one when the back button is pressed.
     @Override
     public void onBackPressed() {
 		if(keyboardDriver != null){
 			tearDownKeyboard();
 		}
 		else{
 	    	super.onBackPressed();
 		}
     }
 	
 	private void setMargins_noKeyboard()
 	{
 		ScrollView fieldsScroller = (ScrollView)findViewById(R.id.object_detail_scroll_view);
 		FrameLayout keyboardFrame = (FrameLayout)findViewById(R.id.keyboard_root);
 		int buttonsKeyboardSize = keyboardFrame.getHeight();
 		
 		MarginLayoutParams frameParams = (MarginLayoutParams)keyboardFrame.getLayoutParams();
 		frameParams.setMargins(0, 0, 0, 0);
 		
 		MarginLayoutParams scrollParams = (MarginLayoutParams)fieldsScroller.getLayoutParams();
 		scrollParams.setMargins(0, 0, 0, 0);
 		
 		keyboardFrame.setLayoutParams(frameParams);
 		fieldsScroller.setLayoutParams(scrollParams);
 	}
 	
 	private void setMargins_keyboard()
 	{
 		ScrollView fieldsScroller = (ScrollView)findViewById(R.id.object_detail_scroll_view);
 		FrameLayout keyboardFrame = (FrameLayout)findViewById(R.id.keyboard_root);
 		int buttonsKeyboardSize = keyboardFrame.getHeight();
 		
 		MarginLayoutParams frameParams = (MarginLayoutParams)keyboardFrame.getLayoutParams();
 		frameParams.setMargins(0, -buttonsKeyboardSize, 0, 0);
 		
 		MarginLayoutParams scrollParams = (MarginLayoutParams)fieldsScroller.getLayoutParams();
 		scrollParams.setMargins(0, 0, 0, buttonsKeyboardSize);
 		
 		keyboardFrame.setLayoutParams(frameParams);
 		fieldsScroller.setLayoutParams(scrollParams);
 	}
 	
 	private void doUpdate(boolean updateOtherCatalogs) {
 		TreeMap<String, String> updateArgs = gatherUpdateArgs();
 		
 		ObservableObjectDAO db = new ObservableObjectDAO(ObjectDetailScreen.this);
 		boolean success = db.updateLogData(id, updateArgs, updateOtherCatalogs);
 		
 		if(success) {
 			Intent intent = new Intent(ObjectDetailScreen.this, ObjectDetailScreen.class);
 			intent.putExtra("com.mobileobservationlog.objectName", objectName);
 			startActivity(intent);
 			ObjectDetailScreen.this.finish();
 		} else {
 			prepForModal();
 			modalHeader.setText("There was an error saving. Please Try Again.");
 			modalCancel.setText(getString(R.string.ok));
 			modalCancel.setVisibility(View.VISIBLE);
 			modalSave.setVisibility(View.GONE);
 			modalClear.setVisibility(View.GONE);
 			modalSelectorsLayout.setVisibility(View.GONE);
 			modalListOneContainer.setVisibility(View.GONE);
 			modalListTwoContainer.setVisibility(View.GONE);
 			modalCancel.setOnClickListener(dismissModal);
 		}
 	}
 	
 	private TreeMap<String, String> gatherUpdateArgs() {
 		TreeMap<String, String> retVal = new TreeMap<String, String>();
 		if(dateInput.getText() != null && dateInput.getText().length() > 0) {
 			retVal.put("logDate", dateInput.getText().toString());
 		}
 		if(timeInput.getText() != null && timeInput.getText().length() > 0) {
 			retVal.put("logTime", timeInput.getText().toString());
 		}
 		if(locationInput.getText() != null && locationInput.getText().length() > 0) {
 			retVal.put("logLocation", locationInput.getText().toString());
 		}
 		if(equipmentInput.getText() != null && equipmentInput.getText().length() > 0) {
 			retVal.put("equipment", equipmentInput.getText().toString());
 		}
 		if(seeingInput.getText().length() > 0 && !seeingInput.getText().equals("")) {
 			String seeing = seeingInput.getText().toString().split("/")[0];
 			if(Integer.parseInt(seeing) > 0) {
 				retVal.put("seeing", seeing);
 			}
 		}
 		if(transInput.getText().length() > 0 && !transInput.getText().equals("")) {
 			String trans = transInput.getText().toString().split("/")[0];
 			if(Integer.parseInt(trans) > 0); {
 				retVal.put("transparency", trans);
 			}
 		}
 		//findingMethod not currently used
 		if(notesInput.getText() != null && notesInput.getText().length() > 0) {
 			retVal.put("viewingNotes", notesInput.getText().toString());
 		}
 		if(retVal.size() > 0) {
 			retVal.put("logged", "true");
 		} else {
 			retVal.put("logged", "false");
 		}
 		return retVal;
 	}
 	
 	private void findModalElements() {
 		modalHeader = (TextView)findViewById(R.id.modal_header);
 		modalButtonContainer = (LinearLayout)findViewById(R.id.save_cancel_container);
 		modalSave = (Button)findViewById(R.id.alert_ok_button);
 		modalCancel = (Button)findViewById(R.id.alert_cancel_button);
 		modalClear = (Button)findViewById(R.id.alert_extra_button);
 		modalSelectorsLayout = (LinearLayout)findViewById(R.id.selectors_container);
 		modalSelectorSetOne = (LinearLayout)findViewById(R.id.selector_set_one);
 		modalSelectorSetTwo = (LinearLayout)findViewById(R.id.selector_set_two);
 		modalSelectorSetThree = (LinearLayout)findViewById(R.id.selector_set_three);
 		modalSelectorOneUpButton = (Button)findViewById(R.id.number_picker_up_button_one);
 		modalSelectorOneDownButton = (Button)findViewById(R.id.number_picker_down_button_one);
 		modalSelectorTwoUpButton = (Button)findViewById(R.id.number_picker_up_button_two);
 		modalSelectorTwoDownButton = (Button)findViewById(R.id.number_picker_down_button_two);
 		modalSelectorThreeUpButton = (Button)findViewById(R.id.number_picker_up_button_three);
 		modalSelectorThreeDownButton = (Button)findViewById(R.id.number_picker_down_button_three);
 		modalSelectorTextOne = (TextView)findViewById(R.id.number_picker_input_field_one);
 		modalSelectorTextTwo = (TextView)findViewById(R.id.number_picker_input_field_two);
 		modalSelectorTextThree = (TextView)findViewById(R.id.number_picker_input_field_three);
 		modalListOneContainer = (LinearLayout)findViewById(R.id.object_selector_modal_list_layout_one);
 		modalListOne = (ListView)findViewById(R.id.modal_list_one);
 		modalListHeaderOne = (TextView)findViewById(R.id.object_selector_modal_list_one_header);
 		modalListTwoContainer = (LinearLayout)findViewById(R.id.object_selector_modal_list_layout_two);
 		modalListTwo = (ListView)findViewById(R.id.modal_list_two);
 		modalListHeaderTwo = (TextView)findViewById(R.id.object_selector_modal_list_two_header);
 	}
 
 	/**
 	 * Helper method to dim out the background and make the list view unclickable in preparation to display a modal
 	 */
 	public void prepForModal() {
 		RelativeLayout blackOutLayer = (RelativeLayout)findViewById(R.id.settings_fog);
 		RelativeLayout mainBackLayer = (RelativeLayout)findViewById(R.id.object_detail_main);
 		ScrollView scroller = (ScrollView)findViewById(R.id.object_detail_scroll_view);
 		
 		mainBackLayer.setEnabled(false);
 		scroller.setEnabled(false);
 		addToList.setEnabled(false);
 		favoriteStar.setEnabled(false);
 		saveButton.setEnabled(false);
 		clearButton.setEnabled(false);
 		dateInput.setEnabled(false);
 		timeInput.setEnabled(false);
 		locationInput.setEnabled(false);
 		equipmentInput.setEnabled(false);
 		seeingInput.setEnabled(false);
 		transInput.setEnabled(false);
 		notesInput.setEnabled(false);
 		blackOutLayer.setVisibility(View.VISIBLE);
 		
 		if(keyboardDriver != null) {
 			tearDownKeyboard();
 		}
 		
 		RelativeLayout alertModal = (RelativeLayout)findViewById(R.id.alert_modal);
 		alertModal.setVisibility(View.VISIBLE);
 	}
 	
 	private void tearDownModal(){
 		RelativeLayout blackOutLayer = (RelativeLayout)findViewById(R.id.settings_fog);
 		RelativeLayout mainBackLayer = (RelativeLayout)findViewById(R.id.object_detail_main);
 		ScrollView scroller = (ScrollView)findViewById(R.id.object_detail_scroll_view);
 		
 		mainBackLayer.setEnabled(true);
 		scroller.setEnabled(true);
 		addToList.setEnabled(true);
 		favoriteStar.setEnabled(true);
 		saveButton.setEnabled(true);
 		clearButton.setEnabled(true);
 		dateInput.setEnabled(true);
 		timeInput.setEnabled(true);
 		locationInput.setEnabled(true);
 		equipmentInput.setEnabled(true);
 		seeingInput.setEnabled(true);
 		transInput.setEnabled(true);
 		notesInput.setEnabled(true);
 		blackOutLayer.setVisibility(View.INVISIBLE);
 		RelativeLayout alertModal = (RelativeLayout)findViewById(R.id.alert_modal);
 		alertModal.setVisibility(View.INVISIBLE);
 	}
 	
 	public void updateModalTextOne(String text) {
 		modalSelectorTextOne.setText(text);
 	}
 	
 	public void updateModalTextTwo(String text) {
 		modalSelectorTextTwo.setText(text);
 	}
 	
 	public void updateModalTextThree(String text) {
 		modalSelectorTextThree.setText(text);
 	}
     
 	////////////////////////////////////
 	// Modal List Inflation Utilities //
 	////////////////////////////////////
 	
 	static class IndividualItem {
 		String optionText;
 		boolean selected;
 		
 		IndividualItem(String text, boolean selected) {
 			optionText = text;
 			this.selected = selected;
 		}
 		
 		String getName() {
 			return optionText;
 		}
 		
 		boolean getSelected() {
 			return selected;
 		}
 	}
 	
 	class IndividualItemAdapter extends ArrayAdapter<IndividualItem>{
 		
 		int listLayout;
 		ArrayList<IndividualItem> list;
 		
 		IndividualItemAdapter(Context context, int listLayout, ArrayList<IndividualItem> list){
 			super(context, listLayout, R.id.filter_option, list);
 			this.listLayout = listLayout;
 			this.list = list;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent){
 			IndividualItemWrapper wrapper = null;
 			
 			if (convertView == null){
 				convertView = getLayoutInflater().inflate(listLayout, null);
 				wrapper = new IndividualItemWrapper(convertView);
 				convertView.setTag(wrapper);
 			}
 			else{
 				wrapper = (IndividualItemWrapper)convertView.getTag();
 			}
 			
 			wrapper.populateFrom(getItem(position));
 			
 			return convertView;
 		}
 		
 		public ArrayList<IndividualItem> getList() {
 			return list;
 		}
 	}
 	
 	class IndividualItemWrapper{
 		
 		private TextView filterOption = null;
 		private ImageView icon = null;
 		private View row = null;
 		
 		IndividualItemWrapper(View row){
 			this.row = row;
 		}
 		
 		TextView getItemOption(){
 			if (filterOption == null){
 				filterOption = (TextView)row.findViewById(R.id.filter_option);
 			}
 			return filterOption;
 		}
 		
 		ImageView getIcon(){
 			if (icon == null){
 				icon = (ImageView)row.findViewById(R.id.checkbox);
 			}
 			return icon;
 		}
 		
 		void populateFrom(IndividualItem item){
 			getItemOption().setText(item.getName());
 			if(item.getSelected()) {
 				getIcon().setImageResource(settingsRef.getCheckbox_Selected());
 			} else {
 				getIcon().setImageResource(settingsRef.getCheckbox_Unselected());
 			}
 		}
 	}
 }
