 package com.km2team.syriush.service;
 
 import android.content.Context;
 import android.location.Location;
 import android.widget.Toast;
 
 import com.km2team.syriush.database.*;
 public class LoggingLocationListener extends SyriushLocationListener {
 
 	
 	Location lastLocation, savedLocation;
 	
 	Database db;
 	Context context;
 	
 	public LoggingLocationListener(Context context) {
 		this.context = context;
 		db = DatabaseFactory.getDatabase(context);
 	}
 	
 	@Override
 	public void onLocationChanged(Location arg0) {
 	
 		/* TODO it's wrong - getTime() is unpredictable */
 		if (Math.abs(arg0.getTime() - lastLocation.getTime()) > 1000) {
 			/* TODO save point, with lower priority */
 			/* XXX XXX XXX XXX */
			db.newPoint((int)arg0.getLatitude(), (int)arg0.getLongitude(), (int)arg0.getAccuracy(), 1);
 			
 		}
 		
 		lastLocation = arg0;
 		
 	}
 	
 	public void savePoint(String note) {
 		/* TODO save point with a note */
		db.newPoint(note, (int)lastLocation.getLatitude(), (int)lastLocation.getLongitude(), (int)lastLocation.getAccuracy(), 1);
 		Toast.makeText(context, "saved checkpoint" , Toast.LENGTH_LONG).show();
 	}
 	
 	public void savePoint() {
 		/* save point and wait for a note */
 		savedLocation = lastLocation;
 	}
 	
 	public void addNote(String note) {
 		/* TODO add note to saved location */
		db.newPoint(note, (int)savedLocation.getLatitude(), (int)savedLocation.getLongitude(), (int)savedLocation.getAccuracy(), 1);
 		Toast.makeText(context, "saved checkpoint" , Toast.LENGTH_LONG).show();
 	}
 	
 
 }
