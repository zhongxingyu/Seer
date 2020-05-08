 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.core;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.etrice.core.ui.internal.RoomActivator;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 
 public class CoreTestsActivator extends Plugin implements BundleActivator {
 
 	private static CoreTestsActivator instance = null;
 	
 	@Inject
 	private Diagnostician diagnostician;
 	
 	public static CoreTestsActivator getInstance() {
 		return instance;
 	}
 	
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		
 		instance = this;
         
		Injector injector = RoomActivator.getInstance().getInjector("org.eclipse.etrice.core.Room");
         injector.injectMembers(this);
 	}
 
 	public Diagnostician getDiagnostician() {
 		return diagnostician;
 	}
 }
