 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 //----------------------------------------------------------------------------
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Logger;
 import java.awt.*;
 
 import giny.model.Node;
 import giny.model.Edge;
 import giny.view.GraphView;
 import giny.view.NodeView;
 import giny.view.EdgeView;
 import giny.view.Label;
 
 import cytoscape.data.CyNetwork;
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.view.NetworkView;
 import cytoscape.visual.ui.VizMapUI;
 
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 //----------------------------------------------------------------------------
 /**
  * Top-level class for controlling the visual appearance of nodes and edges
  * according to data attributes, as well as some global visual attributes.
  * This class holds a reference to a NetworkView that displays the network,
  * a CalculatorCatalog that holds the set of known visual styles and
  * calculators, and a current VisualStyle that is used to determine the
  * values of the visual attributes. A Logger is also supplied to report errors.
  *
  * Note that a null VisualStyle is not allowed; this class always provides
  * at least a default object.
  *
  * The key methods are the apply* methods. These methods first recalculate
  * the visual appearances by delegating to the calculators contained in the
  * current visual style. The usual return value of these methods is an
  * Appearance object that contains the visual attribute values; these
  * values are then applied to the network by calling the appropriate set
  * methods in the graph view API.
  */
 public class VisualMappingManager extends SubjectBase {
 
     NetworkView networkView;      //the object displaying the network
     CalculatorCatalog catalog;    //catalog of visual styles and calculators
     VisualStyle visualStyle;      //the currently active visual style
     Logger logger;                //for reporting errors
 
     // reusable appearance objects
     NodeAppearance myNodeApp = new NodeAppearance();
     EdgeAppearance myEdgeApp = new EdgeAppearance();
     GlobalAppearance myGlobalApp = new GlobalAppearance();
 
     // Optimizer Flag
     private boolean optimizer = true;
 
     public VisualMappingManager(NetworkView networkView,
                                 CalculatorCatalog catalog,
                                 VisualStyle style,
                                 Logger logger) {
         this.networkView = networkView;
         this.catalog = catalog;
         this.logger = logger;
         if (style != null) {
             setVisualStyle(style);
         } else {//get a default from the catalog
             VisualStyle defStyle = catalog.getVisualStyle("default");
             setVisualStyle(defStyle);
         }
     }
 
 
     public NetworkView getNetworkView() {return networkView;}
 
     public CyNetwork getNetwork() {
       return networkView.getNetwork();
     }
 
     public CalculatorCatalog getCalculatorCatalog() {return catalog;}
 
     public VisualStyle getVisualStyle() {return visualStyle;}
 
     /**
      * Sets a new visual style, and returns the old style. Also fires
      * an event to attached listeners.
      *
      * If the argument is null, no change is made, an error message
      * is passed to the logger, and null is returned.
      */
     public VisualStyle setVisualStyle(VisualStyle vs) {
         if (vs != null) {
             VisualStyle tmp = visualStyle;
             visualStyle = vs;
             this.fireStateChanged();
             return tmp;
         } else {
             String s = "VisualMappingManager: Attempt to set null VisualStyle";
             logger.severe(s);
             return null;
         }
     }
 
     /**
      * Sets a new visual style. Attempts to get the style with the given
      * name from the catalog and pass that to setVisualStyle(VisualStyle).
      * The return value is the old style.
      *
      * If no visual style with the given name is found, no change is made,
      * an error message is passed to the logger, and null is returned.
      */
     public VisualStyle setVisualStyle(String name) {
         VisualStyle vs = catalog.getVisualStyle(name);
         if (vs != null) {
             return setVisualStyle(vs);
         } else {
             String s = "VisualMappingManager: unknown VisualStyle: " + name;
             logger.severe(s);
             return null;
         }
     }
 
     /**
      * Recalculates and reapplies all of the node appearances. The
      * visual attributes are calculated by delegating to the
      * NodeAppearanceCalculator member of the current visual style.
      */
     public void applyNodeAppearances() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
         NodeAppearanceCalculator nodeAppearanceCalculator =
                 visualStyle.getNodeAppearanceCalculator();
         for (Iterator i = graphView.getNodeViewsIterator(); i.hasNext(); ) {
             NodeView nodeView = (NodeView)i.next();
             Node node = nodeView.getNode();
 
             nodeAppearanceCalculator.calculateNodeAppearance
                     (myNodeApp,node,network);
             if (optimizer == false) {
                 nodeView.setUnselectedPaint(myNodeApp.getFillColor());
                 nodeView.setBorderPaint(myNodeApp.getBorderColor());
                 nodeView.setBorder(myNodeApp.getBorderLineType().getStroke());
                 nodeView.setHeight(myNodeApp.getHeight());
                 nodeView.setWidth(myNodeApp.getWidth());
                 nodeView.setShape( ShapeNodeRealizer.getGinyShape
                         (myNodeApp.getShape()) );
                 nodeView.getLabel().setText( myNodeApp.getLabel() );
                 Label label = nodeView.getLabel();
                 label.setFont(myNodeApp.getFont());
             } else {
                 Paint existingUnselectedColor = nodeView.getUnselectedPaint();
                 Paint newUnselectedColor = myNodeApp.getFillColor();
                 if (!newUnselectedColor.equals(existingUnselectedColor)) {
                     nodeView.setUnselectedPaint(newUnselectedColor);
                 }
                 Paint existingBorderPaint = nodeView.getBorderPaint();
                 Paint newBorderPaint = myNodeApp.getBorderColor();
                 if (!newBorderPaint.equals(existingBorderPaint)) {
                     nodeView.setBorderPaint(newBorderPaint);
                 }
                 Stroke existingBorderType = nodeView.getBorder();
                 Stroke newBorderType = myNodeApp.getBorderLineType()
                         .getStroke();
                 if (!newBorderType.equals(existingBorderType)) {
                     nodeView.setBorder(newBorderType);
                 }
                 double existingHeight = nodeView.getHeight();
                 double newHeight = myNodeApp.getHeight();
                if (newHeight - existingHeight > .99) {
                     nodeView.setHeight(newHeight);
                 }
                 double existingWidth = nodeView.getWidth();
                 double newWidth = myNodeApp.getWidth();
                if (newWidth - existingWidth > .99) {
                     nodeView.setWidth(newWidth);
                 }
                 int existingShape = nodeView.getShape();
                 int newShape = ShapeNodeRealizer.getGinyShape
                         (myNodeApp.getShape());
                 if (existingShape != newShape) {
                     nodeView.setShape(newShape);
                 }
                 Label label = nodeView.getLabel();
                 String existingLabel = label.getText();
                 String newLabel = myNodeApp.getLabel();
                 if (!newLabel.equals(existingLabel)) {
                     label.setText(newLabel);
                 }
                 Font existingFont = label.getFont();
                 Font newFont = myNodeApp.getFont();
                 if (!newFont.equals(existingFont)) {
                     label.setFont(newFont);
                 }
             }
         }
     }
 
     /**
      * Recalculates and reapplies all of the edge appearances. The
      * visual attributes are calculated by delegating to the
      * EdgeAppearanceCalculator member of the current visual style.
      */
     public void applyEdgeAppearances() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
 
         EdgeAppearanceCalculator edgeAppearanceCalculator =
                 visualStyle.getEdgeAppearanceCalculator();
         for (Iterator i = graphView.getEdgeViewsIterator(); i.hasNext(); ) {
             EdgeView edgeView = (EdgeView)i.next();
             Edge edge = edgeView.getEdge();
             edgeAppearanceCalculator.calculateEdgeAppearance
                     (myEdgeApp,edge,network);
 
             if (optimizer == false) {
                 edgeView.setUnselectedPaint(myEdgeApp.getColor());
                 edgeView.setStroke(myEdgeApp.getLineType().getStroke());
                 edgeView.setSourceEdgeEnd
                         (myEdgeApp.getSourceArrow().getGinyArrow());
                 edgeView.setTargetEdgeEnd
                         (myEdgeApp.getTargetArrow().getGinyArrow());
                 Label label = edgeView.getLabel();
                 label.setText(myEdgeApp.getLabel());
                 label.setFont(myEdgeApp.getFont());
             } else {
                 Paint existingUnselectedPaint = edgeView.getUnselectedPaint();
                 Paint newUnselectedPaint = myEdgeApp.getColor();
                 if (!newUnselectedPaint.equals(existingUnselectedPaint)) {
                     edgeView.setUnselectedPaint(newUnselectedPaint);
                 }
                 Stroke existingStroke = edgeView.getStroke();
                 Stroke newStroke = myEdgeApp.getLineType().getStroke();
                 if (!newStroke.equals(existingStroke)) {
                     edgeView.setStroke(newStroke);
                 }
 
                 int existingSourceEdge = edgeView.getSourceEdgeEnd();
                 int newSourceEdge = myEdgeApp.getSourceArrow().getGinyArrow();
                 if (newSourceEdge != existingSourceEdge) {
                     edgeView.setSourceEdgeEnd(newSourceEdge);
                 }
 
                 int existingTargetEdge = edgeView.getTargetEdgeEnd();
                 int newTargetEdge = myEdgeApp.getTargetArrow().getGinyArrow();
                 if (newTargetEdge != existingTargetEdge) {
                     edgeView.setTargetEdgeEnd(newTargetEdge);
                 }
 
                 Label label = edgeView.getLabel();
                 String existingText = label.getText();
                 String newText = myEdgeApp.getLabel();
                 if (!newText.equals(existingText)) {
                     label.setText(newText);
                 }
                 Font existingFont = label.getFont();
                 Font newFont = myEdgeApp.getFont();
                 if (!newFont.equals(existingFont)) {
                     label.setFont(newFont);
                 }
             }
         }
     }
 
     /**
      * Recalculates and reapplies the global visual attributes. The
      * recalculation is done by delegating to the GlobalAppearanceCalculator
      * member of the current visual style.
      */
     public void applyGlobalAppearances() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
         GlobalAppearanceCalculator globalAppearanceCalculator =
                 visualStyle.getGlobalAppearanceCalculator();
         globalAppearanceCalculator.calculateGlobalAppearance
                 (myGlobalApp, network);
 
         graphView.setBackgroundPaint(myGlobalApp.getBackgroundColor());
         //will ignore sloppy selection color for now
     }
 
     /**
      * Recalculates and reapplies all of the node, edge, and global
      * visual attributes. This method delegates to, in order,
      * applyNodeAppearances, applyEdgeAppearances, and
      * applyGlobalAppearances.
      */
     public void applyAppearances() {
         Date start = new Date();
         /** first apply the node appearance to all nodes */
         applyNodeAppearances();
         /** then apply the edge appearance to all edges */
         applyEdgeAppearances();
         /** now apply global appearances */
         applyGlobalAppearances();
         /** we rely on the caller to redraw the graph as needed */
         Date stop = new Date();
         //System.out.println("Time to apply node styles:  " + (stop.getTime() -
         //        start.getTime()));
   }
 }
