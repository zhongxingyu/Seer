 package uk.co.eelpieconsulting.countdown.api;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.co.eelpieconsulting.countdown.model.Arrival;
import uk.co.eelpieconsulting.countdown.model.BoundingBox;
 import uk.co.eelpieconsulting.countdown.model.Place;
 import uk.co.eelpieconsulting.countdown.model.PlaceSearchResult;
 import uk.co.eelpieconsulting.countdown.model.Stop;
 import uk.co.eelpieconsulting.countdown.model.StopBoard;
 
 public class CountdownApiFunctionalTest {
 	
 	private static final String LIVE_API_URL = "http://countdown.tfl.gov.uk";
 	
 	private CountdownApi api;
 
 	@Before
 	public void setup() {		
 		api = new CountdownApi(LIVE_API_URL);
 	}
 
 	@Test
 	public void stopBoardTest() throws Exception {		
 		StopBoard stopBoard = api.getStopBoard(53550);
 		
 		assertNotNull(stopBoard);		
 		for (Arrival arrival : stopBoard.getArrivals()) {
 			System.out.println(arrival);
 		}
 	}
 	
 	@Test
 	public void stopSearchTest() throws Exception {
 		List<Stop> stops = api.findStopsWithin(51.454, -0.351, 51.481, -0.307);
 		
 		assertNotNull(stops);
 		assertFalse(stops.isEmpty());
 		for (Stop stop : stops) {
 			System.out.println(stop);
 		}
 	}
 	
 	@Test
 	public void placeSearchTest() throws Exception {
 		PlaceSearchResult results = api.searchForPlaces("Twickenham");
 		
 		assertNotNull(results.getBoundingBox());
 		assertFalse(results.getPlaces().isEmpty());
 		for(Place place : results.getPlaces()) {
 			System.out.println(place);
 		}
 	}
	
	// Example usage for README file
	@Test
	public void exampleUsage() throws Exception {
		CountdownApi api = new CountdownApi("http://countdown.tfl.gov.uk");
		
		// Search for a place name
		final String placeName = "Twickenham";
		System.out.println("Searching for places named: " + placeName);
		PlaceSearchResult placeSearchResults = api.searchForPlaces(placeName);
		List<Place> places = placeSearchResults.getPlaces();
		System.out.println("Found " + places.size() + " results");
		Place firstPlace = places.get(0);
		System.out.println("The first one was: " + firstPlace.getName() + "/" + firstPlace.getPlace() + " at " + firstPlace.getLat() + ", " + firstPlace.getLng());
		
		// Load a list of stops with in the bounding box for this place name search result.
		BoundingBox boundingBox = placeSearchResults.getBoundingBox();
		System.out.println("Searching for stops within bounding box: " + boundingBox);
		List<Stop> stops = api.findStopsWithin(boundingBox.getSwLat(), boundingBox.getSwLng(), boundingBox.getNeLat(), boundingBox.getNeLng());
		System.out.println("Found " + stops.size() + " stops");
		Stop firstStop = stops.get(0);
		System.out.println("This first one is: " + firstStop.getName() + "(" + firstStop.getId() + ")" + firstStop.getTowards() + " at " + firstStop.getLatitude() + ", " + firstStop.getLongitude());
		
		// Load a list of expected arrivals for a stop
		StopBoard stopBoard = api.getStopBoard(Integer.parseInt(firstStop.getId()));	// TODO type mismatch
		Arrival firstArrival = stopBoard.getArrivals().get(0);
		System.out.println("Next arrival is: " + firstArrival.getRouteName() + " to " + firstArrival.getDestination() + ": " + firstArrival.getEstimatedWait());
		System.out.println("Last updated: " + stopBoard.getLastUpdated());
	}
	
 }
