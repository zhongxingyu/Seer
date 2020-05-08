 package lv.k2611a.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Unit;
 
 public class AStar {
     private static final Logger log = LoggerFactory.getLogger(AStar.class);
     private HashSet<Node> closedSet;
     private SortedSet<Node> openSet;
 
     public List<Node> calcShortestPath(int fromX, int fromY, int toX, int toY, Map map, int unitId, boolean isHarvester, int ownerId) {
 
         if ((fromX == toX) && (fromY == toY)) {
             return new ArrayList<Node>();
         }
 
         Node start = new Node(fromX, fromY);
         Node goal = new Node(toX, toY);
 
        if (map.isObstacle(goal, unitId, ownerId, isHarvester)) {
            return new ArrayList<Node>();
        }
 
         closedSet = new HashSet<Node>();
         openSet = createSortedNodeSet();
 
         start.setDistanceFromStart(0);
         start.setHeuristicDistanceFromGoal(Integer.MAX_VALUE);
 
 
         openSet.add(start);
         while (!openSet.isEmpty()) {
             Node current = openSet.first();
 
             if (current.equals(goal)) {
                 return reconstructPath(current);
             }
 
             openSet.remove(current);
             closedSet.add(current);
 
             // 500 iterations should be enough.
             if (closedSet.size() >= 500) {
                 log.warn("AStar maximum iteration count reached");
                 return new ArrayList<Node>();
             }
 
 
             for (Node neighbor : map.getTileNeighbourNodes(current.getX(), current.getY())) {
                 if (closedSet.contains(neighbor)) {
                     continue;
                 }
                 boolean obstacle = map.isObstacle(neighbor, unitId, ownerId, isHarvester);
                 if (!obstacle) {
                     double neighborDistanceFromStart = current.getDistanceFromStart() + Map.getDistanceBetween(current, neighbor);
                     if (!openSet.contains(neighbor)) {
                         neighbor.setPreviousNode(current);
                         neighbor.setDistanceFromStart(neighborDistanceFromStart);
                         neighbor.setHeuristicDistanceFromGoal(Map.getDistanceBetween(neighbor, goal));
                         openSet.add(neighbor);
                     } else if (neighborDistanceFromStart < current.getDistanceFromStart()) {
                         neighbor.setPreviousNode(current);
                         neighbor.setDistanceFromStart(neighborDistanceFromStart);
                         neighbor.setHeuristicDistanceFromGoal(Map.getDistanceBetween(neighbor, goal));
                     }
                 }
 
             }
         }
         return new ArrayList<Node>();
 
     }
 
     public List<Node> calcPathEvenIfBlocked(int fromX, int fromY, int toX, int toY, Map map, int unitId, boolean isHarvester, int ownerId) {
 
         if ((fromX == toX) && (fromY == toY)) {
             return new ArrayList<Node>();
         }
 
         Node start = new Node(fromX, fromY);
         Node goal = new Node(toX, toY);
 
         //if (map.isObstacle(goal, unitId, ownerId, isHarvester)) {
         //    return new ArrayList<Node>();
         //}
 
         closedSet = new HashSet<Node>();
         openSet = createSortedNodeSet();
 
         start.setDistanceFromStart(0);
         start.setHeuristicDistanceFromGoal(Integer.MAX_VALUE);
 
 
         openSet.add(start);
         while (!openSet.isEmpty()) {
             Node current = openSet.first();
 
             if (current.equals(goal)) {
                 return reconstructPath(current);
             }
 
             openSet.remove(current);
             closedSet.add(current);
 
             // 500 iterations should be enough.
             if (closedSet.size() >= 500) {
                 log.warn("AStar maximum iteration count reached");
                 return reconstructPath(getBestNode(closedSet));
             }
 
 
             for (Node neighbor : map.getTileNeighbourNodes(current.getX(), current.getY())) {
                 if (closedSet.contains(neighbor)) {
                     continue;
                 }
                 boolean obstacle = map.isObstacle(neighbor, unitId, ownerId, isHarvester);
                 if (!obstacle) {
                     double neighborDistanceFromStart = current.getDistanceFromStart() + Map.getDistanceBetween(current, neighbor);
                     if (!openSet.contains(neighbor)) {
                         neighbor.setPreviousNode(current);
                         neighbor.setDistanceFromStart(neighborDistanceFromStart);
                        neighbor.setHeuristicDistanceFromGoal(2 * Map.getDistanceBetween(neighbor, goal));
                         openSet.add(neighbor);
                     } else if (neighborDistanceFromStart < current.getDistanceFromStart()) {
                         neighbor.setPreviousNode(current);
                         neighbor.setDistanceFromStart(neighborDistanceFromStart);
                        neighbor.setHeuristicDistanceFromGoal(2 * Map.getDistanceBetween(neighbor, goal));
                     }
                 }
 
             }
         }
         List<Node> path = reconstructPath(getBestNode(closedSet));
         return path;
 
     }
 
     private Node getBestNode(HashSet<Node> closedSet) {
         Node bestNode = null;
         for (Node node : closedSet) {
             if (node.getPreviousNode() == null) {
                 continue;
             }
             if (bestNode == null) {
                 bestNode = node;
             } else {
                 if (bestNode.getHeuristicDistanceFromGoal() > node.getHeuristicDistanceFromGoal()) {
                     bestNode = node;
                 }
             }
         }
         return bestNode;
     }
 
     private TreeSet<Node> createSortedNodeSet() {
         return new TreeSet<Node>(new Comparator<Node>() {
             @Override
             public int compare(Node o1, Node o2) {
                 return Double.compare(o1.getHeuristicDistanceFromGoal(), o2.getHeuristicDistanceFromGoal());
             }
         });
     }
 
     private List<Node> reconstructPath(Node current) {
         List<Node> result = new ArrayList<Node>();
         if (current == null) {
             return result;
         }
         while (!(current.getPreviousNode() == null)) {
             result.add(current);
             current = current.getPreviousNode();
         }
         Collections.reverse(result);
         return result;
     }
 
     public static boolean pathExists(Unit unit, Map map, Point targetPoint) {
         AStar aStar = new AStar();
         List<Node> path = aStar.calcShortestPath(unit.getX(), unit.getY(), targetPoint.getX(), targetPoint.getY(), map, unit.getId(), true, unit.getOwnerId());
         return !path.isEmpty();
     }
 
 }
