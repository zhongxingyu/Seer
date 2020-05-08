 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtd.level;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Random;
 import jtd.PointI;
 
 /**
  *
  * @author LostMekka
  */
 public final class PathingGraph {
 	
 	public static final double WALK_COST = 0.6f;
 	public static final double WALK_DIAGONAL_COST = WALK_COST * (double)Math.sqrt(2f);
 	private static final Random random = new Random();
 	
 	public static class Transition{
 		public Node n;
 		public double p;
 		public Transition(Node loc, double p) {
 			this.n = loc;
 			this.p = p;
 		}
 	}
 	
 	public static class Node{
 		public PointI loc;
 		public LinkedList<Transition> transitions;
 		public double cumulativeCost, cumulativeP;
 		public Node(PointI loc, double cumulativeCost) {
 			this.loc = loc;
 			this.cumulativeCost = cumulativeCost;
 			transitions = new LinkedList<>();
 		}
 		public void addTransition(Node n, double p){
 			transitions.add(new Transition(n, p));
 		}
 		public Node getNextNode(){
 			if(transitions.isEmpty()) return null;
 			double maxP = 0d;
 			for(Transition t:transitions) maxP += t.p;
 			double ran = random.nextDouble() * maxP;
 			for(Transition t:transitions){
 				if(ran > t.p){
 					ran -= t.p;
 				} else {
 					return t.n;
 				}
 			}
 			return transitions.getLast().n;
 		}
 		public Node getShortestNode(){
 			if(transitions.isEmpty()) return null;
 			return transitions.getFirst().n;
 		}
 		public boolean containsTransitionTo(Node n){
 			for(Transition t:transitions){
 				if(t.n == n) return true;
 			}
 			return false;
 		}
 	}
 	
 	public class PathingGraphIterator implements Iterator<PointI>{
 		private Node currNode;
 		private PathingGraphIterator(Node currNode) {
 			this.currNode = currNode;
 		}
 		public PathingGraphIterator(PointI start) {
 			if(start.x < 0) start.x = 0;
 			if(start.y < 0) start.y = 0;
 			if(start.x >= nodes.length) start.x = nodes.length;
 			if(start.y >= nodes[0].length) start.y = nodes[0].length;
 			currNode = getNode(start);
 		}
 		@Override
 		public boolean hasNext() {
 			if(currNode == null) return false;
 			return !currNode.transitions.isEmpty();
 		}
 		@Override
 		public PointI next() {
 			if(currNode == null) return null;
 			currNode = currNode.getNextNode();
 			if(currNode == null) return null;
 			return currNode.loc;
 		}
 		public PointI shortest() {
 			if(currNode == null) return null;
 			currNode = currNode.getShortestNode();
 			if(currNode == null) return null;
 			return currNode.loc;
 		}
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("Not supported yet.");
 		}
 		public PointI getLastPoint() {
 			if(currNode == null) return null;
 			return currNode.loc;
 		}
 		public double getDistanceLeft(){
 			if(!hasNext()) return 0f;
 			PathingGraphIterator iter = new PathingGraphIterator(currNode);
 			double ans = 0f;
 			while(iter.hasNext()){
 				PointI p1 = iter.currNode.loc;
 				PointI p2 = iter.shortest();
 				ans += p1.distanceTo(p2);
 			}
 			return ans;
 		}
 	}
 	
 	public LinkedList<PointI> startingPoints;
 	public Node[][] nodes;
 	public long lastTime = 0;
 
 	public Node getNode(PointI p){
 		return nodes[p.x][p.y];
 	}
 	
 	public PathingGraphIterator iterator(PointI start){
 		return new PathingGraphIterator(start);
 	}
 	
 	public PathingGraphIterator iterator(){
 		return new PathingGraphIterator(startingPoints.get(random.nextInt(startingPoints.size())));
 	}
 	
 	public PathingGraph(int mobSize, LevelDataHolder level) {
 		generate(mobSize, level);
 	}
 	
 	private static final byte UNVISITED = 0;
 	private static final byte VISITING = 1;
 	private static final byte VISITED = 2;
 	
 	public long generate(int mobSize, LevelDataHolder level){
 		long startTime = System.currentTimeMillis();
 		nodes = new Node[level.w][level.h];
 		startingPoints = new LinkedList<>();
 		// init node list
 		LinkedList<Node> currNodes = new LinkedList<>();
 		for(PointI p:level.destinations.get(mobSize-1)){
 			currNodes.add(new Node(p, 0d));
 		}
 		double startingPointCost = -1f;
 		for(;;){
 			// construct list with all nodes, that have equal, minimal weights
 			LinkedList<Node> nodesToExpand = new LinkedList<>();
 			double currWeigth = -1;
 			ListIterator<Node> iter = currNodes.listIterator();
 			while(iter.hasNext()){
 				Node n = iter.next();
 				if(nodesToExpand.isEmpty()){
 					nodesToExpand.add(n);
 					iter.remove();
 					currWeigth = n.cumulativeCost;
 				} else {
 					if(n.cumulativeCost == currWeigth){
 						nodesToExpand.add(n);
 						iter.remove();
 					} else {
 						break;
 					}
 				}
 			}
 			// expand all nodes in that list
 			LinkedList<Node> newNodes = new LinkedList<>();
 			while(!nodesToExpand.isEmpty()){
 				Node node = nodesToExpand.get(random.nextInt(nodesToExpand.size()));
 				nodesToExpand.remove(node);
 				int x = node.loc.x;
 				int y = node.loc.y;
 				LinkedList<PointI> reach = level.getWalkableTilesFrom(node.loc, mobSize);
 				for(PointI p:reach){
 					if(!level.isWalkable(p, mobSize)) continue;
 					if(level.destinations.get(mobSize-1).contains(p)) continue;
 					int hDist = p.hammingDistanceTo(node.loc);
 					double cost;
 					int dx = p.x - x;
 					int dy = p.y - y;
 					if((hDist > 2) || (hDist < 1) || (Math.abs(dx) > 1) || (Math.abs(dy) > 1)) continue;
 					// node is valid for expansion. add it to new nodes
 					if(hDist == 1){
 						// straight movement
 						cost = WALK_COST;
 						if(dx == 0){
 							// vertical movement
 							int yy;
 							if(dy > 0){
 								yy = y + mobSize;
 							} else {
 								yy = y - 1;
 							}
 							for(int xx=x; xx<x+mobSize; xx++){
 								cost += (double)level.getPathingWeightAt(new PointI(xx, yy));
 							}
 						} else {
 							//horizontal movement
 							int xx;
 							if(dx > 0){
 								xx = x + mobSize;
 							} else {
 								xx = x - 1;
 							}
 							for(int yy=y; yy<y+mobSize; yy++){
 								cost += (double)level.getPathingWeightAt(new PointI(xx, yy));
 							}
 						}
 					} else {
 						// diagonal movement
 						int xx = p.x;
 						if(dx > 0) xx += mobSize - 1;
 						int yy = p.y;
 						if(dy > 0) yy += mobSize - 1;
 						cost = WALK_DIAGONAL_COST;
 						cost += level.getPathingWeightAt(new PointI(xx, yy));
 						for(int i=1; i<=mobSize; i++){
 							double cx = level.getPathingWeightAt(new PointI(xx, yy - dy * i));
 							double cy = level.getPathingWeightAt(new PointI(xx - dx * i, yy));
 							if(i == mobSize){
 								cost += 0.5f * (cx + cy);
 							} else {
 								cost += cx + cy;
 							}
 						}
 					}
 					cost += node.cumulativeCost;
 					// get node or construct it if it is not there yet
 					Node n = getNode(p);
 					boolean alreadyVisited = false;
 					if(n == null){
 						n = new Node(p, cost);
 						nodes[p.x][p.y] = n;
 						n.addTransition(node, 1d);
 					} else {
 						alreadyVisited = true;
 						if(!node.containsTransitionTo(n)){
							double splashRate = level.getSplashDamageRate(node.loc, mobSize);
							n.addTransition(node, n.cumulativeCost / cost * (1d - splashRate));
 						}
 					}
 					if(!alreadyVisited && !newNodes.contains(n)) newNodes.add(n);
 					// add location to stating point list, if cost is not higher
 					// TODO: make starting points selectable with a node
 					if(level.sources.get(mobSize-1).contains(p)){
 						if(startingPoints.isEmpty() || (n.cumulativeCost <= startingPointCost)){
 							startingPoints.add(p);
 							startingPointCost = n.cumulativeCost;
 						}
 					}
 				}
 			}
 			// add new nodes to curr nodes, mark them as visited
 			for(Node n:newNodes){
 				insertNode(n, currNodes);
 			}
 			if(currNodes.isEmpty()) break;
 		}
 		long endTime = System.currentTimeMillis();
 		lastTime = endTime - startTime;
 		return lastTime;
 	}
 
 	private void insertNode(Node node, LinkedList<Node> list){
 		// if list is empty, insert node as the only element
 		if(list.isEmpty()){
 			list.add(node);
 			return;
 		}
 		// traverse the list and search for the first node with same or equal weight
 		ListIterator<Node> iter = list.listIterator();
 		while(iter.hasNext()){
 			Node next = iter.next();
 			if(next.cumulativeCost >= node.cumulativeCost){
 				// greater node found. insert node one step before this one
 				iter.previous();
 				iter.add(node);
 				return;
 			}
 		}
 		// no node has a greater or equal weight. insert this node at the end of the list
 		list.addLast(node);
 	}
 	
 }
