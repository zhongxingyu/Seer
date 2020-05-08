 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 329523 - Add notification of DiagramTypeProvider after saving a diagram
 *    mwenz - Bug 347152 - Do not log diagnostics errors as errors in the Eclipse error log
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.editor;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.IOperationHistoryListener;
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.OperationHistoryEvent;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.ui.MarkerHelper;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.workspace.IWorkspaceCommandStack;
 import org.eclipse.emf.workspace.ResourceUndoContext;
 import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
 import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
 import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.T;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.ISaveablePart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class DiagramEditorBehavior extends PlatformObject implements IEditingDomainProvider, IOperationHistoryListener {
 
 	/**
 	 * The part this model editor works on.
 	 * 
 	 * @see {@link DiagramEditorBehavior#DiagramEditorBehavior(IEditorPart)}
 	 */
 	private IEditorPart editorPart = null;
 
 	/**
 	 * Keeps track of the editing domain that is used to track all changes to
 	 * the model.
 	 */
 	private TransactionalEditingDomain editingDomain;
 
 	/**
 	 * Closes editor if model object is deleted.
 	 */
 	private ElementDeleteListener elementDeleteListener = null;
 
 	/**
 	 * Is responsible for creating workspace resource markers presented in
 	 * Eclipse's Problems View.
 	 */
 	private final MarkerHelper markerHelper = new EditUIMarkerHelper();
 
 	/**
 	 * Map to store the diagnostic associated with a resource.
 	 */
 	private final Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
 
 	/**
 	 * Controls whether the problem indication should be updated.
 	 */
 	private boolean updateProblemIndication = true;
 
 	/**
 	 * If a dirtyStateUpdater is provided with
 	 * {@link DiagramEditorBehavior#init(IEditorSite, IEditorInput, Runnable)},
 	 * it will be registered such that it toggles the editor's dirty state.
 	 */
 	private Runnable dirtyStateUpdater;
 
 	/**
 	 * The update adapter is added to every {@link Resource} adapters in the
 	 * {@link ResourceSet} of the {@link TransactionalEditingDomain}. When
 	 * notified, it adds an {@link DiagramEditorBehavior#updateAdapter} to the
 	 * adapters of the ResourceSet.
 	 * 
 	 * @see DiagramEditorBehavior#initializeEditingDomain(TransactionalEditingDomain)
 	 */
 	private ResourceSetUpdateAdapter resourceSetUpdateAdapter;
 
 	/**
 	 * Is toggled by {@link DiagramEditorBehavior#updateAdapter}.
 	 */
 	protected boolean resourceDeleted = false;
 
 	/**
 	 * @return the resourceDeleted
 	 */
 	public boolean isResourceDeleted() {
 		return resourceDeleted;
 	}
 
 	/**
 	 * @param resourceDeleted
 	 *            the resourceDeleted to set
 	 */
 	public void setResourceDeleted(boolean resourceDeleted) {
 		this.resourceDeleted = resourceDeleted;
 	}
 
 	/**
 	 * Is toggled by {@link DiagramEditorBehavior#updateAdapter}.
 	 */
 	private boolean resourceChanged = false;
 
 	/**
 	 * @return the resourceChanged
 	 */
 	public boolean isResourceChanged() {
 		return resourceChanged;
 	}
 
 	/**
 	 * @param resourceChanged
 	 *            the resourceChanged to set
 	 */
 	public void setResourceChanged(boolean resourceChanged) {
 		this.resourceChanged = resourceChanged;
 	}
 
 	// TODO, FIXME: 
 	// check if the Synchronizer really works for inter-EditingDomain-eventing
 	// there are some concerns out there regarding WorkspaceSynchronizers, @see
 	// http://www.eclipse.org/forums/index.php?t=msg&th=140242&start=0&
 	// Also EMF group experienced deadlocks caused by the synchronizer.
 	private WorkspaceSynchronizer workspaceSynchronizer;
 
 	/**
 	 * Creates a model editor responsible for the given {@link IEditorPart}.
 	 * 
 	 * @param editorPart
 	 *            the part this model editor works on
 	 */
 	public DiagramEditorBehavior(IEditorPart editorPart) {
 		super();
 		this.editorPart = editorPart;
 	}
 
 	/**
 	 * This sets up the editing domain for this model editor.
 	 * 
 	 * @param domain
 	 *            The {@link TransactionalEditingDomain} that is used within
 	 *            this model editor
 	 */
 	private void initializeEditingDomain(TransactionalEditingDomain domain) {
 		editingDomain = domain;
 		final ResourceSet resourceSet = domain.getResourceSet();
 
 		resourceSetUpdateAdapter = new ResourceSetUpdateAdapter();
 		resourceSet.eAdapters().add(resourceSetUpdateAdapter);
 
 		for (final Resource r : resourceSet.getResources()) {
 			r.eAdapters().add(updateAdapter);
 		}
 
 		// Install synchronizer for editor-external changes to the files underlying the resources of the ED
 		workspaceSynchronizer = new WorkspaceSynchronizer(getEditingDomain(), new DomainModelWorkspaceSynchronizerDelegate(this));
 	}
 
 	/**
 	 * Adapter used to update the problem indication when resources are demanded
 	 * loaded.
 	 */
 	private final EContentAdapter problemIndicationAdapter = new EContentAdapter() {
 		@Override
 		public void notifyChanged(Notification notification) {
 			if (notification.getNotifier() instanceof Resource) {
 				switch (notification.getFeatureID(Resource.class)) {
 				case Resource.RESOURCE__IS_LOADED:
 				case Resource.RESOURCE__ERRORS:
 				case Resource.RESOURCE__WARNINGS: {
 					final Resource resource = (Resource) notification.getNotifier();
 					final Diagnostic diagnostic = analyzeResourceProblems(resource, null);
 					if (diagnostic.getSeverity() != Diagnostic.OK) {
 						resourceToDiagnosticMap.put(resource, diagnostic);
 					} else {
 						resourceToDiagnosticMap.remove(resource);
 					}
 
 					if (updateProblemIndication) {
 						getShell().getDisplay().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								updateProblemIndication();
 							}
 						});
 					}
 					break;
 				}
 				}
 			} else {
 				super.notifyChanged(notification);
 			}
 		}
 
 		@Override
 		protected void setTarget(Resource target) {
 			basicSetTarget(target);
 		}
 
 		@Override
 		protected void unsetTarget(Resource target) {
 			basicUnsetTarget(target);
 		}
 	};
 
 	private Shell getShell() {
 		return editorPart.getSite().getShell();
 	}
 
 	private final Adapter updateAdapter = new AdapterImpl() {
 		@Override
 		public void notifyChanged(Notification msg) {
 			if (!isAdapterActive())
 				return;
 			if (msg.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED) {
 				if (msg.getNewBooleanValue() == Boolean.FALSE) {
 					final Resource resource = (Resource) msg.getNotifier();
 					final URI uri = resource.getURI();
 					if (editingDomain.getResourceSet().getURIConverter().exists(uri, null)) {
 						// file content has changes
 						setResourceChanged(true);
 						final IEditorPart activeEditor = editorPart.getSite().getPage().getActiveEditor();
 						if (activeEditor == editorPart) {
 							getShell().getDisplay().asyncExec(new Runnable() {
 								@Override
 								public void run() {
 									handleActivate();
 								}
 							});
 						}
 					} else {
 						// file has been deleted
 						if (!isDirty()) {
 							final IEditorInput editorInput = editorPart.getEditorInput();
 							if (editorInput instanceof DiagramEditorInput) {
 								final DiagramEditorInput input = (DiagramEditorInput) editorInput;
 								EObject eObject = null;
 								try {
 									eObject = input.getEObject();
 								} catch (final Exception e) {
 									// Exception getting object --> object probably deleted
 									startCloseEditorJob();
 								}
 								if (eObject != null) {
 									final Resource eResource = eObject.eResource();
 									if (eResource == null || eResource.equals(resource)) {
 										startCloseEditorJob();
 									}
 								}
 							}
 						} else {
 							setResourceDeleted(true);
 							final IEditorPart activeEditor = editorPart.getSite().getPage().getActiveEditor();
 							if (activeEditor == editorPart) {
 								getShell().getDisplay().asyncExec(new Runnable() {
 									@Override
 									public void run() {
 										handleActivate();
 									}
 								});
 							}
 						}
 					}
 				}
 			}
 			super.notifyChanged(msg);
 		}
 
 		private void startCloseEditorJob() {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					closeEd();
 				}
 			});
 		}
 	};
 
 	private void closeEd() {
 		IWorkbenchPartSite site = editorPart.getSite();
 		// Since we run async we have to check if our ui is still there.
 		if (site == null)
 			return;
 		IWorkbenchPage page = site.getPage();
 		if (page == null)
 			return;
 		page.closeEditor(editorPart, false);
 	}
 
 	private boolean adapterActive;
 
 	/**
 	 * Handles activation of the editor.
 	 */
 	private void handleActivate() {
 		if (isResourceDeleted()) {
 			if (handleDirtyConflict()) {
 				closeEd();
 			} else {
 				setResourceDeleted(false);
 				setResourceChanged(false);
 			}
 		} else if (isResourceChanged()) {
 			handleChangedResources();
 			setResourceChanged(false);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	protected boolean isAdapterActive() {
 		return adapterActive;
 	}
 
 	private void setAdapterActive(boolean b) {
 		adapterActive = b;
 	}
 
 	/**
 	 * Handles what to do with changed resources on activation.
 	 */
 	private void handleChangedResources() {
 		if (!isDirty() || handleDirtyConflict()) {
 			getOperationHistory().dispose(getUndoContext(), true, true, true);
 
 			updateProblemIndication = false;
 
 			//Disable adapter temporarily.
 			setAdapterActive(false);
 			try {
 				// We unload our resources such that refreshEditorContent does a complete diagram refresh.
 				EList<Resource> resources = getEditingDomain().getResourceSet().getResources();
 				for (Resource resource : resources) {
 					resource.unload();
 				}
 				refreshEditorContent();
 			} finally {
 				setAdapterActive(true);
 			}
 			updateProblemIndication = true;
 			updateProblemIndication();
 		}
 	}
 
 	/**
 	 * Updates the problems indication with the information described in the
 	 * specified diagnostic.
 	 */
 	private void updateProblemIndication() {
 		if (this.updateProblemIndication && editingDomain != null) {
 			final BasicDiagnostic diagnostic = new BasicDiagnostic(Diagnostic.OK, GraphitiUIPlugin.PLUGIN_ID, 0, null,
 					new Object[] { editingDomain.getResourceSet() });
 			for (final Diagnostic childDiagnostic : resourceToDiagnosticMap.values()) {
 				if (childDiagnostic.getSeverity() != Diagnostic.OK) {
 					diagnostic.add(childDiagnostic);
 				}
 			}
 			if (markerHelper.hasMarkers(editingDomain.getResourceSet())) {
 				markerHelper.deleteMarkers(editingDomain.getResourceSet());
 			}
 			if (diagnostic.getSeverity() != Diagnostic.OK) {
 				try {
 					markerHelper.createMarkers(diagnostic);
					T.racer().info(diagnostic.toString());
 				} catch (final CoreException exception) {
 					T.racer().error(exception.getMessage(), exception);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Shows a dialog that asks if conflicting changes should be discarded.
 	 */
 	private boolean handleDirtyConflict() {
 		return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				Messages.DiscardChangesDialog_0_xmsg, Messages.DiscardChangesDialog_1_xmsg);
 	}
 
 	/**
 	 * This returns the editing domain as required by the
 	 * {@link IEditingDomainProvider} interface.
 	 * 
 	 * @return The {@link TransactionalEditingDomain} that is used within this
 	 *         editor
 	 */
 	@Override
 	public TransactionalEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * Returns a diagnostic describing the errors and warnings listed in the
 	 * resource and the specified exception (if any).
 	 */
 	public Diagnostic analyzeResourceProblems(Resource resource, Exception exception) {
 		if ((!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) && editingDomain != null) {
 			final IFile file = GraphitiUiInternal.getEmfService().getFile(resource.getURI(), editingDomain);
 			final String fileName = file != null ? file.getFullPath().toString() : "unknown name"; //$NON-NLS-1$
 			final BasicDiagnostic basicDiagnostic = new BasicDiagnostic(Diagnostic.ERROR, GraphitiUIPlugin.PLUGIN_ID, 0,
 					"Problems encountered in file " + fileName, new Object[] { exception == null ? (Object) resource : exception }); //$NON-NLS-1$
 			basicDiagnostic.merge(EcoreUtil.computeDiagnostic(resource, true));
 			return basicDiagnostic;
 		} else if (exception != null) {
 			return new BasicDiagnostic(Diagnostic.ERROR, GraphitiUIPlugin.PLUGIN_ID, 0, "Problems encountered in file", //$NON-NLS-1$ 
 					new Object[] { exception });
 		} else {
 			return Diagnostic.OK_INSTANCE;
 		}
 	}
 
 	/**
 	 * Answers whether this editor is dirty by querying the command stack of its
 	 * editing domain for changes that are not yet persisted.
 	 * 
 	 * @return the editor's dirty state
 	 * @see ISaveablePart#isDirty()
 	 */
 	public boolean isDirty() {
 		if (editingDomain != null && editingDomain.getCommandStack() != null) { // not disposed yet
 			return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 		}
 		return false;
 	}
 
 	/**
 	 * This is for implementing {@link IEditorPart} and simply saves the model
 	 * file.
 	 * 
 	 * @param progressMonitor
 	 *            The {@link IProgressMonitor} progress monitor
 	 */
 	public Resource[] doSave(IProgressMonitor progressMonitor) {
 		// Save only resources that have actually changed.
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 		final Set<Resource> savedResources = new HashSet<Resource>();
 
 		// Do the work within an operation because this is a long running activity that modifies the workbench.
 		final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			// This is the method that gets invoked when the operation runs.
 			@Override
 			public void execute(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				try {
 					savedResources.addAll(GraphitiUiInternal.getEmfService().save(editingDomain));
 				} catch (final WrappedException e) {
 					final MultiStatus errorStatus = new MultiStatus(GraphitiUIPlugin.PLUGIN_ID, 0, e.getMessage(), e.exception());
 					GraphitiUIPlugin.getDefault().getLog().log(errorStatus);
 					T.racer().error(e.getMessage(), e.exception());
 				}
 			}
 		};
 
 		this.updateProblemIndication = false;
 		try {
 			// This runs the options, and shows progress.
 			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, operation);
 
 			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
 			// Refresh the necessary state.
 			if (dirtyStateUpdater != null)
 				this.dirtyStateUpdater.run();
 			// commandStack.notifyListeners(); // EMFMIG needed?
 		} catch (final Exception exception) {
 			// Something went wrong that shouldn't.
 			T.racer().error(exception.getMessage(), exception);
 		}
 		this.updateProblemIndication = true;
 		updateProblemIndication();
 
 		return savedResources.toArray(new Resource[savedResources.size()]);
 	}
 
 	private IOperationHistory getOperationHistory() {
 		IOperationHistory history = null;
 		final TransactionalEditingDomain domain = getEditingDomain();
 		if (domain != null) {
 			final IWorkspaceCommandStack commandStack = (IWorkspaceCommandStack) getEditingDomain().getCommandStack();
 			if (commandStack != null) {
 				history = commandStack.getOperationHistory();
 			}
 		}
 		return history;
 	}
 
 	/**
 	 * Initialises this editor with the given editor site and input. If a
 	 * dirtyStateUpdater is provided, it will be registered such that it toggles
 	 * the editor's dirty state.
 	 * 
 	 * @param site
 	 *            the editor site
 	 * @param input
 	 *            the editor's input, preferably a {@link DiagramEditorInput}
 	 */
 	public void init(IEditorSite site, IEditorInput editorInput) {
 		init(site, editorInput, null);
 	}
 
 	/**
 	 * Initialises this editor with the given editor site and input. If a
 	 * dirtyStateUpdater is provided, it will be registered such that it toggles
 	 * the editor's dirty state.
 	 * 
 	 * @param site
 	 *            the editor site
 	 * @param input
 	 *            the editor's input, preferably a {@link DiagramEditorInput}
 	 * @param dirtyStateUpdater
 	 *            an optional operation for toggling the dirty state, which is
 	 *            called at the appropriate time. Its implementation should
 	 *            contain a call
 	 *            <code>firePropertyChange(IEditorPart.PROP_DIRTY)</code>.
 	 * @throws PartInitException
 	 *             if the initialisation fails
 	 */
 	/**
 	 * @param site
 	 * @param editorInput
 	 * @param dirtyStateUpdater
 	 */
 	public void init(IEditorSite site, IEditorInput editorInput, Runnable dirtyStateUpdater) {
 		this.dirtyStateUpdater = dirtyStateUpdater;
 		// Retrieve the object from the editor input
 		final EObject object = (EObject) editorInput.getAdapter(EObject.class);
 
 		// Resolve the URI behind the editor input via the editor resource set
 		final TransactionalEditingDomain ed = (TransactionalEditingDomain) editorInput.getAdapter(TransactionalEditingDomain.class);
 		initializeEditingDomain(ed);
 
 		// Problem analysis
 		editingDomain.getResourceSet().eAdapters().add(problemIndicationAdapter);
 
 		// Register for object deletion
 		if (object != null) {
 			elementDeleteListener = new ElementDeleteListener();
 			object.eAdapters().add(this.elementDeleteListener);
 		}
 
 		getOperationHistory().addOperationHistoryListener(this);
 	}
 
 	public void dispose() {
 		updateProblemIndication = false;
 
 		// Remove all the registered listeners
 		editingDomain.getResourceSet().eAdapters().remove(resourceSetUpdateAdapter);
 		getOperationHistory().removeOperationHistoryListener(this);
 
 		for (Resource r : editingDomain.getResourceSet().getResources()) {
 			r.eAdapters().remove(updateAdapter);
 		}
 
 		EObject object = (EObject) editorPart.getEditorInput().getAdapter(EObject.class);
 		if (object != null) {
 			object.eAdapters().remove(elementDeleteListener);
 		}
 
 		workspaceSynchronizer.dispose();
 
 		// Clean up editor input
 		IEditorInput editorInput = editorPart.getEditorInput();
 		if (editorInput instanceof DiagramEditorInput) {
 			((DiagramEditorInput) editorInput).dispose();
 		}
 
 		// Remove references
 		editingDomain = null;
 		editorPart = null;
 	}
 
 	/**
 	 * Adding update adapters to the respective resources.
 	 */
 	private final class ResourceSetUpdateAdapter extends AdapterImpl {
 		@SuppressWarnings("unchecked")
 		@Override
 		public void notifyChanged(Notification msg) {
 			if (msg.getFeatureID(ResourceSet.class) == ResourceSet.RESOURCE_SET__RESOURCES) {
 				switch (msg.getEventType()) {
 				case Notification.ADD:
 					((Resource) msg.getNewValue()).eAdapters().add(updateAdapter);
 					break;
 				case Notification.ADD_MANY:
 					for (final Resource res : (Collection<Resource>) msg.getNewValue()) {
 						res.eAdapters().add(updateAdapter);
 					}
 					break;
 				case Notification.REMOVE:
 					((Resource) msg.getOldValue()).eAdapters().remove(updateAdapter);
 					break;
 				case Notification.REMOVE_MANY:
 					for (final Resource res : (Collection<Resource>) msg.getOldValue()) {
 						res.eAdapters().remove(updateAdapter);
 					}
 					break;
 
 				default:
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Closes editor if model element was deleted.
 	 */
 	private final class ElementDeleteListener extends AdapterImpl {
 
 		@Override
 		public boolean isAdapterForType(Object type) {
 			return type instanceof EObject;
 		}
 
 		@Override
 		public void notifyChanged(Notification msg) {
 			final IEditorPart part = editorPart;
 			if (T.racer().debug()) {
 				final String editorName = part.getTitle();
 				T.racer().debug("Delete listener called of editor " //$NON-NLS-1$
 						+ editorName + " with events " + msg.toString()); //$NON-NLS-1$
 			}
 
 			final IEditorInput in = part.getEditorInput();
 			if (in != null) {
 				final IEditorSite site = part.getEditorSite();
 				if (site == null) {
 					return;
 				}
 				final Shell shell = site.getShell();
 				// Do the real work, e.g. object retrieval from input and
 				// closing, asynchronous to not block this listener longer than necessary,
 				// which may provoke deadlocks.
 				shell.getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (editorPart == null) {
 							return; // disposed
 						}
 						if (shell.isDisposed()) {
 							return; // disposed
 						}
 						EObject obj = null;
 						try {
 							obj = (EObject) in.getAdapter(EObject.class);
 						} catch (final Exception e) {
 							// Ignore, exception indicates that the object has been deleted
 						}
 						if (obj == null || EcoreUtil.getRootContainer(obj) == null) {
 							// object is gone so try to close
 							final IWorkbenchPage page = site.getPage();
 							if (T.racer().debug()) {
 								final String editorName = part.getTitle();
 								T.racer().debug("Closing editor " + editorName); //$NON-NLS-1$
 							}
 							page.closeEditor(part, false);
 						}
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Called by editor parts when focus is set by Eclipse. It is necessary to
 	 * update the action bars (undo menu) accordingly if editor receives focus
 	 */
 	public void setFocus() {
 		this.handleActivate();
 	}
 
 	@Override
 	public void historyNotification(OperationHistoryEvent event) {
 		switch (event.getEventType()) {
 		case OperationHistoryEvent.DONE:
 			final Set<Resource> affectedResources = ResourceUndoContext.getAffectedResources(event.getOperation());
 			final TransactionalEditingDomain editingDomain = getEditingDomain();
 			if (editingDomain != null) {
 				final ResourceSet resourceSet = editingDomain.getResourceSet();
 				// Editing domain might already be disposed 
 				if (resourceSet != null) {
 					final EList<Resource> resourcesInThisEditor = resourceSet.getResources();
 					for (final Resource resource : resourcesInThisEditor) {
 						if (affectedResources.contains(resource)) {
 							//					final IUndoableOperation operation = event.getOperation();
 
 							// The following coding was taken over from the generated EMF editors
 							// In our scenario it seems not to be necessary to remove the default
 							// undo context from the operation to achieve separate undo stacks in
 							// separate editors. In contrast: if we remove the default undo context
 							// we break the undo behavior inside the Diagram Editor!
 							// operation.removeContext(((IWorkspaceCommandStack)
 							// editingDomain.getCommandStack()).getDefaultUndoContext());
 
 							// add our undo context to populate our undo menu
 							// operation.addContext(undoContext);
 							return;
 						}
 					}
 				}
 			}
 			break;
 		case OperationHistoryEvent.REDONE:
 		case OperationHistoryEvent.UNDONE:
 			this.dirtyStateUpdater.run();
 			break;
 		}
 	}
 
 	public IUndoContext getUndoContext() {
 		return ((IWorkspaceCommandStack) getEditingDomain().getCommandStack()).getDefaultUndoContext();
 	}
 
 	private void refreshEditorContent() {
 		if (editorPart instanceof DiagramEditorInternal) {
 			((DiagramEditorInternal) editorPart).refreshContent();
 		}
 	}
 }
