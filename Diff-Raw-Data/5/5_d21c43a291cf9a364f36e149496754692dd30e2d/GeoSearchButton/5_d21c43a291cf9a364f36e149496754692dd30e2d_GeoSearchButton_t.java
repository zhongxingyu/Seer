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
 package de.cismet.cismap.navigatorplugin;
 
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 
 import de.cismet.cids.navigator.utils.CidsBeanDropTarget;
 
 import de.cismet.cismap.commons.features.AbstractNewFeature;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.SearchFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
 
 import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
 import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;
 
 import de.cismet.cismap.tools.gui.CidsBeanDropJPopupMenuButton;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.StayOpenCheckBoxMenuItem;
 
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.ELLIPSE;
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.LINESTRING;
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.POLYGON;
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.RECTANGLE;
 
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchMenuSelectedAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchRectangleAction;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jruiz
  * @version  $Revision$, $Date$
  */
 public class GeoSearchButton extends CidsBeanDropJPopupMenuButton implements PropertyChangeListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GeoSearchButton.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final String interactionMode;
     private final MappingComponent mappingComponent;
     private final String searchName;
     private final AbstractCreateSearchGeometryListener searchListener;
     private Action searchAction;
     private Action searchMenuSelectedAction;
     private Action searchRectangleAction;
     private Action searchPolygonAction;
     private Action searchCidsFeatureAction;
     private Action searchEllipseAction;
     private Action searchPolylineAction;
     private Action searchRedoAction;
     private Action searchShowLastFeatureAction;
     private Action searchBufferAction;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JSeparator jSeparator12;
     private javax.swing.JMenuItem mniSearchBuffer1;
     private javax.swing.JRadioButtonMenuItem mniSearchCidsFeature1;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline1;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle1;
     private javax.swing.JMenuItem mniSearchRedo1;
     private javax.swing.JMenuItem mniSearchShowLastFeature1;
     private javax.swing.JPopupMenu popMenSearch;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new GeoSearchButton object.
      *
      * @param  interactionMode   DOCUMENT ME!
      * @param  mappingComponent  DOCUMENT ME!
      * @param  searchName        DOCUMENT ME!
      */
     public GeoSearchButton(final String interactionMode,
             final MappingComponent mappingComponent,
             final String searchName) {
         this(interactionMode, mappingComponent, searchName, "");
     }
 
     /**
      * Creates new form GeoSearchButton1.
      *
      * @param  interactionMode   DOCUMENT ME!
      * @param  mappingComponent  DOCUMENT ME!
      * @param  searchName        DOCUMENT ME!
      * @param  toolTipText       DOCUMENT ME!
      */
     public GeoSearchButton(final String interactionMode,
             final MappingComponent mappingComponent,
             final String searchName,
             final String toolTipText) {
         super(interactionMode, mappingComponent, searchName);
         searchAction = createSearchAction(interactionMode, mappingComponent);
         searchMenuSelectedAction = createSearchMenuSelectedAction(interactionMode, mappingComponent);
         searchRectangleAction = createSearchRectangleAction(interactionMode, mappingComponent);
         searchPolygonAction = createSearchPolygonAction(interactionMode, mappingComponent);
         searchCidsFeatureAction = createSearchCidsFeatureAction(interactionMode, mappingComponent);
         searchEllipseAction = createSearchEllipseAction(interactionMode, mappingComponent);
         searchPolylineAction = createSearchPolylineAction(interactionMode, mappingComponent);
         searchRedoAction = createSearchRedoAction(interactionMode, mappingComponent);
         searchShowLastFeatureAction = createSearchShowLastFeatureAction(interactionMode, mappingComponent);
         searchBufferAction = createSearchBufferAction(interactionMode, mappingComponent);
         initComponents();
 
         setPopupMenu(popMenSearch);
         new CidsBeanDropTarget(this);
 
         setTargetIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchTarget.png"))); // NOI18N
 
         this.interactionMode = interactionMode;
         this.mappingComponent = mappingComponent;
         this.searchName = searchName;
         this.searchListener = (AbstractCreateSearchGeometryListener)mappingComponent.getInputListener(interactionMode);
 
         setButtonIcon(searchListener.getMode());
         setModeSelection(searchListener.getMode());
         setLastFeature(searchListener.getLastSearchFeature());
 
         searchListener.addPropertyChangeListener(this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchAction(final String interactionMode, final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchAction"); // NOI18N
                     }
                     mappingComponent.setInteractionMode(interactionMode);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchMenuSelectedAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchMenuSelectedAction"); // NOI18N
                     }
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchRectangleAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchRectangleAction"); // NOI18N
                     }
                     mappingComponent.setInteractionMode(interactionMode);
                     searchListener.setMode(CreateSearchGeometryListener.RECTANGLE);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchPolygonAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchPolygonAction"); // NOI18N
                     }
                     mappingComponent.setInteractionMode(interactionMode);
                     searchListener.setMode(CreateSearchGeometryListener.POLYGON);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchCidsFeatureAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchCidsFeatureAction"); // NOI18N
                     }
                     mappingComponent.setInteractionMode(interactionMode);
 
                     CismetThreadPool.execute(new javax.swing.SwingWorker<SearchFeature, Void>() {
 
                             @Override
                             protected SearchFeature doInBackground() throws Exception {
                                 final DefaultMetaTreeNode[] nodes = ComponentRegistry.getRegistry()
                                             .getActiveCatalogue()
                                             .getSelectedNodesArray();
                                 final Collection<Geometry> searchGeoms = new ArrayList<Geometry>();
 
                                 for (final DefaultMetaTreeNode dmtn : nodes) {
                                     if (dmtn instanceof ObjectTreeNode) {
                                         final MetaObject mo = ((ObjectTreeNode)dmtn).getMetaObject();
                                         final CidsFeature cf = new CidsFeature(mo);
                                         searchGeoms.add(cf.getGeometry());
                                     }
                                 }
                                 final Geometry[] searchGeomsArr = searchGeoms.toArray(
                                         new Geometry[0]);
                                 final GeometryCollection coll =
                                     new GeometryFactory().createGeometryCollection(
                                         searchGeomsArr);
 
                                 final Geometry newG = coll.buffer(0.1d);
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("SearchGeom " + newG.toText()); // NOI18N
                                 }
 
                                 final SearchFeature sf = new SearchFeature(newG, interactionMode);
                                 sf.setGeometryType(AbstractNewFeature.geomTypes.MULTIPOLYGON);
                                 return sf;
                             }
 
                             @Override
                             protected void done() {
                                 try {
                                     final SearchFeature search = get();
                                     if (search != null) {
                                         searchListener.search(search);
                                     }
                                 } catch (final Exception e) {
                                     LOG.error("Exception in Background Thread", e); // NOI18N
                                 }
                             }
                         });
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchEllipseAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchEllipseAction"); // NOI18N
                     }
                     mappingComponent.setInteractionMode(interactionMode);
                     searchListener.setMode(CreateSearchGeometryListener.ELLIPSE);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchPolylineAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchPolylineAction"); // NOI18N
                     }
                     mappingComponent.setInteractionMode(interactionMode);
                     searchListener.setMode(CreateSearchGeometryListener.LINESTRING);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchRedoAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("redoSearchAction"); // NOI18N
                     }
 
                     searchListener.redoLastSearch();
                     mappingComponent.setInteractionMode(interactionMode);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchShowLastFeatureAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("searchShowLastFeatureAction"); // NOI18N
                     }
 
                     searchListener.showLastFeature();
                     mappingComponent.setInteractionMode(interactionMode);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   interactionMode   DOCUMENT ME!
      * @param   mappingComponent  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Action createSearchBufferAction(final String interactionMode,
             final MappingComponent mappingComponent) {
         final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
             mappingComponent.getInputListener(interactionMode);
         return new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("bufferSearchGeometry");                 // NOI18N
                     }
                     final String s = (String)JOptionPane.showInputDialog(
                             StaticSwingTools.getParentFrame(mappingComponent),
                             "Geben Sie den Abstand des zu erzeugenden\n"   // NOI18N
                                     + "Puffers der letzten Suchgeometrie an.", // NOI18N
                             "Puffer",                                      // NOI18N
                             JOptionPane.PLAIN_MESSAGE,
                             null,
                             null,
                             "");                                           // NOI18N
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(s);
                     }
 
                     // , statt . ebenfalls erlauben
                     if (s.matches("\\d*,\\d*")) { // NOI18N
                         s.replace(",", ".");  // NOI18N
                     }
 
                     try {
                         final float buffer = Float.valueOf(s);
 
                         final SearchFeature lastFeature = searchListener.getLastSearchFeature();
 
                         if (lastFeature != null) {
                             // Geometrie-Daten holen
                             final Geometry geom = lastFeature.getGeometry();
 
                             // Puffer-Geometrie holen
                             final Geometry bufferGeom = geom.buffer(buffer);
 
                             // und setzen
                             lastFeature.setGeometry(bufferGeom);
 
                             // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder
                             // ähnliches mehr)
                             lastFeature.setGeometryType(AbstractNewFeature.geomTypes.POLYGON);
 
                             for (final Object feature
                                         : mappingComponent.getFeatureCollection().getAllFeatures()) {
                                 final PFeature sel = (PFeature)mappingComponent.getPFeatureHM().get(feature);
 
                                 if (sel.getFeature().equals(lastFeature)) {
                                     // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                                     // setzen
                                     sel.setCoordArr(bufferGeom.getCoordinates());
 
                                     // refresh
                                     sel.syncGeometry();
 
                                     final List v = new ArrayList();
                                     v.add(sel.getFeature());
                                     ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                             .fireFeaturesChanged(v);
                                 }
                             }
 
                             searchListener.search(lastFeature);
                             mappingComponent.setInteractionMode(interactionMode);
                         }
                     } catch (final NumberFormatException ex) {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(mappingComponent),
                             "The given value was not a floating point value.!",
                             "Error",
                             JOptionPane.ERROR_MESSAGE); // NOI18N
                     } catch (final Exception ex) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("", ex);      // NOI18N
                         }
                     }
                 }
             };
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         popMenSearch = new javax.swing.JPopupMenu();
         mniSearchRectangle1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolygon1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchEllipse1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolyline1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         jSeparator12 = new javax.swing.JSeparator();
         mniSearchCidsFeature1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchShowLastFeature1 = new javax.swing.JMenuItem();
         mniSearchRedo1 = new javax.swing.JMenuItem();
         mniSearchBuffer1 = new javax.swing.JMenuItem();
         buttonGroup1 = new javax.swing.ButtonGroup();
 
         popMenSearch.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
 
                 @Override
                 public void popupMenuCanceled(final javax.swing.event.PopupMenuEvent evt) {
                 }
                 @Override
                 public void popupMenuWillBecomeInvisible(final javax.swing.event.PopupMenuEvent evt) {
                 }
                 @Override
                 public void popupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) {
                     popMenSearchPopupMenuWillBecomeVisible(evt);
                 }
             });
 
         mniSearchRectangle1.setAction(searchRectangleAction);
         mniSearchRectangle1.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchRectangle1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchRectangle1.text")); // NOI18N
         mniSearchRectangle1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png")));      // NOI18N
         popMenSearch.add(mniSearchRectangle1);
 
         mniSearchPolygon1.setAction(searchPolygonAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchPolygon1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchPolygon1.text")); // NOI18N
         mniSearchPolygon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png")));        // NOI18N
         popMenSearch.add(mniSearchPolygon1);
 
         mniSearchEllipse1.setAction(searchEllipseAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchEllipse1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchEllipse1.text")); // NOI18N
         mniSearchEllipse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png")));        // NOI18N
         popMenSearch.add(mniSearchEllipse1);
 
         mniSearchPolyline1.setAction(searchPolylineAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchPolyline1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchPolyline1.text")); // NOI18N
         mniSearchPolyline1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png")));       // NOI18N
         popMenSearch.add(mniSearchPolyline1);
         popMenSearch.add(jSeparator12);
 
         mniSearchCidsFeature1.setAction(searchCidsFeatureAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchCidsFeature1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchCidsFeature1.text")); // NOI18N
         mniSearchCidsFeature1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png")));        // NOI18N
         popMenSearch.add(mniSearchCidsFeature1);
 
         mniSearchShowLastFeature1.setAction(searchShowLastFeatureAction);
         mniSearchShowLastFeature1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.CTRL_MASK));
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchShowLastFeature1,
             org.openide.util.NbBundle.getMessage(
                 GeoSearchButton.class,
                 "GeoSearchButton.mniSearchShowLastFeature1.text"));        // NOI18N
         mniSearchShowLastFeature1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 GeoSearchButton.class,
                 "GeoSearchButton.mniSearchShowLastFeature1.toolTipText")); // NOI18N
         popMenSearch.add(mniSearchShowLastFeature1);
 
         mniSearchRedo1.setAction(searchRedoAction);
         mniSearchRedo1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.ALT_MASK
                         | java.awt.event.InputEvent.CTRL_MASK));
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchRedo1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchRedo1.text")); // NOI18N
         mniSearchRedo1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 GeoSearchButton.class,
                 "GeoSearchButton.mniSearchRedo1.toolTipText"));                                                  // NOI18N
         popMenSearch.add(mniSearchRedo1);
 
         mniSearchBuffer1.setAction(searchBufferAction);
         mniSearchBuffer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png")));         // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchBuffer1,
             org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.mniSearchBuffer1.text")); // NOI18N
         mniSearchBuffer1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 GeoSearchButton.class,
                 "GeoSearchButton.mniSearchBuffer1.toolTipText"));                                                  // NOI18N
         popMenSearch.add(mniSearchBuffer1);
 
         setAction(searchAction);
         setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png")));            // NOI18N
         setToolTipText(org.openide.util.NbBundle.getMessage(GeoSearchButton.class, "GeoSearchButton.toolTipText")); // NOI18N
     }                                                                                                               // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
    private void popMenSearchPopupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) { //GEN-FIRST:event_popMenSearchPopupMenuWillBecomeVisible
         searchMenuSelectedAction.actionPerformed(new ActionEvent(popMenSearch, ActionEvent.ACTION_PERFORMED, null));
    }                                                                                                 //GEN-LAST:event_popMenSearchPopupMenuWillBecomeVisible
 
     @Override
     public void propertyChange(final PropertyChangeEvent evt) {
         if (evt.getSource().equals(searchListener)) {
             if (AbstractCreateSearchGeometryListener.PROPERTY_LAST_FEATURE.equals(evt.getPropertyName())) {
                 setLastFeature(searchListener.getLastSearchFeature());
             } else if (AbstractCreateSearchGeometryListener.PROPERTY_MODE.equals(evt.getPropertyName())) {
                 setSelected(true);
                 setModeSelection(searchListener.getMode());
                 setButtonIcon(searchListener.getMode());
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  lastFeature  DOCUMENT ME!
      */
     private void setLastFeature(final AbstractNewFeature lastFeature) {
         if (lastFeature == null) {
             mniSearchShowLastFeature1.setIcon(null);
             mniSearchShowLastFeature1.setEnabled(false);
             mniSearchRedo1.setIcon(null);
             mniSearchRedo1.setEnabled(false);
             mniSearchBuffer1.setEnabled(false);
         } else {
             switch (lastFeature.getGeometryType()) {
                 case ELLIPSE: {
                     mniSearchRedo1.setIcon(mniSearchEllipse1.getIcon());
                     break;
                 }
 
                 case LINESTRING: {
                     mniSearchRedo1.setIcon(mniSearchPolyline1.getIcon());
                     break;
                 }
 
                 case POLYGON: {
                     mniSearchRedo1.setIcon(mniSearchPolygon1.getIcon());
                     break;
                 }
 
                 case RECTANGLE: {
                     mniSearchRedo1.setIcon(mniSearchRectangle1.getIcon());
                     break;
                 }
             }
 
             mniSearchRedo1.setEnabled(true);
             mniSearchBuffer1.setEnabled(true);
             mniSearchShowLastFeature1.setIcon(mniSearchRedo1.getIcon());
             mniSearchShowLastFeature1.setEnabled(true);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  mode  DOCUMENT ME!
      */
     private void setButtonIcon(final String mode) {
         if (CreateSearchGeometryListener.RECTANGLE.equals(mode)) {
             setIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
             setSelectedIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
         } else if (CreateSearchGeometryListener.POLYGON.equals(mode)) {
             setIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchPolygon.png")));   // NOI18N
             setSelectedIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchPolygon.png")));   // NOI18N
         } else if (CreateSearchGeometryListener.ELLIPSE.equals(mode)) {
             setIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchEllipse.png")));   // NOI18N
             setSelectedIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchEllipse.png")));   // NOI18N
         } else if (CreateSearchGeometryListener.LINESTRING.equals(mode)) {
             setIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchPolyline.png")));  // NOI18N
             setSelectedIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchPolyline.png")));  // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  mode  DOCUMENT ME!
      */
     private void setModeSelection(final String mode) {
         mniSearchRectangle1.setSelected(CreateSearchGeometryListener.RECTANGLE.equals(mode));
         mniSearchPolygon1.setSelected(CreateSearchGeometryListener.POLYGON.equals(mode));
         mniSearchEllipse1.setSelected(CreateSearchGeometryListener.ELLIPSE.equals(mode));
         mniSearchPolyline1.setSelected(CreateSearchGeometryListener.LINESTRING.equals(mode));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  metaSearch  DOCUMENT ME!
      */
     public void initSearchTopicMenues(final MetaSearch metaSearch) {
         if ((metaSearch.getSearchTopics() != null) && !metaSearch.getSearchTopics().isEmpty()) {
             popMenSearch.add(new JSeparator());
             for (final SearchTopic searchTopic : metaSearch.getSearchTopics()) {
                 popMenSearch.add(new StayOpenCheckBoxMenuItem(
                         (Action)searchTopic,
                         javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                         Color.WHITE));
 
                 searchTopic.addPropertyChangeListener((PropertyChangeListener)mappingComponent.getInputListener(
                         interactionMode));
             }
         }
     }
 }
