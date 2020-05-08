 package graphimplementations;
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Sets;
 
 import abstractclasses.AbstractGraph;
 
 /**
  * 
  * A class to implement a multigraph, seen as a collection of vertices and edges
  * (possibly repeated) between these. The graph can be directed or undirected.
  * 
  * @author mmcasetti
  * 
  */
 
 public class EdgesGraph extends AbstractGraph {
 
 	private Set<Vertex> vertices;
 	private Multiset<Edge> undirectedEdges;
 	private Multiset<Edge> directedEdges;
 
 	/**
 	 * @param vertices: vertices of the graph
 	 * @param edges: undirected edges of the graph
 	 * @param directed: directed edges of the graph
 	 * if there are directed edges, there are no undirected edges - and 
 	 * vice versa.
 	 * 
 	 */
	public EdgesGraph(Set<Vertex> vertices, Multiset<Edge> undirectedEdges, Multiset<Edge> directed) {
 		if (!undirectedEdges.isEmpty()) {
 			Preconditions.checkArgument(directedEdges.isEmpty(), "Choose between directed and undirected graph.");
 		}
 		if (!directedEdges.isEmpty()) {
 			Preconditions.checkArgument(undirectedEdges.isEmpty(), "Choose between directed and undirected graph.");
 		}
 		for (Edge edge : undirectedEdges) {
 			Preconditions.checkArgument(
 					vertices.contains(edge.getStart()) && vertices.contains(edge.getEnd()),
 					"Endpoints of edges not in vertices");
 			Preconditions.checkArgument(!edge.isDirected(), "Directed edge in undirected edges");
 		}
		for (Edge edge : directed) {
 			Preconditions.checkArgument(
 					vertices.contains(edge.getStart()) && vertices.contains(edge.getEnd()),
 					"Endpoints of edges not in vertices");
 			Preconditions.checkArgument(edge.isDirected(), "Undirected edge in directed edges");
 		}
 		
 		this.vertices = vertices;
 		this.undirectedEdges = undirectedEdges;
		this.directedEdges = directed;
 	}
 
 	/**
 	 * @param originalGraph is copied
 	 */
 	public EdgesGraph(EdgesGraph originalGraph) {
 		this(originalGraph.getVertices(), originalGraph.getUndirectedEdges(), originalGraph.getDirectedEdges());
 	}
 
 	/**
 	 * A generic constructor
 	 */
 	public EdgesGraph() {
 		this(Sets.<Vertex>newHashSet(),
 				HashMultiset.<Edge>create(),
 				HashMultiset.<Edge>create());
 	}
 
 	@Override
 	public Set<Vertex> getVertices() {
 		return vertices;
 	}
 
 	@Override
 	public Multiset<Edge> getUndirectedEdges() {
 		return undirectedEdges;
 	}
 
 	@Override
 	public Multiset<Edge> getDirectedEdges() {
 		return directedEdges;
 	}
 
 	@Override
 	public boolean isDirected() {
 		return (directedEdges.size() > 0);
 	}
 
 	@Override
 	public EdgesGraph makeUndirected() {
 		if (!this.isDirected()) {
 			return this;
 		}
 		
 		Multiset<Edge> edges = HashMultiset.create();
 		for (Edge d : directedEdges) {
 			Edge e = Edge.between(d.getStart()).and(d.getEnd());
 			edges.add(e);
 		}
 		Multiset<Edge> directed = HashMultiset.create();		
 		return new EdgesGraph(getVertices(), edges, directed);
 	}
 	
 	@Override
 	public EdgesGraph makeDirected() {
 		if (this.isDirected()) {
 			return this;
 		}
 		
 		Multiset<Edge> directed = HashMultiset.create();
 		for (Edge e : undirectedEdges) {
 			Edge d1 = Edge.from(e.getStart()).to(e.getEnd());
 			undirectedEdges.add(d1);
 			Edge d2 = Edge.from(e.getEnd()).to(e.getStart());
 			undirectedEdges.add(d2);
 		}
 		Multiset<Edge> edges = HashMultiset.create();		
 		return new EdgesGraph(getVertices(), edges, directed);
 	}
 
 	@Override
 	public int getNoOfVertices() {
 		return getVertices().size();
 	}
 
 	@Override
 	public Multiset<Edge> getEdgesAt(Vertex vertex) {
 		Preconditions.checkArgument(!isDirected(), "Undirected graph");
 		Multiset<Edge> edgesAt = HashMultiset.create();
 		for (Edge e : getUndirectedEdges()) {
 			if (e.getStart().equals(vertex) || e.getEnd().equals(vertex)) {
 				edgesAt.add(e);
 			}
 		}
 		return edgesAt;
 	}	
 	
 	@Override
 	public Multiset<Edge> getEdgesFrom(Vertex vertex) {
 		Preconditions.checkArgument(isDirected(), "Directed graph");
 		
 		Multiset<Edge> edgesFrom = HashMultiset.create(); 
 		for (Edge d : getDirectedEdges()) {
 			if (d.getStart().equals(vertex)) {
 				edgesFrom.add(d);
 			}
 		}
 		return edgesFrom;
 	}
 
 	@Override
 	public Multiset<Edge> getEdgesTo(Vertex vertex) {
 		Preconditions.checkArgument(isDirected(), "Directed graph");
 
 		Multiset<Edge> edgesTo = HashMultiset.create(); 
 		for (Edge d : getDirectedEdges()) {
 			if (d.getEnd().equals(vertex)) {
 				edgesTo.add(d);
 			}
 		}
 		return edgesTo;
 	}
 
 	@Override
 	public int getDegreeAt(Vertex vertex) {
 		return getEdgesAt(vertex).size();
 	}
 
 	@Override
 	public int getOutdegreeAt(Vertex vertex) {
 		return getEdgesFrom(vertex).size();
 	}
 
 	@Override
 	public int getIndegreeAt(Vertex vertex) {
 		return getEdgesTo(vertex).size();
 	}
 
 	@Override
 	public void addVertices(Set<Vertex> newVertices) {
 		for (Vertex v : newVertices) {
 			Preconditions.checkArgument(!getVertices().contains(v),
 					"New vertex already in graph.");
 		}
 
 		getVertices().addAll(newVertices);
 	}
 
 	@Override
 	public void removeVertex(Vertex vertex) {
 		Preconditions.checkArgument(getVertices().contains(vertex),
 				"Vertex not in graph.");
 
 		if (!isDirected()) {
 			for (Edge e : getEdgesAt(vertex)) {
 				getUndirectedEdges().remove(e);
 			}			
 		} else {
 			for (Edge d : getEdgesFrom(vertex)) {
 				getDirectedEdges().remove(d);
 			}
 			for (Edge d : getEdgesTo(vertex)) {
 				getDirectedEdges().remove(d);
 			}
 		}
 		getVertices().remove(vertex);
 	}
 
 	@Override
 	public void addUndirectedEdge(Edge edge) {
 		Preconditions.checkArgument(getVertices().contains(edge.getStart())
 				&& getVertices().contains(edge.getEnd()),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(!edge.isDirected() && !isDirected(), 
 				"Use addDirectedEdge.");
 
 		getUndirectedEdges().add(edge);
 	}
 
 	@Override
 	public void addDirectedEdge(Edge edge) {
 		Preconditions.checkArgument(getVertices().contains(edge.getStart())
 				&& getVertices().contains(edge.getEnd()),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(edge.isDirected() && isDirected(), 
 				"Use addUndirectedEdge");
 
 		getDirectedEdges().add(edge);
 	}
 
 	@Override
 	public void addUndirectedEdge(Vertex start, Vertex end) {
 		Preconditions.checkArgument(
 				getVertices().contains(start) && getVertices().contains(end),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(!isDirected(), 
 				"Use addDirectedEdge.");
 
 		Edge edge = Edge.between(start).and(end);
 		getUndirectedEdges().add(edge);
 	}
 
 	@Override
 	public void addDirectedEdge(Vertex start, Vertex end) {
 		Preconditions.checkArgument(
 				getVertices().contains(start) && getVertices().contains(end),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(isDirected(), 
 				"Use addUndirectedEdge.");
 
 		Edge edge = Edge.from(start).to(end);
 		getDirectedEdges().add(edge);
 	}
 
 	@Override
 	public void removeUndirectedEdge(Edge edge) {
 		Preconditions.checkArgument(getVertices().contains(edge.getStart())
 				&& getVertices().contains(edge.getEnd()),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(!edge.isDirected() && !isDirected(),
 				"Use removeDirectedEdge.");
 		Preconditions.checkArgument(getUndirectedEdges().contains(edge), 
 				"Edge not in graph.");
 
 		getUndirectedEdges().remove(edge);
 	}
 
 	@Override
 	public void removeDirectedEdge(Edge edge) {
 		Preconditions.checkArgument(getVertices().contains(edge.getStart())
 				&& getVertices().contains(edge.getEnd()),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(edge.isDirected() && isDirected(), 
 				"Use removeUndirectedEdge.");
 		Preconditions.checkArgument(
 				getDirectedEdges().contains(edge),
 				"Edge not in graph.");
 
 		getDirectedEdges().remove(edge);
 	}
 
 	@Override
 	public void removeUndirectedEdge(Vertex start, Vertex end) {
 		Preconditions.checkArgument(getVertices().contains(start) && 
 				getVertices().contains(end), "Edge's endpoints not in graph.");
 		Preconditions.checkArgument(!isDirected(), 
 				"Use removeDirectedEdge.");
 		
 		Edge edge = Edge.between(start).and(end);
 		Preconditions.checkArgument(getUndirectedEdges().contains(edge), 
 				"Edge not in graph.");
 
 		getUndirectedEdges().remove(edge);
 	}
 
 	@Override
 	public void removeDirectedEdge(Vertex start, Vertex end) {
 		Preconditions.checkArgument(
 				getVertices().contains(start) && getVertices().contains(end),
 				"Edge's endpoints not in graph.");
 		Preconditions.checkArgument(isDirected(), 
 				"Use removeUndirectedEdge.");
 		Edge edge = Edge.from(start).to(end);
 		Preconditions.checkArgument(directedEdges.contains(edge),
 				"Edge not in graph.");
 
 		getDirectedEdges().remove(edge);
 	}
 	
 	public boolean isEulerian() {
 		if (!isDirected()) {
 			for (Vertex v : getVertices()) {
 				if (getDegreeAt(v) % 2 != 0) {
 					return false;
 				}
 			}
 		} else {
 			for (Vertex v : getVertices()) {
 				if ((getOutdegreeAt(v) != getIndegreeAt(v))) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public boolean isPerfectMatching(Multiset<Edge> subset) {
 		for (Edge e : subset) {
 			Preconditions.checkArgument(
 					undirectedEdges.contains(e) || directedEdges.contains(e),
 					"Edge of subset not in graph.");
 		}
 		// if there is an odd no. of vertices, no perfect matching
 		if (getNoOfVertices() % 2 == 1) {
 			return false;
 		}
 		// else check that there are exactly noOfVertices/2 edges
 		if (subset.size() != getNoOfVertices() / 2) {
 			return false;
 		}
 		// check that each vertex is covered (this will imply that it is covered
 		// once, since the n. of edges in subset is noOfVertices/2)
 		boolean isCovered;
 
 		for (Vertex i : getVertices()) {
 			isCovered = false;
 			for (Edge edge : subset) {
 				// we do not need to use a xor to avoid loops
 				// because we already know that subset.size() == noOfVertices/2
 				if (edge.getStart().equals(i) || edge.getEnd().equals(i)) {
 					if (isCovered) {
 						return false;
 					}
 					isCovered = true;
 				}
 			}
 			if (!isCovered) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public List<Vertex> getEulerianCycle(EdgesGraph graph, Vertex startNode) {
 		Preconditions.checkArgument(vertices.contains(startNode),
 				"Start node not in graph");
 		Preconditions.checkArgument(graph.isEulerian(), "Graph not Eulerian.");
 
 		final EdgesGraph copyOfGraph = new EdgesGraph(graph);
 		List<Vertex> eulerianCycle = findAndRemoveCycle(copyOfGraph, startNode);
 
 		if (!graph.isDirected()) {
 			while (!copyOfGraph.getUndirectedEdges().isEmpty()) {
 				Vertex junction = Iterables.find(eulerianCycle,
 						new Predicate<Vertex>() {
 							@Override
 							public boolean apply(Vertex vertex) {
 								return copyOfGraph.getDegreeAt(vertex) > 0;
 							}
 						});
 				List<Vertex> cycle = findAndRemoveCycle(copyOfGraph, junction);
 				eulerianCycle = graph.mergeTours(Optional.of(eulerianCycle),
 						Optional.of(cycle), junction);
 			}			
 		} else {
 			while (!copyOfGraph.getDirectedEdges().isEmpty()) {
 				Vertex junction = Iterables.find(eulerianCycle,
 						new Predicate<Vertex>() {
 							@Override
 							public boolean apply(Vertex vertex) {
 								return copyOfGraph.getOutdegreeAt(vertex) > 0;
 							}
 						});
 				List<Vertex> cycle = findAndRemoveCycle(copyOfGraph, junction);
 				eulerianCycle = graph.mergeTours(Optional.of(eulerianCycle),
 						Optional.of(cycle), junction);
 			}
 		}
 		return eulerianCycle;
 	}
 	
 	private List<Vertex> findAndRemoveCycle(EdgesGraph edgesGraph,
 			Vertex startNode) {
 		List<Vertex> cycle = Lists.newArrayList(edgesGraph.getCycle(edgesGraph,
 				startNode).get());
 		if (!edgesGraph.isDirected()) {
 			for (Integer i = 0; i < cycle.size() - 1; i++) {
 				edgesGraph.removeUndirectedEdge(cycle.get(i), cycle.get(i + 1));
 			}
 			edgesGraph.removeUndirectedEdge(cycle.get(cycle.size() - 1), cycle.get(0));
 		} else {
 			for (Integer i = 0; i < cycle.size() - 1; i++) {
 				edgesGraph.removeDirectedEdge(cycle.get(i), cycle.get(i + 1));
 			}
 			edgesGraph.removeDirectedEdge(cycle.get(cycle.size() - 1), cycle.get(0));
 		}
 		return cycle;			
 	}
 
 	public Optional<List<Vertex>> getCycle(EdgesGraph edgesGraph,
 			Vertex startNode) {
 		Preconditions.checkArgument(vertices.contains(startNode),
 				"Start node not in graph");
 
 		List<Vertex> cycle = Lists.newArrayList();
 		Vertex currentNode = startNode;
 		EdgesGraph copyOfGraph = new EdgesGraph(edgesGraph);
 
 		if (!edgesGraph.isDirected()) {
 			do {
 				cycle.add(currentNode);
 				Multiset<Edge> edgesAt = copyOfGraph.getEdgesAt(currentNode);
 				if (edgesAt.isEmpty()) {
 					return Optional.<List<Vertex>> absent();
 				}
 				// we take the last element in edgesAt - any edge is ok;
 				// we already know (previous clause) that edgesAt is not empty.
 				Edge edgeFromCurrentNode = Iterables.getLast(edgesAt);
 				Vertex nextNode = (edgeFromCurrentNode.getStart()
 						.equals(currentNode)) ? edgeFromCurrentNode.getEnd()
 						: edgeFromCurrentNode.getStart();
 
 				copyOfGraph.removeUndirectedEdge(currentNode, nextNode);
 				currentNode = nextNode;
 			} while (!currentNode.equals(startNode));			
 		} else {
 			do {
 				cycle.add(currentNode);
 				Multiset<Edge> edgesFrom = copyOfGraph.getEdgesFrom(currentNode);
 				if (edgesFrom.isEmpty()) {
 					return Optional.<List<Vertex>> absent();
 				}
 				// we take the last element in edgesFrom - any edge is ok;
 				// we already know (previous clause) that edgesFrom is not empty.
 				Edge edgeFromCurrentNode = Iterables.getLast(edgesFrom);
 				Vertex nextNode = edgeFromCurrentNode.getEnd();
 
 				copyOfGraph.removeDirectedEdge(currentNode, nextNode);
 				currentNode = nextNode;
 			} while (!currentNode.equals(startNode));			
 		}
 		
 		Optional<List<Vertex>> c = Optional.of(cycle);
 		return c;
 	}
 
 	public List<Vertex> mergeTours(Optional<List<Vertex>> tour1,
 			Optional<List<Vertex>> tour2, Vertex junction) {
 		Preconditions.checkArgument(vertices.contains(junction),
 				"Junction not in graph");
 		Preconditions.checkArgument(tour1.get().contains(junction),
 				"Junction not in tour1");
 		Preconditions.checkArgument(tour2.get().contains(junction),
 				"Junction not in tour2");
 		Preconditions.checkArgument(vertices.containsAll(tour1.get())
 				&& vertices.containsAll(tour2.get()),
 				"Tours not a subset of vertices");
 
 		List<Vertex> tour3 = Lists.newArrayList();
 
 		if (!tour1.isPresent() && !tour2.isPresent()) {
 			return tour3;
 		} else if (!tour1.isPresent()) {
 			return tour2.get();
 		} else if (!tour2.isPresent()) {
 			return tour1.get();
 		}
 
 		boolean tour2HasBeenVisited = false;
 		for (Vertex tour1Vertex : tour1.get()) {
 			tour3.add(tour1Vertex);
 			if (!tour2HasBeenVisited && tour1Vertex.equals(junction)) {
 				int indexOfJunctionInTour2 = tour2.get().indexOf(junction);
 				for (int i = indexOfJunctionInTour2 + 1; i < tour2.get().size(); i++) {
 					Vertex tour2Vertex = tour2.get()
 							.get(i % tour2.get().size());
 					tour3.add(tour2Vertex);
 				}
 				tour2HasBeenVisited = true;
 			}
 		}
 		return tour3;
 	}
 }
