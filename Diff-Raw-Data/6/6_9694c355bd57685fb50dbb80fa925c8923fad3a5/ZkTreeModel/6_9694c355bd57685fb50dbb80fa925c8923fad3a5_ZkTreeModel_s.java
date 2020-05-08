 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.zkutils;
 
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.TreeNode;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * Since default ZK implementation of {@link org.zkoss.zul.TreeModel} doesn't provide handy methods e.g. for searching
  * nodes, this one is created to provide it and a lot more.
  *
  * @author stanislav bashkirtsev
  * @author Guram Savinov
  * @since 0.15
  */
 public class ZkTreeModel<E> extends DefaultTreeModel<E> {
     private static final long serialVersionUID = 20110131094143L;
 
     /**
      * Creates a tree with the specified note as the root.
      *
      * @param root the root (cannot be null)
      */
     public ZkTreeModel(@Nonnull TreeNode<E> root) {
         super(root);
     }
 
     /**
      * Recursively searches for the first node that contains {@code data} as its content and returns it.
      *
      * @param data the data to find a node with
      * @return the first node with the data set to specified one
      * @see org.zkoss.zul.TreeNode#getData()
      */
     public TreeNode<E> find(@Nonnull E data) {
         return find(getRoot(), data);
     }
 
     /**
      * Returns a child of the tree that sits at the specified path. It's a handy analog of {@link
      * org.zkoss.zul.TreeModel#getChild(int[])}.
      *
      * @param path a list of indices of the nodes in the tree. E.g. if you specify 5,4,1, then will be taken the node
      *             with index 5 from the first level of the tree, next will be the 4th child, and then the 1st child of
      *             the 4th child.
      * @return the node at the specified path or {@code null} if there is no such path exists, or returns root element
      *         if zero size array was specified
      */
     @Override
     public TreeNode<E> getChild(int... path) {
         return super.getChild(path);
     }
 
     /**
      * Returns the data of the child located at specified path or {@code null} if no such child was found or the found
      * child contains {@code null} data.
      *
      * @param path a list of indices of the nodes in the tree. E.g. if you specify 5,4,1, then will be taken the node
      *             with index 5 from the first level of the tree, next will be the 4th child, and then the 1st child of
      *             the 4th child.
      * @return the data of the child located at specified path or {@code null} if no node was found there
      */
     public E getChildData(int... path) {
         TreeNode<E> child = getChild(path);
         if (child == null) {
             return null;
         }
         return child.getData();
     }
 
     /**
      * Gets the data of the selected node with specified depth. So if you have a tree with selected path: [1,4]; and the
      * specified depth is 0, then 1 will be returned, if the specified was 1, then 4 will be returned.
      *
      * @param depth the depth of the selected node (because the selected path is an array of nodes including both
      *              selected nodes and all its parents, well except the root)
      * @return the data of the selected node with depth equal to {@code depth}, or {@code null} if nothing is selected
      * @throws IndexOutOfBoundsException if the specified depth is larger than the selected path array. E.g. you have a
      *                                   node selected with path [2,3,0], then if you specify 4, which is larger than
      *                                   the path, then the exception will be raisen.
      */
     public E getSelectedData(@Nonnegative int depth) {
         int[] selectionPath = getSelectionPath();
         if (selectionPath == null) {
             return null;
         }
         if (selectionPath.length <= depth) {
             throw new IndexOutOfBoundsException("There is no selection with level: " + depth +
                     " in the tree. Selected path: " + Arrays.toString(selectionPath));
         }
         return getChildData(Arrays.copyOf(selectionPath, depth + 1));
     }
 
     /**
      * Removes the node that is currently selected in the tree mode. Does nothing if no node is selected at the moment.
      *
      * @return the node that was selected or {@code null} if nothing was
      */
     public TreeNode<E> removeSelected() {
         int[] selectionPath = getSelectionPath();
         if (selectionPath == null) {
             return null;
         }
         return removeChild(selectionPath);
     }
 
     /**
      * Removes the child located at the specified path. Does nothing if the specified path doesn't exist
      *
      * @param path the path of the child to be removed from the tree or empty array if the root should be removed
      * @return the removed child or {@code null} if there wasn't such child
      */
     public TreeNode<E> removeChild(int... path) {
         TreeNode<E> toRemove = getChild(path);
         if (toRemove != null) {
             toRemove.getParent().remove(toRemove);
         }
         return toRemove;
     }
 
     /**
      * Searches the node with specified {@code data} in the node or its children.
      *
      * @param toSearchIn a tree node to find out whether it's the one that contains the specified {@code data} or one of
      *                   its children does
      * @param data       the node data to find the node with it
      * @return the node that contains the specified data or {@code null} if nothing was found
      */
     private TreeNode<E> find(TreeNode<E> toSearchIn, E data) {
         if (toSearchIn.getData().equals(data)) {
             return toSearchIn;
         } else if (toSearchIn.isLeaf()) {
             return null;
         }
         return findOnlyInChildren(toSearchIn, data);
     }
 
     /**
      * Doesn't check whether the specified data is in one of specified node's child, but doesn't check the root node
      * itself. It searches for children recursively.
      *
      * @param toSearchIn a node to take its children and check them whether they contain {@code data}
      * @param data       the data to search for the containing node
      * @return the node that contains data or {@code null} if no such node was found
      */
     private TreeNode<E> findOnlyInChildren(TreeNode<E> toSearchIn, E data) {
         for (TreeNode<E> nextChild : toSearchIn.getChildren()) {
             if (nextChild.getData().equals(data)) {
                 return nextChild;
             }
             TreeNode<E> nextChildSearchResult = find(nextChild, data);
             if (nextChildSearchResult != null) {
                 return nextChildSearchResult;
             }
         }
         return null;
     }
 
     /**
      * Creates a new node with the specified data and puts it into the specified path. The node is going to be not a
      * leaf, but rather the one that can contain other nodes. Adds to the root of the tree if path is not specified.
      *
      * @param data           a data the resulting node will be created with
      * @param parentNodePath a node to find in order to add a child to it, don't specify anything if you want to add a
      *                       node in the root
      * @return just created node
      */
     public ZkTreeNode<E> addExpandableNode(E data, int... parentNodePath) {
         ZkTreeNode<E> node = new ZkTreeNode<E>(data, new ArrayList<TreeNode<E>>());
         getChild(parentNodePath).add(node);
         return node;
     }
 
     /**
      * Drops node before the target. Switches target and all next nodes after dropped.
      *
      * @param node   the node that will be dropped before the target
      * @param target the target, it will be placed with all next nodes after dropped
      */
     public void dropNodeBefore(TreeNode<E> node, TreeNode<E> target) {
         removeChild(getPath(node));
         TreeNode<E> targetParent = target.getParent();
         targetParent.insert(node, targetParent.getIndex(target));
     }
 
     /**
      * Drops node in the target node as child to the end. Removes node from old place.
      *
      * @param node   the node that will be dropped in the target
      * @param target the target in which be dropped node
      */
     public void dropNodeIn(TreeNode<E> node, TreeNode<E> target) {
         removeChild(getPath(node));
         target.add(node);
     }
 
     /**
      * Selects the node.
      *
      * @param node the node that will be selected
      */
     public void setSelectedNode(TreeNode<E> node) {
         clearSelection();
         addToSelection(node);
     }
 }
