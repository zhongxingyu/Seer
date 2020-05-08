 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import org.openide.util.NbBundle;
 
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.custom.objectrenderer.utils.billing.BillingPopup;
 import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
 import de.cismet.cids.custom.objectrenderer.wunda_blau.NivellementPunktAggregationRenderer;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.cids.editors.DefaultBindableReferenceCombo;
 import de.cismet.cids.editors.DefaultCustomObjectEditor;
 import de.cismet.cids.editors.EditorClosedEvent;
 import de.cismet.cids.editors.EditorSaveListener;
 import de.cismet.cids.editors.converters.DoubleToStringConverter;
 
 import de.cismet.cismap.cids.geometryeditor.DefaultCismapGeometryComboBoxEditor;
 
 import de.cismet.cismap.commons.Crs;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.gui.measuring.MeasuringComponent;
 import de.cismet.cismap.commons.gui.printing.JasperDownload;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
 import de.cismet.security.exceptions.MissingArgumentException;
 import de.cismet.security.exceptions.NoHandlerForURLException;
 import de.cismet.security.exceptions.RequestFailedException;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.TitleComponentProvider;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class NivellementPunktEditor extends javax.swing.JPanel implements DisposableCidsBeanStore,
     TitleComponentProvider,
     FooterComponentProvider,
     BorderProvider,
     RequestsFullSizeComponent,
     EditorSaveListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(NivellementPunktEditor.class);
     public static final String[] SUFFIXES = new String[] { "tif", "jpg", "tiff", "jpeg" };
     protected static XBoundingBox INITIAL_BOUNDINGBOX = new XBoundingBox(
             2583621.251964098d,
             5682507.032498134d,
             2584022.9413952776d,
             5682742.852810634d,
             AlkisConstants.COMMONS.SRS_SERVICE,
             true);
     protected static Crs CRS = new Crs(
             AlkisConstants.COMMONS.SRS_SERVICE,
             AlkisConstants.COMMONS.SRS_SERVICE,
             AlkisConstants.COMMONS.SRS_SERVICE,
             true,
             true);
 
     protected static final Converter<Double, String> CONVERTER_HOEHE = new DoubleToStringConverter();
 
     //~ Instance fields --------------------------------------------------------
 
     protected CidsBean cidsBean;
     protected boolean readOnly;
 
     protected String oldDgkBlattnummer;
     protected String oldLaufendeNummer;
     protected String urlOfDocument;
     protected RefreshDocumentWorker currentRefreshDocumentWorker;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup bgrControls;
     private javax.swing.JButton btnHome;
     private javax.swing.JButton btnOpen;
     private javax.swing.JButton btnReport;
     private javax.swing.JCheckBox chkHistorisch;
     private javax.swing.JComboBox cmbFestlegungsart;
     private javax.swing.JComboBox cmbGeometrie;
     private javax.swing.JComboBox cmbLagegenauigkeit;
     private javax.swing.Box.Filler gluFillDescription;
     private javax.swing.Box.Filler gluFiller;
     private javax.swing.Box.Filler gluFillerControls;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel lblBemerkung;
     private javax.swing.JLabel lblFestlegungsart;
     private javax.swing.JLabel lblGeometrie;
     private javax.swing.JLabel lblHeaderDocument;
     private javax.swing.JLabel lblHistorisch;
     private javax.swing.JLabel lblHoeheUeberNN;
     private javax.swing.JLabel lblLagebezeichnung;
     private javax.swing.JLabel lblLagegenauigkeit;
     private javax.swing.JLabel lblMessungsjahr;
     private javax.swing.JLabel lblMissingRasterdocument;
     private javax.swing.JLabel lblPunktnummerNRW;
     private javax.swing.JLabel lblPunktnummerWUP;
     private javax.swing.JLabel lblPunktnummerWUPSeparator;
     private javax.swing.JLabel lblTitle;
     private de.cismet.cismap.commons.gui.measuring.MeasuringComponent measuringComponent;
     private de.cismet.tools.gui.RoundedPanel pnlControls;
     private de.cismet.tools.gui.RoundedPanel pnlDocument;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderDocument;
     private de.cismet.tools.gui.RoundedPanel pnlSimpleAttributes;
     private javax.swing.JPanel pnlTitle;
     private javax.swing.JScrollPane scpBemerkung;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel4;
     private javax.swing.Box.Filler strFooter;
     private javax.swing.JToggleButton togPan;
     private javax.swing.JToggleButton togZoom;
     private javax.swing.JTextArea txaBemerkung;
     private javax.swing.JTextField txtDGKBlattnummer;
     private javax.swing.JTextField txtHoeheUeberNN;
     private javax.swing.JTextField txtLagebezeichnung;
     private javax.swing.JTextField txtLaufendeNummer;
     private javax.swing.JTextField txtMessungsjahr;
     private javax.swing.JTextField txtPunktnummerNRW;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NivellementPunktEditor object.
      */
     public NivellementPunktEditor() {
         this(false);
     }
     /**
      * Creates new form NivellementPunktEditor.
      *
      * @param  readOnly  DOCUMENT ME!
      */
     public NivellementPunktEditor(final boolean readOnly) {
         this.readOnly = readOnly;
 
         initComponents();
         setOpaque(false);
 
         lblMissingRasterdocument.setVisible(false);
 
         if (readOnly) {
             txtDGKBlattnummer.setEditable(false);
             txtLaufendeNummer.setEditable(false);
             txtPunktnummerNRW.setEditable(false);
             txtLagebezeichnung.setEditable(false);
             cmbLagegenauigkeit.setEditable(false);
             cmbLagegenauigkeit.setEnabled(false);
             txtHoeheUeberNN.setEditable(false);
             txtMessungsjahr.setEditable(false);
             txaBemerkung.setEditable(false);
             cmbFestlegungsart.setEditable(false);
             cmbFestlegungsart.setEnabled(false);
             lblGeometrie.setVisible(false);
             chkHistorisch.setEnabled(false);
         }
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
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         pnlTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         bgrControls = new javax.swing.ButtonGroup();
         strFooter = new javax.swing.Box.Filler(new java.awt.Dimension(0, 22),
                 new java.awt.Dimension(0, 22),
                 new java.awt.Dimension(32767, 22));
         pnlSimpleAttributes = new de.cismet.tools.gui.RoundedPanel();
         txtDGKBlattnummer = new javax.swing.JTextField();
         lblHoeheUeberNN = new javax.swing.JLabel();
         txtHoeheUeberNN = new javax.swing.JTextField();
         lblFestlegungsart = new javax.swing.JLabel();
         cmbFestlegungsart = new DefaultBindableReferenceCombo();
         lblLagebezeichnung = new javax.swing.JLabel();
         txtLagebezeichnung = new javax.swing.JTextField();
         lblLagegenauigkeit = new javax.swing.JLabel();
         cmbLagegenauigkeit = new DefaultBindableReferenceCombo();
         lblMessungsjahr = new javax.swing.JLabel();
         txtMessungsjahr = new javax.swing.JTextField();
         lblPunktnummerNRW = new javax.swing.JLabel();
         txtPunktnummerNRW = new javax.swing.JTextField();
         lblBemerkung = new javax.swing.JLabel();
         scpBemerkung = new javax.swing.JScrollPane();
         txaBemerkung = new javax.swing.JTextArea();
         lblGeometrie = new javax.swing.JLabel();
         if (!readOnly) {
             cmbGeometrie = new DefaultCismapGeometryComboBoxEditor();
         }
         lblPunktnummerWUP = new javax.swing.JLabel();
         lblPunktnummerWUPSeparator = new javax.swing.JLabel();
         gluFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
         txtLaufendeNummer = new javax.swing.JTextField();
         lblHistorisch = new javax.swing.JLabel();
         chkHistorisch = new javax.swing.JCheckBox();
         gluFillDescription = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
         pnlDocument = new de.cismet.tools.gui.RoundedPanel();
         pnlHeaderDocument = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderDocument = new javax.swing.JLabel();
         measuringComponent = new MeasuringComponent(INITIAL_BOUNDINGBOX, CRS);
         lblMissingRasterdocument = new javax.swing.JLabel();
         pnlControls = new de.cismet.tools.gui.RoundedPanel();
         togPan = new javax.swing.JToggleButton();
         togZoom = new javax.swing.JToggleButton();
         btnHome = new javax.swing.JButton();
         semiRoundedPanel4 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel3 = new javax.swing.JLabel();
         btnOpen = new javax.swing.JButton();
         btnReport = new javax.swing.JButton();
         gluFillerControls = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
 
         pnlTitle.setOpaque(false);
         pnlTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblTitle.setForeground(java.awt.Color.white);
         lblTitle.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblTitle.text"));     // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlTitle.add(lblTitle, gridBagConstraints);
 
         setOpaque(false);
         setLayout(new java.awt.GridBagLayout());
 
         pnlSimpleAttributes.setAlpha(0);
         pnlSimpleAttributes.setLayout(new java.awt.GridBagLayout());
 
         txtDGKBlattnummer.setMinimumSize(new java.awt.Dimension(100, 20));
         txtDGKBlattnummer.setPreferredSize(new java.awt.Dimension(100, 20));
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.dgk_blattnummer}"),
                 txtDGKBlattnummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         txtDGKBlattnummer.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusGained(final java.awt.event.FocusEvent evt) {
                     txtDGKBlattnummerFocusGained(evt);
                 }
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     txtDGKBlattnummerFocusLost(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.25;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(txtDGKBlattnummer, gridBagConstraints);
 
         lblHoeheUeberNN.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblHoeheUeberNN.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblHoeheUeberNN, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.hoehe_ueber_nn}"),
                 txtHoeheUeberNN,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setConverter(CONVERTER_HOEHE);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(txtHoeheUeberNN, gridBagConstraints);
 
         lblFestlegungsart.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblFestlegungsart.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblFestlegungsart, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.festlegungsart}"),
                 cmbFestlegungsart,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(cmbFestlegungsart, gridBagConstraints);
 
         lblLagebezeichnung.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblLagebezeichnung.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         pnlSimpleAttributes.add(lblLagebezeichnung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.lagebezeichnung}"),
                 txtLagebezeichnung,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         pnlSimpleAttributes.add(txtLagebezeichnung, gridBagConstraints);
 
         lblLagegenauigkeit.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblLagegenauigkeit.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblLagegenauigkeit, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.lagegenauigkeit}"),
                 cmbLagegenauigkeit,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(cmbLagegenauigkeit, gridBagConstraints);
 
         lblMessungsjahr.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblMessungsjahr.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblMessungsjahr, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.messungsjahr}"),
                 txtMessungsjahr,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(txtMessungsjahr, gridBagConstraints);
 
         lblPunktnummerNRW.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblPunktnummerNRW.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblPunktnummerNRW, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.punktnummer_nrw}"),
                 txtPunktnummerNRW,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(txtPunktnummerNRW, gridBagConstraints);
 
         lblBemerkung.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblBemerkung.text")); // NOI18N
         lblBemerkung.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridheight = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblBemerkung, gridBagConstraints);
 
         txaBemerkung.setColumns(20);
         txaBemerkung.setRows(5);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bemerkung}"),
                 txaBemerkung,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         scpBemerkung.setViewportView(txaBemerkung);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridheight = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(scpBemerkung, gridBagConstraints);
 
         lblGeometrie.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblGeometrie.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblGeometrie, gridBagConstraints);
 
         if (!readOnly) {
             binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                     org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                     this,
                     org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geometrie}"),
                     cmbGeometrie,
                     org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
             binding.setConverter(((DefaultCismapGeometryComboBoxEditor)cmbGeometrie).getConverter());
             bindingGroup.addBinding(binding);
         }
         if (!readOnly) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 5;
             gridBagConstraints.gridy = 7;
             gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
             gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
             pnlSimpleAttributes.add(cmbGeometrie, gridBagConstraints);
         }
 
         lblPunktnummerWUP.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblPunktnummerWUP.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblPunktnummerWUP, gridBagConstraints);
 
         lblPunktnummerWUPSeparator.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblPunktnummerWUPSeparator.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         pnlSimpleAttributes.add(lblPunktnummerWUPSeparator, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.gridwidth = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlSimpleAttributes.add(gluFiller, gridBagConstraints);
 
         txtLaufendeNummer.setColumns(3);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.laufende_nummer}"),
                 txtLaufendeNummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         txtLaufendeNummer.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusGained(final java.awt.event.FocusEvent evt) {
                     txtLaufendeNummerFocusGained(evt);
                 }
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     txtLaufendeNummerFocusLost(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.25;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(txtLaufendeNummer, gridBagConstraints);
 
         lblHistorisch.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblHistorisch.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(lblHistorisch, gridBagConstraints);
 
         chkHistorisch.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.chkHistorisch.text")); // NOI18N
         chkHistorisch.setContentAreaFilled(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.historisch}"),
                 chkHistorisch,
                org.jdesktop.beansbinding.BeanProperty.create("selected"),
                "");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlSimpleAttributes.add(chkHistorisch, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlSimpleAttributes.add(gluFillDescription, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(8, 0, 10, 0);
         add(pnlSimpleAttributes, gridBagConstraints);
 
         pnlDocument.setLayout(new java.awt.GridBagLayout());
 
         pnlHeaderDocument.setBackground(java.awt.Color.darkGray);
         pnlHeaderDocument.setLayout(new java.awt.GridBagLayout());
 
         lblHeaderDocument.setForeground(java.awt.Color.white);
         lblHeaderDocument.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblHeaderDocument.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlHeaderDocument.add(lblHeaderDocument, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         pnlDocument.add(pnlHeaderDocument, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlDocument.add(measuringComponent, gridBagConstraints);
 
         lblMissingRasterdocument.setBackground(java.awt.Color.white);
         lblMissingRasterdocument.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblMissingRasterdocument.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/missingRasterdocument.png"))); // NOI18N
         lblMissingRasterdocument.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.lblMissingRasterdocument.text"));                                              // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlDocument.add(lblMissingRasterdocument, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
         add(pnlDocument, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         bgrControls.add(togPan);
         togPan.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/pan.gif"))); // NOI18N
         togPan.setSelected(true);
         togPan.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.togPan.text"));                                    // NOI18N
         togPan.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.togPan.toolTipText"));                             // NOI18N
         togPan.setEnabled(false);
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
         pnlControls.add(togPan, gridBagConstraints);
 
         bgrControls.add(togZoom);
         togZoom.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom.gif"))); // NOI18N
         togZoom.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.togZoom.text"));                                    // NOI18N
         togZoom.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.togZoom.toolTipText"));                             // NOI18N
         togZoom.setEnabled(false);
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
         pnlControls.add(togZoom, gridBagConstraints);
 
         btnHome.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/home.gif"))); // NOI18N
         btnHome.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.btnHome.text"));                                    // NOI18N
         btnHome.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.btnHome.toolTipText"));                             // NOI18N
         btnHome.setEnabled(false);
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
         pnlControls.add(btnHome, gridBagConstraints);
 
         semiRoundedPanel4.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel4.setLayout(new java.awt.FlowLayout());
 
         jLabel3.setForeground(new java.awt.Color(255, 255, 255));
         jLabel3.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.jLabel3.text")); // NOI18N
         semiRoundedPanel4.add(jLabel3);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         pnlControls.add(semiRoundedPanel4, gridBagConstraints);
 
         btnOpen.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/folder-image.png"))); // NOI18N
         btnOpen.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.btnOpen.text"));                                            // NOI18N
         btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.btnOpen.toolTipText"));                                     // NOI18N
         btnOpen.setEnabled(false);
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
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 5);
         pnlControls.add(btnOpen, gridBagConstraints);
 
         btnReport.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/printer.png"))); // NOI18N
         btnReport.setText(org.openide.util.NbBundle.getMessage(
                 NivellementPunktEditor.class,
                 "NivellementPunktEditor.btnReport.text"));                                               // NOI18N
         btnReport.setFocusPainted(false);
         btnReport.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnReportActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
         pnlControls.add(btnReport, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
         add(pnlControls, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         add(gluFillerControls, gridBagConstraints);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togPanActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togPanActionPerformed
         measuringComponent.actionPan();
     }                                                                          //GEN-LAST:event_togPanActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togZoomActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togZoomActionPerformed
         measuringComponent.actionZoom();
     }                                                                           //GEN-LAST:event_togZoomActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnHomeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnHomeActionPerformed
         measuringComponent.actionOverview();
     }                                                                           //GEN-LAST:event_btnHomeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnOpenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOpenActionPerformed
         try {
             if (BillingPopup.doBilling(
                             "nivppdf",
                             urlOfDocument,
                             (Geometry)null,
                             new ProductGroupAmount("ea", 1))) {
                 openDoc(urlOfDocument);
             }
         } catch (Exception e) {
             LOG.error("Error when trying to produce a alkis product", e);
             // Hier noch ein Fehlerdialog
         }
     }                                                                           //GEN-LAST:event_btnOpenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  urlOfDocument  DOCUMENT ME!
      */
     private void openDoc(final String urlOfDocument) {
         if (urlOfDocument != null) {
             final URL url;
             try {
                 url = new URL(urlOfDocument);
             } catch (MalformedURLException ex) {
                 LOG.info("Couldn't download nivellement point from '" + urlOfDocument + "'.", ex);
                 return;
             }
 
             CismetThreadPool.execute(new Runnable() {
 
                     @Override
                     public void run() {
                         if (DownloadManagerDialog.showAskingForUserTitle(NivellementPunktEditor.this)) {
                             final String filename = urlOfDocument.substring(urlOfDocument.lastIndexOf("/") + 1);
                             DownloadManager.instance()
                                     .add(
                                         new HttpDownload(
                                             url,
                                             "",
                                             DownloadManagerDialog.getJobname(),
                                             "NivP-Beschreibung",
                                             filename.substring(0, filename.lastIndexOf(".")),
                                             filename.substring(filename.lastIndexOf("."))));
                         }
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtDGKBlattnummerFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtDGKBlattnummerFocusLost
         if ((oldDgkBlattnummer != null) && !oldDgkBlattnummer.equals(txtDGKBlattnummer.getText())) {
             refreshImage();
         }
     }                                                                              //GEN-LAST:event_txtDGKBlattnummerFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtLaufendeNummerFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtLaufendeNummerFocusLost
         if ((oldLaufendeNummer != null) && !oldLaufendeNummer.equals(txtLaufendeNummer.getText())) {
             refreshImage();
         }
     }                                                                              //GEN-LAST:event_txtLaufendeNummerFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtDGKBlattnummerFocusGained(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtDGKBlattnummerFocusGained
         oldDgkBlattnummer = txtDGKBlattnummer.getText();
     }                                                                                //GEN-LAST:event_txtDGKBlattnummerFocusGained
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtLaufendeNummerFocusGained(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtLaufendeNummerFocusGained
         oldLaufendeNummer = txtLaufendeNummer.getText();
     }                                                                                //GEN-LAST:event_txtLaufendeNummerFocusGained
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnReportActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnReportActionPerformed
         try {
             if (BillingPopup.doBilling(
                             "nivppdf",
                             "no.yet",
                             (Geometry)null,
                             new ProductGroupAmount("ea", 1))) {
                 downloadReport();
             }
         } catch (Exception e) {
             LOG.error("Error when trying to produce a alkis product", e);
             // Hier noch ein Fehlerdialog
         }
     }                                                                             //GEN-LAST:event_btnReportActionPerformed
 
     /**
      * DOCUMENT ME!
      */
     private void downloadReport() {
         final Runnable runnable = new Runnable() {
 
                 @Override
                 public void run() {
                     final Collection<CidsBean> nivellementPunkte = new LinkedList<CidsBean>();
                     nivellementPunkte.add(cidsBean);
                     final Collection<NivellementPunktAggregationRenderer.NivellementPunktReportBean> reportBeans =
                         new LinkedList<NivellementPunktAggregationRenderer.NivellementPunktReportBean>();
                     reportBeans.add(new NivellementPunktAggregationRenderer.NivellementPunktReportBean(
                             nivellementPunkte));
                     final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportBeans);
 
                     final HashMap parameters = new HashMap();
 
                     final JasperReport jasperReport;
                     final JasperPrint jasperPrint;
                     try {
                         jasperReport = (JasperReport)JRLoader.loadObject(getClass().getResourceAsStream(
                                     "/de/cismet/cids/custom/wunda_blau/res/nivp.jasper"));
                         jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                     } catch (JRException ex) {
                         LOG.error("Could not generate report for nivellement points.", ex);
 
                         final ErrorInfo ei = new ErrorInfo(NbBundle.getMessage(
                                     NivellementPunktEditor.class,
                                     "NivellementPunktEditor.btnReportActionPerformed(ActionEvent).ErrorInfo.title"),   // NOI18N
                                 NbBundle.getMessage(
                                     NivellementPunktEditor.class,
                                     "NivellementPunktEditor.btnReportActionPerformed(ActionEvent).ErrorInfo.message"), // NOI18N
                                 null,
                                 null,
                                 ex,
                                 Level.ALL,
                                 null);
                         JXErrorPane.showDialog(NivellementPunktEditor.this, ei);
 
                         return;
                     }
 
                     if (DownloadManagerDialog.showAskingForUserTitle(NivellementPunktEditor.this)) {
                         final String jobname = DownloadManagerDialog.getJobname();
 
                         DownloadManager.instance()
                                 .add(new JasperDownload(jasperPrint, jobname, "Nivellement-Punkt", "nivp"));
                     }
                 }
             };
 
         CismetThreadPool.execute(runnable);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Crs getCrs() {
         return CRS;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static XBoundingBox getInitialBoundingBox() {
         return INITIAL_BOUNDINGBOX;
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void refreshImage() {
         if ((currentRefreshDocumentWorker != null) && !currentRefreshDocumentWorker.isDone()) {
             currentRefreshDocumentWorker.cancel(true);
         }
 
         currentRefreshDocumentWorker = new RefreshDocumentWorker();
         CismetThreadPool.execute(currentRefreshDocumentWorker);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   dgkBlattnummer  the value of dgkBlattnummer
      * @param   laufendeNummer  the value of laufendeNummer
      *
      * @return  DOCUMENT ME!
      */
     public static Collection<URL> getCorrespondingURLs(final java.lang.String dgkBlattnummer,
             final String laufendeNummer) {
         final Collection<URL> validURLs = new LinkedList<URL>();
         final StringBuilder urlBuilder = new StringBuilder(AlkisConstants.COMMONS.NIVP_HOST);
         urlBuilder.append('/');
         urlBuilder.append(dgkBlattnummer);
         urlBuilder.append('/');
         urlBuilder.append(AlkisConstants.COMMONS.NIVP_PREFIX);
         urlBuilder.append(dgkBlattnummer);
         urlBuilder.append(getFormattedLaufendeNummer(laufendeNummer));
         urlBuilder.append('.');
         for (final String suffix : SUFFIXES) {
             URL urlToTry = null;
             try {
                 urlToTry = new URL(urlBuilder.toString() + suffix);
             } catch (MalformedURLException ex) {
                 LOG.warn("The URL '" + urlBuilder.toString() + suffix
                             + "' is malformed. Can't load the corresponding picture.",
                     ex);
             }
 
             if (urlToTry != null) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Valid URL: " + urlToTry.toExternalForm());
                 }
 
                 validURLs.add(urlToTry);
             }
         }
         return validURLs;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   laufendeNummer  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected static String getFormattedLaufendeNummer(final String laufendeNummer) {
         final StringBuilder result;
 
         if (laufendeNummer == null) {
             result = new StringBuilder("000");
         } else {
             result = new StringBuilder(laufendeNummer);
         }
 
         while (result.length() < 3) {
             result.insert(0, "0");
         }
 
         return result.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsBean  DOCUMENT ME!
      */
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         bindingGroup.unbind();
 
         if (cidsBean != null) {
             this.cidsBean = cidsBean;
 
             if (MetaObject.NEW == this.cidsBean.getMetaObject().getStatus()) {
                 try {
                     this.cidsBean.setProperty("dgk_blattnummer", "0000");
                     this.cidsBean.setProperty("laufende_nummer", "000");
                     this.cidsBean.setProperty("historisch", "false");
                     this.cidsBean.setProperty("hoehe_ueber_nn", Double.valueOf(0D));
                 } catch (Exception ex) {
                     LOG.warn("Could not set initial properties to new NivellementPunkt", ex);
                 }
             }
 
             DefaultCustomObjectEditor.setMetaClassInformationToMetaClassStoreComponentsInBindingGroup(
                 bindingGroup,
                 this.cidsBean);
             bindingGroup.bind();
 
             final String dgkBlattnummer = (String)cidsBean.getProperty("dgk_blattnummer");
             final String laufendeNummer = (String)cidsBean.getProperty("laufende_nummer");
             lblTitle.setText(NbBundle.getMessage(NivellementPunktEditor.class, "NivellementPunktEditor.lblTitle.text")
                         + " " + dgkBlattnummer + getFormattedLaufendeNummer(laufendeNummer));
             refreshImage();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         bindingGroup.unbind();
         // dispose panels here if necessary
         measuringComponent.dispose();
         if (!readOnly) {
             ((DefaultCismapGeometryComboBoxEditor)cmbGeometrie).dispose();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getTitleComponent() {
         return pnlTitle;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getTitleBorder() {
         return new EmptyBorder(10, 10, 10, 10);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getFooterBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getCenterrBorder() {
         return new EmptyBorder(0, 5, 0, 5);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  event  DOCUMENT ME!
      */
     @Override
     public void editorClosed(final EditorClosedEvent event) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public boolean prepareForSave() {
         boolean save = true;
 
         Float hoehe = null;
         try {
             hoehe = Float.valueOf(txtHoeheUeberNN.getText().replace(',', '.'));
         } catch (NumberFormatException e) {
         }
 
         if (hoehe == null) {
             save = false;
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 "Die angegebene Höhe ist ungültig.",
                 "Fehler aufgetreten",
                 JOptionPane.WARNING_MESSAGE);
         }
 
         if ((txtDGKBlattnummer.getText() == null) || (txtDGKBlattnummer.getText().trim().length() <= 0)) {
             save = false;
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 "Die angegebene DGK-Blattnummer ist ungültig.",
                 "Fehler aufgetreten",
                 JOptionPane.WARNING_MESSAGE);
         }
 
         if ((txtLaufendeNummer.getText() == null) || (txtLaufendeNummer.getText().trim().length() <= 0)) {
             save = false;
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 "Die angegebene laufende Nummer ist ungültig.",
                 "Fehler aufgetreten",
                 JOptionPane.WARNING_MESSAGE);
         }
 
         return save;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getFooterComponent() {
         return strFooter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
 //        final CidsBean cb = DevelopmentTools.createCidsBeanFromRMIConnectionOnLocalhost(
 //                "WUNDA_BLAU",
 //                "Administratoren",
 //                "admin",
 //                "sb",
 //                "GEOM",
 //                83318655);
 //
 //        System.out.println(cb.getProperty("geo_field"));
 //        cb.setProperty("geo_field", null);
 //        System.out.println("fertich");
 //        System.exit(0);
 
         DevelopmentTools.createEditorInFrameFromRMIConnectionOnLocalhost(
             "WUNDA_BLAU",
             "Administratoren",
             "admin",
             "sb",
             "nivellement_punkt",
             6818,
 //            6833,
             1024,
             768);
 
 //        DevelopmentTools.createRendererInFrameFromRMIConnectionOnLocalhost(
 //            "WUNDA_BLAU",
 //            "Administratoren",
 //            "admin",
 //            "sb",
 //            "nivellement_punkt",
 //            4349,
 //            "Renderer",
 //            1024,
 //            768);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class RefreshDocumentWorker extends SwingWorker<BufferedImage, Object> {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         @Override
         protected BufferedImage doInBackground() throws Exception {
             final Collection<URL> validURLs = getCorrespondingURLs(txtDGKBlattnummer.getText(),
                     txtLaufendeNummer.getText());
 
             InputStream streamToReadFrom = null;
             for (final URL url : validURLs) {
                 try {
                     streamToReadFrom = WebAccessManager.getInstance().doRequest(url);
                     urlOfDocument = url.toExternalForm();
                     break;
                 } catch (MissingArgumentException ex) {
                     LOG.warn("Could not read document from URL '" + url.toExternalForm() + "'. Skipping this url.", ex);
                 } catch (AccessMethodIsNotSupportedException ex) {
                     LOG.warn("Can't access document URL '" + url.toExternalForm()
                                 + "' with default access method. Skipping this url.",
                         ex);
                 } catch (RequestFailedException ex) {
                     LOG.warn("Requesting document from URL '" + url.toExternalForm() + "' failed. Skipping this url.",
                         ex);
                 } catch (NoHandlerForURLException ex) {
                     LOG.warn("Can't handle URL '" + url.toExternalForm() + "'. Skipping this url.", ex);
                 } catch (Exception ex) {
                     LOG.warn("An exception occurred while opening URL '" + url.toExternalForm()
                                 + "'. Skipping this url.",
                         ex);
                 }
             }
 
             BufferedImage result = null;
             if (streamToReadFrom == null) {
                 LOG.error("Couldn't get a connection to associated document.");
                 urlOfDocument = null;
             } else if (LOG.isDebugEnabled()) {
                 LOG.debug("Loading '" + urlOfDocument + "'.");
             }
 
             try {
                 result = ImageIO.read(streamToReadFrom);
             } catch (IOException ex) {
                 LOG.warn("Could not read image.", ex);
                 urlOfDocument = null;
             } finally {
                 try {
                     if (streamToReadFrom != null) {
                         streamToReadFrom.close();
                     }
                 } catch (IOException ex) {
                     LOG.warn("Couldn't close the stream.", ex);
                 }
             }
 
             return result;
         }
 
         /**
          * DOCUMENT ME!
          */
         @Override
         protected void done() {
             BufferedImage document = null;
             try {
                 if (!isCancelled()) {
                     document = get();
                 }
             } catch (InterruptedException ex) {
                 LOG.warn("Was interrupted while refreshing document.", ex);
             } catch (ExecutionException ex) {
                 LOG.warn("There was an exception while refreshing document.", ex);
             }
 
             measuringComponent.reset();
             if ((document != null) && !isCancelled()) {
                 measuringComponent.setVisible(true);
                 lblMissingRasterdocument.setVisible(false);
                 measuringComponent.addImage(document);
                 measuringComponent.zoomToFeatureCollection();
                 btnHome.setEnabled(true);
                 btnOpen.setEnabled(BillingPopup.isBillingAllowed());
                 btnReport.setEnabled(BillingPopup.isBillingAllowed());
                 togPan.setEnabled(true);
                 togZoom.setEnabled(true);
             } else {
                 measuringComponent.setVisible(false);
                 lblMissingRasterdocument.setVisible(true);
                 btnHome.setEnabled(false);
                 btnOpen.setEnabled(false);
                 btnReport.setEnabled(false);
                 togPan.setEnabled(false);
                 togZoom.setEnabled(false);
             }
         }
     }
 }
