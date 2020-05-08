 package GKA_A4;
 
 import java.lang.IllegalArgumentException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import GKA_A1.IAIGraph;
 
 public class Hierholzer {
 
 	private final Long NULL_LONG = -1L;
 	private IAIGraph graph;
 
 	private Set<Long> allEdges;
 
 	public Hierholzer(IAIGraph graph) {
 		this.graph = graph;
 	}
 
 	// Main algorithm
 	public List<Long> hierholzeEs() {
 		resetVariables();
 
 		checkEdgesHaveEvenDegree();
 
 		// Step 1
 		Long v = getInitialVertice();
 		// K is a List of Edges
 		List<Long> k = makeCycleBeginningAtUsingEdges(v, allEdges);
 
 		// Step 2
 		while (!isEulerianPath(graph, k)) {
 
 			// Step 3
 			// vernachlaessige Kanten von K
			Set<Long> edgesWithoutK = new HashSet<Long>(k);
			edgesWithoutK.removeAll(allEdges);
 
 			// Step 4
 			Long newV = getVerticeInKWithPositiveDegree(k);
 
 			if (newV == NULL_LONG)
 				gotoFail("getEdgePos not working. returned -1");
 
 			List<Long> newK = makeCycleBeginningAtUsingEdges(newV,
 					edgesWithoutK);
 
 			// Step 5 and 6
 			// K is updated here
 			// the Vertice newK starts form is also given over,
 			// because it simplfies the function
 			k = integrateLeftCycleIntoRightCycle(newK, k, newV);
 		}
 		return k;
 	}
 
 	// Voraussetzung: Sei G=(V,E) ein zusammenhaengender Graph, der nur Knoten
 	// mit geradem Grad aufweist.
 	// 1. Waehle einen beliebigen Knoten v_0 des Graphen und konstruiere von v_0
 	// ausgehend einen Unterkreis K in G, der keine Kante in G zweimal
 	// durchlaeuft.
 	// 2. Wenn K ein Eulerkreis ist, breche ab. Andernfalls:
 	// 3. Vernachlaessige nun alle Kanten des Unterkreises K.
 	// 4. Am ersten Eckpunkt von K, dessen Grad groesser 0 ist, laesst man nun
 	// einen weiteren Unterkreis K'
 	// entstehen, der keine Kante in K durchlaeuft und keine Kante in G zweimal
 	// enthaelt.
 	// 5. Fuege in K den zweiten Kreis K' ein, indem der Startpunkt von K' durch
 	// alle Punkte von K'
 	// in der richtigen Reihenfolge ersetzt wird.
 	// 6. Nenne jetzt den so erhaltenen Kreis K und fahre bei Schritt 2 fort.
 
 	// Aus Wikipedia: https://de.wikipedia.org/wiki/Algorithmus_von_Hierholzer
 	// (12.12.13)
 
 	private Long getInitialVertice() {
 		for (Long v : graph.getVertexes()) {
 			return v;
 		}
 		return NULL_LONG;
 	}
 
 	private List<Long> integrateLeftCycleIntoRightCycle(List<Long> newK,
 			List<Long> k, Long newKStart) {
 
 		// for vertices a and b the notation a:b means the edge (in newK or K)
 		// inbetween the vertices a and b
 
 		// K: [ 1:7, 7:3, 3:2, 2:1 ]
 		// newK: [ 3:1, 1:8, 8:7, 7:4, 4:3 ]
 		// integrated: [ 1:7, 7:3, 3:1, 1:8, ... 4:3, 3:2, 2:1]
 
 		// We know, that the start Vertice of newK is contained in K.
 		// Therefore, we need to find the first occurrence of this vertice in K
 		// and insert newK at this position
 
 		// the way wikipedia explains this is ambiguous.
 		// therefore we're using this sligtly different approach
 
 		List<Long> mergedK = new ArrayList<>(newK);
 
 		System.out.println("Going to Integrate");
 		System.out.println("K: " + k + "; newK: " + newK + "; startV: "
 				+ newKStart);
 
 		// we start at i=1 because we always take the edge (i-1) and (i)
 		for (int i = 1; i < k.size(); i++) {
 
 			Long prevE = k.get(i - 1);
 			Long currE = k.get(i);
 
 			// calculate the Vertice inbetween both of the edges
 			// (this looks so horrible in java.....)
 
 			Long verticeInbetween = NULL_LONG;
 			Set<Long> common = commonVerticesOf(prevE, currE, graph);
 			Set<Long> intersect = new HashSet<>(common);
 			intersect.retainAll(graph.getSourceTarget(currE));
 			for (Long v : intersect) {
 				verticeInbetween = v;
 				break;
 			}
 
 			if (verticeInbetween.equals(newKStart)) {
 				// the currE is an "outgoing" edge from the newKStart vertice
 
 				// now we have to add the newK cycle inbetween these two edges
 				// now i has the index of the second edge.
 
 				// using this List-Operation, java will
 				// shift this second edge and all subsequent edges
 				// and insert the edges of newK in the expected order
 				// into this position
 
 				mergedK.addAll(i, newK);
 
 				// important to end now. else it would add it multiple times.
 				return mergedK;
 			}
 
 		}
 
 		gotoFail("whoooosh!..... this shouldnt happen! the insertion Vertice wasnt found!");
 		
 		return mergedK;
 	}
 
 	// Java has to have GOTO
 	private void gotoFail(String err_msg) {
 		System.err.println(err_msg);
 		throw new IllegalArgumentException(err_msg);
 	}
 
 	private Long getVerticeInKWithPositiveDegree(List<Long> k) {
 		for (Long e : k) {
 			for (Long v : graph.getSourceTarget(e)) {
 				if (degree(v) > 0) {
 					return v;
 				}
 			}
 
 		}
 		return NULL_LONG;
 	}
 
 	private List<Long> makeCycleBeginningAtUsingEdges(Long startVertice,
 			Set<Long> usableEdges) {
 
 		List<Long> cycleEdges = new ArrayList<>();
 		Long currHeadVertice = startVertice;
 
 		// System.out.println("startVertice: " + startVertice);
 
 		while(cycleEdges.size() < 2 || !lastEdgeReachedVertice(cycleEdges, startVertice)){
 			// this method returns a list with two elements.
 			// the first element is the eID of the chosen edge and the
 			// second element id the vId of the corresponging parter vertice
 			// of the given currHeadVertice
 			// the method name is to be read as:
 			// pick next edge from "currHEadVertice" using "usableEdges"
 
 			System.out.println("CurrHead: " + currHeadVertice + "; Usable Edges: " + usableEdges);
 			List<Long> container = pickNextEdgeFrom_Using_(currHeadVertice,
 					usableEdges);
 
 			Long usedEdge = container.get(0);
 			Long nextVertice = container.get(1);
 
 			usableEdges.remove(usedEdge);
 			cycleEdges.add(usedEdge);
 
 			// begin form the next Vertice in the next iteration
 			currHeadVertice = nextVertice;
 
 		}
 		
 		// identified bug.
 		// the loop is only entered once in the first iteration.
 		// it has to loop atleast twice.
 
 		return cycleEdges;
 	}
 
 	private List<Long> pickNextEdgeFrom_Using_(Long currHeadVertice,
 			Set<Long> usableEdges) {
 
 		Set<Long> incident = graph.getIncident(currHeadVertice);
 
 		// build set od edge incident with the head vertice and
 		// which are also allowed to be used.
 		Set<Long> intersect = new HashSet<>(usableEdges);
 		intersect.retainAll(incident);
 
 		// this set has to have atleast one edge!
 		if (intersect.isEmpty()) {
 			gotoFail(" <<< --- whoooooops!!! --- >>> \nArguments: " + currHeadVertice + " and "
 					+ usableEdges);
 		}
 
 		// fetch the edge and vertice
 		Long edge = NULL_LONG;
 		for (Long e : intersect) { // java has no better way of getting a single
 									// elem out of a set -_-
 			edge = e;
 			break;
 		}
 		if (edge == NULL_LONG)
 			gotoFail("nooo.......");
 
 		Long vertice = NULL_LONG;
 		for (Long v : graph.getSourceTarget(edge)) {
 			if (v != currHeadVertice) {
 				vertice = v;
 			}
 		}
 
 		if (vertice == NULL_LONG || edge == NULL_LONG) {
 			gotoFail(" <---- Nil! ----> \nArguments: " + currHeadVertice + " and "
 					+ usableEdges);
 		}
 		return new ArrayList<>(Arrays.asList(edge, vertice));
 	}
 
 	private boolean lastEdgeReachedVertice(List<Long> cycleEdges, Long v) {
 		Long lastEdge = cycleEdges.get(cycleEdges.size() - 1);
 		return graph.getSourceTarget(lastEdge).contains(v);
 	}
 
 	public static boolean isEulerianPath(IAIGraph graph, List<Long> edges) {
 		if (new HashSet<Long>(edges).size() != graph.getEdges().size())
 			return false;
 
 		for (int i = 0; i < edges.size() - 1; i++) {
 			Long currE = edges.get(i);
 			Long nextE = edges.get(i + 1);
 
 			// check, that there is a vertice between two consecutive edges
 			if (commonVerticesOf(currE, nextE, graph).size() > 3) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	// could be refactored into Graph ADT
 	private static Set<Long> commonVerticesOf(Long e1, Long e2, IAIGraph graph) {
 		Set<Long> commonVertices = graph.getSourceTarget(e1);
 		commonVertices.addAll(graph.getSourceTarget(e2));
 		return commonVertices;
 	}
 
 	private void checkEdgesHaveEvenDegree() {
 		// check that all vertices have an even number of edges
 		// and that the graph is directed
 		if (graph.isDirected()) {
 			throw new IllegalArgumentException("Graph may not be undirected");
 		}
 		for (Long v : graph.getVertexes()) {
 			if (degree(v) % 2 != 0) {
 				throw new IllegalArgumentException(
 						"Graph has Vertices with odd degree!");
 			}
 		}
 	}
 
 	private int degree(Long v) {
 		return graph.getIncident(v).size();
 	}
 
 	private void resetVariables() {
 		this.allEdges = this.graph.getEdges();
 	}
 
 }
