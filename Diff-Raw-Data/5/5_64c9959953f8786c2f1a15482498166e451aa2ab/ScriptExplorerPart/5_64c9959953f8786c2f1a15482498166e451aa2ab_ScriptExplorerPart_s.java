 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.scriptview;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.PerformanceStats;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.StandardModelElementContentProvider;
 import org.eclipse.dltk.internal.ui.dnd.DLTKViewerDragSupport;
 import org.eclipse.dltk.internal.ui.dnd.DLTKViewerDropSupport;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.internal.ui.editor.ExternalStorageEditorInput;
 import org.eclipse.dltk.internal.ui.navigator.ScriptExplorerContentProvider;
 import org.eclipse.dltk.internal.ui.navigator.ScriptExplorerLabelProvider;
 import org.eclipse.dltk.internal.ui.workingsets.ConfigureWorkingSetAction;
 import org.eclipse.dltk.internal.ui.workingsets.ViewActionGroup;
 import org.eclipse.dltk.internal.ui.workingsets.WorkingSetFilterActionGroup;
 import org.eclipse.dltk.internal.ui.workingsets.WorkingSetModel;
 import org.eclipse.dltk.ui.DLTKUILanguageManager;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.IScriptExplorerViewPart;
 import org.eclipse.dltk.ui.ModelElementSorter;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.dltk.ui.actions.CustomFiltersActionGroup;
 import org.eclipse.dltk.ui.viewsupport.AppearanceAwareLabelProvider;
 import org.eclipse.dltk.ui.viewsupport.DecoratingModelLabelProvider;
 import org.eclipse.dltk.ui.viewsupport.FilterUpdater;
 import org.eclipse.dltk.ui.viewsupport.IViewPartInputProvider;
 import org.eclipse.dltk.ui.viewsupport.ProblemTreeViewer;
 import org.eclipse.dltk.ui.viewsupport.StatusBarUpdater;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IElementComparer;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.IOpenListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeExpansionEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.XMLMemento;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.ISetSelectionTarget;
 import org.eclipse.ui.part.IShowInSource;
 import org.eclipse.ui.part.IShowInTarget;
 import org.eclipse.ui.part.IShowInTargetList;
 import org.eclipse.ui.part.ShowInContext;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.framelist.Frame;
 import org.eclipse.ui.views.framelist.FrameAction;
 import org.eclipse.ui.views.framelist.FrameList;
 import org.eclipse.ui.views.framelist.IFrameSource;
 import org.eclipse.ui.views.framelist.TreeFrame;
 
 /**
  * The ViewPart for the ProjectExplorer. It listens to part activation events.
  * When selection linking with the editor is enabled the view selection tracks
  * the active editor page. Similarly when a resource is selected in the packages
  * view the corresponding editor is activated.
  */
 
 public class ScriptExplorerPart extends ViewPart implements
 		ISetSelectionTarget, IMenuListener, IShowInTarget,
 		IScriptExplorerViewPart, IPropertyChangeListener,
 		IViewPartInputProvider {
 
 	private static final String PERF_CREATE_PART_CONTROL = "org.eclipse.dltk.ui/perf/explorer/createPartControl"; //$NON-NLS-1$
 	private static final String PERF_MAKE_ACTIONS = "org.eclipse.dltk.ui/perf/explorer/makeActions"; //$NON-NLS-1$
 
 	private static final int HIERARCHICAL_LAYOUT = 0x1;
 	private static final int FLAT_LAYOUT = 0x2;
 
 	public static final int PROJECTS_AS_ROOTS = 1;
 	public static final int WORKING_SETS_AS_ROOTS = 2;
 
 	public final static String VIEW_ID = "org.eclipse.dltk.ui.view.scriptExplorer"; //$NON-NLS-1$
 
 	// Persistence tags.
 	private static final String TAG_LAYOUT = "layout"; //$NON-NLS-1$
 	private static final String TAG_GROUP_LIBRARIES = "group_libraries"; //$NON-NLS-1$
 	private static final String TAG_ROOT_MODE = "rootMode"; //$NON-NLS-1$
 	private static final String TAG_LINK_EDITOR = "linkWithEditor"; //$NON-NLS-1$
 	private static final String TAG_MEMENTO = "memento"; //$NON-NLS-1$
 
 	private boolean fIsCurrentLayoutFlat; // true means flat, false means
 	// hierarchical
 	private boolean fShowLibrariesNode;
 	private boolean fLinkingEnabled;
 
 	private int fRootMode;
 	private WorkingSetModel fWorkingSetModel;
 
 	private ScriptExplorerLabelProvider fLabelProvider;
 	private DecoratingModelLabelProvider fDecoratingLabelProvider;
 	private ScriptExplorerContentProvider fContentProvider;
 	private FilterUpdater fFilterUpdater;
 
 	private ScriptExplorerActionGroup fActionSet;
 	private ProblemTreeViewer fViewer;
 	private Menu fContextMenu;
 
 	private IMemento fMemento;
 
 	private ISelection fLastOpenSelection;
 	private final ISelectionChangedListener fPostSelectionListener;
 
 	private String fWorkingSetLabel;
 	private IDialogSettings fDialogSettings;
 
 	private IPartListener2 fLinkWithEditorListener = new IPartListener2() {
 		public void partVisible(IWorkbenchPartReference partRef) {
 		}
 
 		public void partBroughtToTop(IWorkbenchPartReference partRef) {
 		}
 
 		public void partClosed(IWorkbenchPartReference partRef) {
 		}
 
 		public void partDeactivated(IWorkbenchPartReference partRef) {
 		}
 
 		public void partHidden(IWorkbenchPartReference partRef) {
 		}
 
 		public void partOpened(IWorkbenchPartReference partRef) {
 		}
 
 		public void partInputChanged(IWorkbenchPartReference partRef) {
 			if (partRef instanceof IEditorReference) {
 				editorActivated(((IEditorReference) partRef).getEditor(true));
 			}
 		}
 
 		public void partActivated(IWorkbenchPartReference partRef) {
 			if (partRef instanceof IEditorReference) {
 				editorActivated(((IEditorReference) partRef).getEditor(true));
 			}
 		}
 	};
 
 	private ITreeViewerListener fExpansionListener = new ITreeViewerListener() {
 		public void treeCollapsed(TreeExpansionEvent event) {
 		}
 
 		public void treeExpanded(TreeExpansionEvent event) {
 			Object element = event.getElement();
 			if (element instanceof ISourceModule) {
 				expandMainType(element);
 			}
 		}
 	};
 
 	protected class PackageExplorerProblemTreeViewer extends ProblemTreeViewer {
 		// fix for 64372 Projects showing up in Package Explorer twice [package
 		// explorer]
 		private List fPendingRefreshes;
 
 		public PackageExplorerProblemTreeViewer(Composite parent, int style) {
 			super(parent, style);
 			fPendingRefreshes = Collections.synchronizedList(new ArrayList());
 		}
 
 		public void add(Object parentElement, Object[] childElements) {
 			if (fPendingRefreshes.contains(parentElement)) {
 				return;
 			}
 			super.add(parentElement, childElements);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.jface.viewers.AbstractTreeViewer#internalRefresh(java
 		 * .lang.Object, boolean)
 		 */
 		protected void internalRefresh(Object element, boolean updateLabels) {
 			try {
 				fPendingRefreshes.add(element);
 				super.internalRefresh(element, updateLabels);
 			} finally {
 				fPendingRefreshes.remove(element);
 			}
 		}
 
 		protected boolean evaluateExpandableWithFilters(Object parent) {
 			if (parent instanceof IScriptProject
 					|| parent instanceof ISourceModule
 					|| parent instanceof BuildPathContainer) {
 				return false;
 			}
 			if (parent instanceof IProjectFragment
 					&& ((IProjectFragment) parent).isArchive()) {
 				return false;
 			}
 			return true;
 		}
 
 		protected boolean isFiltered(Object object, Object parent,
 				ViewerFilter[] filters) {
 			// if (object instanceof PackageFragmentRootContainer) {
 			// return !hasFilteredChildren(object);
 			// }
 
 			boolean res = super.isFiltered(object, parent, filters);
 			if (res && isEssential(object)) {
 				return false;
 			}
 			return res;
 		}
 
 		/*
 		 * Checks if a filtered object in essential (i.e. is a parent that
 		 * should not be removed).
 		 */
 		private boolean isEssential(Object object) {
 			try {
 				if (!isFlatLayout() && object instanceof IScriptFolder) {
 					IScriptFolder fragment = (IScriptFolder) object;
 					if (!fragment.isRootFolder() && fragment.hasSubfolders()) {
 						return hasFilteredChildren(fragment);
 					}
 				}
 			} catch (ModelException e) {
 				DLTKUIPlugin.log(e);
 			}
 			return false;
 		}
 
 		protected void handleInvalidSelection(ISelection invalidSelection,
 				ISelection newSelection) {
 			IStructuredSelection is = (IStructuredSelection) invalidSelection;
 			List ns = null;
 			if (newSelection instanceof IStructuredSelection) {
 				ns = new ArrayList(((IStructuredSelection) newSelection)
 						.toList());
 			} else {
 				ns = new ArrayList();
 			}
 			boolean changed = false;
 			for (Iterator iter = is.iterator(); iter.hasNext();) {
 				Object element = iter.next();
 				if (element instanceof IScriptProject) {
 					IProject project = ((IScriptProject) element).getProject();
 					if (!project.isOpen() && project.exists()) {
 						ns.add(project);
 						changed = true;
 					}
 				} else if (element instanceof IProject) {
 					IProject project = (IProject) element;
 					if (project.isOpen()) {
 						IScriptProject jProject = DLTKCore.create(project);
 						if (jProject != null && jProject.exists()) {
 							ns.add(jProject);
 						}
 						changed = true;
 					}
 				}
 			}
 			if (changed) {
 				newSelection = new StructuredSelection(ns);
 				setSelection(newSelection);
 			}
 			super.handleInvalidSelection(invalidSelection, newSelection);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		protected Object[] addAditionalProblemParents(Object[] elements) {
 			if (getRootMode() == ScriptExplorerPart.WORKING_SETS_AS_ROOTS
 					&& elements != null) {
 				return fWorkingSetModel.addWorkingSets(elements);
 			}
 			return elements;
 		}
 
 		// ---- special handling to preserve the selection correctly
 		private boolean fInPreserveSelection;
 
 		protected void preservingSelection(Runnable updateCode) {
 			try {
 				fInPreserveSelection = true;
 				super.preservingSelection(updateCode);
 			} finally {
 				fInPreserveSelection = false;
 			}
 		}
 
 		protected void setSelectionToWidget(ISelection selection, boolean reveal) {
 			if (true) {
 				super.setSelectionToWidget(selection, reveal);
 				return;
 			}
 			if (!fInPreserveSelection || !(selection instanceof ITreeSelection)) {
 				super.setSelectionToWidget(selection, reveal);
 				return;
 			}
 			IContentProvider cp = getContentProvider();
 			if (!(cp instanceof IMultiElementTreeContentProvider)) {
 				super.setSelectionToWidget(selection, reveal);
 				return;
 			}
 			IMultiElementTreeContentProvider contentProvider = (IMultiElementTreeContentProvider) cp;
 			ITreeSelection toRestore = (ITreeSelection) selection;
 			List pathsToSelect = new ArrayList();
 			for (Iterator iter = toRestore.iterator(); iter.hasNext();) {
 				Object element = iter.next();
 				TreePath[] pathsToRestore = toRestore.getPathsFor(element);
 				CustomHashtable currentParents = createRootAccessedMap(contentProvider
 						.getTreePaths(element));
 				for (int i = 0; i < pathsToRestore.length; i++) {
 					TreePath path = pathsToRestore[i];
 					Object root = path.getFirstSegment();
 					if (root != null
 							&& path.equals((TreePath) currentParents.get(root),
 									getComparer())) {
 						pathsToSelect.add(path);
 					}
 				}
 			}
 			List toSelect = new ArrayList();
 			for (Iterator iter = pathsToSelect.iterator(); iter.hasNext();) {
 				TreePath path = (TreePath) iter.next();
 				int size = path.getSegmentCount();
 				if (size == 0) {
 					continue;
 				}
 				Widget current = getTree();
 				int last = size - 1;
 				Object segment;
 				for (int i = 0; i < size && current != null
 						&& (segment = path.getSegment(i)) != null; i++) {
 					internalExpandToLevel(current, 1);
 					current = internalFindChild(current, segment);
 					if (i == last && current != null) {
 						toSelect.add(current);
 					}
 				}
 			}
 			getTree().setSelection(
 					(TreeItem[]) toSelect
 							.toArray(new TreeItem[toSelect.size()]));
 		}
 
 		private Widget internalFindChild(Widget parent, Object element) {
 			Item[] items = getChildren(parent);
 			for (int i = 0; i < items.length; i++) {
 				Item item = items[i];
 				Object data = item.getData();
 				if (data != null && equals(data, element)) {
 					return item;
 				}
 			}
 			return null;
 		}
 
 		private CustomHashtable createRootAccessedMap(TreePath[] paths) {
 			CustomHashtable result = new CustomHashtable(getComparer());
 			for (int i = 0; i < paths.length; i++) {
 				TreePath path = paths[i];
 				Object root = path.getFirstSegment();
 				if (root != null) {
 					result.put(root, path);
 				}
 			}
 			return result;
 		}
 	}
 
 	public ScriptExplorerPart() {
 		fPostSelectionListener = new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				handlePostSelectionChanged(event);
 			}
 		};
 
 		// exception: initialize from preference
 		fDialogSettings = DLTKUIPlugin.getDefault().getDialogSettingsSection(
 				getClass().getName());
 
 		// on by default
 		fShowLibrariesNode = fDialogSettings
 				.get(ScriptExplorerPart.TAG_GROUP_LIBRARIES) == null
 				|| fDialogSettings
 						.getBoolean(ScriptExplorerPart.TAG_GROUP_LIBRARIES);
 
 		fLinkingEnabled = fDialogSettings
 				.getBoolean(ScriptExplorerPart.TAG_LINK_EDITOR);
 
 		try {
 			fIsCurrentLayoutFlat = fDialogSettings
 					.getInt(ScriptExplorerPart.TAG_LAYOUT) == ScriptExplorerPart.FLAT_LAYOUT;
 		} catch (NumberFormatException e) {
 			fIsCurrentLayoutFlat = true;
 		}
 
 		try {
 			fRootMode = fDialogSettings
 					.getInt(ScriptExplorerPart.TAG_ROOT_MODE);
 		} catch (NumberFormatException e) {
 			fRootMode = ScriptExplorerPart.PROJECTS_AS_ROOTS;
 		}
 
 	}
 
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		if (memento == null) {
 			String persistedMemento = fDialogSettings
 					.get(ScriptExplorerPart.TAG_MEMENTO);
 			if (persistedMemento != null) {
 				try {
 					memento = XMLMemento.createReadRoot(new StringReader(
 							persistedMemento));
 				} catch (WorkbenchException e) {
 					// don't do anything. Simply don't restore the settings
 				}
 			}
 		}
 		fMemento = memento;
 		if (memento != null) {
 			restoreLayoutState(memento);
 			restoreLinkingEnabled(memento);
 			restoreRootMode(memento);
 		}
 		if (getRootMode() == ScriptExplorerPart.WORKING_SETS_AS_ROOTS) {
 			createWorkingSetModel();
 		}
 	}
 
 	private void restoreRootMode(IMemento memento) {
 		Integer value = memento.getInteger(ScriptExplorerPart.TAG_ROOT_MODE);
 		fRootMode = value == null ? ScriptExplorerPart.PROJECTS_AS_ROOTS
 				: value.intValue();
 		if (fRootMode != ScriptExplorerPart.PROJECTS_AS_ROOTS
 				&& fRootMode != ScriptExplorerPart.WORKING_SETS_AS_ROOTS) {
 			fRootMode = ScriptExplorerPart.PROJECTS_AS_ROOTS;
 		}
 	}
 
 	private void restoreLayoutState(IMemento memento) {
 		Integer layoutState = memento.getInteger(ScriptExplorerPart.TAG_LAYOUT);
 		fIsCurrentLayoutFlat = layoutState == null
 				|| layoutState.intValue() == ScriptExplorerPart.FLAT_LAYOUT;
 
 		// on by default
 		Integer groupLibraries = memento
 				.getInteger(ScriptExplorerPart.TAG_GROUP_LIBRARIES);
 		fShowLibrariesNode = groupLibraries == null
 				|| groupLibraries.intValue() != 0;
 	}
 
 	/**
 	 * Returns the package explorer part of the active perspective. If there
 	 * isn't any package explorer part <code>null</code> is returned.
 	 */
 	public static ScriptExplorerPart getFromActivePerspective() {
 		IWorkbenchPage activePage = DLTKUIPlugin.getActivePage();
 		if (activePage == null) {
 			return null;
 		}
 		IViewPart view = activePage.findView(ScriptExplorerPart.VIEW_ID);
 		if (view instanceof ScriptExplorerPart) {
 			return (ScriptExplorerPart) view;
 		}
 		return null;
 	}
 
 	/**
 	 * Makes the package explorer part visible in the active perspective. If
 	 * there isn't a package explorer part registered <code>null</code> is
 	 * returned. Otherwise the opened view part is returned.
 	 */
 	public static ScriptExplorerPart openInActivePerspective() {
 		try {
 			return (ScriptExplorerPart) DLTKUIPlugin.getActivePage().showView(
 					ScriptExplorerPart.VIEW_ID);
 		} catch (PartInitException pe) {
 			return null;
 		}
 	}
 
 	public void dispose() {
 		XMLMemento memento = XMLMemento.createWriteRoot("scriptExplorer"); //$NON-NLS-1$
 		saveState(memento);
 		StringWriter writer = new StringWriter();
 		try {
 			memento.save(writer);
 			fDialogSettings.put(ScriptExplorerPart.TAG_MEMENTO, writer
 					.getBuffer().toString());
 		} catch (IOException e) {
 			// don't do anything. Simply don't store the settings
 		}
 
 		if (fContextMenu != null && !fContextMenu.isDisposed()) {
 			fContextMenu.dispose();
 		}
 
 		getSite().getPage().removePartListener(fLinkWithEditorListener); // always
 		// remove
 		// even
 		// if
 		// we
 		// didn't
 		// register
 
 		DLTKUIPlugin.getDefault().getPreferenceStore()
 				.removePropertyChangeListener(this);
 		if (fViewer != null) {
 			fViewer.removeTreeListener(fExpansionListener);
 		}
 
 		if (fActionSet != null) {
 			fActionSet.dispose();
 		}
 		if (fFilterUpdater != null) {
 			ResourcesPlugin.getWorkspace().removeResourceChangeListener(
 					fFilterUpdater);
 		}
 		if (fWorkingSetModel != null) {
 			fWorkingSetModel.dispose();
 		}
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
 	 * .Composite)
 	 */
 	public void createPartControl(Composite parent) {
 
 		final PerformanceStats stats = PerformanceStats.getStats(
 				ScriptExplorerPart.PERF_CREATE_PART_CONTROL, this);
 		stats.startRun();
 
 		fViewer = createViewer(parent);
 		fViewer.setUseHashlookup(true);
 
 		initDragAndDrop();
 
 		setProviders();
 
 		DLTKUIPlugin.getDefault().getPreferenceStore()
 				.addPropertyChangeListener(this);
 
 		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(this);
 		fContextMenu = menuMgr.createContextMenu(fViewer.getTree());
 		fViewer.getTree().setMenu(fContextMenu);
 
 		// Register viewer with site. This must be done before making the
 		// actions.
 		IWorkbenchPartSite site = getSite();
 		site.registerContextMenu(menuMgr, fViewer);
 		site.setSelectionProvider(fViewer);
 
 		makeActions(); // call before registering for selection changes
 
 		// Set input after filter and sorter has been set. This avoids resorting
 		// and refiltering.
 		restoreFilterAndSorter();
 		fViewer.setInput(findInputElement());
 		initFrameActions();
 		initKeyListener();
 
 		fViewer.addPostSelectionChangedListener(fPostSelectionListener);
 
 		fViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				fActionSet.handleDoubleClick(event);
 			}
 		});
 
 		fViewer.addOpenListener(new IOpenListener() {
 			public void open(OpenEvent event) {
 				fActionSet.handleOpen(event);
 				fLastOpenSelection = event.getSelection();
 			}
 		});
 
 		IStatusLineManager slManager = getViewSite().getActionBars()
 				.getStatusLineManager();
 		fViewer.addSelectionChangedListener(new StatusBarUpdater(slManager));
 		fViewer.addTreeListener(fExpansionListener);
 
 		// ScriptUIHelp.setHelp(fViewer, IScriptHelpContextIds.PACKAGES_VIEW);
 
 		fillActionBars();
 
 		updateTitle();
 
 		fFilterUpdater = new FilterUpdater(fViewer);
 		ResourcesPlugin.getWorkspace()
 				.addResourceChangeListener(fFilterUpdater);
 
 		// Sync'ing the package explorer has to be done here. It can't be done
 		// when restoring the link state since the package explorers input isn't
 		// set yet.
 		setLinkingEnabled(isLinkingEnabled());
 
 		stats.endRun();
 	}
 
 	private void initFrameActions() {
 		fActionSet.getUpAction().update();
 		fActionSet.getBackAction().update();
 		fActionSet.getForwardAction().update();
 	}
 
 	/**
 	 * This viewer ensures that non-leaves in the hierarchical layout are not
 	 * removed by any filters.
 	 * 
 	 * 
 	 */
 	protected ProblemTreeViewer createViewer(Composite composite) {
 		return new PackageExplorerProblemTreeViewer(composite, SWT.MULTI
 				| SWT.H_SCROLL | SWT.V_SCROLL);
 	}
 
 	/**
 	 * Answers whether this part shows the packages flat or hierarchical.
 	 * 
 	 * 
 	 */
 	public boolean isFlatLayout() {
 		return fIsCurrentLayoutFlat;
 	}
 
 	private void setProviders() {
 		// content provider must be set before the label provider
 		fContentProvider = createContentProvider();
 		fContentProvider.setIsFlatLayout(fIsCurrentLayoutFlat);
 		// fContentProvider.setShowLibrariesNode(fShowLibrariesNode);
 		fViewer.setContentProvider(fContentProvider);
 
 		fViewer.setComparer(createElementComparer());
 
 		fLabelProvider = createLabelProvider();
 		fLabelProvider.setIsFlatLayout(fIsCurrentLayoutFlat);
 		fDecoratingLabelProvider = new DecoratingModelLabelProvider(
 				fLabelProvider, false, fIsCurrentLayoutFlat);
 		fViewer.setLabelProvider(fDecoratingLabelProvider);
 		// problem decoration provided by PackageLabelProvider
 	}
 
 	public void setShowLibrariesNode(boolean enabled) {
 		fShowLibrariesNode = enabled;
 		saveDialogSettings();
 
 		// fContentProvider.setShowLibrariesNode(enabled);
 		fViewer.getControl().setRedraw(false);
 		fViewer.refresh();
 		fViewer.getControl().setRedraw(true);
 	}
 
 	boolean isLibrariesNodeShown() {
 		return fShowLibrariesNode;
 	}
 
 	public void setFlatLayout(boolean enable) {
 		// Update current state and inform content and label providers
 		fIsCurrentLayoutFlat = enable;
 		saveDialogSettings();
 
 		if (fViewer != null) {
 			fContentProvider.setIsFlatLayout(isFlatLayout());
 			fLabelProvider.setIsFlatLayout(isFlatLayout());
 			fDecoratingLabelProvider.setFlatPackageMode(isFlatLayout());
 
 			fViewer.getControl().setRedraw(false);
 			fViewer.refresh();
 			fViewer.getControl().setRedraw(true);
 		}
 	}
 
 	/**
 	 * This method should only be called inside this class and from test cases.
 	 * 
 	 * @return the created content provider
 	 */
 	public ScriptExplorerContentProvider createContentProvider() {
 
 		boolean showCUChildren = DLTKUIPlugin.getDefault().getPreferenceStore()
 				.getBoolean(PreferenceConstants.SHOW_SOURCE_MODULE_CHILDREN);
 		if (getRootMode() == ScriptExplorerPart.PROJECTS_AS_ROOTS) {
 			return new ScriptExplorerContentProvider(showCUChildren) {
 				protected IPreferenceStore getPreferenceStore() {
 					return DLTKUIPlugin.getDefault().getPreferenceStore();
 				}
 			};
 		} else {
 			return new WorkingSetAwareContentProvider(showCUChildren,
 					fWorkingSetModel) {
 				protected IPreferenceStore getPreferenceStore() {
 					return DLTKUIPlugin.getDefault().getPreferenceStore();
 				}
 			};
 		}
 	}
 
 	protected ScriptExplorerLabelProvider createLabelProvider() {
 
 		final IPreferenceStore store = DLTKUIPlugin.getDefault()
 				.getPreferenceStore();
 		return new ScriptExplorerLabelProvider(fContentProvider, store);
 	}
 
 	protected ScriptExplorerContentProvider getContentProvider() {
 		return fContentProvider;
 	}
 
 	private IElementComparer createElementComparer() {
 		if (getRootMode() == ScriptExplorerPart.PROJECTS_AS_ROOTS) {
 			return null;
 		} else {
 			return WorkingSetModel.COMPARER;
 		}
 	}
 
 	private void fillActionBars() {
 		IActionBars actionBars = getViewSite().getActionBars();
 		fActionSet.fillActionBars(actionBars);
 	}
 
 	private Object findInputElement() {
 		if (getRootMode() == ScriptExplorerPart.WORKING_SETS_AS_ROOTS) {
 			return fWorkingSetModel;
 		} else {
 			Object input = getSite().getPage().getInput();
 			if (input instanceof IWorkspace) {
 				return DLTKCore.create(((IWorkspace) input).getRoot());
 			} else if (input instanceof IContainer) {
 				IModelElement element = DLTKCore.create((IContainer) input);
 				if (element != null && element.exists()) {
 					return element;
 				}
 				return input;
 			}
 			// 1GERPRT: ITPJUI:ALL - Packages View is empty when shown in Type
 			// Hierarchy Perspective
 			// we can't handle the input
 			// fall back to show the workspace
 			return DLTKCore.create(DLTKUIPlugin.getWorkspace().getRoot());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class key) {
 		if (key.equals(ISelectionProvider.class)) {
 			return fViewer;
 		}
 		if (key == IShowInSource.class) {
 			return getShowInSource();
 		}
 		if (key == IShowInTargetList.class) {
 			return new IShowInTargetList() {
 				public String[] getShowInTargetIds() {
 					return new String[] { IPageLayout.ID_RES_NAV };
 				}
 
 			};
 		}
 		// if (key == IContextProvider.class) {
 		// return ScriptUIHelp.getHelpContextProvider(this,
 		// IScriptHelpContextIds.PACKAGES_VIEW);
 		// }
 		if (DLTKCore.DEBUG) {
 			System.err.println("Add help support here..."); //$NON-NLS-1$
 		}
 
 		return super.getAdapter(key);
 	}
 
 	/**
 	 * Returns the tool tip text for the given element.
 	 */
 	String getToolTipText(Object element) {
 		String result;
 		if (!(element instanceof IResource)) {
 			if (element instanceof IScriptModel) {
 				result = ScriptMessages.PackageExplorerPart_workspace;
 			} else if (element instanceof IModelElement) {
 				result = ScriptElementLabels.getDefault().getTextLabel(element,
 						ScriptElementLabels.ALL_FULLY_QUALIFIED);
 			} else if (element instanceof IWorkingSet) {
 				result = ((IWorkingSet) element).getLabel();
 			} else if (element instanceof WorkingSetModel) {
 				result = ScriptMessages.PackageExplorerPart_workingSetModel;
 			} else {
 				result = fLabelProvider.getText(element);
 			}
 		} else {
 			IPath path = ((IResource) element).getFullPath();
 			if (path.isRoot()) {
 				result = ScriptMessages.PackageExplorer_title;
 			} else {
 				result = path.makeRelative().toString();
 			}
 		}
 
 		if (fRootMode == ScriptExplorerPart.PROJECTS_AS_ROOTS) {
 			if (fWorkingSetLabel == null) {
 				return result;
 			}
 			if (result.length() == 0) {
 				return Messages.format(ScriptMessages.PackageExplorer_toolTip,
 						new String[] { fWorkingSetLabel });
 			}
 			return Messages.format(ScriptMessages.PackageExplorer_toolTip2,
 					new String[] { result, fWorkingSetLabel });
 		} else { // Working set mode. During initialization element and
 			// action set can be null.
 			if (element != null && !(element instanceof IWorkingSet)
 					&& !(element instanceof WorkingSetModel)
 					&& fActionSet != null) {
 				FrameList frameList = fActionSet.getFrameList();
 				int index = frameList.getCurrentIndex();
 				IWorkingSet ws = null;
 				while (index >= 0) {
 					Frame frame = frameList.getFrame(index);
 					if (frame instanceof TreeFrame) {
 						Object input = ((TreeFrame) frame).getInput();
 						if (input instanceof IWorkingSet) {
 							ws = (IWorkingSet) input;
 							break;
 						}
 					}
 					index--;
 				}
 				if (ws != null) {
 					return Messages.format(
 							ScriptMessages.PackageExplorer_toolTip3,
 							new String[] { ws.getLabel(), result });
 				} else {
 					return result;
 				}
 			} else {
 				return result;
 			}
 		}
 	}
 
 	public String getTitleToolTip() {
 		if (fViewer == null) {
 			return super.getTitleToolTip();
 		}
 		return getToolTipText(fViewer.getInput());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 		fViewer.getTree().setFocus();
 	}
 
 	private ISelection getSelection() {
 		return fViewer.getSelection();
 	}
 
 	// ---- Action handling
 	// ----------------------------------------------------------
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IMenuListener#menuAboutToShow(IMenuManager)
 	 */
 	public void menuAboutToShow(IMenuManager menu) {
 		DLTKUIPlugin.createStandardGroups(menu);
 
 		fActionSet.setContext(new ActionContext(getSelection()));
 		fActionSet.fillContextMenu(menu);
 		fActionSet.setContext(null);
 	}
 
 	private void makeActions() {
 
 		final PerformanceStats stats = PerformanceStats.getStats(
 				ScriptExplorerPart.PERF_MAKE_ACTIONS, this);
 		stats.startRun();
 
 		fActionSet = getActionGroup();
 		if (fWorkingSetModel != null) {
 			fActionSet.getWorkingSetActionGroup().setWorkingSetModel(
 					fWorkingSetModel);
 		}
 
 		stats.endRun();
 	}
 
 	/**
 	 * To allow inheritance and manipulating lang. specific explorers actions
 	 */
 	protected ScriptExplorerActionGroup getActionGroup() {
 		return new ScriptExplorerActionGroup(this);
 	}
 
 	// ---- Event handling
 	// ----------------------------------------------------------
 
 	private void initDragAndDrop() {
 		initDrag();
 		initDrop();
 	}
 
	private void initDrag() {
 		new DLTKViewerDragSupport(fViewer).start();
 	}
 
	private void initDrop() {
 		DLTKViewerDropSupport dropSupport = new DLTKViewerDropSupport(fViewer);
 		dropSupport.addDropTargetListener(new WorkingSetDropAdapter(this));
 		dropSupport.start();
 	}
 
 	/**
 	 * Handles post selection changed in viewer.
 	 * 
 	 * Links to editor (if option enabled).
 	 */
 	private void handlePostSelectionChanged(SelectionChangedEvent event) {
 		ISelection selection = event.getSelection();
 		// If the selection is the same as the one that triggered the last
 		// open event then do nothing. The editor already got revealed.
 		if (isLinkingEnabled() && !selection.equals(fLastOpenSelection)) {
 			linkToEditor((IStructuredSelection) selection);
 		}
 		fLastOpenSelection = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface
 	 * .viewers.ISelection)
 	 */
 	public void selectReveal(final ISelection selection) {
 		Control ctrl = getTreeViewer().getControl();
 		if (ctrl == null || ctrl.isDisposed()) {
 			return;
 		}
 
 		fContentProvider.runPendingUpdates();
 		fViewer.setSelection(convertSelection(selection), true);
 	}
 
 	public ISelection convertSelection(ISelection s) {
 		if (!(s instanceof IStructuredSelection)) {
 			return s;
 		}
 
 		Object[] elements = ((IStructuredSelection) s).toArray();
 
 		boolean changed = false;
 		for (int i = 0; i < elements.length; i++) {
 			Object convertedElement = convertElement(elements[i]);
 			changed = changed || convertedElement != elements[i];
 			elements[i] = convertedElement;
 		}
 		if (changed) {
 			return new StructuredSelection(elements);
 		} else {
 			return s;
 		}
 	}
 
 	private Object convertElement(Object original) {
 		if (original instanceof IModelElement) {
 			if (original instanceof ISourceModule) {
 				ISourceModule cu = (ISourceModule) original;
 				IScriptProject javaProject = cu.getScriptProject();
 				if (javaProject != null && javaProject.exists()
 						&& !javaProject.isOnBuildpath(cu)) {
 					// could be a working copy of a .java file that is not on
 					// classpath
 					IResource resource = cu.getResource();
 					if (resource != null) {
 						return resource;
 					}
 				}
 
 			}
 			return original;
 
 		} else if (original instanceof IResource) {
 			IModelElement je = DLTKCore.create((IResource) original);
 			if (je != null && je.exists()) {
 				IScriptProject javaProject = je.getScriptProject();
 				if (javaProject != null && javaProject.exists()) {
 					return je;
 				}
 			}
 
 		} else if (original instanceof IAdaptable) {
 			IAdaptable adaptable = (IAdaptable) original;
 			IModelElement je2 = (IModelElement) adaptable
 					.getAdapter(IModelElement.class);
 			if (je2 != null && je2.exists()) {
 				return je2;
 			}
 
 			IResource r = (IResource) adaptable.getAdapter(IResource.class);
 			if (r != null) {
 				je2 = DLTKCore.create(r);
 				if (je2 != null && je2.exists()) {
 					return je2;
 				} else {
 					return r;
 				}
 			}
 		}
 		return original;
 	}
 
 	public void selectAndReveal(Object element) {
 		selectReveal(new StructuredSelection(element));
 	}
 
 	public boolean isLinkingEnabled() {
 		return fLinkingEnabled;
 	}
 
 	/**
 	 * Links to editor (if option enabled)
 	 * 
 	 * @param selection
 	 *            the selection
 	 */
 	private void linkToEditor(IStructuredSelection selection) {
 		// ignore selection changes if the package explorer is not the active
 		// part.
 		// In this case the selection change isn't triggered by a user.
 		if (!isActivePart()) {
 			return;
 		}
 		Object obj = selection.getFirstElement();
 
 		if (selection.size() == 1) {
 			IEditorPart part = EditorUtility.isOpenInEditor(obj);
 			if (part != null) {
 				IWorkbenchPage page = getSite().getPage();
 				page.bringToTop(part);
 				if (obj instanceof IModelElement) {
 					EditorUtility.revealInEditor(part, (IModelElement) obj);
 				}
 			}
 		}
 	}
 
 	private boolean isActivePart() {
 		return this == getSite().getPage().getActivePart();
 	}
 
 	public void saveState(IMemento memento) {
 		if (fViewer == null && fMemento != null) {
 			// part has not been created -> keep the old state
 			memento.putMemento(fMemento);
 			return;
 		}
 
 		memento.putInteger(ScriptExplorerPart.TAG_ROOT_MODE, fRootMode);
 		if (fWorkingSetModel != null) {
 			fWorkingSetModel.saveState(memento);
 		}
 
 		saveLayoutState(memento);
 		saveLinkingEnabled(memento);
 
 		if (fActionSet != null) {
 			fActionSet.saveFilterAndSorterState(memento);
 		}
 	}
 
 	private void saveLinkingEnabled(IMemento memento) {
 		memento.putInteger(ScriptExplorerPart.TAG_LINK_EDITOR,
 				fLinkingEnabled ? 1 : 0);
 	}
 
 	private void saveLayoutState(IMemento memento) {
 		if (memento != null) {
 			memento.putInteger(ScriptExplorerPart.TAG_LAYOUT, getLayoutAsInt());
 			memento.putInteger(ScriptExplorerPart.TAG_GROUP_LIBRARIES,
 					fShowLibrariesNode ? 1 : 0);
 		}
 	}
 
 	private void saveDialogSettings() {
 		fDialogSettings.put(ScriptExplorerPart.TAG_GROUP_LIBRARIES,
 				fShowLibrariesNode);
 		fDialogSettings.put(ScriptExplorerPart.TAG_LAYOUT, getLayoutAsInt());
 		fDialogSettings.put(ScriptExplorerPart.TAG_ROOT_MODE, fRootMode);
 		fDialogSettings
 				.put(ScriptExplorerPart.TAG_LINK_EDITOR, fLinkingEnabled);
 	}
 
 	private int getLayoutAsInt() {
 		if (fIsCurrentLayoutFlat) {
 			return ScriptExplorerPart.FLAT_LAYOUT;
 		} else {
 			return ScriptExplorerPart.HIERARCHICAL_LAYOUT;
 		}
 	}
 
 	private void restoreFilterAndSorter() {
 		setComparator();
 		if (fMemento != null) {
 			fActionSet.restoreFilterAndSorterState(fMemento);
 		}
 	}
 
 	private void restoreLinkingEnabled(IMemento memento) {
 		Integer val = memento.getInteger(ScriptExplorerPart.TAG_LINK_EDITOR);
 		fLinkingEnabled = val != null && val.intValue() != 0;
 	}
 
 	/**
 	 * Create the KeyListener for doing the refresh on the viewer.
 	 */
 	private void initKeyListener() {
 		fViewer.getControl().addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent event) {
 				fActionSet.handleKeyEvent(event);
 			}
 		});
 	}
 
 	/**
 	 * An editor has been activated. Set the selection in this Packages Viewer
 	 * to be the editor's input, if linking is enabled.
 	 * 
 	 * @param editor
 	 *            the activated editor
 	 */
 	protected void editorActivated(IEditorPart editor) {
 		IEditorInput editorInput = editor.getEditorInput();
 		if (editorInput == null) {
 			return;
 		}
 		Object input = getInputFromEditor(editorInput);
 		if (input == null) {
 			return;
 		}
 		if (!inputIsSelected(editorInput)) {
 			showInput(input);
 		} else {
 			getTreeViewer().getTree().showSelection();
 		}
 	}
 
 	private Object getInputFromEditor(IEditorInput editorInput) {
 		Object input = DLTKUIPlugin.getEditorInputModelElement(editorInput);
 		if (input instanceof ISourceModule) {
 			ISourceModule cu = (ISourceModule) input;
 			if (!cu.getScriptProject().isOnBuildpath(cu)) { // test needed for
 				// Java files in
 				// non-source
 				// folders (bug
 				// 207839)
 				input = cu.getResource();
 			}
 		}
 		if (input == null) {
 			input = editorInput.getAdapter(IFile.class);
 		}
 		if (input == null && editorInput instanceof IStorageEditorInput) {
 			try {
 				input = ((IStorageEditorInput) editorInput).getStorage();
 			} catch (CoreException e) {
 				// ignore
 			}
 		}
 		return input;
 	}
 
 	protected boolean inputIsSelected(IEditorInput input) {
 		IStructuredSelection selection = (IStructuredSelection) fViewer
 				.getSelection();
 		if (selection.size() != 1) {
 			return false;
 		}
 
 		IEditorInput selectionAsInput;
 		try {
 			selectionAsInput = EditorUtility.getEditorInput(selection
 					.getFirstElement());
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 		return input.equals(selectionAsInput);
 	}
 
 	protected boolean showInput(Object input) {
 		Object element = null;
 
 		if (input instanceof IFile && isOnBuildpath((IFile) input)) {
 			element = DLTKCore.create((IFile) input);
 		}
 
 		if (element == null) {
 			element = input;
 		}
 
 		if (element != null) {
 			ISelection newSelection = new StructuredSelection(element);
 			if (fViewer.getSelection().equals(newSelection)) {
 				fViewer.reveal(element);
 			} else {
 				try {
 					fViewer
 							.removePostSelectionChangedListener(fPostSelectionListener);
 					fViewer.setSelection(newSelection, true);
 
 					while (element != null && fViewer.getSelection().isEmpty()) {
 						// Try to select parent in case element is filtered
 						element = getParent(element);
 						if (element != null) {
 							newSelection = new StructuredSelection(element);
 							fViewer.setSelection(newSelection, true);
 						}
 					}
 				} finally {
 					fViewer
 							.addPostSelectionChangedListener(fPostSelectionListener);
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isOnBuildpath(IFile file) {
 		IScriptProject jproject = DLTKCore.create(file.getProject());
 		return jproject.isOnBuildpath(file);
 	}
 
 	/**
 	 * Returns the element's parent.
 	 * 
 	 * @return the parent or <code>null</code> if there's no parent
 	 */
 	private Object getParent(Object element) {
 		if (element instanceof IModelElement) {
 			return ((IModelElement) element).getParent();
 		} else if (element instanceof IResource) {
 			return ((IResource) element).getParent();
 		}
 		// else if (element instanceof IStorage) {
 		// can't get parent - see bug 22376
 		// }
 		return null;
 	}
 
 	/**
 	 * A compilation unit or class was expanded, expand the main type.
 	 */
 	void expandMainType(Object element) {
 		try {
 			IType type = null;
 			if (element instanceof ISourceModule) {
 				ISourceModule cu = (ISourceModule) element;
 				IType[] types = cu.getTypes();
 				if (types.length > 0) {
 					type = types[0];
 				}
 			}
 			if (type != null) {
 				final IType type2 = type;
 				Control ctrl = fViewer.getControl();
 				if (ctrl != null && !ctrl.isDisposed()) {
 					ctrl.getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							Control ctrl2 = fViewer.getControl();
 							if (ctrl2 != null && !ctrl2.isDisposed()) {
 								fViewer.expandToLevel(type2, 1);
 							}
 						}
 					});
 				}
 			}
 		} catch (ModelException e) {
 			// no reveal
 		}
 	}
 
 	protected Object getElementOfInput(IEditorInput input) {
 		if (input instanceof IFileEditorInput)
 			return ((IFileEditorInput) input).getFile();
 		else if (input instanceof ExternalStorageEditorInput)
 			return ((ExternalStorageEditorInput) input).getStorage();
 		else if (input instanceof FileStoreEditorInput) {
 			ISourceModule module = DLTKUIPlugin
 					.resolveSourceModule((FileStoreEditorInput) input);
 			if (module != null) {
 				return module;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the TreeViewer.
 	 */
 	public TreeViewer getTreeViewer() {
 		return fViewer;
 	}
 
 	boolean isExpandable(Object element) {
 		if (fViewer == null) {
 			return false;
 		}
 		return fViewer.isExpandable(element);
 	}
 
 	void setWorkingSetLabel(String workingSetName) {
 		fWorkingSetLabel = workingSetName;
 		setTitleToolTip(getTitleToolTip());
 	}
 
 	void updateToolbar() {
 		IActionBars actionBars = getViewSite().getActionBars();
 		fActionSet.fillToolBar(actionBars.getToolBarManager());
 	}
 
 	/**
 	 * Updates the title text and title tool tip. Called whenever the input of
 	 * the viewer changes.
 	 */
 	void updateTitle() {
 		Object input = fViewer.getInput();
 		if (input == null || (input instanceof IScriptModel)) {
 			setContentDescription(""); //$NON-NLS-1$
 			setTitleToolTip(""); //$NON-NLS-1$
 		} else {
 			String inputText = ScriptElementLabels.getDefault().getTextLabel(
 					input, AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS);
 			setContentDescription(inputText);
 			setTitleToolTip(getToolTipText(input));
 		}
 	}
 
 	/**
 	 * Sets the decorator for the package explorer.
 	 * 
 	 * @param decorator
 	 *            a label decorator or <code>null</code> for no decorations.
 	 * @deprecated To be removed
 	 */
 	public void setLabelDecorator(ILabelDecorator decorator) {
 	}
 
 	/*
 	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		if (fViewer == null) {
 			return;
 		}
 
 		boolean refreshViewer = false;
 
 		if (PreferenceConstants.SHOW_SOURCE_MODULE_CHILDREN.equals(event
 				.getProperty())) {
 			fActionSet.updateActionBars(getViewSite().getActionBars());
 
 			boolean showCUChildren = DLTKUIPlugin.getDefault()
 					.getPreferenceStore().getBoolean(
 							PreferenceConstants.SHOW_SOURCE_MODULE_CHILDREN);
 			((StandardModelElementContentProvider) fViewer.getContentProvider())
 					.setProvideMembers(showCUChildren);
 
 			refreshViewer = true;
 		}
 		if (DLTKCore.DEBUG) {
 			System.err
 					.println("Add members order preference cach support here..."); //$NON-NLS-1$
 		}
 		// } else if
 		// (MembersOrderPreferenceCache.isMemberOrderProperty(event.getProperty()
 		// ))
 		// {
 		// refreshViewer= true;
 		// }
 
 		if (refreshViewer) {
 			fViewer.refresh();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IViewPartInputProvider#getViewPartInput()
 	 */
 	public Object getViewPartInput() {
 		if (fViewer != null) {
 			return fViewer.getInput();
 		}
 		return null;
 	}
 
 	public void collapseAll() {
 		try {
 			fViewer.getControl().setRedraw(false);
 			fViewer.collapseToLevel(getViewPartInput(),
 					AbstractTreeViewer.ALL_LEVELS);
 		} finally {
 			fViewer.getControl().setRedraw(true);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.IShowInTarget#show(org.eclipse.ui.part.ShowInContext)
 	 */
 	public boolean show(ShowInContext context) {
 
 		ISelection selection = context.getSelection();
 		if (selection instanceof IStructuredSelection) {
 			// fix for 64634 Navigate/Show in/Package Explorer doesn't work
 			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
 			if (structuredSelection.size() == 1) {
 				int res = tryToReveal(structuredSelection.getFirstElement());
 				if (res == IStatus.OK) {
 					return true;
 				}
 				if (res == IStatus.CANCEL) {
 					return false;
 				}
 			} else if (structuredSelection.size() > 1) {
 				selectReveal(structuredSelection);
 				return true;
 			}
 		}
 
 		Object input = context.getInput();
 		if (input instanceof IEditorInput) {
 			Object elementOfInput = getInputFromEditor((IEditorInput) input);
 			return elementOfInput != null
 					&& (tryToReveal(elementOfInput) == IStatus.OK);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns the <code>IShowInSource</code> for this view.
 	 */
 	protected IShowInSource getShowInSource() {
 		return new IShowInSource() {
 			public ShowInContext getShowInContext() {
 				return new ShowInContext(getTreeViewer().getInput(),
 						getTreeViewer().getSelection());
 			}
 		};
 	}
 
 	public void setLinkingEnabled(boolean enabled) {
 		fLinkingEnabled = enabled;
 		saveDialogSettings();
 
 		IWorkbenchPage page = getSite().getPage();
 		if (enabled) {
 			page.addPartListener(fLinkWithEditorListener);
 
 			IEditorPart editor = page.getActiveEditor();
 			if (editor != null) {
 				editorActivated(editor);
 			}
 		} else {
 			page.removePartListener(fLinkWithEditorListener);
 		}
 	}
 
 	/**
 	 * Returns the name for the given element. Used as the name for the current
 	 * frame.
 	 */
 	String getFrameName(Object element) {
 		if (element instanceof IModelElement) {
 			return ((IModelElement) element).getElementName();
 		} else if (element instanceof WorkingSetModel) {
 			return ""; //$NON-NLS-1$
 		} else {
 			return fLabelProvider.getText(element);
 		}
 	}
 
 	public int tryToReveal(Object element) {
 		if (revealElementOrParent(element)) {
 			return IStatus.OK;
 		}
 
 		WorkingSetFilterActionGroup workingSetGroup = fActionSet
 				.getWorkingSetActionGroup().getFilterGroup();
 		if (workingSetGroup != null) {
 			IWorkingSet workingSet = workingSetGroup.getWorkingSet();
 			if (workingSetGroup.isFiltered(getVisibleParent(element), element)) {
 				String message;
 				if (element instanceof IModelElement) {
 					IDLTKUILanguageToolkit toolkit = DLTKUILanguageManager
 							.getLanguageToolkit((IModelElement) element);
 					ScriptElementLabels labels = toolkit
 							.getScriptElementLabels();
 					String elementLabel = labels.getElementLabel(
 							(IModelElement) element,
 							ScriptElementLabels.ALL_DEFAULT);
 					message = Messages
 							.format(ScriptMessages.PackageExplorer_notFound,
 									new String[] { elementLabel,
 											workingSet.getLabel() });
 				} else {
 					message = Messages.format(
 							ScriptMessages.PackageExplorer_notFound, workingSet
 									.getLabel());
 				}
 				if (MessageDialog.openQuestion(getSite().getShell(),
 						ScriptMessages.PackageExplorer_filteredDialog_title,
 						message)) {
 					workingSetGroup.setWorkingSet(null, true);
 					if (revealElementOrParent(element)) {
 						return IStatus.OK;
 					}
 				} else {
 					return IStatus.CANCEL;
 				}
 			}
 		}
 		// try to remove filters
 		CustomFiltersActionGroup filterGroup = fActionSet
 				.getCustomFilterActionGroup();
 		String[] currentFilters = filterGroup.internalGetEnabledFilterIds();
 		String[] newFilters = filterGroup.removeFiltersFor(
 				getVisibleParent(element), element, getTreeViewer()
 						.getContentProvider());
 		if (currentFilters.length > newFilters.length) {
 			String message;
 			if (element instanceof IModelElement) {
 				IDLTKUILanguageToolkit toolkit = DLTKUILanguageManager
 						.getLanguageToolkit((IModelElement) element);
 				ScriptElementLabels labels = toolkit.getScriptElementLabels();
 				String elementLabel = labels.getElementLabel(
 						(IModelElement) element,
 						ScriptElementLabels.ALL_DEFAULT);
 				message = Messages.format(
 						ScriptMessages.PackageExplorer_removeFilters,
 						elementLabel);
 			} else {
 				message = ScriptMessages.PackageExplorer_removeFilters;
 			}
 			if (MessageDialog.openQuestion(getSite().getShell(),
 					ScriptMessages.PackageExplorer_filteredDialog_title,
 					message)) {
 				filterGroup.setFilters(newFilters);
 				if (revealElementOrParent(element)) {
 					return IStatus.OK;
 				}
 			} else {
 				return IStatus.CANCEL;
 			}
 		}
 		FrameAction action = fActionSet.getUpAction();
 		while (action.getFrameList().getCurrentIndex() > 0) {
 			// only try to go up if there is a parent frame
 			// fix for bug# 63769 Endless loop after Show in Package Explorer
 			if (action.getFrameList().getSource().getFrame(
 					IFrameSource.PARENT_FRAME, 0) == null) {
 				break;
 			}
 			action.run();
 			if (revealElementOrParent(element)) {
 				return IStatus.OK;
 			}
 		}
 		return IStatus.ERROR;
 	}
 
 	private boolean revealElementOrParent(Object element) {
 		if (revealAndVerify(element)) {
 			return true;
 		}
 		element = getVisibleParent(element);
 		if (element != null) {
 			if (revealAndVerify(element)) {
 				return true;
 			}
 			if (element instanceof IModelElement) {
 				IResource resource = ((IModelElement) element).getResource();
 				if (resource != null) {
 					if (revealAndVerify(resource)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private Object getVisibleParent(Object object) {
 		// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=19104
 		if (object == null) {
 			return null;
 		}
 		if (!(object instanceof IModelElement)) {
 			return object;
 		}
 		IModelElement element2 = (IModelElement) object;
 		switch (element2.getElementType()) {
 		// case IModelElement.IMPORT_DECLARATION:
 		case IModelElement.TYPE:
 		case IModelElement.PACKAGE_DECLARATION:
 		case IModelElement.METHOD:
 		case IModelElement.FIELD:
 			// select parent cu/classfile
 			element2 = element2.getOpenable();
 			break;
 		case IModelElement.SCRIPT_MODEL:
 			element2 = null;
 			break;
 		}
 		return element2;
 	}
 
 	private boolean revealAndVerify(Object element) {
 		if (element == null) {
 			return false;
 		}
 		selectReveal(new StructuredSelection(element));
 		return !getSite().getSelectionProvider().getSelection().isEmpty();
 	}
 
 	public void rootModeChanged(int newMode) {
 		fRootMode = newMode;
 		saveDialogSettings();
 
 		if (getRootMode() == ScriptExplorerPart.WORKING_SETS_AS_ROOTS
 				&& fWorkingSetModel == null) {
 			createWorkingSetModel();
 			if (fActionSet != null) {
 				fActionSet.getWorkingSetActionGroup().setWorkingSetModel(
 						fWorkingSetModel);
 			}
 		}
 		IStructuredSelection selection = new StructuredSelection(
 				((IStructuredSelection) fViewer.getSelection()).toArray());
 		Object input = fViewer.getInput();
 		boolean isRootInputChange = DLTKCore.create(
 				ResourcesPlugin.getWorkspace().getRoot()).equals(input)
 				|| (fWorkingSetModel != null && fWorkingSetModel.equals(input))
 				|| input instanceof IWorkingSet;
 		try {
 			fViewer.getControl().setRedraw(false);
 			if (isRootInputChange) {
 				fViewer.setInput(null);
 			}
 			setProviders();
 			setComparator();
 			fActionSet.getWorkingSetActionGroup().fillFilters(fViewer);
 			if (isRootInputChange) {
 				fViewer.setInput(findInputElement());
 			}
 			fViewer.setSelection(selection, true);
 		} finally {
 			fViewer.getControl().setRedraw(true);
 		}
 		if (isRootInputChange
 				&& getRootMode() == ScriptExplorerPart.WORKING_SETS_AS_ROOTS
 				&& fWorkingSetModel.needsConfiguration()) {
 			ConfigureWorkingSetAction action = new ConfigureWorkingSetAction(
 					getSite());
 			action.setWorkingSetModel(fWorkingSetModel);
 			action.run();
 			fWorkingSetModel.configured();
 		}
 		setTitleToolTip(getTitleToolTip());
 	}
 
 	private void createWorkingSetModel() {
 		SafeRunner.run(new ISafeRunnable() {
 			public void run() throws Exception {
 				fWorkingSetModel = (fMemento != null) ? new WorkingSetModel(
 						fMemento) : new WorkingSetModel();
 			}
 
 			public void handleException(Throwable exception) {
 				fWorkingSetModel = new WorkingSetModel();
 			}
 		});
 	}
 
 	/**
 	 * @return the selected working set to filter if in root mode
 	 *         {@link #PROJECTS_AS_ROOTS}
 	 */
 	public IWorkingSet getFilterWorkingSet() {
 		if (getRootMode() != ScriptExplorerPart.PROJECTS_AS_ROOTS) {
 			return null;
 		}
 
 		if (fActionSet == null) {
 			return null;
 		}
 
 		return fActionSet.getWorkingSetActionGroup().getFilterGroup()
 				.getWorkingSet();
 	}
 
 	public WorkingSetModel getWorkingSetModel() {
 		return fWorkingSetModel;
 	}
 
 	/**
 	 * Returns the root mode: Either {@link #PROJECTS_AS_ROOTS} or
 	 * {@link #WORKING_SETS_AS_ROOTS}.
 	 * 
 	 * @return returns the root mode
 	 */
 	public int getRootMode() {
 		return fRootMode;
 	}
 
 	protected boolean showProjects() {
 		return fRootMode == ViewActionGroup.SHOW_PROJECTS;
 	}
 
 	protected boolean showWorkingSets() {
 		return fRootMode == ViewActionGroup.SHOW_WORKING_SETS;
 	}
 
 	protected void setComparator() {
 		if (showWorkingSets()) {
 			WorkingSetAwareModelElementSorter comparator = new WorkingSetAwareModelElementSorter();
 			comparator.setInnerElements(false);
 			fViewer.setComparator(comparator);
 		} else {
 			ModelElementSorter comparator = new ModelElementSorter();
 			comparator.setInnerElements(false);
 			fViewer.setComparator(comparator);
 		}
 	}
 
 	// ---- test methods for working set mode -------------------------------
 
 	public void internalTestShowWorkingSets(IWorkingSet[] workingSets) {
 		if (fWorkingSetModel == null) {
 			createWorkingSetModel();
 		}
 		fWorkingSetModel.setActiveWorkingSets(workingSets);
 		fWorkingSetModel.configured();
 		rootModeChanged(ScriptExplorerPart.WORKING_SETS_AS_ROOTS);
 	}
 }
