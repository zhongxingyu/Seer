 package org.dawnsci.plotting.tools.diffraction;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.measure.quantity.Quantity;
 import javax.swing.tree.TreeNode;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IPaletteListener;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.PaletteEvent;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.viewers.TreeNodeContentProvider;
 import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
 import org.dawb.common.ui.wizard.persistence.PersistenceImportWizard;
 import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
 import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
 import org.dawnsci.common.widgets.tree.IResettableExpansion;
 import org.dawnsci.common.widgets.tree.NodeFilter;
 import org.dawnsci.common.widgets.tree.NodeLabelProvider;
 import org.dawnsci.common.widgets.tree.NumericNode;
 import org.dawnsci.common.widgets.tree.UnitEditingSupport;
 import org.dawnsci.common.widgets.tree.ValueEditingSupport;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.dawnsci.plotting.draw2d.swtxy.selection.CircleFitSelection;
 import org.dawnsci.plotting.draw2d.swtxy.selection.EllipseFitSelection;
 import org.dawnsci.plotting.preference.DiffractionDefaultsPreferencePage;
 import org.dawnsci.plotting.preference.detector.DiffractionDetectorPreferencePage;
 import org.dawnsci.plotting.preference.diffraction.DiffractionPreferencePage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.JFacePreferences;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
 import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionMetadataUtils;
 import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.roi.CircularFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 
 public class DiffractionTool extends AbstractToolPage implements CalibrantSelectedListener, IResettableExpansion {
 
 	private static final Logger logger = LoggerFactory.getLogger(DiffractionTool.class);
 	
 	private ClearableFilteredTree filteredTree;
 	private TreeViewer      viewer;
 	private Composite       control;
 	private DiffractionTreeModel model;
 	private ILoaderService  service;
 	private Label statusMessage;
 	private String statusString = "";
 	
 	private static DiffractionTool      activeDiffractionTool=null;
 	
 	//Region and region listener added for 1-click beam centring
 	private IRegion               tmpRegion;
 	private IRegionListener       regionListener;
 	private IPaletteListener.Stub paletteListener;
 	private ITraceListener.Stub   traceListener;
 	private IROIListener roiListener;
 	
 	protected DiffractionImageAugmenter augmenter;
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 	
 	
 	public DiffractionTool() {
 		super();
 		
         this.paletteListener = new IPaletteListener.Stub() {
         	protected void updateEvent(PaletteEvent evt) {
         		updateIntensity();
         	}
         };
 
 		this.traceListener = new ITraceListener.Stub() {
 			protected void update(TraceEvent evt) {
 				if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
 				if (getImageTrace()!=null) createDiffractionModel(true);
 				updateIntensity();
 			}
 		};
 		
 		this.service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
       
 	}
 
 	protected void updateIntensity() {
 		try {
 			if (model==null) return;
 			model.setIntensityValues(getImageTrace());
 			if (!(viewer.getControl().isDisposed())) viewer.refresh();
 		} catch (Exception e) {
 			logger.error("Updating intensity values!", e);
 		}
 	}
 
 	@Override
 	public void createControl(final Composite parent) {
 		
 		this.control = new Composite(parent, SWT.NONE);
 		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		control.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(control);
 	
 		this.filteredTree = new ClearableFilteredTree(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true);		
 		viewer = filteredTree.getViewer();
 		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		createColumns(viewer);
 		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
 		viewer.getTree().setLinesVisible(true);
 		viewer.getTree().setHeaderVisible(true);
 		
 		Composite status = new Composite(control, SWT.NONE);
 		status.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		status.setLayout(new GridLayout(2, true));
 		
 		statusMessage = new Label(status, SWT.NONE);
 		statusMessage.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));
 		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
 		statusMessage.setForeground(new Color(statusMessage.getDisplay(), colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR)));
 		statusMessage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		statusMessage.setText(statusString);
 		
 		final Label label = new Label(status, SWT.NONE);
 		label.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
 		label.setForeground(new Color(label.getDisplay(), colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR)));
 		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		label.setText("* Click to change value  ");
 		
 		createDiffractionModel(false);
 		createActions();
 		createListeners();
 
 	}
 	
 	public void activate() {
 		super.activate();
 		createDiffractionModel(true);
 		
 		IPlottingSystem plotting = getPlottingSystem();
 		if (plotting != null) {
 			if (regionListener != null)
 				plotting.addRegionListener(regionListener);
 			if (traceListener != null)
 				plotting.addTraceListener(traceListener);
 			if (tmpRegion != null) {
 				tmpRegion.setVisible(true);
 			}
 		}
 
 		if (augmenter!=null) augmenter.activate();
 		CalibrationFactory.addCalibrantSelectionListener(this);
 		activeDiffractionTool = this;
 			
 		final IDiffractionMetadata dmd = getDiffractionMetaData();
 		if (viewer!=null && viewer.getInput()!=null && model!=null && dmd!=null && dmd.getDetector2DProperties()!=null && dmd.getOriginalDiffractionCrystalEnvironment()!=null) {
 			try {
 			    viewer.refresh();
 			} catch (Throwable ne) {
 				// Sometimes model could not be resolved at this point.
 			}
 		}
 	}
 	
 	public void deactivate() {
 		
 		if (!isActive()) {return;}
 		
 		super.deactivate();
 		IPlottingSystem plotting = getPlottingSystem();
 		if (plotting != null) {
 			plotting.removeRegionListener(regionListener);
 			if (traceListener != null)
 				plotting.removeTraceListener(traceListener);
 			if (tmpRegion != null) {
 				tmpRegion.setVisible(false);
 			}
 		}
 
 		CalibrationFactory.removeCalibrantSelectionListener(this);
 		if (augmenter!=null) augmenter.deactivate();
 		if (activeDiffractionTool==this) activeDiffractionTool = null;
 		if (model!=null) model.deactivate();
 	}
 	
 	public void dispose() {
 		super.dispose();
 		if (model!=null) model.dispose();
 		if (augmenter != null) augmenter.dispose();
 	}
 
 	private void createDiffractionModel(boolean force) {
 		
 		if (!force && model!=null)  return;
 		if (force && model!=null) {
 			model.dispose();
 			model= null;
 		}
 		if (viewer==null)           return;
 		
 		IDiffractionMetadata data=null;
 		try {
 			data  = getDiffractionMetaData();
 			if (data==null || data.getOriginalDetector2DProperties()==null || data.getDiffractionCrystalEnvironment()==null) {
 				return;
 			}
 			model = new DiffractionTreeModel(data);
 			model.setViewer(viewer);
 			model.activate();
 			if (augmenter != null) {
 				augmenter.setDiffractionMetadata(data);
 			}
 		} catch (Exception e) {
 			logger.error("Cannot create model!", e);
 			return;
 		}
 			
 		viewer.setInput(model.getRoot());
 		model.activate();
 		
         resetExpansion();
 		getSite().setSelectionProvider(viewer);
 
 	}
 	
 	public void resetExpansion() {
 		try {
 			if (model == null) return;
 			final List<?> top = model.getRoot().getChildren();
 			for (Object element : top) {
 			   filteredTree.expand(element);
 			}
 		} catch (Throwable ne) {
 			// intentionally silent
 		}
 	}
 	
 	private boolean diffractionMetadataAreEqual(IDiffractionMetadata meta1,IDiffractionMetadata meta2) {
 		
 		if (meta1.getDetector2DProperties().equals(meta2.getDetector2DProperties()) &&
 				meta1.getDiffractionCrystalEnvironment().equals(meta2.getDiffractionCrystalEnvironment())) {
 			return true;
 		}
 		
 		return false;
 		
 	}
 	
 	private IDiffractionMetadata getDiffractionMetaData() {
 		// Now always returns IDiffractionMetadata to prevent creation of a new
 		// metadata object after listeners have been added to the old metadata
 		//TODO improve this section- its pretty horrible
 		IDiffractionMetadata lockedMeta = service.getLockedDiffractionMetaData();
 		
 		if (lockedMeta != null) {
    		    IImageTrace imageTrace = getImageTrace();
 			if (imageTrace==null || imageTrace.getData() ==null) return lockedMeta;
 			
 			IMetaData mdImage = imageTrace.getData().getMetadata();
 			
 			if (mdImage == null || !(mdImage instanceof IDiffractionMetadata)) {
 				//TODO what if the image is rotated?
 				int[] shape = imageTrace.getData().getShape();
 				
 				if (shape[0] == lockedMeta.getDetector2DProperties().getPx() &&
 					shape[1] == lockedMeta.getDetector2DProperties().getPy()) {
 					imageTrace.getData().setMetadata(lockedMeta.clone());
 				} else {
 					IDiffractionMetadata clone = lockedMeta.clone();
 					clone.getDetector2DProperties().setPx(shape[0]);
 					clone.getDetector2DProperties().setPy(shape[1]);
 					statusString = "Locked metadata doesnt match image dimensions!";
 				}
 				
 			} else if (mdImage instanceof IDiffractionMetadata) {
 				if (!diffractionMetadataAreEqual((IDiffractionMetadata)mdImage,lockedMeta)) {
 					try {
 						DiffractionMetadataUtils.copyNewOverOld(lockedMeta, (IDiffractionMetadata)mdImage);
 					} catch (IllegalArgumentException e) {
 						statusString = "Locked metadata doesnt match image dimensions!";
 					}
 					imageTrace.getData().setMetadata(mdImage);
 				}
 			}
 			if (statusString.equals("")) statusString = "Metadata loaded from lock";
 			return lockedMeta;
 		}
 		
 		//If not see if the trace has diffraction meta data
 		IImageTrace imageTrace = getImageTrace();
 		if (imageTrace==null) return null;
 		if (imageTrace.getData()==null) return null;
 		IMetaData mdImage = imageTrace.getData().getMetadata();
 		if (mdImage !=null && mdImage  instanceof IDiffractionMetadata) {
 			if (statusString.equals("")) statusString = "Metadata loaded from image";
 			return (IDiffractionMetadata)mdImage;
 		}
 		
 		
 		int[] imageShape = imageTrace.getData().getShape();
 		
 		//Try and get the filename here, it will help later on
 		String filePath = (mdImage !=null && mdImage.getFilePath()!= null) ? mdImage.getFilePath() : null;
 		
 		if (filePath == null && getPart() instanceof IEditorPart) {
 			filePath = EclipseUtils.getFilePath(((IEditorPart)getPart()).getEditorInput());
 		}
 		
 		if (filePath != null) {
 			//see if we can read diffraction info from nexus files
 			NexusDiffractionMetaCreator ndmc = new NexusDiffractionMetaCreator(filePath);
 			IDiffractionMetadata difMet = ndmc.getDiffractionMetadataFromNexus(imageShape);
 			if (difMet !=null) {
 				imageTrace.getData().setMetadata(difMet);
 				
 				if (ndmc.isCompleteRead() && statusString.equals(""))
 					statusString = "Metadata completely loaded from nexus tree";
 				else if (ndmc.isPartialRead() && statusString.equals(""))
 					statusString = "Required metadata loaded from nexus tree";
 				else if (ndmc.anyValuesRead() && statusString.equals(""))
 					statusString = "Partial metadata loaded from nexus tree";
 				else if (statusString.equals(""))
 					statusString = "No metadata in nexus tree, metadata loaded from preferences";
 				
 				return difMet;
 			}
 		}
 				
 		// if it is null try and get it from the loader service
 		if (mdImage == null  && filePath != null) {
 			IMetaData md = null;
 			try {
 				md = service.getMetaData(filePath, null);
 			} catch (Exception e) {
 				logger.error("Cannot read meta data from "+getPart().getTitle(), e);
 			}
 			
 			//If it is there and diffraction data return it
 			if (md!=null && md instanceof IDiffractionMetadata) {
 				if (statusString.equals("")) statusString = "Metadata loaded from disk";
 				return (IDiffractionMetadata)md;
 			}
 			
 			if (md != null)
 				mdImage = md;
 		}
 		
 		// if there is no meta or is not nexus or IDiff create default IDiff and put it in the dataset
 		mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(filePath,imageTrace.getData().getShape());
 		imageTrace.getData().setMetadata(mdImage);
 		if (statusString.equals("")) statusString = "No metadata found. Values loaded from preferences:";
 		return (IDiffractionMetadata)mdImage;
 	}
 
 	private TreeViewerColumn defaultColumn;
 	
 	private void createColumns(TreeViewer viewer) {
 				
 		viewer.setColumnProperties(new String[] { "Name", "Original", "Value", "Unit" });
 		ColumnViewerToolTipSupport.enableFor(viewer);
 
 		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Name"); // Selected
 		var.getColumn().setWidth(260);
 		var.setLabelProvider(new NodeLabelProvider(0));
 		
 		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
 		var.getColumn().setText("Original"); // Selected
 		var.getColumn().setWidth(0);
 		var.getColumn().setResizable(false);
 		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(1)));
 		defaultColumn = var;
 		
 		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
 		var.getColumn().setText("Value"); // Selected
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));
 		var.setEditingSupport(new ValueEditingSupport(viewer));
 
 		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
 		var.getColumn().setText("Unit"); // Selected
 		var.getColumn().setWidth(90);
 		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(3)));
 		var.setEditingSupport(new UnitEditingSupport(viewer));
 	}
 	
 	private TreeNode   copiedNode;
 	private MenuAction calibrantActions;
 	private Action     calPref;
 
 	private Action refine;
 	private Action findOuter;
 	private Action calibrate;
 	private static Action lock;
 	
 	private void createActions() {
 		
 		final IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
 		final MenuManager     menuMan = new MenuManager();
 		
 		
 		final Action exportMeta = new Action("Export metadata to file", Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
 			public void run() {
 				try {
 					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
 					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 					wd.setTitle(wiz.getWindowTitle());
 					wd.open();
 				} catch (Exception e) {
 					logger.error("Problem opening export!", e);
 				}
 			}			
 		};
 		
 		final Action importMeta = new Action("Import metadata from file", Activator.getImageDescriptor("icons/mask-import-wiz.png")) {
 			public void run() {
 				try {
 					IWizard wiz = EclipseUtils.openWizard(PersistenceImportWizard.ID, false);
 					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 					wd.setTitle(wiz.getWindowTitle());
 					wd.open();
 				} catch (Exception e) {
 					logger.error("Problem opening import!", e);
 				}
 			}			
 		};
 		
 		final Action showDefault = new Action("Show the original/default value column", Activator.getImageDescriptor("icons/plot-tool-diffraction-default.gif")) {
 			public void run() {
 				defaultColumn.getColumn().setWidth(isChecked()?80:0);
 				defaultColumn.getColumn().setResizable(!isChecked());
 			}
 		};
 		showDefault.setChecked(false);
 		
 		final Action reset = new Action("Reset selected field", Activator.getImageDescriptor("icons/reset.gif")) {
 			@Override
 			public void run() {
 				final TreeNode node = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
 				if (node instanceof NumericNode) {
 					((NumericNode<?>)node).reset();
 					viewer.refresh(node);
 				}
 			}
 		};
 		final Action resetAll = new Action("Reset all fields", Activator.getImageDescriptor("icons/reset_red.png")) {
 			@Override
 			public void run() {
 				
 				boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Reset All", "Are you sure that you would like to reset all values?");
 				if (!ok) return;
 				filteredTree.clearText();
 				if (service.getLockedDiffractionMetaData()!=null) {
 					model.reset();
 					viewer.refresh();
 			        resetExpansion();
 				} else {
 					createDiffractionModel(true);
 					model.reset();
 				}
 			}
 		};
 		
 		final Action copy = new Action("Copy value", Activator.getImageDescriptor("icons/copy.gif")) {
 			@Override
 			public void run() {
 				copiedNode = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
 			}
 		};
 
 		final Action paste = new Action("Paste value", Activator.getImageDescriptor("icons/paste.gif")) {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void run() {
 				if (copiedNode!=null) {
 					Object object = ((StructuredSelection)viewer.getSelection()).getFirstElement();
 					if (object instanceof NumericNode) {
 						NumericNode<Quantity> nn = (NumericNode<Quantity>)object;
 						if (!nn.isEditable()) {
 							MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot paste", "The item '"+nn.getLabel()+"' is not writable.\n\nPlease choose a different value to paste to.");
 							return;
 						}
 						
 						try {
 						    nn.mergeValue(copiedNode);
 						} catch (Throwable e) {
 							try {
 								if (EclipseUtils.getActivePage().findView("org.eclipse.pde.runtime.LogView")==null) {
 								    EclipseUtils.getActivePage().showView("org.eclipse.pde.runtime.LogView");
 								}
 							} catch (PartInitException pe) {
 								// Ignored.
 							}
 							IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, "Cannot past into '"+nn.getLabel()+"'", e);
 							Activator.getDefault().getLog().log(status);
 						}
 						viewer.refresh(object);
 					}
 				}
 			}
 		};
 		
 		final Action centre = new Action("One-click beam centre", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				logger.debug("1-click clicked");
 
 				IPlottingSystem plotter = getPlottingSystem();
 				if (plotter == null) {
 					logger.debug("No plotting system found");
 					return;
 				}
 
 				try {
 					clearRegions(plotter);
 					if (tmpRegion != null) {
 						plotter.removeRegion(tmpRegion);
 					}
 					tmpRegion = plotter.createRegion(RegionUtils.getUniqueName("BeamCentrePicker", plotter), IRegion.RegionType.POINT);
 					tmpRegion.setUserRegion(false);
 					tmpRegion.setVisible(false);
 					refine.setEnabled(true);
 				} catch (Exception e) {
 					logger.error("Cannot add beam centre", e);
 				}
 
 			}
 		};
 		centre.setImageDescriptor(Activator.getImageDescriptor("icons/centre.png"));
 		
 		final Action fitRing = new Action("Fit ring", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				logger.debug("Fit ring clicked");
 
 				IPlottingSystem plotter = getPlottingSystem();
 				if (plotter == null) {
 					logger.debug("No plotting system found");
 					return;
 				}
 
 				try {
 					clearRegions(plotter);
 					if (tmpRegion != null) {
 						plotter.removeRegion(tmpRegion);
 					}
 					tmpRegion = plotter.createRegion(RegionUtils.getUniqueName("RingPicker", plotter), IRegion.RegionType.ELLIPSEFIT);
 					tmpRegion.setUserRegion(false);
 					tmpRegion.addROIListener(roiListener);
 					findOuter.setEnabled(true);
 					refine.setEnabled(true);
 				} catch (Exception e) {
 					logger.error("Cannot add ring", e);
 				}
 			}
 		};
 		fitRing.setImageDescriptor(Activator.getImageDescriptor("icons/eclipsecirclepoints.png"));
 		fitRing.setToolTipText("Select 3 or 4 points on ring to fit a circle or 5 points or more for an ellipse");
 
 		refine = new Action("Refine beam centre", IAction.AS_PUSH_BUTTON) {
 			
 			class Compare implements Comparator<IPeak> {
 
 				@Override
 				public int compare(IPeak o1, IPeak o2) {
 					if (o1.getPosition() > o2.getPosition()) {
 						return 1;
 					}
 					if (o1.getPosition() < o2.getPosition()) {
 						return -1;
 					}
 					return 0;
 				}
 
 			}
 			
 			
 			@SuppressWarnings("unchecked")
 			private List<IPeak> loadPeaks() {
 				IToolPage radialTool = getToolSystem().getToolPage(
 						"org.dawb.workbench.plotting.tools.radialProfileTool");
 				IToolPage fittingTool = ((IToolPageSystem)radialTool.getToolPlottingSystem()).getToolPage(
 						"org.dawb.workbench.plotting.tools.fittingTool");
 				if (fittingTool != null) {
 					List<IPeak> fittedPeaks = (List<IPeak>) fittingTool.getAdapter(IPeak.class);
 
 					if (fittedPeaks != null) {
 						Collections.sort(fittedPeaks, new Compare());
 
 						ArrayList<IPeak> peaks = new ArrayList<IPeak>(fittedPeaks.size());
 						if (peaks != null && peaks.size() > 0)
 							peaks.clear();
 						for (IPeak peak : fittedPeaks) {
 							peaks.add(peak);
 						}
 						return peaks;
 					}
 				}
 				
 				return null;
 			}
 
 			@Override
 			public void run() {
 				final IPlottingSystem plotter = getPlottingSystem();
 				final IImageTrace t = getImageTrace();
 				if (tmpRegion instanceof EllipseFitSelection || tmpRegion instanceof CircleFitSelection) {
 					final Display display = control.getDisplay();
 					if (t != null) {
 						Job job = new Job("Circle fit refinement") {
 							@Override
 							protected IStatus run(final IProgressMonitor monitor) {
 								return runEllipseFit(monitor, display, plotter, t, tmpRegion.getROI(), true);
 							}
 						};
 						job.setPriority(Job.SHORT);
 //						job.setUser(true);
 						job.schedule();
 
 					}
 					return;
 				}
 				try {
 					
 					Collection<IRegion> regions = plotter.getRegions(RegionType.SECTOR);
 					if (regions.size() == 0) {
 						throw new IllegalStateException();
 					}
 					SectorROI sroi = (SectorROI) regions.iterator().next().getROI();
 					AbstractDataset dataset = t.getData();
 					AbstractDataset mask = t.getMask();
 					final BeamCenterRefinement beamOffset = new BeamCenterRefinement(dataset, mask, sroi);
 					List<IPeak> peaks = loadPeaks();
 					if (peaks==null) throw new Exception("Cannot find peaks!");
 					beamOffset.setInitPeaks(peaks);
 					
 					beamOffset.optimize(sroi.getPoint());
 				} catch (Throwable ne) {
 					
 					/**
 					 * Long discussion with Iralki on this. The algorithm must be set up in a particular way to 
 					 * run at the moment. 
 					 */
 					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
 							"Experimental Refinement Algorithm Uncomplete",
 							null,
 							"Could not read peak positons to start refinement.\nThere is a process to set up the refinement because it is in an experimental form at the moment:\n\n"+
 							"1. Open the 'Diffraction' tool in a dedicated view (action on the right of the toolbar).\n"+
 							"2. Open the 'Radial Profile' tool (from the plot containing the image).\n" +
 							"3. Select a sector which bisects the rings wanted.\n"+
 							"4. In this 'Radial Profile' tool select peak fitting.\n"+
 							"5. Set up a peak fit on all the rings which the redial profile found.\n"+
 							"6. Now run the refine action in the diffraction tool again.\n\n"+
 							"Please note that the algorithm may not converge. A job is run for the refinement which may be stopped.\n"+
 							"Please contact your support representative for more training/help with refinement.\n\n"+
 							"(NOTE This dialog can be kept open as a guide while doing the proceedure.)",
 							MessageDialog.INFORMATION,
 							new String[]{IDialogConstants.OK_LABEL},
 							0);
 					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
 					dialog.open();
 				}
 			}
 		};
 		refine.setImageDescriptor(Activator.getImageDescriptor("icons/refine.png"));
 		refine.setEnabled(false);
 
 		findOuter = new Action("Find outer rings", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				logger.debug("Find more clicked");
 
 				if (tmpRegion instanceof CircleFitSelection || tmpRegion instanceof EllipseFitSelection) {
 					final IPlottingSystem plotter = getPlottingSystem();
 					final IImageTrace t = getImageTrace();
 					final Display display = control.getDisplay();
 					if (t != null) {
 						Job job = new Job("Ellipse rings finding") {
 							@Override
 							protected IStatus run(final IProgressMonitor monitor) {
 								ROIBase roi = tmpRegion.getROI();
 								IStatus stat = runEllipseFit(monitor, display, plotter, t, roi, false);
 								if (stat.isOK()) {
 									stat = runFindOuterRings(monitor, display, plotter, t, roi);
 								}
 								return stat;
 							}
 						};
 						job.setPriority(Job.SHORT);
 //						job.setUser(true);
 						job.schedule();
 					}
 					return;
 				} else {
 					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
 							"Rings locator - no initial ring",
 							null,
 							"Please define an initial ring",
 							MessageDialog.INFORMATION,
 							new String[]{IDialogConstants.OK_LABEL},
 							0);
 					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
 					dialog.open();
 
 				}
 			}
 		};
 		findOuter.setImageDescriptor(Activator.getImageDescriptor("icons/findmorerings.png"));
 		findOuter.setToolTipText("Find outer rings");
 		findOuter.setEnabled(false);
 
 		calibrate = new Action("Calibrate against standard", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
 				String name = standards.getSelectedCalibrant();
 				if (name != null) {
 					logger.debug("Calibrating against {}", name);
 					final List<HKL> spacings = standards.getCalibrationPeakMap(name).getHKLs();
 
 					final IPlottingSystem plotter = getPlottingSystem();
 					final IImageTrace t = getImageTrace();
 					final Display display = control.getDisplay();
 					if (t != null) {
 						Job job = new Job("Calibrating detector") {
 							@Override
 							protected IStatus run(final IProgressMonitor monitor) {
 								return runCalibrateDetector(monitor, display, plotter, spacings);
 							}
 						};
 						job.setPriority(Job.SHORT);
 //						job.setUser(true);
 						job.schedule();
 
 					}
 					return;
 				} else {
 					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
 							"Calibrator - no standard selected",
 							null,
 							"Please define calibrant",
 							MessageDialog.INFORMATION,
 							new String[]{IDialogConstants.OK_LABEL},
 							0);
 					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
 					dialog.open();
 
 				}
 			}
 		};
 		calibrate.setImageDescriptor(Activator.getImageDescriptor("icons/findmorerings.png"));
 		calibrate.setToolTipText("Calibrate detector using rings");
 		calibrate.setEnabled(false);
 
 		if (lock==null) lock = new Action("Lock the diffraction data and apply it to newly opened files.",IAction.AS_CHECK_BOX) {
 		    @Override
 			public void run() {
 		    	if (isChecked()) {
 		    		IDiffractionMetadata data = activeDiffractionTool.getDiffractionMetaData().clone();
 		    		service.setLockedDiffractionMetaData(data);
 		    	} else {
 		    		service.setLockedDiffractionMetaData(null);
 		    	}
 		    	activeDiffractionTool.createDiffractionModel(true);
 			}
 		};
 		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));
 
 	
 		this.calPref = new Action("Configure Calibrants...") {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		
 		Action configDetectors = new Action("Configure Detectors...") {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionDetectorPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		
 		Action configDefaultMeta = new Action("Configure Default Metadata...") {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionDefaultsPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		
 
 		this.calibrantActions = new MenuAction("Calibrants");
 		calibrantActions.setImageDescriptor(Activator.getImageDescriptor("/icons/calibrant_rings.png"));
 		updateCalibrationActions(CalibrationFactory.getCalibrationStandards());		
 		
 
 		MenuAction dropdown = new MenuAction("Resolution rings");
 	    dropdown.setImageDescriptor(Activator.getImageDescriptor("/icons/resolution_rings.png"));
 
 		augmenter = new DiffractionImageAugmenter((AbstractPlottingSystem)getPlottingSystem());
 	    augmenter.addActions(dropdown);
 		
 	    toolMan.add(exportMeta);
 	    toolMan.add(importMeta);
 		toolMan.add(new Separator());
 	    toolMan.add(lock);
 		toolMan.add(new Separator());
 	    toolMan.add(dropdown);
 	    toolMan.add(calibrantActions);
 		toolMan.add(new Separator());
 		toolMan.add(centre);
 		toolMan.add(fitRing);
 		toolMan.add(refine);
 		toolMan.add(findOuter);
 		toolMan.add(calibrate);
 		toolMan.add(new Separator());
 		toolMan.add(reset);
 		toolMan.add(resetAll);
 		toolMan.add(new Separator());
 		toolMan.add(showDefault);
 		toolMan.add(new Separator());
 		
 		menuMan.add(dropdown);
 	    menuMan.add(centre);
 	    menuMan.add(fitRing);
 	    menuMan.add(refine);
 	    menuMan.add(findOuter);
 	    menuMan.add(calibrate);
 		menuMan.add(new Separator());
 		menuMan.add(reset);
 		menuMan.add(resetAll);
 		menuMan.add(new Separator());
 		menuMan.add(copy);
 		menuMan.add(paste);
 		menuMan.add(new Separator());
 		menuMan.add(showDefault);
 		menuMan.add(new Separator());
 		menuMan.add(calPref);
 		
 		final Menu menu = menuMan.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 
 		getSite().getActionBars().getMenuManager().add(new Separator());
 		getSite().getActionBars().getMenuManager().add(calPref);
 		getSite().getActionBars().getMenuManager().add(configDetectors);
 		getSite().getActionBars().getMenuManager().add(configDefaultMeta);
 		getSite().getActionBars().getMenuManager().add(new Separator());
 	}
 
 	private static final String RING_PREFIX = "Ring";
 	private void clearRegions(IPlottingSystem plotter) {
 		Collection<IRegion> regions = plotter.getRegions();
 		for (IRegion r : regions) {
 			String n = r.getName();
 			if (n.startsWith(RING_PREFIX))
 				plotter.removeRegion(r);
 		}
 	}
 
 	private IStatus runEllipseFit(final IProgressMonitor monitor, Display display, final IPlottingSystem plotter, IImageTrace t, ROIBase roi, final boolean forceCircle) {
 		if (roi == null)
 			return Status.CANCEL_STATUS;
 
 		final boolean circle = forceCircle ? true : (roi instanceof CircularROI);
 		String shape = circle ? "circle" : "ellipse";
 		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 		monitor.beginTask("Refine " + shape + " fit", IProgressMonitor.UNKNOWN);
 		monitor.subTask("Find POIs near initial " + shape);
 		AbstractDataset image = t.getData();
 		BooleanDataset mask = (BooleanDataset) t.getMask();
 		PolylineROI points;
 		EllipticalFitROI efroi;
 		monitor.subTask("Fit POIs");
 		points = roi instanceof CircularROI ? PowderRingsUtils.findPOIsNearCircle(mon, image, mask, (CircularROI) roi)
 				: PowderRingsUtils.findPOIsNearEllipse(mon, image, mask, (EllipticalROI) roi);
 
 		monitor.subTask("Trim POIs");
 		efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 2, circle);
 
 		int npts = efroi.getPoints().getNumberOfPoints();
 		int lpts;
 		do {
 			lpts = npts;
 			points = PowderRingsUtils.findPOIsNearEllipse(mon, image, mask, (EllipticalROI) efroi);
 
 			efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 2, circle);
 			npts = efroi.getPoints().getNumberOfPoints(); 
 		} while (lpts > npts);
 
		final ROIBase froi = circle ? new CircularFitROI(efroi.getPoints()) : efroi.getPoints();
 		monitor.worked(1);
 		logger.debug("{} from peaks: {}", shape, froi);
 
 		final boolean[] status = {true};
 		display.syncExec(new Runnable() {
 
 			public void run() {
 				try {
 					IRegion region = plotter.createRegion(RegionUtils.getUniqueName("Pixel peaks", plotter), circle ? RegionType.CIRCLEFIT : RegionType.ELLIPSEFIT);
 					region.setROI(froi);
 					region.setRegionColor(circle ? ColorConstants.cyan : ColorConstants.orange);
 					plotter.removeRegion(tmpRegion);
 					monitor.subTask("Add region");
 					tmpRegion = region;
 					tmpRegion.setUserRegion(false);
 					tmpRegion.addROIListener(roiListener);
 					roiListener.roiSelected(new ROIEvent(tmpRegion, froi)); // trigger beam centre update
 					plotter.addRegion(region);
 					monitor.worked(1);
 				} catch (Exception e) {
 					status[0] = false;
 				}
 			}
 		});
 
 		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
 	}
 
 	private IStatus runFindOuterRings(final IProgressMonitor monitor, Display display, final IPlottingSystem plotter, IImageTrace t, ROIBase roi) {
 		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 		monitor.beginTask("Find elliptical rings", IProgressMonitor.UNKNOWN);
 		monitor.subTask("Find rings");
 		if (roi instanceof CircularFitROI) {
 			roi = new EllipticalFitROI(((CircularFitROI) roi).getPoints(), true);
 		}
 		final List<EllipticalROI> ells = PowderRingsUtils.findOtherEllipses(mon, t.getData(), (BooleanDataset) t.getMask(), (EllipticalROI) roi);
 		final boolean[] status = {true};
 		display.syncExec(new Runnable() {
 
 			public void run() {
 				try {
 					int emax = ells.size();
 					if (emax > 0)
 						plotter.removeRegion(tmpRegion);
 					for (int i = 0; i < emax; i++) {
 						monitor.subTask("Add region: " + i);
 						EllipticalROI e = ells.get(i);
 						logger.debug("Ellipse from peaks: {}, {}", i, e);
 						IRegion region = plotter.createRegion(RegionUtils.getUniqueName(RING_PREFIX, plotter), e instanceof EllipticalFitROI ? RegionType.ELLIPSEFIT : RegionType.ELLIPSE);
 						region.setMobile(false);
 						region.setROI(e);
 						region.setRegionColor(ColorConstants.orange);
 						region.setUserRegion(false);
 						plotter.addRegion(region);
 						monitor.worked(1);
 					}
 					calibrate.setEnabled(true);
 				} catch (Exception e) {
 					status[0] = false;
 				}
 			}
 		});
 
 		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
 	}
 
 	private IStatus runCalibrateDetector(final IProgressMonitor monitor, Display display, final IPlottingSystem plotter, List<HKL> spacings) {
 		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 		monitor.beginTask("Calibrate detector from rings", IProgressMonitor.UNKNOWN);
 		monitor.subTask("Find rings");
 		Collection<IRegion> regions = plotter.getRegions();
 		List<EllipticalROI> rois = new ArrayList<EllipticalROI>();
 		for (IRegion r : regions) {
 			String n = r.getName();
 			if (n.startsWith(RING_PREFIX))
 				rois.add((EllipticalROI) r.getROI());
 		}
 		monitor.worked(1);
 		
 		monitor.subTask("Fit detector");
 		try {
 			IDiffractionMetadata md = getDiffractionMetaData();
 			final DetectorProperties det = md.getDetector2DProperties();
 			final DiffractionCrystalEnvironment env = md.getDiffractionCrystalEnvironment();
 			final QSpace q = PowderRingsUtils.fitEllipsesToQSpace(mon, det, env, rois, spacings);
 			if (q == null)
 				return Status.CANCEL_STATUS;
 			display.syncExec(new Runnable() {
 				@Override
 				public void run() {
 					det.setGeometry(q.getDetectorProperties());
 					env.setWavelength(q.getWavelength());
 				}
 			});
 		} catch (Exception e) {
 			return Status.CANCEL_STATUS;
 		}
 
 		return Status.OK_STATUS;
 	}
 	
 	private void updateCalibrationActions(final CalibrationStandards standards) {
 		this.calibrantActions.clear();
 		final String selected = standards.getSelectedCalibrant();
 		final CheckableActionGroup grp = new CheckableActionGroup();
 		Action selectedAction=null;
 		for (final String calibrant : standards.getCalibrantList()) {
 			final Action calibrantAction = new Action(calibrant, IAction.AS_CHECK_BOX) {
 				public void run() {
 					standards.setSelectedCalibrant(calibrant, true);
 				}
 			};
 			grp.add(calibrantAction);
 			if (selected!=null&&selected.equals(calibrant)) selectedAction = calibrantAction;
 			calibrantActions.add(calibrantAction);
 		}
 		calibrantActions.addSeparator();
 		calibrantActions.add(calPref);
 		if (selected!=null) selectedAction.setChecked(true);
 	}
 
 	private void createListeners() {
 		
 		this.regionListener = new IRegionListener.Stub() {
 			@Override
 			public void regionAdded(RegionEvent evt) {
 				//test if our region
 				if (evt.getRegion() == tmpRegion) {
 //					logger.debug("Region added (type: {})", tmpRegion.getRegionType());
 					double[] point = tmpRegion.getROI().getPointRef();
 //					logger.debug("Clicked here X: {} Y : {}", point[0], point[1]);
 
 					if (tmpRegion.getRegionType() == RegionType.POINT)
 						getPlottingSystem().removeRegion(tmpRegion);
 					IDiffractionMetadata data = getDiffractionMetaData();
 					DetectorProperties detprop = data.getDetector2DProperties();
 					detprop.setBeamCentreCoords(point);
 					if (!augmenter.isShowingBeamCenter()) {
 						augmenter.drawBeamCentre(true);
 					}
 					((AbstractSelectionRegion) tmpRegion).setShowLabel(true);
 				}
 			}
 		};
 
 		roiListener = new IROIListener.Stub() {
 			@Override
 			public void update(ROIEvent evt) {
 				ROIBase r = evt.getROI();
 				if (r instanceof CircularFitROI || (r instanceof EllipticalFitROI && ((EllipticalFitROI) r).isCircular())) {
 					double[] point = r.getPointRef();
 //					logger.debug("ROI moved here X: {} Y : {}", point[0], point[1]);
 					IDiffractionMetadata data = getDiffractionMetaData();
 					DetectorProperties detprop = data.getDetector2DProperties();
 					detprop.setBeamCentreCoords(point);
 					if (!augmenter.isShowingBeamCenter()) {
 						augmenter.drawBeamCentre(true);
 					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public Control getControl() {
 		return control;
 	}
 
 	@Override
 	public void setFocus() {
 		if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
 	}
 
 	@Override
 	public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
 		updateCalibrationActions((CalibrationStandards)evt.getSource());
 	}
 }
