 package com.ov3rk1ll.pokedroiddex;
 
 import java.util.Arrays;
 import java.util.Locale;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
 import com.ov3rk1ll.pokedroiddex.DamageCalc.Type;
 import com.ov3rk1ll.pokedroiddex.db.DataSource;
 import com.ov3rk1ll.pokedroiddex.db.IconCursorAdapter;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 @SuppressLint("DefaultLocale")
 public class MainActivity extends SherlockListActivity {
 	public static String TAG = "PokeCounter";
 
 	
 	
 	public static Typeface pokemon_pixel_font;
 	
 	public static String language = null;
 	
 	private Type type1 = Type.NORMAL;
 	private Type type2 = null;
 	
 	private IconCursorAdapter suggestionsAdapter;
 	private Menu menu;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		updateGlobals(false);
 		
 		// Set custom font to title
 		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
 		if(titleId == 0)
 		    titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
 
 		TextView mAppName = (TextView) findViewById(titleId);
 		pokemon_pixel_font = Typeface.createFromAsset(getAssets(), "fonts/pokemon_pixel_font.ttf");
		mAppName.setTypeface(pokemon_pixel_font);
		mAppName.setTextSize(14);
 		
 		getSupportActionBar().setDisplayShowHomeEnabled(false);
 		
 		setContentView(R.layout.activity_main);	
 		((TextView)findViewById(R.id.textView1)).setTypeface(pokemon_pixel_font);
 		
 		Type[] selectedGeneration = DamageCalc.GENERATION[DataSource.getInstance().getGeneration() - 1];
 		
 		// Fill Spinner 1
 		TypeAdapter spinner1 = new TypeAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1);
 		for (Type type : selectedGeneration) {
 			int resId1 = getResources().getIdentifier("type_" + type.name().toLowerCase(), "string", getPackageName());
 			int resId2 = getResources().getIdentifier("type_" + type.name().toLowerCase(), "color", getPackageName());
 			spinner1.add(new TypeAdapter.Entry(getString(resId1), type, getResources().getColor(resId2)));
 		}
 			
 		((Spinner)findViewById(R.id.spinnerType1)).setAdapter(spinner1);
 		
 		// Fill Spinner 2
 		TypeAdapter spinner2 = new TypeAdapter(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1);	
 		spinner2.add(new TypeAdapter.Entry(getString(R.string.type_none), null, getResources().getColor(R.color.type_none)));
 		for (Type type : selectedGeneration) {
 			int resId1 = getResources().getIdentifier("type_" + type.name().toLowerCase(), "string", getPackageName());
 			int resId2 = getResources().getIdentifier("type_" + type.name().toLowerCase(), "color", getPackageName());
 			spinner2.add(new TypeAdapter.Entry(getString(resId1), type, getResources().getColor(resId2)));
 		}
 		((Spinner)findViewById(R.id.spinnerType2)).setAdapter(spinner2);
 		
 		// Set OnItemSelectedListener for Spinner 1
 		((Spinner)findViewById(R.id.spinnerType1)).setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
 				type1 = ((TypeAdapter.Entry)adapterView.getAdapter().getItem(position)).getType();
 				((TypeAdapter)((Spinner)findViewById(R.id.spinnerType2)).getAdapter()).setAllEnabled(true);
 				((TypeAdapter)((Spinner)findViewById(R.id.spinnerType2)).getAdapter()).getItem(position + 1).setEnabled(false);
 				
 				// If Spinner 1 is the same as Spinner 2
 				if(position == ((Spinner)findViewById(R.id.spinnerType2)).getSelectedItemPosition() - 1){
 					((Spinner)findViewById(R.id.spinnerType2)).setSelection(0);
 				}
 								
 				updateDamageData();	
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {}			
 		});
 		((Spinner)findViewById(R.id.spinnerType1)).setSelection(0);
 		
 		// Set OnItemSelectedListener for Spinner 2
 		((Spinner)findViewById(R.id.spinnerType2)).setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
 				type2 = ((TypeAdapter.Entry)adapterView.getAdapter().getItem(position)).getType();
 				updateDamageData();				
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {}			
 		});
 		((Spinner)findViewById(R.id.spinnerType2)).setSelection(0);
 	}
 	
 	public void updateDamageData(){
 		DamageTypeAdapter adapter = new DamageTypeAdapter(MainActivity.this,
 				R.layout.simple_list_item,
 				android.R.id.text1,
 				DamageCalc.getFactorList(type1, type2));
 		adapter.sort();
 		setListAdapter(adapter);
 	}
 	
 	public void updateGlobals(boolean refresh) {
 		language = Locale.getDefault().getLanguage();
 		SharedPreferences preferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
 				
 		if(preferences.getString("language", null) != null){
 			language = preferences.getString("language", null);
 		}
 		Locale locale = new Locale(language);
         Configuration config = new Configuration();
         config.locale = locale;
         getResources().updateConfiguration(config, null);
         
         if(refresh){
 			Intent intent = getIntent();
 			overridePendingTransition(0, 0);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			finish();
 
 			overridePendingTransition(0, 0);
 			startActivity(intent);
 
         }
         
         DataSource.init(this, locale.getLanguage(), Integer.parseInt(preferences.getString("generation", "6")));    
         DataSource.getInstance().open();
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		type1 = Type.values()[savedInstanceState.getInt("type1")];
 		if(savedInstanceState.getInt("type2") == -1)
 			type2 = null;
 		else
 			type2 = Type.values()[savedInstanceState.getInt("type2")];
 		int idx1 = ((TypeAdapter)((Spinner)findViewById(R.id.spinnerType1)).getAdapter()).find(type1);	
 		int idx2 = ((TypeAdapter)((Spinner)findViewById(R.id.spinnerType2)).getAdapter()).find(type2);	
 		((Spinner)findViewById(R.id.spinnerType1)).setSelection(idx1);
 		((Spinner)findViewById(R.id.spinnerType2)).setSelection(idx2);
 	}
 	
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt("type1", type1.ordinal());
 		outState.putInt("type2", type2 == null ? -1 : type2.ordinal());
 	}
 		
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		menu.clear();
 		getSupportMenuInflater().inflate(R.menu.main, menu);
 
 		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
 		
 		//Set up cursor		          
 	    String[] from = {"name", "_id"}; 
 	    int[] to = {android.R.id.text1, R.id.imageView1};
 	    suggestionsAdapter = new IconCursorAdapter(this, R.layout.simple_suggestion_item, DataSource.getInstance().getCursor(), from, to, 0);	    
 	    searchView.setSuggestionsAdapter(suggestionsAdapter);
 	    searchView.setOnSuggestionListener(new OnSuggestionListener() {			
 			@Override
 			public boolean onSuggestionSelect(int position) {
 				return false;
 			}
 			
 			@Override
 			public boolean onSuggestionClick(int position) {
 				Cursor cursor = (Cursor) suggestionsAdapter.getItem(position);
 				
 				int idx1 = ((TypeAdapter)((Spinner)findViewById(R.id.spinnerType1)).getAdapter()).find(cursor.getString(2));
 				int idx2 = ((TypeAdapter)((Spinner)findViewById(R.id.spinnerType2)).getAdapter()).find(cursor.getString(3));
 				
 				((Spinner)findViewById(R.id.spinnerType1)).setSelection(idx1);
 				((Spinner)findViewById(R.id.spinnerType2)).setSelection(idx2);
 
 				menu.findItem(R.id.menu_search).collapseActionView();
 				
 				return true;
 			}
 		});
 	    final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				if(newText.length() >= 2)
 					suggestionsAdapter.getFilter().filter(newText);
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				return true;
 			}
 		};
 		searchView.setOnQueryTextListener(queryTextListener);
 		
 		this.menu = menu;
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if(item.getItemId() == R.id.action_about){
 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			try {
 				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);			
 				dialog.setTitle(getString(R.string.app_name) + " v" + pInfo.versionName);
 			} catch (NameNotFoundException e) {
 				e.printStackTrace();
 			}
 			dialog.setMessage(Html.fromHtml(getString(R.string.about)));	
 			dialog.setPositiveButton(android.R.string.ok, null);
 			dialog.show();
 			
 			return true;
 		}else if(item.getItemId() == R.id.action_language){
 			showOptionDialog(R.string.action_language, R.array.language_values, R.array.language_entries, "language", null);
 		}else if(item.getItemId() == R.id.action_generation){
 			showOptionDialog(R.string.action_generation, R.array.generation_values, R.array.generation_entries, "generation", "6");
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	private void showOptionDialog(int titleId, final int valueId, int entryId, final String key, String defaultValue){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		int idx = Arrays
 				.asList(getResources().getStringArray(valueId))
 				.indexOf(getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(key, defaultValue));
 	    builder.setTitle(titleId)
 	           .setSingleChoiceItems(entryId, idx, new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int which) {
 	            	   dialog.cancel();
 	            	   String l = getResources().getStringArray(valueId)[which];
 	            	   SharedPreferences preferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
 	            	   preferences.edit().putString(key, l).commit();
 	            	   updateGlobals(true);
 	           }
 	    });
 	    builder.show();
 	}
 		
 	@Override
 	public boolean onSearchRequested() {
 		MenuItem mi = menu.findItem(R.id.menu_search);
 		if (mi != null) {
 			if (mi.isActionViewExpanded()) {
 				mi.collapseActionView();
 			} else {
 				mi.expandActionView();
 			}
 		}
 		return super.onSearchRequested();
 	}
 }
