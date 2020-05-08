 package net.croxis.plugins.civilmineation;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 import net.croxis.plugins.civilmineation.components.CivilizationComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.research.Tech;
 import net.croxis.plugins.research.TechManager;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TechCommand implements CommandExecutor {
 
 	private Civilmineation plugin;
 
 	public TechCommand(Civilmineation civilmineation) {
 		super();
 		plugin = civilmineation;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 		if(!(sender instanceof Player)){
     		sender.sendMessage("You must be a player in game to use these commands.");
     		return false;
     	}
     	
     	Player player = (Player) sender;
     	ResidentComponent resident = CivAPI.getResident(player.getName());
     	if(args.length == 0){
     		//Empty for null commands
     	} else if(args[0].equalsIgnoreCase("list")){
     		HashSet<Tech> techs = TechManager.getResearched(player);
     		player.sendMessage("You know " + Integer.toString(techs.size()) + " of " + Integer.toString(TechManager.techs.size()) + " techs.");
     		player.sendMessage(stringTechList(techs));
     		return true;
     	} else if(args[0].equalsIgnoreCase("available")){
     		HashSet<Tech> techs = TechManager.getAvailableTech(player);
     		player.sendMessage("You can research the following " + Integer.toString(techs.size()) + " techs.");
     		player.sendMessage(stringTechList(techs));
     		return true;
     	} else if(args[0].equalsIgnoreCase("info") && args.length > 1){
     		String name = "";
     		for(String s : args){
     			if(!s.equalsIgnoreCase("info"))
     				name += s + " ";
     		}
     		name = name.trim();
     		Tech tech = TechManager.techs.get(name);
     		if(tech == null){
     			player.sendMessage("There is no tech by the name " + name);
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
     		if(resident.getCity() != null){    			
 					CivilizationComponent civ = resident.getCity().getCivilization();
 				
     			if(!(resident.isCivAssistant() || CivAPI.isKing(resident))){
     				player.sendMessage("Must be King or Civ assistant");
     				return true;
     			}
     		}
     		String name = "";
     		for(String s : args){
     			if(!s.equalsIgnoreCase("set"))
     				name += s + " ";
     		}
     		name = name.trim();
     		Tech tech = TechManager.techs.get(name);
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
     		if(CivAPI.isKing(resident) || resident.getCity() == null)
     			TechManager.startResearch(player, name);
     		else {
     			// Assistant
     			TechManager.startResearch(CivAPI.getKing(resident).getName(), name);
     		}
     		if(resident.getCity() != null){
     			//int res = universe.getOnlinePlayers(resident.getNation()).size();
     			//float time = (float) ((tech.cost - TechManager.getPoints(player)) / res);
     			CivAPI.broadcastToCiv("Current research now set to: " + tech.name + ".", resident.getCity().getCivilization());
     			//p.sendMessage("Current estimated time to completion: " + Float.toString(time) + " minutes.");
     		} else {
     			float time = (float) (tech.cost - TechManager.getPoints(player));
        		player.sendMessage("Time to completion: " + Float.toString(time * 5) + " minutes.");
     		}
     		
     		return true;    		
     	} else if(args[0].equalsIgnoreCase("progress")){
     		Tech tech = null;
     		String researcher;
     		if(resident.getCity() != null)
     			researcher = CivAPI.getKing(resident).getName();
     		else
     			researcher = player.getName();
 			tech = TechManager.getCurrentResearch(researcher);
     		if(tech == null){
     			player.sendMessage("You are not researching anything right now.");
     			return true;
     		}
     		if(resident.getCity() != null){
     			int res = CivAPI.getResidents(resident.getCity().getCivilization()).size();
     			float time = (float) ((tech.cost - TechManager.getPoints(player)) / res);
 				player.sendMessage("Current Progress for " + tech.name + ": " + Integer.toString(TechManager.getPoints(researcher)) + "/" + Integer.toString(tech.cost));
	    		player.sendMessage("Estimated time to completion: " + Float.toString(time * 5) + " minutes.");
     		} else {
     			float time = (float) (tech.cost - TechManager.getPoints(player));
         		player.sendMessage("Current Progress for " + tech.name + ": " + Integer.toString(TechManager.getPoints(player)) + "/" + Integer.toString(tech.cost));
        		player.sendMessage("Time to completion: " + Float.toString(time * 5) + " minutes.");
     		}
     		return true;    		
     	}
     	
     	
     	sender.sendMessage("Technology manager. Following subcommands: /tech list, /tech info techname, /tech available, /tech set techname, /tech progress");
 		return true;
 
 	}
 	
 	public String stringTechList(HashSet<Tech> techs){
     	String techNames = "";
 		Iterator<net.croxis.plugins.research.Tech> it = techs.iterator();
 		Tech tech;
 		while(it.hasNext()){
 			tech = it.next();
 			if (tech != null){
 				techNames += tech.name;
 				if(it.hasNext())
 					techNames += ", ";
 			}
 		}
 		return techNames;
     }
 
 }
