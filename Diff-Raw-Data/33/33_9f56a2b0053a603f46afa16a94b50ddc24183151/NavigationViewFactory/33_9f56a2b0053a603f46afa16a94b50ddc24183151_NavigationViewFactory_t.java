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
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.lang.reflect.Constructor;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 
 import org.eclipse.riena.core.wire.InjectExtension;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ModuleController;
 import org.eclipse.riena.navigation.ui.controllers.ModuleGroupController;
 import org.eclipse.riena.navigation.ui.swt.views.desc.IModuleDescriptionExtension;
 import org.eclipse.riena.navigation.ui.swt.views.desc.IModuleGroupDescriptionExtension;
 
 /**
  * Factory to create (sub-)views of the navigation view.
  */
 public class NavigationViewFactory implements IViewFactory {
 
 	private IModuleDescriptionExtension moduleDescriptionExtension;
 	private IModuleGroupDescriptionExtension moduleGroupDescriptionExtension;
 
 	@InjectExtension(min = 0, max = 1)
 	public void update(final IModuleDescriptionExtension moduleView) {
 		this.moduleDescriptionExtension = moduleView;
 	}
 
 	@InjectExtension(min = 0, max = 1)
 	public void update(final IModuleGroupDescriptionExtension moduleGroup) {
 		this.moduleGroupDescriptionExtension = moduleGroup;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.IViewFactory#createModuleGroupView(org.eclipse.swt.widgets.Composite)
 	 */
 	public ModuleGroupView createModuleGroupView(final Composite parent) {
 		if (moduleGroupDescriptionExtension != null) {
 			final Class<ModuleGroupView> moduleGroupClazz = moduleGroupDescriptionExtension.getView();
 			if (moduleGroupClazz != null) {
 				try {
 					final Constructor<ModuleGroupView> constructor = moduleGroupClazz.getConstructor(Composite.class,
 							int.class);
 					return constructor.newInstance(parent, SWT.NONE);
 				} catch (final Exception e) {
 					throw new RuntimeException("Instantiating the configured ModuleGroupView " + moduleGroupClazz //$NON-NLS-1$
 							+ " failed.", e); //$NON-NLS-1$
 				}
 			}
 		}
 		return new ModuleGroupView(parent, SWT.None);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.IViewFactory#createModuleGroupController(org.eclipse.riena.navigation.IModuleGroupNode)
 	 */
 	public ModuleGroupController createModuleGroupController(final IModuleGroupNode moduleGroupNode) {
 		ModuleGroupController controller = null;
 		if (moduleGroupDescriptionExtension != null) {
 			final Class<ModuleGroupController> moduleGroupClazz = moduleGroupDescriptionExtension.getController();
 			if (moduleGroupClazz != null) {
 				try {
 					final Constructor<ModuleGroupController> constructor = moduleGroupClazz
 							.getConstructor(IModuleGroupNode.class);
 					controller = constructor.newInstance(moduleGroupNode);
 				} catch (final Exception e) {
 					throw new RuntimeException("Instantiating the configured ModuleGroupController " + moduleGroupClazz //$NON-NLS-1$
 							+ " failed.", e); //$NON-NLS-1$
 				}
 			}
 		}
 		if (controller == null) {
 			controller = new ModuleGroupController(moduleGroupNode);
 		}
 		controller.setNavigationNode(moduleGroupNode);
 		return controller;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.IViewFactory#createModuleView(org.eclipse.swt.widgets.Composite)
 	 */
 	public ModuleView createModuleView(final Composite parent) {
 		if (moduleDescriptionExtension != null) {
 			final Class<ModuleView> moduleViewClazz = moduleDescriptionExtension.getView();
 			try {
 				final Constructor<ModuleView> constructor = moduleViewClazz.getConstructor(Composite.class);
 				return constructor.newInstance(parent);
 			} catch (final Exception e) {
 				throw new RuntimeException("Instantiating the configured ModuleView " + moduleViewClazz //$NON-NLS-1$
 						+ " failed.", e); //$NON-NLS-1$
 			}
 		}
 		return new ModuleView(parent);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.IViewFactory#createModuleController(org.eclipse.riena.navigation.IModuleNode)
 	 */
 	public ModuleController createModuleController(final IModuleNode moduleNode) {
 		ModuleController controller = null;
		if (moduleNode.getNavigationNodeController() != null) {
			return (ModuleController) moduleNode.getNavigationNodeController();
		}
 		if (moduleDescriptionExtension != null) {
 			final Class<ModuleController> moduleViewClazz = moduleDescriptionExtension.getController();
 			if (moduleViewClazz != null) {
 				try {
 					final Constructor<ModuleController> constructor = moduleViewClazz.getConstructor(IModuleNode.class);
 					controller = constructor.newInstance(moduleNode);
 				} catch (final Exception e) {
 					throw new RuntimeException("Instantiating the configured ModuleController " + moduleViewClazz //$NON-NLS-1$
 							+ " failed.", e); //$NON-NLS-1$
 				}
 			}
 		}
 		if (controller == null) {
 			controller = new SWTModuleController(moduleNode);
 		}
 		controller.setNavigationNode(moduleNode);
 		return controller;
 	}
 
 }
