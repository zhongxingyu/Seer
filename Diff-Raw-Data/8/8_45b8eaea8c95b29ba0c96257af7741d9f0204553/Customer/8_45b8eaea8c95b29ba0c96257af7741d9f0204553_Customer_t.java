 package dev.mCraft.Coinz.api.Coins;
 
 import org.bukkit.Bukkit;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dev.mCraft.Coinz.Coinz;
 import dev.mCraft.Coinz.GUI.TellerMenu.TellerPopup;
 import dev.mCraft.Coinz.api.Teller.TellerDepositEvent;
 import dev.mCraft.Coinz.api.Teller.TellerWithdrawEvent;
 
 public class Customer {
 	private Coinz plugin = Coinz.instance;
 	private TellerPopup tellerPopup = TellerPopup.hook;
 	
 	private SpoutItemStack copp;
 	private SpoutItemStack bron;
 	private SpoutItemStack silv;
 	private SpoutItemStack gold;
 	private SpoutItemStack plat;
 	
 	private SpoutPlayer player;
 	private PlayerInventory inv;
 	private SpoutItemStack stack;
 	private short dur;
 	private double oldAmount;
 	private TransactionResult result = TransactionResult.FAILED;
 	
 	private GenericTextField enter = tellerPopup.enter;
 	private GenericLabel balance = tellerPopup.amount;
 	
 	/**
 	 * Constructor for customer.
 	 * <br>
 	 * <br>
 	 * To use you need to do:
 	 * <br>
 	 * <code>private Customer customer = new Customer(Spoutplayer);</code>
 	 * @param player SpoutPlayer
 	 */
 	public Customer(SpoutPlayer player) {
 		this.player = player;
		this.inv = player.getInventory();
 	}
 	
 	public enum TransactionResult {
 		FAILED, IMPROPER_CHANGE, IMPROPER_WITHDRAW_AMOUNT, NOT_ENOUGH_COINS, NOT_ENOUGH_MONEY, SUCCESS
 	}
 	
 	
 	/**
 	 * Tries to deposit money into the players account and remove the coins from the inventory
 	 * @param deposit Double
 	 */
 	public void depositCoins(double deposit) {
 		double amount = deposit;
 		
 		if (hasEnoughCoins(amount)) {
 			oldAmount = amount;
 			
 			for (ItemStack item : inv.getContents()) {
 				if (item != null) {
 					stack = new SpoutItemStack(item);
 					dur = stack.getDurability();
 					
 					if (stack.isCustomItem()) {
 						if (dur == Coinz.CopperCoin.getDurability()) {
 							copp = stack;
 						}
 						
 						if (dur == Coinz.BronzeCoin.getDurability()) {
 							bron = stack;
 						}
 						
 						if (dur == Coinz.SilverCoin.getDurability()) {
 							silv = stack;
 						}
 						
 						if (dur == Coinz.GoldCoin.getDurability()) {
 							gold = stack;
 						}
 						
 						if (dur == Coinz.PlatinumCoin.getDurability()) {
 							plat = stack;
 						}
 					}
 				}
 			}
 			
 			if (plat != null && plat.getDurability() == Coinz.PlatinumCoin.getDurability()) {
 				while (plat.getAmount() >=1 && amount >= 1000) {
 					inv.removeItem(Coinz.PlatinumCoin);
 					amount = amount - 1000;
 					plat.setAmount(plat.getAmount() - 1);
 				}
 			}
 			
 			if (gold != null && gold.getDurability() == Coinz.GoldCoin.getDurability()) {
 				while (gold.getAmount() >= 1 && amount >= 100) {
 					inv.removeItem(Coinz.GoldCoin);
 					amount = amount - 100;
 					gold.setAmount(gold.getAmount() - 1);
 				}
 			}
 			
 			if (silv != null && silv.getDurability() == Coinz.SilverCoin.getDurability()) {
 				while (silv.getAmount() >= 1 && amount >= 10) {
 					inv.removeItem(Coinz.SilverCoin);
 					amount = amount - 10;
 					silv.setAmount(silv.getAmount() - 1);
 				}
 			}
 			
 			if (bron != null && bron.getDurability() == Coinz.BronzeCoin.getDurability()) {
 				while (bron.getAmount() >= 1 && amount >= 1) {
 					inv.removeItem(Coinz.BronzeCoin);
 					amount = amount - 1;
 					bron.setAmount(bron.getAmount() - 1);
 				}
 			}
 			
 			if (copp != null && copp.getDurability() == Coinz.CopperCoin.getDurability()) {
 				while (copp.getAmount() >= 1 && amount >= 0.1) {
 					inv.removeItem(Coinz.CopperCoin);
 					amount = amount - 0.1;
 					copp.setAmount(copp.getAmount() - 1);
 				}
 			}
 			
 			if (amount > 0) {
 				oldAmount = oldAmount - amount;
 				tellerPopup.attachWidget(plugin, tellerPopup.wrongChange);
 				result = TransactionResult.IMPROPER_CHANGE;
 				
 				while (oldAmount >= 1000) {
 					inv.addItem(Coinz.PlatinumCoin);
 					oldAmount = oldAmount - 1000;
 				}
 				
 				while (oldAmount >= 100) {
 					inv.addItem(Coinz.GoldCoin);
 					oldAmount = oldAmount - 100;
 				}
 				
 				while (oldAmount >= 10) {
 					inv.addItem(Coinz.SilverCoin);
 					oldAmount = oldAmount - 10;
 				}
 				
 				while (oldAmount >= 1) {
 					inv.addItem(Coinz.BronzeCoin);
 					oldAmount = oldAmount - 1;
 				}
 				
 				while (oldAmount >= 0.1) {
 					inv.addItem(Coinz.CopperCoin);
 					oldAmount = oldAmount - 0.1;
 				}
 			}
 			else {
				plugin.econ.depositPlayer(player.getName(), deposit);
				player.sendMessage(deposit + " has been added to your account");
 				enter.setText("");
 				balance.setText(plugin.econ.format(plugin.econ.getBalance(player.getName())));
 				result = TransactionResult.SUCCESS;
 			}
 		}
 		
 		else {
 			tellerPopup.attachWidget(plugin, tellerPopup.notEnoughC);
 			result = TransactionResult.NOT_ENOUGH_COINS;
 		}
 		
 		//Calling the deposit event
 		TellerDepositEvent depositEvent = new TellerDepositEvent(player, deposit, result);
 		Bukkit.getServer().getPluginManager().callEvent(depositEvent);
 	}
 	
 	
 	/**
 	 * Checks the players inventory to see if they have enough coins to deposit
 	 * @param amount Double
 	 * @return True if they have enough coins
 	 */
 	public boolean hasEnoughCoins(double amount) {
 		double coin = 0;
 		
 		for (ItemStack item : inv.getContents()) {
 			if (item != null) {
 				stack = new SpoutItemStack(item);
 				dur = stack.getDurability();
 				
 				if (stack.isCustomItem()) {
 					if (dur == Coinz.CopperCoin.getDurability()) {
 						coin = coin + (stack.getAmount() * 0.1);
 					}
 					
 					if (dur == Coinz.BronzeCoin.getDurability()) {
 						coin = coin + (stack.getAmount() * 1);
 					}
 					
 					if (dur == Coinz.SilverCoin.getDurability()) {
 						coin = coin + (stack.getAmount() * 10);
 					}
 					
 					if (dur == Coinz.GoldCoin.getDurability()) {
 						coin = coin + (stack.getAmount() * 100);
 					}
 					
 					if (dur == Coinz.PlatinumCoin.getDurability()) {
 						coin = coin + (stack.getAmount() * 1000);
 					}
 				}
 			}
 		}
 		
 		if (coin >= amount) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	
 	/**
 	 * Tries to withdraw money from a players account and add coins to their inventory
 	 * @param withdraw Double
 	 */
 	public void withdrawCoins(double withdraw) {
 		double amount = withdraw;
 			
 		if (plugin.econ.has(player.getName(), withdraw)) {
 			
 			double oldAmount = amount;
 			
 			while (amount >= 1000) {
 				inv.addItem(Coinz.PlatinumCoin);
 				amount = amount - 1000;
 			}
 			
 			while (amount >= 100) {
 				inv.addItem(Coinz.GoldCoin);
 				amount = amount - 100;
 			}
 			
 			while (amount >= 10) {
 				inv.addItem(Coinz.SilverCoin);
 				amount = amount - 10;
 			}
 			
 			while (amount >= 1) {
 				inv.addItem(Coinz.BronzeCoin);
 				amount = amount - 1;
 			}
 			
 			while (amount >= 0.1) {
 				inv.addItem(Coinz.CopperCoin);
 				amount = amount - 0.1;
 			}
 			
 			if (amount > 0) {
 				oldAmount = oldAmount - amount;
 				
 				tellerPopup.attachWidget(plugin, tellerPopup.invalidAmount);
 				enter.setText("");
 				result = TransactionResult.IMPROPER_WITHDRAW_AMOUNT;
 				
 				for (ItemStack item : inv.getContents()) {
 					if (item != null) {
 						stack = new SpoutItemStack(item);
 						dur = stack.getDurability();
 						
 						if (stack.isCustomItem()) {
 							if (dur == Coinz.CopperCoin.getDurability()) {
 								copp = stack;
 							}
 							
 							if (dur == Coinz.BronzeCoin.getDurability()) {
 								bron = stack;
 							}
 							
 							if (dur == Coinz.SilverCoin.getDurability()) {
 								silv = stack;
 							}
 							
 							if (dur == Coinz.GoldCoin.getDurability()) {
 								gold = stack;
 							}
 							
 							if (dur == Coinz.PlatinumCoin.getDurability()) {
 								plat = stack;
 							}
 						}
 					}
 				}
 				
 				while (oldAmount >= 1000) {
 					if (plat != null && plat.getAmount() >= 1) {
 						plat.setAmount(plat.getAmount() - 1);
 						oldAmount = oldAmount - 1000;
 					}
 				}
 				
 				while (oldAmount >= 100) {
 					if (gold != null && gold.getAmount() >= 1) {
 						gold.setAmount(gold.getAmount() - 1);
 						oldAmount = oldAmount - 100;
 					}
 				}
 				
 				while (oldAmount >= 10) {
 					if (silv != null && silv.getAmount() >= 1) {
 						silv.setAmount(silv.getAmount() - 1);
 						oldAmount = oldAmount - 10;
 					}
 				}
 				
 				while (oldAmount >= 1) {
 					if (bron != null && bron.getAmount() >= 1) {
 						bron.setAmount(bron.getAmount() - 1);
 						oldAmount = oldAmount - 1;
 					}
 				}
 					
 				while (oldAmount >= 0.1) {
 					if (copp != null && copp.getAmount() >= 1) {
 						copp.setAmount(copp.getAmount() - 1);
 						oldAmount = oldAmount - 0.1;
 					}
 				}
 			}
 			
 			else {
 				plugin.econ.withdrawPlayer(player.getName(), withdraw);
 				player.sendMessage(enter.getText() + " " + "has been taken from your account");
 				enter.setText("");
 				balance.setText(plugin.econ.format(plugin.econ.getBalance(player.getName())));
 				result = TransactionResult.SUCCESS;
 			}
 		}
 		
 		else {
 			tellerPopup.attachWidget(plugin, tellerPopup.notEnoughA);
 			result = TransactionResult.NOT_ENOUGH_MONEY;
 		}
 
 		//Calling the withdraw event
 		TellerWithdrawEvent withdrawEvent = new TellerWithdrawEvent(player, withdraw, result);
 		Bukkit.getServer().getPluginManager().callEvent(withdrawEvent);
 	}
 }
 
