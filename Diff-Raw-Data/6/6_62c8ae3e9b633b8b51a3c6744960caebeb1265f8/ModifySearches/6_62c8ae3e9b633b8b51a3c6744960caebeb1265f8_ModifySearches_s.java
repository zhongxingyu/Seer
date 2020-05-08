 package org.lvlv.supersearch;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ModifySearches extends Activity implements OnClickListener,OnKeyListener{
 	private Button add_search_button;
 	private EditText name_text;
 	private EditText url_text;
 	private EditText term_text;
 	private SearchesData searches;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.modifysearches);
 		
 		searches = new SearchesData(this);
 		
 		add_search_button = (Button) findViewById(R.id.add_search_button);
 		name_text = (EditText) findViewById(R.id.name_input);
 		url_text = (EditText) findViewById(R.id.location_input);
 		term_text = (EditText) findViewById(R.id.term_input);
 		
 		add_search_button.setOnClickListener(this);
 		name_text.setOnKeyListener(this);
 		url_text.setOnKeyListener(this);
 		term_text.setOnKeyListener(this);
 		
 	}
 
 
 	public void addSearch() {
 		searches.addSearch(
				name_text.toString(),
				url_text.toString(),
				term_text.toString()
 				);
 	
 	}
 	
 	public void onClick(View v) {
 		addSearch();
 	}
 
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_ENTER) {
 			switch(v.getId()) {
 			case R.id.name_input:
 				break;
 			case R.id.location_input:
 				break;
 			case R.id.term_input:
 				addSearch();
 				
 				break;
 			}
 		}
 		return false;
 	}
 	
 	
 
 }
