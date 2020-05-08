 package edu.ucla.cens.whatsnoisy.services;
 
import edu.ucla.cens.virtualworld.services.ILocationService;
import edu.ucla.cens.virtualworld.services.ILocationServiceCallback;
 import edu.ucla.cens.whatsnoisy.Settings;
 import edu.ucla.cens.whatsnoisy.services.LocationService;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class LocationService extends Service {
 
     private LocationManager lManager;
     private LocationListener lListener;
 	private static final String TAG = "LocationService";
 
     
     @Override
     public void onCreate() {
     	
         //start location listener
         lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    
     
         lListener = new LocationTraceListener();
         lManager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER, 
                 10000, 
                 0, 
                 lListener);
     }
     
     @Override
     public void onDestroy() {
     	lManager.removeUpdates(lListener);
     	Log.d(TAG,"Location Service Stopped");
     }    
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder;
 	}
 
 	private final RemoteCallbackList<ILocationServiceCallback> m_callbacks = new RemoteCallbackList<ILocationServiceCallback>();
 	
 	private final ILocationService.Stub binder = new ILocationService.Stub(){		
		@Override
 		public void registerCallback(ILocationServiceCallback cb)
 				throws RemoteException {
 			if (cb != null) {
 				m_callbacks.register(cb);
 			}
 		}
 
		@Override
 		public void unregisterCallback(ILocationServiceCallback cb)
 				throws RemoteException {
 			Log.d(TAG, "unregister callback");
 			if (cb != null) {
 				m_callbacks.unregister(cb);
 			}
 		}
 	};
 	
 	private void locationChanged(Location loc){
 		final int callbacks = m_callbacks.beginBroadcast();
 		
 		for (int i = 0; i < callbacks; i++) {
 			try {
 				m_callbacks.getBroadcastItem(i).locationUpdated(loc);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		m_callbacks.finishBroadcast();
 	}
 
 	
     /**
      * Implementation of LocationListener
      */
     private class LocationTraceListener implements LocationListener {
     	
         public void onLocationChanged(Location loc) {
             if (loc != null) 
             {
             	// Lets gather all the values
                 locationChanged(loc);
             }
         }//onLocationChanged
 
 		public void onProviderDisabled(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void onProviderEnabled(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 			// TODO Auto-generated method stub
 			
 		}
     }
 
 }
