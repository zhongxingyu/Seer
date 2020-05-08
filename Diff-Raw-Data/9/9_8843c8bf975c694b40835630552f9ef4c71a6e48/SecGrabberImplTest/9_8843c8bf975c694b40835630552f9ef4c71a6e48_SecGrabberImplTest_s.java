 package org.xbrlapi.grabber.tests;
 
 import java.net.URI;
 import java.util.List;
 
 import org.xbrlapi.data.dom.tests.BaseTestCase;
 import org.xbrlapi.grabber.Grabber;
 import org.xbrlapi.grabber.SecGrabberImpl;
 
 public class SecGrabberImplTest extends BaseTestCase {
 
     public SecGrabberImplTest(String arg0) {
         super(arg0);
     }
     
     private List<URI> resources = null;
 	protected void setUp() throws Exception {
 		super.setUp();
         URI feedURI = getURI("test.data.local.sec");             
         Grabber grabber = new SecGrabberImpl(feedURI);
         resources = grabber.getResources();
         logger.info("# resources = " + resources.size());
        assertTrue(resources.size() > 400);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public void testSecGrabberResourceRetrieval() {
 		try {
 
 			long start = System.currentTimeMillis();
 			for (URI resource: resources) {
 				if (! loader.getStore().hasDocument(resource))
 				loader.discover(resource);
 				System.out.println("Time taken = " + ((System.currentTimeMillis() - start) / 1000));
 				if (loader.getStore().getDocumentURIs().size() > 3) {
 				    break;
 				}
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("An unexpected exception was thrown.");
 		}
 	}	
 	
 }
