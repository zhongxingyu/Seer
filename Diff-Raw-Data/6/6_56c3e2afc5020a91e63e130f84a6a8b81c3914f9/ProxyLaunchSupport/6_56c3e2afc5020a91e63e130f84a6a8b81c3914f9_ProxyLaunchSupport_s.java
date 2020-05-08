 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ProxyLaunchSupport.java,v $
 *  $Revision: 1.6 $  $Date: 2004/03/26 23:07:45 $ 
  */
 package org.eclipse.jem.internal.proxy.core;
 
 import java.text.MessageFormat;
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.*;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
  
 /**
  * This is the used to launch the proxy registries.
  * This is a static helper class, it is not meant to be instantiated.
  * 
  * @since 1.0.0
  */
 public class ProxyLaunchSupport {
 	
 	/**
 	 * These are public only so that jem.ui can access this constant. Not meant to be accessed by others.
 	 */
 	public static final QualifiedName PROPERTY_LAUNCH_CONFIGURATION = new QualifiedName("org.eclipse.jem.proxy", "proxyLaunchConfiguration");
 	// If a project's persistent property is set with this value, that means there is at least one
 	// launch configuration with this project, but none are selected as the default. This is here
 	// so that we can check in the object contribution that if not set then don't show the menu
 	// item at all. This is to clean up the popup menu so not so cluttered.
 	// If the property is trully not set, then there is no default and there are no configurations for it.
 	public static final String NOT_SET = "...not..set..";	 
 		
 	/*
 	 * Registry of launch key to LaunchInfo classes.
 	 */
 	private static Map LAUNCH_INFO = new HashMap(2);
 	
 	/**
 	 * LaunchInfo for a launch. Stored by key and retrievable by the key.
 	 * This is only passed to launch delegates. It should not be passed on to
 	 * others, thought the IConfigurationContributionInfo may be.
 	 * 
 	 * <p>
 	 * This class is not intended to be subclassed by clients.
 	 * </p>
 	 * 
 	 * @see ProxyLaunchSupport#getInfo(String)
 	 * @see IConfigurationContributionInfo
 	 * @since 1.0.0
 	 */
 	public static class LaunchInfo {
 		/**
 		 * Contributors for this launch
 		 */
 		public IConfigurationContributor[] contributors;
 		
 		/**
 		 * The registry returned from the launch. The launch needs to set this before it returns.
 		 */
 		public ProxyFactoryRegistry resultRegistry;
 		
 
 		/**
 		 * Public only for access by other launch delegates to set up if they go outside of ProxyLaunchSupport,
 		 * e.g. IDE proxy. Must not be used for any purpose.
 		 * 
 		 * @since 1.0.0
 		 */
 		public static class LaunchSupportIConfigurationContributionInfo implements IConfigurationContributionInfo {
 			/* (non-Javadoc)
 			 * Map of containers (IClasspathContainer) found in classpath (including required projects).
 			 * This is for each project found. If there was a container in more than one project with the
 			 * id, this set will contain the container from each such project. They are not considered the
 			 * same because they come from a different project.
 			 * <p>
 			 * The key will be the containers, and the value will be a <code>Boolean</code>, where true means it
 			 * is visible to the top-level project.
 			 * <p>
 			 * This is used for determining if a project's container implements the desired contributor.
 			 * 
 			 * Will be empty if no project sent in to launch configuration.
 			 * 
 			 * @see org.eclipse.jdt.core.IClasspathContainer
 			 */
 			public Map containers = Collections.EMPTY_MAP;
 			
 			/* (non-Javadoc)
 			 * Map of unique container id strings found in classpath (including required projects).
 			 * If a container with the same id was found in more than one project, only one id will
 			 * be in this set since they are the same.
 			 * <p>
 			 * The key will be the container ids, and the value will be a <code>Boolean</code>, where true means it
 			 * is visible to the top-level project.
 			 * 
 			 * Will be empty if no project sent in to launch configuration.
 			 */
 			public Map containerIds = Collections.EMPTY_MAP;
 			
 			/* (non-Javadoc)
 			 * Set of unique plugin id strings found in classpath (including required projects).
 			 * If a required plugin with the same id was found in more than one project, only one id will
 			 * be in this set since they are the same.
 			 * <p>
 			 * The key will be the plugin ids, and the value will be a <code>Boolean</code>, where true means it
 			 * is visible to the top-level project.
 			 * 
 			 * Will be empty if no project sent in to launch configuration.
 			 */		
 			public Map pluginIds = Collections.EMPTY_MAP;;
 			
 			/* (non-Javadoc)
 			 * Map of unique projects found in classpath (including required projects), but not including top-level project.
 			 * <p>
 			 * The key will be the <code>IPath</code> for the project, and the value will be a <code>Boolean</code>, where true means it
 			 * is visible to the top-level project.
 			 * 
 			 * Will be <code>null</code> if no project sent in to launch configuration.
 			 */		
 			public Map projectPaths;
 			
 			/* (non-Javadoc)
 			 * Java project for this launch. <code>null</code> if not for a project.
 			 */
 			public IJavaProject javaProject;
 			
 			/* (non-Javadoc)
 			 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo#getContainerIds()
 			 */
 			public Map getContainerIds() {
 				return containerIds;
 			}
 			/* (non-Javadoc)
 			 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo#getContainers()
 			 */
 			public Map getContainers() {
 				return containers;
 			}
 			/* (non-Javadoc)
 			 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo#getJavaProject()
 			 */
 			public IJavaProject getJavaProject() {
 				return javaProject;
 			}
 			/* (non-Javadoc)
 			 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo#getPluginIds()
 			 */
 			public Map getPluginIds() {
 				return pluginIds;
 			}
 			/* (non-Javadoc)
 			 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo#getProjectPaths()
 			 */
 			public Map getProjectPaths() {
 				return projectPaths;
 			}
 			
 		};
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo#getJavaProject()
 		 */
 		public IJavaProject getJavaProject() {
 			return configInfo.getJavaProject();
 		}		
 		
 		/**
 		 * Return the IConfigurationContributionInfo for this launch.
 		 * @return
 		 * 
 		 * @since 1.0.0
 		 */
 		public IConfigurationContributionInfo getConfigInfo() {
 			return configInfo;
 		}
 		
 		/**
 		 * Public only so that other launch delegates types can get into it. Not meant for
 		 * general usage.
 		 */
 		public LaunchSupportIConfigurationContributionInfo configInfo = new LaunchSupportIConfigurationContributionInfo();
 		
 	}
 	
 	/**
 	 * Start an implementation using the default config for the given project.
 	 * 
 	 * @param project The project. It must be a java project, and it cannot be <code>null</code>.
 	 * @param vmTitle
 	 * @param aContribs The contributions array. It may be <code>null</code>.
 	 * @param pm
 	 * @return The created registry.
 	 * @throws CoreException
 	 * 
 	 * @since 1.0.0
 	 */
 	public static ProxyFactoryRegistry startImplementation(
 			IProject project,
 			String vmTitle,
 			IConfigurationContributor[] aContribs,
 			IProgressMonitor pm)
 				throws CoreException {
 		
 		// First find the appropriate launch configuration to use for this project.
 		// The process is:
 		//	1) See if the project's persistent property has a setting for "proxyLaunchConfiguration", if it does,
 		//		get the configuration of that name and create a working copy of it.
 		//	2) If not, then get the "org.eclipse.jem.proxy.LocalProxyLaunchConfigurationType"
 		//		and create a new instance working copy.
 
 		IJavaProject javaProject = JavaCore.create(project);
 		if (javaProject == null) {
 			throw new CoreException(
 					new Status(
 							IStatus.WARNING,
 							ProxyPlugin.getPlugin().getDescriptor().getUniqueIdentifier(),
 							0,
 							MessageFormat.format(
 									ProxyMessages.getString(ProxyMessages.NOT_JAVA_PROJECT),
 									new Object[] { project.getName()}),
 							null));
 		}
 
 		// First if specific set.
 		String launchName = project.getPersistentProperty(PROPERTY_LAUNCH_CONFIGURATION);
 		ILaunchConfiguration config = null;		
 		if (launchName != null && !NOT_SET.equals(launchName)) {
 			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
 			for (int i = 0; i < configs.length; i++) {
 				if (configs[i].getName().equals(launchName)) {
 					config = configs[i];
 					break;
 				}
 			}
 			if (config == null || !config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(project.getName())) {
 				project.setPersistentProperty(PROPERTY_LAUNCH_CONFIGURATION, (String) null);	// Config not found, or for a different project, so no longer the default.
 				config = null;
 			}
 		}
 		
 		if (config == null) {
 			ILaunchConfigurationWorkingCopy configwc = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IProxyConstants.LOCAL_LAUNCH_TYPE).newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom("LocalProxy_"+project.getName()));
 			configwc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName()); 
 			config = configwc;
 		}
 		
 		return startImplementation(config, vmTitle, aContribs, pm);
 	}
 	
 	/**
 	 * Launch a registry using the given configuration.
 	 * @param config 
 	 * @param vmTitle
 	 * @param aContribs The contributions array. It may be <code>null</code>.
 	 * @param pm
 	 * @return The registry from this configuration.
 	 * @throws CoreException
 	 * 
 	 * @since 1.0.0
 	 */
 	public static ProxyFactoryRegistry startImplementation(
 			ILaunchConfiguration config,
 			String vmTitle,
 			IConfigurationContributor[] aContribs,
 			IProgressMonitor pm)
 			throws CoreException {
 		
 		if (pm == null)
 			pm = new NullProgressMonitor();
 		
 		final ILaunchConfigurationWorkingCopy configwc = config.getWorkingCopy();
 		
 		// See if build needed or waiting or inprogress, if so, wait for it to complete. We've
 		// decided
 		// too difficult to determine if build would affect us or not, so just wait.
 		pm.beginTask("", 400);
 		pm.subTask(ProxyMessages.getString("ProxyLaunch"));
 		handleBuild(new SubProgressMonitor(pm, 100));
 				
 		if (aContribs != null) {
 			IConfigurationContributor[] newContribs = new IConfigurationContributor[aContribs.length+1];
 			System.arraycopy(aContribs, 0, newContribs, 1, aContribs.length);
 			newContribs[0] = new ProxyContributor();
 			aContribs = newContribs;
 		} else
 			aContribs = new IConfigurationContributor[] {new ProxyContributor()};
 
 		String launchKey = String.valueOf(System.currentTimeMillis());
 		LaunchInfo launchInfo = new LaunchInfo();
 		synchronized (ProxyLaunchSupport.class) {
 			while (LAUNCH_INFO.containsKey(launchKey)) {
 				launchKey += 'a'; // Just add something on to make it unique.
 			}
 			LAUNCH_INFO.put(launchKey, launchInfo);
 		}
 		
 		String projectName = configwc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
 		if (projectName != null) {
 			projectName = projectName.trim();
 			if (projectName.length() > 0) {
 				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 				IJavaProject javaProject = JavaCore.create(project);
 				if (javaProject != null && javaProject.exists()) {
 					launchInfo.configInfo.javaProject = javaProject;
 					launchInfo.configInfo.containerIds = new HashMap(5);
 					launchInfo.configInfo.containers = new HashMap(5);
 					launchInfo.configInfo.pluginIds = new HashMap(5);
 					launchInfo.configInfo.projectPaths = new HashMap(5);
 					ProxyPlugin.getPlugin().getIDsFound(javaProject, launchInfo.configInfo.containerIds, launchInfo.configInfo.containers, launchInfo.configInfo.pluginIds, launchInfo.configInfo.projectPaths);					
 					if (!launchInfo.configInfo.containerIds.isEmpty() || !launchInfo.configInfo.containers.isEmpty() || !launchInfo.configInfo.pluginIds.isEmpty()) {
 						List computedContributors = new ArrayList(launchInfo.configInfo.containerIds.size()+launchInfo.configInfo.containers.size()+launchInfo.configInfo.pluginIds.size());
 						// Note: We don't care about the visibility business here. For contributors to proxy it means
 						// some classes in the projects/plugins/etc. need configuration whether they are visible or not.
 						// This is because even though not visible, some other visible class may instantiate it. So it
 						// needs the configuration.
 						// First handle explicit classpath containers that implement IConfigurationContributor
 						for (Iterator iter = launchInfo.configInfo.containers.keySet().iterator(); iter.hasNext();) {
 							IClasspathContainer container = (IClasspathContainer) iter.next();
 							if (container instanceof IConfigurationContributor)
 								computedContributors.add(container);
 						}
 						
 						// Second add in contributors that exist for a container id.
 						for (Iterator iter = launchInfo.configInfo.containerIds.keySet().iterator(); iter.hasNext();) {
 							String containerid = (String) iter.next();
 							IConfigurationElement[] contributors = ProxyPlugin.getPlugin().getContainerConfigurations(containerid);
 							if (contributors != null)
 								for (int i = 0; i < contributors.length; i++) {
 									Object contributor = contributors[i].createExecutableExtension(ProxyPlugin.PI_CLASS);
 									if (contributor instanceof IConfigurationContributor)
 										computedContributors.add(contributor);
 								}
 						}
 						
 						// Finally add in contributors that exist for a plugin id.
 						for (Iterator iter = launchInfo.configInfo.pluginIds.keySet().iterator(); iter.hasNext();) {
 							String pluginId = (String) iter.next();
 							IConfigurationElement[] contributors = ProxyPlugin.getPlugin().getPluginConfigurations(pluginId);
 							if (contributors != null)
 								for (int i = 0; i < contributors.length; i++) {
 									Object contributor = contributors[i].createExecutableExtension(ProxyPlugin.PI_CLASS);
 									if (contributor instanceof IConfigurationContributor)
 										computedContributors.add(contributor);
 								}
 						}
 						
 						// Now turn into array
 						if (!computedContributors.isEmpty()) {
 							IConfigurationContributor[] newContribs = new IConfigurationContributor[aContribs.length+computedContributors.size()];
 							System.arraycopy(aContribs, 0, newContribs, 0, aContribs.length);
 							IConfigurationContributor[] cContribs = (IConfigurationContributor[]) computedContributors.toArray(new IConfigurationContributor[computedContributors.size()]);
 							System.arraycopy(cContribs, 0, newContribs, aContribs.length, cContribs.length);
 							aContribs = newContribs;
 						}
 					}
 				}
 			}
 		}
 		
 		launchInfo.contributors = aContribs;
 		
 		try {		
 			configwc.setAttribute(IProxyConstants.ATTRIBUTE_LAUNCH_KEY, launchKey);
 			if (vmTitle != null && vmTitle.length()>0)
 				configwc.setAttribute(IProxyConstants.ATTRIBUTE_VM_TITLE, vmTitle);
 			
 			if (ATTR_PRIVATE != null)
 				configwc.setAttribute(ATTR_PRIVATE, true);			
 			
 			// Let contributors modify the configuration.
 			final IConfigurationContributor[] contribs = aContribs;
 			final LaunchInfo linfo = launchInfo;
 			for (int i = 0; i < contribs.length; i++) {
 				// First run the initialize.
 				// Run in safe mode so that anything happens we don't go away.
 				final int ii = i;
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// Don't need to do anything. Platform.run logs it for me.
 					}
 
 					public void run() throws Exception {
 						contribs[ii].initialize(linfo.getConfigInfo());
 					}
 				});
 
 				// Now run the contribute to configuration.
 				// Run in safe mode so that anything happens we don't go away.
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// Don't need to do anything. Platform.run logs it for me.
 					}
 
 					public void run() throws Exception {
 						contribs[ii].contributeToConfiguration(configwc);
 					}
 				});
 			}
 			pm.worked(100);
 			
 			configwc.launch(ILaunchManager.RUN_MODE, new SubProgressMonitor(pm, 100));
 			
 			final ProxyFactoryRegistry reg = launchInfo.resultRegistry;
 			for (int i = 0; i < contribs.length; i++) {
 				final int ii = i;
 				// Run in safe mode so that anything happens we don't go away.
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// Don't need to do anything. Platform.run logs it for me.
 					}
 
 					public void run() throws Exception {
 						contribs[ii].contributeToRegistry(reg);
 					}
 				});
 			}
 		} finally {
 			// Clean up and return.
 			LAUNCH_INFO.remove(launchKey);
 		}	
 		
 		pm.done();
 		return launchInfo.resultRegistry;
 	}
 	
 	private static void handleBuild(IProgressMonitor pm) throws CoreException {
 		boolean autobuilding = ResourcesPlugin.getWorkspace().isAutoBuilding();
 		if (!autobuilding) {
 			// We are not autobuilding. So kick off a build right here and
 			// wait for it.
 			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, pm);			
 		} else {
 			Job[] build = Platform.getJobManager().find(ResourcesPlugin.FAMILY_AUTO_BUILD);
 			pm.beginTask("", 100);
 			if (build.length == 1) {
 				if (build[0].getState() == Job.RUNNING || build[0].getState() == Job.WAITING || build[0].getState() == Job.SLEEPING) {
 					pm.subTask(ProxyMessages.getString("ProxyWaitForBuild"));
 					try {						
 						build[0].join();						
 					} catch (InterruptedException e) {
 						throw new CoreException(
 								new Status(IStatus.ERROR, ProxyPlugin.getPlugin().getDescriptor().getUniqueIdentifier(), IStatus.ERROR, "", e)); //$NON-NLS-1$
 					}
 				}
 			} 
 			pm.done();
 		}
 	}
 	
 	
 	/*
 	 * This prevents the launch from being shown. However these constants are in UI component, and we don't
 	 * want to pre-req that. So we will get them reflectively instead.
 	 * public but only so that launch delegate can get to it.
 	 */
 	public static String ATTR_PRIVATE;
 	static {
 		ATTR_PRIVATE = null;
 		try {
 			// So that we can run headless (w/o ui), need to do class forName for debugui contants
 			Plugin debuguiPlugin = Platform.getPlugin("org.eclipse.debug.ui"); //$NON-NLS-1$
 			if (debuguiPlugin != null) {
 				Class debugUIConstants = debuguiPlugin.getDescriptor().getPluginClassLoader().loadClass("org.eclipse.debug.ui.IDebugUIConstants"); //$NON-NLS-1$
 				ATTR_PRIVATE = (String) debugUIConstants.getField("ATTR_PRIVATE").get(null); //$NON-NLS-1$
 			}
 		} catch (SecurityException e) {
 		} catch (ClassNotFoundException e) {
 		} catch (NoSuchFieldException e) {
 		} catch (IllegalArgumentException e) {
 		} catch (IllegalAccessException e) {
 		}
 	}	
 
 	/* (non-Javadoc)
 	 * Only referenced by launch delegates. public because they are in other packages,
 	 * or even external developers packages. Not meant to be generally available.
 	 * 
 	 * This is needed because we can't pass the generic info into a launch configuration
 	 * because a launch configuration can't take objects. Only can take strings and numbers.  
 	 */
 	public static synchronized LaunchInfo getInfo(String key) {
 		return (LaunchInfo) LAUNCH_INFO.get(key);
 	}
 	
 	/* (non-Javadoc)
 	 * Local contributor used to make sure that certain jars are in the path.
 	 * 
 	 * @since 1.0.0
 	 */
 	static class ProxyContributor extends ConfigurationContributorAdapter {
 		public void contributeClasspaths(IConfigurationContributionController controller) {
 			// Add the required jars to the end of the classpath.
 			controller.contributeClasspath(ProxyPlugin.getPlugin().getDescriptor(), "proxycommon.jar", IConfigurationContributionController.APPEND_USER_CLASSPATH, false);	//$NON-NLS-1$
 			controller.contributeClasspath(ProxyPlugin.getPlugin().getDescriptor(), "initparser.jar", IConfigurationContributionController.APPEND_USER_CLASSPATH, true);	//$NON-NLS-1$			
 		}
 	}
 	
 }
