 package org.codehaus.xfire.loom;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.avalon.framework.configuration.Configurable;
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.logger.AbstractLogEnabled;
 import org.apache.avalon.framework.service.ServiceException;
 import org.apache.avalon.framework.service.ServiceManager;
 import org.apache.avalon.framework.service.Serviceable;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.ServiceRegistry;
import org.codehaus.xfire.service.binding.ObjectBinding;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.soap.Soap11;
 import org.codehaus.xfire.soap.Soap12;
 import org.codehaus.xfire.soap.SoapVersion;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 
 /**
  * Default implementation of ServiceDeployer
  *
  * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
  */
 public class DefaultServiceDeployer
         extends AbstractLogEnabled
         implements ServiceDeployer, Serviceable, Configurable
 {
     private final Map m_services = Collections.synchronizedMap(new HashMap());
 
     private ServiceRegistry m_serviceRegistry;
     private Map m_serviceFactories;
 
     private ServiceFactory m_defaultServiceFactory;
 
     private Map m_configurations;
 
     public void configure(final Configuration configuration)
             throws ConfigurationException
     {
         final Configuration[] kids = configuration.getChildren("service");
 
         m_configurations = new HashMap(kids.length);
 
         for (int i = 0; i < kids.length; i++)
         {
             m_configurations.put(kids[i].getAttribute("key"), kids[i]);
         }
 
         final Configuration child = configuration.getChild("defaultFactory");
 
         m_defaultServiceFactory = (ServiceFactory) m_serviceFactories.get(child.getValue());
 
         if (null == m_defaultServiceFactory)
         {
             final String msg = "Missing default factory '" + child.getValue() + "' at " + child.getLocation();
             throw new ConfigurationException(msg);
         }
     }
 
     public void service(final ServiceManager manager)
             throws ServiceException
     {
         m_serviceRegistry = (ServiceRegistry) manager.lookup(ServiceRegistry.ROLE);
         m_serviceFactories = (Map) manager.lookup(ServiceFactory.class.getName() + "{}");
     }
 
     public void deploy(final String key, final Object object)
             throws Exception
     {
         if (m_services.containsKey(key))
         {
             throw new IllegalStateException("Service with key '" + key + "' already deployed");
         }
 
         final Configuration configuration = (Configuration) m_configurations.get(key);
         final Service endpoint;
 
         if (null == configuration)
         {
             if (getLogger().isInfoEnabled())
                 getLogger().info("No configuration found for '" + key + "', using defaults");
 
             endpoint = m_defaultServiceFactory.create(object.getClass());
         }
         else
         {
             endpoint = createServiceFromConfiguration(configuration);
 
             if (getLogger().isDebugEnabled())
                 getLogger().debug("Created '" + endpoint.getServiceInfo().getName() + "' from key '" + key + "'");
         }
 
        endpoint.getBinding().setInvoker(new ServiceInvoker(object));
 
         registerService(key, endpoint);
     }
 
     private Service createServiceFromConfiguration(final Configuration configuration)
             throws ConfigurationException
     {
         final ServiceFactory factory = getServiceFactory(configuration.getChild("factory").getValue(null));
         String encodingUri = configuration.getChild("encodingStyleURI").getValue(null);
         final Map propertiesMap = new HashMap();
         
         if (encodingUri != null)
         {
             propertiesMap.put(AegisBindingProvider.TYPE_MAPPING_KEY, encodingUri);
         }
 
         if (factory instanceof ObjectServiceFactory)
         {
             ObjectServiceFactory osf = (ObjectServiceFactory) factory;
             osf.setStyle(configuration.getChild("style").getValue("wrapped"));
             osf.setUse(configuration.getChild("use").getValue("wrapped"));
             osf.setSoapVersion(getSoapVersion(configuration.getChild("soapVersion")));
         }
 
         final Configuration[] properties = configuration.getChildren("property");
 
         for (int i = 0; i < properties.length; i++)
         {
             propertiesMap.put(properties[i].getAttribute("name"), properties[i].getAttribute("value"));
         }
         
         final Service service =
                 factory.create(loadClass(configuration.getChild("serviceClass")),
                                configuration.getChild("name").getValue(),
                                configuration.getChild("namespace").getValue(""),
                                propertiesMap);
 
         return service;
     }
 
     private ServiceFactory getServiceFactory(final String key)
     {
         if (m_serviceFactories.containsKey(key))
         {
             return (ServiceFactory) m_serviceFactories.get(key);
         }
         else
         {
             return m_defaultServiceFactory;
         }
     }
 
     private SoapVersion getSoapVersion(final Configuration configuration)
             throws ConfigurationException
     {
         final String value = configuration.getValue("1.1");
 
         if (value.equals("1.1"))
         {
             return Soap11.getInstance();
         }
         else if (value.equals("1.2"))
         {
             return Soap12.getInstance();
         }
         else
         {
             final String msg = "Invalid soap version at " + configuration.getLocation() + ". Must be 1.1 or 1.2.";
             throw new ConfigurationException(msg);
         }
     }
 
     private Class loadClass(final Configuration configuration)
             throws ConfigurationException
     {
         try
         {
             return ClassLoaderUtils.loadClass(configuration.getValue(), getClass());
         }
         catch (ClassNotFoundException e)
         {
             final String msg = "Unable to load " + configuration.getValue() + " at " + configuration.getLocation();
             throw new ConfigurationException(msg, e);
         }
     }
 
     private void registerService(final String key, final Service endpoint)
     {
         m_serviceRegistry.register(endpoint);
 
         m_services.put(key, endpoint.getName());
     }
 
     public void undeploy(final String key)
     {
         if (m_services.containsKey(key))
         {
             m_serviceRegistry.unregister((String) m_services.remove(key));
         }
         else if (getLogger().isWarnEnabled())
         {
             getLogger().warn("Attempted to undeploy unknown key: " + key);
         }
     }
 }
