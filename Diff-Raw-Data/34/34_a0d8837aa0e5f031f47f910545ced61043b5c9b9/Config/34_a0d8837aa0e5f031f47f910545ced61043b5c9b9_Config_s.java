 package com.lala.wordrank;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.lala.wordrank.misc.Perms;
 
 public class Config {
 	
 	private FileConfiguration yml;
 	
 	public Config(WordRank plugin){
 		this.yml = plugin.getConfig();
		plugin.getConfig().options().copyDefaults(true);
 	}
 	
 	public Perms getPerms(){
 		if (permPlugin().equalsIgnoreCase("bPermissions"))
 			return Perms.bPermissions;
 		else if (permPlugin().equalsIgnoreCase("PEX"))
 			return Perms.PEX;
 		else if (permPlugin().equalsIgnoreCase("GroupManager")){
 			return Perms.GroupManager;
 		}else{
 			return Perms.Unknown;
 		}
 	}
 	
 	public String permPlugin(){
 		return yml.getString("perm-plugin");
 	}
 }
