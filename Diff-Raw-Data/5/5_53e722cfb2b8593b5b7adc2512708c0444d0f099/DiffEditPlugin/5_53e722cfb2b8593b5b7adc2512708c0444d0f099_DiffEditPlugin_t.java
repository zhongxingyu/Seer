 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.provider;
 
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.compare.match.metamodel.provider.MatchEditPlugin;
 import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
 
 /**
  * This is the central singleton for the Diff edit plugin.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * @generated
  */
 public final class DiffEditPlugin extends EMFPlugin {
 	/**
 	 * Keep track of the singleton.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final DiffEditPlugin INSTANCE = new DiffEditPlugin();
 
 	/**
 	 * Keep track of the singleton.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated NOT
 	 */
	/* package */static Implementation plugin;
 
 	/**
 	 * Create the instance.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DiffEditPlugin() {
 		super(new ResourceLocator[] {EcoreEditPlugin.INSTANCE, MatchEditPlugin.INSTANCE, });
 	}
 
 	/**
 	 * Returns the singleton instance of the Eclipse plugin.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @return the singleton instance.
 	 * @generated
 	 */
 	public static Implementation getPlugin() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the singleton instance of the Eclipse plugin.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @return the singleton instance.
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getPluginResourceLocator() {
 		return plugin;
 	}
 
 	/**
 	 * The actual implementation of the Eclipse <b>Plugin</b>.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static class Implementation extends EclipsePlugin {
 		/**
 		 * Creates an instance.
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * @generated
 		 */
 		public Implementation() {
 			super();
 
 			// Remember the static instance.
 			//
 			plugin = this;
 		}
 	}
 
 }
