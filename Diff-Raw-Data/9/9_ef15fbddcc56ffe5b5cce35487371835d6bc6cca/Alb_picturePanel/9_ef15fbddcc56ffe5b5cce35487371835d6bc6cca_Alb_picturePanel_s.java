 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * Alb_picturePanel.java
  *
  * Created on 11.12.2009, 14:49:40
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 import de.cismet.cids.custom.objectrenderer.utils.BaulastenPictureFinder;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.MD5Calculator;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollectionEvent;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;
 import de.cismet.tools.BrowserLauncher;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.StaticDecimalTools;
 import de.cismet.tools.collections.TypeSafeCollections;
 import de.cismet.tools.gui.MultiPagePictureReader;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.ListModel;
 import javax.swing.SwingWorker;
 
 /**
  *
  * @author srichter
  */
 public class Alb_picturePanel extends javax.swing.JPanel {
 
     // <editor-fold defaultstate="collapsed" desc="Static Variables">
     private static final String[] MD5_PROPERTY_NAMES = new String[]{"lageplan_md5", "textblatt_md5"};
     private static final int LAGEPLAN_DOCUMENT = 0;
     private static final int TEXTBLATT_DOCUMENT = 1;
     private static final int NO_SELECTION = -1;
     private static final Color KALIBRIERUNG_VORHANDEN = new Color(120,255,190);
     //
     private static final ListModel LADEN_MODEL = new DefaultListModel() {
 
         {
             add(0, "Wird geladen...");
         }
     };
     private static final ListModel FEHLER_MODEL = new DefaultListModel() {
 
         {
             add(0, "Lesefehler.");
         }
     };
 // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Instance Variables">
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alb_picturePanel.class);
     private MultiPagePictureReader pictureReader;
     private CidsBean cidsBean;
     private File[] documentFiles;
     private JButton[] documentButtons;
 //    private File planFile;
 //    private File textFile;
     private JButton currentSelectedButton;
     private static boolean alreadyWarnedAboutPermissionProblem = false;
     private final MessenFeatureCollectionListener messenListener;
     //
     private volatile int currentDocument = NO_SELECTION;
     private volatile int currentPage = NO_SELECTION;
     private final String[] expectedMD5Values;
 //    private String planFileMD5 = "";
 //    private String textFileMD5 = "";
     private String currentActualDocumentMD5 = "";
     private Map<Integer, Geometry> pageGeometries;
 
 // </editor-fold>
     /** Creates new form Alb_picturePanel */
     public Alb_picturePanel() {
         this.pageGeometries = TypeSafeCollections.newHashMap();
         this.documentFiles = new File[2];
         this.documentButtons = new JButton[documentFiles.length];
         initComponents();
         documentButtons[LAGEPLAN_DOCUMENT] = btnPlan;
         documentButtons[TEXTBLATT_DOCUMENT] = btnTextblatt;
         messenListener = new MessenFeatureCollectionListener();
         measureComponent.getFeatureCollection().addFeatureCollectionListener(messenListener);
         this.expectedMD5Values = new String[2];
     }
 
     // <editor-fold defaultstate="collapsed" desc="Public Methods">
     public void zoomToFeatureCollection() {
         measureComponent.zoomToFeatureCollection();
     }
 
     /**
      * @return the cidsBean
      */
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * @param cidsBean the cidsBean to set
      */
     public void setCidsBean(CidsBean cidsBean) {
         this.cidsBean = cidsBean;
         if (cidsBean != null) {
             for (int i = 0; i < MD5_PROPERTY_NAMES.length; ++i) {
                 Object propValObj = cidsBean.getProperty(MD5_PROPERTY_NAMES[i]);
                 if (propValObj instanceof String) {
                     expectedMD5Values[i] = (String) propValObj;
                 } else {
                     expectedMD5Values[i] = null;
                 }
             }
         }
         setCurrentDocumentNull();
         CismetThreadPool.execute(new FileSearchWorker());
 
     }
 
     //    @Override
 //    public void removeNotify() {
 //        //TODO: BUG! wird durchs Fenstermanagement auch umschalten
 //        //auf Vollbild aufgerufen!
 //        super.removeNotify();
 //        closeReader();
 //    }
 // </editor-fold>
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         btnGrpDocs = new javax.swing.ButtonGroup();
         buttonGrpMode = new javax.swing.ButtonGroup();
         panPicNavigation = new javax.swing.JPanel();
         spDocuments = new de.cismet.tools.gui.RoundedPanel();
         btnPlan = new javax.swing.JButton();
         btnTextblatt = new javax.swing.JButton();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel1 = new javax.swing.JLabel();
         rpSeiten = new de.cismet.tools.gui.RoundedPanel();
         scpPictureList = new javax.swing.JScrollPane();
         lstPictures = new javax.swing.JList();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel2 = new javax.swing.JLabel();
         rpMessdaten = new de.cismet.tools.gui.RoundedPanel();
         lblArea = new javax.swing.JLabel();
         lblDistance = new javax.swing.JLabel();
         lblTxtDistance = new javax.swing.JLabel();
         lblTxtArea = new javax.swing.JLabel();
         semiRoundedPanel5 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel6 = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         rpControls = new de.cismet.tools.gui.RoundedPanel();
         togPan = new javax.swing.JToggleButton();
         togZoom = new javax.swing.JToggleButton();
         togMessenLine = new javax.swing.JToggleButton();
         togMessenPoly = new javax.swing.JToggleButton();
         togCalibrate = new javax.swing.JToggleButton();
         btnHome = new javax.swing.JButton();
         semiRoundedPanel4 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel3 = new javax.swing.JLabel();
         btnPrint = new javax.swing.JButton();
         panCenter = new javax.swing.JPanel();
         measureComponent = new de.cismet.cismap.commons.gui.measuring.MeasuringComponent();
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblCurrentViewTitle = new javax.swing.JLabel();
 
         setOpaque(false);
         setLayout(new java.awt.BorderLayout());
 
         panPicNavigation.setMinimumSize(new java.awt.Dimension(140, 216));
         panPicNavigation.setOpaque(false);
         panPicNavigation.setPreferredSize(new java.awt.Dimension(140, 216));
         panPicNavigation.setLayout(new java.awt.GridBagLayout());
 
         spDocuments.setLayout(new java.awt.GridBagLayout());
 
         btnPlan.setText("Plan");
         btnPlan.setToolTipText("Plan");
         btnGrpDocs.add(btnPlan);
         btnPlan.setMaximumSize(new java.awt.Dimension(53, 33));
         btnPlan.setMinimumSize(new java.awt.Dimension(53, 33));
         btnPlan.setPreferredSize(new java.awt.Dimension(53, 33));
         btnPlan.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPlanActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         spDocuments.add(btnPlan, gridBagConstraints);
 
         btnTextblatt.setText("Textblatt");
         btnTextblatt.setToolTipText("Textblatt");
         btnGrpDocs.add(btnTextblatt);
         btnTextblatt.setMaximumSize(new java.awt.Dimension(53, 33));
         btnTextblatt.setMinimumSize(new java.awt.Dimension(53, 33));
         btnTextblatt.setPreferredSize(new java.awt.Dimension(53, 33));
         btnTextblatt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnTextblattActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
         spDocuments.add(btnTextblatt, gridBagConstraints);
 
         semiRoundedPanel2.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel2.setLayout(new java.awt.FlowLayout());
 
         jLabel1.setForeground(new java.awt.Color(255, 255, 255));
         jLabel1.setText("Dokumentauswahl");
         semiRoundedPanel2.add(jLabel1);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         spDocuments.add(semiRoundedPanel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 7);
         panPicNavigation.add(spDocuments, gridBagConstraints);
 
         scpPictureList.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         scpPictureList.setMinimumSize(new java.awt.Dimension(100, 150));
         scpPictureList.setOpaque(false);
         scpPictureList.setPreferredSize(new java.awt.Dimension(100, 150));
 
         lstPictures.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         lstPictures.setEnabled(false);
         lstPictures.setFixedCellWidth(75);
         lstPictures.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstPicturesValueChanged(evt);
             }
         });
         scpPictureList.setViewportView(lstPictures);
 
         rpSeiten.add(scpPictureList, java.awt.BorderLayout.CENTER);
 
         semiRoundedPanel3.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel3.setLayout(new java.awt.FlowLayout());
 
         jLabel2.setForeground(new java.awt.Color(255, 255, 255));
         jLabel2.setText("Seitenauswahl");
         semiRoundedPanel3.add(jLabel2);
 
         rpSeiten.add(semiRoundedPanel3, java.awt.BorderLayout.PAGE_START);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 7);
         panPicNavigation.add(rpSeiten, gridBagConstraints);
 
         rpMessdaten.setLayout(new java.awt.GridBagLayout());
 
         lblArea.setText("-");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
         rpMessdaten.add(lblArea, gridBagConstraints);
 
         lblDistance.setText("-");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
         rpMessdaten.add(lblDistance, gridBagConstraints);
 
        lblTxtDistance.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblTxtDistance.setText("Länge/Umfang:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         rpMessdaten.add(lblTxtDistance, gridBagConstraints);
 
         lblTxtArea.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblTxtArea.setText("Fläche:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         rpMessdaten.add(lblTxtArea, gridBagConstraints);
 
         semiRoundedPanel5.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel5.setLayout(new java.awt.FlowLayout());
 
         jLabel6.setForeground(new java.awt.Color(255, 255, 255));
         jLabel6.setText("Messdaten");
         semiRoundedPanel5.add(jLabel6);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         rpMessdaten.add(semiRoundedPanel5, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 7);
         panPicNavigation.add(rpMessdaten, gridBagConstraints);
 
         jPanel1.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.weighty = 1.0;
         panPicNavigation.add(jPanel1, gridBagConstraints);
 
         rpControls.setLayout(new java.awt.GridBagLayout());
 
         buttonGrpMode.add(togPan);
         togPan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/pan.gif"))); // NOI18N
         togPan.setSelected(true);
         togPan.setText("Verschieben");
         togPan.setToolTipText("Verschieben");
         togPan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togPan.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 togPanActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         rpControls.add(togPan, gridBagConstraints);
 
         buttonGrpMode.add(togZoom);
         togZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom.gif"))); // NOI18N
         togZoom.setText("Zoomen");
         togZoom.setToolTipText("Zoomen");
         togZoom.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togZoom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 togZoomActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         rpControls.add(togZoom, gridBagConstraints);
 
         buttonGrpMode.add(togMessenLine);
         togMessenLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/newLinestring.png"))); // NOI18N
         togMessenLine.setText("Messlinie");
         togMessenLine.setToolTipText("Messen (Linie)");
         togMessenLine.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togMessenLine.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 togMessenLineActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         rpControls.add(togMessenLine, gridBagConstraints);
 
         buttonGrpMode.add(togMessenPoly);
         togMessenPoly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/newPolygon.png"))); // NOI18N
         togMessenPoly.setText("Messfläche");
         togMessenPoly.setToolTipText("Messen (Polygon)");
         togMessenPoly.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togMessenPoly.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 togMessenPolyActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         rpControls.add(togMessenPoly, gridBagConstraints);
 
         buttonGrpMode.add(togCalibrate);
         togCalibrate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/screen.gif"))); // NOI18N
         togCalibrate.setText("Kalibrieren");
         togCalibrate.setToolTipText("Kalibrieren");
         togCalibrate.setEnabled(false);
         togCalibrate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togCalibrate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 togCalibrateActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         rpControls.add(togCalibrate, gridBagConstraints);
 
         btnHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/home.gif"))); // NOI18N
         btnHome.setText("Übersicht");
         btnHome.setToolTipText("Übersicht");
         btnHome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnHome.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnHomeActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         rpControls.add(btnHome, gridBagConstraints);
 
         semiRoundedPanel4.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel4.setLayout(new java.awt.FlowLayout());
 
         jLabel3.setForeground(new java.awt.Color(255, 255, 255));
         jLabel3.setText("Steuerung");
         semiRoundedPanel4.add(jLabel3);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         rpControls.add(semiRoundedPanel4, gridBagConstraints);
 
        btnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/frameprint.png"))); // NOI18N
        btnPrint.setText("Drucken");
        btnPrint.setToolTipText("Übersicht");
         btnPrint.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnPrint.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPrintActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
         rpControls.add(btnPrint, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 7);
         panPicNavigation.add(rpControls, gridBagConstraints);
 
         add(panPicNavigation, java.awt.BorderLayout.WEST);
 
         panCenter.setOpaque(false);
         panCenter.setLayout(new java.awt.BorderLayout());
         panCenter.add(measureComponent, java.awt.BorderLayout.CENTER);
 
         semiRoundedPanel1.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel1.setLayout(new java.awt.FlowLayout());
 
         lblCurrentViewTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblCurrentViewTitle.setText("Keine Auswahl");
         semiRoundedPanel1.add(lblCurrentViewTitle);
 
         panCenter.add(semiRoundedPanel1, java.awt.BorderLayout.NORTH);
 
         add(panCenter, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void lstPicturesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPicturesValueChanged
         if (!evt.getValueIsAdjusting()) {
             final Object selObj = lstPictures.getSelectedValue();
             if (selObj instanceof Integer) {
                 int pageNo = (Integer) selObj;
                 //page -> offset
                 CismetThreadPool.execute(new PictureSelectWorker(pageNo - 1));
             }
         }
 }//GEN-LAST:event_lstPicturesValueChanged
 
     private void btnPlanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlanActionPerformed
         loadPlan();
 }//GEN-LAST:event_btnPlanActionPerformed
 
     private void btnTextblattActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTextblattActionPerformed
         loadTextBlatt();
 }//GEN-LAST:event_btnTextblattActionPerformed
 
     private void togPanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togPanActionPerformed
         measureComponent.actionPan();
     }//GEN-LAST:event_togPanActionPerformed
 
     private void togMessenPolyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togMessenPolyActionPerformed
         measureComponent.actionMeasurePolygon();
     }//GEN-LAST:event_togMessenPolyActionPerformed
 
     private void togZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togZoomActionPerformed
         measureComponent.actionZoom();
     }//GEN-LAST:event_togZoomActionPerformed
 
     private void togMessenLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togMessenLineActionPerformed
         measureComponent.actionMeasureLine();
     }//GEN-LAST:event_togMessenLineActionPerformed
 
     private void togCalibrateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togCalibrateActionPerformed
         if (currentPage != NO_SELECTION) {
             double distance = askForDistanceValue();
             if (distance > 0d) {
                 measureComponent.actionCalibrate(distance);
                 final Geometry documentGeom = measureComponent.getMainDocumentGeometry();
                 try {
                     registerGeometryForPage(documentGeom, currentDocument, currentPage);
                 } catch (Exception ex) {
                     log.error(ex, ex);
                 }
             } else {
                 JOptionPane.showMessageDialog(this, "Eingegebene(r) Distanz bzw. Umfang ist kein gültiger Wert oder gleich 0.", "Ungültige Eingabe", JOptionPane.WARNING_MESSAGE);
             }
             togPan.setSelected(true);
         }
     }//GEN-LAST:event_togCalibrateActionPerformed
 
     private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
         measureComponent.actionOverview();
     }//GEN-LAST:event_btnHomeActionPerformed
 
     private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
         File current = documentFiles[currentDocument];
         try {
             BrowserLauncher.openURL(current.toURI().toURL().toString());
         } catch (Exception ex) {
             log.error(ex, ex);
         }
     }//GEN-LAST:event_btnPrintActionPerformed
     // <editor-fold defaultstate="collapsed" desc="Private Helper Methods">
 
     private Collection<CidsBean> getPages() {
         if (cidsBean != null) {
             Object o = null;
             if (currentDocument == TEXTBLATT_DOCUMENT) {
                 o = cidsBean.getProperty("textblatt_pages");
             } else if (currentDocument == LAGEPLAN_DOCUMENT) {
                 o = cidsBean.getProperty("lageplan_pages");
 
             }
             if (o instanceof Collection) {
                 return (Collection<CidsBean>) o;
             }
         }
         return null;
     }
 
     private void registerGeometryForPage(Geometry geometry, int documentNo, int pageNo) throws Exception {
         if (geometry != null && documentNo > NO_SELECTION && pageNo > NO_SELECTION) {
             Geometry oldVal = pageGeometries.get(pageNo);
             if (oldVal == null || !oldVal.equals(geometry)) {
                 pageGeometries.put(pageNo, geometry);
                 Collection<CidsBean> pageGeoCollection = getPages();
                 if (pageGeoCollection != null) {
                     boolean pageFound = false;
                     for (CidsBean pageGeom : pageGeoCollection) {
                         Object pageNumberObj = pageGeom.getProperty("page_number");
                         if (pageNumberObj instanceof Integer) {
                             if (pageNo == (Integer) pageNumberObj) {
                                 pageGeom.setProperty("geometry", geometry);
                                 pageFound = true;
                                 log.fatal("found: " + pageNo);
                                 break;
                             }
                         }
                     }
                     if (!pageFound) {
                         final CidsBean newBean = CidsBeanSupport.createNewCidsBeanFromTableName("ALB_GEO_DOCUMENT_PAGE");
                         pageGeoCollection.add(newBean);
                         newBean.setProperty("page_number", pageNo);
                         newBean.setProperty("geometry", geometry);
                     }
                     persistBean();
                 } else {
                     log.error("Empty Page Collection!");
                 }
             }
         }
     }
 
     private void loadPlan() {
         currentSelectedButton = btnPlan;
         lblCurrentViewTitle.setText("Lageplan");
         currentDocument = LAGEPLAN_DOCUMENT;
         CismetThreadPool.execute(new PictureReaderWorker(documentFiles[currentDocument]));
         lstPictures.setEnabled(true);
     }
 
     private void loadTextBlatt() {
         currentSelectedButton = btnTextblatt;
         lblCurrentViewTitle.setText("Textblatt");
         currentDocument = TEXTBLATT_DOCUMENT;
         CismetThreadPool.execute(new PictureReaderWorker(documentFiles[currentDocument]));
         lstPictures.setEnabled(true);
     }
 
     private void setControlsEnabled(boolean enabled) {
         for (int i = 0; i < documentFiles.length; ++i) {
             JButton current = documentButtons[i];
             current.setEnabled(documentFiles[LAGEPLAN_DOCUMENT] != null && enabled && currentSelectedButton != current);
         }
     }
 
     private void setCurrentDocumentNull() {
         currentDocument = NO_SELECTION;
         currentActualDocumentMD5 = "";
         pageGeometries.clear();
         setCurrentPageNull();
     }
 
     private void setCurrentPageNull() {
         currentPage = NO_SELECTION;
     }
 
     private void closeReader() {
         if (pictureReader != null) {
             pictureReader.close();
             pictureReader = null;
         }
     }
 
     private void showPermissionWarning() {
         if (!alreadyWarnedAboutPermissionProblem) {
             JOptionPane.showMessageDialog(this, "Kein Schreibrecht", "Kein Schreibrecht für die Klasse. Änderungen werden nicht gespeichert.", JOptionPane.WARNING_MESSAGE);
         }
         log.warn("User has no right to save Baulast bean!");
         alreadyWarnedAboutPermissionProblem = true;
     }
 
     private void persistBean() throws Exception {
         if (CidsBeanSupport.checkWritePermission(cidsBean)) {
             alreadyWarnedAboutPermissionProblem = false;
             cidsBean.persist();
         } else {
             showPermissionWarning();
         }
     }
 
     private double askForDistanceValue() {
         try {
             String laenge = JOptionPane.showInputDialog(this, "Bitte Länge bzw. Umfang in Metern eingeben:", "Kalibrierung", JOptionPane.QUESTION_MESSAGE);
             if (laenge != null) {
                 return Math.abs(Double.parseDouble(laenge.replace(',', '.')));
             }
         } catch (Exception ex) {
             log.warn(ex, ex);
         }
         return 0d;
     }
 // </editor-fold>
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup btnGrpDocs;
     private javax.swing.JButton btnHome;
     private javax.swing.JButton btnPlan;
     private javax.swing.JButton btnPrint;
     private javax.swing.JButton btnTextblatt;
     private javax.swing.ButtonGroup buttonGrpMode;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JLabel lblArea;
     private javax.swing.JLabel lblCurrentViewTitle;
     private javax.swing.JLabel lblDistance;
     private javax.swing.JLabel lblTxtArea;
     private javax.swing.JLabel lblTxtDistance;
     private javax.swing.JList lstPictures;
     private de.cismet.cismap.commons.gui.measuring.MeasuringComponent measureComponent;
     private javax.swing.JPanel panCenter;
     private javax.swing.JPanel panPicNavigation;
     private de.cismet.tools.gui.RoundedPanel rpControls;
     private de.cismet.tools.gui.RoundedPanel rpMessdaten;
     private de.cismet.tools.gui.RoundedPanel rpSeiten;
     private javax.swing.JScrollPane scpPictureList;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel1;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel3;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel4;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel5;
     private de.cismet.tools.gui.RoundedPanel spDocuments;
     private javax.swing.JToggleButton togCalibrate;
     private javax.swing.JToggleButton togMessenLine;
     private javax.swing.JToggleButton togMessenPoly;
     private javax.swing.JToggleButton togPan;
     private javax.swing.JToggleButton togZoom;
     // End of variables declaration//GEN-END:variables
 
     // <editor-fold defaultstate="collapsed" desc="FileSearchWorker">
     final class FileSearchWorker extends SwingWorker<File[], Void> {
 
         public FileSearchWorker() {
             setControlsEnabled(false);
             measureComponent.reset();
             togPan.setSelected(true);
             resetMeasureDataLabels();
         }
 
         @Override
         protected File[] doInBackground() throws Exception {
             final File[] result = new File[documentFiles.length];
             final Object blattObj = getCidsBean().getProperty("textblatt");
             final Object planObj = getCidsBean().getProperty("lageplan");
             log.info("Found blatt " + blattObj);
             log.info("Found plan " + planObj);
             if (blattObj != null) {
                 final File searchResult = BaulastenPictureFinder.findTextblattPicture(blattObj.toString());
                 result[TEXTBLATT_DOCUMENT] = searchResult;
                 log.info("Blatt picture " + result[TEXTBLATT_DOCUMENT]);
             }
             if (planObj != null) {
                 final File searchResult = BaulastenPictureFinder.findPlanPicture(planObj.toString());
                 result[LAGEPLAN_DOCUMENT] = searchResult;
                 log.info("Plan picture " + result[LAGEPLAN_DOCUMENT]);
             }
             return result;
         }
 
         @Override
         protected void done() {
             try {
                 final File[] result = get();
                 for (int i = 0; i < result.length; ++i) {
                     documentFiles[i] = result[i];
                 }
             } catch (InterruptedException ex) {
                 log.warn(ex, ex);
             } catch (ExecutionException ex) {
                 log.error(ex, ex);
             } finally {
                 setControlsEnabled(true);
                 for (int i = 0; i < documentFiles.length; ++i) {
                     documentButtons[i].setEnabled(documentFiles[i] != null);
                 }
                 if (btnTextblatt.isEnabled()) {
                     loadTextBlatt();
                 } else if (btnPlan.isEnabled()) {
                     loadPlan();
                 } else {
                     lstPictures.setModel(new DefaultListModel());
                     measureComponent.removeAllFeatures();
                 }
             }
         }
     }
 
 // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="PictureReaderWorker">
     final class PictureReaderWorker extends SwingWorker<ListModel, Void> {
 
         public PictureReaderWorker(File pictureFile) {
             this.pictureFile = pictureFile;
             log.debug("prepare picture reader for file " + this.pictureFile);
             lstPictures.setModel(LADEN_MODEL);
             measureComponent.removeAllFeatures();
             setControlsEnabled(false);
         }
         private final File pictureFile;
         private boolean md5OK = false;
 
         private void updateMD5() throws Exception {
             expectedMD5Values[currentDocument] = currentActualDocumentMD5;
             cidsBean.setProperty(MD5_PROPERTY_NAMES[currentDocument], currentActualDocumentMD5);
             log.debug("saving md5 value " + currentActualDocumentMD5);
             persistBean();
         }
 
         @Override
         protected ListModel doInBackground() throws Exception {
             final DefaultListModel model = new DefaultListModel();
             currentActualDocumentMD5 = MD5Calculator.generateMD5(pictureFile);
             if (currentDocument != NO_SELECTION && currentDocument < expectedMD5Values.length) {
                 final String expectedMD5 = expectedMD5Values[currentDocument];
                 if (expectedMD5 != null && expectedMD5.length() > 0) {
                     if (expectedMD5.equals(currentActualDocumentMD5)) {
                         md5OK = true;
                     } else {
                         md5OK = false;
                         log.warn("MD5 fail. Expected: " + expectedMD5 + ". Found: " + currentActualDocumentMD5);
                     }
                 } else {
                     md5OK = true;
                     updateMD5();
                 }
             }
             if (md5OK) {
                 readPageGeometriesIntoMap(getPages());
             } else {
 
                 FutureTask<Integer> userInput = new FutureTask<Integer>(new Callable<Integer>() {
 
                     @Override
                     public Integer call() throws Exception {
                         return JOptionPane.showConfirmDialog(measureComponent, "Das Dokument wurde seit dem letzten Kalibrieren geändert. Sollen die Kalibrierungsdaten für das Dokument gelöscht werden?", "Dokument wurde verändert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                     }
                 });
                 EventQueue.invokeAndWait(userInput);
                 if (userInput.get().equals(JOptionPane.OK_OPTION)) {
                     Collection<CidsBean> pageGeoData = getPages();
                     if (pageGeoData != null) {
                         for (CidsBean bean : pageGeoData) {
                             bean.delete();
                         }
                     }
                 } else {
                     updateMD5();
                     readPageGeometriesIntoMap(getPages());
                 }
             }
 
             closeReader();
             pictureReader = new MultiPagePictureReader(pictureFile);
             final int numberOfPages = pictureReader.getNumberOfPages();
             for (int i = 0; i < numberOfPages; ++i) {
                 model.addElement(i + 1);
             }
             return model;
         }
 
         private void readPageGeometriesIntoMap(Collection<CidsBean> pageGeoms) {
             pageGeometries.clear();
             if (pageGeoms != null) {
                 for (CidsBean bean : pageGeoms) {
                     Object pageNumberObj = bean.getProperty("page_number");
                     Object geometryObj = bean.getProperty("geometry");
                     if (pageNumberObj instanceof Integer && geometryObj instanceof Geometry) {
                         pageGeometries.put((Integer) pageNumberObj, (Geometry) geometryObj);
                     }
                 }
             }
         }
 
         @Override
         protected void done() {
             try {
                 final ListModel model = get();
                 lstPictures.setModel(model);
                 if (model.getSize() > 0) {
                     lstPictures.setSelectedIndex(0);
                 } else {
                     lstPictures.setModel(new DefaultListModel());
                 }
             } catch (InterruptedException ex) {
                 setCurrentDocumentNull();
                 lstPictures.setModel(FEHLER_MODEL);
                 log.warn(ex, ex);
             } catch (ExecutionException ex) {
                 lstPictures.setModel(FEHLER_MODEL);
                 setCurrentDocumentNull();
                 log.error(ex, ex);
             } finally {
                 setControlsEnabled(true);
             }
         }
     }
 
 // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="PictureSelectionWorker">
     final class PictureSelectWorker extends SwingWorker<BufferedImage, Void> {
 
         public PictureSelectWorker(int pageNumber) {
             this.pageNumber = pageNumber;
             setCurrentPageNull();
             setControlsEnabled(false);
             measureComponent.reset();
         }
         private final int pageNumber;
 
         @Override
         protected BufferedImage doInBackground() throws Exception {
             if (pictureReader != null) {
                 return pictureReader.loadPage(pageNumber);
             }
             throw new IllegalStateException("PictureReader is null!");
         }
 
         @Override
         protected void done() {
             try {
                 final Geometry pageGeom = pageGeometries.get(pageNumber);
                 currentPage = pageNumber;
                 togPan.setSelected(true);
                 resetMeasureDataLabels();
                 measureComponent.addImage(get(), pageGeom);
                 measureComponent.zoomToFeatureCollection();
                 if(pageGeom != null) {
                     rpMessdaten.setBackground(KALIBRIERUNG_VORHANDEN);
                     rpMessdaten.setAlpha(120);
                 } else {
                     rpMessdaten.setBackground(Color.WHITE);
                     rpMessdaten.setAlpha(60);
                 }
             } catch (InterruptedException ex) {
                 setCurrentPageNull();
                 log.warn(ex, ex);
             } catch (Exception ex) {
                 setCurrentPageNull();
                 log.error(ex, ex);
             } finally {
                 setControlsEnabled(true);
             }
         }
     }
 
 // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="MessenFeatureCollectionListener">
     final class MessenFeatureCollectionListener extends de.cismet.cismap.commons.features.FeatureCollectionAdapter {
 
         @Override
         public void featuresAdded(FeatureCollectionEvent fce) {
             if (!togCalibrate.isEnabled()) {
                 for (Feature f : measureComponent.getFeatureCollection().getAllFeatures()) {
                     if (f instanceof PureNewFeature && !(f.getGeometry() instanceof Point)) {
                         //messgeometrie gefunden
                         togCalibrate.setEnabled(true);
                     }
                 }
             }
             refreshMeasurementsInStatus(fce.getEventFeatures());
         }
 
         @Override
         public void featuresRemoved(FeatureCollectionEvent fce) {
             if (togCalibrate.isEnabled()) {
                 for (Feature f : measureComponent.getFeatureCollection().getAllFeatures()) {
                     if (f instanceof PureNewFeature && !(f.getGeometry() instanceof Point)) {
                         //messgeometrie gefunden.
                         return;
                     }
                 }
                 togCalibrate.setEnabled(false);
             }
         }
 
         @Override
         public void allFeaturesRemoved(FeatureCollectionEvent fce) {
             featuresRemoved(fce);
         }
 
         @Override
         public void featuresChanged(FeatureCollectionEvent fce) {
 //            if (map.getInteractionMode().equals(MY_MESSEN_MODE)) {
             refreshMeasurementsInStatus(fce.getEventFeatures());
 //            } else {
 //                refreshMeasurementsInStatus();
 //            }
         }
 
         @Override
         public void featureSelectionChanged(FeatureCollectionEvent fce) {
 //            refreshMeasurementsInStatus();
             refreshMeasurementsInStatus(fce.getEventFeatures());
         }
     }
 
     private void resetMeasureDataLabels() {
         lblTxtDistance.setText("Länge/Umfang:");
         lblDistance.setText("-");
         lblArea.setText("-");
     }
 
     private void refreshMeasurementsInStatus(Collection<Feature> cf) {
         double umfang = 0.0;
         double area = 0.0;
         for (Feature f : cf) {
             Geometry geom = f.getGeometry();
             if (f instanceof PureNewFeature && geom != null) {
                 area += geom.getArea();
                 umfang += geom.getLength();
                 if (umfang != 0.0) {
                     if (area != 0.0) {
                         lblTxtDistance.setText("Umfang:");
                         lblDistance.setText(StaticDecimalTools.round(umfang) + " m ");
                         lblArea.setText(StaticDecimalTools.round(area) + " m²");
                     } else {
                         if (MessenGeometryListener.POLYGON.equals(measureComponent.getMessenInputListener().getMode())) {
                             //reduce polygon line length to one way
                             umfang *= 0.5;
                         }
                         lblTxtDistance.setText("Länge:");
                         lblDistance.setText(StaticDecimalTools.round(umfang) + " m ");
                         lblArea.setText("-");
                     }
                 } else {
                     resetMeasureDataLabels();
                 }
             }
         }
     }
 }
 // </editor-fold>
 
