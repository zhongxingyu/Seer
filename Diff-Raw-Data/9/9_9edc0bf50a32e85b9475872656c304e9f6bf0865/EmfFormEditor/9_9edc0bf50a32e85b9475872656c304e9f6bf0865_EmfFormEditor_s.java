 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: EmfFormEditor.java,v 1.3 2009/02/15 20:48:16 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.editor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import java.util.List;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.*;
 import org.eclipse.emf.common.command.*;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.ui.MarkerHelper;
 import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.edit.command.CreateChildCommand;
 import org.eclipse.emf.edit.domain.*;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.jface.dialogs.*;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.jface.window.Window;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.pde.emfforms.internal.editor.Messages;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.*;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 
 /**
  * A {@link FormEditor} allowing to edit an EMF {@link EObject} in a convenient
  * way
  * 
  * @param <T>
  *            The type of the {@link EObject} to edit.
  */
 public abstract class EmfFormEditor<T extends EObject> extends FormEditor implements IEditingDomainProvider, ISelectionProvider, IViewerProvider {
 
 	/**
 	 * This keeps track of the editing domain that is used to track all changes
 	 * to the model.
 	 */
 	private EditingDomain _editingDomain;
 
 	/**
 	 * This is the one adapter factory used for providing views of the model.
 	 */
 	private ComposedAdapterFactory _adapterFactory;
 
 	private final IObservableValue _observableValue = new WritableValue();
 
 	private T _currentEObject;
 
 	/**
 	 * This keeps track of all the
 	 * {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are
 	 * listening to this editor.
 	 */
 	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
 	protected ISelection editorSelection = StructuredSelection.EMPTY;
 
 	private CommandStackListener _commandStackListener = new CommandStackListener() {
 		public void commandStackChanged(final EventObject event) {
 			getContainer().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					handleCommandStackChanged(event);
 				}
 			});
 		}
 	};
 
 	private PropertySheetPage propertySheetPage;
 
 	/**
 	 * The MarkerHelper is responsible for creating workspace resource markers presented in Eclipse's Problems View.
 	 */
 	protected MarkerHelper markerHelper = new EditUIMarkerHelper();
 
 	public EmfFormEditor() {
 		_editingDomain = createEditingDomain();
 
 		// Add a listener to set the most recent command's affected objects to
 		// be the selection of the viewer with focus.
 		//
 		getEditingDomain().getCommandStack().addCommandStackListener(_commandStackListener);
 	}
 
 	/**
 	 * This is called during startup.
 	 */
 	@Override
 	public void init(IEditorSite site, IEditorInput editorInput) {
 		setSite(site);
 		setInputWithNotify(editorInput);
 
 		setPartName(getPartName());
 		// -- To manage Copy/Cut/Paste
 		site.setSelectionProvider(this);
 	}
 
 	@Override
 	public abstract String getPartName();
 
 	@Override
 	public void dispose() {
 		getEditingDomain().getCommandStack().removeCommandStackListener(_commandStackListener);
 		if (_adapterFactory != null) {
 			_adapterFactory.dispose();
 		}
 
 		super.dispose();
 	}
 
 	/**
 	 * This sets up the editing domain for the model editor.
 	 */
 	protected EditingDomain createEditingDomain() {
 		_adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		_adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 		_adapterFactory.addAdapterFactory(getSpecificAdapterFactory());
 		_adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 
 		// Create the command stack that will notify this editor as commands are
 		// executed.
 		BasicCommandStack commandStack = new BasicCommandStack();
 
 		// Create the editing domain with a special command stack, and if it's
 		// asked a shared Clipboard
 		if (isUsingSharedClipboard())
 			return new SharedClipboardAdapterFactoryEditingDomain(_adapterFactory, commandStack, new HashMap<Resource, Boolean>());
 		// else
 		return new AdapterFactoryEditingDomain(_adapterFactory, commandStack, new HashMap<Resource, Boolean>());
 	}
 
 	/**
 	 * @return
 	 */
 	protected abstract AdapterFactory getSpecificAdapterFactory();
 
 	@Override
 	protected void addPages() {
 		// Creates the model from the editor input
 		createModel();
 
 		try {
 			for (AbstractEmfFormPage page : getPagesToAdd())
 				addPage(page);
 
 			int i = 0;
 			for (Image img : getPagesImages())
 				setPageImage(i++, img);
 
 		} catch (PartInitException e) {
 			org.eclipse.pde.emfforms.internal.Activator.logException(e, Messages.EmfFormEditor_InitError);
 		}
 	}
 
 	/**
 	 * @throws PartInitException
 	 */
 	protected abstract List<? extends AbstractEmfFormPage> getPagesToAdd() throws PartInitException;
 
 	protected abstract List<Image> getPagesImages();
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		try {
 			internalDoValidate(monitor);
 
 			// Do the work within an operation because this is a long running
 			// activity that modifies the workbench.
 			WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 				// This is the method that gets invoked when the operation runs.
 				@Override
 				public void execute(IProgressMonitor monitor) throws CoreException {
 					try {
 						internalDoSave(monitor);
 					} catch (IOException ioe) {
 						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.EmfFormEditor_SaveError, ioe));
 					}
 				}
 			};
 
 			try {
 				// This runs the options, and shows progress.
 				new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);
 
 				// Refresh the necessary state.
 				((BasicCommandStack) getEditingDomain().getCommandStack()).saveIsDone();
 				firePropertyChange(IEditorPart.PROP_DIRTY);
				getEditingDomain().getCommandStack().flush();
 
 			} catch (Exception exception) {
 				// Something went wrong that shouldn't.
 				MessageDialog.openError(getSite().getShell(), Messages.EmfFormEditor_SaveError_Title, Messages.EmfFormEditor_SaveError_Msg);
 				Activator.logException(exception, Messages.EmfFormEditor_SaveError_Exception);
 			}
 
 		} catch (OperationCanceledException oce) {
 			// Do nothing
 		}
 
 	}
 
 	/**
 	 * @param monitor
 	 * @throws IOException
 	 */
 	protected void internalDoSave(IProgressMonitor monitor) throws IOException {
 		// Save only resources that have actually changed.
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 		// Save the resources to the file system.
 		boolean first = true;
 		for (Resource resource : getEditingDomain().getResourceSet().getResources()) {
 			if ((first || !resource.getContents().isEmpty() || isPersisted(resource)) && !getEditingDomain().isReadOnly(resource)) {
 				resource.save(saveOptions);
 
 				first = false;
 			}
 		}
 	}
 
 	/**
 	 * @param monitor
 	 */
 	protected void internalDoValidate(IProgressMonitor monitor) {
 		VALIDATE_ON_SAVE validateOnSave = validateOnSave();
 
 		// result of the Validation
 		Diagnostic diagnostic = Diagnostic.OK_INSTANCE;
 
 		// if the validation is asked by the user
 		if (validateOnSave != VALIDATE_ON_SAVE.NO_VALIDATION)
 			diagnostic = Diagnostician.INSTANCE.validate(getCurrentEObject());
 
 		if (diagnostic.getSeverity() != Diagnostic.OK) {
 			switch (validateOnSave) {
 				case VALIDATE_AND_WARN :
 					int dialogResult = new DiagnosticDialog(getSite().getShell(), Messages.EmfFormEditor_DiaDialog_InvalidModel_Title, null, diagnostic, Diagnostic.ERROR | Diagnostic.WARNING) {
 						@Override
 						protected Control createMessageArea(Composite composite) {
 							message = Messages.EmfFormEditor_ValidationWarn_Msg;
 							return super.createMessageArea(composite);
 						}
 
 						@Override
 						protected void createButtonsForButtonBar(Composite parent) {
 							// create OK and Details buttons
 							createButton(parent, IDialogConstants.OK_ID, IDialogConstants.YES_LABEL, true);
 							createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.NO_LABEL, true);
 							createDetailsButton(parent);
 						}
 					}.open();
 
 					if (dialogResult != Window.OK) {
 						if (monitor != null)
 							monitor.setCanceled(true);
 						throw new OperationCanceledException();
 					}
 					break;
 				case VALIDATE_AND_ABORT :
 					new DiagnosticDialog(getSite().getShell(), Messages.EmfFormEditor_DiaDialog_InvalidModel_Title, null, diagnostic, Diagnostic.ERROR | Diagnostic.WARNING) {
 						@Override
 						protected Control createMessageArea(Composite composite) {
 							message = Messages.EmfFormEditor_ValidationError_Msg;
 							return super.createMessageArea(composite);
 						}
 					}.open();
 
 					monitor.setCanceled(true);
 
 					throw new OperationCanceledException();
 				default :
 					// fall through
 			}
 
 		}
 	}
 
 	/**
 	 * Return a member of {@link VALIDATE_ON_SAVE}, in order to say if a
 	 * validation must be executed before saving resource
 	 * 
 	 * <ul>
 	 * <li>
 	 * {@link VALIDATE_ON_SAVE.NO_VALIDATION} no validation executed before
 	 * saving resource</li>
 	 * <li>{@link VALIDATE_ON_SAVE.VALIDATE_AND_WARN} a validation is executed,
 	 * and only warn the user if the validation is not OK</li>
 	 * <li>
 	 * {@link VALIDATE_ON_SAVE.VALIDATE_AND_ABORT} a validation is executed, and
 	 * the resource cannot be saved</li>
 	 * </ul>
 	 * 
 	 * Clients can override this method, by default return
 	 * {@link VALIDATE_ON_SAVE.NO_VALIDATION}
 	 * 
 	 * @return {@link VALIDATE_ON_SAVE}
 	 */
 	protected VALIDATE_ON_SAVE validateOnSave() {
 		return VALIDATE_ON_SAVE.NO_VALIDATION;
 	}
 
 	public enum VALIDATE_ON_SAVE {
 		NO_VALIDATION, VALIDATE_AND_WARN, VALIDATE_AND_ABORT
 	}
 
 	/**
 	 * This returns whether something has been persisted to the URI of the
 	 * specified resource. The implementation uses the URI converter from the
 	 * editor's resource set to try to open an input stream.
 	 */
 	protected boolean isPersisted(Resource resource) {
 		boolean result = false;
 		try {
 			InputStream stream = getEditingDomain().getResourceSet().getURIConverter().createInputStream(resource.getURI());
 			if (stream != null) {
 				result = true;
 				stream.close();
 			}
 		} catch (IOException e) {
 			// Ignore
 		}
 		return result;
 	}
 
 	/**
 	 * This also changes the editor's input.
 	 */
 	@Override
 	public void doSaveAs() {
 		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
 		saveAsDialog.open();
 		IPath path = saveAsDialog.getResult();
 		if (path != null) {
 			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 			if (file != null) {
 				doSaveAs(URI.createPlatformResourceURI(file.getFullPath().toString(), true), new FileEditorInput(file));
 			}
 		}
 	}
 
 	protected void doSaveAs(URI uri, IEditorInput editorInput) {
 		(getEditingDomain().getResourceSet().getResources().get(0)).setURI(uri);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		IProgressMonitor progressMonitor = getActionBars().getStatusLineManager() != null ? getActionBars().getStatusLineManager().getProgressMonitor() : new NullProgressMonitor();
 		doSave(progressMonitor);
 	}
 
 	/**
 	 * This is for implementing {@link IEditorPart} and simply tests the command
 	 * stack.
 	 */
 	@Override
 	public boolean isDirty() {
 		return ((BasicCommandStack) getEditingDomain().getCommandStack()).isSaveNeeded();
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 
 	@Override
 	protected PDEFormToolkit createToolkit(Display display) {
 		return new PDEFormToolkit(display);
 	}
 
 	/**
 	 * This is the method called to load a resource into the editing domain's
 	 * resource set based on the editor's input.
 	 */
 	public void createModel() {
 		URI resourceURI = EditUIUtil.getURI(getEditorInput());
 		Resource resource;
 		try {
 			// Load the resource through the editing domain.
 			resource = getEditingDomain().getResourceSet().getResource(resourceURI, true);
 		} catch (Exception e) {
 			resource = getEditingDomain().getResourceSet().getResource(resourceURI, false);
 		}
 
 		setMainResource(resource);
 	}
 
 	protected void setMainResource(Resource resource) {
 		_currentEObject = (T) resource.getContents().get(0);
 		_observableValue.setValue(_currentEObject);
 	}
 
 	/**
 	 * This returns the editing domain as required by the
 	 * {@link IEditingDomainProvider} interface. This is important for
 	 * implementing the static methods of {@link AdapterFactoryEditingDomain}
 	 * and for supporting {@link org.eclipse.emf.edit.ui.action.CommandAction}.
 	 */
 	public EditingDomain getEditingDomain() {
 		return _editingDomain;
 	}
 
 	public EditingDomainActionBarContributor getActionBarContributor() {
 		return (EditingDomainActionBarContributor) getEditorSite().getActionBarContributor();
 	}
 
 	public IActionBars getActionBars() {
 		return getActionBarContributor().getActionBars();
 	}
 
 	/**
 	 * @return The {@link T} currently edited
 	 */
 	protected T getCurrentEObject() {
 		return _currentEObject;
 	}
 
 	public IObservableValue getInputObservable() {
 		return _observableValue;
 	}
 
 	// -------------------------------------------//
 	// -- Context Menu on EMF objects ------------//
 	// -------------------------------------------//
 
 	/**
 	 * @param viewer
 	 */
 	public void addViewerToListenTo(StructuredViewer viewer) {
 		// This listener is used to manage copy/paste...
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
 				setSelection(selectionChangedEvent.getSelection());
 			}
 		});
 	}
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.add(listener);
 	}
 
 	public ISelection getSelection() {
 		return editorSelection;
 	}
 
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.remove(listener);
 	}
 
 	public void setSelection(ISelection selection) {
 		editorSelection = selection;
 
 		for (ISelectionChangedListener listener : selectionChangedListeners) {
 			listener.selectionChanged(new SelectionChangedEvent(this, selection));
 		}
 	}
 
 	public Viewer getViewer() {
 		AbstractEmfFormPage page = (AbstractEmfFormPage) pages.get(getActivePage());
 		return page.getViewer();
 	}
 
 	/**
 	 * @return <code>true</code> if this editor must use a shared clipboard,
 	 *         which will allow copy/paste actions between different instance of
 	 *         the same editor. The default return value is <code>false</code>
 	 */
 	public boolean isUsingSharedClipboard() {
 		return false;
 	}
 
 	/**
 	 * @param event
 	 */
 	protected void handleCommandStackChanged(final EventObject event) {
 		// the edited object has been changed
 		firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
 		// since the title of the editor is, most of the time,
 		// computed using some object attribute, ask the editor
 		// to refresh it!
 		firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
 
 		IFormPage activePage = getActivePageInstance();
 		if (activePage != null) {
 			if (((AbstractEmfFormPage) activePage).getViewer() != null) {
 				// Select the affected objects in the viewer
 				Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
 				if (mostRecentCommand != null) {
 					if (mostRecentCommand instanceof CreateChildCommand) {
 						Collection<?> objects = mostRecentCommand.getAffectedObjects();
 						if (objects != null && !objects.isEmpty())
 							((AbstractEmfFormPage) activePage).getViewer().setSelection(new StructuredSelection(objects.toArray()[objects.size() - 1]), true);
 					}
 				}
 			}
 		}
 	}
 
 	private static class SharedClipboardAdapterFactoryEditingDomain extends AdapterFactoryEditingDomain {
 		private static Collection<Object> clipboard;
 
 		/**
 		 * @param adapterFactory
 		 * @param commandStack
 		 * @param resourceToReadOnlyMap
 		 */
 		public SharedClipboardAdapterFactoryEditingDomain(AdapterFactory adapterFactory, CommandStack commandStack, Map<Resource, Boolean> resourceToReadOnlyMap) {
 			super(adapterFactory, commandStack, resourceToReadOnlyMap);
 
 		}
 
 		@Override
 		public Collection<Object> getClipboard() {
 			return SharedClipboardAdapterFactoryEditingDomain.clipboard;
 		}
 
 		@Override
 		public void setClipboard(Collection<Object> clipboard) {
 			SharedClipboardAdapterFactoryEditingDomain.clipboard = clipboard;
 		}
 	}
 
 	public ComposedAdapterFactory getAdapterFactory() {
 		return _adapterFactory;
 	}
 
 	@Override
 	public Object getAdapter(Class key) {
 		if (key.equals(IPropertySheetPage.class)) {
 			return getPropertySheetPage();
 		} else {
 			return super.getAdapter(key);
 		}
 	}
 
 	public IPropertySheetPage getPropertySheetPage() {
 		if (propertySheetPage == null) {
 			propertySheetPage = new ExtendedPropertySheetPage((AdapterFactoryEditingDomain) _editingDomain) {
 				@Override
 				public void setSelectionToViewer(List<?> selection) {
 					// TODO
 					//	EmfFormEditor.this.setSelectionToViewer(selection);
 					EmfFormEditor.this.setFocus();
 				}
 
 				@Override
 				public void setActionBars(IActionBars actionBars) {
 					super.setActionBars(actionBars);
 					getActionBarContributor().shareGlobalActions(this, actionBars);
 				}
 			};
 			propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(_adapterFactory));
 		}
 
 		return propertySheetPage;
 	}
 }
