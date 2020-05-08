 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.tomcat.core.internal;
 
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jst.server.core.FacetUtil;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.core.internal.J2EEUtil;
 import org.eclipse.osgi.util.NLS;
 
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.model.*;
 /**
  * Generic Tomcat server.
  */
 public class TomcatServer extends ServerDelegate implements ITomcatServer, ITomcatServerWorkingCopy {
 	public static final String PROPERTY_SECURE = "secure";
 	public static final String PROPERTY_DEBUG = "debug";
 
 
 	protected transient TomcatConfiguration configuration;
 	protected transient ITomcatVersionHandler versionHandler;
 
 	/**
 	 * TomcatServer.
 	 */
 	public TomcatServer() {
 		super();
 	}
 
 	/**
 	 * Get the Tomcat runtime for this server.
 	 * 
 	 * @return Tomcat runtime for this server
 	 */
 	public TomcatRuntime getTomcatRuntime() {
 		if (getServer().getRuntime() == null)
 			return null;
 		
 		return (TomcatRuntime) getServer().getRuntime().loadAdapter(TomcatRuntime.class, null);
 	}
 
 	/**
 	 * Gets the Tomcat version handler for this server.
 	 * 
 	 * @return version handler for this server
 	 */
 	public ITomcatVersionHandler getTomcatVersionHandler() {
 		if (versionHandler == null) {
 			if (getServer().getRuntime() == null || getTomcatRuntime() == null)
 				return null;
 
 			versionHandler = getTomcatRuntime().getVersionHandler();
 		}
 		return versionHandler;
 	}
 
 	public ITomcatConfiguration getServerConfiguration() throws CoreException {
 		return getTomcatConfiguration();
 	}
 
 	public TomcatConfiguration getTomcatConfiguration() throws CoreException {
 		if (configuration == null) {
 			IFolder folder = getServer().getServerConfiguration();
 			if (folder == null || !folder.exists()) {
 				String path = null;
 				if (folder != null)
 					path = folder.getFullPath().toOSString();
 				throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorNoConfiguration, path), null));
 			}
 			
 			String id = getServer().getServerType().getId();
 			if (id.indexOf("32") > 0)
 				configuration = new Tomcat32Configuration(folder);
 			else if (id.indexOf("40") > 0)
 				configuration = new Tomcat40Configuration(folder);
 			else if (id.indexOf("41") > 0)
 				configuration = new Tomcat41Configuration(folder);
 			else if (id.indexOf("50") > 0)
 				configuration = new Tomcat50Configuration(folder);
 			else if (id.indexOf("55") > 0)
 				configuration = new Tomcat55Configuration(folder);
 			else if (id.indexOf("60") > 0)
 				configuration = new Tomcat60Configuration(folder);
 			try {
 				configuration.load(folder, null);
 			} catch (CoreException ce) {
 				// ignore
 				configuration = null;
 				throw ce;
 			}
 		}
 		return configuration;
 	}
 
 	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
 		if (runtime == null) {
 			configuration = null;
 			return;
 		}
 		IPath path = runtime.getLocation().append("conf");
 		
 		String id = getServer().getServerType().getId();
 		IFolder folder = getServer().getServerConfiguration();
 		if (id.indexOf("32") > 0)
 			configuration = new Tomcat32Configuration(folder);
 		else if (id.indexOf("40") > 0)
 			configuration = new Tomcat40Configuration(folder);
 		else if (id.indexOf("41") > 0)
 			configuration = new Tomcat41Configuration(folder);
 		else if (id.indexOf("50") > 0)
 			configuration = new Tomcat50Configuration(folder);
 		else if (id.indexOf("55") > 0)
 			configuration = new Tomcat55Configuration(folder);
 		else if (id.indexOf("60") > 0)
 			configuration = new Tomcat60Configuration(folder);
 		try {
 			configuration.importFromPath(path, isTestEnvironment(), monitor);
 		} catch (CoreException ce) {
 			// ignore
 			configuration = null;
 			throw ce;
 		}
 	}
 
 	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
 		if (configuration == null)
 			return;
 		configuration.save(getServer().getServerConfiguration(), monitor);
 	}
 
 	public void configurationChanged() {
 		configuration = null;
 	}
 
 	/**
 	 * Return the root URL of this module.
 	 * @param module org.eclipse.wst.server.core.model.IModule
 	 * @return java.net.URL
 	 */
 	public URL getModuleRootURL(IModule module) {
 		try {
 			if (module == null)
 				return null;
 			
 			TomcatConfiguration config = getTomcatConfiguration();
 			if (config == null)
 				return null;
 			
 			String url = "http://localhost";
 			int port = config.getMainPort().getPort();
 			port = ServerUtil.getMonitoredPort(getServer(), port, "web");
 			if (port != 80)
 				url += ":" + port;
 			
 			url += config.getWebModuleURL(module);
 			
 			if (!url.endsWith("/"))
 				url += "/";
 			
 			return new URL(url);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not get root URL", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns true if the process is set to run in debug mode.
 	 * This feature only works with Tomcat v4.0.
 	 *
 	 * @return boolean
 	 */
 	public boolean isDebug() {
 		return getAttribute(PROPERTY_DEBUG, false);
 	}
 
 	/**
 	 * Returns true if this is a test (run code out of the workbench) server.
 	 *
 	 * @return boolean
 	 */
 	public boolean isTestEnvironment() {
 		return getAttribute(PROPERTY_TEST_ENVIRONMENT, false);
 	}
 
 	/**
 	 * Returns true if the process is set to run in secure mode.
 	 *
 	 * @return boolean
 	 */
 	public boolean isSecure() {
 		return getAttribute(PROPERTY_SECURE, false);
 	}
 	
 	/**
 	 * @see ITomcatServer#getInstanceDirectory()
 	 */
 	public String getInstanceDirectory() {
 		return getAttribute(PROPERTY_INSTANCE_DIR, (String)null);
 	}
 	
 	/**
 	 * @see ITomcatServer#getDeployDirectory()
 	 */
 	public String getDeployDirectory() {
 		// Default to value used by prior WTP versions
 		return getAttribute(PROPERTY_DEPLOY_DIR, LEGACY_DEPLOYDIR);
 	}
 
 	/**
 	 * Returns true if modules should be served without publishing.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isServeModulesWithoutPublish() {
 		// If feature is supported, return current setting
		if (getTomcatVersionHandler().supportsServeModulesWithoutPublish())
 			return getAttribute(PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH, false);
 		return false;
 	}
 	
 	/**
 	 * Returns true if contexts should be saved in separate files
 	 * during server publish.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isSaveSeparateContextFiles() {
 		// If feature is supported, return current setting
		if (getTomcatVersionHandler().supportsSeparateContextFiles())
 			return getAttribute(PROPERTY_SAVE_SEPARATE_CONTEXT_FILES, false);
 		return false;
 	}
 
 	
 	/**
 	 * Gets the base directory where the server instance runs.  This
 	 * path can vary depending on the configuration. Null may be returned
 	 * if a runtime hasn't been specified for the server.
 	 * 
 	 * @return path to base directory for the server or null if
 	 * runtime hasn't been specified.
 	 */
 	public IPath getRuntimeBaseDirectory() {
 		ITomcatVersionHandler tvh = getTomcatVersionHandler();
 		if (tvh != null)
 			return tvh.getRuntimeBaseDirectory(this);
 		return null;
 	}
 
 	/**
 	 * Gets the directory to which modules should be deployed for
 	 * this server.
 	 * 
 	 * @return full path to deployment directory for the server
 	 */
 	public IPath getServerDeployDirectory() {
 		String deployDir = getDeployDirectory();
 		IPath deployPath = new Path(deployDir);
 		if (!deployPath.isAbsolute()) {
 			IPath base = getRuntimeBaseDirectory();
 			deployPath = base.append(deployPath);
 		}
 		return deployPath;
 	}
 	
 	protected static String renderCommandLine(String[] commandLine, String separator) {
 		if (commandLine == null || commandLine.length < 1)
 			return "";
 		StringBuffer buf= new StringBuffer(commandLine[0]);
 		for (int i = 1; i < commandLine.length; i++) {
 			buf.append(separator);
 			buf.append(commandLine[i]);
 		}	
 		return buf.toString();
 	}
 
 	/**
 	 * Returns the child module(s) of this module.
 	 * @param module module from which to get child module(s)
 	 * @return array of child module(s)
 	 */
 	public IModule[] getChildModules(IModule[] module) {
 		if (module == null)
 			return null;
 		
 		IModuleType moduleType = module[0].getModuleType();
 		
 		if (module.length == 1 && moduleType != null && "jst.web".equals(moduleType.getId())) {
 			IWebModule webModule = (IWebModule) module[0].loadAdapter(IWebModule.class, null);
 			if (webModule != null) {
 				IModule[] modules = webModule.getModules();
 				//if (modules != null)
 				//	System.out.println(modules.length);
 				return modules;
 			}
 		}
 		return new IModule[0];
 	}
 
 	/**
 	 * Returns the root module(s) of this module.
 	 * @param module module from which to get the root module
 	 * @return root module
 	 * @throws CoreException 
 	 */
 	public IModule[] getRootModules(IModule module) throws CoreException {
 		if ("jst.web".equals(module.getModuleType().getId())) {
 			IStatus status = canModifyModules(new IModule[] { module }, null);
 			if (status == null || !status.isOK())
 				throw new CoreException(status);
 			return new IModule[] { module };
 		}
 		
 		return J2EEUtil.getWebModules(module, null);
 	}
 
 	/**
 	 * Returns true if the given project is supported by this
 	 * server, and false otherwise.
 	 *
 	 * @param add modules
 	 * @param remove modules
 	 * @return the status
 	 */
 	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
 		if (add != null) {
 			int size = add.length;
 			for (int i = 0; i < size; i++) {
 				IModule module = add[i];
 				if (!"jst.web".equals(module.getModuleType().getId()))
 					return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorWebModulesOnly, null);
 				
 				if (getTomcatVersionHandler() == null)
 					return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorNoRuntime, null);
 				
 				IStatus status = getTomcatVersionHandler().canAddModule(module);
 				if (status != null && !status.isOK())
 					return status;
 				
 				if (module.getProject() != null) {
 					status = FacetUtil.verifyFacets(module.getProject(), getServer());
 					if (status != null && !status.isOK())
 						return status;
 				}
 			}
 		}
 		
 		return Status.OK_STATUS;
 	}
 
 	public ServerPort[] getServerPorts() {
 		if (getServer().getServerConfiguration() == null)
 			return new ServerPort[0];
 		
 		try {
 			List list = getTomcatConfiguration().getServerPorts();
 			ServerPort[] sp = new ServerPort[list.size()];
 			list.toArray(sp);
 			return sp;
 		} catch (Exception e) {
 			return new ServerPort[0]; 
 		}
 	}
 
 	public void setDefaults(IProgressMonitor monitor) {
 		setTestEnvironment(true);
 		setAttribute("auto-publish-setting", 2);
 		setAttribute("auto-publish-time", 1);
 		setDeployDirectory(DEFAULT_DEPLOYDIR);
 	}
 
 	/**
 	 * Sets this process to debug mode. This feature only works
 	 * with Tomcat v4.0.
 	 *
 	 * @param b boolean
 	 */
 	public void setDebug(boolean b) {
 		setAttribute(PROPERTY_DEBUG, b);
 	}
 
 	/**
 	 * Sets this process to secure mode.
 	 * @param b boolean
 	 */
 	public void setSecure(boolean b) {
 		setAttribute(PROPERTY_SECURE, b);
 	}
 
 	/**
 	 * Sets this server to test environment mode.
 	 * 
 	 * @param b boolean
 	 */
 	public void setTestEnvironment(boolean b) {
 		setAttribute(PROPERTY_TEST_ENVIRONMENT, b);
 	}
 	
 	/**
 	 * @see ITomcatServerWorkingCopy#setInstanceDirectory(String)
 	 */
 	public void setInstanceDirectory(String instanceDir) {
 		setAttribute(PROPERTY_INSTANCE_DIR, instanceDir);
 	}
 	
 	/**
 	 * @see ITomcatServerWorkingCopy#setDeployDirectory(String)
 	 */
 	public void setDeployDirectory(String deployDir) {
 		// Remove attribute if setting to legacy value assumed in prior versions of WTP.
 		// Allowing values that differ only in case is asking for more trouble that it is worth.
 		if (LEGACY_DEPLOYDIR.equalsIgnoreCase(deployDir))
 			setAttribute(PROPERTY_DEPLOY_DIR, (String)null);
 		else
 			setAttribute(PROPERTY_DEPLOY_DIR, deployDir);
 	}
 	
 	/**
 	 * @see ITomcatServerWorkingCopy#setServeModulesWithoutPublish(boolean)
 	 */
 	public void setServeModulesWithoutPublish(boolean b) {
 		setAttribute(PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH, b);
 	}
 
 	/**
 	 * @see ITomcatServerWorkingCopy#setSaveSeparateContextFiles(boolean)
 	 */
 	public void setSaveSeparateContextFiles(boolean b) {
 		setAttribute(PROPERTY_SAVE_SEPARATE_CONTEXT_FILES, b);
 	}
 
 	/**
 	 * @see ServerDelegate#modifyModules(IModule[], IModule[], IProgressMonitor)
 	 */
 	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
 		IStatus status = canModifyModules(add, remove);
 		if (status == null || !status.isOK())
 			throw new CoreException(status);
 		
 		TomcatConfiguration config = getTomcatConfiguration();
 		
 		if (add != null) {
 			int size = add.length;
 			for (int i = 0; i < size; i++) {
 				IModule module3 = add[i];
 				IWebModule module = (IWebModule) module3.loadAdapter(IWebModule.class, monitor);
 				String contextRoot = module.getContextRoot();
 				if (contextRoot != null && !contextRoot.startsWith("/") && contextRoot.length() > 0)
 					contextRoot = "/" + contextRoot;
 				String docBase = config.getDocBasePrefix() + module3.getName();
 				WebModule module2 = new WebModule(contextRoot, docBase, module3.getId(), true);
 				config.addWebModule(-1, module2);
 			}
 		}
 		
 		if (remove != null) {
 			int size2 = remove.length;
 			for (int j = 0; j < size2; j++) {
 				IModule module3 = remove[j];
 				String memento = module3.getId();
 				List modules = getTomcatConfiguration().getWebModules();
 				int size = modules.size();
 				for (int i = 0; i < size; i++) {
 					WebModule module = (WebModule) modules.get(i);
 					if (memento.equals(module.getMemento()))
 						config.removeWebModule(i);
 				}
 			}
 		}
 		//config.save(config.getFolder(), monitor);
 	}
 
 	/**
 	 * Return a string representation of this object.
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return "TomcatServer";
 	}
 }
