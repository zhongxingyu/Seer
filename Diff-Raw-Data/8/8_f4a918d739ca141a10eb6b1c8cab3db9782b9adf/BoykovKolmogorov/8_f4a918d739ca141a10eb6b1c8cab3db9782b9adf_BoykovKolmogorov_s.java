 package graph;
 
 import com.sun.xml.internal.bind.util.Which;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Stack;
 
 /**
  *
  *
  * @author <a ref ="zianfanti@gmail.com"> Zian Fanti<a/>
  *
  * @Article{Boykov and Kolmogorov 2004, author = "Yuri Boykov and Vladimir
  * Kolmogorov", title = "An Experimental Comparison of Min-Cut/Max-Flow
  * Algorithms for Energy Minimization in Vision", journal = "<i>IEEE
  * TRANSACTIONS ON PATTERN ANALYSIS AND MACHINE INTELLIGENCE</i>", year =
  * "2004", pages = "1124-1137", keywords = "Energy minimization, graph
  * algorithms, minimum cut, maximum flow, image restoration, segmentation,
  * stereo, multicamera scene reconstruction", }
  */
 public class BoykovKolmogorov {
 
     /**
      * The given graph to apply this algorithm
      */
     private Graph graph;
     /**
      * the source vertex for the algorithm
      */
     private Vertex source;
     /**
      * the sink vertex for the algorithm
      */
     private Vertex target;
     /**
      * The source tree
      */
     private Graph S;
     /**
      * The target tree
      */
     private Graph T;
     /**
      *
      */
     private LinkedList<Vertex> active;
     /**
      *
      */
     private LinkedList<Vertex> orphans;
     /**
      * Flag indicating the affiliation of each vertex, for vertexes in S tree
      */
     private boolean[] S_tree;
     /**
      *
      */
     private boolean[] T_tree;
 
     /**
      *
      * @param graph
      * @param source
      * @param target
      */
     public BoykovKolmogorov(Graph graph, Vertex source, Vertex target) {
         this.graph = graph;
         this.source = source;
         this.target = target;
 
 
         this.S = new Graph();
         this.T = new Graph();
         S.addVertex(source);
         T.addVertex(target);
 
         this.active = new LinkedList<Vertex>();
         active.add(source);
         active.add(target);
 
         this.orphans = new LinkedList<Vertex>();
 
         this.S_tree = new boolean[graph.size()];
         this.T_tree = new boolean[graph.size()];
         S_tree[source.getName()] = true;
         T_tree[target.getName()] = true;
     }
 
     /**
      *
      * @return
      */
     public ArrayList<Vertex> minCut(int width) {
         while (true) {
            Edge[] path = grow();
            if (path.length == 0) {
                 ArrayList<Vertex> mincut = new ArrayList<Vertex>(S.size());
                 for (Vertex v : S.getVertexes()) {
                     if (!v.equals(source)) {
                         mincut.add(v);
                     }
                 }
                 return mincut;
             }
             augment(path);
             adopt(width);
             return null;
         }
     }
 
     /**
      * Active nodes acquire new children from a set of free nodes.
      *
      *
      * @return an <code>ArrayList</code> containing the path between source and
      * target vertexes as a sequence.
      */
     public Edge[] grow() {
         while (!active.isEmpty()) {
             Vertex p = active.pop();
             ArrayList<Edge> currentEdges = graph.getEdges(p);
             for (Edge edge : currentEdges) {
                 // if tree_cap(p->q) > 0
                 if (edge.getWeight() > 0) {
                     Vertex q = edge.getTarget();
                     if (this.tree(q) == 0) {
                         q.setParent(p);
                         switch (this.tree(p)) {
                             case 1:
                                 S.addVertex(q);
                                 S_tree[q.getName()] = true;
                                 break;
                             case 2:
                                 T.addVertex(q);
                                 T_tree[q.getName()] = true;
                                 break;
                         }
                         active.add(q);
                     }
                     if ((this.tree(q) != 0) && (this.tree(q) != this.tree(p))) {
                         // return P = PATH_(s->t)                                                       
                         Stack<Edge> sPath = new Stack<Edge>();
                         // find path from p to s
                         Vertex current = p;
                         Vertex parent = current.getParent();
                         while (parent != null) {
                             Edge e = graph.getEdge(parent, current);
                             sPath.push(e);
                             current = parent;
                         }         
                         
                         ArrayList<Edge> tPath = new ArrayList<Edge>();
                         // find path from q to t 
                         current = q;
                         parent = current.getParent();
                         while (parent != null) {
                             Edge e = graph.getEdge(parent, current);
                             tPath.add(e);
                             current = parent;
                         }         
                         
                         // concatenate the the paths s->p and q->t
                         Edge[] path = new Edge[sPath.size() + tPath.size() + 1];
                         int idx = 0;
                         while(!sPath.empty()) {
                             path[idx] = sPath.pop();
                             idx++;
                         }
                         path[idx] = graph.getEdge(p, q);
                         idx++;
                         for (Edge e : tPath) {
                             path[idx] = e;
                             idx++;
                         }
                                                 
                         return path;
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      *
      * @param path
      */
     public void augment(Edge[] path) {
         // find the bottleneck capacity delta on path P
         float delta = Float.MAX_VALUE;
         for (int i = 0; i < path.length; i++) {
             float tempWeight = path[i].getWeight();
             if (tempWeight < delta) {
                 delta = tempWeight;
             }
         }
 
         // update the residual graph by pushing flow Delta through P
         for (int i = 0; i < path.length; i++) {
             Edge edge = path[i];
             edge.setWeight(edge.getWeight() - delta);
 
 //            Edge residualEdge = graph.getEdge(edge.getTarget(), edge.getSource());
 //            if (residualEdge == null) {
 //                graph.addEdge(new Edge(edge.getTarget(), edge.getSource(), delta));
 //            }
 //            else {
 //                residualEdge.setWeight(residualEdge.getWeight() + delta);
 //            }
 
             if (edge.getWeight() == 0) {
                 Vertex p = edge.getSource();
                 Vertex q = edge.getTarget();
                 if (tree(p) == 1 && tree(q) == 1) {
                     p.setParent(null);
                     orphans.add(q);
                 }
                 if (tree(p) == 2 && tree(q) == 2) {
                     p.setParent(null);
                     orphans.add(p);
                 }
             }
         }
     }
 
     /**
      * All orphan nodes in O are processed until O becomes empty. Each node p
      * being processed tries to find a new valid parent within the same search
      * tree; in case of success, p remains in the tree but with a new parent;
      * otherwise, it becomes a free node and all its children are added to O.
      */
     private void adopt(int width) {
         while (!orphans.isEmpty()) {
             Vertex p = orphans.pop();
 
             // process p
             // find a new valid parent for p
             int[] neighbours = this.getNeighbours(p.getName(), width);
             for (int i = 0; i < neighbours.length; i++) {
                 if (neighbours[i] > 0) {
                     Edge e = graph.getEdge(new Vertex(neighbours[i]), p);
                     Vertex q = e.getSource();
                     if ((tree(q) == tree(p))
                             && (e.getWeight() > 0) && validOrigin(q)) {
                         p.setParent(q);
                     }
                 }
             }
             // If p does not find a valid parent, then p becomes a free node
             ArrayList<Edge> edges = (tree(p) == 1) ? S.getEdges(p) : T.getEdges(p);
             for (Edge e : edges) {
                 Vertex q = e.getTarget();
                 if (e.getWeight() > 0) {
                     active.add(q);
                 }
                 if (q.getParent().equals(p)) {
                     orphans.add(q);
                     p.setParent(null);
                 }
             }
             if (tree(p) == 1) {
                 S_tree[p.getName()] = false;
             }
             if (tree(p) == 2) {
                 T_tree[p.getName()] = false;
             }
             int idx = active.indexOf(p);
             while (idx >= 0) {
                 active.remove(idx);
                 idx = active.indexOf(p);
             }
         }
     }
 
     /**
      * Test if the given vertex belongs to S or T trees, or if vertex is orphan.
      *
      * @param v a vertex in question
      *
      * @return 0 if vertex is orphan. 1 if vertex belongs to S tree. 2 if vertex
      * belongs to T tree.
      */
     private int tree(Vertex v) {
         int name = v.getName();
         if (!S_tree[name] && !T_tree[name]) {
             return 0;
         }
         if (S_tree[name]) {
             return 1;
         }
         if (T_tree[name]) {
             return 2;
         }
         else {
             return -1;
         }
     }
 
     /**
      * Verify if a given
      * <code>Vertex</code> has a conected path to the source or sink vertex;
      *
      * @param q current Vertex
      * @return true if valid path to the source or sink vertex was found, or
      * false in other case
      */
     private boolean validOrigin(Vertex q) {
         Vertex parent = q.getParent();
         while (parent != null) {
             if (parent == source || parent == target) {
                 return true;
             }
             parent = parent.getParent();
         }
         return false;
     }
 
     /**
      * Find the four neighbours of the given pixel index.
      *
      * @param idx
      * @return an array of 4 neighbours of the given index pixel.
      */
     private int[] getNeighbours(int idx, int width) {
         idx--;
         int x = idx % width;
         int y = idx / width;
 
         int[] neighbours = new int[4];
 
         int n_idx = (y * width) + (x - 1);
         if ((n_idx > 0 && n_idx < graph.size() - 1)) {
             neighbours[0] = ++n_idx;
         }
         else {
             neighbours[0] = -1;
         }
         n_idx = ((y - 1) * width) + x;
         if ((n_idx > 0 && n_idx < graph.size() - 1)) {
             neighbours[1] = ++n_idx;
         }
         else {
             neighbours[1] = -1;
         }
         n_idx = (y * width) + (x + 1);
         if ((n_idx > 0 && n_idx < graph.size() - 1)) {
             neighbours[2] = ++n_idx;
         }
         else {
             neighbours[2] = -1;
         }
         n_idx = ((y + 1) * width) + x;
         if ((n_idx > 0 && n_idx < graph.size() - 1)) {
             neighbours[3] = ++n_idx;
         }
         else {
             neighbours[3] = -1;
         }
 
         return neighbours;
     }
     /**
      *
      * @param path
      *
      * @return
      */
 //    private ArrayList<Edge> vertexToEdgePath(Stack<Vertex> inputPath) {
 //        // from target retrieve the source
 //        LinkedList<Vertex> sPath = new LinkedList<Vertex>();
 //        sPath.add(target);
 //        while (sPath.getLast().getParent() != null) {
 //            sPath.add(sPath.getLast().getParent());
 //        }
 //
 //        Edge[] path = new Edge[sPath.size() - 1];
 //        int i = 0;
 //        while (!sPath.isEmpty()) {
 //            Vertex v = sPath.removeFirst();
 //            if (v.getParent() != null) {
 //                ArrayList<Edge> E = tree.getEdges(v.getParent());
 //                for (Edge e : E) {
 //                    if (v.equals(e.getTarget())) {
 //                        path[i] = e;
 ////                            System.out.println(e);
 //                        break;
 //                    }
 //                }
 //            }
 //            i++;
 //        }
 //
 //
 //
 //        if (path.length > 0) {
 //            return path;
 //        } else {
 //            return null;
 //        }
 //    }
 }
