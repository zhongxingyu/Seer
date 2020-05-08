 package com.komodo.tagin;
 
 /**
  * Komodo Lab: Tagin! Project: 3D Tag Cloud
  * Google Summer of Code 2011
  * @authors Reza Shiftehfar, Sara Khosravinasr and Jorge Silva
  */
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 import ca.idi.taginsdk.Fingerprinter;
 import ca.idi.taginsdk.Helper;
 import ca.idi.taginsdk.TaginURN;
 
 /**
  * SampleTagCloud class: this is a sample program to show how the 3D Tag Cloud
  * can be used. It Creates the activity and sets the ContentView to our
  * TagCloudView class
  */
 public class Main extends Activity {
 	
 	public static String URN;
	public static final String EXTRA_URN = "Uniform Resource Name";
 	public static final String EXTRA_TAG_NAME = "tag_name";
 	public static final String EXTRA_TAG_POPULARITY = "popularity";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		tDb = new TagsDatabase(this);
		tDb.open();
 
 		// Step1: register the receiver to get changes in your location using
 		// URN and start it:
 		registerReceiver(mReceiver, new IntentFilter(TaginURN.ACTION_URN_READY));
 		startURNFetchService(); // start the engine
 
 		// Step2: to get a full-screen View:
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		// Step3: get screen resolution:
 		Display display = getWindowManager().getDefaultDisplay();
 		width = display.getWidth();
 		height = display.getHeight();
 
 		// Step4: start splash screen and wait for current location to be found:
 		// tagCloudcreated is used to know whether we need to create or update
 		// the tagcloud
 		tagCloudCreated = false;
 		startSplashScreen();
 		// notice that creation/update of tagcloud has been
 		// moved to BroadcastReceiver.onReceive() method
 		
 	}
 
 	private void startSplashScreen() {
 		setContentView(R.layout.splash);
 	}
 
 	private void stopSplashScreen() {
 	}
 
 	private void createTagCloud(List<Tag> tempTagList) {
 		// create a TagCloudview and set it as the content of this Activity
 		tagList = tempTagList;
 		if (tempTagList == null || tempTagList.isEmpty()){
 			Toast.makeText(this, "No Recorded Tag Exists.",
 					Toast.LENGTH_LONG).show();
 			Intent intent = new Intent(this, TagAdder.class);
 			intent.putExtra(EXTRA_URN, URN);
 			startActivityForResult(intent, TAG_ADDED);
 			return;
 		}
 		mTagCloudView = new TagCloudView(this, width, height, tagList); // passing
 																		// current
 																		// context
 		setContentView(mTagCloudView);
 		mTagCloudView.requestFocus();
 		mTagCloudView.setFocusableInTouchMode(true);
 		tagCloudCreated = true;
 	}
 
 	private void updateTagCloud(List<Tag> tempTagList){
 		if (tempTagList == null)
 			return;
 		List<Tag> tagListMinusTempTagList,tempTagListMinusTagList;
 		tagListMinusTempTagList = listAMinuslistB(tagList, tempTagList);
 		tempTagListMinusTagList = listAMinuslistB(tempTagList, tagList);
 		int noOfPossibleUpdate, noOfNeededUpdate;
 		noOfPossibleUpdate = tagListMinusTempTagList.size();
 		noOfNeededUpdate = tempTagListMinusTagList.size();
 		if (noOfNeededUpdate == 0)
 			return;
 		if (noOfNeededUpdate>noOfPossibleUpdate){ //we also need to add some new Tags
 			//do all the possible updates
 			for (int i = 0; i < noOfPossibleUpdate; i++){
 				mTagCloudView.Replace(tempTagListMinusTagList.get(i),
 										tagListMinusTempTagList.get(i).getText());
 				//update the RGB value and scaled textSize of the tag
 				mTagCloudView.setTagRGBT(tempTagListMinusTagList.get(i), 
 										 tempTagListMinusTagList.get(i).getPopularity());
 				//update the tagList to reflect new tag
 				Replace(tempTagListMinusTagList.get(i),
 						tagListMinusTempTagList.get(i).getText());
 			}
 			//add the remaining as new tags
 			for (int i = noOfPossibleUpdate; i < noOfNeededUpdate; i++){
 				mTagCloudView.addTag(tempTagListMinusTagList.get(i));
 				tagList.add(tempTagListMinusTagList.get(i));
 			}
 		} else{ //no need to add any new tag
 			//do all updates:
 			for (int i = 0; i < noOfNeededUpdate; i++){
 				mTagCloudView.Replace(	tempTagListMinusTagList.get(i),
 										tagListMinusTempTagList.get(i).getText());
 				//update the RGB value and scaled textSize of the tag
 				mTagCloudView.setTagRGBT(tempTagListMinusTagList.get(i), 
 										 tempTagListMinusTagList.get(i).getPopularity());
 				//update the tagList to reflect new tag
 				Replace(tempTagListMinusTagList.get(i),
 						tagListMinusTempTagList.get(i).getText());
 			}
 		}
 		Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
 	}
 
 	public void Replace(Tag newTag, String oldTagText){
 		for (int i=0; i< tagList.size(); i++)
 			if ( oldTagText.equalsIgnoreCase(tagList.get(i).getText())){
 				tagList.get(i).setPopularity(newTag.getPopularity());
 				tagList.get(i).setText(newTag.getText());
 				tagList.get(i).setUrl(newTag.getUrl());
 				break;
 			}
 	}
 
 	private List<Tag> listAMinuslistB(List<Tag> listA, List<Tag> listB) {
 		List<Tag> tempList = new ArrayList<Tag>();
 		for (int i = 0; i < listA.size(); i++) {
 			String listAText = listA.get(i).getText();
 			boolean found = false;
 			for (int j = 0; j < listB.size(); j++) {
 				if (listB.get(j).getText().equalsIgnoreCase(listAText)) {
 					found = true;
 					break;
 				}
 			}
 			if (!found)
 				tempList.add(listA.get(i));
 		}
 		return tempList;
 	}
 
 	/* The "option menu" for the Cloud */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.add_tag:
 			Intent intent = new Intent(this, TagAdder.class);
 			intent.putExtra(EXTRA_URN, URN);
 			startActivityForResult(intent, TAG_ADDED);
 			break;
 		case R.id.exit_app:
 			finish();
 			break;
 		}
 		return true;
 	}
 
 	//executed whenever a new tag is added to the DB
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case TAG_ADDED: // this is the case when user has just added a new Tag to DB
 			//Getting the tag name and popularity
 			String tag_name = data.getStringExtra(EXTRA_TAG_NAME); 
 			int popularity = data.getIntExtra(EXTRA_TAG_POPULARITY, 10);
 			Tag tempTag= new Tag(tag_name, popularity, SEARCH_TEXT + tag_name );			
 			if (!tagCloudCreated) { // first time:: stop splash and create TagCloud
 				List<Tag> tempTagList= new ArrayList<Tag>();
 				tempTagList.add(tempTag);
 				stopSplashScreen();
 				createTagCloud(tempTagList);
 			} else{ // after initial creation of Tag Cloud, just update it
 				List<Tag> tempTagList = new ArrayList<Tag>(tagList);
 				tempTagList.add(tempTag);
 				updateTagCloud(tempTagList);
 			}
 			registerReceiver(mReceiver, new IntentFilter(TaginURN.ACTION_URN_READY));
 			startURNFetchService();
 			break;
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		stopService(new Intent(TaginURN.INTENT_STOP_SERVICE));
 		super.onStop();
 	}
 
 	protected void onResume() {
 		super.onResume();
 	}
 
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (mReceiver != null) {
 			unregisterReceiver(mReceiver);
 			mReceiver = null;
 		}
 		stopService(new Intent(Fingerprinter.INTENT_STOP_SERVICE));
 	}
 
 	/**
 	 * Call this function to start the 3D Cloud Walk => this will start the
 	 * engine
 	 */
 	private void startURNFetchService() {
 		Intent intent = new Intent(TaginURN.INTENT_START_SERVICE);
 		//Pass the number of runs and interval between runs as extras.
 		startService(intent); 
 	}
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (action.equals(TaginURN.ACTION_URN_READY)) {
 				URN = TaginURN.getURN();
 				Log.i(Helper.TAG, URN);
 				List<Tag> tempTagList = new ArrayList<Tag>();
 				tempTagList = fetchTags(URN);
 				if (!tagCloudCreated) { // first time:: stop splash and create TagCloud
 					stopSplashScreen();
 					createTagCloud(tempTagList);
 				} else{ // after initial creation of Tag Cloud, just update it
 					updateTagCloud(tempTagList);
 				}
 			}
 		}
 	};
 
 	private List<Tag> fetchTags(String urn) {
 		//in order to make the Tag URL point to Google search for that		
 		Cursor c1, c2;
 		c1 = tDb.fetchTagId(urn);
 		if (c1 == null){
 			Log.d(Helper.TAG, "No TAGS present");
 			return null;
 		}
 		long tag_id;
 		String tag_name, URL;
 		int popularity;
 		List<Tag> tempList = new ArrayList<Tag>();
 		do {
 			tag_id = c1.getLong(c1.getColumnIndexOrThrow(TagsDatabase.TAG_ID));
 			c2 = tDb.fetchTagDetails(tag_id);
 			if(c2!=null){
 				tag_name = c2.getString(c2.getColumnIndexOrThrow(TagsDatabase.TAG_NAME));
 				popularity = c2.getInt(c2.getColumnIndexOrThrow(TagsDatabase.POPULARITY));
 				URL = SEARCH_TEXT+tag_name;
 				Tag tag = new Tag(tag_name, popularity,URL);
 				tempList.add(tag);
 			}	
 		} while (c1.moveToNext());
 		c1.close();
 		return tempList;
 	}
 	
 	
 	private boolean tagCloudCreated;
 	private int width, height;
 	private List<Tag> tagList;
 	private static final int TAG_ADDED = 1;
 	private TagsDatabase tDb;
 	private TagCloudView mTagCloudView;
 	private String SEARCH_TEXT="http://www.google.com/m?hl=en&q=";
 }
