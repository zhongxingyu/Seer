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
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.PrecisionModel;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.Exceptions;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.io.IOException;
 
 import java.text.DecimalFormatSymbols;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.border.LineBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.plaf.TabbedPaneUI;
 import javax.swing.plaf.basic.BasicButtonUI;
 
 import de.cismet.cids.custom.objectrenderer.utils.billing.BillingPopup;
 import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.utils.butler.ButlerFormat;
 import de.cismet.cids.custom.utils.butler.ButlerProduct;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.MultipleDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class Butler2Dialog extends javax.swing.JDialog implements DocumentListener, ListSelectionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(Butler2Dialog.class);
     private static HashMap<String, CoordWrapper> rahmenKartenMap = new HashMap<String, CoordWrapper>();
     private static final String FELDVERGLEICH = "0903";
     private static final String FELDVERGLEICH_BOX = "600m x 350m";
     private static final String RASTER_DATEN_BILLING_KEY = "skmekomtiff";
     private static final String DXF_BILLING_KEY = "skmekomdxf";
 
     static {
         final Properties prop = new Properties();
         try {
             prop.load(Butler2Dialog.class.getResourceAsStream("rahmenkarten.properties"));
             loadPropertiesIntoMap(prop);
         } catch (IOException ex) {
             LOG.error("Could not read property file with defined boxes for butler 1", ex);
         }
     }
 
     //~ Instance fields --------------------------------------------------------
 
     final java.text.DecimalFormat coordFormatter = new java.text.DecimalFormat("#.###");
     private ArrayList<PredefinedBoxes> boxes;
     private DefaultStyledFeature rectangleFeature;
     private MappingComponent map = new MappingComponent();
     private boolean mapInitDone = false;
     private PredefinedBoxes feldVergleichBox = null;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnCreate;
     private de.cismet.cids.custom.butler.Butler2ProductPanel butler2ProductPanel1;
     private javax.swing.JComboBox cbPointGeom;
     private javax.swing.JComboBox cbSize;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JLabel lblLowerPosition;
     private javax.swing.JLabel lblPointGeoms;
     private javax.swing.JLabel lblRahmenkartenNr;
     private javax.swing.JLabel lblRequestNumber;
     private javax.swing.JLabel lblSize;
     private javax.swing.JPanel pnlControls;
     private javax.swing.JPanel pnlMap;
     private javax.swing.JPanel pnlMapSettings;
     private javax.swing.JPanel pnlProductSettings;
     private javax.swing.JPanel pnlRequestNumber;
     private javax.swing.JTabbedPane tbpProducts;
     private javax.swing.JTextField tfLowerE;
     private javax.swing.JTextField tfLowerN;
     private javax.swing.JTextField tfOrderId;
     private javax.swing.JTextField tfRahmenkartenNr;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Butler2Dialog.
      *
      * @param  parent  DOCUMENT ME!
      * @param  modal   DOCUMENT ME!
      */
     public Butler2Dialog(final java.awt.Frame parent, final boolean modal) {
         super(parent, modal);
         final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
         formatSymbols.setDecimalSeparator('.');
         coordFormatter.setDecimalFormatSymbols(formatSymbols);
         this.setTitle(null);
         boxes = PredefinedBoxes.butler2Boxes;
         initComponents();
         butler2ProductPanel1.addProductListSelectionListener(this);
         tfLowerE.getDocument().addDocumentListener(this);
         tfLowerN.getDocument().addDocumentListener(this);
         tbpProducts.addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final MouseEvent me) {
                     final int tabNr = ((TabbedPaneUI)tbpProducts.getUI()).tabForCoordinate(
                             tbpProducts,
                             me.getX(),
                             me.getY());
                     if (tabNr == (tbpProducts.getTabCount() - 1)) {
                         // if we have only 1 tab we need to enable the clear button on the first tab
                         if (tbpProducts.getTabCount() == 2) {
                             tbpProducts.setTabComponentAt(0, getTabComponent(true, 1));
                         }
                         addCloseableTab();
 
                         tbpProducts.setSelectedIndex(tbpProducts.getTabCount() - 2);
                     }
                 }
             });
         tbpProducts.setBorder(null);
         tbpProducts.setTabComponentAt(0, getTabComponent(false));
         tbpProducts.setToolTipTextAt(0, "Produkt 1");
         // if a rahmenkartennummer is provided we set middleE and middleN programmatically
         tfRahmenkartenNr.getDocument().addDocumentListener(new DocumentListener() {
 
                 @Override
                 public void insertUpdate(final DocumentEvent e) {
                     updateForRahmenKartenNr();
                 }
 
                 @Override
                 public void removeUpdate(final DocumentEvent e) {
                     updateForRahmenKartenNr();
                 }
 
                 @Override
                 public void changedUpdate(final DocumentEvent e) {
                     updateForRahmenKartenNr();
                 }
             });
         for (final PredefinedBoxes box : boxes) {
             if (box.getDisplayName().equals(FELDVERGLEICH_BOX)) {
                 feldVergleichBox = box;
                 break;
             }
         }
         final JPanel addPan = new JPanel();
         addPan.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
         addPan.setOpaque(false);
         addPan.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
         final JLabel addLabel = new JLabel("+");
         addLabel.setOpaque(false);
         addLabel.setBorder(null);
         addLabel.setFocusable(false);
         addPan.add(addLabel);
         tbpProducts.addTab("+", null);
         tbpProducts.setTabComponentAt(tbpProducts.getTabCount() - 1, addPan);
         initMap();
         pnlMap.setLayout(new BorderLayout());
         pnlMap.add(map, BorderLayout.CENTER);
         cbPointGeomActionPerformed(null);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void updateForRahmenKartenNr() {
         CoordWrapper coord = null;
         if (tfRahmenkartenNr.getText().length() == 5) {
             coord = rahmenKartenMap.get(tfRahmenkartenNr.getText());
             if (coord != null) {
                 tfLowerE.setText("" + coord.getMiddleE());
                 tfLowerN.setText("" + coord.getMiddleN());
             } else {
                 JOptionPane.showMessageDialog(
                     StaticSwingTools.getParentFrame(Butler2Dialog.this),
                     org.openide.util.NbBundle.getMessage(
                         Butler2Dialog.class,
                         "Butler2Dialog.RahmenkartenNrCheck.JOptionPane.message"),
                     org.openide.util.NbBundle.getMessage(
                         Butler2Dialog.class,
                         "Butler2Dialog.RahmenkartenNrCheck.JOptionPane.title"),
                     JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  prop  DOCUMENT ME!
      */
     private static void loadPropertiesIntoMap(final Properties prop) {
         final Enumeration keys = prop.propertyNames();
         final ArrayList<String> keyList = new ArrayList<String>();
         while (keys.hasMoreElements()) {
             final String key = (String)keys.nextElement();
             keyList.add(key);
         }
         for (final String key : keyList) {
             final String[] splittedVal = ((String)prop.getProperty(key)).split(";");
             final double middleE = Double.parseDouble(splittedVal[0]);
             final double middleN = Double.parseDouble(splittedVal[1]);
             final CoordWrapper coord = new CoordWrapper(middleE, middleN);
             rahmenKartenMap.put(key, coord);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         pnlProductSettings = new javax.swing.JPanel();
         tbpProducts = new javax.swing.JTabbedPane();
         butler2ProductPanel1 = new de.cismet.cids.custom.butler.Butler2ProductPanel();
         pnlMapSettings = new javax.swing.JPanel();
         lblLowerPosition = new javax.swing.JLabel();
         lblSize = new javax.swing.JLabel();
         cbSize = new javax.swing.JComboBox();
         pnlMap = new javax.swing.JPanel();
         lblRahmenkartenNr = new javax.swing.JLabel();
         lblPointGeoms = new javax.swing.JLabel();
         tfRahmenkartenNr = new javax.swing.JTextField();
         cbPointGeom = new ButlerGeometryComboBox(ButlerGeometryComboBox.GEOM_FILTER_TYPE.POINT);
         jPanel1 = new javax.swing.JPanel();
         tfLowerN = new javax.swing.JTextField();
         tfLowerE = new javax.swing.JTextField();
         pnlControls = new javax.swing.JPanel();
         btnCreate = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         pnlRequestNumber = new javax.swing.JPanel();
         lblRequestNumber = new javax.swing.JLabel();
         tfOrderId = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.title")); // NOI18N
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         pnlProductSettings.setMinimumSize(new java.awt.Dimension(450, 300));
         pnlProductSettings.setPreferredSize(new java.awt.Dimension(450, 300));
         pnlProductSettings.setLayout(new java.awt.GridBagLayout());
 
         tbpProducts.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         tbpProducts.setMinimumSize(new java.awt.Dimension(400, 400));
         tbpProducts.setPreferredSize(new java.awt.Dimension(448, 407));
 
         butler2ProductPanel1.setBorder(null);
         butler2ProductPanel1.setMinimumSize(new java.awt.Dimension(400, 291));
         butler2ProductPanel1.setPreferredSize(new java.awt.Dimension(442, 372));
         tbpProducts.addTab(org.openide.util.NbBundle.getMessage(
                 Butler2Dialog.class,
                 "Butler2Dialog.butler2ProductPanel1.TabConstraints.tabTitle"),
             butler2ProductPanel1); // NOI18N
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlProductSettings.add(tbpProducts, gridBagConstraints);
         tbpProducts.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         Butler2Dialog.class,
                         "Butler2Dialog.tbpProducts.AccessibleContext.accessibleName")); // NOI18N
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlProductSettings, gridBagConstraints);
 
         pnlMapSettings.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblLowerPosition,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.lblLowerPosition.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         pnlMapSettings.add(lblLowerPosition, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblSize,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.lblSize.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(lblSize, gridBagConstraints);
 
         cbSize.setPreferredSize(new java.awt.Dimension(150, 27));
 
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${boxes}");
         final org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJComboBoxBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         cbSize);
         bindingGroup.addBinding(jComboBoxBinding);
 
         cbSize.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbSizeActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(cbSize, gridBagConstraints);
 
         pnlMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         final javax.swing.GroupLayout pnlMapLayout = new javax.swing.GroupLayout(pnlMap);
         pnlMap.setLayout(pnlMapLayout);
         pnlMapLayout.setHorizontalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 435,
                 Short.MAX_VALUE));
         pnlMapLayout.setVerticalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 299,
                 Short.MAX_VALUE));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         pnlMapSettings.add(pnlMap, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblRahmenkartenNr,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.lblRahmenkartenNr.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         pnlMapSettings.add(lblRahmenkartenNr, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblPointGeoms,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.lblPointGeoms.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         pnlMapSettings.add(lblPointGeoms, gridBagConstraints);
 
         tfRahmenkartenNr.setText(org.openide.util.NbBundle.getMessage(
                 Butler2Dialog.class,
                 "Butler2Dialog.tfRahmenkartenNr.text")); // NOI18N
         tfRahmenkartenNr.setEnabled(false);
         tfRahmenkartenNr.setMinimumSize(new java.awt.Dimension(70, 27));
         tfRahmenkartenNr.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(tfRahmenkartenNr, gridBagConstraints);
 
         final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${rectangleFeature.geometry}"),
                 cbPointGeom,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         cbPointGeom.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbPointGeomActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(cbPointGeom, gridBagConstraints);
 
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         tfLowerN.setText(org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.tfLowerN.text")); // NOI18N
         tfLowerN.setMinimumSize(new java.awt.Dimension(70, 27));
         tfLowerN.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 20;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         jPanel1.add(tfLowerN, gridBagConstraints);
 
         tfLowerE.setText(org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.tfLowerE.text")); // NOI18N
         tfLowerE.setMinimumSize(new java.awt.Dimension(70, 27));
         tfLowerE.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 20;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         jPanel1.add(tfLowerE, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(jPanel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(45, 10, 10, 10);
         getContentPane().add(pnlMapSettings, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnCreate,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.btnCreate.text")); // NOI18N
         btnCreate.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCreateActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         pnlControls.add(btnCreate, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnCancel,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.btnCancel.text")); // NOI18N
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         pnlControls.add(btnCancel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlControls, gridBagConstraints);
 
         pnlRequestNumber.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblRequestNumber,
             org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.lblRequestNumber.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 33);
         pnlRequestNumber.add(lblRequestNumber, gridBagConstraints);
 
         tfOrderId.setText(org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.tfOrderId.text")); // NOI18N
         tfOrderId.setMinimumSize(new java.awt.Dimension(70, 27));
         tfOrderId.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
         pnlRequestNumber.add(tfOrderId, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
         getContentPane().add(pnlRequestNumber, gridBagConstraints);
 
         bindingGroup.bind();
 
         pack();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
         this.dispose();                                                           // TODO add your handling code here:
     }                                                                             //GEN-LAST:event_btnCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbSizeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbSizeActionPerformed
         changeMap();
     }                                                                          //GEN-LAST:event_cbSizeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCreateActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCreateActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final StringBuilder missConfiguredProducts = new StringBuilder();
                     boolean showErrorPane = false;
                     int missConfigPrdCount = 0;
                     for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                         final Butler2ProductPanel productPanel = (Butler2ProductPanel)tbpProducts.getComponentAt(i);
                         final ButlerProduct bp = productPanel.getSelectedProduct();
                         if (!isProductConfigurationValid(bp)) {
                             showErrorPane = true;
                             missConfigPrdCount++;
                             missConfiguredProducts.append(i + 1);
                             missConfiguredProducts.append(", ");
                         }
                     }
                     if (showErrorPane) {
                         final String productNumbers = missConfiguredProducts.toString()
                                     .substring(0, missConfiguredProducts.toString().length() - 2);
                         final String message = (missConfigPrdCount == 1)
                             ? org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.ProductConfigCheck.JOptionPane.singularMessage")
                             : org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.ProductConfigCheck.JOptionPane.multiMessage");
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler2Dialog.this),
                             String.format(
                                 message,
                                 productNumbers),                                       // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 Butler1ProductPanel.class,
                                 "Butler1Dialog.ProductConfigCheck.JOptionPane.title"), // NOI18N
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     // check the geom coordinates
                     if ((rectangleFeature == null) || (rectangleFeature.getGeometry() == null)) {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler2Dialog.this),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.GeomConfigCheck.JOptionPane.message"),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.GeomConfigCheck.JOptionPane.title"),
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
 
                     // check that the orderId only consits of [a-z], [A-Z],[0-9] and _
                     final String orderId = tfOrderId.getText();
                     if (!orderId.matches("[a-zA-Z0-9_]*")) {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler2Dialog.this),
                             org.openide.util.NbBundle.getMessage(
                                 Butler2Dialog.class,
                                 "Butler2Dialog.OrderIdCheck.JOptionPane.message"),
                             org.openide.util.NbBundle.getMessage(
                                 Butler2Dialog.class,
                                 "Butler2Dialog.OrderIdCheck.JOptionPane.title"),
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     final Geometry g = rectangleFeature.getGeometry();
                     final Point p = g.getCentroid();
                     final double middleX = p.getX();
                     final double middleY = p.getY();
 
                     // for each product tab we have to create one download
                     final StringBuilder jobnameBuilder = new StringBuilder();
                     if (DownloadManagerDialog.showAskingForUserTitle(
                                     CismapBroker.getInstance().getMappingComponent())) {
                         final String jobname = (!DownloadManagerDialog.getJobname().equals(""))
                             ? DownloadManagerDialog.getJobname() : null;
                         jobnameBuilder.append(jobname);
                     } else {
                         jobnameBuilder.append("");
                     }
 
                     final PredefinedBoxes box = (PredefinedBoxes)cbSize.getSelectedItem();
                     final ArrayList<ButlerDownload> downloads = new ArrayList<ButlerDownload>();
                     for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                         final Butler2ProductPanel productPanel = (Butler2ProductPanel)tbpProducts.getComponentAt(i);
                         final ButlerProduct bp = productPanel.getSelectedProduct();
                         final ButlerFormat format = bp.getFormat();
                         final ButlerDownload download = new ButlerDownload(
                                 jobnameBuilder.toString(),
                                 tfOrderId.getText()
                                         + "_"
                                         + (i + 1),
                                 bp,
                                 box.getKey(),
                                 middleX,
                                 middleY);
                         String productKey = "";
                         if ((format != null) && format.getKey().equals("dxf")) {
                             productKey = DXF_BILLING_KEY;
                         } else {
                             productKey = RASTER_DATEN_BILLING_KEY;
                         }
                         final String requestNr = tfOrderId.getText().trim() + "_" + i;
                         final ArrayList<ProductGroupAmount> list = productPanel.getProductGroupAmounts();
                         final ProductGroupAmount[] groupAmounts = list.toArray(new ProductGroupAmount[list.size()]);
                         try {
                             if (BillingPopup.doBilling(productKey, "butler 2", requestNr, null, groupAmounts)) {
                                 downloads.add(download);
                             }
                         } catch (Exception ex) {
                             LOG.error("error during billing for ALKIS Datenausgabe", ex);
                         }
                     }
                     DownloadManager.instance()
                             .add(new MultipleDownload(downloads, "Butler Downloads " + tfOrderId.getText()));
 
                     Butler2Dialog.this.dispose();
                 }
             });
     } //GEN-LAST:event_btnCreateActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbPointGeomActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbPointGeomActionPerformed
         final Object obj = cbPointGeom.getSelectedItem();
         if ((obj != null) && (obj instanceof Point)) {
             final Point p = (Point)obj;
             tfLowerE.getDocument().removeDocumentListener(this);
             tfLowerN.getDocument().removeDocumentListener(this);
             tfLowerE.setText(coordFormatter.format(p.getX()));
             tfLowerN.setText(coordFormatter.format(p.getY()));
             changeMap();
             tfLowerE.getDocument().addDocumentListener(this);
             tfLowerN.getDocument().addDocumentListener(this);
         }
     }                                                                               //GEN-LAST:event_cbPointGeomActionPerformed
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         final Runnable mapRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     final ActiveLayerModel mappingModel = new ActiveLayerModel();
                     mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                     mappingModel.addHome(getBoundingBox());
 
                     final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(
                                 AlkisConstants.COMMONS.MAP_CALL_STRING));
                     swms.setName("butler-background");
 
                     // add the raster layer to the model
                     mappingModel.addLayer(swms);
                     // set the model
                     map.setMappingModel(mappingModel);
                     // initial positioning of the map
                     final int duration = map.getAnimationDuration();
                     map.setAnimationDuration(0);
 //                    map.gotoInitialBoundingBox();
                     // interaction mode
                     map.setInteractionMode(MappingComponent.ZOOM);
                     // finally when all configurations are done ...
                     map.unlock();
                     map.setInteractionMode("MUTE");
                     map.setAnimationDuration(duration);
                     mapInitDone = true;
                 }
 
                 private XBoundingBox getBoundingBox() {
                     final XBoundingBox currBb = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                                 .getCurrentBoundingBox();
                    final XBoundingBox result = new XBoundingBox(currBb.getGeometry().buffer(20));
 
                     return result;
                 }
             };
 
         if (EventQueue.isDispatchThread()) {
             mapRunnable.run();
         } else {
             EventQueue.invokeLater(mapRunnable);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void changeMap() {
         final Runnable mapChangeRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     if (mapInitDone) {
                         if (rectangleFeature == null) {
                             rectangleFeature = createFeature();
                         }
                         final Geometry g = createGeometry();
                         if (g != null) {
                             updateGeomInAllProducts(g);
                             rectangleFeature.setGeometry(g);
                             if (!map.getFeatureCollection().contains(rectangleFeature)) {
                                 map.getFeatureCollection().addFeature(rectangleFeature);
                             } else {
                             }
                             SwingUtilities.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         map.reconsiderFeature(rectangleFeature);
                                         map.zoomToFeatureCollection();
                                     }
                                 });
                         }
                     }
                 }
             };
         if (EventQueue.isDispatchThread()) {
             mapChangeRunnable.run();
         } else {
             EventQueue.invokeLater(mapChangeRunnable);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geom  DOCUMENT ME!
      */
     private void updateGeomInAllProducts(final Geometry geom) {
         for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
             final Butler2ProductPanel productPan = (Butler2ProductPanel)tbpProducts.getComponentAt(i);
             productPan.setGeometry(geom);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private DefaultStyledFeature createFeature() {
         final DefaultStyledFeature dsf = new DefaultStyledFeature();
         final Geometry geom = createGeometry();
         dsf.setGeometry(geom);
         dsf.setTransparency(0.8F);
         dsf.setFillingPaint(new Color(192, 80, 77, 192));
         return dsf;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry createGeometry() {
         double lowerE = 0;
         double lowerN = 0;
         final PredefinedBoxes box = (PredefinedBoxes)cbSize.getSelectedItem();
         try {
             final double middleE = Double.parseDouble(tfLowerE.getText());
             final double middleN = Double.parseDouble(tfLowerN.getText());
             if (!((middleE >= 361000) && (middleE <= 384000) && (middleN >= 5669000) && (middleN <= 5687000))) {
 //                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
 //                    "Die angegebenen Koordinaten liegen außerhalb des gültigen Bereichs",
 //                    "Fehlerhafte Eingaben",
 //                    JOptionPane.ERROR_MESSAGE);
                 return null;
             }
             lowerE = middleE - (box.getEastSize() / 2);
             lowerN = middleN - (box.getNorthSize() / 2);
         } catch (Exception ex) {
 //            if (LOG.isDebugEnabled()) {
 //                LOG.debug("error during change map - very likely a double parsing error", ex);
 //            }
             return null;
         }
         final PredefinedBoxes selectedBoxSize = (PredefinedBoxes)cbSize.getSelectedItem();
         final double upperE = lowerE + selectedBoxSize.getEastSize();
         final double upperN = lowerN + selectedBoxSize.getNorthSize();
         final Coordinate[] coords = new Coordinate[5];
         final Coordinate startCoord = new Coordinate(lowerE, lowerN);
         coords[0] = new Coordinate(lowerE, lowerN);
         final double c1 = lowerN + (Math.abs(upperN - lowerN));
         coords[1] = new Coordinate(lowerE, c1);
         final double c2 = lowerE + (Math.abs(lowerE - upperE));
         coords[2] = new Coordinate(c2, c1);
         coords[3] = new Coordinate(c2, lowerN);
         coords[4] = startCoord;
 
         final int currentSrid = CrsTransformer.extractSridFromCrs(map.getMappingModel().getSrs().getCode());
         final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(
                     PrecisionModel.FLOATING),
                 currentSrid);
 
         final Polygon p = new Polygon(geometryFactory.createLinearRing(coords), null, geometryFactory);
         return p;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ArrayList<PredefinedBoxes> getBoxes() {
         return boxes;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  boxes  DOCUMENT ME!
      */
     public void setBoxes(final ArrayList<PredefinedBoxes> boxes) {
         this.boxes = boxes;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void addCloseableTab() {
         final Component tabComp = getTabComponent(true);
         final int number = tbpProducts.getTabCount();
         final String title = "Produkt " + number;
         final int tabPos = tbpProducts.getTabCount() - 1;
         final Butler2ProductPanel productPan = new Butler2ProductPanel();
         productPan.addProductListSelectionListener(this);
         if ((rectangleFeature != null) && (rectangleFeature.getGeometry() != null)) {
 //            productPan.setGeometry(rectangleFeature.getGeometry());
         }
         tbpProducts.insertTab("", null, productPan, title, tabPos);
 //        tbpProducts.insertTab(null, null, comp, null, tabPos);
         final int tabPosNew = tbpProducts.indexOfComponent(productPan);
         tbpProducts.setTabComponentAt(tabPosNew, tabComp);
         tbpProducts.setBorder(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   isCloseableTab  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Component getTabComponent(final boolean isCloseableTab) {
         final int number = tbpProducts.getTabCount();
         return getTabComponent(isCloseableTab, number);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   isCloseableTab  DOCUMENT ME!
      * @param   tabNumber       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Component getTabComponent(final boolean isCloseableTab, final int tabNumber) {
         final JPanel tabComp = new JPanel();
         tabComp.setLayout(new java.awt.GridBagLayout());
         java.awt.GridBagConstraints gridBagConstraints;
         tabComp.setOpaque(false);
         // add more space to the top of the component
         tabComp.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
         tabComp.setPreferredSize(new Dimension(120, 22));
 
         final String title = "Produkt " + tabNumber;
         final JLabel label = new JLabel(title);
         label.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/butler/page_white.png")));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         tabComp.add(label, gridBagConstraints);
         // add more space between the label and the button
         label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
         if (isCloseableTab) {
             // tab button
             final JButton button = new JButton("x");
 //            button.setIcon();
             button.setPreferredSize(new Dimension(10, 19));
             button.setToolTipText("close this tab");
             // Make the button looks the same for all Laf's
             button.setUI(new BasicButtonUI());
             // Make it transparent
             button.setContentAreaFilled(false);
             // No need to be focusable
             button.setFocusable(false);
             button.setBorder(null);
             button.setBorderPainted(false);
             button.setRolloverEnabled(true);
             // Close the proper tab by clicking the button
             button.addActionListener(new ActionListener() {
 
                     @Override
                     public void actionPerformed(final ActionEvent ae) {
                         final int i = tbpProducts.indexOfTabComponent(tabComp);
                         if (i != -1) {
                             tbpProducts.remove(i);
                         }
                         if (i == tbpProducts.getSelectedIndex()) {
                             if (i == 0) {
                                 tbpProducts.setSelectedIndex(0);
                             } else {
                                 tbpProducts.setSelectedIndex(i - 1);
                             }
                         }
                         updateTabComponents();
                     }
                 });
             button.addMouseListener(new MouseAdapter() {
 
                     @Override
                     public void mouseEntered(final MouseEvent me) {
                         button.setBorder(new LineBorder(Color.GRAY));
                         button.setBorderPainted(true);
                     }
 
                     @Override
                     public void mouseExited(final MouseEvent me) {
                         button.setBorder(null);
                         button.setBorderPainted(false);
                     }
                 });
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 0;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
             tabComp.add(button, gridBagConstraints);
         }
         return tabComp;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateTabComponents() {
         // check for the first button if we need to set the clear button
         if (tbpProducts.getTabCount() <= 2) {
             tbpProducts.setTabComponentAt(0, getTabComponent(false, 1));
         } else {
             for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                 tbpProducts.setTabComponentAt(i, getTabComponent(true, i + 1));
                 tbpProducts.setToolTipTextAt(i, "Produkt " + (i + 1));
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   bp  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isProductConfigurationValid(final ButlerProduct bp) {
         if (bp == null) {
             return false;
         }
         if ((bp.getKey() == null) || bp.getKey().equals("")) {
             return false;
         }
         if ((bp.getFormat() == null) || (bp.getFormat().getKey() == null) || bp.getFormat().getKey().equals("")) {
             return false;
         }
         if ((bp.getResolution() == null) || (bp.getResolution().getKey() == null)
                     || bp.getResolution().getKey().equals("")) {
             return false;
         }
         return true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  the command line arguments
      */
     public static void main(final String[] args) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(Butler2Dialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(Butler2Dialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(Butler2Dialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(Butler2Dialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the dialog */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final Butler2Dialog dialog = new Butler2Dialog(new javax.swing.JFrame(), true);
                     dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 
                             @Override
                             public void windowClosing(final java.awt.event.WindowEvent e) {
                                 System.exit(0);
                             }
                         });
                     dialog.setVisible(true);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  de  DOCUMENT ME!
      */
     @Override
     public void insertUpdate(final DocumentEvent de) {
         changeMap();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  de  DOCUMENT ME!
      */
     @Override
     public void removeUpdate(final DocumentEvent de) {
         changeMap();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  de  DOCUMENT ME!
      */
     @Override
     public void changedUpdate(final DocumentEvent de) {
         changeMap();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  e  DOCUMENT ME!
      */
     @Override
     public void valueChanged(final ListSelectionEvent e) {
         // check if in one product tab the feldvergleich produkt is selected if so set the box size, and disable all
         // other field else enable all other fields
         final SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
 
                 @Override
                 protected Boolean doInBackground() throws Exception {
                     for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                         final Butler2ProductPanel productPanel = (Butler2ProductPanel)tbpProducts.getComponentAt(i);
                         final ButlerProduct product = productPanel.getSelectedProduct();
                         if ((product != null) && (product.getKey() != null)) {
                             if (product.getKey().equals(FELDVERGLEICH)) {
                                 return true;
                             }
                         }
                     }
                     return false;
                 }
 
                 @Override
                 protected void done() {
                     final Boolean isFeldvergleichSelected;
                     try {
                         isFeldvergleichSelected = get();
 
                         if (isFeldvergleichSelected) {
                             tfRahmenkartenNr.setEnabled(true);
                             cbSize.setSelectedItem(feldVergleichBox);
                             cbSize.setEnabled(false);
                             cbPointGeom.setEnabled(false);
                             tfLowerE.setEnabled(false);
                             tfLowerN.setEnabled((false));
                         } else {
                             tfRahmenkartenNr.setEnabled(false);
                             cbSize.setEnabled(true);
                             cbPointGeom.setEnabled(true);
                             tfLowerE.setEnabled(true);
                             tfLowerN.setEnabled((true));
                         }
                     } catch (InterruptedException ex) {
                         Exceptions.printStackTrace(ex);
                     } catch (ExecutionException ex) {
                         Exceptions.printStackTrace(ex);
                     }
                 }
             };
 
         worker.execute();
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class CoordWrapper {
 
         //~ Instance fields ----------------------------------------------------
 
         private double middleE;
         private double middleN;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new CoordWrapper object.
          *
          * @param  middleE  DOCUMENT ME!
          * @param  middleN  DOCUMENT ME!
          */
         public CoordWrapper(final double middleE, final double middleN) {
             this.middleE = middleE;
             this.middleN = middleN;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public double getMiddleE() {
             return middleE;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  middleE  DOCUMENT ME!
          */
         public void setMiddleE(final double middleE) {
             this.middleE = middleE;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public double getMiddleN() {
             return middleN;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  middleN  DOCUMENT ME!
          */
         public void setMiddleN(final double middleN) {
             this.middleN = middleN;
         }
     }
 }
