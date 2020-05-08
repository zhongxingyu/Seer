 package editor.mapEditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.geom.Dimension2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ParserKeys;
 import linewars.configfilehandler.ConfigData.NoSuchKeyException;
 import linewars.gamestate.BezierCurve;
 import linewars.gamestate.BuildingSpot;
 import linewars.gamestate.Lane;
 import linewars.gamestate.Node;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.shapes.Circle;
 import linewars.gamestate.shapes.Rectangle;
 import linewars.gamestate.shapes.Shape;
 
 public class MapPanel extends JPanel
 {
 	private static final double MAX_ZOOM = 0.15;
 	private static final double MIN_ZOOM = 1.5;
 	
	private static int NEXT_NODE_ID = 1;
	
 	private JSlider laneWidthSlider;
 	private JComboBox nodeSelector;
 	private JComboBox buildingSelector;
 	private JComboBox commandCenterSelector;
 
 	private double zoomLevel;
 	private double lastDrawTime;
 	
 	private String mapURI;
 	private double mapWidth;
 	private double mapHeight;
 
 	private Position mousePosition;
 	private Position lastClickPosition;
 	private Rectangle2D viewport;
 	private Dimension2D mapSize;
 	
 	private MapDrawer mapDrawer;
 	private LaneDrawer laneDrawer;
 	private NodeDrawer nodeDrawer;
 	private BuildingDrawer buildingDrawer;
 	
 	private ArrayList<Lane> lanes;
 	private ArrayList<Node> nodes;
 	private ArrayList<BuildingSpot> buildingSpots;
 	private ArrayList<BuildingSpot> commandCenters;
 	
 	private boolean moving;
 	private boolean moveP1;
 	private boolean moveP2;
 	private boolean resizeW;
 	private boolean resizeH;
 	private boolean rotating;
 	
 	private Lane movingLane;
 	private Node movingNode;
 	private BuildingSpot movingSpot;
 	
 	private Lane selectedLane;
 	private Node selectedNode;
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
 
 	public MapPanel(int width, int height)
 	{
 		super(null);
 		setPreferredSize(new Dimension(width, height));
 		
 		laneWidthSlider = null;
 		nodeSelector = null;
 		buildingSelector = null;
 		commandCenterSelector = null;
 		
 		// starts the user fully zoomed out
 		zoomLevel = 1.0;
 		mousePosition = new Position(0, 0);
 		lastClickPosition = new Position(0, 0);
 		mapSize = new Dimension(0, 0);
 		viewport = new Rectangle2D.Double(0, 0, 100, 100);
 		
 		mapURI = null;
 		mapWidth = 100;
 		mapHeight = 100;
 		
 		mapDrawer = new MapDrawer(this);
 		laneDrawer = new LaneDrawer(this);
 		nodeDrawer = new NodeDrawer(this);
 		buildingDrawer = new BuildingDrawer(this);
 		
 		lanes = new ArrayList<Lane>();
 		nodes = new ArrayList<Node>();
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
 	
 	public void loadConfigFile(ConfigData data, boolean force)
 	{
 		if(!force && !Boolean.getBoolean(data.getString(ParserKeys.valid)))
 			throw new IllegalArgumentException("The config data object is not valid");
 		
 		List<ParserKeys> definedKeys = data.getDefinedKeys();
 		
 		lanes = new ArrayList<Lane>();
 		nodes = new ArrayList<Node>();
 		buildingSpots = new ArrayList<BuildingSpot>();
 		commandCenters = new ArrayList<BuildingSpot>();
 
 		if(definedKeys.contains(ParserKeys.icon))
 			setMapImage(data.getString(ParserKeys.icon));
 		else if(force)
 		{
 			setMapImage(null);
 			return;
 		}
 		else
 			throw new IllegalArgumentException("The map image is not defined");
 		
 		if(definedKeys.contains(ParserKeys.imageWidth) && definedKeys.contains(ParserKeys.imageHeight))
 			setMapSize((int)(double)data.getNumber(ParserKeys.imageWidth), (int)(double)data.getNumber(ParserKeys.imageHeight));
 		else if(!force)
 			throw new IllegalArgumentException("The map size is not defined");
 		
 		if(definedKeys.contains(ParserKeys.lanes))
 		{
 			List<ConfigData> ls = data.getConfigList(ParserKeys.lanes);
 			for(ConfigData l : ls)
 			{
 				Lane lane;
 				try
 				{
 					lane = new Lane(l, force);
 				}
 				catch(NoSuchKeyException e)
 				{
 					if(force)
 						continue;
 					else
 						throw new IllegalArgumentException("A lane could not be properly constructed");
 				}
 				
 				lanes.add(lane);
 			}
 		}
 		else
 			throw new IllegalArgumentException("There are no lanes defined");
 		
 		if(definedKeys.contains(ParserKeys.nodes))
 		{
 			List<ConfigData> ns = data.getConfigList(ParserKeys.nodes);
 			for(ConfigData n : ns)
 			{
 				Node newNode;
 				try
 				{
					newNode = new Node(n, lanes.toArray(new Lane[0]), NEXT_NODE_ID++, force);
 				}
 				catch(NoSuchKeyException e)
 				{
 					if(force)
 						continue;
 					else
 						throw new IllegalArgumentException("A node could not be properly constructed");
 				}
 				
 				nodes.add(newNode);
 				
 				commandCenters.add(newNode.getCommandCenterSpot());
 				for(BuildingSpot s : newNode.getBuildingSpots())
 				{
 					buildingSpots.add(s);
 				}
 			}
 		}
 		else
 			throw new IllegalArgumentException("There are no nodes defined");
 		
 		boolean valid = false;
 		while(!valid)
 		{
 			valid = true;
 			for(Lane l : lanes)
 			{
 				Node[] nodes = l.getNodes();
 				if(nodes.length == 0)
 				{
 					if(!force)
 						throw new IllegalArgumentException("Lane " + l.getName() + " has no nodes");
 					
 					lanes.remove(l);
 				}
 				else if(nodes.length == 1)
 				{
 					if(!force)
 						throw new IllegalArgumentException("Lane " + l.getName() + " has only one attached node");
 						
 					valid = false;
 					nodes[0].removeAttachedLane(l);
 					lanes.remove(l);
 				}
 			}
 			
 			for(Node n : nodes)
 			{
 				if(n.getAttachedLanes().length == 0)
 				{
 					if(!force)
 						throw new IllegalArgumentException("There is a node with no attached lanes");
 						
 					nodes.remove(n);
 				}
 			}
 		}
 	}
 	
 	public ConfigData getData()
 	{
 		ConfigData data = new ConfigData();
 		
 		data.set(ParserKeys.icon, mapURI);
 		data.set(ParserKeys.imageWidth, mapWidth);
 		data.set(ParserKeys.imageHeight, mapHeight);
 		
 		for(Lane l : lanes)
 		{
 			data.add(ParserKeys.lanes, l.getData());
 		}
 		
 		for(Node n : nodes)
 		{
 			data.add(ParserKeys.nodes, n.getData());
 		}
 		
 		data.set(ParserKeys.valid, Boolean.toString(isValidConfig()));
 		
 		return data;
 	}
 	
 	public boolean isValidConfig()
 	{
 		boolean valid = true;
 		
 		for(Node n : nodes)
 		{
 			if(n.getCommandCenterSpot() == null)
 				valid = false;
 			if(n.getAttachedLanes().length == 0)
 				valid = false;
 		}
 		
 		return valid;
 	}
 	
 	public Node[] getNodes()
 	{
 		return nodes.toArray(new Node[0]);
 	}
 	
 	public BuildingSpot[] getBuildingSpots()
 	{
 		return buildingSpots.toArray(new BuildingSpot[0]);
 	}
 	
 	public BuildingSpot[] getCommandCenters()
 	{
 		return commandCenters.toArray(new BuildingSpot[0]);
 	}
 	
 	public void setSelectedNode(Node n)
 	{
 		selectedNode = n;
 	}
 	
 	public void setSelectedBuilding(BuildingSpot b)
 	{
 		selectedBuilding = b;
 	}
 	
 	public void setSelectedCommandCenter(BuildingSpot b)
 	{
 		selectedCommandCenter = b;
 	}
 	
 	public void setNodesVisible(boolean b)
 	{
 		nodesVisible = b;
 	}
 	
 	public void setLanesVisible(boolean b)
 	{
 		lanesVisible = b;
 	}
 	
 	public void setBuildingsVisible(boolean b)
 	{
 		buildingsVisible = b;
 	}
 	
 	public void setCommandCentersVisible(boolean b)
 	{
 		ccsVisible = b;
 	}
 	
 	public void setCreateLane(boolean b)
 	{
 		createLane = b;
 		if(!b)
 			selectedLane = null;
 	}
 	
 	public void setCreateNode(boolean b)
 	{
 		createNode = b;
 		if(!b)
 			selectedNode = null;
 	}
 	
 	public void setCreateBuilding(boolean b)
 	{
 		createBuilding = b;
 	}
 	
 	public void setCreateCommandCenter(boolean b)
 	{
 		createCC = b;
 	}
 	
 	public void setMapImage(String mapURI)
 	{
 		this.mapURI = mapURI;
 		Dimension dim = mapDrawer.setMap(mapURI);
 		setMapSize(dim.getWidth(), dim.getHeight());
 	}
 	
 	public void setMapSize(double width, double height)
 	{
 		mapWidth = width;
 		mapHeight = height;
 		mapSize.setSize(width, height);
 		viewport.setRect(0, 0, width, height);
 	}
 	
 	public void setLaneWidthSlider(JSlider slider)
 	{
 		laneWidthSlider = slider;
 	}
 	
 	public void setNodeSelector(JComboBox box)
 	{
 		nodeSelector = box;
 	}
 	
 	public void setBuildingSelector(JComboBox box)
 	{
 		buildingSelector = box;
 	}
 	
 	public void setCommandCenterSelector(JComboBox box)
 	{
 		commandCenterSelector = box;
 	}
 
 	public void setLaneWidth(double width)
 	{
 		if(selectedLane != null)
 			selectedLane.setWidth(mapHeight * (width / 100));
 	}
 
 	public Position toGameCoord(Position screenCoord)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		return new Position((screenCoord.getX() / scale) + viewport.getX(), (screenCoord.getY() / scale) + viewport.getY());
 	}
 
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
 				
 		//fill the background black
 		g.setColor(Color.black);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		
 		//draw the map
 		mapDrawer.draw(g, viewport, scale);
 		
 		//draw the lanes
 		if(lanesVisible)
 		{
 			for(Lane l : lanes)
 			{
 				laneDrawer.draw(g, l, selectedLane == l, toGameCoord(mousePosition), scale);
 			}
 		}	
 		
 		//draw the nodes
 		if(nodesVisible)
 		{
 			for(Node n : nodes)
 			{
 				nodeDrawer.draw(g, n, selectedNode == n, toGameCoord(mousePosition), scale);
 			}
 		}
 		
 		//draw the building spots
 		if(buildingsVisible)
 		{
 			for(BuildingSpot b : buildingSpots)
 			{
 				buildingDrawer.draw(g, b, b == selectedBuilding, toGameCoord(mousePosition), scale);
 			}
 		}
 		
 		//draw the command centers
 		if(ccsVisible)
 		{
 			for(BuildingSpot b : commandCenters)
 			{
 				buildingDrawer.draw(g, b, b == selectedCommandCenter, toGameCoord(mousePosition), scale, true);
 			}
 		}
 		
 		this.repaint();
 	}
 	
 	private void updateViewPortPan(double fps, double scale)
 	{
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
 
 	private void updateViewPort(double viewX, double viewY, double newW, double newH, boolean zooming)
 	{
 		double oldX = viewport.getX();
 		double oldY = viewport.getY() ;
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
 		if(newX > mapSize.getWidth() - newW)
 		{
 			double x = mapSize.getWidth() - newW;
 			moveX += x - newX;
 			newX = x;
 		}
 		if(newW > mapSize.getWidth())
 		{
 			double x = (mapSize.getWidth() - newW) / 2;
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
 		if(newY > mapSize.getHeight() - newH)
 		{
 			double y = mapSize.getHeight() - newH;
 			moveY += y - newY;
 			newY = y;
 		}
 		if(newH > mapSize.getHeight())
 		{
 			double y = (mapSize.getHeight() - newH) / 2;
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
 
 	private void moveItem(Position change)
 	{
 		if(movingNode != null)
 		{
 			moveNode(change);
 		}
 		else if(movingSpot != null)
 		{
 			moveSpot(change);
 		}
 		else if(movingLane != null)
 		{
 			moveLane(change);
 		}
 	}
 	
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
 
 	private void moveP0andP1(Position change, Lane l)
 	{
 		BezierCurve curve = l.getCurve();
 		Position newP1 = curve.getP1().add(change);
 		
 		Node node = l.getNodes()[0];
 		Position nodePos = node.getTransformation().getPosition();
 		Position pointingVec = newP1.subtract(nodePos).normalize();
 		Position newP0 = pointingVec.scale(node.getBoundingCircle().getRadius()).add(nodePos);
 		
 		curve.setP0(newP0);
 		curve.setP1(newP1);
 	}
 
 	private void moveP2andP3(Position change, Lane l)
 	{
 		BezierCurve curve = l.getCurve();
 		Position newP2 = curve.getP2().add(change);
 		
 		Node node = l.getNodes()[1];
 		Position nodePos = node.getTransformation().getPosition();
 		Position pointingVec = newP2.subtract(nodePos).normalize();
 		Position newP3 = pointingVec.scale(node.getBoundingCircle().getRadius()).add(nodePos);
 		
 		curve.setP2(newP2);
 		curve.setP3(newP3);
 	}
 
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
 		
 		for(Lane l : movingNode.getAttachedLanes())
 		{
 			Node[] nodeArr = l.getNodes();
 			if(movingNode == nodeArr[0])
 			{
 				moveP0andP1(new Position(0, 0), l);
 			}
 			else if(movingNode == nodeArr[1])
 			{
 				moveP2andP3(new Position(0, 0), l);
 			}
 		}
 	}
 	
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
 				r = (Rectangle)r.transform(new Transformation(new Position(0, 0), newRotation - currentRotation - Math.PI / 4));
 				movingSpot.setRect(r);
 			}
 			
 			double width = r.getWidth();
 			double height = r.getHeight();
 			if(resizeW)
 			{
 				double rotation = r.position().getRotation();
 				Position axis = new Position(Math.cos(rotation), Math.sin(rotation));
 				Position ray = toGameCoord(mousePosition).subtract(r.position().getPosition());
 				
 				width = ray.length() * Math.abs(axis.dot(ray.normalize())) * 2;
 			}
 			if(resizeH)
 			{
 				double rotation = r.position().getRotation();
 				Position axis = new Position(Math.sin(rotation), Math.cos(rotation));
 				Position ray = toGameCoord(mousePosition).subtract(r.position().getPosition());
 				
 				height = ray.length() * Math.abs(axis.dot(ray.normalize())) * 2;
 			}
 			
 			movingSpot.setDim((int)width, (int)height);
 		}
 	}
 	
 	private void selectLane(Position p)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		for(Lane l : lanes)
 		{
 			BezierCurve curve = l.getCurve();
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
 			laneWidthSlider.setValue((int)(selectedLane.getWidth() / mapHeight * 100));
 		
 		if(!moveP1 && !moveP2)
 		{
 			for(Node n : nodes)
 			{
 				Circle c = n.getBoundingCircle();
 				if(c.positionIsInShape(p))
 				{
 					selectedNode = n;
 					break;
 				}
 			}
 		}
 	}
 
 	private void selectNode(Position p)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		for(Node n : nodes)
 		{
 			Circle c = n.getBoundingCircle();
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
			movingNode = new Node(p, NEXT_NODE_ID++);
 			nodes.add(movingNode);
 			nodeSelector.addItem(movingNode);
 			
 			resizeW = true;
 			resizeH = true;
 		}
 		
 		selectedNode = movingNode;
 		nodeSelector.setSelectedItem(movingNode);
 	}
 
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
 
 	private void selectBuildingSpot(Position p, ArrayList<BuildingSpot> spots)
 	{
 		double scale = getWidth() / viewport.getWidth();
 		for(BuildingSpot b : spots)
 		{
 			Rectangle r = b.getRect();
 			Rectangle dragSpot = new Rectangle(r.position(), r.getWidth() - 2.5 / scale, r.getHeight() - 2.5 / scale);
 			Rectangle resizeWidth = new Rectangle(r.position(), r.getWidth() + 2.5 / scale, r.getHeight());
 			Rectangle resizeHeight = new Rectangle(r.position(), r.getWidth(), r.getHeight() + 2.5 / scale);
 			
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
 	
 	private void createLane(Position p)
 	{
 		Node otherNode = null;
 		for(Node n : nodes)
 		{
 			Circle c = n.getBoundingCircle();
 			if(c.positionIsInShape(p))
 			{
 				otherNode = n;
 				break;
 			}
 		}
 		
 		if(otherNode != null)
 		{
 			Lane l = new Lane(selectedNode, otherNode);
 			selectedLane = l;
 			
 			if(laneWidthSlider != null)
 				laneWidthSlider.setValue((int)(l.getWidth() / mapHeight * 100));
 			
 			lanes.add(l);
 			selectedNode.addAttachedLane(l);
 			otherNode.addAttachedLane(l);
 		}
 	}
 
 	@Override
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 
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
 			Position newPos = new Position(e.getPoint().getX(),e.getPoint().getY());
 			Position change = toGameCoord(newPos).subtract(toGameCoord(mousePosition));
 			mousePosition = newPos;
 
 			moveItem(change);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			Position p = new Position(e.getPoint().getX(), e.getPoint().getY());
 			lastClickPosition = toGameCoord(p);
 			
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
 			mousePosition = new Position(e.getPoint().getX(),e.getPoint().getY());
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
