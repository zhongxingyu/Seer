 package com.eucsoft.beeper.model;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 public class Room {
 
 	private Collection<User> users = new HashSet<User>();
 	
 	public Collection<User> getUsers() {
 		return users;
 	}
 	
 	public void addUser(User user) {
		if (user.getRoom() != null) {
			user.getRoom().removeUser(user);
 		}
		user.setRoom(this);
		users.add(user);
 	}
 	
 	public void removeUser(User user) {
		if (user.getRoom() != null) {
			user.setRoom(null);
 		}
		users.remove(user);
 	}
 	
 }
