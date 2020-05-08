 package org.xbrlapi.loader.tests;
 
 import java.net.URL;
 
 import org.xbrlapi.data.dom.tests.BaseTestCase;
 import org.xbrlapi.loader.discoverer.Discoverer;
 
 /**
  * Test the loader implementation.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class AsyncLoaderImplTestCase extends BaseTestCase {
 	
 	private final String STARTING_POINT = "test.data.small.schema";
 	private final String STARTING_POINT_2 = "test.data.small.instance";
 	private URL url1 = null;
 	private URL url2 = null;
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		url1 = new URL(getURL(this.STARTING_POINT));
 		url2 = new URL(getURL(this.STARTING_POINT_2));
 	}
 	
 	public AsyncLoaderImplTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * Test discovery given an XBRL instance as a starting point.
 	 */
 	public void testInterruption() {
 		try {
             loader.stashURL(this.url1);
             loader.stashURL(this.url2);
             
             Discoverer d1 = new Discoverer(loader);
             Thread t1 = new Thread(d1);
             t1.start();
 
             Thread.sleep(200);
             loader.requestInterrupt();
             
             while (t1.isAlive()) {
                 Thread.sleep(100);
             }
             loader.storeDocumentsToAnalyse();
             assertTrue(store.getStoredURLs().size() < 14);
             assertTrue(store.getDocumentsToDiscover().size() > 0);
 			loader.discover();
			assertTrue(store.getStoredURLs().size() > 14);
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 	
 }
