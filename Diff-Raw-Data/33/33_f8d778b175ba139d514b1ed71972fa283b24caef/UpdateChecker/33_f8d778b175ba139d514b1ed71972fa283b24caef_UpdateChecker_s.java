 package minny.routes.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import minny.routes.Routes;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class UpdateChecker{
 
 	Routes plugin;
 
 	public UpdateChecker(Routes plugin) {
 		this.plugin = plugin;
 	}
 	
 	public int isUpdate(String str1, String str2){
 		String[] vals1 = str1.split("\\.");
 		String[] vals2 = str2.split("\\.");
 		int i=0;
 		while(i<vals1.length&&i<vals2.length&&vals1[i].equals(vals2[i])) {
 		  i++;
 		}
 
 		if (i<vals1.length && i<vals2.length) {
 		    int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
 		    return diff<0?-1:diff==0?0:1;
 		}
 
 		return vals1.length<vals2.length?-1:vals1.length==vals2.length?0:1;
 	}
 
 	public void checkUpdate() {
 		if (plugin.getConfig().getBoolean("UpdateChecker")) {
 			String current = plugin.getDescription().getVersion();
 			String readurl = "https://raw.github.com/minnymin3/Routes/master/version";
 			Logger log = plugin.getLogger();
 			
 			try {	
 				log.info("Checking for a new version...");
 				URL url = new URL(readurl);
 				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                 String str;
                 while ((str = br.readLine()) != null) {
                     String line = str;
                     
                     if (isUpdate(current, line) == -1){
                    	log.info("There is a new version of SimpleBuild avalable.");
                         log.info("Go to http://dev.bukkit.org/server-mods/routes/ to get the new version");
                         plugin.isUpdate = true;
                         break;
                     } else if (isUpdate(current, line) == 1){
                         plugin.isUpdate = false;                    	
                     	log.info("You are running the development build of Routes");
                         break;
                     } else if (isUpdate(current, line) == 0){
                     	log.info("Routes is up to date");
                     	plugin.isUpdate = false;
                     	break;
                     }
                 }
                 br.close();
 			} catch (IOException e) {
 				log.severe("Was unable to check for update. URL may be invalid");
 			}
 		}
 	}
 	
 	public void checkUpdateInGame(CommandSender sender) {
 		if (plugin.getConfig().getBoolean("UpdateChecker")) {
 			String current = plugin.getDescription().getVersion();
 			String readurl = "https://raw.github.com/minnymin3/Routs/master/version";
 			Logger log = plugin.getLogger();
 			try {
 				log.info(sender.getName() + " is checking for a new version.");
 				sender.sendMessage(ChatColor.GREEN + "Checking for updates...");
 				URL url = new URL(readurl);
 				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                 String str;
                 while ((str = br.readLine()) != null) {
                     String line = str;
                     
                     if (isUpdate(current, line) == -1){
                     	sender.sendMessage(ChatColor.GREEN + "There is a new version of SimpleBuild avalable.");
                         sender.sendMessage(ChatColor.GREEN + "Go to http://dev.bukkit.org/server-mods/routes/ to get the new version");
                         plugin.isUpdate = true;
                         break;
                     } else if (isUpdate(current, line) == 1){
                     	sender.sendMessage(ChatColor.LIGHT_PURPLE + "You are running the development build of Routes");
                         plugin.isUpdate = false;
                         break;
                     } else if (isUpdate(current, line) == 0){
                     	sender.sendMessage(ChatColor.GREEN + "Routes is up to date");
                     	plugin.isUpdate = false;
                     	break;
                     }
                 }
                 br.close();
                 
 
 				
 			} catch (IOException e) {
 				log.info("Was unable to check for update. URL may be invalid");
 				plugin.isUpdate = false;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Checking for updates is disabled. This can be fixed in the SimpleBuild config.");
 		}
 	}
 }
