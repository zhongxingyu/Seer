 package com.lsg.app;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 public class VPlan extends Activity implements ViewPager.OnPageChangeListener {
 	private ProgressDialog loading;
 	private VPlanViewPagerAdapter adapter;
 	private ViewPager pager;
 
 	final Handler handler = new Handler() {
         public void handleMessage(Message msg) {
         	if(msg.arg1 == 1) {
         		loading.cancel();
         		adapter.updateCursor();
         	}
         	if(msg.arg1 == 2) {
         		loading.cancel();
         	}
         	if(msg.arg1 == 3) {
         		loading.cancel();
     			loading = ProgressDialog.show(VPlan.this, "", getString(msg.arg2), true);
         	}
         }
     };
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Functions.testDB(this);
         
 		Functions.setTheme(false, true, this);
 		getWindow().setBackgroundDrawableResource(R.layout.background);
         
 		if(Functions.getSDK() >= 11) {
 			try {
 				AdvancedWrapper actHelper = new AdvancedWrapper();
 				actHelper.dropDownNav(this);
 				} catch (Exception e) {
 					Log.d("error", e.getMessage());
 					}
 		}
 	    setContentView(R.layout.viewpager);
 	    adapter = new VPlanViewPagerAdapter(this);
 	    pager = (ViewPager)findViewById( R.id.viewpager );
 	    pager.setOnPageChangeListener(this);
 	    /*TitlePageIndicator indicator =
 	        (TitlePageIndicator)findViewById( R.id.indicator );*/
 	    pager.setAdapter(adapter);
 	    //indicator.setViewPager( pager );
 	}
 	@Override
 	public void onResume() {
 		//updateCursor(mine);
 		super.onResume();
 	}
 	public void updateVP() {
 		loading = ProgressDialog.show(VPlan.this, "", getString(R.string.loading_vertretungen), true);
 		UpdateBroadcastReceiver.ProgressThread progress = new UpdateBroadcastReceiver.ProgressThread(handler, this);
 		progress.start();
 		}
 	public void setPage(int page) {
 		pager.setCurrentItem(page);
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.vplan, menu);
 	    if(Functions.getSDK() >= 11) {
 	    	AdvancedWrapper ahelp = new AdvancedWrapper();
 	    	ahelp.searchBar(menu, adapter);
 	    }
 	    else
 	    	menu.removeItem(R.id.search);
 	    return true;
 	}
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.removeItem(R.id.all);
 		menu.removeItem(R.id.mine);
 	    if(Functions.getSDK() < 11) {
 	    	if(pager.getCurrentItem() == 0)
 	    		menu.add(0, R.id.all, Menu.NONE, R.string.all);
 	    	else
 	    		menu.add(0, R.id.mine, Menu.NONE, R.string.mine);
 	    }
 		return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.settings:
 	    	Intent settings;
 	    	if(Functions.getSDK() >= 11)
 	    		settings = new Intent(this, SettingsAdvanced.class);
 	    	else
 	    		settings = new Intent(this, Settings.class);
 	    	startActivity(settings);
 	        return true;
 	    case R.id.refresh:
 	    	updateVP();
 	    	return true;
 	    case R.id.mine:
 	    	setPage(0);
 	    	return true;
 	    case R.id.all:
 	    	setPage(1);
 	    	return true;
 	    case R.id.subjects:
             Intent subjects = new Intent(this, SubjectList.class);
             startActivity(subjects);
 	    	return true;
 	    case R.id.info:
 	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    	builder.setMessage(getString(R.string.number_all) + " " + new Integer(adapter.c.getCount()).toString() + "\n"
 	    			+ getString(R.string.number_mine) + " " + new Integer(adapter.second_c.getCount()).toString() + "\n"
 	    			+ getString(R.string.actdate) + prefs.getString("date", "") + " / " + prefs.getString("time", ""))
 	    	       .setCancelable(true)
 	    	       .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 	    	           public void onClick(DialogInterface dialog, int id) {
 	    	                dialog.cancel();
 	    	           }
 	    	       });
 	    	AlertDialog alert = builder.create();
 	    	alert.show();
 	    	return true;
         case android.R.id.home:
             // app icon in action bar clicked; go home
             Intent intent = new Intent(this, lsgapp.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	public void onPageScrollStateChanged (int state) {}
 	public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {}
 	public void onPageSelected (int position) {
 		if(Functions.getSDK() >= 11) {
 			AdvancedWrapper adv = new AdvancedWrapper();
 			adv.selectedItem(position, this);
 			}
 	}
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		Functions.createContextMenu(menu, v, menuInfo, this, Functions.DB_VPLAN_TABLE);
 	}
 	@Override
 	public boolean onContextItemSelected(final MenuItem item) {
 		return Functions.contextMenuSelect(item, this, adapter, Functions.DB_VPLAN_TABLE);
 	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		adapter.closeCursorsDB();
	}
 }
