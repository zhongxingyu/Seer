 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.strategy;
 
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.ClusterDendrogramNode;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.DendrogramNode;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.LeafDendrogramNode;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.node.Node;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 /**
  *
  * @author Jeremy
  */
 public class SingleLinkClusteringStrategy implements ClusteringStrategy {
 
     public DendrogramNode cluster(Set<Node> collapsibleGraphNodes) {
         
         //Map to hold the current set of Node clusters in a top-level DendrogramNode. This is an optimization as each DendrogramNode has a function
         //to retrieve the set of child nodes.
         Map<DendrogramNode, Set<Node>> topLevelClusterMap = new HashMap<DendrogramNode, Set<Node>>();
         //Map of distances to the pair of DendrogramNodes they separate
         TreeMap<Double, Set<DendrogramNode>> distanceMap = new TreeMap<Double, Set<DendrogramNode>>();
         
         Set<DendrogramNode> initialDNodeSet = new HashSet<DendrogramNode>();
         //This loop turns the set of Nodes from the input into a set of singleton DendrogramNodes
         for (Node collapsibleGraphNode : collapsibleGraphNodes) {
             //Create the DendrogramLeafNode to hold this Node
             DendrogramNode newDNodeSet = new LeafDendrogramNode(collapsibleGraphNode);
            
             //As these new Dendrograms are top-level, they must have nodes for topLevelClusters
             Set<Node> singletonClusterSet = new HashSet<Node>();
             singletonClusterSet.add(collapsibleGraphNode);
             topLevelClusterMap.put(newDNodeSet, singletonClusterSet);
             
             //A loop to calculate the distance between this DendrogramNode and each singleton DendrogramNode that we've already added
             //In so doing this will make sure we have each of the pairings necessary
             for(DendrogramNode dNode : initialDNodeSet) {
                 Set<DendrogramNode> clusterPair = new HashSet<DendrogramNode>();
                 clusterPair.add(dNode);
                 clusterPair.add(newDNodeSet);
                 distanceMap.put(findDistance(newDNodeSet, dNode, topLevelClusterMap), clusterPair);
             }
            //We do this last to avoid self-comparisons.
            initialDNodeSet.add(newDNodeSet);
         }
         
         //A loop that runs until the we have only a single pairing left.
         while(distanceMap.keySet().size()>1) {
            //If we're going to iterate over the keys in a map, we can't be altering the map concurrently.
            //Instead, let's record the alterations and do them safely at the end of the iteration over the map's keys.
            Set<Double> mapEntriesToRemove = new HashSet<Double>();
            Map<Double, Set<DendrogramNode>> mapEntriesToAdd = new HashMap<Double, Set<DendrogramNode>>();
            
            //Retrieve the lowest-distance pair 
            Map.Entry<Double, Set<DendrogramNode>> lowestDistancePairEntry = distanceMap.firstEntry();
            //Store the lowest distance Double and the pair of DendrogramNodes that they represent
            Double formerClosestPairDistance = lowestDistancePairEntry.getKey();
            Set<DendrogramNode> formerClosestPairSet = lowestDistancePairEntry.getValue();
            //Create a new DendrogramNodeset to join the pair
 
            DendrogramNode newClusterDNode = new ClusterDendrogramNode(formerClosestPairSet, formerClosestPairDistance);
            
            //Update the topLevelClusters map to lose the old top level clusters and add the new one
            Set<Node> newTopLevelCluster = new HashSet<Node>();
            for(DendrogramNode newlyClusteredNode : formerClosestPairSet) {
                newTopLevelCluster.addAll(topLevelClusterMap.remove(newlyClusteredNode));
            }
            topLevelClusterMap.put(newClusterDNode, newTopLevelCluster);    
            
            //List this old pairing for removal from the distance map
            distanceMap.remove(formerClosestPairDistance); 
            
            //A loop to recalculate each distance pair that involved either of the joined pair
            for(Double preClusterDistance : distanceMap.keySet()) {
                Set<DendrogramNode> preClusterPair = distanceMap.get(preClusterDistance);
                for(DendrogramNode newlyClusteredDNode : preClusterPair) {
                    if(formerClosestPairSet.contains(newlyClusteredDNode)) {
                            formerClosestPairSet.remove(newlyClusteredDNode);
                            if(formerClosestPairSet.size()!=1) {
                                throw new RuntimeException("There's no good reason for a pair to have had anything but 2 nodes!");
                            }
                    //Sketchy way to retrieve the other node from this set
                    DendrogramNode onlyDNode = (DendrogramNode)(formerClosestPairSet.toArray())[0];
                    Double postClusterDistance = findDistance(onlyDNode, newlyClusteredDNode, topLevelClusterMap);
                    
                    //Replace the former node with the newly formed Cluster node
                    formerClosestPairSet.add(newClusterDNode);
                    mapEntriesToAdd.put(postClusterDistance, formerClosestPairSet);
                    }
                }
            }
            //Actually do the work of adding and removing mappings from system
            for(Double distanceToBeRemoved : mapEntriesToRemove) {
                distanceMap.remove(distanceToBeRemoved);
            }
            distanceMap.putAll(mapEntriesToAdd);
         }
         
         Map.Entry<Double, Set<DendrogramNode>> lastEntry = distanceMap.lastEntry();
         DendrogramNode rootOfDendrogram = new ClusterDendrogramNode(lastEntry.getValue(), lastEntry.getKey());
         return rootOfDendrogram;
     }
     
     private Double findDistance(DendrogramNode firstDendrogramNode,DendrogramNode secondDendrogramNode,
          Map<DendrogramNode, Set<Node>> currentClusters) {
         
         double minimumDistance = Double.MAX_VALUE;
         for(Node currentFirstDendrogramNode: currentClusters.get(firstDendrogramNode)) {
             for(Node currentSecondDendrogramNode : currentClusters.get(secondDendrogramNode) ) {
                 double thisDistance = currentFirstDendrogramNode.getDistance(currentSecondDendrogramNode);
                 if( thisDistance < minimumDistance) {
                     minimumDistance = thisDistance;
                 }
             }
         }
         return new Double(minimumDistance);
     }
 }
