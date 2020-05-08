 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.presentation;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.AggregatorPlugin;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.aggregator.p2.provider.MetadataRepositoryItemProvider;
 import org.eclipse.b3.aggregator.p2.provider.ProvidedCapabilityItemProvider;
 import org.eclipse.b3.aggregator.p2.provider.RequiredCapabilityItemProvider;
 import org.eclipse.b3.aggregator.p2.util.MetadataRepositoryResourceImpl;
 import org.eclipse.b3.aggregator.p2view.provider.P2viewItemProviderAdapterFactory;
 import org.eclipse.b3.aggregator.provider.AggregatorEditPlugin;
 import org.eclipse.b3.aggregator.provider.AggregatorItemProviderAdapter;
 import org.eclipse.b3.aggregator.provider.AggregatorItemProviderAdapterFactory;
 import org.eclipse.b3.aggregator.provider.TooltipTextProvider;
 import org.eclipse.b3.aggregator.util.AggregatorResource;
 import org.eclipse.b3.aggregator.util.AggregatorResourceImpl;
 import org.eclipse.b3.aggregator.util.OverlaidImage;
 import org.eclipse.b3.aggregator.util.ResourceDiagnosticImpl;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.b3.aggregator.util.StatusProviderAdapterFactory;
 import org.eclipse.b3.p2.provider.P2ItemProviderAdapterFactory;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.ui.MarkerHelper;
 import org.eclipse.emf.common.ui.celleditor.ExtendedDialogCellEditor;
 import org.eclipse.emf.common.ui.editor.ProblemEditorPart;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemFontProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProvider;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceSetItemProvider;
 import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
 import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
 import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
 import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
 import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
 import org.eclipse.emf.edit.ui.provider.PropertySource;
 import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
 import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.contexts.IContextActivation;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.eclipse.ui.views.properties.PropertySheet;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 
 /**
  * This is an example of a Aggregator model editor.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @SuppressWarnings("unused")
 public class AggregatorEditor extends MultiPageEditorPart implements IEditingDomainProvider, ISelectionProvider,
 		IMenuListener, IViewerProvider, IGotoMarker {
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public class ReverseAdapterFactoryContentProvider extends AdapterFactoryContentProvider {
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * 
 		 * @generated
 		 */
 		public ReverseAdapterFactoryContentProvider(AdapterFactory adapterFactory) {
 			super(adapterFactory);
 		}
 
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * 
 		 * @generated
 		 */
 		@Override
 		public Object[] getChildren(Object object) {
 			Object parent = super.getParent(object);
 			return (parent == null
 					? Collections.EMPTY_SET
 					: Collections.singleton(parent)).toArray();
 		}
 
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * 
 		 * @generated
 		 */
 		@Override
 		public Object[] getElements(Object object) {
 			Object parent = super.getParent(object);
 			return (parent == null
 					? Collections.EMPTY_SET
 					: Collections.singleton(parent)).toArray();
 		}
 
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * 
 		 * @generated
 		 */
 		@Override
 		public Object getParent(Object object) {
 			return null;
 		}
 
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * 
 		 * @generated
 		 */
 		@Override
 		public boolean hasChildren(Object object) {
 			Object parent = super.getParent(object);
 			return parent != null;
 		}
 	}
 
 	class AggregatorMarkerHelper extends EditUIMarkerHelper {
 		public void createMarkers(Resource markerResource, Diagnostic diagnostic) throws CoreException {
 			if(diagnostic.getChildren().isEmpty()) {
 				if(diagnostic.getSeverity() != Diagnostic.OK)
 					createMarkers(getFile(markerResource), diagnostic, null);
 			}
 			else if(diagnostic.getMessage() == null) {
 				for(Diagnostic childDiagnostic : diagnostic.getChildren()) {
 					createMarkers(markerResource, childDiagnostic);
 				}
 			}
 			else {
 				for(Diagnostic childDiagnostic : diagnostic.getChildren()) {
 					createMarkers(getFile(markerResource), childDiagnostic, diagnostic);
 				}
 			}
 		}
 
 		@Override
 		protected void adjustMarker(IMarker marker, Diagnostic diagnostic, Diagnostic parentDiagnostic)
 				throws CoreException {
 			List<?> data = diagnostic.getData();
 			StringBuilder relatedURIs = new StringBuilder();
 			boolean first = true;
 
 			for(Object object : data) {
 				String uriString = null;
 
 				if(object instanceof Resource.Diagnostic)
 					uriString = ((Resource.Diagnostic) object).getLocation();
 				else {
 					URI uri = null;
 
 					if(object instanceof EObject)
 						uri = EcoreUtil.getURI((EObject) object);
 					else if(object instanceof Resource)
 						uri = ((Resource) object).getURI();
 
 					if(uri != null)
 						uriString = uri.toString();
 				}
 
 				if(uriString != null) {
 					if(first) {
 						first = false;
 						marker.setAttribute(EValidator.URI_ATTRIBUTE, uriString);
 					}
 					else {
 						if(relatedURIs.length() != 0) {
 							relatedURIs.append(' ');
 						}
 						relatedURIs.append(URI.encodeFragment(uriString, false));
 					}
 				}
 			}
 
 			if(relatedURIs.length() > 0) {
 				marker.setAttribute(EValidator.RELATED_URIS_ATTRIBUTE, relatedURIs.toString());
 			}
 		}
 
 		@Override
 		protected String getMarkerID() {
 			return AGGREGATOR_NONPERSISTENT_PROBLEM_MARKER;
 		}
 	}
 
 	public static final String AGGREGATOR_EDITOR_SCOPE = "org.eclipse.b3.aggregator.presentation.aggregatorEditorScope";
 
 	public static final String AGGREGATOR_PROBLEM_MARKER = "org.eclipse.b3.aggregator.editor.diagnostic";
 
 	public static final String AGGREGATOR_PERSISTENT_PROBLEM_MARKER = "org.eclipse.b3.aggregator.editor.diagnostic.persistent";
 
 	public static final String AGGREGATOR_NONPERSISTENT_PROBLEM_MARKER = "org.eclipse.b3.aggregator.editor.diagnostic.nonpersistent";
 
 	/**
 	 * This looks up a string in the plugin's plugin.properties file.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	private static String getString(String key) {
 		return AggregatorEditorPlugin.INSTANCE.getString(key);
 	}
 
 	/**
 	 * This looks up a string in plugin.properties, making a substitution.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	private static String getString(String key, Object s1) {
 		return AggregatorEditorPlugin.INSTANCE.getString(key, new Object[] { s1 });
 	}
 
 	private IContextActivation contextActivation;
 
 	/**
 	 * This keeps track of the editing domain that is used to track all changes to the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected AdapterFactoryEditingDomain editingDomain;
 
 	/**
 	 * This is the one adapter factory used for providing views of the model. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	protected ComposedAdapterFactory adapterFactory;
 
 	/**
 	 * This is the content outline page.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IContentOutlinePage contentOutlinePage;
 
 	/**
 	 * This is a kludge...
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IStatusLineManager contentOutlineStatusLineManager;
 
 	/**
 	 * This is the content outline page's viewer.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected TreeViewer contentOutlineViewer;
 
 	/**
 	 * This is the property sheet page.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected PropertySheetPage propertySheetPage;
 
 	/**
 	 * This is the viewer that shadows the selection in the content outline.
 	 * The parent relation must be correctly defined for this to work.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected TreeViewer selectionViewer;
 
 	/**
 	 * This keeps track of the active content viewer, which may be either one of the viewers in the pages or the content
 	 * outline viewer.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Viewer currentViewer;
 
 	/**
 	 * This listens to which ever viewer is active.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected ISelectionChangedListener selectionChangedListener;
 
 	/**
 	 * This keeps track of all the {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are listening to
 	 * this editor.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
 	/**
 	 * This keeps track of the selection of the editor as a whole.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected ISelection editorSelection = StructuredSelection.EMPTY;
 
 	/**
 	 * The MarkerHelper is responsible for creating workspace resource markers presented in Eclipse's Problems View.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected MarkerHelper markerHelper = new EditUIMarkerHelper() {
 		@Override
 		protected String getMarkerID() {
 			return AGGREGATOR_PROBLEM_MARKER;
 		}
 	};
 
 	protected AggregatorMarkerHelper managedMarkerHelper = new AggregatorMarkerHelper();
 
 	private Pattern findIUIdPattern;
 
 	private VersionRange findIUVersionRange;
 
 	private boolean updateMarkers;
 
 	private boolean updateMarkersIsRunning;
 
 	private Job repositoryLoadingJob;
 
 	/**
 	 * This listens for when the outline becomes active <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected IPartListener partListener = new IPartListener() {
 		public void partActivated(IWorkbenchPart p) {
 			if(p instanceof ContentOutline) {
 				if(((ContentOutline) p).getCurrentPage() == contentOutlinePage) {
 					getActionBarContributor().setActiveEditor(AggregatorEditor.this);
 
 					setCurrentViewer(contentOutlineViewer);
 				}
 			}
 			else if(p instanceof PropertySheet) {
 				if(((PropertySheet) p).getCurrentPage() == propertySheetPage) {
 					getActionBarContributor().setActiveEditor(AggregatorEditor.this);
 					handleActivate();
 				}
 			}
 			else if(p == AggregatorEditor.this) {
 				handleActivate();
 			}
 		}
 
 		public void partBroughtToTop(IWorkbenchPart p) {
 			// Ignore.
 		}
 
 		public void partClosed(IWorkbenchPart p) {
 			// Ignore.
 		}
 
 		public void partDeactivated(IWorkbenchPart p) {
 			handleDeactivate();
 		}
 
 		public void partOpened(IWorkbenchPart p) {
 			// Ignore.
 		}
 	};
 
 	/**
 	 * Resources that have been removed since last activation.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<Resource> removedResources = new ArrayList<Resource>();
 
 	/**
 	 * Resources that have been changed since last activation.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<Resource> changedResources = new ArrayList<Resource>();
 
 	/**
 	 * Resources that have been saved.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<Resource> savedResources = new ArrayList<Resource>();
 
 	/**
 	 * Map to store the diagnostic associated with a resource.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
 
 	protected Map<Resource, Diagnostic> managedResourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
 
 	/**
 	 * Controls whether the problem indication should be updated.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected boolean updateProblemIndication = true;
 
 	/**
 	 * Adapter used to update the problem indication when resources are demanded loaded. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected EContentAdapter problemIndicationAdapter = new EContentAdapter() {
 		private boolean analysisStarted = false;
 
 		@Override
 		public void notifyChanged(Notification notification) {
 			if(notification.getNotifier() instanceof Resource) {
 				int featureID = notification.getFeatureID(Resource.class);
 
 				// If a repository is loaded, force updating the context menu by setting selection to current selection
 				if(featureID == Resource.RESOURCE__IS_LOADED) {
 					getSite().getShell().getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							setSelection(currentViewer == null
 									? StructuredSelection.EMPTY
 									: currentViewer.getSelection());
 						}
 					});
 				}
 
 				switch(featureID) {
 					case AggregatorResource.RESOURCE__ANALYSIS_STARTED:
 						analysisStarted = true;
 						break;
 					case AggregatorResource.RESOURCE__ANALYSIS_FINISHED:
 						analysisStarted = false;
 					case Resource.RESOURCE__RESOURCE_SET:
 					case Resource.RESOURCE__IS_LOADED:
 					case Resource.RESOURCE__ERRORS:
 					case Resource.RESOURCE__WARNINGS: {
 						Resource resource = (Resource) notification.getNotifier();
 
 						// filter out notifications when analysing aggregator repository
 						if(resource instanceof AggregatorResourceImpl && analysisStarted)
 							return;
 
 						if(resource instanceof AggregatorResourceImpl) {
 							Diagnostic diagnostic = analyzeResourceProblems(resource, null);
 
 							if(diagnostic.getSeverity() != Diagnostic.OK) {
 								resourceToDiagnosticMap.put(resource, diagnostic);
 							}
 							else {
 								resourceToDiagnosticMap.remove(resource);
 							}
 
 							if(updateProblemIndication) {
 								getSite().getShell().getDisplay().asyncExec(new Runnable() {
 									public void run() {
 										updateProblemIndication();
 									}
 								});
 							}
 						}
 
 						if(resource.getResourceSet() == null)
 							managedResourceToDiagnosticMap.remove(resource);
 						else {
 							Diagnostic diagnostic = analyzeResourceProblems(resource, null, true);
 
 							if(diagnostic.getSeverity() != Diagnostic.OK)
 								managedResourceToDiagnosticMap.put(resource, diagnostic);
 							else
 								managedResourceToDiagnosticMap.remove(resource);
 						}
 
 						updateMarkers();
 						break;
 					}
 				}
 			}
 			else {
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
 
 		private void updateMarkers() {
 			Runnable runnable = new Runnable() {
 				public void run() {
 					synchronized(AggregatorEditor.this) {
 						while(updateMarkers)
 							try {
 								updateMarkers = false;
 								updateMarkersIsRunning = true;
 
 								// only aggregatorResource is a file and can be used for markers
 								Resource aggregatorResource = editingDomain.getResourceSet().getResources().get(0);
 								BasicDiagnostic basicDiagnostic = new BasicDiagnostic(
 									Diagnostic.OK, "org.eclipse.b3.aggregator.editor", 0, null, null);
 
 								for(Diagnostic diagnostic : managedResourceToDiagnosticMap.values())
 									basicDiagnostic.add(diagnostic);
 
 								managedMarkerHelper.deleteMarkers(aggregatorResource);
 								if(basicDiagnostic.getSeverity() != Diagnostic.OK) {
 									try {
 										managedMarkerHelper.createMarkers(aggregatorResource, basicDiagnostic);
 									}
 									catch(CoreException exception) {
 										AggregatorEditorPlugin.INSTANCE.log(exception);
 									}
 								}
 							}
 							finally {
 								updateMarkersIsRunning = false;
 							}
 					}
 				}
 			};
 
 			if(updateMarkers)
 				return;
 
 			updateMarkers = true;
 
 			if(updateMarkersIsRunning)
 				return;
 
 			new Thread(runnable).start();
 		}
 	};
 
 	/**
 	 * This listens for workspace changes.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
 		public void resourceChanged(IResourceChangeEvent event) {
 			IResourceDelta delta = event.getDelta();
 			try {
 				class ResourceDeltaVisitor implements IResourceDeltaVisitor {
 					protected ResourceSet resourceSet = editingDomain.getResourceSet();
 
 					protected Collection<Resource> changedResources = new ArrayList<Resource>();
 
 					protected Collection<Resource> removedResources = new ArrayList<Resource>();
 
 					public Collection<Resource> getChangedResources() {
 						return changedResources;
 					}
 
 					public Collection<Resource> getRemovedResources() {
 						return removedResources;
 					}
 
 					public boolean visit(IResourceDelta delta) {
 						if(delta.getResource().getType() == IResource.FILE) {
 							if(delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.CHANGED &&
 									delta.getFlags() != IResourceDelta.MARKERS) {
 								Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(
 									delta.getFullPath().toString(), true), false);
 								if(resource != null) {
 									if(delta.getKind() == IResourceDelta.REMOVED) {
 										removedResources.add(resource);
 									}
 									else if(!savedResources.remove(resource)) {
 										changedResources.add(resource);
 									}
 								}
 							}
 						}
 
 						return true;
 					}
 				}
 
 				final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
 				delta.accept(visitor);
 
 				if(!visitor.getRemovedResources().isEmpty()) {
 					getSite().getShell().getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							removedResources.addAll(visitor.getRemovedResources());
 							if(!isDirty()) {
 								getSite().getPage().closeEditor(AggregatorEditor.this, false);
 							}
 						}
 					});
 				}
 
 				if(!visitor.getChangedResources().isEmpty()) {
 					getSite().getShell().getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							changedResources.addAll(visitor.getChangedResources());
 							if(getSite().getPage().getActiveEditor() == AggregatorEditor.this) {
 								handleActivate();
 							}
 						}
 					});
 				}
 			}
 			catch(CoreException exception) {
 				AggregatorEditorPlugin.INSTANCE.log(exception);
 			}
 		}
 	};
 
 	/**
 	 * This creates a model editor.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AggregatorEditor() {
 		super();
 		initializeEditingDomain();
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.add(listener);
 	}
 
 	/**
 	 * Returns a diagnostic describing the errors and warnings listed in the resource and the specified exception (if
 	 * any). <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Diagnostic analyzeResourceProblems(Resource resource, Exception exception) {
 		return analyzeResourceProblems(resource, exception, false);
 	}
 
 	public Diagnostic analyzeResourceProblems(Resource resource, Exception exception, boolean managedProblems) {
 		if(resource != null && (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty())) {
 			synchronized(resource) {
 				BasicDiagnostic basicDiagnostic = new BasicDiagnostic(
 					Diagnostic.ERROR, "org.eclipse.b3.aggregator.editor", 0, getString(
 						"_UI_CreateModelError_message", resource.getURI()), new Object[] { exception == null
 							? (Object) resource
 							: exception });
 				Diagnostic diagnostic = computeDiagnostic(resource, true, true, managedProblems);
 
 				if(diagnostic.getSeverity() == Diagnostic.OK &&
 						(diagnostic.getChildren() == null || diagnostic.getChildren().size() == 0))
 					return Diagnostic.OK_INSTANCE;
 
 				basicDiagnostic.merge(diagnostic);
 				return basicDiagnostic;
 			}
 		}
 		else if(exception != null) {
 			return new BasicDiagnostic(Diagnostic.ERROR, "org.eclipse.b3.aggregator.editor", 0, getString(
 				"_UI_CreateModelError_message", resource == null
 						? null
 						: resource.getURI()), new Object[] { exception });
 		}
 		else {
 			return Diagnostic.OK_INSTANCE;
 		}
 	}
 
 	/**
 	 * Get model instance resource and then initialize all repositories contained in the model using a progress bar.
 	 */
 	public void createModel() {
 
 		// get model instance resource
 		URI resourceURI = EditUIUtil.getURI(getEditorInput());
 		Exception exception = null;
 		Resource resource = null;
 		if(ResourceUtils.isCurrentModel(resourceURI))
 			try {
 				// Load the resource through the editing domain.
 				//
 				resource = editingDomain.getResourceSet().getResource(resourceURI, true);
 			}
 			catch(Exception e) {
 				exception = e;
 				resource = editingDomain.getResourceSet().getResource(resourceURI, false);
 			}
 		else {
 			TransformationWizard tz = new TransformationWizard(resourceURI);
 			tz.init(getEditorSite().getWorkbenchWindow().getWorkbench(), (IStructuredSelection) getSelection());
 
 			WizardDialog wd = new WizardDialog(getEditorSite().getShell(), tz);
 			wd.setHelpAvailable(false);
 
 			if(wd.open() == 0) {
 				resource = tz.getTargetResource();
 				editingDomain.getResourceSet().getResources().add(resource);
 
 				IFile modelFile = tz.getModelFile();
 				setPartName(modelFile.getName());
 				setInput(new FileEditorInput(modelFile));
 			}
 			else {
 				throw new RuntimeException("Deprecated resource was not transformed");
 			}
 		}
 
 		Diagnostic diagnostic = analyzeResourceProblems(resource, exception);
 		if(diagnostic.getSeverity() != Diagnostic.OK) {
 			resourceToDiagnosticMap.put(resource, diagnostic);
 		}
 		editingDomain.getResourceSet().eAdapters().add(problemIndicationAdapter);
 
 		// initialize all repositories
 		resourceURI = EditUIUtil.getURI(getEditorInput());
 		resource = editingDomain.getResourceSet().getResource(resourceURI, false);
 
 		if(resource != null) {
 			EList<EObject> contents = resource.getContents();
 			if(contents.size() == 1 && contents.get(0) instanceof Aggregator) {
 				Aggregator aggregator = (Aggregator) contents.get(0);
 
 				// initialize item providers for all MDR references so that they could handle notifications from
 				// repository loaders
 				// + set all proxies to null so that no one tries to resolve them (the MDR's will be set by loaders)
 				for(MetadataRepositoryReference mdrReference : aggregator.getAllMetadataRepositoryReferences(false)) {
 					mdrReference.setMetadataRepository(null);
 					adapterFactory.adapt(mdrReference, IItemLabelProvider.class);
 				}
 
 				final List<MetadataRepositoryReference> repositoriesToLoad = aggregator.getAllMetadataRepositoryReferences(true);
 				repositoryLoadingJob = new Job("Loading repositories...") {
 					@Override
 					protected IStatus run(IProgressMonitor monitor) {
 						try {
 							Collections.sort(repositoriesToLoad, new Comparator<MetadataRepositoryReference>() {
 
 								public int compare(MetadataRepositoryReference mdr1, MetadataRepositoryReference mdr2) {
 									String location1 = mdr1 != null
 											? mdr1.getResolvedLocation()
 											: null;
 									if(location1 == null)
 										location1 = "";
 									String location2 = mdr2 != null
 											? mdr2.getResolvedLocation()
 											: null;
 									if(location2 == null)
 										location2 = "";
 
 									return location1.compareTo(location2);
 								}
 
 							});
 
 							// initialize all resources in alphabetical order
 							for(MetadataRepositoryReference repo : repositoriesToLoad) {
 								MetadataRepositoryResourceImpl res = (MetadataRepositoryResourceImpl) MetadataRepositoryResourceImpl.getResourceForNatureAndLocation(
 									repo.getNature(), repo.getResolvedLocation(), repo.getAggregator());
 								if(monitor.isCanceled())
 									break;
 								res.startAsynchronousLoad(false);
 							}
 
 							return Status.OK_STATUS;
 						}
 						finally {
 							if(monitor != null)
 								monitor.done();
 						}
 					}
 				};
 
 				repositoryLoadingJob.setSystem(true);
 			}
 		}
 	}
 
 	/**
 	 * This is the method used by the framework to install your own controls. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public void createPages() {
 		createPagesGen();
 
 		selectionViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				TreePath path = ((TreeSelection) event.getSelection()).getPaths()[0];
 
 				if(selectionViewer.getExpandedState(path))
 					selectionViewer.collapseToLevel(path, 1);
 				else
 					selectionViewer.expandToLevel(path, 1);
 			}
 		});
 
 		selectionViewer.getTree().addMouseMoveListener(new MouseMoveListener() {
 			private TreeItem lastTreeItem;
 
 			public void mouseMove(MouseEvent e) {
 				TreeItem item = selectionViewer.getTree().getItem(new Point(e.x, e.y));
 
 				if(item == lastTreeItem)
 					return;
 
 				String toolTipText = null;
 
 				if(item != null && item.getData() != null) {
 					IEditingDomainItemProvider provider = (IEditingDomainItemProvider) adapterFactory.getRootAdapterFactory().adapt(
 						item.getData(), IEditingDomainItemProvider.class);
 
 					if(provider != null && provider instanceof TooltipTextProvider)
 						toolTipText = ((TooltipTextProvider) provider).getTooltipText(item.getData());
 				}
 
 				selectionViewer.getTree().setToolTipText(toolTipText);
 
 				lastTreeItem = item;
 			}
 		});
 
 		if(repositoryLoadingJob != null) {
 			repositoryLoadingJob.schedule();
 			repositoryLoadingJob = null;
 		}
 	}
 
 	/**
 	 * This is the method used by the framework to install your own controls. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	public void createPagesGen() {
 		// Creates the model from the editor input
 		//
 		createModel();
 
 		// Only creates the other pages if there is something that can be edited
 		//
 		if(!getEditingDomain().getResourceSet().getResources().isEmpty()) {
 			// Create a page for the selection tree view.
 			//
 			Tree tree = new Tree(getContainer(), SWT.MULTI);
 			selectionViewer = new TreeViewer(tree);
 			setCurrentViewer(selectionViewer);
 
 			selectionViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
 			selectionViewer.setLabelProvider(new AdapterFactoryLabelProvider.FontAndColorProvider(
 				adapterFactory, selectionViewer));
 			selectionViewer.setInput(editingDomain.getResourceSet());
 			selectionViewer.setSelection(
 				new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);
 
 			new AdapterFactoryTreeEditor(selectionViewer.getTree(), adapterFactory);
 
 			createContextMenuFor(selectionViewer);
 			int pageIndex = addPage(tree);
 			setPageText(pageIndex, getString("_UI_SelectionPage_label"));
 
 			getSite().getShell().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					setActivePage(0);
 				}
 			});
 		}
 
 		// Ensures that this editor will only display the page's tab
 		// area if there are more than one page
 		//
 		getContainer().addControlListener(new ControlAdapter() {
 			boolean guard = false;
 
 			@Override
 			public void controlResized(ControlEvent event) {
 				if(!guard) {
 					guard = true;
 					hideTabs();
 					guard = false;
 				}
 			}
 		});
 
 		getSite().getShell().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				updateProblemIndication();
 			}
 		});
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void dispose() {
 		updateProblemIndication = false;
 
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
 
 		getSite().getPage().removePartListener(partListener);
 
 		adapterFactory.dispose();
 
 		if(getActionBarContributor().getActiveEditor() == this) {
 			getActionBarContributor().setActiveEditor(null);
 		}
 
 		if(propertySheetPage != null) {
 			propertySheetPage.dispose();
 		}
 
 		if(contentOutlinePage != null) {
 			contentOutlinePage.dispose();
 		}
 
 		super.dispose();
 	}
 
 	/**
 	 * This is for implementing {@link IEditorPart} and simply saves the model file.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void doSave(IProgressMonitor progressMonitor) {
 		// Save only resources that have actually changed.
 		//
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 
 		// Do the work within an operation because this is a long running activity that modifies the workbench.
 		//
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			// This is the method that gets invoked when the operation runs.
 			//
 			@Override
 			public void execute(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				//
 				boolean first = true;
 				for(Resource resource : editingDomain.getResourceSet().getResources()) {
 					if((first || !resource.getContents().isEmpty() || isPersisted(resource)) &&
 							!editingDomain.isReadOnly(resource)) {
 						try {
 							long timeStamp = resource.getTimeStamp();
 							resource.save(saveOptions);
 							if(resource.getTimeStamp() != timeStamp) {
 								savedResources.add(resource);
 							}
 						}
 						catch(Exception exception) {
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
 		}
 		catch(Exception exception) {
 			// Something went wrong that shouldn't.
 			//
 			AggregatorEditorPlugin.INSTANCE.log(exception);
 		}
 		updateProblemIndication = true;
 		updateProblemIndication();
 	}
 
 	/**
 	 * This also changes the editor's input.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void doSaveAs() {
 		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
 		saveAsDialog.open();
 		IPath path = saveAsDialog.getResult();
 		if(path != null) {
 			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 			if(file != null) {
 				doSaveAs(URI.createPlatformResourceURI(file.getFullPath().toString(), true), new FileEditorInput(file));
 			}
 		}
 	}
 
 	public boolean findNextIU(boolean forward) {
 		if(findIUIdPattern == null || findIUVersionRange == null)
 			return false;
 
 		TreeSelection treeSelection = (TreeSelection) getSelection();
 		Object[] currentSelection = null;
 		if(treeSelection != null && treeSelection.getPaths().length > 0) {
 			TreePath path = treeSelection.getPaths()[0];
 			currentSelection = new Object[path.getSegmentCount()];
 			for(int i = 0; i < path.getSegmentCount(); i++)
 				currentSelection[i] = path.getSegment(i);
 		}
 
 		boolean firstResourceFound = false;
 		Object[] foundNode = null;
 
 		List<Resource> resources = editingDomain.getResourceSet().getResources();
 		if(!forward) {
 			List<Resource> reverseResources = new ArrayList<Resource>();
 
 			for(int i = resources.size(); i > 0; i--)
 				reverseResources.add(resources.get(i - 1));
 
 			resources = reverseResources;
 		}
 
 		for(Resource resource : resources) {
 			if(!(resource instanceof MetadataRepositoryResourceImpl))
 				continue;
 
 			if(!firstResourceFound)
 				if(currentSelection == null || !(currentSelection[0] instanceof MetadataRepositoryResourceImpl) ||
 						resource == currentSelection[0])
 					firstResourceFound = true;
 				else
 					continue;
 
 			foundNode = ((MetadataRepositoryResourceImpl) resource).findIUPresentation(
 				findIUIdPattern, findIUVersionRange, currentSelection, forward);
 			if(foundNode != null)
 				break;
 		}
 
 		if(foundNode != null) {
 			getViewer().setSelection(new TreeSelection(new TreePath(foundNode)), true);
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EditingDomainActionBarContributor getActionBarContributor() {
 		return (EditingDomainActionBarContributor) getEditorSite().getActionBarContributor();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IActionBars getActionBars() {
 		return getActionBarContributor().getActionBars();
 	}
 
 	/**
 	 * This is how the framework determines which interfaces we implement.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class key) {
 		if(key.equals(IContentOutlinePage.class)) {
 			return showOutlineView()
 					? getContentOutlinePage()
 					: null;
 		}
 		else if(key.equals(IPropertySheetPage.class)) {
 			return getPropertySheetPage();
 		}
 		else if(key.equals(IGotoMarker.class)) {
 			return this;
 		}
 		else {
 			return super.getAdapter(key);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AdapterFactory getAdapterFactory() {
 		return adapterFactory;
 	}
 
 	/**
 	 * This accesses a cached version of the content outliner.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IContentOutlinePage getContentOutlinePage() {
 		if(contentOutlinePage == null) {
 			// The content outline is just a tree.
 			//
 			class MyContentOutlinePage extends ContentOutlinePage {
 				@Override
 				public void createControl(Composite parent) {
 					super.createControl(parent);
 					contentOutlineViewer = getTreeViewer();
 					contentOutlineViewer.addSelectionChangedListener(this);
 
 					// Set up the tree viewer.
 					//
 					contentOutlineViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
 					contentOutlineViewer.setLabelProvider(new AdapterFactoryLabelProvider.FontAndColorProvider(
 						adapterFactory, contentOutlineViewer));
 					contentOutlineViewer.setInput(editingDomain.getResourceSet());
 
 					// Make sure our popups work.
 					//
 					createContextMenuFor(contentOutlineViewer);
 
 					if(!editingDomain.getResourceSet().getResources().isEmpty()) {
 						// Select the root object in the view.
 						//
 						contentOutlineViewer.setSelection(new StructuredSelection(
 							editingDomain.getResourceSet().getResources().get(0)), true);
 					}
 				}
 
 				@Override
 				public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager,
 						IStatusLineManager statusLineManager) {
 					super.makeContributions(menuManager, toolBarManager, statusLineManager);
 					contentOutlineStatusLineManager = statusLineManager;
 				}
 
 				@Override
 				public void setActionBars(IActionBars actionBars) {
 					super.setActionBars(actionBars);
 					getActionBarContributor().shareGlobalActions(this, actionBars);
 				}
 			}
 
 			contentOutlinePage = new MyContentOutlinePage();
 
 			// Listen to selection so that we can handle it is a special way.
 			//
 			contentOutlinePage.addSelectionChangedListener(new ISelectionChangedListener() {
 				// This ensures that we handle selections correctly.
 				//
 				public void selectionChanged(SelectionChangedEvent event) {
 					handleContentOutlineSelection(event.getSelection());
 				}
 			});
 		}
 
 		return contentOutlinePage;
 	}
 
 	/**
 	 * This returns the editing domain as required by the {@link IEditingDomainProvider} interface.
 	 * This is important for implementing the static methods of {@link AdapterFactoryEditingDomain} and for supporting
 	 * {@link org.eclipse.emf.edit.ui.action.CommandAction}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * This accesses a cached version of the property sheet. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public IPropertySheetPage getPropertySheetPage() {
 		if(propertySheetPage == null) {
 			propertySheetPage = new ExtendedPropertySheetPage(editingDomain) {
 				@Override
 				public void setActionBars(IActionBars actionBars) {
 					super.setActionBars(actionBars);
 					getActionBarContributor().shareGlobalActions(this, actionBars);
 				}
 
 				@Override
 				public void setSelectionToViewer(List<?> selection) {
 					AggregatorEditor.this.setSelectionToViewer(selection);
 					AggregatorEditor.this.setFocus();
 				}
 			};
 			propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(adapterFactory) {
 				@Override
 				protected IPropertySource createPropertySource(Object object, IItemPropertySource itemPropertySource) {
 					return new PropertySource(object, itemPropertySource) {
 						@Override
 						protected IPropertyDescriptor createPropertyDescriptor(
 								IItemPropertyDescriptor itemPropertyDescriptor) {
 							return new PropertyDescriptor(object, itemPropertyDescriptor) {
 								@Override
 								public CellEditor createPropertyEditor(Composite composite) {
 									if(!itemPropertyDescriptor.canSetProperty(object)) {
 										return null;
 									}
 
 									final EStructuralFeature feature = (EStructuralFeature) itemPropertyDescriptor.getFeature(object);
 									final EClassifier eType = feature.getEType();
 
 									if(VersionRange.class.isAssignableFrom(eType.getInstanceClass())) {
 										final ILabelProvider editLabelProvider = getEditLabelProvider();
 
 										return new ExtendedDialogCellEditor(composite, editLabelProvider) {
 											@Override
 											protected Object openDialogBox(Control cellEditorWindow) {
 												VersionRangeEditorDialog dialog = new VersionRangeEditorDialog(
 													cellEditorWindow.getShell(), editLabelProvider, doGetValue());
 												dialog.open();
 												return dialog.getResult();
 											}
 										};
 									}
 
 									return super.createPropertyEditor(composite);
 								}
 							};
 						}
 
 					};
 				}
 			});
 		}
 
 		return propertySheetPage;
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to return this editor's overall selection.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ISelection getSelection() {
 		return editorSelection;
 	}
 
 	/**
 	 * This returns the viewer as required by the {@link IViewerProvider} interface.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Viewer getViewer() {
 		return currentViewer;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void gotoMarker(IMarker marker) {
 		try {
 			if(marker.getType().equals(EValidator.MARKER) || marker.getType().startsWith(AGGREGATOR_PROBLEM_MARKER)) {
 				String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
 				if(uriAttribute != null) {
 					URI uri = URI.createURI(uriAttribute);
 					if(uri.fragment() != null) {
 						EObject eObject = editingDomain.getResourceSet().getEObject(uri, true);
 						if(eObject != null)
 							setSelectionToViewer(Collections.singleton(editingDomain.getWrapper(eObject)));
 					}
 					else {
 						Resource resource = findResource(uri);
 						if(resource != null)
 							setSelectionToViewer(Collections.singleton(resource));
 					}
 				}
 			}
 		}
 		catch(CoreException exception) {
 			AggregatorEditorPlugin.INSTANCE.log(exception);
 		}
 	}
 
 	/**
 	 * This deals with how we want selection in the outliner to affect the other views.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void handleContentOutlineSelection(ISelection selection) {
 		if(selectionViewer != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
 			Iterator<?> selectedElements = ((IStructuredSelection) selection).iterator();
 			if(selectedElements.hasNext()) {
 				// Get the first selected element.
 				//
 				Object selectedElement = selectedElements.next();
 
 				ArrayList<Object> selectionList = new ArrayList<Object>();
 				selectionList.add(selectedElement);
 				while(selectedElements.hasNext()) {
 					selectionList.add(selectedElements.next());
 				}
 
 				// Set the selection to the widget.
 				//
 				selectionViewer.setSelection(new StructuredSelection(selectionList));
 			}
 		}
 	}
 
 	/**
 	 * This is called during startup.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void init(IEditorSite site, IEditorInput editorInput) {
 		setSite(site);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		site.setSelectionProvider(this);
 		site.getPage().addPartListener(partListener);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 			resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/**
 	 * This is for implementing {@link IEditorPart} and simply tests the command stack.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean isDirty() {
 		return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 	}
 
 	/**
 	 * This always returns true because it is not currently supported.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.action.IMenuListener} to help fill the context menus with contributions
 	 * from the Edit menu.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void menuAboutToShow(IMenuManager menuManager) {
 		((IMenuListener) getEditorSite().getActionBarContributor()).menuAboutToShow(menuManager);
 	}
 
 	public void registerFindIUArguments(Pattern idPattern, VersionRange versionRange) {
 		findIUIdPattern = idPattern;
 		findIUVersionRange = versionRange;
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.remove(listener);
 	}
 
 	/**
 	 * This makes sure that one content viewer, either for the current page or the outline view, if it has focus,
 	 * is the current one.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setCurrentViewer(Viewer viewer) {
 		// If it is changing...
 		//
 		if(currentViewer != viewer) {
 			if(selectionChangedListener == null) {
 				// Create the listener on demand.
 				//
 				selectionChangedListener = new ISelectionChangedListener() {
 					// This just notifies those things that are affected by the section.
 					//
 					public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
 						setSelection(selectionChangedEvent.getSelection());
 					}
 				};
 			}
 
 			// Stop listening to the old one.
 			//
 			if(currentViewer != null) {
 				currentViewer.removeSelectionChangedListener(selectionChangedListener);
 			}
 
 			// Start listening to the new one.
 			//
 			if(viewer != null) {
 				viewer.addSelectionChangedListener(selectionChangedListener);
 			}
 
 			// Remember it.
 			//
 			currentViewer = viewer;
 
 			// Set the editors selection based on the current viewer's selection.
 			//
 			setSelection(currentViewer == null
 					? StructuredSelection.EMPTY
 					: currentViewer.getSelection());
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void setFocus() {
 		getControl(getActivePage()).setFocus();
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to set this editor's overall selection.
 	 * Calling this result will notify the listeners.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setSelection(ISelection selection) {
 		editorSelection = selection;
 
 		for(ISelectionChangedListener listener : selectionChangedListeners) {
 			listener.selectionChanged(new SelectionChangedEvent(this, selection));
 		}
 		setStatusLineManager(selection);
 	}
 
 	/**
 	 * This sets the selection into whichever viewer is active.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setSelectionToViewer(Collection<?> collection) {
 		final Collection<?> theSelection = collection;
 		// Make sure it's okay.
 		//
 		if(theSelection != null && !theSelection.isEmpty()) {
 			Runnable runnable = new Runnable() {
 				public void run() {
 					// Try to select the items in the current content viewer of the editor.
 					//
 					if(currentViewer != null) {
 						currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
 					}
 				}
 			};
 			getSite().getShell().getDisplay().asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setStatusLineManager(ISelection selection) {
 		IStatusLineManager statusLineManager = currentViewer != null && currentViewer == contentOutlineViewer
 				? contentOutlineStatusLineManager
 				: getActionBars().getStatusLineManager();
 
 		if(statusLineManager != null) {
 			if(selection instanceof IStructuredSelection) {
 				Collection<?> collection = ((IStructuredSelection) selection).toList();
 				switch(collection.size()) {
 					case 0: {
 						statusLineManager.setMessage(getString("_UI_NoObjectSelected"));
 						break;
 					}
 					case 1: {
 						String text = new AdapterFactoryItemDelegator(adapterFactory).getText(collection.iterator().next());
 						statusLineManager.setMessage(getString("_UI_SingleObjectSelected", text));
 						break;
 					}
 					default: {
 						statusLineManager.setMessage(getString(
 							"_UI_MultiObjectSelected", Integer.toString(collection.size())));
 						break;
 					}
 				}
 			}
 			else {
 				statusLineManager.setMessage("");
 			}
 		}
 	}
 
 	/**
 	 * This creates a context menu for the viewer and adds a listener as well registering the menu for extension. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void createContextMenuFor(StructuredViewer viewer) {
 		MenuManager contextMenu = new MenuManager("#PopUp");
 		contextMenu.add(new Separator("additions"));
 		contextMenu.setRemoveAllWhenShown(true);
 		contextMenu.addMenuListener(this);
 		Menu menu = contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
 
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
 		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
 		viewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(editingDomain, viewer));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void doSaveAs(URI uri, IEditorInput editorInput) {
 		(editingDomain.getResourceSet().getResources().get(0)).setURI(uri);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		IProgressMonitor progressMonitor = getActionBars().getStatusLineManager() != null
 				? getActionBars().getStatusLineManager().getProgressMonitor()
 				: new NullProgressMonitor();
 		doSave(progressMonitor);
 	}
 
 	/**
 	 * This is here for the listener to be able to call it.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void firePropertyChange(int action) {
 		super.firePropertyChange(action);
 	}
 
 	/**
 	 * Handles activation of the editor or it's associated views. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected void handleActivate() {
 		// Recompute the read only state.
 		//
 		if(editingDomain.getResourceToReadOnlyMap() != null) {
 			editingDomain.getResourceToReadOnlyMap().clear();
 
 			// Refresh any actions that may become enabled or disabled.
 			//
 			setSelection(getSelection());
 		}
 
 		if(!removedResources.isEmpty()) {
 			if(handleDirtyConflict()) {
 				getSite().getPage().closeEditor(AggregatorEditor.this, false);
 			}
 			else {
 				removedResources.clear();
 				changedResources.clear();
 				savedResources.clear();
 			}
 		}
 		else if(!changedResources.isEmpty()) {
 			changedResources.removeAll(savedResources);
 			handleChangedResources();
 			changedResources.clear();
 			savedResources.clear();
 		}
 
 		AggregatorEditorPlugin.INSTANCE.setActiveEditingDomain(editingDomain);
 		contextActivation = ((IContextService) getSite().getWorkbenchWindow().getWorkbench().getAdapter(
 			IContextService.class)).activateContext(AGGREGATOR_EDITOR_SCOPE);
 	}
 
 	/**
 	 * Handles what to do with changed resources on activation.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void handleChangedResources() {
 		if(!changedResources.isEmpty() && (!isDirty() || handleDirtyConflict())) {
 			if(isDirty()) {
 				changedResources.addAll(editingDomain.getResourceSet().getResources());
 			}
 			editingDomain.getCommandStack().flush();
 
 			updateProblemIndication = false;
 			for(Resource resource : changedResources) {
 				if(resource.isLoaded()) {
 					resource.unload();
 					try {
 						resource.load(Collections.EMPTY_MAP);
 					}
 					catch(IOException exception) {
 						if(!resourceToDiagnosticMap.containsKey(resource)) {
 							resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
 						}
 					}
 				}
 			}
 
 			if(AdapterFactoryEditingDomain.isStale(editorSelection)) {
 				setSelection(StructuredSelection.EMPTY);
 			}
 
 			updateProblemIndication = true;
 			updateProblemIndication();
 		}
 	}
 
 	protected void handleDeactivate() {
 		AggregatorEditorPlugin.INSTANCE.setActiveEditingDomain(null);
 
 		if(contextActivation != null)
 			((IContextService) getSite().getWorkbenchWindow().getWorkbench().getAdapter(IContextService.class)).deactivateContext(contextActivation);
 	}
 
 	/**
 	 * Shows a dialog that asks if conflicting changes should be discarded. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	protected boolean handleDirtyConflict() {
 		return MessageDialog.openQuestion(
 			getSite().getShell(), getString("_UI_FileConflict_label"), getString("_WARN_FileConflict"));
 	}
 
 	/**
 	 * If there is just one page in the multi-page editor part, this hides the single tab at the bottom. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void hideTabs() {
 		if(getPageCount() <= 1) {
 			setPageText(0, "");
 			if(getContainer() instanceof CTabFolder) {
 				((CTabFolder) getContainer()).setTabHeight(1);
 				Point point = getContainer().getSize();
 				getContainer().setSize(point.x, point.y + 6);
 			}
 		}
 	}
 
 	/**
 	 * This sets up the editing domain for the model editor. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected void initializeEditingDomain() {
 		// Create an adapter factory that yields item providers.
 		//
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		// Assign specific images to resources
 		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory() {
 			class ResourceItemProviderWithFontSupport extends ResourceItemProvider implements IItemFontProvider,
 					TooltipTextProvider {
 				ResourceItemProviderWithFontSupport(AdapterFactory adapterFactory) {
 					super(adapterFactory);
 				}
 
 				@Override
 				public Object getFont(Object object) {
 					if(object instanceof MetadataRepositoryResourceImpl) {
 						MetadataRepositoryResourceImpl mdr = (MetadataRepositoryResourceImpl) object;
 
 						if(mdr.getStatus().getCode() == StatusCode.WAITING)
 							return IItemFontProvider.ITALIC_FONT;
 					}
 					return null;
 				}
 
 				@Override
 				public Object getImage(Object object) {
 					Object baseImage = null;
 					Object overlayImage = null;
 
 					if(object instanceof AggregatorResourceImpl) {
 						baseImage = super.getImage(object);
 
 						// avoid querying proxies before loading jobs are scheduled/running
 						if(!Job.getJobManager().isSuspended()) {
 							AggregatorResourceImpl res = (AggregatorResourceImpl) object;
 
 							if(res.getContents().size() > 0 &&
 									((Aggregator) res.getContents().get(0)).getStatus().getCode() != StatusCode.OK)
 								overlayImage = AggregatorEditPlugin.INSTANCE.getImage("full/ovr16/Error");
 						}
 					}
 					else if(object instanceof MetadataRepositoryResourceImpl) {
 						baseImage = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/MetadataRepository");
 
 						MetadataRepositoryResourceImpl mdr = (MetadataRepositoryResourceImpl) object;
 
 						if(mdr.getLastException() != null)
 							overlayImage = AggregatorEditPlugin.INSTANCE.getImage("full/ovr16/Error");
 						else if(mdr.getStatus().getCode() == StatusCode.WAITING)
 							overlayImage = AggregatorEditPlugin.INSTANCE.getImage("full/ovr16/Loading");
 					}
 
 					if(baseImage != null) {
 						if(overlayImage != null) {
 							Object[] images = new Object[2];
 							int[] positions = new int[2];
 
 							images[0] = baseImage;
 							positions[0] = OverlaidImage.BASIC;
 
 							images[1] = overlayImage;
 							positions[1] = OverlaidImage.OVERLAY_BOTTOM_RIGHT;
 
 							return new OverlaidImage(images, positions);
 						}
 
 						return baseImage;
 					}
 
 					return super.getImage(object);
 				}
 
 				@Override
 				public String getText(Object object) {
 					String text = super.getText(object);
 					if(object instanceof MetadataRepositoryResourceImpl)
 						return super.getText(object).substring(AggregatorPlugin.B3AGGR_URI_SCHEME.length() + 1);
 
 					return text;
 				}
 
 				public String getTooltipText(Object object) {
 					return AggregatorItemProviderAdapter.getTooltipText(object, this);
 				}
 
 				@Override
 				public void notifyChanged(Notification notification) {
 					super.notifyChanged(notification);
 
 					if(notification.getEventType() == Notification.SET &&
 							notification.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED) {
 						fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 
						ResourceSet resourceSet = ((Resource) notification.getNotifier()).getResourceSet();

						// if the resource is already excluded from the resource set, ignore this notification
						if(resourceSet == null)
							return;

						Aggregator aggregator = (Aggregator) resourceSet.getResources().get(0).getContents().get(0);
 
 						for(MetadataRepositoryReference mdr : aggregator.getAllMetadataRepositoryReferences(true))
 							fireNotifyChanged(new ViewerNotification(notification, mdr, false, true));
 					}
 				}
 			}
 
 			{
 				supportedTypes.add(IItemFontProvider.class);
 			}
 
 			@Override
 			public Adapter createResourceAdapter() {
 				return new ResourceItemProviderWithFontSupport(this);
 			}
 
 			// Present only the main resource and loaded MDR's (not detached contributions)
 			@Override
 			public Adapter createResourceSetAdapter() {
 				return new ResourceSetItemProvider(this) {
 					@Override
 					public Collection<?> getChildren(Object object) {
 						ResourceSet resourceSet = (ResourceSet) object;
 
 						List<Resource> filtered = new ArrayList<Resource>();
 						for(Resource resource : resourceSet.getResources())
 							if(resource instanceof AggregatorResourceImpl ||
 									resource instanceof MetadataRepositoryResourceImpl)
 								filtered.add(resource);
 
 						return filtered;
 					}
 				};
 			}
 		});
 		adapterFactory.addAdapterFactory(new StatusProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new AggregatorItemProviderAdapterFactory());
 
 		// override item providers that should be more specific to aggregator
 		adapterFactory.addAdapterFactory(new P2ItemProviderAdapterFactory() {
 			@Override
 			public Adapter createMetadataRepositoryAdapter() {
 				if(metadataRepositoryItemProvider == null) {
 					metadataRepositoryItemProvider = new MetadataRepositoryItemProvider(this);
 				}
 
 				return metadataRepositoryItemProvider;
 			}
 
 			@Override
 			public Adapter createProvidedCapabilityAdapter() {
 				if(providedCapabilityItemProvider == null) {
 					providedCapabilityItemProvider = new ProvidedCapabilityItemProvider(this);
 				}
 
 				return providedCapabilityItemProvider;
 			}
 
 			@Override
 			public Adapter createRequiredCapabilityAdapter() {
 				if(requiredCapabilityItemProvider == null) {
 					requiredCapabilityItemProvider = new RequiredCapabilityItemProvider(this);
 				}
 
 				return requiredCapabilityItemProvider;
 			}
 		});
 
 		adapterFactory.addAdapterFactory(new P2viewItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 
 		// Create the command stack that will notify this editor as commands are executed.
 		//
 		BasicCommandStack commandStack = new BasicCommandStack();
 
 		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with
 		// focus.
 		//
 		commandStack.addCommandStackListener(new CommandStackListener() {
 			public void commandStackChanged(final EventObject event) {
 				getContainer().getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						firePropertyChange(IEditorPart.PROP_DIRTY);
 
 						// Try to select the affected objects.
 						//
 						Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
 						if(mostRecentCommand != null) {
 							setSelectionToViewer(mostRecentCommand.getAffectedObjects());
 						}
 						if(propertySheetPage != null && !propertySheetPage.getControl().isDisposed()) {
 							propertySheetPage.refresh();
 						}
 					}
 				});
 			}
 		});
 
 		// Create the editing domain with a special command stack.
 		//
 		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>()) {
 			@Override
 			public boolean isReadOnly(Resource resource) {
 				if(resource instanceof MetadataRepositoryResourceImpl)
 					return true;
 
 				return super.isReadOnly(resource);
 			}
 		};
 	}
 
 	protected boolean isPersisted(Resource resource) {
 		return (resource instanceof MetadataRepositoryResourceImpl)
 				? false
 				: isPersistedGen(resource);
 	}
 
 	/**
 	 * This returns whether something has been persisted to the URI of the specified resource.
 	 * The implementation uses the URI converter from the editor's resource set to try to open an input stream.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected boolean isPersistedGen(Resource resource) {
 		boolean result = false;
 		try {
 			InputStream stream = editingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
 			if(stream != null) {
 				result = true;
 				stream.close();
 			}
 		}
 		catch(IOException e) {
 			// Ignore
 		}
 		return result;
 	}
 
 	/**
 	 * This is used to track the active viewer.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void pageChange(int pageIndex) {
 		super.pageChange(pageIndex);
 
 		if(contentOutlinePage != null) {
 			handleContentOutlineSelection(contentOutlinePage.getSelection());
 		}
 	}
 
 	/**
 	 * Returns whether the outline view should be presented to the user.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected boolean showOutlineView() {
 		return false;
 	}
 
 	/**
 	 * If there is more than one page in the multi-page editor part, this shows the tabs at the bottom. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void showTabs() {
 		if(getPageCount() > 1) {
 			setPageText(0, getString("_UI_SelectionPage_label"));
 			if(getContainer() instanceof CTabFolder) {
 				((CTabFolder) getContainer()).setTabHeight(SWT.DEFAULT);
 				Point point = getContainer().getSize();
 				getContainer().setSize(point.x, point.y - 6);
 			}
 		}
 	}
 
 	/**
 	 * Updates the problems indication with the information described in the specified diagnostic.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void updateProblemIndication() {
 		if(updateProblemIndication) {
 			BasicDiagnostic diagnostic = new BasicDiagnostic(
 				Diagnostic.OK, "org.eclipse.b3.aggregator.editor", 0, null,
 				new Object[] { editingDomain.getResourceSet() });
 			for(Diagnostic childDiagnostic : resourceToDiagnosticMap.values()) {
 				if(childDiagnostic.getSeverity() != Diagnostic.OK) {
 					diagnostic.add(childDiagnostic);
 				}
 			}
 
 			int lastEditorPage = getPageCount() - 1;
 			if(lastEditorPage >= 0 && getEditor(lastEditorPage) instanceof ProblemEditorPart) {
 				((ProblemEditorPart) getEditor(lastEditorPage)).setDiagnostic(diagnostic);
 				if(diagnostic.getSeverity() != Diagnostic.OK) {
 					setActivePage(lastEditorPage);
 				}
 			}
 			else if(diagnostic.getSeverity() != Diagnostic.OK) {
 				ProblemEditorPart problemEditorPart = new ProblemEditorPart();
 				problemEditorPart.setDiagnostic(diagnostic);
 				problemEditorPart.setMarkerHelper(markerHelper);
 				try {
 					addPage(++lastEditorPage, problemEditorPart, getEditorInput());
 					setPageText(lastEditorPage, problemEditorPart.getPartName());
 					setActivePage(lastEditorPage);
 					showTabs();
 				}
 				catch(PartInitException exception) {
 					AggregatorEditorPlugin.INSTANCE.log(exception);
 				}
 			}
 
 			if(markerHelper.hasMarkers(editingDomain.getResourceSet())) {
 				markerHelper.deleteMarkers(editingDomain.getResourceSet());
 				if(diagnostic.getSeverity() != Diagnostic.OK) {
 					try {
 						markerHelper.createMarkers(diagnostic);
 					}
 					catch(CoreException exception) {
 						AggregatorEditorPlugin.INSTANCE.log(exception);
 					}
 				}
 			}
 		}
 	}
 
 	private Diagnostic computeDiagnostic(Resource resource, boolean includeWarnings, boolean includeInfos,
 			boolean managedProblems) {
 		if(resource.getErrors().isEmpty() && (!includeWarnings || resource.getWarnings().isEmpty())) {
 			return Diagnostic.OK_INSTANCE;
 		}
 		else {
 			BasicDiagnostic basicDiagnostic = new BasicDiagnostic();
 			for(Resource.Diagnostic resourceDiagnostic : resource.getErrors()) {
 				if(managedProblems) {
 					if(!(resourceDiagnostic instanceof ResourceDiagnosticImpl))
 						continue;
 				}
 				else {
 					if(resourceDiagnostic instanceof ResourceDiagnosticImpl)
 						continue;
 				}
 
 				Diagnostic diagnostic = null;
 				if(resourceDiagnostic instanceof Throwable) {
 					diagnostic = BasicDiagnostic.toDiagnostic((Throwable) resourceDiagnostic);
 				}
 				else {
 					String messagePrefix = "";
 
 					if(resourceDiagnostic instanceof ResourceDiagnosticImpl)
 						messagePrefix = getLabelPrefix(resourceDiagnostic.getLocation());
 
 					diagnostic = new BasicDiagnostic(
 						Diagnostic.ERROR, "org.eclipse.emf.ecore.resource", 0, messagePrefix +
 								resourceDiagnostic.getMessage(), new Object[] { resourceDiagnostic });
 				}
 				basicDiagnostic.add(diagnostic);
 			}
 
 			if(includeWarnings) {
 				for(Resource.Diagnostic resourceDiagnostic : resource.getWarnings()) {
 					if(managedProblems) {
 						if(!(resourceDiagnostic instanceof ResourceDiagnosticImpl))
 							continue;
 					}
 					else {
 						if(resourceDiagnostic instanceof ResourceDiagnosticImpl)
 							continue;
 					}
 
 					Diagnostic diagnostic = null;
 					if(resourceDiagnostic instanceof Throwable) {
 						diagnostic = BasicDiagnostic.toDiagnostic((Throwable) resourceDiagnostic);
 					}
 					else {
 						String messagePrefix = "";
 
 						if(resourceDiagnostic instanceof ResourceDiagnosticImpl)
 							messagePrefix = getLabelPrefix(resourceDiagnostic.getLocation());
 
 						diagnostic = new BasicDiagnostic(
 							Diagnostic.WARNING, "org.eclipse.emf.ecore.resource", 0, messagePrefix +
 									resourceDiagnostic.getMessage(), new Object[] { resourceDiagnostic });
 					}
 					basicDiagnostic.add(diagnostic);
 				}
 			}
 
 			if(includeInfos && resource instanceof AggregatorResource) {
 				for(Resource.Diagnostic resourceDiagnostic : ((AggregatorResource) resource).getInfos()) {
 					if(managedProblems) {
 						if(!(resourceDiagnostic instanceof ResourceDiagnosticImpl))
 							continue;
 					}
 					else {
 						if(resourceDiagnostic instanceof ResourceDiagnosticImpl)
 							continue;
 					}
 
 					Diagnostic diagnostic = null;
 					if(resourceDiagnostic instanceof Throwable) {
 						diagnostic = BasicDiagnostic.toDiagnostic((Throwable) resourceDiagnostic);
 					}
 					else {
 						String messagePrefix = "";
 
 						if(resourceDiagnostic instanceof ResourceDiagnosticImpl)
 							messagePrefix = getLabelPrefix(resourceDiagnostic.getLocation());
 
 						diagnostic = new BasicDiagnostic(
 							Diagnostic.INFO, "org.eclipse.emf.ecore.resource", 0, messagePrefix +
 									resourceDiagnostic.getMessage(), new Object[] { resourceDiagnostic });
 					}
 					basicDiagnostic.add(diagnostic);
 				}
 			}
 
 			return basicDiagnostic;
 		}
 	}
 
 	private Resource findResource(URI uri) {
 		if(uri == null)
 			return null;
 
 		for(Resource resource : editingDomain.getResourceSet().getResources())
 			if(uri.equals(resource.getURI()))
 				return resource;
 
 		return null;
 	}
 
 	private String getLabelPrefix(String location) {
 		if(location != null) {
 			URI uri = URI.createURI(location);
 			if(uri != null && uri.fragment() != null) {
 				EObject eObject = editingDomain.getResourceSet().getEObject(uri, true);
 				if(eObject != null) {
 					IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.getRootAdapterFactory().adapt(
 						eObject, IItemLabelProvider.class);
 
 					if(labelProvider != null)
 						return labelProvider.getText(eObject) + ": ";
 				}
 			}
 		}
 
 		return "";
 	}
 
 	private Resource getResourceByURI(URI uri) {
 		if(uri == null)
 			return null;
 
 		for(Resource resource : editingDomain.getResourceSet().getResources())
 			if(uri.equals(resource.getURI()))
 				return resource;
 
 		return null;
 	}
 }
