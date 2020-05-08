 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.wunda_blau.search;
 
 import Sirius.navigator.actiontag.ActionTagProtected;
 import Sirius.navigator.search.CidsSearchExecutor;
 import Sirius.navigator.search.dynamic.SearchControlListener;
 import Sirius.navigator.search.dynamic.SearchControlPanel;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.search.CidsServerSearch;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.NbBundle;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.Box;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.custom.objecteditors.wunda_blau.VermessungFlurstueckSelectionDialog;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsVermessungRissSearchStatement;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.CidsBeanDropTarget;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.search.clientstuff.CidsWindowSearch;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.features.SearchFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchCreateSearchGeometryListener;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.cismap.tools.gui.CidsBeanDropJPopupMenuButton;
 
 import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
 import de.cismet.tools.gui.JPopupMenuButton;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsWindowSearch.class)
 public class VermessungRissWindowSearch extends javax.swing.JPanel implements CidsWindowSearch,
     ActionTagProtected,
     SearchControlListener,
     PropertyChangeListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(VermessungRissWindowSearch.class);
 
     private static final String ACTION_TAG = "custom.alkis.windowsearch";
 
     //~ Instance fields --------------------------------------------------------
 
     private final boolean geoSearchEnabled;
     private final MetaClass metaClass;
     private ImageIcon icon;
     private final MappingComponent mappingComponent;
     private SearchControlPanel pnlSearchCancel;
     private CidsBeanDropJPopupMenuButton btnGeoSearch;
     private VermessungFlurstueckSelectionDialog flurstueckDialog;
     private DefaultListModel flurstuecksvermessungFilterModel = null;
     private ImageIcon icoPluginRectangle;
     private ImageIcon icoPluginPolygon;
     private ImageIcon icoPluginEllipse;
     private ImageIcon icoPluginPolyline;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup bgrSearch;
     private javax.swing.JButton btnAddFlurstueck;
     private javax.swing.JButton btnFilterSchluessel505To508;
     private javax.swing.JButton btnFilterSchluesselAll;
     private javax.swing.JButton btnRemoveFlurstueck;
     private javax.swing.JCheckBox chkFilterSchluessel501;
     private javax.swing.JCheckBox chkFilterSchluessel502;
     private javax.swing.JCheckBox chkFilterSchluessel503;
     private javax.swing.JCheckBox chkFilterSchluessel504;
     private javax.swing.JCheckBox chkFilterSchluessel505;
     private javax.swing.JCheckBox chkFilterSchluessel506;
     private javax.swing.JCheckBox chkFilterSchluessel507;
     private javax.swing.JCheckBox chkFilterSchluessel508;
     private javax.swing.JCheckBox chkFilterSchluessel600;
     private javax.swing.JCheckBox chkSearchInCismap;
     private javax.swing.JLabel lblBlatt;
     private javax.swing.JLabel lblFilterRissWildcardPercent;
     private javax.swing.JLabel lblFilterRissWildcardUnderline;
     private javax.swing.JLabel lblFilterRissWildcards;
     private javax.swing.JLabel lblFlur;
     private javax.swing.JLabel lblGemarkung;
     private javax.swing.JLabel lblSchluessel;
     private javax.swing.JList lstFlurstuecke;
     private javax.swing.JMenuItem mniSearchBuffer;
     private javax.swing.JRadioButtonMenuItem mniSearchCidsFeature;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle;
     private javax.swing.JMenuItem mniSearchRedo;
     private javax.swing.JMenuItem mniSearchShowLastFeature;
     private javax.swing.JPanel pnlButtons;
     private javax.swing.JPanel pnlFilterFlurstuecke;
     private javax.swing.JPanel pnlFilterRiss;
     private javax.swing.JPanel pnlFilterRissWildcards;
     private javax.swing.JPanel pnlFilterSchluessel;
     private javax.swing.JPanel pnlFilterSchluesselControls;
     private javax.swing.JPopupMenu popMenSearch;
     private javax.swing.JScrollPane scpFlurstuecke;
     private javax.swing.JSeparator sepSearchGeometries;
     private javax.swing.JTextField txtBlatt;
     private javax.swing.JTextField txtFlur;
     private javax.swing.JTextField txtGemarkung;
     private javax.swing.JTextField txtSchluessel;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form VermessungRissWindowSearch.
      */
     public VermessungRissWindowSearch() {
         mappingComponent = CismapBroker.getInstance().getMappingComponent();
         geoSearchEnabled = mappingComponent != null;
         metaClass = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "vermessung_riss");
 
         byte[] iconDataFromMetaclass = new byte[] {};
 
         if (metaClass != null) {
             iconDataFromMetaclass = metaClass.getIconData();
         }
 
         if (iconDataFromMetaclass.length > 0) {
             LOG.info("Using icon from metaclass.");
             icon = new ImageIcon(metaClass.getIconData());
         } else {
             LOG.warn("Metaclass icon is not set. Trying to load default icon.");
             final URL urlToIcon = getClass().getResource("/de/cismet/cids/custom/wunda_blau/search/search.png");
 
             if (urlToIcon != null) {
                 icon = new ImageIcon(urlToIcon);
             } else {
                 icon = new ImageIcon(new byte[] {});
             }
         }
 
         icoPluginRectangle = new ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png"));
         icoPluginPolygon = new ImageIcon(getClass().getResource("/images/pluginSearchPolygon.png"));
         icoPluginEllipse = new ImageIcon(getClass().getResource("/images/pluginSearchEllipse.png"));
         icoPluginPolyline = new ImageIcon(getClass().getResource("/images/pluginSearchPolyline.png"));
 
         initComponents();
 
         pnlSearchCancel = new SearchControlPanel(this);
         final Dimension max = pnlSearchCancel.getMaximumSize();
         final Dimension min = pnlSearchCancel.getMinimumSize();
         final Dimension pre = pnlSearchCancel.getPreferredSize();
         pnlSearchCancel.setMaximumSize(new java.awt.Dimension(
                 new Double(max.getWidth()).intValue(),
                 new Double(max.getHeight() + 6).intValue()));
         pnlSearchCancel.setMinimumSize(new java.awt.Dimension(
                 new Double(min.getWidth()).intValue(),
                 new Double(min.getHeight() + 6).intValue()));
         pnlSearchCancel.setPreferredSize(new java.awt.Dimension(
                 new Double(pre.getWidth() + 6).intValue(),
                 new Double(pre.getHeight() + 6).intValue()));
         pnlButtons.add(pnlSearchCancel);
 
         if (geoSearchEnabled) {
             final VermessungRissCreateSearchGeometryListener vermessungRissCreateSearchGeometryListener =
                 new VermessungRissCreateSearchGeometryListener(mappingComponent, new VermessungRissSearchTooltip(icon));
             vermessungRissCreateSearchGeometryListener.addPropertyChangeListener(this);
 
             pnlButtons.add(Box.createHorizontalStrut(5));
 
             btnGeoSearch = new CidsBeanDropJPopupMenuButton(
                     VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY,
                     mappingComponent,
                     null);
             btnGeoSearch.addActionListener(new java.awt.event.ActionListener() {
 
                     @Override
                     public void actionPerformed(final java.awt.event.ActionEvent evt) {
                         btnGeoSearchActionPerformed(evt);
                     }
                 });
             btnGeoSearch.setToolTipText(org.openide.util.NbBundle.getMessage(
                     VermessungRissWindowSearch.class,
                     "VermessungRissWindowSearch.btnGeoSearch.toolTipText"));
             ((JPopupMenuButton)btnGeoSearch).setPopupMenu(popMenSearch);
             btnGeoSearch.setFocusPainted(false);
             pnlButtons.add(btnGeoSearch);
 
             visualizeSearchMode((MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                     MappingComponent.CREATE_SEARCH_POLYGON));
             mappingComponent.getInteractionButtonGroup().add(btnGeoSearch);
             new CidsBeanDropTarget(btnGeoSearch);
 
             ((CidsBeanDropJPopupMenuButton)btnGeoSearch).setTargetIcon(new javax.swing.ImageIcon(
                     getClass().getResource("/images/pluginSearchTarget.png")));
         } else {
             chkSearchInCismap.setVisible(false);
         }
 
         flurstuecksvermessungFilterModel = new DefaultListModel();
         lstFlurstuecke.setModel(flurstuecksvermessungFilterModel);
 
         flurstueckDialog = new VermessungFlurstueckSelectionDialog(false) {
 
                 @Override
                 public void okHook() {
                     final List<CidsBean> result = getCurrentListToAdd();
                     if (result.size() > 0) {
                         flurstuecksvermessungFilterModel.addElement(result.get(0));
                     }
                 }
             };
 
         flurstueckDialog.pack();
         flurstueckDialog.setLocationRelativeTo(this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         bgrSearch = new javax.swing.ButtonGroup();
         popMenSearch = new javax.swing.JPopupMenu();
         mniSearchRectangle = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolygon = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchEllipse = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolyline = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         sepSearchGeometries = new javax.swing.JSeparator();
         mniSearchCidsFeature = new javax.swing.JRadioButtonMenuItem();
         mniSearchShowLastFeature = new javax.swing.JMenuItem();
         mniSearchRedo = new javax.swing.JMenuItem();
         mniSearchBuffer = new javax.swing.JMenuItem();
         pnlFilterRiss = new javax.swing.JPanel();
         lblSchluessel = new javax.swing.JLabel();
         lblGemarkung = new javax.swing.JLabel();
         lblFlur = new javax.swing.JLabel();
         lblBlatt = new javax.swing.JLabel();
         txtSchluessel = new javax.swing.JTextField();
         txtGemarkung = new javax.swing.JTextField();
         txtFlur = new javax.swing.JTextField();
         txtBlatt = new javax.swing.JTextField();
         pnlFilterRissWildcards = new javax.swing.JPanel();
         lblFilterRissWildcards = new javax.swing.JLabel();
         lblFilterRissWildcardPercent = new javax.swing.JLabel();
         lblFilterRissWildcardUnderline = new javax.swing.JLabel();
         pnlFilterSchluessel = new javax.swing.JPanel();
         chkFilterSchluessel501 = new javax.swing.JCheckBox();
         chkFilterSchluessel502 = new javax.swing.JCheckBox();
         chkFilterSchluessel503 = new javax.swing.JCheckBox();
         chkFilterSchluessel504 = new javax.swing.JCheckBox();
         chkFilterSchluessel505 = new javax.swing.JCheckBox();
         chkFilterSchluessel506 = new javax.swing.JCheckBox();
         chkFilterSchluessel507 = new javax.swing.JCheckBox();
         chkFilterSchluessel508 = new javax.swing.JCheckBox();
         chkFilterSchluessel600 = new javax.swing.JCheckBox();
         pnlFilterSchluesselControls = new javax.swing.JPanel();
         btnFilterSchluesselAll = new javax.swing.JButton();
         btnFilterSchluessel505To508 = new javax.swing.JButton();
         pnlFilterFlurstuecke = new javax.swing.JPanel();
         scpFlurstuecke = new javax.swing.JScrollPane();
         lstFlurstuecke = new javax.swing.JList();
         btnAddFlurstueck = new javax.swing.JButton();
         btnRemoveFlurstueck = new javax.swing.JButton();
         chkSearchInCismap = new javax.swing.JCheckBox();
         pnlButtons = new javax.swing.JPanel();
 
         bgrSearch.add(mniSearchRectangle);
         mniSearchRectangle.setSelected(true);
         mniSearchRectangle.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchRectangle.text"));                                         // NOI18N
         mniSearchRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
         mniSearchRectangle.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchRectangleActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchRectangle);
 
         bgrSearch.add(mniSearchPolygon);
         mniSearchPolygon.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchPolygon.text"));                                       // NOI18N
         mniSearchPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         mniSearchPolygon.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchPolygonActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchPolygon);
 
         bgrSearch.add(mniSearchEllipse);
         mniSearchEllipse.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchEllipse.text"));                                       // NOI18N
         mniSearchEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
         mniSearchEllipse.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchEllipseActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchEllipse);
 
         bgrSearch.add(mniSearchPolyline);
         mniSearchPolyline.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchPolyline.text"));                                        // NOI18N
         mniSearchPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
         mniSearchPolyline.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchPolylineActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchPolyline);
         popMenSearch.add(sepSearchGeometries);
 
         mniSearchCidsFeature.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchCidsFeature.text"));                                       // NOI18N
         mniSearchCidsFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         mniSearchCidsFeature.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchCidsFeatureActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchCidsFeature);
 
         mniSearchShowLastFeature.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchShowLastFeature.text"));        // NOI18N
         mniSearchShowLastFeature.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchShowLastFeature.toolTipText")); // NOI18N
         mniSearchShowLastFeature.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchShowLastFeatureActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchShowLastFeature);
 
         mniSearchRedo.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchRedo.text"));        // NOI18N
         mniSearchRedo.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchRedo.toolTipText")); // NOI18N
         mniSearchRedo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchRedoActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchRedo);
 
         mniSearchBuffer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
         mniSearchBuffer.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchBuffer.text"));                                      // NOI18N
         mniSearchBuffer.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.mniSearchBuffer.toolTipText"));                               // NOI18N
         mniSearchBuffer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchBufferActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchBuffer);
 
         setLayout(new java.awt.GridBagLayout());
 
         pnlFilterRiss.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     VermessungRissWindowSearch.class,
                     "VermessungRissWindowSearch.pnlFilterRiss.border.title"))); // NOI18N
         pnlFilterRiss.setLayout(new java.awt.GridBagLayout());
 
         lblSchluessel.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblSchluessel.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(lblSchluessel, gridBagConstraints);
 
         lblGemarkung.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblGemarkung.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(lblGemarkung, gridBagConstraints);
 
         lblFlur.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblFlur.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(lblFlur, gridBagConstraints);
 
         lblBlatt.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblBlatt.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(lblBlatt, gridBagConstraints);
 
         txtSchluessel.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.txtSchluessel.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(txtSchluessel, gridBagConstraints);
 
         txtGemarkung.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.txtGemarkung.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 70;
         gridBagConstraints.weightx = 0.3;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(txtGemarkung, gridBagConstraints);
 
         txtFlur.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.txtFlur.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(txtFlur, gridBagConstraints);
 
         txtBlatt.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.txtBlatt.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 100;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRiss.add(txtBlatt, gridBagConstraints);
 
         pnlFilterRissWildcards.setLayout(new java.awt.GridBagLayout());
 
         lblFilterRissWildcards.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblFilterRissWildcards.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterRissWildcards.add(lblFilterRissWildcards, gridBagConstraints);
 
         lblFilterRissWildcardPercent.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblFilterRissWildcardPercent.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         pnlFilterRissWildcards.add(lblFilterRissWildcardPercent, gridBagConstraints);
 
         lblFilterRissWildcardUnderline.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.lblFilterRissWildcardUnderline.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 5);
         pnlFilterRissWildcards.add(lblFilterRissWildcardUnderline, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         pnlFilterRiss.add(pnlFilterRissWildcards, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlFilterRiss, gridBagConstraints);
 
         pnlFilterSchluessel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     VermessungRissWindowSearch.class,
                     "VermessungRissWindowSearch.pnlFilterSchluessel.border.title"))); // NOI18N
         pnlFilterSchluessel.setLayout(new java.awt.GridBagLayout());
 
         chkFilterSchluessel501.setSelected(true);
         chkFilterSchluessel501.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel501.text"));        // NOI18N
         chkFilterSchluessel501.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel501.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.2;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         pnlFilterSchluessel.add(chkFilterSchluessel501, gridBagConstraints);
 
         chkFilterSchluessel502.setSelected(true);
         chkFilterSchluessel502.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel502.text"));        // NOI18N
         chkFilterSchluessel502.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel502.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.2;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlFilterSchluessel.add(chkFilterSchluessel502, gridBagConstraints);
 
         chkFilterSchluessel503.setSelected(true);
         chkFilterSchluessel503.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel503.text"));        // NOI18N
         chkFilterSchluessel503.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel503.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.2;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlFilterSchluessel.add(chkFilterSchluessel503, gridBagConstraints);
 
         chkFilterSchluessel504.setSelected(true);
         chkFilterSchluessel504.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel504.text"));        // NOI18N
         chkFilterSchluessel504.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel504.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.2;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlFilterSchluessel.add(chkFilterSchluessel504, gridBagConstraints);
 
         chkFilterSchluessel505.setSelected(true);
         chkFilterSchluessel505.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel505.text"));        // NOI18N
         chkFilterSchluessel505.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel505.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.2;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         pnlFilterSchluessel.add(chkFilterSchluessel505, gridBagConstraints);
 
         chkFilterSchluessel506.setSelected(true);
         chkFilterSchluessel506.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel506.text"));        // NOI18N
         chkFilterSchluessel506.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel506.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         pnlFilterSchluessel.add(chkFilterSchluessel506, gridBagConstraints);
 
         chkFilterSchluessel507.setSelected(true);
         chkFilterSchluessel507.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel507.text"));        // NOI18N
         chkFilterSchluessel507.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel507.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlFilterSchluessel.add(chkFilterSchluessel507, gridBagConstraints);
 
         chkFilterSchluessel508.setSelected(true);
         chkFilterSchluessel508.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel508.text"));        // NOI18N
         chkFilterSchluessel508.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel508.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlFilterSchluessel.add(chkFilterSchluessel508, gridBagConstraints);
 
         chkFilterSchluessel600.setSelected(true);
         chkFilterSchluessel600.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel600.text"));        // NOI18N
         chkFilterSchluessel600.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkFilterSchluessel600.toolTipText")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlFilterSchluessel.add(chkFilterSchluessel600, gridBagConstraints);
 
         btnFilterSchluesselAll.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.btnFilterSchluesselAll.text")); // NOI18N
         btnFilterSchluesselAll.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnFilterSchluesselAllActionPerformed(evt);
                 }
             });
         pnlFilterSchluesselControls.add(btnFilterSchluesselAll);
 
         btnFilterSchluessel505To508.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.btnFilterSchluessel505To508.text")); // NOI18N
         btnFilterSchluessel505To508.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnFilterSchluessel505To508ActionPerformed(evt);
                 }
             });
         pnlFilterSchluesselControls.add(btnFilterSchluessel505To508);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         pnlFilterSchluessel.add(pnlFilterSchluesselControls, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlFilterSchluessel, gridBagConstraints);
 
         pnlFilterFlurstuecke.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     VermessungRissWindowSearch.class,
                     "VermessungRissWindowSearch.pnlFilterFlurstuecke.border.title"))); // NOI18N
         pnlFilterFlurstuecke.setLayout(new java.awt.GridBagLayout());
 
         scpFlurstuecke.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         scpFlurstuecke.setMinimumSize(new java.awt.Dimension(266, 138));
         scpFlurstuecke.setOpaque(false);
 
         lstFlurstuecke.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                     lstFlurstueckeValueChanged(evt);
                 }
             });
         scpFlurstuecke.setViewportView(lstFlurstuecke);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weighty = 0.1;
         pnlFilterFlurstuecke.add(scpFlurstuecke, gridBagConstraints);
 
         btnAddFlurstueck.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddFlurstueck.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.btnAddFlurstueck.text"));                                          // NOI18N
         btnAddFlurstueck.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.btnAddFlurstueck.toolTipText"));                                   // NOI18N
         btnAddFlurstueck.setFocusPainted(false);
         btnAddFlurstueck.setMaximumSize(new java.awt.Dimension(43, 25));
         btnAddFlurstueck.setMinimumSize(new java.awt.Dimension(43, 25));
         btnAddFlurstueck.setPreferredSize(new java.awt.Dimension(43, 25));
         btnAddFlurstueck.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddFlurstueckActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 2);
         pnlFilterFlurstuecke.add(btnAddFlurstueck, gridBagConstraints);
 
         btnRemoveFlurstueck.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveFlurstueck.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.btnRemoveFlurstueck.text"));                                          // NOI18N
         btnRemoveFlurstueck.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.btnRemoveFlurstueck.toolTipText"));                                   // NOI18N
         btnRemoveFlurstueck.setEnabled(false);
         btnRemoveFlurstueck.setFocusPainted(false);
         btnRemoveFlurstueck.setMaximumSize(new java.awt.Dimension(43, 25));
         btnRemoveFlurstueck.setMinimumSize(new java.awt.Dimension(43, 25));
         btnRemoveFlurstueck.setPreferredSize(new java.awt.Dimension(43, 25));
         btnRemoveFlurstueck.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveFlurstueckActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 10, 10);
         pnlFilterFlurstuecke.add(btnRemoveFlurstueck, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlFilterFlurstuecke, gridBagConstraints);
 
         chkSearchInCismap.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissWindowSearch.class,
                 "VermessungRissWindowSearch.chkSearchInCismap.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(chkSearchInCismap, gridBagConstraints);
 
         pnlButtons.setLayout(new javax.swing.BoxLayout(pnlButtons, javax.swing.BoxLayout.LINE_AXIS));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlButtons, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchRectangleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchRectangleActionPerformed
         btnGeoSearch.setIcon(icoPluginRectangle);
         btnGeoSearch.setSelectedIcon(icoPluginRectangle);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                             VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY)).setMode(
                         CreateGeometryListenerInterface.RECTANGLE);
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchRectangleActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchPolygonActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchPolygonActionPerformed
         btnGeoSearch.setIcon(icoPluginPolygon);
         btnGeoSearch.setSelectedIcon(icoPluginPolygon);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                             VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY)).setMode(
                         CreateGeometryListenerInterface.POLYGON);
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchPolygonActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchEllipseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchEllipseActionPerformed
         btnGeoSearch.setIcon(icoPluginEllipse);
         btnGeoSearch.setSelectedIcon(icoPluginEllipse);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                             VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY)).setMode(
                         CreateGeometryListenerInterface.ELLIPSE);
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchEllipseActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchPolylineActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchPolylineActionPerformed
         btnGeoSearch.setIcon(icoPluginPolyline);
         btnGeoSearch.setSelectedIcon(icoPluginPolyline);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                             VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY)).setMode(
                         CreateGeometryListenerInterface.LINESTRING);
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchPolylineActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchCidsFeatureActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchCidsFeatureActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                     final VermessungRissCreateSearchGeometryListener searchListener =
                         ((VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                                 VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY));
 
                     de.cismet.tools.CismetThreadPool.execute(
                         new javax.swing.SwingWorker<SearchFeature, Void>() {
 
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
 
                                 final Geometry[] searchGeomsArr = searchGeoms.toArray(new Geometry[0]);
                                 final GeometryCollection coll =
                                     new GeometryFactory().createGeometryCollection(searchGeomsArr);
 
                                 final Geometry newG = coll.buffer(0.1d);
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("SearchGeom " + newG.toText());
                                 }
 
                                 final SearchFeature sf = new SearchFeature(newG);
                                 sf.setGeometryType(PureNewFeature.geomTypes.MULTIPOLYGON);
                                 return sf;
                             }
 
                             @Override
                             protected void done() {
                                 try {
                                     final SearchFeature search = get();
                                     if (search != null) {
                                         searchListener.search(search);
                                     }
                                 } catch (Exception e) {
                                     LOG.error("Exception in Background Thread", e);
                                 }
                             }
                         });
                 }
             });
     } //GEN-LAST:event_mniSearchCidsFeatureActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchShowLastFeatureActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchShowLastFeatureActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final VermessungRissCreateSearchGeometryListener searchListener =
                         (VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                             VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                     searchListener.showLastFeature();
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchShowLastFeatureActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchRedoActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchRedoActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final VermessungRissCreateSearchGeometryListener searchListener =
                         (VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                             VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                     searchListener.redoLastSearch();
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchRedoActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchBufferActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchBufferActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final String s = (String)JOptionPane.showInputDialog(
                             null,
                             "Geben Sie den Abstand des zu erzeugenden\n"       // NOI18N
                                     + "Puffers der letzten Suchgeometrie an.", // NOI18N
                             "Puffer",                                          // NOI18N
                             JOptionPane.PLAIN_MESSAGE,
                             null,
                             null,
                             "");                                               // NOI18N
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(s);
                     }
 
                     // , statt . ebenfalls erlauben
                     if (s.matches("\\d*,\\d*")) { // NOI18N
                         s.replace(",", ".");      // NOI18N
                     }
 
                     try {
                         final float buffer = Float.valueOf(s);
 
                         final VermessungRissCreateSearchGeometryListener searchListener =
                             (VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                                 VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                         final PureNewFeature lastFeature = searchListener.getLastSearchFeature();
 
                         if (lastFeature != null) {
                             // Geometrie-Daten holen
                             final Geometry geom = lastFeature.getGeometry();
 
                             // Puffer-Geometrie holen
                             final Geometry bufferGeom = geom.buffer(buffer);
 
                             // und setzen
                             lastFeature.setGeometry(bufferGeom);
 
                             // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder
                             // �hnliches mehr)
                             lastFeature.setGeometryType(PureNewFeature.geomTypes.POLYGON);
 
                             for (final Object feature : mappingComponent.getFeatureCollection().getAllFeatures()) {
                                 final PFeature sel = (PFeature)mappingComponent.getPFeatureHM().get(feature);
 
                                 if (sel.getFeature().equals(lastFeature)) {
                                     // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                                     // setzen
                                     sel.setCoordArr(bufferGeom.getCoordinates());
 
                                     // refresh
                                     sel.syncGeometry();
 
                                     final Vector v = new Vector();
                                     v.add(sel.getFeature());
                                     ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                             .fireFeaturesChanged(v);
                                 }
                             }
 
                             searchListener.search(lastFeature);
                             mappingComponent.setInteractionMode(
                                 VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                         }
                     } catch (NumberFormatException ex) {
                         JOptionPane.showMessageDialog(
                             null,
                             "The given value was not a floating point value.!",
                             "Error",
                             JOptionPane.ERROR_MESSAGE); // NOI18N
                     } catch (Exception ex) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("", ex);          // NOI18N
                         }
                     }
                 }
             });
     }                                                   //GEN-LAST:event_mniSearchBufferActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnFilterSchluesselAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFilterSchluesselAllActionPerformed
         chkFilterSchluessel501.setSelected(true);
         chkFilterSchluessel502.setSelected(true);
         chkFilterSchluessel503.setSelected(true);
         chkFilterSchluessel504.setSelected(true);
         chkFilterSchluessel505.setSelected(true);
         chkFilterSchluessel506.setSelected(true);
         chkFilterSchluessel507.setSelected(true);
         chkFilterSchluessel508.setSelected(true);
         chkFilterSchluessel600.setSelected(true);
     }                                                                                          //GEN-LAST:event_btnFilterSchluesselAllActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstFlurstueckeValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstFlurstueckeValueChanged
         if (!evt.getValueIsAdjusting()) {
             btnRemoveFlurstueck.setEnabled(lstFlurstuecke.getSelectedIndex() > -1);
         }
     }                                                                                         //GEN-LAST:event_lstFlurstueckeValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddFlurstueckActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddFlurstueckActionPerformed
         final List<CidsBean> result = new ArrayList<CidsBean>(1);
         flurstueckDialog.setCurrentListToAdd(result);
         flurstueckDialog.setVisible(true);
     }                                                                                    //GEN-LAST:event_btnAddFlurstueckActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveFlurstueckActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveFlurstueckActionPerformed
         final Object[] selection = lstFlurstuecke.getSelectedValues();
         for (final Object flurstueck : selection) {
             flurstuecksvermessungFilterModel.removeElement(flurstueck);
         }
     }                                                                                       //GEN-LAST:event_btnRemoveFlurstueckActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnFilterSchluessel505To508ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFilterSchluessel505To508ActionPerformed
         chkFilterSchluessel501.setSelected(false);
         chkFilterSchluessel502.setSelected(false);
         chkFilterSchluessel503.setSelected(false);
         chkFilterSchluessel504.setSelected(false);
         chkFilterSchluessel505.setSelected(true);
         chkFilterSchluessel506.setSelected(true);
         chkFilterSchluessel507.setSelected(true);
         chkFilterSchluessel508.setSelected(true);
         chkFilterSchluessel600.setSelected(false);
     }                                                                                               //GEN-LAST:event_btnFilterSchluessel505To508ActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnGeoSearchActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mappingComponent.setInteractionMode(
                         VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY);
                 }
             });
     }
     /**
      * DOCUMENT ME!
      */
     protected void visualizeSearchMode() {
         if (geoSearchEnabled) {
             visualizeSearchMode((VermessungRissCreateSearchGeometryListener)mappingComponent.getInputListener(
                     VermessungRissCreateSearchGeometryListener.VERMESSUNGRISS_CREATE_SEARCH_GEOMETRY));
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  searchListener  DOCUMENT ME!
      */
     protected void visualizeSearchMode(final CreateSearchGeometryListener searchListener) {
         final String searchMode = searchListener.getMode();
         final PureNewFeature lastGeometry = searchListener.getLastSearchFeature();
 
         if (CreateGeometryListenerInterface.RECTANGLE.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginRectangle);
             btnGeoSearch.setSelectedIcon(icoPluginRectangle);
         } else if (CreateGeometryListenerInterface.POLYGON.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginPolygon);
             btnGeoSearch.setSelectedIcon(icoPluginPolygon);
         } else if (CreateGeometryListenerInterface.ELLIPSE.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginEllipse);
             btnGeoSearch.setSelectedIcon(icoPluginEllipse);
         } else if (CreateGeometryListenerInterface.LINESTRING.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginPolyline);
             btnGeoSearch.setSelectedIcon(icoPluginPolyline);
         }
 
         mniSearchRectangle.setSelected(CreateGeometryListenerInterface.RECTANGLE.equals(searchMode));
         mniSearchPolygon.setSelected(CreateGeometryListenerInterface.POLYGON.equals(searchMode));
         mniSearchEllipse.setSelected(CreateGeometryListenerInterface.ELLIPSE.equals(searchMode));
         mniSearchPolyline.setSelected(CreateGeometryListenerInterface.LINESTRING.equals(searchMode));
 
         if (lastGeometry == null) {
             mniSearchShowLastFeature.setIcon(null);
             mniSearchShowLastFeature.setEnabled(false);
             mniSearchRedo.setIcon(null);
             mniSearchRedo.setEnabled(false);
             mniSearchBuffer.setEnabled(false);
         } else {
             switch (lastGeometry.getGeometryType()) {
                 case ELLIPSE: {
                     mniSearchRedo.setIcon(mniSearchEllipse.getIcon());
                     break;
                 }
 
                 case LINESTRING: {
                     mniSearchRedo.setIcon(mniSearchPolyline.getIcon());
                     break;
                 }
 
                 case POLYGON: {
                     mniSearchRedo.setIcon(mniSearchPolygon.getIcon());
                     break;
                 }
 
                 case RECTANGLE: {
                     mniSearchRedo.setIcon(mniSearchRectangle.getIcon());
                     break;
                 }
             }
 
             mniSearchRedo.setEnabled(true);
             mniSearchShowLastFeature.setIcon(mniSearchRedo.getIcon());
             mniSearchShowLastFeature.setEnabled(true);
             mniSearchBuffer.setEnabled(true);
         }
     }
 
     @Override
     public void propertyChange(final PropertyChangeEvent evt) {
         if (AbstractCreateSearchGeometryListener.PROPERTY_FORGUI_LAST_FEATURE.equals(evt.getPropertyName())
                     || AbstractCreateSearchGeometryListener.PROPERTY_FORGUI_MODE.equals(evt.getPropertyName())) {
             visualizeSearchMode();
         }
 
         if (VermessungRissCreateSearchGeometryListener.ACTION_SEARCH_STARTED.equals(evt.getPropertyName())) {
             if ((evt.getNewValue() != null) && (evt.getNewValue() instanceof Geometry)) {
                 final CidsServerSearch cidsServerSearch = getServerSearch((Geometry)evt.getNewValue());
                 CidsSearchExecutor.searchAndDisplayResultsWithDialog(cidsServerSearch);
             }
         }
     }
 
     @Override
     public JComponent getSearchWindowComponent() {
         return this;
     }
 
     @Override
     public CidsServerSearch getServerSearch() {
         return getServerSearch(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   geometry  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public CidsServerSearch getServerSearch(final Geometry geometry) {
         final String schluessel = txtSchluessel.getText();
         final String gemarkung = txtGemarkung.getText();
         final String flur = txtFlur.getText();
         final String blatt = txtBlatt.getText();
         final Collection<String> schluesselCollection = new LinkedList<String>();
 
         if (chkFilterSchluessel501.isSelected()) {
             schluesselCollection.add("501");
         }
         if (chkFilterSchluessel502.isSelected()) {
             schluesselCollection.add("502");
         }
         if (chkFilterSchluessel503.isSelected()) {
             schluesselCollection.add("503");
         }
         if (chkFilterSchluessel504.isSelected()) {
             schluesselCollection.add("504");
         }
         if (chkFilterSchluessel505.isSelected()) {
             schluesselCollection.add("505");
         }
         if (chkFilterSchluessel506.isSelected()) {
             schluesselCollection.add("506");
         }
         if (chkFilterSchluessel507.isSelected()) {
             schluesselCollection.add("507");
         }
         if (chkFilterSchluessel508.isSelected()) {
             schluesselCollection.add("508");
         }
         if (chkFilterSchluessel600.isSelected()) {
             schluesselCollection.add("600");
         }
 
         final Collection<Map<String, String>> flurstuecke = new LinkedList<Map<String, String>>();
 
         for (int i = 0; i < flurstuecksvermessungFilterModel.size(); i++) {
             final CidsBean flurstuecksvermessungBean = (CidsBean)flurstuecksvermessungFilterModel.getElementAt(i);
             final Map<String, String> flurstueckMap = new HashMap<String, String>();
 
             try {
                 if (flurstuecksvermessungBean.getProperty("veraenderungsart") instanceof CidsBean) {
                     flurstueckMap.put(
                         CidsVermessungRissSearchStatement.FLURSTUECK_VERAENDERUNGSART,
                         flurstuecksvermessungBean.getProperty("veraenderungsart.id").toString());
                 }
 
                 final CidsBean flurstueckBean = (CidsBean)flurstuecksvermessungBean.getProperty("flurstueck");
 
                 if ("VERMESSUNG_FLURSTUECK_KICKER".equalsIgnoreCase(
                                 flurstueckBean.getMetaObject().getMetaClass().getTableName())) {
                     if (flurstueckBean.getProperty("gemarkung") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_GEMARKUNG,
                             flurstueckBean.getProperty("gemarkung.id").toString());
                     }
                     if (flurstueckBean.getProperty("flur") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_FLUR,
                             flurstueckBean.getProperty("flur").toString());
                     }
                     if (flurstueckBean.getProperty("zaehler") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_ZAEHLER,
                             flurstueckBean.getProperty("zaehler").toString());
                     }
                     if (flurstueckBean.getProperty("nenner") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_NENNER,
                             flurstueckBean.getProperty("nenner").toString());
                     }
                 } else if ("FLURSTUECK".equalsIgnoreCase(
                                 flurstueckBean.getMetaObject().getMetaClass().getTableName())) {
                     if (flurstueckBean.getProperty("gemarkungs_nr") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_GEMARKUNG,
                             flurstueckBean.getProperty("gemarkungs_nr").toString());
                     }
                     if (flurstueckBean.getProperty("flur") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_FLUR,
                             flurstueckBean.getProperty("flur").toString());
                     }
                     if (flurstueckBean.getProperty("fstnr_z") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_ZAEHLER,
                             flurstueckBean.getProperty("fstnr_z").toString());
                     }
                     if (flurstueckBean.getProperty("fstnr_n") != null) {
                         flurstueckMap.put(
                             CidsVermessungRissSearchStatement.FLURSTUECK_NENNER,
                             flurstueckBean.getProperty("fstnr_n").toString());
                     }
                 }
 
                 flurstuecke.add(flurstueckMap);
             } catch (Exception ex) {
                 LOG.error("Can not parse information from Flurstueck bean: " + flurstuecksvermessungBean, ex);
             }
         }
 
         Geometry geometryToSearchFor = null;
         if (geometry != null) {
             geometryToSearchFor = geometry;
         } else {
             if (chkSearchInCismap.isSelected()) {
                 geometryToSearchFor =
                     ((XBoundingBox)CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBox())
                             .getGeometry();
             }
         }
 
         String geometryString = null;
         if (geometryToSearchFor != null) {
             final Geometry transformedBoundingBox = CrsTransformer.transformToDefaultCrs(geometryToSearchFor);
             transformedBoundingBox.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
             geometryString = PostGisGeometryFactory.getPostGisCompliantDbString(transformedBoundingBox);
         }
 
         return new CidsVermessungRissSearchStatement(
                 schluessel,
                 gemarkung,
                 flur,
                 blatt,
                 schluesselCollection,
                 geometryString,
                 flurstuecke);
     }
 
     @Override
     public ImageIcon getIcon() {
         return icon;
     }
 
     @Override
     public String getName() {
         return NbBundle.getMessage(VermessungRissWindowSearch.class, "VermessungRissWindowSearch.name");
     }
 
     @Override
     public boolean checkActionTag() {
         boolean result = false;
 
         try {
            result = ObjectRendererUtils.checkActionTag(ACTION_TAG);
         } catch (Exception ex) {
             LOG.warn("Could not determine if user is admin.", ex);
         }
 
         return result;
     }
 
     @Override
     public CidsServerSearch assembleSearch() {
         return getServerSearch();
     }
 
     @Override
     public void searchStarted() {
     }
 
     @Override
     public void searchDone(final int result) {
     }
 
     @Override
     public void searchCanceled() {
     }
 
     @Override
     public boolean suppressEmptyResultMessage() {
         return false;
     }
 }
