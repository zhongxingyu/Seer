 package com.bukkit.cian1500ww.giveit;
 
 import java.io.*;
 import java.util.*;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijiko.permissions.PermissionHandler;
 
 /**
  * GiveIt for Bukkit
  *
  * @author cian1500ww cian1500ww@gmail.com
  * @version 1.2
  */
 
 public class GiveIt extends JavaPlugin {
 
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     public static int amount = 0;
     public static String name = null;
     public static PermissionHandler Permissions = null;
     public boolean perm = true;
     
     private final Giveme give = new Giveme();
     private final GiveMeInfo givemeinfo = new GiveMeInfo();
     private final GiveMeAdd givemeadd = new GiveMeAdd();
     private ArrayList<String> blocked;
     public GiveIt(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File Folder, File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, Folder,plugin, cLoader);
         // TODO: Place any custom initialisation code here
 
         // NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
     }
 
     public void onEnable() {
         // Check to see if Permissions plugin is being used
     	setupPermissions();
     	// Check to see if allowed.txt exists, if not create a blank one
     	String dir = "plugins/GiveIt";
     	boolean success = (new File(dir)).exists();
     		if (success==false) {
     			new File(dir).mkdir();
     			System.out.println("GiveIt: "+dir+ " not found, creating directory now!!");
     	}
     	String f = "plugins/GiveIt/allowed.txt";
     	File in = new File(f);
     	if(in.exists()!=true){
     		try {
     		System.out.println("GiveIt: No allowed.txt file found, creating blank default now!!");
     		in.createNewFile();
     		BufferedWriter out = new BufferedWriter(new FileWriter(in, true));
     		out.write("#ItemID=Amount.username");
     		out.close();
     		}
     		catch (IOException e){
     			System.out.println("GiveIt: Error creating allowed.txt file!!");
     		}
     	}
     	
     	String e = "plugins/GiveIt/mods.txt";
     	File inagain = new File(e);
     	if(in.exists()!=true && perm==false){
     		try {
     		System.out.println("GiveIt: No mods.txt file found, creating blank default now!!");
     		inagain.createNewFile();
     		BufferedWriter out = new BufferedWriter(new FileWriter(in, true));
     		out.write("");
     		out.close();
     		}
     		catch (IOException e2){
     			System.out.println("GiveIt: Error creating mods.txt file!!");
     		}
     	}
     	
     	String e3 = "plugins/GiveIt/blocked.txt";
     	File blocked = new File(e3);
     	if(in.exists()!=true && perm==false){
     		try {
     		System.out.println("GiveIt: No blocked.txt file found, creating blank default now!!");
     		blocked.createNewFile();
     		BufferedWriter out = new BufferedWriter(new FileWriter(blocked, true));
     		out.write("");
     		out.close();
     		}
     		catch (IOException e4){
     			System.out.println("GiveIt: Error creating blocked.txt file!!");
     		}
     	}
     	
     	// Check to see if log file exists from previous instance and delete if true
     	try {
     		File n = new File("plugins/GiveIt/GiveIt.log");
     		if(n.exists()){
     			n.delete();
     			n.createNewFile();
     		}
     		
     	}
     	catch (Exception e5) {
     		System.out.println("GiveIt: Problem creating new GiveIt.log");
     	}
     	
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " by cian1500ww is enabled!" );
         System.out.println("GiveIt: Email cian1500ww@gmail.com if you're having problems");
     }
     
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
     	String[] trimmedArgs = args;   
         String commandName = command.getName().toLowerCase();
         Player player = (Player)sender;
 		
         String f = "plugins/GiveIt/blocked.txt";
 		
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(f));
 			while(in.readLine().isEmpty()==false){
 				blocked.add(in.readLine());
 			}
 		} 
 		catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
     	// Check to see if player enter's /giveme command
     	if(commandName.equals("giveme") && trimmedArgs.length > 1){
     		
     		// Check for permissions plugin
         	if(perm == true && !Permissions.has(player, "giveit.allow") ){
         		player.sendMessage(ChatColor.DARK_RED+ "You do not have permission to use GiveIt");
         		return true;
         	}
         	else if(perm == true && Permissions.has(player, "giveit.allow") == true ){
         		return give.giveme(sender, trimmedArgs);
         	}
         	else if(blocked.contains(player)==false){
         		return give.giveme(sender,trimmedArgs);
         	}
         	else if(blocked.contains(player)==true){
         		player.sendMessage("GiveIt: You are not allowed to use /giveme");
         		return true;
         	}
    		
     	else if(commandName.equalsIgnoreCase("givemeinfo")){
     		return givemeinfo.givemeinfo(sender);
     	}
     	
     	
     	else if(commandName.equalsIgnoreCase("givemeadd")){
     		
     		// Check for permissions plugin
         	if(perm == true && !Permissions.has(player, "giveit.modify") ){
         		player.sendMessage(ChatColor.DARK_RED+ "You do not have permission to use GiveIt");
         		return true;
         	}
         	else{
         		try {
 				return givemeadd.givemeadd(sender, trimmedArgs);
         		} catch (IOException e) {
 				// TODO Auto-generated catch block
         			e.printStackTrace();
         	    }
         	}
     	}
     	
     	else if(commandName.equalsIgnoreCase("givemeremove")){
     		
     		// Check for permissions plugin
         	if(perm == true && !Permissions.has(player, "giveit.modify") ){
         		player.sendMessage(ChatColor.DARK_RED+ "You do not have permission to use GiveIt");
         		return true;
         	}
         	else{
         		try {
 				return givemeadd.givemeremove(sender, trimmedArgs);
         		} catch (IOException e) {
 				// TODO Auto-generated catch block
         			e.printStackTrace();
         	    }
         	}
    	}
     	
     	return false;
     }
     
     // Method for setting up Permissions
     public void setupPermissions() {
     	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
     	if(GiveIt.Permissions == null) {
     	    if(test != null) {
     		GiveIt.Permissions = ((Permissions)test).getHandler();
     		System.out.println("GiveIt: Permissions support enabled");
     	    } 
     	    else {
     		System.out.println("GiveIt: Permissions not enabled, disabling Permissions support");
     		perm = false;
     	    }
     	}
         }
     
     public void onDisable() {
         // TODO: Place any custom disable code here
 
         System.out.println("Disabling GiveIt!");
     }
     
     
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
     
 }
