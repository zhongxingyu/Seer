 package com.thepastimers.ExtCommands;
 
 import com.thepastimers.Chat.Chat;
 import com.thepastimers.Chat.CommandData;
 import com.thepastimers.Metrics.Metrics;
 import com.thepastimers.Permission.Permission;
 import com.thepastimers.Rank.Rank;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.vanish.VanishManager;
 import org.kitteh.vanish.VanishPlugin;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/13/12
  * Time: 8:05 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ExtCommands extends JavaPlugin implements Listener {
     Permission permission;
     Metrics metrics;
     Rank rank;
     Chat chat;
     VanishPlugin vanishNoPacket;
     VanishManager manager;
 
     @Override
     public void onEnable() {
         getLogger().info("ExtCommands init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
 
         if (permission == null) {
             getLogger().warning("Unable to load Permission plugin.");
         }
 
         metrics = (Metrics)getServer().getPluginManager().getPlugin("Metrics");
 
         if (metrics == null) {
             getLogger().warning("Unable to load Metrics plugin.");
         }
 
         rank = (Rank)getServer().getPluginManager().getPlugin("Rank");
         if (rank == null) {
             getLogger().warning("Unable to load Rank plugin.");
         }
 
         chat = (Chat)getServer().getPluginManager().getPlugin("Chat");
         if (chat == null) {
             getLogger().warning("Unable to load Chat plugin.");
         } else {
             chat.registerCommand("setTitle",ExtCommands.class,this);
         }
 
         vanishNoPacket = (VanishPlugin)getServer().getPluginManager().getPlugin("VanishNoPacket");
         if (vanishNoPacket == null) {
             getLogger().warning("Unable to load VanishNoPacket plugin.");
         }
 
         manager = vanishNoPacket.getManager();
 
         getLogger().info("ExtCommands init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("ExtCommands disabled");
     }
 
     @EventHandler
     public void explosion(EntityExplodeEvent event) {
         if (event.getEntityType() == EntityType.CREEPER) {
             event.blockList().clear();
         }
     }
 
     @EventHandler
     public void blockBreak(BlockBreakEvent event) {
         Player p = event.getPlayer();
         if (permission == null || !permission.hasPermission(p.getName(),"command_warn_break")) {
             return;
         }
 
         if (!event.isCancelled()) {
             ItemStack is = p.getItemInHand();
             if (is.getType() == Material.WOOD_AXE || is.getType() == Material.WOOD_HOE || is.getType() == Material.WOOD_PICKAXE || is.getType() == Material.WOOD_SPADE ||
                 is.getType() == Material.STONE_AXE || is.getType() == Material.STONE_HOE || is.getType() == Material.STONE_PICKAXE || is.getType() == Material.STONE_SPADE ||
                 is.getType() == Material.IRON_AXE || is.getType() == Material.IRON_HOE || is.getType() == Material.IRON_PICKAXE || is.getType() == Material.IRON_SPADE ||
                 is.getType() == Material.GOLD_AXE || is.getType() == Material.GOLD_HOE || is.getType() == Material.GOLD_PICKAXE || is.getType() == Material.GOLD_SPADE ||
                 is.getType() == Material.DIAMOND_AXE || is.getType() == Material.DIAMOND_HOE || is.getType() == Material.DIAMOND_PICKAXE || is.getType() == Material.DIAMOND_SPADE) {
                 short durab = is.getDurability();
                 durab ++;
                 short max = is.getType().getMaxDurability();
                 if (durab >= max-3 && durab <= max) {
                     p.sendMessage(ChatColor.RED + "Warning, your " + is.getType().name() + " will break soon! (" + durab + "/" + max + ")");
                 }
             }
         }
     }
 
     public void handleCommand(CommandData data) {
         String response = "";
         String command = data.getCommand();
         if ("setTitle".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(data.getPlayer(),"title_self")) {
                 data.setResponse(":red:You do not have permission to use this command (title_self)");
                 return;
             }
 
             if (data.getArgumentArray().length > 0) {
                 StringBuilder title = new StringBuilder();
                 for (int i=0;i<data.getArgumentArray().length;i++) {
                     title.append(data.getArgumentArray()[i]);
                     if (i < data.getArgumentArray().length-1) {
                         title.append(" ");
                     }
                 }
 
                 if (rank == null) {
                     data.setResponse("This command is not currently available");
                     return;
                 }
 
                 if (!rank.setTitle(data.getPlayer(),title.toString())) {
                     response = ":red:Unable to set title";
                 } else {
                     response = ":green:Title changed!";
                 }
             } else {
                 response = ":red:/setTitle <title>";
             }
         } else {
             response = ":red:Unknown command";
         }
         data.setResponse(response);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("spawn")) {
             if (permission == null || !permission.hasPermission(playerName,"command_spawn") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             Player p = getServer().getPlayer(playerName);
 
             World w = getServer().getWorld("world");
 
             p.teleport(w.getSpawnLocation());
 
             sender.sendMessage("Teleporting you to spawn.");
         } else if (command.equalsIgnoreCase("tpe")) {
             if (permission == null || !permission.hasPermission(playerName,"command_tpe") || playerName.equalsIgnoreCase("CONSOLE")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (command_tpe)");
                 return true;
             }
 
             if (args.length > 2) {
                 int x,y,z;
                 try {
                     x = Integer.parseInt(args[0]);
                     y = Integer.parseInt(args[1]);
                     z = Integer.parseInt(args[2]);
                 } catch(NumberFormatException e) {
                     sender.sendMessage("Illegal arguments");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
 
                 Location l = new Location(p.getWorld(),x,y,z);
                 p.teleport(l);
 
                 sender.sendMessage("Teleporting!");
             } else {
                 sender.sendMessage("/tpe <x> <y> <z>");
             }
         } else if (command.equalsIgnoreCase("setspawn")) {
             if (permission == null || !permission.hasPermission(playerName,"command_setspawn") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             Player p = getServer().getPlayer(playerName);
 
             Location l = p.getLocation();
             p.getWorld().setSpawnLocation(l.getBlockX(),l.getBlockY(),l.getBlockZ());
 
             sender.sendMessage("Set spawn for this world to (" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")");
         } else if (command.equalsIgnoreCase("clear")) {
             if (permission == null || !permission.hasPermission(playerName,"command_clear")) {
                 sender.sendMessage("You do not have permission to use this command (command_clear)");
                 return true;
             }
 
             if (args.length > 0) {
                 String subCommand = args[0];
 
                 if ("mobs".equalsIgnoreCase(subCommand)) {
                     if (args.length > 1) {
                         String world = args[1];
 
                         World w = getServer().getWorld(world);
                         if (w == null) {
                             sender.sendMessage(ChatColor.RED + "World '" + world + "' does not exist.");
                             return true;
                         }
 
                         List<Entity> entities = getServer().getWorld(world).getEntities();
 
                         int count = 0;
 
                         for (Entity entity : entities) {
                             if (entity.getType() == EntityType.BLAZE ||
                                     entity.getType() == EntityType.ZOMBIE ||
                                     entity.getType() == EntityType.CAVE_SPIDER ||
                                     entity.getType() == EntityType.CREEPER ||
                                     entity.getType() == EntityType.ENDERMAN ||
                                     entity.getType() == EntityType.GHAST ||
                                     entity.getType() == EntityType.MAGMA_CUBE ||
                                     entity.getType() == EntityType.PIG_ZOMBIE ||
                                     entity.getType() == EntityType.SILVERFISH ||
                                     entity.getType() == EntityType.SKELETON ||
                                     entity.getType() == EntityType.SPIDER ||
                                     entity.getType() == EntityType.WITCH) {
                                 entity.remove();
                                 count ++;
                             }
                         }
 
                         sender.sendMessage("Removed " + count + " entities.");
                     } else {
                         sender.sendMessage("/clear mobs <world>");
                     }
                 } else if ("withers".equalsIgnoreCase(subCommand)) {
                     if (args.length > 1) {
                         String world = args[1];
 
                         World w = getServer().getWorld(world);
                         if (w == null) {
                             sender.sendMessage(ChatColor.RED + "World '" + world + "' does not exist.");
                             return true;
                         }
 
                         List<Entity> entities = getServer().getWorld(world).getEntities();
 
                         int count = 0;
 
                         for (Entity entity : entities) {
                             if (entity.getType() == EntityType.WITHER) {
                                 entity.remove();
                                 count ++;
                             }
                         }
 
                         sender.sendMessage("Removed " + count + " entities.");
                     } else {
                         sender.sendMessage("/clear withers <world>");
                     }
                 } else if ("cats".equalsIgnoreCase(subCommand)) {
                     if (args.length > 1) {
                         String world = args[1];
 
                         World w = getServer().getWorld(world);
                         if (w == null) {
                             sender.sendMessage(ChatColor.RED + "World '" + world + "' does not exist.");
                             return true;
                         }
 
                         List<Entity> entities = getServer().getWorld(world).getEntities();
 
                         int count = 0;
 
                         for (Entity entity : entities) {
                             if (entity.getType() == EntityType.OCELOT) {
                                 entity.remove();
                                 count ++;
                             }
                         }
 
                         sender.sendMessage("Removed " + count + " entities.");
                     } else {
                         sender.sendMessage("/clear cats <world>");
                     }
                 }
             } else {
                 sender.sendMessage("/clear <mobs|withers|cats>");
             }
         } else if (command.equalsIgnoreCase("itemCheck")) {
             if (permission == null || !permission.hasPermission(playerName,"command_item_check")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (command_item_check)");
                 return true;
             }
 
             Player p  = (Player)sender;
             ItemStack s = p.getItemInHand();
             if (s == null) {
                 sender.sendMessage(ChatColor.GREEN + "You have nothing in your hand");
             } else {
                 sender.sendMessage(ChatColor.GREEN + "The item in your hand is: " + s.getType().name() + ", durability " + s.getDurability() + "/" + s.getType().getMaxDurability());
             }
         } else if ("opCheck".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"command_op_check")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (command_op_check)");
                 return true;
             }
 
             if (args.length > 0) {
                 String player = args[0];
                 Player p = getServer().getPlayer(player);
 
                 boolean broadcast = false;
                 if (args.length > 1) {
                     if ("broadcast".equalsIgnoreCase(args[1])) {
                         broadcast = true;
                         sender.sendMessage("Broadcasting op result to server");
                     }
                 }
 
                 if (p == null) {
                     sender.sendMessage(ChatColor.RED + player + " is not a valid player.");
                 } else {
                     String message = ChatColor.GREEN + "[OPCHECK]: " + ChatColor.WHITE + sender.getName() + " has run op check on " + player;
                     if (!broadcast) sender.sendMessage(message);
                     if (broadcast) getServer().broadcastMessage(message);
                     if (p.isOp()) {
                         message = ChatColor.GREEN + "[OPCHECK]: " + ChatColor.WHITE + player + ChatColor.GREEN + " is " + ChatColor.WHITE + "an op";
                         if (!broadcast) sender.sendMessage(message);
                         if (broadcast) getServer().broadcastMessage(message);
                     } else {
                         message = ChatColor.GREEN + "[OPCHECK]: " + ChatColor.WHITE + player + ChatColor.RED + " is not " + ChatColor.WHITE + "an op";
                         if (!broadcast) sender.sendMessage(message);
                         if (broadcast) getServer().broadcastMessage(message);
                     }
                 }
 
             } else {
                 sender.sendMessage(ChatColor.RED + "/opCheck <player> <broadcast>");
             }
         } else if ("pot".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"command_pot")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (command_pot)");
                 return true;
             }
 
             if ("Kurama_09".equalsIgnoreCase(playerName)) {
                 Player p = (Player)sender;
                 p.kickPlayer("POT POT POT POT POT");
             } else {
                 sender.sendMessage("This command added for Kurama_09");
             }
         } else if ("setTitle".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"title_self")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (title_self)");
                 return true;
             }
 
             if (args.length > 0) {
                 StringBuilder title = new StringBuilder();
                 for (int i=0;i<args.length;i++) {
                     title.append(args[i]);
                     if (i < args.length-1) {
                         title.append(" ");
                     }
                 }
 
                 if (rank == null) {
                     sender.sendMessage(ChatColor.RED + "This command is not currently available");
                     return true;
                 }
 
                 if (!rank.setTitle(playerName,title.toString())) {
                     sender.sendMessage(ChatColor.RED + "Unable to set title");
                 }
             } else {
                 sender.sendMessage(ChatColor.RED + "/setTitle <title>");
             }
         } else if ("hide".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"command_vanish") || "CONSOLE".equalsIgnoreCase(playerName)) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (command_vanish)");
                 return true;
             }
 
             Player p = (Player)sender;
             if (manager != null) {
                 manager.toggleVanish(p);
             }
         } else {
             return false;
         }
 
         return true;
     }
 }
