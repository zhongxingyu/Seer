 package me.Miny.Paypassage.update;
 
 import java.io.*;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.*;
 import java.util.logging.Level;
 import me.Miny.Paypassage.Paypassage;
 import me.Miny.Paypassage.logger.LoggerUtility;
 import org.bukkit.command.Command;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.SimpleCommandMap;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.*;
 
 public class Update {
 
     /**
      *
      */
     public static final long serialVersionUID = 1L;
     transient int i; // transient: not stored
     public String search = "version";
     private Paypassage plugin;
     private boolean updateaviable = false;
     private float newversion;
     
     public Update(Paypassage up) {
         plugin = up;
     }
 
     public float getNewversion() {
         if(newversion == 0){
             newversion = plugin.getVersion();
         }
         return newversion;
     }
     
     public void install() {
         try {
            if (plugin.getConfigHandler().getConfig().getBoolean("internet")) {
                 try {
                     String path = plugin.getDataFolder().getParent();
                     if (download(path)) {
                         plugin.getLoggerUtility().log("Downloaded new Version!", LoggerUtility.Level.WARNING);
                         plugin.getLoggerUtility().log("Paypassage will be updated on the next restart!", LoggerUtility.Level.WARNING);
                     } else {
                         plugin.getLoggerUtility().log(" Cant download new Version!", LoggerUtility.Level.WARNING);
                     }
                 } catch (Exception e) {
                     plugin.getLoggerUtility().log("Error on downloading new Version!", LoggerUtility.Level.ERROR);
                     plugin.getReportHandler().report(3313, "Error on downloading new Version", e.getMessage(), "Paypassage", e);
                     e.printStackTrace();
                     plugin.getLoggerUtility().log("Uncatched Exeption!", LoggerUtility.Level.WARNING);
                 }
             }
             if (plugin.getConfigHandler().getConfig().getBoolean("installondownload")) {
                 plugin.getLoggerUtility().log("Found Update! Installing now because of 'installondownload = true', please wait!", LoggerUtility.Level.WARNING);
             }
             try {
                 plugin.getPluginManager().unloadPlugin("Paypassage");
             } catch (NoSuchFieldException ex) {
                 plugin.getLoggerUtility().log("Error on installing! Please check the log!", LoggerUtility.Level.ERROR);
                 java.util.logging.Logger.getLogger(Paypassage.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 plugin.getLoggerUtility().log("Error on installing! Please check the log!", LoggerUtility.Level.ERROR);
                 java.util.logging.Logger.getLogger(Paypassage.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 plugin.getPluginManager().loadPlugin("Paypassage");
             } catch (InvalidPluginException ex) {
                 plugin.getLoggerUtility().log("Error on loading after installing! Please check the log!", LoggerUtility.Level.ERROR);
                 java.util.logging.Logger.getLogger(Paypassage.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InvalidDescriptionException ex) {
                 plugin.getLoggerUtility().log("Error on loading after installing! Please check the log!", LoggerUtility.Level.ERROR);
                 java.util.logging.Logger.getLogger(Paypassage.class.getName()).log(Level.SEVERE, null, ex);
             }
             plugin.getLoggerUtility().log("Installing finished!", LoggerUtility.Level.INFO);
         } catch (Exception w) {
             w.printStackTrace();
             plugin.getLoggerUtility().log("Uncatched Exeption!", LoggerUtility.Level.ERROR);
             plugin.getReportHandler().report(3314, "Uncatched Exeption on installing", w.getMessage(), "Paypassage", w);
         }
     }
     
     public void startUpdateTimer(){
         plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
                 @Override
                 public void run() {
                    if (plugin.getConfigHandler().getConfig().getBoolean("internet")) {
                         try {
                             plugin.getLoggerUtility().log("Searching update for Paypassage!", LoggerUtility.Level.DEBUG);
                             newversion = checkUpdate();
                             if (newversion == -1.0F) {
                                 newversion = plugin.getVersion();
                             }
                             plugin.getLoggerUtility().log("installed Paypassage version: " + plugin.getVersion() + ", latest version: " + newversion, LoggerUtility.Level.DEBUG);
                             if (newversion > plugin.getVersion()) {
                                 plugin.getLoggerUtility().log("New version: " + newversion + " found!", LoggerUtility.Level.WARNING);
                                 plugin.getLoggerUtility().log("******************************************", LoggerUtility.Level.WARNING);
                                 plugin.getLoggerUtility().log("*********** Please update!!!! ************", LoggerUtility.Level.WARNING);
                                 plugin.getLoggerUtility().log("* http://dev.bukkit.org/Paypassage *******", LoggerUtility.Level.WARNING);
                                 plugin.getLoggerUtility().log("******************************************", LoggerUtility.Level.WARNING);
                                 updateaviable = true;
                                 if (plugin.getConfig().getBoolean("installondownload")) {
                                     install();
                                 }
                             } else {
                                 plugin.getLoggerUtility().log("No update found!", LoggerUtility.Level.DEBUG);
                             }
                         } catch (Exception e) {
                             plugin.getLoggerUtility().log("Error on doing update check! Message: " + e.getMessage(), LoggerUtility.Level.ERROR);
                             plugin.getLoggerUtility().log("may the mainserver is down!", LoggerUtility.Level.ERROR);
                             plugin.getReportHandler().report(335, "Checking for update failed", e.getMessage(), "Paypassage", e);
                         }
                     }
                 }
             }, 400L, 50000L);
     }
 
     public boolean isUpdateaviable() {
         return updateaviable;
     }
     
 //    private void UpdateAvailable() {
//        if (plugin.getConfigHandler().getConfig().getBoolean("internet")) {
 //            try {
 //                if (checkUpdate() > plugin.getVersion()) {
 //                    updateaviable = true;
 //                }
 //                if (updateaviable) {
 //                    updateaviable = true;
 //                } else {
 //                    updateaviable = false;
 //                }
 //            } catch (Exception e) {
 //                plugin.getLoggerUtility().log("Error checking for new version! Message: " + e.getMessage(), LoggerUtility.Level.ERROR);
 //                plugin.getReportHandler().report(3311, "Error checking for new version", e.getMessage(), "Paypassage", e);
 //                plugin.getLoggerUtility().log("May the mainserver is down!", LoggerUtility.Level.ERROR);
 //                plugin.getLoggerUtility().log("Uncatched Exeption!", LoggerUtility.Level.ERROR);
 //            }
 //        }
 //    }
 
     public float checkUpdate() {
         String link = getLink();
         if (link.equals("Error")) {
             plugin.getLoggerUtility().log("link == Error", LoggerUtility.Level.DEBUG);
             return (float) -1;
         }
         plugin.getLoggerUtility().log("link: " + link, LoggerUtility.Level.DEBUG);
         String newVersion = "-1";
         try {
             newVersion = link.substring(link.lastIndexOf(search), link.lastIndexOf("/")).replace("-", ".");
             newVersion = newVersion.substring(search.length());
         } catch (StringIndexOutOfBoundsException e) {
             plugin.getLoggerUtility().log("Update checking temporaly failed. May dev.bukkit.org is down or is in trouble." + newVersion, LoggerUtility.Level.WARNING);
         }
         plugin.getLoggerUtility().log("current version: " + newVersion, LoggerUtility.Level.DEBUG);
         return Float.parseFloat(newVersion);
     }
 
     public String getLink() {
         String link = "";
         try {
             URL rss = new URL("http://dev.bukkit.org/server-mods/paypassage/files.rss");
             ReadableByteChannel rbc = Channels.newChannel(rss.openStream());
             plugin.getDataFolder().mkdir();
             File outputFile = new File(plugin.getDataFolder(), plugin.getDescription().getName() + ".tmp");
             outputFile.createNewFile();
             FileOutputStream fos = new FileOutputStream(outputFile);
             fos.getChannel().transferFrom(rbc, 0, 1 << 24);
             fos.close();
             Scanner s = new Scanner(outputFile);
             int line = 0;
             while (s.hasNextLine()) {
                 link = s.nextLine();
                 if (link.contains("<link>")) {
                     line++;
                 }
                 if (line == 2) {
                     link = link.substring(link.indexOf(">") + 1);
                     link = link.substring(0, link.indexOf("<"));
                     break;
                 }
             }
             s.close();
             outputFile.delete();
             return link;
         } catch (IOException e) {
             e.printStackTrace();
         }
         return "Error";
     }
 
     public boolean download(String folder) {
         try {
             downloadJar(downloadSite(getLink()), folder);
         } catch (Exception e) {
             return false;
         }
         return true;
     }
 
     public String downloadSite(String url) {
         String link = "";
         try {
             URL website = new URL(url);
             ReadableByteChannel rbc = Channels.newChannel(website.openStream());
             File outputFile = new File(plugin.getDataFolder(), plugin.getDescription().getName() + ".tmp");
             plugin.getDataFolder().mkdir();
             outputFile.createNewFile();
             FileOutputStream fos = new FileOutputStream(outputFile);
             fos.getChannel().transferFrom(rbc, 0, 1 << 24);
             fos.close();
             Scanner s = new Scanner(outputFile);
             while (s.hasNextLine()) {
                 link = s.nextLine();
                 if (link.contains("user-action-download")) {
                     link = link.substring(link.indexOf("href"));
                     link = link.substring(link.indexOf("\"") + 1, link.lastIndexOf("\""));
                     break;
                 }
             }
             s.close();
             outputFile.delete();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return link;
     }
 
     public void downloadJar(String url, String folder) {
         try {
             URL website = new URL(url);
             ReadableByteChannel rbc = Channels.newChannel(website.openStream());
             File outputFile = new File(folder, plugin.getDescription().getName() + ".jar");
             FileOutputStream fos = new FileOutputStream(outputFile);
             fos.getChannel().transferFrom(rbc, 0, 1 << 24);
             fos.close();
             plugin.getLoggerUtility().log("Downloaded" + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion(), LoggerUtility.Level.WARNING);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @SuppressWarnings({ "unchecked", "unused" })
     private void unloadPlugin(final String pluginName) throws NoSuchFieldException, IllegalAccessException {
         PluginManager manager = plugin.getServer().getPluginManager();
         SimplePluginManager spm = (SimplePluginManager) manager;
         SimpleCommandMap commandMap = null;
         List<Plugin> plugins = null;
         Map<String, Plugin> lookupNames = null;
         Map<String, Command> knownCommands = null;
         Map<Event, SortedSet<RegisteredListener>> listeners = null;
         boolean reloadlisteners = true;
         if (spm != null) {
             Field pluginsField = spm.getClass().getDeclaredField("plugins");
             pluginsField.setAccessible(true);
             plugins = (List<Plugin>) pluginsField.get(spm);
             Field lookupNamesField = spm.getClass().getDeclaredField("lookupNames");
             lookupNamesField.setAccessible(true);
             lookupNames = (Map<String, Plugin>) lookupNamesField.get(spm);
             try {
                 Field listenersField = spm.getClass().getDeclaredField("listeners");
                 listenersField.setAccessible(true);
                 listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(spm);
             } catch (Exception e) {
                 reloadlisteners = false;
             }
             Field commandMapField = spm.getClass().getDeclaredField("commandMap");
             commandMapField.setAccessible(true);
             commandMap = (SimpleCommandMap) commandMapField.get(spm);
             Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
             knownCommandsField.setAccessible(true);
             knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
         }
         for (Plugin pl : plugin.getServer().getPluginManager().getPlugins()) {
             if (pl.getDescription().getName().equalsIgnoreCase(pluginName)) {
                 manager.disablePlugin(pl);
                 if (plugins != null && plugins.contains(pl)) {
                     plugins.remove(pl);
                 }
                 if (lookupNames != null && lookupNames.containsKey(pluginName)) {
                     lookupNames.remove(pluginName);
                 }
                 if (listeners != null && reloadlisteners) {
                     for (SortedSet<RegisteredListener> set : listeners.values()) {
                         for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext();) {
                             RegisteredListener value = it.next();
                             if (value.getPlugin() == pl) {
                                 it.remove();
                             }
                         }
                     }
                 }
                 if (commandMap != null) {
                     for (Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
                         Map.Entry<String, Command> entry = it.next();
                         if (entry.getValue() instanceof PluginCommand) {
                             PluginCommand c = (PluginCommand) entry.getValue();
                             if (c.getPlugin() == pl) {
                                 c.unregister(commandMap);
                                 it.remove();
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public void loadPlugin(final String pluginName) throws InvalidPluginException, InvalidDescriptionException {
         PluginManager manager = plugin.getServer().getPluginManager();
         Plugin plugin = manager.loadPlugin(new File("plugins", pluginName + ".jar"));
         if (plugin == null) {
             return;
         }
         manager.enablePlugin(plugin);
     }
 
     /**
      * Checks version with a http-connection
      *
      * @param
      * @return float: latest recommend build.
      */
     /**
     * public float getNewVersion(String url) { if (plugin.config.Internet) {
      * float rt2 = 0; String zeile; try { URL myConnection = new URL(url);
      * URLConnection connectMe = myConnection.openConnection();
      *
      * InputStreamReader lineReader = new
      * InputStreamReader(connectMe.getInputStream()); BufferedReader br = new
      * BufferedReader(new BufferedReader(lineReader)); zeile = br.readLine();
      * rt2 = Float.parseFloat(zeile); } catch (IOException ioe) {
      * ioe.printStackTrace(); plugin.Logger("Exception: IOException!", "Error");
      * return -1; } catch (Exception e) { e.printStackTrace();
      * plugin.Logger("Exception: Exception!", ""); return 0; } return rt2; }
      * else { return plugin.aktuelleVersion(); } } /
      *
      * /**
      * Checks version with a http-connection
      *
      * @param
      * @return float: latest recommend build.
      */
     public String[] getBlacklisted(String url) {
         try {
             URL myConnection = new URL(url);
             URLConnection connectMe = myConnection.openConnection();
 
             InputStreamReader lineReader = new InputStreamReader(connectMe.getInputStream());
             BufferedReader br = new BufferedReader(new BufferedReader(lineReader));
             String Zeile;
             String[] rt;
             for (i = 0; ((Zeile = br.readLine()) != null) && i < 101; i++) {
                 rt = Zeile.split(":");
                 if (rt[0].equalsIgnoreCase(Float.toString(plugin.getVersion()))) {
                     return rt;
                 }
             }
         } catch (IOException ioe) {
             ioe.printStackTrace();
             plugin.getLoggerUtility().log("Exception: IOException!", LoggerUtility.Level.ERROR);
         } catch (Exception e) {
             e.printStackTrace();
             plugin.getLoggerUtility().log("Exception: Exception!", LoggerUtility.Level.INFO);
         }
         return null;
     }
     /**
      * public static String readAll(Reader in) throws IOException { if (in ==
      * null) { throw new NullPointerException("in == null"); } try {
      * StringBuilder sb = new StringBuilder(); char[] buf = new char[1024]; int
      * charsRead; while ((charsRead = in.read(buf)) != -1) { sb.append(buf, 0,
      * charsRead); } return sb.toString(); } finally { in.close(); } }
      *
      * public void autoDownload(String url, String path, String name, String
      * type) throws Exception { File dir = new File(path); if (!dir.exists()) {
      * dir.mkdir(); } File file = new File(path + name);
      *
      * if (file.exists()) { file.delete(); try { URL newurl = new URL(url);
      * //Eingehender Stream wird "erzeugt" BufferedInputStream buffin = new
      * BufferedInputStream(newurl.openStream()); BufferedOutputStream buffout =
      * new BufferedOutputStream(new FileOutputStream(file)); byte[] buffer = new
      * byte[200000]; int len; //Ausgelesene Daten in die Datei schreiben while
      * ((len = buffin.read(buffer)) != -1) { buffout.write(buffer, 0, len); }
      * buffout.flush(); buffout.close(); buffin.close(); plugin.Logger("New " +
      * name + " downloaded, Look up under " + path, "Warning"); } finally { }
      * return; } if (!file.exists()) { try { URL newurl = new URL(url);
      * //Eingehender Stream wird "erzeugt" BufferedInputStream buffin = new
      * BufferedInputStream(newurl.openStream()); BufferedOutputStream buffout =
      * new BufferedOutputStream(new FileOutputStream(file)); byte[] buffer = new
      * byte[200000]; int len; //Ausgelesene Daten in die Datei schreiben while
      * ((len = buffin.read(buffer)) != -1) { buffout.write(buffer, 0, len); }
      * buffout.flush(); buffout.close(); buffin.close(); plugin.Logger("New " +
      * name + " downloaded, Look up under " + path, "Warning"); } finally { } }
      * }
      */
 }
