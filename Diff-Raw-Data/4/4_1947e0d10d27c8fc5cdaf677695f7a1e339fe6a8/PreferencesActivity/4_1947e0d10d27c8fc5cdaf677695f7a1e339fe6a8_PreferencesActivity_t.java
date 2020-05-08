 package ch.unibe.scg.team3.wordfinder;
 
import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import ch.unibe.scg.team3.localDatabase.DataManager;
 /**
  * 
  * @author nils
  * 
  */
public class PreferencesActivity extends Activity {
 	DataManager db;
 	String options[] = {};
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preferences);
 		db = new DataManager(this);
 //		db.getWritableDatabase();
 //		db.close();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.preferences, menu);
 		return true;
 	}
 	public void resetDB(View view){
 		db.reset(this);
 		finish();
 	}
 
 }
