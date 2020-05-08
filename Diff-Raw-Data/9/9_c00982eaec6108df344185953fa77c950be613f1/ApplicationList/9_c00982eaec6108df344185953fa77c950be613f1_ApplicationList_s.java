 package edu.buffalo.cse.phonelab.phonelabservices;
 
 import java.util.ArrayList;
 
 import javax.xml.xpath.XPathExpressionException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import edu.buffalo.cse.phonelab.manifest.PhoneLabApplication;
 import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
 import edu.buffalo.cse.phonelab.utilities.Util;
 
 /**
  *  GUI layout to provide application listing.
  *  
  * @author Muhammed Fatih Bulut
  *
  * mbulut@buffalo.edu
  */
 public class ApplicationList extends ListActivity {
 	private ArrayList<PhoneLabApplication> applications = null;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.application_list_grid);
 
 		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
 		try {
 			if (manifest.getManifest()) {
 				applications = manifest.getAllApplications();
 				String[] values = new String[applications.size()];
 				String[] descriptions = new String[applications.size()];
 				for (int i = 0;i < applications.size();i++) {
 					values[i] = applications.get(i).getName();
 					descriptions[i] = applications.get(i).getDescription();
 				}
 				ApplicationListArrayAdapter adapter = new ApplicationListArrayAdapter(this, values, descriptions);
 				setListAdapter(adapter);
 			}
 		} catch (XPathExpressionException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		String item = (String) getListAdapter().getItem(position);
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "Item: " + item + "Position: " + position + " id: " + id);
 		if (applications != null) {
 			PhoneLabApplication app = applications.get(position);
 			if (app.getPackageName() != null) {
 				startingapplication(app);
 			}
 		}
 	}
 	
 
 	/**
 	 * Used to send manually start intents using android.intent.action.MAIN. 
 	 * @param app the phonelab Application
 	 * @param dbAdapter the hook to device database          
 	 * @author rishi baldawa
 	 */
 	private void startingapplication(PhoneLabApplication app) {
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "Starting " + app.getName() + " now...");
 
 		Intent startAppIntent = new Intent(Intent.ACTION_MAIN);
 		PackageManager manager = getPackageManager();
 		try {
 			startAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
 			startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 			startActivity(startAppIntent);
 		} catch( Exception e ) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
 		}
 	}
 }
