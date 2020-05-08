 package me.furt.industrial;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.URL;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.io.FileUtils;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.design.Texture;
 
 public class Assets {
 	private IndustrialInc plugin;
 	private HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();
 
 	public Assets(IndustrialInc instance) {
 		this.plugin = instance;
 	}
 
 	public BufferedImage getCachedImage(String cacheFileName) {
 		if (imageCache.containsKey(cacheFileName)) {
 			return (BufferedImage) imageCache.get(cacheFileName);
 		}
 		return null;
 	}
 
 	public void freeImageCacheMemory() {
 		imageCache.clear();
 	}
 
 	public Texture getTexture(String cachedName) {
 		BufferedImage bi = getCachedImage(cachedName);
 		return new Texture(plugin, plugin.getDataFolder() + File.separator
 				+ "imageCache" + File.separator + cachedName + ".png",
 				bi.getWidth(), bi.getHeight(), bi.getHeight()
 						/ plugin.getConfig().getInt("pixel_size"));
 
 	}
 
 	public void addAsset(String image) {
 		if (new File(plugin.getDataFolder() + File.separator + "imageCache"
 				+ File.separator + image + ".png").exists()) {
 			File cacheFile = new File(plugin.getDataFolder() + File.separator
 					+ "imageCache" + File.separator + image + ".png");
 			BufferedImage bufferedImage = null;
 			try {
 				bufferedImage = ImageIO.read(cacheFile);
 			} catch (Exception exception) {
 			}
 			imageCache.put(image, bufferedImage);
 			SpoutManager.getFileManager().addToPreLoginCache(plugin, cacheFile);
 			return;
 		}
 		try {
 			URL inputUrl = getClass().getResource(image + ".png");
 			File dest = new File(plugin.getDataFolder() + File.separator
 					+ "imageCache" + File.separator + image + ".png");
 			FileUtils.copyURLToFile(inputUrl, dest);
 			BufferedImage bufferedImage = null;
 			try {
 				bufferedImage = ImageIO.read(dest);
 			} catch (Exception exception) {
 			}
 			imageCache.put(image, bufferedImage);
 			SpoutManager.getFileManager().addToPreLoginCache(plugin, dest);
 			return;
 		} catch (Exception e) {

 		}
 
 		// plugin.getDataFolder() + "/cacheImages/" + image;
 		// URL url = getClass().getResource(image);
 		// imageCache.put(name, bufferedImage);
 		// SpoutManager.getFileManager().addToPreLoginCache(plugin, cacheFile);
 	}
 }
