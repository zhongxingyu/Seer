 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rlokc
  * Date: 27.10.12
  * Time: 11:53
  * Реализация ненаправленого графа. Граф хранится в матрице смежности, где на пересечениях указан их вес.
  * To change this template use File | Settings | File Templates.
  */
 public class UndirectedGraph {
 
     private ArrayList<ArrayList<Integer>> weightList;
     private Integer nodeAmount;
     private ArrayList<Node> nodes;
 //    private Scanner tempScanner = new Scanner(System.in);
     private Scanner scanner = new Scanner(System.in);
 
     /**
      * Nodes list getter
      *
      * @return List of nodes
      */
     public ArrayList<Node> getNodesList() {
         return nodes;
     }
 
     /**
      * Node amount getter
      *
      * @return Node amount
      */
     public Integer getNodeAmount() {
         return nodeAmount;
     }
 
     /**
      * Node amount setter
      *
      * @param nodeAmount Node amount to set
      */
     public void setNodeAmount(Integer nodeAmount) {
         this.nodeAmount = nodeAmount;
     }
 
     /**
      * Weight list setter
      *
      * @param weightList Weight list to set
      */
     public void setWeightList(ArrayList<ArrayList<Integer>> weightList) {
         this.weightList = weightList;
     }
 
     /**
      * Node generator
      */
 
 
     /**
      * Don't know
      *
      * @return ???
      */
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 // GraphGenerator
 //TODO: Make it possible to read all from some file if desired, punching the numbers is quite annoying
 //TODO: Make exceptions for tempScanner if file does not exist
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     public UndirectedGraph generateGraph() {
 /*        System.out.println("Write the file of matrix, if you want to use stdin, write 0");       //TODO: FIX IT!
         String source = tempScanner.next();
         if (source == "0"){
             scanner = tempScanner;
         } else {
             scanner = new Scanner(source);
         }
 */
         System.out.println("Write ammount of nodes in graph:");
         int nodeAmount = scanner.nextInt();
         UndirectedGraph graph = new UndirectedGraph();
         System.out.println("Write matrix:");
         ArrayList<ArrayList<Integer>> matrix = new ArrayList<ArrayList<Integer>>(nodeAmount);
         for (int i = 0; i < nodeAmount; i++) {
             ArrayList<Integer> tmp = new ArrayList<Integer>(nodeAmount);
             tmp.clear();
             for (int j = 0; j < nodeAmount; j++) {
                 tmp.add(scanner.nextInt());
             }
             matrix.add(tmp);
         }
         graph.setWeightList(matrix);
         graph.setNodeAmount(nodeAmount);
         graph.nodeGenerator();
 //        scanner.close();                                      //Seems to close the whole input, so I'll probably delete it
         return graph;
 
     }
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 // Method for Generating nodes. Part of a GraphGenerator
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         public void nodeGenerator() {
             nodes = new ArrayList<Node>(nodeAmount);
             System.out.println("Write addresses of all networks");
             for (int i = 0; i < nodeAmount; i++) {                  //Making nodeAmount of nodes
                 Node tmpNode = new Node(scanner.next());
                 nodes.add(tmpNode);
             }
             for (int i = 0; i < nodeAmount; i++) {                  //Looking through rows (the nodes)
                 Node tmpNode1 = nodes.get(i);
                 for (int j = 0; j < nodeAmount; j++) {              //Looking through links with tmpNodes
                     Node comparableNode = nodes.get(j);
                     if (tmpNode1 != comparableNode) {                 //Checking if not comparing to itself, we don't need this
                         int tmpWeigth = weightList.get(i).get(j);
                         if (tmpWeigth != 0) {
                             Edge tmpEdge = new Edge(tmpNode1, comparableNode, tmpWeigth);    //Adding edges to both nodes
                             tmpNode1.addAdjacency(tmpEdge);
                         }
                     }
                 }
             }
         }
 
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //Fuck Stas's searcher, let's do our own!
 //Dijkstra's Algorithm realisation
 //TODO: Currently it doesn't work with "segmented" graphs (some nodes are separated), but who cares really :3
 //TODO: Good idea to check if it actually works, but i don't have any graphs here right now
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     void dijkstraSearch(Node startNode, Node targetNode) {
         startNode.setMark(0);
         startNode.isPassed = true;
         Node currentNode = startNode;
         Node nextNode = null;                            //Next node we are going to visit
         int nextNodeMark = Integer.MAX_VALUE;
         boolean isFinished = false;
         boolean isReachedTarget = false;
 
         while (!isFinished) {
             for (Edge tmpEdge : currentNode.getAdjacencies()) {  //Looking through each connection in the node
                 Node endNode = tmpEdge.getEndNode();             //Ending node of current edge
                 if (!endNode.isPassed) {                         //If this node wasn't passed early
                     int comparableMark = currentNode.getMark()+tmpEdge.getWeight();      //currentNode mark + edge weigth
                     if (endNode == targetNode) {
                         isReachedTarget = true;
                     }
                     if (comparableMark<endNode.getMark()){                               //Comparing endNode mark to comparableMark
                         endNode.setMark(comparableMark);                                 //Replacing if new mark is less
                     }
                     if (comparableMark < nextNodeMark) {
                         nextNode = endNode;
                     }
                 }
             }
             currentNode.isPassed = true;                       //Our node is now "red", we don't need to visit it
             currentNode = nextNode;
             isFinished = true;                                 //Checking if all nodes are out
             for (Node checkNote : this.getNodesList()) {
                 if (!checkNote.isPassed) {
                     isFinished = false;
                     if (isReachedTarget) {
                        currentNode = checkNote; //If we reached the "end" of graph, do the search for all the others unvisited nodes
                     }
                     break;
                 }
             }
         }
 //Debug
         System.out.println("YOUR BUNNY WROTE: ");
         System.out.print(targetNode.getMark());
     }
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //Master for running desired algorithms
 //TODO:Make it array-based, would be more extendable this way
 //TODO:Use the bloody nodenames you have! (Though it'll make it all a lil bit slower)
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     public void algorithmMaster(){
         System.out.println("Write a number of a desired algorithm:");
         System.out.println("0 for Dijkstra's");
         int choice = scanner.nextInt();
         switch(choice){
             case 0:{                                                               //Dijkstra
                 System.out.println("Write indexes of starting and target nodes:");
                 Node startNode = this.getNodesList().get(scanner.nextInt());
                 Node endNode = this.getNodesList().get(scanner.nextInt());
                 this.dijkstraSearch(startNode,endNode);
                 break;
             }
             default:break;
         }
     }
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //SCANNER CLOSING
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     public void closeScanner(){
         scanner.close();
     }
 }
 
 
 
