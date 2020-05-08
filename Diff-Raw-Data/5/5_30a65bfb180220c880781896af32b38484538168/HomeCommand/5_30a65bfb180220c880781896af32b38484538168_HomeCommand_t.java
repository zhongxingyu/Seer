 package com.norcode.bukkit.telewarp.commands.home;
 
 import com.norcode.bukkit.telewarp.MetaKeys;
 import com.norcode.bukkit.telewarp.PendingTeleport;
 import com.norcode.bukkit.telewarp.Telewarp;
 import com.norcode.bukkit.telewarp.commands.BaseCommand;
 import com.norcode.bukkit.telewarp.persistence.home.Home;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.material.Bed;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class HomeCommand extends BaseCommand {
     private BlockFace[] bedBlockCheckDirections = new BlockFace[] { BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST,
             BlockFace.SOUTH};
     private BlockFace[] bedsideCheckDirections = new BlockFace[] {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST,
             BlockFace.SOUTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.UP};
 
     public HomeCommand(Telewarp plugin) {
         super(plugin, "home");
         allowConsole = false;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, LinkedList<String> args) {
         Player player = (Player) sender;
         Map<String, Home> homes = plugin.getHomeManager().getHomesFor(sender.getName());
         Home targetHome = null;
         if (args.size() == 0) {
             targetHome = homes.get("home");
         } else if (args.size() == 1 && args.get(0).equalsIgnoreCase("list")) {
             return plugin.homesCommand.onCommand(sender, plugin.getServer().getPluginCommand("homes"), "home list", new LinkedList<String>());
         } else if (sender.hasPermission("telewarp.command.home.others") && args.peek().contains(":")) {
             String[] parts = args.pop().split(":");
             homes = plugin.getHomeManager().getHomesFor(parts[0]);
             List<String> matches = new ArrayList<String>();
             for (String n: homes.keySet()) {
                 if (n.startsWith(parts[1].toLowerCase())) {
                     matches.add(n);
                 }
             }
             if (matches.size() == 1) {
                 targetHome = homes.get(matches.get(0));
             } else {
                 sender.sendMessage(plugin.getMsg("home-not-found", args.peek()));
             }
         } else {
             List<String> matches = new ArrayList<String>();
             for (String n: homes.keySet()) {
                 if (n.startsWith(args.peek().toLowerCase())) {
                     matches.add(n);
                 }
             }
             if (matches.size() == 1) {
                 targetHome = homes.get(matches.get(0));
             } else {
                 sender.sendMessage(plugin.getMsg("home-not-found", args.peek()));
             }
         }
 
         if (targetHome == null) {
             sender.sendMessage(plugin.getMsg("no-home-location"));
             return true;
         }
 
         Location loc = new Location(plugin.getServer().getWorld(targetHome.getWorld()), targetHome.getX(),
                 targetHome.getY(), targetHome.getZ(), targetHome.getYaw(), targetHome.getPitch());
 
         if (!loc.getChunk().isLoaded()) {
             loc.getChunk().load(true);
         }
 
         if (!loc.getBlock().getType().equals(Material.BED_BLOCK)) {
             player.sendMessage(plugin.getMsg("bed-missing"));
             plugin.getHomeManager().delHome(targetHome);
             return true;
         }
 
         Location safeSpot = findSafeSpot(loc.getBlock());
 
         if (safeSpot == null) {
             player.sendMessage(plugin.getMsg("no-safe-home-spawn"));
             return true;
         }
         Location lookingLoc = Telewarp.lookAt(safeSpot.getBlock().getRelative(BlockFace.UP).getLocation(), new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5));
         safeSpot.setPitch(lookingLoc.getPitch());
         safeSpot.setYaw(lookingLoc.getYaw());
         plugin.setPlayerMeta(player, MetaKeys.TELEPORT_TYPE, this.getName());
         player.teleport(safeSpot, PlayerTeleportEvent.TeleportCause.COMMAND);
         return true;
     }
 
 
     private Block getOtherBedBlock(Block b) {
         Bed bed = (Bed) b.getState().getData();
         if (bed.isHeadOfBed()) {
             return b.getRelative(bed.getFacing().getOppositeFace());
         } else {
             return b.getRelative(bed.getFacing());
         }
     }
 
     private Location findSafeSpot(Block block) {
         Block b2 = getOtherBedBlock(block);
         for (BlockFace dir: bedsideCheckDirections) {
             if (isSafe(block.getRelative(dir))) {
                return block.getRelative(dir).getLocation().add(0.5, 0.1, 0.5);
             } else if (isSafe(b2.getRelative(dir))) {
                return b2.getRelative(dir).getLocation().add(0.5, 0.1, 0.5);
             }
         }
         return null;
     }
 
     private boolean isSafe(Block feetBlock) {
         if (!feetBlock.isEmpty()) {
             return false;
         }
         if (!feetBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
             return false;
         }
         if (!feetBlock.getRelative(BlockFace.UP).isEmpty()) {
             return false;
         }
         return true;
     }
 
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, LinkedList<String> args) {
         List<String> results = new LinkedList<String>();
         Map<String, Home> homes = plugin.getHomeManager().getHomesFor(sender.getName());
         if (sender.hasPermission("telewarp.commands.home.others")) {
             if (args.peek().contains(":")) {
                 String[] parts = args.pop().split(":");
                 homes = plugin.getHomeManager().getHomesFor(parts[0]);
                 if (homes != null && !homes.isEmpty()) {
                     for (Home h: homes.values()) {
                         if (h.getName().startsWith(parts[1].toLowerCase())) {
                             results.add(parts[0] + ":" + h.getName());
                         }
                     }
                 }
             } else {
                 for (Home h: homes.values()) {
                     if (h.getName().startsWith(args.peek().toLowerCase())) {
                         results.add(h.getName());
                     }
                 }
                 for (OfflinePlayer p: plugin.getServer().getOnlinePlayers()) {
                     if (p.getName().startsWith(args.peek().toLowerCase())) {
                         results.add(p.getName());
                     }
                 }
             }
         } else {
 
             for (Home h: homes.values()) {
                 if (h.getName().toLowerCase().startsWith(args.peekLast().toLowerCase())) {
                     results.add(h.getName());
                 }
             }
         }
         return results;
     }
 }
