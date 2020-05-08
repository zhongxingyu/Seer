 /**
  * Copyright (C) 2012 t7seven7t
  */
 package net.t7seven7t.swornguard;
 
 import net.t7seven7t.swornguard.events.CheatEvent;
 import net.t7seven7t.swornguard.permissions.PermissionType;
 import net.t7seven7t.swornguard.types.CheatType;
 import net.t7seven7t.swornguard.types.PlayerData;
 import net.t7seven7t.util.FormatUtil;
 import net.t7seven7t.util.TimeUtil;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  * @author t7seven7t
  */
 public class CheatHandler {
 	private final SwornGuard plugin;
 	
 	public CheatHandler(SwornGuard plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void announceCheat(CheatEvent event) {
 		PlayerData data = plugin.getPlayerDataCache().getData(event.getPlayerName());
 		plugin.getServer().getPluginManager().callEvent(event);
 		
 		for (Player player : plugin.getServer().getOnlinePlayers()) {
 			if (plugin.getPermissionHandler().hasPermission(player, PermissionType.SHOW_CHEAT_REPORTS.permission)) {
 				player.sendMessage(ChatColor.RED + event.getMessage());
 				if (data.getJails() >= plugin.getConfig().getInt("jailsBeforeNotice") && plugin.getConfig().getBoolean("jailAmountNoticeEnabled"))
 					player.sendMessage(FormatUtil.format(plugin.getMessage("cheat_jail_notice"), event.getPlayerName(), data.getJails()));
 			}
 		}
 		
 		if ((plugin.getAutoModerator().isOnlyModOnline() && plugin.getConfig().getBoolean("autoModEnabled")) || event.getCheat() == CheatType.SPAM) {
 			plugin.getAutoModerator().manageCheatEvent(event);
 		}
 		
 		if (event.getCheat() != CheatType.XRAY || System.currentTimeMillis() - data.getLastXrayWarn() > 432000000L) {
 			data.getProfilerList().add(FormatUtil.format("[{0} GMT] &cpinged the cheat detector for: &6{1}", 
 					TimeUtil.getLongDateCurr(), WordUtils.capitalize(event.getCheat().toString().replaceAll("_", " "))));
 		}
 		
		if (event.getCheat() == CheatType.FLYING || event.getCheat() == CheatType.XRAY) {
 			// Add cheater to patrol list
 			plugin.getPatrolHandler().addCheater(event.getPlayerName());
 			
 			for (Player player : plugin.getServer().getOnlinePlayers()) {
 				if (plugin.getPermissionHandler().hasPermission(player, PermissionType.CMD_CHEAT_TELEPORT.permission))
 					player.sendMessage(ChatColor.RED + "To respond to this cheat alert use /ctp " + event.getPlayerName());
 			}
 		}
 		
 		plugin.getLogHandler().log("Cheatevent player: {0}", event.getPlayerName());
 		plugin.getLogHandler().log("Cheatevent type: {0}", event.getCheat().toString().replaceAll("_", " "));
 	}
 }
