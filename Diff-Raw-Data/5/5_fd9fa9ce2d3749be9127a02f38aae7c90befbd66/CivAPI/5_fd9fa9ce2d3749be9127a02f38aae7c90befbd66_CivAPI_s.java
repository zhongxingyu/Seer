 package net.croxis.plugins.civilmineation;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import javax.persistence.OptimisticLockException;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.CivComponent;
 import net.croxis.plugins.civilmineation.components.Ent;
 import net.croxis.plugins.civilmineation.components.PermissionComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.civilmineation.components.SignComponent;
 import net.croxis.plugins.civilmineation.events.DeleteCityEvent;
 import net.croxis.plugins.civilmineation.events.DeleteCivEvent;
 import net.croxis.plugins.civilmineation.events.NewCityEvent;
 import net.croxis.plugins.civilmineation.events.NewCivEvent;
 import net.croxis.plugins.research.Tech;
 import net.croxis.plugins.research.TechManager;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 public class CivAPI {
 	public static Civilmineation plugin;
 	public static Economy econ = null;
 	public CivAPI(Civilmineation p){
 		plugin = p;
 		new PlotCache(p);
 	}
 	
 	public static ResidentComponent getResident(String name){
     	return plugin.getDatabase().find(ResidentComponent.class).where().ieq("name", name).findUnique();
     }
     
     public static ResidentComponent getResident(Player player){
     	return plugin.getDatabase().find(ResidentComponent.class).where().ieq("name", player.getName()).findUnique();
     }
     
     public static HashSet<ResidentComponent> getResidents(CivComponent civ){
     	Set<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", civ).findSet();
     	HashSet<ResidentComponent> residents = new HashSet<ResidentComponent>();
     	for (CityComponent city : cities){
     		residents.addAll(getResidents(city));
     	}
     	return residents;
     }
     
     public static Set<ResidentComponent> getResidents(CityComponent city){
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).findSet();
     }
     
     public static PlotComponent getPlot(Chunk chunk){
     	return getPlot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
     }
     
     public static PlotComponent getPlot(String world, int x, int z){
     	//return plugin.getDatabase().find(PlotComponent.class).where().eq("x", x).eq("z", z).findUnique();
     	return PlotCache.getPlot(world, x, z);
     }
     
     public static PlotComponent getPlot(Sign sign){
     	return getPlot(sign.getWorld().getName(), sign.getChunk().getX(), sign.getChunk().getZ());
     }
     
     public static Sign getPlotSignBlock(PlotComponent plot){
     	SignComponent sign = getPlotSign(plot);
 		Block block = plugin.getServer().getWorld(plot.getWorld()).getBlockAt(sign.getX(), sign.getY(), sign.getZ());
     	if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST)
 			return (Sign) block.getState();
     	return null;
     }
     
     public static SignComponent getPlotSign(PlotComponent plot){
     	Iterator<SignComponent> signs = CivAPI.getSigns(SignType.PLOT_INFO, plot.getEntityID()).iterator();
     	SignComponent sign = signs.next();
     	while (signs.hasNext()){
     		plugin.getDatabase().delete(signs.next());
     	}
     	return sign;
     	
     }
     
     public static PermissionComponent getPermissions(Ent ent){
     	return plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", ent).findUnique();
     }
     
     public static CivComponent getCiv(String name){
     	return plugin.getDatabase().find(CivComponent.class).where().ieq("name", name).findUnique();
     }
     
 	public static CityComponent getCity(Ent entityID) {
 		return plugin.getDatabase().find(CityComponent.class).where().eq("entityID", entityID).findUnique();
 	}
 	
 	public static CityComponent getCity(String name) {
 		return plugin.getDatabase().find(CityComponent.class).where().ieq("name", name).findUnique();
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
     
     public static Set<PlotComponent> getPlots(ResidentComponent resident){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("resident", resident).findSet();
     }
     
     public static Set<PlotComponent> getPlots(CityPlotType type, CityComponent city){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).eq("type", type).findSet();
     }
     
     public static ResidentComponent getMayor(ResidentComponent resident){
     	if (resident.getCity() == null)
     		return null;
 		return getMayor(resident.getCity());
     }
     
     public static ResidentComponent getMayor(CityComponent city){
 		return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).eq("mayor", true).findUnique();
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
     
     public static boolean isCivAdmin(ResidentComponent resident){
     	if (resident.isCivAssistant())
     		return true;
     	return isKing(resident);
     }
     
     public static boolean isCityAdmin(ResidentComponent resident){
     	if (resident.isCityAssistant())
     		return true;
     	else if (resident.getCity() == null)
     		return false;
     	return resident.isMayor();
     }
     
     public static boolean isClaimed(PlotComponent plot){
     	if (plot.getCity() == null){
 			return false;
 		}
     	return true;
     }
     
     public static boolean isClaimedByCity(PlotComponent plot, CityComponent city){
     	if (plot.getCity() == null)
     		return false;
     	if (plot.getCity().getName().equalsIgnoreCase(city.getName()))
 			return true;
     	return false;
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
 					if (learned.name.equalsIgnoreCase("Currency")){
 						CivAPI.econ.depositPlayer(r.getName(), 10);
 					}
 				}
 				broadcastToCity("You have learned " + ChatColor.BLUE + learned.name + "!", c);
 				if (learned.name.equalsIgnoreCase("Currency")){
 					CivAPI.econ.depositPlayer(c.getName(), 10);
 				}
 			}
 			if (learned.name.equalsIgnoreCase("Currency")){
 				CivAPI.econ.depositPlayer(city.getCivilization().getName(), 10);
 			}
 		}
 	}
 	
 	public static int getResearchPoints(CityComponent city){
 		int points = 0;
 		points += plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).eq("type", CityPlotType.LIBRARY).findList().size();
 		points += plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).eq("type", CityPlotType.UNIVERSITY).findList().size();
 		return points;
 	}
 	
 	public static int getCulturePoints(CityComponent city){
 		int points = 0;
 		points += plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).eq("type", CityPlotType.MONUMENT).findList().size();
 		return points;
 	}
 	
 	public static void generateCitySigns(Block charterBlock){
 		
 	}
     
     public static void updateCityCharter(CityComponent city){
     	if (city == null){
     		Civilmineation.log("Error. No city at that charter");
     		return;
     	}
     	SignComponent signComp = getSign(SignType.CITY_CHARTER, city.getEntityID());
     	Block charter = plugin.getServer().getWorld(signComp.getWorld()).getBlockAt(signComp.getX(), signComp.getY(), signComp.getZ());
     	Sign charterBlock = (Sign) charter.getState();
 		charterBlock.setLine(0, ChatColor.DARK_AQUA + "City Charter");
 		charterBlock.setLine(1, city.getCivilization().getName());
 		charterBlock.setLine(2, city.getName());
 		charterBlock.setLine(3, "Mayor " + getMayor(city).getName());
 		charterBlock.update();		
     	//Sign block = (Sign) charter.getRelative(BlockFace.DOWN).getState();
 		Sign block = getSignBlock(charter.getRelative(BlockFace.DOWN), charterBlock.getRawData());
 		block.setLine(0, "=Demographics=");
 		block.setLine(1, "Population: " + Integer.toString(CivAPI.getResidents(city).size()));
 		block.setLine(2, "=Immigration=");
 		if (block.getLine(3).contains("Open"))
 			block.setLine(3, ChatColor.GREEN + "Open");
 		else
 			block.setLine(3, ChatColor.RED + "Closed");
 		block.update();
 		
 		signComp = getSign(SignType.DEMOGRAPHICS, city.getEntityID());
 		if (signComp == null)
 			signComp = createSign(block.getBlock(), city.getName() + " demographics", SignType.DEMOGRAPHICS, city.getEntityID());
 		try{
 			if (signComp.getRotation() == 4 || signComp.getRotation() == 5){
 				block = getSignBlock(charter.getRelative(BlockFace.EAST), signComp.getRotation());
 				if (TechManager.hasTech(getMayor(city).getName(), "Currency"))
 					block.setLine(0, ChatColor.YELLOW + "Money: " + Double.toString(econ.getBalance(city.getName())));
 				else
 					block.setLine(0, ChatColor.YELLOW + "Need Currency");
 				Tech tech = TechManager.getCurrentResearch(getKing(city).getName());
 				if (tech == null){
 					block.setLine(1, "Research:");
 					block.setLine(2, "None");
 					block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + " / 0");
 				} else {
 					block.setLine(1, "Research:");
 					block.setLine(2, ChatColor.BLUE + tech.name);
 					block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + "/" + Integer.toString(TechManager.getCurrentResearch(getKing(city).getName()).cost));
 				}
 				block.update();
 				signComp = getSign(SignType.CITY_CHARTER_MONEY, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " money", SignType.CITY_CHARTER_MONEY, city.getEntityID());
 				block = getSignBlock(charter.getRelative(BlockFace.WEST), signComp.getRotation());
 				block.setLine(1, "Plots: " + Integer.toString(getPlots(city).size()));
 				block.setLine(2, "Culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(city.getCulture()));
 				block.update();
 				signComp = getSign(SignType.CITY_CHARTER_CULTURE, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " culture", SignType.CITY_CHARTER_CULTURE, city.getEntityID());
 				block = getSignBlock(charter.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST), signComp.getRotation());
 				block.setLine(0, "Civilization");
 				block.setLine(1, "Edit");
 				if (getPermissions(city.getEntityID()).allyEdit)
 					block.setLine(3, ChatColor.GREEN + "Open");
 				else
 					block.setLine(3, ChatColor.RED + "Closed");
 				block.update();
 				signComp = getSign(SignType.CITY_PERM_CIV_BUILD, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " perm civ build", SignType.CITY_PERM_CIV_BUILD, city.getEntityID());
 				
 				charter.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.UP).setTypeIdAndData(68, signComp.getRotation(), true);
 				block = (Sign) charter.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getState();
 				block.setLine(0, "Resident");
 				block.setLine(1, "Edit");
 				if (getPermissions(city.getEntityID()).residentEdit)
 					block.setLine(3, ChatColor.GREEN + "Open");
 				else
 					block.setLine(3, ChatColor.RED + "Closed");
 				block.update();
 				signComp = getSign(SignType.CITY_PERM_RES_BUILD, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " perm res build", SignType.CITY_PERM_RES_BUILD, city.getEntityID());
 				
 				charter.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN).setTypeIdAndData(68, signComp.getRotation(), true);
 				block = (Sign) charter.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN).getState();
 				block.setLine(0, "Outsider");
 				block.setLine(1, "Edit");
 				if (getPermissions(city.getEntityID()).outsiderEdit)
 					block.setLine(3, ChatColor.GREEN + "Open");
 				else
 					block.setLine(3, ChatColor.RED + "Closed");
 				block.update();
 				signComp = getSign(SignType.CITY_PERM_OUT_BUILD, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " perm out build", SignType.CITY_PERM_OUT_BUILD, city.getEntityID());
 				
 			} else if (signComp.getRotation() == 2 || signComp.getRotation() == 3) {
 				block = getSignBlock(charter.getRelative(BlockFace.NORTH), signComp.getRotation());
 				block.setLine(0, "Money: N/A");
 				Tech tech = TechManager.getCurrentResearch(getKing(city).getName());
 				if (tech == null){
 					block.setLine(1, "Research:");
 					block.setLine(2, "None");
 					block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + " / 0");
 				} else {
 					block.setLine(1, "Research:");
 					block.setLine(2, ChatColor.BLUE + tech.name);
 					block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + "/" + Integer.toString(TechManager.getCurrentResearch(getKing(city).getName()).cost));
 				}
 				block.update();
 				signComp = getSign(SignType.CITY_CHARTER_MONEY, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " demographics", SignType.CITY_CHARTER_MONEY, city.getEntityID());
 
 				block = getSignBlock(charter.getRelative(BlockFace.SOUTH), signComp.getRotation());
 				block.setLine(1, "Plots: N/A");
 				block.setLine(2, "Culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(city.getCulture()));
 				block.update();
 				signComp = getSign(SignType.CITY_CHARTER_CULTURE, city.getEntityID());
 				if (signComp == null)
 					createSign(block.getBlock(), city.getName() + " demographics", SignType.CITY_CHARTER_CULTURE, city.getEntityID());
 				
 				block = getSignBlock(charter.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH), signComp.getRotation());
 				block.setLine(0, "Civilization");
 				block.setLine(1, "Edit");
 				if (getPermissions(city.getEntityID()).allyEdit)
 					block.setLine(3, ChatColor.GREEN + "Open");
 				else
 					block.setLine(3, ChatColor.RED + "Closed");
 				block.update();
 				signComp = getSign(SignType.CITY_PERM_CIV_BUILD, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " perm civ build", SignType.CITY_PERM_CIV_BUILD, city.getEntityID());
 				
 				block = getSignBlock(charter.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.UP), signComp.getRotation());
 				block.setLine(0, "Resident");
 				block.setLine(1, "Edit");
 				if (getPermissions(city.getEntityID()).residentEdit)
 					block.setLine(3, ChatColor.GREEN + "Open");
 				else
 					block.setLine(3, ChatColor.RED + "Closed");
 				block.update();
 				signComp = getSign(SignType.CITY_PERM_RES_BUILD, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " perm res build", SignType.CITY_PERM_RES_BUILD, city.getEntityID());
 				
 				charter.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN).setTypeIdAndData(68, signComp.getRotation(), true);
 				block = (Sign) charter.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN).getState();
 				block.setLine(0, "Outsider");
 				block.setLine(1, "Edit");
 				if (getPermissions(city.getEntityID()).outsiderEdit)
 					block.setLine(3, ChatColor.GREEN + "Open");
 				else
 					block.setLine(3, ChatColor.RED + "Closed");
 				block.update();
 				signComp = getSign(SignType.CITY_PERM_OUT_BUILD, city.getEntityID());
 				if (signComp == null)
 					signComp = createSign(block.getBlock(), city.getName() + " perm out build", SignType.CITY_PERM_OUT_BUILD, city.getEntityID());
 				
 			}
 		} catch (Exception e){
 			broadcastToCity("WARNING. CITY CHARTER IS MALFORMED. PLEASE FIX", city);
 			Civilmineation.log("WARNING CITY CHARTER MALFORMED: " + city.getName());
 			e.printStackTrace();
 		}
     }
     
     public static void setPlotSign(Sign plotSign, PlotComponent plot){
     	Civilmineation.logDebug("setPlotSign");
     	SignComponent signComp = getPlotSign(plot);
     	if (signComp == null)
     		signComp = createSign(plotSign.getBlock(), plot.getName(), SignType.PLOT_INFO, plot.getEntityID());
     	signComp.setX(plotSign.getX());
     	signComp.setY(plotSign.getY());
     	signComp.setZ(plotSign.getZ());
 		plugin.getDatabase().save(signComp);
     }
     
     /*public static void updatePlotSign(String world, int x, int z) {
     	updatePlotSign(plugin.getDatabase().find(PlotComponent.class).where().eq("x", x).eq("z", z).findUnique());
     }*/
     
     public static void updatePlotSign(PlotComponent plot) {
     	Civilmineation.logDebug("Updating plot sign");
 		Sign sign = getPlotSignBlock(plot);
 		if(plot.getResident()!=null){
 			if(plugin.getServer().getPlayer(plot.getResident().getName()).isOnline()){
 				sign.setLine(0, ChatColor.GREEN + plot.getResident().getName());
 				sign.update();
 				Civilmineation.logDebug("a");
 			} else {
 				sign.setLine(0, ChatColor.RED + plot.getResident().getName());
 				sign.update();
 				Civilmineation.logDebug("b");
 			}
 		} else { 
 			sign.setLine(0, plot.getCity().getName());
 			sign.update();
 			Civilmineation.logDebug("c");
 			sign.update();
 		}
 		Civilmineation.logDebug("New plot sign xyz: " + Integer.toString(sign.getX()) + ", " + Integer.toString(sign.getY()) + ", " + Integer.toString(sign.getZ()));
 		sign.update();
 	}
     
     public static void updatePlotSign(Sign sign, PlotComponent plot) {
     	SignComponent signComp = getSign(SignType.PLOT_INFO, plot.getEntityID());
     	signComp.setX(sign.getX());
     	signComp.setY(sign.getY());
     	signComp.setZ(sign.getZ());
 		plugin.getDatabase().save(signComp);
 		//updatePlotSign(plot.getX(), plot.getZ());
 		updatePlotSign(plot);
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
 		updateCityCharter(city);
     	return true;
     }
     
 	public static CityComponent createCity(String name, Player player, ResidentComponent mayor, Block charter,
 			CivComponent civ, boolean capital) {
 		Ent cityEntity = createEntity("City " + name);			
 		PermissionComponent cityPerm = new PermissionComponent();
 		cityPerm.setAll(false);
 		cityPerm.setResidentEdit(true);
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
 		city.setTag(name);
 		city.setCivilization(civ);
 		city.setCapital(capital);
 		city.setTaxes(0);
 		city.setRegistered(System.currentTimeMillis());
 		city.setTownBoard("Change me");
 		city.setCulture(10);
 		city.setSpawn_x(player.getLocation().getX());
 		city.setSpawn_y(player.getLocation().getY());
 		city.setSpawn_z(player.getLocation().getZ());
 		//city.setChatcolor(ChatColor.valueOf("AQUA"));
 		plugin.getDatabase().save(city);
 
 		mayor.setCity(city);
 		mayor.setMayor(true);
 		plugin.getDatabase().save(mayor);
 		
 		NewCityEvent nce = new NewCityEvent(city.getName(), city.getEntityID().getId());
 		Bukkit.getServer().getPluginManager().callEvent(nce);
 		
 		return city;
 	}
 	
 	public static CivComponent createCiv(String name){
 		Ent civEntity = createEntity("Civ " + name);
 		CivComponent civ = new CivComponent();
 		civ.setName(name);
 		//addComponent(civEntity, civ);
 		civ.setEntityID(civEntity);
 		civ.setRegistered(System.currentTimeMillis());
 		civ.setTaxes(0);
 		civ.setTag(name);
 		//civ.setChatcolor(ChatColor.valueOf("YELLOW"));
     	plugin.getDatabase().save(civ);
 		
 		PermissionComponent civPerm = new PermissionComponent();
 		civPerm.setAll(false);
 		civPerm.setResidentEdit(true);
 		civPerm.setResidentItemUse(true);
 		civPerm.setResidentSwitch(true);
 		civPerm.setName(name + " permissions");
 		
 		//addComponent(civEntity, civPerm);
 		civPerm.setEntityID(civEntity);
 		plugin.getDatabase().save(civPerm);
 		
 		NewCivEvent nce = new NewCivEvent(civ.getName(), civ.getEntityID().getId());
 		Bukkit.getServer().getPluginManager().callEvent(nce);
     	return civ;
 	}
 	
 	public static void claimPlot(String world, int x, int z, Block plotSign, CityComponent city){
 		claimPlot(world, x, z, city.getName(), plotSign, city);
 	}
 	
 	public static void claimPlot(String world, int x, int z, String name, Block plotSign, CityComponent city){
 		PlotCache.dirtyPlot(world, x, z);
 		PlotComponent plot = getPlot(world, x, z);
 		if (plot.getCity() == null){
 			Ent plotEnt = createEntity();
 			plot.setEntityID(plotEnt);
 		}
 		plot.setCity(city);
 		plot.setName(name);
 		plot.setWorld(plotSign.getWorld().getName());
 		plot.setType(CityPlotType.RESIDENTIAL);
 		save(plot);
 		
 		createSign(plotSign, city.getName() + " plot", SignType.PLOT_INFO, plot.getEntityID());
 	}
 	
 	public static void unclaimPlot(PlotComponent plot){
 		plugin.getDatabase().delete(getSign(SignType.PLOT_INFO, plot.getEntityID()));
 		plot.setCity(null);
 		plot.setResident(null);
 		plot.setType(CityPlotType.WILDS);
 		save(plot);
 	}
     
     public static void broadcastToCity(String message, CityComponent city){
     	Set<ResidentComponent> residents = CivAPI.getResidents(city);
     	for (ResidentComponent resident : residents){
     		Player player = plugin.getServer().getPlayer(resident.getName());
     		if (player != null)
     			player.sendMessage(message);
     	}
     }
     
     public static void broadcastToCiv(String message, CivComponent civ){
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
 			unclaimPlot(plot);
 		}
 		plugin.getDatabase().delete(getAllSigns(city));
 		
 		Ent civEnt = city.getCivilization().getEntityID();
 		CivComponent civ = city.getCivilization();
 		
 		Ent cityEnt = city.getEntityID();
 		Bukkit.getServer().getPluginManager().callEvent(new DeleteCityEvent(city.getName(), city.getEntityID().getId()));
 		plugin.getDatabase().delete(city);
 		plugin.getDatabase().delete(plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", cityEnt).findUnique());
 		try{
 			plugin.getDatabase().delete(cityEnt);
 		} catch (Exception e){
 			Civilmineation.log(Level.WARNING, "A plugin did not properly let go of city entity " + Integer.toString(cityEnt.getId()));
 			Civilmineation.log(Level.WARNING, "Database maintenence will probably be needed");
 			e.printStackTrace();
 		}
 		
 		//List<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civ", civ).findList();
 		if (civ.getCities().isEmpty()){
 			Bukkit.getServer().getPluginManager().callEvent(new DeleteCivEvent(civ.getName(), civ.getEntityID().getId()));
 			plugin.getDatabase().delete(civ);
 			plugin.getDatabase().delete(plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", civEnt).findUnique());
 			try{
 				plugin.getDatabase().delete(civEnt);
 			} catch (Exception e){
 				Civilmineation.log(Level.WARNING, "A plugin did not properly let go of civ entity " + Integer.toString(civEnt.getId()));
 				Civilmineation.log(Level.WARNING, "Database maintenence will probably be needed");
 				e.printStackTrace();
 			}
 		} else {
 			city.setCapital(true);
 			plugin.getDatabase().save(city);
 			broadcastToCiv(city.getName() + " is now the Capital City!", civ);
 		}
 		plugin.getServer().broadcastMessage(name + " has fallen to dust!"); 
 	}
 	
 	public static void removeResident(ResidentComponent resident){
 		if (getResidents(resident.getCity()).size() == 1){
 			disbandCity(resident.getCity());
 			return;
 		}
 		loseTechs(resident);
 		CityComponent city = resident.getCity();
 		Set<PlotComponent> plots = plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).eq("resident", resident).findSet();
 		for (PlotComponent plot : plots){
 			plot.setResident(null);
 			save(plot);
 			updatePlotSign(plot);
 		}
 		resident.setCity(null);
 		plugin.getDatabase().save(resident);
 		broadcastToCity(resident.getName() + " has left our city!", city);
 	}
 	
 	public static SignComponent createSign(Block block, String name, SignType type, Ent entity){
 		Civilmineation.logDebug("createSign");
 		SignComponent sign = new SignComponent();
 		sign.setEntityID(entity);
 		sign.setName(name);
 		sign.setRotation(block.getData());
 		sign.setType(type);
 		sign.setWorld(block.getWorld().getName());
 		sign.setX(block.getX());
 		sign.setY(block.getY());
 		sign.setZ(block.getZ());
 		plugin.getDatabase().save(sign);
 		return sign;
 	}
 	
 	public static Set<SignComponent> getSigns(SignType type, Ent entity){
 		return plugin.getDatabase().find(SignComponent.class).where().eq("entityID", entity).eq("type", type).findSet();
 	}
 	
 	/**
 	 * @param type
 	 * @param entity
 	 * @return
 	 * 
 	 * Returns a single sign of a given type of an entity if only one is expected.
 	 */
 	public static SignComponent getSign(SignType type, Ent entity){
		return getSigns(type, entity).iterator().next();
 	}
 	
 	public static SignComponent getSign(SignType type, Block block){
 		return plugin.getDatabase().find(SignComponent.class).where()
 				.eq("world", block.getWorld().getName())
 				.eq("x", block.getX())
 				.eq("y", block.getX())
 				.eq("z", block.getX())
 				.eq("type", type).findUnique();
 	}
 	
 	public static SignComponent getSign(SignType type, Sign block){
 		return plugin.getDatabase().find(SignComponent.class).where()
 				.eq("world", block.getWorld().getName())
 				.eq("x", block.getX())
 				.eq("y", block.getX())
 				.eq("z", block.getX())
 				.eq("type", type).findUnique();
 	}
 
 	public static SignComponent getSign(Block block) {
 		return plugin.getDatabase().find(SignComponent.class).where()
 				.eq("world", block.getWorld().getName())
 				.eq("x", block.getX())
 				.eq("y", block.getY())
 				.eq("z", block.getZ()).findUnique();
 	}
 	
 	public static Sign getSignBlock(Block block, byte rotation){
 		Sign sign;
 		try{
 			sign = (Sign) block.getState();
 		} catch (Exception e) {
 			block.setTypeIdAndData(68, rotation, true);
 			sign = (Sign) block.getState();
 		}
 		return sign;
 	}
 	
 	public static Set<SignComponent> getSigns(PlotComponent plot) {
 		return plugin.getDatabase().find(SignComponent.class).where().eq("entityID", plot.getEntityID()).findSet();
 	}
 	
 	public static Set<SignComponent> getAllSigns(CityComponent city){
 		Set<SignComponent> signs = plugin.getDatabase().find(SignComponent.class).where().eq("entityID", city.getEntityID()).findSet();
 		for (PlotComponent plot : getPlots(city)){
 			signs.addAll(getSigns(plot));
 		}
 		return signs;
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
 
 	public static void save(PlotComponent plot){
 		PlotCache.dirtyPlot(plot);
 		plugin.getDatabase().save(plot);
 	}
 	
 	public static void save(CivComponent component){
 		plugin.getDatabase().save(component);
 	}
 	
 	public static void save(CityComponent component){
 		try{
 			plugin.getDatabase().save(component);
 		} catch (OptimisticLockException e) {
 			Civilmineation.log("WARNING: " + component.getName() + " has a lock exception. Retrying.");
 			plugin.getDatabase().beginTransaction();
 			CityComponent tmp = plugin.getDatabase().find(CityComponent.class, component.getId());
 			tmp = CityComponent.copy(component);
 			plugin.getDatabase().update(tmp);
 			plugin.getDatabase().endTransaction();
 		}
 	}
 	
 	public static void save(PermissionComponent component){
 		//Flag perm cache for full rebuild
 		PlotCache.flushDatabase();
 		plugin.getDatabase().save(component);
 	}
 	
 	public static void save(ResidentComponent component){
 		plugin.getDatabase().save(component);
 	}
 	
 	public static void setName(String name, CivComponent civ){
 		civ.setName(name);
 		civ.setTag(name);
 		save(civ);
 	}
 	
 	public static void setName(String name, CityComponent city){
 		Set<PlotComponent> plots = getPlots(city);
 		for (PlotComponent plot : plots){
 			if (plot.getName().contains(city.getName())){
 				String newName = plot.getName().replaceAll(city.getName(), name);
 				plot.setName(newName);
 				save(plot);
 				updatePlotSign(plot);
 			}
 		}
 		city.setName(name);
 		city.setTag(name);
 		save(city);
 	}
 }
