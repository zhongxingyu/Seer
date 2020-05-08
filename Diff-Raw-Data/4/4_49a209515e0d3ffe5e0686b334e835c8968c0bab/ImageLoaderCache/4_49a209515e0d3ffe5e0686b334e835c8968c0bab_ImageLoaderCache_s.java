 package net.rptools.maptool.client.swing;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.util.ImageManager;
 
 public class ImageLoaderCache {
 
 	private Map<String, Image> imageMap = new HashMap<String, Image>();
 	
 	public void flush() {
 		imageMap.clear();
 	}
 	
 	public Image get(URL url, ImageObserver... observers) {
 
 		// URLs take a huge amount of time in equals(), so simplify by
 		// converting to a string
 		Image image = imageMap.get(url.toString());
 		if (image == null) {
 
 			String protocol = url.getProtocol();
 			String path = url.getHost() + url.getPath();
 
 			if ("cp".equals(protocol)) {
 				try {
 					image = ImageUtil.getImage(path);
 				} catch (IOException ioe) {
 					// TODO: Show my own broken image
 					ioe.printStackTrace();
 				}
 			} else if ("asset".equals(protocol)) {
 
 				// Look for size request
 				int index = path.indexOf("-");
 				int size = -1;
 				if (index >= 0) {
 					String szStr = path.substring(index+1);
 					path = path.substring(0, index);
 					size = Integer.parseInt(szStr);
 				}
 				
 				image = ImageManager.getImage(AssetManager.getAsset(new MD5Key(path)), observers);
 				boolean imageLoaded = image != ImageManager.UNKNOWN_IMAGE;
 				if (!imageLoaded) {
 					size = 38;
 				}
 
 				if (size > 0) {
 					Dimension sz = new Dimension(image.getWidth(null), image.getHeight(null));
 					SwingUtil.constrainTo(sz, size);
 					
 					BufferedImage img = new BufferedImage(sz.width, sz.height, ImageUtil.pickBestTransparency(image));
 					Graphics2D g = img.createGraphics();
 					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 					g.drawImage(image, 0, 0, sz.width, sz.height, null);
 					g.dispose();
 					
 					image = img;
 				}
 				
 				if (imageLoaded) {
 					// Don't have to load it again
 					imageMap.put(url.toString(), image);
 				}
 				
 				return image;
 				
 			} else {
 
 				// Normal method
 				image = Toolkit.getDefaultToolkit().createImage(url);
 			}
 
 			imageMap.put(url.toString(), image);
 		}
 
 		return image;
 	}
 
 }
