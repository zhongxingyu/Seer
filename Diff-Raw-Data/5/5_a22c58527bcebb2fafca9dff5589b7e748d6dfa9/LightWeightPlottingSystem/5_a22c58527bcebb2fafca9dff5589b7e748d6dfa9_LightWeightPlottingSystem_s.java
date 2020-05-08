 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.plotting.system;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
 import org.csstudio.swt.xygraph.figures.XYGraphFlags;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.csstudio.swt.xygraph.linearscale.LinearScale.Orientation;
 import org.csstudio.swt.xygraph.undo.AddAnnotationCommand;
 import org.csstudio.swt.xygraph.undo.RemoveAnnotationCommand;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IAxis;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingActionBarManager;
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionContainer;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceContainer;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.plotting.printing.PlotExportPrintUtil;
 import org.dawb.workbench.plotting.printing.PlotPrintPreviewDialog;
 import org.dawb.workbench.plotting.printing.PrintSettings;
 import org.dawb.workbench.plotting.system.swtxy.AspectAxis;
 import org.dawb.workbench.plotting.system.swtxy.ImageTrace;
 import org.dawb.workbench.plotting.system.swtxy.LineTrace;
 import org.dawb.workbench.plotting.system.swtxy.RegionArea;
 import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
 import org.dawb.workbench.plotting.system.swtxy.selection.AbstractSelectionRegion;
 import org.dawb.workbench.plotting.system.swtxy.selection.SelectionRegionFactory;
 import org.dawb.workbench.plotting.util.ColorUtility;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.printing.PrintDialog;
 import org.eclipse.swt.printing.Printer;
 import org.eclipse.swt.printing.PrinterData;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 
 
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
 
 	// Controls
 	private Canvas         xyCanvas;
 	private XYRegionGraph  xyGraph;
 		
 	// The plotting mode, used for updates to data
 	private PlotType plottingMode;
 
 	private LightWeightActionBarsManager lightWeightActionBarMan;
 	
 	public LightWeightPlottingSystem() {
 		super();
 		this.lightWeightActionBarMan = (LightWeightActionBarsManager)this.actionBarManager;
 	}
 	
 	
 	public void createPlotPart(final Composite      parent,
 							   final String         plotName,
 							   final IActionBars    bars,
 							   final PlotType       hint,
 							   final IWorkbenchPart part) {
 
 		super.createPlotPart(parent, plotName, bars, hint, part);
 		
 		this.parent  = parent;
 		
 		createUI();
 
 		// TODO Preselect PAN for IMAGE PlotType?
 	}
 	
 	@Override
 	protected PlottingActionBarManager createActionBarManager() {
 		return new LightWeightActionBarsManager(this);
 	}
 
 	
 	@Override
 	public Composite getPlotComposite() {
 		if (xyCanvas!=null)       return xyCanvas;
 		return null;
 	}
 		
 	private LightweightSystem lws;
 	
 	private void createUI() {
 		
 		if (xyCanvas!=null) return;		
 		
 		this.xyCanvas = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED|SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
 		lws = new LightweightSystem(xyCanvas);
 		
 		// Stops a mouse wheel move corrupting the plotting area, but it wobbles a bit.
 		xyCanvas.addMouseWheelListener(getMouseWheelListener());
 		xyCanvas.addKeyListener(getKeyListener());
 		
 		lws.setControl(xyCanvas);
 		xyCanvas.setBackground(xyCanvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 	
 		this.xyGraph = new XYRegionGraph();
 		xyGraph.setSelectionProvider(getSelectionProvider());
 		
 		// We contain the action bars in an internal object
 		// if the API user said they were null. This allows the API
 		// user to say null for action bars and then use:
 		// getPlotActionSystem().fillXXX() to add their own actions.
  		if (bars==null) bars = lightWeightActionBarMan.createEmptyActionBars(); 
  				
 // 		bars.getMenuManager().removeAll();
 // 		bars.getToolBarManager().removeAll();
 
  		lightWeightActionBarMan.init();
  		lightWeightActionBarMan.createConfigActions();
  		lightWeightActionBarMan.createAnnotationActions();
  		lightWeightActionBarMan.createRegionActions();
  		lightWeightActionBarMan.createToolDimensionalActions(ToolPageRole.ROLE_1D, "org.dawb.workbench.plotting.views.toolPageView.1D");
  		lightWeightActionBarMan.createToolDimensionalActions(ToolPageRole.ROLE_2D, "org.dawb.workbench.plotting.views.toolPageView.2D");
  		lightWeightActionBarMan.createToolDimensionalActions(ToolPageRole.ROLE_1D_AND_2D, "org.dawb.workbench.plotting.views.toolPageView.1D_and_2D");
  		lightWeightActionBarMan.createZoomActions(XYGraphFlags.COMBINED_ZOOM);
  		lightWeightActionBarMan.createUndoRedoActions();
  		lightWeightActionBarMan.createExportActionsToolBar();
  		lightWeightActionBarMan.createAspectHistoAction();
  		lightWeightActionBarMan.createPalleteActions();
  		lightWeightActionBarMan.createOriginActions();
  		lightWeightActionBarMan.createExportActionsMenuBar();
  		lightWeightActionBarMan.createAdditionalActions(null);
 		 		
 		lws.setContents(xyGraph);
 		xyGraph.primaryXAxis.setShowMajorGrid(true);
 		xyGraph.primaryXAxis.setShowMinorGrid(true);		
 		xyGraph.primaryYAxis.setShowMajorGrid(true);
 		xyGraph.primaryYAxis.setShowMinorGrid(true);
 		xyGraph.primaryYAxis.setTitle("");
 		
 		if (bars!=null) bars.updateActionBars();
 		if (bars!=null) bars.getToolBarManager().update(true);
            
  		final MenuManager popupMenu = new MenuManager();
 		popupMenu.setRemoveAllWhenShown(true); // Remake menu each time
         xyCanvas.setMenu(popupMenu.createContextMenu(xyCanvas));
         popupMenu.addMenuListener(getIMenuListener());
         
         if (defaultPlotType!=null) {
 		    this.lightWeightActionBarMan.switchActions(defaultPlotType);
         }
 
         parent.layout();
 
 	}
 
 	private IMenuListener popupListener;
 	private IMenuListener getIMenuListener() {
 		if (popupListener == null) {
 			popupListener = new IMenuListener() {			
 				@Override
 				public void menuAboutToShow(IMenuManager manager) {
 					Point   pnt       = Display.getDefault().getCursorLocation();
 					Point   par       = xyCanvas.toDisplay(new Point(0,0));
 					final int xOffset = par.x+xyGraph.getLocation().x;
 					final int yOffset = par.y+xyGraph.getLocation().y;
 					
 					final IFigure fig = xyGraph.findFigureAt(pnt.x-xOffset, pnt.y-yOffset);
 					if (fig!=null) {
 					    if (fig instanceof IRegionContainer) {
 							final IRegion region = ((IRegionContainer)fig).getRegion();
 							SelectionRegionFactory.fillActions(manager, region, xyGraph);
 					    }
 					    if (fig instanceof ITraceContainer) {
 							final ITrace trace = ((ITraceContainer)fig).getTrace();
 							LightWeightActionBarsManager.fillTraceActions(manager, trace, LightWeightPlottingSystem.this);
 					    }
 					    
 					    if (fig instanceof Label && fig.getParent() instanceof Annotation) {
 					    	
 					    	LightWeightActionBarsManager.fillAnnotationConfigure(manager, (Annotation)fig.getParent(), LightWeightPlottingSystem.this);
 					    }
 					}
 					lightWeightActionBarMan.fillZoomActions(manager);
 					manager.update();
 				}
 			};
 		}
 		return popupListener;
 	}
 
 
 	private MouseWheelListener mouseWheelListener;
 	private MouseWheelListener getMouseWheelListener() {
 		if (mouseWheelListener == null) mouseWheelListener = new MouseWheelListener() {
 			@Override
 			public void mouseScrolled(MouseEvent e) {
 				if (xyGraph==null) return;
 				if (e.count==0)    return;
 				int direction = e.count > 0 ? 1 : -1;
 				xyGraph.setZoomLevel(e, direction*0.1d);
 				xyGraph.repaint();
 			}	
 		};
 		return mouseWheelListener;
 	}
 
 	private KeyListener keyListener;
 	private KeyListener getKeyListener() {
 		if (keyListener==null) keyListener = new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.keyCode==16777230 || e.character=='h') {
 					final IContributionItem action = bars.getToolBarManager().find("org.dawb.workbench.plotting.histo");
 				    if (action!=null && action.isVisible() && action instanceof ActionContributionItem) {
 				    	ActionContributionItem iaction = (ActionContributionItem)action;
 				    	iaction.getAction().setChecked(!iaction.getAction().isChecked());
 				    	iaction.getAction().run();
 				    }
 				} else if (e.keyCode==16777217) {//Up
  					Point point = Display.getDefault().getCursorLocation();
  					point.y-=1;
  					Display.getDefault().setCursorLocation(point);
 					
  				} else if (e.keyCode==16777218) {//Down
  					Point point = Display.getDefault().getCursorLocation();
  					point.y+=1;
  					Display.getDefault().setCursorLocation(point);
  					
  				} else if (e.keyCode==16777219) {//Left
  					Point point = Display.getDefault().getCursorLocation();
  					point.x-=1;
  					Display.getDefault().setCursorLocation(point);
 					
  				} if (e.keyCode==16777220) {//Right
  					Point point = Display.getDefault().getCursorLocation();
  					point.x+=1;
  					Display.getDefault().setCursorLocation(point);
  				}
 
 			}
 		};
 		return keyListener;
 	}
 
 
 	public void setFocus() {
 		if (xyCanvas!=null) xyCanvas.setFocus();
 	}
 
 		
 	public void addTraceListener(final ITraceListener l) {
 		super.addTraceListener(l);
 		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).addImageTraceListener(l);
 	}
 	public void removeTraceListener(final ITraceListener l) {
 		super.removeTraceListener(l);
 		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).removeImageTraceListener(l);
 	}
 	
 	
 	public List<ITrace> updatePlot1D(AbstractDataset             x, 
 						             final List<AbstractDataset> ys,
 						             final IProgressMonitor      monitor) {
 
 		final List<ITrace> updatedAndCreated = new ArrayList<ITrace>(3);		
 		final List<AbstractDataset> unfoundYs = new ArrayList<AbstractDataset>(ys.size());
 		
 		for (final AbstractDataset y : ys) {
 			
 			final ITrace trace = getTrace(y.getName());
 			if (trace!=null && trace instanceof ILineTrace) {
 				
 				if (x==null) x = IntegerDataset.arange(y.getSize(), IntegerDataset.INT32);
 				final AbstractDataset finalX = x;
 				final ILineTrace lineTrace = (ILineTrace)trace;
 				updatedAndCreated.add(lineTrace);
 				
 				if (getDisplay().getThread()==Thread.currentThread()) {
 					lineTrace.setData(finalX, y);
 					fireTraceUpdated(new TraceEvent(lineTrace));
 				} else {
 					getDisplay().syncExec(new Runnable() {
 						public void run() {
 							lineTrace.setData(finalX, y);
 							fireTraceUpdated(new TraceEvent(lineTrace));
 						}
 					});
 				}
 				continue;
 			}
 			unfoundYs.add(y);
 		}
 		
 		if (!unfoundYs.isEmpty()) {
 			if (x==null) x = IntegerDataset.arange(unfoundYs.get(0).getSize(), IntegerDataset.INT32);
 			final Collection<ITrace> news = createPlot1D(x, unfoundYs, monitor);
 			updatedAndCreated.addAll(news);
 		}
 		
 		return updatedAndCreated;
 	}
 	
 	@Override
 	public List<ITrace> createPlot1D(final AbstractDataset       xIn, 
 					                 final List<AbstractDataset> ysIn,
 					                 final IProgressMonitor      monitor) {
         return this.createPlot1D(xIn, ysIn, null, monitor);
 	}
 	/**
 	 * Does not have to be called in UI thread.
 	 */
 	@Override
 	public List<ITrace> createPlot1D(final AbstractDataset       xIn, 
 					                 final List<AbstractDataset> ysIn,
 					                 final String                title,
 					                 final IProgressMonitor      monitor) {
 		
 		if (monitor!=null) monitor.worked(1);
 		
 		final Object[] oa = getIndexedDatasets(xIn, ysIn);
 		final AbstractDataset       x   = (AbstractDataset)oa[0];
 		@SuppressWarnings("unchecked")
 		final List<AbstractDataset> ys  = (List<AbstractDataset>)oa[1];
 		final boolean createdIndices    = (Boolean)oa[2];
 		
 
 		final List<ITrace> traces = new ArrayList<ITrace>(7);
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			List<ITrace> ts = createPlot1DInternal(x, ys, title, createdIndices, monitor);
 			if (ts!=null) traces.addAll(ts);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					List<ITrace> ts = createPlot1DInternal(x, ys, title, createdIndices, monitor);
 					if (ts!=null) traces.addAll(ts);
 				}
 			});
 		}
 
 		
 		if (monitor!=null) monitor.worked(1);
 		return traces;
 		
 	}
 	
 	private Display getDisplay() {
 		if (part!=null) return part.getSite().getShell().getDisplay();
 		if (xyCanvas!=null) return xyCanvas.getDisplay();
 		if (parent!=null)  parent.getDisplay();
 		return Display.getDefault();
 	}
 
 	private Object[] getIndexedDatasets(AbstractDataset data,
 			                            List<AbstractDataset> axes) {
 		
 		final AbstractDataset x;
 		final List<AbstractDataset> ys;
 		final boolean createdIndices;
 		if (axes==null || axes.isEmpty()) {
 			ys = new ArrayList<AbstractDataset>(1);
 			ys.add(data);
 			x = IntegerDataset.arange(ys.get(0).getSize());
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
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			appendInternal(name, xValue, yValue, monitor);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					appendInternal(name, xValue, yValue, monitor);
 				}
 			});
 		}
 	}
 
 	/**
      * Do not call before createPlotPart(...)
      */
 	public void setDefaultPlotType(PlotType mode) {
 		this.defaultPlotType = mode;
 		createUI();
 	}
 	
 	public ITrace updatePlot2D(final AbstractDataset       data, 
 							   final List<AbstractDataset> axes,
 							   final IProgressMonitor      monitor) {
 		
 		final Collection<ITrace> traces = getTraces(IImageTrace.class);
 		if (traces!=null && traces.size()>0) {
 			final IImageTrace image = (IImageTrace)traces.iterator().next();
 			final int[]       shape = image.getData()!=null ? image.getData().getShape() : null;
 			if (shape!=null && Arrays.equals(shape, data.getShape())) {
 				if (getDisplay().getThread()==Thread.currentThread()) {
 					if (data.getName()!=null) xyGraph.setTitle(data.getName());
 					image.setData(data, image.getAxes(), false);
 					fireTraceUpdated(new TraceEvent(image));
 				} else {
 					Display.getDefault().syncExec(new Runnable() {
 						public void run() {
 							// This will keep the previous zoom level if there was one
 							// and will be faster than createPlot2D(...) which autoscales.
 							if (data.getName()!=null) xyGraph.setTitle(data.getName());
 							image.setData(data, image.getAxes(), false);
 							fireTraceUpdated(new TraceEvent(image));
 						}
 					});
 				}
 				return image;
 			} else {
 				return createPlot2D(data, axes, monitor);
 			}
 		} else {
 		    return createPlot2D(data, axes, monitor);
 		}
 	}
 
 	/**
 	 * Must be called in UI thread. Creates and updates image.
 	 * NOTE removes previous traces if any plotted.
 	 * 
 	 * @param data
 	 * @param axes, x first.
 	 * @param monitor
 	 */
 	@Override
 	public ITrace createPlot2D(final AbstractDataset       data, 
 							   final List<AbstractDataset> axes,
 							   final IProgressMonitor      monitor) {
   
 		final List<ITrace> traces = new ArrayList<ITrace>(7);
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			ITrace ts = createPlot2DInternal(data, axes, monitor);
 			if (ts!=null) traces.add(ts);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					ITrace ts = createPlot2DInternal(data, axes, monitor);
 					if (ts!=null) traces.add(ts);
 				}
 			});
 		}
 		
 		return traces.size()>0 ? traces.get(0) : null;
 	}
 
 	public ITrace createPlot2DInternal(final AbstractDataset       data, 
 										List<AbstractDataset>       axes,
 										final IProgressMonitor      monitor) {
 		try {
 			
 			this.plottingMode = PlotType.IMAGE;
 			this.lightWeightActionBarMan.switchActions(plottingMode);
 
 			clearTraces(); // Only one image at a time!
 
 			final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 			final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 			xAxis.setTitle(axes!=null&&axes.get(0).getName()!=null ? axes.get(0).getName() : "");
 			xAxis.setLogScale(false);
 			yAxis.setTitle(axes!=null&&axes.get(1).getName()!=null ? axes.get(1).getName() : "");
 			yAxis.setLogScale(false);
             
 			if (data.getName()!=null) xyGraph.setTitle(data.getName());
 			xyGraph.clearTraces();
 			
 			if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
 			traceMap.clear();
 			
 			String traceName = data.getName();
 			if (part!=null&&(traceName==null||"".equals(traceName))) {
 				traceName = part.getTitle();
 			}
 			final ImageTrace trace = xyGraph.createImageTrace(traceName, xAxis, yAxis);
 			trace.setData(data, axes, true);
 			
 			traceMap.put(trace.getName(), trace);
 
 			xyGraph.addImageTrace(trace);
 			
 			fireTraceAdded(new TraceEvent(trace));
 			
 			return trace;
             
 		} catch (Throwable e) {
 			logger.error("Cannot load file "+data.getName(), e);
 			return null;
 		}
 	}
 
 
 	@Override
 	public IImageTrace createImageTrace(String traceName) {
 		final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 		
 		final ImageTrace trace = xyGraph.createImageTrace(traceName, xAxis, yAxis);
 		fireTraceCreated(new TraceEvent(trace));
 		
 		return trace;
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
 	private Map<String, ITrace> traceMap; // Warning can be mem leak
 
 
 	private List<ITrace> createPlot1DInternal(  final AbstractDataset       xIn, 
 										final List<AbstractDataset> ysIn,
 										final String title,
 										final boolean               createdIndices,
 										final IProgressMonitor      monitor) {
 		
 		this.plottingMode = PlotType.PT1D;
 		this.lightWeightActionBarMan.switchActions(plottingMode);
 
 		Object[] oa = getOrderedDatasets(xIn, ysIn, createdIndices);
 		final AbstractDataset       x  = (AbstractDataset)oa[0];
 		@SuppressWarnings("unchecked")
 		final List<AbstractDataset> ys = (List<AbstractDataset>)oa[1];
 		
 		if (colorMap == null && getColorOption()!=ColorOption.NONE) {
 			if (getColorOption()==ColorOption.BY_NAME) {
 				colorMap = new HashMap<Object,Color>(ys.size());
 			} else {
 				colorMap = new IdentityHashMap<Object,Color>(ys.size());
 			}
 		}
 		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
 	
 		if (xyGraph==null) return null;
 		
 		Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		xAxis.setVisible(true);
 		yAxis.setVisible(true);
 
 		if (title==null) {
 			// TODO Fix titles for multiple calls to create1DPlot(...)
 		    xyGraph.setTitle(DatasetTitleUtils.getTitle(x, ys, true, rootName));
 		} else {
 			xyGraph.setTitle(title);
 		}
 		xAxis.setTitle(DatasetTitleUtils.getName(x,rootName));
 		
 
 		//create a trace data provider, which will provide the data to the trace.
 		int iplot = 0;
 		
 		final List<ITrace> traces = new ArrayList<ITrace>(ys.size());
 		for (AbstractDataset y : ys) {
 
 			LightWeightDataProvider traceDataProvider = new LightWeightDataProvider(x, y);
 			
 			//create the trace
 			final LineTrace trace = new LineTrace(DatasetTitleUtils.getName(y,rootName), 
 					                      xAxis, 
 					                      yAxis,
 									      traceDataProvider);	
 			
 			LineTraceImpl wrapper = new LineTraceImpl(this, trace);
 			traces.add(wrapper);
 			
 			if (y.getName()!=null && !"".equals(y.getName())) {
 				traceMap.put(y.getName(), wrapper);
 				trace.setInternalName(y.getName());
 			}
 			
 			//set trace property
 			trace.setPointStyle(PointStyle.NONE);
 			int index = getTraces().size()+iplot-1;
 			if (index<0) index=0;
 			final Color plotColor = ColorUtility.getSwtColour(colorMap!=null?colorMap.values():null, index);
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
 		getDisplay().syncExec(new Runnable() {
 			public void run() {
 				autoscaleAxes();
 				autoscaleAxes();
 			}
 		});
 		fireTracesPlotted(new TraceEvent(traces));
         return traces;
 	}
 	
 	public ILineTrace createLineTrace(String traceName) {
 
 		Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		LightWeightDataProvider traceDataProvider = new LightWeightDataProvider();
 		final LineTrace   trace    = new LineTrace(traceName, xAxis, yAxis, traceDataProvider);
 		final LineTraceImpl wrapper = new LineTraceImpl(this, trace);
 		fireTraceCreated(new TraceEvent(wrapper));
 		return wrapper;
 	}
 	
 	/**
 	 * Adds trace, makes visible
 	 * @param traceName
 	 * @return
 	 */
 	public void addTrace(ITrace trace) {
 		
 		if (traceMap==null) this.traceMap = new HashMap<String, ITrace>(7);
 		traceMap.put(trace.getName(), trace);
 		
 		if (trace instanceof ImageTrace) {
 			this.plottingMode = PlotType.IMAGE;
 			this.lightWeightActionBarMan.switchActions(plottingMode);
 			xyGraph.addImageTrace((ImageTrace)trace);
 			fireTraceAdded(new TraceEvent(trace));
 		} else {
 			this.plottingMode = PlotType.PT1D;
 			this.lightWeightActionBarMan.switchActions(plottingMode);
 			xyGraph.addTrace(((LineTraceImpl)trace).getTrace());
 			xyCanvas.redraw();
 			fireTraceAdded(new TraceEvent(trace));
 		}
 	}
 	/**
 	 * Removes a trace.
 	 * @param traceName
 	 * @return
 	 */
 	public void removeTrace(ITrace trace) {
 		if (traceMap!=null) traceMap.remove(trace.getName());
 		if (trace instanceof LineTraceImpl) {
 			xyGraph.removeTrace(((LineTraceImpl)trace).getTrace());
 		} else if (trace instanceof ImageTrace) {
 			xyGraph.removeImageTrace((ImageTrace)trace);
 		}
 		xyCanvas.redraw();
 		fireTraceRemoved(new TraceEvent(trace));
 	}
 
 	@Override
 	public void renameTrace(final ITrace trace, String name) {
 		if (traceMap!=null) traceMap.remove(trace.getName());
 		trace.setName(name);
 		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(3);
 		traceMap.put(name, trace);
 	}
 	
 	public Collection<ITrace> getTraces() {
 		if (traceMap==null) return Collections.emptyList();
 		return traceMap.values();
 	}
 
 	@Override
 	public ITrace getTrace(String name) {
 		if (traceMap==null) return null;
 		return traceMap.get(name);
 	}
 
 	private void appendInternal(final String           name, 
 					                  Number           xValue,
 							    final Number           yValue,
 							    final IProgressMonitor monitor) {
 
 
 		final ITrace wrapper = traceMap.get(name);
 		if (wrapper==null) return;
 		
 		final Trace trace = ((LineTraceImpl)wrapper).getTrace();
 		
 		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
 		if (prov==null) return;
 
 		prov.append(xValue, yValue);
 	}
 	
 	
     /**
      * Thread safe method
      */
 	@Override
 	public AbstractDataset getData(String name) {
 		
 		final ITrace wrapper = traceMap.get(name);
 		if (wrapper==null) return null;
 		
 		final Trace trace = ((LineTraceImpl)wrapper).getTrace();
 		if (trace==null) return null;
 		
 		return getData(name, trace, true);
 	}
 	
 	/**
 	 * Thread safe method
 	 * @param name
 	 * @param trace
 	 * @param isY
 	 * @return
 	 */
 	protected AbstractDataset getData(String name, Trace trace, boolean isY) {
 
 		if (trace==null) return null;
 		
 		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
 		if (prov==null) return null;
 		
 		return isY ? prov.getY() : prov.getX();
 	}
 
 	private Object[] getOrderedDatasets(final AbstractDataset       xIn,
 										final List<AbstractDataset> ysIn,
 										final boolean               createdIndices) {
 
 		final AbstractDataset       x;
 		final List<AbstractDataset> ys;
 		if ((xfirst || createdIndices) && xIn!=null) {
 			x = xIn;
 			ys= ysIn;
 		} else {
 			ys = new ArrayList<AbstractDataset>(ysIn.size()+1);
 			if (xIn!=null) ys.add(xIn);
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
 			xyGraph.repaint();
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
 	
 
 	@Override
 	public void reset() {
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			resetInternal();
 		} else {
 			getDisplay().syncExec(new Runnable() {
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
 				
 	}
 	
 	@Override
 	public void clear() {
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			clearInternal();
 		} else {
 			getDisplay().syncExec(new Runnable() {
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
 				if (colorMap!=null) colorMap.clear();	
 	
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
 			xyGraph.dispose();
 			xyGraph = null;
 		}
 		if (xyCanvas!=null && !xyCanvas.isDisposed()) {
 			xyCanvas.removeMouseWheelListener(getMouseWheelListener());
 			xyCanvas.removeKeyListener(getKeyListener());
 			xyCanvas.dispose();
 		}
 	}
 
 	private void clearTraces() {
 		if (xyGraph!=null)  xyGraph.clearTraces();
 		if (traceMap!=null) traceMap.clear();
 		fireTracesCleared(new TraceEvent(this));
 	}
 
 	public void repaint() {
 		repaint(true);
 	}
 	public void repaint(final boolean autoScale) {
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			if (xyCanvas!=null) {
 				if (autoScale) LightWeightPlottingSystem.this.xyGraph.performAutoScale();
 				LightWeightPlottingSystem.this.xyCanvas.layout(xyCanvas.getChildren());
 				LightWeightPlottingSystem.this.xyGraph.revalidate();
 				LightWeightPlottingSystem.this.xyGraph.repaint();
 			}
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				public void run() {
 					if (xyCanvas!=null) {
 						if (autoScale)LightWeightPlottingSystem.this.xyGraph.performAutoScale();
 						LightWeightPlottingSystem.this.xyCanvas.layout(xyCanvas.getChildren());
 						LightWeightPlottingSystem.this.xyGraph.revalidate();
 						LightWeightPlottingSystem.this.xyGraph.repaint();
 					}
 				}
 			});
 		}
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
 	 * Access to the XYGraph, may return null. Access discouraged, just for emergencies!
 	 * To use cast your IPlottingSystem to LightWeightPlottingSystem
 	 * 
 	 * @return
 	 */
 	public XYRegionGraph getGraph() {
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
 		
 		if (xyGraph==null) createUI();
 			
 		Axis axis = new AspectAxis(title, isYAxis);
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
 			if (xyGraph==null) createUI();
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
 			if (xyGraph==null) createUI();
 			return new AxisWrapper(xyGraph.primaryYAxis);
 		}
 		return selectedYAxis;
 	}
 
 	@Override
 	public void setSelectedYAxis(IAxis selectedYAxis) {
 		this.selectedYAxis = selectedYAxis;
 	}
 	
 	public boolean addRegionListener(final IRegionListener l) {
 		if (xyGraph==null) createUI();
 		return xyGraph.addRegionListener(l);
 	}
 	
 	public boolean removeRegionListener(final IRegionListener l) {
 		if (xyGraph==null) return false;
 		return xyGraph.removeRegionListener(l);
 	}
 	
 	/**
 	 * Throws exception if region exists already.
 	 * @throws Exception 
 	 */
 	public IRegion createRegion(final String name, final RegionType regionType) throws Exception  {
 
 		if (xyGraph==null) createUI();
 		final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		return xyGraph.createRegion(name, xAxis, yAxis, regionType, true);
 	}
 	
 	/**
 	 * Thread safe
 	 */
 	public void clearRegions() {
 		if (xyGraph==null) return;
 		
 		xyGraph.clearRegions();
 	}
 	
 	protected void clearRegionTool() {
 		if (xyGraph==null) return;
 		
 		xyGraph.clearRegionTool();
 	}
 
 	/**
 	 * Add a selection region to the graph.
 	 * @param region
 	 */
 	public void addRegion(final IRegion region) {		
 		if (xyGraph==null) createUI();
 		final AbstractSelectionRegion r = (AbstractSelectionRegion)region;
 		xyGraph.addRegion(r);		
  	}
 	
 	/**
 	 * Remove a selection region to the graph.
 	 * @param region
 	 */
 	public void removeRegion(final IRegion region) {		
 		if (xyGraph==null) createUI();
 		final AbstractSelectionRegion r = (AbstractSelectionRegion)region;
 		xyGraph.removeRegion(r);
 	}
 	
 	@Override
 	public void renameRegion(final IRegion region, String name) {
 		if (xyGraph==null) return;
 		xyGraph.renameRegion((AbstractSelectionRegion)region, name);
 	}
 
 	/**
 	 * Get a region by name.
 	 * @param name
 	 * @return
 	 */
 	public IRegion getRegion(final String name) {
 		if (xyGraph==null)  return null;
 		return xyGraph.getRegion(name);
 	}
 	/**
 	 * Get regions
 	 * @param name
 	 * @return
 	 */
 	public Collection<IRegion> getRegions() {
 		if (xyGraph==null)  return null;
 		List<AbstractSelectionRegion> regions = xyGraph.getRegions();
 		return new ArrayList<IRegion>(regions);
 	}
 	
 	public IAnnotation createAnnotation(final String name) throws Exception {
 		if (xyGraph==null) createUI();
 		
 		final List<Annotation>anns = xyGraph.getPlotArea().getAnnotationList();
 		for (Annotation annotation : anns) {
 			if (annotation.getName()!=null&&annotation.getName().equals(name)) {
 				throw new Exception("The annotation name '"+name+"' is already taken.");
 			}
 		}
 		
 		final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 		
 		return new AnnotationWrapper(name, xAxis, yAxis);
 	}
 	
 	/**
 	 * Add an annotation to the graph.
 	 * @param region
 	 */
 	public void addAnnotation(final IAnnotation annotation) {
 		
         final AnnotationWrapper wrapper = (AnnotationWrapper)annotation;
         xyGraph.addAnnotation(wrapper.getAnnotation());
         xyGraph.getOperationsManager().addCommand(new AddAnnotationCommand(xyGraph, wrapper.getAnnotation()));
 	}
 	
 	
 	/**
 	 * Remove an annotation to the graph.
 	 * @param region
 	 */
 	public void removeAnnotation(final IAnnotation annotation) {
         final AnnotationWrapper wrapper = (AnnotationWrapper)annotation;
         xyGraph.removeAnnotation(wrapper.getAnnotation());
         xyGraph.getOperationsManager().addCommand(new RemoveAnnotationCommand(xyGraph, wrapper.getAnnotation()));
 	}
 	@Override
 	public void renameAnnotation(final IAnnotation annotation, String name) {
         final AnnotationWrapper wrapper = (AnnotationWrapper)annotation;
         wrapper.getAnnotation().setName(name);
 	}
 	/**
 	 * Get an annotation by name.
 	 * @param name
 	 * @return
 	 */
 	public IAnnotation getAnnotation(final String name) {
 		final List<Annotation>anns = xyGraph.getPlotArea().getAnnotationList();
 		for (Annotation annotation : anns) {
 			if (annotation.getName()!=null&&annotation.getName().equals(name)) {
 				return new AnnotationWrapper(annotation);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Remove all annotations
 	 */
 	public void clearAnnotations(){
 		final List<Annotation>anns = new ArrayList<Annotation>(xyGraph.getPlotArea().getAnnotationList());
 		for (Annotation annotation : anns) {
 			xyGraph.getPlotArea().removeAnnotation(annotation);
 		}
 	}
 
 	@Override
 	public void autoscaleAxes() {
 		if (xyGraph==null) return;
 		xyGraph.performAutoScale();
 	}
 
 	/**
 	 * Override this method to provide an implementation of axis visibility.
 	 * @param isVisible
 	 * @param title
 	 */
 	public void setAxisAndTitleVisibility(boolean isVisible, String title) {
 		if (xyGraph!=null) {
 			xyGraph.primaryXAxis.setVisible(isVisible);
 			xyGraph.primaryYAxis.setVisible(isVisible);
 			xyGraph.setTitle(title);
 			xyGraph.repaint();
 		} else {
 			throw new RuntimeException("Cannot set the axis visibility when the plotting system is not created or plotting something!");
 		}
 	}
 
 	// Print / Export methods
 	private PrintSettings settings;
 
 	@Override
 	public void printPlotting(){
 		if (settings==null) settings = new PrintSettings();
 		PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(xyGraph, Display.getCurrent(), settings);
 		settings=dialog.open();
 	}
 
 	/**
 	 * Print scaled plotting to printer
 	 * TODO to be disable once this works in printPlotting
 	 */
 	public void printSnapshotPlotting(){
 		// Show the Choose Printer dialog
 		PrintDialog dialog = new PrintDialog(Display.getCurrent().getActiveShell(), SWT.NULL);
 		PrinterData printerData = dialog.open();
 		if (printerData != null) {
 			// Create the printer object
 			printerData.orientation = PrinterData.LANDSCAPE; // force landscape
 			Printer printer = new Printer(printerData);
 			// Calculate the scale factor between the screen resolution and printer
 			// resolution in order to correctly size the image for the printer
 			Point screenDPI = Display.getCurrent().getDPI();
 			Point printerDPI = printer.getDPI();
 			int scaleFactorX = printerDPI.x / screenDPI.x;
 			// Determine the bounds of the entire area of the printer
 			Rectangle size = printer.getClientArea();
 			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
 			Rectangle imageSize = new Rectangle(size.x/scaleFactorX, size.y/scaleFactorX, 
 						size.width/scaleFactorX, size.height/scaleFactorX);
 			if (printer.startJob("Print Plot")) {
 				if (printer.startPage()) {
 					GC gc = new GC(printer);
 					Image xyImage = xyGraph.getImage(imageSize);
 					Image printerImage = new Image(printer, xyImage.getImageData());
 					xyImage.dispose();
 					// Draw the image
 					gc.drawImage(printerImage, imageSize.x, imageSize.y,
 							imageSize.width, imageSize.height, -trim.x, -trim.y,
 							size.width-trim.width, size.height-trim.height);
 					// Clean up
 					printerImage.dispose();
 					gc.dispose();
 					printer.endPage();
 				}
 			}
 			// End the job and dispose the printer
 			printer.endJob();
 			printer.dispose();
 		}
 	}
 
 	@Override
 	public void copyPlotting(){
 		PlotExportPrintUtil.copyGraph(xyGraph.getImage());
 	}
 
 	@Override
 	public void savePlotting(String filename){
 		FileDialog dialog = new FileDialog (Display.getCurrent().getActiveShell(), SWT.SAVE);
 		String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps"};
 		// TODO ,"*.svg;*.SVG"};
 		if (filename!=null) {
 			dialog.setFilterPath((new File(filename)).getParent());
 		} else {
 			String filterPath = "/";
 			String platform = SWT.getPlatform();
 			if (platform.equals("win32") || platform.equals("wpf")) {
 				filterPath = "c:\\";
 			}
 			dialog.setFilterPath (filterPath);
 		}
 		dialog.setFilterNames (PlotExportPrintUtil.FILE_TYPES);
 		dialog.setFilterExtensions (filterExtensions);
 		filename = dialog.open();
 		if (filename == null)
 			return;
 		try {
 			PlotExportPrintUtil.saveGraph(filename, PlotExportPrintUtil.FILE_TYPES[dialog.getFilterIndex()], xyGraph.getImage());
 			logger.debug("Plotting saved");
 		} catch (Exception e) {
 			logger.error("Could not save the plotting", e);
 		}
 	}
 
 	@Override
 	public void savePlotting(String filename, String filetype){
 		if (filename == null)
 			return;
 		try {
 			PlotExportPrintUtil.saveGraph(filename, filetype, xyGraph.getImage());
 			logger.debug("Plotting saved");
 		} catch (Exception e) {
 			logger.error("Could not save the plotting", e);
 		}
 	}
 }
