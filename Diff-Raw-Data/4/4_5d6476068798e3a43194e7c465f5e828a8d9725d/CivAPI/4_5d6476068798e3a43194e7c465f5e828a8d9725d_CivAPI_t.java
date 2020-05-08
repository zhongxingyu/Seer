 package net.croxis.plugins.civilmineation;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.croxis.plugins.research.Tech;
 import net.croxis.plugins.research.TechManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 public class CivAPI {
 	public static Civilmineation plugin;
 	public CivAPI(Civilmineation p){
 		plugin = p;
 	}
 	
 	public static ResidentComponent getResident(String name){
     	return plugin.getDatabase().find(ResidentComponent.class).where().ieq("name", name).findUnique();
     }
     
     public static ResidentComponent getResident(Player player){
     	return plugin.getDatabase().find(ResidentComponent.class).where().ieq("name", player.getName()).findUnique();
     }
     
     public static List<ResidentComponent> getResidents(CivilizationComponent civ){
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("civilization", civ).findList();
     }
     
     public static List<ResidentComponent> getResidents(CityComponent city){
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).findList();
     }
     
     public static PlotComponent getPlot(Chunk chunk){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("x", chunk.getX()).eq("z", chunk.getZ()).findUnique();
     }
     
     public static PermissionComponent getPermissions(Ent ent){
     	return plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", ent).findUnique();
     }
     
     public static CivilizationComponent getCiv(String name){
     	return plugin.getDatabase().find(CivilizationComponent.class).where().ieq("name", name).findUnique();
     }
     
     public static CityComponent getCity(Location charterLocation){
     	return plugin.getDatabase().find(CityComponent.class).where().eq("charter_x", charterLocation.getX())
     			.eq("charter_y", charterLocation.getY())
     			.eq("charter_z", charterLocation.getZ()).findUnique();
     }
     
     public static Set<CityComponent> getCities(){
     	return plugin.getDatabase().find(CityComponent.class).findSet();
     }
     
     public static Set<ResidentComponent> getAssistants(CityComponent city){
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).eq("cityAssistant", true).findSet();
     }
     
     public static Set<PlotComponent> getPlots(CityComponent city){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).findSet();
     }
     
     public static ResidentComponent getKing(ResidentComponent resident){
     	CityComponent capital = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", resident.getCity().getCivilization()).eq("capital", true).findUnique();
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", capital).eq("mayor", true).findUnique();
     }
     
     public static ResidentComponent getKing(CityComponent city){
     	CityComponent capital = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", city.getCivilization()).eq("capital", true).findUnique();
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", capital).eq("mayor", true).findUnique();
     }
     
     public static boolean isKing(ResidentComponent resident){
     	return resident.isMayor() && resident.getCity().isCapital();
     }
     
     public static boolean isNationalAdmin(ResidentComponent resident){
     	if (resident.isCivAssistant())
     		return true;
     	return isKing(resident);
     }
     
     public static void addCulture(CityComponent city, int culture){
     	city.setCulture(city.getCulture() + culture);
     	plugin.getDatabase().save(city);
     	updateCityCharter(city);
     }
     
 	public static void addResearch(CityComponent city, int points) {
 		CityComponent capital = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", city.getCivilization())
 				.eq("capital", true).findUnique();
 		ResidentComponent king = plugin.getDatabase().find(ResidentComponent.class).where().eq("city", capital).eq("mayor", true).findUnique();
 		Tech learned = TechManager.addPoints(king.getName(), points);
 		if(learned != null){
 			List<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", capital.getCivilization()).findList();
 			for (CityComponent c : cities){
 				List<ResidentComponent> residents = plugin.getDatabase().find(ResidentComponent.class).where().eq("city", c).findList();
 				for (ResidentComponent r : residents){
 					TechManager.addTech(r.getName(), learned);
 					plugin.getServer().getPlayer(r.getName()).sendMessage("You have learned " + learned.name + "!");
 				}
 			}
 		}
 		
 	}
     
     public static void updateCityCharter(CityComponent city){
     	if (city == null){
     		Civilmineation.log("Error. No city at that charter");
     		return;
     	}
     	Block charter = plugin.getServer().getWorld(city.getCharterWorld()).getBlockAt(city.getCharter_x(), city.getCharter_y(), city.getCharter_z());
     	Sign block = (Sign) charter.getRelative(BlockFace.DOWN).getState();
 		block.setLine(0, "=Demographics=");
 		block.setLine(1, "Population: " + Integer.toString(CivAPI.getResidents(city).size()));
 		block.setLine(2, "=Immigration=");
 		block.setLine(3, ChatColor.GREEN + "Open");
 		block.update();
 		if (city.getCharterRotation() == 4 || city.getCharterRotation() == 5){
     		charter.getRelative(BlockFace.EAST).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.EAST).getState();
 			block.setLine(1, "Money: N/A");
 			Tech tech = TechManager.getCurrentResearch(getKing(city).getName());
 			if (tech == null){
 				block.setLine(2, "Research: None");
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + " / 0");
 			} else {
 				block.setLine(2, "Research: " + ChatColor.BLUE + tech.name);
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + "/" + Integer.toString(TechManager.getCurrentResearch(getKing(city).getName()).cost));
 			}
 			block.update();
 			charter.getRelative(BlockFace.WEST).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.WEST).getState();
 			block.setLine(1, "Plots: N/A");
 			block.setLine(2, "Culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(city.getCulture()));
 			block.update();
 		} else if (city.getCharterRotation() == 2 || city.getCharterRotation() == 3) {
 			charter.getRelative(BlockFace.NORTH).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.NORTH).getState();
 			block.setLine(1, "Money: N/A");
 			Tech tech = TechManager.getCurrentResearch(getKing(city).getName());
 			if (tech == null){
 				block.setLine(2, "Research: None");
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + " / 0");
 			} else {
 				block.setLine(2, "Research: " + ChatColor.BLUE + tech.name);
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + "/" + Integer.toString(TechManager.getCurrentResearch(getKing(city).getName()).cost));
 			}
 			block.update();
 			charter.getRelative(BlockFace.SOUTH).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.SOUTH).getState();
 			block.setLine(1, "Plots: N/A");
 			block.setLine(2, "Culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(city.getCulture()));
 			block.update();
 		}
     }
     
     public static boolean addResident(ResidentComponent resident, CityComponent city){
     	if (resident.getCity() != null)
     		return false;
     	resident.setCity(city);
     	plugin.getDatabase().save(resident);
     	HashSet<Tech> civtechs = TechManager.getResearched(getKing(resident).getName());
     	HashSet<Tech> restechs = TechManager.getResearched(resident.getName());
 		HashSet<Tech> difference = new HashSet<Tech>(restechs);
 		difference.removeAll(civtechs);
 		Player player = Bukkit.getServer().getPlayer(resident.getName());
 		restechs.addAll(civtechs);
 		TechManager.setTech(player, restechs);
 		HashSet<Tech> gaintech = new HashSet<Tech>();
 		HashSet<Tech> canResearch = TechManager.getAvailableTech(getKing(resident).getName());
 		for (Tech t : difference){
 			if(gaintech.size() > 2)
 				break;
 			if(canResearch.contains(t))
 				gaintech.add(t);
 		}
 		for (Tech t : gaintech){
 			for (ResidentComponent r : getResidents(resident.getCity().getCivilization())){
 				TechManager.addTech(r.getName(), t);
 			}
 		}
     	return true;
     }
     
 	public static CityComponent createCity(String name, Player player, ResidentComponent mayor, Block charter,
 			CivilizationComponent civ) {
 		Ent cityEntity = createEntity("City " + name);			
 		PermissionComponent cityPerm = new PermissionComponent();
 		cityPerm.setAll(false);
 		cityPerm.setResidentBuild(true);
 		cityPerm.setResidentDestroy(true);
 		cityPerm.setResidentItemUse(true);
 		cityPerm.setResidentSwitch(true);
 		cityPerm.setName(name + " permissions");
 		//addComponent(cityEntity, cityPerm);
 		cityPerm.setEntityID(cityEntity);
     	plugin.getDatabase().save(cityPerm);
 		
 		CityComponent city = new CityComponent();
 		//addComponent(cityEntity, city);
 		city.setEntityID(cityEntity);
 		city.setName(name);
 		city.setCivilization(civ);
 		city.setTaxes(0);
 		city.setRegistered(System.currentTimeMillis());
 		city.setTownBoard("Change me");
 		city.setCulture(10);
 		city.setSpawn_x(player.getLocation().getX());
 		city.setSpawn_y(player.getLocation().getY());
 		city.setSpawn_z(player.getLocation().getZ());
 		
 		city.setCharterWorld(player.getLocation().getWorld().getName());
 		city.setCharter_x(charter.getX());
 		city.setCharter_y(charter.getY());
 		city.setCharter_z(charter.getZ());
 		byte rotation = charter.getData();
 		city.setCharterRotation(rotation);
 		city.setCapital(true);
 		plugin.getDatabase().save(city);
 
 		mayor.setCity(city);
 		mayor.setMayor(true);
 		plugin.getDatabase().save(mayor);
 		return city;
 	}
     
     public static void broadcastToCity(String message, CityComponent city){
     	List<ResidentComponent> residents = CivAPI.getResidents(city);
     	for (ResidentComponent resident : residents){
    		Player player = plugin.getServer().getPlayer(resident.getName());
    		if (player != null)
    			player.sendMessage(message);
     	}
     }
     
     public static void broadcastToCiv(String message, CivilizationComponent civ){
     	List<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", civ).findList();
     	for (CityComponent city : cities){
     		broadcastToCity(message, city);
     	}
     }
     
     public static void loseTechs(ResidentComponent resident){
     	HashSet<Tech> techs = TechManager.getResearched(resident.getName());
 		System.out.println("Player leaving");
 		System.out.println("Known techs: " + Integer.toString(techs.size()));
 		HashSet<Tech> fiveToLose = new HashSet<Tech>();
 		HashSet<Tech> available;
 		try {
 			available = TechManager.getAvailableTech(resident.getName());
 			for (Tech t : available){
 				for (Tech c : t.children){
 					if (fiveToLose.size() >= 5)
 						break;
 					fiveToLose.add(c);
 				}
 			}
 			
 			if (fiveToLose.size() < 5){
 				for (Tech t : fiveToLose){
 					for (Tech c : t.children){
 						if (fiveToLose.size() >= 5)
 							break;
 						fiveToLose.add(c);
 					}
 				}
 			}
 			
 			techs.removeAll(fiveToLose);
 			OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(resident.getName());
 			TechManager.setTech((Player) player, techs);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
 
 	public static void disbandCity(CityComponent city) {
 		broadcastToCity("City disbanding", city);
 		String name = city.getName();
 		for (ResidentComponent resident : CivAPI.getResidents(city)){
 			resident.setCity(null);
 			resident.setMayor(false);
 			resident.setCityAssistant(false);
 			resident.setCivAssistant(false);
 			loseTechs(resident);
 			plugin.getDatabase().save(resident);
 		}
 		for (PlotComponent plot : getPlots(city)){
 			plot.setCity(null);
 			plot.setResident(null);
 			plugin.getDatabase().save(plot);
 		}
 		Ent civEnt = city.getCivilization().getEntityID();
 		CivilizationComponent civ = city.getCivilization();
 		
 		Ent cityEnt = city.getEntityID();
 		plugin.getDatabase().delete(city);
 		plugin.getDatabase().delete(cityEnt);
 		
 		plugin.getDatabase().delete(civ);
 		plugin.getDatabase().delete(civEnt);
 
 		plugin.getServer().broadcastMessage(name + " has fallen to dust!"); 
 	}
 	
 	public static void removeResident(ResidentComponent resident){
 		if (getResidents(resident.getCity()).size() == 1){
 			disbandCity(resident.getCity());
 			return;
 		}
 		loseTechs(resident);
 		CityComponent city = resident.getCity();
 		resident.setCity(null);
 		plugin.getDatabase().save(resident);
 		broadcastToCity(resident.getName() + " has left our city!", city);
 	}
 
 
 	public static Ent createEntity(){
     	Ent ent = new Ent();
     	plugin.getDatabase().save(ent);
     	return ent;
     }
 	
 	public static Ent createEntity(String name){
     	Ent ent = new Ent();
     	ent.setDebugName(name);
     	plugin.getDatabase().save(ent);
     	return ent;
     }
 
 }
