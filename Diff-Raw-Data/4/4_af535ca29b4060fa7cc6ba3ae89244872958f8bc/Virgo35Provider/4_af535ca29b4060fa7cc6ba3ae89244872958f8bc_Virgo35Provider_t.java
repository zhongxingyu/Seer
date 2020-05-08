 /*******************************************************************************
 * Copyright (c) 2012 - 2013 Eclipse Foundation
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core.runtimes;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
 import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
 import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
 import org.eclipse.virgo.ide.runtime.core.ServerUtils;
 import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocatorVirgo;
 import org.eclipse.wst.server.core.IRuntime;
 
 /**
  * {@link IServerRuntimeProvider} for Virgo Server 3.5.0 and above.
  * 
  * @author Borislav Kapukaranov
  * @author Miles Parker
  */
 public class Virgo35Provider extends VirgoRuntimeProvider {
 
 	// Assumes Stateless
 	public static final VirgoRuntimeProvider INSTANCE = new Virgo35Provider();
 
 	public static final String GEMINI_CONNECTOR_BUNDLE_NAME = "org.eclipse.virgo.ide.management.remote_3.5.0.201208291630.jar"; //$NON-NLS-1$
 
 	private Virgo35Provider() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getRuntimeClass() {
 		return "org.eclipse.equinox.launcher.Main";
 	}
 
 	@Override
 	public String getConfigurationDir() {
 		return "configuration";
 	}
 
 	/**
 	 * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getProfileDir()
 	 */
 	@Override
 	String getProfileDir() {
 		return getConfigurationDir();
 	}
 
 	/**
 	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeClasspath(org.eclipse.core.runtime.IPath)
 	 */
 	@Override
 	public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath) {
 		List<IRuntimeClasspathEntry> cp = super.getRuntimeClasspath(installPath);
 
 		IPath pluginsPath = installPath.append("plugins");
 		if (pluginsPath.toFile().exists()) {
 			File pluginsFolder = pluginsPath.toFile();
 			for (File library : pluginsFolder.listFiles(new FileFilter() {
 				public boolean accept(File pathname) {
 					return pathname.isFile() && pathname.toString().endsWith(".jar")
 							&& pathname.toString().contains("org.eclipse.osgi_")
 							&& pathname.toString().contains("org.eclipse.equinox.console.supportability_");
 				}
 			})) {
 				IPath path = pluginsPath.append(library.getName());
 				cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
 			}
 		}
 
 		return cp;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String[] getRuntimeProgramArguments(IServerBehaviour behaviour) {
 		List<String> list = new ArrayList<String>();
 		list.add("-noExit");
 		return list.toArray(new String[list.size()]);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String[] getRuntimeVMArguments(IServerBehaviour behaviour, IPath installPath, IPath configPath,
 			IPath deployPath) {
 		String[] commonArguments = super.getRuntimeVMArguments(behaviour, installPath, configPath, deployPath);
 		String serverHome = ServerUtils.getServer(behaviour).getRuntimeBaseDirectory().toOSString();
 		List<String> list = new ArrayList<String>();
 		list.addAll(Arrays.asList(commonArguments));
 
 		list.add("-Dorg.eclipse.virgo.kernel.config=" + serverHome + "/" + getConfigurationDir());
 		list.add("-Dosgi.java.profile=file:" + serverHome + "/" + getConfigurationDir() + "/java6-server.profile");
 		list.add("-Declipse.ignoreApp=true");
 		list.add("-Dosgi.install.area=" + serverHome);
 		list.add("-Dosgi.configuration.area=" + serverHome + "/work");
 		list.add("-Dssh.server.keystore=" + serverHome + "/" + getConfigurationDir() + "/hostkey.ser");
		list.add("-Djava.endorsed.dirs=\"" + serverHome + "/lib/endorsed\"");
 
 		String fwClassPath = createFWClassPath(serverHome);
 
 		list.add("-Dosgi.frameworkClassPath=" + fwClassPath);
 
 		return list.toArray(new String[list.size()]);
 	}
 
 	private String createFWClassPath(String serverHome) {
 		StringBuilder fwClassPath = new StringBuilder();
 		File libDir = new File(serverHome + "/lib");
 		if (libDir.exists()) {
 			for (File file : libDir.listFiles()) {
 				if (file.getName().endsWith(".jar")) {
 					fwClassPath.append("file:" + file.getAbsolutePath() + ",");
 				}
 			}
 		}
 		File plugins = new File(serverHome + "/plugins");
 		if (plugins.exists()) {
 			for (File file : plugins.listFiles()) {
 				if (file.getName().contains("org.eclipse.osgi_")) {
 					fwClassPath.append("file:" + file.getAbsolutePath() + ",");
 				}
 				if (file.getName().contains("org.eclipse.equinox.console.supportability_")) {
 					fwClassPath.append("file:" + file.getAbsolutePath() + ",");
 				}
 			}
 		}
 		fwClassPath.deleteCharAt(fwClassPath.length() - 1);
 		return fwClassPath.toString();
 	}
 
 	@Override
 	public String getSupportedVersions() {
 		return "3.5+";
 	}
 
 	/**
 	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#createDependencyLocator(org.eclipse.wst.server.core.IRuntime,
 	 *      java.lang.String, java.lang.String[], java.lang.String,
 	 *      org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion)
 	 */
 	public IDependencyLocator createDependencyLocator(IRuntime runtime, String serverHomePath,
 			String[] additionalSearchPaths, String indexDirectoryPath, JavaVersion javaVersion) throws IOException {
 		return new DependencyLocatorVirgo(serverHomePath, additionalSearchPaths, indexDirectoryPath, javaVersion);
 	}
 
 	/**
 	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getConnectorBundleUri()
 	 */
 	public URI getConnectorBundleUri() {
 		return ServerCorePlugin.getDefault().getBundleUri(GEMINI_CONNECTOR_BUNDLE_NAME);
 	}
 }
