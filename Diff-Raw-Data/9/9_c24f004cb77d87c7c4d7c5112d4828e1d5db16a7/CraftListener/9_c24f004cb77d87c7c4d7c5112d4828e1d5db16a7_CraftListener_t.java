 package org.fatecrafters.plugins.listeners;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.inventory.ItemStack;
 import org.fatecrafters.plugins.RealisticBackpacks;
 
 public class CraftListener implements Listener {
 
 	private final RealisticBackpacks plugin;
 
 	public CraftListener(final RealisticBackpacks plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPrepareCraft(final PrepareItemCraftEvent e) {
 		final ItemStack result = e.getInventory().getResult();
 		final HumanEntity human = e.getView().getPlayer();
		if (!(human instanceof Player) || result == null) {
 			return;
 		}
 		for (final String backpack : plugin.backpacks) {
 			if (plugin.backpackOverrides.get(backpack) != null && result.isSimilar(plugin.backpackOverrides.get(backpack))) {
 				if (RealisticBackpacks.globalGlow && plugin.backpackData.get(backpack).get(17) != null && plugin.backpackData.get(backpack).get(17).equalsIgnoreCase("true")) {
 					e.getInventory().setResult(RealisticBackpacks.NMS.addGlow(plugin.backpackItems.get(backpack)));
 				} else {
 					e.getInventory().setResult(plugin.backpackItems.get(backpack));
 				}
 				break;
 			}
 		}
 		if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
 			for (final String backpack : plugin.backpacks) {
 				final List<String> key = plugin.backpackData.get(backpack);
 				if (!result.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', key.get(3)))) {
 					continue;
 				}
 				if (!human.hasPermission("rb." + backpack + ".craft") && plugin.isUsingPerms()) {
 					e.getInventory().setResult(null);
 					((Player) human).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("craftPermError")));
 					break;
 				}
 				if (RealisticBackpacks.globalGlow && plugin.backpackData.get(backpack).get(17) != null && plugin.backpackData.get(backpack).get(17).equalsIgnoreCase("true")) {
 					e.getInventory().setResult(RealisticBackpacks.NMS.addGlow(plugin.backpackItems.get(backpack)));
 				}
 			}
 		}
 	}
 
 }
