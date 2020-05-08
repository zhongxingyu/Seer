 package me.TfT02.Assassin.Listeners;
 
 import me.TfT02.Assassin.Assassin;
 import me.TfT02.Assassin.util.MessageScrambler;
 import me.TfT02.Assassin.util.PlayerData;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatListener implements Listener {
 	Assassin plugin;
 
 	public ChatListener(Assassin instance) {
 		plugin = instance;
 	}
 
 	private PlayerData data = new PlayerData(plugin);
 	private MessageScrambler message = new MessageScrambler(plugin);
 
 //	private final Random random = new Random();
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
 	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String pName = ChatColor.DARK_RED + "[ASSASSIN]: " + ChatColor.RESET;
 		String msg = event.getMessage();
 
 		if (msg == null) return;
 		if (data.isAssassin(player)) {
 			if (data.getAssassinChatMode(player)) {
 //			String number = data.getAssassinNumber(player);
 				String number = "?";
				String prefix = ChatColor.DARK_RED + "(#" + number + ") " + ChatColor.RESET;
 				for (Player assassin : data.getOnlineAssassins()) {
 					assassin.sendMessage(prefix + msg);
 					event.setCancelled(true);
 				}
 
 				//When in chatting in Assassin chat, other players who are near can hear scrambled chat.
 
 //				float diceroll = random.nextInt(100);
 //				int chance = plugin.getConfig().getInt("Assassin.messages_chance");
 //				if (chance > 0 && chance < diceroll){
 				double chatDistance = 250;
 				if (chatDistance > 0) {
 					for (Player players : Assassin.getInstance().getServer().getOnlinePlayers()) {
 						if (players.getWorld() != player.getWorld() || players.getLocation().distance(player.getLocation()) > chatDistance) {
 							event.getRecipients().remove(players);
 						} else {
 							if (!data.isAssassin(players)) {
 								//Assassins have already received unscrambled message
 								//But this isn't nessecary here... I think
 //								for (Player assassin : data.getOnlineAssassins()) {
 //									event.getRecipients().remove(assassin);
 //								}
 								//Show scrambled chat messages
 								String scrambled = message.Scrambled(msg);
 								event.setFormat(pName + scrambled);
 								players.sendMessage(prefix + msg);//TODO FIX
 								event.setCancelled(true);
 							}
 						}
 					}
 
 				}
 				//If an Assassin chats, but not in Assassin chat, show normal message with pName formatting
 			} else {
 				event.setFormat(pName + msg);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onDeath(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		String name = player.getName();
 		EntityDamageEvent de = player.getLastDamageCause();
 		String deathmessage = event.getDeathMessage();
 
 		boolean isEntityInvolved = false;
 		if (de instanceof EntityDamageByEntityEvent) {
 			isEntityInvolved = true;
 		}
 		if (isEntityInvolved) {
 			EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) de;
 			Entity damager = edbe.getDamager();
 			if (damager instanceof Player){
 				if (data.isAssassin(player)) {
 					String newmsg = deathmessage.replaceAll(name, ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
 					event.setDeathMessage(newmsg);
 				}
 				if (data.isAssassin((Player) damager)) {
 					String damagername = ((HumanEntity) damager).getName();
					String newmsg1 = deathmessage.replaceAll(damagername, ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
					String newmsg2 = newmsg1.replaceAll(name, ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
					event.setDeathMessage(newmsg2);
 				}
 			}
 		} else if (data.isAssassin(player)) {
 			String newmsg = deathmessage.replaceAll(name, ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
 			event.setDeathMessage(newmsg);
 		}
 	}
 }
