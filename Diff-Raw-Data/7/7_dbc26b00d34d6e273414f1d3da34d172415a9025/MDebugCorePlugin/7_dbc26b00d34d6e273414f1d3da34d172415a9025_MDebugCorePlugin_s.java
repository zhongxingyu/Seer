 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Bjorn Freeman-Benson - initial API and implementation
  *******************************************************************************/
 package gov.va.mumps.debug.core;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * Plug-in class for debug examples
  */
 public class MDebugCorePlugin extends Plugin {
 	
 	private static MDebugCorePlugin fgDefault = null;
 	
 	public MDebugCorePlugin() {
 		super();
 		fgDefault = this;
 	}
 	
 	/**
 	 * Returns the singleton Debug Examples plug-in.
 	 *  
 	 * @return the singleton Debug Examples plug-in
 	 */
 	public static MDebugCorePlugin getDefault() {
 		return fgDefault;
 	}
 	
 	/**
 	 * Return a <code>java.io.File</code> object that corresponds to the specified
 	 * <code>IPath</code> in the plugin directory, or <code>null</code> if none.
 	 */
 //	public static File getFileInPlugin(IPath path) {
 //		try {
 //			URL installURL =
 //				new URL(getDefault().getDescriptor().getInstallURL(), path.toString());
 //			URL localURL = Platform.asLocalURL(installURL);
 //			return new File(localURL.getFile());
 //		} catch (IOException ioe) {
 //			return null;
 //		}
 //	}
 //	
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
		System.out.println("CORE STARTED!!!!!!!!!!!");
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
 }
