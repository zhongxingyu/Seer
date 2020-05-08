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
 package org.eclipse.riena.example.client.navigation.model;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.riena.example.client.controllers.ComboSubModuleController;
 import org.eclipse.riena.example.client.controllers.ListSubModuleController;
 import org.eclipse.riena.example.client.views.ComboSubModuleView;
 import org.eclipse.riena.example.client.views.ListSubModuleView;
 import org.eclipse.riena.navigation.AbstractNavigationAssembler;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  *
  */
 public class ComboAndListNodeAssembler extends AbstractNavigationAssembler {
 
 	private Set<String> knownTargetIds = null;
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#buildNode(org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public IModuleGroupNode buildNode(NavigationNodeId presentationId, NavigationArgument navigationArgument) {
 
		IModuleGroupNode node = new ModuleGroupNode(presentationId);
 		IModuleNode module = new ModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.navigate.comboAndList"), "Combo&List"); //$NON-NLS-1$ //$NON-NLS-2$
 		node.addChild(module);
 
 		ISubModuleNode subModule = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.combo"), "Combo"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(subModule, ComboSubModuleController.class,
 				ComboSubModuleView.ID, false);
 		module.addChild(subModule);
 
		subModule = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.list"), "List"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(subModule, ListSubModuleController.class,
 				ListSubModuleView.ID, false);
 
 		SubModuleNode subModule2 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.list2"), "List2"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(subModule2, ListSubModuleController.class,
 				ListSubModuleView.ID, false);
 		subModule.addChild(subModule2);
 
 		SubModuleNode subModule3 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.list3"), "List3"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(subModule3, ListSubModuleController.class,
 				ListSubModuleView.ID, false);
 		subModule.addChild(subModule3);
 
 		SubModuleNode subModule4 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.list4"), "List4"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(subModule4, ListSubModuleController.class,
 				ListSubModuleView.ID, false);
 		subModule.addChild(subModule4);
 
 		module.addChild(subModule);
 		return node;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#acceptsTargetId(String)
 	 */
 	public boolean acceptsToBuildNode(NavigationNodeId nodeId, NavigationArgument argument) {
 
 		if (knownTargetIds == null) {
 			knownTargetIds = new HashSet<String>(Arrays.asList("org.eclipse.riena.example.navigate.comboAndList", //$NON-NLS-1$
 					"org.eclipse.riena.example.combo", //$NON-NLS-1$
					"org.eclipse.riena.example.list" //$NON-NLS-1$
 			));
 			knownTargetIds = Collections.unmodifiableSet(knownTargetIds);
 		}
 
 		return knownTargetIds.contains(nodeId.getTypeId());
 	}
 }
