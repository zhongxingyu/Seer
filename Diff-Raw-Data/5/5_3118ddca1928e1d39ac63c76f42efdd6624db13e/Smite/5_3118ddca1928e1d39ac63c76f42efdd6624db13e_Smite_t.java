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
 			   Block targetblock = player.getTargetBlock(null,  50);
 			   Location location = targetblock.getLocation();
 			   world.strikeLightning(location);
 			   //get "blast-radius" config, defualt to 10 if none set. Thanks to morganm for the help :) no good deed goes un-noticed
 			   int radius = getConfig().getInt("blast-radius", 10);
 			   world.createExplosion(location, radius);
 			   //To spawn a creeper no idea how this will work.. Will eventually include a var in the config.
 			   world.spawnCreature(location, org.bukkit.entity.EntityType.CREEPER);
 			   world.spawnCreature(location, org.bukkit.entity.EntityType.PIG_ZOMBIE);
 			   //next line is for the player variable. /smite [playername] ~added in V1.0
 		   } else if (args.length == 1) {
 			   if(player.getServer().getPlayer(args[0]) != null) {
 				   Player targetplayer = player.getServer().getPlayer(args[0]);
 				   Location location = targetplayer.getLocation();
 				   world.strikeLightning(location);
 				   //get "blast-player" config, defualt to 0 if none set, set 0 to create no explosion. Thanks to morganm for the help :) no good deed goes un-noticed
 				   int radius = getConfig().getInt("blast-player", 0);
 				   world.createExplosion(location, radius);
 				   //Spawns pig zombie ~Comp ~Added in V3.0
 				   world.spawnCreature(location, org.bukkit.entity.EntityType.PIG_ZOMBIE);
 				   //Shows spawner effect ~Comp ~Added in V3.0
 				   world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
 				   player.sendMessage(ChatColor.GRAY + "Smiting Player " + targetplayer.getDisplayName());
 			   } else {
 				   player.sendMessage(ChatColor.RED + "Error: The player is offline please use a different player. ");
 				   getServer().broadcastMessage(getName());
 			   }
 		   } else if (args.length > 1) {
 			   player.sendMessage(ChatColor.RED + "Error: Too Many Args!");
 		   }
 	   }
 	   return false;
    }
 }
