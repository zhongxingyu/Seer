 package com.turt2live.antishare;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Attachable;
 
 /**
  * Utilities
  * 
  * @author turt2live
  */
 public class ASUtils {
 
 	/**
 	 * Adds color to a message
 	 * 
 	 * @param message the message
 	 * @return the colored message
 	 */
 	public static String addColor(String message){
 		return ChatColor.translateAlternateColorCodes('&', message);
 	}
 
 	/**
 	 * Sends a message to a player.<br>
 	 * This will prefix "[AntiShare]" to the message and not send if the message is simply "no message".
 	 * 
 	 * @param target the player to send to
 	 * @param message the message to send
 	 * @param useSimpleNotice set to true if this method should use SimpleNotice if available
 	 */
 	public static void sendToPlayer(CommandSender target, String message, boolean useSimpleNotice){
 		if(!message.equalsIgnoreCase("nomsg")
 				&& !message.equalsIgnoreCase("no message")
 				&& !message.equalsIgnoreCase("none")
 				&& !message.equalsIgnoreCase("noshow")
 				&& !message.equalsIgnoreCase("no show")){
 			message = addColor(message);
 			if(!ChatColor.stripColor(message).startsWith("[AntiShare]")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.WHITE + message;
 			}
 			/* SimpleNotice support provided by feildmaster.
 			 * Support adapted by krinsdeath and further
 			 * modified by turt2live for AntiShare.
 			 */
 			if(target instanceof Player){
				if(((Player) target).getListeningPluginChannels().contains("SimpleNotice")
						&& useSimpleNotice
						&& AntiShare.getInstance().isSimpleNoticeEnabled(target.getName())){
 					((Player) target).sendPluginMessage(AntiShare.getInstance(), "SimpleNotice", message.getBytes(java.nio.charset.Charset.forName("UTF-8")));
 				}else{
 					target.sendMessage(message);
 				}
 			}else{
 				target.sendMessage(message);
 			}
 		}
 	}
 
 	/**
 	 * Gets a boolean from a String
 	 * 
 	 * @param value the String
 	 * @return the boolean (or null if not found)
 	 */
 	public static Boolean getBoolean(String value){
 		if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("on")
 				|| value.equalsIgnoreCase("active") || value.equalsIgnoreCase("1")){
 			return true;
 		}else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("off")
 				|| value.equalsIgnoreCase("inactive") || value.equalsIgnoreCase("0")){
 			return false;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a GameMode from a String
 	 * 
 	 * @param value the string
 	 * @return the GameMode (or null if not found)
 	 */
 	public static GameMode getGameMode(String value){
 		if(value.equalsIgnoreCase("creative") || value.equalsIgnoreCase("c") || value.equalsIgnoreCase("1")){
 			return GameMode.CREATIVE;
 		}else if(value.equalsIgnoreCase("survival") || value.equalsIgnoreCase("s") || value.equalsIgnoreCase("0")){
 			return GameMode.SURVIVAL;
 			// TODO: 1.3
 			//		}else if(value.equalsIgnoreCase("adventure") || value.equalsIgnoreCase("a") || value.equalsIgnoreCase("2")){
 			//			return GameMode.ADVENTURE;
 		}
 		return null;
 	}
 
 	/**
 	 * Determines if a Material is interactable (to AntiShare's standards)
 	 * 
 	 * @param material the material
 	 * @return true if interactable
 	 */
 	public static boolean isInteractable(Material material){
 		switch (material){
 		case DISPENSER:
 		case NOTE_BLOCK:
 		case BED_BLOCK:
 		case CHEST:
 		case WORKBENCH:
 		case FURNACE:
 		case BURNING_FURNACE:
 		case WOODEN_DOOR:
 		case LEVER:
 		case STONE_PLATE:
 		case IRON_DOOR_BLOCK:
 		case WOOD_PLATE:
 		case STONE_BUTTON:
 		case JUKEBOX:
 		case LOCKED_CHEST:
 		case TRAP_DOOR:
 		case MONSTER_EGGS:
 		case FENCE_GATE:
 		case ENCHANTMENT_TABLE:
 		case BREWING_STAND:
 			/* TODO: 1.3
 			 * Add interactable blocks
 			 */
 		case CAULDRON:
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Determines if a block would be dropped if an attached block were to break.<br>
 	 * This also checks if the block is attached to a source.
 	 * 
 	 * @param block the block (attached to the breaking block)
 	 * @param source the block that the checked block may be attached to (null for no source)
 	 * @return true if the block would fall
 	 */
 	public static boolean isDroppedOnBreak(Block block, Block source){
 		boolean attached = false;
 		if(block.getState().getData() instanceof Attachable && !block.getType().equals(Material.PISTON_EXTENSION)){
 			if(source != null){
 				Attachable att = (Attachable) block.getState().getData();
 				// We need to use location because Java is mean like that >.<
 				Location l1 = source.getLocation();
 				Location l2 = block.getRelative(att.getAttachedFace()).getLocation();
 				attached = l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
 			}else{
 				attached = true;
 			}
 		}
 		return attached;
 	}
 
 	/**
 	 * Capitalizes item names. Eg: EXP_BOTTLE -> Exp Bottle
 	 * 
 	 * @param string the string
 	 * @return the string, capitalized correctly
 	 */
 	public static String capitalize(String string){
 		String parts[] = string.toLowerCase().replaceAll(" ", "_").split("_");
 		StringBuilder returnString = new StringBuilder();
 		for(String part : parts){
 			// No need for part.substring(1).toLowerCase(), the split handles this
 			returnString.append(part.substring(0, 1).toUpperCase() + part.substring(1) + " ");
 		}
 		return returnString.toString().trim();
 	}
 
 	/**
 	 * Converts an item stack to a string<br>
 	 * This returns the format 'id:data', data will be zero if no
 	 * data is found, or the data is actually zero. You can set 'zero'
 	 * in the parameters to false to just get the item ID. If 'zero' is
 	 * true and there is data, the correct format (id:data) will be returned.
 	 * 
 	 * @param item the item
 	 * @param zero true to add zero
 	 * @return the item as a string
 	 */
 	public static String stackToString(ItemStack item, boolean zero){
 		if(item == null){
 			return null;
 		}
 		String typeId = "";
 		String data = "";
 		typeId = Integer.toString(item.getTypeId());
 		if(item.getType().getMaxDurability() > 0){
 			data = "0";
 		}else if(item.getDurability() > 0){
 			data = Short.toString(item.getDurability());
 		}else if(item.getData().getData() > 0){
 			data = Byte.toString(item.getData().getData());
 		}else{
 			data = "0";
 		}
 		return typeId + (data.equals("0") && zero ? "" : ":" + data);
 	}
 
 	/**
 	 * Converts a block to a string<br>
 	 * This returns the format 'id:data', data will be zero if no
 	 * data is found, or the data is actually zero. You can set 'zero'
 	 * in the parameters to false to just get the block ID. If 'zero' is
 	 * true and there is data, the correct format (id:data) will be returned.
 	 * 
 	 * @param block the block
 	 * @param zero true to add zero
 	 * @return the block as a string
 	 */
 	public static String blockToString(Block block, boolean zero){
 		if(block == null){
 			return null;
 		}
 		String typeId = "";
 		String data = "";
 		typeId = Integer.toString(block.getTypeId());
 		if(block.getType().getMaxDurability() > 0){
 			data = "0";
 		}else if(block.getData() > 0){
 			data = Byte.toString(block.getData());
 		}else{
 			data = "0";
 		}
 		return typeId + (data.equals("0") && zero ? "" : ":" + data);
 	}
 
 	/**
 	 * Converts a material to a string<br>
 	 * This returns the format 'id:data', data will be zero if no
 	 * data is found, or the data is actually zero. You can set 'zero'
 	 * in the parameters to false to just get the material ID. If 'zero' is
 	 * true and there is data, the correct format (id:data) will be returned.<br>
 	 * <b>Worth Noting:</b> this (if zero is false) will return a :* id, such as
 	 * 1:* if you pass it Material.STONE.
 	 * 
 	 * @param material the material
 	 * @param zero true to add zero
 	 * @return the material as a string
 	 */
 	public static String materialToString(Material material, boolean zero){
 		StringBuilder ret = new StringBuilder();
 		ret.append(material.getId());
 		if(!zero){
 			ret.append(":");
 			ret.append("*");
 		}
 		return ret.toString();
 	}
 
 	/**
 	 * Converts words to ID. Eg: "light blue wool" -> "wool:3"
 	 * 
 	 * @param input the raw input
 	 * @return the wool ID (with data value) or null if not wool
 	 */
 	public static String getWool(String input){
 		if(!input.toLowerCase().contains("wool")){
 			return null;
 		}
 
 		String color = input.replace("wool", "").trim().toLowerCase();
 		color = color.replaceAll(" ", "_");
 		color = color.replace("orange", "1");
 		color = color.replace("white", "0");
 		color = color.replace("magenta", "2");
 		color = color.replace("light_blue", "3");
 		color = color.replace("yellow", "4");
 		color = color.replace("lime", "5");
 		color = color.replace("pink", "6");
 		color = color.replace("gray", "7");
 		color = color.replace("light_gray", "8");
 		color = color.replace("cyan", "9");
 		color = color.replace("purple", "10");
 		color = color.replace("blue", "11");
 		color = color.replace("brown", "12");
 		color = color.replace("green", "13");
 		color = color.replace("red", "14");
 		color = color.replace("black", "15");
 
 		return Material.WOOL.getId() + ":" + color;
 	}
 
 	/**
 	 * Gets the current timestamp of the system
 	 * 
 	 * @return the timestamp
 	 */
 	public static String timestamp(){
 		DateFormat dateFormat = new SimpleDateFormat("d-M-y-HH-mm-ss-SS");
 		Date date = new Date();
 		String timestamp = dateFormat.format(date);
 		return timestamp;
 	}
 }
