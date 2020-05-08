 package dk.wallviz.skylinehotels;
 
 public class Hotel implements Record {
 	String id;
 	String name;
 	String address1;
 	String city;
 	String postalCode;
 	int propertyCategory;
 	double hotelRating;
 	//double confidenceRating":84,
 	double tripAdvisorRating;
 	double highRate;
 	double lat;
 	double lon;
 	double proximityDistance;
 	boolean internet;
 	boolean pool;
 	double distFromColosseum;
 	double distFromTreviFountain;
 	String picture;
 	
 	public Hotel(String id, String name, String address1, String city,
 			String postalCode, int propertyCategory, double hotelRating,
 			double tripAdvisorRating, double highRate, double lat, double lon,
 			double proximityDistance, boolean internet, boolean pool,
 			double distFromColosseum, double distFromTreviFountain,
 			String picture) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.address1 = address1;
 		this.city = city;
 		this.postalCode = postalCode;
 		this.propertyCategory = propertyCategory;
 		this.hotelRating = hotelRating;
 		this.tripAdvisorRating = tripAdvisorRating;
 		this.highRate = highRate;
 		this.lat = lat;
 		this.lon = lon;
 		this.proximityDistance = proximityDistance;
 		this.internet = internet;
 		this.pool = pool;
 		this.distFromColosseum = distFromColosseum;
 		this.distFromTreviFountain = distFromTreviFountain;
 		// custom conversion for this application: data might be placed in different directories
 		int numericId = Integer.parseInt(id);
 		String path = "";
		String[] parts = picture.split("/");
 		if (numericId <= 297078)
			path = parts[0]+"/low/"+parts[1];
		else path = parts[0]+"/high/"+parts[1];
		this.picture = path;
 	}
 	
 	public String toJSON(int order) {
 		return "{\"order\":"+order+","+
 			  "\"id\":"+id+","+
 			  "\"name\":\""+name+"\","+
 			  "\"address1\":\""+address1+"\","+
 			  "\"city\":\""+city+"\","+
 			  "\"postalCode\":\""+postalCode+"\","+
 			  "\"propertyCategory\":"+propertyCategory+","+
 			  "\"hotelRating\":"+hotelRating+","+
 			  //"\"confidenceRating\":"+confidenceRating+","+
 			  "\"tripAdvisorRating\":"+tripAdvisorRating+","+
 			  "\"highRate\":"+highRate+","+
 			  "\"lat\":"+lat+","+
 			  "\"lon\":"+lon+","+
 			  "\"proximityDistance\":"+proximityDistance+","+
 			  "\"internet\":"+internet+","+
 			  "\"pool\":"+pool+","+
 			  "\"distFromColosseum\":"+distFromColosseum+","+
 			  "\"distFromTreviFountain\":"+distFromTreviFountain+","+
 			  "\"picture\":\""+picture+"\"}";
 	} 
 }
