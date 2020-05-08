 /**
  * 
  */
 package graphrep;
 
 import com.carrotsearch.hppc.IntArrayList;
 import com.carrotsearch.hppc.LongObjectOpenHashMap;
 import com.carrotsearch.hppc.cursors.LongCursor;
 
 /**
  * @author nino
  * 
  */
 public class HashNN implements NNSearcher {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	GraphRep graphRep;
	LongObjectOpenHashMap<Object> hashMap;
 	NNSearcher dumpNN;
 	private static final int maxHopLimit = 10;
 
 	public HashNN(GraphRep graphRep) {
 		this.graphRep = graphRep;
 		dumpNN = new DumbNN(graphRep);
		hashMap = new LongObjectOpenHashMap<Object>();
 		for (int i = 0; i < graphRep.getNodeCount(); i++) {
 			long tempLat = graphRep.getNodeLat(i) / 1000;
 			long tempLon = graphRep.getNodeLon(i) / 1000;
 
 			long key = (tempLat << 32) | tempLon;
 			IntArrayList tempValues = (IntArrayList) hashMap.get(key);
 			if (tempValues == null) {
 				tempValues = new IntArrayList(10);
 				tempValues.add(i);
 				hashMap.put(key, tempValues);
 			} else {
 				tempValues.add(i);
 			}
 		}
 		for (LongCursor key : hashMap.keys()) {
 			int[] arr = ((IntArrayList) hashMap.get(key.value)).toArray();
 			hashMap.put(key.value, arr);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see graphrep.NNSearcher#getIDForCoordinates(int, int)
 	 */
 	@Override
 	public int getIDForCoordinates(int lat, int lon) {
 		int pos = -1;
 		long key;
 		long keyLat = lat / 1000;
 		long keyLon = lon / 1000;
 		long dist = Long.MAX_VALUE;
 		long tempDist = Long.MAX_VALUE;
 		boolean found = false;
 		boolean finished = false;
 		int hops = 0;
 		for (int i = 0; i <= hops; i++) {
 			// North
 			for (int j = -i; j <= i; j++) {
 				key = ((keyLat + i) << 32) | (keyLon + j);
 				if (hashMap.containsKey(key)) {
 					int[] ringArr = (int[]) hashMap.get(key);
 					for (int nodeID : ringArr) {
 						tempDist = sqDistToCoords(nodeID, lat, lon);
 						if (tempDist < dist) {
 							dist = tempDist;
 							pos = nodeID;
 						}
 
 					}
 					found = true;
 				}
 			}
 			// East
 			for (int j = -i + 1; j <= i - 1; j++) {
 				key = ((keyLat + j) << 32) | (keyLon - i);
 				if (hashMap.containsKey(key)) {
 					int[] ringArr = (int[]) hashMap.get(key);
 					for (int nodeID : ringArr) {
 						tempDist = sqDistToCoords(nodeID, lat, lon);
 						if (tempDist < dist) {
 							dist = tempDist;
 							pos = nodeID;
 						}
 
 					}
 					found = true;
 				}
 			}
 			// West
 			for (int j = -i + 1; j <= i - 1; j++) {
 				key = ((keyLat + j) << 32) | (keyLon + i);
 				if (hashMap.containsKey(key)) {
 					int[] ringArr = (int[]) hashMap.get(key);
 					for (int nodeID : ringArr) {
 						tempDist = sqDistToCoords(nodeID, lat, lon);
 						if (tempDist < dist) {
 							dist = tempDist;
 							pos = nodeID;
 						}
 
 					}
 					found = true;
 				}
 			}
 
 			// South
 			for (int j = -i; j <= i; j++) {
 				key = ((keyLat - i) << 32) | (keyLon + j);
 				if (hashMap.containsKey(key)) {
 					int[] ringArr = (int[]) hashMap.get(key);
 					for (int nodeID : ringArr) {
 						tempDist = sqDistToCoords(nodeID, lat, lon);
 						if (tempDist < dist) {
 							dist = tempDist;
 							pos = nodeID;
 						}
 
 					}
 					found = true;
 				}
 			}
 			/*
 			 * for (int j = -i; j <= i; j++) { for (int k = -i; k <= i; k++) {
 			 * key = keyLat + j << 32 | keyLon + k; if
 			 * (hashMap.containsKey(key)) { int[] ringArr = (int[])
 			 * hashMap.get(key); for (int nodeID : ringArr) { tempDist =
 			 * (graphRep.getNodeLat(nodeID) - lat) (graphRep.getNodeLat(nodeID)
 			 * - lat) + (graphRep.getNodeLon(nodeID) - lon)
 			 * (graphRep.getNodeLon(nodeID) - lon); if (tempDist < dist) { dist
 			 * = tempDist; pos = nodeID; }
 			 * 
 			 * } found = true; } } }
 			 */
 			if (!found && hops <= maxHopLimit) {
 				hops++;
 			} else if (found && !finished) {
 				finished = true;
 				hops++;
 			}
 
 		}
 
 		if (!found) {
 			pos = dumpNN.getIDForCoordinates(lat, lon);
 		}
 		return pos;
 	}
 
 	private final long sqDistToCoords(int nodeID, int lat, int lon) {
 		return ((long) (graphRep.getNodeLat(nodeID) - lat))
 				* ((long) (graphRep.getNodeLat(nodeID) - lat))
 				+ ((long) (graphRep.getNodeLon(nodeID) - lon))
 				* ((long) (graphRep.getNodeLon(nodeID) - lon));
 	}
 }
