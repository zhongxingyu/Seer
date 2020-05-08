 package com.gurkensalat.android.xingsync;
 
 import java.util.List;
 
 import org.androidannotations.annotations.Bean;
 import org.androidannotations.annotations.Click;
 import org.androidannotations.annotations.EActivity;
 import org.androidannotations.annotations.ViewById;
 import org.androidannotations.annotations.sharedpreferences.Pref;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.View;
 import android.widget.TextView;
 
 import com.gurkensalat.android.xingsync.api.ContactsCall;
 import com.gurkensalat.android.xingsync.api.MeCall;
 import com.gurkensalat.android.xingsync.api.User;
import com.gurkensalat.android.xingsync.preferences.SyncPrefs_;
 import com.gurkensalat.android.xingsync.sync.AccountAuthenticatorService;
 
 @EActivity
 public class HelloAndroidActivity extends Activity
 {
 	private static Logger LOG = LoggerFactory.getLogger(HelloAndroidActivity.class);
 
 	@ViewById(R.id.oauth_api_call_result)
 	TextView oauth_api_call_result;
 
 	@Bean
 	ContactsCall contactsCall;
 
 	@Bean
 	MeCall meCall;
 
 	@Pref
 	SyncPrefs_ syncPrefs;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		LOG.info("onCreate");
 		super.onCreate(savedInstanceState);
 
 		AccountAuthenticatorService.listAccounts(getApplicationContext());
 
 		if (AccountAuthenticatorService.hasAccount(getApplicationContext()))
 		{
 			LOG.info("We have our own account");
 
 			Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 
 			// To show only our own accounts
 			// intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
 			// getResources().getString(R.string.ACCOUNT_TYPE) });
 
 			startActivity(intent);
 		}
 		else
 		{
 			LOG.info("No, no account of appropiate type");
 			// setContentView(R.layout.main);
 
 			Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 
 			startActivity(intent);
 		}
 	}
 
 	@Click(R.id.btn_perform_contacts_call)
 	void performContactsCall(View clickedView)
 	{
 		LOG.info("About to call 'contacts' api method");
 		if (contactsCall != null)
 		{
 			String text = "Call 'Contacts'";
 			text = text + "\n";
 
 			JSONObject json = contactsCall.perform();
 			displayResults(text, json);
 
 			List<User> users = contactsCall.parse(json);
 			if (users != null)
 			{
 				displayAdditionalString(users.toString());
 			}
 		}
 	}
 
 	@Click(R.id.btn_perform_me_call)
 	void performMeCall(View clickedView)
 	{
 		LOG.info("About to call 'me' api method");
 		if (meCall != null)
 		{
 
 			String text = "Call 'Me'";
 			text = text + "\n";
 
 			JSONObject json = meCall.perform();
 			displayResults(text, json);
 
 			User user = meCall.performAndParse();
 			if (user != null)
 			{
 				displayAdditionalString(user.toString());
 			}
 		}
 	}
 
 	@Click(R.id.btn_clear_credentials)
 	void clearCredentials(View clickedView)
 	{
 		LOG.info("About to clear credentials");
 		syncPrefs.edit().oauth_token().put("").apply();
 		syncPrefs.edit().oauth_token_secret().put("").apply();
 	}
 
 	private void displayResults(String text, JSONObject json)
 	{
 		if (json != null)
 		{
 			text = text + json.toString();
 			text = text + "\n";
 		}
 
 		oauth_api_call_result.setText(text);
 	}
 
 	private void displayAdditionalString(String additionalText)
 	{
 		if (additionalText != null)
 		{
 			oauth_api_call_result.setText(oauth_api_call_result.getText() + "\n" + additionalText);
 		}
 	}
 }
