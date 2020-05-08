 package com.webkonsept.bukkit.repairchest;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RepairChestPlugin extends JavaPlugin {
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	
 	private RepairChestPlayerListener playerListener = new RepairChestPlayerListener(this);
 	protected RepairChestBlockListener blockListener = new RepairChestBlockListener(this);
 	private RepairChestEntityListener entityListener = new RepairChestEntityListener(this);
 	protected RepairChestList chestList = new RepairChestList(new File(getDataFolder(),"chests.txt"),this);
 	
 	protected Integer currency = 266; // Gold Ingot
 	protected Material currencyMaterial = Material.GOLD_INGOT;
 	protected String currencyName ="g";
 	protected double baseCost = 0.01; // 100 damage = 1 this.currency
 	protected boolean verbose = false;
 	protected boolean partialRepair = false;
 	protected boolean distributePartialRepair = true;
 	protected String currencyString = "???";
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		boolean success = true;
 		if (! this.isEnabled()) return false;
 		
 		Player player = null;
 		if (sender instanceof Player){
 			player = (Player)sender;
 		}
 		
 		
 		if (command.getName().equalsIgnoreCase("repairchest")){
 			if (args.length == 1){
 				if (player == null || permit(player,"repairchest.command."+args[0])){
 					if (args[0].equalsIgnoreCase("test")){
 						if (player == null){
 							sender.sendMessage("Sorry, but the console can't have an item in it's hand...");
 						}
 						else {
 							ItemStack inHand = player.getItemInHand();
 							if (inHand.getType().equals(Material.AIR)){
 								sender.sendMessage(ChatColor.RED+"You can't test with an empty hand, man.");
 							}
 							else if (inHand.getMaxStackSize() == 1 && inHand.getType().getMaxDurability() > 10){
 								inHand.setDurability((short) (inHand.getType().getMaxDurability() - 5));
 								sender.sendMessage(ChatColor.GREEN+"Your tool has been nearly broken...");
 							}
 							else {
 								sender.sendMessage(ChatColor.RED+"This item isn't suitable for this test.");
 							}
 						}
 					}
 					else if (args[0].equalsIgnoreCase("reload")){
 					    this.reloadConfig();
 						this.loadConfig();
 					}
 				}
 				else {
 					sender.sendMessage(ChatColor.RED+"Permission to '"+args[0]+"' was denied.");
 				}
 			}
 			else {
 				sender.sendMessage(ChatColor.RED+"Invalid number of arguments.  /rc [test|reload]");
 			}
 		}
 		else {
 			success = false;
 		}
 		
 		return success;
 	}
 	@Override
 	public void onDisable() {
 		this.out("Disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		this.loadConfig();
 		this.out("Enabled!  currency: "+currencyString+"   baseCost: "+baseCost);
 		this.babble("VERBOSE MODE!  This will get spammy!");
 		PluginManager pm =getServer().getPluginManager();
 		pm.registerEvents(blockListener,this);
 		pm.registerEvents(playerListener,this);
 		pm.registerEvents(entityListener,this);
 		/* OLD!
 		pm.registerEvent(Event.Type.SIGN_CHANGE,blockListener,Priority.Normal,this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.Normal,this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE,entityListener,Priority.Normal,this);
 		*/
 	}
 	public boolean permit(Player player,String permission){ 
 		return player.hasPermission(permission);
 	}
 	public void out(String message) {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
 	}
 	public void crap(String message){
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " CRAP] " + message);
 	}
 	public void babble(String message){
 		if (!this.verbose){ return; }
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
 	}
 	public String plural(int number) {
 		if (number == 1){
 			return "";
 		}
 		else {
 			return "s";
 		}
 	}
 
 	public void loadConfig() {
 	    getConfig().options().copyDefaults(true);
 
 		verbose = getConfig().getBoolean("verbose", false);
 		currency = getConfig().getInt("currency",266);
 		currencyMaterial = Material.getMaterial(currency);
 		if (currencyMaterial == null){
 			crap("You have selected an invalid currency ("+currency+"), falling back to GOLD_INGOT!");
 			currency = 266;
 			currencyMaterial = Material.GOLD_INGOT;
 			currencyString = "Gold ingot";
 		}
 		else if (currencyMaterial.isEdible()){
 			crap("You've selected an edible currency.  Due to a bug, this won't work.  Falling back to GOLD_INGOT");
 			currency = 266;
 			currencyMaterial = Material.GOLD_INGOT;
 			currencyString = "gold ingot";
 		}
 		else {
 			currencyString = currencyMaterial.toString().replaceAll("_", " ").toLowerCase();
 		}
 		
 		baseCost = getConfig().getDouble("baseCost",0.01);
 		currencyName = getConfig().getString("currencyName","g");
 		partialRepair = getConfig().getBoolean("partialRepair", false);
 		distributePartialRepair = getConfig().getBoolean("distributePartialRepair", true);
 		
		saveConfig();
 	}
 
 }
