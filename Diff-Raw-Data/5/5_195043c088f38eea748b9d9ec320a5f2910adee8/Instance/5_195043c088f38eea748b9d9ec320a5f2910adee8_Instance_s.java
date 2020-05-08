 package com.mcnsa.instanceportals.containers;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 public class Instance {
 	private InstanceSet parent;
 	public Location arrival;
 	public PortalRegion departure;
 	public PortalRegion container;
 	private ArrayList<Player> players = new ArrayList<Player>();
 	
 	public Instance(InstanceSet _parent) {
 		parent = _parent;
 	}
 	
 	public Instance(InstanceSet _parent, Location _arrival, PortalRegion _departure) {
 		parent = _parent;
 		arrival = _arrival;
 		departure = _departure;
 	}
 	
 	public Location getArrival() {
 		return arrival;
 	}
 	
 	public void setArrival(Location loc) {
 		arrival = loc;
 	}
 	
 	public void setDeparture(PortalRegion _departure) {
 		departure = _departure;
 	}
 	
 	public void setContainer(PortalRegion _container) {
 		container = _container;
 	}
 	
 	public Integer getNumPlayers() {
 		return players.size();
 	}
 	
 	public boolean hasPlayer(Player player) {
 		return players.contains(player);
 	}
 	
 	public boolean playerDeparting(Player player) {
 		if(departure == null) {
 			//parent.plugin.debug(player.getName() + " not departing since departure is null");
 			return false;
 		}
 		return departure.containsPlayer(player);
 	}
 	
 	public void bringPlayer(Player player) {
 		if(departure != null && !players.contains(player)) {
 			//player.teleport(arrival);
 			parent.plugin.transportManager.transport(player, arrival);
 			player.setFallDistance(0f);
 			players.add(player);
 			//parent.plugin.debug("added " + player.getName() + " to instance!");
 		}
 	}
 	
 	public void checkAndHandleDepartures(boolean reset) {
 		// now loop over all the players we have here
 		/*if(reset) {
 			parent.plugin.debug("booting " + players.size() + " players due to instance reset");
 		}
 		else {
 			parent.plugin.debug("checking " + players.size() + " players for instance departure");
 		}*/
 		boolean booted = false;
 		Iterator<Player> iterator = players.iterator();
 		ArrayList<Player> removeList = new ArrayList<Player>();
 		while(iterator.hasNext()) {
 			Player player = iterator.next();
 			if(playerDeparting(player)) {
 				parent.transportToExit(player);
 				removeList.add(player);
 				booted = true;
 			}
 			else if(reset) {
 				// teleport them out to the boot exit
 				//parent.plugin.debug("booting " + player.getName() + " from instance for departing");
 				bootPlayerFromInstance(player, true, false);
 				removeList.add(player);
 				booted = true;
 			}
 			/*else {
 				parent.plugin.debug(player.getName() + " not in portal: no action");
 				parent.plugin.debug("\t" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
 				parent.plugin.debug("\t\t" + departure.min.getBlockX() + ", " + departure.min.getBlockY() + ", " + departure.min.getBlockZ());
 				parent.plugin.debug("\t\t" + departure.max.getBlockX() + ", " + departure.max.getBlockY() + ", " + departure.max.getBlockZ());
 			}*/
 		}
 		
 		// now actually remove the players from the array
 		for(int i = 0; i < removeList.size(); i++) {
 			players.remove(removeList.get(i));
 		}
 		removeList.clear();
 		
 		if(booted && players.size() < 1) {
 			// reset the region!
 			resetRegion();
 		}
 	}
 	
 	public void bootPlayerFromInstance(Player player, boolean transport, boolean remove) {
 		if(players.contains(player)) {
 			// send them to the entrance
 			if(remove) {
 				players.remove(player);
 			}
 			if(transport) {
 				//parent.plugin.debug("transported " + player.getName() + " to instance set entrance!");
 				parent.transportToEntrance(player);
 			}
 			//parent.plugin.debug("removed " + player.getName() + " from instance!");
 		}
 	}
 	
 	public void checkReset() {
 		if(players.size() < 1) {
 			resetRegion();
 		}
 	}
 	
 	private void resetRegion() {
 		// reset levers
 		//parent.plugin.debug("resetting instance..");
 		boolean resetPulse = false;
 		for(int x = container.min.getBlockX(); x <= container.max.getBlockX(); x++) {
 			for(int y = container.min.getBlockY(); y <= container.max.getBlockY(); y++) {
				for(int z = container.min.getBlockZ(); z <= container.max.getBlockX(); z++) {
 					if(parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y, z).getType().equals(Material.LEVER)) {
 						byte data = parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y, z).getData();
 						//parent.plugin.debug("resetting lever at: " + x + "," + y + "," + z);
 						parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y, z).setData((byte)(data & 247), true);
 					}
 					else if(parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y, z).getType().equals(Material.CLAY)) {
 						//parent.plugin.debug("setting redstonetorch at: " + x + "," + (y+1) + "," + z);
 						parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y + 1, z).setType(Material.REDSTONE_TORCH_ON);
 						resetPulse = true;
 					}
 				}
 			}
 		}
 		
 		// schedule a reset
 		if(resetPulse) {
 			parent.plugin.getServer().getScheduler().scheduleSyncDelayedTask(parent.plugin, new Runnable() {
 				@Override
 				public void run() {
 					for(int x = container.min.getBlockX(); x <= container.max.getBlockX(); x++) {
 						for(int y = container.min.getBlockY(); y <= container.max.getBlockY(); y++) {
							for(int z = container.min.getBlockZ(); z <= container.max.getBlockX(); z++) {
 								if(parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y, z).getType().equals(Material.CLAY)) {
 									//parent.plugin.debug("clearing redstonetorch at: " + x + "," + (y+1) + "," + z);
 									parent.plugin.getServer().getWorld(container.worldName).getBlockAt(x, y + 1, z).setType(Material.AIR);
 								}
 							}
 						}
 					}
 				}
 			}, 20l);
 		}
 	}
 	
 	public void reset() {
 		checkAndHandleDepartures(true);
 	}
 }
