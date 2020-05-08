 //*****************************************************************************
 // This file is part of bluetrack.
 //
 // bluetrack is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // bluetrack is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with bluetrack.  If not, see <http://www.gnu.org/licenses/>.
 //*****************************************************************************
 
 package org.jonblack.bluetrack.activities;
 
 import org.jonblack.bluetrack.adapters.SessionCursorAdapter;
 import org.jonblack.bluetrack.storage.SessionTable;
 
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class SessionFragment extends ListFragment
                              implements LoaderManager.LoaderCallbacks<Cursor>
 {
   /**
    * SimpleCursorAdapter used by the list view to get data.
    */
   private SessionCursorAdapter mAdapter;
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState)
   {
    return super.onCreateView(inflater, container, savedInstanceState);
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
     mAdapter = new SessionCursorAdapter(getActivity(), null, 0);
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
     return new CursorLoader(getActivity(), SessionTable.CONTENT_URI, null, null,
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
