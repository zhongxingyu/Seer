 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.controlflow;
 
 import fr.jamgotchian.abcd.core.graph.EdgeFactory;
 import fr.jamgotchian.abcd.core.graph.DirectedGraph;
 import fr.jamgotchian.abcd.core.graph.MutableTree;
 import fr.jamgotchian.abcd.core.graph.Tree;
 import fr.jamgotchian.abcd.core.graph.Trees;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class DominatorInfo<N, E> {
 
     private static final Logger logger = Logger.getLogger(DominatorInfo.class.getName());
 
     static {
         logger.setLevel(Level.FINE);
     }
 
     private final DirectedGraph<N, E> graph;
 
     private final N entryNode;
 
     private final N exitNode;
 
     private final EdgeFactory<E> factory;
 
     private Map<N, Set<N>> dominatorsOf;
 
     private MutableTree<N, E> dominatorsTree;
 
     private Map<N, Set<E>> dominanceFrontierOf;
 
     private Map<N, Set<N>> postDominatorsOf;
 
     private MutableTree<N, E> postDominatorsTree;
 
     private Map<N, Set<E>> postDominanceFrontierOf;
 
     public static <N, E> DominatorInfo<N, E> create(DirectedGraph<N, E> graph, N entryNode,
                                                     N exitNode, EdgeFactory<E> factory) {
         DominatorInfo<N, E> domInfo = new DominatorInfo<N, E>(graph, entryNode, exitNode, factory);
         domInfo.update();
         return domInfo;
     }
 
     private DominatorInfo(DirectedGraph<N, E> graph, N entryNode, N exitNode, EdgeFactory<E> factory) {
         this.graph = graph;
         this.entryNode = entryNode;
         this.exitNode = exitNode;
         this.factory = factory;
     }
 
     public Set<N> getDominatorsOf(N n) {
         return dominatorsOf.get(n);
     }
 
     public boolean dominate(N x, N y) {
         return dominatorsOf.get(y).contains(x);
     }
 
     public boolean strictlyDominate(N x, N y) {
         return !x.equals(y) && dominate(x, y);
     }
 
     public N getImmediateDominatorOf(N n) {
         return dominatorsTree.getParent(n);
     }
 
     public Tree<N, E> getDominatorsTree() {
         return dominatorsTree;
     }
 
     public Set<E> getDominanceFrontierOf(N n) {
         return dominanceFrontierOf.get(n);
     }
 
    public Set<N> getDominanceFrontierOf2(N n) {
        Set<N> frontier = new HashSet<N>();
        for (E e : dominanceFrontierOf.get(n)) {
            frontier.add(graph.getEdgeTarget(e));
        }
        return frontier;
    }

     public Set<N> getPostDominatorsOf(N n) {
         return postDominatorsOf.get(n);
     }
 
     public Tree<N, E> getPostDominatorsTree() {
         return postDominatorsTree;
     }
 
     public Set<E> getPostDominanceFrontierOf(N n) {
         return postDominanceFrontierOf.get(n);
     }
 
    public Set<N> getPostDominanceFrontierOf2(N n) {
        Set<N> frontier = new HashSet<N>();
        for (E e : postDominanceFrontierOf.get(n)) {
            frontier.add(graph.getEdgeSource(e));
        }
        return frontier;
    }

     public boolean postDominate(N x, N y) {
         return postDominatorsOf.get(y).contains(x);
     }
 
     public boolean strictlyPostDominate(N x, N y) {
         return !x.equals(y) && postDominate(x, y);
     }
 
     public N getImmediatePostDominatorOf(N n) {
         return postDominatorsTree.getParent(n);
     }
 
     private void buildTree(Map<N, Set<N>> dominatorsOf, N parentNode, MutableTree<N, E> tree) {
         for (Map.Entry<N, Set<N>> entry : dominatorsOf.entrySet()) {
             N node = entry.getKey();
             if (!node.equals(parentNode)) {
                 Set<N> dominators = entry.getValue();
                 if (dominators.contains(parentNode)) {
                     if (tree.containsNode(node)) {
                         tree.setParent(node, parentNode);
                     } else {
                         tree.addNode(parentNode, node, factory.createEdge());
                     }
                 }
             }
         }
         for (N child : new HashSet<N>(tree.getChildren(parentNode))) {
             buildTree(dominatorsOf, child, tree);
         }
     }
 
     public void computeDominanceFrontier() {
         dominanceFrontierOf = new HashMap<N, Set<E>>();
 
         // x dominate a predecessor of z (y) but do not strictly dominate z
         for (N x : graph.getVertices()) {
             dominanceFrontierOf.put(x, new HashSet<E>());
             for (N y : dominatorsTree.getSubTree(x).getNodes()) {
                 for (N z : graph.getSuccessorsOf(y)) {
                    if (!z.equals(x)) {
                        if (!strictlyDominate(x, z)) {
                            E yz = graph.getEdge(y, z);
                            dominanceFrontierOf.get(x).add(yz);
                        }
                     }
                 }
             }
 
             logger.log(Level.FINEST, "Dominance frontier of {0} : {1}",
                     new Object[] {x, graph.toString(dominanceFrontierOf.get(x))});
        }
     }
 
     public void computePostDominanceFrontier() {
         postDominanceFrontierOf = new HashMap<N, Set<E>>();
 
         for (N x : graph.getVertices()) {
             postDominanceFrontierOf.put(x, new HashSet<E>());
             for (N y : postDominatorsTree.getSubTree(x).getNodes()) {
                 for (N z : graph.getPredecessorsOf(y)) {
                    if (!z.equals(x)) {
                        if (!strictlyPostDominate(x, z)) {
                            E zy = graph.getEdge(z, y);
                            postDominanceFrontierOf.get(x).add(zy);
                        }
                     }
                 }
             }
 
             logger.log(Level.FINEST, "Post dominance frontier of {0} : {1}",
                     new Object[] {x, graph.toString(postDominanceFrontierOf.get(x))});
        }
     }
 
     public void update() {
         logger.log(Level.FINER, "Update dominator info");
 
         // find dominators
         dominatorsOf = new DominatorsFinder(graph, entryNode).analyse();
         for (Map.Entry<N, Set<N>> entry : dominatorsOf.entrySet()) {
             logger.log(Level.FINEST, "Dominators of {0} : {1}",
                     new Object[] {entry.getKey(), entry.getValue()});
         }
 
         // build dominators tree
         dominatorsTree = Trees.newTree(entryNode);
         buildTree(dominatorsOf, entryNode, dominatorsTree);
         logger.log(Level.FINEST, "Dominators tree :\n{0}", Trees.toString(dominatorsTree));
 
         // compute dominance frontier
         computeDominanceFrontier();
 
         // find post-dominators
         postDominatorsOf = new PostDominatorsFinder(graph, exitNode).analyse();
         for (Map.Entry<N, Set<N>> entry : postDominatorsOf.entrySet()) {
             logger.log(Level.FINEST, "Post-dominators of {0} : {1}",
                     new Object[] {entry.getKey(), entry.getValue()});
         }
 
         // build post-dominators tree
         postDominatorsTree = Trees.newTree(exitNode);
         buildTree(postDominatorsOf, exitNode, postDominatorsTree);
         logger.log(Level.FINEST, "Post dominators tree :\n{0}", Trees.toString(postDominatorsTree));
 
         // compute post dominance frontier
         computePostDominanceFrontier();
     }
 }
