 package com.yhsif.purge;
 
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 
 public class PurgeActivity extends Activity
 	implements View.OnClickListener, DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
 
 	@Override public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		((Button)findViewById(R.id.do_it)).setOnClickListener(this);
 		((Button)findViewById(R.id.close)).setOnClickListener(this);
 		((CheckBox)findViewById(R.id.sms)).setOnClickListener(this);
 		((CheckBox)findViewById(R.id.locked_sms)).setOnClickListener(this);
 	}
 
 	// for View.OnClickListener
	@Override public void onClick(View view) {
 		switch(view.getId()) {
 			case R.id.close:
 				this.finish();
 				return;
 			case R.id.sms:
 				((CheckBox)findViewById(R.id.locked_sms)).setEnabled(((CheckBox)findViewById(R.id.sms)).isChecked());
 				return;
 			case R.id.locked_sms:
 				{
 					CheckBox check = (CheckBox)findViewById(R.id.locked_sms);
 					if(!check.isChecked()) {
 						// do nothing
 						return;
 					}
 					Builder builder = new Builder(this);
 					builder.setMessage(R.string.confirm_locked)
 						.setOnCancelListener(this)
 						.setPositiveButton(R.string.ok, this)
 						.setNegativeButton(R.string.cancel, this)
 						.show();
 				}
 				return;
 			case R.id.do_it:
 				{
 					// TODO
 				}
 				return;
 		}
 	}
 
 	// for DialogInterface.OnClickListener
	@Override public void onClick(DialogInterface dialog, int which) {
 		CheckBox check = (CheckBox)findViewById(R.id.locked_sms);
 		switch (which){
 			case DialogInterface.BUTTON_POSITIVE:
 				check.setChecked(true);
 				break;
 			case DialogInterface.BUTTON_NEGATIVE:
 				check.setChecked(false);
 				break;
 		}
 	}
 
 	// for DialogInterface.OnCancelListener
	@Override public void onCancel(DialogInterface dialog) {
 		((CheckBox)findViewById(R.id.locked_sms)).setChecked(false);
 	}
 
 }
