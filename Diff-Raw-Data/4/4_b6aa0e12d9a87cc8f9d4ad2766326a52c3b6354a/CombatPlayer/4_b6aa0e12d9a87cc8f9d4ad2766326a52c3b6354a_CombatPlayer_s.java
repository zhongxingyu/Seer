 package net.milkbowl.combatevents;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.milkbowl.combatevents.tasks.LeaveCombatTask;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 
 public class CombatPlayer {
 	private Player player;
 	private Map<Entity, CombatReason> reasons = new HashMap<Entity, CombatReason>(2);
 	private ItemStack[] inventory;
 	private Location lastLocation;
 	private int taskId;
 
 	public CombatPlayer(Player player, CombatReason reason, Entity entity, CombatEventsCore plugin) {
 		this.player = player;
 		this.lastLocation = player.getLocation();
 		this.inventory = player.getInventory().getContents();
 		reasons.put(entity, reason);
 		//if we create a new object always force the player into combat so make a new scheduler for leaving combat
		taskId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new LeaveCombatTask(player, LeaveCombatReason.TIMED, (CombatReason[]) reasons.values().toArray(), plugin), Config.getCombatTime() * 20);
 	}
 
 	public Map<Entity, CombatReason> getReasonMap() {
 		return reasons;
 	}
 
 	public void addReason(Entity entity, CombatReason reason) {
 		this.reasons.put(entity, reason);
 	}
 	
 	public CombatReason getReason(Entity entity) {
 		return this.reasons.get(entity);
 	}
 	
 	public CombatReason[] getReasons() {
 		return (CombatReason[]) this.reasons.values().toArray();
 	}
 
 	public CombatReason removeReason(Entity entity) {
 		return this.reasons.remove(entity);
 	}
 	
 	public void clearReasons() {
 		this.reasons.clear();
 	}
 	
 	public ItemStack[] getInventory() {
 		return inventory;
 	}
 
 	public void setInventory(ItemStack[] inventory) {
 		this.inventory = inventory;
 	}
 
 	public Location getLastLocation() {
 		return lastLocation;
 	}
 
 	public void setLastLocation(Location lastLocation) {
 		this.lastLocation = lastLocation;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getTaskId() {
 		return taskId;
 	}
 
 	public void setTaskId(int taskId) {
 		this.taskId = taskId;
 	}	
 }
