 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.service;
 
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

 import org.osgi.framework.ServiceReference;
 
import org.eclipse.riena.internal.core.ignore.IgnoreFindBugs;

 /**
  * The specialized ranking implementation.
  */
 public class RankingInjector extends ServiceInjector {
 
 	private ServiceReference trackedServiceRef = null;
 
 	/**
 	 * @param serviceId
 	 * @param target
 	 */
 	RankingInjector(final ServiceDescriptor serviceId, final Object target) {
 		super(serviceId, target);
 	}
 
 	/*
 	 * @see org.eclipse.riena.core.service.ServiceInjector#doStart()
 	 */
 	@Override
 	protected void doStart() {
 		final ServiceReference serviceRef = getCurrentHighest();
 		registerServiceListener();
 		doBind(serviceRef);
 	}
 
 	/*
 	 * @see org.eclipse.riena.core.service.ServiceInjector#doStop()
 	 */
 	@Override
 	protected void doStop() {
 		invokeUnbindMethod(trackedServiceRef);
 		trackedServiceRef = null;
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.riena.core.service.ServiceInjector#bind(org.osgi.framework
 	 * .ServiceReference)
 	 */
 	@Override
 	protected void doBind(final ServiceReference serviceRef) {
 		if (serviceRef == null) {
 			return;
 		}
		if (trackedServiceRef != null && serviceRef.compareTo(trackedServiceRef) > 0) {
 			return;
 		}
 
 		invokeUnbindMethod(trackedServiceRef);
 		invokeBindMethod(serviceRef);
 		trackedServiceRef = serviceRef;
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.riena.core.service.ServiceInjector#unbind(org.osgi.framework
 	 * .ServiceReference)
 	 */
 	@Override
 	protected void doUnbind(final ServiceReference serviceRef) {
 		if (serviceRef == null) {
 			return;
 		}
 		if (serviceRef.compareTo(trackedServiceRef) != 0) {
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
 
 	/**
 	 * @return
 	 */
 	private ServiceReference getCurrentHighest() {
 		final ServiceReference[] serviceRefs = getServiceReferences();
 		return highestServiceRef(serviceRefs);
 	}
 
 	/**
 	 * @param serviceRefs
 	 * @return
 	 */
 	private static ServiceReference highestServiceRef(final ServiceReference[] serviceRefs) {
		if (serviceRefs == null) {
 			return null;
 		}
 		if (serviceRefs.length == 1) {
 			return serviceRefs[0];
 		}
		Arrays.sort(serviceRefs, new ObjectRankingComparator());
		return serviceRefs[0];
	}

	@IgnoreFindBugs(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE", justification = "only used locally")
	private static final class ObjectRankingComparator implements Comparator<ServiceReference>, Serializable {
		private static final long serialVersionUID = 1L;

		/*
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(ServiceReference sr1, ServiceReference sr2) {
			return sr1.compareTo(sr2);
 		}
 	}
 
 }
