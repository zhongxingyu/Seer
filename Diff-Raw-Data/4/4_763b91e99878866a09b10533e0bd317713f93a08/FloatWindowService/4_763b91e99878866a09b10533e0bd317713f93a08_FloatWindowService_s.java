 package org.xdty.smilehelper;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class FloatWindowService extends Service {
 
 	/**
 	 * 用于在线程中创建或移除悬浮窗。
 	 */
 	private Handler handler = new Handler();
 
 	/**
 	 * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
 	 */
 	private Timer timer;
 	
 	/**
 	 * 数据库存储
 	 */
 	private DatabaseHelper mDatabaseHelper;
 	
 	/**
 	 * 数据库信息保存到数组中
 	 */
 	private static ArrayList<String> mAppList;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 	    
 	    // 获取数据库列表，保存到mAppList中
 	    mDatabaseHelper = new DatabaseHelper(getApplicationContext());
 	    Cursor cursor = mDatabaseHelper.selectForChecked(true);
 	    mAppList = new ArrayList<String>();
 	    if (cursor.getCount()==0) {
 	        mAppList.add("null");
 	    } else {
 	        while (cursor.moveToNext()) {
 	            mAppList.add(cursor.getString(2));
 	        }
 	    }
 	    
 		// 开启定时器，每隔0.5秒刷新一次
 		if (timer == null) {
 			timer = new Timer();
 			timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
 		}
 		new RefreshTask();
 		return super.onStartCommand(intent, flags, startId);
 	}
 	
 	/**
 	 * 获取当前界面顶部的Acivity名称
 	 * @return 返回完整的类名
 	 */
 	private String getTopAppName() {
 	    ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
         return rti.get(0).topActivity.getClassName();
 	}
 	
 	/**
      * 获取当前界面顶部的Acivity包名称
      * @return 返回包名
      */
     private String getTopPackageName() {
         ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
         return rti.get(0).topActivity.getPackageName();
     }
 	
 	/**
 	 * 判断是否为被添加进appList
 	 */
 	private boolean isInList() {
 	    return mAppList.isEmpty() ? false : mAppList.contains(getTopAppName());
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		// Service被终止的同时也停止定时器继续运行
 		timer.cancel();
 		timer = null;
 	}
 
 	class RefreshTask extends TimerTask {
 
 		@Override
 		public void run() {
 		    
 			if (!isHome() && !MyWindowManager.isWindowShowing() && !isClose()) {
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 					    if (MyWindowManager.getAddState()) {
 					        MyWindowManager.createAddWindow(getApplicationContext());
 					    } else if (isInList()) {
 					        MyWindowManager.createSmallWindow(getApplicationContext());
 					    }
 					}
 				});
 			}
 			
 			else if ((!isInList()||isHome()) && MyWindowManager.isWindowShowing()) {
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 						MyWindowManager.removeSmallWindow(getApplicationContext());
 						MyWindowManager.removeBigWindow(getApplicationContext());
						MyWindowManager.removeAddWindow(getApplicationContext());
 					}
 				});
 			}
 			//闪动效果
 //			else {
 //			    MyWindowManager.removeSmallWindow(getApplicationContext());
 //                MyWindowManager.removeBigWindow(getApplicationContext());
 //                MyWindowManager.removeAddWindow(getApplicationContext());
 //			}
 //			// 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
 //			else if (isHome() && MyWindowManager.isWindowShowing()) {
 //				handler.post(new Runnable() {
 //					@Override
 //					public void run() {
 //						MyWindowManager.updateUsedPercent(getApplicationContext());
 //					}
 //				});
 //			}
 		}
 
 	}
 	
 	private boolean isClose() {
 	    return FloatWindowSmallView.close;
 	}
 
 	/**
 	 * 判断当前界面是否是桌面
 	 */
 	private boolean isHome() {
 		return getHomes().contains(getTopPackageName());
 	}
 
 	/**
 	 * 获得属于桌面的应用的应用包名称
 	 * 
 	 * @return 返回包含所有包名的字符串列表
 	 */
 	private List<String> getHomes() {
 		List<String> names = new ArrayList<String>();
 		PackageManager packageManager = this.getPackageManager();
 		Intent intent = new Intent(Intent.ACTION_MAIN);
 		intent.addCategory(Intent.CATEGORY_HOME);
 		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
 				PackageManager.MATCH_DEFAULT_ONLY);
 		for (ResolveInfo ri : resolveInfo) {
 			names.add(ri.activityInfo.packageName);
 		}
 		return names;
 	}
 }
