 /****************************************
  * SearchFormActivity
  * The inital activity.
  * This activity runs the search form.
  * Upon hitting the submit button, it uses RequestTask
  * 	to create an ArrayList of Profiles.
  * 	It then launches a ListActivityIntent to display them.
  * **************************************/
 
 package edu.grinnell.appdev.grinnelldirectory;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
public class SearchFormActivity extends FragmentActivity implements RequestTask.ParserErrorMessage{
 
    	
    	int parserErrorMessage = RequestTask.NO_ERROR;
    
 	// Fields in the layout
 	TextView firstNameText;
 	TextView lastNameText;
 	TextView usernameText;
 	TextView phoneText;
 	TextView campusAddressText;
 	TextView homeAddressText;
 	Spinner facDeptSpinner;
 	Spinner studentMajorSpinner;
 	Spinner concentrationSpinner;
 	Spinner sgaSpinner;
 	Spinner haitusSpinner;
 	Spinner studentClassSpinner;
 	Button submitButton;
 
 	// An intent for ProfileListActivity
 	Intent listIntent;
 
 	public static final int NETWORK = 3;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//TODO set view to splash screen
 		setContentView(R.layout.activity_search_form);
 
 		BasicSearchFragment simpleSearch = new BasicSearchFragment();
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.fragment_container, simpleSearch).commit();
 		
 		ConnectivityManager cm = (ConnectivityManager)
 				this.getSystemService(Context.CONNECTIVITY_SERVICE);
 		//check connections before downloading..
 
 		if (!networkEnabled(cm)){
 			noNetworkError();
 		}
 
 	}
 
 	// Converts plain-text strings into HTTP-friendly strings.
 	public String cleanString(String str) {
 		str = str.replace(" ", "+");
 		str = str.replace(",", "%2C");
 		str = str.replace("&", "%26");
 		if (str.length() > 36) {
 			str = str.substring(0, 35);
 		}
 		return str;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_search_form, menu);
 		return true;
 	}
 
 	/*
 	 * Return true if the device has a network adapter that is capable of
 	 * accessing the network.
 	 */
 	protected static boolean networkEnabled(ConnectivityManager connec) {
 		// ARE WE CONNECTED TO THE NET
 		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
 			// MESSAGE TO SCREEN FOR TESTING (IF REQ)
 			// Toast.makeText(this, connectionType + � connected�,
 			// Toast.LENGTH_SHORT).show();
 			return true;
 		} else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
 			return false;
 		}
 
 		return false;
 	}
 
 	public void noNetworkError() {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				this);
 		// set title
 		alertDialogBuilder.setTitle("No Internet Connection");
 		// set dialog message
 		alertDialogBuilder
 				.setPositiveButton("Exit",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 								finish();
 							}
 						});
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		// show it
 		alertDialog.show();
 	}

	@Override
	public void setErrorMessage(int message) {
	    parserErrorMessage = message;
	}
 }
