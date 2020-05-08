 package com.typeiisoft.lct;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * This class handles the top-level Lunar Club information. This shows all of 
  * the tabs that contain the features based on the various target types.
  * 
  * @author Michael Reuter
  */
 public class LunarClubFragment extends Fragment {
 	/** Logging identifier. */
 	private final static String TAG = LunarClubFragment.class.getName();
 	/** Holder for the view pager in the tab. */
 	ViewPager pager;
 	
 	/**
 	 * This function creates the view for the Lunar Club main tab.
 	 * @param inflater : The object that creates the view.
 	 * @param container : The layout container for the view.
 	 * @param savedInstanceState : Object containing any state information.
 	 * @return : The view for the fragment.
 	 */
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
     	Log.i(TAG, "Running onCreateView");
         // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.lc_tab, container, false);
         
         // Getting a reference to the ViewPager defined the layout file 
         this.pager = (ViewPager) view.findViewById(R.id.pager);
         // Getting fragment manager 
        // Using the child fragment manager helps with remembering the 
        // state of the child fragments.
        FragmentManager fm = this.getChildFragmentManager();
         // Instantiating FragmentPagerAdapter
         LunarClubPagerAdapter pagerAdapter = new LunarClubPagerAdapter(fm);
  
         // Setting the pagerAdapter to the pager object
         this.pager.setAdapter(pagerAdapter);
 
         return view;
     }
 }
