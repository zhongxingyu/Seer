 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import java.util.logging.Logger;
 import java.util.ArrayList;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.Color;
 
 import javax.swing.tree.*;
 import javax.swing.event.*;
 
 import org.wings.tree.*;
 import org.wings.plaf.*;
 import org.wings.io.Device;
 import org.wings.style.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class STree
     extends SComponent
     implements LowLevelEventListener, Scrollable, SSelectionComponent
 {
     private final static Logger logger = Logger.getLogger("org.wings");
     private static final String cgClassID = "TreeCG";
 
     /**
      * Creates and returns a sample TreeModel. Used primarily for beanbuilders.
      * to show something interesting.
      *
      * @return the default TreeModel
      */
     protected static TreeModel getDefaultTreeModel() {
         DefaultMutableTreeNode      root = new DefaultMutableTreeNode("STree");
 	DefaultMutableTreeNode      parent;
 
 	parent = new DefaultMutableTreeNode("colors");
 	root.add(parent);
 	parent.add(new DefaultMutableTreeNode("blue"));
 	parent.add(new DefaultMutableTreeNode("violet"));
 	parent.add(new DefaultMutableTreeNode("red"));
 	parent.add(new DefaultMutableTreeNode("yellow"));
 
 	parent = new DefaultMutableTreeNode("sports");
 	root.add(parent);
 	parent.add(new DefaultMutableTreeNode("basketball"));
 	parent.add(new DefaultMutableTreeNode("soccer"));
 	parent.add(new DefaultMutableTreeNode("football"));
 	parent.add(new DefaultMutableTreeNode("hockey"));
 
 	parent = new DefaultMutableTreeNode("food");
 	root.add(parent);
 	parent.add(new DefaultMutableTreeNode("hot dogs"));
 	parent.add(new DefaultMutableTreeNode("pizza"));
 	parent.add(new DefaultMutableTreeNode("ravioli"));
 	parent.add(new DefaultMutableTreeNode("bananas"));
         return new DefaultTreeModel(root);
     }
 
     /**
      * TODO: documentation
      */
     protected TreeModel model;
 
     /**
      * TODO: documentation
      */
     transient protected TreeModelListener treeModelListener;
 
     /**
      * TODO: documentation
      */
     protected STreeCellRenderer renderer;
 
     /**
      * TODO: documentation
      */
     protected STreeSelectionModel selectionModel;
 
     /**
      * store here all delayed expansion events
      */
     private ArrayList delayedExpansionEvents;
 
     /**
      * store here expansion paths that will be processed after procession the
      * request. 
      */
     protected final ArrayList requestedExpansionPaths = new ArrayList();
 
     /**
      * TODO: documentation
      */
     protected int nodeIndentDepth = 12;
 
     /**
      * TODO: documentation
      */
     protected AbstractLayoutCache treeState = new VariableHeightLayoutCache();
 
     /**
      * Implementation of the  {@link Scrollable} interface.
      */
     protected Rectangle viewport;
 
     /** The style of selected cells */
     protected String selectionStyle;
 
     /** The dynamic attributes of selected cells */
     protected AttributeSet selectionAttributes = new SimpleAttributeSet();
 
     /** used to forward the selection to the selection Listeners of the tree */
     private final TreeSelectionListener forwardSelectionEvent = 
         new TreeSelectionListener() {
             public void valueChanged(TreeSelectionEvent e) {
                 fireTreeSelectionEvent(e);
             }
         };
 
     /**
      * TODO: documentation
      *
      * @param model
      */
     public STree(TreeModel model) {
         super();
         setModel(model);
         setRootVisible(true);
         setSelectionModel(new SDefaultTreeSelectionModel());
     }
 
     /**
      * TODO: documentation
      *
      * @param model
      */
     public STree() {
         this(getDefaultTreeModel());
     }
 
     /**
      * TODO: documentation
      *
      * @param tsl
      */
     public void addTreeSelectionListener(TreeSelectionListener tsl) {
         addEventListener(TreeSelectionListener.class, tsl);
     }
 
     /**
      * TODO: documentation
      *
      * @param tsl
      */
     public void removeTreeSelectionListener(TreeSelectionListener tsl) {
         removeEventListener(TreeSelectionListener.class, tsl);
     }
 
 
 
     protected void fireTreeSelectionEvent(TreeSelectionEvent e) {
         // Guaranteed to return a non-null array
         Object[] listeners = getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length-2; i>=0; i-=2) {
             if (listeners[i] == TreeSelectionListener.class) {
                 ((TreeSelectionListener)listeners[i+1]).valueChanged(e);
             }
         }
         reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * Adds a listener for <code>TreeWillExpand</code> events.
      *
      * @param tel a <code>TreeWillExpandListener</code> that will be notified 
      *            when a tree node will be expanded or collapsed (a "negative
      *            expansion")
      */
     public void addTreeWillExpandListener(TreeWillExpandListener tel) {
         addEventListener(TreeWillExpandListener.class, tel);
     }
 
     /**
      * Removes a listener for <code>TreeWillExpand</code> events.
      *
      * @param tel the <code>TreeWillExpandListener</code> to remove
      */
     public void removeTreeWillExpandListener(TreeWillExpandListener tel) {
         removeEventListener(TreeWillExpandListener.class, tel);
     }
 
 
     /**
      * Notifies all listeners that have registered interest for
      * notification on this event type.  The event instance 
      * is lazily created using the <code>path</code> parameter.
      *
      * @param path the <code>TreePath</code> indicating the node that was
      *		expanded
      * @see EventListenerList
      */
     public void fireTreeWillExpand(TreePath path, boolean expand) 
         throws ExpandVetoException {
 
         // Guaranteed to return a non-null array
         Object[] listeners = getListenerList();
         TreeExpansionEvent e = null;
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length-2; i>=0; i-=2) {
             if (listeners[i]==TreeWillExpandListener.class) {
                 // Lazily create the event:
                 if (e == null)
                     e = new TreeExpansionEvent(this, path);
 
                 if ( expand ) {
                     ((TreeWillExpandListener)listeners[i+1]).
                         treeWillExpand(e);
                 } else {
                     ((TreeWillExpandListener)listeners[i+1]).
                         treeWillCollapse(e);
                 }
             }          
         }
     }   
 
     /**
      * TODO: documentation
      *
      * @param tel
      */
     public void addTreeExpansionListener(TreeExpansionListener tel) {
         addEventListener(TreeExpansionListener.class, tel);
     }
 
     /**
      * TODO: documentation
      *
      * @param tel
      */
     public void removeTreeExpansionListener(TreeExpansionListener tel) {
         removeEventListener(TreeExpansionListener.class, tel);
     }
 
 
     private class DelayedExpansionEvent {
         TreeExpansionEvent expansionEvent;
         boolean expansion;
 
         DelayedExpansionEvent(TreeExpansionEvent e, boolean b) {
             expansionEvent = e;
             expansion = b;
         }
                     
     }
 
     protected void addDelayedExpansionEvent(TreeExpansionEvent e, 
                                             boolean expansion) {
         if ( delayedExpansionEvents==null ) {
             delayedExpansionEvents = new ArrayList();
         }
 
         delayedExpansionEvents.add(new DelayedExpansionEvent(e, expansion));
     }
 
     protected void fireDelayedExpansionEvents() {
         if ( delayedExpansionEvents!=null &&
              !getSelectionModel().getDelayEvents() ) {
             for ( int i=0; i<delayedExpansionEvents.size(); i++ ) {
                 DelayedExpansionEvent e = 
                     (DelayedExpansionEvent)delayedExpansionEvents.get(i);
 
                 fireTreeExpansionEvent(e.expansionEvent, e.expansion);
             }
             delayedExpansionEvents.clear();
         }
     }
 
 
     /**
      * Notify all listeners that have registered interest for
      * notification on this event type.  The event instance
      * is lazily created using the parameters passed into
      * the fire method.
      *
      * @param path the TreePath indicating the node that was expanded
      * @see EventListenerList
      */
     protected void fireTreeExpanded(TreePath path) {
         fireTreeExpansionEvent(new TreeExpansionEvent(this, path), true);
     }
 
     protected void fireTreeExpansionEvent(TreeExpansionEvent e, boolean expansion) {
         if ( getSelectionModel().getDelayEvents() ) {
             addDelayedExpansionEvent(e, expansion);
         } else {
             // Guaranteed to return a non-null array
             Object[] listeners = getListenerList();
             // Process the listeners last to first, notifying
             // those that are interested in this event
             for (int i = listeners.length-2; i>=0; i-=2) {
                 if (listeners[i] == TreeExpansionListener.class) {
                     if ( expansion ) 
                         ((TreeExpansionListener)listeners[i+1]).treeExpanded(e);
                     else
                         ((TreeExpansionListener)listeners[i+1]).treeCollapsed(e);
                 }
             }
         }
     }
 
     /**
      * Notify all listeners that have registered interest for
      * notification on this event type.  The event instance
      * is lazily created using the parameters passed into
      * the fire method.
      *
      * @param path the TreePath indicating the node that was collapsed
      * @see EventListenerList
      */
     public void fireTreeCollapsed(TreePath path) {
         fireTreeExpansionEvent(new TreeExpansionEvent(this, path), false);
     }
 
     protected void processRequestedExpansionPaths() {
         getSelectionModel().setDelayEvents(true);
         
         for ( int i=0; i<requestedExpansionPaths.size(); i++ ) {
             try {
                 TreePath path = (TreePath)requestedExpansionPaths.get(i);
                 togglePathExpansion(path);
             } catch ( ExpandVetoException ex ) {
                 // do not expand...
             }
         }
         requestedExpansionPaths.clear();
         getSelectionModel().setDelayEvents(false);
     }
 
     public void fireIntermediateEvents() {
         processRequestedExpansionPaths();
 
         getSelectionModel().fireDelayedIntermediateEvents();
     }
 
     public void fireFinalEvents() {
         fireDelayedExpansionEvents();
         getSelectionModel().fireDelayedFinalEvents();
     }
 
     /**
      * TODO: documentation
      *
      * @param rootVisible
      */
     public void setRootVisible(boolean rootVisible) {
         treeState.setRootVisible(rootVisible);
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public boolean isRootVisible() {
         return treeState.isRootVisible();
     }
 
 
     /**
      * TODO: documentation
      *
      * @param m
      */
     public void setModel(TreeModel m) {
         if ( model != null && treeModelListener != null )
             model.removeTreeModelListener(treeModelListener);
         model = m;
         treeState.setModel(m);
 
         if ( model != null ) {
             if ( treeModelListener == null )
                 treeModelListener = createTreeModelListener();
 
             if ( treeModelListener != null )
                 model.addTreeModelListener(treeModelListener);
 
             // Mark the root as expanded, if it isn't a leaf.
             if ( !model.isLeaf(model.getRoot()) )
                 treeState.setExpandedState(new TreePath(model.getRoot()), true);
         }
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public TreeModel getModel() {
         return model;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getRowCount() {
         return treeState.getRowCount();
     }
 
     /**
      * TODO: documentation
      *
      * @param row
      * @return
      */
     public TreePath getPathForRow(int row) {
         return treeState.getPathForRow(row);
     }
 
 
    protected int fillPathForAbsoluteRow(int row, Object node, ArrayList path) {
        // and check if it is the 
         if ( row==0 ) {
             return 0; 
         } // end of if ()
 
         for ( int i=0; i<model.getChildCount(node); i++ ) {
            path.add(model.getChild(node, i));
            row = fillPathForAbsoluteRow(row-1, model.getChild(node, i), path);
             if ( row==0 ) {
                 return 0; 
             } // end of if ()
            path.remove(path.size()-1);
         }
         return row;
     }
 
     public TreePath getPathForAbsoluteRow(int row) {
        // fill path in this array
         ArrayList path = new ArrayList(10);

        path.add(model.getRoot());
         fillPathForAbsoluteRow(row, model.getRoot(), path);

         return new TreePath(path.toArray());
     }
 
     /**
      * Sets the tree's selection model. When a null value is specified
      * an empty electionModel is used, which does not allow selections.
      *
      * @param selectionModel the TreeSelectionModel to use, or null to
      *        disable selections
      * @see TreeSelectionModel
      * @beaninfo
      *        bound: true
      *  description: The tree's selection model.
      */
     public void setSelectionModel(STreeSelectionModel selectionModel) {
         if ( this.selectionModel != null )
             this.selectionModel.removeTreeSelectionListener(forwardSelectionEvent);
 
         if ( selectionModel != null )
             selectionModel.addTreeSelectionListener(forwardSelectionEvent);
 
         if ( selectionModel == null )
             this.selectionModel = SDefaultTreeSelectionModel.NO_SELECTION_MODEL;
         else
             this.selectionModel = selectionModel;
     }
 
     /**
      * Returns the model for selections. This should always return a
      * non-null value. If you don't want to allow anything to be selected
      * set the selection model to null, which forces an empty
      * selection model to be used.
      *
      * @param the TreeSelectionModel in use
      * @see #setSelectionModel
      */
     public STreeSelectionModel getSelectionModel() {
         return selectionModel;
     }
 
     /**
      * Returns JTreePath instances representing the path between index0
      * and index1 (including index1).
      *
      * @param index0  an int specifying a display row, where 0 is the
      *                first row in the display
      * @param index0  an int specifying a second display row
      * @return an array of TreePath objects, one for each node between
      *         index0 and index1, inclusive
      */
     protected TreePath[] getPathBetweenRows(int index0, int index1) {
         int newMinIndex = Math.min(index0, index1);
         int newMaxIndex = Math.max(index0, index1);
 
         TreePath[] selection = new TreePath[newMaxIndex - newMinIndex + 1];
 
         for ( int i=newMinIndex; i<=newMaxIndex; i++ ) {
             selection[i - newMinIndex] = getPathForRow(i);
         }
 
         return selection;
     }
 
     /**
      * Selects the node identified by the specified path.  If any
      * component of the path is hidden (under a collapsed node), it is
      * exposed (made viewable).
      *
      * @param path the TreePath specifying the node to select
      */
     public void setSelectionPath(TreePath path) {
         getSelectionModel().setSelectionPath(path);
     }
 
     /**
      * Selects the nodes identified by the specified array of paths.
      * If any component in any of the paths is hidden (under a collapsed
      * node), it is exposed (made viewable).
      *
      * @param paths an array of TreePath objects that specifies the nodes
      *        to select
      */
     public void setSelectionPaths(TreePath[] paths) {
         getSelectionModel().setSelectionPaths(paths);
     }
 
     /**
      * Selects the node at the specified row in the display.
      *
      * @param row  the row to select, where 0 is the first row in
      *             the display
      */
     public void setSelectionRow(int row) {
         int[] rows = { row };
         setSelectionRows(rows);
     }
 
     /**
      * Selects the nodes corresponding to each of the specified rows
      * in the display.
      *
      * @param rows  an array of ints specifying the rows to select,
      *              where 0 indicates the first row in the display
      */
     public void setSelectionRows(int[] rows) {
         if ( rows == null )
             return;
 
         TreePath paths[] = new TreePath[rows.length];
         for ( int i=0; i<rows.length; i++ ) {
             paths[i] = getPathForRow(i);
         }
 
         setSelectionPaths(paths);
     }
 
     /**
      * Adds the node identified by the specified TreePath to the current
      * selection. If any component of the path isn't visible, it is
      * made visible.
      *
      * @param path the TreePath to add
      */
     public void addSelectionPath(TreePath path) {
         getSelectionModel().addSelectionPath(path);
     }
 
     /**
      * Adds each path in the array of paths to the current selection.  If
      * any component of any of the paths isn't visible, it is
      * made visible.
      *
      * @param paths an array of TreePath objects that specifies the nodes
      *              to add
      */
     public void addSelectionPaths(TreePath[] paths) {
         getSelectionModel().addSelectionPaths(paths);
     }
 
     /**
      * Adds the path at the specified row to the current selection.
      *
      * @param row  an int specifying the row of the node to add,
      *             where 0 is the first row in the display
      */
     public void addSelectionRow(int row) {
         int[] rows = { row };
         addSelectionRows(rows);
     }
 
     /**
      * Adds the paths at each of the specified rows to the current selection.
      *
      * @param rows  an array of ints specifying the rows to add,
      *              where 0 indicates the first row in the display
      */
     public void addSelectionRows(int[] rows) {
         if ( rows != null ) {
             int numRows = rows.length;
             TreePath[] paths = new TreePath[numRows];
 
             for ( int counter = 0; counter < numRows; counter++ )
                 paths[counter] = getPathForRow(rows[counter]);
             addSelectionPaths(paths);
         }
     }
 
     /**
      * Returns the last path component in the first node of the current
      * selection.
      *
      * @return the last Object in the first selected node's TreePath,
      *         or null if nothing is selected
      * @see TreePath#getLastPathComponent
      */
     public Object getLastSelectedPathComponent() {
         Object obj = null;
         TreePath selPath = getSelectionModel().getSelectionPath();
         if ( selPath != null ) {
             obj = selPath.getLastPathComponent();
         }
         return obj;
     }
 
     /**
      * Returns the path to the first selected node.
      *
      * @return the TreePath for the first selected node, or null if
      *         nothing is currently selected
      */
     public TreePath getSelectionPath() {
         return getSelectionModel().getSelectionPath();
     }
 
     /**
      * Returns the paths of all selected values.
      *
      * @return an array of TreePath objects indicating the selected
      *         nodes, or null if nothing is currently selected.
      */
     public TreePath[] getSelectionPaths() {
         return getSelectionModel().getSelectionPaths();
     }
 
     /**
      * Returns all of the currently selected rows.
      *
      * @return an array of ints that identifies all currently selected rows
      *         where 0 is the first row in the display
      */
     public int[] getSelectionRows() {
         return getSelectionModel().getSelectionRows();
     }
 
     /**
      * Returns the number of nodes selected.
      *
      * @return the number of nodes selected
      */
     public int getSelectionCount() {
         return selectionModel.getSelectionCount();
     }
 
     /**
      * Gets the first selected row.
      *
      * @return an int designating the first selected row, where 0 is the
      *         first row in the display
      */
     public int getMinSelectionRow() {
         return getSelectionModel().getMinSelectionRow();
     }
 
     /**
      * Gets the last selected row.
      *
      * @return an int designating the last selected row, where 0 is the
      *         first row in the display
      */
     public int getMaxSelectionRow() {
         return getSelectionModel().getMaxSelectionRow();
     }
 
     /**
      * Returns the row index of the last node added to the selection.
      *
      * @return an int giving the row index of the last node added to the
      *         selection, where 0 is the first row in the display
      */
     public int getLeadSelectionRow() {
         return getSelectionModel().getLeadSelectionRow();
     }
 
     /**
      * Returns the path of the last node added to the selection.
      *
      * @return the TreePath of the last node added to the selection.
      */
     public TreePath getLeadSelectionPath() {
         return getSelectionModel().getLeadSelectionPath();
     }
 
     /**
      * Returns true if the item identified by the path is currently selected.
      *
      * @param path a TreePath identifying a node
      * @return true if the node is selected
      */
     public boolean isPathSelected(TreePath path) {
         return getSelectionModel().isPathSelected(path);
     }
 
     /**
      * Returns true if the node identitifed by row is selected.
      *
      * @param row  an int specifying a display row, where 0 is the first
      *             row in the display
      * @return true if the node is selected
      */
     public boolean isRowSelected(int row) {
         return getSelectionModel().isRowSelected(row);
     }
 
     /**
      * Removes the nodes between index0 and index1, inclusive, from the
      * selection.
      *
      * @param index0  an int specifying a display row, where 0 is the
      *                first row in the display
      * @param index0  an int specifying a second display row
      */
     public void removeSelectionInterval(int index0, int index1) {
         TreePath[] paths = getPathBetweenRows(index0, index1);
         this.getSelectionModel().removeSelectionPaths(paths);
     }
 
     /**
      * Removes the node identified by the specified path from the current
      * selection.
      *
      * @param path  the TreePath identifying a node
      */
     public void removeSelectionPath(TreePath path) {
         getSelectionModel().removeSelectionPath(path);
     }
 
     /**
      * Removes the nodes identified by the specified paths from the
      * current selection.
      *
      * @param paths an array of TreePath objects that specifies the nodes
      *              to remove
      */
     public void removeSelectionPaths(TreePath[] paths) {
         getSelectionModel().removeSelectionPaths(paths);
     }
 
     /**
      * Removes the path at the index <code>row</code> from the current
      * selection.
      *
      * @param path  the TreePath identifying the node to remove
      */
     public void removeSelectionRow(int row) {
         int[] rows = { row };
         removeSelectionRows(rows);
     }
 
     public void removeSelectionRows(int[] rows) {
         TreePath[] paths = new TreePath[rows.length];
         for ( int i = 0; i<rows.length; i++ )
             paths[i] = getPathForRow(rows[i]);
         removeSelectionPaths(paths);
     }
 
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getMaximumExpandedDepth() {
         int max = 0;
         for ( int i=0; i<getRowCount(); i++ )
             max = Math.max(max, getPathForRow(i).getPathCount());
         return max;
     }
 
     /**
      * Expand this tree row.
      * If tree is inside a {@link SScrollPane} try to
      * adjust pane, so that as much as possible new 
      * nodes are visible.
      * @param p the TreePath to expand
      */
     public void expandRow(TreePath p) {
         treeState.setExpandedState(p, true);
         /*
           if ( viewport != null )
           {
           int ccount = model.getChildCount( p.getLastPathComponent() );
           if ( ccount + 1 <= viewport.height )
           viewport.y += ccount;
           else
           viewport.y = treeState.getRowForPath( p );
 			
           }
         */
         fireTreeExpanded(p);
         reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * TODO: documentation
      *
      * @param row
      */
     public void expandRow(int row) {
         expandRow(getPathForRow(row));
     }
 
     /**
      * TODO: documentation
      *
      * @param p
      */
     public void collapseRow(TreePath p) {
         treeState.setExpandedState(p, false);
         fireTreeCollapsed(p);
         reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * TODO: documentation
      *
      * @param row
      */
     public void collapseRow(int row) {
         collapseRow(getPathForRow(row));
     }
 
     /**
      * TODO: documentation
      *
      * @param path
      * @return
      */
     public boolean isVisible(TreePath path) {
         if ( path != null ) {
             TreePath parentPath = path.getParentPath();
 
             if ( parentPath != null )
                 return isExpanded(parentPath);
 
             // Root.
             return true;
         }
 
         return false;
     }
 
     /**
      * TODO: documentation
      *
      * @param path
      * @return
      */
     public boolean isExpanded(TreePath path) {
         return treeState.isExpanded(path);
     }
 
     /**
      * TODO: documentation
      *
      * @param path
      */
     protected void togglePathSelection(TreePath path) {
         if ( path != null ) {
             if ( isPathSelected(path) ) {
                 removeSelectionPath(path);
             }
             else {
                 addSelectionPath(path);
             }
         }
     }
 
     /**
      * TODO: documentation
      *
      * @param path
      */
     protected void togglePathExpansion(TreePath path) 
         throws ExpandVetoException
     {
         if ( path != null ) {
             if ( treeState.isExpanded(path) ) {
                 fireTreeWillExpand(path, false);
                 collapseRow(path);
             }
             else {
                 fireTreeWillExpand(path, true);
                 expandRow(path);
             }
         }
     }
 
     /**
      * This is for plafs only! 
      * With this parameter the tree expands the given node 
      * ({@link #processRequest})
      */
     public String getExpansionParameter(int row, boolean absolute) {
         return (absolute ? "j" : "h") + row;
     } 
 
     /**
      * This is for plafs only! 
      * With this parameter the tree selects the given node 
      * ({@link #processLowLevelEvent})
      */
     public String getSelectionParameter(int row, boolean absolute) {
         return (absolute ? "a" : "b") + row;
     } 
 
 
     public void processLowLevelEvent(String action, String[] values) {
         getSelectionModel().setDelayEvents(true);
         for ( int i=0; i<values.length; i++ ) {
             if ( values[i].length()<2 ) continue; // incorrect format
 
             int row = Integer.parseInt(values[i].substring(1));
 
             if ( row<0 ) continue; // row not found...
 
             if ( values[i].charAt(0)=='b' ) {
                 TreePath path = getPathForRow(row);
                 //selection
                 if (path != null) {
                     togglePathSelection(path);
                 }
             } else if ( values[i].charAt(0)=='h' ) {
                 TreePath path = getPathForRow(row);
                 //selection
                 if (path != null) {
                     requestedExpansionPaths.add(path);
                 }
             } else if ( values[i].charAt(0)=='a' ) {
                 TreePath path = getPathForAbsoluteRow(row);
                 //selection
                 if (path != null) {
                     togglePathSelection(path);
                 }
             } else if ( values[i].charAt(0)=='j' ) {
                 TreePath path = getPathForAbsoluteRow(row);
                 //selection
                 if (path != null) {
                     requestedExpansionPaths.add(path);
                 }
             }
         }
         SForm.addArmedComponent(this);
         getSelectionModel().setDelayEvents(false);
     }
 
     /**
      * TODO: documentation
      *
      * @param depth
      */
     public void setNodeIndentDepth(int depth) {
         nodeIndentDepth = depth;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getNodeIndentDepth() {
         return nodeIndentDepth;
     }
 
     /**
      * TODO: documentation
      *
      * @param x
      */
     public void setCellRenderer(STreeCellRenderer x) {
         renderer = x;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public STreeCellRenderer getCellRenderer() {
         return renderer;
     }
 
     /**
      * Creates an instance of TreeModelHandler.
      */
     protected TreeModelListener createTreeModelListener() {
         return new TreeModelHandler();
     }
 
 
     /**
      * Listens to the model and updates the expandedState accordingly
      * when nodes are removed, or changed.
      */
     protected class TreeModelHandler implements TreeModelListener
     {
         /**
          * TODO: documentation
          *
          * @param e
          */
         public void treeNodesChanged(TreeModelEvent e) {
             if ( e == null )
                 return;
             treeState.treeNodesChanged(e);
             reload(ReloadManager.RELOAD_CODE);
         }
 
         /**
          * TODO: documentation
          *
          * @param e
          */
         public void treeNodesInserted(TreeModelEvent e) {
             if ( e == null )
                 return;
             treeState.treeNodesInserted(e);
             reload(ReloadManager.RELOAD_CODE);
         }
 
         /**
          * TODO: documentation
          *
          * @param e
          */
         public void treeStructureChanged(TreeModelEvent e) {
             if ( e == null )
                 return;
             treeState.treeStructureChanged(e);
             reload(ReloadManager.RELOAD_CODE);
         }
 
         /**
          * TODO: documentation
          *
          * @param e
          */
         public void treeNodesRemoved(TreeModelEvent e) {
             if ( e == null )
                 return;
             treeState.treeNodesRemoved(e);
             reload(ReloadManager.RELOAD_CODE);
         }
     }
 
     /**
      * TODO: documentation
      */
     protected class NodeState {
         boolean expanded;
     }
 
 
     public void setParent(SContainer p) {
         super.setParent(p);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParent(p);
     }
 
     protected void setParentFrame(SFrame f) {
         super.setParentFrame(f);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParentFrame(f);
     }
 
 
     // do not initalize with null!
     private SCellRendererPane cellRendererPane = new SCellRendererPane();
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public SCellRendererPane getCellRendererPane() {
         return cellRendererPane;
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     /**
      * Returns the maximum size of this tree.
      *
      * @return maximum size
      */
     public Rectangle getScrollableViewportSize() {
         return new Rectangle(0,0, 1, getRowCount());
     }
 
     /*
      * Setzt den anzuzeigenden Teil
      */
     /**
      * TODO: documentation
      *
      * @param d
      */
     public void setViewportSize(Rectangle d) {
         Rectangle oldViewport = viewport; 
         viewport = d;
         if ((viewport == null && oldViewport != null) ||
             (viewport != null && !viewport.equals(oldViewport)))
             reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Rectangle getViewportSize() {
         return viewport;
     }
 
     public Dimension getPreferredExtent() {
         return null;
     }
 
     /**
      * @param style the style of selected cells
      */
     public void setSelectionStyle(String selectionStyle) {
         this.selectionStyle = selectionStyle;
     }
 
     /**
      * @return the style of selected cells.
      */
     public String getSelectionStyle() { return selectionStyle; }
 
 
     /**
      * Set the selectionAttributes.
      * @param selectionAttributes the selectionAttributes
      */
     public void setSelectionAttributes(AttributeSet selectionAttributes) {
         if (selectionAttributes == null)
             throw new IllegalArgumentException("null not allowed");
 
         if (!this.selectionAttributes.equals(selectionAttributes)) {
             this.selectionAttributes = selectionAttributes;
             reload(ReloadManager.RELOAD_STYLE);
         }
     }
 
     /**
      * @return the current selectionAttributes
      */
     public AttributeSet getSelectionAttributes() {
         return selectionAttributes;
     }
 
     /**
      * Set the background color.
      * @param c the new background color
      */
     public void setSelectionBackground(Color color) {
         boolean changed = 
             selectionAttributes.putAll(CSSStyleSheet.getAttributes(color, Style.BACKGROUND_COLOR));
         if (changed)
             reload(ReloadManager.RELOAD_STYLE);
     }
 
     /**
      * Return the background color.
      * @return the background color
      */
     public Color getSelectionBackground() {
         return CSSStyleSheet.getBackground(selectionAttributes);
     }
 
     /**
      * Set the foreground color.
      * @param color the foreground color of selected cells
      */
     public void setSelectionForeground(Color color) {
         boolean changed = 
             selectionAttributes.putAll(CSSStyleSheet.getAttributes(color, Style.COLOR));
         if (changed)
             reload(ReloadManager.RELOAD_STYLE);
     }
 
     /**
      * Return the foreground color.
      * @return the foreground color
      */
     public Color getSelectionForeground() {
         return CSSStyleSheet.getForeground(selectionAttributes);
     }
 
     public void setCG(TreeCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
