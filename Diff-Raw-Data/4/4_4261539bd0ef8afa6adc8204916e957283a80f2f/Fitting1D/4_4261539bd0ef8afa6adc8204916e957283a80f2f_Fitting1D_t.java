 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;
 
 import gda.analysis.functions.APeak;
 import gda.analysis.functions.CompositeFunction;
 import gda.analysis.functions.Gaussian;
 import gda.analysis.functions.IdentifiedPeak;
 import gda.analysis.functions.Lorentzian;
 import gda.analysis.functions.Offset;
 import gda.analysis.functions.PearsonVII;
 import gda.analysis.functions.PseudoVoigt;
 
 import java.awt.Color;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.ConcurrentModificationException;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.UUID;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ICellEditorListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.IAnalysisMonitor;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.optimize.ApacheNelderMead;
 import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
 import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
 import uk.ac.diamond.scisoft.analysis.optimize.NelderMead;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotException;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.OverlayType;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.PrimitiveType;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.VectorOverlayStyles;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.fitting.FittedPeakData;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.fitting.FittedPeakList;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.fitting.FittedPeakTableViewer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DConsumer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DProvider;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayProvider;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROITableViewer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectEvent;
 import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
 import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceInitializer;
 
 public class Fitting1D extends SidePlot implements Overlay1DConsumer, SelectionListener, ICellEditorListener,
 		ISelectionChangedListener {
 
 	transient private static final Logger logger = LoggerFactory.getLogger(Fitting1D.class);
 
 	private final FittedPeakList fittedPeakList = new FittedPeakList();
 	private FittedPeakTableViewer fittedPeakTable;
 	private PlotFittedPeaks fittedPlot;
 	private Overlay1DProvider oProvider;
 
 	private Combo peakType;
 	private Combo chooseDataCombo;
 	private Spinner numPeaks;
 	private Button fitPeaks;
 	private Button clearPeaks;
 	private Button showAllPeaks;
 
 	private int selectedAlg;
 	private int selectedPeak;
 	private String[] algNames;
 	private String[] peaknames;
 	private IOptimizer alg;
 
 	private List<DoubleDataset> dataSetList;
 	private DoubleDataset currentDataSet;
 	private List<AxisValues> xAxisList;
 	private AxisValues currentXAxis;
 	private APeak peakToFit;
 	private DoubleDataset slicedData;
 
 	private List<Integer> primitiveIDs = new ArrayList<Integer>();
 	private int draggingPrimID = -1;
 	private double[] startCoord = new double[2];
 
 	private double MAXYVALUE = Short.MAX_VALUE;
 
 	private double accuracy;
 	private int smoothing;
 	private boolean currentlyZooming = false;
 	private boolean autoStopping;
 	private boolean thresholdMeasure;
 	private double threshold;
 
 	private final Color colorButtonFitted = Color.BLUE;
 	private final Color colorDraggedFitted = Color.GREEN;
 
 	private String BUTTON_FITTING_UUID;
 	private String REFITTING_UUID;
 
 	@Override
 	public void registerProvider(OverlayProvider provider) {
 		oProvider = (Overlay1DProvider) provider;
 	}
 
 	@Override
 	public void removePrimitives() {
 		int primNumMax = primitiveIDs.size();
 		if (primNumMax > 0 && !mainPlotter.isDisposed()) {
 			oProvider.unregisterPrimitive(primitiveIDs);
 		}
 		if (draggingPrimID != -1) {
 			oProvider.unregisterPrimitive(draggingPrimID);
 			draggingPrimID = -1;
 		}
 	}
 
 	@Override
 	public void unregisterProvider() {
 		if (oProvider != null) {
 			removePrimitives();
 			oProvider = null;
 		}
 	}
 
 	private void overlaysVisible(boolean visible) {
 		if (oProvider != null) {
 			for (Integer i : primitiveIDs) {
 				oProvider.setPrimitiveVisible(i, visible);
 			}
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		IStructuredSelection selection = (IStructuredSelection) fittedPeakTable.getSelection();
 		if (selection != null) {
 			if (e.widget instanceof MenuItem) {
 				final FittedPeakData cRData = (FittedPeakData) selection.getFirstElement();
 				final int onum = fittedPeakList.indexOf(cRData); // index of selected overlay
 				switch (fittedPeakTable.getContextMenu().indexOf((MenuItem) e.widget)) {
 				case ROITableViewer.ROITABLEMENU_EDIT:
 
 					FitMenuDialog menu = new FitMenuDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
 							peaknames, algNames, selectedPeak, selectedAlg);
 					final boolean ok = menu.open();
 					if (!ok) {
 						overlaysVisible(false);
 						return;
 					}
 
 					APeak peak = cRData.getFittedPeak();
 					double min = peak.getPosition() - 2 * peak.getFWHM();
 					double max = peak.getPosition() + 2 * peak.getFWHM();
 
 					fitPeakFromOverlay(min, max, menu.getFitData());
 					fittedPeakList.remove(onum);
 					break;
 				case ROITableViewer.ROITABLEMENU_DELETE:
 					fittedPeakList.remove(onum);
 
 					peaksUpdated();
 					break;
 				case ROITableViewer.ROITABLEMENU_DELETE_ALL:
 					fittedPeakList.clear();
 					peaksUpdated();
 					break;
 				case ROITableViewer.ROITABLEMENU_COPY:
 					logger.warn("This function has not been enabled");
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void applyEditorValue() {
 		// not going to be implemented
 	}
 
 	@Override
 	public void cancelEditor() {
 		// not going to be implemented
 	}
 
 	@Override
 	public void editorValueChanged(boolean oldValidState, boolean newValidState) {
 		// not going to be implemented
 	}
 
 	@Override
 	public void addToHistory() {
 		// not going to be implemented
 	}
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 
 		parent.setLayout(new FillLayout());
 
 		container = new Composite(parent, SWT.NONE);
 		container.setLayout(new GridLayout(1, false));
 
 		// GUI creation and layout
 		final ScrolledComposite scrollControls = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
 		scrollControls.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false)); //
 
 		final Composite mainControls = new Composite(scrollControls, SWT.FILL);
 		mainControls.setLayout(new GridLayout(1, false));
 
 		final Group controls = new Group(mainControls, SWT.NONE);
 		controls.setLayout(new RowLayout(SWT.HORIZONTAL));
 		controls.setText("Choose data and fit");
 
 		chooseDataCombo = new Combo(controls, SWT.READ_ONLY);
 		chooseDataCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				dataComboChanged();
 			}
 		});
 		chooseDataCombo.add("                                       ");
 
 		fitPeaks = new Button(controls, SWT.NONE);
 		fitPeaks.setText("&Fit");
 		fitPeaks.addSelectionListener(fitPeakListener);
 
 		clearPeaks = new Button(controls, SWT.NONE);
 		clearPeaks.setText("Clear");
 		clearPeaks.addSelectionListener(clearPeaksListener);
 
 		showAllPeaks = new Button(controls, SWT.CHECK);
 		showAllPeaks.setText("Show all peaks");
 		showAllPeaks.setToolTipText("Show the position of all peaks as an overlay on the plot");
 		showAllPeaks.addSelectionListener(showAllPeaksOverlay);
 		showAllPeaks.setSelection(true);
 
 		ExpandableComposite advancedExpandableComposite = new ExpandableComposite(mainControls, SWT.NONE);
 		advancedExpandableComposite.setText("Advanced controls");
 		advancedExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 
 		Group advanced = new Group(advancedExpandableComposite, SWT.NONE);
 		advanced.setLayout(new GridLayout(4, false));
 
 		Label lab = new Label(advanced, SWT.NONE);
 		lab.setText("Peak Type");
 		peakType = new Combo(advanced, SWT.READ_ONLY);
 		peakType.addSelectionListener(peakTypeSelection);
 
 		lab = new Label(advanced, SWT.NONE);
 		lab.setText("Number of peaks");
 		numPeaks = new Spinner(advanced, SWT.NONE);
 		numPeaks.setMinimum(-1);
 		numPeaks.setIncrement(1);
 		numPeaks.setMaximum(200000);
 
 		advancedExpandableComposite.setClient(advanced);
 		ExpansionAdapter expansionListener = new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				mainControls.layout();
 				scrollControls.setContent(mainControls);
 				final Point controlsSize = mainControls.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 				mainControls.setSize(controlsSize);
 
 			}
 		};
 		advancedExpandableComposite.addExpansionListener(expansionListener);
 
 		final SashForm ss = new SashForm(container, SWT.VERTICAL);
 		ss.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		Composite peakTable = new Composite(ss, SWT.NONE);
 		peakTable.setLayout(new FillLayout());
 		fittedPeakTable = new FittedPeakTableViewer(peakTable, this, this, this);
 		fittedPeakTable.setInput(fittedPeakList);
 
 		fittedPlot = new PlotFittedPeaks(ss);
 
 		ss.setWeights(new int[] { 10, 20 });
 
 		scrollControls.setContent(mainControls);
 		final Point controlsSize = mainControls.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 		mainControls.setSize(controlsSize);
 		addPropertyListeners();
 		setupInitialValues();
 
 		parent.getShell().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				parent.layout();
 			}
 		});
 	}
 
 	@Override
 	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
 		Action action = super.createSwitchAction(index, plotUI);
 		action.setText("1D Fitting tool");
 		action.setId("uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.Fitting1DAction");
 		action.setToolTipText("Switch side plot to 1D fitting tool");
 		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/SidePlot-PeakFit.png"));
 
 		return action;
 	}
 
 	@Override
 	public void setMainPlotUI(final IPlotUI ui) {
 
 		if (mainPlotUI != null) {
 			final Plot1DUIAdapter ad = (Plot1DUIAdapter) ui;
 			ad.removeZoomListener(ZOOM_LISTENER);
 		}
 
 		super.setMainPlotUI(ui);
 
 		if (mainPlotUI != null) {
 			final Plot1DUIAdapter ad = (Plot1DUIAdapter) ui;
 			ad.addZoomListener(ZOOM_LISTENER);
 		} else {
 			logger.error("Unable to determine IPlotUI, so cannot ignore zoom events!");
 		}
 	}
 
 	private IPropertyChangeListener ZOOM_LISTENER = new IPropertyChangeListener() {
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			Fitting1D.this.currentlyZooming = (Boolean) event.getNewValue();
 		}
 	};
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (fittedPlot != null)
 			fittedPlot.dispose();
 	}
 
 	@Override
 	public void processPlotUpdate() {
 		try {
 			if (dataSetList == null) {
 				dataSetList = new ArrayList<DoubleDataset>();
 			}
 			dataSetList.clear();
 			// removePrimitives();
 			if (mainPlotter == null) {
 				return;
 			}
 			List<IDataset> dataList = mainPlotter.getCurrentDataSets();
 			if (dataList != null && !dataList.isEmpty()) {
 				xAxisList = mainPlotter.getXAxisValues();
 
 				for (IDataset data : dataList) {
 					if (MAXYVALUE < data.max().doubleValue()) {
 						MAXYVALUE = data.max().intValue() * 1.5;
 					}
 					if (-MAXYVALUE > data.min().doubleValue()) {
 						MAXYVALUE = -data.min().doubleValue() * 1.5;
 					}
 					dataSetList.add((DoubleDataset) DatasetUtils.cast(DatasetUtils.convertToAbstractDataset(data),
 							AbstractDataset.FLOAT64));
 				}
 			}
 
 			updateDataSetComboBox(dataSetList);
 		} catch (Throwable t) {
 			logger.error("a disaster: {}", t);
 		}
 	}
 
 	private void updateDataSetComboBox(List<DoubleDataset> dataList) {
 		String lastSelected = chooseDataCombo.getSelectionIndex() == -1 ? "no dataset" : chooseDataCombo
 				.getItem((chooseDataCombo.getSelectionIndex()));
 
 		chooseDataCombo.removeAll();
 		if (dataSetList != null && !dataSetList.isEmpty()) {
 
 			int i = 0;
 			for (DoubleDataset d : dataList) {
 				String name = d.getName();
 
 				if (name.isEmpty()) {
 					name = "Data Set " + i++;
 				}
 				chooseDataCombo.add(name);
 				if (name.equals(lastSelected)) {
 					chooseDataCombo.select(i);
 				}
 			}
 
 			if (chooseDataCombo.getSelectionIndex() == -1) {
 				chooseDataCombo.select(0);
 			}
 			setupSmoothing();
 
 		}
 		chooseDataCombo.update();
 		dataComboChanged();
 	}
 
 	private void refitPeaks() {
 
 		final List<APeak> newList = new ArrayList<APeak>(fittedPeakList.size());
 		final IOptimizer optimiser = alg;
 		final int smooth = smoothing;
 		final FittedPeakList currentPeakList = fittedPeakList;
 
 		IJobManager manager = Job.getJobManager();
 		// kill current fitting job is data changes
 		manager.cancel(BUTTON_FITTING_UUID);
 		// kill all running refit jobs
 		manager.cancel(REFITTING_UUID);
 		new refitPeaksOnNewDataset(newList, optimiser, smooth, currentPeakList, REFITTING_UUID).schedule();
 	}
 
 	private class refitPeaksOnNewDataset extends Job {
 
 		final List<APeak> newList;
 		final IOptimizer optimiser;
 		final int smooth;
 		final FittedPeakList currentPeakList;
 		private final static String name = "Refitting peaks on new Data";
 		private String UUID;
 
 		public refitPeaksOnNewDataset(List<APeak> newList, IOptimizer alg, int smooting, FittedPeakList fittedPeakList,
 				String UUID) {
 			super(name);
 			this.newList = newList;
 			this.optimiser = alg;
 			this.smooth = smooting;
 			this.currentPeakList = fittedPeakList;
 			this.UUID = UUID;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
 				for (FittedPeakData peakData : currentPeakList) {
 					if (monitor.isCanceled()) {
 						newList.clear();
 						return Status.CANCEL_STATUS;
 					}
 					APeak peak = peakData.getFittedPeak();
 
 					double min = peak.getPosition() - 2 * peak.getFWHM();
 					double max = peak.getPosition() + 2 * peak.getFWHM();
 					try {
 						List<APeak> fittedPeaks = Generic1DFitter.fitPeaks(createXData(min, max),
 								sliceDataSet(min, max), peak, optimiser, smooth, 1);
 						if (fittedPeaks != null) {
 							newList.addAll(fittedPeaks);
 						}
 					} catch (Throwable t) {
 						logger.info("exception trying to refit peaks {} - no problem usually", t);
 					}
 				}
 			} catch (ConcurrentModificationException e) {
 				logger.debug("This was caused by the fitting continuing after the job was canceled");
 			}
 			if (monitor.isCanceled())
 				return Status.CANCEL_STATUS;
 			UIJob updatePlot = new UIJob("Update Plot") {
 
 				@Override
 				public IStatus runInUIThread(IProgressMonitor monitor) {
 					fittedPeakList.clear();
 					addPeaksToList(newList, colorDraggedFitted);
 					return Status.OK_STATUS;
 				}
 			};
 			updatePlot.schedule();
 			return Status.OK_STATUS;
 
 		}
 
 		@Override
 		public boolean belongsTo(Object family) {
 			return UUID.equals(family);
 
 		}
 	}
 
 	private void clearPeakTable() {
 		fittedPeakList.clear();
 		peaksUpdated();
 	}
 
 	@Override
 	public void removeFromHistory() {
 
 	}
 
 	@Override
 	public void areaSelected(AreaSelectEvent event) {
 
 		// When the user is zooming, we do not want to do fitting.
 		if (currentlyZooming) {
 			overlaysVisible(false);
 			return;
 		}
 
 		if (currentDataSet != null) {
 			oProvider.begin(OverlayType.VECTOR2D);
 			if (draggingPrimID == -1) {
 				draggingPrimID = oProvider.registerPrimitive(PrimitiveType.BOX);
 			}
 			if (event.getMode() == 0) {
 				overlaysVisible(false);
 				oProvider.setColour(draggingPrimID, java.awt.Color.GREEN);
 				oProvider.setStyle(draggingPrimID, VectorOverlayStyles.FILLED_WITH_OUTLINE);
 				oProvider.setTransparency(draggingPrimID, 0.8);
 				oProvider.setOutlineTransparency(draggingPrimID, 0);
 				oProvider.setPrimitiveVisible(draggingPrimID, true);
 				startCoord[0] = event.getX();
 				startCoord[1] = MAXYVALUE;
 			}
 			if (event.getMode() == 1) {
 				double[] current = { event.getX(), -MAXYVALUE };
 				oProvider.drawBox(draggingPrimID, startCoord[0], startCoord[1], current[0], current[1]);
 			}
 			if (event.getMode() == 2) {
 				final double[] finalPos = { event.getX(), -MAXYVALUE };
 				oProvider.drawBox(draggingPrimID, startCoord[0], startCoord[1], finalPos[0], finalPos[1]);
 				slicedData = sliceDataSet(startCoord[0], finalPos[0]);
 
 				// Sometimes mainPlotter is null or the component above it is disposed.
 				// So we use the general platform display to show the fit dialog.
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						// Must use active shell as parent or icon is not correct on some platforms
 						// and it appears in a different window with a different window manager bar.
 						FitMenuDialog menu = new FitMenuDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
 								peaknames, algNames, selectedPeak, selectedAlg);
 						final boolean ok = menu.open();
 						if (!ok) {
 							overlaysVisible(false);
 							return;
 						}
 						fitPeakFromOverlay(startCoord[0], finalPos[0], menu.getFitData());
 
 					}
 				});
 				overlaysVisible(true);
 
 			}
 			oProvider.end(OverlayType.VECTOR2D);
 		}
 	}
 
 	private DoubleDataset sliceDataSet(double startPos, double endPos) {
 		if (startPos > endPos) {
 			double temp = startPos;
 			startPos = endPos;
 			endPos = temp;
 		}
 
 		int start = currentXAxis.nearestLowEntry(startPos);
 		if (start < 0) {
 			start = 0;
 		}
 		int stop = currentXAxis.nearestUpEntry(endPos);
 		if (stop < 0) {
 			stop = currentXAxis.size();
 		}
 
 		return currentDataSet.getSlice(new int[] { start }, new int[] { stop }, null);
 	}
 
 	private DoubleDataset createXData(double startPos, double endPos) {
 		if (endPos < startPos) {
 			double temp = startPos;
 			startPos = endPos;
 			endPos = temp;
 		}
 
 		int start = currentXAxis.nearestLowEntry(startPos);
 		if (start < 0) {
 			start = 0;
 		}
 		int stop = currentXAxis.nearestUpEntry(endPos);
 		if (stop < 0) {
 			stop = currentXAxis.size();
 		}
 		return currentXAxis.subset(start, stop).toDataset();
 	}
 
 	/**
 	 * Controls the fitting from clicking on the plot window. There are 2 paths from this method, a single peak fitted
 	 * from the selected region and the normal peak fitting for >1 peak selected. This method will be run as a job and
 	 * also update the GUI.
 	 * 
 	 * @param start
 	 *            start x click
 	 * @param finalpos
 	 *            final x click
 	 * @param fitData
 	 *            information from the popup menu
 	 */
 	public void fitPeakFromOverlay(final double start, final double finalpos, final FitData fitData) {
 		Job fitPeaksFromDragging = new Job("Fit peaks from dragging") {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				slicedData = sliceDataSet(start, finalpos);
 				DoubleDataset xData = createXData(start, finalpos);
 
 				selectPeakFuction(fitData.getPeakSelection());
 				selectAlg(fitData.getAlgType());
 				List<APeak> fittedPeaks = null;
 				if (fitData.getNumberOfPeaks() == 1) {
 					fittedPeaks = fitSinglePeak(xData, slicedData, alg, peakToFit, start, finalpos);
 				} else {
 					fittedPeaks = Generic1DFitter.fitPeaks(xData, slicedData, peakToFit, alg, fitData.getSmoothing(),
 							fitData.getNumberOfPeaks());
 				}
 				if (fittedPeaks == null || fittedPeaks.isEmpty()) {
 					logger.warn("No peaks were found when the region on the plot was selected");
 					return Status.OK_STATUS;
 				}
 				final List<APeak> currentFittedPeaks = fittedPeaks;
 				UIJob updateGUI = new UIJob("Updating GUI") {
 
 					@Override
 					public IStatus runInUIThread(IProgressMonitor monitor) {
 						addPeaksToList(currentFittedPeaks, colorDraggedFitted);
 						return Status.OK_STATUS;
 					}
 				};
 				updateGUI.schedule();
 				return Status.OK_STATUS;
 			}
 		};
 		fitPeaksFromDragging.schedule();
 
 	}
 
 	/**
 	 * @param xData
 	 * @param yData
 	 * @param fittingAlg
 	 * @param peak
 	 * @param start
 	 * @param finalpos
 	 * @return a list containing a single fitted peak
 	 */
 	private List<APeak> fitSinglePeak(final AbstractDataset xData, final AbstractDataset yData, IOptimizer fittingAlg,
 			APeak peak, double start, double finalpos) {
 
 		final List<APeak> fittedpeaks = new ArrayList<APeak>();
 		List<Double> crossings = new ArrayList<Double>();
 		final IOptimizer optomiser = fittingAlg;
 
 		double quartitle = (finalpos - start) / 4;
 		crossings.add(start + quartitle);
 		crossings.add(finalpos - quartitle);
 		final IdentifiedPeak idPeak = new IdentifiedPeak(xData.getDouble(Math.round(xData.getSize() / 2)),
 				xData.getDouble(0), xData.getDouble(xData.getSize() - 1), (Double) yData.sum(), yData.max()
 						.doubleValue() - yData.min().doubleValue(), currentXAxis.nearestLowEntry(start),
 				currentXAxis.nearestUpEntry(finalpos), crossings);
 		final APeak localPeak = generatePeak(idPeak, peak);
 
 		double lowOffset = yData.min().doubleValue();
 		double highOffset = (Double) yData.mean();
 		Offset offset = new Offset(lowOffset, highOffset);
 
 		CompositeFunction comp = new CompositeFunction();
 		comp.addFunction(localPeak);
 		comp.addFunction(offset);
 		try {
 			optomiser.optimize(new AbstractDataset[] { xData }, yData, comp);
 			fittedpeaks.add(localPeak);
 		} catch (Exception e) {
 			logger.error("Could not fit single peak", e);
 
 		}
 		return fittedpeaks;
 	}
 
 	private APeak generatePeak(IdentifiedPeak idPeak, APeak peak) {
 		APeak localPeak = null;
 		try {
 			Constructor<? extends APeak> ctor = peak.getClass().getConstructor(IdentifiedPeak.class);
 			localPeak = ctor.newInstance(idPeak);
 		} catch (IllegalArgumentException e1) {
 			logger.error("There was a problem optimising the peak", e1);
 		} catch (InstantiationException e1) {
 			logger.error("The function could not be created for fitting", e1);
 		} catch (NoSuchMethodException e1) {
 			logger.error("The peak function could not be created.", e1);
 		} catch (IllegalAccessException e1) {
 			logger.error("The function could not be created for fitting", e1);
 		} catch (InvocationTargetException e1) {
 			logger.error("The function could not be created for fitting", e1);
 		} catch (Exception e) {
 			logger.error("There was a problem creating the optimizer.", e);
 		}
 		return localPeak;
 	}
 
 	private void selectAlg(int algType) {
 		String currentAlgName = algNames[algType];
 
 		if (currentAlgName.equalsIgnoreCase("Genetic Algorithm"))
 			alg = new GeneticAlg(accuracy);
 		else if (currentAlgName.equalsIgnoreCase("Nelder Mead"))
 			alg = new NelderMead(accuracy);
 		else if (currentAlgName.equalsIgnoreCase("Apache Nelder Mead"))
 			alg = new ApacheNelderMead();
 		else
 			throw new IllegalArgumentException("Did not recognise the fitting routine");
 
 	}
 
 	private void addPeaksToList(List<APeak> fittedPeaks, Color color) {
 
 		if (fittedPeaks == null || fittedPeaks.size() < 1) {
 			clearPeakTable();
 			return;
 		}
 
 		for (APeak p : fittedPeaks) {
 			// would be nice to get rid of duplicates here
 			// in a clever way that would allow e.g. a Gaussian and a Lorentian to be fitted to the same peak
 			fittedPeakList.add(new FittedPeakData(p, color));
 		}
 		peaksUpdated();
 	}
 
 	private void selectPeakFuction(int peakNum) {
 		// "Gaussian", "Lorentzian", "Pearson VII", "PseudoVoigt"
 		String peakName = peaknames[peakNum];
 		if (peakName.compareToIgnoreCase("Gaussian") == 0) {
 			peakToFit = new Gaussian(1, 1, 1, 1);
 		} else if (peakName.compareToIgnoreCase("Lorentzian") == 0) {
 			peakToFit = new Lorentzian(1, 1, 1, 1);
 		} else if (peakName.compareToIgnoreCase("Pearson VII") == 0) {
 			peakToFit = new PearsonVII(1, 1, 1, 1);
 		} else if (peakName.compareToIgnoreCase("PseudoVoigt") == 0) {
 			peakToFit = new PseudoVoigt(1, 1, 1, 1);
 		} else {
 			logger.warn("Peak type not recognised. Defaulting to Gaussian");
 			peakToFit = new Gaussian(1, 1, 1, 1);
 			peakType.select(0); // displays gaussian in combo box
 		}
 	}
 
 	private SelectionAdapter peakTypeSelection = new SelectionAdapter() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			selectPeakFuction(peakType.getSelectionIndex());
 		}
 	};
 
 	private void dataComboChanged() {
 		if (dataSetList != null && dataSetList.size() > 0) {
 			currentDataSet = dataSetList.get(chooseDataCombo.getSelectionIndex());
 			currentXAxis = xAxisList.get(chooseDataCombo.getSelectionIndex());
 			setupSmoothing();
 			removeAllPeakOverlays();
 			refitPeaks();
 		}
 	}
 
 	private void setupSmoothing() {
 		if (currentDataSet != null) {
 			IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 			if (preferenceStore.getBoolean(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING)) {
 				smoothing = (int) (currentDataSet.getSize() * 0.01);
 			} else {
 				smoothing = preferenceStore.getInt(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE);
 			}
 		}
 	}
 
 	private void setupInitialValues() {
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		accuracy = preferenceStore.getDouble(PreferenceConstants.FITTING_1D_ALG_ACCURACY);
 		threshold = preferenceStore.getInt(PreferenceConstants.FITTING_1D_THRESHOLD);
 
 		String peaksList = preferenceStore.getString(PreferenceConstants.FITTING_1D_PEAKLIST);
 		String selectedPeakName = preferenceStore.getString(PreferenceConstants.FITTING_1D_PEAKTYPE);
 		populateAndSelectPeak(peaksList, selectedPeakName);
 
 		if (preferenceStore.getBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING)) {
 			numPeaks.setSelection(-1);
 			numPeaks.setEnabled(false);
 		} else {
 			numPeaks.setEnabled(true);
 			numPeaks.setSelection(preferenceStore.getInt(PreferenceConstants.FITTING_1D_PEAK_NUM));
 		}
 		autoStopping = preferenceStore.getBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING);
 		determineThresholdMeasure(preferenceStore.getString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE));
 		String algList = preferenceStore.getDefaultString(PreferenceConstants.FITTING_1D_ALG_LIST);
 		String algDefaultName = preferenceStore.getString(PreferenceConstants.FITTING_1D_ALG_TYPE);
 		populateAndSelectAlgnames(algList, algDefaultName);
 
 		// Adding UUID for fitting jobs
 		BUTTON_FITTING_UUID = UUID.randomUUID().toString();
 		REFITTING_UUID = UUID.randomUUID().toString();
 	}
 
 	private void populateAndSelectAlgnames(String algList, String algDefaultName) {
 		StringTokenizer st = new StringTokenizer(algList, PreferenceInitializer.DELIMITER);
 		algNames = new String[st.countTokens()];
 		int count = 0;
 		String algName = "";
 		while (st.hasMoreTokens()) {
 			String temp = st.nextToken();
 			algNames[count] = temp;
 			if (temp.equalsIgnoreCase(algDefaultName)) {
 				algName = temp;
				selectedAlg = count;
			}	
 			count++;
 		}
 
 		if (algName.equalsIgnoreCase("Genetic Algorithm"))
 			alg = new GeneticAlg(accuracy);
 		else if (algName.equalsIgnoreCase("Nelder Mead"))
 			alg = new NelderMead(accuracy);
 		else if (algName.equalsIgnoreCase("Apache Nelder Mead"))
 			alg = new ApacheNelderMead();
 		else
 			throw new IllegalArgumentException("Did not recognise the fitting routine");
 
 	}
 
 	private void populateAndSelectPeak(String peaksList, String defaultPeakName) {
 		StringTokenizer st = new StringTokenizer(peaksList, PreferenceInitializer.DELIMITER);
 		peaknames = new String[st.countTokens()];
 		int count = 0;
 		while (st.hasMoreTokens()) {
 			String temp = st.nextToken();
 			peaknames[count] = temp;
 			peakType.add(temp);
 			if (temp.equalsIgnoreCase(defaultPeakName)) {
 				selectedPeak = count;
 			}
 			count++;
 		}
 		peakType.select(selectedPeak);
 	}
 
 	private void addPropertyListeners() {
 		AnalysisRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				String property = event.getProperty();
 				IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 				if (property.equals(PreferenceConstants.FITTING_1D_ALG_ACCURACY)
 						|| property.equals(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE)
 						|| property.equals(PreferenceConstants.FITTING_1D_ALG_TYPE)
 						|| property.equals(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING)
 						|| property.equals(PreferenceConstants.FITTING_1D_PEAKTYPE)) {
 
 					String peakName;
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_PEAKTYPE)) {
 						peakName = preferenceStore.getDefaultString(PreferenceConstants.FITTING_1D_PEAKTYPE);
 					} else {
 						peakName = preferenceStore.getString(PreferenceConstants.FITTING_1D_PEAKTYPE);
 					}
 					for (int i = 0; i < peaknames.length; i++) {
 						if (peaknames[i].equalsIgnoreCase(peakName)) {
 							peakType.select(i);
 							break;
 						}
 					}
 
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_ALG_ACCURACY)) {
 						accuracy = preferenceStore.getDefaultDouble(PreferenceConstants.FITTING_1D_ALG_ACCURACY);
 					} else {
 						accuracy = preferenceStore.getDouble(PreferenceConstants.FITTING_1D_ALG_ACCURACY);
 					}
 
 					boolean autoSmooth;
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING)) {
 						autoSmooth = preferenceStore.getDefaultBoolean(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING);
 					} else {
 						autoSmooth = preferenceStore.getBoolean(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING);
 					}
 					if (autoSmooth) {
 						setupSmoothing();
 					} else {
 						if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE)) {
 							smoothing = preferenceStore.getDefaultInt(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE);
 						} else {
 							smoothing = preferenceStore.getInt(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE);
 						}
 					}
 
 					String algTypeList;
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_ALG_LIST)) {
 						algTypeList = preferenceStore.getDefaultString(PreferenceConstants.FITTING_1D_ALG_LIST);
 					} else {
 						algTypeList = preferenceStore.getString(PreferenceConstants.FITTING_1D_ALG_LIST);
 					}
 
 					StringTokenizer st = new StringTokenizer(algTypeList, PreferenceInitializer.DELIMITER);
 					algNames = new String[st.countTokens()];
 					int numAlgs = 0;
 					while (st.hasMoreTokens()) {
 						algNames[numAlgs++] = st.nextToken();
 					}
 
 					String algName;
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_ALG_TYPE))
 						algName = preferenceStore.getDefaultString(PreferenceConstants.FITTING_1D_ALG_TYPE);
 					else
 						algName = preferenceStore.getString(PreferenceConstants.FITTING_1D_ALG_TYPE);
 
 					if (algName.equalsIgnoreCase("Genetic Algorithm"))
 						alg = new GeneticAlg(accuracy);
 					else if (algName.equalsIgnoreCase("Nelder Mead"))
 						alg = new NelderMead(accuracy);
 					else if (algName.equalsIgnoreCase("Apache Nelder Mead"))
 						alg = new ApacheNelderMead();
 					else
 						throw new IllegalArgumentException("Did not recognise the fitting routine");
 
 				} else if (property.equals(PreferenceConstants.FITTING_1D_AUTO_STOPPING)
 						|| property.equals(PreferenceConstants.FITTING_1D_THRESHOLD)
 						|| property.equals(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE)
 						|| property.equals(PreferenceConstants.FITTING_1D_PEAK_NUM)) {
 
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_AUTO_STOPPING)) {
 						autoStopping = preferenceStore.getDefaultBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING);
 					} else {
 						autoStopping = preferenceStore.getBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING);
 					}
 
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_THRESHOLD)) {
 						threshold = preferenceStore.getDefaultInt(PreferenceConstants.FITTING_1D_THRESHOLD);
 					} else {
 						threshold = preferenceStore.getInt(PreferenceConstants.FITTING_1D_THRESHOLD);
 					}
 
 					String measure;
 					if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE)) {
 						measure = preferenceStore.getDefaultString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE);
 					} else {
 						measure = preferenceStore.getString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE);
 					}
 					determineThresholdMeasure(measure);
 
 					if (autoStopping) {
 						numPeaks.setSelection(-1);
 						numPeaks.setEnabled(false);
 					} else {
 						numPeaks.setEnabled(true);
 						if (preferenceStore.isDefault(PreferenceConstants.FITTING_1D_PEAK_NUM)) {
 							numPeaks.setSelection(preferenceStore
 									.getDefaultInt(PreferenceConstants.FITTING_1D_PEAK_NUM));
 						} else {
 							numPeaks.setSelection(preferenceStore.getInt(PreferenceConstants.FITTING_1D_PEAK_NUM));
 						}
 					}
 				} else if (property.equals(PreferenceConstants.FITTING_1D_DECIMAL_PLACES)) {
 					fittedPeakTable.refresh();
 				}
 			}
 
 		});
 	}
 
 	protected void determineThresholdMeasure(String measure) {
 		if (measure.equalsIgnoreCase("Height")) {
 			thresholdMeasure = true;
 		} else if (measure.equalsIgnoreCase("Area")) {
 			thresholdMeasure = false;
 		} else {
 			throw new IllegalArgumentException("Did not recognise the threashold measure");
 		}
 	}
 
 	private SelectionAdapter fitPeakListener = new SelectionAdapter() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 
 			clearPeakTable();
 			removeAllPeakOverlays();
 			selectPeakFuction(peakType.getSelectionIndex());
 
 			IJobManager manager = Job.getJobManager();
 			// Cancel all outstanding jobs.
 			manager.cancel(BUTTON_FITTING_UUID);
 			manager.cancel(REFITTING_UUID);
 
 			new FitWholeDataset(currentXAxis.toDataset(), currentDataSet, peakToFit, alg, smoothing,
 					numPeaks.getSelection(), threshold / 100, autoStopping, thresholdMeasure,
 					chooseDataCombo.getItem(chooseDataCombo.getSelectionIndex()), BUTTON_FITTING_UUID).schedule();
 		}
 	};
 
 	private class FitWholeDataset extends Job {
 
 		final DoubleDataset xAxis;
 		final DoubleDataset yAxis;
 		final APeak peak;
 		final IOptimizer optomiser;
 		final int smooth;
 		final int numberOfPeaks;
 		final double cutoff;
 		final boolean autoStop;
 		final boolean measure;
 		private static final String name = "Fitting peaks";
 		private String UUID;
 
 		public FitWholeDataset(DoubleDataset xaxis, DoubleDataset yaxis, APeak peakToFit, IOptimizer alg,
 				int smoothing, int numPeaks, double cutOff, boolean autostop, boolean stoppingMeasure, String dataName,
 				String UUID) {
 			super(name + " " + dataName);
 			xAxis = xaxis;
 			yAxis = yaxis;
 			peak = peakToFit;
 			optomiser = alg;
 			smooth = smoothing;
 			numberOfPeaks = numPeaks;
 			cutoff = cutOff;
 			autoStop = autostop;
 			measure = stoppingMeasure;
 			this.UUID = UUID;
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 			final List<APeak> fittedPeaks = Generic1DFitter.fitPeaks(xAxis, yAxis, peak, optomiser, smooth,
 					numberOfPeaks, cutoff, autoStop, measure, new IAnalysisMonitor() {
 
 						@Override
 						public boolean hasBeenCancelled() {
 							return monitor.isCanceled();
 						}
 					});
 
 			if (monitor.isCanceled())
 				return Status.CANCEL_STATUS;
 			if (fittedPeaks.isEmpty()) {
 				logger.warn("No peaks found");
 				return Status.OK_STATUS;
 			}
 
 			UIJob updateGUI = new UIJob("Updating GUI") {
 
 				@Override
 				public IStatus runInUIThread(IProgressMonitor monitor) {
 					addPeaksToList(fittedPeaks, colorButtonFitted);
 					return Status.OK_STATUS;
 				}
 			};
 			updateGUI.schedule();
 
 			return Status.OK_STATUS;
 		}
 
 		@Override
 		public boolean belongsTo(Object family) {
 			return UUID.equals(family);
 		}
 	}
 
 	private SelectionAdapter showAllPeaksOverlay = new SelectionAdapter() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			if (showAllPeaks.getSelection()) {
 				drawAllPeakOverlays();
 			}
 			if (!showAllPeaks.getSelection()) {
 				removeAllPeakOverlays();
 			}
 		}
 	};
 
 	private void drawAllPeakOverlays() {
 		removeAllPeakOverlays();
 		if (!showAllPeaks.getSelection()) {
 			return;
 		}
 		if (fittedPeakList == null || fittedPeakList.isEmpty()) {
 			return;
 		}
 
 		overlaysVisible(false);
 		if (showAllPeaks.getSelection()) {
 			for (FittedPeakData fpd : fittedPeakList) {
 				int primID = oProvider.registerPrimitive(PrimitiveType.BOX);
 				oProvider.begin(OverlayType.VECTOR2D);
 				double start = fpd.getFittedPeak().getPosition();
 				double fwhm = fpd.getFittedPeak().getFWHM();
 				oProvider.drawBox(primID, start - (fwhm / 2), MAXYVALUE, start + (fwhm / 2), -MAXYVALUE);
 				oProvider.setColour(primID, Color.ORANGE);
 				oProvider.setStyle(primID, VectorOverlayStyles.FILLED_WITH_OUTLINE);
 				oProvider.setTransparency(primID, 0.8);
 				oProvider.setOutlineTransparency(primID, 0);
 				oProvider.end(OverlayType.VECTOR2D);
 				primitiveIDs.add(primID);
 			}
 		}
 	}
 
 	private void removeAllPeakOverlays() {
 		oProvider.begin(OverlayType.VECTOR2D);
 		if (draggingPrimID != -1) {
 			oProvider.setPrimitiveVisible(draggingPrimID, false);
 		}
 		if (primitiveIDs != null && !primitiveIDs.isEmpty()) {
 			oProvider.unregisterPrimitive(primitiveIDs);
 			primitiveIDs.clear();
 		}
 		oProvider.end(OverlayType.VECTOR2D);
 	}
 
 	private void peaksUpdated() {
 		updatePeaksTable();
 		pushPeaksToPlotter();
 		drawAllPeakOverlays();
 	}
 
 	private void pushPeaksToPlotter() {
 		ArrayList<APeak> peaks = new ArrayList<APeak>();
 		for (FittedPeakData p : fittedPeakList) {
 			peaks.add(p.getFittedPeak());
 		}
 		if (guiUpdateManager != null) {
 			guiUpdateManager.putGUIInfo(GuiParameters.FITTEDPEAKS, peaks);
 		}
 	}
 
 	private void updatePeaksTable() {
 		Collections.sort(fittedPeakList, new Compare());
 		fittedPeakTable.refresh();
 	}
 
 	private static class Compare implements Comparator<FittedPeakData> {
 		@Override
 		public int compare(FittedPeakData arg0, FittedPeakData arg1) {
 			if (arg0.getFittedPeak().getPosition() > arg1.getFittedPeak().getPosition()) {
 				return 1;
 			}
 			if (arg0.getFittedPeak().getPosition() < arg1.getFittedPeak().getPosition()) {
 				return -1;
 			}
 			return 0;
 		}
 	}
 
 	private SelectionAdapter clearPeaksListener = new SelectionAdapter() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			fittedPeakList.clear();
 			peaksUpdated();
 		}
 	};
 
 	@Override
 	public void selectionChanged(SelectionChangedEvent event) {
 		ISelection selection = event.getSelection();
 		IStructuredSelection selection2 = (IStructuredSelection) selection;
 		FittedPeakData selectedPeak = (FittedPeakData) selection2.getFirstElement();
 		if (selectedPeak != null) {
 			processPeakInformation(selectedPeak);
 			drawFittedPeakOverlays(selectedPeak);
 			fittedPlot.removePrimitives();
 			fittedPlot.drawCurrentOverlay(selectedPeak);
 		}
 	}
 
 	private void drawFittedPeakOverlays(FittedPeakData selectedPeak) {
 		int primID = oProvider.registerPrimitive(PrimitiveType.BOX);
 		overlaysVisible(false);
 		oProvider.begin(OverlayType.VECTOR2D);
 		double start = selectedPeak.getFittedPeak().getPosition();
 		double fwhm = selectedPeak.getFittedPeak().getFWHM();
 		oProvider.drawBox(primID, (start - (fwhm / 2)), MAXYVALUE, (start + (fwhm / 2)), -MAXYVALUE);
 		oProvider.setColour(primID, Color.ORANGE);
 		oProvider.setStyle(primID, VectorOverlayStyles.FILLED_WITH_OUTLINE);
 		oProvider.setTransparency(primID, 0.8);
 		oProvider.setOutlineTransparency(primID, 0);
 		oProvider.end(OverlayType.VECTOR2D);
 		primitiveIDs.add(primID);
 	}
 
 	/**
 	 * this method will extract the datasets and then send them for plotting
 	 * 
 	 * @param selectedPeak
 	 */
 	private void processPeakInformation(FittedPeakData selectedPeak) {
 		APeak peak = selectedPeak.getFittedPeak();
 
 		double min = peak.getPosition() - 2 * peak.getFWHM();
 		double max = peak.getPosition() + 2 * peak.getFWHM();
 
 		DoubleDataset measuredData = sliceDataSet(min, max);
 		double slicedDataMin = (Double) measuredData.min();
 		CompositeFunction function = new CompositeFunction();
 		Offset os = new Offset(slicedDataMin, slicedDataMin);
 		function.addFunction(peak);
 		function.addFunction(os);
 
 		AbstractDataset axis = createXData(min, max);
 		DoubleDataset fittedData = function.makeDataset(axis);
 
 		fittedPlot.plotDataSets(measuredData, fittedData, new AxisValues(axis));
 	}
 
 	@Override
 	public void showSidePlot() {
 		processPlotUpdate();
 	}
 
 	@Override
 	public int updateGUI(GuiBean bean) {
 		return 0;
 	}
 
 	@Override
 	public void generateMenuActions(IMenuManager manager, final IWorkbenchPartSite site) {
 		Action fitting1dmenu = new Action() {
 			@Override
 			public void run() {
 				IHandlerService handler = (IHandlerService) site.getService(IHandlerService.class);
 				try {
 					handler.executeCommand("uk.ac.diamond.scisoft.analysis.rcp.preferences.Fitting1DPreferencePage",
 							null);
 				} catch (Exception e) {
 					logger.error(e.getMessage());
 				}
 			}
 		};
 		fitting1dmenu.setText("Preferences");
 		manager.add(fitting1dmenu);
 	}
 
 	@Override
 	public void generateToolActions(IToolBarManager manager) {
 		// not going to be implemented
 	}
 }
 
 class PlotFittedPeaks implements Overlay1DConsumer {
 
 	transient private static final Logger logger = LoggerFactory.getLogger(PlotFittedPeaks.class);
 
 	private DataSetPlotter plotter;
 	private Overlay1DProvider sideProvider;
 	private List<Integer> sidePrimID;
 	private int sideID;
 
 	double MaxY = Double.MAX_VALUE, MinY = -Double.MAX_VALUE;
 
 	public PlotFittedPeaks(SashForm ss) {
 		Composite peakPlotter = new Composite(ss, SWT.NONE);
 		peakPlotter.setLayout(new FillLayout());
 		plotter = new DataSetPlotter(PlottingMode.ONED, peakPlotter);
 		plotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
 	}
 
 	public void plotDataSets(DoubleDataset measuredData, DoubleDataset fittedData, AxisValues xAxis) {
 		MaxY = fittedData.max().doubleValue();
 		MinY = fittedData.min().doubleValue();
 		List<IDataset> plottingData = new ArrayList<IDataset>();
 		List<AxisValues> axisValues = new ArrayList<AxisValues>();
 		plottingData.add(measuredData);
 		plottingData.add(fittedData);
 		axisValues.add(xAxis);
 		axisValues.add(xAxis);
 		try {
 			plotter.replaceAllPlots(plottingData, axisValues);
 		} catch (PlotException e) {
 			e.printStackTrace();
 		}
 		plotter.refresh(false);
 	}
 
 	public void clearPeakPlotter() {
 		if (plotter != null) {
 			try {
 				plotter.replaceAllPlots(new ArrayList<IDataset>());
 				removePrimitives();
 			} catch (PlotException e) {
 				logger.warn("The plot could not be cleared in fitting1D sideplot");
 			}
 			plotter.refresh(false);
 		}
 	}
 
 	public void dispose() {
 		if (plotter != null) {
 			plotter.cleanUp();
 		}
 	}
 
 	public void drawCurrentOverlay(FittedPeakData selectedPeak) {
 		double fwhm = selectedPeak.getFittedPeak().getFWHM();
 		double pos = selectedPeak.getFittedPeak().getPosition();
 		if (sideProvider == null) {
 			plotter.registerOverlay(this);
 		}
 		sideProvider.begin(OverlayType.VECTOR2D);
 		sideID = sideProvider.registerPrimitive(PrimitiveType.BOX);
 		sideProvider.setColour(sideID, Color.RED);
 		sideProvider.setStyle(sideID, VectorOverlayStyles.FILLED_WITH_OUTLINE);
 		sideProvider.setTransparency(sideID, 0.8);
 		sideProvider.setOutlineTransparency(sideID, 0);
 		sideProvider.drawBox(sideID, pos - (fwhm / 2), ((MaxY - MinY) / 2) + MinY, pos + (fwhm / 2), MinY);
 		sidePrimID.add(sideID);
 		// position line
 		sideID = sideProvider.registerPrimitive(PrimitiveType.LINE);
 		sideProvider.setColour(sideID, Color.GREEN);
 		sideProvider.drawLine(sideID, pos, MinY, pos, MaxY);
 		sidePrimID.add(sideID);
 		sideProvider.end(OverlayType.VECTOR2D);
 	}
 
 	@Override
 	public void registerProvider(OverlayProvider provider) {
 		sideProvider = (Overlay1DProvider) provider;
 		sidePrimID = new ArrayList<Integer>();
 	}
 
 	@Override
 	public void removePrimitives() {
 		if (sideProvider != null) {
 			// Call directly unregisterPrimitive(...) list much faster than
 			// call to sideProvider.unregisterPrimitive(int) when run
 			// in debugger. This then speeds selection of the table up.
 			sideProvider.unregisterPrimitive(sidePrimID);
 		}
 	}
 
 	@Override
 	public void unregisterProvider() {
 		if (sideProvider != null) {
 			sideProvider = null;
 		}
 	}
 
 	@Override
 	public void areaSelected(AreaSelectEvent event) {
 	}
 }
