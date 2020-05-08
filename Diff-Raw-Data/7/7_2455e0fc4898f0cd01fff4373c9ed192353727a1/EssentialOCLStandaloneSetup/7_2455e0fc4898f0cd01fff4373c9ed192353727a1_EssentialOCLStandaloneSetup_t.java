 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: EssentialOCLStandaloneSetup.java,v 1.3 2011/03/01 08:46:48 ewillink Exp $
  */
 
 package org.eclipse.ocl.examples.xtext.essentialocl;
 
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.ocl.examples.pivot.uml.UMLUtils;
 import org.eclipse.ocl.examples.xtext.essentialocl.cs2pivot.EssentialOCLCS2Pivot;
 import org.eclipse.ocl.examples.xtext.essentialocl.utilities.EssentialOCLCS2MonikerVisitor;
 
 import com.google.inject.Injector;
 
 /**
  * Initialization support for running Xtext languages 
  * without equinox extension registry
  */
 public class EssentialOCLStandaloneSetup extends EssentialOCLStandaloneSetupGenerated
 {
 	private static Injector injector = null;
 	
 	public static void doSetup() {
 		if (injector == null) {
 			injector = new EssentialOCLStandaloneSetup().createInjectorAndDoEMFRegistration();
 		}
 	}
 
 	public static void init() {
 		EcorePackage.eINSTANCE.getClass();
 		EssentialOCLCS2MonikerVisitor.FACTORY.getClass();
 		EssentialOCLCS2Pivot.FACTORY.getClass();
 //		EssentialOCLPivot2CS.FACTORY.getClass();
 		UMLUtils.initializeContents();
 	}
 	
 	/**
 	 * Return the Injector for this plugin.
 	 */
 	public static final Injector getInjector() {
 		if (injector == null) {
 			doSetup();
 		}
 		return injector;
 	}
 
 	@Override
 	public Injector createInjector() {
 		if (Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("xmi"))
 			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().remove("xmi");
		if (!Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey(Resource.Factory.Registry.DEFAULT_EXTENSION))
 			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
 		return super.createInjector();
 	}
 
 	@Override
 	public Injector createInjectorAndDoEMFRegistration() {
 		init();
 		return super.createInjectorAndDoEMFRegistration();
 	}
 }
 
