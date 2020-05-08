 package com.example.therunningapp;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.example.therunningapp.TrappContract.TrappEntry;
 
 public class History extends Activity {
 
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_history);
 		TrappDBHelper mDbHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		
 		
 		String[] projection = {TrappEntry._ID, TrappEntry.COLUMN_NAME_DATE , TrappEntry.COLUMN_NAME_DISTANCE, TrappEntry.COLUMN_NAME_TIME, TrappEntry.COLUMN_NAME_CALORIES};
 		
 		String sortOrder = TrappEntry.COLUMN_NAME_DATE + " DESC";
 		
 		Cursor c = db.query(TrappEntry.TABLE_NAME, projection, null, null,null,null,sortOrder);
 		ListView list = (ListView) findViewById(R.id.listViewHistory);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.list_content);
 
 		if(c.moveToFirst()){
 			do{
 				String date = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_DATE));
 				String distance = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_DISTANCE));
 				String time = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_TIME));
 				String calories = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_CALORIES));
 				adapter.add(date);
 				adapter.add(distance);
 				adapter.add(time);
 				adapter.add(calories);
 			
 			}while(c.moveToNext());
 			list.setAdapter(adapter);
 		}
 		
 		db.close();
 	}
 	
 	public void back(View view){
 		finish();
 	}
 	
 	
 	}
 /*
 	  public void onItemClick(AdapterView<?> l, View v, int position, long id) {
 	        Log.i("TAG", "You clicked item " + id + " at position " + position);
 	        // Here you start the intent to show the contact details
 	    }
 }
 */
 
