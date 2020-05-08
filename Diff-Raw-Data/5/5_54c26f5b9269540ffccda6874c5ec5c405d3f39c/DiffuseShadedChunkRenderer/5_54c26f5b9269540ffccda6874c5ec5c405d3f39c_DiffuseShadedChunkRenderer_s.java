 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.ChunkRenderer;
 import net.nabaal.majiir.realtimerender.image.ImageProvider;
 
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Material;
 import org.jscience.mathematics.vector.Float64Vector;
 
 public class DiffuseShadedChunkRenderer implements ChunkRenderer {
 	
 	private final NormalMap normalMap;
 	private final ImageProvider imageProvider;
 	private final ColorPalette colorPalette;
 	
 	public DiffuseShadedChunkRenderer(ImageProvider imageProvider, NormalMap normalMap, ColorPalette colorPalette) {				
 		this.normalMap = normalMap;
 		this.imageProvider = imageProvider;
 		this.colorPalette = colorPalette;
 	}
 
 	@Override
 	public void render(ChunkSnapshot chunkSnapshot) {
 		BufferedImage image = ImageProvider.createImage(Coordinate.SIZE_CHUNK);
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				int ymax = chunkSnapshot.getHighestBlockYAt(x, z);
 				// TODO: Handle cases where we can start at an opaque non-terrain block
 				for (int y = getTerrainHeight(chunkSnapshot, x, z); y <= ymax; y++) {
 					Color color;
 					Material material = Material.getMaterial(chunkSnapshot.getBlockTypeId(x, y, z));
 					color = getMaterialColor(material, chunkSnapshot, x, y, z);
 					g.setColor(color);
 					g.fillRect(x, z, 1, 1);
 					
 					if (TerrainHelper.isTerrain(material)) {
 						double shading = computeDiffuseShading(chunkSnapshot, x, z);
 						if (shading >= 0) {
 							g.setColor(computeShadeColor(shading));
 							g.fillRect(x, z, 1, 1);
 						}
 					}
 				}
 			}
 		}
 		imageProvider.setImage(Coordinate.fromSnapshot(chunkSnapshot), image);
 	}
 	
 	private Color getMaterialColor(Material material, ChunkSnapshot chunkSnapshot, int x, int y, int z) {
		x += (chunkSnapshot.getX() * 16);
		z += (chunkSnapshot.getZ() * 16);
 		
		Color color = colorPalette.getMaterialColor(material).getColor(chunkSnapshot.getBlockData(x, y, z), x, z, chunkSnapshot.getRawBiomeRainfall(x, z), chunkSnapshot.getRawBiomeTemperature(x, z), chunkSnapshot.getBiome(x, z));
 		
 		if (color != null) {
 			if (material.equals(Material.WATER) && isUnderWater(chunkSnapshot, x, y, z)) {
 				color = setAlpha(color, 32);
 			}
 		} else {
 			//RealtimeRender.getLogger().warning(String.format("RealtimeRender: missing color for material '%s'!", material.toString()));
 			color = new Color(0xFF00FF);
 		}
 		return color;
 	}
 	
 	private static boolean isUnderWater(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
 		return Material.getMaterial(chunkSnapshot.getBlockTypeId(x, y + 1, z)) == Material.STATIONARY_WATER;
 	}
 	
 	private static Color setAlpha(Color color, int a) {
 		return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
 	}
 	
 	private static Color computeShadeColor(double shading) {
 		if (shading < 0.5) {
 			return new Color(0, 0, 0, (int) Math.floor((1 - (shading * 2)) * 255));
 		} else if (shading < 1.0) {
 			return new Color(255, 255, 255, (int) Math.floor(((shading * 2) - 1) * 255));
 		} else {
 			return new Color(255, 255, 255, 255);
 		}
 	}
 	
 	private double computeDiffuseShading(ChunkSnapshot chunkSnapshot, int x, int z) {	
 		Float64Vector n = this.normalMap.getNormal(Coordinate.fromSnapshot(chunkSnapshot).zoomIn(Coordinate.OFFSET_BLOCK_CHUNK).plus(new Coordinate(x, z, Coordinate.LEVEL_BLOCK)));
 		Float64Vector light = Float64Vector.valueOf(-1, -1, -1);
 		if (n == null) {
 			return -1;
 		}
 		double shading = n.times(light).divide((n.norm().times(light.norm()))).doubleValue();
 		return ((shading + 1) * 0.4) + 0.15;
 	}
 	
 	private static int getTerrainHeight(ChunkSnapshot chunkSnapshot, int x, int z) {
 		int y = chunkSnapshot.getHighestBlockYAt(x, z);
 		Material material;
 		do {
 			y--;
 			int id = chunkSnapshot.getBlockTypeId(x, y, z);
 			material = Material.getMaterial(id);
 		} while (!TerrainHelper.isTerrain(material) && (y > 0));
 		return y;
 	}
 
 }
