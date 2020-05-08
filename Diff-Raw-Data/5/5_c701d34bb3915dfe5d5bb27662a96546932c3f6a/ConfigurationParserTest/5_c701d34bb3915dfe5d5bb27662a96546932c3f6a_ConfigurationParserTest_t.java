 package org.layr.jee.routing.natural;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Test;
 import org.layr.jee.commons.JEEConfiguration;
 import org.layr.jee.stubs.StubsFactory;
 import org.xml.sax.SAXException;
 
 public class ConfigurationParserTest {
 	
 	@After
 	public void teardown(){
 		System.setProperty("org.layr.config.cacheEnabled", "true");
 	}
 
 	@Test
 	public void testConfigurationDefaultValues() throws IOException, ParserConfigurationException, SAXException {
 		System.setProperty("org.layr.config.cacheEnabled", "false");
 		
 		JEEConfiguration configuration = new JEEConfiguration(StubsFactory.createServletContext());
 		Assert.assertFalse(configuration.isCacheEnabled());
		Assert.assertEquals("/home/", configuration.getDefaultResource());
 	}
 
 	@Test
 	public void testConfigurationSettedValues() throws IOException, ParserConfigurationException, SAXException {
 		System.setProperty("org.layr.config.cacheEnabled", "true");
 
 		JEEConfiguration configuration = new JEEConfiguration(StubsFactory.createServletContext());
 		Assert.assertTrue(configuration.isCacheEnabled());
		Assert.assertEquals("/home/", configuration.getDefaultResource());
 	}
 
 }
