 package com.amca.android.replace;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.List;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class PlaceSelectorArrayAdapter extends ArrayAdapter<HashMap<String, String>>{
 	private final Context context;
 	private final List<HashMap<String, String>> values;
 
 	public PlaceSelectorArrayAdapter(Context context, List<HashMap<String, String>> values) {
 		super(context, R.layout.activity_place_selector_row, values);
 		this.context = context;
 		this.values = values;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View rowView = inflater.inflate(R.layout.activity_place_selector_row, parent, false);
 		TextView placeName = (TextView) rowView.findViewById(R.id.placeName);
 		TextView placeDesc = (TextView) rowView.findViewById(R.id.placeDesc);
 		TextView placeReviews = (TextView) rowView.findViewById(R.id.placeReviews);
 		TextView placeDistance = (TextView) rowView.findViewById(R.id.placeDistance);
 		
		DecimalFormat df = new DecimalFormat("0.00");
 		
 		HashMap<String, String> value = values.get(position);
 		placeName.setText(value.get("placeName"));
 		placeDesc.setText(value.get("placeDesc"));
 		placeReviews.setText(value.get("placeReviews") + " review(s)");
 		placeDistance.setText( df.format(Float.parseFloat(value.get("placeDistance")) / 1000) + " km");
 
 		return rowView;
 	}
 }
