 package com.inc.im.serptracker;
 
 import java.util.ArrayList;
 
 import com.flurry.android.FlurryAgent;
 import com.google.ads.AdView;
 import com.inc.im.serptracker.R;
 import com.inc.im.serptracker.adapters.DbAdapter;
 import com.inc.im.serptracker.data.Keyword;
 import com.inc.im.serptracker.data.UserProfile;
 import com.inc.im.serptracker.util.Util;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class ManageWebsitesActivity extends Activity {
 
 	ArrayList<UserProfile> data;
 	int selectedUserProfileId;
 	private AdView adView;
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		FlurryAgent.onEndSession(this);
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (adView != null)
 			adView.destroy();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.manage_profiles_layout);
 
 		adView = Util.loadAdmob(this);
 
 		initSpinner();
 
 		bindSaveButton();
 		bindBackButton();
 		bindDeleteButton();
 
 		
 		if (!Boolean.parseBoolean(getString(R.string.isPremium))) {
 
 			int keywordLimit = Integer
 					.parseInt(getString(R.string.keywordLimit));
 
 			Util.setKeywordLimit(
 					keywordLimit,
 					(EditText) findViewById(R.id.editText2),
					getString(R.string.free_version_limit_5_keywords_per_website),
 					ManageWebsitesActivity.this);
 
 		}
 
 	}
 
 	private void bindDeleteButton() {
 		((Button) findViewById(R.id.button3))
 				.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 
 						// get spinner selected value
 						int selectedPosInSpinner = ((Spinner) findViewById(R.id.spinner1))
 								.getSelectedItemPosition();
 
 						if (data == null || selectedPosInSpinner >= data.size())
 							return;
 
 						UserProfile u = data.get(selectedPosInSpinner);
 
 						if (u == null)
 							return;
 
 						if (new DbAdapter(getBaseContext()).deleteProfile(u)) {
 							Toast.makeText(
 									getBaseContext(),
 									getString(R.string.website_)
 											+ " "
 											+ u.url.toUpperCase()
 											+ " "
 											+ getString(R.string._succesfully_deleted),
 									Toast.LENGTH_SHORT).show();
 
 							// if there aren't any profiles to edit > direct to
 							// mainpage
 							if (new DbAdapter(getBaseContext())
 									.loadAllProfiles() == null) {
 								startActivity(new Intent(getBaseContext(),
 										MainActivity.class));
 							} else {
 								// refresh spinner values
 								initSpinner();
 							}
 
 						} else {
 							Toast.makeText(getBaseContext(),
 									getString(R.string.delete_failed),
 									Toast.LENGTH_SHORT).show();
 						}
 
 					}
 				});
 	}
 
 	public void initSpinner() {
 
 		data = new DbAdapter(getBaseContext()).loadAllProfiles();
 		ArrayList<String> spinnerValues = new ArrayList<String>();
 		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
 
 		// on init clear editText
 		((EditText) findViewById(R.id.editText1)).setText("");
 		((EditText) findViewById(R.id.editText2)).setText("");
 
 		// generate spinner values to show user
 		if (data != null)
 			for (UserProfile u : data)
 				spinnerValues.add(u.url + "[" + u.keywords.size() + "]");
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 				getBaseContext(), R.layout.main_activity_spinner_item,
 				R.id.textView1, spinnerValues);
 
 		spinner.setAdapter(adapter);
 
 		// onItemSelect populate edit text fields
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> adapter, View arg1,
 					int index, long arg3) {
 
 				populateEditTexts(index);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 		});
 
 	}
 
 	public void bindBackButton() {
 		((Button) findViewById(R.id.button2))
 				.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						startActivity(new Intent(getBaseContext(),
 								MainActivity.class));
 
 					}
 				});
 	}
 
 	private void bindSaveButton() {
 
 		((Button) findViewById(R.id.button1))
 				.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 
 						// load textView values
 						String inputSite = ((EditText) findViewById(R.id.editText1))
 								.getText().toString();
 						String keyword = ((EditText) findViewById(R.id.editText2))
 								.getText().toString();
 
 						// validating
 						if (keyword == null || keyword.length() < 1) {
 							Toast.makeText(
 									getBaseContext(),
 									getString(R.string.please_enter_some_keywords),
 									Toast.LENGTH_SHORT).show();
 							return;
 						}
 
 						if (inputSite == null || inputSite.length() < 5) {
 							Toast.makeText(
 									getBaseContext(),
 									getString(R.string.website_address_is_too_short_or_invalid),
 									Toast.LENGTH_SHORT).show();
 							return;
 						}
 
 						// inserting
 						Boolean editSuccess = new DbAdapter(getBaseContext())
 								.insertOrUpdate(inputSite, keyword,
 										selectedUserProfileId);
 
 						if (editSuccess) {
 							Toast.makeText(getBaseContext(),
 									R.string.website_edited_successfuly,
 									Toast.LENGTH_SHORT).show();
 
 							startActivity(new Intent(getBaseContext(),
 									MainActivity.class));
 						} else {
 							Toast.makeText(getBaseContext(),
 									R.string.website_edit_failure,
 									Toast.LENGTH_SHORT).show();
 						}
 
 					}
 				});
 	}
 
 	private void populateEditTexts(int index) {
 
 		if (data == null)
 			return;
 
 		if (index >= data.size())
 			return;
 		UserProfile selectedUserProfile = data.get(index);
 
 		if (selectedUserProfile == null)
 			return;
 
 		selectedUserProfileId = selectedUserProfile.id;
 
 		((EditText) findViewById(R.id.editText1))
 				.setText(selectedUserProfile.url);
 
 		ArrayList<Keyword> keywords = selectedUserProfile.keywords;
 
 		if (keywords == null || keywords.size() <= 0)
 			return;
 
 		String keyWordsToPopulate = "";
 
 		// for (Keyword k : keywords)
 		// keyWordsToPopulate += k.value + "\n";
 
 		for (int i = 0; i < keywords.size(); i++)
 			if (i != keywords.size() - 1)
 				keyWordsToPopulate += keywords.get(i).keyword + "\n";
 			else
 				keyWordsToPopulate += keywords.get(i).keyword;
 		((EditText) findViewById(R.id.editText2)).setText(keyWordsToPopulate);
 
 	}
 
 }
