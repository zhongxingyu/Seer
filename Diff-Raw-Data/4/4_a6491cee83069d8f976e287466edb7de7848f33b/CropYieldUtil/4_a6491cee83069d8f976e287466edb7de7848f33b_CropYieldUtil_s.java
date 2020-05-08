 package com.github.namrufus.harvest_time.crop_growth.seasonal.util;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 public class CropYieldUtil {
 	private static class Item {
 		public Material material;
 		public byte data;
 		public double count;
 		
 		public Item(Material material, int data, double count) {
 			this.material = material;
 			this.data = (byte)data;
 			this.count = count;
 		}
 	}
 	
 	private static class CropYieldType {
 		// what each unit of "yield value" consists of
 		public Item[] yieldUnit;
 		// the average vanilla yield of the crop (best case fortune enchantments etc)
 		// minus the amount of crop needed to propagate the crop
 		public double averageVanillaHarvestYield;
 		
 		public CropYieldType(Item[] yieldUnit, double averageVanillaHarvestYield) {
 			this.yieldUnit = yieldUnit;
 			this.averageVanillaHarvestYield = averageVanillaHarvestYield;
 		}
 	}
 	
 	// ================================================================================================================
 	private static Map<Material, CropYieldType> crops = new HashMap<Material, CropYieldType>();
 	
 	static {
 		crops.put(Material.CROPS, new CropYieldType(new Item[]{new Item(Material.WHEAT, 0, 1.0), new Item(Material.SEEDS, 0, 0.25)}, 1.0));
 		
 		crops.put(Material.CARROT, new CropYieldType(new Item[]{new Item(Material.CARROT_ITEM, 0, 1.0)}, 3.0));
 		crops.put(Material.POTATO, new CropYieldType(new Item[]{new Item(Material.POTATO_ITEM, 0, 1.0)}, 3.0));
 		
 		crops.put(Material.NETHER_WARTS, new CropYieldType(new Item[]{new Item(Material.NETHER_STALK, 0, 1.0)}, 3.5));
 		
		crops.put(Material.MELON_STEM, new CropYieldType(new Item[]{new Item(Material.MELON, 0, 1.0)}, 1.0));
		crops.put(Material.PUMPKIN_STEM, new CropYieldType(new Item[]{new Item(Material.PUMPKIN, 0, 1.0)}, 1.0));
 		
 		crops.put(Material.COCOA, new CropYieldType(new Item[]{new Item(Material.INK_SACK, DyeColor.BROWN.getDyeData(), 1.0)}, 1.5));
 	}
 	
 	// ================================================================================================================
 	public static boolean cropFailureSample(Material blockMaterial, double targetYield) {
 		CropYieldType yieldType = crops.get(blockMaterial);
 		
 		// don't fail if that crop type is not supported
 		if (yieldType == null)
 			return false;
 		
 		// don't fail of the vanilla yield is smaller than the target yield
 		if (yieldType.averageVanillaHarvestYield < targetYield)
 			return false;
 		
 		double failChance = 1.0 - (targetYield / yieldType.averageVanillaHarvestYield);	
 		
 		return Math.random() < failChance;
 	}
 	
 	public static List<ItemStack> sampleBonusYield(Material blockMaterial, double targetYield) {
 		CropYieldType yieldType = crops.get(blockMaterial);
 		
 		// no bonus yield if that crop type is not supported
 		if (yieldType == null)
 			return Collections.emptyList();
 		
 		// no bonus yield if the vanilla yeild is larger than the target yield
 		if (yieldType.averageVanillaHarvestYield > targetYield)
 			return Collections.emptyList();
 		
 		double netYield = targetYield - yieldType.averageVanillaHarvestYield;
 		List<ItemStack> items = new LinkedList<ItemStack>();
 		for (Item item : yieldType.yieldUnit) {
 			int count = randomRound(item.count * netYield);
 			ItemStack itemStack = new ItemStack(item.material, count);
 			itemStack.setData(new MaterialData(item.material, item.data));
 			items.add(itemStack);
 		}
 		return items;
 	}
 	
 	// round the value up or down randomly, the distribution of the results will have an average of "value"
 	private static int randomRound(double value) {
 		int floor = (int)Math.floor(value);
 		if (Math.random() < (value - floor))
 			floor += 1;
 		return floor;
 	}
 }
