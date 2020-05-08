 package com.sgs.hotelguru;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
import android.widget.Toast;
 
 public class EnglishMain extends Activity {
 
 	myDatabase db;
 	String mySelectedCL;
 	String GlobalCruiseLine;
 	String GlobalShipName;
 	String GlobalShipDeck;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_english_main);
 		db = new myDatabase(getApplicationContext());
 		this.SetCruiseLine();
 	}
 	
	public void onSpinnerSubmit(View v){
	
		Intent i = new Intent(getApplicationContext(), MapsExample.class);
		i.putExtra("GlobalCruiseLine",GlobalCruiseLine);
		i.putExtra("GlobalShipName", GlobalShipName);
		i.putExtra("GlobalShipDeck", GlobalShipDeck);
		startActivity(i);
		Toast.makeText(EnglishMain.this,"Preparing to move to selected Deck Plans for "+GlobalCruiseLine+" "+GlobalShipName+" "+GlobalShipDeck,
                Toast.LENGTH_SHORT).show();
	}
	
 public void SetCruiseLine()
 {
 	ArrayList<String> myCruiseLines = new ArrayList<String>();
 	myCruiseLines = db.getCruiseLines();
 	
 	Spinner spinner = (Spinner)findViewById(R.id.CruiseLineSpinner);
 	ArrayAdapter<String> spinnerArrayAdapterCLS = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, myCruiseLines);
 	spinner.setAdapter(spinnerArrayAdapterCLS);
 	spinner.setOnItemSelectedListener(new planOnClickListener());
 	
 }
 public void SetCruiseShip(String myCruiseLine)
 {
 	ArrayList<String> myCruiseShips = new ArrayList<String>();
 	myCruiseShips = db.getCruiseShips(myCruiseLine);
 	Spinner shipSpinner = (Spinner) findViewById(R.id.ShipSelectSpinner);
 	ArrayAdapter<String> spinnerArrayAdapterCLS = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, myCruiseShips);
 	shipSpinner.setAdapter(spinnerArrayAdapterCLS);
 	shipSpinner.setOnItemSelectedListener(new cruiseShipOnClickListener());
 }
 public void SetShipDeck(String myShip){
 	ArrayList<String> myShipDecks = new ArrayList<String>();
 	myShipDecks = db.getNumDecks(myShip);
 	Spinner DecksSpinner = (Spinner) findViewById(R.id.DeckSelectSpinner);
 	ArrayAdapter<String> spinnerArrayAdapterDCS = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, myShipDecks);
 	DecksSpinner.setAdapter(spinnerArrayAdapterDCS);
 	DecksSpinner.setOnItemSelectedListener(new DeckOnClickListener());
 }
 		
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.english_main, menu);
 		return true;
 	}
 
 	public class planOnClickListener implements OnItemSelectedListener {
 
 	    @Override
 	   public void onItemSelected(AdapterView<?> parent, View v, int pos,
 	           long id) {
 
 	       String mySelectedCL = parent.getItemAtPosition(pos).toString();
 	       GlobalCruiseLine = mySelectedCL;
 	       Log.v("ENGLISHMAIN","Selected Cruise line is "+mySelectedCL);
 	       SetCruiseShip(mySelectedCL);
 
 	      
 	}
 	   @Override
 	   public void onNothingSelected(AdapterView<?> arg0) {
 	       // TODO Auto-generated method stub
 
 	   }
 	}
 	public class cruiseShipOnClickListener implements OnItemSelectedListener {
 
 	    @Override
 	   public void onItemSelected(AdapterView<?> parent, View v, int pos,
 	           long id) {
 
 	      String myShip = parent.getItemAtPosition(pos).toString();
 	      GlobalShipName = myShip;
 	       SetShipDeck(myShip);
 
 	      
 	}
 	   @Override
 	   public void onNothingSelected(AdapterView<?> arg0) {
 	       // TODO Auto-generated method stub
 
 	   }
 	}
 	public class DeckOnClickListener implements OnItemSelectedListener {
 
 	    @Override
 	   public void onItemSelected(AdapterView<?> parent, View v, int pos,
 	           long id) {
 
 	       String myDeck = parent.getItemAtPosition(pos).toString();
 	       GlobalShipDeck = myDeck;
 
 	      
 	}
 	   @Override
 	   public void onNothingSelected(AdapterView<?> arg0) {
 	       // TODO Auto-generated method stub
 
 	   }
 	}
 }
 
 
 
