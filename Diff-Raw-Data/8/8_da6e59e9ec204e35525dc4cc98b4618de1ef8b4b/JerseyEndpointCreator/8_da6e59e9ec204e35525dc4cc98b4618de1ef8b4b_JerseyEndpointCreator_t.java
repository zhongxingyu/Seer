 package org.ow2.chameleon.rose.rest;
 
 import static java.lang.Integer.valueOf;
 import static org.osgi.service.log.LogService.LOG_DEBUG;
 import static org.osgi.service.log.LogService.LOG_ERROR;
 import static org.osgi.service.log.LogService.LOG_WARNING;
 import static org.ow2.chameleon.rose.RoSeConstants.ENDPOINT_CONFIG;
 
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.Path;
 
 import org.apache.felix.ipojo.annotations.Bind;
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.ServiceProperty;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.ow2.chameleon.rose.AbstractExporterComponent;
 import org.ow2.chameleon.rose.ExporterService;
 import org.ow2.chameleon.rose.RoSeConstants;
 import org.ow2.chameleon.rose.RoseMachine;
 import org.ow2.chameleon.rose.introspect.ExporterIntrospection;
 import org.ow2.chameleon.rose.rest.provider.ManagedComponentProvider;
 import org.ow2.chameleon.rose.rest.provider.ProxiedComponentProvider;
 
 import com.sun.jersey.api.core.ResourceConfig;
 import com.sun.jersey.core.spi.component.ComponentContext;
 import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
 import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
 
 /**
  * This component provides a REST, Jersey based implementation of an
  * {@link ExporterService}.
  * 
  * @author Jonathan Bardin <jonathan.bardin@imag.fr>
  */
 @Component(name = "RoSe_exporter.jersey")
 @Provides(specifications = { ExporterService.class, ExporterIntrospection.class })
 public class JerseyEndpointCreator extends AbstractExporterComponent implements
 		IoCComponentProviderFactory, ExporterService, ExporterIntrospection {
 
 	/**
 	 * Default value for the {@link JerseyEndpointCreator#PROP_HTTP_PORT}
 	 * property.
 	 */
 	private static final int DEFAULT_HTTP_PORT = 80;
 
 	/**
 	 * Property of the HttpService http port.
 	 */
 	private final static String PROP_HTTP_PORT = "org.osgi.service.http.port";
 
 	//relative path
 	private static String PROP_PATH = "path";
 
 	/**
      * Resources representation will be publish under this URL.
      */
     private String myurl;
 
 	/**
 	 * Configuration supported by this component
 	 */
 	@ServiceProperty(name = ENDPOINT_CONFIG, mandatory = true, value = "{jersey,jax-rs,jaxrs,rest}")
 	private String[] configs = { "jersey", "jax-rs", "jaxrs", "rest" };
 
 	@Requires(optional = true)
 	private LogService logger;
 
 	/**
 	 * Set in
 	 * {@link JerseyEndpointCreator#bindHttpService(HttpService, ServiceReference)
 	 * bindHttpService}
 	 */
 	private HttpService httpservice;
 
 	/**
 	 * Require the {@link RoseMachine}.
 	 */
 	@Requires(optional = false, id = "rose.machine")
 	private RoseMachine machine;
 
 	/**
 	 * Property containing the value of the
 	 * {@link JerseyEndpointCreator#PROP_HTTP_PORT} HttpService property. Set in
 	 * {@link JerseyEndpointCreator#bindHttpService(HttpService, ServiceReference)}
 	 */
 	private int httpport;
 
 	private JerseyServletBridge container = null;
 
 	private final MyResourceConfig rsconfig = new MyResourceConfig();
 
 	/**
 	 * The Servlet name of the JERSEY bridge.
 	 */
 	@ServiceProperty(name = "jersey.servlet.name", mandatory = true, value = "/rest")
 	private String rootName;
 
 	private final BundleContext context;
 
 	/*------------------------------------*
 	 *  Component Life-cycle methods      *
 	 *------------------------------------*/
 
 	public JerseyEndpointCreator(BundleContext pcontext) {
 		context = pcontext;
 	}
 
 	/**
 	 * Execute while this instance is starting. Call by iPOJO.
 	 */
 	@Override
 	@Validate
 	protected void start() {
 		super.start();
 		
 		//compute the PROP_CXF_URL property
         try {
			myurl = new URI("http://"+machine.getHost()+":"+httpport+rootName).toString(); //compute the url
 		} catch (Exception e) {
 			logger.log(LOG_ERROR, "Cannot create the URL of the JAX-WS server, this will lead to incomplete EndpointDescription.",e);
 		}
 	}
 
 	/**
 	 * Execute while this instance is stopping. Call by iPOJO.
 	 */
 	@Override
 	@Invalidate
 	protected void stop() {
 		super.stop();
 
 		if (container != null && httpservice != null) {
 			try { // Unregister the jersey server.
 				httpservice.unregister(rootName);
 			} catch (RuntimeException re) {
 				logger.log(LogService.LOG_ERROR, re.getMessage(), re);
 			}
 		}
 	}
 
 	/**
 	 * Bind the {@link HttpService} and set the
 	 * {@link JerseyEndpointCreator#httpport} value.
 	 * 
 	 * @param service
 	 *            the {@link HttpService}
 	 * @param ref
 	 *            the {@link HttpService} {@link ServiceReference}.
 	 */
 	@SuppressWarnings("unused")
 	@Bind(aggregate = false, optional = false)
 	private void bindHttpService(HttpService service, ServiceReference ref) {
 		httpservice = service;
 
 		if (ref.getProperty(PROP_HTTP_PORT) != null) {
 			httpport = valueOf((String) ref.getProperty(PROP_HTTP_PORT));
 		} else if (System.getProperty(PROP_HTTP_PORT) != null) {
 			httpport = valueOf((String) System.getProperty(PROP_HTTP_PORT));
 		} else {
 			httpport = DEFAULT_HTTP_PORT;
 			logger.log(
 					LOG_WARNING,
 					"A default value ("
 							+ httpport
 							+ ") has been set to the http port, this could lead to a bad url property value.");
 		}
 	}
 
 	/**
 	 * Register the servlet contrainer or reload it. Must be called after adding
 	 * or removing a class into the ResourceConfig.
 	 */
 	private void reloadServlet() {
 		if (container == null) {
 			container = new JerseyServletBridge(this);
 			try {
 				httpservice.registerServlet(rootName, container,
 						new Hashtable<String, Object>(), null);
 			} catch (Exception e) {
 				throw new RuntimeException(
 						"Cannot register the JerseyServlet bridge", e);
 			}
 		} else {
 			//no more resources
 			if (rsconfig.isEmpty()){
 				//unload the container & unpublish the servlet
 				httpservice.unregister(rootName);
 				container = null;
 			}else {
 				container.reload();
 			}
 		}
 	}
 
 	/*----------------------------*
 	 *   ExporterService methods  *
 	 *----------------------------*/
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ow2.chameleon.rose.AbstractExporterComponent#createEndpoint(org.osgi
 	 * .framework.ServiceReference, java.util.Map)
 	 */
 	protected EndpointDescription createEndpoint(ServiceReference sref,
 			Map<String, Object> extraProperties) {
 
 		// Get the service object
 		Object service = context.getService(sref);
 
 		Class<?> klass = service.getClass();
 
 		// Release the reference
 		context.ungetService(sref);
 
 		//check if class is annotated by @Path
 		if (klass.isAnnotationPresent(Path.class)) {
			extraProperties.put(PROP_PATH, klass.getAnnotation(Path.class).value());
 			// Set the url property
			extraProperties.put(RoSeConstants.ENDPOINT_URL,myurl+klass.getAnnotation(Path.class)
 					.value());
 		} else {
 			//Works only with jax-rs annotations.
 			throw new IllegalArgumentException("Exported class is not annotated by @Path, cannot be exported");
 		}
 
 		// create the endpoint description
 		EndpointDescription desc = new EndpointDescription(sref,
 				extraProperties);
 
 		addRessource(service, klass);
 
 		return desc;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ow2.chameleon.rose.AbstractExporterComponent#destroyEndpoint(org.
 	 * osgi.service.remoteserviceadmin.EndpointDescription)
 	 */
 	protected void destroyEndpoint(EndpointDescription endesc) {
 		String pathName = (String) endesc.getProperties().get(PROP_PATH);
 		rsconfig.removeComponentProvider(pathName);
 		try {
 			reloadServlet();
 		}catch (RuntimeException e){
 			logger.log(LOG_ERROR, "An exception occured while destroying the endpoint of id " +endesc.getId(),e);
 		}
 		
 		logger.log(LOG_DEBUG, "The endpoint of id: " + endesc.getId()
 				+ " and his associated ressource: " + pathName
 				+ " is no more available along.");
 	}
 
 	/**
 	 * Create a ManagedComponentProvider and register it.
 	 * 
 	 * @param instance
 	 * @param klass
 	 * @throws IllegalArgumentException
 	 */
 	private void addRessource(Object instance, Class<?> klass)
 			throws IllegalArgumentException {
 		// Create the managed component provider and add the class to the
 		// ressource config
 		rsconfig.addComponentProvider(klass, new ManagedComponentProvider(
 				instance));
 
 		// reload the servlet
 		try {
 			reloadServlet();
 		} catch (RuntimeException e) {
 			rsconfig.removeComponentProvider(klass.getAnnotation(Path.class)
 					.value());
 			throw e;
 		}
 	}
 
 	/*------------------------------------------------*
 	 *  IoCComponentProviderFactory methods           *
 	 *------------------------------------------------*/
 
 	/**
 	 * @return The ResourceConfig of this IoCComponentProviderFactory
 	 */
 	ResourceConfig getResourceConfig() {
 		return rsconfig;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seecom.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory#
 	 * getComponentProvider(java.lang.Class)
 	 */
 	public IoCComponentProvider getComponentProvider(final Class<?> klass) {
 		return getComponentProvider(null, klass);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seecom.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory#
 	 * getComponentProvider(com.sun.jersey.core.spi.component.ComponentContext,
 	 * java.lang.Class)
 	 */
 	public IoCComponentProvider getComponentProvider(ComponentContext ccontext,
 			final Class<?> klass) {
 		System.out
 				.println("Get component Provider " + klass.getCanonicalName());
 		// TODO What about the context
 
 		// Singleton case
 		// Check if an instance is available as an OSGi services
 		if (rsconfig.isManaged(klass)) {
 			// The ressource is an OSGi service, return an
 			// OSGiManagedComponentProvider
 			return rsconfig.getComponentProvider(klass);
 		}
 
 		// Otherwise ?
 		// For now, we let jersey handle the creation of the ressource
 		return new ProxiedComponentProvider(klass);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.chameleon.rose.AbstractExporterComponent#getLogService()
 	 */
 	protected LogService getLogService() {
 		return logger;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.chameleon.rose.ExporterService#getConfigPrefix()
 	 */
 	public List<String> getConfigPrefix() {
 		return Arrays.asList(configs);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.chameleon.rose.AbstractExporterComponent#getRoseMachine()
 	 */
 	protected RoseMachine getRoseMachine() {
 		return machine;
 	}
 }
