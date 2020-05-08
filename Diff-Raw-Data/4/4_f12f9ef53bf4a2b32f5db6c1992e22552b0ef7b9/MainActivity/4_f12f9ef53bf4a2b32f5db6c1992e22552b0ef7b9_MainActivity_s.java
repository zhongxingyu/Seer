 package com.bjerva.tsplex;
 
 import org.json.JSONArray;
 
 import android.app.ProgressDialog;
 import android.os.Bundle;
import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 
 public class MainActivity extends FragmentActivity {
 	//private final String jsonURL = "https://teckensprak.zanmato.se/signs.json";
 	private final String jsonURL = "http://130.237.171.46/signs.json?changed_at=2012-03-27";
 	public SignModel[] signs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_sign_listing);
 
 		if (savedInstanceState == null){
			Fragment newFragment = new SignListFragment();
 			getSupportFragmentManager().beginTransaction().add(
 					R.id.fragment_container, newFragment).commit();
 		}
 
 		//Show loading spinner
 		ProgressDialog pbarDialog = new ProgressDialog(this);
 		pbarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		pbarDialog.setMessage("Loading signs...");
 		pbarDialog.setCancelable(false);
 		pbarDialog.show();
 
 		//Load all sign meta data
 		loadSigns();
 
 		//Hide loading spinner
 		pbarDialog.hide();
 
 		// for(SignModel sign: signs) Log.i("SL", sign.toString());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_sign_listing, menu);
 		return true;
 	}
 
 	private void loadSigns(){
 		try {
 			//Load sign meta data
 			JSONArray json = new JSONParser().execute(jsonURL).get();
 
 			//Input all meta data in SignModel array
 			signs = new SignModel[json.length()];
 			for(int i=0; i<json.length(); ++i) signs[i] = new SignModel(json.getJSONObject(i));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
