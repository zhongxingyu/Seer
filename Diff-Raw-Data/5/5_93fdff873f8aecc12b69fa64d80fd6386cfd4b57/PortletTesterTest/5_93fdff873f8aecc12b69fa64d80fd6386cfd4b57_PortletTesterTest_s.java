 package com.portletguru.portletester;
 
 import java.io.IOException;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.portletguru.portlettester.PortletConfigGenerator;
 import com.portletguru.portlettester.PortletTester;
 import com.portletguru.portlettester.mocks.ActionRequestGenerator;
 import com.portletguru.portlettester.mocks.ActionResponseGenerator;
 
 /**
  * Testing different scenarios to use PortletTester
  * 
  * @author Derek Linde Li
  *
  */
 public class PortletTesterTest {
 	
 	private static PortletTester tester;
 	private static MockPortlet portlet;
 	
 	@BeforeClass
 	public static void setupClass() {
 		portlet = new MockPortlet();
 		tester = new PortletTester();
 	}
 	
 	@Test
 	public void testPortletInitializationWithPortletConfig() {
 		PortletConfigGenerator generator = tester.getPortletConfigGenerator();
 		generator.setPortletName("testPortlet");
 		generator.setBundleBaseName("com.test");
 		generator.setDefaultNamespace("testNameSpace");
 		generator.addProcessingEvent("testProcessingEvent");
 		generator.addPublicRenderParameter("p1", "v1");
 		generator.addProcessingEvent("testPublishingEvent");
 		generator.addSupportedLocale("en");
 		generator.addContainerRuntimeOption("ro1", new String[]{"v1", "v2"});
 		
 		PortletConfig portletConfig = generator.generatePortletConfig();
 		PortletException e = null;
 		try {
 			tester.initPortlet(portlet, portletConfig);
 		} catch (PortletException ex) {
 			e = ex;
 		}
 		
 		Assert.assertNull(e);
 		Assert.assertEquals("PortletConfig was changed during init.", portletConfig, portlet.getPortletConfig());
 	}
 	
 	@Test
 	public void testPortletInitializationWithoutPortletConfig() {
 		PortletException e = null;
 		try {
 			tester.initPortlet(portlet);
 		} catch (PortletException ex) {
 			e = ex;
 		}
 		
 		Assert.assertNull(e);
 		
 		PortletConfig portletConfig = portlet.getPortletConfig();
 		Assert.assertNotNull(portletConfig);
 		Assert.assertNotNull(portletConfig.getInitParameterNames());
 		Assert.assertNotNull(portletConfig.getContainerRuntimeOptions());
 		Assert.assertNotNull(portletConfig.getProcessingEventQNames());
 		Assert.assertNotNull(portletConfig.getPublicRenderParameterNames());
 		Assert.assertNotNull(portletConfig.getPublishingEventQNames());
 		Assert.assertNotNull(portletConfig.getPortletContext());
 		Assert.assertNotNull(portletConfig.getSupportedLocales());
 	}
 	
 	@Test
 	public void testActionRequestAttributes() {
 		ActionRequestGenerator requestGenerator = tester.getActionRequestGenerator();
 		ActionResponseGenerator responseGenerator = tester.getActionResponseGenerator();
 		requestGenerator.setAttribute(MockPortlet.TEST_ACTION_ATTRIBUTE, MockPortlet.TEST_ACTION_ATTRIBUTE_VALUE);
 		ActionRequest request = requestGenerator.generateRequest();
		ActionResponse response = responseGenerator.generateActionResponse();
 		
 		Exception e = null;
 		try {
 			portlet.portletRequestTest(request, response);
 		} catch (IOException ex) {
 			e = ex;
 		} catch (PortletException ex) {
 			e = ex;
 		}
 		
 		Assert.assertNull(e);
 		
 		Assert.assertEquals(MockPortlet.TEST_ACTION_ATTRIBUTE_VALUE, response.getRenderParameterMap().get(MockPortlet.TEST_ACTION_ATTRIBUTE)[0]);
 		Assert.assertEquals("1", response.getRenderParameterMap().get(MockPortlet.TEST_ACTION_ATTRIBUTE_SIZE)[0]);
 	}
 	
 	@Test
 	public void testActionRequestParameters() {
 		ActionRequestGenerator requestGenerator = tester.getActionRequestGenerator();
 		ActionResponseGenerator responseGenerator = tester.getActionResponseGenerator();
 		requestGenerator.setParameter(MockPortlet.TEST_ACTION_PARAM, MockPortlet.TEST_ACTION_PARAM_VALUE);
 		ActionRequest request = requestGenerator.generateRequest();
		ActionResponse response = responseGenerator.generateActionResponse();
 		
 		Exception e = null;
 		try {
 			portlet.portletRequestTest(request, response);
 		} catch (IOException ex) {
 			e = ex;
 		} catch (PortletException ex) {
 			e = ex;
 		}
 		
 		Assert.assertNull(e);
 		
 		Assert.assertEquals(MockPortlet.TEST_ACTION_PARAM_VALUE, response.getRenderParameterMap().get(MockPortlet.TEST_ACTION_PARAM)[0]);
 		Assert.assertEquals("1", response.getRenderParameterMap().get(MockPortlet.TEST_ACTION_PARAM_SIZE)[0]);
 	}
 	
 	@After
 	public void tearDown(){
 		tester.reset();
 	}
 }
