 package org.codehaus.xfire.client;
 
 import java.lang.reflect.Method;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.Holder;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.service.invoker.ObjectInvoker;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilder;
 import org.jdom.Document;
 
 public class HeaderTest extends AbstractXFireAegisTest
 {
     private Service service;
     
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         ObjectServiceFactory factory = new ObjectServiceFactory(getTransportManager()) {
             protected boolean isHeader(Method method, int j)
             {
                 return true;
             }
 
             protected boolean isInParam(Method method, int j)
             {
                 return j == 0;
             }
 
             protected boolean isOutParam(Method method, int j)
             {
                 return j == -1 || j == 1;
             }
             
         };
         
         service = factory.create(Echo.class);
         service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, EchoImpl.class);
         
         getServiceRegistry().register(service);
     }
     
     public void testHeaders() throws Exception
     {
         XFireProxyFactory xpf = new XFireProxyFactory(getXFire());
         Echo echo = (Echo) xpf.create(service, "xfire.local://Echo");
         
         Holder h = new Holder();
         assertEquals("hi", echo.echo("hi", h));
         assertEquals("header2", h.getValue());
         
         Document wsdl = getWSDLDocument("Echo");
        printNode(wsdl);
         addNamespace("wsdlsoap", WSDLBuilder.WSDL11_SOAP_NS);
         assertValid("//wsdl:input/wsdlsoap:header[@message='tns:echoRequestHeaders'][@part='in0']", wsdl);
         assertValid("//wsdl:output/wsdlsoap:header[@message='tns:echoResponseHeaders'][@part='out']", wsdl);
         assertValid("//wsdl:output/wsdlsoap:header[@message='tns:echoResponseHeaders'][@part='out0']", wsdl);
     }   
 }
