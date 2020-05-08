 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class LPAstar extends AStar { 
 
 	final static int lineOfSight = 2;
 	
 	public static ArrayList<Integer> calculateKey(Node s, Node goal) 
 	{
 		ArrayList<Integer> key = new ArrayList<Integer>();
 		key.add(Math.min(s.getGScore(), s.getRhsScore() + hScore(s,goal)));
 		key.add(Math.min(s.getGScore(), s.getRhsScore()));
 		return key;
 	}
 
 	public static void initialize(Grid g, Node goal)
 	{
 		Comparator<Pair> pairComparator = new LPAPairComparator();
 		PriorityQueue<Pair> open_set = new PriorityQueue<Pair>(11, pairComparator); 
 		
 		for (Node s : g.getVision(goal, lineOfSight)) 
 		{
 			s.setGScore(1000); 
 			s.setRhsScore(1000);
 		}
 		goal.setRhsScore(0);
 		open_set.add(new Pair(goal,calculateKey(goal,goal)));
 	}
 
 	public static void updateVertex(Node u, Grid g, PriorityQueue<Pair> open_set, Node start, Node goal)
 	{
 		if(!u.equals(start))
 		{
 			u.setRhsScore(findRhs(u, start, g));
 		}
 
 		if (open_set.contains(u))
 			open_set.remove(u);
 		
 		if (u.getGScore() != u.getRhsScore())
 			open_set.add(new Pair(u,calculateKey(u, goal)));
 	}
 
 	public static void computeShortestPath(PriorityQueue<Pair> open_set, Node goal, Grid g)
 	{
 		Comparator<ArrayList<Integer>> keyComparator = new KeyComparator();
		while(keyComparator.compare(calculateKey(open_set.peek().getNode(), goal), calculateKey(goal, goal)) || 
				goal.getRhsScore() != goal.getGScore())
 		{
			Node u = open_set.poll().getNode();
 			if (u.getGScore() > u.getRhsScore())
 			{
 				u.setGScore(u.getRhsScore());
 				for (Node s : u.getConnections())
 					updateVertex(s, g, open_set, goal);
 			}
 			else
 			{
 				u.setGScore(1000);
 				for (Node s : g.getAdjacent(u)) 
 					updateVertex(s, g, open_set, goal);
 				updateVertex(u, g, open_set, goal);
 			}
 		}	
 	}
 
 	public void algorithm(Grid g, Node goal, Node start)
 	{
 		initialize(g, start);
 		computeShortestPath(open_set, goal, g);
 	}
 
 	public static Node[] reconstructPath(Node goal, Node start, Grid g)
 	{
 		ArrayList<Node> path = new ArrayList<Node> ();
 
 		if(goal.equals(start))
 		{
 			Node[] result = path.toArray(new Node[path.size()]);
 			return result;
 		}
 		else
 		{
 			PriorityQueue<Integer> values = new PriorityQueue<Integer>();
 			for (Node s : goal.getConnections())
 				values.add(s.getGScore() + g.edgelength);
 			int minval = values.peek();
 			path.add(minval);
 			reconstructPath(goal, minval, g);
 		}
 	}
 
 	public static int findRhs(Node u, Node start, Grid g)
 	{
 		if(u.equals(start))
 		{
 			u.setRhsScore(0);
 		}
 		else
 		{
 			PriorityQueue<Integer> values = new PriorityQueue<Integer>(11);
 			for (Node s : u.getConnections())
 			{
 				values.add(s.getGScore() + g.getEdgeLength(s,u));
 				s.setRhsScore(values.peek());	
 			}
 		}
 	}
 }
