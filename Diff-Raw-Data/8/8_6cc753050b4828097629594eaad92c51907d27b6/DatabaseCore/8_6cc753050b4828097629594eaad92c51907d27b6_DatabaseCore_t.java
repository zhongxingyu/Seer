 package se.jkrau.bukkit.secretword.database;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import se.jkrau.bukkit.secretword.SecretWord;
 import se.jkrau.bukkit.secretword.settings.Configuration;
 import se.jkrau.bukkit.secretword.utils.DatabaseUtils;
 
 public class DatabaseCore {
 	
 	public SecretWord core = null;
 	public Map<String, SecretPlayer> secplayers = new HashMap<String, SecretPlayer>();
 	public int failedlogins = 0;
 	
 	public DatabaseCore(SecretWord instance) {
 		core = instance;
 	}
 	
 	public void addLogin(String mcuser, String pass) {
 		SecretPlayer player = secplayers.get(mcuser);
 		
 		if (player == null) return;
 		
 		byte[] salt = DatabaseUtils.getSalt();
 		String saltDb = DatabaseUtils.byteToBase64(salt);
 		String password = null;
 		try {
 			password = DatabaseUtils.hashPassword(pass, salt);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (password != null) {
 			// save.
 			player.getData().set("Username", player.getName());
 			player.getData().set("Word", password);
 			player.getData().set("Salt", saltDb);
 			player.getData().set("IP", player.getIP());
 			player.getData().set("Lastlogin", System.currentTimeMillis());
 			player.saveData();
 		}
 		
 	}
 	
 	public boolean halfHourCheck(String user) {
 		if (!userExists(user)) return false;
 		
 		SecretPlayer player = secplayers.get(user);
 		
 		if (player == null) return false;
 		
 		if (!ipMatches(player.getIP(), player.getName())) return false;
 		
 		long time = player.getData().getLong("Lastlogin");
 		long now = System.currentTimeMillis();
 		
 		return ((now - time) < 1800000);
 	}
 	
 	public boolean ipMatches(String ip, String user) {
 		if (!userExists(user)) { 
 			Configuration.log("User does not exist! - " + user);
 			return false; 
 		}
 		
 		SecretPlayer player = secplayers.get(user);
 		if (player == null) {
 			Configuration.log("Player is null! - " + user);
 			return false;
 		}
 		
 		String ipdata = player.getData().getString("IP");
 		
 		if (ipdata == null) {
 			Configuration.log("IP is null! - " + user);
 			return false;
 		}
 		
 		ipdata = ipdata.substring(0, ip.lastIndexOf("."));
 		
 		// check it.
 		ip = ip.substring(0, ip.lastIndexOf("."));
 		return ip.contains(ipdata);
 		
 		// return (player.getData().getString("IP").equalsIgnoreCase(ip));
 	}
 	
 	public boolean checkLogin(String mcuser, String pass) {
 		SecretPlayer player = secplayers.get(mcuser);
 		
 		if (player == null) return false;
 		try {
 			byte[] hashtocompare = DatabaseUtils.base64ToByte(player.getData().getString("Word"));
 			byte[] salt = DatabaseUtils.base64ToByte(player.getData().getString("Salt"));
 			byte[] proposedHash = DatabaseUtils.getHash(1000, pass, salt);
 			
 			if (Arrays.equals(hashtocompare, proposedHash)) {
 				player.getData().set("IP", player.getIP());
 				player.getData().set("Lastlogin", System.currentTimeMillis());
 				player.saveData();
 				return true;
 			}
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	public boolean userExists(String user) {
 		SecretPlayer player = secplayers.get(user);
 		
 		if (player == null) {
 			Configuration.log("User (userExists) is null! - " + user);
 			return false;
 		}
 		
 		return (new File(new File(core.getDataFolder(), "players"), user).exists());
 	}
 	
 	public void closeItUp() {
 		// TODO: Remove function.
 	}
 }
