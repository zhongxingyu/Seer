 package eu.choreos.roles;
 
 import static org.junit.Assert.*;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import eu.choreos.utils.RunWS;
 
 import br.usp.ime.choreos.vv.Item;
 import br.usp.ime.choreos.vv.WSClient;
 
 public class SupermarketImplementationTest {
 	
 	@BeforeClass
 	public static void setUp(){
 		RunWS.startFutureMartWS();
 	}
 	
 	@AfterClass
 	public static void tearDown(){
		RunWS.stopSupermarketCustomerWS();
 	}
 
 	@Test
 	public void futureMartShouldPlayTheSupermarketRole() throws Exception {
		WSClient futureMartWS = new WSClient("http://192.168.32.102:8084/petals/services/futureMart?wsdl");
 		Item response = futureMartWS.request("searchForProduct", "milk");
 		Item product = response.getChild("return");
 		assertEquals("milk", product.getChild("name").getContent());
 		assertEquals(new Double(4.79), product.getChild("price").getContentAsDouble());
 	}
 
 }
