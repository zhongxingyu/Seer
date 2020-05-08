 package com.LoLCompanionApp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MainOther extends Activity {
 
 	String[] menu = { "General Guides"/*, "Jungle Timer"*/ };
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mainother);
 
 		initializeHeader();
 
 		// create the list
 		// Creates list view
 		ListView lv = (ListView) findViewById(R.id.listOtherMenu);
 		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.optionlist, menu));
 
 		// Allows searchings
 		lv.setTextFilterEnabled(true);
 
 		lv.setOnItemClickListener(menuClickListener);
 	}
 
 	public void initializeHeader() {
 		TextView title = (TextView) findViewById(R.id.HeaderTitle);
 		title.setText("Other Information");
 	}
 
 	OnItemClickListener menuClickListener = new OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 
 			String choice = (String) ((TextView) view).getText();
 
 			String className = null;
 
 			if (choice.equals(menu[0])) {
 				className = "com.LoLCompanionApp.GeneralGuides";
			} else if (choice.equals(menu[1])) {
 				className = "com.LoLCompanionApp.JungleMain";
			}
 
 			// Loads up the page
 			Intent newPage = new Intent();
 			newPage.setClassName("com.LoLCompanionApp", className);
 			startActivity(newPage);
 		}
 	};
 
 	public void back(View view) {
 		finish();
 	}
 }
