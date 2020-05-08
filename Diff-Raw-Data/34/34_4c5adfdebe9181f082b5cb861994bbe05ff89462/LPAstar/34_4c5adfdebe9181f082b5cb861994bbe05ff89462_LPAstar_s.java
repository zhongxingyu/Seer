 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class LPAstar extends AStar { 
 
 	final static int lineOfSight = 2;
 	static Comparator<Node> kNodeComparator;
 	static ArrayList<Node> path;
 	private static PriorityQueue<Node> open_set;
 	static Node start;
 	static Node goal;
 	static Grid g;
 	static boolean noPath;
 	
 	public static ArrayList<Integer> calculateKey(Node s) 
 	{
 		ArrayList<Integer> key = new ArrayList<Integer>();
 		key.add(Math.min(s.getGScore(), s.getRhsScore()) + hScore(s,goal));
 		key.add(Math.min(s.getGScore(), s.getRhsScore()));
 		return key;
 	}
 
 	public static void initialize()
 	{
 		kNodeComparator = new KNodeComparator();
 		path = new ArrayList<Node>();
 		noPath = false;
 		open_set = new PriorityQueue<Node>(11, kNodeComparator); 
 		start.setRhsScore(0);
		start.setKScore(null);
 		open_set.add(start);
 	}
 
 	public static void updateVertex(Node u)
 	{
 		if(!u.equals(start))
 			u.setRhsScore(findRhs(u));
 
 		if (open_set.contains(u))
 			open_set.remove(u);
 		
 		if (u.getGScore() != u.getRhsScore())
 		{
 			u.setKScore(calculateKey(u));
 			open_set.add(u);
 		}
 	}
 
 	public static int keyCompare(ArrayList<Integer> one, ArrayList<Integer> two)
 	{
 		//return (one.get(0) < two.get(0) || (one.get(0) == two.get(0) && one.get(1) <= two.get(1)));
 		/*if (one.get(0) < two.get(0))
 			return true;
 		else if (one.get(0) > two.get(0))
 			return false;
 		else if (one.get(1) < two.get(1))
 			return true;
 		else if (one.get(1) > two.get(1))
 			return false;
 		return false;*/
 		if (one.get(0) == two.get(0))
 		{
 			if (one.get(1) > two.get(1))
 				return 1;
 			else if (one.get(1) < two.get(1))
 				return -1;
 			return 0;
 		}
 		else
 		{
 			if (one.get(0) > two.get(0))
 				return 1;
 			else if (one.get(0) < two.get(0))
 				return -1;
 			return 0;
 		}
 	}
 	
 	public static void computeShortestPath()
 	{
		while(keyCompare(calculateKey(open_set.peek()), calculateKey(goal)) < 0
 				|| goal.getRhsScore() != goal.getGScore())
 		{
 			Node u = open_set.poll();
 			if ((u.getGScore() > u.getRhsScore()))
 			{
 				u.setGScore(u.getRhsScore());
 				for (Node s : u.getConnections())
 					updateVertex(s);
 			}
 			else
 			{
 				u.setGScore(2000000);
 				for (Node s : u.getConnections())
 					updateVertex(s);
 				updateVertex(u);
 			}
 			
 
 			if(open_set.size() == 0)
 			{
 				System.out.println("Priority Queue is Empty!");
 				noPath = true;
 				break;
 			}
 		}	
 	}
 
 	public static Node[] algorithm(Grid newG, Node newGoal, Node newStart, Node[] changedEdgeVertices)
 	{
 		g = newG;
 		goal = newGoal;
 		start = newStart;
 		initialize();
 		computeShortestPath();
 		if(changedEdgeVertices != null){
 			for(Node n: changedEdgeVertices){
 				Edge[] changedEdges = n.getNewEdges();
 				if(changedEdges != null){
 					updateVertex(n);
 					for(Edge e: changedEdges)
 						updateVertex(e.getEnd(n));
 				}
 			}
 		}
 		if(noPath)
 			return null;
 		return reconstructPath(goal, start, new ArrayList<Node>());		
 		
 	}
 
 	public static Node[] reconstructPath(Node pathGoal, Node pathStart, ArrayList<Node> deadends)
 	{
 
 		if(pathGoal.equals(pathStart))
 		{
 			path.add(0, goal);
 			Node[] result = path.toArray(new Node[path.size()]);
 			return result;
 		}
 		else
 		{
 			PriorityQueue<Node> values = new PriorityQueue<Node>(11, kNodeComparator);
 			for (Node s : pathGoal.getConnections())
 			{
 				s.setKScore(calculateKey(s));
 				values.add(s);
 			}
 			
 			Node def = values.peek();
 			Node closestNode = values.poll();
 			while(path.contains(closestNode)){
 				closestNode = values.poll();
 				if(deadends.contains(def))
 					def = closestNode;
 			}
 			if(closestNode == null){
 				deadends.add(pathGoal);
 				return reconstructPath(def, pathStart, deadends);
 				//return path.toArray(new Node[path.size()]);
 			}
 			path.add(closestNode);
 			return reconstructPath(closestNode, pathStart, deadends);
 		}
 	}
 
 	public static int findRhs(Node u)
 	{		
 		if(!u.equals(start))
 		{
 			PriorityQueue<Integer> values = new PriorityQueue<Integer>(11);
 			for (Node s : u.getConnections())
 			{
 				values.add(s.getGScore() + g.getEdgeLength(s,u));
 			}
 			if (values.size() == 0)
 				return -1;
 			return values.peek();			
 		}
 		return 0;
 	}
 }
