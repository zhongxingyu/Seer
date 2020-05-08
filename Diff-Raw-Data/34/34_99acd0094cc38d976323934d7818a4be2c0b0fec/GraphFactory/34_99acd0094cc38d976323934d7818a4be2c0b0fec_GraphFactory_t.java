 package edu.kpi.pzks.core.factory;
 
 import edu.kpi.pzks.core.model.Graph;
 import edu.kpi.pzks.core.model.Link;
 import edu.kpi.pzks.core.model.Links;
 import edu.kpi.pzks.core.model.Node;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  *
  * @author Aloren
  */
 public class GraphFactory {
 
     private static int nodesWeightSum;
 
     public static Graph newGraph(int numberOfNodes, int minNodeWeight, int maxNodeWeight, double connectivity) {
         if (connectivity > 1 || connectivity < 0) {
             throw new IllegalArgumentException("Connectivity = " + connectivity + ". connectivity <=1 and >=0");
         }
         //TODO check other params
 
         Graph graph = new Graph(true);
         Random generator = new Random();
         nodesWeightSum = 0;
         List<Node> nodes = generateNodes(numberOfNodes, maxNodeWeight, minNodeWeight);
         double linksWeightSum = (double) (nodesWeightSum / connectivity) - nodesWeightSum;
         double remainder = linksWeightSum;
         Node[] nodesArr = nodes.toArray(new Node[nodes.size()]);
         Links links = new Links();
        final double shouldCreateLink = 0.3;
        final double shouldIncWeightLink = 0.5;
         final double remainderDelta = 0.5;
         int sum = 0;
         System.out.println("LinksWeightSum: " + linksWeightSum);
         loop:
         while (remainder >= remainderDelta) {
             for (int i = 0; i < nodesArr.length; i++) {
                 for (int j = i + 1; j < nodesArr.length; j++) {
                     if (remainder <= remainderDelta) {
                         break loop;
                     }
                     if (generator.nextDouble() <= shouldCreateLink) {
                         final Node fromNode = nodesArr[i];
                         final Node toNode = nodesArr[j];
                        final Link linkBetween = links.getLinkBetween(fromNode, toNode);
                        if (linkBetween == null) {
                             int generatedWeight = generateWeight(1, (int) Math.ceil(remainder));
                             remainder -= generatedWeight;
                             Link link = new Link(fromNode, toNode, generatedWeight);
                             links.add(link);
                             sum += generatedWeight;
                             System.out.println("Gen link: " + link.getWeight());
                        } else {
                            if (generator.nextDouble() <= shouldIncWeightLink) {
                                int generatedWeight = generateWeight(1, (int) Math.ceil(remainder));
                                remainder -= generatedWeight;
                                final int oldWeight = linkBetween.getWeight();
                                linkBetween.setWeight(oldWeight + generatedWeight);
                                sum += generatedWeight;
                                System.out.println("Inc weight: " + oldWeight+" new weight: "+linkBetween.getWeight());
                            }
                         }
                     }
                 }
             }
         }
         System.out.println("Sum: " + sum);
         graph.addNodes(nodes);
         graph.addLinks(links);
         return graph;
     }
 
     protected static List<Node> generateNodes(int numberOfNodes, int maxNodeWeight, int minNodeWeight) {
         List<Node> nodes = new ArrayList<>();
         for (int i = 0; i < numberOfNodes; i++) {
             int weight = generateWeight(minNodeWeight, maxNodeWeight);
             nodesWeightSum += weight;
             Node node = new Node(weight);
             node.setName("N" + i);
             nodes.add(node);
         }
         return nodes;
     }
 
     private static int generateWeight(int minNodeWeight, int maxNodeWeight) {
         if (minNodeWeight > maxNodeWeight) {
             throw new IllegalArgumentException("Cannot generate random in current limits: [" + minNodeWeight + "; " + maxNodeWeight + "]");
         }
         if (minNodeWeight == maxNodeWeight) {
             return minNodeWeight;
         }
         int weight = minNodeWeight + (int) (Math.random() * ((maxNodeWeight - minNodeWeight) + 1));
         return weight;
     }
 }
