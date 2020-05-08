 package net.croxis.plugins.civilmineation;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.PermissionComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.civilmineation.components.SignComponent;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class ActionPermissionListener implements Listener{
 	private long time;
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event){
 		if (event.isCancelled())
 			return;
 		time = System.currentTimeMillis();
 		if (event.getPlayer().hasPermission("civilmineation.admin"))
 			return;
 		//Block block = event.getBlock();
 		
 		PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 		if (plot.getCity() == null)
 			return;
 		
 		// Determine player's relationship to chunk
 		CityComponent city = plot.getCity();
 		//CivilizationComponent civ = city.getCivilization();
 		
 		PermissionComponent cityPerm = CivAPI.getPermissions(city.getEntityID());		 
 		ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 		
 		// TODO: apply plot specific permissions
 		if (plot.getResident() == null){
 			if (resident.getCity() == null){
 				 Civilmineation.logDebug("City is null");
 				 if (!cityPerm.isOutsiderEdit()){
 					 event.setCancelled(true);
 					 Civilmineation.logDebug("No outsider build");
 					 return;
 				 }
 			 } else if (resident.getCity().getName().equalsIgnoreCase(city.getName())){
 				 if (!cityPerm.isResidentEdit()){
 					 event.setCancelled(true);
 					 Civilmineation.logDebug("No res build");
 					 return;
 				 }
 			 } else {
 				 if (!cityPerm.isOutsiderEdit()){
 					 event.setCancelled(true);
 					 Civilmineation.logDebug("No outsider build 2");
 					 return;
 				 }
 			 }
 		 } else {
 			 if (resident.getName().equalsIgnoreCase(plot.getResident().getName()))
 					return;
 			 PermissionComponent perm = CivAPI.getPermissions(plot.getResident().getEntityID());
 			 if (resident.getCity() == null){
 				 Civilmineation.logDebug("City is null");
 				 if (!perm.isOutsiderEdit()){
 					 event.setCancelled(true);
 					 Civilmineation.logDebug("No outsider build");
 					 return;
 				 }
 			 } else if (resident.getCity().getName().equalsIgnoreCase(city.getName())){
 				//TODO: Friends list here
 				 if (!perm.isResidentEdit()){
 					 event.setCancelled(true);
 					 Civilmineation.logDebug("No res build");
 					 return;
 				 }
 			 } else {
 				 if (!perm.isOutsiderEdit()){
 					 event.setCancelled(true);
 					 Civilmineation.logDebug("No outsider build 2");
 					 return;
 				 }
 			 }
 		 }
 		 if (event.getPlayer().getName().equalsIgnoreCase("croxis"))
 			 Civilmineation.logDebug("Block place event: " + Long.toString(System.currentTimeMillis() - time) + " ms");
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		if (event.isCancelled())
 			return;
 		
 		time = System.currentTimeMillis();
 		PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 		if (plot.getCity() == null)
 			return;
 		// Determine player's relationship to chunk
 		CityComponent city = plot.getCity();
 		//CivilizationComponent civ = city.getCivilization();
 		 
 		PermissionComponent cityPerm = CivAPI.getPermissions(city.getEntityID());
 		 
 		ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 		SignComponent signComp = CivAPI.getSign(SignType.CITY_CHARTER, city.getEntityID());	
 		
 		if (!event.getPlayer().hasPermission("civilmineation.admin")){
 			// Prevent destruction of charter info blocks and blocks behind charter
 			if (event.getBlock().getX() == signComp.getX() && 
 					event.getBlock().getY() + 1 == signComp.getY() &&
 					event.getBlock().getZ() == signComp.getZ()){
 				event.setCancelled(true);
 				return;
 			}
 			if (signComp.getRotation() == 2){
 				// Block behind charter
 				if (event.getBlock().getX() == signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() - 1 == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				// Signs left and right
 				if ((event.getBlock().getX() + 1== signComp.getX() || event.getBlock().getX() - 1== signComp.getX()) && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				// Blocks bedhind left and right
 				if ((event.getBlock().getX() + 1== signComp.getX() || event.getBlock().getX() - 1== signComp.getX()) && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() - 1 == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				//Block behind demographics
 				if (event.getBlock().getX() == signComp.getX() && 
 						event.getBlock().getY() + 1 == signComp.getY() &&
 						event.getBlock().getZ() - 1 == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 			} else if (signComp.getRotation() == 3) {
 				// Block behind charter
 				if (event.getBlock().getX() == signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() + 1 == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				// Signs left and right
 				if ((event.getBlock().getX() + 1== signComp.getX() || event.getBlock().getX() - 1== signComp.getX()) && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				// Blocks bedhind left and right
 				if ((event.getBlock().getX() + 1== signComp.getX() || event.getBlock().getX() - 1== signComp.getX()) && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() + 1 == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				//Block behind demographics
 				if (event.getBlock().getX() == signComp.getX() && 
 						event.getBlock().getY() + 1 == signComp.getY() &&
 						event.getBlock().getZ() + 1 == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 			} else if (signComp.getRotation() == 4) {
 				// Block behind charter
 				if (event.getBlock().getX() + 1== signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				// Signs left and right
 				if (event.getBlock().getX() == signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						(event.getBlock().getZ() - 1 == signComp.getZ() || event.getBlock().getZ() + 1 == signComp.getZ())){
 					event.setCancelled(true);
 					return;
 				}
 				// Blocks bedhind left and right
 				if (event.getBlock().getX() + 1 == signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						(event.getBlock().getZ() - 1 == signComp.getZ() || event.getBlock().getZ() + 1 == signComp.getZ())){
 					event.setCancelled(true);
 					return;
 				}
 				//Block behind demographics
 				if (event.getBlock().getX() + 1 == signComp.getX() && 
 						event.getBlock().getY() + 1 == signComp.getY() &&
 						event.getBlock().getZ() == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 			} else if (signComp.getRotation() == 5) {
 				// Block behind charter
 				if (event.getBlock().getX() - 1== signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						event.getBlock().getZ() == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 				// Signs left and right
 				if (event.getBlock().getX() == signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						(event.getBlock().getZ() - 1 == signComp.getZ() || event.getBlock().getZ() + 1 == signComp.getZ())){
 					event.setCancelled(true);
 					return;
 				}
 				// Blocks bedhind left and right
 				if (event.getBlock().getX() - 1 == signComp.getX() && 
 						event.getBlock().getY() == signComp.getY() &&
 						(event.getBlock().getZ() - 1 == signComp.getZ() || event.getBlock().getZ() + 1 == signComp.getZ())){
 					event.setCancelled(true);
 					return;
 				}
 				//Block behind demographics
 				if (event.getBlock().getX() - 1 == signComp.getX() && 
 						event.getBlock().getY() + 1 == signComp.getY() &&
 						event.getBlock().getZ() == signComp.getZ()){
 					event.setCancelled(true);
 					return;
 				}
 			}
 			
 			if (plot.getResident() == null){
 				if (resident.getCity() == null){
 					 if (!cityPerm.isOutsiderEdit()){
 						 event.setCancelled(true);
 						 return;
 					 }
 				 } else if (resident.getCity().getName().equalsIgnoreCase(city.getName())){
 					 if (!cityPerm.isResidentEdit()){
 						 event.setCancelled(true);
 						 return;
 					 }
 				 } else {
 					 if (!cityPerm.isOutsiderEdit()){
 						 event.setCancelled(true);
 						 return;
 					 }
 				 }
 			} else {
 				PermissionComponent perm = CivAPI.getPermissions(plot.getResident().getEntityID());
 				if (resident.getName().equalsIgnoreCase(plot.getResident().getName()))
 					return;
 				if (resident.getCity() == null){
 					if (!perm.isOutsiderEdit()){
 						event.setCancelled(true);
 						return;
 					}
 				} else if (resident.getCity().getName().equalsIgnoreCase(city.getName())){
 					//TODO: Friends list here
 					 if (!perm.isResidentEdit()){
 						 event.setCancelled(true);
 						 return;
 					 }
 				} else {
 					 if (!perm.isOutsiderEdit()){
 						 event.setCancelled(true);
 						 return;
 					 }
 				}
 			}
 		}
 		// Prevent destruction of charter except for mayor
 		if (event.getBlock().getX() == signComp.getX() && 
 				event.getBlock().getY() == signComp.getY() &&
 				event.getBlock().getZ() == signComp.getZ() && !resident.isMayor()){
 			event.setCancelled(true);
 			return;
 		} else if (event.getBlock().getX() == signComp.getX() && 
 				event.getBlock().getY() == signComp.getY() &&
 				event.getBlock().getZ() == signComp.getZ() && resident.isMayor()){
 			Civilmineation.logDebug("Disbanding city");
 			CivAPI.disbandCity(resident.getCity());
 		}
 		if (event.getPlayer().getName().equalsIgnoreCase("croxis"))
 			Civilmineation.logDebug("Block break event: " + Long.toString(System.currentTimeMillis() - time) + " ms");
 	}
 	@EventHandler
 	public void onInteract(PlayerInteractEvent event){
 		if (event.isCancelled())
 			return;
 		time = System.currentTimeMillis();
 		if (event.getPlayer().hasPermission("civilmineation.admin"))
 			return;
 		if (event.getClickedBlock().getTypeId() == 68)
 			return;
 		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK){
 			PlotComponent plot = CivAPI.getPlot(event.getClickedBlock().getChunk());
 			if (plot.getCity() == null)
 				return;
 			
 			// Determine player's relationship to chunk
 			CityComponent city = plot.getCity();
 			//CivilizationComponent civ = city.getCivilization();
 			
 			PermissionComponent cityPerm = CivAPI.getPermissions(city.getEntityID());		 
 			ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 			
 			// TODO: apply plot specific permissions
 			if (plot.getResident() == null){
 				if (resident.getCity() == null){
 					 Civilmineation.logDebug("City is null");
 					 if (!cityPerm.isOutsiderItemUse()){
 						 event.setCancelled(true);
 						 Civilmineation.logDebug("No outsider item");
 						 return;
 					 }
 				 } else if (resident.getCity().getName().equalsIgnoreCase(city.getName())){
 					 if (!cityPerm.isResidentItemUse()){
 						 event.setCancelled(true);
 						 Civilmineation.logDebug("No res item");
 						 return;
 					 }
 				 } else {
 					 if (!cityPerm.isOutsiderItemUse()){
 						 event.setCancelled(true);
 						 Civilmineation.logDebug("No outsider item 2");
 						 return;
 					 }
 				 }
 			 } else {
 				 if (resident.getName().equalsIgnoreCase(plot.getResident().getName()))
 						return;
 				 PermissionComponent perm = CivAPI.getPermissions(plot.getResident().getEntityID());
 				 if (resident.getCity() == null){
 					 Civilmineation.logDebug("City is null");
 					 if (!perm.isOutsiderItemUse()){
 						 event.setCancelled(true);
 						 Civilmineation.logDebug("No outsider item use");
 						 return;
 					 }
 				 } else if (resident.getCity().getName().equalsIgnoreCase(city.getName())){
 					//TODO: Friends list here
 					 if (!perm.isResidentItemUse()){
 						 event.setCancelled(true);
 						 Civilmineation.logDebug("No res item use");
 						 return;
 					 }
 				 } else {
 					 if (!perm.isOutsiderItemUse()){
 						 event.setCancelled(true);
 						 Civilmineation.logDebug("No outsider item use 2");
 						 return;
 					 }
 				 }
 			 }
 			 if (event.getPlayer().getName().equalsIgnoreCase("croxis"))
 				 Civilmineation.logDebug("Block place event: " + Long.toString(System.currentTimeMillis() - time) + " ms");
 		}
 	}
 
 }
