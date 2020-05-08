 package nz.co.searchwellington.model;
 
 import java.io.Serializable;
 
 import geo.google.datamodel.GeoAltitude;
 import geo.google.datamodel.GeoCoordinate;
 import geo.google.datamodel.GeoUtils;
 
 import org.apache.log4j.Logger;
 
 public class Geocode implements Serializable {
     
 	private static Logger log = Logger.getLogger(Geocode.class);
 
 	private static final long serialVersionUID = 1L;
 	
     private int id;
     private String address;
     private Double latitude;
     private Double longitude;
     private String type;
     private Long osmId;
     private String osmType;
     private String resolver;
     
     public Geocode() {        
     }
             
     public Geocode(String address) {
         this.address = address;
         this.latitude = null;
         this.longitude = null;
         this.osmId = null;
         this.osmType = null;
     }
         
     public Geocode(String address, double latitude, double longitude) {     
         this.address = address;
         this.latitude = latitude;
         this.longitude = longitude;
     }
 
     public Geocode(Double latitude, Double longitude) {
     	 this.latitude = latitude;
          this.longitude = longitude;
     }
 
 	public Geocode(String address, double latitude, double longitude, String type, Long osmId, String osmType, String resolver) {
 		this.address = address;
 		this.latitude = latitude;
 		this.longitude = longitude;
 		this.type = type;
 		this.osmId = osmId;
 		this.osmType = osmType;
 		this.resolver = resolver;
 	}
 
 	public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getAddress() {
         return address;
     }
     public void setAddress(String address) {
         this.address = address;
     }
     public Double getLatitude() {
         return latitude;
     }
     public void setLatitude(Double d) {
         this.latitude = d;
     }
     public Double getLongitude() {
     	return longitude;
     }
     public void setLongitude(Double longitude) {
         this.longitude = longitude;
     }
     
     public boolean isValid() {
         return latitude != null && longitude != null;
     }
     
     public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public Long getOsmId() {
 		return osmId;
 	}
 
 	public void setOsmId(Long osmId) {
 		this.osmId = osmId;
 	}
 	
 	public String getOsmType() {
 		return osmType;
 	}
 	
 	public String getResolver() {
 		return resolver;
 	}
 	
 	public void setResolver(String resolver) {
 		this.resolver = resolver;
 	}
 	
 	// TODO These two compare methods shouldn't really be on the domain model
     public boolean isSameLocation(Geocode other) {        
         final double distanceBetweenInKm = getDistanceTo(other.getLatitude(), other.getLongitude());
         log.debug("Points " + this.getAddress() + " and " + other.getAddress() + " are " + distanceBetweenInKm + " km part");
         return distanceBetweenInKm < 0.1;
     }
     
 	public double getDistanceTo(double otherLatitude, double otherLongitude) {
 		GeoCoordinate thisPoint = new GeoCoordinate(this.getLatitude(), this.getLongitude(), new GeoAltitude(0));
 		GeoCoordinate otherPoint = new GeoCoordinate(otherLatitude, otherLongitude, new GeoAltitude(0));
 		double distanceBetweenInKm = GeoUtils.distanceBetweenInKm(thisPoint, otherPoint);
 		log.debug("Distance to " + latitude + ", " + longitude + " is " + distanceBetweenInKm);
 		return distanceBetweenInKm;
 	}
 
 	@Override
 	public String toString() {
 		return "Geocode [address=" + address + ", id=" + id + ", latitude="
 				+ latitude + ", longitude=" + longitude + ", osmPlaceId="
 				+ osmId + ", type=" + type + "]";
 	}
 	
 }
