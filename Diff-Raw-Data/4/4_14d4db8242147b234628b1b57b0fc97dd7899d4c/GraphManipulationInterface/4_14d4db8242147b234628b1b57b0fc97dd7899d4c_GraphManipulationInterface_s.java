 /*
  * This class is an interface, that handels the mani-
  * pulations on the canvas, when implemented.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.drawing;
 
 import java.awt.Color;
 import java.util.List;
 
 /**
  * This class is an interface, that handels the mani-
  * pulations on the canvas, when implemented.
  *
  * @param <V> : vertices of the graph
  * @param <E> : edges of the graph
  */
 public interface GraphManipulationInterface<V, E> {
 
     /**
      * Stops the recording of actions to the undo history.
      */
     void beginNotUndoable();
 
     /**
      * Call to start a block of updates.
      */
     void beginUpdate();
 
     /**
      * Returns a boolean denoting whether the calling graph is able to perform
      * a redo-operation.
      *
      * @return if false then there was no undoable action performed earlier
      */
     boolean canRedo();
 
     /**
      * Returns a boolean denoting whether the calling graph is able to perform
      * an undo-operation.
      *
      * @return if true then there is an action that can be undone
      */
     boolean canUndo();
 
     /**
      * Centers the view of the panel on the selected node.
      *
      * @param node : node of the graph
      */
     void centerNode(V node);
 
     /**
      * Centers the view of the panel on the selected nodes.
      *
      * @param nodes : nodes of the graph
      */
     void centerNodes(V[] nodes);
 
     /**
      * Colors a given node in a given color.
      *
      * @param node  : an array of nodes of the graph
      * @param color : a color-parameter
      */
     void colorNode(V[] node, Color color);
 
     /**
      * Starts the recording of actions to the undo history.
      */
     void endNotUndoable();
 
     /**
      * Call to end and execute a block of updates.
      */
     void endUpdate();
 
     /**
      * Returns the current zoomlevel of the graph. 1.0 stands for 100% zoom.
      *
      * @return Value indicating the current zoomlevel.
      */
     double getZoomLevel();
 
     /**
      * Highlights the parents of a node. If used multiple times on the same
      * nodes (without calling {@link #unHiglightAll()}) the depth is increased.
      *
      * @param roots : The nodes where to begin with highlighting
      */
     void highlightChildren(List<V> roots);
 
     /**
      * Highlights the parents of a node. If used multiple times on the same
      * nodes (without calling {@link #unHiglightAll()}) the depth is increased.
      *
      * @param roots : The nodes where to begin with highlighting
      */
     void highlightParents(List<V> roots);
 
     /**
     * Marks the edge between two given nodes by adding a small grey arrow and
     * coloring the edge.
      *
      * @param edges : an array of edges of the graph
      */
     void markEdge(E[] edges);
 
     /**
      * Gives a hierarchical order to the displayed graph.
      */
     void reapplyHierarchicalLayout();
 
     /**
      * Redoes a previously undone action on the graph.
      */
     void redo();
 
     /**
      * Removes all selected and highlighted nodes from the graph.
      */
     void removeHighlightedNodes();
 
     /**
      * Removes the given node from the graph.
      *
      * @param node : The node which will be deleted.
      */
     void removeNode(V node);
 
     /**
      * Alters the attribute name of a given node by replacing it by a given new
      * name. Renaming only effects the JGraphX-graph.
      *
      * @param node    : a node of the graph
      * @param newName : the name the node is given
      */
     void renameNode(V node, String newName);
 
     /**
      * Sets the backgroundcolor of the graph to a given color.
      *
      * @param color : a color-parameter
      */
     void setBackgroundColor(Color color);
 
     /**
      * Sets the fontcolor of all nodes to a given color.
      *
      * @param color : a color-parameter
      */
     void setFontColor(Color color);
 
     /**
      * Sets the highlightcolor of the graph to a given color.
      *
      * @param color : a color-parameter
      */
     void setHighlightColor(Color color);
 
     /**
      * Sets the selectioncolor of the graph to a given color.
      *
      * @param color : a color-parameter
      */
     void setSelectionColor(Color color);
 
     /**
      * Un-Highlights all nodes that are currently highlighted before.
      */
     void unHighlightAll();
 
     /**
      * unHighlights a selected node and all its highlighted parents and
      * children as well as vertices.
      *
      * @param node The node that should be unhighlighted
      */
     void unHighlightNode(V node);
 
     /**
      * Undoes a previously performed action on the graph.
      */
     void undo();
 
     /**
      * Unmarks the edge between two given nodes by removing
      * the small grey arrow and uncoloring the edge.
      *
      * @param edges : an array of edges of the graph
      */
     void unmarkEdge(E[] edges);
 
     /**
      * Zooms the panel. It will magnify the graph, if the
      * graph is too big for the panel only a section of the whole graph will be
      * shown. This method zooms to the center of the panel.
      *
      * @param zoomIn : a boolean to zoom in or out
      */
     void zoom(boolean zoomIn);
 
     /**
      * Zooms the panel to the given factor. It will magnify the graph, if the
      * graph is too big for the panel only a section of the whole graph will be
      * shown. This method zooms to the center of the panel.
      *
      * @param factor : a double that represents the zoom factor
      *               (ranges from 0 to infinite, 1 is 100%)
      */
     void zoomTo(double factor);
 
     /**
      * Zooms the panel, so that the whole graph is visible.
      */
     void zoomToFit();
 }
 
 /* EOF */
