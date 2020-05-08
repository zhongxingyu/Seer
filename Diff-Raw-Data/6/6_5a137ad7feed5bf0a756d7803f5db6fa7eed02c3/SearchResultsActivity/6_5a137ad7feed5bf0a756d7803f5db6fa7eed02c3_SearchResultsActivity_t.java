 package com.josephblough.sbt.activities.results;
 
 import com.josephblough.sbt.R;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public abstract class SearchResultsActivity extends ListActivity implements OnEditorActionListener, TextWatcher {
 
     protected View searchView;
   
     protected abstract boolean isDetailsViewShowing();
     protected abstract void hideDetailsView();
     
     // Hide the details view on BACK key press if it's showing
     @Override
     public void onBackPressed() {
 	if (isDetailsViewShowing()) {
 	    hideDetailsView();
 	}
 	else if (getListView().getTextFilter() != null && !"".equals(getListView().getTextFilter().toString())) {
 	    getListView().clearTextFilter();
 	    ((EditText) findViewById(R.id.input_search_query)).setText("");
 
 	    // Hide the search bar if visible
 	    if (searchView != null && searchView.isShown()) {
 		hideSearch();
 	    }
 	}
	else if (searchView != null && searchView.isShown()) {
	    // Hide the search bar if visible
	    if (searchView != null && searchView.isShown()) {
		hideSearch();
	    }
	}
 	else {
 	    super.onBackPressed();
 	}
     }
     
     // Search handling
     @Override
     public boolean onKeyDown(final int keyCode, final KeyEvent event) {
 	if (keyCode == KeyEvent.KEYCODE_SEARCH) {
 	    if (isDetailsViewShowing()) {
 		hideDetailsView();
 	    }
 
             return onSearchRequested();
         }
 
 	return super.onKeyDown(keyCode, event);
     }
     
     @Override
     public boolean onSearchRequested() {
 	// Display the search field
 	toggleSearch();
         
         // Returning true indicates that we did launch the search, instead of blocking it.
         return true;
     }
 
     private void toggleSearch() {
 	if (searchView == null) {
 	    searchView = findViewById(R.id.search_bar);
 	    EditText input = ((EditText) searchView.findViewById(R.id.input_search_query));
 	    input.setOnEditorActionListener(this);
 	    input.addTextChangedListener(this);
 	    Button searchButton = ((Button) searchView.findViewById(R.id.button_go));
 	    searchButton.setOnClickListener(new View.OnClickListener() {
 	        
 	        public void onClick(View v) {
 	            toggleSearch();
 	        }
 	    });
 	    
 	    searchView.setVisibility(View.VISIBLE);
 
 	    input.requestFocus();
 	    CharSequence filter = getListView().getTextFilter();
 	    input.setText((filter != null) ? filter : "");
 
 	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 	    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 	}
 	else if (searchView.isShown()) {
 	    searchView.setVisibility(View.GONE);
 	    
 	    EditText input = ((EditText) searchView.findViewById(R.id.input_search_query));
 	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 	    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
 	    input.clearFocus();
 	}
 	else {
 	    searchView.setVisibility(View.VISIBLE);
 
 	    EditText input = ((EditText) searchView.findViewById(R.id.input_search_query));
 	    input.requestFocus();
 	    CharSequence filter = getListView().getTextFilter();
 	    input.setText((filter != null) ? filter : "");
 
 	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 	    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 	}
     }
 
     protected void hideSearch() {
 	if (searchView != null) {
 	    searchView.setVisibility(View.GONE);
 
 	    EditText input = ((EditText) searchView.findViewById(R.id.input_search_query));
 	    input.requestFocus();
 	    CharSequence filter = getListView().getTextFilter();
 	    input.setText((filter != null) ? filter : "");
 
 	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 	    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
 	}
     }
 
     
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 	toggleSearch();
 	v.clearFocus();
 
 	return false;
     }
 
     public void afterTextChanged(Editable s) {
 	if ("".equals(s.toString())) {
 	    getListView().clearTextFilter();
 	}
 	else {
 	    getListView().setFilterText(s.toString());
 	}
     }
 
     public void beforeTextChanged(CharSequence s, int start, int count,
 	    int after) {
 	// TODO Auto-generated method stub
     }
 
     public void onTextChanged(CharSequence s, int start, int before, int count) {
 	// TODO Auto-generated method stub
     }
 }
