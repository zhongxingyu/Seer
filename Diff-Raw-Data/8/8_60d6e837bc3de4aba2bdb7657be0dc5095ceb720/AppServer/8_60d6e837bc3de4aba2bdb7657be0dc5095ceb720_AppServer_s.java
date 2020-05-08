 package lux.appserver;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class AppServer {
     
     private static final String LUX_APPSERVER_PORT = "lux.appserver.port";
     private static final String LUX_SOLR_URL = "lux.solr.url";
     private static final String LUX_SOLR_HOST = "lux.solr.host";
     private static final String LUX_SOLR_PORT = "lux.solr.port";
     private static final String LUX_SOLR_PATH = "lux.solr.path";
     private static final String LUX_RESOURCE_BASE = "lux.appserver.resourceBase";
     private static final String LUX_CONTEXT = "lux.appserver.context";
     
     public static final String DEFAULT = "default";
     public static final String LUX_APP_FORWARDER = "lux.app-forwarder";
     
     // defaults
     private int port = 8080;            // -p
     private String context = "/";       // -c
     private String resourceBase = ".";  // -r
     
     private ServletHolder appForwarderHolder;
     private ServletHolder resourcesHolder;
     private static WebAppContext luxWebapp;
     private Server server;
     
     public static void main (String ... argv) throws Exception {
         //System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
         AppServer server = new AppServer(argv);
         server.start();
     }
     
     public AppServer (String ... argv) {
         
         handleProperties();
         handleArguments(argv);
         
         AppHandler handler = new AppHandler();
         
        AppForwarder xqueryForward = new AppForwarder();
        appForwarderHolder = new ServletHolder(xqueryForward);
        appForwarderHolder.setName(LUX_APP_FORWARDER);
         handler.addServlet(appForwarderHolder);
         
         DefaultServlet resources = new DefaultServlet();
         resourcesHolder = new ServletHolder(resources);
         resourcesHolder.setInitParameter("dirAllowed", "false");
         resourcesHolder.setName(DEFAULT);
         handler.addServlet(resourcesHolder);
 
 
         resourcesHolder.setInitParameter("resourceBase", resourceBase);
         appForwarderHolder.setInitParameter("resourceBase", resourceBase);
         
         if (appForwarderHolder.getInitParameter("solr-host") == null) {
             // embedded solr war:
             // FIXME: expose these paths via configuration?
             luxWebapp = new WebAppContext(handler, "src/main/webapp", "/solr");
             xqueryForward.setServletPath ("/solr");
             xqueryForward.setForwardPath ("/solr/lux");
             xqueryForward.setSolrPort (port);
             handler.setSolrWebapp (luxWebapp);
             // server.setHandler(webapp);
         }
 
         server = new Server(port);
 
         ServletContextHandler root = new ServletContextHandler(server, context);
         root.setWelcomeFiles(new String[] { "index.xqy" });
         root.setServletHandler(handler);
         
     }
     
     // read properties from lux.properties in the current directory, if it exists
     private void handleProperties () {
         File f = new File ("lux.properties");
         if (!f.canRead()) {
             return;
         }
         Properties props = new Properties();
         try {
             props.load(new FileInputStream(f));
         } catch (FileNotFoundException e) {
            error (e.getMessage());
         } catch (IOException e) {
             error (e.getMessage());
         }
         for (Object o : props.keySet()) {
             String pname = o.toString();
             String value = props.getProperty(pname);
             if (pname.equals(LUX_APPSERVER_PORT)) {
                 setPort (value);
             }
             else if (pname.equals(LUX_SOLR_URL)) {
                 setSolrURL(value);
             }
             else if (pname.equals(LUX_SOLR_HOST)) {
                 appForwarderHolder.setInitParameter("solr-host", value);
             }
             else if (pname.equals(LUX_SOLR_PORT)) {
                 appForwarderHolder.setInitParameter("solr-port", value);
             }
             else if (pname.equals(LUX_SOLR_PATH)) {
                 appForwarderHolder.setInitParameter("forward-path", value);
             }
             else if (pname.equals(LUX_RESOURCE_BASE)) {
                 resourceBase = value;
             }
             else if (pname.equals(LUX_CONTEXT)) {
                 context = value;
             }
             else {
                 System.setProperty(pname, value);
             }
         }
     }
 
     private void handleArguments (String... argv) {
         for (int i = 0; i < argv.length; i+=2) {
             String arg = argv[i];
             if (i == argv.length - 1) {
                usage ();
             }
             String value = argv[i+1];
             if (arg.equals("-p")) {
                 setPort(value);
             }
             else if (arg.equals("-c")) {
                 context = value;
             }
             else if (arg.equals("-s")) {
                 setSolrURL(value);
             }
             else if (arg.equals("-r")) {
                 resourceBase = value;
             }
         }
     }
 
     private void setSolrURL(String value) {
         URL solrURL=null;
         try {
             solrURL = new URL(value);
         } catch (MalformedURLException e) {
             error ("-s must be followed by a valid URL, not " + value);
         }
         appForwarderHolder.setInitParameter("solr-host", solrURL.getHost());
         appForwarderHolder.setInitParameter("forward-path", solrURL.getPath());
         setSolrPort (solrURL.getPort());
     }
     
     private void setSolrPort (int solrPort) {
         if (solrPort > 0) {
             appForwarderHolder.setInitParameter("solr-port", Integer.toString(solrPort));
         }
     }
 
     private void setPort(String value) {
         try {
             port = Integer.valueOf(value);
         } catch (NumberFormatException e) {
             error ("port must be a valid port number, not " + value);
         }
     }
     
     private void start () throws Exception {
         server.start();
         server.join();
     }
     
     private static void error (String error) {
         System.err.println (error);
         System.exit(-1);
     }
     
     private static void usage () {
         error ("Usage: java -jar lux-appserver.jar [-p {port}] [-c {context}] [-s http://{solr-host}:{solr-port}/{forward-path}] [-r {resource-base}]");
     }
     
 }
