 package com.bot42;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 public class Account {
 	private File location;
 	
 	public Account (String username) {
 		(new File("accounts")).mkdirs();
 		location = new File("accounts" + File.separator + username + "cfg");
 	}
 	
	public boolean addUser (String username) throws Exception {
 		if (location.exists()) return false;
 		location.createNewFile();
 		BufferedWriter writer = new BufferedWriter(new FileWriter(location));
 		writer.write("username=\"" + username + "\"\n");
 		writer.flush(); writer.close();
 		return true;
 	}
 }
