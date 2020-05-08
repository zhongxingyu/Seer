 package com.github.secretcraft.java;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import net.windwaker.sql.Connection;
 import net.windwaker.sql.DataType;
 import net.windwaker.sql.Table;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.github.secretcraft.java.utils.Utils;
 
 public class FLCommand {
 	
 	private Connection connection;
 	private Table friend_list;
 	private FriendList plugin;
 	Logger log;
 	private Map<Player,Player> playerMap = null;
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public FLCommand(Logger log, FriendList plugin) {
 		connection = plugin.getConnection();
 		this.plugin = plugin;
 		this.log = log;
 		friend_list = new Table(connection, "friend_list");
 		playerMap = new HashMap<Player,Player>();
 		
 		try {
 			if(!(friend_list.exists())) {
 				
 				Map<String, DataType> columnDataTypeMap = new HashMap<String, DataType>();
				columnDataTypeMap.put("id", new DataType("INT NOT NULL AUTO_INCREMENT PRIMARY KEY"));
 				columnDataTypeMap.put("player", new DataType(DataType.TEXT));
 				columnDataTypeMap.put("friend", new DataType(DataType.TEXT));
 				friend_list.create(columnDataTypeMap);
 				
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public void addFriend(Player player, Player friend) {
 		
 		String name = friend.getName();
 		String friendName = "";
 		String playerName = "";
 		
 		try {
 			ResultSet res = connection.query("SELECT friend FROM friend_list WHERE player ='"+player.getName()+"' && friend = '" + friend.getName() + "'");
 			while (res.next()) {
 				friendName = res.getString("friend");
 				playerName = res.getString("player");
 				if(friendName.equalsIgnoreCase(name) && playerName.equalsIgnoreCase(player.getName())) {
 					player.sendMessage(ChatColor.RED + "Der Spieler: " + ChatColor.AQUA + name + ChatColor.RED +
 							" ist bereits in deiner Freundesliste!");
 					return;
 				}
 			}
 			
 			String[] fields = {"player", "friend"};
 			String offlinePlayer = plugin.getServer().getOfflinePlayer(name).getName();
 			String[] values = {player.getName(), offlinePlayer};
 			friend_list.add(fields, values);
 			
 		} catch(SQLException e) {
 			e.getStackTrace();
 		}
 		
 		player.sendMessage(ChatColor.GRAY + "Spieler: " + ChatColor.AQUA + name + ChatColor.GRAY + " hinzugefgt.");
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public void removeFriend(Player player, String name) {
 		
 		try {
 			
 			if(!(friend_list.containsValue("friend", name))) {
 				
 				player.sendMessage(ChatColor.RED + "Der Spieler: " + ChatColor.AQUA + name + ChatColor.RED +
 						" existiert nicht in deiner Freundesliste!");
 				return;
 				
 			}
 			
 			connection.update("DELETE FROM friend_list WHERE player = '" + player.getName() + "' && friend = '" + name + "'");
 			connection.update("DELETE FROM friend_list WHERE player = '" + name + "' && friend = '" + player.getName() + "'");
 			
 		} catch(SQLException e) {
 			e.getStackTrace();
 		}
 		if(plugin.playerIsOnline(name)) {
 			Player p = Utils.getPlayerByName(name);
 			p.sendMessage(ChatColor.GRAY + "Spieler: " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " entfernt.");
 		}
 		player.sendMessage(ChatColor.GRAY + "Spieler: " + ChatColor.AQUA + name + ChatColor.GRAY + " entfernt.");
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public void showList(Player player) {
 		
 		List<String> strList = new ArrayList<String>();
 		String tempMessageOnline = "";
 		String tempMessageOffline = "";
 		
 		try {
 			ResultSet results = Utils.getResults(player, connection);
 			String friend = "";
 			Player onlineFriend = null;
 			while(results.next()) {
 				friend = results.getString("friend");
 				strList.add(friend);
 			}
 			strList = Utils.sortAlphabetic(strList);
 			for(int i = 0; i < strList.size(); i++) {
 				onlineFriend = Utils.getPlayerByName(strList.get(i));
 				if(onlineFriend == null) {
 					tempMessageOffline += ChatColor.RED + strList.get(i) + ChatColor.GRAY + ", ";
 				} else {
 					String displayName = Utils.getDisplayNameFormat(onlineFriend);
 					tempMessageOnline = displayName + ChatColor.GRAY + ", ";
 				}
 			}
 			if((tempMessageOnline.equals("") || tempMessageOnline.length() == 0) && 
 					(tempMessageOffline.equals("") || tempMessageOffline.length() == 0)) {
 				
 				player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer!");
 				return;
 				
 			}
 			if(!tempMessageOnline.equals("")) {
 				tempMessageOnline = tempMessageOnline.substring(0, tempMessageOnline.length() - 2);
 				this.showOnline(player, tempMessageOnline);
 			}
 			if(!tempMessageOffline.equals("")) {
 				tempMessageOffline = tempMessageOffline.substring(0, tempMessageOffline.length() - 2);
 				this.showOffline(player, tempMessageOffline);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public void showOnlyOnline(Player player) {
 		
 		List<String> strList = new ArrayList<String>();
 		String tempMessage = "";
 		
 		try {
 			ResultSet results = Utils.getResults(player, connection);
 			String friend = "";
 			Player onlineFriend = null;
 			while(results.next()) {
 				friend = results.getString("friend");
 				strList.add(friend);
 			}
 			Utils.sortAlphabetic(strList);
 			for(int i = 0; i < strList.size(); i++) {
 				onlineFriend = Utils.getPlayerByName(strList.get(i));
 				if(onlineFriend == null) {
 					tempMessage += "";
 				} else {
 					String displayName = Utils.getDisplayNameFormat(onlineFriend);
 					tempMessage = displayName + ChatColor.WHITE + ", " + tempMessage;
 				}
 			}
 			if(tempMessage.equals("") || tempMessage.length() == 0) {
 				player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer, oder keiner deiner Freunde ist online.");
 				return;
 			}
 			tempMessage = tempMessage.substring(0, tempMessage.length() - 2);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.showOnline(player, tempMessage);
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public void playerAddMessage(Player player, String name) {
 		Player p = Utils.getPlayerByName(name);
 		if(p == null) {
 			player.sendMessage(ChatColor.RED + "Der Spieler " + name + " ist nicht online.");
 		}
 		player.sendMessage(ChatColor.AQUA + "Du hast den Spieler: " + Utils.getDisplayNameFormat(p) + ChatColor.AQUA + " in deine Freundesliste eingeladen.");
 		p.sendMessage(ChatColor.AQUA + "Der Spieler " + Utils.getDisplayNameFormat(player) + ChatColor.AQUA + " versucht dich");
 		p.sendMessage(ChatColor.AQUA + "in seine Freundesliste aufzunehmen.");
 		p.sendMessage(ChatColor.AQUA + "/fl accept - den Spieler in deine Freundesliste aufnehmen.");
 		p.sendMessage(ChatColor.AQUA + "/fl deny   - den Spieler nicht aufnehmen.");
 				
 		playerMap.put(p,player);
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	private void showOnline(Player player, String tempMessage) {
 		player.sendMessage(ChatColor.GOLD + "----Deine Freundesliste----");
		player.sendMessage(ChatColor.GREEN + "Online:");
 		player.sendMessage(tempMessage);
 	}
 	
 	private void showOffline(Player player, String tempMessage) {
		player.sendMessage(ChatColor.RED + "Offline:");
 		player.sendMessage(tempMessage);
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public Player getPlayer(Player player) {
 		return playerMap.get(player);
 	}
 	
 	//---------------------------------------------------------------------------------------------------------------
 	
 	public void removeRequest(Player player) {
 		playerMap.remove(player);
 	}
 	
 }
