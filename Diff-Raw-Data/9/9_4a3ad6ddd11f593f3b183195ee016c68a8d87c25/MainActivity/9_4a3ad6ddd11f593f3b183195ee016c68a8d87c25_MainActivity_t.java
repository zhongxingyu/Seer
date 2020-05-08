 package com.agilevent.repairshop;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity {
     
 	private VehiclesListFragment vlf;  
 	private VehicleDamageWaiverFragment vdf; 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // Get the fragments so we can work with them. 
         vlf = (VehiclesListFragment)getSupportFragmentManager().findFragmentById(R.id.vehicles);
         vdf = (VehicleDamageWaiverFragment)getSupportFragmentManager().findFragmentById(R.id.damage_waiver); 
         
         // Set the waiver as the listener. 
        vlf.setVechicleListener(vdf);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.main, menu);
     	return true; 
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.about: 
     			Toast.makeText(this, getString(R.string.about_content), Toast.LENGTH_LONG).show(); 
     			return true; 
     		case R.id.reset:
     			vlf.reset(); 
     			return true;
     		default: 
     			return super.onOptionsItemSelected(item);
     	}
     	
     }
 }
