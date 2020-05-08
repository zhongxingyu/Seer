 package com.utopia.lijiang;
 
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 
 import com.utopia.lijiang.alarm.Alarm;
 import com.utopia.lijiang.alarm.AlarmListener;
 import com.utopia.lijiang.alarm.AlarmManager;
 import com.utopia.lijiang.service.LocationService;
 import com.utopia.lijiang.widget.MenuBarLayout;
 import com.utopia.lijiang.widget.OnMenuBarSelectListener;
 
 public class MainActivity extends TabActivity {
 	
 	public final static int ADD_POSITION_TAB_INDEX = 0;
 	final static int ANIMATIION_DURATION = 450;	
 	static MainActivity instance = null;
 		
 	View lastView = null;
 	MenuBarLayout menuBar = null;
 	TabHost tabHost = null;
 	int currentTabId,lastTabId;
 	AlarmListener alarmListener = null;
 	AlarmManager alarmMgr = null;
 	AlertDialog alert = null;
 	
 	public static MainActivity getInstance(){
 		return instance;
 	}
 	
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.main_tab);
 	    
 	    instance = this;
 	    initialTabs();
 	    initialMenuBar();
 	    initialAlarmListener();
 	    
	    setCurrentTab(1);
 	}
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		//Should Hide last dialog first
 		if(alert !=null){
 			alert.cancel();
 		}
 		
 	   	showAlarmingAlarms();
 	}
 	
 	/*
 	 *  Tab
 	 */	
 	private void initialTabs(){
 		  tabHost = getTabHost();  
 		  addTab(this,LijiangMapActivity.class,"Position","postion");
 		  addTab(this,LijiangActivity.class,"Alarms","alarms");       
 		  tabHost.setOnTabChangedListener(new OnTabChangeListener(){
 				@Override
 				public void onTabChanged(String tabId) {
 					if(currentTabId > lastTabId){
 						MoveRightToLeft();
 					}else
 					{
 						MoveLeftToRigt();
 					}
 				}	
 		    });
 	} 
 	
 	private void initialMenuBar(){
 		  menuBar = (MenuBarLayout)this.findViewById(R.id.menubar);
 		  menuBar.setOnMenuBarSelectListener(new OnMenuBarSelectListener(){
 
 				@Override
 				public void onSelected(int index, View v) {
 					setCurrentTab(index);
 				}}); 
 	}
 	
 	public void setCurrentTab(int index){
 		   lastView = tabHost.getCurrentView();
 		   lastTabId = currentTabId;
 		   
 		   currentTabId = index;
 		   tabHost.setCurrentTab(index);
 		   menuBar.setButtonSelected(index, true);
 		   
 		   //Is first view
 		   if(lastView == null){
 			   lastView = tabHost.getCurrentView(); 
 		   }
 	}
 	
 	private void addTab(Context ctx, Class<?> cls,String indicator, String tag) {
 		Intent intent = new Intent().setClass(ctx, cls);
 		TabHost.TabSpec spec = tabHost.newTabSpec(tag).setIndicator(indicator).setContent(intent);
 	    tabHost.addTab(spec);
 	}
 	
 	//--------------------------
 	// Show alarming alarms
 	//--------------------------
 	
 	private void initialAlarmListener(){
 		alarmMgr = AlarmManager.getInstance();
     	alarmListener = new AlarmListener(){
 
 			@Override
 			public void onAlarm(Alarm[] alarms) {
 				showAlarmingAlarms();
 			}};
 			
     	alarmMgr.addAlarmListener(alarmListener);
 	}
 	
 	private void showAlarmingAlarms(){
 		if(alarmMgr.getAlarmingAlarms().size() < 1){
 			return;
 		}
 		
 		final Alarm alarm = alarmMgr.getAlarmingAlarms().get(0);
 		String posStr = getString(R.string.known);
 		@SuppressWarnings("unused")
 		String negStr = getString(R.string.no);
 		String msg = String.format(getString(R.string.locatinNearFormat), alarm.getTitle());
 			
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(msg)
 			      .setPositiveButton(posStr, new DialogInterface.OnClickListener() {
 			          public void onClick(DialogInterface dialog, int id) {
 			        	  inactiveAlarm(alarm);
 			        	  refreshStatus();	
 			        	  showAlarmingAlarms();
 			           }
 			       });
 			alert = builder.create();
 			alert.show();
 	}
 	
 	private void inactiveAlarm(Alarm alarm){
 		alarm.setActive(false);
    	  	alarmMgr.save2DB(MainActivity.this); 
 	}
 	
 	private void refreshStatus(){
 		LocationService.getLatestInstance().refreshAlarmNotification();
     	LijiangActivity.getLatestInstance().refreshList();
 	}
 	
 	/*
 	 * Pass the Back Press event to parent
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		showExitNotification();
 	}
 
 	private void showExitNotification(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.exit_warning))
 		       .setCancelable(true)
 		       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					MainActivity.this.finish();
 				}})
 				.setNegativeButton(getString(R.string.no),  new DialogInterface.OnClickListener(){
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}})
 				.create()
 				.show();
 	}
 	
 	/*
 	 * Animation 
 	 */	
 	private void MoveLeftToRigt(){
 		View currentView = tabHost.getCurrentView();
 		 lastView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.out_left_right));
 	     currentView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.in_left_right));
 	}
 	
 	private void MoveRightToLeft(){
 		View currentView = tabHost.getCurrentView();
 		 lastView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.out_right_left));
 	     currentView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.in_right_left));
 	}
 	
 }
