 package com.BreakingBytes.SifterReader;
 
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class IssuesActivity extends ListActivity {
 
 	public static final String STATUSES = "statuses";
 	public static final String PRIORITIES = "priorities";
 	public static final String NUMBER = "number";
 	public static final String STATUS = "status";
 	public static final String PRIORITY = "priority";
 	public static final String SUBJECT = "subject";
 	public static final String PAGE = "page";
 	public static final String TOTAL_PAGES = "total_pages";
 	public static final String NEXT_PAGE_URL = "next_page_url";
 	public static final String PREVIOUS_PAGE_URL = "previous_page_url";
 	public static final String GOTO_PAGE = "page";
 	public static final String PER_PAGE = "per_page";
 	public static final int MAX_PER_PAGE = 25;
 	public static final int SETTINGS_ID = Menu.FIRST;
 	public static final int EXIT_ID = Menu.FIRST + 1;
 	static final int NUMBER_DIALOG_ID = 0;
 	
 	// Members
 	private SifterHelper mSifterHelper;
 	private String mIssuesURL;
 	private JSONObject mIssues = new JSONObject();
 	private JSONObject[] mAllIssues;
 	private int mTotalPages;
 	private int mPage;
 	private int mPerPage;
 	private JSONObject mStatuses = new JSONObject();
 	private JSONObject mPriorities = new JSONObject();
 	private CheckBox[] mStatusCB;
 	private CheckBox[] mPriorityCB;
 	private boolean[] mFilterStatus;
 	private boolean[] mFilterPriority;
 	private int mNumStatuses;
 	private int mNumPriorities;
 	private JSONArray mStatusNames;
 	private JSONArray mPriorityNames;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.issues_list);
 		registerForContextMenu(getListView());
 		
 		mSifterHelper = new SifterHelper(this);
 		try {
 			mSifterHelper.getKey();
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		
 		JSONObject statuses = getFilters(STATUSES);
 		JSONObject priorities = getFilters(PRIORITIES);
 		if (statuses == null || priorities == null)
 			return;
 		mStatuses = statuses;
 		mNumStatuses = mStatuses.length();
 		mStatusNames = mStatuses.names();
 		mPriorities = priorities;
 		mNumPriorities = mPriorities.length();
 		mPriorityNames = mPriorities.names();
 		mFilterStatus = new boolean[mNumStatuses];
 		mFilterPriority = new boolean[mNumPriorities];
 		for (int i = 0; i < mNumStatuses; i++)
 			mFilterStatus[i] = true;
 		for (int i = 0; i < mNumPriorities; i++)
 			mFilterPriority[i] = true;
     	try {
 			JSONObject filters = mSifterHelper.getFilters();
 			if (filters.length() != 0) {
 				JSONArray status = filters.getJSONArray(STATUS);
 				JSONArray priority = filters.getJSONArray(PRIORITY);
 				for (int i = 0; i < mNumStatuses; i++)
 					mFilterStatus[i] = status.getBoolean(i);
 				for (int i = 0; i < mNumPriorities; i++)
 					mFilterPriority[i] = priority.getBoolean(i);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
     	
 		TextView pageTotal = (TextView)findViewById(R.id.page_total);
 		EditText pageNumber = (EditText)findViewById(R.id.page_number);
 		Button gotoPageButton = (Button)findViewById(R.id.goto_page);
 		Button prevPageButton = (Button)findViewById(R.id.previous_page);
 		Button nextPageButton = (Button)findViewById(R.id.next_page);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras == null)
 			return;
 		try {
 			String issuesURL = extras.getString(SifterReader.ISSUES_URL);
			if (issuesURL != null)
				mIssuesURL = issuesURL;
 			JSONObject issues = new JSONObject(extras.getString(SifterReader.ISSUES));
 			if (issues != null) {
 				mIssues = issues; 
 				getIssues();
 				mPage = mIssues.getInt(PAGE);
 				mTotalPages = mIssues.getInt(TOTAL_PAGES);
 				mPerPage = mIssues.getInt(PER_PAGE);
 				pageNumber.setText(String.valueOf(mPage));
 				pageTotal.setText(" / " + String.valueOf(mTotalPages));
 
 				prevPageButton.setOnClickListener(new View.OnClickListener() {
 					// anonymous inner class
 					public void onClick(View view) {
 						loadIssuesPage(PREVIOUS_PAGE_URL);
 					}
 				});
 
 				nextPageButton.setOnClickListener(new View.OnClickListener() {
 					// anonymous inner class
 					public void onClick(View view) {
 						loadIssuesPage(NEXT_PAGE_URL);
 					}
 				});
 
 				gotoPageButton.setOnClickListener(new View.OnClickListener() {
 					// anonymous inner class
 					public void onClick(View view) {
 						EditText pageNumber = (EditText)findViewById(R.id.page_number);
 						int newPage = 0;
 						try {
 							newPage = Integer.valueOf(pageNumber.getText().toString());
 						} catch (NumberFormatException e) {
 							pageNumber.setText(String.valueOf(mPage));
 							e.printStackTrace();
 							mSifterHelper.onException(e.toString());
 							return;
 						}
 						loadIssuesPage(newPage);
 					}
 				});
 
 				fillData();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString()); // return not needed
 		}
 	}
 
 	/** Menu button options. */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, SETTINGS_ID, 0, R.string.issues_settings);
 		menu.add(0, EXIT_ID, 0, R.string.issues_exit);
 		return result;
 	}
 	
 	/** Methods for selected menu option. */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case SETTINGS_ID:
 			showDialog(NUMBER_DIALOG_ID);
 			return true;
 		case EXIT_ID:
 			Intent intent = new Intent(this, SifterReader.class);
 			startActivity(intent);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case NUMBER_DIALOG_ID:
 	    	Dialog perpageDialog = new Dialog(this);
 	    	perpageDialog.setOnDismissListener(mNumberSetListener);
 	    	perpageDialog.setContentView(R.layout.perpage_dialog);
 	    	perpageDialog.setTitle(R.string.issues_settings);
 	    	LinearLayout statusLL = (LinearLayout) perpageDialog.findViewById(R.id.status);
 	    	LinearLayout priorityLL = (LinearLayout) perpageDialog.findViewById(R.id.priority);
 	    	EditText perpage = (EditText) perpageDialog.findViewById(R.id.perpage);
 	    	perpage.setText(String.valueOf(mPerPage));
 	    	Button okButton = (Button) perpageDialog.findViewById(R.id.perpage_ok);
 	    	okButton.setOnClickListener(new View.OnClickListener() {
 				// anonymous inner class
 				public void onClick(View view) {
 					dismissDialog(NUMBER_DIALOG_ID);
 				}
 			});
 	    	mStatusCB = new CheckBox[mNumStatuses];
 	    	mPriorityCB = new CheckBox[mNumPriorities];
 	    	try {
 	    		for (int i = 0; i < mNumStatuses; i++) {
 	    			mStatusCB[i] = new CheckBox(this);
 	    			mStatusCB[i].setText(mStatusNames.getString(i));
 	    			mStatusCB[i].setChecked(mFilterStatus[i]);
 	    			statusLL.addView(mStatusCB[i]);
 	    		}
 	    		for (int i = 0; i < mNumPriorities; i++) {
 	    			mPriorityCB[i] = new CheckBox(this);
 	    			mPriorityCB[i].setText(mPriorityNames.getString(i));
 	    			mPriorityCB[i].setChecked(mFilterPriority[i]);
 	    			priorityLL.addView(mPriorityCB[i]);
 	    		}
 	    	} catch (JSONException e) {
 	    		e.printStackTrace();
 	    		mSifterHelper.onException(e.toString());
 	    		return dialog; // return null on exception
 	    	}
 	    	return perpageDialog;
 	    default:
 	        dialog = null;
 	    }
 	    return dialog;
 	}
 	
 	// the callback received when the user "sets" the number in the dialog
 	private DialogInterface.OnDismissListener mNumberSetListener =
 			new DialogInterface.OnDismissListener() {
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 			Dialog perpageDialog = (Dialog)dialog;
 			EditText perpage = (EditText) perpageDialog.findViewById(R.id.perpage);
 			int newPerPage = 0;
 			try {
 				newPerPage = Integer.valueOf(perpage.getText().toString());
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				mSifterHelper.onException(e.toString());
 				return;
 			}
 			newPerPage = (newPerPage > MAX_PER_PAGE) ? MAX_PER_PAGE : newPerPage;
 			mPerPage = (newPerPage < 1) ? 1 : newPerPage;
 			mFilterStatus = new boolean[mNumStatuses];
 			mFilterPriority = new boolean[mNumPriorities];
 			for (int i = 0; i < mNumStatuses; i++) {
     			mFilterStatus[i] = mStatusCB[i].isChecked();
     		}
     		for (int i = 0; i < mNumPriorities; i++) {
     			mFilterPriority[i] = mPriorityCB[i].isChecked();
     		}
     		mSifterHelper.saveFilters(mFilterStatus, mFilterPriority);
 			loadIssuesPage(mPage);
 		}
 	};
 
 	private void getIssues() {
 		JSONObject[] allIssues = null;
 		try {
 			JSONArray issuesArray = mIssues.getJSONArray(SifterReader.ISSUES);
 			int numberIssues = issuesArray.length();
 			allIssues = new JSONObject[numberIssues];
 			for (int i = 0; i < numberIssues; i++)
 				allIssues[i] = issuesArray.getJSONObject(i);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		mAllIssues = allIssues;
 	}
 	
 	private void fillData() {
 		int iNum = mAllIssues.length;
 		List<Map<String,String>> issuesList = new ArrayList<Map<String,String>>(iNum); 
 		try {
 			for (int j = 0; j < iNum; j++) {
 				Map<String,String> map = new HashMap<String,String>();
 				map.put(NUMBER,mAllIssues[j].getString(NUMBER));
 				map.put(STATUS,mAllIssues[j].getString(STATUS));
 				map.put(PRIORITY,mAllIssues[j].getString(PRIORITY));
 				map.put(SUBJECT,mAllIssues[j].getString(SUBJECT));
 				issuesList.add(map);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
         ListAdapter adapter = new SimpleAdapter(this, issuesList,
         		R.layout.issue_row,
                 new String[] {NUMBER, STATUS, PRIORITY, SUBJECT},
                 new int[] {R.id.issue_number, R.id.issue_status, R.id.issue_priority, R.id.issue_subject});
         setListAdapter(adapter);
 	}
 
 	/** Intent for Project Details Activities. */
 	private void loadIssuesPage(String PAGE_URL) {
 		String pageURL = null;
 		// get project detail url from project
 		try {
 			pageURL = mIssues.getString(PAGE_URL);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		if (pageURL == null)
 			return;
 		changePage(pageURL);
 	}
 	
 	/** Intent for Project Details Activities. */
 	private void loadIssuesPage(int newPage) {
 		newPage = (newPage > mTotalPages) ? mTotalPages : newPage;
 		mPage = (newPage < 1) ? 1 : newPage;
 		String pageURL = mIssuesURL + "?" + PER_PAGE + "=" + mPerPage;
 		pageURL += "&" + GOTO_PAGE + "=" + mPage;
 		changePage(pageURL);
 	}
 	
 	private void changePage(String pageURL) {
 		try {
 			String filterSlug = "&s=";
 			for (int i = 0; i < mNumStatuses; i++) {
 				if (mFilterStatus[i])
 					filterSlug += String.valueOf(mStatuses.getInt(mStatusNames.getString(i))) + "-";
 			}
 			if (filterSlug.length() > 3) {
 				filterSlug = filterSlug.substring(0, filterSlug.length()-1);
 				pageURL += filterSlug;
 			}
 			filterSlug = "&p=";
 			for (int i = 0; i < mNumPriorities; i++) {
 				if (mFilterPriority[i])
 					filterSlug += String.valueOf(mPriorities.getInt(mPriorityNames.getString(i))) + "-";
 			}
 			if (filterSlug.length() > 3) {
 				filterSlug = filterSlug.substring(0, filterSlug.length()-1);
 				pageURL += filterSlug;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		// get url connection
 		URLConnection sifterConnection = mSifterHelper.getSifterConnection(pageURL);
 		if (sifterConnection == null)
 			return;
 		// get JSON object
 		JSONObject sifterJSONObject = new JSONObject();
 		try {
 			sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		Intent intent = new Intent(this, IssuesActivity.class);
 		intent.putExtra(SifterReader.ISSUES, sifterJSONObject.toString());
 		intent.putExtra(SifterReader.ISSUES_URL, mIssuesURL);
 		startActivity(intent);
 		return;
 	}
 	
 	private JSONObject getFilters(String filter) {
 		String filterURL = SifterReader.HTTPS_PREFIX + mSifterHelper.mDomain;
 		filterURL += SifterReader.PROJECTS_URL + filter;
 		// get url connection
 		URLConnection sifterConnection = mSifterHelper.getSifterConnection(filterURL);
 		if (sifterConnection == null)
 			return new JSONObject();
 		// get JSON object
 		JSONObject filterJSONObject = new JSONObject();
 		try {
 			JSONObject sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 			filterJSONObject = sifterJSONObject.getJSONObject(filter);
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return new JSONObject();
 		}
 		return filterJSONObject;
 	}
 }
