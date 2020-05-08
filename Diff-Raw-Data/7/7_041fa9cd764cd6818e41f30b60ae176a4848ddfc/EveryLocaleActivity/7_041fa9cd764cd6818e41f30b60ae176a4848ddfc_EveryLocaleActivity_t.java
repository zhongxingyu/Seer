 /*
     Copyright (C) 2012 Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>
 
     This file is part of Every Locale.
 
     Every Locale is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 3 of the License, or
     (at your option) any later version.
 
     Every Locale is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Every Locale; if not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sweetiepiggy.everylocale;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableLayout;
import android.widget.Toast;
 
 public class EveryLocaleActivity extends Activity
 {
 	private static final String SOURCE_URL = "https://github.com/sweetiepiggy/Every-Locale";
 
 	private HashMap<String, String> language_map = new HashMap<String, String>();
 	private HashMap<String, String> country_map = new HashMap<String, String>();
 
 	/** true if country_map has not been updated since language has been changed */
 	private boolean m_need_rebuild_countries;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		init();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.settings:
 			Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.source:
 			intent = new Intent(Intent.ACTION_VIEW);
 			intent.setDataAndType(Uri.parse(SOURCE_URL), "text/html");
 			startActivity(Intent.createChooser(intent, null));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void init() {
 		Locale default_locale = Locale.getDefault();
 		String language_name = default_locale.getDisplayLanguage();
 		String country_name = default_locale.getDisplayCountry();
 		String variant_code = default_locale.getVariant();
 		m_need_rebuild_countries = false;
 
 		create_language_list();
 		create_country_list(default_locale.getLanguage());
 
 		/* request focus on table layout so autocomplete does not pop down */
 		((TableLayout) findViewById(R.id.table_layout)).requestFocus();
 
 		AutoCompleteTextView language_autocomplete = ((AutoCompleteTextView)findViewById(R.id.language_autocomplete));
 		language_autocomplete.setText(language_name);
 
 		AutoCompleteTextView country_autocomplete = ((AutoCompleteTextView)findViewById(R.id.country_autocomplete));
 		country_autocomplete.setText(country_name);
 
 		/* TODO: should getDisplayVariant be used to display variant name instead of code? */
 		((EditText)findViewById(R.id.variant_edittext)).setText(variant_code);
 
 		language_autocomplete.addTextChangedListener(new TextWatcher(){
 			public void afterTextChanged(Editable s) {
 				m_need_rebuild_countries = true;
 			}
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
 		});
 
 
 		country_autocomplete.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				/* rebuild country list if language has changed */
 				if (m_need_rebuild_countries) {
 					m_need_rebuild_countries = false;
 					String language = ((AutoCompleteTextView) findViewById(R.id.language_autocomplete)).getText().toString();
 					String language_code = language_map.containsKey(language.toLowerCase()) ?
 						language_map.get(language.toLowerCase()) : language;
 					create_country_list(language_code);
 				}
 				return v.onTouchEvent(event);
 			}
 		});
 
 		Button save_button = (Button)findViewById(R.id.save_button);
 		save_button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v)
 			{
 				save();
 			}
 		});
 
 		Button cancel_button = (Button)findViewById(R.id.cancel_button);
 		cancel_button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v)
 			{
 				cancel();
 			}
 		});
 	}
 
 	private void save()
 	{
 		String language = ((AutoCompleteTextView) findViewById(R.id.language_autocomplete)).getText().toString();
 		String country = ((AutoCompleteTextView) findViewById(R.id.country_autocomplete)).getText().toString();
 		String variant = ((EditText) findViewById(R.id.variant_edittext)).getText().toString();
 
 		String language_code;
 		String country_code;
 
 		boolean language_code_found = false;
 		boolean country_code_found = false;
 		String msg = "";
 
 		if (language_map.containsKey(language.toLowerCase())) {
 			language_code = language_map.get(language.toLowerCase());
 			language_code_found = true;
 		} else {
 			language_code = language;
 			msg += getResources().getString(R.string.unknown_language) +
 				": \"" + language + "\"";
 		}
 
 		if (country_map.containsKey(country.toLowerCase())) {
 			country_code = country_map.get(country.toLowerCase());
 			country_code_found = true;
 		} else {
 			country_code = country;
 			if (msg.length() != 0) {
 				msg += '\n';
 			}
 			msg += getResources().getString(R.string.unknown_country) +
 				": \"" + country + "\"";
 		}
 
 		if (!language_code_found || !country_code_found) {
 			prompt_continue(msg, language_code, country_code, variant);
 		} else {
 			update_configuration(language_code, country_code, variant);
 		}
 	}
 
 	private void prompt_continue(String msg, final String language_code,
 			final String country_code, final String variant)
 	{
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle(getResources().getString(android.R.string.dialog_alert_title));
 		alert.setMessage(msg);
 		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				update_configuration(language_code, country_code, variant);
 			}
 		});
 		alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		});
 		alert.show();
 	}
 
 	private void update_configuration(String language_code, String country_code, String variant)
 	{
 		try {
 			Class ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
 			Class IActivityManager = Class.forName("android.app.IActivityManager");
 
 			Method getDefault =  ActivityManagerNative.getMethod("getDefault", null);
 			Object am = IActivityManager.cast(getDefault.invoke(ActivityManagerNative, null));
 
 			Method getConfiguration = am.getClass().getMethod("getConfiguration", null);
 
 			Configuration config = (Configuration) getConfiguration.invoke(am, null);
 			Locale locale = new Locale(language_code, country_code, variant);
 			Locale.setDefault(locale);
 			config.locale = locale;
 
 			Class[] args = new Class[1];
 			args[0] = Configuration.class;
 			Method updateConfiguration = am.getClass().getMethod("updateConfiguration", args);
 			updateConfiguration.invoke(am, config);
 
 			init();
 		} catch (Exception e) {
			Toast.makeText(this,
					(new Error(e)).getMessage(),
					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void cancel()
 	{
 		Locale default_locale = Locale.getDefault();
 		String language_name = default_locale.getDisplayLanguage();
 		String country_name = default_locale.getDisplayCountry();
 		String variant_code = default_locale.getVariant();
 
 		/* request focus on table layout so autocomplete does not pop down */
 		((TableLayout) findViewById(R.id.table_layout)).requestFocus();
 
 		AutoCompleteTextView language_autocomplete = ((AutoCompleteTextView)findViewById(R.id.language_autocomplete));
 		language_autocomplete.setText(language_name);
 
 		AutoCompleteTextView country_autocomplete = ((AutoCompleteTextView)findViewById(R.id.country_autocomplete));
 		country_autocomplete.setText(country_name);
 
 		((EditText)findViewById(R.id.variant_edittext)).setText(variant_code);
 	}
 
 	private void create_language_list() {
 		language_map.clear();
 
 		ArrayAdapter<String> language_names = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
 
 		for (String language_code : Locale.getISOLanguages()) {
 			Locale locale = new Locale(language_code);
 			String local_name = locale.getDisplayLanguage();
 			String native_name = locale.getDisplayLanguage(locale);
 
 			language_names.add(language_code);
 			language_map.put(language_code.toLowerCase(), language_code);
 
 			if (!local_name.equals(language_code)) {
 				language_names.add(local_name);
 				language_map.put(local_name.toLowerCase(), language_code);
 			}
 
 			if (!local_name.equals(native_name)) {
 				language_names.add(native_name);
 				language_map.put(native_name.toLowerCase(), language_code);
 			}
 		}
 
 		AutoCompleteTextView languageTextView = (AutoCompleteTextView) findViewById(R.id.language_autocomplete);
 		languageTextView.setAdapter(language_names);
 	}
 
 	private void create_country_list(String lang) {
 		country_map.clear();
 
 		ArrayAdapter<String> country_names = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
 
 		for (String country_code : Locale.getISOCountries()) {
 			Locale locale = new Locale(lang, country_code);
 			String local_name = locale.getDisplayCountry();
 			String native_name = locale.getDisplayCountry(locale);
 
 			country_names.add(country_code);
 			country_map.put(country_code.toLowerCase(), country_code);
 
 			if (!local_name.equals(country_code)) {
 				country_names.add(local_name);
 				country_map.put(local_name.toLowerCase(), country_code);
 			}
 
 			country_map.put(local_name.toLowerCase(), country_code);
 			if (!local_name.equals(native_name)) {
 				country_names.add(native_name);
 				country_map.put(native_name.toLowerCase(), country_code);
 			}
 		}
 
 		AutoCompleteTextView countryTextView = (AutoCompleteTextView) findViewById(R.id.country_autocomplete);
 		countryTextView.setAdapter(country_names);
 	}
 }
 
