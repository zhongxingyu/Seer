 package kimononet.geo;
 
 import java.util.Date;
 
 import kimononet.net.parcel.Parcel;
 import kimononet.net.parcel.Parcelable;
 
 /**
  * Object stores geolocation in terms of longitude, latitude, and accuracy as 
  * well as keeps track of last update time. Please see below for an example:
  * 
  * <pre>
  * GeoLocation location1 = new GeoLocation(1.0, 2.0, 3.0);		
  * GeoLocation location2 = new GeoLocation(4.0, 5.0, 6.0);
  *
  * System.out.println(location2);
  * 
  * //Convert location1 to a byte array representation. 
  * byte[] location1Bytes = location1.toByteArray()
  * 
  * //Set location from a byte array.
  * location2.setLocation(location1Bytes);
  * 
  * System.out.println(location2);
  * </pre>
  *  
  * @author Zorayr Khalapyan
  * @since 2/9/2012
  *
  */
 public class GeoLocation implements Parcelable {
 
 	/**
 	 * Indicates the number of bytes in a location parcel.
 	 */
 	public static final int PARCEL_SIZE = 24;
 	
 	/**
 	 * Stores the longitude of the current GPS location.
 	 */
 	private double longitude;
 	
 	/**
 	 * Stores the latitude of the current GPS location. 
 	 */
 	private double latitude;
 	
 	/**
 	 * Stores the accuracy of the current GPS location.
 	 */
 	private float accuracy;
 	
 	/**
 	 * Stores the UNIX time of last GPS location update in seconds.
 	 */
 	private int timestamp;
 	
 	/**
 	 * Creates a new GPS location with the specified longitude, latitude, and
 	 * accuracy. Time stamp will be set to the current System's timestamp. 
 	 * 
 	 * @param longitude Longitude to set.
 	 * @param latitude Latitude to set.
 	 * @param accuracy Accuracy to set.
 	 */
 	public GeoLocation(double longitude, double latitude, float accuracy){
 		setLocation(longitude, latitude, accuracy);
 	}
 	
 	public GeoLocation(Parcel parcel){
 		setLocation(parcel);
 	}
 	
 	/**
 	 * Sets GPS longitude, latitude, and accuracy, and also updates the current
 	 * timestamp. 
 	 * 
 	 * @param longitude Longitude to set.
 	 * @param latitude Latitude to set.
 	 * @param accuracy Accuracy to set.
 	 */
 	public void setLocation(double longitude, double latitude, float accuracy){
 		this.longitude = longitude;
 		this.latitude = latitude;
 		this.accuracy = accuracy;
 		
 		updateTimestamp();
 	}
 	
 	/**
 	 * 
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
 	 * 
 	 * @param parcel
 	 */
 	public void parse(Parcel parcel){
 		this.setLocation(parcel);
 	}
 	
 	/**
 	 * Resets the time stamp to the current system's time. 
 	 */
 	private void updateTimestamp(){
 		this.timestamp = (int)(System.currentTimeMillis() / 1000);
 	}
 	
 	/**
 	 * Returns the UNIX time stamp of the last location update.
 	 * @return The UNIX time stamp of the last location update.
 	 */
 	public int getLastUpdateTime(){
 		return this.timestamp;
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
 		       "Timestamp: " + new Date(timestamp * 1000);
 	}
 
 }
