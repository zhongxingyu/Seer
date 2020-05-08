 package org.codehaus.xfire.spring.remoting;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.annotations.AnnotationServiceFactory;
 import org.codehaus.xfire.annotations.WebAnnotations;
 import org.codehaus.xfire.annotations.WebServiceAnnotation;
 import org.codehaus.xfire.client.Client;
 import org.codehaus.xfire.client.XFireProxy;
 import org.codehaus.xfire.server.http.XFireHttpServer;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.service.invoker.ObjectInvoker;
 import org.codehaus.xfire.test.Echo;
 import org.codehaus.xfire.test.EchoImpl;
 import org.codehaus.xfire.transport.Channel;
 import org.easymock.MockControl;
 import org.springframework.aop.framework.AopProxy;
 
 public class XFireClientFactoryBeanTest
     extends AbstractXFireAegisTest
 {
     private String serviceURL = "http://localhost:8080/xfire/services/Echo";
     private String wsdlUrl;
     private XFireClientFactoryBean factory;
     
     protected void setUp() throws Exception {
         wsdlUrl = getTestFile("src/test/org/codehaus/xfire/spring/remoting/echo.wsdl").toURL().toString();
         
         factory = new XFireClientFactoryBean();
         factory.setServiceInterface(Echo.class);
         //factory.setNamespaceUri("urn:Echo");
         //factory.setServiceName("Echo");
         factory.setWsdlDocumentUrl(wsdlUrl);
         factory.setUrl(serviceURL);
     }
     
     public void testMandatory() 
         throws Exception
     {
         factory = new XFireClientFactoryBean();
         try {
             factory.afterPropertiesSet();
             fail("expected exception without WSDL and service interface");
         } catch (IllegalStateException e) {
             // what we expect
         }
 
         factory = new XFireClientFactoryBean();
         try {
             factory.setServiceInterface(Echo.class);
             factory.afterPropertiesSet();
             fail("expected exception without WSDL");
         } catch (IllegalStateException e) {
             // what we expect since WSDL is required
         }
 
         factory = new XFireClientFactoryBean();
         try {
             factory.setWsdlDocumentUrl("test");
             factory.afterPropertiesSet();
             fail("expected exception without service interface");
         } catch (IllegalStateException e) {
             // what we expect since interface is required
         }
     }
     
     public void testDefaults()
         throws Exception
     {
         assertTrue("expected lookupServiceOnStartup default to be true", factory.getLookupServiceOnStartup());
         assertNull("default username must be null", factory.getUsername());
         assertNull("default password must be null", factory.getPassword());
         assertEquals("default service factory is wrong type", ObjectServiceFactory.class, factory.getServiceFactory().getClass());
         assertEquals("default type (before afterPropertiesSet) is not interface", Echo.class, factory.getObjectType());
 
         assertNull("default service name must be null", factory.getServiceName());
         assertNull("default namespaceUri must be null", factory.getNamespaceUri());
     }
     
     public void testXFireProxyFactoryBeanLoadOnStartup()
         throws Exception
     {
         factory.afterPropertiesSet();
         
         Class objectType = factory.getObjectType();
         assertTrue("object created by factory does not implement interface", Echo.class.isAssignableFrom(objectType));
 
         Echo obj = (Echo)factory.getObject(); 
         Object handler = Proxy.getInvocationHandler(obj);
         Class handlerClass = handler.getClass();
         assertTrue("factory created own proxy: " + handlerClass, XFireProxy.class.isAssignableFrom(handlerClass));        
         XFireProxy fireProxy = (XFireProxy)handler;
         checkAuth(fireProxy, null, null);
         Client c = fireProxy.getClient();
         System.out.println(serviceURL);
         System.out.println(c.getUrl());
         assertEquals("wrong service URL", serviceURL, c.getUrl());
     }    
     
     public void testServerURL()
         throws Exception
     {
         factory.setUrl("http://localhost/test");
         
         factory.afterPropertiesSet();
         
         Class objectType = factory.getObjectType();
         assertTrue("object created by factory does not implement interface", Echo.class.isAssignableFrom(objectType));
     
         Echo obj = (Echo)factory.getObject(); 
         Object handler = Proxy.getInvocationHandler(obj);
         Class handlerClass = handler.getClass();
         assertTrue("factory created own proxy: " + handlerClass, XFireProxy.class.isAssignableFrom(handlerClass));        
         XFireProxy fireProxy = (XFireProxy)handler;
         checkAuth(fireProxy, null, null);
         Client c = fireProxy.getClient();
        
         assertEquals("wrong service URL", "http://localhost/test", c.getUrl());
     }
         
     public void testXFireProxyFactoryBeanNoLoadOnStartup()
         throws Exception
     {
         factory.setLookupServiceOnStartup(false);
         factory.afterPropertiesSet();
         
         Class objectType = factory.getObjectType();
         assertTrue("object created by factory does not implement interface", Echo.class.isAssignableFrom(objectType));
         Echo obj = (Echo)factory.getObject(); 
         Object handler = Proxy.getInvocationHandler(obj);
         Class handlerClass = handler.getClass();
         assertTrue("factory did not create own proxy: " + handlerClass, AopProxy.class.isAssignableFrom(handlerClass));
         
         assertEquals("Wrong uninit toString() for proxy", 
                      "Un-initialized XFire client proxy for: interface org.codehaus.xfire.test.Echo at: " + serviceURL, 
                      obj.toString());
     }
     
     public void testAuthentication() 
         throws Exception
     {
         String expectedUsername = "fried";
         String expectedPassword = "hoeben";
         factory.setUsername(expectedUsername);
         factory.setPassword(expectedPassword);
         factory.afterPropertiesSet();
 
         Echo obj = (Echo)factory.getObject(); 
         
         XFireProxy handler = (XFireProxy)Proxy.getInvocationHandler(obj);
         checkAuth(handler, expectedUsername, expectedPassword);
     }
 
     private void checkAuth(XFireProxy handler, String expectedUsername, String expectedPassword)
     {
         Client client = handler.getClient();
         String username = (String)client.getProperty(Channel.USERNAME);
         assertEquals("wrong username", expectedUsername, username);
         String password = (String)client.getProperty(Channel.PASSWORD);
         assertEquals("wrong password", expectedPassword, password);
     }
     
     public void testSetWSDLProperties()
         throws Exception
     {
         // first ensure we have a WSDL to parse
         super.setUp();
         ServiceFactory serverFact = getServiceFactory();
      
         XFireHttpServer server = new XFireHttpServer(getXFire());
         server.setPort(8191);
         server.start();
         
         Service service = serverFact.create(Echo.class);
         service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, EchoImpl.class);
 
         getServiceRegistry().register(service);
 
         // now create a special factory that will actually use the created WSDL
         factory = new XFireClientFactoryBean();
         factory.setServiceInterface(Echo.class);
         factory.setWsdlDocumentUrl("http://localhost:8191/Echo?wsdl");
         factory.afterPropertiesSet();
         
         Echo echo = (Echo) factory.getObject();
         assertEquals("hi", echo.echo("hi"));
         
         server.stop();
     }
  
     public void testServerClass() 
         throws Exception
     {
         MockControl control = MockControl.createControl(WebAnnotations.class);
         WebAnnotations webAnnotations = (WebAnnotations) control.getMock();
         
         WebServiceAnnotation serviceAnnotation = new WebServiceAnnotation();
         webAnnotations.getWebServiceAnnotation(Echo.class);
         control.setDefaultReturnValue(serviceAnnotation);
         
         webAnnotations.hasWebServiceAnnotation(Echo.class);
         control.setDefaultReturnValue(true);
         
         webAnnotations.hasWebServiceAnnotation(EchoImpl.class);
         control.setDefaultReturnValue(true);
         webAnnotations.hasHandlerChainAnnotation(EchoImpl.class);
         control.setReturnValue(false);
        webAnnotations.hasSOAPBindingAnnotation(Echo.class);
         control.setReturnValue(false);
         webAnnotations.hasWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(true);
         
         serviceAnnotation = new WebServiceAnnotation();
         serviceAnnotation.setServiceName("Echo");
         serviceAnnotation.setTargetNamespace("urn:Echo");
         serviceAnnotation.setEndpointInterface(Echo.class.getName());
         webAnnotations.getWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(serviceAnnotation);
         webAnnotations.getWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(serviceAnnotation);
         
         Method echoMethod = EchoImpl.class.getMethod("echo", new Class[]{String.class});
         webAnnotations.hasWebMethodAnnotation(echoMethod);
         control.setDefaultReturnValue(true);
         
         webAnnotations.hasWebMethodAnnotation(echoMethod);
         control.setDefaultReturnValue(false);
         webAnnotations.hasWebParamAnnotation(echoMethod, 0);
         control.setDefaultReturnValue(false);
         webAnnotations.hasWebResultAnnotation(echoMethod);
         control.setDefaultReturnValue(false);
         webAnnotations.hasOnewayAnnotation(echoMethod);
         control.setDefaultReturnValue(false);
 
        
         control.replay();
         
         factory.setServiceFactory(new AnnotationServiceFactory(webAnnotations, 
                                                                getTransportManager()));
         factory.setServiceClass(EchoImpl.class);
         
         factory.afterPropertiesSet();
         
         control.verify();
     }
 }
