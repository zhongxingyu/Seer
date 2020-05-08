 package at.roadrunner.android.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import at.roadrunner.android.R;
 import at.roadrunner.android.couchdb.CouchDBService;
 import at.roadrunner.android.couchdb.RequestWorker;
 import at.roadrunner.android.model.Log.LogType;
 import at.roadrunner.android.service.ReplicationService;
 import at.roadrunner.android.util.AppInfo;
 
 public class Roadrunner extends Activity {
 	// Intent for scanning
 	private static final String SCAN_INTENT = "com.google.zxing.client.android.SCAN";
 	private static final String SCAN_PACKAGE = "com.google.zxing.client.android";
 
 	private Intent _replicateServer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_roadrunner);
 		
 		showLoginDialog();
 		
 		// start CouchDB Service
 		CouchDBService.startCouchDB(this);
 		
 		// start Monitoring Service
 		_replicateServer = new Intent(this, ReplicationService.class);
 		startService(_replicateServer);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		CouchDBService.stopCouchDB(this);
 		stopService(_replicateServer);
 	}
 	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//ignore orientation change
		super.onConfigurationChanged(newConfig);
	}
 	
 	private void showLoginDialog() {
 		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		AlertDialog.Builder ld = new AlertDialog.Builder(this).setTitle("Login");
 	
 		View loginView = getLayoutInflater().inflate(R.layout.dialog_login, (ViewGroup)findViewById(R.id.dialog_login_root));
 		//EditText username = (EditText)findViewById(R.id.roadrunner_dialog_login_username);
 		//username.setText(prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER));
 		//EditText password = (EditText)findViewById(R.id.roadrunner_dialog_login_password);
 		//password.setText(prefs.getString("password", Config.ROADRUNNER_AUTHENTICATION_PASSWORD));
 		
 		ld.setView(loginView);
 		
 		// add buttons
 		ld.setPositiveButton(R.string.app_dialog_login, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.dismiss();
 			}
 		});
 		ld.setNegativeButton(R.string.app_dialog_cancel, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.dismiss();
 			}
 		});
 		
 		ld.show();
 		
 	}
 	
 	public void onScanClick(View view) {
 		scan();
 		openContextMenu(view);
 	}
 	
 	public void onDeliveriesClick(View view) {
 		//scanItem(LogType.UNLOAD);
 	}
 	
 	private void scan() {
 		if (AppInfo.isIntentAvailable(this, SCAN_INTENT)) {
 			Intent intent = new Intent(SCAN_INTENT);
 			intent.setPackage(SCAN_PACKAGE);
 			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 			
 			startActivityForResult(intent, 0);
 		} else {
 			// show dialog
 			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 			alertBuilder.setMessage(R.string.roadrunner_dialog_missingScanner);
 			alertBuilder.setCancelable(false);
 			alertBuilder.setPositiveButton(R.string.app_dialog_yes, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SCAN_PACKAGE));
 					startActivity(intent);
 				}
 			});
 			alertBuilder.setNegativeButton(R.string.app_dialog_no, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			});	
 			alertBuilder.show();
 		}
 	}
 	
 	/*
 	 * onActivityResult
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == 0) {
 			if (resultCode == RESULT_OK) {
 				final String item = intent.getStringExtra("SCAN_RESULT");
 				final RequestWorker reqWorker = new RequestWorker(this);
 				
 				// get layout
 				AlertDialog.Builder sd = new AlertDialog.Builder(this).setTitle("Perform Action");
 				View scanView = getLayoutInflater().inflate(R.layout.dialog_scan, (ViewGroup)findViewById(R.id.dialog_scan_root));
 				Button btnLoad = (Button) scanView.findViewById(R.id.roadrunner_dialog_scan_load);
 				Button btnUnload = (Button) scanView.findViewById(R.id.roadrunner_dialog_scan_unload);
 				Button btnView = (Button) scanView.findViewById(R.id.roadrunner_dialog_scan_view);
 				sd.setView(scanView);
 				
 				// create AlertDialog
 				final AlertDialog dialog = sd.create();
 				
 				btnLoad.setOnClickListener(new OnClickListener() {
 	                @Override
 	                    public void onClick(View v) {
 		                	// save the log
 		    				reqWorker.saveLog(item, LogType.LOAD);
 		    				dialog.dismiss();
 	                    }
 	                });
 				
 				btnUnload.setOnClickListener(new OnClickListener() {
 	                @Override
 	                    public void onClick(View v) {
 		                	// save the log
 		                	reqWorker.saveLog(item, LogType.LOAD);
 		                	dialog.dismiss();
 	                    }
 	                });
 				
 				btnView.setOnClickListener(new OnClickListener() {
 	                @Override
 	                    public void onClick(View v) {
 	                		dialog.dismiss();
 	                    }
 	                });
 				
 				// check if item is already loaded and modify the menu
 				if (new RequestWorker(this).isLocalDocumentExisting(item)) {
 					btnLoad.setEnabled(false);
 				} else {
 					btnUnload.setEnabled(false);
 					btnView.setEnabled(false);
 				}
 				
 				dialog.show();
 			} 
 		}
 	}
 
 	/*
 	 * inflate menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.roadrunner_menu, menu);
 		return true;
 	}
 
     /*
      * Event OptionsMenuItemSelected
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.roadrunner_menu_settings:
         	startActivity(new Intent(this, Settings.class));
             return true;
         case R.id.roadrunner_menu_info:
         	startActivity(new Intent(this, Info.class));
             return true;
         case R.id.roadrunner_menu_systemtest:
         	startActivity(new Intent(this, SystemTest.class));
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
