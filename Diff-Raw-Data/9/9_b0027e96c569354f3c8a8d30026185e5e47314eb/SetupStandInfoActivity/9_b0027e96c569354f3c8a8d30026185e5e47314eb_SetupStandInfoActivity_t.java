 package edu.upenn.cis.fruity;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class SetupStandInfoActivity extends Activity {
 
 	public static final int InventoryPreprocessActivity_ID = 8;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_setup_stand_info);
 		Intent intent = getIntent();
 		
 		// Set school title from prior screen
 		String schoolName = (String) intent.getExtras().get("schoolName");
 		TextView schoolNameView = (TextView) findViewById(R.id.standInfo_schoolName);
 		schoolNameView.setText(schoolName);
 		
 		// Populate weather choice spinner
 		Spinner weatherInput = (Spinner) findViewById(R.id.standInfo_weatherInput);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 		        R.array.weather_options, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		weatherInput.setAdapter(adapter);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	// TODO: Take data from input boxes and log.
 	public void onInventoryPreprocessButtonClick(View v){
 		EditText dateIn = (EditText) findViewById(R.id.standInfo_dateInput);
		//EditText tempIn = (EditText) findViewById(R.id.standInfo_temperatureInput);
		//EditText weatherIn = (EditText) findViewById(R.id.standInfo_weatherInput);
 		EditText cashIn = (EditText) findViewById(R.id.standInfo_cashBoxInput);
 		EditText volunteerIn = (EditText) findViewById(R.id.standInfo_volunteersInput);
 		
 		// TODO: Figure out how we are going to format, store this info.
		//String temperature = tempIn.getText().toString();
		//String weather = weatherIn.getText().toString();
 		String cashBoxCount = cashIn.getText().toString();
 		String volunteerCount = volunteerIn.getText().toString();
 		
 		Intent i = new Intent(this, InventoryPreprocessActivity.class);
 		startActivityForResult(i, InventoryPreprocessActivity_ID);
 	}
 }
