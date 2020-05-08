 package linewars.display.layers;
 
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 
 import linewars.display.ColoredEdge;
 import linewars.display.ColoredNode;
 import linewars.gamestate.GameState;
 import linewars.gamestate.Lane;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.mapItems.CommandCenter;
 import linewars.gamestate.mapItems.Node;
 
 public class GraphLayer implements ILayer
 {
 	@Override
 	public void draw(Graphics g, GameState gamestate, Rectangle2D visibleScreen)
 	{
 		//the following code is for testing of the ColoredEdge class
 //		Lane l = new Lane(new Position(305, 350), new Position(10, 250), new Position(540, 115), new Position(350, 345), 10);
 //		ColoredEdge test = new ColoredEdge(l);
 //		test.draw(g);
 		//end test code
 
 		//the following code is for testing of the ColoredNode class
//		CommandCenter center = new CommandCenter(new Transformation(new Position(300, 300), 0), null, null);
 //		Node n = new Node(null, new Lane[]{}, center, null);
 //		ColoredNode testNode = new ColoredNode(n);
 //		testNode.draw(g);
 		//end test code
 
 		/*
 		 * List<Node> nodes = gamestate.getNodes();
 		 * for (Node n : nodes)
 		 * {
 		 * 		if (node is visible)
 		 * 		{
 		 * 			drawNode(n, g);
 		 * 		}
 		 * }
 		 * 
 		 * List<Edges> edges = gamestate.getEdges();
 		 * for (Edge e : edges)
 		 * {
 		 * 		if (edge is visible)
 		 * 		{
 		 * 			drawEdge(e, g);
 		 * 		}
 		 * }
 		 */
 	}
 	
 	/*
 	 * private void drawNode(Node n, Graphics g)
 	 * {
 	 * 	...
 	 * }
 	 * 
 	 * private void drawEdge(Edge n, Graphics g)
 	 * {
 	 * 	...
 	 * }
 	 */
 
 }
