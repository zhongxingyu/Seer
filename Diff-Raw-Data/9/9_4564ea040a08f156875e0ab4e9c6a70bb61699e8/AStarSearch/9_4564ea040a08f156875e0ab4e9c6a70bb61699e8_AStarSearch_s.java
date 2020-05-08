 /*
  * Created on Jan 23, 2005
  */
 package org.spacebar.escape.j2se;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import javax.swing.JOptionPane;
 
 import org.spacebar.escape.common.BitInputStream;
 import org.spacebar.escape.common.Entity;
 import org.spacebar.escape.common.hash.MD5;
 
 /**
  * @author adam
  */
 public class AStarSearch implements Runnable {
     PriorityQueue<AStarNode> open = new PriorityQueue<AStarNode>(11,
             new AStarPQComparator());
 
     Map<Level, AStarNode> openMap = new HashMap<Level, AStarNode>();
 
     Set<MD5> closed = new HashSet<MD5>();
 
     int moveLimit = Integer.MAX_VALUE;
 
     private AStarNode start;
 
     public void setMoveLimit(int moves) {
         moveLimit = moves;
     }
 
     final int manhattanMap[][];
 
     public AStarSearch(Level l) {
         // construct initial node
         start = new AStarNode(null, Entity.DIR_NONE, l, 0);
        manhattanMap = new int[start.level.getWidth()][start.level.getHeight()];
         computeManhattanMap();
     }
 
     private void computeManhattanMap() {
         int mmap[][] = manhattanMap;
         Level l = start.level;
         int w = l.getWidth();
         int h = l.getHeight();
 
         // initialize
         for (int y = 0; y < h; y++) {
             for (int x = 0; x < w; x++) {
                 mmap[x][y] = Integer.MAX_VALUE;
             }
         }
 
         // find each exit item
         for (int y = 0; y < h; y++) {
             for (int x = 0; x < w; x++) {
                 if (isUsefulTile(l, x, y)) {
                     mmap[x][y] = 0;
                     doBrushFire(mmap, x, y, 1);
                 }
             }
         }
 
        printMmap(mmap);
     }
 
     // !
     private void doBrushFire(int maze[][], int x, int y, int depth) {
         if (y < maze[0].length - 1 && depth < maze[x][y + 1]) {
             maze[x][y + 1] = depth;
             doBrushFire(maze, x, y + 1, depth + 1);
         }
         if (x > 0 && depth < maze[x - 1][y]) {
             maze[x - 1][y] = depth;
             doBrushFire(maze, x - 1, y, depth + 1);
         }
         if (y > 0 && depth < maze[x][y - 1]) {
             maze[x][y - 1] = depth;
             doBrushFire(maze, x, y - 1, depth + 1);
         }
         if (x < maze.length - 1 && depth < maze[x + 1][y]) {
             maze[x + 1][y] = depth;
             doBrushFire(maze, x + 1, y, depth + 1);
         }
     }
 
     private void printMmap(int[][] mmap) {
         // print
         for (int x = 0; x < mmap[0].length; x++) {
             for (int y = 0; y < mmap.length; y++) {
                 int val = mmap[y][x];
                 String s = Integer.toString(val);
                 System.out.print(s + " ");
                 if (s.length() == 1) {
                     System.out.print(" ");
                 }
             }
             System.out.println();
         }
     }
 
     private void updateOpen(AStarNode a) {
         assert sanityCheck();
         if (openMap.containsKey(a.level)) {
             open.removeAll(Collections.singleton(a));
             assert !open.contains(a);
             openMap.remove(a.level);
         }
         assert sanityCheck();
 
         open.add(a);
         openMap.put(a.level, a);
         assert open.size() == openMap.size();
         assert openMap.containsKey(new Level(a.level));
         assert sanityCheck();
     }
 
     private AStarNode getFromOpen(AStarNode a) {
         assert sanityCheck();
         AStarNode node = openMap.get(a.level);
         assert sanityCheck();
         return node;
     }
 
     private AStarNode removeFromOpen() {
         assert sanityCheck();
         // System.out.println("AStarSearch.removeFromOpen()");
         assert open.size() == openMap.size();
         AStarNode a;
         a = open.remove();
 
         assert openMap.containsValue(a);
         AStarNode result = openMap.remove(a.level);
         assert !openMap.containsValue(a);
         assert a.level.equals(result.level);
         assert open.size() == openMap.size();
         assert sanityCheck();
         return a;
     }
 
     private boolean sanityCheck() {
         List<AStarNode> extraOpenItems = new ArrayList<AStarNode>();
         List<AStarNode> extraOpenMapItems = new ArrayList<AStarNode>();
 
         for (AStarNode node : open) {
             if (!openMap.containsValue(node)) {
                 extraOpenItems.add(node);
             }
         }
 
         for (AStarNode node : openMap.values()) {
             if (!open.contains(node)) {
                 extraOpenMapItems.add(node);
             }
         }
 
         boolean bad = false;
         if (!extraOpenItems.isEmpty()) {
             System.out.println("extra items in open: " + extraOpenItems);
             bad = true;
         }
         if (!extraOpenMapItems.isEmpty()) {
             System.out.println("extra items in openMap: " + extraOpenMapItems);
             bad = true;
         }
 
         return !bad;
     }
 
     int h(Level l) {
         // default heuristic -- override
         int m = manhattan(l);
         bestH = Math.min(bestH, m);
         return m;
     }
 
     private int manhattan(Level l) {
         return manhattanMap[l.getPlayerX()][l.getPlayerY()];
     }
 
     private boolean isUsefulTile(Level l, int x, int y) {
         int t = l.tileAt(x, y);
         int o = l.oTileAt(x, y);
         return t == Level.T_EXIT || t == Level.T_SLEEPINGDOOR
                 || o == Level.T_EXIT || o == Level.T_SLEEPINGDOOR;
     }
 
     List<AStarNode> generateChildren(AStarNode node) {
         // default children generation -- override
         List<AStarNode> l = new ArrayList<AStarNode>();
         Level level = node.level;
 
         if (!level.isDead() && !level.isWon() && node.g < moveLimit) {
             for (int i = Entity.FIRST_DIR; i <= Entity.LAST_DIR; i++) {
                 Level lev = new Level(level);
                 testChild(node, l, i, lev);
             }
             // System.out.println("----------");
         }
 
         return l;
 
     }
 
     protected void testChild(AStarNode node, List<AStarNode> l, int i, Level lev) {
         if (lev.move(i)) {
             // System.out.println(" child");
             if (!closed.contains(lev.MD5())) {
                 l.add(new AStarNode(node, i, lev, node.g + 1)); // cost
                 // of
                 // move is 1
                 // lev.print(System.out);
             }
         }
     }
 
     class AStarNode {
         final AStarNode parent;
 
         final int dirToGetHere;
 
         final Level level;
 
         final int f;
 
         final int g;
 
         @Override
         public int hashCode() {
             return level.hashCode();
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof AStarNode) {
                 AStarNode item = (AStarNode) obj;
                 return level.equals(item.level);
             }
             return false;
         }
 
         @Override
         public String toString() {
             return "(f: " + f + "," + level.toString() + ")";
         }
 
         AStarNode(AStarNode parent, int dir, Level l, int g) {
             this.parent = parent;
             dirToGetHere = dir;
             this.level = l;
             this.g = g;
             int parentF = parent == null ? 0 : parent.f;
             f = Math.max(parentF, g + h(l)); // pathmax!
         }
 
         boolean isGoal() {
             boolean result = !level.isDead() && level.isWon();
             if (result) {
                 System.out.println("GOAL");
             }
             return result;
         }
 
     }
 
     private class AStarPQComparator implements Comparator<AStarNode> {
         public int compare(AStarNode o1, AStarNode o2) {
             if (o1.f < o2.f) {
                 return -1;
             } else if (o1.f > o2.f) {
                 return 1;
             } else {
                 return 0;
             }
         }
     }
 
     int bestH;
 
     private List<Integer> solution;
 
     public void run() {
         long time = 0;
         int prevOpen = 0;
         int prevClosed = 0;
         while (!open.isEmpty()) {
             if (System.currentTimeMillis() - time > 1000) {
                 int os = openMap.size();
                 int cs = closed.size();
                 System.out.println("Open nodes: " + os + ", closed nodes: "
                         + cs + "   (deltas: " + (os - prevOpen) + ", "
                         + (cs - prevClosed) + ")");
                 // System.out.println("best h: " + bestH);
                 prevOpen = os;
                 prevClosed = cs;
                 time = System.currentTimeMillis();
             }
             AStarNode a = removeFromOpen();
             // System.out.println("getting node from open list, f: " + a.f);
             // a.level.print(System.out);
             // System.out.println();
             if (a.isGoal()) {
                 // GOAL
                 solution = constructSolution(a);
                 return;
             } else {
                 // System.out.println("adding to closed list");
                 closed.add(a.level.MD5());
                 List<AStarNode> children = generateChildren(a);
                 Collections.shuffle(children);
                 for (AStarNode node : children) {
                     AStarNode oldNode = getFromOpen(node);
                     if (oldNode == null) {
                         updateOpen(node);
                     } else if (node.g < oldNode.g) {
                         assert node.equals(oldNode);
                         assert node.f < oldNode.f;
                         updateOpen(node);
                     }
                 }
             }
         }
         System.out.println("closed nodes: " + closed.size());
         solution = null;
     }
 
     public void initialize() {
         assert sanityCheck();
         // init lists
         // open.clear();
         // openMap.clear();
         closed.clear();
         assert sanityCheck();
 
         // add to open list
         updateOpen(start);
 
         // init
         bestH = Integer.MAX_VALUE;
     }
 
     private List<Integer> constructSolution(AStarNode a) {
         List<Integer> moves = new ArrayList<Integer>();
         while (a != null) {
             moves.add(a.dirToGetHere);
             a = a.parent;
         }
         Collections.reverse(moves);
         moves.remove(0);
 
         return moves;
     }
 
     public boolean solutionFound() {
         return solution != null;
     }
 
     public void printSolution() {
         if (solution == null) {
             System.out.println("No solution found");
             return;
         }
 
         System.out.println("moves: " + solution.size());
         int lastMove = Entity.DIR_NONE;
         int moveCount = 0;
         for (Integer move : solution) {
             if (move != lastMove) {
                 if (lastMove != Entity.DIR_NONE) {
                     System.out.print(moveCount
                             + Entity.directionToString(lastMove) + " ");
                 }
                 lastMove = move;
                 moveCount = 1;
             } else {
                 moveCount++;
             }
         }
         System.out.println(moveCount + Entity.directionToString(lastMove));
     }
 
     int countSpheres(Level l) {
         // number of spheres
         int count = 0;
         for (int i = 0; i < l.getWidth() * l.getHeight(); i++) {
             if (l.tileAt(i) == Level.T_BSPHERE) {
                 count++;
             }
         }
         if (count < bestH) {
             bestH = count;
         }
         return count;
     }
 
     public static void main(String[] args) {
         try {
             Level l = new Level(
                     new BitInputStream(new FileInputStream(args[0])));
 
             int startingMoves = 2;
             if (args.length > 1) {
                 startingMoves = Integer.parseInt(args[1]);
             }
 
             AStarSearch search2 = new AStarSearch(l) {
                 @Override
                 public int h(Level l) {
                     return countSpheres(l);
                 }
 
                 @Override
                 public List<AStarNode> generateChildren(AStarNode node) {
                     List<AStarNode> l = new ArrayList<AStarNode>();
                     Level level = node.level;
 
                     if (!level.isDead() && !level.isWon() && node.g < moveLimit) {
                         if (countSpheres(level) == 0) {
                             for (int i = Entity.FIRST_DIR; i <= Entity.LAST_DIR; i++) {
                                 Level lev = new Level(level);
                                 testChild(node, l, i, lev);
                             }
                         } else {
                             if (level.getPlayerY() == 3) { // sphererow
                                 Level lev = new Level(level);
                                 testChild(node, l, Entity.DIR_UP, lev);
                                 lev = new Level(level);
                                 testChild(node, l, Entity.DIR_RIGHT, lev);
                             } else {
                                 Level lev = new Level(level);
                                 testChild(node, l, Entity.DIR_DOWN, lev);
                                 lev = new Level(level);
                                 testChild(node, l, Entity.DIR_LEFT, lev);
                                 lev = new Level(level);
                                 testChild(node, l, Entity.DIR_RIGHT, lev);
                             }
                         }
                     }
 
                     return l;
                 }
             };
 
             l.print(System.out);
 
             AStarSearch search = new AStarSearch(l);
             for (int i = startingMoves; i <= 65536; i *= 2) {
                 System.out.print("trying in " + i + " moves, ");
                 search.initialize();
                 search.setMoveLimit(i);
 
                 search.run();
 
                 if (search.solutionFound()) {
                     search.printSolution();
                     robot(search.solution);
                     break;
                 }
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static void robot(List<Integer> s) {
         int time = 2;
         String options[] = { "Type The Solution", "Exit" };
         int result = JOptionPane.showOptionDialog(null,
                "Do you want to run the solution in " + time + " seconds?",
                 "Solution Found", JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
         if (result == JOptionPane.YES_OPTION) {
             Robot r = null;
             try {
                 r = new Robot();
             } catch (AWTException e) {
                 e.printStackTrace();
                 return;
             }
 
             r.setAutoDelay(40);
 
             System.out.print("Running in " + time + " seconds...");
             System.out.flush();
             try {
                 Thread.sleep(time * 1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             System.out.println();
 
             for (int dir : s) {
                 System.out.print(Entity.directionToString(dir) + " ");
                 System.out.flush();
 
                 int k = directionToKey(dir);
                 r.keyPress(k);
                 r.keyRelease(k);
             }
             System.out.println();
         }
     }
 
     private static int directionToKey(int dir) {
         switch (dir) {
         case Entity.DIR_UP:
             return KeyEvent.VK_UP;
         case Entity.DIR_DOWN:
             return KeyEvent.VK_DOWN;
         case Entity.DIR_LEFT:
             return KeyEvent.VK_LEFT;
         case Entity.DIR_RIGHT:
             return KeyEvent.VK_RIGHT;
 
         default:
             assert false;
             return 0;
         }
     }
 }
