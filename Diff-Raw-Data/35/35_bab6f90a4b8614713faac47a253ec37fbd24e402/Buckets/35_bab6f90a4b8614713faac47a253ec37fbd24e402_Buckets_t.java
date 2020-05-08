 package kademlia;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import kademlia.messages.PingResponse;
 import kademlia.messages.Response;
 
 
 /**
  * 
  * Object that holds the k-buckets routing table for the known nodes in Kademlia network
  *
  */
 public class Buckets implements NodeStatusListener {
 	protected List<LinkedHashMap<Identifier, Node>> buckets;
 	final protected NetworkInstance networkInstance;
 	final int k;
 	
 	
 	/**
 	 * Constructor
 	 * @param instance
 	 */
 	public Buckets(NetworkInstance instance) {
 		networkInstance = instance;
 		k = networkInstance.getConfiguration().getK();
 		int b = networkInstance.getConfiguration().getB();
 		buckets = new ArrayList<LinkedHashMap<Identifier, Node>>(b);
 		for (int i = 0; i < b; i++) {
 			buckets.add(i, new LinkedHashMap<Identifier, Node>());
 		}
 	}
 	
 	/**
 	 * Should be called when a node is detected to be offline
 	 */
 	public void onNodeDown(Node node) {
 		buckets.get(calculateBucketNumber(node)).remove(node);
 	}
 	
 	/**
 	 * Called when a node is found online
 	 */
 	public void onNodeAlive(Node node) {
 		add(node); 
 	}
 	
 	
 	/**
 	 * Find the closest nodes in buckets routing table to the given node
 	 * @param id ID of the target node
 	 * @param numberOfNodes number of results to return
 	 * @return List<Node>
 	 */
 	public List<Node> getNearestNodes(Identifier id, int numberOfNodes) {
 		int bucketNumber = calculateBucketNumber(id);
 		
 		SortedSet<Node> nearSet = new TreeSet<Node>(new IdentifiableDistanceComparator(id));
 		
 		nearSet.addAll(buckets.get(bucketNumber).values());
 		
 		int left = bucketNumber - 1;
 		int right = bucketNumber + 1;
 		while (nearSet.size() < numberOfNodes) {
 			if (left >= 0) {
 				nearSet.addAll(buckets.get(left--).values());
 			}
 			if (right < buckets.size() && nearSet.size() < numberOfNodes) {
 				nearSet.addAll(buckets.get(right++).values());
 			}
 		}
 		
 		List<Node> nearest = new ArrayList<Node>(nearSet);
 		return nearest.subList(0, nearest.size() > numberOfNodes ? numberOfNodes : nearest.size());
 	}
 	
 	/**
 	 * Return the number of nodes in a given bucket
 	 * @param bucket
 	 * @return
 	 */
 	public int getBucketNodeCount(int bucket) {
 		return buckets.get(bucket).size();
 	}
 	
 	/**
 	 * To calculate the bucket number a Node or other identifiable object will fall in
 	 * @param obj
 	 * @return
 	 */
 	public int calculateBucketNumber(Identifiable obj) {
 		System.out.println(networkInstance.getLocalNodeIdentifier().getIntegerValue());
 		System.out.println(obj.getIdentifier().getIntegerValue());
 		System.out.println("DISTANCE: " + Math.floor(Math.log(Identifier.calculateDistance(
 				networkInstance.getLocalNodeIdentifier(), obj).doubleValue())/Math.log(2)));
		
		if (obj.equals(networkInstance.getLocalNodeIdentifier())) {
			return 0;
		}
		
 		return (int) Math.floor(Math.log(Identifier.calculateDistance(
 				networkInstance.getLocalNodeIdentifier(), obj).doubleValue())/Math.log(2));
 	}
 	
 	/**
 	 * to add a collection of nodes to the buckets routing table
 	 * @param nodes Collection of Nodes
 	 */
 	public void addAll(Collection<Node> nodes) {
 		for (Node node : nodes) {
 			add(node);
 		}		
 	}
 
 	/**
 	 * To add a single Node to the buckets routing table
 	 * @param newNode
 	 */
 	public void add(final Node newNode) {
		if (newNode.getIdentifier().equals(networkInstance.getLocalNodeIdentifier())) {
			return;
		}
		
 		int bucketNumber = calculateBucketNumber(newNode.getIdentifier());
 		final LinkedHashMap<Identifier, Node> currentBucket = buckets.get(bucketNumber);
 		if(currentBucket.containsKey(newNode.getIdentifier())){
 			// Move existing node to the front
 			currentBucket.remove(newNode.getIdentifier());
 			currentBucket.put(newNode.getIdentifier(), newNode);
 		}
 		else {
 			if (currentBucket.size() < k) {
 				// Add new node
 				currentBucket.put(newNode.getIdentifier(), newNode);
 			} else {
 				// gets the first element from the LinkedHashMap
 				final Node firstNode = currentBucket.entrySet().iterator().next().getValue();
 				networkInstance.ping(firstNode, new ResponseListener<PingResponse>(){
 					@Override
 					public void onFailure() {
 						currentBucket.remove(firstNode.getIdentifier());
 						currentBucket.put(newNode.getIdentifier(), newNode);
 					}
 					@Override
 					public void onResponseReceived(PingResponse response) {
 						currentBucket.remove(firstNode);
 						currentBucket.put(firstNode.getIdentifier(), firstNode);
 					}					
 				});
 			}
 		}
 	}
 	
 	/**
 	 * To find a Node by its Identifier
 	 * @param identifier
 	 * @return Node
 	 */
 	public Node findNodeByIdentifier(Identifier identifier) {
 		int bucketNumber = calculateBucketNumber(identifier);
 		return buckets.get(bucketNumber).get(identifier);
 	}
 }
