 /*
  * This file is part of VIMCPlugin.
  *
  * Copyright © 2013-2014 Visual Illusions Entertainment
  *
  * VIMCPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * VIMCPlugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with VIMCPlugin.
  * If not, see http://www.gnu.org/licenses/lgpl.html.
  */
 package net.visualillusionsent.minecraft.plugin.spout;
 
 import net.visualillusionsent.minecraft.plugin.VisualIllusionsMinecraftPlugin;
 import net.visualillusionsent.minecraft.plugin.VisualIllusionsPlugin;
 import net.visualillusionsent.utils.JarUtils;
 import net.visualillusionsent.utils.ProgramChecker;
 import net.visualillusionsent.utils.ProgramStatus;
 import org.spout.api.plugin.Plugin;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.StringTokenizer;
 import java.util.jar.Manifest;
 import java.util.logging.Logger;
 
 /**
  * Visual Illusions Spout Plugin extension
  *
  * @author Jason (darkdiplomat)
  */
 public abstract class VisualIllusionsSpoutPlugin extends Plugin implements VisualIllusionsPlugin {
 
     private final ProgramChecker pChecker;
     private final Manifest manifest;
     private final long[] versionArray;
     protected final boolean debug;
 
     public VisualIllusionsSpoutPlugin() {
         this.manifest = readManifest();
         this.versionArray = createVersionArray();
         this.debug = Boolean.valueOf(System.getProperty("debug.".concat(getPluginName().toLowerCase()), "false"));
         this.pChecker = new ProgramChecker(getPluginName(), getVersionArray(), getStatusURL(), getStatus());
     }
 
     @Override
     public void onEnable() {
         VisualIllusionsMinecraftPlugin.checkVersion(this);
         VisualIllusionsMinecraftPlugin.checkStatus(this);
     }
 
     @Override
     public final String getPluginName() {
         if (manifest != null) {
            return manifest.getMainAttributes().getValue("Specification-Title");
         }
        return "UnknownVICanaryPlugin";
     }
 
     @Override
     public final String getBuild() {
         if (manifest != null) {
            return manifest.getMainAttributes().getValue("Implementation-Version");
         }
         return "dev";
     }
 
     @Override
     public final String getBuildTime() {
         if (manifest != null) {
             return manifest.getMainAttributes().getValue("Build-Time");
         }
         return "19700101-0000";
     }
 
     @Override
     public final ProgramChecker getProgramChecker() {
         return pChecker;
     }
 
     @Override
     public final String getWikiURL() {
         if (manifest != null) {
             return manifest.getMainAttributes().getValue("Wiki-URL");
         }
         return "http://visualillusionsent.net";
     }
 
     @Override
     public final String getIssuesURL() {
         if (manifest != null) {
             return manifest.getMainAttributes().getValue("Issues-URL");
         }
         return "https://github.com/Visual-Illusions/";
     }
 
     @Override
     public final String getDevelopers() {
         if (manifest != null) {
             return manifest.getMainAttributes().getValue("Developers");
         }
         return "darkdiplomat";
     }
 
     @Override
     public final String getCopyYear() {
         if (manifest != null) {
             return manifest.getMainAttributes().getValue("Copyright");
         }
         return "2013-" + Calendar.getInstance().get(Calendar.YEAR);
     }
 
     @Override
     public final String getPluginVersion() {
         if (manifest != null) {
            return manifest.getMainAttributes().getValue("Specification-Version");
         }
         return "0.0.1-SNAPSHOT";
     }
 
     @Override
     public final ProgramStatus getStatus() {
         return getPluginVersion().contains("-SNAPSHOT") ? ProgramStatus.SNAPSHOT : ProgramStatus.STABLE;
     }
 
     @Override
     public final long[] getVersionArray() {
         return versionArray;
     }
 
     @Override
     public Logger getPluginLogger() {
         return logger;
     }
 
     private Manifest readManifest() {
         try {
             return JarUtils.getManifest(this.getClass());
         }
         catch (IOException ioex) {
         }
         return null;
     }
 
     private long[] createVersionArray() {
         StringTokenizer tokenizer = new StringTokenizer(getPluginVersion().replace("-SNAPSHOT", ""), ".");
         long[] mmr = new long[tokenizer.countTokens()];
         for (int index = 0; index < mmr.length; index++) {
             mmr[index] = Long.parseLong(tokenizer.nextToken());
         }
         return mmr;
     }
 
     private URL getStatusURL() {
         if (manifest != null) {
             try {
                 return new URL(manifest.getMainAttributes().getValue("Status-URL"));
             }
             catch (MalformedURLException murlex) {
 
             }
         }
         return null;
     }
 }
