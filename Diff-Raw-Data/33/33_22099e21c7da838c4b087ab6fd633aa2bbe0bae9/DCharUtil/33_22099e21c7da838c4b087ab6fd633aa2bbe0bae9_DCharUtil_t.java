 package com.legit2.Demigods.Utilities;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import com.google.common.base.Joiner;
 import com.legit2.Demigods.DConfig;
 import com.legit2.Demigods.DDatabase;
 
 public class DCharUtil
 {
 	/*
 	 *  createChar() : Creates the character according to the values passed in. Returns true on success,
 	 *  false on fail.
 	 */
 	public static boolean createChar(Player player, String charName, String charDeity)
 	{
 		if(!DDataUtil.hasChar(player, charName))
 		{
 			// Define variables
 			int charID = DObjUtil.generateInt(5);
 			String charAlliance = DDeityUtil.getDeityAlliance(charDeity);
 			int charHP = 20;
 			int charExp = 25;
 			double charX = player.getLocation().getX();
 			double charY = player.getLocation().getY();
 			double charZ = player.getLocation().getZ();
 			String charW = player.getLocation().getWorld().getName();
 			int charFavor = 500;
 			int charDevotion = 500;
 			int charAscensions = 1;
 			
 			DDataUtil.addChar(player, charID);
 			DDataUtil.removePlayerData(player, "current_char");
 			DDataUtil.savePlayerData(player, "current_char", charID);
 			DDataUtil.saveCharData(player, charID, "char_name", charName);
 			DDataUtil.saveCharData(player, charID, "char_alliance", charAlliance);
 			DDataUtil.saveCharData(player, charID, "char_deity", charDeity);
 			DDataUtil.saveCharData(player, charID, "char_immortal", true);
 			DDataUtil.saveCharData(player, charID, "char_hp", charHP);
 			DDataUtil.saveCharData(player, charID, "char_exp", charExp);
 			DDataUtil.saveCharData(player, charID, "char_lastX", charX);
 			DDataUtil.saveCharData(player, charID, "char_lastY", charY);
 			DDataUtil.saveCharData(player, charID, "char_lastZ", charZ);
 			DDataUtil.saveCharData(player, charID, "char_lastW", charW);
 			DDataUtil.saveCharData(player, charID, "char_favor", charFavor);
 			DDataUtil.saveCharData(player, charID, "char_devotion", charDevotion);
 			DDataUtil.saveCharData(player, charID, "char_ascensions", charAscensions);
 			
 			// Add character to player's character list
 			String chars;
 			if(DPlayerUtil.getChars(player) != null)
 			{
 				ArrayList<String> charsTemp = DPlayerUtil.getChars(player);
 				charsTemp.add("" + charID);
 				chars = Joiner.on(",").join(charsTemp);
 			}
 			else chars = "" + charID;
 			
 			DDataUtil.savePlayerData(player, "player_characters", chars);
 
 			try
 			{
 				DDatabase.addCharToDB(player, charID);
 				DDatabase.savePlayerData(player);
 			}
 			catch(SQLException e)
 			{
 				// Error with saving character... send them an error code				
 				player.sendMessage(ChatColor.RED + "There was a problem with saving your character.");
 				player.sendMessage(ChatColor.RED + "Please give this error code to an administrator: " + ChatColor.RESET + ChatColor.ITALIC + ChatColor.RED + "2002");
 			}
 			
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 *  removeChar() : Removes the character with (int)id. Returns true on success, false on fail.
 	 */
 	public static boolean removeChar(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.removeChar(player, charID))
 		{
 			DDatabase.savePlayerData(player);
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 *  getCharByID() : Returns the complete character info for the character with (int)id.
 	 */
 	public static HashMap<String, Object> getCharInfo(OfflinePlayer player, int charID)
 	{
 		return DDataUtil.getAllCharData(player, charID);
 	}
 	
 	/*
 	 *  getWhereDeity() : Returns the (int)charID for (Player)player's (String)deity.
 	 */
 	public static int getCharWhereDeity(Player player, String deity)
 	{		
 		for(Entry<Integer, HashMap<String, Object>> character : DDataUtil.getAllPlayerChars(player).entrySet())
 		{
 			int charID = character.getKey();
 			HashMap<String, Object> charData = character.getValue();
 			
 			for(Entry<String, Object> charDataEntry : charData.entrySet())
 			{
 				if(charDataEntry.getKey().equalsIgnoreCase("deity") && ((String) charDataEntry.getValue()).equalsIgnoreCase(deity)) return charID;
 				else return -1;
 			}
 		}
 		return -1;
 	}
 
 	/*
 	 *  hasDeity() : Returns boolean for if the character has the deity.
 	 */
 	public static boolean hasDeity(Player player, String deity)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(getDeity(player, charID).equalsIgnoreCase(deity)) return true;
 		else return false;
 	}	
 	
 	/*
 	 *  getPlayer() : Returns the (int)playerID of the player owning (int)charID.
 	 */
 	public static int getPlayer(int charID)
 	{
 		return -1;
 	}
 	
 	/*
 	 *  getCharID() : Returns the (int)charID for the character with the name (String)charName.
 	 */
 	public static int getCharID(OfflinePlayer player, String charName)
 	{
 		HashMap<Integer, HashMap<String, Object>> playerCharData = DDataUtil.getAllPlayerChars(player);
 		for(Entry<Integer, HashMap<String, Object>> playerChar : playerCharData.entrySet())
 		{
 			// Define character-specific variables
 			int charID = playerChar.getKey();
 			
 			if(((String) playerCharData.get(charID).get("char_name")).equalsIgnoreCase(charName)) return charID;
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getName() : Returns the (String)charName of the character with (int)charID.
 	 */
 	public static String getName(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.charExistsByID(player, charID)) return (String) DDataUtil.getCharData(player, charID, "char_name");
 		else return null;
 	}
 	
 	/*
 	 *  getDeity() : Returns the (String)deity for (int)charID.
 	 */
 	public static String getDeity(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_deity")) return (String) DDataUtil.getCharData(player, charID, "char_deity");
 		else return null;
 	}
 
 	/*
 	 *  getAlliance() : Returns the (String)alliance for (int)charID.
 	 */
 	public static String getAlliance(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_alliance")) return (String) DDataUtil.getCharData(player, charID, "char_alliance");
 		else return null;
 	}
 	
 	/*
 	 *  getImmortal() : Returns the (Boolean)immortal for (int)charID.
 	 */
 	public static boolean getImmortal(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_immortal")) return DObjUtil.toBoolean(DDataUtil.getCharData(player, charID, "char_immortal"));
 		else return false;
 	}
 	
 	/*
 	 *  getFavor() : Returns the (int)favor for (int)charID.
 	 */
 	public static int getFavor(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_favor")) return DObjUtil.toInteger(DDataUtil.getCharData(player, charID, "char_favor"));
 		else return -1;
 	}
 	
 	/*
 	 *  getHP() : Returns the (int)hp for (int)charID.
 	 */
 	public static int getHP(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_hp")) return DObjUtil.toInteger(DDataUtil.getCharData(player, charID, "char_hp"));
 		else return -1;
 	}
 	
 	/*
 	 *  getExp() : Returns the (int)favor for (int)charID.
 	 */
 	public static int getExp(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_exp")) return DObjUtil.toInteger(DDataUtil.getCharData(player, charID, "char_exp"));
 		else return -1;
 	}
 	
 	/*
 	 *  getDevotion() : Returns the (int)favor for (int)charID.
 	 */
 	public static int getDevotion(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_devotion")) return DObjUtil.toInteger(DDataUtil.getCharData(player, charID, "char_devotion"));
 		else return -1;
 	}
 	
 	/*
 	 *  getAscensions() : Returns the (int)favor for (int)charID.
 	 */
 	public static int getAscensions(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.hasCharData(player, charID, "char_ascensions")) return DObjUtil.toInteger(DDataUtil.getCharData(player, charID, "char_ascensions"));
 		else return -1;
 	}
 	
 	/*
 	 *  setFavor() : Sets the (String)username's favor to (int)amount.
 	 */
 	public static void setFavor(Player player, int amount)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		DDataUtil.saveCharData(player, charID, "favor", amount);
 	}
 	
 	/*
 	 *  subtractFavor() : Subtracts (int)amount from the (String)username's favor.
 	 */
 	public static void subtractFavor(Player player, int charID, int amount)
 	{
 		setFavor(player, getFavor(player, charID) - amount);
 	}
 	
 	/*
 	 *  giveFavor() : Gives (int)amount favor to (int)charID.
 	 */
 	public static void giveFavor(Player player, int charID, int amount)
 	{
 		// Define variables
 		int favor;
 
 		// Perform favor cap check
 		if((getFavor(player, charID) + amount) > DConfig.getSettingInt("max_favor"))
 		{
 			favor = DConfig.getSettingInt("max_favor");
 		}
 		else favor = getFavor(player, charID) + amount;
 		
 		setFavor(player, favor);
 	}
 	
 	/*
 	 *  setAscensions() : Sets the (String)username's ascensions to (int)amount.
 	 */
 	public static void setAscensions(Player player, int amount)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		DDataUtil.saveCharData(player, charID, "ascensions", amount);
 	}
 
 	/*
 	 *  subtractAscensions() : Subtracts (int)amount from the (String)username's ascensions.
 	 */
 	public static void subtractAscensions(Player player, int charID, int amount)
 	{
 		if(getAscensions(player, charID) - amount < 0)
 		{
 			setAscensions(player, 0);
 		}
 		else setAscensions(player, getAscensions(player, charID) - amount);
 	}
 	
 	/*
 	 *  giveAscensions() : Gives (int)amount ascensions to (String)username.
 	 */
 	public static void giveAscensions(Player player, int charID, int amount)
 	{
 		DDataUtil.saveCharData(player, charID, "ascensions", getAscensions(player, charID) + amount);
 	}
 
 	/*
 	 *  setDevotion() : Sets the (String)username's devotion to (int)amount for (String)deity.
 	 */
 	public static void setDevotion(Player player, int charID, int amount)
 	{
 		DDataUtil.saveCharData(player, charID, "devotion", amount);
 	}
 
 	/*
 	 *  subtractDevotion() : Subtracts (int)amount from the (String)username's (String)deity devotion.
 	 */
 	public static void subtractDevotion(Player player, int charID, int amount)
 	{
 		setDevotion(player, charID, getDevotion(player, charID) - amount);
 	}
 	
 	/*
 	 *  giveDevotion() : Gives (int)amount devotion to (String)username for (String)deity.
 	 */
 	public static void giveDevotion(Player player, int charID, int amount)
 	{
 		setDevotion(player, charID, getDevotion(player, charID) + amount);
 	}
 	
 	/*
 	 *  setAlliance() : Sets the (String)username's alliance to (String)alliance.
 	 */
 	public static void setAlliance(Player player, int charID, String alliance)
 	{
 		DDataUtil.saveCharData(player, charID, "alliance", alliance);
 	}
 	
 	/*
 	 *  isImmortal() : Gets if the player is immortal or not.
 	 */
 	public static Boolean isImmortal(OfflinePlayer player)
 	{
		if(DPlayerUtil.getCurrentChar(player) == -1) return false;
 		if(DDataUtil.getCharData(player, DPlayerUtil.getCurrentChar(player), "char_immortal") == null) return false;
 		else return DObjUtil.toBoolean(DDataUtil.getCharData(player, DPlayerUtil.getCurrentChar(player), "char_immortal"));
 	}
 	
 	/*
 	 *  setImmortal() : Sets the player's immortal boolean.
 	 */
 	public static void setImmortal(OfflinePlayer player, boolean option)
 	{			
 		DDataUtil.savePlayerData(player, "immortal", option);
 	}
 	
 	/*
 	 *  isEnabledAbility() : Returns a boolean for if (String)ability is enabled for (String)username.
 	 */
 	public static boolean isEnabledAbility(Player player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(DDataUtil.getCharData(player, charID, "boolean_" + ability.toLowerCase()) != null)
 		{
 			return DObjUtil.toBoolean(DDataUtil.getCharData(player, charID, "boolean_" + ability.toLowerCase()));
 		}
 		else return false;
 	}
 	
 	/*
 	 *  enableAbility() : Enables (String)ability for (String)player.
 	 */
 	public static void enableAbility(Player player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(!isEnabledAbility(player, ability))
 		{
 			DDataUtil.saveCharData(player, charID,  "boolean_" + ability.toLowerCase(), true);
 		}
 	}
 	
 	/*
 	 *  disableAbility() : Disables (String)ability for (String)player.
 	 */
 	public static void disableAbility(Player player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(isEnabledAbility(player, ability))
 		{
 			DDataUtil.saveCharData(player, charID,  "boolean_" + ability.toLowerCase(), false);
 		}
 	}
 	
 	/*
 	 *  isCooledDown() : Returns a boolean for is (String)ability is cooled down.
 	 */
 	public static boolean isCooledDown(Player player, String ability, long ability_time, boolean sendMsg)
 	{
 		if(ability_time > System.currentTimeMillis())
 		{
 			if(sendMsg) player.sendMessage(ChatColor.RED + ability + " has not cooled down!");
 			return false;
 		}
 		else return true;
 	}
 	
 	/*
 	 *  getBind() : Returns the bind for (String)username's (String)ability.
 	 */
 	public static Material getBind(OfflinePlayer player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(DDataUtil.getCharData(player, charID, ability + "_bind") != null)
 		{
 			Material material = (Material) DDataUtil.getCharData(player, charID, ability + "_bind");
 			return material;
 		}
 		else return null;
 	}
 	
 	/*
 	 *  getBindings() : Returns all bindings for (Player)player.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Material> getBindings(OfflinePlayer player)
 	{		
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(DPlayerUtil.hasChar(player, charID))
 		{
 			return (ArrayList<Material>) DDataUtil.getCharData(player, charID, "bindings");
 		}
 		else return new ArrayList<Material>();
 	}
 	
 	/*
 	 *  setBound() : Sets (Material)material to be bound for (Player)player.
 	 */
 	public static boolean setBound(OfflinePlayer player, String ability, Material material)
 	{	
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(DDataUtil.getCharData(player, charID, ability + "_bind") == null)
 		{
 			if(((Player) player).getItemInHand().getType() == Material.AIR)
 			{
 				((Player) player).sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
 			}
 			else
 			{
 				if(isBound(player, material))
 				{
 					((Player) player).sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
 					return false;
 				}
 				else if(material == Material.AIR)
 				{
 					((Player) player).sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
 					return false;
 				}
 				else
 				{			
 					if(DDataUtil.hasCharData(player, charID, "bindings"))
 					{
 						ArrayList<Material> bindings = getBindings(player);
 						
 						if(!bindings.contains(material)) bindings.add(material);
 						
 						DDataUtil.saveCharData(player, charID, "bindings", bindings);
 					}
 					else
 					{
 						ArrayList<Material> bindings = new ArrayList<Material>();
 						
 						bindings.add(material);
 						DDataUtil.saveCharData(player, charID, "bindings", bindings);
 					}
 					
 					DDataUtil.saveCharData(player, charID, ability + "_bind", material);
 					((Player) player).sendMessage(ChatColor.YELLOW + ability + " is now bound to: " + material.name().toUpperCase());
 					return true;
 				}
 			}
 		}
 		else
 		{
 			removeBind(player, ability, ((Material) DDataUtil.getCharData(player, charID, ability + "_bind")));
 			((Player) player).sendMessage(ChatColor.YELLOW + ability + "'s bind has been removed.");
 		}
 		return false;
 	}
 	
 	/*
 	 *  isBound() : Checks if (Material)material is bound for (Player)player.
 	 */
 	public static boolean isBound(OfflinePlayer player, Material material)
 	{
 		if(getBindings(player) != null && getBindings(player).contains(material)) return true;
 		else return false;
 	}
 	
 	/*
 	 *  removeBind() : Checks if (Material)material is bound for (Player)player.
 	 */
 	public static boolean removeBind(OfflinePlayer player, String ability, Material material)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		ArrayList<Material> bindings = null;
 
 		if(DDataUtil.hasCharData(player, charID, "bindings"))
 		{
 			bindings = getBindings(player);
 			
 			if(bindings != null && bindings.contains(material)) bindings.remove(material);
 		}
 		
 		DDataUtil.saveCharData(player, charID, "bindings", bindings);
 		DDataUtil.removeCharData(player, charID, ability + "_bind");
 
 		return true;
 	}
 }
