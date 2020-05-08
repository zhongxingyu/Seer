 package com.typeiisoft.lct;
 
 import com.typeiisoft.lct.db.DataBaseHelper;
 import com.typeiisoft.lct.features.FeatureAdapter;
 import com.typeiisoft.lct.features.FeatureDialogFragment;
 import com.typeiisoft.lct.features.LunarFeature;
 
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 /**
  * This class handles displaying the features that are visible for the Lunar 
  * Club. The list of features will depend on the target type being requested.
  * 
  * @author Michael Reuter
  */
 public class LunarClubFeaturesFragment extends ListFragment {
 	/** Logging identifier. */
 	private static final String TAG = LunarClubFeaturesFragment.class.getName();
 	/** Holder for the current feature type. */
 	private String currentType;
 	
 	/**
 	 * This function is the instance constructor.
 	 * @param currType : The requested feature category.
 	 * @return : A new instance of the object with the requested category.
 	 */
 	static LunarClubFeaturesFragment newInstance(String currType) {
 		LunarClubFeaturesFragment lcf = new LunarClubFeaturesFragment();
 		Bundle data = new Bundle();
 		data.putString("feature_type", currType);
 		lcf.setArguments(data);
 		return lcf;
 	}
 	
 	/**
 	 * This function sets the initial information into fragment.
 	 * @param savedInstanceState : Object containing any state information.
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Getting the arguments from the Bundle object 
         Bundle data = getArguments();
         this.currentType = data.getString("feature_type");
     	Log.i(TAG, "Running onCreate for " + this.currentType + " tab.");
     }
 
 	/**
 	 * This function creates the Lunar Club features view.
 	 * @param inflater : The object that creates the view.
 	 * @param container : The layout container for the view.
 	 * @param savedInstanceState : Object containing any state information.
 	 * @return : The view for the fragment.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 		Log.i(TAG, "Creating " + this.currentType + " tab.");
		
 		// Inflate the layout for this fragment
    	View view = inflater.inflate(R.layout.features, container, false);
    	
     	// Set the content
     	DataBaseHelper moonDB = new DataBaseHelper(this.getActivity());
 		FeatureAdapter adapter = new FeatureAdapter(this.getActivity().getApplicationContext(), 
 				moonDB.getLunarClubFeatures(this.currentType));
 		this.setListAdapter(adapter);
		
		return view;
 	}

 	/**
 	 * This function shows detailed information on the feature when a list item 
 	 * is clicked.
 	 * @param l : The current ListView
 	 * @param v : The current View
 	 * @param position : The index of the item being clicked
 	 * @param id : The item ID
 	 */
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		DialogFragment featureFragment = new FeatureDialogFragment();
 		LunarFeature lf = (LunarFeature) l.getItemAtPosition(position);
 		featureFragment.setArguments(lf.toBundle());
 		featureFragment.show(this.getActivity().getSupportFragmentManager(), "lc_feature");
 	}
 }
