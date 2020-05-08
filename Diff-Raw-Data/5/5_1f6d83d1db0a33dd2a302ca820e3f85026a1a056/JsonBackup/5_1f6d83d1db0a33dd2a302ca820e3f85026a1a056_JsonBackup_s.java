 package lu.albert.android.jsonbackup;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * Activity to create a plain-text backup of you contact list. Only the most
  * basic contact information is stored. So a dump/restore operation is
  * guaranteed to lose data. Only use this application if your know what you
  * are doing.
  * 
  * @author Michel Albert <michel@albert.lu>
  */
 public class JsonBackup extends Activity {
 
 	/** The filename that will be stored on disk */
 	public static final String FILE_NAME = "contacts.json";
 	
 	private static final int MENU_EULA = Menu.FIRST;
 	private static final int MENU_LICENSE = Menu.FIRST + 1;
 	private static final int MENU_USAGE = Menu.FIRST + 2;
 	
 	private static final int DIALOG_CONFIRM_OVERWRITE = 0;
 	private static final int DIALOG_CANCELLED = 1;
 	private static final int DIALOG_BACKUP_PROGRESS = 2;
 	protected static final int DIALOG_FINISHED = 3;
 	private static final int DIALOG_RESTORE_PROGRESS = 4;
 	private static final int DIALOG_ERROR = 5;
 	private static final int DIALOG_EULA = 6;
 	private static final int DIALOG_USAGE = 8;
 	private static final int DIALOG_CONFIRM_RESTORE = 9;
 
 	private static int ACTIVITY_VIEW_LICENSE = 0;
 	
 	protected static final int RESTORE_MSG_PROGRESS = 0;
 	protected static final int RESTORE_MSG_INFO = 1;
 	
 	/** 
 	 * A handler message type for errors. If a message of this kind is
 	 * received, an error message will popup. The message must also contain
 	 * a key with the name "message" containing the message string to be
 	 * displayed.
 	 */
 	public static final int RESTORE_SHOW_ERROR = 2;
 	
 	/** The preferences name */
 	public static final String PREFS_NAME = "lu.albert.android.jsonbackup.prefs";
 
 	/** The tag used in the logging facility */
 	public static final String TAG = "JsonBackup";
 
 	private Button mBackupButton;
 	private Button mRestoreButton;
 	private BackupThread mProgressThread;
 	private RestoreThread mRestoreThread;
 	private ProgressDialog mProgressDialog;
 	private AlertDialog mErrorDialog;
 
 	/**
 	 * A handler which deals with updating the progress bar
 	 * while creating a backup file
 	 */
 	final Handler dumpHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			int position = msg.getData().getInt("position");
 			int total = msg.getData().getInt("total");
 			mProgressDialog.setProgress(position);
 			mProgressDialog.setIndeterminate(false);
 			mProgressDialog.setMax(total);
 			if (position >= total) {
				dismissDialog(DIALOG_BACKUP_PROGRESS);
 				mProgressThread.setState(BackupThread.STATE_DONE);
 				showDialog(DIALOG_FINISHED);
 			}
 		}
 	};
 	
 	/**
 	 * A handler which deals with updating the progress bar
 	 * while restoring contacts
 	 */
 	final Handler restore_handler = new Handler() {
 		public void handleMessage(Message msg) {
 			
 			switch ( msg.what ){
 			case RESTORE_MSG_PROGRESS:
 				long position = msg.getData().getLong("position");
 				long total = msg.getData().getLong("total");
 				mProgressDialog.setProgress((int)position);
 				mProgressDialog.setIndeterminate(false);
 				mProgressDialog.setMax((int)total);
 				if (position >= total) {
					dismissDialog(DIALOG_RESTORE_PROGRESS);
 					mRestoreThread.setState(BackupThread.STATE_DONE);
 					showDialog(DIALOG_FINISHED);
 				}
 				break;
 			case RESTORE_SHOW_ERROR:
 				String message = msg.getData().getString("message");
 				mErrorDialog.setMessage(message);
 				showDialog(DIALOG_ERROR);
 				break;
 			case RESTORE_MSG_INFO:
 				String name = msg.getData().getString("name");
 				mProgressDialog.setMessage( "Restored " + name );
 				break;
 			default:
 				// do nothing
 				break;
 			}
 		}
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    menu.add(0, MENU_EULA, 0, "EULA")
 	    	.setIcon(android.R.drawable.ic_dialog_info);
 	    menu.add(0, MENU_USAGE, 0, "Help")
 		.setIcon(android.R.drawable.ic_menu_help);
 	    menu.add(0, MENU_LICENSE, 0, "License")
 	    	.setIcon(android.R.drawable.ic_menu_agenda);
 	    return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 	    switch (item.getItemId()) {
 	    case MENU_EULA:
 	    	showDialog( DIALOG_EULA );
 	        return true;
 	    case MENU_LICENSE:
 	    	intent = new Intent(this, HtmlView.class);
 	    	intent.putExtra(HtmlView.KEY_DOC_ID, R.raw.gpl3);
 	    	startActivityForResult(intent, ACTIVITY_VIEW_LICENSE );
 	        return true;
 	    case MENU_USAGE:
 	    	intent = new Intent(this, HtmlView.class);
 	    	intent.putExtra(HtmlView.KEY_DOC_ID, R.raw.usage);
 	    	startActivityForResult(intent, ACTIVITY_VIEW_LICENSE );
 	        return true;
 	    }
 	    return false;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		if ( !settings.getBoolean("eulaAccepted", false) ){
 			showDialog( DIALOG_EULA );
 		}
 		
 		mBackupButton = (Button)findViewById(R.id.backup_button);
 		mBackupButton.setOnClickListener( new BackupListener() );
 		mRestoreButton = (Button)findViewById(R.id.restore_button);
 		mRestoreButton.setOnClickListener( new RestoreListener() );
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(JsonBackup.this);
 		builder.setTitle(getString(R.string.error))
 				.setMessage(getString(R.string.unspecified_error))
 				.setCancelable(false)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setNegativeButton(getString(android.R.string.ok),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 		mErrorDialog = builder.create();
 
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		Dialog dialog;
 		AlertDialog.Builder builder;
 
 		switch (id) {
 		case DIALOG_CONFIRM_RESTORE:
 			/*
 			 * Create a dialog which asks the user if the restoration should be
 			 * done (will delete all contacts)
 			 */
 			builder = new AlertDialog.Builder(this);
 			
 			builder.setMessage(getString(R.string.confirm_restore_contacts))
 					.setCancelable(false)
 					.setPositiveButton(getString(android.R.string.yes),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int id) {
 								File file1 = null;
 								file1 = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
 								if (file1.exists()) {
 									showDialog(DIALOG_RESTORE_PROGRESS);
 								}
 							}
 						})
 					// XXX: This string resolves to "cancel". I'm hoping it will be fixed in a future SDK release, so I'll leave that here.
 					.setNegativeButton(getString(android.R.string.no),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int id) {
 								showDialog(DIALOG_CANCELLED);
 								dialog.cancel();
 							}
 						});
 			dialog = builder.create();
 			break;
 
 		case DIALOG_CONFIRM_OVERWRITE:
 			/*
 			 * Create a dialog which asks the user if the existing file should
 			 * be overwritten (it will be deleted before the new one is 
 			 * created)
 			 */
 			builder = new AlertDialog.Builder(this);
 			
 			builder.setMessage(
 					String.format(getString(R.string.file_exists), FILE_NAME))
 					.setCancelable(false)
 					.setPositiveButton(getString(android.R.string.yes),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int id) {
 								JsonBackup.this.deleteDump();
 								showDialog(DIALOG_BACKUP_PROGRESS);
 							}
 						})
 					// XXX: This string resolves to "cancel". I'm hoping it will be fixed in a future SDK release, so I'll leave that here.
 					.setNegativeButton(getString(android.R.string.no),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int id) {
 								showDialog(DIALOG_CANCELLED);
 								dialog.cancel();
 							}
 						});
 			dialog = builder.create();
 			break;
 
 		case DIALOG_CANCELLED:
 			/*
 			 * Tell the user that the operation was cancelled
 			 */
 			builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.cancelled_by_user_request))
 					.setCancelable(false)
 					.setNegativeButton(getString(android.R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 			dialog = builder.create();
 			break;
 
 		case DIALOG_FINISHED:
 			/*
 			 * Tell the user that the operation finished as expected
 			 */
 			builder = new AlertDialog.Builder(JsonBackup.this);
 			builder.setMessage(getString(R.string.operation_finished))
 					.setCancelable(false)
 					.setNegativeButton(getString(android.R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 			dialog = builder.create();
 			break;
 
 		case DIALOG_BACKUP_PROGRESS:
 			/*
 			 * Display the backup progress
 			 */
 			mProgressDialog = new ProgressDialog(JsonBackup.this);
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgressDialog.setMessage(getString(R.string.serializing));
 			mProgressThread = new BackupThread(dumpHandler, this);
 			mProgressDialog.setIndeterminate(true);
 			mProgressThread.start();
 			dialog = mProgressDialog;
 			break;
 
 		case DIALOG_RESTORE_PROGRESS:
 			/*
 			 * Display the restoration progress
 			 */
 			mProgressDialog = new ProgressDialog(JsonBackup.this);
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgressDialog.setMessage(getString(R.string.restoring));
 			mRestoreThread = new RestoreThread(restore_handler, this);
 			mProgressDialog.setIndeterminate(true);
 			mRestoreThread.start();
 			dialog = mProgressDialog;
 			break;
 			
 		case DIALOG_ERROR:
 			/*
 			 * Display a generic error dialog.
 			 */
 			dialog = mErrorDialog;
 			break;
 
 		case DIALOG_EULA:
 			/*
 			 * Display the EULA on first start
 			 */
 			builder = new AlertDialog.Builder(JsonBackup.this);
 			builder.setMessage(getEula())
 					.setCancelable(false)
 					.setPositiveButton(getString(android.R.string.yes), 
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 								SharedPreferences.Editor editor = settings.edit();
 							    editor.putBoolean("eulaAccepted", true);
 							    editor.commit();
 							}
 						})
 					.setNegativeButton(getString(android.R.string.no),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 								SharedPreferences.Editor editor = settings.edit();
 							    editor.putBoolean("eulaAccepted", false);
 							    editor.commit();
 								dialog.cancel();
 							}
 						});
 			dialog = builder.create();
 			break;
 			
 		case DIALOG_USAGE:
 			/*
 			 * Display the Usage
 			 */
 			builder = new AlertDialog.Builder(JsonBackup.this);
 			builder.setMessage(getUsage())
 					.setCancelable(false)
 					.setNegativeButton(getString(android.R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 			dialog = builder.create();
 			break;
 			
 		default:
 			/*
 			 * If an invalid dialog was specified, do nothing 
 			 */
 			dialog = null;
 		}
 
 		return dialog;
 	}
 	
 	@Override
 	protected void onPause() {
 		try {
 			if (mProgressThread != null && mProgressThread.isAlive()) {
 				mProgressThread.finish();
 				mProgressThread.join();
 			}
 			if (mRestoreThread != null && mRestoreThread.isAlive()) {
 				mRestoreThread.finish();
 				mRestoreThread.join();
 			}
 		} catch (InterruptedException e) {
 			// Thread already dead. We can ignore this... I hope.
 		}
 		super.onPause();
 	}
 
 	/**
 	 * Delete the dump file
 	 */
 	protected void deleteDump() {
 		File fp = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
 		fp.delete();
 	}
 
 	/**
 	 * @return The text contained in res/raw/eula.txt
 	 */
 	private String getEula(){
 		InputStream eula_stream = getResources().openRawResource(R.raw.eula);
 		StringBuffer content = new StringBuffer();
 		int result;
 		try {
 			result = eula_stream.read();
 			while ( result != -1 ){
 				content.append((char)result);
 				result = eula_stream.read();
 			}
 		} catch (IOException e) {
 			mErrorDialog.setMessage("Unable to open EULA!");
 			showDialog(DIALOG_ERROR);
 		}
 		return content.toString();
 	}
 	
 	/**
 	 * @return The text contained in res/raw/license.txt
 	 */
 	private String getUsage(){
 		InputStream file_stream = getResources().openRawResource(R.raw.usage);
 		StringBuffer content = new StringBuffer();
 		int result;
 		try {
 			result = file_stream.read();
 			while ( result != -1 ){
 				content.append((char)result);
 				result = file_stream.read();
 			}
 		} catch (IOException e) {
 			mErrorDialog.setMessage("Unable to open the usage file!");
 			showDialog(DIALOG_ERROR);
 		}
 		return content.toString();
 	}
 
 	/**
 	 * Listens to clicks on the "start backup" button
 	 * 
 	 * @author Michel Albert <michel@albert.lu>
 	 */
 	private class BackupListener implements OnClickListener{
 
 		@Override
 		public void onClick(View v) {
 			
 			if ( !getSharedPreferences(PREFS_NAME, 0).getBoolean("eulaAccepted", false) ){
 				/* EULA not accepted. Bail out! */
 				mErrorDialog.setMessage("You did not accept the EULA! You can access and accept it via the Menu button.");
 				showDialog(DIALOG_ERROR);
 				return;
 			}
 
 			File file1 = null;
 			file1 = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
 			if (file1.exists()) {
 				showDialog(DIALOG_CONFIRM_OVERWRITE);
 			} else {
 				showDialog(DIALOG_BACKUP_PROGRESS);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Listens to clicks on the "restore" button
 	 * 
 	 * @author Michel Albert <michel@albert.lu>
 	 */
 	private class RestoreListener implements OnClickListener{
 
 		@Override
 		public void onClick(View v) {
 			
 			if ( !getSharedPreferences(PREFS_NAME, 0).getBoolean("eulaAccepted", false) ){
 				/* EULA not accepted. Bail out! */
 				mErrorDialog.setMessage("You did not accept the EULA! You can access and accept it via the Menu button.");
 				showDialog(DIALOG_ERROR);
 				return;
 			}
 			showDialog(DIALOG_CONFIRM_RESTORE);
 		}
 		
 	}
 
 }
