 /*
  * Copyright (C) 2011 cismet GmbH
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * AlkisBuchungsblattAggregationRenderer.java
  *
  * Created on 18.07.2011, 16:28:22
  */
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanAggregationRenderer;
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.FeatureCollection;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.MultipleDownload;
 import de.cismet.tools.gui.downloadmanager.SingleDownload;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import org.apache.log4j.Logger;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author jweintraut
  */
 public class AlkisBuchungsblattAggregationRenderer extends javax.swing.JPanel implements CidsBeanAggregationRenderer {
     private static final Logger LOG = Logger.getLogger(AlkisBuchungsblattAggregationRenderer.class);
 
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW = "custom.alkis.product.bestandsnachweis_nrw";
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM = "custom.alkis.product.bestandsnachweis_kom";
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN = "custom.alkis.product.bestandsnachweis_kom_intern";
     
     private static final Color[] COLORS = new Color[]{
         new Color(247, 150, 70),
         new Color(155, 187, 89),
         new Color(128, 100, 162),
         new Color(75, 172, 198),
         new Color(192, 80, 77)
     };
     
     private static volatile boolean initialisedMap = false;
     
     private List<CidsBeanWrapper> cidsBeanWrappers;
     private BuchungsblattTableModel tableModel;
     private MappingComponent map;
     private CidsBeanWrapper selectedCidsBeanWrapper;
     private Thread mapThread;
 
     /** Creates new form AlkisBuchungsblattAggregationRenderer */
     public AlkisBuchungsblattAggregationRenderer() {
         tableModel = new BuchungsblattTableModel();
         initComponents();
         
         map = new MappingComponent();
         pnlMap.add(map, BorderLayout.CENTER);
         tblBuchungsblaetter.setDefaultRenderer(Color.class, new ColorRenderer());
         tblBuchungsblaetter.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                 
                 if(lsm.isSelectionEmpty()) {
                     selectedCidsBeanWrapper = null;
                 } else {
                     selectedCidsBeanWrapper = tableModel.get(lsm.getLeadSelectionIndex());
                 }
                 changeMap();
             }
         });
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         pnlButtons = new RoundedPanel();
         srpHeaderButtons = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderButtons = new javax.swing.JLabel();
         jxlBestandsnachweisNRW = new org.jdesktop.swingx.JXHyperlink();
         jxlBestandsnachweisKommunal = new org.jdesktop.swingx.JXHyperlink();
         jxlBestandsnachweisKommunalIntern = new org.jdesktop.swingx.JXHyperlink();
         pnlBuchungsblaetter = new RoundedPanel();
         srpHeaderBuchungsblaetter = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderBuchungsblaetter = new javax.swing.JLabel();
         scpBuchungsblaetter = new javax.swing.JScrollPane();
         tblBuchungsblaetter = new javax.swing.JTable();
         pnlMap = new javax.swing.JPanel();
 
         setLayout(new java.awt.GridBagLayout());
 
         pnlButtons.setOpaque(false);
         pnlButtons.setLayout(new java.awt.GridBagLayout());
 
         srpHeaderButtons.setBackground(java.awt.Color.darkGray);
         srpHeaderButtons.setLayout(new java.awt.GridBagLayout());
 
         lblHeaderButtons.setForeground(java.awt.Color.white);
         lblHeaderButtons.setText(org.openide.util.NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.lblHeaderButtons.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeaderButtons.add(lblHeaderButtons, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         pnlButtons.add(srpHeaderButtons, gridBagConstraints);
 
         jxlBestandsnachweisNRW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlBestandsnachweisNRW.setText(org.openide.util.NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.jxlBestandsnachweisNRW.text")); // NOI18N
         jxlBestandsnachweisNRW.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlBestandsnachweisNRWActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlBestandsnachweisNRW, gridBagConstraints);
 
         jxlBestandsnachweisKommunal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlBestandsnachweisKommunal.setText(org.openide.util.NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.jxlBestandsnachweisKommunal.text")); // NOI18N
         jxlBestandsnachweisKommunal.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlBestandsnachweisKommunalActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlBestandsnachweisKommunal, gridBagConstraints);
 
         jxlBestandsnachweisKommunalIntern.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlBestandsnachweisKommunalIntern.setText(org.openide.util.NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.jxlBestandsnachweisKommunalIntern.text")); // NOI18N
         jxlBestandsnachweisKommunalIntern.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlBestandsnachweisKommunalInternActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlBestandsnachweisKommunalIntern, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
         add(pnlButtons, gridBagConstraints);
 
         pnlBuchungsblaetter.setMinimumSize(new java.awt.Dimension(309, 67));
         pnlBuchungsblaetter.setOpaque(false);
        pnlBuchungsblaetter.setPreferredSize(new java.awt.Dimension(309, 446));
         pnlBuchungsblaetter.setLayout(new java.awt.GridBagLayout());
 
         srpHeaderBuchungsblaetter.setBackground(java.awt.Color.darkGray);
         srpHeaderBuchungsblaetter.setLayout(new java.awt.GridBagLayout());
 
         lblHeaderBuchungsblaetter.setForeground(java.awt.Color.white);
         lblHeaderBuchungsblaetter.setText(org.openide.util.NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.lblHeaderBuchungsblaetter.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeaderBuchungsblaetter.add(lblHeaderBuchungsblaetter, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlBuchungsblaetter.add(srpHeaderBuchungsblaetter, gridBagConstraints);
 
         scpBuchungsblaetter.setPreferredSize(new java.awt.Dimension(250, 402));
 
         tblBuchungsblaetter.setModel(tableModel);
         tblBuchungsblaetter.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         tblBuchungsblaetter.setShowVerticalLines(false);
         tblBuchungsblaetter.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 tblBuchungsblaetterFocusLost(evt);
             }
         });
         scpBuchungsblaetter.setViewportView(tblBuchungsblaetter);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.35;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlBuchungsblaetter.add(scpBuchungsblaetter, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
         add(pnlBuchungsblaetter, gridBagConstraints);
 
         pnlMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlMap.setLayout(new java.awt.BorderLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         add(pnlMap, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     private void jxlBestandsnachweisNRWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlBestandsnachweisNRWActionPerformed
         downloadEinzelnachweisProduct(jxlBestandsnachweisNRW.getText(), AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_NRW_PDF, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW);
 }//GEN-LAST:event_jxlBestandsnachweisNRWActionPerformed
 
     private void jxlBestandsnachweisKommunalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlBestandsnachweisKommunalActionPerformed
         downloadEinzelnachweisProduct(jxlBestandsnachweisKommunal.getText(), AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_PDF, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM);
 }//GEN-LAST:event_jxlBestandsnachweisKommunalActionPerformed
 
     private void jxlBestandsnachweisKommunalInternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlBestandsnachweisKommunalInternActionPerformed
         downloadEinzelnachweisProduct(jxlBestandsnachweisKommunalIntern.getText(), AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_INTERN_PDF, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN);
 }//GEN-LAST:event_jxlBestandsnachweisKommunalInternActionPerformed
 
     private void tblBuchungsblaetterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblBuchungsblaetterFocusLost
         map.gotoInitialBoundingBox();
         tblBuchungsblaetter.clearSelection();
     }//GEN-LAST:event_tblBuchungsblaetterFocusLost
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXHyperlink jxlBestandsnachweisKommunal;
     private org.jdesktop.swingx.JXHyperlink jxlBestandsnachweisKommunalIntern;
     private org.jdesktop.swingx.JXHyperlink jxlBestandsnachweisNRW;
     private javax.swing.JLabel lblHeaderBuchungsblaetter;
     private javax.swing.JLabel lblHeaderButtons;
     private javax.swing.JPanel pnlBuchungsblaetter;
     private javax.swing.JPanel pnlButtons;
     private javax.swing.JPanel pnlMap;
     private javax.swing.JScrollPane scpBuchungsblaetter;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeaderBuchungsblaetter;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeaderButtons;
     private javax.swing.JTable tblBuchungsblaetter;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public Collection<CidsBean> getCidsBeans() {
         final Collection<CidsBean> result = new LinkedList<CidsBean>();
         
         for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
             result.add(cidsBeanWrapper.getCidsBean());
         }
         
         return result;
     }
 
     @Override
     public void setCidsBeans(Collection<CidsBean> cidsBeans) {
         if(cidsBeans != null) {
             int colorIndex = 0;
             cidsBeanWrappers = new LinkedList<CidsBeanWrapper>();
             
             for(CidsBean cidsBean : cidsBeans) {
                 cidsBeanWrappers.add(new CidsBeanWrapper(cidsBean, true));
             }            
             Collections.sort(cidsBeanWrappers);
             for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
                 cidsBeanWrapper.setColor(COLORS[colorIndex]);
                 colorIndex = (colorIndex + 1) % COLORS.length;
             }
             
             tableModel.setCidsBeans(cidsBeanWrappers);
             initMap();
             
             if(tblBuchungsblaetter != null && tblBuchungsblaetter.getColumnModel() != null) {
                 TableColumn column = tblBuchungsblaetter.getColumnModel().getColumn(0);
                 if(column != null) {
                     column.setPreferredWidth(20);
                 }
                 column = tblBuchungsblaetter.getColumnModel().getColumn(3);
                 if(column != null) {
                     column.setPreferredWidth(15);
                 }
             }
             
             changeButtonAvailability(cidsBeanWrappers.size() > 0);
         }
     }
 
     @Override
     public void dispose() {
         map.dispose();
     }
 
     @Override
     public String getTitle() {
         return NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.title", (cidsBeanWrappers != null ? cidsBeanWrappers.size() : "0"));
     }
 
     @Override
     public void setTitle(String title) {
         //NOP
     }
     
     private void initMap() {
         mapThread = new Thread(new InitialiseMapRunnable());
         if (EventQueue.isDispatchThread()) {
             mapThread.start();
         } else {
             EventQueue.invokeLater(mapThread);
         }
         
         revalidate();
         repaint();
     }
     
     private void changeMap() {
         if(mapThread != null && mapThread.isAlive()) {
             if(initialisedMap) {
                 //Map is initialised. Can be changed.
                 mapThread.interrupt();
             } else {
                 //Initialising the map is still running. Don't change the map now.
                 return;
             }
         }
         
         mapThread = new Thread(new ChangeMapRunnable());
         if (EventQueue.isDispatchThread()) {
             mapThread.start();
         } else {
             EventQueue.invokeLater(mapThread);
         }
         
         revalidate();
         repaint();
     }
     
     private void changeButtonAvailability(final boolean enable) {
         jxlBestandsnachweisNRW.setEnabled(enable);
         jxlBestandsnachweisKommunal.setEnabled(enable);
         jxlBestandsnachweisKommunalIntern.setEnabled(enable);
     }
     
     private void downloadEinzelnachweisProduct(String downloadTitle, String product, String actionTag) {
         if(!ObjectRendererUtils.checkActionTag(actionTag)) {
             showNoProductPermissionWarning();
             //return;
         }
         
         if(!DownloadManagerDialog.showAskingForUserTitle(StaticSwingTools.getParentFrame(this))) {
             return;
         }
         final String jobname = DownloadManagerDialog.getJobname();
         
         List<SingleDownload> downloads = new LinkedList<SingleDownload>();
         
         for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
             if(!cidsBeanWrapper.isSelected()) {
                 continue;
             }
             
             String queryID = getCompleteBuchungsblattCode(cidsBeanWrapper.getCidsBean());
             URL url = null;
             
             if(queryID == null || queryID.trim().length() <= 0) {
                 continue;
             }
             
             queryID = AlkisUtils.escapeHtmlSpaces(queryID);
             
             try {
                 url = AlkisUtils.PRODUCTS.productEinzelNachweisUrl(queryID, product);
                 
                 if (url != null) {
                     downloads.add(new SingleDownload(url, "", jobname, downloadTitle, product, ".pdf"));
                 }
                 
             } catch (Exception ex) {
                 ObjectRendererUtils.showExceptionWindowToUser(
                         "Fehler beim Aufruf des Produkts: " + product,
                         ex,
                         AlkisBuchungsblattAggregationRenderer.this);
                 LOG.error("The URL to download product '" + product + "' (actionTag: " + actionTag + ") could not be constructed.", ex);
             }
         }
         
         if(downloads.size() > 1) {
             DownloadManager.instance().add(new MultipleDownload(downloads, jobname));
         } else if(downloads.size() == 1) {
             DownloadManager.instance().add(downloads.get(0));
         }
     }
     
     private String fixBuchungslattCode(final String buchungsblattcode) {
         String result = "";
         
         if (buchungsblattcode != null) {
             final StringBuffer buchungsblattcodeBuffer = new StringBuffer(buchungsblattcode);
             
             // Fix SICAD-API-strangeness...
             while (buchungsblattcodeBuffer.length() < 14) {
                 buchungsblattcodeBuffer.append(" ");
             }
             
             result = buchungsblattcodeBuffer.toString();
         }
         
         return result;
     }
 
     private String getCompleteBuchungsblattCode(CidsBean cidsBean) {
         String result = "";
         
         if (cidsBean != null) {
             final Object buchungsblattcode = cidsBean.getProperty("buchungsblattcode");
             
             if (buchungsblattcode != null) {
                 result = fixBuchungslattCode(buchungsblattcode.toString());
             }
         }
         
         return result;
     }
 
     private void showNoProductPermissionWarning() {
         JOptionPane.showMessageDialog(this, "Sie besitzen keine Berechtigung zur Erzeugung dieses Produkts!");
     }
     
     private class BuchungsblattTableModel extends AbstractTableModel {
         private int selectedCidsBeans = 0;
         
         @Override
         public int getRowCount() {
             if(cidsBeanWrappers == null) {
                 return 0;
             }
             
             return cidsBeanWrappers.size();
         }
 
         @Override
         public int getColumnCount() {
             if(cidsBeanWrappers == null) {
                 return 0;
             }
             
             return 4;
         }
 
         @Override
         public Class<?> getColumnClass(int columnIndex) {
             if(columnIndex == 0) {
                 return Boolean.class;
             } else if(columnIndex == 3) {
                 return Color.class;
             } else {
                 return String.class;
             }
         }
 
         @Override
         public String getColumnName(int column) {
             return NbBundle.getMessage(AlkisBuchungsblattAggregationRenderer.class, "AlkisBuchungsblattAggregationRenderer.BuchungsblattTableModel.getColumnName(" + column + ")");
         }
         
         @Override
         public Object getValueAt(int rowIndex, int columnIndex) {
             if(cidsBeanWrappers == null) {
                 return null;
             }
             
             CidsBeanWrapper cidsBeanWrapper = cidsBeanWrappers.get(rowIndex);
             if(columnIndex == 0) {
                 return cidsBeanWrapper.isSelected();
             } else if(columnIndex == 1) {
                 return cidsBeanWrapper.getBlattart();
             }else if(columnIndex == 2) {
                 return cidsBeanWrapper.getBuchungsblattcode();
             } else {
                 return cidsBeanWrapper.getColor();
             }
         }
         
         @Override
         public boolean isCellEditable(int row, int column) {
             return column == 0;
         }
         
         public void setCidsBeans(List<CidsBeanWrapper> cidsBeans) {
             if(cidsBeans != null) {
                 selectedCidsBeans = cidsBeanWrappers.size();
                 fireTableStructureChanged();
             }
         }
     
         @Override
         public void setValueAt(Object value, int row, int column) {
             if(column != 0) {
                 return;
             }
             
             CidsBeanWrapper cidsBeanWrapper = cidsBeanWrappers.get(row);
             cidsBeanWrapper.setSelected(!cidsBeanWrapper.isSelected());
             if(cidsBeanWrapper.isSelected()) {
                 selectedCidsBeans++;
             } else {
                 selectedCidsBeans--;
             }
             
             fireTableRowsUpdated(row, row);
             changeMap();
             changeButtonAvailability(selectedCidsBeans > 0);
         }
         
         public CidsBeanWrapper get(int index) {
             return cidsBeanWrappers.get(index);
         }
     }
     
     private class InitialiseMapRunnable implements Runnable {
         @Override
         public void run() {
             initialisedMap = false;
             
             final ActiveLayerModel mappingModel = new ActiveLayerModel();
             mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
             mappingModel.addHome(getBoundingBox());
             
             final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisConstants.COMMONS.MAP_CALL_STRING));
             swms.setName("Buchungsblatt");
             
             // add the raster layer to the model
             mappingModel.addLayer(swms);
             // set the model
             map.setMappingModel(mappingModel);
             // initial positioning of the map
             final int duration = map.getAnimationDuration();
             map.setAnimationDuration(0);
             map.gotoInitialBoundingBox();
             // interaction mode
             map.setInteractionMode(MappingComponent.ZOOM);
             // finally when all configurations are done ...
             map.unlock();
             map.setInteractionMode("MUTE");
             
             for(final CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
                 map.getFeatureCollection().addFeatures(cidsBeanWrapper.getFeatures());
             }
             
             map.setAnimationDuration(duration);
             
             initialisedMap = true;
         }
         
         private XBoundingBox getBoundingBox() {
             XBoundingBox result = null;
             
             for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
                 for(Geometry geometry : cidsBeanWrapper.getGeometries()) {
                     if(result == null) {
                         result = new XBoundingBox(geometry.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
                         result.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                         result.setMetric(true);
                     } else {
                         XBoundingBox temp = new XBoundingBox(geometry.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
                         temp.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                         temp.setMetric(true);
 
                         if(temp.getX1() < result.getX1()) {
                             result.setX1(temp.getX1());
                         }
                         if(temp.getY1() < result.getY1()) {
                             result.setY1(temp.getY1());
                         }
                         if(temp.getX2() > result.getX2()) {
                             result.setX2(temp.getX2());
                         }
                         if(temp.getY2() > result.getY2()) {
                             result.setY2(temp.getY2());
                         }
                     }
                 }
             }
             
             return result;
         }
     }
     
     private class ChangeMapRunnable implements Runnable {
         @Override
         public void run() {
             final GeometryCollection geoCollection = new GeometryCollection(selectedCidsBeanWrapper.getGeometries().toArray(
                         new Geometry[selectedCidsBeanWrapper.getGeometries().size()]),
                         new GeometryFactory());
             final XBoundingBox boxToGoto = new XBoundingBox(geoCollection.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
             boxToGoto.setX1(boxToGoto.getX1() - AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth());
             boxToGoto.setX2(boxToGoto.getX2() + AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth());
             boxToGoto.setY1(boxToGoto.getY1() - AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight());
             boxToGoto.setY2(boxToGoto.getY2() + AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight());
             map.gotoBoundingBox(boxToGoto, false, true, 500);
         }
     }
     
     private class ColorRenderer extends JLabel implements TableCellRenderer {
         public ColorRenderer() {
             setOpaque(true);
         }
 
         @Override
         public Component getTableCellRendererComponent(
                 JTable table, Object color,
                 boolean isSelected, boolean hasFocus,
                 int row, int column) {
             
             Color newColor = (Color) color;
             setBackground(newColor);
             
             return this;
         }
     }
     
     private class CidsBeanWrapper implements Comparable<CidsBeanWrapper> {
         private CidsBean cidsBean;
         private boolean selected;
         private Color color;
         private String blattart;
         private String buchungsblattcode;
         private Collection<Geometry> geometries;
         private Collection<StyledFeature> features;
         
         public CidsBeanWrapper(final CidsBean cidsBean, final boolean selected) {
             this.cidsBean = cidsBean;
             this.selected = selected;
             this.blattart = cidsBean.getProperty("blattart").toString();
             this.buchungsblattcode = cidsBean.getProperty("buchungsblattcode").toString();
             this.geometries = new LinkedList<Geometry>();
             this.features = new LinkedList<StyledFeature>();
             
             for(CidsBean landparcel : this.cidsBean.getBeanCollectionProperty("landparcels")) {
                 final Object geometry = landparcel.getProperty("geometrie.geo_field");
                 if (geometry instanceof Geometry) {
                     final Geometry transformedGeometry = CrsTransformer.transformToGivenCrs((Geometry) geometry, AlkisConstants.COMMONS.SRS_SERVICE);
                     
                     final StyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(transformedGeometry);
 
                     geometries.add(transformedGeometry);
                     features.add(dsf);
                 }
             }
         }
         
         public CidsBean getCidsBean() {
             return cidsBean;
         }
 
         public Color getColor() {
             return color;
         }
 
         public void setColor(Color color) {
             this.color = color;
             for(StyledFeature feature : features) {
                 feature.setFillingPaint(this.color);
             }
         }
         
         public boolean isSelected() {
             return selected;
         }
 
         public void setSelected(boolean selected) {
             this.selected = selected;
         }
         
         public String getBlattart() {
             return blattart;
         }
         
         public String getBuchungsblattcode() {
             return buchungsblattcode;
         }
         
         public Collection<Geometry> getGeometries() {
             return geometries;
         }
 
         public Collection<StyledFeature> getFeatures() {
             return features;
         }
         
         @Override
         public int compareTo(CidsBeanWrapper o) {
             final CidsBean cidsBean1 = cidsBean;
             final CidsBean cidsBean2 = o.cidsBean;
             
             if(cidsBean1 == null && cidsBean2 == null) {
                 return 0;
             } else if(cidsBean1 == null) {
                 return -1;
             } else if(cidsBean2 == null) {
                 return 1;
             }
             
             int districtComparison = cidsBean1.getProperty("blattart").toString().compareTo(cidsBean2.getProperty("blattart").toString());
             
             if(districtComparison != 0) {
                 return districtComparison;
             } else {
                 return cidsBean1.getProperty("buchungsblattcode").toString().compareTo(cidsBean2.getProperty("buchungsblattcode").toString());
             }
         }
     }
 }
