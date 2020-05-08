 /*
  * This class implements the GraphManipulationInterface. It handles
  * manipulations that are done on the JGraphX-graph that
  * is viewed on the panel.
  * Manipulation will be done by the user.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.drawing;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.model.mxICell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.swing.mxGraphOutline;
 import com.mxgraph.util.mxConstants;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxUndoManager;
 import com.mxgraph.util.mxUndoableEdit;
 import com.mxgraph.util.mxUtils;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.view.mxGraphView;
 import org.jgrapht.Graph;
 import teo.isgci.util.Latex2Html;
 
 import java.awt.Color;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * This class implements the GraphManipulationInterface. It handles
  * manipulations that are done on the JGraphX-graph that is viewed on the panel.
  * Manipulation will be done by the user.
  *
  * @param <V> : vertices of the graph
  * @param <E> : edges of the graph
  */
 class GraphManipulation<V, E> implements GraphManipulationInterface<V, E> {
 
     /**
      * How far the user can zoom in.
      */
     private static final double MAXZOOMLEVEL = 4;
     /**
      * Defines the color that should be used for highlighting.
      */
     private static final Color[] HIGHLIGHTCOLORS = new Color[]{new Color(0x00,
             0xFF, 0x00), new Color(0x44,
             0x44, 0xFF), new Color(0xFF,
             0xAA, 0x00), new Color(0xFF,
             0x00, 0x00)};
     /**
      * Defines the thickness for highlighting.
      */
     private static final String HIGHLIGHTTHICKNESS = "2";
     protected mxIEventListener undoHandler = new mxIEventListener() {
         public void invoke(Object source, mxEventObject evt) {
             if (recordUndoableActions) {
                 undoManager.undoableEditHappened((mxUndoableEdit) evt
                         .getProperty("edit"));
             }
         }
     };
     /**
      * Defines whether or not the undoHandler should record actions.
      */
     private boolean recordUndoableActions = true;
     /**
      * GraphComponent is the panel the graph is drawn in.
      */
     private mxGraphComponent graphComponent;
     /**
      * The corresponding minimap, which can be hidden depending on zoom.
      */
     private mxGraphOutline graphOutline;
     /**
      * Manages the undo-operations on the calling graph.
      */
     private mxUndoManager undoManager;
     /**
      * Currently highlighted cells with their previous color.
      */
     private HashMap<mxICell, Color> markedCellsColor;
     /**
      * Currently highlighted cells with their previous thickness.
      */
     private HashMap<mxICell, String> markedCellsThickness;
 
     /**
      * Constructor of the class. Creates an instance of the GraphManipulation
      * class that operates on a given graphComponent.
      *
      * @param pGraphComponent : a JGRaphX graphComponent, shown on the panel
      * @param pGraphOutline   : the corresponding graphoutline
      */
     public GraphManipulation(mxGraphComponent pGraphComponent,
                              mxGraphOutline pGraphOutline) {
         graphComponent = pGraphComponent;
         graphOutline = pGraphOutline;
 
         // initiation of undoManager variable
         undoManager = new mxUndoManager();
 
         // notify undoManager about edits
         graphComponent.getGraph().getModel()
                 .addListener(mxEvent.UNDO, undoHandler);
         graphComponent.getGraph().getView()
                 .addListener(mxEvent.UNDO, undoHandler);
 
         // notify this component about size changes, to check if the minimap
         // has to be displayed/hidden
         graphComponent.addComponentListener(new ComponentListener() {
 
             @Override
             public void componentShown(ComponentEvent e) {
             }
 
             @Override
             public void componentResized(ComponentEvent e) {
                 setMinimapVisibility();
             }
 
             @Override
             public void componentMoved(ComponentEvent e) {
             }
 
             @Override
             public void componentHidden(ComponentEvent e) {
             }
         });
 
         markedCellsColor = new HashMap<mxICell, Color>();
         markedCellsThickness = new HashMap<mxICell, String>();
     }
 
     /**
      * Returns the current graph adapter.
      *
      * @return The current graph adapter
      */
     @SuppressWarnings("unchecked")
     private JGraphXAdapter<V, E> getGraphAdapter() {
         return (JGraphXAdapter<V, E>) graphComponent.getGraph();
     }
 
     /**
      * Returns the cell associated with the given node.
      *
      * @param node
      * @return
      */
     private mxICell getCellFromNode(V node) {
         return getGraphAdapter().getVertexToCellMap().get(node);
     }
 
     /**
      * Returns the cell associated with the given edge.
      *
      * @param edge
      * @return
      */
     private mxICell getCellFromEdge(E edge) {
         return getGraphAdapter().getEdgeToCellMap().get(edge);
     }
 
     /**
      * Returns the cells associated with the given nodes.
      *
      * @param nodes
      * @return
      */
     private mxICell[] getCellsFromNodes(V[] nodes) {
         mxICell[] cells = new mxICell[nodes.length];
         for (int i = 0; i < nodes.length; i++) {
             cells[i] = getCellFromNode(nodes[i]);
         }
         return cells;
     }
 
     /**
      * Returns the cells associated with the given edges.
      *
      * @param edges
      * @return
      */
     private mxICell[] getCellsFromEdges(E[] edges) {
         mxICell[] cells = new mxICell[edges.length];
         for (int i = 0; i < edges.length; i++) {
             cells[i] = getCellFromEdge(edges[i]);
         }
         return cells;
     }
 
     @Override
     public boolean canRedo() {
         return undoManager.canRedo();
     }
 
     @Override
     public boolean canUndo() {
         return undoManager.canUndo();
     }
 
     @Override
     public void centerNode(V node) {
         graphComponent.scrollCellToVisible(getCellFromNode(node), true);
     }
 
     @Override
     public void colorNode(V[] nodes, Color color) {
 
         mxGraph graph = graphComponent.getGraph();
 
         beginUpdate();
         try {
             graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                     mxUtils.hexString(color), getCellsFromNodes(nodes));
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void setFontColor(Color color) {
 
         mxGraph graph = graphComponent.getGraph();
 
         beginUpdate();
         try {
 
             graph.setCellStyles(mxConstants.STYLE_FONTCOLOR,
                     mxUtils.hexString(color),
                     graph.getChildVertices(graph.getDefaultParent()));
 
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void setBackgroundColor(Color color) {
         beginUpdate();
         try {
             graphComponent.getViewport().setBackground(color);
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void markEdge(E[] edges) {
 
         mxGraph graph = graphComponent.getGraph();
 
         beginUpdate();
         try {
             graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.hexString(Color.black), getCellsFromEdges(edges));
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void unmarkEdge(E[] edges) {
         mxGraph graph = graphComponent.getGraph();
 
         beginUpdate();
         try {
             graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.hexString(new Color(100, 130, 185)),
                     getCellsFromEdges(edges));
 
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void reapplyHierarchicalLayout() {
         mxGraph graph = graphComponent.getGraph();
 
         beginUpdate();
         try {
             mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
             layout.execute(graph.getDefaultParent());
 
         } finally {
             endUpdate();
         }
 
         setMinimapVisibility();
     }
 
     @Override
     public void redo() {
         undoManager.redo();
 
         setMinimapVisibility();
     }
 
     @Override
     public void removeNode(V node) {
         mxGraph graph = graphComponent.getGraph();
 
         Object[] cells = new Object[]{getCellFromNode(node)};
 
         // Adds all edges connected to the node
         cells = graph.addAllEdges(cells);
 
         beginUpdate();
         try {
             // Deletes every cell
             for (Object object : cells) {
                 graph.getModel().remove(object);
             }
         } finally {
             endUpdate();
         }
 
         setMinimapVisibility();
     }
 
     @Override
     public void highlightNode(V node, boolean hightlightNeighbors) {
 
         ArrayList<mxICell> cells = new ArrayList<mxICell>();
         ArrayList<mxICell> neighborCells = new ArrayList<mxICell>();
         mxICell startCell = getCellFromNode(node);
 
         cells.add(startCell);
 
         if (!markedCellsColor.containsKey(startCell)) {
             markedCellsThickness.put(startCell,
                     mxUtils.getString(getGraphAdapter()
                     .getCellStyle(startCell), mxConstants.STYLE_STROKEWIDTH));
 
             markedCellsColor.put(startCell, mxUtils.getColor(getGraphAdapter()
                     .getCellStyle(startCell), mxConstants.STYLE_STROKECOLOR));
         }
 
         if (!hightlightNeighbors) {
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(HIGHLIGHTCOLORS[0]),
                     cells.toArray());
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                     HIGHLIGHTTHICKNESS, cells.toArray());
 
         } else {
 
             beginUpdate();
             try {
                 for (int i = 0; i < HIGHLIGHTCOLORS.length; i++) {
 
                     for (mxICell cell : cells) {
                         if(cell.isEdge())
                             continue;
                         for (int j = 0; j < cell.getEdgeCount(); j++) {
                             mxCell edge = (mxCell) cell.getEdgeAt(j);
 
                             if(markedCellsColor.containsKey(edge))
                             {
                                 continue;
                             }
 
                             markedCellsColor.put(edge, mxUtils.getColor(getGraphAdapter()
                                     .getCellStyle(edge), mxConstants.STYLE_STROKECOLOR));
 
                             markedCellsThickness.put(edge, mxUtils.getString(
                                     getGraphAdapter().getCellStyle(edge),
                                     mxConstants.STYLE_STROKEWIDTH));
 
                             neighborCells.add(edge);
 
                             mxICell source = edge.getSource();
                             mxICell target = edge.getTarget();
 
                             if (!markedCellsColor.containsKey(source)) {
                                 markedCellsThickness.put(source, mxUtils.getString(
                                         getGraphAdapter().getCellStyle(source),
                                         mxConstants.STYLE_STROKEWIDTH));
 
                                 markedCellsColor.put(source, mxUtils.getColor(
                                         getGraphAdapter().getCellStyle(source),
                                         mxConstants.STYLE_STROKECOLOR));
                                 neighborCells.add(source);
                             }
 
                             if (!markedCellsColor.containsKey(target)) {
 
                                 markedCellsThickness.put(target, mxUtils.getString(
                                         getGraphAdapter().getCellStyle(target),
                                         mxConstants.STYLE_STROKEWIDTH));
 
                                 markedCellsColor.put(target, mxUtils.getColor(
                                         getGraphAdapter().getCellStyle(target),
                                         mxConstants.STYLE_STROKECOLOR));
                                 neighborCells.add(target);
                             }
                         }
                     }
 
                     if (i > 0)
                         cells.clear();
 
                     cells.addAll(neighborCells);
 
                     getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                             mxUtils.getHexColorString(HIGHLIGHTCOLORS[i]),
                             cells.toArray());
                     getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                             HIGHLIGHTTHICKNESS, cells.toArray());
 
                     if (i == 0)
                         cells.remove(startCell);
 
                     neighborCells.clear();
                 }
             } finally {
                 endUpdate();
             }
         }
     }
 
     /**
      * Starts the recording of actions to the undo history.
      */
     @Override
     public void endNotUndoable() {
         recordUndoableActions = true;
     }
 
     /**
      * Stops the recording of actions to the undo history.
      */
     @Override
     public void beginNotUndoable() {
         recordUndoableActions = false;
     }
 
     @Override
     public void unHiglightAll() {
         beginUpdate();
         try {
             for (mxICell cell : markedCellsColor.keySet()) {
 
                 getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                         mxUtils.getHexColorString(markedCellsColor.get(cell)),
                         new Object[]{cell});
 
                 getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                         markedCellsThickness.get(cell), new Object[]{cell});
             }
         } finally {
             endUpdate();
             markedCellsColor.clear();
             markedCellsThickness.clear();
         }
     }
 
     /**
      * Call to start a block of updates.
      */
     @Override
     public void beginUpdate() {
         getGraphAdapter().getModel().beginUpdate();
     }
 
     /**
      * Call to end and execute a block of updates.
      */
     @Override
     public void endUpdate() {
         getGraphAdapter().getModel().endUpdate();
     }
 
     @Override
     public void renameNode(V node, String newName) {
         mxGraph graph = graphComponent.getGraph();
 
         newName = Latex2Html.getInstance().html(newName);
 
         mxICell cell = getCellFromNode(node);
 
         graph.getModel().setValue(cell, newName);
         graph.updateCellSize(cell);
 
         setMinimapVisibility();
     }
 
     @Override
     public void resetLayout() {
         Graph<V, E> graphT = getGraphAdapter().getGraph();
 
         JGraphXAdapter<V, E> newGraphAdapter = new JGraphXAdapter<V, E>(graphT);
         setMinimapVisibility();
     }
 
     @Override
     public void undo() {
         undoManager.undo();
         setMinimapVisibility();
     }
 
     @Override
     public double getZoomLevel() {
         return graphComponent.getGraph().getView().getScale();
     }
 
     @Override
     public void zoomTo(double factor) {
         factor = Math.min(MAXZOOMLEVEL, factor);
 
         graphComponent.zoomTo(factor, true);
 
         setMinimapVisibility();
     }
 
     @Override
     public void zoom(boolean zoomIn) {
         graphComponent.setCenterZoom(true);
 
         if (zoomIn) {
             if (getZoomLevel() < MAXZOOMLEVEL) {
                 graphComponent.zoomIn();
             }
         } else {
             graphComponent.zoomOut();
         }
 
         setMinimapVisibility();
     }
 
     @Override
     public void zoomToFit() {
         mxGraphView view = graphComponent.getGraph().getView();
 
         int compLen = graphComponent.getWidth();
         int viewLen = (int) view.getGraphBounds().getWidth();
 
         view.setScale((double) compLen / viewLen * view.getScale());
         setMinimapVisibility();
     }
 
     /**
      * Checks if the minimap should be hidden and hides it if necessary.
      */
 
     public void setMinimapVisibility() {
         mxGraphView view = graphComponent.getGraph().getView();
 
         int compLen = graphComponent.getWidth();
         int viewLen = (int) view.getGraphBounds().getWidth();
 
         double scale = ((double) compLen / viewLen * view.getScale());
 
         if (getZoomLevel() > scale) {
             graphOutline.setVisible(true);
         } else {
             graphOutline.setVisible(false);
         }
     }
 }
 
 /* EOF */
