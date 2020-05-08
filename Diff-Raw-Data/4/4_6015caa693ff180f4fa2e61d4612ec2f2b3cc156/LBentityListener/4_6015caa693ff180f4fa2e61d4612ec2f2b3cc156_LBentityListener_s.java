 package org.lavaboat;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.Vector;
 
 public class LBentityListener implements Listener{
 
 		public static LavaBoat plugin;
 	    public LBentityListener(LavaBoat instance) {
 			plugin = instance;
 	    }
 	    @EventHandler
 	    public void onEntityFire(EntityCombustEvent event) {
 	    	Entity e= event.getEntity();
 	    	Boolean use = Boolean.parseBoolean(LavaBoat.config.getWorld(e.getWorld().getName()));//getting if it's enabled in this world
 			if(use){
 				if(e instanceof Player){
 					Player player=(Player) event.getEntity();
 					if(player.getVehicle() instanceof Boat  && plugin.canUseB(player)){		
 						event.setCancelled(true);
 						player.setFireTicks(0);	
 						player.getVehicle().setFireTicks(0);
 					}
 					else if(player.getVehicle() instanceof Minecart && plugin.canUseM(player)){		
 						event.setCancelled(true);
 						player.setFireTicks(0);
 						player.getVehicle().setFireTicks(0);
 					}
 					/*else if(player2.getVehicle() instanceof Pig && plugin.canUseP(player2)){		
 						event.setCancelled(true);
 						player2.setFireTicks(0);
 						player2.getVehicle().setFireTicks(0);
 					}*/
 					Location loc = e.getLocation();
 					Material mat=loc.getWorld().getBlockAt(loc).getType();
 					if(mat==Material.LAVA||mat==Material.STATIONARY_LAVA&&plugin.canWalk(player)){
 						event.setCancelled(true);
 						e.setFireTicks(0);
 						e.setVelocity(new Vector(e.getVelocity().getX(),0.3,e.getVelocity().getZ()));
 		    		}
 				}
 				/*else if(event.getEntity() instanceof Pig){
 					event.getEntity().setFireTicks(0);
 					event.setCancelled(true);
 				}*/
 			}
 		}
 	@EventHandler
 	public void onEntityloseLife(EntityDamageEvent event) {
 		if (event.getEntity() instanceof Player){
 			Player player=(Player)event.getEntity();
 			Boolean use = Boolean.parseBoolean(LavaBoat.config.getWorld(player.getWorld().getName()));//getting if it's enabled in this world
 			if(use){
 				if (player.getVehicle() instanceof Boat && plugin.canUseB(player)) {
 					event.setCancelled(true);
 					event.getEntity().setFireTicks(0);
 					player.getVehicle().setFireTicks(0);
 				}
 				else if (player.getVehicle() instanceof Minecart && plugin.canUseM(player)) {
 					event.setCancelled(true);
 					event.getEntity().setFireTicks(0);
 					player.getVehicle().setFireTicks(0);
 				}
 				/*else if (player.getVehicle() instanceof Pig && plugin.canUseP(player)) {
 					event.setCancelled(true);
 					event.getEntity().setFireTicks(0);
 					player.getVehicle().setFireTicks(0);
 				}*/
 				Location loc = player.getLocation();
 				Material mat=loc.getWorld().getBlockAt(loc).getType();
 				if(mat==Material.LAVA||mat==Material.STATIONARY_LAVA && plugin.canWalk(player)){
 					event.setCancelled(true);
 					player.setFireTicks(0);
 	    		}
 			}
 		}
		if(event.getEntity() instanceof Vehicle && event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.ENTITY_ATTACK ||
				event.getEntityType() == EntityType.BOAT){
 			event.setCancelled(false);
 			event.setDamage(1000);
 		}
 		/*else if(event.getEntity() instanceof Pig&&event.getCause()==DamageCause.LAVA||event.getCause()==DamageCause.FIRE_TICK||
 		 * event.getCause()==DamageCause.FIRE){
 			Boolean use = Boolean.parseBoolean(LavaBoat.config.getWorld(event.getEntity().getWorld().getName()));
 			if(use){
 				event.getEntity().setFireTicks(0);
 				event.setDamage(0);
 				event.setCancelled(true);
 			}
 		}*/
 	}
 }
 
 
 
