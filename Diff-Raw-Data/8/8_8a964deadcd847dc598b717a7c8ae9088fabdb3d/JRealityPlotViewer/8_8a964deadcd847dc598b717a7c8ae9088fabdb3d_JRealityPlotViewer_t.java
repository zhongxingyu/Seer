 package org.dawnsci.plotting.jreality;
 
 import java.awt.Component;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JApplet;
 import javax.swing.JPanel;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.roi.data.SurfacePlotROI;
 import org.dawb.common.ui.util.DisplayUtils;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.trace.ILineStackTrace;
 import org.dawnsci.plotting.api.trace.ISurfaceTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
 import org.dawnsci.plotting.jreality.compositing.CompositeEntry;
 import org.dawnsci.plotting.jreality.compositing.CompositingControl;
 import org.dawnsci.plotting.jreality.core.AxisMode;
 import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;
 import org.dawnsci.plotting.jreality.data.ColourImageData;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1D;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1DStack;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot2D;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot2DMulti;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot3D;
 import org.dawnsci.plotting.jreality.impl.DataSetScatterPlot2D;
 import org.dawnsci.plotting.jreality.impl.DataSetScatterPlot3D;
 import org.dawnsci.plotting.jreality.impl.HistogramChartPlot1D;
 import org.dawnsci.plotting.jreality.impl.Plot1DAppearance;
 import org.dawnsci.plotting.jreality.impl.Plot1DGraphTable;
 import org.dawnsci.plotting.jreality.impl.PlotException;
 import org.dawnsci.plotting.jreality.impl.SurfPlotStyles;
 import org.dawnsci.plotting.jreality.legend.LegendChangeEvent;
 import org.dawnsci.plotting.jreality.legend.LegendChangeEventListener;
 import org.dawnsci.plotting.jreality.legend.LegendComponent;
 import org.dawnsci.plotting.jreality.legend.LegendTable;
 import org.dawnsci.plotting.jreality.swt.InfoBoxComponent;
 import org.dawnsci.plotting.jreality.tick.TickFormatting;
 import org.dawnsci.plotting.jreality.tool.CameraRotationTool;
 import org.dawnsci.plotting.jreality.tool.ClickWheelZoomTool;
 import org.dawnsci.plotting.jreality.tool.ClickWheelZoomToolWithScrollBar;
 import org.dawnsci.plotting.jreality.tool.PanActionListener;
 import org.dawnsci.plotting.jreality.tool.PanningTool;
 import org.dawnsci.plotting.jreality.tool.SceneDragTool;
 import org.dawnsci.plotting.jreality.util.JOGLChecker;
 import org.dawnsci.plotting.jreality.util.PlotColorUtility;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import de.jreality.math.MatrixBuilder;
 import de.jreality.scene.Camera;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.scene.tool.Tool;
 import de.jreality.tools.ClickWheelCameraZoomTool;
 import de.jreality.ui.viewerapp.AbstractViewerApp;
 import de.jreality.ui.viewerapp.ViewerApp;
 import de.jreality.ui.viewerapp.ViewerAppSwt;
 import de.jreality.util.CameraUtility;
 import de.jreality.util.SceneGraphUtility;
 import de.jreality.util.Secure;
 import de.jreality.util.SystemProperties;
 
 /**
  * A class allowing plotting to be done to a hardware accelerated 
  * component sitting in an SWT Composite.
  * 
  * Code lifted out of DataSetPlotter. Could refactor at some point.
  * 
  * @author fcp94556
  *
  */
 public class JRealityPlotViewer implements SelectionListener, PaintListener, Listener {
 
 	
 	private static Logger logger = LoggerFactory.getLogger(JRealityPlotViewer.class);
 	
 	// TODO FIXME Too many classwide variables here.
 	// Move into separate deligates to deal with separate things
 	// If this many are really needed, I'll eat my hat.
 	protected IDataSet3DCorePlot plotter = null;
 
 	private Plot1DGraphTable graphColourTable;
 	private InfoBoxComponent infoBox = null;
 	private boolean hasJOGL;
 	private boolean exporting = false;
 
 	private boolean showScrollBars = true;
 	private LegendComponent legendTable = null;	
 	private boolean donotProcessEvent = false;
 	private boolean hasJOGLshaders;
 	private PlottingMode currentMode;
 	private boolean useLegend = false;
 	private CompositingControl cmpControl = null;
 	
 	// Jreality
 	private AbstractViewerApp viewerApp;
 	private SceneGraphComponent root;
 	private SceneGraphComponent graph;
 	private SceneGraphComponent coordAxes;
 	private SceneGraphComponent coordXLabels;
 	private SceneGraphComponent coordYLabels;
 	private SceneGraphComponent coordZLabels;
 	private SceneGraphComponent coordGrid = null;
 	private SceneGraphComponent coordTicks = null;
 	private SceneGraphComponent toolNode = null;
 	private SceneGraphComponent cameraNode = null;
 	private SceneGraphComponent bbox = null;
 	private CameraRotationTool cameraRotateTool = null;
 	private SceneDragTool dragTool = null;
 	private ClickWheelZoomTool zoomTool = null;
 	private ClickWheelCameraZoomTool cameraZoomTool = null;
 	private PanningTool panTool = null;
 
 	// SWT
 	private SashForm container;
 	private Composite plotArea;
 	private ScrollBar hBar;
 	private ScrollBar vBar;
 	private Cursor defaultCursor;
 
 	private JRealityPlotActions plotActions;
 	private AbstractPlottingSystem     system;
 	
 	public void init(IPlottingSystem system) {
 		this.system = (AbstractPlottingSystem)system;
 	}
 
 	/**
 	 * Call to create plotting
 	 * @param parent
 	 * @param initialMode may be null
 	 */
 	public void createControl(final Composite parent) {
 		
 		init(parent);
 		createUI(parent);
 		plotActions = new JRealityPlotActions(this, system);
 		plotActions.createActions();
 		system.getActionBars().getToolBarManager().update(true);
 		system.getActionBars().updateActionBars();
 	}
 	
 
 	public Control getControl() {
 		return container;
 	}
 	
 	public ILineStackTrace createStackTrace(final String name) {
 		StackTrace stack = new StackTrace(this, name);
 		// No more than 25 in the stack.
 		stack.setWindow(new LinearROI(new double[]{0.0,0.0}, new double[]{25.0,0.0}));
 		return stack;
 	}
 	
 	protected void addStackTrace(final ILineStackTrace trace) {
 		StackTrace stack = (StackTrace)trace;
 		try {
 			stack.setPlottingSystem((AbstractPlottingSystem)system);
 			graph.setVisible(false);
 			plot(stack.createAxisValues(), stack.getWindow(), PlottingMode.ONED_THREED, stack.getStack());
 
 			// TODO Colour of lines?
 		} finally {
 			graph.setVisible(true);
 			refresh(true);
 		}
 		stack.setActive(true);
 	}
 	
 	/**
 	 * Clear the surface from being plotted.
 	 * 
 	 * The surface will be deactivated after removal but may be added again later.
 	 * 
 	 * @param name
 	 * @return
 	 * @throws Exception 
 	 */
 	public void removeStackTrace(final ILineStackTrace trace) {
 		StackTrace surface = (StackTrace)trace;
 		removeOldSceneNodes();
 		surface.setActive(false);
 	}
 	
 	/**
 	 * Create a surface trace to be plotted in 3D.
 	 * 
 	 * As soon as you call this the plotting system will switch to surface mode.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public ISurfaceTrace createSurfaceTrace(final String name) {
 		SurfaceTrace surface = new SurfaceTrace(this, name);
 		surface.setWindow(new RectangularROI(0, 0, 300, 300, 0));
 		return surface;
 	}
 	
 	private ITrace currentTrace;
 	public void addTrace(ITrace trace) {
 		
 		if (trace instanceof ISurfaceTrace) {
 			system.setPlotType(PlotType.SURFACE);
 		} else if (trace instanceof ILineStackTrace) {
 			system.setPlotType(PlotType.XY_STACKED_3D);
 		}
 		TraceWillPlotEvent evt = new TraceWillPlotEvent(trace, true);
 		system.fireWillPlot(evt);
 		if (!evt.doit) return;
 
 		if (trace instanceof ISurfaceTrace) {
 			addSurfaceTrace((ISurfaceTrace)trace);
 		} else if (trace instanceof ILineStackTrace) {
 			addStackTrace((ILineStackTrace)trace);
 		}
  		currentTrace = trace;
 	}
 
 	/**
 	 * Create a surface trace to be plotted in 3D.
 	 * 
 	 * As soon as you call this the plotting system will switch to surface mode.
 	 * 
 	 * @param name
 	 * @return
 	 * @throws Exception 
 	 */
 	protected void addSurfaceTrace(final ISurfaceTrace trace) {	
 		SurfaceTrace surface = (SurfaceTrace)trace;
 		try {
 			surface.setPlottingSystem((AbstractPlottingSystem)system);
 			graph.setVisible(false);
 			plot(surface.createAxisValues(), getWindow(surface.getWindow()), PlottingMode.SURF2D, surface.getData());
 			plotter.handleColourCast(surface.createImageData(), graph, surface.getData().min().doubleValue(), surface.getData().max().doubleValue());
 		} finally {
 			graph.setVisible(true);
 			refresh(true);
 		}
 		surface.setActive(true);
 	}
 	
 	/**
 	 * Clear the surface from being plotted.
 	 * 
 	 * The surface will be deactivated after removal but may be added again later.
 	 * 
 	 * @param name
 	 * @return
 	 * @throws Exception 
 	 */
 	public void removeSurfaceTrace(final ISurfaceTrace trace) {
 		SurfaceTrace surface = (SurfaceTrace)trace;
 		removeOldSceneNodes();
 		surface.setActive(false);
 	}
 	
 	//////////////////////////////////////////////////////////////////////////////////////////
 	
 	protected SurfacePlotROI getWindow(IROI roi) {
 		if (roi==null) return null;
 		SurfacePlotROI surfRoi = null;
 		if (roi instanceof SurfacePlotROI) {
 			surfRoi = (SurfacePlotROI)roi;
 		} else if (roi instanceof RectangularROI) {
 			RectangularROI rroi = (RectangularROI)roi;
 			final int[] start = rroi.getIntPoint();
 			final int[] lens  = rroi.getIntLengths();
 			surfRoi = new SurfacePlotROI(start[0], start[1], start[0]+lens[0], start[1]+lens[1], 0,0,0,0);
 		} else {
 			throw new RuntimeException("The region '"+roi+"' is not supported for windows! Only rectangles are!");
 		}
 		return surfRoi;
 	}
 	
 	protected void setSurfaceWindow(IROI window) {
 		if (currentMode == PlottingMode.SURF2D) {
 			final SurfacePlotROI surfRoi = getWindow(window);
 			((DataSet3DPlot3D) plotter).setDataWindow(surfRoi);
 			refresh(false);		
 		}
 	}
 
 	public void setStackWindow(IROI window) {
 		if (currentMode == PlottingMode.ONED_THREED) {
 			final StackTrace stack = (StackTrace)currentTrace;
 			try {
 				graph.setVisible(false);
 				plot(stack.createAxisValues(), stack.getWindow(), PlottingMode.ONED_THREED, stack.getStack());
 			} finally {
 				graph.setVisible(true);
 				refresh(false);
 			}
 		}		
 	}
 
 	
 	/**
 	 * 
 	 * @param data, its name is used for the title
 	 * @param axes Axes values for each axis required, there should be three.
 	 * @param window - may be null
 	 * @param mode
 	 * @return true if something plotted
 	 */
 	protected final boolean updatePlot(final List<AxisValues>    axes, 
 						               final IROI             window,
 						               final PlottingMode        mode,
 						               final AbstractDataset...  data) {
		return plot(axes, window, mode, data);
 	}
 
 	
 	/**
 	 * 
 	 * @param data, its name is used for the title
 	 * @param axes Axes values for each axis required, there should be three.
 	 * @param window - may be null
 	 * @param mode
 	 * @return true if something plotted
 	 */
 	private final boolean plot(final List<AxisValues>    axes, 
 			                   final IROI             window,
 			                   final PlottingMode        mode,
 			                   final IDataset...         rawData) {
 
 		
 		final boolean newMode = setMode(mode);
 
 		List<IDataset> data = Arrays.asList(rawData);
 		
 		if (window!=null && window instanceof LinearROI && data.size()>1) {
 			final int x1 = window.getIntPoint()[0];
 			final int x2 = (int)Math.round(((LinearROI)window).getEndPoint()[0]);
 			try {
 				data = data.subList(x1, x2);
 			} catch (Throwable ignored) {
 				// Leave data alone.
 			}
 		}
 
 		final AxisValues xAxis = axes.get(0);
 		final AxisValues yAxis = axes.get(1);
 		final AxisValues zAxis = axes.get(2);
 
 		setAxisModes((xAxis != null && xAxis.isData() ? AxisMode.CUSTOM : AxisMode.LINEAR),
 				     (yAxis != null && yAxis.isData() ? AxisMode.CUSTOM : AxisMode.LINEAR),
 				     (zAxis != null && zAxis.isData() ? AxisMode.CUSTOM : AxisMode.LINEAR));
 
 		if (xAxis.isData()) plotter.setXAxisValues(xAxis, 1);
 		if (yAxis.isData()) plotter.setYAxisValues(yAxis);
 		if (zAxis.isData()) plotter.setZAxisValues(zAxis);
 
 		setXTickLabelFormat(TickFormatting.roundAndChopMode);
 		setYTickLabelFormat(TickFormatting.roundAndChopMode);
 
 		try {
 
 			update(newMode, data);
 			plotter.setXAxisLabel(getName(xAxis.getName(), "X-Axis"));
 			plotter.setYAxisLabel(getName(yAxis.getName(), "Y-Axis"));
 			plotter.setZAxisLabel(getName(zAxis.getName(), "Z-Axis"));
 			
 			setTickGridLines(xcoord, ycoord, zcoord);
 
 			if (window instanceof SurfacePlotROI) {
 			    ((DataSet3DPlot3D) plotter).setDataWindow((SurfacePlotROI)window);
 			}
 			setTitle(data.get(0).getName());
 
 		} catch (Throwable e) {
 			throw new RuntimeException(e);
 		}
 
 		return true;
 	}
 	
 	private String getName(String name, String defaultName) {
 		if (name!=null && !"".equals(name)) return name;
 		return defaultName;
 	}
 
 	@Override
 	public void paintControl(PaintEvent e) {
 		viewerApp.getCurrentViewer().render();
 	}
 	
 	private void update(boolean newMode, List<IDataset> sets) throws Exception {
 		
     	checkAndAddLegend(sets);		
 		sanityCheckDataSets(sets);
 		
 		if (newMode) {
 			graph = plotter.buildGraph(sets, graph);
 		} else {
 			plotter.updateGraph(sets);
 		}
 		
 		if (currentMode == PlottingMode.SURF2D     || 
 			currentMode == PlottingMode.SCATTER3D  ||
 			currentMode == PlottingMode.ONED_THREED) {
 			root.removeChild(bbox);
 			bbox = plotter.buildBoundingBox();
 			root.addChild(bbox);
 		}
 	}
 	
 	private void checkAndAddLegend(List<? extends IDataset> dataSets) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.SCATTER2D || currentMode==PlottingMode.ONED_THREED) {
 			if (dataSets != null && dataSets.size() > graphColourTable.getLegendSize()) {
 				logger.info("# graphs > # of entries in the legend will auto add entries");
 				for (int i = graphColourTable.getLegendSize(); i < dataSets.size(); i++) {
 					
 					final String name = getName(dataSets.get(i).getName(), "");
 					graphColourTable.addEntryOnLegend(new Plot1DAppearance(PlotColorUtility.getDefaultColour(i),
 							                                               PlotColorUtility.getDefaultStyle(i), 
 							                                               name));
 				}
 			}
 		}
 	}
 	
 	private void setXTickLabelFormat(TickFormatting newFormat) {
 		if (plotter != null) plotter.setXAxisLabelMode(newFormat);
 	}
 	private void setYTickLabelFormat(TickFormatting newFormat) {
 		if (plotter != null) plotter.setYAxisLabelMode(newFormat);
 	}
 	@SuppressWarnings("unused")
 	private void setZTickLabelFormat(TickFormatting newFormat) {
 		if (plotter != null) plotter.setZAxisLabelMode(newFormat);
 	}
 
 	private void init(Composite parent) {
 		graphColourTable = new Plot1DGraphTable();
 		Secure.setProperty(SystemProperties.AUTO_RENDER, "false");
 		root = SceneGraphUtility.createFullSceneGraphComponent("world");
 		graph = SceneGraphUtility.createFullSceneGraphComponent("graph");
 		graph.setOwner(root);
 		coordAxes = SceneGraphUtility.createFullSceneGraphComponent("axis");
 		coordXLabels = SceneGraphUtility.createFullSceneGraphComponent("xLabels");
 		coordYLabels = SceneGraphUtility.createFullSceneGraphComponent("yLabels");
 		coordZLabels = SceneGraphUtility.createFullSceneGraphComponent("zLabels");
 		root.addChild(coordAxes);
 		root.addChild(coordXLabels);
 		root.addChild(coordYLabels);
 		root.addChild(coordZLabels);
 		root.addChild(graph);
 		// check if JOGL is available
 		hasJOGL = JOGLChecker.isHardwareEnabled(parent);
 		
 	}
 	
 
 	private static final String ERROR_MESG_NO_SHADERS = "System does not support OpenGL shaders falling back to compatibily mode. Some advanced features might not work";
 
 	private Composite createUI(Composite parent) {
 		
 		container = new SashForm(parent, SWT.NONE|SWT.VERTICAL);
 		if (parent.getLayout() instanceof GridLayout) {
 			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		}
 		container.addPaintListener(this);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		// Margins make the graph look bad when put as the
 		// main item in a view which is the most common usage.
 		gridLayout.marginBottom = 0;
 		gridLayout.marginTop = 0;
 		gridLayout.horizontalSpacing = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 		gridLayout.marginHeight = 0;
 		container.setLayout(gridLayout);
 		if (hasJOGL)
 			plotArea = new Composite(container, SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL);
 		else
 			plotArea = new Composite(container, SWT.DOUBLE_BUFFERED | SWT.EMBEDDED | SWT.V_SCROLL | SWT.H_SCROLL);
 		hBar = plotArea.getHorizontalBar();
 		hBar.addSelectionListener(this);
 		vBar = plotArea.getVerticalBar();
 		vBar.addSelectionListener(this);
 
 		// Linux GTK hack to keep scroll wheel events from changing
 		// the vertical scrollbar position this interferes with
 		// the MouseWheel zooming
 		if (SWT.getPlatform().equals("gtk")) {
 			plotArea.addListener(SWT.MouseWheel, new Listener() {
 
 				@Override
 				public void handleEvent(Event event) {
 					donotProcessEvent = true;
 				}
 			});
 		}
 
 		hBar.setVisible(false);
 		vBar.setVisible(false);
 		defaultCursor = plotArea.getCursor();
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		plotArea.setLayoutData(gridData);
 		if (hasJOGL) {
 			plotArea.setLayout(new FillLayout());
 			plotArea.addListener(SWT.Resize, this);
 			viewerApp = new ViewerAppSwt(root, plotArea);
 			hasJOGLshaders = JOGLChecker.isShadingSupported(viewerApp);
 			if (!hasJOGLshaders)
 				logger.warn(ERROR_MESG_NO_SHADERS);
 		} else {
 			final JPanel viewerPanel = new JPanel();
 			viewerApp = new ViewerApp(root, true);
 			BoxLayout layout = new BoxLayout(viewerPanel, BoxLayout.Y_AXIS);
 			viewerPanel.setLayout(layout);
 			java.awt.Component comp = ((ViewerApp) viewerApp).getContent();
 			viewerPanel.add(comp);
 			comp.addComponentListener(new ComponentAdapter() {
 				@Override
 				public void componentResized(ComponentEvent arg0) {
 					if (plotter != null) {
 						Component comp = ((ViewerApp) viewerApp).getViewingComponent();
 						plotter.notifyComponentResize(comp.getWidth(), comp.getHeight());
 					}
 				}
 			});
 			java.awt.Frame frame = SWT_AWT.new_Frame(plotArea);
 			JApplet applet = new JApplet();
 			frame.add(applet);
 			applet.add(viewerPanel);
 		}
 		viewerApp.setBackgroundColor(java.awt.Color.white);
 		if (useLegend) buildLegendTable();
 		removeInitialTools();
 		return container;
 	}
 	
 	private void buildLegendTable() {
 		if (legendTable == null || legendTable.isDisposed()) {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.heightHint = 75;
 			legendTable = new LegendTable(container, SWT.DOUBLE_BUFFERED);
 			legendTable.setLayoutData(gridData);
 		}
 		container.setWeights(new int[] {90, 10});		
 		legendTable.addLegendChangeEventListener(new LegendChangeEventListener() {
 			@SuppressWarnings("unused")
 			@Override
 			public void legendDeleted(LegendChangeEvent evt) {
 				if (currentMode == PlottingMode.ONED) {
 					int index = evt.getEntryNr();
 					// TODO... We do not usually use HardwarePlotting for 1D
 					// use DataSetPlotter if this is needed.
 					
 				}
 			}
 
 			@Override
 			public void legendUpdated(LegendChangeEvent evt) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 
 	/**
 	 * Force the render to refresh. Thread safe.
 	 * 
 	 * @param async
 	 */
 	public void refresh(final boolean async) {
 		DisplayUtils.runInDisplayThread(false, null, new Runnable() {
 			public void run() {
 				if (!exporting) {
 					if (viewerApp != null) {
 						if (async)
 							viewerApp.getCurrentViewer().renderAsync();
 						else
 							viewerApp.getCurrentViewer().render();
 					}
 				}
 				if ((currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED || currentMode == PlottingMode.SCATTER2D)
 						&& legendTable != null)
 					legendTable.updateTable(graphColourTable);
 			}
 		});
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public synchronized void widgetSelected(SelectionEvent e) {
 		if (!donotProcessEvent) {
 			if (e.getSource().equals(hBar)) {
 				if (currentMode == PlottingMode.TWOD) {
 					double[] matrix = toolNode.getTransformation().getMatrix();
 					double tTransX = matrix[3];
 					double tTransY = matrix[7];
 					double tTransZ = matrix[11];
 					double minValue = -hBar.getMaximum() / 20.0;
 					tTransX = -minValue - hBar.getSelection() / 10.0f;
 					MatrixBuilder.euclidean().translate(tTransX, tTransY, tTransZ).assignTo(toolNode);
 					refresh(false);
 				}
 			} else	if (e.getSource().equals(vBar) && (e.detail != 0 || SWT.getPlatform().equals("gtk"))) {
 				if (currentMode == PlottingMode.TWOD) {
 					double[] matrix = toolNode.getTransformation().getMatrix();
 					double tTransX = matrix[3];
 					double tTransY = matrix[7];
 					double tTransZ = matrix[11];
 					double minValue = -vBar.getMaximum() / 20.0;
 					tTransY = (minValue + vBar.getSelection() / 10.0f)*1.15f;
 					MatrixBuilder.euclidean().translate(tTransX, tTransY, tTransZ).assignTo(toolNode);
 					refresh(false);
 				}
 			} else if (e.data != null) {
 				if (e.data instanceof List<?> && currentMode == PlottingMode.MULTI2D) {
 					List<CompositeEntry> list = (List<CompositeEntry>)e.data;
 					((DataSet3DPlot2DMulti)plotter).updateCompositingSettings(list);
 					refresh(false);
 				}
 			}
 		}
 		donotProcessEvent = false;
 	}
 
 
 
 	@Override
 	public void handleEvent(Event event) {
 		if (plotter != null) {
 			Rectangle bounds = plotArea.getBounds();
 			plotter.notifyComponentResize(bounds.width, bounds.height);
 		}
 	}
 
 	private void removeInitialTools() {
 		List<SceneGraphComponent> children = viewerApp.getSceneRoot().getChildComponents();
 		List<Tool> rootTools = viewerApp.getSceneRoot().getTools();
 		for (Tool t : rootTools) {
 			if (t instanceof de.jreality.tools.ClickWheelCameraZoomTool)
 				cameraZoomTool = (ClickWheelCameraZoomTool) t;
 		}
 		// remove the automated added rotation tool
 
 		Tool rotateTool = null;
 		Tool dragingTool = null;
 		for (SceneGraphComponent child : children) {
 			List<Tool> tools = child.getTools();
 			for (Tool t : tools) {
 				if (t instanceof de.jreality.tools.RotateTool) {
 					cameraNode = child;
 					rotateTool = t;
 				}
 				if (t instanceof de.jreality.tools.DraggingTool) {
 					toolNode = child;
 					dragingTool = t;
 				}
 			}
 		}
 		if (toolNode != null && dragingTool != null) {
 			toolNode.removeTool(dragingTool);
 			cameraNode.removeTool(rotateTool);
 			panTool = new PanningTool(toolNode);
 			panTool.addPanActionListener(new PanActionListener() {
 				
 				@Override
 				public synchronized void panPerformed(final double xTrans, final double yTrans) {
 					hBar.getDisplay().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							double xtrans = xTrans;
 							double ytrans = yTrans;
 							if (hBar.isVisible()) {
 								double minValue = -hBar.getMaximum() / 20.0;
 								xtrans = Math.max(xtrans, minValue - 0.25);
 								xtrans = Math.min(xtrans, -minValue + 0.25);
 								hBar.setSelection(hBar.getMaximum() - (int) ((xtrans - minValue) * 10));
 							}
 							if (vBar.isVisible()) {
 								double minValue = -vBar.getMaximum() / 20.0;
 								ytrans = Math.max(ytrans, minValue - 1.5);
 								ytrans = Math.min(ytrans, -minValue + 1.5);
 								vBar.setSelection((int) ((ytrans - minValue) * 10));
 							}
 							MatrixBuilder.euclidean().translate(xtrans, ytrans, 0.0).assignTo(toolNode);
 							refresh(false);
 						}
 					});
 				}
 			});
 			cameraRotateTool = new CameraRotationTool();
 			dragTool = new SceneDragTool();
 			toolNode.addTool(panTool);
 		}
 		zoomTool = new ClickWheelZoomToolWithScrollBar(root, 
 													   toolNode, 
 													   (showScrollBars ? hBar : null), 
 													   (showScrollBars ? vBar : null));
 
 	}
 
 	
 	public void setUseLegend(final boolean useLeg) {
 		this.useLegend = useLeg;
 		if (useLegend) {
 			if (legendTable == null) buildLegendTable();
 			legendTable.setVisible(true);
 			container.setWeights(new int[] {90, 10});		
 			legendTable.updateTable(graphColourTable);
 		} else {
 			if (legendTable != null) {
 				legendTable.setVisible(false);
 				container.setWeights(new int[] {100, 0});		
 			}
 		}
 		if (legendTable != null)  legendTable.getParent().layout();
 	}
 	
 	private void setAxisModes(AxisMode xAxis, AxisMode yAxis, AxisMode zAxis) {
 		plotter.setAxisModes(xAxis, yAxis, zAxis);
 	}
 
 	private String title;
 	public void setTitle(final String titleStr) {
 		this.title = titleStr;
 		if(plotter != null) {
 			plotter.setTitle(titleStr);
 			if (infoBox != null) {
 				infoBox.getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						infoBox.setName(titleStr);
 					}
 				});
 			}
 		}
 	}
 
 	public String getTitle() {
 		return title;
 	}
 	
 	/**
 	 * Set the plotter to a new plotting mode
 	 * 
 	 * @param newPlotMode
 	 *            the new plotting mode
 	 */
 	private boolean setMode(PlottingMode newPlotMode) {
 		
 		if (newPlotMode==currentMode) return false;
 		removeOldSceneNodes();
 		currentMode = newPlotMode;
 		if (hasJOGL) plotArea.setFocus();
 
 		// this might be a bit strange but to make sure
 		// the tool doesn't get added twice first remove
 		// it if it isn't attached it will simply do nothing
 
 		toolNode.removeTool(panTool);
 		toolNode.removeTool(dragTool);
 		cameraNode.removeTool(cameraRotateTool);
 		viewerApp.getSceneRoot().removeTool(zoomTool);
 		viewerApp.getSceneRoot().removeTool(cameraZoomTool);
 
 		
 		switch (currentMode) {
 		case ONED:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSet3DPlot1D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			buildLegendTable();
 			container.layout();
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);			
 			break;
 		case ONED_THREED:
 			if (coordAxes!=null) root.removeChild(coordAxes);
 			plotter = new DataSet3DPlot1DStack(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			buildLegendTable();
 			container.layout();
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);
 			hBar.setVisible(false);
 			vBar.setVisible(false);			
 			break;
 		case SCATTER2D:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSetScatterPlot2D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			buildLegendTable();
 			container.layout();
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case TWOD:
 		{	
 			root.removeChild(coordTicks);
 			plotter = new DataSet3DPlot2D(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			toolNode.addTool(panTool);
 			viewerApp.getSceneRoot().addTool(zoomTool);
 			if (useLegend)
 				buildInfoBox();
 			container.layout();
 			root.getTransformation().addTransformationListener((DataSet3DPlot2D) plotter);
 			setPerspectiveCamera(true,false);			
 			break;
 		}
 		case MULTI2D:
 			root.removeChild(coordTicks);
 			plotter = new DataSet3DPlot2DMulti(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			toolNode.addTool(panTool);
 			viewerApp.getSceneRoot().addTool(zoomTool);
 			buildCompositingControl();
 			container.layout();
 			root.getTransformation().addTransformationListener((DataSet3DPlot2D) plotter);
 			setPerspectiveCamera(true,false);			
 			break;
 		case SURF2D:
 			setUseLegend(false);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSet3DPlot3D(viewerApp, hasJOGL, true);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildCoordAxis(coordAxes);
 			container.layout();
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);	
 			break;
 		case SCATTER3D:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSetScatterPlot3D(viewerApp, hasJOGL, true);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildCoordAxis(coordAxes);
 			container.layout();
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case BARCHART:
 			plotter = new HistogramChartPlot1D(viewerApp, graphColourTable, hasJOGL);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case EMPTY:
 			Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
 			sceneCamera.setPerspective(true);			
 			break;
 		}
 		coordXLabels.setVisible(true);
 		coordYLabels.setVisible(true);	
 		coordZLabels.setVisible(true);
 		coordAxes.setVisible(true);
 		
 		return true;
 	}
 
 	private void buildInfoBox() {
 		if (infoBox == null || infoBox.isDisposed()) {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.heightHint = 55;
 			infoBox = new InfoBoxComponent(container, SWT.DOUBLE_BUFFERED);
 			infoBox.setLayoutData(gridData);
 			container.setWeights(new int[] {90, 10});		
 		}
 	}
 	private void buildCompositingControl() {
 		if (cmpControl == null || cmpControl.isDisposed()) {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.heightHint = 95;
 			cmpControl = new CompositingControl(container, SWT.DOUBLE_BUFFERED);
 			cmpControl.setLayoutData(gridData);		
 			cmpControl.addSelectionListener(this);
 			container.setWeights(new int[] {90, 10});					
 		}
 	}
 
 	private double perspFOV = 56.5;
 	private double orthoFOV = 140.0;
 	/**
 	 * Switch between perspective and orthographic camera
 	 * 
 	 * @param persp
 	 *            should this be a perspective camera (true) otherwise false
 	 */
 	private void setPerspectiveCamera(boolean persp, boolean needToRender) {
 		Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
 		if (sceneCamera.isPerspective())
 			perspFOV = sceneCamera.getFieldOfView();
 		else
 			orthoFOV = sceneCamera.getFieldOfView();
 
 		sceneCamera.setPerspective(persp);
 		if (persp)
 			sceneCamera.setFieldOfView(perspFOV);
 		else
 			sceneCamera.setFieldOfView(orthoFOV);
 		if (needToRender) viewerApp.getCurrentViewer().render();
 	}
 
 	private void removeOldSceneNodes() {
 		if (bbox != null) {
 			root.removeChild(bbox);
 			bbox = null;
 		}
 		if (coordTicks != null) {
 			root.removeChild(coordTicks);
 			coordTicks = null;
 		}
 		if (coordGrid != null) {
 			root.removeChild(coordGrid);
 			coordGrid = null;
 		}
 		if (currentMode == PlottingMode.TWOD) {
 			if (root != null && root.getTransformation() != null)
 				root.getTransformation().removeTransformationListener((DataSet3DPlot2D) plotter);
 		}
 		if (graph != null) {
 			graph.setGeometry(null);
 		}
 		// remove all hanged on children on the graph node
 		if (plotter != null)
 			plotter.cleanUpGraphNode();
 
 		// since we removed all the previous
 		// scene nodes now might be a good time
 		// to call garbage collector to make sure
 
 		// System.gc();
 	}
 
 
 	private boolean checkForNan(IDataset data) {
 		if (data instanceof AbstractDataset)
 			return ((AbstractDataset) data).containsNans();
 
 		for (int i = 0; i < data.getShape()[0]; i++)
 			if (Double.isNaN(data.getDouble(i)))
 				return true;
 		return false;
 	}
 
 	private boolean checkForInf(IDataset data) {
 		if (data instanceof AbstractDataset)
 			return ((AbstractDataset) data).containsInfs();
 
 		for (int i = 0; i < data.getShape()[0]; i++)
 			if (Double.isInfinite(data.getDouble(i)))
 				return true;
 		return false;
 	}
 
 	private static final String ERROR_MESG = "DataSet contains either NaNs or Infs can not plot";
 	private void sanityCheckDataSets(Collection<? extends IDataset> datasets) throws PlotException {
 		Iterator<? extends IDataset> iter = datasets.iterator();
 		while (iter.hasNext()) {
 			IDataset dataset = iter.next();
 
 			if (checkForNan(dataset) || checkForInf(dataset)) {
 				throw new PlotException(ERROR_MESG);
 			}
 		}
 	}
 
 	protected void handleColourCast(ColourImageData imageData, double minValue, double maxValue) {
 		plotter.handleColourCast(imageData, graph, minValue, maxValue);
 		refresh(false);
 	}
 
 
 	public void reset() {
 		if (plotter!=null) {
 			removeOldSceneNodes();
 		}
 	}
 
 
 	public void dispose() {
 		reset();
 		if (plotActions!=null) {
 			plotActions.dispose();
 		}
 		if (legendTable != null) {
 			legendTable.removeAllLegendChangeEventListener();
 			legendTable.dispose();
 			legendTable = null;
 		}
 		if (infoBox != null) {
 			infoBox.dispose();
 			infoBox = null;
 		}
 		if (cmpControl != null) {
 			cmpControl.removeSelectionListener(this);
 			cmpControl.dispose();
 			cmpControl = null;
 		}
 	}
 
 	private boolean xcoord=true, ycoord=true, zcoord=true;
 	public void setTickGridLines(boolean xcoord, boolean ycoord, boolean zcoord) {
 		if (plotter!=null) {
 			this.xcoord = xcoord;
 			this.ycoord = ycoord;
 			this.zcoord = zcoord;
 			plotter.setTickGridLinesActive(xcoord, ycoord, zcoord);
 			refresh(false);	
 		}
 	}
 	
 	public void updatePlottingRole(PlotType type) {
 		// TODO Tells us if surfaces or scatter etc.
 		
 	}
 
 	protected void setBoundingBoxEnabled(boolean checked) {
 		if (checked) {
 			bbox = plotter.buildBoundingBox();
 			root.addChild(bbox);
 		} else {
 			root.removeChild(bbox);
 		}
 	}
 
 	protected void resetView() {
 		MatrixBuilder.euclidean().translate(0.0f, 0.0f, 0.0f).assignTo(toolNode);
 		MatrixBuilder.euclidean().translate(0.0f, 0.0f, 0.0f).assignTo(root);
 		if (currentMode == PlottingMode.ONED_THREED || currentMode == PlottingMode.SURF2D) {
 			Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
 
 			if (sceneCamera.isPerspective()) {
 				sceneCamera.setFieldOfView(56.5);
 			} else {
 				sceneCamera.setFieldOfView(140.0);
 			}
 		}
 		if (vBar != null) {
 			vBar.setVisible(false);
 			vBar.setMaximum(0);
 			vBar.setMinimum(0);
 			vBar.setIncrement(0);
 		}
 		if (hBar != null) {
 			hBar.setVisible(false);
 			hBar.setMaximum(0);
 			hBar.setMinimum(0);
 			hBar.setIncrement(0);
 		}
 		plotArea.redraw();
 		plotter.resetView();
 	}
 
 	protected boolean isExporting() {
 		return exporting;
 	}
 
 	protected void setExporting(boolean exporting) {
 		this.exporting = exporting;
 	}
 
 	protected AbstractViewerApp getViewer() {
 		return viewerApp;
 	}
 
 	protected PlottingMode getPlottingMode() {
 		return currentMode;
 	}
 
 	protected Plot1DGraphTable getGraphTable() {
 		return graphColourTable;
 	}
 
 	/**
 	 * Set the plotting style in 2D surface plotting
 	 * 
 	 * @param newStyle
 	 *            the new selected style
 	 */
 	public void setPlot2DSurfStyle(SurfPlotStyles newStyle) {
 		if (currentMode == PlottingMode.SURF2D) {
 			((DataSet3DPlot3D) plotter).setStyle(newStyle);
 		}
 	}
 }
