 package editor.mapEditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Formatter.BigDecimalLayoutForm;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 
 import editor.BigFrameworkGuy;
 import editor.BigFrameworkGuy.ConfigType;
 
 import linewars.gamestate.BezierCurve;
 import linewars.gamestate.BuildingSpot;
 import linewars.gamestate.LaneConfiguration;
 import linewars.gamestate.MapConfiguration;
 import linewars.gamestate.NodeConfiguration;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.shapes.Circle;
 import linewars.gamestate.shapes.Rectangle;
 import linewars.gamestate.shapes.Shape;
 
 /**
  * The panel that contains the map in the map editor.
  * 
  * @author Ryan Tew
  * 
  */
 @SuppressWarnings("serial")
 public class MapPanel extends JPanel
 {
 	private static final double MAX_ZOOM = 0.15;
 	private static final double MIN_ZOOM = 1.5;
 
 	private MapEditor parent;
 
 	private JSlider laneWidthSlider;
 	private JComboBox nodeSelector;
 	private JComboBox buildingSelector;
 	private JComboBox commandCenterSelector;
 	private JTextField mapWidthTextField;
 	private JTextField mapHeightTextField;
 
 	private double zoomLevel;
 	private double lastDrawTime;
 
 	private Position mousePosition;
 	private Rectangle2D viewport;
 	private Position mapSize;
 	private String mapURI;
 
 	private MapDrawer mapDrawer;
 	private LaneDrawer laneDrawer;
 	private NodeDrawer nodeDrawer;
 	private BuildingDrawer buildingDrawer;
 
 	private List<LaneConfiguration> lanes;
 	private List<NodeConfiguration> nodes;
 	private List<BuildingSpot> buildingSpots;
 	private List<BuildingSpot> commandCenters;
 
 	private boolean moving;
 	private boolean moveP1;
 	private boolean moveP2;
 	private boolean resizeW;
 	private boolean resizeH;
 	private boolean rotating;
 
 	private LaneConfiguration movingLane;
 	private NodeConfiguration movingNode;
 	private BuildingSpot movingSpot;
 
 	private LaneConfiguration selectedLane;
 	private NodeConfiguration selectedNode;
 	private BuildingSpot selectedBuilding;
 	private BuildingSpot selectedCommandCenter;
 
 	private boolean lanesVisible;
 	private boolean nodesVisible;
 	private boolean buildingsVisible;
 	private boolean ccsVisible;
 
 	private boolean createLane;
 	private boolean createNode;
 	private boolean createBuilding;
 	private boolean createCC;
 
 	/**
 	 * Constructs this map panel with a default, empty map and sets the
 	 * preferred size of the panel.
 	 * 
 	 * @param parent
 	 *            The MapEditor that contains this MapPanel.
 	 * @param width
 	 *            The desired width of the panel.
 	 * @param height
 	 *            The desired height of the panel.
 	 */
 	public MapPanel(MapEditor parent, int width, int height)
 	{
 		super(null);
 		setPreferredSize(new Dimension(width, height));
 
 		this.parent = parent;
 		
 		laneWidthSlider = null;
 		nodeSelector = null;
 		buildingSelector = null;
 		commandCenterSelector = null;
 
 		// starts the user fully zoomed out
 		zoomLevel = 1.0;
 		mousePosition = new Position(0, 0);
 		mapSize = new Position(100, 100);
 		viewport = new Rectangle2D.Double(0, 0, 100, 100);
 
 		mapURI = null;
 
 		mapDrawer = new MapDrawer(this);
 		mapDrawer.setMapSize(100, 100);
 		laneDrawer = new LaneDrawer(this);
 		nodeDrawer = new NodeDrawer(this);
 		buildingDrawer = new BuildingDrawer(this);
 
 		lanes = new ArrayList<LaneConfiguration>();
 		nodes = new ArrayList<NodeConfiguration>();
 		buildingSpots = new ArrayList<BuildingSpot>();
 		commandCenters = new ArrayList<BuildingSpot>();
 
 		moving = false;
 		moveP1 = false;
 		moveP2 = false;
 		resizeW = false;
 		resizeH = false;
 		rotating = false;
 
 		movingLane = null;
 		movingNode = null;
 		movingSpot = null;
 
 		selectedLane = null;
 		selectedNode = null;
 
 		lanesVisible = true;
 		nodesVisible = true;
 		buildingsVisible = true;
 		ccsVisible = true;
 
 		createLane = false;
 		createNode = false;
 		createBuilding = false;
 		createCC = false;
 
 		// ignores system generated repaints
 		setIgnoreRepaint(true);
 		setOpaque(false);
 
 		// adds the mouse input handler
 		InputHandler ih = new InputHandler();
 		addMouseWheelListener(ih);
 		addMouseMotionListener(ih);
 		addMouseListener(ih);
 	}
 
 	/**
 	 * Loads a map from a ConfigData.
 	 * 
 	 * @param data
 	 *            The map to load.
 	 * @param force
 	 *            If true this indicates that all errors should be caught and
 	 *            default values should be put in where needed. If false the
 	 *            ConfigData is assumed to be correct and valid, if it is not an
 	 *            error will most likely be thrown.
 	 */
 	public void loadMap(MapConfiguration map)
 	{
 		setMapImage(map.getImageURI());
 		Position mapSize = map.getImageSize();
 		if(mapSize != null)
 			setMapSize(mapSize, true);
 		
 		lanes = map.getLanes();
 		nodes = map.getNodes();
 		
 		commandCenters = new ArrayList<BuildingSpot>();
 		buildingSpots = new ArrayList<BuildingSpot>();
 		for(NodeConfiguration n : nodes)
 		{
			BuildingSpot cc = n.getCommandCenterSpot();
			if(cc != null)
				commandCenters.add(cc);
			
 			buildingSpots.addAll(n.buildingSpots());
 		}
 	}
 
 	/**
 	 * Creates a ConfigData that represents the map currently being displayed.
 	 * 
 	 * @return The ConfigData for the current map.
 	 */
 	public ConfigType getData(MapConfiguration toSet)
 	{
 		if(mapURI == null)
 			createMapImage();
 
 		toSet.setImageURI(mapURI);
 		toSet.setImageSize(mapSize);
 		toSet.setLanes(lanes);
 		toSet.setNodes(nodes);
 
 		return BigFrameworkGuy.ConfigType.map;
 	}
 
 	/**
 	 * Creates a default map image using the current configuration of the map as
 	 * a guide.
 	 */
 	private void createMapImage()
 	{
 		BufferedImage map = new BufferedImage((int)mapSize.getX(), (int)mapSize.getY(), BufferedImage.TYPE_INT_ARGB);
 		Graphics g = map.getGraphics();
 
 		g.setColor(Color.black);
 		g.fillRect(0, 0, (int)mapSize.getX(), (int)mapSize.getY());
 
 		for(LaneConfiguration l : lanes)
 			laneDrawer.createMap(g, l);
 
 		for(NodeConfiguration n : nodes)
 			nodeDrawer.createMap(g, n);
 
 		for(BuildingSpot b : buildingSpots)
 			buildingDrawer.createMap(g, b, false);
 
 		for(BuildingSpot b : commandCenters)
 			buildingDrawer.createMap(g, b, true);
 
 		int i = 0;
 		File file = null;
 		boolean fileFound = false;
 		while(!fileFound)
 		{
 			file = new File("resources/images/map" + ++i + ".png");
 			fileFound = !file.exists();
 		}
 
 		mapURI = "map" + i + ".png";
 		try
 		{
 			ImageIO.write(map, "png", file);
 		}
 		catch (IOException e)
 		{
 			JOptionPane.showMessageDialog(null, "The map image file " + mapURI + " could not be saved!", "ERROR",
 					JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Gets all of the nodes in the map.
 	 * 
 	 * @return All of the nodes in the map.
 	 */
 	public NodeConfiguration[] getNodes()
 	{
 		return nodes.toArray(new NodeConfiguration[0]);
 	}
 
 	/**
 	 * Gets all of the buildings in the map.
 	 * 
 	 * @return All of the buildings in the map.
 	 */
 	public BuildingSpot[] getBuildingSpots()
 	{
 		return buildingSpots.toArray(new BuildingSpot[0]);
 	}
 
 	/**
 	 * Gets all of the command centers in the map.
 	 * 
 	 * @return All of the command centers in the map.
 	 */
 	public BuildingSpot[] getCommandCenters()
 	{
 		return commandCenters.toArray(new BuildingSpot[0]);
 	}
 
 	/**
 	 * Sets the selected node in the map.
 	 * 
 	 * @param n
 	 *            The new selected node.
 	 */
 	public void setSelectedNode(NodeConfiguration n)
 	{
 		selectedNode = n;
 	}
 
 	/**
 	 * Sets the selected building in the map.
 	 * 
 	 * @param b
 	 *            The new selected building.
 	 */
 	public void setSelectedBuilding(BuildingSpot b)
 	{
 		selectedBuilding = b;
 	}
 
 	/**
 	 * Sets the selected command center in the map.
 	 * 
 	 * @param b
 	 *            The new selected command center.
 	 */
 	public void setSelectedCommandCenter(BuildingSpot b)
 	{
 		selectedCommandCenter = b;
 	}
 
 	/**
 	 * Sets the nodes to be drawn on the map
 	 * 
 	 * @param b
 	 *            True will draw the nodes. False will not.
 	 */
 	public void setNodesVisible(boolean b)
 	{
 		nodesVisible = b;
 	}
 
 	/**
 	 * Sets the lanes to be drawn on the map
 	 * 
 	 * @param b
 	 *            True will draw the lanes. False will not.
 	 */
 	public void setLanesVisible(boolean b)
 	{
 		lanesVisible = b;
 	}
 
 	/**
 	 * Sets the buildings to be drawn on the map
 	 * 
 	 * @param b
 	 *            True will draw the buildings. False will not.
 	 */
 	public void setBuildingsVisible(boolean b)
 	{
 		buildingsVisible = b;
 	}
 
 	/**
 	 * Sets the command centers to be drawn on the map
 	 * 
 	 * @param b
 	 *            True will draw the command centers. False will not.
 	 */
 	public void setCommandCentersVisible(boolean b)
 	{
 		ccsVisible = b;
 	}
 
 	/**
 	 * Sets lanes to be editable.
 	 * 
 	 * @param b
 	 *            True will allow editing of the lanes. False will not.
 	 */
 	public void setCreateLane(boolean b)
 	{
 		createLane = b;
 		if(!b)
 			selectedLane = null;
 	}
 
 	/**
 	 * Sets nodes to be editable.
 	 * 
 	 * @param b
 	 *            True will allow editing of the nodes. False will not.
 	 */
 	public void setCreateNode(boolean b)
 	{
 		createNode = b;
 		if(!b)
 			selectedNode = null;
 	}
 
 	/**
 	 * Sets buildings to be editable.
 	 * 
 	 * @param b
 	 *            True will allow editing of the buildings. False will not.
 	 */
 	public void setCreateBuilding(boolean b)
 	{
 		createBuilding = b;
 	}
 
 	/**
 	 * Sets command centers to be editable.
 	 * 
 	 * @param b
 	 *            True will allow editing of the command centers. False will
 	 *            not.
 	 */
 	public void setCreateCommandCenter(boolean b)
 	{
 		createCC = b;
 	}
 
 	/**
 	 * Sets the map image for the map.
 	 * 
 	 * @param mapURI
 	 *            The location of the image.
 	 */
 	public void setMapImage(String mapURI)
 	{
 		this.mapURI = mapURI;
 		Position dim = mapDrawer.setMap(mapURI);
 		setMapSize(dim, true);
 	}
 
 	/**
 	 * Sets the size of the map.
 	 * @param mapSize
 	 *            The height of the map in game units.
 	 * @param setEdits
 	 *            Set the map size text fields?
 	 */
 	public void setMapSize(Position mapSize, boolean setEdits)
 	{
 		double width = mapSize.getX();
 		double height = mapSize.getY();
 		
 		mapDrawer.setMapSize(width, height);
 		this.mapSize = new Position(mapSize.getX(), mapSize.getY());
 		double scale = (getHeight() / height) / (getWidth() / width);
 		viewport = new Rectangle2D.Double(0, 0, width, height * scale);
 
 		if(setEdits)
 		{
 			mapWidthTextField.setText(Double.toString(width));
 			mapHeightTextField.setText(Double.toString(height));
 		}
 	}
 
 	/**
 	 * Sets the reference to the JSlider controlling the width of the lane.
 	 * 
 	 * @param slider
 	 *            The JSlider controlling the width of the lane.
 	 */
 	public void setLaneWidthSlider(JSlider slider)
 	{
 		laneWidthSlider = slider;
 	}
 
 	/**
 	 * Sets the reference to the JComboBox that shows the selected node.
 	 * 
 	 * @param box
 	 *            The JComboBox that shows the selected node.
 	 */
 	public void setNodeSelector(JComboBox box)
 	{
 		nodeSelector = box;
 	}
 
 	/**
 	 * Sets the reference to the JComboBox that shows the selected building.
 	 * 
 	 * @param box
 	 *            The JComboBox that shows the selected building.
 	 */
 	public void setBuildingSelector(JComboBox box)
 	{
 		buildingSelector = box;
 	}
 
 	/**
 	 * Sets the reference to the JComboBox that shows the selected command
 	 * center.
 	 * 
 	 * @param box
 	 *            The JComboBox that shows the selected command center.
 	 */
 	public void setCommandCenterSelector(JComboBox box)
 	{
 		commandCenterSelector = box;
 	}
 
 	/**
 	 * Sets the reference to the JTextField the displays the width of the map.
 	 * 
 	 * @param t
 	 *            The JTextFeild that displays the width of the map.
 	 */
 	public void setMapWidthTextField(JTextField t)
 	{
 		mapWidthTextField = t;
 	}
 
 	/**
 	 * Sets the reference to the JTextField the displays the height of the map.
 	 * 
 	 * @param t
 	 *            The JTextFeild that displays the height of the map.
 	 */
 	public void setMapHeightTextField(JTextField t)
 	{
 		mapHeightTextField = t;
 	}
 
 	/**
 	 * Sets the width of the selected lane.
 	 * 
 	 * @param width
 	 *            The percentage of the width of the map that the lane width
 	 *            will be.
 	 */
 	public void setLaneWidth(double width)
 	{
 		if(selectedLane != null)
 			selectedLane.setWidth(mapSize.getY() * (width / 100));
 	}
 
 	/**
 	 * Deletes selected item that is eligible for editing.
 	 */
 	public void deleteSelectedItem()
 	{
 		if(createLane)
 		{
 			if(selectedLane == null)
 				return;
 			
 			selectedLane.getNode(0).attachedLanes().remove(selectedLane);
 			selectedLane.getNode(1).attachedLanes().remove(selectedLane);
 			lanes.remove(selectedLane);
 			selectedLane = null;
 		}
 		else if(createNode)
 		{
 			if(selectedNode == null)
 				return;
 			
 			List<LaneConfiguration> attachedLanes = selectedNode.attachedLanes();
 			for(LaneConfiguration l : attachedLanes)
 			{
 				NodeConfiguration node0 = l.getNode(0);
 				NodeConfiguration node1 = l.getNode(1);
 				
 				if(node0 != selectedNode)
 					node0.attachedLanes().remove(l);
 				else if(node1 != selectedNode)
 					node1.attachedLanes().remove(l);
 
 				lanes.remove(l);
 			}
 
 			nodes.remove(selectedNode);
 			nodeSelector.removeItem(selectedNode);
 			selectedNode = null;
 			
 			parent.refreshNodeEditingPanel();
 		}
 		else if(createBuilding)
 		{
 			if(selectedBuilding == null)
 				return;
 			
 			for(NodeConfiguration n : nodes)
 			{
 				n.buildingSpots().remove(selectedBuilding);
 			}
 
 			buildingSpots.remove(selectedBuilding);
 			buildingSelector.removeItem(selectedBuilding);
 			selectedBuilding = null;
 			
 			parent.refreshNodeEditingPanel();
 		}
 		else if(createCC)
 		{
 			if(selectedCommandCenter == null)
 				return;
 			
 			for(NodeConfiguration n : nodes)
 			{
 				if(n.getCommandCenterSpot() == selectedCommandCenter)
 				{
 					n.setCommandCenterSpot(null);
 				}
 			}
 
 			commandCenters.remove(selectedCommandCenter);
 			commandCenterSelector.removeItem(selectedCommandCenter);
 			selectedCommandCenter = null;
 			
 			parent.refreshNodeEditingPanel();
 		}
 	}
 
 	/**
 	 * Converts the given position from screen coordinates to game coordinates.
 	 * 
 	 * @param screenCoord
 	 *            The position to be converted.
 	 * @return The position in game coordinates.
 	 */
 	public Position toGameCoord(Position screenCoord)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		return new Position((screenCoord.getX() / scale) + viewport.getX(), (screenCoord.getY() / scale)
 				+ viewport.getY());
 	}
 
 	/**
 	 * Converts the given position from game coordinates to screen coordinates.
 	 * 
 	 * @param screenCoord
 	 *            The position to be converted.
 	 * @return The position in screencoordinates.
 	 */
 	public Position toScreenCoord(Position gameCoord)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		return new Position((gameCoord.getX() - viewport.getX()) * scale, (gameCoord.getY() - viewport.getY()) * scale);
 	}
 
 	/**
 	 * Draws everything to the screen.
 	 */
 	@Override
 	public void paint(Graphics g)
 	{
 		long curTime = System.currentTimeMillis();
 		double fps = 1000.0 / (curTime - lastDrawTime);
 		lastDrawTime = curTime;
 
 		double scale = getWidth() / viewport.getWidth();
 		updateViewPortPan(fps, scale);
 
 		// fill the background black
 		g.setColor(Color.black);
 		g.fillRect(0, 0, getWidth(), getHeight());
 
 		// draw the map
 		mapDrawer.draw(g, viewport, scale);
 
 		// draw the lanes
 		if(lanesVisible)
 		{
 			for(LaneConfiguration l : lanes)
 			{
 				laneDrawer.draw(g, l, selectedLane == l, toGameCoord(mousePosition), scale);
 			}
 		}
 
 		// draw the nodes
 		if(nodesVisible)
 		{
 			for(NodeConfiguration n : nodes)
 			{
 				nodeDrawer.draw(g, n, selectedNode == n, toGameCoord(mousePosition), scale);
 			}
 		}
 
 		// draw the building spots
 		if(buildingsVisible)
 		{
 			for(BuildingSpot b : buildingSpots)
 			{
 				buildingDrawer.draw(g, b, b == selectedBuilding, toGameCoord(mousePosition), scale);
 			}
 		}
 
 		// draw the command centers
 		if(ccsVisible)
 		{
 			for(BuildingSpot b : commandCenters)
 			{
 				buildingDrawer.draw(g, b, b == selectedCommandCenter, toGameCoord(mousePosition), scale, true);
 			}
 		}
 
 		this.repaint();
 	}
 
 	/**
 	 * Determines if the viewport needs to be moved and moves it.
 	 * 
 	 * @param fps
 	 *            The current framerate, used to calculate how far to move the
 	 *            viewport.
 	 * @param scale
 	 *            The current scale factor between the veiwport and the screen.
 	 */
 	private void updateViewPortPan(double fps, double scale)
 	{
 		if(mousePosition == null)
 			return;
 
 		double moveX = 0.0;
 		double moveY = 0.0;
 
 		if(mousePosition.getX() < 25)
 		{
 			moveX = (-1000 / fps) / scale;
 		}
 		else if(mousePosition.getX() > getWidth() - 25)
 		{
 			moveX = (1000 / fps) / scale;
 		}
 
 		if(mousePosition.getY() < 25)
 		{
 			moveY = (-1000 / fps) / scale;
 		}
 		else if(mousePosition.getY() > getHeight() - 25)
 		{
 			moveY = (1000 / fps) / scale;
 		}
 
 		updateViewPort(moveX, moveY, viewport.getWidth(), viewport.getHeight(), false);
 	}
 
 	/**
 	 * Moves the viewport but makes sure it is still over the map.
 	 * 
 	 * @param viewX
 	 *            The new X position of the viewport.
 	 * @param viewY
 	 *            The new Y position of the viewport.
 	 * @param newW
 	 *            The new width position of the viewport.
 	 * @param newH
 	 *            The new height position of the viewport.
 	 * @param zooming
 	 *            True if we are zooming. False if we are panning.
 	 */
 	private void updateViewPort(double viewX, double viewY, double newW, double newH, boolean zooming)
 	{
 		double oldX = viewport.getX();
 		double oldY = viewport.getY();
 		double moveX = 0;
 		double moveY = 0;
 
 		// calculates the new x for the viewport
 		double newX = oldX + viewX;
 		if(newX < 0)
 		{
 			double x = 0;
 			moveX += x - newX;
 			newX = x;
 		}
 		if(newX > mapSize.getX() - newW)
 		{
 			double x = mapSize.getX() - newW;
 			moveX += x - newX;
 			newX = x;
 		}
 		if(newW > mapSize.getX())
 		{
 			double x = (mapSize.getX() - newW) / 2;
 			moveX += x - newX;
 			newX = x;
 		}
 
 		// calculates the new y for the viewport
 		double newY = oldY + viewY;
 		if(newY < 0)
 		{
 			double y = 0;
 			moveY += y - newY;
 			newY = y;
 		}
 		if(newY > mapSize.getY() - newH)
 		{
 			double y = mapSize.getY() - newH;
 			moveY += y - newY;
 			newY = y;
 		}
 		if(newH > mapSize.getY())
 		{
 			double y = (mapSize.getY() - newH) / 2;
 			moveY += y - newY;
 			newY = y;
 		}
 
 		if(zooming)
 		{
 			moveItem(new Position(moveX, moveY));
 		}
 		else
 		{
 			moveItem(new Position(newX - oldX, newY - oldY));
 		}
 
 		viewport.setRect(newX, newY, newW, newH);
 	}
 
 	/**
 	 * Moves the item being modified by the vector change.
 	 * 
 	 * @param change
 	 *            The amount to move the item.
 	 */
 	private void moveItem(Position change)
 	{
 		if(movingNode != null)
 		{
 			moveNode(change);
 			parent.refreshNodeEditingPanel();
 		}
 		else if(movingSpot != null)
 		{
 			moveSpot(change);
 			parent.refreshNodeEditingPanel();
 		}
 		else if(movingLane != null)
 		{
 			moveLane(change);
 		}
 	}
 
 	/**
 	 * Adjusts the lane that is being modified.
 	 * 
 	 * @param change
 	 *            The amount to adjust by.
 	 */
 	private void moveLane(Position change)
 	{
 		if(moveP1)
 		{
 			moveP0andP1(change, movingLane);
 		}
 		else if(moveP2)
 		{
 			moveP2andP3(change, movingLane);
 		}
 	}
 
 	/**
 	 * Moves P1 of l by the vector change. While doing this it keeps P0 on the
 	 * line between the node on that end and P1.
 	 * 
 	 * @param change
 	 *            The amount to move P1 by.
 	 * @param l
 	 *            The lane to modify.
 	 */
 	private void moveP0andP1(Position change, LaneConfiguration l)
 	{
 		BezierCurve curve = l.getBezierCurve();
 		Position newP1 = curve.getP1().add(change);
 
 		NodeConfiguration node = l.getNode(0);
 		Position nodePos = node.getShape().position().getPosition();
 		Position pointingVec = newP1.subtract(nodePos).normalize();
 		Position newP0 = pointingVec.scale(node.getShape().boundingCircle().getRadius()).add(nodePos);
 
 		curve.setP0(newP0);
 		curve.setP1(newP1);
 	}
 
 	/**
 	 * Moves P2 of l by the vector change. While doing this it keeps P3 on the
 	 * line between the node on that end and P2.
 	 * 
 	 * @param change
 	 *            The amount to move P2 by.
 	 * @param l
 	 *            The lane to modify.
 	 */
 	private void moveP2andP3(Position change, LaneConfiguration l)
 	{
 		BezierCurve curve = l.getBezierCurve();
 		Position newP2 = curve.getP2().add(change);
 
 		NodeConfiguration node = l.getNode(1);
 		Position nodePos = node.getShape().position().getPosition();
 		Position pointingVec = newP2.subtract(nodePos).normalize();
 		Position newP3 = pointingVec.scale(node.getShape().boundingCircle().getRadius()).add(nodePos);
 
 		curve.setP2(newP2);
 		curve.setP3(newP3);
 	}
 
 	/**
 	 * Adjusts the node that is being modified.
 	 * 
 	 * @param change
 	 *            The amount to adjust by.
 	 */
 	private void moveNode(Position change)
 	{
 		Shape s = movingNode.getShape();
 
 		if(moving)
 		{
 			s = s.transform(new Transformation(change, 0));
 		}
 		else if(resizeW || resizeH)
 		{
 			double newRadius = toGameCoord(mousePosition).subtract(s.position().getPosition()).length();
 			s = new Circle(s.position(), newRadius);
 		}
 
 		movingNode.setShape(s);
 
 		for(LaneConfiguration l : movingNode.attachedLanes())
 		{
 			if(movingNode == l.getNode(0))
 			{
 				moveP0andP1(new Position(0, 0), l);
 			}
 			else if(movingNode == l.getNode(1))
 			{
 				moveP2andP3(new Position(0, 0), l);
 			}
 		}
 	}
 
 	/**
 	 * Adjusts the BuildingSpot that is being modified.
 	 * 
 	 * @param change
 	 *            The amount to adjust by.
 	 */
 	private void moveSpot(Position change)
 	{
 		Rectangle r = movingSpot.getRect();
 
 		if(moving)
 		{
 			r = (Rectangle)r.transform(new Transformation(change, 0));
 
 			movingSpot.setRect(r);
 		}
 		else
 		{
 			if(rotating)
 			{
 				Position mouse = toGameCoord(mousePosition);
 				Position rectPos = r.position().getPosition();
 
 				Position rotatedVector = mouse.subtract(rectPos);
 				double newRotation = Math.atan2(rotatedVector.getY(), rotatedVector.getX());
 				double currentRotation = r.position().getRotation();
 				r = (Rectangle)r.transform(new Transformation(new Position(0, 0), newRotation - currentRotation
 						- Math.PI / 4));
 				movingSpot.setRect(r);
 			}
 
 			double width = r.getWidth();
 			double height = r.getHeight();
 			if(resizeW)
 			{
 				Position[] corners = r.getVertexPositions();
 				Position axis = corners[3].subtract(r.position().getPosition())
 						.add(corners[0].subtract(r.position().getPosition())).normalize();
 				Position ray = toGameCoord(mousePosition).subtract(r.position().getPosition());
 
 				width = ray.length() * Math.abs(axis.dot(ray.normalize())) * 2;
 			}
 			if(resizeH)
 			{
 				Position[] corners = r.getVertexPositions();
 				Position axis = corners[1].subtract(r.position().getPosition())
 						.add(corners[0].subtract(r.position().getPosition())).normalize();
 				Position ray = toGameCoord(mousePosition).subtract(r.position().getPosition());
 
 				height = ray.length() * Math.abs(axis.dot(ray.normalize())) * 2;
 			}
 
 			if(width < 25)
 				width = 25;
 			if(height < 25)
 				height = 25;
 
 			movingSpot.setDim((int)width, (int)height);
 		}
 	}
 
 	/**
 	 * Determines if a lane has been selected and which point on the lane was
 	 * selected.
 	 * 
 	 * @param p
 	 *            The position that was clicked.
 	 */
 	private void selectLane(Position p)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		for(LaneConfiguration l : lanes)
 		{
 			BezierCurve curve = l.getBezierCurve();
 			Position p1 = curve.getP1();
 			Position p2 = curve.getP2();
 			Circle c1 = new Circle(new Transformation(p1, 0), 5 / scale);
 			Circle c2 = new Circle(new Transformation(p2, 0), 5 / scale);
 
 			if(c1.positionIsInShape(p))
 			{
 				movingLane = l;
 				moveP1 = true;
 				break;
 			}
 			else if(c2.positionIsInShape(p))
 			{
 				movingLane = l;
 				moveP2 = true;
 				break;
 			}
 		}
 
 		selectedLane = movingLane;
 		if(selectedLane != null && laneWidthSlider != null)
 			laneWidthSlider.setValue((int)(selectedLane.getWidth() / mapSize.getY() * 100));
 
 		if(!moveP1 && !moveP2)
 		{
 			for(NodeConfiguration n : nodes)
 			{
 				Circle c = n.getShape().boundingCircle();
 				if(c.positionIsInShape(p))
 				{
 					selectedNode = n;
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determines if a node has been selected and if the node is going to be
 	 * moved or resized.
 	 * 
 	 * @param p
 	 *            The position that was clicked.
 	 */
 	private void selectNode(Position p)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		for(NodeConfiguration n : nodes)
 		{
 			Circle c = n.getShape().boundingCircle();
 			Circle dragNode = new Circle(c.position(), c.getRadius() - 2.5 / scale);
 			Circle resizeNode = new Circle(c.position(), c.getRadius() + 2.5 / scale);
 
 			if(dragNode.positionIsInShape(p))
 			{
 				movingNode = n;
 				moving = true;
 				break;
 			}
 			else if(resizeNode.positionIsInShape(p))
 			{
 				movingNode = n;
 				resizeW = true;
 				resizeH = true;
 				break;
 			}
 		}
 
 		if(!moving && !resizeW && !resizeH)
 		{
 			movingNode = new NodeConfiguration(p);
 			nodes.add(movingNode);
 			nodeSelector.addItem(movingNode);
 
 			resizeW = true;
 			resizeH = true;
 		}
 
 		selectedNode = movingNode;
 		nodeSelector.setSelectedItem(movingNode);
 	}
 
 	/**
 	 * Determines if a building has been selected and if the building is going
 	 * to be moved or resized.
 	 * 
 	 * @param p
 	 *            The position that was clicked.
 	 */
 	private void selectBuilding(Position p)
 	{
 		selectBuildingSpot(p, buildingSpots);
 
 		if(!moving && !resizeW && !resizeH && !rotating)
 		{
 			movingSpot = new BuildingSpot(p);
 			buildingSpots.add(movingSpot);
 			buildingSelector.addItem(movingSpot);
 
 			resizeW = true;
 			resizeH = true;
 			rotating = true;
 		}
 
 		selectedBuilding = movingSpot;
 		buildingSelector.setSelectedItem(movingSpot);
 	}
 
 	/**
 	 * Determines if a command center has been selected and if the command
 	 * center is going to be moved or resized.
 	 * 
 	 * @param p
 	 *            The position that was clicked.
 	 */
 	private void selectCommandCenter(Position p)
 	{
 		selectBuildingSpot(p, commandCenters);
 
 		if(!moving && !resizeW && !resizeH && !rotating)
 		{
 			movingSpot = new BuildingSpot(p);
 			commandCenters.add(movingSpot);
 			commandCenterSelector.addItem(movingSpot);
 
 			resizeW = true;
 			resizeH = true;
 			rotating = true;
 		}
 
 		selectedCommandCenter = movingSpot;
 		commandCenterSelector.setSelectedItem(movingSpot);
 	}
 
 	/**
 	 * Determines if a BuildingSpot has been selected and if the BuildingSpot is
 	 * going to be moved or resized.
 	 * 
 	 * @param p
 	 *            The position that was clicked.
 	 */
 	private void selectBuildingSpot(Position p, List<BuildingSpot> spots)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		for(BuildingSpot b : spots)
 		{
 			Rectangle r = b.getRect();
 			Rectangle dragSpot = new Rectangle(r.position(), r.getWidth() - 2.5 / scale, r.getHeight() - 2.5 / scale);
 			Rectangle resizeWidth = new Rectangle(r.position(), r.getWidth() + 2.5 / scale, r.getHeight() - 2.5 / scale);
 			Rectangle resizeHeight = new Rectangle(r.position(), r.getWidth() - 2.5 / scale, r.getHeight() + 2.5
 					/ scale);
 
 			Position circlePos = r.getVertexPositions()[0];
 			Circle rotate = new Circle(new Transformation(circlePos, 0), 5 / scale);
 
 			if(rotate.positionIsInShape(p))
 			{
 				movingSpot = b;
 				rotating = true;
 				break;
 			}
 			else if(dragSpot.positionIsInShape(p))
 			{
 				movingSpot = b;
 				moving = true;
 				break;
 			}
 			else
 			{
 				if(resizeWidth.positionIsInShape(p))
 				{
 					movingSpot = b;
 					resizeW = true;
 				}
 				if(resizeHeight.positionIsInShape(p))
 				{
 					movingSpot = b;
 					resizeH = true;
 				}
 
 				if(resizeW || resizeH)
 					break;
 			}
 		}
 	}
 
 	/**
 	 * Checks to see if the mouse was released within a node, if it was this
 	 * will create a new lane with a default width between that node and the
 	 * selected node.
 	 * 
 	 * @param p
 	 *            The position the mouse was released at.
 	 */
 	private void createLane(Position p)
 	{
 		NodeConfiguration otherNode = null;
 		for(NodeConfiguration n : nodes)
 		{
 			Circle c = n.getShape().boundingCircle();
 			if(c.positionIsInShape(p))
 			{
 				otherNode = n;
 				break;
 			}
 		}
 
 		if(otherNode != null)
 		{
 			LaneConfiguration l = new LaneConfiguration(selectedNode, otherNode);
 			selectedLane = l;
 
 			if(laneWidthSlider != null)
 				laneWidthSlider.setValue((int)(l.getWidth() / mapSize.getY() * 100));
 
 			lanes.add(l);
 			selectedNode.attachedLanes().add(l);
 			otherNode.attachedLanes().add(l);
 		}
 	}
 
 	@Override
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 
 	/**
 	 * Handles the mouse events for this MapPanel.
 	 * 
 	 * @author Ryan Tew
 	 * 
 	 */
 	private class InputHandler extends MouseAdapter
 	{
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			Position p = toGameCoord(new Position(e.getPoint().getX(), e.getPoint().getY()));
 
 			if(createLane)
 			{
 				selectLane(p);
 			}
 			else if(createNode)
 			{
 				selectNode(p);
 			}
 			else if(createBuilding)
 			{
 				selectBuilding(p);
 			}
 			else if(createCC)
 			{
 				selectCommandCenter(p);
 			}
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e)
 		{
 			Position newPos = new Position(e.getPoint().getX(), e.getPoint().getY());
 			Position change = toGameCoord(newPos).subtract(toGameCoord(mousePosition));
 			mousePosition = newPos;
 
 			moveItem(change);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			Position p = new Position(e.getPoint().getX(), e.getPoint().getY());
 
 			if(createLane && selectedNode != null)
 				createLane(toGameCoord(p));
 
 			moving = false;
 			moveP1 = false;
 			moveP2 = false;
 			resizeW = false;
 			resizeH = false;
 			rotating = false;
 
 			movingNode = null;
 			movingSpot = null;
 			movingLane = null;
 
 			if(createLane)
 			{
 				selectedNode = null;
 			}
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e)
 		{
 			mousePosition = new Position(e.getPoint().getX(), e.getPoint().getY());
 		}
 
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent e)
 		{
 			// makes sure the zoom is within the max and min range
 			double newZoom = zoomLevel + e.getWheelRotation() * Math.exp(zoomLevel) * 0.04;
 			if(newZoom < MAX_ZOOM)
 				newZoom = MAX_ZOOM;
 			if(newZoom > MIN_ZOOM)
 				newZoom = MIN_ZOOM;
 
 			// calculates the ratios of the zoom and position
 			double zoomRatio = newZoom / zoomLevel;
 			double ratio = viewport.getWidth() / getWidth();
 
 			// converts the mouse point to the game space
 			double mouseX = mousePosition.getX() * ratio;
 			double mouseY = mousePosition.getY() * ratio;
 
 			// calculates the change in postion of the viewport
 			double viewX = mouseX - mouseX * zoomRatio;
 			double viewY = mouseY - mouseY * zoomRatio;
 
 			// calculates the new dimension of the viewport
 			double newW = viewport.getWidth() * zoomRatio;
 			double newH = viewport.getHeight() * zoomRatio;
 
 			updateViewPort(viewX, viewY, newW, newH, true);
 			zoomLevel = newZoom;
 		}
 	}
 }
