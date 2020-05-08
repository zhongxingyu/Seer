 package dk.bigherman.android.hello;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.database.SQLException;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class PopulateMapArea extends AsyncTask<ArrayList<OverlayItem>, Void, ArrayList<OverlayItem>>
 {
 	private ProgressDialog dialog;
 	private MapView myMap;
 	private MapArea mapArea; 
 	//private ArrayList<OverlayItem> overlayItemList;
 	private HelloItemizedOverlay itemizedlist;
 	
 	public PopulateMapArea(MapView myMap, MapArea mapArea, HelloItemizedOverlay itemizedlist)
 	{
 		this.myMap = myMap;
 		this.mapArea = mapArea;
 		this.itemizedlist = itemizedlist;
 	}
 	
     @Override
     protected void onPreExecute()
     {
         dialog = new ProgressDialog(this.myMap.getContext());
         dialog.setTitle("Loading...");
         dialog.setMessage("Please wait...");
         dialog.setIndeterminate(true);
         dialog.show();
         
         myMap.getOverlays().clear();
        // mapOverlays.clear();
 
     }
 	
 	@Override
 	protected ArrayList<OverlayItem> doInBackground(ArrayList<OverlayItem>... args)
 	{
 		DataBaseHelper myDbHelper = new DataBaseHelper(this.myMap.getContext());
 		Log.i("airfields", "Start db load");
         try {
         	myDbHelper.createDataBase();
         	myDbHelper.openDataBase();
 	 	} catch (IOException ioe) {
 	 		throw new Error("Unable to create database");
 	 	} catch(SQLException sqle) {
 	 		throw sqle;
 	 	}
 	 
 	 	ArrayList<Airfield> airfields = myDbHelper.airfieldsInArea(
 	 			mapArea.getMinRow(),
 	 			mapArea.getMaxRow(),
 	 			mapArea.getMinCol(),
 	 			mapArea.getMaxCol()
 	 	);
         myDbHelper.close();
 		// db shit here
         
 		for (int i=0; i<airfields.size();i++) {
 			Log.i("airfields", "Next airfield call, ICAO=" + airfields.get(i).getIcaoCode());
 
			int lat = (int)Math.round(airfields.get(i).getLat());
			int lng = (int)Math.round(airfields.get(i).getLng());
 			Log.i("airfields", "lat=" + lat + ",long=" + lng);
 			String metar = "No metar available";
 			String stationName = airfields.get(i).getName();
 
			GeoPoint point = new GeoPoint((int)(lat*1E6),(int)(lng*1E6));
 		//	new GeoPoint()
 			OverlayItem overlayitem = new OverlayItem(point, stationName, metar);
 			//args[0].addOverlay(overlayitem);
 			args[0].add(overlayitem);
 		}
 
 		Log.i("PopulateMapArea", "Returning data");
 		
 		return args[0];
 	}
 	
 	protected void onPostExecute(ArrayList<OverlayItem> result)
 	{
 		Log.i("PopulateMapArea", "onPostExecute, result size=" + result.size());
 		/*for (OverlayItem item : result) {
 			this.itemizedlist.addOverlay(item);
 		}
 		this.myMap.postInvalidate();*/
 		
 		for (OverlayItem item : result) {
 			this.itemizedlist.addOverlay(item);
 		}
 		this.myMap.getOverlays().add(this.itemizedlist);
 		this.myMap.invalidate();
 		
         this.dialog.dismiss();
     }
 }
