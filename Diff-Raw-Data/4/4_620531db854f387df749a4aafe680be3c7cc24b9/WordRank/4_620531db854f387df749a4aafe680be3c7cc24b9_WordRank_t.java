 package com.lala.wordrank;
 
 import java.io.File;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class WordRank extends JavaPlugin{
 	public static PermissionHandler permissionHandler;
 	public static File data;
 	private final W w = new W(this);
 	public void onEnable(){
 		data = getDataFolder();
 		getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, new Chat(this), Priority.Normal, this);		
 		Config.loadPluginSettings();
 		getCommand("w").setExecutor(w);
 		setupPermissions();
 		System.out.println("[WordRank] Enabled!");
 	}
 	public void onDisable(){
 		System.out.println("[WordRank] Disabled!");
 	}
 	private void setupPermissions() {
 	    if (permissionHandler != null) {
 	        return;
 	    }	    
 	    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");	    
 	    if (permissionsPlugin == null) {
	        System.out.println("Permissions not detected, expect errors!");        
 	        return;
 	    }
 	    permissionHandler = ((Permissions) permissionsPlugin).getHandler();	    
 	}
 }
