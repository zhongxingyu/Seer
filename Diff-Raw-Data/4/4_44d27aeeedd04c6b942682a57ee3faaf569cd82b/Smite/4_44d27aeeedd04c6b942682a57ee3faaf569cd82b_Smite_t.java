 package me.comp.Smite;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Smite extends JavaPlugin {
    public static Smite plugin;
    public final Logger logger = Logger.getLogger("Minecraft");
    public String deathMessage = "Boom";
  
    public void onDisable() {
 	  PluginDescriptionFile pdffile = this.getDescription();
 	  this.logger.info(pdffile.getName() + "is now disabled.");
    }
  
    public void onEnable() {
 	  PluginDescriptionFile pdffile = this.getDescription();
 	  this.logger.info(pdffile.getName() + "is now enabled.");
 	  getConfig().options().copyDefaults(true);
 	  this.saveDefaultConfig();
    }
 
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 	   Player player = (Player) sender;
 	   World world = player.getWorld();
 	   this.deathMessage = "boom";
 	   if(commandLabel.equalsIgnoreCase("smite")) {
 		   if(args.length == 0) {
 			   //permissions line, op only for now. ~Comp added v. 3.6P
 			   if(player.hasPermission("smite.ground")) {
 			   Block targetblock = player.getTargetBlock(null,  50);
 			   Location location = targetblock.getLocation();
 			   world.strikeLightning(location);
 			   //get "blast-radius" config, defualt to 10 if none set. Thanks to morganm for the help :) no good deed goes un-noticed
 			   int radius = getConfig().getInt("blast-radius", 10);
 			   world.createExplosion(location, radius);
 			   //To spawn a creeper no idea how this will work.. Will eventually include a var in the config.
 			   //world.spawnCreature(location, org.bukkit.entity.EntityType.CREEPER);
 			   world.spawnEntity(location, org.bukkit.entity.EntityType.CREEPER);
 			   world.spawnEntity(location, org.bukkit.entity.EntityType.PIG_ZOMBIE);
 			   //world.spawnCreature(location, org.bukkit.entity.EntityType.PIG_ZOMBIE);
 			   world.createExplosion(location, radius, isEnabled());
 			   }else{
 				   player.sendMessage("You do not have permission to do this");
 			   }
 			   //next line is for the player variable. /smite [playername] ~added in V1.0
 		   } else if (args.length == 1) {
 			   if(player.getServer().getPlayer(args[0]) != null) {
 				   Player targetplayer = player.getServer().getPlayer(args[0]);
 				   Location location = targetplayer.getLocation();
 				   if(player.hasPermission("smite.player")) {
 				   world.strikeLightning(location);
 				   //get "blast-player" config, defualt to 0 if none set, set 0 to create no explosion. Thanks to morganm for the help :) no good deed goes un-noticed
 				   int radius = getConfig().getInt("blast-player", 0);
 				   world.createExplosion(location, radius);
 				   world.createExplosion(location, radius, isEnabled());
 				   //Spawns pig zombie ~Comp ~Added in V3.0
 				   world.spawnEntity(location, org.bukkit.entity.EntityType.PIG_ZOMBIE);
 				   //world.spawnCreature(location, org.bukkit.entity.EntityType.PIG_ZOMBIE);
 				   //Shows spawner effect ~Comp ~Added in V3.0
 				   world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
 				   player.sendMessage(ChatColor.GRAY + "Smiting Player " + targetplayer.getDisplayName());
 				   //Should add in name on smite to broadcast to server~Comp V3.2 V3.3 added space
 				   getServer().broadcastMessage(ChatColor.RED + "Smiting Player " + targetplayer.getDisplayName());
 				   //Permission denied line. ~Comp
 				   //New feature in the works.. obv isnt finished yet but when it is this will ensure the player DIES when run.~Comp
		   } else if (commandLabel.equalsIgnoreCase("smitekill")) {
			   if(args.length == 0) {
 			   if(player.getServer().getPlayer(args[0]) != null) {
 				   Location locationthis = targetplayer.getLocation();
 				   if(player.hasPermission("smite.playerkill")) {
 					   world.createExplosion(locationthis, 1);
 					   world.setStorm(isEnabled());
 					   getServer().broadcastMessage(ChatColor.RED + "Killing This Player With DeathSmite: " + targetplayer.getDisplayName());
 				   }
 				   }
 						   
 					   }
 				   }else{
 					   player.sendMessage(ChatColor.RED + "You do not have permission to do this");
 				   }
 			   } else {
 				   player.sendMessage(ChatColor.RED + "Error: The player is offline please use a different player. ");
 			   }
 		   } else if (args.length > 1) {
 			   player.sendMessage(ChatColor.RED + "Error: Too Many Args!");
 		   }
 	   }
 	   return false;
    }
 }
