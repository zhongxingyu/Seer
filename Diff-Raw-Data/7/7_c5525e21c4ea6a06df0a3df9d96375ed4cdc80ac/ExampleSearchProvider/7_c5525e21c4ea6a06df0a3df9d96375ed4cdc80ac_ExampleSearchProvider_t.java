 package com.ceejii.search;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 
 import com.ceejii.gui.ExampleSearchSuggestionListener;
 import com.ceejii.gui.component.SearchSuggestionComponent;
 import com.ceejii.gui.component.SearchSuggestionsDisplayer;
 
 public class ExampleSearchProvider implements SearchProvider {
 
 	private String searchString;
 	private SearchSuggestionsDisplayer view;
 	private int resultCount;
 	private List<String> db;
	private static Object lock = new Object();
 
 	public void quickSearchForString(String searchString,
 			SearchSuggestionsDisplayer view, int resultCount) {
 		int searchDelayMilliSec = 400;
		synchronized (lock) {
 			this.searchString = searchString;
 		}
 		this.view = view;
 		this.resultCount = resultCount;
 		delayedSearch(searchDelayMilliSec);
 
 	}
 
 	private void search(String searchString) {
 		synchronized (new Object()) {
 			db = new ArrayList<String>();
 			db.add("Stockholm");
 			db.add("Gteborg");
 			db.add("Malm");
 			db.add("Stockport");
 		}
 		Thread thread = new Thread() {
 
 			@Override
 			public void run() {
 				List<String> results = new ArrayList<String>();
 				String searchString = "";
 				synchronized (new Object()) {
 					searchString = ExampleSearchProvider.this.searchString;
 				}
 				String lowerCase = searchString.toLowerCase();
 				if (!searchString.equals("")) {
 					for (String line : db) {
 						if (line.toLowerCase().startsWith(lowerCase)) {
 							results.add(line);
 						}
 					}
 				}
 				if(results.size() > resultCount) {
 					results = results.subList(0, resultCount);
 				}
 				view.showSearchResults(results, new ExampleSearchSuggestionListener());
 			}
 		};
 		thread.start();
 	}
 
 	private boolean delayedSearch(int searchDelayMilliSec) {
 		Timer timer = new Timer();
 		final String originalSearchString = this.searchString;
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
				synchronized (lock) {
 					if(originalSearchString.equals(ExampleSearchProvider.this.searchString)) {
 							ExampleSearchProvider.this.search(originalSearchString);
 					}
 				}
 			}},	 searchDelayMilliSec);
 		return true;
 	}
 
 	public void fullSearchForString(String searchString,
 			SearchSuggestionsDisplayer view) {
 		System.out.println("Full search (Enter key pressed).");
 	}
 
 }
