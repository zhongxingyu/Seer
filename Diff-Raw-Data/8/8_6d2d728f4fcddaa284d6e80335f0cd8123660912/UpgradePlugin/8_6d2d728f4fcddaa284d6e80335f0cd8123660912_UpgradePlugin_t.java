 package com.tropicalwikis.tuxcraft.plugins.upgrade;
 
 /*
  * Copyright (c) 2012, tuxed
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of tuxed nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL TUXED BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.List;
 import java.util.logging.Logger;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class UpgradePlugin extends JavaPlugin {
 
     private static final Logger log = Logger.getLogger("Minecraft");
     private static Economy econ = null;
     private static Permission perms = null;
     
     protected final YamlConfiguration config = new YamlConfiguration();
     public static UpgradePlugin plugin;
     public final File dir = new File("plugins/Upgrade");
     public final File yml = new File(this.dir, "config.yml");
     
     boolean broadcast;
 
     @Override
     public void onEnable(){
     if(!setupEconomy()){
             log.info("Vault not found!");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
     if(!this.yml.exists()){
       firstRunSettings();
     }
     Loaded(this.yml);
     read();
    log.info("[Upgrade] by: Tuxed is now enabled");
     setupPermissions();
     }
     
     public void read() {
         if (this.config.getBoolean("broadcast-promotions")) {
             this.broadcast = true;
         }
     }
     
     public void Loaded(File file){
         try{
             this.config.load(file);
         }catch (Exception e){
         }
     }
     
     private void firstRunSettings() {
         try{
             this.dir.mkdir();
             FileWriter fstream = new FileWriter(this.yml);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write("promotion-command: vault\n");
             out.write("broadcast-promotions: true\n");
             out.write("ranks:\n");
             out.write("  - default\n");
             out.write("  - builder\n");
             out.write("ranksprices:\n");
             out.write("  default: 0\n");
             out.write("  builder: 1000\n");
 
             out.close();
             }catch(Exception e){
             log.severe("Failed to create config file!");
         }
     }
     
     private boolean setupEconomy(){
         if (getServer().getPluginManager().getPlugin("Vault") == null){
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null){
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
     private boolean setupPermissions(){
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perms = rsp.getProvider();
         return perms != null;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         try{
            if(sender.hasPermission("upgrade.reload") && command.getName().equalsIgnoreCase("rankup") && args.length == 1){
                 reloadConfig();
                 sender.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
                 return true;
             }
         }catch (Exception e){}
     	
     	if(!(sender instanceof Player)) {
             sender.sendMessage("This is a in-game command.");
             return true;
         }
         Player player = (Player)sender;
         if(sender.hasPermission("upgrade.rankup")){
         String c = perms.getPrimaryGroup(player);
         List<String> groups = getConfig().getStringList("ranks");
         if(groups.contains(c)){
     		String s;
     		try{
     			s = groups.get(groups.indexOf(c) + 1);
     		}catch(IndexOutOfBoundsException e){
     			sender.sendMessage(ChatColor.RED + "You're at the highest rank already!");
     			return true;
     		}
     		double price = getConfig().getDouble("rankprices."+s, 1000.0);
                 double stillNeeded = price - econ.getBalance(sender.getName());
     		if(econ.getBalance(sender.getName()) >= price) {
                     econ.withdrawPlayer(sender.getName(), price);
                     // Now, do one of the following:
                     // 1. If the command is "vault", attempt to change permissions using Vault
                     // 2. Otherwise invoke the command; %username% and %group% will automatically be filled in.
                     String cmd = getConfig().getString("promotion-command", "vault");
                     if(cmd.equals("vault")) {
                         perms.playerAddGroup(player, s);
                         // better be safer than sorry
                         perms.playerRemoveGroup(player, c);
                     }else{
                         getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replaceAll("%username%", player.getName()).replaceAll("%group%", s));
                     }
                     if(broadcast) {
                        getServer().broadcastMessage(ChatColor.GOLD + "" +ChatColor.BOLD + sender.getName() + ChatColor.RESET + "" + ChatColor.GREEN + " has been promoted to " + ChatColor.AQUA + "" + ChatColor.BOLD + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + "!");
                     }else{
                         player.sendMessage(ChatColor.GREEN + "You have been promoted to " + ChatColor.AQUA + "" + ChatColor.BOLD + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + "!");
                         player.sendMessage(ChatColor.GREEN + "Your account balance is now " + ChatColor.RED +econ.getBalance(sender.getName()) + "!");
                     }
     		}else{
                     sender.sendMessage(ChatColor.RED + "You do not have enough money. The next rank, "+ ChatColor.AQUA + "" + ChatColor.BOLD + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + ChatColor.RED + ", costs " + econ.format(price) + ".");
                     sender.sendMessage(ChatColor.RED + "Which means you still need " + ChatColor.GOLD + "" + ChatColor.BOLD +econ.format(stillNeeded));
     		}
             }
             }else{
         	sender.sendMessage(ChatColor.RED + "You do not have permission to rank up.");
             }
             return true;
         }
     }
