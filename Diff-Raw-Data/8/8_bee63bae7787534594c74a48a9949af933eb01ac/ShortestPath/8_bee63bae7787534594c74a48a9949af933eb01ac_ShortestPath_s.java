 package algorithms;
 
 import graphrep.GraphRep;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import computecore.ComputeRequest;
 
 public class ShortestPath extends GraphAlgorithm {
 
 	private final Heap heap;
 
 	public ShortestPath(GraphRep graph) {
 		super(graph);
 		heap = new algorithms.Heap(10000);
 		multipliedDist = new int[graph.getNodeCount()];
 		prevEdges = new int[graph.getNodeCount()];
 	}
 
 	// dists in this array are stored with the multiplier applied. They also are
 	// rounded and are stored as integers
 	private final int[] multipliedDist;
 
 	/**
 	 * edge id
 	 */
 	private final int[] prevEdges;
 
 	// in meters
 	private double directDistance;
 
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
 
 		// reset dists
 		for (int i = 0; i < multipliedDist.length; i++) {
 			multipliedDist[i] = Integer.MAX_VALUE;
 		}
 		heap.resetHeap();
 		Points points = req.getPoints();
 
 		// Check if we have enough points to do something useful
 		if (points.size() < 2) {
 			throw new ComputeException("Not enough points, need at least 2");
 		}
 
 		Points resultPoints = req.getResultPoints();
 		int distance = 0;
 		int srclat, srclon;
 		int destlat, destlon;
 		int srcid, destid;
 
 		for (int pointIndex = 0; pointIndex < points.size() - 1; pointIndex++) {
 			srclat = points.getPointLat(pointIndex);
 			srclon = points.getPointLon(pointIndex);
 			destlat = points.getPointLat(pointIndex + 1);
 			destlon = points.getPointLon(pointIndex + 1);
 
 			srcid = graph.getIDForCoordinates(srclat, srclon);
 			destid = graph.getIDForCoordinates(destlat, destlon);
 			directDistance = calcDirectDistance(srclat, srclon, destlat,
 					destlon);
 			multipliedDist[srcid] = 0;
 			heap.insert(srcid, multipliedDist[srcid]);
 
 			int nodeID = -1;
 			int nodeDist;
 			int targetNode = 0;
 			int tempDist;
 
 			long starttime = System.nanoTime();
 			long dijkstratime;
 			long backtracktime;
 
 			DIJKSTRA: while (!heap.isEmpty()) {
 				nodeID = heap.peekMinId();
 				nodeDist = heap.peekMinDist();
 				heap.removeMin();
 				if (nodeID == destid) {
 					break DIJKSTRA;
 				} else if (nodeDist > multipliedDist[nodeID]) {
 					continue;
 				}
 				for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
 					// Ignore Shortcuts
 					if (graph.getOutShortedId(nodeID, i) != -1) {
 						continue;
 					}
 					targetNode = graph.getOutTarget(nodeID, i);
 
 					// without multiplier = shortest path
 					tempDist = multipliedDist[nodeID]
 							+ graph.getOutDist(nodeID, i);
 
 					// with multiplier = fastest path
 					// tempDist = multipliedDist[nodeID]
 					// + graph.getOutMultipliedDist(nodeID, i);
 
 					if (tempDist < multipliedDist[targetNode]) {
 						multipliedDist[targetNode] = tempDist;
 						prevEdges[targetNode] = graph.getOutEdgeID(nodeID, i);
 						heap.insert(targetNode, multipliedDist[targetNode]);
 					}
 				}
 			}
 			dijkstratime = System.nanoTime();
 
 			if (nodeID != destid) {
 				// TODO: send errmsg to client and do something useful
 				System.err.println("There is no path from src to dest");
 				throw new ComputeException("No path found");
 			}
 
 			// Find out how much space to allocate
 			int currNode = nodeID;
 			int routeElements = 0;
 
 			do {
 				routeElements++;
 				currNode = graph.getSource(prevEdges[currNode]);
 			} while (currNode != srcid);
 
 			System.out.println("path goes over " + routeElements + " nodes");
 			// Add them without values we set the values in the next step
 			resultPoints.addEmptyPoints(routeElements);
 
 			// backtracking here
 			// Don't read distance from multipliedDist[], because there are
 			// distances with
 			// regard to the multiplier
 			currNode = nodeID;
 			do {
 				distance += graph.getDist(prevEdges[currNode]);
 				routeElements--;
 
				points.setPointLat(routeElements, graph.getNodeLat(currNode));
				points.setPointLon(routeElements, graph.getNodeLon(currNode));
 
 				currNode = graph.getSource(prevEdges[currNode]);
 			} while (routeElements > 0);
 
 			backtracktime = System.nanoTime();
 
 			System.out.println("found sp with dist = " + (distance / 1000.0)
 					+ " km (direct distance: " + (directDistance / 1000.0)
 					+ " km; Distance with multiplier: "
 					+ (multipliedDist[destid] / 1000.0) + ")");
 			System.out.println("Dijkstra: "
 					+ ((dijkstratime - starttime) / 1000000.0)
 					+ " ms; Backtracking: "
 					+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");
 		}
 
 		Map<String, Object> misc = new HashMap<String, Object>(1);
 		misc.put("distance", distance);
 		req.setMisc(misc);
 	}
}
