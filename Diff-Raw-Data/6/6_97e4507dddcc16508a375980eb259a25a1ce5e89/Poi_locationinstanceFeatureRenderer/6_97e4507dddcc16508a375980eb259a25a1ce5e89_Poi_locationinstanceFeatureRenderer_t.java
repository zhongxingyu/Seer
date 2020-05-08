 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.featurerenderer.wunda_blau;
 
 import de.cismet.cids.custom.wunda_blau.res.StaticProperties;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
 import de.cismet.cismap.commons.Refreshable;
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 import java.net.URL;
 import javax.swing.JComponent;
 
 /**
  *
  * @author srichter
  */
 public class Poi_locationinstanceFeatureRenderer extends CustomCidsFeatureRenderer {
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Poi_locationinstanceFeatureRenderer.class);
     private static final FeatureAnnotationSymbol DEFAULT_SYMBOL;
     private FeatureAnnotationSymbol symbol;
    private boolean assigned = false;
    
     static {
         DEFAULT_SYMBOL = getSymbolFromURLString(StaticProperties.POI_SIGNATUR_DEFAULT_ICON);
     }
 
     private static final FeatureAnnotationSymbol getSymbolFromURLString(final String in) {
         if (in != null && in.length() > 0) {
             try {
                 final URL symbolURL = new URL(in);
                 if (symbolURL != null) {
                     return new FeatureAnnotationSymbol(symbolURL);
                 }
             } catch (Exception ex) {
                 log.warn(ex, ex);
             }
         }
         return null;
     }

 
     @Override
     public void assign() {
         if (!assigned) {
             if (metaObject != null) {
                 cidsBean = metaObject.getBean();
                 if (cidsBean != null) {
                     String iconUrl;
                     symbol = null;
                     Object o = cidsBean.getProperty("signatur");
                     if (o instanceof CidsBean) {
                         iconUrl = getUrlStringFromSignature(o);
                         if (iconUrl != null) {
                             symbol = getSymbolFromURLString(iconUrl);
                         }
                     }
                     if (symbol == null) {
                         o = cidsBean.getProperty("mainlocationtype");
                         if (o instanceof CidsBean) {
                             final CidsBean mainLocationType = (CidsBean) o;
                             o = mainLocationType.getProperty("signatur");
                             iconUrl = getUrlStringFromSignature(o);
                             if (iconUrl != null) {
                                 symbol = getSymbolFromURLString(iconUrl);
                             }
                         }
                     }
                     if (symbol == null) {
                         symbol = DEFAULT_SYMBOL;
                     }
                 }
             assigned=true;
             }
         }
     }
 
     private final String getUrlStringFromSignature(Object signature) {
         if (signature instanceof CidsBean) {
             final CidsBean signatur = (CidsBean) signature;
             try {
                 final Object fileName = signatur.getProperty("filename");
 //                final Object fileExtension = signatur.getProperty("fileextension");
                 if (fileName != null) {
                     return StaticProperties.POI_SIGNATUR_URL_PREFIX + fileName + StaticProperties.POI_SIGNATUR_URL_SUFFIX;
                 }
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
         return null;
     }
 
     @Override
     public FeatureAnnotationSymbol getPointSymbol() {
         assign();
         final FeatureAnnotationSymbol ret = symbol;
         if (ret != null) {
             return ret;
         } else if (DEFAULT_SYMBOL != null) {
             return DEFAULT_SYMBOL;
         } else {
             return new FeatureAnnotationSymbol();
         }
     }
 
     @Override
     public JComponent getInfoComponent(Refreshable refresh) {
         return null;
     }
 
     @Override
     public float getTransparency() {
         return 1f;
     }
 }
