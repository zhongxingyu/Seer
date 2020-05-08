 /*The MIT License
 
 Copyright (c) 2012 Mark Lohstroh
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 
 package net.evtr.hungergames;
 
 import java.util.Vector;
 
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class BlockListener implements Listener
 {
 	HungerGames plugin;
 	private Vector<Material> allowedMaterials;
 	
 	public BlockListener(HungerGames instance)
 	{
 		plugin = instance;
 		allowedMaterials = new Vector<Material>();
 		allowedMaterials.add(Material.LEAVES);
		allowedMaterials.add(Material.LONG_GRASS);
		allowedMaterials.add(Material.YELLOW_FLOWER);
		allowedMaterials.add(Material.RED_ROSE);
 	}
 	
 	@EventHandler
 	public void LogBurnDestruction(BlockBurnEvent event)
 	{
 		plugin.currentGame.logBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getType(), event.getBlock().getData());
 	}
 	
 	@EventHandler
 	public void LogPhysicsDestruction(BlockPhysicsEvent event)
 	{
 		plugin.currentGame.logBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getType(), event.getBlock().getData());
 	}
 	
 	@EventHandler
 	public void LogDecayDestruction(LeavesDecayEvent event) {
 		plugin.currentGame.logBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getType(), event.getBlock().getData());
 	}
 	
 	@EventHandler
 	public void StopBlockBreaks(BlockBreakEvent event)
 	{
 		Player player = event.getPlayer();
 		//all we care about is his OP status
 		if(plugin.currentGame != null)
 		{
 			if (!player.isOp())
 			{
 				if(!isMaterialInList(event.getBlock().getType()))
 				{
 					event.setCancelled(true);
 				}
 			}
 		}
 		if ( !event.isCancelled() )
 		{
 			plugin.currentGame.logBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getType(), event.getBlock().getData());
 		}
 	}
 	
 	@EventHandler
 	public void playerOpensChest(PlayerInteractEvent event) 
 	{
 		if ( plugin.currentGame == null ) return;
 		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST)
 		{
 			Chest chest = (Chest)event.getClickedBlock().getState();
 			
 			HungerPlayer player = plugin.currentGame.getPlayer(event.getPlayer());
 			if ( player != null )
 			{
 				player.getPlayer().openInventory(plugin.currentGame.inventory.getInventory(player, chest));
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	private boolean isMaterialInList(Material material)
 	{
 		for(Material tempMaterial : allowedMaterials)
 		{
 			if(tempMaterial == material)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 }
