 package com.kdude63.orelogger;
 
import java.io.Console;
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class main extends JavaPlugin implements Listener{
 	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	FileConfiguration config;
 	List<Integer> ores;
 	
 	
 	public void onEnable()
 	{
 		getServer().getPluginManager().registerEvents(this, this);
 		if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
             saveDefaultConfig();
		ores = config.getIntegerList("OresToCheck");
 		config = getConfig();
 	}
 	
 	@EventHandler
 	public void onBlockBreakEvent(BlockBreakEvent e){
 		System.out.println(e.getBlock().toString());
 		if(ores.contains(e.getBlock())){
 			
 		}
 	}
 }
