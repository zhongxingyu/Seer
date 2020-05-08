 package inc.meh.MileageTracker;
 
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 
 
 
 
 public class Trip {
 
 	public Main Main;
 	private DAO dh;
 	int InsertStringTripId;
 	double dCumulativeDistance;
 	double dCumDist;
 
 
 	private double distance;
 	private Date startDate;
 	private Date endDate;
 	private String name;
 
 
 	public double getCumulativeTripDistance() {
 		return dCumDist;
 	}
 	
 	
 	public int getTripId() {
		//return InsertStringTripId;
		int InsertStringTripID = dh.getTripId();
		return InsertStringTripID;
 		
 	}
 	public void setInsertStringTripId(int insertStringTripId) {
 		InsertStringTripId = insertStringTripId;
 	}
 	
 	public double getDistance() {
 		return distance;
 	}
 	public void setDistance(double distance) {
 		this.distance = distance;
 	}
 	public Date getStartDate() {
 		return startDate;
 	}
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 	public Date getEndDate() {
 		return endDate;
 	}
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Trip(Context context) {
 	
 		// initialize the database
 		this.dh = new DAO(context);
 		//this.Main = new Main();
 		
 		
 	}
 
 	//Method to begin tracking mileage
 	protected void StartTrack(Location mLocation) {
 				
 		//get the trip id
 		InsertStringTripId = dh.getTripId();
 		
 		//this is a "start" activity (not "auto" or "stop")
 		String InsertStringInsertype = "Start";
 		
 		//get lat and long from location object
 		Double InsertStringLat = mLocation.getLatitude();
 		Double InsertStringLon = mLocation.getLongitude();
 
 		//insert first entry in Database for this trip
 		dh.insert(InsertStringTripId,InsertStringInsertype, InsertStringLat,
 				InsertStringLon, 0.0, 0.0);
 
 	}
 	//Method to stop tracking trip
 	protected void StopTrack(LocationManager mLocationManager, Criteria criteria, LocationListener mLocationListener) {
 		
 		
 		//this is a "stop" activity (not "auto" or "start")
 		String InsertStringInsertype = "Stop";
 	
 		// get last location in database
 		double[] dLastLocation = getLastLocation();
 	
 		// break out lat and long
 		double dStartLat = dLastLocation[0];
 		double dStartLong = dLastLocation[1];
 	
 		//initialize final results (returned as float)
 		float[] results = { 999f };
 	
 		//get best location provider
 		String locationprovider = mLocationManager.getBestProvider(criteria, true);
 		
 		//get last known location
 		Location mLocation = mLocationManager.getLastKnownLocation(locationprovider);
 	
 		//is location known?
 		if (mLocation != null) {
 			
 			InsertStringInsertype = "Stop";
 			
 			//get lat and long
 			Double InsertStringLat = mLocation.getLatitude();
 			Double InsertStringLon = mLocation.getLongitude();
 	
 			Double dist2Prev = 0.0;
 			Double cumDist = 0.0;
 	
 			// is the db empty?
 			if (dLastLocation != null) {
 	
 				//get cumulative distance
 				dCumDist = dLastLocation[3];
 	
 				if (dCumDist != 0) {
 					cumDist = dCumDist;
 				}
 				
 				// get distance between here and last record in database
 				android.location.Location.distanceBetween(dStartLat,
 						dStartLong, InsertStringLat, InsertStringLon,
 						results);
 	
 				//the distance to the previous location/breadcrumb
 				double dDist2Prev = results[0];
 	
 				dist2Prev = dDist2Prev;
 				
 				//increment cumulative distance with distance to previous
 				cumDist += dDist2Prev;
 				
 				mLocationManager.removeUpdates(mLocationListener);  // moved later in routine to allow for removal of requestLocationupdates
 			}
 			
 			
 			// show me the money!!!
 			android.location.Location.distanceBetween(dStartLat, dStartLong, InsertStringLat, InsertStringLon, results);
 	
 			//write to DAO
 			dh.insert(InsertStringTripId,InsertStringInsertype, InsertStringLat,
 					InsertStringLon, dist2Prev, cumDist);
 			
 			//update cumulative distance for UI
 			dCumDist=cumDist;
 	
 		} else {
 			//GPS is not working
 			
 		}
 	}
 	// helper method to get last location in db for breadcrumbs
 	private double[] getLastLocation() {
 		String sStartLat = "";
 		String sStartLong = "";
 		List<String> dbRow = dh.selectOneRow("");
 	
 		sStartLat = dbRow.get(1);// sFromDBArray[1];
 		sStartLong = dbRow.get(2);// sFromDBArray[2];
 	
 		String sDist2Prev = dbRow.get(3);// sFromDBArray[3];
 		String sCumDist = dbRow.get(4);// sFromDBArray[4];
 	
 		sStartLat = sStartLat.replace("'", "");
 		sStartLong = sStartLong.replace("'", "");
 		double dStartLat = Double.parseDouble(sStartLat);
 		double dStartLong = Double.parseDouble(sStartLong);
 	
 		double dDist2Prev = Double.parseDouble(sDist2Prev);
 		double dCumDist = Double.parseDouble(sCumDist);
 	
 		double[] dReturn = { dStartLat, dStartLong, dDist2Prev, dCumDist };
 		
 		return dReturn;
 	
 	}
 	// helper method to calculate distances
 	protected double[] CalculateDistance(double InsertStringLat, double InsertStringLon) {
 		// get last location in database
 		//double[] dLastLocation = {23,45,34,56,67,78};//getLastLocation();
 		double[] dLastLocation = getLastLocation();
 	
 		Double dist2Prev = 0.0;
 		Double dcumDist = 0.0;
 	
 		// is the db empty?
 		if (dLastLocation != null) {
 			// break out lat and long
 			double dStartLat = dLastLocation[0];
 			double dStartLong = dLastLocation[1];
 	
 			// double dDist2Prev = dLastLocation[3];
 			dcumDist = dLastLocation[3];
 	
 			float[] results = { 999f };
 			// get distance between here and last record in database
 			android.location.Location.distanceBetween(dStartLat,
 					dStartLong, InsertStringLat, InsertStringLon,
 					results);
 	
 			// float fDistanceBeween= results[0];
 	
 			double dDist2Prev = results[0];
 	
 			dist2Prev = dDist2Prev;
 			dcumDist += dDist2Prev;
 			
 		}  // end if (dLastLocation != null) 
 	
 		double[] dReturn = { dist2Prev, dcumDist };
 		return dReturn;
 	
 	}
 	
 
 	
 	
 
 }
