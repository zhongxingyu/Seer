 /*
  * Copyright (C) 2013 Jan Renz
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
 
 package de.janrenz.app.mediathek;
 
 
 import android.app.SearchManager;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.app.NavUtils;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.view.Menu;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.app.ActionBar;
 import android.view.MenuItem;
 import android.widget.LinearLayout;
 import android.support.v7.widget.SearchView;
 import java.util.ArrayList;
 import org.holoeverywhere.widget.TextView;
 
 
 
 public class SearchActivity extends org.holoeverywhere.app.Activity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
 
     private SearchView searchView = null;
 
     Menu mMenu = null;
     String mQuery = "";
     GridView mGridView = null;
     // The list adapter for the list we are displaying
     SimpleCursorAdapter mListAdapter;
     private Cursor myCursor;
 
     // The listener we are to notify when a headline is selected
 
     private ArrayList<Movie> mAllItems = new ArrayList<Movie>();
 
     @Override
     public void onStart(){
         super.onStart();
     }
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.searchresults);
 
         mGridView = (GridView) this.findViewById(R.id.searchResultGrid);
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         //now lets decide the layout, note wa also use a different item in the cursor adapter
         int layoutId =   R.layout.headline_item;
         if (getResources().getBoolean(R.bool.has_two_panes)) {
             mGridView.setNumColumns(3);
             layoutId = R.layout.headline_item_grid;
         }
         mListAdapter = new RemoteImageCursorAdapter(
                 this,
                 layoutId, null,
                 new String[] { "title", "image" }, new int[] {
                 R.id.text_view,
                 R.id.thumbnail });
         mGridView.setAdapter(mListAdapter);
         mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
                 myCursor.moveToPosition(position);
                 String cExtId = myCursor.getString(myCursor.getColumnIndexOrThrow("extId"));
                 if (getResources().getBoolean(R.bool.has_two_panes)) {
                     // display it on the article fragment
                     try {
                         //BusProvider.getInstance().post(new MovieSelectedEvent(position, cExtId, getArguments().getInt("dateint", 0), mAllItems));
                     } catch (Exception e) {
                         // TODO: handle exception
                     }
                     //this.getListView().setSelection(position);
 
                 } else {
                 }
 
                 // use separate activity
                 Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                 i.putExtra("pos", position);
                 i.putExtra("movies", mAllItems);
                 i.putExtra("title", "Suchergebnisse");
                 startActivity(i);
             }
         });
         restoreMe(savedInstanceState);
     }
 
     private void restoreMe(Bundle state) {
         if (state!=null) {
             mQuery = state.getString("query");
             if (mQuery != null) {
               this.doSearch(mQuery);
             }
         }
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.search_actions, menu);
         MenuItem searchItem = menu.findItem(R.id.action_search_in_search);
         SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
         searchView.setQueryHint("Suche in Mediathek…");
         searchView.setOnQueryTextListener(this);
         mMenu = menu;
         handleIntent(getIntent());
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpTo(this, new Intent(this, MediathekActivity.class));
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 
     private void handleIntent(Intent intent) {
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             String query = mQuery = intent.getStringExtra(SearchManager.QUERY);
             doSearch(query);
         }
     }
 
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         if ( mQuery != null ) {
             outState.putString("query", mQuery.toString());
         }
     }
 
     private void triggerLoad(String query, Boolean forceReload ){
         Bundle args = new Bundle();
         args.putString("query", query);
         mQuery = query;
 
         int loaderId = 300;
         //different loader id per day by using the timestamp of the firstmobve!
         if (forceReload){
            getSupportLoaderManager().restartLoader(loaderId, args, this);
         }else{
            getSupportLoaderManager().initLoader(loaderId, args, this);
         }
     }
 
     private void doSearch(String queryStr) {
         try {
             this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
             this.findViewById(R.id.noresultsmsg).setVisibility(View.GONE);
             mGridView.setVisibility(View.GONE);
         }catch(Exception e){}
         triggerLoad(queryStr, true);
         InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mGridView.getApplicationWindowToken(), 0);
         MenuItem searchItem = mMenu.findItem(R.id.action_search_in_search);
         SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
         if (searchView != null && searchView.isIconified()== false) {
             searchView.clearFocus();
             MenuItemCompat.collapseActionView(searchItem);
         }
         getSupportActionBar().setTitle("Suche nach \"" + queryStr + "\"");
     }
 
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         // query code
         Uri queryUri = Uri.parse("content://de.janrenz.app.mediathek.cursorloader.data");
         queryUri = queryUri.buildUpon().appendQueryParameter("method", "search").appendQueryParameter("query", mQuery).build();
         try {
             //setListShown(false);
         } catch (Exception e) {
             Log.e("ERROR_____", e.getMessage());
         }
         return new CursorLoader(
                 this,
                 queryUri,
                 new String[] { "title", "image" , "extId", "startTime", "startTimeAsTimestamp", "isLive"},
                 null,
                 null,
                 null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
         mListAdapter.swapCursor(cursor);
         mAllItems = new ArrayList<Movie>();
 
         myCursor = cursor;
         for(myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
             // The Cursor is now set to the right position
             Movie mMovie = new Movie();
             //this seems to be reverse here
             mMovie.setTitle(myCursor.getString(myCursor.getColumnIndexOrThrow("subtitle")));
             mMovie.setSubtitle(myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
             mMovie.setExtId(myCursor.getString(myCursor.getColumnIndexOrThrow("extId")));
             mMovie.setStarttime(myCursor.getString(myCursor.getColumnIndexOrThrow("startTime")));
             mMovie.setStarttimestamp(myCursor.getInt(myCursor.getColumnIndexOrThrow("startTimeAsTimestamp")));
             mMovie.setIsLive(myCursor.getString(myCursor.getColumnIndexOrThrow("isLive")));
 
             mAllItems.add(mMovie);
         }
         try {
             if (mAllItems.size() > 0){
                 this.findViewById(R.id.noresultsmsg).setVisibility(View.GONE);
                 this.findViewById(R.id.progressBar).setVisibility(View.GONE);
                 mGridView.setVisibility(View.VISIBLE);
             }else{
                 TextView textview = (TextView)this.findViewById(R.id.noresultsmsg);
                 textview.setText ( "Keine Suchergebnisse für "+ mQuery + " gefunden" );
                 if (mQuery != ""){
                     textview.setVisibility(View.VISIBLE);
                 }else{
                     textview.setVisibility(View.GONE);
                 }
                 this.findViewById(R.id.progressBar).setVisibility(View.GONE);
                 mGridView.setVisibility(View.GONE);
             }
         }catch(Exception e){}
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
         if (myCursor != null) {
             mListAdapter.swapCursor(null);
             myCursor = null;
         }
     }
 
     /** if a user enters some text in the search field in the action bar **/
     @Override
     public boolean onQueryTextSubmit(String query) {
         if (!query.equalsIgnoreCase("")){
             mQuery = query;
             doSearch(query);
         }
         return true;
     }
     @Override
     public boolean onQueryTextChange(String query) {
         return true;
     }
 
 }
