 package com.example.zonedhobbitsportfolio;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class CustomAdapter extends ArrayAdapter<Object> {
 	
 	private Context context;
 	private Object[] values;
 	
 	private int checkId;
 	private int setLayout;
 
 	public CustomAdapter(Context context, int resource, Object[] values) {
 		super(context, resource, values);
 		// TODO Auto-generated constructor stub
 		this.context = context;
 		this.values = values;
 		
 		checkId = resource;
 		
 		Log.d("ID ID ID ID", String.valueOf(checkId));
 		
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent){
 		
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.project_list_row, parent, false);
 		
 		return rowView;
 		
 	}
 	
	public int getLayout(){
 		
 		if(checkId == R.id.list_main){
 			return R.layout.project_list_row;
 		}
 		
 		// Remove comments and fix the if statement when list is added.
 		
 		/*
 		if(checkId == R.id.NAME FOR LIST){
 			return LAYOUT;
 		}
 		*/
 		
 		return 0;
 		
 	}
 
 }
