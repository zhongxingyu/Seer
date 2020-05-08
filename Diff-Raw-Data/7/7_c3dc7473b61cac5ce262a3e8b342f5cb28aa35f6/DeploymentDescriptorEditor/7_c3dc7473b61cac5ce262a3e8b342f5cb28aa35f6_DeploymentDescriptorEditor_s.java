 package org.zend.php.zendserver.deployment.ui.editors;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.zend.php.zendserver.deployment.core.IncrementalDeploymentBuilder;
 import org.zend.php.zendserver.deployment.core.descriptor.ChangeEvent;
 import org.zend.php.zendserver.deployment.core.descriptor.DescriptorContainerManager;
 import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorChangeListener;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
 import org.zend.php.zendserver.deployment.core.descriptor.ResourceMapper;
 import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;
 import org.zend.php.zendserver.deployment.ui.Activator;
 import org.zend.php.zendserver.deployment.ui.Messages;
 import org.zend.php.zendserver.deployment.ui.editors.DescriptorEditorPage.FormDecoration;
 import org.zend.sdklib.mapping.MappingModelFactory;
 
 public class DeploymentDescriptorEditor extends FormEditor implements
 		IResourceChangeListener {
 
 	public static final String ID = "org.zend.php.zendserver.deployment.ui.editors.DeploymentDescriptorEditor"; //$NON-NLS-1$
 
 	public static final String TOOLBAR_LOCATION_URI = "toolbar:org.zend.php.zendserver.deployment.ui.editors.DeploymentDescriptorEditor"; //$NON-NLS-1$
 	
 	private SourcePage descriptorSourcePage;
 	private SourcePage propertiesSourcePage;
 	
 	protected FormToolkit createToolkit(Display display) {
 		// Create a toolkit that shares colors between editors.
 		return new FormToolkit(Activator.getDefault().getFormColors(display));
 	}
 
 	protected void addPages() {
 		try {
 			addPage(new OverviewPage(this));
 			addPage(new DescriptorMasterDetailsPage(this, new DependenciesMasterDetailsProvider(), "dependencies", Messages.DeploymentDescriptorEditor_Dependencies)); //$NON-NLS-1$
 			addPage(new ScriptsPage(this, "scripts", Messages.DeploymentDescriptorEditor_Scripts)); //$NON-NLS-1$
 			addMappingPages();
 			descriptorSourcePage = new SourcePage("source", this); //$NON-NLS-1$
 			addPage(descriptorSourcePage, getEditorInput());
 		} catch (PartInitException e) {
 			//
 		}
 	}
 
 	private IDescriptorContainer fModel;
 	private IDocumentProvider fDocumentProvider;
 	private String iconLocation = Activator.IMAGE_DESCRIPTOR_OVERVIEW;
 
 	private ResourceMapper fResourceMapper;
 
 	private FileEditorInput propertiesInput;
 
 	/**
 	 * Creates a multi-page editor example.
 	 */
 	public DeploymentDescriptorEditor() {
 		super();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 		fDocumentProvider = new DescriptorDocumentProvider();
 	}
 
 	/**
 	 * The <code>MultiPageEditorPart</code> implementation of this
 	 * <code>IWorkbenchPart</code> method disposes all nested editors.
 	 * Subclasses may extend.
 	 */
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		super.dispose();
 	}
 
 	/**
 	 * Saves the multi-page editor's document.
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		descriptorSourcePage.doSave(monitor);
 		if (isMappingAvailable() && propertiesSourcePage != null) {
 			propertiesSourcePage.doSave(monitor);
 		}
 	}
 
 	/**
 	 * Saves the multi-page editor's document as another file. Also updates the
 	 * control for page 0's tab, and updates this multi-page editor's input to
 	 * correspond to the nested editor's.
 	 */
 	public void doSaveAs() {
 		IEditorPart editor = getEditor(0);
 		editor.doSaveAs();
 		setPageText(0, editor.getTitle());
 		setInput(editor.getEditorInput());
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IEditorPart
 	 */
 	public void gotoMarker(IMarker marker) {
 		setActivePage(0);
 		IDE.gotoMarker(getEditor(0), marker);
 	}
 
 	/**
 	 * The <code>MultiPageEditorExample</code> implementation of this method
 	 * checks that the input is an instance of <code>IFileEditorInput</code>.
 	 */
 	public void init(IEditorSite site, IEditorInput editorInput)
 			throws PartInitException {
 		if (!(editorInput instanceof IFileEditorInput))
 			throw new PartInitException(
 					"Invalid Input: Must be IFileEditorInput"); //$NON-NLS-1$
 
 		IFileEditorInput fileInput = (IFileEditorInput) editorInput;
 		fModel = DescriptorContainerManager.getService()
 				.openDescriptorContainer(fileInput.getFile());
 		super.init(site, editorInput);
 		
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 		
 		initDescriptor(editorInput);
 		initMapping();
 		final IFile file = fModel.getFile();
 		if (file != null) {
 			setPartName(file.getProject().getName());
 		}
 	}
 
 	private void initDescriptor(IEditorInput editorInput) throws PartInitException {
 		try {
 			fDocumentProvider.connect(editorInput);
 		} catch (CoreException e) {
 			throw new PartInitException(new Status(IStatus.ERROR,
 					Activator.PLUGIN_ID, e.getMessage(), e));
 		}
 		
 		fModel.connect(getDocument());
 		changeIcon(fModel.getDescriptorModel().getIconLocation());
 		
 		fModel.getDescriptorModel().addListener(new IDescriptorChangeListener() {
 
 			public void descriptorChanged(ChangeEvent event) {
 				if (event.target instanceof IDeploymentDescriptor) {
 					handleModelUpdate((IDeploymentDescriptor)event.target);
 				}
 			}
 		});
 	}
 	
 	private void initMapping() throws PartInitException {
 		IFile propsFile = (IFile) fModel.getFile().getParent()
 				.getFile(new Path(MappingModelFactory.DEPLOYMENT_PROPERTIES));
 		propertiesInput = new FileEditorInput(propsFile);
 		try {
 			fDocumentProvider.connect(getPropertiesInput());
 		} catch (CoreException e) {
 			throw new PartInitException(new Status(IStatus.ERROR,
 					Activator.PLUGIN_ID, e.getMessage(), e));
 		}
 		fModel.initializeMappingModel(fDocumentProvider
 				.getDocument(propertiesInput));
 	}
 
 	protected void handleModelUpdate(final IDeploymentDescriptor descr) {
 		getEditorSite().getShell().getDisplay().asyncExec(new Runnable() {
 			
 			public void run() {
 				String newIconLocation = descr.getIconLocation();
 				changeIcon(newIconLocation);
 			}
 		});
 	}
 	
 	private void changeIcon(String newIconLocation) {
 		if ((newIconLocation == iconLocation)
 				|| (newIconLocation != null && (newIconLocation
 						.equals(iconLocation)))) {
 			return;
 		}
 
 		ImageRegistry reg = Activator.getDefault().getImageRegistry();
 		Image img = null;
 		if (newIconLocation != null) {
 	   	   img = reg.get(newIconLocation);
 		}
 		
 		if (img != null) {
 			iconLocation = newIconLocation;
 		} else {
 			IFile file = null;
 			try {
 				file = fModel.getProject().getFile(newIconLocation);
 			} catch (RuntimeException e) {
 				// ignore
 			}
 
 			if (file != null && file.exists()) {
 				String filePath = file.getLocation().toFile().getAbsolutePath();
 				try {
 				img = Activator.getDefault().createWorkspaceImage(filePath, 16);
 				} catch (SWTException ex) {
 					// ignore, e.g. unsupported file format exception
 				}
 
 				if (img != null) {
 					iconLocation = newIconLocation;
 					// reg.remove(iconLocation); // remove previous image
 					reg.put(iconLocation, img);
 				} else {
 					iconLocation = null;
 				}
 			} else {
 				iconLocation = null;
 			}
 		}
 
 		updateImage();
 	}
 
 	private void updateImage() {
 		String icon = iconLocation != null ? iconLocation : Activator.IMAGE_DESCRIPTOR_OVERVIEW;
 		Image img = Activator.getDefault().getImage(icon);
 		IFormPage page = getActivePageInstance();
 		if ((page != null) && (page instanceof FormPage)) {
 			FormPage fpage = (FormPage) page;
 			fpage.getManagedForm().getForm().setImage(img);
 		}
 	}
 	
 	private boolean isMappingAvailable() {
 		return fModel.getMappingModel() != null && fModel.getMappingModel().isLoaded();
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IEditorPart.
 	 */
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 
 	/**
 	 * Calculates the contents of page 2 when the it is activated.
 	 */
 	protected void pageChange(int newPageIndex) {
 		super.pageChange(newPageIndex);
 		updateImage();
 		if (newPageIndex == 2) {
 			//
 		}
 	}
 
 	public void resourceChanged(IResourceChangeEvent event) {
 		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(
 				IncrementalDeploymentBuilder.PROBLEM_MARKER, true);
 		if (markerDeltas.length > 0) {
 			refreshProblemMarkers(markerDeltas);
 		}
 		
 		IResourceDelta delta = event.getDelta();
 		if (delta == null) {
 			return;
 		}
 		IResourceDelta[] children = delta.getAffectedChildren();
 		if (children != null) {
 			for (IResourceDelta child : children) {
 				if (child.getResource() == fModel.getFile().getParent()) {
 					boolean isAvailable = false;
 					if (fModel.getFile().getParent().findMember(
 							MappingModelFactory.DEPLOYMENT_PROPERTIES) != null) {
 						isAvailable = true;
 					}
 					if (isAvailable && !isMappingAvailable()) {
 						addMappingPages();
 					} else if (!isAvailable && isMappingAvailable()) {
 						removeMappingPages();
 					}
 				}
 			}
 		}
 	}
 
 	private void refreshProblemMarkers(IMarkerDelta[] markerDeltas) {
 		IFormPage page = getActivePageInstance();
 		if (page instanceof DescriptorEditorPage) {
			((DescriptorEditorPage) page).showMarkers();
 		}
 	}
 
 	private void removeMappingPages() {
 		getEditorSite().getShell().getDisplay().asyncExec(new Runnable() {
 			
 			public void run() {
 				removePage(4);
 				removePage(3);
 				try {
 					fModel.getMappingModel().load(null, null);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		
 	}
 
 	private void addMappingPages() {
 		try {
 			if (!isMappingAvailable()) {
 				initMapping();
 			}
 		} catch (PartInitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (isMappingAvailable()) {
 			final DeploymentDescriptorEditor editor = this;
 			getEditorSite().getShell().getDisplay().asyncExec(new Runnable() {
 
 				public void run() {
 					try {
 						addPage(3, new DeploymentPropertiesPage(fModel, editor, "package", //$NON-NLS-1$
 								Messages.DeploymentDescriptorEditor_Package));
 						propertiesSourcePage = new PropertiesSourcePage("propertiesSource", editor); //$NON-NLS-1$
 						addPage(4, propertiesSourcePage, getPropertiesInput());
 					} catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 	}
 	
 	public IDeploymentDescriptor getModel() {
 		return fModel.getDescriptorModel();
 	}
 
 	public IDescriptorContainer getDescriptorContainer() {
 		return fModel;
 	}
 
 	public IProject getProject() {
 		return fModel.getProject();
 	}
 
 	public IDocument getDocument() {
 		return fDocumentProvider.getDocument(getEditorInput());
 	}
 
 	public IDocumentProvider getDocumentProvider() {
 		return fDocumentProvider;
 	}
 
 	public ResourceMapper getResourceMapper() {
 		if (fResourceMapper == null) {
 			fResourceMapper = new ResourceMapper(fModel);
 		}
 		return fResourceMapper;
 	}
 
 	public FileEditorInput getPropertiesInput() {
 		return propertiesInput;
 	}
 
 	public Map<Feature, FormDecoration> getDecorationsForFeatures(Collection<Feature> keyset, int index) {
 		if (keyset.isEmpty()) {
 			return Collections.emptyMap();
 		}
 		
 		IFile file = fModel.getFile();
 		IMarker[] markers;
 		try {
 			markers = file.findMarkers(IncrementalDeploymentBuilder.PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			Activator.log(e);
 			return Collections.emptyMap();
 		}
 		
 		Map<Integer, Feature> featureIds = new HashMap<Integer, Feature>();
 		for (Feature f : keyset) {
 			featureIds.put(f.id, f);
 		}
 		
 		Map<Feature, FormDecoration> toShow = new HashMap<Feature, FormDecoration>(); 
 		for (IMarker marker : markers) {
 			int featureId = marker.getAttribute(IncrementalDeploymentBuilder.FEATURE_ID, -1);
 			int objNo = marker.getAttribute(IncrementalDeploymentBuilder.OBJECT_NUMBER, -1);
 			if ((featureId != -1) && (objNo == index)) {
 				Feature feature = featureIds.get(featureId);
 				if (feature != null) {
 					toShow.put(feature, markerToDecoration(marker));
 				}
 			}
 		}
 		
 		return toShow;
 	}
 	
 	public Map<Feature, FormDecoration> getDecorationsForFeatures(Collection<Feature> keyset) {
 		return getDecorationsForFeatures(keyset, -1);
 	}
 	
 	private FormDecoration markerToDecoration(IMarker marker) {
 		String message = marker.getAttribute(IMarker.MESSAGE, null);
 		int severity = marker.getAttribute(IMarker.SEVERITY, 0);
 		return new FormDecoration(message, severity);
 	}
 
 }
