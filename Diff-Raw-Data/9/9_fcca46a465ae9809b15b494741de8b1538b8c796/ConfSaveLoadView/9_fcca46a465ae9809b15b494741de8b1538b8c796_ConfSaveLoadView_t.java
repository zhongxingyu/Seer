 package ru.izelentsov.android.lifegame.view;
 
 import android.app.Activity;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import ru.izelentsov.android.lifegame.R;
 
 
 
 
 public class ConfSaveLoadView {
 
 	
 	public interface IListener {
 		public void saveRequested 			(String aSaveName);
 		public void loadRequested 			(String aSaveName);
 		public void deleteRequested 		(String aSaveName);
 		public void cancelRequested 		();
 		public void confNameChoiceRequested ();
 	}
 
 	private class VoidListener implements IListener {
 		@Override
 		public void saveRequested (String aSaveName) {
 		}
 		@Override
 		public void cancelRequested () {
 		}
 		@Override
 		public void loadRequested (String aSaveName) {
 		}
 		@Override
 		public void deleteRequested (String aSaveName) {
 		}
 		@Override
 		public void confNameChoiceRequested () {
 		}
 	}
 	
 	
 	private IListener listener = null;
 	private EditText confNameEdit = null;
 	private Button saveButton = null;
 	private Button loadButton = null;
 	private Button deleteButton = null;
 	private Button cancelButton = null;
 	private Button chooseButton = null;
 	
 	
 	
 	public ConfSaveLoadView (Activity anActivity) {
 		listener = new VoidListener ();
 		anActivity.setContentView (R.layout.conf_save_load);
 		setupControls (anActivity);
 	}
 
 
 	public void setListener (IListener aListener) {
 		listener = (aListener != null) ? aListener : new VoidListener ();
 	}
 	
 	
 
 	private void setupControls (Activity anActivity) {
 		confNameEdit = (EditText) anActivity.findViewById (R.id.confSaveEdit);
//		confNameEdit.setOnEditorActionListener (new NameEditListener ());
 		
 		saveButton = (Button) anActivity.findViewById (R.id.confSaveBtn);
 		saveButton.setOnClickListener (new OnClickListener () {
 			@Override
 			public void onClick (View v) {
 				listener.saveRequested (confNameEdit.getText ().toString ());
 			}
 		});
 
 		loadButton = (Button) anActivity.findViewById (R.id.confLoadBtn);
 		loadButton.setOnClickListener (new OnClickListener () {
 			@Override
 			public void onClick (View v) {
 				listener.loadRequested (confNameEdit.getText ().toString ());
 			}
 		});
 
 		deleteButton = (Button) anActivity.findViewById (R.id.confDeleteBtn);
 		deleteButton.setOnClickListener (new OnClickListener () {
 			@Override
 			public void onClick (View v) {
 				listener.deleteRequested (confNameEdit.getText ().toString ());
 			}
 		});
 		
 		cancelButton = (Button) anActivity.findViewById (R.id.confCancelBtn);
 		cancelButton.setOnClickListener (new OnClickListener () {
 			@Override
 			public void onClick (View v) {
 				listener.cancelRequested ();
 			}
 		});
 		
 		
 		chooseButton = (Button) anActivity.findViewById (R.id.confChooseBtn);
 		chooseButton.setOnClickListener (new OnClickListener () {
 			@Override
 			public void onClick (View v) {
 				listener.confNameChoiceRequested ();
 			}
 		});
 	}
 
 	
 	
 	public void setConfName (String aConfName) {
 		if (aConfName != null) {
 			confNameEdit.setText (aConfName);
 		}
 	}
 	
 	public void clearConfName () {
 		confNameEdit.setText ("");
 	}
 	
 	
 	public void enableActions () {
 		boolean hasText = confNameEdit.getText ().length () > 0;
 		saveButton.setEnabled (hasText);
 		loadButton.setEnabled (hasText);
 		deleteButton.setEnabled (hasText);
 	}
 
 
 	public void setChoiceEnabled (boolean anEnabledFlag) {
 		chooseButton.setEnabled (anEnabledFlag);
 	}
 	
 	
 	
 	private class NameEditListener implements OnEditorActionListener {
 
 		@Override
 		public boolean onEditorAction (TextView aView, int arg1, KeyEvent arg2) {
 			enableActions ();
 			return false;
 		}
 		
 	}
 	
 }
