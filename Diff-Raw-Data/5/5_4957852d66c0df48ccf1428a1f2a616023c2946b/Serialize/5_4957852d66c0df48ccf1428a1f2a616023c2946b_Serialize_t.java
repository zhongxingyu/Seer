 /**
  *  Name:    Serialize.java
  *  Created: 18:10:55 - 18 jun 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  *  Utility to serialize and deserialize misc stuff.
  *  Uses a lot of regular expressions.
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class Serialize {
 	
 	/**
 	 * Serialize an entire inventory into a single string.
 	 * This saves all position data (uses the position in the array).
 	 * Use stringToInventory() to get the items back.
 	 * 
 	 * @param contents - ItemStack[] containing the inventory.
 	 * @return Serialized inventory.
 	 */
 	public static String inventoryToString(ItemStack[] contents) {
 		StringBuilder string = new StringBuilder();
 		
 		for(int i = 0 ; i < contents.length ; i++) {
 			ItemStack stack = contents[i];
 			if(stack == null) continue;
			string.append(i).append(';').append(itemstackToString(stack)).append('_');
 		}
 		
 		return string.toString();
 	}
 	
 	/**
 	 * Deserializes the string from inventoryToString() and returns a map with
 	 * the stacks position as key and the stack as value.
 	 * 
 	 * @param serial - Serial to deserialize.
 	 * @return Map with position as key and stack as value. Null if deserialization failed.
 	 */
 	public static Map<Integer, ItemStack> stringToInventory(String serial) {
 		HashMap<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
 		
 		SerializePatterns p = SerializePatterns.INV_POSITION;
 		
		for(String s : serial.split("_")) {
 			
 			Matcher m = p.pattern.matcher(s);
 			
 			if(m.find()) {
 				int pos = 0;
 				
 				try { 
 					pos = Integer.parseInt(m.group(p.groupID)); 
 				}
 				catch(NumberFormatException e) { 
 					return null;
 				}
 				
 				String stack = s.split(";")[1];
 				map.put(pos, Serialize.stringToItemstack(stack));
 			}
 		}
 		
 		return map;
 	}
 	
 	/**
 	 * <pre>Serialize a single ItemStack into a string.
 	 * 
 	 * Character cheat sheet:
 	 * {@literal @} - Start
 	 * # - Stop
 	 * 
 	 * -- Sections
 	 * a - Amount
 	 * t - Type (id)
 	 * D - Data
 	 * d - Durability
 	 * e - Enchantments
 	 * m - Metadata
 	 *   mn - Displayname
 	 *   ml - Lore
 	 * 
 	 * -- Separators
 	 * ! - Section separator
 	 * + - Enchantments separator
 	 * & - Metadata separator</pre>
 	 * 
 	 * @param stack - The stack to be serialized.
 	 * @return Serialized string.
 	 */
 	public static String itemstackToString(ItemStack stack) {
 		StringBuilder string = new StringBuilder();
 		string.append('@'); // Start string character
 		
 		// Amount
 		string.append("a:").append(stack.getAmount()).append('!');
 		
 		// Material id
 		string.append("t:").append(stack.getData().getItemTypeId()).append('!');
 		
 		// Material data
 		string.append("D:").append(stack.getData().getData()).append('!');
 		
 		// Durability
 		string.append("d:").append(stack.getDurability()).append('!');
 		
 		// Enchantments
 		string.append("e:");
 		for(Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
 			string.append(entry.getKey().getId()).append('-').append(entry.getValue()).append('+');
 		}
 		string.append('!');
 		
 		// Meta
 		string.append("m:");
 		if(stack.hasItemMeta()) {
 			ItemMeta meta = stack.getItemMeta();
 			
 			// Displayname
 			if(meta.hasDisplayName()) {
 				string.append("mn:").append(stringToASCIIValues(meta.getDisplayName())).append('&');
 			}
 			
 			// Lore
 			if(meta.hasLore()) {
 				string.append("ml:");
 				for(String l : meta.getLore()) {
 					string.append(stringToASCIIValues(l)).append('/');
 				}
 				string.append('&');
 			}
 		}
 		string.append('!');
 		
 		string.append('#'); // End string character
 		return string.toString();
 	}
 	
 	/**
 	 * Deserializes the string given from the itemstackToString() method.
 	 * 
 	 * @param serial - Serial to deserialize.
 	 * @return Deserialized ItemStack. Null if deserialization didn't work.
 	 */
 	public static ItemStack stringToItemstack(String serial) {
 		if(!serial.startsWith("@") || !serial.endsWith("#")) return null;
 		
 		int amount, id; short durability; byte data;
 		
 		try {
 			amount     = Integer.parseInt(locate(SerializePatterns.STACK_AMOUNT,     serial).get(0));
 			id         = Integer.parseInt(locate(SerializePatterns.STACK_TYPE,       serial).get(0));
 			data       = Byte.parseByte  (locate(SerializePatterns.STACK_DATA,       serial).get(0));
 			durability = Short.parseShort(locate(SerializePatterns.STACK_DURABILITY, serial).get(0));
 		}
 		catch(NumberFormatException e) {
 			return null;
 		}
 		
 		ItemStack stack = new ItemStack(Material.getMaterial(id), amount);
 		stack.setDurability(durability);
 		stack.getData().setData(data);
 		
 		ArrayList<String> enchantments = locate(SerializePatterns.STACK_ENCHANTMENTS, serial);
 		if(enchantments.size() != 0) {
 			
 			Pattern ench = Pattern.compile("(\\d{1,2})-(\\d{1,2})");
 			
 			for(String s : enchantments) {
 				int ench_id  = Integer.parseInt(locate(ench, 1, s).get(0));
 				int ench_lvl = Integer.parseInt(locate(ench, 2, s).get(0));
 				
 				stack.addEnchantment(Enchantment.getById(ench_id), ench_lvl);
 			}
 		}
 		
 		ItemMeta meta = stack.getItemMeta();
 		
 		ArrayList<String> displayname = locate(SerializePatterns.STACK_META_DISPLAYNAME, serial);
 		if(displayname.size() == 1) {
 			meta.setDisplayName(asciiToString(displayname.get(0)));
 		}
 		
 		ArrayList<String> ascii_lore = locate(SerializePatterns.STACK_META_LORE, serial);
 		if(ascii_lore.size() > 0) {
 			ArrayList<String> lore = new ArrayList<String>();
 			for(String s : ascii_lore) {
 				lore.add(asciiToString(s));
 			}
 			meta.setLore(lore);
 		}
 		
 		stack.setItemMeta(meta);
 		
 		return stack;
 	}
 	
 	/** 
 	 * Converts a string of regular characters into a string of their ASCII-values.
 	 * 
 	 * @param string - String to convert.
 	 * @return String with the ASCII-values.
 	 */
 	public static String stringToASCIIValues(String string) {
 		StringBuilder values = new StringBuilder().append('[');
 		
 		for(int i = 0 ; i < string.length() ; i++) {
 			values.append((int)string.charAt(i)).append(',');
 		}
 		
 		return values.append(']').toString();
 	}
 	
 	/**
 	 * Convert the ASCII-string from the stringToASCIIValues() method into a normal readable string.
 	 * 
 	 * @param ascii - ASCII-string to convert.
 	 * @return A normal readable string.
 	 */
 	public static String asciiToString(String ascii) {
 		StringBuilder string = new StringBuilder();
 		
 		ArrayList<String> list = locate(SerializePatterns.ASCII, ascii);
 		
 		for(String s : list) {
 			string.append((char)Integer.parseInt(s));
 		}
 		
 		return string.toString();
 	}
 	
 	/**
 	 * Locates and returns the values in the search string using the given regex-pattern.
 	 * 
 	 * @param regex - Expression to use when searching.
 	 * @param search - The string you want to search through.
 	 * @return Returns ArrayList containing found strings.
 	 */
 	public static ArrayList<String> locate(SerializePatterns pattern, String search) {
 		return locate(pattern.pattern, pattern.groupID, search);
 	}
 	
 	
 	private static ArrayList<String> locate(Pattern pattern, int group, String search) {
 		ArrayList<String> values = new ArrayList<String>();
 		
 		Matcher matcher = pattern.matcher(search);
 		
 		while(matcher.find()) {
 			values.add(matcher.group(group));
 		}
 		
 		return values;
 	}
 }
