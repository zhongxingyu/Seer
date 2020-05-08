 package com.engine9;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 //Converts a Vector of strings into a list of strings
 public class FavouriteAdapter extends ArrayAdapter<String> {
 	
 	private Context context;
 	private Vector<String> favs;
 	
 	public FavouriteAdapter(Context context, Vector<String> favs){
 		super(context, R.layout.list_favourite, favs);
 		this.context = context;
 		this.favs = favs;
 		
 	}
 	
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return favs.size();
 	}
 
 
 	@Override
 	public long getItemId(int arg0) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
 	 * @param position 
 	 */
 	@Override
 	public View getView(int position, View convert, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		View row = inflater.inflate(R.layout.list_favourite, parent, false);
		TextView favV = (TextView) row.findViewById(R.id.fav_text);
 		
 		if(favs.size() > 0){
 			favV.setText(favs.get(position));
 		}
 		return row;
 	}
 
 }
