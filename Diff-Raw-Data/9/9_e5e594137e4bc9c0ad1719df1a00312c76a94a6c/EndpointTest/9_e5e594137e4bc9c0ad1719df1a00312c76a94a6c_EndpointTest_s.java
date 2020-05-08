 package gov.usgs.service;
 
 import gov.usgs.service.Endpoint.EndpointType;
 import java.net.URL;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.MatcherAssert.*;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class EndpointTest {
     
     public EndpointTest() {
     }
 
     private static Endpoint testpoint;
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
		testpoint = new Endpoint("http://internal.cida.usgs.gov/gdp/process/WebProcessingService?service=WPS&request=GetCapabilities");
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
		testpoint = null;
 	}
 
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getURL method, of class Endpoint.
      */
     @Test
     public void testGetURL() {
         String expResult = "http://someogcserver.com/wms/service?request=getcapabilities";
         Endpoint instance = new Endpoint(expResult);
         String result = instance.getURL();
         assertThat(expResult, is(equalTo(result)));
     }
 
     /**
      * Test of getType method, of class Endpoint.
      */
     @Test
     public void testGetType() {
         EndpointType expResult = EndpointType.WPS;
         EndpointType result = testpoint.getType();
         assertThat(expResult, is(equalTo(result)));
     }
 
     /**
      * Test of getComparisonString method, of class Endpoint.
      */
     @Test
     public void testGetComparisonString() {
         String url = "http://someogcserver.com/wms/service?request=getcapabilities";
         Endpoint instance = new Endpoint(url);
         String expResult = "http://someogcserver.com/wms/";
         String result = instance.getComparisonString();
         assertThat(expResult, is(equalTo(result)));
     }
     
     /**
      * Test of getComparisonString method, of class Endpoint.
      */
     @Test(expected=UnsupportedOperationException.class)
     public void testBadUrl() {
         String url = "someogcserver.com";
         Endpoint instance = new Endpoint(url);
         //String expResult = "";
         //String result = instance.getComparisonString();
         //assertThat(expResult, is(equalTo(result)));
     }
 
     /**
      * Test of generateGetCapabilitiesURL method, of class Endpoint.
      */
     @Test
     public void testGenerateGetCapabilitiesURL() throws Exception {
         String url = "http://someogcserver.com/wms/service?request=GetMap";
         Endpoint instance = new Endpoint(url);
         URL expResult = new URL("http://someogcserver.com/wms/service?request=GetCapabilities&service=WMS");
         URL result = instance.generateGetCapabilitiesURL();
         assertThat(expResult, is(equalTo(result)));
     }
 
     /**
      * Test of equals method, of class Endpoint.
      */
     @Test
     public void testEquals_Endpoint() {
         Endpoint other = new Endpoint("http://someserver.org/wcs/service?request=DescribeCoverage");
         Endpoint instance = new Endpoint("http://someserver.org/wcs/service?request=GetCoverage");;
         boolean result = instance.equals(other);
         assertThat(result, is(true));
     }
 
     /**
      * Test of hashCode method, of class Endpoint.
      */
     @Test
     public void testHashCode() {
         String url = "http://someogcserver.com/wms/service?request=GetMap";
         Endpoint instance = new Endpoint(url);
         int expResult = 1494733094;
         int result = instance.hashCode();
         assertThat(expResult, is(equalTo(result)));
     }
 
 	/**
 	 * Test of getComparisonString method, of class Endpoint.
 	 */ @Test
 	public void testCreateComparisonString() {
 		System.out.println("createComparisonString");
 		String expResult = "http://internal.cida.usgs.gov/gdp/process/";
 		String result = testpoint.getComparisonString();
 		assertThat(expResult, is(equalTo(result)));
 		testpoint = new Endpoint("http://internal.cida.usgs.gov/gdp/process/ServletServlet");
 		result = testpoint.getComparisonString();
 		assertThat(expResult, is(equalTo(result)));
 		testpoint = new Endpoint("http://internal.cida.usgs.gov/gdp/process/Servlet?query=with/slashes");
 		result = testpoint.getComparisonString();
 		assertThat(expResult, is(equalTo(result)));
 	}
 
 	/**
 	 * Test of equals method, of class Endpoint.
 	 */
      @Test
 	public void testEquals() {
 		System.out.println("equals");
 		Endpoint other = new Endpoint("http://internal.cida.usgs.gov/gdp/process/WebProcessingService?service=WPS&request=DescribeProcess&id=Algorithm1");
 		boolean expResult = true;
 		boolean result = testpoint.equals(other);
 		assertThat(expResult, is(equalTo(result)));
 	}
 
 	/**
 	 * Test of equals method, of class Endpoint.
 	 */ @Test
 	public void testEqualsFalse() {
 		System.out.println("equalsFalse");
 		Endpoint other = new Endpoint("http://internal.cida.usgs.gov/gdp/utility/WebProcessingService?Service=WPS&Request=GetCapabilities");
 		boolean expResult = false;
 		boolean result = testpoint.equals(other);
 		assertThat(expResult, is(equalTo(result)));
 	}
 }
