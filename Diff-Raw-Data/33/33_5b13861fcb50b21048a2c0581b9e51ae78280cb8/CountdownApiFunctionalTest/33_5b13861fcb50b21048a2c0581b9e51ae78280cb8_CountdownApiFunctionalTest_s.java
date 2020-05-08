 package uk.co.eelpieconsulting.countdown.api;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.co.eelpieconsulting.countdown.model.Arrival;
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

 }
