 /**
  *  Name:    Main.java
  *  Created: 17:24:17 - 3 jun 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  * 
  */
 
 package me.lucasemanuel.raygun;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 
 	private ShapedRecipe raygun;
 
 	public void onEnable() {
 		setupRecipe();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	@SuppressWarnings("serial")
 	private void setupRecipe() {
 		ItemStack raygun = new ItemStack(Material.CARROT_ITEM);
 		ItemMeta meta = raygun.getItemMeta();
 		meta.setDisplayName("RayGun");
 		meta.setLore(new ArrayList<String>() {{
			add("WARNING! Extremely dangerous!");
 		}});
 		raygun.setItemMeta(meta);
 
 		ShapedRecipe recipe = new ShapedRecipe(new ItemStack(raygun));
 		recipe.shape(new String[] { "BDB", "BCB", "BBB" });
 		recipe.setIngredient('B', Material.DIAMOND_BLOCK);
 		recipe.setIngredient('C', Material.CARROT_ITEM);
 		recipe.setIngredient('D', Material.DIAMOND);
 		getServer().addRecipe(recipe);
 		
 		this.raygun = recipe;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Player player = event.getPlayer();
 			ItemStack is = player.getItemInHand();
			if (is.hasItemMeta() 
					&& is.getItemMeta().hasDisplayName()
					&& is.getItemMeta().hasLore() 
					&& is.getItemMeta().getDisplayName().equals("RayGun") 
					&& is.getItemMeta().getLore().contains("WARNING! Extremely dangerous!")) {
 
 				Location pl = player.getEyeLocation();
 
 				double px = pl.getX();
 				double py = pl.getY();
 				double pz = pl.getZ();
 
 				double yaw = Math.toRadians(pl.getYaw() + 90);
 				double pitch = Math.toRadians(pl.getPitch() + 90);
 
 				double x = Math.sin(pitch) * Math.cos(yaw);
 				double y = Math.sin(pitch) * Math.sin(yaw);
 				double z = Math.cos(pitch);
 
 				for (int i = 1; i <= 70; i++) {
 					Location loc = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
 					if (loc.getBlock().getType() == Material.AIR)
 						player.getWorld().createExplosion(loc, 1f);
 					else
 						break;
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void preCraft(PrepareItemCraftEvent event) {
 		
 		boolean equal = RecipeUtil.areEqual(event.getRecipe(), raygun);
 
 		if (equal) {
 			HumanEntity human = event.getView().getPlayer();
 
 			if (!human.hasPermission("raygun.craft")) {
 				
 				event.getInventory().setResult(null);
 
 				if (human instanceof Player) {
 					Player player = (Player) human;
 					player.sendMessage(ChatColor.RED + "Need permission to craft this!");
 				}
 			}
 		}
 	}
 }
