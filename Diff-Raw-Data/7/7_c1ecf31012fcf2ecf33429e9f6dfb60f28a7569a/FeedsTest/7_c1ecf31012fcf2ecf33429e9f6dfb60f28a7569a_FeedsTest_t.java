 package burrito.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
 import burrito.BurritoRouter;
 import burrito.services.FeedsSubscription;
 import burrito.services.FeedsSubscriptionMessage;
 
 public class FeedsTest extends TestBase {
 
 	@Test
 	@SuppressWarnings("unchecked")
	public void testFeedsControllers() throws UnsupportedEncodingException, InterruptedException {
 
 		/*
 		 * Create a new subscription
 		 */
 
 		Object o = TestUtils.runController("/burrito/feeds/subscription/new/push", BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		Map<String, String> result = (Map<String, String>) o;
 
 		assertEquals("ok", result.get("status"));
 
 		String subscriptionId = result.get("subscriptionId");
 		String channelId = result.get("channelId");
 
 		assertNotNull(subscriptionId);
 
 		FeedsSubscription subscription = FeedsSubscription.getById(Long.valueOf(subscriptionId));
 
 		assertNotNull(subscription);
 
 		String clientId = subscription.getClientId();
 
 		assertNotNull(clientId);
 		assertEquals(subscriptionId, subscription.getId().toString());
 		assertEquals(channelId, subscription.getChannelId());
 
 		List<String> feedIds = subscription.getFeedIds();
 
 		assertNull(feedIds);
 
 		Date created = subscription.getCreated();
 
 		assertNotNull(created);
 		assertEquals(created, subscription.getTimestamp());
 
 		/*
 		 * Add feeds to the subscription
 		 */
 
 		String testFeedIds[] = { "A", "B", "C", "A" };
 		Set<String> uniqueTestFeedIds = new HashSet<String>(); 
 		for (String feedId : testFeedIds) {
 			if (!uniqueTestFeedIds.contains(feedId)) {
 				uniqueTestFeedIds.add(feedId);
 			}
 		}
 
 		for (int i = 0; i < testFeedIds.length; i++)
 		{
 			StringBuilder sb = new StringBuilder();
 			sb.append("/burrito/feeds/subscription/");
 			sb.append(subscriptionId);
 			sb.append("/addFeed/");
 			sb.append(URLEncoder.encode(testFeedIds[ i ], "UTF-8"));
 
 			o = TestUtils.runController(sb.toString(), BurritoRouter.class);
 
 			assertTrue(o instanceof Map<?, ?>);
 
 			result = (Map<String, String>) o;
 
 			assertEquals("ok", result.get("status"));
 
 			subscription = FeedsSubscription.getById(Long.valueOf(subscriptionId));
 
 			assertNotNull(subscription);
 			assertEquals(subscriptionId, subscription.getId().toString());
 			assertEquals(clientId, subscription.getClientId());
 			assertEquals(channelId, subscription.getChannelId());
 
 			feedIds = subscription.getFeedIds();
 
 			assertNotNull(feedIds);
 			assertEquals(Math.min(i + 1, uniqueTestFeedIds.size()), feedIds.size());
 
 			for (int j = 0; j <= i; j++)
 			{
 				assertTrue(feedIds.contains(testFeedIds[ j ]));
 			}
 
 			assertEquals(created, subscription.getCreated());
 			assertEquals(created, subscription.getTimestamp());
 		}
 
 		/*
 		 * Call keepAlive on the subscription
 		 */
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("/burrito/feeds/subscription/");
 		sb.append(subscriptionId);
 		sb.append("/keepAlive");
 
		// adding a short delay since this testcase sometimes failed because
		// everything happened within a millisecond.
		Thread.sleep(10);
		
 		o = TestUtils.runController(sb.toString(), BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		result = (Map<String, String>) o;
 
 		assertEquals("ok", result.get("status"));
 
 		subscription = FeedsSubscription.getById(Long.valueOf(subscriptionId));
 
 		assertNotNull(subscription);
 		assertEquals(subscriptionId, subscription.getId().toString());
 		assertEquals(clientId, subscription.getClientId());
 		assertEquals(channelId, subscription.getChannelId());
 
 		feedIds = subscription.getFeedIds();
 
 		assertNotNull(feedIds);
 		assertEquals(uniqueTestFeedIds.size(), feedIds.size());
 		assertEquals(created, subscription.getCreated());
 
 		Date timestamp = subscription.getTimestamp();
 		assertTrue(created.before(timestamp));
 
 		/*
 		 * Request a new channel for the subscription
 		 */
 
 		sb = new StringBuilder();
 		sb.append("/burrito/feeds/subscription/");
 		sb.append(subscriptionId);
 		sb.append("/newChannel");
 
 		o = TestUtils.runController(sb.toString(), BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		result = (Map<String, String>) o;
 
 		assertEquals("ok", result.get("status"));
 
 		subscription = FeedsSubscription.getById(Long.valueOf(subscriptionId));
 
 		assertNotNull(subscription);
 		assertEquals(subscriptionId, subscription.getId().toString());
 
 		if (channelId != null) {
 			assertNotSame(clientId, subscription.getClientId());
 			assertNotSame(channelId, subscription.getChannelId());
 		}
 
 		feedIds = subscription.getFeedIds();
 
 		assertNotNull(feedIds);
 		assertEquals(uniqueTestFeedIds.size(), feedIds.size());
 		assertEquals(created, subscription.getCreated());
 		assertEquals(timestamp, subscription.getTimestamp());
 
 		/*
 		 * Try adding a feed to a non-existing subscription
 		 */
 
 		o = TestUtils.runController("/burrito/feeds/subscription/" + Long.MAX_VALUE + "/addFeed/whatever", BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		result = (Map<String, String>) o;
 
 		assertEquals("error", result.get("status"));
 
 		/*
 		 * Try calling keepAlive on a non-existing subscription
 		 */
 
 		o = TestUtils.runController("/burrito/feeds/subscription/" + Long.MAX_VALUE + "/keepAlive", BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		result = (Map<String, String>) o;
 
 		assertEquals("error", result.get("status"));
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testPolling()
 	{
 		FeedsSubscriptionMessage msg = new FeedsSubscriptionMessage();
 		msg.setFeedId("hello");
 		msg.setMessage("world");
 		msg.setSubscriptionId(123L);
 		msg.insert();
 
 		Object o = TestUtils.runController("/burrito/feeds/subscription/123/poll", BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		Map<String, Object> result = (Map<String, Object>) o;
 
 		assertEquals("ok", result.get("status"));
 
 		o = result.get("messages");
 
 		assertTrue(o instanceof List<?>);
 
 		List<Map<String, String>> messages = (List<Map<String, String>>) o;
 
 		assertEquals(1, messages.size());
 
 		Map<String, String> message = messages.get(0);
 
 		assertEquals("hello", message.get("feedId"));
 		assertEquals("world", message.get("message"));
 
 		o = TestUtils.runController("/burrito/feeds/subscription/123/poll", BurritoRouter.class);
 
 		assertTrue(o instanceof Map<?, ?>);
 
 		result = (Map<String, Object>) o;
 
 		assertEquals("ok", result.get("status"));
 
 		o = result.get("messages");
 
 		assertTrue(o instanceof List<?>);
 
 		messages = (List<Map<String, String>>) o;
 
 		assertEquals(0, messages.size());
 	}
 }
