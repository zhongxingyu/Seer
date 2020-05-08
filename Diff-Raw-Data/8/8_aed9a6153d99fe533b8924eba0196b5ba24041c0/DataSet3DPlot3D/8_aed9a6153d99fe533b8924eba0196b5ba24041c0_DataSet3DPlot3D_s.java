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
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting;
 
 import java.awt.Color;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.ColourImageData;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.ScaleType;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.SurfPlotStyles;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.TickFormatting;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.SurfacePlotROI;
 import de.jreality.geometry.IndexedLineSetFactory;
 import de.jreality.geometry.PointSetFactory;
 import de.jreality.geometry.QuadMeshFactory;
 import de.jreality.math.MatrixBuilder;
 import de.jreality.scene.Appearance;
 import de.jreality.scene.Camera;
 import de.jreality.scene.Geometry;
 import de.jreality.scene.IndexedLineSet;
 import de.jreality.scene.PointSet;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.scene.data.Attribute;
 import de.jreality.scene.data.DataList;
 import de.jreality.shader.CommonAttributes;
 import de.jreality.shader.DefaultGeometryShader;
 import de.jreality.shader.DefaultPointShader;
 import de.jreality.shader.DefaultTextShader;
 import de.jreality.shader.ShaderUtility;
 import de.jreality.ui.viewerapp.AbstractViewerApp;
 import de.jreality.util.CameraUtility;
 import de.jreality.util.SceneGraphUtility;
 
 /**
  *
  */
 public class DataSet3DPlot3D implements IDataSet3DCorePlot {
 
 	/**
 	 * Maximum dimension that can be displayed at once 
 	 */
 	public static int MAXDIM;
 
 	/**
 	 * Define the handness of the coordinate system
 	 */
 	public static final double HANDNESS = 1.0; // -1.0 right hand system 1.0 left hand system
 	
 	protected static final Logger logger = LoggerFactory
 	.getLogger(DataSetPlotter.class);	
 	
 	protected static final int MAXJOGLDIM = 1000;
 	protected static final int MAXSOFTDIM = 256;
 	protected IDataset currentData = null;
 	protected IDataset displayData = null;
  	protected AxisMode xAxis = AxisMode.LINEAR;
 	protected AxisMode yAxis = AxisMode.LINEAR;
 	protected AxisMode zAxis = AxisMode.LINEAR;
 
 	protected SceneGraphComponent graph = null;
 	protected SceneGraphComponent xLabelNode = null;
 	protected SceneGraphComponent yLabelNode = null;
 	protected SceneGraphComponent zLabelNode = null;
 	protected SceneGraphComponent xTicksNode = null;
 	protected SceneGraphComponent yTicksNode = null;
 	protected SceneGraphComponent zTicksNode = null;	
 	protected Appearance graphAppearance = null;
 	protected DefaultGeometryShader dgsGraph = null;
 
 	protected AxisValues xAxisValues;
 	protected AxisValues yAxisValues;	
 	protected AxisValues zAxisValues;
 	protected ColourImageData colourTable = null;
 
 	protected double globalZmin;
 	protected double globalZmax;
	protected double globalRealXmin;
	protected double globalRealXmax;
	protected double globalRealYmin;
	protected double globalRealYmax;
 	protected double xSpan;
 	protected double ySpan;
 	protected double colourTableMin;
 	protected double colourTableMax;
 
 	protected int windowEndPosX;
 	protected int windowEndPosY;
 	protected int windowStartPosX;
 	protected int windowStartPosY;
 	protected boolean useWindow;
 	protected boolean hasJOGL;
 	protected boolean xCoordActive = true;
 	protected boolean yCoordActive = true;
 	protected boolean zCoordActive = true;
 	
 	private AbstractViewerApp app;
 
 	private SceneGraphComponent axis = null;
 
 	private SceneGraphComponent boundBoxNode = null;
 	private SceneGraphComponent xAxisLabel = null;
 	private SceneGraphComponent yAxisLabel = null;
 	private SceneGraphComponent zAxisLabel = null;
 
 	private DefaultTextShader dtsXTicks = null;
 	private DefaultTextShader dtsYTicks = null;
 	private DefaultTextShader dtsZTicks = null;
 	private TickFactory tickFactory = null;	
 
 	private int MAXDIMSQR;
 	private int currentXdim;
 	private int currentYdim;
 	private int samplingRate;
 
 
 	private ScaleType zScaling = ScaleType.LINEAR;
 	private double xOffset;
 	private double yOffset;
 //	private double zOffset;
 
 	private TickFormatting xLabelMode = TickFormatting.plainMode;
 	private TickFormatting yLabelMode = TickFormatting.plainMode;
 	private TickFormatting zLabelMode = TickFormatting.plainMode;
 	private String xAxisLabelStr;
 	private String yAxisLabelStr;
 	private String zAxisLabelStr;
 	private SurfPlotStyles currentStyle = SurfPlotStyles.FILLED;
 	
 	/**
 	 * Constructor of a DataSet3DPlot3D
 	 * @param app ViewerApp
 	 * @param useJOGL is JOGL/OpenGL used
 	 * @param useWindow should a window on the data be used if too large
 	 */
 	public DataSet3DPlot3D(AbstractViewerApp app, 
 						   boolean useJOGL, 
 						   boolean useWindow)
 	{
 		this.app = app;
 		hasJOGL = useJOGL;
 		this.useWindow = useWindow;
 		if (hasJOGL)
 		{
 			MAXDIM = MAXJOGLDIM;
 		} else {
 			MAXDIM = MAXSOFTDIM;
 		}
 		MAXDIMSQR = MAXDIM * MAXDIM;
 		tickFactory = new TickFactory(yLabelMode);
 	}
 	
 	private IndexedLineSet createBBoxGeometry() {
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		
 		double [][] coords = new double[8][3];
 		int [][] edges = new int[12][2];
 		
 		coords[0][0] = -xSpan*0.5;
 		coords[0][1] = 0.0;
 		coords[0][2] = -ySpan;
 		coords[1][0] = xSpan*0.5;
 		coords[1][1] = 0.0;
 		coords[1][2] = -ySpan;
 		coords[2][0] = xSpan*0.5;
 		coords[2][1] = 0.0;
 		coords[2][2] = 0.0;
 		coords[3][0] = -xSpan*0.5;
 		coords[3][1] = 0.0;
 		coords[3][2] = 0.0;	
 		
 		coords[4][0] = -xSpan*0.5;
 		coords[4][1] = MAXZ;
 		coords[4][2] = -ySpan;
 		coords[5][0] = xSpan*0.5;
 		coords[5][1] = MAXZ;
 		coords[5][2] = -ySpan;
 		coords[6][0] = xSpan*0.5;
 		coords[6][1] = MAXZ;
 		coords[6][2] = 0.0;
 		coords[7][0] = -xSpan*0.5;
 		coords[7][1] = MAXZ;
 		coords[7][2] = 0.0;	
 
 		
 		edges[0][0] = 0;
 		edges[0][1] = 1;
 		edges[1][0] = 1;
 		edges[1][1] = 2;
 		edges[2][0] = 2;
 		edges[2][1] = 3;
 		edges[3][0] = 3;
 		edges[3][1] = 0;
 
 		edges[4][0] = 4;
 		edges[4][1] = 5;
 		edges[5][0] = 5;
 		edges[5][1] = 6;
 		edges[6][0] = 6;
 		edges[6][1] = 7;
 		edges[7][0] = 7;
 		edges[7][1] = 4;
 
 		edges[8][0] = 0;
 		edges[8][1] = 4;
 		edges[9][0] = 1;
 		edges[9][1] = 5;
 		edges[10][0] = 2;
 		edges[10][1] = 6;
 		edges[11][0] = 3;
 		edges[11][1] = 7;
 	
 		
 		factory.setEdgeCount(12);
 		factory.setVertexCount(8);
 		
 		factory.setVertexCoordinates(coords);
 		factory.setEdgeIndices(edges);
 		
 		factory.update();
 		return factory.getIndexedLineSet();
 	}
 	
 	@Override
 	public SceneGraphComponent buildBoundingBox() {
 		boundBoxNode = SceneGraphUtility.createFullSceneGraphComponent("BBox");
 		Appearance app = new Appearance();
 		boundBoxNode.setAppearance(app);
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(app, true);
 		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 		de.jreality.shader.DefaultLineShader dls = (de.jreality.shader.DefaultLineShader) dgs.createLineShader("default");
 		dls.setDiffuseColor(Color.GRAY);
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);
 		boundBoxNode.setGeometry(createBBoxGeometry());
 		MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(boundBoxNode);
 		return boundBoxNode;
 	}
 
 	protected IndexedLineSet createXAxisTicksGeometry() {
 		
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		
 		double min = globalRealXmin;
 		double max = globalRealXmax;
 		tickFactory.setTickMode(xLabelMode);
 		int width = app.getCurrentViewer().getViewingComponentSize().width;
 	    // protect against rubbish RCP early lazy initialisation
 		if (width == 0)
 	    	width = FONT_SIZE_PIXELS_WIDTH;
 		LinkedList<Tick> ticks = 
 			tickFactory.generateTicks(width, 
 					min, max, (short)0,false);
 		
         factory.setVertexCount(ticks.size()*2);
         factory.setEdgeCount(ticks.size());
         double [] coords = new double[ticks.size()*2*3];
         int [][] edges = ArrayPoolUtility.getIntArray(ticks.size());
 		for (int i = 0; i < ticks.size(); i++)
         {
         	double value = tickFactory.getTickUnit() * i;
 			coords[(i*2)*3] = -xSpan * 0.5 + (value/(globalRealXmax-globalRealXmin)) * xSpan;
 			coords[(i*2)*3 +1] = 0.0;
 			coords[(i*2)*3 +2] = -ySpan;
 			coords[(i*2+1)*3] = -xSpan * 0.5 + (value/(globalRealXmax-globalRealXmin)) * xSpan;
 			coords[(i*2+1)*3 +1] = 0.0;
 			coords[(i*2+1)*3 +2] = 0.0;
 			edges[i][0] = i*2;
 			edges[i][1] = i*2 + 1;
         }
         factory.setVertexCoordinates(coords);
         factory.setEdgeIndices(edges);
         factory.update();
 		return factory.getIndexedLineSet();
 	}
 	
 	protected IndexedLineSet createYAxisTicksGeometry() {
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		
 		tickFactory.setTickMode(yLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 	    // protect against rubbish RCP early lazy initialisation
 		if (height == 0)
 	    	height = FONT_SIZE_PIXELS_WIDTH;
 		LinkedList<Tick> ticks = 
 			tickFactory.generateTicks(height, 
 					globalRealYmin, globalRealYmax, (short)1,false);
 		
         factory.setVertexCount(ticks.size()*2);
         factory.setEdgeCount(ticks.size());
         double [] coords = new double[ticks.size()*2*3];
         int [][] edges = ArrayPoolUtility.getIntArray(ticks.size());
 		double range = globalRealYmax - globalRealYmin;
         double step = (ySpan * ((tickFactory.getTickUnit() * ticks.size())/range))/ticks.size();			
 
         for (int i = 0; i < ticks.size(); i++)
         {
 			coords[(i*2)*3] = -xSpan*0.5;
 			coords[(i*2)*3 +1] = 0.0;
 			coords[(i*2)*3 +2] = -ySpan + i * step;
 
 			coords[(i*2+1) * 3] = xSpan*0.5;
 			coords[(i*2+1) * 3 +1] = 0.0;
 			coords[(i*2+1) * 3 +2] = -ySpan + i * step;
 
 			edges[i][0] = i*2;
 			edges[i][1] = i*2 + 1;
         }
         factory.setVertexCoordinates(coords);
         factory.setEdgeIndices(edges);
         factory.update();
 		return factory.getIndexedLineSet();
 	}
 	
 	protected IndexedLineSet createZAxisTicksGeometry() {
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		setScalingSmallFlag(globalZmax);
 		double min = ScalingUtility.valueScaler(globalZmin, zScaling);
 		double max = ScalingUtility.valueScaler(globalZmax, zScaling);
 		double range = max - min;		
 		tickFactory.setTickMode(zLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 		LinkedList<Tick> ticks =
 			tickFactory.generateTicks(height, globalZmin, globalZmax, (short)1,false);
 		
         factory.setVertexCount(ticks.size()*2);
         factory.setEdgeCount(ticks.size());
         double [] coords = new double[ticks.size()*2 *3];
         int [][] edges = ArrayPoolUtility.getIntArray(ticks.size());
         for (int i = 0; i < ticks.size(); i++)
         {
         	double zValue = ticks.get(i).getTickValue();
 			zValue = ScalingUtility.valueScaler(zValue, zScaling);      	
 			coords[(i*2)*3] = -xSpan*0.5;
 			coords[(i*2)*3 +1] = MAXZ * (zValue - min) / range;
 			coords[(i*2)*3 +2] = -ySpan;
 
 			coords[(i*2+1)*3] = xSpan*0.5;
 			coords[(i*2+1)*3 +1] = MAXZ * (zValue - min) / range;
 			coords[(i*2+1)*3 +2] = -ySpan;
 			
 			edges[i][0] = i*2;
 			edges[i][1] = i*2 + 1;
         }
         factory.setVertexCoordinates(coords);
         factory.setEdgeIndices(edges);
         factory.update();
         return factory.getIndexedLineSet();
 	}
 	
 	@Override
 	public SceneGraphComponent buildCoordAxesTicks() {
 		SceneGraphComponent ticks = SceneGraphUtility.createFullSceneGraphComponent("ticks");
 		xTicksNode = SceneGraphUtility.createFullSceneGraphComponent("xTicks");
 		yTicksNode = SceneGraphUtility.createFullSceneGraphComponent("yTicks");
 		zTicksNode = SceneGraphUtility.createFullSceneGraphComponent("zTicks");
 		Appearance tickApp = new Appearance();
 		tickApp.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(tickApp, true);
 		de.jreality.shader.DefaultLineShader dls = (de.jreality.shader.DefaultLineShader) dgs.createLineShader("default");
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);
 		dls.setDiffuseColor(Color.black);
 		dls.setLineStipple(true);
 		xTicksNode.setAppearance(tickApp);
 		yTicksNode.setAppearance(tickApp);
 		zTicksNode.setAppearance(tickApp);
 		
 		ticks.addChild(xTicksNode);
 		ticks.addChild(yTicksNode);
 		ticks.addChild(zTicksNode);
 		
 		return ticks;
 	}
 
 	private IndexedLineSet createAxisGeometry() {
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		
 		double [][] coords = new double[6][3];
 		int [][] edges = new int[3][2];
 		
 		coords[0][0] = -xSpan*0.5;
 		coords[0][1] = 0.0;
 		coords[0][2] = -ySpan;
 		coords[1][0] = xSpan*0.5;
 		coords[1][1] = 0.0;
 		coords[1][2] = -ySpan;
 		
 		coords[2][0] = -xSpan*0.5;
 		coords[2][1] = 0.0;
 		coords[2][2] = -ySpan;
 		coords[3][0] = -xSpan*0.5;
 		coords[3][1] = MAXZ;
 		coords[3][2] = -ySpan;	
 
 		coords[4][0] = -xSpan*0.5;
 		coords[4][1] = 0.0;
 		coords[4][2] = -ySpan;
 		coords[5][0] = -xSpan*0.5;
 		coords[5][1] = 0.0;
 		coords[5][2] = 0.0;
 		
 		edges[0][0] = 0;
 		edges[0][1] = 1;
 		edges[1][0] = 2;
 		edges[1][1] = 3;
 		edges[2][0] = 4;
 		edges[2][1] = 5;
 		factory.setEdgeCount(3);
 		factory.setVertexCount(6);
 		
 		factory.setVertexCoordinates(coords);
 		factory.setEdgeIndices(edges);
 		
 		factory.update();
 		return factory.getIndexedLineSet();
 	}
 	
 	private PointSet createXAxisLabel() {
 		PointSetFactory pointFactory = new PointSetFactory();
 		pointFactory.setVertexCount(1);
 		double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 		coords[0][0] = 0.0;
 		coords[0][1] = 0.0;
 		coords[0][2] = 0.0;
 		String label = (xAxisLabelStr == null ? "" : xAxisLabelStr);
 		String[] labels = {label};
 		pointFactory.setVertexLabels(labels);
 		pointFactory.setVertexCoordinates(coords);
 		pointFactory.update();
 		return pointFactory.getPointSet();
 	}
 	
 	private PointSet createYAxisLabel() {
 		PointSetFactory pointFactory = new PointSetFactory();
 		pointFactory.setVertexCount(1);
 		double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 		coords[0][0] = -xSpan*0.5-0.25;
 		coords[0][1] = 0.25;
 		coords[0][2] = -ySpan*0.5;
 		String label = (yAxisLabelStr == null ? "" : yAxisLabelStr);
 		String[] labels = {label};
 		pointFactory.setVertexLabels(labels);
 		pointFactory.setVertexCoordinates(coords);
 		pointFactory.update();
 		return pointFactory.getPointSet();	
 	}
 	
 	private PointSet createZAxisLabel() {
 		PointSetFactory pointFactory = new PointSetFactory();
 		pointFactory.setVertexCount(1);
 		double[][] coords = ArrayPoolUtility.getDoubleArray(1);
 		coords[0][0] = -xSpan*0.5-0.25;
 		coords[0][1] = MAXZ * 0.5;
 		coords[0][2] = -ySpan;
 		String label = (zAxisLabelStr == null ? "" : zAxisLabelStr);
 		String[] labels = {label};
 		pointFactory.setVertexLabels(labels);
 		pointFactory.setVertexCoordinates(coords);
 		pointFactory.update();
 		return pointFactory.getPointSet();	}
 	
 	@Override
 	public SceneGraphComponent buildCoordAxis(SceneGraphComponent axis) {
 		this.axis = axis;
 		xAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("xAxisLabel");
 		yAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("yAxisLabel");
 		zAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("zAxisLabel");
 		
 		this.axis.addChild(xAxisLabel);
 		this.axis.addChild(yAxisLabel);
 		this.axis.addChild(zAxisLabel);
 		// x Axis
 		Appearance labelApp = new Appearance();
 		xAxisLabel.setAppearance(labelApp);
 		labelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		DefaultGeometryShader dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelApp,true);
 		DefaultPointShader dpoints =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dpoints.setPointSize(1.0);
 		dpoints.setDiffuseColor(java.awt.Color.white);
 		DefaultTextShader dtsXaxis = (DefaultTextShader)dpoints.createTextShader("default");
 		dtsXaxis.setDiffuseColor(java.awt.Color.black);
 		dtsXaxis.setScale(FONT_AXIS_SCALE);
 		// y Axis
 		labelApp = new Appearance();
 		yAxisLabel.setAppearance(labelApp);
 		
 		labelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelApp,true);
 		dpoints =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dpoints.setPointSize(1.0);
 		dpoints.setDiffuseColor(java.awt.Color.white);
 		DefaultTextShader dtsYaxis = (DefaultTextShader)dpoints.createTextShader("default");
 		dtsYaxis.setDiffuseColor(java.awt.Color.black);
 		dtsYaxis.setScale(FONT_AXIS_SCALE);
 		// z Axis
 		labelApp = new Appearance();
 		zAxisLabel.setAppearance(labelApp);
 
 		
 		labelApp.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelApp,true);
 		dpoints =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dpoints.setPointSize(1.0);
 		dpoints.setDiffuseColor(java.awt.Color.white);
 		DefaultTextShader dtsZaxis = (DefaultTextShader)dpoints.createTextShader("default");
 		dtsZaxis.setDiffuseColor(java.awt.Color.black);
 		dtsZaxis.setScale(FONT_AXIS_SCALE);	
 		dtsZaxis.setTextdirection(1);
 		Appearance app = axis.getAppearance();
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(app, true);
 		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);
 		return axis;
 	}
 
 	protected void determineGraphSize(int xAspect, int yAspect) {
 		if (xAspect == 0 || yAspect == 0) {
 			int xSize = displayData.getShape()[1];
 			int ySize = displayData.getShape()[0];
 			int maxDim = xSize;
 			if (ySize > maxDim)
 				maxDim = ySize;
 			
 			xSpan = MAXX * ((double)xSize / (double)maxDim);
 			ySpan = MAXY * ((double)ySize / (double)maxDim);
 			xSpan = Math.max(xSpan, 4.0);
 			ySpan = Math.max(ySpan, 4.0);
 		} else {
 			if (xAspect > yAspect) {
 				xSpan = MAXX;
 				ySpan = MAXX * ((double)yAspect/(double)xAspect);
 			} else {
 				ySpan = MAXY;
 				xSpan = MAXY * ((double)xAspect/(double)yAspect);				
 			}
 		}
 	}
 	
 	private Geometry switchToLineGraphGeometry() {
 		if (graph != null) {
 			DataList coordData = 
 				graph.getGeometry().getAttributes(Geometry.CATEGORY_VERTEX,Attribute.COORDINATES);
 			DataList colourData =
 				graph.getGeometry().getAttributes(Geometry.CATEGORY_VERTEX,Attribute.COLORS);
 			int xSize = displayData.getShape()[1];
 			int ySize = displayData.getShape()[0];
 			IndexedLineSetFactory lineFactory = new IndexedLineSetFactory();
 			lineFactory.setVertexCount(xSize * ySize);
 			lineFactory.setEdgeCount((xSize - 1) * (ySize - 1));
 			lineFactory.setVertexCoordinates(coordData);
 			lineFactory.setVertexColors(colourData);
 			int edgeFaces[][] = new int[(xSize - 1) * (ySize - 1)][2];
 			for (int y = 0; y < ySize - 1; y++)
 				for (int x = 0; x < xSize - 1; x++) {
 					edgeFaces[x + y * (xSize - 1)][0] = x + y * xSize;
 					edgeFaces[x + y * (xSize - 1)][1] = x + 1 + y * xSize;
 				}
 			lineFactory.setEdgeIndices(edgeFaces);
 			lineFactory.update();
 			return lineFactory.getIndexedLineSet();
 		}
 		return null;
 	}
 	
 	private Geometry switchToStandardGraphGeometry() {
 		if (graph != null) {
 			DataList coordData = 
 				graph.getGeometry().getAttributes(Geometry.CATEGORY_VERTEX,Attribute.COORDINATES);
 			DataList colourData =
 				graph.getGeometry().getAttributes(Geometry.CATEGORY_VERTEX,Attribute.COLORS);
 			int xSize = displayData.getShape()[1];
 			int ySize = displayData.getShape()[0];
 			QuadMeshFactory quadFactory = new QuadMeshFactory();
 			quadFactory.setVLineCount(ySize); 
 			quadFactory.setULineCount(xSize);
 			quadFactory.setClosedInUDirection(false);
 			quadFactory.setClosedInVDirection(false);		
 			quadFactory.setVertexCoordinates(coordData);
 			quadFactory.setVertexColors(colourData);
 			quadFactory.setGenerateFaceNormals(true);
 			quadFactory.setGenerateTextureCoordinates(false);
 			quadFactory.setGenerateEdgesFromFaces(true);
 			quadFactory.setEdgeFromQuadMesh(true);
 			quadFactory.update();
 			return quadFactory.getIndexedFaceSet();
 		}
 		return null;
 	}
 	
 	protected Geometry createGraphGeometry(int xAspect, int yAspect)
 	{
 		int xSize = displayData.getShape()[1];
 		int ySize = displayData.getShape()[0];
 		determineGraphSize(xAspect,yAspect);
 		double[] coords = new double[xSize * ySize * 3];
 		double[] colours = new double[xSize * ySize * 3];
 		double xStep = xSpan / xSize;
 		double yStep = ySpan / ySize;
 		setScalingSmallFlag(globalZmin);
 		double min = ScalingUtility.valueScaler(globalZmin, zScaling);
 		double max = ScalingUtility.valueScaler(globalZmax, zScaling);		
 		double zScale = MAXZ / (max - min);
 		for (int y = 0; y < ySize; y++)
 		{
 			for (int x = 0; x < xSize; x++)
 			{
 				double dataEntry = displayData.getDouble(y, x);
 				coords[(x + y * xSize) * 3] = -xSpan * 0.5f + xStep * x;
 				coords[(x + y * xSize) * 3 + 1] = zScale * (ScalingUtility.valueScaler(dataEntry,zScaling) - min); 
 				coords[(x + y * xSize) * 3 + 2] = (ySpan * -HANDNESS) + y * HANDNESS * yStep;
 				if (colourTable == null) {
 					colours[(x + y * xSize) * 3] = 0.25;
 					colours[(x + y * xSize) * 3 + 1] = (ScalingUtility.valueScaler(dataEntry, zScaling) - min) / (max - min);
 					colours[(x + y * xSize) * 3 + 2] = 0.25;
 				} else {
 					double cmin = ScalingUtility.valueScaler(colourTableMin, zScaling);
 					double cmax = ScalingUtility.valueScaler(colourTableMax, zScaling);
 					if (hasJOGL) {
 						int index = (int) (colourTable.getWidth() * ((ScalingUtility.valueScaler(dataEntry,zScaling) - cmin) / (cmax - cmin)));
 						index = Math.min(Math.max(0,index),colourTable.getWidth()-1);
 						int packedRGBcolour = colourTable.get(index);
 						int red = (packedRGBcolour >> 16) & 0xff;
 						int green = (packedRGBcolour >> 8) & 0xff;
 						int blue = (packedRGBcolour) & 0xff;							
 						colours[(x+y*xSize) * 3] = red / 255.0;
 						colours[(x+y*xSize) * 3 + 1] = green / 255.0;
 						colours[(x+y*xSize) * 3 + 2] = blue / 255.0;
 					} else {
 						int x1 = Math.min(x, colourTable.getWidth()-1);
 						int y1 = Math.min(y, colourTable.getHeight()-1);
 						int packedRGBcolour = colourTable.get(x1 + y1 * colourTable.getWidth());
 						int red = (packedRGBcolour >> 16) & 0xff;
 						int green = (packedRGBcolour >> 8) & 0xff;
 						int blue = (packedRGBcolour) & 0xff;							
 						colours[(x+y*xSize) * 3] = red / 255.0;
 						colours[(x+y*xSize) * 3 + 1] = green / 255.0;
 						colours[(x+y*xSize) * 3 + 2] = blue / 255.0;
 					}
 				}
 			}
 		}
 		QuadMeshFactory quadFactory = new QuadMeshFactory();		
 		quadFactory.setVLineCount(ySize); 
 		quadFactory.setULineCount(xSize);
 		quadFactory.setClosedInUDirection(false);
 		quadFactory.setClosedInVDirection(false);		
 		quadFactory.setVertexCoordinates(coords);
 		quadFactory.setVertexColors(colours);
 		quadFactory.setGenerateFaceNormals(false);
 		quadFactory.setGenerateTextureCoordinates(false);
 		quadFactory.setGenerateEdgesFromFaces(true);
 		quadFactory.setEdgeFromQuadMesh(false);
 		quadFactory.update();
 		return quadFactory.getIndexedFaceSet();
 	}
 	
 	protected void buildDisplayDataSet()
 	{
 		int dimX = currentData.getShape()[1];
 		int dimY = currentData.getShape()[0];
 		if (dimX * dimY > MAXDIMSQR)
 		{
 			if (useWindow) {
 				logger.info("DataSet is too large to visualize all at once using window");
 				samplingRate = 1;
 				float reduceFactor = (float)MAXDIMSQR / (float)(dimX * dimY);
 				float xAspect = (float)dimX / (float)(dimX+dimY);
 				float yAspect = (float)dimY / (float)(dimX+dimY);
 				float xReduce = 1.0f - (1.0f - reduceFactor) * xAspect;
 				float yReduce = 1.0f - (1.0f - reduceFactor) * yAspect;
 				currentXdim = (int)(dimX * xReduce * 0.75f);
 				currentYdim = (int)(dimY * yReduce * 0.75f);
 				windowStartPosX = 0;
 				windowStartPosY = 0;
 				windowEndPosX = currentXdim-1;
 				windowEndPosY = currentYdim-1;
 			} else {
 				logger.info("DataSet is too large to visualize all at once using subsampling");
 		        if (dimY > dimX) 
 		        	samplingRate = (int)Math.ceil(dimY / MAXDIM);
 		        else 
 		        	samplingRate = (int)Math.ceil(dimX / MAXDIM);
 		        currentYdim = dimY / samplingRate;
 		        currentXdim = dimX / samplingRate;
 			}
 
 			int startP[] = {0,0};
 			int endP[] = {(useWindow ? currentYdim : dimY),(useWindow ? currentXdim : dimX)};
 			int steps[] = {samplingRate,samplingRate};
 			displayData = currentData.getSlice(startP,endP,steps);
 		} else {
 			displayData = currentData;
 			windowEndPosX = dimX-1;
 			windowEndPosY = dimY-1;
 		}
 		
 	}
 	
 	protected void setGlobalMinMax() {
 		switch (xAxis) {
 			case LINEAR:
 				globalRealXmin = 0;
 				if (!useWindow)
 					globalRealXmax = currentData.getShape()[1];
 				else {
 					globalRealXmax = windowEndPosX;
 					globalRealXmin = windowStartPosX;
 				}
 			break;
 			case LINEAR_WITH_OFFSET:
 				globalRealXmin = xOffset;
 				if (!useWindow)
 					globalRealXmax = currentData.getShape()[1] + xOffset;
 				else {
 					globalRealXmax = windowEndPosX + xOffset;
 					globalRealXmin = windowStartPosX + xOffset;
 				}
 			break;
 			case CUSTOM:
 				if (!useWindow || windowEndPosX == 0)
 				{
 					globalRealXmin = xAxisValues.getMinValue();
 					globalRealXmax = xAxisValues.getMaxValue();
 				} else {
 					globalRealXmin = xAxisValues.getValue(windowStartPosX);
 					globalRealXmax = xAxisValues.getValue(windowEndPosX);
 				}
 			break;
 		}
 		switch (yAxis) {
 			case LINEAR:
 				globalRealYmin = 0;
 				if (!useWindow) 
 					globalRealYmax = currentData.getShape()[0];
 				else {
 					globalRealYmax = windowEndPosY;
 					globalRealYmin = windowStartPosY;
 				}
 			break;
 			case LINEAR_WITH_OFFSET:
 				globalRealYmin = yOffset;
 				if (!useWindow) 				
 					globalRealYmax = currentData.getShape()[0] + yOffset;					
 				else {
 					globalRealYmax = windowEndPosY + yOffset;
 					globalRealYmin = windowStartPosY + yOffset;
 				}
 			break;
 			case CUSTOM:
 				if (!useWindow || windowEndPosY == 0)
 				{
 					globalRealYmin = yAxisValues.getMinValue();
 					globalRealYmax = yAxisValues.getMaxValue();
 				} else {
 					globalRealYmin = yAxisValues.getValue(windowStartPosY);
 					globalRealYmax = yAxisValues.getValue(windowEndPosY);
 				}			
 			break;
 		}
 	}
 
 	/**
 	 * Set a new Data window position
 	 * @param roi SurfacePlot region of interest object contains all the necessary
 	 *            information to build a new displaying dataset
 	 */
 	public void setDataWindow(SurfacePlotROI roi) {
 		if (useWindow) {
 			windowStartPosX = roi.getStartX();
 			windowStartPosY = roi.getStartY();
 			windowEndPosX = roi.getEndX();
 			windowEndPosY = roi.getEndY();
 			int swap;
 			if (windowStartPosX > windowEndPosX)
 			{
 				swap = windowStartPosX;
 				windowStartPosX = windowEndPosX;
 				windowEndPosX = swap;				
 			}
 			
 			if (windowStartPosY > windowEndPosY)
 			{
 				swap = windowStartPosY;
 				windowStartPosY = windowEndPosY;
 				windowEndPosY = swap;				
 			}
 			
 			int dimWidth = Math.abs(windowEndPosX - windowStartPosX);
 			int dimHeight = Math.abs(windowEndPosY - windowStartPosY);
 			
 			if (dimWidth < 2) {
 				if (windowEndPosX+2 >= currentData.getShape()[1])
 					windowStartPosX-=2;
 				else
 					windowEndPosX+=2;
 			}
 			
 			if (dimHeight < 2) {
 				if (windowEndPosY+2 >= currentData.getShape()[0])
 					windowStartPosY-=2;
 				else
 					windowEndPosY+=2;
 			}
 			
 			if (windowStartPosX == windowEndPosX)
 				windowEndPosX++;
 			if (windowStartPosY == windowEndPosY)
 				windowEndPosY++;
 			
 			int startP[] = {windowStartPosY,windowStartPosX};
 			int endP[] = {windowEndPosY,windowEndPosX};
 			int steps[] = {1,1};
 			displayData = currentData.getSlice(startP,endP,steps);
 			if (roi.getXSamplingMode() > 0 ||
 				roi.getYSamplingMode() > 0) {
 				int xDim = Math.abs(windowEndPosX - windowStartPosX);
 				int yDim = Math.abs(windowEndPosY - windowStartPosY);
 			    float totalSubFactor = (float)MAXDIMSQR / (float)(xDim * yDim);
 			    float xRatio = (float)xDim / (float)(yDim+xDim);
 			    float yRatio = (float)yDim / (float)(yDim+xDim);
 			    float xSampleFactor = (1.0f - totalSubFactor) * (roi.getYSamplingMode() != 0 ? xRatio : 1.0f);
 			    float ySampleFactor = (1.0f - totalSubFactor) * (roi.getXSamplingMode() != 0 ? yRatio : 1.0f);
 			    xSampleFactor = (float)Math.sqrt(xSampleFactor);
 			    ySampleFactor = (float)Math.sqrt(ySampleFactor);
 				int xSize = (roi.getXSamplingMode() == 0 ? xDim : (int)Math.floor(xDim * (1.0f - xSampleFactor)));
 				int ySize = (roi.getYSamplingMode() == 0 ? yDim : (int)Math.floor(yDim * (1.0f - ySampleFactor)));
 				DownsampleMode mode = DownsampleMode.POINT;
 				switch(roi.getXSamplingMode()) {
 					case 2 : mode = DownsampleMode.MEAN;
 					break;
 					case 3 : mode = DownsampleMode.MAXIMUM;
 					break;
 					case 4: mode = DownsampleMode.MINIMUM;
 					break;
 				}
 				int xSampleRate = (int)Math.ceil((double)xDim / (double)xSize);
 				int ySampleRate = (int)Math.round((double)yDim / (double)ySize);
 				Downsample sample = new Downsample(mode,
 												  (roi.getYSamplingMode() == roi.getXSamplingMode() ? ySampleRate : 1),
 												   xSampleRate);
 				displayData = sample.value(displayData).get(0);
 				if (roi.getYSamplingMode() != roi.getXSamplingMode() &&
 					roi.getYSamplingMode() != 0) {
 
 					switch(roi.getYSamplingMode()) {
 						case 1: mode = DownsampleMode.POINT;
 						break;
 						case 2 : mode = DownsampleMode.MEAN;
 						break;
 						case 3 : mode = DownsampleMode.MAXIMUM;
 						break;
 						case 4: mode = DownsampleMode.MINIMUM;
 						break;
 					}
 					sample = new Downsample(mode,ySampleRate,1);
 					displayData = sample.value(displayData).get(0);					
 				}
 			}
 			updateDisplay(roi.getXAspect(),roi.getYAspect());
 		}
 	}
 	
 	protected void buildOtherNodes() {
 		Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
         sceneCamera.setFieldOfView(56.5f);
         MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(graph);
 		if (axis != null) {
 			axis.setGeometry(createAxisGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(axis);
 		}
 		if (xLabelNode != null) {
 			xLabelNode.setGeometry(createXLabelsGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(xLabelNode);
 		}
 		if (yLabelNode != null) {
 			yLabelNode.setGeometry(createYLabelsGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(yLabelNode);
 		}
 		if (zLabelNode != null) {
 			zLabelNode.setGeometry(createZLabelsGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(zLabelNode);
 		}
 		if (xTicksNode != null) {
 			xTicksNode.setGeometry(createXAxisTicksGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(xTicksNode);
 		}
 		if (yTicksNode != null) {
 			yTicksNode.setGeometry(createYAxisTicksGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(yTicksNode);
 		}
 		if (zTicksNode != null) {
 			zTicksNode.setGeometry(createZAxisTicksGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(zTicksNode);
 		}
 		if (xAxisLabel != null) {
 			xAxisLabel.setGeometry(createXAxisLabel());
 		}
 		if (yAxisLabel != null) {
 			yAxisLabel.setGeometry(createYAxisLabel());
 		}
 		if (zAxisLabel != null) {
 			zAxisLabel.setGeometry(createZAxisLabel());
 		}		
 	}
 	
 	@Override
 	public SceneGraphComponent buildGraph(List<IDataset> datasets,
 			SceneGraphComponent graph) {
 		assert (datasets.size() > 0);
 		if (graph != null)
 		{
 			this.graph = graph;
 			currentData = datasets.get(0);
 			buildDisplayDataSet();
 			setGlobalMinMax();
 			
 			globalZmin = displayData.min().doubleValue();
 			globalZmax = displayData.max().doubleValue();
 			graphAppearance = new Appearance();
 			graph.setGeometry(createGraphGeometry(0,0));
 			graph.setAppearance(graphAppearance);
 			dgsGraph = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 			graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);	
 			graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 			graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.POINT_SIZE, 1.0);
 			graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE,false);
 			dgsGraph.setShowFaces(true);
 			dgsGraph.setShowLines(false);
 			dgsGraph.setShowPoints(false);
 			buildOtherNodes();
 		}
 		return graph;
 	}
 
 	protected PointSet createXLabelsGeometry()
 	{	
 		PointSetFactory factory = new PointSetFactory();
 		double min = globalRealXmin;
 		double max = globalRealXmax;
 		tickFactory.setTickMode(xLabelMode);
 		int width = app.getCurrentViewer().getViewingComponentSize().width;
 	    // protect against rubbish RCP early lazy initialisation
 		if (width == 0)
 	    	width = FONT_SIZE_PIXELS_WIDTH;
 		LinkedList<Tick> ticks = 
 			tickFactory.generateTicks(width, 
 					min, max, (short)0,false);
 		
         String[] edgeLabels = new String[ticks.size()];
         factory.setVertexCount(ticks.size());
         double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
         double oldX = -100.0f;
         if (xAxis == AxisMode.CUSTOM) {
 			boolean ascending = xAxisValues.isAscending();
         	for (int i = 0; i < ticks.size(); i++)
 	        {
 	        	double value = tickFactory.getTickUnit() * i;
 	        	Tick currentTick = null;
 	        	if (ascending)
 	        		currentTick = ticks.get(i);
 	        	else
 	        		currentTick = ticks.get(ticks.size()-1-i);
 	        	double newX = -xSpan * 0.5 + (value/(globalRealXmax-globalRealXmin)) * xSpan;
 				coords[i][0] = newX;
 				coords[i][1] = -0.125;
 				coords[i][2] = -ySpan-0.25;
 				if (Math.abs(newX-oldX) > 0.5f) {
 					edgeLabels[i] = currentTick.getTickName();
 					oldX = newX;
 				} else
 					edgeLabels[i] = "";
 	        }
         } else {
 			for (int i = 0; i < ticks.size(); i++)
 	        {
 	        	double value = tickFactory.getTickUnit() * i;
 	        	Tick currentTick = ticks.get(i);
 	        	double newX = -xSpan * 0.5 + (value/(globalRealXmax-globalRealXmin)) * xSpan;
 				coords[i][0] = -xSpan * 0.5 + (value/(globalRealXmax-globalRealXmin)) * xSpan;
 				coords[i][1] = -0.125;
 				coords[i][2] = -ySpan-0.25;
 				if (Math.abs(newX-oldX) > 0.5f) {
 					edgeLabels[i] = currentTick.getTickName();
 					oldX = newX;
 				} else
 					edgeLabels[i] = "";
 	        }        	
         }
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);
 		factory.update();
 		return factory.getPointSet();
 	}	
 
 	protected PointSet createYLabelsGeometry()
 	{	
 		PointSetFactory factory = new PointSetFactory();
 		tickFactory.setTickMode(yLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 		LinkedList<Tick> ticks =
 			tickFactory.generateTicks(height, globalRealYmin, globalRealYmax, (short)1,false);
 		double range = globalRealYmax - globalRealYmin;		
 
         
 		double step = (ySpan * ((tickFactory.getTickUnit() * ticks.size())/range))/ticks.size();			
         String[] edgeLabels = new String[ticks.size()];
         factory.setVertexCount(ticks.size());
         double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
         if (yAxis == AxisMode.CUSTOM) {
         	boolean isAscending = yAxisValues.isAscending();
 	        for (int i = 0; i < ticks.size(); i++)
 	        {
 				Tick currentTick = null;
 				
 				if (isAscending)
 					currentTick = ticks.get(i);
 				else
 					currentTick = ticks.get(ticks.size()-1-i);
 				
 				coords[i][0] = -xSpan*0.5-0.25;
 				coords[i][1] = 0.0;
 				coords[i][2] = -ySpan + i * step;
 				edgeLabels[i] = currentTick.getTickName();
 	        }        	
         } else {
 	        for (int i = 0; i < ticks.size(); i++)
 	        {
 				Tick currentTick = ticks.get(i);
 				coords[i][0] = -xSpan*0.5-0.25;
 				coords[i][1] = 0.0;
 				coords[i][2] = -ySpan + i * step;
 				edgeLabels[i] = currentTick.getTickName();
 	        }
         }
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);		
 		factory.update();
 		return factory.getPointSet();
 	}
 	
 	protected PointSet createZLabelsGeometry()
 	{	
 		PointSetFactory factory = new PointSetFactory();
 		setScalingSmallFlag(globalZmax);
 		double min = ScalingUtility.valueScaler(globalZmin, zScaling);
 		double max = ScalingUtility.valueScaler(globalZmax, zScaling);
 		double range = max - min;		
 		tickFactory.setTickMode(zLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 		LinkedList<Tick> ticks =
 			tickFactory.generateTicks(height, globalZmin, globalZmax, (short)1,false);
         String[] edgeLabels = new String[ticks.size()];
         factory.setVertexCount(ticks.size());
         double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
         double lastEntry = - 10.0;
         for (int i = 0; i < ticks.size(); i++)
         {
 			Tick currentTick = ticks.get(i);
 	      	double zValue = ticks.get(i).getTickValue();
 			zValue = ScalingUtility.valueScaler(zValue, zScaling);      	
 			coords[i][0] = -xSpan*0.5-0.25;
 			coords[i][1] = MAXZ * (zValue - min) / range;
 			coords[i][2] = -ySpan;
 			if (Math.abs(coords[i][1]-lastEntry) > 0.3)
 				edgeLabels[i] = currentTick.getTickName();
 			else
 				edgeLabels[i] = "";
 			lastEntry = coords[i][1];
         }
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);		
 		factory.update();
 		return factory.getPointSet();
 	}
 	
 	@Override
 	public void buildXCoordLabeling(SceneGraphComponent comp) {
 		xLabelNode = comp;
 		Appearance labelAppearance = new Appearance();
 		xLabelNode.setAppearance(labelAppearance);
 		DefaultGeometryShader dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelAppearance,true);
 		labelAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		DefaultPointShader dps =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsXTicks = (DefaultTextShader) dps.getTextShader();
 		double[] offset = new double[]{0,0.0,0.0};
 		dtsXTicks.setOffset(offset);
 		dtsXTicks.setScale(FONT_SCALE);
 		dtsXTicks.setDiffuseColor(java.awt.Color.black);
 		dtsXTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 	}
 
 	@Override
 	public void buildYCoordLabeling(SceneGraphComponent comp) {
 		this.yLabelNode = comp;
 		Appearance labelAppearance = new Appearance();
 		yLabelNode.setAppearance(labelAppearance);
 		DefaultGeometryShader dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelAppearance,true);
 		labelAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		DefaultPointShader dps =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsYTicks = (DefaultTextShader) dps.getTextShader();
 		double[] offset = new double[]{0,0.0,0.0};
 		dtsYTicks.setOffset(offset);
 		dtsYTicks.setScale(FONT_SCALE);
 		dtsYTicks.setDiffuseColor(java.awt.Color.black);
 		dtsYTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 
 	}
 
 	@Override
 	public void buildZCoordLabeling(SceneGraphComponent comp) {
 		this.zLabelNode = comp;
 		Appearance labelAppearance = new Appearance();
 		zLabelNode.setAppearance(labelAppearance);
 		DefaultGeometryShader dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelAppearance,true);
 		labelAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		DefaultPointShader dps =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsZTicks = (DefaultTextShader) dps.getTextShader();
 		double[] offset = new double[]{0,0.0,0.0};
 		dtsZTicks.setOffset(offset);
 		dtsZTicks.setScale(FONT_SCALE);
 		dtsZTicks.setDiffuseColor(java.awt.Color.black);
 		dtsZTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 	}
 
 	@Override
 	public void cleanUpGraphNode() {
 		if (axis != null) {
 			axis.removeChild(xAxisLabel);
 			axis.removeChild(yAxisLabel);
 			axis.removeChild(zAxisLabel);
 		}
 		if (xAxisLabel != null) xAxisLabel.setGeometry(null);
 		if (yAxisLabel != null) yAxisLabel.setGeometry(null);
 		if (zAxisLabel != null) zAxisLabel.setGeometry(null);
 		if (zLabelNode != null) zLabelNode.setGeometry(null);
 	}
 
 	@Override
 	public void handleColourCast(ColourImageData colourTable,
 			SceneGraphComponent graph, double minValue, double maxValue) {
 		
 		if (graph != null) {
 			this.colourTable = colourTable;
 			colourTableMin = minValue;
 			colourTableMax = maxValue;
 			if (graph.getGeometry() != null) {
 				PointSet geom = (PointSet)graph.getGeometry();
 				int width = displayData.getShape()[1];
 				int height = displayData.getShape()[0];
 				double [][] colours = new double[width * height][3];
 				
 				if (hasJOGL) {
 					for (int y = 0; y < height; y++) {
 						for (int x = 0; x < width; x++) {
 							double value = displayData.getDouble(y, x);
 							int index = (int) (colourTable.getWidth() * ((value - minValue) / (maxValue - minValue)));
 							index = Math.min(Math.max(0,index),colourTable.getWidth()-1);
 							int packedRGBcolour = colourTable.get(index);
 							int red = (packedRGBcolour >> 16) & 0xff;
 							int green = (packedRGBcolour >> 8) & 0xff;
 							int blue = (packedRGBcolour) & 0xff;							
 							colours[x+y*width][0] = red / 255.0;
 							colours[x+y*width][1] = green / 255.0;
 							colours[x+y*width][2] = blue / 255.0;
 						}
 					}
 				} else {
 					for (int y = 0; y < height; y++) {
 						for (int x= 0; x < width; x++) {
 							int packedRGBcolour = colourTable.get(x + y * width);
 							int red = (packedRGBcolour >> 16) & 0xff;
 							int green = (packedRGBcolour >> 8) & 0xff;
 							int blue = (packedRGBcolour) & 0xff;							
 							colours[x+y*width][0] = red / 255.0;
 							colours[x+y*width][1] = green / 255.0;
 							colours[x+y*width][2] = blue / 255.0;
 						}
 					}
 				}
 				geom.setVertexAttributes(de.jreality.scene.data.Attribute.COLORS,
 						new de.jreality.scene.data.DoubleArrayArray.Array(colours));
 			}
 		}
 	}
 
 	@Override
 	public void notifyComponentResize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setAxisModes(AxisMode axis, AxisMode axis2, AxisMode axis3) {
 		this.xAxis = axis;
 		this.yAxis = axis2;
 		this.zAxis = axis3;
 	}
 
 	@Override
 	public void setScaling(ScaleType newScaling) {
 		zScaling = newScaling;
 		graph.setGeometry(createGraphGeometry(0,0));
 		if (zLabelNode != null) {
 			zLabelNode.setGeometry(createZLabelsGeometry());
 		}
 		if (zTicksNode != null && zCoordActive) {
 			zTicksNode.setGeometry(createZAxisTicksGeometry());
 		}
 	}
 
 
 	@Override
 	public void setTickGridLinesActive(boolean xcoord, boolean ycoord,
 			boolean zcoord) {
 		xCoordActive = xcoord;
 		yCoordActive = ycoord;
 		zCoordActive = zcoord;
 		if (xcoord) {
 			xTicksNode.setGeometry(createXAxisTicksGeometry());
 		} else
 			xTicksNode.setGeometry(null);
 		if (ycoord) {
 			yTicksNode.setGeometry(createYAxisTicksGeometry());
 		} else
 			yTicksNode.setGeometry(null);
 		if (zcoord) {
 			zTicksNode.setGeometry(createZAxisTicksGeometry());
 		} else
 			zTicksNode.setGeometry(null);
 
 	}
 
 
 	@Override
 	public void setTitle(String title) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setXAxisLabel(String label) {
 		if (!label.equals(xAxisLabelStr)) {
 			xAxisLabelStr = label;
 			if (xAxisLabel != null)
 				xAxisLabel.setGeometry(createXAxisLabel());
 		}
 	}
 
 	@Override
 	public void setXAxisLabelMode(TickFormatting newFormat) {
 		xLabelMode = newFormat;
 		if (xLabelNode != null)
 			xLabelNode.setGeometry(createXLabelsGeometry());
 	}
 
 	@Override
 	public void setXAxisOffset(double offset) {
 		xOffset = offset;
 	}
 
 	@Override
 	public void setXAxisValues(AxisValues axis, int numOfDataSets) {
 		xAxisValues = axis;
 	}
 
 	@Override
 	public void setYAxisLabel(String label) {
 		if (!label.equals(yAxisLabelStr)) {
 			yAxisLabelStr = label;
 			if (yAxisLabel != null)
 				yAxisLabel.setGeometry(createYAxisLabel());
 		}
 	}
 
 	@Override
 	public void setYAxisLabelMode(TickFormatting newFormat) {
 		yLabelMode = newFormat;
 		if (yLabelNode != null)
 			yLabelNode.setGeometry(createYLabelsGeometry());
 
 	}
 
 	@Override
 	public void setYAxisOffset(double offset) {
 		yOffset = offset;
 	}
 
 	@Override
 	public void setYAxisValues(AxisValues axis) {
 		yAxisValues = axis;
 	}
 
 	@Override
 	public void setZAxisLabel(String label) {
 		if (!label.equals(zAxisLabelStr)) {
 			zAxisLabelStr = label;
 			if (zAxisLabel != null)
 				zAxisLabel.setGeometry(createZAxisLabel());
 		}	
 	}
 
 	@Override
 	public void setZAxisLabelMode(TickFormatting newFormat) {
 		zLabelMode = newFormat;
 		if (zLabelNode != null)
 			zLabelNode.setGeometry(createZLabelsGeometry());
 	}
 
 	@Override
 	public void setZAxisOffset(double offset) {
 //		zOffset = offset;
 	}
 
 	@Override
 	public void setZAxisValues(AxisValues axis) {
 		zAxisValues = axis;
 	}
 
 	protected void updateDisplay(int xAspect, int yAspect) {
 		setGlobalMinMax();
 		globalZmin = displayData.min().doubleValue();
 		globalZmax = displayData.max().doubleValue();
 		graph.setGeometry(createGraphGeometry(xAspect,yAspect));
 		if (axis != null) {
 			axis.setGeometry(createAxisGeometry());
 			MatrixBuilder.euclidean().translate(0.0f,-MAXY*0.5f,0.0f).assignTo(axis);			
 		}
 		if (xLabelNode != null) {
 			xLabelNode.setGeometry(createXLabelsGeometry());
 		}
 		if (yLabelNode != null) {
 			yLabelNode.setGeometry(createYLabelsGeometry());
 		}
 		if (zLabelNode != null) {
 			zLabelNode.setGeometry(createZLabelsGeometry());
 		}
 		if (xTicksNode != null && xCoordActive) {
 			xTicksNode.setGeometry(createXAxisTicksGeometry());
 		}
 		if (yTicksNode != null && yCoordActive) {
 			yTicksNode.setGeometry(createYAxisTicksGeometry());
 		}
 		if (zTicksNode != null && zCoordActive) {
 			zTicksNode.setGeometry(createZAxisTicksGeometry());
 		}
 		if (boundBoxNode != null) {
 			boundBoxNode.setGeometry(createBBoxGeometry());
 		}
 	}
 	@Override
 	public void updateGraph(IDataset newData) {
 		if (graph != null)
 		{
 			currentData = newData;
 			buildDisplayDataSet();
 			updateDisplay(0,0);
 
 		}
 	}
 
 	@Override
 	public void updateGraph(List<IDataset> datasets) {
 		updateGraph(datasets.get(0));
 	}
 	
 	/**
 	 * Set the current plotting style
 	 * @param newStyle the new style that should be used
 	 */
 	public void setStyle(SurfPlotStyles newStyle) {
 		if (newStyle != currentStyle) {
 			
 			if (currentStyle == SurfPlotStyles.LINEGRAPH)
 				graph.setGeometry(switchToStandardGraphGeometry());
 
 			if (currentStyle != SurfPlotStyles.LINEGRAPH &&
 				newStyle == SurfPlotStyles.LINEGRAPH)
 				graph.setGeometry(switchToLineGraphGeometry());
 			
 			switch (newStyle) {
 				case FILLED:
 					dgsGraph.setShowFaces(true);
 					dgsGraph.setShowLines(false);
 					dgsGraph.setShowPoints(false);
 				break;
 				case WIREFRAME:
 					dgsGraph.setShowFaces(false);
 					dgsGraph.setShowLines(true);
 					dgsGraph.setShowPoints(false);
 				break;
 				case LINEGRAPH:
 					dgsGraph.setShowFaces(false);
 					dgsGraph.setShowLines(true);
 					dgsGraph.setShowPoints(false);
 				break;
 				case POINTS:
 					dgsGraph.setShowFaces(false);
 					dgsGraph.setShowLines(false);
 					dgsGraph.setShowPoints(true);					
 				break;
 			}
 			currentStyle = newStyle;
 		}
 	}
 
 	@Override
 	public void resetView() {
 		Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
         sceneCamera.setFieldOfView(56.5f);
 	}
 
 	@Override
 	public ScaleType getScaling() {
 		return zScaling;
 	}
 
 	@Override
 	public List<AxisValues> getAxisValues() {
 		return null;
 	}
 
 	private void setScalingSmallFlag(double value) {
 		switch (zScaling) {
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
 	
 	@Override
 	public void toggleErrorBars(boolean xcoord, boolean ycoord, boolean zcoord) {
 		// TODO Auto-generated method stub	
 		// not yet implemented
 	}
 	
 }
