 package org.knuth.notebook;
 
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.TwoLineListItem;
 
 public class SearchNotes extends ListActivity{
 	
 	private BookDatabase db_con;
 	private String query;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Display a Note if nothing is in the Database:
         TextView empty = new TextView(this);
         empty.setText(R.string.empty_search_note);
         // IMPORTANT:
         empty.setVisibility(View.VISIBLE);
         ((ViewGroup)this.getListView().getParent()).addView(empty);
         // IMPORTANT END
         this.getListView().setEmptyView(empty);
         // Getting the Search-Query:
         Intent i = this.getIntent();
         if (i.getAction().equals(Intent.ACTION_SEARCH)){
         	query = i.getStringExtra(SearchManager.QUERY);
         }
 	}
 	
 	private void searchAndDisplay(){
 		SQLiteDatabase db = db_con.getReadableDatabase();
 		final Cursor c = db.rawQuery(
 				"SELECT headline, id as '_id' FROM entry WHERE " +
 				"(headline LIKE ?) OR (content LIKE ?) " +
 				"ORDER BY id DESC",
 				new String[] {"%"+query+"%", "%"+query+"%"});
		this.startManagingCursor(c);
 		final ListAdapter searchAdapter = new SimpleCursorAdapter(
 				this, 
     			android.R.layout.simple_list_item_2, c, 
     			new String[] {"headline", "_id"}, 
     			new int[] {android.R.id.text1, android.R.id.text2});
 		this.setListAdapter(searchAdapter);
 		db.close();
 	}
 	
 	@Override
     public void onStart(){
     	super.onStart();
     	db_con = new BookDatabase(getApplicationContext());
         searchAndDisplay();
     }
 	
 	@Override
     protected void onListItemClick(ListView l, View v, int position, long id){
     	super.onListItemClick(l, v, position, id);
     	// Zwei-Reihen List Item beinhaltet zwei TextViews:
     	TwoLineListItem curr = (TwoLineListItem) v;
     	TextView curr_line = (TextView) curr.getText2();
    	// Aus zweitem TextView wert auslesen:
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
 
 }
