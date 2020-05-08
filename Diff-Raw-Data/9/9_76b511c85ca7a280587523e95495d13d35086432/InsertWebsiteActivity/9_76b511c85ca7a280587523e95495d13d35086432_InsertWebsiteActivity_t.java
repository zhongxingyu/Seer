 package com.inc.im.serptracker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.flurry.android.FlurryAgent;
 import com.google.ads.AdView;
 import com.inc.im.serptracker.adapters.DbAdapter;
 import com.inc.im.serptracker.util.Premium;
 import com.inc.im.serptracker.util.Util;
 
 public class InsertWebsiteActivity extends Activity {
 
 	private AdView adView;
 	private Boolean isPremium;
 
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
 		setContentView(R.layout.insertprofile_activity_layout);
 
 		adView = Util.loadAdmob(this);
 
 		bindAddButton();
 		bindBackButton();
 
 		isPremium = Boolean.parseBoolean(getString(R.string.isPremium));
 
 		// keyword limit disabled
 
 		if (!isPremium) {
 			// Util.setKeywordLimit(-1, (EditText) findViewById(R.id.editText2),
 			// getString(R.string.free_version_limit_5_keywords_per_website),
 			// getBaseContext());
 			// else{
 
 			int keywordLimit = Integer
 					.parseInt(getString(R.string.keywordLimit));
 
 			Util.setKeywordLimit(
 					keywordLimit,
 					(EditText) findViewById(R.id.editText2),
					getString(R.string.free_version_limit_7_keywords_per_website),
 					InsertWebsiteActivity.this);
 
 		}
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
 
 	private void bindAddButton() {
 
 		((Button) findViewById(R.id.button1))
 				.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 
 						// get
 						String inputSite = ((EditText) findViewById(R.id.editText1))
 								.getText().toString();
 						String keyword = ((EditText) findViewById(R.id.editText2))
 								.getText().toString();
 
 						// validate
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
 
 						// insert
 						int limit = Integer
 								.parseInt(getString(R.string.profileLimit));
 
 						if (!isPremium
 								&& new DbAdapter(getBaseContext())
 										.loadAllProfiles().size() >= limit) {
 
 							Premium.showBuyPremiumDialog(
 									getString(R.string.free_version_supports_only_)
 											+ limit
 											+ getString(R.string._websites_would_you_like_to_buy_the_premium_version_to_remove_this_limitation_),
 									InsertWebsiteActivity.this);
 
 							return;
 
 						}
 
 						if (new DbAdapter(getBaseContext()).insertOrUpdate(
 								inputSite, keyword, 0)) {
 							Toast.makeText(
 									getBaseContext(),
 									getString(R.string.website_added_successfuly),
 									Toast.LENGTH_SHORT).show();
 
 							startActivity(new Intent(getBaseContext(),
 									MainActivity.class));
 						} else {
 							Toast.makeText(getBaseContext(),
 									getString(R.string.website_add_failure),
 									Toast.LENGTH_SHORT).show();
 						}
 					}
 				});
 	}
 }
