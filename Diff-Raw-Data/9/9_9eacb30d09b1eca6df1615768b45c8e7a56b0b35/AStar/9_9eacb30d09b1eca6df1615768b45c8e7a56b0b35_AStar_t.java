 package projectrts.model.core.pathfinding;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import projectrts.model.core.Position;
 
 /**
  * A* pathfinding algorithm.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStar {
 
 	private static World world;
 	/**
 	 * Initializes the A* class
 	 * @param world Game world
 	 */
 	public static void initialize(World world)
 	{
 		AStar.world = world;
 	}
 	
 	/**
 	 * Creates a new A* instance. AStar must have been initialized,
 	 * otherwise an IllegalStateException will be thrown.
 	 */
 	public AStar()
 	{
 		if (world == null)
 		{
 			throw new IllegalStateException("You must initialize the AStar class before you can use it");
 		}
 	}
 	
 	/**
 	 * Calculates a path using the A* algorithm.
 	 * @param startPos Start position.
 	 * @param targetPos End position.
 	 * @return An AStarPath from startPos to targetPos.
 	 */
 	public AStarPath calculatePath(Position startPos, Position targetPos)
 	{
 		// TODO Plankton: Use threads or something to not "freeze" the game when calculating?
 		AStarNode startNode = new AStarNode(world.getNodeAt(startPos));
 		AStarNode endNode = new AStarNode(world.getNodeAt(targetPos));
 		List<AStarNode> openList = new ArrayList<AStarNode>();
 		List<AStarNode> closedList = new ArrayList<AStarNode>();
 		
 		if (endNode.isObstacle())
 		{
 			// TODO Plankton: Find some faster way to do it.
 			// Find the walkable node that's the closest to endNode.
 			// If there are more than one, pick the one with the shortest path from the player.
 		}
 		
 		// A* algorithm starts here
 		openList.add(startNode);
 		while (openList.size() > 0) // while open list is not empty
 		{
 			// current node  = node from the open list with the lowest cost
 			Collections.sort(openList);
 			AStarNode currentNode = openList.get(0);
 			
 			if (currentNode.equals(endNode))
 			{
 				return generatePath(startNode, currentNode);
 			}
 			else
 			{
 				// http://www.policyalmanac.org/games/aStarTutorial.htm
 				
 				// move current node to the closed list
 				openList.remove(0);
 				closedList.add(currentNode);
 				
 				// examine each node adjacent to the current node
 				List<AStarNode> adjacentNodes = currentNode.getNeighbours();
 				for (AStarNode node : adjacentNodes)
 				{
 					if (!node.isObstacle()) // if not an obstacle
 					{
 						if (!closedList.contains(node)) // and not on closed list
 						{
 							if (!openList.contains(node)) // and not on open list
 							{
 								// move to open list and calculate cost
 								openList.add(node);
 								node.calculateCost(currentNode, endNode);
 							}
 							else // if on open list, check to see if new path is better
 							{
 								node.refreshCost(currentNode);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// path not found, return empty path
		//return new AStarPath();
		
 		Collections.sort(closedList, AStarNode.getHeuristicComparator());
		return generatePath(startNode, closedList.get(1));
		// the second element in closedList since the first one is the start node
 	}
 	
 	private AStarPath generatePath(AStarNode startNode, AStarNode endNode)
 	{
 		AStarPath path = new AStarPath();
 		AStarNode nextNode = endNode;
 		
 		while (!nextNode.equals(startNode))
 		{
 			path.addNodeToPath(nextNode);
 			nextNode = nextNode.getParent();
 		}
 		
 		return path;
 	}
 }
