 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.framemanager.tree;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.interfaces.FrameInfoListener;
 import com.dmdirc.interfaces.NotificationListener;
 import com.dmdirc.interfaces.SelectionListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.interfaces.UIController;
 import com.dmdirc.ui.interfaces.Window;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Manages open windows in the application in a tree style view.
  */
 public final class TreeFrameManager implements FrameManager,
         Serializable, ConfigChangeListener, SelectionListener,
         NotificationListener, FrameInfoListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 5;
     /** display tree. */
     private Tree tree;
     /** data model. */
     private TreeViewModel model;
     /** node storage, used for adding and deleting nodes correctly. */
     private final Map<FrameContainer<?>, TreeViewNode> nodes;
     /** UI Controller. */
     private SwingController controller;
 
     /** creates a new instance of the TreeFrameManager. */
     public TreeFrameManager() {
         nodes = new HashMap<FrameContainer<?>, TreeViewNode>();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean canPositionVertically() {
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean canPositionHorizontally() {
         return false;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setParent(final JComponent parent) {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final JScrollPane scrollPane = new JScrollPane(tree);
                 scrollPane.setAutoscrolls(true);
                 scrollPane.setHorizontalScrollBarPolicy(
                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
                 parent.setLayout(new MigLayout("ins 0, fill"));
                 parent.add(scrollPane, "grow");
                 parent.setFocusable(false);
 
                 setColours();
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void setController(final UIController controller) {
         if (!(controller instanceof SwingController)) {
             throw new IllegalArgumentException("Controller must be an instance "
                     + "of SwingController");
         }
         this.controller = (SwingController) controller;
 
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 model = new TreeViewModel(new TreeViewNode(null, null));
                 tree = new Tree(TreeFrameManager.this, model, 
                         TreeFrameManager.this.controller.getWindowFactory());
 
                 tree.setCellRenderer(new TreeViewTreeCellRenderer(
                         TreeFrameManager.this));
                 tree.setVisible(true);
 
                 IdentityManager.getGlobalConfig().addChangeListener("treeview",
                         TreeFrameManager.this);
                 IdentityManager.getGlobalConfig().addChangeListener("ui",
                         "backgroundcolour", TreeFrameManager.this);
                 IdentityManager.getGlobalConfig().addChangeListener("ui",
                         "foregroundcolour", TreeFrameManager.this);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowAdded(final Window parent, final Window window) {
         if (parent == null) {
             addWindow(model.getRootNode(), window.getContainer());
         } else {
             addWindow(nodes.get(parent.getContainer()), window.getContainer());
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowDeleted(final Window parent, final Window window) {
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (nodes == null || nodes.get(window.getContainer()) == null) {
                     return;
                 }
                 final DefaultMutableTreeNode node =
                         nodes.get(window.getContainer());
                 if (node.getLevel() == 0) {
                     Logger.appError(ErrorLevel.MEDIUM,
                             "delServer triggered for root node" +
                             node.toString(),
                             new IllegalArgumentException());
                 } else {
                     model.removeNodeFromParent(nodes.get(window.getContainer()));
                 }
                 synchronized (nodes) {
                     nodes.remove(window.getContainer());
                 }
                 window.getContainer().removeSelectionListener(TreeFrameManager.this);
                 window.getContainer().removeFrameInfoListener(TreeFrameManager.this);
                 window.getContainer().removeNotificationListener(TreeFrameManager.this);
             }
         });
     }
 
     /** 
      * Adds a window to the frame container.
      * 
      * @param parent Parent node
      * @param window Window to add
      */
     public void addWindow(final TreeViewNode parent,
             final FrameContainer<?> window) {
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final TreeViewNode node =
                         new TreeViewNode(new NodeLabel(window),
                         window);
                 synchronized (nodes) {
                     nodes.put(window, node);
                 }
                 if (parent == null) {
                     model.insertNodeInto(node, model.getRootNode());
                 } else {
                     model.insertNodeInto(node, parent);
                 }
                 tree.expandPath(new TreePath(node.getPath()).getParentPath());
                 final Rectangle view =
                         tree.getRowBounds(tree.getRowForPath(new TreePath(node.
                         getPath())));
                 if (view != null) {
                     tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(),
                             0, 0));
                 }
                 window.addSelectionListener(TreeFrameManager.this);
                 window.addFrameInfoListener(TreeFrameManager.this);
                 window.addNotificationListener(TreeFrameManager.this);
             }
         });
     }
 
     /**
      * Returns the tree for this frame manager.
      *
      * @return Tree for the manager
      */
     public JTree getTree() {
         return tree;
     }
 
     /**
      * Checks for and sets a rollover node.
      * 
      * @param event event to check 
      */
     protected void checkRollover(final MouseEvent event) {
         NodeLabel node = null;
 
         if (event == null) {
             node = null;
         } else if (tree.getNodeForLocation(event.getX(), event.getY()) !=
                 null) {
             node =
                     tree.getNodeForLocation(event.getX(), event.getY()).getLabel();
         }
 
         synchronized (nodes) {
             for (TreeViewNode treeNode : nodes.values()) {
                 final NodeLabel label = treeNode.getLabel();
                 label.setRollover(node == null ? false : label == node);
             }
         }
         tree.repaint();
     }
 
     /** Sets treeview colours. */
     private void setColours() {
         tree.setBackground(IdentityManager.getGlobalConfig().getOptionColour(
                 "treeview", "backgroundcolour",
                 "ui", "backgroundcolour"));
         tree.setForeground(IdentityManager.getGlobalConfig().getOptionColour(
                 "treeview", "foregroundcolour",
                 "ui", "foregroundcolour"));
 
         tree.repaint();
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         if ("sortrootwindows".equals(key) || "sortchildwindows".equals(key)) {
             redoTreeView();
         } else {
             setColours();
         }
     }
 
     /**
      * Starts the tree from scratch taking into account new sort orders.
      */
     private void redoTreeView() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 ((DefaultTreeModel) tree.getModel()).setRoot(null);
                 ((DefaultTreeModel) tree.getModel()).setRoot(new TreeViewNode(
                         null, null));
 
                 for (FrameContainer<?> window : WindowManager.getRootWindows()) {
                     addWindow(null, window);
                     final Collection<FrameContainer<?>> childWindows = window.getChildren();
                     for (FrameContainer childWindow : childWindows) {
                         addWindow(nodes.get(window), childWindow);
                     }
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void selectionChanged(final FrameContainer window) {
         synchronized (nodes) {
             final Collection<TreeViewNode> collection =
                     new ArrayList<TreeViewNode>(nodes.values());
             for (TreeViewNode treeNode : collection) {
                 final NodeLabel label = treeNode.getLabel();
                 label.selectionChanged(window);
             }
         }
 
         if (window != null) {
             UIUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     final TreeNode[] treePath =
                             ((DefaultTreeModel) tree.getModel()).getPathToRoot(
                             nodes.get(window));
                     if (treePath != null && treePath.length > 0) {
                         final TreePath path = new TreePath(treePath);
                         if (path != null) {
                             tree.setTreePath(path);
                             tree.scrollPathToVisible(path);
                         }
                     }
                 }
             });
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void notificationSet(final FrameContainer window, final Color colour) {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 synchronized (nodes) {
                     final TreeViewNode node = nodes.get(window);
                     if (window != null && node != null) {
                         final NodeLabel label = node.getLabel();
                         if (label != null) {
                             label.notificationSet(window, colour);
                             tree.repaint();
                         }
                     }
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void notificationCleared(final FrameContainer window) {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 synchronized (nodes) {
                     final TreeViewNode node = nodes.get(window);
                     if (window != null && node != null) {
                         final NodeLabel label = node.getLabel();
                         if (label != null) {
                             label.notificationCleared(window);
                             tree.repaint();
                         }
                     }
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void iconChanged(final FrameContainer window, final String icon) {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 synchronized (nodes) {
                     final TreeViewNode node = nodes.get(window);
                     if (node != null) {
                         final NodeLabel label = node.getLabel();
                         if (label != null) {
                             label.iconChanged(window, icon);
                             tree.repaint();
                         }
                     }
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void nameChanged(final FrameContainer window, final String name) {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 synchronized (nodes) {
                     final TreeViewNode node = nodes.get(window);
                     if (node != null) {
                         final NodeLabel label = node.getLabel();
                         if (label != null) {
                             label.nameChanged(window, name);
                             tree.repaint();
                         }
                     }
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void titleChanged(final FrameContainer window, final String title) {
         // Do nothing
     }
 }
