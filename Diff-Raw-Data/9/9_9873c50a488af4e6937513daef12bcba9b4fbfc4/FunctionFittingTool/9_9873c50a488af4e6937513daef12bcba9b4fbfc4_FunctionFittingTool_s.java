 package org.dawnsci.plotting.tools.fitting;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
 import org.dawb.common.ui.wizard.persistence.PersistenceImportWizard;
 import org.dawb.workbench.jmx.UserPlotBean;
 import org.dawnsci.common.widgets.gda.function.FunctionDialog;
 import org.dawnsci.common.widgets.gda.function.FunctionWidget;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunctionService;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.StraightLine;
 import uk.ac.diamond.scisoft.analysis.optimize.ApacheNelderMead;
 import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
 import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.gda.common.rcp.util.GridUtils;
 
 public class FunctionFittingTool extends AbstractToolPage implements IFunctionService {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(FunctionFittingTool.class);
 
 	private Composite composite;
 	protected IROIListener roiListener;
 	protected IRegion region = null;
 	private CompositeFunction compFunction = null;
 	protected ILineTrace estimate;
 	private TableViewer viewer;
 	private ILineTrace fitTrace;
 	private CompositeFunction resultFunction;
 	private FunctionWidget functionWidget;
 	private Integer selectedPosition;
 	private Action deleteAction;
 	private Action updateAction;
 	private UpdateFitPlotJob updateFittedPlotJob;
 	private Action updateAllAction;
 	private ITraceListener traceListener;
 	private Action addFunctionAction;
 
 	private Composite infoComposite;
 
 	private Label chiSquaredInfoLabel;
 
 	private Label chiSquaredValueLabel;
 	
 	private double accuracy = 0.0001;
 
 	private Label accurasyInfoLabel;
 
 	private Text accurasyValueText;
 
 	private Button refitButton;
 
 	private Action duplicateAction;
 
 	private Action importAction;
 
 	private Action exportAction;
 
 	public FunctionFittingTool() {
 
 		roiListener = new IROIListener() {
 
 			@Override
 			public void roiSelected(ROIEvent evt) {
 				return;
 			}
 
 			@Override
 			public void roiDragged(ROIEvent evt) {
 				return;
 			}
 
 			@Override
 			public void roiChanged(ROIEvent evt) {
 				updateFunctionPlot();
 			}
 		};
 
 		traceListener = new ITraceListener() {
 
 			boolean updating = false;
 
 			private void update() {
 				if (!updating) {
 					try {
 						updating = true;
 						updateFunctionPlot();
 					} finally {
 						updating = false;
 					}
 				}
 			}
 
 			@Override
 			public void tracesUpdated(TraceEvent evt) {
 			}
 
 			@Override
 			public void tracesRemoved(TraceEvent evet) {
 			}
 
 			@Override
 			public void tracesAdded(TraceEvent evt) {
 				update();
 			}
 
 			@Override
 			public void traceWillPlot(TraceWillPlotEvent evt) {
 			}
 
 			@Override
 			public void traceUpdated(TraceEvent evt) {
 			}
 
 			@Override
 			public void traceRemoved(TraceEvent evt) {
 			}
 
 			@Override
 			public void traceCreated(TraceEvent evt) {
 			}
 
 			@Override
 			public void traceAdded(TraceEvent evt) {
 				update();
 			}
 		};
 
 		// Initialise with a simple function.
 		compFunction = new CompositeFunction();
 		compFunction.addFunction(new StraightLine(new double[] { 0, 0 }));
 
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_1D;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 
 		createActions();
 
 		composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(composite);
 
 		infoComposite = new Composite(composite, SWT.NONE);
 		infoComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		infoComposite.setLayout(new GridLayout(2, false));
 		
 		chiSquaredInfoLabel = new Label(infoComposite, SWT.NONE);
 		chiSquaredInfoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		chiSquaredInfoLabel.setText("Normalised goodness of fit");
 		
 		chiSquaredValueLabel = new Label(infoComposite, SWT.NONE);
 		chiSquaredValueLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		chiSquaredValueLabel.setText("Not Calculated"); 
 		
 		accurasyInfoLabel = new Label(infoComposite, SWT.NONE);
 		accurasyInfoLabel.setText("Accuracy of Fitting Routine");
 		
 		accurasyValueText = new Text(infoComposite, SWT.BORDER);
 		accurasyValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		accurasyValueText.setText(Double.toString(accuracy));
 		accurasyValueText.addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				try {
 					accuracy = Double.parseDouble(accurasyValueText.getText());
 				} catch (Exception exception) {
 					logger.debug("Had a problem whilst passing the accurasy String",e);
 				}
 			}
 		});
 		
 		refitButton = new Button(infoComposite, SWT.TOGGLE);
 		refitButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		refitButton.setText("Auto Refit");
 		
 		refitButton.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseUp(MouseEvent e) {
 				if(refitButton.getSelection()) {
 					updateFunctionPlot();
 				} else {
 					getPlottingSystem().removeTrace(fitTrace);
 				}
 			}
 			
 			@Override
 			public void mouseDown(MouseEvent e) {
 				return;
 			}
 			
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				return;
 			}
 		});
 		
 		combo = new CCombo(infoComposite, SWT.BORDER);
 		combo.setEditable(false);
 		combo.setListVisible(true);
 		combo.setItems(new String[] {"Nelder Mead Fitting", "Genetic Algorithm"});
 		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		combo.select(0);
 		
 		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE
 				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 
 		TableViewerColumn tblCol1 = new TableViewerColumn(viewer, SWT.NONE);
 		tblCol1.getColumn().setText("Function Name");
 		tblCol1.getColumn().setWidth(120);
 
 		TableViewerColumn tblCol2 = new TableViewerColumn(viewer, SWT.NONE);
 		tblCol2.getColumn().setText("Initial Parameters");
 		tblCol2.getColumn().setWidth(150);
 
 		TableViewerColumn tblCol3 = new TableViewerColumn(viewer, SWT.NONE);
 		tblCol3.getColumn().setText("Fitted Parameters");
 		tblCol3.getColumn().setWidth(200);
 
 		viewer.getTable().setLinesVisible(true);
 		viewer.getTable().setHeaderVisible(true);
 		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		viewer.setLabelProvider(new TableLabelProvider());
 		viewer.setContentProvider(new ContentProvider());
 
 		viewer.setInput(compFunction);
 		viewer.getTable().addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				selectedPosition = (Integer) viewer.getTable().getSelection()[0]
 						.getData();
 				updateFunctionWidget();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		final MenuManager menuManager = new MenuManager();
 		menuManager.add(deleteAction);
 		menuManager.add(updateAction);
 		menuManager.add(addFunctionAction);
 		menuManager.add(duplicateAction);
 		viewer.getControl().setMenu(
 				menuManager.createContextMenu(viewer.getControl()));
 
 		functionWidget = new FunctionWidget(composite);
 		functionWidget
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 
 					@Override
 					public void selectionChanged(SelectionChangedEvent event) {
 						compFunction.setFunction(selectedPosition,
 								functionWidget.getFunction());
 						viewer.refresh();
 						updateFunctionPlot();
 					}
 				});
 	}
 
 	private void updateFunctionWidget() {
 		functionWidget.setFunction(compFunction
 				.getFunction(selectedPosition));
 	}
 	
 	@Override
 	public Control getControl() {
 		return composite;
 	}
 
 	@Override
 	public void setFocus() {
 		composite.setFocus();
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		try {
 			getPlottingSystem().addTraceListener(traceListener);
 
 			region = getPlottingSystem().getRegion("fit_region");
 			if (region == null) {
 				region = getPlottingSystem().createRegion("fit_region",
 						RegionType.XAXIS);
 
 				region.setROI(new RectangularROI(getPlottingSystem()
 						.getSelectedXAxis().getLower(), 0, getPlottingSystem()
 						.getSelectedXAxis().getUpper()
 						- getPlottingSystem().getSelectedXAxis().getLower(),
 						100, 0));
 				getPlottingSystem().addRegion(region);
 			} else {
 				region.setVisible(true);
 			}
 			region.addROIListener(roiListener);
 			updateFunctionPlot();
 
 		} catch (Exception e) {
 			logger.error("Failed to Activate Function fitting tool", e);
 		}
 	}
 
 	@Override
 	public void deactivate() {
 		if (region != null) {
 			region.removeROIListener(roiListener);
 			region.setVisible(false);
 		}
 		Collection<ITrace> traces = getPlottingSystem().getTraces();
 		if (traces.contains(estimate)) getPlottingSystem().removeTrace(estimate);
 		if (traces.contains(fitTrace)) getPlottingSystem().removeTrace(fitTrace);
 		
 		getPlottingSystem().removeTraceListener(traceListener);
 		
 		super.deactivate();
 	}
 
 	private void setChiSquaredValue(double value) {
 		chiSquaredValueLabel.setText(Double.toString(value)); 
 	}
 	
 	private void createActions() {
 		// export action
 		exportAction = new Action("Export Functions",
 				Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
 			public void run() {
 				try {
 					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
 					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 					wd.setTitle(wiz.getWindowTitle());
 					wd.open();
 				} catch (Exception e) {
 					logger.error("Problem opening import!", e);
 				}
 			}
 		};
 		exportAction
 		.setToolTipText("Export function data from an H5 file");
 		getSite().getActionBars().getToolBarManager().add(exportAction);
 
 		// import action
 		importAction = new Action("Import Functions",
 				Activator.getImageDescriptor("icons/mask-import-wiz.png")) {
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
 		importAction
 		.setToolTipText("Import function data from an H5 file");
 		getSite().getActionBars().getToolBarManager().add(importAction);
 
 		// Add Function action
 		addFunctionAction = new Action("Add a new Function",
 				Activator.getImageDescriptor("icons/add.png")) {
 			public void run() {
 				compFunction.addFunction(showFunctionDialog(null));
 				viewer.refresh();
 				updateFunctionPlot();
 				updateFunctionWidget();
 			}
 		};
 		addFunctionAction.setToolTipText("Add a new function to be fitted");
 		getSite().getActionBars().getToolBarManager().add(addFunctionAction);
 
 		// Delete function action
 		deleteAction = new Action("Delete function selected",
 				Activator.getImageDescriptor("icons/delete.gif")) {
 			public void run() {
 				Integer index = (Integer) viewer.getTable().getSelection()[0]
 						.getData();
 				compFunction.removeFunction(index);
 				if (resultFunction != null) {
 					resultFunction.removeFunction(index);
 				}
 				viewer.refresh();
 				updateFunctionPlot();
 				updateFunctionWidget();
 			}
 		};
 		deleteAction
 				.setToolTipText("Delete the selected function from the list");
 		getSite().getActionBars().getToolBarManager().add(deleteAction);
 		
 		// Duplicate a function
 		duplicateAction = new Action("Duplicate Selected Function",
 				Activator.getImageDescriptor("icons/copy.gif")) {
 			public void run() {
 				Integer index = (Integer) viewer.getTable().getSelection()[0]
 						.getData();
 				try {
 					compFunction.addFunction(compFunction.getFunction(index).copy());
 				} catch (Exception e) {
 					logger.error("Could not copy function",e);
 				}
 				viewer.refresh();
 				updateFunctionPlot();
 				updateFunctionWidget();
 			}
 		};
 		duplicateAction
 		.setToolTipText("Duplicate the current function");
 		getSite().getActionBars().getToolBarManager().add(duplicateAction);
 
 		// Update the parameters
 		updateAction = new Action("Update Single Parameters",
 				Activator.getImageDescriptor("icons/copy.gif")) {
 			public void run() {
 				Integer index = (Integer) viewer.getTable().getSelection()[0]
 						.getData();
 				compFunction.getFunction(index).setParameterValues(
 						resultFunction.getFunction(index).getParameterValues());
 				viewer.refresh();
 				updateFunctionPlot();
 				updateFunctionWidget();
 			}
 		};
 		updateAction
 				.setToolTipText("Update the initial parameters to the fitted parameters for this Function");
 		getSite().getActionBars().getToolBarManager().add(updateAction);
 
 		// Update all the parameters
 		updateAllAction = new Action("Update All Parameters",
 				Activator.getImageDescriptor("icons/apply.gif")) {
 			public void run() {
 				compFunction.setParameterValues(resultFunction
 						.getParameterValues());
 				viewer.refresh();
 				updateFunctionPlot();
 				updateFunctionWidget();
 			}
 		};
 		updateAllAction
 				.setToolTipText("Update the initial parameters to the fitted parameters for All Functions");
 		getSite().getActionBars().getToolBarManager().add(updateAllAction);
 	}
 
 	private void updateFunctionPlot() {
 		boolean firstTrace = true;
 		for (ITrace selectedTrace : getPlottingSystem().getTraces()) {
 			if (selectedTrace instanceof ILineTrace) {
 				if (selectedTrace.isUserTrace() && firstTrace) {
 					firstTrace = false;
 					ILineTrace trace = (ILineTrace) selectedTrace;
 					// We chop x and y by the region bounds. We assume the
 					// plot is an XAXIS selection therefore the indices in
 					// y = indices chosen in x.
 					RectangularROI roi = (RectangularROI) region.getROI();
 
 					final double[] p1 = roi.getPointRef();
 					final double[] p2 = roi.getEndPoint();
 
 					// We peak fit only the first of the data sets plotted
 					// for now.
 					AbstractDataset x = (AbstractDataset)trace.getXData();
 					AbstractDataset y = (AbstractDataset)trace.getYData();
 
 					try {
 						AbstractDataset[] a = FittingUtils.xintersection(x, y,
 								p1[0], p2[0]);
 						x = a[0];
 						y = a[1];
 					} catch (Throwable npe) {
 						continue;
 					}
 
 					estimate = (ILineTrace) getPlottingSystem().getTrace(
 							"Estimate");
 					if (estimate == null) {
 						estimate = getPlottingSystem().createLineTrace(
 								"Estimate");
 						estimate.setUserTrace(false);
 						estimate.setTraceType(ILineTrace.TraceType.DASH_LINE);
 						getPlottingSystem().addTrace(estimate);
 					}
 
 					DoubleDataset functionData = compFunction.makeDataset(x);
 					estimate.setData(x, functionData);
 
 					System.out.println(x);
 					System.out.println(y);
 
 					getPlottingSystem().repaint();
 
 					updateFittedPlot(x, y);
 				}
 			}
 		}
 		if (viewer != null ) refreshViewer();
 	}
 
 	private void updateFittedPlot(final AbstractDataset x,
 			final AbstractDataset y) {
 
 		
 		if(refitButton != null && refitButton.getSelection()) {
 
 			if (updateFittedPlotJob == null) {
 				updateFittedPlotJob = new UpdateFitPlotJob("Update Fitted Plot");
 			}
 			updateFittedPlotJob.setData(x, y);
 			updateFittedPlotJob.schedule();
 		}
 
 	}
 
 	private AFunction showFunctionDialog(AFunction function) {
 		final FunctionDialog dialog = new FunctionDialog(new Shell(
 				Display.getDefault()));
 		dialog.create();
 		dialog.getShell().setSize(650, 500);
 		dialog.getShell().setText("Edit Function");
 
 		if (function != null) {
 			dialog.setFunction(function);
 		}
 
 		dialog.open();
 		return dialog.getFunction();
 
 	}
 
 	private class TableLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			Integer index = (Integer) element;
 			String result = "Not Defined";
 			switch (columnIndex) {
 			case 0:
 				try {
 					result = compFunction.getFunction(index).getName();
 				} catch (Exception e) {
 					logger.debug("Some error occured in the label provider of the Function fitting view.",e);
 				}
 				break;
 			case 1:
 				try {
 					result = Double.toString(compFunction.getFunction(index).getParameterValue(0));
 					for (int i = 1; i < compFunction.getFunction(index).getNoOfParameters(); i++) {
 						result += System.getProperty("line.separator");
 						result += compFunction.getFunction(index).getParameterValue(i);
 					}
 				} catch (Exception e) {
 					logger.debug("Some error occured in the label provider of the Function fitting view.",e);
 				}
 				break;
 			case 2:
 				try {
 					result = Double.toString(resultFunction.getFunction(index).getParameterValue(0));
 					for (int i = 1; i < resultFunction.getFunction(index).getNoOfParameters(); i++) {
 						result += System.getProperty("line.separator");
 						result += resultFunction.getFunction(index).getParameterValue(i);
 					}
 				} catch (Exception e) {
 					logger.debug("Some error occured in the label provider of the Function fitting view. Not Normally an Issue");
 				}
 				break;
 			}
 			return result;
 		}
 	}
 
 	private class ContentProvider implements IStructuredContentProvider {
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			return;
 
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof CompositeFunction) {
 				Integer count = ((CompositeFunction) inputElement)
 						.getNoOfFunctions();
 
 				Integer[] output = new Integer[count];
 				for (int i = 0; i < count; i++) {
 					output[i] = i;
 				}
 
 				return output;
 			}
 			return null;
 		}
 
 	}
 
 	private class UpdateFitPlotJob extends Job {
 
 		public UpdateFitPlotJob(String name) {
 			super(name);
 		}
 
 		private AbstractDataset x;
 		private AbstractDataset y;
 		private int index;
 
 		public void setData(AbstractDataset x, AbstractDataset y) {
 			this.x = x.clone();
 			this.y = y.clone();
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			
 			
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					fitTrace.setVisible(false);
 					getPlottingSystem().repaint();
 					index = combo.getSelectionIndex();
 				}
 			});
 
 			try {
 				resultFunction = compFunction.duplicate();
 				logger.debug("Accurasy is set to {}",accuracy);
 				IOptimizer fitMethod = null;
 				switch (index) {
 				case 0:
 					fitMethod = new ApacheNelderMead();
 					break;
 				case 1:
 					fitMethod = new GeneticAlg(accuracy);
 					break;
 				default:
 					fitMethod = new ApacheNelderMead();
 					break;
 				} 
 				
 				resultFunction = Fitter.fit(x, y, fitMethod,
 						resultFunction.getFunctions());
 			} catch (Exception e) {
 				return Status.CANCEL_STATUS;
 			}
 
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 
 					setChiSquaredValue(resultFunction.residual(true, y, x)/x.count());
 					
 					fitTrace = (ILineTrace) getPlottingSystem().getTrace("Fit");
 					if (fitTrace == null) {
 						fitTrace = getPlottingSystem().createLineTrace("Fit");
 						fitTrace.setUserTrace(false);
 						fitTrace.setLineWidth(2);
 						getPlottingSystem().addTrace(fitTrace);
 					}
 
 					System.out.println("Plotting");
 					System.out.println(resultFunction);
 					DoubleDataset resultData = resultFunction.makeDataset(x);
 					fitTrace.setData(x, resultData);
 					fitTrace.setVisible(true);
 
 					getPlottingSystem().repaint();
 					refreshViewer();
 				}
 			});
 
 			return Status.OK_STATUS;
 		}
 
 	}
 
 	private Map<String, Serializable> functions=null;
 	private CCombo combo;
 	/**
 	 * Override to set the tool data to something specific
 	 * @param toolData
 	 */
 	@Override
 	public void setToolData(Serializable toolData) {
 		
 		final UserPlotBean bean = (UserPlotBean)toolData;
 		functions = bean.getFunctions();
 		
 		compFunction = new CompositeFunction();
 		for (String key : functions.keySet()) {
 			if (functions.get(key) instanceof AFunction) {
 				AFunction function = (AFunction) functions.get(key);
 				compFunction.addFunction(function);
 				
 			}
 		}
 
 	}
 
 	/**
 	 * @see IToolPage.getToolData()
 	 */
 	@Override
 	public Serializable getToolData() {
 		
 		UserPlotBean bean = new UserPlotBean();
 		
 		int count = 0;
 		for (String key : functions.keySet()) {
 			functions.put(key, compFunction.getFunction(count));
 			count++;
 		}
 		
 		bean.setFunctions(functions); // We only set functions because it does a replace merge.
 		
 		return bean;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class key) {
 		if (key == IFunctionService.class) return this;
 		return super.getAdapter(key);
 	}
 
 	@Override
 	public Map<String, IFunction> getFunctions() {
 
 		HashMap<String, IFunction> functions = new HashMap<String, IFunction>();
 		
 		if (compFunction != null) {
 			for (int i = 0; i < compFunction.getNoOfFunctions(); i++) {
 				String key = String.format("%03d_initial_%s", i,compFunction.getFunction(i).getName());
 				functions.put(key, compFunction.getFunction(i));
 			}
 		}
 		
 		if(resultFunction != null) {
 			for (int i = 0; i < resultFunction.getNoOfFunctions(); i++) {
 				String key = String.format("%03d_result_%s", i,resultFunction.getFunction(i).getName());
 				functions.put(key, resultFunction.getFunction(i));
 			}
 		}
 		
 		return functions;
 	}
 
 	@Override
 	public void setFunctions(Map<String, IFunction> functions) {
 		// clear the composite function
 		compFunction = new CompositeFunction();
 		for (String key : functions.keySet()) {
 			if (key.contains("_initial_")) {
 				compFunction.addFunction((AFunction)functions.get(key));
 			}
 		}
 		
 		resultFunction = new CompositeFunction();
 		for (String key : functions.keySet()) {
 			if (key.contains("_result_")) {
 				resultFunction.addFunction((AFunction)functions.get(key));
 			}
 		}
 		
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				getPlottingSystem().repaint();
 				refreshViewer();
 			}
 		});
 	}
 	
 	private void refreshViewer() {
 		viewer.setInput(compFunction);
 		viewer.refresh();
 	}
 	
 }
