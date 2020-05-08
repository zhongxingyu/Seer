 package com.thepastimers.Edit;
 
 import com.thepastimers.Coord.Coord;
 import com.thepastimers.Coord.CoordData;
 import com.thepastimers.Permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Stairs;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/14/12
  * Time: 4:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Edit extends JavaPlugin {
     Permission permission;
     Coord coord;
 
     @Override
     public void onEnable() {
         getLogger().info("Edit init");
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
 
         if (permission == null) {
             getLogger().warning("Unable to load Permission plugin");
         }
 
         coord = (Coord)getServer().getPluginManager().getPlugin("Coord");
 
         if (coord == null) {
             getLogger().warning("Unable to load Coord plugin");
         }
 
         getLogger().info("Edit init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Edit disable");
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
 
         if (command.equalsIgnoreCase("check")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_check") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             if (coord == null) {
                 sender.sendMessage("This functionality is not currently available");
                 return true;
             }
 
             List<CoordData> coords = coord.popCoords(playerName,1);
 
             if (coords.size() < 1) {
                 sender.sendMessage("You must have 1 coord set in order to do this");
                 return true;
             }
 
             sender.sendMessage("You now have " + coord.getCoordSize(playerName) + " coords set");
 
             CoordData d = coords.get(0);
 
             int x = (int)d.getX();
             int y = (int)d.getY();
             int z = (int)d.getZ();
 
             Player p = getServer().getPlayer(playerName);
 
             World w = p.getWorld();
 
             Block b = w.getBlockAt(x,y,z);
 
             if (b != null) {
                 sender.sendMessage(ChatColor.GREEN + "Block at (" + x + "," + y + "," + z + "): " + b.getType().name() + " with data " + b.getData());
             }
         } else if (command.equalsIgnoreCase("fill")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command (edit_fill)");
                 return true;
             }
 
             if (args.length > 0) {
                 List<Material> materials = new ArrayList<Material>();
                 boolean error = false;
                 for (int i=0;i<args.length;i++) {
                     String name = args[i];
                     Material m = Material.getMaterial(name);
                     if (m == null) {
                         sender.sendMessage(name + " is unknown material");
                         error = true;
                     } else {
                         materials.add(m);
                     }
                 }
                 if (error) return true;
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 int x,y,z;
 
                 for (x=x1;x<=x2;x++) {
                     for (y=y1;y<=y2;y++) {
                         for (z=z1;z<=z2;z++) {
                             Block b = w.getBlockAt(x,y,z);
 
                             int rand = (int)(Math.random()*materials.size()-1);
 
                             Material m = materials.get(rand);
 
                             b.setType(m);
 
                             sender.sendMessage("(" + x + "," + y + "," + z + ")");
                         }
                     }
                 }
             } else {
                 sender.sendMessage("/fill <material>");
             }
         } else if(command.equalsIgnoreCase("pyramid")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             if (args.length > 0) {
                 String name = args[0];
 
                 Material m = Material.getMaterial(name);
 
                 if (m == null) {
                     sender.sendMessage("Unknown material");
                     return true;
                 }
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 int x,y,z;
 
                 y2 = y1+Math.abs(x1-x2)/2;
                 int radius = 0;
 
                 for (y=y1;y<=y2;y++) {
                     z = z1+radius;
                     for (x=x1+radius;x<=x2-radius;x++) {
                         Block b = w.getBlockAt(x,y,z);
 
                         b.setType(m);
 
                         sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     }
                     z = z2-radius;
                     for (x=x1+radius;x<=x2-radius;x++) {
                         Block b = w.getBlockAt(x,y,z);
 
                         b.setType(m);
 
                         sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     }
                     x = x1+radius;
                     for (z=z1+radius;z<=z2-radius;z++) {
                         Block b = w.getBlockAt(x,y,z);
 
                         b.setType(m);
 
                         sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     }
                     x = x2-radius;
                     for (z=z1+radius;z<=z2-radius;z++) {
                         Block b = w.getBlockAt(x,y,z);
 
                         b.setType(m);
 
                         sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     }
                     radius ++;
                 }
             } else {
                 sender.sendMessage("/pyramid <material>");
             }
         } else if(command.equalsIgnoreCase("pyramidlegs")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             if (args.length > 0) {
                 String name = args[0];
 
                 Material m = Material.getMaterial(name);
 
                 if (m == null) {
                     sender.sendMessage("Unknown material");
                     return true;
                 }
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 int x,y,z;
 
                 y2 = y1+Math.abs(x1-x2)/2;
                 int radius = 0;
 
                 for (y=y1;y<=y2;y++) {
                     z = z1+radius;
 
                     x=x1+radius;
                     Block b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     x=x2-radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
 
                     z = z2-radius;
                     x=x1+radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     x=x2-radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
 
                     x = x1+radius;
                     z=z1+radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     z=z2-radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
 
                     x = x2-radius;
                     z=z1+radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
                     z=z2-radius;
                     b = w.getBlockAt(x,y,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y + "," + z + ")");
 
                     radius ++;
                 }
             } else {
                 sender.sendMessage("/pyramid <material>");
             }
         } else if (command.equalsIgnoreCase("filldiag")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             if (args.length > 0) {
                 String name = args[0];
 
                 Material m = Material.getMaterial(name);
 
                 if (m == null) {
                     sender.sendMessage("Unknown material");
                     return true;
                 }
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 int x,y,z;
 
                 y2 = y1+Math.abs(x1-x2)/2;
                 int radius = 0;
 
                 for (y=y1;y<=y2;y++) {
                     z = z1+radius;
                     x=x1+radius;
                     Block b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
                     x=x2-radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
 
                     z = z2-radius;
                     x=x1+radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
                     x=x2-radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
 
                     x = x1+radius;
                     z=z1+radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
                     z=z2-radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
 
                     x = x2-radius;
                     z=z1+radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
                     z=z2-radius;
                     b = w.getBlockAt(x,y1,z);
                     b.setType(m);
                     sender.sendMessage("(" + x + "," + y1 + "," + z + ")");
 
                     radius ++;
                 }
             } else {
                 sender.sendMessage("/filldiag <material>");
             }
         } else if (command.equalsIgnoreCase("shift")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_shift") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command (edit_shift)");
                 return true;
             }
 
             if (args.length > 2) {
                 int shiftX = Integer.parseInt(args[0]);
                 int shiftY = Integer.parseInt(args[1]);
                 int shiftZ = Integer.parseInt(args[2]);
 
                 Material fill = Material.AIR;
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 for (int x=x1;x<=x2;x++) {
                     for (int y=y2;y>=y1;y--) {
                         for (int z=z1;z<=z2;z++) {
                             Block b = w.getBlockAt(x,y,z);
 
                             sender.sendMessage("Block at (" + x + "," + y + "," + z + ") is of type " + b.getType().name());
 
                             Material type = b.getType();
 
                             Block b2 = w.getBlockAt(x+shiftX,y+shiftY,z+shiftZ);
 
                             b2.setType(type);
 
                             b.setType(fill);
                         }
                     }
                 }
             } else {
                 sender.sendMessage("/shift <x> <y> <z>");
             }
         } else if ("encase".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command (edit_fill)");
                 return true;
             }
 
             if (args.length > 0) {
                 String name = args[0];
                 Player p = getServer().getPlayer(name);
 
                 if (p == null) {
                     sender.sendMessage(ChatColor.RED + "Can't find that player");
                     return true;
                 }
 
                 Location l = p.getLocation();
 
                 int x = l.getBlockX();
                 int y = l.getBlockY();
                 int z = l.getBlockZ();
 
                 World w = p.getWorld();
 
                 Map<Block,Material> blocks = new HashMap<Block,Material>();
 
                 blocks.put(w.getBlockAt(x+1,y,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x-1,y,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y,z+1),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y,z-1),Material.COBBLESTONE);
 
                 blocks.put(w.getBlockAt(x+1,y+1,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x-1,y+1,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y+1,z+1),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y+1,z-1),Material.COBBLESTONE);
 
                 blocks.put(w.getBlockAt(x+1,y+2,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x-1,y+2,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y+2,z+1),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y+2,z-1),Material.COBBLESTONE);
 
                 blocks.put(w.getBlockAt(x,y-1,z),Material.COBBLESTONE);
                 blocks.put(w.getBlockAt(x,y+3,z),Material.COBBLESTONE);
 
                 for (Block b : blocks.keySet()) {
                     if (b.getType() == Material.AIR) {
                         b.setType(blocks.get(b));
                     }
                 }
             } else {
                 sender.sendMessage(ChatColor.RED + "/encase <player name>");
             }
         } else if (command.equalsIgnoreCase("replace")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             if (args.length > 1) {
                 String name = args[0];
                 Material from = Material.getMaterial(name);
                 if (from == null) {
                     sender.sendMessage(name + " is unknown material");
                     return true;
                 }
                 name = args[1];
                 Material to = Material.getMaterial(name);
                 if (to == null) {
                     sender.sendMessage(name + " is unknown material");
                     return true;
                 }
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 int x,y,z;
 
                 for (x=x1;x<=x2;x++) {
                     for (y=y1;y<=y2;y++) {
                         for (z=z1;z<=z2;z++) {
                             Block b = w.getBlockAt(x,y,z);
 
                             if (b.getType() == from){
                                 b.setType(to);
 
                                 sender.sendMessage("(" + x + "," + y + "," + z + ")");
                             }
                         }
                     }
                 }
             } else {
                 sender.sendMessage("/fill <from material> <to material>");
             }
         } else if (command.equalsIgnoreCase("extend")) {
             if (permission == null || !permission.hasPermission(playerName,"edit_fill") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command");
                 return true;
             }
 
             if (args.length > 1) {
                 String name = args[0];
                 Material from = Material.getMaterial(name);
                 if (from == null) {
                     sender.sendMessage(name + " is unknown material");
                     return true;
                 }
                 String dir = args[1];
 
                 List<CoordData> coords = coord.popCoords(playerName,2);
 
                 if (coords.size() < 2) {
                     sender.sendMessage("You must have 2 coords set in order to do this");
                     return true;
                 }
 
                 Player p = getServer().getPlayer(playerName);
                 World w = p.getWorld();
 
                 CoordData c1 = coords.get(0);
                 CoordData c2 = coords.get(1);
 
                 int x1 = (int)c1.getX();
                 int y1 = (int)c1.getY();
                 int z1 = (int)c1.getZ();
 
                 int x2 = (int)c2.getX();
                 int y2 = (int)c2.getY();
                 int z2 = (int)c2.getZ();
 
                 if (x1 > x2) {
                     int tmp = x1;
                     x1 = x2;
                     x2 = tmp;
                 }
 
                 if (y1 > y2) {
                     int tmp = y1;
                     y1 = y2;
                     y2 = tmp;
                 }
 
                 if (z1 > z2) {
                     int tmp = z1;
                     z1 = z2;
                     z2 = tmp;
                 }
 
                 int x,y,z;
 
                 for (x=x1;x<=x2;x++) {
                     for (y=y1;y<=y2;y++) {
                         for (z=z1;z<=z2;z++) {
                             Block b = w.getBlockAt(x,y,z);
 
                             if (b.getType() == from){
                                 if ("+y".equalsIgnoreCase(dir)) {
                                     for (int i=y;i<=y2;i++) {
                                         sender.sendMessage("(" + x + "," + i + "," + z + ")");
                                         Block b2 = w.getBlockAt(x,i,z);
                                         b2.setType(from);
                                     }
                                 } else if ("+x".equalsIgnoreCase(dir)) {
                                     for (int i=x;i<=x2;i++) {
                                         sender.sendMessage("(" + i + "," + y + "," + z + ")");
                                         Block b2 = w.getBlockAt(i,y,z);
                                         b2.setType(from);
                                     }
                                 } else if ("-z".equalsIgnoreCase(dir)) {
                                     for (int i=z;i<=z2;i++) {
                                         sender.sendMessage("(" + x + "," + y + "," + i + ")");
                                         Block b2 = w.getBlockAt(x,y,i);
                                         b2.setType(from);
                                     }
                                 } else if ("-y".equalsIgnoreCase(dir)) {
                                     for (int i=y;i>=y1;i--) {
                                         sender.sendMessage("(" + x + "," + i + "," + z + ")");
                                         Block b2 = w.getBlockAt(x,i,z);
                                         b2.setType(from);
                                     }
                                 } else if ("-x".equalsIgnoreCase(dir)) {
                                     for (int i=x;i>=x1;i--) {
                                         sender.sendMessage("(" + i + "," + y + "," + z + ")");
                                         Block b2 = w.getBlockAt(i,y,z);
                                         b2.setType(from);
                                     }
                                 } else if ("-z".equalsIgnoreCase(dir)) {
                                     for (int i=z;i>=z1;i--) {
                                         sender.sendMessage("(" + x + "," + y + "," + i + ")");
                                         Block b2 = w.getBlockAt(x,y,i);
                                         b2.setType(from);
                                     }
                                 }
                             }
                         }
                     }
                 }
             } else {
                 sender.sendMessage("/extend <material> <x|y|z (direction)>");
             }
         } else {
             return false;
         }
 
         return true;
     }
 }
