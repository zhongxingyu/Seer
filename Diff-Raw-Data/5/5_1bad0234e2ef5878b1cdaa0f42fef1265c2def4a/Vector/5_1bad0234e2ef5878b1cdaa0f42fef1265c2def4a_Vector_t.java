 package org.vaadin.vol;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.AbstractComponent;
 
 public abstract class Vector extends AbstractComponent {
 
     private String projection;
 
     private Point[] points;
 
     private Style style;
 
     private Attributes vectAttributes = null;
 
     public void setPoints(Point... points) {
         setPointsWithoutRepaint(points);
         requestRepaint();
     }
     
     protected void setPointsWithoutRepaint(Point... points) {
         this.points = points;
     }
 
     public Point[] getPoints() {
         return points;
     }
 
     public void setProjection(String projection) {
         this.projection = projection;
     }
 
     public String getProjection() {
         if(projection == null && getApplication() != null) {
             OpenLayersMap parent2 = (OpenLayersMap) getParent().getParent();
             return parent2.getApiProjection();
         }
         return projection;
     }
 
     /**
      * @return the custom style declaration assosicated with this Vector
      */
    public Style getCustomStyle() {
         return style;
     }
    
     /**
      * @param style
      *            the custom style declaration to be used for rendering this
      *            Vector
      */
     public void setCustomStyle(Style style) {
         this.style = style;
         requestRepaint();
     }
 
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
         target.addAttribute("points", getPoints());
         if(getProjection() != null) {
             target.addAttribute("projection", getProjection());
         }
         if (style != null) {
             style.paint("olStyle", target);
         }
         if (vectAttributes != null) {
             vectAttributes.paint("olVectAttributes", target);
         }
     }
     
     public void select() {
         if(getParent() != null) {
             ((VectorLayer) getParent()).setSelectedVector(this);
         }
     }
     
     /**
      * Vectors styleName does not modify CSS style name as the method does for
      * standard Components. Instead the style name defines rendered intent that
      * will be used by OpenLayers to style the Vector. Rendered intents can be
      * configured with {@link StyleMap}s.
      * 
      * @see com.vaadin.ui.AbstractComponent#setStyleName(java.lang.String)
      */
     @Override
     public void setStyleName(String style) {
         super.setStyleName(style);
     }
 
     /**
      * Sets a custom renderer intent that OpenLayers should use to render the
      * vector. The default is 'default'.
      * 
      * 
      * @see StyleMap
      * 
      * @param style
      *            the name of renderer intent.
      */
     public void setRenderIntent(String style) {
         setStyleName(style);
     }
 
     /**
      * @return the vectAttributes
      */
     public Attributes getAttributes() {
         return vectAttributes;
     }
 
     /**
      * @param vectAttributes
      *            the vectAttributes to set
      */
     public void setAttributes(Attributes attributes) {
         this.vectAttributes = attributes;
     }
 
 }
