 /*
  * This file is part of FruitTrees.
  *
  * Copyright © 2013 Visual Illusions Entertainment
  *
  * FruitTrees is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * FruitTrees is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with FruitTrees.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.minecraft.server.mod.canary.plugin.fruittrees;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 import net.canarymod.Canary;
 import net.canarymod.api.world.World;
 import net.canarymod.config.Configuration;
 import net.canarymod.logger.CanaryLevel;
 import net.canarymod.plugin.Plugin;
 import net.visualillusionsent.fruittrees.FruitTree;
 import net.visualillusionsent.fruittrees.FruitTrees;
 import net.visualillusionsent.fruittrees.FruitTreesConfigurations;
 import net.visualillusionsent.fruittrees.TreeTracker;
 import net.visualillusionsent.fruittrees.TreeType;
 import net.visualillusionsent.fruittrees.TreeWorld;
 import net.visualillusionsent.fruittrees.data.MySQLTreeStorage;
 import net.visualillusionsent.fruittrees.data.TreeStorage;
 import net.visualillusionsent.fruittrees.data.XMLTreeStorage;
 import net.visualillusionsent.minecraft.server.mod.canary.plugin.fruittrees.commands.FruitTreesCommands;
 import net.visualillusionsent.utils.ProgramStatus;
 import net.visualillusionsent.utils.PropertiesFile;
 import net.visualillusionsent.utils.VersionChecker;
 
 public class CanaryFruitTrees extends Plugin implements FruitTrees {
 
     private final CanaryTreeWorldCache world_cache = new CanaryTreeWorldCache(this);
     private TreeStorage storage;
     private FruitTreesConfigurations ft_cfg;
     private final VersionChecker vc;
     private float version;
     private short build;
     private String buildTime;
     private ProgramStatus status;
     private static CanaryFruitTrees $;
 
     public CanaryFruitTrees() {
         readManifest();
         vc = new VersionChecker(getName(), String.valueOf(version), String.valueOf(build), "http://visualillusionsent.net/minecraft/plugins/", status, false);
         getLogman().setLevel(Level.ALL);
     }
 
     @Override
     public boolean enable() {
         $ = this;
         checkStatus();
         checkVersion();
         ft_cfg = new FruitTreesConfigurations(this);
         SeedGen.genAll();
         if (ft_cfg.isMySQL()) {
             try {
                 storage = new MySQLTreeStorage(this);
             }
             catch (SQLException ex) {
                 getLogman().log(Level.SEVERE, "Failed to initialize MySQL Data storage...", ex);
                 disable();
                 return false;
             }
         }
         else {
             storage = new XMLTreeStorage(this);
         }
         for (World world : Canary.getServer().getWorldManager().getAllWorlds()) {
             world_cache.setExistingWorlds(new CanaryTreeWorld(this, world, world.getFqName()));
             storage.loadTreesForWorld(world_cache.getTreeWorld(world.getFqName()));
         }
         new CanaryFruitTreesListener(this);
         new FruitTreesCommands(this);
         return true;
     }
 
     @Override
     public void disable() {
         $ = null;
         SeedGen.clearSeeds();
     }
 
     public TreeWorld getWorldForName(String name) {
         return world_cache.getTreeWorld(name);
     }
 
     protected void addLoadedWorld(World world) {
         TreeWorld tree_world = world_cache.getTreeWorld(world.getFqName());
         if (tree_world == null) {
             world_cache.setExistingWorlds(new CanaryTreeWorld(this, world, world.getFqName()));
             storage.loadTreesForWorld(world_cache.getTreeWorld(world.getFqName()));
         }
         else {
             tree_world.isLoaded();
         }
     }
 
     protected final void worldUnload(TreeWorld tree_world) {
         for (FruitTree tree : TreeTracker.getTreesInWorld(tree_world)) {
             storage.storeTree(tree);
         }
     }
 
     public static final CanaryFruitTrees instance() {
         return $;
     }
 
     public final void debug(String msg) {
         if (ft_cfg.debug()) {
             getLogman().log(CanaryLevel.PLUGIN_DEBUG, msg);
         }
     }
 
     public final void info(String msg) {
         getLogman().info(msg);
     }
 
     public final void warning(String msg) {
         getLogman().warning(msg);
     }
 
     public final void severe(String msg) {
         getLogman().severe(msg);
     }
 
     public final void severe(String msg, Throwable thrown) {
         getLogman().log(Level.SEVERE, msg, thrown);
     }
 
     public final PropertiesFile getConfig() {
        return Configuration.getPluginConfig(this);
     }
 
     public final FruitTreesConfigurations getFruitTreesConfig() {
         return this.ft_cfg;
     }
 
     public final TreeStorage getStorage() {
         return storage;
     }
 
     private final Manifest getManifest() throws Exception {
         Manifest toRet = null;
         Exception ex = null;
         JarFile jar = null;
         try {
             jar = new JarFile(getJarPath());
             toRet = jar.getManifest();
         }
         catch (Exception e) {
             ex = e;
         }
         finally {
             if (jar != null) {
                 try {
                     jar.close();
                 }
                 catch (IOException e) {}
             }
             if (ex != null) {
                 throw ex;
             }
         }
         return toRet;
     }
 
     private final void readManifest() {
         try {
             Manifest manifest = getManifest();
             Attributes mainAttribs = manifest.getMainAttributes();
             version = Float.parseFloat(mainAttribs.getValue("Version").replace("-SNAPSHOT", ""));
             build = Short.parseShort(mainAttribs.getValue("Build"));
             buildTime = mainAttribs.getValue("Build-Time");
             try {
                 status = ProgramStatus.valueOf(mainAttribs.getValue("ProgramStatus"));
             }
             catch (IllegalArgumentException iaex) {
                 status = ProgramStatus.UNKNOWN;
             }
         }
         catch (Exception ex) {
             version = -1.0F;
             build = -1;
             buildTime = "19700101-0000";
         }
     }
 
     private final void checkStatus() {
         if (status == ProgramStatus.UNKNOWN) {
             getLogman().severe(String.format("%s has declared itself as an 'UNKNOWN STATUS' build. Use is not advised and could cause damage to your system!", getName()));
         }
         else if (status == ProgramStatus.ALPHA) {
             getLogman().warning(String.format("%s has declared itself as a 'ALPHA' build. Production use is not advised!", getName()));
         }
         else if (status == ProgramStatus.BETA) {
             getLogman().warning(String.format("%s has declared itself as a 'BETA' build. Production use is not advised!", getName()));
         }
         else if (status == ProgramStatus.RELEASE_CANDIDATE) {
             getLogman().info(String.format("%s has declared itself as a 'Release Candidate' build. Expect some bugs.", getName()));
         }
     }
 
     private final void checkVersion() {
         Boolean islatest = vc.isLatest();
         if (islatest == null) {
             getLogman().warning("VersionCheckerError: " + vc.getErrorMessage());
         }
         else if (!vc.isLatest()) {
             getLogman().warning(vc.getUpdateAvailibleMessage());
             getLogman().warning(String.format("You can view update info @ http://wiki.visualillusionsent.net/%s#ChangeLog", getName()));
         }
     }
 
     public final float getRawVersion() {
         return version;
     }
 
     public final short getBuildNumber() {
         return build;
     }
 
     public final String getBuildTime() {
         return buildTime;
     }
 
     public final VersionChecker getVersionChecker() {
         return vc;
     }
 
     public boolean checkEnabled(TreeType type) {
         return ft_cfg.checkEnabled(type);
     }
 }
