 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Julia Gustafsson
  *
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.plingnote;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * A note fragment activity with a layout holding a fragment. 
  * @author Julia Gustafsson
  */
 public class ActivityNote extends FragmentActivity {
 	private int id = -1;
 	private Location location= new Location(0.0,0.0);
 	private FragmentSnotebar snotebarFragment = null;
 	private Fragment anotherFragment = null;
 	private boolean deleteNote = false;
 	private String address = "";
 	private DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 
 	/**
 	 * Set content view and try to fetch id from saved instance or intent,
 	 * Decide which fragment to be shown 'anotherFragment' or new fragment
 	 * and if new fragment should be instanced and added again. 
 	 * Fetching values from database.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_note);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		try{
 			if(getIntent().getExtras().getBoolean(IntentExtra.justId.toString()) == false){
 				//Try to set location to intent extra values.
 				//If exist a the local location is set to the extra values long and lat
 				this.location = new Location(getIntent().getExtras().getDouble(IntentExtra.longitude.toString())
 						,getIntent().getExtras().getDouble(IntentExtra.latitude.toString()));
 				this.address = getIntent().getExtras().getString(IntentExtra.city.toString());
 			}
 		}catch(Exception e){			
 		}
 		try{
 			//Try to set id to saved instance. Then fetch this id's values. This happens if the screen is turned.
 			this.id =savedInstanceState.getInt(IntentExtra.id.toString());
 		}catch(Exception e){
 		}
 		try { 
 			//Try to set antoherfragment to to fragment saved in savedinstance. 
 			//If turning to phone when when to snotebar 
 			//is replaced with some other fragment this will set antotherfragment to that.
 			this.anotherFragment = getSupportFragmentManager().getFragment(savedInstanceState, "anotherFragment");
 		}catch(Exception e){
 			if(snotebarFragment == null){
 				try{
 					if(this.id == -1){
 						//If an intent with id (that not is -1) the id will be setted here. Fetch values from the id.
 						if(getIntent().getExtras().getInt(IntentExtra.id.toString()) != -1){ 
 							this.id = getIntent().getExtras().getInt(IntentExtra.id.toString());
 						}
 						if(getIntent().getExtras().getBoolean(IntentExtra.reminderDone.toString()) == true){
 							 dbHandler.updateAlarm(this.id, "");
 						}
 					}
 				}catch(Exception el){
 				}
 				//Made a new snotebar and add an intent with id if this note's id is setted
 				FragmentSnotebar newFragment = new FragmentSnotebar();
 				newFragment.setRetainInstance(true);
 				if(this.id != -1){
 					Bundle args = new Bundle();
 					args.putInt(IntentExtra.id.toString(), this.id);
 					newFragment.setArguments(args);
 				}
 				FragmentManager fragmentManager = getSupportFragmentManager();
 				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 				fragmentTransaction.attach(newFragment);
 				fragmentTransaction.add(R.id.fragmentcontainer, newFragment);
 				fragmentTransaction.commit();
 			}
 		}
 	}
 
 	/**
 	 * Sets widht and height of the edittext for the notes title by using the screens widht and height.
 	 */
 	public void setNoteTitleLayoutParam(){
 		EditText noteTitle = (EditText)findViewById(R.id.notetitle);
 		Rect rec = Utils.getScreenPixels(this);
 		int height = rec.height();
 		int widht = rec.width();	
 		getResources().getConfiguration();
 		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
 			noteTitle.setLayoutParams(new LinearLayout.LayoutParams(widht, height/7));
 		}
 		else{
 			noteTitle.setLayoutParams(new LinearLayout.LayoutParams(widht, height/11));
 		}	
 	}
 
 	/**
 	 * Set onkeylistner to the edittext field representing the note's title.
 	 * If the keycode is enter to focus goes to the edittext field representing the note's text.
 	 */
 	public void setKeyListenertoTitle(){
 		EditText noteTitle = (EditText)findViewById(R.id.notetitle);
 		noteTitle.setOnKeyListener(new View.OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				switch(keyCode) {
 				case KeyEvent.KEYCODE_ENTER:		    
 					EditText noteText = (EditText)findViewById(R.id.notetext);
 					noteText.setFocusable(true);
 					noteText.requestFocus();
 					return true;
 				default:
 					break;
 				}
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * Sets widht and height of the edittext for the notes text by using the screens widht and height
 	 */
 	public void setNoteTextLayoutParam(){
 		EditText noteText = (EditText)findViewById(R.id.notetext);
 		Rect  rec = Utils.getScreenPixels(this);
 		int height = rec.height();
 		int widht = rec.width();	
 		getResources().getConfiguration();
 		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
 			int h = height/3;
 			noteText.setLayoutParams(new LinearLayout.LayoutParams(widht, h));
 		}
 		else{
 			int h = height/9;
 			noteText.setLayoutParams(new LinearLayout.LayoutParams(widht, h*4));
 		}
 	}
 
 	/**
 	 * If id is set this method set the text of this note in the edittext field for the text that is saved in the database
 	 */
 	public void setNoteText(){
 		EditText noteText = (EditText)findViewById(R.id.notetext);
 		//If this class was opened with an intent or saved instances, the note text will get the text from the database
 		if(this.id != -1){				
 			String txt = dbHandler.getNote(this.id).getText();
 			//The cursor position will be saved
 			//even if turning the phone horizontal. Doesn't work with just setText or setSelection(noteText.getText().length()) if turning phone horizontal.
 			noteText.setText(""); 
 			noteText.append(txt);
 			noteText.invalidate(); 
 		}
 	}
 
 	/**
 	 * If id is set this method set the title of this note in the edittext field for the title that is saved in the database
 	 */
 	public void setNoteTitle(){
 		EditText noteTitle = (EditText)findViewById(R.id.notetitle);
 		//If this class was opened with an intent or saved instances, the note text will get the text from the database
 		if(this.id != -1){
 			String title =  dbHandler.getNote(this.id).getTitle();
 			noteTitle.setText(title);		
 			noteTitle.invalidate(); 
 		}
 	}
 
 	/**
 	 *Set the view noteText text to the latest inserted note's text if is during editing.
 	 *Sets the curser of the view noteText to the bottom of the text.
 	 */
 	@Override
 	public void onStart(){
 		super.onStart();
 		setNoteTitleLayoutParam();
 		setNoteTextLayoutParam();
 		setNoteText();	
 		setNoteTitle();
 		setNoteAddress();
 		setKeyListenertoTitle();
 	}
 
 	/**
 	 * Set the address if it isn't an empty string and if this note has a id bigger than -1.
 	 */
 	public void setNoteAddress(){
 			if(!(this.address.equals(""))){
 				TextView textView = (TextView) findViewById(R.id.address);
 				textView.setText(this.address);
		}else if(this.id != -1){
			if(!(dbHandler.getNote(this.id).getAddress().equals(""))){
				TextView textView = (TextView) findViewById(R.id.address);
				textView.setText(this.address);
			}
					
 		}
 	}
 
 	/**
 	 * Remove snotebarFragment and replace with the param fragment.
 	 * @param fragment
 	 */
 	public void replaceFragment(Fragment fragment){
 		this.saveToDatabase();
 		this.anotherFragment= fragment;
 		getSupportFragmentManager().beginTransaction().replace(R.id.fragmentcontainer, anotherFragment).commit();
 	}	
 
 	/**
 	 * Update the database with the pluginfragment values by checking what kind of plguin it is.
 	 * Makes a new snotebar and set id as argument.
 	 * Remove the pluginfragment and the new one.
 	 * @param fragment
 	 */
 	public void replaceFragmentBack(PluginableFragment fragment){
 		if(fragment.getValue() != null){
 			if(fragment.getKind().equals(NoteExtra.REMINDER)){
 				dbHandler.updateAlarm(this.id, fragment.getValue());
 			}
 			if(fragment.getKind().equals(NoteExtra.IMAGE)){
 				dbHandler.updateImagePath(this.id, fragment.getValue());
 			}
 			if(fragment.getKind().equals(NoteExtra.CATEGORY)){
 				dbHandler.updateCategory(this.id, fragment.getCategory());
 			}
 		}
 		this.changeToSnotebarFragment();
 	}	
 
 	/**
 	 * if the boolean deletenote is false the information will be saved to the database.
 	 */
 	@Override
 	public void onPause(){
 		super.onPause();
 		if(deleteNote == false){
 			if(!isNoteEmpty())
 				this.saveToDatabase();
 		}
 	}
 
 	/**
 	 * A function that checks if all value; text,title,reminder,location,imagepath, is not setted.
 	 * @return
 	 */
 	public boolean isNoteEmpty(){
 		if(this.id != -1){
 			if((dbHandler.getNote(this.id).getAlarm().equals("") 
 				|| dbHandler.getNote(this.id).getAlarm() == null)
 				&& (dbHandler.getNote(this.id).getImagePath().equals("") 
 				|| dbHandler.getNote(this.id).getImagePath() == null) 
 				&& (dbHandler.getNote(this.id).getCategory() == NoteCategory.NO_CATEGORY) 
 				&& (dbHandler.getNote(this.id).getLocation() == null)
 				&& this.getTextofNoteText().equals("") && this.getTextofNoteText().equals(""))
 				return true;
 			}
 		return false;
 	}
 
 	/**
 	 * Return the id 
 	 * @return id
 	 */
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * Check if extra value is setted by checking the local extra values
 	 * @param noteExtra
 	 * @return
 	 */
 	public boolean checkIfValueIsSetted(NoteExtra noteExtra){
 		if(this.id != -1){
 			if(noteExtra.equals(NoteExtra.REMINDER)){
 				if(!(dbHandler.getNote(this.id).getAlarm().equals(""))){
 					return true;
 				}
 			}
 			if(noteExtra.equals(NoteExtra.IMAGE)){
 				if(!(dbHandler.getNote(this.id).getImagePath().equals(""))){
 					return true;
 				}
 			}
 			if(noteExtra.equals(NoteExtra.LOCATION)){
 				if(!(dbHandler.getNote(this.id).getLocation() == null)){
 					return true;
 				}
 			}	
 			if(noteExtra.equals(NoteExtra.CATEGORY)){
 				if(!(dbHandler.getNote(this.id).getCategory() == NoteCategory.NO_CATEGORY)){
 					return true;
 				}
 			}	
 		}
 		return false;
 	}
 
 	/**
 	 * Delete value from database
 	 * @param noteExtra
 	 */
 	public void deleteValue(NoteExtra noteExtra){
 		if(noteExtra.equals(NoteExtra.REMINDER)){
 			dbHandler.updateAlarm(this.id, "");
 		}
 		if(noteExtra.equals(NoteExtra.IMAGE)){
 			dbHandler.updateImagePath(this.id, "");
 		}
 		if(noteExtra.equals(NoteExtra.LOCATION)){
 			dbHandler.updateLocation(this.id, null);
 			dbHandler.updateAddress(this.id, "");
 		}
 		if(noteExtra.equals(NoteExtra.CATEGORY)){
 			dbHandler.updateCategory(this.id, NoteCategory.NO_CATEGORY);
 		}	
 		this.changeToSnotebarFragment();
 	}
 
 	/**
 	 * Save to database, remove anotherFragment and replace it with snotbarFragment
 	 */
 	private void changeToSnotebarFragment(){
 		this.saveToDatabase();
 		snotebarFragment = new FragmentSnotebar();
 		try{
 			Bundle bundleToFrag = new Bundle();
 			bundleToFrag.putInt(IntentExtra.id.toString(), this.id);
 			snotebarFragment.setArguments(bundleToFrag);
 		} catch(Exception e){		        	
 		}
 		getSupportFragmentManager().beginTransaction().replace(R.id.fragmentcontainer, snotebarFragment).commit();
 		getSupportFragmentManager().beginTransaction().remove(anotherFragment);
 		this.anotherFragment = null;
 	}
 
 	/**
 	 * Get the text from the view noteText. If the id isn't -1the note's value will be updated in the database.
 	 * If not the note will be inserted in the database and the id will be saved. 
 	 */
 	public void saveToDatabase(){
 		if(this.id != -1){
 			dbHandler.updateText(this.id, this.getTextofNoteText());
 			dbHandler.updateTitle(this.id, this.getTitleofNoteText());
			dbHandler.updateLocation(this.id, this.location);
			dbHandler.updateAddress(this.id, this.address);
 		}
 		//If this class not was opened with an intent or saved instance, it'd id is set to -1 and we are inserting the note in database.
 		else if(this.id == -1){
 			dbHandler.
 			insertNote(this.getTitleofNoteText(), this.getTextofNoteText(), this.location, "", "", NoteCategory.NO_CATEGORY, this.address);
 			this.id = DatabaseHandler.getInstance(this).getLastId();
 		}
 	}
 
 	/**
 	 * Find the first line of the text in notetext in the layout and returning it as a string
 	 * @return String
 	 */
 	public String getTitleofNoteText(){
 		EditText noteText = (EditText) findViewById(R.id.notetitle);
 		if(noteText.getText().toString().length() > 0){
 			String txt = noteText.getText().toString();
 			String[] txtLines = txt.split("\n");
 			return txtLines[0]+"\n";
 		}else
 			return "";
 	}
 
 	/**
 	 * Find the text(without the title) in notetext in the layout  as a string
 	 * @return String
 	 */
 	public String getTextofNoteText(){
 		EditText noteText = (EditText) findViewById(R.id.notetext);
 		String txt = noteText.getText().toString();
 		if(txt.length() > 0){
 			return txt;
 		}else
 			return "";
 	}
 
 	/**
 	 * If the boolean 'deletenote' is false,thismethod will save the id and 'anotherFragment' if it isn't null.
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		if(deleteNote == false){
 			super.onSaveInstanceState(savedInstanceState);
 			savedInstanceState.putInt(IntentExtra.id.toString(), this.id);
 			//If anotherFragment isn't null is should be saved
 			if(anotherFragment != null){
 				getSupportFragmentManager().putFragment(savedInstanceState, "anotherFragment", anotherFragment);
 			}
 		}
 	}
 
 	/**
 	 * Makes the back button behave like the home button. Calling finish() if back button is pressed.
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			this.finish();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * If choosing to go back to home, the keyboard will be hided and call finish.
 	 * If choosing to delete note, the current note will be deleted and the note activity will be deleted
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {	
 		case android.R.id.home:
 			InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
 			this.finish();
 			return true;
 		case R.id.delete_note:
 			if(this.id!=-1)
 				dbHandler.deleteNote(this.id);
 			deleteNote = true;
 			this.finish();
 			return true;
 		case R.id.reset_snotebar:
 			this.deleteAllValues();
 			return true;
 		case R.id.clean_notetext:
 			this.deleteNoteTextandTitle();		
 			return true;
 		case R.id.clean_note:
 			this.deleteNoteTextandTitle();
 			this.deleteAllValues();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Create the settings menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_note, menu);
 		return true;
 	}
 
 	/**
 	 * Delete the note's text and title
 	 */
 	public void deleteNoteTextandTitle(){
 		EditText noteText = (EditText)findViewById(R.id.notetext);
 		noteText.setText(""); 
 		noteText.invalidate(); 
 		EditText noteTitle = (EditText)findViewById(R.id.notetitle);
 		noteTitle.setText(""); 
 		noteTitle.invalidate(); 
 		this.saveToDatabase();
 	}
 
 	/**
 	 * Delete all note extra values location,imagepath,reminder.
 	 */
 	public void deleteAllValues(){					
 		dbHandler.updateNote(this.id, "", "", null, "", "", NoteCategory.NO_CATEGORY, "");	
 		this.snotebarFragment = new FragmentSnotebar();
 		try{
 			Bundle bundleToFrag = new Bundle();
 			bundleToFrag.putInt(IntentExtra.id.toString(), this.id);
 			this.snotebarFragment.setArguments(bundleToFrag);
 		} catch(Exception e){		        	
 		}
 		getSupportFragmentManager().beginTransaction().replace(R.id.fragmentcontainer, snotebarFragment).commit();
 	}
 }
