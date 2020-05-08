 package org.dawnsci.rcp.histogram;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.PaletteEvent;
 import org.dawb.common.ui.plot.trace.PaletteListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawnsci.rcp.functions.ColourSchemeContribution;
 import org.dawnsci.rcp.functions.TransferFunctionContribution;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;
 
 
 public class HistogramToolPage extends AbstractToolPage {
 
 	// LOGGER
 	private static final Logger logger = LoggerFactory.getLogger(HistogramToolPage.class);
 
 
 	// STATICS
 	private static final int SLIDER_STEPS = 1000;
 
 
 	// MODES
 	private static final int FULL = 0;
 	private static final int AUTO = 1;
 	private static final int FIXED = 2;
 
 	private int mode = FULL;
 
 
 	// HISTOGRAM 
 	private double rangeMax = 100.0;
 	private double rangeMin = 0.0;
 	private double histoMax = 50.0;
 	private double histoMin = 25.0;
 
 	private AbstractDataset imageDataset;
 
 	private AbstractDataset histogramX;
 	private AbstractDataset histogramY;
 
 	private int num_bins = 2048;
 
 	private boolean histogramDirty = true;
 
 
 	// GUI
 	private Composite composite;
 	private ExpansionAdapter expansionAdapter;
 
 
 	// COLOUR SCHEME GUI 
 	private ExpandableComposite colourSchemeExpander;
 	private Composite colourSchemeComposite;
 	private CCombo cmbColourMap;	
 	private SelectionListener colourSchemeListener;
 
 
 	// PER CHANNEL SCHEME GUI
 	private ExpandableComposite perChannelExpander;
 	private Composite perChannelComposite;
 	private CCombo cmbAlpha;	
 	private CCombo cmbRedColour;
 	private CCombo cmbGreenColour;
 	private CCombo cmbBlueColour;
 	private Button btnGreenInverse;
 	private Button btnBlueInverse;
 	private Button btnAlphaInverse;
 	private Button btnRedInverse;
 
 	private SelectionListener colourSelectionListener;
 
 
 	// BRIGHTNESS CONTRAST GUI
 	private static final String BRIGHTNESS_LABEL = "Brightness";
 	private static final String CONTRAST_LABEL = "Contrast";
 	private ExpandableComposite bcExpander;
 	private Composite bcComposite;
 	private SpinnerSliderSet brightnessContrastValue;
 	private SelectionListener brightnessContrastListener;
 
 
 	// MIN MAX GUI	
 	private static final String MAX_LABEL = "Max";
 	private static final String MIN_LABEL = "Min";
 	private ExpandableComposite rangeExpander;
 	private Composite rangeComposite;
 	private SpinnerSliderSet minMaxValue;
 	private SelectionListener minMaxValueListener;
 
 
 	// HISTOGRAM PLOT
 	private ExpandableComposite histogramExpander;
 	private Composite histogramComposite;
 	private AbstractPlottingSystem histogramPlot;
 
 	private ITraceListener traceListener;
 
 	private IImageTrace image;
 
 	private ILineTrace histoTrace;
 	private ILineTrace redTrace;
 	private ILineTrace greenTrace;
 	private ILineTrace blueTrace;
 
 	// HELPERS
 	private ExtentionPointManager extentionPointManager;
 	private UIJob imagerepaintJob;
 	private PaletteData palleteData;
 	private int internalEvent = 0;
 
 
 	private PaletteListener paletteListener;
 
 
 	/**
 	 * Basic Constructor
 	 */
 	public HistogramToolPage() {
 		super();
 		try {
 			histogramPlot = PlottingFactory.getPlottingSystem();
 
 
 		} catch (Exception ne) {
 			logger.error("Cannot locate any plotting systems!", ne);
 		}
 
 
 		// Connect to the trace listener to deal with new images coming in
 		traceListener = new ITraceListener.Stub() {
 			@Override
 			public void tracesPlotted(TraceEvent evt) {
 
 				logger.trace("tracelistener firing");
 				
 				if (!(evt.getSource() instanceof List<?>)) {
 					return;
 				}
 
 				updateImage();
 			}
 		};
 		
 		
 		// get a palette update listener to deal with palatte updates
 		paletteListener = new PaletteListener(){
 
 			@Override
 			public void paletteChanged(PaletteEvent event) {
 				if (internalEvent > 0) return;
 				logger.trace("paletteChanged");
 				palleteData = event.getPaletteData();		
 				updateHistogramToolElements(null, false);
 			}
 
 			@Override
 			public void minChanged(PaletteEvent event) {
 				if (internalEvent > 0) return;
 				logger.trace("paletteListener minChanged firing");
				histoMin = image.getCalculatedMin().doubleValue();
 				updateHistogramToolElements(null, false);
 				
 			}
 
 			@Override
 			public void maxChanged(PaletteEvent event) {
 				if (internalEvent > 0) return;
 				logger.trace("paletteListener maxChanged firing");
				histoMax = image.getCalculatedMax().doubleValue();
 				updateHistogramToolElements(null, false);
 			}
 			
 		};
 
 
 
 		// Set up all the GUI element listeners
 		minMaxValueListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("minMaxValueListener");
 				histoMax = minMaxValue.getValue(MAX_LABEL);
 				histoMin = minMaxValue.getValue(MIN_LABEL);
 				if (histoMax < histoMin) {
 					histoMax = histoMin;
 				}
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		brightnessContrastListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("brightnessContrastListener");
 				histoMax = brightnessContrastValue.getValue(BRIGHTNESS_LABEL)+
 						brightnessContrastValue.getValue(CONTRAST_LABEL)/2.0;
 				histoMin = brightnessContrastValue.getValue(BRIGHTNESS_LABEL)-
 						brightnessContrastValue.getValue(CONTRAST_LABEL)/2.0;
 				if (histoMax < histoMin) {
 					histoMax = histoMin;
 				}
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		colourSelectionListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("colourSelectionListener");
 				buildPalleteData();
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		colourSchemeListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("colourSchemeListener");
 				updateColourScheme();
 				buildPalleteData();
 				updateHistogramToolElements(event);;
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		// Specify the expansion Adapter
 		expansionAdapter = new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				logger.trace("perChannelExpander");
 				composite.layout();
 			}
 		};
 
 
 		// Get all information from the extention points
 		extentionPointManager = new ExtentionPointManager();
 
 
 		// Set up the repaint job
 		imagerepaintJob = new UIJob("Colour Scale Image Update") {			
 
 			@Override
 			public IStatus runInUIThread(IProgressMonitor mon) {
 				logger.trace("imagerepaintJob running");
 				internalEvent++;
 				// update the colourscale
 				image.setMax(histoMax);
 				if (mon.isCanceled()) return Status.CANCEL_STATUS;
 
 				image.setMin(histoMin);
 				if (mon.isCanceled()) return Status.CANCEL_STATUS;
 
 				image.setPaletteData(palleteData);
 				if (mon.isCanceled()) return Status.CANCEL_STATUS;
 
 				internalEvent--;
 				return Status.OK_STATUS;
 			}
 		};
 
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void createControl(final Composite parent) {
 		// Set up the composite to hold all the information
 		composite = new Composite(parent, SWT.RESIZE);
 		composite.setLayout(new GridLayout(1, false));		
 
 		// Set up the Colour scheme part of the GUI
 		colourSchemeExpander = new ExpandableComposite(composite, SWT.NONE);
 		colourSchemeExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		colourSchemeExpander.setLayout(new GridLayout(1, false));
 		colourSchemeExpander.setText("Colour Scheme");
 
 		colourSchemeComposite = new Composite(colourSchemeExpander, SWT.NONE);
 		colourSchemeComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		colourSchemeComposite.setLayout(new GridLayout(1, false));
 
 		cmbColourMap = new CCombo(colourSchemeComposite, SWT.BORDER | SWT.READ_ONLY);
 		cmbColourMap.setToolTipText("Change the color scheme.");
 		cmbColourMap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		cmbColourMap.addSelectionListener(colourSchemeListener);
 
 		// Populate the control
 		for (ColourSchemeContribution contribution : extentionPointManager.getColourSchemeContributions()) {
 			cmbColourMap.add(contribution.getName());
 		}
 
 		cmbColourMap.select(0);
 
 		colourSchemeExpander.setClient(colourSchemeComposite);
 		colourSchemeExpander.addExpansionListener(expansionAdapter);
 
 
 		// Set up the per channel colour scheme part of the GUI		
 		perChannelExpander = new ExpandableComposite(composite, SWT.NONE);
 		perChannelExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		perChannelExpander.setLayout(new GridLayout(1, false));
 		perChannelExpander.setText("Colour Scheme per Channel");
 
 		perChannelComposite = new Composite(perChannelExpander, SWT.NONE);
 		perChannelComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		perChannelComposite.setLayout(new GridLayout(3, false));
 		{
 			Label lblRed = new Label(perChannelComposite, SWT.NONE);
 			lblRed.setText("Red");
 			cmbRedColour = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbRedColour.addSelectionListener(colourSelectionListener);
 			btnRedInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnRedInverse.setText("Inverse");
 			btnRedInverse.addSelectionListener(colourSelectionListener);
 
 			Label lblGreen = new Label(perChannelComposite, SWT.NONE);
 			lblGreen.setText("Green");
 			cmbGreenColour = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbGreenColour.addSelectionListener(colourSelectionListener);
 			btnGreenInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnGreenInverse.setText("Inverse");
 			btnGreenInverse.addSelectionListener(colourSelectionListener);
 
 			Label lblBlue = new Label(perChannelComposite, SWT.NONE);
 			lblBlue.setText("Blue");
 			cmbBlueColour = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbBlueColour.addSelectionListener(colourSelectionListener);
 			btnBlueInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnBlueInverse.setText("Inverse");
 			btnBlueInverse.addSelectionListener(colourSelectionListener);
 
 			Label lblAlpha = new Label(perChannelComposite, SWT.NONE);
 			lblAlpha.setText("Alpha");
 			cmbAlpha = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbAlpha.addSelectionListener(colourSelectionListener);
 			btnAlphaInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnAlphaInverse.setText("Inverse");
 			btnAlphaInverse.addSelectionListener(colourSelectionListener);
 		}		
 
 		// populate the control
 		for (TransferFunctionContribution contribution : extentionPointManager.getTransferFunctionContributions()) {
 			cmbRedColour.add(contribution.getName());
 			cmbGreenColour.add(contribution.getName());
 			cmbBlueColour.add(contribution.getName());
 			cmbAlpha.add(contribution.getName());
 		}
 
 		cmbRedColour.select(0);
 		cmbGreenColour.select(0);
 		cmbBlueColour.select(0);
 		cmbAlpha.select(0);
 
 		perChannelExpander.setClient(perChannelComposite);
 		perChannelExpander.addExpansionListener(expansionAdapter);
 
 
 		// Set up the Brightness and contrast part of the GUI
 		bcExpander = new ExpandableComposite(composite, SWT.NONE);
 		bcExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		bcExpander.setLayout(new GridLayout(1, false));
 		bcExpander.setText("Brightness and Contrast");
 
 		bcComposite = new Composite(bcExpander, SWT.NONE);
 		bcComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		bcComposite.setLayout(new GridLayout(1, false));
 
 		brightnessContrastValue = new SpinnerSliderSet(bcComposite, SLIDER_STEPS, BRIGHTNESS_LABEL, CONTRAST_LABEL);
 		brightnessContrastValue.addSelectionListener(brightnessContrastListener);
 
 		bcExpander.setClient(bcComposite);
 		bcExpander.addExpansionListener(expansionAdapter);
 
 
 		// Set up the Min Max range part of the GUI
 		rangeExpander = new ExpandableComposite(composite, SWT.NONE);
 		rangeExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		rangeExpander.setLayout(new GridLayout(1, false));
 		rangeExpander.setText("Histogram Range");
 
 		rangeComposite = new Composite(rangeExpander, SWT.NONE);
 		rangeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		rangeComposite.setLayout(new GridLayout(1, false));
 
 		minMaxValue = new SpinnerSliderSet(rangeComposite, SLIDER_STEPS, MAX_LABEL, MIN_LABEL);
 		minMaxValue.addSelectionListener(minMaxValueListener);
 
 		rangeExpander.setClient(rangeComposite);
 		rangeExpander.addExpansionListener(expansionAdapter);
 
 
 
 		// Set up the histogram plot part of the GUI
 		histogramExpander = new ExpandableComposite(composite, SWT.NONE);
 		histogramExpander.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		histogramExpander.setLayout(new GridLayout(1, false));
 		histogramExpander.setText("Histogram Plot");
 
 		histogramComposite = new Composite(histogramExpander, SWT.NONE);
 		histogramComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		histogramComposite.setLayout(new GridLayout(1, false));
 
 		final IPageSite site = getSite();
 
 		histogramPlot.createPlotPart( histogramComposite, 
 				getTitle(), 
 				site.getActionBars(), 
 				PlotType.PT1D,
 				null);
 		histogramPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		histogramExpander.setClient(histogramComposite);
 		histogramExpander.addExpansionListener(expansionAdapter);
 
 		// Activate this so the initial screen has content
 		activate();		
 	}
 
 	/**
 	 * Use the controls from the GUI to set the individual colour elements from the selected colour scheme
 	 */
 	protected void updateColourScheme() {
 		ColourSchemeContribution colourScheme = extentionPointManager.getColourSchemeContribution(cmbColourMap.getText());
 		String red = extentionPointManager.getTransferFunctionFromID(colourScheme.getRedID()).getName();
 		String green = extentionPointManager.getTransferFunctionFromID(colourScheme.getGreenID()).getName();
 		String blue = extentionPointManager.getTransferFunctionFromID(colourScheme.getBlueID()).getName();
 		String alpha = extentionPointManager.getTransferFunctionFromID(colourScheme.getAlphaID()).getName();
 
 		setComboByName(cmbRedColour, red);
 		setComboByName(cmbGreenColour, green);
 		setComboByName(cmbBlueColour, blue);
 		setComboByName(cmbAlpha, alpha);
 		
 		btnRedInverse.setSelection(colourScheme.getRedInverted());
 		btnGreenInverse.setSelection(colourScheme.getGreenInverted());
 		btnBlueInverse.setSelection(colourScheme.getBlueInverted());
 		btnAlphaInverse.setSelection(colourScheme.getAlphaInverted());
 
 	}
 
 	/**
 	 * Sets the selected item in a combo based on the name given
 	 * @param combo The combo box to set selected
 	 * @param name The name of the item to select
 	 */
 	private void setComboByName(CCombo combo, String name) {
 		for (int i = 0; i < combo.getItems().length; i++) {
 			if(combo.getItem(i).compareTo(name)== 0) {
 				combo.select(i);
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Update when a new image comes in, this involves getting the data and then setting 
 	 * up all the local parameters
 	 */
 	private void updateImage() {
 		if (getControl()==null) return; // We cannot plot unless its been created.
 
 		Collection<ITrace> traces = getPlottingSystem().getTraces(IImageTrace.class);
 		image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next():null;
 
 		if (image != null) {
 
 			// get the image data
 			imageDataset = image.getData();
 
 			switch (mode) {
 			case AUTO:
 				// TODO implement AUTO functionality
 				break;
 			case FIXED:
 				// TODO implement FIXED functionality 
 				break;
 			default:
 				// this is the FULL implementation (a good default)
 				rangeMax = imageDataset.max().doubleValue();
 				rangeMin = imageDataset.min().doubleValue();
 				histoMax = image.getMax().doubleValue();
 				histoMin = image.getMin().doubleValue();
 				break;
 			}
 			
 			// Update the paletteData
 			palleteData = image.getPaletteData();
 
 			// calculate the histogram
 			generateHistogram(imageDataset);
 
 			// update all based on slider positions
 			updateHistogramToolElements(null);
 			
 			// fianlly tie in the listener to the paletedata changes
 			image.addPaletteListener(paletteListener);
 		}				
 	}
 	
 	private void removeImagePalleteListener() {
 		if (getControl()==null) return; // We cannot plot unless its been created.
 
 		Collection<ITrace> traces = getPlottingSystem().getTraces(IImageTrace.class);
 		image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next():null;
 
 		if (image != null) {
 
 			image.removePaletteListener(paletteListener);
 		}				
 	}
 
 	
 	private void updateHistogramToolElements(SelectionEvent event) {
 		updateHistogramToolElements(event, true);
 	}
 	
 	/**
 	 * Update everything based on the new slider positions  
 	 * @param event 
 	 */
 	private void updateHistogramToolElements(SelectionEvent event, boolean repaintImage) {
 		// update the ranges
 		updateRanges(event);
 
 		// plot the histogram
 		plotHistogram();
 
 		// repaint the image if required
 		if(repaintImage) imagerepaintJob.schedule();
 	}
 
 
 	/**
 	 * This will take an image, and pull out all the parameters required to calculate the histogram
 	 * @param image the image to histogram
 	 */
 	private void generateHistogram(AbstractDataset image) {
 		// calculate the histogram for the whole image
 		Histogram hist = new Histogram(num_bins, rangeMin, rangeMax, true);
 		List<AbstractDataset> histogram_values = hist.value(image);
 		histogramX = histogram_values.get(1).getSlice(
 				new int[] {0},
 				new int[] {num_bins},
 				new int[] {1});
 		histogramX.setName("Intiesity");
 		histogramY = histogram_values.get(0);
 		histogramY = Maths.log10((Maths.add(histogramY, 1.0)));
 		histogramY.setName("Histogram");
 
 		histogramDirty = true;
 	}
 
 
 	/**
 	 * Update all the gui element ranges based on the internal values for them
 	 * @param event 
 	 */
 	private void updateRanges(SelectionEvent event) {
 
 		// set the minmax values
 		minMaxValue.setMin(MIN_LABEL, rangeMin);
 		minMaxValue.setMax(MIN_LABEL, rangeMax);
 		minMaxValue.setValue(MIN_LABEL, histoMin);
 
 		minMaxValue.setMin(MAX_LABEL, rangeMin);
 		minMaxValue.setMax(MAX_LABEL, rangeMax);
 		minMaxValue.setValue(MAX_LABEL, histoMax);
 
 		// Set the brightness
 		brightnessContrastValue.setMin(BRIGHTNESS_LABEL, rangeMin);
 		brightnessContrastValue.setMax(BRIGHTNESS_LABEL, rangeMax);
 		brightnessContrastValue.setValue(BRIGHTNESS_LABEL, (histoMax+histoMin)/2.0);
 
 		// Set the contrast
 		brightnessContrastValue.setMin(CONTRAST_LABEL, rangeMin);
 		brightnessContrastValue.setMax(CONTRAST_LABEL, rangeMax);
 		brightnessContrastValue.setValue(CONTRAST_LABEL, histoMax-histoMin);
 	}
 
 
 	/**
 	 * Plots the histogram, and RGB lines
 	 */
 	private void plotHistogram() {	
 
 
 		// Initialise the histogram Plot if required
 
 		if (histoTrace == null) {
 
 			histogramPlot.clear();
 
 			// Set up the histogram trace
 			histoTrace = histogramPlot.createLineTrace("Histogram");
 			histoTrace.setTraceType(TraceType.AREA);
 			histoTrace.setLineWidth(1);
 			histoTrace.setTraceColor(new Color(null, 0, 0, 0));
 
 			// Set up the RGB traces
 			redTrace = histogramPlot.createLineTrace("Red");
 			greenTrace = histogramPlot.createLineTrace("Green");
 			blueTrace = histogramPlot.createLineTrace("Blue");
 
 			redTrace.setLineWidth(2);
 			greenTrace.setLineWidth(2);
 			blueTrace.setLineWidth(2);
 
 			redTrace.setTraceColor(new Color(null, 255, 0, 0));
 			greenTrace.setTraceColor(new Color(null, 0, 255, 0));
 			blueTrace.setTraceColor(new Color(null, 0, 0, 255));
 
 			// Finally add everything in a threadsafe way.
 			getControl().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					histogramPlot.addTrace(histoTrace);
 					histogramPlot.addTrace(redTrace);
 					histogramPlot.addTrace(greenTrace);
 					histogramPlot.addTrace(blueTrace);
 				};
 			});
 		}
 
 		// now build the RGB Lines
 		PaletteData paletteData = image.getPaletteData();
 		final DoubleDataset R = new DoubleDataset(paletteData.colors.length);
 		final DoubleDataset G = new DoubleDataset(paletteData.colors.length);
 		final DoubleDataset B = new DoubleDataset(paletteData.colors.length);
 		final DoubleDataset RGBX = new DoubleDataset(paletteData.colors.length);
 		R.setName("red");
 		G.setName("green");
 		B.setName("blue");
 		RGBX.setName("Axis");
 		double scale = ((histogramY.max().doubleValue())/256.0);
 		if(scale <= 0) scale = 1.0/256.0;
 		
 		//palleteData.colors = new RGB[256];
 		for (int i = 0; i < paletteData.colors.length; i++) {
 			R.set(paletteData.colors[i].red*scale, i);
 			G.set(paletteData.colors[i].green*scale, i);
 			B.set(paletteData.colors[i].blue*scale, i);
 			RGBX.set(histoMin+(i*((histoMax-histoMin)/paletteData.colors.length)), i);
 		}
 
 		// Now update all the trace data in a threadsafe way
 		final double finalScale = scale;
 
 		getControl().getDisplay().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				if (histogramDirty) {
 					histoTrace.setData(histogramX, histogramY);
 					histogramDirty = false;
 				}
 				redTrace.setData(RGBX, R);
 				greenTrace.setData(RGBX, G);
 				blueTrace.setData(RGBX, B);
 				histogramPlot.getSelectedXAxis().setRange(histoMin, histoMax);
 				histogramPlot.getSelectedYAxis().setRange(0, finalScale*256);
 			}
 		});
 	}
 
 
 	/**
 	 * Add the trace listener and plot intial data
 	 */
 	public void activate() {
 		super.activate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 			updateImage(); 
 		}
 	}
 
 	/**
 	 * remove the trace listener to avoid unneeded event triggering
 	 */
 	public void deactivate() {
 		super.deactivate();
 		
 		if (getPlottingSystem()!=null) {
 			removeImagePalleteListener();
 			getPlottingSystem().removeTraceListener(traceListener);
 		}
 	}
 
 
 	@Override
 	public Control getControl() {
 		return composite;
 	}
 
 	@Override
 	public void setFocus() {
 		if (composite!=null && !composite.isDisposed()) composite.setFocus();
 	}
 
 	/**
 	 * Build a pallete data from the RGB values which have been set in the GUI
 	 */
 	private void buildPalleteData() {
 
 		// first get the appropriate bits from the extension points
 		int[] red = extentionPointManager.getTransferFunction(cmbRedColour.getText()).getFunction().getArray();
 		int[] green = extentionPointManager.getTransferFunction(cmbGreenColour.getText()).getFunction().getArray();
 		int[] blue = extentionPointManager.getTransferFunction(cmbBlueColour.getText()).getFunction().getArray();
 
 		if (btnRedInverse.getSelection()) {
 			red = invert(red);
 		}
 		if (btnGreenInverse.getSelection()) {
 			green = invert(green);
 		}
 		if (btnBlueInverse.getSelection()) {
 			blue = invert(blue);
 		}
 		
 		palleteData.colors = new RGB[256];
 
 		for (int i = 0; i < 256; i++) {
 			palleteData.colors[i] = new RGB(red[i], green[i], blue[i]);
 		}
 	}
 	
 	private int[] invert(int[] array) {
 		int[] result = new int[array.length];
 		for(int i = 0; i < array.length; i++) {
 			result[i] = array[array.length-1-i];
 		}
 		return result;
 	}
 }
