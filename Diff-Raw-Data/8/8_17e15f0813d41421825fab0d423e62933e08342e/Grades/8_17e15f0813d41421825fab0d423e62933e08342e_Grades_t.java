 package info.vanderkooy.ucheck;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Grades extends Activity {
 	private APIHandler handler;
 	private Preferences prefs;
 	private JSONObject data;
 	private JSONArray subjects;
 	private List<String> studies;
 	private Spinner spinner;
 	private ProgressDialog dialog;
 
 	private Map<String, String> studieLijst = new HashMap<String, String>();
 
 	private int numberOfStudies;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.grades);
 
 		spinner = (Spinner) findViewById(R.id.spinner);
 		handler = new APIHandler(getApplicationContext());
 		prefs = new Preferences(getApplicationContext());
 		studies = new ArrayList<String>();
 		
 		prefs.forceNewGrades();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (prefs.gradesNeedUpdate()) {
 			load();
 		}
 	}
 
 	private void load() {
 		dialog = ProgressDialog.show(Grades.this, "", "Data wordt opgehaald.", true);
 
 		Thread thread = new Thread(new Runnable() {
 			public void run() {
 				Meta.fillStudieLijst(studieLijst);
 				data = handler.getGrades();
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						int success = processData();
 						if (dialog.isShowing()) {
 							dialog.hide();
 							dialog.dismiss();
 						}
						if(success == -1 && !prefs.getKey().equals("")) {
 							Intent loginIntent = new Intent().setClass(Grades.this, Login.class);
 							Grades.this.startActivity(loginIntent);
 						}
 					}
 				});
 			}
 		});
 		thread.start();
 	}
 
 	private int processData() {
 		if (data == null) {
 			Toast toast = Toast
 					.makeText(
 							getApplicationContext(),
 							"Er is iets mis gegaan bij het ophalen van cijferdata. Probeer het later nog een keer.",
 							6);
 			toast.show();
 		}
 		String loginerror = "uninit";
 		try {
 			loginerror = data.getString("error");
 		} catch (JSONException e1) {
 			// No error, so password was correct
 		}
 		if(loginerror.equals("loginerror")) {
 			return -1;			
 		}
 		prefs.setLastGradesUpdate();
 		try {
 			subjects = data.getJSONArray("vakken");
 		} catch (JSONException e) {
 			Toast toast = Toast
 					.makeText(
 							getApplicationContext(),
 							"Er is iets mis gegaan bij het ophalen van cijferdata. Probeer het later nog een keer.",
 							6);
 			toast.show();
 			e.printStackTrace();
 		}
 		numberOfStudies = 0;
 		studies.clear();
 		for (int i = 0; i < subjects.length(); i++) {
 			String vak = "";
 			try {
 				vak = (String) subjects.getJSONObject(i).get("studie");
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if (!studies.contains(vak) && !vak.equals("")) {
 				studies.add(vak);
 				numberOfStudies++;
 			}
 		}
 		if (numberOfStudies > 1) {
 			spinner.setVisibility(0);
 			updateSpinner();
 		} else {
 			spinner.setVisibility(8);
 			makeList("Alle");
 		}
 		return 0;
 	}
 
 	private void updateSpinner() {
 		ArrayList<String> spinnerArray = new ArrayList<String>();
 		spinnerArray.add("Alle cijfers");
 		for (int i = 0; i < studies.size(); i++) {
 			if (studieLijst.get(studies.get(i)) != null)
 				spinnerArray.add(studieLijst.get(studies.get(i)));
 			else
 				spinnerArray.add(studies.get(i));
 		}
 
 		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
 				this, android.R.layout.simple_spinner_item, spinnerArray);
 		spinnerArrayAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(spinnerArrayAdapter);
 		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	}
 
 	private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
 		for (Entry<T, E> entry : map.entrySet()) {
 			if (value.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	public void makeList(String subject) {
 		ListView list = (ListView) findViewById(R.id.list);
 		String studie = "";
 
 		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map;
 		map = new HashMap<String, String>();
 		map.put("subject", "Vak");
 		map.put("grade", "Cijfer");
 		map.put("EC", "EC");
 		mylist.add(map);
 		for (int i = 0; i < subjects.length(); i++) {
 			map = new HashMap<String, String>();
 			try {
 				map.put("subject", (String) subjects.getJSONObject(i)
 						.get("vak"));
 				map.put("grade",
 						(String) subjects.getJSONObject(i).get("cijfer"));
 				map.put("EC", (String) subjects.getJSONObject(i).get("ects"));
 				if (!(Boolean) subjects.getJSONObject(i).get("gehaald"))
 					map.put("gehaald", "false");
 				studie = (String) subjects.getJSONObject(i).get("studie");
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			if (subject.equals("Alle cijfers") || subject.equals(studie)) {
 				mylist.add(map);
 			}
 		}
 		ListAdapter mSchedule = new ListAdapter(this, mylist,
 				R.layout.rowgrades, new String[] { "subject", "grade", "EC" },
 				new int[] { R.id.subject, R.id.grade, R.id.EC });
 		list.setAdapter(mSchedule);
 		list.setSelector(android.R.color.transparent);
 
 	}
 
 	private class MyOnItemSelectedListener implements OnItemSelectedListener {
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			String value = getKeyByValue(studieLijst,
 					parent.getItemAtPosition(pos).toString());
 			String subject = "";
 			if (value != null)
 				subject = value;
 			else
 				subject = parent.getItemAtPosition(pos).toString();
 			makeList(subject);
 		}
 
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do nothing.
 		}
 	}
 }
