 /*******************************************************************************
  *  Copyright (C) 2013 Justin Stoecker
  *  The MIT License. See LICENSE in project root.
  *******************************************************************************/
 package roboviz.draw.drawable;
 
 import java.awt.EventQueue;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  * Contains drawables in a tree structure. A HashMap is used to quickly check if newly added
  * drawables can be placed in an existing node; if not, nodes are created that reflect the desired
  * drawable's node name.
  * 
  * @author justin
  */
 public class DrawableGraph implements TreeModel {
 
   private final List<TreeModelListener> listeners = new CopyOnWriteArrayList<TreeModelListener>();
   private final String                  rootName;
   DrawableNode                          root;
   HashMap<String, DrawableNode>         nodeMap   = new HashMap<String, DrawableNode>();
 
   public DrawableGraph(String rootName) {
     this.rootName = rootName;
     root = new DrawableNode(rootName, null);
   }
 
   @Override
   public DrawableNode getChild(Object parent, int index) {
     DrawableNode parentNode = (DrawableNode) parent;
     return parentNode.children.get(index);
   }
 
   @Override
   public int getChildCount(Object parent) {
     DrawableNode parentNode = (DrawableNode) parent;
     return (parentNode.children == null) ? 0 : parentNode.children.size();
   }
 
   @Override
   public int getIndexOfChild(Object parent, Object child) {
     if (parent == null || child == null)
       return -1;
     DrawableNode parentNode = (DrawableNode) parent;
     if (parentNode.children == null)
       return -1;
     return parentNode.children.indexOf(child);
   }
 
   @Override
   public synchronized DrawableNode getRoot() {
     return root;
   }
 
   @Override
   public boolean isLeaf(Object node) {
     return ((DrawableNode) node).children == null;
   }
 
   @Override
   public void valueForPathChanged(TreePath path, Object newValue) {
   }
 
   @Override
   public void addTreeModelListener(TreeModelListener listener) {
     if (listener != null && !listeners.contains(listener)) {
       listeners.add(listener);
     }
   }
 
   @Override
   public void removeTreeModelListener(TreeModelListener listener) {
     listeners.remove(listener);
   }
 
   public void fireTreeNodesChanged(TreeModelEvent e) {
     for (TreeModelListener l : listeners)
       l.treeNodesChanged(e);
   }
 
   public void fireTreeNodesInserted(TreeModelEvent e) {
     for (TreeModelListener l : listeners)
       l.treeNodesInserted(e);
   }
 
   public void fireTreeNodesRemoved(TreeModelEvent e) {
     for (TreeModelListener l : listeners)
       l.treeNodesRemoved(e);
   }
 
   public void fireTreeStructureChanged(TreeModelEvent e) {
     for (TreeModelListener l : listeners)
       l.treeStructureChanged(e);
   }
 
   public synchronized void add(Drawable drawable) {
     DrawableNode node = nodeMap.get(drawable.id);
     if (node != null) {
       node.put(drawable);
     } else {
       String[] nodePath = drawable.id.split("\\.");
       add(root, nodePath, 0, drawable, new TreePath(root), 0);
     }
   }
 
   private void add(DrawableNode parent, String[] pathNames, int depth, Drawable drawable,
       TreePath path, int adds) {
     DrawableNode node = new DrawableNode(pathNames[depth], parent);
 
     int nodeIndex = (parent.children == null) ? -1 : parent.children.indexOf(node);
     if (nodeIndex < 0) {
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < depth; i++)
         sb.append(pathNames[i] + ".");
       sb.append(pathNames[depth]);
       add(parent, node, sb.toString());
       adds++;
     } else {
       node = parent.children.get(nodeIndex);
     }
 
     if (depth == pathNames.length - 1) {
       node.put(drawable);
 
       if (adds > 1) {
         for (int i = 0; i < adds - 1; i++)
           path = path.getParentPath();
         // the fireTree* methods are causing problems with deadlocks
         treeStructureChanged(path);
       } else {
         treeNodesInserted(node, parent, path);
       }
     } else {
       add(node, pathNames, depth + 1, drawable, path.pathByAddingChild(node), adds);
     }
   }
 
   private void treeStructureChanged(final TreePath path) {
     EventQueue.invokeLater(new Runnable() {
       public void run() {
         fireTreeStructureChanged(new TreeModelEvent(this, path));
       }
     });
   }
 
   private void treeNodesInserted(final DrawableNode node, final DrawableNode parent,
       final TreePath path) {
     EventQueue.invokeLater(new Runnable() {
       public void run() {
         int[] childIndices = new int[] { parent.children.indexOf(node) };
         Object[] children = parent.children.toArray();
         fireTreeNodesInserted(new TreeModelEvent(this, path, childIndices, children));
       }
     });
   }
 
   private void add(DrawableNode parent, DrawableNode child, String name) {
     if (parent.children == null)
       parent.children = new ArrayList<DrawableNode>();
     parent.children.add(child);
     nodeMap.put(name, child);
 
     Collections.sort(parent.children);
   }
 
   public synchronized void clear() {
     root = new DrawableNode(rootName, null);
     nodeMap.clear();
   }
 
   public synchronized void swapBuffers(String name) {
     if (name == null || name.isEmpty()) {
       swapBuffers(root);
     } else {
      if (name.endsWith("."))
        name = name.substring(0, name.length()-1);
       swapBuffers(nodeMap.get(name));
     }
   }
 
   private void swapBuffers(DrawableNode node) {
    if (node == null) {
       return;
    }

     node.swapBuffers();
     if (node.children != null) {
       for (DrawableNode child : node.children) {
         swapBuffers(child);
       }
     }
   }
 }
