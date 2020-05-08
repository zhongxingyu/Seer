 ﻿/**
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla.fragments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 import dat255.grupp06.bibbla.MainActivity;
 import dat255.grupp06.bibbla.frontend.BookOverlayActivity;
 import dat255.grupp06.bibbla.model.Book;
 
 /**
  * ListFragment that is used to display the search-results after a search.
  * 
  * @author Jonathan Orr�
  *
  */
 public class SearchListFragment extends SherlockListFragment {
 	public final static String BOOK = "dat255.grupp06.bibbla.AUTHOR";
 	private ArrayList<Book> currentBooks;
 	private int currentPage;
 	private String lastSearch;
 	
 	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
         currentBooks = new ArrayList<Book>();
         currentPage = 0;
     }
 
     @Override
     /**
      * When an item is clicked in the list this method is called
      */
     public void onListItemClick(ListView l, View v, int position, long id) {
     	if(position < currentBooks.size()) {
 	    	Intent intent = new Intent(getSherlockActivity(), BookOverlayActivity.class);
 	    	intent.putExtra(BOOK, currentBooks.get(position));
 	    	startActivity(intent);
     	} else {
     		currentPage++;
     		((MainActivity) getActivity()).moreSearchResultsClicked(currentPage+1, lastSearch);
     	}
     }
     
     /**
      * Receives the search-results and swaps the contents in the list with them.
      */
     public void updateList(List<Book> books) {
     	//Adds a book to act as a placeholder for the 
     	//"fetch more search results"-message
     	currentBooks.clear();
     	currentBooks.addAll(books);
     	books.add(new Book());
 
     	ListAdapter adapter = new BookListAdapter(getSherlockActivity(), books, true);
 
     	this.setListAdapter(adapter);
     }
     
     public void appendList(List<Book> books) {
     	int selection = currentBooks.size();
     	currentBooks.addAll(books);
     	currentBooks.add(new Book());
     	ListAdapter adapter = new BookListAdapter(getSherlockActivity(), currentBooks, true);
     	currentBooks.remove(currentBooks.size()-1);
     	this.setListAdapter(adapter);
     	this.setSelection(selection);
     }
     
     public void setLastSearchString(String s) {
     	lastSearch = s;
     }
 }
 
