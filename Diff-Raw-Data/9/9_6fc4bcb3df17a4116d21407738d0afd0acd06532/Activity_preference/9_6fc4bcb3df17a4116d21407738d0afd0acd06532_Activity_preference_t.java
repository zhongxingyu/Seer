 package info.ohgita.bincalc_android;
 
import android.annotation.SuppressLint;
 import android.os.Bundle;
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockPreferenceActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 public class Activity_preference extends SherlockPreferenceActivity {
	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		addPreferencesFromResource(R.xml.preference);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
