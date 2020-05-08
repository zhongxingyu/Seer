 package org.subsurface;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.subsurface.controller.DiveController;
 import org.subsurface.model.DiveLocationLog;
 import org.subsurface.ws.WsException;
 
 import android.app.AlertDialog;
import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * Activity for dive details.
  * @author Aurelien PRALONG
  *
  */
 public class DiveDetailActivity extends SherlockActivity implements com.actionbarsherlock.view.ActionMode.Callback {
 
 	public static final String PARAM_DIVE_ID = "PARAM_DIVE_ID";
 
 	protected static final String TAG = null;
 
 	private DiveLocationLog dive = null;
 	private ActionMode actionMode = null;
 
 	private void initNormalView() {
 		setContentView(R.layout.dive_detail);
 		((TextView) findViewById(R.id.title)).setText(dive.getName());
 		((TextView) findViewById(R.id.date)).setText(
 				new SimpleDateFormat(getString(R.string.date_format_full)).format(new Date(dive.getTimestamp())));
 		((TextView) findViewById(R.id.coordinates)).setText(
 				getString(R.string.details_coordinates, (float) dive.getLatitude(), (float) dive.getLongitude()));
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		this.dive = DiveController.instance.getDiveById(getIntent().getLongExtra(PARAM_DIVE_ID, 0));
 		if (dive != null) {
 			initNormalView();
 		} else {
 			Toast.makeText(this, R.string.error_no_associated_dive, Toast.LENGTH_SHORT).show();
 			finish();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (dive != null) {
 			getSupportMenuInflater().inflate(R.menu.dive_details, menu);
 			if (dive.isSent()) {
 				menu.findItem(R.id.menu_send).setVisible(false);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			finish();
 			return true;
 		} else if (item.getItemId() == R.id.menu_map) {
 			startActivity(new Intent(
 					Intent.ACTION_VIEW,
 					Uri.parse("geo:" + dive.getLatitude() + "," + dive.getLongitude())));
 		} else if (item.getItemId() == R.id.menu_send) {
 			new Thread(new Runnable() {
 				public void run() {
 					int messageCode = R.string.error_send;
 					try {
 						DiveController.instance.sendDiveLog(dive);
 						messageCode = R.string.confirmation_location_sent;
 					} catch (WsException e) {
 						messageCode = e.getCode();
 					} catch (Exception e) {
 						Log.d(TAG, "Could not send dive " + dive.getName(), e);
 					}
 					final String message = getString(messageCode, dive.getName());
 					runOnUiThread(new Runnable() {
 						public void run() {
 							Toast.makeText(DiveDetailActivity.this, message, Toast.LENGTH_SHORT).show();
 						}
 					});
 				}
 			}).start();
 		} else if (item.getItemId() == R.id.menu_delete) {
 			new AlertDialog.Builder(this)
 			.setTitle(R.string.menu_delete)
 			.setMessage(R.string.confirm_delete_dive)
 			.setNegativeButton(android.R.string.cancel, null)
 			.setCancelable(true)
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					DiveController.instance.deleteDiveLog(dive);
 					DiveDetailActivity.this.finish();
 				}
 			}).create().show();
 		} else if (item.getItemId() == R.id.menu_edit) {
 			this.actionMode = startActionMode(DiveDetailActivity.this);
 		} else if (item.getItemId() == R.id.menu_settings) { // Settings
     		startActivity(new Intent(this, Preferences.class));
     		return true;
     	}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 		mode.setTitle(getString(R.string.details_action_mode_title));
 		getSupportMenuInflater().inflate(R.menu.dive_details_edit, menu);
 		setContentView(R.layout.dive_detail_edit);
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 		((EditText) findViewById(R.id.title)).setText(dive.getName());
 		((TextView) findViewById(R.id.date)).setText(
 				new SimpleDateFormat(getString(R.string.date_format_full)).format(new Date(dive.getTimestamp())));
 		((TextView) findViewById(R.id.coordinates)).setText(
 				getString(R.string.details_coordinates, (float) dive.getLatitude(), (float) dive.getLongitude()));
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 		return false;
 	}
 
 	@Override
 	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 		if (item.getItemId() == R.id.menu_save) {
 			dive.setName(((EditText) findViewById(R.id.title)).getText().toString());
 			DiveController.instance.updateDiveLog(dive);
 		}
 		actionMode.finish();
 		return true;
 	}
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 		initNormalView();
 		actionMode = null;
 	}
 }
