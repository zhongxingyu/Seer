 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cismap.cids.geometryeditor;
 
 import com.vividsolutions.jts.geom.Point;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JList;
import javax.swing.ListCellRenderer;
 import javax.swing.UIManager;
 
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.XStyledFeature;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @version  $Revision$, $Date$
  */
public class FeatureComboBoxRenderer extends DefaultListCellRenderer implements ListCellRenderer {
 
     //~ Instance fields --------------------------------------------------------
 
     Color background = UIManager.getDefaults().getColor("ComboBox.background");                  // NOI18N
     Color selectedBackground = UIManager.getDefaults().getColor("ComboBox.selectionBackground"); // NOI18N
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of FeatureComboBoxRenderer.
      */
     public FeatureComboBoxRenderer() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Component getListCellRendererComponent(final JList list,
             final Object value,
             final int index,
             final boolean isSelected,
             final boolean cellHasFocus) {
         super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         try {
             if (value != null) {
                 if ((value instanceof CidsFeature) && (((CidsFeature)value).getGeometry() != null)) {
                     setText(org.openide.util.NbBundle.getMessage(
                             FeatureComboBoxRenderer.class,
                             "FeatureComboBoxRenderer.getListCellRendererComponent(JList,Object,int,booblean,boolean).text.assignedGeometry",
                             new Object[] { ((CidsFeature)value).getGeometry().getGeometryType() })); // NOI18N
                 } else if ((value instanceof XStyledFeature) && (((XStyledFeature)value).getGeometry() != null)) {
                     setText(((XStyledFeature)value).getName());
                     setIcon(((XStyledFeature)value).getIconImage());
                     // log.fatal("xxxlala "+CismapBroker.getInstance().getMappingComponent().getPFeatureHM());
                     final PFeature pf = (PFeature)CismapBroker.getInstance().getMappingComponent().getPFeatureHM()
                                 .get(value);
 
                     final PFeature clonePf = (PFeature)pf.clone();
 
                     if (clonePf.getFeature().getGeometry() instanceof Point) {
                         clonePf.getChild(0).removeAllChildren();
                     } else {
                         clonePf.removeAllChildren();
                     }
 
                     setToolTipText("@@@@" + getText()); // NOI18N
                     // i=clonePf.toImage(100,55,null);
                 } else {
                     setText(value.getClass() + ":" + value.toString()); // NOI18N
                     setIcon(null);
                 }
             } else {
                 setText(org.openide.util.NbBundle.getMessage(
                         FeatureComboBoxRenderer.class,
                         "FeatureComboBoxRenderer.getListCellRendererComponent(JList,Object,int,booblean,boolean).text.noAssignement")); // NOI18N
             }
         } catch (Throwable t) {
             log.error("Error in the renderer of the ComboBox", t);      // NOI18N
 
             setText(org.openide.util.NbBundle.getMessage(
                     FeatureComboBoxRenderer.class,
                     "FeatureComboBoxRenderer.getListCellRendererComponent(JList,Object,int,booblean,boolean).text.value",
                     new Object[] { ((Feature)value).getGeometry() })); // NOI18N
         }
         return this;
     }
 }
