 /*******************************************************************************
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
  *  Winston Prakash
  *
  ******************************************************************************/
 
 package org.eclipse.hudson.init;
 
 import hudson.util.VersionNumber;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility that provides information about the plugins 
  * that are already installed during Initial Setup
  * 
  * @author Winston Prakash
  */
 public final class InstalledPluginManager {
     
     private Logger logger = LoggerFactory.getLogger(InstalledPluginManager.class);
 
     private Map<String, InstalledPluginInfo> installedPluginInfos = new HashMap<String, InstalledPluginInfo>();
     private File pluginsDir;
 
     public InstalledPluginManager(File dir) {
         pluginsDir = dir;
         loadInstalledPlugins();
     }
 
     public File getPluginsDir() {
         return pluginsDir;
     }
 
     public Set<String> getInstalledPluginNames() {
         return installedPluginInfos.keySet();
     }
 
     public InstalledPluginInfo getInstalledPlugin(String name) {
         return installedPluginInfos.get(name);
     }
     
     void loadInstalledPlugins() {
         File[] hpiArchives = pluginsDir.listFiles(new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith("hpi");
             }
         });
         
         if ((hpiArchives != null) && (hpiArchives.length > 0)) {
 
             for (File archive : hpiArchives) {
                 try {
                     InstalledPluginInfo installedPluginInfo = new InstalledPluginInfo(archive);
                     installedPluginInfos.put(installedPluginInfo.getShortName(), installedPluginInfo);
                 } catch (IOException exc) {
                     logger.warn("Failed to load plugin ", exc);
                 }
             }
         }
     }
 
     public final static class InstalledPluginInfo {
 
         private File hpiArchive;
         private String shortName;
         private String wikiUrl;
         private String longName;
         private String version;
 
         public InstalledPluginInfo(File archive) throws IOException {
             hpiArchive = archive;
             parseManifest();
         }
 
         void parseManifest() throws IOException {
             JarFile jarfile = new JarFile(hpiArchive);
             Manifest manifest = jarfile.getManifest();
 
             shortName = manifest.getMainAttributes().getValue("Short-Name");
             if (shortName == null) {
                 manifest.getMainAttributes().getValue("Extension-Name");
             }
             if (shortName == null) {
                 shortName = hpiArchive.getName();
                 int idx = shortName.lastIndexOf('.');
                 if (idx >= 0) {
                     shortName = shortName.substring(0, idx);
                 }
             }
 
             wikiUrl = manifest.getMainAttributes().getValue("Url");
 
             longName = manifest.getMainAttributes().getValue("Long-Name");
             if (longName == null) {
                 longName = shortName;
             }
 
             version = manifest.getMainAttributes().getValue("Plugin-Version");
             if (version == null) {
                 version = manifest.getMainAttributes().getValue("Implementation-Version");
             }
        }
        
        public boolean isDisabled() {
            File disabledMarker = new File(hpiArchive.getPath() + ".disabled");
            return disabledMarker.exists();
         }
 
         public VersionNumber getVersionNumber() {
             return new VersionNumber(version);
         }
 
         public boolean isOlderThan(VersionNumber v) {
             try {
                 return getVersionNumber().compareTo(v) < 0;
             } catch (IllegalArgumentException e) {
                 // Assume older
                 return true;
             }
         }
 
         public String getLongName() {
             return longName;
         }
 
         public String getShortName() {
             return shortName;
         }
 
         public String getVersion() {
             return version;
         }
 
         public String getWikiUrl() {
             return wikiUrl;
         }
 
         @Override
         public String toString() {
             return "[Plugin Name:" + shortName + " Long Name:" + longName + " Version:" + version + " Wiki:" + wikiUrl + "]";
         }
     }
 }
