 /**
  * *****************************************************************************
  *
  * Copyright (c) 2012 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  * Winston Prakash
  *
  ******************************************************************************
  */
 package org.eclipse.hudson.init;
 
 import hudson.ProxyConfiguration;
 import hudson.Util;
 import hudson.XmlFile;
 import hudson.model.Hudson;
 import hudson.model.User;
 import hudson.security.Permission;
 import org.eclipse.hudson.WebAppController;
 import hudson.triggers.SafeTimerTask;
 import hudson.triggers.Trigger;
 import hudson.util.DaemonThreadFactory;
 import hudson.util.HudsonFailedToLoad;
 import hudson.util.HudsonIsLoading;
 import hudson.util.VersionNumber;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.*;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletResponse;
 import org.eclipse.hudson.init.AvailablePluginManager.AvailablePluginInfo;
 import org.eclipse.hudson.init.InstalledPluginManager.InstalledPluginInfo;
 import org.eclipse.hudson.security.HudsonSecurityEntitiesHolder;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.QueryParameter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides support for initial setup during first run. Gives opportunity to
  * Hudson Admin to 
  *  - Install mandatory and recommended plugins 
  *  - Update recommended and mandatory plugins suitable for current Hudson 
  *  - Setup Security 
  *  - Setup required tools
  *
  * @author Winston Prakash
  */
 final public class InitialSetup {
     
     private Logger logger = LoggerFactory.getLogger(InitialSetup.class);
 
     private File pluginsDir;
     private URL initPluginsJsonUrl;
     private ServletContext servletContext;
     private AvailablePluginManager availablePluginManager;
     private InstalledPluginManager installedPluginManager;
     private List<AvailablePluginInfo> installedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installablePlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> updatablePlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installedMandatoryPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installableMandatoryPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> updatableMandatoryPlugins = new ArrayList<AvailablePluginInfo>();
     private ProxyConfiguration proxyConfig;
     private ExecutorService installerService = Executors.newSingleThreadExecutor(
             new DaemonThreadFactory(new ThreadFactory() {
 
         @Override
         public Thread newThread(Runnable r) {
             Thread t = new Thread(r);
             t.setName("Update center installer thread");
             return t;
         }
     }));
     
     private HudsonSecurityManager hudsonSecurityManager;
     
     private XmlFile initSetupFile;
     
     private File hudsonHomeDir;
 
     public InitialSetup(File homeDir, ServletContext context) throws MalformedURLException, IOException {
         hudsonHomeDir = homeDir;
         pluginsDir = new File(homeDir, "plugins");
         servletContext = context;
         hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         proxyConfig = new ProxyConfiguration(homeDir);
         initPluginsJsonUrl = servletContext.getResource("/WEB-INF/init-plugins.json");
         availablePluginManager = new AvailablePluginManager(initPluginsJsonUrl);
         installedPluginManager = new InstalledPluginManager(pluginsDir);
         initSetupFile = new XmlFile(new File(homeDir, "initSetup.xml"));
         check();
     }
 
     // For testing
     InitialSetup(File dir, URL pluginsJsonUrl) throws MalformedURLException, IOException {
         pluginsDir = dir;
         initPluginsJsonUrl = pluginsJsonUrl;
         proxyConfig = new ProxyConfiguration(dir);
         availablePluginManager = new AvailablePluginManager(initPluginsJsonUrl);
         installedPluginManager = new InstalledPluginManager(pluginsDir);
         check();
     }
     
     public boolean needsInitSetup(){
         return !initSetupFile.exists();
     }
     
     public HudsonSecurityManager getHudsonSecurityManager() {
         return hudsonSecurityManager;
     }
 
     public ProxyConfiguration getProxyConfig() {
         return proxyConfig;
     }
 
     public List<AvailablePluginInfo> getInstalledPlugins() {
         return installedPlugins;
     }
 
     public List<AvailablePluginInfo> getInstallablePlugins() {
         return installablePlugins;
     }
 
     public List<AvailablePluginInfo> getUpdatablePlugins() {
         return updatablePlugins;
     }
 
     public List<AvailablePluginInfo> getInstallableMandatoryPlugins() {
         return installableMandatoryPlugins;
     }
 
     public List<AvailablePluginInfo> getInstalledMandatoryPlugins() {
         return installedMandatoryPlugins;
     }
 
     public List<AvailablePluginInfo> getUpdatableMandatoryPlugins() {
         return updatableMandatoryPlugins;
     }
 
     public InstalledPluginInfo getInstalled(AvailablePluginInfo plugin) {
         return installedPluginManager.getInstalledPlugin(plugin.getName());
     }
 
     // For test purpose
     Future<PluginInstallationJob> install(AvailablePluginInfo plugin) {
         return install(plugin, true);
     }
 
     public Future<PluginInstallationJob> install(AvailablePluginInfo plugin, boolean useProxy) {
         for (AvailablePluginInfo dep : getNeededDependencies(plugin)) {
             install(dep, useProxy);
         }
         return submitInstallationJob(plugin, useProxy);
     }
 
     public boolean isProxyNeeded() {
         try {
             // Try opening a URL and see if the proxy works fine
             proxyConfig.openUrl(new URL("http://www.google.com"));
         } catch (IOException ex) {
             logger.debug(ex.getLocalizedMessage());
             return true; 
         }
         return false;
     }
 
     public HttpResponse doinstallPlugin(@QueryParameter String pluginName) {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         AvailablePluginInfo plugin = availablePluginManager.getAvailablePlugin(pluginName);
         Future<PluginInstallationJob> installJob = install(plugin, false);
         try {
             PluginInstallationJob job = installJob.get();
             if (!job.getStatus()) {
                 return HttpResponses.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, job.getErrorMsg());
             }
         } catch (InterruptedException ex) {
             return HttpResponses.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
         } catch (ExecutionException ex) {
             return HttpResponses.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
         }
         reCheck();
         return HttpResponses.ok();
     }
 
     public HttpResponse doProxyConfigure(
             @QueryParameter("proxy.server") String server,
             @QueryParameter("proxy.port") String port,
             @QueryParameter("proxy.noProxyFor") String noProxyFor,
             @QueryParameter("proxy.userName") String userName,
             @QueryParameter("proxy.password") String password,
             @QueryParameter("proxy.authNeeded") String authNeeded) throws IOException {
 
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
 
         try {
             boolean proxySet = setProxy(server, port, noProxyFor, userName, password, authNeeded);
             if (proxySet){
                 proxyConfig.save();
             }
             // Try opening a URL and see if the proxy works fine
             proxyConfig.openUrl(new URL("http://www.google.com"));
 
         } catch (IOException ex) {
             return HttpResponses.error(HttpServletResponse.SC_BAD_REQUEST, ex);
         }
         return HttpResponses.ok();
     }
     
     public HttpResponse doCheckFinish() {
         if (!canFinish()) {
             return HttpResponses.error(HttpServletResponse.SC_BAD_REQUEST, "Mandatory Plugins need to be installed first");
         } else {
             try {
                 initSetupFile.write("Hudson 3.0 Initial Setup Done");
             } catch (IOException ex) {
                  logger.error(ex.getLocalizedMessage());
             }
             invokeHudson();
            
             return HttpResponses.ok();
         }
     }
     
     public void invokeHudson(){
         final WebAppController controller = WebAppController.get();
         
         controller.install(new HudsonIsLoading());
 
             new Thread("hudson initialization thread") {
                 @Override
                 public void run() {
                     try {
                         // Creating of the god object performs most of the booting muck
                         Hudson hudson = new Hudson(hudsonHomeDir, servletContext);
 
                         // once its done, hook up to stapler and things should be ready to go
                         controller.install(hudson);
 
                         // trigger the loading of changelogs in the background,
                         // but give the system 10 seconds so that the first page
                         // can be served quickly
                         Trigger.timer.schedule(new SafeTimerTask() {
                             public void doRun() {
                                 User.getUnknown().getBuilds();
                             }
                         }, 1000*10);
                     } catch (Error e) {
                         logger.error("Failed to initialize Hudson", e);
                         controller.install(new HudsonFailedToLoad(e));
                         throw e;
                     } catch (Exception e) {
                         logger.error("Failed to initialize Hudson", e);
                         controller.install(new HudsonFailedToLoad(e));
                     }
                 }
             }.start();
     }
     
     private boolean canFinish(){
         check();
         return (getInstallableMandatoryPlugins().size() == 0) && (getUpdatableMandatoryPlugins().size() == 0);
     }
 
     private boolean setProxy(String server, String port, String noProxyFor,
             String userName, String password, String authNeeded) throws IOException {
         server = Util.fixEmptyAndTrim(server);
 
         if ((server != null) && !"".equals(server)) {
             // If port is not specified assume it is port 80 (usual default for HTTP port)
             int portNumber = 80;
             if (!"".equals(Util.fixNull(port))) {
                 portNumber = Integer.parseInt(Util.fixNull(port));
             }
 
             boolean proxyAuthNeeded = "on".equals(Util.fixNull(authNeeded));
             if (!proxyAuthNeeded) {
                 userName = "";
                 password = "";
             }
 
             proxyConfig.configure(server, portNumber, Util.fixEmptyAndTrim(noProxyFor),
                     Util.fixEmptyAndTrim(userName), Util.fixEmptyAndTrim(password), "on".equals(Util.fixNull(authNeeded)));
             return true;
 
         }else{
            proxyConfig.getXmlFile().delete();
            proxyConfig.name = null;
            return true;
         }
     }
 
     private Future<PluginInstallationJob> submitInstallationJob(AvailablePluginInfo plugin, boolean useProxy) {
         PluginInstallationJob job = new PluginInstallationJob(plugin, pluginsDir, useProxy);
         return installerService.submit(job, job);
     }
 
     private boolean isNewerThan(String availableVersion, String installedVersion) {
         try {
             return new VersionNumber(installedVersion).compareTo(new VersionNumber(availableVersion)) < 0;
         } catch (IllegalArgumentException e) {
             // couldn't parse as the version number.
             return false;
         }
     }
 
     void reCheck() {
         installedPlugins.clear();
         installablePlugins.clear();
         updatablePlugins.clear();
         installableMandatoryPlugins.clear();
         installedMandatoryPlugins.clear();
         updatableMandatoryPlugins.clear();
         installedPluginManager.loadInstalledPlugins();
         check();
     }
 
     private void check() {
         Set<String> installedPluginNames = installedPluginManager.getInstalledPluginNames();
         Set<String> availablePluginNames = availablePluginManager.getAvailablePluginNames();
         for (String pluginName : availablePluginNames) {
             AvailablePluginInfo availablePlugin = availablePluginManager.getAvailablePlugin(pluginName);
             if (installedPluginNames.contains(pluginName)) {
                 //Installed
                 InstalledPluginInfo installedPlugin = installedPluginManager.getInstalledPlugin(pluginName);
                 if (availablePlugin.isMandatory()) {
                     //Installed Mandatory Plugin
                     if (isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion())) {
                         //Installed Mandatory Plugin update needed
                         updatableMandatoryPlugins.add(availablePlugin);
                     } else {
                         //Installed Mandatory Plugin. No updates available
                         installedMandatoryPlugins.add(availablePlugin);
                     }
                 } else {
                     if (isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion())) {
                         //Installed regular Plugin update needed
                         updatablePlugins.add(availablePlugin);
                     } else {
                         //Installed regular Plugin. No updates available
                         installedPlugins.add(availablePlugin);
                     }
                 }
 
             } else {
                 //Not installed
                 if (availablePlugin.isMandatory()) {
                     //Mandatory Plugin. Need to be installed
                     installableMandatoryPlugins.add(availablePlugin);
                 } else {
                     //Regular Plugin. Available for installation
                     installablePlugins.add(availablePlugin);
                 }
             }
         }
     }
 
     private List<AvailablePluginInfo> getNeededDependencies(AvailablePluginInfo pluginInfo) {
         List<AvailablePluginInfo> deps = new ArrayList<AvailablePluginInfo>();

        for (Map.Entry<String, String> e : pluginInfo.getDependencies().entrySet()) {
            AvailablePluginInfo depPlugin = availablePluginManager.getAvailablePlugin(e.getKey());
            VersionNumber requiredVersion = new VersionNumber(e.getValue());

            // Is the plugin installed already? If not, add it.
            InstalledPluginInfo current = installedPluginManager.getInstalledPlugin(depPlugin.getName());

            if (current == null) {
                deps.add(depPlugin);
            } else if (current.isOlderThan(requiredVersion)) {
                deps.add(depPlugin);
             }
         }
 
         return deps;
     }
 }
