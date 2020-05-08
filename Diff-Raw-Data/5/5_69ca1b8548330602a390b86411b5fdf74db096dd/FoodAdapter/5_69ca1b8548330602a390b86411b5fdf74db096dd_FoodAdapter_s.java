 package com.example.grubber;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FoodAdapter extends ArrayAdapter<FoodContent>{
 	private ArrayList<FoodContent> foodList;
 	private ImageLoader imageLoader;
 	public FoodAdapter(Context context, ArrayList<FoodContent> resultsList) {
 		super (context, R.layout.food_list_item, resultsList);
 		this.foodList = resultsList;
 		imageLoader = new ImageLoader(context);
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		// if null, inflate/render it
 		if (row == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			row = inflater.inflate(R.layout.food_list_item, null);
 		}
 		
 		FoodContent res = foodList.get(position);
 		
 		TextView tname = (TextView) row.findViewById(R.id.food_name);
 		TextView tdescription = (TextView) row.findViewById(R.id.food_description);
 		TextView tvote = (TextView) row.findViewById(R.id.food_vote);
 		ImageView icon = (ImageView) row.findViewById(R.id.food_icon);
 
 		//show picture
 		String picurl = "http://cse190.myftp.org/picture/"+ res.getFoodId() + ".jpg";
 		imageLoader.DisplayImage(picurl, icon);
 
  		if (tname != null)
  		{
  			String n = res.getName();
 			if(n.length() > 22)
 				tname.setText(n.substring(0, 22) + "...");
 			else
				tname.setText(n); 		
 		}
  		
 		if (tdescription != null)
 		{
 			String n = res.getDescription();
 			if(n.length() > 17)
				tdescription.setText(n.substring(0, 27) + "...");
 			else
 				tdescription.setText(n);
 		}
 		
 		if (tvote != null) {
 			tvote.setText(res.getVote());
 		}
 				
 		return row;
 	}
 }
