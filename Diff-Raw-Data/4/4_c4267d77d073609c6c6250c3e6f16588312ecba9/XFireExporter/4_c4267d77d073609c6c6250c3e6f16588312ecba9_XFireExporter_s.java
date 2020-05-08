 package org.codehaus.xfire.spring;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.binding.BeanInvoker;
 import org.codehaus.xfire.service.binding.DefaultObjectService;
 import org.codehaus.xfire.soap.Soap11;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.soap.SoapVersion;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.remoting.support.RemoteExporter;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 
 /**
  * Web controller that exports the specified service bean as a XFire Soap service endpoint.
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse </a>
  * @author <a href="mailto:poutsma@mac.com">Arjen Poutsma</a>
  */
 public class XFireExporter
         extends RemoteExporter
         implements Controller, InitializingBean, BeanNameAware
 {
     private DefaultObjectService service;
     private ServiceFactory serviceFactory;
     private XFireServletControllerAdapter delegate;
     private XFire xFire;
     private String name;
     private String namespace;
     private String style;
     private String use;
     private SoapVersion soapVersion;
     private String beanName;
 
 
     public void afterPropertiesSet()
             throws Exception
     {
         // Use specific name if given, else fall back to bean name.
         String theName = (this.name != null ? this.name : this.beanName);
 
         service = (DefaultObjectService) serviceFactory.create(getServiceInterface(),
                                                                theName,
                                                                namespace,
                                                                soapVersion,
                                                                style,
                                                                use,
                                                                null);
         if (logger.isInfoEnabled())
         {
             logger.info("Exposing SOAP v." + service.getSoapVersion().getVersion() + " service " + service.getName() +
                         " as " + service.getStyle() + "/" + service.getUse());
         }
 
         xFire.getServiceRegistry().register(service);
 
         service.setInvoker(new BeanInvoker(getProxyForService()));
 
         delegate = new XFireServletControllerAdapter(xFire, service.getName());
     }
 
     /**
      * Process the incoming SOAP request and create a SOAP response.
      *
      * @param request  current HTTP request
      * @param response current HTTP response
      * @return <code>null</code>
      * @throws Exception in case of errors
      */
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
             throws Exception
     {
         return delegate.handleRequest(request, response);
     }
 
     public void setServiceFactory(ServiceFactory serviceFactory)
     {
         this.serviceFactory = serviceFactory;
     }
 
     public void setXfire(XFire xFire)
     {
         this.xFire = xFire;
     }
 
     /**
      * Sets the service name. Default is the bean name of this exporter.
      */
     public void setName(String name)
     {
         this.name = name;
     }
 
     /**
      * Sets the service default namespace. Default is a namespace based on the package of the {@link
      * #getServiceInterface() service interface}.
      */
     public void setNamespace(String namespace)
     {
         this.namespace = namespace;
     }
 
     /**
      * Sets the style to use for service creation. Default is {@link SoapConstants#STYLE_WRAPPED wrapped}.
      */
     public void setStyle(String style)
     {
         this.style = style;
     }
 
 
     /**
      * Sets the use for service creation. Default is {@link SoapConstants#USE_LITERAL literal}.
      */
     public void setUse(String use)
     {
         this.use = use;
     }
 
     /**
      * Sets the soap version to use for service creation. Default is {@link Soap11 Soap v.1.1}.
      */
     public void setSoapVersion(SoapVersion soapVersion)
     {
         this.soapVersion = soapVersion;
     }
 
     public void setBeanName(String beanName)
     {
         this.beanName = beanName;
     }
 }
