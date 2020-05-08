 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.presentation;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.AvailableVersion;
 import org.eclipse.b3.aggregator.Configuration;
 import org.eclipse.b3.aggregator.Contact;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.CustomCategory;
 import org.eclipse.b3.aggregator.EnabledStatusProvider;
import org.eclipse.b3.aggregator.InstallableUnitRequest;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.aggregator.engine.Builder;
 import org.eclipse.b3.aggregator.engine.Builder.ActionType;
 import org.eclipse.b3.aggregator.engine.Engine;
 import org.eclipse.b3.aggregator.impl.AggregatorImpl;
 import org.eclipse.b3.aggregator.p2.util.MetadataRepositoryResourceImpl;
 import org.eclipse.b3.aggregator.p2view.IUPresentation;
 import org.eclipse.b3.aggregator.p2view.RequirementWrapper;
 import org.eclipse.b3.aggregator.provider.AggregatorEditPlugin;
 import org.eclipse.b3.aggregator.util.AddIUsToContributionCommand;
 import org.eclipse.b3.aggregator.util.AddIUsToCustomCategoryCommand;
 import org.eclipse.b3.aggregator.util.AddIUsToParentRepositoryCommand;
 import org.eclipse.b3.aggregator.util.AggregatorResourceImpl;
 import org.eclipse.b3.aggregator.util.ItemSorter;
 import org.eclipse.b3.aggregator.util.ItemSorter.ItemGroup;
 import org.eclipse.b3.aggregator.util.ItemUtils;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.b3.aggregator.util.SortCommand;
 import org.eclipse.b3.aggregator.util.TwoColumnMatrix;
 import org.eclipse.b3.p2.InstallableUnit;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.ui.action.CreateChildAction;
 import org.eclipse.emf.edit.ui.action.CreateSiblingAction;
 import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
 import org.eclipse.emf.edit.ui.action.LoadResourceAction;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.MetadataFactory;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.SubContributionItem;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.PopupDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.xml.sax.SAXException;
 
 /**
  * This is the action bar contributor for the Aggregator model editor.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class AggregatorActionBarContributor extends EditingDomainActionBarContributor implements
 		ISelectionChangedListener {
 
 	class AddToCustomCategoryAction extends Action {
 		private EditingDomain domain;
 
 		private AddIUsToCustomCategoryCommand command;
 
 		public AddToCustomCategoryAction(EditingDomain domain, CustomCategory customCategory,
 				List<IInstallableUnit> selectedFeatures) {
 			Object imageURL = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/Contribution.gif");
 
 			if(imageURL != null && imageURL instanceof URL)
 				setImageDescriptor(ImageDescriptor.createFromURL((URL) imageURL));
 
 			this.domain = domain;
 			command = new AddIUsToCustomCategoryCommand(customCategory, selectedFeatures);
 
 			setText((customCategory.getLabel() == null || customCategory.getLabel().length() == 0)
 					? "''"
 					: customCategory.getLabel());
 			setEnabled(command.canExecute());
 		}
 
 		@Override
 		public void run() {
 			domain.getCommandStack().execute(command);
 		}
 	}
 
 	class AddToParentRepositoryAction extends Action {
 		private EditingDomain domain;
 
 		private AddIUsToParentRepositoryCommand command;
 
 		public AddToParentRepositoryAction(EditingDomain domain, List<IInstallableUnit> selectedIUs, int operation) {
 			this.domain = domain;
 			command = new AddIUsToParentRepositoryCommand(
 				ResourceUtils.getAggregator(domain.getResourceSet()), selectedIUs, operation);
 
 			Object imageURL = null;
 
 			if((operation & AggregatorEditPlugin.ADD_IU) > 0) {
 				imageURL = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/Feature.gif");
 				setText(AggregatorEditorPlugin.INSTANCE.getString("_UI_Mapped_Feature"));
 
 			}
 			else if((operation & AggregatorEditPlugin.ADD_EXCLUSION_RULE) > 0) {
 				imageURL = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/ExclusionRule.gif");
 				setText(AggregatorEditorPlugin.INSTANCE.getString("_UI_Exclusion_Rule"));
 			}
 			if((operation & AggregatorEditPlugin.ADD_VALID_CONFIGURATIONS_RULE) > 0) {
 				imageURL = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/ValidConfigurationsRule.gif");
 				setText(AggregatorEditorPlugin.INSTANCE.getString("_UI_Valid_Configurations_Rule"));
 			}
 
 			if(imageURL != null && imageURL instanceof URL)
 				setImageDescriptor(ImageDescriptor.createFromURL((URL) imageURL));
 
 			setEnabled(command.canExecute());
 		}
 
 		@Override
 		public void run() {
 			domain.getCommandStack().execute(command);
 		}
 	}
 
 	class BuildRepoAction extends Action {
 		private final Builder.ActionType actionType;
 
 		public BuildRepoAction(Builder.ActionType actionType) {
 			this.actionType = actionType;
 			String txt;
 			String imageURLPath = null;
 			switch(actionType) {
 				case CLEAN:
 					txt = "Clean Repository";
 					break;
 				case VERIFY:
 					txt = "Verify Repository";
 					break;
 				case BUILD:
 					txt = "Build Repository";
 					imageURLPath = "full/obj16/start_task.gif";
 					break;
 				default:
 					txt = "Clean then Build Repository";
 					imageURLPath = "full/obj16/start_task.gif";
 			}
 			setText(txt);
 			if(imageURLPath != null) {
 				Object imageURL = AggregatorEditorPlugin.INSTANCE.getImage(imageURLPath);
 
 				if(imageURL != null && imageURL instanceof URL)
 					setImageDescriptor(ImageDescriptor.createFromURL((URL) imageURL));
 			}
 		}
 
 		@Override
 		public void run() {
 			if(getActiveEditor() == null) {
 				MessageBox messageBox = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
 				messageBox.setMessage("No editor is active");
 				messageBox.open();
 				return;
 			}
 
 			if(saveModel()) {
 				new Job("b3 Aggregator") {
 					{
 						setUser(true);
 						setPriority(Job.LONG);
 					}
 
 					@Override
 					protected IStatus run(IProgressMonitor monitor) {
 						try {
 							Builder builder = new Builder();
 							Resource resource = ((IEditingDomainProvider) activeEditorPart).getEditingDomain().getResourceSet().getResources().get(
 								0);
 							org.eclipse.emf.common.util.URI emfURI = resource.getURI();
 							URL fileURL = FileLocator.toFileURL(new URI(emfURI.toString()).toURL());
 							if(!"file".equals(fileURL.getProtocol()))
 								throw new Exception("URI scheme is not \"file\"");
 							URI uri = new URI(fileURL.getProtocol() + ":/" +
 									URLEncoder.encode(fileURL.getPath(), "UTF-8").replaceAll("\\+", "%20"));
 							builder.setBuildModelLocation(new File(uri));
 							// builder.setLogLevel(LogUtils.DEBUG);
 							builder.setAction(actionType);
 
 							if(builder.run(true, monitor) != IApplication.EXIT_OK)
 								throw new Exception("Build failed (see log for more details)");
 						}
 						catch(Throwable e) {
 							Throwable cause = unwind(e);
 							IStatus status = (cause instanceof CoreException)
 									? ((CoreException) cause).getStatus()
 									: new Status(IStatus.ERROR, Engine.PLUGIN_ID, IStatus.OK, cause.getMessage(), cause);
 
 							return status;
 						}
 
 						return Status.OK_STATUS;
 					}
 
 				}.schedule();
 			}
 
 		}
 
 		protected boolean saveModel() {
 			if(getActiveEditor() == null || !getActiveEditor().isDirty())
 				return true;
 
 			getActiveEditor().doSave(new NullProgressMonitor());
 
 			if(getActiveEditor().isDirty()) {
 				MessageBox messageBox = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
 				messageBox.setMessage("Cannot save aggregator definition");
 				messageBox.open();
 				return false;
 			}
 
 			return true;
 		}
 
 		private Throwable unwind(Throwable t) {
 			for(;;) {
 				Class<?> tc = t.getClass();
 
 				// We don't use instanceof operator since we want
 				// the explicit class, not subclasses.
 				//
 				if(tc != RuntimeException.class && tc != InvocationTargetException.class && tc != SAXException.class &&
 						tc != IOException.class)
 					break;
 
 				Throwable cause = t.getCause();
 				if(cause == null)
 					break;
 
 				String msg = t.getMessage();
 				if(msg != null && !msg.equals(cause.toString()))
 					break;
 
 				t = cause;
 			}
 			return t;
 		}
 
 	}
 
 	class EnabledStatusAction extends Action {
 		public static final int ENABLE = 0;
 
 		public static final int DISABLE = 1;
 
 		public static final int BOTH = 2;
 
 		private List<EnabledStatusProvider> refs;
 
 		private boolean enableState;
 
 		public EnabledStatusAction(boolean enableState) {
 			this.enableState = enableState;
 		}
 
 		public int determineStatus(List<EnabledStatusProvider> selectedItems) {
 			for(EnabledStatusProvider enabledStatusProvider : selectedItems) {
 				if(enabledStatusProvider.isEnabled() != selectedItems.get(0).isEnabled())
 					return EnabledStatusAction.BOTH;
 			}
 			return selectedItems.get(0).isEnabled()
 					? EnabledStatusAction.ENABLE
 					: EnabledStatusAction.DISABLE;
 		}
 
 		public List<EnabledStatusProvider> getRefs() {
 			return refs;
 		}
 
 		@Override
 		public String getText() {
 			if(refs == null)
 				return null;
 			return isEnableState()
 					? AggregatorEditorPlugin.INSTANCE.getString("_UI_Enable_menu_item")
 					: AggregatorEditorPlugin.INSTANCE.getString("_UI_Disable_menu_item");
 		}
 
 		public void initialize(List<EnabledStatusProvider> selectedItems, EditingDomain domain) {
 			for(EnabledStatusProvider enabledStatusProvider : selectedItems) {
 				IEditingDomainItemProvider itemProvider = (IEditingDomainItemProvider) ((AdapterFactoryEditingDomain) domain).getAdapterFactory().adapt(
 					enabledStatusProvider, IEditingDomainItemProvider.class);
 				if(itemProvider instanceof ItemProviderAdapter) {
 					ItemProviderAdapter itemProviderAdapter = (ItemProviderAdapter) itemProvider;
 					IItemPropertyDescriptor itemPropertyDescriptor = itemProviderAdapter.getPropertyDescriptor(
 						enabledStatusProvider, AggregatorPackage.Literals.ENABLED_STATUS_PROVIDER__ENABLED.getName());
 					setEnabled(itemPropertyDescriptor.canSetProperty(enabledStatusProvider));
 					return;
 				}
 				else {
 					setEnabled(false);
 					return;
 				}
 
 			}
 			setEnabled(false);
 		}
 
 		public boolean isEnableState() {
 			return enableState;
 		}
 
 		public boolean isSupportedFor(List<Object> items) {
 			for(Object object : items) {
 				if(!(object instanceof EnabledStatusProvider)) {
 					return false;
 				}
 			}
 			return true;
 		}
 
 		@Override
 		public void run() {
 			CompoundCommand compoundCommand = new CompoundCommand();
 			EditingDomain domain = ((IEditingDomainProvider) activeEditorPart).getEditingDomain();
 
 			for(EnabledStatusProvider ref : refs) {
 				EStructuralFeature feature = ((EObject) ref).eClass().getEStructuralFeature("enabled");
 
 				SetCommand command = (SetCommand) SetCommand.create(
 					domain, ref, feature, Boolean.valueOf(isEnableState()));
 				compoundCommand.append(command);
 			}
 
 			compoundCommand.setLabel(getText());
 			domain.getCommandStack().execute(compoundCommand);
 		}
 
 		public void setReference(List<EnabledStatusProvider> refs) {
 			this.refs = refs;
 		}
 
 		public void setRefs(List<EnabledStatusProvider> refs) {
 			this.refs = refs;
 		}
 	}
 
 	class MapToContributionAction extends Action {
 		private EditingDomain domain;
 
 		private AddIUsToContributionCommand command;
 
 		public MapToContributionAction(EditingDomain domain, Contribution contribution,
 				List<MetadataRepository> selectedMDRs, List<IInstallableUnit> selectedIUs) {
 			Object imageURL = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/Contribution.gif");
 
 			if(imageURL != null && imageURL instanceof URL)
 				setImageDescriptor(ImageDescriptor.createFromURL((URL) imageURL));
 
 			this.domain = domain;
 			command = new AddIUsToContributionCommand(contribution, selectedMDRs, selectedIUs);
 
 			setText(command.getLabel());
 			setEnabled(command.canExecute());
 		}
 
 		@Override
 		public void run() {
 			domain.getCommandStack().execute(command);
 		}
 	}
 
 	class ReloadOrCancelRepoAction extends Action {
 		private Set<MetadataRepositoryResourceImpl> metadataRepositoryResources;
 
 		private ImageDescriptor reloadImageDescriptor;
 
 		private ImageDescriptor cancelImageDescriptor;
 
 		private String loadText;
 
 		private boolean nextActionIsLoad;
 
 		public ReloadOrCancelRepoAction() {
 			Object imageURL = AggregatorEditorPlugin.INSTANCE.getImage("full/obj16/refresh.gif");
 
 			if(imageURL != null && imageURL instanceof URL)
 				reloadImageDescriptor = ImageDescriptor.createFromURL((URL) imageURL);
 
 			imageURL = AggregatorEditorPlugin.INSTANCE.getImage("full/obj16/progress_stop.gif");
 
 			if(imageURL != null && imageURL instanceof URL)
 				cancelImageDescriptor = ImageDescriptor.createFromURL((URL) imageURL);
 		}
 
 		synchronized public void addMetadataRepositoryResource(MetadataRepositoryResourceImpl res) {
 			metadataRepositoryResources.add(res);
 		}
 
 		@Override
 		synchronized public ImageDescriptor getImageDescriptor() {
 			return nextActionIsLoad
 					? reloadImageDescriptor
 					: cancelImageDescriptor;
 		}
 
 		@Override
 		synchronized public String getText() {
 			// the getText() is the first thing to be called when opening the menu - let's set the next action here
 			nextActionIsLoad = !isLoading();
 			return nextActionIsLoad
 					? loadText
 					: "Cancel Repository Loading";
 		}
 
 		synchronized public void initMetadataRepositoryReferences() {
 			metadataRepositoryResources = new HashSet<MetadataRepositoryResourceImpl>();
 		}
 
 		@Override
 		synchronized public void run() {
 			for(MetadataRepositoryResourceImpl mdr : metadataRepositoryResources)
 				if(mdr != null)
 					if(nextActionIsLoad)
 						mdr.startAsynchronousLoad(true);
 					else
 						mdr.cancelLoadingJob();
 		}
 
 		public void setLoadText(String loadText) {
 			this.loadText = loadText;
 		}
 
 		synchronized private boolean isLoading() {
 			for(MetadataRepositoryResourceImpl mdr : metadataRepositoryResources)
 				if(mdr.getStatus().getCode() == StatusCode.WAITING)
 					return true;
 
 			return false;
 		}
 	}
 
 	class SelectMatchingIUAction extends Action {
 		private IRequirement requirement;
 
 		private MetadataRepository mdr;
 
 		public SelectMatchingIUAction(IRequirement requirement) {
 			this(null, requirement);
 		}
 
 		public SelectMatchingIUAction(MetadataRepository mdr, IRequirement requirement) {
 			this.mdr = mdr;
 			this.requirement = requirement;
 
 			setText(getString("_UI_Select_matching_IU_menu_item"));
 		}
 
 		@Override
 		public void run() {
 			if(activeEditorPart instanceof IViewerProvider) {
 				final Viewer viewer = ((IViewerProvider) activeEditorPart).getViewer();
 				if(viewer != null) {
 					Shell parent = activeEditorPart.getSite().getShell();
 					final EditingDomain editingDomain = ((IEditingDomainProvider) activeEditorPart).getEditingDomain();
 
 					final Map<MetadataRepositoryResourceImpl, TwoColumnMatrix<IUPresentation, Object[]>> foundIUs = new LinkedHashMap<MetadataRepositoryResourceImpl, TwoColumnMatrix<IUPresentation, Object[]>>();
 
 					for(Resource resource : editingDomain.getResourceSet().getResources()) {
 						if(!(resource instanceof MetadataRepositoryResourceImpl))
 							continue;
 
 						MetadataRepositoryResourceImpl mdrResource = (MetadataRepositoryResourceImpl) resource;
 
 						if(mdr != null && mdr != mdrResource.getMetadataRepository())
 							continue;
 
 						TwoColumnMatrix<IUPresentation, Object[]> result = mdrResource.findIUPresentationsWhichSatisfies(
 							requirement, true);
 
 						if(result != null && result.size() > 0)
 							foundIUs.put(mdrResource, result);
 					}
 
 					if(foundIUs.size() == 0) {
 						parent.getDisplay().beep();
 						MessageDialog.openWarning(
 							parent, getString("_UI_Warning_windowTitle"), getString("_UI_No_matching_IU_was_found"));
 					}
 					else if(foundIUs.size() == 1 && foundIUs.values().size() == 1) {
 						TwoColumnMatrix<IUPresentation, Object[]> foundIU = foundIUs.values().iterator().next();
 
 						viewer.setSelection(new TreeSelection(
 							new TreePath(foundIU.getValue(0)).createChildPath(foundIU.getKey(0))), true);
 					}
 					else {
 
 						final PopupDialog pppDialog = new PopupDialog(
 							parent, PopupDialog.INFOPOPUP_SHELLSTYLE, true, false, false, false, false,
 							getString("_UI_More_matching_IUs_were_found"), getString("_UI_select_IU")) {
 							class MouseListener extends MouseTrackAdapter implements MouseMoveListener {
 								private Tree tree;
 
 								private TreeItem lastSelectedTreeItem;
 
 								public MouseListener(Tree tree) {
 									this.tree = tree;
 								}
 
 								@Override
 								public void mouseExit(MouseEvent e) {
 									if(lastSelectedTreeItem != null) {
 										lastSelectedTreeItem.setBackground(null);
 										lastSelectedTreeItem = null;
 									}
 								}
 
 								public void mouseMove(MouseEvent e) {
 									TreeItem item = tree.getItem(new Point(e.x, e.y));
 
 									// only IU can be selected - MDR selects its first child
 									if(item != null && item.getItemCount() > 0)
 										item = item.getItem(0);
 
 									if(item == lastSelectedTreeItem)
 										return;
 
 									if(lastSelectedTreeItem != null)
 										lastSelectedTreeItem.setBackground(null);
 
 									if(item != null)
 										item.setBackground(tree.getDisplay().getSystemColor(
 											SWT.COLOR_WIDGET_LIGHT_SHADOW));
 
 									lastSelectedTreeItem = item;
 								}
 							}
 
 							@Override
 							protected Control createDialogArea(Composite parent) {
 								Composite composite = (Composite) super.createDialogArea(parent);
 								final TreeViewer treeViewer = new TreeViewer(composite, SWT.NO_FOCUS);
 								treeViewer.setLabelProvider(new LabelProvider() {
 									@Override
 									public Image getImage(Object element) {
 										if(element == null)
 											return null;
 
 										Object realElement = getRealElement(element);
 
 										if(realElement == null)
 											return null;
 
 										IItemLabelProvider labelProvider = (IItemLabelProvider) ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory().adapt(
 											realElement, IItemLabelProvider.class);
 										if(labelProvider == null)
 											return null;
 										return ExtendedImageRegistry.getInstance().getImage(
 											labelProvider.getImage(realElement));
 									}
 
 									@Override
 									public String getText(Object element) {
 										if(element == null)
 											return "";
 
 										Object realElement = getRealElement(element);
 
 										if(realElement == null)
 											return element.toString();
 
 										IItemLabelProvider labelProvider = (IItemLabelProvider) ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory().adapt(
 											realElement, IItemLabelProvider.class);
 										if(labelProvider == null)
 											return realElement.toString();
 										return labelProvider.getText(realElement);
 									}
 
 									@Override
 									public boolean isLabelProperty(Object element, String property) {
 										return false;
 									}
 
 									private Object getRealElement(Object element) {
 										Object realElement = null;
 										if(element instanceof Entry<?, ?>)
 											realElement = ((Entry<?, ?>) element).getKey();
 										else if(element instanceof TwoColumnMatrix<?, ?>.MatrixEntry)
 											realElement = ((TwoColumnMatrix<?, ?>.MatrixEntry) element).getKey();
 
 										return realElement;
 									}
 								});
 
 								treeViewer.setContentProvider(new ITreeContentProvider() {
 									public void dispose() {
 									}
 
 									@SuppressWarnings("unchecked")
 									public Object[] getChildren(Object parentElement) {
 										return ((Entry<MetadataRepositoryResourceImpl, TwoColumnMatrix<IUPresentation, Object[]>>) parentElement).getValue().getEntries().toArray();
 									}
 
 									public Object[] getElements(Object inputElement) {
 										return ((Map<?, ?>) inputElement).entrySet().toArray();
 									}
 
 									public Object getParent(Object element) {
 										return null;
 									}
 
 									public boolean hasChildren(Object element) {
 										return element instanceof Entry<?, ?>;
 									}
 
 									public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 									}
 								});
 								treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 									@SuppressWarnings("unchecked")
 									public void selectionChanged(SelectionChangedEvent event) {
 										close();
 										Object selection = ((TreeSelection) event.getSelection()).getFirstElement();
 
 										TwoColumnMatrix<IUPresentation, Object[]>.MatrixEntry matrixEntry = null;
 
 										if(selection instanceof Entry<?, ?>)
 											matrixEntry = ((TwoColumnMatrix<IUPresentation, Object[]>) ((Entry<?, ?>) selection).getValue()).getEntry(0);
 										else if(selection instanceof TwoColumnMatrix<?, ?>.MatrixEntry)
 											matrixEntry = (TwoColumnMatrix<IUPresentation, Object[]>.MatrixEntry) selection;
 
 										if(matrixEntry != null)
 											viewer.setSelection(
 												new TreeSelection(
 													new TreePath(matrixEntry.getValue()).createChildPath(matrixEntry.getKey())),
 												true);
 
 									}
 								});
 
 								MouseListener ml = new MouseListener(treeViewer.getTree());
 								treeViewer.getTree().addMouseTrackListener(ml);
 								treeViewer.getTree().addMouseMoveListener(ml);
 
 								treeViewer.setInput(foundIUs);
 								treeViewer.expandAll();
 								return composite;
 							}
 						};
 						pppDialog.open();
 					}
 				}
 			}
 		}
 	}
 
 	class SortAction<T> extends Action {
 
 		private EditingDomain editingDomain;
 
 		private SortCommand<T> command;
 
 		public SortAction(EditingDomain editingDomain, EList<T> containment, T itemTemplate, String label) {
 			this.editingDomain = editingDomain;
 
 			command = new SortCommand<T>(editingDomain, containment, itemTemplate, label);
 
 			setEnabled(command.canExecute());
 
 			setText(label);
 			setImageDescriptor(ImageDescriptor.createFromURL((URL) command.getImage()));
 		}
 
 		@Override
 		public void run() {
 			editingDomain.getCommandStack().execute(command);
 		}
 	}
 
 	private static String getString(String key) {
 		return AggregatorEditorPlugin.INSTANCE.getString(key);
 	}
 
 	/**
 	 * This keeps track of the active editor.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IEditorPart activeEditorPart;
 
 	/**
 	 * This keeps track of the current selection provider.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected ISelectionProvider selectionProvider;
 
 	/**
 	 * This action opens the Properties view.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IAction showPropertiesViewAction = new Action(
 		AggregatorEditorPlugin.INSTANCE.getString("_UI_ShowPropertiesView_menu_item")) {
 		@Override
 		public void run() {
 			try {
 				getPage().showView("org.eclipse.ui.views.PropertySheet");
 			}
 			catch(PartInitException exception) {
 				AggregatorEditorPlugin.INSTANCE.log(exception);
 			}
 		}
 	};
 
 	/**
 	 * This action refreshes the viewer of the current editor if the editor
 	 * implements {@link org.eclipse.emf.common.ui.viewer.IViewerProvider}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IAction refreshViewerAction = new Action(
 		AggregatorEditorPlugin.INSTANCE.getString("_UI_RefreshViewer_menu_item")) {
 		@Override
 		public boolean isEnabled() {
 			return activeEditorPart instanceof IViewerProvider;
 		}
 
 		@Override
 		public void run() {
 			if(activeEditorPart instanceof IViewerProvider) {
 				Viewer viewer = ((IViewerProvider) activeEditorPart).getViewer();
 				if(viewer != null) {
 					viewer.refresh();
 				}
 			}
 		}
 	};
 
 	/**
 	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateChildAction} corresponding to each descriptor
 	 * generated for the current selection by the item provider.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<IAction> createChildActions;
 
 	/**
 	 * This is the menu manager into which menu contribution items should be added for CreateChild actions. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IMenuManager createChildMenuManager;
 
 	/**
 	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} corresponding to each descriptor
 	 * generated for the current selection by the item provider.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<IAction> createSiblingActions;
 
 	/**
 	 * This is the menu manager into which menu contribution items should be added for CreateSibling actions. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected IMenuManager createSiblingMenuManager;
 
 	protected Map<EnabledStatusAction, Boolean> enabledStatusActionVisibility;
 
 	protected boolean reloadOrCancelRepoActionVisible;
 
 	protected ReloadOrCancelRepoAction reloadOrCancelRepoAction;
 
 	protected BuildRepoAction cleanRepoAction;
 
 	protected BuildRepoAction verifyRepoAction;
 
 	protected BuildRepoAction buildRepoAction;
 
 	protected BuildRepoAction cleanBuildRepoAction;
 
 	protected List<IAction> addToParentRepositoryActions;
 
 	protected List<IAction> addToCustomCategoriesActions;
 
 	protected IAction selectMatchingIUAction;
 
 	private Aggregator aggregator;
 
 	private IEditorPart lastActiveEditorPart;
 
 	private ISelection lastSelection;
 
 	private boolean aggregatorSelected;
 
 	/**
 	 * This creates an instance of the contributor. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public AggregatorActionBarContributor() {
 		super(ADDITIONS_LAST_STYLE);
 		loadResourceAction = new LoadResourceAction();
 		validateAction = new AggregatorValidateAction();
 		controlAction = new DetachContributionResourceAction();
 		enabledStatusActionVisibility = new LinkedHashMap<EnabledStatusAction, Boolean>();
 		enabledStatusActionVisibility.put(new EnabledStatusAction(true), Boolean.FALSE);
 		enabledStatusActionVisibility.put(new EnabledStatusAction(false), Boolean.FALSE);
 		reloadOrCancelRepoAction = new ReloadOrCancelRepoAction();
 		reloadOrCancelRepoActionVisible = false;
 		cleanRepoAction = new BuildRepoAction(ActionType.CLEAN);
 		verifyRepoAction = new BuildRepoAction(ActionType.VERIFY);
 		buildRepoAction = new BuildRepoAction(ActionType.BUILD);
 		cleanBuildRepoAction = new BuildRepoAction(ActionType.CLEAN_BUILD);
 	}
 
 	/**
 	 * This adds to the menu bar a menu and some separators for editor additions,
 	 * as well as the sub-menus for object creation items.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void contributeToMenu(IMenuManager menuManager) {
 		super.contributeToMenu(menuManager);
 
 		IMenuManager submenuManager = new MenuManager(
 			AggregatorEditorPlugin.INSTANCE.getString("_UI_AggregatorEditor_menu"), "org.eclipse.b3.aggregatorMenuID");
 		menuManager.insertAfter("additions", submenuManager);
 		submenuManager.add(new Separator("settings"));
 		submenuManager.add(new Separator("actions"));
 		submenuManager.add(new Separator("additions"));
 		submenuManager.add(new Separator("additions-end"));
 
 		// Prepare for CreateChild item addition or removal.
 		//
 		createChildMenuManager = new MenuManager(AggregatorEditorPlugin.INSTANCE.getString("_UI_CreateChild_menu_item"));
 		submenuManager.insertBefore("additions", createChildMenuManager);
 
 		// Prepare for CreateSibling item addition or removal.
 		//
 		createSiblingMenuManager = new MenuManager(
 			AggregatorEditorPlugin.INSTANCE.getString("_UI_CreateSibling_menu_item"));
 		submenuManager.insertBefore("additions", createSiblingMenuManager);
 
 		// Force an update because Eclipse hides empty menus now.
 		//
 		submenuManager.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager menuManager) {
 				menuManager.updateAll(true);
 			}
 		});
 
 		addGlobalActions(submenuManager);
 	}
 
 	/**
 	 * This adds Separators for editor additions to the tool bar.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void contributeToToolBar(IToolBarManager toolBarManager) {
 		toolBarManager.add(new Separator("aggregator-settings"));
 		toolBarManager.add(new Separator("aggregator-additions"));
 	}
 
 	@Override
 	public void menuAboutToShow(IMenuManager menuManager) {
 		menuAboutToShowGen(menuManager);
 
 		// actions need to be created just before menu is displayed, otherwise it would be possible to add an IU as a
 		// Feature and then add the same IU as Exclusion Rule
 		addToParentRepositoryActions = generateAddToParentRepositoryAction(lastSelection);
 		addToCustomCategoriesActions = generateAddToCustomCategoryActions(lastSelection);
 
 		if(addToParentRepositoryActions != null && addToParentRepositoryActions.size() > 0)
 			if(addToParentRepositoryActions.size() == 1) {
 				IAction action = addToParentRepositoryActions.get(0);
 
 				Object imageURL = AggregatorEditPlugin.INSTANCE.getImage("full/obj16/Contribution.gif");
 
 				if(imageURL != null && imageURL instanceof URL)
 					action.setImageDescriptor(ImageDescriptor.createFromURL((URL) imageURL));
 
 				action.setText(AggregatorEditPlugin.INSTANCE.getString("_UI_Add_to_parent_Mapped_Repository"));
 
 				menuManager.insertBefore("edit", action);
 			}
 			else {
 				MenuManager submenuManager = new MenuManager(
 					AggregatorEditorPlugin.INSTANCE.getString("_UI_Add_to_parent_Mapped_Repository_As"));
 				populateManager(submenuManager, addToParentRepositoryActions, null);
 				menuManager.insertBefore("edit", submenuManager);
 			}
 
 		if(addToCustomCategoriesActions != null && addToCustomCategoriesActions.size() > 0) {
 			MenuManager submenuManager = new MenuManager(
 				AggregatorEditorPlugin.INSTANCE.getString("_UI_Add_to_Custom_Category_menu_item"));
 			populateManager(submenuManager, addToCustomCategoriesActions, null);
 			menuManager.insertBefore("edit", submenuManager);
 		}
 
 		if(selectMatchingIUAction != null)
 			menuManager.insertBefore("edit", selectMatchingIUAction);
 
 		for(Map.Entry<EnabledStatusAction, Boolean> entry : enabledStatusActionVisibility.entrySet()) {
 			if(entry.getValue()) {
 				depopulateManager(menuManager, Collections.singleton(entry.getKey()));
 				menuManager.insertBefore("edit", entry.getKey());
 			}
 		}
 		if(reloadOrCancelRepoActionVisible) {
 			depopulateManager(menuManager, Collections.singleton(reloadOrCancelRepoAction));
 			menuManager.insertBefore("edit", new ActionContributionItem(reloadOrCancelRepoAction) {
 				// force updating the text and image immediately
 				@Override
 				public boolean isDynamic() {
 					return true;
 				}
 			});
 		}
 
 		if(aggregatorSelected) {
 			EditingDomain editingDomain = ((IEditingDomainProvider) activeEditorPart).getEditingDomain();
 
 			IAction sortConfigurationsAction = new SortAction<Configuration>(
 				editingDomain, aggregator.getConfigurations(), AggregatorFactory.eINSTANCE.createConfiguration(),
 				"Configurations");
 			IAction sortContactsAction = new SortAction<Contact>(
 				editingDomain, aggregator.getContacts(), AggregatorFactory.eINSTANCE.createContact(), "Contacts");
 			IAction sortContributionsAction = new SortAction<Contribution>(
 				editingDomain, aggregator.getContributions(), AggregatorFactory.eINSTANCE.createContribution(),
 				"Contributions");
 			IAction sortCustomCategoriesAction = new SortAction<CustomCategory>(
 				editingDomain, aggregator.getCustomCategories(), AggregatorFactory.eINSTANCE.createCustomCategory(),
 				"Custom Categories");
 
 			MenuManager submenuManager = new MenuManager("Sort");
 			submenuManager.add(sortConfigurationsAction);
 			submenuManager.add(sortContactsAction);
 			submenuManager.add(sortContributionsAction);
 			submenuManager.add(sortCustomCategoriesAction);
 			menuManager.insertBefore("edit", submenuManager);
 		}
 	}
 
 	/**
 	 * This populates the pop-up menu before it appears.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void menuAboutToShowGen(IMenuManager menuManager) {
 		super.menuAboutToShow(menuManager);
 		MenuManager submenuManager = null;
 
 		submenuManager = new MenuManager(AggregatorEditorPlugin.INSTANCE.getString("_UI_CreateChild_menu_item"));
 		populateManager(submenuManager, createChildActions, null);
 		menuManager.insertBefore("edit", submenuManager);
 
 		submenuManager = new MenuManager(AggregatorEditorPlugin.INSTANCE.getString("_UI_CreateSibling_menu_item"));
 		populateManager(submenuManager, createSiblingActions, null);
 		menuManager.insertBefore("edit", submenuManager);
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionChangedListener}, handling {@link org.eclipse.jface.viewers.SelectionChangedEvent}s
 	 * by querying for the children and siblings that can be
 	 * added to the selected object and updating the menus accordingly. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		lastSelection = event.getSelection();
 		updateContextMenu(event.getSelection());
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionChangedListener},
 	 * handling {@link org.eclipse.jface.viewers.SelectionChangedEvent}s by querying for the children and siblings
 	 * that can be added to the selected object and updating the menus accordingly.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void selectionChangedGen(SelectionChangedEvent event) {
 		// Remove any menu items for old selection.
 		//
 		if(createChildMenuManager != null) {
 			depopulateManager(createChildMenuManager, createChildActions);
 		}
 		if(createSiblingMenuManager != null) {
 			depopulateManager(createSiblingMenuManager, createSiblingActions);
 		}
 
 		// Query the new selection for appropriate new child/sibling descriptors
 		//
 		Collection<?> newChildDescriptors = null;
 		Collection<?> newSiblingDescriptors = null;
 
 		ISelection selection = event.getSelection();
 		if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
 			Object object = ((IStructuredSelection) selection).getFirstElement();
 
 			EditingDomain domain = ((IEditingDomainProvider) activeEditorPart).getEditingDomain();
 
 			newChildDescriptors = domain.getNewChildDescriptors(object, null);
 			newSiblingDescriptors = domain.getNewChildDescriptors(null, object);
 		}
 
 		// Generate actions for selection; populate and redraw the menus.
 		//
 		createChildActions = generateCreateChildActions(newChildDescriptors, selection);
 		createSiblingActions = generateCreateSiblingActions(newSiblingDescriptors, selection);
 
 		if(createChildMenuManager != null) {
 			populateManager(createChildMenuManager, createChildActions, null);
 			createChildMenuManager.update(true);
 		}
 		if(createSiblingMenuManager != null) {
 			populateManager(createSiblingMenuManager, createSiblingActions, null);
 			createSiblingMenuManager.update(true);
 		}
 	}
 
 	/**
 	 * When the active editor changes, this remembers the change and registers with it as a selection provider. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void setActiveEditor(IEditorPart part) {
 		super.setActiveEditor(part);
 		activeEditorPart = part;
 
 		// Switch to the new selection provider.
 		//
 		if(selectionProvider != null) {
 			selectionProvider.removeSelectionChangedListener(this);
 		}
 		if(part == null) {
 			selectionProvider = null;
 		}
 		else {
 			selectionProvider = part.getSite().getSelectionProvider();
 			selectionProvider.addSelectionChangedListener(this);
 
 			// Fake a selection changed event to update the menus.
 			//
 			if(selectionProvider.getSelection() != null) {
 				selectionChanged(new SelectionChangedEvent(selectionProvider, selectionProvider.getSelection()));
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void updateContextMenu(ISelection selection) {
 		// Remove any menu items for old selection.
 		//
 		if(createChildMenuManager != null) {
 			depopulateManager(createChildMenuManager, createChildActions);
 		}
 		if(createSiblingMenuManager != null) {
 			depopulateManager(createSiblingMenuManager, createSiblingActions);
 		}
 
 		// Query the new selection for appropriate new child/sibling descriptors
 		//
 		Collection<?> newChildDescriptors = null;
 		Collection<?> newSiblingDescriptors = null;
 		selectMatchingIUAction = null;
 		reloadOrCancelRepoActionVisible = false;
 
 		if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() >= 1) {
 			EditingDomain domain = ((IEditingDomainProvider) activeEditorPart).getEditingDomain();
 			List<Object> selectedItems = ((IStructuredSelection) selection).toList();
 
 			int state = -1;
 			boolean first = true;
 			for(Map.Entry<EnabledStatusAction, Boolean> sa : enabledStatusActionVisibility.entrySet()) {
 				sa.setValue(false);
 				if(sa.getKey().isSupportedFor(selectedItems)) {
 					List<EnabledStatusProvider> items = new ArrayList<EnabledStatusProvider>();
 					for(Object obj : selectedItems)
 						items.add((EnabledStatusProvider) obj);
 
 					sa.getKey().setReference(items);
 
 					sa.getKey().initialize(items, domain);
 					if(first) {
 						state = sa.getKey().determineStatus(items);
 						first = false;
 					}
 
 					if((state == EnabledStatusAction.DISABLE || state == EnabledStatusAction.BOTH) &&
 							sa.getKey().isEnableState()) {
 						sa.setValue(Boolean.TRUE);
 					}
 					if((state == EnabledStatusAction.ENABLE || state == EnabledStatusAction.BOTH) &&
 							!sa.getKey().isEnableState()) {
 						sa.setValue(Boolean.TRUE);
 					}
 				}
 			}
 
 			reloadOrCancelRepoAction.setEnabled(true);
 			reloadOrCancelRepoAction.initMetadataRepositoryReferences();
 			Aggregator aggregator = getAggregator();
 
 			aggregatorSelected = false;
 			if(selectedItems.size() == 1 && selectedItems.get(0).equals(aggregator)) {
 				aggregatorSelected = true;
 				reloadOrCancelRepoAction.setLoadText("Reload All Repositories");
 				reloadOrCancelRepoActionVisible = true;
 				ResourceSet resourceSet = ((AggregatorImpl) aggregator).eResource().getResourceSet();
 				for(Resource resource : resourceSet.getResources())
 					if(resource instanceof MetadataRepositoryResourceImpl)
 						reloadOrCancelRepoAction.addMetadataRepositoryResource((MetadataRepositoryResourceImpl) resource);
 			}
 			else {
 				reloadOrCancelRepoAction.setLoadText("Reload Repository");
 				for(Object object : selectedItems) {
 					if(object instanceof MetadataRepositoryReference) {
 						MetadataRepositoryReference metadataRepositoryReference = (MetadataRepositoryReference) object;
 						if(metadataRepositoryReference.isBranchEnabled()) {
 							MetadataRepositoryResourceImpl res = (MetadataRepositoryResourceImpl) MetadataRepositoryResourceImpl.getResourceForNatureAndLocation(
 								metadataRepositoryReference.getNature(),
 								metadataRepositoryReference.getResolvedLocation(), aggregator);
 							reloadOrCancelRepoAction.addMetadataRepositoryResource(res);
 						}
 						else {
 							reloadOrCancelRepoAction.setEnabled(false);
 						}
 						reloadOrCancelRepoActionVisible = true;
 					}
 					else if(object instanceof MetadataRepositoryResourceImpl) {
 						MetadataRepositoryResourceImpl res = (MetadataRepositoryResourceImpl) object;
 						reloadOrCancelRepoAction.addMetadataRepositoryResource(res);
 						reloadOrCancelRepoActionVisible = true;
 					}
 					else {
 						reloadOrCancelRepoAction.setEnabled(false);
 						reloadOrCancelRepoActionVisible = false;
 						reloadOrCancelRepoAction.initMetadataRepositoryReferences();
 						break;
 					}
 				}
 			}
 
 			if(((IStructuredSelection) selection).size() == 1) {
 				Object object = ((IStructuredSelection) selection).getFirstElement();
 
 				newChildDescriptors = domain.getNewChildDescriptors(object, null);
 				newSiblingDescriptors = domain.getNewChildDescriptors(null, object);
 
 				if(object instanceof RequirementWrapper) {
 					selectMatchingIUAction = new SelectMatchingIUAction(((RequirementWrapper) object).getGenuine());
 				}
 				if(object instanceof AvailableVersion) {
 					AvailableVersion av = (AvailableVersion) object;
					InstallableUnitRequest mappedUnit = (InstallableUnitRequest) ((EObject) av).eContainer();
 
 					IRequirement requiredCapability = MetadataFactory.createRequirement(
 						IInstallableUnit.NAMESPACE_IU_ID, mappedUnit.getName(), new VersionRange(
 							av.getVersion(), true, av.getVersion(), true), null, false, true);
 					selectMatchingIUAction = new SelectMatchingIUAction(null, requiredCapability);
 				}
 			}
 		}
 
 		// Generate actions for selection; populate and redraw the menus.
 		//
 		createChildActions = generateCreateChildActions(newChildDescriptors, selection);
 		createSiblingActions = generateCreateSiblingActions(newSiblingDescriptors, selection);
 
 		if(createChildMenuManager != null) {
 			populateManager(createChildMenuManager, createChildActions, null);
 			createChildMenuManager.update(true);
 		}
 		if(createSiblingMenuManager != null) {
 			populateManager(createSiblingMenuManager, createSiblingActions, null);
 			createSiblingMenuManager.update(true);
 		}
 	}
 
 	@Override
 	protected void addGlobalActions(IMenuManager menuManager) {
 		menuManager.insertBefore("additions", new ActionContributionItem(cleanRepoAction));
 		menuManager.insertBefore("additions", new ActionContributionItem(verifyRepoAction));
 		menuManager.insertBefore("additions", new ActionContributionItem(buildRepoAction));
 		menuManager.insertBefore("additions", new ActionContributionItem(cleanBuildRepoAction));
 		menuManager.insertBefore("additions", new Separator());
 		addGlobalActionsGen(menuManager);
 	}
 
 	/**
 	 * This inserts global actions before the "additions-end" separator.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addGlobalActionsGen(IMenuManager menuManager) {
 		menuManager.insertAfter("additions-end", new Separator("ui-actions"));
 		menuManager.insertAfter("ui-actions", showPropertiesViewAction);
 
 		refreshViewerAction.setEnabled(refreshViewerAction.isEnabled());
 		menuManager.insertAfter("ui-actions", refreshViewerAction);
 
 		super.addGlobalActions(menuManager);
 	}
 
 	/**
 	 * This removes from the specified <code>manager</code> all {@link org.eclipse.jface.action.ActionContributionItem}s
 	 * based on the {@link org.eclipse.jface.action.IAction}s contained in the <code>actions</code> collection. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void depopulateManager(IContributionManager manager, Collection<? extends IAction> actions) {
 		if(actions != null) {
 			IContributionItem[] items = manager.getItems();
 			for(int i = 0; i < items.length; i++) {
 				// Look into SubContributionItems
 				//
 				IContributionItem contributionItem = items[i];
 				while(contributionItem instanceof SubContributionItem) {
 					contributionItem = ((SubContributionItem) contributionItem).getInnerItem();
 				}
 
 				// Delete the ActionContributionItems with matching action.
 				//
 				if(contributionItem instanceof ActionContributionItem) {
 					IAction action = ((ActionContributionItem) contributionItem).getAction();
 					if(actions.contains(action)) {
 						manager.remove(contributionItem);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateChildAction} for each object in <code>descriptors</code>, and returns the
 	 * collection of these actions. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<IAction> generateCreateChildActions(Collection<?> descriptors, ISelection selection) {
 		Collection<IAction> actions = new ArrayList<IAction>();
 		if(descriptors != null) {
 			for(Object descriptor : descriptors) {
 				actions.add(new CreateChildAction(activeEditorPart, selection, descriptor));
 			}
 		}
 		return actions;
 	}
 
 	/**
 	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} for each object in <code>descriptors</code>, and returns the
 	 * collection of these actions. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	protected Collection<IAction> generateCreateSiblingActions(Collection<?> descriptors, ISelection selection) {
 		Collection<IAction> actions = new ArrayList<IAction>();
 		if(descriptors != null) {
 			for(Object descriptor : descriptors) {
 				actions.add(new CreateSiblingAction(activeEditorPart, selection, descriptor));
 			}
 		}
 		return actions;
 	}
 
 	/**
 	 * This populates the specified <code>manager</code> with {@link org.eclipse.jface.action.ActionContributionItem}s
 	 * based on the {@link org.eclipse.jface.action.IAction}s contained in the <code>actions</code> collection,
 	 * by inserting them before the specified contribution item <code>contributionID</code>.
 	 * If <code>contributionID</code> is <code>null</code>, they are simply added.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void populateManager(IContributionManager manager, Collection<? extends IAction> actions,
 			String contributionID) {
 		if(actions != null) {
 			for(IAction action : actions) {
 				if(contributionID != null) {
 					manager.insertBefore(contributionID, action);
 				}
 				else {
 					manager.add(action);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This ensures that a delete action will clean up all references to deleted objects.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected boolean removeAllReferencesOnDelete() {
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<IAction> generateAddToCustomCategoryActions(ISelection selection) {
 		if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() > 0) {
 			ItemSorter itemSorter = new ItemSorter(((IStructuredSelection) selection).toList());
 
 			List<IAction> addToActions = new ArrayList<IAction>();
 
 			if(itemSorter.getTotalItemCount() > 0 &&
 					(itemSorter.getTotalItemCount() == itemSorter.getGroupItems(ItemGroup.FEATURE).size() || (itemSorter.getTotalItemCount() == itemSorter.getGroupItems(
 						ItemGroup.FEATURE_STRUCTURED).size()))) {
 				List<IInstallableUnit> features = new ArrayList<IInstallableUnit>();
 				features.addAll((List<InstallableUnit>) itemSorter.getGroupItems(ItemGroup.FEATURE));
 				features.addAll(ItemUtils.getIUs((List<org.eclipse.b3.aggregator.p2view.Feature>) itemSorter.getGroupItems(ItemGroup.FEATURE_STRUCTURED)));
 
 				for(CustomCategory customCategory : getAggregator().getCustomCategories())
 					addToActions.add(new AddToCustomCategoryAction(
 						((IEditingDomainProvider) activeEditorPart).getEditingDomain(), customCategory, features));
 			}
 
 			return addToActions;
 		}
 
 		return Collections.emptyList();
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<IAction> generateAddToParentRepositoryAction(ISelection selection) {
 		List<IAction> actions = new ArrayList<IAction>();
 
 		if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() > 0) {
 			ItemSorter itemSorter = new ItemSorter(((IStructuredSelection) selection).toList());
 
 			if(itemSorter.getTotalItemCount() > 0 &&
 					(itemSorter.getTotalItemCount() == itemSorter.getGroupItems(ItemGroup.IU).size() || (itemSorter.getTotalItemCount() == itemSorter.getGroupItems(
 						ItemGroup.IU_STRUCTURED).size()))) {
 				List<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
 
 				ius.addAll((List<InstallableUnit>) itemSorter.getGroupItems(ItemGroup.IU));
 				ius.addAll(ItemUtils.getIUs((List<IUPresentation>) itemSorter.getGroupItems(ItemGroup.IU_STRUCTURED)));
 
 				if(itemSorter.getTotalItemCount() == itemSorter.getGroupItems(ItemGroup.FEATURE_STRUCTURED).size()) {
 					actions.add(new AddToParentRepositoryAction(
 						((IEditingDomainProvider) activeEditorPart).getEditingDomain(), ius,
 						AggregatorEditPlugin.ADD_IU));
 					actions.add(new AddToParentRepositoryAction(
 						((IEditingDomainProvider) activeEditorPart).getEditingDomain(), ius,
 						AggregatorEditPlugin.ADD_EXCLUSION_RULE));
 					actions.add(new AddToParentRepositoryAction(
 						((IEditingDomainProvider) activeEditorPart).getEditingDomain(), ius,
 						AggregatorEditPlugin.ADD_VALID_CONFIGURATIONS_RULE));
 				}
 				else {
 					actions.add(new AddToParentRepositoryAction(
 						((IEditingDomainProvider) activeEditorPart).getEditingDomain(), ius,
 						AggregatorEditPlugin.ADD_IU));
 				}
 			}
 
 			return actions;
 		}
 
 		return actions;
 	}
 
 	private Aggregator getAggregator() {
 		if(activeEditorPart == null || !(activeEditorPart instanceof IEditingDomainProvider))
 			return null;
 
 		if(lastActiveEditorPart == activeEditorPart)
 			return aggregator;
 
 		lastActiveEditorPart = activeEditorPart;
 
 		IEditingDomainProvider edProvider = (IEditingDomainProvider) activeEditorPart;
 
 		EList<Resource> resources = edProvider.getEditingDomain().getResourceSet().getResources();
 		Resource aggregatorResource = null;
 		for(Resource resource : resources)
 			if(resource instanceof AggregatorResourceImpl) {
 				aggregatorResource = resource;
 				break;
 			}
 		return aggregator = (aggregatorResource == null
 				? null
 				: (Aggregator) aggregatorResource.getContents().get(0));
 	}
 }
