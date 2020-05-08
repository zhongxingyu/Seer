 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.butler;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 
 import java.awt.Component;
 
 import java.text.DecimalFormatSymbols;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollection;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class ButlerGeometryComboBox extends JComboBox {
 
     //~ Enums ------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public enum GEOM_FILTER_TYPE {
 
         //~ Enum constants -----------------------------------------------------
 
         POINT, RECTANGLE
     }
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ButlerGeometryComboBox object.
      *
      * @param  filter  DOCUMENT ME!
      */
     public ButlerGeometryComboBox(final GEOM_FILTER_TYPE filter) {
         final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
         final FeatureCollection fc = mc.getFeatureCollection();
         final List objects = new ArrayList();
         objects.add("keine Auswahl");
         for (final Feature f : fc.getAllFeatures()) {
            final Geometry g = f.getGeometry();
             // todo check that the geoms are in the right crs
             if (validatesFilter(g, filter)) {
                 objects.add(g);
             }
         }
 
         DefaultComboBoxModel model = null;
         model = new DefaultComboBoxModel(objects.toArray());
 
         this.setModel(model);
         this.setRenderer(new ButlerCbRenderer(filter));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   g       DOCUMENT ME!
      * @param   filter  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean validatesFilter(final Geometry g, final GEOM_FILTER_TYPE filter) {
         if (filter == GEOM_FILTER_TYPE.POINT) {
             return g instanceof Point;
         }
         if (filter == GEOM_FILTER_TYPE.RECTANGLE) {
             return g.isRectangle();
         }
         return false;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ButlerCbRenderer extends DefaultListCellRenderer {
 
         //~ Instance fields ----------------------------------------------------
 
         GEOM_FILTER_TYPE filter;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ButlerCbRenderer object.
          *
          * @param  filter  DOCUMENT ME!
          */
         public ButlerCbRenderer(final GEOM_FILTER_TYPE filter) {
 //            setHorizontalAlignment(CENTER);
             setVerticalAlignment(CENTER);
             this.filter = filter;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             String text = "";
             if (filter == GEOM_FILTER_TYPE.POINT) {
                 if (value instanceof Geometry) {
                     final Point p = (Point)value;
                     final double x = p.getX();
                     final double y = p.getY();
                     final java.text.DecimalFormat myFormatter = new java.text.DecimalFormat("#.###");
                     text = "Punkt " + myFormatter.format(x) + "/" + myFormatter.format(y);
                 } else {
                     text = value.toString();
                 }
             } else if (filter == GEOM_FILTER_TYPE.RECTANGLE) {
                 if (value instanceof Geometry) {
                     final Envelope g = ((Geometry)value).getEnvelopeInternal();
                     final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
                     formatSymbols.setDecimalSeparator('.');
                     final java.text.DecimalFormat myFormatter = new java.text.DecimalFormat("#.###", formatSymbols);
                     text = "Polygon (" + "(" + myFormatter.format(g.getMinX()) + "," + myFormatter.format(g.getMinY())
                                 + ")"
                                 + "(" + myFormatter.format(g.getMaxX()) + "," + myFormatter.format(g.getMaxY()) + ")"
                                 + ")";
                 } else {
                     text = value.toString();
                 }
             }
 
             setText(text);
             return this;
         }
     }
 }
