 package dk.itu.realms.services.util;
 
 import org.primefaces.model.map.LatLng;
 
 import dk.itu.realms.model.entity.Mark;
 import dk.itu.realms.model.entity.Realm;
 
 public class LatLonCalculator {
 
 	private static int EARTH_RADIUS = 6371; // In kilometers
 
 	public boolean insideRealm(LatLng userPosition, Realm realm) {
 		System.out.println("--------------------------------------");
 		System.out.println("Lat, Lon " + userPosition.getLat() + ", "
 				+ userPosition.getLng());
 		double dLat = Math.toRadians(realm.getLatitude() - userPosition.getLat());
 		System.out.println("dLat: " + dLat);
 		double dLon = Math.toRadians(realm.getLongitude() - userPosition.getLng());
 		System.out.println("dLon: " + dLon);
 		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
 				* Math.sin(dLon / 2)
 				* Math.cos(Math.toRadians(realm.getLatitude()))
 				* Math.cos(Math.toRadians(userPosition.getLng()));
 		System.out.println("a: " + a);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		System.out.println("c is " + c + " Distance to center is: " + c
 				* EARTH_RADIUS + "km - Radius of realm is: "
 				+ realm.getRadius());
 		double dist = c * EARTH_RADIUS * 1000;
 		if (!Double.isNaN(dist)) {
 			System.out.println("Not NaN: " + c * EARTH_RADIUS * 1000);
 			return c * EARTH_RADIUS * 1000 <= realm.getRadius();
 		} else {
 			System.out.println("NaN: " + c * EARTH_RADIUS * 1000);
 			return c * EARTH_RADIUS <= realm.getRadius() / 1000;
 		}
 
 	}
 	
 	public boolean insideMark(LatLng userPosition, Mark mark) {
 		System.out.println("-----------------------------------------");
 		System.out.println(userPosition.getLat() + ", " + userPosition.getLng() + ", " + mark.getRadius());
 		double dLat = Math.toRadians(mark.getLatitude() - userPosition.getLat());
 		double dLon = Math.toRadians(mark.getLongitude() - userPosition.getLng());
 		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
 				* Math.sin(dLon / 2)
 				* Math.cos(Math.toRadians(mark.getLatitude()))
 				* Math.cos(Math.toRadians(userPosition.getLng()));
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double dist = c * EARTH_RADIUS * 1000;
 		System.out.println("c:" + c);
 		if (!Double.isNaN(dist)) {
 			return c * EARTH_RADIUS * 1000 <= mark.getRadius();
 		} else {
 			return c * EARTH_RADIUS <= mark.getRadius() / 1000;
 		}
 		
	}
 }
