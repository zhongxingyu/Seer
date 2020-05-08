 package alviz2.algo;
 
 import org.jgrapht.Graph;
 import org.jgrapht.Graphs;
 import org.jgrapht.VertexFactory;
 import org.jgrapht.EdgeFactory;
 
 import javafx.scene.paint.Color;
 import javafx.scene.chart.XYChart;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Stack;
 
 import alviz2.graph.Node;
 import alviz2.graph.Edge;
 import alviz2.util.*;
 
 @AlgorithmRequirements (
 	graphType = GraphType.COMPLETE_GRAPH,
 	graphInitOptions = {GraphInit.EDGE_COST}
 )
 public class BranchAndBound implements Algorithm<Node, Edge> {
 
 	Graph<Node, Edge> gph;
 	Node.PropChanger npr;
 	Edge.PropChanger epr;
 	Node curNode;
 	ArrayList<Edge> edges;
 	int edgeState[];
 	
 	int counter;
 	boolean forward;
 	Set<Edge> curredges;
 	Set<Edge> banned;
 	int currmin;
	double currcost;
 	Set<Edge> minsave;
 	
 
 	public BranchAndBound() {
 		gph = null;
 		npr = null;
 		currmin = Integer.MAX_VALUE;
 		currcost = 0;
 		counter = 0;
 		forward = true;
 		curredges = new HashSet<Edge>();
 		banned = new HashSet<Edge>();
 				
 	}
 
 	public static class VFac implements VertexFactory<Node> {
 		int id;
 		
 		private VFac() {
 			id = 0;
 		}
 
 		@Override public Node createVertex() {
 			return new Node(id++);
 		}
 	}
 
 	public static class EFac implements EdgeFactory<Node, Edge> {
 		int id;
 
 		private EFac() {
 			id = 0;
 		}
 
 		@Override public Edge createEdge(Node s, Node d) {
 			return new Edge(id++, s, d);
 		}
 	}
 
 	@Override public void setGraph(Graph<Node,Edge> graph, Node.PropChanger npr, Edge.PropChanger epr, Set<Node> Start, Set<Node> Goal)
 	{
 		gph = graph;
 		this.npr = npr;
 		this.epr = epr;
 
 		for (Node n: gph.vertexSet()) {
 			npr.setVisible(n, true);
 			curNode = n;
 		}
 		npr.setFillColor(curNode, Color.BLUE);
 		Set<Node> nodes = gph.vertexSet();
 		for(Node n:nodes){
 			Set<Edge> te = gph.edgesOf(n);
 			n.orderedEdges = new ArrayList<Edge>(te);
 			Collections.sort(n.orderedEdges);
 		}
 		edges = new ArrayList<Edge>(gph.edgeSet());
 		edgeState = new int[edges.size()];
 	}
 
 	@Override public void setChart(XYChart<Number,Number> chart)
 	{
 		return;
 	}
 
 	@Override public VertexFactory<Node> getVertexFactory()
 	{
 		return new VFac();
 	}
 
 	@Override public EdgeFactory<Node,Edge> getEdgeFactory()
 	{
 		return new EFac();
 	}
 	
 	
 	
 //	void bab(Set<Edge> sel, Set<Edge> rej){
 //		Node curr;
 //		double lb;
 //		double currcost = 0;
 //		while(true){
 //			curr = rem.pop();
 //			for (Edge e : gph.edgesOf(curr)) {
 //				Node other = otherEnd(e, curr);
 //				if (other.deg < 2) {
 //					lb = lowerbound(e, sel, rej, currcost);
 //					if (lb < lowestYet)
 //						rem.push(other);
 //				}
 //			}
 //			boolean isCycle;
 //			isCycle = bfs(gph.getEdgeSource(curr), gph.getEdgeTarget(curr), sel);
 //		}
 //	}
 	
 	Node otherEnd(Edge e, Node s) {
 		Node temps, tempt, other;
 		temps = gph.getEdgeSource(e);
 		tempt = gph.getEdgeTarget(e);
 		other = temps.equals(s) ? tempt : temps;
 		return other;
 	}	
 
 	boolean bfs(Node start, Node end, Set<Edge> edges){
 		Node curr = start;
 		//System.out.println("BFS: start " + start.id + " end " + end.id);
 		Set<Node> closed = new HashSet<Node>();
 		Queue<Node> nq = new LinkedList<Node>();
 		nq.add(start);
 		closed.add(start);
 		while(!nq.isEmpty() && curr != end){
 			curr = nq.poll();
 			for(Edge e:gph.edgesOf(curr)){
 				Node other = otherEnd(e,curr);
 				if(edges.contains(e) && !closed.contains(other)) {
 					nq.add(other);
 					closed.add(other);
 				}
 			}
 		}
 		boolean ret = curr.getId() == end.getId();
 		//System.out.println(curr.getId()+ " " + end.getId() + " " + ret);
 		return ret;
 	}	
 	@Override public boolean executeSingleStep()
 	{	
 	if(forward){
 			if(allowed(counter)){
 				select(counter);
 				if(curredges.size() == gph.vertexSet().size()){
 					int cost = 0;
 					for(Edge e:curredges) cost += e.getCost();
 					if(cost < currmin){
 						currmin = cost;
 						minsave = new HashSet<Edge>(curredges);
 						System.out.println("current min: " + currmin);
 					}
					else{
						System.out.println("cost:" + cost);
					}
 					forward = false;
 					return true;
 				}
 				if(counter == edges.size() - 1){ //> or >=??
 					forward = false;
 					return true;
 				}
 				counter++;
 				return true;
 			}
 			else{
 				ban(counter);
 			    counter++;
 				if(counter >= edges.size()){ //> or >=??
 					counter--;
 					forward = false;
 				}
 				return true;
 			}
 			
 		}
 		else{
 			while(true){
 				if(counter < 0) {
 					for(Edge e:minsave){
 						epr.setVisible(e, true);
 						epr.setStrokeColor(e, Color.GREEN);
 					}
 					return false;
 				}
 				assert(counter < edges.size());
 				if(counter == edges.size() - 1){
 					if(edgeState[counter] == -1){
 						unban(counter);
 					}
 					if(edgeState[counter] == 1){
 						deselect(counter);
 					}
 					counter--;
 				}
 				if(edgeState[counter] == 1){
 					deselect(counter);
 					ban(counter);
 					forward = true;
 					counter++;
 					return true;
 				}
 				else{
 					unban(counter);
                     counter--;
 				}
 			}
 		}
 	}
 	
 	boolean allowed(int edgenum){
 		Edge curr = edges.get(edgenum);
 		Node src, target;
 		src = gph.getEdgeSource(curr);
 		target = gph.getEdgeTarget(curr);
 		if(src.deg > 1 || target.deg > 1){
 			return false;
 		}
 		boolean isCycle = bfs(src, target, curredges);
 		if(isCycle && curredges.size() + 1 != gph.vertexSet().size()){
 			//TODO: is this correct???
 			return false;			
 		}
 		double lb;
 		select(edgenum);
 		lb = lowerbound();
 		deselect(edgenum);
 		return lb < currmin;
 	}
 	
 	double lowerbound() {
 		double lb;
 		lb = currcost;
 		for (Node n : gph.vertexSet()) {
 			if (n.deg == 0) {
 				int tmpcount = 0;
 				for (Edge e : n.orderedEdges) {
 					if (!banned.contains(e)) {
 						lb += e.getCost();
 						tmpcount++;
 					}
 					if (tmpcount == 2)
 						break;
 				}
 			}
 			if (n.deg == 1) {
 				for (Edge e : n.orderedEdges) {
 					if (!banned.contains(e) && !curredges.contains(e)) {
 						lb += e.getCost();
 						break;
 					}
 				}
 
 			}
 
 		}
 
 		return lb;
 	}
 	
 	void select(int edgenum){
 		Edge curr = edges.get(edgenum);
 		epr.setVisible(curr, true);
 		epr.setStrokeColor(curr, Color.BLACK);
 		curredges.add(curr);
 		gph.getEdgeSource(curr).deg++;
 		gph.getEdgeTarget(curr).deg++;
 		edgeState[edgenum] = 1;
 		currcost += curr.getCost();
 	}
 	void deselect(int edgenum){
 		Edge des = edges.get(edgenum);
 		//assert(currcost - des.getCost() >= 0);
 		epr.setVisible(des, false);
 		curredges.remove(des);
 		gph.getEdgeSource(des).deg--;
 		gph.getEdgeTarget(des).deg--;
 		edgeState[edgenum] = 0;
 		currcost -= des.getCost();
 	}
 	void ban(int edgenum){
 		Edge curr = edges.get(edgenum);
 		edgeState[edgenum] = -1;
 		banned.add(curr);
 		epr.setVisible(curr, true);
 		epr.setStrokeColor(curr, Color.RED);
 	}
 	void unban(int edgenum){
 		Edge curr = edges.get(edgenum);
 		edgeState[edgenum] = 0;
 		banned.remove(curr);
 		epr.setVisible(curr, false);
 	}
 	
 }
