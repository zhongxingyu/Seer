 /*
 * ChatSounds - a bukkit plugin to allow sounds effects in chat
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.thereverend403;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class chatsounds extends JavaPlugin {
     public final PlayerListener pl = new PlayerListener(this);
 	@Override
 	public void onEnable(){
 		FileConfiguration config = this.getConfig();
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new PlayerListener(this), this);
 		config = getConfig();
 		config.addDefault("chatsounds.global", true);
         config.addDefault("chatsounds.aliases.cat", "meow");
         config.addDefault("chatsounds.aliases.catpurr", "purr");
         config.addDefault("chatsounds.aliases.dog", "woof");
         config.addDefault("chatsounds.aliases.doggrowl", "grrr");
         config.addDefault("chatsounds.aliases.pig", "oink");
         config.addDefault("chatsounds.aliases.chicken", "cluck");
         config.addDefault("chatsounds.aliases.cow", "moo");
         config.addDefault("chatsounds.aliases.sheep", "baaa");
         config.addDefault("chatsounds.aliases.creeper", "hiss");
         config.addDefault("chatsounds.aliases.enderman", "slender");
         config.addDefault("chatsounds.aliases.explosion", "boom");
         config.addDefault("chatsounds.aliases.dragondeath", "dragondeath");
 		config.options().copyDefaults(true);
 		saveConfig();
         getLogger().info("ChatSounds has successfully enabled");
 		}
 
 	@Override
 	public void onDisable(){
 		//Do more things.
 		getLogger().info("ChatSounds has successfully disabled");
 	}
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
         if(cmd.getName().equalsIgnoreCase("chatsounds")){
                  if(args.length < 1){
                     sender.sendMessage(ChatColor.RED + "Not enough arguments.");
                     return true;
                  }
                  if(args.length > 1){
                     sender.sendMessage(ChatColor.RED + "Too many arguments.");
                     return true;
                  }
             if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("chatsounds.reload")){
                 reloadConfig();
                 sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
                 return true;
         }else{
             sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
             return true;
         }
     }
         return false;
     }
 }
