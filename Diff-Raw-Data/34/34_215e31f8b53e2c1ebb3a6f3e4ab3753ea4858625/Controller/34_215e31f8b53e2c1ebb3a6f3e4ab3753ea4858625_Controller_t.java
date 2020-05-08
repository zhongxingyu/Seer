 package controller;
 
 import exceptions.ExceptionController;
 import files.FileLoader;
 import files.FileLoaderConnectionOnly;
 import GUI.GUI;
 
import java.awt.Component;
 import java.util.HashSet;
import java.util.LinkedList;

 import visualization.MapComponent;
 import dataStructure.Connection;
 import dataStructure.ConnectionQuadTree;
 import dataStructure.Interval;
 import dataStructure.Interval2D;
 import dataStructure.TernarySearchTries;
 
 /**
  * 
  * @author Claus L. Henriksen clih@itu.dk
  * @author Pacmans
  * @version 10. April 2012
  * 
  */
 public final class Controller {
 	private static Controller instance; // singleton
 	private static GUI gui;
 	private MapComponent map;
 	private Connection[] connections;
 	private TernarySearchTries<Integer> tries;
 	private ConnectionQuadTree qt;
 	private double xMin, yMin, xMax, yMax;
 
 	/**
 	 * Constructor for this class loads connections and points from FileLoader
 	 * 
 	 * @see FileLoader
 	 */
 	public Controller() {
 	  instance = this;
 		try {
 			FileLoader fileLoader = new FileLoaderConnectionOnly();
 			connections = fileLoader.getConnections();
 			tries = new TernarySearchTries<Integer>();
 			xMin = fileLoader.getxMin().intValue();
 			yMin = fileLoader.getyMin().intValue();
 			xMax = fileLoader.getxMax().intValue();
 			yMax = fileLoader.getyMax().intValue();
 			qt = fileLoader.getConnectionQuadTree();
 		} catch (Exception e) {
 			ExceptionController.recieveException(e);
 		}
		
 
 	/**
 	 * 
 	 * @return Returns an instance of the singleton Controller (this class)
 	 */
 	public static Controller getInstance() {
 		if (instance == null) // should not happen
 			new Controller(); //Constructor in Controller updates instance
 		return instance;
 	}
 
 	/**
 	 * 
 	 * @return Returns instance of the singleton class GUI
 	 * @see GUI
 	 */
 	public static GUI getGUI() {
 	  if(gui == null) gui = new GUI();
 		return gui;
 	}
 
 	/**
 	 * 
 	 * @return Returns instance of the singleton class Map which paints the map
 	 * @see MapComponent
 	 */
 	public MapComponent getMap() {
 	  if(map == null) map = new MapComponent();
 		return map;
 	}
 
 	/**
 	 * Get all connections within rectangle
 	 * 
 	 * @param x1
 	 * @param y1
 	 * @param x2
 	 * @param y2
 	 *            ArrayList of connections within rectangle
 	 * @return
 	 */
 	public Connection[] getConnections(double x1, double y1, double x2,
 			double y2) {
 		// get HashSet of connection IDs from QuadTree
 		// this is fast? NOOOOOOO!
 		HashSet<Integer> cons = qt.getConnections(
 				new Interval2D(new Interval(x1, x2), new Interval(y1, y2)));
 		Connection[] cs = new Connection[cons.size()];
 		int size = 0;
 		for (Integer i : cons) { // this is slow
 			cs[size++] = connections[i];
 		}
 		return cs;
 	}
 
 	/**
 	 * Get array of all connections
 	 * 
 	 * @return Array of all connections
 	 * @see Connection
 	 */
 	public Connection[] getConnections() {
 		return connections;
 	}
 
 	/**
 	 * Set status label on GUI
 	 * 
 	 * @param s
 	 *            Status to be shown
 	 */
 	public static void setStatus(String s) {
 		// GUI.setStatus(s);
 	}
 
 	/**
 	 * Show or hide a type of road
 	 * 
 	 * @param n
 	 *            type_id
 	 * @param b
 	 *            To show or not to show
 	 */
 	public void updateMap(int n, boolean m) {
 		map.updateRoadTypes(n, m);
 	}
 
 	public void showAll() {
 		map.resetZoom();
 	}
 
 	public double getxMin() {
 		return xMin;
 	}
 
 	public double getyMin() {
 		return yMin;
 	}
 
 	public double getxMax() {
 		return xMax;
 	}
 
 	public double getyMax() {
 		return yMax;
 	}
 
 	/**
 	 * Main method creates a new GUI
 	 * 
 	 * @see GUI-class
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Controller.getGUI();
 	}
 }
