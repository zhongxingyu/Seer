 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Collection;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 import javax.net.ssl.TrustManager;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.adito.boot.BootProgressMonitor;
 import com.adito.boot.Context;
 import com.adito.boot.ContextListener;
 import com.adito.boot.LogBootProgressMonitor;
 import com.adito.boot.PropertyClass;
 import com.adito.boot.PropertyPreferences;
 import com.adito.boot.RequestHandler;
 import com.adito.boot.RequestHandlerRequest;
 import com.adito.boot.RequestHandlerResponse;
 import com.adito.boot.SystemProperties;
 import com.adito.boot.VersionInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * Whilst Adito is largely a standard web application, it has
  * requirements of its environment above and beyond this.
  * <p>
  * Then environment that services these requires is known as the Context. There
  * should be a single instance of the implementation of this interface and it
  * should be registered with
  * {@link com.adito.boot.ContextHolder#setContext(Context)}.
  * <p>
  * The instance of the context may then be accessed in the web application and
  * boot classes using {@link com.adito.boot.ContextHolder#getContext()}.
  * <p>
  * The context responsibilities include :-
  * <ul>
  * <li>Service control. I.e. shutdown and restart</li>
  * <li>Provide locations to store configuration, temporary files, logs etc</li>
  * <li>Provide an HTTP server to serve web content and handle custom HTTP
  * connections</li>
  * <li>Provide and manager databases used for storing configuration and
  * Adito resources</li>
  * <li>Obfuscating / deobfuscating passwords</li>
  * <li>Configuring the class loader</li>
  * <li>Providing storewhere to store <i>Context properties</i>, i.e. those
  * that are only applicable to this Context implementation</li>
  * </ul>
  * 
  */
 public class CoreContext implements Context {
 
 	private static File dbDir = null;
     private static File confDir = null;
     private static File tmpDir = null;
     private static File logDir = null;
     private static File appDir = null;
     private static VersionInfo.Version version = null;
     
     private static BootProgressMonitor bootProgressMonitor;
     private static Preferences preferences = null;
     
     final static Log log = LogFactory.getLog(CoreContext.class);
     
     /**
      * We will not be running in setup mode, so returning false.
      * 
      * @return running in setup mode.
      */
     public boolean isSetupMode() {
     	
     	log.info("CALLED");
     	return false;
 
     }
 
     
     /**
      * We will not be able to restart the servlet context, for now at least.
      * 
      * @return restart available
      */
     public boolean isRestartAvailableMode() {
     	
     	log.info("CALLED");
     	return false;
     	
     }
 
     
     /**
      * We will not be able to shut down the servlet context, for now at least.
      * 
      * @param restart restart when shutdown complete
      */
     public void shutdown(boolean restart) 
     { 
     	log.info("SHOULD NOT BE CALLED");
     	// Do nothing
     	
     }
 
     
     /**
      * Get the current version of Adito.
      * TODO How we get the version in the right way?
      * 
      * @return current version
      */
     public VersionInfo.Version getVersion() {
     	
     	if (version == null) {
     		String versionString = "0.9.1"; // TODO Hardcoded!
     		version = new VersionInfo.Version(versionString);
     	}
     	log.info("RETURNING " + version);
     	return version;
     	
     }
 
     
     /**
      * Get the directory when configuration files are stored.
      * 
      * @return configuration file directory
      */
     public File getConfDirectory() {
     	
     	if (confDir == null) {
     		String realPath = CoreServlet.getServlet().getServletContext().getRealPath("/");
             String WIPath = realPath + "/WEB-INF/";
             String confPath = WIPath + SystemProperties.get("adito.directories.conf", "conf");
     		confDir = new File(confPath);
     	}
     	log.info("RETURNING " + confDir);
     	return confDir;
     	
     }
 
     
     /**
      * Get the directory where tempory files are stored
      * 
      * @return temporary directory
      */
     public File getTempDirectory() {
     	
     	if (tmpDir == null) {
     		String realPath = CoreServlet.getServlet().getServletContext().getRealPath("/");
             String WIPath = realPath + "/WEB-INF/";
             String tmpPath = WIPath + SystemProperties.get("adito.directories.tmp", "tmp");
     		tmpDir = new File(tmpPath);
     	}
     	log.info("RETURNING " + tmpDir);
     	return tmpDir;
     	
     }
 
     
     /**
      * Get the directory where logs are stored
      * 
      * @return logs
      */
     public File getLogDirectory() {
     	
     	if (logDir == null) {
     		String realPath = CoreServlet.getServlet().getServletContext().getRealPath("/");
             String WIPath = realPath + "/WEB-INF/";
             String logPath = WIPath + SystemProperties.get("adito.directories.logs", "logs");
     		logDir = new File(logPath);
     	}
     	log.info("RETURNING " + logDir);
     	return logDir;
     	
     }
 
     /**
      * Get the directory where database files are stored.
      * 
      * @return database files
      */
     public File getDBDirectory() {
     	
     	if (dbDir == null) {
     		String realPath = CoreServlet.getServlet().getServletContext().getRealPath("/");
             String WIPath = realPath + "/WEB-INF/";
             String dbPath = WIPath + SystemProperties.get("adito.directories.db", "db");
     		dbDir = new File(dbPath);
     	}
     	log.info("RETURNING " + dbDir);
     	return dbDir;
     	
     }
 
     /**
      * Get the directory where application extensions are stored.
      * 
      * @return application extension directory
      */
     public File getApplicationDirectory() {
     	
     	if (appDir == null) {
     		String realPath = CoreServlet.getServlet().getServletContext().getRealPath("/");
             String WIPath = realPath + "/WEB-INF/";
             String appPath = WIPath + SystemProperties.get("adito.directories.apps", "tmp/extensions");
     		appDir = new File(appPath);
     	}
     	log.info("RETURNING " + appDir);
     	return appDir;
     	
     }
 
     /**
      * Get the main thread. Not implemented for now.
      * 
      * @return main thread
      */
     public Thread getMainThread() {
     	
     	log.info("NOT IMPLEMENTED");
     	return null;
     	
     }
 
     
     /**
      * Add a new location that contains web resources. Web resources include
      * things suchs as HTML files, Images, CSS, JSP etc.
      * <p>
      * When serving content, the containing web server must search any added
      * resource bases for the requested resource starting with the first added
      * resource base until something is found.
      * <p>
      * This is a key part of the extension architecture
      * 
      * @param url url to add
      */
     public void addResourceBase(URL url) {
     	// TODO Copied as is from Main
     	/*
     	if (log.isInfoEnabled())
             log.info("Adding new resource base " + base.toExternalForm());
         ResourceCache cache = new ResourceCache();
         cache.setMimeMap(webappContext.getMimeMap());
         cache.setEncodingMap(webappContext.getEncodingMap());
         cache.setResourceBase(base.toExternalForm());
         try {
             cache.start();
             webappContext.addResourceCache(cache);
             if (httpContext != null) {
                 httpContext.addResourceCache(cache);
             }
             resourceCaches.put(base, cache);
         } catch (Exception e) {
             log.error("Failed to add new resource base " + base.toExternalForm() + ".", e);
         }
         */
     	log.info("NOT IMPLEMENTED");
     }
 
     
     /**
      * Remove a location from the list of locations that contains web resources.
      * 
      * @param url url to remove
      * @see Context#addResourceBase(URL)
      */
     public void removeResourceBase(URL url) {
     	// TODO Copied as is from Main
     	/*
         if (log.isInfoEnabled())
             log.info("Removing resource base " + base.toExternalForm());
         ResourceCache cache = (ResourceCache) resourceCaches.get(base);
         webappContext.removeResourceCache(cache);
         if (httpContext != null) {
             httpContext.removeResourceCache(cache);
         }
 		*/
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Get the list of current resource bases
      * 
      * @return resources bases
      */
     public Collection<URL> getResourceBases() {
     	// TODO Copied as is from Main
     	//return resourceCaches.keySet();
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     /**
      * Return the host name on which Adito is running.
      * 
      * @return hostname
      */
     public String getHostname()
     {
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     /**
      * Return the actual port Adito is running on. This may be different
      * to the port in the configuration if the server was invoked with the port
      * argument.
      * 
      * @return port server is running on
      */
     public int getPort()
     {
    	return 0;
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Add a location to the class loader. Any classes found at this location
      * should then made available to all plugins, the core and any JSP pages.
      * 
      * @param u location of classes
      */
     public void addContextLoaderURL(URL url) { 
     	
     	try {
             Class sysclass = URLClassLoader.class;
             Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
             method.setAccessible(true);
             method.invoke(getClass().getClassLoader(), new Object[] { url });
             if (log.isInfoEnabled())
                 log.info(url.toExternalForm() + " added to context classloader");
         } catch (Exception e) {
             log.error("Failed to add to classpath.", e);
         }
     	
     }
 
     /**
      * Add a custom request handler.
      * 
      * @param requestHandler request handler
      */
     public void registerRequestHandler(RequestHandler requestHandler) { 
     	log.info("NOT IMPLEMENTED");
     }
     
     /**
      * Add a custom request handler.
      * 
      * @param requestHandler request handler
      */
     public void registerRequestHandler(RequestHandler requestHandler, HandlerProtocol protocol) { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Remove a custom request handler
      * 
      * @param requestHandler request handler
      */
     public void deregisterRequestHandler(RequestHandler requestHandler) { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Obfuscate a password in a way that it be de-obfuscated with
      * {@link #deobfuscatePassword(String)}.
      * 
      * @param password password to obfuscate
      * @return de-obfuscated password
      */
     public String obfuscatePassword(String password)
     {
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     /**
      * De-ebfuscate a password that has been obfuscated with
      * {@link #obfuscatePassword(String)}.
      * 
      * @param password obfuscated password
      * @return deobfuscated password
      */
     public String deobfuscatePassword(String password)
     {
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     /**
      * Set the trust manager to use for incoming SSL connections.
      * 
      * @param trustManager trust manager to set
      * @param require required
      */
     public void setTrustMananger(TrustManager trustManager, boolean require) { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Add a new web application
      * 
      * @param contextPathSpec path (either / or /path/*)
      * @param webApp path to webapp or WAR file
      * @throws IOException on any error
      * @throws Exception
      */
     public void addWebApp(String contextPathSpec, String webApp) throws Exception { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Add a listener to those being notified of events from the <i>Context</i>.
      * 
      * @param contextListener listener to add
      */
     public void addContextListener(ContextListener contextListener) {
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * remove a listener from those being notified of events from the <i>Context</i>.
      * 
      * @param contextListener listener to remove
      */
     public void removeContextListener(ContextListener contextListener) { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Get the root preferences node. Any component of Adito core or its
      * plugins may use this to store configuration information.
      * 
      * @return root preferences node
      */
     public Preferences getPreferences() {
     	
     	log.info("CALLED");
     	if (preferences == null) {
 	    	File newPrefDir = new File(getConfDirectory(), "prefs");
 	        preferences = PropertyPreferences.SYSTEM_ROOT;
 	        try {
 	            if (!newPrefDir.exists() && Preferences.systemRoot().node("/com").nodeExists("adito")) {
 	                Preferences from = Preferences.systemRoot().node("/com/adito");
 	                log.warn("Migrating preferences");
 	                try {
 	                    copyNode(from.node("core"), preferences.node("core"));
 	                    from.node("core").removeNode();
 	                    copyNode(from.node("plugin"), preferences.node("plugin"));
 	                    from.node("plugin").removeNode();
 	                    copyNode(from.node("extensions"), preferences.node("extensions"));
 	                    from.node("extensions").removeNode();
 	                    copyNode(from.node("dbupgrader"), preferences.node("dbupgrader"));
 	                    from.node("dbupgrader").removeNode();
 	                } catch (Exception e) {
 	                    log.error("Failed to migrate preferences.", e);
 	                }
 	                try {
 	                    from.flush();
 	                } catch (BackingStoreException bse) {
 	                    log.error("Failed to flush old preferences");
 	                }
 	                try {
 	                	preferences.flush();
 	                } catch (BackingStoreException bse) {
 	                    log.error("Failed to flush new preferences");
 	                }
 	                if (log.isInfoEnabled()) {
 	                    log.info("Flushing preferences");
 	                }
 	
 	            }
 	        } catch (BackingStoreException bse) {
 	            log.error("Failed to migrate preferences.", bse);
 	        }
     	}
     	log.info("RETURNING");
     	return preferences;
     	
     }
 
     /**
      * Get the property class used for system configuration
      * 
      * @return system config property class
      */
     public PropertyClass getConfig()
     {
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     /**
      * Get the list of URLs that the context loader uses as its class path
      * 
      * @return context loader class path
      */
     public URL[] getContextLoaderClassPath()
     {
     	return null;
     }
 
     /**
      * Get the context class loadeer
      * 
      * @return context class loader
      */
     public ClassLoader getContextLoader()
     {
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     /**
      * Create an alias for the given URI to the given location.
      * 
      * @param uri uri to alias
      * @param location actual location
      */
     public void setResourceAlias(String uri, String location) { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Remove an alias for the given URI.
      * 
      * @param uri uri to alias
      */
     public void removeResourceAlias(String uri) { 
     	log.info("NOT IMPLEMENTED");
     }
 
     /**
      * Get the boot progress monitor. Calling methods on the monitor will have
      * no effect after the server has started
      * 
      * @return boot progress monitor
      */
     public BootProgressMonitor getBootProgressMonitor() {
     	
     	log.info("CALLED");
     	if (bootProgressMonitor == null) {
     		bootProgressMonitor = new LogBootProgressMonitor();
     	}
     	return bootProgressMonitor;
     	
     }
 
     /**
      * Create a HttpServletRequest from a RequestHandlerRequest. This only works
      * for RequestAdapter implementations as others are already
      * HttpServletRequests.
      * 
      * @param request
      * @return
      */
     public HttpServletRequest createServletRequest(RequestHandlerRequest request)
     {
     	log.info("NOT IMPLEMENTED");
     	return null;
     }
 
     
     /**
      * Not implementing, just returning null.
      * 
      * @param response
      * @param request
      * @return
      */
     public HttpServletResponse createServletResponse(RequestHandlerResponse response, HttpServletRequest request)
     {
    	return null;
     	log.info("NOT IMPLEMENTED");
     }
 
     
     /**
      * No need to implement?
      * 
      * @param session session
      */
     public void access(HttpSession session) {
     	
     	log.info("NOT IMPLEMENTED");
     	// Do nothing
     	
     }
     
     private void copyNode(Preferences from, Preferences to) throws BackingStoreException {
         String[] keys = from.keys();
         for (int i = 0; i < keys.length; i++) {
             to.put(keys[i], from.get(keys[i], ""));
         }
         String childNodes[] = from.childrenNames();
         for (int i = 0; i < childNodes.length; i++) {
             Preferences cn = from.node(childNodes[i]);
             Preferences tn = to.node(childNodes[i]);
             copyNode(cn, tn);
         }
     }
 
 }
