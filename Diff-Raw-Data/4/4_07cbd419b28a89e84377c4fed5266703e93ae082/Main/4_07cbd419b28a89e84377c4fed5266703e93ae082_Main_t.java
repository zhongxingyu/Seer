 package qs.swornshop;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayDeque;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import qs.swornshop.notification.BuyNotification;
 import qs.swornshop.notification.Claimable;
 import qs.swornshop.notification.Notification;
 import qs.swornshop.notification.Request;
 import qs.swornshop.notification.SellRequest;
 import qs.swornshop.serialization.BlockLocation;
 import qs.swornshop.serialization.State;
 
 public class Main extends JavaPlugin implements Listener {
 	
 	// TODO: Timed notifications
 	
 	/**
 	 * The block ID for a signpost
 	 */
 	private static final int SIGN = 63;
 	
 	/**
 	 * The distance from the sign in any direction which the player can go before they leave the shop
 	 */
 	private static final int SHOP_RANGE = 4*4;
 	
 	/**
 	 * A single instance of Main for external access
 	 */
 	public static Main instance;
 	
 	/**
 	 * The Vault economy
 	 */
 	public static Economy econ;
 
 	/**
 	 * A lookup table for aliases.
 	 * Aliases are stored as <code>alias =&gt; (ID &lt;&lt; 16) | (damageValue)</code>
 	 */
 	protected static HashMap<String, Long> aliases = new HashMap<String, Long>();
 	/**
 	 * A lookup table for item names.
 	 * Item names are stored as <code>(ID &lt;&lt; 16) | (damageValue) =&gt; itemName</code>
 	 */
 	protected static HashMap<Long, String> itemNames = new HashMap<Long, String>();
 	
 	/**
 	 * A map of shops, accessed by their location in the world
 	 */
 	protected HashMap<Location, Shop> shops = new HashMap<Location, Shop>();
 	/**
 	 * A map containing each player's currently selected shop and other selection data
 	 */
 	protected HashMap<Player, ShopSelection> selectedShops = new HashMap<Player, ShopSelection>();
 	/**
 	 * A map containing each player's notifications
 	 */
 	protected HashMap<String, ArrayDeque<Notification>> pending = new HashMap<String, ArrayDeque<Notification>>();
 	/**
 	 * The plugin logger
 	 */
 	protected Logger log;
 	
 	public Main() {}
 
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		instance = this;
 		log = this.getLogger();
 		loadItemNames();
 		loadAliases();
 		if (!economySetup()) {
 			log.warning("Could not set up server economy! Is Vault installed?");
 			getPluginLoader().disablePlugin(this);
 			return;
 		}
 		State state = reloadAll();
 		if (state != null) {
 			shops = state.getShops();
 			pending = state.pending;
 		} else{
 			log.info("Shops could not be loaded. If this is the first launch of the plugin, " +
 					"this is expected. If not, your data files may be corrupt. Try replacing " +
 					"state.dat with your backup (state.dat_old)");
 		}
 		
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				saveAll();
 			}
 		}, 6000L, 36000L);
 	}
 	
 	@Override
 	public void onDisable() {
 		saveAll();
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("shop")) {
 			String action = args.length == 0 ? "" : args[0];
 			
 			if (action.equalsIgnoreCase("save")) {
 				if (!(sender instanceof Player) || sender.hasPermission("swornshop.admin"))
 					saveAll();
 				return true;
 				
 			} else if (action.equalsIgnoreCase("backup")) {
 				if (!(sender instanceof Player) || sender.hasPermission("swornshop.admin"))
 					backup();
 				return true;
 				
 			}
 			if (!(sender instanceof Player)) {
 				if(action.equalsIgnoreCase("removeallnotifications")){
 					pending.clear();
 					return true;
 				}
 				sendError(sender, "/shop commands can only be used by a player");
 				return true;
 			}
 			
 			Player pl = (Player) sender;
 			ShopSelection selection = selectedShops.get(pl);
 			if (args.length == 0) {
 				Help.showHelp(pl, selection);
 				return true;
 			}
 			if (action.equalsIgnoreCase("create") || 
 					action.equalsIgnoreCase("mk")) {
 				if (args.length < 2) {
 					sendError(pl, Help.create.toUsageString());
 					return true;
 				}
 				if (!sender.hasPermission("swornshop.admin")) {
 					sendError(pl, "You cannot create shops");
 					return true;
 				}
 				
 				Location loc = pl.getLocation();
 				Location locUnder = pl.getLocation();
 				locUnder.setY(locUnder.getY() - 1);
 				Block b = loc.getBlock();
 				Block blockUnder = loc.getBlock();
 				if (blockUnder.getTypeId() == 0 ||
 						blockUnder.getTypeId() == 46){
 					sendError(pl, "This is not a valid block to place a shop on!");
 					return true;
 				}
 				byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);
 				b.setTypeIdAndData(SIGN, angle, false);
 
 				Sign sign = (Sign) b.getState();
 				String owner = args[1];
 				sign.setLine(0, "");
 				sign.setLine(1, (owner.length() < 13 ? owner : owner.substring(0, 12) + '§') + "'s");
 				sign.setLine(2, "shop");
 				sign.setLine(3, "");
 				sign.update();
 				
 				Shop shop = new Shop();
 				shop.owner = owner;
 				shop.location = b.getLocation();
 				shop.isInfinite = args.length > 2 && (args[2].equalsIgnoreCase("yes") || args[2].equalsIgnoreCase("true"));
 				shops.put(shop.location, shop);
 				
 			} else if (action.equalsIgnoreCase("delete") || 
 					action.equalsIgnoreCase("del")) {
 				if (selection == null) {
 					sendError(pl, "You must select a shop");
 					return true;
 				}
 				if (!pl.hasPermission("swornshop.admin") && !selection.isOwner) {
 					sendError(pl, "You cannot remove this shop");
 					return true;
 				}
 				Location loc = selection.shop.location;
 				Block b = loc.getBlock();
 				Sign sign = (Sign) b.getState();
 				sign.setLine(0, "This shop is");
 				sign.setLine(1, "out of");
 				sign.setLine(2, "business.");
 				sign.setLine(3, "Sorry! D:");
 				sign.update();
 				shops.remove(loc);
 				
 				pl.sendMessage("§B" + selection.shop.owner + "§F's shop has been removed");
 				
 			} else if (action.equalsIgnoreCase("add") ||
 					action.equalsIgnoreCase("+") ||
 					action.equalsIgnoreCase("ad")) {
 				if (args.length < 2) {
 					sendError(pl, Help.add.toUsageString());
 					return true;
 				}
 				if (selection == null) {
 					sendError(pl, "You must select a shop");
 					return true;
 				}
 				if (!selection.isOwner && !pl.hasPermission("swornshop.admin")) {
 					sendError(pl, "You cannot add items to this shop");
 					return true;
 				}
 				
 				float retailAmount, refundAmount;
 				try {
 					retailAmount = Math.round(100f * Float.parseFloat(args[1])) / 100f;
 				} catch (NumberFormatException e) {
 					sendError(pl, "Invalid buy price");
 					sendError(pl, Help.add.toUsageString());
 					return true;
 				}
 				try {
 					refundAmount = args.length > 2 ? Math.round(100f * Float.parseFloat(args[2])) / 100f : -1;
 				} catch (NumberFormatException e) {
 					sendError(pl, "Invalid sell price");
 					sendError(pl, Help.add.toUsageString());
 					return true;
 				}
 				ItemStack stack = pl.getItemInHand();
 				if (stack == null || stack.getTypeId() == 0) {
 					sendError(pl, "You must be holding the item you wisth to add to this shop");
 					return true;
 				}
 				
 				if (selection.shop.containsItem(stack)) {
 					sendError(pl, "That item has already been added to this shop");
 					sendError(pl, "Use /shop restock to restock");
 					return true;
 				}
 				if (selection.shop.isInfinite)
 					stack.setAmount(-8);
 				ShopEntry newEntry = new ShopEntry();
 				newEntry.setItem(stack);
 				newEntry.retailPrice = retailAmount;
 				newEntry.refundPrice = refundAmount;
 				selection.shop.addEntry(newEntry);
 				
 				pl.setItemInHand(null);
 				
 			} else if ((action.equalsIgnoreCase("restock") ||
 					action.equalsIgnoreCase("r"))) {
 				if (selection == null) {
 					sendError(pl, "You must select a shop");
 					return true;
 				}
 				if (!selection.isOwner && !pl.hasPermission("swornshop.admin") && selection.shop.isInfinite) {
 					sendError(pl, "You cannot restock this shop");
 					return true;
 				}
 				ItemStack stack = pl.getItemInHand();
 				
 				
 				if (stack == null || stack.getTypeId() == 0) {
 					sendError(pl, "You must be holding the item you wish to add to this shop");
 					return true;
 				}
 				ShopEntry entry = selection.shop.findEntry(stack);
 				if (entry == null) {
 					sendError(pl, "That item has not been added to this shop");
 					sendError(pl, "Use /shop add to add a new item");
 					return true;
 				}
 				entry.setAmount(entry.item.getAmount() + stack.getAmount());
 				pl.setItemInHand(null);
 				
 			} else if (action.equalsIgnoreCase("set")) {
 				if (!selection.isOwner && !pl.hasPermission("swornshop.admin")) {
 					sendError(pl, "You cannot change this shop's prices");
 					return true;
 				}
 				if (args.length < 3) {
 					sendError(pl, Help.set.toUsageString());
 					return true;
 				}
 				long item;
 				int id;
 				short damage;
 				
 				Shop shop = selection.shop;
 				ShopEntry entry;
 				try {
 					int index = Integer.parseInt(args[1]);
 					entry = shop.getEntryAt(index - 1);
 				} catch (NumberFormatException e) {
 					try{
 						item = getItemFromAlias(args[1]);
 						id = (int) (item >> 16);
 						damage = (short) (item & 0xFFFF);
 						entry = shop.findEntry(id, damage);
 					} catch (NullPointerException ex){
 						sendError(pl, "That is not a valid alias for an item!");
 						return true;
 					}
 				} catch (IndexOutOfBoundsException e) {
 					sendError(pl, "That item is not in this shop");
 					return true;
 				}
 				
 				if (entry == null) {
 					sendError(pl, "That item is not in this shop");
 					return true;
 				}
 				
 				float retailAmount, refundAmount;
 				try {
 					retailAmount = Math.round(100f * Float.parseFloat(args[2])) / 100f;
 				} catch (NumberFormatException e) {
 					sendError(pl, "Invalid buy price");
 					sendError(pl, Help.set.toUsageString());
 					return true;
 				}
 				try {
 					refundAmount = args.length > 3 ? Math.round(100f * Float.parseFloat(args[3])) / 100f : -1;
 				} catch (NumberFormatException e) {
 					sendError(pl, "Invalid sell price");
 					sendError(pl, Help.set.toUsageString());
 					return true;
 				}
 				
 				entry.retailPrice = retailAmount;
 				entry.refundPrice = refundAmount;
 				
 			} else if (action.equalsIgnoreCase("buy") ||
 					action.equalsIgnoreCase("b")) {
 				if (args.length < 2) {
 					sendError(pl, Help.buy.toUsageString());
 					return true;
 				}
 				if (selection == null) {
 					sendError(pl, "You must select a shop");
 					return true;
 				}
 				if (selection.isOwner && !pl.hasPermission("swornshop.self")) {
 					sendError(pl, "You cannot buy items from this shop");
 					return true;
 				}
 				
 				int amount;
 				if (args.length < 3) {
 					amount = 1;
 				} else {
 					try {
 						amount = Integer.parseInt(args[2]);
 					} catch (NumberFormatException e) {
 						sendError(pl, Help.buy.toUsageString());
 						return true;
 					}
 				}
 				if (amount <= 0) {
 					sendError(pl, "You must buy a positive number of this item");
 					return true;
 				}
 
 				Shop shop = selection.shop;
 				ShopEntry entry;
 				try {
 					int index = Integer.parseInt(args[1]);
 					entry = shop.getEntryAt(index - 1);
 				} catch (NumberFormatException e) {
 					try{
 						long item = getItemFromAlias(args[1]);
 						int id = (int) (item >> 16);
 						short damage = (short) (item & 0xFFFF);
 						entry = shop.findEntry(id, damage);
 					} catch (NullPointerException ex){
 						sendError(pl, "That is not a valid alias for an item!");
 						return true;
 					}
 				} catch (IndexOutOfBoundsException e) {
 					sendError(pl, "That item is not in this shop");
 					return true;
 				}
 				if (entry == null) {
 					sendError(pl, "That item is not in this shop");
 					return true;
 				}
 				if (entry.item.getAmount() < amount && !shop.isInfinite) {
 					sendError(pl, "There are not enough of that item in the shop");
 					return true;
 				}
 				
 				String itemName = getItemName(entry.item);
 				if (!econ.has(pl.getName(), amount * entry.retailPrice)) {
 					sendError(pl, "You do not have sufficient funds");
 					return true;
 				}
 				ItemStack purchased = entry.item.clone();
 				purchased.setAmount(amount);
 				
 				HashMap<Integer, ItemStack> overflow =  pl.getInventory().addItem(purchased);
 				int refunded = 0;
 				if (overflow.size() > 0) {
 					refunded = overflow.get(0).getAmount();
 					if (overflow.size() == amount) {
 						sendError(pl, "You do not have any room in your inventory");
 						return true;
 					}
 					sender.sendMessage(String.format(
 							"Only §B%d %s§F fit in your inventory. You were charged §B$%.2f§F.",
 							amount - refunded, itemName, (amount - refunded) * entry.retailPrice));
 				} else {
 					sender.sendMessage(String.format(
 							"You bought §B%d %s§F for §B$%.2f§F.",
 							amount, itemName, amount * entry.retailPrice));
 				}
 				econ.withdrawPlayer(pl.getName(), (amount - refunded) * entry.retailPrice);
 				if (!shop.isInfinite) {
 					entry.item.setAmount(entry.item.getAmount() - (amount - refunded));
 				}
 				econ.depositPlayer(shop.owner, (amount - refunded) * entry.retailPrice);
 				
 				ShopEntry e = new ShopEntry();
 				e.setItem(purchased);
 				e.retailPrice = entry.retailPrice;
 				sendNotification(shop.owner, new BuyNotification(shop, e, pl.getName()));
 				
 			} else if (action.equalsIgnoreCase("sell") ||
 					action.equalsIgnoreCase("s")) {
 				if (selection == null) {
 					sendError(pl, "You must select a shop");
 					return true;
 				}
 				if (selection.isOwner && !pl.hasPermission("swornshop.self")) {
 					sendError(pl, "You cannot sell items to your own shop");
 					sendError(pl, "To add items, use /shop add");
 					return true;
 				}
 				
 				ItemStack itemsToSell = pl.getItemInHand();
 				if (itemsToSell == null || itemsToSell.getTypeId() == 0) {
 					sendError(pl, "You must be holding the item you wish to sell");
 					return true;
 				}
 				
 				Shop shop = selection.shop;
 				ShopEntry entry = shop.findEntry(itemsToSell.getTypeId(), itemsToSell.getDurability());
 				if (entry == null || entry.refundPrice < 0) {
 					sendError(pl, "You cannot sell that item");
 					return true;
 				}
 				
 				pl.setItemInHand(null);
 				
 				String name = getItemName(itemsToSell);
 				pl.sendMessage(String.format(
 						"Your request to sell §B%d %s§F for §B$%.2f§F has been sent",
 						itemsToSell.getAmount(), name, entry.refundPrice * itemsToSell.getAmount()));
 				pl.sendMessage("§7You will be notified when this offer is accepted or rejected");
 				
 				ShopEntry req = new ShopEntry();
 				req.setItem(itemsToSell);
 				req.refundPrice = entry.refundPrice;
 				SellRequest request = new SellRequest(shop, req, pl.getName());
 				sendNotification(shop.owner, request);
 				
 			} else if (action.equalsIgnoreCase("pending") ||
 					action.equalsIgnoreCase("p") ||
 					action.equalsIgnoreCase("notifications") ||
 					action.equalsIgnoreCase("n")) {
 				showNotification(pl);
 				
 			} else if (action.equalsIgnoreCase("sign")) {
 				if (selection == null) {
 					sendError(pl, "You must select a shop");
 					return true;
 				}
 				if (!pl.hasPermission("swornshop.admin") && !selection.isOwner) {
 					sendError(pl, "You cannot change this sign's text");
 					return true;
 				}
 				Block b = selection.shop.location.getBlock();
 				if (b.getTypeId() != SIGN) {
 					log.warning(String.format("%s's shop is missing a sign", selection.shop.owner));
 					return true;
 				}
 				
 				Sign sign = (Sign) b.getState();
 				StringBuilder sb = new StringBuilder();
 				for (int i = 1; i < args.length; ++i) {
 					sb.append(args[i]);
 					sb.append(" ");
 				}
 				int len = sb.length();
 				if (len > 0) {
 					sb.deleteCharAt(sb.length() - 1);
 				}
 				if (len > 60) {
 					sendError(pl, "That sign text is too long");
 					return true;
 				}
 				String[] lines = sb.toString().split("\\|");
 				for (int i = 0; i < lines.length; ++i)
 					if (lines[i].length() > 15) {
 						sendError(pl, String.format("Line %d is too long. Lines may only be 15 characters", i + 1));
 						return true;
 					}
 				if (lines.length < 3) {
 					sign.setLine(0, "");
 					sign.setLine(1, lines[0]);
 					sign.setLine(2, lines.length > 1 ? lines[1] : "");
 					sign.setLine(3, "");
 				} else {
 					sign.setLine(0, lines[0]);
 					sign.setLine(1, lines.length > 1 ? lines[1] : "");
 					sign.setLine(2, lines.length > 2 ? lines[2] : "");
 					sign.setLine(3, lines.length > 3 ? lines[3] : "");
 				}
 				sign.update();
 				
 			} else if (action.equalsIgnoreCase("accept") ||
 					action.equalsIgnoreCase("yes") ||
 					action.equalsIgnoreCase("a") ||
 					action.equalsIgnoreCase("claim") ||
 					action.equalsIgnoreCase("c")) {
 				ArrayDeque<Notification> notifications = getNotifications(pl);
 				if (notifications.isEmpty()) {
 					sendError(pl, "You have no notifications");
 					return true;
 				}
 				Notification n = notifications.getFirst();
 				if (n instanceof Request) {
 					Request r = (Request) n;
 					if (r.accept(pl))
 						notifications.removeFirst();
 				} else if (n instanceof Claimable) {
 					Claimable c = (Claimable) n;
 					if (c.claim(pl))
 						notifications.removeFirst();
 				}
 
 				showNotification(pl);
 				
 			} else if (action.equalsIgnoreCase("reject") ||
 					action.equalsIgnoreCase("no")) {
 				ArrayDeque<Notification> notifications = getNotifications(pl);
 				if (notifications.isEmpty()) {
 					sendError(pl, "You have no notifications");
 					return true;
 				}
 				Notification n = notifications.getFirst();
 				if (n instanceof Request) {
 					Request r = (Request) n;
 					if (r.reject(pl))
 						notifications.removeFirst();
 				}
 				
 				showNotification(pl);
 				
 			} else if (action.equalsIgnoreCase("skip") ||
 					action.equalsIgnoreCase("sk")) {
 				ArrayDeque<Notification> notifications = getNotifications(pl);
 				if (notifications.isEmpty()) {
 					sendError(pl, "You have no notifications");
 					return true;
 				}
 				notifications.add(notifications.removeFirst());
 				showNotification(pl);
 				
 			} else if (action.equalsIgnoreCase("lookup")) {
 				if (args.length < 2) {
 					sendError(pl, Help.lookup.toUsageString());
 					return true;
 				}
 				Long alias = getItemFromAlias(args[1]);
 				if (alias == null) {
 					sendError(pl, "Alias not found");
 					return true;
 				}
 				int id = (int) (alias >> 16);
 				int damage = (int) (alias & 0xFFFF);
 				sender.sendMessage(String.format("%s is an alias for %d:%d", args[1], id, damage));
 				
 			} else if ((action.equalsIgnoreCase("help") ||
 					action.equalsIgnoreCase("h")) &&
 					args.length > 1) {
 				String helpCmd = args[1];
 				CommandHelp h = Help.getHelpFor(helpCmd);
 				if (h == null) {
 					sendError(pl, String.format("'/shop %s' is not an action", helpCmd));
 					return true;
 				}
 				pl.sendMessage(h.toHelpString());
 				
 			} else {
 				Help.showHelp(pl, selection);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Block b = event.getClickedBlock();
 		if (b == null || b.getTypeId() != SIGN){
 			Location loc = b.getLocation();
 			loc.setY(loc.getY() + 1);
 			if(loc.getBlock().getTypeId() == SIGN){
 				if(shops.containsKey(loc)){
 					event.setCancelled(true);
 					return;
 				}
 			}
 			return;
 		}
 		
 		Shop shop = shops.get(b.getLocation());
 		Player pl = event.getPlayer();
 		boolean isOwner = shop.owner.equals(pl.getName());
 		
 		ShopSelection selection = selectedShops.get(pl);
 		if (selection == null) {
 			selection = new ShopSelection();
 			selectedShops.put(pl, selection);
 		}
 		if (selection.shop == shop) {
 			int pages = shop.getPages();
 			if (pages == 0) {
 				selection.page = 0;
 			} else {
 				int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? -1 : 1;
 				selection.page = (((selection.page + delta) % pages) + pages) % pages;
 			}
 			pl.sendMessage("");
 			pl.sendMessage("");
 		} else {
 			selection.isOwner = isOwner;
 			selection.shop = shop;
 			selection.page = 0;
 			pl.sendMessage(new String[] {
 				isOwner ? "§FWelcome to your shop." :
 						String.format("§FWelcome to §B%s§F's shop.", shop.owner),
 				"§7For help with shops, type §3/shop help§7."
 			});
 		}
 		
 		showListing(pl, selection);
 		
 		event.setCancelled(true);
 		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
 			b.getState().update();
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onExplosion(EntityExplodeEvent event){
 		for (Block b : event.blockList()) {
 			if (b.getTypeId() != SIGN) continue;
 			
 			Location loc = b.getLocation();
 			if (shops.containsKey(loc)) {
 				event.setCancelled(true);
 				return;
 			}
 			loc.setY(loc.getY() + 1);
 			if (shops.containsKey(loc)) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerJoin(PlayerJoinEvent event){
 		ArrayDeque<Notification> p = getNotifications(event.getPlayer());
 		if (!p.isEmpty())
 			event.getPlayer().sendMessage("You have shop notifications. Use §B/shop pending§F to view them");
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerMove(PlayerMoveEvent event){
 		Player pl = event.getPlayer();
 		ShopSelection s = selectedShops.get(pl);
 		if (s != null) {
 			Location shopLoc = s.shop.location;
 			Location pLoc = event.getTo();
 			if (shopLoc.getWorld() != pl.getWorld() || shopLoc.distanceSquared(pLoc) > SHOP_RANGE) {
 				pl.sendMessage(s.isOwner ? "§7[Left your shop]" : 
 					String.format("§7[Left §3%s§7's shop] §FThank you, and come again!", s.shop.owner));
 				selectedShops.remove(event.getPlayer());
 			}
 		}
 	}
 	
 	
 
 	/**
 	 * Attempts to find an item which matches the given item name (alias).
 	 * @param alias the item name
 	 * @return a Long which contains the item ID and damage value as follows: (id << 16) | (damage)
 	 */
 	public Long getItemFromAlias(String alias) {
 		alias = alias.toLowerCase();
 		return aliases.get(alias);
 	}
 
 	/**
 	 * Gets the name of an item.
 	 * @param item an item stack
 	 * @return the item's name
 	 */
 	public String getItemName(ItemStack item) {
 		return getItemName(item.getTypeId(), item.getDurability());
 	}
 
 	/**
 	 * Gets the name of an item.
 	 * @param item an item stack
 	 * @return the item's name
 	 */
 	public String getItemName(ShopEntry entry) {
 		return getItemName(entry.itemID, entry.itemDamage);
 	}
 
 	/**
 	 * Gets the name of an item.
 	 * @param id the item's id
 	 * @param damage the item's damage value (durability)
 	 * @return the item's name
 	 */
 	public String getItemName(int id, int damage) {
 		String name = itemNames.get((long) id << 16 | damage);
 		if (name == null) {
 			name = itemNames.get((long) id << 16);
 			if (name == null) return String.format("%d:%d", id, damage);
 		}
 		return name;
 	}
 	
 	/**
 	 * Gets a list of notifications for a player.
 	 * @param pl the player
 	 * @return the player's notifications
 	 */
 	public ArrayDeque<Notification> getNotifications(Player pl) {
 		return getNotifications(pl.getName());
 	}
 	
 	/**
 	 * Gets a list of notifications for a player.
 	 * @param player the player
 	 * @return the player's notifications
 	 */
 	public ArrayDeque<Notification> getNotifications(String player) {
 		ArrayDeque<Notification> n = pending.get(player);
 		if (n == null) {
 			n = new ArrayDeque<Notification>();
 			pending.put(player, n);
 		}
 		return n;
 	}
 	
 	/**
 	 * Shows a player his/her most recent notification.
 	 * Also shows the notification count.
 	 * @param pl the player
 	 */
 	public void showNotification(Player pl) {
 		showNotification(pl, true);
 	}
 
 	/**
 	 * Shows a player his/her most recent notification.
 	 * @param pl the player
 	 * @param showCount whether the notification count should be shown as well
 	 */
 	public void showNotification(Player pl, boolean showCount) {
 		ArrayDeque<Notification> notifications = getNotifications(pl);
 		if (notifications.isEmpty()) {
 			if (showCount)
 				pl.sendMessage("§7You have no notifications");
 			return;
 		}
 		if (showCount) {
 			int size = notifications.size();
 			pl.sendMessage(size == 1 ? "§7You have §31§7 notification" : String.format("§7You have §3%d§7 notifications", size));
 		}
 		
 		Notification n = notifications.getFirst();
 		pl.sendMessage(n.getMessage(pl));
 		if (n instanceof Request)
 			pl.sendMessage("§7Use §3/shop accept§7 or §3/shop reject§7 to manage this request");
 		else if (n instanceof Claimable)
 			pl.sendMessage("§7Use §3/shop claim§7 to claim and remove this notification");
 		else notifications.removeFirst();
 	}
 
 	/**
 	 * Sends a notification to a player.
 	 * @param pl the player
 	 * @param n the notification
 	 */
 	public void sendNotification(Player pl, Notification n) {
 		sendNotification(pl.getName(), n);
 	}
 
 	/**
 	 * Sends a notification to a player.
 	 * @param player the player
 	 * @param n the notification
 	 */
 	public void sendNotification(String player, Notification n) {
 		ArrayDeque<Notification> ns = getNotifications(player);
 		ns.add(n);
 		Player pl = getServer().getPlayer(player);
 		if (pl != null && pl.isOnline())
 			showNotification(pl, false);
 	}
 	
 	/**
 	 * Checks whether an item stack will fit in a player's inventory.
 	 * @param pl the player
 	 * @param item the item
 	 * @return whether the item will fit
 	 */
 	public static boolean inventoryFitsItem(Player pl, ItemStack item) {
 		int quantity = item.getAmount(),
 			id = item.getTypeId(),
 			damage = item.getDurability(),
 			max = item.getMaxStackSize();
 		Inventory inv = pl.getInventory();
 		ItemStack[] contents = inv.getContents();
 		ItemStack s;
 		if (max == -1) max = inv.getMaxStackSize();
 		for (int i = 0; i < contents.length; ++i) {
 			if ((s = contents[i]) == null || s.getTypeId() == 0) {
 				quantity -= max;
 				if (quantity <= 0) return true;
 				continue;
 			}
 			if (s.getTypeId() == id && s.getDurability() == damage) {
 				quantity -= max - s.getAmount();
 				if (quantity <= 0) return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Informs a player of an error.
 	 * @param sender the player
 	 * @param message the error message
 	 */
 	public static void sendError(CommandSender sender, String message) {
 		sender.sendMessage("§C" + message);
 	}
 	
 	/**
 	 * Show a page of a shop's inventory listing.
 	 * @param sender the player to which the listing is shown
 	 * @param selection the player's shop selection
 	 */
 	public static void showListing(CommandSender sender, ShopSelection selection) {
 		Shop shop = selection.shop;
 		int pages = shop.getPages();
 		if (pages == 0) {
 			sender.sendMessage(CommandHelp.header("Empty"));
 			sender.sendMessage("");
 			sender.sendMessage("This shop has no items");
 			int stop = Shop.ITEMS_PER_PAGE - 2;
 			if (selection.isOwner) {
 				sender.sendMessage("Use /shop add to add items");
 				--stop;
 			}
 			for (int i = 0; i < stop; ++i) {
 				sender.sendMessage("");
 			}
 			return;
 		}
 		sender.sendMessage(CommandHelp.header(String.format("Page %d/%d", selection.page + 1, pages)));
 		int i = selection.page * Shop.ITEMS_PER_PAGE,
 			stop = (selection.page + 1) * Shop.ITEMS_PER_PAGE,
 			max = Math.min(stop, shop.getInventorySize());
 		for (; i < max; ++i)
 			sender.sendMessage(shop.getEntryAt(i).toString(i + 1));
 		for (; i < stop; ++i)
 			sender.sendMessage("");
 	}
 	
 	/**
 	 * Loads the alias map from the aliases.txt resource.
 	 */
 	public void loadAliases() {
 		InputStream stream = getResource("aliases.txt");
 		if (stream == null)
 			return;
 		int i = 1;
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
 			String line;
 			
 			while ((line = br.readLine()) != null) {
 				if (line.length() == 0 || line.charAt(0) == '#') continue;
 				Scanner current = new Scanner(line);
 				String name = current.next();
 				int id = current.nextInt();
 				int damage = current.hasNext() ? current.nextInt() : 0;
 				aliases.put(name, (long) id << 16 | damage);
 				i++;
 			}
 			stream.close();
 		} catch (IOException e) {
 			log.warning("Failed to load aliases: " + e.toString());
 		}catch (NoSuchElementException e){
 			log.info("loadAliases broke at line: " + i);
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * Loads the item names map from the items.txt resource.
 	 */
 	public void loadItemNames() {
 		InputStream stream = getResource("items.txt");
 		if (stream == null)
 			return;
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
 			String line = br.readLine();
 			while (line != null) {
 				if (line.length() == 0 || line.charAt(0) == '#') continue;
 				Scanner current = new Scanner(line);
 				int id = current.nextInt(),
 					damage = 0;
 				String name = "";
 				while (current.hasNext()) {
 					name += ' ' + current.next();
 				}
 				if (name.length() == 0)
 					break;
 				itemNames.put((long) id << 16 | damage, name.substring(1));
 				line = br.readLine();
 				if (line != null && line.charAt(0) == '|') {
 					do {
 						if (line.length() == 0 || line.charAt(0) == '#') continue;
 						current = new Scanner(line);
 						if (!current.next().equals("|")) break;
 						if (!current.hasNextInt(16)) break;
 						damage = current.nextInt(16);
 						name = "";
 						while (current.hasNext()) {
 							name += ' ' + current.next();
 						}
 						itemNames.put((long) id << 16 | damage, name.substring(1));
 					} while ((line = br.readLine()) != null);
 				}
 			}
 			stream.close();
 		} catch (IOException e) {
 			log.warning("Failed to load item names: " + e.toString());
 		}
 	}
 	
 	/**
 	 * Sets up Vault.
 	 * @return true on success, false otherwise
 	 */
 	private boolean economySetup() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
 		
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) return false;
 		
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 	
 	/**
 	 * Saves all shops
 	 */
 	public void saveAll() {
 		if (!backup())
 			log.warning("Failed to back up shops");
 		
 		State state = new State();
 		for (Entry<Location, Shop> entry : shops.entrySet()) {
 			Shop shop = entry.getValue();
 			for (ShopEntry e : shop.inventory) {
 				e.quantity = e.item.getAmount();
 				Map<Enchantment, Integer> enchantments = e.item.getEnchantments();
 				e.enchantments = new HashMap<Integer, Integer>(enchantments.size());
 				for (Entry<Enchantment, Integer> en : enchantments.entrySet())
 					e.enchantments.put(en.getKey().getId(), en.getValue());
 			}
 			state.shops.put(new BlockLocation(entry.getKey()), shop);
 		}
 		state.pending = this.pending;
 		
 		try {
 			File dir = getDataFolder();
 			if (!dir.exists()) dir.mkdirs();
 			File f = new File(dir, "shops.dat");
 			FileOutputStream fs = new FileOutputStream(f);
 			ObjectOutputStream out = new ObjectOutputStream(fs);
 			out.writeObject(state);
 		} catch (FileNotFoundException e) {
 			log.warning("Save failed");
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.warning("Save failed");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Attempts to back up the shops.dat savefile.
 	 * @return a boolean indicating success
 	 */
 	public boolean backup() {
 		File stateLocation = new File(getDataFolder(), "shops.dat");
 		if (stateLocation.exists()) {
 			File backup = new File(getDataFolder(), "shops.dat_old");
 			try {
 				InputStream in = new FileInputStream(stateLocation);
 				OutputStream out = new FileOutputStream(backup);
 				byte[] buf = new byte[1024];
 				int i;
 				while ((i = in.read(buf)) > 0) {
 					out.write(buf, 0, i);
 				}
 				in.close();
 				out.close();
 			} catch (FileNotFoundException e) {
 				log.warning("Backup failed");
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				log.warning("Backup failed");
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 		log.warning("Aborting backup: shops.dat not found");
 		return false;
 	}
 	
 	
 	/**
 	 * Loads all shops from the shops.dat savefile.
 	 * @return the saved State
 	 */
 	public State reloadAll() {
 		File stateLocation = new File(getDataFolder(), "shops.dat");
 		if (stateLocation.exists()) {
 			try {
 				FileInputStream fs = new FileInputStream(stateLocation);
 				ObjectInputStream stream = new ObjectInputStream(fs);
 				Object obj = stream.readObject();
 				if (obj instanceof State)
 					return (State) obj;
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 }
