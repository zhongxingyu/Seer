 package ogo.spec.game.model;
 
 import java.util.PriorityQueue;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.HashMap;
 import java.util.Collection;
 
 /**
  * The map of the game.
  *
  * Includes methods for finding the shortest path between two points on the map.
  */
 public class GameMap
 {
     /**
      * All the tiles.
      */
     protected Tile[][] tiles;
 
     // distance between two tiles
     public final static int DIST = 1000;
 
     /**
      * Constructor.
      */
     public GameMap(TileType[][] types) {
         tiles = new Tile[types.length][types[0].length];
         for (int i = 0; i < tiles.length; i++) {
             for (int j = 0; j < tiles[0].length; j++) {
                 tiles[i][j] = new Tile(types[i][j], i, j);
             }
         }
     }
 
     /**
      * Get a tile.
      */
     public Tile getTile(int x, int y)
     {
         return tiles[x][y];
     }
 
     /**
      * Get de width of the map.
      */
     public int getWidth() {
         return tiles[0].length;
     }
 
     /**
      * Get de height of the map.
      */
     public int getHeight() {
         return tiles.length;
     }
 
     /**
      * A node in the path.
      */
     private class Node implements Comparable<Node>
     {
         Tile tile;
         Node prev;
 
         // g and f values
         int g = Integer.MAX_VALUE, f = Integer.MAX_VALUE;
 
         Node(Tile tile)
         {
             this.tile = tile;
         }
 
         /**
          * Get the h(x) value
          */
         int h(Tile x)
         {
             return getDistance(tile, x);
         }
 
         /**
          * Compare.
          */
         public int compareTo(Node t)
         {
             return f - t.f;
         }
     }
 
     /**
      * Calculate a path from a source to a target.
      *
      * This method uses A*
      */
     public Collection<Tile> calculatePath(Tile source, Tile target, Set<TileType> allowedTypes)
     {
         Node link = AyStar(source, target, allowedTypes);
        
         // now traverse the path
         LinkedList<Tile> path = new LinkedList<Tile>();
 
         if (link == null) {
             return path;
         }
        
         do {
             path.addFirst(link.tile);
         } while ((link = link.prev) != null); // equivalent to link=link.prev;link!=null
 
         return path;
     }
 
     /**
      * Get the distance between two tiles.
      */
     public int getDistance(Tile a, Tile b)
     {
         int dx = Math.abs(a.x - b.x);
         int dy = Math.abs(a.y - b.y);
         return (int) (Math.hypot(dx, dy) * 1000);
     }
 
     /**
      * Get the neighbours of a tile.
      */
     public List<Tile> getNeighbours(Tile tile)
     {
         LinkedList<Tile> neighbours = new LinkedList<Tile>();
         for (int x = tile.x - 1; x <= tile.x + 1; x++) {
             for (int y = tile.y - 1; y <= tile.y + 1; y++) {
                 if ((x == tile.x && y == tile.y) || x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) {
                     continue;
                 }
                 neighbours.add(tiles[x][y]);
             }
         }
         return neighbours;
     }
 
     /**
      * A* algorithm.
      *
      * @see <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">Wikipedia article on A*<a>
      */
     private Node AyStar(Tile source, Tile target, Set<TileType> allowedTypes)
     {
         Set<Tile> done = new HashSet<Tile>();
         HashMap<Tile,Node> open = new HashMap<Tile,Node>();
         PriorityQueue<Node> Q = new PriorityQueue<Node>();
 
         // initialize the source
         Node start = new Node(source);
         start.g = 0;
         start.f = start.g + start.h(target);
 
         Q.add(start);
         open.put(source, start);
 
         Node current;
         while ((current = Q.poll()) != null) {
             if (current.tile == target) {
                 return current;
             }
 
             open.remove(current.tile);
             done.add(current.tile);
 
             // loop through all neighbours, and add them
             for (Tile neighbour : getNeighbours(current.tile)) {
                 if (done.contains(neighbour) || !allowedTypes.contains(neighbour.getType())) {
                     continue;
                 }
 
                 Node neighbourNode;
 
                 // create the node if required
                 if (!open.containsKey(neighbour)) {
                     neighbourNode = new Node(neighbour);
                 } else {
                     neighbourNode = open.get(neighbour);
                 }
 
                 int distanceThroughNeighbour = current.g + getDistance(current.tile, neighbour);
 
                 // get the shortest distance
                 if (distanceThroughNeighbour <= neighbourNode.g) {
                     neighbourNode.g = distanceThroughNeighbour;
                     neighbourNode.f = distanceThroughNeighbour + current.h(neighbour);
                     open.put(neighbour, neighbourNode);
                     Q.remove(neighbourNode);
                     Q.add(neighbourNode);
                 }
             }
         }
         // nothing found
         return null;
     }
 }
