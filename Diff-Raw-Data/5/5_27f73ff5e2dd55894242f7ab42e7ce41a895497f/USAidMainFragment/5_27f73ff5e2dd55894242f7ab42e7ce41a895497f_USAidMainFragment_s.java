 /**
  * Copyright (c) 2013 Thermopylae Sciences and Technology. All rights reserved.
  */
 package com.tscience.usaid.evaluations;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 import com.tscience.usaid.evaluations.io.USAidListDataTask;
 import com.tscience.usaid.evaluations.utils.USAidDataObject;
 import com.tscience.usaid.evaluations.utils.USAidUtils;
 
 /**
  * This fragment displays the main screen.
  * 
  * @author spotell at t-sciences.com
  */
 public class USAidMainFragment extends SherlockListFragment {
     
     /** Log id of this class name. */
     private static final String LOG_TAG = "USAidMainFragment";
     
     private USAidListAdapter myListAdapter;
     
     private ArrayList<USAidDataObject> currentData;
     
     private Menu myMenu;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         
         View listView = inflater.inflate(R.layout.fragment_usaid_main, container, false);
             
         // start getting data
         USAidListDataTask usaidListDataTask = new USAidListDataTask(this);
         usaidListDataTask.execute(getString(R.string.usaid_json_query));
         
         return listView;
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         
     	// get the data object
     	USAidDataObject usaidDataObject = currentData.get(position);
     	
     	// make the new bundle
     	Bundle bundle = new Bundle();
     	bundle.putParcelable(USAidConstants.USAID_BUNDLE_DATA_OBJECT, usaidDataObject);
     	
     	// create the view
     	USAidDescriptionDialog usaidDescriptionDialog = USAidDescriptionDialog.newInstance(bundle);
     	usaidDescriptionDialog.show(getActivity().getSupportFragmentManager(), "description");
     	
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         
         inflater.inflate(R.menu.usaid_main, menu);
         
         // save instance of menu
         myMenu = menu;
         
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         
         int currentItemId = item.getItemId();
         
         if (currentItemId == R.id.action_filter_reset) {
             
             setTheListData(currentData, false, false);
             
             // uncheck everything
            getActivity().invalidateOptionsMenu();
             
             return true;
             
         } else if (currentItemId == R.id.action_load_cache) {
             
             // uncheck everything
            getActivity().invalidateOptionsMenu();
             
             USAidListDataTask usaidListDataTask = new USAidListDataTask(this);
             usaidListDataTask.execute("");
                 
         }  else if (currentItemId == R.id.action_download_pdf_viewer) {
             
             USAidUtils.getAdobeReader(getActivity());
             return true;
             
         } else if (currentItemId == R.id.action_about) {
             
             // create the view
             USAidAboutFragment usaidAboutFragment = new USAidAboutFragment();
             usaidAboutFragment.show(getActivity().getSupportFragmentManager(), "about");
             
         }
         
         // handle if checkbox
         if (item.isCheckable()) {
         
             if (item.isChecked()) {
                 item.setChecked(false);
             } else {
                 item.setChecked(true);
             }
             
             displaySectors();
             return true;
         
         }
         
         return false;
     }
 
     /**
      * Display the list data.
      * 
      * @param value The array of USAidDataObject's to display.
      * @param update    Update the current data (only when pull from server).
      * @param cachedData    False this is live data or True this is cached data.
      */
     public void setTheListData(ArrayList<USAidDataObject> value, boolean update, boolean cachedData) {
         
         if (update) {
             currentData = value;
         }
         
         try {
             // add padding around list
             this.getListView().setPadding(24, 24, 24, 24);
             
             // add space between items
             this.getListView().setDividerHeight(24);
             
             // attach to the list
             myListAdapter = new USAidListAdapter(getActivity(), R.layout.usaid_list_layout, value);
             
             this.setListAdapter(myListAdapter);
             
             myListAdapter.notifyDataSetChanged();
             
         } catch (Exception ignore) {
             Log.e(LOG_TAG, "---------------------------------------- " + ignore.toString());
         }
         
         // if cached data show toast
         if (cachedData) {
             
             Toast.makeText(getActivity(), R.string.usaid_json_cache_file_toast, Toast.LENGTH_LONG).show();
             
         }
         
     } // end setTheListData
     
     public void noCachedData() {
         
         Toast.makeText(getActivity(), R.string.usaid_cache_nodata, Toast.LENGTH_LONG).show();
         
     }
     
     /**
      * Creates a new array of USAidDataObject's for a sector.
      * 
      * @param value The sorted list with sector selections.
      */
     private void displaySectors() {
         
         if ((currentData == null) || (currentData.size() == 0)) {
             
             setTheListData(null, false, false);
             return;
             
         }
         
         // get the submenu
         SubMenu subMenu = myMenu.findItem(R.id.action_sector).getSubMenu();
         
         int menuSize = subMenu.size();
         
         Log.d(LOG_TAG, "----------------------------------------menuSize sectors: " + menuSize);
         
         MenuItem menuItem = null;
         
         Vector<Integer> checkedVector = new Vector<Integer>();
         
         // what menu items are checked
         for (int x = 0; x < menuSize; x++) {
             
             menuItem = subMenu.getItem(x);
             
             if (menuItem.isChecked()) {
                 
                 int checkedNum = Integer.valueOf(USAidUtils.getMenuItemSectorConstant(menuItem.getItemId()));
                 
                 if (checkedNum >= 0) {
                     checkedVector.add(checkedNum);
                 }
                 
             }
             
         } // end looking for checked menu items
         
         // size of checked menu items
         int numberChecked = checkedVector.size();
         
         Log.d(LOG_TAG, "---------------------------------------- numberChecked sectors: " + numberChecked);
         
         ArrayList<USAidDataObject> newData = null;
         
         if (numberChecked == 0) {
             
             // display all
             newData = currentData;
             
         } else {
             
             // only display filtered items
             newData = new ArrayList<USAidDataObject>();
             
             int maxValues = currentData.size();
             
             for (int i = 0; i < maxValues; i++) {
                 
                 // check each one of these against checked vector
                 for (int j = 0; j < numberChecked; j++) {
                 
                     if (currentData.get(i).sectorValue == checkedVector.get(j).intValue()) {
                         newData.add(currentData.get(i));
                     }
                     
                 }
                 
             } // end maxValues
         
         }
         
         // now check the regions before set datalist
         displayCountryAndRegions(newData);
         
     } // end displaySectors
     
     /**
      * This method does the same thing as displaySectors but for the regions.
      * 
      * @param value The sorted list with regions selections.
      */
     private void displayCountryAndRegions(ArrayList<USAidDataObject> value) {
         
         if ((value == null) || (value.size() == 0)) {
             
             setTheListData(null, false, false);
             return;
             
         }
         
         // get the region submenu
         SubMenu subMenu = myMenu.findItem(R.id.action_region).getSubMenu();
         
         int menuSize = subMenu.size();
         
         Log.d(LOG_TAG, "----------------------------------------menuSize region: " + menuSize);
         
         MenuItem menuItem = null;
         
         Vector<Integer> checkedRegionVector = new Vector<Integer>();
         
         // what menu items are checked
         for (int x = 0; x < menuSize; x++) {
             
             menuItem = subMenu.getItem(x);
             
             if (menuItem.isChecked()) {
                 
                 int checkedNum = Integer.valueOf(USAidUtils.getMenuItemRegionConstant(menuItem.getItemId()));
                 
                 if (checkedNum >= 0) {
                     checkedRegionVector.add(checkedNum);
                 }
                 
             }
             
         } // end looking for checked menu items
         
         // get the country's checked
         subMenu = myMenu.findItem(R.id.action_country).getSubMenu();
         
         menuSize = subMenu.size();
         
         Log.d(LOG_TAG, "----------------------------------------menuSize country: " + menuSize);
         
         menuItem = null;
         
         Vector<Integer> checkedCountryVector = new Vector<Integer>();
         
         // what menu items are checked
         for (int x = 0; x < menuSize; x++) {
             
             menuItem = subMenu.getItem(x);
             
             if (menuItem.isChecked()) {
                 
                 int checkedNum = Integer.valueOf(USAidUtils.getMenuItemCountryConstant(menuItem.getItemId()));
                 
                 if (checkedNum > 0) {
                     checkedCountryVector.add(checkedNum);
                 }
                 
             }
             
         } // end looking for checked menu items
         
         // size of checked menu items
         int numberRegionChecked = checkedRegionVector.size();
         
         Log.d(LOG_TAG, "---------------------------------------- numberRegionChecked: " + numberRegionChecked);
         
         int numberCountryChecked = checkedCountryVector.size();
         
         Log.d(LOG_TAG, "---------------------------------------- numberCountryChecked: " + numberCountryChecked);
         
         ArrayList<USAidDataObject> newData = null;
         
         if ((numberRegionChecked == 0) && (numberCountryChecked == 0)) {
             
             // display all
             newData = value;
             
         } else {
                 
             // only display filtered items
             newData = new ArrayList<USAidDataObject>();
             
             int maxValues = value.size();
             
             boolean addedValue = false;
             
             for (int i = 0; i < maxValues; i++) {
                 
                 // reset added item
                 addedValue = false;
                 
                 // check each one of these against region checked vector
                 for (int j = 0; j < numberRegionChecked; j++) {
                 
                     // is this defined in the region
                     if (value.get(i).regionValue == checkedRegionVector.get(j).intValue()) {
                         newData.add(value.get(i));
                         addedValue = true;
                         break;
                     }
                     
                 } // end checking regions
                 
                 // check against the country vector if not added by region
                 if (!addedValue) {
                     
                     for (int k = 0; k < numberCountryChecked; k++) {
                         
                         // is this defined for the country
                         if (value.get(i).countryCode == checkedCountryVector.get(k).intValue()) {
                             newData.add(value.get(i));
                             break;
                         }
                         
                     }
                     
                 } // end checking countries
                 
             } // end maxValues
         
         }
         
         Log.d(LOG_TAG, "---------------------------------------- final number reports: " + newData.size());
         
         
         setTheListData(newData, false, false);
         
     } // end displayRegions
     
     /**
      * This is the array adapter class used for our custom view.
      * 
      * @author spotell at t-sciences.com
      */
     private class USAidListAdapter extends ArrayAdapter<USAidDataObject> {
         
         private ArrayList<USAidDataObject> items;
         
         private LayoutInflater inflater;
 
         public USAidListAdapter(Context context, int textViewResourceId, ArrayList<USAidDataObject> objects) {
             super(context, textViewResourceId, objects);
             
             items = objects;
             
             inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             
             View currentView = convertView;
             
             // if the view has not been created create it
             if (currentView == null) {
                 
                 currentView = inflater.inflate(R.layout.usaid_list_layout, null);
                 
                 // create the tag we are using
                 currentView.setTag(new USAidViewHolder());
                 
             }
             
             USAidViewHolder usaidViewHolder = (USAidViewHolder) currentView.getTag();
             
             // load the holder if empty
             if (usaidViewHolder.publishDateView == null) {
                 
                 usaidViewHolder.publishDateView = (TextView) currentView.findViewById(R.id.usaid_publish_date);
                 usaidViewHolder.imageView = (ImageView) currentView.findViewById(R.id.usaid_data_image_type);
                 usaidViewHolder.titleView = (TextView) currentView.findViewById(R.id.usaid_title);
                 usaidViewHolder.descriptionView = (TextView) currentView.findViewById(R.id.usaid_description);
                 
             }
             
             // the jagwireDataObject object we are working with
             usaidViewHolder.usaidDataObject = items.get(position);
             
             // show the date published
             usaidViewHolder.publishDateView.setText(usaidViewHolder.usaidDataObject.publishedString);
             
             // show the type image
             usaidViewHolder.imageView.setImageDrawable(getActivity().getResources().getDrawable(USAidUtils.getImageId(usaidViewHolder.usaidDataObject.sectorValue)));
             
             // show the title
             usaidViewHolder.titleView.setText(usaidViewHolder.usaidDataObject.title);
             
             // show the description
             if (usaidViewHolder.usaidDataObject.abstractString.length() == 0) {
                 usaidViewHolder.descriptionView.setText(getActivity().getString(R.string.usaid_description_value_none));
             } else {
                 usaidViewHolder.descriptionView.setText(usaidViewHolder.usaidDataObject.abstractString);
             }
             
             return currentView;
             
         }
         
     } // end USAidListAdapter
     
     /**
      * Static class for view holder pattern.
      * 
      * @author spotell at t-sciences.com
      *
      */
     static class USAidViewHolder {
         
         TextView publishDateView;
         ImageView imageView;
         TextView titleView;
         TextView descriptionView;
         
         USAidDataObject usaidDataObject;
         
     } // end SearchResultsViewHolder
 
 } // end USAidMainFragment
