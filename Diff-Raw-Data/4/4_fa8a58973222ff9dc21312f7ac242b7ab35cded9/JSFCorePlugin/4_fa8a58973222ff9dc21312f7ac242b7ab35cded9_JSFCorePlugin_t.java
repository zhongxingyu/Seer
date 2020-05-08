 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *    Ian Trimble - JSFLibraryRegistry work
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.internal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.ArchiveFile;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistry;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryFactory;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.PluginProvidedJSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.adapter.MaintainDefaultImplementationAdapter;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl.JSFLibraryRegistryPackageImpl;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.util.JSFLibraryRegistryResourceFactoryImpl;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.util.JSFLibraryRegistryResourceImpl;
 import org.eclipse.jst.jsf.core.internal.provisional.jsflibraryregistry.PluginProvidedJSFLibraryCreationHelper;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.eclipse.wst.common.frameworks.internal.WTPPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 /**
  * JSF Core plugin.
  * 
  * @author Gerry Kessler - Oracle, Ian Trimble - Oracle
  */
 public class JSFCorePlugin extends WTPPlugin {
 	/**
 	 * The plugin id
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.jst.jsf.core";//org.eclipse.jst.jsf.core.internal.JSFCorePlugin"; //$NON-NLS-1$
 
 	/**
 	 * The JSF facet identifier
 	 */
 	public static final String FACET_ID = "jst.jsf"; //$NON-NLS-1$
 	
 	// The shared instance.
 	private static JSFCorePlugin plugin;
 
 	// The workspace-relative part of the URL of the JSF Library Registry
 	// persistence store.
 	private static final String JSF_LIBRARY_REGISTRY_URL = ".metadata/.plugins/org.eclipse.jst.jsf.core/JSFLibraryRegistry.xml"; //$NON-NLS-1$
 
 	// The NS URI of the JSF Library Registry's Ecore package. (Must match
 	// setting on package in Ecore model.)
 	private static final String JSF_LIBRARY_REGISTRY_NSURI = "http://www.eclipse.org/webtools/jsf/schema/jsflibraryregistry.xsd"; //$NON-NLS-1$
 
 	/**
 	 * The id of the library extension point
 	 */
 	public static final String LIB_EXT_PT = "jsfLibraries"; //$NON-NLS-1$
 
 	// The JSF Library Registry EMF resource instance.
 	private JSFLibraryRegistryResourceImpl jsfLibraryRegistryResource = null;
 
 	// The JSF Library Registry instance.
 	private JSFLibraryRegistry jsfLibraryRegistry = null;
 
     private IPreferenceStore  preferenceStore;
 
 	/**
 	 * The constructor.
 	 */
 	public JSFCorePlugin() {
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 * @param context 
 	 * @throws Exception 
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		loadJSFLibraryRegistry();
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 * @param context 
 	 * @throws Exception 
 	 */
 	public void stop(BundleContext context) throws Exception {
 		saveJSFLibraryRegistry();
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * @return the shared instance
 	 */
 	public static JSFCorePlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the JSFLibraryRegistry EMF object.
 	 * 
 	 * @return the JSFLibraryRegistry EMF object.
 	 */
 	public JSFLibraryRegistry getJSFLibraryRegistry() {
 		return jsfLibraryRegistry;
 	}
 
 	/**
 	 * Loads the JSFLibraryRegistry EMF object from plugin-specfic workspace
 	 * settings location. (Called from start(BundleContext).)
 	 */
 	public void loadJSFLibraryRegistry() {
 		try {
 			URL jsfLibRegURL = new URL(Platform.getInstanceLocation().getURL(), JSF_LIBRARY_REGISTRY_URL);
 			URI jsfLibRegURI = URI.createURI(jsfLibRegURL.toString());
 			EPackage.Registry.INSTANCE.put(JSF_LIBRARY_REGISTRY_NSURI, JSFLibraryRegistryPackageImpl.init());
 			JSFLibraryRegistryResourceFactoryImpl resourceFactory = new JSFLibraryRegistryResourceFactoryImpl();
 			jsfLibraryRegistryResource = (JSFLibraryRegistryResourceImpl)resourceFactory.createResource(jsfLibRegURI);
 			try {
 				Map options = new HashMap();
 				//disable notifications during load to avoid changing stored default implementation
 				options.put(XMLResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
 				jsfLibraryRegistryResource.load(options);
 				jsfLibraryRegistry = (JSFLibraryRegistry)jsfLibraryRegistryResource.getContents().get(0);
 				loadJSFLibraryExtensions();
 			} catch(IOException ioe) {
 				/**
 				 * Remove the 3rd throwable parameter in the statement below 
 				 * to surpress error stack in log file since it is only 
 				 * informational.  Bug 144947.
 				 */  
 				//log(IStatus.INFO, Messages.JSFLibraryRegistry_NoLoadCreatingNew, ioe);
				//removed below [174679]
//				log(IStatus.INFO, Messages.JSFLibraryRegistry_NoLoadCreatingNew);
				
 				jsfLibraryRegistry = JSFLibraryRegistryFactory.eINSTANCE.createJSFLibraryRegistry();
 				jsfLibraryRegistryResource.getContents().add(jsfLibraryRegistry);
 				loadJSFLibraryExtensions();
 			}
 			//add adapter to maintain default implementation
 			if (jsfLibraryRegistry != null) {
 				jsfLibraryRegistry.eAdapters().add(MaintainDefaultImplementationAdapter.getInstance());
 			}
 		} catch(MalformedURLException mue) {
 			log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorCreatingURL, mue);
 		}
 	}
 
 	/**
 	 * Creates library registry items from extension points.
 	 */
 	private void loadJSFLibraryExtensions() {
 		try {
 			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(JSFCorePlugin.PLUGIN_ID, JSFCorePlugin.LIB_EXT_PT);
 			IExtension[] extensions = point.getExtensions();
 			for (int i=0;i < extensions.length;i++){
 				IExtension ext = extensions[i];
 				for (int j=0;j < ext.getConfigurationElements().length;j++){
 					PluginProvidedJSFLibraryCreationHelper newLibCreator = new PluginProvidedJSFLibraryCreationHelper(ext.getConfigurationElements()[j]);						
 					JSFLibrary newLib = newLibCreator.create();
 					
 					/**
 					 * Additional check on if a plug-in contributes jsflibraries is an expanded folder.
 					 * Fix related to bug 144954.  
 					 * 
 					 * It would be ideal to check if a plug-in is distributed as a JAR 
 					 * before a JSFLibrary is created.
 					 * 
 					 * This is a temporary solution since JARs in a JAR case is not 
 					 * supported in this release.  Bug 14496.
 					 */
 					if (newLib != null && isJSFLibinExpandedFolder(newLib))
 						jsfLibraryRegistry.addJSFLibrary(newLib);
 				}
 			}
 		} catch (InvalidRegistryObjectException e) {
 			log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorLoadingFromExtPt, e);
 		}
 	}
 	
 	/**
 	 * Saves the JSFLibraryRegistry EMF object from plugin-specfic workspace
 	 * settings location. (Called from stop(BundleContext).)
 	 * @return true if save is successful
 	 */
 	public boolean saveJSFLibraryRegistry() {
 		boolean saved = false;
 		if (jsfLibraryRegistryResource != null) {
 			try {
 				jsfLibraryRegistryResource.save(Collections.EMPTY_MAP);
 				saved = true;
 			} catch(IOException ioe) {
 				log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorSaving, ioe);
 			}
 		} else {
 			log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorSaving);
 		}
 		return saved;
 	}
 
 	/**
 	 * @param e
 	 * @param msg
 	 */
 	public static void log(final Exception e, final String msg) {
 		final ILog log = getDefault().getLog();
 
 		log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e));
 	}
 
 	/**
 	 * Logs using the default ILog implementation provided by getLog().
 	 * 
 	 * @param severity Severity (IStatus constant) of log entry
 	 * @param message Human-readable message describing log entry
 	 * @param ex Throwable instance (can be null)
 	 */
 	public static void log(int severity, String message, Throwable ex) {
 		getDefault().getLog().log(new Status(severity, PLUGIN_ID, IStatus.OK, message, ex));
 	}
 
 	/**
 	 * Logs using the default ILog implementation provided by getLog().
 	 * 
 	 * @param severity Severity (IStatus constant) of log entry
 	 * @param message Human-readable message describing log entry
 	 */
 	public static void log(int severity, String message) {
 		log(severity, message, null);
 	}
 
     /**
      * Logs a message for this plugin
      * 
      * @param message
      * @param t
      */
     public static void log(String message, Throwable t)
     {
         ILog log = plugin.getLog();
         log.log(
            new Status(
              IStatus.ERROR, plugin.getBundle().getSymbolicName(), 0, message, t));
     }
     
 	public String getPluginID() {
 		return PLUGIN_ID;
 	}
 	
 	/**
 	 * To check if a jsflibraries contribution plug-in is 
 	 * distributed in a JAR.
 	 * 
 	 * This is temporary for bug 144954.  Need to be removed when 
 	 * fixing 144996.
 	 *    
 	 * @param jsflib
 	 * @return true if jsflib is in the expanded folder
 	 */
 	private boolean isJSFLibinExpandedFolder(JSFLibrary jsflib) {
 		boolean exists = false;
 		if (jsflib instanceof PluginProvidedJSFLibrary) { 		// No need to check probably  
 			if (jsflib.getArchiveFiles().size() > 0) {
 				ArchiveFile ar = (ArchiveFile) jsflib.getArchiveFiles().get(0);
 				String resolvedSourceLocation = ar.getResolvedSourceLocation();
 				if (resolvedSourceLocation != null) {
 					exists = new File(resolvedSourceLocation).exists();
 				}		
 			}			
 		}		
 		return exists;
 	}
 
     /**
      * @return all registered symbol source providers
      */
     public synchronized static Map getVariableResolvers()
     {
         if (_registeredVariableResolvers == null)
         {
             registerVariableResolverProviders();
             if (_registeredVariableResolvers == null)
             {
                 throw new AssertionError("registerProviders failed");
             }
         }
         return Collections.unmodifiableMap(_registeredVariableResolvers);
     }
     
     private static Map    _registeredVariableResolvers;
     private final static String VARIABLE_RESOLVER_EXT_POINT_NAME = "variableresolver";
     
     private static void registerVariableResolverProviders()
     {
         _registeredVariableResolvers = new HashMap();
         loadRegisteredExtensions(VARIABLE_RESOLVER_EXT_POINT_NAME,
                                 _registeredVariableResolvers,
                                  "variableresolver");
     }
     
     /**
      * @return a map of all registered property resolvers by id
      */
     public synchronized static Map getPropertyResolvers()
     {
         if (_registeredPropertyResolvers == null)
         {
             registerPropertyResolverProviders();
             if (_registeredPropertyResolvers == null)
             {
                 throw new AssertionError("registerProviders failed");
             }
         }
         return Collections.unmodifiableMap(_registeredPropertyResolvers);
     }
     
     private static Map    _registeredPropertyResolvers;
     private final static String PROPERTY_RESOLVER_EXT_POINT_NAME = 
                                                              "propertyresolver";
     
     private static void registerPropertyResolverProviders()
     {
         _registeredPropertyResolvers = new HashMap();
         loadRegisteredExtensions(PROPERTY_RESOLVER_EXT_POINT_NAME,
                                 _registeredPropertyResolvers,
                                  "propertyresolver");
     }
     
     
     /**
      * @return a map of all registered method resolvers by id
      */
     public synchronized static Map getMethodResolvers()
     {
         if (_registeredMethodResolvers == null)
         {
             registerMethodResolverProviders();
             if (_registeredMethodResolvers == null)
             {
                 throw new AssertionError("registerProviders failed");
             }
         }
         return Collections.unmodifiableMap(_registeredMethodResolvers);
     }
 
     private static Map     _registeredMethodResolvers;
     private final static String METHOD_RESOLVER_EXT_POINT_NAME = 
                                                                "methodresolver";
     
     private static void registerMethodResolverProviders()
     {
         _registeredMethodResolvers = new HashMap();
         loadRegisteredExtensions(METHOD_RESOLVER_EXT_POINT_NAME,
                 _registeredMethodResolvers,
                  "methodresolver");
 
     }
 
     /**
      * @return a map of all registered external context providers by id
      */
     public synchronized static Map getExternalContextProviders()
     {
         if (_registeredExternalContextProviders == null)
         {
             registerExternalContextProviders();
             if (_registeredExternalContextProviders == null)
             {
                 throw new AssertionError("registerProviders failed");
             }
         }
         return Collections.unmodifiableMap(_registeredExternalContextProviders);
     }
     
     private static Map     _registeredExternalContextProviders;
     private final static String EXTERNAL_CONTEXT_EXT_POINT_NAME = 
                                                                "externalcontext";
 
     private static void registerExternalContextProviders()
     {
         _registeredExternalContextProviders = new HashMap();
         loadRegisteredExtensions(EXTERNAL_CONTEXT_EXT_POINT_NAME,
                                  _registeredExternalContextProviders,
                                  "externalcontext");
     }
     
     private static void loadRegisteredExtensions(final String extName,
                                                  final Map    registry,
                                                  final String elementName)
     {
         final IExtensionPoint point = Platform.getExtensionRegistry().
         getExtensionPoint(plugin.getBundle().getSymbolicName(), 
                 extName);
         final IExtension[] extensions = point.getExtensions();
 
         for (int i = 0; i < extensions.length; i++)
         {
             final IExtension extension = extensions[i];
             final IConfigurationElement[] elements = 
                 extension.getConfigurationElements();
             final String bundleId = extension.getContributor().getName();
             
             for (int j = 0; j < elements.length; j++)
             {
                 final IConfigurationElement element = elements[j];
                 if (elementName.equals(element.getName())
                         && element.getAttribute("class") != null
                         && element.getAttribute("id") != null)
                 {
                     final String factoryClassName = element.getAttribute("class");
                     final String id = element.getAttribute("id");
                     final Bundle bundle = Platform.getBundle(bundleId);
                     
                     if (bundle != null)
                     {
                         try
                         {
                             final Class factoryClass = 
                                 bundle.loadClass(factoryClassName);
                             
                             final Object variableResolver= 
                                 factoryClass.newInstance();
     
                             registry.put(id, variableResolver);
                         }
                         catch (Exception e)
                         {
                             final ILog        logger_ = getDefault().getLog();
                             logger_.log(new Status(IStatus.ERROR, plugin.getBundle()
                                     .getSymbolicName(), 0, 
                                     "Error loading property resolver provider extension point",e));
                         }
                     }
                 }
             }
         }
     }
     
     /**
      * @return the preference store for this bundle
      * TODO: this is copied from AbstractUIPlugin; need to upgrade to new IPreferencesService
      */
     public IPreferenceStore getPreferenceStore() {
         // Create the preference store lazily.
         if ( this.preferenceStore == null) {
             this.preferenceStore = new ScopedPreferenceStore(new InstanceScope(),getBundle().getSymbolicName());
 
         }
         return this.preferenceStore;
     }
 }
