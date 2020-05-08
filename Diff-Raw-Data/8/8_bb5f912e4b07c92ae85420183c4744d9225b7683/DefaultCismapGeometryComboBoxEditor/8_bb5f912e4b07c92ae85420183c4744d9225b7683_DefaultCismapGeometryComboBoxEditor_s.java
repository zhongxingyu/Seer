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
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.Validator;
 
 import javax.swing.JComboBox;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.Bindable;
 
 import de.cismet.cismap.commons.BoundingBox;
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
 
     //~ Instance fields --------------------------------------------------------
 
     private CidsBean geometryBean;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
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
         if (log.isDebugEnabled()) {
             log.debug("cismap: " + PluginRegistry.getRegistry().getPlugin(CISMAP_PLUGIN_ID)); // NOI18N
         }
         if (editable) {
             try {
                 if (log.isDebugEnabled()) {
                     log.debug("getting & setting plugin");                                    // NOI18N
                 }
                 cismap = (CismapPlugin)PluginRegistry.getRegistry().getPlugin(CISMAP_PLUGIN_ID);
             } catch (Exception e) {
                 log.error("Error during init of " + this.getClass(), e);                      // NOI18N
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
 
                 // try { metaObjectNode = (MetaObjectNode)
                 // ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNode().getNode(); log.fatal("A:" +
                 // metaObjectNode.getObject().getDebugString()); } catch (Exception runtimeException) { }
                 if (dmtn != null) {
                     metaObjectNode = (MetaObjectNode)dmtn.getNode();
                 }
                 // log.fatal("B:" + metaObjectNode.getObject().getDebugString());
 
                 // refreshModel();
                 CismapBroker.getInstance()
                         .getMappingComponent()
                         .getFeatureCollection()
                         .addFeatureCollectionListener(comboModel);
             } catch (Exception e) {
                 log.error("Error during init of " + this.getClass(), e); // NOI18N
             }
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @Override  public void removeNotify() { try {
      *            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeatureCollectionListener(comboModel);
      *            if (selectedFeature != null) {
      *            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(selectedFeature);
      *            // ???? // selectedCidsFeature.setEditable(false); //
      *            CismapBroker.getInstance().getMappingComponent().showHandles(false); //Entferne dei HAndles } } catch
      *            (Exception e) { log.error("Error during removeNotify of " + this.getClass(), e); // NOI18N } }
      */
     public void dispose() {
         try {
             CismapBroker.getInstance()
                     .getMappingComponent()
                     .getFeatureCollection()
                     .removeFeatureCollectionListener(comboModel);
 
             if (selectedFeature != null) {
                 CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(selectedFeature); // ????
                 // selectedCidsFeature.setEditable(false);
                 // CismapBroker.getInstance().getMappingComponent().showHandles(false); //Entferne dei HAndles
             }
         } catch (Exception e) {
             log.error("Error during removeNotify of " + this.getClass(), e); // NOI18N
         }
     }
 
 //    private void refreshModel() {
 //        Object sel = getSelectedItem();
 //        comboModel = new CismapGeometryComboModel(DefaultCismapGeometryComboBoxEditor.this, selectedFeature);
 //        setModel(comboModel);
 //        setSelectedItem(sel);
 //    }
 //    @Override
 //    public void setSelectedItem(Object item) {
 //
 //        de.cismet.cismap.commons.features.FeatureCollection features = CismapBroker.getInstance().getMappingComponent().getFeatureCollection();
 //        features.removeFeatureCollectionListener(featureCollectionListener);
 //        if (selectedFeature != item) {
 //            if (selectedFeature instanceof CidsFeature) {
 //                features.removeFeature(selectedFeature);
 //            }
 //
 //            super.setSelectedItem(item);
 //
 //            selectedFeature = (Feature) item;
 //            if (!features.getAllFeatures().contains(selectedFeature)) {
 //                features.addFeature(selectedFeature);
 //            }
 //        }
 //        features.addFeatureCollectionListener(featureCollectionListener);
 //    }
     @Override
     public String getBindingProperty() {
         return "selectedItem"; // NOI18N
     }
 
     @Override
     public Object getNullSourceValue() {
         return null;
     }
 
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 
     @Override
     public Converter getConverter() {
         return new Converter<CidsBean, Feature>() {
 
                 @Override
                 public Feature convertForward(final CidsBean value) {
                     try {
                         if (log.isDebugEnabled()) {
                             log.debug("convertForward", new CurrentStackTrace()); // NOI18N
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
 
                                     @Override
                                     public void setGeometry(final Geometry geom) {
                                         if (geom == null) {
                                             log.fatal("ATTENTION geom=null");            // NOI18N
                                         }
                                         final Geometry oldValue = getGeometry();
                                         super.setGeometry(geom);
                                         try {
                                             if (((oldValue == null) && (geom != null))
                                                         || ((oldValue != null) && !oldValue.equals(geom))) {
                                                 geometryBean.setProperty(GEOM_FIELD, geom);
                                             }
                                         } catch (Exception e) {
                                             log.error("Error when setting the geomtry.", e); // NOI18N
                                         }
                                     }
                                 };
                             selectedFeature = cidsFeature;
 
                             comboModel.setCurrentObjectFeature(selectedFeature);
                             final FeatureCollection cismapFeatures = CismapBroker.getInstance()
                                         .getMappingComponent()
                                         .getFeatureCollection();
 
                             if (cismapFeatures.getAllFeatures().contains(selectedFeature)) {
                                 if (log.isDebugEnabled()) {
                                     log.debug("feature already exist"); // NOI18N
                                 }
                                 //
                                 // CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(selectedFeature);
                                 // PFeature pf = (PFeature)
                                 // CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(selectedFeature);
                                 // selectedFeature = pf.getFeature();
                             } else {
                                 if (log.isDebugEnabled()) {
                                     log.debug("add selectedFeature " + selectedFeature + " with geometry "
                                                 + selectedFeature.getGeometry());
                                 }
                                 if (selectedFeature.getGeometry() == null) {
                                     selectedFeature.setGeometry((Geometry)value.getProperty(GEOM_FIELD));
                                 }
                                 cismapFeatures.addFeature(selectedFeature);
                             }
                             // CismapBroker.getInstance().getMappingComponent().getFeatureCollection().;
                             selectedFeature.setEditable(true);
                             // CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeatureCollectionListener(featureCollectionListener);
 
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
                             // setModel(new CismapGeometryComboModel(DefaultCismapGeometryComboBoxEditor.this,
                             // selectedFeature));
                             setSelectedItem(selectedFeature);
                             // CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeatureCollectionListener(featureCollectionListener);
                         }
                         return selectedFeature;
                     } catch (Throwable t) {
                         log.error("Error in convertForward", t); // NOI18N
                         return null;
                     }
                 }
 
                 @Override
                 public CidsBean convertReverse(final Feature value) {
                    log.fatal("convertReverse: " + value); // NOI18N
                     if (value == null) {
                         return null;
                     } else {
                         try {
                             if (geometryBean == null) {
                                 geometryBean = metaClass.getEmptyInstance().getBean();
                             }
                             final Geometry oldValue = (Geometry)geometryBean.getProperty(GEOM_FIELD);
                             final Geometry geom = value.getGeometry();
                             // log.fatal("geometryBean.setProperty(old=" + geometryBean.getProperty(GEOM_FIELD) +
                             // ",new=" + value.getGeometry() + ")");
                             if (((oldValue == null) && (geom != null))
                                         || ((oldValue != null) && !oldValue.equals(geom))) {
                                 geometryBean.setProperty(GEOM_FIELD, value.getGeometry());
                             }
                        } catch (Exception ex) {
                             log.error("Error during set geo_field", ex); // NOI18N
                         }
                         return geometryBean;
                     }
                 }
             };
     }
 
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
         if (log.isDebugEnabled()) {
             log.debug("getting plugin: " + cismap); // NOI18N
         }
         return cismap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cismap  DOCUMENT ME!
      */
     public void setCismap(final CismapPlugin cismap) {
         if (log.isDebugEnabled()) {
             log.debug("setting plugin to: " + cismap); // NOI18N
         }
         this.cismap = cismap;
     }
 
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
