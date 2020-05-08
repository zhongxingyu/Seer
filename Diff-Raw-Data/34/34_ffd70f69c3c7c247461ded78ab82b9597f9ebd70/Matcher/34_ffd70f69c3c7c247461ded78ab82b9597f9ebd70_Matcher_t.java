 package MaxWeightMatching;
 
 import java.util.*;
 
 import Weighting.Weighting;
 
 import Items.Slot;
 import Items.Tutor;
 
 public class Matcher {
     private static Tutor [] t;
     private static Slot [] s;
     private static Graph g;
     private static Weighting w;
     private static int n, sz;
     
     public static void match (Tutor [] tutors, Slot [] slots, Weighting waiter) {
         t = tutors; s = slots; w = waiter;
         
         n = 2 * t.length + s.length; // add t.length dummy variables
         sz = t.length + s.length; // anything indexed after sz is a dummy variable
         
         // run twice, but only assign one slot one tutor
        boolean [] ignore = new boolean [s.length];
         for (int r = 0; r < 2; r++) {
             init();
             
             // compute matching
            for (int i = 0; i < t.length; i++) {
                for (int j = 0; j < s.length; j++) {
                    if (!ignore [j]) { // on second iteration, ignore matched slots
                         updateMatching (i, j + t.length, w.weight(t[i], s[j]));
                     }
                 }
             }
             
             // assign partners
             for (int i = 0; i < t.length; i++) {
                 int k = partner [i] - t.length;
                if (k >= s.length) continue;
                 
                 if (t [i].slot == null)
                     t [i].slot = s [k];
                 else 
                     t [i].slot2 = s [k];
                 
                 s [k].tutor = t [i];
                ignore [k] = true;
             }
         }
         
         
         // one more time
         init();
         // now check if any tutors still need slots
        for (int i = 0; i < t.length; i++) {
            if (t [i].officer && t [i].slot2 == null)
                for (int j = 0; j < s.length; j++) {
                     updateMatching (i, j + t.length, w.weight(t[i], s[j]));
             }
         }
         
         for (int i = 0; i < t.length; i++) {
             int k = partner [i] - t.length;
            if (k >= s.length) continue;
             
             t [i].slot2 = s [k];
             if (s [k].tutor == null)
                 s [k].tutor = t [i];
             else 
                 s [k].tutor2 = t [i];
         }
     }
     
     private static void init() {
      // initialize graph
         g = new Graph (n);
         price = new double [n];
         partner = new int [n];
         matched = new boolean [n];
         Arrays.fill (partner, -1);
         
         // match to dummy variables
         for (int i = 0; i < t.length; i++) {
             g.addEdge (i, i + sz, 0);
             matched [i] = true;
             matched [i + sz] = true;
             partner [i] = i + sz;
             partner [i + sz] = i;
         }
     }
     
     private static int [] prev, ovis, partner;
     private static double [] delta, price;
     private static boolean [] vis, matched;
     
     private static void updateMatching (int a, int b, double c) {
         g.addEdge (a, b, c);
         double d = c - price [a] - price [b];
        if (d <= 0) return; // no need to further process this case
         
         // get a feasible matching
         price [a] += d;
         // rematch a and b if necessary
        if (matched [a]) {
             matched [partner [a]] = false;
             partner [partner [a]] = -1;
         }
        if (matched [b]) {
             matched [partner [b]] = false;
             partner [partner [b]] = -1;
         }
         matched [a] = true; matched [b] = true;
         partner [a] = b; partner [b] = a;
         
         prev = new int [n];
         Arrays.fill (prev, -1);
         vis = new boolean [n];
         ovis = new int [n];
         delta = new double [n];
         
         // now try to find all augmenting paths
         // should be at most 2, since we remove at most 1 matched pair, and we can add at most one new matched pair
         while (true) {
             int end = findAugmentingPath();
             updatePrices();
             if (end == -1) break;
             augmentPath (end);
         }
     }
     
     /**
      * finds an augmenting path and returns the end node
      * we can follow prev pointers to recreate path
      * Running time is O(M log M), M is number of edges (which is usually about N^2) 
      */
     private static int idx;
     private static int findAugmentingPath () {
         idx = -1; 
         double C = 0;
         PriorityQueue <Edge> pq = new PriorityQueue <Edge> ();
         for (int i = 0; i < t.length; i++) {
             if (!matched [i]) {
                 for (Pair node : g.getNeighbors(i)) {
                     pq.add (new Edge (i, node.to, price [i] + price [node.to] - node.weight));
                 }
                 ovis [++idx] = i;
             }
         }
         
         int end = -1;
         while (pq.size() > 0) {
             Edge f = pq.poll();
             if (vis [f.to]) continue;
             vis [f.to] = true; 
             prev [f.to] = f.from;
             
             double v = f.weight - C; 
             C += v; delta [idx] += v;
             
             if (!matched [f.to]) {end = f.to; break;}
             int l = partner [f.to];
             for (Pair node : g.getNeighbors (l)) {
                 if (!vis [node.to]) {
                     pq.add (new Edge (l, node.to, price [l] + price [node.to] - node.weight + C));
                 }
             }
             ovis [++idx] = f.to; ovis [++idx] = l;
         }
         
         return end;
     }
     
     /**
      * Keeps prices up to date
      * Running time: O(N), N is number of nodes
      */
     private static void updatePrices() {
         int k = 0;
         while (idx >= 0) {
             k += delta [idx];
             if (ovis [idx] < t.length) {
                 price [ovis [idx]] -= k;
             } else {
                 price [ovis [idx]] += k;
             }
             idx--;
         }
     }
     
     /**
      * Switches path parities
      * Running time: O(N), N is number of nodes
      * @param end : the end of the path
      */
     private static void augmentPath (int end) {
         while (end != -1) {
             matched [end] = true;
             matched [prev [end]] = true;
             partner [end] = prev [end];
             int next = partner [prev [end]];
             partner [prev [end]] = end;
             end = next;
         }
     }
 }
