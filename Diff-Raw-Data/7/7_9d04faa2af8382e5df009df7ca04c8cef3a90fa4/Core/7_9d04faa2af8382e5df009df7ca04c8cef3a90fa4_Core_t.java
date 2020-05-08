 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of DirectorPlugin.
  * 
  * DirectorPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * DirectorPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with DirectorPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.director;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.bukkit.gemo.BukkitHTTP.HTTPCore;
 import com.bukkit.gemo.BukkitHTTP.HTTPPlugin;
 
 import de.minestar.director.area.AreaHandler;
 import de.minestar.director.commands.dir.AreaSaveCommand;
 import de.minestar.director.commands.dir.DirCommand;
 import de.minestar.director.commands.dir.ResetCommand;
 import de.minestar.director.commands.dir.SelectCommand;
 import de.minestar.director.commands.dir.ShowArea;
 import de.minestar.director.commands.dir.StartCommand;
 import de.minestar.director.database.DatabaseHandler;
 import de.minestar.director.listener.AreaDefineListener;
 import de.minestar.director.listener.BlockChangeListener;
 import de.minestar.director.web.DirectorHTTP;
 import de.minestar.minestarlibrary.commands.CommandList;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class Core extends JavaPlugin {
 
     public final static String NAME = "Director";
 
     // WEBSERVER
     public static HTTPPlugin thisHTTP;
 
     private static Core instance;
 
     private DatabaseHandler dbHandler;
 
     private AreaHandler areaHandler;
 
     private AreaDefineListener adListener;
 
     private CommandList cmdList;
 
     @Override
     public void onDisable() {
         dbHandler.flushQueue();
         dbHandler.closeConnection();
         dbHandler = null;
         adListener = null;
         cmdList = null;
 
         ConsoleUtils.printInfo(NAME, "Disabled!");
     }
 
     @Override
     public void onEnable() {
 
         File dataFolder = getDataFolder();
         dataFolder.mkdirs();
 
         dbHandler = new DatabaseHandler(NAME, dataFolder);
         // when we don't have a connection to the database, the whole plugin
         // can't work
         if (!dbHandler.hasConnection()) {
             ConsoleUtils.printError(NAME, "------------------------------------------");
             ConsoleUtils.printError(NAME, "- COULD NOT CONNECT TO DIRECTOR DATABASE -");
             ConsoleUtils.printError(NAME, "------------------------------------------");
             return;
         }
 
         areaHandler = new AreaHandler(dbHandler, dataFolder);
         // Register event listener
         PluginManager pm = getServer().getPluginManager();
        adListener = new AreaDefineListener(dbHandler, areaHandler);
        pm.registerEvents(adListener, this);
         pm.registerEvents(new BlockChangeListener(dbHandler, areaHandler), this);
 
         // INIT COMMANDLIST
         initCommandList();
 
         // REGISTER HTTP-LISTENER
         registerHTTP();
 
         instance = this;
 
         ConsoleUtils.printInfo(NAME, getDescription().getVersion() + " is enabled!");
     }
 
     public static JavaPlugin getInstance() {
         return instance;
     }
 
     private void initCommandList() {
         //@formatter:off
         cmdList = new CommandList(NAME,
 
                 new DirCommand  ("/dir","","",
                     new AreaSaveCommand ("save",    "<Name>","directorsplugin.save",adListener, dbHandler, areaHandler),
                     new SelectCommand   ("select",  "","directorsplugin.select", adListener),
                     new ResetCommand    ("reset",   "<Name>","directorsplugin.reset", areaHandler),
                     new ShowArea        ("show",    "<AreaName>","directorsplugin.show", areaHandler, getDataFolder()),
                     new StartCommand    ("start",   "<AreaName>","directorsplugin.start",dbHandler,areaHandler)
             )
         );
         //@formatter:on
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         cmdList.handleCommand(sender, label, args);
         return true;
     }
 
     // REGISTER AT BukkitHTTP
     public void registerHTTP() {
 
         Plugin httpPlugin = Bukkit.getPluginManager().getPlugin("BukkitHTTP");
         if (httpPlugin != null) {
 
             if (!httpPlugin.isEnabled())
                 Bukkit.getPluginManager().enablePlugin(httpPlugin);
 
             HTTPCore http = (HTTPCore) httpPlugin;
             thisHTTP = new DirectorHTTP("director", "DirectorsPlugin", "DirectorsPlugin/web/", false);
             thisHTTP.setOwn404Page(true);
             http.registerPlugin(thisHTTP);
         } else
             ConsoleUtils.printError(NAME, "BukkitHTTP not found!");
     }
 }
