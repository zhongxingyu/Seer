 package nz.co.searchwellington.feeds;
 
 import static org.junit.Assert.assertEquals;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.frontend.FrontendFeed;
 import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.utils.TextTrimmer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 import uk.co.eelpieconsulting.common.geo.model.Place;
 
 public class FeednewsItemToNewsitemServiceTest {
 
	@Mock  TextTrimmer textTrimmer;
	
 	@Mock Place place;
 	@Mock private Feed feed;
 	
 	FeednewsItemToNewsitemService service;
 	
 	@Before
 	public void setup() {
 		MockitoAnnotations.initMocks(this);
		service = new FeednewsItemToNewsitemService(textTrimmer);
 	}
 	
 	@Test
 	public void shouldSetGeocodeWhenAcceptingFeedNewsitem() throws Exception {
 		Mockito.when(place.getAddress()).thenReturn("A place");
 		FrontendFeed frontendFeed = new FrontendFeed();
 		FrontendFeedNewsitem feedNewsitem = new FrontendFeedNewsitem();
 		feedNewsitem.setFeed(frontendFeed);
 		feedNewsitem.setPlace(place);
 		
 		Newsitem newsitem = service.makeNewsitemFromFeedItem(feed, feedNewsitem);
 		
 		assertEquals("A place", newsitem.getGeocode().getAddress());
 	}
 	
 }
