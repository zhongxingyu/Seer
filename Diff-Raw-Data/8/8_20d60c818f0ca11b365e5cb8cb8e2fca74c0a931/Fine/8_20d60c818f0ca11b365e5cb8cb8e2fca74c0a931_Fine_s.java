 package com.turt2live.antishare.money;
 
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.turt2live.antishare.ASUtils;
 import com.turt2live.antishare.AntiShare.LogType;
 import com.turt2live.antishare.metrics.TrackerList.TrackerType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 
 /**
 * Reward for doing something
  * 
  * @author turt2live
  */
 public class Fine extends Tender {
 
 	private double overcharge;
 
 	/**
 	 * Creates a new fine
 	 * 
 	 * @param type the type
 	 * @param amount the amount (positive to remove from account)
 	 * @param enabled true to enable
 	 * @param overcharge the amount to charge if the account has less than or equal to zero
 	 */
 	public Fine(TenderType type, double amount, boolean enabled, double overcharge){
 		super(type, amount, enabled);
 		this.overcharge = overcharge;
 	}
 
 	/**
 	 * Gets the overcharge value for this fine
 	 * 
 	 * @return the overcharge value
 	 */
 	public double getOverCharge(){
 		return overcharge;
 	}
 
 	@Override
 	public void apply(Player player){
 		if(!isEnabled() || plugin.getPermissions().has(player, PermissionNodes.MONEY_NO_FINE)){
 			return;
 		}
 
 		// Apply to account
 		double amount = getAmount();
 		if(plugin.getMoneyManager().getRawEconomyHook().requiresTab(player)){
 			amount = overcharge;
 		}
 		TransactionResult result = plugin.getMoneyManager().subtractFromAccount(player, amount);
 		if(!result.completed){
 			ASUtils.sendToPlayer(player, ChatColor.RED + "Fine Failed: " + ChatColor.ITALIC + result.message);
 			plugin.getMessenger().log("Fine Failed (" + player.getName() + "): " + result.message, Level.WARNING, LogType.BYPASS);
 			return;
 		}else{
 			String formatted = plugin.getMoneyManager().formatAmount(getAmount());
 			String balance = plugin.getMoneyManager().formatAmount(plugin.getMoneyManager().getBalance(player));
 			if(!plugin.getMoneyManager().isSilent(player.getName())){
 				ASUtils.sendToPlayer(player, ChatColor.RED + "You've been fined " + formatted + "!");
 				ASUtils.sendToPlayer(player, "Your new balance is " + ChatColor.YELLOW + balance);
 			}
 		}
 
 		// Increment statistic
 		plugin.getTrackers().getTracker(TrackerType.FINE_GIVEN).increment(1); // Does not have a name!
 	}
 
 }
