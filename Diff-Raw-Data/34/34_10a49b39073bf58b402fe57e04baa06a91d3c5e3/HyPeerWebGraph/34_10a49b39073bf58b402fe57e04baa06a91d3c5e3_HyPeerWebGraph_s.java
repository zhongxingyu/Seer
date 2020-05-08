 package hypeerweb;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Objects;
 import java.util.TreeSet;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * Draws a directed graph of a HyPeerWeb node
  * Red-links = neighbors
  * Pink-links = isneighbors
  * Green-links = sneighbors
  * Cyan = selected node
  * Yellow = selected node's children
  * Green = selcted node's parent
  * 
  * - Right click a node hide it
  * - Middle click to restore all hidden nodes
  * 
  * @author isaac
  */
 public class HyPeerWebGraph extends JFrame{
 	private HyPeerWeb web;
 	private Graph draw;
 	private static int winSize = 700;
 	private int levels;
 	
 	public HyPeerWebGraph() throws Exception{
 		web = HyPeerWeb.getInstance();
 		
 		//Initialize window
 		setTitle("HyPeerWeb Directed Graph");
 		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(winSize, winSize);
 		draw = new Graph();
 		add(draw);
 		
 		//Mouse listeners
 		addMouseListener(new MouseListener(){
 			@Override
 			public void mousePressed(MouseEvent e) {
 				draw.mouseClick(e.getButton());
 			}
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 			@Override
 			public void mouseClicked(MouseEvent e) {}
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 		});
 		addMouseMotionListener(new MouseMotionListener(){
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				draw.dragMouse(e.getX(), e.getY()-20);
 			}
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				draw.moveMouse(e.getX(), e.getY()-20);
 			}
 		});
 	}
 	
 	/**
 	 * Draws the specified node out to levels
 	 * @param n 
 	 * @param level number of levels to go out
 	 */
 	public void drawNode(Node n, int level){
 		levels = level;
 		draw.draw(n);
 	}
 	
 	//INNER DRAWING CLASS:
 	private class Graph extends JPanel{
 		private Node n;							//Node we are going to draw
 		private int nodeSize = 10,				//Node size, in pixels
					margin = 0,				//Minimum margin between nodes (within allowed window space)
 					selMargin = 5,				//Mimimum margin before a node is close enough to mouse for selection
 					mx, my;						//Mouse coordinates
 		private TreeSet<Link> links;			//Set of links for the graph
 		//Link types
 		private final Node.Connections.ConnectionType[] Ntypes = {
 			Node.Connections.ConnectionType.NEIGHBOR,
 			Node.Connections.ConnectionType.SNEIGHBOR,
 			Node.Connections.ConnectionType.ISNEIGHBOR
 		};
 		//Drawing modes
 		private HashSet<Node> hide;
 		private HashMap<Node, DrawData> data;
 		private final AlphaComposite compMode = AlphaComposite.getInstance(AlphaComposite.DST_OVER);
 		private final float[] dash1 = {4}, dash2 = {10};
 		private final Stroke
 			strokeN = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
 			strokeSN = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dash1, 10),
 			strokeISN = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dash2, 10);
 		//Drawing buffer
 		private BufferedImage buffer;
 		private Entry<Node, DrawData> selected;
 		private boolean redraw = false;
 		
 		/**
 		 * Draws the specified node
 		 */
 		public void draw(Node n){
 			this.n = n;
 			hide = new HashSet<>();
 			data = new HashMap<>();
 			links = new TreeSet<>();
 			buffer = null;
 			repaint();
 		}
 		/**
 		 * Draws the mouse cursor
 		 * @param x x-coordinate
 		 * @param y y-coordinate
 		 */
 		public void moveMouse(int x, int y){
 			mx = x;
 			my = y;
 			//Highlight a node for selection
 			Point2D temp;
 			for (Entry<Node, DrawData> datum: data.entrySet()){
 				if (datum.getValue().coord.distance(mx, my) <= nodeSize){
 					selected = datum;
 					repaint();
 					return;
 				}
 			}
 			if (selected != null){
 				selected = null;
 				repaint();
 			}
 		}
 		public void dragMouse(int x, int y){
 			//Rotate, if a node is selected
 			DrawData ddChild, ddParent;
 			if (selected != null && (ddChild = selected.getValue()).parent != null){
 				ddParent = data.get(ddChild.parent);
 				double px = ddParent.coord.getX(),
 						py = ddParent.coord.getY();
 				ddParent.skew += Math.atan2(y-py, x-px)-Math.atan2(my-py, mx-px);
 				redraw();
 			}
 			mx = x;
 			my = y;
 		}
 		public void mouseClick(int button){
 			if (button == MouseEvent.BUTTON2){
 				hide.clear();
 				redraw();
 			}
 			//Hide all nodes descending from this one
 			if (button == MouseEvent.BUTTON3 && selected != null){
 				hide.add(selected.getKey());
 				ArrayList<Node> nodes = selected.getValue().children;
 				while (nodes != null && !nodes.isEmpty()){
 					hide.addAll(nodes);
 					ArrayList<Node> temp = new ArrayList<>();
 					for (Node z: nodes){
 						ArrayList<Node> childs = data.get(z).children;
 						if (childs != null)
 							temp.addAll(childs);
 					}
 					nodes = temp;
 				}
 				redraw();
 			}
 		}
 		
 		private void redraw(){
 			redraw = true;
 			repaint();
 		}
 		
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			Graphics2D g2 = (Graphics2D) g;
 			//Redrawing
 			if (buffer != null && !redraw){
 				g2.drawImage(buffer, null, 0, 0);
 				//Selected nodes
 				if (selected != null){
 					DrawData temp = selected.getValue();
 					g2.setColor(Color.CYAN);
 					repaintNode(g2, selected.getKey());
 					//Children of selection
 					if (temp.children != null){
 						g2.setColor(Color.YELLOW);
 						for (Node child: temp.children)
 							repaintNode(g2, child);
 					}
 					//Parent of selection
 					if (temp.parent != null){
 						g2.setColor(Color.GREEN);
 						repaintNode(g2, temp.parent);
 					}
 				}
 				return;
 			}
 			//Draw a node
 			if (n != null){
 				buffer = new BufferedImage(winSize, winSize, BufferedImage.TYPE_INT_ARGB);
 				Graphics2D gbi = buffer.createGraphics();
 				//Get diameters of circles
 				int radius = (int) (winSize/2-levels*margin);
 				//Paint the starting node inthe center of the screen
 				gbi.setColor(Color.BLUE);
 				if (!redraw)
 					addNodeData(n, null, null, -1, 0, null);
 				paintNode(gbi, n, winSize/2, winSize/2);
 				//Loop through each connection level and draw circles
 				ArrayList<Node> parents = new ArrayList<>();
 				ArrayList<Node> friends;
 				parents.add(n);
 				for (int level = 0; level <= levels; level++){
 					radius /= 2;
 					friends = new ArrayList<>();
 					for (Node parent: parents){
 						if (!hide.contains(parent))
 							friends.addAll(paintCircle(gbi, parent, radius+margin, redraw ? -1 : level));
 					}
 					if (friends.isEmpty()) break;
 					parents = friends;
 				}
 				//Paint all links
 				gbi.setComposite(compMode);
 				paintLinks(gbi);
 				g2.drawImage(buffer, null, 0, 0);
 			}
 			redraw = false;
 		}
 		/**
 		 * Paints a circle of nodes
 		 * @param g graphics context
 		 * @param n the parent node to draw around
 		 * @param radius the radius of the circle
 		 * @param level what is the depth of this graph circle? Use -1 when redrawing
 		 * @return a list of nodes added children nodes (of n, the parent)
 		 */
 		private ArrayList<Node> paintCircle(Graphics2D g, Node n, int radius, int level){
 			DrawData parentData = data.get(n);
 			Point2D origin = parentData.coord;
 			//Get a list of nodes that haven't been drawn already
 			ArrayList<Node> friends;
 			if (level != -1){
 				friends = new ArrayList<>();
 				Node[][] Nlinks = {
 					n.getNeighbors(),
 					n.getSurrogateNeighbors(),
 					n.getInverseSurrogateNeighbors()
 				};
 				for (int i=0; i<Ntypes.length; i++){
 					for (Node link: Nlinks[i]){
 						if (!data.containsKey(link))
 							friends.add(link);
 						links.add(new Link(n, link, Ntypes[i]));
 					}
 				}
 				parentData.children = friends;
 			}
 			else friends = parentData.children;
 			//Remove hidden friends
 			ArrayList<Node> temp = new ArrayList<>();
 			for (Node z: friends){
 				if (!hide.contains(z))
 					temp.add(z);
 			}
 			friends = temp;
 			//Draw these friends in a circle
 			if (parentData.skew == -1)
 				parentData.skew = Math.random()*2*Math.PI/friends.size();
 			double theta = 2*Math.PI/(double) (friends.size() == 2 ? 3 : friends.size()),
 					angle = parentData.skew;
 			Point2D coord = new Point2D.Double();
 			for (Node friend: friends){
 				if (level != -1)
 					addNodeData(friend, n, null, -1, level, null);
 				coord.setLocation(
 					radius*Math.cos(angle),
 					radius*Math.sin(angle)
 				);
 				paintNode(g, friend,
 					(int) (origin.getX()+coord.getX()),
 					(int) (origin.getY()+coord.getY())
 				);
 				angle += theta;
 			}
 			return friends;
 		}
 		/**
 		 * Draws an individual node
 		 * @param g graphics context
 		 * @param n the node to draw
 		 * @param x x-coord location
 		 * @param y y-coord location
 		 */
 		private void paintNode(Graphics2D g, Node n, int x, int y){
 			data.get(n).coord.setLocation(x, y);
 			drawNode(g, n, x, y);
 		}
 		private void repaintNode(Graphics2D g, Node n){
 			Point2D p = data.get(n).coord;
 			drawNode(g, n, (int) p.getX(), (int) p.getY());
 		}
 		private void drawNode(Graphics2D g, Node n, int x, int y){
 			if (!hide.contains(n)){
 				g.fillOval(x-nodeSize/2, y-nodeSize/2, nodeSize, nodeSize);
 				g.drawString(n.getWebId()+" ("+n.getHeight()+")", x+5, y-5);
 			}
 		}
 		/**
 		 * Draws all links between the nodes in the graph
 		 * @param g graphics context
 		 */
 		private void paintLinks(Graphics2D g){
 			DrawData d1, d2;
 			Node.Connections.ConnectionType curType = null;
 			ArrayList<Link> badLinks = new ArrayList<>();
 			for (Link l: links){
 				d1 = data.get(l.friend);
 				d2 = data.get(l.origin);
 				//Make sure references exist in the graph
 				if (d1 != null && d2 != null){
 					if (hide.contains(l.friend) || hide.contains(l.origin))
 						continue;
 					//Switch drawing mode
 					//TreeSet is ordered by type, so we'll only switch modes thrice
 					if (curType != l.type){
 						curType = l.type;
 						switch (l.type){
 							case NEIGHBOR:
 								g.setStroke(strokeN);
 								g.setColor(Color.RED);
 								break;
 							case SNEIGHBOR:
 								g.setStroke(strokeSN);
 								g.setColor(Color.GREEN);
 								break;
 							case ISNEIGHBOR:
 								g.setStroke(strokeISN);
 								g.setColor(Color.MAGENTA);
 								break;
 						}
 					}
 					//Draw a line
 					g.drawLine((int) d1.coord.getX(), (int) d1.coord.getY(), (int) d2.coord.getX(), (int) d2.coord.getY());
 				}
 				else badLinks.add(l);
 			}
 			links.removeAll(badLinks);
 		}
 		
 		private void addNodeData(Node n, Node parent, ArrayList<Node> children, double skew, int level, Point2D coord){
 			if (coord == null)
 				coord = new Point2D.Double();
 			data.put(n, new DrawData(parent, children, skew, level, coord));
 		}
 		
 		private class Link implements Comparable{
 			public Node origin;
 			public Node friend;
 			public Node.Connections.ConnectionType type;
 			
 			public Link(Node origin, Node friend, Node.Connections.ConnectionType type){
 				this.origin = origin;
 				this.friend = friend;
 				this.type = type;
 			}
 			
 			@Override
 			public boolean equals(Object n){
 				if (n == null || getClass() != n.getClass())
 					return false;
 				Link l = (Link) n;
 				return type == l.type &&
 						((origin == l.origin && friend == l.friend) ||
 						 (friend == l.origin && origin == l.friend));
 			}
 			@Override
 			public int hashCode() {
 				int hash = 7;
 				hash = 29 * hash + Objects.hashCode(this.origin);
 				hash = 29 * hash + Objects.hashCode(this.friend);
 				hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
 				return hash;
 			}
 			@Override
 			public int compareTo(Object o) {
 				if (o == null || getClass() != o.getClass())
 					return 1;
 				Link l = (Link) o;
 				if (l.type == type)
 					return this.equals(l) ? 0 : 1;
 				return l.type.compareTo(type);
 			}
 		}
 		private class DrawData{
 			ArrayList<Node> children;
 			Node parent;		//Parent node (if level != 0)
 			double skew;		//Rotation skew angle
 			int level;			//Level that this was drawn at
 			Point2D coord;		//Coordinates on screen
 			
 			public DrawData(Node parent, ArrayList<Node> children, double skew, int level, Point2D coord){
 				this.parent = parent;
 				this.children = children;
 				this.skew = skew;
 				this.level = level;
 				this.coord = coord;
 			}
 		}
 	};
 }
