 package com.eucsoft.beeper.handler;
 
 import com.eucsoft.beeper.Beeper;
 import com.eucsoft.beeper.logging.AudioLogger;
 import com.eucsoft.beeper.model.User;
 
  public class ClientHandler {
 	
 	private User user;
 	private Beeper beeper = Beeper.getInstance();
 	AudioLogger logger;
	 
 	public void onConnect(String userInfo) {
 		user = new User(userInfo);
 		beeper.addUser(user);
 	}
 	
 	public void onGetRoom() {
 		beeper.addUserToBench(user);
 	}
 	
 	public void onMessageBegin() {
 		logger = new AudioLogger();
 		logger.init(user);
 	}
 	
 	public void onMessage(byte[] message) {
 		logger.append(message);
 		for (User nextUserInRoom : user.getRoom().getUsers()) {
 			if (! nextUserInRoom.equals(user)) {
 				ClientHandler nextListener = beeper.getClientHandler(nextUserInRoom);
 				nextListener.sendMessage(message);
 			}
 		}
 	}
 	
 	public void onMessageEnd() {
 		logger.close();
 	}
 	
 	public void onDisconnect() {
 		if (user.getRoom() != null) {
 			user.getRoom().removeUser(user);
 		}
 		beeper.removeUserFromBench(user);
 		beeper.removeUser(user);
 	}
 	
 	
 	public void sendMessageBegin() {
 	}
 	
 	public void sendMessage(byte[] message) {
 	}
 	
 	public void sendMessageEnd() {
 	}
 	
 	public void sendChangeRoom() {
 	}
 	
 }
