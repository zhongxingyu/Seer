 /* *************************************************************************************************
  *                                         eNotes                                                  *
  * *************************************************************************************************
  * File:        NoteView.java                                                                      *
  * Copyright:   (c) 2011-2012 Emanuele Alimonda, Giovanni Serra                                    *
  *              eNotes is free software: you can redistribute it and/or modify it under the terms  *
  *              of the GNU General Public License as published by the Free Software Foundation,    *
  *              either version 3 of the License, or (at your option) any later version.  eNotes is *
  *              distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without  *
  *              even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  *
  *              See the GNU General Public License for more details.  You should have received a   *
  *              copy of the GNU General Public License along with eNotes.                          *
  *              If not, see <http://www.gnu.org/licenses/>                                         *
  * *************************************************************************************************/
 
 package it.unica.enotes;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 /**
  * Activity to display the contents of a single note
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class NoteView extends Activity {
    private static final int kMenuItemEdit = 100;
    private static final int kMenuItemDelete = 101;
    private static final int kMenuItemSend = 102;
    long _noteID;
 
    private static final String kTag = "NoteView";
    private NoteDB database;
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Log.v(kTag, "created activity");
       this._noteID = -1;
       setContentView(R.layout.view);
       //Bundle extras = getIntentactivity().getExtras();
       Bundle extras = getIntent().getExtras();
       if (extras == null) {
     	  Log.v(kTag, "Invalid or missing bundle");
          return;
       }
 
       // shows selected note's details
       this._noteID = extras.getLong(Note.kID);
       Log.v(kTag, "Loading note: " + this._noteID);
       database = new NoteDB();
    }
    
    @Override
    public void onResume() {
       super.onResume();
       
       Note note = database.getNoteById(this, this._noteID);
       
       TextView titleField = (TextView) findViewById(R.id.ViewTitle);
       TextView contentsField = (TextView) findViewById(R.id.ViewContents);
       TextView tagsField = (TextView) findViewById(R.id.ViewTags);
       TextView urlField = (TextView) findViewById(R.id.ViewUrl);
 
       titleField.setText(note.getTitle());
       contentsField.setText(note.getText());
       tagsField.setText(note.getTagsAsString());
       urlField.setText(note.getURL());
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       menu.add(0, kMenuItemEdit, 1, R.string.editItem).setIcon(getResources().getDrawable(R.drawable.ic_menu_edit));
       menu.add(0, kMenuItemDelete, 2, R.string.deleteItem).setIcon(getResources().getDrawable(R.drawable.ic_menu_delete));
       menu.add(0, kMenuItemSend, 3, R.string.sendItem).setIcon(getResources().getDrawable(R.drawable.ic_menu_send));
       return true;
    }
    
    /*@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
       if (item.getItemId() == kMenuItemEdit) {
      	 Intent i = new Intent(this, NoteEdit.class);
      	 i.putExtra(Note.kID, this._noteID);
      	 startActivityForResult(i, 0);
       } else if (item.getItemId() == kMenuItemDelete) {
          if (database.deleteNote(this, this._noteID)) {
         	 this.finish();
          }
       }
         else if (item.getItemId() == kMenuItemSend) {
         	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
         	emailIntent.setType("plain/text");
         	startActivity(emailIntent);
       }
       return true;
    }*/
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
       if (item.getItemId() == kMenuItemEdit) {
      	 Intent i = new Intent(this, NoteEdit.class);
      	 i.putExtra(Note.kID, this._noteID);
      	 startActivityForResult(i, 0);
       } else if (item.getItemId() == kMenuItemDelete) {
          if (database.deleteNote(this, this._noteID)) {        	 
         	 new AlertDialog.Builder(this).setTitle("Confirm Delete")
              .setMessage("Do you want to delete this note?")
              .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {                	 
                 		 finish();
                 	 	}                 
              })
              .setNeutralButton("Cancel", null) // don't need to do anything but dismiss here
              .create()
              .show();
          }
       }
         else if (item.getItemId() == kMenuItemSend) {
        	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        	emailIntent.setType("plain/text");
        	startActivity(emailIntent);
       }
       return true;
    }
 
 }
 
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
