 package org.codehaus.xfire.loom;
 
 import java.net.URL;
 import java.util.Map;
 
import javax.wsdl.Definition;
 import javax.xml.namespace.QName;
 
 import org.apache.avalon.framework.activity.Initializable;
 import org.apache.avalon.framework.logger.AbstractLogEnabled;
 import org.apache.avalon.framework.service.ServiceException;
 import org.apache.avalon.framework.service.ServiceManager;
 import org.apache.avalon.framework.service.Serviceable;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.aegis.type.TypeMappingRegistry;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.transport.TransportManager;
 
 /**
  * Default implementation of a ServiceFactory
  *
  * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
  */
 public class DefaultServiceFactory
         extends AbstractLogEnabled
         implements ServiceFactory, Serviceable, Initializable
 {
     private TypeMappingRegistry m_typeMappingRegistry;
     private TransportManager m_transportManager;
 
     private ObjectServiceFactory m_factory;
 
     public void service(final ServiceManager manager)
             throws ServiceException
     {
         m_typeMappingRegistry = (TypeMappingRegistry) manager.lookup(TypeMappingRegistry.ROLE);
         m_transportManager = (TransportManager) manager.lookup(TransportManager.ROLE);
     }
 
     protected final TransportManager getTransportManager()
     {
         return m_transportManager;
     }
 
     protected final TypeMappingRegistry getTypeMappingRegistry()
     {
         return m_typeMappingRegistry;
     }
 
     protected final void setFactory(final ObjectServiceFactory factory)
     {
         m_factory = factory;
     }
 
     public void initialize()
             throws Exception
     {
         setFactory(new ObjectServiceFactory(m_transportManager,
                                             new AegisBindingProvider(m_typeMappingRegistry)));
     }
 
     public Service create(final Class clazz)
     {
         return m_factory.create(clazz);
     }
 
     public void addIgnoredMethods(String className)
     {
         m_factory.addIgnoredMethods(className);
     }
     
     public Service create(Class clazz, QName service, URL wsdlUrl, Map properties)
     {
         return m_factory.create(clazz, service, wsdlUrl, properties);
     }
 
     public Service create(Class clazz, Map properties)
     {
         return m_factory.create(clazz, properties);
     }
 
     public Service create(Class clazz, String name, String namespace, Map properties)
     {
         return m_factory.create(clazz, name, namespace, properties);
     }

    public Service create(Class clazz, QName service, Definition def, Map properties)
    {
        return m_factory.create(clazz, service, def, properties);
    }
 }
