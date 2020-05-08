 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.dawnsci.plotting.tools.window;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.util.DisplayUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.plotting.roi.SurfacePlotROI;
 import org.dawnsci.plotting.tools.Activator;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.PlottingFactory;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.region.RegionEvent;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.ILineStackTrace;
 import org.dawnsci.plotting.api.trace.IPaletteListener;
 import org.dawnsci.plotting.api.trace.IPaletteTrace;
 import org.dawnsci.plotting.api.trace.ISurfaceTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.IWindowTrace;
 import org.dawnsci.plotting.api.trace.PaletteEvent;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.mihalis.opal.rangeSlider.RangeSlider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.gda.richbeans.components.scalebox.IntegerBox;
 import uk.ac.gda.richbeans.components.scalebox.NumberBox;
 import uk.ac.gda.richbeans.event.ValueAdapter;
 import uk.ac.gda.richbeans.event.ValueEvent;
 
 /**
  * A tool which has one box region for configuring the region
  * which defines the window of a 3D plot.
  * 
  * TODO Add aspect ratio controls like the old windowing tool used to have.
  * 
  * @author fcp94556
  *
  */
 public class WindowTool extends AbstractToolPage {
 
 	private static final Logger logger = LoggerFactory.getLogger(WindowTool.class);
 	
 	private IPlottingSystem        windowSystem;
 	private IRegionListener        regionListener;
 	private IROIListener           roiListener;
 	private ITraceListener         traceListener;
 	private IPaletteListener       paletteListener;
 	private SelectionListener      selectionListener;
 	private WindowJob              windowJob;
 	private Composite              sliceControl, windowControl, blankComposite;
 	private Composite              content;
 
 	// FIXME Too many unecessary member variables = bad design.
 	// Consider using anonymous classes with final variables when 
 	// these are created, then there is no need to have them all as members.
 	private Spinner spnStartX;
 	private Spinner spnStartY;
 	private Spinner spnWidth;
 	private Spinner spnHeight;
 	private Button btnOverwriteAspect;
 	private Spinner spnXAspect;
 	private Spinner spnYAspect;
 
 	public WindowTool() {
 		try {
 			this.windowSystem  = PlottingFactory.createPlottingSystem();
 			this.windowJob     = new WindowJob();
 			
 			this.traceListener = new ITraceListener.Stub() {
 				protected void update(TraceEvent evt) {
 					ITrace trace = getTrace();
 					if (trace!=null) {
 						updateTrace(trace);
 					} else {
 						windowSystem.clear();
 					}
 				}
 			};
 			
 			this.paletteListener = new IPaletteListener.Stub() {
 				@Override
 				public void paletteChanged(PaletteEvent evt) {
 					try {
 					     ITrace trace = windowSystem.getTraces().iterator().next();
 					     if (trace instanceof IPaletteTrace) {
 					    	 ((IPaletteTrace)trace).setPaletteData(evt.getPaletteData()); 
 					     }
 					} catch (Exception ne) {
 						logger.error("Cannot set new palette.", ne);
 					}
 				}
 			};
 			
 			this.roiListener = new IROIListener.Stub() {
 				public void update(ROIEvent evt) {
 					IROI roi = evt.getROI();
 					if(roi!=null){
 						final int startX = (int)Math.round(roi.getPointX());
 						final int startY = (int)Math.round(roi.getPointY());
 						if (roi instanceof RectangularROI){
 							RectangularROI rroi = (RectangularROI) roi;
 							int roiWidth = (int)Math.round(rroi.getLengths()[0]);
 							int roiHeight = (int)Math.round(rroi.getLengths()[1]);
 							int endX = (int)Math.round(rroi.getEndPoint()[0]);
 							int endY = (int)Math.round(rroi.getEndPoint()[1]);
 							setSpinnerValues(startX, startY, roiWidth, roiHeight);
 							if(btnOverwriteAspect.getSelection()){
 								int xSize = getTrace().getData().getShape()[1];
 								int ySize = getTrace().getData().getShape()[0];
 								int xSamplingRate = Math.max(1, xSize / MAXDISPLAYDIM);
 								int ySamplingRate = Math.max(1, ySize / MAXDISPLAYDIM);
 								SurfacePlotROI sroi = new SurfacePlotROI(startX * xSamplingRate, 
 																startY * ySamplingRate, 
 																endX * xSamplingRate, 
 																endY * ySamplingRate, 
 																0, 0, 
 																spnXAspect.getSelection(), 
 																spnYAspect.getSelection());
 								windowJob.schedule(sroi);
 							} else {
 								windowJob.schedule(rroi);
 							}
 						}
 					}
 				}
 			};
 			
 			this.regionListener = new IRegionListener.Stub() {
 				@Override
 				public void regionAdded(RegionEvent evt) {
 					evt.getRegion().addROIListener(roiListener);
 				}
 
 				@Override
 				public void regionRemoved(RegionEvent evt) {
 					evt.getRegion().removeROIListener(roiListener);
 				}			
 			};
 
 			this.selectionListener = new SelectionListener() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					if (!e.getSource().equals(btnOverwriteAspect)) {
 						int startPosX = spnStartX.getSelection();
 						int startPosY = spnStartY.getSelection();
 						int width = spnWidth.getSelection();
 						int height = spnHeight.getSelection();
 						if (startPosX + width > spnWidth.getMaximum()) {
 							width = spnWidth.getMaximum() - startPosX;
 						}
 						if (startPosY + height > spnHeight.getMaximum()) {
 							height = spnHeight.getMaximum() - startPosY;
 						}
 						int endPtX = width + startPosX;
 						int endPtY = height + startPosY;
 						IRegion region = windowSystem.getRegion("Window");
 						RectangularROI rroi = new RectangularROI(startPosX, startPosY, width, height, 0);
 						if (region != null)
 							region.setROI(rroi);
 						if(btnOverwriteAspect.getSelection()){
 							int xSize = getTrace().getData().getShape()[1];
 							int ySize = getTrace().getData().getShape()[0];
 							int xSamplingRate = Math.max(1, xSize / MAXDISPLAYDIM);
 							int ySamplingRate = Math.max(1, ySize / MAXDISPLAYDIM);
 							SurfacePlotROI sroi = new SurfacePlotROI(startPosX * xSamplingRate, 
 													startPosY * ySamplingRate, 
 													endPtX * xSamplingRate, 
 													endPtY * ySamplingRate, 
 													0, 0, 
 													spnXAspect.getSelection(), 
 													spnYAspect.getSelection());
 							windowJob.schedule(sroi);
 						} else {
 							windowJob.schedule(rroi);
 						}
 					} else if (e.getSource().equals(btnOverwriteAspect)) {
 						spnXAspect.setEnabled(btnOverwriteAspect.getSelection());
 						spnYAspect.setEnabled(btnOverwriteAspect.getSelection());
 						if (btnOverwriteAspect.getSelection())
 							windowJob.schedule();
 						else
 							windowJob.schedule();
 					}
 				}
 				
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 				}
 			};
 
 		} catch (Exception e) {
 			logger.error("Cannot create a plotting system, something bad happened!", e);
 		}
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		
 		this.content = new Composite(parent, SWT.NONE);
 		final StackLayout stackLayout = new StackLayout();
 		content.setLayout(stackLayout);
 
 		this.windowControl = createWindowRegionControl();
         this.sliceControl = createSliceControl();
    
         final ITrace trace = getTrace();
 		if (trace instanceof ISurfaceTrace) {
 			stackLayout.topControl = windowControl;
 		} else if (trace instanceof ILineStackTrace) {
 		    stackLayout.topControl = sliceControl;
 		}
 		
 		this.blankComposite = new Composite(content, SWT.BORDER);
         
 	}
 	
 	private CLabel      errorLabel;
 	private RangeSlider sliceSlider;
 	private NumberBox   lowerControl, upperControl;
 	private int         lastLower = -1, lastUpper = -1;
 
 	private Composite createSliceControl() {
 		Composite sliceControl = new Composite(content, SWT.NONE);
 		sliceControl.setLayout(new GridLayout(3, false));
 		
 		final Label info = new Label(sliceControl, SWT.WRAP);
 		info.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 2));
 		info.setText("Please edit the window of the data, not more than 100 symultaneous plots are allowed in 3D.");
 	
 		sliceSlider = new RangeSlider(sliceControl, SWT.HORIZONTAL);
 		sliceSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2));
 		sliceSlider.setMinimum(0);
 		sliceSlider.setMaximum(25);
 		sliceSlider.setLowerValue(0);
 		sliceSlider.setUpperValue(25);
 		sliceSlider.setIncrement(1);
 		
 		GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1);
 		gridData.widthHint=130;
 		
         lowerControl = new IntegerBox(sliceControl, SWT.NONE);
         lowerControl.setLabel(" Lower    ");
         lowerControl.setLayoutData(gridData);
         lowerControl.setIntegerValue(sliceSlider.getLowerValue());
         lowerControl.setActive(true);
         lowerControl.on();
         lowerControl.addValueListener(new ValueAdapter() {			
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				sliceSlider.setLowerValue(lowerControl.getIntegerValue());
 				final int lower = sliceSlider.getLowerValue();
 				final int upper = sliceSlider.getUpperValue();
 				if (lower<0 || upper<0)                   return;
 				updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
 				lastLower = lower;
 				lastUpper = upper;
 			}
 		});
  
         upperControl = new IntegerBox(sliceControl, SWT.NONE);
         upperControl.setLabel(" Upper    ");
         upperControl.setLayoutData(gridData);
         upperControl.setIntegerValue(sliceSlider.getUpperValue());
         upperControl.setActive(true);
         upperControl.on();
         upperControl.addValueListener(new ValueAdapter() {			
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				sliceSlider.setUpperValue(upperControl.getIntegerValue());
 				final int lower = sliceSlider.getLowerValue();
 				final int upper = sliceSlider.getUpperValue();
 				if (lower<0 || upper<0)                   return;
 				updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
 				lastLower = lower;
 				lastUpper = upper;
 			}
 		});
 
         upperControl.setMinimum(lowerControl);
         upperControl.setMaximum(25);
         lowerControl.setMinimum(0);
         lowerControl.setMaximum(upperControl);
         
 		errorLabel = new CLabel(sliceControl, SWT.WRAP);
 		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 		errorLabel.setText("The slice range is too large.");
 		errorLabel.setImage(Activator.getImage("icons/error.png"));
         
 		sliceSlider.addMouseMoveListener(new MouseMoveListener() {
 			@Override
 			public void mouseMove(MouseEvent e) {
 				if ((e.button & SWT.BUTTON1)==0) {
 					final int lower = sliceSlider.getLowerValue();
 					final int upper = sliceSlider.getUpperValue();
 					if (lower<0 || upper<0)                   return;
 					if (lower==lastLower && upper==lastUpper) return;
 					
 					sliceSlider.setToolTipText("("+sliceSlider.getMinimum()+")  "+lower+" <-> "+upper+"  ("+sliceSlider.getMaximum()+")");
 					upperControl.setIntegerValue(upper);
 					lowerControl.setIntegerValue(lower);
 					updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
 					
 					lastLower = lower;
 					lastUpper = upper;
 				}
 			}
 		});
 
 		return sliceControl;
 	}
 
 	private Composite createWindowRegionControl(){
 		Composite windowComposite = new Composite(content, SWT.NONE);
 		windowComposite.setLayout(new FillLayout(SWT.VERTICAL));
 
 		windowSystem.createPlotPart(windowComposite, getTitle(), getSite().getActionBars(), PlotType.IMAGE, this.getViewPart());
 		final ISurfaceTrace surface = getSurfaceTrace();
 		//create Region
 		try {
 			final IRegion region = windowSystem.createRegion("Window", RegionType.BOX);
 			region.setROI(surface!=null && surface.getWindow() != null ? surface.getWindow() : new SurfacePlotROI(0,0,300,300, 0 ,0, 0, 0));
 			windowSystem.addRegion(region);
 		} catch (Exception e) {
 			logger.debug("Cannot create region for surface!", e);
 		}
 		int xStartPt = (int) (surface != null && surface.getWindow() != null ? surface.getWindow().getPoint()[0] : 0);
 		int yStartPt = (int) (surface!=null && surface.getWindow() != null ? surface.getWindow().getPoint()[1] : 0);
 		int width = 300;
 		int height = 300;
 		if(surface!=null && surface.getWindow() instanceof SurfacePlotROI){
 			width = surface!=null ? ((SurfacePlotROI)surface.getWindow()).getEndX() : width;
 			height = surface!=null ? ((SurfacePlotROI)surface.getWindow()).getEndY() : height;
 		}
 		ITrace trace = getTrace();
 		int xSize = 0, ySize = 0;
 		if (trace != null) {
 			if (trace instanceof ISurfaceTrace) {
 				xSize = getTrace().getData().getShape()[1];
 				ySize = getTrace().getData().getShape()[0];
 			}
 		} else {
 			xSize = 1000;
 			ySize = 1000;
 		}
 
 		Composite bottomComposite = new Composite(windowComposite,SWT.NONE | SWT.BORDER);
 		bottomComposite.setLayout(new GridLayout(1, false));
 		bottomComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite spinnersComp = new Composite(bottomComposite, SWT.NONE);
 		spinnersComp.setLayout(new GridLayout(4, false));
 		spinnersComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		Label lblStartX = new Label(spinnersComp, SWT.RIGHT);
 		lblStartX.setText("Start X:");
 		
 		spnStartX = new Spinner(spinnersComp, SWT.BORDER);
 		spnStartX.setMinimum(0);
 		spnStartX.setMaximum(xSize);
 		spnStartX.setSize(62, 18);
 		spnStartX.addSelectionListener(selectionListener);
 
 		Label lblStartY = new Label(spinnersComp, SWT.RIGHT);
 		lblStartY.setText("Start Y:");
 
 		spnStartY = new Spinner(spinnersComp, SWT.BORDER);
 		spnStartY.setMinimum(0);
 		spnStartY.setMaximum(ySize);
 		spnStartY.setSize(62, 18);
 		spnStartY.addSelectionListener(selectionListener);
 
 		Label lblEndX = new Label(spinnersComp, SWT.RIGHT);
 		lblEndX.setText("Width:");
 
 		spnWidth = new Spinner(spinnersComp, SWT.BORDER);
 		spnWidth.setMinimum(0);
 		spnWidth.setMaximum(xSize);
 		spnWidth.setSize(62, 18);
 		spnWidth.addSelectionListener(selectionListener);
 
 		Label lblEndY = new Label(spinnersComp, SWT.RIGHT);
 		lblEndY.setText("Height:");
 
 		spnHeight = new Spinner(spinnersComp, SWT.BORDER);
 		spnHeight.setSize(62, 18);
 		spnHeight.setMinimum(0);
 		spnHeight.setMaximum(ySize);
 		spnHeight.addSelectionListener(selectionListener);
 
 		setSpinnerValues(xStartPt, yStartPt, width, height);
 
 		Composite aspectComp = new Composite(bottomComposite, SWT.NONE); 
 		aspectComp.setLayout(new GridLayout(4, false));
 		aspectComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		btnOverwriteAspect = new Button(aspectComp,SWT.CHECK);
 		btnOverwriteAspect.setText("Override Aspect-Ratio");
 		btnOverwriteAspect.addSelectionListener(selectionListener);
 
 		spnXAspect = new Spinner(aspectComp,SWT.NONE);
 		spnXAspect.setEnabled(false);
 		spnXAspect.setMinimum(1);
 		spnXAspect.setMaximum(10);
 		spnXAspect.setSelection(1);
 		spnXAspect.setIncrement(1);
 		spnXAspect.addSelectionListener(selectionListener);
 
 		Label lblDelimiter = new Label(aspectComp,SWT.NONE);
 		lblDelimiter.setText(":");
 
 		spnYAspect = new Spinner(aspectComp,SWT.NONE);
 		spnYAspect.setEnabled(false);
 		spnYAspect.setMinimum(1);
 		spnYAspect.setMaximum(10);
 		spnYAspect.setSelection(1);
 		spnYAspect.setIncrement(1);
 		spnYAspect.addSelectionListener(selectionListener);
 
 		return windowComposite;
 	}
 
 	protected void updateSliceRange(int lower, int upper, int max, boolean setValue) {
 		if (upper-lower>100) {
 			GridUtils.setVisible(errorLabel, true);
 			errorLabel.getParent().layout();
 			return;
 		}
 		
 		GridUtils.setVisible(errorLabel, false);
 		errorLabel.getParent().layout();
 
 		if (setValue) { // Send to UI
 			sliceSlider.setMaximum(max);
 			sliceSlider.setLowerValue(lower);
 			sliceSlider.setUpperValue(upper);
 			lowerControl.setIntegerValue(lower);
 			upperControl.setIntegerValue(upper);
 	        upperControl.setMaximum(max);
 		} else {        // Send to region
 			final LinearROI   roi   = new LinearROI(new double[]{lower,0}, new double[]{upper,0});
 			windowJob.schedule(roi);
 		}
 	
 	}
 
 	protected void updateTrace(ITrace trace) {
 		if (trace instanceof ISurfaceTrace) {
 		    setActionsEnabled(true);
 			updateWindowPlot((ISurfaceTrace)trace);
 			((ISurfaceTrace)trace).addPaletteListener(paletteListener);
 		} else if (trace instanceof ILineStackTrace) {
 		    setActionsEnabled(false);
 		    updateSlicePlot((ILineStackTrace)trace);
 		} else {
 		    setActionsEnabled(false);
 			StackLayout stackLayout = (StackLayout)content.getLayout();
 			stackLayout.topControl  = blankComposite;
 			content.layout();
 		}
 	}
 
 	private void setActionsEnabled(boolean enabled) {
 		IContributionManager[] mans = new IContributionManager[]{ 
 				                             getSite().getActionBars().getToolBarManager(),
 				                             getSite().getActionBars().getMenuManager()};
 		for (IContributionManager man : mans) {
 			IContributionItem[] items = man.getItems();
 			for (IContributionItem item : items) {
 				item.setVisible(enabled);
 			}
 			man.update(true);
 		}
 	}
 
 	protected void updateWindowPlot(ISurfaceTrace trace) {
 		StackLayout stackLayout = (StackLayout)content.getLayout();
 		stackLayout.topControl = windowControl;
 
 		AbstractDataset data =  (AbstractDataset)trace.getData();
 		List<IDataset> axes = trace.getAxes();
 		if (axes!=null) axes = Arrays.asList(axes.get(0), axes.get(1));
 		windowSystem.updatePlot2D(data, axes, null);	
 		
 		content.layout();
 	}
 	
 	protected void updateSlicePlot(ILineStackTrace trace) {
 		
 		StackLayout stackLayout = (StackLayout)content.getLayout();
 		stackLayout.topControl  = sliceControl;
 		
 		final LinearROI roi = (LinearROI)trace.getWindow();
 		if (roi!=null) {
 			final int lower = roi.getIntPoint()[0];
 			final int upper = (int)Math.round(((LinearROI)roi).getEndPoint()[0]);
 		    updateSliceRange(lower, upper, trace.getStack().length, true);
 		}
 		content.layout();
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 		}
 		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
 			final ITrace trace = getTrace();
 			if (trace!=null) updateTrace(trace);
 
 			windowSystem.addRegionListener(regionListener);
 
 			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
 			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.addROIListener(roiListener);
 			windowJob.schedule();
 		}
 		if (spnStartX != null && !spnStartX.isDisposed())
 			spnStartX.addSelectionListener(selectionListener);
 		if (spnStartY != null && !spnStartY.isDisposed())
 			spnStartY.addSelectionListener(selectionListener);
 		if (spnWidth != null && spnWidth.isDisposed())
 			spnWidth.addSelectionListener(selectionListener);
 		if (spnHeight != null && !spnHeight.isDisposed())
 			spnHeight.addSelectionListener(selectionListener);
 	}
 
 	@Override
 	public void deactivate() {
 		super.deactivate();
 
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeTraceListener(traceListener);
 			
 		}
 		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
 			windowSystem.removeRegionListener(regionListener);
 			
 			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
 			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.removeROIListener(roiListener);
 		}
 		if (spnStartX != null && !spnStartX.isDisposed())
 			spnStartX.removeSelectionListener(selectionListener);
 		if (spnStartY != null && !spnStartY.isDisposed())
 			spnStartY.removeSelectionListener(selectionListener);
 		if (spnWidth != null && !spnWidth.isDisposed())
 			spnWidth.removeSelectionListener(selectionListener);
 		if (spnHeight != null && !spnHeight.isDisposed())
 			spnHeight.removeSelectionListener(selectionListener);
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_3D;
 	}
 
 	@Override
 	public Control getControl() {
 		return content;
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return windowSystem;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		if (windowSystem!=null) windowSystem.setFocus();
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	private class WindowJob extends Job {
 
 		private IROI window;
 
 		public WindowJob() {
 			super("Window");
 			setPriority(Job.INTERACTIVE);
 			setUser(false);
 			setSystem(true);
 		}
 		
 		protected void schedule(IROI window) {
 			cancel();
 			this.window = window;
 			schedule();
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 			
 			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
 			final IWindowTrace windowTrace = getWindowTrace();
 			if (windowTrace!=null) {
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						if (monitor.isCanceled()) return;
 						windowTrace.setWindow(window);
 					}
 				});
 			} else {
 				return Status.CANCEL_STATUS;
 			}
 			return Status.OK_STATUS;
 		}
 		
 	}
 
 	private final static int MAXDISPLAYDIM = 1024;
 
 	/**
 	 * Set the spinner values
 	 * @param startX start position in x dimension
 	 * @param startY start position in y dimension
 	 * @param width
 	 * @param height
 	 */
 	protected void setSpinnerValues(final int startX, 
 								 final int startY, 
 								 final int width, 
 								 final int height) {
 		DisplayUtils.runInDisplayThread(true, getControl(), new Runnable() {
 			@Override
 			public void run() {
 				spnStartX.setSelection(startX);
 				spnStartY.setSelection(startY);
 				spnWidth.setSelection(width);
 				spnHeight.setSelection(height);
 			}
 		});
 	}
 }
