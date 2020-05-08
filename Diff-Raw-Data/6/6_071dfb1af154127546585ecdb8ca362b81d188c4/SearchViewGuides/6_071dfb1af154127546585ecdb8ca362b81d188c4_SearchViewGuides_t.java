 //        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
 //        Copyright (C) 2013  Adrián Romero Corchado
 //
 //        This program is free software: you can redistribute it and/or modify
 //        it under the terms of the GNU General Public License as published by
 //        the Free Software Foundation, either version 3 of the License, or
 //        (at your option) any later version.
 //
 //        This program is distributed in the hope that it will be useful,
 //        but WITHOUT ANY WARRANTY; without even the implied warranty of
 //        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //        GNU General Public License for more details.
 //
 //        You should have received a copy of the GNU General Public License
 //        along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.adrguides;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.util.Log;
 import android.view.MenuItem;
 import android.widget.FilterQueryProvider;
 import android.widget.SearchView;
 import android.widget.SimpleCursorAdapter;
 
 import com.adrguides.model.Place;
 
 import java.util.List;
 
 /**
  * Created by adrian on 29/08/13.
  */
 public class SearchViewGuides {
 
     private TTSFragment ttsFragment;
 
     public SearchViewGuides(final Activity activity, final MenuItem menuitem) {
 
         ttsFragment = (TTSFragment) activity.getFragmentManager().findFragmentByTag(TTSFragment.TAG);
 
         final SearchView searchView = (SearchView) menuitem.getActionView();
         // Configure the search info and add any event listeners
 
         menuitem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
             @Override
             public boolean onMenuItemActionExpand(MenuItem menuItem) {
                 String[] from = {"text"};
                 int[] to = {android.R.id.text1};
 
                 SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity.getActionBar().getThemedContext(), android.R.layout.simple_list_item_activated_1, getSuggestionsCursor(null), from, to, 0);
                 adapter.setFilterQueryProvider(new FilterQueryProvider() {
                     public Cursor runQuery(CharSequence constraint) {
                        if (constraint == null) {
                            return null;
                        } else {
                            return getSuggestionsCursor(constraint.toString());
                        }
                     }
                 });
                 searchView.setSuggestionsAdapter(adapter);
                 return true;
             }
 
             @Override
             public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                 return true;
             }
         });
 
 
         searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
             @Override
             public boolean onSuggestionSelect(int i) {
                 return false;
             }
 
             @Override
             public boolean onSuggestionClick(int i) {
                 Cursor row = (Cursor) searchView.getSuggestionsAdapter().getItem(i);
                 searchView.setQuery(row.getString(2), true);
                 return true;
             }
         });
 
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String s) {
 
                 if (s == null) {
                     return false;
                 }
 
                 Log.d("com.adrguides.SearchViewGuides", "man submitao");
                 List<Place> places = ttsFragment.getGuide().getPlaces();
                 for(int i = 0; i < places.size(); i++){
                     Place item = places.get(i);
                     if ((item.getId() != null && item.getId().equals(s)) ||
                             item.getTitle().toLowerCase().equals(s.toLowerCase()) ||
                             item.getVisibleLabel().toLowerCase().equals(s.toLowerCase())) {
 
                         ttsFragment.gotoChapter(i);
                         menuitem.collapseActionView();
                         return true;
                     }
                 }
                 return false ;// true if the query has been handled by the listener, false to let the SearchView perform the default action.
             }
             @Override
             public boolean onQueryTextChange(String s) {
                 return false; // false if the SearchView should perform the default action of showing any suggestions if available, true if the action was handled by the listener.
             }
         });
     }
 
 
     private Cursor getSuggestionsCursor(String filter) {
         Log.d("com.adrguides.SearchViewGuides",  "man filtrao");
 
         String[] columnNames = {"_id", "_title", "text"};
         MatrixCursor cursor = new MatrixCursor(columnNames);
         String[] temp = new String[3];
         int id = 0;
         for(Place item : ttsFragment.getGuide().getPlaces()){
 
             if (filter == null ||
                     (item.getId() != null && item.getId().contains(filter)) ||
                     item.getTitle().toLowerCase().contains(filter.toLowerCase())) {
                 temp[0] = item.getId();
                 temp[1] = item.getTitle();
                 temp[2] = item.getVisibleLabel();
                 cursor.addRow(temp);
             }
         }
 
         return cursor;
     }
 }
