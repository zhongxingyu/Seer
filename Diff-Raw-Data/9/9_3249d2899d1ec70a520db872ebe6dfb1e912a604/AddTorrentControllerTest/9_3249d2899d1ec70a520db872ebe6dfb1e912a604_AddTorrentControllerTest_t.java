 package ar.edu.utn.tacs.group5.controller;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.when;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.slim3.tester.MockHttpServletRequest;
 
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.model.Item;
 import ar.edu.utn.tacs.group5.service.FeedService;
 
 import com.google.api.server.spi.config.ApiMethod.HttpMethod;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.common.net.MediaType;
 
 public class AddTorrentControllerTest extends AbstractAuthorizedControllerTest<AddTorrentController> {
 
 	private FeedService feedService = new FeedService();
 
 	@Test
 	public void testRunAddedOk() throws Exception {
 		doLogin();
 		Feed feed = prepareValidRequest();
 		tester.start(resource());
 		assertController(HttpStatus.SC_CREATED);
 
 		feed = feedService.getByKey(feed.getKey());
 		assertThat(feed.getItems().size(), is(1));
 		Item item = feed.getItems().get(0);
 		assertThat(item.getTitle(), is("foo"));
 		assertThat(item.getLink(), is("http://www.foo.com"));
 		assertThat(item.getDescription(), is("bar"));
 	}
 
 	@Test
 	public void testRunIsNotAllowed() throws Exception {
 		doLogin();
 		tester.request.setMethod(HttpMethod.GET);
 		tester.start(resource());
 		assertController(HttpStatus.SC_METHOD_NOT_ALLOWED);
 	}
 
 	@Test
 	public void testRunHasNoTorrent() throws Exception {
 		doLogin();
 		MockHttpServletRequest request = tester.request;
 		request.setContentType(MediaType.JSON_UTF_8.toString());
 		request.setMethod(HttpMethod.POST);
 		tester.start(resource());
 		assertController(HttpStatus.SC_BAD_REQUEST);
 	}
 
 	@Test
 	public void testRunHasNoFeedKey() throws Exception {
 		doLogin();
 		MockHttpServletRequest request = tester.request;
 		request.setContentType(MediaType.JSON_UTF_8.toString());
 		request.setMethod(HttpMethod.POST);
 		tester.start(resource());
 		assertController(HttpStatus.SC_BAD_REQUEST);
 	}
 
 	@Test
 	public void testRunHasInvalidFeedKey() throws Exception {
 		doLogin();
 		MockHttpServletRequest request = tester.request;
 		request.setContentType(MediaType.JSON_UTF_8.toString());
 		request.setMethod(HttpMethod.POST);
 		request.setParameter(Constants.FEED, "xxxx9999xxxxx");
 		tester.start(resource());
 		assertController(HttpStatus.SC_BAD_REQUEST);
 	}
 
 	@Test
 	public void testRunFromFB() throws Exception {
 		doLogin();
		prepareValidRequest();
		tester.request.setParameter(Constants.FROM_FB, String.valueOf(true));
 		tester.start(resource());
 		AddTorrentController controller = tester.getController();
 		assertThat(controller, is(notNullValue()));
		assertThat(tester.response.getStatus(), is(HttpStatus.SC_MOVED_TEMPORARILY));
 		assertThat(tester.isRedirect(), is(true));
 		assertThat(tester.getDestinationPath(), is("index.jsp"));
 	}
 
 	@Override
 	protected String resource() {
 		return "/AddTorrent";
 	}
 
 	private Feed prepareValidRequest() throws IOException {
 		String torrent = "{ title: \"foo\", description: \"bar\", link: \"http://www.foo.com\" }";
 
 		Feed feed = new Feed();
 		feed.setTitle("My Feed");
 		feed.setDescription("My Feed description");
 		feedService.insert(feed);
 
 		BufferedReader reader = Mockito.mock(BufferedReader.class);
 		when(reader.readLine()).thenReturn(torrent);
 		MockHttpServletRequest request = tester.request;
 		request.setReader(reader);
 		request.setContentType(MediaType.JSON_UTF_8.toString());
 		request.setMethod(HttpMethod.POST);
 		request.addParameter(Constants.FEED, KeyFactory.keyToString(feed.getKey()));
 		return feed;
 	}
 
 }
