 package com.lake.tahoe.activities;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;

 import com.lake.tahoe.R;
 import com.lake.tahoe.dialogs.BlockerDialog;
 import com.lake.tahoe.utils.ActivityUtil;
 import com.lake.tahoe.utils.HandlesErrors;
import com.parse.ParseException;

 import net.simonvt.messagebar.MessageBar;
 
 public class TahoeActivity extends FragmentActivity implements HandlesErrors {
 
 	private MessageBar messageBar;
 	BlockerDialog blockerDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		blockerDialog = new BlockerDialog(this, R.style.DialogBlocker);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		toggleBlocker(false);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		toggleBlocker(false);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		messageBar = new MessageBar(this);
 		super.onPostCreate(savedInstanceState);
 	}
 
 	public void showMessage(String message) {
 		messageBar.show(message);
 	}
 
 	public void toggleBlocker(boolean show) {
 		if (show) blockerDialog.show();
 		else blockerDialog.hide();
 	}
 
 	@Override
 	public void onError(Throwable t) {
		String errorFormat = getString(R.string.error_format);

 		String PREFIX = "OddJobError";

		// Suppress no results found for query exception.
		// Don't throw stack traces either since they often come from getUnfinishedRequest()
		// on location changes.
		if(t instanceof ParseException) {
			ParseException e = (ParseException) t;
			if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
				Log.e(PREFIX, "No results found for query...ignoring.");
				return;
			}
		}
 		Log.e(PREFIX, getLocalClassName(), t);
 		showMessage(String.format(errorFormat, t.getLocalizedMessage()));

		if (t instanceof IllegalStateException) {
 			refreshState();
		}
 	}
 
 	public void refreshState() {
 		toggleBlocker(true);
 		ActivityUtil.startDelegateActivity(this);
 		ActivityUtil.transitionFade(this);
 	}
 }
