 package com.stealthyone.bukkit.McmmoSkillGiver;
 
 import com.stealthyone.bukkit.McmmoSkillGiver.commands.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BasePlugin extends JavaPlugin {
 
 	public LogManager log;
 	public PermHandler perm;
 	public PluginMethods methods;
 	
 	@Override
 	public void onEnable() {
 		/**
 		 * Plugin enabled
 		 */
 		//Setup logger
 		log = new LogManager(this);
 		
 		//Setup permission manager
 		perm = new PermHandler(this);
 		
 		//Setup methods
 		methods = new PluginMethods(this);
 		
 		//Setup commands
 		this.getCommand("skillgiver").setExecutor(new CmdSkillGiver(this));
 		this.getCommand("mmoaward").setExecutor(new CmdMmoAward(this));
 		
		this.log.info(this.getName() + " v" + getPlVersion() + " enabled!");
 	}
 	
 	@Override
 	public void onDisable() {
 		//Plugin disabled
 		
		this.log.info(this.getName() + " v" + getPlVersion() + " disabled!");
 	}
 	
 	public String getPlVersion() {
 		return this.getDescription().getVersion();
 	}
 	
 	public boolean isDebug() {
		return false;
 	}
 }
