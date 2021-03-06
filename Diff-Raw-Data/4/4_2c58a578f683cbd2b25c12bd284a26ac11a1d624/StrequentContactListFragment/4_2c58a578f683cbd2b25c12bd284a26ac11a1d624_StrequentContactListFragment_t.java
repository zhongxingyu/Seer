 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.android.contacts.list;
 
 import com.android.contacts.R;
 import com.android.contacts.StrequentMetaDataLoader;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.LoaderManager;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 /**
  * Fragment containing a list of starred contacts followed by a list of frequently contacted.
  */
 public class StrequentContactListFragment extends Fragment {
 
     public interface Listener {
         public void onContactSelected(Uri contactUri);
     }
 
     private static int LOADER_STREQUENT = 1;
     private static final int NUM_COLS = 2;
 
     private Listener mListener;
     private ContactTileAdapter mAdapter;
     private ListView mListView;
     private Context mContext;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         mAdapter = new ContactTileAdapter(activity, mAdapterListener, NUM_COLS);
         mContext = activity;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.strequent_fragment, container, false);
         mListView = (ListView) v.findViewById(R.id.strequent_list);
         mListView.setItemsCanFocus(true);
         return v;
     }
 
     public void setListener(Listener listener) {
         mListener = listener;
     }
 
     @Override
     public void onStart(){
         super.onStart();
        // Commenting this out temporarily to fix a crash on load
        // TODO: Bring this back
//        getLoaderManager().restartLoader(LOADER_STREQUENT, null, mStrequentLoaderListener);
     }
 
     /**
      * The listener for the strequent meta data loader.
      */
     private final LoaderManager.LoaderCallbacks<Cursor> mStrequentLoaderListener =
             new LoaderCallbacks<Cursor>() {
 
         @Override
         public CursorLoader onCreateLoader(int id, Bundle args) {
             return new StrequentMetaDataLoader(mContext);
         }
 
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
             mAdapter.setCursor(data);
             mListView.setAdapter(mAdapter);
         }
 
         @Override
         public void onLoaderReset(Loader<Cursor> loader) {
             mAdapter.setCursor(null);
         }
     };
 
     private ContactTileAdapter.Listener mAdapterListener =
             new ContactTileAdapter.Listener() {
         @Override
         public void onContactSelected(Uri contactUri) {
             if (mListener != null) {
                 mListener.onContactSelected(contactUri);
             }
         }
     };
 }
