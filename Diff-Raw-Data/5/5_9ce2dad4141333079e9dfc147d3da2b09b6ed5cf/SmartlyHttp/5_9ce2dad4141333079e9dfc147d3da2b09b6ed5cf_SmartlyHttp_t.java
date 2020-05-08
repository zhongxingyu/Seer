 package org.smartly.packages.http;
 
 
 import org.json.JSONObject;
 import org.smartly.Smartly;
 import org.smartly.commons.io.jsonrepository.JsonRepository;
 import org.smartly.commons.lang.CharEncoding;
 import org.smartly.commons.logging.Level;
 import org.smartly.commons.util.FileUtils;
 import org.smartly.commons.util.JsonWrapper;
 import org.smartly.commons.util.PathUtils;
 import org.smartly.commons.util.StringUtils;
 import org.smartly.packages.AbstractPackage;
 import org.smartly.packages.ISmartlyModalPackage;
 import org.smartly.packages.ISmartlySystemPackage;
 import org.smartly.packages.http.config.Deployer;
 import org.smartly.packages.http.impl.WebServer;
 import org.smartly.packages.velocity.SmartlyVelocity;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * This package must be started ( load() method ) before application packages and must be ready ( method ready() )
  * after all other packages.
  */
 public class SmartlyHttp
         extends AbstractPackage
         implements ISmartlySystemPackage, ISmartlyModalPackage {
 
     public static final String NAME = "smartly_http";
 
     public SmartlyHttp() {
         super(NAME, 2);
         super.setDescription("Http Module");
         super.setMaintainerName("Gian Angelo Geminiani");
         super.setMaintainerMail("angelo.geminiani@gmail.com");
         super.setMaintainerUrl("http://www.smartfeeling.org");
 
         //-- module dependencies --//
         super.addDependency(SmartlyVelocity.NAME, ""); // all versions
 
         //-- lib dependencies --//
         super.addDependency("org.mongodb:mongo-java-driver:2.7.3", "");
     }
 
     @Override
     public void load() {
         Smartly.register(new Deployer(Smartly.getConfigurationPath()));
     }
 
     @Override
     public void ready() {
         this.init();
     }
 
     @Override
     public void unload() {
         this.getLogger().info("EXITING " + this.getClass().getSimpleName());
     }
 
     // --------------------------------------------------------------------
     //               p r i v a t e
     // --------------------------------------------------------------------
 
     private void init() {
         //-- web server settings --//
         final boolean enabled = Smartly.getConfiguration().getBoolean("http.webserver.enabled");
         if (enabled) {
             final JSONObject configuration = Smartly.getConfiguration().getJSONObject("http.webserver");
             final String docRoot = JsonWrapper.getString(configuration, "root");
             final String absoluteDocRoot = Smartly.getAbsolutePath(docRoot);
 
             //-- the web server --//
             this.startWebserver(absoluteDocRoot, configuration);
         } else {
             super.getLogger().warning("Web Server not enabled! Check configuration file.");
         }
     }
 
     private void startWebserver(final String docRoot, final JSONObject configuration) {
         try {
             // ensure resource base exists
             FileUtils.mkdirs(docRoot);
 
             final WebServer server = new WebServer(docRoot, configuration);
             server.start();
         } catch (Throwable t) {
             super.getLogger().log(Level.SEVERE, null, t);
         }
     }
 
     // --------------------------------------------------------------------
     //               S T A T I C
     // --------------------------------------------------------------------
 
     private static String __htdocs;
     private static Set<String> _cmsPaths = new HashSet<String>(); // connector for CMS module. (paths are required in SmartlyResourceHandler)
 
     public static void registerCMSPaths(final Set<String> paths) {
         _cmsPaths.addAll(paths);
     }
 
     public static Set<String> getCMSPaths() {
         return _cmsPaths;
     }
 
     public static String getHTTPUrl(final String path) {
         final StringBuilder result = new StringBuilder();
         final String protocol = "http://";
         final String domain = Smartly.getConfiguration().getString("http.webserver.domain"); //getDomain(item);
        final int port = Smartly.getConfiguration().getInt("http.webserver.connectors.http.port", 80); //getPort(item);
 
         result.append(protocol);
         result.append(domain);
         if (port != 80) {
            result.append(":").append(port);
         }
 
         final String url;
         if (StringUtils.hasText(path)) {
             url = PathUtils.join(result.toString(), path);
         } else {
             url = result.toString().concat("/");
         }
 
         return url;
     }
 
     /**
      * Returns file path of doc root.
      *
      * @return
      */
     public static String getDocRoot() {
         if (!StringUtils.hasText(__htdocs)) {
             JsonRepository config;
             try {
                 config = Smartly.getConfiguration(true);
             } catch (Throwable ignored) {
                 config = Smartly.getConfiguration();
             }
             if (null != config) {
                 final String path = config.getString("http.webserver.root");
                 if (StringUtils.hasText(path)) {
                     __htdocs = Smartly.getAbsolutePath(path);
                 }
             }
         }
         return __htdocs;
     }
 
     public static String readFile(final String path) throws IOException {
         final String fullPath = PathUtils.concat(getDocRoot(), path);
         return new String(FileUtils.copyToByteArray(new File(fullPath)), CharEncoding.getDefault());
     }
 }
