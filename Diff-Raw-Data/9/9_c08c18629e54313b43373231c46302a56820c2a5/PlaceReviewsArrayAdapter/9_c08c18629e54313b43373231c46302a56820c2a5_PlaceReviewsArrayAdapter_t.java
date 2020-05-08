 package com.amca.android.replace;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class PlaceReviewsArrayAdapter extends ArrayAdapter<HashMap<String, String>>{
 	private final Context context;
 	private final ArrayList<HashMap<String, String>> values;
 
 	public PlaceReviewsArrayAdapter(Context context, ArrayList<HashMap<String, String>> values) {
 		super(context, R.layout.activity_place_reviews_row, values);
 		this.context = context;
 		this.values = values;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View rowView = inflater.inflate(R.layout.activity_place_reviews_row, parent, false);
 		TextView reviewUser = (TextView) rowView.findViewById(R.id.reviewUser);
 		TextView reviewPoint = (TextView) rowView.findViewById(R.id.reviewPoint);
 		TextView similarity = (TextView) rowView.findViewById(R.id.similarity);
 		TextView reviewText = (TextView) rowView.findViewById(R.id.reviewText);
 		
		DecimalFormat df = new DecimalFormat("#0.##");
 
 		HashMap<String, String> value = values.get(position);
 		reviewUser.setText(value.get("reviewUser"));
 		reviewPoint.setText(value.get("reviewPoint") + " star(s)");
 		similarity.setText(df.format(Float.parseFloat(value.get("similarity"))) + " similar");
 		reviewText.setText(value.get("reviewText"));
 
 		return rowView;
 	}
 }
