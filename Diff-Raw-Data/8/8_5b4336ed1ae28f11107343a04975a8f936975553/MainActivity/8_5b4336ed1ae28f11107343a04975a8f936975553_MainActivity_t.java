 //liuchong@baixing.com
 package com.baixing.activity;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 
 import com.baixing.activity.SplashJob.JobDoneListener;
 import com.baixing.broadcast.PushMessageService;
 import com.baixing.broadcast.push.PageJumper;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.imageCache.ImageLoaderManager;
 import com.baixing.tracking.Sender;
 import com.baixing.tracking.TrackConfig;
 import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
 import com.baixing.tracking.Tracker;
 import com.baixing.util.LocationService;
 import com.baixing.util.PerformEvent.Event;
 import com.baixing.util.PerformanceTracker;
 import com.baixing.util.Util;
 import com.baixing.view.fragment.HomeFragment;
 import com.quanleimu.activity.R;
 import com.umeng.update.UmengUpdateAgent;
 
 //import com.tencent.mm.sdk.openapi.BaseReq;
 //import com.tencent.mm.sdk.openapi.BaseResp;
 //import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
 public class MainActivity extends BaseTabActivity implements /*IWXAPIEventHandler,*/ JobDoneListener {
 	
 	private List<Runnable> pendingTask;
 	
 //	private boolean isRestoring;
 	private SplashJob splashJob;
 	private List<Runnable> resumeTask = new ArrayList<Runnable>(); 
 	
 	public MainActivity(){
 		super();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 	
 	protected void onNewIntent(Intent intent)
 	{
 		super.onNewIntent(intent);
 		
 		if (Intent.ACTION_MAIN.equals(intent.getAction()) && GlobalDataManager.getInstance().getLastActiveClass() != null) {
 //			resumeTask.add(new Runnable() {
 //				public void run() {
 //					deprecatSelect(TAB_INDEX_CAT);
 //				}
 //			});
 			Intent go = new Intent();
 //			go.addCategory(Intent.CATEGORY_LAUNCHER);
 			go.setClassName(this, GlobalDataManager.getInstance().getLastActiveClass().getName());
 			go.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			startActivity(go);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 
 		if (!this.isChangingTab) {
 			Log.d("ddd","onpause");
 			
 			Tracker.getInstance().event(BxEvent.APP_PAUSE).end();
 		}
 		
 		super.onPause();
 		
 //		
 //		MobclickAgent.onPause(this);
 //		
 //		Log.d("Umeng SDK API call", "onPause() called from QuanleimuMainActivity:onPause()!!");
 	}
 	
 	@Override
 	protected void onResume() {
 		if (!this.isChangingTab) {
 			Log.d("ddd", "onresume");
 			
 			Tracker.getInstance().event(BxEvent.APP_RESUME).end();
 		}
 		
 //		Profiler.markStart("mainresume");
 		bundle.putString("backPageName", "");
 		super.onResume();
 		BaseFragment bf = this.getCurrentFragment();
 		if(bf == null && splashJob == null){
 			splashJob = new SplashJob(this, this);
 		}
 		if (splashJob != null && !splashJob.isJobDone())
 		{
 			splashJob.doSplashWork();
 		}
 		else
 		{
 			jumpToPage();
 			responseOnResume();
 		}
 		
 		for (Runnable task : resumeTask) {
 			task.run();
 		}
 		resumeTask.clear();
 		
 //		Profiler.markEnd("mainresume");
 		
 //		MobclickAgent.onResume(this);
 //		
 //		Log.d("Umeng SDK API call", "onResume() called from QuanleimuMainActivity:onResume()!!");
 	} 
 	
 	
 	
 	
 	@Override
 	protected void onStop() {
 		
 		if (!this.isChangingTab) {
 			Log.d("ddd","onstop");
 			
 			Tracker.getInstance().event(BxEvent.APP_STOP).end();
 			Tracker.getInstance().save();
 			Sender.getInstance().notifySendMutex();
 		}
 		
 		super.onStop();
 	}
 	
 	protected void onDestory()
 	{
 		LocationService.getInstance().stop();
 		super.onDestroy();
 	}
 
 	private void responseOnResume()
 	{
 	}
 	
 	static public final String WX_APP_ID = "wx862b30c868401dbc";
 //	static public final String WX_APP_ID = "wx47a12013685c6d3b";//debug
 //	static public final String WX_APP_ID = "wxc54c9e29fcd6993d";////burizado, baixingwang2
 	
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		Log.d("quanleimu", "onSaveInstanceState");
 
 		// BxSender & BxTracker save data into file.
 		try {
 			Sender.getInstance().save();
 			Tracker.getInstance().save();
 		} catch (Exception e) {
 		}
 		
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		Log.d("quanleimu", "onRestoreInstanceState");
 //		// BxSender & BxTracker save data into file.
 //		try {
 //			Tracker.getInstance().load();
 //		} catch (Exception e) {
 //		}
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public void onJobDone() {
 		PerformanceTracker.stamp(Event.E_Handle_Jobdone);
 		//Start server when application is start.
 		Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
 		startPush.putExtra("updateToken", true);
 		this.startService(startPush);
 		
 		//Update UI after splash.
 		initTitleAction();
 		
 		if(!GlobalDataManager.update){
 			GlobalDataManager.update = true;
             //去掉幽梦旧sdk 更新调用
 		}
 		PerformanceTracker.stamp(Event.E_Start_HomeFragment);
 		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
 		{
 			this.pushFragment(new HomeFragment(), bundle, true);
			Tracker.getInstance().event(BxEvent.APP_START).append(TrackConfig.TrackMobile.Key.CITY, GlobalDataManager.getInstance().getCityEnglishName()).end();
			Tracker.getInstance().save();
			Sender.getInstance().notifySendMutex();

 		}
 		else
 		{
 			this.notifyStackTop();
 		}
 		
 		responseOnResume();
 		
 		this.splashJob = null; //Remove splash job reference.
 		if (pendingTask != null && pendingTask.size() > 0)
 		{
 			for (Runnable task : pendingTask)
 			{
 				task.run();
 			}
 		}
 		((new Thread(new Runnable(){
 			@Override
 			public void run(){
 				new UpdateCityAndCatCommand(MainActivity.this).execute();
 			}
 		}))).start();
 		
 //		Toast.makeText(this, Profiler.dump(), Toast.LENGTH_LONG).show();
 //		Profiler.clear();
 		jumpToPage();
 		
 		PerformanceTracker.stamp(Event.E_Handle_Jobdone_End);
 	}
 	
 	private void jumpToPage(){
 		Intent intent = this.getIntent();
 		if(intent != null){ //FIXME FIXME: need to check if the push have bad effects.
 			if(intent.getBooleanExtra("pagejump", false)){
 				Bundle data = intent.getExtras();
 				PageJumper.jumpToPage(this, data.getString("page"), data.getString("data"));
 				///intent.getExtras()
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 //		Profiler.markStart("maincreate");
 //		Debug.startMethodTracing();
 		PerformanceTracker.stamp(Event.E_MainActivity_Begin_Create);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
 		super.onCreate(savedInstanceState);
 //		ImageLoaderManager.initImageLoader();
 		GlobalDataManager.context = new WeakReference<Context>(this);
 //		Intent pushIntent = new Intent(this, com.baixing.broadcast.BXNotificationService.class);
 //		this.stopService(pushIntent);
 		
 //		Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
 //		startPush.putExtra("updateToken", true);
 //		this.startService(startPush);
 		pendingTask = new ArrayList<Runnable>();
 		
 		setContentView(R.layout.main_activity);
 		onSetRootView(this.findViewById(R.id.root));
 		
 		findViewById(R.id.splash_cover).setVisibility(savedInstanceState == null ? View.VISIBLE : View.GONE);
 		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
 		
 		splashJob = new SplashJob(this, this);
 		
 //		isRestoring = savedInstanceState != null;
 //		Profiler.markEnd("maincreate");
 
 		byte[] checkFlgData = Util.loadData(this, "umeng_update_check_flg");
         String checkFlg = checkFlgData == null ? null : new String(checkFlgData);//(String)Util.loadDataFromLocate(this, "umeng_update_check_flg", String.class);
         String todayFlg = String.valueOf(new Date().getDate());
         if (checkFlg == null || checkFlg.equals( todayFlg ) == false) {
             UmengUpdateAgent.update(this);
 //            Util.saveDataToLocate(this,"umeng_update_check_flg", todayFlg);
             Util.saveDataToFile(this, null, "umeng_update_check_flg", todayFlg.getBytes());
         }
 
 
 //        if (MobileConfig.getInstance().isUseUmengUpdate()) {
 //            UmengUpdateAgent.update(this);
 //        }
         PerformanceTracker.stamp(Event.E_MainActivity_End_Create);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
 	}
 	
 	@Override
 	protected void onStart() {
 		if (!this.isChangingTab) {
 			Log.d("ddd","onstart");
 			
 		}
 		
 		super.onStart();
 	}
 	
 	protected boolean handleIntent()
 	{
 		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0)
 		{
 			setIntent(intent);
 		}
 		return false;
 	}
 	
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
         	handleBack();
         }
         
         else{
         	return super.onKeyDown(keyCode, event);
         }
         
         return true;
     }
 	
 }
