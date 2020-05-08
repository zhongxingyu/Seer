 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.featurerenderer.wunda_blau;
 
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
import java.awt.Image;
 import java.awt.Paint;
 import java.awt.image.BufferedImage;
 
 import java.net.URL;
 
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
 
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class AlkisPointFeatureRenderer extends CustomCidsFeatureRenderer {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(AlkisPointFeatureRenderer.class);
 
     private static final String PATH2ICONS = "/de/cismet/cids/custom/featurerenderer/wunda_blau/";
 
     private static final HashMap<String, String> pointtypeIcons = new HashMap<String, String>(6);
     private static final HashMap<String, Color> pointtypeColors = new HashMap<String, Color>(6);
 
     static {
         pointtypeIcons.put("Aufnahmepunkt", PATH2ICONS + "pushpin_red.png");
         pointtypeIcons.put("Sonstiger Vermessungspunkt", PATH2ICONS + "pushpin_orange.png");
         pointtypeIcons.put("Grenzpunkt", PATH2ICONS + "pushpin_green.png");
         pointtypeIcons.put("Besonderer Gebaeudepunkt", PATH2ICONS + "pushpin_yellow.png");
         pointtypeIcons.put("Besonderer Bauwerkspunkt", PATH2ICONS + "pushpin_gray.png");
         pointtypeIcons.put("Besonderer Topographischer Punkt", PATH2ICONS + "pushpin_sienna.png");
 
         pointtypeColors.put("Aufnahmepunkt", new Color(0xFF, 0x00, 0x00));
         pointtypeColors.put("Sonstiger Vermessungspunkt", new Color(0xFF, 0x45, 0x00));
         pointtypeColors.put("Grenzpunkt", new Color(0x00, 0x80, 0x00));
         pointtypeColors.put("Besonderer Gebaeudepunkt", new Color(0xFF, 0xFF, 0x00));
         pointtypeColors.put("Besonderer Bauwerkspunkt", new Color(0x80, 0x80, 0x80));
         pointtypeColors.put("Besonderer Topographischer Punkt", new Color(0xA0, 0x52, 0x2D));
     }
 
     //~ Instance fields --------------------------------------------------------
 
     private Color alkisPointColor = new Color(0x00, 0x00, 0xFF);
     private ImageIcon alkisPointIcon;
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void setMetaObject(final MetaObject metaObject) throws ConnectionException {
         super.setMetaObject(metaObject);
 
         if (cidsBean != null) {
             String bezeichnung = null;
             try {
                 bezeichnung = ((CidsBean)cidsBean.getProperty("pointtype")).getProperty("bezeichnung").toString();
             } catch (Exception e) {
                 LOG.warn("Could not determine cidsBeans pointtype. Is it an alkis point?", e);
             }
 
             if ((bezeichnung == null) || !pointtypeColors.containsKey(bezeichnung)) {
                 LOG.warn("There is no icon or color defined for pointtype '" + bezeichnung + "'. Using default.");
                 return;
             }
 
             final URL urlToIcon = getClass().getResource(pointtypeIcons.get(bezeichnung));
             if (urlToIcon != null) {
                 alkisPointIcon = new ImageIcon(urlToIcon);
             }
 
             if (pointtypeColors.containsKey(bezeichnung)) {
                 alkisPointColor = pointtypeColors.get(bezeichnung);
             }
         }
     }
 
     @Override
     public Paint getLinePaint(final CidsFeature subFeature) {
         return getFillingStyle();
     }
 
     @Override
     public Paint getFillingStyle(final CidsFeature subFeature) {
         return alkisPointColor;
     }
 
     @Override
     public FeatureAnnotationSymbol getPointSymbol() {
         FeatureAnnotationSymbol result;
 
         if ((alkisPointIcon != null) && (alkisPointIcon != null)) {
             result = new FeatureAnnotationSymbol(alkisPointIcon.getImage());
            result.setSweetSpotX(0.5);
            result.setSweetSpotY(1.0);
         } else {
             final int fallbackSymbolSize = 8;
             final BufferedImage bufferedImage = new BufferedImage(
                     fallbackSymbolSize,
                     fallbackSymbolSize,
                     BufferedImage.TYPE_INT_ARGB);
 
             final Graphics2D graphics = (Graphics2D)bufferedImage.getGraphics();
             graphics.setColor(alkisPointColor);
             graphics.fillOval(0, 0, fallbackSymbolSize, fallbackSymbolSize);
 
             result = new FeatureAnnotationSymbol(bufferedImage);
             result.setSweetSpotX(0.5);
             result.setSweetSpotY(0.5);
         }
 
         return result;
     }
 
     @Override
     public void assign() {
     }
 }
