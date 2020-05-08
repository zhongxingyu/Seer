 package btwmod.livemap;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import btwmods.ModLoader;
 import net.minecraft.src.Chunk;
 
 public class MapImage {
 
 	private final MapLayer mapLayer;
 	
 	public final int chunkX;
 	public final int chunkZ;
 	public final String chunkKey;
 	
 	private BufferedImage colorImage = null;
 	private BufferedImage heightImage = null;
 	
 	public final File colorImageFile;
 	public final File heightImageFile;
 	
 	public MapImage(MapLayer mapLayer, int chunkX, int chunkZ) throws Exception {
 		this.mapLayer = mapLayer;
 		
 		this.chunkX = chunkX;
 		this.chunkZ = chunkZ;
 		chunkKey = mapLayer.getChunkKey(chunkX, chunkZ);
 		
 		colorImageFile = new File(mapLayer.layerDirectory, chunkKey + ".png");
 		heightImageFile = new File(mapLayer.layerDirectory, chunkKey + "h.png");
 		
 		setBlankImages();
 	}
 	
 	public int getHeightAt(int x, int z) {
 		return heightImage.getRGB(x, z) & 0xFF;
 	}
 	
 	public boolean loadImages() throws Exception {
 		boolean success = true;
 		
 		colorImage = heightImage = null;
 		
 		if (colorImageFile.exists() && (colorImage = ImageIO.read(colorImageFile)) == null) {
 			success = false;
 		}
 		
 		if (heightImageFile.exists() && (heightImage = ImageIO.read(heightImageFile)) == null) {
 			success = false;
 		}
 		
 		if (colorImage == null)
 			colorImage = createBlank();
 		
 		if (heightImage == null)
 			heightImage = createBlank();
 		
 		return success;
 	}
 	
 	public void setBlankImages() {
 		colorImage = createBlank();
 		heightImage = createBlank();
 	}
 	
 	private BufferedImage createBlank() {
 		return new BufferedImage(mapLayer.map.imageSize, mapLayer.map.imageSize, BufferedImage.TYPE_INT_ARGB);
 	}
 	
 	public boolean isLoaded() {
 		return colorImage != null && heightImage != null;
 	}
 	
 	public void drawPixels(int x, int z, PixelColor colorPixel, PixelColor heightPixel) {
 		if (!isLoaded())
 			return;
 		
 		for (int iX = 0; iX < mapLayer.pixelSize; iX++) {
 			for (int iZ = 0; iZ < mapLayer.pixelSize; iZ++) {
 				colorImage.setRGB(x + iX, z + iZ, colorPixel.asColor().getRGB());
 				heightImage.setRGB(x + iX, z + iZ, heightPixel.asColor().getRGB());
 			}
 		}
 	}
 
 	public void renderChunk(Chunk chunk) {
 		if (!isLoaded())
 			return;
 		
 		MapPos pos = new MapPos(chunk, mapLayer);
 		
 		int increment = (int)Math.max(1.0F, 1.0F / mapLayer.pixelSize);
 		
 		PixelColor colorPixel = new PixelColor();
 		PixelColor heightPixel = new PixelColor();
 
 		byte[] biomes = chunk.getBiomeArray();
 		
 		for (int xOffset = 0; xOffset < 16; xOffset += increment) {
 			for (int zOffset = 0; zOffset < 16; zOffset += increment) {
 				
 				int pixelX = pos.getX(xOffset);
 				int pixelZ = pos.getZ(zOffset);
 				
 				calculateColor(chunk, xOffset, zOffset, biomes, colorPixel, heightPixel);
 				
 				// Adjust brightness for height.
 				//if (blockId != Block.lavaMoving.blockID && blockId != Block.lavaStill.blockID && blockId != Block.waterMoving.blockID && blockId != Block.waterStill.blockID)
 				colorPixel.scale((Math.min(1.25F, Math.max(0.75F, getBlockHeight(heightPixel) / .65F / 100.0F))) / 8 * 8);
 				
 				float darken = 0.85F;
				float lighten = 1.06F;
 				
 				if (mapLayer.pixelSize < 1)
 					darken = 0.91F;
 				
 				// Compare to northern block
 				int northPosZ = zOffset - 1;
 				if (northPosZ >= 0) {
 					//if (mapLayer.pixelSize < 1)
 					//	colorPixel.composite(new Color(colorImage.getRGB(posX, northPosZ)), 0.25F);
 					
 					int northHeight = getHeightAt(chunk, xOffset, northPosZ);
 					//int x = heightImage.getRGB(pixelX, pixelZ - 1);
 					//if (x > 0)
 					//	Integer.toString(x);
 					if (northHeight > getBlockHeight(heightPixel))
 						colorPixel.scale(darken);
 					else if (northHeight < getBlockHeight(heightPixel))
						colorPixel.scale(lighten);
 				}
 				
 				// Compare to eastern block
 				int eastPosX = xOffset - 1;
 				if (eastPosX >= 0) {
 					int eastHeight = getHeightAt(chunk, eastPosX, zOffset);
 					if (eastHeight > getBlockHeight(heightPixel))
 						colorPixel.scale(darken);
 					else if (eastHeight < getBlockHeight(heightPixel))
						colorPixel.scale(lighten);
 				}
 				
 				drawPixels(pixelX, pixelZ, colorPixel, heightPixel);
 				
 				colorPixel.clear();
 				heightPixel.clear();
 			}
 		}
 	}
 	
 	protected void calculateColor(Chunk chunk, int x, int z, byte[] biomes, PixelColor colorPixel, PixelColor heightPixel) {
 		float count = 0.0F;
 		int red = 0;
 		int green = 0;
 		int blue = 0;
 		float alpha = 0;
 		int height = 0;
 		int biomeId = 255;
 		int[] biomeIdCounts = new int[256];
 		
 		colorPixel.clear();
 		heightPixel.clear();
 		
 		BlockColor[] stack;
 
 		int increment = (int)Math.max(1.0F, 1.0F / mapLayer.pixelSize);
 		
 		for (int iX = 0; iX < increment; iX++) {
 			for (int iZ = 0; iZ < increment; iZ++) {
 				int posY = Math.max(0, chunk.getHeightValue(x + iX, z +iZ));
 				
 				while (chunk.getBlockID(x + iX, posY, z + iZ) == 0 && posY > 0)
 					posY--;
 				
 				// Get the stack of BlockColors for this exact block location.
 				stack = getBlockColorStack(chunk, x + iX, posY, z + iZ, biomes == null ? -1 : biomes[z << 4 | x] & 255);
 				if (stack.length > 0) {
 					count++;
 					
 					// Create a composite color from the BlockColor stack.
 					PixelColor stackColor = new PixelColor();
 					for (int i = stack.length - 1; i >= 0; i--) {
 						stack[i].addTo(stackColor);
 						if (biomes != null && biomes[z << 4 | x] != 255) {
 							if (biomeId == 255 || biomeIdCounts[biomeId] < ++biomeIdCounts[biomes[z << 4 | x]]) {
 								biomeId = biomes[z << 4 | x];
 							}
 						}
 					}
 					
 					// Add to the average color for the map pixel.
 					red += stackColor.red * stackColor.alpha;
 					green += stackColor.green * stackColor.alpha;
 					blue += stackColor.blue * stackColor.alpha;
 					alpha += stackColor.alpha;
 					height += posY;
 				}
 			}
 		}
 		
 		if (count > 0.0F) {
 			colorPixel.set(red / count, green / count, blue / count, alpha / count);
 			heightPixel.set(new Color(0, Math.round(height / count), biomeId));
 			
 			if (getBlockHeight(heightPixel) != Math.round(height / count))
 				ModLoader.outputError("HeightPixel height " + getBlockHeight(heightPixel) + " (color: " + heightPixel.red + "," + heightPixel.green + "," + heightPixel.blue + ") doesn't match " + Math.round(height / count) + " for " + x + "," + z + " for chunk " + chunk.xPosition + "," + chunk.zPosition + " in layer " + mapLayer.chunksPerImage);
 		}
 		else {
 			System.out.println("Unable to calc color for " + x + "," + z + " for chunk " + chunk.xPosition + "," + chunk.zPosition);
 		}
 	}
 	
 	protected BlockColor[] getBlockColorStack(Chunk chunk, int x, int y, int z, int biomeId) {
 		List<BlockColor> colorStack = new ArrayList<BlockColor>();
 		int[] alphaLimits = null;
 		
 		while (y >= 0) {
 			int blockId = chunk.getBlockID(x, y, z);
 			if (isRenderedBlock(blockId)) {
 				BlockColor blockColor = BlockColor.fromBlockList(mapLayer.map.blockColors[blockId], blockId, biomeId, chunk, x, y, z);
 				if (blockColor != null) {
 					
 					// Only create the array when needed.
 					if (blockColor.alphaLimit > 0 && alphaLimits == null)
 						alphaLimits = new int[mapLayer.map.blockColors.length];
 					
 					if (blockColor.alphaLimit <= 0 || ++alphaLimits[blockId] <= blockColor.alphaLimit) {
 						colorStack.add(blockColor);
 						
 						// Stop once we hit a opaque block.
 						if (!blockColor.isTransparent())
 							break;
 					}
 					else if (blockColor.alphaLimit > 0 && blockColor.solidAfterAlphaLimit && alphaLimits[blockId] > blockColor.alphaLimit) {
 						colorStack.add(blockColor.asSolid());
 						break;
 					}
 				}
 			}
 			
 			y--;
 		}
 		
 		return colorStack.toArray(new BlockColor[colorStack.size()]);
 	}
 	
 	protected int getHeightAt(Chunk chunk, int x, int z) {
 		int y = Math.max(0, chunk.getHeightValue(x, z));
 		int blockId = chunk.getBlockID(x, y, z);
 		
 		while (!isRenderedBlock(blockId) && y > 0)
 			blockId = chunk.getBlockID(x, --y, z);
 		
 		return blockId > 0 ? y : -1;
 	}
 	
 	protected boolean isRenderedBlock(int blockId) {
 		return blockId > 0 && blockId < mapLayer.map.blockColors.length && mapLayer.map.blockColors[blockId] != null;
 	}
 
 	protected void save() throws Exception {
 		if (ImageIO.write(colorImage, "png", mapLayer.map.mod.tempSave)) {
 			if (!colorImageFile.exists() || colorImageFile.delete()) {
 				if (mapLayer.map.mod.tempSave.renameTo(colorImageFile)) {
 					colorImageFile.setReadable(true, false);
 				}
 				else {
 					ModLoader.outputError(mapLayer.map.mod.getName() + "'s " + MapImage.class.getSimpleName() + " failed to move the temp image to: " + colorImageFile.getPath());
 				}
 			}
 			else {
 				ModLoader.outputError(mapLayer.map.mod.getName() + "'s " + MapImage.class.getSimpleName() + " failed to delete the old image at: " + colorImageFile.getPath());
 			}
 		}
 		else {
 			ModLoader.outputError(mapLayer.map.mod.getName() + "'s " + MapImage.class.getSimpleName() + " failed to save the color image to the temp file for " + chunkX + "," + chunkZ);
 		}
 		if (ImageIO.write(heightImage, "png", mapLayer.map.mod.tempSave)) {
 			if (!heightImageFile.exists() || heightImageFile.delete()) {
 				if (mapLayer.map.mod.tempSave.renameTo(heightImageFile)) {
 					heightImageFile.setReadable(true, false);
 				}
 				else {
 					ModLoader.outputError(mapLayer.map.mod.getName() + "'s " + MapImage.class.getSimpleName() + " failed to move the temp image to: " + heightImageFile.getPath());
 				}
 			}
 			else {
 				ModLoader.outputError(mapLayer.map.mod.getName() + "'s " + MapImage.class.getSimpleName() + " failed to delete the old image at: " + heightImageFile.getPath());
 			}
 		}
 		else {
 			ModLoader.outputError(mapLayer.map.mod.getName() + "'s " + MapImage.class.getSimpleName() + " failed to save the height image to the temp file for " + chunkX + "," + chunkZ);
 		}
 	}
 	
 	public static int getBlockHeight(PixelColor color) {
 		return Math.round(color.green);
 	}
 	
 	public static int getBlockBiome(PixelColor color) {
 		return Math.round(color.blue);
 	}
 }
