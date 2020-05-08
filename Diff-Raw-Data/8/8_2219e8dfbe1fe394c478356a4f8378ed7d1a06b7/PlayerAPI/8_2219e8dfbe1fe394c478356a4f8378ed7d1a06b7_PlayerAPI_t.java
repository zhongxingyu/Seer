 package com.censoredsoftware.Demigods.API;
 
 import java.util.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayerInventory;
 
 public class PlayerAPI
 {
 	/**
 	 * Returns the current character of the <code>player</code>.
 	 * 
 	 * @param player the player to check.
 	 * @return Demigod
 	 */
 	public static PlayerCharacter getCurrentChar(OfflinePlayer player)
 	{
 		try
 		{
 			return TrackedPlayer.getMeta(player).getCurrent();
 		}
 		catch(Exception e)
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the current alliance for <code>player</code>.
 	 * 
 	 * @param player the player to check.
 	 * @return String
 	 */
 	public static String getCurrentAlliance(OfflinePlayer player)
 	{
 		PlayerCharacter character = getCurrentChar(player);
 		if(character == null || !character.isImmortal()) return "Mortal";
 		return character.getAlliance();
 	}
 
 	/**
 	 * Returns true if <code>player1</code> is allied with <code>player2</code> based
 	 * on their current alliances.
 	 * 
 	 * @param player1 the first player to check.
 	 * @param player2 the second player to check.
 	 * @return boolean
 	 */
 	public static boolean areAllied(Player player1, Player player2)
 	{
 		String player1Alliance = getCurrentAlliance(player1);
 		String player2Alliance = getCurrentAlliance(player2);
 
 		return player1Alliance.equalsIgnoreCase(player2Alliance);
 	}
 
 	/**
 	 * Returns a List of all of <code>player</code>'s characters.
 	 * 
 	 * @param player the player to check.
 	 * @return List the list of all character IDs.
 	 */
 	public static List<PlayerCharacter> getChars(OfflinePlayer player)
 	{
 		List<PlayerCharacter> characters = new ArrayList<PlayerCharacter>();
 		for(PlayerCharacter character : CharacterAPI.getAllChars())
 		{
 			if(character.getOwner() == player) characters.add(character);
 		}
 		return characters;
 	}
 
 	/**
 	 * Changes the <code>offlinePlayer</code>'s current character to <code>charID</code>.
 	 * 
 	 * @param offlinePlayer the player whose character to change.
 	 * @return boolean based on if the change was successful or not.
 	 */
 	public static boolean changeCurrentChar(OfflinePlayer offlinePlayer, PlayerCharacter character) // TODO Most of this should be in a listener, not here in the API.
 	{
 		// Define variables
 		Player player = offlinePlayer.getPlayer();
 
 		if(!getChars(offlinePlayer).contains(character))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that.");
 			return false;
 		}
 
 		// Disable prayer just to be safe
 		togglePraying(player, false);
 
 		// Update the current character (if it exists)
 		PlayerCharacter currentChar = getCurrentChar(player);
 		if(currentChar != null)
 		{
 			// Save info
 			currentChar.setHealth(player.getHealth());
 			currentChar.setHunger(player.getFoodLevel());
 			currentChar.setLevel(player.getLevel());
 			currentChar.setExperience(player.getExp());
 			currentChar.setLocation(player.getLocation());
 			currentChar.saveInventory();
 
 			// Set them to inactive
 			currentChar.setActive(false);
 		}
 
 		// Everything is good, let's switch
 		TrackedPlayer.getMeta(player).setCurrent(character);
 		character.setActive(true);
 
 		// If it's their first character save their inventory
 		if(getChars(player).size() <= 1) character.saveInventory();
 
 		// Update inventory
 		player.getInventory().clear();
 		player.getInventory().setBoots(new ItemStack(Material.AIR));
 		player.getInventory().setChestplate(new ItemStack(Material.AIR));
 		player.getInventory().setLeggings(new ItemStack(Material.AIR));
 		player.getInventory().setBoots(new ItemStack(Material.AIR));
 
 		if(character.getInventory() != null)
 		{
 			TrackedPlayerInventory charInv = character.getInventory();
 			charInv.setToPlayer(player);
 		}
 
 		// Update health and experience
 		player.setHealth(character.getHealth());
 		player.setFoodLevel(character.getHunger());
 		player.setExp(character.getExperience());
 
 		try
 		{
 			// Teleport them
 			player.teleport(character.getLocation().toLocation());
 		}
 		catch(Exception e)
 		{
 			Demigods.message.severe("Something went wrong when trying to teleport to a character location..."); // TODO Better error message.
 		}
 
 		// Enable movement and chat to be safe
 		togglePlayerChat(player, true);
 		togglePlayerMovement(player, true);
 
 		return true;
 	}
 
 	/**
 	 * Returns true if the <code>player</code> is currently immortal.
 	 * 
 	 * @param player the player to check.
 	 * @return boolean
 	 */
 	public static boolean isImmortal(OfflinePlayer player)
 	{
 		PlayerCharacter character = getCurrentChar(player);
 		return character != null && character.isImmortal();
 	}
 
 	/**
 	 * Returns true if <code>player</code> has a character with the id <code>charID</code>.
 	 * 
 	 * @param player the player to check.
 	 * @param charID the charID to check with.
 	 * @return boolean
 	 */
 	public static boolean hasCharID(OfflinePlayer player, int charID)
 	{
 		return getChars(player) != null && getChars(player).contains(charID);
 	}
 
 	/**
 	 * Returns true if <code>player</code> has a character with the name <code>charName</code>.
 	 * 
 	 * @param player the player to check.
 	 * @param charName the charName to check with.
 	 * @return boolean
 	 */
 	public static boolean hasCharName(OfflinePlayer player, String charName)
 	{
 		List<PlayerCharacter> characters = getChars(player);
 
 		for(PlayerCharacter character : characters)
 		{
 			if(character == null) continue;
 			if(character.getName().equalsIgnoreCase(charName)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the <code>player</code> is currently praying.
 	 * 
 	 * @param player the player to check.
 	 * @return boolean
 	 */
 	public static boolean isPraying(OfflinePlayer player)
 	{
		try
		{
			return Boolean.parseBoolean(DemigodsData.getValueTemp(player.getName(), "temp_praying").toString());
		}
		catch(Exception ignored)
		{}
		return false;
 	}
 
 	/**
 	 * Returns the number of total kills for <code>player</code>.
 	 * 
 	 * @param player the player to check.
 	 * @return int
 	 */
 	public static int getKills(OfflinePlayer player)
 	{
 		return -1; // TODO DemigodsData.playerData.getDataInt(player, "player_kills");
 	}
 
 	/**
 	 * Sets the amount of kills for <code>player</code> to <code>amount</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 * @param amount the amount of kills to set to.
 	 */
 	public static void setKills(OfflinePlayer player, int amount)
 	{
 		// TODO DemigodsData.playerData.saveData(player, "player_kills", amount);
 	}
 
 	/**
 	 * Adds 1 kill to <code>player</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 */
 	public static void addKill(OfflinePlayer player)
 	{
 		// TODO DemigodsData.playerData.saveData(player, "player_kills", getKills(player) + 1);
 	}
 
 	/**
 	 * Returns the number of deaths for <code>player</code>.
 	 * 
 	 * @param player the player to check.
 	 * @return int
 	 */
 	public static int getDeaths(OfflinePlayer player)
 	{
 		return -1; // TODO DemigodsData.playerData.getDataInt(player, "player_deaths");
 	}
 
 	/**
 	 * Sets the number of deaths for <code>player</code> to <code>amount</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 * @param amount the amount of deaths to set.
 	 */
 	public static void setDeaths(OfflinePlayer player, int amount)
 	{
 		// TODO DemigodsData.playerData.saveData(player, "player_deaths", amount);
 	}
 
 	/**
 	 * Adds a death to <code>player</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 */
 	public static void addDeath(OfflinePlayer player)
 	{
 		// TODO DemigodsData.playerData.saveData(player, "player_deaths", getDeaths(player) + 1);
 	}
 
 	/**
 	 * Returns an ArrayList of all online admins.
 	 * 
 	 * @return ArrayList
 	 */
 	public static ArrayList<Player> getOnlineAdmins() // TODO Does this belong here?
 	{
 		ArrayList<Player> toReturn = new ArrayList<Player>();
 		for(Player player : Bukkit.getOnlinePlayers())
 		{
 			if(Demigods.permission.hasPermissionOrOP(player, "demigods.admin")) toReturn.add(player);
 		}
 		return toReturn;
 	}
 
 	/**
 	 * Returns an ArrayList of all online players.
 	 * 
 	 * @return ArrayList
 	 */
 	public static Set<Player> getOnlinePlayers() // TODO Is this even needed?
 	{
 		Set<Player> toReturn = new HashSet<Player>();
 		Collections.addAll(toReturn, Bukkit.getOnlinePlayers());
 		return toReturn;
 	}
 
 	/**
 	 * Returns an ArrayList of all offline players.
 	 * 
 	 * @return ArrayList
 	 */
 	public static Set<OfflinePlayer> getOfflinePlayers() // TODO Is this even needed?
 	{
 		Set<OfflinePlayer> toReturn = getAllPlayers();
 		for(Player player : Bukkit.getOnlinePlayers())
 		{
 			toReturn.remove(player);
 		}
 		return toReturn;
 	}
 
 	/**
 	 * Returns an ArrayList of all players, offline and online.
 	 * 
 	 * @return ArrayList
 	 */
 	public static Set<OfflinePlayer> getAllPlayers()
 	{
 		Set<OfflinePlayer> toReturn = new HashSet<OfflinePlayer>();
 		for(TrackedPlayer player : TrackedPlayer.loadAll())
 		{
 			toReturn.add(player.getPlayer());
 		}
 		return toReturn;
 	}
 
 	/**
 	 * Changes prayer status for <code>player</code> to <code>option</code>.
 	 * 
 	 * @param player the player the manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePraying(OfflinePlayer player, boolean option)
 	{
 		if(option)
 		{
 			togglePlayerChat(player, false);
 			togglePlayerMovement(player, false);
 			DemigodsData.setTemp(player.getName(), "temp_praying", option);
 		}
 		else
 		{
 			togglePlayerChat(player, true);
 			togglePlayerMovement(player, true);
 			DemigodsData.removeTemp(player.getName(), "temp_praying");
 		}
 	}
 
 	/**
 	 * Enables or disables player movement for <code>player</code> based on <code>option</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePlayerMovement(OfflinePlayer player, boolean option)
 	{
 		if(DemigodsData.hasKeyTemp(player.getName(), "temp_player_hold") && option) DemigodsData.removeTemp(player.getName(), "temp_player_hold");
 		else DemigodsData.setTemp(player.getName(), "temp_player_hold", true);
 	}
 
 	/**
 	 * Enables or disables player chat for <code>player</code> based on <code>option</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePlayerChat(OfflinePlayer player, boolean option)
 	{
 		if(DemigodsData.hasKeyTemp(player.getName(), "temp_no_chat") && option) DemigodsData.removeTemp(player.getName(), "temp_no_chat");
 		else DemigodsData.setTemp(player.getName(), "temp_no_chat", true);
 	}
 }
