 package org.vandymobile.dining;
 
 import com.google.android.maps.MapActivity;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 
 public class DiningMap extends MapActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dining_map_large);
         //getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_dining_map, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
 
 }
