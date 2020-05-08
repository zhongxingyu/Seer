 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.structure;
 
 import org.osgi.framework.BundleContext;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class Activator extends AbstractUIPlugin {
 
 	// The shared instance
 	private static Activator plugin;
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.etrice.ui.structure";
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
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
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static Image getImage(String path) {
		Image img = getDefault().getImageRegistry().get(path);
 		if (img==null) {
 			ImageDescriptor desc = imageDescriptorFromPlugin(PLUGIN_ID, path);
 			if (desc==null)
 				System.err.println("image not found: "+path);
 			else {
				getDefault().getImageRegistry().put(path, desc);
				getDefault().getImageRegistry().get(path);
 			}
 		}
 		return img;
 	}
 
 }
