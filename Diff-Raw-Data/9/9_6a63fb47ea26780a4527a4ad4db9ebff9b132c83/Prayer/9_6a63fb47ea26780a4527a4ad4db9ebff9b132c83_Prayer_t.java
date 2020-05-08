 package com.censoredsoftware.demigods.engine.conversation;
 
 import com.censoredsoftware.censoredlib.data.player.Notification;
 import com.censoredsoftware.censoredlib.helper.WrappedConversation;
 import com.censoredsoftware.censoredlib.language.Symbol;
 import com.censoredsoftware.censoredlib.util.Strings;
 import com.censoredsoftware.censoredlib.util.Times;
 import com.censoredsoftware.censoredlib.util.Titles;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.DemigodsPlugin;
 import com.censoredsoftware.demigods.engine.base.DemigodsConversation;
 import com.censoredsoftware.demigods.engine.data.*;
 import com.censoredsoftware.demigods.engine.language.English;
 import com.censoredsoftware.demigods.engine.mythos.Alliance;
 import com.censoredsoftware.demigods.engine.mythos.Deity;
 import com.censoredsoftware.demigods.engine.mythos.Structure;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Messages;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.*;
 import org.bukkit.conversations.*;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.lang.reflect.Field;
 import java.util.*;
 
 @SuppressWarnings("unchecked")
 public class Prayer implements WrappedConversation
 {
 
 	@Override
 	public org.bukkit.event.Listener getUniqueListener()
 	{
 		return new Listener();
 	}
 
 	/**
 	 * Defines categories that can be used during prayer.
 	 */
 	public enum Menu
 	{
 		CONFIRM_FORSAKE('F', new ConfirmForsake()), CANCEL_FORSAKE('X', new CancelForsake()), CONFIRM_CHARACTER('C', new ConfirmCharacter()), CANCEL_CREATE_CHARACTER('X', new CancelCreateCharacter()), CREATE_CHARACTER('1', new CreateCharacter()), VIEW_CHARACTERS('2', new ViewCharacters()), VIEW_WARPS('3', new ViewWarps()), FORSAKE_CHARACTER('4', new Forsake()), VIEW_SKILL_POINTS('5', new ViewSkills()), VIEW_NOTIFICATIONS('6', new ViewNotifications());
 
 		private final char id;
 		private final DemigodsConversation.Category category;
 
 		private Menu(char id, DemigodsConversation.Category category)
 		{
 			this.id = id;
 			this.category = category;
 		}
 
 		public char getId()
 		{
 			return this.id;
 		}
 
 		public DemigodsConversation.Category getCategory()
 		{
 			return this.category;
 		}
 
 		public static Menu getFromId(char id)
 		{
 			for(Menu menu : Menu.values())
 				if(menu.getId() == id) return menu;
 			return null;
 		}
 	}
 
 	@Override
 	public Conversation startMenu(Player player)
 	{
 		return startPrayer(player);
 	}
 
 	public static Conversation startPrayer(Player player)
 	{
 		try
 		{
 			Map<Object, Object> conversationContext = Maps.newHashMap();
 
 			if(!Demigods.Util.isRunningSpigot())
 			{
 				// Compatibility with vanilla Bukkit
 				Field sessionDataField = ConversationContext.class.getDeclaredField("sessionData");
 				sessionDataField.setAccessible(true);
 				if(DataManager.hasKeyTemp(player.getName(), "prayer_context")) conversationContext = (Map<Object, Object>) sessionDataField.get(DataManager.getValueTemp(player.getName(), "prayer_context"));
 			}
 			else
 			{
 				// Grab the context Map
 				if(DataManager.hasKeyTemp(player.getName(), "prayer_context")) conversationContext.putAll(((ConversationContext) DataManager.getValueTemp(player.getName(), "prayer_context")).getAllSessionData());
 			}
 
 			// Build the CONVERSATION_FACTORY and begin
 			Conversation prayerConversation = Demigods.CONVERSATION_FACTORY.withEscapeSequence("/exit").withLocalEcho(false).withInitialSessionData(conversationContext).withFirstPrompt(new StartPrayer()).buildConversation(player);
 			prayerConversation.begin();
 
 			return prayerConversation;
 		}
 		catch(IllegalAccessException ignored)
 		{
 			// ignored
 		}
 		catch(NoSuchFieldException ignored)
 		{
 			// ignored
 		}
 
 		return null;
 	}
 
 	// Main prayer menu
 	static class StartPrayer extends ValidatingPrompt
 	{
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			// Clear chat
 			Messages.clearRawChat(player);
 
 			// Send NoGrief menu
 			Messages.clearRawChat(player);
 			player.sendRawMessage(ChatColor.AQUA + " -- Prayer Menu --------------------------------------");
 			player.sendRawMessage(" ");
 			for(String message : English.PRAYER_INTRO.getLines())
 				player.sendRawMessage(message);
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + " To begin, choose an option by entering its number in the chat:");
 			player.sendRawMessage(" ");
 
 			for(Menu menu : Menu.values())
 				if(menu.getCategory().canUse(context)) player.sendRawMessage(ChatColor.GRAY + "   [" + menu.getId() + ".] " + menu.getCategory().getChatName(context));
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			try
 			{
 				Menu menu = Menu.getFromId(java.lang.Character.toUpperCase(message.charAt(0)));
 				return menu != null && menu.getCategory().canUse(context);
 			}
 			catch(Exception ignored)
 			{
 				// ignored
 			}
 			return false;
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			return Menu.getFromId(java.lang.Character.toUpperCase(message.charAt(0))).getCategory();
 		}
 	}
 
 	// Warps
 	static class ViewWarps extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.LIGHT_PURPLE + "View Warps " + ChatColor.GRAY + "(& Invites)";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent() != null;
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			Messages.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Warps & Invites"));
 			player.sendRawMessage(" ");
 
 			if(character.getMeta().hasWarps() || character.getMeta().hasInvites())
 			{
 				player.sendRawMessage(ChatColor.LIGHT_PURPLE + "  Light purple" + ChatColor.GRAY + " represents the warp(s) at this location.");
 				player.sendRawMessage(" ");
 
 				for(Map.Entry<String, Object> entry : character.getMeta().getWarps().entrySet())
 				{
 					Location location = CLocationManager.load(UUID.fromString(entry.getValue().toString())).toLocation();
 					player.sendRawMessage((player.getLocation().distance(location) < 8 ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " (" + StringUtils.capitalize(location.getWorld().getName().toLowerCase()) + ": " + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + ")");
 				}
 				for(Map.Entry<String, Object> entry : character.getMeta().getInvites().entrySet())
 				{
 					Location location = CLocationManager.load(UUID.fromString(entry.getValue().toString())).toLocation();
 					player.sendRawMessage((player.getLocation().distance(location) < 8 ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " (" + StringUtils.capitalize(location.getWorld().getName().toLowerCase()) + ": " + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + ") " + ChatColor.GREEN + "Invited by [ALLAN!!]"); // TODO: Invited by
 				}
 
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "new <warp name>" + ChatColor.GRAY + " to create a warp at this Altar,");
 				player.sendRawMessage(ChatColor.YELLOW + "  warp <warp name>" + ChatColor.GRAY + " to teleport to a warp, or " + ChatColor.YELLOW + "delete");
 				player.sendRawMessage(ChatColor.YELLOW + "  <warp name>" + ChatColor.GRAY + " remove a warp. You can also invite a player");
 				player.sendRawMessage(ChatColor.GRAY + "  by using " + ChatColor.YELLOW + "invite <player/character> <warp name>" + ChatColor.GRAY + ".");
 			}
 			else
 			{
 				player.sendRawMessage(ChatColor.RED + "    You have no warps or invites!");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "new <warp name>" + ChatColor.GRAY + " to create a warp at this Altar.");
 			}
 
 			// Display notifications if available
 			if(context.getSessionData("warp_notifications") != null && !((List<English>) context.getSessionData("warp_notifications")).isEmpty())
 			{
 				// Grab the notifications
 				List<English> notifications = (List<English>) context.getSessionData("warp_notifications");
 
 				player.sendRawMessage(" ");
 
 				// List them
 				for(English notification : notifications)
 					player.sendRawMessage("  " + notification.getLine());
 
 				// Remove them
 				notifications.clear();
 			}
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length >= 2 ? message.split(" ")[1] : null;
 			String arg2 = message.split(" ").length >= 3 ? message.split(" ")[2] : null;
 
 			// Create and save the notification list
 			context.setSessionData("warp_notifications", Lists.newArrayList());
 			List<English> notifications = (List<English>) context.getSessionData("warp_notifications");
 
 			// Check validity
 			if(message.equalsIgnoreCase("menu")) return true;
 			else if(arg0.equalsIgnoreCase("new"))
 			{
 				if(StringUtils.isAlphanumeric(arg1) && !character.getMeta().getWarps().containsKey(arg1.toLowerCase())) return true;
 				notifications.add(English.NOTIFICATION_ERROR_CREATING_WARP);
 			}
 			else if(arg0.equalsIgnoreCase("warp"))
 			{
 				if((character.getMeta().getWarps().containsKey(arg1.toLowerCase()) || character.getMeta().getInvites().containsKey(arg1.toLowerCase()))) return true;
 				notifications.add(English.NOTIFICATION_ERROR_WARPING);
 			}
 			else if(arg0.equalsIgnoreCase("delete"))
 			{
 				if((character.getMeta().getWarps().containsKey(arg1.toLowerCase()) || character.getMeta().getInvites().containsKey(arg1.toLowerCase()))) return true;
 				notifications.add(English.NOTIFICATION_ERROR_DELETING_WARP);
 			}
 			else if(arg0.equalsIgnoreCase("invite"))
 			{
 				if((DCharacter.Util.charExists(arg1) || (DPlayer.Util.getPlayerFromName(arg1) != null && DPlayer.Util.getPlayerFromName(arg1).getCurrent() != null)) && arg2 != null && character.getMeta().getWarps().containsKey(arg2.toLowerCase())) return true;
 				notifications.add(English.NOTIFICATION_ERROR_INVITING);
 			}
 			else
 			{
 				// Fallback notification
 				notifications.add(English.NOTIFICATION_ERROR_MISC);
 			}
 
 			return false;
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length >= 2 ? message.split(" ")[1] : null;
 			String arg2 = message.split(" ").length >= 3 ? message.split(" ")[2] : null;
 
 			// Create and save the notification list
 			context.setSessionData("warp_notifications", Lists.newArrayList());
 			List<English> notifications = (List<English>) context.getSessionData("warp_notifications");
 
 			Messages.clearRawChat(player);
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				// THEY WANT THE MENU!? SOCK IT TO 'EM!
 				return new StartPrayer();
 			}
 			if(arg0.equalsIgnoreCase("new"))
 			{
 				// Save notification
 				notifications.add(English.NOTIFICATION_WARP_CREATED);
 
 				// Add the warp
 				character.getMeta().addWarp(arg1, player.getLocation());
 
 				// Return to view warps
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("delete"))
 			{
 				// Save notification
 				notifications.add(English.NOTIFICATION_WARP_DELETED);
 
 				// Remove the warp/invite
 				if(character.getMeta().getWarps().containsKey(arg1.toLowerCase())) character.getMeta().removeWarp(arg1);
 				else if(character.getMeta().getInvites().containsKey(arg1.toLowerCase())) character.getMeta().removeInvite(arg1);
 
 				// Return to view warps
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("invite"))
 			{
 				// Save notification
 				notifications.add(English.NOTIFICATION_INVITE_SENT);
 
 				// Define variables
 				DCharacter invitee = DCharacter.Util.charExists(arg1) ? DCharacter.Util.getCharacterByName(arg1) : DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(arg1)).getCurrent();
 				Location warp = CLocationManager.load(UUID.fromString(character.getMeta().getWarps().get(arg2).toString())).toLocation(); // NPE (can't figure out why)
 
 				// Add the invite
 				invitee.getMeta().addInvite(character.getName(), warp);
 
 				// Message the player if they're online
 				if(invitee.getOfflinePlayer().isOnline())
 				{
 					invitee.getOfflinePlayer().getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You've been invited to a warp by " + character.getName() + "!");
 					invitee.getOfflinePlayer().getPlayer().sendMessage(ChatColor.GRAY + "Go to an Altar to accept this invite.");
 				}
 
 				// Return to warps menu
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("warp"))
 			{
 				// Disable prayer
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 
 				// Teleport and message
 				if(character.getMeta().getWarps().containsKey(arg1.toLowerCase())) player.teleport(CLocationManager.load(UUID.fromString(character.getMeta().getWarps().get(arg1.toLowerCase()).toString())).toLocation());
 				else if(character.getMeta().getInvites().containsKey(arg1.toLowerCase()))
 				{
 					player.teleport(CLocationManager.load(UUID.fromString(character.getMeta().getInvites().get(arg1.toLowerCase()).toString())).toLocation());
 					character.getMeta().removeInvite(arg1.toLowerCase());
 				}
 				player.sendMessage(ChatColor.GRAY + "Teleported to " + ChatColor.LIGHT_PURPLE + StringUtils.capitalize(arg1.toLowerCase()) + ChatColor.GRAY + ".");
 			}
 			return null;
 		}
 	}
 
 	// Skills
 	static class ViewSkills extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.DARK_PURPLE + "View Skills";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return DPlayer.Util.getPlayer((Player) context.getForWhom()).hasCurrent();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			int skillPoints = character.getMeta().getSkillPoints();
 
 			Messages.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Skills"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage("  " + English.DIRECTIONS_MAIN_MENU_PRAYER);
 			player.sendRawMessage(" ");
 
 			for(Skill skill : character.getMeta().getSkills())
 			{
 				if(skill.getType().isLevelable()) player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + (skill.hasMetCap() ? ChatColor.GRAY + "" + ChatColor.ITALIC : ChatColor.AQUA) + skill.getType().getName() + ChatColor.RESET + ChatColor.GRAY + " (" + (skill.hasMetCap() ? ChatColor.GREEN + "Max Level [" + skill.getLevel() + "]" : "Level " + ChatColor.GREEN + skill.getLevel() + ChatColor.GRAY + ") (" + ChatColor.YELLOW + skill.getRequiredPoints() + ChatColor.GRAY + " skill points from level " + ChatColor.YELLOW + (skill.getLevel() + 1)) + ChatColor.GRAY + ")");
 			}
 
 			player.sendRawMessage(" ");
 			if(skillPoints > 0) // TODO Translations
 			{
 				player.sendRawMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + "  You currently have " + ChatColor.GREEN + character.getMeta().getSkillPoints() + "" + ChatColor.GRAY + " skill points available.");
 				player.sendRawMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + "  To assign your skill points, use " + ChatColor.YELLOW + "assign <amount> <skill>" + ChatColor.GRAY + ".");
 			}
 			else
 			{
 				player.sendRawMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + "  You currently have no skill points available for assignment.");
 				player.sendRawMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + "  Battle to earn some!");
 			}
 
 			// Display notifications if available
 			if(context.getSessionData("skill_notifications") != null && !((List<English>) context.getSessionData("skill_notifications")).isEmpty())
 			{
 				// Grab the notifications
 				List<English> notifications = (List<English>) context.getSessionData("skill_notifications");
 
 				player.sendRawMessage(" ");
 
 				// List them
 				for(English notification : notifications)
 					player.sendRawMessage("  " + notification.getLine());
 
 				// Remove them
 				notifications.clear();
 			}
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			String[] splitMsg = message.split(" ");
 
 			// Create and save the notification list
 			context.setSessionData("skill_notifications", Lists.newArrayList());
 			List<English> notifications = (List<English>) context.getSessionData("skill_notifications");
 
 			try
 			{
 				if(message.equalsIgnoreCase("menu")) return true;
 
 				if(splitMsg[0].equalsIgnoreCase("assign") && splitMsg.length >= 3)
 				{
 					ArrayList<String> input = new ArrayList<>(Arrays.asList(splitMsg));
 
 					// This looks funky, but it's supposed to be like this
 					input.remove(0);
 					input.remove(0);
 
 					Skill.Type skillType = Skill.Type.valueOf(StringUtils.join(input, "_").toUpperCase());
 
 					if(DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent().getMeta().getSkill(skillType) != null && skillType.isLevelable())
 					{
 						return true;
 					}
 					else
 					{
 						notifications.add(English.NOTIFICATION_ERROR_UNOBTAINED_SKILL);
 						return false;
 					}
 				}
 				else
 				{
 					notifications.add(English.NOTIFICATION_ERROR_SKILL_DOESNT_EXIST);
 					return false;
 				}
 			}
 			catch(Exception errored)
 			{
 				notifications.add(English.NOTIFICATION_ERROR_MISC);
 				return false;
 			}
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			String[] splitMsg = message.split(" ");
 
 			// Create and save the notifications
 			context.setSessionData("skill_notifications", Lists.newArrayList());
 			List<English> notifications = (List<English>) context.getSessionData("skill_notifications");
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				// THEY WANT THE MENU!? SOCK IT TO 'EM!
 				return new StartPrayer();
 			}
 			else if(splitMsg[0].equalsIgnoreCase("assign"))
 			{
 				ArrayList<String> input = new ArrayList<>(Arrays.asList(splitMsg));
 
 				int points = Integer.parseInt(input.get(1));
 
 				// Again, this looks funky, but it's supposed to be like this
 				input.remove(0);
 				input.remove(0);
 
 				Skill skill = character.getMeta().getSkill(Skill.Type.valueOf(StringUtils.join(input, "_").toUpperCase()));
 
 				if(character.getMeta().getSkillPoints() >= points)
 				{
 					// Apply the points and notify
 					skill.addPoints(points);
 					character.getMeta().subtractSkillPoints(points);
 
 					// Save the notification
 					notifications.add(English.NOTIFICATION_SKILL_POINTS_ASSIGNED);
 				}
 				else
 				{
 					// They don't have enough points, save the notification
 					notifications.add(English.ERROR_NOT_ENOUGH_SKILL_POINTS);
 				}
 			}
 
 			return new ViewSkills();
 		}
 	}
 
 	// Notifications
 	static class ViewNotifications extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.GREEN + "View Notifications";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			return character != null && character.getMeta().hasNotifications();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			Messages.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Notifications"));
 			player.sendRawMessage(" ");
 
 			for(String string : character.getMeta().getNotifications())
 			{
 				Notification notification = NotificationManager.load(UUID.fromString(string));
 				// Determine color
 				ChatColor color;
 				switch(notification.getDanger())
 				{
 					case GOOD:
 						color = ChatColor.GREEN;
 						break;
 					case BAD:
 						color = ChatColor.RED;
 						break;
 					case NEUTRAL:
 					default:
 						color = ChatColor.YELLOW;
 						break;
 				}
 
 				// Set expires
 				String expires = notification.hasExpiration() ? ChatColor.GRAY + " (expires in " + Times.getTimeTagged(notification.getExpiration(), true) + ")" : "";
 
 				// Send the notification
 				player.sendRawMessage(color + "    " + notification.getMessage() + expires);
 			}
 
 			player.sendRawMessage(" ");
 			for(String message : English.NOTIFICATIONS_PRAYER_FOOTER.getLines())
 				player.sendRawMessage(message);
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.equalsIgnoreCase("clear") || message.equalsIgnoreCase("menu");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				// THEY WANT THE MENU!? SOCK IT TO 'EM!
 				return new StartPrayer();
 			}
 			else if(message.equalsIgnoreCase("clear"))
 			{
 				// Clear them
 				for(String string : character.getMeta().getNotifications())
 					NotificationManager.remove(NotificationManager.load(UUID.fromString(string)));
 				character.getMeta().clearNotifications();
 
 				// Send to the menu
 				return new StartPrayer();
 			}
 			return null;
 		}
 	}
 
 	// DCharacter viewing
 	static class ViewCharacters extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.YELLOW + "View Characters";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return DPlayer.Util.getPlayer((Player) context.getForWhom()).getCharacters() != null && !DPlayer.Util.getPlayer((Player) context.getForWhom()).getCharacters().isEmpty();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			Messages.clearRawChat(player);
 
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Character"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.LIGHT_PURPLE + "  Light purple" + ChatColor.GRAY + " represents your current character.");
 			player.sendRawMessage(" ");
 
 			for(DCharacter character : DPlayer.Util.getPlayer(player).getCharacters())
 				player.sendRawMessage((character.isActive() ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + character.getName() + ChatColor.GRAY + " [" + character.getDeity().getColor() + character.getDeity().getName() + ChatColor.GRAY + " / Fav: " + Strings.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") / Asc: " + ChatColor.GREEN + character.getMeta().getAscensions() + ChatColor.GRAY + "]");
 
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "<character name> info" + ChatColor.GRAY + " for detailed information or");
 			player.sendRawMessage(ChatColor.GRAY + "  type " + ChatColor.YELLOW + "<character name> switch" + ChatColor.GRAY + " to change your current");
 			player.sendRawMessage(ChatColor.GRAY + "  character.");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Use " + ChatColor.YELLOW + "menu" + ChatColor.GRAY + " to return to the main menu.");
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			String[] splitMsg = message.split(" ");
 			DPlayer player = DPlayer.Util.getPlayer((Player) context.getForWhom());
 			DCharacter character = DCharacter.Util.getCharacterByName(splitMsg[0]);
 			return message.equalsIgnoreCase("menu") || splitMsg.length == 2 && (DPlayer.Util.hasCharName((Player) context.getForWhom(), splitMsg[0]) && (splitMsg[1].equalsIgnoreCase("info") || (DPlayer.Util.hasCharName((Player) context.getForWhom(), splitMsg[0]) && splitMsg[1].equalsIgnoreCase("switch")) && (player.getCurrent() == null || !player.getCurrent().getName().equalsIgnoreCase(character.getName()))));
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length == 2 ? message.split(" ")[1] : "";
 
 			if(message.equalsIgnoreCase("menu")) return new StartPrayer();
 			if(arg1.equalsIgnoreCase("info"))
 			{
 				context.setSessionData("viewing_character", arg0);
 				return new DetailedInfo();
 			}
 			else if(arg1.equalsIgnoreCase("switch"))
 			{
 				DPlayer.Util.getPlayer((Player) context.getForWhom()).switchCharacter(DCharacter.Util.getCharacterByName(arg0));
 			}
 			return null;
 		}
 
 		// Detailed character info
 		class DetailedInfo extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				// Define variables
 				Player player = (Player) context.getForWhom();
 				DCharacter character = DCharacter.Util.getCharacterByName(context.getSessionData("viewing_character").toString());
 				String status = character.isActive() ? ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "(Current) " + ChatColor.RESET : ChatColor.RED + "" + ChatColor.ITALIC + "(Inactive) " + ChatColor.RESET;
 
 				// Clear chat
 				Messages.clearRawChat(player);
 
 				// Send the player the info
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Character"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage("    " + status + ChatColor.YELLOW + character.getName() + ChatColor.GRAY + " > Allied to " + character.getDeity().getColor() + character.getDeity() + ChatColor.GRAY + " of the " + ChatColor.GOLD + character.getAlliance() + "s");
 				player.sendRawMessage(ChatColor.GRAY + "  --------------------------------------------------");
 				player.sendRawMessage(ChatColor.GRAY + "    Health: " + ChatColor.WHITE + Strings.getColor(character.getHealth(), character.getMaxHealth()) + character.getHealth() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMaxHealth() + ChatColor.GRAY + ")" + ChatColor.GRAY + "  |  Hunger: " + ChatColor.WHITE + Strings.getColor(character.getHunger(), 20) + character.getHunger() + ChatColor.GRAY + " (of " + ChatColor.GREEN + 20 + ChatColor.GRAY + ")" + ChatColor.GRAY + "  |  Exp: " + ChatColor.GREEN + Math.round(character.getExperience())); // TODO: Exp isn't correct.
 				player.sendRawMessage(ChatColor.GRAY + "  --------------------------------------------------");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "    Ascensions: " + ChatColor.GREEN + character.getMeta().getAscensions());
 				player.sendRawMessage(ChatColor.GRAY + "    Favor: " + Strings.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") " + ChatColor.YELLOW + "+" + character.getFavorRegen() + " every " + Configs.getSettingInt("regeneration_rates.favor") + " seconds");
 				player.sendRawMessage(" ");
 				if(character.isActive()) player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "back" + ChatColor.GRAY + " to return to your characters.");
 				else
 				{
 					player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "back" + ChatColor.GRAY + " to return to your characters or type " + ChatColor.YELLOW + "switch");
 					player.sendRawMessage(ChatColor.GRAY + "  to change your current character to " + character.getDeity().getColor() + character.getName() + ChatColor.GRAY + ".");
 				}
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				DPlayer player = DPlayer.Util.getPlayer((Player) context.getForWhom());
 				DCharacter character = DCharacter.Util.getCharacterByName(context.getSessionData("viewing_character").toString());
 
 				return message.equalsIgnoreCase("back") || (message.equalsIgnoreCase("switch") && (player.getCurrent() == null || !player.getCurrent().getName().equalsIgnoreCase(character.getName())));
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.equalsIgnoreCase("back")) return new ViewCharacters();
 				else if(message.equalsIgnoreCase("switch")) DPlayer.Util.getPlayer((Player) context.getForWhom()).switchCharacter(DCharacter.Util.getCharacterByName(context.getSessionData("viewing_character").toString()));
 				return null;
 			}
 		}
 	}
 
 	// Deity forsaking
 	static class Forsake extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.DARK_RED + "Forsake Current Deity";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			return character != null && ((Player) context.getForWhom()).hasPermission("demigods.basic.forsake") && !DataManager.hasTimed(((Player) context.getForWhom()).getName(), "currently_creating") && !DataManager.hasTimed(((Player) context.getForWhom()).getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			Deity deity = character.getDeity();
 
 			Messages.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Forsake Current Deity"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage("  " + deity.getColor() + deity.getName() + ChatColor.GRAY + " is angry with your decision and demands");
 			player.sendRawMessage(ChatColor.GRAY + "  payment from you before forsaking!");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Are you sure that you want to forsake " + deity.getColor() + deity.getName() + ChatColor.GRAY + "? " + ChatColor.GRAY + "(y/n)");
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.equalsIgnoreCase("y") || message.equalsIgnoreCase("n");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			if(message.equalsIgnoreCase("n")) return new StartPrayer();
 			else if(message.equalsIgnoreCase("y"))
 			{
 				// Define variables
 				Player player = (Player) context.getForWhom();
 				DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 				Deity deity = character.getDeity();
 
 				Messages.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Forsake Current Deity"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage("  " + deity.getColor() + deity.getName() + ChatColor.GRAY + " requires that you bring the following items");
 				player.sendRawMessage(ChatColor.GRAY + "  before forsaking:");
 				player.sendRawMessage(" ");
 				for(Map.Entry<Material, Integer> entry : deity.getForsakeItems().entrySet())
 					player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Return to an Altar after obtaining these items to finish");
 				player.sendRawMessage(ChatColor.GRAY + "  forsaking.");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.AQUA + "  Your prayer has been disabled.");
 				player.sendRawMessage(" ");
 
 				// Save temporary data, end the CONVERSATION_FACTORY, and return
 				DataManager.saveTimed(player.getName(), "currently_forsaking", true, 600);
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 			}
 			return null;
 		}
 	}
 
 	// Forsaking confirmation
 	static class ConfirmForsake extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.DARK_RED + "Finish Forsaking " + ChatColor.GRAY + "(" + Times.getTimeTagged(DataManager.getTimedExpiration(((Player) context.getForWhom()).getName(), "currently_forsaking"), true) + " remaining)";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasTimed(player.getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			Deity deity = DPlayer.Util.getPlayer(player).getCurrent().getDeity();
 
 			// Clear chat
 			Messages.clearRawChat(player);
 
 			// Ask them if they have the items
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Forsaking " + deity.getName()));
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.AQUA + "  Do you have the following items in your inventory? " + ChatColor.GRAY + "(y/n)");
 			player.sendRawMessage(" ");
 			for(Map.Entry<Material, Integer> entry : deity.getForsakeItems().entrySet())
 				player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.contains("y") || message.contains("n");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			Player player = (Player) context.getForWhom();
 
 			// Open inventory
 			if(message.contains("y"))
 			{
 				Inventory inv = Bukkit.getServer().createInventory(player, 9, "Place Items Here");
 				player.openInventory(inv);
 			}
 			else return new StartPrayer();
 
 			return null;
 		}
 	}
 
 	// Forsaking cancellation
 	static class CancelForsake extends MessagePrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.DARK_RED + "Cancel Forsaking";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasTimed(player.getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			// Cancel the temp data
 			DataManager.removeTimed(player.getName(), "currently_forsaking");
 
 			return "";
 		}
 
 		@Override
 		protected Prompt getNextPrompt(ConversationContext context)
 		{
 			return new StartPrayer();
 		}
 	}
 
 	// DCharacter creation
 	static class CreateCharacter extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.GREEN + "Create Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return ((Player) context.getForWhom()).hasPermission("demigods.basic.create") && !DataManager.hasTimed(((Player) context.getForWhom()).getName(), "currently_creating") && !DataManager.hasTimed(((Player) context.getForWhom()).getName(), "currently_forsaking") && DPlayer.Util.getPlayer((Player) context.getForWhom()).canMakeCharacter();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			Messages.clearRawChat((Player) context.getForWhom());
 			return ChatColor.AQUA + "Continue to character creation?" + ChatColor.GRAY + " (y/n)";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.contains("y") || message.contains("n");
 		}
 
 		@Override
 		protected ValidatingPrompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			if(message.contains("y")) return new ChooseName();
 			return new StartPrayer();
 		}
 
 		class ChooseName extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 				Messages.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Creating Character"));
 				player.sendRawMessage(" ");
 
 				if(context.getSessionData("name_errors") == null)
 				{
 					// No errors, continue
 					player.sendRawMessage(ChatColor.AQUA + "  Enter a name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 				}
 				else
 				{
 					// Grab the errors
 					List<English> errors = (List<English>) context.getSessionData("name_errors");
 
 					// List the errors
 					for(English error : errors)
 					{
 						player.sendRawMessage(ChatColor.RED + "  " + error.getLine().replace("{maxCaps}", String.valueOf(Configs.getSettingInt("character.max_caps_in_name"))));
 					}
 
 					// Ask for a new name
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.AQUA + "  Enter a different name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 				}
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String name)
 			{
 				if(name.length() < 2 || name.length() > 13 || !StringUtils.isAlphanumeric(name) || Strings.hasCapitalLetters(name, Configs.getSettingInt("character.max_caps_in_name")) || DCharacter.Util.charExists(name) || Strings.containsAnyInCollection(name, getBlackList()))
 				{
 					// Create the list
 					List<English> errors = Lists.newArrayList();
 
 					// Check the errors
 					if(name.length() < 2 || name.length() >= 13) errors.add(English.ERROR_NAME_LENGTH);
 					if(!StringUtils.isAlphanumeric(name)) errors.add(English.ERROR_ALPHA_NUMERIC);
 					if(Strings.hasCapitalLetters(name, Configs.getSettingInt("character.max_caps_in_name"))) errors.add(English.ERROR_MAX_CAPS);
 					if(DCharacter.Util.charExists(name) || Strings.containsAnyInCollection(name, getBlackList())) errors.add(English.ERROR_CHAR_EXISTS);
 
 					// Save the info
 					context.setSessionData("name_errors", errors);
 					return false;
 				}
 				else
 				{
 					context.setSessionData("name_errors", null);
 					return true;
 				}
 			}
 
 			@Override
 			protected ConfirmName acceptValidatedInput(ConversationContext context, String name)
 			{
 				context.setSessionData("chosen_name", name);
 				return new ConfirmName();
 			}
 		}
 
 		class ConfirmName extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Messages.clearRawChat((Player) context.getForWhom());
 				return ChatColor.GRAY + "Are you sure you want to use " + ChatColor.YELLOW + context.getSessionData("chosen_name") + ChatColor.GRAY + "? (y/n)";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.contains("y") || message.contains("n");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.contains("y")) return new ChooseAlliance();
 				else
 				{
 					context.setSessionData("chosen_name", null);
 					return new ChooseName();
 				}
 			}
 		}
 
 		class ChooseAlliance extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 
 				Messages.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Creating Character"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.AQUA + "  Please choose an Alliance: " + ChatColor.GRAY + "(Type in the name of the Alliance)");
 				player.sendRawMessage(" ");
 
 				for(Alliance alliance : Demigods.mythos().getAlliances())
 					if(player.hasPermission(alliance.getPermission()) && alliance.isPlayable() && Alliance.Util.getLoadedMajorPlayableDeitiesInAllianceWithPerms(alliance, player).size() > 0) player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.YELLOW + StringUtils.capitalize(alliance.getName().toLowerCase()));
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, final String alliance)
 			{
 				try
 				{
 					Alliance chosen = Alliance.Util.valueOf(alliance);
 					return chosen.isPlayable() && ((Player) context.getForWhom()).hasPermission(chosen.getPermission());
 				}
 				catch(Exception ignored)
 				{
 					// ignored
 				}
 				return false;
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String alliance)
 			{
 				context.setSessionData("chosen_alliance", Alliance.Util.valueOf(alliance));
 				return new ConfirmAlliance();
 			}
 		}
 
 		class ConfirmAlliance extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Messages.clearRawChat((Player) context.getForWhom());
 				return ChatColor.GRAY + "Are you sure you want to join the " + ChatColor.YELLOW + StringUtils.capitalize(((Alliance) context.getSessionData("chosen_alliance")).getName()) + "s" + ChatColor.GRAY + "? (y/n)";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.contains("y") || message.contains("n");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.contains("y")) return new ChooseDeity();
 				else
 				{
 					context.setSessionData("chosen_alliance", null);
 					return new ChooseAlliance();
 				}
 			}
 		}
 
 		class ChooseDeity extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 
 				Messages.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Creating Character"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.AQUA + "  Please choose a Deity: " + ChatColor.GRAY + "(Type in the name of the Deity)");
 				player.sendRawMessage(" ");
 
 				for(Deity deity : Alliance.Util.getLoadedMajorPlayableDeitiesInAllianceWithPerms((Alliance) context.getSessionData("chosen_alliance"), player))
 					if(player.hasPermission(deity.getPermission())) player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + (deity.getFlags().contains(Deity.Flag.DIFFICULT) ? ChatColor.DARK_RED : ChatColor.YELLOW) + StringUtils.capitalize(deity.getName()) + ChatColor.GRAY + " - " + deity.getShortDescription());
 
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  A " + ChatColor.DARK_RED + "dark red" + ChatColor.GRAY + " name represents a difficult Deity to please.");
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String deityName)
 			{
 				return Demigods.mythos().getDeity(deityName) != null && Demigods.mythos().getDeity(deityName).getFlags().contains(Deity.Flag.PLAYABLE) && ((Player) context.getForWhom()).hasPermission(Demigods.mythos().getDeity(deityName).getPermission());
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String deityName)
 			{
 				context.setSessionData("chosen_deity", deityName);
 				return new ConfirmDeity();
 			}
 		}
 
 		class ConfirmDeity extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Messages.clearRawChat((Player) context.getForWhom());
 				Deity deity = Demigods.mythos().getDeity((String) context.getSessionData("chosen_deity"));
 				return ChatColor.GRAY + "Are you sure you want to use " + deity.getColor() + deity.getName() + ChatColor.GRAY + "? (y/n)";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.contains("y") || message.contains("n");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.contains("y"))
 				{
 					// Define variables
 					Player player = (Player) context.getForWhom();
 					String chosenDeity = (String) context.getSessionData("chosen_deity");
 
 					// Give the player further directions
 					Messages.clearRawChat(player);
 					player.sendRawMessage(ChatColor.AQUA + "  Before you can confirm your lineage with " + ChatColor.YELLOW + StringUtils.capitalize(chosenDeity) + ChatColor.AQUA + ",");
 					player.sendRawMessage(ChatColor.AQUA + "  you must first sacrifice the following items:");
 					player.sendRawMessage(" ");
 					for(Map.Entry<Material, Integer> entry : Demigods.mythos().getDeity(chosenDeity).getClaimItems().entrySet())
						player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(Strings.plural(entry.getKey().name().toLowerCase(), entry.getValue())));
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.GRAY + "  After you obtain these items, return to an Altar to");
 					player.sendRawMessage(ChatColor.GRAY + "  confirm your new character.");
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.AQUA + "  Your prayer has been disabled.");
 					player.sendRawMessage(" ");
 
 					// Save temporary data, end the CONVERSATION_FACTORY, and return
 					DataManager.saveTimed(player.getName(), "currently_creating", true, 600);
 					DPlayer.Util.togglePrayingSilent(player, false, true);
 					return null;
 				}
 				else
 				{
 					context.setSessionData("chosen_deity", null);
 					return new ChooseDeity();
 				}
 			}
 		}
 	}
 
 	// DCharacter confirmation
 	static class ConfirmCharacter extends ValidatingPrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.GREEN + "Confirm Character " + ChatColor.GRAY + "(" + Times.getTimeTagged(DataManager.getTimedExpiration(((Player) context.getForWhom()).getName(), "currently_creating"), true) + " remaining)";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasTimed(player.getName(), "currently_creating");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			String chosenDeity = (String) context.getSessionData("chosen_deity");
 
 			// Clear chat
 			Messages.clearRawChat(player);
 
 			// Ask them if they have the items
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Confirming Character"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.AQUA + "  Do you have the following items in your inventory?" + ChatColor.GRAY + " (y/n)");
 			player.sendRawMessage(" ");
 			for(Map.Entry<Material, Integer> entry : Demigods.mythos().getDeity(chosenDeity).getClaimItems().entrySet())
 				player.sendRawMessage(ChatColor.GRAY + "    " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.contains("y") || message.contains("n");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			Player player = (Player) context.getForWhom();
 
 			// Open inventory
 			Inventory inv = Bukkit.getServer().createInventory(player, 9, "Place Your Tributes Here");
 			player.openInventory(inv);
 
 			return null;
 		}
 	}
 
 	// DCharacter creation cancellation
 	static class CancelCreateCharacter extends MessagePrompt implements DemigodsConversation.Category
 	{
 		@Override
 		public String getChatName(ConversationContext context)
 		{
 			return ChatColor.DARK_RED + "Cancel Character Creation";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasTimed(player.getName(), "currently_creating");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			// Cancel the temp data
 			DataManager.removeTimed(player.getName(), "currently_creating");
 
 			return "";
 		}
 
 		@Override
 		protected Prompt getNextPrompt(ConversationContext context)
 		{
 			return new StartPrayer();
 		}
 	}
 
 	public static class Listener implements org.bukkit.event.Listener
 	{
 		@EventHandler(priority = EventPriority.HIGH)
 		public void prayerInteract(PlayerInteractEvent event)
 		{
 			if(event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 			// Define variables
 			Player player = event.getPlayer();
 
 			// First we check if the player is clicking a prayer block
 			if(Structure.Util.isClickableBlockWithFlag(event.getClickedBlock().getLocation(), Structure.Flag.PRAYER_LOCATION))
 			{
 				if(!DPlayer.Util.isPraying(player))
 				{
 					if(DPlayer.Util.getPlayer(player).canPvp())
 					{
 						for(String message : English.PVP_NO_PRAYER.getLines())
 							player.sendMessage(message);
 						event.setCancelled(true);
 						return;
 					}
 
 					// Toggle praying
 					DPlayer.Util.togglePraying(player, true);
 
 					// Tell nearby players that the user is praying
 					for(Entity entity : player.getNearbyEntities(20, 20, 20))
 						if(entity instanceof Player) ((Player) entity).sendMessage(ChatColor.AQUA + English.KNELT_FOR_PRAYER.getLine().replace("{player}", ChatColor.stripColor(player.getDisplayName())));
 				}
 				else if(DPlayer.Util.isPraying(player))
 				{
 					// Toggle prayer to false
 					DPlayer.Util.togglePraying(player, false);
 				}
 
 				event.setCancelled(true);
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void confirmDeity(final InventoryCloseEvent event)
 		{
 			try
 			{
 				if(!(event.getPlayer() instanceof Player)) return;
 				Player player = (Player) event.getPlayer();
 				final OfflinePlayer offlinePlayer = player;
 
 				// If it isn't a confirmation chest then exit
 				if(!event.getInventory().getName().contains("Place Your Tributes Here")) return;
 
 				// Exit if this isn't for character creation
 				if(!DPlayer.Util.isPraying(player)) return;
 
 				// Define variables
 				ConversationContext prayerContext = DPlayer.Util.getPrayerContext(player);
 				final String chosenName = (String) prayerContext.getSessionData("chosen_name");
 				final Deity deity = Demigods.mythos().getDeity((String) prayerContext.getSessionData("chosen_deity"));
 				final String deityName = deity.getName();
 				final ChatColor deityColor = deity.getColor();
 
 				// Check the chest items
 				int items = 0;
 				final int neededItems = deity.getClaimItems().size();
 
 				for(Map.Entry<Material, Integer> entry : deity.getClaimItems().entrySet())
 					if(event.getInventory().contains(entry.getKey(), entry.getValue())) items++;
 
 				// Clear chat and send update
 				Messages.clearRawChat(player);
 
 				// Stop their praying
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 
 				player.sendMessage(deityColor + deityName + ChatColor.GRAY + " is pondering your offerings...");
 
 				// Finalize stuff for delay
 				final int finalItems = items;
 
 				// Play scary sound
 				player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 0.6F, 1F);
 
 				// Delay for dramatic effect
 				Bukkit.getScheduler().scheduleSyncDelayedTask(DemigodsPlugin.plugin(), new BukkitRunnable()
 				{
 					@Override
 					public void run()
 					{
 						if(neededItems == finalItems)
 						{
 							// Accepted, finish everything up!
 							DCharacter.Util.create(DPlayer.Util.getPlayer(offlinePlayer), deity.getName(), chosenName, true);
 
 							// Remove temp data
 							DataManager.removeTemp(offlinePlayer.getName(), "currently_creating");
 							DataManager.removeTimed(offlinePlayer.getName(), "currently_creating");
 
 							// If the player is online, let them know
 							if(offlinePlayer.isOnline())
 							{
 								// Redefine player
 								Player player = offlinePlayer.getPlayer();
 
 								// Play acceptance sound
 								player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1F, 1F);
 
 								// Message them and do cool things
 								player.sendMessage(ChatColor.GREEN + English.CHARACTER_CREATE_COMPLETE.getLine().replace("{deity}", deityName));
 								player.getWorld().strikeLightningEffect(player.getLocation());
 
 								// Fancy particles
 								for(int i = 0; i < 20; i++)
 									player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
 							}
 						}
 						else if(offlinePlayer.isOnline())
 						{
 							// Redefine player
 							Player player = offlinePlayer.getPlayer();
 
 							// Play denial sound
 							player.playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 0.7F, 2F);
 							player.playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 2F, 2F);
 							player.playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 0.4F, 2F);
 							player.playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 1F, 2F);
 
 							player.sendMessage(ChatColor.RED + deityName + " has rejected you due to your insufficient tribute.");
 						}
 
 						// Clear the prayer session
 						DPlayer.Util.clearPrayerSession(offlinePlayer);
 
 						// Clear the confirmation case
 						event.getInventory().clear();
 					}
 				}, 80); // 4 seconds
 			}
 			catch(Exception errored)
 			{
 				// Print error for debugging
 				Messages.logException(errored);
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void forsakeDeity(InventoryCloseEvent event)
 		{
 			try
 			{
 				if(!(event.getPlayer() instanceof Player)) return;
 				Player player = (Player) event.getPlayer();
 
 				// If it isn't a confirmation chest then exit
 				if(!event.getInventory().getName().contains("Place Items Here")) return;
 
 				// Exit if this isn't for character creation
 				if(!DPlayer.Util.isPraying(player)) return;
 
 				// Define variables
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 				Deity deity = character.getDeity();
 
 				// Check the chest items
 				int items = 0;
 				int neededItems = deity.getForsakeItems().size();
 
 				for(Map.Entry<Material, Integer> entry : deity.getForsakeItems().entrySet())
 					if(event.getInventory().contains(entry.getKey(), entry.getValue())) items++;
 
 				// Stop their praying
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 
 				// Clear chat and send update
 				Messages.clearRawChat(player);
 				player.sendMessage(ChatColor.YELLOW + deity.getName() + " is debating your departure...");
 
 				if(neededItems == items)
 				{
 					// TODO: Look into why this is so fancy even though the player gets kicked immediately...
 
 					// Clear the prayer session first
 					DPlayer.Util.clearPrayerSession(player);
 
 					// Accepted, delete the character and message the player
 					character.remove();
 					player.sendMessage(ChatColor.GREEN + "You are now free from the will of " + deity.getName() + "!");
 
 					// Add potion effects for fun
 					PotionEffect potion = new PotionEffect(PotionEffectType.WEAKNESS, 1200, 3);
 					player.addPotionEffect(potion);
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED + deity.getName() + " has denied your forsaking!");
 				}
 
 				// Clear the confirmation case
 				event.getInventory().clear();
 			}
 			catch(Exception errored)
 			{
 				// Print error for debugging
 				Messages.logException(errored);
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		private void onPlayerMove(PlayerMoveEvent event)
 		{
 			// Define variables
 			Player player = event.getPlayer();
 
 			if(DPlayer.Util.isPraying(player) && event.getTo().distance((Location) DataManager.getValueTemp(player.getName(), "prayer_location")) >= Configs.getSettingInt("zones.prayer_radius")) DPlayer.Util.togglePraying(player, false);
 		}
 	}
 
 	public static Set<String> getBlackList()
 	{
 		Set<String> set = Sets.newHashSet();
 
 		// Manual Blacklist
 		set.add("Pussy");
 		set.add("Cock");
 		set.add("Fuck");
 		set.add("Shit");
 		set.add("Ass");
 		set.add("Dick");
 		set.add("Penis");
 		set.add("Vagina");
 		set.add("Cunt");
 		set.add("Bitch");
 		set.add("Nigger");
 		set.add("Phil");
 		set.add("Staff");
 		set.add("Server");
 		set.add("Console");
 		set.add("Disowned");
 
 		// Deities
 		for(Deity deity : Demigods.mythos().getDeities())
 		{
 			set.add(deity.getName());
 			set.add(deity.getAlliance().getName());
 		}
 
 		return set;
 	}
 }
