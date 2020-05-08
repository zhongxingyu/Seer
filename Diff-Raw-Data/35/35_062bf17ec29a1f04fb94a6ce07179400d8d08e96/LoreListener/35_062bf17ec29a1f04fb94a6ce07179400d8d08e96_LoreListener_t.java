 /*
  * This file is part of Lore.
  *
  * Copyright (c) 2013, AlmuraDev <http://www.almuradev.com/>
  * Lore is licensed under the Almura Development License.
  *
  * Lore is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Lore is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuradev.lore.listener;
 
 import java.util.List;
import java.util.logging.Level;
 
 import com.almuradev.lore.LorePlugin;
 import com.almuradev.lore.util.VaultUtil;
 
import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 
 public class LoreListener implements Listener {
 	private final LorePlugin plugin;
 
 	public LoreListener(LorePlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();

		// Get Book contents
 		String joinMessage = plugin.getConfiguration().getJoinMessage();
 		String bookAuthor = plugin.getConfiguration().getBookAuthor();
 		String bookTitle = plugin.getConfiguration().getBookTitle();
 		List<String> bookContent = plugin.getConfiguration().getBookContent();

		// Do some magic
 		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
 		BookMeta meta = (BookMeta) book.getItemMeta();
 
 		// If the player hasn't played before, continue.
		if (!player.hasPlayedBefore() && VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.obtain")) {
			if (bookContent != null && !bookContent.isEmpty()) {
 				meta.setAuthor(bookAuthor);
 				meta.setTitle(bookTitle);
 				meta.setPages(bookContent);
 				book.setItemMeta(meta);
 				player.getInventory().addItem(book);
				Bukkit.getLogger().log(Level.INFO, player.getName() + " has received a Lore book.");
				if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.message") && joinMessage != null && !joinMessage.isEmpty()) {
					player.sendMessage(joinMessage);
				}
			} else {
				Bukkit.getLogger().log(Level.WARNING, "Lore was unable to create a book for " + player.getName() + ".");
 			}
 		}
 	}
 }
