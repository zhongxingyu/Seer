 package gov.usgs.cida.harri.commons.datamodel;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  *
  * @author isuftin
  */
 public class TomcatTest {
	private String resultString = "{\"managerUsername\":\"test-user\",\"httpPort\":8080,\"httpsPort\":8443,\"appList\":[\"test\"],\"applicationMap\":{\"testApp\":{\"context\":\"test\",\"startTime\":\"1\",\"startupTime\":\"2\",\"running\":true}}}";
 	public TomcatTest() {
 	}
 
 	@Test
 	public void testToJSON() {
 		Tomcat input = new Tomcat();
 		Map<String,ApplicationInfo> appMap = new HashMap<String, ApplicationInfo>();
 		appMap.put("testApp", new ApplicationInfo("test", "1", "2", Boolean.TRUE));
 		
 		List<String> al = new ArrayList<String>();
 		al.add("test");
 		
 		input.setAppList(al);
 		input.setApplicationMap(appMap);
 		input.setHttpPort(8080);
 		input.setHttpsPort(8443);
 		input.setManagerUsername("test-user");
 		input.setManagerPassword("xxxxxxxxx");
 		
 		String result = input.toJSON();
 		assertNotNull(result);
		assertEquals(resultString, result);
 	}
 
 	@Test
 	@Ignore
 	public void testFromJSON() {
 		String input = resultString;
 		Tomcat result = Tomcat.fromJSON(input);
 		assertNotNull(result);
 		assertEquals(result.getAppList().size(), 1);
 		assertEquals(result.getApplicationMap().size(), 1);
 		assertTrue(result.getApplicationMap().containsKey("testApp"));
 		assertEquals(result.getApplicationMap().get("testApp").getStartTime(), 1);
 		assertEquals(result.getApplicationMap().get("testApp").getStartupTime(), 2);
 		assertTrue(result.getApplicationMap().get("testApp").getRunning());
 		assertEquals(result.getHttpPort().intValue(), 8080);
 		assertEquals(result.getHttpsPort().intValue(), 8443);
 	}
 }
