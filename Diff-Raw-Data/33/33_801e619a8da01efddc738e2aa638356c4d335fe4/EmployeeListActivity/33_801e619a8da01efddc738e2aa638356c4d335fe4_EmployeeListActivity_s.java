 package com.ith.project;
 
 import java.util.ArrayList;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import com.ith.project.R;
 import com.ith.project.menu.CustomMenu;
 import com.ith.project.menu.CustomMenuListAdapter;
 import com.ith.project.sdcard.EmployeeLocal;
 import com.ith.project.sqlite.BulletinSQLite;
 import com.ith.project.sqlite.EmployeeSQLite;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class EmployeeListActivity extends Activity implements OnClickListener,
 		OnItemClickListener, TextWatcher {
 
 	private static final int MENU_EXIT = 0;
 	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/GetEmployeeList";
 
 	private static ArrayList<Employee> itemDetails;
 	private static ArrayList<Employee> returnItemDetails;
 	private static ArrayList<Employee> selectedItemDetails;
 	private ListView listView;
 	private ImageButton menuButton;
 	private ImageButton EmployeeButton;
 	private ImageButton homeButton;
 	private EditText searchBox;
 	private ImageButton searchButton;
 	private CheckBox checkBox;
 	private int currPos;
 	private HttpConnection conn;
 	private EmployeeLocal employeeLocal;
 	private EmployeeSQLite employeeSQLite;
 	private Employee employee;
 	private static EmployeeListItemArrayAdapter listItemArrAdapter;
 	private static int employeeCount;
 	private ProgressDialog pdialog;
 	private LinearLayout linLayoutMenu;
 	private ListView menuListView;
 	static CustomMenuListAdapter menuAdapter;
 	private Dialog dialog;
 	private static EmployeeListActivity context;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		pdialog = new ProgressDialog(this);
 		pdialog.setCancelable(true);
 		pdialog.setMessage("Loading ....");
 		pdialog.show();
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.employee_list_view);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
 				R.layout.custom_title);
 		context = this;
 		init();
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		employeeSQLite.closeDB();
 		pdialog.dismiss();
 		if (dialog != null)
 			dialog.dismiss();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		pdialog.dismiss();
 	}
 
 	/************************************************************************************
 	 * Initialize values first
 	 * ***********************************************************************************/
 	private void init() {
 		new Thread(new Runnable() {
 
 			public void run() {
 
 				JSONObject inputJson;
 				conn = HttpConnection.getSingletonConn();
 				// conn = new HttpConnection(url);
 				// employeeLocal = new EmployeeLocal();
 
 				employeeSQLite = new EmployeeSQLite(EmployeeListActivity.this);
 				employeeSQLite.openDB();
 
 				employee = new Employee();
 
 				inputJson = employee.getJsonUserLoginId(LoginAuthentication
 						.getUserLoginId());
 
 				Log.v("getemployee inquiry", "" + inputJson.toString());
 
 				/**
 				 * To remove add Bulletin for normal users and change the logo
 				 * as well
 				 **/
 				modifyEmployeeAdd4Admin(LoginAuthentication.getUserRoleId());
 
 				/** To establish connection to the web service **/
 				String employeesFromWS = conn.getJSONFromUrl(inputJson, url);
 
 				Log.v("Employees:", "" + employeesFromWS);
 
 				/** Update the local file according to the web service **/
 				// employeeLocal.updateLocalFiles(inputJson, employeesFromWS);
 				employeeSQLite.updateDBUsersTableJson(employeesFromWS);
 				// employeeLocal.updateLocalFiles();
 
 				/** Now read from the local file always **/
 				// JSONArray outputJson = employeeLocal
 				// .getJSONFromLocal(inputJson);
 
 				// Log.v("JsonArray:", "" + outputJson.toString());
 
 				// itemDetails = setEmployee(outputJson);
 				itemDetails = employeeSQLite.getJSONFromDB();
 				returnItemDetails = itemDetails;
 
 				// Log.v("Employees:",""+
 				// employeeLocal.getJSONFromLocal(inputJson).toString());
 
 				/**
 				 * To run the main thread after completion of the connection
 				 * thread
 				 **/
 				runOnUiThread(new Runnable() {
 
 					public void run() {
 
 						searchBox = (EditText) findViewById(R.id.editTextSearch);
 						searchBox.setFocusable(true);
 						searchBox.setFocusableInTouchMode(true);
 						searchBox
 								.addTextChangedListener(EmployeeListActivity.this);
 
 						searchButton = (ImageButton) findViewById(R.id.buttonSearch);
 						searchButton
 								.setOnClickListener(EmployeeListActivity.this);
 
 						menuButton = (ImageButton) findViewById(R.id.menu);
 						menuButton
 								.setOnClickListener(EmployeeListActivity.this);
 
 						// EmployeeButton = (ImageButton)
 						// findViewById(R.id.bulletin_add_icon);
 						// EmployeeButton.setOnClickListener(EmployeeListActivity.this);
 
 						homeButton = (ImageButton) findViewById(R.id.home);
 						homeButton
 								.setOnClickListener(EmployeeListActivity.this);
 
 						registerForContextMenu(homeButton);
 
 						listView = (ListView) findViewById(R.id.listView1);
 
 						Log.d("***Welcome to ListView***", ".......");
 						listItemArrAdapter = new EmployeeListItemArrayAdapter(
 								EmployeeListActivity.this,
 								R.layout.employee_list_items, itemDetails);
 						listView.setAdapter(listItemArrAdapter);
 						employeeCount = listItemArrAdapter.getCount();
 						listView.setOnItemClickListener(EmployeeListActivity.this);
 
 						pdialog.dismiss();
 					}
 				});
 			}
 
 		}).start();
 
 	}
 
 	/********************************************************************************
 	 * Fill Employee class and itemDetails ArrayList
 	 * *********************************************************************************/
 	public ArrayList<Employee> setEmployee(JSONArray tempJsonArr) {
 
 		ArrayList<Employee> tempArrList = new ArrayList<Employee>();
 		int jsonArrLength = tempJsonArr.length();
 		for (int i = 0; i < jsonArrLength; i++) {
 			try {
 				Employee tempEmployee = new Employee();
 				JSONObject tempJson = tempJsonArr.getJSONObject(i);
 				tempEmployee.setValues(tempJson);
 				tempArrList.add(tempEmployee);
 				Log.v("json object(Employees):", "" + tempJson.toString());
 
 			} catch (JSONException e) {
 				Log.e("jSONException", "" + e.getMessage());
 				e.printStackTrace();
 			}
 
 		}
 		return tempArrList;
 	}
 
 	/*********************************************************************************
 	 * Called when the LExit Button is Clicked [DEPRECIATED]
 	 * ******************************************************************************/
 	public void modifyEmployeeAdd4Admin(int userRolesId) {
 
 		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutCustomTitle);
 		LayoutInflater inflater = (LayoutInflater) this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		inflater.inflate(R.layout.custom_title, lin, false);
 		// ImageButton addEmployeeBut = (ImageButton)
 		// findViewById(R.id.bulletin_add_icon);
 		int id = getResources().getIdentifier("add_employee", "drawable",
 				getApplicationContext().getPackageName());
 		// addEmployeeBut.setImageResource(id);
 		// if (userRolesId == 2)
 		// findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
 	}
 
 	/****************************************************************************
 	 * Get the correct employee ID
 	 *************************************************************************/
 	public static ArrayList<Employee> getEmployeeArrayList() {
 		return returnItemDetails;
 	}
 
 	/****************************************************************************
 	 * View Employee
 	 *************************************************************************/
 	public void onItemClick(AdapterView<?> adapterView, View view,
 			int position, long id) {
 
 		pdialog.show();
 		Log.v("Employee LIst Item clicked", "@ " + position);
 		Intent intent = new Intent(EmployeeListActivity.this,
 				EmployeeViewActivity.class);
 		intent.putExtra("PositionOfEmployee", (position));
 		EmployeeListActivity.this.startActivity(intent);
 
 	}
 
 	public void onClick(View v) {
 		if (v.equals(menuButton)) {
 			pdialog.show();
 			Intent intent = new Intent(EmployeeListActivity.this,
 					GridItemActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			this.startActivity(intent);
 			// this.finish();
 		} else if (v.equals(EmployeeButton)) {
 			pdialog.show();
 			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
 			Intent intent = new Intent(EmployeeListActivity.this,
 					EmployeeAddActivity.class);
 			this.startActivity(intent);
 			this.finish();
 
 		} else if (v.equals(homeButton)) {
 			// Toast.makeText(this, "Home Button", Toast.LENGTH_SHORT).show();
 			// v.performLongClick();
 			callMenuDialog();
 		}
 
 	}
 
 	private void callMenuDialog() {
 
 		LayoutInflater menuInflater = (LayoutInflater) this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		linLayoutMenu = (LinearLayout) findViewById(R.id.linearLayoutCustomMenu_2);
 		// LinearLayout linLayoutMenu = new LinearLayout(this);
 		menuInflater.inflate(R.layout.menu_list_view, linLayoutMenu, false);
 
 		/** To bring front the Dialog box **/
 		dialog = new Dialog(this, R.style.mydialogstyle);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setCanceledOnTouchOutside(true);
 
 		/** To set the alignment of the Dialog box in the screen **/
 		WindowManager.LayoutParams WMLP = dialog.getWindow().getAttributes();
 		WMLP.x = getWindowManager().getDefaultDisplay().getWidth();
 		WMLP.gravity = Gravity.TOP;
 		WMLP.verticalMargin = 0.08f; // To put it below header
 		dialog.getWindow().setAttributes(WMLP);
 
 		/** To set the dialog box with the List layout in the android xml **/
 		dialog.setContentView(R.layout.menu_list_view);
 
 		menuListView = (ListView) dialog.findViewById(R.id.listView2);
 
 		/** make an arrayList of items to display at the CustomMenu **/
 		ArrayList<CustomMenu> tempArrList = new ArrayList<CustomMenu>();
 
 		/** To remove add Bulletin for normal users **/
 		if (LoginAuthentication.getUserRoleId() == 1) {
 			tempArrList.add(setMenuItems("Add Employee", "add_employee"));
 			tempArrList.add(setMenuItems("Delete Employee", "delete_user"));
 		}
 		tempArrList.add(setMenuItems("Send Web Message", "mail_web"));
 		tempArrList.add(setMenuItems("Send SMS", "mail_sms"));
 		tempArrList.add(setMenuItems("Phone Call", "call"));
 		tempArrList.add(setMenuItems("Exit", "exit"));
 
 		menuAdapter = new CustomMenuListAdapter(EmployeeListActivity.this,
 				R.layout.custom_menu_2, tempArrList);
 		menuListView.setAdapter(menuAdapter);
 		menuListView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> adapterView, View view,
 					int position, long id) {
 
 				TextView c = (TextView) view
 						.findViewById(R.id.textViewCustomMenu_2);
 				String keyword = c.getText().toString();
 
 				/** When "Add Employee" menu item is pressed **/
 				if (keyword.equals("Add Employee")) {
 					pdialog.show();
 					Intent intent = new Intent(EmployeeListActivity.this,
 							EmployeeAddActivity.class);
 					EmployeeListActivity.this.startActivity(intent);
 				} else if (keyword.equals("Delete Employee")) {
 					deleteEmployee();
 				} else if (keyword.equals("Send Web Message")) {
 
 				} else if (keyword.equals("Send SMS")) {
 
 				} else if (keyword.equals("Phone Call")) {
 
 				}
 				/** When "Exit" menu item is pressed **/
 				else if (keyword.equals("Exit")) {
 					pdialog.show();
 					EmployeeListActivity.this.finish();
 					GridItemActivity.getGridItemActivityInstance().finish();
 					ListItemActivity.getListItemActivityInstance().finish();
 				}
 			}
 
 			private void deleteEmployee() {
 				selectedItemDetails = new ArrayList<Employee>();
 				for (int i = 0; i < employeeCount; i++) {
 					if (itemDetails.get(i).getChecked()) {
 						selectedItemDetails.add(itemDetails.get(i));
 						Log.e("Deleted Items", ""
 								+ itemDetails.get(i).getEmployeeName());
 					}
 				}
 
 			}
 		});
 
 		dialog.show();
 
 	}
 
 	/****************************************************************************
 	 * When we have to set Menu Items in the ArrayList
 	 *************************************************************************/
 	public CustomMenu setMenuItems(String menuString, String menuIcon) {
 
 		CustomMenu menu = new CustomMenu(menuString, menuIcon);
 		menu.setValues(menuString, menuIcon);
 
 		return menu;
 	}
 
 	/****************************************************************************
 	 * When Home Button is Clicked
 	 *************************************************************************/
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 
 	}
 
 	/****************************************************************************
 	 * When the custom menu item is clicked
 	 *************************************************************************/
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		// String[] names = getResources().getStringArray(R.array.names);
 		switch (item.getItemId()) {
 		case R.id.itemAddEmployee:
 			Toast.makeText(this, "Add  Employee", Toast.LENGTH_SHORT).show();
 			pdialog.show();
 			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
 			Intent intent = new Intent(EmployeeListActivity.this,
 					EmployeeAddActivity.class);
 			this.startActivity(intent);
 			this.finish();
 			return true;
 
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	/****************************************************************************
 	 * Menu Item Creation
 	 *************************************************************************/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.exit);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/****************************************************************************
 	 * Menu Item Clicked
 	 *************************************************************************/
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case MENU_EXIT: {
 			// When Exit Button is clicked
 			this.finish();
 		}
 
 		default:
 			break;
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			// do something on back.
 			pdialog.show();
 			this.finish();
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/******************************************************************************************
 	 * A new ArrayAdapter to handle the List View
 	 * *****************************************************************************************/
 	private class EmployeeListItemArrayAdapter extends ArrayAdapter<Employee>
 			implements Filterable {
 
 		private Context cntxt;
 		private ArrayList<Employee> itemDets;
 
 		public EmployeeListItemArrayAdapter(Context context,
 				int textViewResourceId, ArrayList<Employee> itemDetails) {
 			super(context, textViewResourceId);
 			this.cntxt = context;
 			this.itemDets = itemDetails;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) cntxt
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			currPos = position;
 			View view;
 			// if (convertView == null)
 			{
 				view = new View(this.cntxt);
 
 				view = inflater.inflate(R.layout.employee_list_items, parent,
 						false);
 
 				parent.setBackgroundColor(Color.rgb(221, 221, 221));
 
 				TextView textView = (TextView) view
 						.findViewById(R.id.textViewEmployeeName);
 				textView.setText(this.itemDets.get(position).getEmployeeName());
 				textView.setFocusable(false);
 
 				TextView location = (TextView) view
 						.findViewById(R.id.textViewEmployeeLocation);
 				location.setText(this.itemDets.get(position).getAddress());
 				location.setFocusable(false);
 
 				ImageView imageView = (ImageView) view
 						.findViewById(R.id.imageViewEmployee);
 				if (this.itemDets.get(position).getGender().equals("Female"))
 					imageView.setBackgroundResource(R.drawable.female_employee);
 				else
 					imageView.setBackgroundResource(R.drawable.male_employee);
 				imageView.setFocusable(false);
 
 				CheckBox checkBox = (CheckBox) view
 						.findViewById(R.id.checkBox1);
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							itemDets.get(currPos).setChecked(true);
						}
 
 					}
 
 				});
 				inflater = null;
 			} // else
 				// view = (View) convertView;
 
 			return view;
 		}
 
 		public int getCount() {
 
 			return itemDets.size();
 		}
 
 		public Employee getItem(int arg0) {
 
 			return itemDets.get(arg0);
 		}
 
 		public long getItemId(int position) {
 
 			return position;
 		}
 
 		@Override
 		public void notifyDataSetChanged() {
 			super.notifyDataSetChanged();
 		}
 
 		@Override
 		public Filter getFilter() {
 
 			Filter filter = new Filter() {
 
 				@Override
 				protected FilterResults performFiltering(CharSequence constraint) {
 
 					FilterResults results = new FilterResults();
 					ArrayList<Employee> FilteredItemDetails = new ArrayList<Employee>();
 
 					/** If search string (constraint) is null, display all **/
 					if (constraint == null || constraint.length() == 0) {
 
 						results.values = itemDetails;
 						results.count = itemDetails.size();
 						returnItemDetails = itemDetails;
 					}
 					/** If constraint is not null */
 					else {
 
 						constraint = constraint.toString().toLowerCase();
 						for (int i = 0; i < itemDetails.size(); i++) {
 							Employee dataNames = itemDetails.get(i);
 							if (dataNames.getEmployeeName().toLowerCase()
 									.contains(constraint.toString())) {
 								FilteredItemDetails.add(dataNames);
 							}
 						}
 
 						results.count = FilteredItemDetails.size();
 						results.values = FilteredItemDetails;
 						returnItemDetails = FilteredItemDetails;
 					}
 					return results;
 				}
 
 				@Override
 				protected void publishResults(CharSequence constraint,
 						FilterResults results) {
 
 					itemDets = (ArrayList<Employee>) results.values;
 					notifyDataSetChanged();
 				}
 			};
 			return filter;
 		}
 	}
 
 	/****************************************************************************************
 	 * For text change detector
 	 * **************************************************************************************/
 	public void afterTextChanged(Editable arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int berore,
 			int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 
 		listItemArrAdapter.getFilter().filter(s.toString().toLowerCase());
 		listItemArrAdapter.notifyDataSetChanged();
 	}
 
 	/****************************************************************************
 	 * Get the Activity instance of GridItemActivity
 	 *************************************************************************/
 	public static EmployeeListActivity getEmployeeListActivityInstance() {
 		return context;
 	}
 }
