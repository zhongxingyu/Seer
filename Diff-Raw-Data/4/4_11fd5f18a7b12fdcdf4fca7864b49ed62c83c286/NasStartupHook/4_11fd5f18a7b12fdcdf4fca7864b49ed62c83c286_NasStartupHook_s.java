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
 package de.cismet.cids.custom.nas;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
import org.openide.util.Exceptions;
 import org.openide.util.lookup.ServiceProvider;
 
 import java.util.HashSet;
 
 import javax.swing.SwingUtilities;
 
 import de.cismet.cids.custom.wunda_blau.search.actions.NasDataQueryAction;
 
 import de.cismet.cids.server.actions.ServerActionParameter;
 
 import de.cismet.tools.configuration.StartupHook;
 
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = StartupHook.class)
 public class NasStartupHook implements StartupHook {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static String SEVER_ACTION = "nasDataQuery";
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void applicationStarted() {
         SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final ServerActionParameter paramMethod = new ServerActionParameter(
                             NasDataQueryAction.PARAMETER_TYPE.METHOD.toString(),
                             NasDataQueryAction.METHOD_TYPE.GET_ALL);
                     HashSet<String> openOrderIds = null;
                     try {
                         openOrderIds = (HashSet<String>)SessionManager.getProxy()
                                     .executeTask(
                                             SEVER_ACTION,
                                             "WUNDA_BLAU",
                                             null,
                                             paramMethod);
                     } catch (ConnectionException ex) {
                        log.error("error while getting the list of undelivered nas orders from server",ex);
                         return;
                     }
                     if ((openOrderIds == null) || openOrderIds.isEmpty()) {
                         log.info("no pending nas orders found for the logged in user");
                         return;
                     }
                     final StringBuilder logMessageBuilder = new StringBuilder();
                     for (final String s : openOrderIds) {
                         logMessageBuilder.append(s);
                         logMessageBuilder.append(",");
                     }
                     log.fatal("pending nas orders found: " + logMessageBuilder.toString());
                     // generate a new NasDownload object for pending orders
                     for (final String s : openOrderIds) {
                         final NASDownload download = new NASDownload(s);
                         DownloadManager.instance().add(download);
                     }
                 }
             });
     }
 }
