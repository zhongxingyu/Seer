 package org.computer.knauss.reqtDiscussion.io.jazz.rest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.sql.Date;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Stack;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolVersion;
 import org.apache.http.StatusLine;
 import org.apache.http.message.BasicHttpResponse;
 import org.computer.knauss.reqtDiscussion.io.DAOException;
 import org.computer.knauss.reqtDiscussion.io.IDAOProgressMonitor;
 import org.computer.knauss.reqtDiscussion.io.Util;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.DiscussionFactory;
 import org.jdom2.JDOMException;
 import org.junit.Before;
 import org.junit.Test;
 
 public class JazzJDOMDAOTest {
 
 	private JazzJDOMDAO dao;
 	private ConnectorProbe connector;
 
 	@Before
 	public void setup() throws IOException {
 		DiscussionFactory.getInstance().clear();
 		connector = new ConnectorProbe();
 		this.dao = new JazzJDOMDAO(connector);
 	}
 
 	@Test
 	public void testGetProjectAreas() throws Exception {
 		String[] projectAreas = this.dao.getProjectAreas();
 		assertEquals(5, projectAreas.length);
 
 		assertEquals("Jazz Collaborative ALM", projectAreas[0]);
 		assertEquals("Rational Team Concert", projectAreas[1]);
 		assertEquals("Jazz Foundation", projectAreas[2]);
 		assertEquals("Testing Purposes Only - CLM 2012  (Change Management)",
 				projectAreas[3]);
 		assertEquals("Rational AMC", projectAreas[4]);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSelectWorkitemsNoProjectArea() throws JDOMException,
 			IOException, Exception {
 		this.dao.getWorkitemsForType();
 	}
 
 	@Test
 	public void testLimit() throws DAOException {
 		assertEquals(50, this.dao.getLimit());
 		this.dao.setProjectArea("Rational Team Concert");
 
		// The test fixture returns 50 items. But I need to query them to see if
		// the query is correct.
 		assertEquals(50, this.dao.getDiscussions().length);
 
 		Stack<String> requestURLs = this.connector.requestURL;
 		String request = requestURLs.pop();
 		// get all the requests for comments from the stack
 		while (request.endsWith("cm:comments"))
 			request = requestURLs.pop();
 		assertEquals("pageSize=50", request.substring(request.length() - 11));
 
 		this.dao.setLimit(25);
 		assertEquals(25, this.dao.getLimit());
 		// The test fixture returns 50 items. But I need to query them to see if
 		// the query is correct.
 		assertEquals(50, this.dao.getDiscussions().length);
 		request = requestURLs.pop();
 		while (request.endsWith("cm:comments"))
 			request = requestURLs.pop();
 		assertEquals("pageSize=25", request.substring(request.length() - 11));
 
 		this.dao.setLimit(-5);
 		assertEquals(0, this.dao.getLimit());
		// The test fixture returns 50 items. But I need to query them to see if
		// the query is correct.
 		assertEquals(50, this.dao.getDiscussions().length);
 		request = requestURLs.pop();
 		while (request.endsWith("cm:comments"))
 			request = requestURLs.pop();
 		assertEquals("pageSize=0", request.substring(request.length() - 10));
 	}
 
 	@Test
 	public void testSimpleQuery() throws JDOMException, IOException, Exception {
 		this.dao.setProjectArea("Rational Team Concert");
 
 		String[] results = this.dao.getWorkitemsForType();
 		assertEquals(50, results.length);
 
 		// System.out.println(results[0]);
 
 		assertEquals("Not a correct xml fragment?", "<ChangeRequest resource=",
 				results[0].substring(0, 24));
 	}
 
 	@Test
 	public void testGetDiscussionEventsForDiscussion() throws DAOException {
 		this.dao.setProjectArea("Rational Team Concert");
 
 		DiscussionEvent[] des = this.dao
 				.getDiscussionEventsOfDiscussion(117709);
 		assertEquals(4, des.length);
 
 		// test if the ids are correct
 		for (int i = 0; i < des.length; i++)
 			assertEquals(i + ". element", i, des[i].getID());
 
 		assertEquals(117709, des[0].getDiscussionID());
 		assertEquals("jimtykal", des[0].getCreator());
 		assertEquals(new Date(1277806629252l), des[0].getCreationDate());
 		assertEquals(
 				"This is an important component of the sample application for Rational User Education. We will want to have lab exercises directed at .NET as well as Eclipse developers.",
 				des[0].getContent());
 		assertEquals(117709, des[1].getDiscussionID());
 		assertEquals("tfeeney", des[1].getCreator());
 		assertEquals(
 				"@sreerupa, once you get going on this, can we (sample asset/scenario team) get some early visibility into what you have planned? meetings with Ben/JM on the web/Java version have been very helpful in getting the app where it needs to be (or will be) for all stakeholders.",
 				des[1].getContent());
 
 		des = this.dao.getDiscussionEventsOfDiscussion(50162);
 		assertEquals(1, des.length);
 
 		assertEquals(50162, des[0].getDiscussionID());
 		assertEquals("csun", des[0].getCreator());
 		assertEquals("verified in m6.", des[0].getContent());
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void testStoreDiscussionEvent() throws DAOException {
 		this.dao.storeDiscussionEvent(new DiscussionEvent());
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void testStoreDiscussionEvents() throws DAOException {
 		this.dao.storeDiscussionEvents(new DiscussionEvent[] { new DiscussionEvent() });
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void testGetSpecificDiscussionEvents() throws DAOException {
 		this.dao.getDiscussionEvent(123);
 	}
 
 	@Test
 	public void testGetDiscussion() throws DAOException {
 		this.dao.setProjectArea("Rational Team Concert");
 
 		Discussion d = this.dao.getDiscussion(117709);
 
 		assertEquals(117709, d.getID());
 		assertEquals(Util.parseDate("2010-06-11T03:44:02.373Z"),
 				d.getCreationDate());
 		assertEquals("sreerupa", d.getCreator());
 		assertEquals(
 				"We'll look at the application scenario described in plan item 102112 and add some VS specific sources/component to it.<br/><br/>What we could do&nbsp; is to have a separate component altogether with a WPF based application in it that does client authentication or some simple UI work that fits into the application scenario.<br/><br/>This project will need to be install automatically in VS should the user choose to install the sample app. So we'd have to add some code to do that as well.<br/><br/>If we have something like this, @dcustic could use it in his tutorials as well. ",
 				d.getDescription());
 		assertEquals("Add VS projects to the sample app", d.getSummary());
 		assertEquals(
 				"https://jazz.net/jazz/oslc/types/_1w8aQEmJEduIY7C8B09Hyw/com.ibm.team.apt.workItemType.story",
 				d.getType());
 
 		// should also have the discussion events
 		DiscussionEvent[] des = d.getDiscussionEvents();
 		assertEquals(4, des.length);
 
 		assertEquals(117709, des[0].getDiscussionID());
 		assertEquals("jimtykal", des[0].getCreator());
 		assertEquals(new Date(1277806629252l), des[0].getCreationDate());
 		assertEquals(
 				"This is an important component of the sample application for Rational User Education. We will want to have lab exercises directed at .NET as well as Eclipse developers.",
 				des[0].getContent());
 	}
 
 	@Test
 	public void testGetDiscussions() throws DAOException {
 		this.dao.setProjectArea("Rational Team Concert");
 
 		Discussion[] d = this.dao.getDiscussions();
 		assertEquals(50, d.length);
 		assertEquals(117709, d[0].getID());
 		assertEquals(4, d[0].getDiscussionEvents().length);
 	}
 
 	@Test
 	public void testGetNextDiscussion() throws DAOException {
 		this.dao.setProjectArea("Rational Team Concert");
 
 		assertFalse(this.dao.hasMoreDiscussions());
 
 		Discussion[] d = this.dao.getDiscussions();
 		assertTrue(this.dao.hasMoreDiscussions());
 
 		this.dao.setProjectArea("Rational Team Concert");
 
 		ProgressMonitorProbe pm = new ProgressMonitorProbe();
 
 		// testframe gives the same file again
 		d = this.dao.getMoreDiscussions(pm);
 		assertEquals(50, d.length);
 
 		Discussion disc = d[0];
 		assertEquals("testframe gives the same file again", 117709,
 				disc.getID());
 
 		assertEquals("Adding DiscussionEvents", pm.message);
 		assertEquals(101, pm.step);
 		assertEquals(102, pm.totalSteps);
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void testStoreDiscussion() throws DAOException {
 		this.dao.storeDiscussion(DiscussionFactory.getInstance().getDiscussion(
 				-1));
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void testStoreDiscussions() throws DAOException {
 		this.dao.storeDiscussions(new Discussion[] { DiscussionFactory
 				.getInstance().getDiscussion(-1) });
 	}
 
 	private class ProgressMonitorProbe implements IDAOProgressMonitor {
 
 		int totalSteps = 0;
 		int step = 0;
 		String message = "";
 
 		@Override
 		public void setTotalSteps(int steps) {
 			this.totalSteps = steps;
 		}
 
 		@Override
 		public void setStep(int step) {
 			this.step = step;
 		}
 
 		@Override
 		public void setStep(int step, String message) {
 			this.step = step;
 			this.message = message;
 		}
 
 		@Override
 		public boolean isCancelled() {
 			return false;
 		}
 
 	}
 
 	private class ConnectorProbe implements IWebConnector {
 
 		HttpResponseDummy response = new HttpResponseDummy();
 		Stack<String> requestURL = new Stack<String>();
 
 		@Override
 		public HttpResponse performHTTPSRequestXML(String requestURL)
 				throws Exception {
 			if (requestURL == null) {
 				System.err.println("empty request");
 				return null;
 			}
 			this.requestURL.push(requestURL);
 			if (requestURL.endsWith("rootservices")) {
 				URL resource = getClass().getResource(
 						"/jazz.xml/rootservices.xml");
 				this.response.entity.stream = new FileInputStream(
 						resource.getFile());
 			} else if (requestURL.endsWith("catalog"))
 				this.response.entity.stream = new FileInputStream(getClass()
 						.getResource("/jazz.xml/catalog.xml").getFile());
 			else if (requestURL.endsWith("services.xml"))
 				this.response.entity.stream = new FileInputStream(getClass()
 						.getResource("/jazz.xml/services.xml").getFile());
 			else if (requestURL.endsWith("workitems")
 					|| requestURL.indexOf("query") > 0) {
 				// if this file is missing, store the results of the query under
 				// its name.
 				this.response.entity.stream = new FileInputStream(getClass()
 						.getResource("/jazz.xml/50-stories.xml").getFile());
 			} else if (requestURL.endsWith("comments")) {
 				// if this file is missing, store the comments for the first
 				// story in '50-stories.xml' into that file.
 				// System.out.println(requestURL);
 				if (requestURL
 						.equals("https://jazz.net/jazz/oslc/workitems/_IJb7oHULEd-GXMPQSbP08A/rtc_cm:comments"))
 					this.response.entity.stream = new FileInputStream(
 							getClass().getResource(
 									"/jazz.xml/117709-comments.xml").getFile());
 				else
 					this.response.entity.stream = new FileInputStream(
 							getClass().getResource(
 									"/jazz.xml/50162-comments.xml").getFile());
 
 			} else if (requestURL.endsWith("_startIndex=50")) {
 				// this is the more-query!;
 				this.response.entity.stream = new FileInputStream(getClass()
 						.getResource("/jazz.xml/50-stories.xml").getFile());
 			}
 			return response;
 		}
 
 		@Override
 		public void configure(Properties properties) {
 
 		}
 
 		@Override
 		public Properties getConfiguration() {
 			return null;
 		}
 
 		@Override
 		public Map<String, String> checkConfiguration() {
 			return null;
 		}
 
 	}
 
 	private class HttpResponseDummy extends BasicHttpResponse {
 
 		HttpEntityDummy entity = new HttpEntityDummy();
 
 		public HttpResponseDummy() {
 			super(new StatusLine() {
 
 				@Override
 				public int getStatusCode() {
 					return 0;
 				}
 
 				@Override
 				public String getReasonPhrase() {
 					return null;
 				}
 
 				@Override
 				public ProtocolVersion getProtocolVersion() {
 					return null;
 				}
 			});
 		}
 
 		@Override
 		public HttpEntity getEntity() {
 			return entity;
 		}
 
 		@Override
 		public void setEntity(HttpEntity entity) {
 			this.entity = (HttpEntityDummy) entity;
 		}
 
 	}
 
 	private class HttpEntityDummy implements HttpEntity {
 
 		InputStream stream;
 
 		@Override
 		public void consumeContent() throws IOException {
 
 		}
 
 		@Override
 		public InputStream getContent() throws IOException,
 				IllegalStateException {
 			return stream;
 		}
 
 		@Override
 		public Header getContentEncoding() {
 			return null;
 		}
 
 		@Override
 		public long getContentLength() {
 			return 0;
 		}
 
 		@Override
 		public Header getContentType() {
 			return null;
 		}
 
 		@Override
 		public boolean isChunked() {
 			return false;
 		}
 
 		@Override
 		public boolean isRepeatable() {
 			return false;
 		}
 
 		@Override
 		public boolean isStreaming() {
 			return false;
 		}
 
 		@Override
 		public void writeTo(OutputStream arg0) throws IOException {
 
 		}
 
 	}
 
 }
