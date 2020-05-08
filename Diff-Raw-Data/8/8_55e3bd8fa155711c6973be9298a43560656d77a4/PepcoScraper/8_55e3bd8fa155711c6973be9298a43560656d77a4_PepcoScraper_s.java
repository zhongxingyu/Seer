 package com.pocketcookies.pepco.scraper;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.StringFilter;
 import org.htmlparser.util.ParserException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.pocketcookies.pepco.model.AbstractOutageRevision;
 import com.pocketcookies.pepco.model.Outage;
 import com.pocketcookies.pepco.model.OutageClusterRevision;
 import com.pocketcookies.pepco.model.OutageRevision;
 import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
 import com.pocketcookies.pepco.model.ParserRun;
 import com.pocketcookies.pepco.model.Summary;
 import com.pocketcookies.pepco.model.dao.OutageDAO;
 
 public class PepcoScraper {
 	private static final String dataHTMLPrefix = "http://www.pepco.com/home/emergency/maps/stormcenter/data/outages/";
 	private static final Logger logger = Logger.getLogger(PepcoScraper.class);
 	private static final int maxZoom = 15;
 	private final String outagesFolder;
 	private final SessionFactory sessionFactory;
 	private final OutageDAO outageDao;
 	private final HttpClient client;
 	final ParserRun run;
 
 	/**
 	 * Used to record which indices we have scraped so we don't have to scrape
 	 * them again.
 	 */
 	private final Set<String> scrapedSpatialIndices = new TreeSet<String>();
 	/**
 	 * Once we are done scraping, we want to find out which outages have been
 	 * closed. Therefore, we use this list to keep track of all the outages we
 	 * have discovered so we can close them later.
 	 */
 	private final Set<Integer> discoveredOutageIds = new TreeSet<Integer>();
 
 	public PepcoScraper(final SessionFactory sessionFactory,
 			final OutageDAO outageDao, final HttpClient client)
 			throws ClientProtocolException, IllegalStateException, IOException,
 			SAXException, ParserConfigurationException,
 			FactoryConfigurationError {
 		this.outagesFolder = getOutagesFolderName();
 		this.sessionFactory = sessionFactory;
 		this.outageDao = outageDao;
 		this.client = client;
 		this.run = new ParserRun(new Timestamp(new Date().getTime()));
 		sessionFactory.getCurrentSession().save(run);
 	}
 
 	public String getOutagesFolderName() throws ClientProtocolException,
 			IOException, IllegalStateException, SAXException,
 			ParserConfigurationException, FactoryConfigurationError {
 		HttpGet get = new HttpGet(dataHTMLPrefix + "metadata.xml");
 		final HttpParams params = get.getParams();
 		params.setLongParameter("timestamp", new Date().getTime());
 		return DocumentBuilderFactory
 				.newInstance()
 				.newDocumentBuilder()
 				.parse(new DefaultHttpClient().execute(get).getEntity()
 						.getContent()).getElementsByTagName("directory")
 				.item(0).getFirstChild().getNodeValue();
 	}
 
 	/**
 	 * Here is a short description of how scraping will work.
 	 * 
 	 * We request the most zoomed out version of the data. Then we save all
 	 * outages to the database. Then, for each clustered outage, we that that
 	 * clustered outage's lat lon coords and request the next level of detail
 	 * for those coordinates.
 	 * 
 	 * @author John Edmonds
 	 */
 	private class Scraper implements Runnable {
 		public final String spatialIndex;
 		// Used for the cookie. Otherwise, we seem to get a 403.
 		public final double lat, lon;
 		// Used for the cookie. Otherwise, we seem to get a 403.
 		public final int zoom;
 
 		public Scraper(String spatialIndex, double lat, double lon, int zoom) {
 			super();
 			this.spatialIndex = spatialIndex;
 			this.lat = lat;
 			this.lon = lon;
 			this.zoom = zoom;
 		}
 
 		@Override
 		public void run() {
 			try {
 				if (this.zoom > maxZoom
 						|| scrapedSpatialIndices.contains(this.spatialIndex))
 					return;
 				scrapedSpatialIndices.add(this.spatialIndex);
 				logger.info("Scraping " + this.spatialIndex);
 				parse(makeRequest());
 			} catch (Exception e) {
 				logger.error("Error while attempting to download outage list.",
 						e);
 			}
 		}
 
 		private Document makeRequest() throws ClientProtocolException,
 				IOException, IllegalStateException, SAXException,
 				ParserConfigurationException, FactoryConfigurationError {
 			final HttpGet get = new HttpGet(dataHTMLPrefix + outagesFolder
 					+ "/" + spatialIndex + ".xml");
 			get.getParams().setLongParameter("timestamp", new Date().getTime());
 			final CookieStore cookies = new BasicCookieStore();
 			final HttpContext context = new BasicHttpContext();
 			context.setAttribute(ClientContext.COOKIE_STORE, cookies);
 			cookies.addCookie(new BasicClientCookie("pepscstate",
 					"map_type:r|homepage:" + lat + "," + lon + "," + zoom));
 			final HttpResponse response = client.execute(get);
 			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
 					.parse(response.getEntity().getContent());
 		}
 
 		/**
 		 * Parses the document.
 		 * 
 		 * @param doc
 		 * @throws ParserException
 		 * @throws ParseException
 		 */
 		private void parse(Document doc) throws ParserException, ParseException {
 			final SimpleDateFormat dateParser = getPepcoDateFormat();
 			final NodeList list = doc.getElementsByTagName("item");
 			for (int i = 0; i < list.getLength(); i++) {
 				final Element el = (Element) list.item(i);
 				final Polygon location;
 				final PointDouble center;
 				// Find the lat/lon (or polygon).
 				if (el.getElementsByTagName("georss:point").getLength() == 0)
 					location = new Polygon(el
 							.getElementsByTagName("georss:polygon").item(0)
 							.getFirstChild().getNodeValue());
 				else
 					location = new Polygon(el
 							.getElementsByTagName("georss:point").item(0)
 							.getFirstChild().getNodeValue());
 				center = location.getCenter();
 
 				// Parse the description.
 				final int numCustomersAffected;
 				final Date earliestReport;
 				final Date estimatedRestoration;
 				final org.htmlparser.util.NodeList descList = Parser
 						.createParser(
 								"<body>" // Make sure nodes get a parent.
 										+ el.getElementsByTagName("description")
 												.item(0).getFirstChild()
 												.getNodeValue() + "</body>",
 								null).parse(null).elementAt(0).getChildren();
 				final String sNumCustomersAffected = descList
 						.extractAllNodesThatMatch(
 								new StringFilter("Customers Affected:"))
 						.elementAt(0).getNextSibling().getNextSibling()
 						.getText().trim();
 				numCustomersAffected = sNumCustomersAffected
 						.equals("Less than 5") ? 0 : Integer
 						.parseInt(sNumCustomersAffected);
 				earliestReport = dateParser.parse(descList
 						.extractAllNodesThatMatch(new StringFilter("Report"))
 						.elementAt(0).getNextSibling().getNextSibling()
 						.getText().trim());
 				estimatedRestoration = dateParser.parse(descList
 						.extractAllNodesThatMatch(
 								new StringFilter("Restoration")).elementAt(0)
 						.getNextSibling().getNextSibling().getText().trim());
 
 				// Load the outage.
 				final Outage existingOutage = outageDao.getActiveOutage(
 						center.lat, center.lon);
 				final Outage outage;
 				if (existingOutage == null)
 					outage = new Outage(center.lat, center.lon, new Timestamp(
 							earliestReport.getTime()), null);
 				else
 					outage = existingOutage;
 				final AbstractOutageRevision revision;
 				// Check if this is an Outage or an OutageCluster. This will
 				// determine whether we create an OutageClusterRevision or an
 				// Outage revision. Each of these has different fields which we
 				// will need to parse.
 				if (el.getElementsByTagName("title").item(0).getFirstChild()
 						.getNodeValue().contains("outagesclusters")) {
 					final int numOutages;
 					numOutages = Integer
 							.parseInt(descList
 									.extractAllNodesThatMatch(
 											new StringFilter(
 													"Number of Outage Orders:"))
 									.elementAt(0).getNextSibling()
 									.getNextSibling().getText().trim());
 					revision = new OutageClusterRevision(numCustomersAffected,
 							new Timestamp(estimatedRestoration.getTime()),
 							new Timestamp(new Date().getTime()), outage, run,
 							numOutages);
 				} else {
 					final String cause;
 					final CrewStatus status;
 					cause = descList
 							.extractAllNodesThatMatch(new StringFilter("Cause"))
 							.elementAt(0).getNextSibling().getNextSibling()
 							.getText().trim();
 					status = CrewStatus.valueOf(descList
 							.extractAllNodesThatMatch(
 									new StringFilter("Crew Status"))
 							.elementAt(0).getNextSibling().getNextSibling()
 							.getText().trim().replace(' ', '_').toUpperCase());
 					revision = new OutageRevision(numCustomersAffected,
 							new Timestamp(estimatedRestoration.getTime()),
 							new Timestamp(new Date().getTime()), outage, run,
 							cause, status);
 
 				}
				// Record that we have encountered this outage.
				discoveredOutageIds.add(outage.getId());
 				// If the outage already existed, there is no need to save it
 				// again--indeed, that would probably cause a crash.
 				if (outage != existingOutage) {
 					sessionFactory.getCurrentSession().save(outage);
 				}
 				if (outage.getRevisions().isEmpty()
 						|| !outage.getRevisions().get(0)
 								.equalsIgnoreObservationDate(revision))
 					sessionFactory.getCurrentSession().save(revision);
 				sessionFactory.getCurrentSession().flush();
 				// We want to make sure that when we load an outage, it has all
 				// its revisions with it. Evicting the outage forces Hibernate
 				// to re-load it later which means it will have all its
 				// revisions in the proper order.
 				sessionFactory.getCurrentSession().evict(outage);
 				// If this is a clustered outage, try to take a closer look
 				// and
 				// see if we can get the individual outages.
 				for (final String spatialIndex : getSpatialIndicesForPoint(
 						center.lat, center.lon, zoom + 1))
 					new Scraper(spatialIndex, center.lat, center.lon, zoom + 1)
 							.run();
 			}
 		}
 	}
 
 	public Collection<String> getSpatialIndicesForPoint(double lat, double lon,
 			double zoom) {
 		double minLat = -85.05112878;
 		double maxLat = 85.05112878;
 		double minLong = -180;
 		double maxLong = 180;
 		String indexName = "";
 		int A = 0;
 		int curZoom = 0;
 		int D = 0;
 		Collection<String> indexNames = new LinkedList<String>();
 		String[] zoomLevels = new String[] { "0", "1", "2", "3" };
 		for (curZoom = 0; curZoom < zoom; curZoom++) {
 			A = 0;
 			if (lat < ((maxLat + minLat) / 2)) {
 				A = A + 2;
 				maxLat = (maxLat + minLat) / 2;
 			} else {
 				minLat = (maxLat + minLat) / 2;
 			}
 			if (lon > ((maxLong + minLong) / 2)) {
 				A = A + 1;
 				minLong = (maxLong + minLong) / 2;
 			} else {
 				maxLong = (maxLong + minLong) / 2;
 			}
 			indexName = indexName + A;
 		}
 		if (indexName.length() > 1) {
 			indexName = indexName.substring(0, indexName.length() - 2);
 			for (curZoom = 0; curZoom < zoomLevels.length; curZoom++) {
 				for (D = 0; D < zoomLevels.length; D++) {
 					indexNames.add(indexName + zoomLevels[curZoom]
 							+ zoomLevels[D]);
 				}
 			}
 		} else {
 			indexNames = Arrays.asList(zoomLevels);
 		}
 		return indexNames;
 	};
 
 	@SuppressWarnings("unchecked")
 	public void scrape() {
 		scrapeSummary(sessionFactory, run);
 		for (final String spatialIndex : getSpatialIndicesForPoint(
 				38.96000000000001, -77.02999999999999, 7)) {
 			new Scraper(spatialIndex, 38.96000000000001, -77.02999999999999, 7)
 					.run();
 		}
 		// We want to make sure that we record when outages disappear.
 		// Therefore, we will use the list of outages we discovered and check
 		// whether there are any active outages which we did not discover during
 		// this run.
 		for (final Outage o : (List<Outage>) (this.sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Outage where id not in (:ids) and observedEnd=null")
 				.setParameterList("ids", discoveredOutageIds).list())) {
 			o.setObservedEnd(new Timestamp(new Date().getTime()));
 			this.sessionFactory.getCurrentSession().save(o);
 		}
 	}
 
 	private void scrapeSummary(final SessionFactory sessionFactory,
 			final ParserRun run) {
 		final HttpGet get = new HttpGet(
 				"http://www.pepco.com/home/emergency/maps/stormcenter/data/data.xml");
 		get.getParams().setLongParameter("timestamp", new Date().getTime());
 		try {
 			final HttpResponse response = client.execute(get);
 			try {
 				final Document doc = DocumentBuilderFactory.newInstance()
 						.newDocumentBuilder()
 						.parse(response.getEntity().getContent());
 				final Timestamp whenGenerated = new Timestamp(
 						getPepcoDateFormat()
 								.parse(doc
 										.getElementsByTagName("date_generated")
 										.item(0).getFirstChild().getNodeValue())
 								.getTime());
 				final NodeList areas = doc.getElementsByTagName("area");
 				final Element dc = (Element) areas.item(0), pg = (Element) areas
 						.item(1), mont = (Element) areas.item(2);
 				// Customers out
 				final int dcOut = getCustomersOut(dc), pgOut = getCustomersOut(pg), montOut = getCustomersOut(mont);
 				// Total customers
 				final int dcTot = getTotalCustomers(dc), pgTot = getTotalCustomers(pg), montTot = getTotalCustomers(mont);
 				final int totalOutages = Integer.parseInt(doc
 						.getElementsByTagName("total_outages").item(0)
 						.getFirstChild().getNodeValue());
 				sessionFactory.getCurrentSession().save(
 						new Summary(totalOutages, dcOut, dcTot, pgOut, pgTot,
 								montOut, montTot, whenGenerated, run));
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				// Make sure we release the connection.
 				EntityUtils.consume(response.getEntity());
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private int getCustomersOut(Element el) {
 		return Integer.parseInt(el.getElementsByTagName("custs_out").item(0)
 				.getFirstChild().getNodeValue());
 	}
 
 	private int getTotalCustomers(Element el) {
 		return Integer.parseInt(el.getElementsByTagName("total_custs").item(0)
 				.getFirstChild().getNodeValue());
 	}
 
 	private SimpleDateFormat getPepcoDateFormat() {
 		return new SimpleDateFormat("MMM dd, h:mm a");
 	}
 
 	public static void main(final String[] args)
 			throws ClientProtocolException, IOException, IllegalStateException,
 			SAXException, ParserConfigurationException,
 			FactoryConfigurationError, ParserException {
 		final SessionFactory sessionFactory = new Configuration().configure()
 				.buildSessionFactory();
 		sessionFactory.getCurrentSession().beginTransaction();
 		new PepcoScraper(sessionFactory, new OutageDAO(sessionFactory),
 				new DefaultHttpClient()).scrape();
 		sessionFactory.getCurrentSession().getTransaction().commit();
 		sessionFactory.getCurrentSession().close();
 		sessionFactory.close();
 	}
 
 	/**
 	 * Represents a 2D coordinate using double precision.
 	 * 
 	 * @author jack
 	 * 
 	 */
 	private static class PointDouble {
 		public final double lat, lon;
 
 		public PointDouble(double lat, double lon) {
 			super();
 			this.lat = lat;
 			this.lon = lon;
 		}
 
 		/**
 		 * Takes a lat,lon separate by the space character (' ').
 		 * 
 		 * @param s
 		 *            Latitude Longitude separated by the space character (' ').
 		 */
 		public PointDouble(final String s) {
 			final StringTokenizer st = new StringTokenizer(s);
 			this.lat = Double.parseDouble(st.nextToken());
 			this.lon = Double.parseDouble(st.nextToken());
 		}
 
 		@Override
 		public String toString() {
 			return lat + " " + lon;
 		}
 	}
 
 	private static class Polygon {
 		public final Collection<PointDouble> vertices;
 
 		public Polygon(final String s) {
 			final StringTokenizer st = new StringTokenizer(s);
 			if (st.countTokens() % 2 != 0)
 				throw new IllegalArgumentException(
 						"Polygon must be constructed with a list of lat, lon points separated by spaces.  It looks like the last point may be missing a longitude.");
 			vertices = new ArrayList<PointDouble>(st.countTokens() / 2);
 			while (st.hasMoreTokens()) {
 				vertices.add(new PointDouble(
 						Double.parseDouble(st.nextToken()), Double
 								.parseDouble(st.nextToken())));
 			}
 		}
 
 		public PointDouble getCenter() {
 			double lat = 0, lon = 0;
 			for (final PointDouble p : vertices) {
 				lat += p.lat;
 				lon += p.lon;
 			}
 			lat /= vertices.size();
 			lon /= vertices.size();
 			return new PointDouble(lat, lon);
 		}
 
 		@Override
 		public String toString() {
 			return StringUtils.join(vertices, ' ');
 		}
 	}
 }
