 /*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering, Technische Universitaet Muenchen. All rights
 * reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.common;
 
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecp.common.util.AbstractECPPlugin;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.osgi.framework.BundleContext;
 
 /**
  * . The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractECPPlugin {
 
 	/**
 	 * . The plug-in ID
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.emf.ecp.common";
 
 	/**
 	 * . The shared instance
 	 */
 	private static Activator plugin;
 
 	/**
 	 * . The constructor
 	 */
 	public Activator() {
 	}
 
 	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext) {@inheritDoc}
 	 */
 	@Override
 	// BEGIN SUPRESS CATCH EXCEPTION
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		Platform.getLog(Platform.getBundle("org.eclipse.emf.ecp.model")).addLogListener(new ILogListener() {
 
 			public void logging(IStatus status, String plugin) {
 				if (status.getSeverity() == Status.ERROR) {
 					// TODO: ChainSaw logging
 					// UiUtil.showReportErrorDialog(status);
 				}
 
 			}
 
 		});
 
 		Platform.getLog(Platform.getBundle("org.eclipse.emf.emfstore.client")).addLogListener(new ILogListener() {
 
 			public void logging(IStatus status, String plugin) {
 				if (status.getSeverity() == Status.ERROR) {
 					// TODO: ChainSaw logging
 					// UiUtil.showReportErrorDialog(status);
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	// END SUPRESS CATCH EXCEPTION
 
 	/**
 	 * . Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
	 * Returns an image descriptor for the image file at the given. plug-in relative path
 	 * 
	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
 	/**
 	 * Logs exception.
 	 * 
	 * @param e the exception
 	 */
 	public void logException(Exception e) {
 		getDefault().getLog().log(
 			new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
 
 	}
 
 }
