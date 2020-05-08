 package com.ayan4m1.multiarrow;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.ayan4m1.multiarrow.arrows.*;
 
 /**
  * Handle events for all Player related events
  * @author ayan4m1
  */
 public class MultiArrowPlayerListener extends PlayerListener {
 	private final MultiArrow plugin;
 
 	public MultiArrowPlayerListener(MultiArrow instance) {
 		plugin = instance;
 	}
 
 	private String toProperCase(String input) {
 		return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
 	}
 
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (plugin.activeArrowType.containsKey(event.getPlayer().getName())) {
 			plugin.activeArrowType.remove(event.getPlayer().getName());
 		}
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		if (player.getItemInHand().getType() == Material.BOW) {
 			if (event.getAction() == Action.RIGHT_CLICK_AIR	|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				PlayerInventory inventory = player.getInventory();
 				if (inventory.contains(Material.ARROW)) {
 					if (!plugin.activeArrowType.containsKey(player.getName())) {
 						plugin.activeArrowType.put(player.getName(), ArrowType.NORMAL);
 					}
 
					event.setCancelled(true);

 					ArrowType arrowType = plugin.activeArrowType.get(player.getName());
 
 					if (!player.hasPermission("multiarrow.free") && plugin.config.getRequiredTypeId(arrowType) > 0) {
 						try {
 							if (inventory.contains(Material.getMaterial(plugin.config.getRequiredTypeId(arrowType)))) {
 								ItemStack reqdStack = player.getInventory().getItem(player.getInventory().first(Material.getMaterial(plugin.config.getRequiredTypeId(arrowType))));
 								if (reqdStack.getAmount() > 1) {
 									reqdStack.setAmount(reqdStack.getAmount() - 1);
 								} else {
 									inventory.clear(inventory.first(Material.getMaterial(plugin.config.getRequiredTypeId(arrowType))));
 								}
 								//HACK: This method is deprecated, but its use here fixes an issue
 								player.updateInventory();
 							} else {
 								player.sendMessage("You do not have any " + this.toProperCase(Material.getMaterial(plugin.config.getRequiredTypeId(arrowType)).toString().replace('_', ' ')));
 								return;
 							}
 						} catch (Exception e) {
 							plugin.log.warning("MultiArrow could not check for item with id " + ((Integer)plugin.config.getRequiredTypeId(arrowType)).toString());
 						}
 					}
 
 					ItemStack arrowStack = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
 					if (arrowStack.getAmount() > 1) {
 						arrowStack.setAmount(arrowStack.getAmount() - 1);
 					} else {
 						inventory.clear(inventory.first(Material.ARROW));
 					}
 
 					Arrow arrow = player.shootArrow();
 
 					if (arrowType != ArrowType.NORMAL) {
 						CustomArrowEffect arrowEffect = null;
 
 						String className = this.toProperCase(arrowType.toString()) + "ArrowEffect";
 						try {
 							arrowEffect = (CustomArrowEffect) Class.forName("com.ayan4m1.multiarrow.arrows." + className).newInstance();
 						} catch (ClassNotFoundException e) {
 							plugin.log.warning("Failed to find class " + className);
 						} catch (InstantiationException e) {
 							plugin.log.warning("Could not instantiate class " + className);
 						} catch (IllegalAccessException e) {
 							plugin.log.warning("Could not access class " + className);
 						}
 
 						if (arrowEffect != null) {
 							plugin.activeArrowEffect.put(arrow, arrowEffect);
 						}
 					}
 				}
 			} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				if (plugin.activeArrowType.containsKey(player.getName())) {
 					// Get the currently selected arrow type for our player
 					int arrowTypeIndex = plugin.activeArrowType.get(player.getName()).ordinal();
 
 					// If player can use all arrow types, select next type
 					if (player.hasPermission("multiarrow.use.all")) {
 						if (arrowTypeIndex == ArrowType.values().length - 1) {
 							arrowTypeIndex = 0;
 						} else {
 							arrowTypeIndex++;
 						}
 					} else {
 						// Search for a valid type until looped around
 						int initialIndex = arrowTypeIndex++;
 						while (arrowTypeIndex != initialIndex) {
 							String permissionNode = "multiarrow.use." + ArrowType.values()[arrowTypeIndex].toString().toLowerCase();
 							if (player.hasPermission(permissionNode)) {
 								break;
 							}
 
 							if (arrowTypeIndex == ArrowType.values().length - 1) {
 								arrowTypeIndex = 0;
 								break;
 							} else {
 								arrowTypeIndex++;
 							}
 						}
 					}
 
 					plugin.activeArrowType.put(player.getName(), ArrowType.values()[arrowTypeIndex]);
 				} else {
 					plugin.activeArrowType.put(player.getName(), ArrowType.NORMAL);
 				}
 
 				player.sendMessage("Selected " + this.toProperCase(plugin.activeArrowType.get(player.getName()).toString()) + "!");
 			}
 		}
 	}
 }
