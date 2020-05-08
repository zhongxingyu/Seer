 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
import Sirius.navigator.ui.RequestsFullSizeComponent;

 import com.vividsolutions.jts.geom.Geometry;
 
 import java.awt.EventQueue;
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.text.DateFormat;
 
 import java.util.Date;
 import java.util.Locale;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.ImageIcon;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.cismet.cids.annotations.CidsAttribute;
 
 import de.cismet.cids.custom.deprecated.CoolTabPanel;
 import de.cismet.cids.custom.deprecated.JBreakLabel;
 import de.cismet.cids.custom.deprecated.JLoadDots;
 import de.cismet.cids.custom.deprecated.TabbedPaneUITransparent;
 
 import de.cismet.cids.tools.metaobjectrenderer.BlurredMapObjectRenderer;
 
 import de.cismet.tools.BrowserLauncher;
 
 /**
  * de.cismet.cids.objectrenderer.CoolMauerRenderer.
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 //de.cismet.cids.objectrenderer.CoolMauerRenderer
public class MauerRenderer extends BlurredMapObjectRenderer implements RequestsFullSizeComponent {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String TITLE = "Mauern";
     private static int lastSelected = 0;
 
     //~ Instance fields --------------------------------------------------------
 
     @CidsAttribute("Georeferenz.GEO_STRING")
     public Geometry geometry = null;
     @CidsAttribute("Hauptpruefung_1")
     public Date hpr_1 = null;
     @CidsAttribute("Hauptpruefung_2")
     public Date hpr_2 = null;
     @CidsAttribute("Nebenpruefung")
     public Date npr = null;
     @CidsAttribute("Bauwerksbuchfertigstellung")
     public Date bwbfs = null;
     @CidsAttribute("Stuetzmauertyp")
     public String stuetzmauertyp = null; // ------------------------------------------------
     @CidsAttribute("Zustand_Gelaender")
     public Float zustand_Gelaender = 0.0f;
     @CidsAttribute("San_Kosten_Gelaender")
     public Double san_Kosten_Gelaender = null;
     @CidsAttribute("San_Eingriff_Gelaender")
     public Float san_Eingriff_Gelaender = 0.0f;
     @CidsAttribute("San_Massnahme_Gelaender")
     public String san_Massnahme_Gelaender = null;
     @CidsAttribute("Beschreibung_Gelaender")
     public String beschreibung_Gelaender = null;
     // ------------------------------------------------
     @CidsAttribute("Zustand_Kopf")
     public Float zustand_Kopf = 0.0f;
     @CidsAttribute("San_Kosten_Kopf")
     public Double san_Kosten_Kopf = null;
     @CidsAttribute("San_Eingriff_Kopf")
     public Float san_Eingriff_Kopf = 0.0f;
     @CidsAttribute("San_Massnahme_Kopf")
     public String san_Massnahme_Kopf = null;
     @CidsAttribute("Beschreibung_Kopf")
     public String beschreibung_Kopf = null;
     // ------------------------------------------------
     @CidsAttribute("Zustand_Ansicht")
     public Float zustand_Ansicht = 0.0f;
     @CidsAttribute("San_Kosten_Ansicht")
     public Double san_Kosten_Ansicht = null;
     @CidsAttribute("San_Eingriff_Ansicht")
     public Float san_Eingriff_Ansicht = 0.0f;
     @CidsAttribute("San_Massnahme_Ansicht")
     public String san_Massnahme_Ansicht = null;
     @CidsAttribute("Beschreibung_Ansicht")
     public String beschreibung_Ansicht = null;
     // ------------------------------------------------
     @CidsAttribute("Zustand_Gruendung")
     public Float zustand_Gruendung = 0.0f;
     @CidsAttribute("San_Kosten_Gruendung")
     public Double san_Kosten_Gruendung = null;
     @CidsAttribute("San_Eingriff_Gruendung")
     public Float san_Eingriff_Gruendung = 0.0f;
     @CidsAttribute("San_Massnahme_Gruendung")
     public String san_Massnahme_Gruendung = null;
     @CidsAttribute("Beschreibung_Gruendung")
     public String beschreibung_Gruendung = null;
     // ------------------------------------------------
     @CidsAttribute("Zustand_Verformung")
     public Float zustand_Verformung = 0.0f;
     @CidsAttribute("San_Kosten_Verformung")
     public Double san_Kosten_Verformung = null;
     @CidsAttribute("San_Eingriff_Verformung")
     public Float san_Eingriff_Verformung = 0.0f;
     @CidsAttribute("San_Massnahme_Verformung")
     public String san_Massnahme_Verformung = null;
     @CidsAttribute("Beschreibung_Verformung")
     public String beschreibung_Verformung = null;
     // ------------------------------------------------
     @CidsAttribute("Zustand_Gelaende")
     public Float zustand_Gelaende = 0.0f;
     @CidsAttribute("San_Kosten_Gelaende")
     public Double san_Kosten_Gelaende = null;
     @CidsAttribute("San_Eingriff_Gelaende")
     public Float san_Eingriff_Gelaende = 0.0f;
     @CidsAttribute("San_Massnahme_Gelaende")
     public String san_Massnahme_Gelaende = null;
     @CidsAttribute("Beschreibung_Gelaende")
     public String beschreibung_Gelaende = null;
     // ------------------------------------------------
     @CidsAttribute("Umgebung")
     public String umgebung = null;
     @CidsAttribute("Lagebeschreibung")
     public String lagebeschreibung = null;
     @CidsAttribute("Sanierung")
     public Float sanierung = 0.0f;
     @CidsAttribute("Lagebezeichnung")
     public String lagebezeichnung = null;
     @CidsAttribute("Neigung")
     public String neigung = null;
     @CidsAttribute("Eigentuemer")
     public String eigentuemer = null;
     @CidsAttribute("Laenge")
     public String laenge = null;
     @CidsAttribute("Hoehe")
     public String hoehe = null;
     @CidsAttribute("Materialtyp")
     public String materialtyp = null;
     @CidsAttribute("Besonderheiten")
     public String besonderheiten = null;
     @CidsAttribute("Standsicherheit")
     public String standsicherheit = null;
     @CidsAttribute("Dauerhaftigkeit")
     public String dauerhaftigkeit = null;
     @CidsAttribute("Verkehrssicherheit")
     public String verkehrssicherheit = null;
     @CidsAttribute("Bild_1.OBJECT_NAME")
     public String bild1 = null;
     @CidsAttribute("Bild_2.OBJECT_NAME")
     public String bild2 = null;
     @CidsAttribute("Bild_1.URL_BASE_ID.SERVER")
     public String server = null;
     @CidsAttribute("Bild_1.URL_BASE_ID.PROT_PREFIX")
     public String prot = null;
     @CidsAttribute("Bild_1.URL_BASE_ID.PATH")
     public String path = null; // TODO: ggf path fuer bild_2 extra machen
     private ImageIcon ii1 = null;
     private ImageIcon ii2 = null;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private Date timer;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JLabel jLabe31;
     private javax.swing.JLabel jLabe32;
     private javax.swing.JLabel jLabe33;
     private javax.swing.JLabel jLabe42;
     private javax.swing.JLabel jLabe43;
     private javax.swing.JLabel jLabe44;
     private javax.swing.JLabel jLabe45;
     private javax.swing.JLabel jLabe46;
     private javax.swing.JLabel jLabe47;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel35;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel38;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel40;
     private javax.swing.JLabel jLabel41;
     private javax.swing.JLabel jLabel48;
     private javax.swing.JLabel jLabel49;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel51;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JLabel lbl1;
     private javax.swing.JLabel lbl10;
     private javax.swing.JLabel lbl11;
     private javax.swing.JLabel lbl12;
     private javax.swing.JLabel lbl13;
     private javax.swing.JLabel lbl14;
     private javax.swing.JLabel lbl15;
     private javax.swing.JLabel lbl16;
     private javax.swing.JLabel lbl17;
     private javax.swing.JLabel lbl18;
     private javax.swing.JLabel lbl19;
     private javax.swing.JLabel lbl2;
     private javax.swing.JLabel lbl20;
     private javax.swing.JLabel lbl21;
     private javax.swing.JLabel lbl22;
     private javax.swing.JLabel lbl23;
     private javax.swing.JLabel lbl24;
     private javax.swing.JLabel lbl25;
     private javax.swing.JLabel lbl26;
     private javax.swing.JLabel lbl27;
     private javax.swing.JLabel lbl28;
     private javax.swing.JLabel lbl29;
     private javax.swing.JLabel lbl3;
     private javax.swing.JLabel lbl30;
     private javax.swing.JLabel lbl31;
     private javax.swing.JLabel lbl32;
     private javax.swing.JLabel lbl33;
     private javax.swing.JLabel lbl34;
     private javax.swing.JLabel lbl35;
     private javax.swing.JLabel lbl36;
     private javax.swing.JLabel lbl37;
     private javax.swing.JLabel lbl38;
     private javax.swing.JLabel lbl39;
     private javax.swing.JLabel lbl4;
     private javax.swing.JLabel lbl40;
     private javax.swing.JLabel lbl41;
     private javax.swing.JLabel lbl42;
     private javax.swing.JLabel lbl43;
     private javax.swing.JLabel lbl44;
     private javax.swing.JLabel lbl45;
     private javax.swing.JLabel lbl46;
     private javax.swing.JLabel lbl47;
     private javax.swing.JLabel lbl5;
     private javax.swing.JLabel lbl51;
     private javax.swing.JLabel lbl6;
     private javax.swing.JLabel lbl7;
     private javax.swing.JLabel lbl8;
     private javax.swing.JLabel lbl9;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel panContent;
     private javax.swing.JPanel panInhalt;
     private javax.swing.JPanel panInhalt1;
     private javax.swing.JPanel panInhalt2;
     private javax.swing.JPanel panInhalt3;
     private javax.swing.JPanel panInhalt4;
     private javax.swing.JPanel panInhalt5;
     private javax.swing.JPanel panInhalt6;
     private javax.swing.JPanel panInhalt7;
     private javax.swing.JPanel panInter;
     private javax.swing.JPanel panMap;
     private javax.swing.JPanel panSpinner;
     private javax.swing.JPanel panTitle;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Konstruktor.
      */
     public MauerRenderer() {
         initComponents();
         setPanContent(panContent);
         setPanInter(panInter);
         setPanMap(panMap);
         setPanTitle(panTitle);
         setSpinner(panSpinner);
         timer = new Date();
         jTabbedPane1.addChangeListener(new ChangeListener() {
 
                 @Override
                 public void stateChanged(final ChangeEvent e) {
                     lastSelected = jTabbedPane1.getSelectedIndex();
                     timer = new Date();
                 }
             });
         if ((new Date().getTime() - timer.getTime()) < (60 * 1000L)) {
             jTabbedPane1.setSelectedIndex(lastSelected);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void assignAggregation() {
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void assignSingle() {
         if (this.geometry != null) {
             setGeometry(this.geometry);
         }
 
         if (lagebezeichnung != null) {
             lblTitle.setText("Mauer - " + lagebezeichnung.trim());
             lbl1.setText(lagebezeichnung.trim());
         } else {
             lbl1.setVisible(false);
             jLabel1.setVisible(false);
         }
 
         if (lagebeschreibung != null) {
             lbl2.setText((lagebeschreibung.trim()));
         } else {
             lbl2.setVisible(false);
             jLabel2.setVisible(false);
         }
         if (umgebung != null) {
             lbl3.setText((umgebung.trim()));
         } else {
             lbl3.setVisible(false);
             jLabel3.setVisible(false);
         }
         if (neigung != null) {
             lbl4.setText((neigung.trim()));
         } else {
             lbl4.setVisible(false);
             jLabel4.setVisible(false);
         }
         if (stuetzmauertyp != null) {
             lbl5.setText((stuetzmauertyp.trim()));
         } else {
             lbl5.setVisible(false);
             jLabel5.setVisible(false);
         }
         if (materialtyp != null) {
             lbl6.setText((materialtyp.trim()));
         } else {
             lbl6.setVisible(false);
             jLabel6.setVisible(false);
         }
         if (hoehe != null) {
             lbl7.setText((hoehe.trim()));
         } else {
             lbl7.setVisible(false);
             jLabel7.setVisible(false);
         }
         if (laenge != null) {
             lbl8.setText((laenge.trim()));
         } else {
             lbl8.setVisible(false);
             jLabel8.setVisible(false);
         }
         if (eigentuemer != null) {
             lbl9.setText((eigentuemer.trim()));
         } else {
             lbl9.setVisible(false);
             jLabel9.setVisible(false);
         }
         if (hpr_1 != null) {
             lbl10.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(hpr_1));
         } else {
             lbl10.setVisible(false);
             jLabel10.setVisible(false);
         }
         if (hpr_2 != null) {
             lbl11.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(hpr_2));
         } else {
             lbl11.setVisible(false);
             jLabel11.setVisible(false);
         }
         if (npr != null) {
             lbl51.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(npr));
         } else {
             lbl51.setVisible(false);
             jLabel51.setVisible(false);
         }
         if (bwbfs != null) {
             lbl12.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(bwbfs));
         } else {
             lbl12.setVisible(false);
             jLabel12.setVisible(false);
         }
         // -------------------------------------------------------------
         if (beschreibung_Gelaender != null) {
             lbl13.setText((beschreibung_Gelaender.trim()));
         } else {
             lbl13.setVisible(false);
             jLabel13.setVisible(false);
         }
 
         lbl14.setText(String.valueOf(zustand_Gelaender));
 
         if (san_Massnahme_Gelaender != null) {
             lbl15.setText((san_Massnahme_Gelaender.trim()));
         } else {
             lbl15.setVisible(false);
             jLabel5.setVisible(false);
         }
 
         lbl16.setText(String.valueOf(san_Kosten_Gelaender));
 
         lbl17.setText(String.valueOf(san_Eingriff_Gelaender));
         // -------------------------------------------------------------
         if (beschreibung_Kopf != null) {
             lbl18.setText((beschreibung_Kopf.trim()));
         } else {
             lbl18.setVisible(false);
             jLabel18.setVisible(false);
         }
 
         lbl19.setText(String.valueOf(zustand_Kopf));
 
         if (san_Massnahme_Kopf != null) {
             lbl20.setText((san_Massnahme_Kopf.trim()));
         } else {
             lbl20.setVisible(false);
             jLabel20.setVisible(false);
         }
 
         lbl21.setText(String.valueOf(san_Kosten_Kopf));
 
         lbl22.setText(String.valueOf(san_Eingriff_Kopf));
         // -------------------------------------------------------------
         if (beschreibung_Ansicht != null) {
             lbl23.setText((beschreibung_Ansicht.trim()));
         } else {
             lbl23.setVisible(false);
             jLabel23.setVisible(false);
         }
 
         lbl24.setText(String.valueOf(zustand_Ansicht));
 
         if (san_Massnahme_Ansicht != null) {
             lbl25.setText((san_Massnahme_Ansicht.trim()));
         } else {
             lbl25.setVisible(false);
             jLabel25.setVisible(false);
         }
 
         lbl26.setText(String.valueOf(san_Kosten_Ansicht));
 
         lbl27.setText(String.valueOf(san_Eingriff_Ansicht));
         // -------------------------------------------------------------
         if (beschreibung_Gruendung != null) {
             lbl28.setText((beschreibung_Gruendung.trim()));
         } else {
             lbl28.setVisible(false);
             jLabel28.setVisible(false);
         }
 
         lbl29.setText(String.valueOf(zustand_Gruendung));
 
         if (san_Massnahme_Gelaender != null) {
             lbl30.setText((san_Massnahme_Gruendung.trim()));
         } else {
             lbl30.setVisible(false);
             jLabel30.setVisible(false);
         }
 
         lbl31.setText(String.valueOf(san_Kosten_Gruendung));
 
         lbl32.setText(String.valueOf(san_Eingriff_Gruendung));
         // -------------------------------------------------------------
         if (beschreibung_Verformung != null) {
             lbl33.setText((beschreibung_Verformung.trim()));
         } else {
             lbl33.setVisible(false);
             jLabe33.setVisible(false);
         }
 
         lbl34.setText(String.valueOf(zustand_Verformung));
 
         if (san_Massnahme_Verformung != null) {
             lbl35.setText((san_Massnahme_Verformung.trim()));
         } else {
             lbl35.setVisible(false);
             jLabel35.setVisible(false);
         }
 
         lbl36.setText(String.valueOf(san_Kosten_Verformung));
 
         lbl37.setText(String.valueOf(san_Eingriff_Verformung));
         // -------------------------------------------------------------
         if (beschreibung_Gelaende != null) {
             lbl38.setText((beschreibung_Gelaende.trim()));
         } else {
             lbl38.setVisible(false);
             jLabel38.setVisible(false);
         }
 
         lbl39.setText(String.valueOf(zustand_Gelaende));
 
         if (san_Massnahme_Gelaende != null) {
             lbl40.setText((san_Massnahme_Gelaende.trim()));
         } else {
             lbl40.setVisible(false);
             jLabel40.setVisible(false);
         }
 
         lbl41.setText(String.valueOf(san_Kosten_Gelaende));
 
         lbl42.setText(String.valueOf(san_Eingriff_Gelaende));
         // -------------------------------------------------------------
 
         lbl43.setText(String.valueOf(sanierung));
 
         if ((standsicherheit != null) && !standsicherheit.trim().equals("null") && !standsicherheit.trim().equals("")) {
             lbl44.setText((standsicherheit.trim()));
         } else {
             lbl44.setVisible(false);
             jLabe44.setVisible(false);
         }
 
         if ((verkehrssicherheit != null) && !verkehrssicherheit.trim().equals("null")
                     && !verkehrssicherheit.trim().equals("")) {
             lbl45.setText((verkehrssicherheit.trim()));
         } else {
             lbl45.setVisible(false);
             jLabe45.setVisible(false);
         }
 
         if ((dauerhaftigkeit != null) && !dauerhaftigkeit.trim().equals("null") && !dauerhaftigkeit.trim().equals("")) {
             lbl46.setText((dauerhaftigkeit.trim()));
         } else {
             lbl46.setVisible(false);
             jLabe46.setVisible(false);
         }
         if ((besonderheiten != null) && !besonderheiten.trim().equals("null") && !besonderheiten.trim().equals("")) {
             lbl47.setText((besonderheiten.trim()));
         } else {
             lbl47.setVisible(false);
             jLabe47.setVisible(false);
         }
         // ---------------------------------------------------------------
         if (bild1 != null) {
             final Thread t1 = new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             try {
                                 final BufferedImage bi = ImageIO.read(new URL(prot + server + path + bild1));
                                 if (bi.getWidth() > bi.getHeight()) {
                                     setIi1(new ImageIcon((bi.getScaledInstance(150, -1, BufferedImage.SCALE_SMOOTH))));
                                 } else {
                                     setIi1(new ImageIcon((bi.getScaledInstance(-1, 150, BufferedImage.SCALE_SMOOTH))));
                                 }
                             } catch (MalformedURLException ex) {
                                 ex.printStackTrace();
                             } catch (IOException ex) {
                                 ex.printStackTrace();
                             }
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         if (getIi1() != null) {
                                             jButton1.setIcon(getIi1());
                                         } else {
                                             jButton1.setVisible(false);
                                             jLabel48.setVisible(false);
                                         }
                                     }
                                 });
                         }
                     });
             t1.start();
         } else {
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         jButton1.setVisible(false);
                         jLabel48.setVisible(false);
                     }
                 });
         }
 
         if (bild2 != null) {
             final Thread t2 = new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             try {
                                 final BufferedImage bi = ImageIO.read(new URL(prot + server + path + bild2));
                                 if (bi.getWidth() > bi.getHeight()) {
                                     setIi2(new ImageIcon((bi.getScaledInstance(150, -1, BufferedImage.SCALE_SMOOTH))));
                                 } else {
                                     setIi2(new ImageIcon((bi.getScaledInstance(-1, 150, BufferedImage.SCALE_SMOOTH))));
                                 }
                             } catch (MalformedURLException ex) {
                                 ex.printStackTrace();
                             } catch (IOException ex) {
                                 ex.printStackTrace();
                             }
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         if (getIi2() != null) {
                                             jButton2.setIcon(getIi2());
                                         } else {
                                             jButton2.setVisible(false);
                                             jLabel49.setVisible(false);
                                         }
                                     }
                                 });
                         }
                     });
             t2.start();
         } else {
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         jButton2.setVisible(false);
                         jLabel49.setVisible(false);
                         if (bild1 == null) {
                             jPanel2.setBorder(null);
                             jPanel2.setVisible(false);
                         }
                     }
                 });
         }
     }
 
     /**
      * Gibt das Verhaeltnis der Breite des Renderers zur Breite des internen Browsers aus.
      *
      * @return  Verhaeltnis Renderers / interner Browser
      */
     @Override
     public double getWidthRatio() {
         return 1.0;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ImageIcon getIi1() {
         return ii1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  ii1  DOCUMENT ME!
      */
     public void setIi1(final ImageIcon ii1) {
         this.ii1 = ii1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ImageIcon getIi2() {
         return ii2;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  ii2  DOCUMENT ME!
      */
     public void setIi2(final ImageIcon ii2) {
         this.ii2 = ii2;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         panMap = new javax.swing.JPanel();
         panSpinner = new JLoadDots();
         panInter = new javax.swing.JPanel();
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         panContent = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jTabbedPane1.setUI(new TabbedPaneUITransparent());
         jPanel4 = new CoolTabPanel();
         panInhalt = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         lbl1 = new JBreakLabel();
         lbl2 = new JBreakLabel();
         lbl3 = new JBreakLabel();
         lbl4 = new JBreakLabel();
         lbl5 = new JBreakLabel();
         lbl6 = new JBreakLabel();
         lbl7 = new JBreakLabel();
         lbl8 = new JBreakLabel();
         lbl9 = new JBreakLabel();
         lbl10 = new JBreakLabel();
         lbl11 = new JBreakLabel();
         lbl12 = new JBreakLabel();
         jLabel51 = new javax.swing.JLabel();
         lbl51 = new JBreakLabel();
         jPanel5 = new CoolTabPanel();
         panInhalt1 = new javax.swing.JPanel();
         jLabel13 = new javax.swing.JLabel();
         lbl13 = new JBreakLabel();
         jLabel14 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         lbl14 = new JBreakLabel();
         lbl15 = new JBreakLabel();
         jLabel16 = new javax.swing.JLabel();
         lbl16 = new JBreakLabel();
         jLabel17 = new javax.swing.JLabel();
         lbl17 = new JBreakLabel();
         jPanel6 = new CoolTabPanel();
         panInhalt2 = new javax.swing.JPanel();
         jLabel18 = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
         lbl19 = new JBreakLabel();
         jLabel20 = new javax.swing.JLabel();
         lbl20 = new JBreakLabel();
         jLabel21 = new javax.swing.JLabel();
         lbl21 = new JBreakLabel();
         jLabel22 = new javax.swing.JLabel();
         lbl22 = new JBreakLabel();
         lbl18 = new JBreakLabel();
         jPanel7 = new CoolTabPanel();
         panInhalt3 = new javax.swing.JPanel();
         jLabel23 = new javax.swing.JLabel();
         jLabel24 = new javax.swing.JLabel();
         jLabel25 = new javax.swing.JLabel();
         jLabel26 = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         lbl27 = new JBreakLabel();
         lbl26 = new JBreakLabel();
         lbl25 = new JBreakLabel();
         lbl24 = new JBreakLabel();
         lbl23 = new JBreakLabel();
         jPanel8 = new CoolTabPanel();
         panInhalt4 = new javax.swing.JPanel();
         jLabel28 = new javax.swing.JLabel();
         jLabel29 = new javax.swing.JLabel();
         lbl28 = new JBreakLabel();
         lbl29 = new JBreakLabel();
         jLabel30 = new javax.swing.JLabel();
         lbl30 = new JBreakLabel();
         jLabe31 = new javax.swing.JLabel();
         lbl31 = new JBreakLabel();
         jLabe32 = new javax.swing.JLabel();
         lbl32 = new JBreakLabel();
         jPanel9 = new CoolTabPanel();
         panInhalt5 = new javax.swing.JPanel();
         jLabe33 = new javax.swing.JLabel();
         lbl33 = new JBreakLabel();
         jLabel34 = new javax.swing.JLabel();
         lbl34 = new JBreakLabel();
         jLabel35 = new javax.swing.JLabel();
         lbl35 = new JBreakLabel();
         jLabel36 = new javax.swing.JLabel();
         lbl36 = new JBreakLabel();
         jLabel37 = new javax.swing.JLabel();
         lbl37 = new JBreakLabel();
         jPanel10 = new CoolTabPanel();
         panInhalt6 = new javax.swing.JPanel();
         jLabel38 = new javax.swing.JLabel();
         lbl38 = new JBreakLabel();
         jLabel39 = new javax.swing.JLabel();
         lbl39 = new JBreakLabel();
         jLabel40 = new javax.swing.JLabel();
         lbl40 = new JBreakLabel();
         jLabel41 = new javax.swing.JLabel();
         lbl41 = new JBreakLabel();
         jLabe42 = new javax.swing.JLabel();
         lbl42 = new JBreakLabel();
         jPanel1 = new CoolTabPanel();
         jPanel2 = new javax.swing.JPanel();
         jLabel48 = new javax.swing.JLabel();
         jLabel49 = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jPanel11 = new javax.swing.JPanel();
         panInhalt7 = new javax.swing.JPanel();
         jLabe43 = new javax.swing.JLabel();
         lbl43 = new JBreakLabel();
         jLabe44 = new javax.swing.JLabel();
         lbl44 = new JBreakLabel();
         jLabe45 = new javax.swing.JLabel();
         lbl45 = new JBreakLabel();
         jLabe46 = new javax.swing.JLabel();
         lbl46 = new JBreakLabel();
         jLabe47 = new javax.swing.JLabel();
         lbl47 = new JBreakLabel();
 
         setMinimumSize(new java.awt.Dimension(520, 417));
         setOpaque(false);
         setPreferredSize(new java.awt.Dimension(520, 417));
         setLayout(new java.awt.BorderLayout());
 
         panMap.setBackground(new java.awt.Color(0, 51, 51));
         panMap.setOpaque(false);
         panMap.setLayout(new java.awt.GridBagLayout());
 
         panSpinner.setOpaque(false);
 
         final javax.swing.GroupLayout panSpinnerLayout = new javax.swing.GroupLayout(panSpinner);
         panSpinner.setLayout(panSpinnerLayout);
         panSpinnerLayout.setHorizontalGroup(
             panSpinnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 100,
                 Short.MAX_VALUE));
         panSpinnerLayout.setVerticalGroup(
             panSpinnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 100,
                 Short.MAX_VALUE));
 
         panMap.add(panSpinner, new java.awt.GridBagConstraints());
 
         add(panMap, java.awt.BorderLayout.CENTER);
 
         panInter.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 5, 1));
         panInter.setOpaque(false);
 
         final javax.swing.GroupLayout panInterLayout = new javax.swing.GroupLayout(panInter);
         panInter.setLayout(panInterLayout);
         panInterLayout.setHorizontalGroup(
             panInterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 589,
                 Short.MAX_VALUE));
         panInterLayout.setVerticalGroup(
             panInterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 9,
                 Short.MAX_VALUE));
 
         add(panInter, java.awt.BorderLayout.SOUTH);
 
         panTitle.setOpaque(false);
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18));
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("Mauer");
 
         final javax.swing.GroupLayout panTitleLayout = new javax.swing.GroupLayout(panTitle);
         panTitle.setLayout(panTitleLayout);
         panTitleLayout.setHorizontalGroup(
             panTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 panTitleLayout.createSequentialGroup().addContainerGap().addComponent(lblTitle).addContainerGap(
                     523,
                     Short.MAX_VALUE)));
         panTitleLayout.setVerticalGroup(
             panTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 panTitleLayout.createSequentialGroup().addContainerGap().addComponent(lblTitle).addContainerGap(
                     javax.swing.GroupLayout.DEFAULT_SIZE,
                     Short.MAX_VALUE)));
 
         add(panTitle, java.awt.BorderLayout.NORTH);
 
         panContent.setOpaque(false);
         panContent.setLayout(new java.awt.BorderLayout());
 
         jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 10));
         jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
         jTabbedPane1.setFont(new java.awt.Font("Tahoma", 1, 11));
 
         jPanel4.setOpaque(false);
         jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt.setOpaque(false);
         panInhalt.setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel1.setText("Lagebezeichnung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 7);
         panInhalt.add(jLabel1, gridBagConstraints);
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel2.setText("Lagebeschreibung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel2, gridBagConstraints);
 
         jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel3.setText("Umgebung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel3, gridBagConstraints);
 
         jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel4.setText("Neigung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel4, gridBagConstraints);
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel5.setText("Stützmauer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel5, gridBagConstraints);
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel6.setText("Materialtyp:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel6, gridBagConstraints);
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel7.setText("Höhe:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel7, gridBagConstraints);
 
         jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel8.setText("Länge:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel8, gridBagConstraints);
 
         jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel9.setText("Eigentümer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel9, gridBagConstraints);
 
         jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel11.setText("Zweite Hauptprüfung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel11, gridBagConstraints);
 
         jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel10.setText("Erste Hauptprüfung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel10, gridBagConstraints);
 
         jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel12.setText("Bauwerksbuchfertigstellung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 7);
         panInhalt.add(jLabel12, gridBagConstraints);
 
         lbl1.setText("Beyenburger Furt");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt.add(lbl1, gridBagConstraints);
 
         lbl2.setText("Baustelle");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl2, gridBagConstraints);
 
         lbl3.setText("2004-08-02");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl3, gridBagConstraints);
 
         lbl4.setText("2004-08-30");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl4, gridBagConstraints);
 
         lbl5.setText("2005-03-11");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl5, gridBagConstraints);
 
         lbl6.setText("Sadowski");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl6, gridBagConstraints);
 
         lbl7.setText("Meiswinkel");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl7, gridBagConstraints);
 
         lbl8.setText("4,5x6 Color-Neg");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl8, gridBagConstraints);
 
         lbl9.setText("ja");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl9, gridBagConstraints);
 
         lbl10.setText("571283.tiff");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl10, gridBagConstraints);
 
         lbl11.setText("105 25");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl11, gridBagConstraints);
 
         lbl12.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt.add(lbl12, gridBagConstraints);
 
         jLabel51.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel51.setText("Nebenprüfung");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 7);
         panInhalt.add(jLabel51, gridBagConstraints);
 
         lbl51.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt.add(lbl51, gridBagConstraints);
 
         jPanel4.add(panInhalt);
 
         jTabbedPane1.addTab("Allgemein", jPanel4);
 
         jPanel5.setOpaque(false);
         jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt1.setOpaque(false);
         panInhalt1.setLayout(new java.awt.GridBagLayout());
 
         jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel13.setText("Beschreibung Geländer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt1.add(jLabel13, gridBagConstraints);
 
         lbl13.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt1.add(lbl13, gridBagConstraints);
 
         jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel14.setText("Zustand Geländer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt1.add(jLabel14, gridBagConstraints);
 
         jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel15.setText("Sanierungsmaßnahmen Geländer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt1.add(jLabel15, gridBagConstraints);
 
         lbl14.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt1.add(lbl14, gridBagConstraints);
 
         lbl15.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt1.add(lbl15, gridBagConstraints);
 
         jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel16.setText("Sanierungskosten Geländer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt1.add(jLabel16, gridBagConstraints);
 
         lbl16.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt1.add(lbl16, gridBagConstraints);
 
         jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel17.setText("Eingriff Geländer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt1.add(jLabel17, gridBagConstraints);
 
         lbl17.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt1.add(lbl17, gridBagConstraints);
 
         jPanel5.add(panInhalt1);
 
         jTabbedPane1.addTab("Geländer", jPanel5);
 
         jPanel6.setOpaque(false);
         jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt2.setOpaque(false);
         panInhalt2.setLayout(new java.awt.GridBagLayout());
 
         jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel18.setText("Beschreibung Kopf:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt2.add(jLabel18, gridBagConstraints);
 
         jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel19.setText("Zustand Kopf:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt2.add(jLabel19, gridBagConstraints);
 
         lbl19.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt2.add(lbl19, gridBagConstraints);
 
         jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel20.setText("Sanierungsmaßnahmen Kopf:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt2.add(jLabel20, gridBagConstraints);
 
         lbl20.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt2.add(lbl20, gridBagConstraints);
 
         jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel21.setText("Sanierungskosten Kopf:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt2.add(jLabel21, gridBagConstraints);
 
         lbl21.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt2.add(lbl21, gridBagConstraints);
 
         jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel22.setText("Eingriff Kopf:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt2.add(jLabel22, gridBagConstraints);
 
         lbl22.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt2.add(lbl22, gridBagConstraints);
 
         lbl18.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt2.add(lbl18, gridBagConstraints);
 
         jPanel6.add(panInhalt2);
 
         jTabbedPane1.addTab("Kopf", jPanel6);
 
         jPanel7.setOpaque(false);
         jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt3.setOpaque(false);
         panInhalt3.setLayout(new java.awt.GridBagLayout());
 
         jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel23.setText("Beschreibung Ansicht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt3.add(jLabel23, gridBagConstraints);
 
         jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel24.setText("Zustand Ansicht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt3.add(jLabel24, gridBagConstraints);
 
         jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel25.setText("Sanierungsmaßnahmen Ansicht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt3.add(jLabel25, gridBagConstraints);
 
         jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel26.setText("Sanierungskosten Ansicht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt3.add(jLabel26, gridBagConstraints);
 
         jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel27.setText("Eingriff Ansicht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt3.add(jLabel27, gridBagConstraints);
 
         lbl27.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt3.add(lbl27, gridBagConstraints);
 
         lbl26.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt3.add(lbl26, gridBagConstraints);
 
         lbl25.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt3.add(lbl25, gridBagConstraints);
 
         lbl24.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt3.add(lbl24, gridBagConstraints);
 
         lbl23.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt3.add(lbl23, gridBagConstraints);
 
         jPanel7.add(panInhalt3);
 
         jTabbedPane1.addTab("Ansicht", jPanel7);
 
         jPanel8.setOpaque(false);
         jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt4.setOpaque(false);
         panInhalt4.setLayout(new java.awt.GridBagLayout());
 
         jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel28.setText("Beschreibung Gründung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt4.add(jLabel28, gridBagConstraints);
 
         jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel29.setText("Zustand Gründung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt4.add(jLabel29, gridBagConstraints);
 
         lbl28.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt4.add(lbl28, gridBagConstraints);
 
         lbl29.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt4.add(lbl29, gridBagConstraints);
 
         jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel30.setText("Sanierungsmaßnahmen Gründung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt4.add(jLabel30, gridBagConstraints);
 
         lbl30.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt4.add(lbl30, gridBagConstraints);
 
         jLabe31.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe31.setText("Sanierungskosten Gründung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt4.add(jLabe31, gridBagConstraints);
 
         lbl31.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt4.add(lbl31, gridBagConstraints);
 
         jLabe32.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe32.setText("Eingriff Gründung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt4.add(jLabe32, gridBagConstraints);
 
         lbl32.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt4.add(lbl32, gridBagConstraints);
 
         jPanel8.add(panInhalt4);
 
         jTabbedPane1.addTab("Gründung", jPanel8);
 
         jPanel9.setOpaque(false);
         jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt5.setOpaque(false);
         panInhalt5.setLayout(new java.awt.GridBagLayout());
 
         jLabe33.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe33.setText("Beschreibung Verformung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt5.add(jLabe33, gridBagConstraints);
 
         lbl33.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt5.add(lbl33, gridBagConstraints);
 
         jLabel34.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel34.setText("Zustand Verformung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt5.add(jLabel34, gridBagConstraints);
 
         lbl34.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt5.add(lbl34, gridBagConstraints);
 
         jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel35.setText("Sanierungsmaßnahmen Verformung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt5.add(jLabel35, gridBagConstraints);
 
         lbl35.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt5.add(lbl35, gridBagConstraints);
 
         jLabel36.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel36.setText("Sanierungskosten Verformung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt5.add(jLabel36, gridBagConstraints);
 
         lbl36.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt5.add(lbl36, gridBagConstraints);
 
         jLabel37.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel37.setText("Eingriff Verformung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt5.add(jLabel37, gridBagConstraints);
 
         lbl37.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt5.add(lbl37, gridBagConstraints);
 
         jPanel9.add(panInhalt5);
 
         jTabbedPane1.addTab("Verformung", jPanel9);
 
         jPanel10.setOpaque(false);
         jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt6.setOpaque(false);
         panInhalt6.setLayout(new java.awt.GridBagLayout());
 
         jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel38.setText("Beschreibung Gelände:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt6.add(jLabel38, gridBagConstraints);
 
         lbl38.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt6.add(lbl38, gridBagConstraints);
 
         jLabel39.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel39.setText("Zustand Gelände:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt6.add(jLabel39, gridBagConstraints);
 
         lbl39.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt6.add(lbl39, gridBagConstraints);
 
         jLabel40.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel40.setText("Sanierungsmaßnahmen Gelände:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt6.add(jLabel40, gridBagConstraints);
 
         lbl40.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt6.add(lbl40, gridBagConstraints);
 
         jLabel41.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel41.setText("Sanierungskosten Gelände:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt6.add(jLabel41, gridBagConstraints);
 
         lbl41.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt6.add(lbl41, gridBagConstraints);
 
         jLabe42.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe42.setText("Eingriff Gelände:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt6.add(jLabe42, gridBagConstraints);
 
         lbl42.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt6.add(lbl42, gridBagConstraints);
 
         jPanel10.add(panInhalt6);
 
         jTabbedPane1.addTab("Gelände", jPanel10);
 
         jPanel1.setOpaque(false);
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createEmptyBorder(1, 10, 10, 10),
                 javax.swing.BorderFactory.createEtchedBorder(
                     new java.awt.Color(255, 255, 255),
                     new java.awt.Color(0, 0, 0))));
         jPanel2.setOpaque(false);
         jPanel2.setLayout(new java.awt.GridBagLayout());
 
         jLabel48.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel48.setText("Bild 1:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
         jPanel2.add(jLabel48, gridBagConstraints);
 
         jLabel49.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel49.setText("Bild 2:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
         jPanel2.add(jLabel49, gridBagConstraints);
 
         jButton1.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     jButton1ActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         jPanel2.add(jButton1, gridBagConstraints);
 
         jButton2.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     jButton2ActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         jPanel2.add(jButton2, gridBagConstraints);
 
         jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);
 
         jPanel11.setOpaque(false);
         jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         panInhalt7.setOpaque(false);
         panInhalt7.setLayout(new java.awt.GridBagLayout());
 
         jLabe43.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe43.setText("Sanierung:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt7.add(jLabe43, gridBagConstraints);
 
         lbl43.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
         panInhalt7.add(lbl43, gridBagConstraints);
 
         jLabe44.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe44.setText("Standsicherheit:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt7.add(jLabe44, gridBagConstraints);
 
         lbl44.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt7.add(lbl44, gridBagConstraints);
 
         jLabe45.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe45.setText("Verkehrssicherheit:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt7.add(jLabe45, gridBagConstraints);
 
         lbl45.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt7.add(lbl45, gridBagConstraints);
 
         jLabe46.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe46.setText("Dauerhaftigkeit:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt7.add(jLabe46, gridBagConstraints);
 
         lbl46.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 7);
         panInhalt7.add(lbl46, gridBagConstraints);
 
         jLabe47.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabe47.setText("Besonderheiten:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt7.add(jLabe47, gridBagConstraints);
 
         lbl47.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 10, 7);
         panInhalt7.add(lbl47, gridBagConstraints);
 
         jPanel11.add(panInhalt7);
 
         jPanel1.add(jPanel11, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("Weiteres", jPanel1);
 
         panContent.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
 
         add(panContent, java.awt.BorderLayout.WEST);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void jButton2ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton2ActionPerformed
         try {
             BrowserLauncher.openURL(prot + server + path + bild2);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }                                                                            //GEN-LAST:event_jButton2ActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
         try {
             BrowserLauncher.openURL(prot + server + path + bild1);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }                                                                            //GEN-LAST:event_jButton1ActionPerformed
 }
