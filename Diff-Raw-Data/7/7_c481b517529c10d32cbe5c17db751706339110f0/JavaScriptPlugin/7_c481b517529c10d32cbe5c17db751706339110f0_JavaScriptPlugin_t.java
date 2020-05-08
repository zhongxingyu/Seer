 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class JavaScriptPlugin extends Plugin {
 
 	public static final String PLUGIN_ID = "org.eclipse.dltk.javascript.core";
 
 	// The shared instance.
 	private static JavaScriptPlugin plugin;
 
 	/**
 	 * The constructor.
 	 */
 	public JavaScriptPlugin() {
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static JavaScriptPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * @since 2.0
 	 */
	public static void error(String message) {
		error(message, null);
	}

	/**
	 * @since 2.0
	 */
 	public static void error(Throwable e) {
 		error(e.getLocalizedMessage(), e);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static void error(String message, Throwable t) {
 		plugin.getLog().log(
 				new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, t));
 	}
 
 }
