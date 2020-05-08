 package org.paxle.filter.robots.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 import junitx.framework.ListAssert;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.Element;
 
 import org.paxle.filter.robots.impl.rules.RobotsTxt;
 import org.paxle.filter.robots.impl.store.FileStore;
 
 public class RobotsTxtManagerTest extends TestCase {
 	
 	private RobotsTxtManager manager = null;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		this.manager = new RobotsTxtManager(new FileStore(new File("target/temp")));
 	}
 
 	protected void tearDown() throws Exception {
 		this.manager.terminate();
 		
 		super.tearDown();
 	}
 	
 	private RobotsTxt getRobotsTxt(File robotsTxtFile) throws IOException {
 		assertTrue(robotsTxtFile.exists());
 		assertTrue(robotsTxtFile.canRead());
 		
 		InputStream fileInput = null;
 		try {
 			fileInput = new FileInputStream(robotsTxtFile);
 			RobotsTxt robotsTxt = new RobotsTxt("host:port", RobotsTxt.RELOAD_INTERVAL_DEFAULT, "200 OK");
 			return this.manager.parseRobotsTxt(robotsTxt, fileInput); 
 		} finally {
 			if (fileInput != null) try { fileInput.close(); } catch (Exception e) {/* ignore this */}
 		}
 	}
 	
 	/**
 	 * Function to parse a robots.txt from {@link File} and to place this {@link RobotsTxt} into
 	 * the internal manager-cache.
 	 * 
 	 * @param robotsTxtFileName name of the robots.txt file
 	 * @param hostnamePort the hostname-port to use to insert the element into the {@link RobotsTxtManager#getCache() cache}.
 	 * @throws IOException 
 	 */
 	private void parseAndPutIntoCache(String robotsTxtFileName, String hostnamePort) throws IOException {
 		File robotsTxtFile = new File(robotsTxtFileName);
 		assertTrue(robotsTxtFile.exists());
 		assertTrue(robotsTxtFile.canRead());
 		
 		RobotsTxt rtxt = this.getRobotsTxt(robotsTxtFile);
 		assertNotNull(rtxt);
 
 		// append it to the cache to avoid real downloading of the robots.txt file
 		Element e = new Element(hostnamePort,rtxt);
 		this.manager.getCache().put(e);
 	}
 	
 	public void testCache() {
 		Cache cache = this.manager.getCache();
 		assertNotNull(cache);
 		assertEquals(0, cache.getSize());
 		
 		String hostport = "test";
 		RobotsTxt rtxt = new RobotsTxt(hostport,-1,"OK");
 		Element rtxtE = new Element(hostport,rtxt);
 		cache.put(rtxtE);
 		assertEquals(1, cache.getSize());
 		
 		rtxtE = cache.get(hostport);
 		assertNotNull(rtxtE);
 		assertEquals(hostport, rtxtE.getKey());
 		rtxt = (RobotsTxt) rtxtE.getValue();
 		assertNotNull(rtxt);
 	}
 	
 	public void _testDownloadAndParserRobotsTxt() throws IOException, URISyntaxException {
 		String uri = "http://www.hibernate.org/robots.txt";
 		RobotsTxt rtxt = this.manager.getFromWeb(URI.create(uri));
 		assertNotNull(rtxt);
 	}
 	
 	public void testParseRobotsTxt() throws IOException {
 		File robotsTxtFile = new File("src/test/resources/robots.txt");
 		RobotsTxt rtxt = this.getRobotsTxt(robotsTxtFile);
 		assertNotNull(rtxt);
 	}
 	
 	public void testParseRobotsTxtWithEmptyAgent() throws IOException {
 		File robotsTxtFile = new File("src/test/resources/robots2.txt");
 		RobotsTxt rtxt = this.getRobotsTxt(robotsTxtFile);
 		assertNotNull(rtxt);
 		assertEquals(2, rtxt.size());
 		assertNotNull(rtxt.getRuleBlock(1));
 		assertEquals(1, rtxt.getRuleBlock(1).agentsCount());
 	}
 	
 	
 	public void testIsDisallowed() throws IOException {
 		String hostPort = "xxxxx:80";
 		this.parseAndPutIntoCache("src/test/resources/robots.txt",hostPort);
 		
 		// check disallowed
 		boolean disallowed = this.manager.isDisallowed("http://xxxxx/");
 		assertFalse(disallowed);
 		
 		disallowed = this.manager.isDisallowed("http://xxxxx/secret");
 		assertFalse(disallowed);
 		
 		disallowed = this.manager.isDisallowed("http://xxxxx/secret/");
 		assertTrue(disallowed);
 	}
 	
 	public void testIsDisallowedBlock() throws IOException {
 		String hostPort = "xxxxx:80";
 		this.parseAndPutIntoCache("src/test/resources/robots.txt",hostPort);
 		
 		ArrayList<URI> uriList = new ArrayList<URI>(Arrays.asList(new URI[]{
 			URI.create("http://xxxxx/"),
 			URI.create("http://xxxxx/secret"),
 			URI.create("http://xxxxx/secret/")
 		}));
 		
 		// check disallowed
 		List<URI> disallowedURIs = this.manager.isDisallowed(uriList);
 		assertNotNull(disallowedURIs);
 		assertEquals(1,disallowedURIs.size());
 		ListAssert.assertContains(disallowedURIs, URI.create("http://xxxxx/secret/"));
 	}
 	
 	public void testGetSitemaps() throws IOException {
 		String hostPort = "xxxxx:80";
 		this.parseAndPutIntoCache("src/test/resources/robots3.txt",hostPort);
 		
 		// expected list of sitemaps
 		ArrayList<URI> expectedSiteMapList = new ArrayList<URI>(Arrays.asList(new URI[]{
 				URI.create("http://www.heise.de/sitemap_softwarevz.xml.gz"),
 				URI.create("http://www.heise.de/handysitemap.xml.gz")
 		}));
 		
 		// get the sitemaps
 		Collection<URI> sitemaps = this.manager.getSitemaps("http://" + hostPort);
 		assertNotNull(sitemaps);
 		assertEquals(2, sitemaps.size());
 		
 		ArrayList<URI> actualSitemaps = new ArrayList<URI>(sitemaps);
 		for (URI sitemap : expectedSiteMapList) {
 			ListAssert.assertContains(actualSitemaps, sitemap);
 		}
 	}
 	
 	public void testGetProperties() throws IOException {
 		String hostPort = "xxxxx:80";
 		this.parseAndPutIntoCache("src/test/resources/robots4.txt",hostPort);
 		
 		Map<String,String> props = this.manager.getRobotsProperties("http://" + hostPort);
 		assertNotNull(props);
 		assertEquals(1, props.size());
 		assertTrue(props.containsKey("Crawl-Delay"));
 	}
 	
 	public void testGetProperties2() throws IOException {
 		String hostPort = "xxxxx:80";
 		this.parseAndPutIntoCache("src/test/resources/robots5.txt",hostPort);
 		
 		Map<String,String> props = this.manager.getRobotsProperties("http://" + hostPort);
 		assertNotNull(props);
 		assertEquals(0, props.size());
 	}
 	
 	/**
 	 * Testing if a Crawl-Delay defined as float is read and rounded properly 
 	 * @throws IOException 
 	 */
 	public void testReatFloatCrawlDelay() throws IOException {
 		String hostPort = "xxxxx:80";
 		this.parseAndPutIntoCache("src/test/resources/robots6.txt",hostPort);
 		
 		Map<String,String> props = this.manager.getRobotsProperties("http://" + hostPort);
 		assertNotNull(props);
 		assertEquals(1, props.size());
 		assertTrue(props.containsKey("Crawl-Delay"));
 		assertEquals("3", props.get("Crawl-Delay"));
 	}
 }
