 package com.plingnote;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.EditText;
 
 public class PreferenceDialogEmail extends DialogPreference {
 	private Context context;
 	private String deviceInfoString = "------------------\n"
 									+ Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.BRAND + ")\n"
 									+ Build.DISPLAY + "\n"
 			 						+ "SDK: " + Build.VERSION.SDK_INT + "\n";
 	private String appVersionName = "Plingnote ";
 
 	
 	public PreferenceDialogEmail(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = context;
 		appVersionName += this.context.getString(R.string.app_version_name);
 		setPersistent(false);
 		setDialogLayoutResource(R.layout.preference_dialog_email);
 	}
 	
 	@Override
 	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
 		builder.setPositiveButton(null, null);
 		builder.setNegativeButton(null, null);
 		super.onPrepareDialogBuilder(builder);  
 	}
 
 	@Override
 	public void onBindDialogView(View view){
		final EditText editText = (EditText)view.findViewById(R.id.editText_email);
 		Button sendButton = (Button)view.findViewById(R.id.email_send_button);
 		sendButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				String userText = editText.getText().toString() + "\n\n";
 				Intent intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("message/rfc822");
 				intent.putExtra(Intent.EXTRA_EMAIL,new String[] { "plingnote@plingnote.com" });
 				intent.putExtra(Intent.EXTRA_SUBJECT, appVersionName);
				intent.putExtra(Intent.EXTRA_TEXT, userText + deviceInfoString);
 				context.startActivity(Intent.createChooser(intent, "Send with..."));
 			}
 		});
 		
 		Button cancelButton = (Button)view.findViewById(R.id.email_cancel_button);
 		cancelButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				getDialog().dismiss();
 			}
 		});
 
 		super.onBindDialogView(view);
 	}
 
 
 }
