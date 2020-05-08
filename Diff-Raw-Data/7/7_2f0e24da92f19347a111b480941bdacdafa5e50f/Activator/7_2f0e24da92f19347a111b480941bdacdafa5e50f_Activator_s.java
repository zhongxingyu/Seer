 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.tools.common.osgi;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.savara.protocol.util.ProtocolServices;
 import org.savara.tools.common.eclipse.BundleRegistry;
 import org.savara.tools.common.generation.Generator;
 import org.savara.tools.common.generation.ui.GenerateDialog;
 import org.savara.tools.common.logging.EclipseLogger;
 import org.savara.tools.common.properties.PropertyDefinitions;
 import org.scribble.common.resource.FileContent;
 import org.scribble.protocol.DefaultProtocolContext;
 import org.scribble.protocol.export.DefaultProtocolExportManager;
 import org.scribble.protocol.model.ProtocolModel;
 import org.scribble.protocol.parser.DefaultProtocolParserManager;
 import org.scribble.protocol.parser.ProtocolParser;
 import org.scribble.protocol.parser.antlr.ANTLRProtocolParser;
 import org.scribble.protocol.projection.impl.ProtocolProjectorImpl;
 import org.scribble.protocol.validation.DefaultProtocolValidationManager;
 import org.scribble.protocol.validation.rules.DefaultProtocolComponentValidator;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin implements IStartup {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.savara.tools.core";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	private static final Logger LOG=Logger.getLogger(Activator.class.getName());
 	
     /**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		
         ProtocolServices.setParserManager(new DefaultProtocolParserManager());
 
         ANTLRProtocolParser pp=new ANTLRProtocolParser();
         
         ProtocolServices.getParserManager().getParsers().add(pp);
         
         ProtocolServices.setProtocolProjector(new ProtocolProjectorImpl());
         
         ProtocolServices.setValidationManager(new DefaultProtocolValidationManager());
         ProtocolServices.getValidationManager().getValidators().add(new DefaultProtocolComponentValidator());
         
         ProtocolServices.setProtocolExportManager(new DefaultProtocolExportManager());
         ProtocolServices.getProtocolExportManager().getExporters().add(
         		new org.scribble.protocol.export.text.TextProtocolExporter());
         ProtocolServices.getProtocolExportManager().getExporters().add(
         		new org.scribble.protocol.export.monitor.MonitorProtocolExporter());
         
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
 								validateResource(res);
 							}
 							
 				        	return(ret);
 				        }
 				 	});
 				} catch(Exception e) {
 					LOG.log(Level.SEVERE, "Failed to process resource change event", e);
 				}
 			}
 		};
 	
 		// Register the resource change listener
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(rcl,
 				IResourceChangeEvent.POST_CHANGE);		
 
 		try {
 			// Initialize list of generators
 			IExtensionRegistry registry = Platform.getExtensionRegistry();
 			IExtensionPoint point = registry.getExtensionPoint("org.savara.tools.common.generation.Generator");
 	
 			if (point != null) {
 				IExtension[] extensions = point.getExtensions();
 				
 				for (int i = 0; i < extensions.length; i++) {
 					for (int j=0; j < extensions[i].getConfigurationElements().length; j++) {
 						
 						if (extensions[i].getConfigurationElements()[j].getName().equals("generator")) {
 							IConfigurationElement elem=extensions[i].getConfigurationElements()[j];
 							
 							if (LOG.isLoggable(Level.FINE)) {
 								LOG.fine("Generator extension: "+elem.getAttribute("class"));
 							}
 							
 							try {
 								Object am=elem.createExecutableExtension("class");	
 								
 								if (am instanceof Generator) {
 									GenerateDialog.addGenerator((Generator)am);
 								} else {
 									LOG.severe("Failed to load generator: "+am);
 								}
 							} catch(Exception e) {
 								LOG.log(Level.SEVERE, "Failed to load generator", e);
 							}
 						}
 					}
 				}
 			}
 		} catch(Throwable t) {
 			// Ignore classes not found, so can be used outside Eclipse
 		}
 		
 		try {
 			// Initialize list of generators
 			IExtensionRegistry registry = Platform.getExtensionRegistry();
 			IExtensionPoint point = registry.getExtensionPoint("org.scribble.protocol.parser.ProtocolParser");
 	
 			if (point != null) {
 				IExtension[] extensions = point.getExtensions();
 				
 				for (int i = 0; i < extensions.length; i++) {
 					for (int j=0; j < extensions[i].getConfigurationElements().length; j++) {
 						
 						if (extensions[i].getConfigurationElements()[j].getName().equals("parser")) {
 							IConfigurationElement elem=extensions[i].getConfigurationElements()[j];
 							
 							if (LOG.isLoggable(Level.FINE)) {
 								LOG.fine("Protocol parser extension: "+elem.getAttribute("class"));
 							}
 							
 							try {
 								Object am=elem.createExecutableExtension("class");	
 								
 								if (am instanceof ProtocolParser) {
 									ProtocolServices.getParserManager().getParsers().add((ProtocolParser)am);
 
 									String bundleName=elem.getAttribute("bundle");
 									
 									Bundle bundle=Platform.getBundle(bundleName);
 									
 									if (bundle != null) {
 										BundleRegistry.register(bundle);
 									}
 								} else {
 									LOG.severe("Failed to load protocol parser: "+am);
 								}
 							} catch(Exception e) {
 								LOG.log(Level.SEVERE, "Failed to load protocol parser", e);
 							}
 						}
 					}
 				}
 			}
 		} catch(Throwable t) {
 			// Ignore classes not found, so can be used outside Eclipse
 		}
 	}
 
 	/**
 	 * This method validates the supplied resource.
 	 * 
 	 * @param res The resource
 	 */
 	protected void validateResource(IResource res) {
 		
         try {
              FileContent content=new FileContent(((IFile)res).getRawLocation().toFile());
              
              if (ProtocolServices.getParserManager().isParserAvailable(content)) {
 
                  DefaultProtocolContext context=new DefaultProtocolContext();
                  context.setProtocolParserManager(ProtocolServices.getParserManager());
                  
                  EclipseLogger journal=new EclipseLogger((IFile)res);
 
                  ProtocolModel pm=ProtocolServices.getParserManager().parse(context, content, journal);
             	 
                 if (!journal.hasErrorOccurred()) {
                 	 ProtocolServices.getValidationManager().validate(context, pm, journal);
                  }
                  
                  journal.finished();
              }
             
         } catch (Exception e) {
            Activator.logError("Failed to record validation issue on resource '"+res+"'", e);
         }
 	}
 	
 	/**
 	 * This method determines whether the supplied resource
 	 * change event is relevant.
 	 * 
 	 * @param res The resource
 	 * @param deltaFlags The flags
 	 * @return Whether the change is relevant
 	 */
 	protected boolean isChangeRelevant(IResource res, IResourceDelta delta) {
 		boolean ret=false;
 
 		if (res != null && PropertyDefinitions.isValidationEnabled(res) &&
 				(((delta.getFlags() & IResourceDelta.CONTENT) != 0) ||
 				delta.getKind() == IResourceDelta.ADDED)) {
 			ret = true;
 		}
 
 		return(ret);
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
 		
 		LOG.log(Level.SEVERE, "LOG ERROR: "+mesg+
 				(t == null ? "" : ": "+t), t);
 	}
 
 	@Override
 	public void earlyStartup() {
 	}
 }
