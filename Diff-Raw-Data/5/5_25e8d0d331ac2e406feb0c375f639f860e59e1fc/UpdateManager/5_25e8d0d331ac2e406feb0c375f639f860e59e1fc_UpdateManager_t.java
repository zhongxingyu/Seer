 package de.splashfish.flinky;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class UpdateManager {
 	
 	private final String latest_url = new String("https://raw.github.com/lukasdietrich/Flinky/master/latest");
 	
 	private File flinky_file;
 	private File flinky_old;
 	
 	public UpdateManager(File data) {
 		this.flinky_file = new File(data.getAbsolutePath()+"/../Flinky.jar");
 		this.flinky_old = new File(this.flinky_file.getAbsolutePath()+".old");
 	}
 	
 	public boolean invokeUpdate(CommandSender sender) {
 		try {
 			sender.sendMessage(ChatColor.DARK_PURPLE + "Searching for updates..");
 			String[] newest = getNewest();
			if(newest != null && newest.length >= 2) {
 				if(!newest[0].equalsIgnoreCase(Flinky.version)) {
 					sender.sendMessage(ChatColor.DARK_PURPLE + "Update found!");
 					if(updateJar(newest[1])) {
 						sender.sendMessage(ChatColor.DARK_PURPLE + "Successfully updated! Changes will be applied on next reload.");
 					} else {
 						sender.sendMessage(ChatColor.DARK_PURPLE + "A error occurred while updating!");
 					}
 				} else {
 					sender.sendMessage(ChatColor.DARK_PURPLE + "Flinky is up-to-date!");
 				}
 			} else {
				sender.sendMessage(ChatColor.DARK_PURPLE + "The 'latest'-file seems to be corrupted! Or was not able to be downloaded.");
 			}
 		} catch (IOException e) {
 			sender.sendMessage(ChatColor.DARK_PURPLE + "A error occurred while searching for updates!");
 		}
 		return true;
 	}
 	
 	private String[] getNewest() throws IOException {
 		String tmp = downloadAsString(latest_url);
 		return tmp.split("\n");
 	}
 	
 	private String downloadAsString(String url) throws IOException {
 		URL https_url = new URL(url);
 		HttpsURLConnection con = (HttpsURLConnection) https_url.openConnection();
 		
 		StringBuffer buffer = new StringBuffer();
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
 			String line;
 			while((line = reader.readLine()) != null)
 				buffer.append(line +"\n");
 		reader.close();
 		
 		return buffer.toString();
 	}
 	
 	private boolean updateJar(String url) {
 		try {
 			if(flinky_file.exists()) {
 				flinky_file.renameTo(flinky_old);
 			}
 			
 			URL https_url = new URL(url);
 			HttpsURLConnection con = (HttpsURLConnection) https_url.openConnection();
 			
 			InputStream 	sourcestream = con.getInputStream();
 			OutputStream	outputstream = new FileOutputStream(flinky_file);
 			
 			byte[] transferbuffer = new byte[1024];
 			int length;
 			while ((length = sourcestream.read(transferbuffer)) > 0) {
 				outputstream.write(transferbuffer, 0, length);
 			}
 			sourcestream.close();
 			outputstream.close();
 			
 			if(flinky_file.length() > 0) {
 				flinky_old.delete();
 				return true;
 			} else {
 				flinky_file.delete();
 				flinky_old.renameTo(flinky_file);
 			}
 		} catch (Exception e) {
 			if(flinky_file.exists() && flinky_old.exists()) {
 				flinky_file.delete();
 				flinky_old.renameTo(flinky_file);
 			}
 		}
 		
 		return false;
 	}
 }
