 package cz.mapyhazardu.api.domain;
 
 //TODO: longtitude rename to longitude
 public class GeographicCoordinate {
 	
 	private double latitude;
 	private double longtitude;
 	
 	public GeographicCoordinate() {
 	}
 	
	public GeographicCoordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longtitude = longitude;
	}
	
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	public double getLongtitude() {
 		return longtitude;
 	}
 
 	public GeographicCoordinate setLatitude(double latitude) {
 		this.latitude = latitude;
 		return this;
 	}
 
 	public GeographicCoordinate setLongtitude(double longtitude) {
 		this.longtitude = longtitude;
 		return this;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(latitude);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		temp = Double.doubleToLongBits(longtitude);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		GeographicCoordinate other = (GeographicCoordinate) obj;
 		if (Double.doubleToLongBits(latitude) != Double
 				.doubleToLongBits(other.latitude))
 			return false;
 		if (Double.doubleToLongBits(longtitude) != Double
 				.doubleToLongBits(other.longtitude))
 			return false;
 		return true;
 	}
 	
 	
 	
 	
 	
 }
