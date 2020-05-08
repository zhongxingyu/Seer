 package com.nextgen.bemore;
 
     /**
      * This is the "top-level" fragment, showing a list of items that the
      * user can pick.  Upon picking an item, it takes care of displaying the
      * data to the user as appropriate based on the currrent UI layout.
      */
 import java.io.IOException;
 import java.net.URI;
 
 
 import com.nextgen.database.DataBaseHelper;
 import com.nextgen.viewpager.TestFragment;
 
 import android.support.v4.app.*;
 import android.support.v4.content.CursorLoader;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
     public class EventListFragment extends ListFragment {
         boolean mDualPane;
         int mCurCheckPosition = 0;
         long mCurId = 0;
         
         int mShownCheckPosition = -1;
         private DataBaseHelper myDbHelper;         
 
         public static EventListFragment newInstance(String content) {
             EventListFragment fragment = new EventListFragment();
             
             return fragment;
         }
         
         @Override
         public void onActivityCreated(Bundle savedInstanceState) {
             super.onActivityCreated(savedInstanceState);
 
             //Open Database
             openDatabase();
             
             // Populate list with data from the db.
             fillData();
 
             // Check to see if we have a frame in which to embed the details
             // fragment directly in the containing UI.
             View detailsFrame = getActivity().findViewById(R.id.details_fragment);
             View recommendedFrame = getActivity().findViewById(R.id.recommended);
             View buyFrame = getActivity().findViewById(R.id.buy_fragment);
             mDualPane = buyFrame != null && recommendedFrame != null && detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
 
             if (savedInstanceState != null) {
                 // Restore last state for checked position.
                 mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
                 mCurId = savedInstanceState.getLong("curId", 1);
                 mShownCheckPosition = savedInstanceState.getInt("shownChoice", -1);
             }
 
             if (mDualPane) {
                 // In dual-pane mode, the list view highlights the selected item.
                 getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                 // Make sure our UI is in the correct state.
                 showDetails(mCurCheckPosition, mCurId);
                 
                 //TODO: Implement for portrait mode as well...
             }
       
         }
 
         @Override
         public void onSaveInstanceState(Bundle outState) {
             super.onSaveInstanceState(outState);
             outState.putInt("curChoice", mCurCheckPosition);
             outState.putLong("curId", mCurId);
             outState.putInt("shownChoice", mShownCheckPosition);
         }
 
         @Override
         public void onListItemClick(ListView l, View v, int position, long id) {
             showDetails(position, id);   
         }
         
 //        @Override
 //        protected void onListItemClick(ListView l, View v, int position, long id) {
 //            super.onListItemClick(l, v, position, id);
 //            Intent i = new Intent(this, MatchDisplay.class);
 //            i.putExtra(MatchDbAdapter.KEY_ROWID, id);
 //            startActivityForResult(i, ACTIVITY_EDIT);
 //        }
 
         /**
          * Helper function to show the details of a selected item, either by
          * displaying a fragment in-place in the current UI, or starting a
          * whole new activity in which it is displayed.
          */
         void showDetails(int index, long id) {
             mCurCheckPosition = index;
             mCurId = id;
             
             if (mDualPane) {
                 // We can display everything in-place with fragments, so update
                 // the list to highlight the selected item and show the data.
                 getListView().setItemChecked(index, true);
 
                 if (mShownCheckPosition != mCurCheckPosition) {
                     // If we are not currently showing a fragment for the new
                     // position, we need to create and install a new one.
                     EventDetailsFragment df = EventDetailsFragment.newInstance(id);
 
                     // Execute a transaction, replacing any existing fragment
                     // with this one inside the frame.
                     FragmentTransaction ft = getFragmentManager().beginTransaction();
                     ft.replace(R.id.details_fragment, df);
                     ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 
                     EventRecommendationFragment rf = EventRecommendationFragment.newInstance(id);
                     ft.replace(R.id.recommended, rf);
                     ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 
                     BuyRecommendationFragment bf = BuyRecommendationFragment.newInstance(id);
                     ft.replace(R.id.buy_fragment, bf);
                     ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
               
                     mShownCheckPosition = index;                  
                     ft.commit(); 
                 }
 
             } else {
                 // Otherwise we need to launch a new activity to display
                 // the dialog fragment with selected text.
                 Intent intent = new Intent();
                 intent.setClass(getActivity(), EventDetailsActivity.class);
                 intent.putExtra("index", index);
                 startActivity(intent);
             }
         }
         
         private void fillData() {
             Cursor eventsCursor = myDbHelper.fetchAllEvents();
 
             // Create an array to specify the fields we want to display in the list (only TITLE)
             
             String[] from = new String[]{DataBaseHelper.KEY_EVENT_NAME, DataBaseHelper.KEY_DATE, DataBaseHelper.KEY_IMAGE_BANNER};
 
             
             // and an array of the fields we want to bind those fields to
            int[] to = new int[]{R.id.event_name, R.id.date, R.id.event_row_image_banner};
 
             // Now create a simple cursor adapter and set it to display
             MySimpleCursorAdapter events = 
                 new MySimpleCursorAdapter(this.getActivity(),R.layout.event_row, eventsCursor, from, to);
             setListAdapter(events);
         }        
 
         private void openDatabase() {
             myDbHelper = new DataBaseHelper(this.getActivity().getApplicationContext());
             try {
                 myDbHelper.createDataBase();
             } catch (IOException ioe) {
                 throw new Error("Unable to create database");
             }
 
             try {
                 myDbHelper.openDataBase();
             }catch(SQLException sqle){
                 throw sqle;
             }
         }        
     
     }
