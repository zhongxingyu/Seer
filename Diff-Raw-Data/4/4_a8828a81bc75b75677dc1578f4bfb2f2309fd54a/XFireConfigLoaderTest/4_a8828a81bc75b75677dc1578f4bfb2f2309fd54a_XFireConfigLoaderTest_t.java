 package org.codehaus.xfire.spring.config;
 
 
 import javax.servlet.ServletContext;
 
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.handler.Handler;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.invoker.BeanInvoker;
 import org.codehaus.xfire.service.invoker.Invoker;
 import org.codehaus.xfire.service.invoker.ObjectInvoker;
 import org.codehaus.xfire.spring.AbstractXFireSpringTest;
 import org.codehaus.xfire.spring.ServiceBean;
 import org.codehaus.xfire.spring.TestHandler;
 import org.codehaus.xfire.spring.XFireConfigLoader;
 import org.codehaus.xfire.test.Echo;
 import org.codehaus.xfire.test.EchoImpl;
 import org.codehaus.xfire.transport.http.SoapHttpTransport;
 import org.springframework.context.ApplicationContext;
 import org.springframework.mock.web.MockServletContext;
 
 /**
  * @author tomeks
  *
  */
 public class XFireConfigLoaderTest
     extends AbstractXFireSpringTest
 {
     public void testConfigLoader()
         throws Exception
     {
         XFireConfigLoader configLoader = new XFireConfigLoader();
         XFire xfire = configLoader.loadConfig("META-INF/xfire/sservices.xml", null);
         
         doAssertions(xfire);
     }
     
     public void testConfigLoaderWithFilesystem()
         throws Exception
     {
         XFireConfigLoader configLoader = new XFireConfigLoader();
         XFire xfire = configLoader.loadConfig(getTestFile("src/test/META-INF/xfire/sservices.xml").getAbsolutePath());
         
         doAssertions(xfire);
     }
 
     public void testConfigLoaderWithMultipleFiles()
         throws Exception
     {
         XFireConfigLoader configLoader = new XFireConfigLoader();
         configLoader.setBasedir(getTestFile("."));
         XFire xfire = configLoader.loadConfig("src/test/META-INF/xfire/sservices.xml, " +
                 "org/codehaus/xfire/spring/config/OperationMetadataServices.xml");
         
         doAssertions(xfire);
     }
        
     public void testConfigLoaderWithParentContext() throws Exception 
     {
     	ServletContext servletCtx = new MockServletContext();
     	ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[] {"org/codehaus/xfire/spring/xfire.xml"});
 
         XFireConfigLoader configLoader = new XFireConfigLoader();
         XFire xfire = configLoader.loadConfig("META-INF/xfire/sservices.xml", appCtx);
 
         doAssertions(xfire);
     }
 
     private void doAssertions(XFire xfire){
         
         assertNotNull(xfire);
         assertEquals(2, xfire.getInHandlers().size());
         assertTrue(xfire.getInHandlers().get(1) instanceof TestHandler);
         assertEquals(xfire.getOutHandlers().size(),1);
         assertEquals(xfire.getFaultHandlers().size(),1);
         
         Service service = xfire.getServiceRegistry().getService("testservice");
         assertNotNull(service);
         
         assertEquals(4, service.getBindings().size());
         assertNotNull(service.getBinding(SoapHttpTransport.SOAP11_HTTP_BINDING));
         assertNotNull(service.getBinding(SoapHttpTransport.SOAP12_HTTP_BINDING));
         
         assertEquals(1, service.getEndpoints().size());
         Endpoint ep = (Endpoint) service.getEndpoints().iterator().next();
         assertNotNull(ep);
         assertEquals("http://localhost/TestService", ep.getUrl());
         
        assertEquals(3, service.getInHandlers().size());
        Handler testHandler = (Handler) service.getInHandlers().get(2); 
         assertTrue(testHandler instanceof TestHandler);
         assertEquals(testHandler.getAfter().size(),1);
         assertEquals(testHandler.getBefore().size(),2);
         
         assertEquals(service.getOutHandlers().size(),1);
         
         assertEquals("value", service.getProperty("myKey"));
         assertEquals("value1", service.getProperty("myKey1"));
 
         service = xfire.getServiceRegistry().getService("EchoWithJustImpl");
         assertEquals(EchoImpl.class, service.getServiceInfo().getServiceClass());
         
         service = xfire.getServiceRegistry().getService("EchoWithBean");
         Invoker invoker = service.getInvoker();
         assertTrue(invoker instanceof BeanInvoker);
         assertEquals(Echo.class, service.getServiceInfo().getServiceClass());
         
         service = xfire.getServiceRegistry().getService("EchoWithBeanNoServiceClass");
         invoker = service.getInvoker();
         assertTrue(invoker instanceof BeanInvoker);
         assertEquals(EchoImpl.class, service.getServiceInfo().getServiceClass());
         
         service = xfire.getServiceRegistry().getService("EchoWithSchemas");
         
         ServiceBean serviceBean = (ServiceBean) getBean("EchoWithServiceFactory");
         assertTrue(serviceBean.getServiceFactory() instanceof CustomServiceFactory);
         
         serviceBean = (ServiceBean) getBean("EchoWithBeanServiceFactory");
         assertTrue(serviceBean.getServiceFactory() instanceof CustomServiceFactory);
         
         serviceBean = (ServiceBean) getBean("EchoWithInvoker");
         assertTrue(serviceBean.getInvoker() instanceof ObjectInvoker);
     }
     
     protected ApplicationContext createContext()
     {
         return new ClassPathXmlApplicationContext(new String[] {
                 "org/codehaus/xfire/spring/xfire.xml", "META-INF/xfire/sservices.xml" });
     }
 }
