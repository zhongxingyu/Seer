 package me.croxis.plugins.tech;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.croxis.plugins.research.TechManager;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Tech extends JavaPlugin {
 	Logger logger;
 	int points = 1;
 	int seconds = 60;
 	int taskid = -1;
 	
 	public void logInfo(String message){
     	logger.log(Level.INFO, "[Tech] " + message);
     }
     
     public void logWarning(String message){
     	logger.log(Level.WARNING, "[Tech] " + message);
     }
 
     public void onDisable() {
         getServer().getScheduler().cancelTask(taskid);
         System.out.println(this + " is now disabled!");
     }
 
     public void onEnable() {
     	logger = Logger.getLogger(JavaPlugin.class.getName());
     	points = this.getConfig().getInt("points");
     	seconds = this.getConfig().getInt("seconds");
     	this.getConfig().options().copyDefaults(true);
         saveConfig();
         taskid = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
         	public void run(){
             	for(World w : getServer().getWorlds()){
             		for(Player p : w.getPlayers()){
            			if(p.hasPermission("tech"))
            				TechManager.addPoints(p, points);
             		}
             	}
             }
         }, seconds * 20, seconds * 20);
         
         System.out.println(this + " is now enabled!");
     }
     
     
     
     public String stringTechList(ArrayList<net.croxis.plugins.research.Tech> techs){
     	String techNames = "";
 		Iterator<net.croxis.plugins.research.Tech> it = techs.iterator();
 		while(it.hasNext()){
 			techNames += it.next().name;
 			if(it.hasNext())
 				techNames += ", ";
 		}
 		return techNames;
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
     	if(!(sender instanceof Player)){
     		sender.sendMessage("You must be a player in game to use these commands.");
     		return false;
     	}
     	
     	Player player = (Player) sender;
     	
     	if(args.length == 0){
     		//Empty for null commands
     	} else if(args[0].equalsIgnoreCase("list")){
     		ArrayList<net.croxis.plugins.research.Tech> techs = TechManager.getResearched(player);
     		player.sendMessage("You know " + Integer.toString(techs.size()) + " of " + Integer.toString(TechManager.techs.size()) + " techs.");
     		player.sendMessage(stringTechList(techs));
     		return true;
     	} else if(args[0].equalsIgnoreCase("available")){
     		ArrayList<net.croxis.plugins.research.Tech> techs = TechManager.getAvailableTech(player);
     		player.sendMessage("You can research the following " + Integer.toString(techs.size()) + " techs.");
     		player.sendMessage(stringTechList(techs));
     		return true;
     	} else if(args[0].equalsIgnoreCase("info") && args.length > 1){
     		String name = "";
     		for(String s : args){
     			if(!s.equalsIgnoreCase("info"))
     				name += s;
     		}
     		name = name.replaceAll("\\s+$", "");
     		net.croxis.plugins.research.Tech tech = TechManager.techs.get(name);
     		if(tech == null){
     			player.sendMessage("There is no tech by that name.");
     			return false;
     		}
     		player.sendMessage("Name: " + tech.name + " | Costs: " + Integer.toString(tech.cost));
     		player.sendMessage("Description: " + tech.description);
     		String parents = "";
     		String children = "";
     		for(net.croxis.plugins.research.Tech parent : tech.parents){
     			parents += parent.name + " ";
     		}
     		for(net.croxis.plugins.research.Tech child : tech.children){
     			children += child.name + " ";
     		}
     		player.sendMessage("Requires: " + parents.replaceAll("\\s+$", ""));
     		player.sendMessage("Enables: " + children.replaceAll("\\s+$", ""));
     		return true;    		
     	} else if(args[0].equalsIgnoreCase("set") && args.length > 1){
     		String name = "";
     		for(String s : args){
     			if(!s.equalsIgnoreCase("set"))
     				name += s;
     		}
     		name = name.replaceAll("\\s+$", "");
     		net.croxis.plugins.research.Tech tech = TechManager.techs.get(name);
     		if(tech == null){
     			player.sendMessage("There is no tech by that name.");
     			return false;
     		}
     		if(TechManager.getResearched(player).contains(tech)){
     			player.sendMessage("You have already researched that tech.");
     			return false;
     		}
     		if(!TechManager.canResearch(player, tech)){
     			player.sendMessage("You can not research that tech yet.");
     			return false;
     		}
     		TechManager.startResearch(player, name);
     		float time = (float) (tech.cost - TechManager.getPoints(player)) / (float) seconds;
     		player.sendMessage("Time to completion: " + Float.toString(time) + " minutes.");
     		return true;    		
     	} else if(args[0].equalsIgnoreCase("progress")){
     		net.croxis.plugins.research.Tech tech = TechManager.getCurrentResearch(player);
     		if(tech == null){
     			player.sendMessage("You are not researching anything right now.");
     			return true;
     		}
     		float time = (float) (tech.cost - TechManager.getPoints(player)) / (float) seconds;
     		player.sendMessage("Current Progress: " + Integer.toString(TechManager.getPoints(player)) + "/" + Integer.toString(tech.cost));
     		player.sendMessage("Time to completion: " + Float.toString(time) + " minutes.");
     		return true;    		
     	}
     	
     	
     	sender.sendMessage("Technology manager. Following subcommands: /tech list, /tech info techname, /tech available, /tech set techname, /tech progress");
 		return true;
     }
 }
