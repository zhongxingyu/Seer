 package cytoscape.visual.ui.editors.continuous;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.util.CyColorChooser;
 import cytoscape.visual.VisualPropertyType;
 
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.ContinuousMapping;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JColorChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 
 /**
  * Drawing and updating below & above values in Gradient Editor.
  *
  * @author $author$
  */
 public class BelowAndAbovePanel extends JPanel {
     private VisualPropertyType type;
     private Color boxColor;
     private boolean below;
     private Object value;
 
     /**
      * DOCUMENT ME!
      */
     public static final String COLOR_CHANGED = "COLOR_CHANGED";
 
     /**
      * Creates a new BelowAndAbovePanel object. This will be used for drawing
      * below & above triangle
      *
      * @param color
      *            DOCUMENT ME!
      * @param below
      *            DOCUMENT ME!
      */
     public BelowAndAbovePanel(VisualPropertyType type, Color color,
         boolean below) {
         this.boxColor = color;
         this.below = below;
         this.type = type;
 
         if (below)
            this.setToolTipText("Click triangle to set below color...");
         else
            this.setToolTipText("Click triangle to set above color...");
 
         this.addMouseListener(new MouseEventHandler(this));
     }
 
     /**
      * Creates a new BelowAndAbovePanel object.
      *
      * @param type DOCUMENT ME!
      * @param below DOCUMENT ME!
      */
     public BelowAndAbovePanel(VisualPropertyType type, boolean below) {
         this(type, Color.DARK_GRAY, below);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param newColor DOCUMENT ME!
      */
     public void setColor(Color newColor) {
         final Color oldColor = boxColor;
         this.boxColor = newColor;
         this.repaint();
         this.getParent()
             .repaint();
 
         this.firePropertyChange(COLOR_CHANGED, oldColor, newColor);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param value DOCUMENT ME!
      */
     public void setValue(Object value) {
         this.value = value;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param g
      *            DOCUMENT ME!
      */
     public void paintComponent(Graphics g) {
         final Graphics2D g2d = (Graphics2D) g;
 
         final Polygon poly = new Polygon();
 
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
 
         g2d.setStroke(new BasicStroke(1.0f));
         g2d.setColor(boxColor);
 
         if (below) {
             poly.addPoint(9, 0);
             poly.addPoint(9, 10);
             poly.addPoint(0, 5);
         } else {
             poly.addPoint(0, 0);
             poly.addPoint(0, 10);
             poly.addPoint(9, 5);
         }
 
         g2d.fillPolygon(poly);
 
         g2d.setColor(Color.black);
         g2d.draw(poly);
     }
 
     class MouseEventHandler extends MouseAdapter {
         private BelowAndAbovePanel caller;
 
         public MouseEventHandler(BelowAndAbovePanel c) {
             this.caller = c;
         }
 
         public void mouseClicked(MouseEvent e) {
             if (e.getClickCount() == 2) {
             	
             	Object newValue = null;
                 if (type.getDataType() == Color.class) {
                 	newValue = CyColorChooser.showDialog(caller, "Select new color",
                             boxColor);
                 	caller.setColor((Color)newValue);
                 }
                 else if (type.getDataType() == Number.class) {
                 	newValue = Double.parseDouble(JOptionPane.showInputDialog(caller, "Please enter new value."));
                 	caller.setValue(newValue);
                 }
                 
                 if(newValue == null) {
                 	return;
                 }
 
                 final ContinuousMapping cMapping;
 
                 if (type.isNodeProp())
                     cMapping = (ContinuousMapping) Cytoscape.getVisualMappingManager()
                                                             .getVisualStyle()
                                                             .getNodeAppearanceCalculator()
                                                             .getCalculator(type)
                                                             .getMapping(0);
                 else
                     cMapping = (ContinuousMapping) Cytoscape.getVisualMappingManager()
                                                             .getVisualStyle()
                                                             .getEdgeAppearanceCalculator()
                                                             .getCalculator(type)
                                                             .getMapping(0);
 
                 BoundaryRangeValues brv;
                 BoundaryRangeValues original;
 
                 if (below) {
                     original = cMapping.getPoint(0)
                                        .getRange();
                     brv = new BoundaryRangeValues(newValue,
                             original.equalValue, original.greaterValue);
                     cMapping.getPoint(0)
                             .setRange(brv);
                 } else {
                     original = cMapping.getPoint(cMapping.getPointCount() - 1)
                                        .getRange();
 
                     brv = new BoundaryRangeValues(original.lesserValue,
                             original.equalValue, newValue);
                     cMapping.getPoint(cMapping.getPointCount() - 1)
                             .setRange(brv);
                 }
 
                 cMapping.fireStateChanged();
 
                 // Update view.
                 Cytoscape.getVisualMappingManager()
                          .getNetworkView()
                          .redrawGraph(false, true);
 
                 caller.repaint();
                 caller.getParent()
                       .repaint();
             }
         }
     }
 }
