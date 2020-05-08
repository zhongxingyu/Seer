 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.ISourceProviderListener;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.util.RienaConfiguration;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.UIProcessRidget;
 import org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade;
 import org.eclipse.riena.navigation.ApplicationModelFailure;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubApplicationNodeListener;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.ui.controllers.SubApplicationController;
 import org.eclipse.riena.navigation.ui.swt.binding.DelegatingRidgetMapper;
 import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
 import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
 import org.eclipse.riena.ui.ridgets.SubModuleUtils;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.swt.uiprocess.UIProcessControl;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  * View of a sub-application.
  * 
  * @since 3.0
  */
 public class SubApplicationView implements INavigationNodeView<SubApplicationNode>, IPerspectiveFactory {
 
 	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), SubApplicationView.class);
 
 	private final LinkedList<SwtViewId> subModuleViewStock;
 	private final AbstractViewBindingDelegate binding;
 	private final RienaMenuHelper menuBindHelper;
 	private final ISourceProviderListener menuSourceProviderListener;
 	private SubApplicationController subApplicationController;
 	private SubApplicationListener subApplicationListener;
 	private SubApplicationNode subApplicationNode;
 
 	/**
 	 * Creates a new instance of {@code SubApplicationView}.
 	 */
 	public SubApplicationView() {
 		subModuleViewStock = new LinkedList<SwtViewId>();
 		binding = createBinding();
 		menuBindHelper = new RienaMenuHelper();
 		menuSourceProviderListener = new MenuSourceProviderListener();
 	}
 
 	public void addUpdateListener(final IComponentUpdateListener listener) {
 		throw new UnsupportedOperationException();
 
 	}
 
 	/**
 	 * Binds the navigation node to the view. Creates the widgets and the controller if necessary.<br>
 	 * Also the menus and the tool bar items are binded.
 	 * 
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void bind(final SubApplicationNode node) {
 		menuBindHelper.addSourceProviderListener(menuSourceProviderListener);
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			final IController controller = (IController) node.getNavigationNodeController();
 			binding.injectRidgets(controller);
 			binding.bind(controller);
 			bindMenuAndToolItems(controller);
 			controller.afterBind();
 		}
 		subApplicationListener = new SubApplicationListener();
 		getNavigationNode().addListener(subApplicationListener);
 	}
 
 	public void createInitialLayout(final IPageLayout layout) {
 		addUIControls();
 		subApplicationNode = (SubApplicationNode) locateSubApplication(layout);
 		subApplicationController = createController(subApplicationNode);
 		initializeListener(subApplicationController);
 		bind(subApplicationNode);
 		subApplicationController.afterBind();
 		doBaseLayout(layout);
 	}
 
 	public SubApplicationNode getNavigationNode() {
 		return subApplicationNode;
 	}
 
 	public void unbind() {
 		if (getNavigationNode() != null) {
 
 			getNavigationNode().removeListener(subApplicationListener);
 
 			if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 				final IController controller = (IController) getNavigationNode().getNavigationNodeController();
 				binding.unbind(controller);
 				menuBindHelper.unbind(controller);
 			}
 		}
 		menuBindHelper.removeSourceProviderListener(menuSourceProviderListener);
 	}
 
 	protected AbstractViewBindingDelegate createBinding() {
 		final DelegatingRidgetMapper ridgetMapper = new DelegatingRidgetMapper(SwtControlRidgetMapper.getInstance());
 		addMappings(ridgetMapper);
 		return new InjectSwtViewBindingDelegate(ridgetMapper);
 	}
 
 	/**
 	 * Creates controller of the sub-application view and create and set the some ridgets.
 	 * 
 	 * @param subApplication
 	 *            sub-application node
 	 * @return controller of the sub-application view
 	 */
 	protected SubApplicationController createController(final ISubApplicationNode subApplication) {
 		return new SubApplicationController(subApplication);
 	}
 
 	protected void doBaseLayout(final IPageLayout layout) {
 		layout.setEditorAreaVisible(false);
 		layout.setFixed(true);
 	}
 
 	// helping methods
 	//////////////////
 
 	private void addMappings(final DelegatingRidgetMapper ridgetMapper) {
 		ridgetMapper.addMapping(UIProcessControl.class, UIProcessRidget.class);
 	}
 
 	private void addUIControls() {
 		initUIProcessRidget();
 	}
 
 	/**
 	 * Creates Ridgets for the menu items and the cool bar items and binds the Ridgets of the items with the UI widgets.
 	 * 
 	 * @param controller
 	 */
 	private void bindMenuAndToolItems(final IController controller) {
 		final Shell shell = getShell();
 		menuBindHelper.bindMenuAndToolItems(controller, shell, shell);
 	}
 
 	/**
 	 * Returns the shell of the application.
 	 * 
 	 * @return application shell
 	 */
 	private Shell getShell() {
 		final SWTBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 		final Shell[] shells = Display.getDefault().getShells();
 		for (final Shell shell : shells) {
 			final String value = locator.locateBindingProperty(shell);
 			if ((value != null) && value.equals(ApplicationViewAdvisor.SHELL_RIDGET_PROPERTY)) {
 				return shell;
 			}
 		}
 		Shell shell = WorkbenchFacade.getInstance().getActiveWindowShell();
 		if (shell == null) {
 			shell = Display.getDefault().getActiveShell();
 		}
 		return shell;
 	}
 
 	/**
 	 * Adds a listener for all sub-module nodes of the sub-application.
 	 * 
 	 * @param controller
 	 *            controller of the sub-application
 	 */
 	private void initializeListener(final SubApplicationController controller) {
 		final NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new MySubModuleNodeListener());
 
 		navigationTreeObserver.addListenerTo(controller.getNavigationNode());
 	}
 
 	private void initUIProcessRidget() {
 		final UIProcessControl uiControl = new UIProcessControl(getShell());
 		uiControl.setPropertyName("uiProcessRidget"); //$NON-NLS-1$
 		binding.addUIControl(uiControl);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected ISubApplicationNode locateSubApplication(final IPageLayout layout) {
 		return SwtViewProvider.getInstance().getNavigationNode(layout.getDescriptor().getId(), ISubApplicationNode.class);
 	}
 
 	// helping classes
 	//////////////////
 
 	private class SubApplicationListener extends SubApplicationNodeListener {
 		@Override
 		public void block(final ISubApplicationNode source, final boolean block) {
 			super.block(source, block);
 			for (final IModuleGroupNode group : source.getChildren()) {
 				for (final IModuleNode module : group.getChildren()) {
 					module.setBlocked(block);
 				}
 			}
 		}
 
 		@Override
 		public void disposed(final ISubApplicationNode source) {
 			unbind();
 		}
 
 		@Override
 		public void nodeIdChange(final ISubApplicationNode source, final NavigationNodeId oldId, final NavigationNodeId newId) {
 			if (source.equals(getNavigationNode())) {
 				SwtViewProvider.getInstance().replaceNavigationNodeId(source, oldId, newId);
 			}
 		}
 	}
 
 	/*
 	 * Delegates to the SwtViewProvider to locate all View users for the given SetViewId
 	 */
 	/**
 	 * @since 3.0
 	 */
 	protected int getViewUserCount(final SwtViewId id) {
 		return SwtViewProvider.getInstance().getViewUsers(id).size();
 	}
 
 	private void updateMenuBar() {
 		final List<MenuCoolBarComposite> menuCoolBarComposites = menuBindHelper.getMenuCoolBarComposites(getShell());
 		for (final MenuCoolBarComposite menuBarComp : menuCoolBarComposites) {
 			menuBarComp.updateMenuItems();
 		}
 		final IController controller = (IController) getNavigationNode().getNavigationNodeController();
 		bindMenuAndToolItems(controller);
 	}
 
 	/**
 	 * After a sub-module node was activated, the corresponding view is shown.
 	 */
 	private class MySubModuleNodeListener extends SubModuleNodeListener {
 		private boolean navigationUp = false;
 
 		@Override
 		public void prepared(final ISubModuleNode source) {
 			if (SubModuleUtils.isPrepareView() /* && source.isSelectable() */) {
 				checkBaseStructure();
 
 				final SwtViewId id = getViewId(source);
 				if (id == null) {
 					return;
 				}
 				final SwtViewProvider viewProvider = SwtViewProvider.getInstance();
 				viewProvider.setCurrentPrepared(source);
 				prepareView(id, source);
 				viewProvider.setCurrentPrepared(null);
 			}
 		}
 
 		@Override
 		public void afterActivated(final ISubModuleNode source) {
 			updateMenuBar();
 		}
 
 		@Override
 		public void activated(final ISubModuleNode source) {
 			checkBaseStructure();
 			if (null != source && !source.isSelectable()) {
 				return;
 			}
 
 			final SwtViewId id = getViewId(source);
 			prepareView(id, null);
 			showView(id);
 
 		}
 
 		@Override
 		public void disposed(final ISubModuleNode source) {
 			try {
 				final SwtViewId id = getViewId(source);
 				/*
 				 * hideView internally disposes(RCP) a view if ref count is 0. For shared views this behavior is critical as RCP doesn�t know if a View is
 				 * reused the Riena way. We just hide it if Riena has no more references. For a list of references we use the SwtViewProvider#getViewUsers API.
 				 * We cannot set parts invisible as this collides with the marker concept.
 				 */
				if (/* Allways hide "unshared" views */id.getSecondary() == null || !id.getSecondary().startsWith(SubModuleView.SHARED_ID)
 						|| /* no more instances needed of the given shared view */getViewUserCount(id) < 1) {
 					hideView(id);
 				}
 				final SwtViewProvider viewProvider = SwtViewProvider.getInstance();
 				viewProvider.unregisterSwtViewId(source);
 			} catch (final ApplicationModelFailure amf) {
 				// not selectable SubModules dont't have an associated view, so if hiding creates an exception we can ignore it TODO : never close a not selectable node
 				if (source.isSelectable()) {
 					LOGGER.log(LogService.LOG_ERROR, "Error disposing node " + source + ": " + amf.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 
 		protected String createNextId() {
 			return String.valueOf(System.currentTimeMillis());
 		}
 
 		/**
 		 * At the very first time (a sub-module was activated), the view parts of the sub-application switcher and the navigation tree are shown.
 		 */
 		private void checkBaseStructure() {
 			if (!navigationUp) {
 				createNavigation();
 				navigationUp = true;
 			}
 		}
 
 		private void createNavigation() {
 			final String secId = createNextId();
 			prepareView(NavigationViewPart.ID, secId, null);
 			showView(NavigationViewPart.ID, secId);
 		}
 
 	}
 
 	/**
 	 * Returns the view ID of the given sub-module node.
 	 * 
 	 * @param source
 	 *            sub-module node
 	 * @return view ID
 	 * @since 3.0
 	 */
 	protected SwtViewId getViewId(final ISubModuleNode node) {
 		return SwtViewProvider.getInstance().getSwtViewId(node);
 	}
 
 	/**
 	 * Returns the currently active page.
 	 * 
 	 * @return active page
 	 */
 	private IWorkbenchPage getActivePage() {
 		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 	}
 
 	private IPerspectiveDescriptor getActivePerspective() {
 		final IWorkbenchPage page = getActivePage();
 		if (page != null) {
 			// get the active perspective
 			return page.getPerspective();
 		}
 		return null;
 	}
 
 	/**
 	 * Hides the view in the active page.
 	 * 
 	 * @param id
 	 *            the id of the view extension to use
 	 * @param secondaryId
 	 *            the secondary id to use
 	 * @return {@code true} if the view was hidden successfully.
 	 * 
 	 * @since 3.0
 	 */
 	private boolean hideView(final String id, final String secondary) {
 		boolean successful = false;
 		final IViewReference viewRef = getActivePage().findViewReference(id, secondary);
 		if (viewRef != null) {
 			final IViewPart view = viewRef.getView(false);
 			if (view instanceof INavigationNodeView<?>) {
 				((INavigationNodeView<?>) view).unbind();
 			}
 			getActivePage().hideView(view);
 			removeFromStock(new SwtViewId(id, secondary));
 			successful = true;
 		}
 		return successful;
 	}
 
 	/**
 	 * Hides the view in the active page.
 	 * 
 	 * @param id
 	 *            ID of the view
 	 * 
 	 * @return {@code true} if the view was hidden successfully.
 	 * 
 	 * @since 5.0
 	 * 
 	 */
 	protected boolean hideView(final SwtViewId id) {
 		return hideView(id.getId(), id.getSecondary());
 	}
 
 	private void showView(final SwtViewId id) {
 		showView(id.getId(), id.getSecondary());
 	}
 
 	/**
 	 * Shows a view in the active page.
 	 * 
 	 * @param id
 	 *            the id of the view extension to use
 	 * @param secondaryId
 	 *            the secondary id to use
 	 */
 	private void showView(final String id, final String secondary) {
 		final IWorkbenchPage page = getActivePage();
 		final IViewReference viewRef = page.findViewReference(id, secondary);
 		if (viewRef != null) {
 			WorkbenchFacade.getInstance().showView(page, viewRef);
 			if (id != NavigationViewPart.ID) {
 				final SwtViewId swtViewId = new SwtViewId(id, secondary);
 				addToStock(swtViewId);
 				hideUnusedView();
 			}
 		}
 	}
 
 	/**
 	 * Hides (disposes) the longest unused view, if the maximum number of stocked views is reached.
 	 */
 	private void hideUnusedView() {
 		final SwtViewId firstView = getFirstOfStock();
 		SwtViewId swtViewId = getLastOfStock();
 		while ((swtViewId != null) && (!firstView.equals(swtViewId))) {
 			if (!hideView(swtViewId)) {
 				// view wasn't hidden
 				addToStock(swtViewId);
 			}
 			swtViewId = getLastOfStock();
 		}
 	}
 
 	private IViewReference prepareView(final SwtViewId id, final ISubModuleNode currentPrepared) {
 		return prepareView(id.getId(), id.getSecondary(), currentPrepared);
 	}
 
 	/**
 	 * Returns the perspective of the given sub-application.
 	 * 
 	 * @param subApp
 	 *            node of the sub-application
 	 * @return perspective of sub-application or {@code null} if no perspective was found
 	 */
 	private IPerspectiveDescriptor getPerspectiveOfSubApplication(final ISubApplicationNode subApp) {
 
 		final Object subAppIdObject = WorkareaManager.getInstance().getDefinition(getNavigationNode()).getViewId();
 		final String subAppId = String.valueOf(subAppIdObject);
 		final IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
 		for (final IPerspectiveDescriptor perspective : perspectives) {
 			if (StringUtils.equals(subAppId, perspective.getId())) {
 				return perspective;
 			}
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Prepares a view so that is can be shown.
 	 * 
 	 * @param id
 	 *            the id of the view extension to use
 	 * @param secondary
 	 *            the secondary id to use
 	 * @param currentPrepared
 	 * @return the view reference, or <code>null</code> if none is found
 	 */
 	private IViewReference prepareView(final String id, final String secondary, final ISubModuleNode currentPrepared) {
 
 		try {
 			final IWorkbenchPage page = getActivePage();
 			IViewPart viewPart = null;
 			if (secondary != null && secondary.startsWith(SubModuleView.SHARED_ID)) {
 				viewPart = SwtViewProvider.getInstance().getRegisteredView(id, secondary);
 			}
 			//			if (SubModuleView.SHARED_ID.equals(secondary)) {
 			//				viewPart = SwtViewProvider.getInstance().getRegisteredView(id);
 			//			}
 			if (viewPart == null) {
 				final IPerspectiveDescriptor activePerspective = getActivePerspective();
 				final IPerspectiveDescriptor subAppPerspective = getPerspectiveOfSubApplication(getNavigationNode());
 				if (subAppPerspective != null) {
 					page.setPerspective(subAppPerspective);
 				} else {
 					LOGGER.log(LogService.LOG_WARNING, "No perspective found for sub application: " //$NON-NLS-1$
 							+ getNavigationNode());
 				}
 				// open view but don't activate it and don't bring it to top
 				viewPart = page.showView(id, secondary, IWorkbenchPage.VIEW_VISIBLE);
 				if (subAppPerspective != null) {
 					page.setPerspective(activePerspective);
 				}
 			}
 
 			/*
 			 * Shared views are only created once. All binding logic is done in createPartControl of the view. Prepared Nodes initially need a controller with
 			 * all ridgets injected. If showView did not instantiate a new instance we need to trigger node preparation for controller/ridget binding manually.
 			 */
 			if (currentPrepared != null && currentPrepared.getNavigationNodeController() == null) {
 				if (viewPart instanceof SubModuleView) {
 					((SubModuleView) viewPart).prepareNode(currentPrepared);
 				}
 			}
 			return page.findViewReference(id, secondary);
 		} catch (final PartInitException exc) {
 			final String msg = String.format("Failed to prepare/show view: %s, %s", id, secondary); //$NON-NLS-1$
 			LOGGER.log(0, msg, exc);
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * After changing a source the menu bar of this sub-application is updated.
 	 */
 	private class MenuSourceProviderListener implements ISourceProviderListener {
 
 		/**
 		 * Updates the menu bar (only if the priority is correct and this sub-application is selected).
 		 * 
 		 * @param sourcePriority
 		 *            A bit mask of all the source priorities that have changed.
 		 */
 		private void update(final int sourcePriority) {
 			if ((sourcePriority & ISources.ACTIVE_MENU) == ISources.ACTIVE_MENU) {
 				if (getNavigationNode().isSelected()) {
 					updateMenuBar();
 				}
 			}
 		}
 
 		public void sourceChanged(final int sourcePriority, final Map sourceValuesByName) {
 			update(sourcePriority);
 		}
 
 		public void sourceChanged(final int sourcePriority, final String sourceName, final Object sourceValue) {
 			update(sourcePriority);
 		}
 
 	}
 
 	/**
 	 * Add the given view to the to beginning to the stock.
 	 * 
 	 * @param viewId
 	 *            ID of the view
 	 */
 	private void addToStock(final SwtViewId viewId) {
 		removeFromStock(viewId);
 		subModuleViewStock.addFirst(viewId);
 	}
 
 	/**
 	 * Returns the first view of the stock.
 	 * 
 	 * @return ID of the first view
 	 * 
 	 * @throws NoSuchElementException
 	 *             if this list is empty
 	 */
 	private SwtViewId getFirstOfStock() {
 		return subModuleViewStock.getFirst();
 	}
 
 	/**
 	 * Returns the last view of the stock...
 	 * 
 	 * @return ID of the last view or ...
 	 */
 	private SwtViewId getLastOfStock() {
 		if (getMaxStockedViews() <= 0) {
 			return null;
 		}
 		if (getMaxStockedViews() >= subModuleViewStock.size()) {
 			return null;
 		}
 		return subModuleViewStock.getLast();
 	}
 
 	/**
 	 * Removes the given view form the stock.
 	 * 
 	 * @param viewId
 	 *            ID of the view
 	 * @return {@code true} if the stock contained the given view
 	 */
 	private boolean removeFromStock(final SwtViewId viewId) {
 		return subModuleViewStock.remove(viewId);
 	}
 
 	/**
 	 * Returns the maximal number of views that will be stocked.
 	 * 
 	 * @return maximal number of stocked views; 0 for indefinitely number of stocked views
 	 */
 	private int getMaxStockedViews() {
 		final String value = RienaConfiguration.getInstance().getProperty(RienaConfiguration.MAX_STOCKED_VIEWS_KEY);
 		try {
 			return Integer.parseInt(value);
 		} catch (final NumberFormatException e) {
 			return 0;
 		}
 	}
 
 }
