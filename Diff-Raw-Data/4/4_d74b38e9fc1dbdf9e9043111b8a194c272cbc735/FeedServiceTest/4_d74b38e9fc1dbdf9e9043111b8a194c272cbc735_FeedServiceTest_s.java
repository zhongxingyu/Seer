 package ar.edu.utn.tacs.group5.service;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
import org.slim3.tester.AppEngineTestCase;
 
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.model.Item;
 
public class FeedServiceTest extends AppEngineTestCase {
 
 	private FeedService service = new FeedService();
 
 	@Test
 	public void testInsert() throws Exception {
 		Feed feed = new Feed();
 		service.insert(feed);
 		assertThat(feed.getKey(), is(notNullValue()));
 	}
 
 	@Test
 	public void testHasDefaultFeed() throws Exception {
 		long userId = 123456789L;
 		assertFalse(service.hasDefaultFeed(userId));
 		Feed feed = new Feed();
 		feed.setUserId(userId);
 		service.insert(feed);
 		assertTrue(service.hasDefaultFeed(userId));
 	}
 
 	@Test
 	public void testAddItem() throws Exception {
 		Feed feed = new Feed();
 		service.insert(feed);
 		service.addItem(feed, new Item());
 		feed = service.getByKey(feed.getKey());
 		assertThat(feed.getItems().size(), is(1));
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testAddItemWithNullItem() throws Exception {
 		service.addItem(new Feed(), null);
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testAddItemWithNullFeed() throws Exception {
 		service.addItem(null, new Item());
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testAddItemWithNullFeedAndItem() throws Exception {
 		service.addItem(null, null);
 	}
 
 	@Test
 	public void testGetByKey() throws Exception {
 		Feed feed = new Feed();
 		feed.setTitle("foo");
 		feed.setDescription("Foo description");
 		service.insert(feed);
 		Feed feedByKey = service.getByKey(feed.getKey());
 		assertNotNull(feedByKey);
 		assertThat(feedByKey.getTitle(), is("foo"));
 		assertThat(feedByKey.getDescription(), is("Foo description"));
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testGetByKeyWithNullKey() throws Exception {
 		service.getByKey(null);
 	}
 
 	@Test
 	public void testGetByUserId() throws Exception {
 		long userId = 123456789L;
 		service.insert(userId);
 		Feed feed = service.getByUserId(userId);
 		assertNotNull(feed);
 		assertThat(feed.getTitle(), is(FeedService.DEFAULT_FEED));
 	}
 
 	@Test
 	public void testGetByUserIdWithInvalidId() throws Exception {
 		Feed feed = service.getByUserId(9999999L);
 		assertNull(feed);
 	}
 
 }
