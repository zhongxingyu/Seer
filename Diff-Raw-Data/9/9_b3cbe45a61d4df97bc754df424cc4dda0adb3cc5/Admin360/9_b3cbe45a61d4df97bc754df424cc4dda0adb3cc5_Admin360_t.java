 package com.vidhucraft.Admin360;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.vidhucraft.Admin360.commands.A3;
import com.vidhucraft.Admin360.datasource.DataSource;
import com.vidhucraft.Admin360.datasource.SQLite_DataSource;
 
 public class Admin360 extends JavaPlugin{
	public static DataSource ds = new SQLite_DataSource();
 	
 	@Override
 	public void onEnable(){
 		//Set a3 and helpme command executer
 		A3 a3 = new A3(this);
 		getCommand("a3").setExecutor(a3);
 		getCommand("helpme").setExecutor(a3);
 		
		//Connect to the database
		ds.connect();
		ds.setUp();
 	}
 	
 	@Override
 	public void onDisable(){
 		
 	}
 }
