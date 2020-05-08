 package cytoscape.visual.ui.icon;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 
 
 /**
  * Icon for node shapes.
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
  *
   */
 public class NodeShapeIcon extends VisualPropertyIcon {
     /**
      * Creates a new NodeShapeIcon object.
      *
      * @param shape
      * @param width
      * @param height
      * @param name
      */
     public NodeShapeIcon(Shape shape, int width, int height, String name) {
         super(shape, width, height, name);
     }
 
     /**
      * Creates a new NodeShapeIcon object.
      *
      * @param shape DOCUMENT ME!
      * @param width DOCUMENT ME!
      * @param height DOCUMENT ME!
      * @param name DOCUMENT ME!
      * @param color DOCUMENT ME!
      */
     public NodeShapeIcon(Shape shape, int width, int height, String name,
         Color color) {
         super(shape, width, height, name, color);
     }
 
     /**
      * Draw icon using Java2D.
      *
      * @param c DOCUMENT ME!
      * @param g DOCUMENT ME!
      * @param x DOCUMENT ME!
      * @param y DOCUMENT ME!
      */
     public void paintIcon(Component c, Graphics g, int x, int y) {
         final Graphics2D g2d = (Graphics2D) g;
         final double shapeWidth = shape.getBounds2D()
                                        .getWidth();
         final double shapeHeight = shape.getBounds2D()
                                         .getHeight();
 
         final double xRatio = width / shapeWidth;
         final double yRatio = height / shapeHeight;
 
         final AffineTransform af = new AffineTransform();
 
         // AA on
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
 
         g2d.setStroke(new BasicStroke(2.0f));
         g2d.setColor(color);
 
         final Rectangle2D bound = shape.getBounds2D();
         final double minx = bound.getMinX();
         final double miny = bound.getMinY();
 
         Shape newShape = shape;
 
         if (minx < 0) {
             af.setToTranslation(
                 Math.abs(minx),
                 0);
             newShape = af.createTransformedShape(newShape);
         }
 
         if (miny < 0) {
            af.setToTranslation(0, Math.abs(miny));
             newShape = af.createTransformedShape(newShape);
         }
 
         af.setToScale(xRatio, yRatio);
         newShape = af.createTransformedShape(newShape);
 
         af.setToTranslation(10,
             ((height + 20) - newShape.getBounds2D()
                                      .getHeight()) / 2);
         newShape = af.createTransformedShape(newShape);
 
        //System.out.println("Shape: " + name + ", " + newShape.getBounds2D());
        
         g2d.draw(newShape);
     }
 }
