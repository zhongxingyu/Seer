 /*
  * This file is part of SmartStreets.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ru.jcorp.smartstreets.gui.map.bundle;
 
 import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
 import org.apache.commons.collections15.Factory;
 import ru.jcorp.smartstreets.gui.map.MapEditor;
 import ru.jcorp.smartstreets.map.RoadSign;
 import ru.jcorp.smartstreets.map.SmartMapLine;
 
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 /**
  * <p>$Id$</p>
  *
  * @author Artamonov Yuriy
  */
 public class GraphEditMousePlugin extends EditingGraphMousePlugin<GraphNode, GraphLink> {
 
     private MapEditor editor;
 
     public GraphEditMousePlugin(MapEditor editor,
                                 Factory<GraphNode> vertexFactory,
                                 Factory<GraphLink> edgeFactory) {
         super(vertexFactory, edgeFactory);
         this.editor = editor;
     }
 
     @SuppressWarnings("unchecked")
     public void mouseReleased(MouseEvent e) {
         if (checkModifiers(e)) {
             editor.clearPath();
 
             final VisualizationViewer<GraphNode, GraphLink> vv =
                     (VisualizationViewer<GraphNode, GraphLink>) e.getSource();
             final Point2D p = e.getPoint();
             Layout<GraphNode, GraphLink> layout = vv.getModel().getGraphLayout();
             GraphElementAccessor<GraphNode, GraphLink> pickSupport = vv.getPickSupport();
             if (pickSupport != null) {
                 final GraphNode vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
                 if (vertex != null && startVertex != null) {
                     Graph<GraphNode, GraphLink> graph = vv.getGraphLayout().getGraph();
 
                     GraphLink coGraphLink;
                     if (edgeFactory instanceof LinkedLinesFactory) {
                         coGraphLink = ((LinkedLinesFactory) edgeFactory).createLinkedLine(startVertex, vertex);
                     } else {
                         coGraphLink = edgeFactory.create();
                     }
 
                     SmartMapLine line = coGraphLink.getMapLine();
                     if (line.getCodirectionalSign() == null)
                         line.setCodirectionalSign(new RoadSign());
                     if (line.getOppositelySign() == null)
                         line.setOppositelySign(new RoadSign());
 
                     GraphLink opGraphLink = new GraphLink(coGraphLink.getMapLine(), coGraphLink.getMapLine().getOppositelySign());
 
                     coGraphLink.setNeighbor(opGraphLink);
                     opGraphLink.setNeighbor(coGraphLink);
 
                    graph.addEdge(coGraphLink, vertex, startVertex, edgeIsDirected);
                    graph.addEdge(opGraphLink, startVertex, vertex, edgeIsDirected);
                     vv.repaint();
                 }
             }
             startVertex = null;
             down = null;
             edgeIsDirected = EdgeType.UNDIRECTED;
             vv.removePostRenderPaintable(edgePaintable);
             vv.removePostRenderPaintable(arrowPaintable);
         }
     }
 }
