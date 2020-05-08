 package com.ichthus.patientrecordsystem;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.widget.CursorAdapter;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.FilterQueryProvider;
 import android.widget.Filterable;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewAllPatients extends Activity implements OnItemClickListener {
 	
 	AutoCompleteTextView tv;
 	ListView lv;
 	TextView fname, lastffup, diagnosis;
 	private DBHelper dbHelper;
 	Cursor cursor;
 	String[] search;
 	CustomAdapter ca;
 	ArrayAdapter<String> ad;
 	Typeface tf;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.viewallpatients);
 		
 		initControls();
 		
 	}
 	
 	private void initControls(){
 		
 		lv = (ListView) findViewById (R.id.lvListOfPatients);
 		lv.setOnItemClickListener(this);
 		lv.setTextFilterEnabled(true);
 
 		displayResults();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> lv, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		   // Get the cursor, positioned to the corresponding row in the result set
		   cursor = (Cursor) lv.getItemAtPosition(position);
		
		   String fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_FNAME + "|| ' ' ||" + DBHelper.KEY_LNAME));
 		   Toast.makeText(getApplicationContext(),
 		     fname, Toast.LENGTH_SHORT).show();
 	}
 	
 	private void displayResults(){
 		
 		dbHelper = new DBHelper(this);
 
         try {
 
                 dbHelper.createDataBase();
 
         } catch (IOException ioe) {
 
                 throw new Error("Unable to create database");
 
         }
 
         try {
 
                 dbHelper.openDataBase();
 
         } catch (SQLException sqle) {
 
                 throw sqle;
 
         }
         
         cursor = dbHelper.getAllPatients();
         //String[] data = new String[]{
         		//DBHelper.KEY_FNAME + "|| ' ' ||" + DBHelper.KEY_LNAME, DBHelper.KEY_DIAGNOSIS, DBHelper.KEY_LASTFFUP};
         //int[] to = new int[] {R.id.fullname, R.id.diagnosis, R.id.lastffup};
         //dataAdapter = new SimpleCursorAdapter(this, R.layout.custom_row, cursor, data, to, 0);
         
         ca = new CustomAdapter(this, cursor);
         
         tv = (AutoCompleteTextView) findViewById (R.id.etSearch);
 		
         search = dbHelper.getAllItemFilter();
         lv.setAdapter(ca); 
         lv.setTextFilterEnabled(true);
         lv.setFastScrollEnabled(true);
 
 		   // Print out the values to the log
 	       for(int i = 0; i < search.length; i++)
 	       {
 	           Log.i(this.toString(), search[i]);
 	       }
 	       ad = new ArrayAdapter<String>(this, R.layout.my_list_item, search);
 	       tv.setThreshold( 2 );
 		   tv.setAdapter( ad );
 		   tv.setTextColor( Color.BLACK );
 		   
 		   tv.addTextChangedListener(new TextWatcher() {
 			   public void afterTextChanged(Editable s) {
 			   }
 			 
 			   public void beforeTextChanged(CharSequence s, int start,
 			     int count, int after) {
 			   }
 			 
 			   public void onTextChanged(CharSequence s, int start,
 			     int before, int count) {
 			    ca.getFilter().filter(s.toString());
 			   }
 			  });
 			   
 			  ca.setFilterQueryProvider(new FilterQueryProvider() {
 			         public Cursor runQuery(CharSequence constraint) {
 			             return dbHelper.fetchPatientsByName(constraint.toString());
 			         }
 			     });
 		   
 	    //dbHelper.close();
   
 	}
 
 	public class CustomAdapter extends CursorAdapter implements Filterable
 	{
 		LayoutInflater inflater;
 		@SuppressWarnings("deprecation")
 		public CustomAdapter(Context context, Cursor c) {
 			super(context, c);
 			inflater = LayoutInflater.from(context);
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		// TODO Auto-generated method stub
 		TextView tv1 = (TextView)view.findViewById(R.id.fullname);
 		TextView tv2 = (TextView)view.findViewById(R.id.lastffup);
 		TextView tv3 = (TextView)view.findViewById(R.id.diagnosis);
 		
 		tf = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
 		
 		tv1.setTypeface(tf);
 		tv2.setTypeface(tf);
 		tv3.setTypeface(tf);
 		
 		int position = cursor.getPosition();
 		int mCount = cursor.getCount();
 		
 		if (position == 0 && mCount == 1) {
             view.setBackgroundResource(R.drawable.selector_rounded_corner_top_bottom);
         } else if (position == 0) {
             view.setBackgroundResource(R.drawable.selector_rounded_corner_top);
         } else if (position == mCount - 1) {
             view.setBackgroundResource(R.drawable.selector_rounded_corner_bottom);
         } else {
             view.setBackgroundResource(R.drawable.selector_list_entry_middle);
         }
 	 
 		String index = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_ID));
 		Log.d(index, index);
 		tv1.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_FNAME + "|| ' ' ||" + DBHelper.KEY_LNAME)));
 		tv2.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_LASTFFUP)));
 		tv3.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DIAGNOSIS)));
 		
 	}
 	 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		return inflater.inflate(R.layout.custom_row, parent, false);
 	 }
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		dbHelper.close();
 		super.onDestroy();
 	}
 	
 	
 	
 	
 }
