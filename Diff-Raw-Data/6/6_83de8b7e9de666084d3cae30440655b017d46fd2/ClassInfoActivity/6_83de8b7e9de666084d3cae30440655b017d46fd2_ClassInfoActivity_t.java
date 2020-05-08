 package com.seawolfsanctuary.tmt;
 
 import java.io.InputStream;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.app.ExpandableListActivity;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ClassInfoActivity extends ExpandableListActivity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(new ClassInfoAdapter());
 		registerForContextMenu(getExpandableListView());
 	}
 
 	class ClassInfoAdapter extends BaseExpandableListAdapter {
 
 		ArrayList<String> entries = loadClassInfo(true);
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
 				names.add(entry[0]);
 			}
 			return names;
 		}
 
 		private ArrayList<ArrayList<String>> getData(ArrayList<String[]> entries) {
 			ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
 
 			for (int i = 0; i < entries.size(); i++) {
 				String[] entry = entries.get(i);
 				ArrayList<String> split = new ArrayList<String>();
 
 				if (entry.length < 5) {
 					String[] new_entry = new String[] { entry[0], entry[1],
 							entry[2], entry[3], "", "" };
 					entry = new_entry;
 				}
 
 				try {
 					entry[1] = NumberFormat.getIntegerInstance().format(
 							Integer.parseInt(entry[1]))
 							+ "mm";
 				} catch (Exception e) {
 					// meh
 				}
 
 				if (entry[4].length() < 1) {
 					entry[4] = "still in service";
 				}
 
 				split.add("Guage: " + entry[1] + "\nEngine: " + entry[2]);
 				split.add("Built: " + entry[3] + "\nRetired: " + entry[4]);
 
 				ArrayList<String> operatorList = parseOperators(entry[5]);
 				String operators = "";
 				for (String operator : operatorList) {
 					operators = operators + operator + ", ";
 				}
 				operators = operators.substring(0, operators.length() - 2);
 				split.add("Operators: " + operators);
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
 
 			TextView textView = new TextView(ClassInfoActivity.this);
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
 
 		private String[] read_csv(String filename) {
 			String[] array = {};
 
 			try {
 				InputStream input;
 				input = getAssets().open(filename);
 				int size = input.available();
 				byte[] buffer = new byte[size];
 
 				input.read(buffer);
 				input.close();
 				array = new String(buffer).split("\n");
 
 				Toast.makeText(
 						getBaseContext(),
 						"Loaded " + array.length + " entr"
 								+ (array.length == 1 ? "y" : "ies")
								+ " from data file.", Toast.LENGTH_SHORT)
						.show();
 
 			} catch (Exception e) {
				String error_msg = "Error reading data file!";
 			}
 
 			return array;
 		}
 
 		private ArrayList<String> loadClassInfo(boolean showToast) {
 
 			try {
 				ArrayList<String> array = new ArrayList<String>();
 
 				String[] classInfo = read_csv("classes.csv");
 				for (String infoLine : classInfo) {
 					array.add(infoLine);
 				}
 
 				return array;
 
 			} catch (Exception e) {
 				Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 						Toast.LENGTH_LONG).show();
 
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
 						System.out.println("  - Found at " + j + ": "
 								+ entry[j]);
 					}
 
 					data.add(entry);
 				}
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 				Toast.makeText(getApplicationContext(),
 						"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
 			}
 
 			return data;
 		}
 
 		private ArrayList<String> parseOperators(String operatorString) {
 			ArrayList<String> operators = new ArrayList<String>();
 			if (operatorString.length() > 0) {
 				String[] pipedOperators = operatorString.split("[|]");
 				for (String operator : pipedOperators) {
 					operators.add(operator);
 				}
 			} else {
 				operators.add("(none)");
 			}
 			return operators;
 		}
 	}
 
 }
