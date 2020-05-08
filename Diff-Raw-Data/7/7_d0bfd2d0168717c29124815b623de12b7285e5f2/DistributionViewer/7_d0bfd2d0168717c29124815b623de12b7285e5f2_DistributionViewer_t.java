 /*
  * Copyright 2013 Netherlands eScience Center
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.esciencecenter.esalsa.tools;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 import nl.esciencecenter.esalsa.util.Block;
 import nl.esciencecenter.esalsa.util.Coordinate;
 import nl.esciencecenter.esalsa.util.Distribution;
 import nl.esciencecenter.esalsa.util.Grid;
 import nl.esciencecenter.esalsa.util.Layer;
 import nl.esciencecenter.esalsa.util.Layers;
 import nl.esciencecenter.esalsa.util.Line;
 import nl.esciencecenter.esalsa.util.Neighbours;
 import nl.esciencecenter.esalsa.util.Set;
 import nl.esciencecenter.esalsa.util.Topography;
 import nl.esciencecenter.esalsa.util.TopographyCanvas;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * DistributionViewer is an application used to interactively inspect a block distribution.
  * 
  * @author Jason Maassen <J.Maassen@esciencecenter.nl>
  * @version 1.0
  * @since 1.0
  * @see Set
  * @see Block
  * @see Topography
  * @see TopographyCanvas 
  *
  */
 public class DistributionViewer {
 
 	/** Logger used for debugging. */
 	private static final Logger logger = LoggerFactory.getLogger(DistributionViewer.class);
 
 	/** The line color used for clusters */
 	private final Color LINE_COLOR_CLUSTER;
 	
 	/** The color used for cluster ocean halos */
 	private final Color HALO_COLOR_CLUSTER = new Color(128, 64,  0, 96);
 
 	/** The color used for cluster land halos */
 	private final Color LAND_COLOR_CLUSTER = new Color(0, 64,  128, 32);
 
 	/** The line width used for clusters */
 	private final float LINE_WIDTH_CLUSTER;
 
 	/** The line color used for nodes */
 	private final Color LINE_COLOR_NODE;
 	
 	/** The color used for node ocean halos */
 	private final Color HALO_COLOR_NODE = new Color(160, 80,  0, 160);
 
 	/** The color used for node land halos */
 	private final Color LAND_COLOR_NODE = new Color(0, 80,  160, 80);
 
 	/** The line width used for nodes */
 	private final float LINE_WIDTH_NODE;
 
 	/** The line color used for cores */	
 	private final Color LINE_COLOR_CORE;
 	
 	/** The color used for core ocean halos */
 	private final Color HALO_COLOR_CORE = new Color(255, 128,  0, 192);
 
 	/** The color used for core land halos */
 	private final Color LAND_COLOR_CORE = new Color(0, 128, 255, 128);
 
 	/** The line width used for cores */
 	private final float LINE_WIDTH_CORE;
 
 	/** The line color used for blocks */
 	private final Color LINE_COLOR_BLOCK;
 	
 	/** The color used for block ocean halos */
 	private final Color HALO_COLOR_BLOCK   = new Color(255, 128, 0, 255);
 
 	/** The color used for block land halos */
 	private final Color LAND_COLOR_BLOCK   = new Color(0, 128, 255, 255);
 
 	/** The line width used for blocks */
 	private final float LINE_WIDTH_BLOCK;
 
 	/** The color used for selected blocks */
 	private final Color CENTER = new Color(255,0,0,210);
 
 	/** The distribution to show */ 
 	private final Distribution distribution;
 
 	/** The topography to use. */ 
 	private final Topography topography;
 
 	/** The function object use to determine block neighbors. */
 	private final Neighbours neighbours; 
 
 	/** A TopographyCanvas showing the topography */
 	private final TopographyCanvas view; 
 
 	/** The Grid to use */
 	private final Grid grid;
 
 	/** The Layers containing the various subdivisions of blocks into subsets */ 
 	private final Layers layers;
 
 	/** Used the receive mouse clicks on the TopographyView */
 	class MyListener implements MouseListener {
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			try { 
 				clicked(e.getPoint());
 			} catch (Exception ex) {
 				logger.warn("Failed to select block!", ex);
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 		}
 	}
 
 	/** 
 	 * Create a new DistributionViewer for the given topography, grid and layers. 
 	 * 
 	 * @param distribution the distribution to show. 
 	 * @param topography the topography to use. 
 	 * @param grid the grid to use. 
 	 * @param neighbours the function object use to determine block neighbors.
 	 * @param showGUI should the GUI be shown ?
	 * @param highContrast should a highContrast image be used ?
 	 * @throws Exception if the DistributionViewer could not be initialized.
 	 */
 	public DistributionViewer(Distribution distribution, Topography topography, Grid grid, Neighbours neighbours, 
			boolean showGUI, boolean highContrast) throws Exception {
 		
 		this.distribution = distribution;
 		this.topography = topography;
 		this.grid = grid;
 		this.neighbours = neighbours;		
 		this.layers = distribution.toLayers();
 
 		view = new TopographyCanvas(topography, grid);
 	
		if (highContrast) {
 			view.addLayer("WORK");
 			
 			for (int y=0;y<grid.height;y++) { 
 				for (int x=0;x<grid.width;x++) { 
 					if (grid.get(x,y) == null) { 
 						view.fillBlock("WORK", x, y, Color.BLACK);
 					} else { 
 						view.fillBlock("WORK", x, y, Color.WHITE);
 					}
 				}
 			}
 			
 			LINE_COLOR_CLUSTER = new Color(255,255,0);		
 			LINE_COLOR_CORE = new Color(225,0,0);
 			LINE_COLOR_NODE = new Color(255,128,0); 
 			LINE_COLOR_BLOCK = new Color(128,128,128); 
 
 			LINE_WIDTH_CLUSTER = 17f;
 			LINE_WIDTH_NODE = 15f;
 			LINE_WIDTH_CORE = 10f;
 			LINE_WIDTH_BLOCK = 5f;
 			
 		} else { 
 			LINE_COLOR_CLUSTER = new Color(255,255,0,200);
 			LINE_COLOR_CORE = new Color(225,0,0,160);
 			LINE_COLOR_NODE = new Color(200,128,0,160);
 			LINE_COLOR_BLOCK = new Color(128,128,128,50);
 		
 			LINE_WIDTH_CLUSTER = 15f;
 			LINE_WIDTH_NODE = 11f;
 			LINE_WIDTH_CORE = 5f;
 			LINE_WIDTH_BLOCK = 4f;
 		}
 		
 		view.addLayer("BLOCKS");
 		view.addLayer("CORES");
 		view.addLayer("NODES");
 		view.addLayer("CLUSTERS");
 		view.addLayer("FILL");
 
 		if (showGUI) { 
 			JFrame frame = new JFrame("Topography");
 			frame.setSize(1000, 667);
 			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			frame.getContentPane().add(view);
 			frame.setVisible(true);			
 			view.addMouseListener(new MyListener());
 		}
 	}
 	
 	public void repaint() { 
 		view.repaint();
 	}
 	
 	public void addLayer(String name) throws Exception { 
 		view.addLayer(name);
 	}
 
 	public void clearLayer(String name) throws Exception { 
 		view.clearLayer(name);
 	}
 	
 	public void fillBlock(String layer, int x, int y, Color color) throws Exception {		
 		view.fillBlock(layer, x, y, color);
 	}
 	
 	public void draw(String layer, int x, int y, Color color) throws Exception {
 		view.draw(layer, x, y, color);
 	}
 	
 	/** 
 	 * Color the edge of the <code>set</set> with the provided colors for <code>ocean</code> and <code>land</code>.
 	 * 
 	 * @param layer the layer at which the edge must be drawn.
 	 * @param s the set to draw the edge for. 
 	 * @param ocean the color to use for ocean neighbors.
 	 * @param land the color to use for land neighbors.
 	 * @throws Exception if the edge could not be drawn.
 	 */
 	private void colorEdge(String layer, Set s, Color ocean, Color land) throws Exception { 
 
 		if (s == null) { 
 			return;
 		}
 
 		Coordinate [] tmp = s.getNeighbours(neighbours);
 
 		for (Coordinate c : tmp) { 
 			if (grid.get(c.x, c.y) != null) { 
 				view.fillBlock(layer, c.x, c.y, ocean);
 			} else { 
 				view.fillBlock(layer, c.x, c.y, land);
 			}
 		}
 	}
 
 	/** 
 	 * Callback function for the <code>MouseListener</code> attached to the TopographyView.
 	 * 
 	 * @param p the Point that has been clicked. 
 	 * @throws Exception if the mouse click could not be processed. 
 	 */
 	private void clicked(Point p) throws Exception { 
 
 		if (logger.isDebugEnabled()) { 
 			logger.debug("Click at " + p.x + " " + p.y);
 		}
 
 		double w = view.getWidth();
 		double h = view.getHeight();
 
 		int posX = (int) ((p.x / w) * topography.width);
 		int posY = topography.height - (int) ((p.y / h) * topography.height);
 
 		int bx = posX / grid.blockWidth;
 		int by = posY / grid.blockHeight;
 
 		int commBlock = -1;
 		int commCore = -1;
 		int commNode = -1;
 		int commCluster = -1;
 
 		int block = -1;
 		int core = -1;
 		int node = -1;
 		int cluster = -1;
 
 /*
  		Coordinate c = new Coordinate(bx, by);
 		
 		int [][] comm = neighbours.getCommunication(c);
 
 		System.out.println("Communication on BLOCK layer: ");
 
 		for (int i=0;i<3;i++) { 
 			for (int j=0;j<3;j++) {
 				System.out.print(" " + comm[i][j]);
 			}
 			System.out.println();
 		}
 */
 				
 		view.clearLayer("FILL");
 		
 		if (layers.contains("BLOCKS")) { 
 
 			Layer l = layers.get("BLOCKS");		
 			Set s = l.locate(bx, by);
 
 			if (s != null) { 
 				block = s.index;
 				commBlock = s.getCommunication(neighbours);
 				colorEdge("FILL", s, HALO_COLOR_BLOCK, LAND_COLOR_BLOCK);
 			}			
 		}
 		
 		if (layers.contains("CORES")) { 
 			Layer l = layers.get("CORES");		
 			Set s = l.locate(bx, by);
 
 			if (s != null) { 
 				core = s.index;
 				commCore = s.getCommunication(neighbours);
 				colorEdge("FILL", s, HALO_COLOR_CORE, LAND_COLOR_CORE);
 			}			
 		}
 
 		if (layers.contains("NODES")) { 
 			Layer l = layers.get("NODES");
 			Set s = l.locate(bx, by);
 
 			if (s != null) {
 				node = s.index;
 				commNode = s.getCommunication(neighbours);
 				colorEdge("FILL", s, HALO_COLOR_NODE, LAND_COLOR_NODE);
 			}			
 		}
 
 		if (layers.contains("CLUSTERS")) { 
 			Layer l = layers.get("CLUSTERS");
 			Set s = l.locate(bx, by);
 
 			if (s != null) { 				
 				cluster = s.index;				
 				commCluster = s.getCommunication(neighbours);
 				colorEdge("FILL", s, HALO_COLOR_CLUSTER, LAND_COLOR_CLUSTER);
 			}			
 		}
 
 		System.out.print("Selected");	
 		
 		if (cluster >= 0) { 
 			System.out.print(" cluster " + cluster);	
 		}
 		
 		if (node >= 0) { 
 			System.out.print(" node " + node);	
 		}
 		
 		System.out.println(" core " + core + " block " + block + " (" + bx + "x" + by + ")");
 		
 		System.out.println("Communication on layer BLOCK " + commBlock);
 		System.out.println("Communication on layer CORE " + commCore);
 		System.out.println("Communication on layer NODE " + commNode);
 		System.out.println("Communication on layer CLUSTER " + commCluster);
 
 		/*
 		Coordinate [][] tmp = neighbours.getNeighbours(c, true);
 
 		for (int i=0;i<3;i++) { 
 			for (int j=0;j<3;j++) {
 				if (tmp[i][j] != null) { 
 
 					int nx = tmp[i][j].x;
 					int ny = tmp[i][j].y;
 
 					//System.out.println("Fill neighbour " + nx + "x" + ny);
 
 					if (grid.get(nx, ny) != null) { 
 						view.fillBlock("FILL", nx, ny, HALO_COLOR_BLOCK);
 					} else { 
 						view.fillBlock("FILL", nx, ny, LAND_COLOR_BLOCK);
 					}
 				}
 			}
 		}
 */
 		
 		view.fillBlock("FILL", bx, by, CENTER);
 		view.repaint();
 	}
 
 	/**
 	 * Creates a HashMap containing all edges (Lines) of all Blocks in the given <code>set</code>. 
 	 * <p>
 	 * Each edge in the HashMap maps to an Integer value which indicates how often the edge was encountered in the set. The edges 
 	 * that have a count of <code>1</code> together form the outer edge of the set. 
 	 * 
 	 * @param s the set for which to collect the edges. 
 	 * @param out the HashMap to which the result will be added. 
 	 */
 	private void collectLines(Set s, HashMap<Line, Integer> out) { 
 
 		for (int i=0;i<s.size();i++) { 
 
 			Coordinate c = s.get(i).coordinate;
 
 			if (c.x < grid.width) { 
 				addLine(out, new Line(c, c.offset(1, 0)));
 
 				if (c.y < grid.height) { 
 					addLine(out, new Line(c.offset(0, 1), c.offset(1, 1)));
 					addLine(out, new Line(c.offset(1, 0), c.offset(1, 1)));
 				}
 			}
 
 			if (c.y < grid.height) {
 				addLine(out, new Line(c, c.offset(0, 1)));
 			}
 		}	
 	}
 
 	/**
 	 * Add a line to the HashMap.
 	 * 
 	 * If the line was not in the HashMap yet, it is added mapping to value <code>1</code>. Else the value it maps to is 
 	 * incremented by one.  
 	 *  
 	 * @param map the HashMap to add the line to. 
 	 * @param line the Line to add.  
 	 */
 	private void addLine(HashMap<Line, Integer> map, Line line) { 
 
 		if (!map.containsKey(line)) { 
 			map.put(line, 1);
 			return;
 		}
 
 		int count = map.get(line);
 		map.put(line, count+1);				
 	}
 
 	/** 
 	 * Draw all lines in the HashMap which map to the value <code>1</code> in the specified layer.   
 	 * 
 	 * @param layer the layer to draw to. 
 	 * @param map the collection of lines. 
 	 * @param color the color to draw the lines in. 
 	 * @param lineWidth the width of the lines to draw.   
 	 * @throws Exception if the lines could not be drawn. 
 	 */
 	private void drawLines(String layer, HashMap<Line, Integer> map, Color color, float lineWidth) throws Exception {
 
 		if (view == null) { 
 			return;
 		}
 
 		// We draw each line that has only been added to the map once. 
 		for (Line l : map.keySet()) { 
 
 			int count = map.get(l);
 
 			if (count == 1) {
 				view.draw(layer, l, color, lineWidth);
 			}
 		}
 	}
 
 	/** 
 	 * Draw the edges of all sets in the specified layer.    
 	 * 
 	 * @param layer the layer to draw. 
 	 * @param color the color to draw the lines in. 
 	 * @param lineWidth the width of the lines to draw.   
 	 * @throws Exception if the lines could not be drawn. 
 	 */
 	private void drawLayer(Layer l, Color color, float lineWidth) throws Exception  { 
 
 		if (view == null || l == null) {
 			return;
 		}
 
 		for (int i=0;i<l.size();i++) {
 
 			Set s = l.get(i);
 
 			HashMap<Line, Integer> map = new HashMap<Line, Integer>();
 
 			collectLines(s, map);
 
 			drawLines(l.name, map, color, lineWidth);
 		}
 
 		view.repaint();
 	}
 
 	/** 
 	 * Draw the outline of all ocean blocks in the grid.  
 	 * 
 	 * @throws Exception if the outline of the ocean blocks could not be drawn.  
 	 */
 	public void drawBlocks() throws Exception {
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("Showing layer BLOCKS");
 		}
 
 		drawLayer(layers.get("BLOCKS"), LINE_COLOR_BLOCK, LINE_WIDTH_BLOCK);
 	}
 
 	/** 
 	 * Draw the outline of all cluster sets.   
 	 * 
 	 * @throws Exception if the outline of the cluster sets could not be drawn.  
 	 */
 	public void drawClusters() throws Exception {
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("Showing layer CLUSTERS");
 		}
 
 		drawLayer(layers.get("CLUSTERS"), LINE_COLOR_CLUSTER, LINE_WIDTH_CLUSTER);
 	}
 
 	/** 
 	 * Draw the outline of all node sets.   
 	 * 
 	 * @throws Exception if the outline of the node sets could not be drawn.  
 	 */
 	public void drawNodes() throws Exception {
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("Showing layer NODES");
 		}
 
 		drawLayer(layers.get("NODES"), LINE_COLOR_NODE, LINE_WIDTH_NODE);
 	}
 
 	/** 
 	 * Draw the outline of all core sets.   
 	 * 
 	 * @throws Exception if the outline of the core sets could not be drawn.  
 	 */
 	public void drawCores() throws Exception {
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("Showing layer CORES");
 		}
 
 		drawLayer(layers.get("CORES"), LINE_COLOR_CORE, LINE_WIDTH_CORE);
 	}	
 	
 	/** 
 	 * Draw the outline of all blocks, cores, nodes and clusters   
 	 * 
 	 * @throws Exception if the outline of the core sets could not be drawn.  
 	 */
 	public void drawAll() throws Exception {
 
 		drawBlocks();
 		
 		if (distribution.clusters > 1) { 
 			drawClusters();
 		}
 		
 		drawNodes();
 		drawCores();
 	}	
 
 	/** 
 	 * Save the current image as a png file.  
 	 *  
 	 * @param file the filename of the file to save. 
 	 * @throws IOException if the file could not be saved. 
 	 */
 	public void save(String file) throws IOException {
 		view.save(file);
 	}
 
 	/** 
 	 * Main entry point into application. 
 	 * 
 	 * @param args the command line arguments provided by the user. 
 	 */
 	public static void main(String [] args) { 
 
 		if (args.length < 2) { 
 			System.out.println("Usage: DistributionViewer topography_file distribution_file\n" + 
 					"\n" + 
 					"Read a topography file and work distribution file and show a graphical interface that allows " + 
 					"the user to interactively explore the work distribution.\n" + 
 					"\n" + 
 					"  topography_file     a topography file that contains the index of the deepest ocean level at " + 
 					"each gridpoint.\n" + 
 					"  distribution_file   a work distribution file.\n" + 
 					"  [--contrast]        color blocks according to work for high contrast image.\n" + 					
 					"  [--image image.png] store the result in a png image instead of showing it in a GUI.\n");
 
 			System.exit(1);
 		}
 
 		String topographyFile = args[0];
 		String distributionFile = args[1];
 
 		String output = null;
 		boolean highconstrast= false;
 		boolean showGUI = true;	
 		
 		int i=2;
 		
 		while (i<args.length) { 
 
 			if (args[i].equals("--contrast")) { 
 				highconstrast = true;
 				i++;
 				
 			} else if (args[i].equals("--image")) {
 				
 				if ((i+1) >= args.length) {
 					Utils.fatal("Option \"--image\" requires parameter!");
 				}
 					
 				output = args[i+1];
 				showGUI = false;
 				i += 2;
 				
 			} else { 
 				Utils.fatal("Unknown option " + args[i]);
 			}
 		}
 		
 		try { 			
 			Distribution d = new Distribution(distributionFile);
 			Topography t = new Topography(d.topographyWidth, d.topographyHeight, topographyFile);
 			Grid g = new Grid(t, d.blockWidth, d.blockHeight);
 			Neighbours n = new Neighbours(g, d.blockWidth, d.blockHeight, Neighbours.CYCLIC, Neighbours.TRIPOLE);
 	
 			DistributionViewer dv = new DistributionViewer(d, t, g, n, showGUI, highconstrast);
 			dv.drawAll();
 	
 			if (output != null) { 
 				dv.save(output);
 			}
 			
 		} catch (Exception e) {
 			Utils.fatal("Failed to run DistributionViewer ", e);
 		}
 	}
 }
