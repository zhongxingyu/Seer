 package tv.mineinthebox.ManCo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.inventory.ItemStack;
 
 import tv.mineinthebox.ManCo.configuration.configuration;
 import tv.mineinthebox.ManCo.events.cratescheduler;
 import tv.mineinthebox.ManCo.exceptions.ChestHasNoOwnerException;
 import tv.mineinthebox.ManCo.exceptions.InvalidChestStorageException;
 import tv.mineinthebox.ManCo.exceptions.InvalidRareCrateException;
 import tv.mineinthebox.ManCo.exceptions.PlayerOnSlabException;
 import tv.mineinthebox.ManCo.utils.normalCrate;
 import tv.mineinthebox.ManCo.utils.normalCrateList;
 import tv.mineinthebox.ManCo.utils.rareCrate;
 import tv.mineinthebox.ManCo.utils.rareCrateList;
 import tv.mineinthebox.ManCo.utils.util;
 
 public class api implements Listener {
 
 	private static HashMap<Entity, ItemStack[]> entityChest = new HashMap<Entity, ItemStack[]>();
 	
 	private ArrayList<String> getRareCrateList() {
 		return rareCrate.getRareCrateList();
 	}
 
 	public ItemStack[] convertArrayListToItemStackArray(ArrayList<ItemStack> array) {
		ItemStack[] items = new ItemStack[array.size()];
 		return items;
 	}
 	
 	public boolean isCrate(Chest chest) {
 		if(normalCrateList.getCrateList.containsValue(chest)) {
 			return true;
 		} else if(normalCrateList.getCrateList2.containsValue(chest)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isRareCrate(Chest chest) {
 		if(rareCrateList.getCrateList.containsValue(chest)) {
 			return true;
 		} else if(rareCrateList.getCrateList2.containsValue(chest)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public void destroyCrate(Player p) {
 		configuration.clearPlayerCrate(p);
 	}
 	
 	public void destroyCrate(String name) {
 		configuration.clearPlayerCrate(name);
 	}
 	
 	public String getCrateOwner(Chest chest) throws ChestHasNoOwnerException {
 		String normal = configuration.getNormalChestOwner(chest);
 		String rare = configuration.getRareChestOwner(chest);
 		if(normal != null) {
 			return normal;
 		} else if(rare != null) {
 			return rare;
 		} else {
 			throw new ChestHasNoOwnerException("[ManCo-API]ChestHasNoOwnerException: this chest has no ManCo ownership!");
 		}
 	}
 	
 	 
 	public void spawnCrate(Location loc, ItemStack[] items) throws PlayerOnSlabException {
 		if(util.isSlab(loc.getBlock())) {
 			throw new PlayerOnSlabException("[ManCo-API]PlayerOnSlabException: this crate cannot fall on a slab!\ndebug information:\nVersion: + " + manCo.getPlugin().getDescription().getVersion() +  "\nLocation: "+loc.toString() + "\nitems: "+items.toString());
 		} else {
 			int y = normalCrate.getCrateSpawnHeight(loc);
 			loc.setY(loc.getY() + y);
 			FallingBlock entity = loc.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 			entityChest.put(entity, items);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	private void fillChestApi(EntityChangeBlockEvent e) throws InvalidChestStorageException {
 		if(entityChest.containsKey(e.getEntity())) {
 			e.setCancelled(true);
 			e.getBlock().setTypeIdAndData(Material.CHEST.getId(), (byte) 1, true);
 			Chest chest = (Chest) e.getBlock().getState();
 			if(entityChest.get(e.getEntity()).length < 27) {
				chest.getInventory().setContents(entityChest.get(e.getEntity()));
 				entityChest.remove(e.getEntity());
 			} else {
 				//memory safety in case it throws allready.
 				entityChest.remove(e.getEntity());
 				throw new InvalidChestStorageException("[ManCo-API]InvalidChestStorage: the ItemStack[] amount is more than the single chest could obtain!");
 			}
 		}
 	}
 
 	public void spawnCrate(Player p) throws PlayerOnSlabException {
 		if(util.isSlab(p.getLocation().getBlock())) {
 			throw new PlayerOnSlabException("[ManCo-API]PlayerOnSlabException: this crate cannot fall on a slab!\ndebug information:\nVersion: + " + manCo.getPlugin().getDescription().getVersion() +  "\nLocation: "+p.getLocation().toString());
 		} else {
 			cratescheduler.doCrateNative(p);
 		}
 	}
 
 	public void spawnRareCrate(Player p, String crateName) throws InvalidRareCrateException {
 		if(!rareCrate.getRareCrateList().contains(crateName)) {
 			throw new InvalidRareCrateException("[ManCo-API]InvalidRareCrateException: invalid crate name!");
 		} else {
 			for(int i = 0; i < getRareCrateList().size(); i++) {
 				if(i == getRareCrateList().size()) {
 					if(getRareCrateList().get(i) == crateName) {
 						cratescheduler.doRareCrateNative(p, i);
 						break;
 					} else {
 						throw new InvalidRareCrateException("[ManCo-API]InvalidRareCrateException: invalid crate name! maybe the name is case sensitive?");
 					}
 				} else {
 					if(getRareCrateList().get(i) == crateName) {
 						cratescheduler.doRareCrateNative(p, i);
 						break;
 					}	
 				}
 			}
 		}
 	}
 
 }
