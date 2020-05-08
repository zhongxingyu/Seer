 package de.thm.hcia.twofactorlockscreen;
 
 import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
 
 import de.thm.hcia.twofactorlockscreen.fragments.AboutFragment;
 import de.thm.hcia.twofactorlockscreen.fragments.MainFragment;
 import de.thm.hcia.twofactorlockscreen.fragments.MenuFragment;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.Toast;
 
 public class MainActivity extends SlidingFragmentActivity {
 
 	private static final String TAG = "MainActivity";
 	
 	public static final int REQ_CODE_CREATE_PATTERN = 1;
 	public static final int REQ_CODE_COMPARE_PATTERN = 2;
 	
 	private static Fragment 			mContent;
 	private static SharedPreferences 	mSettings;
 	SharedPreferences.Editor 			mPrefEditor; 
 	private static SlidingMenu 			mMenu;
 	public static CheckBox 				mDontShowAgain;
 	private static Context 				mContext;
 	private static String 				mAppVersion;
 	private static char[] 				savedPattern = null;
 		
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mContext = this;
 		setTitle(R.string.app_name);
 		setContentView(R.layout.responsive_content_frame);		
 		mSettings = getSharedPreferences("AppPrefs", MODE_PRIVATE);
 		mPrefEditor = mSettings.edit();
 		
		//try to get app version
 		try {
 			mAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 			mAppVersion = "1.0";
 		}
	
 		//check if the content frame contains the menu frame
 		if(findViewById(R.id.menu_frame) == null){
 			setBehindContentView(R.layout.menu_frame);
 			getSlidingMenu().setSlidingEnabled(true);
 			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 			// show home as up so we can toggle
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		}else{
 			/*
 			 *  add a dummy view
 			 *  disable SlidingMenu
 			 */
 			View v = new View(this);
 			setBehindContentView(v);
 			getSlidingMenu().setSlidingEnabled(false);
 			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
 		}
 		
 		
 		// set the Above View Fragment
 		if (savedInstanceState != null){
 			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
 		}		
 		if (mContent == null){
 			mContent = new MainFragment();	
 		}			
 		getSupportFragmentManager()
 		.beginTransaction()
 		.replace(R.id.content_frame, mContent)
 		.commit();
 		
 		// set the Behind View Fragment
 		getSupportFragmentManager()
 		.beginTransaction()
 		.replace(R.id.menu_frame, new MenuFragment())
 		.commit();
         
 		//set sliding menu
         prepareSlidingMenu();  
         
         //Show explanation dialog (one time)   
         if (savedInstanceState == null && mSettings.getBoolean("informationRead", false) == false){
        	AlertDialog.Builder adb = new AlertDialog.Builder(this);
         	
			LayoutInflater adbInflater = LayoutInflater.from(this);
			View checkBoxLayout = adbInflater.inflate(R.layout.checkbox, null);
			mDontShowAgain = (CheckBox) checkBoxLayout.findViewById(R.id.never_again);
         	
 			adb.setView(checkBoxLayout);
         	adb.setTitle(R.string.app_explanation_head);
 			adb.setMessage(R.string.app_explanation_txt);
 			adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {					
 					if(mDontShowAgain.isChecked()){					
 			        	mPrefEditor.putBoolean("informationRead", true);
 			            mPrefEditor.commit();
 					}					
 				}
 			})
 			.show();     	
         }      	
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.i(TAG, item.toString());
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle();
 			break;
 		case R.id.action_toggle_sliding_menu:
 			toggle();
 			break;
 		case R.id.action_about:
 			switchContent(new AboutFragment());
 			break;
 		case R.id.action_code_on_github:
 			//Open Browser
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/thomasbreitbach/hci_advanced/"));
 			startActivity(browserIntent);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * sets some menu preferences
 	 */
 	private void prepareSlidingMenu() {
 		mMenu = getSlidingMenu();
 		mMenu.setMode(SlidingMenu.LEFT);
 		mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 		mMenu.setShadowWidthRes(R.dimen.shadow_width);
 		mMenu.setShadowDrawable(R.drawable.shadow);
 		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
 		mMenu.setFadeDegree(0.35f);
 	}
 
 	/**
 	 * switches the fragment
 	 * @param fragment
 	 */
 	public void switchContent(final Fragment fragment) {
 		mContent = fragment;
 		getSupportFragmentManager()
 		.beginTransaction()
 		.replace(R.id.content_frame, fragment)
 		.commit();
 		Handler h = new Handler();
 		h.postDelayed(new Runnable() {
 			public void run() {
 				getSlidingMenu().showContent();
 			}
 		}, 50);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		Log.i(TAG, "onActivityResult");
 		switch(requestCode) {
 	    case REQ_CODE_CREATE_PATTERN:
 	        if(resultCode == RESULT_OK){
 	            savedPattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
 	            
	            //save pattern in prefs
 	        	mPrefEditor.putString("savedPattern", savedPattern.toString());
 	            if(mPrefEditor.commit()){
 	            	Toast.makeText(mContext, R.string.pattern_recorded, Toast.LENGTH_SHORT).show();
 	            }else{
	            	//TODO String-Ressource anlegen!
	            	Toast.makeText(mContext, "Konnte aus unbekannten Gründen nicht gespeichert werden", Toast.LENGTH_SHORT).show();
 	            }      
 	        }
 	        break;
 	    }
 	} 
 	
 	public static String getAppVersion(){
 		return mAppVersion;
 	}
 }
  
