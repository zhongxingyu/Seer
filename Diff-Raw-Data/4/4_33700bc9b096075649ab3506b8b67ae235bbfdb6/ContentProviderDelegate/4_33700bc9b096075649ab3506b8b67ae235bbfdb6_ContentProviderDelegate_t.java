 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.navigator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.ITreePathContentProvider;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerRedirector;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.ui.internal.preferences.IPreferenceKeys;
 import org.eclipse.tcf.te.tcf.ui.navigator.nodes.PeerRedirectorGroupNode;
 import org.eclipse.tcf.te.ui.swt.DisplayUtil;
 import org.eclipse.tcf.te.ui.views.Managers;
 import org.eclipse.tcf.te.ui.views.extensions.CategoriesExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.tcf.te.ui.views.interfaces.IRoot;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.internal.navigator.NavigatorFilterService;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.navigator.ICommonContentExtensionSite;
 import org.eclipse.ui.navigator.ICommonContentProvider;
 import org.eclipse.ui.navigator.ICommonFilterDescriptor;
 import org.eclipse.ui.navigator.INavigatorContentService;
 import org.eclipse.ui.navigator.INavigatorFilterService;
 
 
 /**
  * Content provider delegate implementation.
  */
 @SuppressWarnings("restriction")
 public class ContentProviderDelegate implements ICommonContentProvider, ITreePathContentProvider {
 	private final static Object[] NO_ELEMENTS = new Object[0];
 
 	// The "Redirected Peers" filter id
 	private final static String REDIRECT_PEERS_FILTER_ID = "org.eclipse.tcf.te.tcf.ui.navigator.RedirectPeersFilter"; //$NON-NLS-1$
 	// The current user filter id
 	private final static String CURRENT_USER_FILTER_ID = "org.eclipse.tcf.te.tcf.ui.navigator.PeersByCurrentUserFilter"; //$NON-NLS-1$
 
 
 	// The locator model listener instance
 	/* default */ IModelListener modelListener = null;
 
 	// Internal map of RemotePeerDiscoverRootNodes per peer id
 	private final Map<String, PeerRedirectorGroupNode> roots = new HashMap<String, PeerRedirectorGroupNode>();
 
 	// Flag to remember if invisible nodes are to be included in the list of
 	// returned children.
 	private final boolean showInvisible;
 
 	INavigatorFilterService navFilterService = null;
 
 	/**
 	 * Constructor.
 	 */
 	public ContentProviderDelegate() {
 		this(false);
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param showInvisible If <code>true</code>, {@link #getChildren(Object)} will include invisible nodes too.
 	 */
 	public ContentProviderDelegate(boolean showInvisible) {
 		super();
 		this.showInvisible = showInvisible;
 	}
 
 	/**
 	 * Determines if the given peer model node is a proxy or a value-add.
 	 *
 	 * @param peerModel The peer model node. Must not be <code>null</code>.
 	 * @return <code>True</code> if the peer model node is a proxy or value-add, <code>false</code> otherwise.
 	 */
 	/* default */ final boolean isProxyOrValueAdd(IPeerModel peerModel) {
 		Assert.isNotNull(peerModel);
 
 		boolean isProxy = peerModel.getPeer().getAttributes().containsKey("Proxy"); //$NON-NLS-1$
 
 		String value = peerModel.getPeer().getAttributes().get("ValueAdd"); //$NON-NLS-1$
 		boolean isValueAdd = value != null && ("1".equals(value.trim()) || Boolean.parseBoolean(value.trim())); //$NON-NLS-1$
 
 		return isProxy || isValueAdd;
 	}
 
 	/**
 	 * Determines if the given peer model node is filtered from the view completely.
 	 *
 	 * @param peerModel The peer model node. Must not be <code>null</code>.
 	 * @return <code>True</code> if filtered, <code>false</code> otherwise.
 	 */
 	/* default */ final boolean isFiltered(IPeerModel peerModel) {
 		Assert.isNotNull(peerModel);
 
 		boolean filtered = false;
 
 		filtered |= isProxyOrValueAdd(peerModel) && UIPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceKeys.PREF_HIDE_PROXIES_AND_VALUEADDS);
 		if (!showInvisible) {
 			filtered |= !peerModel.isVisible();
 		}
 
 		return filtered;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Object[] getChildren(Object parentElement) {
 		Object[] children = NO_ELEMENTS;
 
 		// The category element if the parent element is a category node
 		final ICategory category = parentElement instanceof ICategory ? (ICategory)parentElement : null;
 		// The category id if the parent element is a category node
 		final String catID = category != null ? category.getId() : null;
 
 		// If the parent element is a category, than we assume
 		// the locator model as parent element.
 		if (parentElement instanceof ICategory) {
 			parentElement = Model.getModel();
 		}
 		// If it is the locator model, get the peers
 		if (parentElement instanceof ILocatorModel) {
 			final ILocatorModel model = (ILocatorModel)parentElement;
 			final IPeerModel[] peers = model.getPeers();
 			final List<IPeerModel> candidates = new ArrayList<IPeerModel>();
 
 					if (IUIConstants.ID_CAT_FAVORITES.equals(catID)) {
 						for (IPeerModel peer : peers) {
 							ICategorizable categorizable = (ICategorizable)peer.getAdapter(ICategorizable.class);
 							if (categorizable == null) {
 								categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(peer, ICategorizable.class);
 							}
 							Assert.isNotNull(categorizable);
 
 							boolean isFavorite = Managers.getCategoryManager().belongsTo(catID, categorizable.getId());
 							if (isFavorite && !candidates.contains(peer)) {
 								candidates.add(peer);
 							}
 						}
 					}
 					else if (IUIConstants.ID_CAT_MY_TARGETS.equals(catID)) {
 						for (IPeerModel peer : peers) {
 							// Check for filtered nodes (Value-add's and Proxies)
 							if (isFiltered(peer)) {
 								continue;
 							}
 
 							ICategorizable categorizable = (ICategorizable)peer.getAdapter(ICategorizable.class);
 							if (categorizable == null) {
 								categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(peer, ICategorizable.class);
 							}
 							Assert.isNotNull(categorizable);
 
 							boolean isStatic = peer.isStatic();
 
 							// Static peers, or if launched by current user -> add automatically to "My Targets"
 							boolean startedByCurrentUser = System.getProperty("user.name").equals(peer.getPeer().getUserName()); //$NON-NLS-1$
 							boolean isMyTargets = Managers.getCategoryManager().belongsTo(catID, categorizable.getId());
 							if (!isMyTargets && (isStatic || startedByCurrentUser)) {
 								// "Value-add's" are not saved to the category persistence automatically
 								if (isProxyOrValueAdd(peer)) {
 									Managers.getCategoryManager().addTransient(catID, categorizable.getId());
 								} else {
 									Managers.getCategoryManager().add(catID, categorizable.getId());
 								}
 								isMyTargets = true;
 							}
 
 							if (isMyTargets && !candidates.contains(peer)) {
 								candidates.add(peer);
 							}
 						}
 					}
 					else if (IUIConstants.ID_CAT_NEIGHBORHOOD.equals(catID)) {
 						for (IPeerModel peer : peers) {
 							// Check for filtered nodes (Value-add's and Proxies)
 							if (isFiltered(peer)) {
 								continue;
 							}
 
 							ICategorizable categorizable = (ICategorizable)peer.getAdapter(ICategorizable.class);
 							if (categorizable == null) {
 								categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(peer, ICategorizable.class);
 							}
 							Assert.isNotNull(categorizable);
 
 							boolean isStatic = peer.isStatic();
 
 							boolean startedByCurrentUser = System.getProperty("user.name").equals(peer.getPeer().getUserName()); //$NON-NLS-1$
							boolean isNeighborhood = Managers.getCategoryManager().belongsTo(catID, categorizable.getId());
							if (!isNeighborhood && !isStatic && !startedByCurrentUser) {
 								// "Neighborhood" is always transient
 								Managers.getCategoryManager().addTransient(catID, categorizable.getId());
 								isNeighborhood = true;
 							}
 
 							if (isNeighborhood && !candidates.contains(peer)) {
 								candidates.add(peer);
 							}
 						}
 					}
 					else if (catID != null) {
 						for (IPeerModel peer : peers) {
 							ICategorizable categorizable = (ICategorizable)peer.getAdapter(ICategorizable.class);
 							if (categorizable == null) {
 								categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(peer, ICategorizable.class);
 							}
 							Assert.isNotNull(categorizable);
 
 							boolean belongsTo = category.belongsTo(peer);
 							if (belongsTo) {
 								candidates.add(peer);
 							}
 						}
 					}
 					else {
 						for (IPeerModel peer : peers) {
 							// Check for filtered nodes (Value-add's and Proxies)
 							if (isFiltered(peer)) {
 								continue;
 							}
 							candidates.add(peer);
 						}
 					}
 
 			children = candidates.toArray(new IPeerModel[candidates.size()]);
 		}
 		// If it is a peer model itself, get the child peers
 		else if (parentElement instanceof IPeerModel) {
 			String parentPeerId = ((IPeerModel)parentElement).getPeerId();
 			List<IPeerModel> candidates = Model.getModel().getChildren(parentPeerId);
 			if (candidates != null && candidates.size() > 0) {
 				PeerRedirectorGroupNode rootNode = roots.get(parentPeerId);
 				if (rootNode == null) {
 					rootNode = new PeerRedirectorGroupNode(parentPeerId);
 					roots.put(parentPeerId, rootNode);
 				}
 				children = new Object[] { rootNode };
 			} else {
 				roots.remove(parentPeerId);
 			}
 		}
 		// If it is a remote peer discover root node, return the children
 		// for the associated peer id.
 		else if (parentElement instanceof PeerRedirectorGroupNode) {
 			List<IPeerModel> candidates = Model.getModel().getChildren(((PeerRedirectorGroupNode)parentElement).peerId);
 			if (candidates != null && candidates.size() > 0) {
 				children = candidates.toArray();
 			}
 		}
 
 		return children;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreePathContentProvider#getChildren(org.eclipse.jface.viewers.TreePath)
 	 */
 	@Override
 	public Object[] getChildren(TreePath parentPath) {
 		// getChildren is independent of the elements tree path
 		return parentPath != null ? getChildren(parentPath.getLastSegment()) : NO_ELEMENTS;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
 	 */
 	@Override
 	public Object getParent(final Object element) {
 		// If it is a peer model node, return the parent locator model
 		if (element instanceof IPeerModel) {
 			// If it is a peer redirector, return the parent remote peer discover root node
 			if (((IPeerModel)element).getPeer() instanceof IPeerRedirector) {
 				IPeer parentPeer =  ((IPeerRedirector)((IPeerModel)element).getPeer()).getParent();
 				String parentPeerId = parentPeer.getID();
 				if (!roots.containsKey(parentPeerId)) {
 					roots.put(parentPeer.getID(), new PeerRedirectorGroupNode(parentPeerId));
 				}
 				return roots.get(parentPeerId);
 			}
 
 			// Determine the parent category node
 			ICategory category = null;
 			String[] categoryIds = Managers.getCategoryManager().getCategoryIds(((IPeerModel)element).getPeerId());
 			// If we have more than one, take the first one as parent category.
 			// To get all parents, the getParents(Object) method must be called
 			if (categoryIds != null && categoryIds.length > 0) {
 				category = CategoriesExtensionPointManager.getInstance().getCategory(categoryIds[0], false);
 			}
 
 			return category != null ? category : ((IPeerModel)element).getModel();
 		} else if (element instanceof PeerRedirectorGroupNode) {
 			// Return the parent peer model node
 			final AtomicReference<IPeerModel> parent = new AtomicReference<IPeerModel>();
 			final Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					parent.set(Model.getModel().getService(ILocatorModelLookupService.class).lkupPeerModelById(((PeerRedirectorGroupNode)element).peerId));
 				}
 			};
 
 			Assert.isTrue(!Protocol.isDispatchThread());
 
 			// The caller thread is very likely the display thread. We have to us a little
 			// trick here to avoid blocking the display thread via a wait on a monitor as
 			// this can (and has) lead to dead-locks with the TCF event dispatch thread if
 			// something fatal (OutOfMemoryError in example) happens in-between.
 			ExecutorsUtil.executeWait(new Runnable() {
 				@Override
 				public void run() {
 					Protocol.invokeAndWait(runnable);
 				}
 			});
 
 			return parent.get();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreePathContentProvider#getParents(java.lang.Object)
 	 */
 	@Override
 	public TreePath[] getParents(Object element) {
 		// Not sure if we ever have to calculate the _full_ tree path. The parent NavigatorContentServiceContentProvider
 		// is consuming only the last segment.
 		List<TreePath> pathes = new ArrayList<TreePath>();
 
 		if (element instanceof IPeerModel) {
 			if (Managers.getCategoryManager().belongsTo(IUIConstants.ID_CAT_FAVORITES, ((IPeerModel)element).getPeerId())) {
 				// Get the "Favorites" category
 				ICategory favCategory = CategoriesExtensionPointManager.getInstance().getCategory(IUIConstants.ID_CAT_FAVORITES, false);
 				if (favCategory != null) {
 					pathes.add(new TreePath(new Object[] { favCategory }));
 				}
 			}
 
 			// Determine the default parent
 			Object parent = getParent(element);
 			if (parent != null) {
 				pathes.add(new TreePath(new Object[] { parent }));
 			}
 		}
 
 		return pathes.toArray(new TreePath[pathes.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
 	 */
 	@Override
 	public boolean hasChildren(Object element) {
 		Object[] children = getChildren(element);
 
 		if (children != null && children.length > 0 && navFilterService != null) {
 			for (ViewerFilter filter : navFilterService.getVisibleFilters(true)) {
 				children = filter.filter(null, element, children);
 			}
 		}
 
 		return children != null && children.length > 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreePathContentProvider#hasChildren(org.eclipse.jface.viewers.TreePath)
 	 */
 	@Override
 	public boolean hasChildren(TreePath path) {
 		// hasChildren is independent of the elements tree path
 		return path != null ? hasChildren(path.getLastSegment()) : false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 		roots.clear();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
 		final ILocatorModel model = Model.getModel();
 
 		// Create and attach the model listener if not yet done
 		if (modelListener == null && model != null && viewer instanceof CommonViewer) {
 			modelListener = new ModelListener(model, (CommonViewer)viewer);
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					model.addListener(modelListener);
 				}
 			});
 		}
 
 		if (model != null && newInput instanceof IRoot) {
 			// Refresh the model asynchronously
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					model.getService(ILocatorModelRefreshService.class).refresh(null);
 				}
 			});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
 	 */
 	@Override
 	public void init(ICommonContentExtensionSite config) {
 		Assert.isNotNull(config);
 
 		// Make sure that the hidden "Redirected Peers" filter is active
 		INavigatorContentService cs = config.getService();
 		navFilterService = cs != null ? cs.getFilterService() : null;
 		if (navFilterService instanceof NavigatorFilterService) {
 			final NavigatorFilterService filterService = (NavigatorFilterService)navFilterService;
 			boolean activeFiltersChanged = false;
 
 			// Reconstruct the list of active filters based on the visible filter descriptors
 			List<String> activeFilderIds = new ArrayList<String>();
 
 			ICommonFilterDescriptor[] descriptors = filterService.getVisibleFilterDescriptors();
 			for (ICommonFilterDescriptor descriptor : descriptors) {
 				if (descriptor.getId() != null && !"".equals(descriptor.getId()) && filterService.isActive(descriptor.getId())) { //$NON-NLS-1$
 					activeFilderIds.add(descriptor.getId());
 				}
 			}
 
 			if (!activeFilderIds.contains(REDIRECT_PEERS_FILTER_ID)) {
 				activeFilderIds.add(REDIRECT_PEERS_FILTER_ID);
 				activeFiltersChanged = true;
 			}
 
 			if (UIPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceKeys.PREF_ACTIVATE_CURRENT_USER_FILTER)
 					&& !navFilterService.isActive(CURRENT_USER_FILTER_ID)) {
 				IDialogSettings settings = UIPlugin.getDefault().getDialogSettings();
 				IDialogSettings section = settings.getSection(this.getClass().getSimpleName());
 				if (section == null) section = settings.addNewSection(this.getClass().getSimpleName());
 				if (!section.getBoolean(IPreferenceKeys.PREF_ACTIVATE_CURRENT_USER_FILTER + ".done")) { //$NON-NLS-1$
 					activeFilderIds.add(CURRENT_USER_FILTER_ID);
 					activeFiltersChanged = true;
 					section.put(IPreferenceKeys.PREF_ACTIVATE_CURRENT_USER_FILTER + ".done", true); //$NON-NLS-1$
 				}
 			}
 
 			if (activeFiltersChanged) {
 				final String[] finActiveFilterIds = activeFilderIds.toArray(new String[activeFilderIds.size()]);
 				// Do the update view asynchronous to avoid reentrant viewer calls
 				DisplayUtil.safeAsyncExec(new Runnable() {
 					@Override
 					public void run() {
 						filterService.activateFilterIdsAndUpdateViewer(finActiveFilterIds);
 					}
 				});
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void restoreState(IMemento aMemento) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void saveState(IMemento aMemento) {
 	}
 }
