 package com.me.tft_02.tftaddon;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.me.tft_02.tftaddon.util.UserProfiles;
 
 class Commands implements CommandExecutor {
     private TfTAddon plugin;
 
     public Commands(final TfTAddon instance) {
         plugin = instance;
     }
 
     private UserProfiles users = new UserProfiles();
 
     private String duraChance;
 
     private float dura_level_cap;
     private float dura_percentage_max;
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         Player player = null;
 
         dura_level_cap = plugin.getConfig().getInt("Skills.Axes.Dura_level_cap");
         dura_percentage_max = plugin.getConfig().getInt("Skills.Axes.Dura_percentage_max");
 
         if (sender instanceof Player) {
             player = (Player) sender;
         }
 
         if (cmd.getName().equalsIgnoreCase("tftaddon")) {
             if (player == null) {
                 sender.sendMessage("TfTAddon adds extra features for mcMMO.");
                 sender.sendMessage("Type /tfthelp for a list of all the commands.");
             } else {
                 if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                     return reloadConfiguration(sender);
                 }
                 if (player.hasPermission("tftaddon.tftaddon")) {
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "TfT Addon" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.GOLD + "TfTAddon has these extra features for mcMMO:");
                     player.sendMessage(ChatColor.GREEN + "Reduce durability for Axes.");
                     player.sendMessage(ChatColor.GREEN + "Change the weather with Herbalism.");
                     player.sendMessage(ChatColor.GREEN + "Receive Repair warning, of the item in use.");
                     player.sendMessage(ChatColor.GOLD + "type /tfthelp for a list of all the commands.");
                 }
             }
             return true;
         } else if (cmd.getName().equalsIgnoreCase("tfthelp")) {
             if (player == null) {
                 sender.sendMessage("Available commands: /tftaddon /tfthelp");
             } else {
                 if (player.hasPermission("tftaddon.tfthelp")) {
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "TfT Help" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.GOLD + "Commands:");
 
                    if (player.hasPermission("tftaddon.commands.reload")) {
                        player.sendMessage(ChatColor.GREEN + "/tftaddon [reload]" + ChatColor.GRAY + " Displays general info, or reload the config file.");
                    } else if (player.hasPermission("tftaddon.tftaddon")) {
                         player.sendMessage(ChatColor.GREEN + "/tftaddon" + ChatColor.GRAY + " Displays general info.");
                     }
                     if (player.hasPermission("tftaddon.axes")) {
                         player.sendMessage(ChatColor.GREEN + "/tftaxes" + ChatColor.GRAY + " Check Axes addon info.");
                     }
                     if (player.hasPermission("tftaddon.herbalism")) {
                         player.sendMessage(ChatColor.GREEN + "/tftherbalism" + ChatColor.GRAY + " Check Herbalism addon info.");
                     }
                     if (player.hasPermission("tftaddon.repair")) {
                         player.sendMessage(ChatColor.GREEN + "/tftrepair" + ChatColor.GRAY + " Check Repair addon info.");
                     }
                     if (player.hasPermission("tftaddon.dura")) {
                        player.sendMessage(ChatColor.GREEN + "/dura" + ChatColor.GRAY + " Check durability of item in hand.");
                     }
                 }
             }
             return true;
 
         } else if (cmd.getName().equalsIgnoreCase("tftaxes")) {
             if (player == null) {
                 sender.sendMessage("This command does not support console usage.");
             } else {
                 if (player.hasPermission("tftaddon.axes")) {
                     float level_current = users.getSkillLevel(player, "AXES");
                     dataCalculations(level_current);
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "TfT AXES" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.GRAY + "Extra abilities from TfTAddon");
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "YOUR STATS" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.RED + "Chance to reduce durability: " + ChatColor.YELLOW + duraChance + "%");
                 }
             }
             return true;
         } else if (cmd.getName().equalsIgnoreCase("tftherbalism")) {
             if (player == null) {
                 sender.sendMessage("This command does not support console usage.");
             } else {
                 if (player.hasPermission("tftaddon.herbalism")) {
                     int summonAmount = plugin.getConfig().getInt("Skills.Herbalism.SunnyDay_cost");
                     int sunnydayUnlock = plugin.getConfig().getInt("Skills.Herbalism.SunnyDay_unlock_level");
                     float herbaValue = users.getSkillLevel(player, "HERBALISM");
 
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "TfT HERBALISM" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.GRAY + "Extra abilities from TfTAddon");
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "EFFECTS" + ChatColor.RED + "[]-----");
                     if (herbaValue < sunnydayUnlock) {
                         player.sendMessage(ChatColor.GRAY + "LOCKED UNTIL " + sunnydayUnlock + "+ SKILL (SUNNY DAY)");
                     } else {
                         player.sendMessage(ChatColor.DARK_AQUA + "Sunny Day: " + ChatColor.GREEN + "Makes the sun shine bright.");
                         player.sendMessage(ChatColor.GRAY + "Crouch and left-click with " + summonAmount + " seeds in your hand.");
                     }
                 }
             }
             return true;
 
         } else if (cmd.getName().equalsIgnoreCase("tftrepair")) {
             if (player == null) {
                 sender.sendMessage("This command does not support console usage.");
             } else {
                 if (player.hasPermission("tftaddon.repair")) {
 
                     int blacksmithsinstinctUnlock = plugin.getConfig().getInt("Skills.Repair.BlacksmithsInstinct_unlock_level");
                     float repairValue = users.getSkillLevel(player, "REPAIR");
 
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "TfT Repair" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.GRAY + "Extra abilities from TfTAddon");
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "EFFECTS" + ChatColor.RED + "[]-----");
                     if (repairValue < blacksmithsinstinctUnlock) {
                         player.sendMessage(ChatColor.GRAY + "LOCKED UNTIL " + blacksmithsinstinctUnlock + "+ SKILL (BLACKSMITH'S INSTINCT)");
                     } else {
                         player.sendMessage(ChatColor.DARK_AQUA + "Blacksmith's instinct: " + ChatColor.GREEN + "Sense when tools need a repair.");
                     }
                 }
             }
             return true;
 
         } else if (cmd.getName().equalsIgnoreCase("dura")) {
             if (player == null) {
                 sender.sendMessage("This command does not support console usage.");
             } else {
                 if (player.hasPermission("tftaddon.dura")) {
                     ItemStack itemInHand = player.getItemInHand();
                     float durability = itemInHand.getDurability();
 
                     player.sendMessage(ChatColor.RED + "-----[]" + ChatColor.GREEN + "TfT Addon" + ChatColor.RED + "[]-----");
                     player.sendMessage(ChatColor.RED + "Item durability is: " + ChatColor.YELLOW + durability);
                 }
             }
             return true;
         }
         return false;
     }
 
     private void dataCalculations(float level_current) {
         dura_level_cap = plugin.getConfig().getInt("Skills.Axes.Dura_level_cap");
         dura_percentage_max = plugin.getConfig().getInt("Skills.Axes.Dura_percentage_max");
         if (level_current < dura_level_cap) {
             duraChance = String.valueOf((dura_percentage_max / dura_level_cap) * level_current);
         } else if (level_current >= dura_level_cap) {
             duraChance = String.valueOf(dura_percentage_max);
         }
     }
 
     private boolean reloadConfiguration(CommandSender sender) {
         if (sender instanceof Player && !((Player) sender).hasPermission("tftaddon.commands.reload")) {
             return false;
         }
 
         plugin.reloadConfig();
         sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
         return false;
     }
 }
