 package rocks6205.editor.model.elements;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 
 import rocks6205.editor.model.adt.SVGLengthUnit;
 import rocks6205.editor.model.adt.SVGLengthUnitType;
 import rocks6205.editor.model.adt.SVGPaintingType;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.awt.BasicStroke;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 
 /**
  * The <code>SVGLineElement</code> class is used to parse <<code>line</code>> element in
  * SVG documents and convert to <code>SVGLineElement</code> which is subsequently drawn on
  * the image canvas.
  *
  * @author Sugar CheeSheen Chan
  *
  * @since 1.1
  *
  */
 public final class SVGLineElement extends SVGShapeElement {
 
     /*
      * PROPERTIES
      */
 
     /**
      * x-coordinate of the first point
      */
     private SVGLengthUnit x1;
 
     /**
      * y-coordinate of the first point
      */
     private SVGLengthUnit y1;
 
     /**
      * x-coordinate of the second point
      */
     private SVGLengthUnit x2;
 
     /**
      * y-coordinate of the second point
      */
     private SVGLengthUnit y2;
 
     /**
      * Default constructor.
      * Construct an instance of <code>SVGLineElement</code> with coordinates initialised to zero.
      */
     public SVGLineElement() {
         x1 = new SVGLengthUnit(0);
         y1 = new SVGLengthUnit(0);
         x2 = new SVGLengthUnit(0);
         y2 = new SVGLengthUnit(0);
     }
 
     /*
      * CONSTRUCTORS
      */
 
     /**
      * Construct an instance of <code>SVGLineElement</code> with the coordinates
      * of the first point and the second point.
      *
      * @param x1 x-coordinate of the first point
      * @param y1 y-coordinate of the first point
      * @param x2 x-coordinate of the second point
      * @param y2 y-coordinate of the second point
      */
     public SVGLineElement(SVGLengthUnit x1, SVGLengthUnit y1, SVGLengthUnit x2, SVGLengthUnit y2) {
         setX1(x1);
         setY1(y1);
         setX2(x2);
         setY2(y2);
     }
 
     /*
      * ACCESSORS
      */
 
     /**
      * @return x1 x-coordinate of the first point
      */
     public SVGLengthUnit getX1() {
         return this.x1;
     }
 
     /**
      * @return y1 y-coordinate of the first point
      */
     public SVGLengthUnit getY1() {
         return this.y1;
     }
 
     /**
      * @return x2 x-coordinate of the second point
      */
     public SVGLengthUnit getX2() {
         return this.x2;
     }
 
     /**
      * @return y2 y-coordinate of the second point
      */
     public SVGLengthUnit getY2() {
         return this.y2;
     }
 
     /*
      * MUTATORS
      */
 
     /**
      * @param x1 x-coordinate of the first point
      */
     public void setX1(SVGLengthUnit x1) {
         if (x1 != null) {
             this.x1 = x1;
         }
     }
 
     /**
      * @param y1 y-coordinate of the first point
      */
     public void setY1(SVGLengthUnit y1) {
         if (y1 != null) {
             this.y1 = y1;
         }
     }
 
     /**
      * @param x2 x-coordinate of the second point
      */
     public void setX2(SVGLengthUnit x2) {
         if (x2 != null) {
             this.x2 = x2;
         }
     }
 
     /**
      * @param y2 y-coordinate of the second point
      */
     public void setY2(SVGLengthUnit y2) {
         if (y2 != null) {
             this.y2 = y2;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Rectangle2D.Float getBounds() {
         Rectangle2D.Float bounds;
         float            padding = 0;
 
         if (getResultantStroke().getPaintType() != SVGPaintingType.NONE) {
             padding = getResultantStrokeWidth().getValue(SVGLengthUnitType.PX) / 2;
         }
 
         float computedX = (float) (Math.min(x1.getValue(SVGLengthUnitType.PX), x2.getValue(SVGLengthUnitType.PX))
                                    - padding);
         float computedY = (float) (Math.min(y1.getValue(SVGLengthUnitType.PX), y2.getValue(SVGLengthUnitType.PX))
                                    - padding);
         float computedWidth = (float) (Math.abs(x2.getValue(SVGLengthUnitType.PX) - x1.getValue(SVGLengthUnitType.PX))
                                        + 2 * padding);
         float computedHeight = (float) (Math.abs(y2.getValue(SVGLengthUnitType.PX) - y1.getValue(SVGLengthUnitType.PX))
                                         + 2 * padding);
 
         bounds = new Rectangle2D.Float(computedX, computedY, computedWidth, computedHeight);
 
         Rectangle2D.Double rect = (Rectangle2D.Double) getTransform().createTransformedShape(bounds).getBounds2D();
         return new Rectangle2D.Float((float)rect.x, (float)rect.y, (float)rect.width, (float)rect.height);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void draw(Graphics2D g) {
         Shape line = new Line2D.Float(x1.getValue(SVGLengthUnitType.PX), y1.getValue(SVGLengthUnitType.PX),
                                        x2.getValue(SVGLengthUnitType.PX), y2.getValue(SVGLengthUnitType.PX));
 
         line = getTransform().createTransformedShape(line);
         g.setPaint(getResultantStroke().getPaintColor());
         g.setStroke(new BasicStroke((float) getResultantStrokeWidth().getValue(SVGLengthUnitType.PX),
                                     getStrokeLineCap(), getStrokeLineJoin()));
         g.draw(line);
     }
 
     /*
      * METHOD
      */
 
     /**
      * Parse line element from DOM level to object interpretable as defined
      *
      * @param element         Element from the document returned by the XMLParser
      * @return                Line element parsed from Element to be drawn
      */
     public static SVGLineElement parseElement(Element element) {
         SVGLineElement line   = new SVGLineElement();
         Attr           x1Attr = element.getAttributeNodeNS(null, "x1");
 
         if (x1Attr != null) {
            line.setX1(SVGLengthUnit.parse(x1Attr.getValue()));
         }
 
         Attr y1Attr = element.getAttributeNodeNS(null, "y1");
 
         if (y1Attr != null) {
            line.setY1(SVGLengthUnit.parse(y1Attr.getValue()));
         }
 
         Attr x2Attr = element.getAttributeNodeNS(null, "x2");
 
         if (x2Attr != null) {
            line.setX2(SVGLengthUnit.parse(x2Attr.getValue()));
         }
 
         Attr y2Attr = element.getAttributeNodeNS(null, "y2");
 
         if (y2Attr != null) {
            line.setY2(SVGLengthUnit.parse(y2Attr.getValue()));
         }
 
         line.parseAttributes(element);
        System.out.printf("%f %f %f %f\n", line.x1.getValue(),line.y1.getValue(),line.x2.getValue(),line.y2.getValue());
         return line;
     }
     
     /**
      * 
      * {@inheritDoc} 
      */
    @Override
    public String getElementType() {
       return "SVGLineElement";
    }
 }
