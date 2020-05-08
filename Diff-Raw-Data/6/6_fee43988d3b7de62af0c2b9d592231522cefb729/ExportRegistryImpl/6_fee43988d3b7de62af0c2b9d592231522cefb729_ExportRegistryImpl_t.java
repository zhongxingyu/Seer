 package org.ow2.chameleon.rose.internal;
 
 import static org.osgi.framework.Constants.OBJECTCLASS;
 import static org.osgi.framework.FrameworkUtil.createFilter;
 import static org.osgi.service.log.LogService.LOG_ERROR;
 import static org.osgi.service.log.LogService.LOG_WARNING;
 import static org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID;
 import static org.ow2.chameleon.rose.util.RoseTools.endDescToDico;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.EndpointListener;
 import org.osgi.service.remoteserviceadmin.ExportReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.ow2.chameleon.rose.registry.ExportRegistry;
 import org.ow2.chameleon.rose.util.DefaultLogService;
 
 public class ExportRegistryImpl implements ExportRegistry {
 	
 	private static final String FILTER = "(" + OBJECTCLASS + "=" + ExportReference.class.getName() + ")";
 	
 	/**
 	 * Default logger.
 	 */
 	private static final LogService defaultlogger = new DefaultLogService();
 	
 	private final Map<Object, ServiceRegistration> registrations;
 	private final Map<EndpointListener,ListenerWrapper> listeners;
 	private final BundleContext context;
 
 	
 	
 	public ExportRegistryImpl(BundleContext pContext) {
 		context=pContext;
 		registrations = new HashMap<Object, ServiceRegistration>();
 		listeners = new HashMap<EndpointListener, ListenerWrapper>();
 	}
 	
 	protected void stop(){
 		synchronized (registrations) {
 			for (Iterator<ServiceRegistration> iterator = registrations.values().iterator(); iterator.hasNext();) {
 				ServiceRegistration regis = (ServiceRegistration) iterator.next();
 				regis.unregister();
 				iterator.remove();
 			}
 		}
 	}
 	
 
 	public void put(Object key, ExportReference xref) {
 		
 		synchronized (registrations) {
 			
 			if (registrations.containsKey(key)){
 				throw new IllegalStateException("An ExportReference associated with the given key as already been registered");
 			}
 			
 			ServiceRegistration reg = context.registerService(ExportReference.class.getName(), xref, endDescToDico(xref.getExportedEndpoint()));
 			registrations.put(key, reg);
 		}
 		
 	}
 
 	public ExportReference remove(Object key) {
 		ServiceRegistration sreg;
 
 		synchronized (registrations) {
 			sreg = registrations.remove(key);
 		}
 		
 		if(sreg == null){
 			return null;
 		}
 
 		ExportReference xref = (ExportReference) context.getService(sreg.getReference());
 		sreg.unregister();
 
 		return xref;
 	}
 	
 	public boolean contains(ExportReference xref) {
 		StringBuilder filter = new StringBuilder("(");
 		filter.append(ENDPOINT_ID);
 		filter.append("=");
 		filter.append(xref.getExportedEndpoint().getId());
 		filter.append(")");
 		
 		synchronized (registrations) {
 			try {
 				return context.getServiceReferences(ExportReference.class.getName(),filter.toString()) != null;
 			} catch (InvalidSyntaxException e) {
 				//XXX What would Dr Gordon Freeman do ?
 				return false;
 			}
 		}
 	}
 	
 	/*-----------------------------------------*
 	 * ExportRegistryListening service methods *
 	 *-----------------------------------------*/
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.registry.ExportRegistryListening#addEndpointListener(org.osgi.service.remoteserviceadmin.EndpointListener)
 	 */
 	public void addEndpointListener(EndpointListener listener) {
 		try {
 			addEndpointListener(listener, null);
 		} catch (InvalidSyntaxException e) {
 			getLogger().log(LOG_ERROR, "This exeception should not occured. ",e);
 			assert false; //impossible right ?
 		}
 	}
 
 	public void addEndpointListener(EndpointListener listener, String filter)
 			throws InvalidSyntaxException {
 		
 		//Check the filter if != null
 		if (filter != null){
 			createFilter(filter);
 		}
 		
 		//XXX The filter must not contains the ObjectClass filter.
 		
 		synchronized (listeners) {
 
 			ListenerWrapper slist;
 
 			// update if was already present
 			if (listeners.containsKey(listener)) {
 
 				listeners.get(listener).setFilter(filter);
 				
 			} else { // create otherwise
 				slist = new ListenerWrapper(listener, filter);
 				// add the listeners to the listeners map.
 				listeners.put(listener, slist);
 			}
 
 			
 
 		}
 		
 	}
 
 	public void removeEndpointListener(EndpointListener listener) {
 		synchronized (listeners) {
 			if (listeners.containsKey(listener)){
 				listeners.remove(listener).close(); //remove and close ! (stop the tracker)
 			}
 		}
 	}
 	
 	
 	
 	/**
 	 * InnerClass, wrap an EndpointListener in a {@link ServiceTrackerCustomizer}
 	 */
 	public final class ListenerWrapper implements ServiceTrackerCustomizer {
 		private ServiceTracker tracker;
 		private final EndpointListener listener;
 		private String filter;
 		
 		
 		private ListenerWrapper(EndpointListener pListener,String pFilter) throws InvalidSyntaxException {
 			listener = pListener; 
			
			//Check if the filter is null
			if (pFilter == null){
				pFilter=""; //set to an empty string.
			}
			
 			Filter ofilter = createFilter("(&" +FILTER + pFilter +")");
 			tracker = new ServiceTracker(context, ofilter, this);
 			filter = pFilter;
 			tracker.open();
 		}
 		
 		public void close(){
 			tracker.close();
 		}
 		
 		private void setFilter(String pFilter) throws InvalidSyntaxException{
 			//update only if the filter are not equal
 			if ( (pFilter!= filter) && (pFilter != null && !pFilter.equals(filter))){
 				Filter ofilter = createFilter("(&" +FILTER + pFilter +")");
 				filter=pFilter;
 				tracker.close();
 				tracker = new ServiceTracker(context,ofilter,this);
 				tracker.open();
 			}
 		}
 
 		public Object addingService(ServiceReference reference) {
 			ExportReference xref = (ExportReference) context.getService(reference);
 			listener.endpointAdded(xref.getExportedEndpoint(), filter);
 			return xref.getExportedEndpoint();
 		}
 
 		public void modifiedService(ServiceReference reference, Object service) {
 			getLogger().log(LOG_WARNING, "Modification of an ExportReference " +
 									"is not supported. EndpointDescription: " +
 									service);
 		}
 
 		public void removedService(ServiceReference reference, Object service) {
 			listener.endpointRemoved((EndpointDescription) service, filter);
 		}
 	}
 
 	
 	/*------------------------*
 	 * Convenient log methods *
 	 *------------------------*/
 	
 	private LogService getLogger(){
 		LogService logger = null;
 		ServiceReference sref = context.getServiceReference(LogService.class.getName());
 		if (sref != null){
 			logger = (LogService) context.getService(sref);
 			context.ungetService(sref);
 		}
 		
 		return logger!=null ?logger : defaultlogger;
 	}
 }
