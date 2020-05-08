 /*
  * MCTrade
  * Copyright (C) 2012 Fogest <http://fogest.net16.net> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.fogest.mctrade.commands;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import me.fogest.mctrade.DatabaseManager;
 import me.fogest.mctrade.MCTrade;
 import me.fogest.mctrade.MessageHandler;
 import me.fogest.mctrade.Msg;
 import me.fogest.mctrade.UrlShortener;
 import me.fogest.mctrade.Verify;
 import me.fogest.mctrade.AcceptTrade;
 
 public class PlayerCommands implements CommandExecutor {
 	private int itemId;
 	private int itemAmount;
 	private Material itemMaterial;
 	private MessageHandler m;
 
 	private double tax = MCTrade.tax;
 	private double taxAmount;
 	private String webURL = MCTrade.webAddress;
 	private String longLink = webURL + "registration.php";
 
 	private boolean trade = true;
 	private boolean tradeGo = true;
 
 	private int userId, ver, id, tradeStatus;
 
 	// Command Handlers
 
 	public PlayerCommands(final MCTrade plugin, MessageHandler m) {
 		this.m = m;
 	}
 
 	public boolean onCommand(final CommandSender sender, final Command command, String cmdLabel, String[] args) {
 		Player player = (Player) sender;
 		// Gives usage message if player simply inputs /mct
 		if (args.length <= 0 && checkPerms(player, "mctrade")) {
 			m.tellPlayer(player, Msg.COMMAND_USAGE);
 		}
 		// Checking if the user actually put something after /mct
 		else if (args.length >= 1) {
 			// Attempt trade creation command if second var is number
 			if (args[0].matches("[0-9]+") && checkPerms(player,"trade")) {
 				prepareTrade(player);
 				int active = DatabaseManager.getActiveLevel(player.getName());
				if (userId > 1 && active == 2) {
 					CreateTrade(player, args);
 				}
 				else if(active != 2){
 					m.tellPlayer(player, Msg.NOT_ACTIVE);
 				}
 				else if (userId < 1) {
 					m.tellPlayer(player, Msg.ACCOUNT_REQUIRED);
 					m.tellPlayer(player, UrlShortener.shortenURL(longLink));
 				} else {
 					m.tellPlayer(player, Msg.USERID_GET_ERROR);
 				}
 			}
 			// Verifying Online Account
 			else if (args[0].equalsIgnoreCase("verify") && checkPerms(player, "verify")) {
 				userId = getUserId(player);
 				int active = DatabaseManager.getActiveLevel(player.getName());
 				if(active < 2 && userId > 0) {
 					ver = Verify.createUserVerification(player.getName());
 					m.tellPlayer(player, "Your verification code is: " + ver);
 					m.tellPlayer(player, "Enter this code on the site");
 					m.tellPlayer(player,"" + webURL);
 				}
 				else if(active == 2) {
 					m.tellPlayer(player, Msg.ALREADY_VERIFIED);
 				}
 				else if(userId < 1){
 					m.tellPlayer(player, Msg.ACCOUNT_REQUIRED);
 					m.tellPlayer(player, UrlShortener.shortenURL(longLink));
 				}
 				else {
 					m.tellPlayer(player, Msg.GENERAL_ERROR);
 				}
 			} 
 			// Accepting Trade
 			else if (args[0].equalsIgnoreCase("accept") && checkPerms(player, "accept")) {
 				userId = getUserId(player);
 				int active = DatabaseManager.getActiveLevel(player.getName());
 				if(active == 2 && userId > 0) {
 					AcceptTrade(player, args);
 				}
 				else if(active != 2){
 					m.tellPlayer(player, Msg.NOT_ACTIVE);
 				}
 				else if(userId < 1){
 					m.tellPlayer(player, Msg.ACCOUNT_REQUIRED);
 					m.tellPlayer(player, UrlShortener.shortenURL(longLink));
 				}
 				else {
 					m.tellPlayer(player, Msg.GENERAL_ERROR);
 				}
 				
 				
 			}
 		}
 		return false;
 	}
 
 	private void AcceptTrade(Player player, String[] args) {
 		if (args.length == 2 && args[1].matches("[0-9]+")) {
 			id = Integer.parseInt(args[1]);
 			String mcTrader = DatabaseManager.getTradeUsername(id);
 			if (!(mcTrader.equals(player.getName()))) {
 
 				if (MCTrade.checkIP == true) {
 					if (!(player.getAddress().getAddress().getHostAddress().equals(DatabaseManager.getTraderIP(id)))) {
 						tradeGo = false;
 					} else {
 						tradeGo = true;
 					}
 				}
 
 				if (tradeGo == true) {
 					tradeStatus = DatabaseManager.getTradeStatus(id);
 					if (tradeStatus == 1) {
 						double cost = DatabaseManager.getItemCost(id);
 						if (MCTrade.econ.getBalance(player.getName()) >= cost) {
 							AcceptTrade accept = new AcceptTrade(Integer.parseInt(args[1]), player);
 							m.tellPlayer(player, "You have sucessfully purchased " + accept.getAmount() + " " + accept.getTradeItem() + "'s");
 							MCTrade.econ.withdrawPlayer(player.getName(), (cost));
 							MCTrade.econ.depositPlayer(DatabaseManager.getTradeUsername(id), (cost));
 						} else {
 							m.tellPlayer(player, "Sorry, that trade costs: " + cost + " and you only have: " + MCTrade.econ.getBalance(player.getName()));
 						}
 					} else if (tradeStatus == 2) {
 						m.tellPlayer(player, Msg.TRADE_ALREADY_ACCEPTED);
 					} else if (tradeStatus == 3) {
 						m.tellPlayer(player, Msg.TRADE_ALREADY_HIDDEN);
 					}
 				} else {
 					m.tellPlayer(player, Msg.TRADE_CANNOT_ACCEPT_OWN);
 				}
 			} else {
 				m.tellPlayer(player, Msg.TRADE_CANNOT_ACCEPT_OWN);
 			}
 		} else {
 			m.tellPlayer(player, Msg.TRADE_ACCEPT_USAGE);
 		}
 	}
 
 	private void CreateTrade(Player player, String[] args) {
 		if (!(getItemMaterial().toString().equals("AIR"))) {
 			if (args.length == 2 && args[1].matches("[0-9]+")) {
 				int tempItemAmount = Integer.parseInt(args[1]);
 				if (checkItemMax(player) >= tempItemAmount) {
 					setItemAmount(tempItemAmount);
 					trade = true;
 				} else {
 					trade = false;
 				}
 			}
 			int price = Integer.parseInt(args[0]);
 			taxAmount = (price * tax);
 			double balance = (MCTrade.econ.getBalance(player.getName()));
 			if (trade == true && balance >= taxAmount) {
 				removeItem(player, getItemMaterial(), getItemAmount());
 				MCTrade.econ.withdrawPlayer(player.getName(), taxAmount);
 				int tId = DatabaseManager.createTrade(player.getName(), getItemId(), getItemMaterial().toString(), getItemAmount(), args[0], player.getAddress().getAddress().getHostAddress());
 				m.tellAll(player.getName() + " has created a new trade (" + tId + ")");

 				m.tellAll("Item: " + ChatColor.GRAY + getItemMaterial() + ChatColor.WHITE + " Amount: " + ChatColor.GRAY + getItemAmount() + ChatColor.WHITE + " Price: " + ChatColor.GRAY + price);
				m.tellAll("Trade Info: " + UrlShortener.shortenURL(webURL + "trades.html?id=" + tId));
 				m.tellPlayer(player, "You have been charged " + taxAmount + " for the creation of this trade!");
 
 				m.info("Player " + player.getName() + " has created a trade with the following info: Price:" + args[0] + " Item Amount: " + getItemAmount() + " Item: " + getItemMaterial()
 						+ " Item ID: " + getItemId());
 			} else if (trade == false) {
 				m.tellPlayer(player, Msg.TRADE_NOT_ENOUGH_ITEMS);
 			} else if (balance < taxAmount) {
 				m.tellPlayer(player,
 						"To prevent abuse, tax is charged on your item, on purchase rather then when your trade is accepted. Tax is based on the price you set the trade at and the tax for this one is: "
 								+ taxAmount + "And you only have " + balance);
 			}
 		} else {
 			m.tellPlayer(player, Msg.TRADE_AIR);
 		}
 	}
 
 	// Checks global mctrade permissions
 	private boolean checkPerms(Player player) {
 		if (MCTrade.perms.has(player, "mctrade.*")) {
 			return true;
 		}
 		m.tellPlayer(player, Msg.PERMISSION_DENIED);
 		return false;
 	}
 
 	// Checks specific permissions for sub commands in mctrade and global.
 	private boolean checkPerms(Player player, String p) {
 		if (MCTrade.perms.has(player, "mctrade." + p)) {
 			return true;
 		} else if (MCTrade.perms.has(player, "mctrade.*")) {
 			return true;
 		}
 		m.tellPlayer(player, Msg.PERMISSION_DENIED);
 		return false;
 	}
 
 	public int checkItemMax(Player p) {
 		int amount = 0;
 		for (ItemStack i : p.getInventory().getContents()) {
 			if (i == null) {
 				continue;
 			}
 			if (i.getType().equals(getItemMaterial())) {
 				amount = amount + i.getAmount();
 			}
 		}
 		return amount;
 	}
 
 	public void removeItem(Player player, Material type, int amount) {
 		ItemStack[] contents = player.getInventory().getContents();
 		int counter = 0;
 		for (ItemStack stack : Arrays.asList(contents)) {
 			if (stack == null) {
 				continue;
 			}
 			if (!(stack.getType().equals(type))) {
 				continue;
 			}
 			if (stack.getAmount() < amount) {
 				contents[counter] = null;
 				amount -= stack.getAmount();
 			}
 			if (stack.getAmount() == amount) {
 				contents[counter] = null;
 				break;
 			}
 			if (stack.getAmount() > amount) {
 				stack.setAmount(stack.getAmount() - amount);
 				contents[counter] = stack;
 				break;
 			}
 			counter++;
 		}
 		player.getInventory().setContents(contents);
 	}
 
 	public boolean onTradeRemoveItem(ItemStack is, Player p) {
 		p.getInventory().removeItem(is);
 		return true;
 	}
 
 	private void prepareTrade(Player player) {
 		setItemId(player.getItemInHand().getTypeId());
 		setItemAmount(player.getItemInHand().getAmount());
 		setItemMaterial(player.getItemInHand().getType());
 		userId = DatabaseManager.getUserId(player.getName());
 	}
 
 	private int getUserId(Player player) {
 		return DatabaseManager.getUserId(player.getName());
 	}
 
 	public int getItemId() {
 		return itemId;
 	}
 
 	public void setItemId(int itemId) {
 		this.itemId = itemId;
 	}
 
 	public Material getItemMaterial() {
 		return itemMaterial;
 	}
 
 	public void setItemMaterial(Material itemMaterial) {
 		this.itemMaterial = itemMaterial;
 	}
 
 	public int getItemAmount() {
 		return itemAmount;
 	}
 
 	public void setItemAmount(int itemAmount) {
 		this.itemAmount = itemAmount;
 	}
 }
