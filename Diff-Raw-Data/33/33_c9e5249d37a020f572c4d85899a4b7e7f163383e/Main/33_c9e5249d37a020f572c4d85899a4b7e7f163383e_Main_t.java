 package org.knuth.notebook;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.TwoLineListItem;
 
 public class Main extends ListActivity {
 	
 	private BookDatabase db_con;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Display a Note if nothing is in the Database:
         TextView empty = new TextView(this);
         empty.setText(R.string.empty_list_note);
         // IMPORTANT:
         empty.setVisibility(View.VISIBLE);
         ((ViewGroup)this.getListView().getParent()).addView(empty);
         // IMPORTANT END
         this.getListView().setEmptyView(empty);
     }
     
     /**
      * Populate the List with entrys:
      */
     @Override
     public void onStart(){
     	super.onStart();
    	db_con = new BookDatabase(this);
         listNotes();
     }
     
     private void listNotes(){
     	SQLiteDatabase db = db_con.getReadableDatabase();
     	final Cursor c = db.rawQuery("SELECT headline, id as '_id' " +
     			"FROM entry " +
     			"ORDER BY id DESC", null);
     	this.startManagingCursor(c);
     	final ListAdapter noteAdapter = new SimpleCursorAdapter(
     			this, 
     			android.R.layout.simple_list_item_2, c, 
     			new String[] {"headline", "_id"}, 
     			new int[] {android.R.id.text1, android.R.id.text2});
     	this.setListAdapter(noteAdapter);
     	// DONT CLOSE THE CURSOR!!!
     	db.close();
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id){
     	super.onListItemClick(l, v, position, id);
     	// Double-Row List Item with the Headline and ID:
     	TwoLineListItem curr = (TwoLineListItem) v;
     	TextView curr_line = (TextView) curr.getText2();
     	// Get the Value from the Second TextView:
     	Log.d("OnlyLog", curr_line.getText().toString());
     	// Intent:
     	Intent i = new Intent(this, DisplayNote.class);
     	i.putExtra("entry_id", curr_line.getText().toString() );
     	this.startActivity(i);
     }
     
     @Override
     protected void onPause(){
     	db_con.close();
     	super.onPause();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
     	this.getMenuInflater().inflate(R.menu.main_menu, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	switch (item.getItemId()){
     	case R.id.menu_create:
     		// Neuer Eintrag:
     		Intent i = new Intent(getApplicationContext(), NewNote.class);
     		this.startActivity(i);
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
 }
