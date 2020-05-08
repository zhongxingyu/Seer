 package nz.co.searchwellington.geocoding;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import nz.co.searchwellington.model.Geocode;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class NominatimGeocodingFunctionalServiceTest {
 
 	private NominatimGeocodingService service;
 	
 	@Before
 	public void setup() {
 		service = new NominatimGeocodingService();
 	}
 
 	@Test
 	public void canResolveBuildingNameToLocation() {
 		List<Geocode> results = service.resolveAddress("St James Presbyterian Church, Newtown, Wellington");
 		final Geocode firstMatch = results.get(0);
 		assertEquals("St James' Presbyterian Church, Adelaide Road, Newtown, Wellington, Wellington Region, 6021, New Zealand", firstMatch.getAddress());
		assertEquals(301919657, firstMatch.getOsmId().longValue());
 		assertEquals("place_of_worship", firstMatch.getType());
 	}
 
 	@Test
 	public void canResolveAddressWithMultipleResults() throws Exception {
 		List<Geocode> results = service.resolveAddress("Civic Square, Wellington");
 		assertEquals(2, results.size());
 	}
 	
 }
