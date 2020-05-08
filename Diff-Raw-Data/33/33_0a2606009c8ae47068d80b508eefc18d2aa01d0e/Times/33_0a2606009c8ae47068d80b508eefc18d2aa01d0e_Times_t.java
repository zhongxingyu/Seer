 package com.gtdclan.signtimer;
 
 import java.util.List;
 import java.util.logging.Level;
 
 import com.avaje.ebean.Query;
 
 public class Times {
 	
 	private final Main plugin;
 	
 	public Times(Main instance) {
 		this.plugin = instance;
 	}
 	
 	public void addTime(String playerName, int time) {
 		String message;
 		DB data = this.plugin.database.getDatabase().find(DB.class).where().ieq("Playername", playerName).findUnique();
 		if (data == null) {
 			data = new DB();
 			data.setPlayername(playerName);
 			data.setTime(time);
 			message = "Your time has been saved.";
 			this.plugin.database.getDatabase().save(data);
 		}
 		else {
 			Long savedTime = data.getTime();
 			
 			if (time < savedTime) {
 				message = "New personal best. " + this.plugin.Util.getFriendly(Integer.valueOf(time));
 				data.setTime(time);
 				this.plugin.database.getDatabase().save(data);
 			}
 			else if (time == savedTime) {
 				message = "You matched your best time of " + this.plugin.Util.getFriendly(Integer.valueOf(time));
 			}
 			else {
 				Integer difference = (int) (time - savedTime);
 				String fasterTime = this.plugin.Util.getFriendly(difference);
 				message = "Your old time was " + fasterTime + " faster.";
 			}
 		}
 		
 		this.updateRanks();
 		this.plugin.Util.messagePlayer(playerName, message);
 	}
 	
 	public void getTime(String playerName) {
 		String message;
		String playerInfo = this.plugin.rankList.get(playerName);
 		if (playerInfo != null) {
			String[] pInfo = this.plugin.rankList.get(playerName).split(",");
 			String playerTime = this.plugin.Util.getFriendly(Integer.valueOf(pInfo[1]));
 			
 			message = " " + String.format("At rank # %02d", Integer.valueOf(pInfo[0])) + ", your best time is " + playerTime;
 		}
 		else {
 			message = "Error: could not find any times for you.";
 		}
 		
 		this.plugin.Util.messagePlayer(playerName, message);
 	}
 	
 	public void rankList(String senderName) {
 		this.rankList(senderName, "", true);
 	}
 	
 	public void rankList(String senderName, String rankPlayer, Boolean top) {
		if (this.plugin.rankList.size() > 0) {
 			if (top) {
 				
 				this.plugin.Util.messagePlayer(senderName, "^gold^underline  Rank  |  Name  |  Time");
 				
 				int i = 0;
				for (String playerName : this.plugin.rankList.keySet()) {
					String[] playerInfo = this.plugin.rankList.get(playerName).split(",");
 					String playerTime = this.plugin.Util.getFriendly(Integer.valueOf(playerInfo[1]));
 					
 					this.plugin.Util.messagePlayer(senderName, "  " + String.format("%02d", Integer.valueOf(playerInfo[0])) + "  |  " + playerName + "  |  " + playerTime);
 					i++;
 					if (i > 9) {
 						break;
 					}
 				}
 			}
 			else {
				String playerInfo = this.plugin.rankList.get(rankPlayer);
 				if (playerInfo != null) {
					String[] pInfo = this.plugin.rankList.get(rankPlayer).split(",");
 					String playerTime = this.plugin.Util.getFriendly(Integer.valueOf(pInfo[1]));
 					
 					this.plugin.Util.messagePlayer(senderName, " " + String.format(rankPlayer + " is rank # %02d", Integer.valueOf(pInfo[0])) + ", with a time of " + playerTime);
 				}
 				else {
 					this.plugin.Util.messagePlayer(senderName, "Error: could not find any times for " + rankPlayer + ".");
 				}
 			}
 		}
 		else {
 			this.plugin.Util.messagePlayer(senderName, "Error: could not find any times.");
 		}
 	}
 	
 	public void updateRanks() {
 		Query<DB> data = this.plugin.database.getDatabase().find(DB.class).where().gt("id", 0).order().asc("Time");
 		int timeCount = data.findRowCount();
		this.plugin.log.log(Level.INFO, "timeCount " + timeCount);
 		if (data != null && timeCount > 0) {
 			List<DB> timeList = data.findList();
 			int i = 1;
 			this.plugin.rankList.clear();
 			for (DB time : timeList) {
 				String playerName = time.getPlayername();
 				Long playerTime = time.getTime();
 				this.plugin.rankList.put(playerName, i + "," + playerTime);
 				i++;
 			}
 		}
 		else {
			this.plugin.log.log(Level.INFO, "Error: could not find any times in DB.");
 		}
 	}
 }
