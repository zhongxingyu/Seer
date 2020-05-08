 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.actions;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.rcp.ui.EMFCompareRCPUIPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.StructureMergeViewerGrouper;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.impl.DefaultGroupProvider;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuCreator;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 /**
  * This menu will display actions that will allow the user to group differences together.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  * @since 3.0
  */
 public class GroupActionMenu extends Action implements IMenuCreator {
 	/** The viewer grouper that will be affected by this menu's actions. */
 	private final StructureMergeViewerGrouper structureMergeViewerGrouper;
 
 	/** Menu Manager that will contain our menu. */
 	private MenuManager menuManager;
 
 	/** The default group provider. */
 	private DefaultGroupProvider defaultGroupProvider;
 
 	/**
 	 * Constructs our grouping menu.
 	 * 
 	 * @param structureMergeViewerGrouper
 	 *            The viewer grouper that will be affected by this menu's actions.
 	 * @param menuManager
 	 *            The Menu Manager that will contain our menu.
 	 * @param defaultGroupProvider
 	 *            The default group provider.
 	 */
 	public GroupActionMenu(StructureMergeViewerGrouper structureMergeViewerGrouper, MenuManager menuManager,
 			DefaultGroupProvider defaultGroupProvider) {
 		super("", IAction.AS_DROP_DOWN_MENU); //$NON-NLS-1$
 		this.menuManager = menuManager;
 		this.structureMergeViewerGrouper = structureMergeViewerGrouper;
 		this.defaultGroupProvider = defaultGroupProvider;
 		setToolTipText("Groups");
 		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EMFCompareRCPUIPlugin.PLUGIN_ID,
 				"icons/full/toolb16/group.gif")); //$NON-NLS-1$
 		setMenuCreator(this);
 	}
 
 	/**
 	 * Create the grouping action in the given menu.
 	 * 
 	 * @param scope
 	 *            The scope on which the groups will be applied.
 	 * @param comparison
 	 *            The comparison which differences are to be split into groups.
 	 */
 	public void createActions(IComparisonScope scope, Comparison comparison) {
		menuManager.removeAll();
 		final IAction defaultAction = new GroupAction(defaultGroupProvider.getLabel(),
 				structureMergeViewerGrouper, defaultGroupProvider);
 		defaultAction.setChecked(true);
 		menuManager.add(defaultAction);
 		IDifferenceGroupProvider.Registry registry = EMFCompareRCPUIPlugin.getDefault()
 				.getDifferenceGroupProviderRegistry();
 		boolean alreadyChecked = false;
 		for (IDifferenceGroupProvider dgp : registry.getGroupProviders(scope, comparison)) {
 			GroupAction action = new GroupAction(dgp.getLabel(), structureMergeViewerGrouper, dgp);
 			menuManager.add(action);
 			if (dgp.defaultSelected() && !alreadyChecked) {
 				defaultAction.setChecked(false);
 				action.setChecked(true);
 				alreadyChecked = true;
 				action.run();
 			}
 
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see IMenuCreator#dispose()
 	 */
 	public void dispose() {
 		menuManager.dispose();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see IMenuCreator#getMenu(Control)
 	 */
 	public Menu getMenu(Control parent) {
 		return menuManager.createContextMenu(parent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see IMenuCreator#getMenu(Menu)
 	 */
 	public Menu getMenu(Menu parent) {
 		return menuManager.getMenu();
 	}
 }
