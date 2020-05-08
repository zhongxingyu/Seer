 package Commands.Trade;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import Main.EtriaCommands;
 
 public class TradeSession {
 	
 	private Player init;
 	private Player targ;
 	
 	private Inventory inv = null;
 	
 	private boolean initconf;
 	private boolean targconf;
 	
 	private int cancelTaskId;
 	
 	
 	public TradeSession(Player p, Player o) {
 		this.init = p;
 		this.targ = o;
 	}
 	
 	public void doTrade() {
 		for(ItemStack is : TradeCmd.getLowerContents(this.inv)) {
 			if (is == null) continue;
 			this.init.getInventory().addItem(is);
 		}
 		for (ItemStack is : TradeCmd.getUpperContents(this.inv)) {
 			if (is == null) continue;
 			this.targ.getInventory().addItem(is);
 		}
 		this.targ.sendMessage("aTrade Complete");
 		this.init.sendMessage("aTrade Complete");
 		terminate();
 	}
 	
 	public boolean isTradeSet() {
 		return (inv == null)? false : true;
 	}
 	
 	/*
 	 * Confirms Trade
 	 * 
 	 * p - Confirm this player
 	 */
 	public void confirmPlayer(Player p) {
 		if (p.equals(targ)) {
 			if (targconf) return;
 			this.targconf = true;
 			init.sendMessage("e" + targ.getName() + " aconfirmed the trade");
 		} else {
 			if (initconf) return;
 			this.initconf = true;
 			targ.sendMessage("e" + init.getName() + " aconfirmed the trade");
 		}
 		if (isConfirmed()) doTrade();
 	}
 	
 	/*
 	 * Returns if trade can continue
 	 */
 	public boolean isConfirmed() {
 		return (initconf && targconf);
 	}
 	
 	/*
 	 * Aborts trade, returns items
 	 */
 	public void abort() {
 		init.closeInventory();
 		targ.closeInventory();
 		//Tick Delay, makes sure all inventories are closed, possibly stopping duping.
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EtriaCommands.instance, new Runnable() {
 			@Override
 			public void run() {
 				for(ItemStack is : TradeCmd.getUpperContents(inv)) {
 					if (is == null) continue;
 					init.getInventory().addItem(is);
 				}
 				for (ItemStack is : TradeCmd.getLowerContents(inv)) {
 					if (is == null) continue;
 					targ.getInventory().addItem(is);
 				}
 			}
		}, 5L);
 		init.sendMessage("cTrade aborted");
 		targ.sendMessage("cTrade aborted");
 		terminate();
 	}
 	
 	/*
 	 * Sets trade sessions inventory
 	 * 
 	 * inv - inventory to set it to
 	 */
 	public void setInventory(Inventory inv) {
 		this.inv = inv;
 	}
 	
 	/*
 	 * Remove Trade Session
 	 */
 	public void terminate() {
 		unscheduleTradeCancel();
 		TradeCmd.trades.remove(new Pair(this.init, this.targ));
 		TradeCmd.traders.remove(init);
 	}
 	
 	/*
 	 * Send Traders summary of trade
 	 */
 	public void sendSummary() {
 		String accept = "aType e/trade confirma to confirm, or e/trade cancela to cancel";
 		ItemStack[] inithalf = TradeCmd.getUpperContents(inv);
 		ItemStack[] targhalf = TradeCmd.getLowerContents(inv);
 		String initlist = TradeCmd.getItemList(inithalf);
 		String targlist = TradeCmd.getItemList(targhalf);
 		init.sendMessage("aTradinge " + initlist + "afore " + targlist);
 		targ.sendMessage("aTradinge " + targlist + " afore " + initlist);
 		init.sendMessage(accept);
 		targ.sendMessage(accept);
 		scheduleTradeCancel();
 	}
 	
 	private void scheduleTradeCancel() {
 		this.cancelTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EtriaCommands.instance, new Runnable() {
 			
 			@Override
 			public void run() {
 				targ.sendMessage("cTrade Expired");
 				init.sendMessage("cTrade Expired");
 				abort();
 			}
 			
		}, 300L);
 	}
 	
 	private void unscheduleTradeCancel() {
 		Bukkit.getServer().getScheduler().cancelTask(this.cancelTaskId);
 	}
 
 }
