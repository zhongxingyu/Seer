 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.service;
 
 import org.osgi.framework.ServiceReference;
 
 /**
  * The specialized ranking implementation.
  */
 public class RankingInjector extends ServiceInjector {
 
 	private ServiceReference trackedServiceRef = null;
 
 	RankingInjector(final ServiceDescriptor serviceId, final Object target) {
 		super(serviceId, target);
 	}
 
 	@Override
 	protected void doStart() {
 		final ServiceReference serviceRef = getCurrentHighest();
 		doBind(serviceRef);
 	}
 
 	@Override
 	protected void doStop() {
 		invokeUnbindMethod(trackedServiceRef);
 		trackedServiceRef = null;
 	}
 
 	@Override
 	protected void doBind(final ServiceReference serviceRef) {
 		if (serviceRef == null) {
 			return;
 		}
		final ServiceReference tempRef = trackedServiceRef;
		if (tempRef != null && serviceRef.compareTo(tempRef) < 0) {
 			return;
 		}
 
 		invokeUnbindMethod(trackedServiceRef);
 		invokeBindMethod(serviceRef);
 		trackedServiceRef = serviceRef;
 	}
 
 	@Override
 	protected void doUnbind(final ServiceReference serviceRef) {
 		if (serviceRef == null) {
 			return;
 		}
		final ServiceReference tempRef = trackedServiceRef;
		if (tempRef != null && serviceRef.compareTo(tempRef) != 0) {
 			return;
 		}
 		invokeUnbindMethod(serviceRef);
 		final ServiceReference highest = getCurrentHighest();
 		if (highest == null) {
 			trackedServiceRef = null;
 			return;
 		}
 		invokeBindMethod(highest);
 		trackedServiceRef = highest;
 	}
 
 	private ServiceReference getCurrentHighest() {
 		final ServiceReference[] serviceRefs = getServiceReferences();
 		return highestServiceRef(serviceRefs);
 	}
 
 	private static ServiceReference highestServiceRef(final ServiceReference[] serviceRefs) {
 		if (serviceRefs == null || serviceRefs.length == 0) {
 			return null;
 		}
 		if (serviceRefs.length == 1) {
 			return serviceRefs[0];
 		}
 		ServiceReference highest = serviceRefs[0];
 		for (int i = 1; i < serviceRefs.length; i++) {
 			if (serviceRefs[i].compareTo(highest) > 0) {
 				highest = serviceRefs[i];
 			}
 		}
 		return highest;
 	}
 
 }
