 package com.scottwoodward.rpshops.listeners;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Villager;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.material.Dye;
 
 /**
  * Copyright 2013 - 2013 Scott Woodward
  *
  * This file is part of RPShops
  *
  * RPShops is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * RPShops is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with RPShops.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 /**
  * VillagerInteractListener.java
  * Purpose: 
  *
  * @author Scott Woodward
  */
 public class VillagerInteractListener implements Listener {
 
     @EventHandler
     public void onInteract(PlayerInteractEntityEvent event){
         if(event.getRightClicked() instanceof Villager){
             event.setCancelled(true);
             Inventory inv = Bukkit.getServer().createInventory(event.getPlayer(), 9, "Shop");
             
             
             ItemStack buy = new ItemStack(Material.EMERALD);
             List<String> lore = new ArrayList<String>();
             lore.add("Click to sell an emerald for 20 gold coins");
             ItemMeta meta = buy.getItemMeta();
             meta.setDisplayName("Sell Emerald");
             meta.setLore(lore);
             buy.setItemMeta(meta);
             
             /*Dye lapis = new Dye();
             ItemStack sell = new ItemStack(Material.LAPIS_BLOCK);
             lore = new ArrayList<String>();
             lore.add("Click to sell items to the store");
             meta = sell.getItemMeta();
             meta.setDisplayName("Sell Items");
             meta.setLore(lore);
             sell.setItemMeta(meta);
             
             ItemStack config = new ItemStack(Material.BOOK_AND_QUILL);
             lore = new ArrayList<String>();
             lore.add("Click to configure the store");
             meta = config.getItemMeta();
             meta.setDisplayName("Configure Store");
             meta.setLore(lore);
             config.setItemMeta(meta);*/
             
             inv.setItem(0, buy);
             //inv.setItem(1, sell);
             //inv.setItem(2, config);
             
             event.getPlayer().openInventory(inv);
         }
     }
 }
