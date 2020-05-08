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
 
 package org.dawnsci.plotting.jreality.impl;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.dawnsci.plotting.jreality.core.AxisMode;
 import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;
 import org.dawnsci.plotting.jreality.core.ScaleType;
 import org.dawnsci.plotting.jreality.data.ColourImageData;
 import org.dawnsci.plotting.jreality.overlay.Overlay1DConsumer;
 import org.dawnsci.plotting.jreality.overlay.Overlay1DProvider;
 import org.dawnsci.plotting.jreality.overlay.OverlayType;
 import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;
 import org.dawnsci.plotting.jreality.overlay.enums.LabelOrientation;
 import org.dawnsci.plotting.jreality.overlay.primitives.BoxPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.LabelPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.LinePrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.OverlayPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
 import org.dawnsci.plotting.jreality.tick.Tick;
 import org.dawnsci.plotting.jreality.tick.TickFactory;
 import org.dawnsci.plotting.jreality.tick.TickFormatting;
 import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
 import org.dawnsci.plotting.jreality.tool.AreaSelectListener;
 import org.dawnsci.plotting.jreality.tool.AreaSelectTool;
 import org.dawnsci.plotting.jreality.tool.PlotActionComplexEvent;
 import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
 import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;
 import org.dawnsci.plotting.jreality.tool.PlotActionTool;
 import org.dawnsci.plotting.jreality.tool.PlotRightClickActionTool;
 import org.dawnsci.plotting.jreality.tool.SelectedWindow;
 import org.dawnsci.plotting.jreality.util.ArrayPoolUtility;
 import org.dawnsci.plotting.jreality.util.ScalingUtility;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import de.jreality.geometry.IndexedLineSetFactory;
 import de.jreality.geometry.PointSetFactory;
 import de.jreality.geometry.QuadMeshFactory;
 import de.jreality.math.MatrixBuilder;
 import de.jreality.scene.Appearance;
 import de.jreality.scene.Camera;
 import de.jreality.scene.ClippingPlane;
 import de.jreality.scene.IndexedFaceSet;
 import de.jreality.scene.IndexedLineSet;
 import de.jreality.scene.PointSet;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.shader.CommonAttributes;
 import de.jreality.shader.DefaultGeometryShader;
 import de.jreality.shader.DefaultLineShader;
 import de.jreality.shader.DefaultPointShader;
 import de.jreality.shader.DefaultTextShader;
 import de.jreality.shader.ShaderUtility;
 import de.jreality.ui.viewerapp.AbstractViewerApp;
 import de.jreality.util.CameraUtility;
 import de.jreality.util.SceneGraphUtility;
 
 /**
  *
  */
 public class DataSet3DPlot1D implements IDataSet3DCorePlot, AreaSelectListener, PlotActionEventListener,
 		Overlay1DProvider {
 
 	/**
 	 * Prefix of each scene node name that are used as graph geometry nodes
 	 */
 	public static String GRAPHNAMEPREFIX = "graph.subGraph";
 
 	protected static final Logger logger = LoggerFactory.getLogger(DataSet3DPlot1D.class);
 
 	protected List<IDataset> sets;
 	protected SelectedWindow currentSelectWindow = null;
 	protected SceneGraphComponent xTicks = null;
 	protected SceneGraphComponent yTicks = null;
 	protected AbstractViewerApp app;
 	protected SceneGraphComponent graph = null;
 	protected SceneGraphComponent areaSelection = null;
 	protected SceneGraphComponent background = null;
 	protected SceneGraphComponent axis = null;
 	protected SceneGraphComponent graphGroupNode = null;
 	protected SceneGraphComponent xLabels = null;
 	protected double globalYmin = Float.MAX_VALUE;
 	protected double globalYmax = Float.MIN_VALUE;
 	protected double globalXmin = 0;
 	protected double globalXmax = -1;
 	protected double globalRealXmax = Float.MIN_VALUE;
 	protected double globalRealXmin = Float.MAX_VALUE;
 	protected double graphXmax = -Float.MAX_VALUE;
 	protected double graphXmin = Float.MAX_VALUE;
 	protected double graphYmax = -Float.MAX_VALUE;
 	protected double graphYmin = Float.MAX_VALUE;
 
 	protected int numGraphs = 0;
 	protected List<SceneGraphComponent> subGraphs = Collections.synchronizedList(new LinkedList<SceneGraphComponent>());
 	protected LinkedList<DefaultLineShader> graphLineShaders;
 	protected LinkedList<DefaultGeometryShader> graphShaders;
 	protected Plot1DGraphTable graphColours;
 	protected List<SelectedWindow> undoSelectStack = Collections.synchronizedList(new LinkedList<SelectedWindow>());
 
 	protected AreaSelectTool tool = null;
 	protected TickFactory tickFactory = null;
 	protected double font_scale;
 	protected double font_scale_axis;
 	protected double yOffset = 0;
 
 	protected final static double xInset = 0.25;
 	protected final static double yInset = 0.25;
 	
 	protected boolean zoomToolEnabled = false;
 	protected boolean rangeZoom = true;
 	protected boolean showXTicks = true;
 	protected boolean showYTicks = true;
 	protected boolean isUpdateOperation = false;
 	protected double[] areaSelectStart = new double[2];
 	protected double[] areaSelectEnd = new double[2];
 	protected AxisMode zAxis = AxisMode.LINEAR;
 	protected TickFormatting xLabelMode = TickFormatting.plainMode;
 	protected LinkedList<Tick> xTicksLabels = null;
 	protected LinkedList<Tick> yTicksLabels = null;
 	protected double xTicksUnitSize = 0.0;
 	protected double yTicksUnitSize = 0.0;
 	protected AxisMode xAxis = AxisMode.LINEAR;
 	protected AxisMode yAxis = AxisMode.LINEAR;
 	protected List<AxisValues> xAxes = Collections.synchronizedList(new LinkedList<AxisValues>());
 	protected List<Double> offsets = Collections.synchronizedList(new LinkedList<Double>());
 
 	private LinkedList<Tick> x2ndTicksLabels = null;
 	
 	private AxisValues secondaryXAxes;
 	private AxisValues displaySecondaryXAxes;
 	private DefaultTextShader dtsXTicks;
 	private DefaultTextShader dtsX2Ticks;
 	private DefaultTextShader dtsYTicks;
 	private DefaultTextShader dtsXAxisLabel;
 	private DefaultTextShader dtsX2AxisLabel;
 	private DefaultTextShader dtsYAxisLabel;
 	private DefaultTextShader dtsTitleLabel;
 	private IDataset currentDataSet;
 	private SceneGraphComponent yLabels = null;
 	private SceneGraphComponent xAxisLabel = null;
 	private SceneGraphComponent secondXAxisLabel = null;
 	private SceneGraphComponent yAxisLabel = null;
 	private SceneGraphComponent titleLabel = null;
 	private SceneGraphComponent secondaryAxes = null;
 	private SceneGraphComponent secondaryXAxisTicks = null;
 	private SceneGraphComponent secondaryXAxisLabel = null;
 	private SceneGraphComponent topClip = null;
 	private SceneGraphComponent bottomClip = null;
 	@SuppressWarnings("unused")
 	private ArrayPoolUtility pool = null;
 	private QuadMeshFactory areaSelectFactory = null;
 
 	private String xAxisLabelStr = null;
 	private String x2AxisLabelStr = null;
 
 	private String yAxisLabelStr = null;
 	private String titleLabelStr = null;
 	private PlotActionTool actionTool = null;
 	private PlotRightClickActionTool rightClickActionTool = null;
 
 	private List<PlotActionEventListener> actionListeners = Collections
 			.synchronizedList(new LinkedList<PlotActionEventListener>());
 
 	private Map<Integer, OverlayPrimitive> prim1DMap = Collections
 			.synchronizedMap(new HashMap<Integer, OverlayPrimitive>(1000));
 	private List<Overlay1DConsumer> overlayConsumer = new LinkedList<Overlay1DConsumer>();
 	private TickFormatting yLabelMode = TickFormatting.plainMode;
 	private ScaleType yScaling = ScaleType.LINEAR;
 
 	private Composite plotArea;
 	private Cursor defaultCursor;
 
 	private double xOffset = 0;
 	private boolean actionToolEnabled = false;
 	private boolean rightClickActionToolEnabled = false;
 	private boolean allowZoomMouseGesture = true;
 	private boolean overlayInOperation = false;
 	private int primKeyID = 0;
 	private static final int DEFAULTMAXGRAPHSIZE = 50000;
 	private static int MAXGRAPHSIZE = 50000;
 	private static String ERRORMSG_WRONG_NUM_ENTRIES = "The number of entries in the value list does not match the number of graphs";
 	private static String ERRORMSG_NO_GRAPH = "There is no graph data can be added to ";
 	private static String ERRORMSG_NOT_LINEAR = "Can not add DataPoints to CUSTOM mode with this function";
 	private static String ERRORMSG_NOT_CUSTOM = "Can not add DataPoints to NON CUSTOM mode with this function";
 	private static final String LODSIZE_PROPERTY_STRING = "uk.ac.diamond.analysis.rcp.plotting.LODsize";
 	private static final String USEUNZOOM_MOUSEGESTURE = "uk.ac.diamond.analysis.rcp.plotting.UnzoomGesture";
 	private int crosshairID_x = -1;
 	private int crosshairID_y = -1;
 
 	/**
 	 * Constructor of a 1D plotter
 	 * 
 	 * @param app
 	 *            jReality ViewerApp
 	 * @param plotArea
 	 *            Composite that contains the plot area
 	 * @param defaultCursor
 	 *            Default cursor
 	 * @param colourTable
 	 * @param hasJOGL
 	 */
 
 	public DataSet3DPlot1D(AbstractViewerApp app, Composite plotArea, Cursor defaultCursor,
 			Plot1DGraphTable colourTable, boolean hasJOGL) {
 		String propString = System.getProperty(LODSIZE_PROPERTY_STRING);
 		this.plotArea = plotArea;
 		this.defaultCursor = defaultCursor;
 		if (propString != null) {
 			try {
 				MAXGRAPHSIZE = Integer.parseInt(propString);
 			} catch (NumberFormatException ex) {
 				MAXGRAPHSIZE = DEFAULTMAXGRAPHSIZE;
 			}
 		} else
 			MAXGRAPHSIZE = DEFAULTMAXGRAPHSIZE;
 		propString = System.getProperty(USEUNZOOM_MOUSEGESTURE);
 		if (propString != null) {
 			propString = propString.toUpperCase();
 			if (propString.equals("TRUE"))
 				allowZoomMouseGesture = true;
 			else
 				allowZoomMouseGesture = false;
 		}
 		if (hasJOGL) {
 			font_scale = FONT_SCALE;
 			font_scale_axis = FONT_AXIS_SCALE;
 		} else {
 			font_scale = FONT_SCALE_SOFTWARE;
 			font_scale_axis = FONT_AXIS_SCALE_SOFTWARE;
 		}
 		this.app = app;
 		pool = new ArrayPoolUtility();
 		tickFactory = new TickFactory(TickFormatting.plainMode);
 		tool = new AreaSelectTool();
 		actionTool = new PlotActionTool();
 		actionTool.addPlotActionEventListener(this);
 		rightClickActionTool = new PlotRightClickActionTool();
 		rightClickActionTool.addPlotActionEventListener(this);
 		tool.addAreaSelectListener(this);
 		graphLineShaders = new LinkedList<DefaultLineShader>();
 		graphShaders = new LinkedList<DefaultGeometryShader>();
 		graphColours = colourTable;
 	}
 
 	@Override
 	public SceneGraphComponent buildBoundingBox() {
 		// Nothing to do
 		return null;
 	}
 
 	protected void refresh() {
 		app.getCurrentViewer().render();
 	}
 
 	@Override
 	public SceneGraphComponent buildCoordAxesTicks() {
 		// Nothing to do here
 		return null;
 	}
 
 	private void setScalingSmallFlag(double value) {
 		switch (yScaling) {
 		case LINEAR:
 			ScalingUtility.setSmallLogFlag(false);
 			break;
 		case LN:
 			ScalingUtility.setSmallLogFlag((value < Math.E && value > 0.0));
 			break;
 		case LOG10:
 			ScalingUtility.setSmallLogFlag((value < 10.0 && value > 0.0));
 			break;
 		case LOG2:
 			ScalingUtility.setSmallLogFlag((value < 2.0 && value > 0.0));
 			break;
 		}
 	}
 
 	private IndexedLineSet createSecondaryAxis() {
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();		
 		factory.setVertexCount(4);
 		factory.setEdgeCount(2);
 		double[][] axisCoords = ArrayPoolUtility.getDoubleArray(2);
 		axisCoords[0][0] = 0;
 		axisCoords[0][1] = (MAXY - yInset);
 		axisCoords[0][2] = 0;
 		axisCoords[1][0] = (MAXX - xInset);
 		axisCoords[1][1] = (MAXY - yInset);
 		axisCoords[1][2] = 0;
 		int[][] axisEdges = ArrayPoolUtility.getIntArray(1);
 		axisEdges[0][0] = 0;
 		axisEdges[0][1] = 1;
 		factory.setVertexCoordinates(axisCoords);
 		factory.setEdgeIndices(axisEdges);
 		factory.update();
 		return factory.getIndexedLineSet();
 	}
 	
 	protected IndexedLineSet createAxisGeometry() {
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		factory.setVertexCount(4);
 		factory.setEdgeCount(2);
 		double[][] axisCoords = ArrayPoolUtility.getDoubleArray(4);
 		axisCoords[0][0] = 0;
 		axisCoords[0][1] = 0;
 		axisCoords[0][2] = 0;
 		axisCoords[1][0] = (MAXX - xInset);
 		axisCoords[1][1] = 0;
 		axisCoords[1][2] = 0;
 		axisCoords[2][0] = 0;
 		axisCoords[2][1] = 0;
 		axisCoords[2][2] = 0;
 		axisCoords[3][0] = 0;
 		axisCoords[3][1] = (MAXY - yInset);
 		axisCoords[3][2] = 0;
 		int[][] axisEdges = ArrayPoolUtility.getIntArray(2);
 		axisEdges[0][0] = 0;
 		axisEdges[0][1] = 1;
 		axisEdges[1][0] = 2;
 		axisEdges[1][1] = 3;
 		factory.setVertexCoordinates(axisCoords);
 		factory.setEdgeIndices(axisEdges);
 		factory.update();
 		return factory.getIndexedLineSet();
 	}
 
 	protected void repositionAndScaleAxis(float yRatio) {
 		if (yRatio > 1.0f)
 			MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0).assignTo(axis);
 		else
 			MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 					.assignTo(axis);
 	}
 
 	@Override
 	public SceneGraphComponent buildCoordAxis(SceneGraphComponent axis) {
 		this.axis = axis;
 		xAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("xAxisLabel");
 		yAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("yAxisLabel");
 		titleLabel = SceneGraphUtility.createFullSceneGraphComponent("titleLabel");
 		secondXAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("x2AxisLabel");
 		secondaryAxes = SceneGraphUtility.createFullSceneGraphComponent("SecondXAxis");
 		secondaryXAxisTicks = SceneGraphUtility.createFullSceneGraphComponent("SecondXAxisTicks");
 		secondaryXAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("SecondaryAxisLabels");
 		axis.addChild(xAxisLabel);
 		axis.addChild(secondXAxisLabel);
 		axis.addChild(yAxisLabel);
 		axis.addChild(titleLabel);
 		axis.addChild(secondaryAxes);
 		Appearance graphAppearance = new Appearance();
 		axis.setAppearance(graphAppearance);
 		secondaryAxes.setAppearance(graphAppearance);
 		axis.setGeometry(createAxisGeometry());
 		secondaryAxes.setGeometry(createSecondaryAxis());
 		secondaryAxes.addChild(secondaryXAxisTicks);
 		secondaryAxes.addChild(secondaryXAxisLabel);
 		// build secondaryAxesTicks appearance
 		{
 			Appearance tickAppearance = new Appearance();
 			secondaryXAxisTicks.setAppearance(tickAppearance);
 			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(tickAppearance, true);
 			tickAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 			DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 
 			dls.setLineWidth(1.0);
 			dls.setLineStipple(false);
 			dls.setDiffuseColor(java.awt.Color.lightGray);
 			dgs.setShowFaces(false);
 			dgs.setShowLines(true);
 			dgs.setShowPoints(false);		
 		}
 		// build secondaryAxesLabels appearance
 		{
 			Appearance labelAppearance = new Appearance();
 			secondaryXAxisLabel.setAppearance(labelAppearance);
 			DefaultGeometryShader dgsLabels = ShaderUtility.createDefaultGeometryShader(labelAppearance, true);
 			labelAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 			labelAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 			labelAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 			DefaultPointShader dps = (DefaultPointShader) dgsLabels.createPointShader("default");
 			dgsLabels.setShowFaces(false);
 			dgsLabels.setShowLines(false);
 			dgsLabels.setShowPoints(true);
 			dps.setPointSize(1.0);
 			dps.setDiffuseColor(java.awt.Color.white);
 			dtsX2Ticks = (DefaultTextShader) dps.getTextShader();
 			double[] offset = new double[] { 0, 0.0, 0.0 };
 			dtsX2Ticks.setOffset(offset);
 			dtsX2Ticks.setScale(font_scale);
 			dtsX2Ticks.setDiffuseColor(java.awt.Color.lightGray);
 			dtsX2Ticks.setAlignment(javax.swing.SwingConstants.CENTER);
 		}
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 
 		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 		dls.setLineWidth(1.0);
 		dls.setDiffuseColor(java.awt.Color.black);
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);
 		Appearance xaxisLabelApp = new Appearance();
 		Appearance x2axisLabelApp = new Appearance();
 		Appearance yaxisLabelApp = new Appearance();
 		Appearance titleLabelApp = new Appearance();
 		xAxisLabel.setAppearance(xaxisLabelApp);
 		secondXAxisLabel.setAppearance(x2axisLabelApp);
 		yAxisLabel.setAppearance(yaxisLabelApp);
 		titleLabel.setAppearance(titleLabelApp);
 		xaxisLabelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		xaxisLabelApp.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 		xaxisLabelApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		x2axisLabelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		x2axisLabelApp.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 		x2axisLabelApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 
 		yaxisLabelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		yaxisLabelApp.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 		yaxisLabelApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 
 		titleLabelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		titleLabelApp.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 		titleLabelApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		DefaultGeometryShader dgsLabel = ShaderUtility.createDefaultGeometryShader(xaxisLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		DefaultPointShader dps = (DefaultPointShader) dgsLabel.createPointShader("default");
 		dtsXAxisLabel = (DefaultTextShader) dps.getTextShader();
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsXAxisLabel.setDiffuseColor(java.awt.Color.black);
 		dtsXAxisLabel.setTextdirection(0);
 		dtsXAxisLabel.setScale(font_scale_axis);
 		// dtsAxisLabels.setAlignment(javax.swing.SwingConstants.CENTER);
 
 		dgsLabel = ShaderUtility.createDefaultGeometryShader(x2axisLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		dps = (DefaultPointShader) dgsLabel.createPointShader("default");
 		dtsX2AxisLabel = (DefaultTextShader) dps.getTextShader();
 		dtsX2AxisLabel.setDiffuseColor(java.awt.Color.lightGray);
 		dtsX2AxisLabel.setTextdirection(0);
 		dtsX2AxisLabel.setScale(font_scale_axis);
 
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 
 		dgsLabel = ShaderUtility.createDefaultGeometryShader(yaxisLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		dps = (DefaultPointShader) dgsLabel.createPointShader("default");
 		dtsYAxisLabel = (DefaultTextShader) dps.getTextShader();
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsYAxisLabel.setDiffuseColor(java.awt.Color.black);
 		dtsYAxisLabel.setTextdirection(1);
 		dtsYAxisLabel.setScale(font_scale_axis);
 
 		dgsLabel = ShaderUtility.createDefaultGeometryShader(titleLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		dps = (DefaultPointShader) dgsLabel.createPointShader("default");
 
 		dtsTitleLabel = (DefaultTextShader) dps.getTextShader();
 		dtsTitleLabel.setDiffuseColor(java.awt.Color.black);
 		dtsTitleLabel.setAlignment(javax.swing.SwingConstants.CENTER);
 		dtsTitleLabel.setTextdirection(0);
 		dtsTitleLabel.setScale(font_scale_axis);
 
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 
 		java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
 		int width = dim.width;
 		int height = dim.height;
 		if (width == 0)
 			width = FONT_SIZE_PIXELS_WIDTH;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 
 		float yRatio = (float) dim.width / (float) dim.height;
 		repositionAndScaleAxis(yRatio);
 
 		double fontScale = 1.0;
 		if (width < FONT_SIZE_PIXELS_WIDTH) {
 			fontScale = FONT_SIZE_PIXELS_WIDTH / (double) dim.width;
 
 		}
 		if (height < FONT_SIZE_PIXELS_HEIGHT) {
 			fontScale = Math.max(fontScale, (double) FONT_SIZE_PIXELS_HEIGHT / (double) dim.height);
 		}
 		if (fontScale > 1.0) {
 			dtsXAxisLabel.setScale(font_scale_axis * fontScale);
 			dtsYAxisLabel.setScale(font_scale_axis * fontScale);
 			dtsTitleLabel.setScale(font_scale_axis * fontScale);
 		}
 		return axis;
 	}
 
 	private IndexedLineSet createGraphGeometry(IDataset plotSet, double offset) {
 		int graphSize = plotSet.getShape()[0];
 		float factor = 1.0f;
 		if (graphSize > MAXGRAPHSIZE) {
 			factor = (float) graphSize / (float) MAXGRAPHSIZE;
 			graphSize = MAXGRAPHSIZE;
 		}
 		final IndexedLineSetFactory graphFactory = new IndexedLineSetFactory();
 		graphFactory.setVertexCount(graphSize);
 		graphFactory.setEdgeCount(graphSize - 1);
 		final double[] coords = new double[graphSize * 3];
 		setScalingSmallFlag(graphYmin);
 		final double xFactor = (MAXX - xInset) / (globalRealXmax - globalRealXmin);
 		final double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		final double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		final double yFactor = (MAXY - yInset) / (max - min);
 		final double localRealXmin = offset;
 		for (int x = 0; x < graphSize; x++) {
 			coords[x * 3] = (x + Math.max(0, localRealXmin - globalRealXmin)) * xFactor;
 			coords[x * 3 + 1] = (ScalingUtility.valueScaler(plotSet.getDouble((int) (x * factor)), yScaling) - min)
 					* yFactor;
 			coords[x * 3 + 2] = 0.0;
 		}
 		int[][] edges = ArrayPoolUtility.getIntArray(graphSize - 1);
 		for (int i = 0; i < graphSize - 1; i++) {
 			edges[i][0] = i;
 			edges[i][1] = i + 1;
 		}
 		graphFactory.setVertexCoordinates(coords);
 		graphFactory.setEdgeIndices(edges);
 		graphFactory.update();
 		return graphFactory.getIndexedLineSet();
 	}
 
 	protected IndexedLineSet createGraphGeometry(IDataset plotSet, AxisValues xvalues) {
 		int graphSize = plotSet.getShape()[0];
 		double increment = 1.0;
 		if (graphSize > MAXGRAPHSIZE) {
 			increment = (double) MAXGRAPHSIZE / (double) graphSize;
 			graphSize = MAXGRAPHSIZE;
 		}
 		final IndexedLineSetFactory graphFactory = new IndexedLineSetFactory();
 		final double[] coords = new double[graphSize * 3];
 		setScalingSmallFlag(graphYmin);
 		final double xFactor = (MAXX - xInset) / (graphXmax - graphXmin);
 		final double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		final double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		final double yFactor = (MAXY - yInset) / (max - min);
 		for (int x = 0; x < graphSize; x++) {
 			int i = (int) (x/increment);
 			double xValue = xvalues.getValue(i);
 			coords[x * 3] = (xValue - graphXmin) * xFactor;
 			coords[x * 3 + 1] = (ScalingUtility.valueScaler(plotSet.getDouble(i), yScaling) - min)
 					* yFactor;
 			coords[x * 3 + 2] = 0.0;
 			
 		}
 		graphFactory.setVertexCount(graphSize);
 		graphFactory.setEdgeCount(graphSize - 1);
 		int[][] edges = ArrayPoolUtility.getIntArray(graphSize - 1);
 		for (int i = 0; i < graphSize - 1; i++) {
 			edges[i][0] = i;
 			edges[i][1] = i + 1;
 		}
 		graphFactory.setVertexCoordinates(coords);
 		graphFactory.setEdgeIndices(edges);
 		graphFactory.update();
 		return graphFactory.getIndexedLineSet();
 	}
 
 	protected IndexedLineSet createGraphGeometry(IDataset plotSet) {
 		int graphSize = plotSet.getShape()[0];
 		float factor = 1.0f;
 		if (graphSize > MAXGRAPHSIZE) {
 			factor = (float) graphSize / (float) MAXGRAPHSIZE;
 			graphSize = MAXGRAPHSIZE;
 		}
 		final IndexedLineSetFactory graphFactory = new IndexedLineSetFactory();
 		graphFactory.setVertexCount(graphSize);
 		graphFactory.setEdgeCount(graphSize - 1);
 		final double[] coords = new double[graphSize * 3];
 		setScalingSmallFlag(graphYmin);
 		final double xFactor = (MAXX - xInset) / (graphSize - 1);
 		final double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		final double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		final double yFactor = (MAXY - yInset) / (max - min);
 		for (int x = 0; x < graphSize; x++) {
 			coords[x * 3] = x * xFactor;
 			coords[x * 3 + 1] = (ScalingUtility.valueScaler(plotSet.getDouble((int) (x * factor)), yScaling) - min)
 					* yFactor;
 			coords[x * 3 + 2] = 0.0;
 		}
 		int[][] edges = ArrayPoolUtility.getIntArray(graphSize - 1);
 		for (int i = 0; i < graphSize - 1; i++) {
 			edges[i][0] = i;
 			edges[i][1] = i + 1;
 		}
 		graphFactory.setVertexCoordinates(coords);
 		graphFactory.setEdgeIndices(edges);
 		graphFactory.update();
 		return graphFactory.getIndexedLineSet();
 	}
 
 	@Override
 	public void setXAxisLabel(String label) {
 		if (xAxisLabelStr == null || !label.equals(xAxisLabelStr)) {
 			xAxisLabelStr = label;
 			PointSetFactory factory = new PointSetFactory();
 			factory.setVertexCount(1);
 			double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 			String[] edgeLabels = new String[1];
 			edgeLabels[0] = label;
 			coords[0][0] = MAXX * 0.5;
 			coords[0][1] = -0.5;
 			coords[0][2] = 0;
 			factory.setVertexCoordinates(coords);
 			factory.setVertexLabels(edgeLabels);
 			factory.update();
 			PointSet set = factory.getPointSet();
 			if (xAxisLabel!=null && set!=null) xAxisLabel.setGeometry(set);
 		}
 	}
 
 	public void setSecondaryXAxisLabel(String label) {
 		if (x2AxisLabelStr == null || !label.equals(x2AxisLabelStr)) {
 			x2AxisLabelStr = label;
 			PointSetFactory factory = new PointSetFactory();
 			factory.setVertexCount(1);
 			double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 			String[] edgeLabels = new String[1];
 			edgeLabels[0] = label;
 			coords[0][0] = MAXX * 0.5;
 			coords[0][1] = MAXY;
 			coords[0][2] = 0;
 			factory.setVertexCoordinates(coords);
 			factory.setVertexLabels(edgeLabels);
 			factory.update();
 			if (secondaryAxes != null)
 				secondXAxisLabel.setGeometry(factory.getPointSet());
 			else
 				secondXAxisLabel.setGeometry(null);
 		}
 	}
 	
 	@Override
 	public void setYAxisLabel(String label) {
 		if (yAxisLabelStr == null || !label.equals(yAxisLabelStr)) {
 			yAxisLabelStr = label;
 			PointSetFactory factory = new PointSetFactory();
 			factory.setVertexCount(1);
 			double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 			String[] edgeLabels = new String[1];
 			edgeLabels[0] = label;
 			coords[0][0] = -0.75;
 			coords[0][1] = (MAXY-yInset) * 0.5;
 			coords[0][2] = 0;
 			factory.setVertexCoordinates(coords);
 			factory.setVertexLabels(edgeLabels);
 			factory.update();
 			PointSet set = factory.getPointSet();
 			if (yAxisLabel!=null && set!=null) yAxisLabel.setGeometry(set);
 		}
 	}
 
 	@Override
 	public void setZAxisLabel(String label) {
 		// Do nothing
 	}
 
 	private ArrayList<Tick> createLabelTicks(LinkedList<Tick> ticks) {
 		double oldPos = 0;
 		setScalingSmallFlag(graphYmin);
 		final double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		final double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		final double yFactor = (MAXY - yInset) / (max - min);
 		ArrayList<Tick> labelTicks = new ArrayList<Tick>();
 
 		Iterator<Tick> iter = ticks.iterator();
 		while (iter.hasNext()) {
 			Tick currentTick = iter.next();
 			double yValue = ScalingUtility.valueScaler((currentTick.getTickValue()), yScaling);
 			if (labelTicks.size() == 0) {
 				oldPos = (yValue - min) * yFactor;
 				Tick newTick = new Tick();
 				newTick.setTickName(currentTick.getTickName());
 				newTick.setYCoord(oldPos);
 				labelTicks.add(newTick);
 			} else {
 				double curPos = (yValue - min) * yFactor;
 				if (curPos > (oldPos + FONT_SCALE * 64)) {
 					Tick newTick = new Tick();
 					newTick.setTickName(currentTick.getTickName());
 					newTick.setYCoord(curPos);
 					labelTicks.add(newTick);
 					oldPos = curPos;
 				}
 			}
 		}
 		return labelTicks;
 	}
 
 	private PointSet createYLabelsGeometry(LinkedList<Tick> ticks) {
 		PointSetFactory factory = new PointSetFactory();
 
 		// first build the label ticks
 
 		ArrayList<Tick> labelTicks = createLabelTicks(ticks);
 
 		String[] edgeLabels = new String[labelTicks.size()];
 		factory.setVertexCount(labelTicks.size());
 		double[][] coords = ArrayPoolUtility.getDoubleArray(labelTicks.size());
 		Iterator<Tick> iter = labelTicks.iterator();
 		int counter = 0;
 		while (iter.hasNext()) {
 			Tick currentTick = iter.next();
 			coords[counter][0] = -0.7;
 			coords[counter][1] = currentTick.getYCoord();
 			coords[counter][2] = 0.0;
 			edgeLabels[counter] = currentTick.getTickName();
 			counter++;
 		}
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);
 		factory.update();
 		return factory.getPointSet();
 	}
 
 	protected IndexedLineSet createYTicksGeometry() {
 
 		setScalingSmallFlag(graphYmin);
 		final double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		final double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 
 		if (yLabels != null)
 			yLabels.setGeometry(createYLabelsGeometry(yTicksLabels));
 
 		if (showYTicks) {
 			IndexedLineSetFactory factory = new IndexedLineSetFactory();
 			factory.setVertexCount(yTicksLabels.size() * 2);
 			factory.setEdgeCount(yTicksLabels.size());
 			double[][] coords = ArrayPoolUtility.getDoubleArray(yTicksLabels.size() * 2);
 			int[][] edges = ArrayPoolUtility.getIntArray(yTicksLabels.size());
 			final double yFactor = (MAXY - yInset) / (max - min);
 			Iterator<Tick> iter = yTicksLabels.iterator();
 			for (int i = 0, imax = yTicksLabels.size(); i < imax; i++) {
 				coords[i * 2][0] = -0.125;
 				double yValue = iter.next().getTickValue();
 				yValue = ScalingUtility.valueScaler(yValue, yScaling);
 				double yCoord = (yValue - min) * yFactor;
 				coords[i * 2][1] = yCoord;
 				coords[i * 2][2] = -0.0001;
 				coords[i * 2 + 1][0] = MAXX;
 				coords[i * 2 + 1][1] = yCoord;
 				coords[i * 2 + 1][2] = -0.0001;
 				edges[i][0] = i * 2;
 				edges[i][1] = i * 2 + 1;
 			}
 			factory.setVertexCoordinates(coords);
 			factory.setEdgeIndices(edges);
 			factory.update();
 			return factory.getIndexedLineSet();
 		}
 		return null;
 	}
 
 	private PointSet create2ndXLabelsGeometry(LinkedList<Tick> ticks) {
 		PointSetFactory factory = new PointSetFactory();		
 		String[] edgeLabels = new String[ticks.size()];
 		double[][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
 		factory.setVertexCount(ticks.size());
 		double diff = (globalRealXmin - graphXmin);
 		double graphRange = (graphXmax - graphXmin);
 		double dataRange = (globalRealXmax - globalRealXmin);
 		double xStartPoint = (MAXX - xInset) * (diff / graphRange); 
 		double xRangeSize = ((MAXX-xInset) * dataRange) / graphRange;
 		double min = displaySecondaryXAxes.getMinValue();
 		double max = displaySecondaryXAxes.getMaxValue();
 		final double xFactor = xRangeSize / (max - min);
 		for (int i = 0, imax = ticks.size(); i < imax; i++) {
 			Tick currentTick = ticks.get(i);
 			double value = currentTick.getTickValue();
 			coords[i][0] = xStartPoint + (value - min) * xFactor;
 			coords[i][1] = (MAXY-yInset)+0.275;
 			coords[i][2] = 0.0;
 			edgeLabels[i] = currentTick.getTickName();			
 		}		
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);
 		factory.update();
 		return factory.getPointSet();
 	}
 	
 	protected PointSet createXLabelsGeometry(LinkedList<Tick> ticks) {
 		PointSetFactory factory = new PointSetFactory();
 		String[] edgeLabels = new String[ticks.size()];
 		factory.setVertexCount(ticks.size());
 		double[][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
 		final double xFactor = (MAXX - xInset) / (graphXmax - graphXmin);
 		if (xAxis == AxisMode.CUSTOM && xAxes.size() > 0) {
 			for (int i = 0, imax = ticks.size(); i < imax; i++) {
 				double value = xTicksUnitSize * i;
 				Tick currentTick = null;
 				currentTick = ticks.get(i);
 				coords[i][0] = value * xFactor;
 				coords[i][1] = -0.275;
 				coords[i][2] = 0.0;
 				edgeLabels[i] = currentTick.getTickName();
 			}
 		} else {
 			for (int i = 0, imax = ticks.size(); i < imax; i++) {
 				double value = xTicksUnitSize * i;
 				Tick currentTick = ticks.get(i);
 				coords[i][0] = value * xFactor;
 				coords[i][1] = -0.275;
 				coords[i][2] = 0.0;
 				edgeLabels[i] = currentTick.getTickName();
 			}
 		}
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);
 		factory.update();
 		return factory.getPointSet();
 	}
 
 	protected IndexedLineSet createXTicksGeometry() {
 		double min = graphXmin;
 		if (xLabels != null)
 			xLabels.setGeometry(createXLabelsGeometry(xTicksLabels));
 		if (showXTicks) {
 			IndexedLineSetFactory factory = new IndexedLineSetFactory();
 			factory.setVertexCount(xTicksLabels.size() * 2);
 			factory.setEdgeCount(xTicksLabels.size());
 			double[][] coords = ArrayPoolUtility.getDoubleArray(xTicksLabels.size() * 2);
 			int[][] edges = ArrayPoolUtility.getIntArray(xTicksLabels.size());
 			final double xFactor = (MAXX - xInset) / (graphXmax - min);
 			for (int i = 0, imax = xTicksLabels.size(); i < imax; i++) {
 				double value = xTicksUnitSize * i;
 				coords[i * 2][0] = value * xFactor;
 				coords[i * 2][1] = -0.125;
 				coords[i * 2][2] = -0.0001;
 				coords[i * 2 + 1][0] = value * xFactor;
 				coords[i * 2 + 1][1] = (MAXY-yInset);
 				coords[i * 2 + 1][2] = -0.0001;
 				edges[i][0] = i * 2;
 				edges[i][1] = i * 2 + 1;
 			}
 			factory.setVertexCoordinates(coords);
 			factory.setEdgeIndices(edges);
 			factory.update();
 			return factory.getIndexedLineSet();
 		}
 		return null;
 	}
 	
 	private IndexedLineSet create2ndXTicksGeometry() {
 		if (secondaryXAxisLabel != null)
 			secondaryXAxisLabel.setGeometry(create2ndXLabelsGeometry(x2ndTicksLabels));		
 		if (showXTicks) {
 			IndexedLineSetFactory factory = new IndexedLineSetFactory();
 			factory.setVertexCount(x2ndTicksLabels.size() * 2);
 			factory.setEdgeCount(x2ndTicksLabels.size());
 			double[][] coords = ArrayPoolUtility.getDoubleArray(x2ndTicksLabels.size() * 2);
 			int[][] edges = ArrayPoolUtility.getIntArray(x2ndTicksLabels.size());
 			final double xFactor = (MAXX - xInset) / (graphXmax - graphXmin);
 
 			final double xStartPoint = xFactor * (globalRealXmin - graphXmin); 
 			final double min = displaySecondaryXAxes.getMinValue();
 			final double max = displaySecondaryXAxes.getMaxValue();
 			final double xRangeFactor = xFactor * (globalRealXmax - globalRealXmin) / (max - min);
 			final double y = (MAXY-yInset)+0.125;
 			for (int i = 0, imax = x2ndTicksLabels.size(); i < imax; i++) {
 				double value = x2ndTicksLabels.get(i).getTickValue();
 				coords[i * 2][0] = xStartPoint + (value - min) * xRangeFactor;
 				coords[i * 2][1] = 0.0;
 				coords[i * 2][2] = -0.0001;
 				coords[i * 2 + 1][0] = coords[i * 2][0];
 				coords[i * 2 + 1][1] = y;
 				coords[i * 2 + 1][2] = -0.0001;
 				edges[i][0] = i * 2;
 				edges[i][1] = i * 2 + 1;
 			}
 			factory.setVertexCoordinates(coords);
 			factory.setEdgeIndices(edges);
 			factory.update();
 			return factory.getIndexedLineSet();
 		}
 		return null;
 	}
 
 	protected de.jreality.scene.Geometry createAreaSelection() {
 		double[][] coords = ArrayPoolUtility.getDoubleArray(4);
 		coords[0][0] = coords[0][0] = areaSelectStart[0];
 		coords[0][1] = (rangeZoom ? 0.0 : areaSelectEnd[1]);
 		coords[0][2] = 0.0;
 		coords[1][0] = areaSelectEnd[0];
 		coords[1][1] = (rangeZoom ? 0.0 : areaSelectEnd[1]);
 		coords[1][2] = 0.0;
 		coords[2][0] = areaSelectStart[0];
 		coords[2][1] = (rangeZoom ? (MAXY-yInset) : areaSelectStart[1]);
 		coords[2][2] = 0.0;
 		coords[3][0] = areaSelectEnd[0];
 		coords[3][1] = (rangeZoom ? (MAXY-yInset) : areaSelectStart[1]);
 		coords[3][2] = 0.0;
 
 		areaSelectFactory.setVLineCount(2); // important: the v-direction is the
 											// left-most index
 		areaSelectFactory.setULineCount(2); // and the u-direction the
 											// next-left-most index
 		areaSelectFactory.setClosedInUDirection(false);
 		areaSelectFactory.setClosedInVDirection(false);
 		areaSelectFactory.setVertexCoordinates(coords);
 		areaSelectFactory.setGenerateFaceNormals(true);
 		areaSelectFactory.setGenerateEdgesFromFaces(true);
 		areaSelectFactory.update();
 		return areaSelectFactory.getIndexedFaceSet();
 	}
 
 	protected IndexedFaceSet createBackground() {
 		QuadMeshFactory factory = new QuadMeshFactory();
 		double[][][] coords = new double[2][2][3];
 		coords[0][0][0] = -1.5;
 		coords[0][0][1] = (MAXY-yInset) + 1.5;
 		coords[0][0][2] = 0.0;
 		coords[0][1][0] = MAXX + 1.5;
 		coords[0][1][1] = (MAXY-yInset) + 1.5;
 		coords[0][1][2] = 0.0;
 		coords[1][0][0] = -1.5;
 		coords[1][0][1] = -1.5;
 		coords[1][0][2] = 0.0;
 		coords[1][1][0] = MAXX + 1.5;
 		coords[1][1][1] = -1.5;
 		coords[1][1][2] = 0.0;
 		factory.setVLineCount(2); // important: the v-direction is the left-most
 									// index
 		factory.setULineCount(2); // and the u-direction the next-left-most
 									// index
 		factory.setClosedInUDirection(false);
 		factory.setClosedInVDirection(false);
 		factory.setVertexCoordinates(coords);
 		factory.setGenerateFaceNormals(true);
 		factory.update();
 		coords = null;
 		return factory.getIndexedFaceSet();
 	}
 
 	protected void buildAreaSelection() {
 		if (areaSelectFactory == null)
 			areaSelectFactory = new QuadMeshFactory();
 		areaSelection = SceneGraphUtility.createFullSceneGraphComponent("areaSelect");
 		Appearance selectAppearance = new Appearance();
 		areaSelection.setAppearance(selectAppearance);
 		selectAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		selectAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(selectAppearance, true);
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);
 		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 		dls.setLineWidth(2.0);
 		dls.setDiffuseColor(java.awt.Color.gray);
 	}
 
 	protected void buildBackground() {
 		background = SceneGraphUtility.createFullSceneGraphComponent("background");
 		background.setGeometry(createBackground());
 		Appearance backAppearance = new Appearance();
 		background.setAppearance(backAppearance);
 		backAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		backAppearance.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
 		backAppearance.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED, false);
 		backAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY, 1.0);
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(backAppearance, true);
 		dgs.setShowFaces(true);
 		dgs.setShowLines(false);
 		dgs.setShowPoints(false);
 	}
 
 	protected void sanityCheckMinMax() {
 		if (Math.abs(globalYmax - globalYmax) < 1E-10) {
 			globalYmin -= 1E-5;
 			globalYmax += 1E-5;
 		}
 		if (globalYmin == Float.MAX_VALUE && globalYmax == -Float.MAX_VALUE) {
 			globalYmin = 0.0;
 			globalYmax = 0.1;
 		}
 		if (globalRealXmin == Float.MAX_VALUE && globalRealXmax == -Float.MAX_VALUE) {
 			globalRealXmin = 0.0;
 			globalRealXmax = 0.1;
 		}
 		if (globalXmin == Float.MAX_VALUE && globalXmax == -Float.MAX_VALUE) {
 			globalXmin = 0.0;
 			globalXmax = 0.1;
 		}
 	}
 
 	protected void updateWindowWithNewRanges(List<IDataset> datasets, SelectedWindow window) {
 		Iterator<IDataset> iter = datasets.iterator();
 		Iterator<Double> offIter = offsets.iterator();
 		Iterator<AxisValues> axisIter = xAxes.iterator();
 		double ymin = Float.MAX_VALUE;
 		double ymax = -Float.MAX_VALUE;
 		double realXmin = Float.MAX_VALUE;
 		double realXmax = -Float.MAX_VALUE;
 		while (iter.hasNext()) {
 			IDataset set = iter.next();
 			ymin = Math.min(ymin, set.min().doubleValue());
 			ymax = Math.max(ymax, set.max().doubleValue());
 			switch (xAxis) {
 			case LINEAR:
 				realXmin = 0;
 				realXmax = Math.max(realXmax, set.getShape()[0]) - 1;
 				break;
 			case LINEAR_WITH_OFFSET: {
 				double offset = offIter.next();
 				realXmin = Math.min(offset, realXmin);
 				realXmax = Math.max(realXmax, set.getShape()[0] + offset) - 1;
 			}
 				break;
 			case CUSTOM: {
 				AxisValues xAxis = axisIter.next();
 				realXmin = Math.min(realXmin, xAxis.getMinValue());
 				realXmax = Math.max(realXmax, xAxis.getMaxValue());
 			}
 				break;
 			}
 		}
 		window.setStartWindowX(realXmin);
 		window.setEndWindowX(realXmax);
 		window.setStartWindowY(ymin);
 		window.setEndWindowY(ymax);
 	}
 
 	private boolean determineRangeChange(IDataset dataSet, int plotNumber) {
 		boolean returnValue = false;
 		double currentYmin = Float.MAX_VALUE;
 		double currentYmax = -Float.MAX_VALUE;
 		double currentXmin = Float.MAX_VALUE;
 		double currentXmax = -Float.MAX_VALUE;
 		double currentRealXmin = Float.MAX_VALUE;
 		double currentRealXmax = -Float.MAX_VALUE;
 		currentYmin = Math.min(currentYmin, dataSet.min().doubleValue());
 		currentYmax = Math.max(currentYmax, dataSet.max().doubleValue());
 		currentXmax = Math.max(currentXmax, dataSet.getShape()[0]);
 		switch (xAxis) {
 		case LINEAR:
 			currentRealXmin = 0;
 			currentRealXmax = currentXmax - 1;
 			break;
 		case LINEAR_WITH_OFFSET: {
 			double offset = offsets.get(plotNumber);
 			currentRealXmin = Math.min(offset, currentRealXmin);
 			currentRealXmax = Math.max(currentXmax + offset, currentRealXmax) - 1;
 		}
 			break;
 		case CUSTOM: {
 			AxisValues xAxis = xAxes.get(plotNumber);
 			currentRealXmin = Math.min(currentRealXmin, xAxis.getMinValue());
 			currentRealXmax = Math.max(currentRealXmax, xAxis.getMaxValue());
 		}
 			break;
 		}
 		buildTickLists();
 		if (currentYmin < globalYmin) {
 			globalYmin = currentYmin;
 			returnValue = true;
 		}
 		if (currentYmax > globalYmax) {
 			globalYmax = currentYmax;
 			returnValue = true;
 		}
 		if (currentXmin < globalXmin) {
 			globalXmin = currentXmin;
 			returnValue = true;
 		}
 		if (currentXmax > globalXmax) {
 			globalXmax = currentXmax;
 			returnValue = true;
 		}
 		if (currentRealXmin < globalRealXmin) {
 			globalRealXmin = currentRealXmin;
 			returnValue = true;
 		}
 		if (currentRealXmax > globalRealXmax) {
 			globalRealXmax = currentRealXmax;
 			returnValue = true;
 		}
 		return returnValue;
 	}
 
 	protected void buildTickLists() {
 		tickFactory.setTickMode(xLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 		if (height == 0)
 			height = (FONT_SIZE_PIXELS_HEIGHT >> 1);
 		int width = app.getCurrentViewer().getViewingComponentSize().width;
 		// protect against rubbish RCP early lazy initialisation
 
 		if (width == 0)
 			width = FONT_SIZE_PIXELS_WIDTH;
 
 		graphXmax = globalRealXmax;
 		graphXmin = globalRealXmin;
 		if (xAxis == AxisMode.CUSTOM) {
 			tickFactory.setTickMode(xLabelMode);
 			xTicksLabels = tickFactory.generateTicks(width, globalRealXmin, globalRealXmax, (short) 0, true);
 			graphXmin = tickFactory.getLabelMin();
 			graphXmax = tickFactory.getLabelMax();
 		} else {
 			xTicksLabels = tickFactory.generateTicks(width, globalRealXmin, globalRealXmax, (short) 0, false);
 		}
 		xTicksUnitSize = tickFactory.getTickUnit();
 		tickFactory.setTickMode(yLabelMode);
 		yTicksLabels = tickFactory.generateTicks(height, globalYmin, globalYmax, (short) 1, true);
 		yTicksUnitSize = tickFactory.getTickUnit();
 		graphYmin = tickFactory.getLabelMin();
 		graphYmax = tickFactory.getLabelMax();
 		// build secondary axis labels if there is one
 		if (secondaryXAxes != null) {
 			x2ndTicksLabels = tickFactory.generateTicks(width, displaySecondaryXAxes.getMinValue(), 
 					displaySecondaryXAxes.getMaxValue(), (short)0, false);
 			double min = displaySecondaryXAxes.getMinValue();
 			x2ndTicksLabels.get(0).setTickValue(min);
 			int nfrac = (min == 0.0) ? 0 :(int) Math.max(-Math.floor(Math.log10(Math.abs(min))), 3);
 			String formatStr = String.format("%%.%df",nfrac+1);
 			x2ndTicksLabels.get(0).setTickName(String.format(formatStr,min));
 		}
 	}
 
 	protected void determineRanges(List<IDataset> datasets) {
 		Iterator<IDataset> iter = datasets.iterator();
 		Iterator<Double> offIter = offsets.iterator();
 		Iterator<AxisValues> axisIter = xAxes.iterator();
 		globalYmin = Float.MAX_VALUE;
 		globalYmax = -Float.MAX_VALUE;
 		globalXmin = Float.MAX_VALUE;
 		globalXmax = -Float.MAX_VALUE;
 		globalRealXmin = Float.MAX_VALUE;
 		globalRealXmax = -Float.MAX_VALUE;
 		while (iter.hasNext()) {
 			IDataset set = iter.next();
 			globalYmin = Math.min(globalYmin, set.min().doubleValue());
 			globalYmax = Math.max(globalYmax, set.max().doubleValue());
 			globalXmax = Math.max(globalXmax, set.getShape()[0]);
 			switch (xAxis) {
 			case LINEAR:
 				globalRealXmin = 0;
 				globalRealXmax = globalXmax - 1;
 				break;
 			case LINEAR_WITH_OFFSET: {
 				double offset = offIter.next();
 				globalRealXmin = Math.min(offset, globalRealXmin);
 				globalRealXmax = Math.max(globalXmax + offset, globalRealXmax) - 1;
 			}
 				break;
 			case CUSTOM: {
 				AxisValues xAxis = axisIter.next();
 				globalRealXmin = Math.min(globalRealXmin, xAxis.getMinValue());
 				globalRealXmax = Math.max(globalRealXmax, xAxis.getMaxValue());
 			}
 				break;
 			}
 		}
 
 		// check on yAxis is in offset mode if yes it will be added to the
 		// minimum
 		if (yAxis == AxisMode.LINEAR_WITH_OFFSET)
 			globalYmin += yOffset;
 		sanityCheckMinMax();
 		// Now potential overwrite the global min/max depending on what
 		// the tick labels come up with
 		buildTickLists();
 
 	}
 
 	protected void buildClipPlanes(SceneGraphComponent graph) {
 		if (bottomClip == null) {
 			ClippingPlane plane = new ClippingPlane();
 			plane.setLocal(true);
 			bottomClip = SceneGraphUtility.createFullSceneGraphComponent("bottomClipBorder");
 			bottomClip.setGeometry(plane);
 			graph.addChild(bottomClip);
 		}
 		if (topClip == null) {
 			ClippingPlane plane = new ClippingPlane();
 			plane.setLocal(true);
 			topClip = SceneGraphUtility.createFullSceneGraphComponent("topClipBorder");
 			topClip.setGeometry(plane);
 			bottomClip.addChild(topClip);
 		}
 		MatrixBuilder.euclidean().rotateX(0.5 * Math.PI).assignTo(bottomClip);
 		MatrixBuilder.euclidean().rotateX(-0.5 * Math.PI).translate(0.0f, (MAXY-yInset), 0.0f).rotateX(-0.5 * Math.PI)
 				.assignTo(topClip);
 		MatrixBuilder.euclidean().rotateX(0.5 * Math.PI).translate(0.0, -(MAXY-yInset), 0.0).assignTo(graphGroupNode);
 		topClip.addChild(graphGroupNode);
 	}
 
 	protected SceneGraphComponent buildAdditionalGraphNode() {
 		SceneGraphComponent subGraph = SceneGraphUtility.createFullSceneGraphComponent(GRAPHNAMEPREFIX + numGraphs);
 		subGraphs.add(subGraph);
 		Appearance graphAppearance = new Appearance();
 		subGraph.setAppearance(graphAppearance);
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 		graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 		Plot1DAppearance plotApp = graphColours.getLegendEntry(numGraphs);
 		plotApp.updateGraph(dls, dgs);
 		graphLineShaders.add(dls);
 		graphShaders.add(dgs);
 		graphGroupNode.addChild(subGraph);
 		numGraphs++;
 		return subGraph;
 	}
 
 	protected void resizeAndPositionNodes() {
 		java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
 		float yRatio = (float) dim.width / (float) dim.height;
 		if (yRatio > 1.0f) {
 			MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0).assignTo(graph);
 
 			if (xTicks != null)
 				MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(xTicks);
 			if (yTicks != null)
 				MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(yTicks);
 
 		} else {
 			MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 					.assignTo(graph);
 			if (xTicks != null)
 				MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(xTicks);
 			if (yTicks != null)
 				MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(yTicks);
 		}
 	}
 
 	@Override
 	public SceneGraphComponent buildGraph(List<IDataset> datasets, SceneGraphComponent graph) {
 
 		assert datasets.size() > 0;
 		if (graph != null) {
 			graphGroupNode = SceneGraphUtility.createFullSceneGraphComponent("groupNode");
 			this.sets = datasets;
 			this.graph = graph;
 			determineRanges(datasets);
 			Iterator<IDataset> iter = datasets.iterator();
 			Iterator<Double> offsetIter = offsets.iterator();
 			Iterator<AxisValues> axisIter = xAxes.iterator();
 			currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 			numGraphs = 0;
 			
 			AxisValues xAxisValues = null;
 			
 			while (iter.hasNext()) {
 				IDataset currentDataSet = iter.next();
 				SceneGraphComponent subGraph = SceneGraphUtility.createFullSceneGraphComponent(GRAPHNAMEPREFIX
 						+ numGraphs);
 				subGraphs.add(subGraph);
 				switch (xAxis) {
 				case LINEAR:
 					subGraph.setGeometry(createGraphGeometry(currentDataSet));
 					break;
 				case LINEAR_WITH_OFFSET: {
 					double offset = offsetIter.next();
 					subGraph.setGeometry(createGraphGeometry(currentDataSet, offset));
 				}
 					break;
 				case CUSTOM: {
 					xAxisValues = axisIter.hasNext() ? axisIter.next() : xAxisValues;
 					subGraph.setGeometry(createGraphGeometry(currentDataSet, xAxisValues));
 				}
 					break;
 				}
 				Appearance graphAppearance = new Appearance();
 				subGraph.setAppearance(graphAppearance);
 				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 				graphAppearance
 						.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 				graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 				graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 				DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 				Plot1DAppearance plotApp = graphColours.getLegendEntry(numGraphs);
 				plotApp.updateGraph(dls, dgs);
 				graphLineShaders.add(dls);
 				graphShaders.add(dgs);
 				graphGroupNode.addChild(subGraph);
 				numGraphs++;
 			}
 			buildClipPlanes(graph);
 			resizeAndPositionNodes();
 			Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
 			sceneCamera.setFieldOfView(56.5f);
 
 			// now add an invisible background component
 			// that we use for the picking so we can easily get
 			// object space coordinates for our mouse for the AreaSelection Tool
 			// make sure that this node gets removed from the graph node
 			// when DataSet3DPlot1D is no longer the active plotter
 			// and the tool as well
 
 			if (background == null) {
 				buildBackground();
 				graph.addChild(background);
 				graph.addTool(tool);
 				if (actionToolEnabled)
 					graph.addTool(actionTool);
 				if (rightClickActionToolEnabled) {
 					graph.addTool(rightClickActionTool);
 				}
 			}
 
 			if (areaSelection == null) {
 				buildAreaSelection();
 			}
 			
 			if (secondaryXAxes != null) {
 				secondaryAxes.setVisible(true);
 				secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 			} else if (secondaryAxes != null) {
 				secondaryAxes.setVisible(false);
 			}
 			
 			if (xTicks != null)
 				xTicks.setGeometry(createXTicksGeometry());
 			if (yTicks != null)
 				yTicks.setGeometry(createYTicksGeometry());
 
 		}
 		return graph;
 	}
 
 	@Override
 	public void buildXCoordLabeling(SceneGraphComponent xTicks) {
 		if (xTicks != null) {
 			this.xTicks = xTicks;
 			xLabels = SceneGraphUtility.createFullSceneGraphComponent("xLabels");
 			xTicks.addChild(xLabels);
 
 			Appearance tickAppearance = new Appearance();
 			xTicks.setAppearance(tickAppearance);
 			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(tickAppearance, true);
 			tickAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 			DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 
 			dls.setLineWidth(1.0);
 			dls.setLineStipple(true);
 			dls.setDiffuseColor(java.awt.Color.black);
 			dgs.setShowFaces(false);
 			dgs.setShowLines(true);
 			dgs.setShowPoints(false);
 
 			Appearance labelAppearance = new Appearance();
 			xLabels.setAppearance(labelAppearance);
 			DefaultGeometryShader dgsLabels = ShaderUtility.createDefaultGeometryShader(labelAppearance, true);
 			labelAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 			labelAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 			labelAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 			DefaultPointShader dps = (DefaultPointShader) dgsLabels.createPointShader("default");
 			dgsLabels.setShowFaces(false);
 			dgsLabels.setShowLines(false);
 			dgsLabels.setShowPoints(true);
 			dps.setPointSize(1.0);
 			dps.setDiffuseColor(java.awt.Color.white);
 			dtsXTicks = (DefaultTextShader) dps.getTextShader();
 			double[] offset = new double[] { 0, 0.0, 0.0 };
 			dtsXTicks.setOffset(offset);
 			dtsXTicks.setScale(font_scale);
 			dtsXTicks.setDiffuseColor(java.awt.Color.black);
 			dtsXTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 			java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
 			int width = dim.width;
 			int height = dim.height;
 			if (width == 0)
 				width = FONT_SIZE_PIXELS_WIDTH;
 			if (height == 0)
 				height = FONT_SIZE_PIXELS_HEIGHT;
 
 			float yRatio = (float) width / (float) height;
 			if (yRatio > 1.0f)
 				MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(xTicks);
 			else
 				MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(xTicks);
 		}
 	}
 
 	@Override
 	public void buildYCoordLabeling(SceneGraphComponent yTicks) {
 		if (yTicks != null) {
 			this.yTicks = yTicks;
 			yLabels = SceneGraphUtility.createFullSceneGraphComponent("yLabels");
 			yTicks.addChild(yLabels);
 			Appearance tickAppearance = new Appearance();
 			yTicks.setAppearance(tickAppearance);
 			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(tickAppearance, true);
 			tickAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 			DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 			Appearance labelAppearance = new Appearance();
 			yLabels.setAppearance(labelAppearance);
 			DefaultGeometryShader dgsLabels = ShaderUtility.createDefaultGeometryShader(labelAppearance, true);
 			labelAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 			labelAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 			labelAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 			DefaultPointShader dps = (DefaultPointShader) dgsLabels.createPointShader("default");
 			dgsLabels.setShowFaces(false);
 			dgsLabels.setShowLines(false);
 			dgsLabels.setShowPoints(true);
 			dps.setPointSize(1.0);
 			dps.setDiffuseColor(java.awt.Color.WHITE);
 			dtsYTicks = (DefaultTextShader) dps.getTextShader();
 			double[] offset = new double[] { 0.0, 0.0, 0.0 };
 			dtsYTicks.setOffset(offset);
 			dtsYTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 			dtsYTicks.setScale(font_scale);
 			dtsYTicks.setDiffuseColor(java.awt.Color.black);
 			dls.setLineWidth(1.0);
 			dls.setLineStipple(true);
 			dls.setDiffuseColor(java.awt.Color.black);
 			dgs.setShowFaces(false);
 			dgs.setShowLines(true);
 			dgs.setShowPoints(false);
 			java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
 			int width = dim.width;
 			int height = dim.height;
 			if (width == 0)
 				width = FONT_SIZE_PIXELS_WIDTH;
 			if (height == 0)
 				height = FONT_SIZE_PIXELS_HEIGHT;
 
 			float yRatio = (float) width / (float) height;
 
 			if (yRatio > 1.0f)
 				MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(yTicks);
 			else
 				MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(yTicks);
 		}
 	}
 
 	@Override
 	public void buildZCoordLabeling(SceneGraphComponent comp) {
 		// Nothing to do
 	}
 
 	@Override
 	public void handleColourCast(ColourImageData colourTable, SceneGraphComponent graph, double minValue,
 			double maxValue) {
 		// Nothing to do
 
 	}
 
 	@Override
 	public void setScaling(ScaleType newScaling) {
 		yScaling = newScaling;
 		if (graph != null) {
 			switch (xAxis) {
 			case LINEAR:
 				refreshZoomedGraphsLinear((int) graphXmin, (int) graphXmax);
 				break;
 			case LINEAR_WITH_OFFSET:
 				refreshZoomedGraphsOffset(graphXmin, graphXmax);
 				break;
 			case CUSTOM:
 				refreshZoomedGraphsCustom(graphXmin, graphXmax);
 				break;
 			}
 			xTicks.setGeometry(createXTicksGeometry());
 			yTicks.setGeometry(createYTicksGeometry());
 			if (secondaryXAxes != null) {
 				if (secondaryXAxisTicks != null)				
 					secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 			}
 			repositionOverlaysAfterZoom();
 			// refresh();
 		}
 	}
 
 	protected void updateGraphs() {
 		if (graph != null) {
 			// check first if we have enough nodes if not we might have to
 			// add a few more
 			for (int i = subGraphs.size(); i < sets.size(); i++)
 				buildAdditionalGraphNode();
 			Iterator<IDataset> iter = sets.iterator();
 			Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 			Iterator<Double> offsetIter = offsets.iterator();
 			Iterator<AxisValues> axisIter = xAxes.iterator();
 			AxisValues xAxisValues=null;
 			while (iter.hasNext()) {
 				IDataset dataSet = iter.next();
 				SceneGraphComponent currentGraph = graphIter.next();
 				switch (xAxis) {
 				case LINEAR:
 					currentGraph.setGeometry(createGraphGeometry(dataSet));
 					break;
 				case LINEAR_WITH_OFFSET: {
 					double offset = offsetIter.next();
 					currentGraph.setGeometry(createGraphGeometry(dataSet, offset));
 				}
 					break;
 				case CUSTOM: {
 					xAxisValues = axisIter.hasNext() ? axisIter.next() : xAxisValues;
 					currentGraph.setGeometry(createGraphGeometry(dataSet, xAxisValues));
 				}
 					break;
 				}
 			}
 			// now set the geometry to the remaining graph nodes to null
 			while (graphIter.hasNext()) {
 				SceneGraphComponent currentGraph = graphIter.next();
 				currentGraph.setGeometry(null);
 			}
 		}
 	}
 
 	private void updateGraph(IDataset newData, int plotNumber) {
 		if (graph != null) {
 			SceneGraphComponent currentGraph = subGraphs.get(plotNumber);
 			switch (xAxis) {
 			case LINEAR:
 				currentGraph.setGeometry(createGraphGeometry(newData));
 				break;
 			case LINEAR_WITH_OFFSET: {
 				double offset = offsets.get(plotNumber);
 				currentGraph.setGeometry(createGraphGeometry(newData, offset));
 			}
 				break;
 			case CUSTOM: {
 				AxisValues xAxis = xAxes.get(plotNumber);
 				currentGraph.setGeometry(createGraphGeometry(newData, xAxis));
 			}
 				break;
 			}
 		}
 	}
 
 	protected void updateGraphInZoom() {
 		double startPosX = currentSelectWindow.getStartWindowX();
 		double endPosX = currentSelectWindow.getEndWindowX();
 		SelectedWindow bottom = undoSelectStack.get(undoSelectStack.size() - 1);
 		updateWindowWithNewRanges(sets, bottom);
 		switch (xAxis) {
 		case LINEAR:
 			refreshZoomedGraphsLinear((int) startPosX, (int) endPosX);
 			break;
 		case LINEAR_WITH_OFFSET:
 			refreshZoomedGraphsOffset(startPosX, endPosX);
 			break;
 		case CUSTOM:
 			refreshZoomedGraphsCustom(startPosX, endPosX);
 			break;
 		}
 	}
 
 	@Override
 	public void updateGraph(IDataset newData) {
 		currentDataSet = newData;
 		if (!isUpdateOperation || undoSelectStack.size() == 0) {
 			undoSelectStack.clear();
 			determineRanges(sets);
 			currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 			updateGraphs();
 		} else {
 			updateGraphInZoom();
 		}
 		if (xTicks != null)
 			xTicks.setGeometry(createXTicksGeometry());
 		if (yTicks != null)
 			yTicks.setGeometry(createYTicksGeometry());
 		if (secondaryXAxes != null) {
 			if (secondaryXAxisTicks != null)
 				secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 			secondaryAxes.setVisible(true);
 		} else {
 			secondaryAxes.setVisible(false);
 			secondXAxisLabel.setGeometry(null);
 		}	
 	}
 
 	/**
 	 * Update a single graph in a series of graphs
 	 * 
 	 * @param newData
 	 *            new dataset for the graph to update
 	 * @param plotNumber
 	 *            the number of the plot in the graph list
 	 */
 	public void updateAGraph(IDataset newData, int plotNumber) {
 		sets.set(plotNumber, newData);
 		if (!isUpdateOperation || undoSelectStack.size() == 0) {
 			undoSelectStack.clear();
 			boolean needToUpdateTicks = determineRangeChange(newData, plotNumber);
 			currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 			updateGraph(newData, plotNumber);
 
 			if (needToUpdateTicks) {
 				if (xTicks != null)
 					xTicks.setGeometry(createXTicksGeometry());
 				if (yTicks != null)
 					yTicks.setGeometry(createYTicksGeometry());
 			}
 			if (secondaryXAxes != null) {
 				if (secondaryXAxisTicks != null)
 					secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 				secondaryAxes.setVisible(true);
 			} else {
 				secondaryAxes.setVisible(false);
 				secondXAxisLabel.setGeometry(null);
 			}			
 		} else {
 			updateGraphInZoom();
 		}
 	}
 
 	@Override
 	public void updateGraph(List<IDataset> datasets) {
 		sets = datasets;
 		if (!isUpdateOperation || undoSelectStack.size() == 0) {
 			undoSelectStack.clear();
 			determineRanges(sets);
 			currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 			updateGraphs();
 		} else {
 			updateGraphInZoom();
 		}
 		if (xTicks != null)
 			xTicks.setGeometry(createXTicksGeometry());
 		if (yTicks != null)
 			yTicks.setGeometry(createYTicksGeometry());
 		if (secondaryXAxes != null) {
 			if (secondaryXAxisTicks != null)
 				secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 			secondaryAxes.setVisible(true);
 		} else {
 			if (secondaryAxes!=null)    secondaryAxes.setVisible(false);
 			if (secondXAxisLabel!=null) secondXAxisLabel.setGeometry(null);
 		}
 	}
 
 	@Override
 	public void notifyComponentResize(int width, int height) {
 		if (graph != null) {
 			float yRatio = (float) width / (float) height;
 			if (yRatio > 1.0) {
 				MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(graph);
 				if (axis != null)
 					MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 							.assignTo(axis);
 				if (xTicks != null)
 					MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 							.assignTo(xTicks);
 				if (yTicks != null)
 					MatrixBuilder.euclidean().scale(yRatio, 1.0, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 							.assignTo(yTicks);
 			} else {
 				MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 						.assignTo(graph);
 				if (axis != null)
 					MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 							.assignTo(axis);
 				if (xTicks != null)
 					MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 							.assignTo(xTicks);
 				if (yTicks != null)
 					MatrixBuilder.euclidean().scale(1.0, 1.0 / yRatio, 1.0).translate(-MAXX * 0.45, -MAXY * 0.5, 0.0)
 							.assignTo(yTicks);
 			}
 		}
 		double fontScale = 1.0;
 		if (width < FONT_SIZE_PIXELS_WIDTH) {
 			fontScale = FONT_SIZE_PIXELS_WIDTH / (double) width;
 
 		}
 		if (height < FONT_SIZE_PIXELS_HEIGHT) {
 			fontScale = Math.max(fontScale, (double) FONT_SIZE_PIXELS_HEIGHT / (double) height);
 		}
 		buildTickLists();
 		fontScale = Math.max(fontScale, 0.5);
 		if (dtsXTicks != null)
 			dtsXTicks.setScale(font_scale * fontScale);
 		if (dtsX2Ticks != null)
 			dtsX2Ticks.setScale(font_scale * fontScale);
 		if (dtsYTicks != null)
 			dtsYTicks.setScale(font_scale * fontScale);
 		if (dtsXAxisLabel != null) {
 			dtsXAxisLabel.setScale(font_scale_axis * fontScale);
 		}
 		if (dtsX2AxisLabel != null) {
 			dtsX2AxisLabel.setScale(font_scale_axis * fontScale);
 		}
 		if (dtsYAxisLabel != null) {
 			dtsYAxisLabel.setScale(font_scale_axis * fontScale);
 		}
 		if (dtsTitleLabel != null) {
 			dtsTitleLabel.setScale(font_scale_axis * fontScale);
 		}
 		if (xTicks != null) {
 			if (currentDataSet != null)
 				xTicks.setGeometry(createXTicksGeometry());
 		}
 		if (yTicks != null) {
 			if (currentDataSet != null)
 				yTicks.setGeometry(createYTicksGeometry());
 		}
 		if (secondaryXAxes != null) {
 			if (secondaryXAxisTicks != null)
 				secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 		}
 	}
 
 	private void refreshZoomedGraphsLinear(int startPosX, int endPosX) {
 		Iterator<IDataset> iter = sets.iterator();
 		Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 		globalXmin = 0;
 		globalRealXmin = startPosX;
 		globalRealXmax = (startPosX + globalXmax);
 
 		if (startPosX == 0)
 			globalRealXmax -= 1.0;
 		if (rangeZoom) {
 			globalYmin = Float.MAX_VALUE;
 			globalYmax = -Float.MAX_VALUE;
 		}
 		LinkedList<IDataset> tempBuffer = new LinkedList<IDataset>();
 		while (iter.hasNext()) {
 			IDataset currentSet = iter.next();
 			int subSetEndPos = Math.min(currentSet.getShape()[0], endPosX + 1);
 			if (startPosX < currentSet.getShape()[0]) {
 				IDataset zoomedDataSet = currentSet.getSlice(new int[] { startPosX }, new int[] { subSetEndPos },
 						new int[] { 1 });
 				// protection against infinite zoom
 				if (rangeZoom) {
 					if (zoomedDataSet.getSize() != 1) {
 						globalYmin = Math.min(globalYmin, zoomedDataSet.min().doubleValue());
 						globalYmax = Math.max(globalYmax, zoomedDataSet.max().doubleValue());
 					} else {
 						globalYmin = zoomedDataSet.getDouble(0);
 						globalYmax = zoomedDataSet.getDouble(0) + 0.0001;
 					}
 				}
 				tempBuffer.add(zoomedDataSet);
 			} else
 				tempBuffer.add(null);
 		}
 		sanityCheckMinMax();
 		buildTickLists();
 		// now do the actual plotting
 		iter = tempBuffer.iterator();
 		while (iter.hasNext()) {
 			IDataset currentSet = null;
 			SceneGraphComponent currentGraph = graphIter.next();
 			if (iter.hasNext())
 				currentSet = iter.next();
 			if (currentSet != null)
 				currentGraph.setGeometry(createGraphGeometry(currentSet));
 			else
 				currentGraph.setGeometry(null);
 		}
 		tempBuffer.clear();
 	}
 
 	private void refreshZoomedGraphsOffset(double startPosX, double endPosX) {
 		globalRealXmax = Math.round(endPosX);
 		globalRealXmin = Math.round(startPosX);
 		Iterator<IDataset> iter = sets.iterator();
 		Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 		Iterator<Double> offsetIter = offsets.iterator();
 		if (rangeZoom) {
 			globalYmin = Float.MAX_VALUE;
 			globalYmax = -Float.MAX_VALUE;
 		}
 		LinkedList<IDataset> tempBuffer = new LinkedList<IDataset>();
 		LinkedList<Double> tempOffsets = new LinkedList<Double>();
 		while (iter.hasNext()) {
 			IDataset currentSet = iter.next();
 			double offset = offsetIter.next();
 			int startPosInData = Math.max(0, (int) Math.round(startPosX - offset));
 			int endPosInData = Math.max(0, (int) Math.round(endPosX - offset));
 			endPosInData = Math.min(currentSet.getShape()[0], endPosInData + 1);
 			if (endPosInData > 0) {
 				IDataset zoomedDataSet = currentSet.getSlice(new int[] { startPosInData }, new int[] { endPosInData },
 						new int[] { 1 });
 				if (rangeZoom) {
 					// protection against infinite zoom
 					if (zoomedDataSet.getSize() != 1) {
 						globalYmin = Math.min(globalYmin, zoomedDataSet.min().doubleValue());
 						globalYmax = Math.max(globalYmax, zoomedDataSet.max().doubleValue());
 					} else {
 						globalYmin = zoomedDataSet.getDouble(0);
 						globalYmax = zoomedDataSet.getDouble(0) + 0.0001;
 					}
 				}
 				tempBuffer.add(zoomedDataSet);
 				tempOffsets.add(offset);
 			} else {
 				tempBuffer.add(null);
 				tempOffsets.add(null);
 			}
 		}
 		sanityCheckMinMax();
 		buildTickLists();
 		// now do the actual plotting
 		offsetIter = tempOffsets.iterator();
 		iter = tempBuffer.iterator();
 		while (iter.hasNext()) {
 			SceneGraphComponent currentGraph = graphIter.next();
 			IDataset currentSet = null;
 			double offset = 0.0;
 			if (iter.hasNext()) {
 				currentSet = iter.next();
 				offset = offsetIter.next();
 			}
 			if (currentSet != null)
 				currentGraph.setGeometry(createGraphGeometry(currentSet, offset));
 			else
 				currentGraph.setGeometry(null);
 		}
 		tempBuffer.clear();
 		tempOffsets.clear();
 
 	}
 
 	private void refreshZoomSecondaryAxis(double startPosX, double endPosX) {
 		displaySecondaryXAxes = null;
 
 		if (secondaryXAxes.getMaxValue() > startPosX && secondaryXAxes.getMinValue() < endPosX) {
 			int startPosInData = 0;
 			if (rangeZoom)
 				startPosInData = secondaryXAxes.nearestUpEntry(startPosX);
 			else
 				startPosInData = secondaryXAxes.nearestLowEntry(startPosX);
 
 			int endPosInData = secondaryXAxes.nearestUpEntry(endPosX);
 			if (startPosInData == -1)
 				startPosInData = 0;
 			if (endPosInData == -1)
 				endPosInData = secondaryXAxes.size()-1;
 			else
 				endPosInData = Math.min(endPosInData + 1, secondaryXAxes.size());
 
 			if (startPosInData < endPosInData) {
 				displaySecondaryXAxes = secondaryXAxes.subset(startPosInData, endPosInData);
 			}	
 		}
 	}
 	
 	protected void refreshZoomedGraphsCustom(double startPosX, double endPosX) {
 		globalRealXmax = endPosX;
 		globalRealXmin = startPosX;
 		Iterator<IDataset> iter = sets.iterator();
 		Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 		Iterator<AxisValues> axisIter = xAxes.iterator();
 		if (rangeZoom) {
 			globalYmin = Float.MAX_VALUE;
 			globalYmax = -Float.MAX_VALUE;
 		}
 		globalXmin = Float.MAX_VALUE;
 		globalXmax = -Float.MAX_VALUE;
 
 		while (iter.hasNext()) {
 			IDataset currentSet = iter.next();
 			AxisValues xAxis = axisIter.next();
 			if (xAxis.getMaxValue() > globalRealXmin && xAxis.getMinValue() < globalRealXmax) {
 				int startPosInData = 0;
 				if (rangeZoom)
 					startPosInData = xAxis.nearestUpEntry(startPosX);
 				else
 					startPosInData = xAxis.nearestLowEntry(startPosX);
 
 				int endPosInData = xAxis.nearestUpEntry(endPosX);
 				if (startPosInData == -1)
 					startPosInData = 0;
 				if (endPosInData == -1)
 					endPosInData = currentSet.getShape()[0] - 1;
 				globalXmin = Math.min(globalXmin, xAxis.getValue(startPosInData));
 				globalXmax = Math.max(globalXmax, xAxis.getValue(endPosInData));
 			} 
 		}
 
 		double oldXmax = globalRealXmax;
 		double oldXmin = globalRealXmin;
 		globalRealXmax = globalXmax;
 		globalRealXmin = globalXmin;
 
 		iter = sets.iterator();
 		graphIter = subGraphs.iterator();
 		axisIter = xAxes.iterator();
 
 		LinkedList<IDataset> tempBuffer = new LinkedList<IDataset>();
 		LinkedList<AxisValues> tempAxisVals = new LinkedList<AxisValues>();
 		while (iter.hasNext()) {
 			IDataset currentSet = iter.next();
 			AxisValues xAxis = axisIter.next();
 			if (xAxis.getMaxValue() > oldXmin && xAxis.getMinValue() < oldXmax) {
 				int startPosInData = 0;
 				if (rangeZoom)
 					startPosInData = xAxis.nearestUpEntry(startPosX);
 				else
 					startPosInData = xAxis.nearestLowEntry(startPosX);
 				int endPosInData = xAxis.nearestUpEntry(endPosX);
				if (startPosInData > endPosInData) { // ensure reversed axis works
					int t = startPosInData;
					startPosInData = endPosInData;
					endPosInData = t;
				}
 				if (startPosInData == -1)
 					startPosInData = 0;
 				if (endPosInData == -1)
 					endPosInData = currentSet.getShape()[0];
 				else
 					endPosInData = Math.min(endPosInData + 1, currentSet.getShape()[0]);
 				IDataset zoomedDataSet = null;
 				if (startPosInData < endPosInData) {
 					zoomedDataSet = currentSet.getSlice(new int[] { startPosInData }, new int[] { endPosInData },
 							new int[] { 1 });
 				}
 				// protection against infinite zoom
 				if (rangeZoom && zoomedDataSet != null) {
 					if (zoomedDataSet.getSize() != 1) {
 						globalYmin = Math.min(globalYmin, zoomedDataSet.min().doubleValue());
 						globalYmax = Math.max(globalYmax, zoomedDataSet.max().doubleValue());
 					} else {
 						globalYmin = Math.min(globalYmin, zoomedDataSet.getDouble(0));
 						globalYmax = Math.max(globalYmax, zoomedDataSet.getDouble(0) + 0.0001);
 					}
 				}
 				AxisValues subXaxis = null;
 
 				if (startPosInData < endPosInData) {
 					subXaxis = xAxis.subset(startPosInData, endPosInData);
 				}
 
 				tempBuffer.add(zoomedDataSet);
 				tempAxisVals.add(subXaxis);
 			} else {
 				tempBuffer.add(null);
 				tempAxisVals.add(null);
 			}
 		}
 		if (rangeZoom && yAxis == AxisMode.LINEAR_WITH_OFFSET) {
 			globalYmin += yOffset;
 		}
 		buildTickLists();
 		sanityCheckMinMax();
 		// now do the actual plotting
 		axisIter = tempAxisVals.iterator();
 		iter = tempBuffer.iterator();
 		while (graphIter.hasNext()) {
 			SceneGraphComponent currentGraph = graphIter.next();
 			IDataset currentSet = null;
 			AxisValues subXaxis = null;
 			if (iter.hasNext()) {
 				currentSet = iter.next();
 				subXaxis = axisIter.next();
 			} 
 			if (currentSet != null) {
 				currentGraph.setGeometry(createGraphGeometry(currentSet, subXaxis));
 			} else
 				currentGraph.setGeometry(null);
 		}
 		tempAxisVals.clear();
 		tempBuffer.clear();
 	}
 
 	private void goToZoomLevel(SelectedWindow currentSelectWindow) {
 		globalXmax = currentSelectWindow.getEndWindowX() - currentSelectWindow.getStartWindowX();
 		globalYmax = currentSelectWindow.getEndWindowY();
 		globalYmin = currentSelectWindow.getStartWindowY();
 		if (secondaryXAxes != null) 
 		{
 			refreshZoomSecondaryAxis(currentSelectWindow.getStartWindowX2(),
 					 currentSelectWindow.getEndWindowX2());
 		}
 		switch (xAxis) {
 		case LINEAR:
 			refreshZoomedGraphsLinear((int) currentSelectWindow.getStartWindowX(),
 					(int) currentSelectWindow.getEndWindowX());
 			break;
 		case LINEAR_WITH_OFFSET:
 			refreshZoomedGraphsOffset(currentSelectWindow.getStartWindowX(), currentSelectWindow.getEndWindowX());
 			break;
 		case CUSTOM:
 			refreshZoomedGraphsCustom(currentSelectWindow.getStartWindowX(), currentSelectWindow.getEndWindowX());
 			break;
 		}		
 		xTicks.setGeometry(createXTicksGeometry());
 		yTicks.setGeometry(createYTicksGeometry());
 		
 		if (secondaryXAxes != null) 
 		{
 			secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 		}
 		repositionOverlaysAfterZoom();
 	}
 
 	/**
 	 * Reset the whole zoom chain to completely no zoom
 	 */
 
 	public void resetZoom() {
 		if (undoSelectStack.size() > 0) {
 			currentSelectWindow = undoSelectStack.get(undoSelectStack.size() - 1);
 			undoSelectStack.clear();
 			goToZoomLevel(currentSelectWindow);
 			refresh();
 		}
 	}
 
 	/**
 	 * Undo a zoom step
 	 */
 	public void undoZoom() {
 		if (undoSelectStack.size() > 0) {
 			currentSelectWindow = undoSelectStack.get(0);
 			undoSelectStack.remove(0);
 			goToZoomLevel(currentSelectWindow);
 			refresh();
 		}
 	}
 
 	@Override
 	public void areaSelectDragged(AreaSelectEvent e) {
 		areaSelectEnd[0] = e.getPosition()[0];
 		areaSelectEnd[1] = e.getPosition()[1];
 
 		if (areaSelectEnd[0] < 0.0)
 			areaSelectEnd[0] = 0.0;
 
 		if (areaSelectEnd[1] < 0.0)
 			areaSelectEnd[1] = 0.0;
 
 		if (areaSelectEnd[0] > MAXX)
 			areaSelectEnd[0] = MAXX;
 
 		if (areaSelectEnd[1] > (MAXY-yInset))
 			areaSelectEnd[1] = (MAXY-yInset);
 
 		if (zoomToolEnabled)
 			areaSelection.setGeometry(createAreaSelection());
 		if (overlayConsumer.size() > 0) {
 			double position[] = { graphXmin + areaSelectEnd[0] * (graphXmax - graphXmin) / (MAXX - xInset),
 					areaSelectEnd[1] * (graphYmax - graphYmin) / (MAXY-yInset) };
 			AreaSelectEvent event = new AreaSelectEvent((AreaSelectTool) e.getSource(), position, (char) 1,
 					e.getPrimitiveID());
 
 			for (Overlay1DConsumer consumer : overlayConsumer) {
 				consumer.areaSelected(event);
 			}
 		}
 		refresh();
 	}
 
 	private void determineRangeZoomYBounds(double startYArea, double endYArea) {
 		setScalingSmallFlag(graphYmin);
 		double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		double yRange = max - min;
 		double yStart = startYArea / ((MAXY-yInset) / yRange) + min;
 		yStart = ScalingUtility.inverseScaler(yStart, yScaling);
 		double yEnd = endYArea / ((MAXY - yInset) / yRange) + min;
 		yEnd = ScalingUtility.inverseScaler(yEnd, yScaling);
 		globalYmin = yStart;
 		globalYmax = yEnd;
 	}
 
 	private void zoomLinear(double startXArea, double endXArea, double startYArea, double endYArea) {
 		int graphSize = (int) globalXmax;
 		int startPosX = (int) (graphSize * startXArea / (MAXX - xInset));
 		int endPosX = (int) (graphSize * endXArea / (MAXX - xInset));
 		currentSelectWindow.setStartWindowY(globalYmin);
 		currentSelectWindow.setEndWindowY(globalYmax);
 		if (!rangeZoom) {
 			determineRangeZoomYBounds(startYArea, endYArea);
 		}
 		globalXmax = endPosX - startPosX;
 		if (undoSelectStack.size() > 0) {
 			int oldStartX = (int) currentSelectWindow.getStartWindowX();
 			int oldEndX = (int) currentSelectWindow.getEndWindowX();
 			startPosX += oldStartX;
 			endPosX = oldEndX - (graphSize - endPosX);
 		}
 		// add current window view into the undo stack and create a new one
 		undoSelectStack.add(0, currentSelectWindow);
 		currentSelectWindow = new SelectedWindow(startPosX, endPosX, globalYmin, globalYmax);
 
 		refreshZoomedGraphsLinear(startPosX, endPosX);
 	}
 
 	private void zoomLinearOffset(double startXArea, double endXArea, double startYArea, double endYArea) {
 		double graphSize = globalRealXmax - globalRealXmin;
 		double startPosX = (graphSize * startXArea / (MAXX - xInset)) + globalRealXmin;
 		double endPosX = (graphSize * endXArea / (MAXX - xInset)) + globalRealXmin;
 		currentSelectWindow.setStartWindowY(globalYmin);
 		currentSelectWindow.setEndWindowY(globalYmax);
 		// add current window view into the undo stack and create a new one
 		if (!rangeZoom) {
 			determineRangeZoomYBounds(startYArea, endYArea);
 		}
 		if (undoSelectStack.size() == 0) {
 			currentSelectWindow.setStartWindowX(globalRealXmin);
 			currentSelectWindow.setEndWindowX(globalRealXmax);
 		}
 		undoSelectStack.add(0, currentSelectWindow);
 		currentSelectWindow = new SelectedWindow(startPosX, endPosX, globalYmin, globalYmax);
 
 		refreshZoomedGraphsOffset(startPosX, endPosX);
 	}
 
 	protected void zoomCustom(double startXArea, double endXArea, double startYArea, double endYArea) {
 		double graphSize = graphXmax - graphXmin;
 		double startPosX = (graphSize * startXArea / (MAXX - xInset)) + graphXmin;
 		double endPosX = (graphSize * endXArea / (MAXX - xInset)) + graphXmin;
 		// add current window view into the undo stack and create a new one
 		currentSelectWindow.setStartWindowY(globalYmin);
 		currentSelectWindow.setEndWindowY(globalYmax);
 		if (!rangeZoom) {
 			determineRangeZoomYBounds(startYArea, endYArea);
 		}
 		if (undoSelectStack.size() == 0) {
 			currentSelectWindow.setStartWindowX(graphXmin);
 			currentSelectWindow.setEndWindowX(graphXmax);
 		}
 		undoSelectStack.add(0, currentSelectWindow);
 		currentSelectWindow = new SelectedWindow(startPosX, endPosX, globalYmin, globalYmax);
 
 		refreshZoomedGraphsCustom(startPosX, endPosX);
 	}
 
 	private void zoomSecondaryAxis(double startXArea, double endXArea) {
 		if (undoSelectStack.size() == 0) {
 			currentSelectWindow.setStartWindowX2(displaySecondaryXAxes.getMinValue());
 			currentSelectWindow.setEndWindowX2(displaySecondaryXAxes.getMaxValue());
 		}		
 		double diff = (globalRealXmin - graphXmin);
 		double graphRange = (graphXmax - graphXmin);
 		double dataRange = (globalRealXmax - globalRealXmin);
 		double xStartPoint = (MAXX - xInset) * (diff / graphRange);
 		double xRangeSize = ((MAXX-xInset) * dataRange) / graphRange;
 		double min = displaySecondaryXAxes.getMinValue();
 		double max = displaySecondaryXAxes.getMaxValue();
 		double xRange = max - min;
 	    double xPos = startXArea - xStartPoint;
 	    double xPos1 = endXArea - xStartPoint;
 	    if (xPos < 0.0) xPos = 0.0;
 	    if (xPos1 < 0.0) xPos1 = 0.0;
 		double startPosX = xPos * xRange / xRangeSize + min;
 		double endPosX = xPos1 * xRange / xRangeSize + min;
 		refreshZoomSecondaryAxis(startPosX,endPosX);
 	//	double startPosX = (graphRange * startXArea / (MAXX - xInset)) + graphXmin;
 
 	}
 	
 	private void repositionOverlaysAfterZoom() {
 		overlayInOperation = true;
 		Iterator<Integer> iter = prim1DMap.keySet().iterator();
 
 		while (iter.hasNext()) {
 			OverlayPrimitive prim = prim1DMap.get(iter.next());
 			setScalingSmallFlag(graphYmin);
 			double ymin = ScalingUtility.valueScaler(graphYmin, yScaling);
 			double ymax = ScalingUtility.valueScaler(graphYmax, yScaling);
 
 			if (prim instanceof BoxPrimitive) {
 				BoxPrimitive box = (BoxPrimitive) prim;
 				double[][] coords = box.getDataPoints();
 
 				double xPos = (MAXX - xInset) * (coords[0][0] - graphXmin) / (graphXmax - graphXmin);
 
 				if (xPos < 0.0)
 					xPos = 0.0;
 				if (xPos > (MAXX - xInset))
 					xPos = (MAXX - xInset);
 
 				double yPos = (MAXY-yInset) * (ScalingUtility.valueScaler(coords[0][1], yScaling) - ymin) / (ymax - ymin);
 
 				if (yPos < 0.0)
 					yPos = 0.0;
 				if (yPos > (MAXY - yInset))
 					yPos = (MAXY - yInset);
 
 				double x1Pos = (MAXX - xInset) * (coords[1][0] - graphXmin) / (graphXmax - graphXmin);
 
 				if (x1Pos < 0.0)
 					x1Pos = 0.0;
 				if (x1Pos > (MAXX - xInset))
 					x1Pos = (MAXX - xInset);
 
 				double y1Pos = (MAXY-yInset) * (ScalingUtility.valueScaler(coords[1][1], yScaling) - ymin) / (ymax - ymin);
 
 				if (y1Pos < 0.0)
 					y1Pos = 0.0;
 				if (y1Pos > (MAXY-yInset))
 					y1Pos = (MAXY-yInset);
 				box.setBoxPoints(xPos, yPos, x1Pos, y1Pos);
 			} else if (prim instanceof LinePrimitive) {
 				LinePrimitive line = (LinePrimitive) prim;
 				double[][] coords = line.getDataPoints();
 				double xPos = (MAXX - xInset) * (coords[0][0] - graphXmin) / (graphXmax - graphXmin);
 				if (xPos < 0.0)
 					xPos = 0.0;
 				if (xPos > (MAXX - xInset))
 					xPos = (MAXX - xInset);
 
 				double yPos = (MAXY - yInset) * (ScalingUtility.valueScaler(coords[0][1], yScaling) - ymin) / (ymax - ymin);
 				if (yPos < 0.0)
 					yPos = 0.0;
 				if (yPos > (MAXY - yInset))
 					yPos = (MAXY - yInset);
 
 				double x1Pos = (MAXX - xInset) * (coords[1][0] - graphXmin) / (graphXmax - graphXmin);
 
 				if (x1Pos < 0.0)
 					x1Pos = 0.0;
 				if (x1Pos > (MAXX - xInset))
 					x1Pos = (MAXX - xInset);
 
 				double y1Pos = (MAXY - yInset) * (ScalingUtility.valueScaler(coords[1][1], yScaling) - ymin) / (ymax - ymin);
 
 				if (y1Pos < 0.0)
 					y1Pos = 0.0;
 				if (y1Pos > (MAXY - yInset))
 					y1Pos = (MAXY-yInset);
 				line.setLinePoints(xPos, yPos, x1Pos, y1Pos);
 			} else if (prim instanceof LabelPrimitive) {
 				LabelPrimitive labelPrim = (LabelPrimitive) prim;
 				double[][] coords = labelPrim.getDataPoints();
 				double xPos = (MAXX - xInset) * (coords[0][0] - graphXmin) / (graphXmax - graphXmin);
 				if (xPos < 0.0)
 					xPos = 0.0;
 				if (xPos > (MAXX - xInset))
 					xPos = (MAXX - xInset);
 
 				double yPos = (MAXY-yInset) * (ScalingUtility.valueScaler(coords[0][1], yScaling) - ymin) / (ymax - ymin);
 				if (yPos < 0.0)
 					yPos = 0.0;
 				if (yPos > (MAXY-yInset))
 					yPos = (MAXY-yInset);
 				labelPrim.setLabelPosition(xPos, yPos);
 			}
 			prim.updateNode();
 		}
 		overlayInOperation = false;
 	}
 
 	public void setPlotZoomArea(double startX, double startY, double endX, double endY) {
 
 		// compute back from data coordinate to object coordinates so we
 		// can go through the same code flow as the UI events. I know it is
 		// a bit stupid but so much logic has been put in to get it working
 		// with all the different modes
 
 		double startPosX = 0.0;
 		double endPosX = 0.0;
 
 		switch (xAxis) {
 		case LINEAR:
 			startPosX = startX * (MAXX - xInset) / globalXmax;
 			endPosX = endX * (MAXX - xInset) / globalXmax;
 			break;
 		case LINEAR_WITH_OFFSET: {
 			double graphSize = globalRealXmax - globalRealXmin;
 			startPosX = (startX - globalRealXmin) * (MAXX - xInset) / graphSize;
 			endPosX = (startX - globalRealXmin) * (MAXX - xInset) / graphSize;
 		}
 			break;
 		case CUSTOM: {
 			double graphSize = graphXmax - graphXmin;
 			startPosX = (startX - graphXmin) * (MAXX - xInset) / graphSize;
 			endPosX = (endX - graphXmin) * (MAXX - xInset) / graphSize;
 		}
 			break;
 		}
 
 		if (startPosX < 0.0)
 			startPosX = 0.0;
 		if (startPosX > (MAXX - xInset))
 			startPosX = (MAXX - xInset);
 		if (endPosX < 0.0)
 			endPosX = 0.0;
 		if (endPosX > (MAXX - xInset))
 			endPosX = (MAXX - xInset);
 		setScalingSmallFlag(graphYmin);
 
 		double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		double yRange = max - min;
 		double yStart = (startY - min) * ((MAXY-yInset) / yRange);
 		yStart = ScalingUtility.inverseScaler(yStart, yScaling);
 		double yEnd = (endY - min) * ((MAXY-yInset) / yRange);
 		yEnd = ScalingUtility.inverseScaler(yEnd, yScaling);
 
 		if (yStart < 0.0)
 			yStart = 0.0;
 		if (yStart > (MAXY-yInset))
 			yStart = (MAXY-yInset);
 		if (yEnd < 0.0)
 			yEnd = 0.0;
 		if (yEnd > (MAXY-yInset))
 			yEnd = (MAXY-yInset);
 		boolean oldRangeZoom = rangeZoom;
 		rangeZoom = false;
 		setZoomArea(startPosX, yStart, endPosX, yEnd);
 		rangeZoom = oldRangeZoom;
 	}
 
 	protected void setZoomArea(double startX, double startY, double endX, double endY) {
 		if (graph != null) {
 			if (secondaryXAxes != null) {
 				zoomSecondaryAxis(startX,endX);
 			}			
 			switch (xAxis) {
 			case LINEAR:
 				zoomLinear(startX, endX, startY, endY);
 				break;
 			case LINEAR_WITH_OFFSET:
 				zoomLinearOffset(startX, endX, startY, endY);
 				break;
 			case CUSTOM:
 				zoomCustom(startX, endX, startY, endY);
 				break;
 			}
 			xTicks.setGeometry(createXTicksGeometry());
 			yTicks.setGeometry(createYTicksGeometry());
 			if (secondaryXAxes != null) {
 				if (undoSelectStack.size() != 0) {
 					currentSelectWindow.setStartWindowX2(displaySecondaryXAxes.getMinValue());
 					currentSelectWindow.setEndWindowX2(displaySecondaryXAxes.getMaxValue());
 				}
 				if (secondaryXAxisTicks != null)
 					secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 			}
 			repositionOverlaysAfterZoom();
 		}
 	}
 
 	@Override
 	public void areaSelectEnd(AreaSelectEvent e) {
 
 		if (zoomToolEnabled) {
 			boolean zoomIn = true;
 			if (undoSelectStack.size() > 0) {
 				if (areaSelectStart[0] > areaSelectEnd[0] && areaSelectStart[1] <= areaSelectEnd[1]
 						&& allowZoomMouseGesture)
 					zoomIn = false;
 			}
 			if (zoomIn) {
 				double startXArea = Math.min(areaSelectStart[0], areaSelectEnd[0]);
 				double endXArea = Math.max(areaSelectStart[0], areaSelectEnd[0]);
 				double startYArea = Math.min(areaSelectStart[1], areaSelectEnd[1]);
 				double endYArea = Math.max(areaSelectStart[1], areaSelectEnd[1]);
 				setZoomArea(startXArea, startYArea, endXArea, endYArea);
 				areaSelectStart[0] = areaSelectStart[1] = areaSelectEnd[0] = areaSelectEnd[1] = 0.0;
 				areaSelection.setGeometry(createAreaSelection());
 			} else
 				undoZoom();
 			graph.removeChild(areaSelection);
 		}
 		if (overlayConsumer.size() > 0) {
 			double position[] = { graphXmin + areaSelectEnd[0] * (graphXmax - graphXmin) / (MAXX - xInset),
 					areaSelectEnd[1] * (graphYmax - graphYmin) / (MAXY-yInset) };
 
 			AreaSelectEvent event = new AreaSelectEvent((AreaSelectTool) e.getSource(), position, (char) 2,
 					e.getPrimitiveID());
 
 			for (Overlay1DConsumer consumer : overlayConsumer) {
 				consumer.areaSelected(event);
 			}
 		}
 		refresh();
 	}
 
 	@Override
 	public void areaSelectStart(AreaSelectEvent e) {
 		areaSelectStart[0] = e.getPosition()[0];
 		areaSelectStart[1] = e.getPosition()[1];
 
 		if (areaSelectStart[0] < 0.0)
 			areaSelectStart[0] = 0.0;
 
 		if (areaSelectStart[1] < 0.0)
 			areaSelectStart[1] = 0.0;
 
 		if (areaSelectStart[0] > MAXX)
 			areaSelectStart[0] = MAXX;
 
 		if (areaSelectStart[1] > (MAXY - yInset))
 			areaSelectStart[1] = (MAXY - yInset);
 
 		if (zoomToolEnabled)
 			graph.addChild(areaSelection);
 		if (overlayConsumer.size() > 0) {
 			double position[] = { graphXmin + areaSelectStart[0] * (graphXmax - graphXmin) / (MAXX - xInset),
 					areaSelectStart[1] * (graphYmax - graphYmin) / (MAXY-yInset) };
 
 			AreaSelectEvent event = new AreaSelectEvent((AreaSelectTool) e.getSource(), position, (char) 0,
 					e.getPrimitiveID());
 
 			for (Overlay1DConsumer consumer : overlayConsumer) {
 				consumer.areaSelected(event);
 			}
 		}
 	}
 
 	/**
 	 * Sets the colour of the graph
 	 * 
 	 * @param graphNr
 	 *            number of the graph the colour should be assigned to
 	 */
 
 	public void updateGraphAppearance(int graphNr) {
 		if (graphLineShaders.size() > 0) {
 			DefaultLineShader currentShader = graphLineShaders.get(graphNr);
 			DefaultGeometryShader currentGeomShader = graphShaders.get(graphNr);
 			Plot1DAppearance plotApp = graphColours.getLegendEntry(graphNr);
 			plotApp.updateGraph(currentShader, currentGeomShader);
 		}
 	}
 
 	/**
 	 * Update all the graph appearances at once
 	 */
 	public void updateAllGraphAppearances() {
 		for (int i = 0, imax = graphLineShaders.size(); i < imax; i++) {
 			DefaultLineShader currentShader = graphLineShaders.get(i);
 			DefaultGeometryShader currentGeomShader = graphShaders.get(i);
 			Plot1DAppearance plotApp = graphColours.getLegendEntry(i);
 			plotApp.updateGraph(currentShader, currentGeomShader);
 			SceneGraphComponent graph = subGraphs.get(i);
 			graph.setVisible(plotApp.isVisible());
 		}
 	}
 
 	/**
 	 * Add another graph node
 	 */
 
 	public void addGraphNode() {
 		if (graph != null) {
 			SceneGraphComponent subGraph = SceneGraphUtility.createFullSceneGraphComponent(GRAPHNAMEPREFIX + numGraphs);
 			subGraphs.add(subGraph);
 			Appearance graphAppearance = new Appearance();
 			subGraph.setAppearance(graphAppearance);
 			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 			graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 			graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
 			graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 			DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 			Plot1DAppearance plotApp = graphColours.getLegendEntry(numGraphs);
 			plotApp.updateGraph(dls, dgs);
 			graphLineShaders.add(dls);
 			graphShaders.add(dgs);
 			graphGroupNode.addChild(subGraph);
 			numGraphs++;
 			if (xAxis == AxisMode.LINEAR_WITH_OFFSET)
 				offsets.add(0, xOffset);
 			else if (xAxis == AxisMode.CUSTOM)
 				xAxes.add(xAxes.get(0).clone());
 		}
 	}
 
 	public void removeGraphNode(int elementNr) {
 		if (graph != null) {
 			assert graphLineShaders.size() > 1;
 			if (elementNr < graphLineShaders.size())
 				graphLineShaders.remove(elementNr);
 			if (elementNr < graphShaders.size()) {
 				graphShaders.remove(elementNr);
 			}
 			if (elementNr < subGraphs.size()) {
 				SceneGraphComponent subGraph = subGraphs.get(elementNr);
 				graphGroupNode.removeChild(subGraph);
 				subGraphs.remove(elementNr);
 			}
 			if (elementNr < xAxes.size()) {
 				xAxes.remove(elementNr);
 			}
 			if (elementNr < offsets.size()) {
 				offsets.remove(elementNr);
 			}
 			numGraphs--;
 		}
 	}
 
 	/**
 	 * Remove the last graph node from the list
 	 */
 	public void removeLastGraphNode() {
 		if (graph != null) {
 			assert graphLineShaders.size() > 1;
 			graphLineShaders.remove(graphLineShaders.size() - 1);
 			graphShaders.remove(graphShaders.size() - 1);
 			int lastGraph = subGraphs.size();
 			SceneGraphComponent subGraph = subGraphs.remove(lastGraph-1);
 			graphGroupNode.removeChild(subGraph);
 
 			// make sure that the number of xAxes isn't larger
 			// than the number of subgraphs so this will prune
 			// all unnecessary out
 			while (xAxes.size() > (lastGraph - 1))
 				xAxes.remove(xAxes.size() - 1);
 
 			// make sure that the number of offsets isn't larger
 			// than the number of subgraphs so this will prune
 			// all unnecessary out
 			while (offsets.size() > (lastGraph - 1))
 				offsets.remove(offsets.size() - 1);
 
 			numGraphs--;
 		}
 	}
 
 	@Override
 	public void setXAxisOffset(double offset) {
 		if (xAxis == AxisMode.LINEAR_WITH_OFFSET) {
 			globalXmin = offset;
 			xOffset = offset;
 			if (offsets.size() > 0)
 				offsets.set(0, xOffset);
 			else
 				offsets.add(0, xOffset);
 		}
 	}
 
 	@Override
 	public void setYAxisOffset(double offset) {
 		yOffset = offset;
 	}
 
 	@Override
 	public void setZAxisOffset(double offset) {
 		// Nothing to do
 
 	}
 
 	@Override
 	public void setAxisModes(AxisMode xaxis, AxisMode yaxis, AxisMode zaxis) {
 		xAxis = xaxis;
 		yAxis = yaxis;
 		zAxis = zaxis;
 	}
 
 	/**
 	 * Set the whole range of axes values
 	 * 
 	 * @param axes
 	 * @param numHistory
 	 *            provide the number of elements to be kept
 	 */
 
 	public void setXAxisValues(List<AxisValues> axes, int numHistory) {
 		if (numHistory == 0)
 			xAxes.clear();
 		else
 			for (int i = axes.size() - 1; i >= numHistory; i--)
 				xAxes.remove(i);
 
 		for (int i = 0, imax = axes.size(); i < imax; i++)
 			xAxes.add(axes.get(i));
 	}
 
 	public void set2ndXAxisValues(AxisValues secondAxis) {
 		secondaryXAxes = secondAxis;
 		if (secondAxis != null)
 			displaySecondaryXAxes = secondAxis.clone();
 		else
 			displaySecondaryXAxes = null;
 	}
 	
 	@Override
 	public void setXAxisValues(AxisValues axis, int numOfDataSets) {
 		if (xAxes.size() > 0) {
 			for (int i = 0, imax = Math.min(numOfDataSets, xAxes.size()); i < imax; i++)
 				xAxes.set(i, axis);
 			for (int i = xAxes.size(); i < numOfDataSets; i++)
 				xAxes.add(i, axis);
 		} else
 			for (int i = 0; i < numOfDataSets; i++)
 				xAxes.add(i, axis);
 	}
 
 	/**
 	 * Replace an already existing x-axis value with a new one
 	 * 
 	 * @param axis
 	 *            the new x-axis value
 	 * @param plotNumber
 	 *            the plot number the x-axis value is associated to
 	 */
 	public void replaceXAxisValue(AxisValues axis, int plotNumber) {
 		if (xAxes.size() > plotNumber) {
 			xAxes.set(plotNumber, axis);
 		}
 	}
 
 	@Override
 	public void setYAxisValues(AxisValues axis) {
 		// Nothing to do
 
 	}
 
 	private void notifyAllPlotActionListener(PlotActionEvent newEvent) {
 		Iterator<PlotActionEventListener> iter = actionListeners.iterator();
 		while (iter.hasNext()) {
 			PlotActionEventListener listener = iter.next();
 			listener.plotActionPerformed(newEvent);
 		}
 	}
 
 	private double[] determineDataPos(double xPos, double yPos) {
 
 		double graphXSize = graphXmax - graphXmin;
 		setScalingSmallFlag(graphYmin);
 		double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 		double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 		double graphYSize = max - min;
 
 		double dataPosX = (graphXSize * xPos / (MAXX - xInset)) + graphXmin;
 		
 		double dataPosY = (graphYSize * yPos / (MAXY - yInset)) + min;
 		double[] dataPos = new double[2];
 		if (displaySecondaryXAxes != null) {
 			dataPos = new double[3];
 			double diff = (globalRealXmin - graphXmin);
 			double graphRange = (graphXmax - graphXmin);
 			double dataRange = (globalRealXmax - globalRealXmin);
 			double xStartPoint = (MAXX - xInset) * (diff / graphRange);
 			double xRangeSize = ((MAXX - xInset) * dataRange) / graphRange;
 			double x2min = displaySecondaryXAxes.getMinValue();
 			double x2max = displaySecondaryXAxes.getMaxValue();
 			double xRange = x2max - x2min;
 		    double x2Pos = xPos - xStartPoint;
 			x2Pos = x2Pos * xRange / xRangeSize + x2min;
 			dataPos[2] = x2Pos;
 		}
 		dataPosY = ScalingUtility.inverseScaler(dataPosY, yScaling);
 		dataPos[0] = dataPosX;
 		dataPos[1] = dataPosY;
 		return dataPos;
 	}
 
 	@Override
 	public void plotActionPerformed(PlotActionEvent event) {
 		double x = event.getPosition()[0];
 		double y = event.getPosition()[1];
 		if (x < 0.0)
 			x = 0.0;
 		if (x > MAXX)
 			x = MAXX;
 		if (y < 0.0)
 			y = 0.0;
 		if (y > (MAXY-yInset))
 			y = (MAXY - yInset);
 
 		double[] dataPos = determineDataPos(x, y);
 		drawActionCrosshair(dataPos[0], dataPos[1]);
 		if (event.getSource().equals(rightClickActionTool)) {
 			int graphNr = event.getSelectedGraphNr();
 			IDataset data = null;
 			AxisValues axisVal = null;
 			if (graphNr >= 0 && graphNr < sets.size()) {
 				data = sets.get(graphNr);
 				if (xAxes != null && xAxes.size() > graphNr)
 					axisVal = xAxes.get(graphNr);
 			}
 
 			PlotActionComplexEvent newEvent = new PlotActionComplexEvent(rightClickActionTool, dataPos, data, axisVal,
 					currentSelectWindow);
 			notifyAllPlotActionListener(newEvent);
 		} else {
 			PlotActionEvent newEvent = new PlotActionEvent(actionTool, dataPos);
 			notifyAllPlotActionListener(newEvent);
 		}
 	}
 
 	/**
 	 * Add a PlotActionEventListener to the listener list
 	 * 
 	 * @param listener
 	 *            another PlotActionEventListener
 	 */
 	public void addPlotActionEventListener(PlotActionEventListener listener) {
 		actionListeners.add(listener);
 	}
 
 	/**
 	 * Remove a PlotActionEventListener from the listener list
 	 * 
 	 * @param listener
 	 *            the PlotActionEventListener that should be removed
 	 */
 	public void removePlotActionEventListener(PlotActionEventListener listener) {
 		actionListeners.remove(listener);
 	}
 
 	/**
 	 * Enable/Disable the zoom tool
 	 * 
 	 * @param enabled
 	 *            true if tool should be enabled otherwise false
 	 */
 	public void enableZoomTool(boolean enabled) {
 		zoomToolEnabled = enabled;
 	}
 
 	/**
 	 * Enable/Disable the PlotActionTool
 	 * 
 	 * @param enabled
 	 *            true if tool should be enabled otherwise false
 	 */
 	public void enablePlotActionTool(boolean enabled) {
 		actionToolEnabled = enabled;
 		if (graph != null) {
 			if (enabled) {
 				if (crosshairID_x == -1)
 					crosshairID_x = registerPrimitive(PrimitiveType.LINE);
 				else
 					setPrimitiveVisible(crosshairID_x, true);
 				if (crosshairID_y == -1)
 					crosshairID_y = registerPrimitive(PrimitiveType.LINE);
 				else
 					setPrimitiveVisible(crosshairID_y, true);
 
 				graph.addTool(actionTool);
 
 			} else {
 				setPrimitiveVisible(crosshairID_x, false);
 				setPrimitiveVisible(crosshairID_y, false);
 				graph.removeTool(actionTool);
 				app.getCurrentViewer().render();
 			}
 		}
 	}
 
 	/**
 	 * Enable/Disable the PlotRightClickActionTool
 	 * 
 	 * @param enabled
 	 *            true if tool should be enabled otherwise false
 	 */
 
 	public void enableRightClickActionTool(boolean enabled) {
 		rightClickActionToolEnabled = enabled;
 		if (graph != null) {
 			if (enabled)
 				graph.addTool(rightClickActionTool);
 			else
 				graph.removeTool(rightClickActionTool);
 		}
 	}
 
 	@Override
 	public void setXAxisLabelMode(TickFormatting newFormat) {
 		xLabelMode = newFormat;
 		buildTickLists();
 		if (xTicks != null && numGraphs > 0)
 			xTicks.setGeometry(createXTicksGeometry());
 		if (secondaryXAxes != null) {
 			if (secondaryXAxisTicks != null)
 				secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 		}		
 	}
 
 	@Override
 	public void setYAxisLabelMode(TickFormatting newFormat) {
 		yLabelMode = newFormat;
 		buildTickLists();
 		if (yTicks != null && numGraphs > 0)
 			yTicks.setGeometry(createYTicksGeometry());
 	}
 
 	@Override
 	public void setZAxisLabelMode(TickFormatting newFormat) {
 		// Nothing to do
 	}
 
 	private void cleanUpOverlay() {
 		if (overlayConsumer.size() > 0) {
 
 			for (Overlay1DConsumer consumer : overlayConsumer) {
 				consumer.removePrimitives();
 			}
 			overlayConsumer.clear();
 		}
 
 		overlayInOperation = false;
 		
 		Integer[] primitiveKeyset = prim1DMap.keySet().toArray(new Integer[0]);
 		for (Integer primitiveKey : primitiveKeyset){
 			unregisterPrimitive(primitiveKey);
 		}
 
 		prim1DMap.clear();
 	}
 
 	@Override
 	public void cleanUpGraphNode() {
 		if (crosshairID_x != -1)
 			unregisterPrimitive(crosshairID_x);
 		if (crosshairID_y != -1)
 			unregisterPrimitive(crosshairID_y);
 		cleanUpOverlay();
 		if (graph != null) {
 			graph.removeChild(background);
 			Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 			while (graphIter.hasNext()) {
 				graphGroupNode.removeChild(graphIter.next());
 			}
 			graph.removeChild(graphGroupNode);
 			if (tool != null)
 				graph.removeTool(tool);
 			if (actionTool != null && actionToolEnabled)
 				graph.removeTool(actionTool);
 			if (rightClickActionTool != null && rightClickActionToolEnabled)
 				graph.removeTool(rightClickActionTool);
 			subGraphs.clear();
 		}
 		if (axis != null) {
 			axis.removeChild(xAxisLabel);
 			axis.removeChild(secondXAxisLabel);
 			axis.removeChild(yAxisLabel);
 			axis.removeChild(titleLabel);
 			axis.removeChild(secondaryAxes);
 		}
 		if (xTicks != null)
 			xTicks.removeChild(xLabels);
 		if (yTicks != null)
 			yTicks.removeChild(yLabels);
 	}
 
 	@Override
 	public void setTitle(String title) {
 		if (title != null) {
 			if (titleLabelStr == null || !title.equals(titleLabelStr)) {
 				titleLabelStr = title;
 				PointSetFactory factory = new PointSetFactory();
 				factory.setVertexCount(1);
 				double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 				String[] edgeLabels = new String[1];
 				edgeLabels[0] = title;
 				coords[0][0] = MAXX * 0.5;
 				coords[0][1] = (MAXY - yInset) + 0.1;
 				coords[0][2] = 0;
 				factory.setVertexCoordinates(coords);
 				factory.setVertexLabels(edgeLabels);
 				factory.update();
 				final PointSet set = factory.getPointSet();
 				if (set!=null && titleLabel!=null) titleLabel.setGeometry(set);
 			}
 		}
 	}
 
 	/**
 	 * Set the zoom mode
 	 * 
 	 * @param areaMode
 	 *            if areaMode (true) otherwise regionMode (false)
 	 */
 	public void setZoomMode(boolean areaMode) {
 		rangeZoom = !areaMode;
 	}
 
 	/**
 	 * Register an overlay consumer to this overlay provider
 	 * 
 	 * @param consumer
 	 */
 
 	public void registerOverlay(Overlay1DConsumer consumer) {
 		consumer.registerProvider(this);
 		overlayConsumer.add(consumer);
 	}
 
 	/**
 	 * Unregister an overlay consumer to this overlay provider
 	 * 
 	 * @param consumer
 	 */
 	public void unRegisterOverlay(Overlay1DConsumer consumer) {
 		overlayInOperation = false;
 		consumer.unregisterProvider();
 		overlayConsumer.remove(consumer);
 		final Iterator<Integer> iter = prim1DMap.keySet().iterator();
 		List<Integer> removeList = new ArrayList<Integer>();
 		while (iter.hasNext()) {
 			int primID = iter.next();
 			if (primID != crosshairID_x && primID != crosshairID_y) {
 				OverlayPrimitive primitive = prim1DMap.get(primID);
 				if (primitive != null) {
 					if (graphGroupNode != null) {
 						graphGroupNode.removeChild(primitive.getNode());
 					}
 					removeList.add(primID);
 				}
 			}
 		}
 		for (Integer prim : removeList) {
 			prim1DMap.remove(prim);
 		}
 		refresh();
 	}
 
 	@Override
 	public boolean begin(OverlayType type) {
 		if (!overlayInOperation) {
 			overlayInOperation = true;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean drawBox(int primID, double lux, double luy, double rlx, double rly) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			BoxPrimitive boxPrim = (BoxPrimitive) prim1DMap.get(primID);
 			if (boxPrim != null) {
 				setScalingSmallFlag(graphYmin);
 				double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 				double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 				double xPos = (MAXX - xInset) * (lux - graphXmin) / (graphXmax - graphXmin);
 				double yPos = (MAXY-yInset) * (ScalingUtility.valueScaler(luy, yScaling) - min) / (max - min);
 				double x1Pos = (MAXX - xInset) * (rlx - graphXmin) / (graphXmax - graphXmin);
 				double y1Pos = (MAXY-yInset) * (ScalingUtility.valueScaler(rly, yScaling) - min) / (max - min);
 				boxPrim.setDataPoints(lux, luy, rlx, rly);
 				boxPrim.setBoxPoints(xPos, yPos, x1Pos, y1Pos);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public boolean drawLine(int primID, double sx, double sy, double ex, double ey) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LinePrimitive linePrim = (LinePrimitive) prim1DMap.get(primID);
 			if (linePrim != null) {
 				setScalingSmallFlag(graphYmin);
 				double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 				double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 				double xPos = (MAXX - xInset) * (sx - graphXmin) / (graphXmax - graphXmin);
 				double yPos = (MAXY-yInset) * (ScalingUtility.valueScaler(sy, yScaling) - min) / (max - min);
 				double x1Pos = (MAXX - xInset) * (ex - graphXmin) / (graphXmax - graphXmin);
 				double y1Pos = (MAXY-yInset) * (ScalingUtility.valueScaler(ey, yScaling) - min) / (max - min);
 				linePrim.setDataPoints(sx, sy, ex, ey);
 				linePrim.setLinePoints(xPos, yPos, x1Pos, y1Pos);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public void end(OverlayType type) {
 		overlayInOperation = false;
 		switch (type) {
 		case IMAGE: {
 			logger.warn("Unsupported operation in 1D overlay");
 		}
 			break;
 		case VECTOR2D: {
 			Iterator<Integer> iter = prim1DMap.keySet().iterator();
 			while (iter.hasNext()) {
 				OverlayPrimitive prim = prim1DMap.get(iter.next());
 				prim.updateNode();
 			}
 		}
 			break;
 		case THREED: {
 			logger.warn("Unsupported operation in 1D overlay");
 		}
 			break;
 		}
 		refresh();
 	}
 
 	@SuppressWarnings("incomplete-switch")
 	@Override
 	public int registerPrimitive(PrimitiveType primType) {
 		if (graphGroupNode != null
 				&& (primType == PrimitiveType.LINE || primType == PrimitiveType.BOX || primType == PrimitiveType.LABEL)) {
 			primKeyID++;
 			while (prim1DMap.containsKey(primKeyID)) {
 				primKeyID = (primKeyID + 1) % Integer.MAX_VALUE;
 			}
 			OverlayPrimitive prim = null;
 			SceneGraphComponent comp = SceneGraphUtility.createFullSceneGraphComponent(OVERLAYPREFIX + primKeyID);
 			graphGroupNode.addChild(comp);
 			switch (primType) {
 			case LINE:
 				prim = new LinePrimitive(comp);
 				break;
 			case BOX:
 				prim = new BoxPrimitive(comp);
 				break;
 			case LABEL:
 				prim = new LabelPrimitive(comp);
 				break;
 			}
 
 			prim1DMap.put(primKeyID, prim);
 			return primKeyID;
 		}
 		return -1;
 	}
 
 	@Override
 	public void setColour(int primID, Color colour) {
 		if (overlayInOperation) {
 			OverlayPrimitive prim = prim1DMap.get(primID);
 			if (prim != null)
 				prim.setColour(colour);
 		}
 	}
 
 	@Override
 	public void setOutlineColour(int primID, Color colour) {
 		OverlayPrimitive primitive = prim1DMap.get(primID);
 		if (primitive != null) {
 			primitive.setOutlineColour(colour);
 		}
 	}
 
 	@Override
 	public void setStyle(int primID, VectorOverlayStyles newStyle) {
 		OverlayPrimitive primitive = prim1DMap.get(primID);
 		if (primitive != null) {
 			primitive.setStyle(newStyle);
 		}
 	}
 
 	@Override
 	public boolean setTransparency(int primID, double transparency) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim1DMap.get(primID);
 			if (primitive != null) {
 				primitive.setTransparency(transparency);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public void translatePrimitive(int primID, double tx, double ty) {
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim1DMap.get(primID);
 			if (primitive != null) {
 				double[] translation = new double[2];
 				setScalingSmallFlag(graphYmin);
 				double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 				double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 				translation[0] = (tx / (graphXmax - graphXmin)) * (MAXX - xInset);
 				translation[1] = (ScalingUtility.valueScaler(ty, yScaling) / (max - min)) * (MAXY-yInset);
 				primitive.translate(translation);
 			}
 		}
 	}
 
 	@Override
 	public void unregisterPrimitive(int primID) {
 		OverlayPrimitive primitive = prim1DMap.remove(primID);
 		if (primitive != null) {
 			if (graphGroupNode != null) {
 				graphGroupNode.removeChild(primitive.getNode());
 			}
 		}
 		refresh();
 	}
 
 	@Override
 	public void unregisterPrimitive(List<Integer> ids) {
 		Iterator<Integer> primIter = ids.iterator();
 		while (primIter.hasNext()) {
 			OverlayPrimitive primitive = prim1DMap.remove(primIter.next());
 			if (primitive != null) {
 				if (graphGroupNode != null) {
 					graphGroupNode.removeChild(primitive.getNode());
 				}
 			}
 		}
 		refresh();
 	}
 
 	@Override
 	public void setTickGridLinesActive(boolean xcoord, boolean ycoord, boolean zcoord) {
 		showXTicks = xcoord;
 		showYTicks = ycoord;
 		if (xTicks != null && graph != null)
 			xTicks.setGeometry(createXTicksGeometry());
 		if (yTicks != null && graph != null)
 			yTicks.setGeometry(createYTicksGeometry());
 		if (secondaryXAxes != null) {
 			if (secondaryXAxisTicks != null)
 				secondaryXAxisTicks.setGeometry(create2ndXTicksGeometry());
 		}		
 	}
 
 	@Override
 	public void setZAxisValues(AxisValues axis) {
 		// Nothing to do
 	}
 
 	@Override
 	public void setLineThickness(int primID, double thickness) {
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim1DMap.get(primID);
 			if (primitive != null) {
 				primitive.setLineThickness(thickness);
 			}
 		}
 	}
 
 	@Override
 	public boolean setOutlineTransparency(int primID, double transparency) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim1DMap.get(primID);
 			if (primitive != null) {
 				primitive.setOutlineTransparency(transparency);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 
 	}
 
 	@Override
 	public boolean setPrimitiveVisible(int primID, boolean visible) {
 		boolean returnValue = false;
 		OverlayPrimitive primitive = prim1DMap.get(primID);
 		if (primitive != null) {
 			if (visible) {
 				if (primitive.isHidden() && (graphGroupNode != null)) {
 					graphGroupNode.addChild(primitive.getNode());
 					primitive.unhide();
 					returnValue = true;
 				}
 			} else {
 				if (!primitive.isHidden() && (graphGroupNode != null)) {
 					graphGroupNode.removeChild(primitive.getNode());
 					primitive.hide();
 					returnValue = true;
 				}
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public void resetView() {
 		// Nothing to do
 	}
 
 	@Override
 	public int registerPrimitive(PrimitiveType primType, boolean fixedSize) {
 		// Nothing to do
 		return -1;
 	}
 
 	/**
 	 * @param points
 	 * @throws PlotException
 	 */
 	public void addDataPoints(double[] points) throws PlotException {
 		if (xAxis == AxisMode.LINEAR || xAxis == AxisMode.LINEAR_WITH_OFFSET) {
 			if (sets.size() > 0) {
 				IDataset currentDataSet = sets.get(0);
 				SceneGraphComponent graph = subGraphs.get(0);
 				int n = currentDataSet.getSize();
 				currentDataSet.resize(n + points.length);
 				for (double p : points)
 					currentDataSet.set(p, n++);
 				if (undoSelectStack.size() == 0) {
 					determineRanges(sets);
 					currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 					if (xAxis == AxisMode.LINEAR)
 						graph.setGeometry(createGraphGeometry(currentDataSet));
 					else {
 						double offset = offsets.get(0);
 						graph.setGeometry(createGraphGeometry(currentDataSet, offset));
 					}
 					if (xTicks != null)
 						xTicks.setGeometry(createXTicksGeometry());
 					if (yTicks != null)
 						yTicks.setGeometry(createYTicksGeometry());
 				} else {
 					SelectedWindow bottom = undoSelectStack.get(undoSelectStack.size() - 1);
 					updateWindowWithNewRanges(sets, bottom);
 				}
 			} else
 				throw new PlotException(ERRORMSG_NO_GRAPH);
 		} else
 			throw new PlotException(ERRORMSG_NOT_LINEAR);
 	}
 
 	/**
 	 * @param points
 	 * @param axisVals
 	 * @throws PlotException
 	 */
 	public void addDataPoints(double[] points, double[] axisVals) throws PlotException {
 		if (xAxis == AxisMode.CUSTOM) {
 			if (sets.size() > 0) {
 				IDataset currentDataSet = sets.get(0);
 				SceneGraphComponent graph = subGraphs.get(0);
 				AxisValues axisVal = xAxes.get(0);
 				int n = currentDataSet.getSize();
 				currentDataSet.resize(n + points.length);
 				for (double p : points)
 					currentDataSet.set(p, n++);
 				axisVal.addValues(axisVals);
 				if (undoSelectStack.size() == 0) {
 					determineRanges(sets);
 					currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 					graph.setGeometry(createGraphGeometry(currentDataSet, axisVal));
 					if (xTicks != null)
 						xTicks.setGeometry(createXTicksGeometry());
 					if (yTicks != null)
 						yTicks.setGeometry(createYTicksGeometry());
 				} else {
 					SelectedWindow bottom = undoSelectStack.get(undoSelectStack.size() - 1);
 					updateWindowWithNewRanges(sets, bottom);
 				}
 			} else
 				throw new PlotException(ERRORMSG_NO_GRAPH);
 		} else
 			throw new PlotException(ERRORMSG_NOT_CUSTOM);
 	}
 
 	/**
 	 * @param valueList
 	 * @throws PlotException
 	 */
 	public void addDataPoints(List<double[]> valueList) throws PlotException {
 		if (xAxis == AxisMode.LINEAR || xAxis == AxisMode.LINEAR_WITH_OFFSET) {
 			if (valueList.size() == sets.size()) {
 				Iterator<double[]> newDataIter = valueList.iterator();
 				Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 				Iterator<IDataset> dataIter = sets.iterator();
 				Iterator<Double> offsetIter = null;
 				if (xAxis == AxisMode.LINEAR_WITH_OFFSET)
 					offsetIter = offsets.iterator();
 				while (newDataIter.hasNext()) {
 					double[] newValues = newDataIter.next();
 					IDataset currentDataSet = dataIter.next();
 					int n = currentDataSet.getSize();
 					currentDataSet.resize(n + newValues.length);
 					for (double p : newValues)
 						currentDataSet.set(p, n++);
 				}
 				if (undoSelectStack.size() == 0) {
 					determineRanges(sets);
 					currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 					dataIter = sets.iterator();
 					while (graphIter.hasNext()) {
 						SceneGraphComponent graph = graphIter.next();
 						IDataset currentDataSet = dataIter.next();
 						if (xAxis == AxisMode.LINEAR) {
 							graph.setGeometry(createGraphGeometry(currentDataSet));
 						} else {
 							double offset = offsetIter.next();
 							graph.setGeometry(createGraphGeometry(currentDataSet, offset));
 						}
 					}
 					if (xTicks != null)
 						xTicks.setGeometry(createXTicksGeometry());
 					if (yTicks != null)
 						yTicks.setGeometry(createYTicksGeometry());
 				} else {
 					SelectedWindow bottom = undoSelectStack.get(undoSelectStack.size() - 1);
 					updateWindowWithNewRanges(sets, bottom);
 				}
 			} else
 				throw new PlotException(ERRORMSG_WRONG_NUM_ENTRIES);
 		} else
 			throw new PlotException(ERRORMSG_NOT_LINEAR);
 	}
 
 	/**
 	 * @param valueList
 	 * @param axisValueList
 	 * @throws PlotException
 	 */
 	public void addDataPoints(List<double[]> valueList, List<double[]> axisValueList) throws PlotException {
 		if (xAxis == AxisMode.CUSTOM) {
 			if (valueList.size() == sets.size()) {
 				Iterator<double[]> newDataIter = valueList.iterator();
 				Iterator<double[]> newAxisDataIter = axisValueList.iterator();
 				Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 				Iterator<IDataset> dataIter = sets.iterator();
 				Iterator<AxisValues> axisIter = xAxes.iterator();
 				while (newDataIter.hasNext()) {
 					double[] newValues = newDataIter.next();
 					double[] newAxisValues = newAxisDataIter.next();
 					IDataset currentDataSet = dataIter.next();
 					AxisValues axis = axisIter.next();
 					int n = currentDataSet.getSize();
 					currentDataSet.resize(n + newValues.length);
 					for (double p : newValues)
 						currentDataSet.set(p, n++);
 					axis.addValues(newAxisValues);
 				}
 				if (undoSelectStack.size() == 0) {
 					determineRanges(sets);
 					currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
 					dataIter = sets.iterator();
 					axisIter = xAxes.iterator();
 					while (graphIter.hasNext()) {
 						SceneGraphComponent graph = graphIter.next();
 						IDataset currentDataSet = dataIter.next();
 						AxisValues axis = axisIter.next();
 						graph.setGeometry(createGraphGeometry(currentDataSet, axis));
 					}
 					if (xTicks != null)
 						xTicks.setGeometry(createXTicksGeometry());
 					if (yTicks != null)
 						yTicks.setGeometry(createYTicksGeometry());
 				} else {
 					SelectedWindow bottom = undoSelectStack.get(undoSelectStack.size() - 1);
 					updateWindowWithNewRanges(sets, bottom);
 				}
 			} else
 				throw new PlotException(ERRORMSG_WRONG_NUM_ENTRIES);
 		} else
 			throw new PlotException(ERRORMSG_NOT_CUSTOM);
 	}
 
 	@Override
 	public boolean drawLabel(int primID, double sx, double sy) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive labelPrim = (LabelPrimitive) prim1DMap.get(primID);
 			if (labelPrim != null) {
 				setScalingSmallFlag(graphYmin);
 				double min = ScalingUtility.valueScaler(graphYmin, yScaling);
 				double max = ScalingUtility.valueScaler(graphYmax, yScaling);
 				double xPos = (MAXX - xInset) * (sx - graphXmin) / (graphXmax - graphXmin);
 				double yPos = (MAXY - yInset) * (ScalingUtility.valueScaler(sy, yScaling) - min) / (max - min);
 				labelPrim.setLabelPosition(xPos, yPos);
 				labelPrim.setDataPoints(sx, sy);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public boolean setLabelFont(int primID, Font font) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive labelPrim = (LabelPrimitive) prim1DMap.get(primID);
 			if (labelPrim != null) {
 				labelPrim.setLabelFont(font);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public boolean setLabelOrientation(int primID, LabelOrientation orient) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive labelPrim = (LabelPrimitive) prim1DMap.get(primID);
 			if (labelPrim != null) {
 				labelPrim.setLabelDirection(orient);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public boolean setLabelText(int primID, String text, int alignment) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive labelPrim = (LabelPrimitive) prim1DMap.get(primID);
 			if (labelPrim != null) {
 				labelPrim.setLabelString(text);
 				labelPrim.setLabelAlignment(alignment);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	private void drawActionCrosshair(double xPos, double yPos) {
 		if (crosshairID_x != -1 && crosshairID_y != -1) {
 			begin(OverlayType.VECTOR2D);
 			setColour(crosshairID_x, java.awt.Color.RED);
 			setColour(crosshairID_y, java.awt.Color.RED);
 			drawLine(crosshairID_x, graphXmin, yPos, graphXmax, yPos);
 			drawLine(crosshairID_y, xPos, graphYmin, xPos, graphYmax);
 			end(OverlayType.VECTOR2D);
 		}
 	}
 
 	/**
 	 * Notify the plot that all plot replacement/update operations are actually an update of an previous plot.
 	 * 
 	 * @param isUpdate
 	 *            is this a update of a previous plot?
 	 */
 
 	public void setUpdateOperation(boolean isUpdate) {
 		isUpdateOperation = isUpdate;
 	}
 
 	@Override
 	public ScaleType getScaling() {
 		return yScaling;
 	}
 
 	@Override
 	public List<AxisValues> getAxisValues() {
 		return xAxes;
 	}
 
 	@Override
 	public void restoreDefaultPlotAreaCursor() {
 		plotArea.getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				plotArea.setCursor(defaultCursor);
 			}
 		});
 
 	}
 
 	@Override
 	public void setPlotAreaCursor(final int cursor) {
 		plotArea.getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				Cursor tempCursor = plotArea.getDisplay().getSystemCursor(cursor);
 				if (tempCursor != null)
 					plotArea.setCursor(tempCursor);
 			}
 		});
 	}
 
 	@Override
 	public void rotatePrimitive(int primID, double angle, double rcx, double rcy) {
 		// Nothing to do so far
 		
 	}
 
 	@Override
 	public void setAnchorPoints(int primID, double x, double y) {
 		// Nothing to do so far
 		
 	}
 
 	@Override
 	public void toggleErrorBars(boolean xcoord, boolean ycoord, boolean zcoord) {
 		// TODO Auto-generated method stub	
 		// not yet implemented in most plots.
 	}
 	
 	
 }
