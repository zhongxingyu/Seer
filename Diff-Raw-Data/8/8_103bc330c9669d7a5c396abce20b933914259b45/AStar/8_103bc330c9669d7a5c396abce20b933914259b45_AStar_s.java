
 import java.awt.Point;
 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class AStar {
 	public Node[] algorithm (Grid g, Point start, Point goal)
 	{
		PriorityQueue closedSet = new PriorityQueue();
		PriorityQueue openSet = new PriorityQueue(Grid.getCoordinates(start));
 		Node current = null;
 		ArrayList<Node> list = new ArrayList<Node>();
 		
 		while(openSet.size() != 0)
 		{
 			current = openSet.peek();
 			
 			if (current.equals(goal))
 			{
 				return list;
 			}
 
 			openSet.remove(current);
 			closedSet.add(current);
 			
 			if closedSet.add(openSet.poll())
 			{
 					current.getConnections()
 			}	
 			else 
 			{
 					failure
 			}
 
 		}
 		/*
 		 *  function A*(start,goal)
      closedset := the empty set    // The set of nodes already evaluated.
      openset := {start}    // The set of tentative nodes to be evaluated, initially containing the start node
      came_from := the empty map    // The map of navigated nodes.
  
      g_score[start] := 0    // Cost from start along best known path.
      // Estimated total cost from start to goal through y.
      f_score[start] := g_score[start] + heuristic_cost_estimate(start, goal)
  
      while openset is not empty
          current := the node in openset having the lowest f_score[] value
          if current = goal
              return reconstruct_path(came_from, goal)
  
          remove current from openset
          add current to closedset
          for each neighbor in neighbor_nodes(current)
              tentative_g_score := g_score[current] + dist_between(current,neighbor)
              if neighbor in closedset
                  if tentative_g_score >= g_score[neighbor]
                      continue
  
              if neighbor not in openset or tentative_g_score < g_score[neighbor] 
                  came_from[neighbor] := current
                  g_score[neighbor] := tentative_g_score
                  f_score[neighbor] := g_score[neighbor] + heuristic_cost_estimate(neighbor, goal)
                  if neighbor not in openset
                      add neighbor to openset
  
      return failure
  
  function reconstruct_path(came_from, current_node)
      if current_node in came_from
          p := reconstruct_path(came_from, came_from[current_node])
          return (p + current_node)
      else
          return current_node
 
 		 * 
 		 */
 	}
 	
 	public static fScore ()
 	{
 		
 	}
 
 	
 }
