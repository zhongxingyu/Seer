 package nz.co.searchwellington.controllers;
 
 import java.util.Date;
 
import nz.co.searchwellington.controllers.submission.UrlProcessor;
 import nz.co.searchwellington.geocoding.osm.CachingNominatimGeocodingService;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.UrlWordsGenerator;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.HandTaggingDAO;
 import nz.co.searchwellington.repositories.HibernateResourceDAO;
 import nz.co.searchwellington.repositories.TagDAO;
 import nz.co.searchwellington.utils.UrlCleaner;
 
 import org.apache.struts.mock.MockHttpServletRequest;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 public class SubmissionProcessingServiceTest {
 
 	private static final String FEED_NAME = "A feed";
	
 	@Mock UrlCleaner urlCleaner;
 	@Mock CachingNominatimGeocodingService geocodeService;
 	@Mock TagDAO tagDAO;
 	@Mock HandTaggingDAO tagVoteDAO;
 	@Mock HibernateResourceDAO resourceDAO;
 	@Mock Newsitem resource;
 	@Mock Feed feed;
 	@Mock User loggedInUser;	
	@Mock UrlProcessor urlProcessor;
	@Mock private CachingNominatimGeocodingService nominatimGeocodingService;
 	
 	private MockHttpServletRequest request;
 	private SubmissionProcessingService submissionProcessingService;
 	
 	@Before
 	public void setup() {
 		MockitoAnnotations.initMocks(this);
 		request = new MockHttpServletRequest();
 		Mockito.when(feed.getName()).thenReturn(FEED_NAME);
		submissionProcessingService = new SubmissionProcessingService(nominatimGeocodingService, tagDAO, tagVoteDAO, resourceDAO, urlProcessor);
 	}
 	
 	@Test
 	public void acceptsEmbargoDates() throws Exception {
 		Date embargoDate = new DateTime().toDate();
 		request.setAttribute("embargo_date", embargoDate);
 		
 		submissionProcessingService.processEmbargoDate(request, resource);
 		
 		Mockito.verify(resource).setEmbargoedUntil(embargoDate);
 	}
 	
 	@Test
 	public void shouldFlattenLoudCapsHeadlinesInUserSubmissions() throws Exception {	
 		request.addParameter("title", "THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG");
 		
 		submissionProcessingService.processTitle(request, resource);
 		
 		Mockito.verify(resource).setName("The quick brown fox jumped over the lazy dog");
 	}
 	
 	@Test
 	public void shouldPopulateAcceptanceFieldsOnInitalSubmission() throws Exception {
 		request.addParameter("acceptedFromFeed", UrlWordsGenerator.makeUrlWordsFromName(FEED_NAME));	
 		Mockito.when(resourceDAO.loadFeedByUrlWords("a-feed")).thenReturn(feed);
 		
 		submissionProcessingService.processAcceptance(request, resource, loggedInUser);
 		
 		Mockito.verify(resource).setFeed(feed);
 		Mockito.verify(resource).setAcceptedBy(loggedInUser);
 		// TODO assert acceptance time was set!
 	}
 	
 }
