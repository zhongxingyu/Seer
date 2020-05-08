 package org.telekommunisten;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ListPdn extends ListActivity {
 
 	private Cursor cursor;
 	private String force = "";
 	private String tag = "ListPdn";
 	
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
     @Override
     public void onListItemClick(ListView l, View v, int pos, long id) {
         String pdntext = (String) l.getItemAtPosition(pos);
         Log.d("dialstation", pdntext);
 
 //        startActivity(new Intent(Intent.ACTION_DIAL,  Uri.parse("tel:+491792900944")));
 //        startActivity(new Intent(Intent.ACTION_DIAL,  Uri.parse("tel:+" + pdntext )));
         startActivity(new Intent(Intent.ACTION_CALL,  Uri.parse("tel:+" + pdntext )));
 
     }
 
 
 	    @Override
 	    protected void onStart() {
 	    	// TODO Auto-generated method stub
 	    	Log.d(tag,"onstart");
 	    	super.onStart();
 	    	if (PreferenceManager.getDefaultSharedPreferences(this).getString("dialstation_user_path", null) == null)
 	    	{
 	    		startActivity(new Intent(this,SettingsActivity.class));
 	    		
 	    		Toast.makeText(this, "enter credentials"+force, Toast.LENGTH_LONG).show();
 	    		force += "!!!";
 	    		if (force.contains("!!!!!!!!!!!!")) force = " AND DESTROY YOUR PHONE!";
 	    	}
 	    	else {
 	    		Log.d(tag,"really provider called");
 		    	cursor = getContentResolver().query(Uri.parse("content://com.dialstation"), null, null, null, null);
 		    	//Log.d(tag,"cols.....: "+cursor.getColumnCount());
 		    	Log.d(tag,"nooooclomun");
 
 		    	setListAdapter(new BaseAdapter() {
 					
 					@Override
 					public View getView(int position, View convertView, ViewGroup parent) {
 						if (convertView == null) {
 							convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);				
 						} 
 						
 						cursor.moveToPosition(position);
						((TextView) convertView.findViewById(android.R.id.text1)).setText(cursor.getString(6));
						((TextView) convertView.findViewById(android.R.id.text2)).setText(cursor.getString(1));
 
 						((TextView) convertView.findViewById(android.R.id.text1)).setTextColor(Color.GREEN);
 						((TextView) convertView.findViewById(android.R.id.text2)).setTextColor(Color.RED);
 //						((TextView) convertView).setTextSize(42);
 //						((TextView) convertView).setTextColor(Color.GREEN);
 						return convertView;
 					}
 					
 					@Override
 					public long getItemId(int position) {
 						cursor.moveToPosition(position);
 						return Long.valueOf(cursor.getString(cursor.getColumnIndex("id")));
 	
 						// TODO Auto-generated method stub
 					}
 					
 					@Override
 					public Object getItem(int position) {
 						// TODO Auto-generated method stub
 						cursor.moveToPosition(position);
 						return cursor.getString(1);
 					}
 					
 					@Override
 					public int getCount() {
 						// TODO Auto-generated method stub
 						Log.d("ListPdn",cursor.getCount()+"<- cursor.length");
 						return cursor.getCount();
 					}
 				});
 	    	}
 	    }
 	   
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	        MenuInflater inflater = getMenuInflater();
 	        inflater.inflate(R.menu.main, menu);
 	        return super.onCreateOptionsMenu(menu);
 	    }
 
 		@Override
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
 
 	        case R.id.search:
 	            onSearchRequested();
 	            break;
 
 	        case R.id.settings:
 	            startActivity(new Intent(this, SettingsActivity.class));
 	            break;
 	        case R.id.feedback:
 	            LogCollector.feedback(this, "flo@andlabs.de", "blah blah blah");
 	            break;
 	        }
 	        return super.onOptionsItemSelected(item);
 	    }
 
 	}
 	
 	
