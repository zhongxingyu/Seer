 package com.fusionx.tilal6991.multiboot;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Finalisation extends Activity {
 	static final int DIALOG_LACK_OF_SYSTEM_IMAGE = 0;
 	private Bundle b;
 
 	private String mChosen;
 
 	private void chooseRom(final File mPath) {
 		final FilenameFilter filter = new FilenameFilter() {
 			@Override
 			public boolean accept(final File dir, final String filename) {
 				return filename.endsWith(".zip")
 						|| new File(dir.getAbsolutePath() + "/" + filename)
 								.isDirectory();
 			}
 		};
 		final String[] mFileList = mPath.list(filter);
 		java.util.Arrays.sort(mFileList, String.CASE_INSENSITIVE_ORDER);
 
 		final Builder builder = new Builder(this);
 		builder.setTitle("Choose your ROM");
 
 		final DialogInterface.OnClickListener k = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(final DialogInterface dialog, final int which) {
 				mChosen = mFileList[which];
 				final File file = new File(mPath + "/" + mChosen);
 				if (file.isDirectory())
 					chooseRom(file);
 				else {
 					((TextView) findViewById(R.id.txtRom)).setText(file.getAbsolutePath());
 					findViewById(R.id.button1).setEnabled(true);
 					return;
 				}
 			}
 		};
 		builder.setItems(mFileList, k);
 		builder.show();
 	}
 
 	public void chooseRom(final View v) {
 		chooseRom(Environment.getExternalStorageDirectory());
 	}
 
 	public void finish(final View view) {
 		final String systemImage = ((EditText) findViewById(R.id.edtSystem))
 				.getText().toString();
 		final String dataImage = ((EditText) findViewById(R.id.edtData))
 				.getText().toString();
 		Intent intent = null;
 		if (b.getBoolean("gapps") == true) {
 			intent = new Intent(this, CreateOther.class);
 			if (new File(Environment.getExternalStorageDirectory()
 					+ "/multiboot/" + systemImage).exists())
 				intent.putExtra("systemimagename", systemImage);
 			else {
 				onCreateDialog(DIALOG_LACK_OF_SYSTEM_IMAGE);
 				return;
 			}
 		} else {
 			intent = new Intent(this, CreateRom.class);
 			if (b.getBoolean("createdataimage") == false)
 				if (new File(Environment.getExternalStorageDirectory()
 						+ "/multiboot/" + dataImage).exists())
 					intent.putExtra("dataimagename", dataImage);
 				else {
 					onCreateDialog(DIALOG_LACK_OF_SYSTEM_IMAGE);
 					return;
 				}
 			if (b.getBoolean("createsystemimage") == false)
 				if (new File(Environment.getExternalStorageDirectory()
 						+ "/multiboot/" + systemImage).exists())
 					intent.putExtra("systemimagename", systemImage);
 				else {
 					onCreateDialog(DIALOG_LACK_OF_SYSTEM_IMAGE);
 					return;
 				}
 		}
 		intent.putExtra("filename", mChosen);
 		intent.putExtras(getIntent().getExtras());
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_finalisation);
 		b = getIntent().getExtras();
 		if ((b.getBoolean("gapps") == true) || (b.getBoolean("createdataimage") == true)) {
 			findViewById(R.id.edtData).setVisibility(4);
 			findViewById(R.id.txtData).setVisibility(4);
 		}
 	    if (!(b.getBoolean("gapps") == true) && (b.getBoolean("createsystemimage") == true)) {
 			findViewById(R.id.edtSystem).setVisibility(4);
 			findViewById(R.id.txtSystem).setVisibility(4);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(final int id) {
 		Dialog dialog = null;
 		switch (id) {
 		case DIALOG_LACK_OF_SYSTEM_IMAGE:
 			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("One of the images doesn't exist!")
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										final DialogInterface dialog,
 										final int id) {
									return;
 								}
 							});
 			final AlertDialog alert = builder.create();
 			alert.show();
 			break;
 		}
 		return dialog;
 	}
 }
