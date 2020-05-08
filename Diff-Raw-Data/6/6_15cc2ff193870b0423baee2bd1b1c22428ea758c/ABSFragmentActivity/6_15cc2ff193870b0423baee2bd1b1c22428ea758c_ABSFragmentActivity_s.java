 package com.example.sprint;
 
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.compuware.apm.uem.mobile.android.CompuwareUEM;
 
 public class ABSFragmentActivity extends SherlockFragmentActivity {
 
 	private ActionBar actionBar;
 	private final static int BARCODE_SCAN_REQUEST = 2345;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// initialize actionBar
 		actionBar = getSupportActionBar();
 
 		actionBar.setTitle(getResources().getString(R.string.app_name));
 		actionBar.setIcon(R.drawable.printer_icon);
 		actionBar.setHomeButtonEnabled(true);
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		actionBar.setBackgroundDrawable(new ColorDrawable(0xfff99c20));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		// TODO Auto-generated method stub
 		getSupportMenuInflater().inflate(R.menu.main_sherlock, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case android.R.id.home:
 			goHome();
 			return true;
 			
 		case R.id.menu_settings:
 			startPreferences();
 			return true;
 		case R.id.menu_scan:
 			startScanner();
 			return true;
 
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	protected void startScanner() {
 		Intent i = new Intent();	
 		/* setting the action string.  No other apps should respond to this request
 		 * because of the unique action string
 		 */
 		i.setAction("com.compuware.pdp.sprint");
 		i.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		startActivityForResult(i, BARCODE_SCAN_REQUEST);	
		
 	}
 
 	protected boolean startPreferences() {
 		/* Start preferences for versions below android 3.0 */
 		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
 			startActivity(new Intent(this, EditPreferences.class));
 			return true;
 		}
 		/* Start preferences for android 3.0 + */
 		else {
 			Intent intent = new Intent( this, EditPreferencesHC.class );
 			/* Adding extras to skip showing headers in the preference activity */
 			intent.putExtra( EditPreferencesHC.EXTRA_SHOW_FRAGMENT, StockPreferenceFragment.class.getName() );
 			intent.putExtra( EditPreferencesHC.EXTRA_NO_HEADERS, true );
 			startActivity(intent);
 			return true;
 		}
 		
 	}
 	
 	@Override 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.v("ABS", "request code: " + requestCode);
     	if(requestCode == BARCODE_SCAN_REQUEST && resultCode == RESULT_OK) {
     		String contents = data.getStringExtra("SCAN_RESULT");
     		
     		//dynaTrace End Scanner
     		CompuwareUEM.leaveAction("scannerActivity");
     		
     		startJobListActivity(contents);
     	}
 	}
 	
 	
 
 	protected void startJobListActivity(String printerName) {
 		Intent intent = new Intent(this, JobListActivity.class);
 		intent.putExtra("printer_name", printerName);
 		startActivity(intent);
 		
 	}
 
 	protected void goHome() {
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 
 }
