 package com.ask.pws;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 public class PhoneWatchingService extends Service {
 
 	private List<Watcher> watchers = new ArrayList<Watcher>();
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		addWatcher(new SimWatcher());
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		// TODO Auto-generated method stub
 		super.onStart(intent, startId);
 
 		startWatching();
 	}
 
 	protected void startWatching() {
 
 		for (int i = 0; i < watchers.size(); i++) {
 			final Watcher watcher = watchers.get(i);
 			if (watcher != null) {
 				new Thread(new Runnable() {
					@Override
 					public void run() {
 						if (watcher.onWatch()) {
 							PhoneWatchingService.this.stopSelf();
 						}
 					}
 				}).start();
 			}
 		}
 	}
 
 	public void addWatcher(Watcher watcher) {
 		watchers.add(watcher);
 	}
 
 	public List<Watcher> getWatchers() {
 		return watchers;
 	}
 
 }
