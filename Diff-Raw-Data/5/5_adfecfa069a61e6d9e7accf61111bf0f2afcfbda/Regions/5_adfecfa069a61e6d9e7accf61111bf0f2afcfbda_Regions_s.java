 /******************************************************************************\
 |                                     ,,                                       |
 |                    db             `7MM                                       |
 |                   ;MM:              MM                                       |
 |                  ,V^MM.    ,pP"Ybd  MMpMMMb.  .gP"Ya `7Mb,od8                |
 |                 ,M  `MM    8I   `"  MM    MM ,M'   Yb  MM' "'                |
 |                 AbmmmqMA   `YMMMa.  MM    MM 8M""""""  MM                    |
 |                A'     VML  L.   I8  MM    MM YM.    ,  MM                    |
 |              .AMA.   .AMMA.M9mmmP'.JMML  JMML.`Mbmmd'.JMML.                  |
 |                                                                              |
 |                                                                              |
 |                                ,,    ,,                                      |
 |                     .g8"""bgd `7MM    db        `7MM                         |
 |                   .dP'     `M   MM                MM                         |
 |                   dM'       `   MM  `7MM  ,p6"bo  MM  ,MP'                   |
 |                   MM            MM    MM 6M'  OO  MM ;Y                      |
 |                   MM.    `7MMF' MM    MM 8M       MM;Mm                      |
 |                   `Mb.     MM   MM    MM YM.    , MM `Mb.                    |
 |                     `"bmmmdPY .JMML..JMML.YMbmd'.JMML. YA.                   |
 |                                                                              |
 \******************************************************************************/
 /******************************************************************************\
 | Copyright (c) 2012, Asher Glick                                              |
 | All rights reserved.                                                         |
 |                                                                              |
 | Redistribution and use in source and binary forms, with or without           |
 | modification, are permitted provided that the following conditions are met:  |
 |                                                                              |
 | * Redistributions of source code must retain the above copyright notice,     |
 |   this list of conditions and the following disclaimer.                      |
 | * Redistributions in binary form must reproduce the above copyright notice,  |
 |   this list of conditions and the following disclaimer in the documentation  |
 |   and/or other materials provided with the distribution.                     |
 |                                                                              |
 | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  |
 | AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    |
 | IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   |
 | ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    |
 | LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          |
 | CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         |
 | SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     |
 | INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      |
 | CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      |
 | ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   |
 | POSSIBILITY OF SUCH DAMAGE.                                                  |
 \******************************************************************************/
 package iggy.Regions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import iggy.Economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.dynmap.DynmapAPI;
 import org.dynmap.markers.AreaMarker;
 import org.dynmap.markers.MarkerAPI;
 import org.dynmap.markers.MarkerSet;
 
 
 public class Regions extends JavaPlugin{
   //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// GLOBAL DECLARATIONS ////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	Logger logger = Logger.getLogger("Minecraft");
 	
 	String pluginTitle;
 	PluginDescriptionFile pdFile;
 	
 	Plugin dynmap;
 	Plugin economy;
 	DynmapAPI dynmapapi;
 	MarkerAPI markerapi;
 	Economy economyapi;
 	
 	World mainworld;
 	World thenether;
 	
 	DisplayPlotTitles displayPlotTitles = new DisplayPlotTitles(this);
 	
 	
 	public Map<Position,String> chunkNames = new HashMap<Position,String>();
 	public Map<String,Owners> chunkOwners = new HashMap<String,Owners>();
 	
 	BlockMonitor pluginMonitor;
   //////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// ENABLE / DISABLE //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	@Override
 	public void onDisable() {
 		// TODO clear the regions so they don't double
 		saveRegions();
 		info(" Version " + pdFile.getVersion() +" is disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		pdFile = this.getDescription();
 		String pluginName = pdFile.getName();
 		pluginTitle = "[\033[2;33m"+pluginName+"\033[0m]";
 
 		pluginMonitor = new BlockMonitor(this);
 		
 		// define worlds
 		mainworld = Bukkit.getWorld("world");
 		thenether = Bukkit.getWorld("world_nether");
 		
 		displayPlotTitles.EnableRegionDisplayNames();
 		
 		loadRegions();
 		
 		// add external plugin links
 		PluginManager pm = getServer().getPluginManager();
 		dynmap = pm.getPlugin("dynmap");
 		economy = pm.getPlugin("Economy");
 		if (dynmap == null){
 			severe("cannot find dynmap");
 			return;
 		}
 		if (economy == null) {
 			severe("cannot find economy");
 			return;
 		}
 		dynmapapi = (DynmapAPI) dynmap;
 		info("Loaded Dynmap");
 		economyapi = (Economy) economy;
 		info ("Loaded Economy");
 		
 		//set up all the block listeners to prevent distruction
 		
 		//economy is required for buying new chunks
 		//dynmap is required for mapfunctions
 		
 		if (!economy.isEnabled() || !dynmap.isEnabled()) {
 			getServer().getPluginManager().registerEvents(new OurServerListener(), this);
 			if (!economy.isEnabled()) {
 				info("Waiting for Economy to be enabled");
 			}
 			if (!dynmap.isEnabled()) {
 				info("Waiting for Dynmap to be enabled");
 			}
 		}
 		if (economy.isEnabled()){
 			activateEconomy();
 		}
 		if (dynmap.isEnabled()){
 			activatedynmap();
 		}
 		info (" Version " + pdFile.getVersion() +" is enabled");
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// INPUT COMMANDS ///////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		//World world = player.getWorld();
 		/************************************ CLAIM ***********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("claim")){
 			// if economy is enabled
 			if (!economy.isEnabled()) {
 				player.sendMessage("Economy plugin is not enabled, contact admin for help");
 				return false;
 			}
 			// is plot is not already claimed
 			else if (chunkNames.containsKey(new Position(player.getLocation()))){
 				player.sendMessage("This plot has allready been claimed, you cannot claim it");
 				return false;
 			}
 			// is plot in the regular world or the nether
 			else if (player.getWorld() != mainworld && player.getWorld() != thenether){
 				player.sendMessage("You can only claim plots in the nether or the main world");
 			}
 			// is a name given
 			else if (args.length == 0) {
 				player.sendMessage("You need to specify a name for this plot");
 				return false;
 			}
 			// try to claim block
 			else {
 				String plotName = args[0];
 				for (int i = 1; i < args.length; i++) {
 					plotName += " "+args[i];
 				}
 				// check to see if the name has already been taken
 				if (chunkOwners.containsKey(plotName)) {
 					player.sendMessage("This plot name has allready been taken");
 					return false;
 				}
 				
 				if (economyapi.chargeMoney(player, 5000)) {
 					Position plot = new Position(player.getLocation());
 					Owners owner = new Owners();
 					owner.addOwner(player.getName());
 					chunkOwners.put(plotName, owner);
 					chunkNames.put(plot, plotName);
 					
 					// find highest block at the four corners
 					plot.placeTorches();
 					
 					player.sendMessage("You bought the plot "+plotName+" for $5000");
 				}
 				
 				else {
 					player.sendMessage("You dont have enough money to buy this plot");
 					return false;
 				}
 			}
 			saveRegions();
 			
 			// dynmap overlay
 			if (dynmap.isEnabled()){
 				refreshRegions ();
 			}
 		}
 		/*********************************** EXPAND ***********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("expand") || commandLabel.equalsIgnoreCase("ex") ||
 				commandLabel.equalsIgnoreCase("dark-expand") || commandLabel.equalsIgnoreCase("dx")) {
 			boolean dark = false;
 			// check to see if the expand should have torches
 			if (commandLabel.equalsIgnoreCase("dark-expand") || commandLabel.equalsIgnoreCase("dx")) {
 				dark = true;
 			}
 			// TODO this is not finished yet, it needs to find out the clostest plot to your position
 			// if economy is enabled
 			if (!economy.isEnabled()) {
 				player.sendMessage("Economy plugin is not enabled, contact admin for help");
 				return false;
 			}
 			// is plot is not already claimed
 			else if (chunkNames.containsKey(new Position(player.getLocation()))){
 				player.sendMessage("This plot has allready been claimed, you cannot claim it");
 				return false;
 			}
 			// is plot in the regular world or the nether
 			else if (player.getWorld() != mainworld && player.getWorld() != thenether){
 				player.sendMessage("You can only claim plots in the nether or the main world");
 			}
 
 			// try to claim block
 			else {
 				// if a plot you own is adjacent
 				Position plot = new Position(player.getLocation());
 				String plotName = "";
 				
 				Position plotN = new Position(player.getLocation().add( 8,0, 0));
 				Position plotS = new Position(player.getLocation().add(-8,0, 0));
 				Position plotE = new Position(player.getLocation().add(0, 0, 8));
 				Position plotW = new Position(player.getLocation().add(0, 0,-8));
 				
 				// Check plot to the north
 				if (chunkNames.containsKey(plotN)){
 					String tempName = chunkNames.get(plotN);
 					Owners plotOwners = chunkOwners.get(tempName);
 					// if the player owns the found chunk
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("error finding plot owners for "+tempName);
 				}
 				if (chunkNames.containsKey(plotS)){
 					String tempName = chunkNames.get(plotS);
 					Owners plotOwners = chunkOwners.get(tempName);
 					// if the player owns the found chunk
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("error finding plot owners for "+tempName);
 				}
 				if (chunkNames.containsKey(plotE)){
 					String tempName = chunkNames.get(plotE);
 					Owners plotOwners = chunkOwners.get(tempName);
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("error finding plot owners for "+tempName);
 				}
 				if (chunkNames.containsKey(plotW)){
 					String tempName = chunkNames.get(plotW);
 					Owners plotOwners = chunkOwners.get(tempName);
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("error finding plot owners for "+tempName);
 				}
 				if (plotName.equalsIgnoreCase("")) {
 					player.sendMessage("No adjacent plot found, cannot expand");
 					return false;
 				}
 				
 				// check to see if the name has already been taken (this is not nessasary
 				// because only plots you own can be expanded
 				/*
 				if (chunkOwners.containsKey(plotName)) {
 					player.sendMessage("This plot name has allready been taken");
 					return false;
 				}*/
 				
 				if (economyapi.chargeMoney(player, 1000)) {
 					
 					chunkNames.put(plot, plotName);
 					
 					// find highest block at the four corners
 					if (!dark) {
 						plot.placeTorches();
 					}
 					
 					player.sendMessage("You expanded the plot "+plotName+" for $1000");
 				}
 				
 				else {
 					player.sendMessage("You dont have enough money to buy this plot");
 					return false;
 				}
 			}
 			saveRegions();
 			// dynmap overlay
 			if (dynmap.isEnabled()){
 				refreshRegions ();
 			}
 		}
 		return false;
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////// WAIT FOR OTHER PLUGINS ///////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	// listener class to wait for the other plugins to enable
 	private class OurServerListener implements Listener {
 		// warnings are suppressed becasue this is called using registerEvents when the 
 		@SuppressWarnings("unused")
 		// this function runs whenever a plugin is enabled
 		@EventHandler (priority = EventPriority.MONITOR)
         public void onPluginEnable(PluginEnableEvent event) {
             Plugin p = event.getPlugin();
             String name = p.getDescription().getName();
             if(name.equals("dynmap")) {
             	activatedynmap();
             }
             if(name.equals("Economy")) {
             	activateEconomy();
             }
         }
     }
 
 	// funtion to finish activating the plugin once the other plugins are enabled
 	public void activateEconomy(){
 		//TODO: make these features not enabled if the plugin is not enabeled
 		info ("Economy features (claim, expand) enabled");
 	}
 	
 	/******************************* ACTIVATE DYNMAP ******************************\
 	| This function is run when dynmap is activated. It sets up the functions that |
 	| map the regions onto the dynmap display                                      |
 	\******************************************************************************/
 	MarkerSet set;
 	public void activatedynmap() {
 		markerapi =  dynmapapi.getMarkerAPI();
 		if (markerapi == null){
 			severe ("error loading the dynmap marker api");
 			return;
 		}
 		// create the market set
 		/* Now, add marker set for mobs (make it transient) */
         set = markerapi.getMarkerSet("regions.markerset");
         if(set == null)
             set = markerapi.createMarkerSet("regions.markerset", "regions", null, false);
         else
            set.setMarkerSetLabel("regions");
         if(set == null) {
             severe("Error creating marker set");
             return;
         }
         int minzoom = 0;
         if(minzoom > 0)
         set.setMinZoom(minzoom);
         set.setLayerPriority(10);
        set.setHideByDefault(false);
         //use3d = cfg.getBoolean("use3dregions", false);
         //infowindow = cfg.getString("infowindow", DEF_INFOWINDOW);
         
         // TEST CODE :)
         /*
         if(true){
         	double[] x = new double[10];
         	double[] z = new double[10];
         	x[0] = 0*8; z[0] = 0*8;
         	x[1] = 3*8; z[1] = 0*8;
         	x[2] = 3*8; z[2] = 3*8;
         	x[3] = 0*8; z[3] = 3*8;
         	
         	x[4] = 1*8; z[4] = 2*8;
         	x[5] = 2*8; z[5] = 2*8;
         	x[6] = 2*8; z[6] = 1*8;
         	x[7] = 1*8; z[7] = 1*8;
         	
         	x[8] = 1*8; z[8] = 2*8;
         	x[9] = 0*8; z[9] = 3*8;
         			
         	AreaMarker m = set.createAreaMarker("HOLE IN THE MIDDLE", "", false, "world", x, z, false);
         	m.setFillStyle(0, 0);
         	//m.setLineStyle(0, 0, 0);
         }*/
         
         /*
         for (int i = 0; i < 10; i++){
         	double[] x = new double[4];
     		double[] z = new double[4];
     		x[0]=(i*8)+0; z[0]=0;
     		x[1]=(i*8)+0; z[1]=8;
     		x[2]=(i*8)+8; z[2]=8;
     		x[3]=(i*8)+8; z[3]=0;
     		AreaMarker m = set.createAreaMarker("Color"+i, "Color"+i, false, "world", x, z, false);
     		m.setFillStyle(m.getFillOpacity(), 0x00FF00);
         }
         */
         // make the plots show up on the map
         // TODO: make the plots able to refresh live duing play
         refreshRegions();
 		info("dynmap features (view plots on map) enabled");
 	}
 	
 	
 	private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();
 
 
 	public void refreshRegions () {
 		int newCount = 0;
 		int replaceCount = 0;
 		
 		int countID = 0;
 		
 		// Create a map for the new region areas
 		Map<String, AreaMarker> newresareas = new HashMap<String, AreaMarker>();
 		
 		// Create a map for name to region lists
 		Map<String, HashSet <Position> > regions = new HashMap <String, HashSet <Position>>();
 		
 		for (Entry<Position,String> regionIterator : chunkNames.entrySet()) {
 			
 		}
 		for (){
 		    //String wname = areas[i].getWorld().getName();
 		    //if(isVisible(resid, wname) == false) continue;
 		    
 		    //String id = resid + "%" + i; /* Make area ID for cubiod */
 			String id = "region"+countID;
 			String name = regionIterator.getValue();
 		    
 			
 			double getx = regionIterator.getKey().getMinimumXCorner();
         	double gety = regionIterator.getKey().getMinimumZCorner();
         	
         	
         	String wname = regionIterator.getKey()._world;
         	
         	// draw an outline
     		double[] x = new double[4];
     		double[] z = new double[4];
     		x[0]=getx+0; z[0]=gety+0;
     		x[1]=getx+0; z[1]=gety+8;
     		x[2]=getx+8; z[2]=gety+8;
     		x[3]=getx+8; z[3]=gety+0;
 			
 			AreaMarker m = resareas.remove(id); /* Existing area? */
 		    if(m == null) {
 			    m = set.createAreaMarker(id, name, false, wname, x, z, false);
 		        if(m == null) {info("null region");continue;}
 		        m.setLineStyle(0, 0, 0);
 		        newCount++;
 		    }
 		    else {
 		        m.setCornerLocations(x, z); /* Replace corner locations */
 		        m.setLabel(name); /* Update label */
 		        replaceCount++;		        
 		    }
 		    newresareas.put(id, m);
 		    countID += 1;
 		}
 		resareas = newresareas;
 		info ("NEW COUNT:"+newCount);
 		info ("REPLACE COUNT:"+replaceCount);
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// REGION STORAGE ///////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public void saveRegions() {
 		getConfig().set("regions", "");
 		
 		Map<String,List<String> > plotLists = new HashMap<String,List<String>>();
 		
 		// load all of the plots together in an array with their plotname
 		Iterator<Entry<Position, String>> plotIterator = chunkNames.entrySet().iterator();
 		while (plotIterator.hasNext()){
 			Entry<Position, String> pair = plotIterator.next();
 			List <String> plotsLocations = plotLists.get(pair.getValue());
 			if (plotsLocations == null){
 				plotsLocations = new ArrayList<String>();
 			}
 			plotsLocations.add(pair.getKey().toString());
 			plotLists.put(pair.getValue(), plotsLocations);
 		}
 		
 		// write the plots by their plotnames at regions.plotname.plots
 		Iterator<Entry<String, List<String>>> plotList = plotLists.entrySet().iterator();
 		while (plotList.hasNext()) {
 			Entry<String, List<String>> pair = plotList.next();
 			getConfig().set("regions."+pair.getKey()+".plots", pair.getValue());
 			// draw the regions
 		}
 		
 		// write the owners to the plots as well
 		Iterator<Entry<String, Owners>> ownerIterator = chunkOwners.entrySet().iterator();
 		while (ownerIterator.hasNext()) {
 			Entry<String, Owners> pairs = ownerIterator.next();
 			this.getConfig().set("regions."+pairs.getKey()+".owners",pairs.getValue().getOwners());
 		}
 		this.saveConfig();
 		info("Regions Saved");
 	}
 	public void loadRegions() {
 		chunkNames.clear();
 		chunkOwners.clear();
 		ConfigurationSection regionSection = getConfig().getConfigurationSection("regions");
 		if (regionSection == null){
 			severe("cannot load regions (region section no found)");
 			return;
 		}
 		Set<String> regions = regionSection.getKeys(false);
 		if (regions == null){
 			severe("cannot load regions (no regions found in regions)");
 			return;
 		}
 		// for each region
 		for (String region : regions){
 			 List<String> plots = getConfig().getStringList("regions."+region+".plots");
 			 List<String> ownerslist= getConfig().getStringList("regions."+region+".owners");
 			 if (plots == null) {
 				 severe("errot loading configuration (no plots found for this region)");
 				 continue;
 			 }
 			 if (ownerslist == null) {
 				 severe("error loading configuration (no owners found for this region)");
 				 continue;
 			 }
 			 // add all of the plots for each region to the region list
 			 for (String plot : plots){
 				Position position = new Position();
 				position.setFromString(plot);
 				chunkNames.put(position, region);
 			 }
 			 // add all of the users to the user list
 			 Owners owners = new Owners();
 			 owners.addOwners(ownerslist);
 			 chunkOwners.put(region, owners);
 		}
 		info ("regions loaded");
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// DISPLAY HELPERS //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public void info(String input) {
 		this.logger.info(pluginTitle + input);
 	}
 	public void severe (String input) {
 		this.logger.severe(pluginTitle+"\033[31m"+input+"\033[0m");
 	}
 	
 }
