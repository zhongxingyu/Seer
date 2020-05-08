 package kimononet.geo;
 
 import java.util.Date;
 
 import kimononet.net.parcel.Parcel;
 import kimononet.net.parcel.Parcelable;
 
 /**
  * Object stores location in terms of longitude, latitude, and accuracy as 
  * well as keeps track of last update time. 
  *  
  * @author Zorayr Khalapyan
  * @author Wade Norris
  * @since 2/9/2012
  * @version 3/16/2012
  *
  */
 public class GeoLocation implements Parcelable {
 
 	/**
 	 * Indicates the number of bytes in a location parcel.
 	 * @see #getParcelSize()
 	 */
 	public static final int PARCEL_SIZE = 24;
 	
 	/**
 	 * The median radius of the Earth in meters.
 	 */
 	public static final int EARTH_MEDIAN_RADIUS = 6371000;
 	
 	/**
 	 * Stores the longitude of the current GPS location.
 	 * @see #getLongitude()
 	 */
 	private double longitude;
 	
 	/**
 	 * Stores the latitude of the current GPS location. 
 	 * @see #getLatitude()
 	 */
 	private double latitude;
 	
 	/**
 	 * Stores the accuracy of the current GPS location.
 	 * @see #getAccuracy()
 	 */
 	private float accuracy;
 	
 	/**
 	 * Stores the UNIX time of last GPS location update in seconds.
 	 * @see #getTimestamp()
 	 */
 	private int timestamp;
 
 	
 	/**
 	 * Creates a new GPS location with the specified longitude, latitude, and
 	 * accuracy. Timestamp will be set to the current System's timestamp. 
 	 * 
 	 * @param longitude Longitude of the current location.
 	 * @param latitude Latitude of the current location.
 	 * @param accuracy Accuracy (in feet) of the current geo location.
 	 */
 	public GeoLocation(double longitude, double latitude, float accuracy){
 		setLocation(longitude, latitude, accuracy);
 		this.timestamp = (int)(System.currentTimeMillis() / 1000);
 	}
 	
 	/**
 	 * Creates a new GPS location with the specified longitude, latitude,
 	 * accuracy, and timestamp.
 	 * 
 	 * @param longitude Longitude of the current location.
 	 * @param latitude Latitude of the current location.
 	 * @param accuracy Accuracy (in feet) of the current geo location.
 	 * @param timestamp Timestamp when the location was fetched.
 	 */
 	public GeoLocation(double longitude, double latitude, float accuracy, int timestamp){
 		setLocation(longitude, latitude, accuracy);
 		this.timestamp = timestamp;
 	}
 	
 	/**
 	 * Creates a new location utilizing a parcel.
 	 * 
 	 * @param parcel Parcel constructed according to protocol specification.
 	 */
 	public GeoLocation(Parcel parcel){
 		setLocation(parcel);
 	}
 	
 	/**
 	 * Sets GPS longitude, latitude, and accuracy. Timestamp does not change 
 	 * after setting a location. 
 	 * 
 	 * @param longitude Longitude to set.
 	 * @param latitude Latitude to set.
 	 * @param accuracy Accuracy to set.
 	 */
 	public void setLocation(double longitude, double latitude, float accuracy){
/*		if (longitude < -180d || longitude > 180d)
 			throw new GeoLocationException("Longitude must be within the range [-180, 180].");
 		else if (latitude < -90d || latitude > 90d)
 			throw new GeoLocationException("Latitude must be within the range [-90, 90].");
		else */{
 			this.longitude = longitude;
 			this.latitude = latitude;
 			this.accuracy = accuracy;
 		}
 	}
 	
 	/**
 	 * Parses a location from a parcel. 
 	 * 
 	 * @see {@link #toByteArray()}
 	 */
 	public void setLocation(Parcel parcel){
 		
 		this.longitude = parcel.getDouble();
 		this.latitude  = parcel.getDouble();
 		this.accuracy  = parcel.getFloat();
 		this.timestamp = parcel.getInt();	
 	}
 	
 	/**
 	 * @return A parcel representation of the current GPS location.
 	 */
 	public Parcel toParcel(){
 		
 		Parcel parcel = new Parcel(24);
 		
 		parcel.add(this.getLongitude());
 		parcel.add(this.getLatitude());
 		parcel.add(this.getAccuracy());
 		parcel.add(this.timestamp);
 		
 		return parcel;
 	}	
 	
 	/**
 	 * Sets the current location values according to the values stored in the 
 	 * specified parcel.
 	 * @param parcel The parcel to parse.
 	 * @see #setLocation(Parcel)
 	 */
 	public void parse(Parcel parcel){
 		this.setLocation(parcel);
 	}
 	
 
 	/**
 	 * Returns the UNIX time stamp of the last location update.
 	 * @return The UNIX time stamp of the last location update.
 	 * @see #getTimestamp()
 	 */
 	public int getTimestamp(){
 		return this.timestamp;
 	}
 
 	/**
 	 * Sets the current timestamp.
 	 * @param timestamp The timestamp for this location.
 	 * @see #GeoLocation(double, double, float, int)
 	 * @see #getTimestamp()
 	 */
 	public void setTimestamp(int timestamp){
 		this.timestamp = timestamp;
 	}
 	
 	/**
 	 * Returns current GPS longitude.
 	 * @return Current GPS longitude.
 	 */
 	public double getLongitude() {
 		return longitude;
 	}
 	
 	/**
 	 * Returns current GPS latitude.
 	 * @return Current GPS latitude.
 	 */
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	/**
 	 * Returns current GPS accuracy.
 	 * @return Current GPS accuracy.
 	 */
 	public float getAccuracy() {
 		return accuracy;
 	}
 	
 	/**
 	 * Returns the current parcel size.
 	 * @return Parcel size.
 	 * @see #PARCEL_SIZE
 	 */
 	@Override
 	public int getParcelSize(){
 		return PARCEL_SIZE;
 	}
 	
 	/**
 	 * Returns a string representation of the current location. The string will
 	 * contain the longitude, latitude, accuracy, and time stamp separated by 
 	 * tab characters. 
 	 * 
 	 * @return A string representation of the current location.
 	 */
 	@Override
 	public String toString(){
 		
 		return "Longitude: " + getLongitude() + "\t" + 
 		       "Latitude: " + getLatitude() + "\t" +
 		       "Accuracy: " + getAccuracy() + "\t" +
 
 		       //Date is created with milliseconds; hence, the conversion. 
 		       "Timestamp: " + new Date(timestamp * 1000);
 	}
 	
 	/**
 	 * Given a velocity and a current time, the method updates the current 
 	 * location after moving the current location by v*t.
 	 * 
 	 * All calculation are done according to the formulas specified at 
 	 * <a href="http://www.movable-type.co.uk/scripts/latlong.html">
 	 * Calculate distance, bearing and more between Latitude/Longitude points
 	 * </a>
 	 * 
 	 * 
 	 * @param velocity The current velocity at which the location is changing.
 	 * 
 	 * @param currentTime The current time. This value will be compared with the
 	 *                    timestamp of the current location in order to compute
 	 *                    change in time and hence, change in distance.
 	 *
 	 */
 	public void move(GeoVelocity velocity, int currentTime){
 		
 		
 		//Assuming that currentTime > timestamp, then calculate the distance 
 		//traveled as the product of speed in m/s time change in time in 
 		//seconds.
 		double distance = velocity.getSpeed() * (currentTime - timestamp);
 		
 		//Calculate the new latitude value provided the equation below.
 		//lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(θ))
 		double latitude = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(getLatitude()))*Math.cos(distance/EARTH_MEDIAN_RADIUS) + 
                           Math.cos(Math.toRadians(getLatitude()))*Math.sin(distance/EARTH_MEDIAN_RADIUS)*Math.cos(velocity.getBearing())));
 		
 		//Calculate the new longitude value provided the equation below.
 		//lon2 = lon1 + atan2(sin(θ)*sin(d/R)*cos(lat1), cos(d/R)−sin(lat1)*sin(lat2))
 		double longitude = Math.toDegrees(Math.toRadians(getLongitude()) + Math.atan2(Math.sin(velocity.getBearing())*Math.sin(distance/EARTH_MEDIAN_RADIUS)*Math.cos(Math.toRadians(getLatitude())), 
                            Math.cos(distance/EARTH_MEDIAN_RADIUS)-Math.sin(Math.toRadians(getLatitude()))*Math.sin(Math.toRadians(latitude))));
 		
 		
 		this.longitude = longitude;
 		this.latitude = latitude;
 		this.timestamp = currentTime;
 		
 	}
 
 	/**
 	 * Uses Haversine formula to accurately calculate the distance to another 
 	 * GeoLocation.
 	 * 
 	 * @param loc2 GeoLocation of second location to use in calculating distance.
 	 * @return Returns double precision float with distance to other GeoLocation
 	 */
 	public double distanceTo(GeoLocation loc2)
 	{
 		double lat2 = loc2.getLatitude();
 		double lon2 = loc2.getLongitude();
 		double lat1 = this.getLatitude();
 		double lon1 = this.getLongitude();
 		
 		double dLat = Math.toRadians(lat2-lat1);
 		double dLon = Math.toRadians(lon2-lon1);
 		lat1 = Math.toRadians(lat1);
 		lat2 = Math.toRadians(lat2);
 
 		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
 		        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
 		double d = EARTH_MEDIAN_RADIUS * c;
 		
 		return d;
 	}
 
 	/**
 	 * Calculates the bearing from one GeoLocation to another.
 	 * @param loc2 GeoLocation of second location to calculate bearing to.
 	 * @return Returns single precision float with bearing between this location
 	 * and given other location in radians.
 	 */
 	public float bearingTo(GeoLocation loc2)
 	{
 		double dLon = Math.toRadians(loc2.getLongitude()-this.getLongitude());
 		double lat1 = Math.toRadians(this.getLatitude());
 		double lat2 = Math.toRadians(loc2.getLatitude());
 
 		double y = Math.sin(dLon) * Math.cos(lat2);
 		double x = Math.cos(lat1)*Math.sin(lat2) -
 		        Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
 
 		float brng = (float) Math.atan2(y, x);
 
 		return brng;
 	}
 	
 	/**
 	 * Generates a random location bounded by the upper left corner and the 
 	 * bottom right corner. Accuracy will be set to the average accuracy of the
 	 * specified two corner locations.
 	 *  
 	 * @param upperLeftLocation Location of the upper left location.
 	 * @param lowerRightLocation Location of the lower right corner.
 	 * 
 	 * @return A random location bounded by the upper left corner and the 
 	 * bottom right corner.
 	 */
 	public static GeoLocation generateRandomGeoLocation(GeoLocation upperLeftLocation, 
 														GeoLocation lowerRightLocation){
 		
 		
 		//Calculate the random longitude and latitude values.
 		double longitude = upperLeftLocation.longitude + (lowerRightLocation.longitude - upperLeftLocation.longitude) * Math.random();
 		double latitude = upperLeftLocation.latitude + (lowerRightLocation.latitude - upperLeftLocation.latitude) * Math.random();
 		
 		
 		return new GeoLocation(longitude, latitude, (upperLeftLocation.accuracy + lowerRightLocation.accuracy) / 2);
 		
 	}
 	
 	/**
 	 * Returns a random location bounded by the provided map.
 	 * @param map The generated random map will be bounded by this value.
 	 * 
 	 * @return A randomly generated location within the bounded map.
 	 * @see #generateRandomGeoLocation(GeoLocation, GeoLocation)
 	 */
 	public static GeoLocation generateRandomGeoLocation(GeoMap map){
 		return GeoLocation.generateRandomGeoLocation(map.getUpperLeft(), map.getLowerRight());
 	}
 	
 	/**
 	 * Returns a random bearing value between 0 and 2PI.
 	 * @return A random bearing value between 0 and 2PI.
 	 */
 	public static float generateRandomBearing(){
 		return (float)(Math.random() * Math.PI * 2);
 	}
 
 }
