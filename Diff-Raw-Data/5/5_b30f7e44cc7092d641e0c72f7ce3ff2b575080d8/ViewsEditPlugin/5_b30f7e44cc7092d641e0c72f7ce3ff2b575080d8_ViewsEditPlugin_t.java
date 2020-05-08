 /**
  *  Copyright (c) 2008 Obeo.
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *  
  *  Contributors:
  *      Obeo - initial API and implementation
  * 
  *
 * $Id: ViewsEditPlugin.java,v 1.3 2009/08/22 09:46:01 glefur Exp $
  */
 package org.eclipse.emf.eef.views.provider;
 
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
 import org.eclipse.emf.eef.runtime.impl.validation.EEFValidator;
 import org.eclipse.emf.eef.views.ViewsPackage;
 
 /**
  * This is the central singleton for the Views edit plugin.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public final class ViewsEditPlugin extends EMFPlugin {
 	/**
 	 * Keep track of the singleton.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final ViewsEditPlugin INSTANCE = new ViewsEditPlugin();
 
 	/**
 	 * Keep track of the singleton.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static Implementation plugin;
 
 	/**
 	 * Create the instance.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewsEditPlugin() {
		super(new ResourceLocator[] {});
 	}
 
 	/**
 	 * Returns the singleton instance of the Eclipse plugin.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the singleton instance.
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getPluginResourceLocator() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the singleton instance of the Eclipse plugin.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the singleton instance.
 	 * @generated
 	 */
 	public static Implementation getPlugin() {
 		return plugin;
 	}
 
 	/**
 	 * The actual implementation of the Eclipse <b>Plugin</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static class Implementation extends EclipsePlugin {
 		/**
 		 * Creates an instance.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated NOT
 		 */
 		public Implementation() {
 			super();
 
 			// Remember the static instance.
 			//
 			plugin = this;
 			EValidator.Registry.INSTANCE.put(ViewsPackage.eINSTANCE,
 					new EEFValidator());
 		}
 	}
 
 }
