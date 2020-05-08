 package models;
 
 import googleMapsDirections.Directions;
import play.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 
 @Entity
 public class TripRequest extends Trip {
 
     private static final double tripOverhead = 1.1;
 
     /**
      *
      */
     private static final long serialVersionUID = -7829362116641187593L;
     public static Finder<Integer, TripRequest> find = new Finder<Integer, TripRequest>(Integer.class, TripRequest.class);
 
     public TripRequest() {
         super();
     }
 
     public List<TripOffer> getMatchingOffers() {
 
         List<TripOffer> matches = new ArrayList<TripOffer>();
 
         // Reduce to matching offers in time window
         List<TripOffer> matchesInTimeWindow = TripOffer.find.where().le("start_time_min", getStartTimeMax()).ge("start_time_max", getStartTimeMin())
                 .ge("end_time_max", getEndTimeMin()).le("end_time_min", getEndTimeMax()).ge("number_of_seats", getNumberOfSeats()).findList();
 
         // Reduce matching offer to matching offers in boundary box and
         // travelDistance
         Directions d;
         for (TripOffer t : matchesInTimeWindow) {
             d = new Directions();
             d.addWaypoint(new Waypoint(t.getOriginLong(), t.getOriginLat(), t.getStartTimeMin(), t.getStartTimeMax()));
             d.addWaypoint(new Waypoint(t.getDestinationLong(), t.getDestinationLat(), t.getEndTimeMin(), t.getEndTimeMax()));
 
             if (isBetweenBounds(d.getNorthWestBounds(), d.getSouthEastBounds()) && isPossibleMatchOnTravelDistance(t)) {
                 matches.add(t);
             }
         }
         return matches;
     }
 
     private boolean isPossibleMatchOnTravelDistance(TripOffer t) {
         Waypoint offerOrigin = new Waypoint(t.getOriginLong(), t.getOriginLat(), t.getStartTimeMin(), t.getStartTimeMax());
         Waypoint offerDestination = new Waypoint(t.getDestinationLong(), t.getDestinationLat(), t.getEndTimeMin(), t.getEndTimeMax());
         Waypoint requestOrigin = new Waypoint(getOriginLong(), getOriginLat(), getStartTimeMin(), getStartTimeMax());
         Waypoint requestDestination = new Waypoint(getDestinationLong(), getDestinationLat(), getEndTimeMin(), getEndTimeMax());
 
         Directions originalOffer = new Directions();
         originalOffer.addWaypoint(offerOrigin);
         originalOffer.addWaypoint(offerDestination);
 
         Directions offerIncludingRequest = new Directions();
         offerIncludingRequest.addWaypoint(offerOrigin);
         offerIncludingRequest.addWaypoint(requestOrigin);
         offerIncludingRequest.addWaypoint(requestDestination);
         offerIncludingRequest.addWaypoint(offerDestination);
 
         if (originalOffer.getApproximateRouteDistance() * tripOverhead >= offerIncludingRequest.getApproximateRouteDistance()) {
 
             if (!t.getMetaData().hasResultsFromAPI()) {
                 originalOffer.retrieveGoogleAPICalculations();
                 offerIncludingRequest.retrieveGoogleAPICalculations();
 
                 t.getMetaData().setCalculatedDuration(originalOffer.getCalculatedTravelTimeInSeconds());
                 t.getMetaData().setDirectionsDistance(originalOffer.getTotalDirectionDistance());
                 t.getMetaData().save();
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                    Logger.error(e.getMessage());
                 }
             }
 
             if (t.getMetaData().hasResultsFromAPI() && originalOffer.getTotalDirectionDistance() * tripOverhead >= offerIncludingRequest.getTotalDirectionDistance()) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean isBetweenBounds(Location northWestBounds, Location southEastBounds) {
         boolean inBoundaries;
 
         inBoundaries = (northWestBounds.getLongitude() >= getOriginLong());
         inBoundaries = inBoundaries && (northWestBounds.getLongitude() >= getDestinationLong());
         inBoundaries = inBoundaries && (northWestBounds.getLatitude() <= getOriginLat());
         inBoundaries = inBoundaries && (northWestBounds.getLatitude() <= getDestinationLat());
         inBoundaries = inBoundaries && (southEastBounds.getLongitude() <= getOriginLong());
         inBoundaries = inBoundaries && (southEastBounds.getLongitude() <= getDestinationLong());
         inBoundaries = inBoundaries && (southEastBounds.getLatitude() >= getOriginLat());
         inBoundaries = inBoundaries && (southEastBounds.getLatitude() >= getDestinationLat());
 
         return inBoundaries;
     }
 
 }
