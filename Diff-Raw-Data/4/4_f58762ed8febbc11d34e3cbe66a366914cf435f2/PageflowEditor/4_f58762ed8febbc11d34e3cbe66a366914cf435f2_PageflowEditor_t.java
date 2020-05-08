 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.gef.ContextMenuProvider;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.KeyHandler;
 import org.eclipse.gef.KeyStroke;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CommandStackEventListener;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.gef.editparts.ScalableRootEditPart;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.requests.CreationFactory;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.DeleteAction;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.actions.RedoAction;
 import org.eclipse.gef.ui.actions.SelectionAction;
 import org.eclipse.gef.ui.actions.StackAction;
 import org.eclipse.gef.ui.actions.UndoAction;
 import org.eclipse.gef.ui.actions.UpdateAction;
 import org.eclipse.gef.ui.actions.ZoomInAction;
 import org.eclipse.gef.ui.actions.ZoomOutAction;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
 import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.util.TransferDropTargetListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jst.jsf.facesconfig.common.logging.Logger;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.action.AlignmentAction;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.action.OpenEditorAction;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.action.ShowPropertyViewAction;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.command.PreExecuteCommandStack;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.ConfigurableRootEditPart;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.IConnectionPreference;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.IFigurePreference;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.ILayerPanePreference;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.INodePreference;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.PageflowEditPartsFactory;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.PageflowNodeEditPart;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.Pageflow;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.synchronization.FC2PFTransformer;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.EditPartMarkerUtil;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowAnnotationUtil;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowModelManager;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowResourceFactory;
 import org.eclipse.jst.jsf.facesconfig.ui.preference.GEMPreferences;
 import org.eclipse.jst.jsf.facesconfig.ui.util.WebrootUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 
 /**
  * This the the main editor page for modifying a complete pageflow.
  */
 public class PageflowEditor extends GraphicalEditorWithFlyoutPalette implements
 		IAdaptable, IPropertyChangeListener, IGotoMarker {
 	/** log instance */
 	private static final Logger log = EditorPlugin
 			.getLogger(PageflowEditor.class);
 
 	/** pageflow context menu registration ID */
 	private static final String PAGEFLOW_CONTEXTMENU_REG_ID = ".pageflow.editor.contextmenu";
 
 	/** the edit domain */
 	private final DefaultEditDomain domain;
 
 	/** the palette root */
 	private PaletteRoot paletteRoot = null;
 
 	/** the parent multi-page editor */
 	private IEditorPart parentEditor = null;
 
 	/** the graphical viewer */
 	private GraphicalViewer viewer = null;
 
 	/** the undoable <code>IPropertySheetPage</code> */
 	private PropertySheetPage undoablePropertySheetPage = null;
 
 	/** the editor's action registry */
 	private ActionRegistry actionRegistry = null;
 
 	public static final String PAGE_ID = "org.eclipse.jst.jsf.facesconfig.ui.pageflow.PageflowEditor";
 
 	/** the list of action ids that are to EditPart actions */
 	private List editPartActionIDs = new ArrayList();
 
 	/** the selection listener */
 	private ISelectionListener selectionListener = new ISelectionListener() {
 		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 			updateActions();
 		}
 	};
 
 	/** the selection synchronizer for the edit part viewer */
 	private SelectionSynchronizer synchronizer = null;
 
 	/** the shared key handler */
 	private KeyHandler sharedKeyHandler = null;
 
 	/** pageflow model manager */
 	private PageflowModelManager pageflowManager;
 
 	/** the dirty status of this page */
 	private boolean isDirty = false;
 
 	/** the command stack of this page */
 	private CommandStack commandStack;
 
 	private FC2PFTransformer modelsTransform;
 
 	List stackActions = new ArrayList();
 
 	public FC2PFTransformer getModelsTransform() {
 		if (modelsTransform == null) {
 			modelsTransform = new FC2PFTransformer();
 		}
 		return modelsTransform;
 	}
 
 	public void updateActions() {
 		updateActions(stackActions);
 		updateActions(editPartActionIDs);
 	}
 
 	/**
 	 * This class listens for command stack changes of the page and decides if
 	 * the editor is dirty or not.
 	 * 
 	 */
 	private class PageCommandStackListener implements CommandStackEventListener {
 		public void stackChanged(CommandStackEvent event) {
 			if (((CommandStack) event.getSource()).isDirty()) {
 				// at least one command stack is dirty,
 				// so the multi page editor is dirty too
 				setDirty(true);
 			}
 			updateActions();
 		}
 	}
 
 	/**
 	 * This class listens to changes to the file system in the workspace, and
 	 * validate the current pageflow based on web files' status.
 	 */
 	private class ResourceTracker implements IResourceChangeListener,
 			IResourceDeltaVisitor {
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 		 */
 		public void resourceChanged(IResourceChangeEvent event) {
 			IResourceDelta delta = event.getDelta();
 			try {
 				if (delta != null) {
 					delta.accept(this);
 				}
 			} catch (CoreException exception) {
 				// Pageflow.PageflowEditor.Error.ResourceChange = Failed in the
 				// resource change.
 				log.error("Pageflow.PageflowEditor.Error.ResourceChange",
 						exception);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) {
 			// if the delta is not a file instance, just return true
 			if (!(delta.getResource() instanceof IFile)) {
 				return true;
 			}
 
 			// web file is changed.
 			if (WebrootUtil.isValidWebFile(((IFile) delta.getResource())
 					.getFullPath())) {
 				webPageChanged(((IFile) delta.getResource()).getFullPath());
 				return false;
 			}
 			return false;
 		}
 	}
 
 	/** the resource tracker instance */
 	private ResourceTracker resourceTracker = null;
 
 	/**
 	 * Returns the resource tracker instance
 	 * 
 	 * @return - Returns the resource tracker instance
 	 */
 	private ResourceTracker getResourceTracker() {
 		if (null == resourceTracker) {
 			resourceTracker = new ResourceTracker();
 		}
 		return resourceTracker;
 	}
 
 	/**
 	 * Changes the dirty state.
 	 * 
 	 * @param dirty -
 	 *            dirty state
 	 */
 	protected void setDirty(boolean dirty) {
 		if (isDirty != dirty) {
 			isDirty = dirty;
 		}
 	}
 
 	/**
 	 * Updates the specified actions.
 	 * 
 	 * @param actionIds -
 	 *            the list of ids of actions to update
 	 */
 	protected void updateActions(List actionIds) {
 		for (Iterator ids = actionIds.iterator(); ids.hasNext();) {
 			IAction action = getActionRegistry().getAction(ids.next());
 			if (null != action && action instanceof UpdateAction) {
 				((UpdateAction) action).update();
 			}
 		}
 	}
 
 	/**
 	 * Creates a new PageflowPage instance.
 	 * <p>
 	 * By design this page uses its own <code>EditDomain</code>. The main
 	 * goal of this approach is that this page has its own undo/redo command
 	 * stack.
 	 * 
 	 * @param parent -
 	 *            the parent multi page editor
 	 */
 	public PageflowEditor(IEditorPart parent) {
 		domain = new DefaultEditDomain(parent);
 		domain.setCommandStack(getCommandStack());
 		this.setEditDomain(domain);
 		parentEditor = parent;
 	}
 
 	/**
 	 * Adds an <code>CommandStack</code> action to this editor.
 	 * <p>
 	 * <code>CommandStack</code> actions are actions that depend and work on
 	 * the <code>CommandStack</code>.
 	 * 
 	 * @param action -
 	 *            the <code>CommandStack</code> action
 	 */
 	protected void addStackAction(StackAction action) {
 		getActionRegistry().registerAction(action);
 		stackActions.add(action.getId());
 	}
 
 	/**
 	 * Creates different kinds of actions and registers them to the
 	 * ActionRegistry.
 	 */
 	protected void createActions() {
 		// register delete action
 		addEditPartAction(new DeleteAction((IWorkbenchPart) this));
 		// register undo/redo action
 		addStackAction(new UndoAction(this));
 		addStackAction(new RedoAction(this));
 
 		// Allows opening of JSP files from the pageflow
 		addEditPartAction(new OpenEditorAction((IWorkbenchPart) this));
 
 		// Allows showing property view for the pageflow
 		SelectionAction action = new ShowPropertyViewAction(
 				(IWorkbenchPart) this);
 		action
 				.setImageDescriptor(getImageDescriptorForView("org.eclipse.ui.views.PropertySheet"));
 		addEditPartAction(action);
 		// Allows showing property view for the pageflow
 		// addEditPartAction(new ShowPaletteViewAction((IWorkbenchPart) this));
 
 		// register alignment actions
 		addEditPartAction(new AlignmentAction((IWorkbenchPart) this,
 				PositionConstants.LEFT));
 		addEditPartAction(new AlignmentAction((IWorkbenchPart) this,
 				PositionConstants.RIGHT));
 		addEditPartAction(new AlignmentAction((IWorkbenchPart) this,
 				PositionConstants.TOP));
 		addEditPartAction(new AlignmentAction((IWorkbenchPart) this,
 				PositionConstants.BOTTOM));
 		addEditPartAction(new AlignmentAction((IWorkbenchPart) this,
 				PositionConstants.CENTER));
 		addEditPartAction(new AlignmentAction((IWorkbenchPart) this,
 				PositionConstants.MIDDLE));
 
 		// register zoom in/out action
 		IAction zoomIn = new ZoomInAction(getZoomManager(getGraphicalViewer()));
 		IAction zoomOut = new ZoomOutAction(
 				getZoomManager(getGraphicalViewer()));
 		addAction(zoomIn);
 		addAction(zoomOut);
 
 		getSite().getKeyBindingService().registerAction(zoomIn);
 		getSite().getKeyBindingService().registerAction(zoomOut);
 	}
 
 	/**
 	 * Returns the zoom manager of the specified viewer.
 	 * 
 	 * @param viewer -
 	 *            the viewer to get the zoom manager from
 	 * @return - the zoom manager
 	 */
 	private ZoomManager getZoomManager(GraphicalViewer viewer) {
 		// get zoom manager from root edit part
 		RootEditPart rootEditPart = viewer.getRootEditPart();
 		ZoomManager zoomManager = null;
 		if (rootEditPart instanceof ScalableFreeformRootEditPart) {
 			zoomManager = ((ScalableFreeformRootEditPart) rootEditPart)
 					.getZoomManager();
 		} else if (rootEditPart instanceof ScalableRootEditPart) {
 			zoomManager = ((ScalableRootEditPart) rootEditPart)
 					.getZoomManager();
 		}
 		return zoomManager;
 	}
 
 	/**
 	 * Adds an action to this editor's <code>ActionRegistry</code>.
 	 * 
 	 * @param action -
 	 *            the action to add.
 	 */
 	protected void addAction(IAction action) {
 		getActionRegistry().registerAction(action);
 	}
 
 	/**
 	 * Adds an <code>EditPart</code> action to this editor.
 	 * <p>
 	 * <code>EditPart</code> actions are actions that depend and work on the
 	 * selected <code>EditPart</code>s.
 	 * 
 	 * @param action -
 	 *            the <code>EditPart</code> action
 	 */
 	protected void addEditPartAction(SelectionAction action) {
 		getActionRegistry().registerAction(action);
 		editPartActionIDs.add(action.getId());
 	}
 
 	/**
 	 * Returns the action registry of this editor.
 	 * 
 	 * @return - the action registry
 	 */
 	public ActionRegistry getActionRegistry() {
 		if (null == actionRegistry) {
 			actionRegistry = new ActionRegistry();
 		}
 
 		return actionRegistry;
 	}
 
 	/**
 	 * Creates the GraphicalViewer on the specified <code>Composite</code>.
 	 * 
 	 * @param parent -
 	 *            the parent composite
 	 */
 	public void createGraphicalViewer(Composite parent) {
 		viewer = new ScrollingGraphicalViewer();
 		viewer.createControl(parent);
 
 		// configure the viewer
 		viewer.getControl().setBackground(parent.getBackground());
 
 		viewer.setRootEditPart(new ConfigurableRootEditPart());
 		// _viewer.setRootEditPart(new ScalableFreeformRootEditPart());
 		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
 
 		// hook the viewer into the editor
 		registerEditPartViewer(viewer);
 
 		// configure the viewer with context menu and template drag and drop
 		configureEditPartViewer(viewer);
 
 		// initialize the viewer with input
 		viewer.setEditPartFactory(new PageflowEditPartsFactory());
 		// viewer.setContents(getPageflow());
 
 		// support the resource drag&drop
 		viewer
 				.addDropTargetListener((TransferDropTargetListener) new ResourceTransferDropTargetListener(
 						viewer, getParentEditor()) {
 					protected CreationFactory getFactory(Object obj) {
 						return getResourceFactory((IResource) obj);
 					}
 				});
 
 		// apply Editor's preferences
 		// propertyChange(null);
 		// add listener to Editor's preferences changing
 		EditorPlugin.getDefault().getPreferenceStore()
 				.addPropertyChangeListener(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see FlowEditor#getResourceFactory(IResource, ICodeGenEditor)
 	 */
 	protected CreationFactory getResourceFactory(IResource resource) {
 		return new PageflowResourceFactory(resource);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public final void doSave(IProgressMonitor monitor) {
 		// our policy: delegate saving to the parent
 		getParentEditor().doSave(monitor);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISaveablePart#doSaveAs()
 	 */
 	public final void doSaveAs() {
 		// our policy: delegate saving to the parent
 		getParentEditor().doSaveAs();
 	}
 
 	/**
 	 * Saves the pageflow under the specified path.
 	 * 
 	 * @param pageflow
 	 * @param path
 	 *            workspace relative path
 	 * @param progressMonitor
 	 */
 	public void doSave(IFile file, IProgressMonitor progressMonitor)
 			throws CoreException {
 		if (((FileEditorInput) getEditorInput()).getFile() != file) {
 			// TODO: save to other page.
 		}
 		if (null == progressMonitor) {
 			progressMonitor = new NullProgressMonitor();
 		}
 		// Pageflow.Label.Saving = Saving
 		progressMonitor.beginTask(PageflowMessages.Pageflow_Label_Saving + " "
 				+ file.getFullPath(), 2);
 
 		if (null == getPageflowManager()) {
 			// Pageflow.PageflowEditor.Alert.nullModelManager = No model manager
 			// found for saving the file.
 			EditorPlugin.getAlerts().throwCoreException(
 					"Pageflow.PageflowEditor.Alert.nullModelManager");
 		}
 
 		// save pageflow to file
 		try {
 			getPageflowManager().save(getPageflowFilePath(file));
 
 			progressMonitor.worked(1);
 			file.refreshLocal(IResource.DEPTH_ZERO, new SubProgressMonitor(
 					progressMonitor, 1));
 			progressMonitor.done();
 			setDirty(false);
 		} catch (FileNotFoundException e) {
 			// Pageflow.PageflowEditor.Alert.errorSaveFileInfo = The current
 			// pageflow model could not be saved.
 			EditorPlugin.getAlerts().throwCoreException(e);
 		} catch (IOException e) {
 			// Pageflow.PageflowEditor.Alert.errorSaveFileInfo = The current
 			// pageflow model could not be saved.
 			EditorPlugin.getAlerts().throwCoreException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IEditorPart#init(org.eclipse.ui.IEditorSite,
 	 *      org.eclipse.ui.IEditorInput)
 	 */
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		setSite(site);
 		setInput(input);
 		IFile fileFacesConfig = null;
 		try {
 			fileFacesConfig = ((FileEditorInput) input).getFile();
 
 			// load and validate pageflow
 			if (null == createPageflow(getPageflowFilePath(fileFacesConfig))) {
 				// Pageflow.PageflowEditor.Error.invalidPageflowFile = The
 				// specified input is not a valid pageflow.
 				log.error("Pageflow.PageflowEditor.Error.invalidPageflowFile");
 				throw new PartInitException(
 						EditorPlugin
 								.getResourceString("Pageflow.PageflowEditor.Error.invalidPageflowFile"));
 			}
 
 		} catch (CoreException e) {
 			// Pageflow.PageflowEditor.Error.invalidPageflowFile = The specified
 			// input is not a valid pageflow.
 			log.error("Pageflow.PageflowEditor.Error.invalidPageflowFile", e);
 			throw new PartInitException(e.getStatus());
 		} catch (IOException e) {
 			// Pageflow.PageflowEditor.Alert.errorSaveFileInfo = The current
 			// pageflow model could not be saved.
 			log.error("Pageflow.PageflowEditor.Alert.errorSaveFileInfo", e);
 		}
 
 		// add selection change listener
 		getSite().getWorkbenchWindow().getSelectionService()
 				.addSelectionListener(getSelectionListener());
 
 		// Add resource change listener
 		fileFacesConfig.getWorkspace().addResourceChangeListener(
 				getResourceTracker());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
 	 */
 	public void dispose() {
 		// remove selection change listener
 		getModelsTransform().dispose();
 		getSite().getWorkbenchWindow().getSelectionService()
 				.removeSelectionListener(getSelectionListener());
 
 		// remove listener to Editor's preferences changing
 		EditorPlugin.getDefault().getPreferenceStore()
 				.removePropertyChangeListener(this);
 
 		if (getEditorInput() != null) {
 			IFile file = (IFile) getEditorInput().getAdapter(IResource.class);
 			if (file != null) {
 				file.getWorkspace().removeResourceChangeListener(
 						getResourceTracker());
 			}
 		}
 		super.dispose();
 	}
 
 	/**
 	 * get the pageflow file path based on faces-config.xml file path
 	 * 
 	 * @return
 	 */
 	private IPath getPageflowFilePath(IFile file) {
 		IPath pageflowFilePath;
 		pageflowFilePath = PageflowModelManager.makePageflowPath(file
 				.getFullPath());
 		return pageflowFilePath;
 	}
 
 	/**
 	 * Returns the pageflow object from the specified file.
 	 * 
 	 * @param file -
 	 *            the file resource
 	 * @return -the pageflow object from the specified file
 	 * @throws IOException
 	 */
 	private Pageflow createPageflow(IPath pathPageflow) throws CoreException,
 			IOException {
 		Pageflow pageflow = null;
 
 		try {
 			getPageflowManager().load(pathPageflow);
 		} catch (Exception e) {
 			// Pageflow.PageflowEditor.Error.invalidPageflowFile = The specified
 			// input is not a valid pageflow.
 			// _log.error("Pageflow.PageflowEditor.Error.invalidPageflowFile",
 			// e);
 			getPageflowManager().createPageflow(pathPageflow);
 		}
 		IFile fileFacesConfig = ((FileEditorInput) getEditorInput()).getFile();
 		// it should update related config file
 		if (!fileFacesConfig.getFullPath().toString().trim().equalsIgnoreCase(
 				getPageflowManager().getModel().getConfigfile())) {
 			getPageflowManager().getModel().setConfigfile(
 					fileFacesConfig.getFullPath().toString());
 			getPageflowManager().save(pathPageflow);
 		}
 		pageflow = getPageflowManager().getModel();
 		if (null == pageflow) {
 			// Pageflow.PageflowEditor.Error.invalidPageflowModel = The model in
 			// the pageflow file is not a valid pageflow model.
 			log.error("Pageflow.PageflowEditor.Error.invalidPageflowModel");
 			EditorPlugin.getAlerts().throwCoreException(
 					"Pageflow.PageflowEditor.Error.invalidPageflowModel");
 		}
 		return pageflow;
 	}
 
 	/** get the pageflow manager for this page */
 	public PageflowModelManager getPageflowManager() {
 		if (pageflowManager == null) {
 			pageflowManager = new PageflowModelManager();
 		}
 		return pageflowManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISaveablePart#isDirty()
 	 */
 	public final boolean isDirty() {
 		return isDirty;
 	}
 
 	/**
 	 * Returns the <code>CommandStack</code> of this editor page.
 	 * 
 	 * @return - the <code>CommandStack</code> of this editor page
 	 */
 	public final CommandStack getCommandStack() {
 		if (commandStack == null) {
 			commandStack = new PreExecuteCommandStack();
 			commandStack
 					.addCommandStackEventListener(new PageCommandStackListener());
 		}
 		return commandStack;
 	}
 
 	/**
 	 * Returns the default <code>PaletteRoot</code> for this editor and all
 	 * its pages.
 	 * 
 	 * @return - the default <code>PaletteRoot</code>
 	 */
 	protected PaletteRoot getPaletteRoot() {
 		if (null == paletteRoot) {
 			// create root
 			paletteRoot = new PageflowPaletteRoot();
 		}
 		return paletteRoot;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISaveablePart#isSaveAsAllowed()
 	 */
 	public final boolean isSaveAsAllowed() {
 		// our policy: delegate saving to the parent
 		return getParentEditor().isSaveAsAllowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IWorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 		getGraphicalViewer().getControl().setFocus();
 	}
 
 	/**
 	 * Returns the multi page pageflow editor this editor page is contained in.
 	 * 
 	 * @return - the parent multi page editor
 	 */
 	protected final IEditorPart getParentEditor() {
 		return parentEditor;
 	}
 
 	/**
 	 * Returns the edit domain this editor page uses.
 	 * 
 	 * @return - the edit domain this editor page uses
 	 */
 	public final DefaultEditDomain getEditDomain() {
 		return domain;
 	}
 
 	/**
 	 * Hooks a <code>EditPartViewer</code> to the rest of the Editor.
 	 * <p>
 	 * By default, the viewer is added to the SelectionSynchronizer, which can
 	 * be used to keep 2 or more EditPartViewers in sync. The viewer is also
 	 * registered as the ISelectionProvider for the Editor's PartSite.
 	 * 
 	 * @param viewer -
 	 *            the viewer to hook into the editor
 	 */
 	protected void registerEditPartViewer(EditPartViewer viewer) {
 		// register viewer to edit domain
 		getEditDomain().addViewer(viewer);
 
 		// the multi page pageflow editor keeps track of synchronizing
 		getSelectionSynchronizer().addViewer(viewer);
 
 		// add viewer as selection provider
 		getSite().setSelectionProvider(viewer);
 	}
 
 	/**
 	 * Configures the specified <code>EditPartViewer</code> including context
 	 * menu, key handler, etc.
 	 * 
 	 * @param viewer -
 	 *            the pageflow graphical viewer.
 	 */
 	protected void configureEditPartViewer(EditPartViewer viewer) {
 		// configure the shared key handler
 		if (null != viewer.getKeyHandler()) {
 			viewer.getKeyHandler().setParent(getSharedKeyHandler());
 		}
 		// create the ActionRegistry
 		createActions();
 
 		// append the parent editor's action registry.
 		ActionRegistry actionRegistry = (ActionRegistry) getParentEditor()
 				.getAdapter(ActionRegistry.class);
 		if (actionRegistry != null) {
 			for (Iterator iter = actionRegistry.getActions(); iter.hasNext();) {
 				getActionRegistry().registerAction((IAction) iter.next());
 			}
 		}
 		// configure and register the context menu
 		ContextMenuProvider provider = new PageflowEditorContextMenuProvider(
 				viewer, getActionRegistry());
 		viewer.setContextMenu(provider);
 		getSite().registerContextMenu(
 				EditorPlugin.getPluginId() + PAGEFLOW_CONTEXTMENU_REG_ID,
 				provider, getSite().getSelectionProvider()); //$NON-NLS-1$
 
 		// enable viewer as drop target for template transfers
 		viewer
 				.addDropTargetListener((TransferDropTargetListener) new PageflowTemplateTransferDropTargetListener(
 						viewer));
 
 	}
 
 	/**
 	 * Returns the pageflow that is edited.
 	 * 
 	 * @return - the pageflow that is edited
 	 */
 	public Pageflow getPageflow() {
 		return getPageflowManager().getModel();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see AbstractEditorPage#getGraphicalViewerForZoomSupport()
 	 */
 	public GraphicalViewer getGraphicalViewer() {
 		return viewer;
 	}
 
 	public void setGraphicalViewerContents(Object contents) {
 		viewer.setContents(contents);
 		propertyChange(null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IAdaptable#getAdapter(Class)
 	 */
 	public Object getAdapter(Class type) {
 		if (type == IContentOutlinePage.class) {
 			return getOutlinePage();
 		} else if (type == CommandStack.class) {
 			return getCommandStack();
 		} else if (type == ActionRegistry.class) {
 			return getActionRegistry();
 		} else if (type == IPropertySheetPage.class) {
 			return getPropertySheetPage();
 		} else if (type == ZoomManager.class) {
 			return getZoomManager(getGraphicalViewer());
 		}
 		return super.getAdapter(type);
 	}
 
 	/**
 	 * Returns the outline page for the outline view with lazy creation
 	 * 
 	 * @return - the outline page
 	 */
 	protected PageflowEditorOutlinePage getOutlinePage() {
 		PageflowEditorOutlinePage outlinePage = new PageflowEditorOutlinePage(
 				this);
 		outlinePage.initialize(this);
 		return outlinePage;
 	}
 
 	/**
 	 * Returns the undoable <code>PropertySheetPage</code> for this editor.
 	 * 
 	 * @return - the undoable <code>PropertySheetPage</code>
 	 */
 	protected IPropertySheetPage getPropertySheetPage() {
 		if (null == undoablePropertySheetPage) {
 			undoablePropertySheetPage = new PropertySheetPage();
 
 			/** set the property source for property sheet page */
 			undoablePropertySheetPage
 					.setRootEntry(new org.eclipse.gef.ui.properties.UndoablePropertySheetEntry(
 							(CommandStack) getAdapter(CommandStack.class)));
 
 		}
 
 		return undoablePropertySheetPage;
 	}
 
 	/**
 	 * Returns the selection syncronizer object. The synchronizer can be used to
 	 * sync the selection of 2 or more EditPartViewers.
 	 * 
 	 * @return - the syncrhonizer
 	 */
 	protected SelectionSynchronizer getSelectionSynchronizer() {
 		if (null == synchronizer) {
 			synchronizer = new SelectionSynchronizer();
 		}
 		return synchronizer;
 	}
 
 	/**
 	 * Returns the shared KeyHandler that should be used for all viewers.
 	 * 
 	 * @return - the shared KeyHandler
 	 */
 	protected KeyHandler getSharedKeyHandler() {
 		if (null == sharedKeyHandler) {
 			sharedKeyHandler = new KeyHandler();
 
 			// configure common keys for all viewers
 			sharedKeyHandler
 					.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
 							getActionRegistry().getAction(
 									ActionFactory.DELETE.getId()));
 			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
 					getActionRegistry().getAction(
 							GEFActionConstants.DIRECT_EDIT));
 		}
 		return sharedKeyHandler;
 	}
 
 	/**
 	 * Returns the selection listener.
 	 * 
 	 * @return - the <code>ISelectionListener</code>
 	 */
 	protected ISelectionListener getSelectionListener() {
 		return selectionListener;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		String property = (event == null) ? null : event.getProperty();
 
 		propagateProperty(property, viewer.getRootEditPart());
 	}
 
 	/**
 	 * propagate property change to children edit part
 	 * 
 	 * @param property -
 	 *            property's string name
 	 * @param part -
 	 *            parent edit part.
 	 */
 	private void propagateProperty(String property, EditPart part) {
 		processPropertyChange(property, part);
 
 		if (part instanceof GraphicalEditPart) {
 			// get the connections edit part
 			Iterator iterConns = ((GraphicalEditPart) part)
 					.getSourceConnections().iterator();
 			while (iterConns.hasNext()) {
 				EditPart child = (EditPart) iterConns.next();
 				propagateProperty(property, child);
 			}
 		}
 		Iterator iter = part.getChildren().iterator();
 		while (iter.hasNext()) {
 			EditPart child = (EditPart) iter.next();
 			propagateProperty(property, child);
 		}
 	}
 
 	/**
 	 * process the property change FIXME: The property change should be category
 	 * to improve the performance.
 	 * 
 	 * @param property -
 	 *            property's string name
 	 * @param part-EditPart
 	 */
 	private void processPropertyChange(String property, EditPart part) {
 		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
 
 		if (property != null
 				&& property.equals(GEMPreferences.USE_SYSTEM_COLORS)) {
 			// reload all properties - it's easiest
 			property = null;
 		}
 
 		if (property == null || GEMPreferences.SNAP_TO_GRID.equals(property)) {
 			boolean bSnapToGrid = store.getBoolean(GEMPreferences.SNAP_TO_GRID);
 			if (part instanceof ILayerPanePreference) {
 				((ILayerPanePreference) part).setGridVisible(bSnapToGrid);
 			}
 		}
 
 		if (property == null || GEMPreferences.GRID_WIDTH.equals(property)
 				|| GEMPreferences.GRID_HEIGHT.equals(property)) {
 			Dimension gridSpacing = new Dimension(store
 					.getInt(GEMPreferences.GRID_WIDTH), store
 					.getInt(GEMPreferences.GRID_HEIGHT));
 			if (part instanceof ILayerPanePreference) {
 				((ILayerPanePreference) part).setGridSpacing(gridSpacing);
 			}
 		}
 
 		if (property == null || GEMPreferences.GRID_COLOR.equals(property)) {
 			Color gridFgColor = GEMPreferences.getColor(store,
 					GEMPreferences.GRID_COLOR);
 			if (part instanceof ILayerPanePreference) {
 				((ILayerPanePreference) part)
 						.setGridForegroundColor(gridFgColor);
 			}
 		}
 
 		if (property == null || GEMPreferences.CANVAS_COLOR.equals(property)) {
 			Color containerBgColor = GEMPreferences.getColor(store,
 					GEMPreferences.CANVAS_COLOR);
 			if (part instanceof IFigurePreference) {
 				((IFigurePreference) part).setBackgroundColor(containerBgColor);
 			}
 		}
 
 		if (property == null || GEMPreferences.LINE_WIDTH.equals(property)) {
 			int linkLineWidth = store.getInt(GEMPreferences.LINE_WIDTH);
 
 			if (part instanceof IConnectionPreference) {
 				((IConnectionPreference) part).setLineWidth(linkLineWidth);
 			}
 		}
 
 		if (property == null || GEMPreferences.LINE_COLOR.equals(property)) {
 			Color linkLineColor = GEMPreferences.getColor(store,
 					GEMPreferences.LINE_COLOR);
 			if (part instanceof IConnectionPreference) {
 				((IConnectionPreference) part)
 						.setForegroundColor(linkLineColor);
 			}
 		}
 
 		if (property == null
 				|| GEMPreferences.SHOW_LINE_LABELS.equals(property)) {
 			boolean bLinkLabelVisible = store
 					.getBoolean(GEMPreferences.SHOW_LINE_LABELS);
 			if (part instanceof IConnectionPreference) {
 				((IConnectionPreference) part)
 						.setLabelVisible(bLinkLabelVisible);
 			}
 		}
 
 		if (property == null || GEMPreferences.LINE_LABEL_FONT.equals(property)
 				|| GEMPreferences.LINE_LABEL_FONT_COLOR.equals(property)) {
 			Font linkLabelFont = getLinkLabelFont();
 			Color linkLabelFgColor = GEMPreferences.getColor(store,
 					GEMPreferences.LINE_LABEL_FONT_COLOR);
 			if (part instanceof IConnectionPreference) {
 				((IConnectionPreference) part).setFont(linkLabelFont);
 				((IConnectionPreference) part)
 						.setLabelForegroundColor(linkLabelFgColor);
 			}
 		}
 
 		if (property == null
 				|| GEMPreferences.LINE_LABEL_COLOR.equals(property)) {
 			Color linkLabelBgColor = GEMPreferences.getColor(store,
 					GEMPreferences.LINE_LABEL_COLOR);
 			if (part instanceof IConnectionPreference) {
 				((IConnectionPreference) part)
 						.setLabelBackgroundColor(linkLabelBgColor);
 			}
 		}
 
 		if (property == null || GEMPreferences.LINE_ROUTING.equals(property)) {
 			String connectionStyle = store
 					.getString(GEMPreferences.LINE_ROUTING);
 			int style;
 			if (GEMPreferences.LINE_ROUTING_MANHATTAN.equals(connectionStyle)) {
 				style = ILayerPanePreference.LINE_ROUTING_MANHATTAN;
 			} else {
 				style = ILayerPanePreference.LINE_ROUTING_MANUAL;
 			}
 
 			if (part instanceof ILayerPanePreference) {
 				((ILayerPanePreference) part).setConnectionRouterStyle(style);
 			} else if (part instanceof IConnectionPreference) {
 				((IConnectionPreference) part).setConnectionRouterStyle(style);
 			}
 		}
 
 		if (property == null
 				|| GEMPreferences.FIGURE_LABEL_FONT.equals(property)
 				|| GEMPreferences.FIGURE_LABEL_FONT_COLOR.equals(property)) {
 			Font nodeLabelFont = getNodeLabelFont();
 			Color nodeLabelFgColor = GEMPreferences.getColor(store,
 					GEMPreferences.FIGURE_LABEL_FONT_COLOR);
 
 			if (part instanceof INodePreference) {
 				((INodePreference) part).setFont(nodeLabelFont);
 				((INodePreference) part).setForegroundColor(nodeLabelFgColor);
 			}
 		}
 
 		if (property == null || GEMPreferences.LABEL_PLACEMENT.equals(property)) {
 			int placement = PositionConstants.SOUTH;
 			String nodeLabelPlacement = store
 					.getString(GEMPreferences.LABEL_PLACEMENT);
 			if (GEMPreferences.LABEL_PLACEMENT_TOP.equals(nodeLabelPlacement))
 				placement = PositionConstants.NORTH;
 			else if (GEMPreferences.LABEL_PLACEMENT_BOTTOM
 					.equals(nodeLabelPlacement))
 				placement = PositionConstants.SOUTH;
 			else if (GEMPreferences.LABEL_PLACEMENT_LEFT
 					.equals(nodeLabelPlacement))
 				placement = PositionConstants.WEST;
 			else if (GEMPreferences.LABEL_PLACEMENT_RIGHT
 					.equals(nodeLabelPlacement))
 				placement = PositionConstants.EAST;
 			if (part instanceof INodePreference)
 				((INodePreference) part).setTextPlacement(placement);
 		}
 	}
 
 	private Font getLinkLabelFont() {
 		FontRegistry registry = JFaceResources.getFontRegistry();
 		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
 		FontData fontData = PreferenceConverter.getFontData(store,
 				GEMPreferences.LINE_LABEL_FONT);
 		if (!registry.get(fontData.toString()).equals(registry.defaultFont()))
 			return registry.get(fontData.toString());
 		
 		registry.put(fontData.toString(), new FontData[] {fontData});
 		return registry.get(fontData.toString());
 	}
 
 	private Font getNodeLabelFont() {
 		FontRegistry registry = JFaceResources.getFontRegistry();
 		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
 		FontData fontData = PreferenceConverter.getFontData(store,
 				GEMPreferences.FIGURE_LABEL_FONT);
 		if (!registry.get(fontData.toString()).equals(registry.defaultFont()))
 			return registry.get(fontData.toString());
 		
 		registry.put(fontData.toString(), new FontData[] {fontData});
 		return registry.get(fontData.toString());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.ide.IGotoMarker#gotoMarker(org.eclipse.core.resources.IMarker)
 	 */
 	public void gotoMarker(IMarker marker) {
 		// The LOCATION attribute in the marker should be the ID string
 		Object id = null;
 		try {
 			id = marker.getAttribute(IMarker.LOCATION);
 		} catch (CoreException e) {
 			// Pageflow.PageflowEditor.Error.invalidMarkerAttribute = Unable to
 			// get marker's attribute
 			log
 					.error(
 							"Pageflow.PageflowEditor.Error.invalidMarkerAttribute",
 							e);
 		}
 		if (id instanceof String) {
 			GraphicalEditPart part = EditPartMarkerUtil.findEditPart(
 					(GraphicalEditPart) getGraphicalViewer().getRootEditPart(),
 					(String) id);
 			if (part != null) {
 				getGraphicalViewer().reveal(part);
 				getGraphicalViewer().select(part);
 				return;
 			}
 		}
 
 	}
 
 	/**
 	 * the related web page is changed in outside editor, the pageflow should be
 	 * revalidated to update the validation icons
 	 * 
 	 * @param fullPath
 	 */
 	public void webPageChanged(IPath fullPath) {
 		PageflowPage page = getPageflowManager().foundPage(
 				WebrootUtil.getWebPath(fullPath));
 
 		if (page != null && getGraphicalViewer() != null
 				&& getGraphicalViewer().getRootEditPart() != null) {
 			GraphicalEditPart pagePart = EditPartMarkerUtil.findEditPart(
 					(GraphicalEditPart) getGraphicalViewer().getRootEditPart(),
 					page.getId());
 			PageflowAnnotationUtil
 					.validatePage((PageflowNodeEditPart) pagePart);
 		}
 	}
 
 	/**
 	 * Get the image desriptor from the view's id.
 	 * 
 	 * @param viewid
 	 * @return
 	 */
 	private ImageDescriptor getImageDescriptorForView(String viewid) {
 		IConfigurationElement[] elements = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor("org.eclipse.ui.views");
 		for (int i = 0; i < elements.length; i++) {
 			String name = elements[i].getName();
 			String id = elements[i].getAttribute("id");
 			if ("view".equals(name) && viewid.equals(id)) {
 				String iconPath = elements[i].getAttribute("icon");
 				if (iconPath != null) {
 					return AbstractUIPlugin.imageDescriptorFromPlugin(
 							elements[i].getDeclaringExtension().getNamespace(),
 							iconPath);
 				}
 			}
 		}
 		return null;
 	}
}
