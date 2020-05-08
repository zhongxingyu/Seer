 package com.namarius.weathernews;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WeatherNews extends JavaPlugin {
 	
 	private WeatherListener wl;
 
 	@Override
 	public void onDisable() {
 		wl=null;
 	}
 
 	@Override
 	public void onEnable() {
		if(!this.getDataFolder().exists())
			this.getDataFolder().mkdir();
 		File config = new File(this.getDataFolder(), "config.yml");
 		if(!config.exists())
 		{
 			byte[] buffer = new byte[1024];
 			InputStream def = WeatherNews.class.getResourceAsStream("/config.yml");
 			try
 			{
				//config.createNewFile();
 				FileOutputStream sconfig = new FileOutputStream(config);
 				for(int i=0;(i=def.read(buffer))!=-1;)
 					sconfig.write(buffer,0,i);
 			}
 			catch(Exception e)
 			{
 				this.getServer().getLogger().warning(e.getMessage());
 				return;
 			}
 		}
 		if(wl==null)
 			this.wl = new WeatherListener(this);
 		this.getServer().getPluginManager().registerEvents(this.wl, this);
 		this.getServer().getLogger().info("Fully loaded and ready to run.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if(sender instanceof Player)
 		{
 			Player player = (Player) sender;
 			World w = player.getWorld();
 			this.wl.runNews(w, player);
 		}
 		else
 		{
 			for(World w : this.getServer().getWorlds())
 			{
 				this.wl.runNews(w);
 			}
 		}
 		return true;
 	}
 
 }
