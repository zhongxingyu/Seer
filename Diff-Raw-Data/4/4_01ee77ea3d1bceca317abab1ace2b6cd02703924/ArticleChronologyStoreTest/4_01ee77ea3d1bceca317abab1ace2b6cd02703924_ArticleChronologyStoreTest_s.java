 package com.madgag.guardian.guardian;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 
import java.util.Arrays;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.madgag.guardian.contentapi.jaxb.Content;
 
 
 public class ArticleChronologyStoreTest {
 
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
     @Before
     public void setUp() {
         helper.setUp();
     }
 
     @After
     public void tearDown() {
         helper.tearDown();
     }
 	
 	@Test
 	public void shouldDoStuff() throws Exception {
 		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
 		ArticleChronologyStore articleChronologyStore = new ArticleChronologyStore(datastoreService);
 		Content content = new Content();
 		content.id="myId";
 		content.webPublicationDate=new DateTime();
 		DateTime d = content.webPublicationDate;
 		articleChronologyStore.storeChronologyFrom(asList(content));
 		
 		Iterable<String> articleIds = articleChronologyStore.getArticleIdsFor(new Interval(d.minusHours(1),d.plusHours(1)));
 		assertThat(articleIds, hasItem(content.id));
 	}
 	
 }
