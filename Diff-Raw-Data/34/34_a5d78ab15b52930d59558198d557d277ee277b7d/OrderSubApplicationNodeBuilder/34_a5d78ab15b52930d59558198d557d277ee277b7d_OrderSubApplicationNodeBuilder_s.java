 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.demo.client.customer.navigation.model;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.riena.demo.client.customer.application.ExampleIcons;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 public class OrderSubApplicationNodeBuilder extends NavigationNodeBuilder {
 
 	private Set<String> knownTargetIds = null;
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#buildNode(org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public INavigationNode<?> buildNode(NavigationNodeId navigationNodeId, NavigationArgument navigationArgument) {
 
 		ISubApplicationNode subApplication = new SubApplicationNode(navigationNodeId, "Order"); //$NON-NLS-1$
 		subApplication.setIcon(createIconPath(ExampleIcons.ICON_APPLICATION));
 		WorkareaManager.getInstance().registerDefinition(subApplication, "order"); //$NON-NLS-1$
		subApplication.setSelected(true);
 
 		// getNavigationNode().navigate(new
 		// NavigationNodeId("org.eclipse.riena.example.client.CustomerRecord"));
 
 		return subApplication;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#acceptsTargetId(String)
 	 */
 	public boolean acceptsToBuildNode(NavigationNodeId nodeId, NavigationArgument argument) {
 
 		if (knownTargetIds == null) {
 			knownTargetIds = new HashSet<String>(Arrays.asList("org.eclipse.riena.demo.client.customer.order" //$NON-NLS-1$
 					));
 			knownTargetIds = Collections.unmodifiableSet(knownTargetIds);
 		}
 
 		return knownTargetIds.contains(nodeId.getTypeId());
 	}
 }
