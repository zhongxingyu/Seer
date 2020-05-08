 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.parser.text;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.DyeColor;
 import org.bukkit.GrassSpecies;
 import org.bukkit.Material;
 import org.bukkit.SandstoneType;
 import org.bukkit.TreeSpecies;
 import org.bukkit.material.MonsterEggs;
 import org.bukkit.material.Step;
 
 import com.comphenix.xp.extra.SmoothBrickType;
 import com.comphenix.xp.parser.TextParser;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.Utility;
 
 public class ItemDurabilityParser extends TextParser<Integer> {
 
 	// Quick lookup of items
 	private static Map<Integer, ItemCategory> lookup = new HashMap<Integer, ItemCategory>();
 	
 	// Every item type
 	public enum ItemCategory {
 		TREE_BLOCKS(Material.WOOD.getId(), Material.LEAVES.getId(), WOODEN_STEPS, WOODEN_DOUBLE_STEPS),
 		DYED_BLOCKS(Material.WOOL.getId(), Material.INK_SACK.getId()),
 		GRASS_BLOCKS(Material.GRASS.getId()),
 		STEP_BLOCKS(Material.STEP.getId(), Material.DOUBLE_STEP.getId()),
 		SANDSTONE_BLOCKS(Material.SANDSTONE.getId()),
 		SMOOTH_BRICK_BLOCKS(Material.SMOOTH_BRICK.getId(), Material.SMOOTH_STAIRS.getId()),
 		MONSTER_EGGS_BLOCKS(Material.MONSTER_EGGS.getId()),
 		COAL_ITEMS(Material.COAL.getId());
 		
 		private ItemCategory(Integer... items) {
 			for (Integer item : items)
 				lookup.put(item, this);
 		}
 		
 		public static ItemCategory matchItem(Integer itemID) {
 			return lookup.get(itemID);
 		}
 	}
 	
 	// New blocks
 	private static final int WOODEN_STEPS = 125;
 	private static final int WOODEN_DOUBLE_STEPS = 126;
 	
 	private static final int COAL_NORMAL = 0;
 	private static final int COAL_CHARCOAL = 1;
 
 	private Integer itemID;
 	private boolean usedName;
 	
 	public ItemDurabilityParser() {
 		this.itemID = null;
 	}
 	
 	public ItemDurabilityParser(Integer itemID) {
 		this.itemID = itemID;
 	}
 	
 	public Integer getItemID() {
 		return itemID;
 	}
 	
 	public void setItemID(Integer itemID) {
 		this.itemID = itemID;
 	}
 	
 	public boolean isUsedName() {
 		return usedName;
 	}
 
 	public void setUsedName(boolean usedName) {
 		this.usedName = usedName;
 	}
 
 	@Override
 	public Integer parse(String text) throws ParsingException {
 		
 		if (Utility.isNullOrIgnoreable(text))
 			throw new ParsingException("Text cannot be empty or null.");
 		
 		String filtered = Utility.getEnumName(text);
 		Integer durability = tryParse(text);
 		
 		if (durability == null) {
 			
 			if (isItem(itemID, Material.POTION)) {
 				// Let the caller figure this out. The token are to be processed further,
 				// so we'll keep it in.
				throw new ParsingException("This is a potion.");
 			}
 			
 			// ToDo: Make it possible to use named durabilities without the correct item ID
 			if (itemID == null) {
 				throw ParsingException.fromFormat(
 						"Cannot parse %s - named durabilities only works with known item ids.", text);
 			}
 			
 			ItemCategory category = ItemCategory.matchItem(itemID);
 			
 			// Cannot parse durability
 			if (category == null) {
 				throw ParsingException.fromFormat("Invalid durability value %s.", text);
 			}
 			
 			// Quickly find the correct durability list to use
 			switch (category) {
 			case TREE_BLOCKS:
 				durability = getTreeSpecies(text, filtered);
 				break;
 			case DYED_BLOCKS:
 				// Convert color values
 				durability = getDyeColor(text, filtered);
 				break;
 			case GRASS_BLOCKS:
 				// Grass types
 				durability = getGrassSpecies(text, filtered);
 				break;
 			case STEP_BLOCKS:
 				durability = getStepMaterial(text);
 				break;
 			case SANDSTONE_BLOCKS:
 				durability = getSandstoneType(text, filtered);
 				break;
 			case SMOOTH_BRICK_BLOCKS:
 				durability = getSmoothstoneType(text, filtered);
 				break;
 			case MONSTER_EGGS_BLOCKS:
 				durability = getMonsterEgg(text);
 				break;
 			case COAL_ITEMS:
 				durability = getCoalData(text, filtered);
 				break;
 			}
 
 			// We used a name!
 			setUsedName(true);
 		}
 		
 		return durability;
 	}
 	
 	public static boolean inSameCategory(List<Integer> listItemID) {
 		
 		Integer first = !listItemID.isEmpty() ?  listItemID.get(0) : null;
 		ItemCategory category = ItemCategory.matchItem(first);
 		
 		// Make sure every item is in the same category
 		for (Integer item : listItemID) {
 			if (ItemCategory.matchItem(item) != category)
 				return false;
 		}
 		
 		return true;
 	}
 	
 	private int getCoalData(String text, String filtered) throws ParsingException {
 		
 		// No corresponding constants in Bukkit
 		if (filtered.equals("NORMAL") || filtered.equals("COAL_NORMAL"))
 			return COAL_NORMAL;
 		else if (filtered.equals("CHARCOAL") || filtered.equals("CHAR_COAL"))
 			return COAL_CHARCOAL;
 		else
 			throw ParsingException.fromFormat("Unable to find coal type %s", text);
 	}
 	
 	private int getTreeSpecies(String text, String filtered) throws ParsingException {
 
 		try {
 			// Hard coded names
 			if (filtered.equals("OAK"))
 				return 0;
 			else
 				return (int) TreeSpecies.valueOf(filtered).getData();
 
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find tree species %s.", text);
 		}
 	}
 	
 	private int getStepMaterial(String text) throws ParsingException {
 		
 		Material material = getMaterial(text);
 		Step step = new Step();
 		
 		// Get texture durability value
 		if (step.getTextures().contains(material)) {
 			return (int) new Step(material).getData();	
 		} else {
 			throw ParsingException.fromFormat("Unable to parse texture material %s.", text);
 		}
 	}
 	
 	private int getSandstoneType(String text, String filtered) throws ParsingException {
 		
 		// Extra types
 		if (filtered.equals("NORMAL"))
 			return 0;
 		if (filtered.equals("CHISELED"))
 			return 1;
 
 		// Note: Like many enums, this doesn't correspond to MinecraftWiki
 		try {
 			return (int) SandstoneType.valueOf(filtered).getData();
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find sandstone type %s.", text);
 		}
 	}
 	
 	private int getMonsterEgg(String text) throws ParsingException {
 		
 		Material material = getMaterial(text);
 		MonsterEggs monsterEgg = new MonsterEggs();
 		
 		// Get texture durability value
 		if (monsterEgg.getTextures().contains(material)) {
 			return (int) new MonsterEggs(material).getData();	
 		} else {
 			throw ParsingException.fromFormat("Unable to parse texture material %s", text);
 		}
 	}
 	
 	private int getSmoothstoneType(String text, String filtered) throws ParsingException {
 		
 		// Alternatives
 		if (filtered.equals("CHISELED"))
 			return 3;
 		
 		try {
 			return (int) SmoothBrickType.valueOf(filtered).getData();
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find smoothstone type %s.", text);
 		}
 	}
 	
 	private int getDyeColor(String text, String filtered) throws ParsingException {
 		
 		try {
 			// Look up value directly
 			return (int) DyeColor.valueOf(filtered).getData();
 			
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find dye color %s.", text);
 		}
 	}
 	
 	private int getGrassSpecies(String text, String filtered) throws ParsingException {
 		
 		try {
 			return (int) GrassSpecies.valueOf(filtered).getData();
 			
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to grass species %s.", text);
 		}
 	}
 	
 	private boolean isItem(Integer itemID, Material material) {
 		
 		// Check for equality
 		if (itemID == null)
 			return false;
 		else
 			return itemID == material.getId();
 	}
 	
 	private Material getMaterial(String text) throws ParsingException {
 		
 		try {
 		
 			ItemNameParser parser = new ItemNameParser();
 			Set<Integer> attempt = parser.parse(text);
 			
 			// Get the first Bukkit material
 			for (Integer id : attempt) {
 				Material mat = Material.getMaterial(id);
 				
 				if (mat != null)
 					return mat;
 			}
 			
 			// Or just NULL
 			return null;
 		
 		} catch (ParsingException e) {
 			return null;
 		} catch (IllegalArgumentException e) {
 			return null;
 		}
 	}
 }
