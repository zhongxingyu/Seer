 package com.amrak.gradebook;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class Categories extends Activity {
 
 	// database
 	TermsDBAdapter termsDB = new TermsDBAdapter(this);
 	CoursesDBAdapter coursesDB = new CoursesDBAdapter(this);
 	CategoriesDBAdapter categoriesDB = new CategoriesDBAdapter(this);
 	EvaluationsDBAdapter evalsDB = new EvaluationsDBAdapter(this);
 	RelativeLayout rLayoutLabels;
 
 	// context
 	Context context = this;
 
 	// view(s)
 	ListView listView;
 	TextView courseTitle;
 	TextView courseMark;
 	TextView courseCode;
 	View vCatDivLine;
 
 	// list adapter
 	private CategoriesListAdapter categoriesListAdapter;
 
 	// categories vector
 	ArrayList<CategoryData> categories = new ArrayList<CategoryData>();
 
 	// variables
 	final private String TAG = "Categories";
 	int[] refIDPass_Category;
 	int refIDGet_Term;
 	int refIDGet_Course;
 	int contextSelection;
 
 	// context menu option variables; context menu managed by a switch statement
 	final static int CONTEXT_EDIT = 0;
 	final static int CONTEXT_DELETE = 1;
 
 	CourseData courseData;
 
 	DecimalFormat twoDForm = new DecimalFormat("0.00");
 	// onCreate
 	@TargetApi(11)
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_categories);
 
 		// up button in action bar
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		Intent iCategory = getIntent();
 		refIDGet_Term = iCategory.getIntExtra("refID_Term", -1);
 		refIDGet_Course = iCategory.getIntExtra("refID_Course", -1);
 
 		// initialization of views
 		listView = (ListView) findViewById(R.id.lvCategories);
 		courseTitle = (TextView) findViewById(R.id.tvCategoriesCourseTitle);
 		courseMark = (TextView) findViewById(R.id.tvCategoriesCourseMark);
 		courseCode = (TextView) findViewById(R.id.tvCategoriesCourseCode);
 		rLayoutLabels = (RelativeLayout) findViewById(R.id.rCatLayoutLabelCategories);
 		vCatDivLine = (View) findViewById(R.id.vCatDivLine);
 		// read from data base
 		coursesDB.open();
 		Cursor cCourse = coursesDB.getCourse(refIDGet_Course);
 
 		courseData = new CourseData(cCourse.getInt(cCourse
 				.getColumnIndex("_id")), cCourse.getString(cCourse
 				.getColumnIndex("courseTitle")), cCourse.getString(cCourse
 				.getColumnIndex("courseCode")), cCourse.getInt(cCourse
 				.getColumnIndex("courseUnits")), cCourse.getString(cCourse
 				.getColumnIndex("notes")), cCourse.getInt(cCourse
 				.getColumnIndex("termRef")), context);
 
 		courseTitle.setText(courseData.getTitle());
 		courseCode.setText(courseData.getCode());
 		courseMark
 				.setText(String.valueOf(twoDForm.format(courseData.getMark())));
 		coursesDB.close();
 
 		dataReadToList();
 
 		categoriesListAdapter = new CategoriesListAdapter(this, categories);
 		listView.setAdapter(categoriesListAdapter);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> a, View v, int position,
 					long id) {
 				try {
 					@SuppressWarnings("rawtypes")
 					Class cEvaluations;
 					cEvaluations = Class
 							.forName("com.amrak.gradebook.Evaluations");
 					Intent iEvaluations = new Intent(Categories.this,
 							cEvaluations);
 					iEvaluations.putExtra("refID_Term", refIDGet_Term);
 					iEvaluations.putExtra("refID_Course", refIDGet_Course);
 					iEvaluations.putExtra("refID_Category",
 							refIDPass_Category[position]);
 					startActivity(iEvaluations);
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		});
 		registerForContextMenu(listView);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		dataReset();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_categories, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.preferences:
 			Intent i = new Intent("com.amrak.gradebook.SETTINGS");
 			startActivity(i);
 			break;
 		case R.id.addcategory:
 			Intent iAddCat = new Intent("com.amrak.gradebook.ADDCAT");
 			iAddCat.putExtra("refID_Course", refIDGet_Course);
 			iAddCat.putExtra("refID_Term", refIDGet_Term);
 			iAddCat.putExtra("id_Mode", 0);
 			startActivity(iAddCat);
 			break;
 		case android.R.id.home:
 			finish();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	// Context menu
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		CategoryData catSelected = (CategoryData) categoriesListAdapter
 				.getItem(info.position);
 		String evalTitle = catSelected.getTitle();
 		if (evalTitle != "All"){
 			menu.setHeaderTitle(evalTitle);
 			menu.add(0, CONTEXT_EDIT, 0, "Edit Category");
 			menu.add(0, CONTEXT_DELETE, 0, "Delete Category");
 		}
 	}
 
 	public boolean onContextItemSelected(MenuItem menuItem) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem
 				.getMenuInfo();
 		contextSelection = info.position;
 
 		switch (menuItem.getItemId()) {
 		case CONTEXT_EDIT:
 			Intent iAddCategory = new Intent("com.amrak.gradebook.ADDCAT");
 			iAddCategory.putExtra("refID_Term", refIDGet_Term);
 			iAddCategory.putExtra("refID_Course", refIDGet_Course);
 			iAddCategory.putExtra("idEdit_Item",
 					refIDPass_Category[contextSelection]);
 			iAddCategory.putExtra("id_Mode", 1);
 			startActivity(iAddCategory);
 			dataReset();
 			return true;
 		case CONTEXT_DELETE:
 			categoriesDB.open();
 			Cursor cDelete = categoriesDB
 					.getCategory(refIDPass_Category[contextSelection]);
 			String titleDelete = cDelete.getString(cDelete
 					.getColumnIndex("catTitle"));
 			categoriesDB.close();
 
 			new AlertDialog.Builder(this)
 					.setTitle("Delete Category and Evaluations?")
 					.setMessage(
 							"Are you sure you want to delete \"" + titleDelete
 									+ "\" and ALL Evaluations within it?")
 					.setPositiveButton("Delete",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 
 									evalsDB.open();
 									evalsDB.deleteEvaluationsOfCategory(refIDPass_Category[contextSelection]);
 									evalsDB.close();
 
 									categoriesDB.open();
 									Cursor cDelete = categoriesDB
 											.getCategory(refIDPass_Category[contextSelection]);
 									String titleDelete = cDelete.getString(cDelete
 											.getColumnIndex("catTitle"));
 									categoriesDB
 											.deleteCategory(refIDPass_Category[contextSelection]);
 									categoriesDB.close();
 
 									Toast toast = Toast
 											.makeText(
 													context,
 													titleDelete
 															+ " and all Evaluations within it was deleted successfully.",
 													Toast.LENGTH_SHORT);
 									try {
 										// center toast
 										((TextView) ((LinearLayout) toast
 												.getView()).getChildAt(0))
 												.setGravity(Gravity.CENTER_HORIZONTAL);
 									} catch (ClassCastException cce) {
 										Log.d(TAG, cce.getMessage());
 									}
 									toast.show();
 									dataReset();
 								}
 							}).setNegativeButton("Cancel", null).show();
 
 			return true;
 		default:
 			return super.onContextItemSelected(menuItem);
 		}
 	}
 
 	public void dataReset() {
 		// clear data
 		categories.clear();
 		// read database
 		dataReadToList();
 
 		// input listview data
 		categoriesListAdapter.notifyDataSetChanged();
 
 		// refreshes mark after editing sublevels
 		coursesDB.open();
 		Cursor cCourse = coursesDB.getCourse(refIDGet_Course);
 		courseData = new CourseData(cCourse.getInt(cCourse
 				.getColumnIndex("_id")), cCourse.getString(cCourse
 				.getColumnIndex("courseTitle")), cCourse.getString(cCourse
 				.getColumnIndex("courseCode")), cCourse.getInt(cCourse
 				.getColumnIndex("courseUnits")), cCourse.getString(cCourse
 				.getColumnIndex("notes")), cCourse.getInt(cCourse
 				.getColumnIndex("termRef")), context);
 		courseMark
 				.setText(String.valueOf(twoDForm.format(courseData.getMark())));
 		coursesDB.close();
 
 	}
 
 	public void dataReadToList() {
 		categoriesDB.open();
 
 		Cursor c = categoriesDB.getCategoriesOfCourse(refIDGet_Course);
 		refIDPass_Category = new int[c.getCount() + 1];
 		int i = 0;
 		if (c.moveToFirst()) {
 			categories.add(new CategoryData(0, "All", 100, c.getInt(c
 					.getColumnIndex("courseRef")), c.getInt(c
 					.getColumnIndex("termRef")), context));
 			refIDPass_Category[i] = 0;
 			i++;
 			do {
 				refIDPass_Category[i] = c.getInt(c.getColumnIndex("_id")); // get
 																			// ids
 																			// of
 																			// each.
 				categories.add(new CategoryData(c.getInt(c
 						.getColumnIndex("_id")), c.getString(c
 						.getColumnIndex("catTitle")), c.getInt(c
 						.getColumnIndex("catWeight")), c.getInt(c
 						.getColumnIndex("courseRef")), c.getInt(c
 						.getColumnIndex("termRef")), context));
 				i++;
 			} while (c.moveToNext());
 		}
 
 		// set label and divider to visible/invisible if there is at least 1
 		// category
 		if (c.getCount() > 0) {
 			rLayoutLabels.setVisibility(View.VISIBLE);
 			vCatDivLine.setVisibility(View.VISIBLE);
 		} else {
 			rLayoutLabels.setVisibility(View.INVISIBLE);
 			vCatDivLine.setVisibility(View.INVISIBLE);
 
 		}
 		categoriesDB.close();
 	}
 
 }
