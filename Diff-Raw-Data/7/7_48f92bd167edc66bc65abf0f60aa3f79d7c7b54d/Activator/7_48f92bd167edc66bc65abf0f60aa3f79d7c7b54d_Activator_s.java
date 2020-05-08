 /*
  * Copyright 2009 www.scribble.org
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.scribble.protocol.designer.osgi;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.scribble.protocol.designer.DesignerServices;
 import org.scribble.protocol.designer.editor.*;
 import org.scribble.protocol.designer.editor.lang.*;
 import org.scribble.protocol.designer.editor.util.*;
 import org.scribble.protocol.designer.validator.ProtocolValidator;
 import org.scribble.protocol.export.ProtocolExportManagerFactory;
 import org.scribble.protocol.export.monitor.MonitorProtocolExporter;
 import org.scribble.protocol.export.text.TextProtocolExporter;
 import org.scribble.protocol.monitor.ProtocolMonitor;
 import org.scribble.protocol.parser.ProtocolParser;
 import org.scribble.protocol.parser.ProtocolParserManager;
 import org.scribble.protocol.projection.ProtocolProjector;
 import org.scribble.protocol.validation.DefaultProtocolValidationManager;
 import org.scribble.protocol.validation.ProtocolValidationManager;
 
 import java.util.logging.*;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	//private static final String SERVICE_COMPONENT = "Service-Component";
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.scribble.protocol.designer";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		// Make sure any bundles, associated with scribble, are started (excluding
 		// the designer itself)
 		Bundle[] bundles=context.getBundles();
 
 		for (int i=0; i < bundles.length; i++) {
 			Bundle bundle=bundles[i];
 			
 			if (bundle != null) {
 				//Object val=bundle.getHeaders().get(SERVICE_COMPONENT);
 				if (bundle.getSymbolicName().startsWith("org.scribble.") &&
 						bundle.getSymbolicName().endsWith("designer") == false) {
 				
 					//if (bundle.getState() == Bundle.RESOLVED) {
 						logger.fine("Pre-empt bundle start: "+bundle);
 						bundle.start();
 					//}
 				}
 			}
 		}
 
 		// TODO: Obtain validation manager pre-initialised
 		// SCRIBBLE-29
 		
 		ProtocolValidationManager vm=new DefaultProtocolValidationManager();
		vm.addValidator(new org.scribble.protocol.validation.rules.DefaultProtocolComponentValidator());
 		
 		DesignerServices.setValidationManager(vm);
 		
 		// Obtain reference to protocol parser
 		ServiceReference sr=context.getServiceReference(ProtocolParserManager.class.getName());
 	
 		ProtocolParserManager pp=null;
 		
 		if (sr != null) {
 			pp = (ProtocolParserManager)context.getService(sr);
 		}
 			
 		if (pp != null) {
 			DesignerServices.setParserManager(pp);				
 		} else {
 			
 	        ServiceListener sl1 = new ServiceListener() {
 	        	public void serviceChanged(ServiceEvent ev) {
 	        		ServiceReference sr = ev.getServiceReference();
 	        		switch(ev.getType()) {
 	        		case ServiceEvent.REGISTERED:
 	        			ProtocolParserManager pp=
 	        				(ProtocolParserManager)context.getService(sr);
 	        			DesignerServices.setParserManager(pp);
 	        			break;
 	        		case ServiceEvent.UNREGISTERING:
 	        			break;
 	        		}
 	        	}
 	        };
 	              
 	        String filter1 = "(objectclass=" + ProtocolParser.class.getName() + ")";
 	        
 	        try {
 	        	context.addServiceListener(sl1, filter1);
 	        } catch(Exception e) {
 	        	e.printStackTrace();
 	        }
 		}
 		
 		// Obtain reference to protocol parser
 		sr=context.getServiceReference(ProtocolProjector.class.getName());
 	
 		ProtocolProjector ppj=null;
 		
 		if (sr != null) {
 			ppj = (ProtocolProjector)context.getService(sr);
 		}
 			
 		if (ppj != null) {
 			DesignerServices.setProtocolProjector(ppj);				
 		} else {
 			
 	        ServiceListener sl1 = new ServiceListener() {
 	        	public void serviceChanged(ServiceEvent ev) {
 	        		ServiceReference sr = ev.getServiceReference();
 	        		switch(ev.getType()) {
 	        		case ServiceEvent.REGISTERED:
 	        			ProtocolProjector ppj=
 	        				(ProtocolProjector)context.getService(sr);
 	        			DesignerServices.setProtocolProjector(ppj);
 	        			break;
 	        		case ServiceEvent.UNREGISTERING:
 	        			break;
 	        		}
 	        	}
 	        };
 	              
 	        String filter1 = "(objectclass=" + ProtocolProjector.class.getName() + ")";
 	        
 	        try {
 	        	context.addServiceListener(sl1, filter1);
 	        } catch(Exception e) {
 	        	e.printStackTrace();
 	        }
 		}
 		
 		// Create the export manager
 		DesignerServices.setProtocolExportManager(ProtocolExportManagerFactory.getExportManager());
		ProtocolExportManagerFactory.getExportManager().addExporter(new TextProtocolExporter());
		ProtocolExportManagerFactory.getExportManager().addExporter(new MonitorProtocolExporter());
 		
 		// Register protocol monitor
 		sr=context.getServiceReference(ProtocolMonitor.class.getName());
 		
 		ProtocolMonitor pm=null;
 		
 		if (sr != null) {
 			pm = (ProtocolMonitor)context.getService(sr);
 		}
 			
 		if (pm != null) {
 			DesignerServices.setProtocolMonitor(pm);				
 		} else {
 			
 	        ServiceListener sl1 = new ServiceListener() {
 	        	public void serviceChanged(ServiceEvent ev) {
 	        		ServiceReference sr = ev.getServiceReference();
 	        		switch(ev.getType()) {
 	        		case ServiceEvent.REGISTERED:
 	        			ProtocolMonitor pm=
 	        				(ProtocolMonitor)context.getService(sr);
 	        			DesignerServices.setProtocolMonitor(pm);
 	        			break;
 	        		case ServiceEvent.UNREGISTERING:
 	        			break;
 	        		}
 	        	}
 	        };
 	              
 	        String filter1 = "(objectclass=" + ProtocolMonitor.class.getName() + ")";
 	        
 	        try {
 	        	context.addServiceListener(sl1, filter1);
 	        } catch(Exception e) {
 	        	e.printStackTrace();
 	        }
 		}
 		
 		// Register resource change listener
 		IResourceChangeListener rcl=
 			new IResourceChangeListener() {
 	
 			public void resourceChanged(IResourceChangeEvent evt) {
 		
 				try {
 					evt.getDelta().accept(new IResourceDeltaVisitor() {
 						
 				        public boolean visit(IResourceDelta delta) {
 				        	boolean ret=true;
 				        	IResource res = delta.getResource();
 				        	
 							// Determine if the change is relevant
 							if (isChangeRelevant(res,
 										delta)) {
 								
 								// Validate the resource
 								m_validator.validateResource(res);
 							}
 							
 				        	return(ret);
 				        }
 				 	});
 				} catch(Exception e) {
 					logger.log(Level.SEVERE,
 						"Failed to process resource change event",
 						e);
 				}
 			}
 		};
 		
 		// Register the resource change listener
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(rcl,
 				IResourceChangeEvent.POST_CHANGE);		
 
 	}
 	
 	protected boolean isChangeRelevant(IResource res, IResourceDelta delta) {
 		return(res instanceof IFile &&
 					DesignerServices.PROTOCOL_FILE_EXTENSION.equals(((IFile)res).getFileExtension()) &&
 					(((delta.getFlags() & IResourceDelta.CONTENT) != 0) ||
 							delta.getKind() == IResourceDelta.ADDED));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * This method logs an error against the plugin.
 	 * 
 	 * @param mesg The error message
 	 * @param t The optional exception
 	 */
 	public static void logError(String mesg, Throwable t) {
 		
 		if (getDefault() != null) {
 			Status status=new Status(IStatus.ERROR,
 					PLUGIN_ID, 0, mesg, t);
 			
 			getDefault().getLog().log(status);
 		}
 		
 		logger.severe("LOG ERROR: "+mesg+
 				(t == null ? "" : ": "+t));
 	}
 	
 	/**
 	 * Return a scanner for creating Java partitions.
 	 * 
 	 * @return a scanner for creating Java partitions
 	 */
 	 public ScribblePartitionScanner getScribblePartitionScanner() {
 		if (fPartitionScanner == null)
 			fPartitionScanner= new ScribblePartitionScanner();
 		return fPartitionScanner;
 	}
 	
 	/**
 	 * Returns the singleton Java code scanner.
 	 * 
 	 * @return the singleton Java code scanner
 	 */
 	 public RuleBasedScanner getScribbleCodeScanner() {
 	 	if (fCodeScanner == null)
 			fCodeScanner= new ScribbleCodeScanner(getScribbleColorProvider());
 		return fCodeScanner;
 	}
 	
 	/**
 	 * Returns the singleton Java color provider.
 	 * 
 	 * @return the singleton Java color provider
 	 */
 	 public ScribbleColorProvider getScribbleColorProvider() {
 	 	if (fColorProvider == null)
 			fColorProvider= new ScribbleColorProvider();
 		return fColorProvider;
 	}
 	
 	private static Logger logger = Logger.getLogger("org.scribble.protocol.designer");
 
 	public final static String SCRIBBLE_PARTITIONING= "__scribble_partitioning";   //$NON-NLS-1$
 	
 	private ScribblePartitionScanner fPartitionScanner;
 	private ScribbleColorProvider fColorProvider;
 	private ScribbleCodeScanner fCodeScanner;
 	
 	private ProtocolValidator m_validator=new ProtocolValidator();
 }
