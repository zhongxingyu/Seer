 import java.util.TreeSet;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.ArrayList;
 import java.util.Stack;
 
 
 public final class GraphFunctions {
     /* Chapter 9
      * Paragraph 5
      */
     public static <T extends Comparable<? super T>> void visita(Graph<T> g, Node<T> r) {
         TreeSet<Node<T> > s = new TreeSet<Node<T>>();
         Set<Node<T> > visited = new TreeSet<Node<T>>();
 
         s.add(r);
         visited.add(r);
 
         while (s.size() > 0) {
             Node<T> u = s.first();
             s.remove(u);
             System.out.println("Visiting node: " + u.key + "=" + u.value);
             for (Node<T> v : g.adj(u)) {
                 if (!visited.contains(v)) {
                     visited.add(v);
                     s.add(v);
                 }
             }
         }
     }
 
 
     /* Chapter 9
      * Paragraph 5.1
      */
     public static <T extends Comparable<? super T>> void bfs(Graph<T> g, Node<T> r) {
         Queue<Node<T> > s = new LinkedBlockingQueue<Node<T>>();
         Set<Node<T> > visited = new TreeSet<Node<T>>();
 
         s.offer(r);
         visited.add(r);
 
         while (!s.isEmpty()) {
             Node<T> u = s.poll();
             System.out.println("Visiting node: " + u.key + "=" + u.value);
 
             for (Node<T> v : g.adj(u)) {
                 if (!visited.contains(v)) {
                     visited.add(v);
                     s.offer(v);
                 }
             }
         }
     };
 
 
     /* Chapter 9
      * Paragraph 5.2
      */
     public static <T extends Comparable<? super T>> void erdos(Graph<T> g, Node<T> r, int[] erdos_numbers, ArrayList<Node<T>> paths) {
         Queue<Node<T> > s = new LinkedBlockingQueue<Node<T>>();
 
         s.offer(r);
 
         for (int i=0; i<g.numVertex(); i++) {
             erdos_numbers[i] = -1;
             paths.add(i, null);
         }
         erdos_numbers[g.id(r)] = 0;
 
         while (!s.isEmpty()) {
             Node<T> u = s.poll();
 
             for (Node<T> v : g.adj(u)) {
                 if (erdos_numbers[g.id(v)] == -1) {
                     erdos_numbers[g.id(v)] = erdos_numbers[g.id(u)] + 1;
                     paths.add(g.id(v), u);
                     s.offer(v);
                 }
             }
         }
     }
 
 
     /* Chapter 9
      * Paragraph 5.3
      */
     public static <T extends Comparable<? super T>> void stampaCammino(Graph<T> g, Node<T> r, Node<T> s, ArrayList<Node<T>> paths) {
         if (r == s) {
             System.out.println("Nodo " + r.key + "=" + r.value);
         }
         else if (paths.get(g.id(s)) == null) {
             System.out.println("Nessun cammino da r a s");
         }
         else {
             stampaCammino(g, r, paths.get(g.id(s)), paths);
             System.out.println("Nodo " + s.key + "=" + s.value);
         }
     }
 
 
     /* Chapter 9
      * Paragraph 5.4
      */
     public static <T extends Comparable<? super T>> void dfs(Graph<T> g, Node<T> u) {
         TreeSet<Node<T> > visited = new TreeSet<Node<T>>();
         dfs_rec(g, u, visited);
     }
 
     public static <T extends Comparable<? super T>> void dfs_rec(Graph<T> g, Node<T> u, Set<Node<T>> visited) {
         visited.add(u);
         System.out.println("Visiting node: " + u.key + "=" + u.value);
         for (Node<T> v : g.adj(u)) {
             if (!visited.contains(v)) {
                 visited.add(v);
                 dfs_rec(g, v, visited);
             }
         }
     }
 
 
     /* Chapter 9
      * Paragraph 5.5
      *
      * id is the output, it's a map with nodes as keys and the id of the
      * connected component as value
      */
     public static <T extends Comparable<? super T>> void cc(Graph<T> g, ArrayList<Node<T> > ordine, TreeMap<Node<T>, Integer> id) {
         for (Node<T> node : g.v()) {
             id.put(node, 0);
         }
         int conta = 0;
 
         for (int i=0; i<ordine.size(); i++) {
             if (id.get(ordine.get(i)) == 0) {
                 conta++;
                 ccdfs(g, conta, ordine.get(i), id);
             }
         }
     }
 
     public static <T extends Comparable<? super T>> void ccdfs(Graph<T> g, int conta, Node<T> u, TreeMap<Node<T>, Integer> id) {
         id.put(u, conta);
 
         for (Node<T> v : g.adj(u)) {
             if (id.get(v) == 0) {
                 ccdfs(g, conta, v, id);
             }
         }
     }
 
 
     /* Chapter 9
      * Paragraph 5.7
      */
     public static <T extends Comparable<? super T>> boolean is_ciclico(Graph<T> g) {
         int[] dt = new int[g.numVertex()];
         int[] ft = new int[g.numVertex()];
 
         for (Node<T> node : g.v()) {
             for (int i=0; i<g.numVertex(); i++) {
                 dt[i] = 0;
                 ft[i] = 0;
             }
             if (ciclico(g, node, 0, dt, ft)) {
                 return true;
             }
         }
         return false;
     }
 
     public static <T extends Comparable<? super T>> boolean ciclico(Graph<T> g, Node<T> u, int time, int[] dt, int[] ft) {
         time++;
         dt[g.id(u)] = time;
 
         for (Node<T> v : g.adj(u)) {
             if (dt[g.id(v)] == 0) {
                 if (ciclico(g, v, time, dt, ft)) {
                     return true;
                 }
             }
             else if (dt[g.id(u)] > dt[g.id(v)] && ft[g.id(v)] == 0) {
                 return true;
             }
         }
         time++;
         ft[g.id(u)] = time;
 
         return false;
     }
 
 
     /* Chapter 9
      * Paragraph 5.7
      */
     public static <T extends Comparable<? super T>> void dfs_stack(Graph<T> g, Set<Node<T> > visited, Stack<Node<T> > s, Node<T> u) {
         visited.add(u);
 
         for (Node<T> v : g.adj(u)) {
             if (!visited.contains(v)) {
                 dfs_stack(g, visited, s, v);
             }
         }
         s.push(u);
     }
 
 
     public static <T extends Comparable<? super T>> void scc(Graph<T> g, TreeMap<Node<T>, Integer> id) {
         Stack<Node<T>> s = new Stack<Node<T>>();
         TreeSet<Node<T>> visited = new TreeSet<Node<T>>();
 
         Set<Node<T> > vertices = g.v();
         for (Node<T> v : vertices) {
            if (!visited.contains(v)) {
                dfs_stack(g, visited, s, v);
            }
         }
 
         ListsGraph<T> gt = new ListsGraph<T>(g.numVertex());
         for (Node<T> v : vertices) {
             gt.insertNode(v);
         }
 
         for (Node<T> u : vertices) {
             for (Node<T> v : g.adj(u)) {
                 gt.insertEdge(v, u);
             }
         }
 
         ArrayList<Node<T> > ordine = new ArrayList<Node<T>>();
         for (int i=0; i<s.size(); i++) {
             ordine.add(s.pop());
         }
 
         cc(gt, ordine, id);
     }
 
 
     public static <T extends Comparable<? super T>> void topSort(Graph<T> g, ArrayList<Node<T> > ordine) {
         TreeSet<Node<T> > visited = new TreeSet<Node<T>>();
 
         for (Node<T> u : g.v()) {
             if (!visited.contains(u)) {
                 ts_dfs(g, u, visited, ordine);
             }
         }
     }
 
     public static <T extends Comparable<? super T>> void ts_dfs(Graph<T> g, Node<T> u, Set<Node<T>> visited, ArrayList<Node<T>> ordine) {
         visited.add(u);
 
         for (Node<T> v : g.adj(u)) {
             if (!visited.contains(v)) {
                 ts_dfs(g, v, visited, ordine);
             }
         }
         ordine.add(u);
     }
 }
