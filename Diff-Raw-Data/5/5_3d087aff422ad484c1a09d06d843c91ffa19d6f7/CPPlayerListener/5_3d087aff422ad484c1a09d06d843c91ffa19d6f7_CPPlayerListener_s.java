 /************************************************************************
  * This file is part of CreativePlus.
  *
  * CreativePlus is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CreativePlus.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.CP.Listeners;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 import de.Lathanael.CP.CreativePlus.CPConfigEnum;
 import de.Lathanael.CP.CreativePlus.CreativePlus;
 import be.Balor.Manager.Permissions.PermissionManager;
 import be.Balor.Tools.Utils;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class CPPlayerListener implements Listener {
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerPickUpItem(PlayerPickupItemEvent event) {
 		if (CPConfigEnum.PICK_ITEM.getBoolean())
 			return;
 		if (event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		if (!CreativePlus.worlds.contains(player.getWorld().getName()))
 			return;
 		if (player.getGameMode() != GameMode.CREATIVE)
 			return;
 		if (PermissionManager.hasPerm(player, "creativeplus.pickitems", false))
 			return;
 		event.setCancelled(true);
 		Utils.sI18n(player, "NoItemPickUp");
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if (CPConfigEnum.DROPITEMS.getBoolean())
 			return;
 		if (event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		if (!CreativePlus.worlds.contains(player.getWorld().getName()))
 			return;
 		if (player.getGameMode() != GameMode.CREATIVE)
 			return;
 		if (PermissionManager.hasPerm(player, "creativeplus.dropitems", false))
 			return;
 		event.setCancelled(true);
 		Utils.sI18n(player, "NoItemsDrop");
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		if (!CreativePlus.worlds.contains(player.getWorld().getName()))
 			return;
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Material mat = event.getClickedBlock().getType();
 			if ((mat.equals(Material.CHEST) || mat.equals(Material.DISPENSER) || mat.equals(Material.BREWING_STAND)
 					|| mat.equals(Material.WORKBENCH) || mat.equals(Material.BURNING_FURNACE)
 					|| mat.equals(Material.FURNACE) || mat.equals(Material.ENCHANTMENT_TABLE))
 					&& !PermissionManager.hasPerm(player, "creativeplus.storage.allowed")) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		if (event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		if (!CreativePlus.worlds.contains(player.getWorld().getName()))
 			return;
 		Entity clicked = event.getRightClicked();
 		if ((clicked instanceof StorageMinecart || clicked instanceof PoweredMinecart)
 				&& !PermissionManager.hasPerm(player, "creativeplus.storage.allowed"))
 			event.setCancelled(true);
 	}
 }
