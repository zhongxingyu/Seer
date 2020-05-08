 /**
  * (c) 2014 dmulloy2
  */
 package net.t7seven7t.swornguard.handlers;
 
 import net.t7seven7t.swornguard.SwornGuard;
 import net.t7seven7t.swornguard.permissions.PermissionType;
 import net.t7seven7t.swornguard.types.PlayerData;
 import net.t7seven7t.swornguard.types.Reloadable;
 import net.t7seven7t.swornguard.types.TrollType;
 import net.t7seven7t.util.FormatUtil;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginManager;
 
 import com.massivecraft.factions.Conf;
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.FPlayers;
 import com.massivecraft.factions.struct.ChatMode;
 
 /**
  * @author dmulloy2
  */
 
 public class TrollHandler implements Reloadable {
 	private final SwornGuard plugin;
 
 	public TrollHandler(SwornGuard plugin) {
 		this.plugin = plugin;
 	}
 
 	public final void putTrollInHell(Player troll, TrollType type) {
 		PlayerData data = plugin.getPlayerDataCache().getData(troll);
 
 		data.setTrollHell(true);
 
 		forceIntoPublicChat(troll);
 		hidePlayers(troll);
 
 		if (type == TrollType.MUTE) {
 			data.setTrollMuted(true);
 		} else if (type == TrollType.BAN) {
 			data.setTrollBanned(true);
 		}
 	}
 
 	public final void freeFromHell(Player troll, TrollType type) {
 		PlayerData data = plugin.getPlayerDataCache().getData(troll);
 
 		data.setTrollBanned(false);
 		data.setTrollMuted(false);
 
 		if (type == TrollType.HELL) {
 			data.setTrollHell(false);
 			showPlayers(troll);
 		}
 	}
 
 	public final void forceIntoPublicChat(Player troll) {
 		PlayerData data = plugin.getPlayerDataCache().getData(troll);
 		if (data.isTrollHell()) {
 			try {
 				PluginManager pm = plugin.getServer().getPluginManager();
 				if (pm.getPlugin("Factions") != null || pm.getPlugin("SwornNations") != null) {
 					if (Conf.factionOnlyChat) {
 						FPlayer fplayer = FPlayers.i.get(troll);
 						if (fplayer.getChatMode() != ChatMode.PUBLIC) {
 							fplayer.setChatMode(ChatMode.PUBLIC);
 						}
 					}
 				}
 			} catch (Throwable ex) {
 				// Probably a different version of Factions
 			}
 		}
 	}
 
 	public final void regulateChat(AsyncPlayerChatEvent event) {
 		PlayerData data = plugin.getPlayerDataCache().getData(event.getPlayer());
 		if (data.isTrollHell()) {
 			event.getRecipients().clear();
 			if (data.isTrollMuted() || data.isTrollBanned()) {
 				event.getRecipients().add(event.getPlayer());
 				return;
 			}
 
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				PlayerData data1 = plugin.getPlayerDataCache().getData(p);
 				if (data1.isTrollHell()) {
 					event.getRecipients().add(p);
 				}
 			}
 
 			String admMsg = FormatUtil.format(plugin.getMessage("troll_format"), event.getPlayer().getName(), event.getMessage());
 
 			String node = plugin.getPermissionHandler().getPermissionString(PermissionType.TROLL_SPY.permission);
 			plugin.getServer().broadcast(admMsg, node);
 		}
 	}
 
 	public final void handleJoin(PlayerJoinEvent event) {
 		PlayerData data = plugin.getPlayerDataCache().getData(event.getPlayer());
 		if (data.isTrollHell()) {
 			event.setJoinMessage(null);
 			plugin.getTrollHandler().hidePlayers(event.getPlayer());
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				PlayerData data1 = plugin.getPlayerDataCache().getData(p);
 				if (data1.isTrollHell() && ! data.isTrollMuted() && ! data.isTrollBanned()) {
 					p.sendMessage(event.getJoinMessage());
 					continue;
 				}
 
 				if (plugin.getPermissionHandler().hasPermission(p, PermissionType.TROLL_SPY.permission)) {
 					p.sendMessage(FormatUtil.format(plugin.getMessage("troll_join"), event.getPlayer().getName()));
 				}
 			}
 
 			hidePlayers(event.getPlayer());
 			forceIntoPublicChat(event.getPlayer());
 		}
 	}
 
 	public final void handleQuit(PlayerQuitEvent event) {
 		PlayerData data = plugin.getPlayerDataCache().getData(event.getPlayer());
 		if (data.isTrollHell()) {
 			event.setQuitMessage(null);
 			plugin.getTrollHandler().hidePlayers(event.getPlayer());
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				PlayerData data1 = plugin.getPlayerDataCache().getData(p);
 				if (data1.isTrollHell() && ! data.isTrollMuted() && ! data.isTrollBanned()) {
 					p.sendMessage(event.getQuitMessage());
 					continue;
 				}
 
 				if (plugin.getPermissionHandler().hasPermission(p, PermissionType.TROLL_SPY.permission)) {
					p.sendMessage(FormatUtil.format("troll_leave", event.getPlayer().getName()));
 				}
 			}
 		}
 
 		showPlayers(event.getPlayer());
 	}
 
 	public final void handleKick(PlayerKickEvent event) {
 		PlayerData data = plugin.getPlayerDataCache().getData(event.getPlayer());
 		if (data.isTrollHell()) {
 			event.setLeaveMessage(null);
 			plugin.getTrollHandler().hidePlayers(event.getPlayer());
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				PlayerData data1 = plugin.getPlayerDataCache().getData(p);
 				if (data1.isTrollHell() && ! data.isTrollMuted() && ! data.isTrollBanned()) {
 					p.sendMessage(event.getLeaveMessage());
 					continue;
 				}
 
 				if (plugin.getPermissionHandler().hasPermission(p, PermissionType.TROLL_SPY.permission)) {
					p.sendMessage(FormatUtil.format(plugin.getMessage("troll_join"), event.getPlayer().getName()));
 				}
 			}
 		}
 
 		showPlayers(event.getPlayer());
 	}
 
 	public final void hidePlayers(Player player) {
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 		if (data.isTrollHell()) {
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				PlayerData data1 = plugin.getPlayerDataCache().getData(p);
 				if (data1.isTrollHell() && ! data1.isTrollMuted() && ! data1.isTrollBanned()) {
 					continue;
 				}
 
 				player.hidePlayer(p);
 			}
 		}
 	}
 
 	public final void showPlayers(Player player) {
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 		if (data.isTrollHell()) {
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				PlayerData data1 = plugin.getPlayerDataCache().getData(p);
 				if (data1.isTrollHell() && ! data1.isTrollMuted() && ! data1.isTrollBanned()) {
 					continue;
 				}
 
 				player.showPlayer(p);
 			}
 		}
 	}
 
 	@Override
 	public void reload() {
 		//
 	}
 }
