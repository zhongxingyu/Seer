 package com.bot42;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 public class Account {
 	private File location;
	private String username;
 	
 	public Account (String username) {
 		(new File("accounts")).mkdirs();
 		location = new File("accounts" + File.separator + username + "cfg");
		this.username = username;
 	}
 	
	public boolean addUser () throws Exception {
 		if (location.exists()) return false;
 		location.createNewFile();
 		BufferedWriter writer = new BufferedWriter(new FileWriter(location));
 		writer.write("username=\"" + username + "\"\n");
 		writer.flush(); writer.close();
 		return true;
 	}
 }
