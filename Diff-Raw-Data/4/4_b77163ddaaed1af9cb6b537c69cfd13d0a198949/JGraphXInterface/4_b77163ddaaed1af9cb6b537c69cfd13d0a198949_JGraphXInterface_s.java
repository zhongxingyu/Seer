 /*
  * Replace this line with a (multi-line) description of this file...
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 
 package teo.isgci.drawing;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.MouseEvent;
 
 import com.mxgraph.layout.mxIGraphLayout;
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxCellState;
 
 import org.jgrapht.Graph;
 import org.jgrapht.ListenableGraph;
 import org.jgrapht.graph.DefaultEdge;
 
 import teo.isgci.gui.LatexLabel;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 /**
  * Dumbed down version of the original, WIP interface
  * TODO: replace this with the final one
  */
 class JGraphXInterface<V, E> implements DrawingLibraryInterface<V, E> {
 
     private mxGraphComponent graphComponent;
 
     private GraphManipulation graphManipulation;
 
     private GraphEvent graphEvent;
 
     private JGraphXAdapter<V, E> graphAdapter;
 
     /**
      * The constructor for JGraphXInterface.
      *
      * @param g JGraphT graph to draw
      */
     public JGraphXInterface(Graph<V, E> g) {
 
         // Convert to JGraphT-Graph
         graphAdapter = createNewAdapter(g);
 
         // Create the mxGraphComponent used to draw the graph
         // Also overrides the default behavior of JGraphX panning
         // implementation so the users are not required to hold down shift
         // and ctrl
         // and draws the label via latexlabel
         graphComponent = new mxGraphComponent(graphAdapter) {
             @Override
             public boolean isPanningEvent(MouseEvent event) {
                 if (event == null) {
                     return false;
                 }
 
                 mxCell cell = (mxCell) getCellAt(event.getX(),
                         event.getY());
 
                if (!getBounds().contains(event.getPoint())) {
                    return false;
                }

                 return cell == null || cell.isEdge();
             }
             
             @Override
             public Component[] createComponents(mxCellState state) {
                 if (getGraph().getModel().isVertex(state.getCell())) {
                     String label = state.getLabel();
                     // get rid of these nasty [] around all labels 
                     label = label.replace("[", "");
                     label = label.replace("]", "");
                     
                     LatexLabel latexlabel = new LatexLabel(label);
                     // make background transparent
                     latexlabel.setBackground(new Color(0, 0, 0, 0));
                     
                     return new Component[] { latexlabel };
                 }
                 return null;
             };
         };
 
         graphManipulation = new GraphManipulation(graphComponent, graphAdapter);
         graphEvent = new GraphEvent(graphComponent);
 
         graphComponent.setWheelScrollingEnabled(false);
         graphEvent.registerMouseAdapter(
                 new InternalMouseAdapter(graphComponent));
 
         graphManipulation.reapplyHierarchicalLayout();
 
         applyCustomGraphSettings();
 
     }
 
     /**
      * Creates a new JGraphXAdapter form the given Graph with edge selection
      * and movement disabled.
      *
      * @param g JGraphT graph
      * @return JGraphXAdapter
      */
     private JGraphXAdapter<V, E> createNewAdapter(Graph<V, E> g) {
         return new JGraphXAdapter<V, E>(g) {
             @Override
             public boolean isCellSelectable(Object cell) {
                 if (model.isEdge(cell)) {
                     return false;
                 }
                 return super.isCellSelectable(cell);
             }
 
             @Override
             public boolean isCellMovable(Object cell) {
                 if (model.isEdge(cell)) {
                     return false;
                 }
                 return super.isCellMovable(cell);
             }
         };
     }
 
 
     /**
      * Applies some custom settings to the graph.
      */
     private void applyCustomGraphSettings() {
 
         graphAdapter.setKeepEdgesInBackground(true);
         graphAdapter.setAllowDanglingEdges(false);
         graphAdapter.setAllowLoops(false);
         graphAdapter.setCellsDeletable(false);
         graphAdapter.setCellsDisconnectable(false);
         graphAdapter.setCellsBendable(false);
         graphAdapter.setCellsCloneable(false);
         graphAdapter.setCellsEditable(false);
         graphAdapter.setCellsResizable(false);
         graphAdapter.setVertexLabelsMovable(false);
         graphAdapter.setConnectableEdges(false);
         graphAdapter.setAutoSizeCells(true);
     }
 
     /**
      * Exports the current graph.
      * 
      * @param format
      *            The actual format (.ps, .svg, .graphml)
      * @param path
      *            The path where the graph will be exported to
      */
     @Override
     public final void export(final String format, final String path) {
         if (format == "eps") {
             exportEPS(path);
         } else if (format == "svg") {
             exportSVG(path);
         } else if (format == "graphml") {
             exportGraphML(path);
         }
     }
 
     /**
      * Exports the canvas as an eps under the given path, by converting an
      * existing .svg representation of it.
      * 
      * @param path
      *            The path where the .eps file will be saved to
      */
     private void exportEPS(final String path) {
 
     }
 
     /**
      * Exports the canvas as an svg under the given path.
      * 
      * @param path
      *            The path where the .svg file will be saved to
      */
     private void exportSVG(final String path) {
  
     }
 
     /**
      * Exports the canvas as an GraphML under the given path.
      * 
      * @param path
      *            The path where the .graphml file will be saved to
      */
     private void exportGraphML(final String path) {
   
     }
 
     /**
      * Returns an Array of all currently implemented export formats.
      * 
      * @return An array of String with the formats
      */
     @Override
     public final String[] getAvailableExportFormats() {
         return new String[] { "ps", "svg", "graphml" };
     }
 
     @Override
     public final GraphEventInterface getGraphEventInterface() {
         return graphEvent;
     }
 
     @Override
     public final GraphManipulationInterface getGraphManipulationInterface() {
         return graphManipulation;
     }
 
     @Override
     public final JComponent getPanel() {
         return graphComponent;
     }
 
     @Override
     public final void setGraph(final Graph<V, E> g) {
 
         graphAdapter = new JGraphXAdapter<V, E>(g);
         graphComponent.setGraph(graphAdapter);
 
         applyCustomGraphSettings();
 
         graphManipulation.reapplyHierarchicalLayout();
     }
 
     @Override
     public Graph<V, E> getGraph() {
         return graphAdapter.getGraph();
     }
 }
