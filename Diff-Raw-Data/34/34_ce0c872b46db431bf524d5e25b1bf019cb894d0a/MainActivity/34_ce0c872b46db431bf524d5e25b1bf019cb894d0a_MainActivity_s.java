 package se.fpt.ft;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Set;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnItemSelectedListener {
 	static String currentCity = null;
 	static boolean firstspinn = false;
 	private static final int RESULT_SETTINGS = 1;
 	Set<String> currentSettings = new HashSet<String>();
 	SharedPreferences sharedPreferences = null;
 	SharedPreferences.Editor sharedPreferencesEditor = null;
 	boolean backFromInbox = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		sharedPreferences = getPreferences(MODE_PRIVATE);
 		sharedPreferencesEditor = sharedPreferences.edit();
 		
 		currentCity = sharedPreferences.getString("currentCity", 
 				getResources().getStringArray(R.array.cities)[0]);
 		
 		// Restore saved settings
 		final String[] array = new String[sharedPreferences.getInt(
 				"lenghtofcurrentSettings", 0)];
 		for (int i = 0; i < array.length; i++) {
 			currentSettings.add(sharedPreferences.getString(String.valueOf(i), ""));
 		}
 		initSpinner();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(backFromInbox) {
 			backFromInbox = false;
 			moveTaskToBack(true);
 		}
 
 		if(PreferenceManager.getDefaultSharedPreferences(this)
 				.getBoolean("stealthmode", false)) {
 			findViewById(R.id.button1).performClick();
 		}
 	}
 
 	private void initSpinner() {
 		setContentView(getResources().getIdentifier(
 						currentCity.toLowerCase(Locale.US).replaceAll("Å", "A")
 						.replaceAll("Ä", "A").replaceAll("Ö", "O")
 						.replaceAll("å", "a").replaceAll("ä", "a")
 						.replaceAll("ö", "o"), "layout", "se.fpt.ft"));
 
 		final Spinner spinner = (Spinner) findViewById(R.id.spinner);
 		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
 				.createFromResource(this, R.array.cities,
 						android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setOnItemSelectedListener(MainActivity.this);
 		spinner.setAdapter(adapter);
 		String[] cities = getResources().getStringArray(R.array.cities);
 		for (int i = 0; i < cities.length; i++) {
 			if(cities[i].equals(currentCity)){
 				spinner.setSelection(i);
 			}
 		}
 
 		firstspinn = true;
 
 		if (!currentSettings.isEmpty()) {
 			final Iterator<String> iter = currentSettings.iterator();
 			while (iter.hasNext()) {
 				final String setting = iter.next();
 				if (setting.startsWith(currentCity + ":")) {
 					final String[] settings = setting.split(":");
 					final CompoundButton test = (CompoundButton) findViewById(Integer
 							.valueOf(settings[1]));
 					test.setChecked(true);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case RESULT_SETTINGS:
 			initSpinner();
 			break;
 		}
 	}
 
 	public void onButtonClick(View v) {
 		// Gogoogooo
 		Ticket.setCurrentSettings(currentSettings);
 		ClassLoader classLoader = MainActivity.class.getClassLoader();
 		try {
 	        classLoader.loadClass("se.fpt.ft." + currentCity).newInstance();
 	    } catch (ClassNotFoundException e) {
 	        e.printStackTrace();
 	    } catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
		if(Ticket.getError() != null) {
 			Toast.makeText(
 					this,
					Ticket.getError(), Toast.LENGTH_LONG)
 					.show();
 		}
 
 		final SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		final ContentValues contentInboxValues = Ticket.getContentInboxValues();
 		if (!(contentInboxValues.containsKey("address") && contentInboxValues
 				.containsKey("body"))) {
 			return;
 		}
 		
 		new Thread(new Runnable() {
 		    public void run() {
 				Uri sentSMS = null;	
 				Uri fakedSMS = null;
 				long sentDate = 0;
 				long fakedDate = 0;
 				Cursor cursor = null;
 				
 				if (sharedPrefs.getBoolean("smsout", false)) {
 					sentSMS = getContentResolver().insert(Uri.parse("content://sms/sent"),
 							Ticket.getContentOutboxValues());
 					cursor = getContentResolver().query(sentSMS, null, null, null, null);
 			    	cursor.moveToNext();
 			    	sentDate = cursor.getLong(4);
 				}
 				
 		    	try{ Thread.sleep(1000);}
 		    		catch(InterruptedException e){ }
 		    	fakedSMS = getContentResolver().insert(Uri.parse("content://sms/inbox"),
 						contentInboxValues);
 		    	cursor = getContentResolver().query(fakedSMS, null, null, null, null);
 		    	cursor.moveToNext();
 		    	fakedDate = cursor.getLong(4);
		    	
		    	if(sharedPrefs.getString("smsRemoveTime", "0") != "0" ) {
 		    		try{ Thread.sleep(Long.valueOf(sharedPrefs.getString("smsRemoveTime", "0"))); }
 		    		catch(InterruptedException e){ }
 			    	if(sentSMS != null) {
 			    		cursor = getContentResolver().query(sentSMS, null, null, null, null);
 				    	cursor.moveToNext();
				    	if(sentDate == cursor.getLong(4)) {
				    		getContentResolver().delete(sentSMS, null, null);
				    	}
 			    	}
 			    	cursor = getContentResolver().query(fakedSMS, null, null, null, null);
 			    	cursor.moveToNext();
			    	if(fakedDate == cursor.getLong(4)) {
			    		getContentResolver().delete(fakedSMS, null, null);
			    	}
 		    	}
 		    }
 		  }).start();
 		
 		final String exitTo = sharedPrefs.getString("exitsetting",
 				"exitToDesktop");
 		if (exitTo.contains("exitToDesktop")) {
 			moveTaskToBack(true);
 		} else if (exitTo.contains("exitToInbox")) {
 			backFromInbox = true;
 			final Intent smsIntent = new Intent(Intent.ACTION_MAIN);
 			smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
 			smsIntent.setType("vnd.android-dir/mms-sms");
 			startActivity(smsIntent);
 		}
 	}
 
 	public void onCheckboxClicked(View view) {
 		if (((CheckBox) view).isChecked()) {
 			currentSettings.add(currentCity + ":"
 					+ String.valueOf(view.getId()));
 		} else {
 			currentSettings.remove(currentCity + ":"
 					+ String.valueOf(view.getId()));
 		}
 	}
 
 	public void onCheckedChanged(View view) {
 		// Remove all values in selected radiogroup that match currentcity
 		final Iterator<String> iter = currentSettings.iterator();
 		while (iter.hasNext()) {
 			final String setting = iter.next();
 			final String[] settings = setting.split(":");
 			if (settings[0].equals(currentCity)) {
 				if (Integer.valueOf(settings[1]) == view.getId()) {
 					iter.remove();
 				}
 			}
 		}
 		currentSettings.add(currentCity + ":"
 				+ String.valueOf(view.getId()));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position,
 			long id) {
 		if (firstspinn) {
 			firstspinn = false;
 			return;
 		}
 		currentCity = getResources().getStringArray(R.array.cities)[position];
 		initSpinner();
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			final Intent i = new Intent(this, UserSettingActivity.class);
 			startActivity(i);
 
 			break;
 
 		}
 
 		return true;
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		sharedPreferencesEditor.putString("currentCity", currentCity);
 
 		// Store selectedCheckBoxes
 		final String[] array = currentSettings.toArray(new String[0]);
 		sharedPreferencesEditor.putInt("lenghtofcurrentSettings", array.length);
 		for (int i = 0; i < array.length; i++) {
 			sharedPreferencesEditor.putString(String.valueOf(i), array[i]);
 		}
 		sharedPreferencesEditor.commit();
 	}
 
 }
