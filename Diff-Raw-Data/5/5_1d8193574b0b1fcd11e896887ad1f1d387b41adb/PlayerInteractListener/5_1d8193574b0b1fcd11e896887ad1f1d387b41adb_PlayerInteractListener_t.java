 package kr.kkiro.projects.bukkit.EntityProtect.events;
 
 import kr.kkiro.projects.bukkit.EntityProtect.utils.BreedChecker;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.ChatUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.EntityActivity;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.EntityUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.PermissionUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.cache.BreedCache;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.DatabaseUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.EntitySet;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class PlayerInteractListener implements Listener {
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		Entity entity = event.getRightClicked();
 		Material material = player.getItemInHand().getType();
 		if (entity == null)
 			return;
 		if (!EntityUtils.isEnabled(entity.getType()))
 			return;
 		EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 		if (entityset != null) {
 			String owner = entityset.getOwnerItem().getPlayer();
 			ChatUtils.sendLang(player, "owner-info", 
 					"#mobs."+entity.getType().getName(),
 					(owner.equals(player.getName())) ? "#you" : owner);
 		}
 		if (entity instanceof Ageable) {
 			if (((Ageable) entity).canBreed() && BreedChecker.check(entity.getType(), material)) {
 				if (PermissionUtils.canBreed(player, entityset)) {
 					BreedCache.getInstance().refresh();
 					BreedCache.getInstance().add(player.getName(), entity);
 					return;
 				} else {
 					event.setCancelled(true);
 					EntityUtils.playEffect(player, entity);
 					return;
 				}
 			}
 		}
 		if (entity instanceof Tameable) {
 			if (!((Tameable) entity).isTamed()
 					&& (entity instanceof Wolf && player.getItemInHand()
 					.getType().equals(Material.BONE))
 					|| (entity instanceof Ocelot && player.getItemInHand()
 					.getType().equals(Material.RAW_FISH))) {
 				if (PermissionUtils.canBreed(player, entityset)) {
 					BreedCache.getInstance().refresh();
 					BreedCache.getInstance().add(player.getName(), entity);
 					return;
 				} else {
 					event.setCancelled(true);
 					EntityUtils.playEffect(player, entity);
 					return;
 				}
 			}
 		}
 		if (entity.getType().equals(EntityType.PIG) && material.equals(Material.SADDLE)) {
 			if (PermissionUtils.canBypass(EntityActivity.SADDLE_PIG, player, entityset)) {
 				return;
 			} else {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		}
 		if (entity.getType().equals(EntityType.SHEEP) && material.equals(Material.SHEARS)) {
 			if (PermissionUtils.canBypass(EntityActivity.SHEAR_SHEEP, player, entityset)) {
 				return;
 			} else {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		}
 		if (entity.getType().equals(EntityType.MUSHROOM_COW) && material.equals(Material.SHEARS)) {
 			if (PermissionUtils.canBypass(EntityActivity.SHEAR_MUSHROOM_COW, player, entityset)) {
 				return;
 			} else {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		}
 		if (entity.getType().equals(EntityType.COW) && material.equals(Material.BUCKET)) {
 			if (PermissionUtils.canBypass(EntityActivity.FILL_COW, player, entityset)) {
 				return;
 			} else {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		}
 		if (entity.getType().equals(EntityType.MUSHROOM_COW) && material.equals(Material.BOWL)) {
 			if (PermissionUtils.canBypass(EntityActivity.FILL_MUSHROOM_COW, player, entityset)) {
 				return;
 			} else {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		}
 		if(entity.getType().equals(EntityType.HORSE) && player.isSneaking()) {
 			if (PermissionUtils.canBypass(EntityActivity.INVENTORY_HORSE, player, entityset)) {
 				return;
 			} else {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
 		Player player = event.getPlayer();
 		Egg entity = event.getEgg();
 		if (PermissionUtils.canBreed(player, null)) {
 			BreedCache.getInstance().refresh();
 			BreedCache.getInstance().add(player.getName(), entity);
 			return;
 		} else {
 			event.setHatching(false);
 			EntityUtils.playEffect(player, entity);
 			return;
 		}
 	}
 	
 	@EventHandler
 	public void onInventoryOpen(InventoryOpenEvent event) {
 		HumanEntity human = event.getPlayer();
 		if(!(human instanceof Player)) {
 			return;
 		}
 		Player player = (Player) human;
 		if(player.getVehicle() != null) {
 			Entity entity = player.getVehicle();
 			if(entity instanceof Horse) {
 				EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 				if (!PermissionUtils.canBypass(EntityActivity.INVENTORY_HORSE, player, entityset)) {
 					ChatUtils.sendLang(player, "access-denied");
 					event.setCancelled(true);
 					EntityUtils.playEffect(player, entity);
 					return;
 				}
				//TODO: workaround before horse tame event comes
				EntityUtils.registerEntity(player.getName(), entity);
 			}
 		}
 	}
 	//TODO: Support monster eggs
 }
