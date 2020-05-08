 /**
  * $$\\ToureNPlaner\\$$
  */
 package algorithms;
 
 import graphrep.GraphRep;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.carrotsearch.hppc.BitSet;
 import com.carrotsearch.hppc.IntArrayDeque;
 import computecore.ComputeRequest;
 
 public class ShortestPathCH extends GraphAlgorithm {
 
 	private final Heap heap;
 
 	// Used to mark nodes with BFS
 	private final BitSet marked;
 	private final BitSet visited;
 
 	// TODO: Replace with int array based fifo
 	private final IntArrayDeque deque;
 
 	public ShortestPathCH(GraphRep graph) {
 		super(graph);
 		heap = new algorithms.Heap();
 		dists = new int[graph.getNodeCount()];
 		prevEdges = new int[graph.getNodeCount()];
 		marked = new BitSet(graph.getEdgeCount());
 		visited = new BitSet(graph.getNodeCount());
 		deque = new IntArrayDeque();
 	}
 
 	private void reset() {
 		// reset dists
 		visited.clear();
 		marked.clear();
 
 		for (int i = 0; i < graph.getNodeCount(); i++) {
 			dists[i] = Integer.MAX_VALUE;
 		}
 		heap.resetHeap();
 	}
 
 	// dists in this array are stored with the multiplier applied. They also are
 	// rounded and are stored as integers
 	private final int[] dists;
 
 	/**
 	 * edge id
 	 */
 	private final int[] prevEdges;
 
 	// maybe use http://code.google.com/p/simplelatlng/ instead
 	private final double calcDirectDistance(double lat1, double lng1,
 			double lat2, double lng2) {
 		double earthRadius = 6370.97327862;
 		double dLat = Math.toRadians(lat2 - lat1);
 		double dLng = Math.toRadians(lng2 - lng1);
 		double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2))
 				+ (Math.cos(Math.toRadians(lat1))
 						* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math
 							.sin(dLng / 2));
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double dist = earthRadius * c;
 		return dist * 1000;
 	}
 
 	@Override
 	public void compute(ComputeRequest req) throws ComputeException {
 		assert (req != null) : "We ended up without a request object in run";
 
 		// TODO: send error messages to client
 		Points points = req.getPoints();
 		// Check if we have enough points to do something useful
 		if (points.size() < 2) {
 			throw new ComputeException("Not enough points, need at least 2");
 		}
 
 		Points resultPoints = req.getResultPoints();
 		int distance = 0;
 		distance = shortestPath(points, resultPoints);
 
 		Map<String, Object> misc = new HashMap<String, Object>(1);
 		misc.put("distance", distance);
 		req.setMisc(misc);
 	}
 
 	/**
 	 * Computes the shortest path over points[0] -> points[1] -> points[2]...
 	 * and stores all points on the path in resultPoints
 	 * 
 	 * @param points
 	 * @param resultPoints
 	 * @return
 	 * @throws Exception
 	 */
 	public int shortestPath(Points points, Points resultPoints)
 			throws ComputeException {
 		int srclat;
 		int srclon;
 		int destlat;
 		int destlon;
 
 		int srcid = 0;
 		int destid = 0;
 		int distance = 0;
 		// in meters
 		double directDistance;
 		for (int pointIndex = 0; pointIndex < (points.size() - 1); pointIndex++) {
 
 			// New Dijkstra need to reset
 
 			srclat = points.getPointLat(pointIndex);
 			srclon = points.getPointLon(pointIndex);
 			destlat = points.getPointLat(pointIndex + 1);
 			destlon = points.getPointLon(pointIndex + 1);
 			reset();
 			long nntime = System.nanoTime();
 			srcid = graph.getIDForCoordinates(srclat, srclon);
 			destid = graph.getIDForCoordinates(destlat, destlon);
 			System.out.println("NNSearch took "
					+ ((System.nanoTime() - nntime) / 1000000.0) + " ms");
 			directDistance = calcDirectDistance(((double) srclat) / 10000000.0,
 					((double) srclon) / 10000000,
 					((double) destlat) / 10000000,
 					((double) destlon) / 10000000);
 
 			int nodeID = destid;
 			int sourceNode;
 			int edgeId;
 			/*
 			 * Start BFS on G_down beginning at destination and marking edges
 			 * for consideration
 			 * 
 			 * TODO: Create proper infrastructure for getting arrays with
 			 * |nodes| length
 			 */
 
 			System.out.println("Start BFS");
 			long starttime = System.nanoTime();
 			deque.clear();
 			deque.addLast(destid);
 			// visited[destid] = true;
 			visited.set(destid);
 			while (!deque.isEmpty()) {
 				nodeID = deque.removeLast();
 
 				for (int i = 0; i < graph.getInEdgeCount(nodeID); i++) {
 					edgeId = graph.getInEdgeID(nodeID, i);
 					sourceNode = graph.getSource(edgeId);
 
 					if (!visited.get(sourceNode)
 							&& (graph.getRank(sourceNode) >= graph
 									.getRank(nodeID))) {
 						// Mark the edge
 						marked.set(edgeId);
 						visited.set(sourceNode);
 						// Add source for exploration
 						deque.addFirst(sourceNode);
 					}
 
 				}
 			}
 
 			dists[srcid] = 0;
 			heap.insert(srcid, dists[srcid]);
 
 			int nodeDist;
 
 			int tempDist;
 			int targetNode;
 			long dijkstratime;
 			long backtracktime;
 
 			DIJKSTRA: while (!heap.isEmpty()) {
 				nodeID = heap.peekMinId();
 				nodeDist = heap.peekMinDist();
 				heap.removeMin();
 				if (nodeID == destid) {
 					break DIJKSTRA;
 				} else if (nodeDist > dists[nodeID]) {
 					continue;
 				}
 				for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
 					edgeId = graph.getOutEdgeID(nodeID, i);
 					targetNode = graph.getTarget(edgeId);
 					// Either marked (by BFS) or G_up edge
 					if (marked.get(edgeId)
 							|| (graph.getRank(nodeID) <= graph
 									.getRank(targetNode))) {
 						// without multiplier = shortest path
 						// tempDist = dist[nodeID] + graph.getOutDist(nodeID,
 						// i);
 
 						// with multiplier = fastest path
 						tempDist = dists[nodeID] + graph.getDist(edgeId);
 
 						if (tempDist < dists[targetNode]) {
 							dists[targetNode] = tempDist;
 							prevEdges[targetNode] = graph.getOutEdgeID(nodeID,
 									i);
 							heap.insert(targetNode, tempDist);
 						}
 
 					}
 				}
 			}
 			dijkstratime = System.nanoTime();
 
 			if (nodeID != destid) {
 				// TODO: send errmsg to client and do something useful
 				System.err.println("There is no path from src to dest");
 				throw new ComputeException("No Path found");
 			}
 
 			// backtracking and shortcut unpacking use dequeue as stack
 			deque.clear();
 			// Add the edges to our unpacking stack we
 			// go from destination to start so add at the end of the deque
 			int currNode = destid;
 			int outofSource, outofShortcuted, shortcutted, source;
 
			do {
 				edgeId = prevEdges[currNode];
 				deque.addFirst(edgeId);
 				currNode = graph.getSource(edgeId);
			} while (currNode != srcid);
 
 			// System.out.println("Start unpacking " + deque.size() + " edges");
 			// Unpack shortcuts "recursively"
 			while (!deque.isEmpty()) {
 				// Get the top edge and check if it's a shortcut that needs
 				// further
 				// unpacking
 				edgeId = deque.removeFirst();
 				shortcutted = graph.getShortedId(edgeId);
 				if (shortcutted != -1) {
 
 					// We have a shortcut unpack it
 					source = graph.getSource(edgeId);
 					outofShortcuted = graph.getOutEdgeID(shortcutted,
 							graph.getEdgeShortedNum(edgeId));
 					outofSource = graph.getOutEdgeID(source,
 							graph.getEdgeSourceNum(edgeId));
 					deque.addFirst(outofShortcuted);
 					deque.addFirst(outofSource);
 				} else {
 					// No shortcut remember it
 					distance += graph.getDist(edgeId);
 					currNode = graph.getSource(edgeId);
 					resultPoints.addPoint(graph.getNodeLat(currNode),
 							graph.getNodeLon(currNode));
 
 				}
 			}
 
 			backtracktime = System.nanoTime();
 			System.out.println("found sp with dist = "
 					+ ((double) distance / 1000.0) + " km (direct distance: "
 					+ (directDistance / 1000.0)
 					+ " km; Distance with multiplier: "
 					+ (dists[destid] / 1000.0) + ")");
 			System.out.println("Dijkstra: "
 					+ ((dijkstratime - starttime) / 1000000.0)
 					+ " ms; Backtracking: "
 					+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");
 		}
 
 		Map<String, Integer> misc = new HashMap<String, Integer>(2);
 		misc.put("distance", distance);
 		// Don't forget to add destination
 		resultPoints.addPoint(graph.getNodeLat(destid),
 				graph.getNodeLon(destid));
 		return distance;
 	}
 }
