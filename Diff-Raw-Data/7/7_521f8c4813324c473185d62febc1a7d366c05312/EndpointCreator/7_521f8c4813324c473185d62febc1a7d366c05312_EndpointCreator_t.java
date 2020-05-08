 package org.ow2.chameleon.rose.jsonrpc;
 
 import static java.lang.Integer.valueOf;
 import static java.util.Arrays.asList;
 import static org.osgi.service.log.LogService.LOG_ERROR;
 import static org.osgi.service.log.LogService.LOG_WARNING;
 import static org.ow2.chameleon.rose.RoSeConstants.ENDPOINT_CONFIG;
 import static org.ow2.chameleon.rose.RoSeConstants.ENDPOINT_URL;
 
 import java.net.URI;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.felix.ipojo.annotations.Bind;
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.ServiceProperty;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.jabsorb.JSONRPCBridge;
 import org.jabsorb.JSONRPCServlet;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.ow2.chameleon.rose.AbstractExporterComponent;
 import org.ow2.chameleon.rose.ExporterService;
 import org.ow2.chameleon.rose.RoseMachine;
 import org.ow2.chameleon.rose.introspect.ExporterIntrospection;
 
 @Component(name="RoSe_exporter.jabsorb")
 @Provides(specifications={ExporterService.class,ExporterIntrospection.class})
 public class EndpointCreator extends AbstractExporterComponent implements ExporterService,ExporterIntrospection {
 	
     /**
      * Default value for the {@link EndpointCreator#PROP_HTTP_PORT} property.
      */
 	private static final int DEFAULT_HTTP_PORT = 80;
 	
 	/**
 	 * Property of the HttpService http port.
 	 */
 	private final static String PROP_HTTP_PORT = "org.osgi.service.http.port";
 	
 	/**
 	 * Configuration supported by this component
 	 */
 	@ServiceProperty(name=ENDPOINT_CONFIG,mandatory=true,value="{json-rpc,jsonrpc,org.jabsorb}")
 	private String[] configs;
 	
     /**
      * Name of the Property needed by JSONRPCServlet, the gzip threshold.
      */
     private static final String PROP_GZIP_THRESHOLD = "gzip.threshold";
 
 
     /**
      * Values of the Property needed by JSONRPCServlet, the gzip threshold.
      */
     @ServiceProperty(name=PROP_GZIP_THRESHOLD,mandatory=true,value="200")
     private String gzip_threshold;
     
     /**
      * The Servlet name of the JSON-RPC bridge.
      */
     @ServiceProperty(name="jsonrpc.servlet.name",mandatory=true,value="/JSONRPC")
     private String servletname;
     
     /**
      * This class implements a bridge that unmarshalls JSON objects in JSON-RPC
      * request format, invokes a method on the exported object, and then
      * marshalls the resulting Java objects to JSON objects in JSON-RPC result
      * format.
      */
     private JSONRPCBridge jsonbridge;
     
     /**
     * Url compute from the servletname and the http server root.
      */
     private String myurl;
 
 	/**
 	 * Property containing the value of the
 	 * {@link EndpointCreator#PROP_HTTP_PORT} HttpService property.
 	 * Set in {@link EndpointCreator#bindHttpService(HttpService, ServiceReference)}
 	 */
     private int httpport;
     
     /**
      * Set which contains the names of the endpoint created by this factory.
      */
     private Set<String> endpointIds = new HashSet<String>();
     
 	@Requires(optional=true)
 	private LogService logger;
 	
 	/**
 	 * Set in {@link EndpointCreator#bindHttpService(HttpService, ServiceReference) bindHttpService}
 	 */
 	private HttpService httpservice;
 	
 	/**
 	 * Require the {@link RoseMachine}.
 	 */
 	@Requires(optional=false,id="rose.machine")
 	private RoseMachine machine;
 	
 	private final BundleContext context;
 	
 	public EndpointCreator(BundleContext pContext) {
 		context = pContext;
 	}
 	
 	/*--------------------------*
 	 *  Component LifeCycle     *
 	 *--------------------------*/
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractEndpointCreator#start()
 	 */
 	@Override
 	@Validate
 	protected void start() {
 		super.start();
 
         Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put(PROP_GZIP_THRESHOLD, gzip_threshold);
 
         try {
             // Registered the JSONRPCServlet
             httpservice.registerServlet(servletname, new JSONRPCServlet(), properties, null);
         } catch (NamespaceException e) {
             logger.log(LOG_ERROR, e.getMessage(), e);
         } catch (Exception e) {
             logger.log(LOG_ERROR, e.getMessage(), e);
         }
 
         // Set the bridge to a global bridge. (HttpSession are not managed here)
         // TODO support the HttpSession
         jsonbridge = JSONRPCBridge.getGlobalBridge();
         
         //compute the PROP_JABSORB_URL property
         try {
 			myurl = new URI("http://"+machine.getHost()+":"+httpport+servletname).toString(); //compute the url
 		} catch (Exception e) {
 			logger.log(LOG_ERROR, "Cannot create the URL of the Json-rpc server, this will lead to incomplete EndpointDescription.",e);
 		}
 	}
 	
 	/**
 	 * Bind the {@link HttpService} and set the {@link EndpointCreator#httpport} value.
 	 * @param service the {@link HttpService}
 	 * @param ref the {@link HttpService} {@link ServiceReference}.
 	 */
 	@SuppressWarnings("unused")
 	@Bind(aggregate=false,optional=false)
 	private void bindHttpService(HttpService service,ServiceReference ref){
 		httpservice = service;
 		
 		if (ref.getProperty(PROP_HTTP_PORT) != null){
 			httpport = valueOf((String) ref.getProperty(PROP_HTTP_PORT));
 		}
        else if (context.getProperty(PROP_HTTP_PORT) != null ){
            httpport = valueOf(context.getProperty(PROP_HTTP_PORT));
 		} else {
 			httpport = DEFAULT_HTTP_PORT;
 			logger.log( LOG_WARNING, "A default value ("+
 					    DEFAULT_HTTP_PORT + 
 					    ") has been set to the http port, this could lead to a bad " +
 					    ENDPOINT_URL + " property value.");
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractEndpointCreator#stop()
 	 */
 	@Invalidate
 	@Override
 	protected void stop() {
 		super.stop();
 		
         try { //Unregister the jsonrpc server.
             if (httpservice != null) {
                 httpservice.unregister(servletname);
             }
         } catch (RuntimeException re) {
             logger.log(LogService.LOG_ERROR, re.getMessage(), re);
         }
 	}
 
 	/*----------------------------*
 	 *  EndpointCreator methods   *
 	 *----------------------------*/
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractEndpointCreator#createEndpoint(org.osgi.framework.ServiceReference, java.util.Map)
 	 */
 	protected EndpointDescription createEndpoint(ServiceReference sref,
 			Map<String, Object> extraProperties) {
 
 		//Set the url property
 		extraProperties.put(ENDPOINT_URL, myurl);
 		
 		//create the endpoint description
 		EndpointDescription desc = new EndpointDescription(sref, extraProperties);
 		
         String id = desc.getId();
         
         Object service = context.getService(sref);
         
         //Release the reference
         context.ungetService(sref);
         
         //Check if the name is valid
         if (endpointIds.contains(id)){
             throw new IllegalArgumentException("An endpoint of id: "+id+" has already been created. Please chose a different id.");
         }
         
         if (desc.getInterfaces().size() > 1) {
             // Export all !
             jsonbridge.registerObject(id, service);
         }
 
         //only the specified interface
         else {
             String itf = desc.getInterfaces().get(0);
 
             try {
                 jsonbridge.registerObject(id, service, sref.getBundle().loadClass(itf));
             } catch (ClassNotFoundException e) {
                 throw new IllegalArgumentException("Cannot load the service interface " + itf + " from the service classloader.", e);
             }
         }
         
         endpointIds.add(id); //add the id to the list
 		return desc;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractEndpointCreator#destroyEndpoint(org.osgi.service.remoteserviceadmin.EndpointDescription)
 	 */
 	protected void destroyEndpoint(EndpointDescription endesc) {
         //Destroy the endpoint
 		if (endpointIds.remove(endesc.getId())){
 			jsonbridge.unregisterObject(endesc.getId());
 		}
         
 	}
 	
 	
 	/*-----------------*
 	 * Service Getter  *
 	 *-----------------*/
 	
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractEndpointCreator#getLogService()
 	 */
 	protected LogService getLogService() {
 		return logger;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.introspect.EndpointCreatorIntrospection#getConfigPrefix()
 	 */
 	public List<String> getConfigPrefix() {
 		return asList(configs);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractExporterComponent#getRoseMachine()
 	 */
 	protected RoseMachine getRoseMachine() {
 		return machine;
 	}
 }
 
