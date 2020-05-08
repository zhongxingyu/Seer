 package com.parworks.mars.view.search;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.parworks.androidlibrary.ar.ARErrorListener;
 import com.parworks.androidlibrary.ar.ARListener;
 import com.parworks.mars.MarsMenuFragment;
 import com.parworks.mars.R;
 import com.parworks.mars.model.sync.SyncHandler;
 import com.parworks.mars.utils.JsonMapper;
 import com.parworks.mars.utils.User;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 @SuppressLint("ValidFragment")
 public class SearchFragment extends MarsMenuFragment {
 
 	private static final String TAG = "SearchFragment";
 	
 	private SlidingFragmentActivity mContext;
 	
 	private AutoCompleteTextView autoCompleteView;
 	private Fragment popularSearchFragment;
 	private Fragment searchResultFragment;
 
 	private List<String> allTags;
 
 	public SearchFragment() {
 		super();
 	}
 
 	public SearchFragment(SlidingFragmentActivity context) {
 		super();
 		mContext = context;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// retrieve the tags for search
 		SharedPreferences myPrefs = this.getActivity().getSharedPreferences("MARSTAGS", 0);
 		String allTags = myPrefs.getString("allTags", "[]");
 		try {
 			Log.d(TAG, "Loading all tags: " + allTags);
 			this.allTags = JsonMapper.get().readValue(allTags, new TypeReference<List<String>>(){});
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to parse the tags", e);
 		}
 	}
 		
 	@Override
 	public void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		ImageButton button = (ImageButton) mContext.getSupportActionBar().getCustomView().findViewById(R.id.rightBarButton);
 		button.setBackgroundDrawable(null);
 		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_search, null);	
 
 		// config search text box
 		autoCompleteView = (AutoCompleteTextView) v.findViewById(R.id.searchText);
 		// config width/height
 		@SuppressWarnings("deprecation")
 		int w = this.getActivity().getWindowManager().getDefaultDisplay().getWidth();		
 		autoCompleteView.getLayoutParams().height = w * 92 / 640;
 		// fill all the tags to assist input
 		autoCompleteView.setAdapter(new ArrayAdapter<String>(
 				this.getActivity(), R.layout.popular_searches_list_row, allTags));
 		// handle click action
 		autoCompleteView.setOnItemClickListener(new OnItemClickListener() {              
 		    public void onItemClick (AdapterView<?> parentView, View selectedItemView, int position, long id) {
 		    	triggerSearch(((TextView)selectedItemView).getText().toString());
 		    }
 		});
 		
 		// reset cursor whenever being clicked
 		autoCompleteView.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				autoCompleteView.setCursorVisible(true);
 			}
 		});		
 		autoCompleteView.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				autoCompleteView.setCursorVisible(true);
 				return false;
 			}
 		});
 		
 		// handle key search action
 		autoCompleteView.setOnKeyListener(new OnKeyListener() {
             @Override
            public boolean onKey(View arg0, int arg1, KeyEvent event) {
            	if (event.getAction() == KeyEvent.ACTION_UP) {
             		triggerSearch(autoCompleteView.getText().toString());
             		return true;
             	}
                 return false;
             }
         });
 
 		// hide search result view
 		popularSearchFragment = new PopularSearchFragment(this);
 		switchToSearchResult(false);
 		return v;
 	}
 
 	public void triggerSearchFromSuggestion(String searchWord) {
 		// update text view
 		autoCompleteView.setText(searchWord);
 		autoCompleteView.setSelection(searchWord.length());		
 		triggerSearch(searchWord);
 	}
 	
 	private void triggerSearch(String searchWord) {
 		Log.d(TAG, "Start searching: " + searchWord);
 		// hide keyboard
 		hideKeyBoard();
         
 		// search 
 		User.getARSites().searchSitesByTag(searchWord, new ARListener<List<String>>() {			
 			@Override
 			public void handleResponse(List<String> resp) {
 				// start sync all site info at the background
 				SyncHandler.syncListSiteInfo(resp);
 				// show result 
 				displaySearchResult(resp);
 			}
 		}, new ARErrorListener() {			
 			@Override
 			public void handleError(Exception e) {
 				Log.e(TAG, "Failed to search the result", e);
 			}
 		});
 	}
 	
 	private void displaySearchResult(List<String> siteIds) {
 		Log.d(TAG, "Search found the following sites: " + siteIds);
 		if (siteIds != null && siteIds.size() > 0) {
 			autoCompleteView.setCursorVisible(false);
 			searchResultFragment = new SearchResultFragment(siteIds);				
 			// show the result view fragment
 			switchToSearchResult(true);
 		} else {
 			Toast.makeText(mContext, "No results found.", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private void hideKeyBoard() {
 		// close key board
 		InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(autoCompleteView.getApplicationWindowToken(),
         InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		hideKeyBoard();
 	}
 
 	/**
 	 * Change the Fragment component in the main activity 
 	 */
 	public void switchToSearchResult(boolean showResult) {		
 		if (showResult) {
 			mContext.getSupportFragmentManager()
 					.beginTransaction()
 					.replace(R.id.search_content_frame, searchResultFragment)
 					.commit();
 		} else {
 			mContext.getSupportFragmentManager()
 					.beginTransaction()
 					.replace(R.id.search_content_frame, popularSearchFragment)
 					.commit();	
 		}
 	}	
 }
