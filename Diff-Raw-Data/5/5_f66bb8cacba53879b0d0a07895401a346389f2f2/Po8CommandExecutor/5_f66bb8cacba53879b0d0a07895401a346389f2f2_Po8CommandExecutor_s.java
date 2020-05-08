 package com.aegamesi.mc.po8;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.aegamesi.mc.po8.support.CardboardBox;
 import com.aegamesi.mc.po8.support.SerializedLocation;
 
 public class Po8CommandExecutor implements CommandExecutor {
 	public Po8Plugin plugin;
 
 	public Po8CommandExecutor(Po8Plugin plugin) {
 		this.plugin = plugin;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		// setup
 		boolean isPlayer = sender instanceof Player;
 		Player p = null;
 		if (isPlayer)
 			p = (Player) sender;
 
 		// pre-check
 		if (!cmd.getName().equalsIgnoreCase("po8"))
 			return false;
 		if (!isPlayer) {
 			Po8Util.message(sender, "&cYou can't use Po8 from the console!");
 			return true;
 		}
 		if (args.length == 0) {
 			sendHelp(sender, 1);
 			return true;
 		}
 
 		// implementation
 		if (args[0].equalsIgnoreCase("help")) {
 			int num = 0;
 			if (args.length >= 2) {
 				try {
 					num = Integer.parseInt(args[1]);
 				} catch (NumberFormatException e) {
 					Po8Util.message(sender, "Invalid arguments for /po8 help");
 					return true;
 				}
 			}
 			sendHelp(sender, num);
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("balance")) {
 			if (!sender.hasPermission("po8.balance")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 1) {
 				Po8Util.message(sender, "Invalid arguments for /po8 balance");
 				return true;
 			}
 			Po8Util.message(sender, "You have &a" + Po8Util.round2(Po8.playerMap.get(p.getName()).balance) + "&f Po8");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("transfer")) {
 			if (!sender.hasPermission("po8.transfer")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 3) {
 				Po8Util.message(sender, "Invalid arguments for /po8 transfer");
 				return true;
 			}
 			double amount;
 			try {
 				amount = Double.parseDouble(args[2]);
 			} catch (NumberFormatException e) {
 				Po8Util.message(sender, "Invalid arguments for /po8 transfer");
 				return true;
 			}
 			Player to = Bukkit.getPlayer(args[1]);
 			if (to == null) {
 				Po8Util.message(sender, "&c\"" + args[1] + "\" is not currently online");
 				return true;
 			}
 			if (amount < 0 || amount > Po8.playerMap.get(p.getName()).balance) {
 				Po8Util.message(sender, "&cYou don't have enough Po8");
 				return true;
 			}
 			Po8.playerMap.get(p.getName()).balance -= amount;
 			Po8.playerMap.get(to.getName()).balance += amount;
 			Po8Util.message(p, "Transferred &a" + amount + "&f Po8 to &a" + to.getDisplayName());
 			Po8Util.message(to, "You received &a" + amount + " Po8 from &a" + p.getDisplayName());
 			Po8Util.log(p.getName(), to.getName(), amount, "P2P_TRANSFER", "--");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("grant")) {
 			if (!sender.hasPermission("po8.grant")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 3) {
 				Po8Util.message(sender, "Invalid arguments for /po8 grant");
 				return true;
 			}
 			double amount;
 			try {
 				amount = Double.parseDouble(args[2]);
 			} catch (NumberFormatException e) {
 				Po8Util.message(sender, "Invalid arguments for /po8 grant");
 				return true;
 			}
 			Player to = Bukkit.getPlayer(args[1]);
 			if (to == null) {
 				Po8Util.message(sender, "&c\"" + args[1] + "\" is not currently online");
 				return true;
 			}
 			Po8.playerMap.get(to.getName()).balance += amount;
 			Po8Util.message(p, "Granted &a" + amount + "&f Po8 to &a" + to.getDisplayName());
 			if(amount >= 0)
 				Po8Util.message(to, "You have been granted &a" + amount + " Po8 by &a" + p.getDisplayName());
 			else
 				Po8Util.message(to, "&a" + amount + " Po8 has been removed from your account by &a" + p.getDisplayName());
 			Po8Util.log(p.getName(), to.getName(), amount, "GRANT", "--");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("version")) {
 			Po8Util.message(sender, "&dPo8 Plugin by PickleMan (aegamesi) (admin.aegamesi@gmail.com) v1.2.9.0");
 			if (args.length > 1)
 				Po8Util.message(sender, Po8Util.combine(args, 1, 0));
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("info")) {
 			if (!sender.hasPermission("po8.info") && isPlayer) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length < 2) {
 				Po8Util.message(sender, "Invalid arguments for /po8 info");
 				return true;
 			}
 			int amount = 1;
 			int temp = -1;
 			if (args.length >= 3) {
 				try {
 					temp = Integer.parseInt(args[args.length - 1]);
 				} catch (NumberFormatException e) {
 					temp = -1;
 				}
 				if (temp != -1)
 					amount = temp;
 			}
 			String item = Po8Util.combine(args, 1, temp == -1 ? 0 : args.length - 2);
 			String id = null;
 			if (item.equalsIgnoreCase("held")) {
 				ItemStack stack = p.getInventory().getItemInHand();
 				if (stack != null)
 					id = Po8Util.stockKey(stack.getData());
 			} else {
 				id = Po8Util.getBlock(item);
 			}
 			if (id == null || Po8.stockCheck(id) < 0) {
 				Po8Util.message(sender, "&cCould not find item \"" + item + "\"");
 				return true;
 			}
 			double price = Po8Util.getBasePrice(id) * amount;
 			int stock = Po8.stockCheck(id);
 			Po8Util.message(sender, "Po8 has &a" + stock + " " + Po8.itemMap.get(id).name + " &f in stock");
 			Po8Util.message(sender, "Po8 will buy &a" + amount + "x " + Po8.itemMap.get(id).name + "&f for &a" + Po8Util.round2(price / 2) + "&f Po8");
 			Po8Util.message(sender, "Po8 will sell &a" + amount + "x " + Po8.itemMap.get(id).name + "&f for &a" + Po8Util.round2(price) + "&f Po8");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("spawnchest")) {
 			if (!sender.hasPermission("po8.spawnchest")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 2) {
 				Po8Util.message(sender, "Invalid arguments for /po8 spawnchest");
 				return true;
 			}
 			int type = Po8.BUY;
 			if (args.length != 2 || (!args[1].equalsIgnoreCase("buy") && !args[1].equalsIgnoreCase("sell"))) {
 				Po8Util.message(sender, "Invalid arguments for /po8 spawnchest");
 				return true;
 			}
 			type = args[1].equalsIgnoreCase("sell") ? Po8.SELL : Po8.BUY;
 			Location l = p.getLocation();
 			l.setY(l.getBlockY() - 1);
 			l.getBlock().setType(Material.CHEST);
 			l.setX(l.getBlockX());
 			l.setY(l.getBlockY());
 			l.setZ(l.getBlockZ());
 			l.setYaw(0);
 			l.setPitch(0);
 			Po8.chestMap.put(new SerializedLocation(l), type);
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("examine")) {
 			if (!sender.hasPermission("po8.examine")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length < 3) {
 				Po8Util.message(sender, "Invalid arguments for /po8 examine");
 				return true;
 			}
 			String newName = "";
 			for (Map.Entry<String, Po8Player> entry : Po8.playerMap.entrySet()) {
 				String key = entry.getKey();
 				if (key.toLowerCase().equals(args[1].toLowerCase())) {
 					newName = key;
 					break;
 				}
 			}
 			if (newName.length() <= 0) {
 				Po8Util.message(sender, "&cPlayer not found. Be sure to use the exact name.");
 				return true;
 			}
 			int type = Po8.BUY;
 			if ((!args[2].equalsIgnoreCase("buy") && !args[2].equalsIgnoreCase("sell"))) {
 				Po8Util.message(sender, "Invalid arguments for /po8 examine");
 				return true;
 			}
 			type = args[2].equalsIgnoreCase("sell") ? Po8.SELL : Po8.BUY;
 			Po8Player player = Po8.playerMap.get(newName);
 			ItemStack[] inv = player.getInventory(type);
 			Po8InventoryHolder holder = new Po8InventoryHolder(type, newName, inv);
 			p.openInventory(holder.getInventory());
 			Po8Util.message(sender, "&a" + newName + " &f has &a" +  Po8Util.round2(Po8.playerMap.get(newName).balance) + " &fPo8");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("sell")) {
 			if (!sender.hasPermission("po8.sell")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			double totalValue = 0;
 			boolean notAllowed = false;
 			ArrayList<ItemStack> sellOrder = new ArrayList<ItemStack>();
 			ItemStack[] inv = Po8.playerMap.get(sender.getName()).getInventory(Po8.SELL);
 			for (int i = 0; i < inv.length; i++) {
 				ItemStack stack = inv[i];
 				if (stack == null)
 					continue;
 				if (Po8.stockCheck(Po8Util.stockKey(stack.getData())) >= 0) {
 					totalValue += (Po8Util.getBasePrice(Po8Util.stockKey(stack.getData())) / 2) * stack.getAmount();
 					sellOrder.add(stack);
 					inv[i] = null;
 				} else {
 					notAllowed = true;
 				}
 			}
 			if (totalValue > 0) {
 				Po8.playerMap.get(sender.getName()).setInventory(Po8.SELL, inv);
 				Po8Util.message(sender, "&6Successfully Submitted Sell Order!");
 				Po8Util.message(sender, "Please wait for processing. Value: &a" + Po8Util.round2(totalValue) + "&f Po8");
 				Po8.orderList.add(new Po8Order(Po8.SELL, totalValue, sender.getName(), sellOrder.toArray(new ItemStack[0])));
 			} else {
 				Po8Util.message(sender, "&cError: Empty Sell Order");
 				Player[] players = Bukkit.getServer().getOnlinePlayers();
 				for (Player player : players) {
 					if (player.hasPermission("po8.review") && Po8.playerMap.get(player.getName()).notify)
 						Po8Util.message(player, "&dNOTE: The following user submitted an empty sell order: " + sender.getName());
 				}
 				return true;
 			}
 			if (notAllowed)
 				Po8Util.message(sender, "&dWARNING: Your sell chest contains one or more unsellable items!");
 			//
 			Player[] players = Bukkit.getServer().getOnlinePlayers();
 			for (Player player : players) {
 				if (player.hasPermission("po8.review") && Po8.playerMap.get(player.getName()).notify)
 					Po8Util.message(player, "&dThere is a new Po8 order to be reviewed. Total: &5 " + Po8.orderList.size());
 			}
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("value")) {
 			if (!sender.hasPermission("po8.value")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			double totalValue = 0;
 			boolean notAllowed = false;
 			for (ItemStack stack : Po8.playerMap.get(sender.getName()).getInventory(Po8.SELL)) {
 				if (stack == null)
 					continue;
 				if (Po8.stockCheck(Po8Util.stockKey(stack.getData())) >= 0)
 					totalValue += (Po8Util.getBasePrice(Po8Util.stockKey(stack.getData())) / 2) * stack.getAmount();
 				else
 					notAllowed = true;
 			}
 			Po8Util.message(sender, "The total value of your Po8 Sell Chest is &a" + Po8Util.round2(totalValue) + "&f Po8");
 			if (notAllowed)
 				Po8Util.message(sender, "&dWARNING: Your sell chest contains one or more unsellable items!");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("order")) {
 			if (!sender.hasPermission("po8.order")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length < 2) {
 				sendHelp(sender, 2);
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("new")) {
 				Po8Util.message(sender, "&dCreated a new order");
 				Po8.playerMap.get(sender.getName()).buyOrder.clear();
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("price")) {
 				double totalValue = 0;
 				for (Map.Entry<String, Integer> entry : Po8.playerMap.get(sender.getName()).buyOrder.entrySet()) {
 					String key = entry.getKey();
 					int value = entry.getValue();
 					totalValue += Po8Util.getBasePrice(key) * value;
 				}
 				Po8Util.message(sender, "The total price of your Po8 Buy Order is &a" + Po8Util.round2(totalValue) + "&f Po8");
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("list")) {
 				Po8Util.message(sender, "&6---- Current Po8 Order ----");
 				for (Map.Entry<String, Integer> entry : Po8.playerMap.get(sender.getName()).buyOrder.entrySet()) {
 					String key = entry.getKey();
 					int value = entry.getValue();
 					Po8Util.message(sender, "&a" + value + "&fx&a " + Po8.itemMap.get(key).name);
 				}
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("add")) {
 				if (args.length < 3) {
 					Po8Util.message(sender, "Invalid arguments for /po8 order add");
 					return true;
 				}
 				int amount = 1;
 				int temp = -1;
 				if (args.length >= 4) {
 					try {
 						temp = Integer.parseInt(args[args.length - 1]);
 					} catch (NumberFormatException e) {
 						temp = -1;
 					}
 					if (temp != -1)
 						amount = temp;
 				}
 				String item = Po8Util.combine(args, 2, temp == -1 ? 0 : args.length - 3);
 				System.out.println(item);
 				String id = null;
 				if (item.equalsIgnoreCase("held")) {
 					ItemStack stack = p.getInventory().getItemInHand();
 					if (stack != null)
 						id = Po8Util.stockKey(stack.getData());
 				} else {
 					id = Po8Util.getBlock(item);
 				}
 				int stock = id == null ? -1 : Po8.stockCheck(id);
 				if (stock < 0) {
 					Po8Util.message(sender, "&cCould not find item \"" + item + "\"");
 					return true;
 				}
 				int current = Po8.playerMap.get(sender.getName()).buyOrder.containsKey(id) ? Po8.playerMap.get(sender.getName()).buyOrder.get(id) : 0;
 				current += amount;
 				if (current > stock) {
 					Po8Util.message(sender, "&cWARNING: Po8 only has " + stock + " of those. Your order has been reduced.");
 					current = stock;
 				}
 				Po8.playerMap.get(sender.getName()).buyOrder.put(id, current);
 				Po8Util.message(sender, "Your order now contains &a" + current + " " + Po8.itemMap.get(id).name);
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("remove")) {
 				if (args.length < 3) {
 					Po8Util.message(sender, "Invalid arguments for /po8 order remove");
 					return true;
 				}
 				int amount = 1;
 				int temp = -1;
 				if (args.length >= 4) {
 					try {
 						temp = Integer.parseInt(args[args.length - 1]);
 					} catch (NumberFormatException e) {
 						temp = -1;
 					}
 					if (temp != -1)
 						amount = temp;
 				}
 				String item = Po8Util.combine(args, 2, temp == -1 ? 0 : args.length - 3);
 				String id = Po8Util.getBlock(item);
 				int stock = id == null ? -1 : Po8.stockCheck(id);
 				if (stock < 0) {
 					Po8Util.message(sender, "&cCould not find item \"" + item + "\"");
 					return true;
 				}
 				int current = Po8.playerMap.get(sender.getName()).buyOrder.containsKey(id) ? Po8.playerMap.get(sender.getName()).buyOrder.get(id) : 0;
 				current -= amount;
 				if (current <= 0) {
 					current = 0;
 					Po8.playerMap.get(sender.getName()).buyOrder.remove(id);
 				}
 				Po8.playerMap.get(sender.getName()).buyOrder.put(id, current);
 				Po8Util.message(sender, "Your order now contains &a" + current + " " + Po8.itemMap.get(id).name);
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("submit")) {
 				if (Po8.playerMap.get(sender.getName()).buyOrder.size() <= 0) {
 					Po8Util.message(sender, "&cThe current buy order is empty!");
 					return true;
 				}
 				ArrayList<ItemStack> stack = new ArrayList<ItemStack>();
 				boolean reduced = false;
 				double price = 0;
 				for (Map.Entry<String, Integer> entry : Po8.playerMap.get(sender.getName()).buyOrder.entrySet()) {
 					String key = entry.getKey();
 					int amt = entry.getValue();
 					int stock = Po8.stockCheck(key);
 					if (stock < amt) {
 						reduced = true;
 						amt = stock;
 					}
 					if (amt <= 0)
 						continue;
 					price += Po8Util.getBasePrice(key) * amt;
 					Collections.addAll(stack, Po8Util.splitStack(key, amt));
 				}
 				if (price <= 0) {
 					Po8Util.message(sender, "&cThe current buy order is empty!");
 					return true;
 				}
 				if (stack.size() > 54) {
 					Po8Util.message(sender, "&cYou cannot buy more than 54 stacks of items at a time! Current order: " + stack.size());
 					return true;
 				}
 				if (reduced)
 					Po8Util.message(sender, "&dWARNING: Po8 does not have all of the items you have requested! Those items have been removed.");
 				if (price > Po8.playerMap.get(sender.getName()).balance) {
 					Po8Util.message(sender, "&cYou do not have enough Po8! Remove some items and try again.");
 					Po8Util.message(sender, "&cAvailable balance: " + Po8.playerMap.get(sender.getName()).balance + ", Required: " + price);
 					return true;
 				}
 				Po8Util.message(sender, "&6Successfully Submitted Buy Order!");
 				Po8Util.message(sender, "Please wait for processing. Price: &a" + price + "&f Po8");
 				Po8.playerMap.get(sender.getName()).buyOrder.clear();
 				Po8.orderList.add(new Po8Order(Po8.BUY, price, sender.getName(), stack.toArray(new ItemStack[0])));
 				for (ItemStack itemstack : stack)
 					Po8.stockAdd(Po8Util.stockKey(itemstack.getData()), -itemstack.getAmount());
 				Po8.playerMap.get(sender.getName()).balance -= price;
 				//
 				Player[] players = Bukkit.getServer().getOnlinePlayers();
 				for (Player player : players) {
 					if (player.hasPermission("po8.review") && Po8.playerMap.get(player.getName()).notify)
 						Po8Util.message(player, "&dThere is a new Po8 order to be reviewed. Total: &5 " + Po8.orderList.size());
 				}
 				return true;
 			}
 			sendHelp(sender, 2);
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("review")) {
 			// accept, deny
 			if (!sender.hasPermission("po8.review")) {
 				Po8Util.message(sender, "&cYou don't have permission to do that!");
 				return true;
 			}
 			if (args.length < 2) {
 				sendHelp(sender, 3);
 				return true;
 			}
 			// deny:
 			// buy: return items to stock, return po8 to user
 			// sell: return items to chest
 			if (args[1].equalsIgnoreCase("deny")) {
 				if (Po8.playerMap.get(sender.getName()).reviewOrder == null) {
 					Po8Util.message(sender, "&cYou are not currently reviewing an order!");
 					return true;
 				}
 				if (args.length < 3) {
 					Po8Util.message(sender, "Invalid arguments for /po8 review deny");
 					return true;
 				}
 				String reason = Po8Util.combine(args, 2, 0);
 				Po8Order order = Po8.playerMap.get(sender.getName()).reviewOrder;
 				Po8.playerMap.get(sender.getName()).reviewOrder = null;
 				if (Bukkit.getPlayerExact(order.owner) != null) {
 					if (order.type == Po8.BUY)
 						Po8Util.message(Bukkit.getPlayerExact(order.owner), "&cYour Po8 Buy order has been denied. Your Po8 has been returned to you.");
 					if (order.type == Po8.SELL)
 						Po8Util.message(Bukkit.getPlayerExact(order.owner), "&cYour Po8 Sell order has been denied. Your items are in your Po8 Buy Chest");
 					Po8Util.message(Bukkit.getPlayerExact(order.owner), "&cReason: &f" + reason);
 				}
 				if (order.type == Po8.BUY) {
 					Po8.playerMap.get(order.owner).balance += order.value;
 					for (ItemStack stack : order.getItems())
 						Po8.stockAdd(Po8Util.stockKey(stack.getData()), stack.getAmount());
 					Po8Util.log(order.owner, "Po8", order.value, "BUY_DENIED", p.getName() + " | " + reason);
 				}
 				if (order.type == Po8.SELL) {
 					// transfer items to chest
 					ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
 					Collections.addAll(stacks, order.getItems());
 					System.out.println(order.getItems().length + " " + stacks.size());
					ItemStack[] buyInv = Po8.playerMap.get(sender.getName()).getInventory(Po8.BUY);
 					for (int i = 0; i < buyInv.length; i++) {
 						if (stacks.size() <= 0)
 							break;
 						if (buyInv[i] == null)
 							buyInv[i] = stacks.remove(0);
 					}
 					Po8.playerMap.get(order.owner).setInventory(Po8.BUY, buyInv);
 					for (int i = 0; i < stacks.size(); i++)
 						Po8.playerMap.get(order.owner).extendedInv.add(new CardboardBox(stacks.get(i)));
 					Po8Util.log("Po8", order.owner, order.value, "SELL_DENIED", p.getName() + " | " + reason);
 				}
 				Po8Util.message(sender, "&dOrder denied.");
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("accept")) {
 				if (Po8.playerMap.get(sender.getName()).reviewOrder == null) {
 					Po8Util.message(sender, "&cYou are not currently reviewing an order!");
 					return true;
 				}
 				Po8Order order = Po8.playerMap.get(sender.getName()).reviewOrder;
 				Po8.playerMap.get(sender.getName()).reviewOrder = null;
 				double commission = Po8Util.round2((order.value * (Po8.commission / 100.0)));
 				if (!sender.getName().toLowerCase().equals(order.owner.toLowerCase())) {
 					Po8Util.message(sender, "&a" + Po8Util.round2(commission) + "&f Po8 of commission has been sent to your Po8 account");
 					Po8.playerMap.get(sender.getName()).balance += commission;
 				}
 				if (Bukkit.getPlayerExact(order.owner) != null) {
 					if (order.type == Po8.BUY)
 						Po8Util.message(Bukkit.getPlayerExact(order.owner), "There are new items in your Po8 Buy Chest!");
 					if (order.type == Po8.SELL)
 						Po8Util.message(Bukkit.getPlayerExact(order.owner), "You have successfully sold &a" + Po8Util.round2(order.value) + " &fPo8 worth of items!");
 				}
 				if (order.type == Po8.BUY) {
 					// transfer items to chest
 					ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
 					Collections.addAll(stacks, order.getItems());
 					System.out.println(order.getItems().length + " " + stacks.size());
					ItemStack[] buyInv = Po8.playerMap.get(sender.getName()).getInventory(Po8.BUY);
 					for (int i = 0; i < buyInv.length; i++) {
 						if (stacks.size() <= 0)
 							break;
 						if (buyInv[i] == null)
 							buyInv[i] = stacks.remove(0);
 					}
 					Po8.playerMap.get(order.owner).setInventory(Po8.BUY, buyInv);
 					for (int i = 0; i < stacks.size(); i++)
 						Po8.playerMap.get(order.owner).extendedInv.add(new CardboardBox(stacks.get(i)));
 					Po8Util.log(order.owner, "Po8", order.value, "BUY", p.getName());
 				}
 				if (order.type == Po8.SELL) {
 					// add stuff to stock
 					Po8.playerMap.get(order.owner).balance += order.value;
 					for (ItemStack stack : order.getItems())
 						Po8.stockAdd(Po8Util.stockKey(stack.getData()), stack.getAmount());
 					Po8Util.log("Po8", order.owner, order.value, "SELL", p.getName());
 				}
 
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("status")) {
 				Po8Util.message(sender, "&dThere are&5 " + Po8.orderList.size() + " &dnew Po8 orders to be reviewed.");
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("skip")) {
 				if (Po8.playerMap.get(sender.getName()).reviewOrder != null) {
 					Po8.orderList.add(Po8.playerMap.get(sender.getName()).reviewOrder);
 					Po8Util.message(sender, "&dSkipping current review order");
 					Po8.playerMap.get(sender.getName()).reviewOrder = null;
 					return true;
 				}
 				Po8Util.message(sender, "&cYou are not currently reviewing an order!");
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("next")) {
 				if (Po8.playerMap.get(sender.getName()).reviewOrder != null) {
 					Po8Util.message(sender, "&cYou must accept, deny, or skip your current review!");
 					return true;
 				}
 				if (Po8.orderList.size() <= 0) {
 					Po8Util.message(sender, "&dThere are&5 0 &dnew Po8 orders to be reviewed.");
 					return true;
 				}
 				Po8.playerMap.get(sender.getName()).reviewOrder = Po8.orderList.remove(0);
 				Po8Order review = Po8.playerMap.get(sender.getName()).reviewOrder;
 				Inventory inv = Bukkit.getServer().createInventory(null, 54, review.owner + "'s " + (review.type == Po8.SELL ? "Sell Order" : "Buy Order"));
 				inv.setContents(review.getItems());
 				p.openInventory(inv);
 				Po8.playerMap.get(sender.getName()).isReviewingOrder = true;
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("show")) {
 				if (Po8.playerMap.get(sender.getName()).reviewOrder == null) {
 					Po8Util.message(sender, "&cYou are not currently reviewing any order!");
 					return true;
 				}
 				Po8Order review = Po8.playerMap.get(sender.getName()).reviewOrder;
 				Inventory inv = Bukkit.getServer().createInventory(null, 54, review.owner + "'s " + (review.type == Po8.SELL ? "Sell Order" : "Buy Order"));
 				inv.setContents(review.getItems());
 				p.openInventory(inv);
 				Po8.playerMap.get(sender.getName()).isReviewingOrder = true;
 				return true;
 			}
 			if (args[1].equalsIgnoreCase("togglenotify")) {
 				Po8.playerMap.get(sender.getName()).notify = !Po8.playerMap.get(sender.getName()).notify;
 				Po8Util.message(sender, "Toggled. New setting: " + Po8.playerMap.get(sender.getName()).notify);
 				return true;
 			}
 			sendHelp(sender, 3);
 			return true;
 		}
 		sendHelp(sender, 1);
 		return true;
 	}
 
 	public void sendHelp(CommandSender to, int page) {
 		Po8Util.message(to, "&a---------- &2Po8 Help &a----------");
 		page = Math.min(3, Math.max(1, page));
 
 		if (page == 1) {
 			Po8Util.message(to, " &e/po8 help [page] - &7Shows help");
 			if (to.hasPermission("po8.balance"))
 				Po8Util.message(to, " &e/po8 balance - &7Checks your Po8 balance");
 			if (to.hasPermission("po8.transfer"))
 				Po8Util.message(to, " &e/po8 transfer [username] [amount] - &7Transfers some Po8 to another player");
 			if (to.hasPermission("po8.info"))
 				Po8Util.message(to, " &e/po8 info [item name|held|id:data] [amount] - &7Checks the buy/sell price and stock for a given item. Amount is optional");
 			if (to.hasPermission("po8.sell"))
 				Po8Util.message(to, " &e/po8 sell - &7Sells the contents of your sell chest");
 			if (to.hasPermission("po8.value"))
 				Po8Util.message(to, " &e/po8 value - &7Values the contents of your sell chest");
 			Po8Util.message(to, "&dPo8 Help: Page (1/3)");
 		}
 		if (page == 2) {
 			if (to.hasPermission("po8.order")) {
 				Po8Util.message(to, " &e/po8 order new - &7Creates a new order, discarding the current one");
 				Po8Util.message(to, " &e/po8 order add [item name|held|id:data] [amount] - &7Adds the specified item to the current order");
 				Po8Util.message(to, " &e/po8 order remove [item name|held|id:data] [amount] - &7Removes the specified item the current order");
 				Po8Util.message(to, " &e/po8 order price - &7Checks the price of current order");
 				Po8Util.message(to, " &e/po8 order list - &7Lists the items in your current order");
 				Po8Util.message(to, " &e/po8 order submit - &7Submits the order");
 			}
 			if (to.hasPermission("po8.examine")) {
 				Po8Util.message(to, " &e/po8 examine [player] [type] - &7Views the given Po8 chest of a player. Must be exact.");
 				Po8Util.message(to, " -------- &7Types: 'buy' or 'sell' ");
 			}
 			Po8Util.message(to, "&dPo8 Help: Page (2/3)");
 		}
 		if (page == 3) {
 			if (to.hasPermission("po8.review")) {
 				Po8Util.message(to, " &e/po8 review status - &7Checks how many orders are in the review queue");
 				Po8Util.message(to, " &e/po8 review next - &7Shows you the next review in the queue.");
 				Po8Util.message(to, " &e/po8 review skip - &7Skips the current review order");
 				Po8Util.message(to, " &e/po8 review show - &7Shows you the current order to review if you closed it.");
 				Po8Util.message(to, " &e/po8 review accept - &7Accepts the current review");
 				Po8Util.message(to, " &e/po8 review deny [reason] - &7Denies the current review");
 				Po8Util.message(to, " &e/po8 review togglenotify - &7Toggles notifying on every Po8 Order");
 			}
 			if (to.hasPermission("po8.spawnchest")) {
 				Po8Util.message(to, " &e/po8 spawnchest [type] - &7Turns the block you're standing on into a special chest");
 				Po8Util.message(to, "  -------- &7Types: 'buy' or 'sell' ");
 			}
 			if (to.hasPermission("po8.grant")) {
 				Po8Util.message(to, " &e/po8 grant [username] [amount] - &7Grants some Po8 to a player");
 			}
 			Po8Util.message(to, "&dPo8 Help: Page (3/3)");
 		}
 	}
 }
