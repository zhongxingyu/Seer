 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * BaulastWindowSearch.java
  *
  * Created on 09.12.2010, 14:33:10
  */
 package de.cismet.cids.custom.wunda_blau.search;
 
 import Sirius.navigator.actiontag.ActionTagProtected;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.search.dynamic.SearchControlListener;
 import Sirius.navigator.search.dynamic.SearchControlPanel;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.search.CidsServerSearch;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 
 import java.awt.EventQueue;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.custom.objecteditors.wunda_blau.FlurstueckSelectionDialoge;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.wunda_blau.search.server.BaulastSearchInfo;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsBaulastSearchStatement;
 import de.cismet.cids.custom.wunda_blau.search.server.FlurstueckInfo;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.DefaultBindableReferenceCombo;
 
 import de.cismet.cids.navigator.utils.CidsBeanDropListener;
 import de.cismet.cids.navigator.utils.CidsBeanDropTarget;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.search.clientstuff.CidsWindowSearch;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 /**
  * DOCUMENT ME!
  *
  * @author   stefan
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsWindowSearch.class)
 public class BaulastWindowSearch extends javax.swing.JPanel implements CidsWindowSearch,
     CidsBeanDropListener,
     ActionTagProtected,
     SearchControlListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaulastWindowSearch.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final MetaClass mc;
     private final ImageIcon icon;
     private final FlurstueckSelectionDialoge fsSelectionDialoge;
     private final DefaultListModel model;
     private SearchControlPanel pnlSearchCancel;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAddFS;
     private javax.swing.JButton btnFromMapFS;
     private javax.swing.JButton btnRemoveFS;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JComboBox cbArt;
     private javax.swing.JCheckBox chkBeguenstigt;
     private javax.swing.JCheckBox chkBelastet;
     private javax.swing.JCheckBox chkGeloescht;
     private javax.swing.JCheckBox chkGueltig;
     private javax.swing.JCheckBox chkKartenausschnitt;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JList lstFlurstueck;
     private javax.swing.JPanel panCommand;
     private javax.swing.JPanel panSearch;
     private javax.swing.JPanel pnlAttributeFilter;
     private javax.swing.JPanel pnlFlurstuecksfilter;
     private javax.swing.JPanel pnlSearchFor;
     private javax.swing.JRadioButton rbBaulastBlaetter;
     private javax.swing.JRadioButton rbBaulasten;
     private javax.swing.JTextField txtBlattnummer;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form BaulastWindowSearch.
      */
     public BaulastWindowSearch() {
         mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "ALB_BAULAST");
         icon = new ImageIcon(mc.getIconData());
         fsSelectionDialoge = new FlurstueckSelectionDialoge(false) {
 
                 @Override
                 public void okHook() {
                     final List<CidsBean> result = getCurrentListToAdd();
                     if (result.size() > 0) {
                         model.addElement(result.get(0));
                     }
                 }
             };
         initComponents();
         final MetaClass artMC = ClassCacheMultiple.getMetaClass("WUNDA_BLAU", "ALB_BAULAST_ART");
         final DefaultComboBoxModel cbArtModel;
         try {
             cbArtModel = DefaultBindableReferenceCombo.getModelByMetaClass(artMC, true);
             cbArt.setModel(cbArtModel);
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         model = new DefaultListModel();
         lstFlurstueck.setModel(model);
         AutoCompleteDecorator.decorate(cbArt);

         new CidsBeanDropTarget(this);
         fsSelectionDialoge.pack();
         fsSelectionDialoge.setLocationRelativeTo(this);
 //        cmdAbort.setVisible(false);
         pnlSearchCancel = new SearchControlPanel(this);
         panCommand.add(pnlSearchCancel);
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
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         panSearch = new javax.swing.JPanel();
         panCommand = new javax.swing.JPanel();
         pnlAttributeFilter = new javax.swing.JPanel();
         chkGeloescht = new javax.swing.JCheckBox();
         chkGueltig = new javax.swing.JCheckBox();
         cbArt = new javax.swing.JComboBox();
         txtBlattnummer = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         pnlFlurstuecksfilter = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         lstFlurstueck = new javax.swing.JList();
         btnAddFS = new javax.swing.JButton();
         btnRemoveFS = new javax.swing.JButton();
         btnFromMapFS = new javax.swing.JButton();
         chkBeguenstigt = new javax.swing.JCheckBox();
         chkBelastet = new javax.swing.JCheckBox();
         pnlSearchFor = new javax.swing.JPanel();
         rbBaulastBlaetter = new javax.swing.JRadioButton();
         rbBaulasten = new javax.swing.JRadioButton();
         jPanel5 = new javax.swing.JPanel();
         jPanel6 = new javax.swing.JPanel();
         chkKartenausschnitt = new javax.swing.JCheckBox();
 
         setMaximumSize(new java.awt.Dimension(325, 460));
         setMinimumSize(new java.awt.Dimension(325, 460));
         setPreferredSize(new java.awt.Dimension(325, 460));
         setLayout(new java.awt.BorderLayout());
 
         panSearch.setMaximumSize(new java.awt.Dimension(400, 150));
         panSearch.setMinimumSize(new java.awt.Dimension(400, 150));
         panSearch.setPreferredSize(new java.awt.Dimension(400, 150));
         panSearch.setLayout(new java.awt.GridBagLayout());
 
         panCommand.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSearch.add(panCommand, gridBagConstraints);
 
         pnlAttributeFilter.setBorder(javax.swing.BorderFactory.createTitledBorder("Attributfilter"));
         pnlAttributeFilter.setLayout(new java.awt.GridBagLayout());
 
         chkGeloescht.setSelected(true);
         chkGeloescht.setText("gelöscht / geschlossen");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAttributeFilter.add(chkGeloescht, gridBagConstraints);
 
         chkGueltig.setSelected(true);
         chkGueltig.setText("gültig");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAttributeFilter.add(chkGueltig, gridBagConstraints);
 
         cbArt.setEditable(true);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAttributeFilter.add(cbArt, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAttributeFilter.add(txtBlattnummer, gridBagConstraints);
 
         jLabel1.setText("Blattnummer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAttributeFilter.add(jLabel1, gridBagConstraints);
 
         jLabel2.setText("Art der Baulast:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAttributeFilter.add(jLabel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSearch.add(pnlAttributeFilter, gridBagConstraints);
 
         pnlFlurstuecksfilter.setBorder(javax.swing.BorderFactory.createTitledBorder("Flurstücksfilter"));
         pnlFlurstuecksfilter.setLayout(new java.awt.GridBagLayout());
 
         jScrollPane1.setMaximumSize(new java.awt.Dimension(200, 100));
         jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 100));
         jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 100));
 
         jScrollPane1.setViewportView(lstFlurstueck);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.gridheight = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFlurstuecksfilter.add(jScrollPane1, gridBagConstraints);
 
         btnAddFS.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit-add.png"))); // NOI18N
         btnAddFS.setToolTipText("Flurstück hinzufügen");
         btnAddFS.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddFSActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFlurstuecksfilter.add(btnAddFS, gridBagConstraints);
 
         btnRemoveFS.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit-delete.png"))); // NOI18N
         btnRemoveFS.setToolTipText("Ausgewählte Flurstücke entfernen");
         btnRemoveFS.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveFSActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFlurstuecksfilter.add(btnRemoveFS, gridBagConstraints);
 
         btnFromMapFS.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/bookmark-new.png"))); // NOI18N
         btnFromMapFS.setToolTipText("Selektierte Flurstücke aus Karte hinzufügen");
         btnFromMapFS.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnFromMapFSActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFlurstuecksfilter.add(btnFromMapFS, gridBagConstraints);
 
         chkBeguenstigt.setSelected(true);
         chkBeguenstigt.setText("begünstigt");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFlurstuecksfilter.add(chkBeguenstigt, gridBagConstraints);
 
         chkBelastet.setSelected(true);
         chkBelastet.setText("belastet");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFlurstuecksfilter.add(chkBelastet, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSearch.add(pnlFlurstuecksfilter, gridBagConstraints);
 
         pnlSearchFor.setBorder(javax.swing.BorderFactory.createTitledBorder("Suche nach"));
         pnlSearchFor.setLayout(new java.awt.GridBagLayout());
 
         buttonGroup1.add(rbBaulastBlaetter);
         rbBaulastBlaetter.setText("Baulastblätter");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSearchFor.add(rbBaulastBlaetter, gridBagConstraints);
 
         buttonGroup1.add(rbBaulasten);
         rbBaulasten.setText("Baulasten");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSearchFor.add(rbBaulasten, gridBagConstraints);
 
         jPanel5.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.weightx = 1.0;
         pnlSearchFor.add(jPanel5, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSearch.add(pnlSearchFor, gridBagConstraints);
 
         jPanel6.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.weighty = 1.0;
         panSearch.add(jPanel6, gridBagConstraints);
 
         chkKartenausschnitt.setText("<html>Nur im aktuellen<br>Kartenausschnitt suchen</html>");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         panSearch.add(chkKartenausschnitt, gridBagConstraints);
 
         add(panSearch, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddFSActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddFSActionPerformed
         final List<CidsBean> result = new ArrayList<CidsBean>(1);
         fsSelectionDialoge.setCurrentListToAdd(result);
         fsSelectionDialoge.setVisible(true);
     }                                                                            //GEN-LAST:event_btnAddFSActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveFSActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveFSActionPerformed
         final Object[] selection = lstFlurstueck.getSelectedValues();
         for (final Object o : selection) {
             model.removeElement(o);
         }
     }                                                                               //GEN-LAST:event_btnRemoveFSActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnFromMapFSActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFromMapFSActionPerformed
         final Collection<Feature> selFeatures = CismapBroker.getInstance()
                     .getMappingComponent()
                     .getFeatureCollection()
                     .getSelectedFeatures();
         for (final Feature feature : selFeatures) {
             if (feature instanceof CidsFeature) {
                 final CidsFeature cf = (CidsFeature)feature;
                 final MetaObject mo = cf.getMetaObject();
                 final String tableName = mo.getMetaClass().getTableName();
                 if ("FLURSTUECK".equalsIgnoreCase(tableName) || "ALB_FLURSTUECK_KICKER".equalsIgnoreCase(tableName)) {
 //                if ("FLURSTUECK".equalsIgnoreCase(tableName) || "ALB_FLURSTUECK_KICKER".equalsIgnoreCase(tableName) || "ALKIS_LANDPARCEL".equalsIgnoreCase(tableName)) {
                     model.addElement(mo.getBean());
                 }
             }
         }
     }                                                                                //GEN-LAST:event_btnFromMapFSActionPerformed
 
     @Override
     public ImageIcon getIcon() {
         return icon;
     }
 
     @Override
     public String getName() {
         return "Baulast Suche";
     }
 
     @Override
     public JComponent getSearchWindowComponent() {
         return this;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private BaulastSearchInfo getBaulastInfoFromGUI() {
         final BaulastSearchInfo bsi = new BaulastSearchInfo();
         bsi.setResult(rbBaulastBlaetter.isSelected() ? CidsBaulastSearchStatement.Result.BAULASTBLATT
                                                      : CidsBaulastSearchStatement.Result.BAULAST);
         if (chkKartenausschnitt.isSelected()) {
             final XBoundingBox bb = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                         .getCurrentBoundingBox();
             final Geometry g = CrsTransformer.transformToDefaultCrs(bb.getGeometry());
             bsi.setBounds(g.toText());
         }
         bsi.setBelastet(chkBelastet.isSelected());
         bsi.setBeguenstigt(chkBeguenstigt.isSelected());
         bsi.setUngueltig(chkGeloescht.isSelected());
         bsi.setGueltig(chkGueltig.isSelected());
         bsi.setBlattnummer(txtBlattnummer.getText());
         final Object art = cbArt.getSelectedItem();
         if (art != null) {
             bsi.setArt(art.toString());
         }
         return bsi;
     }
 
     @Override
     public CidsServerSearch getServerSearch() {
         final BaulastSearchInfo bsi = getBaulastInfoFromGUI();
         for (int i = 0; i < model.size(); ++i) {
             final CidsBean fsBean = (CidsBean)model.getElementAt(i);
             try {
                 if ("ALB_FLURSTUECK_KICKER".equalsIgnoreCase(fsBean.getMetaObject().getMetaClass().getTableName())) {
                     final FlurstueckInfo fi = new FlurstueckInfo((Integer)fsBean.getProperty("gemarkung"),
                             (String)fsBean.getProperty("flur"),
                             (String)fsBean.getProperty("zaehler"),
                             (String)fsBean.getProperty("nenner"));
                     bsi.getFlurstuecke().add(fi);
                 } else if ("FLURSTUECK".equalsIgnoreCase(fsBean.getMetaObject().getMetaClass().getTableName())) {
                     final CidsBean gemarkung = (CidsBean)fsBean.getProperty("gemarkungs_nr");
                     final FlurstueckInfo fi = new FlurstueckInfo((Integer)gemarkung.getProperty("gemarkungsnummer"),
                             (String)fsBean.getProperty("flur"),
                             String.valueOf(fsBean.getProperty("fstnr_z")),
                             String.valueOf(fsBean.getProperty("fstnr_n")));
                     bsi.getFlurstuecke().add(fi);
                 }
 //                else if ("ALKIS_LANDPARCEL".equalsIgnoreCase(fsBean.getMetaObject().getMetaClass().getTableName())) {
 //                //TODO: merke
 //                    FlurstueckInfo fi = new FlurstueckInfo(Integer.parseInt(String.valueOf(fsBean.getProperty("gemarkung"))), (String) fsBean.getProperty("flur"), String.valueOf(fsBean.getProperty("fstck_zaehler")), String.valueOf(fsBean.getProperty("fstck_nenner")));
 //                    bsi.getFlurstuecke().add(fi);
 //                }
             } catch (Exception ex) {
                 log.error("Can not parse information from Flurstueck bean: " + fsBean, ex);
             }
         }
         MetaClass mc = ClassCacheMultiple.getMetaClass("WUNDA_BLAU", "ALB_BAULAST");
         final int baulastClassID = mc.getID();
         mc = ClassCacheMultiple.getMetaClass("WUNDA_BLAU", "ALB_BAULASTBLATT");
         final int baulastblattClassID = mc.getID();
         return new CidsBaulastSearchStatement(bsi, baulastClassID, baulastblattClassID);
     }
 
     @Override
     public void beansDropped(final ArrayList<CidsBean> beans) {
         for (final CidsBean bean : beans) {
             if ("FLURSTUECK".equalsIgnoreCase(bean.getMetaObject().getMetaClass().getTableName())) {
 //            if ("FLURSTUECK".equalsIgnoreCase(bean.getMetaObject().getMetaClass().getTableName()) || "ALKIS_LANDPARCEL".equalsIgnoreCase(bean.getMetaObject().getMetaClass().getTableName())) {
                 model.addElement(bean);
             }
             lstFlurstueck.repaint();
         }
     }
 
     @Override
     public boolean checkActionTag() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "navigator.baulasten.search") != null;
         } catch (ConnectionException ex) {
             log.error("Can not validate ActionTag for Baulasten Suche!", ex);
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         DevelopmentTools.initSessionManagerFromRMIConnectionOnLocalhost(
             "WUNDA_BLAU",
             "Administratoren",
             "admin",
             "krissenich");
         final BaulastWindowSearch bws = new BaulastWindowSearch();
         DevelopmentTools.showTestFrame(bws, 800, 600);
     }
 
     @Override
     public CidsServerSearch assembleSearch() {
         CidsServerSearch result = null;
 
         final BaulastSearchInfo bsi = getBaulastInfoFromGUI();
         final boolean keineBlattNummer = (bsi.getBlattnummer() == null) || (bsi.getBlattnummer().trim().length() == 0);
         final boolean keineArt = (bsi.getArt() == null) || (bsi.getArt().trim().length() == 0);
         final boolean keinFlurstueck = lstFlurstueck.getModel().getSize() == 0;
         final boolean keinKartenausschnitt = !chkKartenausschnitt.isSelected();
 
         if (keineBlattNummer && keinKartenausschnitt && keineArt && keinFlurstueck) {
             JOptionPane.showMessageDialog(
                 this,
                 "Ihre Suchanfrage ist nicht plausibel. Bitte präzisieren Sie die\n"
                         + "Suchanfrage durch weitere Angaben im Attribut- und Flurstücksfilter.",
                 "Plausibilitätskontrolle",
                 JOptionPane.WARNING_MESSAGE);
         } else {
             result = getServerSearch();
         }
 
         return result;
     }
 
     @Override
     public void searchStarted() {
     }
 
     @Override
     public void searchDone(final Node[] result) {
         if ((result != null) && (result.length > 0)) {
             txtBlattnummer.setText("");
            cbArt.setSelectedItem("");
             chkKartenausschnitt.setSelected(false);
         }
     }
 
     @Override
     public void searchCancelled() {
     }
 
     @Override
     public boolean displaysEmptyResultMessage() {
         return true;
     }
 }
