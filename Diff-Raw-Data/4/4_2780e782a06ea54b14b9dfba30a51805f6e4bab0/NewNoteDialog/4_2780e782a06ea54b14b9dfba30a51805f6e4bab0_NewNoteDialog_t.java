 /*******************************************************
  *
  * Part of ch.blinkenlights.android.ntyfr
  *
  * (C) 2012 Adrian Ulrich
  *
  * Licensed under the GPLv2 (only)
  *
  *******************************************************/
 
 package ch.blinkenlights.android.ntyfr;
 
 /* Standard android stuff */
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 
 /* Widgets we are using */
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /* Menu stuff */
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 
 /* Some utils */
 import android.util.Log;
 import java.util.ArrayList;
 
 
 
 
 public class NewNoteDialog extends Activity {
 	
 	private NotificationManager notify_manager;
 	private NoteUtil            note_util;
 	private ConfigUtil          config_util;
 	private Stuff               stuff_util;
 	private final static int    ACT_ICON_PICKER = 1;
 	private static int          needs_init      = 1;
 	int current_nid = 0;
 	int current_icn = 0;
 	
 	/* Called when the activity is created
 	 * -> Reads the current extraBundle 'nid' and updates the global 'current_nid' value
 	*/
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		note_util      = new NoteUtil(getApplicationContext());
 		config_util    = new ConfigUtil(getApplicationContext());
 		stuff_util     = new Stuff(getApplicationContext());
 		notify_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		current_nid    = getIntent().getIntExtra("nid",0);
 		
 		/* load all notes if user didn't click on notemessage AND we
 		 * are freshly created
 		 */
 		if(current_nid <= 0 && needs_init == 1) {
 			LoadAllNotes();
			stuff_util.UpdateNewNoteShortcut(); /* Also called by DisplayNote but there might be no notes (yet) */
 		}
 		needs_init = 0;
 		
 		/* --> all notes are now initialized <-- */
 		
 		if(current_nid < 0) {
 			/* -> set by OnBoot receiver to initiate onboot load */
 			finish();
 		}
 		else {
 			/* -> normal call: display and pre-fill mainview */
 			setContentView(R.layout.main);
 			
 			((Button)findViewById(R.id.remove))
 			    .setVisibility( (current_nid == 0 ? View.GONE : View.VISIBLE) );
 			
 			((Button)findViewById(R.id.create))
 			    .setVisibility( (current_nid != 0 ? View.GONE : View.VISIBLE) );
 			
 			((Button)findViewById(R.id.update))
 			    .setVisibility( (current_nid == 0 ? View.GONE : View.VISIBLE) );
 			
 			if(current_nid == 0) {
 				setTitle(R.string.title_create_note);
 				
 				int rnd_max = -1; /* random starts at 0, IconCount at 1 */
 				if(config_util.ShowRandomIcon() == true) {
 					rnd_max += stuff_util.getIconCount();
 				} else {
 					rnd_max += stuff_util.getDotIconCount();
 				}
 				
 				SetCurrentIcon( GetRandomIcon(rnd_max) );
 			}
 			else {
 				setTitle(R.string.title_update_note);
 				/* load existing note and pre-fill form */
 				ArrayList this_note = note_util.LoadNoteById(current_nid);
 				
 				((EditText)findViewById(R.id.usertitle)).setText( (String)this_note.get(2) ); // title
 				((EditText)findViewById(R.id.usertext)).setText(  (String)this_note.get(3) ); // text
 				SetCurrentIcon( (Integer)this_note.get(1) );                                  // icon
 			}
 		}
 		
 	}
 	
 	@Override
 	protected void onActivityResult(int rqcode, int result, Intent data) {
 		super.onActivityResult(rqcode, result, data);
 		if(data != null && rqcode == ACT_ICON_PICKER) {
 			SetCurrentIcon(result);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		
 		/* SaveAsNew is only available while updating notes */
 		((MenuItem)menu.findItem(R.id.menu_save_as_new))
 		    .setEnabled( current_nid != 0 );
 		
 		return true;
 	}
 	
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_about:
 				(new Stuff(this)).ShowAboutDialog();
 				return true;
 			case R.id.menu_settings:
 				Intent cI = new Intent(NewNoteDialog.this, ConfigDialog.class);
 				startActivity(cI);
 				return true;
 			case R.id.menu_save_as_new:
 				AddNewNote();
 				finish();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	/* Returns a random icon - the current time is good enough */
 	private int GetRandomIcon(int max) {
 		int nowtime = (int)(System.currentTimeMillis() / 1000L);
 		return ( nowtime%(max+1) );
 	}
 	
 	private void SetCurrentIcon(int icon_id) {
 		current_icn = icon_id;
 		((ImageButton)findViewById(R.id.iconpicker)).setImageResource(R.drawable.x000 + icon_id);
 	}
 	
 	
 	/* Creates a new pending intent to ourself */
 	private PendingIntent ViewNoteIntent(int nid) {
 		PendingIntent pe = PendingIntent.getActivity(this, nid,
 		   new Intent(this, NewNoteDialog.class)
 		      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
 		      .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
 		      .putExtra("nid", nid),
 		   PendingIntent.FLAG_CANCEL_CURRENT);
 		return pe;
 	}
 	
 	
 	
 	/* Button callback -> Close this Dialog */
 	public void CancelNote(View v) {
 		finish();
 	}
 	
 	
 	/* Button Callback -> Creates a new note */
 	public void AddNewNote(View v) {
 		AddNewNote();
 		finish();
 	}
 	
 	private void AddNewNote() {
 		long noteid;
 		String this_title = ((EditText)findViewById(R.id.usertitle)).getText().toString();
 		String this_body  = ((EditText)findViewById(R.id.usertext)).getText().toString();
 		Resources res     = getResources();
 		
 		if(this_title.length()+this_body.length() > 0 ) {
 			int new_nid = (new NoteUtil(getApplicationContext())).CreateNote(current_icn, this_title, this_body);
 			
 			DisplayNoteInStatusBar(new_nid);
 			
 			Toast("\""+this_title+"\" "+res.getString(R.string.generic_saved));
 		}
 		else {
 			Toast(res.getString(R.string.generic_notxt));
 		}
 	}
 	
 	/* Button callback -> Update note */
 	public void UpdateExistingNote(View v) {
 		UpdateExistingNote();
 		finish();
 	}
 	
 	private void UpdateExistingNote() {
 		if(current_nid != 0) {
 			note_util.RemoveNoteById(current_nid);
 			notify_manager.cancel(current_nid);
 			AddNewNote();
 		}
 	}
 	
 	
 	/* Deletes a note from stable storage and removes it from statusbar */
 	public void RemoveNote(View v) {
 		new AlertDialog.Builder(this)
 		    .setTitle(R.string.dlg_remove_title)
 		    .setMessage(R.string.dlg_remove_message)
 		    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
 		        @Override
 		        public void onClick(DialogInterface dialog, int which) {
 		            RemoveNote();
 		            finish();
 		        }
 		    })
 		    .setNegativeButton(R.string.button_no, null)
 		    .show();
 	}
 	
 	public void RemoveNote() {
 		if(current_nid != 0) {
 			boolean could_remove = note_util.RemoveNoteById(current_nid);
 			
 			if(could_remove) {
 				notify_manager.cancel(current_nid);
 			}
 			
 		}
 	}
 	
 	
 	public void IconPicker(View v) {
 		startActivityForResult(new Intent(NewNoteDialog.this, IconPicker.class), ACT_ICON_PICKER);
 	}
 	
 	/* Button callback -> Debug operation */
 	public void DebugAction(View v) {
 		LoadAllNotes();
 		finish();
 	}
 
 	
 	/* Display/Render the given NID in statusbar */
 	private void DisplayNoteInStatusBar(int nid) {
 		
 		ArrayList this_note  = note_util.LoadNoteById(nid);
 		int       this_icon  = (Integer)this_note.get(1);
 		String    this_title = (String)this_note.get(2);
 		String    this_body  = (String)this_note.get(3);
 		
 		if(this_icon >= 0) {
 			Notification n = new Notification(R.drawable.x000 + this_icon, null, 0);
 			n.setLatestEventInfo(this, this_title, this_body, ViewNoteIntent(nid));
 			n.flags = Notification.FLAG_NO_CLEAR;
 			notify_manager.notify(nid,n);
			stuff_util.UpdateNewNoteShortcut(); /* Extrawurst for Android 4.x: pushes it upwards */
 		}
 	}
 	
 	
 	/* Wipe statusbar and load all notes */
 	private void LoadAllNotes() {
 		int nid = -1;
 		int i   = 0;
 		ArrayList<Integer> allnotes = note_util.GetAllNoteIds();		
 		
 		for(i=0;i<allnotes.size();i++) {
 			DisplayNoteInStatusBar( allnotes.get(i) );
 		}
 	}
 	
 	
 	private void DebugMessage(String s) {
 		Log.d("NewNoteDialog.java", s);
 		Toast(s);
 	}
 	
 	private void Toast(String s) {
 		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
 	}
 	
 }
