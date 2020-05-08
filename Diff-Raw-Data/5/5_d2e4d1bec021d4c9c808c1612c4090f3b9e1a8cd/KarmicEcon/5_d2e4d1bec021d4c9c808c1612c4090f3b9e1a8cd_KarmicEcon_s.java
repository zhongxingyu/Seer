 package com.mitsugaru.Karmiconomy;
 
 import java.util.EnumMap;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.black_ixx.playerPoints.PlayerPointsAPI;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.mitsugaru.Karmiconomy.config.Config;
 import com.mitsugaru.Karmiconomy.config.KConfig;
 import com.mitsugaru.Karmiconomy.database.Field;
 
 public class KarmicEcon
 {
 	private static Karmiconomy plugin;
 	private static Config rootConfig;
 	private static Economy eco;
 	private static boolean playerpoints, vault;
 
 	public KarmicEcon(Karmiconomy plugin)
 	{
 		KarmicEcon.plugin = plugin;
 		KarmicEcon.rootConfig = plugin.getPluginConfig();
 	}
 
 	public boolean setupEconomy()
 	{
 		// Check vault
 		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer()
 				.getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null)
 		{
 			eco = economyProvider.getProvider();
 			vault = true;
 		}
 		// Check playerpoints
 		final Plugin playerPointsPlugin = plugin.getServer().getPluginManager()
 				.getPlugin("PlayerPoints");
 		if (playerPointsPlugin != null)
 		{
 			playerpoints = true;
 		}
 		// None fond
 		if (!playerpoints && !vault)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean denyPay(Player player, double pay)
 	{
 		boolean paid = false;
 		if (vault)
 		{
 			// Deny by player balance
 			final double balance = eco.getBalance(player.getName());
 			if (pay < 0.0)
 			{
 				// Only care about negatives. Need to change to positive for
 				// comparison.
 				pay *= -1;
 				if (pay > balance)
 				{
 					paid = true;
 				}
 			}
 		}
 		if (playerpoints)
 		{
 			final int playerPoints = PlayerPointsAPI.look(player.getName());
 			if (pay < 0.0)
 			{
				pay *= 1;
 				if (pay > playerPoints)
 				{
 					paid = true;
 				}
 			}
 		}
 		return paid;
 	}
 
 	public static boolean pay(Field field, Player player, KConfig config,
 			Item item, String command)
 	{
 		boolean paid = false;
 		final double amount = config.getPayValue(field, item, command);
 		final boolean local = config.sendBroadcast(field);
 		if (amount == 0.0)
 		{
 			// Just record that it happened
 			return true;
 		}
 		final EnumMap<LocalString.Flag, String> info = new EnumMap<LocalString.Flag, String>(
 				LocalString.Flag.class);
 		info.put(LocalString.Flag.TAG, Karmiconomy.TAG);
 		info.put(LocalString.Flag.AMOUNT, "" + String.format("%.2f", amount));
 		info.put(LocalString.Flag.EVENT, field.name());
 		info.put(LocalString.Flag.EXTRA, "");
 		if (item != null)
 		{
 			info.put(LocalString.Flag.EXTRA, ChatColor.WHITE + "- " + item.name);
 		}
 		else if (command != null)
 		{
 			info.put(LocalString.Flag.EXTRA, ChatColor.WHITE + "- " + command);
 		}
 		if (vault)
 		{
 			EconomyResponse response = null;
 			if (amount > 0.0)
 			{
 				response = eco.depositPlayer(player.getName(), amount);
 			}
 			else if (amount < 0.0)
 			{
 				response = eco.withdrawPlayer(player.getName(), (amount * -1));
 			}
 			if (response != null)
 			{
 				String message = "";
 				switch (response.type)
 				{
 					case FAILURE:
 					{
 						if (config.getDenyPay(field, item, command))
 						{
 							message = LocalString.ECONOMY_FAILURE
 									.parseString(info);
 							player.sendMessage(message);
 							Karmiconomy.sentMessages.put(player.getName(),
 									message);
 							if (rootConfig.debugEconomy)
 							{
 								plugin.getLogger()
 										.severe("Eco Failure: "
 												+ response.errorMessage);
 							}
 						}
 						break;
 					}
 					case NOT_IMPLEMENTED:
 					{
 						message = LocalString.ECONOMY_FAILURE.parseString(info);
 						player.sendMessage(message);
 						Karmiconomy.sentMessages.put(player.getName(), message);
 						if (rootConfig.debugEconomy)
 						{
 							plugin.getLogger().severe(
 									"Eco not implemented: "
 											+ response.errorMessage);
 						}
 						break;
 					}
 					case SUCCESS:
 					{
 						if (rootConfig.debugEconomy)
 						{
 							plugin.getLogger().info(
 									"Eco success for player '"
 											+ player.getName()
 											+ "' of amount: " + amount);
 						}
 						paid = true;
 						if (local)
 						{
 							player.sendMessage(LocalString.LOCAL_MESSAGE
 									.parseString(info));
 						}
 					}
 					default:
 						break;
 				}
 			}
 		}
 		if (playerpoints)
 		{
 			int points = (int) amount;
 			if (points == 0)
 			{
 				return true;
 			}
 			else
 			{
 				if (amount > 0.0)
 				{
 					paid = PlayerPointsAPI.give(player.getName(), points);
 				}
 				else if (amount < 0.0)
 				{
 					// Don't override vault
 					if (paid)
 					{
 						PlayerPointsAPI.take(player.getName(), points);
 					}
 					else
 					{
 						paid = PlayerPointsAPI.take(player.getName(), points);
 					}
 				}
 				//Send message if enabled
 				if (local && paid)
 				{
 					player.sendMessage(LocalString.LOCAL_MESSAGE
 							.parseString(info));
 				}
 				else if (local)
 				{
 					player.sendMessage(LocalString.ECONOMY_FAILURE
 							.parseString(info));
 				}
 			}
 		}
 		return paid;
 	}
 
 	public static void payMessage(Field field, Player player, double amount,
 			Item item, String command)
 	{
 		// If message is enabled, notify player
 	}
 }
