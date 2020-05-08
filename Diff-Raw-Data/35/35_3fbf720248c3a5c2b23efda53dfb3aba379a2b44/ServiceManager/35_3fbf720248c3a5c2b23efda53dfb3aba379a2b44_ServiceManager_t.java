 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.common.services;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.PlatformUI;
 /**
  * This class gets a service even if we are running in headless mode.
  * 
  * This is useful for unit tests and workflows to get services which would otherwise
  * only be available when a workbench is running
  * 
  * @author fcp94556
  *
  */
 public class ServiceManager {
 	
 	private static final Collection<Class<? extends Object>> OFFLINE_SERVICES; 
 	static {
 		OFFLINE_SERVICES = new HashSet<Class<? extends Object>>(4);
 		OFFLINE_SERVICES.add(IHardwareService.class);
 		OFFLINE_SERVICES.add(ILoaderService.class);
 		OFFLINE_SERVICES.add(IThumbnailService.class);
 		OFFLINE_SERVICES.add(IUserInputService.class);
 		OFFLINE_SERVICES.add(IImageService.class);
 		OFFLINE_SERVICES.add(ISystemService.class);
 		OFFLINE_SERVICES.add(ITransferService.class);
		OFFLINE_SERVICES.add(IClassLoaderService.class);
	}
	
	public static Object getService(final Class serviceClass) throws Exception {
		return getService(serviceClass, true);
	}
 
 	/**
 	 * Gets a service given a class, using the workbench if we are not
 	 * in headless mode, otherwise returns our services providing the
 	 * class declared for the extension point, also implements the service.
 	 * 
 	 * @param serviceClass
 	 * @return
 	 */
	public static Object getService(final Class serviceClass, boolean exceptionOnError) throws Exception {
 		
 		if (PlatformUI.isWorkbenchRunning()) {
 			return PlatformUI.getWorkbench().getService(serviceClass);
 		}
		
		try {
			// Designed to get dawb factories which implement the serviceClass, might not
			// get other services properly. To help this we throw an exception if it is
			// one we don't know about.
			if (!OFFLINE_SERVICES.contains(serviceClass)) throw new Exception("Cannot get an implementor for "+serviceClass+" in headless mode!");
			final IConfigurationElement[] ele = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.services");
			for (IConfigurationElement i : ele) {
				final Object factory = i.createExecutableExtension("factoryClass");
				if (factory!=null && factory.getClass()==serviceClass) return factory;
			}
		} catch (Exception ne) {
			if (exceptionOnError) throw ne;
			ne.printStackTrace();// Only in test decks does this happen
			return null;
 		}
 		
 		return null;
 	}
 }
