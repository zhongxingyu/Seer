 package com.lala.wordrank;
 
 import java.util.logging.Logger;
 
 import lib.PatPeter.SQLibrary.SQLite;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.lala.wordrank.misc.Perms;
 import com.lala.wordrank.sql.SQLWord;
 
 import de.bananaco.permissions.Permissions;
 import de.bananaco.permissions.worlds.WorldPermissionsManager;
 
 public class WordRank extends JavaPlugin {
 	
 	public WorldPermissionsManager bperm;
 	public PermissionManager pex;
 	
 	public SQLite sql;
 	private Logger log = Logger.getLogger("Minecraft");
 	
 	public boolean bpermEnabled;
 	public boolean pexEnabled;
 	
 	public void onEnable(){
 		getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, new ChatListen(this), Priority.Normal, this);		
 		setupPermissions();
 		setupSQL();
 		send("is now enabled, version: "+this.getDescription().getVersion());
 	}
 	
 	public void onDisable(){
		sql.close();
 		send("is now disabled!");
 	}
 	
 	private void setupSQL(){
 		this.sql = new SQLite(log, "[WordRank]", "wordrank_words", this.getDataFolder().getAbsolutePath());
		sql.createTable("CREATE TABLE IF NOT EXISTS wordrank (name String, groupname String)");
 	}
 	
 	private void setupPermissions() {
 		send("Checking permission plugins");
 		Config config = new Config(this);
 	    try {
 	    	bperm = Permissions.getWorldPermissionsManager();
 	    	bpermEnabled = true;
 	    	send("bPermissions detected");
 	    } catch (Exception e) {
 	    	send("bPermissions not detected");
 	    	bpermEnabled = false;
 	    }
 	    
 	    if (getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
 	    	pex = PermissionsEx.getPermissionManager();
 	    	pexEnabled = true;
 	    	send("PEX detected");
 	    }else{
 	    	send("PEX not detected");
 	    	pexEnabled = false;
 	    }
 	    
 	    if (!bpermEnabled && !pexEnabled){
 	    	sendErr("No compatible permission plugins found. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    }
 	    
 	    if (config.getPerms().equals(Perms.bPermissions) && !bpermEnabled){
 	    	sendErr("Config is set to bPermissions, however bPermissions is not detected. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    }
 	    
 	    if (config.getPerms().equals(Perms.PEX) && !pexEnabled){
 	    	sendErr("Config is set to PEX, however PEX is not detected. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    }
 	    
 	    if (config.getPerms().equals(Perms.GroupManager)){
 	    	sendErr("Config is set to GroupManager, however GroupManager is not supported yet. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    }
 	    
 	    if (config.getPerms().equals(Perms.Unknown)){
 	    	sendErr("Config is set to the unknown permission plugin '"+config.permPlugin()+"' WordRank will now disable.");
 	    	this.setEnabled(false);
 	    }
 	}
 	
 	public void send(String message){
 		System.out.println("[WordRank] "+message);
 	}
 	
 	public void send(String message, int times){
 		for (int i = times; i > 0; i--){
 			send(message);
 		}
 	}
 	
 	public void sendErr(String message){
 		System.err.println("[WordRank] [Error] "+message);
 	}
 	
 	public void sendErr(String message, int times){
 		for (int i = times; i > 0; i--){
 			sendErr(message);
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (args[0].equalsIgnoreCase("add")){
 			if (sender.hasPermission("wordrank.add")){
 				if (args.length == 3){
 					Word w = new Word(args[1], args[2]);
 					SQLWord sw = new SQLWord(this, w);
 					sender.sendMessage(sw.addToDB());
 					return true;
 				}else{
 					sender.sendMessage(ChatColor.RED+"Syntax Error, invalid argument length");
 					sender.sendMessage(ChatColor.AQUA+"/wordrank add [word] [groupname]");
 					return true;
 				}
 			}else{
 				sender.sendMessage(ChatColor.DARK_RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		else if (args[0].equalsIgnoreCase("remove")){
 			if (sender.hasPermission("wordrank.remove")){
 				if (args.length == 2){
 					Word w = new Word(args[1], "unknown");
 					SQLWord sw = new SQLWord(this, w);
 					sender.sendMessage(sw.removeFromDB());
 					return true;
 				}else{
 					sender.sendMessage(ChatColor.RED+"Syntax Error, invalid argument length");
 					sender.sendMessage(ChatColor.AQUA+"/wordrank remove [word]");
 					return true;
 				}
 			}else{
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		return true;
 	}
 }
