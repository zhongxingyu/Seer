 package org.eclipse.jem.internal.proxy.core;
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ProxyPlugin.java,v $
 *  $Revision: 1.18 $  $Date: 2004/05/21 19:26:26 $ 
  */
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Level;
 
 import org.eclipse.core.boot.BootLoader;
 import org.eclipse.core.internal.plugins.PluginRegistry;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.*;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.osgi.util.ManifestElement;
 import org.eclipse.pde.core.IModel;
 import org.eclipse.pde.core.plugin.*;
 import org.eclipse.pde.internal.core.PDECore;
 import org.eclipse.pde.internal.core.WorkspaceModelManager;
 import org.osgi.framework.*;
 
 import com.ibm.wtp.common.logger.proxy.Logger;
 import com.ibm.wtp.logger.proxyrender.EclipseLogger;
 
 /**
  * The plugin class for the org.eclipse.jem.internal.proxy.core plugin.
  */
 
 public class ProxyPlugin extends Plugin {
 	
 	/**
 	 * This interface is for a listener that needs to know if this plugin (ProxyPlugin) is being shutdown. 
 	 * It is needed because there are some extensions that get added dynamically that need to know when the
 	 * plugin is being shutdown. Can't use new bundle listener for this because it notifies AFTER shutdown.
 	 * 
 	 * @since 1.0.0
 	 */
 	public interface IProxyPluginShutdownListener {
 		/**
 		 * ProxyPlugin is in shutdown.
 		 * 
 		 * @since 1.0.0
 		 */
 		public void shutdown();
 	}
 	
 	private static ProxyPlugin PROXY_PLUGIN = null;
 		
 	// If this is set to true, then in development mode and it will try for proxy jars in directories.
 	private boolean devMode;
 	
 	private ListenerList shutdownListeners;
 
 	public ProxyPlugin(IPluginDescriptor pluginDescriptor) {
 		super(pluginDescriptor);
 		PROXY_PLUGIN = this;
 		devMode = BootLoader.inDevelopmentMode();	// TODO need to get rid of this, they use system properties. Not sure if set though.
 		
 	}
 
 	/**
 	 * Accessor method to get the singleton plugin.
 	 */
 	public static ProxyPlugin getPlugin() {
 		return PROXY_PLUGIN;
 	}
 	
 	private Logger logger;
 	public Logger getLogger() {
 		if (logger == null)
 			logger = EclipseLogger.getEclipseLogger(this);
 		return logger;
 	}	
 
 	/**
 	 * See localizeFromPluginDescriptor...
 	 * This is just a helper to pass in a Plugin where the plugin is handy instead of the IPluginDescriptor.
 	 */
 	public String localizeFromPlugin(Plugin plugin, String fileNameWithinPlugin) {
 		return localizeFromPluginDescriptor(plugin.getDescriptor(), fileNameWithinPlugin);
 	}
 	
 	/**
 	 * This will take the plugin and file name and make it local and return that
 	 * fully qualified. It will not take fragments into account.
 	 * 
 	 * If we are in development and it will pick it up from the path
 	 * that is listed in the proxy.jars file located in the plugin passed in. This allows development code to be
 	 * used in place of the actual runtime jars. If the runtime jars are found,
 	 * they will be used.
 	 * 
 	 * For example if looking for file runtime/xyz.jar in plugin abc, then in plugin directory for abc,
 	 * there should be a file called proxy.jars. This should only be in development, this file should not
 	 * be distributed for production. It would be distributed in the SDK environment when testing is desired.
 	 * 
 	 * The format of the file is:
 	 * 	runtimefile=/projectname/builddirectory
 	 *
 	 * For this to work when the actual jar is not found, the Eclipse must of been started in 
 	 * dev mode (i.e. the plugin location will be a project within the developer Eclipse. That way
 	 * we can go up one level for the current install location and assume the above projectname
 	 * will be found relative to the directory.
 	 * 
 	 * For the above example:
 	 * 	runtime/xyz.jar=/xyzproject/bin
 	 * 
 	 * It will return "." if file can't be found. It means nothing but it won't cause jvm to crash.
 	 */
 	public String localizeFromPluginDescriptor(IPluginDescriptor pluginDescriptor, String filenameWithinPlugin) {
 		URL url = urlLocalizeFromPluginDescriptor(pluginDescriptor, filenameWithinPlugin);
 		return url != null ? url.getFile() : "."; //$NON-NLS-1$
 	}
 	
 	/**
 	 * localizeFromPluginDescriptorAndFragments.
 	 * Just like localizeFromPluginDescriptor except it will return an array of Strings. It will look for the filename
 	 * within the plugin and any fragments of the plugin. If none are found, an empty array will be returned.
 	 * 
 	 * To find the files in the fragments that are in the runtime path (i.e. libraries), it will need to use a suffix,
 	 * This is because the JDT will get confused if a runtime jar in a fragment has the same name
 	 * as a runtime jar in the main plugin. So we will use the following search pattern:
 	 * 
 	 * 1) Find in all of the fragments those that match the name exactly
 	 * 2) Find in all of the fragments, in their runtime path (library stmt), those that match the name 
 	 *    but have a suffix the same as the uniqueid of the fragment (preceeded by a period). This is so that it can be easily
 	 *    found but yet be unique in the entire list of fragments. For example if looking for "runtime/xyz.jar"
 	 *    and we have fragment "a.b.c.d.frag", then in the runtime path we will look for the file
 	 *    "runtime/xyz.a.b.c.d.frag.jar".
 	 * 
 	 * If the files in the fragments are not in the fragments library path then it can have the same name.
 	 * 
 	 * This is useful for nls where the nls for the filename will be in one or more of the fragments of the plugin.
 	 */
 	public String[] localizeFromPluginDescriptorAndFragments(IPluginDescriptor pluginDescriptor, String filenameWithinPlugin) {
 		URL[] urls = urlLocalizeFromPluginDescriptorAndFragments(pluginDescriptor, filenameWithinPlugin);
 		String[] result = new String[urls.length];
 		for (int i = 0; i < urls.length; i++) {
 			result[i] = urls[i].getFile();
 		}
 		return result;
 	}
 
 
 	/**
 	 * See localizeFromPluginDescriptorAndFragments...
 	 * This is a helper to return a list of URLs instead.
 	 */
 	public URL[] urlLocalizeFromPluginDescriptorAndFragments(IPluginDescriptor pluginDescriptor, String filenameWithinPlugin) {
 
 		// TODO Need to switch to OSGi API when stable. This will not pick up non-legacy Bundles.
 		try {
 			Bundle[] fragments = Platform.getFragments(pluginDescriptor.getPlugin().getBundle()); // See if there are any fragments
 			if (fragments == null || fragments.length == 0) {
 				URL result = urlLocalizeFromPluginDescriptor(pluginDescriptor, filenameWithinPlugin);
 				return result != null ? new URL[] { result } : new URL[0];
 			} else {
 				ArrayList urls = new ArrayList(fragments.length + 1);
 				URL url = urlLocalizeFromPluginDescriptor(pluginDescriptor, filenameWithinPlugin);
 				if (url != null)
 					urls.add(url);
 				for (int i = 0; i < fragments.length; i++) {
 					Bundle fragment = fragments[i];
 					url = fragment.getEntry(filenameWithinPlugin);
 					if (url != null)
 						urls.add(url);
 					// Also, look through the libraries of the fragment to see if one matches the special path.				
 					// This is where one of the runtime libraries has the fragment id in it. 
 					// TODO This needs to be completely relooked at when we have a stable OSGi API. Not sure how
 					// this will work with that. (As for why we are doing this, look at the comment for localizeFromPluginDescriptorAndFragments
 					String classpath = (String) fragment.getHeaders().get(Constants.BUNDLE_CLASSPATH);
 					try {
 						ManifestElement[] classpaths = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, classpath);
 						if (classpaths != null && classpaths.length > 0) {
 							int extndx = filenameWithinPlugin.lastIndexOf('.');
 							String libFile = null;
 							if (extndx != -1)
 								libFile =
 									filenameWithinPlugin.substring(0, extndx)
 										+ '.'
 										+ fragment.getBundleId()
 										+ filenameWithinPlugin.substring(extndx);
 							else
 								libFile = filenameWithinPlugin + '.' + fragment.getBundleId();
 							for (int j = 0; j < classpaths.length; j++) {
 								IPath cp = new Path(classpaths[j].getValue());
 								// The last segment should be the file name. That is the name we are looking for.
 								if (libFile.equals(cp.lastSegment())) {
 									url = fragment.getEntry(classpaths[j].getValue());
 									// Though the actual classpath entry is the file we are looking for.
 									if (url != null)
 										urls.add(url);
 									break;
 								}
 							}
 						}
 					} catch (BundleException e) {
 						ProxyPlugin.getPlugin().getLogger().log(e, Level.INFO);
 					}
 				}
 				return (URL[]) urls.toArray(new URL[urls.size()]);
 			}
 		} catch (CoreException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e, Level.INFO);
 			return new URL[0];
 		}
 	}
 	
 	private static final IPath PROXYJARS_PATH = new Path("proxy.jars");
 	
 	/**
 	 * @see ProxyPlugin#localizeFromPluginDescriptor(IPluginDescriptor, String)
 	 * 
 	 * This is just a helper to return a url instead.
 	 * 
 	 * @param pluginDescriptor
 	 * @param filenameWithinPlugin
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	public URL urlLocalizeFromPluginDescriptor(IPluginDescriptor pluginDescriptor, String filenameWithinPlugin) {
 		return urlLocalizeFromPluginDescriptor(pluginDescriptor, new Path(filenameWithinPlugin));
 	}
 	
 	/**
 	 * @see ProxyPlugin#localizeFromPluginDescriptor(IPluginDescriptor, String)
 	 * 
 	 * This is just a helper to return a url instead.
 	 * 
 	 * @param pluginDescriptor
 	 * @param filenameWithinPlugin
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	public URL urlLocalizeFromPluginDescriptor(IPluginDescriptor pluginDescriptor, IPath filenameWithinPlugin) {					
 		try {
 			URL pvm = pluginDescriptor.find(filenameWithinPlugin);
 			if (pvm != null)
 				pvm = Platform.asLocalURL(pvm);
 			if (devMode) {
 				// Need to test if found in devmode. Otherwise we will just assume it is found. If not found on remote and moved to cache, an IOException would be thrown.
 				if (pvm != null) {
 					InputStream ios = null;
 					try {
 						ios = pvm.openStream();
 						if (ios != null)
 							return pvm; // Found it, so return it.
 					} finally {
 						if (ios != null)
 							ios.close();
 					}
 				}
 			} else
 				return pvm;
 		} catch (IOException e) {
 		}
 
 		if (devMode) {
 			// Got this far and in dev mode means it wasn't found, so we'll try for development style.
 			// It is assumed that in dev mode, we are running with the IDE as local and any 
 			// build outputs will be local so local file protocol will be returned
 			// from Platform.resolve(). We won't be running in dev mode with our entireplugin being in a jar,
 			// or on a separate system.
 			try {
 				URL pvm = pluginDescriptor.find(PROXYJARS_PATH);
 				if (pvm != null) {
 					InputStream ios = null;
 					try {
 						ios = pvm.openStream();
 						Properties props = new Properties();
 						props.load(ios);
 						String pathString = props.getProperty(filenameWithinPlugin.toString());
 						if (pathString != null) {
 							IPath path = new Path(Platform.resolve(pluginDescriptor.getInstallURL()).getFile());
 							path = path.removeLastSegments(1); // Move up one level to workspace root of development workspace.
 							path = path.append(pathString);
 							return new URL("file", null, path.toString()); //$NON-NLS-1$
 						}
 					} finally {
 						if (ios != null)
 							ios.close();
 					}
 				}
 			} catch (IOException e) {
 			}
 		}
 
 		return null; // Nothing found
 	}
 
 	/**
 	 * A helper to order the plugin descriptors into pre-req order. 
 	 * If A eventually depends on B, then B will be ahead of A in the
 	 * list of plugins. (I.e. B is a pre-req somewhere of A).
 	 *  
 	 * @param pluginDescriptorsToOrder - IPluginDescriptors of interest. The results will have these in thiee correct order.
 	 * @return An array of the IPluginDescriptors in there order from no prereqs in set to the leaves.
 	 * 
 	 * @since 1.0.0
 	 */
 	public static IPluginDescriptor[] orderPlugins(final Set pluginDescriptorsToOrder) {	
 		PluginRegistry registry = (PluginRegistry) Platform.getPluginRegistry();
 		int ndx = pluginDescriptorsToOrder.size();
 		IPluginDescriptor[] result = new IPluginDescriptor[ndx];
 		Map dependents = getDependentCounts(false, pluginDescriptorsToOrder);	// We want the inactive ones too. That way have complete order. They can be ignored later if necessary.
 		// keep iterating until all have been visited. This will actually find them in reverse order from what we
 		// want, i.e. it will find the leaves first. So we will build result array in reverse order.
 		while (!dependents.isEmpty()) {
 			// loop over the dependents list.  For each entry, if there are no dependents, visit
 			// the plugin and remove it from the list.  Make a copy of the keys so we don't end up
 			// with concurrent accesses (since we are deleting the values as we go)
 			Iterator pds = dependents.entrySet().iterator();
 			while (pds.hasNext()) {
 				Map.Entry entry = (Map.Entry) pds.next();
 				IPluginDescriptor descriptor = (IPluginDescriptor) entry.getKey() ;
 				int[] count = (int[]) entry.getValue();
 				if (count != null && count[0] <= 0) {
 					if (pluginDescriptorsToOrder.contains(descriptor))
 						result[--ndx] = descriptor;
 					pds.remove();
 					// decrement the dependent count for all of the prerequisites.
 					IPluginPrerequisite[] requires = descriptor.getPluginPrerequisites();
 					int reqSize = (requires == null) ? 0 : requires.length;
 					for (int j = 0; j < reqSize; j++) {
 						String id = requires[j].getUniqueIdentifier();
 						IPluginDescriptor prereq = registry.getPluginDescriptor(id);
 						int[] countPrereq = (int[]) dependents.get(prereq);
 						if (countPrereq != null)
 							--countPrereq[0];
 					}
 				}
 			}
 		}		
 		return result;
 	}
 	
 	
 	private static Map getDependentCounts(boolean activeOnly, Set startingSet) {
 		// TODO This needs to move to OSGi format when that API becomes stable. Currently this cannot handle
 		// plugins that are totally OSGi and not legacy.
 		IPluginRegistry registry = Platform.getPluginRegistry();
 		Map dependents = new HashMap(startingSet.size());
 		// build a table of all dependent counts.  The table is keyed by descriptor and
 		// the value the integer number of dependent plugins.
 		List processNow = new ArrayList(startingSet);
 		List processNext = new ArrayList(processNow.size());
 		if (!processNow.isEmpty()) {
 			// Go through the first time from the starting set to get an entry into the list.
 			// If there is an entry, then it won't be marked for processNext. Only new entries
 			// are added to processNext in the following loop.
 			int pnSize = processNow.size();
 			for (int i = 0; i < pnSize; i++) {
 				IPluginDescriptor pd = (IPluginDescriptor) processNow.get(i);
 				if (activeOnly && !pd.isPluginActivated())
 					continue;
 				// ensure there is an entry for this descriptor (otherwise it will not be visited)
 				int[] entry = (int[]) dependents.get(pd);
 				if (entry == null)
 					dependents.put(pd, new int[1]);
 			}
 		}
 		
 		// Now process the processNow to find the requireds, increment them, and add to processNext if never found before.
 		while (!processNow.isEmpty()) {
 			processNext.clear();
 			int pnSize = processNow.size();
 			for (int i = 0; i < pnSize; i++) {
 				IPluginDescriptor pd = (IPluginDescriptor) processNow.get(i);
 				if (activeOnly && !pd.isPluginActivated())
 					continue;			
 				IPluginPrerequisite[] requires = pd.getPluginPrerequisites();
 				int reqSize = (requires == null ? 0 : requires.length);
 				for (int j = 0; j < reqSize; j++) {
 					String id = requires[j].getUniqueIdentifier();
 					IPluginDescriptor prereq = registry.getPluginDescriptor(id);
 					if (prereq == null || activeOnly && !prereq.isPluginActivated())
 						continue;
 					int[] entry = (int[]) dependents.get(prereq);
 					if (entry == null) {
 						dependents.put(prereq, new int[] {1});
 						processNext.add(prereq);	// Never processed before, so we add it to the next process loop.
 					} else
 						++entry[0];
 				}
 			}
 			
 			// Now swap the lists so that we processNext will be now and visa-versa.
 			List t = processNext;
 			processNext = processNow;
 			processNow = t;
 		}
 		return dependents;
 	}
 		
 	/**
 	 * Add a shutdown listener
 	 * @param listener
 	 * 
 	 * @since 1.0.0
 	 */
 	public void addProxyShutdownListener(IProxyPluginShutdownListener listener) {
 		if (shutdownListeners == null)
 			shutdownListeners = new ListenerList();
 		shutdownListeners.add(listener);
 	}
 
 	/**
 	 * Remove a shutdown listener
 	 * @param listener
 	 * 
 	 * @since 1.0.0
 	 */
 	public void removeProxyShutdownListener(IProxyPluginShutdownListener listener) {
 		if (shutdownListeners != null)
 			shutdownListeners.remove(listener);
 	}
 	
 	private ILaunchConfigurationListener launchListener = new ILaunchConfigurationListener() {
 		public void launchConfigurationAdded(ILaunchConfiguration configuration) {
 			try {
 				if (!configuration.isWorkingCopy() && IProxyConstants.ID_PROXY_LAUNCH_GROUP.equals(configuration.getCategory()))
 					startCleanupJob();
 			} catch (Exception e) {
 			}
 		}
 
 		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
 			try {
 				if (!configuration.isWorkingCopy() && IProxyConstants.ID_PROXY_LAUNCH_GROUP.equals(configuration.getCategory()))
 					startCleanupJob();
 			} catch (Exception e) {
 			}
 		}
 
 		public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
 			try {
 				// On delete you can't tell the category or anything because all of that info has already removed.
 				if (!configuration.isWorkingCopy())
 					startCleanupJob();
 			} catch (Exception e) {
 			}
 		}
 	};
 	
 	private Job cleanupJob = new Job("Clean up default proxy launch configurations.") {
 		{
 			setSystem(true);	// So it doesn't show up in progress monitor. No need to interrupt user.
 			setPriority(Job.SHORT);	// A quick running job.
 		}
 		protected IStatus run(IProgressMonitor monitor) {
 			synchronized (this) {
 				if (monitor.isCanceled())
 					return Status.CANCEL_STATUS;
 			}
 			// all we want to do is find out if any launch configurations (from proxy launch group) exist for
 			// a project. If they don't, then unset the project's property. If they do, and the property is not
 			// set, then set it to NOT_SET to indicate not set, but there are some configs for it.
 			// We just gather the project names that have launch configurations.
 			try {
 				Set projectNames = new HashSet();
 				ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
 				for (int i = 0; i < configs.length; i++) {
 					if (IProxyConstants.ID_PROXY_LAUNCH_GROUP.equals(configs[i].getCategory())
 						&& (ProxyLaunchSupport.ATTR_PRIVATE == null || !configs[i].getAttribute(ProxyLaunchSupport.ATTR_PRIVATE, false)))
 						projectNames.add(configs[i].getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
 				}
 
 				IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
 				IJavaElement[] children = model.getChildren();
 				int cancelCount = 10;
 				for (int j = 0; j < children.length; j++) {
 					if (children[j].getElementType() == IJavaElement.JAVA_PROJECT) {
 						if (--cancelCount <= 0)
 							synchronized (this) {
 								cancelCount = 10;	// Rest for next set of ten.
 								// Checking on every 10 projects because they may be many projects, while only few configs.
 								// This way it will stop sooner.
 								if (monitor.isCanceled())
 									return Status.CANCEL_STATUS;
 							}						
 						IProject p = ((IJavaProject) children[j]).getProject();
 						if (projectNames.contains(p.getName())) {
 							// This project has a launch config. If it has a setting, then do nothing, else need to put on not set. 
 							if (p.getPersistentProperty(ProxyLaunchSupport.PROPERTY_LAUNCH_CONFIGURATION) == null)
 								p.getProject().setPersistentProperty(
 									ProxyLaunchSupport.PROPERTY_LAUNCH_CONFIGURATION,
 									ProxyLaunchSupport.NOT_SET);
 						} else {
 							// This project has no launch configs. Remove any setting if it exists.
 							p.setPersistentProperty(ProxyLaunchSupport.PROPERTY_LAUNCH_CONFIGURATION, (String) null);
 						}
 					}
 				}
 				return Status.OK_STATUS;
 			} catch (CoreException e) {
 				return e.getStatus();
 			}
 		}
 	};
 	
 	private void startCleanupJob() {
 		cleanupJob.cancel();	// Stop what we are doing.
 		cleanupJob.schedule(1000l);	// Schedule to start in one second.
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugin#startup()
 	 */
 	public void startup() throws CoreException {
 		super.startup();
 		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(launchListener);
 		startCleanupJob();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugin#shutdown()
 	 */
 	public void shutdown() throws CoreException {
 		// Handle case where debug plugin shuts down before we do since order not guarenteed.
 		if (DebugPlugin.getDefault() != null)
 			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(launchListener);
 		cleanupJob.cancel();	// Stop what we are doing.		
 		if (shutdownListeners != null) {
 			Object[] listeners = shutdownListeners.getListeners();
 			for (int i = 0; i < listeners.length; i++) {
 				((IProxyPluginShutdownListener) listeners[i]).shutdown();
 			}
 		}
 		super.shutdown();
 	}
 	
 	public static final String PI_CONFIGURATION_CONTRIBUTION_EXTENSION_POINT = "org.eclipse.jem.proxy.contributors";
 	public static final String PI_CONTAINER = "container";
 	public static final String PI_PLUGIN = "plugin";
 	public static final String PI_CLASS = "class";
 	
 	/*
 	 * Map of container id's to their ordered array of contribution config elements.
 	 */
 	protected Map containerToContributions = null;
 	/*
 	 * Map of plugin id's to their ordered array of contribution config elements.
 	 */
 	protected Map pluginToContributions = null;
 	
 	/**
 	 * Return the plugin ordered array of configuration elements for the given container, or <code>null</code> if not contributed.
 	 * 
 	 * @param containerid
 	 * @return Array of configuration elements or <code>null</code> if this container has no contributions.
 	 * 
 	 * @since 1.0.0
 	 */
 	public IConfigurationElement[] getContainerConfigurations(String containerid) {
 		if (containerToContributions == null)
 			processProxyContributionExtensionPoint();
 		return (IConfigurationElement[]) containerToContributions.get(containerid);
 	}
 
 	/**
 	 * Return the plugin ordered array of configuration elements for the given plugin, or <code>null</code> if not contributed.
 	 * 
 	 * @param pluginid
 	 * @return Array of configuration elements or <code>null</code> if this plugin has no contributions.
 	 * 
 	 * @since 1.0.0
 	 */
 	public IConfigurationElement[] getPluginConfigurations(String pluginid) {
 		if (pluginToContributions == null)
 			processProxyContributionExtensionPoint();
 		return (IConfigurationElement[]) pluginToContributions.get(pluginid);
 	}
 	
 	protected void processProxyContributionExtensionPoint() {
 		ContributorExtensionPointInfo info = processContributionExtensionPoint(PI_CONFIGURATION_CONTRIBUTION_EXTENSION_POINT);
 		containerToContributions = info.containerToContributions;
 		pluginToContributions = info.pluginToContributions;
 	}
 	
 	/**
 	 * Result form processContributionExtensionPoint.
 	 * 
 	 * @see ProxyPlugin#processContributionExtensionPoint(String)
 	 * @since 1.0.0
 	 */
 	public static class ContributorExtensionPointInfo {
 		/**
 		 * Map of container ids (String) to contributions (IConfigurationElement[]) that was found with that id. For each container,
 		 * the contributions will be listed in plugin prereq order.
 		 */
 		public Map containerToContributions;
 		
 		/**
 		 * Map of plugin ids (String) to contributions (IConfigurationElement[]) that was found with that id. For each plugin,
 		 * the contributions will be listed in plugin prereq order.
 		 */
 		public Map pluginToContributions;
 	}
 
 	/**
 	 * Process the extension point looking contributors. It will find entries that have the "container" or "plugin" attributes
 	 * set on them.
 	 * 
 	 * @param extensionPoint fully-qualified extension point id, including plugin id of the extension point.
 	 * @return the contributor info record.
 	 * 
 	 * @since 1.0.0
 	 */
 	public static ContributorExtensionPointInfo processContributionExtensionPoint(String extensionPoint) {	
 		// We are processing this once because it is accessed often (once per vm per project).
 		// This can add up so we get it together once here.
 		IExtensionPoint extp = Platform.getExtensionRegistry().getExtensionPoint(extensionPoint);
 		ContributorExtensionPointInfo result = new ContributorExtensionPointInfo();
 		if (extp == null) {
 			result.containerToContributions = Collections.EMPTY_MAP;
 			result.pluginToContributions = Collections.EMPTY_MAP;
 			return result;
 		}
 		
 		IExtension[] extensions = extp.getExtensions();
 		// Need to be in plugin order so that first ones processed have no dependencies on others.
 		HashMap pluginDescriptorsToExtensions = new HashMap(extensions.length);
 		for (int i = 0; i < extensions.length; i++) {
 			IPluginDescriptor desc = extensions[i].getDeclaringPluginDescriptor();
 			IExtension[] ext = (IExtension[]) pluginDescriptorsToExtensions.get(desc);
 			if (ext == null)
 				pluginDescriptorsToExtensions.put(desc, new IExtension[] {extensions[i]});
 			else {
 				// More than one extension defined in this plugin.
 				IExtension[] newExt = new IExtension[ext.length + 1];
 				System.arraycopy(ext, 0, newExt, 0, ext.length);
 				newExt[newExt.length-1] = extensions[i];
 				pluginDescriptorsToExtensions.put(desc, newExt);
 			}
 		}
 		
 		// Now order them so we process in required order.
 		IPluginDescriptor[] ordered = ProxyPlugin.orderPlugins(pluginDescriptorsToExtensions.keySet());
 		result.containerToContributions = new HashMap(ordered.length);
 		result.pluginToContributions = new HashMap(ordered.length);
 		for (int i = 0; i < ordered.length; i++) {
 			IExtension[] exts = (IExtension[]) pluginDescriptorsToExtensions.get(ordered[i]);
 			for (int j = 0; j < exts.length; j++) {
 				IConfigurationElement[] configs = exts[j].getConfigurationElements();
 				// Technically we expect the config elements to have a name of "contributor", but since that
 				// is all that can be there, we will ignore it. The content is what is important.
 				for (int k = 0; k < configs.length; k++) {
 					String container = configs[k].getAttributeAsIs(PI_CONTAINER);
 					if (container != null) {
 						List contributions = (List) result.containerToContributions.get(container);
 						if (contributions == null) {
 							contributions = new ArrayList(1);
 							result.containerToContributions.put(container, contributions);
 						}
 						contributions.add(configs[k]);
 					}
 					String plugin = configs[k].getAttributeAsIs(PI_PLUGIN);
 					if (plugin != null) {
 						List contributions = (List) result.pluginToContributions.get(plugin);
 						if (contributions == null) {
 							contributions = new ArrayList(1);
 							result.pluginToContributions.put(plugin, contributions);
 						}
 						contributions.add(configs[k]);
 					}
 				}
 			} 
 		}
 		
 		// Now go through and turn all of the contribution lists into arrays.
 		for (Iterator iter = result.containerToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			entry.setValue(((List) entry.getValue()).toArray(new IConfigurationElement[((List) entry.getValue()).size()]));
 		}
 		for (Iterator iter = result.pluginToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			entry.setValue(((List) entry.getValue()).toArray(new IConfigurationElement[((List) entry.getValue()).size()]));
 		}
 
 		return result;
 	}
 	
 	/**
 	 * For the given java project, return the maps of container paths and plugins found. The keys will be of type as specified for the parms
 	 * while the value will be Boolean, true if it was visible, and false if it wasn't.
 	 * For example if <code>/SWT_CONTAINER/subpath1</code> is found in the projects path (or from required projects), then
 	 * the container id will be added to the map. They come from the raw classpath entries of the projects.
 	 *
 	 * @param jproject
 	 * @param containerIds This map will be filled in with container ids as keys (type is <code>java.lang.String</code>) that are found in the projects build path. The value will be a Boolean, true if this container id was visible to the project (i.e. was in the project or was exported from a required project).
 	 * @param containers This map will be filled in with classpath containers as keys found in the projects build path. The value will be a Boolean as in container ids map.
 	 * @param pluginIds This map will be filled in with plugin ids as keys (type is <code>java.lang.String</code>) that are found in the projects build path. The value will be a Boolean as in container ids map.
 	 * @param projects This map will be filled in with project paths (except the top project) as keys (type is <code>org.eclipse.core.runtime.IPath</code>) that are found in the projects build path. The value will be a Boolean as in container ids map.
 	 * 
 	 * @since 1.0.0
 	 */
 	public void getIDsFound(IJavaProject jproject, Map containerIds, Map containers, Map pluginIds, Map projects) throws JavaModelException {		
 		IPath projectPath = jproject.getProject().getFullPath();
 		projects.put(projectPath, Boolean.TRUE);		
 		expandProject(projectPath, containerIds, containers, pluginIds, projects, true, true);
 		projects.remove(projectPath);	// Don't need to include itself now, was needed for testing so if ciruclar we don't get into a loop.
 	}
 	
 	/*
 	 * The passed in visible flag tells if this project is visible and its contents are visible if they are exported.
 	 * Only exception is if first is true, then all contents are visible to the top level project.
 	 */
 	private void expandProject(IPath projectPath, Map containerIds, Map containers, Map pluginIds, Map projects, boolean visible, boolean first) throws JavaModelException {
 		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.lastSegment());
 		if (res == null)
 			return;	// Not exist so don't delve into it.
 		IJavaProject project = (IJavaProject)JavaCore.create(res);
 		if (project == null || !project.exists() || !project.getProject().isOpen())
 			return;	// Not exist as a java project or not open, so don't delve into it.
 
 		IClasspathEntry[] entries = project.getRawClasspath();
 		for (int i = 0; i < entries.length; i++) {
 			IClasspathEntry entry = entries[i];
 			switch (entry.getEntryKind()) {
 				case IClasspathEntry.CPE_PROJECT:
 					if (!projects.containsKey(entry.getPath())) {
 						projects.put(entry.getPath(), first || (visible && entry.isExported()) ? Boolean.TRUE : Boolean.FALSE );
 						expandProject(entry.getPath(), containerIds, containers, pluginIds, projects, visible && entry.isExported(), false);
 					}
 					break;
 				case IClasspathEntry.CPE_CONTAINER:
 					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
 					if (!containers.containsKey(container))
 						containers.put(container, first || (visible && entry.isExported()) ? Boolean.TRUE : Boolean.FALSE );
 					if (!containerIds.containsKey(entry.getPath().segment(0)))
 						containerIds.put(entry.getPath().segment(0), first || (visible && entry.isExported()) ? Boolean.TRUE : Boolean.FALSE );					
 					break;
 				case IClasspathEntry.CPE_VARIABLE:
 					// We only care about JRE_LIB. If we have that, then we will treat it as JRE_CONTAINER. Only
 					// care about first project too, because the first project is the one that determines the JRE type.
 					if (first && "JRE_LIB".equals(entry.getPath().segment(0))) {
 						if (!containerIds.containsKey("org.eclipse.jdt.launching.JRE_CONTAINER"))
 							containerIds.put("org.eclipse.jdt.launching.JRE_CONTAINER", Boolean.TRUE);
 					}
 					break;
 				default:
 					break;
 			}
 		}		
 		
 		processPlugin(project, pluginIds, visible, first);	// expand the plugins for this project, if any.
 	}
 	
 	private void processPlugin(IJavaProject project, Map pluginIds, boolean visible, boolean first) {
 		WorkspaceModelManager wm = (WorkspaceModelManager)PDECore.getDefault().getWorkspaceModelManager();
		IPluginModelBase m = wm.getWorkspacePluginModel(project.getProject());
 		if (m instanceof IPluginModel) {
 			// it is a plugin, process it.
 			IPlugin plugin = ((IPluginModel) m).getPlugin();			
 			if (pluginIds.containsKey(plugin.getId()))
 				return;	// already processed it
 			pluginIds.put(plugin.getId(), first || visible ? Boolean.TRUE : Boolean.FALSE);			
 			expandPlugin(plugin, pluginIds, visible, first);
 		}
 		return;
 	}
 	
 	private void expandPlugin(IPlugin plugin, Map pluginIds, boolean visible, boolean first) {
 		IPluginImport[] imports = plugin.getImports();
 		for (int i = 0; i < imports.length; i++) {
 			IPluginImport pi = imports[i];
 			Boolean piValue = (Boolean) pluginIds.get(pi.getId());
 			boolean importVisible = first || (visible && pi.isReexported());
 			if (piValue != null && (!importVisible || !piValue.booleanValue()))
 				continue;	// we already processed it, this time not visible, or this time visible and was previously visible.
 			// Now either first time, or it was there before, but now visible, but this time it is visible.
 			// We want it to become visible in that case. 
 			pluginIds.put(pi.getId(), importVisible ? Boolean.TRUE : Boolean.FALSE);			
 			IPlugin pb = PDECore.getDefault().findPlugin(pi.getId(),
 				pi.getVersion(),
 				pi.getMatch());
 			if (pb != null)
 				expandPlugin(pb, pluginIds, importVisible, false);
 		}
 	}
 }
