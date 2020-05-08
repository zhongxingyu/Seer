 package org.royaldev.royalmessages;
 
 /*
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
  This plugin was written by jkcclemens <jkc.clemens@gmail.com>.
  If forked and not credited, alert him.
  */
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RoyalMessages extends JavaPlugin {
 
    public String version = "0.0.1";
 
     private final PListener playerListener = new PListener(this);
 
     public String loginformat = null;
     public String quitformat = null;
     public String kickFormat = null;
     public Boolean uselogin = null;
     public Boolean usequit = null;
     public Boolean useKick = null;
     public Boolean tellconsole = null;
 
     public void loadConfiguration() {
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
         loginformat = this.getConfig().getString("login-format")
                 .replaceAll("(&([a-f0-9]))", "\u00A7$2");
         quitformat = this.getConfig().getString("quit-format")
                 .replaceAll("(&([a-f0-9]))", "\u00A7$2");
         kickFormat = this.getConfig().getString("kick-format").replaceAll("(&([a-f0-9]))", "\u00A7$2");
         usequit = this.getConfig().getBoolean("enable-quit-message");
         uselogin = this.getConfig().getBoolean("enable-login-message");
         tellconsole = this.getConfig().getBoolean("tell-console");
         useKick = this.getConfig().getBoolean("enable-kick-message");
     }
 
     Logger log = Logger.getLogger("Minecraft");
 
     public void onEnable() {
 
         Commands cmdExec = new Commands(this);
 
         getCommand("rmessages").setExecutor(cmdExec);
 
         PluginManager pm = this.getServer().getPluginManager();
 
         pm.registerEvents(playerListener, this);
 
         loadConfiguration();
 
         log.info("[RoyalMessages] RoyalMessages v" + version + " initiated.");
 
     }
 
     public void onDisable() {
 
        log.info("[RoyalMessages] RoyalMessages v" + version + "disabled.");
 
     }
 
 }
