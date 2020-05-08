 package com.LoLCompanionApp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ChampionSkills extends Activity {
 
 	public static ArrayList<HashMap<String, String>> champskills;
 	DatabaseHelper database;
 	String champion;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.champskills);
 
 		database = new DatabaseHelper(this);
 
 		// get the name of the chosen champion
 		champion = getIntent().getStringExtra("name");
 
 		createHeader();
 
 		// get all the skills for the champion
 		String[][] skills = database.getAllSkillsByChampName(champion);
 
 		if (skills != null) {
 			// create a map list that stores the data for each champ
 			champskills = new ArrayList<HashMap<String, String>>();
 			HashMap<String, String> map;
 			// add the data to the list (5 = numSkills per champ)
 			for (int i = 0; i < skills.length; i += 1) {
 				map = new HashMap<String, String>();
 				map.put("name", skills[i][0]);
 				map.put("text", skills[i][1]);
 
 				// create stats text
 				String stats = "";
				if (!skills[i][2].equals("null")) {
 					stats += "Type: " + skills[i][2];
 				}
				if (!skills[i][3].equals("null")) {
 					stats += "\nCost: " + skills[i][3].toLowerCase();
 				}
				if (!skills[i][4].equals("null")) {
 					stats += "\nRange: " + skills[i][4];
 				}
				if (!skills[i][5].equals("null")) {
 					stats += "\nCooldown: " + skills[i][5];
 				}
 
 				map.put("stats", stats.toString());
 				champskills.add(map);
 			}
 
 			ListView lv = (ListView) findViewById(R.id.listSkillsView);
 
 			// add custom adapter to display data
 			lv.setAdapter(new ChampSkillAdapter());
 
 		} else {
 			Toast.makeText(this, "Could not retrieve data.", Toast.LENGTH_LONG)
 					.show();
 		}
 
 		database.close();
 	}
 
 	private void createHeader() {
 		// Creates header
 		TextView champName = (TextView) findViewById(R.id.champName);
 		TextView champTitle = (TextView) findViewById(R.id.champTitle);
 		ImageView champImage = (ImageView) findViewById(R.id.champPicture);
 		champName.setText(champion);
 		String champPic = database.removeSpecialChars(champion);
 		int path = getResources().getIdentifier(
 				champPic.toLowerCase() + "_square_0", "drawable",
 				"com.LoLCompanionApp");
 
 		champTitle.setText(database.getChampionTitle(champion));
 		champImage.setImageResource(path);
 	}
 
 	public void back(View view) {
 		finish();
 	}
 
 	class ChampSkillAdapter extends SimpleAdapter {
 		ChampSkillAdapter() {
 			// pass all parameters to the ArayAdapter
 			super(getBaseContext(), ChampionSkills.champskills,
 					R.layout.skilllayout, new String[] { "name", "text",
 							"stats" }, new int[] { R.id.skillName,
 							R.id.skillText, R.id.skillStats });
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = super.getView(position, convertView, parent);
 
 			ImageView icon = (ImageView) row.findViewById(R.id.skillPicture);
 			TextView textName = (TextView) row.findViewById(R.id.skillName);
 
 			// get the name of the skill, remove formatting and any extra spaces
 			// in front or behind
 			String skillImg = database.removeSpecialChars(
					textName.getText().toString().trim().replace(" ", "_")).toLowerCase();
 			// special cases
 			if (skillImg.equals("90_caliber_net")) {
 				skillImg = "ninety_caliber_net";
 			}
 
 			// get the image path based on the name of the variable being put on
 			// the screen
 			int path = getResources().getIdentifier(skillImg, "drawable",
 					"com.LoLCompanionApp");
 
 			// if a picture was found
 			if (path != 0) {
 				// set the image
 				icon.setImageResource(path);
 			}
 
 			return row;
 		}
 	}
 }
