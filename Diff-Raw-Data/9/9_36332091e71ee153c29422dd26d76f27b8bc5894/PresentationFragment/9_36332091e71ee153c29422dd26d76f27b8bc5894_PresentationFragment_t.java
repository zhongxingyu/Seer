 package com.horeca;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class PresentationFragment extends Fragment {
 	private TextView distance_label = null;
 	private TextView distance = null;
 	private TextView horeca_numtel = null;
 	private TextView horeca_description = null;
 	private TextView horeca_pricerange = null;
 	private ImageButton favorite = null;
 	private Horeca horeca = null;
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
         Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.presentation_view, container, false);
         
 		// Open the db
 		MySqliteHelper sqliteHelper = new MySqliteHelper(getActivity());
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		// Get the horeca from which to take the plats
 		// specified by the HorecaListActivity calling this activity
 		Bundle b = getActivity().getIntent().getExtras();
 		horeca = new Horeca(b.getLong("horeca_id"), db);
 		
 		// close db
 		db.close();
 		
 		// it sets the title because it is the default tab
 		getActivity().setTitle(horeca.getName());
 		
 		GPSTracker gps = new GPSTracker(getActivity());
 		distance = (TextView) view.findViewById(R.id.horeca_distance);
		if (gps.getLocation() != null) {
 			// I cast it to long to get less precision decimals
 			distance.setText(String.valueOf((long) horeca.getDistance(gps)) + " m");
 		} else {
 			distance.setVisibility(View.GONE);
 			distance_label = (TextView) view.findViewById(R.id.horeca_distance_label);
 			distance_label.setVisibility(View.GONE);
 		}
 		
 		horeca_numtel = (TextView) view.findViewById(R.id.horeca_numtel);
 		horeca_numtel.setText(horeca.getNumtel());
         
 		horeca_description = (TextView) view.findViewById(R.id.horeca_description);
 		if (horeca.hasDescription()) {
 			horeca_description.setText(horeca.getDescription());
 		} else {
 			view.findViewById(R.id.horeca_description_label).setVisibility(View.GONE);
 			horeca_description.setVisibility(View.GONE);
 		}
 		horeca_pricerange = (TextView) view.findViewById(R.id.horeca_pricerange);
 		horeca_pricerange.setText("You can eat here from " + horeca.getMinPrice() + " € to " + horeca.getMaxPrice() + " €.");
 		favorite=(ImageButton)view.findViewById(R.id.favorite);
 		if (horeca.getIsFavorite()) {
 			favorite.setImageResource(R.drawable.star_on);
 		} else {
 			favorite.setImageResource(R.drawable.star_off);
 		}
 	    favorite.setOnClickListener(new AdapterView.OnClickListener(){
 	    	@Override
 	    	public void onClick(View v){
 	    		if (horeca.getIsFavorite()) {
 	    			favorite.setImageResource(R.drawable.star_off);
 	    		} else {
 	    			favorite.setImageResource(R.drawable.star_on);
 	    		}
 	    	}
 	    });
 		
 		return view;
     }
 }
