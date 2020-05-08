 package com.seawolfsanctuary.tmt;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.app.ExpandableListActivity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DeepDiveListSavedActivity extends ExpandableListActivity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(new DeepDiveListSavedAdapter());
 		registerForContextMenu(getExpandableListView());
 	}
 
 	class DeepDiveListSavedAdapter extends BaseExpandableListAdapter {
 
 		ArrayList<String> entries = loadSavedEntries(true);
 		ArrayList<String[]> data = parseEntries(entries);
 		ArrayList<String> names = new ArrayList<String>(getNames(data));
 
 		private String[] presentedNames = Helpers
 				.arrayListToArray(getNames(data));
 		private String[][] presentedData = Helpers
 				.multiArrayListToArray(getData(data));
 
 		private ArrayList<String> getNames(ArrayList<String[]> data) {
 			ArrayList<String> names = new ArrayList<String>();
 			for (int i = 0; i < data.size(); i++) {
 				String[] entry = data.get(i);
 				names.add("[" + entry[1] + "/" + entry[2] + "/" + entry[3]
 						+ "]" + "\n" + entry[0] + " -> " + entry[6]);
 			}
 			return names;
 		}
 
 		private ArrayList<ArrayList<String>> getData(ArrayList<String[]> entries) {
 			ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
 
 			for (int i = 0; i < entries.size(); i++) {
 				String[] entry = entries.get(i);
 				ArrayList<String> split = new ArrayList<String>();
 
 				split.add("From: " + entry[0] + "\nOn: " + entry[1] + "/"
 						+ entry[2] + "/" + entry[3] + "\nAt: " + entry[4] + ":"
 						+ entry[5]);
 				split.add("To: " + entry[6] + "\nOn " + entry[7] + "/"
 						+ entry[8] + "/" + entry[9] + "\nAt: " + entry[10]
 						+ ":" + entry[11]);
 
 				data.add(split);
 			}
 
 			return data;
 		}
 
 		public Object getChild(int groupPosition, int childPosition) {
 			return presentedData[groupPosition][childPosition];
 		}
 
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			return presentedData[groupPosition].length;
 		}
 
 		public TextView getGenericView() {
 			// Layout parameters for the ExpandableListView
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.FILL_PARENT, 64);
 
 			TextView textView = new TextView(DeepDiveListSavedActivity.this);
 			textView.setLayoutParams(lp);
 			// Centre the text vertically
 			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 			// Set the text starting position
 			textView.setPadding(36, 0, 0, 0);
 			return textView;
 		}
 
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			TextView textView = getGenericView();
 			textView.setText(getChild(groupPosition, childPosition).toString());
 			return textView;
 		}
 
 		public Object getGroup(int groupPosition) {
 			return presentedNames[groupPosition];
 		}
 
 		public int getGroupCount() {
 			return presentedNames.length;
 		}
 
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			TextView textView = getGenericView();
 			textView.setText(getGroup(groupPosition).toString());
 			return textView;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return true;
 		}
 
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		private ArrayList<String> loadSavedEntries(boolean showToast) {
 			String state = Environment.getExternalStorageState();
 			if (Environment.MEDIA_MOUNTED.equals(state)
 					|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 
 				try {
 					String line = null;
 					ArrayList<String> array = new ArrayList<String>();
 
 					File f = new File(Environment.getExternalStorageDirectory()
 							.toString(), "/tmt.csv");
 					BufferedReader reader = new BufferedReader(
 							new FileReader(f));
 
 					while ((line = reader.readLine()) != null) {
 						String str = new String(line);
 						array.add(str);
 					}
 					reader.close();
 
 					if (showToast) {
 						Toast.makeText(
 								getBaseContext(),
 								"Loaded " + array.size() + " entr"
 										+ (array.size() == 1 ? "y" : "ies")
 										+ " from CSV file.", Toast.LENGTH_SHORT)
 								.show();
 					}
 					return array;
 
 				} catch (Exception e) {
 					Toast.makeText(getBaseContext(),
 							"Error: " + e.getMessage(), Toast.LENGTH_LONG)
 							.show();
 
 					return new ArrayList<String>();
 				}
 
 			} else {
 				return new ArrayList<String>();
 			}
 		}
 
 		private ArrayList<String[]> parseEntries(ArrayList<String> entries) {
 			ArrayList<String[]> data = new ArrayList<String[]>();
 
 			try {
 				for (Iterator<String> i = entries.iterator(); i.hasNext();) {
 					String str = (String) i.next();
					String[] elements = str.split(",");
					String[] entry = new String[elements.length];
 
 					for (int j = 0; j < entry.length; j++) {
						entry[j] = Helpers.trimCSVSpeech(elements[j]);
 					}
 
 					data.add(entry);
 				}
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(),
 						"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
 			}
 
 			return data;
 		}
 
 	}
 
 }
