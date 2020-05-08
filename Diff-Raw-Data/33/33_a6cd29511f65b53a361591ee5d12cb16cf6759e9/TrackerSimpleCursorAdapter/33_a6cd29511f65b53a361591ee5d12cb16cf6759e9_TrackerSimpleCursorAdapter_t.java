 package com.valfom.tracker;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class TrackerSimpleCursorAdapter extends SimpleCursorAdapter {
 
 	private Cursor cursor;
 	private Activity activity; 
 	private static LayoutInflater inflater = null;
 	
 	@SuppressWarnings("deprecation")
 	public TrackerSimpleCursorAdapter(Context context, int layout, Cursor c,
 			String[] from, int[] to) {
 		
 		super(context, layout, c, from, to);
 		
 		activity = (Activity) context;
 		cursor = c;
 		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
     	
         View v = convertView;
         
         if(convertView == null)
         		v = inflater.inflate(R.layout.list_row, null);
  
         TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
         TextView tvDistance = (TextView) v.findViewById(R.id.tvDistance);
         TextView tvDuration = (TextView) v.findViewById(R.id.tvDuration);
         TextView tvId = (TextView) v.findViewById(R.id.tvId);
         
         tvId.setVisibility(View.GONE);
 
         cursor.moveToPosition(position);
         	
 	    int id = cursor.getInt(cursor.getColumnIndex(DB.KEY_PREFIX_ID));
 	    double distance = cursor.getDouble(cursor.getColumnIndex(DB.KEY_DIST));
 	    String date = cursor.getString(cursor.getColumnIndex(DB.KEY_DATE));
 	    long duration = cursor.getLong(cursor.getColumnIndex(DB.KEY_DURATION));
 	    
 	    TrackerSettings settings = new TrackerSettings(activity);
 	    
 	    distance = settings.convertDistance(distance);
 	    
 	    long millis = duration;
 		int seconds = (int) (millis / 1000);
 		int minutes = seconds / 60;
 		seconds     = seconds % 60;
 		int hours = minutes / 60;
 		minutes = minutes % 60;
 	    
 	    tvId.setText(String.valueOf(id));
 	    tvDate.setText(date);
 	    tvDistance.setText(String.format("%.2f", distance));
 	    tvDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
 			    
         return v;
     }
 }
