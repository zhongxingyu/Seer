 package me.kennydude.transtimetable.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.kennydude.transtimetable.ITransService;
 import me.kennydude.transtimetable.Station;
 import me.kennydude.transtimetable.Utils;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.os.IBinder;
 
 /**
  * I am an activity that deals with transit information via IPC
  * 
  * @author kennydude
  *
  */
 public abstract class TransitActivity extends Activity {
 	
 	public abstract static class LocateStationHelperCallback<T extends Object>{
 		public abstract List<T> getStations( TransServiceBinder tsb, ComponentName cn );
 		public abstract void finishLocation( List<T> results );
 		@SuppressWarnings("unchecked")
 		public void fLocation(List<Object> r){
 			finishLocation((List<T>) r);
 		}
 	}
 	
 	static int at = 0;
 	
 	/**
 	 * I help you to query all of the providers for data
 	 */
 	public static void locateStationHelper(final Activity a, final LocateStationHelperCallback<?> cback){
 		final List<Object> results = new ArrayList<Object>();
 		
 		PackageManager pm = a.getPackageManager();
 		final Intent intent = new Intent(Utils.TRANSIT_SERVICE_ACTION);
 		final List<ResolveInfo> ril = pm.queryIntentServices(intent, 0);
 		
 		final ServiceConnection mConnection = new ServiceConnection(){
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public void onServiceConnected(ComponentName cn, IBinder binder) {
				List<Object> b = (List<Object>) cback.getStations( new TransServiceBinder( ITransService.Stub.asInterface( binder ) ), cn );
 				if(b != null)
 					results.addAll( b );
 				
 				System.out.println(at + " done. total results " + results.size());
 				
 				a.unbindService(this);
 				// Now bind onto the next one!
 				at += 1;
 				if(at < ril.size()){
 					intent.setClassName(ril.get(at).serviceInfo.packageName, ril.get(at).serviceInfo.name);
 					a.bindService(intent, this, Context.BIND_AUTO_CREATE);
 				} else{
 					cback.fLocation(results);
 				}
 			}
 
 			@Override public void onServiceDisconnected(ComponentName arg0) {}
 			
 		};
 		
 		at = 0;
 		if(ril.size() == 0){
 			cback.fLocation(results);
 			return;
 		}
 		intent.setClassName(ril.get(at).serviceInfo.packageName, ril.get(at).serviceInfo.name);
 		a.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 	
 	boolean mBound = false;
 	TransServiceBinder mTransService;
 	
 	public abstract void onConnectedToBackendService();
 	public TransServiceBinder getTransService(){
 		if(mBound){
 			return mTransService;
 		} return null;
 	}
 	
 	@Override
 	public void onCreate(Bundle bis){
 		super.onCreate(bis);
 		
 		Intent i = new Intent();
 		i.setClassName(getIntent().getStringExtra("pname"), getIntent().getStringExtra("class"));
 		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if(mBound){
 			try{
 				unbindService(mConnection);
 			} catch(Exception e){}
 			mBound = false;
 		}
 	}
 	
 	private ServiceConnection mConnection = new ServiceConnection(){
 
 		@Override
 		public void onServiceConnected(ComponentName arg0, IBinder binder) {
 			mBound = true;
			mTransService = new TransServiceBinder(ITransService.Stub.asInterface( binder ) );
 			onConnectedToBackendService();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 		
 	};
 }
