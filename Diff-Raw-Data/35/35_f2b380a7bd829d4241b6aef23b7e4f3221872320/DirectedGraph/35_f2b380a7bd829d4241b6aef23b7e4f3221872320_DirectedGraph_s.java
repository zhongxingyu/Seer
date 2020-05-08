 package se.chalmers.datastrukt.lab3;
 
 import java.util.*;
 
 public class DirectedGraph<E extends Edge> {
 
 	private List<E>[] edges;
 	private CompKruskalEdge cKE = new CompKruskalEdge();
 	private int noOfNodes;
 	private PriorityQueue<DijkstraElement> prioQueue;
 
 	public DirectedGraph(int noOfNodes) {
 		this.noOfNodes = noOfNodes;
 		this.prioQueue = new PriorityQueue<DijkstraElement>();
 		edges = new List[noOfNodes];
 		for (int i = 0; i < noOfNodes; i++) {
 			edges[i] = new LinkedList<E>();
 		}
 	}
 
 	public void addEdge(E e) {
 		edges[e.getSource()].add(e);
		
		}
 
 	public Iterator<E> shortestPath(int from, int to) {
 		boolean[] visitedNode = new boolean[noOfNodes];
 
 		prioQueue.add(new DijkstraElement<E>(from));
 		while (prioQueue.size() != 0) {
 			DijkstraElement<E> dE = prioQueue.poll();
 			if (!visitedNode[dE.to]) {
 				if (dE.to == to) {
 					dE.getPath().iterator();
 				} else {
 					visitedNode[dE.to] = true;
 					for (E edge : this.edges[dE.to]) {
 						visitedNode[edge.to] = true;
 						prioQueue.add(new DijkstraElement(dE, edge));
 					}
 				}
 			}
 		}
 		return null;
 
 	}
 
 	public Iterator<E> minimumSpanningTree() {
 		return null;
 	}
 
 	private class CompKruskalEdge {
		
 	}
 
 	private class DijkstraElement<E extends Edge> implements
 			Comparable<DijkstraElement> {
 		// private public kmr t dem i klassen eftersom dem r publika men ej
 		// utanfr klassen eftersom dem r i en inre klass!
 		public int to;
 		public double weight;
 		public List<E> theShortestPath;
	
 		public DijkstraElement(int from) {
 			this.to = from;
 			this.weight = 0;
 			this.theShortestPath = new LinkedList<E>();
			
 
 		}
 
 		public DijkstraElement(DijkstraElement<E> elem, E edge) {
 			this.to = elem.to; // to?
			this.weight = elem.weight;// hmmm			
 			this.theShortestPath = new LinkedList<E>(elem.theShortestPath);
 			theShortestPath.add(edge);
 			this.weight += edge.getWeight();
			//this.to = to;

 		}
 		public List<E> getPath() {
 			List<E> temp = new LinkedList<E>();
			
			for(E elem : this.theShortestPath) {
 				temp.add(elem);
 			}
			
 			return temp;
 		}
 
 		public int compareTo(DijkstraElement other) {
 			return Double.compare(weight, other.weight);
 		}
 	}
 }
