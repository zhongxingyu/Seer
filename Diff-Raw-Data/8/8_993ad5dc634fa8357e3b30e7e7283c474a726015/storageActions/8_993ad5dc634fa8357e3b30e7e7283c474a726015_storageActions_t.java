 package com.Zolli.EnderCore.Storage;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.Zolli.EnderCore.API.IPlayerAction;
 import com.Zolli.EnderCore.Storage.Storage.storageEngine;
 import com.Zolli.EnderCore.Utils.tagHelper;
 
 public class storageActions implements IPlayerAction {
 
 	private Storage storage;
 	private Connection conn;
 	private storageEngine selectedEngine;
 	private tagHelper tagHelper;
 	private YamlConfiguration ffStorage;
 	
 	public storageActions(Storage s) {
 		this.storage = s;
 		this.tagHelper = new tagHelper();
 		this.selectedEngine = this.storage.getSelectedEngine();
 		conn = this.storage.getConnection();
 		
 		if(selectedEngine.equals(storageEngine.FLATFILE)) {
 			this.ffStorage = storage.getFlatFileStorage();
 		}
 	}
 	
 	public boolean addPlayer(Player pl) {
 		switch(this.selectedEngine) {
 			case MySQL:
 			case SQLITE:
 			case H2DB:
 				try {
 					PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `players` (playerName,dragonDefeated, specialTags) VALUES(?, ?, ?)");
 					pstmt.setString(1, pl.getName());
 					pstmt.setString(2, "0");
 					pstmt.setString(3, "");
 					pstmt.executeUpdate();
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			break;
 			case FLATFILE:
				if(!(this.ffStorage.contains(pl.getName()))) {
 					this.ffStorage.set(pl.getName() + ".dragonDefeted", "0");
 					this.ffStorage.set(pl.getName() + ".group", "");
 					return true;
 				}
 			break;
 		default:
 			break;
 		}
 		return false;
 	}
 	
 	public boolean setDefeated(Player pl, boolean b) {
 		switch(this.selectedEngine) {
 			case MySQL:
 			case SQLITE:
 			case H2DB:
 				try {
 					PreparedStatement pstmt = conn.prepareStatement("UPDATE `players` SET dragonDefeated = ? WHERE playerName = ?");
 					if(b) {
 						pstmt.setString(1, "1");
 						pstmt.setString(2, pl.getName());
 					} else {
 						pstmt.setString(1, "0");
 						pstmt.setString(2, pl.getName());
 					}
 					pstmt.executeUpdate();
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			break;
 			case FLATFILE:
 				if(b) {
 					this.ffStorage.set(pl.getName() + ".dragonDefeted", "true");
 				} else {
 					this.ffStorage.set(pl.getName() + ".dragonDefeted", "false");
 				}
 				return true;
 		default:
 			break;
 		}
 		return false;
 	}
 	
 	public boolean getDefeatStatus(Player pl) {
 		switch(this.selectedEngine) {
 			case MySQL:
 			case SQLITE:
 			case H2DB:
 				try {
 					PreparedStatement pstmt = conn.prepareStatement("SELECT dragonDefeated FROM `players` WHERE playerName = ? LIMIT 1");
 					pstmt.setString(1, pl.getName());
 					ResultSet rs = pstmt.executeQuery();
 					int result = 0;
 					
 					while(rs.next()) {
 						result = rs.getInt(1);
 					}
 					
 					if(result == 1) {
 						return true;
 					} else {
 						return false;
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				break;
 			case FLATFILE: 
 				return this.ffStorage.getBoolean(pl.getName() + ".dragonDefeted", false);
 		default:
 				break;
 		}
 		return false;
 	}
 	
 	public boolean setTag(Player pl, String key, String value) {
 		String insertString = key + "=" + value + ";";
 		
 		switch (this.selectedEngine) {
 		case MySQL:
 		case SQLITE:
 		case H2DB:
 			try {
 				Statement stmt = conn.createStatement();
 				int rows = stmt.executeUpdate("UPDATE `users` SET uniqueTags = CONCAT(uniqueTags, '" + insertString + "') WHERE playerName = " + pl.getName());
 				if(rows == 1) {
 					return true;
 				} else {
 					return false;
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return false;
 		case FLATFILE:
 			@SuppressWarnings("unchecked")
 			List<String> originalContent = (List<String>) this.ffStorage.getList(pl.getName() + ".specialTags");
 			this.ffStorage.set(pl.getName() + ".specialTags", originalContent.add(insertString));
 			break;
 		default:
 			break;
 		}
 		return false;
 	}
 	
 	public Map<String, String> getTag(Player pl) {
 		String object = null;
 		
 		switch (this.selectedEngine) {
 		case MySQL:
 		case SQLITE:
 		case H2DB:
 			try {
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT uniqueTags FROM `users` WHERE userName=" + pl.getName() + " LIMIT 1");
 				object = rs.getString(0);
 				stmt.close();
 				return tagHelper.createMap(object);
 				
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return null;
 		case FLATFILE:
 			@SuppressWarnings( "unchecked" )
 			List<String> content = (List<String>) this.ffStorage.get(pl.getName() + ".specailTags");
 			Iterator it = content.iterator();
 			String assambledList = null;
 			
 			while(it.hasNext()) {
 				assambledList = assambledList + it.next();
 			}
 			
 			return this.tagHelper.createMap(assambledList);
 		default:
 			break;
 		}
 		return null;
 	}
 }
