 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.awt.Color;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 
 import org.bukkit.Material;
 
 public class DefaultColorPalette implements ColorPalette {
 
 	private final Map<Material, SimpleMaterialColor> colors = new HashMap<Material, SimpleMaterialColor>();
 	
 	public DefaultColorPalette() {
 		colors.put(Material.AIR, new SimpleMaterialColor(new Color(0, 0, 0, 0)));
 		colors.put(Material.STONE, new SimpleMaterialColor(new Color(128, 132, 136)));
 		colors.put(Material.VINE, new SimpleMaterialColor(new Color(0, 0xDD, 0, 32)));
 		colors.put(Material.WATER_LILY, new SimpleMaterialColor(new Color(0, 0xDD, 0, 32)));
 		colors.put(Material.YELLOW_FLOWER, new SimpleMaterialColor(new Color(0xDD, 0xDD, 0, 192)));
 		colors.put(Material.DIRT, new SimpleMaterialColor(new Color(134, 96, 67)));
 		colors.put(Material.COBBLESTONE, new SimpleMaterialColor(new Color(100, 100, 100)));
 		colors.put(Material.WOOD, new SimpleMaterialColor(new Color(157, 128, 79)));
 		colors.put(Material.SAPLING, new SimpleMaterialColor(new Color(120, 205, 120, 64)));
 		colors.put(Material.BEDROCK, new SimpleMaterialColor(new Color(84, 84, 84)));
 		colors.put(Material.STATIONARY_LAVA, new SimpleMaterialColor(new Color(255, 108, 16))); 
 		colors.put(Material.LAVA, new SimpleMaterialColor(new Color(255, 108, 16)));
 		colors.put(Material.SAND, new SimpleMaterialColor(new Color(218, 210, 158)));
 		colors.put(Material.GRAVEL, new SimpleMaterialColor(new Color(136, 126, 126)));
 		colors.put(Material.GOLD_ORE, new SimpleMaterialColor(new Color(143, 140, 125)));
 		colors.put(Material.IRON_ORE, new SimpleMaterialColor(new Color(136, 130, 127)));
 		colors.put(Material.COAL_ORE, new SimpleMaterialColor(new Color(115, 115, 115)));
 		colors.put(Material.LOG, new SimpleMaterialColor(new Color(102, 81, 51)));
 		colors.put(Material.LAPIS_ORE, new SimpleMaterialColor(new Color(102, 112, 134, 255)));
 		colors.put(Material.LAPIS_BLOCK, new SimpleMaterialColor(new Color(29, 71, 165, 255)));
 		colors.put(Material.DISPENSER, new SimpleMaterialColor(new Color(107, 107, 107, 255)));
 		colors.put(Material.SANDSTONE, new SimpleMaterialColor(new Color(218, 210, 158, 255)));
 		colors.put(Material.NOTE_BLOCK, new SimpleMaterialColor(new Color(100, 67, 50, 255)));
 		colors.put(Material.GOLD_BLOCK, new SimpleMaterialColor(new Color(255, 237, 140, 255)));
 		colors.put(Material.IRON_BLOCK, new SimpleMaterialColor(new Color(217, 217, 217, 255)));
 		colors.put(Material.DOUBLE_STEP, new SimpleMaterialColor(new Color(200, 200, 200, 255)));
 		colors.put(Material.STEP, new SimpleMaterialColor(new Color(200, 200, 200, 255)));
 		colors.put(Material.BRICK, new SimpleMaterialColor(new Color(86, 35, 23, 255))); 
 		colors.put(Material.TNT, new SimpleMaterialColor(new Color(255, 0, 0, 255)));
 		colors.put(Material.BOOKSHELF, new SimpleMaterialColor(new Color(191, 169, 116, 255)));
 		colors.put(Material.MOSSY_COBBLESTONE, new SimpleMaterialColor(new Color(127, 174, 125, 255)));
 		colors.put(Material.OBSIDIAN, new SimpleMaterialColor(new Color(17, 13, 26, 255))); 
 		colors.put(Material.TORCH, new SimpleMaterialColor(new Color(255, 225, 96, 208))); 
 		colors.put(Material.FIRE, new SimpleMaterialColor(new Color(224, 174, 21, 255))); 
 		colors.put(Material.WOOD_STAIRS, new SimpleMaterialColor(new Color(191, 169, 116, 255)));
 		colors.put(Material.CHEST, new SimpleMaterialColor(new Color(191, 135, 2, 255)));
 		colors.put(Material.REDSTONE_WIRE, new SimpleMaterialColor(new Color(111, 1, 1, 255))); 
 		colors.put(Material.DIAMOND_ORE, new SimpleMaterialColor(new Color(129, 140, 143, 255)));
 		colors.put(Material.DIAMOND_BLOCK, new SimpleMaterialColor(new Color(45, 166, 152, 255))); 
 		colors.put(Material.WORKBENCH, new SimpleMaterialColor(new Color(169, 107, 0, 255))); 
 		colors.put(Material.CROPS, new SimpleMaterialColor(new Color(144, 188, 39, 128))); 
 		colors.put(Material.SOIL, new SimpleMaterialColor(new Color(134, 96, 67, 255))); 
 		colors.put(Material.FURNACE, new SimpleMaterialColor(new Color(188, 188, 188, 255)));
 		colors.put(Material.BURNING_FURNACE, new SimpleMaterialColor(new Color(221, 221, 221, 255))); 
 		colors.put(Material.RAILS, new SimpleMaterialColor(new Color(120, 120, 120, 128)));
 		colors.put(Material.COBBLESTONE_STAIRS, new SimpleMaterialColor(new Color(120, 120, 120, 128)));
 		colors.put(Material.STONE_PLATE, new SimpleMaterialColor(new Color(120, 120, 120, 255)));
 		colors.put(Material.REDSTONE_ORE, new SimpleMaterialColor(new Color(143, 125, 125, 255)));
 		colors.put(Material.GLOWING_REDSTONE_ORE, new SimpleMaterialColor(new Color(163, 145, 145, 255)));
 		colors.put(Material.REDSTONE_TORCH_OFF, new SimpleMaterialColor(new Color(181, 140, 64, 32))); 
 		colors.put(Material.REDSTONE_TORCH_ON, new SimpleMaterialColor(new Color(255, 0, 0, 176))); 
 		colors.put(Material.STONE_BUTTON, new SimpleMaterialColor(new Color(128, 128, 128, 16))); 
 		colors.put(Material.SNOW, new SimpleMaterialColor(new Color(245, 245, 245, 255)));
 		colors.put(Material.ICE, new SimpleMaterialColor(new Color(150, 192, 255, 150)));
 		colors.put(Material.SNOW_BLOCK, new SimpleMaterialColor(new Color(205, 205, 205, 255)));
 		colors.put(Material.CACTUS, new SimpleMaterialColor(new Color(85, 107, 47, 255))); 
 		colors.put(Material.CLAY, new SimpleMaterialColor(new Color(144, 152, 168, 255)));
 		colors.put(Material.SUGAR_CANE_BLOCK, new SimpleMaterialColor(new Color(193, 234, 150, 255)));
 		colors.put(Material.JUKEBOX, new SimpleMaterialColor(new Color(125, 66, 44, 255))); 
 		colors.put(Material.FENCE, new SimpleMaterialColor(new Color(88, 54, 22, 200))); 
 		colors.put(Material.PUMPKIN, new SimpleMaterialColor(new Color(227, 144, 29, 255))); 
 		colors.put(Material.NETHERRACK, new SimpleMaterialColor(new Color(194, 115, 115, 255)));
 		colors.put(Material.SOUL_SAND, new SimpleMaterialColor(new Color(121, 97, 82, 255))); 
 		colors.put(Material.GLOWSTONE, new SimpleMaterialColor(new Color(255, 188, 94, 255))); 
 		colors.put(Material.PORTAL, new SimpleMaterialColor(new Color(60, 13, 106, 127))); 
 		colors.put(Material.JACK_O_LANTERN, new SimpleMaterialColor(new Color(227, 144, 29, 255))); 
 		colors.put(Material.CAKE_BLOCK, new SimpleMaterialColor(new Color(228, 205, 206, 255)));
 		colors.put(Material.RED_ROSE, new SimpleMaterialColor(new Color(111, 1, 1, 255)));
 		colors.put(Material.SMOOTH_BRICK, new SimpleMaterialColor(new Color(128, 132, 136)));
 		colors.put(Material.SMOOTH_STAIRS, new SimpleMaterialColor(new Color(128, 132, 136)));
 		colors.put(Material.MELON_BLOCK, new SimpleMaterialColor(new Color(153, 194, 110, 255)));
 		colors.put(Material.MELON_STEM, new SimpleMaterialColor(new Color(102, 81, 51, 64)));
 		colors.put(Material.PUMPKIN_STEM, new SimpleMaterialColor(new Color(102, 81, 51, 64)));
 		colors.put(Material.MYCEL, new SimpleMaterialColor(new Color(0x7b6e83)));
		colors.put(Material.HUGE_MUSHROOM_1, new SimpleMaterialColor(new Color(111, 1, 1, 255)));
		colors.put(Material.HUGE_MUSHROOM_2, new SimpleMaterialColor(new Color(102, 81, 51)));
 		colors.put(Material.DEAD_BUSH, new SimpleMaterialColor(new Color(157, 128, 79, 70)));
 	}
 	
 	@Override
 	public MaterialColor getMaterialColor(Material material) {
 		
 		if (material.equals(Material.WOOL)) {
 			return new WoolMaterialColor();
 		}
 		
 		if (material.equals(Material.GRASS)) {
 			return new GrassMaterialColor();
 		}
 		
 		if (material.equals(Material.LONG_GRASS)) {
 			return new LongGrassMaterialColor();
 		}
 		
 		if (material.equals(Material.STATIONARY_WATER)) {
 			return new WaterMaterialColor();
 		}
 		
 		if (material.equals(Material.LEAVES)) {
 			return new FoliageMaterialColor();
 		}
 		
 		return colors.get(material);
 		
 	}
 	
 	public static Color setAlpha(Color color, int a) {
 		return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
 	}
 	
 	public static Color computeShadedColor(Color color, double multiplier) {		
 		int r = color.getRed();
 		int g = color.getGreen();
 		int b = color.getBlue();
 		
 		if (multiplier < 1) {
 			r *= multiplier;
 			g *= multiplier;
 			b *= multiplier;	
 		}
 		
 		r *= multiplier;
 		g *= multiplier;
 		b *= multiplier;
 	
 		r = Math.min(r, 255);
 		g = Math.min(g, 255);
 		b = Math.min(b, 255);
 		
 		r = Math.max(r, 0);
 		g = Math.max(g, 0);
 		b = Math.max(b, 0);
 		
 		return new Color(r, g, b, color.getAlpha());
 	}
 	
 	public static Coordinate getBiomeColorIndex(double rainfall, double temperature) {
 		rainfall *= temperature;
 		int i = (int)((1.0d - temperature) * 255.0d);
 		int j = (int)((1.0d - rainfall) * 255.0d);
 		
 		// TODO: Refactor without using Coordinate
 		return new Coordinate(i, j, Coordinate.LEVEL_BLOCK);
 	}
 
 }
