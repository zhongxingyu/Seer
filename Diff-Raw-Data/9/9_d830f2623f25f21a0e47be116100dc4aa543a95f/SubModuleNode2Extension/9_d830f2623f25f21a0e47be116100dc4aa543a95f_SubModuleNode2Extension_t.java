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
 package org.eclipse.riena.navigation.extension;
 
 import org.eclipse.riena.navigation.ISubModuleNodeExtension;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 
 /**
  * Implementation of the interface {@code ISubModuleNode2Extension}. This is
  * only used for conversion of the legacy extension
  * {@link ISubModuleNodeExtension}.
  * 
  * @since 2.0
  */
 public class SubModuleNode2Extension extends Node2Extension implements ISubModuleNode2Extension {
 
 	private String viewId;
 	private Class<? extends IController> controller;
 	private boolean sharedView;
 	private boolean selectable;
 	private boolean requiresPreparation;
 	private boolean visible;
 	private boolean expanded;
 
 	/**
 	 * Creates a new instance of {@code SubModuleNode2Extension} and sets the
 	 * default value of the property {@code selectable}.
 	 */
 	public SubModuleNode2Extension() {
 		selectable = true;
 		visible = true;
 		expanded = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ISubModuleNode2Extension[] getChildNodes() {
 		return (ISubModuleNode2Extension[]) super.getChildNodes();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ISubModuleNode2Extension[] getSubModuleNodes() {
 		return getChildNodes();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getViewId() {
 		return viewId;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Class<? extends IController> getController() {
 		return controller;
 	}
 
 	/**
 	 * @since 2.1
 	 */
 	public IController createController() {
		try {
			return controller.newInstance();
		} catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isSharedView() {
 		return sharedView;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isSelectable() {
 		return selectable;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isRequiresPreparation() {
 		return requiresPreparation;
 	}
 
 	/**
 	 * Sets the ID of the view associated with the sub module. Must match a view
 	 * elements id attribute of an "org.eclipse.ui.view" extension.
 	 * 
 	 * @param viewId
 	 *            ID of the view
 	 */
 	public void setViewId(final String viewId) {
 		this.viewId = viewId;
 	}
 
 	/**
 	 * Sets the controller that controls the UI widgets in the view through
 	 * Ridgets.
 	 * 
 	 * @param controller
 	 *            controller of the sub module
 	 */
 	public void setController(final Class<? extends IController> controller) {
 		this.controller = controller;
 	}
 
 	/**
 	 * Sets whether the view is shared i.e. whether one instance of the view
 	 * should be used for all sub module instances.
 	 * 
 	 * @param sharedView
 	 *            {@code true} if the specified view should be a shared view,
 	 *            {@code false} otherwise
 	 */
 	public void setSharedView(final boolean sharedView) {
 		this.sharedView = sharedView;
 	}
 
 	/**
 	 * Sets whether or not this node can be selected.
 	 * 
 	 * @param selectable
 	 *            {@code true} if selectable, otherwise {@code false}
 	 */
 	public void setSelectable(final boolean selectable) {
 		this.selectable = selectable;
 	}
 
 	/**
 	 * Sets whether the navigation node should be prepared after the sub module
 	 * node is created. Preparation means that the controller is created (among
 	 * other things).
 	 * 
 	 * @param requiresPreparation
 	 *            {@code true} should be prepared; otherwise {@code false}
 	 */
 	public void setRequiresPreparation(final boolean requiresPreparation) {
 		this.requiresPreparation = requiresPreparation;
 	}
 
 	public boolean isExpanded() {
 		return expanded;
 	}
 
 	public boolean isVisible() {
 		return visible;
 	}
 
 	/**
 	 * Sets whether the SubModule should be expanded on system start.
 	 * 
 	 * @param expanded
 	 *            {@code true} should be expanded; otherwise {@code false}
 	 */
 	public void setExpanded(final boolean expanded) {
 		this.expanded = expanded;
 	}
 
 	/**
 	 * Sets whether the SubModule should be visible on system start.
 	 * 
 	 * @param expanded
 	 *            {@code true} should be visible; otherwise {@code false}
 	 */
 	public void setVisible(final boolean visible) {
 		this.visible = visible;
 	}
 
 }
