 /*
  * Last modification information:
 * $Revision: 1.10 $
 * $Date: 2004-09-10 15:13:33 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.ui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import javax.swing.*;
 
 import java.util.*;
 
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.framework.data.stream.*;
 import org.concord.graph.ui.*;
 import org.concord.graph.engine.*;
 import org.concord.graph.event.GraphWindowListener;
 import org.concord.graph.event.GraphWindowResizeEvent;
 import org.concord.graph.examples.*;
 
 /**
  * DataGraph
  * This is a panel with a graph and a toolbar
  * The graph has one default graph area that is not the whole window,
  * The grid is drawn at the beginning (LEFT and BOTTOM)
  * and there is some space left for the axis labels.
  *
  * Date created: June 18, 2004
  *
  * @author Scott Cytacki<p>
  * @author Ingrid Moncada<p>
  *
  */
 
 public class DataGraph extends JPanel
 	implements DataConsumer, GraphWindowListener
 {
 	//Graph, grid and toolbar
 	protected GraphWindow graph;
 	protected Grid2D grid;
 	protected GraphWindowToolBar toolBar;
 	
 	protected Hashtable sources = new Hashtable();
 
 	protected boolean selectionMode = true;
 
 	protected GraphableList objList;
 	
 	protected DefaultCoordinateSystem2D defaultCS;
 	protected GraphArea defaultGA;
 	
 	protected boolean adjustOriginOnReset = true;
 	
 	protected DashedBox selectionBox;
 	protected boolean limitsSet = false;
 	
 	public DataGraph()
 	{
 		////////
 		// Graph
 		//Create the graph
 		graph = new GraphWindow();
 		defaultGA = graph.getDefaultGraphArea();
 		CoordinateSystem cs = defaultGA.getCoordinateSystem();
 		//Make sure we are using a DefaultCoordinateSystem2D
 		if (!(cs instanceof DefaultCoordinateSystem2D)){
 			graph.setDefaultGraphArea(new GraphArea(new DefaultCoordinateSystem2D()));
 		}
 		defaultGA = graph.getDefaultGraphArea();
 		defaultCS = (DefaultCoordinateSystem2D)defaultGA.getCoordinateSystem();
 
 		defaultGA.setInsets(new Insets(5,40,40,5));
 		
 		graph.addGraphWindowListener(this);
 		
 		//By default, the origin is the lower left corner of the graph area
 		setOriginOffsetPercentage(0,0);
 		
 		//setOriginOffsetDisplay(20, 0);
 		//defaultGA.setYCentered(true);
 		////////
 		
 		////////
 		// Grid
 		grid = createGrid();
 		
 		//Add the grid to the graph
 		graph.addDecoration(grid);
 		////////
 		
 		////////
 		// Tool Bar
 		toolBar = new GraphWindowToolBar();
 		toolBar.setGraphWindow(graph);
 		toolBar.setButtonsMargin(0);
 		toolBar.setFloatable(false);
 		////////
 
 		selectionBox = new DashedBox();
 		selectionBox.setVisible(false);
 		graph.add(selectionBox);
 		
 		////////
 		// List of Graphable Objects
 		objList = new SelectableList();
 		graph.add(objList);
 		////////
 
 		setLayout(new BorderLayout());
 		add(graph);
 		add(toolBar, BorderLayout.EAST);		
 		
 		initScaleObject();
 	}
 	
 	protected Grid2D createGrid()
 	{
 		Grid2D gr = new Grid2D();
 		//gr.setInterval(1.0,1.0);
 		//gr.setLabelFormat(new DecimalFormat("#"));
 		gr.getXGrid().setAxisLabelSize(12);
 		gr.getYGrid().setAxisLabelSize(12);
 		
 		gr.getXGrid().setAxisDrawMode(SingleAxisGrid.BEGINNING);
 		gr.getYGrid().setAxisDrawMode(SingleAxisGrid.BEGINNING);
 		
 		gr.getXGrid().setDrawGridOnAxis(true);
 		gr.getYGrid().setDrawGridOnAxis(true);
 		
 		return gr;
 	}
 
 	protected void initScaleObject()
 	{
 		addScaleAxis(defaultGA);
 	}
 	
 	protected void addScaleAxis(GraphArea ga)
 	{
 		//Adding the scaling object for the graph area
 		AxisScale axisScale = new AxisScale();
 		axisScale.setGraphArea(ga);
 		axisScale.setDragMode(AxisScale.DRAGMODE_NONE);
 		axisScale.setShowMessage(false);
 		axisScale.setShowCover(false);
 		graph.add(axisScale);
 		toolBar.addAxisScale(axisScale);
 	}
 	
 	public GraphWindow getGraph()
 	{
 		return graph;
 	}
 	
 	public Grid2D getGrid()
 	{
 		return grid;
 	}
 
 	public void setSelectionMode(boolean mode)
 	{
 		selectionMode = mode;
 	}
 
 /*
 	public void mouseDragged(MouseEvent e)
 	{
 		if (!selectionMode) return;
 
 		Point2D.Double draggedWorldPoint = new Point2D.Double();
 
 
 		Point2D.Double mousePoint = new Point2D.Double(e.getX(), e.getY());
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 		
 		coord.transformToWorld(mousePoint, draggedWorldPoint);
 
 		setSelection((float)pressedWorldPoint.x, (float)pressedWorldPoint.y, 
 					 (float)draggedWorldPoint.x-(float)pressedWorldPoint.x,
 					 (float)draggedWorldPoint.y-(float)pressedWorldPoint.y);
 	}
 
 	public void mouseMoved(MouseEvent e){}
 	public void mouseClicked(MouseEvent e){}
 	public void mouseEntered(MouseEvent e){}
 	public void mouseExited(MouseEvent e){}
 
 	Point2D.Double pressedWorldPoint = new Point2D.Double();
 	public void mousePressed(MouseEvent e)
 	{
 		if (!selectionMode) return;
 		
 		Point2D.Double mousePoint = new Point2D.Double(e.getX(), e.getY());
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 		coord.transformToWorld(mousePoint, pressedWorldPoint);
 		
 		setSelection((float)pressedWorldPoint.x, (float)pressedWorldPoint.y, 0, 0);
 	}
 
 	public void mouseReleased(MouseEvent e)
 	{
 
 	}
 */	
 	public void zoomSelection()
 	{
 		selectionBox.zoom();		
 	}
 
 	/**
 	 * Sets the scale of the coordinate system given the number of pixels
 	 * desired in a world unit
 	 * @param xScale	Number of pixels per world unit (x direction)
 	 * @param yScale	Number of pixels per world unit (y direction)
 	 */
 	public void setScale(double xScale, double yScale)
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 
 		Point2D.Double scale = new Point2D.Double(xScale, yScale);
 		coord.setScale(scale);
 	}
 	
 	public double getXScale()
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 		
 		return coord.getScale().getX();
 	}
 
 	public double getYScale()
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 		
 		return coord.getScale().getY();
 	}
 	
 	/**
 	 * Sets the position of the axes' origin, relative to
 	 * the UPPER LEFT corner of the graph area
 	 * @param xPos	Position of the origin in the x direction, relative to the left edge (DISPLAY COORDINATES)
 	 * @param yPos	Position of the origin in the x direction, relative to the left edge (DISPLAY COORDINATES)
 	 */
 	public void setOriginOffsetDisplay(double xPos, double yPos)
 	{
 		setOriginOffsetPercentage(-1, -1);
 
 		//xPos and yPos are relative to the UPPER LEFT CORNER of the window
 		xPos = xPos + defaultGA.getInsets().left;
 		yPos = yPos + defaultGA.getInsets().top;
 		
 		Point2D.Double origin = new Point2D.Double(xPos, yPos);
 		defaultCS.setOriginOffsetDisplay(origin);
 	}
 	
 	/**
 	 * Sets the percentage of the position of the axes' origin, relative to
 	 * the LOWER LEFT corner of the window
 	 * The parameters (between 0 and 1) represent values as a 
 	 * PERCENTAGE of the SIZE of the graph area.
 	 * 0 means the origin will be AT the LOWER LEFT corner
 	 * 1 means it will be at the opposite corner
 	 * 0.2 means the origin will be at a distance of 20% the size of the grap area, 
 	 * from the lower left corner
 	 * @param originPercentageX	Distance (x direction) from the origin to the left edge, as a percentage of
 	 * 							the WIDTH of the graph area (a value from 0 to 1)
 	 * @param originPercentageY	Distance (y direction) from the origin to the lower edge, as a percentage of
 	 * 							the HEIGHT of the graph area (a value from 0 to 1)
 	 */
 	public void setOriginOffsetPercentage(double originPercentageX, double originPercentageY)
 	{
 		getGraph().getDefaultGraphArea().setOriginPositionPercentage(originPercentageX, originPercentageY);
 	}
 	
 	public double getXOriginOffsetDisplay()
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 
 		double xPos = coord.getOriginOffsetDisplay().getX();
 		
 		//xPos is relative to the UPPER LEFT CORNER of the window
 		xPos = xPos - defaultGA.getInsets().left;
 		
 		return xPos;		
 	}
 
 	public double getYOriginOffsetDisplay()
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 
 		double yPos = coord.getOriginOffsetDisplay().getY(); 
 			
 		//yPos is relative to the UPPER LEFT CORNER of the window
 		yPos = yPos - defaultGA.getInsets().top;
 		
 		return yPos;		
 	}
 	
 	public DataGraphable getGraphable(DataProducer source)
 	{
 		return (DataGraphable)sources.get(source);
 	}
 
 	public void reset()
 	{
 		//Reset each data graphable
 		for(int i=0; i<objList.size(); i++)
 		{
 			if(objList.elementAt(i) instanceof DataGraphable)
 			{
 				DataGraphable dGraphable = (DataGraphable)objList.elementAt(i);
 				dGraphable.reset();
 			}
 		}
 		
 		resetGraphArea();
 	}
 	
 	protected void resetGraphArea()
 	{
 		//Reset graph areas
 		if (adjustOriginOnReset){
 			defaultGA.adjustCoordinateSystem();
 		}
 	}
 	
 	public void setSelection(float x, float y, float width, float height)
 	{
 		if(width < 0)
 		{
 			width=-width;
 			x-=width;
 		}
 		if(height < 0)
 		{
 			height=-height;
 			y-=height;
 		}
 
 		selectionBox.setBounds(x,y,width,height);
 	}
 
 	/**
 	 * Sets up the axis of the graph.
 	 * It will display values from minX to maxX on the X axis and
 	 * from minY to maxY on the Y axis
 	 * The values are ALL in WORLD COORDINATES!
 	 * @param minX	minimum value displayed in the x axis (WORLD COORDINATES)
 	 * @param maxX	maximum value displayed in the x axis (WORLD COORDINATES)
 	 * @param minY	minimum value displayed in the y axis (WORLD COORDINATES)
 	 * @param maxY	maximum value displayed in the y axis (WORLD COORDINATES)
 	 */
 	public void setLimitsAxisWorld(double minX, double maxX, double minY, double maxY)
 	{
 		setOriginOffsetPercentage(-1, -1);
 		
 		setSelection((float)minX, (float)minY, (float)(maxX - minX), (float)(maxY - minY));
 		zoomSelection();
 		
 		limitsSet = true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.datastream.DataConsumer#addDataSource(org.concord.framework.datastream.DataProducer)
 	 */
 	public void addDataProducer(DataProducer source)
 	{
 		addDataProducer(source, defaultGA);		
 	}
 
 	/**
 	 * 
 	 * @param source
 	 * @param ga
 	 */
 	protected void addDataProducer(DataProducer source, GraphArea ga)
 	{
 		// Create a graphable for this datasource
 		// add it to the graph
 		DataGraphable dGraphable = new DataGraphable();
 		dGraphable.setDataProducer(source);
 		
 		dGraphable.setGraphArea(ga);
 		
 		objList.add(dGraphable);
 		sources.put(source, dGraphable);		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.datastream.DataConsumer#removeDataSource(org.concord.framework.datastream.DataProducer)
 	 */
 	public void removeDataProducer(DataProducer source)
 	{
 
 		// TODO Auto-generated method stub
 		// remove the associated data source from 
 		// the graph
 		DataGraphable dGraphable = (DataGraphable)sources.get(source);
 		if (dGraphable != null){
 			dGraphable.setDataProducer(null);
 			objList.remove(dGraphable);
 		}
 	}
 
 	/**
 	 * Creates a data graphable that will graph the data coming from the specified
 	 * data source, using channelXAxis as the index for the channel that will be in the x axis
 	 * of the graph, and channelYAxis as the index for the channel that will be in the y axis.
 	 * If one of the indexes is -1, it will take the dt as the data for that axis
 	 * This data graphable can then be added to the graph
 	 * @param source
 	 * @param channelXAxis
 	 * @param channelYAxis
 	 */
 	public DataGraphable createDataGraphable(DataProducer source, int channelXAxis, int channelYAxis)
 	{
 		// Create a graphable for this datasource
 		// add it to the graph
 		DataGraphable dGraphable = new DataGraphable();
 		dGraphable.setDataProducer(source);
 		dGraphable.setChannelX(channelXAxis);
 		dGraphable.setChannelY(channelYAxis);
 		
 		return dGraphable;
 	}
 	
 	/**
 	 * Adds a data graphable to the list of graphables
 	 * @param graphable
 	 */
 	public void addDataGraphable(DataGraphable graphable)
 	{
 		if (graphable.getGraphArea() == null){
 			graphable.setGraphArea(defaultGA);
 		}
 		objList.add(graphable);
 	}
 	
 	public GraphableList getObjList()
 	{
 		return objList;
 	}
 
 	public GraphWindowToolBar getToolBar()
 	{
 		return toolBar;
 	}
 
 	/**
 	 * @return Returns the adjustOnReset.
 	 */
 	public boolean isAdjustOriginOffsetOnReset()
 	{
 		return adjustOriginOnReset;
 	}
 
 	/**
 	 * @param adjustOnReset The adjustOnReset to set.
 	 */
 	public void setAdjustOriginOffsetOnReset(boolean adjustOnReset)
 	{
 		this.adjustOriginOnReset = adjustOnReset;
 	}
 	
 	//Testing purposes
     public static void main(String args[]) {
 		final JFrame frame = new JFrame();
 		final JPanel fa = new DataGraph();
 		frame.getContentPane().add(fa);
 		frame.setSize(800,600);
 		frame.show();
 		
 		frame.addWindowListener( new WindowAdapter() {
 			public void windowClosing(WindowEvent e){				
 				System.exit(0);
 			}			
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.graph.event.GraphWindowListener#windowChanged(java.util.EventObject)
 	 */
 	public void windowChanged(EventObject e)
 	{
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.graph.event.GraphWindowListener#windowResized(org.concord.graph.event.GraphWindowResizeEvent)
 	 */
 	public void windowResized(GraphWindowResizeEvent e)
 	{
 		if (limitsSet){
 			zoomSelection();
 		}
 	}
 }
