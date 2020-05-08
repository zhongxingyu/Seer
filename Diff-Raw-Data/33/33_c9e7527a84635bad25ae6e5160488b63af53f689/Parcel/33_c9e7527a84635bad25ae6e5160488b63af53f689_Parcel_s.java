 package pl.michalorman.spu.model;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 @XStreamAlias("parcel")
 public class Parcel {
 
	private String id;
 
 	private String latitude;
 
 	private String longitude;
 
	public String getId() {
 		return id;
 	}
 
	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getLatitude() {
 		return latitude;
 	}
 
 	public void setLatitude(String latitude) {
 		this.latitude = latitude;
 	}
 
 	public String getLongitude() {
 		return longitude;
 	}
 
 	public void setLongitude(String longitude) {
 		this.longitude = longitude;
 	}
 
 }
