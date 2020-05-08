 /*
  * Copyright (c) 2013 cedeel.
  * All rights reserved.
  * 
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * The name of the author may not be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.lankylord.SimpleHomes.commands;
 
 import com.github.lankylord.SimpleHomes.SimpleHomes;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author cedeel
  */
 public class SetHomeCommand implements CommandExecutor {
 
     private SimpleHomes instance;
 
     public SetHomeCommand(SimpleHomes instance) {
         this.instance = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
         if (sender instanceof Player && sender.hasPermission("simplehomes.homes")) {
             Player player = (Player) sender;
             if (instance.getHomes().get(player.getName().toLowerCase()) == null) {
                 instance.getHomes().createSection(player.getName().toLowerCase());
             }
 
             Location coords = player.getLocation();
 
             String homeName = "default";
             if (args.length == 1 && sender.hasPermission("simplehomes.multihomes")) {
                 homeName = args[0].toLowerCase();
             }
            
             String section = player.getName().toLowerCase() + "." + homeName;
             if (instance.getHomes().get(section) == null) {
                 instance.getHomes().createSection(section);
             }
 
             ConfigurationSection home = instance.getHomes().getConfigurationSection(section);
 
             home.set("world", player.getWorld().getName());
             home.set("x", coords.getBlockX());
             home.set("y", coords.getBlockY());
             home.set("z", coords.getBlockZ());
             instance.saveHomes();
             sender.sendMessage(ChatColor.YELLOW + "Home set.");
            
             return true;
         }
         return false;
 
     }
 }
