 package com.firestar.systemtest;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.io.*;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 /**
 * @author Firestar
 */
 
 public class systemtest extends JavaPlugin{
 	private final systemtestPlayerlistener playerListener = new systemtestPlayerlistener(this);
 	private final systemtestBlocklistener blockListener = new systemtestBlocklistener(this);
 	private final systemtestVehiclelistener vehicleListener = new systemtestVehiclelistener(this);
 	private final systemtestEntitylistener entityListener = new systemtestEntitylistener(this);
 	private final systemtestWorldlistener worldListener = new systemtestWorldlistener(this);
 	protected static final Logger log = Logger.getLogger("Minecraft");
 	public Hashtable<String,Integer> information= new Hashtable<String,Integer>();
 	public Hashtable<String,Boolean> event_disabled= new Hashtable<String,Boolean>();
 	public Hashtable<String,Integer> spammy_time= new Hashtable<String,Integer>();
 	public Hashtable<String,List<String>> categorized_events= new Hashtable<String,List<String>>();
 	public Hashtable<String,Integer> spammy= new Hashtable<String,Integer>();
 	public Hashtable<String,Integer> information_old= new Hashtable<String,Integer>();
 	private String name = "SystemTest";
 	private String version = "0.1";
 	class SPAMCHECK extends Thread {
 		public systemtest mi=null;
 		public SPAMCHECK(systemtest m){
 			mi=m;
 		}
 		public void send_message(String msg){
 			for (Player pi : getServer().getOnlinePlayers()) {
     			pi.sendMessage(msg);
     		}
 		}
 	    public void run() {
 	    	while(true){
 	    		for ( Map.Entry<String, Integer> entry : information.entrySet() ){
 	    			String key = entry.getKey();
 	    			Integer value = entry.getValue();
 	    			if(mi.information_old.containsKey(key)){
     					Integer value_i=mi.information_old.get(key);
     					if((value-value_i)>get_spam_time(key)){
     						if(!mi.spammy.containsKey(key)){
     							send_message("REGISTERED "+key+" AS A SPAMMY EVENT!");
     							mi.spammy.put(key,0);
     						}else{
     	    					Integer j=mi.spammy.get(key);
     	    					mi.spammy.put(key,(j+1));
     	    				}
     					}
 	    				mi.information_old.put(key, value);
 	    			}else{
 	    				mi.information_old.put(key, value);
 	    			}
 	            }
 	    		try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 	    	}
 	    }
 	    
 	}
 	public int get_spam_time(String key){
 		if(spammy_time.containsKey(key)){
 			return spammy_time.get(key);
 		}
 		return 200;
 	}
 	public systemtest(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File  folder, File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
         System.out.println(name + " " + version + " initialized");
     }
 	public boolean onCommand(Player player, Command command, String commandLabel, String[] args) {
 		if(player.isOp()){
 	        String commandName = command.getName().toLowerCase();
 	        if(commandName.equalsIgnoreCase("st_events")){
 	        	player.sendMessage(information.toString());
 	    		return true;
 	        }else if(commandName.equalsIgnoreCase("st_event")){
 	        	if(args.length>1){
 	        		if(information.containsKey(args[1])){
 	        			player.sendMessage(args[1]+" has fired "+information.get(args[1])+" time(s)");
 	        			return true;
 	        		}else{
 	        			player.sendMessage(args[1]+" does not exist!");
 	        			return true;
 	        		}
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_spam_all")){
 	        	player.sendMessage(spammy.toString());
 	    		return true;
 	        }else if(commandName.equalsIgnoreCase("st_spam")){
 	        	if(args.length>1){
 	        		if(spammy.containsKey(args[1])){
 	        			player.sendMessage(args[1]+" spam has been going on for "+(((int)spammy.get(args[1])+1)*5)+" second(s)");
 	        			return true;
 	        		}else{
 	        			player.sendMessage(args[1]+" not a spammy event!");
 	        			return true;
 	        		}
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_toggle")){
 	        	if(args.length>1){
 	        		if(event_disabled.containsKey(args[1])){
 	        			if(event_disabled.get(args[1])){
 	        				event_disabled.put(args[1], false);
 	        				player.sendMessage(args[1]+" Enabled!");
 	        			}else{
 	        				event_disabled.put(args[1], true);
 	        				player.sendMessage(args[1]+" Disabled!");
 	        			}
 	        			return true;
 	        		}else{
 	        			event_disabled.put(args[1], true);
 	        			player.sendMessage(args[1]+" Disabled!");
 	        			return true;
 	        		}
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_stime")){
 	        	if(args.length>2){
 	        		spammy_time.put(args[1], Integer.valueOf(args[2]));
 	        		player.sendMessage(args[1]+" set spammy time!");
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_gtime")){
 	        	if(args.length>1){
 	        		if(spammy_time.containsKey(args[1])){
 	        			player.sendMessage(args[1]+" spam time: "+spammy_time.get(args[1]));
 	        			return true;
 	        		}else{
 	        			player.sendMessage(args[1]+" spam time: 200");
 	        			return true;
 	        		}
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_reset")){
 	        	player.sendMessage("Resetting system tests!");
 	        	RESET();
 	        }else if(commandName.equalsIgnoreCase("st_save")){
 	        	if(args.length>1){
 	        		if(args[1].contentEquals("events")){
 	        			try{
 	        				FileWriter fstream = new FileWriter("st_dump_events.txt");
 	        				BufferedWriter out = new BufferedWriter(fstream);
 	        				for ( Map.Entry<String, Integer> entry : information.entrySet() ){
 	        					String key = entry.getKey();
 	        	    			Integer value = entry.getValue();
 	        					out.write(key+":"+value+"\n");
 	        				}
 	        				out.close();
 	        				player.sendMessage("Events dump file saved!");
 	        			}catch (Exception e){
 	        			}
 	        			return true;
 	        		}else if(args[1].contentEquals("spam")){
 	        			try{
 	        				FileWriter fstream = new FileWriter("st_dump_spam.txt");
 	        				BufferedWriter out = new BufferedWriter(fstream);
 	        				for ( Map.Entry<String, Integer> entry : spammy.entrySet() ){
 	        					String key = entry.getKey();
 	        	    			Integer value = entry.getValue();
 	        					out.write(key+":"+value+"\n");
 	        				}
 	        				out.close();
 	        				player.sendMessage("Spam dump file saved!");
 	        			}catch (Exception e){
 	        			}
 	        			return true;
 	        		}
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_get")){
 	        	if(args.length>1){
 	        		if(args[1].contentEquals("player")){
 	        			player.sendMessage("Player Event Names Below");
 	        			player.sendMessage("----------------------------------");
 	        			for(String event : categorized_events.get("player")){
 	        				player.sendMessage(event);
 	        			}
 	        			return true;
 	        		}else if(args[1].contentEquals("block")){
 	        			player.sendMessage("Block Event Names Below");
 	        			player.sendMessage("----------------------------------");
 	        			for(String event : categorized_events.get("block")){
 	        				player.sendMessage(event);
 	        			}
 	        			return true;
 	        		}else if(args[1].contentEquals("world")){
 	        			player.sendMessage("World Event Names Below");
 	        			player.sendMessage("----------------------------------");
 	        			for(String event : categorized_events.get("world")){
 	        				player.sendMessage(event);
 	        			}
 	        			return true;
 	        		}else if(args[1].contentEquals("entity")){
 	        			player.sendMessage("Entity Event Names Below");
 	        			player.sendMessage("----------------------------------");
 	        			for(String event : categorized_events.get("entity")){
 	        				player.sendMessage(event);
 	        			}
 	        			return true;
 	        		}else if(args[1].contentEquals("vehicle")){
 	        			player.sendMessage("Vehicle Event Names Below");
 	        			player.sendMessage("----------------------------------");
 	        			for(String event : categorized_events.get("entity")){
 	        				player.sendMessage(event);
 	        			}
 	        			return true;
 	        		}
 	        	}
 	        }else if(commandName.equalsIgnoreCase("st_disabled")){
 	        	player.sendMessage("Disabled Events Below");
 	        	player.sendMessage("----------------------------------");
	        	for(String event : categorized_events.get("entity")){
	        		player.sendMessage(event);
	        	}
 	        	return true;
 	        }else if(commandName.equalsIgnoreCase("st_disable_all")){
 	        	for ( Map.Entry<String, List<String>> entry : categorized_events.entrySet() ){
 	    			List<String> event_list = entry.getValue();
 	    			for(String event : event_list){
 	    				event_disabled.put(event, true);
 	    			}
 	    		}
 	        	player.sendMessage("Disabled all events.");
 	        	return true;
 	        }else if(commandName.equalsIgnoreCase("st_enable_all")){
 	        	for ( Map.Entry<String, List<String>> entry : categorized_events.entrySet() ){
 	    			List<String> event_list = entry.getValue();
 	    			for(String event : event_list){
 	    				event_disabled.put(event, false);
 	    			}
 	    		}
 	        	player.sendMessage("Enabled all events.");
 	        	return true;
 	        }
 		}
         return false;
     }
 	public void RESET(){
 		spammy= new Hashtable<String,Integer>();
 		information_old= new Hashtable<String,Integer>();
 		information= new Hashtable<String,Integer>();
 		event_disabled= new Hashtable<String,Boolean>();
 		for ( Map.Entry<String, List<String>> entry : categorized_events.entrySet() ){
 			String key = entry.getKey();
 			List<String> event_list = entry.getValue();
 			System.out.println("SystemTest, loading "+key);
 			for(String event : event_list){
 				information.put(event, 0);
 			}
 		}
         information.put("spawn_item", 0);
         information.put("spawn_creature", 0);
         //information.put("inv_change", 0);
         //information.put("inv_click", 0);
         //information.put("inv_close", 0);
         //information.put("inv_open", 0);
         //information.put("inv_trans", 0);
 	}
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_EGG_THROW, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.CHUNK_GENERATION, worldListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.CHUNK_LOADED, worldListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.CHUNK_UNLOADED, worldListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.CREATURE_SPAWN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_COMBUST, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGEDBY_BLOCK, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGEDBY_PROJECTILE, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.INVENTORY_CHANGE, playerListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.INVENTORY_CLICK, playerListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.INVENTORY_CLOSE, playerListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.INVENTORY_OPEN, playerListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.INVENTORY_TRANSACTION, playerListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.ITEM_SPAWN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.LEAVES_DECAY, blockListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.LIQUID_DESTROY, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_EXIT, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_CREATE, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.VEHICLE_COLLISION_BLOCK, vehicleListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         List<String> playerlisteners = new ArrayList<String>();
         playerlisteners.add("players_move");
         playerlisteners.add("players_teleport");
         playerlisteners.add("players_chat");
         playerlisteners.add("players_item");
         playerlisteners.add("players_command");
         playerlisteners.add("loggedin");
         playerlisteners.add("loggedattempt");
         playerlisteners.add("loggedout");
         categorized_events.put("player",playerlisteners);
         
         List<String> blocklisteners = new ArrayList<String>();
         blocklisteners.add("block_rightclick");
         blocklisteners.add("block_placed");
         blocklisteners.add("block_physics");
         blocklisteners.add("block_interact");
         blocklisteners.add("block_ignite");
         blocklisteners.add("block_flow");
         blocklisteners.add("block_damaged");
         blocklisteners.add("block_canbuild");
         blocklisteners.add("redstone");
         blocklisteners.add("liquid");
         blocklisteners.add("leaves");
         categorized_events.put("block",blocklisteners);
         
         List<String> entitylisteners = new ArrayList<String>();
         entitylisteners.add("entity_combust");
         entitylisteners.add("entity_damaged");
         entitylisteners.add("entity_explode");
         //entitylisteners.add("entity_death");
         entitylisteners.add("entity_damagedby_entity");
         entitylisteners.add("entity_damagedby_block");
         entitylisteners.add("entity_damagedby_projectile");
         categorized_events.put("entity",entitylisteners);
         
         List<String> worldlisteners = new ArrayList<String>();
         worldlisteners.add("chunk_loaded");
         worldlisteners.add("chunk_unloaded");
         worldlisteners.add("chunk_gen");
         categorized_events.put("world",worldlisteners);
         
         List<String> vehiclelisteners = new ArrayList<String>();
         vehiclelisteners.add("vehicle_move");
         vehiclelisteners.add("vehicle_exit");
         vehiclelisteners.add("vehicle_enter");
         vehiclelisteners.add("vehicle_damage");
         vehiclelisteners.add("vehicle_create");
         vehiclelisteners.add("vehicle_collision_entity");
         vehiclelisteners.add("vehicle_collision_block");
         categorized_events.put("vehicle",vehiclelisteners);
         
         RESET();
         Thread thread = new SPAMCHECK(this);
         thread.start();
 	}
 	public void onDisable() {
 	}
 }
