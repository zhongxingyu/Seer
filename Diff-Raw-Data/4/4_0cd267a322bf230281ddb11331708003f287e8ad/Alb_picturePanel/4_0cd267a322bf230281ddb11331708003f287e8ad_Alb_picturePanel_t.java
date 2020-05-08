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
 /*
  * Alb_picturePanel.java
  *
  * Created on 11.12.2009, 14:49:40
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.io.File;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.ListModel;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.custom.objectrenderer.utils.BaulastenPictureFinder;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.tools.StaticCidsUtilities;
 
 import de.cismet.cismap.commons.Crs;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollectionEvent;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.gui.measuring.MeasuringComponent;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;
 
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.StaticDebuggingTools;
 import de.cismet.tools.StaticDecimalTools;
 
 import de.cismet.tools.gui.MultiPagePictureReader;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class Alb_picturePanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alb_picturePanel.class);
 //    private static final String[] MD5_PROPERTY_NAMES = new String[]{"lageplan_md5", "textblatt_md5"};
     private static final String TEXTBLATT_PROPERTY = "textblatt";
     private static final String LAGEPLAN_PROPERTY = "lageplan";
     public static final String BLATTNUMMER_PROPERTY = "blattnummer";
     public static final String LFDNUMMER_PROPERTY = "laufende_nummer";
     private static final int LAGEPLAN_DOCUMENT = 0;
     private static final int TEXTBLATT_DOCUMENT = 1;
     private static final int NO_SELECTION = -1;
     private static final Color KALIBRIERUNG_VORHANDEN = new Color(120, 255, 190);
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
 
     private static boolean alreadyWarnedAboutPermissionProblem = false;
 
     //~ Instance fields --------------------------------------------------------
 
     private XBoundingBox initialBoundingBox = new XBoundingBox(
             2583621.251964098d,
             5682507.032498134d,
             2584022.9413952776d,
             5682742.852810634d,
             "EPSG:31466",
             true);
     private Crs crs = new Crs("EPSG:31466", "EPSG:31466", "EPSG:31466", true, true);
     private PictureSelectWorker currentPictureSelectWorker = null;
     private MultiPagePictureReader pictureReader;
     private CidsBean cidsBean;
     private URL[] documentURLs;
     private JButton[] documentButtons;
     private transient PropertyChangeListener updatePicturePathListener = null;
     private JButton currentSelectedButton;
     private final MessenFeatureCollectionListener messenListener;
     //
     private volatile int currentDocument = NO_SELECTION;
     private volatile int currentPage = NO_SELECTION;
     private boolean pathsChanged = false;
     private final Map<Integer, Geometry> pageGeometries = new HashMap<Integer, Geometry>();
     private String collisionWarning = "";
     private final boolean selfPersisting;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup btnGrpDocs;
     private javax.swing.JButton btnHome;
     private javax.swing.JButton btnOpen;
     private javax.swing.JButton btnPlan;
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
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Alb_picturePanel.
      */
     public Alb_picturePanel() {
         this(false);
     }
 
     /**
      * Creates a new Alb_picturePanel object.
      *
      * @param  selfPersisting  DOCUMENT ME!
      */
     public Alb_picturePanel(final boolean selfPersisting) {
         this.selfPersisting = selfPersisting;
         documentURLs = new URL[2];
         documentButtons = new JButton[documentURLs.length];
         initComponents();
         documentButtons[LAGEPLAN_DOCUMENT] = btnPlan;
         documentButtons[TEXTBLATT_DOCUMENT] = btnTextblatt;
         messenListener = new MessenFeatureCollectionListener();
         measureComponent.getFeatureCollection().addFeatureCollectionListener(messenListener);
 //        expectedMD5Values = new String[2];
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     public void dispose() {
         measureComponent.getFeatureCollection().removeFeatureCollectionListener(messenListener);
         measureComponent.dispose();
         updatePicturePathListener = null;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void zoomToFeatureCollection() {
         measureComponent.zoomToFeatureCollection();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void updateIfPicturePathsChanged() {
         if (pathsChanged) {
             setCurrentDocumentNull();
             CismetThreadPool.execute(new FileSearchWorker());
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the cidsBean
      */
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsBean  the cidsBean to set
      */
     public void setCidsBean(final CidsBean cidsBean) {
        documentURLs = new URL[2];
        lstPictures.setModel(new DefaultListModel());
        measureComponent.removeAllFeatures();
        setEnabled(false);
         this.cidsBean = cidsBean;
         if (cidsBean != null) {
             updatePicturePathListener = new PropertyChangeListener() {
 
                     @Override
                     public void propertyChange(final PropertyChangeEvent evt) {
                         final String evtProp = evt.getPropertyName();
                         if (TEXTBLATT_PROPERTY.equals(evtProp) || LAGEPLAN_PROPERTY.equals(evtProp)) {
                             pathsChanged = true;
                         }
                     }
                 };
             cidsBean.addPropertyChangeListener(WeakListeners.propertyChange(updatePicturePathListener, cidsBean));
         }
         setCurrentDocumentNull();
         CismetThreadPool.execute(new FileSearchWorker());
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
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
         btnOpen = new javax.swing.JButton();
         panCenter = new javax.swing.JPanel();
         measureComponent = new MeasuringComponent(initialBoundingBox, crs);
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblCurrentViewTitle = new javax.swing.JLabel();
 
         setMinimumSize(new java.awt.Dimension(800, 700));
         setOpaque(false);
         setPreferredSize(new java.awt.Dimension(800, 700));
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
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
         rpSeiten.setMaximumSize(new java.awt.Dimension(75, 140));
         rpSeiten.setMinimumSize(new java.awt.Dimension(75, 140));
         rpSeiten.setPreferredSize(new java.awt.Dimension(75, 140));
 
         scpPictureList.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         scpPictureList.setMaximumSize(new java.awt.Dimension(75, 125));
         scpPictureList.setMinimumSize(new java.awt.Dimension(75, 125));
         scpPictureList.setOpaque(false);
         scpPictureList.setPreferredSize(new java.awt.Dimension(75, 125));
 
         lstPictures.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         lstPictures.setEnabled(false);
         lstPictures.setFixedCellWidth(75);
         lstPictures.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
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
 
         lblTxtArea.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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
         togPan.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/pan.gif"))); // NOI18N
         togPan.setSelected(true);
         togPan.setText("Verschieben");
         togPan.setToolTipText("Verschieben");
         togPan.setFocusPainted(false);
         togPan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togPan.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
         togZoom.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom.gif"))); // NOI18N
         togZoom.setText("Zoomen");
         togZoom.setToolTipText("Zoomen");
         togZoom.setFocusPainted(false);
         togZoom.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togZoom.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
         togMessenLine.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/newLinestring.png"))); // NOI18N
         togMessenLine.setText("Messlinie");
         togMessenLine.setToolTipText("Messen (Linie)");
         togMessenLine.setFocusPainted(false);
         togMessenLine.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togMessenLine.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
         togMessenPoly.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/newPolygon.png"))); // NOI18N
         togMessenPoly.setText("Messfläche");
         togMessenPoly.setToolTipText("Messen (Polygon)");
         togMessenPoly.setFocusPainted(false);
         togMessenPoly.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togMessenPoly.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
         togCalibrate.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/screen.gif"))); // NOI18N
         togCalibrate.setText("Kalibrieren");
         togCalibrate.setToolTipText("Kalibrieren");
         togCalibrate.setEnabled(false);
         togCalibrate.setFocusPainted(false);
         togCalibrate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togCalibrate.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togCalibrateActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         rpControls.add(togCalibrate, gridBagConstraints);
 
         btnHome.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/home.gif"))); // NOI18N
         btnHome.setText("Übersicht");
         btnHome.setToolTipText("Übersicht");
         btnHome.setFocusPainted(false);
         btnHome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnHome.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
         btnOpen.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/folder-image.png"))); // NOI18N
         btnOpen.setText("Öffnen");
         btnOpen.setToolTipText("Download zum Öffnen in externer Anwendung");
         btnOpen.setFocusPainted(false);
         btnOpen.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnOpen.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnOpenActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
         rpControls.add(btnOpen, gridBagConstraints);
 
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
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstPicturesValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstPicturesValueChanged
         if (!evt.getValueIsAdjusting()) {
             final Object selObj = lstPictures.getSelectedValue();
             if (selObj instanceof Integer) {
                 final int pageNo = (Integer)selObj;
                 final PictureSelectWorker oldWorkerTest = currentPictureSelectWorker;
                 if (oldWorkerTest != null) {
                     oldWorkerTest.cancel(true);
                 }
                 currentPictureSelectWorker = new PictureSelectWorker(pageNo - 1);
                 // page -> offset
                 CismetThreadPool.execute(currentPictureSelectWorker);
             }
         }
     } //GEN-LAST:event_lstPicturesValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnPlanActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnPlanActionPerformed
         loadPlan();
     }                                                                           //GEN-LAST:event_btnPlanActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnTextblattActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnTextblattActionPerformed
         loadTextBlatt();
     }                                                                                //GEN-LAST:event_btnTextblattActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togPanActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togPanActionPerformed
         measureComponent.actionPan();
     }                                                                          //GEN-LAST:event_togPanActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togMessenPolyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togMessenPolyActionPerformed
         measureComponent.actionMeasurePolygon();
     }                                                                                 //GEN-LAST:event_togMessenPolyActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togZoomActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togZoomActionPerformed
         measureComponent.actionZoom();
     }                                                                           //GEN-LAST:event_togZoomActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togMessenLineActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togMessenLineActionPerformed
         measureComponent.actionMeasureLine();
     }                                                                                 //GEN-LAST:event_togMessenLineActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togCalibrateActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togCalibrateActionPerformed
         if (currentPage != NO_SELECTION) {
             final Double distance = askForDistanceValue();
             if (distance != null) {
                 if (distance > 0d) {
                     measureComponent.actionCalibrate(distance);
                     final Geometry documentGeom = measureComponent.getMainDocumentGeometry();
                     try {
                         registerGeometryForPage(documentGeom, currentDocument, currentPage);
                     } catch (Exception ex) {
                         log.error(ex, ex);
                         final ErrorInfo ei = new ErrorInfo(
                                 "Fehler beim Speichern der Kalibrierung",
                                 "Beim Speichern der Kalibrierung ist ein Fehler aufgetreten",
                                 null,
                                 null,
                                 ex,
                                 Level.SEVERE,
                                 null);
                         JXErrorPane.showDialog(this, ei);
                     }
                 } else {
                     JOptionPane.showMessageDialog(
                         StaticSwingTools.getParentFrame(this),
                         "Eingegebene(r) Distanz bzw. Umfang ist kein gültiger Wert oder gleich 0.",
                         "Ungültige Eingabe",
                         JOptionPane.WARNING_MESSAGE);
                 }
             }
             togPan.setSelected(true);
         }
     }                                                                                //GEN-LAST:event_togCalibrateActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnHomeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnHomeActionPerformed
         measureComponent.actionOverview();
     }                                                                           //GEN-LAST:event_btnHomeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnOpenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOpenActionPerformed
         if ((currentDocument == NO_SELECTION) || (documentURLs == null) || (currentDocument >= documentURLs.length)
                     || (currentDocument < 0)) {
             return;
         }
 
         final URL current = documentURLs[currentDocument];
 
         if (current == null) {
             return;
         }
 
         final String path = current.toExternalForm();
 
         final URL url;
         url = current;
 
         CismetThreadPool.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     final String filename = path.substring(path.lastIndexOf("/") + 1);
                     if (DownloadManagerDialog.showAskingForUserTitle(Alb_picturePanel.this)) {
                         DownloadManager.instance()
                                 .add(
                                     new HttpDownload(
                                         url,
                                         "",
                                         DownloadManagerDialog.getJobname(),
                                         "Baulast",
                                         filename.substring(0, filename.lastIndexOf(".")),
                                         filename.substring(filename.lastIndexOf("."))));
                     }
                 }
             });
     } //GEN-LAST:event_btnOpenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Collection<CidsBean> getPages() {
         if (cidsBean != null) {
             Object o = null;
             if (currentDocument == TEXTBLATT_DOCUMENT) {
                 o = cidsBean.getProperty("textblatt_pages");
             } else if (currentDocument == LAGEPLAN_DOCUMENT) {
                 o = cidsBean.getProperty("lageplan_pages");
             }
             if (o instanceof Collection) {
                 return (Collection<CidsBean>)o;
             }
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   geometry    DOCUMENT ME!
      * @param   documentNo  DOCUMENT ME!
      * @param   pageNo      DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void registerGeometryForPage(final Geometry geometry, final int documentNo, final int pageNo)
             throws Exception {
         if ((geometry != null) && (documentNo > NO_SELECTION) && (pageNo > NO_SELECTION)) {
             final Geometry oldVal = pageGeometries.get(pageNo);
             if ((oldVal == null) || !oldVal.equals(geometry)) {
                 pageGeometries.put(pageNo, geometry);
                 final Collection<CidsBean> pageGeoCollection = getPages();
                 if (pageGeoCollection != null) {
                     boolean pageFound = false;
                     for (final CidsBean pageGeom : pageGeoCollection) {
                         final Object pageNumberObj = pageGeom.getProperty("page_number");
                         if (pageNumberObj instanceof Integer) {
                             if (pageNo == (Integer)pageNumberObj) {
                                 pageGeom.setProperty("geometry", geometry);
                                 pageFound = true;
                                 break;
                             }
                         }
                     }
                     if (!pageFound) {
                         final CidsBean newBean = CidsBeanSupport.createNewCidsBeanFromTableName(
                                 "ALB_GEO_DOCUMENT_PAGE");
                         newBean.setProperty("page_number", pageNo);
                         newBean.setProperty("geometry", geometry);
                         pageGeoCollection.add(newBean);
                         if (log.isDebugEnabled()) {
                             log.debug(newBean.getMetaObject().getDebugString());
                         }
                     }
                     if (selfPersisting) {
                         persistBean();
                     }
                     rpMessdaten.setBackground(KALIBRIERUNG_VORHANDEN);
                     rpMessdaten.setAlpha(120);
                 } else {
                     log.error("Empty Page Collection!");
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void loadPlan() {
         currentSelectedButton = btnPlan;
         lblCurrentViewTitle.setText("Lageplan");
         currentDocument = LAGEPLAN_DOCUMENT;
         CismetThreadPool.execute(new PictureReaderWorker(documentURLs[currentDocument]));
         lstPictures.setEnabled(true);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void loadTextBlatt() {
         currentSelectedButton = btnTextblatt;
         lblCurrentViewTitle.setText("Textblatt");
         currentDocument = TEXTBLATT_DOCUMENT;
         CismetThreadPool.execute(new PictureReaderWorker(documentURLs[currentDocument]));
         lstPictures.setEnabled(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  enabled  DOCUMENT ME!
      */
     private void setControlsEnabled(final boolean enabled) {
         for (int i = 0; i < documentURLs.length; ++i) {
             final JButton current = documentButtons[i];
             current.setEnabled((documentURLs[i] != null) && enabled && (currentSelectedButton != current));
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void setCurrentDocumentNull() {
         currentDocument = NO_SELECTION;
         pageGeometries.clear();
         setCurrentPageNull();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void setCurrentPageNull() {
         currentPage = NO_SELECTION;
         rpMessdaten.setBackground(Color.WHITE);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void closeReader() {
         if (pictureReader != null) {
             pictureReader.close();
             pictureReader = null;
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showPermissionWarning() {
         if (!alreadyWarnedAboutPermissionProblem) {
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 "Kein Schreibrecht",
                 "Kein Schreibrecht für die Klasse. Änderungen werden nicht gespeichert.",
                 JOptionPane.WARNING_MESSAGE);
         }
         log.warn("User has no right to save Baulast bean!");
         alreadyWarnedAboutPermissionProblem = true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void persistBean() throws Exception {
         if (CidsBeanSupport.checkWritePermission(cidsBean)) {
             alreadyWarnedAboutPermissionProblem = false;
             final SwingWorker<Void, Void> persistWorker = new SwingWorker<Void, Void>() {
 
                     @Override
                     protected Void doInBackground() throws Exception {
                         cidsBean.persist();
                         return null;
                     }
 
                     @Override
                     protected void done() {
                         try {
                             get();
                         } catch (Exception ex) {
                             log.error(ex, ex);
                             final ErrorInfo ei = new ErrorInfo(
                                     "Fehler beim Speichern der Kalibrierung",
                                     "Beim Speichern der Kalibrierung ist ein Fehler aufgetreten",
                                     null,
                                     null,
                                     ex,
                                     Level.SEVERE,
                                     null);
                             JXErrorPane.showDialog(Alb_picturePanel.this, ei);
                         }
                     }
                 };
             CismetThreadPool.execute(persistWorker);
         } else {
             showPermissionWarning();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Double askForDistanceValue() {
         try {
             final String laenge = JOptionPane.showInputDialog(
                     StaticSwingTools.getParentFrame(this),
                     "Bitte Länge bzw. Umfang in Metern eingeben:",
                     "Kalibrierung",
                     JOptionPane.QUESTION_MESSAGE);
             if (laenge != null) {
                 return Math.abs(Double.parseDouble(laenge.replace(',', '.')));
             } else {
                 return null;
             }
         } catch (Exception ex) {
             log.warn(ex, ex);
         }
         return 0d;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getCollisionWarning() {
         return collisionWarning;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void clearCollisionWarning() {
         this.collisionWarning = "";
     }
 
     /**
      * DOCUMENT ME!
      */
     private void resetMeasureDataLabels() {
         lblTxtDistance.setText("Länge/Umfang:");
         lblDistance.setText("-");
         lblArea.setText("-");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cf  DOCUMENT ME!
      */
     private void refreshMeasurementsInStatus(final Collection<Feature> cf) {
         double umfang = 0.0;
         double area = 0.0;
         for (final Feature f : cf) {
             final Geometry geom = f.getGeometry();
             if ((f instanceof PureNewFeature) && (geom != null)) {
                 area += geom.getArea();
                 umfang += geom.getLength();
                 if (umfang != 0.0) {
                     if (area != 0.0) {
                         lblTxtDistance.setText("Umfang:");
                         lblDistance.setText(StaticDecimalTools.round(umfang) + " m ");
                         lblArea.setText(StaticDecimalTools.round(area) + " m²");
                     } else {
                         if (MessenGeometryListener.POLYGON.equals(
                                         measureComponent.getMessenInputListener().getMode())) {
                             // reduce polygon line length to one way
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
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     //J-
     final class FileSearchWorker extends SwingWorker<List[], Void> {
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new FileSearchWorker object.
          */
         public FileSearchWorker() {
             setControlsEnabled(false);
             measureComponent.reset();
             togPan.setSelected(true);
             resetMeasureDataLabels();
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         protected List[] doInBackground() throws Exception {
             final List[] result = new List[2];
 //            if (!StaticDebuggingTools.checkHomeForFile("BAULASTENHTTPDOCPREVIEW")) {
 //                final Object blattObj = getCidsBean().getProperty(TEXTBLATT_PROPERTY);
 //                final Object planObj = getCidsBean().getProperty(LAGEPLAN_PROPERTY);
 //                log.info("Found blatt property " + blattObj);
 //                log.info("Found plan property " + planObj);
 //                if (blattObj != null) {
 //                    result[TEXTBLATT_DOCUMENT] =
 //                            BaulastenPictureFinder.findTextblattPicture(
 //                            blattObj.toString().replaceAll("\\\\", "/"));
 //                }
 //                if (planObj != null) {
 //                    result[LAGEPLAN_DOCUMENT] =
 //                            BaulastenPictureFinder.findPlanPicture(
 //                            planObj.toString().replaceAll("\\\\", "/"));
 //                }
 //
 //            }
 //            else {
 
             String blattnummer = (String) getCidsBean().getProperty(BLATTNUMMER_PROPERTY);
             String lfdNummer = (String) getCidsBean().getProperty(LFDNUMMER_PROPERTY);
 
             result[TEXTBLATT_DOCUMENT] = BaulastenPictureFinder.findTextblattPicture(blattnummer, lfdNummer);
             result[LAGEPLAN_DOCUMENT] = BaulastenPictureFinder.findPlanPicture(blattnummer, lfdNummer);
 
             log.debug("Textblätter:" + result[TEXTBLATT_DOCUMENT]);
             log.debug("Lagepläne:" + result[LAGEPLAN_DOCUMENT]);
             return result;
 
         }
 
         @Override
         protected void done() {
             try {
                 final List[] result = get();
                 final StringBuffer collisionLists = new StringBuffer();
                 for (int i = 0; i < result.length; ++i) {
                     //cast!
                     final List<URL> current = result[i];
                     if (current != null) {
                         if (current.size() > 0) {
                             if (current.size() > 1) {
                                 if (collisionLists.length() > 0) {
                                     collisionLists.append(",\n");
                                 }
                                 collisionLists.append(current);
                             }
                             documentURLs[i] = current.get(0);
                         }
                     }
                 }
                 if (collisionLists.length() > 0) {
                     collisionWarning =
                             "Achtung: im Zielverzeichnis sind mehrere Dateien mit"
                             + " demselben Namen in unterschiedlichen Dateiformaten "
                             + "vorhanden.\n\nBitte löschen Sie die ungültigen Formate "
                             + "und setzen Sie die Bearbeitung in WuNDa anschließend fort."
                             + "\n\nDateien:\n"
                             + collisionLists
                             + "\n";
                     log.info(collisionWarning);
                 }
             } catch (InterruptedException ex) {
                 log.warn(ex, ex);
             } catch (Exception ex) {
                 log.error(ex, ex);
             } finally {
                 setControlsEnabled(true);
                 pathsChanged = false;
                 setEnabled(true);
                 for (int i = 0; i < documentURLs.length; ++i) {
                     documentButtons[i].setEnabled(documentURLs[i] != null);
                 }
                 if (btnTextblatt.isEnabled()) {
                     loadTextBlatt();
                 } else if (btnPlan.isEnabled()) {
                     loadPlan();
                 } else {
                     lstPictures.setModel(new DefaultListModel());
                     measureComponent.removeAllFeatures();
                     setEnabled(false);
                 }
             }
         }
     }
     //J+
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class PictureReaderWorker extends SwingWorker<ListModel, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final URL pictureURL;
 //        private boolean md5OK = false;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PictureReaderWorker object.
          *
          * @param  pictureURL  DOCUMENT ME!
          */
         public PictureReaderWorker(final URL pictureURL) {
             this.pictureURL = pictureURL;
             if (log.isDebugEnabled()) {
                 log.debug("prepare picture reader for file " + this.pictureURL);
             }
             lstPictures.setModel(LADEN_MODEL);
             measureComponent.removeAllFeatures();
             setControlsEnabled(false);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * private void updateMD5() throws Exception { expectedMD5Values[currentDocument] = currentActualDocumentMD5;
          * cidsBean.setProperty(MD5_PROPERTY_NAMES[currentDocument], currentActualDocumentMD5); log.debug("saving md5
          * value " + currentActualDocumentMD5); persistBean(); }.
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         @Override
         protected ListModel doInBackground() throws Exception {
             final DefaultListModel model = new DefaultListModel();
             readPageGeometriesIntoMap(getPages());
 
             closeReader();
             pictureReader = new MultiPagePictureReader(pictureURL);
 //            pictureReader.setCaching(false);
             final int numberOfPages = pictureReader.getNumberOfPages();
             for (int i = 0; i < numberOfPages; ++i) {
                 model.addElement(i + 1);
             }
             return model;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  pageGeoms  DOCUMENT ME!
          */
         private void readPageGeometriesIntoMap(final Collection<CidsBean> pageGeoms) {
             pageGeometries.clear();
             if (pageGeoms != null) {
                 for (final CidsBean bean : pageGeoms) {
                     final Object pageNumberObj = bean.getProperty("page_number");
                     final Object geometryObj = bean.getProperty("geometry");
                     if ((pageNumberObj instanceof Integer) && (geometryObj instanceof Geometry)) {
                         pageGeometries.put((Integer)pageNumberObj, (Geometry)geometryObj);
                     }
                 }
             }
         }
 
         /**
          * DOCUMENT ME!
          */
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
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class PictureSelectWorker extends SwingWorker<BufferedImage, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final int pageNumber;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PictureSelectWorker object.
          *
          * @param  pageNumber  DOCUMENT ME!
          */
         public PictureSelectWorker(final int pageNumber) {
             this.pageNumber = pageNumber;
             setCurrentPageNull();
             setControlsEnabled(false);
             measureComponent.reset();
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception              DOCUMENT ME!
          * @throws  IllegalStateException  DOCUMENT ME!
          */
         @Override
         protected BufferedImage doInBackground() throws Exception {
             if (pictureReader != null) {
                 return pictureReader.loadPage(pageNumber);
             }
             throw new IllegalStateException("PictureReader is null!!");
         }
 
         /**
          * DOCUMENT ME!
          */
         @Override
         protected void done() {
             try {
                 if (!isCancelled()) {
                     final Geometry pageGeom = pageGeometries.get(pageNumber);
                     currentPage = pageNumber;
                     measureComponent.addImage(get(), pageGeom, CrsTransformer.extractSridFromCrs(crs.getCode()));
                     togPan.setSelected(true);
                     resetMeasureDataLabels();
                     if (pageGeom != null) {
                         rpMessdaten.setBackground(KALIBRIERUNG_VORHANDEN);
                         rpMessdaten.setAlpha(120);
                     } else {
                         rpMessdaten.setBackground(Color.WHITE);
                         rpMessdaten.setAlpha(60);
                     }
                     measureComponent.zoomToFeatureCollection();
                 }
             } catch (InterruptedException ex) {
                 setCurrentPageNull();
                 log.warn(ex, ex);
             } catch (Exception ex) {
                 setCurrentPageNull();
                 log.error(ex, ex);
             } finally {
                 setControlsEnabled(true);
                 currentPictureSelectWorker = null;
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class MessenFeatureCollectionListener extends de.cismet.cismap.commons.features.FeatureCollectionAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  fce  DOCUMENT ME!
          */
         @Override
         public void featuresAdded(final FeatureCollectionEvent fce) {
             if (!togCalibrate.isEnabled()) {
                 for (final Feature f : measureComponent.getFeatureCollection().getAllFeatures()) {
                     if ((f instanceof PureNewFeature) && !(f.getGeometry() instanceof Point)) {
                         // messgeometrie gefunden
                         togCalibrate.setEnabled(true);
                     }
                 }
             }
             refreshMeasurementsInStatus(fce.getEventFeatures());
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  fce  DOCUMENT ME!
          */
         @Override
         public void featuresRemoved(final FeatureCollectionEvent fce) {
             if (togCalibrate.isEnabled()) {
                 for (final Feature f : measureComponent.getFeatureCollection().getAllFeatures()) {
                     if ((f instanceof PureNewFeature) && !(f.getGeometry() instanceof Point)) {
                         // messgeometrie gefunden.
                         return;
                     }
                 }
                 togCalibrate.setEnabled(false);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  fce  DOCUMENT ME!
          */
         @Override
         public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
             featuresRemoved(fce);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  fce  DOCUMENT ME!
          */
         @Override
         public void featuresChanged(final FeatureCollectionEvent fce) {
             refreshMeasurementsInStatus(fce.getEventFeatures());
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  fce  DOCUMENT ME!
          */
         @Override
         public void featureSelectionChanged(final FeatureCollectionEvent fce) {
             refreshMeasurementsInStatus(fce.getEventFeatures());
         }
     }
 }
