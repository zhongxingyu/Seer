 package dk.wallviz.skylinehotels;
 
 import java.util.*;
 
 public class RepresentativeSkyline {
 	private Collection<Integer> atts;
 	private Hotel[] skylineHotels;
 	private HotelPredicate minmax;
 	
 	public RepresentativeSkyline(Hotel[] skylineHotels, Collection<Integer> atts) {
 		super();
 		this.atts = atts;
 		this.skylineHotels = skylineHotels;
 	}
 	
 	Hotel[] execute() {
 		ArrayList<Hotel> result = new ArrayList<Hotel>();
	
 		// find min/max values
 		minmax = HotelPredicate.fit(skylineHotels);
 		// select one and collect the others in a decent data structure
 		result.add(skylineHotels[0]);
 		Set<Hotel> unprocessed = new HashSet<Hotel>();
		for (int i=0; i<skylineHotels.length; i++)
 			unprocessed.add(skylineHotels[i]);
 		
 		// Repeat k-1 times (in this case, for all hotels):
 		for (int i=0; i<4; i++) {
 		//while (unprocessed.size()>0) {
 			if (unprocessed.size()==0) break;
 			// Find record r maximizing representative distance
 			double currentMaxDistance = 0;
 			Hotel currentFurthestHotel = null;
 			for (Hotel h: unprocessed) {
 				double dist = representativeDistance(h,result);
 				if (dist>=currentMaxDistance) { 
 					currentMaxDistance = dist;
 					currentFurthestHotel = h;
 				}
 			}
 			//System.out.println(currentMaxDistance);
 			// Add h to the result and delete it from the unprocessed set
 			result.add(currentFurthestHotel);
 			unprocessed.remove(currentFurthestHotel);
 		}
 		return result.toArray(new Hotel[0]);
 	}
 	
 	/**
 	 * Representative distance
 	 */
 	private double representativeDistance(Hotel h, Collection<Hotel> representativeHotels) {
 		double currentDistance = Double.POSITIVE_INFINITY;
 		for (Hotel representativeHotel: representativeHotels) {
 			double dist = dist(h, representativeHotel);
 			if (dist<currentDistance) currentDistance = dist;
 		}
 		return currentDistance;
 	}
 	
 	private double dist(Hotel h1, Hotel h2) {
 		double dist = 0;
 		for (int att: atts) {
 			if (Hotel.isBoolean(att)) {
 				if (h1.getBoolean(att) ^ h2.getBoolean(att))
 					dist += 1;
 			}
 			else if (Hotel.isDouble(att)) {
 				if (minmax.getMax(att)!=minmax.getMin(att))
 					dist += Math.abs((h1.getDouble(att)-h2.getDouble(att))/(minmax.getMax(att)-minmax.getMin(att)));
 			}
 		}
 		return dist/atts.size();
 		
 		/*int numIntermediatePoints = 0;
 		for (Hotel h: skylineHotels) {
 			if (atts.hotelRating) {
 				if (((h1.hotelRating >= h.hotelRating) && (h.hotelRating >= h2.hotelRating)) ||
 					((h2.hotelRating >= h.hotelRating) && (h.hotelRating >= h1.hotelRating)))
 					numIntermediatePoints++;
 			}
 			if (atts.tripAdvisorRating) {
 				if (((h1.tripAdvisorRating >= h.tripAdvisorRating) && (h.tripAdvisorRating >= h2.tripAdvisorRating)) ||
 						((h2.tripAdvisorRating >= h.tripAdvisorRating) && (h.tripAdvisorRating >= h1.tripAdvisorRating)))
 						numIntermediatePoints++;
 				}
 			if (atts.highRate) {
 				if (((h1.highRate >= h.highRate) && (h.highRate >= h2.highRate)) ||
 						((h2.highRate >= h.highRate) && (h.highRate >= h1.highRate)))
 						numIntermediatePoints++;
 				}
 			if (atts.proximityDistance) {
 				if (((h1.proximityDistance >= h.proximityDistance) && (h.proximityDistance >= h2.proximityDistance)) ||
 						((h2.proximityDistance >= h.proximityDistance) && (h.proximityDistance >= h1.proximityDistance)))
 						numIntermediatePoints++;
 			}
 			if (atts.distFromColosseum) {
 				if (((h1.distFromColosseum >= h.distFromColosseum) && (h.distFromColosseum >= h2.distFromColosseum)) ||
 						((h2.distFromColosseum >= h.distFromColosseum) && (h.distFromColosseum >= h1.distFromColosseum)))
 						numIntermediatePoints++;
 			}
 			if (atts.distFromTreviFountain) {
 				if (((h1.distFromTreviFountain >= h.distFromTreviFountain) && (h.distFromTreviFountain >= h2.distFromTreviFountain)) ||
 						((h2.distFromTreviFountain >= h.distFromTreviFountain) && (h.distFromTreviFountain >= h1.distFromTreviFountain)))
 						numIntermediatePoints++;
 			}
 			if (atts.pool) {
 				if (h1.pool ^ h2.pool)
 					numIntermediatePoints++;
 			}
 			if (atts.internet) {
 				if (h1.internet ^ h2.internet)
 					numIntermediatePoints++;
 			}
 		}
 		return (double)numIntermediatePoints/(skylineHotels.length-1)/atts.length();*/
 	}
 }
