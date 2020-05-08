 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.core.activator;
 
 import org.eclipse.tcf.te.runtime.tracing.TraceHandler;
 import org.eclipse.tcf.te.tcf.core.internal.Startup;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class CoreBundleActivator implements BundleActivator {
 	// The bundle context
 	private static BundleContext context;
 	// The trace handler instance
 	private static volatile TraceHandler traceHandler;
 
 	/**
 	 * Returns the bundle context
 	 *
 	 * @return the bundle context
 	 */
 	public static BundleContext getContext() {
 		return context;
 	}
 
 	/**
 	 * Convenience method which returns the unique identifier of this plugin.
 	 */
 	public static String getUniqueIdentifier() {
 		if (getContext() != null && getContext().getBundle() != null) {
 			return getContext().getBundle().getSymbolicName();
 		}
 		return "org.eclipse.tcf.te.tcf.core"; //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the bundles trace handler.
 	 *
 	 * @return The bundles trace handler.
 	 */
 	public static TraceHandler getTraceHandler() {
 		if (traceHandler == null) {
 			traceHandler = new TraceHandler(getUniqueIdentifier());
 		}
 		return traceHandler;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext bundleContext) throws Exception {
 		CoreBundleActivator.context = bundleContext;

		// To workaround Bug 388125 (Oracle bug 7179799) with Java 7, force
		// "java.net.preferIPv4Stack" to be set to "true"
		if (System.getProperty("java.net.preferIPv4Stack") == null) { //$NON-NLS-1$
			System.setProperty("java.net.preferIPv4Stack", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext bundleContext) throws Exception {
 		CoreBundleActivator.context = null;
 		// Mark the core framework as not started anymore
 		Startup.setStarted(false);
 		traceHandler = null;
 	}
 
 }
