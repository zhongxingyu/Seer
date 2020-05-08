 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.editors.plotting;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
 import org.csstudio.swt.xygraph.dataprovider.Sample;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.csstudio.swt.xygraph.linearscale.LinearScale.Orientation;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IAxis;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlotUpdateEvent;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.fable.extensions.FableImageWrapper;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.editors.plotting.swtxy.XYRegionGraph;
 import org.dawb.workbench.ui.editors.plotting.swtxy.XYRegionToolbar;
 import org.dawb.workbench.ui.editors.preference.EditorConstants;
 import org.dawb.workbench.ui.editors.util.ColorUtility;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPart3;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import fable.imageviewer.component.ActionsProvider;
 import fable.imageviewer.component.ImageComponent;
 import fable.imageviewer.component.ImagePlay;
 import fable.imageviewer.model.ImageModel;
 import fable.imageviewer.model.ImageModelFactory;
 
 
 /**
  * Link between EDNA 1D plotting and csstudio plotter and fable plotter.
  * 
  * 
  * @author gerring
  *
  */
 public class LightWeightPlottingSystem extends AbstractPlottingSystem {
 
 	private Logger logger = LoggerFactory.getLogger(LightWeightPlottingSystem.class);
 	
 	private Composite      parent;
 	private IWorkbenchPart part;
 	private IActionBars    bars;
 
 	// 1D Controls
 	private Canvas             xyCanvas;
 	private XYRegionGraph  xyGraph;
 	
 	// 2D Controls
 	private Composite       imageComposite;
 	private ImageComponent  imageComponent;
 	
 	// The plotting mode, used for updates to data
 	private PlotType plottingMode;
 	
 	public void createPlotPart(final Composite      parent,
 							   final String         plotName,
 							   final IActionBars    bars,
 							   final PlotType       hint,
 							   final IWorkbenchPart part) {
 
 		this.parent  = parent;
 		this.part    = part;
 		this.bars    = bars;
 		
 		if (hint==PlotType.IMAGE) {
 			createImageUI();
 		} else {
 			create1DUI();
 		}
 		
 		
 	}
 	
 	@Override
 	public Composite getPlotComposite() {
 		if (xyCanvas!=null)       return xyCanvas;
 		if (imageComposite!=null) return imageComposite;
 		return null;
 	}
 	
 	private void create1DUI() {
 		
 		if (xyCanvas!=null) return;
 		
 		this.xyCanvas = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED|SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND|SWT.V_SCROLL|SWT.H_SCROLL);
 		xyCanvas.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		final LightweightSystem lws = new LightweightSystem(xyCanvas);
 	
 		this.xyGraph = new XYRegionGraph();
 		
         if (bars!=null) if (bars.getMenuManager()!=null)    bars.getMenuManager().removeAll();
         if (bars!=null) if (bars.getToolBarManager()!=null) bars.getToolBarManager().removeAll();
 
         final MenuManager rightClick = new MenuManager();
         if (bars!=null) {
         	final XYRegionToolbar toolbar = new XYRegionToolbar(xyGraph);
         	toolbar.createGraphActions(bars.getToolBarManager(), rightClick);
         }
 
         createAdditionalActions(rightClick);
 		
 		lws.setContents(xyGraph);
 		xyGraph.primaryXAxis.setShowMajorGrid(true);
 		xyGraph.primaryXAxis.setShowMinorGrid(true);		
 		xyGraph.primaryYAxis.setShowMajorGrid(true);
 		xyGraph.primaryYAxis.setShowMinorGrid(true);
 		xyGraph.primaryYAxis.setTitle("");
 		
 		if (bars!=null) bars.updateActionBars();
 		if (bars!=null) bars.getToolBarManager().update(true);
                  
         final Menu rightClickMenu = rightClick.createContextMenu(xyCanvas);
         xyCanvas.setMenu(rightClickMenu);
  
         parent.layout();
 
 	}
 	
 	private Action plotIndex, plotX;
 
 	private boolean datasetChoosingRequired = true;
 
 	/**
 	 * Also uses 'bars' field to add the actions
 	 * @param rightClick
 	 */
 	private void createAdditionalActions(final IContributionManager rightClick) {
 		
         // Add additional if required
         if (extra1DActions!=null&&!extra1DActions.isEmpty()){
         	bars.getToolBarManager().add(new Separator());
         	for (IAction action : extra1DActions) bars.getToolBarManager().add(action);
         }
         
         // Add more actions
         // Rescale
         if (bars!=null) bars.getToolBarManager().add(new Separator());
 		rightClick.add(new Separator());
 		
 		final Action rescaleAction = new Action("Rescale axis when plotted data changes", Activator.getImageDescriptor("icons/rescale.png")) {
 		    public void run() {
 				rescale = !rescale;
 		    }
 		};
 		if (bars!=null) bars.getToolBarManager().add(rescaleAction);
 		rightClick.add(rescaleAction);
 		rescaleAction.setChecked(this.rescale);
 
 		if (bars!=null) bars.getToolBarManager().add(new Separator());
 		rightClick.add(new Separator());
 		
 		if (datasetChoosingRequired) {
 			// By index or using x 
 			final CheckableActionGroup group = new CheckableActionGroup();
 			plotIndex = new Action("Plot using indices", IAction.AS_CHECK_BOX) {
 			    public void run() {
 			    	Activator.getDefault().getPreferenceStore().setValue(EditorConstants.PLOT_X_DATASET, false);
 			    	setChecked(true);
 			    	xfirst = false;
 			    	firePlotListeners(new PlotUpdateEvent(xyGraph));
 			    }
 			};
 			plotIndex.setImageDescriptor(Activator.getImageDescriptor("icons/plotindex.png"));
 			group.add(plotIndex);
 			
 			plotX = new Action("Plot using first data set selected as x-axis", IAction.AS_CHECK_BOX) {
 			    public void run() {
 			    	Activator.getDefault().getPreferenceStore().setValue(EditorConstants.PLOT_X_DATASET, true);
 			    	setChecked(true);
 			    	xfirst = true;
 			    	firePlotListeners(new PlotUpdateEvent(xyGraph));
 			    }
 			};
 			plotX.setImageDescriptor(Activator.getImageDescriptor("icons/plotxaxis.png"));
 			group.add(plotX);
 			
 			this.xfirst = Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.PLOT_X_DATASET);
 			if (xfirst) {
 				plotX.setChecked(true);
 			} else {
 				plotIndex.setChecked(true);
 			}
 			if (bars!=null) {
 				bars.getToolBarManager().add(new Separator());
 				bars.getToolBarManager().add(plotIndex);
 				bars.getToolBarManager().add(plotX);
 		        bars.getToolBarManager().add(new Separator());
 			}
 			
 			rightClick.add(new Separator());
 			rightClick.add(plotIndex);
 			rightClick.add(plotX);
 			rightClick.add(new Separator());
 		}
 		
 				
 	}
 	
 	public void setXfirst(boolean xfirst) {
 		super.setXfirst(xfirst);
 		if (xfirst) {
 			if (plotX!=null) plotX.setChecked(true);
 		} else {
 			if (plotIndex!=null) plotIndex.setChecked(true);
 		}
 	}
 	
 	public void setDatasetChoosingRequired(boolean choosingRequired) {
 		if (plotX!=null)     plotX.setEnabled(choosingRequired);
 		if (plotIndex!=null) plotIndex.setEnabled(choosingRequired);
 		this.datasetChoosingRequired  = choosingRequired;
 	}	
 	
 	private void createImageUI() {
 		
 		if (imageComposite!=null) return;
 		
         this.imageComposite = new Composite(parent, SWT.NONE);
         if (bars.getMenuManager()!=null)    bars.getMenuManager().removeAll();
         if (bars.getToolBarManager()!=null) bars.getToolBarManager().removeAll();
         
         this.imageComponent = new ImageComponent((IWorkbenchPart3)part, new ActionsProvider() {		
 			@Override
 			public IActionBars getActionBars() {
 				return bars;
 			}
 		});
         imageComponent.setStatusLabel(pointControls);
         imageComponent.createPartControl(imageComposite);
         
         // Add additional if required
         if (extraImageActions!=null&&!extraImageActions.isEmpty()){
         	for (IAction action : extraImageActions) bars.getToolBarManager().add(action);
         }
         
         ImagePlay.setView(imageComponent);
          
         GridUtils.removeMargins(imageComposite);
         
         bars.getToolBarManager().update(true);
 		bars.updateActionBars();
         
         parent.layout();
 	}
 
 	/**
 	 * Does not have to be called in UI thread.
 	 */
 	@Override
 	protected void createPlot(final AbstractDataset       data, 
 			                  final List<AbstractDataset> axes,
 			                  final PlotType              mode, 
 			                  final IProgressMonitor      monitor) {
 		
 		if (monitor!=null) monitor.worked(1);
 		
 		final Object[] oa = getIndexedDatasets(data, axes);
 		final AbstractDataset       x   = (AbstractDataset)oa[0];
 		final List<AbstractDataset> ys  = (List<AbstractDataset>)oa[1];
 		final boolean createdIndices    = (Boolean)oa[2];
 		
 
 		if (part!=null) {
 			if (part.getSite().getWorkbenchWindow().getShell().getDisplay().getThread()==Thread.currentThread()) {
 				createPlotInternal(mode, x, ys, createdIndices, monitor);
 			} else {
 				part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						createPlotInternal(mode, x, ys, createdIndices, monitor);
 					}
 				});
 			}
 		} else {
 			createPlotInternal(mode, x, ys, createdIndices, monitor);
 		}
 		
 	}
 	
 	private Object[] getIndexedDatasets(AbstractDataset data,
 			                            List<AbstractDataset> axes) {
 		
 		final AbstractDataset x;
 		final List<AbstractDataset> ys;
 		final boolean createdIndices;
 		if (axes==null || axes.isEmpty()) {
 			ys = new ArrayList<AbstractDataset>(1);
 			ys.add(data);
 			x = DoubleDataset.arange(ys.get(0).getSize());
 			x.setName("Index of "+data.getName());
 			createdIndices = true;
 		} else {
 			x  = data;
 			ys = axes;
 			createdIndices = false;
 		}
 		return new Object[]{x,ys,createdIndices};
 	}
 
 	@Override
 	public void append( final String           name, 
 			            final Number           xValue,
 					    final Number           yValue,
 					    final IProgressMonitor monitor) throws Exception  {       
 		
 		if (!this.plottingMode.is1D()) throw new Exception("Can only add in 1D mode!");
 		if (name==null || "".equals(name)) throw new IllegalArgumentException("The dataset name must not be null or empty string!");
 		
 		if (part!=null) {
 			if (part.getSite().getWorkbenchWindow().getShell().getDisplay().getThread()==Thread.currentThread()) {
 				appendInternal(name, xValue, yValue, monitor);
 			} else {
 				part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						appendInternal(name, xValue, yValue, monitor);
 					}
 				});
 			}
 		} else {
 			appendInternal(name, xValue, yValue, monitor);
 		}
 
 	}
 
 	/**
 	 * Must be called in UI thread.
 	 * 
 	 * @param mode
 	 * @param x
 	 * @param ys
 	 * @param createdIndices
 	 * @param monitor
 	 */
 	private void createPlotInternal(final PlotType mode, 
 			                        final AbstractDataset x, 
 			                        final List<AbstractDataset> ys, 
 			                        final boolean createdIndices, 
 			                        final IProgressMonitor monitor) {
 		
 		this.plottingMode = mode;
 		switchPlotUI(mode.is1D());
 		if (mode.is1D()) {
 			create1DPlot(x,ys,createdIndices,monitor);
 		} else {
             createImagePlot(x,ys,monitor);
 		}
 		if (monitor!=null) monitor.worked(1);
 	}
 	
     /**
      * Do not call before createPlotPart(...)
      */
 	public void setDefaultPlotType(PlotType mode) {
 		switchPlotUI(mode.is1D());
 	}
 
 	/**
 	 * Must be called in UI thread
 	 * @param is1d
 	 */
 	private void switchPlotUI(final boolean is1d) {
 
 		if (is1d) {
 			if (imageComponent!=null) {
 				imageComposite.setVisible(false);
 				imageComposite.dispose();
 				imageComposite = null;
 				imageComponent.dispose();
 				imageComponent= null;
 			}
 			create1DUI();
 
 		} else {
 			clearTraces();
 			if (xyCanvas!=null) {
 				xyCanvas.setVisible(false);
 				xyCanvas.dispose();
 				xyCanvas=null;
 				xyGraph.removeAll();
 				xyGraph=null;
 			}
 			createImageUI();
 		}
 	}
 
 	/**
 	 * Must be called in UI thread. Creates and updates image.
 	 * 
 	 * @param data
 	 * @param axes
 	 * @param monitor
 	 */
 	private void createImagePlot(final AbstractDataset       data, 
 								 final List<AbstractDataset> axes,
 								 final IProgressMonitor      monitor) {
 
 		try {
 			if (colorMap != null) colorMap.clear();
 			final FableImageWrapper wrapper = new FableImageWrapper(data.getName(), data, axes, -1);
 			if (monitor!=null) monitor.worked(1);
 
 			final ImageModel model = ImageModelFactory.getImageModel(wrapper.getFileName(),
 					                                                 wrapper.getWidth(),
 					                                                 wrapper.getHeight(),
 					                                                 wrapper.getImage());
 			imageComponent.loadModel(model);
 
 			if (monitor!=null) monitor.worked(1);
 
 			String name = data.getName();
 			if ("".equals(name))name=null;
 			imageComponent.setPlotTitle(name);
 
 
 		} catch (Throwable e) {
 			logger.error("Cannot load file "+data.getName(), e);
 		}
 	}
 
 	
 	/**
 	 * An IdentityHashMap used to map AbstractDataset to color used to plot it.
 	 * records keys for both strings and sets so that different models for the
 	 * file being plotted work. Sometimes dataset name is unique but the set is
 	 * not, sometimes the dataset is unique but its name is not.
 	 */
 	private Map<Object, Color> colorMap; // Warning can be mem leak
 	
 	
 	/**
 	 * A map for recording traces to be used in the update method.
 	 * 
 	 * Uses a map of abstract data set name to Trace to retrieve Trace on the
 	 * update.
 	 */
 	private Map<String, Trace> traceMap; // Warning can be mem leak
 
 
 	private void create1DPlot(  final AbstractDataset       xIn, 
 								final List<AbstractDataset> ysIn,
 								final boolean               createdIndices,
 								final IProgressMonitor      monitor) {
 		
 		Object[] oa = getOrderedDatasets(xIn, ysIn, createdIndices);
 		final AbstractDataset       x  = (AbstractDataset)oa[0];
 		final List<AbstractDataset> ys = (List<AbstractDataset>)oa[1];
 		
 		if (colorMap == null && getColorOption()!=ColorOption.NONE) {
 			if (getColorOption()==ColorOption.BY_NAME) {
 				colorMap = new HashMap<Object,Color>(ys.size());
 			} else {
 				colorMap = new IdentityHashMap<Object,Color>(ys.size());
 			}
 		}
 		if (traceMap==null) traceMap = new HashMap<String, Trace>(31);
 	
 		if (xyGraph==null) return;
 		
 		Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		xAxis.setVisible(true);
 		yAxis.setVisible(true);
 
 		// TODO Fix titles for multiple calls to create1DPlot(...)
 		xyGraph.setTitle(DatasetTitleUtils.getTitle(x, ys, true, rootName));
 		xAxis.setTitle(DatasetTitleUtils.getName(x,rootName));
 		
 		processRescale(x,ys);
 
 		//create a trace data provider, which will provide the data to the trace.
 		int iplot = 0;
 		final double[] xArray = getDoubleArray(x);
 		for (AbstractDataset y : ys) {
 
 			CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
 			if (y.getBuffer()!=null && y.getShape()!=null) {
 				traceDataProvider.setBufferSize(y.getSize());	
 				traceDataProvider.setCurrentXDataArray(xArray);
 				traceDataProvider.setCurrentYDataArray(getDoubleArray(y));	
 			}
 			
 			//create the trace
 			final Trace trace = new Trace(DatasetTitleUtils.getName(y,rootName), 
 					                      xAxis, 
 					                      yAxis,
 									      traceDataProvider);	
 			
 			if (y.getName()!=null && !"".equals(y.getName())) {
 				traceMap.put(y.getName(), trace);
 			}
 			
 			//set trace property
 			trace.setPointStyle(PointStyle.NONE);
			final Color plotColor = ColorUtility.getSwtColour(colorMap!=null?colorMap.values():null, iplot);
 			if (colorMap!=null) {
 				if (getColorOption()==ColorOption.BY_NAME) {
 					colorMap.put(y.getName(),plotColor);
 				} else {
 					colorMap.put(y,          plotColor);
 				}
 			}
 			trace.setTraceColor(plotColor);
 
 			//add the trace to xyGraph
 			xyGraph.addTrace(trace);
 			if (monitor!=null) monitor.worked(1);
 			iplot++;
 		}
 		
 		xyCanvas.redraw();
 
 	}
 
 	private void appendInternal(final String           name, 
 					                  Number           xValue,
 							    final Number           yValue,
 							    final IProgressMonitor monitor) {
 
 
 		final Trace trace = traceMap.get(name);
 		if (trace==null) return;
 		
 		CircularBufferDataProvider prov = (CircularBufferDataProvider)trace.getDataProvider();
 		if (prov==null) return;
 
 		if (rescale) {
 			try {
 				Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 				
 				if (xValue==null) xValue = xAxis.getRange().getUpper()+1d;					
 				double min = Math.min(xAxis.getRange().getLower(), xValue.doubleValue());
 				double max = Math.max(xAxis.getRange().getUpper(), xValue.doubleValue());
 				xAxis.setRange(min,max);
 
 				Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 				min = Math.min(yAxis.getRange().getLower(), yValue.doubleValue());
 				max = Math.max(yAxis.getRange().getUpper(), yValue.doubleValue());
 				yAxis.setRange(min,max);
 				
 			} catch (Throwable e) {
 				logger.error("Cannot rescale data, internal error.\nNormally this would be thrown back to the calling API but it happening in an update thread.", e);
 				return;
 			}
 		}		
 
 	    
 	    // View all the data - not we can 
 	    // Also do tickers if the buffer size is not
         // changed.
 	    prov.setBufferSize(prov.getSize()+1);  
 	    
 	    // Change data
 	    prov.addSample(new Sample(xValue.doubleValue(), yValue.doubleValue()));
 	    
 	    
 	}
 
 	private void processRescale(final AbstractDataset       x,
 								final List<AbstractDataset> ys) {
 		if (rescale) {
 			try {
 				double min = x.min().doubleValue();
 				double max = x.max().doubleValue();
 				if (min==max) max+=100;
 				Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 				xAxis.setRange(min,max);
 
 				min = getMin(ys);
 				max = getMax(ys);
 				if (min==max) max = max+1;
 				Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 				yAxis.setRange(min,max);
 			} catch (Throwable e) {
 				logger.error("Cannot rescale data, internal error.\nNormally this would be thrown back to the calling API but it happening in an update thread.", e);
 				return;
 			}
 		}		
 	}
 
 	private Object[] getOrderedDatasets(final AbstractDataset       xIn,
 										final List<AbstractDataset> ysIn,
 										final boolean               createdIndices) {
 
 		final AbstractDataset       x;
 		final List<AbstractDataset> ys;
 		if (xfirst || createdIndices) {
 			x = xIn;
 			ys= ysIn;
 		} else {
 			ys = new ArrayList<AbstractDataset>(ysIn.size()+1);
 			ys.add(xIn);
 			ys.addAll(ysIn);
 
 			final int max = getMaxSize(ys);
 			x = AbstractDataset.arange(0, max, 1, AbstractDataset.INT32);
 			x.setName("Indices");
 
 		}
 
 		return new Object[]{x,ys};
 	}
 
 	/**
 	 * Override this method to provide an implementation of title setting.
 	 * @param title
 	 */
 	public void setTitle(final String title) {
 		
 		if (xyGraph!=null) {
 			xyGraph.setTitle(title);
 		} else if (imageComponent!=null) {
 			imageComponent.setPlotTitle(title);
 		} else {
 			throw new RuntimeException("Cannot set the plot title when the plotting system is not created or plotting something!");
 		}
 	}
 	
 	public Color get1DPlotColor(Object object) {
 		if (getColorOption()==ColorOption.NONE) return null;
 		if (colorMap==null) return null;
 		if (object==null) return null;
 		if (colorOption==ColorOption.BY_DATA) {
 			return colorMap.get(object);
 		} else if (colorOption==ColorOption.BY_NAME) {
 			return colorMap.get((String)object);
 		}
 		return null;
 	}
 
 	private double getMin(List<AbstractDataset> sets) {
 		
 		boolean foundSomething = false;
 		double min = Double.MAX_VALUE;
 		for (AbstractDataset set : sets) {
 			try {
 				min = Math.min(min, set.min().doubleValue());
 			} catch (NullPointerException npe) {
 				continue;
 			}
 			foundSomething = true;
 		}
 		if (!foundSomething) return 0d;
 		return min-(Math.abs(min)*0.01);
 	}
 	
 	private double getMax(List<AbstractDataset> sets) {
 		
 		boolean foundSomething = false;
 		double max = -Double.MAX_VALUE;
 		for (AbstractDataset set : sets) {
 			try {
 				max = Math.max(max, set.max().doubleValue());
 			} catch (NullPointerException npe) {
 				continue;
 			}
 			foundSomething = true;
 		}
 		if (!foundSomething) return 100d;
 		return max+(Math.abs(max)*0.01);
 	}
 	
 	private int getMaxSize(List<AbstractDataset> sets) {
 		int max = 1; // Cannot be less than one
 		for (AbstractDataset set : sets) {
 			try {
 			    max = Math.max(max, set.getSize());
 			} catch (NullPointerException npe) {
 				continue;
 			}
 		}
 		
 		return max;
 	}
 	
 
 	private double[] getDoubleArray(AbstractDataset x) {
 		return (double[])DatasetUtils.cast(x, AbstractDataset.FLOAT64).getBuffer();
 	}
 
 	@Override
 	public void reset() {
 		if (part.getSite().getWorkbenchWindow().getShell().getDisplay().getThread()==Thread.currentThread()) {
 			resetInternal();
 		} else {
 			part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					resetInternal();
 				}
 			});
 		}
 	}
 	private void resetInternal() {
 		
 		if (colorMap!=null) colorMap.clear();
 		if (xyGraph!=null) {
 			try {
 				clearTraces();
 				for (Axis axis : xyGraph.getAxisList()) axis.setRange(0,100);
 	
 			} catch (Throwable e) {
 				logger.error("Cannot remove plots!", e);
 			}
 		}
 		
 		if (imageComponent!=null) {
 			imageComponent.loadModel(ImageModelFactory.getImageModel("", 0, 0, new float[]{0,0}));
 		}
 		
 	}
 	
 	@Override
 	public void clear() {
 		if (part.getSite().getWorkbenchWindow().getShell().getDisplay().getThread()==Thread.currentThread()) {
 			clearInternal();
 		} else {
 			part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					clearInternal();
 				}
 			});
 		}
 	}
 	private void clearInternal() {		
 		if (xyGraph!=null) {
 			try {
 				clearTraces();
				//if (colorMap!=null) colorMap.clear();	
 	
 			} catch (Throwable e) {
 				logger.error("Cannot remove traces!", e);
 			}
 		}	
 	}
 
 
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (colorMap!=null) {
 			colorMap.clear();
 			colorMap = null;
 		}
 		clearTraces();
 		if (xyGraph!=null) {
 			xyGraph.removeAll();
 			xyGraph = null;
 		}
 	    plotIndex = null;
 	    plotX     = null;
  	}
 
 	private void clearTraces() {
 		if (xyGraph!=null)  xyGraph.clearTraces();
 		if (traceMap!=null) traceMap.clear();
 	}
 
 	public void repaint() {
 		part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable() {
 			public void run() {
 				if (xyCanvas!=null) LightWeightPlottingSystem.this.xyCanvas.redraw();
 				if (imageComponent!=null) LightWeightPlottingSystem.this.imageComposite.redraw();
 			}
 		});
 	}
 	
 	/**
 	 * Creates an image of the same size as the Rectangle passed in.
 	 * @param size
 	 * @return
 	 */
 	@Override
 	public Image getImage(Rectangle size) {
 		return xyGraph.getImage(size);
 	}
 	
 	/**
 	 * Can throw exception, gives access to Trace objects for when custom Trace work is needed.
 	 * 
 	 * Maybe return null or throw exception if not 1D. Access discouraged, just for emergencies!
 	 * To use cast your IPlottingSystem to LightWeightPlottingSystem
 	 * 
 	 * @param plotName
 	 * @return
 	 */
 	public Trace getTrace(final String plotName) {
 		return traceMap.get(plotName);
 	}
 	
 	/**
 	 * Access to the XYGraph, may return null. Access discouraged, just for emergencies!
 	 * To use cast your IPlottingSystem to LightWeightPlottingSystem
 	 * 
 	 * @return
 	 */
 	public XYGraph getGraph() {
 		return xyGraph;
 	}
 
 	/**
 	 * Use this method to create axes other than the default y and x axes.
 	 * 
 	 * @param title
 	 * @param isYAxis, normally it is.
 	 * @param side - either SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM
 	 * @return
 	 */
 	public IAxis createAxis(final String title, final boolean isYAxis, int side) {
 		
 		if (xyGraph==null) switchPlotUI(true);
 			
 		Axis axis = new Axis(title, isYAxis);
 		if (isYAxis) {
 			axis.setOrientation(Orientation.VERTICAL);
 		} else {
 			axis.setOrientation(Orientation.HORIZONTAL);
 		}
 		if (side==SWT.LEFT||side==SWT.BOTTOM) {
 		    axis.setTickLableSide(LabelSide.Primary);
 		} else {
 			axis.setTickLableSide(LabelSide.Secondary);
 		}
 		axis.setAutoScaleThreshold(0.1);
 		axis.setShowMajorGrid(true);
 		axis.setShowMinorGrid(true);		
 	
 		xyGraph.addAxis(axis);
 		
 		return new AxisWrapper(axis);
 	}
 	
 	private IAxis selectedXAxis;
 	private IAxis selectedYAxis;
 
 	@Override
 	public IAxis getSelectedXAxis() {
 		if (selectedXAxis==null) {
 			if (xyGraph==null) switchPlotUI(true);
 			return new AxisWrapper(xyGraph.primaryXAxis);
 		}
 		return selectedXAxis;
 	}
 
 	@Override
 	public void setSelectedXAxis(IAxis selectedXAxis) {
 		this.selectedXAxis = selectedXAxis;
 	}
 
 	@Override
 	public IAxis getSelectedYAxis() {
 		if (selectedYAxis==null) {
 			if (xyGraph==null) switchPlotUI(true);
 			return new AxisWrapper(xyGraph.primaryYAxis);
 		}
 		return selectedYAxis;
 	}
 
 	@Override
 	public void setSelectedYAxis(IAxis selectedYAxis) {
 		this.selectedYAxis = selectedYAxis;
 	}
 	
 }
