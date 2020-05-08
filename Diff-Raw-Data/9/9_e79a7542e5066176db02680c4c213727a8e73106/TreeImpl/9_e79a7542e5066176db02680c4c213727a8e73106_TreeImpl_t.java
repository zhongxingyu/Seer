 /*
  * Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
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
 
 package fr.jamgotchian.abcd.core.graph;
 
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import static fr.jamgotchian.abcd.core.graph.GraphvizUtil.*;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 class TreeImpl<N, E> implements MutableTree<N, E> {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(TreeImpl.class);
 
     private final GraphvizRenderer<N> NODE_GRAPHVIZ_RENDERER
             = new DefaultGraphvizRenderer<N>();
 
     private final GraphvizRenderer<E> EDGE_GRAPHVIZ_RENDERER
             = new DefaultGraphvizRenderer<E>();
 
     private static class Connection<N> {
 
         private final N source;
 
         private final N target;
 
         private Connection(N source, N target) {
             this.source = source;
             this.target = target;
         }
 
         public N getSource() {
             return source;
         }
 
         public N getTarget() {
             return target;
         }
     }
 
     private static class Neighbors<N, E> {
 
         private N parentNode;
 
         private E incomingEdge;
 
         private final Map<N, E> children;
 
         private Neighbors(N parentNode, E incomingEdge, Map<N, E> children) {
             this.parentNode = parentNode;
             this.incomingEdge = incomingEdge;
             this.children = children;
         }
 
         private Neighbors(N parentNode, E incomingEdge) {
             this(parentNode, incomingEdge, new LinkedHashMap<N, E>());
         }
 
         private Neighbors() {
             this(null, null);
         }
 
         public Map<N, E> getChildren() {
             return children;
         }
 
         public N getParentNode() {
             return parentNode;
         }
 
         public void setParentNode(N parentNode) {
             this.parentNode = parentNode;
         }
 
         public E getIncomingEdge() {
             return incomingEdge;
         }
 
         public void setIncomingEdge(E incomingEdge) {
             this.incomingEdge = incomingEdge;
         }
     }
 
     private final N root;
 
     private final Map<E, Connection<N>> edges = new LinkedHashMap<E, Connection<N>>();
 
     private final Map<N, Neighbors<N,E>> nodes = new LinkedHashMap<N, Neighbors<N,E>>();
 
     TreeImpl(N root) {
         this.root = root;
         nodes.put(root, new Neighbors<N, E>());
     }
 
     @Override
     public N getRoot() {
         return root;
     }
 
     @Override
     public Set<N> getNodes() {
         return nodes.keySet();
     }
 
     @Override
     public int getNodeCount() {
         return nodes.size();
     }
 
     @Override
     public Set<E> getEdges() {
         return edges.keySet();
     }
 
     @Override
     public void addNode(N parent, N node, E edge) {
         if (nodes.containsKey(node)) {
             throw new ABCDException("Node " + node + " already present");
         }
         if (edges.containsKey(edge)) {
             throw new ABCDException("Edge " + edge + " already present");
         }
         Neighbors<N,E> parentNeighbors = nodes.get(parent);
         if (parentNeighbors == null) {
             throw new ABCDException("Parent node " + parent + " not found");
         }
         parentNeighbors.getChildren().put(node, edge);
         nodes.put(node, new Neighbors<N, E>(parent, edge));
         edges.put(edge, new Connection<N>(parent, node));
     }
 
     @Override
     public void insertNode(N node, N child, E edge) {
         if (child.equals(root)) {
             throw new ABCDException("Can't insert node before root");
         }
         if (nodes.containsKey(node)) {
             throw new ABCDException("Node " + node + " already present");
         }
         if (edges.containsKey(edge)) {
             throw new ABCDException("Edge " + edge + " already present");
         }
         Neighbors<N,E> childNeighbors = nodes.get(child);
        if (childNeighbors == null) {
             throw new ABCDException("Child " + child + " not found");
         }
         N parent = childNeighbors.getParentNode();
         Neighbors<N,E> parentNeighbors = nodes.get(parent);
         parentNeighbors.getChildren().remove(child);
         parentNeighbors.getChildren().put(node, edge);
         Neighbors<N, E> neighbors = new Neighbors<N, E>(parent, edge);
         neighbors.getChildren().put(child, childNeighbors.getIncomingEdge());
         childNeighbors.setParentNode(node);
         nodes.put(node, neighbors);
         edges.put(edge, new Connection<N>(parent, node));
     }
 
     @Override
     public void addTree(Tree<N, E> tree, N parent, E edge) {
         if (!nodes.containsKey(parent)) {
             throw new ABCDException("Parent node " + parent + " not found");
         }
         addNode(parent, tree.getRoot(), edge);
         for (N child : tree.getChildren(tree.getRoot())) {
             addTree2(tree, child, tree.getRoot());
         }
     }
 
     private void addTree2(Tree<N, E> tree, N node, N parent) {
         addNode(parent, node, tree.getIncomingEdge(node));
         for (N child : tree.getChildren(node)) {
             addTree2(tree, child, node);
         }
     }
 
     @Override
     public boolean containsNode(N node) {
         return nodes.containsKey(node);
     }
 
     @Override
     public N getParent(N node) {
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         return neighbors.getParentNode();
     }
 
     private void getAncestors(N node, List<N> ancestors) {
         N parent = getParent(node);
         if (parent != null) {
             ancestors.add(parent);
             getAncestors(parent, ancestors);
         }
     }
 
     @Override
     public Collection<N> getAncestors(N node) {
         List<N> ancestors = new ArrayList<N>();
         getAncestors(node, ancestors);
         return ancestors;
     }
 
     @Override
     public E getIncomingEdge(N node) {
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         return neighbors.getIncomingEdge();
     }
 
     @Override
     public void setNewParent(N node, N newParent) {
         if (node.equals(root)) {
             throw new ABCDException("Can't change parent of root node");
         }
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         Neighbors<N,E> newParentNeighbors = nodes.get(newParent);
         if (newParentNeighbors == null) {
             throw new ABCDException("New parent node " + newParent + " not found");
         }
         E edge = neighbors.getIncomingEdge();
         N oldParent = getParent(node);
         Neighbors<N,E> oldParentNeighbors = nodes.get(oldParent);
         oldParentNeighbors.children.remove(node);
         newParentNeighbors.children.put(node, edge);
         neighbors.setParentNode(newParent);
         edges.put(edge, new Connection<N>(newParent, node));
     }
 
     @Override
     public Set<N> getChildren(N node) {
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         return neighbors.getChildren().keySet();
     }
 
     @Override
     public Set<N> getChildren(N node, Filter<N> filter) {
         Set<N> filteredChildren = new LinkedHashSet<N>();
         for (N child : getChildren(node)) {
             if (filter.accept(child)) {
                 filteredChildren.add(child);
             }
         }
         return filteredChildren;
     }
 
     @Override
     public N getFirstChild(N node) {
         if (getChildrenCount(node) == 0) {
             return null;
         } else {
             return getChildren(node).iterator().next();
         }
     }
 
     @Override
     public N getFirstChild(N node, Filter<N> filter) {
         for (N child : getChildren(node)) {
             if (filter.accept(child)) {
                 return child;
             }
         }
         return null;
     }
 
     @Override
     public int getChildrenCount(N node) {
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         return neighbors.getChildren().size();
     }
 
     @Override
     public Set<N> getLeaves() {
         Set<N> leaves = new HashSet<N>(1);
         for (N node : getNodes()) {
             if (getChildrenCount(node) == 0) {
                 leaves.add(node);
             }
         }
         return leaves;
     }
 
     @Override
     public N getEdgeSource(E edge) {
         if (edge == null) {
             throw new ABCDException("edge == null");
         }
         Connection<N> connection = edges.get(edge);
         if (connection == null) {
             throw new ABCDException("Edge " + edge + " not found");
         }
         return connection.getSource();
     }
 
     @Override
     public N getEdgeTarget(E edge) {
         if (edge == null) {
             throw new ABCDException("edge == null");
         }
         Connection<N> connection = edges.get(edge);
         if (connection == null) {
             throw new ABCDException("Edge " + edge + " not found");
         }
         return connection.getTarget();
     }
 
     @Override
     public int getDepthFromRoot(N node) {
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         int depth = 0;
         for (N n = getParent(node); n != null; n = getParent(n)) {
             depth++;
         }
         return depth;
     }
 
     @Override
     public Tree<N, E> getSubTree(N node) {
         MutableTree<N, E> subTree = new TreeImpl<N, E>(node);
         buildSubTree(node, subTree);
         return subTree;
     }
 
     private void buildSubTree(N node, MutableTree<N, E> subTree) {
         for (N child : getChildren(node)) {
             subTree.addNode(node, child, getIncomingEdge(child));
         }
         for (N child : getChildren(node)) {
             buildSubTree(child, subTree);
         }
     }
 
     @Override
     public Iterator<N> iterator(N node) {
         return getSubTree(root).getNodes().iterator();
     }
 
     @Override
     public Iterator<N> iterator() {
         return iterator(root);
     }
 
     @Override
     public List<N> getNodesPostOrder() {
         List<N> nodesPostOrder = new ArrayList<N>(nodes.size());
         visitNodePostOrder(root, nodesPostOrder);
         return nodesPostOrder;
     }
 
     private void visitNodePostOrder(N node, List<N> nodesPostOrder) {
         for (N child : getChildren(node)) {
             visitNodePostOrder(child, nodesPostOrder);
         }
         nodesPostOrder.add(node);
     }
 
     @Override
     public List<N> getNodesPreOrder() {
         List<N> nodesPreOrder = new ArrayList<N>(nodes.size());
         visitNodePreOrder(root, nodesPreOrder);
         return nodesPreOrder;
     }
 
     private void visitNodePreOrder(N node, List<N> nodesPreOrder) {
         nodesPreOrder.add(node);
         for (N child : getChildren(node)) {
             visitNodePreOrder(child, nodesPreOrder);
         }
     }
 
     private Collection<N> getNodePlusAncestors(N node) {
         List<N> ancestors = new ArrayList<N>(1);
         ancestors.add(node);
         getAncestors(node, ancestors);
         return ancestors;
     }
 
     @Override
     public N getFirstCommonAncestor(Collection<N> nodes) {
         if (nodes == null) {
             throw new ABCDException("nodes == null");
         }
         if (nodes.isEmpty()) {
             throw new ABCDException("nodes.isEmpty()");
         }
         if (nodes.size() == 1) {
             return nodes.iterator().next();
         }
         List<N> commonAncestors = null;
         for (N node : nodes) {
             Collection<N> ancestors = getNodePlusAncestors(node);
             if (commonAncestors == null) {
                 commonAncestors = new ArrayList<N>(ancestors);
             } else {
                 commonAncestors.retainAll(ancestors);
             }
         }
         if (commonAncestors.isEmpty()) {
             throw new ABCDException("Common ancestor not found");
         }
         return commonAncestors.get(0);
     }
 
     @Override
     public void removeNode(N node) {
         if (node.equals(root)) {
             throw new ABCDException("Can't remove root node");
         }
         Neighbors<N,E> neighbors = nodes.get(node);
         if (neighbors == null) {
             throw new ABCDException("Node " + node + " not found");
         }
         E edge = neighbors.getIncomingEdge();
         N parent = neighbors.getParentNode();
         Neighbors<N,E> parentNeighbors = nodes.get(parent);
         parentNeighbors.getChildren().remove(node);
         parentNeighbors.getChildren().putAll(neighbors.getChildren());
         for (Map.Entry<N, E> child : neighbors.getChildren().entrySet()) {
             Neighbors<N,E> childNeighbors = nodes.get(child.getKey());
             childNeighbors.setParentNode(parent);
         }
         nodes.remove(node);
         edges.remove(edge);
     }
 
     @Override
     public void removeSubtree(N subtreeRoot) {
         for (N node : getSubTree(subtreeRoot).getNodesPostOrder()) {
             removeNode(node);
         }
     }
 
     @Override
     public void exportPane(Writer writer, String title, int paneId, int indentLevel,
                            GraphvizRenderer<N> nodeRenderer,
                            GraphvizRenderer<E> edgeRenderer) throws IOException {
         writeIndent(writer, indentLevel);
         writer.append("subgraph cluster_title_").append(Integer.toString(paneId)).append(" {\n");
         writeIndent(writer, indentLevel+1);
         writer.append("fontsize=\"18\";\n");
         writeIndent(writer, indentLevel+1);
         writer.append("labeljust=\"left\";\n");
         writeIndent(writer, indentLevel+1);
         writer.append("label=\"").append(title).append("\";\n");
 
         for (N node : getNodes()) {
             writeIndent(writer, indentLevel+1);
             writer.append(Integer.toString(System.identityHashCode(node)))
                     .append(Integer.toString(paneId))
                     .append(" ");
             writeAttributes(writer, nodeRenderer.getAttributes(node));
             writer.append("\n");
         }
 
         for (E edge : getEdges()) {
             N source = getEdgeSource(edge);
             N target = getEdgeTarget(edge);
             writeIndent(writer, indentLevel+1);
             writer.append(Integer.toString(System.identityHashCode(source)))
                     .append(Integer.toString(paneId))
                     .append(" -> ")
                     .append(Integer.toString(System.identityHashCode(target)))
                     .append(Integer.toString(paneId));
             writeAttributes(writer, edgeRenderer.getAttributes(edge));
             writer.append("\n");
         }
 
         writeIndent(writer, indentLevel);
         writer.append("}\n");
     }
 
     @Override
     public void export(Writer writer, String title,
                        GraphvizRenderer<N> nodeRenderer,
                        GraphvizRenderer<E> edgeRenderer) throws IOException {
         writer.append("digraph T").append(" {\n");
         exportPane(writer, title, 0, 1, nodeRenderer, edgeRenderer);
         writer.append("}\n");
 
     }
 
     @Override
     public void export(Writer writer, String title) throws IOException {
         export(writer, title, NODE_GRAPHVIZ_RENDERER, EDGE_GRAPHVIZ_RENDERER);
     }
 
     @Override
     public void export(String fileName, String title,
                        GraphvizRenderer<N> nodeRenderer,
                        GraphvizRenderer<E> edgeRenderer) {
         Writer writer = null;
         try {
             writer = new FileWriter(fileName);
             export(writer, title, nodeRenderer, edgeRenderer);
         } catch (IOException e) {
             LOGGER.error(e.toString(), e);
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                     LOGGER.error(e.toString(), e);
                 }
             }
         }
     }
 
     @Override
     public void export(String fileName, String title) {
         Writer writer = null;
         try {
             writer = new FileWriter(fileName);
             export(writer, title);
         } catch (IOException e) {
             LOGGER.error(e.toString(), e);
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                     LOGGER.error(e.toString(), e);
                 }
             }
         }
     }
 
 }
