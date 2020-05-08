 package org.opengeo;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geotools.util.logging.Logging;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mortbay.thread.BoundedThreadPool;
 
 public class Dashboard {
     
     static final Logger log = Logging.getLogger("org.opengeo");
     
     public static void main(String[] args) {
         Server jetty = null;
 
         try {
             jetty = new Server();
 
             // don't even think of serving more than XX requests in parallel... we
             // have a limit in our processing and memory capacities
             BoundedThreadPool tp = new BoundedThreadPool();
             tp.setMaxThreads(50);
 
             SocketConnector conn = new SocketConnector();
             
             //port
             int port = -1;
             try {
                 port = Integer.parseInt(System.getProperty("jetty.port"));
             }
             catch(Exception e) {
                 port = 8080;
             }
             
             //set up connector
             conn.setPort(port);
             conn.setThreadPool(tp);
             conn.setAcceptQueueSize(100);
             conn.setMaxIdleTime(1000 * 60 * 60);
             conn.setSoLingerTime(-1);
             jetty.setConnectors(new Connector[] { conn });
             
             List contexts = new ArrayList();
             
             //geoserver context
             WebAppContext cxt = new WebAppContext();
             cxt.setContextPath("/geoserver");
             cxt.setWar("../geoserver/web/app/src/main/webapp");
             
             //check for GEOSERVER_DATA_DIR
             String gdd = System.getProperty("GEOSERVER_DATA_DIR");
             if (gdd == null) {
                 gdd = new File("../data_dir").getCanonicalPath();
                 System.setProperty("GEOSERVER_DATA_DIR", gdd);
             }
             contexts.add(cxt);
             
             //styler context
             cxt = new WebAppContext();
             cxt.setContextPath("/styler");
             cxt.setWar("../styler");
             contexts.add(cxt);
             
            //geoeditor context
            cxt = new WebAppContext();
            cxt.setContextPath("/geoeditor");
            cxt.setWar("../geoeditor/target/geoeditor");
            contexts.add(cxt);

             //geoexplorer context
             cxt = new WebAppContext();
             cxt.setContextPath("/geoexplorer");
             cxt.setWar("../geoexplorer/build/geoexplorer");
             contexts.add(cxt);
             
             //recipes context
             cxt = new WebAppContext();
             cxt.setContextPath("/recipes");
             cxt.setWar("../recipes");
             contexts.add(cxt);
 
             //dashboard context
             cxt = new WebAppContext();
             cxt.setContextPath("/dashboard");
             cxt.setWar("OpenGeo Dashboard/Resources");
             contexts.add(cxt);
             
             jetty.setHandlers((Handler[])contexts.toArray(new Handler[contexts.size()]));
             jetty.start();
 
             // use this to test normal stop behaviour, that is, to check stuff that
             // need to be done on container shutdown (and yes, this will make 
             // jetty stop just after you started it...)
             // jettyServer.stop(); 
         } catch (Exception e) {
             log.log(Level.SEVERE, "Could not start the Jetty server: " + e.getMessage(), e);
 
             if (jetty != null) {
                 try {
                     jetty.stop();
                 } catch (Exception e1) {
                     log.log(Level.SEVERE,
                         "Unable to stop the " + "Jetty server:" + e1.getMessage(), e1);
                 }
             }
         }
     }
 
 }
