 package org.ksa14.webhard.sftp;
 
 import java.io.*;
 import java.math.*;
 import java.net.*;
 import java.security.*;
 import java.util.*;
 
 import com.jcraft.jsch.*;
 
 public class SftpUtil {
 	public static final String Username = "apache";
 	public static final String Host = "webhard.ksa14.org";
 
 	private JSch SUJsch;
 	private Session SUSession;
 	private Channel SUChannel;
 	
 	private static String md5(String input) {
 		try {
			byte hash[] = MessageDigest.getInstance("MD5").digest(input.getBytes());
			return String.format("%1$032x", new BigInteger(1, hash));
 		} catch (NoSuchAlgorithmException nsae) {
 			// This should never happen
 			assert false;
 			return null;
 		}
 	}
  
 	public static String[] GetKeys(String id) {
 		try {
 			URL url = new URL("http://webhard.ksa14.org/key.php?id=" + id);
 			URLConnection connection = url.openConnection();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			String publicKey = reader.readLine() + "\n", line;
 			
 			StringBuffer privateKey = new StringBuffer();
 			while((line = reader.readLine()) != null) 
 				privateKey.append(line + "\n");
 			
 			if(privateKey.length() == 0)
 			return null;
 			
 			String keys[] = {publicKey, privateKey.toString()};
 			return keys;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public SftpUtil() {		
 	}
 	
 	public boolean Connect(String id, String pw) {
 		try {
 			// Get SSH public, private keys from server
 			String[] SshKeys = GetKeys(id);
 			if (SshKeys == null)
 				return false;
 			
 			// Init Jsch
 			SUJsch = new JSch();
 			String hpw = md5(pw);
 			SUJsch.addIdentity("Key", SshKeys[1].getBytes(), SshKeys[0].getBytes(), hpw.getBytes());
 			
 			// Start SSH session
 			SUSession = SUJsch.getSession(Username, Host, 22);
 			Properties SUConf = new Properties();
 			SUConf.put("StrictHostKeyChecking", "no");
 			SUSession.setConfig(SUConf);
 			
 			SUSession.connect();
 			
 			// Start SFTP channel
 			SUChannel = SUSession.openChannel("sftp");
 			SUChannel.connect();
 		} catch (Exception e) {
 			return false;
 		}
 		
 		return true;
 	}
 }
