 package info.ohgita.bincalc_android;
 
 import android.os.Bundle;
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockPreferenceActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 public class Activity_preference extends SherlockPreferenceActivity {
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
 		addPreferencesFromResource(R.xml.preference);
 	}
 	
 	@Override  
 	public boolean onOptionsItemSelected(MenuItem item) {  
 		switch(item.getItemId()) {  
 			case android.R.id.home:  
 				finish();  
 				return true;  
 		}
 		return super.onOptionsItemSelected(item);  
 	}  
 	
 }
