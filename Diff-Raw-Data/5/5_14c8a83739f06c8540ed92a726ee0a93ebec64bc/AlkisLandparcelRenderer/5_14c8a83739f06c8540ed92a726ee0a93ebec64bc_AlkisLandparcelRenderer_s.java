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
  * Alkis_pointRenderer.java
  *
  * Created on 10.09.2009, 15:52:16
  */
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import Sirius.navigator.ui.ComponentRegistry;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import de.aedsicad.aaaweb.service.alkis.info.ALKISInfoServices;
 import de.aedsicad.aaaweb.service.util.Buchungsblatt;
 import de.aedsicad.aaaweb.service.util.Buchungsstelle;
 import de.aedsicad.aaaweb.service.util.LandParcel;
 
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
 
 import org.jdesktop.swingx.graphics.ReflectionRenderer;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.LayoutManager;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.Caret;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.StyleSheet;
 
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.StyleListCellRenderer;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtil;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisSOAPWorkerService;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.SOAPAccessProvider;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.TitleComponentProvider;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class AlkisLandparcelRenderer extends javax.swing.JPanel implements BorderProvider,
         CidsBeanRenderer,
         TitleComponentProvider,
         FooterComponentProvider {
 
     //~ Static fields/initializers ---------------------------------------------
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     private static final String ALKIS_RES_PACKAGE = ICON_RES_PACKAGE + "alkis/";
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
             AlkisLandparcelRenderer.class);
     private static final String CARD_1 = "CARD_1";
     private static final String CARD_2 = "CARD_2";
     //
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKSNACHWEIS = "custom.alkis.product.flurstuecksnachweis";
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_NRW = "custom.alkis.product.flurstuecks_eigentumsnachweis_nrw";
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM = "custom.alkis.product.flurstuecks_eigentumsnachweis_kom";
     private static final String PRODUCT_ACTION_TAG_KARTE = "custom.alkis.product.karte";
     //
     private static final String BUCHUNGSBLATT_TABLE = "alkis_buchungsblatt";
     private static final String DOMAIN = "WUNDA_BLAU";
     private final boolean buchungsblattPermission;
 
     // <editor-fold defaultstate="collapsed" desc="Border- and Titleprovider method implementations">
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the landparcel
      */
     public Object getLandparcel() {
         return landparcel;
     }
 
     @Override
     public Border getTitleBorder() {
         return new EmptyBorder(10, 10, 10, 10);
     }
 
     @Override
     public Border getFooterBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     @Override
     public Border getCenterrBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     @Override
     public void dispose() {
         bindingGroup.unbind();
         if (!continueInBackground) {
             AlkisSOAPWorkerService.cancel(retrieveBuchungsblaetterWorker);
             setWaiting(false);
         }
         map.dispose();
     }
 // </editor-fold>
     //~ Instance fields --------------------------------------------------------
 // private ImageIcon FORWARD_PRESSED;
 // private ImageIcon FORWARD_SELECTED;
 // private ImageIcon BACKWARD_PRESSED;
 // private ImageIcon BACKWARD_SELECTED;
     private ImageIcon BUCH_PDF;
     private ImageIcon BUCH_HTML;
     private ImageIcon BUCH_EIG_NRW_PDF;
     private ImageIcon BUCH_EIG_NRW_HTML;
     private ImageIcon BUCH_EIG_KOM_PDF;
     private ImageIcon BUCH_EIG_KOM_HTML;
     private ImageIcon KARTE_PDF;
 //    private static final ImageIcon FORWARD_PRESSED;
 //    private static final ImageIcon FORWARD_SELECTED;
 //    private static final ImageIcon BACKWARD_PRESSED;
 //    private static final ImageIcon BACKWARD_SELECTED;
 //    private static final ImageIcon BUCH_PDF;
 //    private static final ImageIcon BUCH_HTML;
 //    private static final ImageIcon BUCH_EIG_PDF;
 //    private static final ImageIcon BUCH_EIG_HTML;
     private final Map<CidsBean, Buchungsblatt> buchungsblaetter;
     private final Map<Object, ImageIcon> productPreviewImages;
     private final Map<String, CidsBean> gotoBeanMap;
     private final CardLayout cardLayout;
     private final MappingComponent map;
     private SOAPAccessProvider soapProvider;
     private ALKISInfoServices infoService;
     private LandParcel landparcel;
     private CidsBean cidsBean;
     private String title;
     private RetrieveBuchungsblaetterWorker retrieveBuchungsblaetterWorker;
     private boolean continueInBackground = false;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXBusyLabel blWait;
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnForward;
     private javax.swing.JEditorPane epInhaltBuchungsblatt;
     private javax.swing.JEditorPane epLage;
     private org.jdesktop.swingx.JXHyperlink hlFlurstuecksEigentumsnachweisKomHtml;
     private org.jdesktop.swingx.JXHyperlink hlFlurstuecksEigentumsnachweisKomPdf;
     private org.jdesktop.swingx.JXHyperlink hlFlurstuecksEigentumsnachweisNrwHtml;
     private org.jdesktop.swingx.JXHyperlink hlFlurstuecksEigentumsnachweisNrwPdf;
     private org.jdesktop.swingx.JXHyperlink hlFlurstuecksnachweisHtml;
     private org.jdesktop.swingx.JXHyperlink hlFlurstuecksnachweisPdf;
     private org.jdesktop.swingx.JXHyperlink hlKarte;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JLabel lblBack;
     private javax.swing.JLabel lblBuchungsblaetter;
     private javax.swing.JLabel lblDescGemarkung;
     private javax.swing.JLabel lblDescGemeinde;
     private javax.swing.JLabel lblDescGroesse;
     private javax.swing.JLabel lblDescLage;
     private javax.swing.JLabel lblDescLandparcelCode;
     private javax.swing.JLabel lblEnthalteneFlurstuecke;
     private javax.swing.JLabel lblForw;
     private javax.swing.JLabel lblGemarkung;
     private javax.swing.JLabel lblGemeinde;
     private javax.swing.JLabel lblGroesse;
     private javax.swing.JLabel lblInhalt;
     private javax.swing.JLabel lblLandparcelCode;
     private javax.swing.JLabel lblPreviewHead;
     private javax.swing.JLabel lblProductPreview;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JList lstBuchungsblaetter;
     private javax.swing.JList lstBuchungsblattFlurstuecke;
     private javax.swing.JPanel panBuchungEigentum;
     private javax.swing.JPanel panButtons;
     private javax.swing.JPanel panDescription;
     private javax.swing.JPanel panFlurstueckMap;
     private javax.swing.JPanel panFooter;
     private javax.swing.JPanel panFooterLeft;
     private javax.swing.JPanel panFooterRight;
     private javax.swing.JPanel panHtmlProducts;
     private javax.swing.JPanel panInhaltBuchungsblatt;
     private javax.swing.JPanel panMainInfo;
     private javax.swing.JPanel panPdfProducts;
     private javax.swing.JPanel panProductPreview;
     private javax.swing.JPanel panProducts;
     private javax.swing.JPanel panSpacing;
     private javax.swing.JPanel panTitle;
     private javax.swing.JScrollPane scpBuchungsblaetter;
     private javax.swing.JScrollPane scpBuchungsblattFlurstuecke;
     private javax.swing.JScrollPane scpInhaltBuchungsblatt;
     private javax.swing.JScrollPane scpLage;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel1;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel3;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel4;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel5;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 // static {
 // final ReflectionRenderer reflectionRenderer = new ReflectionRenderer(0.5f, 0.15f, false);
 // BACKWARD_SELECTED = new ImageIcon(Object.class.getResource(ICON_RES_PACKAGE + "arrow-left-sel.png"));
 // BACKWARD_PRESSED = new ImageIcon(Object.class.getResource(ICON_RES_PACKAGE + "arrow-left-pressed.png"));
 //
 // FORWARD_SELECTED = new ImageIcon(Object.class.getResource(ICON_RES_PACKAGE + "arrow-right-sel.png"));
 // FORWARD_PRESSED = new ImageIcon(Object.class.getResource(ICON_RES_PACKAGE + "arrow-right-pressed.png"));
 // BufferedImage i1 = null, i2 = null, i3 = null, i4 = null;
 // try {
 // i1 = reflectionRenderer.appendReflection(ImageIO.read(Object.class.getResource(ALKIS_RES_PACKAGE + "buchnachweispdf.png")));
 //            i2 = reflectionRenderer.appendReflection(ImageIO.read(Object.class.getResource(ALKIS_RES_PACKAGE + "buchnachweishtml.png")));
 //            i3 = reflectionRenderer.appendReflection(ImageIO.read(Object.class.getResource(ALKIS_RES_PACKAGE + "bucheignachweispdf.png")));
 //            i4 = reflectionRenderer.appendReflection(ImageIO.read(Object.class.getResource(ALKIS_RES_PACKAGE + "bucheignachweishtml.png")));
 //        } catch (Exception ex) {
 //            log.error(ex, ex);
 //        }
 //        BUCH_PDF = new ImageIcon(i1);
 //        BUCH_HTML = new ImageIcon(i2);
 //        BUCH_EIG_PDF = new ImageIcon(i3);
 //        BUCH_EIG_HTML = new ImageIcon(i4);
 //    }
     /**
      * Creates new form Alkis_pointRenderer.
      */
     public AlkisLandparcelRenderer() {
         buchungsblattPermission = ObjectRendererUtils.hasCurrentUserPermissionOnMetaClass(BUCHUNGSBLATT_TABLE, DOMAIN, ObjectRendererUtils.PermissionType.READ);
         buchungsblaetter = TypeSafeCollections.newConcurrentHashMap();
         productPreviewImages = TypeSafeCollections.newHashMap();
         gotoBeanMap = TypeSafeCollections.newHashMap();
         initIcons();
         initSoapServiceAccess();
         initComponents();
         initFooterElements();
         initProductPreview();
         scpInhaltBuchungsblatt.getViewport().setOpaque(false);
         scpLage.getViewport().setOpaque(false);
         blWait.setVisible(false);
         final LayoutManager layoutManager = getLayout();
         if (layoutManager instanceof CardLayout) {
             cardLayout = (CardLayout) layoutManager;
             cardLayout.show(this, CARD_1);
         } else {
             cardLayout = new CardLayout();
             log.error("Alkis_landparcelRenderer exspects CardLayout as major layout manager, but has " + getLayout()
                     + "!");
         }
         lblEnthalteneFlurstuecke.setVisible(false);
         scpBuchungsblattFlurstuecke.setVisible(false);
         lstBuchungsblaetter.setCellRenderer(new StyleListCellRenderer());
         lstBuchungsblattFlurstuecke.setCellRenderer(new StyleListCellRenderer());
         epInhaltBuchungsblatt.addHyperlinkListener(new HyperlinkListener() {
 
             @Override
             public void hyperlinkUpdate(final HyperlinkEvent e) {
                 if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                     final CidsBean blatt = gotoBeanMap.get(e.getDescription());
                     if (blatt != null) {
                         continueInBackground = true;
                         ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(blatt.getMetaObject(), "");
                     } else {
                         log.warn("Could not find buchungsblatt bean in gotoMap");
                     }
                 }
             }
         });
 
         epLage.addHyperlinkListener(new HyperlinkListener() {
 
             @Override
             public void hyperlinkUpdate(final HyperlinkEvent e) {
                 if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                     final CidsBean adresse = gotoBeanMap.get(e.getDescription());
                     if (adresse != null) {
                         continueInBackground = true;
                         ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(adresse.getMetaObject(), "");
                     } else {
                         log.warn("Could not find adress bean in gotoMap");
                     }
                 }
             }
         });
         map = new MappingComponent();
         panFlurstueckMap.add(map, BorderLayout.CENTER);
         initEditorPanes();
         if (!AlkisUtil.validateUserHasAlkisProductAccess()) {
             // disable Product page if user does not have the right to see it.
             btnForward.setEnabled(false);
             lblForw.setEnabled(false);
         }
         if(!buchungsblattPermission) {
             panBuchungEigentum.setVisible(false);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
     /**
      * DOCUMENT ME!
      */
     private void initIcons() {
         final ReflectionRenderer reflectionRenderer = new ReflectionRenderer(0.5f, 0.15f, false);
 //        BACKWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-sel.png"));
 //        BACKWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-pressed.png"));
 //
 //        FORWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-sel.png"));
 //        FORWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-pressed.png"));
         BufferedImage i1 = null;
         BufferedImage i2 = null;
         BufferedImage i3 = null;
         BufferedImage i4 = null;
         BufferedImage i5 = null;
         BufferedImage i6 = null;
         BufferedImage i7 = null;
         try {
             //TODO: Richtige Screenshots machen und zuordnen!
             i1 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "buchnachweispdf.png")));
             i2 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "buchnachweishtml.png")));
             i3 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bucheignachweispdf.png")));
             i4 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bucheignachweishtml.png")));
             i5 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "karte.png")));
             i6 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "buchnachweispdf.png")));
             i7 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "buchnachweishtml.png")));
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         BUCH_PDF = new ImageIcon(i1);
         BUCH_HTML = new ImageIcon(i2);
         BUCH_EIG_NRW_PDF = new ImageIcon(i3);
         BUCH_EIG_NRW_HTML = new ImageIcon(i4);
         KARTE_PDF = new ImageIcon(i5);
         BUCH_EIG_KOM_PDF = new ImageIcon(i6);
         BUCH_EIG_KOM_HTML = new ImageIcon(i7);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initProductPreview() {
         initProductPreviewImages();
         int maxX = 0;
         int maxY = 0;
         for (final ImageIcon ii : productPreviewImages.values()) {
             if (ii.getIconWidth() > maxX) {
                 maxX = ii.getIconWidth();
             }
             if (ii.getIconHeight() > maxY) {
                 maxY = ii.getIconHeight();
             }
         }
         final Dimension previewDim = new Dimension(maxX + 20, maxY + 40);
         ObjectRendererUtils.setAllDimensions(panProductPreview, previewDim);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initEditorPanes() {
         // Font and Layout
         final Font font = UIManager.getFont("Label.font");
         final String bodyRule = "body { font-family: " + font.getFamily() + "; "
                 + "font-size: " + font.getSize() + "pt; }";
         final String tableRule = "td { padding-right : 15px; }";
         final String tableHeadRule = "th { padding-right : 15px; }";
         final StyleSheet css = ((HTMLEditorKit) epInhaltBuchungsblatt.getEditorKit()).getStyleSheet();
         final StyleSheet css2 = ((HTMLEditorKit) epLage.getEditorKit()).getStyleSheet();
         css.addRule(bodyRule);
         css.addRule(tableRule);
         css.addRule(tableHeadRule);
         css2.addRule(bodyRule);
         // Change scroll behaviour: avoid autoscrolls on setText(...)
         final Caret caret = epInhaltBuchungsblatt.getCaret();
         if (caret instanceof DefaultCaret) {
             final DefaultCaret dCaret = (DefaultCaret) caret;
             dCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initProductPreviewImages() {
         productPreviewImages.put(hlFlurstuecksEigentumsnachweisNrwPdf, BUCH_EIG_NRW_PDF);
         productPreviewImages.put(hlFlurstuecksEigentumsnachweisKomPdf, BUCH_EIG_KOM_PDF);
         productPreviewImages.put(hlFlurstuecksEigentumsnachweisNrwHtml, BUCH_EIG_NRW_HTML);
         productPreviewImages.put(hlFlurstuecksEigentumsnachweisKomHtml, BUCH_EIG_KOM_HTML);
         productPreviewImages.put(hlFlurstuecksnachweisHtml, BUCH_HTML);
         productPreviewImages.put(hlFlurstuecksnachweisPdf, BUCH_PDF);
         productPreviewImages.put(hlKarte, KARTE_PDF);
         final ProductLabelMouseAdaper productListener = new ProductLabelMouseAdaper();
         hlFlurstuecksEigentumsnachweisNrwPdf.addMouseListener(productListener);
         hlFlurstuecksEigentumsnachweisKomPdf.addMouseListener(productListener);
         hlFlurstuecksEigentumsnachweisNrwHtml.addMouseListener(productListener);
         hlFlurstuecksEigentumsnachweisKomHtml.addMouseListener(productListener);
         hlFlurstuecksnachweisHtml.addMouseListener(productListener);
         hlFlurstuecksnachweisPdf.addMouseListener(productListener);
         hlKarte.addMouseListener(productListener);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblattBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private Buchungsblatt getBuchungsblatt(final CidsBean buchungsblattBean) throws Exception {
         Buchungsblatt buchungsblatt = null;
         if (buchungsblattBean != null) {
             buchungsblatt = buchungsblaetter.get(buchungsblattBean);
             if (buchungsblatt == null) {
                 final String buchungsblattcode = String.valueOf(buchungsblattBean.getProperty("buchungsblattcode"));
                 if ((buchungsblattcode != null) && (buchungsblattcode.length() > 5)) {
                     buchungsblatt = infoService.getBuchungsblatt(soapProvider.getIdentityCard(),
                             soapProvider.getService(),
                             AlkisBuchungsblattRenderer.fixBuchungslattCode(buchungsblattcode));
                     buchungsblaetter.put(buchungsblattBean, buchungsblatt);
                 }
             }
         }
         return buchungsblatt;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  waiting  DOCUMENT ME!
      */
     private void setWaiting(final boolean waiting) {
         blWait.setVisible(waiting);
         blWait.setBusy(waiting);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isWaiting() {
         return blWait.isBusy();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initFooterElements() {
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
                 lblForw,
                 btnForward,
                 ObjectRendererUtils.FORWARD_SELECTED,
                 ObjectRendererUtils.FORWARD_PRESSED);
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
                 lblBack,
                 btnBack,
                 ObjectRendererUtils.BACKWARD_SELECTED,
                 ObjectRendererUtils.BACKWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, FORWARD_SELECTED, FORWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, BACKWARD_SELECTED, BACKWARD_PRESSED);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initSoapServiceAccess() {
         try {
             soapProvider = new SOAPAccessProvider();
             infoService = soapProvider.getAlkisInfoService();
         } catch (Exception ex) {
             log.fatal(ex, ex);
         }
     }
 
     private void openEinzelnachweisProduct(String product, String actionTag) {
         if (ObjectRendererUtils.checkActionTag(actionTag)) {
             try {
                 final String parcelCode = AlkisUtil.getLandparcelCodeFromParcelBeanObject(cidsBean);
                 if (parcelCode.length() > 0) {
                     AlkisUtil.COMMONS.PRODUCTS.productEinzelNachweis(parcelCode, product);
                 }
             } catch (Exception ex) {
                 ObjectRendererUtils.showExceptionWindowToUser(
                         "Fehler beim Aufruf des Produkts: " + product,
                         ex,
                         AlkisLandparcelRenderer.this);
                 log.error(ex);
             }
         } else {
             showNoProductPermissionWarning();
         }
     }
 
     private void openKarteProduct() {
         try {
             final String parcelCode = AlkisUtil.getLandparcelCodeFromParcelBeanObject(cidsBean);
             if (parcelCode.length() > 0) {
                 AlkisUtil.COMMONS.PRODUCTS.productKarte(parcelCode);
             }
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser(
                     "Fehler beim Aufruf des Produkts: Kartenprodukt",
                     ex,
                     AlkisLandparcelRenderer.this);
             log.error(ex);
         }
     }
 
     private void showNoProductPermissionWarning() {
         JOptionPane.showMessageDialog(this, "Sie besitzen keine Berechtigung zur Erzeugung dieses Produkts!");
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
 
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         blWait = new org.jdesktop.swingx.JXBusyLabel();
         panFooter = new javax.swing.JPanel();
         panButtons = new javax.swing.JPanel();
         panFooterLeft = new javax.swing.JPanel();
         lblBack = new javax.swing.JLabel();
         btnBack = new javax.swing.JButton();
         panFooterRight = new javax.swing.JPanel();
         btnForward = new javax.swing.JButton();
         lblForw = new javax.swing.JLabel();
         panDescription = new javax.swing.JPanel();
         panBuchungEigentum = new RoundedPanel();
         scpBuchungsblaetter = new javax.swing.JScrollPane();
         lstBuchungsblaetter = new javax.swing.JList();
         scpBuchungsblattFlurstuecke = new javax.swing.JScrollPane();
         lstBuchungsblattFlurstuecke = new javax.swing.JList();
         lblBuchungsblaetter = new javax.swing.JLabel();
         lblInhalt = new javax.swing.JLabel();
         lblEnthalteneFlurstuecke = new javax.swing.JLabel();
         panInhaltBuchungsblatt = new javax.swing.JPanel();
         scpInhaltBuchungsblatt = new javax.swing.JScrollPane();
         epInhaltBuchungsblatt = new javax.swing.JEditorPane();
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel1 = new javax.swing.JLabel();
         panMainInfo = new RoundedPanel();
         lblLandparcelCode = new javax.swing.JLabel();
         lblDescLandparcelCode = new javax.swing.JLabel();
         lblDescGemeinde = new javax.swing.JLabel();
         lblGemeinde = new javax.swing.JLabel();
         lblDescGemarkung = new javax.swing.JLabel();
         lblGemarkung = new javax.swing.JLabel();
         lblDescLage = new javax.swing.JLabel();
         lblGroesse = new javax.swing.JLabel();
         lblDescGroesse = new javax.swing.JLabel();
         scpLage = new javax.swing.JScrollPane();
         epLage = new javax.swing.JEditorPane();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel6 = new javax.swing.JLabel();
         panFlurstueckMap = new javax.swing.JPanel();
         panProducts = new javax.swing.JPanel();
         panPdfProducts = new RoundedPanel();
         hlKarte = new org.jdesktop.swingx.JXHyperlink();
         hlFlurstuecksEigentumsnachweisNrwPdf = new org.jdesktop.swingx.JXHyperlink();
         hlFlurstuecksnachweisPdf = new org.jdesktop.swingx.JXHyperlink();
         jPanel1 = new javax.swing.JPanel();
         semiRoundedPanel4 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel4 = new javax.swing.JLabel();
         hlFlurstuecksEigentumsnachweisKomPdf = new org.jdesktop.swingx.JXHyperlink();
         panHtmlProducts = new RoundedPanel();
         hlFlurstuecksEigentumsnachweisKomHtml = new org.jdesktop.swingx.JXHyperlink();
         hlFlurstuecksnachweisHtml = new org.jdesktop.swingx.JXHyperlink();
         jPanel2 = new javax.swing.JPanel();
         semiRoundedPanel5 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel5 = new javax.swing.JLabel();
         hlFlurstuecksEigentumsnachweisNrwHtml = new org.jdesktop.swingx.JXHyperlink();
         panSpacing = new javax.swing.JPanel();
         panProductPreview = new RoundedPanel();
         lblProductPreview = new javax.swing.JLabel();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblPreviewHead = new javax.swing.JLabel();
 
         panTitle.setOpaque(false);
         panTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18));
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("TITLE");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panTitle.add(lblTitle, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 5);
         panTitle.add(blWait, gridBagConstraints);
 
         panFooter.setOpaque(false);
         panFooter.setLayout(new java.awt.BorderLayout());
 
         panButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
         panButtons.setOpaque(false);
         panButtons.setLayout(new java.awt.GridBagLayout());
 
         panFooterLeft.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setMinimumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setOpaque(false);
         panFooterLeft.setPreferredSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
 
         lblBack.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblBack.setForeground(new java.awt.Color(255, 255, 255));
         lblBack.setText("Info");
         lblBack.setEnabled(false);
         lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblBackMouseClicked(evt);
             }
         });
         panFooterLeft.add(lblBack);
 
         btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left.png"))); // NOI18N
         btnBack.setBorder(null);
         btnBack.setBorderPainted(false);
         btnBack.setContentAreaFilled(false);
         btnBack.setEnabled(false);
         btnBack.setFocusPainted(false);
         btnBack.setMaximumSize(new java.awt.Dimension(30, 30));
         btnBack.setMinimumSize(new java.awt.Dimension(30, 30));
         btnBack.setPreferredSize(new java.awt.Dimension(30, 30));
         btnBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-pressed.png"))); // NOI18N
         btnBack.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-sel.png"))); // NOI18N
         btnBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBackActionPerformed(evt);
             }
         });
         panFooterLeft.add(btnBack);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panButtons.add(panFooterLeft, gridBagConstraints);
 
         panFooterRight.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterRight.setOpaque(false);
         panFooterRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         btnForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right.png"))); // NOI18N
         btnForward.setBorder(null);
         btnForward.setBorderPainted(false);
         btnForward.setContentAreaFilled(false);
         btnForward.setFocusPainted(false);
         btnForward.setMaximumSize(new java.awt.Dimension(30, 30));
         btnForward.setMinimumSize(new java.awt.Dimension(30, 30));
         btnForward.setPreferredSize(new java.awt.Dimension(30, 30));
         btnForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnForwardActionPerformed(evt);
             }
         });
         panFooterRight.add(btnForward);
 
         lblForw.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblForw.setForeground(new java.awt.Color(255, 255, 255));
         lblForw.setText("Produkte");
         lblForw.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblForwMouseClicked(evt);
             }
         });
         panFooterRight.add(lblForw);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panButtons.add(panFooterRight, gridBagConstraints);
 
         panFooter.add(panButtons, java.awt.BorderLayout.CENTER);
 
         setLayout(new java.awt.CardLayout());
 
         panDescription.setOpaque(false);
         panDescription.setLayout(new java.awt.GridBagLayout());
 
         panBuchungEigentum.setLayout(new java.awt.GridBagLayout());
 
         scpBuchungsblaetter.setMaximumSize(new java.awt.Dimension(100, 200));
         scpBuchungsblaetter.setMinimumSize(new java.awt.Dimension(100, 200));
         scpBuchungsblaetter.setOpaque(false);
         scpBuchungsblaetter.setPreferredSize(new java.awt.Dimension(100, 200));
 
         lstBuchungsblaetter.setOpaque(false);
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cidsBean.buchungsblaetter}");
         org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, eLProperty, lstBuchungsblaetter);
         bindingGroup.addBinding(jListBinding);
 
         lstBuchungsblaetter.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lstBuchungsblaetterMouseClicked(evt);
             }
         });
         lstBuchungsblaetter.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstBuchungsblaetterValueChanged(evt);
             }
         });
         scpBuchungsblaetter.setViewportView(lstBuchungsblaetter);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
         panBuchungEigentum.add(scpBuchungsblaetter, gridBagConstraints);
 
         scpBuchungsblattFlurstuecke.setMaximumSize(new java.awt.Dimension(140, 200));
         scpBuchungsblattFlurstuecke.setMinimumSize(new java.awt.Dimension(140, 200));
         scpBuchungsblattFlurstuecke.setOpaque(false);
         scpBuchungsblattFlurstuecke.setPreferredSize(new java.awt.Dimension(140, 200));
 
         lstBuchungsblattFlurstuecke.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         lstBuchungsblattFlurstuecke.setOpaque(false);
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${selectedElement.landparcels}");
         jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, lstBuchungsblaetter, eLProperty, lstBuchungsblattFlurstuecke);
         bindingGroup.addBinding(jListBinding);
 
         scpBuchungsblattFlurstuecke.setViewportView(lstBuchungsblattFlurstuecke);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
         panBuchungEigentum.add(scpBuchungsblattFlurstuecke, gridBagConstraints);
 
         lblBuchungsblaetter.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblBuchungsblaetter.setText("Buchungsblätter:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         panBuchungEigentum.add(lblBuchungsblaetter, gridBagConstraints);
 
         lblInhalt.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblInhalt.setText("Inhalt:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         panBuchungEigentum.add(lblInhalt, gridBagConstraints);
 
         lblEnthalteneFlurstuecke.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblEnthalteneFlurstuecke.setText("Enthaltene Flurstücke:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         panBuchungEigentum.add(lblEnthalteneFlurstuecke, gridBagConstraints);
 
         panInhaltBuchungsblatt.setOpaque(false);
         panInhaltBuchungsblatt.setLayout(new java.awt.BorderLayout());
 
         scpInhaltBuchungsblatt.setBorder(null);
         scpInhaltBuchungsblatt.setMaximumSize(new java.awt.Dimension(250, 200));
         scpInhaltBuchungsblatt.setMinimumSize(new java.awt.Dimension(250, 200));
         scpInhaltBuchungsblatt.setOpaque(false);
         scpInhaltBuchungsblatt.setPreferredSize(new java.awt.Dimension(250, 200));
 
         epInhaltBuchungsblatt.setBorder(null);
         epInhaltBuchungsblatt.setContentType("text/html");
         epInhaltBuchungsblatt.setEditable(false);
         epInhaltBuchungsblatt.setText("\n");
         epInhaltBuchungsblatt.setMaximumSize(new java.awt.Dimension(250, 200));
         epInhaltBuchungsblatt.setMinimumSize(new java.awt.Dimension(250, 200));
         epInhaltBuchungsblatt.setOpaque(false);
         epInhaltBuchungsblatt.setPreferredSize(new java.awt.Dimension(250, 200));
         scpInhaltBuchungsblatt.setViewportView(epInhaltBuchungsblatt);
 
         panInhaltBuchungsblatt.add(scpInhaltBuchungsblatt, java.awt.BorderLayout.CENTER);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 15);
         panBuchungEigentum.add(panInhaltBuchungsblatt, gridBagConstraints);
 
         semiRoundedPanel1.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel1.setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setForeground(new java.awt.Color(255, 255, 255));
         jLabel1.setText("Buchungsblätter");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel1.add(jLabel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panBuchungEigentum.add(semiRoundedPanel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         panDescription.add(panBuchungEigentum, gridBagConstraints);
 
         panMainInfo.setLayout(new java.awt.GridBagLayout());
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.alkis_id}"), lblLandparcelCode, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
         panMainInfo.add(lblLandparcelCode, gridBagConstraints);
 
         lblDescLandparcelCode.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescLandparcelCode.setText("Flurstückskennzeichen:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panMainInfo.add(lblDescLandparcelCode, gridBagConstraints);
 
         lblDescGemeinde.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescGemeinde.setText("Gemeinde:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescGemeinde, gridBagConstraints);
 
         lblGemeinde.setText("Wuppertal");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         panMainInfo.add(lblGemeinde, gridBagConstraints);
 
         lblDescGemarkung.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescGemarkung.setText("Gemarkung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescGemarkung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.gemarkung}"), lblGemarkung, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         panMainInfo.add(lblGemarkung, gridBagConstraints);
 
         lblDescLage.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescLage.setText("Lage:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescLage, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.groesse} m²"), lblGroesse, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         panMainInfo.add(lblGroesse, gridBagConstraints);
 
         lblDescGroesse.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescGroesse.setText("Größe:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescGroesse, gridBagConstraints);
 
         scpLage.setBorder(null);
         scpLage.setMaximumSize(new java.awt.Dimension(250, 20));
         scpLage.setMinimumSize(new java.awt.Dimension(250, 20));
         scpLage.setOpaque(false);
         scpLage.setPreferredSize(new java.awt.Dimension(250, 20));
 
         epLage.setBorder(null);
         epLage.setContentType("text/html");
         epLage.setEditable(false);
         epLage.setOpaque(false);
         scpLage.setViewportView(epLage);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMainInfo.add(scpLage, gridBagConstraints);
 
         semiRoundedPanel2.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel2.setLayout(new java.awt.GridBagLayout());
 
         jLabel6.setText("Flurstücksinformation");
         jLabel6.setForeground(new java.awt.Color(255, 255, 255));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel2.add(jLabel6, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panMainInfo.add(semiRoundedPanel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         panDescription.add(panMainInfo, gridBagConstraints);
 
         panFlurstueckMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panFlurstueckMap.setLayout(new java.awt.BorderLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         panDescription.add(panFlurstueckMap, gridBagConstraints);
 
         add(panDescription, "CARD_1");
 
         panProducts.setOpaque(false);
         panProducts.setLayout(new java.awt.GridBagLayout());
 
         panPdfProducts.setOpaque(false);
         panPdfProducts.setLayout(new java.awt.GridBagLayout());
 
         hlKarte.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlKarte.setText("Karte");
         hlKarte.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlKarteActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 10, 7);
         panPdfProducts.add(hlKarte, gridBagConstraints);
 
         hlFlurstuecksEigentumsnachweisNrwPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlFlurstuecksEigentumsnachweisNrwPdf.setText("Flurstücks- und Eigentumsnachweis (NRW)");
         hlFlurstuecksEigentumsnachweisNrwPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlFlurstuecksEigentumsnachweisNrwPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPdfProducts.add(hlFlurstuecksEigentumsnachweisNrwPdf, gridBagConstraints);
 
         hlFlurstuecksnachweisPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlFlurstuecksnachweisPdf.setText("Flurstücksnachweis");
         hlFlurstuecksnachweisPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlFlurstuecksnachweisPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(17, 7, 7, 7);
         panPdfProducts.add(hlFlurstuecksnachweisPdf, gridBagConstraints);
 
         jPanel1.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         panPdfProducts.add(jPanel1, gridBagConstraints);
 
         semiRoundedPanel4.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel4.setLayout(new java.awt.GridBagLayout());
 
         jLabel4.setForeground(new java.awt.Color(255, 255, 255));
         jLabel4.setText("PDF-Produkte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel4.add(jLabel4, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panPdfProducts.add(semiRoundedPanel4, gridBagConstraints);
 
         hlFlurstuecksEigentumsnachweisKomPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlFlurstuecksEigentumsnachweisKomPdf.setText("Flurstücks- und Eigentumsnachweis (kommunal)");
         hlFlurstuecksEigentumsnachweisKomPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlFlurstuecksEigentumsnachweisKomPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPdfProducts.add(hlFlurstuecksEigentumsnachweisKomPdf, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 9, 5);
         panProducts.add(panPdfProducts, gridBagConstraints);
 
         panHtmlProducts.setOpaque(false);
         panHtmlProducts.setLayout(new java.awt.GridBagLayout());
 
         hlFlurstuecksEigentumsnachweisKomHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlFlurstuecksEigentumsnachweisKomHtml.setText("Flurstücks- und Eigentumsnachweis (kommunal)");
         hlFlurstuecksEigentumsnachweisKomHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlFlurstuecksEigentumsnachweisKomHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 10, 7);
         panHtmlProducts.add(hlFlurstuecksEigentumsnachweisKomHtml, gridBagConstraints);
 
         hlFlurstuecksnachweisHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlFlurstuecksnachweisHtml.setText("Flurstücksnachweis");
         hlFlurstuecksnachweisHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlFlurstuecksnachweisHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(17, 7, 7, 7);
         panHtmlProducts.add(hlFlurstuecksnachweisHtml, gridBagConstraints);
 
         jPanel2.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         panHtmlProducts.add(jPanel2, gridBagConstraints);
 
         semiRoundedPanel5.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel5.setLayout(new java.awt.GridBagLayout());
 
         jLabel5.setForeground(new java.awt.Color(255, 255, 255));
         jLabel5.setText("HTML-Produkte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel5.add(jLabel5, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panHtmlProducts.add(semiRoundedPanel5, gridBagConstraints);
 
         hlFlurstuecksEigentumsnachweisNrwHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlFlurstuecksEigentumsnachweisNrwHtml.setText("Flurstücks- und Eigentumsnachweis (NRW)");
         hlFlurstuecksEigentumsnachweisNrwHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlFlurstuecksEigentumsnachweisNrwHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 10, 7);
         panHtmlProducts.add(hlFlurstuecksEigentumsnachweisNrwHtml, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(9, 5, 5, 5);
         panProducts.add(panHtmlProducts, gridBagConstraints);
 
         panSpacing.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.weighty = 1.0;
         panProducts.add(panSpacing, gridBagConstraints);
 
         panProductPreview.setOpaque(false);
         panProductPreview.setLayout(new java.awt.BorderLayout());
 
         lblProductPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblProductPreview.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
         panProductPreview.add(lblProductPreview, java.awt.BorderLayout.CENTER);
 
         semiRoundedPanel3.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel3.setLayout(new java.awt.GridBagLayout());
 
         lblPreviewHead.setText("Vorschau");
         lblPreviewHead.setForeground(new java.awt.Color(255, 255, 255));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel3.add(lblPreviewHead, gridBagConstraints);
 
         panProductPreview.add(semiRoundedPanel3, java.awt.BorderLayout.NORTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panProducts.add(panProductPreview, gridBagConstraints);
 
         add(panProducts, "CARD_2");
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlKarteActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlKarteActionPerformed
         if (ObjectRendererUtils.checkActionTag(PRODUCT_ACTION_TAG_KARTE)) {
             openKarteProduct();
         } else {
             showNoProductPermissionWarning();
         }
     }//GEN-LAST:event_hlKarteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlFlurstuecksnachweisPdfActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlFlurstuecksnachweisPdfActionPerformed
         openEinzelnachweisProduct(AlkisUtil.COMMONS.PRODUCTS.FLURSTUECKSNACHWEIS_PDF, PRODUCT_ACTION_TAG_FLURSTUECKSNACHWEIS);
     }//GEN-LAST:event_hlFlurstuecksnachweisPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlFlurstuecksEigentumsnachweisKomHtmlActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlFlurstuecksEigentumsnachweisKomHtmlActionPerformed
         openEinzelnachweisProduct(AlkisUtil.COMMONS.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_KOMMUNAL_HTML, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM);
     }//GEN-LAST:event_hlFlurstuecksEigentumsnachweisKomHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlFlurstuecksnachweisHtmlActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlFlurstuecksnachweisHtmlActionPerformed
         openEinzelnachweisProduct(AlkisUtil.COMMONS.PRODUCTS.FLURSTUECKSNACHWEIS_HTML, PRODUCT_ACTION_TAG_FLURSTUECKSNACHWEIS);
     }//GEN-LAST:event_hlFlurstuecksnachweisHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnBackActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, CARD_1);
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
     }//GEN-LAST:event_btnBackActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnForwardActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, CARD_2);
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
     }//GEN-LAST:event_btnForwardActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblBackMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
     }//GEN-LAST:event_lblBackMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblForwMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
     }//GEN-LAST:event_lblForwMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstBuchungsblaetterValueChanged(final javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstBuchungsblaetterValueChanged
         if (buchungsblattPermission && !evt.getValueIsAdjusting()) {
             final Object[] selectedObjs = lstBuchungsblaetter.getSelectedValues();
             if ((selectedObjs != null) && (selectedObjs.length > 0)) {
                 final Collection<CidsBean> selectedBeans = TypeSafeCollections.newArrayList(selectedObjs.length);
                 for (final Object selectedObj : selectedObjs) {
                     if (selectedObj instanceof CidsBean) {
                         selectedBeans.add((CidsBean) selectedObj);
                     }
                 }
 
                 final RetrieveBuchungsblaetterWorker oldWorker = retrieveBuchungsblaetterWorker;
                 if (oldWorker != null) {
                     AlkisSOAPWorkerService.cancel(oldWorker);
                 }
                 retrieveBuchungsblaetterWorker = new RetrieveBuchungsblaetterWorker(selectedBeans);
                 AlkisSOAPWorkerService.execute(retrieveBuchungsblaetterWorker);
             }
         }
     }//GEN-LAST:event_lstBuchungsblaetterValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlFlurstuecksEigentumsnachweisNrwPdfActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlFlurstuecksEigentumsnachweisNrwPdfActionPerformed
         openEinzelnachweisProduct(AlkisUtil.COMMONS.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_NRW_PDF, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_NRW);
     }//GEN-LAST:event_hlFlurstuecksEigentumsnachweisNrwPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstBuchungsblaetterMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstBuchungsblaetterMouseClicked
         if (evt.getClickCount() > 1) {
             final Object selObject = lstBuchungsblaetter.getSelectedValue();
             if (selObject instanceof CidsBean) {
                 final CidsBean selBean = (CidsBean) selObject;
                 ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(selBean.getMetaObject(), "");
             }
         }
     }//GEN-LAST:event_lstBuchungsblaetterMouseClicked
 
     private void hlFlurstuecksEigentumsnachweisKomPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlFlurstuecksEigentumsnachweisKomPdfActionPerformed
         openEinzelnachweisProduct(AlkisUtil.COMMONS.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_KOMMUNAL_PDF, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM);
     }//GEN-LAST:event_hlFlurstuecksEigentumsnachweisKomPdfActionPerformed
 
     private void hlFlurstuecksEigentumsnachweisNrwHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlFlurstuecksEigentumsnachweisNrwHtmlActionPerformed
         openEinzelnachweisProduct(AlkisUtil.COMMONS.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_NRW_HTML, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_NRW);
     }//GEN-LAST:event_hlFlurstuecksEigentumsnachweisNrwHtmlActionPerformed
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     @Override
     public void setCidsBean(final CidsBean cb) {
         bindingGroup.unbind();
         if (cb != null) {
             this.cidsBean = cb;
             initMap();
             initLage();
             initGotoBeanMap();
             bindingGroup.bind();
             final int anzahlBuchungsblaetter = lstBuchungsblaetter.getModel().getSize();
             if (anzahlBuchungsblaetter < 5) {
                 lblBuchungsblaetter.setVisible(false);
                 scpBuchungsblaetter.setVisible(false);
                 lblInhalt.setVisible(false);
                 final int[] selection = new int[anzahlBuchungsblaetter];
                 for (int i = 0; i < selection.length; ++i) {
                     selection[i] = i;
                 }
                 lstBuchungsblaetter.setSelectedIndices(selection);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initGotoBeanMap() {
         final Object buchungsblaetterCollectionObj = cidsBean.getProperty("buchungsblaetter");
         if (buchungsblaetterCollectionObj instanceof List) {
             final List<CidsBean> blaetterList = (List<CidsBean>) buchungsblaetterCollectionObj;
             for (final CidsBean blatt : blaetterList) {
                 gotoBeanMap.put(blatt.getMetaObject().getMetaClass().getID() + AlkisUtil.LINK_SEPARATOR_TOKEN
                         + blatt.getMetaObject().getID(),
                         blatt);
             }
         } else {
             log.error("Fehler bei initGotoMap. buchungsbaetter = " + buchungsblaetterCollectionObj);
         }
         final Object adressenCollectionObj = cidsBean.getProperty("adressen");
         if (adressenCollectionObj instanceof List) {
             final List<CidsBean> adressenList = (List<CidsBean>) adressenCollectionObj;
             for (final CidsBean adresse : adressenList) {
                 gotoBeanMap.put(adresse.getMetaObject().getMetaClass().getID() + AlkisUtil.LINK_SEPARATOR_TOKEN
                         + adresse.getMetaObject().getID(),
                         adresse);
             }
         } else {
             log.error("Fehler bei initGotoMap. adressen = " + buchungsblaetterCollectionObj);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initLage() {
         final Map<String, List<CidsBean>> streetToBeans = TypeSafeCollections.newHashMap();
         final Object adressenObj = cidsBean.getProperty("adressen");
         if (adressenObj instanceof List) {
             final List<CidsBean> adressenBeans = (List<CidsBean>) adressenObj;
             for (final CidsBean adresse : adressenBeans) {
                 final Object strasseObj = adresse.getProperty("strasse");
                 List<CidsBean> beansWithThisStreet;
                 if (strasseObj != null) {
                     final String strasse = strasseObj.toString();
                     beansWithThisStreet = streetToBeans.get(strasse);
                     if (beansWithThisStreet == null) {
                         beansWithThisStreet = TypeSafeCollections.newArrayList();
                         streetToBeans.put(strasse, beansWithThisStreet);
                     }
                     beansWithThisStreet.add(adresse);
                 }
             }
         }
         final StringBuilder adressenContent = new StringBuilder(
                 "<html><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" valign=\"top\">");
         // sort by street
         final List<String> sortStrassen = TypeSafeCollections.newArrayList(streetToBeans.keySet());
         Collections.sort(sortStrassen);
         int entryCount = sortStrassen.size();
         for (final String strasse : sortStrassen) {
             final List<CidsBean> beansWithThisStreet = streetToBeans.get(strasse);
             Collections.sort(beansWithThisStreet, new Comparator<CidsBean>() {
 
                 @Override
                 public int compare(final CidsBean o1, final CidsBean o2) {
                     if ((o1 != null) && (o2 != null)) {
                         final Object n1 = o1.getProperty("nummer");
                         final Object n2 = o2.getProperty("nummer");
                         return AlphanumComparator.getInstance().compare(String.valueOf(n1), String.valueOf(n2));
                     }
                     return 0;
                 }
             });
 
             final Map<String, CidsBean> hausnummernToBeans = TypeSafeCollections.newLinkedHashMap();
             for (final CidsBean adresse : beansWithThisStreet) {
                 final Object hausnummerObj = adresse.getProperty("nummer");
                 if (hausnummerObj != null) {
                     hausnummernToBeans.put(hausnummerObj.toString(), adresse);
                 }
             }
             if (hausnummernToBeans.isEmpty()) {
                 for (final CidsBean bean : beansWithThisStreet) {
                     adressenContent.append("<tr><td>");
                     adressenContent.append(AlkisUtil.generateLinkFromCidsBean(bean, strasse));
                     adressenContent.append("</td></tr>");
                 }
             } else {
                 // allocate an extra line if number of housenumbers is big
                 entryCount += (hausnummernToBeans.size() / 7);
                 adressenContent.append("<tr><td>");
                 adressenContent.append(strasse).append("&nbsp;");
                 adressenContent.append("</td>");
                 adressenContent.append("<td>");
                 for (final Entry<String, CidsBean> entry : hausnummernToBeans.entrySet()) {
                     final String nummer = entry.getKey();
                     final CidsBean numberBean = entry.getValue();
                     adressenContent.append(AlkisUtil.generateLinkFromCidsBean(numberBean, nummer));
                     adressenContent.append(", ");
                 }
                 adressenContent.delete(adressenContent.length() - 2, adressenContent.length());
                 adressenContent.append("</td>");
                 adressenContent.append("</tr>");
             }
         }
         adressenContent.append("</table></html>");
         epLage.setText(adressenContent.toString());
         final int linecount = entryCount;
         if (linecount > 1) {
             if (linecount < 5) {
                 ObjectRendererUtils.setAllDimensions(
                         scpLage,
                         new Dimension(scpLage.getPreferredSize().width, 20 * linecount));
             } else {
                 ObjectRendererUtils.setAllDimensions(scpLage, new Dimension(scpLage.getPreferredSize().width, 100));
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         final Object geoObj = cidsBean.getProperty("geometrie.geo_field");
         if (geoObj instanceof Geometry) {
             final Geometry pureGeom = (Geometry) geoObj;
             final BoundingBox box = new BoundingBox(pureGeom.getEnvelope().buffer(AlkisUtil.COMMONS.GEO_BUFFER));
 
             final Runnable mapRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     final ActiveLayerModel mappingModel = new ActiveLayerModel();
                     mappingModel.setSrs(AlkisUtil.COMMONS.SRS_GEOM);
                     mappingModel.addHome(new XBoundingBox(
                             box.getX1(),
                             box.getY1(),
                             box.getX2(),
                             box.getY2(),
                             AlkisUtil.COMMONS.SRS_GEOM,
                             true));
                     final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisUtil.COMMONS.MAP_CALL_STRING));
                     swms.setName("Flurstueck");
                     final StyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(pureGeom);
                     dsf.setFillingPaint(new Color(1, 0, 0, 0.5f));
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
                     map.addCustomInputListener("MUTE", new PBasicInputEventHandler() {
 
                         @Override
                         public void mouseClicked(final PInputEvent evt) {
                             if (evt.getClickCount() > 1) {
                                 final CidsBean bean = cidsBean;
                                 ObjectRendererUtils.switchToCismapMap();
                                 ObjectRendererUtils.addBeanGeomAsFeatureToCismapMap(bean, false);
                             }
                         }
                     });
                     map.setInteractionMode("MUTE");
                     map.getFeatureCollection().addFeature(dsf);
                     map.setAnimationDuration(duration);
                 }
             };
             if (EventQueue.isDispatchThread()) {
                 mapRunnable.run();
             } else {
                 EventQueue.invokeLater(mapRunnable);
             }
         }
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void setTitle(String title) {
         if (title == null) {
             title = "<Error>";
         } else {
             title = AlkisUtil.prettyPrintLandparcelCode(title);
         }
         this.title = title;
         lblTitle.setText(this.title);
     }
 
     //~ Inner Classes ----------------------------------------------------------
     /**
      * <editor-fold defaultstate="collapsed" desc="Retrieve Worker">.
      *
      * @version  $Revision$, $Date$
      */
     final class RetrieveBuchungsblaetterWorker extends SwingWorker<String, String> {
 
         //~ Static fields/initializers -----------------------------------------
         private static final String LOAD_TEXT = "Weitere werden geladen...";
         //~ Instance fields ----------------------------------------------------
         private final Collection<CidsBean> buchungsblaetterBeans;
         private final StringBuilder currentInfoText;
         private int current;
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new RetrieveBuchungsblaetterWorker object.
          *
          * @param  buchungsblatterBeans  DOCUMENT ME!
          */
         public RetrieveBuchungsblaetterWorker(final Collection<CidsBean> buchungsblatterBeans) {
             this.buchungsblaetterBeans = buchungsblatterBeans;
             this.currentInfoText = new StringBuilder();
             setWaiting(true);
             epInhaltBuchungsblatt.setText("Wird geladen... (" + buchungsblatterBeans.size() + ")");
             current = 1;
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         protected String doInBackground() throws Exception {
             for (final CidsBean buchungsblattBean : buchungsblaetterBeans) {
                 if (buchungsblattBean != null) {
                     final Buchungsblatt buchungsblatt = getBuchungsblatt(buchungsblattBean);
                     if (buchungsblatt.getBuchungsstellen() != null) {
                         for (final Buchungsstelle stelle : buchungsblatt.getBuchungsstellen()) {
                             stelle.getFraction();
                         }
                     }
                     currentInfoText.append(AlkisUtil.buchungsblattToString(buchungsblatt, buchungsblattBean));
                     if (isCancelled()) {
                         return currentInfoText.toString();
                     }
                     publish(currentInfoText.toString());
                 }
             }
             return currentInfoText.toString();
         }
 
         @Override
         protected void process(final List<String> chunks) {
             if (!isCancelled()) {
                 final StringBuilder infos = new StringBuilder(chunks.get(chunks.size() - 1));
                 infos.append(LOAD_TEXT).append(" (").append((current += chunks.size())).append(" / ").append(buchungsblaetterBeans.size()).append(")");
                 epInhaltBuchungsblatt.setText("<table>" + infos.toString() + "</table>");
 //                epInhaltBuchungsblatt.setText("<font face=\"" + FONT + "\" size=\"11\">" + "<table>" + infos.toString() + "</table>" + "</font>");
 //                epInhaltBuchungsblatt.setText("<pre>" + infos.toString() + "</pre>");
             }
         }
 
         @Override
         public String toString() {
             return super.toString() + " " + buchungsblaetterBeans;
         }
 
         @Override
         protected void done() {
             if (!isCancelled()) {
                 try {
                     setWaiting(false);
                     epInhaltBuchungsblatt.setText(get());
 //                    epInhaltBuchungsblatt.setText("<pre>" + get() + "</pre>");
                 } catch (InterruptedException ex) {
                     if (log.isDebugEnabled()) {
                         log.debug(ex, ex);
                     }
                 } catch (Exception ex) {
                     epInhaltBuchungsblatt.setText("Fehler beim Empfangen.");
                     ObjectRendererUtils.showExceptionWindowToUser(
                             "Fehler beim Empfangen",
                             ex,
                             AlkisLandparcelRenderer.this);
                     log.error(ex, ex);
                 }
             }
         }
     }
 
 // </editor-fold>
     /**
      * <editor-fold defaultstate="collapsed" desc="Listeners">.
      *
      * @version  $Revision$, $Date$
      */
     class ProductLabelMouseAdaper extends MouseAdapter {
 
         //~ Methods ------------------------------------------------------------
         @Override
         public void mouseEntered(final MouseEvent e) {
             final Object srcObj = e.getSource();
             final ImageIcon imageIcon = productPreviewImages.get(srcObj);
             if (imageIcon != null) {
                 lblProductPreview.setIcon(imageIcon);
             }
         }
 
         @Override
         public void mouseExited(final MouseEvent e) {
             lblProductPreview.setIcon(null);
         }
     }
 // </editor-fold>
     /**
      * cancel worker if renderer is disposed.
      */
 // @Override
 // public void removeNotify() {
 // super.removeNotify();
 // if (!continueInBackground) {
 // AlkisSOAPWorkerService.cancel(retrieveBuchungsblaetterWorker);
 // setWaiting(false);
 // }
 // }
 }
