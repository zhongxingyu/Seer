 package com.wehuibao;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class DocListActivity extends SherlockFragmentActivity {
 	public static final String LIST_TYPE = "LIST_TYPE";
 	public static final String LIST_URL = "LIST_URL";
 	public static final String IS_START = "IS_START";
 	public static final String USER_NAME = "USER_NAME";
	public static final String USER_ID = "USER_ID";
 	private static final String ME = "@me";
 	static final String HOT_URL = "http://wehuibao.com/api/hot/";
 	private static final String DOC_LIST_URL = "http://wehuibao.com/api/doclist/";
 	private ListType lt;
 	private String userId;
 	private boolean isStart = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		String cookie = prefs.getString("cookie", null);
 		Bundle fragmentArgs = new Bundle();
 
 		Intent intent = getIntent();
 		String listType = intent.getStringExtra(LIST_TYPE);
 		if (listType != null) {
 			lt = ListType.getListType(listType);
 		} else {
 			if (cookie != null) {
 				lt = ListType.ME;
 			} else {
 				lt = ListType.HOT;
 			}
 		}
 		fragmentArgs.putString(LIST_TYPE, lt.toString());
 
 		switch (lt) {
 		case ME:
 			userId = "@me";
 			isStart = true;
 			fragmentArgs.putString(LIST_URL, DOC_LIST_URL + ME);
 			getSupportActionBar().setTitle(getString(R.string.menu_home));
 			break;
 		case OTHER:
 			userId = listType;
 			fragmentArgs.putString(LIST_URL, DOC_LIST_URL + userId);
 			String userName = intent.getStringExtra(USER_NAME);
			fragmentArgs.putString(USER_NAME, userName);
			fragmentArgs.putString(USER_ID, userId);
 			getSupportActionBar().setTitle(
 					userName + getString(R.string.user_home));
 			break;
 		default:
 			isStart = true;
 			fragmentArgs.putString(LIST_URL, HOT_URL);
 			this.getSupportActionBar().setTitle(R.string.menu_hot);
 		}
 		fragmentArgs.putBoolean(IS_START, isStart);
 		DocListFragment docListFragment = new DocListFragment();
 		docListFragment.setArguments(fragmentArgs);
 
 		if (this.getSupportFragmentManager().findFragmentById(
 				android.R.id.content) == null) {
 			this.getSupportFragmentManager().beginTransaction()
 					.add(android.R.id.content, docListFragment).commit();
 		}
 	}
 }
