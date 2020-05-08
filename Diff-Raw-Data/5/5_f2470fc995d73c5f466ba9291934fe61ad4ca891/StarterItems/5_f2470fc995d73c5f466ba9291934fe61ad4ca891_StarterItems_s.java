 package com.github.TrungLam;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class StarterItems extends JavaPlugin implements Listener{
 	public static StarterItems plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	
 	public void onDisable(){
		logger.info(this.getDescription() + " is disabled");
 	}
 	public void onEnable(){
		logger.info(this.getDescription() + " is enabled");
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event){
 		Player player = event.getPlayer();
 		if (!player.hasPlayedBefore()){
 			ItemStack[] kit = {new ItemStack(Material.WOOD_AXE), new ItemStack(Material.WOOD_PICKAXE), 
 			new ItemStack(Material.WOOD_SWORD), new ItemStack(Material.WOOD_SPADE)};
 			for (ItemStack i : kit){
 				player.getInventory().addItem(i);
 			}
 		}
 	}
 }
