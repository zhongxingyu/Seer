 package com.t2.vas.activity;
 
 import com.t2.vas.R;
 import com.t2.vas.activity.editor.GroupListActivity;
 import com.t2.vas.activity.preference.MainPreferenceActivity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class BaseActivity extends Activity {
 	private static final String TAG = BaseActivity.class.getName();
 	public static final int GROUP_EDITOR = 4325;
 	public static final int SETTINGS = 4326;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		//this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
 		super.onCreate(savedInstanceState);
 	}
 	
 	/*@Override
 	protected void onPause() {
 		this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
 		super.onPause();
 	}*/
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		
 		if(this.getHelp() == 0) {
 			menu.removeItem(R.id.help);
 		}
 		
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch(item.getItemId()){
 			case R.id.settings:
 				i = new Intent(this, MainPreferenceActivity.class);
 				this.startActivityForResult(i, SETTINGS);
 				return true;
 				
 			case R.id.help:
 				i = new Intent(this, HelpActivity.class);
 				i.putExtra("string_resource_id", this.getHelp());
 				this.startActivity(i);
 				return true;
 		}
 		
 		return super.onContextItemSelected(item);
 	}
 	
 	public int getHelp() {
 		return 0;
 	}
 }
