 package com.valfom.skydiving.altimeter;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class AltimeterSimpleCursorAdapter extends SimpleCursorAdapter {
 
 	private Context context;
 	
 	public AltimeterSimpleCursorAdapter(Context context, int layout, Cursor c,
 			String[] from, int[] to, int flags) {
 		
 		super(context, layout, c, from, to, flags);
 		
 		this.context = context;
 	}
 	
	public int getRowType(int position) {
 		
 		int type;
 		Cursor cursor;
 		
 		cursor = getCursor();
 		cursor.moveToPosition(position);
 		type = cursor.getInt(cursor.getColumnIndex(AltimeterDB.KEY_TRACKS_TYPE));
 		
 		return type;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
     	
 		if (convertView == null) {
 		
 			int type;
 			Cursor cursor;
 			
 			cursor = getCursor();
 			cursor.moveToPosition(position);
 			
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			
			type = getRowType(position); 
 			
 			if (type == 1) {
 			
 				convertView = inflater.inflate(R.layout.list_row, null, true);
 				
 				TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
 				
 				String time = cursor.getString(cursor.getColumnIndex(AltimeterDB.KEY_TRACKS_TIME));
 				
 				tvTime.setText(time);
 				
 			} else if (type == 2) {
 				
 				convertView = inflater.inflate(R.layout.list_row_section, null, true);
 				
 				TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
 				
 				String date = cursor.getString(cursor.getColumnIndex(AltimeterDB.KEY_TRACKS_DATE));
 				
				tvDate.setText("Header " + date);
 			}
 		}
 	    
         return convertView;
     }
 }
