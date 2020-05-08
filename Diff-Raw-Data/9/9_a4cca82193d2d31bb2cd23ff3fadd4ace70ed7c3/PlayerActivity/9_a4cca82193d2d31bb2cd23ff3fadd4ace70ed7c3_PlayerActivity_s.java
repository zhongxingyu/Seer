 package com.jlebrech.matchreport;
 
 //import static android.provider.BaseColumns._ID;
 import android.app.ListActivity;
 //import android.content.ContentValues;
 import android.database.Cursor;
 //import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.SimpleCursorAdapter;
 
 public class PlayerActivity extends ListActivity {
   PlayerModel player;
   TextView output;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
     setContentView(R.layout.list);
    
     player = new PlayerModel(this);
     
     player.addPlayer("Hello Android Event");
     Cursor cursor = player.getAll();
     
 // the desired columns to be bound
     String[] columns = new String[] { PlayerDBHelper.TITLE, PlayerDBHelper.TIME };
     // the XML defined views which the data will be bound to
     int[] to = new int[] { R.id.name_entry, R.id.number_entry };
 
     // create the adapter using the cursor pointing to the desired data as well as the layout information
     SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.list_item, cursor, columns, to);
 
     // set this adapter as your ListActivity's adapter
     this.setListAdapter(mAdapter);
   }
   
   @Override
   public void onDestroy() {
     player = null;
   }
 }
