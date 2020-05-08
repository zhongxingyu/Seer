 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.dsdev.greencrate;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Greenlock28
  */
 public class GreenCrate extends JavaPlugin {
 
     @Override
     public void onEnable() {
         File defaultConfig = new File(getDataFolder(), "config.yml");
         if (!defaultConfig.exists()) {
             getLogger().info("Saving default GreenCrate config file...");
             saveDefaultConfig();
         }
         reloadConfig();
 
         listen = new CrateListener(getConfig(), getServer());
 
         getServer().getPluginManager().registerEvents(listen, this);
 
         getLogger().info("GreenCrate has been enabled!");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("GreenCrate has been disabled!");
     }
     
     
     
     public static Random rand = new Random();
     private CrateListener listen;
 
     
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         
         if (cmd.getName().equalsIgnoreCase("crate") && sender.hasPermission("greencrate.crate")) {
             
             if (sender instanceof Player) {
 
                 Player sendingPlayer = (Player)sender;
 
                 if (args.length < 1) {
                     DoCommandHelp(sender, sendingPlayer);
                     return true;
                 }
 
                 switch (args[0].toLowerCase()) {
                     case "spawn":
                         DoCommandSpawn(sender, sendingPlayer, args);
                         break;
                     case "give":
                         DoCommandGive(sender, sendingPlayer, args);
                         break;
                     case "random":
                         DoCommandRandom(sender, sendingPlayer, args);
                         break;
                     case "giverandom":
                         DoCommandGiveRandom(sender, sendingPlayer, args);
                         break;
                     case "open":
                         DoCommandOpen(sender, sendingPlayer, args);
                         break;
                     case "openfor":
                         DoCommandOpenFor(sender, sendingPlayer, args);
                         break;
                     case "openrandom":
                         DoCommandOpenRandom(sender, sendingPlayer, args);
                         break;
                     case "openrandomfor":
                         DoCommandOpenRandomFor(sender, sendingPlayer, args);
                         break;
                     case "reload":
                         DoCommandReload(sender, sendingPlayer, args);
                         break;
                     case "help":
                         DoCommandHelp(sender, sendingPlayer);
                         break;
                     case "?":
                         DoCommandHelp(sender, sendingPlayer);
                         break;
                     case "list":
                         DoCommandList(sender, sendingPlayer);
                         break;
                     default:
                         DoCommandHelp(sender, sendingPlayer);
                         break;
                 } 
             } else {
                 
                 if (args.length < 1) {
                     return false;
                 }
 
                 switch (args[0]) {
                     case "give":
                         DoConsoleCommandGive(sender, args);
                         break;
                     case "giverandom":
                         DoConsoleCommandGiveRandom(sender, args);
                         break;
                     case "openfor":
                         DoConsoleCommandOpenFor(sender, args);
                         break;
                     case "openrandomfor":
                         DoConsoleCommandOpenRandomFor(sender, args);
                         break;
                     case "reload":
                         DoConsoleCommandReload(sender, args);
                         break;
                     default:
                         return false;
                 }
             }
         }
         return true;
     }
 
     
     
     public void DoCommandSpawn(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.spawn"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
             return;
         }
 
         if (args.length < 2) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Not enough arguments supplied.");
             return;
         }
 
         if (getConfig().getString("crates." + args[1] + ".item-id") == null) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Specified crate does not exist.");
             return;
         }
 
         sender.getInventory().addItem(GetCrateItemStack(args[1], sender.getName(), getConfig()));
         
         sender.sendMessage("§2[§aGreenCrate§2]§r Spawned in crate \"" + args[1].replace("_", " ") + "\"");
     }
 
     public void DoCommandGive(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.give"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
             return;
         }
         
         if (args.length < 3) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Not enough arguments supplied.");
             return;
         }
         
         Player target = getServer().getPlayer(args[1]);
         
         if (target == null) {
             sender.sendMessage("§2[§aGreenCrate§2]§r The specified player is not online!");
             return;
         }
 
         if (getConfig().getString("crates." + args[2] + ".item-id") == null) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Specified crate does not exist.");
             return;
         }
 
         target.getInventory().addItem(GetCrateItemStack(args[2], target.getName(), getConfig()));
         
         sender.sendMessage("§2[§aGreenCrate§2]§r Gave crate \"" + args[2].replace("_", " ") + "\" to player " + target.getName());
     }
 
     public void DoCommandRandom(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.random"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
             return;
         }
 
         int crateindex = rand.nextInt(getConfig().getConfigurationSection("crates").getKeys(false).size());
         String cratename = (String) getConfig().getConfigurationSection("crates").getKeys(false).toArray()[crateindex];
 
         sender.getInventory().addItem(GetCrateItemStack(cratename, sender.getName(), getConfig()));
         
         sender.sendMessage("§2[§aGreenCrate§2]§r Spawned in random crate \"" + cratename.replace("_", " ") + "\"");
     }
 
     public void DoCommandGiveRandom(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.giverandom"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
             return;
         }
         
         if (args.length < 2) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Not enough arguments supplied.");
             return;
         }
         
         Player target = getServer().getPlayer(args[1]);
 
         if (target == null) {
             sender.sendMessage("§2[§aGreenCrate§2]§r The specified player is not online!");
             return;
         }
         
         int crateindex = rand.nextInt(getConfig().getConfigurationSection("crates").getKeys(false).size());
         String cratename = (String) getConfig().getConfigurationSection("crates").getKeys(false).toArray()[crateindex];
 
         target.getInventory().addItem(GetCrateItemStack(cratename, target.getName(), getConfig()));
         
         sender.sendMessage("§2[§aGreenCrate§2]§r Gave random crate \"" + cratename.replace("_", " ") + "\" to player " + target.getName());
     }
 
     public void DoCommandOpen(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.open"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
         }
 
         if (args.length < 2) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Not enough arguments supplied.");
         }
 
         List<ItemStack> items = listen.GetCrateItems(args[1]);
 
         if (getConfig().getBoolean("crates." + args[1] + ".gui.enabled")) {
             Inventory inv = sender.getServer().createInventory(null, getConfig().getInt("crates." + args[1] + ".gui.chest-rows") * 9, getConfig().getString("crates." + args[1] + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             sender.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 sender.getInventory().addItem(i);
             }
 
             sender.updateInventory();
         }
     }
 
     public void DoCommandOpenFor(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.openfor"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
         }
 
         if (args.length < 3) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Not enough arguments supplied.");
         }
 
         List<ItemStack> items = listen.GetCrateItems(args[2]);
 
         Player target = getServer().getPlayer(args[1]);
         
         if (target == null) {
             sender.sendMessage("§2[§aGreenCrate§2]§r The specified player is not online!");
             return;
         }
         
         if (getConfig().getBoolean("crates." + args[2] + ".gui.enabled")) {
             Inventory inv = target.getServer().createInventory(null, getConfig().getInt("crates." + args[2] + ".gui.chest-rows") * 9, getConfig().getString("crates." + args[2] + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             target.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 target.getInventory().addItem(i);
             }
 
             target.updateInventory();
         }
     }
     
     public void DoCommandOpenRandom(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.openrandom"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
             return;
         }
 
         int crateindex = rand.nextInt(getConfig().getConfigurationSection("crates").getKeys(false).size());
         String cratename = (String) getConfig().getConfigurationSection("crates").getKeys(false).toArray()[crateindex];
 
         List<ItemStack> items = listen.GetCrateItems(cratename);
 
         if (getConfig().getBoolean("crates." + cratename + ".gui.enabled")) {
             Inventory inv = sender.getServer().createInventory(null, getConfig().getInt("crates." + cratename + ".gui.chest-rows") * 9, getConfig().getString("crates." + cratename + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             sender.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 sender.getInventory().addItem(i);
             }
 
             sender.updateInventory();
         }
     }
     
     public void DoCommandOpenRandomFor(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.openrandomfor"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
             return;
         }
 
         if (args.length < 2) {
             sender.sendMessage("§2[§aGreenCrate§2]§r Not enough arguments supplied.");
             return;
         }
         
         int crateindex = rand.nextInt(getConfig().getConfigurationSection("crates").getKeys(false).size());
         String cratename = (String) getConfig().getConfigurationSection("crates").getKeys(false).toArray()[crateindex];
 
         List<ItemStack> items = listen.GetCrateItems(cratename);
 
         Player target = getServer().getPlayer(args[1]);
         
         if (target == null) {
             sender.sendMessage("§2[§aGreenCrate§2]§r The specified player is not online!");
             return;
         }
         
         if (getConfig().getBoolean("crates." + cratename + ".gui.enabled")) {
             Inventory inv = target.getServer().createInventory(null, getConfig().getInt("crates." + cratename + ".gui.chest-rows") * 9, getConfig().getString("crates." + cratename + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             target.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 target.getInventory().addItem(i);
             }
 
             target.updateInventory();
         }
     }
 
     public void DoCommandReload(CommandSender cmdSender, Player sender, String[] args) {
         
         if (!(sender.hasPermission("greencrate.crate.reload"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
         }
 
         reloadConfig();
         HandlerList.unregisterAll(this);
         listen = new CrateListener(getConfig(), getServer());
         getServer().getPluginManager().registerEvents(listen, this);
 
         sender.sendMessage("§2[§aGreenCrate§2]§r Config reloaded!");
     }
 
     public void DoCommandHelp(CommandSender cmdSender, Player sender) {
         
         sender.sendMessage("§2[§aGreenCrate§2]§r Commands:");
         
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.spawn") + "/crate spawn <cratename>§r  --  Spawns in a new crate item.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.give") + "/crate give <player> <cratename>§r  --  Gives the specified player a given crate.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.random") + "/crate random§r  --  Spawns in a random crate.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.giverandom") + "/crate giverandom <player>§r  --  Gives the specified player a random crate.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.open") + "/crate open <cratename>§r  --  Opens a given crate.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.openrandom") + "/crate openrandom§r  --  Opens a random crate.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.openrandomfor") + "/crate openrandomfor§r  --  Opens a random crate for the given player.");
         sender.sendMessage("§r§" + GetPermissionColor(cmdSender, "greencrate.crate.reload") + "/crate reload§r  --  Reloads the GreenCrate config.yml file.");
     }
     
     public void DoCommandList(CommandSender cmdSender, Player sender) {
         if (!(sender.hasPermission("greencrate.crate.list"))) {
             sender.sendMessage("§2[§aGreenCrate§2]§r You do not have permission for this command.");
         }
         
         sender.sendMessage("§2[§aGreenCrate§2]§r Available crates:");
         if (getConfig().getBoolean("global.require-crate-perms")) {
             for (String cratename : getConfig().getConfigurationSection("crates").getKeys(false))
                 if (sender.hasPermission("greencrate.use." + cratename))
                     sender.sendMessage("§r§6- " + cratename);
         } else {
             for (String cratename : getConfig().getConfigurationSection("crates").getKeys(false))
                 sender.sendMessage("§r§6- " + cratename);
         }
     }
 
     
     
     public void DoConsoleCommandGive(CommandSender cmdSender, String[] args) {
         
         Player target = getServer().getPlayer(args[1]);
 
         if (target == null) {
             getLogger().warning("The specified player is not online!");
             return;
         }
         
         if (args.length < 3) {
             return;
         }
 
         if (getConfig().getString("crates." + args[2] + ".item-id") == null) {
             return;
         }
 
         target.getInventory().addItem(GetCrateItemStack(args[2], target.getName(), getConfig()));
     }
 
     public void DoConsoleCommandGiveRandom(CommandSender cmdSender, String[] args) {
         
         Player target = getServer().getPlayer(args[1]);
 
         if (target == null) {
             getLogger().warning("The specified player is not online!");
             return;
         }
         
         if (args.length < 2) {
             return;
         }
 
         int crateindex = rand.nextInt(getConfig().getConfigurationSection("crates").getKeys(false).size());
         String cratename = (String) getConfig().getConfigurationSection("crates").getKeys(false).toArray()[crateindex];
 
         target.getInventory().addItem(GetCrateItemStack(cratename, target.getName(), getConfig()));
     }
 
     public void DoConsoleCommandOpenFor(CommandSender cmdSender, String[] args) {
         
         if (args.length < 3) {
             return;
         }
 
         List<ItemStack> items = listen.GetCrateItems(args[2]);
 
         Player target = getServer().getPlayer(args[1]);
         
         if (target == null) {
             getLogger().warning("The specified player is not online!");
             return;
         }
         
         if (getConfig().getBoolean("crates." + args[2] + ".gui.enabled")) {
             Inventory inv = target.getServer().createInventory(null, getConfig().getInt("crates." + args[2] + ".gui.chest-rows") * 9, getConfig().getString("crates." + args[2] + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             target.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 target.getInventory().addItem(i);
             }
 
             target.updateInventory();
         }
     }
     
     public void DoConsoleCommandOpenRandomFor(CommandSender cmdSender, String[] args) {
         
         if (args.length < 2) {
             return;
         }
         
         int crateindex = rand.nextInt(getConfig().getConfigurationSection("crates").getKeys(false).size());
         String cratename = (String) getConfig().getConfigurationSection("crates").getKeys(false).toArray()[crateindex];
 
         List<ItemStack> items = listen.GetCrateItems(cratename);
 
         Player target = getServer().getPlayer(args[1]);
         
         if (target == null) {
             getLogger().warning("The specified player is not online!");
             return;
         }
         
         if (getConfig().getBoolean("crates." + cratename + ".gui.enabled")) {
             Inventory inv = target.getServer().createInventory(null, getConfig().getInt("crates." + cratename + ".gui.chest-rows") * 9, getConfig().getString("crates." + cratename + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             target.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 target.getInventory().addItem(i);
             }
 
             target.updateInventory();
         }
     }
 
     public void DoConsoleCommandReload(CommandSender cmdSender, String[] args) {
         reloadConfig();
         HandlerList.unregisterAll(this);
         listen = new CrateListener(getConfig(), getServer());
         getServer().getPluginManager().registerEvents(listen, this);
     }
 
     
     
     public static ItemStack GetCrateItemStack(String cratename, String playername, FileConfiguration config) {
         ItemStack crate = new ItemStack(config.getInt("crates." + cratename + ".item-id"), 1, (short)config.getInt("crates." + cratename + ".item-data"));
         ItemMeta cratemeta = crate.getItemMeta();
 
         /*if (config.getBoolean("crates." + cratename + ".enable-crate-number")) {
             cratemeta.setDisplayName(config.getString("crates." + cratename + ".display-name").replace("&", "§") + " #" + Integer.toString(rand.nextInt(1000)));
         } else {
             cratemeta.setDisplayName(config.getString("crates." + cratename + ".display-name").replace("&", "§"));
         }*/
         cratemeta.setDisplayName(config.getString("crates." + cratename + ".display-name").replace("&", "§").replace("{rand}", Integer.toString(rand.nextInt(1000))));
 
         ArrayList<String> lore = new ArrayList();
         if (config.getBoolean("crates." + cratename + ".enable-lore-name"))
                 lore.add(cratename.replace("_", " "));
        if (!(config.getString("crates." + cratename + ".item-lore").equals("none")))
                 lore.add(config.getString("crates." + cratename + ".item-lore"));
         if (config.getBoolean("crates." + cratename + ".bind-to-player"))
                 lore.add("§r§8Only for " + playername);
         if (!lore.isEmpty())
             cratemeta.setLore(lore);
         
         crate.setItemMeta(cratemeta);
 
         return crate;
     }
 
     public String GetPermissionColor(CommandSender sender, String perm) {
         if (sender.hasPermission(perm))
             return "6";
         else
             return "8";
     }
 }
