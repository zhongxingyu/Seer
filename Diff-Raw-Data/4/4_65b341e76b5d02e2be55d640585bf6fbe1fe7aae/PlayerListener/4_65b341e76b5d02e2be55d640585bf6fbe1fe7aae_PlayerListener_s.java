 package com.gmail.snipsrevival.listeners;
 
 import java.io.File;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 import com.gmail.snipsrevival.AdminAid;
 import com.gmail.snipsrevival.CommonUtilities;
 import com.gmail.snipsrevival.ConfigValues;
 import com.gmail.snipsrevival.Updater;
 import com.gmail.snipsrevival.Updater.VersionCheckException;
 import com.gmail.snipsrevival.utilities.FileUtilities;
 
 public class PlayerListener implements Listener {
 	
 	private AdminAid plugin;
 	private CommonUtilities common;
 	
 	public PlayerListener(AdminAid instance) {
 		plugin = instance;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 			
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		
 		common = new CommonUtilities(plugin);
 
 		final Player player = event.getPlayer();
 		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
 		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
 		List<String> mailListNew = userFile.getStringList("NewMail");
 		List<String> mailListRead = userFile.getStringList("ReadMail");
 		String ipAddress = player.getAddress().getHostName();
 		
 		if(player.isOp()) {
 			Updater updater = new Updater(plugin);
 			try {
 				if(!updater.isLatest() && plugin.getConfig().getBoolean("EnableVersionChecker") == true) {
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 						public void run() {
 							player.sendMessage(ChatColor.RED + "There is a newer version of AdminAid available");
 						}
 					});
 				}
 			} 
 			catch (VersionCheckException e) {
 			}
 		}
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				for(String line : new ConfigValues(plugin).getLoginMessages(player)) {
 					player.sendMessage(line);
 				}
 			}
 		});
 		
 		if(player.hasPermission("adminaid.banexempt")) userFile.set("BanExempt", true);
 		if(player.hasPermission("adminaid.muteexempt")) userFile.set("MuteExempt", true);
 		if(player.hasPermission("adminaid.kickexempt")) userFile.set("KickExempt", true);	
 		if(player.hasPermission("adminaid.staffmember")) userFile.set("StaffMember", true);
 		if(!player.hasPermission("adminaid.banexempt")) userFile.set("BanExempt", false);
 		if(!player.hasPermission("adminaid.muteexempt")) userFile.set("MuteExempt", false);
 		if(!player.hasPermission("adminaid.kickexempt")) userFile.set("KickExempt", false);
 		if(!player.hasPermission("adminaid.staffmember")) userFile.set("StaffMember", false);
 		if(userFile.get("ChatSpy") == null) userFile.set("ChatSpy", false);
 		userFile.set("IPAddress", ipAddress);
 		userFile.set("NewMail", mailListNew);
 		userFile.set("ReadMail", mailListRead);
 		FileUtilities.saveYamlFile(userFile, file);
 		
 		if(player.hasPermission("adminaid.mail.read")) {
 			if(!mailListNew.isEmpty()) {
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 					public void run() {
 						player.sendMessage(ChatColor.GREEN + "You have unread mail in your mailbox");
 						player.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/mail read " + ChatColor.GREEN + "to read your mail");
 					}
 				});
 				return;
 			}
 			if(plugin.getConfig().getBoolean("AlwaysNotifyMailboxMessage") == true ) {
 				if(!mailListRead.isEmpty()) {
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 						public void run() {
 							player.sendMessage(ChatColor.GREEN + "You have mail in your mailbox");
 							player.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/mail read " + ChatColor.GREEN + "to read your mail");
 						}
 					});
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		
 		common = new CommonUtilities(plugin);
 		
 		Player player = event.getPlayer();
 		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
 		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
 		String ipAddress = player.getAddress().getHostName();
 		Location loc = player.getLocation();
 		int xCoord = loc.getBlockX();
 		int yCoord = loc.getBlockY();
 		int zCoord = loc.getBlockZ();
 		String world = loc.getWorld().getName();
 	
 		if(player.hasPermission("adminaid.banexempt")) userFile.set("BanExempt", true);
 		if(player.hasPermission("adminaid.muteexempt")) userFile.set("MuteExempt", true);
 		if(player.hasPermission("adminaid.kickexempt")) userFile.set("KickExempt", true);	
 		if(player.hasPermission("adminaid.staffmember")) userFile.set("StaffMember", true);
 		if(!player.hasPermission("adminaid.banexempt")) userFile.set("BanExempt", false);
 		if(!player.hasPermission("adminaid.muteexempt")) userFile.set("MuteExempt", false);
 		if(!player.hasPermission("adminaid.kickexempt")) userFile.set("KickExempt", false);
 		if(!player.hasPermission("adminaid.staffmember")) userFile.set("StaffMember", false);
 		if(userFile.get("ChatSpy") == null) userFile.set("ChatSpy", false);
 		userFile.set("IPAddress", ipAddress);
 		userFile.set("Location.X", xCoord);
 		userFile.set("Location.Y", yCoord);
 		userFile.set("Location.Z", zCoord);
 		userFile.set("Location.World", world);
 		userFile.set("NewMail", mailListNew);
 		userFile.set("ReadMail", mailListRead);
 		FileUtilities.saveYamlFile(userFile, file);
 		
 		while(AdminAid.lastSender.values().remove(player.getName())) {}
 		if(AdminAid.staffChat.contains(player.getName())) {
 			AdminAid.staffChat.remove(player.getName());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		
 		common = new CommonUtilities(plugin);
 		
 		Player player = event.getPlayer();
 		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
 		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
 		
 		if(common.isPermaBanned(player)) {
 			event.setResult(Result.KICK_BANNED);
 			String defaultMessage = "permanently banned from this server";
 			event.setKickMessage("You are " + userFile.getString("PermaBanReason", defaultMessage));
 		}
 		else if(common.isTempBanned(player)) {
 			String defaultMessage = "temporarily banned from this server";
 			if(System.currentTimeMillis()/1000 >= userFile.getDouble("TempBanEnd")) {
 				userFile.set("TempBanned", null);
 				userFile.set("TempBanReason", null);
 				userFile.set("TempBanEnd", null);
 				FileUtilities.saveYamlFile(userFile, file);
 			}
 			else {
 				event.setResult(Result.KICK_BANNED);
 				event.setKickMessage("You are " + userFile.getString("TempBanReason", defaultMessage));
 			}
 		}
 	}
 }
