 package org.jonblack.bluetrack.activities;
 
import org.jonblack.bluetrack.R;
 import org.jonblack.bluetrack.storage.DeviceTable;
 
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleCursorAdapter;
 
 public class DevicesFragment extends ListFragment
                              implements LoaderManager.LoaderCallbacks<Cursor>
 {
   /**
    * SimpleCursorAdapter used by the list view to get data.
    */
   private SimpleCursorAdapter mAdapter;
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState)
   {
    super.onCreateView(inflater, container, savedInstanceState);
    
    // Create an return the custom ListView layout which also displays an
    // 'empty list' message.
    return inflater.inflate(R.layout.devices_list, null);
   }
   
   @Override
   public void onDestroyView()
   {
     super.onDestroyView();
   }
   
   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
     super.onActivityCreated(savedInstanceState);
     
     // Configure the ListView adapter, which will connect to the database.
     mAdapter= new SimpleCursorAdapter(getActivity(),
                                       android.R.layout.two_line_list_item,
                                       null,
                                       new String[] {"name",
                                                     "mac_address"},
                                       new int[] {android.R.id.text1,
                                                  android.R.id.text2}, 0);
     setListAdapter(mAdapter);
     
     // Prepare the loader. Either re-connect with an existing one, or start a
     // new one.
     getLoaderManager().initLoader(0, null, this);
   }
   
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args)
   {
     // Now create and return a CursorLoader that will take care of
     // creating a Cursor for the data being displayed.
     return new CursorLoader(getActivity(), DeviceTable.CONTENT_URI, null, null,
                             null, null);
   }
 
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data)
   {
     // Swap the new cursor in. (The framework will take care of closing the
     // old cursor once we return.)
     mAdapter.swapCursor(data);
   }
 
   @Override
   public void onLoaderReset(Loader<Cursor> loader)
   {
     // This is called when the last Cursor provided to onLoadFinished()
     // above is about to be closed.  We need to make sure we are no
     // longer using it.
     mAdapter.swapCursor(null);
   }
 }
