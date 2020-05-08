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
 package org.eclipse.riena.navigation;
 
 import org.eclipse.riena.navigation.common.ITypecastingAdaptable;
 
 /**
  * Describes the controller of a model object. The controller manages the
  * showing and hiding of views for the model
  */
 public interface INavigationNodeController extends ITypecastingAdaptable {
 
 	/**
 	 * Check if the node can be activated within the navigation context
 	 * 
 	 * @param node
 	 *            the node to check
 	 * @param context
 	 *            current navigation context
 	 * @return true if the node can be disposed
 	 */
 	boolean allowsActivate(INavigationNode<?> node, INavigationContext context);
 
 	/**
 	 * Check if the node can be deactivated within the navigation context
 	 * 
 	 * @param node
 	 *            the node to check
 	 * @param context
 	 *            current navigation context
 	 * @return true if the node can be deactivated
 	 */
 	boolean allowsDeactivate(INavigationNode<?> node, INavigationContext context);
 
 	/**
 	 * Check if the node can be disposed within the navigation context
 	 * 
 	 * @param node
 	 *            the node to check
 	 * @param context
 	 *            current navigation context
 	 * @return true if the node can be disposed
 	 */
 	boolean allowsDispose(INavigationNode<?> node, INavigationContext context);
 
 	/**
 	 * This method is called if the {@link NavigationArgument} of the node
 	 * changes while navigating.
 	 * 
 	 * @param argument
 	 *            The current {@link NavigationArgument}
 	 */
 	void navigationArgumentChanged(NavigationArgument argument);
 
 }
