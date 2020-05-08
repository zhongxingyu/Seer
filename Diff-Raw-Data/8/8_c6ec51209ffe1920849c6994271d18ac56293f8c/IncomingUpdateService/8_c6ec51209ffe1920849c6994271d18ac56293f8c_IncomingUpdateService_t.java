 package com.hackathon.photohunt.services;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 
 import com.hackathon.photohunt.utility.LocationUtility;
 import com.hackathon.photohunt.utility.SmsUtility;
 
 public class IncomingUpdateService extends Service {
 	
 	public LocationUtility m_locUtil;
 	public String m_destPhoneNumber;
 	public String mName;
 	
 	@Override
 	public void onCreate() {
 
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int i1, int i2) {
 		Bundle extras = intent.getExtras();
 		m_destPhoneNumber = extras.getString("phoneNumber");
 		String destinationLocation = extras.getString("location");
 		mName = extras.getString("name");
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		Editor prefEdit = prefs.edit();
 		prefEdit.putString("destination", destinationLocation);
 		prefEdit.commit();
 		
 		m_locUtil = new LocationUtility(this);
		m_locUtil.setLocationListener(createNewLocationListener(), 1000 * 10);
 		return Service.START_STICKY;
 	}
     
 	public void inProximity(Location loc1, Location loc2)
 	{
 		if(loc1 == null)
 			return;
 		float[] results = new float[3];
 		Location.distanceBetween(loc1.getLatitude(), loc1.getLongitude(),
 				loc2.getLatitude(), loc2.getLongitude(), results);
 		if(results[0] <= 10)
 			this.stopSelf();
 	}
 	
     public LocationListener createNewLocationListener()
 	{
 		return new LocationListener() 
 		{
 		    public void onLocationChanged(Location location) 
 		    {
 		    	inProximity(m_locUtil.getCurrentLocationObject(), location);
 		    	String eta = "-1";
 		    	String loc = LocationUtility.convertLatLongToString(location.getLatitude(), location.getLongitude());
 		    	TelephonyManager mTelephonyMgr;
 		         mTelephonyMgr = (TelephonyManager)
 		                 getSystemService(Context.TELEPHONY_SERVICE); 
 		         String myNumber = mTelephonyMgr.getLine1Number();
 		    	SmsUtility.sendLocationUpdateText(m_destPhoneNumber, myNumber, loc, eta, mName);
 		    }
 
 		    public void onProviderEnabled(String provider) 
 		    {
 		    	
 		    }
 
 		    public void onProviderDisabled(String provider) 
 		    {
 		    	
 		    }
 
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras)
 			{
 				
 			}
 		};
 	}
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 }
