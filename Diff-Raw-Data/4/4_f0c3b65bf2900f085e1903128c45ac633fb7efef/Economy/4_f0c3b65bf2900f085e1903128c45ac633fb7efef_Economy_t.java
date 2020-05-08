 /******************************************************************************\
 |                                     ,,                                       |
 |                    db             `7MM                                       |
 |                   ;MM:              MM                                       |
 |                  ,V^MM.    ,pP"Ybd  MMpMMMb.  .gP"Ya `7Mb,od8                |
 |                 ,M  `MM    8I   `"  MM    MM ,M'   Yb  MM' "'                |
 |                 AbmmmqMA   `YMMMa.  MM    MM 8M""""""  MM                    |
 |                A'     VML  L.   I8  MM    MM YM.    ,  MM                    |
 |              .AMA.   .AMMA.M9mmmP'.JMML  JMML.`Mbmmd'.JMML.                  |
 |                                                                              |
 |                                                                              |
 |                                ,,    ,,                                      |
 |                     .g8"""bgd `7MM    db        `7MM                         |
 |                   .dP'     `M   MM                MM                         |
 |                   dM'       `   MM  `7MM  ,p6"bo  MM  ,MP'                   |
 |                   MM            MM    MM 6M'  OO  MM ;Y                      |
 |                   MM.    `7MMF' MM    MM 8M       MM;Mm                      |
 |                   `Mb.     MM   MM    MM YM.    , MM `Mb.                    |
 |                     `"bmmmdPY .JMML..JMML.YMbmd'.JMML. YA.                   |
 |                                                                              |
 \******************************************************************************/
 /******************************************************************************\
 | Copyright (c) 2012, Asher Glick                                              |
 | All rights reserved.                                                         |
 |                                                                              |
 | Redistribution and use in source and binary forms, with or without           |
 | modification, are permitted provided that the following conditions are met:  |
 |                                                                              |
 | * Redistributions of source code must retain the above copyright notice,     |
 |   this list of conditions and the following disclaimer.                      |
 | * Redistributions in binary form must reproduce the above copyright notice,  |
 |   this list of conditions and the following disclaimer in the documentation  |
 |   and/or other materials provided with the distribution.                     |
 |                                                                              |
 | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  |
 | AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    |
 | IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   |
 | ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    |
 | LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          |
 | CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         |
 | SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     |
 | INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      |
 | CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      |
 | ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   |
 | POSSIBILITY OF SUCH DAMAGE.                                                  |
 \******************************************************************************/
 package iggy.Economy;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Economy extends JavaPlugin{
   //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// GLOBAL DECLARATIONS ////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public static Economy plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public Map<String,Long> playerBanks = new HashMap<String,Long>();
 	
 	// prices of each block
 	// TODO: make a way of separating the blocks into categories that can be sold by salesNPCs
 	public Map<Material,Long> blockPrices = new HashMap<Material,Long>();
 	
 	// Keep a record of where to send the player back to when they leave the shop
 	public Map<Player,Location> shopReturnLocations = new HashMap<Player,Location>();
 	
 	// Initialize income events
 	public EventBasedIncome incomeEvents = new EventBasedIncome(this);
 	
 	// Initialize sign shop events
 	public SignShops signshops = new SignShops(this);
 	
 	PluginDescriptionFile pdFile;
 	String pluginName;
 	String pluginTitle;
 	
 	// World List
 	World shopworld;
 	World mainworld;
 	World thenether;
 	World endworld;
   //////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// ENABLE / DISABLE //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	@Override
 	public void onDisable() {
 		// save all the configuration info
 		saveMoney();
 		savePrices();
 		// reporth that the plugin is disabled
 		info("Version " + pdFile.getVersion() +" is disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		pdFile = this.getDescription();
 		pluginName = pdFile.getName();
 		pluginTitle = "[\033[0;32m"+pluginName+"\033[0m]";
 		
 		// create shop world
 		shopworld = Bukkit.getServer().getWorld("shopworld");
 		if (shopworld == null){
 			info("Shopworld not found, creating shopworld");
 			WorldCreator worldCreator = new WorldCreator("shopworld");
 			worldCreator.generator(new ShopGenerator());
 			shopworld = worldCreator.createWorld();
 			shopworld.setSpawnFlags(false, false);
 			shopworld.setPVP(false);
 			shopworld.setTime(0);
 			shopworld.setSpawnLocation(0, 65, 0);
 			info("Created shopworld");
 		}
 		// set world variables
 		mainworld = Bukkit.getServer().getWorld("world");
 		thenether = Bukkit.getServer().getWorld("world_nether");
 		endworld = Bukkit.getServer().getWorld("world_the_end");
 		
 		Bukkit.getServer().getPluginManager().registerEvents(signshops, this);
 		
 		// Load all the configuration info
 		loadMoney();
 		loadPrices();
 		// activate income events
 		Bukkit.getServer().getPluginManager().registerEvents(incomeEvents, this);
 		// report that the plugin is enabled
 		info("Version " + pdFile.getVersion() +" is enabled");
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// INPUT COMMANDS ///////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		//World world = player.getWorld();
 		/************************************ SHOP ************************************\
 		| This command will teleport the player to and from the shop world. It saves   |
 		| where they are and teleports them to the center of the shop world. If the    |
 		| player does not have a saved location when they try to teleport back they    |
 		| respawn at the world spawn point. It will not let players teleport to the    |
 		| shop if they are not in the main world or the nether                         |
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("shop")){
 			if (player == null) {
 				info("This command can only be run by a player");
 				return false;
 			}
 			else {
 				// Remove the player from any vehicle to avoid breaking movement code
 				if (player.isInsideVehicle()){player.getVehicle().eject();}
 				
 				// If the player is in the main world or nether teleport them to the shop
 				if (player.getWorld() == mainworld || player.getWorld() == thenether) {
 					shopReturnLocations.put(player,player.getLocation());
 					player.teleport(shopworld.getSpawnLocation());
 					player.sendMessage("Teleported to the shop");
 				}
 				
 				// If the player is in the shop world, return them to where they used to be
 				else if (player.getWorld() == shopworld){
 					if (shopReturnLocations.containsKey(player)) {
 						player.teleport(shopReturnLocations.get(player));
 						shopReturnLocations.remove(player);
 						player.sendMessage("Returned you to your world");
 					}
 					else {
 						player.teleport(mainworld.getSpawnLocation());
 						player.sendMessage("Your old location was corrupted, respawning");
 					}
 				}
 				
 				// If the player is in some other world, prevent them from entering the shop
 				else {
 					player.sendMessage("you cant go to the shop from here");
 				}
 			}
 		}
 		/************************************ PRICE ***********************************\
 		| This command gets the price of the item stack you are holding. If the block  |
 		| cannot be sold then the buy price is set to 0 and the sell price is not      |
 		| given. The sell price is multiplied by the quantity of the block as well     |
 		|                                                                              |
 		| TODO: allow the console to run this function with one argument as well       |
 		\******************************************************************************/
 		else if (commandLabel.equalsIgnoreCase("price")){
 			if (player == null) {
 				info("This command can only be run by a player");
 				return false;
 			}
 			// Set the default quantity of the item to be questioned
 			int amount = 1;
 			Material material;
 			// If no arguments get the item the player is holding
 			// If the player is not holding an item, get the item
 			// the player is pointing at
 			if (args.length == 0) {
 				amount = player.getItemInHand().getAmount();
 				if( amount > 0 ){
 					material = player.getItemInHand().getType();
 				}
 				else{
 					material = player.getTargetBlock(null, 16).getType();
 					amount = 1;
 				}
 			}
 			// if one argument try to find the material requested
 			else if (args.length == 1){
 				material = Material.matchMaterial(args[0]);
 				if (material == null) {
 					player.sendMessage("Unknown material " + args[0]);
 					return false;
 				}
 			}
 			// if more then 1 argument report error and exit
 			else {
 				player.sendMessage("You cannot list more then one item at a time");
 				return false;
 			}
 			long blockPrice = blockPrices.get(material);
 			if (blockPrice == -1) {
 				player.sendMessage(""+amount+" "+material.toString()+" will sell for $"+ChatColor.GREEN+"0"+ChatColor.WHITE+" and cannot be bought");
 			}
 			else {
 				player.sendMessage(""+amount+" "+material.toString()+" will sell for $"+ChatColor.GREEN+(amount*blockPrice/2)+ChatColor.WHITE+" and can be bought for $"+ChatColor.GREEN+(amount*blockPrice)+ChatColor.WHITE);
 			}
 		}
 		/************************************ MONEY ***********************************\
 		| Check to see how much money you have. If you are op, have the permission     |
 		| economy.moneymonitor or you are the console you can check the money of any   |
 		| player that you specify                                                      |
 		\******************************************************************************/
 		else if (commandLabel.equalsIgnoreCase("money")){
 			// if no player is specified return the caller's balance
 			if (args.length == 0){
 				if (player == null){
 					info("You need to type in a player name");
 				}
 				else {
 					long money = getMoney(player.getName());
 					player.sendMessage("You have $"+ChatColor.GREEN+money+ChatColor.WHITE+" in the bank");
 				}
 			}
 			// if a player is specified try to find that player and return the balance
 			else if (args.length == 1){
 				if (player == null){
 					String playername = getFullPlayerName(args[0]);
 					if (playername == null) return false;
 					long money = getMoney(playername);
 					info(playername+" has $"+money);
 				}
 				else if (player.hasPermission("economy.moneymonitor")||player.isOp()) {
 					String playername = getFullPlayerName(args[0]);
 					if (playername == null) return false;
 					long money = getMoney(playername);
 					player.sendMessage(playername+" has $"+ChatColor.GREEN+money+ChatColor.WHITE+" in the bank");
 				}
 			}
 		}
 		else if (commandLabel.equalsIgnoreCase("grant")) {
 			if (args.length != 2) {
 				if (player == null) {
 					info("Correct usage is /grant <player> <money>");
 				}
 				else {
 					player.sendMessage(" Correct usage is /grant <player> <money>");
 				}
 			}
 			
 			else if (args.length == 2) {
 				
 				// Get Player Name
 				String target = getFullPlayerName(args[0]);
 				if (target == null) {
 					if (player == null) info ("Player " + args[0] + " not found");
 					else player.sendMessage("Player " + args[0] + " not found");
 					return false;
 				}
 				// Get money to be charged
 				
 				long money = 0;
 				
 				
 				try {
 					money = (long)Long.parseLong(args[1]);
 				}
 				catch (Exception e) {
 					player.sendMessage("Could not compute the number " + args[1]);
 					return false;
 				}
 				
 				
 				// attempt to charge money
 				if (player != null) {
 					if (!player.isOp()) {
 						if (money < 0) {
 							player.sendMessage("You cannot steal money from other players");
							return false;
 						}
 						if (!chargeMoney(player.getName(), money)) {
 							player.sendMessage("You do not have "+ChatColor.GREEN+"$" + money + ChatColor.WHITE +" to give to " + target);
 							return false;
 						}
 					}
 				}
 				
 				
 				
 				giveMoney(target, money);
 				
 				//TODO make this able to send to the server or the player depending on who typed it
 				if (player != null) {
					player.sendMessage("You gave "+ChatColor.GREEN+"$" + money + ChatColor.WHITE + " to " + target);
 				}
 				
 				
 				
 				Player recipient = getServer().getPlayer(target);
 				if (recipient != null) {
 					String donersName = "";
 					if (player == null) {donersName = "The Server";}
 					else {donersName = player.getName();}
 					
 					recipient.sendMessage("You recieved "+ChatColor.GREEN+"$"+money+ChatColor.WHITE+" from "+ donersName);
 				}			
 			}
 		}
 		else if (commandLabel.equalsIgnoreCase("")){
 			
 		}
 		// return false if none of these commands are called (to make java happy)
 		return false;
 	}
 	/**************************** GET FULL PLAYER NAME ****************************\
 	| This is a helper function for the commands when trying to find a player.     |
 	| if a player is not found it returns null                                     |
 	\******************************************************************************/
 	public String getFullPlayerName (String name) {
 		String playername = name;
 		Player findplayer = Bukkit.getServer().getPlayer(name);
 		if (findplayer == null){
 			info("No online player found by the name " + name);
 			//return null;
 		}
 		else {
 			playername = findplayer.getName();
 		}
 		return playername;
 	}
   //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////// Money ///////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public void saveMoney() {
 		this.getConfig().set("banks", "");
 		Iterator<Entry<String, Long>> bankIterator =  playerBanks.entrySet().iterator();
 		while (bankIterator.hasNext()) {
 			Entry<String, Long> pairs = bankIterator.next();
 			this.getConfig().set("banks."+pairs.getKey(), pairs.getValue());
 		}
 		this.saveConfig();
 		info("Players' accounts saved");
 	}
 	public void loadMoney() {
 		playerBanks.clear();
 		ConfigurationSection bankConfig = this.getConfig().getConfigurationSection("banks");
 		
 		if (bankConfig == null) {
 			severe(" Failed to load bank accounts from config (banks section not found)");
 			return;
 		}
 		
 		Set<String> players = bankConfig.getKeys(false);
 		
 		if (players == null) {
 			severe(" Failed to load bank accounts from config (No players found)");
 			return;
 		}
 		
 		Iterator<String> it = players.iterator();
 		while (it.hasNext()) {
 			String player = it.next();
 			
 			long money = this.getConfig().getLong("banks."+player);
 			playerBanks.put(player, money);
 		}
 		info("Players' accounts loaded");
 	}
 	
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////// Prices ///////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////	
 	public void savePrices() {
 		this.getConfig().set("blocks", "");
 		Iterator<Entry<Material, Long>> blockIterator = blockPrices.entrySet().iterator();
 		while (blockIterator.hasNext()) {
 			Entry<Material, Long> pairs = blockIterator.next();
 			this.getConfig().set("blocks."+pairs.getKey().toString(), pairs.getValue());
 		}
 		this.saveConfig();
 		info("Block Prices Saved");
 	}
 	public void loadPrices() {
 		blockPrices.clear();
 		for (int i = 0; i < Material.values().length; i++) {
 			long price = -1;
 			blockPrices.put(Material.values()[i], price);
 		}
 		
 		ConfigurationSection blockConfig = this.getConfig().getConfigurationSection("blocks");
 		if (blockConfig == null) {
 			severe(" Failed to load Block prices from configuration (Blocks section not found)");
 			return;
 		}
 		
 		Set<String> blocks = blockConfig.getKeys(false);
 		if (blocks == null) {
 			severe(" Failed to load block prices from config (No blocks found)");
 			return;
 		}
 		
 		Iterator<String> it = blocks.iterator();
 		while (it.hasNext()) {
 			String blockname = it.next();
 			Material block = Material.getMaterial(blockname);
 			if (block == null) {
 				severe(" unknown block found in price list ("+blockname+")");
 				continue;
 			}
 			long price = this.getConfig().getLong("blocks."+blockname);
 			blockPrices.put(block, price);
 		}
 		
 		
 		info("Block Prices Loaded");
 	}
   //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Money Modification /////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	//the amount of money needed to change at once for any account to be saved
 	public long moneyDeadZone = 10;
 	/********************************** GET MONEY *********************************\
 	| This function returns the amount of money a player has in the bank, if they  |
 	| don't have a bank account yet the create account function is called to       |
 	| create one with the default amount of money                                  |
 	\******************************************************************************/
 	public long getMoney(String player) {
 		long playerMoney = 0;
 		if (playerBanks.containsKey(player)){
 			playerMoney = playerBanks.get(player);
 		}
 		else {
 			playerMoney = createAccount (player);
 		}
 		return playerMoney;
 	}
 	/********************************** SET MONEY *********************************\
 	| Sets a player's money to a specific value. This function does not do an      |
 	| error checking. It also does not save the money to the config because it     |
 	| cannot check to see if the amount of money added is above the 'deadzone'     |
 	\******************************************************************************/
 	public void setMoney(String player, long money) {
 		playerBanks.put(player, money);
 	}
 	/******************************* CREATE ACCOUNT *******************************\
 	| Creates a new user account for a player with a default amount of money. Then |
 	| returns the amount of money placed in the new player's account               |
 	\******************************************************************************/
 	public long createAccount(String player) {
 		long money = 10000;
 		playerBanks.put(player, money);
 		saveMoney();
 		return money;
 	}
 	/******************************** CHARGE MONEY ********************************\
 	| This function will charge money from the player, it will first check to see  |
 	| if the player has enough money and will return true if the money was         |
 	| successfully charged to the account. It will return false if it was not. If  |
 	| a player account cannot be found then it creates an account with the default |
 	| amount of money, then attempts to charge it                                  |
 	\******************************************************************************/
 	public boolean chargeMoney (Player player, long money) {return chargeMoney(player.getName(),money);}
 	public boolean chargeMoney (String player, long money) {
 		long playerMoney = getMoney(player);
 		
 		if (playerMoney >= money) {
 			playerBanks.put(player, playerMoney-money);
 			info (player+" was charged $"+money);
 			if (money > moneyDeadZone){
 				saveMoney();
 			}
 			return true;
 		}
 		return false;
 	}
 	/********************************* GIVE MONEY *********************************\
 	| This function gives money to the player, it will not first check the amount  |
 	| of money in the players account (only a problem if the player has more then  |
 	| 9000000000000000 money                                                       |
 	\******************************************************************************/
 	public boolean giveMoney (Player player,long money) {return giveMoney(player.getName(),money);}
 	public boolean giveMoney (String player,long money) {
 		info (player+" was given $"+money);
 		setMoney(player,getMoney(player)+money);
 		if (money > moneyDeadZone){
 			saveMoney();
 		}
 		return false;
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// DISPLAY HELPERS //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	/************************************ INFO ************************************\
 	| Info will display to the terminal prefacing it with the colored plugin title |
 	\******************************************************************************/
 	public void info(String input) {this.logger.info("  " + pluginTitle + " " + input);}
 	/*********************************** SEVERE ***********************************\
 	| Severe will display a severe message to the terminal window and color it red |
 	\******************************************************************************/
 	public void severe (String input) {this.logger.severe(pluginTitle+" \033[31m"+input+"\033[0m");}
 }
