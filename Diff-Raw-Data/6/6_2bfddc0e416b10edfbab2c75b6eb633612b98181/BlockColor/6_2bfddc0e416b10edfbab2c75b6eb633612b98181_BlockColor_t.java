 package btwmod.livemap;
 
 import java.awt.Color;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.minecraft.src.Block;
 
 public class BlockColor {
 
 	private static Map<String, Set<Block>> blockNameLookup = null;
 
 	public static final float opaque = 1.0F;
 	public static final int noAlphaLimit = 0;
 
 	public static Set<Block> getBlocksByName(String name) {
 		if (blockNameLookup == null) {
 			blockNameLookup = new HashMap<String, Set<Block>>();
 			
 			for (int i = 0; i < Block.blocksList.length; i++) {
 				if (Block.blocksList[i] != null) {
 					Set<Block> blocks = blockNameLookup.get(Block.blocksList[i].getBlockName());
 					if (blocks == null)
 						blockNameLookup.put(Block.blocksList[i].getBlockName(), blocks = new LinkedHashSet<Block>());
 					
 					blocks.add(Block.blocksList[i]);
 				}
 			}
 		}
 
 		return blockNameLookup.get(name);
 	}
 
 	public final String blockName;
 	
 	public final int blockId;
 	
 	public final int red;
 	public final int blue;
 	public final int green;
 	
 	public final int[] biomeIds;
 	
 	public final float alpha;
 	public final int alphaLimit;
 	public final boolean solidAfterAlphaLimit;
 	
 	public final int metadata;
 	public final int metadataMask;
 	public final boolean hasMetadata;
 	
 	public final float hue;
 	public final float hueAlpha;
 	
 	private BlockColor asSolid = null;
 
 	public BlockColor(String blockName, int blockId, int red, int green, int blue, float alpha, int alphaLimit, boolean solidAfterAlphaLimit, int[] biomeIds) {
 		this(blockName, blockId, red, green, blue, alpha, alphaLimit, solidAfterAlphaLimit, 0.0F, 0, 0, false, biomeIds);
 	}
 
 	public BlockColor(String blockName, int blockId, int red, int green, int blue, float alpha, int alphaLimit, boolean solidAfterAlphaLimit, float hueAlpha, int[] biomeIds) {
 		this(blockName, blockId, red, green, blue, alpha, alphaLimit, solidAfterAlphaLimit, hueAlpha, 0, 0, false, biomeIds);
 	}
 
 	public BlockColor(String blockName, int blockId, int red, int green, int blue, float alpha, int alphaLimit, boolean solidAfterAlphaLimit, int metadata, int metadataMask, int[] biomeIds) {
 		this(blockName, blockId, red, green, blue, alpha, alphaLimit, solidAfterAlphaLimit, 0.0F, metadata, metadataMask, true, biomeIds);
 	}
 
 	public BlockColor(String blockName, int blockId, int red, int green, int blue, float alpha, int alphaLimit, boolean solidAfterAlphaLimit, float hueAlpha, int metadata, int metadataMask, int[] biomeIds) {
 		this(blockName, blockId, red, green, blue, alpha, alphaLimit, solidAfterAlphaLimit, hueAlpha, metadata, metadataMask, true, biomeIds);
 	}
 
 	private BlockColor(String blockName, int blockId, int red, int green, int blue, float alpha, int alphaLimit, boolean solidAfterAlphaLimit, float hueAlpha, int metadata, int metadataMask, boolean hasMetadata, int[] biomeIds) {
 		this.blockName = blockName;
 		
 		this.blockId = blockId;
 		
 		this.red = red;
 		this.green = green;
 		this.blue = blue;
 		
 		this.biomeIds = biomeIds;
 		
 		this.alpha = alpha;
 		this.alphaLimit = alphaLimit;
 		this.solidAfterAlphaLimit = solidAfterAlphaLimit;
 		
 		this.hueAlpha = hueAlpha;
 		
 		this.metadata = metadata;
 		this.metadataMask = metadataMask;
 		this.hasMetadata = hasMetadata;
 		
 		for (int biomeId : biomeIds)
 			if (biomeId < -1 || biomeId > 255)
 				throw new IllegalArgumentException();
 		
 		if (blockId < 0 || blockId > 255
 				|| red < 0 || blue < 0 || green < 0 || alpha < 0.0F
 				|| red > 255 || blue > 255 || green > 255 || alpha > 1.0F
 				|| hueAlpha < 0.0F || hueAlpha > 1.0F)
 			
 			throw new IllegalArgumentException();
 		
 		this.hue = Color.RGBtoHSB(red, green, blue, null)[0];
 	}
 
 	public boolean hasFilter() {
 		return blockId > 0 || biomeIds.length > 0 || hasMetadata;
 	}
 	
 	public boolean isStricterThan(BlockColor color) {
 		if (blockId > 0 && color.blockId <= 0)
 			return true;
 		
 		if (hasMetadata && !color.hasMetadata)
 			return true;
 		
 		if (biomeIds.length > 0 && color.biomeIds.length == 0)
 			return true;
 		
 		return false;
 	}
 	
 	public BlockColor asSolid() {
 		if (!isTransparent())
 			return this;
 		
 		if (asSolid == null)
 			asSolid = new BlockColor(blockName, 0, red, green, blue, 1.0F, 0, false, 0.0F, metadata, metadataMask, hasMetadata, biomeIds);
 		
 		return asSolid;
 	}
 
 	public boolean isTransparent() {
 		return alpha < 1.0F;
 	}
 
 	public void addTo(PixelColor color) {
 		addTo(color, alpha);
 	}
 
 	public void addTo(PixelColor color, float alpha) {
 		if (hueAlpha > 0.0F)
 			color.hue(hue, hueAlpha);
 		
 		color.composite(red, green, blue, alpha);
 	}
 	
 	public Color asColor(boolean withAlpha) {
 		if (withAlpha)
 			return new Color(red, green, blue, alpha * 255);
 		else
 			return new Color(red, green, blue, 255);
 	}
 	
 	public int asRGB() {
 		return asColor(false).getRGB();
 	}
 	
 	public int asRGBA() {
 		return asColor(true).getRGB();
 	}
 	
 	public static float clamp_float(float par0, float par1, float par2) {
 		return par0 < par1 ? par1 : (par0 > par2 ? par2 : par0);
 	}
 	
 	public static BlockColor fromBlock(Block block) {
 		if (block == null)
 			return null;
 		
 		int mapColor = block.blockMaterial.materialMapColor.colorValue;
 		int red = mapColor >> 16 & 255;
 		int green = mapColor >> 8 & 255;
 		int blue = mapColor & 255;
 		
 		return new BlockColor(block.getBlockName(), 0, red, green, blue, BlockColor.opaque, BlockColor.noAlphaLimit, true, new int[0]);
 	}
 
 	public static BlockColor fromConfigLine(String line) {
 		if (line == null || line.trim().length() < 2 || line.trim().charAt(0) == '#')
 			return null;
 
 		String[] columns = line.split("[ \t]+");
 
 		// Make sure the block name is not an empty string.
 		if (columns.length > 0 && columns[0].length() == 0)
 			return null;
 		
 		int blockId = 0;
 
 		int red = 0;
 		int green = 0;
 		int blue = 0;
 		
 		int[] biomeIds = new int[0];
 		
 		float alpha = opaque;
 		int alphaLimit = noAlphaLimit;
 		boolean solidAfterAlphaLimit = false;
 		
 		float hueAlpha = 0.0F;
 		
 		int metadata = 0;
 		int metadataMask = 0xFF;
 		boolean hasMetadata = false;
 
 		for (int i = 1; i < columns.length; i++) {
 			if (columns[i].equalsIgnoreCase("blockmaterial")) {
 				Set<Block> blocks = getBlocksByName(columns[0]);
 
 				// Fail if the block name wasn't found.
 				if (blocks == null || blocks.size() == 0)
 					return null;
 
 				// TODO: OK that we're just using the first material for the block name?
 				for (Block block : blocks) {
 					int mapColor = block.blockMaterial.materialMapColor.colorValue;
 					red = mapColor >> 16 & 255;
 					green = mapColor >> 8 & 255;
 					blue = mapColor & 255;
 					break;
 				}
 
 			} else if (columns[i].length() > 0) {
 				try {
 					int marker;
 					switch (columns[i].charAt(0)) {
 						case 'r':
 							if (columns[i].charAt(1) == '+')
 								red += Integer.parseInt(columns[i].substring(2));
 							else if (columns[i].charAt(1) == '-')
 								red -= Integer.parseInt(columns[i].substring(2));
 							else
 								red = Integer.parseInt(columns[i].substring(1));
 							break;
 						case 'g':
 							if (columns[i].charAt(1) == '+')
 								green += Integer.parseInt(columns[i].substring(2));
 							else if (columns[i].charAt(1) == '-')
 								green -= Integer.parseInt(columns[i].substring(2));
 							else
 								green = Integer.parseInt(columns[i].substring(1));
 							break;
 						case 'b':
 							if (columns[i].charAt(1) == '+')
 								blue += Integer.parseInt(columns[i].substring(2));
 							else if (columns[i].charAt(1) == '-')
 								blue -= Integer.parseInt(columns[i].substring(2));
 							else
 								blue = Integer.parseInt(columns[i].substring(1));
 							break;
 						case 'a':
 							// Reset this in case alpha is specified twice by accident.
 							solidAfterAlphaLimit = false;
 
 							marker = columns[i].indexOf(':');
 							if (marker >= 0) {
 								
 								// Check for the solid after alpha limit flag.
 								if (columns[i].charAt(columns[i].length() - 1) == 's') {
 									solidAfterAlphaLimit = true;
 									columns[i] = columns[i].substring(0, columns[i].length() - 1);
 								}
 								
 								alphaLimit = Integer.parseInt(columns[i].substring(marker + 1));
 								columns[i] = columns[i].substring(0, marker);
 							}
 							
 							if (columns[i].charAt(1) == '+')
 								alpha += Float.parseFloat(columns[i].substring(2));
 							else if (columns[i].charAt(1) == '-')
 								alpha -= Float.parseFloat(columns[i].substring(2));
 							else
 								alpha = Float.parseFloat(columns[i].substring(1));
 							break;
 						case 'h':
 							if (columns[i].substring(1).indexOf('.') > 0)
 								hueAlpha = Float.parseFloat(columns[i].substring(1));
 							else
 								hueAlpha = (float)Integer.parseInt(columns[i].substring(1)) / 360.0F;
 							break;
 						case '+':
 							red += Math.max(0, Math.min(255, Integer.parseInt(columns[i].substring(1))));
 							green += Math.max(0, Math.min(255, Integer.parseInt(columns[i].substring(1))));
 							blue += Math.max(0, Math.min(255, Integer.parseInt(columns[i].substring(1))));
 							break;
 						case '-':
 							red -= Math.max(0, Math.min(255, Integer.parseInt(columns[i].substring(1))));
 							green -= Math.max(0, Math.min(255, Integer.parseInt(columns[i].substring(1))));
 							blue -= Math.max(0, Math.min(255, Integer.parseInt(columns[i].substring(1))));
 							break;
 						case '#':
							red = Integer.parseInt(columns[i].substring(1, 3), 16);
							green = Integer.parseInt(columns[i].substring(3, 5), 16);
							blue = Integer.parseInt(columns[i].substring(5, 7), 16);
 							break;
 						case 'm':
 
 							marker = columns[i].indexOf(':');
 							if (marker >= 0) {
 								metadataMask = Integer.parseInt(columns[i].substring(marker + 1));
 								columns[i] = columns[i].substring(0, marker);
 							}
 							
 							metadata = Integer.parseInt(columns[i].substring(1));
 							hasMetadata = true;
 							break;
 						case 'i':
 							blockId = Integer.parseInt(columns[i].substring(1));
 							break;
 						case 'o':
 							String[] split = columns[i].substring(1).split("[,;]+");
 							biomeIds = new int[split.length];
 							for (int iSplit = 0; iSplit < split.length; iSplit++) {
 								biomeIds[iSplit] = Integer.parseInt(split[iSplit]);
 							}
 							break;
 					}
 				} catch (NumberFormatException e) {
 					return null;
 				} catch (IndexOutOfBoundsException e) {
 					return null;
 				}
 			}
 		}
 
 		try {
 			return new BlockColor(columns[0], blockId, red, green, blue, alpha, alphaLimit, solidAfterAlphaLimit, hueAlpha, metadata, metadataMask, hasMetadata, biomeIds);
 		}
 		catch (IllegalArgumentException e) {
 			return null;
 		}
 	}
 }
