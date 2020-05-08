 package net.croxis.plugins.civilmineation;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.CivilizationComponent;
 import net.croxis.plugins.civilmineation.components.Ent;
 import net.croxis.plugins.civilmineation.components.PermissionComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.civilmineation.components.SignComponent;
 import net.croxis.plugins.civilmineation.events.ResidentJoinEvent;
 import net.croxis.plugins.research.RPlayer;
 import net.croxis.plugins.research.Tech;
 import net.croxis.plugins.research.TechManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.milkbowl.vault.economy.Economy;
 
 public class Civilmineation extends JavaPlugin implements Listener {
 	public static CivAPI api;
 	//private static final Logger logger = Logger.getLogger("Minecraft");
 	public static boolean debug = false;
 	public static Logger logger = Bukkit.getLogger();
 	
 	public static void log(String message){
 		logger.info("[Civ] " + message);
 	}
 	
 	public static void log(Level level, String message){
 		logger.info("[Civ] " + message);
 	}
 	
 	public static void logDebug(String message){
 		if (debug)
 			logger.info("[Civ][Debug] " + message);
 	}
 	
     public void onDisable() {
         // TODO: Place any custom disable code here.
     }
     
     static public boolean setupEconomy() {
         if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         CivAPI.econ = rsp.getProvider();
         return CivAPI.econ != null;
     }
 
     public void onEnable() {
     	setupDatabase();
     	
     	api = new CivAPI(this);
     	//if (!setupEconomy() ) {
         //    log(Level.SEVERE, "Disabled due to no Vault dependency found!");
         //    getServer().getPluginManager().disablePlugin(this);
         //    return;
         //}
     	
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getPluginManager().registerEvents(new ActionPermissionListener(), this);
         getServer().getPluginManager().registerEvents(new SignInteractListener(), this);
         getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
         
         getCommand("tech").setExecutor(new TechCommand(this));
         getCommand("civ").setExecutor(new CommandDebug(this));
         
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         	public void run(){
         		logDebug("Running Turn");
         		getServer().broadcastMessage(ChatColor.GOLD + "Ending turn");
         		for (Player player : getServer().getOnlinePlayers()){
         			ResidentComponent resident = CivAPI.getResident(player);
         			if (resident.getCity() != null){
         				CivAPI.addCulture(resident.getCity(), 1);
         				CivAPI.addResearch(resident.getCity(), 1);
         			} else {
         				Tech learned = TechManager.addPoints(player, 1);
         				if(learned != null)
         					player.sendMessage("You have learned " + learned.name);
         			}
         		}
         		for (CityComponent city : CivAPI.getCities()){
         			CivAPI.addResearch(city, CivAPI.getResearchPoints(city));
         			CivAPI.updateCityCharter(city);
         		}
         		PlotCache.upkeep(900000);
         		getServer().broadcastMessage(ChatColor.AQUA + "Beginning turn");
         	}
         }, 18000, 18000);
     }
     
     public Ent createEntity(){
     	Ent ent = new Ent();
     	getDatabase().save(ent);
     	return ent;
     }
     
     public Ent createEntity(String name){
     	Ent ent = new Ent();
     	ent.setDebugName(name);
     	getDatabase().save(ent);
     	return ent;
     }
     
     @Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(Ent.class);
 		list.add(ResidentComponent.class);
         list.add(CityComponent.class);
         list.add(CivilizationComponent.class);
         list.add(PlotComponent.class);
         list.add(PermissionComponent.class);
         list.add(SignComponent.class);
 		return list;
 	}
     
     private void setupDatabase()
 	{
 		try
 		{
 			getDatabase().find(Ent.class).findRowCount();
 		}
 		catch(PersistenceException ex)
 		{
 			System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
 			installDDL();
 		}
 		//Ent ent = createEntity();
 		//CivilizationComponent wilderness = new CivilizationComponent();
 		//wilderness.setName("Wilderness");
 		//addComponent(ent, wilderness);
 	}
     
     @EventHandler
     public void onSignChangeEvent(SignChangeEvent event){
     	ResidentComponent resident = CivAPI.getResident(event.getPlayer().getName());
 		PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 		// Void plots are ok, if fact required for this set
     	if (event.getLine(0).equalsIgnoreCase("[new city]")){
     		if (CivAPI.isClaimed(plot)){
     			event.getPlayer().sendMessage("This plot is claimed");
     			event.setCancelled(true);
     			event.getBlock().breakNaturally();
     			return;
     		}  
     		if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()){
     			event.getPlayer().sendMessage("City name on second line, Mayor name on third line");
     			event.setCancelled(true);
     			event.getBlock().breakNaturally();
     			return;
     		}
     		if (!CivAPI.isCivAdmin(resident)){
     			event.getPlayer().sendMessage("You must be a national leader or assistant.");
     			event.setCancelled(true);
 
     			event.getBlock().breakNaturally();
     			return;
     		}
     		ResidentComponent mayor = CivAPI.getResident(event.getLine(2));
     		if (mayor == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			event.getBlock().breakNaturally();
     			return;
     		}
 			CityComponent cityComponent = getDatabase().find(CityComponent.class).where().ieq("name", event.getLine(2)).findUnique();
 			if (cityComponent != null){
 				event.getPlayer().sendMessage("That city name already exists");
 				event.setCancelled(true);
     			event.getBlock().breakNaturally();
 				return;
 			}			
 			if (mayor.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
     			event.getBlock().breakNaturally();
 				return;
 			}
 			if (!mayor.getCity().getCivilization().getName().equalsIgnoreCase(resident.getCity().getCivilization().getName())){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
     			event.getBlock().breakNaturally();
 				return;
 			}
 			if (mayor.isMayor()){
 				event.getPlayer().sendMessage("That player can not be an existing mayor.");
 				event.setCancelled(true);
     			event.getBlock().breakNaturally();
 				return;
 			}
 			if (plot.getCity() != null){
 				event.getPlayer().sendMessage("That plot is part of a city.");
 				event.setCancelled(true);
 				event.getBlock().breakNaturally();
 				return;
 			}
 			
 			if (resident.getCity().getCulture() < 50){
 				event.getPlayer().sendMessage("Not enough culture to found a city.");
 				event.setCancelled(true);
 				event.getBlock().breakNaturally();
 				return;
 			}
 			
 			resident.getCity().setCulture(resident.getCity().getCulture() - 50);
 			getDatabase().save(resident.getCity());
 			
 			CityComponent city = CivAPI.createCity(event.getLine(1), event.getPlayer(), mayor, event.getBlock(), mayor.getCity().getCivilization(), false);
 			SignComponent signComp = CivAPI.createSign(event.getBlock(), city.getName() + " charter", SignType.CITY_CHARTER, city.getEntityID());
 			CivAPI.claimPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), city.getName() + " Founding Square", event.getBlock().getRelative(BlockFace.UP), resident.getCity());
 			
 			event.setLine(0, ChatColor.BLUE + "City Charter");
 			event.setLine(1, city.getCivilization().getName());
 			event.setLine(2, city.getName());
 			event.setLine(3, "Mayor " + mayor.getName());
 			event.getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData(68, signComp.getRotation(), true);
 			//event.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setTypeIdAndData(68, rotation, true);
 			CivAPI.updateCityCharter(city);
 			CivAPI.broadcastToCiv("The city of " + city.getName() + " has been founded!", mayor.getCity().getCivilization());
     	} else if (event.getLine(0).equalsIgnoreCase("[civ assist]")){
     		ResidentComponent king = CivAPI.getResident(event.getPlayer());
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Assistant name on the second line.");
     			event.setCancelled(true);
     			return;
     		} else if (!CivAPI.isKing(king)){
     			event.getPlayer().sendMessage("You must be a king.");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent assistant = CivAPI.getResident(event.getLine(1));
     		
     		if (assistant == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			return;
     		}						
 			if (assistant.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (!king.getCity().getCivilization().getName().equalsIgnoreCase(assistant.getCity().getCivilization().getName())){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
 				return;
 			}
 			assistant.setCivAssistant(!assistant.isCivAssistant());
 			getDatabase().save(assistant);
 			if(assistant.isCivAssistant())
 				CivAPI.broadcastToCiv(assistant.getName() + " is now a civ assistant!", king.getCity().getCivilization());
 			else
 				CivAPI.broadcastToCiv(assistant.getName() + " is no longer a civ assistant!", king.getCity().getCivilization());
 			return;
     	} else if (event.getLine(0).equalsIgnoreCase("[city assist]")){
     		ResidentComponent mayor = CivAPI.getResident(event.getPlayer());
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Assistant name on the second line.");
     			event.setCancelled(true);
     			return;
     		} else if (!mayor.isMayor()){
     			event.getPlayer().sendMessage("You must be a mayor.");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent assistant = CivAPI.getResident(event.getLine(1));
     		if (assistant == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			return;
     		}						
 			if (assistant.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your city!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (!mayor.getCity().getName().equalsIgnoreCase(assistant.getCity().getName())){
 				event.getPlayer().sendMessage("That player must be in your city!.");
 				event.setCancelled(true);
 				return;
 			}
 			assistant.setCityAssistant(!assistant.isCityAssistant());
 			getDatabase().save(assistant);
 			if(assistant.isCityAssistant())
 				CivAPI.broadcastToCity(assistant.getName() + " is now a city assistant!", mayor.getCity());
 			else
 				CivAPI.broadcastToCity(assistant.getName() + " is no longer a city assistant!", mayor.getCity());
 			return;
     	}  else if (event.getLine(0).equalsIgnoreCase("[kick]")) {
     		event.getBlock().breakNaturally();
     		if (!CivAPI.isCityAdmin(resident)){
     			event.getPlayer().sendMessage("You are not a city admin");
     			event.setCancelled(true);
     			return;
     		}
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Kickee name on the second line.");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent kickee = CivAPI.getResident(event.getLine(1));
     		if (kickee == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			return;
     		}						
 			if (kickee.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your city!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (!resident.getCity().getName().equalsIgnoreCase(kickee.getCity().getName())){
 				event.getPlayer().sendMessage("That player must be in your city!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (CivAPI.isCityAdmin(kickee)){
 				event.getPlayer().sendMessage("Mayors and assistants must be demoted before kick!.");
 				event.setCancelled(true);
 				return;
 			}
 			CivAPI.removeResident(kickee);
     	} else if (event.getLine(0).equalsIgnoreCase("[sell]")) {
     		double price = 0;
     		if (!CivAPI.isClaimed(plot)){
     			event.getPlayer().sendMessage("This plot is unclaimed");
     			event.setCancelled(true);
     			event.getBlock().breakNaturally();
     			return;
     		}  
     		if(!event.getLine(1).isEmpty())
 	    		try{
 	    			price = Double.parseDouble(event.getLine(1));
 	    		} catch (NumberFormatException e) {
 	    			event.getPlayer().sendMessage("Bad price value");
 	    			event.setCancelled(true);
 	    			event.getBlock().breakNaturally();
 	    			return;
     			}
     		if(plot.getResident() == null){
     			if(!CivAPI.isCityAdmin(resident)){
     				event.getPlayer().sendMessage("You are not a city admin");
         			event.setCancelled(true);
         			event.getBlock().breakNaturally();
         			return;
     			}
     			Sign sign = CivAPI.getPlotSign(plot);
     			if(sign == null){
     				CivAPI.setPlotSign((Sign) event.getBlock().getState());
     				plot = CivAPI.getPlot(event.getBlock().getChunk());
     				CivAPI.updatePlotSign(plot);
     			} else {
     				event.getBlock().breakNaturally();
     			}
     			sign = CivAPI.getPlotSign(plot);
     			sign.setLine(2, "=For Sale=");
     			sign.setLine(3, Double.toString(price));
     			sign.update();
     			event.getBlock().breakNaturally();
     			return;
     		} else {
     			if(CivAPI.isCityAdmin(resident) || plot.getResident().getName().equalsIgnoreCase(resident.getName())){
     				Sign sign = CivAPI.getPlotSign(plot);
         			if(sign == null){
         				CivAPI.setPlotSign((Sign) event.getBlock().getState());
         				plot = CivAPI.getPlot(event.getBlock().getChunk());
         				CivAPI.updatePlotSign(plot);
         			} else {
         				event.getBlock().breakNaturally();
         			}
         			sign = CivAPI.getPlotSign(plot);
         			sign.setLine(2, "=For Sale=");
         			sign.setLine(3, Double.toString(price));
         			return;
     			} else {
     				event.getPlayer().sendMessage("You are not a city admin or plot owner");
         			event.setCancelled(true);
         			event.getBlock().breakNaturally();
         			return;
     			}
     		}
     	}  else if (event.getLine(0).equalsIgnoreCase("[plot]")) {
     		//NOTE: This has to be set inside event. Cannot cast as block as
     		//event will override sign.setLine() 
     		if (!CivAPI.isClaimed(plot)){
     			event.getPlayer().sendMessage("This plot is unclaimed");
     			event.setCancelled(true);
     			event.getBlock().breakNaturally();
     			return;
     		}  
     		try{
     			CivAPI.getPlotSign(plot).getBlock().breakNaturally();
     		} catch (Exception e){
     		}
     		if(plot.getResident() == null){
     			if(!CivAPI.isCityAdmin(resident)){
     				event.getPlayer().sendMessage("You are not a city admin");
         			event.setCancelled(true);
         			event.getBlock().breakNaturally();
         			return;
     			}
     			event.setLine(0, plot.getCity().getName());
     			event.getPlayer().sendMessage("Plot sign updated");
     		} else {
     			if(CivAPI.isCityAdmin(resident) || plot.getResident().getName().equalsIgnoreCase(resident.getName())){
     				CivAPI.setPlotSign((Sign) event.getBlock().getState());    				
     				if(getServer().getPlayer(plot.getResident().getName()).isOnline()){
     					event.setLine(0, ChatColor.GREEN + plot.getResident().getName());
     				} else {
     					event.setLine(0, ChatColor.RED + plot.getResident().getName());
     				}
     				event.getPlayer().sendMessage("Plot sign updated");
     			}
     		}
 		}  else if (event.getLine(0).equalsIgnoreCase("[name plot]")) {
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Put a name on the second line!");
     			event.setCancelled(true);
     			return;
     		}
     		plot.setName(event.getLine(1));
     		CivAPI.save(plot);
     		CivAPI.updatePlotSign(plot);
     	} else if (event.getLine(0).equalsIgnoreCase("[friend]")) {
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Put a name on the second line!");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent friend = CivAPI.getResident(event.getLine(1));
     		if (friend == null){
     			event.getPlayer().sendMessage("That player does not exist!");
     			event.setCancelled(true);
     			return;
     		}
     		resident.getFriends().add(friend);
     		getDatabase().save(resident);
     	}
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
     	ResidentComponent resident = CivAPI.getResident(event.getPlayer());
     	if (resident == null){
     		Ent entity = createEntity();
     		resident = new ResidentComponent();
     		resident.setRegistered(System.currentTimeMillis());
     		resident.setName(event.getPlayer().getName());
     		resident.setEntityID(entity);
         	getDatabase().save(resident);
         	PermissionComponent perm = new PermissionComponent();
     		perm.setEntityID(resident.getEntityID());
     		perm.setName(resident.getName());
     		perm.setAll(false);
     		perm.setResidentBuild(true);
     		perm.setResidentDestroy(true);
     		perm.setResidentItemUse(true);
     		perm.setResidentSwitch(true);
     		getDatabase().save(perm);
     	}
     	
     	String title = "";
     	if (resident.isMayor()){
     		title = "Mayor ";
 	    	if (resident.getCity().isCapital())
 	    		title = "King ";
     	} else if (resident.getCity() == null)
     		title = "Wild ";
         event.getPlayer().sendMessage("Welcome, " + title + event.getPlayer().getDisplayName() + "!");
         ResidentJoinEvent resEvent = new ResidentJoinEvent(resident.getName(), resident.getEntityID().getId());
         Bukkit.getServer().getPluginManager().callEvent(resEvent);
         
         for (PlotComponent plot:CivAPI.getPlots(resident)){
         	Sign sign = CivAPI.getPlotSign(plot);
         	if (sign.getLine(0).contains(event.getPlayer().getName())){
 	        	sign.setLine(0, ChatColor.GREEN + sign.getLine(0).substring(2));
 	        	sign.setLine(1, "");
 	        	sign.update();
         	}
         }
     }
     
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event){
     	for (PlotComponent plot:CivAPI.getPlots(CivAPI.getResident(event.getPlayer()))){
         	Sign sign = CivAPI.getPlotSign(plot);
         	if (sign.getLine(0).contains(event.getPlayer().getName())){
         		DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");
         		Date date = new Date();
 	        	sign.setLine(0, ChatColor.RED + sign.getLine(0).substring(2));
 	        	sign.setLine(1, dateFormat.format(date));
 	        	sign.update();
         	}
         }
     }
     
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event){
     	if (event.getFrom().getWorld().getChunkAt(event.getFrom()).getX() 
     			!= event.getTo().getWorld().getChunkAt(event.getTo()).getX() 
     			|| event.getFrom().getWorld().getChunkAt(event.getFrom()).getZ() 
     			!= event.getTo().getWorld().getChunkAt(event.getTo()).getZ()){
     		//event.getPlayer().sendMessage("Message will go here");
     		PlotComponent plot = CivAPI.getPlot(event.getTo().getChunk());
     		PlotComponent plotFrom = CivAPI.getPlot(event.getFrom().getChunk());
     		if (plot.getCity() == null && plotFrom.getCity() != null){
     			event.getPlayer().sendMessage("Wilderness");
     		} else if (plot.getCity() == null && plotFrom.getCity() == null){
     			// Needed to prevent future NPES
     		} else if (plot.getCity() != null && plotFrom.getCity() == null){
     			// TODO: City enter event
     			event.getPlayer().sendMessage(plot.getName());
     		} else if (!plot.getName().equalsIgnoreCase(plotFrom.getName())){
     			event.getPlayer().sendMessage(plot.getName());
     		}
     	}
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerChat(PlayerChatEvent event) {
         if (event.isCancelled()) {
             return;
         }
         // Check whether the Server is set to prefix the chat with the World name.
         // If not we do nothing, if so we need to check if the World has an Alias.
         String prefix = "";
         
         ResidentComponent resident = CivAPI.getResident(event.getPlayer());
         if (resident.getCity() == null)
         	prefix = "[" + ChatColor.RED + "Barbarian" + ChatColor.WHITE + "]";
         else
        	prefix = "[" + resident.getCity().getCivilization().getChatcolor() + resident.getCity().getCivilization().getName() + ChatColor.WHITE + "]" 
        		+ "[" + resident.getCity().getChatcolor() + resident.getCity().getName() + ChatColor.WHITE + "]";
         event.setFormat(prefix + event.getFormat());
 
     }
 }
 
