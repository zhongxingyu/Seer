 package fr.noogotte.useful_commands.command;
 
 import static fr.noogotte.useful_commands.LocationUtil.getDistantLocation;
 import static fr.noogotte.useful_commands.LocationUtil.getTargetBlockLocation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.aumgn.bukkitutils.geom.Vector;
 import fr.aumgn.bukkitutils.geom.Vector2D;
 import fr.aumgn.bukkitutils.util.Util;
 
 @NestedCommands(name = "useful")
 public class WorldCommands extends UsefulCommands {
 
     @Command(name = "seed", min = 0, max = 1)
     public void seed(CommandSender sender, CommandArgs args) {
         List<World> worlds = args.getList(0, World.class).match(sender);
 
         for (World world : worlds) {
             sender.sendMessage(ChatColor.GREEN + "Seed de "
                     + ChatColor.BLUE + world.getName()
                     + ChatColor.GREEN + " : "
                     + ChatColor.BLUE + world.getSeed());
         }
     }
 
     @Command(name = "setspawn", min = 0, max = 2)
     public void setSpawn(CommandSender sender, CommandArgs args) {
         Vector position = args.getVector(0).value(sender);
         World world = args.getWorld(0).value(sender);
         world.setSpawnLocation(
                 position.getBlockX(),
                 position.getBlockY(),
                 position.getBlockZ());
         sender.sendMessage(ChatColor.GREEN + "Vous avez défini le spawn !");
     }
 
     @Command(name = "time", min = 1, max = 2)
     public void time(CommandSender sender, CommandArgs args) {
         String arg = args.get(0);
         List<World> worlds = args.getList(1, World.class).value(sender);
 
         int time;
         if (arg.equalsIgnoreCase("day")) {
             time = 20 * 60;
         } else if (arg.equalsIgnoreCase("night")) {
             time = 20 * 60 * 11;
         } else {
             throw new CommandUsageError(
                     "Argument " + arg + " inconnu.");
         }
 
         for (World world : worlds) {
             world.setTime(time);
             if(time == 20 * 60) {
                Util.broadcast("useful.world.time.broadcast", ChatColor.AQUA + sender.getName()
                         + ChatColor.GOLD + " a mis le jour "
                         + ChatColor.AQUA + world.getName());
             } else if (time == 20 * 60 * 11) {
                Util.broadcast("useful.world.time.broadcast", ChatColor.AQUA + sender.getName()
                         + ChatColor.GOLD + " a mis la nuit "
                         + ChatColor.AQUA + world.getName());
             }
         }
     }
 
     @Command(name = "weather", min = 1, max = 2)
     public void weather(CommandSender sender, CommandArgs args) {
         String arg = args.get(0);
         List<World> worlds = args.getList(1, World.class).value(sender);
 
         boolean storm;
         if (arg.equalsIgnoreCase("sun")) {
             storm = false;
         } else if(arg.equalsIgnoreCase("storm")) {
             storm = true;
         } else {
             throw new CommandUsageError(
                     "Argument " + arg + " inconnu.");
         }
 
         for (World world : worlds) {
             world.setStorm(storm);
             
             if(storm == true) {
                 Util.broadcast("useful.weather.broadcast", ChatColor.AQUA + sender.getName()
                         + ChatColor.GOLD + " a mis la pluit dans "
                         + ChatColor.AQUA + world.getName());
             } else if (storm == false) {
                 Util.broadcast("useful.weather.broadcast", ChatColor.AQUA + sender.getName()
                         + ChatColor.GOLD + " a arrété la pluit dans "
                         + ChatColor.AQUA + world.getName());
             }
         }
     }
 
     @Command(name = "spawnmob", flags = "tp", argsFlags = "d", min = 1, max = 3)
     public void spawnmob(Player sender, CommandArgs args) {
         EntityType entity = args.getEntityType(0).value();
         if (!entity.isSpawnable() && isNotAMob(entity)) {
             throw new CommandError("Vous ne pouvez pas spawner ce type d'entité");
         }
 
         int count = args.getInteger(1).value(1);
         List<Location> locations = new ArrayList<Location>();
         if (args.hasFlag('t')) {
             for (Player target : args.getPlayers(2).value(sender)) {
                 Location location = getTargetBlockLocation(target, 180)
                         .toLocation(sender.getWorld());
                 locations.add(location);
             }
         } else if (args.hasArgFlag('d')) {
             int distance = args.get('d', Integer.class).value();
             for (Player target : args.getPlayers(2).value(sender)) {
                 Location location = getDistantLocation(target, distance)
                         .toLocation(sender.getWorld());
                 locations.add(location);
             }
         } else if (args.hasFlag('p')) {
             Vector2D pos2D = args.getVector2D(2).value();
             Vector pos = pos2D.toHighest(sender.getWorld());
             locations.add(pos.toLocation(sender.getWorld()));
         } else {
             for (Player target : args.getPlayers(2).value(sender)) {
                 locations.add(target.getLocation());
             }
         }
 
         int totalCount = 0;
         for (int i = 0; i < count; i++) {
             for (Location location : locations) {
                 location.getWorld().spawnCreature(location, entity);
                 totalCount++;
             }
         }
 
         sender.sendMessage(ChatColor.GREEN + "Vous avez spawn "
                 + ChatColor.GOLD + totalCount
                 + ChatColor.GREEN + " " + entity.getName());
     }
 
     @Command(name = "removemob", argsFlags = "wcp", min = 0, max = 2)
     public void removemob(CommandSender sender, CommandArgs args) {
         List<EntityType> types;
         if (args.length() == 0 || args.get(0).equals("*")) {
             types = null;
         } else {
             types = args.getList(0, EntityType.class).value();
 
             for (EntityType type : types) {
                 if (isNotAMob(type)) {
                     throw new CommandError(type.getName() + " n'est pas un mob.");
                 }
             }
         }
 
         boolean hasRadius = args.length() > 1;
         int radius = 0;
         Vector from = null;
         World world = null;
 
         if (hasRadius) {
             radius = args.getInteger(1).value(1);
             radius *= radius;
 
             if (args.hasArgFlag('c')) {
                 if (!args.hasArgFlag('w')) {
                     throw new CommandUsageError("Vous devez specifier un monde.");
                 }
                 from = args.get('c', Vector.class).value(); 
                 world = args.get('w', World.class).value();
             } else {
                 Player target = args.get('p', Player.class).value(sender);
                 from = new Vector(target);
                 world = target.getWorld();
             }
         } else {
             if (args.hasArgFlag('p')) {
                 world = args.get('p', Player.class).value().getWorld();
             } else {
                 world = args.get('w', World.class).value(sender);
             }
         }
 
         int count = 0;
         for (Entity entity : world.getEntities()) {
             EntityType entityType = entity.getType();
             if (isNotAMob(entityType)) {
                 continue;
             }
 
             if (types != null
                     && !types.contains(entityType)) {
                 continue;
             }
 
             if (hasRadius
                     && new Vector(entity).distanceSq(from) > radius) {
                 continue;
             }
 
             count++;
             entity.remove();
         }
 
         sender.sendMessage(ChatColor.GREEN + "Vous avez supprimé "
                         + ChatColor.GOLD + count
                         + ChatColor.GREEN + " mobs");
     }
 
     private boolean isNotAMob(EntityType type) {
         return type.equals(EntityType.ARROW)
                 || type.equals(EntityType.BOAT)
                 || type.equals(EntityType.COMPLEX_PART)
                 || type.equals(EntityType.DROPPED_ITEM)
                 || type.equals(EntityType.EGG)
                 || type.equals(EntityType.ENDER_CRYSTAL)
                 || type.equals(EntityType.ENDER_PEARL)
                 || type.equals(EntityType.EXPERIENCE_ORB)
                 || type.equals(EntityType.FALLING_BLOCK)
                 || type.equals(EntityType.FIREBALL)
                 || type.equals(EntityType.FISHING_HOOK)
                 || type.equals(EntityType.LIGHTNING)
                 || type.equals(EntityType.MINECART)
                 || type.equals(EntityType.PAINTING)
                 || type.equals(EntityType.PLAYER)
                 || type.equals(EntityType.PRIMED_TNT)
                 || type.equals(EntityType.SMALL_FIREBALL)
                 || type.equals(EntityType.SNOWBALL)
                 || type.equals(EntityType.SPLASH_POTION)
                 || type.equals(EntityType.THROWN_EXP_BOTTLE)
                 || type.equals(EntityType.UNKNOWN)
                 || type.equals(EntityType.WEATHER);
     }
 }
