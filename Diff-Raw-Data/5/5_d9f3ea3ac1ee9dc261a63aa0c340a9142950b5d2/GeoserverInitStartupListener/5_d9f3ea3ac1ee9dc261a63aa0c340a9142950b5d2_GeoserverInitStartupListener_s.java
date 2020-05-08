 package org.geoserver;
 
 import java.util.logging.Logger;
 
 import javax.imageio.spi.ImageReaderSpi;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.geotools.factory.Hints;
 import org.geotools.resources.image.ImageUtilities;
 import org.geotools.util.logging.Logging;
 
 /**
  * Listens for GeoServer startup and tries to configure axis order, logging
  * redirection, and a few other things that really need to be set up before
  * anything else starts up
  */
 public class GeoserverInitStartupListener implements ServletContextListener {
     private static final Logger LOGGER = Logging
             .getLogger("org.geoserver.logging");
 
     public void contextDestroyed(ServletContextEvent sce) {
     }
 
     public void contextInitialized(ServletContextEvent sce) {
         // if the server admin did not set it up otherwise, force X/Y axis
         // ordering
         // This one is a good place because we need to initialize this property
         // before any other opeation can trigger the initialization of the CRS
         // subsystem
         if (System.getProperty("org.geotools.referencing.forceXY") == null) {
            Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
                    Boolean.TRUE);
         }
         if (Boolean.TRUE.equals(Hints
                 .getSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER))) {
             Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");
         }
 
         // HACK: java.util.prefs are awful. See
         // http://www.allaboutbalance.com/disableprefs. When the site comes
         // back up we should implement their better way of fixing the problem.
         System.setProperty("java.util.prefs.syncInterval", "5000000");
 
         // HACK: under JDK 1.4.2 the native java image i/o stuff is failing
         // in all containers besides Tomcat. If running under jdk 1.4.2 we
         // disable the native codecs, unless the user forced the setting already
         if (System.getProperty("java.version").startsWith("1.4")
                 && (System.getProperty("com.sun.media.imageio.disableCodecLib") == null)) {
             LOGGER.warning("Disabling mediaLib acceleration since this is a "
                     + "java 1.4 VM.\n If you want to force its enabling, " //
                     + "set -Dcom.sun.media.imageio.disableCodecLib=true "
                     + "in your virtual machine");
             System.setProperty("com.sun.media.imageio.disableCodecLib", "true");
         } else {
             // in any case, the native png reader is worse than the pure java ones, so
             // let's disable it (the native png writer is on the other side faster)...
             ImageUtilities.allowNativeCodec("png", ImageReaderSpi.class, false);
         }
     }
 
 }
