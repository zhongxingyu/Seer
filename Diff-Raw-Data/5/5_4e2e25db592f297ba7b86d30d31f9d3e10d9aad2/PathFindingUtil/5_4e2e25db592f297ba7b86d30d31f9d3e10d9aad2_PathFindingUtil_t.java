 package edu.drexel.cs680.prj1.pathfinding;
 
 import java.util.List;
 
 import edu.drexel.cs680.prj1.executeorders.Node;
 import edu.drexel.cs680.prj1.logistics.Logistics.Squadron;
 import eisbot.proxy.JNIBWAPI;
 
 public class PathFindingUtil {
 	private JNIBWAPI bwapi;
 	public static PathFindingUtil instance;
 
 	public enum ALGO {
 		ASTAR,
 		TBASTAR
 	}
 
 	public PathFindingUtil(JNIBWAPI bwapi) {
 		instance = this;
 		this.bwapi = bwapi;
 	}
 
 	public List<Node> findPath(int xStart, int yStart, int xGoal,
 			int yGoal) {
 		List<Node> path = findPath(xStart, yStart, xGoal, yGoal, ALGO.ASTAR, null);
 		if (path == null) {
 			System.err.println("Path is null!!");
 		} else {
			System.out.println(String.format("algo:%s path has %d steps", ALGO.TBASTAR, path.size()));
 		}
 		
 		return path;
 	}
 
 	public List<Node> findPath(int xStart, int yStart, int xGoal,
 			int yGoal, ALGO algo, Squadron squad) {
 		List<Node> path = null;
 		Node start = new Node(xStart, yStart);
 		Node goal = new Node(xGoal, yGoal);
 
 		switch (algo) {
 		case ASTAR:
 			PathFinding aStar = new AStar(start, goal, bwapi);
 			path = aStar.calc(Integer.MAX_VALUE, Integer.MAX_VALUE);
 			break;
 
 		case TBASTAR:
 			PathFinding tbaStar = new TBAStar(start, goal, bwapi, squad);
			path = tbaStar.calc(100, 100);
 			break;
 			
 		default:
 			aStar = new AStar(start, goal, bwapi);
 			path = aStar.calc(Integer.MAX_VALUE, Integer.MAX_VALUE);
 			break;
 		}
 
 		return path;
 	}
 }
