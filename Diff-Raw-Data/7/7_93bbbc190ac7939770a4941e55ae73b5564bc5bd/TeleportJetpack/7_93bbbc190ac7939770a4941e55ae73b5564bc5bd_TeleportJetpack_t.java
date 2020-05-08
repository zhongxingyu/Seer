 package net.jselby.ej.impl;
 
 import net.jselby.ej.Utils;
 import net.jselby.ej.api.FlightTypes;
 import net.jselby.ej.api.Jetpack;
 import net.jselby.ej.api.JetpackEvent;
 
 import org.bukkit.ChatColor;
import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * A jetpack for teleporting around, using the power of ender pearls
  * 
  * @author James
  * 
  */
 public class TeleportJetpack extends Jetpack {
 
 	@Override
 	public String getName() {
 		return ChatColor.RESET + "" + ChatColor.GOLD + "Teleportation Jetpack";
 	}
 
 	@Override
 	public String[] getDescription() {
 		return new String[] { ChatColor.RESET + "Like a ender pearl, but",
 				ChatColor.RESET + "more expensive and time ",
 				ChatColor.RESET + "consuming to obtain." };
 	}
 
 	@Override
 	public Material getMaterial() {
 		return Material.getMaterial(getConfig().getString(
 				"jetpacks.teleport.material", "CHAINMAIL_CHESTPLATE"));
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onFlyEvent(JetpackEvent event) {
 		Block block = event.getPlayer().getTargetBlock(null, 30);
 		if (block != null) {
			Location loc = block.getLocation().add(0, 2, 0);
			loc.setYaw(event.getPlayer().getLocation().getYaw());
			loc.setPitch(event.getPlayer().getLocation().getPitch());
			event.getPlayer().teleport(loc);
 		}
 	}
 
 	@Override
 	public void onFuelUsageEvent(JetpackEvent event) {
 		if (getConfig().getBoolean("fuel.enabled", true))
 			event.getPlayer()
 					.getInventory()
 					.removeItem(
 							new ItemStack(Material.getMaterial(getConfig()
 									.getString("fuel.material", "COAL")), 1));
 		if (getConfig().getBoolean("jetpacks.teleport.durability", true))
 			Utils.damage(event.getPlayer(), getSlot(), 150);
 	}
 
 	@Override
 	public boolean onFuelCheckEvent(JetpackEvent event) {
 		if (getConfig().getBoolean("fuel.enabled", true)) {
 			boolean containsCoal = event
 					.getPlayer()
 					.getInventory()
 					.contains(
 							Material.getMaterial(getConfig().getString(
 									"fuel.material", "COAL")));
 			if (!containsCoal)
 				event.getPlayer().sendMessage(
 						ChatColor.RED + "You don't have enough fuel!");
 			return containsCoal;
 		}
 		return true;
 	}
 
 	@Override
 	public FlightTypes getMovementType() {
 		return FlightTypes.CROUCH;
 	}
 
 	@Override
 	public CraftingRecipe getCraftingRecipe() {
 		if (!getConfig().getBoolean("jetpacks.teleport.craftable", true)) {
 			return null;
 		}
 
 		CraftingRecipe recipe = new CraftingRecipe(getItem());
 		recipe.setSlot(0, Material.ENDER_PEARL);
 		recipe.setSlot(3, Material.ENDER_PEARL);
 		recipe.setSlot(6, Material.ENDER_PEARL);
 
 		recipe.setSlot(1, Material.DIAMOND);
 		recipe.setSlot(4, Material.GOLD_CHESTPLATE);
 		recipe.setSlot(7, Material.FURNACE);
 
 		recipe.setSlot(2, Material.ENDER_PEARL);
 		recipe.setSlot(5, Material.ENDER_PEARL);
 		recipe.setSlot(8, Material.ENDER_PEARL);
 		return recipe;
 	}
 
 	@Override
 	public String getGiveName() {
 		return "teleportation";
 	}
 
 	@Override
 	public Slot getSlot() {
 		return Slot.CHESTPLATE;
 	}
 }
