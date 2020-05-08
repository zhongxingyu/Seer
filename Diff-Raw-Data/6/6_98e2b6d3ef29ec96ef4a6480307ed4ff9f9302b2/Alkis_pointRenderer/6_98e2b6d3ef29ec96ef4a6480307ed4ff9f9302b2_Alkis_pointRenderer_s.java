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
 
 import de.aedsicad.aaaweb.service.alkis.info.ALKISInfoServices;
 import de.aedsicad.aaaweb.service.util.Point;
 import de.aedsicad.aaaweb.service.util.PointLocation;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisCommons;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisSOAPWorkerService;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.SOAPAccessProvider;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 import de.cismet.tools.collections.TypeSafeCollections;
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.TitleComponentProvider;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingWorker;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.jdesktop.swingx.graphics.ReflectionRenderer;
 
 /**
  *
  * @author srichter
  */
 public class Alkis_pointRenderer extends javax.swing.JPanel implements CidsBeanRenderer, TitleComponentProvider, FooterComponentProvider, BorderProvider {
 
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     private static final String ALKIS_RES_PACKAGE = ICON_RES_PACKAGE + "alkis/";
     private static final Pattern ERHEBUNG_FILTER_PATTERN = Pattern.compile("<LI_Source><description>(.*)</description></LI_Source>");
     private static final String CARD_1 = "CARD_1";
     private static final String CARD_2 = "CARD_2";
     private static final String ERHEBUNGS_PROPERTIES = "datenerhebung.properties";
 //    private ImageIcon FORWARD_PRESSED;
 //    private ImageIcon FORWARD_SELECTED;
 //    private ImageIcon BACKWARD_PRESSED;
 //    private ImageIcon BACKWARD_SELECTED;
     private static final Color PUNKTORT_MIT_KARTENDARSTELLUNG = new Color(120, 255, 190);
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alkis_pointRenderer.class);
     private final Map<Object, ImageIcon> productPreviewImages;
     private final List<JLabel> retrieveableLabels;
     private SOAPAccessProvider soapProvider;
     private ALKISInfoServices infoService;
     private Point point;
     private CidsBean cidsBean;
     private String title;
     private final CardLayout cardLayout;
 //    private BindingGroup punktOrtBindingGroup;
     private List<PointLocation> pointLocations;
     //should be static!
     private ImageIcon PUNKT_PDF;
     private ImageIcon PUNKT_HTML;
     private ImageIcon PUNKT_TXT;
     private static final Converter<String, String> ALKIS_BOOLEAN_CONVERTER = new Converter<String, String>() {
 
         private static final String TRUE_REP = "Ja";
         private static final String FALSE_REP = "Nein";
 
         @Override
         public String convertForward(String s) {
             if (s != null && s.equals("1")) {
                 return TRUE_REP;
             } else {
                 return FALSE_REP;
             }
         }
 
         @Override
         public String convertReverse(String t) {
             if (TRUE_REP.equals(t)) {
                 return "1";
             } else {
                 return "0";
             }
         }
     };
     private static final Converter<String, String> ALKIS_ERHEBUNG_CONVERTER = new Converter<String, String>() {
         //anonymous constructor
 
         {
             this.datenerhebungWerte = new Properties();
             try {
                 this.datenerhebungWerte.load(getClass().getResource(ALKIS_RES_PACKAGE + ERHEBUNGS_PROPERTIES).openStream());
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
         private final Properties datenerhebungWerte;
 
         @Override
         public String convertForward(String s) {
             if (s != null) {
                 final Matcher matcher = ERHEBUNG_FILTER_PATTERN.matcher(s);
                 if (matcher.find()) {
                     //take the 4 digit code
                     final String matcherResultG1 = matcher.group(1);
                     if (matcherResultG1 != null && matcherResultG1.length() > 3) {
                         final String searchKey = matcherResultG1.substring(0, 4);
                         //lookup description for code
                         final String descr = datenerhebungWerte.getProperty(searchKey);
                         if (descr != null) {
                             //return result + format with html (max. column length)
                             return "<html><table width=\"300\" border=\"0\"><tr><td>(" + searchKey + ") " + descr + "</tr></table></html>";
                         } else {
                             log.warn("No description found for Erhebung with key: " + searchKey);
                         }
                     }
                 }
                 log.warn("Could not translate response: " + s);
             }
             return "keine Angabe";
         }
 
         @Override
         public String convertReverse(String t) {
             throw new UnsupportedOperationException("Will not be supported!");
         }
     };
     private static final Converter<CidsBean, String> ALKIS_VERMARKUNG_CONVERTER = new Converter<CidsBean, String>() {
 
         @Override
         public String convertForward(CidsBean bean) {
             Object markenObj = bean.getProperty("abmarkung");
             if (markenObj == null) {
                 markenObj = bean.getProperty("vermarkung");
             }
             return markenObj == null ? null : markenObj.toString();
         }
 
         @Override
         public CidsBean convertReverse(String vermarkungText) {
             throw new UnsupportedOperationException("Will/Can not be supported!");
         }
     };
     private static final Comparator<PointLocation> POINTLOCATION_COMPARATOR = new Comparator<PointLocation>() {
 
         @Override
         public int compare(PointLocation p1, PointLocation p2) {
             int result = compareKartendarstellung(p1, p2);
             if (result != 0) {
                 //descending order
                 return -result;
             } else {
                 //descending order
                 return -compareDate(p1, p2);
             }
 
         }
 
         private int compareKartendarstellung(PointLocation p1, PointLocation p2) {
             String kd1 = p1.getKartendarstellung();
             String kd2 = p2.getKartendarstellung();
             if (kd1 != kd2) {
                 if (kd1 != null) {
                     if (kd2 != null) {
                         return kd1.compareTo(kd2);
                     } else {
                         return 1;
                     }
                 } else {
                     return -1;
                 }
             } else {
                 return 0;
             }
         }
 
         private int compareDate(PointLocation p1, PointLocation p2) {
             String lz1 = p1.getLebenszeitIntervallBeginnt();
             String lz2 = p2.getLebenszeitIntervallBeginnt();
             if (lz1 != lz2) {
                 if (lz1 != null) {
                     if (lz2 != null) {
                         if (lz1.length() > 9 && lz2.length() > 9) {
                             //10 = length of YYYY-MM-DD
                             return compareDateStrings(lz1.substring(0, 11), lz2.substring(0, 11));
                         } else {
                             throw new IllegalStateException("Could not parse Dates: " + lz1 + " or " + lz2);
                         }
                     } else {
                         return 1;
                     }
                 } else {
                     return -1;
                 }
             } else {
                 return 0;
             }
         }
 
         private int compareDateStrings(String ds1, String ds2) {
             String[] ymd1 = ds1.split("-");
             String[] ymd2 = ds2.split("-");
             if (ymd1.length == 3 && ymd2.length == 3) {
                 int result = 0;
                 for (int i = 0; i < 3 && result == 0; ++i) {
                     result = ymd1[i].compareTo(ymd2[i]);
                 }
                 return result;
             } else {
                 throw new IllegalStateException("Could not parse Dates: " + ds1 + " or " + ds2);
             }
         }
     };
 
     /** Creates new form Alkis_pointRenderer */
     public Alkis_pointRenderer() {
         retrieveableLabels = TypeSafeCollections.newArrayList();
         productPreviewImages = TypeSafeCollections.newHashMap();
         try {
             soapProvider = new SOAPAccessProvider();
 //            searchService = soapProvider.getAlkisSearchService();
             infoService = soapProvider.getAlkisInfoService();
         } catch (Exception ex) {
             log.fatal(ex, ex);
         }
         initIcons();
         initComponents();
         initFooterElements();
         initProductPreview();
         setWait(false);
         panLocationInfos.setVisible(false);
         cbPunktorte.setRenderer(new LocationComboBoxRenderer());
         final LayoutManager layoutManager = getLayout();
         if (layoutManager instanceof CardLayout) {
             cardLayout = (CardLayout) layoutManager;
             cardLayout.show(this, CARD_1);
         } else {
             cardLayout = new CardLayout();
             log.error("Alkis_landparcelRenderer exspects CardLayout as major layout manager, but has " + getLayout() + "!");
         }
         retrieveableLabels.add(lblTxtBeginn);
         retrieveableLabels.add(lblTxtEnde);
         retrieveableLabels.add(lblTxtModellart);
         retrieveableLabels.add(lblTxtDienststelle);
         retrieveableLabels.add(lblTxtLand);
         retrieveableLabels.add(lblTxtDienststelle);
         retrieveableLabels.add(lblTxtAnlass);
 
     }
 
     private void initProductPreviewImages() {
         productPreviewImages.put(hlPunktlistePdf, PUNKT_PDF);
         productPreviewImages.put(hlPunktlisteHtml, PUNKT_HTML);
         productPreviewImages.put(hlPunktlisteTxt, PUNKT_TXT);
         final ProductLabelMouseAdaper productListener = new ProductLabelMouseAdaper();
         hlPunktlistePdf.addMouseListener(productListener);
         hlPunktlisteHtml.addMouseListener(productListener);
         hlPunktlisteTxt.addMouseListener(productListener);
     }
 
     private void initProductPreview() {
         initProductPreviewImages();
         int maxX = 0, maxY = 0;
         for (ImageIcon ii : productPreviewImages.values()) {
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
 
     private void initFooterElements() {
         ObjectRendererUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, ObjectRendererUtils.FORWARD_SELECTED, ObjectRendererUtils.FORWARD_PRESSED);
         ObjectRendererUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, ObjectRendererUtils.BACKWARD_SELECTED, ObjectRendererUtils.BACKWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, FORWARD_SELECTED, FORWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, BACKWARD_SELECTED, BACKWARD_PRESSED);
     }
 
     private void initIcons() {
 //        BACKWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-sel.png"));
 //        BACKWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-pressed.png"));
 //
 //        FORWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-sel.png"));
 //        FORWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-pressed.png"));
         final ReflectionRenderer reflectionRenderer = new ReflectionRenderer(0.5f, 0.15f, false);
         BufferedImage i1 = null, i2 = null, i3 = null;
         try {
             //TODO: own picture!
             i1 = reflectionRenderer.appendReflection(ImageIO.read(getClass().getResource(ALKIS_RES_PACKAGE + "punktlistepdf.png")));
             i2 = reflectionRenderer.appendReflection(ImageIO.read(getClass().getResource(ALKIS_RES_PACKAGE + "punktlistehtml.png")));
             i3 = reflectionRenderer.appendReflection(ImageIO.read(getClass().getResource(ALKIS_RES_PACKAGE + "punktlistetxt.png")));
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         PUNKT_PDF = new ImageIcon(i1);
         PUNKT_HTML = new ImageIcon(i2);
         PUNKT_TXT = new ImageIcon(i3);
     }
 
     private void setWait(boolean waiting) {
         blWaiting.setBusy(waiting);
         blWaiting.setVisible(waiting);
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
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         blWaiting = new org.jdesktop.swingx.JXBusyLabel();
         panFooter = new javax.swing.JPanel();
         panButtons = new javax.swing.JPanel();
         panFooterLeft = new javax.swing.JPanel();
         lblBack = new javax.swing.JLabel();
         btnBack = new javax.swing.JButton();
         panFooterRight = new javax.swing.JPanel();
         btnForward = new javax.swing.JButton();
         lblForw = new javax.swing.JLabel();
         panInfo = new javax.swing.JPanel();
         panPointInfo = new RoundedPanel();
         lblTxtPunktkennung = new javax.swing.JLabel();
         lblTxtIdentifikator = new javax.swing.JLabel();
         btnRetrieve = new javax.swing.JButton();
         lblDescIdentifikator = new javax.swing.JLabel();
         lblDescMarke = new javax.swing.JLabel();
         lblDescPunktart = new javax.swing.JLabel();
         lblTxtPunktart = new javax.swing.JLabel();
         lblDescPunktkennung = new javax.swing.JLabel();
         lblTxtAbmarkungMarke = new javax.swing.JLabel();
         lblTxtBeginn = new javax.swing.JLabel();
         lblDescModellart = new javax.swing.JLabel();
         lblDescLand = new javax.swing.JLabel();
         lblDescDienststelle = new javax.swing.JLabel();
         lblDescBeginn = new javax.swing.JLabel();
         lblTxtModellart = new javax.swing.JLabel();
         lblTxtLand = new javax.swing.JLabel();
         lblTxtDienststelle = new javax.swing.JLabel();
         lblDescEnde = new javax.swing.JLabel();
         lblTxtEnde = new javax.swing.JLabel();
         lblDescAnlass = new javax.swing.JLabel();
         lblTxtAnlass = new javax.swing.JLabel();
         srpPointInfo = new de.cismet.tools.gui.SemiRoundedPanel();
         lblPointHead = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         panLocationInfos = new RoundedPanel();
         cbPunktorte = new javax.swing.JComboBox();
         lblDescKartendarstellung = new javax.swing.JLabel();
         lblDescRechtswert = new javax.swing.JLabel();
         lblDescHochwert = new javax.swing.JLabel();
         lblDescDatenerhebung = new javax.swing.JLabel();
         lblTxtRechtswert = new javax.swing.JLabel();
         lblTxtHochwert = new javax.swing.JLabel();
         lblTxtHinweise = new javax.swing.JLabel();
         lblDescKoordStatus = new javax.swing.JLabel();
         lblTxtKoordStatus = new javax.swing.JLabel();
         lblDescGenauigkeitsstufe = new javax.swing.JLabel();
         lblDescHinweise = new javax.swing.JLabel();
         lblTxtGenauigkeitsstufe = new javax.swing.JLabel();
         lblTxtDatenerhebung = new javax.swing.JLabel();
         srpHeadLocInfo = new de.cismet.tools.gui.SemiRoundedPanel();
         lblLocHead = new javax.swing.JLabel();
         lblDescPunktorte = new javax.swing.JLabel();
         lblDescPOModellart = new javax.swing.JLabel();
         lblTxtPOModellart = new javax.swing.JLabel();
         lblDescPOAnlass = new javax.swing.JLabel();
         lblTxtPOAnlass = new javax.swing.JLabel();
         lblDescPOBeginn = new javax.swing.JLabel();
         lblTxtPOBeginn = new javax.swing.JLabel();
         lblDescPOEnde = new javax.swing.JLabel();
         lblTxtPOEnde = new javax.swing.JLabel();
         jPanel4 = new javax.swing.JPanel();
         lblDescPLIdentifikator = new javax.swing.JLabel();
         lblTxtPLIdentifikator = new javax.swing.JLabel();
         lblDescPLObjektart = new javax.swing.JLabel();
         lblTxtPLObjektart = new javax.swing.JLabel();
         lblTxtKartendarstellung = new javax.swing.JLabel();
         jPanel5 = new javax.swing.JPanel();
         jPanel6 = new javax.swing.JPanel();
         panProducts = new javax.swing.JPanel();
         panPdfProducts = new RoundedPanel();
         hlPunktlistePdf = new org.jdesktop.swingx.JXHyperlink();
         jPanel1 = new javax.swing.JPanel();
         semiRoundedPanel4 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel4 = new javax.swing.JLabel();
         panHtmlProducts = new RoundedPanel();
         hlPunktlisteHtml = new org.jdesktop.swingx.JXHyperlink();
         jPanel2 = new javax.swing.JPanel();
         semiRoundedPanel5 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel5 = new javax.swing.JLabel();
         panSpacing = new javax.swing.JPanel();
         panProductPreview = new RoundedPanel();
         lblProductPreview = new javax.swing.JLabel();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblPreviewHead = new javax.swing.JLabel();
         panTxtProducts = new RoundedPanel();
         hlPunktlisteTxt = new org.jdesktop.swingx.JXHyperlink();
         jPanel7 = new javax.swing.JPanel();
         semiRoundedPanel6 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel6 = new javax.swing.JLabel();
 
         panTitle.setMinimumSize(new java.awt.Dimension(101, 32));
         panTitle.setOpaque(false);
         panTitle.setPreferredSize(new java.awt.Dimension(101, 32));
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
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panTitle.add(blWaiting, gridBagConstraints);
 
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
 
         panInfo.setOpaque(false);
         panInfo.setLayout(new java.awt.GridBagLayout());
 
         panPointInfo.setMaximumSize(new java.awt.Dimension(350, 500));
         panPointInfo.setMinimumSize(new java.awt.Dimension(350, 500));
         panPointInfo.setOpaque(false);
         panPointInfo.setPreferredSize(new java.awt.Dimension(350, 500));
         panPointInfo.setLayout(new java.awt.GridBagLayout());
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.pointcode}"), lblTxtPunktkennung, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(14, 7, 7, 7);
         panPointInfo.add(lblTxtPunktkennung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.uuid}"), lblTxtIdentifikator, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtIdentifikator, gridBagConstraints);
 
         btnRetrieve.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/network-wired.png"))); // NOI18N
         btnRetrieve.setText("Punktorte laden");
         btnRetrieve.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRetrieveActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panPointInfo.add(btnRetrieve, gridBagConstraints);
 
         lblDescIdentifikator.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescIdentifikator.setText("Identifikator:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescIdentifikator, gridBagConstraints);
 
         lblDescMarke.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescMarke.setText("Vermarkung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescMarke, gridBagConstraints);
 
         lblDescPunktart.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPunktart.setText("Punktart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescPunktart, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.pointtype.bezeichnung}"), lblTxtPunktart, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtPunktart, gridBagConstraints);
 
         lblDescPunktkennung.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPunktkennung.setText("Punktkennung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(14, 7, 7, 7);
         panPointInfo.add(lblDescPunktkennung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean}"), lblTxtAbmarkungMarke, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         binding.setConverter(ALKIS_VERMARKUNG_CONVERTER);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtAbmarkungMarke, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.beginn}"), lblTxtBeginn, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtBeginn, gridBagConstraints);
 
         lblDescModellart.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescModellart.setText("Modellart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescModellart, gridBagConstraints);
 
         lblDescLand.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescLand.setText("Land:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescLand, gridBagConstraints);
 
         lblDescDienststelle.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescDienststelle.setText("Dienststelle:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescDienststelle, gridBagConstraints);
 
         lblDescBeginn.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescBeginn.setText("Lebenszeit-Beginn:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescBeginn, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.modellart}"), lblTxtModellart, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtModellart, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.land}"), lblTxtLand, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtLand, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.dienststelle}"), lblTxtDienststelle, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtDienststelle, gridBagConstraints);
 
         lblDescEnde.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescEnde.setText("Lebenszeit-Ende:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescEnde, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.ende}"), lblTxtEnde, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtEnde, gridBagConstraints);
 
         lblDescAnlass.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescAnlass.setText("Anlass:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblDescAnlass, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.anlass}"), lblTxtAnlass, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panPointInfo.add(lblTxtAnlass, gridBagConstraints);
 
         srpPointInfo.setBackground(java.awt.Color.darkGray);
         srpPointInfo.setLayout(new java.awt.GridBagLayout());
 
         lblPointHead.setForeground(new java.awt.Color(255, 255, 255));
         lblPointHead.setText("Info");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpPointInfo.add(lblPointHead, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         panPointInfo.add(srpPointInfo, gridBagConstraints);
 
         jPanel3.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.weighty = 1.0;
         panPointInfo.add(jPanel3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
         panInfo.add(panPointInfo, gridBagConstraints);
 
         panLocationInfos.setMaximumSize(new java.awt.Dimension(500, 500));
         panLocationInfos.setMinimumSize(new java.awt.Dimension(500, 500));
         panLocationInfos.setOpaque(false);
         panLocationInfos.setPreferredSize(new java.awt.Dimension(500, 500));
         panLocationInfos.setLayout(new java.awt.GridBagLayout());
 
         cbPunktorte.setMaximumSize(new java.awt.Dimension(200, 20));
         cbPunktorte.setMinimumSize(new java.awt.Dimension(200, 20));
         cbPunktorte.setPreferredSize(new java.awt.Dimension(200, 20));
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${pointLocations}");
         org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, eLProperty, cbPunktorte);
         jComboBoxBinding.setSourceNullValue(null);
         jComboBoxBinding.setSourceUnreadableValue(null);
         bindingGroup.addBinding(jComboBoxBinding);
 
         cbPunktorte.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbPunktorteActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(14, 7, 7, 7);
         panLocationInfos.add(cbPunktorte, gridBagConstraints);
 
         lblDescKartendarstellung.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescKartendarstellung.setText("Kartendarstellung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescKartendarstellung, gridBagConstraints);
 
         lblDescRechtswert.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescRechtswert.setText("Rechtswert:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescRechtswert, gridBagConstraints);
 
         lblDescHochwert.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescHochwert.setText("Hochwert:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescHochwert, gridBagConstraints);
 
         lblDescDatenerhebung.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescDatenerhebung.setText("Datenerhebung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescDatenerhebung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.rechtswert}"), lblTxtRechtswert, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtRechtswert, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.hochwert}"), lblTxtHochwert, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtHochwert, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.hinweis}"), lblTxtHinweise, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtHinweise, gridBagConstraints);
 
         lblDescKoordStatus.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescKoordStatus.setText("Koordinatenstatus:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescKoordStatus, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.koordinatenStatusName}"), lblTxtKoordStatus, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtKoordStatus, gridBagConstraints);
 
         lblDescGenauigkeitsstufe.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescGenauigkeitsstufe.setText("Genauigkeitsstufe:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescGenauigkeitsstufe, gridBagConstraints);
 
         lblDescHinweise.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescHinweise.setText("Hinweise:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescHinweise, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.qualitaetsangabenGenauigkeitsstufeName}"), lblTxtGenauigkeitsstufe, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtGenauigkeitsstufe, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.qualitaetsangabenHerkunft}"), lblTxtDatenerhebung, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         binding.setConverter(ALKIS_ERHEBUNG_CONVERTER);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtDatenerhebung, gridBagConstraints);
 
         srpHeadLocInfo.setBackground(java.awt.Color.darkGray);
         srpHeadLocInfo.setLayout(new java.awt.GridBagLayout());
 
         lblLocHead.setForeground(new java.awt.Color(255, 255, 255));
         lblLocHead.setText("Punktorte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeadLocInfo.add(lblLocHead, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         panLocationInfos.add(srpHeadLocInfo, gridBagConstraints);
 
         lblDescPunktorte.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPunktorte.setText("Koordinatensystem:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(14, 7, 7, 7);
         panLocationInfos.add(lblDescPunktorte, gridBagConstraints);
 
         lblDescPOModellart.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPOModellart.setText("Modellart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescPOModellart, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.modellArt}"), lblTxtPOModellart, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtPOModellart, gridBagConstraints);
 
         lblDescPOAnlass.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPOAnlass.setText("Anlass:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescPOAnlass, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.anlass}"), lblTxtPOAnlass, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtPOAnlass, gridBagConstraints);
 
         lblDescPOBeginn.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPOBeginn.setText("Lebenszeit-Beginn:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 13;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescPOBeginn, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.lebenszeitIntervallBeginnt}"), lblTxtPOBeginn, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 13;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtPOBeginn, gridBagConstraints);
 
         lblDescPOEnde.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPOEnde.setText("Lebenszeit-Ende:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 14;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescPOEnde, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.lebenszeitIntervallEndet}"), lblTxtPOEnde, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 14;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtPOEnde, gridBagConstraints);
 
         jPanel4.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 15;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.weighty = 1.0;
         panLocationInfos.add(jPanel4, gridBagConstraints);
 
         lblDescPLIdentifikator.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPLIdentifikator.setText("Identifikator:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescPLIdentifikator, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.UUId}"), lblTxtPLIdentifikator, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtPLIdentifikator, gridBagConstraints);
 
         lblDescPLObjektart.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescPLObjektart.setText("Objektart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblDescPLObjektart, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.pointLocationType}"), lblTxtPLObjektart, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtPLObjektart, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, cbPunktorte, org.jdesktop.beansbinding.ELProperty.create("${selectedItem.kartendarstellung}"), lblTxtKartendarstellung, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("<Error>");
         binding.setConverter(ALKIS_BOOLEAN_CONVERTER);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
         panLocationInfos.add(lblTxtKartendarstellung, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
         panInfo.add(panLocationInfos, gridBagConstraints);
 
         jPanel5.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.weightx = 1.0;
         panInfo.add(jPanel5, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.weighty = 1.0;
         panInfo.add(jPanel6, gridBagConstraints);
 
         add(panInfo, "CARD_1");
 
         panProducts.setOpaque(false);
         panProducts.setLayout(new java.awt.GridBagLayout());
 
         panPdfProducts.setMaximumSize(new java.awt.Dimension(175, 80));
         panPdfProducts.setMinimumSize(new java.awt.Dimension(175, 80));
         panPdfProducts.setOpaque(false);
         panPdfProducts.setPreferredSize(new java.awt.Dimension(175, 80));
         panPdfProducts.setLayout(new java.awt.GridBagLayout());
 
         hlPunktlistePdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlPunktlistePdf.setText("Punktliste");
         hlPunktlistePdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlPunktlistePdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(17, 7, 7, 7);
         panPdfProducts.add(hlPunktlistePdf, gridBagConstraints);
 
         jPanel1.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
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
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 9, 5);
         panProducts.add(panPdfProducts, gridBagConstraints);
 
         panHtmlProducts.setMaximumSize(new java.awt.Dimension(175, 80));
         panHtmlProducts.setMinimumSize(new java.awt.Dimension(175, 80));
         panHtmlProducts.setOpaque(false);
         panHtmlProducts.setPreferredSize(new java.awt.Dimension(175, 80));
         panHtmlProducts.setLayout(new java.awt.GridBagLayout());
 
         hlPunktlisteHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlPunktlisteHtml.setText("Punktliste");
         hlPunktlisteHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlPunktlisteHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(17, 7, 7, 7);
         panHtmlProducts.add(hlPunktlisteHtml, gridBagConstraints);
 
         jPanel2.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
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
         gridBagConstraints.gridy = 3;
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
         gridBagConstraints.gridheight = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panProducts.add(panProductPreview, gridBagConstraints);
 
         panTxtProducts.setMaximumSize(new java.awt.Dimension(175, 80));
         panTxtProducts.setMinimumSize(new java.awt.Dimension(175, 80));
         panTxtProducts.setOpaque(false);
         panTxtProducts.setPreferredSize(new java.awt.Dimension(175, 80));
         panTxtProducts.setLayout(new java.awt.GridBagLayout());
 
         hlPunktlisteTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-plain.png"))); // NOI18N
         hlPunktlisteTxt.setText("Punktliste");
         hlPunktlisteTxt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlPunktlisteTxtActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(17, 7, 7, 7);
         panTxtProducts.add(hlPunktlisteTxt, gridBagConstraints);
 
         jPanel7.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         panTxtProducts.add(jPanel7, gridBagConstraints);
 
         semiRoundedPanel6.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel6.setLayout(new java.awt.GridBagLayout());
 
         jLabel6.setForeground(new java.awt.Color(255, 255, 255));
         jLabel6.setText("Textformat-Produkte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel6.add(jLabel6, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panTxtProducts.add(semiRoundedPanel6, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(9, 5, 5, 5);
         panProducts.add(panTxtProducts, gridBagConstraints);
 
         add(panProducts, "CARD_2");
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnRetrieveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetrieveActionPerformed
         final CidsBean bean = cidsBean;
         if (bean != null) {
             final String pointCode = String.valueOf(bean.getProperty("pointcode"));
             if (pointCode != null) {
                 AlkisSOAPWorkerService.execute(new RetrieveWorker(pointCode));
             }
         }
     }//GEN-LAST:event_btnRetrieveActionPerformed
 
     private void lblBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
 }//GEN-LAST:event_lblBackMouseClicked
 
     private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, CARD_1);
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
 }//GEN-LAST:event_btnBackActionPerformed
 
     private void btnForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, CARD_2);
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
 }//GEN-LAST:event_btnForwardActionPerformed
 
     private void lblForwMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
 }//GEN-LAST:event_lblForwMouseClicked
 
     private void hlPunktlistePdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlPunktlistePdfActionPerformed
         try {
             final String pointID = lblTxtIdentifikator.getText();
             final String pointArt = lblTxtPunktart.getText();
             AlkisCommons.Products.productPunktliste(pointID, pointArt, AlkisCommons.ProductFormat.PDF);
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Aufruf des Produkts", ex, Alkis_pointRenderer.this);
             log.error(ex);
         }
 }//GEN-LAST:event_hlPunktlistePdfActionPerformed
 
     private void hlPunktlisteHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlPunktlisteHtmlActionPerformed
         try {
             final String pointID = lblTxtIdentifikator.getText();
             final String pointArt = lblTxtPunktart.getText();
             AlkisCommons.Products.productPunktliste(pointID, pointArt, AlkisCommons.ProductFormat.HTML);
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Aufruf des Produkts", ex, Alkis_pointRenderer.this);
             log.error(ex);
         }
 }//GEN-LAST:event_hlPunktlisteHtmlActionPerformed
 
     private void cbPunktorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPunktorteActionPerformed
         Object selection = cbPunktorte.getSelectedItem();
         if (selection instanceof PointLocation) {
             PointLocation pointLoc = (PointLocation) selection;
             if (pointLoc.getKartendarstellung() != null && pointLoc.getKartendarstellung().equals("1")) {
                 cbPunktorte.setBackground(PUNKTORT_MIT_KARTENDARSTELLUNG);
             } else {
                 cbPunktorte.setBackground(Color.WHITE);
             }
         }
     }//GEN-LAST:event_cbPunktorteActionPerformed
 
     private void hlPunktlisteTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlPunktlisteTxtActionPerformed
         try {
             final String pointID = lblTxtIdentifikator.getText();
             final String pointArt = lblTxtPunktart.getText();
             AlkisCommons.Products.productPunktliste(pointID, pointArt, AlkisCommons.ProductFormat.TEXT);
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Aufruf des Produkts", ex, Alkis_pointRenderer.this);
             log.error(ex);
         }
     }//GEN-LAST:event_hlPunktlisteTxtActionPerformed
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     @Override
     public void setCidsBean(CidsBean cb) {
         bindingGroup.unbind();
         if (cb != null) {
             this.cidsBean = cb;
             bindingGroup.bind();
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
             title = "Punkt " + title;
         }
         this.title = title;
         lblTitle.setText(this.title);
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
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXBusyLabel blWaiting;
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnForward;
     private javax.swing.JButton btnRetrieve;
     private javax.swing.JComboBox cbPunktorte;
     private org.jdesktop.swingx.JXHyperlink hlPunktlisteHtml;
     private org.jdesktop.swingx.JXHyperlink hlPunktlistePdf;
     private org.jdesktop.swingx.JXHyperlink hlPunktlisteTxt;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JLabel lblBack;
     private javax.swing.JLabel lblDescAnlass;
     private javax.swing.JLabel lblDescBeginn;
     private javax.swing.JLabel lblDescDatenerhebung;
     private javax.swing.JLabel lblDescDienststelle;
     private javax.swing.JLabel lblDescEnde;
     private javax.swing.JLabel lblDescGenauigkeitsstufe;
     private javax.swing.JLabel lblDescHinweise;
     private javax.swing.JLabel lblDescHochwert;
     private javax.swing.JLabel lblDescIdentifikator;
     private javax.swing.JLabel lblDescKartendarstellung;
     private javax.swing.JLabel lblDescKoordStatus;
     private javax.swing.JLabel lblDescLand;
     private javax.swing.JLabel lblDescMarke;
     private javax.swing.JLabel lblDescModellart;
     private javax.swing.JLabel lblDescPLIdentifikator;
     private javax.swing.JLabel lblDescPLObjektart;
     private javax.swing.JLabel lblDescPOAnlass;
     private javax.swing.JLabel lblDescPOBeginn;
     private javax.swing.JLabel lblDescPOEnde;
     private javax.swing.JLabel lblDescPOModellart;
     private javax.swing.JLabel lblDescPunktart;
     private javax.swing.JLabel lblDescPunktkennung;
     private javax.swing.JLabel lblDescPunktorte;
     private javax.swing.JLabel lblDescRechtswert;
     private javax.swing.JLabel lblForw;
     private javax.swing.JLabel lblLocHead;
     private javax.swing.JLabel lblPointHead;
     private javax.swing.JLabel lblPreviewHead;
     private javax.swing.JLabel lblProductPreview;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JLabel lblTxtAbmarkungMarke;
     private javax.swing.JLabel lblTxtAnlass;
     private javax.swing.JLabel lblTxtBeginn;
     private javax.swing.JLabel lblTxtDatenerhebung;
     private javax.swing.JLabel lblTxtDienststelle;
     private javax.swing.JLabel lblTxtEnde;
     private javax.swing.JLabel lblTxtGenauigkeitsstufe;
     private javax.swing.JLabel lblTxtHinweise;
     private javax.swing.JLabel lblTxtHochwert;
     private javax.swing.JLabel lblTxtIdentifikator;
     private javax.swing.JLabel lblTxtKartendarstellung;
     private javax.swing.JLabel lblTxtKoordStatus;
     private javax.swing.JLabel lblTxtLand;
     private javax.swing.JLabel lblTxtModellart;
     private javax.swing.JLabel lblTxtPLIdentifikator;
     private javax.swing.JLabel lblTxtPLObjektart;
     private javax.swing.JLabel lblTxtPOAnlass;
     private javax.swing.JLabel lblTxtPOBeginn;
     private javax.swing.JLabel lblTxtPOEnde;
     private javax.swing.JLabel lblTxtPOModellart;
     private javax.swing.JLabel lblTxtPunktart;
     private javax.swing.JLabel lblTxtPunktkennung;
     private javax.swing.JLabel lblTxtRechtswert;
     private javax.swing.JPanel panButtons;
     private javax.swing.JPanel panFooter;
     private javax.swing.JPanel panFooterLeft;
     private javax.swing.JPanel panFooterRight;
     private javax.swing.JPanel panHtmlProducts;
     private javax.swing.JPanel panInfo;
     private javax.swing.JPanel panLocationInfos;
     private javax.swing.JPanel panPdfProducts;
     private javax.swing.JPanel panPointInfo;
     private javax.swing.JPanel panProductPreview;
     private javax.swing.JPanel panProducts;
     private javax.swing.JPanel panSpacing;
     private javax.swing.JPanel panTitle;
     private javax.swing.JPanel panTxtProducts;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel3;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel4;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel5;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel6;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeadLocInfo;
     private de.cismet.tools.gui.SemiRoundedPanel srpPointInfo;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
     }
 
     /**
      * @return the point
      */
     public Point getPoint() {
         return point;
     }
 
     /**
      * @param point the point to set
      */
     public void setPoint(Point point) {
         this.point = point;
     }
 
     /**
      * @return the pointLocations
      */
     public List<PointLocation> getPointLocations() {
         return pointLocations;
     }
 
     /**
      * @param pointLocations the pointLocations to set
      */
     public void setPointLocations(List<PointLocation> pointLocations) {
         this.pointLocations = pointLocations;
     }
 
     @Override
     public void dispose() {
         bindingGroup.unbind();
     }
 
     final class RetrieveWorker extends SwingWorker<Point, Void> {
 
         public RetrieveWorker(String pointCode) {
             setWait(true);
             this.pointCode = pointCode;
             btnRetrieve.setEnabled(false);
         }
         private final String pointCode;
 
         @Override
         protected Point doInBackground() throws Exception {
             return infoService.getPoint(soapProvider.getIdentityCard(), soapProvider.getService(), pointCode);
         }
 
         private void restoreOnException() {
             btnRetrieve.setEnabled(true);
         }
 
         @Override
         protected void done() {
             setWait(false);
             if (!isCancelled()) {
                 try {
                     final Point point = get();
                     if (point != null) {
                         Alkis_pointRenderer.this.setPoint(point);
                         final PointLocation[] pointlocArr = point.getPointLocations();
                        Arrays.sort(pointlocArr, POINTLOCATION_COMPARATOR);
                        Alkis_pointRenderer.this.setPointLocations(Arrays.asList(pointlocArr));
                         Alkis_pointRenderer.this.bindingGroup.unbind();
                         Alkis_pointRenderer.this.bindingGroup.bind();
                         panLocationInfos.setVisible(true);
                     }
                 } catch (InterruptedException ex) {
                     restoreOnException();
                     log.warn(ex, ex);
                 } catch (Exception ex) {
                     //TODO show error message to user?
                     restoreOnException();
                     org.jdesktop.swingx.error.ErrorInfo ei = new ErrorInfo("Fehler beim Retrieve", ex.getMessage(), null, null, ex, Level.ALL, null);
                     org.jdesktop.swingx.JXErrorPane.showDialog(StaticSwingTools.getParentFrame(Alkis_pointRenderer.this), ei);
                     log.error(ex, ex);
                 }
             }
         }
     }
 
     static final class LocationComboBoxRenderer extends JLabel
             implements ListCellRenderer {
 
         public LocationComboBoxRenderer() {
             setOpaque(true);
         }
 
         @Override
         public Component getListCellRendererComponent(
                 JList list,
                 Object value,
                 int index,
                 boolean isSelected,
                 boolean cellHasFocus) {
 
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(Color.WHITE);
 //                setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
 
             if (value instanceof PointLocation) {
                 final PointLocation loc = (PointLocation) value;
                 setText(loc.getKoordinatenReferenzSystem());
                 if (loc.getKartendarstellung() != null && loc.getKartendarstellung().equals("1")) {
                     if (!isSelected) {
                         setBackground(PUNKTORT_MIT_KARTENDARSTELLUNG);
                     }
                 }
             } else {
                 setText(value == null ? "" : value.toString());
             }
             return this;
         }
     }
 
     class ProductLabelMouseAdaper extends MouseAdapter {
 
         @Override
         public void mouseEntered(MouseEvent e) {
             final Object srcObj = e.getSource();
             final ImageIcon imageIcon = productPreviewImages.get(srcObj);
             if (imageIcon != null) {
                 lblProductPreview.setIcon(imageIcon);
             }
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
             lblProductPreview.setIcon(null);
         }
     }
 }
