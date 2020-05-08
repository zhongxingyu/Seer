 package com.NFCGeo;
 
 import java.sql.SQLException;
 import java.util.Scanner;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ViewPointActivity extends Activity {
 
 	private static final char DEGREE = 176;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Get the message from the intent
 		Intent intent = getIntent();
 		
 		String cacheName = intent.getStringExtra(ScanCacheActivity.CACHE_NAME);		
 		String cacheID = intent.getStringExtra(ScanCacheActivity.CACHE_ID);
 		boolean found = intent.getBooleanExtra(	ScanCacheActivity.CACHE_FOUND, false);
 		
 			
 		
 		Cache foundMe;
 		
 		try {
 			foundMe = Caching.GetID(cacheID);
 		} catch (Exception e) {
 			
 			foundMe = null;
 		}
 		
 		if (foundMe!=null) {
 
 			
 			// caches[i] = new Cache(ID, Title, Lat, Long, Creator, TimesFound,
 			// Rating);
 			// switch (Integer.parseInt(message))
 			// {
 			// case 1:
 			// cache = new Cache("01", "Title 1", 19240000, -99120000,
 			// "Creator 1");
 			// break;
 			// case 2:
 			// cache = new Cache("02", "Title 2", 35410000, 139460000,
 			// "Creator 2");
 			// break;
 			// case 3:
 			// cache = new Cache("03", "Title 3", 42026167, -93648040,
 			// "Creator 3");
 			// break;
 			// default:
 			// cache = new Cache("04", "", 0, 0, null);
 			// }
 			//
 			// Formats the latitude/longitude of the cache
 			
 			LinearLayout view = new LinearLayout(this);
 
 			TextView title = new TextView(this);
 			title.setTextSize(40);
 
 			
			
			String location = getLocation(foundMe.getLoc_lat(), foundMe.getLoc_long());
 			
 			String titleText = "";
 
 			if (found)
 				titleText = "Found Cache \"" + foundMe.getName() + "\" !";
 			else
 				titleText = foundMe.getName();
 
 			title.setText(titleText);
 
 			TextView creator = new TextView(this);
 			creator.setText("Created by: " + foundMe.getCreator());
 
 			TextView loc = new TextView(this);
 			loc.setText("Cache at: " + location);
 
 			TextView rating = new TextView(this);
 			rating.setText("Rating: " + foundMe.getRating() + " stars");
 
 			TextView timesFound = new TextView(this);
 			timesFound.setText("Times Found: " + foundMe.getTimesFound());
 
 			// Sets settings for the page layout
 			view.setOrientation(LinearLayout.VERTICAL);
 			view.setPadding(16, 0, 16, 0);
 
 			// Adds text views to the layout
 			view.addView(title);
 			view.addView(creator);
 			view.addView(loc);
 			view.addView(rating);
 			view.addView(timesFound);
 
 			setContentView(view);
 
 		} 
 		else{
 			
 			
 			LinearLayout view = new LinearLayout(this);
 
 			TextView title = new TextView(this);
 			TextView loc = new TextView(this);
 
 			title.setTextSize(40);
 			String titleText = "";
 			
 			// If the Cache is marked as found, the intent came from scanning the tag
 			// and there is location data available
 			if (found)
 			{
 				titleText = "Found Cache \"" + cacheName + "\" !";
 			}
 				
 			else
 			{
 				titleText = cacheName;
 			}
 			
 			String locationRecord = intent.getStringExtra(ScanCacheActivity.CACHE_LOCATION);
 			
 			String locationValues[] = locationRecord.split(",");
 
 			int lat = Integer.parseInt(locationValues[0]);	
 			int lon = Integer.parseInt(locationValues[1]);
 			
 			String creatorName = intent.getStringExtra(ScanCacheActivity.CACHE_CREATOR);
 			
			loc.setText("Cache at: " + getLocation(lat, lon) );
 			
 			title.setText(titleText);
 			
 			TextView creator = new TextView(this);
 			creator.setText("Created by: " + creatorName);
 			
 			TextView error = new TextView(this);
 			error.setText("** Database Error **");
 			
 			// Sets settings for the page layout
 			view.setOrientation(LinearLayout.VERTICAL);
 			view.setPadding(16, 0, 16, 0);
 			
 			view.addView(title);
 			view.addView(creator);
 			view.addView(loc);
 			view.addView(error);
 
 
 			setContentView(view);
 		}
 	}
 
	private String getLocation(int loc_lat, int loc_long)
 	{		
 
		double lat = Math.round( loc_lat / 1000.0) / 1000.0;
		double lon = Math.round( loc_long / 1000.0) / 1000.0;
		
 		String location;
		if (lat > 0)
 			location = "N";
 		else
 			location = "S";
 
		location += " " + Math.abs(lat) + DEGREE;
 
		if (lon > 0)
 			location += " E";
 		else
 			location += " W";
 
		location += " " + Math.abs(lon) + DEGREE;
 
 		return location;
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_view_point, menu);
 		return true;
 	}
 
 }
