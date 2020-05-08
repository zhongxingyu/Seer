 package uk.co.exec64.EmeraldExchange;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class EmeraldExchange extends JavaPlugin {
 	
 	public static Economy econ = null;
 	
 	//Contains a market for each material allowed
 	private HashMap<Material, Market> markets;
 	
 	//material names/aliases and the actual material they mean
 	private HashMap<String, Material> materials;
 	
 	//Revenue owed to each player for collection, pre tax
 	private HashMap<String, Double> revenue;
 
 	//Deliveries, items that need to be given to each player
 	private HashMap<String, List<EEItemStack>> deliveries;
 
 	
 	
 	public void onEnable() {
 
 		//Fill our hashmap of material aliases with the ones permitted.
 		PopulateMaterials();
 		
 		//Load the markets, deliveries and revenue
 		load();
 		
 		//If we didn't load the markets, make them
 		if(markets == null) {		
 			markets = new HashMap<Material, Market>();
 			
 			for( Material mat : materials.values() ) {
 				markets.put(mat,  new Market(this, mat));
 			}
 		}
 		
 		//If we didn't load any revenue, start blank
 		if(revenue == null) {
 			revenue = new HashMap<String, Double>();
 		}
 		
 		//If we didn't load any deliveries, start blank
 		if(deliveries == null) {		
 			deliveries = new HashMap<String, List<EEItemStack>>();
 		}
 
 		
 		//Make sure vault has connected to an economy.
 		if (!setupEconomy() ) {
 			getLogger().info("EmeraldExchange - Disabled due to no Vault dependency found!");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
 		
 		//Now enable event listener
 		new EEEventListener(this);
 		
 		//Now schedule a collection notification every 5 minutes
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				//Notify all players.
 				Player[] players = getServer().getOnlinePlayers();
 				
 				for(Player player : players) {
 					notifyPlayer(player.getName());
 				}
 			}
 		}, 5 * 60 * 20L, 5 * 60 * 20L);
 		//The above line is initial delay of 5 minutes, repeating every 5 minutes
 		
 		getLogger().info("EmeraldExchange Enabled.");
 	}
 	
 	public void onDisable() {
 		
 		//Save markets, revenue and deliveries to disk
 		save();
 		
 		getLogger().info("EmeraldExchange Disabled.");		
 	}
 	
 	//Connect to vault
 	private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 	
 	//All potential type errors are caught safely, no need for the warnings
 	@SuppressWarnings("unchecked")
 	public void load() {
 		//Load markets, revenue and deliveries
 		//If they cannot be loaded set them to null and they can be constructed by onEnable
 		
 		//Load markets
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Conf.dataFolder + "markets.bin"));
 			Object result = ois.readObject();
 			ois.close();
 
 			markets = (HashMap<Material, Market>)result;
 			
 			for( Market market : markets.values() )
 				market.setPlugin(this);
 			
 			
 			//If new materials were added since we last loaded we need to give them markets or we might crash!
 			for( Material material : materials.values() ) {
 				Market market = markets.get(material);
 				
 				if(market == null) {
 					market = new Market(this, material);
 					markets.put(material, market);
 				}
 				
 			}
 		
 			getLogger().info("Loaded markets.bin");
 			
 		} catch(Exception e) {
 			getLogger().info("markets.bin not found, generating blank markets instead");
 			markets = null;
 		}
 
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Conf.dataFolder + "revenue.bin"));
 			Object result = ois.readObject();
 			ois.close();
 
 			revenue = (HashMap<String, Double>)result;
 			
 			getLogger().info("Loaded revenue.bin");
 			
 		}catch(Exception e){
 			getLogger().info("revenue.bin not found, assuming clean slate.");
 			revenue = null;
 		}		
 
 		try{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Conf.dataFolder + "deliveries.bin"));
 			Object result = ois.readObject();
 			ois.close();
 
 			deliveries = (HashMap<String, List<EEItemStack>>)result;
 			
 			getLogger().info("Loaded deliveries.bin");
 		}catch(Exception e){
 			getLogger().info("deliveries.bin not found, assuming clean slate.");
 			deliveries = null;
 		}
 
 	}
 	
 	public void save() {
 		//Save markets
 		
 		//Clear the plugin pointer to null
 		for( Market market : markets.values() )
 			market.setPlugin(null);
 		
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Conf.dataFolder + "markets.bin"));
 			oos.writeObject(markets);
 			oos.flush();
 			oos.close();
 			getLogger().info("Saved markets.bin");
 			//Handle I/O exceptions
 		}catch(Exception e){
 			getLogger().info("Could not write markets.bin, goodbye lovely data :(");
 		}
 		
 		//Restore the plugin pointer
 		for( Market market : markets.values() )
 			market.setPlugin(this);
 		
 		//Save revenue
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Conf.dataFolder + "revenue.bin"));
 			oos.writeObject(revenue);
 			oos.flush();
 			oos.close();
 			getLogger().info("Saved revenue.bin");
 			//Handle I/O exceptions
 		}catch(Exception e){
 			getLogger().info("Could not write revenue.bin, goodbye lovely data :(");
 		}
 		
 		//Save deliveries
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Conf.dataFolder + "deliveries.bin"));
 			oos.writeObject(deliveries);
 			oos.flush();
 			oos.close();
 			getLogger().info("Saved deliveries.bin");
 			//Handle I/O exceptions
 		}catch(Exception e){
 			getLogger().info("Could not write deliveries.bin, goodbye lovely data :(");
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if( cmd.getName().equalsIgnoreCase("ee")) {
 			//This is an Emerald Exchange command
 			
 			if(player != null) {
 				
 				//Default to the help command if only /ee is given
 				if(args.length == 0)
 					cmdHelp(sender, args);
 				
 				if(args.length > 0) {
 					try {
 					if(args[0].equalsIgnoreCase("help"))
 						cmdHelp(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("top"))
 						cmdTop(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("offers"))
 						cmdOffers(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("bids"))
 						cmdBids(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("offer"))
 						cmdOffer(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("bid"))
 						cmdBid(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("quote"))
 						cmdQuote(sender, args);
 					
 					else if(args[0].equalsIgnoreCase("collect"))
 						cmdCollect(sender, args);
 					else
 						sender.sendMessage(Conf.colorWarning + "Emerald Exchange - Unknown command " + args[0]);
 					
 					} catch (Exception e) {
 						//One of the commands crashed somewhere... oops
 						e.printStackTrace();
 					}
 				}
 				
 			} else {
 				sender.sendMessage("Emerald Exchange can only be used ingame.");
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void cmdHelp(CommandSender sender, String[] args) {
 		
 		if( args.length < 2 ) {
 			sender.sendMessage(Conf.colorAccent + "--------------------");
 			sender.sendMessage(Conf.colorMain +   "Emerald Exchange - HELP");
 			sender.sendMessage(Conf.colorAccent + "--------------------");
 			sender.sendMessage(Conf.colorMain + "Emerald Exchange is a market plugin that allows you to buy and sell goods with ease.");
 			sender.sendMessage(Conf.colorMain + "Buyers can place bids on quantities of goods. Sellers can place offers to sell goods.");
 			sender.sendMessage(Conf.colorMain + "Revenue from selling goods and purchased goods is collected using /ee collect");
 			sender.sendMessage(Conf.colorAccent + "/ee help <command>");
 			sender.sendMessage(Conf.colorAccent + "/ee offers <material name>");
 			sender.sendMessage(Conf.colorAccent + "/ee bids <material name>");
 			sender.sendMessage(Conf.colorAccent + "/ee offer <quantity> <material name> <price>");
 			sender.sendMessage(Conf.colorAccent + "/ee bid <quantity> <material name> <price>");
 			sender.sendMessage(Conf.colorAccent + "/ee quote <quantity> <material name>");
 			sender.sendMessage(Conf.colorAccent + "/ee collect");
 		}
 		
 		if( args.length >= 2 ) {
 			
 			if(args[1].equalsIgnoreCase("help")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee help <command>");
 				sender.sendMessage(Conf.colorMain + "Displays help for the specified command.");
 				
 			} else if( args[1].equalsIgnoreCase("offers")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee offers <material name>");
 				sender.sendMessage(Conf.colorMain + "Displays the offers for the specified material.");
 				
 			} else if( args[1].equalsIgnoreCase("bids")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee bids <material name>");
 				sender.sendMessage(Conf.colorMain + "Displays the bids for the specified material.");
 				
 			} else if( args[1].equalsIgnoreCase("offer")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee offer <quantity> <material name> <price>");
 				sender.sendMessage(Conf.colorMain + "Places an offer to sell the quantity of the material specified for the total price specified.");
 				if(Conf.listingFee > 0) {
 					sender.sendMessage(Conf.colorMain + "This also incurs a listing fee of " + Conf.listingFee);
 				}
 				
 			} else if( args[1].equalsIgnoreCase("bid")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee bid <quantity> <material name> <price>");
 				sender.sendMessage(Conf.colorMain + "Places an offer to buy the quantity of the material specified for the total price specified.");
 				if(Conf.listingFee > 0) {
 					sender.sendMessage(Conf.colorMain + "This also incurs a listing fee of " + Conf.listingFee);
 				}
 				
 			} else if( args[1].equalsIgnoreCase("quote")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee quote <quantity> <material name>");
 				sender.sendMessage(Conf.colorMain + "Calculates the cost of buying the quantity of the material specified.");
 				sender.sendMessage(Conf.colorMain + "Also calculates the revenue (pre tax) from selling the quantity of the material specified.");
 				
 			} else if( args[1].equalsIgnoreCase("collect")) {
 				
 				sender.sendMessage(Conf.colorAccent + "/ee collect");
 				sender.sendMessage(Conf.colorMain + "Checks for any revenue or deliveries, gives them to the player if possible.");
 
 			} else {
 				sender.sendMessage(Conf.colorWarning + "Unknown command to seek help for: " + args[1]);
 			}
 		}
 
 	}
 	
 	public void cmdOffers(CommandSender sender, String[] args) {
 		
 		if( args.length < 2 ) {
 			sender.sendMessage(Conf.colorMain + "Usage:");
 			sender.sendMessage(Conf.colorAccent + "/ee offers <material name>");
 			return;
 		}
 		
 		Material material = materials.get(args[1]);
 		
 		if( material == null ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid material");
 			return;
 		}
 		
 		Market market = markets.get(material);
 		
 		int totalQuantity = 0;
 		double totalPrice = 0;
 		
 		sender.sendMessage(Conf.colorMain + "" + market.getOffersQuantity() + " " + args[1] + " available");
 		
 		//Don't bother with the table if there's nothing to put in it.
 		if(market.getOffersQuantity() == 0)
 			return;
 		
 		sender.sendMessage(Conf.colorMain + "Quantity " +
 						   Conf.colorAccent + "Price " +
 						   Conf.colorMain + "Price Each " +
 						   Conf.colorAccent + "Total Qty " +
 						   Conf.colorMain + "Total Price");
 		
 		
 		int remaining = 5;
 		for( Object object : market.getOffers()) {
 			if(remaining > 0)
 				remaining--;
 			else
 				break;
 			
 			Order order = (Order)object;
 			totalQuantity += order.getQuantity();
 			totalPrice += order.getPrice();
 			sender.sendMessage(Conf.colorMain + " " + order.getQuantity() +
 							   Conf.colorAccent + " " + order.getPrice() +
 							   Conf.colorMain + " " + (order.getPrice() / order.getQuantity()) +
 							   Conf.colorAccent + " " + totalQuantity +
 							   Conf.colorMain + " " + totalPrice);
 		}
 	}
 	
 	public void cmdBids(CommandSender sender, String[] args) {
 		if( args.length < 2 ) {
 			sender.sendMessage(Conf.colorMain + "Usage:");
 			sender.sendMessage(Conf.colorAccent + "/ee bids <material name>");
 			return;
 		}
 		
 		Material material = materials.get(args[1]);
 		
 		if( material == null ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid material");
 			return;
 		}
 		
 		Market market = markets.get(material);
 		
 		int totalQuantity = 0;
 		double totalPrice = 0;
 		
 		sender.sendMessage(Conf.colorMain + "" + market.getBidsQuantity() + " " + args[1] + " wanted");
 		
 		//Don't bother with the table if there's nothing to put in it.
 				if(market.getBidsQuantity() == 0)
 					return;
 	
 		sender.sendMessage(Conf.colorMain + "Quantity " +
 						   Conf.colorAccent + "Price " +
 						   Conf.colorMain + "Price Each " +
 						   Conf.colorAccent + "Total Qty " +
 						   Conf.colorMain + "Total Price");
 		
 		int remaining = 5;
 		for( Object object : market.getBids()) {
 			if(remaining > 0)
 				remaining--;
 			else
 				break;
 			
 			Order order = (Order)object;
 			totalQuantity += order.getQuantity();
 			totalPrice += order.getPrice();
 			sender.sendMessage(Conf.colorMain + " " + order.getQuantity() +
 					   		   Conf.colorAccent + " " + order.getPrice() +
 					   		   Conf.colorMain + " " + (order.getPrice() / order.getQuantity()) +
 					   		   Conf.colorAccent + " " + totalQuantity +
 					   		   Conf.colorMain + " " + totalPrice);
 		}
 	}
 	
 	public void cmdOffer(CommandSender sender, String[] args) {
 		
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if(args.length != 4) {
 			sender.sendMessage(Conf.colorMain + "Usage:");
 			sender.sendMessage(Conf.colorAccent + "/ee offer <quantity> <material name> <price>");
 			return;
 		}
 		
 		int quantity = 0;
 		double price;
 		Material material;
 		
 		try {
 			quantity = Integer.parseInt(args[1]);
 			
 		} catch( Exception e ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid quantity");
 			return;
 		}
 		
 		if(quantity <= 0) {
 			sender.sendMessage(Conf.colorWarning + "Please enter a quantity greater than 0");
 			return;
 		}
 		
 		material = materials.get(args[2]);
 		
 		if( material == null ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid material");
 			return;
 		}
 		
 		try {
 			price = Double.parseDouble(args[3]);
 			
 		} catch( Exception e ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid price");
 			return;
 		}
 		
 		if(price <= 0 ) {
 			sender.sendMessage(Conf.colorWarning + "Please enter a price greater than 0");
 			return;
 		}
 		
 		//Pay the listing fee
 		if(Conf.listingFee > 0) {
 			
 			if(!econ.withdrawPlayer(sender.getName(), Conf.listingFee).transactionSuccess()) {
 				//Could not withdraw the money
 				sender.sendMessage(Conf.colorWarning + "You can't afford to pay the listing fee (" + Conf.listingFee + ")");
 				return;
 			} else {
 				sender.sendMessage(Conf.colorMain + "A listing fee of " + Conf.listingFee + " has been deducted from your account.");
 
 			}
 		}
 		
 		
 		//Take the items from the inventory
 		Inventory inv = player.getInventory();
 		
 		if(inv.contains(material, quantity)) {
 			int taken = 0;
 			while(taken != quantity) {
 				ItemStack stack = inv.getItem(inv.first(material));
 				
 				if(stack.getAmount() <= quantity - taken) {
 					taken += stack.getAmount();
 					inv.setItem(inv.first(material), null);
 				} else {
 					//Remove the remaining material
 					stack.setAmount(stack.getAmount() - (quantity - taken));
 					taken = quantity;
 				}
 			}
 		} else {
 			sender.sendMessage(Conf.colorWarning + "You do not have " + quantity + " " + args[2] + " in your inventory.");
 			return;
 		}
 		
 		Order offer = new Order(quantity, price, player.getName());
 		
 		Market market = markets.get(material);
 		
 		market.addOffer(offer);
 		
 		sender.sendMessage(Conf.colorMain + "Offer for " + quantity + " " + args[2] + " for " + price + " submitted.");
 		getLogger().info(player.getDisplayName() + " created offer for " + quantity + " " + args[2] + " for " + price);
 		
 		market.balance();
 		
 		save();
 	}
 	
 	public void cmdBid(CommandSender sender, String[] args) {
 		
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if(args.length != 4) {
 			sender.sendMessage(Conf.colorMain + "Usage:");
 			sender.sendMessage(Conf.colorAccent + "/ee bid <quantity> <material name> <price>");
 			return;
 		}
 		
 		int quantity = 0;
 		double price;
 		Material material;
 		
 		try {
 			quantity = Integer.parseInt(args[1]);
 			
 		} catch( Exception e ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid quantity");
 			return;
 		}
 		
 		if(quantity <= 0) {
 			sender.sendMessage(Conf.colorWarning + "Please enter a quantity greater than 0");
 			return;
 		}
 		
 		material = materials.get(args[2]);
 		
 		if( material == null ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid material");
 			return;
 		}
 		
 		try {
 			price = Double.parseDouble(args[3]);
 			
 		} catch( Exception e ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid price");
 			return;
 		}
 		
 		if(price <= 0 ) {
 			sender.sendMessage(Conf.colorWarning + "Please enter a price greater than 0");
 			return;
 		}
 		
 		if(!econ.withdrawPlayer(sender.getName(), price + Conf.listingFee).transactionSuccess()) {
 			//Could not withdraw the money
 			sender.sendMessage(Conf.colorWarning + "You can't afford to bid that.");
 			return;
 		} else {
 			sender.sendMessage(Conf.colorMain + "" + price + " has been deducted from your account.");
 			
 			if(Conf.listingFee > 0) {
 				sender.sendMessage(Conf.colorAccent + "(A listing fee of " + Conf.listingFee + " has also been deducted.)");
 			}
 		}
 		
 		Order bid = new Order(quantity, price, player.getName());
 		
 		Market market = markets.get(material);
 		
 		market.addBid(bid);
 		
 		sender.sendMessage(Conf.colorMain + "Bid for " + quantity + " " + args[2] + " for " + price + " submitted.");
 		getLogger().info(player.getDisplayName() + " created bid for " + quantity + " " + args[2] + " for " + price);
 		
 		market.balance();
 		
 		save();
 	}
 	
 	public void cmdQuote(CommandSender sender, String[] args) {
 		
 		if(args.length != 3) {
 			sender.sendMessage(Conf.colorMain + "Usage:");
 			sender.sendMessage(Conf.colorAccent + "/ee quote <quantity> <material name>");
 			return;
 		}
 		
 		int quantity = 0;
 		Material material;
 		
 		try {
 			quantity = Integer.parseInt(args[1]);
 			
 		} catch( Exception e ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid quantity");
 			return;
 		}
 		
 		if(quantity <= 0) {
 			sender.sendMessage(Conf.colorWarning + "Please enter a quantity greater than 0");
 			return;
 		}
 		
 		material = materials.get(args[2]);
 		
 		if( material == null ) {
 			sender.sendMessage(Conf.colorWarning + "Invalid material");
 			return;
 		}
 		
 		Market market = markets.get(material);
 		
 		int offerQuantity = market.getOffersQuantity();
 		double offerPrice = market.getOfferPrice(quantity);
 		
 		double bidPrice = market.getBidPrice(quantity);
 		int bidQuantity = market.getBidsQuantity();
 		
 		if(offerQuantity == 0) {
 			sender.sendMessage(Conf.colorMain + "No " + args[2] + " is available.");
 		} else if(quantity < offerQuantity) {
 			sender.sendMessage(Conf.colorAccent + "" + quantity + " " + Conf.colorMain + args[2] + " is available for " + Conf.colorAccent + "" + offerPrice);
 		} else {
 			sender.sendMessage(Conf.colorAccent + "" + offerQuantity + " " + Conf.colorMain + args[2] + " is available for " + Conf.colorAccent + offerPrice);
 		}
 		
 		if(bidQuantity == 0) {
 			sender.sendMessage(Conf.colorMain + "No " + args[2] + " is wanted.");
 		} else if(quantity < bidQuantity) {
 			sender.sendMessage(Conf.colorAccent + "" + quantity + " " + Conf.colorMain +  args[2] + " is wanted for " + Conf.colorAccent + bidPrice);
 		} else {
 			sender.sendMessage(Conf.colorAccent + "" + bidQuantity + " " + Conf.colorMain + args[2] + " is wanted for " + Conf.colorAccent + bidPrice);
 		}
 		
 		
 	}
 	
 	public void cmdCollect(CommandSender sender, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		List<EEItemStack> stacks = getDeliveries(player.getName());
 		double revenue = getRevenue(player.getName());
 		
 		if(stacks.size() == 0  && revenue == 0 ) {
 			sender.sendMessage(Conf.colorMain + "You have no deliveries waiting.");
 			return;
 		}
 		
 		double tax = revenue * Conf.tax;
 		revenue -= tax;
 		
 		if(econ.depositPlayer(player.getName(), revenue).transactionSuccess()) {
 			sender.sendMessage(Conf.colorMain + "You have been paid " + Conf.colorAccent + revenue + Conf.colorMain + " in revenue.");
 			getLogger().info(player.getDisplayName() + " paid " + revenue + " in revenue.");
 			
 			if(tax > 0)
 				sender.sendMessage(Conf.colorAccent + "" + tax + Conf.colorMain + " of your revenue was taken as tax (" + Conf.colorAccent + (Conf.tax * 100) + "%" + Conf.colorMain + ")");
 			
 			setRevenue(player.getName(), 0);
 		} else {
 			sender.sendMessage(Conf.colorWarning + "Error, for some reason we couldn't pay you your money. Don't worry, it's safe.");
 		}
 		
 		Inventory inv = player.getInventory();
 		
 
 		while(inv.firstEmpty() != -1 && stacks.size() > 0) {
 			EEItemStack stack = stacks.get(0);
 			inv.setItem(inv.firstEmpty(), new ItemStack(stack.material, stack.quantity));
 			stacks.remove(0);
 		}
 
 		if(stacks.size() == 0)
 			sender.sendMessage(Conf.colorMain + "All items have been delivered successfully.");
 		else
 			sender.sendMessage(Conf.colorWarning + "Inventory full, please deposit some items in a chest and try again.");
 		
 		getLogger().info("Items delivered to " + player.getDisplayName());
 		
 	}
 	
 	public void cmdTop(CommandSender sender, String[] args) {
 		//Iterate over each market, get total bids/offers
		for( Market market : markets ) {
			int total = 0;
			//total += market.getOffers().size
		}
 		
 		//Sort totals
 		
 		//Print top 10
 	}
 	
 	
 	public double getRevenue(String player) {
 		if(revenue.get(player) == null)
 			revenue.put(player, 0.0);
 		
 		return revenue.get(player);
 	}
 	
 	public void setRevenue(String player, double revenue) {
 		this.revenue.put(player, revenue);
 	}
 	
 	public void notifyPlayer(String playerName) {
 		if(hasDeliveries(playerName)) {
 			
 			Player player = this.getServer().getPlayer(playerName);
 			
 			if(player != null)
 				player.sendMessage(Conf.colorMain + "[EE] You have deliveries pending. Use /ee collect to collect them!");
 		}
 	}
 	
 	public boolean hasDeliveries(String player) {
 		
 		if(getDeliveries(player).size() > 0)
 			return true;
 		else
 			return false;
 		
 	}
 	
 	public List<EEItemStack> getDeliveries(String player) {
 		if(deliveries.get(player) == null)
 			deliveries.put(player, new ArrayList<EEItemStack>());
 		
 		return deliveries.get(player);
 	}
 	
 	
 	private void PopulateMaterials()
 	{
 		materials = new HashMap<String, Material>();
 		
 		//Items may have multiple aliases
 		materials.put("apple", Material.APPLE);
 		materials.put("arrow", Material.ARROW);
 		materials.put("bed", Material.BED);
 		materials.put("blazepowder", Material.BLAZE_POWDER);
 		materials.put("blaze_powder", Material.BLAZE_POWDER);
 		materials.put("blazerod", Material.BLAZE_ROD);
 		materials.put("blaze_rod", Material.BLAZE_ROD);
 		materials.put("boat", Material.BOAT);
 		materials.put("bone", Material.BONE);
 		materials.put("book", Material.BOOK);
 		materials.put("bookshelf", Material.BOOKSHELF);
 		materials.put("bow", Material.BOW);
 		materials.put("bowl", Material.BOWL);
 		materials.put("bread", Material.BREAD);
 		materials.put("brewingstand", Material.BREWING_STAND);
 		materials.put("brewing_stand", Material.BREWING_STAND);
 		materials.put("brick", Material.BRICK);
 		materials.put("brickstairs", Material.BRICK_STAIRS);
 		materials.put("brick_stairs", Material.BRICK_STAIRS);
 		materials.put("brownmushroom", Material.BROWN_MUSHROOM);
 		materials.put("brown_mushroom", Material.BROWN_MUSHROOM);
 		materials.put("bucket", Material.BUCKET);
 		materials.put("cactus", Material.CACTUS);
 		materials.put("cake", Material.CAKE);
 		materials.put("cauldron", Material.CAULDRON);
 		materials.put("chest", Material.CHEST);
 		materials.put("clay", Material.CLAY_BALL);
 		materials.put("clayball", Material.CLAY_BALL);
 		materials.put("coal", Material.COAL);
 		materials.put("cobblestone", Material.COBBLESTONE);
 		materials.put("cobble", Material.COBBLESTONE);
 		materials.put("cobblestairs", Material.COBBLESTONE_STAIRS);
 		materials.put("cobblestonestairs", Material.COBBLESTONE_STAIRS);
 		materials.put("cobble_stairs", Material.COBBLESTONE_STAIRS);
 		materials.put("cobblestone_stairs", Material.COBBLESTONE_STAIRS);
 		materials.put("compass", Material.COMPASS);
 		materials.put("cookedbeef", Material.COOKED_BEEF);
 		materials.put("cooked_beef", Material.COOKED_BEEF);
 		materials.put("cookedchicken", Material.COOKED_CHICKEN);
 		materials.put("cooked_chicken", Material.COOKED_CHICKEN);
 		materials.put("cookedfish", Material.COOKED_FISH);
 		materials.put("cooked_fish", Material.COOKED_FISH);
 		materials.put("cookie", Material.COOKIE);
 		materials.put("detectorrail", Material.DETECTOR_RAIL);
 		materials.put("detectorail", Material.DETECTOR_RAIL);
 		materials.put("detector_rail", Material.DETECTOR_RAIL);
 		materials.put("diamond", Material.DIAMOND);
 		materials.put("diode", Material.DIODE);
 		materials.put("repeater", Material.DIODE);		
 		materials.put("dirt", Material.DIRT);
 		materials.put("dispenser", Material.DISPENSER);
 		materials.put("doublestep", Material.DOUBLE_STEP);
 		materials.put("double_step", Material.DOUBLE_STEP);
 		materials.put("doubleslab", Material.DOUBLE_STEP);
 		materials.put("double_slab", Material.DOUBLE_STEP);
 		materials.put("egg", Material.EGG);
 		materials.put("enchantmenttable", Material.ENCHANTMENT_TABLE);
 		materials.put("enchantmentable", Material.ENCHANTMENT_TABLE);
 		materials.put("enchantment_table", Material.ENCHANTMENT_TABLE);
 		materials.put("enderpearl", Material.ENDER_PEARL);
 		materials.put("ender_pearl", Material.ENDER_PEARL);
 		materials.put("enderstone", Material.ENDER_STONE);
 		materials.put("ender_stone", Material.ENDER_STONE);
 		materials.put("eyeofender", Material.EYE_OF_ENDER);
 		materials.put("eye_of_ender", Material.EYE_OF_ENDER);
 		materials.put("feather", Material.FEATHER);
 		materials.put("fence", Material.FENCE);
 		materials.put("fencegate", Material.FENCE_GATE);
 		materials.put("fence_gate", Material.FENCE_GATE);
 		materials.put("fermentedspidereye", Material.FERMENTED_SPIDER_EYE);
 		materials.put("fermented_spider_eye", Material.FERMENTED_SPIDER_EYE);
 		materials.put("fishingrod", Material.FISHING_ROD);
 		materials.put("fishing_rod", Material.FISHING_ROD);
 		materials.put("flint", Material.FLINT);
 		materials.put("flintandsteel", Material.FLINT_AND_STEEL);
 		materials.put("flintsteel", Material.FLINT_AND_STEEL);
 		materials.put("flint_and_steel", Material.FLINT_AND_STEEL);
 		materials.put("flint_steel", Material.FLINT_AND_STEEL);
 		materials.put("furnace", Material.FURNACE);
 		materials.put("ghast_tear", Material.GHAST_TEAR);
 		materials.put("glass", Material.GLASS);
 		materials.put("glassbottle", Material.GLASS_BOTTLE);
 		materials.put("glass_bottle", Material.GLASS_BOTTLE);
 		materials.put("glowstone", Material.GLOWSTONE);
 		materials.put("glowstonedust", Material.GLOWSTONE_DUST);
 		materials.put("glowstone_dust", Material.GLOWSTONE_DUST);
 		materials.put("gold", Material.GOLD_INGOT);
 		materials.put("goldingot", Material.GOLD_INGOT);
 		materials.put("gold_ingot", Material.GOLD_INGOT);
 		materials.put("goldnugget", Material.GOLD_NUGGET);
 		materials.put("gold_nugget", Material.GOLD_NUGGET);
 		materials.put("goldapple", Material.GOLDEN_APPLE);
 		materials.put("goldenapple", Material.GOLDEN_APPLE);
 		materials.put("gold_apple", Material.GOLDEN_APPLE);
 		materials.put("golden_apple", Material.GOLDEN_APPLE);		
 		materials.put("gravel", Material.GRAVEL);
 		materials.put("grilledpork", Material.GRILLED_PORK);
 		materials.put("grilled_pork", Material.GRILLED_PORK);
 		materials.put("cookedpork", Material.GRILLED_PORK);
 		materials.put("cooked_pork", Material.GRILLED_PORK);
 		materials.put("cookedporkchop", Material.GRILLED_PORK);
 		materials.put("cooked_porkchop", Material.GRILLED_PORK);
 		materials.put("ice", Material.ICE);
 		materials.put("inksack", Material.INK_SACK);
 		materials.put("ink_sack", Material.INK_SACK);
 		materials.put("irondoor", Material.IRON_DOOR);
 		materials.put("iron_door", Material.IRON_DOOR);
 		materials.put("ironfence", Material.IRON_FENCE);
 		materials.put("iron_fence", Material.IRON_FENCE);
 		materials.put("iron", Material.IRON_INGOT);
 		materials.put("ironingot", Material.IRON_INGOT);
 		materials.put("iron_ingot", Material.IRON_INGOT);
 		materials.put("jackolantern", Material.JACK_O_LANTERN);
 		materials.put("jack_o_lantern", Material.JACK_O_LANTERN);
 		materials.put("jukebox", Material.JUKEBOX);
 		materials.put("ladder", Material.LADDER);
 		materials.put("lapisblock", Material.LAPIS_BLOCK);
 		materials.put("lapis_block", Material.LAPIS_BLOCK);
 		materials.put("lava_bucket", Material.LAVA_BUCKET);
 		materials.put("leather", Material.LEATHER);
 		materials.put("leaves", Material.LEAVES);
 		materials.put("lever", Material.LEVER);
 		materials.put("log", Material.LOG);
 		materials.put("magmacream", Material.MAGMA_CREAM);
 		materials.put("magma_cream", Material.MAGMA_CREAM);
 		materials.put("melon", Material.MELON);
 		materials.put("melonseeds", Material.MELON_SEEDS);
 		materials.put("melon_seeds", Material.MELON_SEEDS);
 		materials.put("milkbucket", Material.MILK_BUCKET);
 		materials.put("minecart", Material.MINECART);
 		materials.put("mossycobblestone", Material.MOSSY_COBBLESTONE);
 		materials.put("mossy_cobblestone", Material.MOSSY_COBBLESTONE);
 		materials.put("mossycobble", Material.MOSSY_COBBLESTONE);
 		materials.put("mossy_cobble", Material.MOSSY_COBBLESTONE);
 		materials.put("mushroomsoup", Material.MUSHROOM_SOUP);
 		materials.put("mushroom_soup", Material.MUSHROOM_SOUP);
 		materials.put("netherbrick", Material.NETHER_BRICK);
 		materials.put("nether_brick", Material.NETHER_BRICK);
 		materials.put("netherbrickstairs", Material.NETHER_BRICK_STAIRS);
 		materials.put("nether_brick_stairs", Material.NETHER_BRICK_STAIRS);
 		materials.put("netherfence", Material.NETHER_FENCE);
 		materials.put("nether_fence", Material.NETHER_FENCE);
 		materials.put("netherwarts", Material.NETHER_WARTS);
 		materials.put("nether_warts", Material.NETHER_WARTS);
 		materials.put("netherrack", Material.NETHERRACK);
 		materials.put("noteblock", Material.NOTE_BLOCK);
 		materials.put("note_block", Material.NOTE_BLOCK);
 		materials.put("obsidian", Material.OBSIDIAN);
 		materials.put("painting", Material.PAINTING);
 		materials.put("paper", Material.PAPER);
 		materials.put("piston", Material.PISTON_BASE);
 		materials.put("pork", Material.PORK);
 		materials.put("poweredminecart", Material.POWERED_MINECART);
 		materials.put("powered_minecart", Material.POWERED_MINECART);
 		materials.put("pumpkin", Material.PUMPKIN);
 		materials.put("pumpkinseeds", Material.PUMPKIN_SEEDS);
 		materials.put("pumpkin_seeds", Material.PUMPKIN_SEEDS);
 		materials.put("rails", Material.RAILS);
 		materials.put("rawbeef", Material.RAW_BEEF);
 		materials.put("beef", Material.RAW_BEEF);
 		materials.put("raw_beef", Material.RAW_BEEF);
 		materials.put("rawchicken", Material.RAW_CHICKEN);
 		materials.put("chicken", Material.RAW_CHICKEN);
 		materials.put("raw_chicken", Material.RAW_CHICKEN);
 		materials.put("rawfish", Material.RAW_FISH);
 		materials.put("fish", Material.RAW_FISH);
 		materials.put("raw_fish", Material.RAW_FISH);
 		materials.put("redmushroom", Material.RED_MUSHROOM);
 		materials.put("red_mushroom", Material.RED_MUSHROOM);
 		materials.put("rose", Material.RED_ROSE);
 		materials.put("redrose", Material.RED_ROSE);
 		materials.put("red_rose", Material.RED_ROSE);
 		materials.put("redstone", Material.REDSTONE);
 		materials.put("redstonetorch", Material.REDSTONE_TORCH_ON);
 		materials.put("redstone_torch", Material.REDSTONE_TORCH_ON);
 		materials.put("rottenflesh", Material.ROTTEN_FLESH);
 		materials.put("rotten_flesh", Material.ROTTEN_FLESH);
 		materials.put("saddle", Material.SADDLE);
 		materials.put("sand", Material.SAND);
 		materials.put("sandstone", Material.SANDSTONE);
 		materials.put("sapling", Material.SAPLING);
 		materials.put("seeds", Material.SEEDS);
 		materials.put("shears", Material.SHEARS);
 		materials.put("sign", Material.SIGN);
 		materials.put("slime", Material.SLIME_BALL);
 		materials.put("slimeball", Material.SLIME_BALL);
 		materials.put("slime_ball", Material.SLIME_BALL);
 		materials.put("stonebrick", Material.SMOOTH_BRICK);
 		materials.put("stone_brick", Material.SMOOTH_BRICK);
 		materials.put("stonestairs", Material.SMOOTH_STAIRS);
 		materials.put("stone_stairs", Material.SMOOTH_STAIRS);
 		materials.put("snowball", Material.SNOW_BALL);
 		materials.put("snow_ball", Material.SNOW_BALL);
 		materials.put("snowblock", Material.SNOW_BLOCK);
 		materials.put("snow_block", Material.SNOW_BLOCK);
 		materials.put("snow", Material.SNOW_BLOCK);
 		materials.put("soulsand", Material.SOUL_SAND);
 		materials.put("soul_sand", Material.SOUL_SAND);
 		materials.put("speckledmelon", Material.SPECKLED_MELON);
 		materials.put("speckled_melon", Material.SPECKLED_MELON);
 		materials.put("glisteringmelon", Material.SPECKLED_MELON);
 		materials.put("glistering_melon", Material.SPECKLED_MELON);
 		materials.put("spidereye", Material.SPIDER_EYE);
 		materials.put("spider_eye", Material.SPIDER_EYE);
 		materials.put("sponge", Material.SPONGE);
 		materials.put("step", Material.STEP);
 		materials.put("slab", Material.STEP);
 		materials.put("stoneslab", Material.STEP);
 		materials.put("stone", Material.STONE);
 		materials.put("stonebutton", Material.STONE_BUTTON);
 		materials.put("stone_button", Material.STONE_BUTTON);
 		materials.put("button", Material.STONE_BUTTON);
 		materials.put("storageminecart", Material.STORAGE_MINECART);
 		materials.put("storage_minecart", Material.STORAGE_MINECART);
 		materials.put("string", Material.STRING);
 		materials.put("sugar", Material.SUGAR);
 		materials.put("sugarcane", Material.SUGAR_CANE);
 		materials.put("sugar_cane", Material.SUGAR_CANE);
 		materials.put("sulphur", Material.SULPHUR);
 		materials.put("sulfur", Material.SULPHUR);
 		materials.put("glasspane", Material.THIN_GLASS);
 		materials.put("glass_pane", Material.THIN_GLASS);
 		materials.put("tnt", Material.TNT);
 		materials.put("torch", Material.TORCH);
 		materials.put("trapdoor", Material.TRAP_DOOR);
 		materials.put("trap_door", Material.TRAP_DOOR);
 		materials.put("vine", Material.VINE);
 		materials.put("watch", Material.WATCH);
 		materials.put("waterbucket", Material.WATER_BUCKET);
 		materials.put("water_bucket", Material.WATER_BUCKET);
 		materials.put("web", Material.WEB);
 		materials.put("spiderweb", Material.WEB);
 		materials.put("spider_web", Material.WEB);
 		materials.put("wheat", Material.WHEAT);
 		materials.put("wood", Material.WOOD);
 		materials.put("planks", Material.WOOD);
 		materials.put("door", Material.WOOD_DOOR);
 		materials.put("wooddoor", Material.WOOD_DOOR);
 		materials.put("wood_door", Material.WOOD_DOOR);
 		materials.put("woodendoor", Material.WOOD_DOOR);
 		materials.put("wooden_door", Material.WOOD_DOOR);
 		materials.put("wool", Material.WOOL);
 		materials.put("workbench", Material.WORKBENCH);
 		materials.put("yellowflower", Material.YELLOW_FLOWER);
 		materials.put("yellow_flower", Material.YELLOW_FLOWER);
 	}
 
 }
 
