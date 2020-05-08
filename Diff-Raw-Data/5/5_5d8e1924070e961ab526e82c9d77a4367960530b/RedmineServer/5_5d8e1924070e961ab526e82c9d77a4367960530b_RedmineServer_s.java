 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.its.redmine;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jabox.apis.embedded.AbstractEmbeddedServer;
 import org.jabox.environment.Environment;
 import org.jabox.utils.DownloadHelper;
 import org.jabox.utils.Unzip;
 
 public class RedmineServer extends AbstractEmbeddedServer {
     private static final long serialVersionUID = 9207781259797681188L;
 
    private final String version = "2.1.4";
 
     public List<String> plugins = getDefaultPlugins();
 
     private void injectPlugins() {
         for (String plugin : plugins) {
             injectPlugin(plugin);
         }
     }
 
     private void injectPlugin(final String plugin) {
         // File dest = new File(Environment.getHudsonHomeDir(), resource);
 
         String type = plugin.split(";")[0];
         String url = plugin.split(";")[1];
         String directory = plugin.split(";")[2];
 
         if ("zip".equals(type)) {
             File outputFile =
                 new File(Environment.getDownloadsDir(), directory + ".zip");
             File file = DownloadHelper.downloadFile(url, outputFile);
             try {
                 Unzip.unzip(file, getRedminePluginDir());
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private static File getRedminePluginDir() {
         return new File(Environment.getRedmineHomeDir(), "vendor/plugins");
     }
 
     @Override
     public String getServerName() {
         return "redmine";
     }
 
     @Override
     public String getWarPath() {
         File downloadsDir = Environment.getDownloadsDir();
 
        // Download the nexus.war
         File war = new File(downloadsDir, "redmine.war");
         String url =
             "http://www.jabox.org/repository/releases/org/redmine/redmine/"
                 + version + "/redmine-" + version + ".war";
         war = DownloadHelper.downloadFile(url, war);
         injectPlugins();
         return war.getAbsolutePath();
     }
 
     /**
      * @return
      */
     private List<String> getDefaultPlugins() {
         List<String> pl = new ArrayList<String>();
         // pl.add("zip;http://dev.holgerjust.de/attachments/download/41/redmine_opensearch_0.1.zip;redmine_opensearch");
         // pl.add("zip;https://github.com/thumbtack-technology/redmine-issue-hot-buttons/zipball/0.4.1;issue_hot_buttons_plugin");
         return pl;
     }
 
 }
