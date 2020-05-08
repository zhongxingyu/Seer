 package net.stormdev.ucars.trade;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import net.stormdev.ucars.stats.HandlingDamagedStat;
 import net.stormdev.ucars.stats.HealthStat;
 import net.stormdev.ucars.stats.NameStat;
 import net.stormdev.ucars.stats.SpeedStat;
 import net.stormdev.ucars.stats.Stat;
 import net.stormdev.ucars.utils.Car;
 import net.stormdev.ucars.utils.CarForSale;
 import net.stormdev.ucars.utils.CarGenerator;
 import net.stormdev.ucars.utils.CarValueCalculator;
 import net.stormdev.ucars.utils.IconMenu;
 import net.stormdev.ucars.utils.InputMenu;
 import net.stormdev.ucars.utils.InputMenu.OptionClickEvent;
 import net.stormdev.ucars.utils.InputMenuClickEvent;
 import net.stormdev.ucars.utils.TradeBoothClickEvent;
 import net.stormdev.ucars.utils.TradeBoothMenuType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryAction;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.AnvilInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.useful.ucars.CarHealthData;
 import com.useful.ucars.Lang;
 import com.useful.ucars.PlaceManager;
 import com.useful.ucars.ucarDeathEvent;
 import com.useful.ucars.ucars;
 import com.useful.ucarsCommon.StatValue;
 
 public class UTradeListener implements Listener {
 	main plugin = null;
 	public UTradeListener(main plugin){
 		this.plugin = plugin;
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	void itemCraft(CraftItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		ItemStack recipe = event.getCurrentItem();
 		if(!(recipe.getType() == Material.MINECART)){
 			return;
 		}
 		if(recipe.getDurability() < 20){
 			return;
 		}
 		Car car = CarGenerator.gen();
         event.setCurrentItem(car.getItem());
         main.plugin.carSaver.cars.put(car.getId(), car);
         main.plugin.carSaver.save();
 		return;
 	}
 	@EventHandler (priority = EventPriority.MONITOR)
 	void carUpgradeAnvil(InventoryClickEvent event){
 		if(event.getAction()==InventoryAction.CLONE_STACK){ //Don't work but left here until bukkit fixes
 			ItemStack cloned = event.getCursor();
 			if(cloned.getType() == Material.MINECART && cloned.getDurability()>19){
 				event.setCancelled(true);
 			}
 			return;
 		}
 		final Player player = (Player) event.getWhoClicked();
 		InventoryView view = event.getView();
 		final Inventory i = event.getInventory();
 		if(!(i instanceof AnvilInventory)){
 			return;
 		}
 		int slotNumber = event.getRawSlot();
 		if(!(slotNumber == view.convertSlot(slotNumber))){
 			//Not clicking in the anvil
 			return;
 		}
 		//AnvilInventory i = (AnvilInventory) inv;
 		Boolean update = true;
 		Boolean save = false;
 		Boolean pickup = false;
 		if(event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME){
 			update = false;
 			pickup = true;
 			if(slotNumber == 2){ //Result slot
 				save = true;
 			}
 		}
 		ItemStack carItem = null;
 		try {
 			carItem = i.getItem(0);
 		} catch (Exception e) {
 			return;
 		}
 		if(carItem == null){
 			return;
 		}
 		if(!(carItem.getType() == Material.MINECART) || !(carItem.getDurability() > 19)){
 			return; //Not a car
 		}
 		//Anvil contains a car in first slot.
 		ItemMeta meta = carItem.getItemMeta();
 		List<String> lore = meta.getLore();
 		final UUID id;
 		try {
 			if(lore.size() < 1){
 				return;
 			}
 			id = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 		} catch (Exception e) {
 			return;
 		}
 		Car car = null;
 		if(!plugin.carSaver.cars.containsKey(id)){
 			return;
 		}
 		car = plugin.carSaver.cars.get(id);
 		final HashMap<String, Stat> stats = car.getStats();
         if(save && slotNumber ==2){
 			//They are renaming it
         	ItemStack result = event.getCurrentItem();
         	String name = ChatColor.stripColor(result.getItemMeta().getDisplayName());
         	stats.put("trade.name", new NameStat(name, plugin));
         	car.setStats(stats);
         	plugin.carSaver.cars.remove(id);
         	plugin.carSaver.cars.put(id, car);
         	plugin.carSaver.save();
         	player.sendMessage(main.colors.getSuccess()+"+"+main.colors.getInfo()+" Renamed car to: '"+name+"'");
         	return;
 		}
 		InventoryAction a = event.getAction();
 		ItemStack upgrade = null;
 		Boolean set = false;
 		final ItemStack up = upgrade;
 		final Boolean updat = update;
 		final Boolean sav = save;
 		final Car ca = car;
 		if(slotNumber == 1 && (a==InventoryAction.PLACE_ALL || a==InventoryAction.PLACE_ONE || a==InventoryAction.PLACE_SOME) && event.getCursor().getType()!=Material.AIR){
 			//upgrade = event.getCursor().clone();
 			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
 
 				public void run() {
 					ItemStack upgrade = up;
 					try {
 						upgrade = i.getItem(1); //Upgrade slot
 					} catch (Exception e) {
 						return;
 					}
 					if(upgrade == null){
 						return;
 					}
 					//A dirty trick to get the inventory to look correct on the client
 					applyUpgrades(upgrade, stats, ca, updat, sav, player, i, id);
 					return;
 				}}, 1l);
 			set = true;
 			return;
 		}
 		if(!set){
 		    try {
 				upgrade = i.getItem(1); //Upgrade slot
 			} catch (Exception e) {
 				return;
 			}
 		}
 		if(upgrade == null){
 			return;
 		}
 		if(pickup && slotNumber == 1){
 			return; //Don't bother tracking and updating, etc...
 		} 
 		applyUpgrades(upgrade, stats, car, update, save, player, i, id);
 		return;
 	}
 	@SuppressWarnings("deprecation")
 	public void applyUpgrades(ItemStack upgrade, HashMap<String, Stat> stats, Car car, Boolean update, Boolean save, Player player, Inventory i, UUID carId){
 		   String upgradeMsg = net.stormdev.ucars.trade.Lang.get("general.upgrade.msg");
 		   if(upgrade.getType() == Material.IRON_BLOCK){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 					health = stat.getHealth();
 				}
 				double bonus = (9*upgrade.getAmount());
 				health = health + bonus; //Add 9 to health stat
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
 				player.sendMessage(upgradeMsg);
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else if(upgrade.getType() == Material.IRON_INGOT){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 					health = stat.getHealth();
 				}
 				double bonus = 1*upgrade.getAmount();
 				health = health + bonus; //Add 1 to health stat
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
 				player.sendMessage(upgradeMsg);
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else if(upgrade.getType() == Material.LEVER){
 				stats.remove("trade.handling"); //Fix handling if broken
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), "Fixed");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "all damage to the car");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), "Undamaged");
 				player.sendMessage(upgradeMsg);
 				upgrade.setAmount(0);
 			}
 			else if(upgrade.getType() == Material.REDSTONE){
 				//Increment speed
 				double speed = 1;
 				SpeedStat speedStat = new SpeedStat(speed, plugin);
 				if(stats.containsKey("trade.speed")){
 					speedStat = (SpeedStat) stats.get("trade.speed");
 					speed = speedStat.getSpeedMultiplier();
 				}
 				speed = speed + (0.05*upgrade.getAmount());
 				speed = speed * 100; //0.05 -> 5
 				speed = Math.round(speed);
 				speed = speed / 100; //5 -> 0.05
 				if(speed > 5){
 					speed = 5;
 				}
 				speedStat.setSpeedMultiplier(speed);
 				stats.put("trade.speed", speedStat);
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), "0.05x");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+"x (Max: "+5+"x)");
 				player.sendMessage(upgradeMsg);
 				upgrade.setAmount(0);
 			}
 			else{
 				//Invalid item
 				return;
 			}
 			i.clear(1);
 			if(update){
 				car.setStats(stats);
 				i.setItem(0, car.getItem());
 				plugin.carSaver.cars.remove(carId);
 				plugin.carSaver.cars.put(carId, car);
 				plugin.carSaver.save();
 				player.updateInventory();
 			}
 			return;
 	}
 	/*
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGHEST)
 	void carUpgradeRecipes(InventoryClickEvent event){
 		Inventory inv = event.getInventory();
 		if(!(inv instanceof CraftingInventory)){
 			return;
 		}
 		ItemStack[] items = inv.getContents().clone();
 		ArrayList<ItemStack> upgradeItems = new ArrayList<ItemStack>();
 		Boolean isUpgrade = false;
 		ItemStack car = null;
 		for(ItemStack item:items){
 			if(item.getType().equals(Material.MINECART)){
 				if(item.getDurability() > 19){
 					isUpgrade  = true;
 					car = item;
 				}
 			}
 			upgradeItems.add(item);
 		}
 		if(!isUpgrade){
 			return;
 		}
 		ItemMeta meta = car.getItemMeta();
 		List<String> lore = meta.getLore();
 		UUID id;
 		try {
 			if(lore.size() < 1){
 				return;
 			}
 			id = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 		} catch (Exception e) {
 			return;
 		}
 		Car c = null;
 		if(!plugin.carSaver.cars.containsKey(id)){
 			return;
 		}
 		c = plugin.carSaver.cars.get(id);
 		HashMap<String, Stat> stats = c.getStats();
 		upgradeItems.add(event.getCurrentItem());
 		for(ItemStack upgrade:upgradeItems){
 			if(upgrade.getType() == Material.MINECART || upgrade.getType() == Material.AIR){
 				//Allowed
 			}
 			else if(upgrade.getType() == Material.IRON_BLOCK){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 				}
 				health = health + (9*upgrade.getAmount()); //Add 9 to health stat
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else if(upgrade.getType() == Material.IRON_INGOT){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 				}
 				health = health + (1*upgrade.getAmount()); //Add 1 to health stat
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else{
 				//Invalid item
 				return;
 			}
 		}
 		c.setStats(stats);
 	    CraftingInventory ci = (CraftingInventory) inv;
 	    ci.clear();
 	    ((Player)event.getView().getPlayer()).getInventory().addItem(c.getItem());
 	    ((Player)event.getView().getPlayer()).updateInventory();
 	    plugin.carSaver.cars.put(id, c);
 	    plugin.carSaver.save();
 		return;
 	}
 	*/
 	@EventHandler(priority = EventPriority.LOW)
 	void carPlace(PlayerInteractEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
 			return;
 		}
 		Block block = event.getClickedBlock();
 		ItemStack inHand = event.getPlayer().getItemInHand().clone();
 		if(inHand.getDurability() < 20){ //Not a car
 			return;
 		}
 		if (inHand.getType() == Material.MINECART) {
 			// Its a minecart!
 			int iar = block.getTypeId();
 			if (iar == 66 || iar == 28 || iar == 27) {
 				return;
 			}
 			if(!PlaceManager.placeableOn(iar, block.getData())){
 				return;
 			}
 			if (!ucars.config.getBoolean("general.cars.enable")) {
 				return;
 			}
 			if (ucars.config.getBoolean("general.cars.placePerm.enable")) {
 				String perm = ucars.config
 						.getString("general.cars.placePerm.perm");
 				if (!event.getPlayer().hasPermission(perm)) {
 					String noPerm = Lang.get("lang.messages.noPlacePerm");
 					noPerm = noPerm.replaceAll("%perm%", perm);
 					event.getPlayer().sendMessage(
 							ucars.colors.getError() + noPerm);
 					return;
 				}
 			}
 			if (event.isCancelled()) {
 				event.getPlayer().sendMessage(
 						ucars.colors.getError()
 								+ Lang.get("lang.messages.noPlaceHere"));
 				return;
 			}
 			List<String> lore = inHand.getItemMeta().getLore();
 			Car c = null;
 			if(lore.size() > 0){
 			UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 			if(!plugin.carSaver.cars.containsKey(carId)){
 				return;
 			}
 			c = plugin.carSaver.cars.get(carId);
 			}
 			else{
 				return;
 			}
 			HashMap<String, Stat> stats = c.getStats();
 			Location loc = block.getLocation().add(0, 1.5, 0);
 			loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
 			final Entity car = event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
 			double health = ucars.config.getDouble("general.cars.health.default");
 			if(stats.containsKey("trade.health")){
 				try {
 					health = (Double) stats.get("trade.health").getValue();
 				} catch (Exception e) {
 					//Leave health to default
 				}
 			}
 			Runnable onDeath = new Runnable(){
 				//@Override
 				public void run(){
 					plugin.getServer().getPluginManager().callEvent(new ucarDeathEvent((Minecart) car));
 				}
 			};
 			car.setMetadata("carhealth", new CarHealthData(health, onDeath, plugin));
 			/*
 			 * Location carloc = car.getLocation();
 			 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
 			 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
 			 * car.setVelocity(new Vector(0,0,0));
 			 */
 			UUID id = car.getUniqueId();
 			car.setMetadata("trade.car", new StatValue(true, plugin));
 				ItemStack placed = event.getPlayer().getItemInHand();
 				placed.setAmount(0);
 				event.getPlayer().getInventory().setItemInHand(placed);
 			event.setCancelled(true);
 			while(plugin.carSaver.cars.containsKey(id)){
 				Car cr = plugin.carSaver.cars.get(id);
 				plugin.carSaver.cars.remove(id);
 			    UUID newId = UUID.randomUUID();
 			    while(plugin.carSaver.cars.containsKey(newId)){
 			    	newId = UUID.randomUUID();
 			    }
 			    plugin.carSaver.cars.put(newId, cr);
 			}
 			plugin.carSaver.cars.remove(c.getId());
 			c.setId(id); //Bind car id to minecart id
 			c.isPlaced = true;
 			plugin.carSaver.cars.put(id, c);
 			plugin.carSaver.save();
 			String name = "Unnamed";
 			if(stats.containsKey("trade.name")){
 				name = stats.get("trade.name").getValue().toString();
 			}
 			String placeMsg = net.stormdev.ucars.trade.Lang.get("general.place.msg");
 			placeMsg = main.colors.getInfo() + placeMsg.replaceAll(Pattern.quote("%name%"), "'"+name+"'");
 			event.getPlayer().sendMessage(placeMsg);
 			//Registered car
 		}
 		return;
 	}
 	@EventHandler (priority=EventPriority.LOW)
 	void carRemoval(ucarDeathEvent event){
 		Minecart cart = event.getCar();
 		UUID id = cart.getUniqueId();
 		if(!plugin.carSaver.cars.containsKey(id)){
 			return;
 		}
 		Car car = plugin.carSaver.cars.get(id);
 		if(!car.isPlaced){
 			return;
 		}
 		car.isPlaced = false;
 		if(main.random.nextBoolean()){
 			if(main.random.nextBoolean()){
 				if(main.random.nextBoolean()){
 				    car.stats.put("trade.handling", new HandlingDamagedStat(true, plugin));
 				}
 			}
 		}
 		plugin.carSaver.cars.put(id, car);
 		plugin.carSaver.save();
 		cart.eject();
 		Location loc = cart.getLocation();
 		cart.remove();
 		loc.getWorld().dropItemNaturally(loc, new ItemStack(car.getItem()));
 		//Remove car and get back item
 		event.setCancelled(true);
 		return;
 	}
 	//Add extra functions; eg. Car trade station, etc...
 	@EventHandler
 	public void signWriter(SignChangeEvent event){
 		String[] lines = event.getLines();
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Trade]")){
 			lines[0] = ChatColor.GREEN+"[Trade]";
 			lines[1] = ChatColor.RED + ChatColor.stripColor(lines[1]);
 			lines[2] = "Place chest";
 			lines[3] = "above";
 		}
 		return;
 	}
 	@EventHandler
 	public void tradeBooth(InventoryOpenEvent event){
 		if(!main.config.getBoolean("general.carTrading.enable")){
 			//Don't do car trading
 			return;
 		}
 		if(main.economy == null){
 	        Boolean installed = plugin.setupEconomy();
 	        if(!installed){
 	        	main.config.set("general.carTrading.enable", false);
 	        	main.logger.info(main.colors.getError()+"[Important] Unable to find an economy plugin:"
 	        			+ " trade booths have been closed.");
 	        	return;
 	        }
 		}
 		Inventory inv = event.getInventory();
 		if (!(inv.getHolder() instanceof Chest || inv.getHolder() instanceof DoubleChest)){
             return;
         }
 		//They opened a chest
 		Block block = null;
 		if(inv.getHolder() instanceof Chest){
 			block = ((Chest)inv.getHolder()).getBlock();
 		}
 		else{
 			block = ((DoubleChest)inv.getHolder()).getLocation().getBlock();
 		}
 		Block underBlock = block.getRelative(BlockFace.DOWN);
 		if(!(underBlock.getState() instanceof Sign)){
 			return;
 		}
 		Sign sign = (Sign) underBlock.getState();
 		if(!(ChatColor.stripColor(sign.getLines()[0])).equalsIgnoreCase("[Trade]") || !(ChatColor.stripColor(sign.getLines()[1])).equalsIgnoreCase("cars")){
 			return;
 		}
 		//A trade sign for cars
 		//Create a trade inventory
 		Player player = (Player) event.getPlayer(); //Get the player from the event
 		event.getView().close();
 		event.setCancelled(true); //Cancel the event
 		plugin.tradeMenu.open(player);
 		//Made the trade booth
 		return;
 	}
 	@EventHandler
 	public void tradeMenuSelect(final TradeBoothClickEvent event){
 		IconMenu.OptionClickEvent clickEvent = event.getClickEvent();
 		final Player player = clickEvent.getPlayer();
 		int position = clickEvent.getPosition();
 		Runnable doAfter = null;
 		if(event.getMenuType() == TradeBoothMenuType.MENU){ //They are selecting which menu to open
 			if(position == 0){ //Read tutorial
 				player.sendMessage(main.colors.getTitle()+"Tutorial coming soon!");
 				return;
 			}
 			else if(position == 1){ //Buy cars
 				doAfter = new Runnable(){
 					public void run() {
 						getCarsForSaleMenu(1).open(player);
 						return;
 				    }};
 			    //Don't return
 			}
 			else if(position == 2){ //Sell cars
 				doAfter = new Runnable(){
 					public void run() {
 						getSellCarsInputMenu().open(player);
 						return;
 				    }};
 				//Don't return
 			}
 			else if(position == 3){ //Buy upgrades
 				doAfter = new Runnable(){
 					public void run() {
 						getUpgradesForSaleMenu(1).open(player);
 						return;
 				    }};
 			    //Don't return
 			}
 			else if(position == 4){ //Sell upgrades
 				//TODO
 			}
 		}
 		else if(event.getMenuType() == TradeBoothMenuType.BUY_CARS){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 53){
 				//Next page
 				doAfter = new Runnable(){
 					public void run() {
 						getCarsForSaleMenu(event.getPage()+1).open(player);
 						return;
 				    }};
 			}
 			else if(position == 52){
 				int page = event.getPage();
 				if(page > 1){
 					page--;
 				}
 				final int p = page;
 				doAfter = new Runnable(){
 					public void run() {
 						getCarsForSaleMenu(p).open(player);
 						return;
 				    }};
 			}
 			else{
 				//Positions 1-> 51
 				//TODO Get car and buy it using vault
 			}
 		}
 		else if(event.getMenuType() == TradeBoothMenuType.BUY_UPGRADES){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 53){
 				//Next page
 				doAfter = new Runnable(){
 					public void run() {
 						getUpgradesForSaleMenu(event.getPage()+1).open(player);
 						return;
 				    }};
 			}
 			else if(position == 52){
 				int page = event.getPage();
 				if(page > 1){
 					page--;
 				}
 				final int p = page;
 				doAfter = new Runnable(){
 					public void run() {
 						getUpgradesForSaleMenu(p).open(player);
 						return;
 				    }};
 			}
 			else{
 				//Positions 1-> 51
 				//TODO Get car and sell it using vault
 			}
 		}
 		if(doAfter != null){
 			plugin.getServer().getScheduler().runTaskLater(plugin, doAfter, 2l);
 		}
 		return;
 	}
 	@EventHandler
 	void sellStuff(InputMenuClickEvent event){
 		OptionClickEvent clickEvent = event.getClickEvent();
 		final Player player = clickEvent.getPlayer();
 		int position = clickEvent.getPosition();
 		Runnable doAfter = null;
 		if(event.getMenuType() == TradeBoothMenuType.SELL_CARS){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 8){
 				//TODO Check if valid and try to sell it
 				Inventory i = clickEvent.getInventory();
 				if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
 					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 					return;
 				}
 				ItemStack carItem = i.getItem(4);
 				if(carItem.getType() != Material.MINECART || carItem.getDurability() < 20){
 					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 					return;
 				}
 				//Is a valid car to sell
 				List<String> lore = carItem.getItemMeta().getLore();
 				Car c = null;
 				if(lore.size() > 0){
 				UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 				if(!plugin.carSaver.cars.containsKey(carId)){
 					return;
 				}
 				c = plugin.carSaver.cars.get(carId);
 				}
 				else{
 					player.sendMessage(main.colors.getInfo()+"Invalid item to sell!");
 					return;
 				}
 				double price = CarValueCalculator.getCarValueForSale(c);
 				if(main.economy == null){
 					return;
 				}
 				String msg = net.stormdev.ucars.trade.Lang.get("general.sell.msg");
 				msg = msg.replaceAll(Pattern.quote("%item%"), "a car");
				msg = msg.replaceAll(Pattern.quote("%price%"), main.config.getString("general.carTrading.currencySign")+price);
 				// Add to market for sale
 				double purchase = CarValueCalculator.getCarValueForPurchase(c);
 				CarForSale saleItem = new CarForSale(c.id, player.getName(), purchase);
 				if(!plugin.salesManager.carsForSale.containsKey(c.id)){
 					plugin.salesManager.carsForSale.put(c.id, saleItem);
 					plugin.salesManager.saveCars();
 					player.sendMessage(main.colors.getInfo()+msg); //Tell player they are selling it on the market
 				}
 			}
 			else if(position == 4){
 				//Check if car and if it is update the sale button
 				final Inventory inv = clickEvent.getInventory();
 				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
 
 					public void run() {
 						Inventory i = inv;
 						if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
 							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 							return;
 						}
 						ItemStack carItem = i.getItem(4);
 						if(carItem.getType() != Material.MINECART || carItem.getDurability() < 20){
 							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 							return;
 						}
 						List<String> lore = carItem.getItemMeta().getLore();
 						Car c = null;
 						if(lore.size() > 0){
 						UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 						if(!plugin.carSaver.cars.containsKey(carId)){
 							return;
 						}
 						c = plugin.carSaver.cars.get(carId);
 						}
 						else{
 							player.sendMessage(main.colors.getInfo()+"Invalid item to sell!");
 							return;
 						}
 						double price = CarValueCalculator.getCarValueForSale(c);
 						if(main.economy == null){
 							return;
 						}
 						ItemStack sellItem = new ItemStack(Material.EMERALD);
 						ItemMeta im = sellItem.getItemMeta();
 						im.setDisplayName(main.colors.getTitle()+"Sell");
 						ArrayList<String> lre = new ArrayList<String>();
 					    lre.add(main.colors.getInfo()+"For: "+main.config.getString("general.carTrading.currencySign")+price);
 						im.setLore(lre);
 						sellItem.setItemMeta(im);
 						inv.setItem(8, sellItem);
 						return;
 					}}, 1l);
 			}
 		}
 		if(doAfter != null){
 			plugin.getServer().getScheduler().runTaskLater(plugin, doAfter, 2l);
 		}
 		return;
 	}
 	IconMenu getCarsForSaleMenu(final int page){
 		//TODO Create method
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.buyCars")+" Page: "+page;
 		if(title.length() > 32){
 			title = main.colors.getError()+"Buy Upgrades (ERROR:Too Long)";
 		}
 		IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
             public void onOptionClick(IconMenu.OptionClickEvent event) {
             	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.BUY_CARS, page);
             	plugin.getServer().getPluginManager().callEvent(evt);
             	event.setWillClose(true);
             	event.setWillDestroy(true);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
 		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
 		//1-51 slots available on the page
 		int pos = 1;
 		int start = (page-1)*51;
 		@SuppressWarnings("unchecked")
 		HashMap<UUID, CarForSale> cars = (HashMap<UUID, CarForSale>) plugin.salesManager.carsForSale.clone();
 		Object[] keys = cars.keySet().toArray();
 		for(int i=start;i<keys.length;i++){
 			CarForSale car= cars.get(keys[i]);
 			double price = car.getPrice();
 	        String seller = car.getSeller();
 	        UUID carId = car.getCarId();
 	        ItemStack item = new ItemStack(Material.AIR);
 	        String name = "Car";
 	        List<String> lore = new ArrayList<String>();
 	        if(plugin.carSaver.cars.containsKey(carId)){
 	        	Car c = plugin.carSaver.cars.get(carId);
 	        	if(c.getStats().containsKey("trade.name")){
 	        		name = c.getStats().get("trade.name").getValue().toString();
 	        	}
 	        	item = c.getItem();
 	        	ItemMeta im = item.getItemMeta();
 	        	lore.add(main.colors.getInfo()+main.config.getString("general.carTrading.currencySign")+price);
 	        	lore.add(main.colors.getInfo()+"Seller: "+seller);
 	        	lore.addAll(2, im.getLore());
 	        	im.setLore(lore);
 	        	item.setItemMeta(im);
 	        }   
 	        if(pos < 52){
         		menu.setOption(pos, item, main.colors.getTitle()+name, lore);
         		pos++;
         	}
 		}
 		//TODO Set option slots for all cars for sale
 		return menu;
 	}
 	IconMenu getUpgradesForSaleMenu(final int page){
 		//TODO Create method
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.buyUpgrades")+" Page: "+page;
 		if(title.length() > 32){
 			title = main.colors.getError()+"Buy Upgrades (ERROR:Too Long)";
 		}
 		IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
             public void onOptionClick(IconMenu.OptionClickEvent event) {
             	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.BUY_UPGRADES, page);
             	plugin.getServer().getPluginManager().callEvent(evt);
             	event.setWillClose(true);
             	event.setWillDestroy(true);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
 		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
 		//TODO Set option slots for all upgrades for sale
 		return menu;
 	}
 	
 	InputMenu getSellCarsInputMenu(){
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.sellCars");
 		if(title.length() > 32){
 			title = main.colors.getError()+"Sell a car";
 		}
 		InputMenu menu = new InputMenu(title, 9, new InputMenu.OptionClickEventHandler() {
             public void onOptionClick(InputMenu.OptionClickEvent event) {
             	if(event.getPosition() == 0 || event.getPosition() == 8){
             		event.setWillClose(true);
             	}
             	InputMenuClickEvent evt = new InputMenuClickEvent(event, TradeBoothMenuType.SELL_CARS);
             	plugin.getServer().getPluginManager().callEvent(evt);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.addButtonSlot(0);
 		menu.setOption(8, new ItemStack(Material.EMERALD), main.colors.getTitle()+"Sell", main.colors.getError()+"Unavailable");
 		menu.addButtonSlot(8);
 		menu.setOption(1, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(1);
 		menu.setOption(2, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(2);
 		menu.setOption(3, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(3);
 		menu.setOption(5, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(5);
 		menu.setOption(6, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(6);
 		menu.setOption(7, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(7);
 		//4 is the input box
 		return menu;
 	}
 
 }
