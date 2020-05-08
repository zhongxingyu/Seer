 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
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
 	private double scale;
 	private transient BufferedImage image;
 	private transient Asset asset;
 
 	public DrawableTexturePaint() {
 		// Serializable
 	}
 	
 	public DrawableTexturePaint(MD5Key id) {
 		this(id, 1);
 	}
 	public DrawableTexturePaint(MD5Key id, double scale) {
 		assetId = id;
 		this.scale = scale;
 	}
 	public DrawableTexturePaint(Asset asset) {
		this(asset.getId());
 		this.asset = asset;
 	}
 	public DrawableTexturePaint(Asset asset, double scale) {
 		this(asset.getId(), 1);
 		this.asset = asset;
 	}
 
 	@Override
 	public Paint getPaint(int offsetX, int offsetY, double scale) {
 		BufferedImage texture = null;
 		if (image != null) {
 			texture = image;
 		} else {
 			texture = ImageManager.getImage(getAsset(), this);
 			if (texture != ImageManager.UNKNOWN_IMAGE) {
 				image = texture;
 			}
 		}
 
 		return new TexturePaint(texture, new Rectangle2D.Double(offsetX, offsetY, texture.getWidth()*scale*this.scale, texture.getHeight()*scale*this.scale));
 	}
 	
 	@Override
 	public Paint getPaint() {
 		return getPaint(0, 0, 1);
 	}
 
 	public Asset getAsset() {
		if (asset == null) {
 			asset = AssetManager.getAsset(assetId);
 		}
 		
 		return asset;
 	}
 	
 	public MD5Key getAssetId() {
 		return assetId;
 	}
 	
 	////
 	// IMAGE OBSERVER
 	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
 
 		if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 			MapTool.getFrame().getCurrentZoneRenderer().flushDrawableRenderer();
 			MapTool.getFrame().refresh();
 		}
 		return false;
 	}
 }
