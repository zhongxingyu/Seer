 /*
     Montréal Just in Case
     Copyright (C) 2011  Mudar Noufal <mn@mudar.ca>
 
     Geographic locations of public safety services. A Montréal Open Data
     project.
 
     This file is part of Montréal Just in Case.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.mudar.mtlaucasou;
 
 import com.google.android.maps.GeoPoint;
 
 import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
 import ca.mudar.mtlaucasou.ui.widgets.PlacemarksCursorAdapter;
 import ca.mudar.mtlaucasou.utils.ActivityHelper;
 import ca.mudar.mtlaucasou.utils.AppHelper;
 import ca.mudar.mtlaucasou.utils.NotifyingAsyncQueryHandler;
 
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.SupportActivity;
 import android.view.View;
 import android.widget.ListView;
 
 public class BaseListFragment extends ListFragment implements
         NotifyingAsyncQueryHandler.AsyncQueryListener {
     protected static final String TAG = "BaseListFragment";
 
     protected ActivityHelper mActivityHelper;
 
     protected static final int QUERY_TOKEN = 0x1;
 
     protected NotifyingAsyncQueryHandler mHandler;
     protected PlacemarksCursorAdapter mAdapter;
     protected Cursor mCursor;
     protected OnPlacemarkSelectedListener mListener;
 
     static final String[] PLACEMARKS_SUMMARY_PROJECTION = new String[] {
             BaseColumns._ID,
             PlacemarkColumns.PLACEMARK_NAME,
             PlacemarkColumns.PLACEMARK_ADDRESS,
             PlacemarkColumns.PLACEMARK_GEO_LAT,
             PlacemarkColumns.PLACEMARK_GEO_LNG
     };
 
     /**
      * Initialized by constructor
      */
     protected int indexSection;
     protected String defaultSort;
 
     /**
      * BaseListActivity Constructor
      * 
      * @param indexSection Used by {@link ActivityHelper} to get the each
      *            section's content
      * @param listActivity The class called by the intent launched by the
      *            actionBar handler
      */
     public BaseListFragment(int indexSection, String defaultSort) {
         this.indexSection = indexSection;
         // this.mapActivity = mapActivity;
         this.defaultSort = defaultSort;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
//        Log.v(TAG, "onCreate");
         super.onCreate(savedInstanceState);
 
         mActivityHelper = ActivityHelper.createInstance(getActivity());
 
         mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
 
         if (mCursor != null) {
             getActivity().stopManagingCursor(mCursor);
             mCursor = null;
         }
 
         setListAdapter(null);
         mHandler.cancelOperation(QUERY_TOKEN);
 
         mCursor = getActivity().getContentResolver().query(
                 mActivityHelper.getContentUri(indexSection),
                 PLACEMARKS_SUMMARY_PROJECTION, null, null, defaultSort);
         getActivity().startManagingCursor(mCursor);
 
         mAdapter = new PlacemarksCursorAdapter(getActivity(),
                 R.layout.fragment_list_item_placemarks,
                 mCursor,
                 new String[] {
                         PlacemarkColumns.PLACEMARK_NAME,
                         PlacemarkColumns.PLACEMARK_ADDRESS,
                         PlacemarkColumns.PLACEMARK_GEO_LAT,
                         PlacemarkColumns.PLACEMARK_GEO_LNG
                 }, new int[] {
                         R.id.placemark_name, R.id.placemark_address
                 }, 0);
         mAdapter.setLocation(((AppHelper) getActivity().getApplicationContext()).getLocation());
 
         setListAdapter(mAdapter);
 
         String select = "";
         mHandler.startQuery(QUERY_TOKEN, null,
                 mActivityHelper.getContentUri(indexSection),
                 PLACEMARKS_SUMMARY_PROJECTION, select, null,
                 PlacemarkColumns.PLACEMARK_NAME);
     }
 
     /**
      * Container Activity must implement this interface to receive the list item
      * clicks.
      */
     public interface OnPlacemarkSelectedListener {
         public void onPlacemarkSelected(GeoPoint geoPoint);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
 
         Cursor cursor = (Cursor) getListAdapter().getItem(position);
 
         double geoLat = cursor.getDouble(cursor
                 .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_GEO_LAT));
         double geoLng = cursor.getDouble(cursor
                 .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_GEO_LNG));
 
//        Log.v(TAG, "onListItemClick. id = " + id + ". Geo = " + geoLat + "," + geoLng);
 
         GeoPoint geoPoint = new GeoPoint((int) (geoLat * 1E6), (int) (geoLng * 1E6));
 
         if (mListener != null) {
             mListener.onPlacemarkSelected(geoPoint);
         }
     }
 
     @Override
     public void onQueryComplete(int token, Object cookie, Cursor cursor) {
         if (this == null) {
             return;
         }
 
        getActivity().stopManagingCursor(mCursor);
         mCursor = cursor;
         getActivity().startManagingCursor(mCursor);
         mAdapter.changeCursor(mCursor);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         getActivity().getContentResolver().registerContentObserver(
                 mActivityHelper.getContentUri(indexSection),
                 true, mTransactionsChangesObserver);
         if (mCursor != null) {
             mCursor.requery();
         }
         getListView().setFastScrollEnabled(true);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         getActivity().getContentResolver().unregisterContentObserver(mTransactionsChangesObserver);
     }
 
     /**
      * Attach a listener
      */
     @Override
     public void onAttach(SupportActivity activity) {
         super.onAttach(activity);
         try {
             // TODO Verify if listener should be released onHide or onPause
             mListener = (OnPlacemarkSelectedListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement OnPlacemarkSelectedListener");
         }
     }
 
     protected ContentObserver mTransactionsChangesObserver = new ContentObserver(new Handler()) {
         @Override
         public void onChange(boolean selfChange) {
             if (mCursor != null) {
                 mAdapter.notifyDataSetChanged();
                 mCursor.requery();
             }
         }
     };
 
 }
