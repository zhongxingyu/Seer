 package org.xbrlapi.loader.tests;
 
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xbrlapi.Fragment;
 import org.xbrlapi.data.dom.tests.BaseTestCase;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.loader.LoaderImpl;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Test the loader implementation.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class LoaderImplTestCase extends BaseTestCase {
 	
 	private final String STARTING_POINT = "test.data.small.schema";
 	private final String STARTING_POINT_2 = "test.data.small.instance";
 	private URL url1 = null;
 	private URL url2 = null;
 	private List<URL> urls = new LinkedList<URL>();
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		url1 = new URL(getURL(this.STARTING_POINT));
 		url2 = new URL(getURL(this.STARTING_POINT_2));
 		urls.add(url1);
 		urls.add(url2);
 /*		loader.discover(this.getURL(STARTING_POINT));		
 		loader.discover(this.getURL(STARTING_POINT_2));		
 */	}
 	
 	public LoaderImplTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * Test the ability to create a loader given a 
 	 * null data store and a XLink Processor.
 	 * Class under test for void LoaderImpl(Store, XLinkProcessor)
 	 */
 	public void testLoaderImplStoreXLinkProcessor_FailOnNullStore() throws Exception {
 		try {
 			Loader l = new LoaderImpl(null,loader.getXlinkProcessor());
 			fail("Null data stores are not allowed for loaders.");
 		} catch (XBRLException expected) {
 			;
 		}
 	}
 
 	/**
 	 * Test the ability to create a loader given a 
 	 * data store and a null XLink Processor.
 	 * Class under test for void LoaderImpl(Store, XLinkProcessor)
 	 */
 	public void testLoaderImplStoreXLinkProcessor_FailOnNullXLinkProcessor() {
 		try {
 			Loader l = new LoaderImpl(store,null);
 			fail("Null XLink processors are not allowed for loaders.");
 		} catch (XBRLException expected) {
 			;
 		}
 	}
 
 	/**
 	 * Tests the creation of loaders with empty and populated lists
 	 * Class under test for void LoaderImpl(Store, XLinkProcessor, List).
 	 */
 	public void testLoaderImplStoreXLinkProcessorList() {
 		try {
 			Loader l = new LoaderImpl(store,loader.getXlinkProcessor(),urls);
 			Loader l2 = new LoaderImpl(store,loader.getXlinkProcessor(),new LinkedList<URL>());
 		} catch (XBRLException e) {
 			fail("Unexpected exception creating a loader using a list of URLs.");
 		}
 	}
 
 	public void testLoaderImplStoreXLinkProcessorList_FailOnNullList() {
 		try {
 			List<URL> list = null;//URL right?
 			Loader l = new LoaderImpl(store,loader.getXlinkProcessor(),list);
 			fail("Null list is not allowed for loaders.");
 		} catch (XBRLException expected) {
 			;
 		}
 	}
 	
 	/**
 	 * Test the ability to get the store being used by the loader.
 	 *
 	 */
 	public void testGetStore() {
 		try {
 			Loader l = new LoaderImpl(store,loader.getXlinkProcessor());
 			assertEquals(store,l.getStore());
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	/**
 	 * Test the ability to get the Xlink processor
 	 * used by the loader.
 	 */
 	public void testGetXlinkProcessor() {
 		try {
 			Loader l = new LoaderImpl(store,loader.getXlinkProcessor());
 			assertEquals(loader.getXlinkProcessor(),l.getXlinkProcessor());
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test discovery given an array of URL starting points
 	 */
 	public void testDiscover() {
 		try {
 			loader.stashURL(url1);
 			loader.discover();
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Test discovery given an XBRL instance as a starting point.
 	 */
 	public void testInstanceDiscover() {
 		try {
 			loader.stashURL(url1);
 			loader.stashURL(url2);
 			loader.discover();
			assertTrue(store.getStoredURLs().size() > 10);
 		} catch (XBRLException e) {
 		    e.printStackTrace();
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test discovery of a list of URLs passed in
 	 * as arguments to the discover method.
 	 */
 	public void testDiscoverURLList() {
 		try {
 			loader.discover(urls);
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}		
 	}
 
 	/**
 	 * Test discovery given an array of URL starting points
 	 */
 	public void testProcessSchemaLocationAttributes() {
 		try {
 			loader.setSchemaLocationAttributeUsage(true);
 			loader.discover(urls);
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Test discovery one URL at a time
 	 */
 	public void testDiscoverNext() {
 		try {
 			loader.stashURL(url1);
 			loader.stashURL(url2);
 			while (! loader.getDocumentsStillToAnalyse().isEmpty()) {
 				for (String document: loader.getDocumentsStillToAnalyse()) {
 					logger.debug("still to process " + document);
 				}
 				loader.discoverNext();
 			}
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 	
 	
 	/** 
 	 * Test null return from a call to get a fragment from a loader
 	 * where no data has been loaded.
 	 */
 	public void testGetFragmentWhenNoFragmentsAreAvailable() {
 		try {
 			Fragment f = loader.getFragment();
 			assertNull(f);
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	/**
 	 * Test loading of a serialised data set from a serialised
 	 * version of the data in the data set available at the
 	 * specified file.
 	 */
 	public void testLoadFile() {
 		//TODO Implement and test the loading of a serialised data store from a File.
 	}
 
 	/**
 	 * Test loading of a serialised data set from a serialised
 	 * version of the data in the data set available at the
 	 * specified URL.
 	 */
 	public void testLoadURL() {
 		//TODO Implement and test the loading of a serialised DTS from a URL.
 	}
 
 }
