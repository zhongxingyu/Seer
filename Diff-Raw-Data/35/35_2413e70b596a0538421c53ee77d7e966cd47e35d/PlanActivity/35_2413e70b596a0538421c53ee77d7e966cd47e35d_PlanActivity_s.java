 package com.example.p1;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlanActivity extends Activity {
 	private final String TAG = "PlanActivity";
 	int idNumber = 0;
 	TableLayout tl2;
 	String et1;
 	Spinner spinner1, spinner2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.i(TAG, "===== ENTER onCreate =====");
 
 		System.out.println("      Plan Act");
 		setContentView(R.layout.activity_plan);
 
		Utilities.readPlansTasks(getApplicationContext());
		
 		// 
 		
 		spinner2 = (Spinner) findViewById(R.id.planslist);
 		List<String> list = new ArrayList<String>();
 
 		Globals g = Globals.getInstance();
 		int sz = g.plansSize();
 		System.out.println("PlanActivity -- plans sz=" + sz);
 
 		g.sortPlans();
 
 		ArrayList<Plan> planz = g.getPlansArray();
 
 		for (Plan x : planz) {
 			list.add(x.name);
 		};
 
 		
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, list);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner2.setAdapter(dataAdapter);
 
 		tl2 = (TableLayout) findViewById(R.id.recTable);
 		tl2.removeAllViews();
 		tl2.setVerticalScrollBarEnabled(true);
 	
 		int tsz = g.tasksSize();
 		System.out.println("PlanActivity -- plans tsz=" + tsz);
 		ArrayList<Task> taskz = g.getTaskArray();
 
 		for (Task x : taskz) {
 			// list.add(x.name);
 			addRecRow(tl2, x.name);
 		};
 		
 		//	addRecRow(tl2, idNumber + " " + et1);
 		
 		
 		Button btnAdd = (Button) findViewById(R.id.add1);
 		btnAdd.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Log.i(TAG, "===== ENTER btnAdd clicked =====");
 				// startVend();
 
 				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 
 				EditText text1 = (EditText) findViewById(R.id.searchInput);
 				et1 = text1.getText().toString();
 
 				Log.i(TAG, "===== ENTER input:" + et1); // simply use et1 for
 														// now
 
 				// move to table: EditText stuff = (EditText)
 				// findViewById(R.id.stuffToDo);
 
 				idNumber += 1;
 
 				// move to table: stuff.append("\n\n" + idNumber + " " + text1);
 
 				Toast.makeText(getApplicationContext(),
 						"Your selection is:" + et1 + ".", Toast.LENGTH_SHORT)
 						.show();
 
 				// ========
 
 				// for (String rec : Globals.recs) {
 				// String plainRec = extractPlainRec(rec);
 				// System.out.println(" rec = " + rec);
 				addRecRow(tl2, idNumber + " " + et1);
 				// };
 				// ========
 
 			}
 		});
 		Button btnAddPlan = (Button) findViewById(R.id.addPlan);
 		btnAddPlan.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Log.i(TAG, "===== ENTER btnAddPlan clicked =====");
 				startAddPlanActivity();
 			}
 		});
 	}
 
 	public void addRecRow(TableLayout tl, String rowData) {
 		TableRow tr2 = new TableRow(this);
 
 		Button btnSelect = new Button(this);
 		btnSelect.setText("Edit");
 		btnSelect.setTextSize(10.0f);
 		btnSelect.setTextColor(Color.BLACK);
 		// 1 Drawable sBackground =
 		// getResources().getDrawable(R.drawable.round_button_background);
 		// 1 btnSelect.setBackgroundDrawable(sBackground);
 
 		btnSelect.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				//
 				View p = (View) v.getParent();
 				TableRow tr = (TableRow) v.getParent();
 				TextView c = (TextView) tr.getChildAt(2);
 
 				String s = c.getText().toString();
 
 				System.out.println("      v = " + v);
 				System.out.println("      p = " + p);
 				System.out.println("      tr = " + tr);
 				System.out.println("      c = " + c);
 				System.out.println("      s = " + s); // so this IS what we
 														// select via DCAT
 
 				Toast.makeText(getApplicationContext(),
 						"Thanks for the Edit button press. Data: " + s,
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		LayoutParams lp = new LayoutParams(
 				LinearLayout.LayoutParams.FILL_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		lp.setMargins(5, 5, 5, 5);
 
 		// btnSelect.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 		// LayoutParams.WRAP_CONTENT));
 
 		btnSelect.setLayoutParams(lp);
 
 		/* Add Button to row. */
 		tr2.addView(btnSelect);
 
 		Button btnInfo = new Button(this);
 		btnInfo.setText("Info");
 		btnInfo.setTextSize(10.0f);
 		btnInfo.setTextColor(Color.BLACK);
 		// 1 btnInfo.setBackgroundDrawable(sBackground);
 		btnInfo.setLayoutParams(lp);
 
 		btnInfo.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 
 				// output.setText("");
 				View p = (View) v.getParent();
 				TableRow tr = (TableRow) v.getParent();
 				TextView c = (TextView) tr.getChildAt(2);
 
 				String s = c.getText().toString();
 
 				System.out.println("      v = " + v);
 				System.out.println("      p = " + p);
 				System.out.println("      tr = " + tr);
 				System.out.println("      c = " + c);
 				System.out.println("      s = " + s); // so this IS what we
 														// select via DCAT
 
 				Toast.makeText(getApplicationContext(),
 						"Thanks for the Info button press. Data: " + s,
 						Toast.LENGTH_SHORT).show();
 				//
 				// String fileName =
 				// com.faw.proto2.TMWActivity.convertRecommendationToFileName(s);
 				//
 				// popupPresoNew("file:///sdcard/MyMovies/" + fileName +
 				// ".html"); // convertRecommendationToFileName
 
 			}
 		});
 
 		// btnInfo.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 		// LayoutParams.WRAP_CONTENT));
 
 		tr2.addView(btnInfo);
 
 		tl.addView(tr2, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 
 		tl.invalidate(); // This line does not work
 
 		TextView t1 = new TextView(this);
 		// t1.setTextSize(40.0f);
 		t1.setTextSize(16.0f);
 		t1.setText(rowData);
 		t1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		/* Add Button to row. */
 		tr2.addView(t1);
 
 	}
 
 	public void addListenerOnSpinnerItemSelection() {
 		spinner1 = (Spinner) findViewById(R.id.planslist);
 		// ?? spinner1.setOnItemSelectedListener(new
 		// CustomOnItemSelectedListener());
 	}
 
 	// get the selected dropdown list value
 	public void addListenerOnButton() {
 
 		spinner1 = (Spinner) findViewById(R.id.planslist);
 		// btnSubmit = (Button) findViewById(R.id.btnSubmit);
 		//
 		// btnSubmit.setOnClickListener(new OnClickListener() {
 		// }
 		// @Override
 		// public void onClick(View v) {
 		//
 		// Toast.makeText(getApplicationContext(),
 		// "OnClickListener : " +
 		// "\nSpinner 1 : "+ String.valueOf(spinner1.getSelectedItem()) +
 		// "\nSpinner 2 : "+ String.valueOf(spinner2.getSelectedItem()),
 		// Toast.LENGTH_SHORT).show();
 		// }
 
 		// });
 	}
 
 	private void startAddPlanActivity() {
 		Intent workerIntent = new Intent(this, AddPlanActivity.class);
 		startActivity(workerIntent);
 	}
 
 
 }
