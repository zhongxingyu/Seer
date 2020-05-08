 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.48 $
  * $Date: 2007-08-30 21:03:21 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.geom.Point2D;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import org.concord.datagraph.engine.DataGraphAutoScaler;
 import org.concord.datagraph.engine.DataGraphAutoScroller;
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.datagraph.engine.DataGraphableEx;
 import org.concord.framework.data.DataFlow;
 import org.concord.framework.data.DataFlowCapabilities;
 import org.concord.framework.data.stream.DataConsumer;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.graph.engine.AxisScale;
 import org.concord.graph.engine.CoordinateSystem;
 import org.concord.graph.engine.DefaultCoordinateSystem2D;
 import org.concord.graph.engine.GraphArea;
 import org.concord.graph.engine.GraphableList;
 import org.concord.graph.engine.MultiRegionAxisScale;
 import org.concord.graph.engine.SelectableList;
 import org.concord.graph.examples.GraphWindowToolBar;
 import org.concord.graph.ui.DashedBox;
 import org.concord.graph.ui.GraphWindow;
 import org.concord.graph.ui.Grid2D;
 import org.concord.graph.ui.SingleAxisGrid;
 
 /**
  * DataGraph
  * This is a panel with a graph and a toolbar on the right
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
 	implements DataFlow, DataConsumer, DataFlowCapabilities
 {
 	/**
      * This class isn't intended to be serialized but 
      * this removed some compiler warnings.
      */
     private static final long serialVersionUID = 1L;
     
     public final static int AUTO_FIT_NONE = 0;
 	public final static int AUTO_SCALE_MODE = 1;
 	public final static int AUTO_SCROLL_MODE = 2;	
 	public final static int AUTO_SCROLL_RUNNING_MODE = 3;
 	
 	//Graph, grid and toolbar
 	protected GraphWindow graph;
 	protected Grid2D grid;
 	protected GraphWindowToolBar toolBar;
 	protected Vector axisScaleObjs = new Vector();
 	
 	
 	protected Hashtable producers = new Hashtable();
 
 	protected GraphableList objList;
 	protected GraphableList backgroundList;
 	
 	protected boolean adjustOriginOnReset = true;
 	
 	protected DashedBox selectionBox;
 
 	protected DataGraphAutoScaler scaler = null;
 	protected DataGraphAutoScroller scroller = null;
     private int autoFitMode;
     private JLabel titleLabel;		
     private boolean useDataGraphableWithShapes = false; 
 	
 	/**
 	 * Creates a default data graph with or without a tool bar
 	 * @param showToolbar	indicates if the toolbar should be visible or not
 	 */
 	public DataGraph(boolean showToolbar)
 	{
 		this();
 		toolBar.setVisible(showToolbar);
 	}
 	
 	/**
 	 * Creates a default data graph that will have: a GraphWindow with a Grid2D that displays
 	 * the x axis at the bottom and the y axis at the left, with the origin at the bottom left
 	 * corner of the graph, with a default scale of 20 pixels per unit, with a default graph area
 	 * in the middle or the graph that has a margin of 5 pixels on the top/bottom, and 40 pixels 
 	 * on the side. 
 	 * It also has a toolbar to control the graph.   
 	 */
 	public DataGraph()
 	{
 		////////
 		// Graph
 		//Create the graph
 		graph = new GraphWindow();
 		GraphArea defaultGA = graph.getDefaultGraphArea();
 		CoordinateSystem defaultCS = defaultGA.getCoordinateSystem();
 		//Make sure we are using a DefaultCoordinateSystem2D
 		if (!(defaultCS instanceof DefaultCoordinateSystem2D)){
 			graph.setDefaultGraphArea(new GraphArea(new DefaultCoordinateSystem2D()));
 		}
 		defaultGA = graph.getDefaultGraphArea();
 		defaultCS = (DefaultCoordinateSystem2D)defaultGA.getCoordinateSystem();
 
 		defaultGA.setInsets(new Insets(10,50,40,10));
 		
 		//By default, the origin is the lower left corner of the graph area
 		setOriginOffsetPercentage(0,0);
 		
 		//setOriginOffsetDisplay(20, 0);
 		//defaultGA.setYCentered(true);
 		////////
 		
 		////////
 		// Grid
 		grid = createGrid();
 		grid.getXGrid().setAutoFormatLabels(true);
 		grid.getYGrid().setAutoFormatLabels(true);
 		
 		//Add the grid to the graph
 		graph.addDecoration(grid);
 		////////
 		
 		selectionBox = new DashedBox();
 		selectionBox.setVisible(false);
 		graph.add(selectionBox);
 
 		backgroundList = new SelectableList();
 		graph.add(backgroundList);
 		
 		////////
 		// List of Graphable Objects
 		objList = new SelectableList();
 		
 		// set the label of this list so if someone is debugging
 		// this they can identify this list easily
         objList.setLabel("DataGraphables");
 		graph.add(objList);
 		////////
 		
 		setLayout(new BorderLayout());
 		add(graph);
 
 		////////
 		// Tool Bar
 		GraphWindowToolBar gwToolBar = new GraphWindowToolBar();		
 		gwToolBar.setButtonsMargin(0);
 		gwToolBar.setFloatable(false);
 		setToolBar(gwToolBar);
 		////////
 
 		initScaleObject();
 	}
 	
 	public void setTitle(String title)
 	{
 	    if(titleLabel == null){
 	        titleLabel = new JLabel(title);
 	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
 	        titleLabel.setBackground(Color.WHITE);
 	        titleLabel.setOpaque(true);
 	        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
 	        add(titleLabel, BorderLayout.NORTH);
 	    } else {
 	        titleLabel.setText(title);
 	    }
 	}
 	
 	/**
 	 *
 	 */
 	public void changeToDataGraphToolbar()
 	{	
 		DataGraphToolbar dgToolbar = new DataGraphToolbar();
 		dgToolbar.setButtonsMargin(0);
 		dgToolbar.setFloatable(false);
 				
 		setToolBar(dgToolbar);
 	}
 		
 	protected Grid2D createGrid()
 	{		
 		Grid2D gr = new Grid2D(new SingleDataAxisGrid(1), 
 				new SingleDataAxisGrid(2));
 		//gr.setInterval(1.0,1.0);
 		//gr.setLabelFormat(new DecimalFormat("#"));
 		gr.getXGrid().setAxisLabelSize(12);
 		gr.getYGrid().setAxisLabelSize(12);
 		
 		gr.getXGrid().setAxisDrawMode(SingleAxisGrid.BEGINNING);
 		gr.getYGrid().setAxisDrawMode(SingleAxisGrid.BEGINNING);
 		
 		gr.getXGrid().setDrawGridOnAxis(true);
 		gr.getYGrid().setDrawGridOnAxis(true);
 		
 		gr.useAutoTickScaling();
 		
 		return gr;
 	}
 
 	protected void initScaleObject()
 	{
 		addScaleAxis(getGraphArea());
 	}
 	
 	protected void addScaleAxis(GraphArea ga)
 	{
 		//Adding the scaling object for the graph area
 		AxisScale axisScale = new MultiRegionAxisScale(getGrid());
 		axisScale.setGraphArea(ga);
 		axisScale.setDragMode(AxisScale.DRAGMODE_NONE);
 		axisScale.setShowMessage(false);
 		axisScale.setShowCover(false);
 		axisScale.setOriginDragFixPoint(false);
 		graph.add(axisScale);
 		axisScaleObjs.add(axisScale);
 		toolBar.addAxisScale(axisScale);
 	}
 	
 	/**
 	 * Returns the graph of this data graph (a GraphWindow object)
 	 * This is useful if you need to set the properties of the graph directly
 	 * @return
 	 */
 	public GraphWindow getGraph()
 	{
 		return graph;
 	}
 	
 	/**
 	 * Returns the grid used in this data graph
 	 * @return
 	 */
 	public Grid2D getGrid()
 	{
 		return grid;
 	}
 
 	public GraphArea getGraphArea()
 	{
 		return graph.getDefaultGraphArea();
 	}
 	
 	public DefaultCoordinateSystem2D getCoordinateSystem()
 	{
 		CoordinateSystem cs = getGraphArea().getCoordinateSystem();
 		if (!(cs instanceof DefaultCoordinateSystem2D)) return null;
 		return (DefaultCoordinateSystem2D)cs;
 	}
 	
 	public void setGraphArea(GraphArea ga)
 	{
 		graph.setDefaultGraphArea(ga);
 		
 		//Correct the grid
 		grid.setGraphArea(ga);
 		
 		//Correct the graphables?? NO for now
 		
 		//Correct the axis scale objects
 		for (int i=0; i<axisScaleObjs.size(); i++){
 			AxisScale ax = (AxisScale)axisScaleObjs.elementAt(i);
 			ax.setGraphArea(ga);
 		}
 	}
 	
 	/**
 	 * Sets the selection on this data graph
 	 */
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
 	 * Zooms to the selection of this data graph (set with setSelection()) 
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
 	
 	/**
 	 * Returns the scale of the x axis
 	 * @return
 	 */
 	public double getXScale()
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 		
 		return coord.getScale().getX();
 	}
 
 	/**
 	 * Returns the scale of the y axis
 	 * @return
 	 */
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
 		xPos = xPos + getGraphArea().getInsets().left;
 		yPos = yPos + getGraphArea().getInsets().top;
 		
 		Point2D.Double origin = new Point2D.Double(xPos, yPos);
 		getCoordinateSystem().setOriginOffsetDisplay(origin);
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
 		xPos = xPos - getGraphArea().getInsets().left;
 		
 		return xPos;		
 	}
 
 	public double getYOriginOffsetDisplay()
 	{
 		CoordinateSystem coord = getGraph().getDefaultGraphArea().getCoordinateSystem();
 
 		double yPos = coord.getOriginOffsetDisplay().getY(); 
 			
 		//yPos is relative to the UPPER LEFT CORNER of the window
 		yPos = yPos - getGraphArea().getInsets().top;
 		
 		return yPos;		
 	}
 		
 	/**
 	 * Scroll the graph area back so the x value starts at 0,
 	 * keep the scale the same.
 	 * You can use the org.concord.graph.engine.GraphArea.
 	 * @param graphArea
 	 */
 	protected void resetGraphArea(GraphArea graphArea)
 	{
 	    graphArea.setOriginPositionPercentage(0,-1);	    
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
 	    getGraph().getDefaultGraphArea().setAutoCSMode(
 	            GraphArea.FIXED_AXIS_LIMITS);
 	    getGraph().getDefaultGraphArea().
 	    	setLimitsAxisWorld(minX, maxX, minY, maxY);	    
 	}
 
 	/**
 	 * Returns the current minimum x value shown in the X Axis
 	 * @return
 	 */
 	public double getMinXAxisWorld()
 	{
 		return getGraphArea().getLowerLeftCornerWorld().getX();
 	}
 	
 	/**
 	 * Returns the current maximum x value shown in the X Axis
 	 * @return
 	 */
 	public double getMaxXAxisWorld()
 	{
 		return getGraphArea().getUpperRightCornerWorld().getX();
 	}
 
 	/**
 	 * Returns the current minimum y value shown in the Y Axis
 	 * @return
 	 */
 	public double getMinYAxisWorld()
 	{
 		return getGraphArea().getLowerLeftCornerWorld().getY();
 	}
 	
 	/**
 	 * Returns the current minimum x value shown in the X Axis
 	 * @return
 	 */
 	public double getMaxYAxisWorld()
 	{
 		return getGraphArea().getUpperRightCornerWorld().getY();
 	}
 	
 	/**
 	 * Adds to the graph a data graphable associated with the specified data producer
 	 * By default, it will create a DataGraphable with -1 as the channel for the x axis (dt)
 	 * and 0 as the channel of the y axis,
 	 * so that means it will graph dt vs. channel 0
 	 * @param dataProducer data producer that will produce the data for the data graphable
 	 * @see org.concord.framework.datastream.DataConsumer#addDataProducer(org.concord.framework.datastream.DataProducer)
 	 */
 	public void addDataProducer(DataProducer dataProducer)
 	{
 		addDataProducer(dataProducer, getGraphArea());		
 	}
 
 	/**
 	 * Adds to the graph a data graphable associated with the specified data producer
 	 * @param dataProducer
 	 * @param ga
 	 */
 	protected void addDataProducer(DataProducer dataProducer, GraphArea ga)
 	{
 		// Create a graphable for this data Producer
 		// add it to the graph
 		DataGraphable dGraphable = createDataGraphable(dataProducer);
 		
 		dGraphable.setGraphArea(ga);
 		
 		objList.add(dGraphable);
 	}
 	
 	/**
 	 * Removes the first Data Graphable associated with the specified Data Producer
 	 * @see org.concord.framework.datastream.DataConsumer#removeDataProducer(org.concord.framework.datastream.DataProducer)
 	 */
 	public void removeDataProducer(DataProducer dataProducer)
 	{
 		//TODO it should use getGraphables (should return a Vector) and it should remove 
 		//ALL the graphables associated with the data producer, not only the first one  
 		//remove the associated dataProducer from 
 		//the graph
 		DataGraphable dGraphable = getGraphable(dataProducer);
 		if (dGraphable != null){
 			dGraphable.setDataProducer(null);
 			objList.remove(dGraphable);
 		}
 	}
 
 	/**
 	 * Returns the first Data Graphable in the graph that is associated with the specified
 	 * data producer. 
 	 * @param dataProducer
 	 * @return
 	 */
 	public DataGraphable getGraphable(DataProducer dataProducer)
 	{
 		DataGraphable dGraphable = (DataGraphable)producers.get(dataProducer);
 		if (dGraphable == null){
 			//Look for the first data graphable that has a data producer == dataProducer
 			//TODO: it should actually return a list of graphables that have that data producer
 			//specially for removeDataProducer
 			//I think the vector thing should be returned in a new method: getGraphables()
 			for (int i=0; i < objList.size(); i++){
 				Object obj = objList.elementAt(i);
 				if (obj instanceof DataGraphable){
 					dGraphable = (DataGraphable)obj;
                     DataProducer gDataProducer = dGraphable.findDataProducer();
                     if(gDataProducer != null &&
                             gDataProducer == dataProducer) {
                         return dGraphable;
                     }                    
 				}
 			}
 		}
 		return dGraphable;
 	}
 	
 	/**
 	 * Creates a data graphable that will graph the data coming from the specified
 	 * data producer, using channelXAxis as the index for the channel that will be in the x axis
 	 * of the graph, and channelYAxis as the index for the channel that will be in the y axis.
 	 * If one of the indexes is -1, it will take the dt as the data for that axis
 	 * This data graphable can then be added to the graph, is NOT added
 	 * automatically.
 	 * 
 	 * @param dataProducer
 	 * @param channelXAxis
 	 * @param channelYAxis
 	 */
 	public DataGraphable createDataGraphable(DataProducer dataProducer, int channelXAxis, int channelYAxis)
 	{
 		// Create a graphable for this dataProducer
 		// add it to the graph
 		DataGraphable dGraphable = (!useDataGraphableWithShapes)?new DataGraphable():new DataGraphableEx();
 		dGraphable.setDataProducer(dataProducer);
 		dGraphable.setChannelX(channelXAxis);
 		dGraphable.setChannelY(channelYAxis);
 		
 		producers.put(dataProducer, dGraphable);
 		
 		return dGraphable;
 	}
 	
 	/**
 	 * Creates a data graphable that will graph the data coming from the specified
 	 * data store, using channelXAxis as the index for the channel that will be in the x axis
 	 * of the graph, and channelYAxis as the index for the channel that will be in the y axis.
 	 * If one of the indexes is -1, it will take the dt as the data for that axis
 	 * This data graphable can then be added to the graph, it is NOT added
 	 * automatically.
 	 * @param dataStore
 	 * @param channelXAxis
 	 * @param channelYAxis
 	 */
 	public DataGraphable createDataGraphable(DataStore dataStore, int channelXAxis, int channelYAxis)
 	{
 		// Create a graphable for this dataProducer
 		// add it to the graph
 		DataGraphable dGraphable = (!useDataGraphableWithShapes)?new DataGraphable():new DataGraphableEx();
 		dGraphable.setDataStore(dataStore);
 		dGraphable.setChannelX(channelXAxis);
 		dGraphable.setChannelY(channelYAxis);
 		
 		return dGraphable;
 	}
 	
 	/**
 	 * Creates a data graphable that will graph the data coming from the specified
 	 * data store, using the first 2 channels of the data store (0 and 1) 
 	 * It will use 0 as the index for the channel that will be in the x axis
 	 * of the graph, and 1 as the index for the channel that will be in the y axis.
 	 * This data graphable can then be added to the graph
 	 * @param dataStore
 	 */
 	public DataGraphable createDataGraphable(DataStore dataStore)
 	{
 		return createDataGraphable(dataStore, 0, 1);
 	}
 	
 	/**
 	 * Creates a data graphable that will graph the data coming from the specified
 	 * data producer, using dt and the first channel for x and y respectively. 
 	 * It will use -1 as the index for the channel that will be in the x axis (dt)
 	 * of the graph, and 0 as the index for the channel that will be in the y axis.
 	 * This data graphable can then be added to the graph
 	 * @param dataStore
 	 */
 	public DataGraphable createDataGraphable(DataProducer dataProducer)
 	{
 		return createDataGraphable(dataProducer, -1, 0);
 	}
 	
 	/**
 	 * Adds a data graphable to the list of graphables
 	 * @param graphable data graphable to add
 	 */
 	public void add(DataGraphable graphable)
 	{
 		addDataGraphable(graphable);
 	}
 	
 	/**
 	 * Adds a data graphable to the list of graphables
 	 * @param graphable data graphable to add
 	 */
 	public void addDataGraphable(DataGraphable graphable)
 	{
 		if (graphable.getGraphArea() == null){
 			graphable.setGraphArea(getGraphArea());
 		}
 		objList.add(graphable);
 	}
 		
 	/**
 	 * Removes a data graphable from the list of graphables
 	 * @param graphable data graphable to remove
 	 */
 	public void removeDataGraphable(DataGraphable graphable)
 	{
 		objList.remove(graphable);
 	}
 	
 	/**
 	 * Adds a data graphable to the list of background graphables
 	 * @param graphable data graphable to add
 	 */
 	public void addBackgroundDataGraphable(DataGraphable graphable)
 	{
 		if (graphable.getGraphArea() == null){
 			graphable.setGraphArea(getGraphArea());
 		}
 		backgroundList.add(graphable);
 	}
 		
 	/**
 	 * Removes a data graphable from the list of background 
 	 * graphables
 	 * @param graphable data graphable to remove
 	 */
 	public void removeBackgroundDataGraphable(DataGraphable graphable)
 	{
 		backgroundList.remove(graphable);
 	}
 
 	/**
 	 * Returns the top-level list of foreground graphables of this graph 
 	 * @return
 	 */
 	public GraphableList getObjList()
 	{
 		return objList;
 	}
 
 	/**
 	 * Returns the toolbar of this graph
 	 * @return
 	 */
 	public GraphWindowToolBar getToolBar()
 	{
 		return toolBar;
 	}
 
 	/**
 	 * Sets the tool bar of this graph
 	 * this sets the graphwindow and grid of the 
 	 * tool bar to be the graphwindow and grid
 	 * of this data graph.
 	 * It also adds the toolbar to this panel.
 	 * 
 	 * @param gwToolbar
 	 */
 	public void setToolBar(GraphWindowToolBar gwToolbar, boolean bAddToPanel)
 	{
 	    if(toolBar != null) {
 	        // remove references to this graph from 
 	        // the old toolbar
 	        toolBar.setGraphWindow(null);
 	        toolBar.setGrid(null);
 	        remove(toolBar);
 	    }
 	    
 	    toolBar = gwToolbar;
 
 	    if(toolBar != null) {
 			toolBar.setGraphWindow(graph);
 			toolBar.setGrid(grid);
 			
 			for(int i=0; i<axisScaleObjs.size(); i++){
 			    toolBar.addAxisScale((AxisScale)axisScaleObjs.get(i));
 			}
 			
 			if (bAddToPanel){
 				add(toolBar, BorderLayout.EAST);
 			}
 	    }
 	}
 	
 	/**
 	 * Sets the tool bar of this graph
 	 * this sets the graphwindow and grid of the 
 	 * tool bar to be the graphwindow and grid
 	 * of this data graph.
 	 * It also adds the toolbar to this panel.
 	 * 
 	 * @param gwToolbar
 	 */
 	public void setToolBar(GraphWindowToolBar gwToolbar)
 	{
 		setToolBar(gwToolbar, true);
 	}
 	
 	/**
 	 * Returns if the graph should adjust the origin offset to the original origin offset
 	 * when the graph is reset
 	 * @return 
 	 */
 	public boolean isAdjustOriginOffsetOnReset()
 	{
 		return adjustOriginOnReset;
 	}
 
 	/**
 	 * Sets if the graph should adjust the origin offset to the original origin offset
 	 * when the graph is reset
 	 * @param adjustOnReset 
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
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	}
 
 	/**
 	 * @see org.concord.framework.data.DataFlow#stop()
 	 */
 	public void stop()
 	{
 	    if(autoFitMode == AUTO_SCROLL_RUNNING_MODE){
 	        scroller.setEnabled(false);
 	    }
 	}
 
 	/**
 	 * @see org.concord.framework.data.DataFlow#start()
 	 */
 	public void start()
 	{
 	    if(autoFitMode == AUTO_SCROLL_RUNNING_MODE){
 	        scroller.setEnabled(true);
 	    }
 	}
 	
 	/**
 	 * Resets all the graphables in the data graph
 	 * @see org.concord.framework.data.DataFlow#reset()
 	 */
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
 		
 		if (adjustOriginOnReset){
 		    resetGraphArea(getGraphArea());
 		}
 	}
 
 	/**
 	 * sets the mode used to resize the graph as new points are added
 	 * AUTO_FIT_NONE - don't resize the graph automatically
 	 * AUTO_SCALE_MODE - change the scale of the graph so all the data is
 	 *   visible.  If you want to customize the behavior of this mode
 	 *   you can use the getAutoScaler() method and customize the returned
 	 *   object.
 	 * AUTO_SCROLL_MODE - change the position of the display origin,
 	 *   so new data is visible. If you want to customize the behavior of
 	 *   this mode you can use the getAutoScroller() method and customize
 	 *   the returned object.
 	 * AUTO_SCROLL_RUNNING_MODE - turn on auto scroll when the graph 
 	 *   is started.  This is only useful for data producers. 
 	 */
 	public void setAutoFitMode(int mode)
 	{
 	    autoFitMode = mode;
 	    
 		switch(mode){
 			case AUTO_FIT_NONE:
 				if (scaler != null) {
 					scaler.setEnabled(false);
 				}
 				if (scroller != null) {
 					scroller.setEnabled(false);
 				}
 				break;
 			case AUTO_SCALE_MODE:
 				getAutoScaler();
 				scaler.setEnabled(true);
 				if (scroller != null) {
 					scroller.setEnabled(false);
 				}
 				break;
 			case AUTO_SCROLL_MODE:
 				getAutoScroller();
 				scroller.setEnabled(true);
 				if (scaler != null) {
 					scaler.setEnabled(false);
 				}
 				break;
 			case AUTO_SCROLL_RUNNING_MODE:
 				getAutoScroller();
 				scroller.setEnabled(false);
 				if (scaler != null) {
 					scaler.setEnabled(false);
 				}
 				break;
 			default:
 				throw new RuntimeException("Invalid fit mode: " + mode);
 		}
 	}
 	
 	/**
 	 * Get the object used for auto scaling.  This object can be
 	 * configured to change the behavior of auto scaling.
 	 * @return
 	 */
 	public DataGraphAutoScaler getAutoScaler()
 	{
 		if(scaler == null) {
 			scaler = new DataGraphAutoScaler();
 			scaler.setGraph(this);
 			scaler.setGraphables(getObjList());
 			scaler.setEnabled(false);
 		}
 		return scaler;
 	}
 	
 	/**
 	 * Get the object used for auto scrolling.  This object can be
 	 * configured to change the behavior of auto scrolling.
 	 * @return
 	 */
 	public DataGraphAutoScroller getAutoScroller()
 	{
 		if(scroller == null) {
 			// This needs to be adjust based on the graph dimensions
 			scroller = new DataGraphAutoScroller(6,4);
 			scroller.setGraph(this);
 			scroller.setGraphables(getObjList());
 			scroller.setMinXValue(0);
 			// This needs to be adjust based on the graph dimensions
 			scroller.setXPaddingPercentage(5,90);
 			scroller.setEnabled(false);
 		}
 		return scroller;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.DataFlowCapabilities#getDataFlowCapabilities()
 	 */
 	public DataFlowCapabilities.Capabilities getDataFlowCapabilities() {
 		// TODO Auto-generated method stub
 		return new DataFlowCapabilities.Capabilities(false, false, true);
 	}	
 
 /*
  * @param useDataGraphableWithShapes if it <code>true</code>
  * internal datagraphable will be instance of DefaultGraphableEx
  * otherwise it will be instance of DefaultGraphable
  * default value is <code>false</code>
  * @see org.concord.graph.engine.DefaultGraphableEx
  */
     public void setUseDataGraphableWithShapes(boolean useDataGraphableWithShapes){
         this.useDataGraphableWithShapes = useDataGraphableWithShapes;
     }
 
 /*
  * @param useDataGraphableWithShapes if it <code>true</code>
  * @see setUseDataGraphableWithShapes
  */
     public boolean getUseDataGraphableWithShapes(){
         return useDataGraphableWithShapes;
     }
 
     public void registerDataProducerForDataGraphable(DataGraphable dGraphable){
         if(producers == null || dGraphable == null) return;
         DataProducer dataProducer = dGraphable.getDataProducer();
         if(dataProducer == null) return;
 		producers.put(dataProducer, dGraphable);
     }
 
 }
