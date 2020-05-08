 package com.LoLCompanionApp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.LoLCompanionApp.ChampionCounterpicks.CounterAdapter;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class ChampionCounterpicksDelete extends Activity {
 
 	DatabaseMain databaseMain;
 	DatabaseExtra databaseExtra;
 	String champion;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.champcounterpicksdelete);
 
 		databaseMain = new DatabaseMain(this);
 		databaseExtra = new DatabaseExtra(this);
 
 		// get the name of the chosen champion
 		champion = getIntent().getStringExtra("name");
 
 		createHeader();
 
 		populateLists();
 	}
 
 	private void deleteCounterInformation(View view) {
 
 	}
 
 	public void populateLists() {
 		ListView listCounteredBy = (ListView) findViewById(R.id.listCounters);
 		ListView listCountering = (ListView) findViewById(R.id.listCountering);
 
 		String[][] counteredBy = databaseExtra.getCounteredByChampions(champion);
 		String[][] countering = databaseExtra.getCounteringChampions(champion);
 
 		if (countering != null) {
 			listCountering.setAdapter( new CounterListAdapter(getHashmap(countering, "countering")));
 		} else {
 			// header.append("\n\nNo information in the database.");
 		}
 		
 		if (counteredBy != null) {
 			listCounteredBy.setAdapter( new CounterListAdapter(getHashmap(counteredBy, "countered")));
 		} else {
 			// header.append("\n\nNo information in the database.");
 		}
 	}
 
 	private ArrayList<HashMap<String, String>> getHashmap(
 			String[][] counterArray, String type) {
 		// create a map list that stores the data for each champ
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map;
 		// add the data to the list
 		for (int i = 0; i < counterArray.length; i += 1) {
 			map = new HashMap<String, String>();
 
 			// depending on type of list, change values for countering and
 			// countered by
 			if (type.equals("countering")) {
 				map.put("champ", counterArray[i][0]);
 				map.put("counter", champion);
 			} else {
 				map.put("champ", champion);
 				map.put("counter", counterArray[i][0]);
 			}
 			map.put("details", "Role: " + counterArray[i][2]
 					+ "\nDescription: " + counterArray[i][1] + "\nTips: "
 					+ counterArray[i][3]);
 			result.add(map);
 		}
 		return result;
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
 
 	class CounterListAdapter extends SimpleAdapter {
 
 		CounterListAdapter(ArrayList<HashMap<String, String>> hashMap) {
 			// pass all parameters to the ArayAdapter
 			super(getBaseContext(), hashMap, R.layout.quickchampcounterlist,
					new String[] { "name", "counter", "details" }, new int[] {
 							R.id.deleteCounteredChamp, R.id.deleteCounterChamp,
 							R.id.deleteDetails });
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = super.getView(position, convertView, parent);
 
 			ImageView iconCounter = (ImageView) row
 					.findViewById(R.id.imageCounterChamp);
 			ImageView iconCountered = (ImageView) row
 					.findViewById(R.id.imageCounteredChamp);
 
 			TextView counterChamp = (TextView) row
 					.findViewById(R.id.deleteCounterChamp);
 			TextView counteredChamp = (TextView) row
 					.findViewById(R.id.deleteCounteredChamp);
 
 			// get the image path based on the name of the variable being put on
 			// the screen
 			int pathCounter = getResources().getIdentifier(
 					databaseMain.removeSpecialChars(counterChamp.getText()
							.toString()) + "_square_0", "drawable",
 					"com.LoLCompanionApp");
 
 			int pathCountered = getResources().getIdentifier(
 					databaseMain.removeSpecialChars(counteredChamp.getText()
							.toString()) + "_square_0", "drawable",
 					"com.LoLCompanionApp");
 
 			// if a picture was found
 			if (pathCounter != 0) {
 				// set the image
 				iconCounter.setImageResource(pathCounter);
 			}
 			if (pathCountered != 0) {
 				// set the image
 				iconCountered.setImageResource(pathCountered);
 			}
 
 			return (row);
 		}
 	}
 }
