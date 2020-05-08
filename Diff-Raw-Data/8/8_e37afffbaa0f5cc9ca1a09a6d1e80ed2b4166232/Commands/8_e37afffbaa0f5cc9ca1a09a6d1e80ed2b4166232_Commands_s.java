 package com.mistphizzle.donationpoints.plugin;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 
 import com.mistphizzle.donationpoints.plugin.Objects.Transaction;
 
 public class Commands {
 
 	DonationPoints plugin;
 
 	public Commands(DonationPoints instance) {
 		this.plugin = instance;
 		init();
 	}
 
 	// Strings
 	public static String Prefix;
 	public static String RestrictedWorldMessage;
 	public static String noPermissionMessage;
 	public static String InvalidArguments;
 	public static String NoCommandExists;
 	public static String DPConfirm;
 	public static String DPActivate;
 	public static String DPSuccessfulActivation;
 	public static String DPFailedActivation;
 	public static String ExpireDate;
 	public static String DPGive;
 	public static String DPTake;
 	public static String NoAccount;
 	public static String AccountCreated;
 	public static String TransferOff;
 	public static String NoTransfer;
 	public static String TransferSent;
 	public static String TransferReceive;
 	public static String PlayerOnly;
 	public static String ReloadSuccessful;
 	public static String PlayerBalance;
 	public static String OtherBalance;
 	public static String AccountAlreadyExists;
 	public static String NoPurchaseStarted;
 	public static String NeedActivation;
 	public static String PurchaseSuccessful;
 	public static String TooLongOnConfirm;
 	public static String LimitReached;
 	public static String PackageActivated;
 	public static String DPSet;
 	public static String NotEnoughPoints;
 	public static String InvalidPackage;
 	public static String DPPrerequisite;
 	public static String SignLeftClick;
 	public static String SignLeftClickDescription;
 	public static String Server;
 	public static String RequiredInventorySpace;
 
 	private void init() {
 		PluginCommand donationpoints = plugin.getCommand("donationpoints");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length < 1) {
 					// Base Command
 					s.sendMessage("-----§4DonationPoints Commands§f-----");
 					s.sendMessage("§3/dp basic§f - Show basic DonationPoints commands.");
 					s.sendMessage("§3/dp packages§f - Show the DonationPoints packages commands.");
 					s.sendMessage("§3/dp admin§f - Show the DonationPoints Admin Commands.");
 					return true;
 					// Packages Commands
 				} if (args[0].equalsIgnoreCase("packages")) {
 					s.sendMessage("-----§4DonationPoints Package Commands§f-----");
 					if (DonationPoints.permission.has(s, "donationpoints.package.info")) {
 						s.sendMessage("§3/dp package info <packageName>§f - Shows package information.");
 					} if (DonationPoints.permission.has(s, "donationpoints.package.list")) {
 						s.sendMessage("§3/dp package list§f - List all packages.");
 					} else {
 						s.sendMessage("§cYou don't have permission to use any of the packages commands.");
 					}
 					// Admin Commands
 				} if (args[0].equalsIgnoreCase("admin")) {
 					s.sendMessage("-----§4DonationPoints Admin Commands§f-----");
 					if (DonationPoints.permission.has(s, "donationpoints.give")) {
 						s.sendMessage("§3/dp give <player> <amount>§f - Give points to a player.");
 					} if (DonationPoints.permission.has(s, "donationpoints.take")) {
 						s.sendMessage("§3/dp take <player> <amount>§f - Take points from a player.");
 					} if (DonationPoints.permission.has(s, "donationpoints.set")) {
 						s.sendMessage("§3/dp set <player> <amount>§f - Set a player's balance.");
 					} if (DonationPoints.permission.has(s, "donationpoints.version")) {
 						s.sendMessage("§3/dp version§f - Shows the version of the plugin you're running.");
 					} if (DonationPoints.permission.has(s, "donationpoints.update")) {
 						s.sendMessage("§3/dp update§f - Checks if there is an update available.");
 					} if (DonationPoints.permission.has(s, "donationpoints.reload")) {
 						s.sendMessage("§3/dp reload§f - Reloads the Configuration / Packages.");
 					} if (DonationPoints.permission.has(s, "donationpoints.purge")) {
 						s.sendMessage("§3/dp purge§f - Purges Empty Accounts.");
 					} if (DonationPoints.permission.has(s, "donationpoints.delete")) {
 						s.sendMessage("§3/dp delete§f - Deletes a player's account.");
 					} else {
 						s.sendMessage("§cYou don't have any permission for ANY DonationPoints Admin Commands.");
 					}
 				} if (args[0].equalsIgnoreCase("basic")) {
 					s.sendMessage("-----§4DonationPoints Basic Commands§f-----");
 					if (DonationPoints.permission.has(s, "donationpoints.create")) {
 						s.sendMessage("§3/dp create§f - Creates a points account for you.");
 					}
 					if (DonationPoints.permission.has(s, "donationpoints.balance")) {
 						s.sendMessage("§3/dp balance§f - Checks your points balance.");
 					} if (DonationPoints.permission.has(s, "donationpoints.transfer")) {
 						s.sendMessage("§3/dp transfer <player> <amount>§f - Transfer Points.");
 					} if (DonationPoints.permission.has(s, "donationpoints.transfer")) {
 						s.sendMessage("§3/dp purchase <package>§f - Purchase a package.");
 					} else {
 						s.sendMessage("§cYou don't have permission for any DonationPoints Basic Commands.");
 					}
 				} if (args[0].equalsIgnoreCase("transfer")) {
 					if (!plugin.getConfig().getBoolean("General.Transferrable")) {
 						s.sendMessage(Prefix + TransferOff);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.transfer")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (!(s instanceof Player)) {
 						s.sendMessage(Prefix + PlayerOnly);
 						return true;
 					}
 					if (args.length < 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					String sender = s.getName();
 					String target = args[1];
 					Double transferamount = Double.parseDouble(args[2]);
 					if (!Methods.hasAccount(sender.toLowerCase())) {
 						s.sendMessage(Prefix + NoAccount.replace("%player", sender));
 						return true;
 					}
 					if (target.equalsIgnoreCase(sender)) {
 						s.sendMessage(Prefix + NoTransfer);
 						return true;
 					}
 					if (transferamount < 0) {
 						s.sendMessage(Prefix + "§cYou cannot send a negative balance.");
 						return true;
 					}
 					if (transferamount > Methods.getBalance(sender)) {
 						s.sendMessage(Prefix + NoTransfer);
 						return true;
 					}
 					if (transferamount == 0) {
 						s.sendMessage(Prefix + NoTransfer);
 						return true;
 					}
 					Methods.addPoints(transferamount, target.toLowerCase());
 					Methods.removePoints(transferamount, sender.toLowerCase());
 					Double transferamount2 = Methods.roundTwoDecimals(transferamount);
 					s.sendMessage(Prefix + TransferSent.replace("%player", target).replace("%amount", transferamount2.toString()));
 					if (Bukkit.getPlayer(target) != null) {
 						Bukkit.getPlayer(target).sendMessage(Prefix + TransferReceive.replace("%player", sender).replace("%amount", transferamount2.toString()));
 					}
 					return true;
 				} if (args[0].equalsIgnoreCase("reload")) {
 					if (!Methods.hasPermission(s, "donationpoints.reload")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					plugin.reloadConfig();
 					try {
 						plugin.configCheck();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					s.sendMessage(Prefix + ReloadSuccessful);
 					return true;
 				} if (args[0].equalsIgnoreCase("balance")) {
 					if (!Methods.hasPermission(s, "donationpoints.balance")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length == 1) {
 						if (!Methods.hasAccount(s.getName().toLowerCase())) {
 							s.sendMessage(Prefix + NoAccount.replace("%player", s.getName()));
 							return true;
 						}
 						Double balance = Methods.getBalance(s.getName().toLowerCase());
 						String balance2 = balance.toString();
 						s.sendMessage(Prefix + PlayerBalance.replace("%amount", balance2));
 						return true;
 					} if (args.length == 2) {
 						if (!Methods.hasPermission(s, "donationpoints.balance.others")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 							return true;
 						}
 						String string = args[1];
 						if (!Methods.hasAccount(string.toLowerCase())) {
 							s.sendMessage(Prefix + NoAccount.replace("%player", string));
 							return true;
 						}
 						Double balance = Methods.getBalance(string);
 						String balance2 = balance.toString();
 						s.sendMessage(Prefix + OtherBalance.replace("%player", string).replace("%amount", balance2));
 					}
 				} if (args[0].equalsIgnoreCase("create")) {
 					if (!Methods.hasPermission(s, "donationpoints.create")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length == 1) {
 						String string = s.getName();
 						if (!Methods.hasAccount(string)) {
 							Methods.createAccount(string);
 							s.sendMessage(Prefix + AccountCreated.replace("%player", string));
 							return true;
 						}
 						s.sendMessage(Prefix + AccountAlreadyExists.replace("%player", string));
 						return true;
 					} if (args.length == 2) {
 						if (!Methods.hasPermission(s, "donationpoints.create.others")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 							return true;
 						}
 						String string = args[1];
 						if (!Methods.hasAccount(string)) {
 							Methods.createAccount(string);
 							s.sendMessage(Prefix + AccountCreated.replace("%player", string));
 							return true;
 						} if (Methods.hasAccount(string)) {
 							s.sendMessage(Prefix + AccountAlreadyExists.replace("%player", string));
 							return true;
 						}
 					}					
 				} if (args[0].equalsIgnoreCase("give")) {
 					if (!Methods.hasPermission(s, "donationpoints.give")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length != 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					Double addamount = Double.parseDouble(args[2]);
 					String target = args[1].toLowerCase();
 					if (!Methods.hasAccount(target)) {
 						s.sendMessage(Prefix + NoAccount);
 						return true;
 					}
 					Methods.addPoints(addamount, target);
 					String addamount2 = addamount.toString();
 					s.sendMessage(Prefix + DPGive.replace("%amount", addamount2).replace("%player", target));
 				} if (args[0].equalsIgnoreCase("take")) {
 					if (args.length != 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.take")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					Double takeamount = Double.parseDouble(args[2]);
 					String target = args[1].toLowerCase();
 					Methods.removePoints(takeamount, target);
 					String takeamount2 = takeamount.toString();
 					s.sendMessage(Prefix + DPTake.replace("%amount", takeamount2).replace("%player", target));
 				} if (args[0].equalsIgnoreCase("confirm")) {
 					Bukkit.getScheduler().cancelTask(PlayerListener.confirmTask);
 					if (!DonationPoints.permission.has(s, "donationpoints.confirm")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (!PlayerListener.purchases.containsKey(s.getName().toLowerCase())) {
 						s.sendMessage(Prefix + NoPurchaseStarted);
 						return true;
 					}
 
 					String pack = PlayerListener.purchases.get(s.getName().toLowerCase());
 					Double price;
 
 					if (Methods.hasPermission(s, "donationpoints.free")) {
 						price = 0.0;
 					} else {
 						price = plugin.getConfig().getDouble("packages." + pack + ".price");
 					}
 
 					String expireDate = getExpireDate(pack);
 					Integer limit = plugin.getConfig().getInt("packages." + pack + ".limit");
 					boolean ActivateImmediately = plugin.getConfig().getBoolean("packages." + pack + ".ActivateImmediately");
 					Integer requiredSlots = plugin.getConfig().getInt("packages." + pack + ".RequiredInventorySpace");
 					if (requiredSlots == null) {
 						requiredSlots = 0;
 					}
 
 					Player player = (Player) s;
 
 					if (!Methods.hasInventorySpace(player, requiredSlots)) {
 						s.sendMessage(Prefix + RequiredInventorySpace.replace("%slot", requiredSlots.toString()));
 						return true;
 					}
 					if (Methods.NeedActive(s.getName().toLowerCase(), pack)) {
 						s.sendMessage(Prefix + NeedActivation.replace("%pack", pack));
 						s.sendMessage(Prefix + DPActivate.replace("%pack", pack));
 						return true;
 					}
 
 					boolean UseVaultEconomy = plugin.getConfig().getBoolean("packages." + pack + ".UseVaultEconomy");
 
 					String expires;
 					String activated;
 					int ExpireTime = plugin.getConfig().getInt("packages." + pack + ".ExpireTime");
 					if (ExpireTime != 0) {
 						expires = "true";
 					} else {
 						expires = "false";
 					}
 					if (ActivateImmediately) {
 						activated = "true";
 					} else {
 						activated = "false";
 					}
 
 					if (limit != 0) {
 						int totalPurchased = Methods.getTotalPackagePurchased(s.getName().toLowerCase(), pack);
 						if (totalPurchased >= limit) {
 							s.sendMessage(Prefix + LimitReached.replace("%pack", pack).replace("%limit", limit.toString()));
 							return true; // We have dealt with any issue with limits.
 						}
 					}
 
 					/*
 					 * By this point we are sure the purchase is going through. So now is just activating it / removing points / money
 					 */
 
 					Transaction tran = new Transaction(s.getName().toLowerCase(), price, pack, Methods.getCurrentDate(), activated, expires, expireDate, "false", Commands.Server);
 
 					if (UseVaultEconomy) {
 						Methods.econ.withdrawPlayer(s.getName(), price);
 						s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack).replace("%amount", price.toString()));
 					} else {
 						Methods.removePoints(price, s.getName().toLowerCase());
 					}
 
 					if (!ActivateImmediately) {
 						s.sendMessage(Prefix + DPActivate.replace("%pack", pack));
 					} else {
 						List<String> commands = plugin.getConfig().getStringList("packages." + pack + ".ActivationCommands");
 						for (String cmd: commands) {
 							if (cmd.contains("p:")) {
 								String[] parts = cmd.split(":");
 								String cmd2 = parts[1];
 								plugin.getServer().dispatchCommand(s, cmd2.replace("%player", s.getName()));
 							} else {
 								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", s.getName()));
 							}
 						}
 						s.sendMessage(Prefix + PackageActivated.replace("%pack", pack));
 					}
 					PlayerListener.purchases.remove(s.getName().toLowerCase());
 					Methods.logTransaction(tran);
 				} if (args[0].equalsIgnoreCase("activate")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.activate")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length != 2) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					String pack2 = args[1];
 					String expiredate = getExpireDate(pack2);
 					String sender = s.getName();
 					Boolean expires = plugin.getConfig().getBoolean("packages." + pack2 + ".expires");
 					Double ActualPrice = plugin.getConfig().getDouble("packages." + pack2 + ".price");
 
 					ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable + " WHERE player = '" + sender + "' AND package = '" + pack2 + "' AND activated = 'false';");
 					try {
 						if(rs2.next()) {
 							if (ActualPrice == 0) {
 								s.sendMessage(Prefix + DPFailedActivation.replace("%pack", pack2));
 								s.sendMessage(Prefix + "§cPackage names are case sensitive.");
 								return true;
 							}
 							DBConnection.sql.modifyQuery("UPDATE " + DBConnection.transactionTable + " SET activated = 'true' WHERE player = '" + sender + "' AND package = '" + pack2 + "';");
 							s.sendMessage(Prefix + PackageActivated.replace("%pack", pack2));
 							List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".commands");
 							for (String cmd : commands) {
 								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", s.getName()));
 							}
 
 							if (expires.equals(true)) {
 								DBConnection.sql.modifyQuery("UPDATE " + DBConnection.transactionTable + " SET expiredate = '" + expiredate + "' WHERE player = '" + sender + "' AND package = '" + pack2 + "';");
 								s.sendMessage(Prefix + ExpireDate.replace("%pack", pack2).replace("%expiredate", expiredate));
 							} return true;
 						} if (!rs2.next()) {
 							s.sendMessage(Prefix + DPFailedActivation.replace("%pack", pack2));
 							s.sendMessage(Prefix + "§cPackage names are case sensitive.");
 							return true;
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 				} if (args[0].equalsIgnoreCase("purge")) {
 					if (!DonationPoints.permission.has(s,  "donationpoints.purge")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					Methods.purgeEmptyAccounts();
 					s.sendMessage(Prefix + " §cAll Empty Accounts Purged.");
 					return true;
 				} if (args[0].equalsIgnoreCase("set")) {
 					if (args.length != 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.set")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					String target = args[1].toLowerCase();
 					Double amount = Double.parseDouble(args[2]);
 					Methods.setPoints(amount, target);
 					String amount2 = amount.toString();
 					s.sendMessage(Prefix + DPSet.replace("%player", target).replace("%amount", amount2));
 					return true;
 				} if (args[0].equalsIgnoreCase("package")) {					
 					if (args[1].equalsIgnoreCase("info")) {
 						if (!DonationPoints.permission.has(s, "donationpoints.package.info")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 							return true;
 						}
 						if (args.length < 2 | args.length > 4) {
 							s.sendMessage(Prefix + InvalidArguments);
 							return true;
 						}
 						String packName = args[2];
 						String caseSensitivePackName = packName;
 
 						// Resolve the supplied package name to the case sensitive equivalent from the config.
 						List<String> packages = new ArrayList<String>(plugin.getConfig().getConfigurationSection("packages").getKeys(false));
 						for (String packageName : packages) {
 							if (packageName.toLowerCase().equals(packName.toLowerCase())) {
 								caseSensitivePackName = packageName;
 								break;
 							}
 						}
 						packName = caseSensitivePackName;
 
 						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
 						String description = plugin.getConfig().getString("packages." + packName + ".description");
 						s.sendMessage("-----§e" + packName + " Info§f-----");
 						s.sendMessage("§aPackage Name:§3 " + packName);
 						s.sendMessage("§aPrice:§3 " + price + "0");
 						s.sendMessage("§aDescription:§3 " + description);
 						if (plugin.getConfig().getBoolean("packages." + packName + ".expires") == false) {
 							s.sendMessage("§aExpires: §3Never");
 						} else if (plugin.getConfig().getBoolean("packages." + packName + ".expires") == true) {
 							int expiretime = plugin.getConfig().getInt("packages." + packName + ".expiretime");
 							if (expiretime == 1) {
 								s.sendMessage("§aExpires After: §3" + expiretime + " Day");
 							} else if (expiretime != 1) {
 								s.sendMessage("§aExpires After: §3" + expiretime + " Days");
 							}
 						}
 						if (plugin.getConfig().getBoolean("packages." + packName + ".requireprerequisite") == false) {
 							s.sendMessage("§aPrerequisite: §3None");
 						} else if (plugin.getConfig().getBoolean("packages." + packName + ".requireprerequisite") == true) {
 							s.sendMessage("§aPrerequisite: §3" + plugin.getConfig().getString("packages." + packName + ".prerequisite"));
 						}
 					}
 
 					if (args[1].equalsIgnoreCase("list")) {
 						if (!DonationPoints.permission.has(s, "donationpoints.package.list")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 							return true;
 						}
 						List<String> packages = new ArrayList<String>(plugin.getConfig().getConfigurationSection("packages").getKeys(false));
 						List<String> packagestoRemove = new ArrayList<String> ();
 						for (String p1: packages) {
 							if (plugin.getConfig().getBoolean("packages." + p1 + ".requireprerequisite")) {
 								String prerequisite = plugin.getConfig().getString("packages." + p1 + ".prerequisite");
 								if (!Methods.hasPurchased(s.getName(), prerequisite, Server)) {
 									packagestoRemove.add(p1);
 								}
 							}
 							if (plugin.getConfig().getBoolean("General.SpecificPermissions")) {
 								if (!DonationPoints.permission.has(s, "donationpoints.purchase." + p1)) {
 									packagestoRemove.add(p1);
 								}
 							}
 						}
 						packages.removeAll(packagestoRemove);
 						s.sendMessage(Prefix + "§3Available Packages: §a" + packages.toString());
 						return true;
 					}
 				} if (args[0].equalsIgnoreCase("purchase")) {
 					if (args.length != 2) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					} if (!DonationPoints.permission.has(s, "donationpoints.purchase")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;				
 					}
 					String packName = args[1];
 					String caseSensitivePackName = packName;
 
 					// Resolve the supplied package name to the case sensitive equivalent from the config.
 					List<String> packages = new ArrayList<String>(plugin.getConfig().getConfigurationSection("packages").getKeys(false));
 					for (String packageName : packages) {
 						if (packageName.toLowerCase().equals(packName.toLowerCase())) {
 							caseSensitivePackName = packageName;
 							break;
 						}
 					}
 					packName = caseSensitivePackName;
 
 					String purchasedPack = packName;
 					boolean usesVault;
 					boolean hasPreRequisites;
 					Set<String> preRequisites = new HashSet<String>();
 
 					if (Methods.isPackMisconfigured(purchasedPack)) {
 						s.sendMessage(Commands.Prefix + ChatColor.RED + "This package is misconfigured. Consult an administrator.");
 						return true;
 					}
 
 					if (plugin.getConfig().getStringList("packages." + purchasedPack + ".PreRequisites") == null 
 							|| plugin.getConfig().getStringList("packages." + purchasedPack + ".PreRequisites").isEmpty()) {
 						hasPreRequisites = false;
 					} else {
 						hasPreRequisites = true;
 					}
 
 					if (hasPreRequisites) {
 						preRequisites.addAll(plugin.getConfig().getStringList("packages." + purchasedPack + ".PreRequisites"));
 					}
 
 					for (String preRequisite: preRequisites) {
 						if (!Methods.hasPurchased(s.getName().toLowerCase(), preRequisite, Commands.Server)) {
 							s.sendMessage(Commands.Prefix + Commands.DPPrerequisite.replace("%pack", preRequisite));
 							return true;
 						}
 					}
 
 					if (plugin.getConfig().getBoolean("General.SpecificPermission", true)) {
 						if (!Methods.hasPermission(s, "donationpoints.purchase." + purchasedPack)) {
 							s.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 							return true;
 						}
 					}
 
 					if (plugin.getConfig().getBoolean("packages." + purchasedPack + ".UseVaultEconomy")) {
 						usesVault = true;
 					} else {
 						usesVault = false;
 					}
 
 					Double price;
 					boolean worldRestricted;
 
 					if (plugin.getConfig().getStringList("packages." + purchasedPack + ".RestrictToWorlds") != null
 							&& plugin.getConfig().getStringList("packages." + purchasedPack + ".RestrictToWorlds").isEmpty()) {
 						worldRestricted = false;
 					} else {
 						worldRestricted = true;
 					}
 
 					if (worldRestricted) {
 						List<String> applicableWorlds = plugin.getConfig().getStringList("packages." + purchasedPack + ".RestrictToWorlds");
 						if (!applicableWorlds.contains(((Player) s).getWorld().getName().toLowerCase())) {
 							s.sendMessage(Commands.Prefix + Commands.RestrictedWorldMessage.replace("%worlds", applicableWorlds.toString()));
 							return true;
 						}
 					}
 
 					if (Methods.hasPermission(s, "donationpoints.free")) {
 						price = 0.0;
 					} else {
 						price = plugin.getConfig().getDouble("packages." + purchasedPack + ".price");
 					}
 
 					if (usesVault) {
 						if (!Methods.econ.has(s.getName(), price)) {
 							s.sendMessage(Commands.Prefix + Commands.NotEnoughPoints);
 							return true;
 						}
 					} else {
 						if (Methods.getBalance(s.getName().toLowerCase()) < price) {
 							s.sendMessage(Commands.Prefix + Commands.NotEnoughPoints);
 							return true;
 						}
 					}
 
 					final Player player = (Player) s;
 					PlayerListener.purchases.put(player.getName().toLowerCase(), purchasedPack);
 					if (PlayerListener.purchases.containsKey(player.getName().toLowerCase())) {
 						player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%pack", purchasedPack).replace("%price", price.toString()));
 						PlayerListener.confirmTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 							public void run() {
 								if (PlayerListener.purchases.containsKey(player.getName().toLowerCase())) {
 									PlayerListener.purchases.remove(player.getName().toLowerCase());
 									player.sendMessage(Commands.Prefix + Commands.TooLongOnConfirm);
 								}
 							}
 						}, 300L);
 					}
 				} else if (args[0].equalsIgnoreCase("delete")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.delete")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length != 2) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					String accountName = args[1];
 					if (!Methods.hasAccount(accountName)) {
 						s.sendMessage(Prefix + NoAccount.replace("%player", accountName));
 						return true;						
 					}
 					Methods.deleteAccount(accountName.toLowerCase());
 					s.sendMessage(Prefix + "§cDeleted §3" + accountName + "'s §caccount.");
 				} else if (args[0].equalsIgnoreCase("link")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.link")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length != 2) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					String packName = args[1];
 					if (!Methods.getPackageExists(packName)) {
 						s.sendMessage(Prefix + InvalidPackage);
 						return true;
 					}
 					if (PlayerListener.links.containsKey(s.getName())) {
 						PlayerListener.links.remove(s.getName());
 					}
 					s.sendMessage(Prefix + "§cClick the Item Frame you would like to link §3" + packName + " §cto.");
 					PlayerListener.links.put(s.getName(), packName);
 				} else if (args[0].equalsIgnoreCase("version")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.version")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					s.sendMessage(Prefix + plugin.getDescription().getVersion());
 					s.sendMessage(Prefix + "http://dev.bukkit.org/server-mods/DonationPoints");
 					s.sendMessage(Prefix + "Created by: MistPhizzle");
 				} else {
 					s.sendMessage(Prefix + NoCommandExists);
 					return true;
 				}
 				return true;
 			}
 		}; donationpoints.setExecutor(exe);
 	}
 
 	public String getExpireDate(String packagename) {
 		int minutes = plugin.getConfig().getInt("packages." + packagename + ".ExpireTime");
 		if (!(minutes == 0)) {
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			Calendar c = Calendar.getInstance();
 			c.add(Calendar.MINUTE, minutes);
 			return sdf.format(c.getTime());
 
 		}
 		return null;
 		//		int days = plugin.getConfig().getInt("packages." + packagename + ".expiretime");
 		//		if (!(days == 0)) {
 		//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		//			Calendar c = Calendar.getInstance();
 		//			try {
 		//				c.setTime(sdf.parse(Methods.getCurrentDate()));
 		//			} catch (ParseException e) {
 		//				e.printStackTrace();
 		//			}
 		//
 		//			c.add(Calendar.DATE, days);
 		//			String exp = sdf.format(c.getTime());
 		//			return exp;
 		//		}
 		//		return null;
 	}
 
 }
