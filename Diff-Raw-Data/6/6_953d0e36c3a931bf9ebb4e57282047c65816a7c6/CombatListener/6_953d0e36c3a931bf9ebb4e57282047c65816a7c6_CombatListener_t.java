 package net.milkbowl.combatevents.pvp;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import net.milkbowl.combatevents.CombatEventsCore;
 import net.milkbowl.combatevents.CombatReason;
 import net.milkbowl.combatevents.LeaveCombatReason;
 import net.milkbowl.combatevents.listeners.CombatEventsListener;
 import net.milkbowl.combatevents.events.EntityKilledByEntityEvent;
 import net.milkbowl.combatevents.events.PlayerLeaveCombatEvent;
 
 public class CombatListener extends CombatEventsListener {
 
 	private CombatEventsCore ceCore;
 	private CombatEventsPvP plugin;
 	CombatListener(CombatEventsCore ceCore, CombatEventsPvP plugin) {
 		this.ceCore = ceCore;
 		this.plugin = plugin;
 	}
 
 	public void onPlayerLeaveCombat(PlayerLeaveCombatEvent event) {
 
 		if (Config.isPunish() && (event.getReason().equals(LeaveCombatReason.QUIT) || event.getReason().equals(LeaveCombatReason.KICK)) ) {
 			for (CombatReason reason : event.getCombatReasons())
 				if (reason.equals(CombatReason.DAMAGED_BY_PLAYER) || reason.equals(CombatReason.ATTACKED_PLAYER)) {
 					Location dropLoc = ceCore.getCombatPlayer(event.getPlayer()).getLastLocation();
 					ItemStack[] drops = ceCore.getCombatPlayer(event.getPlayer()).getInventory();
 
					for (int i = 0; i < drops.length; i++) {
						if (drops[i] == null)
							continue;
 						dropLoc.getWorld().dropItemNaturally(dropLoc, drops[i]);
					}
 
 					event.getPlayer().damage(1000);
 					event.getPlayer().getInventory().clear();
 					plugin.punishSet.add(event.getPlayer().getName());
 					Config.saveConfig(plugin);
 				}
 		}
 	}
 
 	@Override
 	public void onEntityKilledByEntity(EntityKilledByEntityEvent event) {
 		if (event.getAttacker() instanceof Player && event.getKilled() instanceof Player) {
 			Player attacker = (Player) event.getAttacker();
 			Player killed = (Player) event.getKilled();
 			//If we have global PvP messages active - lets send a random one
 			if (Config.isGlobalPvPMessage()) {
 				String message = null;
 				//Check if there is more than 1 message in the list
 				if (Config.getGlobalPvPMessages().size() > 1) {
 					int rand = new Random().nextInt(Config.getGlobalPvPMessages().size() - 1);
 					message = Config.getGlobalPvPMessages().get(rand);
 				} else if (Config.getGlobalPvPMessages().size() == 1)
 					message = Config.getGlobalPvPMessages().get(0);
 
 				if (message != null)
 					for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 						player.sendMessage(ChatColor.YELLOW + formatMessage(message, new String[] {"%ATTACKER%", "%KILLED%"}, new String[] {attacker.getName(), killed.getName()}));
 					}
 			}
 			
 			double reward = Config.getReward();
 			if (reward > 0) {
 				double killedBalance = CombatEventsPvP.econ.getBalance(killed.getName());
 				if (Config.getRewardType().equals("flat")) {
 					//check to make sure player has enough money to pay out the player
 					if (killedBalance < reward)
 						reward = killedBalance;
 				} else if (Config.getRewardType().equals("percent")) {
 					reward = (reward / 100) * killedBalance;
 				}
 
 				CombatEventsPvP.econ.withdrawPlayer(killed.getName(), reward);
 				CombatEventsPvP.econ.depositPlayer(attacker.getName(), reward);
 				//Send our messages
 				attacker.sendMessage(formatMessage(Config.getKillerMessage(), new String[] {"%PLAYER%", "%REWARD%"}, new String[] {killed.getName(), CombatEventsPvP.econ.format(reward)}));
 				killed.sendMessage(formatMessage(Config.getDeathMessage(), new String[] {"%PLAYER%", "%REWARD%"}, new String[] {attacker.getName(), CombatEventsPvP.econ.format(reward)}));
 			}
 		}
 	}
 
 	private String formatMessage(String message, String[] replaceText, String[] replacers) {
 		for (int i = 0; i < replaceText.length; i++) {
 			message = message.replace(replaceText[i], replacers[i]);
 		}
 		return message;
 	}
 
 }
