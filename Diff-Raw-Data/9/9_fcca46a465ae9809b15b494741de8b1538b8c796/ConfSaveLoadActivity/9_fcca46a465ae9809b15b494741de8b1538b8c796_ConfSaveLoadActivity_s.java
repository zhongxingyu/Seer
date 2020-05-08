 package ru.izelentsov.android.lifegame;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Toast;
 import ru.izelentsov.android.lifegame.model.ConfStorage;
 import ru.izelentsov.android.lifegame.view.ConfSaveLoadView;
 
 
 
 
 
 
 public class ConfSaveLoadActivity extends Activity {
 
 	public static final String NAME_EXTRA = "saveName";
 	public static final String ACTION_EXTRA = "action";
 	private static final int NAME_CHOICE_DIALOG_ID = 0;
 	private static final int REWRITE_CONFIRM_DIALOG_ID = 1;
 	private static final int DELETE_CONFIRM_DIALOG_ID = 2;
 	private static final int NO_CONF_DIALOG_ID = 3;
 	
 	private ConfSaveLoadView view = null;
 	private ConfStorage storage = null;
 	private RewriteConfirmListener rewriteConfirmListener = null;
 	private DeleteConfirmListener deleteConfirmListener = null;
 	
 	
 	public enum Actions {
 		SAVE (0),
 		LOAD (1);
 	
 		private int code = -1;
 		private Actions (int aCode) {
 			code = aCode;
 		}
 		public int code () {
 			return code;
 		}
 	}
 	
 		
 	
 	@Override
 	protected void onCreate (Bundle savedInstanceState) {
 		super.onCreate (savedInstanceState);
 		view = new ConfSaveLoadView (this);
 		view.setListener (new ViewListener ());
 		storage = new ConfStorage ();
 		rewriteConfirmListener = new RewriteConfirmListener ();
 		deleteConfirmListener = new DeleteConfirmListener ();
 	}
 	
 
 	@Override
 	protected void onStart () {
 		super.onStart ();
 		setChoiceEnabled ();
		view.enableActions ();
 	}
 
 	
 	@Override
 	protected Dialog onCreateDialog (int id) {
 		Dialog dialog = null;
 		
 		switch (id) {
 		case NAME_CHOICE_DIALOG_ID:
 			dialog = createNameChoiceDialog ();
 			break;
 		case REWRITE_CONFIRM_DIALOG_ID:
 			dialog = createRewriteConfirmDialog ();
 			break;
 		case DELETE_CONFIRM_DIALOG_ID:
 			dialog = createDeleteConfirmDialog ();
 			break;
 		case NO_CONF_DIALOG_ID:
 			dialog = createNoConfDialog ();
 			break;
 		default:
 			break;
 		}
 		return dialog;
 	}
 	
 	
 	
 	private AlertDialog createNameChoiceDialog () {
 		AlertDialog dialog = null;
 		ArrayList<String> nms = storage.listStoredConfNames (this);
 		String[] names = nms.toArray (new String [nms.size ()]);
 		
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder (this);
 		dialogBuilder.setTitle (R.string.confNameChoiceDialogTitle);
 		dialogBuilder.setItems (names,  new NameChoiceDialogListener (names));
 		dialogBuilder.setCancelable (true);
 		dialog = dialogBuilder.create ();
 		dialog.setOnDismissListener (new DialogInterface.OnDismissListener() {
 			@Override
 			public void onDismiss (DialogInterface dialog) {
 				removeDialog (NAME_CHOICE_DIALOG_ID);
 			}
 		});
 		return dialog;
 	}
 	
 	
 	private AlertDialog createRewriteConfirmDialog () {
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder (this);
 		dialogBuilder.setMessage (R.string.confRewriteMsg)
 			.setCancelable (false)
 			.setPositiveButton (R.string.confRewriteAcceptLabel, 
 					rewriteConfirmListener)
 			.setNegativeButton (R.string.confRewriteRejectLabel, 
 					new OnClickListener() {
 						@Override
 						public void onClick (DialogInterface dialog, int which) {
 							dialog.cancel ();
 						}
 					});
 		return dialogBuilder.create ();
 	}
 	
 	
 	private AlertDialog createDeleteConfirmDialog () {
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder (this);
 		dialogBuilder.setMessage (R.string.confDeleteMsg)
 			.setCancelable (false)
 			.setPositiveButton (R.string.confDeleteAcceptLabel, 
 					deleteConfirmListener)
 			.setNegativeButton (R.string.confDeleteRejectLabel, 
 					new OnClickListener() {
 						@Override
 						public void onClick (DialogInterface dialog, int which) {
 							dialog.cancel ();
 						}
 					});
 		return dialogBuilder.create ();
 	}	
 
 	
 	private AlertDialog createNoConfDialog () {
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder (this);
 		dialogBuilder.setMessage (R.string.confNoConfMsg)
 			.setPositiveButton (R.string.confNoConfOKLabel, 
 					new OnClickListener() {
 						@Override
 						public void onClick (DialogInterface dialog, int which) {
 							dialog.dismiss ();
 						}
 				});
 		return dialogBuilder.create ();
 	}
 	
 	
 	private void setChoiceEnabled () {
 		view.setChoiceEnabled (storage.listStoredConfNames (this).size () > 0);
 	}
 	
 	
 	
 	private void save (String aSaveName) {
 		Intent i = new Intent ();
 		i.putExtra (NAME_EXTRA, aSaveName);
 		i.putExtra (ACTION_EXTRA, Actions.SAVE.code ());
 		setResult(RESULT_OK, i);
 		finish ();
 	}
 	
 	
 	private void load (String aLoadName) {
 		Intent i = new Intent ();
 		i.putExtra (NAME_EXTRA, aLoadName);
 		i.putExtra (ACTION_EXTRA, Actions.LOAD.code ());
 		setResult(RESULT_OK, i);
 		finish ();
 	}
 	
 	
 	private boolean saveExists (String aConfName) {
 		return storage.confExists (this, aConfName);
 	}
 
 	
 	private void confirmRewrite (String aSaveName) {
 		rewriteConfirmListener.setSaveName (aSaveName);
 		showDialog (REWRITE_CONFIRM_DIALOG_ID);
 	}
 	
 	
 	private void delete (String aSaveName) {
 		storage.deleteConf (ConfSaveLoadActivity.this, aSaveName);
 		view.clearConfName ();
 		setChoiceEnabled ();
 		Toast.makeText (ConfSaveLoadActivity.this, 
 				getResources ().getText (R.string.confDeletedMsg) + 
 				": " + aSaveName, Toast.LENGTH_LONG).show ();
 	}
 	
 	
 	private void confirmDelete (String aDeleteName) {
 		deleteConfirmListener.setDeleteName (aDeleteName);
 		showDialog (DELETE_CONFIRM_DIALOG_ID);
 	}
 	
 	
 	
 	
 	
 	private class ViewListener implements ConfSaveLoadView.IListener {
 
 		@Override
 		public void saveRequested (String aSaveName) {
 			if (saveExists (aSaveName)) {
 				confirmRewrite (aSaveName);
 			} else {
 				save (aSaveName);
 			}
 		}
 
 		@Override
 		public void loadRequested (String aSaveName) {
 			if (saveExists (aSaveName)) {
 				load (aSaveName);
 			} else {
 				showDialog (NO_CONF_DIALOG_ID);
 			}
 		}
 		
 		@Override
 		public void deleteRequested (String aSaveName) {
 			confirmDelete (aSaveName);
 		}
 
 		@Override
 		public void cancelRequested () {
 			setResult(RESULT_CANCELED);
 			finish ();
 		}
 
 
 		@Override
 		public void confNameChoiceRequested () {
 			showDialog (NAME_CHOICE_DIALOG_ID);
 		}
 
 	}
 	
 	
 	
 	private class NameChoiceDialogListener implements DialogInterface.OnClickListener {
 
 		private String[] names = null;
 		
 		public NameChoiceDialogListener (String[] aNamesList) {
 			names = aNamesList;
 		}
 		
 		@Override
 		public void onClick (DialogInterface dialog, int which) {
 			view.setConfName (names[which]);
 			dialog.dismiss ();
 		}
 	}
 	
 	
 	private class RewriteConfirmListener implements OnClickListener {
 
 		private String saveName = null;
 		
 		public void setSaveName (String aSaveName) {
 			saveName = aSaveName;
 		}
 		
 		@Override
 		public void onClick (DialogInterface dialog, int which) {
 			save (saveName);
 		}
 	}
 
 
 	private class DeleteConfirmListener implements OnClickListener {
 
 		private String deleteName = null;
 		
 		public void setDeleteName (String aDeleteName) {
 			deleteName = aDeleteName;
 		}
 
 		@Override
 		public void onClick (DialogInterface dialog, int which) {
 			delete (deleteName);
 		}
 	}
 	
 	
 }
