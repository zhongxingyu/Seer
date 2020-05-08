 package dev.mCraft.Coinz;
 
 import java.io.File;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.FileManager;
 
 public class Cache {
 	
 	private Coinz plugin;
 	
 	private FileManager fm;
 	
 	public Cache() {
 		plugin = Coinz.instance;
 		
 		fm = SpoutManager.getFileManager();
 		
		fm.addToPreLoginCache(plugin, plugin.getResource("Images/PluginImages.png"), "PluginImages.png");
 		plugin.log.info(plugin.tag + " Block designs have been loaded");
 		
 		fm = SpoutManager.getFileManager();
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/CopperCoin.png"), "CopperCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfBronzeCoin.png"), "HalfBronzeCoin.png");
		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/BronzeCoin.png"), "BronzeCoin.png");
		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfSilverCoin.png"), "HalfSilverCoin.png");
		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/SilverCoin.png"), "SilverCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfGoldCoin.png"), "HalfGoldCoin.png");
		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/GoldCoin.png"), "GoldCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfPlatinumCoin.png"), "HalfPlatinumCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/PlatinumCoin.png"), "PlatinumCoin.png");
		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/GuiScreen.png"), "GuiScreen.png");
		
		/*plugin.saveResource("Images/CopperCoin.png", false);
		plugin.saveResource("Images/HalfBronzeCoin.png", false);
		plugin.saveResource("Images/BronzeCoin.png", false);
		plugin.saveResource("Images/HalfSilverCoin.png", false);
 		plugin.saveResource("Images/SilverCoin.png", false);
		plugin.saveResource("Images/HalfGoldCoin.png", false);
		plugin.saveResource("Images/GoldCoin.png", false);
		plugin.saveResource("Images/HalfPlatinumCoin.png", false);
		plugin.saveResource("Images/PlatinumCoin.png", false);
		plugin.saveResource("Images/GuiScreen.png", false);*/
 		
 		File path = new File("plugins/Coinz/Images");
 		File file = new File(path, "SilverCoin.png");
 		
 		fm.addToPreLoginCache(plugin, file);
 		plugin.log.info(plugin.tag + " Item images have been loaded");
 		
 
 		plugin.log.info(fm.getCache(plugin) + " ");
 	}
 
 }
