 package com.censoredsoftware.demigods.engine.player;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.core.region.Region;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.conversation.Prayer;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.element.Structure.Structure;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.util.Structures;
 
 @Model
 public class DPlayer
 {
 	@Id
 	private Long id;
 	@Attribute
 	@Indexed
 	private String player;
 	@Attribute
 	@Indexed
 	private Boolean canPvp;
 	@Attribute
 	private long lastLoginTime;
 	@Reference
 	private DCharacter current;
 	@Reference
 	private DCharacter previous;
 
 	void setPlayer(String player)
 	{
 		this.player = player;
 	}
 
 	public void setCanPvp(boolean pvp)
 	{
 		this.canPvp = pvp;
 		Util.save(this);
 	}
 
 	public void updateCanPvp()
 	{
 		if(!getOfflinePlayer().isOnline()) return;
 
 		// Define variables
 		final Player player = getOfflinePlayer().getPlayer();
 		final boolean inNoPvpZone = Structures.isInRadiusWithFlag(player.getLocation(), Structure.Flag.NO_PVP, true);
 
 		if(!canPvp() && !inNoPvpZone)
 		{
 			setCanPvp(true);
 			player.sendMessage(ChatColor.GRAY + Demigods.language.getText(Translation.Text.UNSAFE_FROM_PVP));
 		}
 		else if(!inNoPvpZone)
 		{
 			setCanPvp(true);
 			DataManager.removeTimed(player.getName(), "pvp_cooldown");
 		}
 		else if(canPvp() && !DataManager.hasTimed(player.getName(), "pvp_cooldown"))
 		{
 			int delay = Demigods.config.getSettingInt("zones.pvp_area_delay_time");
 			DataManager.saveTimed(player.getName(), "pvp_cooldown", true, delay);
 
 			Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					if(Structures.isInRadiusWithFlag(player.getLocation(), Structure.Flag.NO_PVP, true))
 					{
 						setCanPvp(false);
 						player.sendMessage(ChatColor.GRAY + Demigods.language.getText(Translation.Text.SAFE_FROM_PVP));
 					}
 				}
 			}, (delay * 20));
 		}
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return Bukkit.getOfflinePlayer(this.player);
 	}
 
 	public void setLastLoginTime(Long time)
 	{
 		this.lastLoginTime = time;
 		Util.save(this);
 	}
 
 	public Long getLastLoginTime()
 	{
 		return this.lastLoginTime;
 	}
 
 	public void switchCharacter(DCharacter newChar)
 	{
 		Demigods.message.broadcast("Weiner check 1"); // TODO
 
 		Player player = getOfflinePlayer().getPlayer();
 
 		if(!newChar.getOfflinePlayer().getName().equals(player.getName()))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that.");
 			return;
 		}
 
 		Demigods.message.broadcast("Weiner check 2"); // TODO
 
 		// Update the current character
 		DCharacter currChar = getCurrent();
 
 		Demigods.message.broadcast("Weiner check 3"); // TODO
 
 		if(currChar != null)
 		{
 			Demigods.message.broadcast("Weiner check 4"); // TODO
 
 			// Set to inactive and update previous
 			currChar.setActive(false);
 			this.previous = currChar;
 
 			Demigods.message.broadcast("Weiner check 5"); // TODO
 
 			// Set the values
 			// TODO: Confirm that this covers all of the bases.
 			currChar.setMaxHealth(player.getMaxHealth());
 			currChar.setHealth(player.getHealth());
 			currChar.setHunger(player.getFoodLevel());
 			currChar.setLevel(player.getLevel());
 			currChar.setExperience(player.getExp());
 			currChar.setLocation(player.getLocation());
 			currChar.saveInventory();
 
 			Demigods.message.broadcast("Weiner check 6"); // TODO
 
 			// Disown pets
 			Pet.Util.disownPets(currChar.getName());
 
 			Demigods.message.broadcast("Weiner check 7"); // TODO
 
 			// Save it
 			JOhm.save(currChar);
 
 			Demigods.message.broadcast("Weiner check 8"); // TODO
 		}
 
 		Demigods.message.broadcast("Weiner check 9"); // TODO
 
 		// Set new character to active
 		newChar.setActive(true);
 		this.current = newChar;
 
 		Demigods.message.broadcast("Weiner check 10"); // TODO
 
 		// Update their inventory
 		if(getCharacters().size() == 1) newChar.saveInventory();
 		newChar.getInventory().setToPlayer(player);
 
 		Demigods.message.broadcast("Weiner check 11"); // TODO
 
 		// Update health, experience, and name
 		// TODO: Confirm that this covers all of the bases too.
 		player.setDisplayName(newChar.getDeity().getInfo().getColor() + newChar.getName());
 		try
 		{
 			player.setPlayerListName(newChar.getDeity().getInfo().getColor() + newChar.getName());
 		}
 		catch(Exception e)
 		{
 			Demigods.message.warning("Character name too long.");
 			e.printStackTrace();
 		}
 		player.setMaxHealth(newChar.getMaxHealth());
 		player.setHealth(newChar.getHealth());
 		player.setFoodLevel(newChar.getHunger());
 		player.setExp(newChar.getExperience());
 		player.setLevel(newChar.getLevel());
 
 		Demigods.message.broadcast("Weiner check 12"); // TODO
 
 		// Re-own pets
 		Pet.Util.reownPets(player, newChar);
 
 		Demigods.message.broadcast("Weiner check 13"); // TODO
 
 		// Teleport them
 		try
 		{
 			player.teleport(newChar.getLocation());
 		}
 		catch(Exception e)
 		{
 			Demigods.message.severe("There was a problem while teleporting a player to their character.");
 		}
 
 		Demigods.message.broadcast("Weiner check 14"); // TODO
 
 		// Save instances
 		Util.save(this);
 		DCharacter.Util.save(newChar);
 
 		Demigods.message.broadcast("Weiner check 15"); // TODO
 	}
 
 	public Long getId()
 	{
 		return this.id;
 	}
 
 	public Boolean canPvp()
 	{
 		return this.canPvp;
 	}
 
 	public boolean hasCurrent()
 	{
 		return getCurrent() != null;
 	}
 
 	public Region getRegion()
 	{
 		if(getOfflinePlayer().isOnline()) return Region.Util.getRegion(getOfflinePlayer().getPlayer().getLocation());
 		return Region.Util.getRegion(getCurrent().getLocation());
 	}
 
 	public DCharacter getCurrent()
 	{
 		if(this.current != null && this.current.isUsable()) return this.current;
 		return null;
 	}
 
 	public DCharacter getPrevious()
 	{
 		return this.previous;
 	}
 
 	public Set<DCharacter> getCharacters()
 	{
 		return new HashSet<DCharacter>()
 		{
 			{
				for(DCharacter character : (List<DCharacter>) JOhm.find(DCharacter.class, "player", getId()))
 				{
 					if(character != null && character.isUsable()) add(character);
 				}
 			}
 		};
 	}
 
 	public boolean canUseCurrent()
 	{
 		if(getCurrent() == null || !getCurrent().isUsable())
 		{
 			getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to load!");
 			getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
 			return false;
 		}
 		else
 		{
 			return getOfflinePlayer().isOnline();
 		}
 
 	}
 
 	public static class Util
 	{
 		public static DPlayer create(OfflinePlayer player)
 		{
 			DPlayer trackedPlayer = new DPlayer();
 			trackedPlayer.setPlayer(player.getName());
 			trackedPlayer.setLastLoginTime(player.getLastPlayed());
 			trackedPlayer.setCanPvp(true);
 			save(trackedPlayer);
 			return trackedPlayer;
 		}
 
 		public static void save(DPlayer trackedPlayer)
 		{
 			JOhm.save(trackedPlayer);
 		}
 
 		public static DPlayer load(Long id)
 		{
 			return JOhm.get(DPlayer.class, id);
 		}
 
 		public static DPlayer getPlayer(OfflinePlayer player)
 		{
 			try
 			{
 				List<DPlayer> list = JOhm.find(DPlayer.class, "player", player.getName());
 				return list.get(0);
 			}
 			catch(Exception ignored)
 			{}
 			return create(player);
 		}
 
 		public static DCharacter getCurrentCharacter(OfflinePlayer player)
 		{
 			return getPlayer(player).getCurrent();
 		}
 
 		/**
 		 * Returns the current alliance for <code>player</code>.
 		 * 
 		 * @param player the player to check.
 		 * @return String
 		 */
 		public static String getCurrentAlliance(OfflinePlayer player)
 		{
 			DCharacter character = getPlayer(player).getCurrent();
 			if(character == null || !character.isImmortal()) return "Mortal";
 			return character.getAlliance();
 		}
 
 		/**
 		 * Returns a List of all of <code>player</code>'s characters.
 		 * 
 		 * @param player the player to check.
 		 * @return List the list of all character IDs.
 		 */
 		public static Set<DCharacter> getCharacters(OfflinePlayer player)
 		{
 			return getPlayer(player).getCharacters();
 		}
 
 		/**
 		 * Returns true if the <code>player</code> is currently immortal.
 		 * 
 		 * @param player the player to check.
 		 * @return boolean
 		 */
 		public static boolean isImmortal(OfflinePlayer player)
 		{
 			DCharacter character = getPlayer(player).getCurrent();
 			return character != null && character.isImmortal();
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
 			final Set<DCharacter> characters = getCharacters(player);
 
 			for(DCharacter character : characters)
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
 		public static boolean isPraying(Player player)
 		{
 			try
 			{
 				return DataManager.hasKeyTemp(player.getName(), "prayer_conversation");
 			}
 			catch(Exception ignored)
 			{}
 			return false;
 		}
 
 		/**
 		 * Removes all temp data related to prayer for the <code>player</code>.
 		 * 
 		 * @param player the player to clean.
 		 */
 		public static void clearPrayerSession(Player player)
 		{
 			DataManager.removeTemp(player.getName(), "prayer_conversation");
 			DataManager.removeTemp(player.getName(), "prayer_context");
 			DataManager.removeTemp(player.getName(), "prayer_location");
 		}
 
 		/**
 		 * Returns the context for the <code>player</code>'s prayer converstion.
 		 * 
 		 * @param player the player whose context to return.
 		 * @return ConversationContext
 		 */
 		public static ConversationContext getPrayerContext(Player player)
 		{
 			if(!isPraying(player)) return null;
 			return (ConversationContext) DataManager.getValueTemp(player.getName(), "prayer_context");
 		}
 
 		/**
 		 * Changes prayer status for <code>player</code> to <code>option</code> and tells them.
 		 * 
 		 * @param player the player the manipulate.
 		 * @param option the boolean to set to.
 		 */
 		public static void togglePraying(Player player, boolean option)
 		{
 			if(option)
 			{
 				// Toggle on
 				togglePrayingSilent(player, true);
 			}
 			else
 			{
 				// Toggle off
 				togglePrayingSilent(player, false);
 
 				// Message them
 				clearChat(player);
 				for(String message : Demigods.language.getTextBlock(Translation.Text.PRAYER_ENDED))
 				{
 					player.sendMessage(message);
 				}
 			}
 		}
 
 		/**
 		 * Changes prayer status for <code>player</code> to <code>option</code> silently.
 		 * 
 		 * @param player the player the manipulate.
 		 * @param option the boolean to set to.
 		 */
 		public static void togglePrayingSilent(Player player, boolean option)
 		{
 			if(option)
 			{
 				// Create the conversation and save it
 				Conversation prayer = Prayer.startPrayer(player);
 				DataManager.saveTemp(player.getName(), "prayer_conversation", prayer);
 				DataManager.saveTemp(player.getName(), "prayer_location", player.getLocation());
 				player.setSneaking(true);
 			}
 			else
 			{
 				// Save context and abandon the conversation
 				if(DataManager.hasKeyTemp(player.getName(), "prayer_conversation"))
 				{
 					Conversation prayer = (Conversation) DataManager.getValueTemp(player.getName(), "prayer_conversation");
 					DataManager.saveTemp(player.getName(), "prayer_context", prayer.getContext());
 					prayer.abandon();
 				}
 
 				// Remove the data
 				DataManager.removeTemp(player.getName(), "prayer_conversation");
 				DataManager.removeTemp(player.getName(), "prayer_location");
 				player.setSneaking(false);
 			}
 		}
 
 		/**
 		 * Clears the chat for <code>player</code> using .sendMessage().
 		 * 
 		 * @param player the player whose chat to clear.
 		 */
 		public static void clearChat(Player player)
 		{
 			for(int x = 0; x < 120; x++)
 				player.sendMessage(" ");
 		}
 
 		/**
 		 * Clears the chat for <code>player</code> using .sendRawMessage().
 		 * 
 		 * @param player the player whose chat to clear.
 		 */
 		public static void clearRawChat(Player player)
 		{
 			for(int x = 0; x < 120; x++)
 				player.sendRawMessage(" ");
 		}
 
 		/**
 		 * Updates favor for all online players.
 		 * 
 		 * @param multiplier the favor multiplier.
 		 */
 		public static void updateFavor(double multiplier)
 		{
 			for(Player player : Bukkit.getOnlinePlayers())
 			{
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 				if(character == null || !character.isImmortal()) continue;
 				int regenRate = (int) Math.ceil(multiplier * character.getMeta().getAscensions());
 				if(regenRate < 5) regenRate = 5;
 				character.getMeta().addFavor(regenRate);
 			}
 		}
 	}
 }
