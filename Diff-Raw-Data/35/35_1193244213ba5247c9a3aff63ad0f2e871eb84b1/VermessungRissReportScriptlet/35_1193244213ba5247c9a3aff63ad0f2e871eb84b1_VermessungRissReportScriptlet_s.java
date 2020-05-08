 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import net.sf.jasperreports.engine.JRDefaultScriptlet;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.net.URL;
 
 import java.util.Collection;
 
 import de.cismet.cids.custom.objecteditors.wunda_blau.VermessungRissEditor;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
 import de.cismet.security.exceptions.MissingArgumentException;
 import de.cismet.security.exceptions.NoHandlerForURLException;
 import de.cismet.security.exceptions.RequestFailedException;
 
 import de.cismet.tools.gui.MultiPagePictureReader;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class VermessungRissReportScriptlet extends JRDefaultScriptlet {
 
     //~ Static fields/initializers ---------------------------------------------
 
     protected static final Logger LOG = Logger.getLogger(VermessungRissReportScriptlet.class);
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   host        laufendeNummer DOCUMENT ME!
      * @param   schluessel  path DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   blatt       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Boolean isImageAvailable(final String host,
             final String schluessel,
             final Integer gemarkung,
             final String flur,
             final String blatt) {
         final Collection<URL> validURLs = VermessungRissEditor.getCorrespondingURLs(
                 host,
                 gemarkung,
                 flur,
                 schluessel,
                 blatt);
 
         InputStream streamToReadFrom = null;
         for (final URL url : validURLs) {
             try {
                 streamToReadFrom = WebAccessManager.getInstance().doRequest(url);
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
 
         return streamToReadFrom != null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   host        DOCUMENT ME!
      * @param   schluessel  path DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   blatt       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Image[] loadImages(final String host,
             final String schluessel,
             final Integer gemarkung,
             final String flur,
             final String blatt) {
         Image[] result = new Image[0];
         final Collection<URL> validURLs = VermessungRissEditor.getCorrespondingURLs(
                 host,
                 gemarkung,
                 flur,
                 schluessel,
                 blatt);
 
         MultiPagePictureReader reader = null;
         for (final URL url : validURLs) {
             try {
                 reader = new MultiPagePictureReader(url);
                 break;
             } catch (Exception ex) {
                 LOG.warn("Could not read document from URL '" + url.toExternalForm() + "'. Skipping this url.", ex);
             }
         }
 
         if (reader == null) {
             return result;
         }
 
         try {
             if (reader.getNumberOfPages() > 0) {
                 result = new Image[reader.getNumberOfPages()];
 
                 for (int i = 0; i < reader.getNumberOfPages(); i++) {
                     result[i] = reader.loadPage(i);
                 }
             }
         } catch (IOException ex) {
             LOG.error("Could not load associated images. Host: '" + host + "', schluessel: '" + schluessel
                         + "', gemarkung: '" + gemarkung + "', flur: '" + flur + "', blatt: '" + blatt + "'.",
                 ex);
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   host        DOCUMENT ME!
      * @param   schluessel  path DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   blatt       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Image loadImage(final String host,
             final String schluessel,
             final Integer gemarkung,
             final String flur,
             final String blatt) {
         final Collection<URL> validURLs = VermessungRissEditor.getCorrespondingURLs(
                 host,
                 gemarkung,
                 flur,
                 schluessel,
                 blatt);
 
         MultiPagePictureReader reader = null;
         for (final URL url : validURLs) {
             try {
                 reader = new MultiPagePictureReader(url);
                 break;
             } catch (Exception ex) {
                 LOG.warn("Could not read document from URL '" + url.toExternalForm() + "'. Skipping this url.", ex);
             }
         }
 
         BufferedImage result = null;
         if (reader == null) {
             return result;
         }
 
         try {
             if (reader.getNumberOfPages() > 0) {
                 result = reader.loadPage(0);
             }
         } catch (IOException ex) {
             LOG.error("Could not load associated image. Host: '" + host + "', schluessel: '" + schluessel
                         + "', gemarkung: '" + gemarkung + "', flur: '" + flur + "', blatt: '" + blatt + "'.",
                 ex);
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
 
         return result;
     }
 }
