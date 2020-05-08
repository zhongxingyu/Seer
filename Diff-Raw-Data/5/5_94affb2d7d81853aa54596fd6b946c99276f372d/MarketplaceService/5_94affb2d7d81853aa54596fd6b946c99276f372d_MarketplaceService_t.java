 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2011 Pentaho Corporation.  All rights reserved.
  *
  * Created Oct 10th, 2011
  * @author Will Gorman (wgorman@pentaho.com)
  */
 package org.pentaho.marketplace;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.StringReader;
 import java.net.URLClassLoader;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.di.core.Result;
 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.logging.LogLevel;
 import org.pentaho.di.job.Job;
 import org.pentaho.di.job.JobMeta;
 import org.pentaho.platform.api.engine.IPluginManager;
 import org.pentaho.platform.api.engine.IPluginResourceLoader;
 import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
 import org.pentaho.platform.engine.core.system.PentahoSystem;
 import org.pentaho.platform.engine.security.SecurityHelper;
 import org.pentaho.platform.util.VersionHelper;
 import org.pentaho.platform.util.VersionInfo;
 import org.pentaho.platform.util.web.HttpUtil;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import flexjson.JSONSerializer;
 import java.util.ArrayList;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.pentaho.telemetry.BaPluginTelemetry;
 import org.pentaho.telemetry.TelemetryHelper.TelemetryEventType;
 
 @Path("/marketplace/api/")
 public class MarketplaceService {
 
     private Log logger = LogFactory.getLog(MarketplaceService.class);
 
     public static final String PLUGIN_NAME = "marketplace";
     public static final String UNAUTORIZED_ACCESS = "Unauthorized Access. Your Pentaho roles do not allow you to make changes to plugins.";
     private XPath xpath;
 
     public MarketplaceService() {
 
         xpath = XPathFactory.newInstance().newXPath();
     }
 
     
     
     
     public static class MarketplaceSecurityException extends Exception {
 
         private static final long serialVersionUID = -1852471739131561628L;
     }
 
     
     
     public List<Plugin> getPlugins() {// throws MarketplaceSecurityException {
         // return a unauthorized exception if unauthorized?
         if (!hasMarketplacePermission()) {
             //throw new MarketplaceSecurityException();
         }
 
         // load plugins from url
         List<Plugin> plugins = loadPluginsFromSite();
 
 
         // There are 2 methods of doing this. 
         // 1) Plugin manager
         // 2) Scan file system
         // We'll use 2) because 1) gets totally screwed up after we do an install/uninstall operation
 
         // List<String> installedPlugins = getInstalledPluginsFromPluginManager();
         List<String> installedPlugins = getInstalledPluginsFromFileSystem();
 
 
 
         if (installedPlugins.size() > 0) {
             Map<String, Plugin> marketplacePlugins = new HashMap<String, Plugin>();
             if (plugins != null) {
                 for (Plugin plugin : plugins) {
                     marketplacePlugins.put(plugin.getId(), plugin);
                 }
             }
 
             for (String installedPlugin : installedPlugins) {
                 Plugin plugin = marketplacePlugins.get(installedPlugin);
                 if (plugin != null) {
                     plugin.setInstalled(true);
                     discoverInstalledVersion(plugin);
                 }
             }
         }
         return plugins;
     }
 
     protected List<String> getInstalledPluginsFromPluginManager() {
 
 
         // determine if any of the plugins are installed and what version they are
         IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
         return pluginManager.getRegisteredPlugins();
 
     }
     
     
     
     protected boolean reloadPlugins() {
         IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
         return pluginManager.reload();
     }
     
 
     private List<String> getInstalledPluginsFromFileSystem() {
         
         ArrayList<String> plugins = new ArrayList<String>();
         
         File systemDir = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/"));
         
         String[] dirs = systemDir.list(DirectoryFileFilter.INSTANCE);
         
         for (int i = 0; i < dirs.length; i++) {
             String dir = dirs[i];
             if((new File(systemDir.getAbsolutePath()+File.separator+dir+File.separator+"plugin.xml")).isFile()){
                 plugins.add(dir);
             }
             
         }
         
         return plugins;   
     }
     
     private void closeClassLoader(String pluginId) {
       IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
       ClassLoader cl = pluginManager.getClassLoader(pluginId);
       if (cl != null && cl instanceof URLClassLoader) {
         Util.closeURLClassLoader((URLClassLoader)cl);
       }
     }
     
     public StatusMessage uninstallPlugin(String id) throws MarketplaceSecurityException {
         if (!hasMarketplacePermission()) {
             throw new MarketplaceSecurityException();
         }
 
         List<Plugin> plugins = getPlugins();
         Plugin toUninstall = null;
 
         for (Plugin plugin : plugins) {
             if (plugin.getId().equals(id)) {
                 toUninstall = plugin;
             }
         }
         if (toUninstall == null) {
             return new StatusMessage("NO_PLUGIN", "Plugin Not Found");
         }
 
                 
         // before deletion, close class loader
         closeClassLoader(toUninstall.getId());
         
         String versionBranch = toUninstall.getInstalledBranch();
         // get plugin path
         String jobPath = PentahoSystem.getApplicationContext().getSolutionPath("system/" + PLUGIN_NAME + "/processes/uninstall_plugin.kjb");
 
         try {
             JobMeta uninstallJobMeta = new JobMeta(jobPath, null);
             Job job = new Job(null, uninstallJobMeta);
 
             File file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/backups"));
             file.mkdirs();
 
             String uninstallBackup = PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/backups/" + toUninstall.getId() + "_" + new Date().getTime());
             job.getJobMeta().setParameterValue("uninstallLocation", PentahoSystem.getApplicationContext().getSolutionPath("system/" + toUninstall.getId()));
             job.getJobMeta().setParameterValue("uninstallBackup", uninstallBackup);
             job.getJobMeta().setParameterValue("samplesDir", "/public/plugin-samples/" + toUninstall.getId());
             
             job.copyParametersFrom(job.getJobMeta());
             job.activateParameters();
             job.start();
             job.waitUntilFinished();
             Result result = job.getResult(); // Execute the selected job.
           
             if (result == null || result.getNrErrors() > 0) {
               return new StatusMessage("FAIL", "Failed to execute uninstall, see log for details.");
             }
         } catch (KettleException e) {
             logger.error(e.getMessage(), e);
         }
 
         
         
         BaPluginTelemetry telemetryEvent = new BaPluginTelemetry(PLUGIN_NAME);
         Map<String, String> extraInfo = new HashMap<String, String>(1);
         extraInfo.put("uninstalledPlugin", toUninstall.getId());
         extraInfo.put("uninstalledPluginVersion", toUninstall.getInstalledVersion());
         extraInfo.put("uninstalledPluginBranch", toUninstall.getInstalledBranch());
         telemetryEvent.sendTelemetryRequest(TelemetryEventType.REMOVAL, extraInfo);
         
         return new StatusMessage("PLUGIN_UNINSTALLED", toUninstall.getName() + " was successfully uninstalled.  Please restart your BI Server.");
 
     }
 
     /**
      * this method installs a specified plugin based on id.  
      * 
      * @param id the plugin to install
      * @return a status mesasge to display the user
      */
     public StatusMessage installPlugin(String id, String versionBranch) throws MarketplaceSecurityException {
         if (!hasMarketplacePermission()) {
             throw new MarketplaceSecurityException();
         }
 
         List<Plugin> plugins = getPlugins();
         Plugin toInstall = null;
         for (Plugin plugin : plugins) {
             if (plugin.getId().equals(id)) {
                 toInstall = plugin;
             }
         }
         if (toInstall == null) {
             return new StatusMessage("NO_PLUGIN", "Plugin Not Found");
         }
 
         // this checks to make sure the plugin metadata isn't attempting to overwrite a folder on the system.
         // TODO: Test a .. encoded in UTF8, etc to see if there is a way to thwart this check
         if (toInstall.getId().indexOf(".") >= 0) {
             return new StatusMessage("NO_PLUGIN", "Plugin ID contains an illegal character");
         }
 
          // before deletion, close class loader
         closeClassLoader(toInstall.getId());      
         
         
         
         String downloadUrl, samplesDownloadUrl, availableVersion;
 
         if (versionBranch != null && versionBranch.length() > 0) {
             PluginVersion v = toInstall.getVersionByBranch(versionBranch);
             if (v == null) {
                 return new StatusMessage("NO_PLUGIN", "Plugin version not found");
             }
             downloadUrl = v.getDownloadUrl();
             samplesDownloadUrl = v.getSamplesDownloadUrl();
             availableVersion = v.getVersion();
         } else {
             return new StatusMessage("FAIL", "Version " + versionBranch + " not found for plugin " + id + ", see log for details.");
         }
 
         // get plugin path
         String jobPath = PentahoSystem.getApplicationContext().getSolutionPath("system/" + PLUGIN_NAME + "/processes/download_and_install_plugin.kjb");
         
         
         try {      
             JobMeta installJobMeta = new JobMeta(jobPath, null); 
             Job job = new Job(null, installJobMeta);
 
             File file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/downloads"));
             file.mkdirs();
             file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/backups"));
             file.mkdirs();
             file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/staging"));
             file.mkdirs();
 
             job.getJobMeta().setParameterValue("downloadUrl", downloadUrl);
             if (toInstall.getVersionByBranch(versionBranch).getSamplesDownloadUrl() != null) {
                 job.getJobMeta().setParameterValue("samplesDownloadUrl", samplesDownloadUrl);
                 job.getJobMeta().setParameterValue("samplesDir", "/public/plugin-samples");
                 job.getJobMeta().setParameterValue("samplesTargetDestination", PentahoSystem.getApplicationContext().getSolutionPath("plugin-samples/" + toInstall.getId()));
                 job.getJobMeta().setParameterValue("samplesTargetBackup", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/backups/" + toInstall.getId() + "_samples_" + new Date().getTime()));
                job.getJobMeta().setParameterValue("samplesDownloadDestination", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/downloads/" + toInstall.getId() + "-samples-" + availableVersion + "_" + new Date().getTime() + ".zip"));
                 job.getJobMeta().setParameterValue("samplesStagingDestination", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/staging_samples"));
                 job.getJobMeta().setParameterValue("samplesStagingDestinationAndDir", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/staging_samples/" + toInstall.getId()));
             }
            job.getJobMeta().setParameterValue("downloadDestination", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/downloads/" + toInstall.getId() + "-" + availableVersion + "_" + new Date().getTime() + ".zip"));
             job.getJobMeta().setParameterValue("stagingDestination", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/staging"));
             job.getJobMeta().setParameterValue("stagingDestinationAndDir", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/staging/" + toInstall.getId()));
             job.getJobMeta().setParameterValue("targetDestination", PentahoSystem.getApplicationContext().getSolutionPath("system/" + toInstall.getId()));
             job.getJobMeta().setParameterValue("targetBackup", PentahoSystem.getApplicationContext().getSolutionPath("system/plugin-cache/backups/" + toInstall.getId() + "_" + new Date().getTime()));
 
             job.copyParametersFrom(job.getJobMeta());
             job.setLogLevel(LogLevel.DETAILED);
             job.activateParameters();
             job.start();
             job.waitUntilFinished();
             Result result = job.getResult(); // Execute the selected job.
 
             if (result == null || result.getNrErrors() > 0) {
               return new StatusMessage("FAIL", "Failed to execute install, see log for details.");
             }
         } catch (KettleException e) {
             logger.error(e.getMessage(), e);
         }
 
         
         BaPluginTelemetry telemetryEvent = new BaPluginTelemetry(PLUGIN_NAME);
         Map<String, String> extraInfo = new HashMap<String, String>(1);
         extraInfo.put("installedPlugin", toInstall.getId());
         extraInfo.put("installedVersion", availableVersion);
         extraInfo.put("installedBranch", versionBranch);
         
         telemetryEvent.sendTelemetryRequest(TelemetryEventType.INSTALLATION, extraInfo);
         
         reloadPlugins();
         
         return new StatusMessage("PLUGIN_INSTALLED", toInstall.getName() + " was successfully installed.  Please restart your BI Server. \n" + toInstall.getInstallationNotes());
     }
 
     /**
      *  This method wraps the installPlugin method, returning JSON instead of XML.
      */
      @POST
      @Path("/plugin/{pluginId}/{versionBranch}")
      @Produces(MediaType.APPLICATION_JSON)    
     public String installPluginJson(@PathParam("pluginId") String pluginId, 
             @PathParam("versionBranch") String versionBranch) {
         try {
             StatusMessage msg = installPlugin(pluginId, versionBranch);
             JSONSerializer serializer = new JSONSerializer();
             String json = serializer.deepSerialize(msg);
             return json;
         } catch (MarketplaceSecurityException e) {
             logger.debug(e.getMessage(), e);
             return createJsonMessage(UNAUTORIZED_ACCESS, "ERROR_0002_UNAUTHORIZED_ACCESS");
         }
     }
 
      
      @DELETE
      @Path("/plugin/{pluginId}")
      @Produces(MediaType.APPLICATION_JSON)         
     public String uninstallPluginJson(@PathParam("pluginId")  String pluginId) {
         try {
             StatusMessage msg = uninstallPlugin(pluginId);
             JSONSerializer serializer = new JSONSerializer();
             String json = serializer.deepSerialize(msg);
             return json;
         } catch (MarketplaceSecurityException e) {
             logger.debug(e.getMessage(), e);
             return createJsonMessage(UNAUTORIZED_ACCESS, "ERROR_0002_UNAUTHORIZED_ACCESS");
         }
     }
 
     
     
     @GET
     @Path("/plugins")
     @Produces(MediaType.APPLICATION_JSON)    
     public String getPluginsJson() {
         //try {
             List<Plugin> pluginArray = getPlugins();
             JSONSerializer serializer = new JSONSerializer();
             String json = serializer.deepSerialize(pluginArray);
             return json;
         //} catch (MarketplaceSecurityException e) {
         //    logger.debug(e.getMessage(), e);
             // error(Messages.getErrorString("UserSettingService.ERROR_0002_SETTINGS_READ", e.getLocalizedMessage()), e); //$NON-NLS-1$
             // return createJsonMessage(Messages.getString("UserSettingService.ERROR_0002_SETTINGS_READ", e.getLocalizedMessage()), "ERROR_0002_SETTINGS_READ"); //$NON-NLS-1$ //$NON-NLS-2$
         //    return createJsonMessage("Unauthorized Access", "ERROR_0002_UNAUTHORIZED_ACCESS"); //$NON-NLS-1$ //$NON-NLS-2$
         //}
     }
 
     protected boolean hasMarketplacePermission() {
         Authentication auth = SecurityHelper.getInstance().getAuthentication(PentahoSessionHolder.getSession(), true);
         IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
         String roles = null;
         String users = null;
 
         try {
             roles = resLoader.getPluginSetting(getClass(), "settings/marketplace-roles"); //$NON-NLS-1$
             users = resLoader.getPluginSetting(getClass(), "settings/marketplace-users"); //$NON-NLS-1$
         } catch (Exception e) {
             logger.debug("Error getting data access plugin settings", e);
         }
 
         if (roles == null) {
             // If it's true, we'll just check if the user is admin
             return SecurityHelper.getInstance().isPentahoAdministrator(PentahoSessionHolder.getSession());            
         }
 
         String roleArr[] = roles.split(","); //$NON-NLS-1$
 
         for (String role : roleArr) {
             for (GrantedAuthority userRole : auth.getAuthorities()) {
                 if (role != null && role.trim().equals(userRole.getAuthority())) {
                     return true;
                 }
             }
         }
         if (users != null) {
             String userArr[] = users.split(","); //$NON-NLS-1$
             for (String user : userArr) {
                 if (user != null && user.trim().equals(auth.getName())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     protected StatusMessage createMessage(String message, String code) {
         StatusMessage msg = new StatusMessage();
         msg.setCode(code);
         msg.setMessage(message);
         return msg;
     }
 
     protected String createJsonMessage(String message, String code) {
         StatusMessage msg = createMessage(message, code);
         JSONSerializer serializer = new JSONSerializer();
         String json = serializer.deepSerialize(msg);
         return json;
     }
 
     protected boolean withinParentVersion(PluginVersion pv) {
       // need to compare plugin version min and max parent with system version.
       // replace the version of the xml url path with the current release version:
       VersionInfo versionInfo = VersionHelper.getVersionInfo(PentahoSystem.class);
       String v = versionInfo.getVersionNumber();  
       return new VersionData(v).within(new VersionData(pv.getMinParentVersion()), new VersionData(pv.getMaxParentVersion()));
     }
 
     protected String getMarketplaceSiteContent() {
         IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
         String site = null;
         try {
             site = resLoader.getPluginSetting(getClass(), "settings/marketplace-site"); //$NON-NLS-1$
         } catch (Exception e) {
             logger.debug("Error getting data access plugin settings", e);
         }
 
         if (site == null || "".equals(site)) {
 
             site = "https://raw.github.com/pentaho/marketplace-metadata/master/marketplace.xml";
         }
 
         return HttpUtil.getURLContent(site);
     }
 
     /**
      * This method determines the installed version of a plugin.  If the plugin doesn't define a version correctly, it returns "Unknown".
      * This method makes the assumption that the plugin id also equals the plugin folder name.
      *
      * @param pluginId the plugin id related to the version.
      */
     protected String discoverInstalledVersion(Plugin plugin) {
 
         String versionPath = PentahoSystem.getApplicationContext().getSolutionPath("system/" + plugin.getId() + "/version.xml");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         FileReader reader = null;
         try {
             File file = new File(versionPath);
             if (!file.exists()) {
                 return "Unknown";
             }
             DocumentBuilder db = dbf.newDocumentBuilder();
             reader = new FileReader(versionPath);
             Document dom = db.parse(new InputSource(reader));
             NodeList versionElements = dom.getElementsByTagName("version");
             if (versionElements.getLength() >= 1) {
                 Element versionElement = (Element) versionElements.item(0);
 
                 plugin.setInstalledBuildId(versionElement.getAttribute("buildId"));
                 plugin.setInstalledBranch(versionElement.getAttribute("branch"));
                 plugin.setInstalledVersion(versionElement.getTextContent());
 
                 return versionElement.getTextContent();
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (Exception e) {
             }
         }
         return "Unknown";
     }
 
     protected List<Plugin> loadPluginsFromSite() {
         String content = getMarketplaceSiteContent();
         //Sometimes this call fails. Second attemp is always succesfull
         if (StringUtils.isEmpty(content))
           content = getMarketplaceSiteContent();
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(new InputSource(new StringReader(content)));
             NodeList plugins = dom.getElementsByTagName("market_entry");
             List<Plugin> pluginList = new ArrayList<Plugin>();
             for (int i = 0; i < plugins.getLength(); i++) {
                 Element element = (Element) plugins.item(i);
                 String type = getElementChildValue(element, "type");
                 if (!"Platform".equals(type)) {
                   continue;
                 }
                 Plugin plugin = new Plugin();
                 plugin.setId(getElementChildValue(element, "id"));
                 plugin.setName(getElementChildValue(element, "name"));
                 plugin.setDescription(getElementChildValue(element, "description"));
 
                 plugin.setCompany(getElementChildValue(element, "author"));
                 plugin.setCompanyUrl(getElementChildValue(element, "author_url"));
                 plugin.setCompanyLogo(getElementChildValue(element, "author_logo"));
                 plugin.setImg(getElementChildValue(element, "img"));
 
                 plugin.setSmallImg(getElementChildValue(element, "small_img"));
                 plugin.setLearnMoreUrl(getElementChildValue(element, "documentation_url"));
                 plugin.setInstallationNotes(getElementChildValue(element, "installation_notes"));
                 plugin.setLicense(getElementChildValue(element, "license"));
                 plugin.setDependencies(getElementChildValue(element, "dependencies"));
 
 
                 //NodeList availableVersions = element.getElementsByTagName("version");
                 NodeList availableVersions = (NodeList) xpath.evaluate("versions/version", element, XPathConstants.NODESET);
                 
                 if (availableVersions.getLength() > 0) {
                     List<PluginVersion> versions = new ArrayList<PluginVersion>();
                     for (int j = 0; j < availableVersions.getLength(); j++) {
                         Element versionElement = (Element) availableVersions.item(j);
                         PluginVersion pv = new PluginVersion(getElementChildValue(versionElement, "branch"),
                                 getElementChildValue(versionElement, "name"),
                                 getElementChildValue(versionElement, "version"),
                                 getElementChildValue(versionElement, "package_url"),
                                 getElementChildValue(versionElement, "samples_url"),
                                 getElementChildValue(versionElement, "description"),
                                 getElementChildValue(versionElement, "changelog"),
 
                                 getElementChildValue(versionElement, "build_id"),
                                 getElementChildValue(versionElement, "releaseDate"),                                
                                 getElementChildValue(versionElement, "min_parent_version"),
                                 getElementChildValue(versionElement, "max_parent_version"));
                         if (withinParentVersion(pv)) {
                           versions.add(pv);
                         }
                     
                     }
                     plugin.setVersions(versions);
                 }
                 
                 NodeList availableScreenshots = (NodeList) xpath.evaluate("screenshots/screenshot", element, XPathConstants.NODESET);
                 if (availableScreenshots.getLength() > 0) {
                     String[] screenshots = new String[availableScreenshots.getLength()];
                     
                     for (int j = 0; j < availableScreenshots.getLength(); j++) {
                         Element screenshotElement = (Element) availableScreenshots.item(j);
                         screenshots[j] = screenshotElement.getTextContent();
                     }
                     
                     plugin.setScreenshots(screenshots);
                 }
 
                 // only include plugins that have versions within this release 
                 if (plugin.getVersions() != null && plugin.getVersions().size() > 0) {
                   pluginList.add(plugin);
                 }
             }
             return pluginList;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     protected String getElementChildValue(Element element, String child) {
         NodeList list = element.getElementsByTagName(child);
         if (list.getLength() >= 1) {
             return list.item(0).getTextContent();
         } else {
             return null;
         }
     }
 }
