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
 package de.cismet.cismap.cids.geometryeditor;
 
 import Sirius.navigator.plugin.PluginRegistry;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.Validator;
 
 import javax.swing.JComboBox;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.Bindable;
 
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollection;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 import de.cismet.cismap.navigatorplugin.CismapPlugin;
 
 import de.cismet.tools.CurrentStackTrace;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class DefaultCismapGeometryComboBoxEditor extends JComboBox implements Bindable, MetaClassStore {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String GEOM_FIELD = "geo_field";    // NOI18N
     private static final String CISMAP_PLUGIN_ID = "cismap"; // NOI18N
     private static final Logger LOG = Logger.getLogger(DefaultCismapGeometryComboBoxEditor.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private CidsBean geometryBean;
     private MetaObjectNode metaObjectNode;
     private MetaClass metaClass;
     private CismapPlugin cismap;
     private Feature selectedFeature = null;
     private CidsFeature cidsFeature = null;
     private CismapGeometryComboModel comboModel = null;
     private MetaObject cidsMetaObject = null;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DefaultCismapGeometryComboBoxEditor object.
      */
     public DefaultCismapGeometryComboBoxEditor() {
         this(true);
     }
 
     /**
      * Creates a new DefaultCismapGeometryComboBoxEditor object.
      *
      * @param  editable  DOCUMENT ME!
      */
     public DefaultCismapGeometryComboBoxEditor(final boolean editable) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("cismap: " + PluginRegistry.getRegistry().getPlugin(CISMAP_PLUGIN_ID)); // NOI18N
         }
 
         if (editable) {
             try {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("getting & setting plugin"); // NOI18N
                 }
 
                 cismap = (CismapPlugin)PluginRegistry.getRegistry().getPlugin(CISMAP_PLUGIN_ID);
             } catch (final Exception e) {
                 LOG.error("Error during init of " + this.getClass(), e); // NOI18N
                 // TODO: isn't that a reason to cancel init?
             }
         }
 
         comboModel = new CismapGeometryComboModel(DefaultCismapGeometryComboBoxEditor.this, selectedFeature);
         setModel(comboModel);
         setRenderer(new FeatureComboBoxRenderer());
 
         if (editable) {
             try {
                 final DefaultMetaTreeNode dmtn = (DefaultMetaTreeNode)ComponentRegistry.getRegistry()
                             .getAttributeEditor()
                             .getTreeNode();
 
                 if (dmtn != null) {
                     metaObjectNode = (MetaObjectNode)dmtn.getNode();
                 }
 
                 CismapBroker.getInstance()
                         .getMappingComponent()
                         .getFeatureCollection()
                         .addFeatureCollectionListener(comboModel);
             } catch (final Exception e) {
                 LOG.error("Error during init of " + this.getClass(), e); // NOI18N
             }
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     public void dispose() {
         try {
             CismapBroker.getInstance()
                     .getMappingComponent()
                     .getFeatureCollection()
                     .removeFeatureCollectionListener(comboModel);
 
             if (selectedFeature != null) {
                 // ????
                 // TODO: ???? isn't a very helpful comment, so what is wrong here
                 CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(selectedFeature);
             }
         } catch (final Exception e) {
             LOG.error("Error during removeNotify of " + this.getClass(), e); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public String getBindingProperty() {
         return "selectedItem"; // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Object getNullSourceValue() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Converter getConverter() {
         return new Converter<CidsBean, Feature>() {
 
                 @Override
                 public Feature convertForward(final CidsBean value) {
                     try {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("convertForward", new CurrentStackTrace()); // NOI18N
                         }
 
                         geometryBean = value;
                         if (value != null) {
                             MetaObject cidsFeatureMetaObject = null;
 
                             if (cidsMetaObject != null) {
                                 cidsFeatureMetaObject = cidsMetaObject;
                             } else if (metaObjectNode != null) {
                                 cidsFeatureMetaObject = metaObjectNode.getObject();
                             }
 
                             cidsFeature = new CidsFeature(cidsFeatureMetaObject) {

                                     private Geometry lastGeom;

                                     @Override
                                     public void setGeometry(final Geometry geom) {
                                         if (geom == null) {
                                             LOG.warn("ATTENTION geom=null");              // NOI18N
                                         }
                                         final Geometry oldValue = lastGeom;
                                         super.setGeometry(geom);
                                         try {
                                             if (((oldValue == null) && (geom != null))
                                                         || ((oldValue != null) && !oldValue.equalsExact(geom))) {
                                                 geometryBean.setProperty(GEOM_FIELD, geom);
                                                 if (geom != null) {
                                                     lastGeom = (Geometry)geom.clone();
                                                 } else {
                                                     lastGeom = null;
                                                 }
                                             }
                                         } catch (final Exception e) {
                                             LOG.error("Error when setting the geometry.", e); // NOI18N
                                         }
                                     }
                                 };
                             selectedFeature = cidsFeature;
 
                             comboModel.setCurrentObjectFeature(selectedFeature);
                             final FeatureCollection cismapFeatures = CismapBroker.getInstance()
                                         .getMappingComponent()
                                         .getFeatureCollection();
 
                             if (cismapFeatures.getAllFeatures().contains(selectedFeature)) {
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("Feature already exists. Remove it from map."); // NOI18N
                                 }
 
                                 // As you see some lines above, selectedFeature contains a recently created object.
                                 // CidsFeature.equals(Object) only compares some essential attributes, and so tells
                                 // that selectedFeature is in the map. Without replacing the feature in the map with
                                 // the selectedFeature all following invocations on selectedFeature won't have an
                                 // effect on the feature in the map, especially setEditable(true) would be useless.
                                 cismapFeatures.removeFeature(selectedFeature);
                             }
 
                             if (selectedFeature.getGeometry() == null) {
                                 selectedFeature.setGeometry((Geometry)value.getProperty(GEOM_FIELD));
                             }
 
                             if (LOG.isDebugEnabled()) {
                                 LOG.debug("Add selectedFeature '" + selectedFeature + "' with geometry '" // NOI18N
                                             + selectedFeature.getGeometry() + "' to feature collection.");
                             }
                             cismapFeatures.addFeature(selectedFeature);
 
                             selectedFeature.setEditable(true);
 
                             cismapFeatures.holdFeature(selectedFeature);
                             cismapFeatures.select(selectedFeature);
 
                             CismapBroker.getInstance().getMappingComponent().showHandles(true);
                             if (selectedFeature.getGeometry() != null) {
                                 CismapBroker.getInstance()
                                         .getMappingComponent()
                                         .gotoBoundingBox(new XBoundingBox(selectedFeature.getGeometry()),
                                             false,
                                             true,
                                             0);
                             }
 
                             setSelectedItem(selectedFeature);
                         }
                         return selectedFeature;
                     } catch (final Exception e) {
                         LOG.error("Error in convertForward", e); // NOI18N
                         return null;
                     }
                 }
 
                 @Override
                 public CidsBean convertReverse(final Feature value) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("convertReverse: " + value); // NOI18N
                     }
 
                     if (value == null) {
                         return null;
                     } else {
                         try {
                             if (geometryBean == null) {
                                 geometryBean = metaClass.getEmptyInstance().getBean();
                             }
                             final Geometry oldValue = (Geometry)geometryBean.getProperty(GEOM_FIELD);
                             final Geometry geom = value.getGeometry();
 
                             if (((oldValue == null) && (geom != null))
                                         || ((oldValue != null) && !oldValue.equalsExact(geom))) {
                                 geometryBean.setProperty(GEOM_FIELD, value.getGeometry());
                             }
                         } catch (final Exception ex) {
                             LOG.error("Error during set geo_field", ex); // NOI18N
                         }
                         return geometryBean;
                     }
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Validator getValidator() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public CismapPlugin getCismap() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("getting plugin: " + cismap); // NOI18N
         }
 
         return cismap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cismap  DOCUMENT ME!
      */
     public void setCismap(final CismapPlugin cismap) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("setting plugin to: " + cismap); // NOI18N
         }
 
         this.cismap = cismap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public MetaClass getMetaClass() {
         return metaClass;
     }
 
     /**
      * Set the MetaClass of the geometry object.
      *
      * @param  metaClass  DOCUMENT ME!
      */
     @Override
     public void setMetaClass(final MetaClass metaClass) {
         this.metaClass = metaClass;
     }
 
     /**
      * Set the MetaObject of the Cids Bean, that is represented by the geometry that is bound to this ComboBox. This
      * method should be invoked before the invocation of bind and is only needed, if the object that is represented by
      * the geometry of this ComboBox differs from the object that is represented by the selected tree node.
      *
      * <p>If the MetaObject is not set, the MetaObject object from the DefaultMetaTreeNode will be used.</p>
      *
      * @param  cidsMetaObject  cidsFeatureClass DOCUMENT ME!
      */
     public void setCidsMetaObject(final MetaObject cidsMetaObject) {
         this.cidsMetaObject = cidsMetaObject;
     }
 
     /**
      * If you want to use this combo box for different cids beans, then you should invoke this method before you bind a
      * new cids bean and after an invocation of the dispose()-method and the unbind-method of the corresponding
      * BindingGroup object.
      */
     public void initForNewBinding() {
         // normally, the dispose()-method was invoked before the CidsMetaObject changes
         // and in this case, the feature collection listener must be registered.
         CismapBroker.getInstance()
                 .getMappingComponent()
                 .getFeatureCollection()
                 .removeFeatureCollectionListener(comboModel);
 
         geometryBean = null;
         selectedFeature = null;
         cidsFeature = null;
 
         comboModel = new CismapGeometryComboModel(DefaultCismapGeometryComboBoxEditor.this, selectedFeature);
         setModel(comboModel);
 
         CismapBroker.getInstance()
                 .getMappingComponent()
                 .getFeatureCollection()
                 .addFeatureCollectionListener(comboModel);
     }
 
     /**
      * @see  setCidsMetaObject(final MetaObject cidsMetaObject)
      */
     public MetaObject getCidsMetaObject() {
         return this.cidsMetaObject;
     }
 }
