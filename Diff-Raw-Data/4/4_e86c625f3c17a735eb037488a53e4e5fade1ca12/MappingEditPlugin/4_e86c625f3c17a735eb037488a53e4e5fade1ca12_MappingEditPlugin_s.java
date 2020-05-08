 /*******************************************************************************
  * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.mapping.provider;
 
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.eef.mapping.MappingPackage;
 import org.eclipse.emf.eef.runtime.impl.validation.EEFValidator;
 
 /**
  * This is the central singleton for the Mapping edit plugin.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public final class MappingEditPlugin extends EMFPlugin {
 	/**
 	 * Keep track of the singleton.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final MappingEditPlugin INSTANCE = new MappingEditPlugin();
 
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
 	public MappingEditPlugin() {
		super(new ResourceLocator[] {MappingEditPlugin.INSTANCE,});
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
 			EValidator.Registry.INSTANCE.put(MappingPackage.eINSTANCE, new EEFValidator());
 		}
 	}
 
 }
