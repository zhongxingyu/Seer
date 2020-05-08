 package routefiend;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 public class DjikstraSolver
 {
 	public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;
 	private static final int INITIAL_CAPACITY = 8;
 	
 	private final Comparator<Intersection> shortestDistanceComparator = new Comparator<Intersection>()
 	{
 		public int compare(Intersection left, Intersection right)
 		{
 			// note that this trick doesn't work for huge distances, close to Integer.MAX_VALUE
 			int result = getShortestDistance(left) - getShortestDistance(right);
 
 			return (result == 0) ? left.compareTo(right) : result;
 		}
 	};
 	
 	private final PriorityQueue<Intersection> unvisitedNodes = new PriorityQueue<Intersection>(INITIAL_CAPACITY, shortestDistanceComparator);
 	private final Set<Intersection> visitedNodes = new HashSet<Intersection>();
 	
 	private final Map<Intersection, Integer> shortestDistances = new HashMap<Intersection, Integer>();
 	private final Map<Intersection, Intersection> predecessors = new HashMap<Intersection, Intersection>();
 	
 
     private void init(Intersection start)
     {
     	visitedNodes.clear();
         unvisitedNodes.clear();
         
         shortestDistances.clear();
         predecessors.clear();
         
         // add source
         setShortestDistance(start, 0);
         unvisitedNodes.add(start);
     }
     
     public DjikstraSolver(String address, String username, String password)
     {
     	Street.readStreetsFrom(address, username, password);
     	Intersection.readIntersectionFrom(address, username, password);
     	TrafficLoad.readTrafficsFrom(address, username, password);
     }
     
     public int[] solveForDestinationsAndTimes(int[] destinationIds, String[] times)
     {
     	int[] result = new int[0];
 		return result;
     	
     }
     
     public int[] findShortestDistance(int startId, int endId, int hour)
     {
     	Intersection start = Intersection.getIntersectionForId(startId);
     	Intersection end = Intersection.getIntersectionForId(endId);
     	init(start);
     	Intersection current;
     	while((current = unvisitedNodes.poll()) != null)
     	{
     		if(current == end)
     		{
     			Intersection ancient = predecessors.get(current);
     			System.out.print(current.name() + " ");
     			while(ancient != null)
     			{
     				System.out.print(ancient.name() + " ");
     				ancient = predecessors.get(ancient);
     			}
     			System.out.println();
     			break;
     		}
     		
     		visitedNodes.add(current);
     		computeShortestToNeighbours(current, 9);
     	}
     	int[] route = new int[predecessors.size() + 1];
     	int id = 1;
     	route[0] = end.id();
     	while(current != null)
     	{
     		current = predecessors.get(current);
     		route[id] = current.id();
     		id++;
     	}
     	
     	return route;
     }
     
     public void computeShortestToNeighbours(Intersection intersect, int hour)
     {
     	for(TrafficLoad route : TrafficLoad.getNeighbours(intersect))
     	{
     		Intersection neighbour = (route.intersection1() == intersect) ? route.intersection2() : route.intersection1();
     		if(visitedNodes.contains(neighbour))
     			continue;
     		
     		int shortest = getShortestDistance(intersect) + route.load()[hour];
     		if(shortest < getShortestDistance(neighbour))
     		{
     			setShortestDistance(neighbour, shortest);
     			predecessors.put(neighbour, intersect);
     		}
     			
     	}
     }
     
     public int getShortestDistance(Intersection intersect)
     {
         Integer d = shortestDistances.get(intersect);
         return (d == null) ? INFINITE_DISTANCE : d;
     }
     
     //Rebalance the queue, according to the comparator, when setting new distance
     private void setShortestDistance(Intersection intersect, int distance)
     {
     	unvisitedNodes.remove(intersect);
     	shortestDistances.put(intersect, distance);
     	unvisitedNodes.add(intersect);
     }
 }
