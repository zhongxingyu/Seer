 package CIAPI.Java.examples;
 
import static org.junit.Assert.assertTrue;

 import org.junit.Before;
 import org.junit.Test;
 
 import CIAPI.Java.ApiException;
import CIAPI.Java.examples.ciapi.SyncApi;
 import CIAPI.Java.examples.ciapi.ApiFActory;
 
 public class TestSynchCIAPI {
 
 	private SyncApi api;
 
 	@Before
 	public void setUp() throws Exception {
		api = new ApiFActory().getApi();
 	}
 
 	public void tearDown() throws Exception {
 		api = null;
 	}
 
 	public void testLogOn() throws ApiException {
 		//api.logIn("", "", true);
 		//assertTrue(api.isLoggedOn());
 	}
 
 	public void testLogOnAndOff() throws ApiException {
 		// assertTrue(api.isLoggedOn());
 		// assertTrue(!api.isLoggedOn());
 	}
 
 	@Test
 	public void testGetClientAndTradingAccount() throws ApiException {
 	}
 }
