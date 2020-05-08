 package org.operationsproject.operations.layer2;
 
 import org.operationsproject.operations.graph.CycleException;
 import org.operationsproject.operations.graph.Graph;
 import org.operationsproject.operations.graph.Node;
 import org.operationsproject.operations.graph.UnknownNodeException;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Class representing an Operations application.
  * @author Matthew Johnstone
  *         Date: 01/06/13
  *         Time: 23:00
  */
 public class Application {
 
     private Set<Function> functions;
     private Graph graph;
 
     private Map<Function, Node> functionsToNodes;
     private Map<Node, Function> nodesToFunctions;
 
     public Application() {
         functions = new HashSet<>();
         graph = new Graph();
 
         functionsToNodes = new HashMap<>();
         nodesToFunctions = new HashMap<>();
     }
 
     public void addFunction(Function function) {
         if (!functions.contains(function)) {
             functions.add(function);
             Node node = new Node();
 
             functionsToNodes.put(function, node);
             nodesToFunctions.put(node, function);
 
             graph.addNode(node);
 
         }
 
     }
 
     public String evaluate(Function function) {
         // Get the node for the function
 
         // Traverse the graph, finding the root data source
         Node node = functionsToNodes.get(function);
 
         Set<Node> linkedNodes;
 
         Node finalNode = node;
 
         while (!(linkedNodes = graph.getLinkedNodes(Graph.EdgeDirection.LINKS_TO, finalNode)).isEmpty()) {
             assert linkedNodes.size() == 1;
             finalNode = linkedNodes.iterator().next();
         }
 
         assert finalNode != null;
 
         Node returnNode = finalNode;
 
         String currentInput = nodesToFunctions.get(finalNode).apply();
 
         while (true) {
 
             Function currentFunction = nodesToFunctions.get(returnNode);
 
             currentFunction.setInput(currentInput);
             currentInput = currentFunction.apply();
 
             Set<Node> linkedNodes1 = graph.getLinkedNodes(Graph.EdgeDirection.LINKED_TO_BY, returnNode);
 
            returnNode = linkedNodes1.iterator().next();
 
             if (returnNode == node) {
                 break;
             }
 
         }
 
         return currentInput;
 
     }
 
     public void addDataDependency(Function sourceFunction, Function destinationFunction) {
         Node node1 = functionsToNodes.get(sourceFunction);
         Node node2 = functionsToNodes.get(destinationFunction);
 
         assert node1 != null && node2 != null;
 
         try {
             graph.linkNodes(node1, node2);
         } catch (CycleException | UnknownNodeException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             throw new RuntimeException(e);
         }
 
         assert node2 == graph.getLinkedNodes(Graph.EdgeDirection.LINKED_TO_BY, node1).iterator().next();
     }
 }
