 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.communication.publisher.hessian;
 
 import java.util.Hashtable;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.communication.core.publisher.IServicePublisher;
import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.RienaActivator;
 import org.eclipse.riena.core.RienaConstants;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends RienaActivator {
 	private ServiceRegistration publisherReg;
 	private HessianRemoteServicePublisher publisher;
 
 	private Logger logger;
 
 	// The shared instance
 	private static Activator plugin;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		Activator.plugin = this;
 
		logger = Log4r.getLogger(Activator.class);
 		logger.log(LogService.LOG_INFO, "start hessian support on server"); //$NON-NLS-1$
 
 		publisher = new HessianRemoteServicePublisher();
 		final Hashtable<String, Object> properties = RienaConstants.newDefaultServiceProperties();
 		properties.put(IServicePublisher.PROP_PROTOCOL, publisher.getProtocol());
 		publisherReg = context.registerService(IServicePublisher.class.getName(), publisher, properties);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		publisherReg.unregister();
 		publisherReg = null;
 		publisher = null;
 
 		logger.log(LogService.LOG_INFO, "stop hessian support on server"); //$NON-NLS-1$
 		Activator.plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Get the plugin instance.
 	 * 
 	 * @return
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * 
 	 * @return the publisher or null if the bundle is stopped
 	 */
 	public HessianRemoteServicePublisher getPublisher() {
 		return publisher;
 	}
 }
