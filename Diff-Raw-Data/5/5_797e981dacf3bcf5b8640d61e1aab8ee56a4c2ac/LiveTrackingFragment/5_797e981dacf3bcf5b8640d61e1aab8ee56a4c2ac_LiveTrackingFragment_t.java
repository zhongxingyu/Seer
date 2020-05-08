 package de.dhbw.contents;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import de.dhbw.container.R;
 import de.dhbw.database.AnalysisCategory;
 import de.dhbw.database.CategoryPosition;
 import de.dhbw.database.Coordinates;
 import de.dhbw.database.DataBaseHandler;
 import de.dhbw.tracking.GPSTracker;
 
 public class LiveTrackingFragment extends SherlockFragment {
 
 	private GPSTracker gps;
     int i;
     private DataBaseHandler db;
     private View mView;
     
 	public LiveTrackingFragment() {
 		// Empty constructor required for fragment subclasses
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// inflate and return the layout
 		View v = inflater.inflate(R.layout.live_tracking_fragment, container,
 				false);
 		
 		v.findViewById(R.id.mapview).setVisibility(View.GONE);
 		
 		db = new DataBaseHandler(getActivity());
 
 		Button trackingButton = (Button) v.findViewById(R.id.tracking);
 		trackingButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				changeTrackingState(view);
 			}
 		});
 		
 		return v;
 	}
 	
 	@Override
 	public void onResume() {
 		// Set List
 		mView = getView();
 		setList();
 		super.onResume();
 	}
 	
 	public void setList()
 	{
 		
 		for (i=0; i<7; i++)
 		{
 			AnalysisCategory ac = db.getAnalysisCategoryById(db.getCategoryIdByPosition(i+1));
 			if (ac == null)
 				continue;
 			
 			int viewId = getResources().getIdentifier("workout_element_"+i, "id", getActivity().getPackageName());
 			View listElement = mView.findViewById(viewId);
 			listElement.setOnClickListener(new CustomListOnClickListener());
 			
 			String format = ac.getFormat();
 			TextView valueView = ((TextView) listElement.findViewById(R.id.live_tracking_element_value_text));
 			
 			//Werte zuordnen
 			switch(ac.getId())
 			{
 				case 1:		//Dauer
 					break;
 				case 2:		//Distanz
 					break;
 				case 3:		//Seehhe
 					break;
 				case 4:		//Hhenmeter aufwrts
 					break;
 				case 5:		//Hhenmeter abwrts
 					break;
 				case 6:		//Kalorien
 					break;
 				case 7:		//Durchschnittsgeschwindigkeit
 					break;
 				case 8:		//Zeit
 					Calendar c = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
 					valueView.setText(sdf.format(c.getTime()));
 					break;
 				default:	//Wird nie erreicht
 					break;
 			}
 			
 			if (format.equals("hh:mm:ss"))
 				valueView.setText("00:00:00");
 			else if (format.equals("km"))
 				valueView.setText("0");
 			else if (format.equals("m"))
 				valueView.setText("0");
 			else if (format.equals("kcal"))
 				valueView.setText("0");
 			else if (format.equals("kmh"))
 				valueView.setText("0,0");	
 			else if (format.equals("hh:mm"))
 			{
 				if (!ac.getName().equals("Zeit"))
 					valueView.setText("00:00");
 			}
 			
 			int imageId = getResources().getIdentifier(ac.getImageName(), "drawable", getActivity().getPackageName());
 			((ImageView) listElement.findViewById(R.id.live_tracking_element_value_icon)).setImageResource(imageId);
 						
 			((TextView) listElement.findViewById(R.id.live_tracking_element_name)).setText(ac.getName() + " (" + ac.getFormat() + ")");
 		}
 	}
 	
 	private class CustomListOnClickListener implements View.OnClickListener
 	{
 		@Override
 		public void onClick(View v) {
 			for (int i=0; i<7; i++)
 			{
 				if (v.getId() == getResources().getIdentifier("workout_element_"+i, "id", getActivity().getPackageName()))
 				{
 					CategoryListFragment mCategoryListFragment = new CategoryListFragment();
 					Bundle args = new Bundle();
 					args.putInt("position", i);
 					
 					mCategoryListFragment.setArguments(args);
 					if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
 						getActivity().getSupportFragmentManager().beginTransaction()
 								.replace(R.id.currentFragment, mCategoryListFragment)
 								.addToBackStack(null).commit();
 					} else {
 						getActivity().getSupportFragmentManager().beginTransaction()
 								.replace(R.id.currentFragment, mCategoryListFragment).commit();
 					}
 				}
 			}
 			
 		}	
 	}
 
 	public void changeTrackingState(View view) {
 		if (view.getTag() == null) {
 			gps = new GPSTracker(getActivity(), this);
 			view.setTag(1);
 			((View)view.getParent()).findViewById(R.id.mapview).setVisibility(View.VISIBLE);
 			((TextView) view).setText(getString(R.string.button_workout_stop));
 		} else if ((Integer) view.getTag() == 1) {
 			if (gps.canGetLocation()) {
 				
 				DataBaseHandler db = new DataBaseHandler(getActivity());
 				List <Coordinates> listContents = new ArrayList<Coordinates>();
 				listContents = db.getAllCoordinatePairs();
 				
 				for (i = 0; i < listContents.size(); i++) {
 					Toast.makeText(
 							getActivity(),
 							"Your Location is - \nLon: "
 									+ listContents.get(i).get_longitude()
 									+ "\nLat: "
 									+ listContents.get(i).get_latitude()
 									+ "\nAlt: "
 									+ gps.getElevationFromGoogleMaps(
 											listContents.get(i).get_longitude(),
 											listContents.get(i).get_latitude())
 									+ "\nTime: "
 									+ listContents.get(i).get_timestamp(),
 							Toast.LENGTH_LONG).show();
 				}
 			}else {
 				gps.showSettingsAlert();
 			}
 			view.setTag(2);
 			mView.findViewById(R.id.workout_layout).setVisibility(View.GONE);
 			mView.findViewById(R.id.mapview).setVisibility(View.VISIBLE);
 			((TextView) view).setText(getString(R.string.button_workout_analyse));
 		} else if ((Integer) view.getTag() == 2) {
 			gps.stopUsingGPS();
 			getToTrackingEvaluation();
 		}
 	}
 
 	public void getToTracking(SherlockFragment single_evaluation) {
 		getActivity().getSupportFragmentManager().beginTransaction()
 				.add(R.id.currentFragment, single_evaluation).commit();
 	}
 
 	public void getToTrackingEvaluation() {
 		getActivity()
 				.getSupportFragmentManager()
 				.beginTransaction()
 				.replace(R.id.currentFragment,
 						EvaluationViewPager.newInstance(),
 						EvaluationViewPager.TAG).addToBackStack(null).commit();
 	}
 
 	public void getToTotalEvaluation(SherlockFragment total_evaluation) {
 		if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
 			getActivity().getSupportFragmentManager().beginTransaction()
 					.replace(R.id.currentFragment, total_evaluation)
 					.addToBackStack(null).commit();
 		} else {
 			getActivity().getSupportFragmentManager().beginTransaction()
 					.replace(R.id.currentFragment, total_evaluation).commit();
 		}
 	}
 
 }
