 package org.codehaus.xfire.spring.remoting;
 
 import java.lang.reflect.Modifier;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.aegis.type.TypeMappingRegistry;
 import org.codehaus.xfire.annotations.AnnotationServiceFactory;
 import org.codehaus.xfire.annotations.WebAnnotations;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.invoker.BeanInvoker;
 import org.codehaus.xfire.spring.SpringUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanIsAbstractException;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
 
 /**
  * Implementation of the {@link org.springframework.web.servlet.HandlerMapping} interface that recognizes {@link
  * org.codehaus.xfire.annotations.WebServiceAnnotation web service annotations} on Spring-managed beans and
  * automatically wires them into the current servlet's WebApplicationContext.
  * <p/>
  * All beans Services are exposed by their {@link org.codehaus.xfire.annotations.WebServiceAnnotation#getServiceName()
  * service name}, with a defined {@link #setUrlPrefix(String) prefix}. For instance, a bean annotated with the service
  * name <code>EchoService</code> will be exposed as <code>/services/EchoService</code>.
  *
  * @author <a href="mailto:poutsma@mac.com">Arjen Poutsma</a>
  * @see WebAnnotations
  * @see org.codehaus.xfire.annotations.WebServiceAnnotation
  */
 public class Jsr181HandlerMapping
         extends AbstractUrlHandlerMapping
 {
     private WebAnnotations webAnnotations;
     private XFire xFire;
     private TypeMappingRegistry typeMappingRegistry;
     private String urlPrefix = "/services/";
     private String servletControllerAdapterName;
 
     protected void initApplicationContext()
             throws BeansException
     {
        AegisBindingProvider provider;
        if (typeMappingRegistry == null)
            provider = new AegisBindingProvider();
        else
            provider = new AegisBindingProvider(typeMappingRegistry);
        
         AnnotationServiceFactory serviceFactory =
                 new AnnotationServiceFactory(webAnnotations,
                                              xFire.getTransportManager(),
                                             provider);
 
         ApplicationContext context = getApplicationContext();
  
         while (true)
         {
             if (context == null) break;
             
             processBeans(context, serviceFactory);
 
             context = context.getParent();
         }
     }
 
     private void processBeans(ApplicationContext beanFactory, AnnotationServiceFactory serviceFactory)
     {
         String[] beanNames = beanFactory.getBeanDefinitionNames();
 
         ConfigurableApplicationContext ctxt = (ConfigurableApplicationContext) beanFactory;
 
         // Take any bean name or alias that has a web service annotation
         for (int i = 0; i < beanNames.length; i++)
         {
             BeanDefinition def = ctxt.getBeanFactory().getBeanDefinition(beanNames[i]);
     
             if (!def.isSingleton() || def.isAbstract()) continue;
             
             Class clazz;
             Object bean;
             try
             {
                 clazz = getApplicationContext().getType(beanNames[i]);
                 bean = beanFactory.getBean(beanNames[i]);
                 try
                 {
                     clazz = SpringUtils.getUserTarget(bean).getClass();
                 }
                 catch (Exception e)
                 {
                     logger.error("Failed to get the User Target Class of bean " + beanNames[i], e);
                 }
             }
             catch (BeanIsAbstractException e)
             {
                 // The bean is abstract, we won't be doing anything with it.
                 continue;
             }
 
             if (clazz != null && 
                     !Modifier.isAbstract(clazz.getModifiers()) && 
                     webAnnotations.hasWebServiceAnnotation(clazz))
             {
                 Service endpoint = serviceFactory.create(clazz);
                 if (logger.isInfoEnabled())
                 {
                     logger.info("Exposing  service " + endpoint.getName() + 
                                 " to " + urlPrefix + endpoint.getSimpleName());
                 }
                 
                 xFire.getServiceRegistry().register(endpoint);
                 endpoint.setInvoker(new BeanInvoker(bean));
                 Object controller = createController(endpoint.getName());
                 registerHandler(urlPrefix + endpoint.getSimpleName(), controller);
             }
             else
             {
                 if (logger.isDebugEnabled())
                 {
                     logger.debug("Rejected bean '" + beanNames[i] + "' since it has no WebService annotation");
                 }
             }
         }
     }
     
     /**
      * Creates the XFireServletControllerAdapter either indirectly using the 
      * XFireServletControllerAdapter prototype bean declared in the Spring app context,
      * or directly through the the constructor of XFireServletControllerAdapter. 
      * 
      * @param endpointName The endpointName to inject into the XFireServletControllerAdapter
      * @return A new instance of XFireServletControllerAdapter
      */
     private Object createController(QName endpointName) 
     {
         if (servletControllerAdapterName != null)
         {
             Object controller = getApplicationContext().getBean(servletControllerAdapterName);
             if (controller == null)
             {
                 logger.error("Failed to find bean with name " + servletControllerAdapterName);
             }
             else
             {
                 try
                 {
                     XFireServletControllerAdapter xFireServletControllerAdapter = (XFireServletControllerAdapter) SpringUtils
                             .getUserTarget(controller);
                     xFireServletControllerAdapter.setServiceName(endpointName);
                     return controller;
                 }
                 catch (Exception e)
                 {
                     logger.error("Failed to create a Controller for endpoint " + endpointName, e);
                 }
             }
         }
         return new XFireServletControllerAdapter(xFire, endpointName);
     }
 
     /**
      * Sets the web annotations implementation to use.
      */
     public void setWebAnnotations(WebAnnotations webAnnotations)
     {
         this.webAnnotations = webAnnotations;
     }
 
     /**
      * Sets the XFire instance.
      */
     public void setXfire(XFire xFire)
     {
         this.xFire = xFire;
     }
 
     /**
      * Sets the type mapping registry.
      */
     public void setTypeMappingRegistry(TypeMappingRegistry typeMappingRegistry)
     {
         this.typeMappingRegistry = typeMappingRegistry;
     }
 
     /**
      * Sets the url prefix used when exposing services. Defaults to <code>/services/</code>.
      */
     public void setUrlPrefix(String urlPrefix)
     {
         this.urlPrefix = urlPrefix;
     }
 
     public void setServletControllerAdapterName(String servletControllerAdapterName) 
     {
         this.servletControllerAdapterName = servletControllerAdapterName;
     }
 }
