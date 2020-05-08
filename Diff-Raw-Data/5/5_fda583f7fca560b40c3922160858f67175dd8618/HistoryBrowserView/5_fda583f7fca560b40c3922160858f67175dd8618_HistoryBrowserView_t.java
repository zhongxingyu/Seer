 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.views.historybrowserview;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.client.model.observers.DeleteProjectSpaceObserver;
 import org.eclipse.emf.emfstore.client.model.util.ProjectSpaceContainer;
 import org.eclipse.emf.emfstore.client.ui.Activator;
 import org.eclipse.emf.emfstore.client.ui.dialogs.EMFStoreMessageDialog;
 import org.eclipse.emf.emfstore.client.ui.views.changes.ChangePackageVisualizationHelper;
 import org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.provider.ESBrowserLabelProvider;
 import org.eclipse.emf.emfstore.client.ui.views.historybrowserview.graph.IPlotCommit;
 import org.eclipse.emf.emfstore.client.ui.views.historybrowserview.graph.IPlotCommitProvider;
 import org.eclipse.emf.emfstore.client.ui.views.historybrowserview.graph.PlotCommitProvider;
 import org.eclipse.emf.emfstore.client.ui.views.historybrowserview.graph.PlotLane;
 import org.eclipse.emf.emfstore.client.ui.views.historybrowserview.graph.SWTPlotRenderer;
 import org.eclipse.emf.emfstore.client.ui.views.scm.SCMContentProvider;
 import org.eclipse.emf.emfstore.client.ui.views.scm.SCMLabelProvider;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationId;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * This the History Browser view.
  * 
  * @author Hodaie
  * @author Wesendonk
  * @author Shterev
  * @author Aumann
  */
 public class HistoryBrowserView extends ViewPart implements ProjectSpaceContainer {
 
 	private static final int INFOS_ABOVE_BASE = 2;
 	private static final int INFOS_BELOW_BASE = 4;
 	private static final boolean DEFAULT_SHOW_ALL_BRANCHES = true;
 
 	private final class ExCoAction extends Action {
 		private final ImageDescriptor expandImg;
 		private final ImageDescriptor collapseImg;
 
 		private ExCoAction(String text, int style, ImageDescriptor expandImg, ImageDescriptor collapseImg) {
 			super(text, style);
 			this.expandImg = expandImg;
 			this.collapseImg = collapseImg;
 		}
 
 		@Override
 		public void run() {
 			if (!isChecked()) {
 				setImage(true);
 				viewer.collapseAll();
 			} else {
 				setImage(false);
 				viewer.expandToLevel(2);
 			}
 		}
 
 		public void setImage(boolean expand) {
 			setImageDescriptor(expand ? expandImg : collapseImg);
 		}
 	}
 
 	/**
 	 * Treeviewer that provides a model element selection for selected
 	 * operations and mode element ids.
 	 * 
 	 * @author koegel
 	 */
 	private final class TreeViewerWithModelElementSelectionProvider extends TreeViewer {
 		private TreeViewerWithModelElementSelectionProvider(Composite parent, int style) {
 			super(parent, style);
 		}
 
 		@Override
 		protected Widget internalExpand(Object elementOrPath, boolean expand) {
 			// TODO Auto-generated method stub
 			return super.internalExpand(elementOrPath, expand);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getSelection()
 		 */
 		@Override
 		public ISelection getSelection() {
 			Control control = getControl();
 
 			if (control == null || control.isDisposed()) {
 				return super.getSelection();
 			}
 
 			Widget[] items = getSelection(getControl());
 			if (items.length != 1) {
 				return super.getSelection();
 			}
 
 			Widget item = items[0];
 			Object data = item.getData();
 			if (data == null) {
 				return super.getSelection();
 			}
 
 			// TODO: remove assignment
 			Object element = data;
 			EObject selectedModelElement = null;
 
 			if (element instanceof CompositeOperation) {
 				selectedModelElement = handleCompositeOperation((CompositeOperation) element);
 			} else if (element instanceof AbstractOperation) {
 				selectedModelElement = handleAbstractOperation((AbstractOperation) element);
 			} else if (element instanceof ProjectSpace) {
 				selectedModelElement = ((ProjectSpace) element).getProject();
 			} else if (element instanceof ModelElementId
 				&& projectSpace.getProject().contains((ModelElementId) element)) {
 				selectedModelElement = projectSpace.getProject().getModelElement((ModelElementId) element);
 			} else if (projectSpace.getProject().containsInstance((EObject) element)) {
 				selectedModelElement = (EObject) element;
 			}
 
 			if (selectedModelElement != null) {
 				return new StructuredSelection(selectedModelElement);
 			}
 
 			return super.getSelection();
 		}
 
 		private EObject handleCompositeOperation(CompositeOperation op) {
 			AbstractOperation mainOperation = op.getMainOperation();
 			if (mainOperation != null) {
 				ModelElementId modelElementId = mainOperation.getModelElementId();
 				EObject modelElement = projectSpace.getProject().getModelElement(modelElementId);
 				return modelElement;
 			}
 
 			return null;
 		}
 
 		private EObject handleAbstractOperation(AbstractOperation op) {
 			ModelElementId modelElementId = op.getModelElementId();
 			EObject modelElement = projectSpace.getProject().getModelElement(modelElementId);
 			return modelElement;
 		}
 	}
 
 	private List<HistoryInfo> historyInfos;
 
 	private PaginationManager paginationManager;
 
 	private ProjectSpace projectSpace;
 
 	private int headVersion;
 
 	private EObject modelElement;
 
 	private final Font nFont;
 
 	private TreeViewer viewer;
 	private Map<Integer, ChangePackage> changePackageCache;
 
 	private ChangePackageVisualizationHelper changePackageVisualizationHelper;
 
 	private SCMContentProvider contentProvider;
 
 	private SCMLabelProvider labelProvider;
 
 	private Link noProjectHint;
 
 	private Composite parent;
 
 	private boolean isUnlinkedFromNavigator;
 
 	private TreeViewerColumn changesColumn;
 
 	private TreeViewerColumn commitInfoColumn;
 
 	private TreeViewerColumn messageColumn;
 
 	private LogMessageColumnLabelProvider logLabelProvider;
 
 	private ComposedAdapterFactory adapterFactory;
 
 	private AdapterFactoryLabelProvider adapterFactoryLabelProvider;
 
 	private TreeViewerColumn graphColumn;
 
 	private static final int BRANCH_COLUMN = 1;
 
 	private SWTPlotRenderer renderer;
 
 	private IPlotCommitProvider commitProvider;
 
 	private ExCoAction expandAndCollapse;
 
 	/**
 	 * Constructor.
 	 */
 	public HistoryBrowserView() {
 		historyInfos = new ArrayList<HistoryInfo>();
 		changePackageCache = new HashMap<Integer, ChangePackage>();
 		nFont = PlatformUI.getWorkbench().getDisplay().getSystemFont();
 		WorkspaceManager.getObserverBus().register(new DeleteProjectSpaceObserver() {
 
 			public void projectSpaceDeleted(ProjectSpace projectSpace) {
 				if (HistoryBrowserView.this.projectSpace == projectSpace) {
 					HistoryBrowserView.this.getViewSite().getPage().hideView(HistoryBrowserView.this);
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		GridLayoutFactory.fillDefaults().applyTo(parent);
 		this.parent = parent;
 
 		noProjectHint = new Link(parent, SWT.WRAP);
 		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(noProjectHint);
 
 		noProjectHint
 			.setText("Select a <a>project</a> or call 'Show history' from the context menu of an element in the navigator.");
 		noProjectHint.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				ElementListSelectionDialog elsd = new ElementListSelectionDialog(parent.getShell(),
 					new ESBrowserLabelProvider());
 				List<ProjectSpace> relevantProjectSpaces = new ArrayList<ProjectSpace>();
 				for (ProjectSpace ps : WorkspaceManager.getInstance().getCurrentWorkspace().getProjectSpaces()) {
 					if (ps.getUsersession() != null) {
 						relevantProjectSpaces.add(ps);
 					}
 				}
 				elsd.setElements(relevantProjectSpaces.toArray());
 				elsd.setMultipleSelection(false);
 				elsd.setTitle("Select a project from the workspace");
 				elsd.setMessage("Please select a project from the current workspace.");
 				if (Dialog.OK == elsd.open()) {
 					for (Object o : elsd.getResult()) {
 						ProjectSpace resultSelection = (ProjectSpace) o;
 						if (resultSelection != null) {
 							setInput(resultSelection);
 						}
 						break;
 					}
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				this.widgetSelected(e);
 			}
 		});
 		// noProjectHint = new Label(parent, SWT.WRAP);
 		// GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(noProjectHint);
 		// noProjectHint.setText("Please call 'Show history' from the context menu of an element in the navigator.");
 
 		viewer = new TreeViewerWithModelElementSelectionProvider(parent, SWT.NONE);
 
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.add(new Separator("additions"));
 		getSite().registerContextMenu(menuMgr, viewer);
 		Control control = viewer.getControl();
 		Menu menu = menuMgr.createContextMenu(control);
 		control.setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 
 		getSite().setSelectionProvider(viewer);
 
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
 		ColumnViewerToolTipSupport.enableFor(viewer);
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 
 			public void doubleClick(DoubleClickEvent event) {
 				if (event.getSelection() instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
 					if (element instanceof EObject) {
 						// TODO: not implementors for OpenModelElementObserver available
 						// WorkspaceManager.getObserverBus().notify(OpenModelElementObserver.class)
 						// .openModelElement((EObject) element);
 						// ElementOpenerHelper.openModelElement((EObject) node.getValue(), VIEW_ID);
 					}
 				}
 
 			}
 		});
 
 		changesColumn = new TreeViewerColumn(viewer, SWT.NONE);
 		changesColumn.getColumn().setText("Changes");
 		changesColumn.getColumn().setWidth(200);
 
 		graphColumn = new TreeViewerColumn(viewer, SWT.NONE);
 		graphColumn.getColumn().setText("Branches");
 		graphColumn.getColumn().setWidth(150);
 		viewer.getTree().addListener(SWT.PaintItem, new Listener() {
 
 			public void handleEvent(Event event) {
 				doPaint(event);
 
 			}
 		});
 
 		messageColumn = new TreeViewerColumn(viewer, SWT.NONE);
 		messageColumn.getColumn().setText("Commit message");
 		messageColumn.getColumn().setWidth(250);
 
 		commitInfoColumn = new TreeViewerColumn(viewer, SWT.NONE);
 		commitInfoColumn.getColumn().setText("Author and date");
 		commitInfoColumn.getColumn().setWidth(200);
 
 		renderer = new SWTPlotRenderer(parent.getDisplay());
 
 		Tree tree = viewer.getTree();
 		tree.setHeaderVisible(true);
 
 		hookToobar();
 	}
 
 	/**
 	 * Paints a certain column of the TreeViewer.
 	 * 
 	 * @param event The underlying paint event.
 	 */
 	protected void doPaint(final Event event) {
 		if (event.index != BRANCH_COLUMN) {
 			return;
 		}
 
 		Object data;
 		TreeItem currItem = (TreeItem) event.item;
 		data = currItem.getData();
 		boolean isCommitItem = true;
 
 		while (!(data instanceof HistoryInfo)) {
 			isCommitItem = false;
 			currItem = currItem.getParentItem();
 			if (currItem == null) {
 				// no history info in parent hierarchy, do not draw.
 				// Happens e.g. if the user deactivates showing the commits
 				return;
 			}
 			data = currItem.getData();
 		}
 
 		assert data instanceof HistoryInfo : "Would have returned otherwise.";
 
 		final IPlotCommit c = commitProvider.getCommitFor((HistoryInfo) data, !isCommitItem);
 		final PlotLane lane = c.getLane();
 		if (lane != null && lane.getSaturatedColor().isDisposed()) {
 			return;
 		}
 		// if (highlight != null && c.has(highlight))
 		// event.gc.setFont(hFont);
 		// else
 		event.gc.setFont(nFont);
 
 		renderer.paint(event, c);
 	}
 
 	private void hookToobar() {
 		IActionBars bars = getViewSite().getActionBars();
 		IToolBarManager menuManager = bars.getToolBarManager();
 
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 
 		addExpandAllAndCollapseAllAction(menuManager);
 		addRefreshAction(menuManager);
 		addShowAllBranchesAction(menuManager);
 		addNextAndPreviousAction(menuManager);
 		addJumpToRevisionAction(menuManager);
 		addLinkWithNavigatorAction(menuManager);
 	}
 
 	private void addExpandAllAndCollapseAllAction(IToolBarManager menuManager) {
 		final ImageDescriptor expandImg = Activator.getImageDescriptor("icons/expandall.gif");
 		final ImageDescriptor collapseImg = Activator.getImageDescriptor("icons/collapseall.gif");
 
 		expandAndCollapse = new ExCoAction("", SWT.TOGGLE, expandImg, collapseImg);
 		expandAndCollapse.setImageDescriptor(expandImg);
 		expandAndCollapse.setToolTipText("Use this toggle to expand or collapse all elements");
 		menuManager.add(expandAndCollapse);
 	}
 
 	private void addRefreshAction(IToolBarManager menuManager) {
 		Action refresh = new Action() {
 			@Override
 			public void run() {
 				refresh();
 			}
 
 		};
 		refresh.setImageDescriptor(Activator.getImageDescriptor("/icons/refresh.png"));
 		refresh.setToolTipText("Refresh");
 		menuManager.add(refresh);
 	}
 
 	private void addShowAllBranchesAction(IToolBarManager menuManager) {
 		Action showAllBranches = new Action("", SWT.TOGGLE) {
 			@Override
 			public void run() {
 				paginationManager.setShowAllVersions(isChecked());
 				refresh();
 			}
 
 		};
 		showAllBranches.setImageDescriptor(Activator.getImageDescriptor("icons/arrow_branch.png"));
 		showAllBranches.setToolTipText("Show All Branches");
 		showAllBranches.setChecked(DEFAULT_SHOW_ALL_BRANCHES);
 		menuManager.add(showAllBranches);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (adapterFactory != null) {
 			adapterFactory.dispose();
 		}
 		if (changePackageVisualizationHelper != null) {
 			changePackageVisualizationHelper.dispose();
 		}
 		super.dispose();
 	}
 
 	private void addNextAndPreviousAction(IToolBarManager menuManager) {
 		Action prev = new Action() {
 			@Override
 			public void run() {
 				paginationManager.previousPage();
 				refresh();
 			}
 
 		};
 		prev.setImageDescriptor(Activator.getImageDescriptor("/icons/prev.png"));
 		prev.setToolTipText("Previous " + INFOS_ABOVE_BASE + " items");
 		menuManager.add(prev);
 
 		Action next = new Action() {
 			@Override
 			public void run() {
 				paginationManager.nextPage();
 				refresh();
 			}
 
 		};
 		next.setImageDescriptor(Activator.getImageDescriptor("/icons/next.png"));
 		next.setToolTipText("Next " + INFOS_BELOW_BASE + " items");
 		menuManager.add(next);
 	}
 
 	private void addJumpToRevisionAction(IToolBarManager menuManager) {
 		Action jumpTo = new Action() {
 			@Override
 			public void run() {
 				InputDialog inputDialog = new InputDialog(getSite().getShell(), "Go to revision", "Revision", "", null);
 				if (inputDialog.open() == Window.OK) {
 					try {
 						int temp = Integer.parseInt(inputDialog.getValue());
 						paginationManager.setVersion(temp);
 						refresh();
 					} catch (NumberFormatException e) {
 						MessageDialog.openError(getSite().getShell(), "Error", "A numeric value was expected!");
 						run();
 					} catch (EmfStoreException e) {
 						EMFStoreMessageDialog
 							.showExceptionDialog("Error: The version you requested does not exist.", e);
 					}
 				}
 			}
 
 		};
 		jumpTo.setImageDescriptor(Activator.getImageDescriptor("/icons/magnifier.png"));
 		jumpTo.setToolTipText("Go to revision...");
 		menuManager.add(jumpTo);
 	}
 
 	private void addLinkWithNavigatorAction(IToolBarManager menuManager) {
 		isUnlinkedFromNavigator = Activator.getDefault().getDialogSettings().getBoolean("LinkWithNavigator");
 		Action linkWithNavigator = new Action("Link with navigator", SWT.TOGGLE) {
 
 			@Override
 			public void run() {
 				Activator.getDefault().getDialogSettings().put("LinkWithNavigator", !this.isChecked());
 				isUnlinkedFromNavigator = (!this.isChecked());
 			}
 
 		};
 		linkWithNavigator.setImageDescriptor(Activator.getImageDescriptor("icons/link_with_editor.gif"));
 		linkWithNavigator.setToolTipText("Link with Navigator");
 		linkWithNavigator.setChecked(!isUnlinkedFromNavigator);
 		menuManager.add(linkWithNavigator);
 	}
 
 	/**
 	 * Refreshes the view using the current end point.
 	 * 
 	 * @throws EmfStoreException
 	 */
 	public void refresh() {
 		int prevHead = headVersion;
 		try {
 			headVersion = projectSpace.resolveVersionSpec(Versions.createHEAD(projectSpace.getBaseVersion()))
 				.getIdentifier();
 		} catch (EmfStoreException e) {
 			headVersion = prevHead;
 		}
 		expandAndCollapse.setChecked(false);
 		expandAndCollapse.setImage(true);
 		load();
 		viewer.setContentProvider(contentProvider);
 		List<HistoryInfo> historyInfos = getHistoryInfos();
 		commitProvider.refresh(historyInfos);
 		viewer.setInput(historyInfos);
 	}
 
 	private void load() {
 		try {
 			new ServerCall<Void>(projectSpace.getUsersession()) {
 				@Override
 				protected Void run() throws EmfStoreException {
 					loadContent();
 					return null;
 				}
 			}.execute();
 		} catch (EmfStoreException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 	}
 
 	private void loadContent() throws EmfStoreException {
 		if (projectSpace == null) {
 			historyInfos.clear();
 			return;
 		}
 		// HistoryQuery query = getQuery(centerVersion);
 		// List<HistoryInfo> historyInfo = projectSpace.getHistoryInfo(query);
 		List<HistoryInfo> historyInfo = paginationManager.retrieveHistoryInfos();
 
 		if (historyInfo != null) {
 			for (HistoryInfo hi : historyInfo) {
 				if (hi.getPrimerySpec().equals(projectSpace.getBaseVersion())) {
 					TagVersionSpec spec = VersioningFactory.eINSTANCE.createTagVersionSpec();
 					spec.setName(VersionSpec.BASE);
 
 					if (!containsTag(hi.getTagSpecs(), spec)) {
 						hi.getTagSpecs().add(spec);
 					}
 					break;
 				}
 			}
 			historyInfos.clear();
 			historyInfos.addAll(historyInfo);
 		}
 		ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();
 		changePackage.getOperations().addAll(ModelUtil.clone(projectSpace.getOperations()));
 		changePackageCache.put(-1, changePackage);
 		for (HistoryInfo hi : historyInfos) {
 			if (hi.getChangePackage() != null) {
 				changePackageCache.put(hi.getPrimerySpec().getIdentifier(), hi.getChangePackage());
 			}
 		}
 		changePackageVisualizationHelper = new ChangePackageVisualizationHelper(new ArrayList<ChangePackage>(
 			changePackageCache.values()), projectSpace.getProject());
 		labelProvider.setChangePackageVisualizationHelper(changePackageVisualizationHelper);
 		logLabelProvider.setChangePackageVisualizationHelper(changePackageVisualizationHelper);
 
 		// contentProvider.setChangePackageVisualizationHelper(changePackageVisualizationHelper);
 	}
 
 	/**
 	 * Checks whether the list of TagVersionSpec's contains the given spec. This method exists as the EList
 	 * implementation does not base contain on {@link #equals(Object)}.
 	 * 
 	 * @param tagSpecs The list to search for the containing object.
 	 * @param spec The object to search for.
 	 * @return true if the object has been found, false otherwise.
 	 */
 	private boolean containsTag(EList<TagVersionSpec> tagSpecs, TagVersionSpec spec) {
 		for (TagVersionSpec listSpec : tagSpecs) {
 			if (listSpec.equals(spec)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Set the input for the History Browser.
 	 * 
 	 * @param projectSpace
 	 *            the input project space
 	 */
 	public void setInput(ProjectSpace projectSpace) {
 		setInput(projectSpace, null);
 	}
 
 	/**
 	 * Set the input for the History Browser.
 	 * 
 	 * @param projectSpace
 	 *            the input project space
 	 * @param me
 	 *            the input model element
 	 */
 	public void setInput(ProjectSpace projectSpace, EObject me) {
 		noProjectHint.dispose();
 		this.parent.layout();
 		this.projectSpace = projectSpace;
 		modelElement = me;
 		String label = "History for ";
 		Project project = projectSpace.getProject();
 		contentProvider = new SCMContentProvider();
 		commitProvider = new PlotCommitProvider();
 		paginationManager = new PaginationManager(projectSpace, me, INFOS_ABOVE_BASE, INFOS_BELOW_BASE);
 		paginationManager.setShowAllVersions(DEFAULT_SHOW_ALL_BRANCHES);
 
 		if (me != null && project.containsInstance(me)) {
 			label += adapterFactoryLabelProvider.getText(me);
			// contentProvider.setShowRootNodes(false);
 		} else {
 			label += projectSpace.getProjectName();
			// contentProvider.setShowRootNodes(true);
 		}
 
 		setContentDescription(label);
 
 		graphColumn.setLabelProvider(new BranchGraphLabelProvider());
 
 		labelProvider = new HistorySCMLabelProvider(project);
 		changesColumn.setLabelProvider(labelProvider);
 
 		logLabelProvider = new LogMessageColumnLabelProvider(project);
 		messageColumn.setLabelProvider(logLabelProvider);
 
 		commitInfoColumn.setLabelProvider(new CommitInfoColumnLabelProvider());
 
 		refresh();
 	}
 
 	/**
 	 * Returns a list of history infos.
 	 * 
 	 * @return a list of history infos
 	 */
 	public List<HistoryInfo> getHistoryInfos() {
 
 		ArrayList<HistoryInfo> revisions = new ArrayList<HistoryInfo>();
 		if (projectSpace != null) {
 			// TODO: add a feature "hide local revision"
 			HistoryInfo localHistoryInfo = VersioningFactory.eINSTANCE.createHistoryInfo();
 			ChangePackage changePackage = projectSpace.getLocalChangePackage(false);
 			// filter for modelelement, do additional sanity check as the
 			// project space could've been also selected
 			if (modelElement != null && projectSpace.getProject().containsInstance(modelElement)) {
 				Set<AbstractOperation> operationsToRemove = new HashSet<AbstractOperation>();
 				for (AbstractOperation ao : changePackage.getOperations()) {
 					if (!ao.getAllInvolvedModelElements().contains(
 						ModelUtil.getProject(modelElement).getModelElementId(modelElement))) {
 						operationsToRemove.add(ao);
 					}
 				}
 				changePackage.getOperations().removeAll(operationsToRemove);
 			}
 			localHistoryInfo.setChangePackage(changePackage);
 			PrimaryVersionSpec versionSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 			versionSpec.setIdentifier(-1);
 			localHistoryInfo.setPrimerySpec(versionSpec);
 			revisions.add(localHistoryInfo);
 		}
 		revisions.addAll(historyInfos);
 
 		return revisions;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	/**
 	 * @return the changePackageVisualizationHelper
 	 */
 	public ChangePackageVisualizationHelper getChangePackageVisualizationHelper() {
 		return changePackageVisualizationHelper;
 	}
 
 	/**
 	 * Highlights the given operations.
 	 * 
 	 * @param operations
 	 *            the operations
 	 */
 	public void highlightOperations(List<OperationId> operations) {
 		labelProvider.getHighlighted().clear();
 		labelProvider.getHighlighted().addAll(operations);
 		refresh();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.util.ProjectSpaceContainer#getProjectSpace()
 	 */
 	public ProjectSpace getProjectSpace() {
 		if (isUnlinkedFromNavigator) {
 			return null;
 		}
 		return this.projectSpace;
 	}
 
 }
