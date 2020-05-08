 /*
    Copyright 2013 Scott Spittle, James Loyd
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package me.ScottSpittle.MuezliPlugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class BukkitLogger {
 
 	public static Main plugin;
 	
 	public final Logger logger = Logger.getLogger("Minecraft");
 	
 	public void enabled(boolean enabled){
 		PluginDescriptionFile pdfFile = plugin.getDescription();
 		if(enabled){
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has been Enabled");
 		}else {
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has been Disabled");
 		}
 	}
 }
