 package at.roadrunner.android.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 import at.roadrunner.android.R;
 import at.roadrunner.android.couchdb.RequestWorker;
 import at.roadrunner.android.model.Log.LogType;
 import at.roadrunner.android.util.Intents;
 
 public class Roadrunner extends Activity {
 	// Intent for scanning
 	private static final String SCAN_INTENT = "com.google.zxing.client.android.SCAN";
 	private static final String SCAN_PACKAGE = "com.google.zxing.client.android";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_roadrunner);
 	}
 
 	/*
 	 * Event onScanClick
 	 */
 	public void onScanClick(View view) {
 		if (Intents.isIntentAvailable(this, SCAN_INTENT)) {
 			Intent intent = new Intent(SCAN_INTENT);
 			intent.setPackage(SCAN_PACKAGE);
 			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 
 			startActivityForResult(intent, 0);
 		} else {
 			// show dialog
 			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 			alertBuilder.setMessage(R.string.roadrunner_dialog_missingApp);
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
 			AlertDialog alert = alertBuilder.create();
 			
 			alert.show();
 		}
 	}
 	
 	public void onReplicateClick(View view)  {
 		
 		RequestWorker.replicate();
 		
 	}
 
 	/*
 	 * onActivityResult
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == 0) {
 			if (resultCode == RESULT_OK) {
 				String contents = intent.getStringExtra("SCAN_RESULT");
 				@SuppressWarnings("unused")
 				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
 
 				Toast toast = Toast.makeText(getApplicationContext(), contents,
 						3);
 				toast.show();
 				
 				RequestWorker.saveLog(contents, LogType.LOAD);
 				
 			} else if (resultCode == RESULT_CANCELED) {
 
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
