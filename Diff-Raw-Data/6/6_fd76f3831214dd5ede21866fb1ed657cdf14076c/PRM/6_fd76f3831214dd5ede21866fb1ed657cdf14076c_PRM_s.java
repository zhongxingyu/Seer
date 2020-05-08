 package ex3;
 
 import ex3.search.SearchAlgorithm;
 import ex4.Printer;
 import geometry_msgs.Point;
 import geometry_msgs.Pose;
 import geometry_msgs.PoseArray;
 import geometry_msgs.PoseStamped;
 import geometry_msgs.PoseWithCovarianceStamped;
 import java.util.ArrayList;
 import java.util.Random;
 import launcher.RunParams;
 import nav_msgs.OccupancyGrid;
 import org.ros.message.MessageFactory;
 import org.ros.message.MessageListener;
 import org.ros.namespace.GraphName;
 import org.ros.node.AbstractNodeMain;
 import org.ros.node.ConnectedNode;
 import org.ros.node.Node;
 import org.ros.node.topic.Publisher;
 import org.ros.node.topic.Subscriber;
 import visualization_msgs.Marker;
 import visualization_msgs.MarkerArray;
 
 public class PRM extends AbstractNodeMain {
 
     public int MAX_REGENERATION_ATTEMPTS = RunParams.getInt("MAX_REGENERATION_ATTEMPTS");//200;
     
     PRMUtil util;
     PRMGraph graph;
     SearchAlgorithm search;
     boolean mapReceived = false;
     boolean graphGenerationComplete = false;
     boolean routeSearchDone = false;
     private int regenerationAttempts = 0;
     public static ConnectedNode node;
     OccupancyGrid originalMap;
     OccupancyGrid inflatedMap;
     private boolean experimentMode = false;
     long seed = System.currentTimeMillis();
 
     ArrayList<Vertex> route;
     ArrayList<Vertex> flatRoute;
     Pose currentPosition;
     Pose goalPosition;
 
     public static final int NO_PATH = 0;
     public static final int PATH_FOUND = 1;
     public static final int GOAL_REACHED = 2;
     
     Subscriber<OccupancyGrid> grid;
     Publisher<MarkerArray> PRMMarkers;
     Publisher<MarkerArray> pathMarkers;
     Publisher<PoseArray> routePub;
     Publisher<OccupancyGrid> inflatedMapPublisher;
     Publisher<std_msgs.Int32> prmInfo;
     Subscriber<PoseStamped> goals;
     Subscriber<PoseWithCovarianceStamped> initialPosition;
 
     public PRM(SearchAlgorithm search, boolean experimentMode){
         this.search = search;
         this.experimentMode = experimentMode;
     }
 
     public PRM(SearchAlgorithm search, boolean experimentMode, long seed){
         this.search = search;
         this.experimentMode = experimentMode;
         this.seed = seed;
     }
 
     @Override
     public GraphName getDefaultNodeName() {
         return GraphName.of("PRMnode");
     }
 
     @Override
     public void onStart(final ConnectedNode node) {
         grid = node.newSubscriber("map", OccupancyGrid._TYPE);
         goals = node.newSubscriber("goal", PoseStamped._TYPE);
         initialPosition = node.newSubscriber("initialpose", PoseWithCovarianceStamped._TYPE);
         inflatedMapPublisher = node.newPublisher("inflatedMap", OccupancyGrid._TYPE);
         PRMMarkers = node.newPublisher("markers", MarkerArray._TYPE);
         routePub = node.newPublisher("route", PoseArray._TYPE);
         prmInfo = node.newPublisher("goalInfo", std_msgs.Int32._TYPE);
 
         pathMarkers = node.newPublisher("pathMarkers", MarkerArray._TYPE);
         if (! experimentMode) {
             PRMMarkers.setLatchMode(true);
         }
         pathMarkers.setLatchMode(true);
         PRM.node = node;
 
         final MessageFactory factory = node.getTopicMessageFactory();
         grid.addMessageListener(new MessageListener<OccupancyGrid>() {
             @Override
             public void onNewMessage(OccupancyGrid map) {
                 System.out.println("PRM node received map. Initialising road map.");
                 originalMap = map;
                 mapReceived = true;
                 initialisePRM(factory);
             }
         });
 
         goals.addMessageListener(new MessageListener<PoseStamped>() {
             @Override
             public void onNewMessage(PoseStamped t) {
                 if (currentPosition == null) {
                     System.out.println("Starting position unset!");
                     return;
                 }
 
                 boolean inFreeSpace = PRMUtil.isPositionValid(t.getPose(), inflatedMap);
                 if (inFreeSpace){
                     goalPosition = t.getPose();
                     Printer.println("GoalPosition in free space", "REDF");
                 } else {
                    System.out.println("Cannot move to specified location. In a wall or outside the map.");
                     return; // If the pose is not in free space, we reject this goal.
                 }
 
                 Printer.println("Generating route...", "REDF");
                 generateRoute();
             }
         });
 
         initialPosition.addMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
             @Override
             public void onNewMessage(PoseWithCovarianceStamped t) {
                 boolean inFreeSpace = util.isPositionValid(t.getPose().getPose(), inflatedMap);
 
                 if (inFreeSpace){
                     currentPosition = t.getPose().getPose();
                     System.out.println("PRM Starting position received.");
                 } else {
                     System.out.println("Cannot start inside a wall or outside the map.");
                 }
             }
         });
 
     }
 
     /** Generates route to goal, regenerating the graph if necessary. */
     public void generateRoute() {
         regenerationAttempts = 0;
         route = null; // Start with no path
         // Try to find a path or regenerate graph until able to
         while (route == null && regenerationAttempts < MAX_REGENERATION_ATTEMPTS) {
             System.out.println("Finding route attempt: " + ++regenerationAttempts);
 
             if (regenerationAttempts > 1) {
                 // Regenerate graph
                 graph.generatePRM(util, inflatedMap);
             }
 
             Vertex start = new Vertex(currentPosition.getPosition());
             Vertex goal = new Vertex(goalPosition.getPosition());
 
             // Try and add the start and goal points to the graph
             boolean startAdded = graph.addVertex(start, util, inflatedMap);
             boolean goalAdded = graph.addVertex(goal, util, inflatedMap);
 
 
             // Check to see if we've already added the start or goal points to the
             // graph before, and if not, we add them.
             if (!startAdded) {
                 start = graph.getVertices().get(graph.getVertices().indexOf(start));
             } else {
                 System.out.println("Start point added to the graph: "
                         + start.getLocation().getX() + ", " + start.getLocation().getY());
             }
             if (!goalAdded) {
                 goal = graph.getVertices().get(graph.getVertices().indexOf(goal));
             } else {
                 System.out.println("Goal point added to the graph: "
                         + goal.getLocation().getX() + ", " + goal.getLocation().getY());
             }
 
             System.out.println("Publishing markers for graph...");
             publishMarkers(graph);
             System.out.println("Finding route to goal...");
             route = findRoute(start, goal);
         }
 
 
         // What the hell are we doing here?
         if (route == null) {
             System.out.println("Could not find a path. Are you sure "
                     + "the destination is reachable?");
 //            MarkerArray paths = pathMarkers.newMessage();
 //            ArrayList<Marker> pathList = new ArrayList<Marker>();
 //            paths.setMarkers(pathList);
 //            pathMarkers.publish(paths);
 //            route = new ArrayList<Vertex>();
             std_msgs.Int32 info = prmInfo.newMessage();
             info.setData(NO_PATH); // we could not find a route - send a message to the info topic
             prmInfo.publish(info);
             return;
         }
 
         // Find a flattened path and print some information about it
         System.out.println("Found route of size: " + route.size() + ". Flattening...");
         flatRoute = util.flattenDrunkenPath(route, -1, inflatedMap); // -1 is flatten fully
         System.out.println("Flattened to size: " + flatRoute.size());
         double percentage = (double) flatRoute.size() / (double) route.size();
         System.out.println("Unflattened path length is: " + PRMUtil.getPathLength(route));
         System.out.println("Flattened path length is: " + PRMUtil.getPathLength(flatRoute));
         System.out.printf("New path is %.2f times the size of the original.\n", percentage);
         MarkerArray paths = pathMarkers.newMessage();
         ArrayList<Marker> pathList = new ArrayList<Marker>();
         pathList.add(util.makePathMarker(route,"originalPath", "blue", 2));
         pathList.add(util.makePathMarker(flatRoute, "flattenedPath", "orange", 3));
 
         paths.setMarkers(pathList);
         pathMarkers.publish(paths);
         routeSearchDone = true;
         std_msgs.Int32 info = prmInfo.newMessage();
         info.setData(PATH_FOUND); // found a route
         prmInfo.publish(info);
         
         // Publish the flattened route so that it can be used by others.
         publishRoute(flatRoute);
     }
 
     /* Initialises the PRM with a utility object and a graph. */
     public void initialisePRM(MessageFactory factory) {
         inflatedMap = PRMUtil.inflateMap(originalMap, inflatedMapPublisher);
         util = new PRMUtil(new Random(seed), factory, inflatedMap);
         graph = new PRMGraph();
         graph.generatePRM(util, inflatedMap);
 
         publishMarkers(graph);
         if (! experimentMode) {
             inflatedMapPublisher.setLatchMode(true);
         }
         inflatedMapPublisher.publish(inflatedMap);
 
         publishGrid();
 
         System.out.println("Average path length: " + util.averageConnectionLength(graph));
         graphGenerationComplete = true;
     }
 
     /*
      * Helper method for finding a route which times how long it takes for the
      * route to be found.
      */
     private ArrayList<Vertex> findRoute(Vertex v1, Vertex v2){
         double start = System.currentTimeMillis();
         ArrayList<Vertex> path =  search.shortestPath(v1, v2, graph, util);
         System.out.println(search + " search took " + (System.currentTimeMillis() - start) + "ms.");
         return path;
     }
 
     /*
      * Publishes the graph markers.
      */
     public void publishMarkers(PRMGraph graph){
         MarkerArray array = PRMMarkers.newMessage();
         array.setMarkers(util.getGraphMarkers(graph, inflatedMap, "/map"));
         PRMMarkers.publish(array);
     }
 
     public void publishGrid(){
         double mapHeight = inflatedMap.getInfo().getHeight();
         double mapWidth = inflatedMap.getInfo().getWidth();
         double mapRes = inflatedMap.getInfo().getResolution();
         MarkerArray arr = PRMMarkers.newMessage();
         Marker m = util.setUpMarker("/map", "grid", 10, Marker.ADD, Marker.LINE_LIST, null, null, null);
         m.getPose().getOrientation().setZ(0.1f);
         m.getColor().setA(1.0f);
         m.getColor().setB(1.0f);
         m.getScale().setX(0.1f);
         int cellWidthMap = (int)(RunParams.getDouble("CELL_WIDTH")/mapRes);
         for (int i = 0; i < mapWidth; i += cellWidthMap) {
             Point p1 = util.factory.newFromType(Point._TYPE);
             Point p2 = util.factory.newFromType(Point._TYPE);
             p1.setX(-i*mapRes);
             p1.setY(0);
             p2.setX(-i*mapRes);
             p2.setY(-mapHeight*mapRes);
 
             m.getPoints().add(p1);
             m.getPoints().add(p2);
         }
         for (int j = 0; j < mapHeight; j += cellWidthMap) {
             Point p1 = util.factory.newFromType(Point._TYPE);
             Point p2 = util.factory.newFromType(Point._TYPE);
             p1.setX(0);
             p1.setY(-j*mapRes);
             p2.setX(-mapWidth*mapRes);
             p2.setY(-j*mapRes);
             m.getPoints().add(p1);
             m.getPoints().add(p2);
         }
 
         ArrayList<Marker> mk = new ArrayList<Marker>();
         mk.add(m);
         arr.setMarkers(mk);
         PRMMarkers.publish(arr);
 
     }
 
     /*
      * Converts the given arraylist of vertices into poses and then publishes it
      */
     public void publishRoute(ArrayList<Vertex> rt){
         PoseArray pa = util.convertVertexList(rt);
         routePub.publish(pa);
     }
 
     /*
      * Reconnects the vertices in the graph. Useful to ensure that you maintain
      * a reasonable number of connections if you are removing nodes and or edges
      * from the graph to deal with obstacles.
      *
      * WARNING: MAY TAKE A LONG TIME!
      */
     public void reconnectGraph() {
         graph.reconnectGraph(util);
     }
 
 
     public boolean routeSearchDone() {
         return routeSearchDone;
     }
 
     public ArrayList<Vertex> getRoute() {
         return route;
     }
 
     public ArrayList<Vertex> getFlatRoute(){
         return flatRoute;
     }
 
     public Pose getCurrentPosition() {
         return currentPosition;
     }
 
     public Pose getGoalPosition() {
         return goalPosition;
     }
 
     public OccupancyGrid getOriginalMap() {
         return originalMap;
     }
 
     public OccupancyGrid getInflatedMap() {
         return inflatedMap;
     }
 
     /*
      * When the PRM receives a map, the grid is updated with the new map.
      * The nodes and edges in the graph are modified to make sure that paths
      * do not pass through obstacles that have been added to the map.
      */
     public void setInflatedMap(OccupancyGrid infMap) {
         System.out.println("Setting inflated map");
         this.inflatedMap = infMap;
         util.setInflatedMap(this.inflatedMap);
         PRMUtil._checkAndPruneGraph(this.graph, this.inflatedMap);
         Printer.println("Graph pruned in PRM", "REDF");
         inflatedMapPublisher.publish(this.inflatedMap);
         publishMarkers(this.graph);
     }
 
     public void setCurrentPosition(Pose currentPosition) {
         this.currentPosition = currentPosition;
     }
 
     public void setGoalPosition(Pose goalPosition) {
         this.goalPosition = goalPosition;
     }
 
     /** How many times we regenerated the graph the last time we searched
      * for a path. */
     public int getRegenerationsForLastSearch() {
         return regenerationAttempts;
     }
 
     /* Sets the search algorithm to be used by the graph to search for the
      * shortest path from one vertex in the graph to another */
     public void setSearchAlgorithm(SearchAlgorithm searchAlg) {
         this.search = searchAlg;
     }
 
     public boolean graphGenerationComplete() {
         return graphGenerationComplete;
     }
 
     @Override
     public void onShutdownComplete(Node node) {
         System.out.println("PRM node successfully shut down.");
     }
 
 
 }
