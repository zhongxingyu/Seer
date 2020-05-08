 package structures;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
  * @author jonathan
  */
 public class DisjointSet {
     private HashMap<Integer, Integer> parent;
 
     /**
      * Constructor
      */
     public DisjointSet() {
         parent = new HashMap<Integer, Integer>();
     }
     
     /**
      * Constructor
      * ESTIMATED group size. It's better to guess low.
      */
     public DisjointSet(int size) {
         parent = new HashMap<Integer, Integer>(size);
     }
 
     /**
      * Union operation
      */
     public void union(int p1, int p2) {
         int parent1 = find(p1);
         int parent2 = find(p2);
         
        if(p1 >= p2) 
             parent.put(parent1, parent2);
         else
             parent.put(parent2, parent1);
     }
     
     /**
      * @return true if the given coord comes from the same component,
      *         or false otherwise
      */
     public boolean isConnected(int p1, int p2) {
         return find(p1) == find(p2);
     }
     
     /**
      * Gets the parent of a node
      */
     public int parentOf(int p) {
         if(parent.containsKey(p))
             return parent.get(p);
         return p;
     }
     
     /**
      * Returns the parent that identifies this component
      */
     public int find(int p) {
        while(p != parentOf(p)) p = parentOf(p);
         
         //Auto-balance
        HashSet<Integer> examined = new HashSet<Integer>();
         for(Integer e : examined) {
            parent.put(p, parent.get(e));
         }
         return p;
     }
 }
