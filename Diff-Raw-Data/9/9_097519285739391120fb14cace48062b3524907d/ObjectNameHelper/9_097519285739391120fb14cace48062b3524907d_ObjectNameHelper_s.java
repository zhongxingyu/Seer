 package com.buschmais.osgi.maexo.framework.commons.mbean.objectname;
 
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import javax.management.ObjectName;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides common functionality to work with object names
  */
 public class ObjectNameHelper {
 
 	private static Logger logger = LoggerFactory
 			.getLogger(ObjectNameHelper.class);
 
 	private BundleContext bundleContext;
 
 	public ObjectNameHelper(BundleContext bundleContext) {
 		this.bundleContext = bundleContext;
 	}
 
 	/**
 	 * Creates an object name from the given resource by looking up an object
 	 * name factory, which handles one of the interfaces implemented by the
 	 * resource's class.
 	 * 
 	 * @param resource
 	 *            the resource
 	 * @return the object name
 	 */
 	public ObjectName getObjectName(Object resource) {
		if (resource == null) {
			throw new IllegalArgumentException("resource must not be null");
		}
 		return this
 				.getObjectName(resource, resource.getClass().getInterfaces());
 	}
 
 	/**
 	 * Creates an object name from the given resource by looking up the
 	 * corresponding object name factory
 	 * 
 	 * @param resource
 	 *            the resource
 	 * @param resourceInterface
 	 *            the interface to use for looking up the object name factor
 	 * @return the object name
 	 */
 	public ObjectName getObjectName(Object resource, Class<?> resourceInterface) {
 		return this.getObjectName(resource,
 				new Class<?>[] { resourceInterface });
 	}
 
 	/**
 	 * Creates an object name from the given resource by looking up the
 	 * corresponding object name factory
 	 * 
 	 * @param resource
 	 *            the resource
 	 * @param resourceInterfaces
 	 *            the interfaces to use for looking up the object name factor
 	 * @return the object name
 	 */
 	public ObjectName getObjectName(Object resource,
 			Class<?>[] resourceInterfaces) {
 		if (resource == null || resourceInterfaces == null
 				|| resourceInterfaces.length == 0) {
 			throw new IllegalArgumentException(
 					"resource and resourceInterface must not be null");
 		}
		// find the object name factory that handles instance of the resource's
 		// interfaces
 		if (logger.isDebugEnabled()) {
 			logger
 					.debug("looking up object name factory service for interfaces "
 							+ Arrays.asList(resourceInterfaces));
 		}
 		ServiceReference[] serviceReferences;
 		StringBuilder filter = new StringBuilder();
 		filter.append("(|");
 		for (Class<?> resourceInterface : resourceInterfaces) {
 			filter.append("("
 					+ ObjectNameFactory.SERVICE_PROPERTY_RESOURCEINTERFACE
 					+ "=" + resourceInterface.getName() + ")");
 		}
 		filter.append(")");
 		if (logger.isDebugEnabled()) {
 			logger.debug("using filter : " + filter.toString());
 		}
 		try {
 			serviceReferences = this.bundleContext.getServiceReferences(
 					ObjectNameFactory.class.getName(), filter.toString());
 		} catch (InvalidSyntaxException e) {
 			throw new IllegalArgumentException(e);
 		}
 		// iterate over all available service reference and use the first
 		// available one to create the object name
 		if (serviceReferences != null) {
 			for (ServiceReference serviceReference : serviceReferences) {
 				try {
 					ObjectNameFactory objectNameFactory = (ObjectNameFactory) this.bundleContext
 							.getService(serviceReference);
 					if (objectNameFactory != null) {
 						if (logger.isDebugEnabled()) {
 							logger.debug("using object name factory "
 									+ objectNameFactory);
 						}
 						return objectNameFactory.getObjectName(resource);
 					}
 				} finally {
 					this.bundleContext.ungetService(serviceReference);
 				}
 			}
 		}
 		// throw an exception if the object name could not be constructed
 		throw new ObjectNameFactoryException(
 				"no object name factory found for resource interface "
 						+ Arrays.asList(resourceInterfaces));
 	}
 
 	/**
 	 * Registers an object name factory for a given type as service
 	 * 
 	 * @param objectNameFactory
 	 *            the object name factory
 	 * @param resourceInterface
 	 *            the class of the resources which are supported
 	 * @return the service reference
 	 */
 	public ServiceRegistration registerObjectNameFactory(
 			ObjectNameFactory objectNameFactory, Class<?> resourceInterface) {
 		if (objectNameFactory == null || resourceInterface == null) {
 			throw new IllegalArgumentException(
 					"objectNameFactory and resourceInterface must be provided");
 		}
 		Dictionary<String, String> properties = new Hashtable<String, String>();
 		properties.put(ObjectNameFactory.SERVICE_PROPERTY_RESOURCEINTERFACE,
 				resourceInterface.getName());
 		return this.bundleContext.registerService(ObjectNameFactory.class
 				.getName(), objectNameFactory, properties);
 	}
 
 	/**
 	 * Constructs an object name from the provided properties using the default
 	 * domain
 	 * 
 	 * @param properties
 	 *            the properties
 	 * @return the object name
 	 */
 	public static ObjectName getObjectName(Properties properties) {
 		return getObjectName(ObjectNameFactory.DEFAULT_DOMAIN, properties);
 	}
 
 	/**
 	 * Constructs an object name from the provided domain and properties
 	 * 
 	 * @param domain
 	 *            the domain
 	 * @param properties
 	 *            the properties
 	 * @return the object name
 	 */
 	public static ObjectName getObjectName(String domain, Properties properties) {
		StringBuilder sb = new StringBuilder(ObjectNameFactory.DEFAULT_DOMAIN);
 		sb.append(':');
 		int i = 0;
 		for (Entry<Object, Object> entry : properties.entrySet()) {
 			if (i > 0) {
 				sb.append(',');
 			}
 			i++;
 			sb.append(entry.getKey().toString());
 			sb.append('=');
 			sb.append(entry.getValue().toString());
 		}
 		try {
 			return new ObjectName(sb.toString());
 		} catch (Exception e) {
 			throw new IllegalArgumentException(
 					"cannot create object name instance, domain=" + domain
 							+ ", properties=" + properties, e);
 		}
 	}
 
 }
