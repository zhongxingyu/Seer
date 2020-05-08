 package rocks6205.editor.model.elements;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import org.w3c.dom.Element;
 
 import rocks6205.editor.model.adt.SVGLengthUnit;
 import rocks6205.editor.model.adt.SVGPainting;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Rectangle2D;
 
 import java.util.ArrayList;
 
 /**
  * The <code>SVGGElement</code> class is a container used to group objects
  * corresponds to the <<code>g</code>> element in the SVG document.
  * Transformations applied to the <<code>g</code>> element are performed on all of its child
  * elements.
  *
  * @author Cheow Yeong Chi
  * @since 1.1
  *
  */
 public class SVGGElement extends SVGContainerElement {
 
     /*
      * CONSTRUCTOR
      */
 
     /**
      * Default constructor
      */
     public SVGGElement() {}
 
     public ArrayList<SVGGenericElement> ungroup() {
         recurseAttributes(this);
 
         return getDescendants();
     }
 
     /**
      * {@inheritDoc}
      */
     public Rectangle2D.Float getBounds() {
         Rectangle2D.Float rect           = null;
        Rectangle2D.Float descendantRect = null;
 
         for (SVGGenericElement descendant : getDescendants()) {
             descendantRect = descendant.getBounds();
 
             if ((descendantRect.width > 0) && (descendantRect.height > 0)) {
                 if (rect == null) {
                     rect = descendantRect;
                 } else {
                     Rectangle2D.union(descendantRect, rect, rect);
                 }
             }
         }
 
        return (Rectangle2D.Float) getTransform().createTransformedShape(rect).getBounds2D();
     }
 
     /**
      * {@inheritDoc}
      */
     public void draw(Graphics2D g) {
         AffineTransform affTrans = getTransform();
 
         g.transform(affTrans);
 
         for (SVGGenericElement descendant : getDescendants()) {
             descendant.draw(g);
         }
 
         try {
             affTrans.invert();
         } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
         }
 
         g.transform(affTrans);
     }
 
     /*
      * METHODS
      */
     private static void recurseAttributes(SVGGElement group) {
         SVGPainting   fill        = group.getFill();
         SVGPainting   stroke      = group.getStroke();
         SVGLengthUnit strokeWidth = group.getStrokeWidth();
         SVGLengthUnit tx          = group.getTranslateX();
         SVGLengthUnit ty          = group.getTranslateY();
         SVGLengthUnit descendantTx;
         SVGLengthUnit descendantTy;
 
         for (SVGGenericElement descendant : group.getDescendants()) {
             if (descendant.getFill() == null) {
                 descendant.setFill(fill);
             }
 
             if (descendant.getStroke() == null) {
                 descendant.setStroke(stroke);
             }
 
             if (descendant.getStrokeWidth() == null) {
                 descendant.setStrokeWidth(strokeWidth);
             }
 
             if (tx != null) {
                 descendantTx = descendant.getTranslateX();
 
                 if (descendantTx == null) {
                     descendantTx = new SVGLengthUnit(tx.getValue());
                 } else {
                     descendantTx = new SVGLengthUnit(descendantTx.getValue() + tx.getValue());
                 }
 
                 descendant.setTranslateX(descendantTx);
             }
 
             if (ty != null) {
                 descendantTy = descendant.getTranslateY();
 
                 if (descendantTy == null) {
                     descendantTy = new SVGLengthUnit(ty.getValue());
                 } else {
                     descendantTy = new SVGLengthUnit(descendantTy.getValue() + ty.getValue());
                 }
 
                 descendant.setTranslateY(descendantTy);
             }
         }
     }
 
     /**
      * Parses the attributes on the  <<code>g</code>> element in the SVG document
     * @param e lement from the document returned by the XMLParser
      * @return <code>SVGGElement</code> object
      */
     public static SVGGElement parseElement(Element e) {
         SVGGElement g_e = new SVGGElement();
 
         g_e.parseAttributes(e);
 
         return g_e;
     }
     
     /**
      * 
      * {@inheritDoc} 
      */
    @Override
    public String getElementType() {
       return "SVGGElement";
    }
 }
