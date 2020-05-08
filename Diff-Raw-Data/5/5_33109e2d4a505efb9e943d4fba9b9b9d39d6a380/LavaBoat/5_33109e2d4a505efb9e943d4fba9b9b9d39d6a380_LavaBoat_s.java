 package org.lavaboat;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LavaBoat extends JavaPlugin{
 	public static LavaBoat plugin;
 	private static final Logger log = Logger.getLogger("Minecraft");
 	private final LBvehiclelistener vehiclelistener = new LBvehiclelistener(this);
 	private final LBentityListener entitylistener = new LBentityListener(this);
 	private final LBPlayerListener playerlistener = new LBPlayerListener(this);
 	private ArrayList<Player> disabledWalk = new ArrayList<Player>();
 	public static LBConfig config ;
	public static final String pref = "[LavaBoat] 0.9 ";
 	
 	/**
 	 * 
 	 * @param p is the specified player
 	 * @return if the specified player can use boats
 	 */
 	public boolean canUseB(Player p) {
         if(p.hasPermission("lb.boats"))
             	return true;
         else if(p.hasPermission("lb.*"))
             	return true;
         return p.isOp();
     }
 	/**
 	 * 
 	 * @param p is the specified player
 	 * @return if the specified player can manage worlds enablement
 	 */
 	public boolean canManageWorlds(Player p) {
         if(p.hasPermission("lb.world"))
             	return true;
         else if(p.hasPermission("lb.*"))
             	return true;
         return p.isOp();
     }
 	/**
 	 * 
 	 * @param p is the specified player
 	 * @return if the specified player can use pigs
 	 */
 	/*public boolean canUseP(Player p) {
         if(p.hasPermission("lavaboat.use.pigs")){
             	return true;
         }
         else if(p.hasPermission("lavaboat.use.*")){
             	return true;
         }
         return p.isOp();
     }*/
 	/**
 	 * 
 	 * @param p is the specified player
 	 * @return if the specified player can use minecarts
 	 */
 	public boolean canUseM(Player p) {
 		if(p.hasPermission("lb.minecarts"))
         	return true;
 		else if(p.hasPermission("lb.*"))
 			return true;
 		return p.isOp();
 	}
 	/**
 	 * 
 	 * @param p is the specified player
 	 * @return if the specified player can walk on lava
 	 */
 	public boolean canWalk(Player p) {
 		if(disabledWalk.contains(p))
 			return false;
 		if(p.hasPermission("lb.walk"))
         	return true;
 		else if(p.hasPermission("lb.*"))
         	return true;
 		return p.isOp();
 	}
 	
 	public void onDisable() {
 		log.info(pref+"has been disabled");
 	}
 
 	
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(vehiclelistener, this);
 		getServer().getPluginManager().registerEvents(entitylistener, this);
 		getServer().getPluginManager().registerEvents(playerlistener, this);
 		config = new LBConfig(this);
 		log.info(pref+"has been enabled");
 	}
 	//Not working why ?
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("lb")&&args.length==1){ 
 			if(args[0].equalsIgnoreCase("enable")&& sender instanceof Player){
 				Player p = (Player) sender;
 				if(!canManageWorlds(p)){
 					sender.sendMessage(ChatColor.RED+"You do not have acces to that feature.");
 					return true;
 				}
 				config.addWorld(p.getWorld().getName());
 				sender.sendMessage(ChatColor.GREEN+pref+" is now enabled in this world");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("disable")&& sender instanceof Player){
 				Player p = (Player) sender;
 				if(!canManageWorlds(p)){
 					sender.sendMessage(ChatColor.RED+"You do not have acces to that feature.");
 					return true;
 				}
 				config.removeWorld(p.getWorld().getName());
 				sender.sendMessage(ChatColor.RED+pref+" is now disabled in this world");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("walk")&& sender instanceof Player){
 				Player p = (Player) sender;
				if(!canWalk(p)){
 					sender.sendMessage(ChatColor.RED+"You do not have acces to that feature.");
 					return true;
 				}
 				if(disabledWalk.contains(p)){
 					disabledWalk.remove(p);
 					sender.sendMessage(ChatColor.GOLD+"Lava walking is now OFF ");
 				}
 				else{
 					disabledWalk.add(p);
 					sender.sendMessage(ChatColor.GOLD+"Lava walking is now ON");
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }
