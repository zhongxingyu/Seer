 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package buscript;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * The main Plugin class which allows this script library to be run as a plugin and gives access to the command "run".
  */
 public class BuscriptPlugin extends JavaPlugin {
 
     private Buscript buscript;
 
     @Override
     public void onEnable() {
         buscript = new Buscript(this);
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 getAPI().executeScript(getStartupScript());
             }
         });
     }
 
     /**
      * Retrieves the primary API for this plugin.  The object this returns is where all of the good stuff happens.
      *
      * @return The buscript library API.
      */
     public Buscript getAPI() {
         return buscript;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (label.equalsIgnoreCase("run")) {
             if (args.length < 1) {
                 return false;
             }
             File scriptFile = new File(getAPI().getScriptFolder(), args[0]);
             if (!scriptFile.exists()) {
                 sender.sendMessage("Script '" + scriptFile + "' does not exist!");
                 return true;
             }
             Player player = null;
             if (sender instanceof Player) {
                 player = (Player) sender;
             }
             if (args.length == 1) {
                 getAPI().executeScript(scriptFile, player);
                 return true;
             } else if (args.length == 2) {
                 getAPI().executeScript(scriptFile, args[1], player);
                 return true;
             }
         } else if (label.equalsIgnoreCase("breload")) {
             buscript.clearScriptCache();
             sender.sendMessage("Script cache cleared!");
         }
         return false;
     }
 
     private File getStartupScript() {
         File scriptFile = new File(getDataFolder(), "startup-script.txt");
         if (!scriptFile.exists()) {
             try {
                 this.saveResource("startup-script.txt", false);
                 if (!scriptFile.exists()) {
                     scriptFile.createNewFile();
                 }
             } catch (IOException e) {
                getLogger().severe("Error creating startup script file: " + e.getMessage());
                 getServer().getPluginManager().disablePlugin(this);
                 return null;
            } catch (IllegalArgumentException e){
                getLogger().info("Start up script not found in jar.");
             }
         }
         return scriptFile;
     }
 }
