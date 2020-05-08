 package in.thekreml.plugins.multiarrow;
 
 import in.thekreml.plugins.multiarrow.arrows.*;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.MaterialData;
 
 //import com.iConomy.iConomy;
 
 /**
  * Changes arrow types, fires arrows
  * @author ayan4m1
  */
 public class MultiArrowPlayerListener implements Listener {
 	private final MultiArrow plugin;
 
 	public MultiArrowPlayerListener(MultiArrow instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (plugin.activeArrowType.containsKey(event.getPlayer().getName())) {
 			plugin.activeArrowType.remove(event.getPlayer().getName());
 		}
 	}
 
 	@EventHandler(priority=EventPriority.HIGH)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		if (player.getItemInHand().getType() == Material.BOW) {
 			if (event.getAction() == Action.RIGHT_CLICK_AIR	|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				event.setCancelled(true);
 
 				if (!plugin.activeArrowType.containsKey(player.getName())) {
 					plugin.activeArrowType.put(player.getName(), ArrowType.NORMAL);
 				}
 
 				ArrowType arrowType = plugin.activeArrowType.get(player.getName());
 				MaterialData arrowMaterial = plugin.config.getReqdMaterialData(arrowType);
 
 				PlayerInventory inventory = player.getInventory();
 				if (!player.hasPermission("multiarrow.free-materials") && arrowMaterial.getItemType() != Material.AIR) {
 					String arrowMaterialName = arrowMaterial.getItemType().toString().toLowerCase().replace('_', ' ');
 					if (arrowMaterial.getData() > 0) {
 						arrowMaterialName += " (" + ((Byte)arrowMaterial.getData()).toString() + ")";
 					}
 					if (inventory.contains(arrowMaterial.getItemType())) {
 						ItemStack reqdStack = inventory.getItem(inventory.first(arrowMaterial.getItemType()));
 						if (reqdStack.getAmount() > 1) {
 							reqdStack.setAmount(reqdStack.getAmount() - 1);
 						} else {
 							inventory.clear(inventory.first(arrowMaterial.getItemType()));
 						}
 					} else {
 						player.sendMessage("You do not have any " + arrowMaterialName);
 						return;
 					}
 				}
 
 				if (!player.hasPermission("multiarrow.infinite")) {
 					if (inventory.contains(Material.ARROW)) {
 						ItemStack arrowStack = inventory.getItem(inventory.first(Material.ARROW));
 						if (arrowStack.getAmount() > 1) {
 							arrowStack.setAmount(arrowStack.getAmount() - 1);
 						} else {
 							inventory.remove(arrowStack);
 						}
 					} else {
 						player.sendMessage("Out of arrows!");
 						return;
 					}
 				}
 
 				//HACK: Without this the arrow count does not update correctly
 				player.updateInventory();
 
				player.shootArrow();
 			} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				if (plugin.activeArrowType.containsKey(player.getName())) {
 					// Get the currently selected arrow type for our player
 					int arrowTypeIndex = plugin.activeArrowType.get(player.getName()).ordinal();
 
 					// If player can use all arrow types, select next type
 					if (player.hasPermission("multiarrow.use.all")) {
 						arrowTypeIndex = this.nextArrowIndex(arrowTypeIndex, player.isSneaking());
 					} else {
 						//Start with the next arrow type
 						int initialIndex = arrowTypeIndex;
 						arrowTypeIndex = this.nextArrowIndex(arrowTypeIndex, player.isSneaking());
 
 						//Search for a valid type until looped around
 						while (arrowTypeIndex != initialIndex) {
 							String permissionNode = "multiarrow.use." + ArrowType.values()[arrowTypeIndex].toString().toLowerCase();
 							if (player.hasPermission(permissionNode)) {
 								break;
 							}
 
 							if (player.isSneaking()) {
 								if (arrowTypeIndex == 0) {
 									arrowTypeIndex = ArrowType.values().length - 1;
 								} else {
 									arrowTypeIndex--;
 								}
 							} else {
 								if (arrowTypeIndex == ArrowType.values().length - 1) {
 									arrowTypeIndex = 0;
 									break;
 								} else {
 									arrowTypeIndex++;
 								}
 							}
 						}
 					}
 
 					plugin.activeArrowType.put(player.getName(), ArrowType.values()[arrowTypeIndex]);
 				} else {
 					plugin.activeArrowType.put(player.getName(), ArrowType.NORMAL);
 				}
 
 				ArrowType arrowType = plugin.activeArrowType.get(player.getName());
 				Double arrowFee = plugin.config.getArrowFee(arrowType);
 				String message = "Selected " + plugin.toProperCase(arrowType.toString());
 				/*if (plugin.iconomy != null && arrowFee > 0D) {
 					message += " (" + iConomy.format(arrowFee) + ")";
 				}*/
 
 				player.sendMessage(message);
 			}
 		}
 	}
 
 	private int nextArrowIndex(int startIndex, boolean isSneaking) {
 		int currentIndex = startIndex;
 		if (isSneaking) {
 			if (currentIndex == 0) {
 				currentIndex = ArrowType.values().length - 1;
 			} else {
 				currentIndex--;
 			}
 		} else {
 			if (currentIndex == ArrowType.values().length - 1) {
 				currentIndex = 0;
 			} else {
 				currentIndex++;
 			}
 		}
 		return currentIndex;
 	}
 }
