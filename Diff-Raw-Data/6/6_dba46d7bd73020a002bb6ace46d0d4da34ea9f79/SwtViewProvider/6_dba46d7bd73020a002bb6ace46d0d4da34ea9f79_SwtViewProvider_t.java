 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.presentation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.core.singleton.SessionSingletonProvider;
 import org.eclipse.riena.core.singleton.SingletonProvider;
 import org.eclipse.riena.navigation.ApplicationModelFailure;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
 import org.eclipse.riena.ui.workarea.IWorkareaDefinition;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  * Manages the reference between the navigation nodes and the view id's
  */
 public class SwtViewProvider {
 
 	private final Map<INavigationNode<?>, SwtViewId> views;
 	private final Map<String, Integer> viewCounter;
 	private final HashMap<String, Boolean> viewShared;
 	private INavigationNode<?> currentPrepared = null;
 
 	private static final SingletonProvider<SwtViewProvider> SVP = new SessionSingletonProvider<SwtViewProvider>(
 			SwtViewProvider.class);
 
 	/**
 	 * Gets the singleton SwtViewProvider.
 	 * 
 	 * @return
 	 * @since 1.2
 	 */
 	public static SwtViewProvider getInstance() {
 		return SVP.getInstance();
 	}
 
 	/**
 	 * Create new instance and initialize
 	 */
 	protected SwtViewProvider() {
 		super();
 		viewCounter = new LinkedHashMap<String, Integer>();
 		views = new LinkedHashMap<INavigationNode<?>, SwtViewId>();
 		viewShared = new HashMap<String, Boolean>();
 	}
 
 	public SwtViewId getSwtViewId(final INavigationNode<?> node) {
 		SwtViewId swtViewId = views.get(node);
 		if (swtViewId == null) {
 			final ISubModuleNode submodule = node.getTypecastedAdapter(ISubModuleNode.class);
 			// submodules need special treatment as they may be shared 
 			if (submodule != null && submodule.getNodeId() != null) {
 				swtViewId = createAndRegisterSwtViewId(submodule);
 			} else {
 				swtViewId = createAndRegisterSwtViewId(node);
 			}
 		}
 
 		boolean found = false;
 		for (final Entry<INavigationNode<?>, SwtViewId> entry : views.entrySet()) {
 			if (entry.getKey() == node) {
 				found = true;
 			}
 		}
 
		Assert.isLegal(found, "Cannot register node " + node //$NON-NLS-1$  
				+ " because a different node with the same id is already registered."); //$NON-NLS-1$
 		return swtViewId;
 	}
 
 	public void unregisterSwtViewId(final INavigationNode<?> node) {
 		views.remove(node);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void replaceNavigationNodeId(final INavigationNode<?> node, final NavigationNodeId oldId,
 			final NavigationNodeId newId) {
 		node.setNodeId(oldId);
 		final SwtViewId swtViewId = views.remove(node);
 		if (swtViewId != null) {
 			node.setNodeId(newId);
 			views.put(node, swtViewId);
 		}
 	}
 
 	private SwtViewId createAndRegisterSwtViewId(final INavigationNode<?> node) {
 
 		final IWorkareaDefinition def = WorkareaManager.getInstance().getDefinition(node);
 		final String viewId = getViewId(node, def);
 
 		final SwtViewId swtViewId = new SwtViewId(viewId, getNextSecondaryId(viewId));
 		views.put(node, swtViewId);
 
 		return swtViewId;
 	}
 
 	private SwtViewId createAndRegisterSwtViewId(final ISubModuleNode submodule) {
 
 		final IWorkareaDefinition def = WorkareaManager.getInstance().getDefinition(submodule);
 		final String viewId = getViewId(submodule, def);
 
 		if (viewShared.get(viewId) != null) {
 			if (def.isViewShared() != viewShared.get(viewId)) {
 				throw new ApplicationModelFailure(
 						"Inconsistent view usage. The view with the id \"" //$NON-NLS-1$
 								+ viewId
 								+ "\" is already used with a different 'shared' state. A view must be defined in all workarea definitions as either shared or not shared."); //$NON-NLS-1$
 			}
 		} else {
 			viewShared.put(viewId, def.isViewShared());
 		}
 		SwtViewId swtViewId = null;
 		if (def.isViewShared()) {
 			if (views.get(submodule) == null) {
 				viewCounter.put(viewId, 0);
 			}
 			if (viewCounter.get(viewId) == 0) {
 				// first node with this view
 				swtViewId = new SwtViewId(viewId, "shared"); //$NON-NLS-1$
 				views.put(submodule, swtViewId);
 				viewCounter.put(viewId, 1);
 			} else {
 				// view has been referenced already
 				swtViewId = views.get(getNavigationNode(viewId, null, ISubModuleNode.class, true));
 				views.put(submodule, swtViewId);
 			}
 		} else {
 			swtViewId = new SwtViewId(viewId, getNextSecondaryId(viewId));
 			views.put(submodule, swtViewId);
 		}
 
 		return swtViewId;
 	}
 
 	private String getViewId(final INavigationNode<?> node, final IWorkareaDefinition def) {
 		if (def == null) {
 			throw new ApplicationModelFailure("no work area definition for node " + node.getNodeId()); //$NON-NLS-1$
 		}
 		final Object viewId = def.getViewId();
 		if (viewId == null) {
 			throw new ApplicationModelFailure("viewId is null for nodeId " + node.getNodeId()); //$NON-NLS-1$
 		}
 		if (!(viewId instanceof String)) {
 			throw new ApplicationModelFailure("viewId is not a String for nodeId " + node.getNodeId()); //$NON-NLS-1$
 		}
 		return (String) viewId;
 	}
 
 	private String getNextSecondaryId(final String pViewId) {
 		if (viewCounter.get(pViewId) == null) {
 			viewCounter.put(pViewId, 0);
 		}
 		viewCounter.put(pViewId, viewCounter.get(pViewId) + 1);
 		return String.valueOf(viewCounter.get(pViewId));
 	}
 
 	private boolean isViewShared(final String viewId) {
 		final Boolean result = this.viewShared.get(viewId);
 		return result == null ? false : result;
 	}
 
 	public <N extends INavigationNode<?>> N getNavigationNode(final String pId, final Class<N> pClass) {
 		return getNavigationNode(pId, null, pClass);
 	}
 
 	public <N extends INavigationNode<?>> N getNavigationNode(final String pId, final String secondary,
 			final Class<N> pClass) {
 		return getNavigationNode(pId, secondary, pClass, false);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public <N extends INavigationNode<?>> N getNavigationNode(final String pId, final String secondary,
 			final Class<N> pClass, final boolean ignoreSharedState) {
 		for (final Entry<INavigationNode<?>, SwtViewId> entry : views.entrySet()) {
 			if (entry.getValue() == null) {
 				continue;
 			}
 			if (entry.getValue().getId().equals(pId) && //
 					(secondary == null || secondary.equals(entry.getValue().getSecondary()))) {
 
 				if (ignoreSharedState || !isViewShared(pId) || entry.getKey().isActivated()) {
 
 					return entry.getKey().getTypecastedAdapter(pClass);
 				} else if (isViewShared(pId)) {
 					if (entry.getKey().getTypecastedAdapter(pClass).equals(currentPrepared)) {
 						return (N) currentPrepared;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Locates all {@link INavigationNode} instances in the application
 	 * navigation model which reference the given {@link SwtViewId}. Disposed
 	 * nodes are not considered.
 	 * 
 	 * @param id
 	 *            the {@link SwtViewId}
 	 * @return a list of {@link INavigationNode}s as "users" of the given
 	 *         {@link SwtViewId}
 	 * @since 3.0
 	 */
 	public List<INavigationNode<?>> getViewUsers(final SwtViewId id) {
 		final List<INavigationNode<?>> result = new ArrayList<INavigationNode<?>>();
 		final Set<INavigationNode<?>> nodes = views.keySet();
 		for (final INavigationNode<?> regNode : nodes) {
 			final SwtViewId swtViewId = views.get(regNode);
 			if (!regNode.isDisposed() && swtViewId != null && swtViewId.getCompoundId().equals(id.getCompoundId())) {
 				result.add(regNode);
 			}
 		}
 		return result;
 
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void setCurrentPrepared(final INavigationNode<?> node) {
 		this.currentPrepared = node;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public INavigationNode<?> getCurrentPrepared() {
 		return currentPrepared;
 	}
 
 	private final Map<String, SubModuleView> registerdViewInstances = new HashMap<String, SubModuleView>();
 
 	/**
 	 * Registers the given view as being created
 	 * 
 	 * @param id
 	 *            the id of the view
 	 * @param subModuleView
 	 *            the {@link SubModuleView} instance
 	 * @since 3.0
 	 */
 	public void registerView(final String id, final SubModuleView subModuleView) {
 		registerdViewInstances.put(id, subModuleView);
 	}
 
 	/**
 	 * Returns the {@link SubModuleView} associated with the given id
 	 * 
 	 * @since 3.0
 	 */
 	public SubModuleView getRegisteredView(final String id) {
 		return registerdViewInstances.get(id);
 	}
 
 	/**
 	 * Unregisters the {@link SubModuleView} associated with the given id
 	 * 
 	 * @since 3.0
 	 */
 	public void unregisterView(final String id) {
 		registerdViewInstances.remove(id);
 
 	}
 }
