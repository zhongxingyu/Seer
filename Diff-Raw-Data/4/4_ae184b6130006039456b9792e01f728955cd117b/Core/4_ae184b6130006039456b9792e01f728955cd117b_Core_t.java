 /*
  * An easily extendable chat bot for any chat service.
  * Copyright (C) 2013 bogeymanEST
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.superfuntime.chatty;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.logging.log4j.Level;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.superfuntime.chatty.chat.ChatListener;
 import org.superfuntime.chatty.events.EventManager;
 import org.superfuntime.chatty.utils.BotClassInfo;
 import org.superfuntime.chatty.utils.LoggingStream;
 import org.superfuntime.chatty.utils.PluginInfo;
 import org.superfuntime.chatty.yml.YAMLNode;
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.*;
 import java.util.jar.JarFile;
 
 /**
  * Hello world!
  */
 public class Core {
     /**
      * The ID of the Skype user
      */
     private static final Logger logger = LogManager.getLogger();
     private static final List<Bot> BOTS = new ArrayList<Bot>();
     private static final Map<String, BotClassInfo> BOT_INFO = new HashMap<String, BotClassInfo>();
     private static final Map<String, PluginInfo> PLUGIN_INFO = new HashMap<String, PluginInfo>();
     private static final File botsDirecotry = new File("bots");
     private static final File pluginsDirecotry = new File("plugins");
 
     /**
      * Gets the number of running bots.
      *
      * @return The number of running bots.
      */
     public static int getBotCount() {
         return BOTS.size();
     }
 
     /**
      * Gets the number of running plugins.
      *
      * @return The number of running plugins.
      */
     public static int getPluginCount() {
         return PLUGIN_INFO.size();
     }
 
     public static void main(String[] args) {
         botsDirecotry.mkdir();
         pluginsDirecotry.mkdir();
         logger.info("Setting up");
         EventManager.addListener(new ChatListener());
         System.setOut(new PrintStream(new LoggingStream(LogManager.getLogger("STDOUT"), Level.INFO)));
         System.setErr(new PrintStream(new LoggingStream(LogManager.getLogger("STDERR"), Level.ERROR)));
         loadBots();
         startBots();
         if (BOTS.size() == 0) {
             logger.error("No bots started! Stopping...");
             return;
         }
         startPlugins();
         logger.info("Setup finished");
     }
 
     @SuppressWarnings("unchecked")
     private static void startPlugins() {
         logger.info("Loading available plugins");
         final Collection<File> botJars = FileUtils.listFiles(pluginsDirecotry, new String[]{"jar"}, false);
         for (File botJar : botJars) {
             try {
                 JarFile jar = new JarFile(botJar);
                 InputStream is = jar.getInputStream(jar.getEntry("plugin.yml"));
                 YAMLNode n = new YAMLNode(new Yaml().loadAs(is, Map.class), true);
                 String mainClass = n.getString("mainClass");
                 String name = n.getString("name");
                 URLClassLoader loader = new URLClassLoader(new URL[]{botJar.toURI().toURL()},
                                                            Core.class.getClassLoader());
                 Plugin plugin = loader.loadClass(mainClass).asSubclass(Plugin.class).newInstance();
                 plugin.start();
                 PLUGIN_INFO.put(name, new PluginInfo(name, plugin, n));
                 logger.info("Loaded plugin '{}'", name);
             } catch (Exception e) {
                 logger.error("Failed to load plugin from {}", botJar.getName());
                 e.printStackTrace();
             }
         }
         logger.info("Loaded {} plugin(s)", PLUGIN_INFO.size());
     }
 
     private static void startBots() {
         logger.info("Starting bots");
         List<YAMLNode> nodes = ChatBot.getSettingsManager().getMainConfig().getNodeList("bots", null);
         for (YAMLNode node : nodes) {
             final String botType = node.getString("type");
             logger.info("Starting {} bot", botType);
             try {
                 BotClassInfo botClassInfo = BOT_INFO.get(botType);
                if (botClassInfo == null) {
                    logger.error("Cannot start bot '{}': the specified bot doesn't exist!", botType);
                    continue;
                }
                 Bot bot = botClassInfo.getBotClass().newInstance();
                 bot.info = botClassInfo;
                 bot.start();
                 BOTS.add(bot);
             } catch (Exception e) {
                 logger.error("Failed to start bot '{}'", botType);
                 e.printStackTrace();
             }
         }
         logger.info("Started {} bot(s)", BOTS.size());
     }
 
     @SuppressWarnings("unchecked")
     private static void loadBots() {
         logger.info("Loading available bots");
         final Collection<File> botJars = FileUtils.listFiles(botsDirecotry, new String[]{"jar"}, false);
         for (File botJar : botJars) {
             try {
                 JarFile jar = new JarFile(botJar);
                 InputStream is = jar.getInputStream(jar.getEntry("bot.yml"));
                 YAMLNode n = new YAMLNode(new Yaml().loadAs(is, Map.class), true);
                 String mainClass = n.getString("mainClass");
                 String type = n.getString("type");
                 URLClassLoader loader = new URLClassLoader(new URL[]{botJar.toURI().toURL()},
                                                            Core.class.getClassLoader());
                 BOT_INFO.put(type, new BotClassInfo(loader.loadClass(mainClass).asSubclass(Bot.class), n));
                 logger.info("Loaded bot '{}'", type);
             } catch (Exception e) {
                 logger.error("Failed to load bot from {}", botJar.getName());
                 e.printStackTrace();
             }
         }
         logger.info("Loaded {} bot(s)", BOT_INFO.size());
     }
 }
