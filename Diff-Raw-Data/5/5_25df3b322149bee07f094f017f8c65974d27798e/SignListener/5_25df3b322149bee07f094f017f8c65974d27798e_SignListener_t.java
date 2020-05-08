 package com.rojel.parkourpvp.listeners;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import com.rojel.parkourpvp.data.Room;
 import com.rojel.parkourpvp.data.RoomState;
 import com.rojel.parkourpvp.managers.PlayerManager;
 import com.rojel.parkourpvp.managers.RoomManager;
 import com.rojel.pluginsignapi.events.PluginSignClickEvent;
 import com.rojel.pluginsignapi.events.PluginSignUpdateEvent;
 
 public class SignListener implements Listener {
 	@EventHandler
 	public void onSignUpdate(PluginSignUpdateEvent event) {
 		if(event.getPlugin().equalsIgnoreCase("ppvp")) {
 			if(event.getPurpose().equalsIgnoreCase("join")) {
 				Room room = RoomManager.getRoom(event.getData());
 				if(room != null) {
 					if(room.getState() == RoomState.WAITING) {
 						event.setLine(0, "aJoin room");
 						event.setLine(3, secondsToString(room.getWaitingCounter()) + " until start");
 					}
 					else if(room.getState() == RoomState.ENDING)
 						event.setLine(0, "eEnding");
 					else if(room.getState() == RoomState.RUNNING) {
 						event.setLine(0, "cRunning");
 						event.setLine(3, secondsToString(room.getGameCounter()) + " left");
 					}
 					
 					event.setLine(1, room.getName());
 					event.setLine(2, "o" + room.getPlayerCount() + "/" + Room.MAX_PLAYERS + " players");
 				} else {
 					event.setLine(0, event.getData());
 					event.setLine(1, "cDOES NOT");
 					event.setLine(2, "cEXIST");
 				}
 			} else if(event.getPurpose().equalsIgnoreCase("leave")) {
 				Room room = RoomManager.getRoom(event.getData());
 				if(room != null) {
 					event.setLine(1, "aLeave");
 					event.setLine(2, room.getName());
 				} else {
 					event.setLine(0, event.getData());
 					event.setLine(1, "cDOES NOT");
 					event.setLine(2, "cEXIST");
 				}
 			} else if(event.getPurpose().equalsIgnoreCase("info")) {
 				Room room = RoomManager.getRoom(event.getData());
 				if(room != null) {
 					if(room.getState() == RoomState.WAITING) {
 						event.setLine(0, "aWaiting");
 						event.setLine(3, secondsToString(room.getWaitingCounter()) + " until start");
 					}
 					else if(room.getState() == RoomState.ENDING)
 						event.setLine(0, "eEnding");
 					else if(room.getState() == RoomState.RUNNING) {
 						event.setLine(0, "cRunning");
 						event.setLine(3, secondsToString(room.getGameCounter()) + " left");
 					}
 					
 					event.setLine(1, room.getName());
 					event.setLine(2, "o" + room.getPlayerCount() + "/" + Room.MAX_PLAYERS + " players");
 				} else {
 					event.setLine(0, event.getData());
 					event.setLine(1, "cDOES NOT");
 					event.setLine(2, "cEXIST");
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onSignClick(PluginSignClickEvent event) {
 		if(event.getPlugin().equalsIgnoreCase("ppvp")) {
 			if(event.getPurpose().equalsIgnoreCase("join")) {
 				Room room = RoomManager.getRoom(event.getData());
 				if(room != null) {
 					if(room.isJoinable())
 						room.joinRoom(PlayerManager.getData(event.getPlayer()));
 					else
 						event.getPlayer().sendMessage("cThis room is either full, running or not setup.");
 				} else
 					event.getPlayer().sendMessage("cThe room you want to join does not exist.");
 			} else if(event.getPurpose().equalsIgnoreCase("leave")) {
 				Room room = RoomManager.getRoom(event.getData());
 				if(room != null)
 					room.leaveRoom(PlayerManager.getData(event.getPlayer()));
 				else
 					event.getPlayer().sendMessage("cThe room you want to leave does not exist.");
 			}
 		}
 	}
 	
 	public String secondsToString(int seconds) {
 		int minutes = (int) (seconds / 60);
 		int secondsLeft = seconds - minutes * 60;
 		
		if(minutes == 0)
			return secondsLeft + "s";
		else
			return minutes + "min " + secondsLeft + "s";
 	}
 }
