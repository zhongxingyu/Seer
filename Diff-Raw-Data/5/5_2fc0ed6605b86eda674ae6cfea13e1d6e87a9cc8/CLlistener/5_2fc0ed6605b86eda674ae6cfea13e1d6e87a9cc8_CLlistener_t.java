 package net.teamio.server.utils.cartload;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.vehicle.VehicleCreateEvent;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.util.Vector;
 
 public class CLlistener implements Listener {
 	
	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onVehicleCreate(VehicleCreateEvent e){
 		Vehicle v = e.getVehicle();
 		if (v instanceof Minecart){
 			if (CartLoad.config.getProperty("minecarts_dont_slow_down","false").equalsIgnoreCase("true"))
 				((Minecart)v).setSlowWhenEmpty(false);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onVehicleMove(VehicleMoveEvent event) {
 		if (!CartLoad.config.getProperty("load_chunks_on_move", "true")
 				.equalsIgnoreCase("false")) {
 			Vehicle vehicle = event.getVehicle();
 			Location location = vehicle.getLocation();
 			Block block = location.getBlock();
 			Chunk chunk = block.getChunk();
 
 			int x = chunk.getX();
 			int z = chunk.getZ();
 
 			World world = chunk.getWorld();
 
 			int radius = Math.abs(Integer.parseInt(CartLoad.config.getProperty(
 					"radius_of_loaded_chunks", "3")));
 
 			for (int i = -radius; i <= radius; i++) {
 				for (int j = -radius; j <= radius; j++) {
 					if (world.isChunkLoaded(x + i, j + z))
 						continue;
 					world.loadChunk(x + i, z + j);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
 		boolean instanceofplayer = event.getEntity() instanceof Player;
 		boolean ignoreplayers = Boolean.parseBoolean(CartLoad.config
 				.getProperty("minecarts_ignore_players"));
 		boolean ignoreentities = Boolean.parseBoolean(CartLoad.config
 				.getProperty("minecarts_ignore_entities"));
 
 		if ((instanceofplayer) && (ignoreplayers)) {
 			event.setCancelled(true);
 			event.setCollisionCancelled(true);
 		} else if ((ignoreentities) && (!instanceofplayer)) {
 			event.setCancelled(true);
 			event.setCollisionCancelled(true);
 		}
 
 		if (((event.getEntity() instanceof Player))
 				&& (CartLoad.config.getProperty("minecarts_run_over_players",
 						"false").equalsIgnoreCase("true"))) {
 			if (event.getVehicle().getVelocity().length() > 0.08D) {
 				Vector velocity = event.getVehicle().getVelocity();
 				Location location = event.getVehicle().getLocation();
 				Location loc2 = event.getEntity().getLocation();
 				double dx = loc2.getX() - location.getX();
 				double dz = loc2.getZ() - location.getZ();
 				velocity.setX(dx);
 				velocity.setZ(dz);
 				velocity.setY(1);
 
 				event.getEntity().setVelocity(velocity);
 			}
 		} else if (((event.getEntity() instanceof LivingEntity))
 				&& (!(event.getEntity() instanceof Player))
 				&& (CartLoad.config.getProperty("minecarts_run_over_mobs",
 						"false").equalsIgnoreCase("true"))
 				&& (event.getVehicle().getVelocity().length() > 0.08D)) {
 			Vector velocity = event.getVehicle().getVelocity();
 			Location location = event.getVehicle().getLocation();
 			Location loc2 = event.getEntity().getLocation();
 			double dx = loc2.getX() - location.getX();
 			double dz = loc2.getZ() - location.getZ();
 			velocity.setX(dx);
 			velocity.setZ(dz);
 			velocity.setY(1);
 			velocity.multiply(2);
 			event.getEntity().setVelocity(velocity);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onChunkUnload(ChunkUnloadEvent event) {
 		if (CartLoad.config
 				.getProperty("keep_stationary_carts_loaded", "true")
 				.equalsIgnoreCase("true")) {
 			Chunk chunk = event.getChunk();
 			int x = chunk.getX();
 			int z = chunk.getZ();
 
 			World world = chunk.getWorld();
 
 			int radius = Math.abs(Integer.parseInt(CartLoad.config.getProperty(
 					"radius_of_loaded_chunks", "3")));
 
 			for (int i = -radius; i <= radius; i++) {
 				for (int j = -radius; j <= radius; j++) {
					Chunk testchunk = world.getChunkAt(x + i, z + j);
 					Entity[] entities = testchunk.getEntities();
 
 					for (Entity entity : entities){
 						if ((entity instanceof Minecart)){
 							event.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
