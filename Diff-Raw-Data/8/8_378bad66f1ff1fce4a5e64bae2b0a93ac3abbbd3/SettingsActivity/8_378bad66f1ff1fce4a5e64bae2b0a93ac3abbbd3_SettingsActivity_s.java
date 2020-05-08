 package org.gotext;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.gotext.logic.Service;
 import org.gotext.logic.goTextApplication;
 
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class SettingsActivity extends Activity implements OnItemSelectedListener{
 	Spinner spinner, spinnerHidden;
 	TextView textViewHidden;
 	EditText edit;
 	Button next,apply;
 	ArrayAdapter<CharSequence> adapter;
 	String lang = "";
 	goTextApplication ga = null;
 	SharedPreferences settings;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		ga = (goTextApplication)getApplication();
 		settings = ga.getSettingsPref();
 
 		buildUi();
 
 	}
 	
 
 	
     public void onClick(View view) {
       	 switch (view.getId()) {
       	 case R.id.next_settings:
       		
      		//if(settings.getBoolean("firststart", true)) {
      			settings.edit().putString("lang", lang).commit();
 	     		settings.edit().putBoolean("firststart", false).commit();
 	     		settings.edit().putString("int_prefix", edit.getText().toString()).commit();
 	      		Intent intention = new Intent(getApplicationContext(), NetworkActivity.class);
 	            startActivity(intention);
      		/*} else {
      			
      			settings.edit().putString("lang", lang).commit();
      			if (spinnerHidden.getSelectedItemPosition()!=0){
      				settings.edit().putString("default_service", (String)spinnerHidden.getSelectedItem());
      			}
      			setResult(0);
      			finish();
      		}*/
                break;
       	 case R.id.apply_settings:
       		 
       		settings.edit().putString("lang", lang).commit();
      		settings.edit().putString("int_prefix", edit.getText().toString()).commit();
  			if (spinnerHidden.getSelectedItemPosition()!=0){
  				String str = spinnerHidden.getSelectedItem().toString();
  				settings.edit().putString("default_service", str);
  			}
  			setResult(0);
  			finish();
       		 
 
       	 }
       }
 	
 	
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3) {
 			Log.i("SettingsActivity","Selezionato :"+spinner.getSelectedItemPosition());
 			int language = spinner.getSelectedItemPosition();
 			
 			if (language == 0 && !lang.equals("en")){
 				Log.i("SettingsActivity","English");
 				Locale locale = Locale.ENGLISH;
 				Locale.setDefault(locale);
 				Configuration config = new Configuration();
 				config.locale = locale;
 				onConfigurationChanged(config);
 			
 			}else if (language == 1  && !lang.equals("it")){
 				Log.i("SettingsActivity","ITALIANO");
 				Locale locale = Locale.ITALIAN;
 				Locale.setDefault(locale);
 				Configuration config = new Configuration();
 				config.locale = locale;
 				onConfigurationChanged(config);
 			
 			}
 			
 			
 			
 		
 	}
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	  // refresh your views here
 	
 	  super.onConfigurationChanged(newConfig);
 	  getBaseContext().getResources().updateConfiguration(newConfig,
 		      getBaseContext().getResources().getDisplayMetrics());
 	  //setContentView(R.layout.settings);
 	  Log.i(this.getLocalClassName(),"new config");
 	  buildUi();
	  setTitle(getString(R.id.menu_settings));
 
 
 
 	}
 	
 	
 	private void buildUi(){
 		
 		setContentView(R.layout.settings);
 		spinner = (Spinner)findViewById(R.id.spinner_lang);
 		edit = (EditText)findViewById(R.id.editPrefix);
 		next = (Button)findViewById(R.id.next_settings);
 		apply = (Button)findViewById(R.id.apply_settings);
 		
 		adapter = ArrayAdapter.createFromResource(this,
 		        R.array.lang_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		spinner.setOnItemSelectedListener(this);
 		
 		lang = Locale.getDefault().getLanguage();
 		int value = 0;
 		String prefix = "+1";
 		if (lang.equals("en")){
 			value = 0;
 			if (Locale.getDefault().getDisplayCountry().equals("US")){
 				prefix = "+1";
 			} else if (Locale.getDefault().getDisplayCountry().equals("GB")){
 				prefix = "+44";
 			}
 			
 		} else if (lang.equals("it")){
 			value = 1;
 			prefix = "+39";
 			
 		} else if (lang.equals("es")){
 			value = 2;
 		} else if (lang.equals("id")){
 			value = 3;
 		} else if (lang.equals("tr")){
 			value = 4;
 		}
 		
 		spinner.setSelection(value);
 		edit.setText(prefix);
 		
 		if (ga.getServiceCount()>0){
 			textViewHidden = (TextView)findViewById(R.id.textViewServ);
 			spinnerHidden = (Spinner)findViewById(R.id.spinnerServ);
 			
 			textViewHidden.setVisibility(View.VISIBLE);
 			spinnerHidden.setVisibility(View.VISIBLE);
 			
 			ArrayAdapter <CharSequence> adapter =
 					  new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
 					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			
 			ArrayList<Service> services = ga.getServices();
 			String ds = settings.getString("default_service", "");
 			boolean hasDefault = !ds.equals("");
 			adapter.add(getString(R.string.none));
 			int position = 0;
 			for (int i = 0; i< services.size(); i++){
 				String uid = services.get(i).getUid();
 				adapter.add(uid);
 				if (hasDefault && uid.equals(ds)){
 					position = i;
 				}
 			}
 			
 			spinnerHidden.setAdapter(adapter);
 			spinnerHidden.setSelection(position);
 			
 			
 
 		}
 		
 		if(!settings.getBoolean("firststart", true)){
 			next.setVisibility(View.INVISIBLE);
 			apply.setVisibility(View.VISIBLE);
 		}
 		
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    if(resultCode == 0) {
 	        finish();
 	    }
 	}
 
 }
