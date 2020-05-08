 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package org.jbpm.enterprise.bundle;
 
 import java.util.Calendar;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.jbpm.enterprise.platform.ExecutionEngine;
 import org.jbpm.enterprise.platform.ExecutionEngineFactory;
 import org.jbpm.enterprise.platform.impl.DefaultExecutionEngineConfiguration;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 
 
 public class JbpmBundleActivator implements BundleActivator {
 
 	@SuppressWarnings("rawtypes")
 	private ServiceRegistration registeredEngine;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void start(BundleContext context) throws Exception {
 		// look up proper version of ExecutionEngineFactory
 		String filter = "(version=5.3.0)";
 		ServiceReference[] refs = 
 			 context.getServiceReferences(ExecutionEngineFactory.class.getName(), filter);
 		ExecutionEngineFactory eeFactory = null;
 		if (refs != null && refs.length >= 1) {
 			eeFactory = (ExecutionEngineFactory) context.getService(refs[0]);
 		} else {
 			throw new IllegalStateException("ExecutionEngineFactory not found in OSGi registry using filter " + filter);
 		}
 
 		DefaultExecutionEngineConfiguration config = new DefaultExecutionEngineConfiguration();
 		config.setChangeSet("classpath:ChangeSet.xml");
 		ExecutionEngine engine = eeFactory.newExecutionEngine(this.getClass().getClassLoader(), config);
 		
 		Dictionary<String, Object> properties = new Hashtable<String, Object>();
 		// register with version number for resolution based on version
 		properties.put("version", context.getBundle().getVersion().toString());
 		//register with valid time to be automatically selected based on current time (time must be given as milliseconds)
 		// just as an example valid for two days from now
 		Calendar cal = Calendar.getInstance();
 		
 		properties.put("validFrom", cal.getTimeInMillis());
 		cal.add(Calendar.DAY_OF_YEAR, 2);
 		properties.put("validTo", cal.getTimeInMillis());
 		// register it in OSGi service registry
 		registeredEngine = context.registerService(ExecutionEngine.class.getName(), engine, properties);
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		if (registeredEngine != null) {
 			registeredEngine.unregister();
 		}
 		
 	}
 }
