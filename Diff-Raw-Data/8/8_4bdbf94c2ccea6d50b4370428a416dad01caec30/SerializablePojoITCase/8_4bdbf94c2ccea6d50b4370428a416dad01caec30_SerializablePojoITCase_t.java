 package eu.dm2e.ws.api;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import eu.dm2e.ws.OmnomUnitTest;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.annotations.RDFProperty;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
 import eu.dm2e.ws.grafeo.test.IntegerPojo;
 import eu.dm2e.ws.services.xslt.XsltService;
 
 public class SerializablePojoITCase extends OmnomUnitTest {
 	
 	public static final String MOCK_CLASS = "omnom:Mock";
 	public static final String MOCK_PROP = "omnom:fooProp";
 	
 	@RDFClass(MOCK_CLASS)
 	public static class MockPojo extends SerializablePojo<MockPojo> {
 		
 		@RDFProperty(MOCK_PROP)
 		private String fooProp;
 		public String getFooProp() { return this.fooProp; }
 		public void setFooProp(String fooProp) { this.fooProp = fooProp; }
 	}
 
 	@Test
 	public void testGetRDFClassUri() throws Exception {
 	//		MockPojo mock = ;
 		Grafeo g = new GrafeoImpl();
 		assertEquals(g.expand(MOCK_CLASS), new MockPojo().getRDFClassUri());
 	}
 
 	@Test
 	public void testGetGrafeo() throws Exception {
 		assertNotNull(new MockPojo().getGrafeo());
 	}
 
 	@Test
 	public void testCopy() throws Exception {
 		{
 			MockPojo orig = new MockPojo();
 			orig.setFooProp("FOO");
 			MockPojo copy = orig.copy();
 			assertEquals(orig.getId(), copy.getId());
 			assertEquals(orig.getFooProp(), copy.getFooProp());
 			GrafeoAssert.graphsAreEquivalent(orig.getGrafeo(), copy.getGrafeo());
 		}
 		{
 			IntegerPojo orig = new IntegerPojo(10);
 			IntegerPojo copy = orig.copy();
 			assertNull(orig.getId(), orig.getId());
 			assertNull(copy.getId(), copy.getId());
 			assertEquals(orig.getId(), copy.getId());
 			assertEquals(orig.getSomeNumber(), copy.getSomeNumber());
 			GrafeoAssert.graphsAreEquivalent(orig.getGrafeo(), copy.getGrafeo());
 		}
 		{
 			String THE_URI = "http://foo3000";
 			IntegerPojo orig = new IntegerPojo(THE_URI, 3000);
 			IntegerPojo copy = orig.copy();
 			assertEquals(THE_URI, orig.getId());
 			assertEquals(orig.getId(), copy.getId());
 			assertEquals(orig.getSomeNumber(), copy.getSomeNumber());
 			GrafeoAssert.graphsAreEquivalent(orig.getGrafeo(), copy.getGrafeo());
 			assertEquals(orig.getClass(), copy.getClass());
 		}
 	}	
 
 	@Ignore("Test with actual implementation")
 	@Test
 	public void testJson() {
 		{
 			MockPojo mock = new MockPojo();
 			mock.setId("http://foo");
 			mock.setFooProp("23");
//			log.info(mock.getFlatJson());
 		}
 		{
 			WebservicePojo ws = new XsltService().getWebServicePojo();
 			log.info(ws.getTerseTurtle());
//			log.info(ws.getFlatJson());
 		}
 		{
 			WebservicePojo ws = new XsltService().getWebServicePojo();
 			WebserviceConfigPojo wsconf = ws.createConfig();
 			wsconf.addParameterAssignment(XsltService.PARAM_XML_IN, "http://foo");
//			log.info(wsconf.getFlatJson());
 		}
 	}
 }
