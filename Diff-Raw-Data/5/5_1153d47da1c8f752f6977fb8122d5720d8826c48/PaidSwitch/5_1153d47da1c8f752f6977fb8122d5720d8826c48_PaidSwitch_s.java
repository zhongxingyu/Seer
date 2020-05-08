 /*
 Copyright (c) 2012 Wolf480pl (wolf480@interia.pl)
 
 This software is provided 'as-is', without any express or implied
 warranty. In no event will the authors be held liable for any damages
 arising from the use of this software.
 
 Permission is granted to anyone to use this software for any purpose,
 including commercial applications, and to alter it and redistribute it
 freely, subject to the following restrictions:
 
 1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
 
 2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
 
 3. This notice may not be removed or altered from any source
 distribution.
  */
 
 package com.github.wolf480pl.PaidSwitch;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 //import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityInteractEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PaidSwitch extends JavaPlugin implements Listener {
 	private Logger log;
 	private Economy eco;
 	public void onEnable(){
 		log = getLogger();
 		getServer().getPluginManager().registerEvents(this, this);
 		if(SetupEco())
 			log.info("Vault economy found.");
 		else
 			log.info("Vault economy not found yet.");
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 	}
 	public void onDisable(){
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("paidswitch")){
 			if(args.length > 0){
 				reloadConfig();
 				sender.sendMessage("[PaidSwitch] Config reloaded.");
 			} else
 				sender.sendMessage(cmd.getUsage().split("\n"));
 			return true;
 		}
 		return false;
 	}
 	@EventHandler
 	public void onEntityInteract(EntityInteractEvent event){
 		if(event.isCancelled()) return;
 //		String msg = event.getEntity().getType().name() + " uzyl  " + event.getBlock().getType().name() + " !";
 //		getServer().broadcastMessage(msg);
 		if(isSwitch(event.getBlock())){
 			Payment paid = findSign(event.getBlock());
 			if((paid != null) && paid.isValid())event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getClickedBlock() == null) return;
 		if(event.isCancelled()) return;
 //		String msg = event.getPlayer().getName() + " uzyl  " + event.getAction().name() + " na " + event.getClickedBlock().getType().name() + " !";
 //		getServer().broadcastMessage(msg);
 		if(isSwitch(event.getClickedBlock())){
 			Payment paid = findSign(event.getClickedBlock());
 			if((paid != null) && paid.isValid()){
 				if(!event.getPlayer().hasPermission("paidswitch.use")){
 					event.getPlayer().sendMessage(getConfig().getString("messages.use-noperm"));
 					event.setCancelled(true);
 					return;
 				}
 //				getServer().broadcastMessage(paid.Amount + " for " + paid.Account);
 				if(event.getPlayer().hasPermission("paidswitch.use.free")){
 					event.getPlayer().sendMessage(getConfig().getString("messages.use-free"));
 					if(getConfig().getBoolean("earn-for-free")) eco.depositPlayer(paid.Account, paid.Amount);
 					return;
 				}
 				if((eco == null) && !SetupEco()){
 					log.log(Level.SEVERE,"No economy plugin found!");
 					if(!event.getPlayer().getName().equalsIgnoreCase(paid.Account))
 						event.setCancelled(true);
 				} else {
 					if(eco.has(event.getPlayer().getName(), paid.Amount)){
 						EconomyResponse response = eco.withdrawPlayer(event.getPlayer().getName(),paid.Amount);
 						eco.depositPlayer(paid.Account, paid.Amount);
						event.getPlayer().sendMessage(String.format(getConfig().getString("messages.use-paid"),eco.format(paid.Amount),eco.format(response.balance)));
 					} else {
 						event.getPlayer().sendMessage(String.format(getConfig().getString("messages.use-need"),eco.format(paid.Amount)));
 						event.setCancelled(true);
 					}
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onSignChange(SignChangeEvent event){
 		if (event.isCancelled()) return;
 		if(!event.getLine(0).equalsIgnoreCase("[PaidSw]")){
 //			getServer().broadcastMessage(String.format("%s doesn't match [PaidSw]",event.getLine(0)));
 			return;
 		}
 //		getServer().broadcastMessage("[PaidSw]!");
 		if(!event.getPlayer().hasPermission("paidswitch.create")){
 			event.getPlayer().sendMessage(getConfig().getString("messages.create-noperm"));
 			event.setCancelled(true);
 			event.getBlock().breakNaturally();
 			return;
 		}
 		if(!findSwitch(event.getBlock())){
 			event.getPlayer().sendMessage(getConfig().getString("messages.create-noswitch"));
 			event.setCancelled(true);
 			event.getBlock().breakNaturally();
 			return;
 		}
 		if(!event.getLine(1).isEmpty()){
 			if(!event.getPlayer().hasPermission("paidswitch.create.others")){
 				event.getPlayer().sendMessage(getConfig().getString("messages.create-others"));
 				event.setCancelled(true);
 				event.getBlock().breakNaturally();
 				return;					
 			}
 			if(!eco.hasAccount(event.getLine(1))){
 				event.getPlayer().sendMessage(String.format(getConfig().getString("messages.create-noaccount"),(event.getLine(1))));
 				event.setCancelled(true);
 				event.getBlock().breakNaturally();
 				return;										
 			}
 		} else {
 			event.setLine(1, event.getPlayer().getName());
 //			getServer().broadcastMessage("Setting player name!");
 		}
 		try{
 			Double.parseDouble(event.getLine(2));
 		} catch (NumberFormatException ex) {
 			event.getPlayer().sendMessage(String.format(getConfig().getString("messages.create-noprice"),(event.getLine(2))));
 			event.setCancelled(true);
 			event.getBlock().breakNaturally();
 			return;
 		}
 		event.getPlayer().sendMessage(getConfig().getString("messages.create-ok"));
 	}
 	private Payment findSign(Block block){
 		Payment paid = checkSign(block, BlockFace.UP);
 		if(paid == null) paid = checkSign(block, BlockFace.NORTH);
 		if(paid == null) paid = checkSign(block, BlockFace.EAST);
 		if(paid == null) paid = checkSign(block, BlockFace.SOUTH);
 		if(paid == null) paid = checkSign(block, BlockFace.WEST);
 		if(paid == null) paid = checkSign(block, BlockFace.DOWN);
 		return paid;
 	}
 	
 	private Payment checkSign(Block block, BlockFace face){
 		BlockState bl = block.getRelative(face).getState();
 		if(bl instanceof Sign){
			getServer().broadcastMessage(((Sign) bl).getLine(0));
 			if(((Sign) bl).getLine(0).equalsIgnoreCase("[PaidSw]"))
 				return new Payment(((Sign) bl).getLine(1),((Sign) bl).getLine(2));
 		}
 		return null;
 	}
 	private boolean SetupEco(){
 		RegisteredServiceProvider<Economy> ecoProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if( ecoProvider != null )
 			eco = ecoProvider.getProvider();
 		return (eco != null);
 	}
 	private boolean isSwitch(Block block){
 		return block.getType().equals(Material.WOOD_PLATE) ||
 				block.getType().equals(Material.STONE_PLATE) ||
 				block.getType().equals(Material.STONE_BUTTON) ||
 				block.getType().equals(Material.LEVER);
 	}
 	private boolean findSwitch(Block block){
 		return isSwitch(block.getRelative(BlockFace.DOWN)) || 
 				isSwitch(block.getRelative(BlockFace.SOUTH)) || 
 				isSwitch(block.getRelative(BlockFace.WEST)) || 
 				isSwitch(block.getRelative(BlockFace.NORTH)) || 
 				isSwitch(block.getRelative(BlockFace.EAST)) || 
 				isSwitch(block.getRelative(BlockFace.UP));
 	}
 }
