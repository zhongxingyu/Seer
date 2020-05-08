 package uk.co.quartzcraft.kingdoms.kingdom;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.entity.QPlayer;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.entity.QKPlayer;
 
 public class Kingdom {
 	
 	private static QuartzKingdoms plugin;
 	
 	public void QuartzKingdomsConfig(QuartzKingdoms plugin) {
 		this.plugin = plugin;
 	}
 	
 	public static boolean createKingdom(String kingdomName, CommandSender sender) {
 		Player player = (Player) sender;
 		int userID = QPlayer.getUserID(player);
         int kuserID = QKPlayer.getID(player);
 		if(exists(kingdomName)) {
 			return false;
 		}
 		
 		if(userID == 0 | kuserID == 0) {
 			return false;
 		} 
 		
 		try {
 			java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 			java.sql.PreparedStatement s = connection.prepareStatement("INSERT INTO Kingdoms (KingdomName, KingID) VALUES ('" + kingdomName + "', " + kuserID + ");");
             s.executeUpdate();
             QKPlayer.joinKingdom(player, kingdomName);
             return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static boolean deleteKingdom(String kingdomName, CommandSender sender) {
 		String name_error = "name_error";
 		String king_error = "error";
 		String error = "error";
 		Player player = (Player) sender;
 		int userID = QKPlayer.getID(player);
 		
 		if(!exists(kingdomName)) {
 			return false;
 		}
 		
 		if(!QKPlayer.isKing(kingdomName, player)) {
 			return false;
 		}
 		
 		try {
 			java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 			java.sql.PreparedStatement s = connection.prepareStatement("DELETE FROM Kingdoms WHERE KingdomName='" + kingdomName + "' AND KingID='" + userID + "';");
             QKPlayer.leaveKingdom(player, kingdomName);
 			if(s.executeUpdate() == 1) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	public static boolean promotePlayer(String kingdomName, CommandSender sender, String playerToPromote, String group, Plugin plugin) {
 		String[] ranks = null;
 		ranks[0] = "Citizen";
         ranks[1] = "UpperClass";
 		ranks[2] = "Knight";
 		ranks[3] = "Noble";
         //ranks[4] = "King";
 		
 		int i = 1;
 		int a = 1;
 		int current = 0;
 		if(i == 1) {
             for(String rank : ranks) {
                 QPlayer.addSecondaryGroup(sender, playerToPromote, rank, false, plugin);
                 current++;
             }
 
 			for(String rank : ranks) {
 				if(group.equalsIgnoreCase(rank)) {
 					if(QPlayer.addSecondaryGroup(sender, playerToPromote, rank, true, plugin)) {
 						return true;
 					} else {
 						return false;
 					}
 				}
 				current++;
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
     public static boolean exists(int kingdomID) {
         java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
         try {
             java.sql.PreparedStatement s = connection.prepareStatement("SELECT * FROM Kingdoms WHERE id ='" + kingdomID + "';");
             ResultSet res2 = s.executeQuery();
             if(res2.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public static boolean exists(String kingdomName) {
         java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
         try {
             java.sql.PreparedStatement s = connection.prepareStatement("SELECT * FROM Kingdoms WHERE KingdomName ='" + kingdomName + "';");
             ResultSet res2 = s.executeQuery();
             if(res2.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 	
 	public static Map getInfo(String kingdomName) {
 		HashMap<String, String> info = new HashMap<String, String>();
 		
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
 			java.sql.PreparedStatement s = connection.prepareStatement("SELECT * FROM Kingdoms WHERE KingdomName =" + kingdomName + ";");
 			ResultSet res2 = s.executeQuery();
 			if(res2.next()) {
 				info.put("id", res2.getString(1));
 				info.put("Name", res2.getString(2));
 				info.put("Invite Only", res2.getString(3));
 				info.put("King", QPlayer.getDisplayName(QKPlayer.getCoreID(res2.getInt(4))));
 				info.put("Members", res2.getString(5));
 		    	 return info;
 		     } else {
 		    	 return null;
 		     }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static String getName(int id) {
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
 			Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			ResultSet res2 = s.executeQuery("SELECT * FROM Kingdoms WHERE id =" + id + ";");
 			if(res2.next()) {
 		    	 String kingdomName = res2.getString("KingdomName");
 		    	 return kingdomName;
 		     } else {
 		    	 return null;
 		     }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	        	
 	}
 	
 	public static int setRelationshipStatus(String kingdom, String relatingKingdom, int status) {
 		//TODO
 		switch(status) {
 			case 1:
                 //Neutral
                 try {
                     java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("SELECT FROM relationships WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                     ResultSet res = s.executeQuery();
                     if(isNeutral(kingdom, relatingKingdom)) {
                         return 0;
                     } else if(res.next() && res.getInt("kingdom_id") == getID(kingdom)) {
                         if(res.getString("status") == "11") {
                             java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE relationships SET status=1 WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                             if(s1.executeUpdate() == 1) {
                                 return 2;
                             } else {
                                 return 0;
                             }
                         } else {
                             java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE relationships SET status=11 WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                             if(s1.executeUpdate() == 1) {
                                 return 1;
                             } else {
                                 return 0;
                             }
                         }
                     } else {
                         java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("INSERT INTO relationships (kingdom_id, sec_kingdom_id, status) VALUES (" + getID(kingdom) + ", " + getID(relatingKingdom) + ", 11);");
                         if(s1.executeUpdate() == 1) {
                             return 1;
                         } else {
                             return 0;
                         }
                     }
                 }  catch (SQLException e) {
                    e.printStackTrace();
                     return 0;
                 }
 			case 2:
 				//Ally
                 try {
                     java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("SELECT FROM relationships WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                     ResultSet res = s.executeQuery();
                     if(isNeutral(kingdom, relatingKingdom)) {
                         return 0;
                     } else if(res.next()) {
                         if(res.getString("status") == "11") {
                             java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE relationships SET status=2 WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                             if(s1.executeUpdate() == 1) {
                                 return 2;
                             } else {
                                 return 0;
                             }
                         } else {
                             java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE relationships SET status=22 WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                             if(s1.executeUpdate() == 1) {
                                 return 1;
                             } else {
                                 return 0;
                             }
                         }
                     } else {
                         java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("INSERT INTO relationships (kingdom_id, sec_kingdom_id, status) VALUES (" + getID(kingdom) + ", " + getID(relatingKingdom) + ", 22);");
                         if(s1.executeUpdate() == 1) {
                             return 1;
                         } else {
                             return 0;
                         }
                     }
                 }  catch (SQLException e) {
                    e.printStackTrace();
                     return 0;
                 }
 			case 3:
 				//War
                 try {
                     java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("SELECT FROM relationships WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                     ResultSet res = s.executeQuery();
                     if(isNeutral(kingdom, relatingKingdom)) {
                         return 0;
                     } else if(res.next()) {
                         if(res.getString("status") == "11") {
                             java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE relationships SET status=3 WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                             if(s1.executeUpdate() == 1) {
                                 return 2;
                             } else {
                                 return 0;
                             }
                         } else {
                             java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE relationships SET status=33 WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + ";");
                             if(s1.executeUpdate() == 1) {
                                 return 1;
                             } else {
                                 return 0;
                             }
                         }
                     } else {
                         java.sql.PreparedStatement s1 = QuartzKingdoms.MySQLking.openConnection().prepareStatement("INSERT INTO relationships (kingdom_id, sec_kingdom_id, status) VALUES (" + getID(kingdom) + ", " + getID(relatingKingdom) + ", 33);");
                         if(s1.executeUpdate() == 1) {
                             return 1;
                         } else {
                             return 0;
                         }
                     }
                 }  catch (SQLException e) {
                    e.printStackTrace();
                     return 0;
                 }
 			default:
 				//Do nothing
 				return 0;
 		}
 		
 	}
 
     public static boolean isNeutral(String kingdom, String relatingKingdom) {
         try {
             java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("SELECT FROM relationships WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + " AND status=1;");
             ResultSet res = s.executeQuery();
             if(res.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         }
     }
 
     public static boolean isEnemy(String kingdom, String relatingKingdom) {
         try {
             java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("SELECT FROM relationships WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + " AND status=3;");
             ResultSet res = s.executeQuery();
             if(res.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         }
     }
 
     public static boolean isAlly(String kingdom, String relatingKingdom) {
         try {
             java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("SELECT FROM relationships WHERE kingdom_id=" + getID(kingdom) + " AND sec_kingdom_id=" + getID(relatingKingdom) + " AND status=2;");
             ResultSet res = s.executeQuery();
             if(res.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         }
     }
 	
 	public static boolean setOpen(String kingdomName, boolean status) {
 		//TODO
 		if(status) {
             try {
                 java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE Kingdoms SET invite_only=0 WHERE id=" + getID(kingdomName) + ";");
                 if(s.executeUpdate() == 1) {
                     return true;
                 } else {
                     return false;
                 }
             } catch (SQLException e) {
                 return false;
             }
 		} else {
             try {
                 java.sql.PreparedStatement s = QuartzKingdoms.MySQLking.openConnection().prepareStatement("UPDATE Kingdoms SET invite_only=1 WHERE id=" + getID(kingdomName) + ";");
                 if(s.executeUpdate() == 1) {
                     return true;
                 } else {
                     return false;
                 }
             } catch (SQLException e) {
                 return false;
             }
 		}
 	}
 
     public static boolean isOpen(String kingdomName) {
         java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
         try {
             Statement s = connection.createStatement();
             ResultSet res2 = s.executeQuery("SELECT * FROM Kingdoms WHERE KingdomName ='" + kingdomName + "';");
             if(res2.next()) {
                 int open = res2.getInt("invite_only");
                 if(open == 0) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
 	public static int getID(String kingdomName) {
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
 			Statement s = connection.createStatement();
 			ResultSet res2 = s.executeQuery("SELECT * FROM Kingdoms WHERE KingdomName ='" + kingdomName + "';");
 			if(res2.next()) {
 		    	 int id = res2.getInt("id");
 		    	 return id;
 		     } else {
 		    	 return 0;
 		     }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 	}
 
 	public static boolean addUser(Player player) {
 		// TODO Probably not going to use this
 		return true;
 	}
 
     public static boolean removeUser(Player player) {
         // TODO Probably not going to use this
         return true;
     }
 
 	public static String getKing(String kingdomName) {
 		int id = Kingdom.getID(kingdomName);
         Player player = null;
         OfflinePlayer oplayer = null;
         String playerName = null;
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
 			Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			ResultSet res2 = s.executeQuery("SELECT * FROM Kingdoms WHERE id ='" + id + "';");
 			if(res2.next()) {
 		    	int kingID = res2.getInt("KingID");
 		    	int coreKingID = QKPlayer.getCoreID(kingID);
                 if(Bukkit.getOfflinePlayer(QPlayer.getDisplayName(coreKingID)).isOnline()) {
                     player = Bukkit.getServer().getPlayer(QPlayer.getDisplayName(coreKingID));
                     playerName = player.getName();
                 } else {
                     oplayer = Bukkit.getOfflinePlayer(QPlayer.getDisplayName(coreKingID));
                     playerName = oplayer.getName();
                 }
 		        return playerName;
 		    } else {
 		        return null;
 		    }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static boolean compareKingdom(Player p1, Player p2) {
 		if(QKPlayer.getKingdomID(p1) == QKPlayer.getKingdomID(p2)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
     public static int getPower(String kingdomName) {
         try {
             Statement s2 = QuartzKingdoms.MySQLking.openConnection().createStatement();
             Statement s3 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 
             ResultSet res1 = s2.executeQuery("SELECT * FROM Kingdoms WHERE kingdomName ='" + kingdomName + "';");
 
             if(res1.next()) {
                 int power = res1.getInt("Power");
                 return power;
             } else {
                 return 0;
             }
 
         } catch(SQLException e) {
             return 0;
         }
     }
 
     public static boolean setPower(String kingdomName, boolean addRemove, int amount) {
         int currentPower = Kingdom.getPower(kingdomName);
         int powerAmount;
         if(addRemove) {
             powerAmount = currentPower + amount;
         } else {
             powerAmount = currentPower - amount;
         }
         if(powerAmount > 50) {
             powerAmount = 50;
         } else if(powerAmount < -50) {
             powerAmount = -50;
         }
         try {
             java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
             java.sql.PreparedStatement s = connection.prepareStatement("UPDATE Kingdoms SET Power=" + powerAmount + " WHERE id=" + Kingdom.getID(kingdomName) + ";");
             if(s.executeUpdate() == 1) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         }
     }
 }
