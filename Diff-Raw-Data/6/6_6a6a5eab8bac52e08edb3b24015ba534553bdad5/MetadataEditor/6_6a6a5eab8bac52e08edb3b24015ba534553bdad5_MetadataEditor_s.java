 package liglab.imag.fr.metadata.ui.editor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import liglab.imag.fr.metadata.editor.ComponentEditorPlugin;
 import liglab.imag.fr.metadata.ui.editor.page.component.ComponentMasterPage;
 import liglab.imag.fr.metadata.ui.editor.page.instance.InstanceMasterPage;
 
 import org.apache.felix.ComponentType;
 import org.apache.felix.DocumentRoot;
 import org.apache.felix.IpojoType;
 import org.apache.felix.ProvidesType;
 import org.apache.felix.RequiresType;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.ui.MarkerHelper;
 import org.eclipse.emf.common.ui.editor.ProblemEditorPart;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.pde.core.project.IBundleProjectDescription;
 import org.eclipse.pde.core.project.IPackageExportDescription;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.part.MultiPageEditorSite;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML;
 
 public class MetadataEditor extends FormEditor implements IResourceChangeListener {
 
 	/** The text editor used in page 0. */
 	private StructuredTextEditor xmlEditor;
 
 	/**
 	 * The model been edited.
 	 */
 	private IpojoType pojoModel;
 
 	/**
 	 * This keeps track of the editing domain that is used to track all changes
 	 * to the model. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected AdapterFactoryEditingDomain editingDomain;
 
 	/**
 	 * Map to store the diagnostic associated with a resource. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
 
 	/**
 	 * Adapter used to update the problem indication when resources are demanded
 	 * loaded. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected EContentAdapter problemIndicationAdapter = new EContentAdapter() {
 		@Override
 		public void notifyChanged(Notification notification) {
 			if (notification.getNotifier() instanceof Resource) {
 				switch (notification.getFeatureID(Resource.class)) {
 				case Resource.RESOURCE__IS_LOADED:
 				case Resource.RESOURCE__ERRORS:
 				case Resource.RESOURCE__WARNINGS: {
 					Resource resource = (Resource) notification.getNotifier();
 					Diagnostic diagnostic = analyzeResourceProblems(resource, null);
 					if (diagnostic.getSeverity() != Diagnostic.OK) {
 						resourceToDiagnosticMap.put(resource, diagnostic);
 					} else {
 						resourceToDiagnosticMap.remove(resource);
 					}
 
 					if (updateProblemIndication) {
 						getSite().getShell().getDisplay().asyncExec(new Runnable() {
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
 
 	/**
 	 * Controls whether the problem indication should be updated. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected boolean updateProblemIndication = true;
 
 	/**
 	 * The MarkerHelper is responsible for creating workspace resource markers
 	 * presented in Eclipse's Problems View. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected MarkerHelper markerHelper = new EditUIMarkerHelper();
 
 	/**
 	 * This is the one adapter factory used for providing views of the model.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected ComposedAdapterFactory adapterFactory;
 
 	/**
 	 * Resources that have been saved. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<Resource> savedResources = new ArrayList<Resource>();
 
 	public MetadataEditor() {
 		super();
 		initializeEditingDomain();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 
 	@Override
 	protected void addPages() {
 
 		createModel();
 
 		try {
 			addPage(new ComponentMasterPage(this, "master-details-component", "Component Types Definition"));
 			addPage(new InstanceMasterPage(this, "master-details-instance", "Components Configuration"));
 			// XML Editor is created
 			xmlEditor = new StructuredTextEditor() {
 				@Override
 				public boolean isEditable() {
 					return false;
 				}
 			};
 
 			int index = addPage(xmlEditor, getEditorInput());
 			setPageText(index, "XML (Read Only)");
 
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		
 		final IProject project = ((IFileEditorInput) this.getEditorInput()).getFile().getProject();
 		
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 
 		// Do the work within an operation because this is a long running activity
 		// that modifies the workbench.
 		//
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			// This is the method that gets invoked when the operation runs.
 			//
 			@Override
 			public void execute(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				//
 				boolean first = true;
 				for (Resource resource : editingDomain.getResourceSet().getResources()) {
 					if ((first || !resource.getContents().isEmpty() || isPersisted(resource))
 					      && !editingDomain.isReadOnly(resource)) {
 						try {
 							long timeStamp = resource.getTimeStamp();
 							resource.save(saveOptions);
 							if (resource.getTimeStamp() != timeStamp) {
 								savedResources.add(resource);
 							}
 							
 							/*
 							 * Update Manifest to automatically handle export and import packages
 							 * 
 							 * TODO this should be better done by the iPOJO builder, to discuss
 							 */
 							
 							updateManifest(project, monitor);
 							
 						} catch (Exception exception) {
 							resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
 						}
 						first = false;
 					}
 				}
 			}
 		};
 
 		updateProblemIndication = false;
 		try {
 			// This runs the options, and shows progress.
 			//
 			new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);
 
 			// Refresh the necessary state.
 			//
 			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
 			firePropertyChange(IEditorPart.PROP_DIRTY);
 		} catch (Exception exception) {
 			// Something went wrong that shouldn't.
 			//
 			// IpojocoreEditorPlugin.INSTANCE.log(exception);
 		}
 		updateProblemIndication = true;
 		updateProblemIndication();
 
 		
 		// Build the project
 		try {
 			((IFileEditorInput) this.getEditorInput()).getFile().getProject()
 			      .build(IncrementalProjectBuilder.FULL_BUILD, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Updates the Manifest to automatically add exports for referenced (provided and required) packages
 	 * declared in the project.
 	 */
 	private void updateManifest(IProject project, IProgressMonitor monitor) throws CoreException {
         SubMonitor progress = SubMonitor.convert(monitor,"update manifets", 1);
         try {
         	
         	IJavaProject javaProject 			= JavaCore.create(project);
         	Set<String> referencedInterfaces	= new HashSet<String>();
         	
         	/*
         	 * Verify all the provided and required services, if the specified interface is declared
         	 * in the project it must be exported 
         	 */
         	for (ComponentType component : getModel().getComponent()) {
  
        			/*
     			 * TODO In principle, we could be provide a list of interfaces, but the editor only
     			 * allow to specify a single interface
     			 */
         		for (ProvidesType provided : component.getProvides()) {
         			referencedInterfaces.add(provided.getSpecifications());
 				}
         		
         		for (RequiresType required : component.getRequires()) {
         			referencedInterfaces.add(required.getSpecification());
 				}
 			}
 
         	Set<IPackageFragment> exportedPackages = new HashSet<IPackageFragment>();
         	for (String referencedInterface : referencedInterfaces) {
 
         		IType referencedClass = javaProject.findType(referencedInterface);
     			if (referencedClass == null)
     				continue;
     			
     			IPackageFragment packageFragment = referencedClass.getPackageFragment();
    			if (packageFragment.isDefaultPackage() || packageFragment.getResource() == null)
     				continue;
     			
     			exportedPackages.add(packageFragment);
 			}
 
 			IBundleProjectDescription bundle 		= ComponentEditorPlugin.getDefault().getDescription(project);
         	List<IPackageExportDescription> exports = new ArrayList<IPackageExportDescription>(optional(bundle.getPackageExports()));
         	
         	for (IPackageFragment exportedPackage : exportedPackages) {
         		exports.add(ComponentEditorPlugin.getDefault().getExportDescription(exportedPackage.getElementName(),null));
 			}
         	
         	/*
         	 * Split long manifest lines generated by the iPOJO metadata
         	 */
         	String manipulatedMetadata = bundle.getHeader("iPOJO-Components");
         	if (manipulatedMetadata != null && manipulatedMetadata.length() > 0) {
         		bundle.setHeader("iPOJO-Components", fixedWidthLine(manipulatedMetadata));
         	}
         	bundle.setPackageExports(exports.toArray(new IPackageExportDescription[exports.size()]));
 			bundle.apply(progress.newChild(1));
         	
                           
         } finally {
             if (monitor != null) {
             	monitor.done();
             }
         } 
 	}
 	
 	private static String fixedWidthLine(String value) {
 		StringBuilder result = new StringBuilder();
 		while (value.length() > 72) {
 			result.append(value.substring(0,72)).append("\n ");
 			value = value.substring(72);
 		}
 		
 		if (value.length() > 0)
 			result.append(value);
 		
 		return result.toString();
 	}
 	
 	private static <E> List<E> optional (E... args) {
 		return args == null ? Collections.<E>emptyList() : Arrays.asList(args);
 	}
 
 	@Override
 	public void doSaveAs() {
 		doSave(null);
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	protected IEditorSite createSite(IEditorPart page) {
 		IEditorSite site = null;
 		if (page == xmlEditor) {
 			site = new MultiPageEditorSite(this, page) {
 				public String getId() {
 					// Sets this ID so nested editor is configured for XML source
 					return ContentTypeIdForXML.ContentTypeID_XML + ".source"; //$NON-NLS-1$;
 				}
 			};
 		} else {
 			site = super.createSite(page);
 		}
 		return site;
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		super.init(site, input);
 	}
 
 	@Override
 	public boolean isDirty() {
 		// return dirty;
 		return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 	}
 
 	public IpojoType getModel() {
 		return pojoModel;
 	}
 
 	public void fireDirtyProperty() {
 		firePropertyChange(IEditorPart.PROP_DIRTY);
 	}
 
 	/**
 	 * This is the method called to load a resource into the editing domain's
 	 * resource set based on the editor's input. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void createModel() {
 		URI resourceURI = EditUIUtil.getURI(getEditorInput());
 		Exception exception = null;
 		Resource resource = null;
 		try {
 			// Load the resource through the editing domain.
 			//
 			resource = editingDomain.getResourceSet().getResource(resourceURI, true);
 		} catch (Exception e) {
 			exception = e;
 			resource = editingDomain.getResourceSet().getResource(resourceURI, false);
 		}
 
 		if (resource.getContents().size() > 0) {
 			DocumentRoot root = (DocumentRoot) resource.getContents().get(0);
 			pojoModel = root.getIpojo();
 		}
 
 		Diagnostic diagnostic = analyzeResourceProblems(resource, exception);
 		if (diagnostic.getSeverity() != Diagnostic.OK) {
 			resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
 		}
 		editingDomain.getResourceSet().eAdapters().add(problemIndicationAdapter);
 	}
 
 	/**
 	 * Returns a diagnostic describing the errors and warnings listed in the
 	 * resource and the specified exception (if any). <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Diagnostic analyzeResourceProblems(Resource resource, Exception exception) {
 		if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) {
 			BasicDiagnostic basicDiagnostic = new BasicDiagnostic(Diagnostic.ERROR, "ipojo-model.editor", 0,
 			      "_UI_CreateModelError_message", new Object[] { exception == null ? (Object) resource : exception });
 			basicDiagnostic.merge(EcoreUtil.computeDiagnostic(resource, true));
 			return basicDiagnostic;
 		} else if (exception != null) {
 			return new BasicDiagnostic(Diagnostic.ERROR, "ipojo-model.editor", 0, "_UI_CreateModelError_message",
 			      new Object[] { exception });
 		} else {
 			return Diagnostic.OK_INSTANCE;
 		}
 	}
 
 	/**
 	 * Updates the problems indication with the information described in the
 	 * specified diagnostic. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void updateProblemIndication() {
 		if (updateProblemIndication) {
 			BasicDiagnostic diagnostic = new BasicDiagnostic(Diagnostic.OK, "ipojo-model.editor", 0, null,
 			      new Object[] { editingDomain.getResourceSet() });
 			for (Diagnostic childDiagnostic : resourceToDiagnosticMap.values()) {
 				if (childDiagnostic.getSeverity() != Diagnostic.OK) {
 					diagnostic.add(childDiagnostic);
 				}
 			}
 
 			int lastEditorPage = getPageCount() - 1;
 			if (lastEditorPage >= 0 && getEditor(lastEditorPage) instanceof ProblemEditorPart) {
 				((ProblemEditorPart) getEditor(lastEditorPage)).setDiagnostic(diagnostic);
 				if (diagnostic.getSeverity() != Diagnostic.OK) {
 					setActivePage(lastEditorPage);
 				}
 			} else if (diagnostic.getSeverity() != Diagnostic.OK) {
 				ProblemEditorPart problemEditorPart = new ProblemEditorPart();
 				problemEditorPart.setDiagnostic(diagnostic);
 				problemEditorPart.setMarkerHelper(markerHelper);
 				try {
 					addPage(++lastEditorPage, problemEditorPart, getEditorInput());
 					setPageText(lastEditorPage, problemEditorPart.getPartName());
 					setActivePage(lastEditorPage);
 					showTabs();
 				} catch (PartInitException exception) {
 					// IpojocoreEditorPlugin.INSTANCE.log(exception);
 				}
 			}
 
 			if (markerHelper.hasMarkers(editingDomain.getResourceSet())) {
 				markerHelper.deleteMarkers(editingDomain.getResourceSet());
 				if (diagnostic.getSeverity() != Diagnostic.OK) {
 					try {
 						markerHelper.createMarkers(diagnostic);
 					} catch (CoreException exception) {
 						// IpojocoreEditorPlugin.INSTANCE.log(exception);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * If there is more than one page in the multi-page editor part, this shows
 	 * the tabs at the bottom. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void showTabs() {
 		if (getPageCount() > 1) {
 			setPageText(0, "_UI_SelectionPage_label");
 			if (getContainer() instanceof CTabFolder) {
 				((CTabFolder) getContainer()).setTabHeight(SWT.DEFAULT);
 				Point point = getContainer().getSize();
 				getContainer().setSize(point.x, point.y - 6);
 			}
 		}
 	}
 
 	/**
 	 * This sets up the editing domain for the model editor. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void initializeEditingDomain() {
 		// Create an adapter factory that yields item providers.
 		//
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		// Create the command stack that will notify this editor as commands are
 		// executed.
 		//
 		BasicCommandStack commandStack = new BasicCommandStack();
 
 		// Add a listener to set the most recent command's affected objects to be
 		// the selection of the viewer with focus.
 		//
 		commandStack.addCommandStackListener(new CommandStackListener() {
 			public void commandStackChanged(final EventObject event) {
 				getContainer().getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						firePropertyChange(IEditorPart.PROP_DIRTY);
 
 						// Try to select the affected objects.
 						//
 						Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
 						if (mostRecentCommand != null) {
 							IEditorPart editorPart = getEditor(1);
 							if (editorPart != null) {
 								if (editorPart instanceof InstanceMasterPage) {
 									InstanceMasterPage page = (InstanceMasterPage) editorPart;
 									page.refreshComponentList();
 								}
 							}
 						}
 
 						// if (mostRecentCommand != null) {
 						// setSelectionToViewer(mostRecentCommand.getAffectedObjects());
 						// }
 						// if (propertySheetPage != null &&
 						// !propertySheetPage.getControl().isDisposed()) {
 						// propertySheetPage.refresh();
 						// }
 					}
 				});
 			}
 		});
 
 		// Create the editing domain with a special command stack.
 		//
 		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
 	}
 
 	/**
 	 * This returns whether something has been persisted to the URI of the
 	 * specified resource. The implementation uses the URI converter from the
 	 * editor's resource set to try to open an input stream. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected boolean isPersisted(Resource resource) {
 		boolean result = false;
 		try {
 			InputStream stream = editingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
 			if (stream != null) {
 				result = true;
 				stream.close();
 			}
 		} catch (IOException e) {
 			// Ignore
 		}
 		return result;
 	}
 
 	public ComposedAdapterFactory getAdapterFactory() {
 		return adapterFactory;
 	}
 
 	public void setAdapterFactory(ComposedAdapterFactory adapterFactory) {
 		this.adapterFactory = adapterFactory;
 	}
 
 	public EditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * Tracking when the project is closed or deleted to close the editor
 	 */
 	@Override
 	public void resourceChanged(final IResourceChangeEvent event) {
 
 		final IEditorInput input = getEditorInput();
 		if (!(input instanceof IFileEditorInput))
 			return;
 		final IFile editorFile = ((IFileEditorInput) input).getFile();
 
 		if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
 			IProject editorFileProject = editorFile.getProject();
 			IProject projectToClose = (IProject) event.getResource();
 
 			if (editorFileProject == projectToClose) {
 
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						if (getSite() == null)
 							return;
 						if (getSite().getWorkbenchWindow() == null)
 							return;
 
 						IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
 						for (int i = 0; i < pages.length; i++) {
 							IEditorPart editorPart = pages[i].findEditor(input);
 							pages[i].closeEditor(editorPart, true);
 						}
 					}
 				});
 			}
 		}
 	}
 
 }
