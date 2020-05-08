 package com.vidhucraft.Admin360;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.vidhucraft.Admin360.commands.A3;
 
 public class Admin360 extends JavaPlugin{
	
 	
 	@Override
 	public void onEnable(){
 		//Set a3 and helpme command executer
 		A3 a3 = new A3(this);
 		getCommand("a3").setExecutor(a3);
 		getCommand("helpme").setExecutor(a3);
 		
 	}
 	
 	@Override
 	public void onDisable(){
 		
 	}
 }
