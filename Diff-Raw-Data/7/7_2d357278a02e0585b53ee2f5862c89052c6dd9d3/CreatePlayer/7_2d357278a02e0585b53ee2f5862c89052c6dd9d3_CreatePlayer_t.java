 /**
  * NEW COMMENT TO TEST GIT
  */
 package com.sportsboards2d;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
import android.util.*;
 
 
 /**
  * Coded by Nathan King
  */
 
 /**
  * Copyright 2011 5807400 Manitoba Inc. All rights reserved.
  */
 public class CreatePlayer extends Activity implements OnItemSelectedListener{
 	
 	private EditText textFirstName;
 	private EditText textLastName;
 	private EditText jerseyNumber;
 	private Spinner spinner;
 	
 	private int arrayID;
 
 	private boolean validFirstName = false;
 	private boolean validLastName = false;
 	private boolean validJerseyNumber = false;
 	private boolean validPosition = false;
 	
	private static final String TAG = "CreatePlayer";
	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		 super.onCreate(savedInstanceState);
 		 setContentView(R.layout.createplayer);
 		 arrayID = getIntent().getExtras().getInt("arrayID");
          textFirstName = (EditText)findViewById(R.id.playername_first_edit);
          textLastName = (EditText)findViewById(R.id.playername_last_edit);
          jerseyNumber = (EditText)findViewById(R.id.playernum_edit);
          spinner = (Spinner) findViewById(R.id.spinner);
          ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                  this, arrayID, android.R.layout.simple_spinner_item);
          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          spinner.setAdapter(adapter);
          spinner.setOnItemSelectedListener(this);
          textFirstName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
         	 @Override
         	 public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         		 if (actionId == EditorInfo.IME_ACTION_DONE) {
         			 
         			 boolean result = v.getText().toString().matches(getString(R.string.regex_first_name));
         			 System.out.println(v.getText());
         			 System.out.println(result);
        			 Log.v(TAG, "text=" + v.getText());
        			 Log.v(TAG, "result=" + result);
         			 
         			 if(result){
         				 validFirstName = true;
         			 }
         			 
         		 }
         		 return false;
         	 }
          }
          );
          textLastName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
         	 @Override
         	 public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         		 if (actionId == EditorInfo.IME_ACTION_DONE) {
         			 boolean result = v.getText().toString().matches(getString(R.string.regex_last_name));
         			 System.out.println(v.getText());
         			 System.out.println(result);
         			 if(result){
         				 validLastName = true;
         			 }
         		 }
         		 return false;
         	 }
          }
          );
          jerseyNumber.setOnEditorActionListener(new EditText.OnEditorActionListener() {
         	 @Override
         	 public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         		 if (actionId == EditorInfo.IME_ACTION_DONE) {
         			 boolean result = v.getText().toString().matches(getString(R.string.regex_jerseynum));
         			 System.out.println(v.getText());
         			 System.out.println(result);
         			 if(result){
             			 validJerseyNumber = true;
             		 }
         		 }
         		 return false;
         	 }
          }
          );
 	}
 	
 	public void okClicked(View v){
 		
 		PlayerInfo newPlayer;
 		
 		if(validFirstName && validLastName && validJerseyNumber && validPosition){
 			int num = Integer.parseInt(jerseyNumber.getText().toString());
 			String type = spinner.getSelectedItem().toString();
 			System.out.println(type);
 			String name = textFirstName.getText().toString() + " " + textLastName.getText().toString();
 			newPlayer = new PlayerInfo(BaseBoard.playerIDCounter, num, type, name);
 			Intent result = new Intent();
 			result.putExtra(getString(R.string.players_create), newPlayer);
 			setResult(5, result);
 			this.finish();
 		}
 		else{
 			Toast toast = Toast.makeText(getApplicationContext(), "Invalid input. Try something like John Smith, #27, PG", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 	}
 	
 	public void cancelClicked(View v){
 		this.setResult(-1, null);
 		this.finish();
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
 		validPosition = true;
 		System.out.println(parent.getItemAtPosition(pos).toString());
 	}
 
 	
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {}
 	
 }
