 package org.kercoin.tekku.carto;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 
 public class Cartograph extends Activity implements OnClickListener, OnMenuItemClickListener {
 	
 	private static final int OPTION = 0x01;
 	private static final int GPS = 0x02;
 	
 	private transient ArrayAdapter<CharSequence> networkAdapter;
 	private transient ArrayAdapter<CharSequence> lineAdapter;
 
 	private transient ArrayAdapter<CharSequence> directionAdapter;
 	private transient ArrayAdapter<CharSequence> stationAdapter;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	load1of2();
     }
 
 	private void load1of2() {
 		setContentView(R.layout.main1of2);
 		if (networkAdapter == null)
 			networkAdapter = createAdapter(R.array.networks);
 		Spinner network = (Spinner) findViewById(R.id.network);
 		network.setAdapter(networkAdapter);
 
 		if (lineAdapter == null)
 			lineAdapter = createAdapter(R.array.lines);
 		Spinner line = (Spinner) findViewById(R.id.line);
 		line.setAdapter(lineAdapter);
 		
 		Button next1of2 = (Button) findViewById(R.id.btn_1of2_next);
 		next1of2.setOnClickListener(this);
 	}
 	
 	private void load2of2() {
 		setContentView(R.layout.main2of2);
 		if (directionAdapter == null)
 			directionAdapter = createAdapter(R.array.directions);
 		Spinner direction = (Spinner) findViewById(R.id.direction);
 		direction.setAdapter(directionAdapter);
 		
 		if (stationAdapter == null)
 			stationAdapter = createAdapter(R.array.stations);
 		Spinner departure = (Spinner) findViewById(R.id.departure);
 		departure.setAdapter(stationAdapter);
 		Spinner arrival = (Spinner) findViewById(R.id.arrival);
 		arrival.setAdapter(stationAdapter);
 		
 		Button previous2of2 = (Button) findViewById(R.id.btn_2of2_previous);
 		previous2of2.setOnClickListener(this);
 		Button go = (Button) findViewById(R.id.btn_2of2_go);
 		go.setOnClickListener(this);
 	}
 	
 	private ArrayAdapter<CharSequence> createAdapter(int array) {
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 			this, array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		return adapter;
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	boolean superResult = super.onCreateOptionsMenu(menu);
     	MenuItem option = menu.add(Menu.NONE, OPTION, Menu.NONE, R.string.menu_option);
     	option.setIcon(android.R.drawable.ic_menu_preferences);
     	MenuItem gps = menu.add(Menu.NONE, GPS, Menu.NONE, R.string.menu_gps);
     	gps.setIcon(android.R.drawable.ic_menu_compass);
     	gps.setOnMenuItemClickListener(this);
 		return superResult;
     }
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btn_1of2_next:
 			load2of2();
 			break;
 		case R.id.btn_2of2_previous:
 			load1of2();
 			break;
 		case R.id.btn_2of2_go:
 			startActivity(new Intent(this, Measure.class));
 			break;
 		}
 	}
 
 	@Override
 	public boolean onMenuItemClick(MenuItem item) {
 		switch (item.getItemId()) {
 		case GPS:
 			startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)); 
 			return true;
 		case OPTION:
 			Log.i("Tekku", "Options");
 			return true;
 		}
 		return false;
 	}
 		
 }
