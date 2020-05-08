 package com.westeroscraft.border;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.WeakHashMap;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class BorderPlugin extends JavaPlugin implements Listener {
 	private static final String CONFIG = "config.yml";
 	private ConfigurationSection wConfigs = null;
 	private WeakHashMap<World,Border> borders = new WeakHashMap<World,Border>();
 	private HashMap<String,violationEvent> errorCounter = new HashMap<String,violationEvent>();
 	
 	private class Border {
 		long minX,minZ,maxX,maxZ;
 		Location respawn = null;
 		Border(){ minX = minZ = maxX = maxZ = 0; } //Never do this, could omit this line altogether
 		boolean contains(long x, long z) {
 			return !(x < minX || x > maxX || z < minZ || z > maxZ);
 		}
 		
 	}
 	private class violationEvent {
 		int offense = 0;
 		long lastupdate = 0;
 	}
 	
 	private class teleportEvent implements Runnable{
 		Player p;
 		Location l;
 		teleportEvent(Player p,Location l) {
 			this.p = p;
 			this.l = l;
 		}
 		public void run() {
 			p.teleport(l);			
 		}
 	}
 	
 	private Border loadConfiguration(ConfigurationSection section, World w) {
 		ConfigurationSection bsection = section.getConfigurationSection(w.getName());
 		if(bsection == null) {
 			return null;
 		}
 		ConfigurationSection respawn = bsection.getConfigurationSection("respawn");
 		Location spawn = null;
 		if(respawn != null) {
 			spawn = LCS.fromConfig(respawn, getServer());
 		}
 		
 		Border b = new Border();
 		b.respawn = spawn;
 		b.maxX = bsection.getLong("maxX", 1000);
 		b.maxZ = bsection.getLong("maxZ", 1000);
 		b.minX = bsection.getLong("minX", -1000);
 		b.minZ = bsection.getLong("minZ", -1000);
 		return b;
 	}
 
     public void onEnable() {
         File cFile = new File(this.getDataFolder(),BorderPlugin.CONFIG);
         YamlConfiguration cyaml = new YamlConfiguration();
         try {
 			cyaml.load(cFile);
 		} catch (Exception e) {
 			this.getLogger().log(Level.WARNING, "Unable to load configuration file for " + this.getName());
 			e.printStackTrace();
 		}
         wConfigs = cyaml.getConfigurationSection("worlds");
         if(wConfigs != null) {
 	        for(World w : this.getServer().getWorlds()) {
 	        	Border b = this.loadConfiguration(wConfigs, w);
 	        	if(b == null) {
 	        		continue;
 	        	}
 	        	this.borders.put(w, b);
 	        }
         }
         getServer().getPluginManager().registerEvents(this, this);
     }
     
     public void handleException(Player p, Border b) {
     	p.sendMessage("You have reached the border!");
     	violationEvent v = this.errorCounter.get(p.getName());
     	if(v == null) {
     		v = new violationEvent();
     	}
     	v.offense++;
     	if(System.currentTimeMillis() - v.lastupdate > 15000) {
     		v.offense = 1;
     	}
     	v.lastupdate = System.currentTimeMillis();
     	if(v.offense > 25) {
     		this.errorCounter.remove(p.getName());
    		Location destination = b.respawn;
    		if(destination == null) {
    			destination = p.getWorld().getSpawnLocation();
    		}
    		teleportEvent te = new teleportEvent(p,destination);
     		this.getServer().getScheduler().scheduleSyncDelayedTask(this, te);
     	}
     	this.errorCounter.put(p.getName(), v);
     }
 
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
     	Location destination = event.getTo();
     	Border b = this.borders.get(destination.getWorld());
     	if(b != null && !b.contains(destination.getBlockX(), destination.getBlockZ())) {
     		event.setCancelled(true);
     		handleException(event.getPlayer(),b);
     	}
     }
     @EventHandler
     public void onVehicleMove(VehicleMoveEvent event) {
     	Location destination = event.getTo();
     	Border b = this.borders.get(destination.getWorld());
     	if(b != null && !b.contains(destination.getBlockX(), destination.getBlockZ())) {
     		Vector v = event.getVehicle().getVelocity().multiply(-1);
     		event.getVehicle().setVelocity(v); //Flip the velocity of the vehicle
     		Entity e = event.getVehicle().getPassenger();
     		if(e instanceof Player) {
         		handleException((Player)e,b);
     		}
     	}
     }
     @EventHandler
     public void onPlayerTeleport(PlayerTeleportEvent e) {
     	Location destination = e.getTo();
     	Border b = this.borders.get(destination.getWorld());
     	if(b != null && !b.contains(destination.getBlockX(), destination.getBlockZ())) {
     		e.setCancelled(true);
     		handleException(e.getPlayer(),b);
     	}
     }
     @EventHandler(priority = EventPriority.MONITOR)
     public void onWorldLoad(WorldLoadEvent e) {
     	Border b = this.loadConfiguration(wConfigs, e.getWorld());
     	if(b != null) {
     		this.borders.put(e.getWorld(), b);
     	}
     }
 }
 
