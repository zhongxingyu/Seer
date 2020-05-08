 package com.LoLCompanionApp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ChampionCounterpicks extends Activity {
 
 	DatabaseMain databaseMain;
 	DatabaseExtra databaseExtra;
 	String champion;
 	SharedPreferences prefs;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.champcounterpicks);
 
 		databaseMain = new DatabaseMain(this);
 		databaseExtra = new DatabaseExtra(this);
 
 		// get the name of the chosen champion
 		champion = getIntent().getStringExtra("name");
 
 		createHeader();
 		createButtons();
 
 		// get the perferences
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// get the page preferences
 		String viewCounter = prefs.getString("ViewCounter", "Counters");
 
 		// change the text for the page
 		TextView header = (TextView) findViewById(R.id.textCounters);
 
 		// create the list of counters for the page
 		ListView listCounter = (ListView) findViewById(R.id.listCounters);
 		String[][] counter;
 
 		// find the counters in he database
 		if (viewCounter.equals("Counters")) {
 			counter = databaseExtra.getCounteringChampions(champion);
 		} else {
 			counter = databaseExtra.getCounteredByChampions(champion);
 		}
 
 		// if its not null, display the counters
 		if (counter != null) {
 			header.setPadding(0, 0, 0, 0);
 			header.setGravity(Gravity.LEFT);
 			header.setBackgroundResource(0);
 			header.setTextColor(Color.WHITE);
 			header.setText(champion + " " + viewCounter);
 
			if (!viewCounter.equals("Counters")) {
				header.setText(champion + " is " + viewCounter);
			}

 			listCounter.setAdapter(new CounterAdapter(counter,
 					getHashmap(counter)));
 		} else {
 			header.append("\nNo information in the database.");
 			header.setPadding(20, 20, 20, 20);
 			header.setGravity(Gravity.CENTER);
 			header.setBackgroundResource(R.drawable.bgskills);
 			header.setTextColor(Color.BLACK);
 
 			listCounter.setAdapter(null);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		databaseExtra.close();
 	}
 
 	private ArrayList<HashMap<String, String>> getHashmap(
 			String[][] counterArray) {
 		// create a map list that stores the data for each champ
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map;
 		// add the data to the list
 		for (int i = 0; i < counterArray.length; i += 1) {
 			map = new HashMap<String, String>();
 			map.put("name", counterArray[i][0]);
 			map.put("text", counterArray[i][1]);
 			map.put("role", "Role: " + counterArray[i][2]);
 			map.put("tips", counterArray[i][3]);
 			result.add(map);
 		}
 		return result;
 	}
 
 	public void editCounters(View view) {
 		// go to next page on button pressed
 		Intent editPage = new Intent();
 		editPage.setClassName("com.LoLCompanionApp",
 				"com.LoLCompanionApp.ChampionCounterpicksEditMenu");
 		editPage.putExtra("name", champion);
 		startActivity(editPage);
 	}
 
 	private void createButtons() {
 		// Creates Listview
 		GridView gv = (GridView) findViewById(R.id.gridCounterMenu);
 
 		// Creates adapter
 		gv.setAdapter(new ArrayAdapter<String>(this, R.layout.optionlist,
 				new String[] { "Counters", "Countered By" }));
 
 		gv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// get button choice
 				String choice = (String) ((TextView) view).getText();
 
 				// if the choice is already selected do not restart the activity
 				if (!choice.equals(prefs.getString("ViewCounter", "Counters"))) {
 					Editor editor = prefs.edit();
 					editor.putString("ViewCounter", choice);
 					editor.commit();
 
 					// restart screen with new view type
 					finish();
 					startActivity(getIntent());
 				}
 			}
 		});
 	}
 
 	class CounterAdapter extends SimpleAdapter {
 
 		String champions[][];
 
 		CounterAdapter(String[][] champs,
 				ArrayList<HashMap<String, String>> hashMap) {
 			// pass all parameters to the ArayAdapter
 			super(getBaseContext(), hashMap, R.layout.counterpickslayout,
 					new String[] { "name", "text", "role", "tips" }, new int[] {
 							R.id.counterChamp, R.id.counterDescription,
 							R.id.counterRole, R.id.counterTips });
 
 			this.champions = champs;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = super.getView(position, convertView, parent);
 			ImageView icon = (ImageView) row.findViewById(R.id.counterChampPic);
 			TextView textName = (TextView) row.findViewById(R.id.counterChamp);
 
 			String champion = textName.getText().toString();
 
 			// convert name to a usable format for finding pictures
 			String champImg = champion.toLowerCase();
 			champImg = databaseMain.removeSpecialChars(champImg);
 
 			// get the image path based on the name of the variable being put on
 			// the screen
 			int path = getResources().getIdentifier(champImg + "_square_0",
 					"drawable", "com.LoLCompanionApp");
 
 			// if a picture was found
 			if (path != 0) {
 				// set the image
 				icon.setImageResource(path);
 			}
 			return (row);
 		}
 	}
 
 	private void createHeader() {
 		// Creates header
 		TextView champName = (TextView) findViewById(R.id.champName);
 		TextView champTitle = (TextView) findViewById(R.id.champTitle);
 		ImageView champImage = (ImageView) findViewById(R.id.champPicture);
 		champName.setText(champion);
 		String champPic = databaseMain.removeSpecialChars(champion);
 		int path = getResources().getIdentifier(
 				champPic.toLowerCase() + "_square_0", "drawable",
 				"com.LoLCompanionApp");
 
 		champTitle.setText(databaseMain.getChampionTitle(champion));
 		champImage.setImageResource(path);
 	}
 
 	public void back(View view) {
 		finish();
 	}
 
 }
