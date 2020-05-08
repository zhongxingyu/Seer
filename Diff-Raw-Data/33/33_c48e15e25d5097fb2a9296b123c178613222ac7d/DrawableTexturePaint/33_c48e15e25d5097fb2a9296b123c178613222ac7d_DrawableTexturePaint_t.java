 package net.rptools.maptool.model.drawing;
 
 import java.awt.Image;
 import java.awt.Paint;
 import java.awt.TexturePaint;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.Serializable;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.util.ImageManager;
 
 public class DrawableTexturePaint extends DrawablePaint implements Serializable, ImageObserver {
 
 	private MD5Key assetId;
 	private transient BufferedImage image;
 
 	public DrawableTexturePaint() {
 		// Serializable
 	}
 	
 	public DrawableTexturePaint(Asset asset) {
 		assetId = asset.getId();
 	}
 	
 	@Override
 	public Paint getPaint() {
 		BufferedImage texture = null;
 		if (image != null) {
 			texture = image;
 		} else {
			texture = ImageManager.getImage(AssetManager.getAsset(assetId), this);
 			if (texture != ImageManager.UNKNOWN_IMAGE) {
 				image = texture;
 			}
 		}
 
 		return new TexturePaint(texture, new Rectangle2D.Float(0, 0, texture.getWidth(), texture.getHeight()));
 	}
 
 	public Asset getAsset() {
 		return AssetManager.getAsset(assetId);
 	}
 	
 	public MD5Key getAssetId() {
 		return assetId;
 	}
 	
 	////
 	// IMAGE OBSERVER
 	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		System.out.println("imageUpdate");
		MapTool.getFrame().getCurrentZoneRenderer().flushDrawableRenderer();
		MapTool.getFrame().refresh();
 		return false;
 	}
 }
