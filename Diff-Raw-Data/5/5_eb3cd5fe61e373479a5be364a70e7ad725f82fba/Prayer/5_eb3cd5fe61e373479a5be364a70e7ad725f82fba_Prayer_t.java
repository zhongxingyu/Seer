 package com.censoredsoftware.Demigods.Engine.Conversation;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.conversations.Prompt;
 import org.bukkit.conversations.ValidatingPrompt;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Conversation.ConversationInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.General.DemigodsLocation;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureInfo;
 import com.censoredsoftware.Demigods.Engine.Utility.*;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class Prayer implements ConversationInfo
 {
 	// Define variables
 	private static org.bukkit.conversations.Conversation prayerConversation;
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new PrayerListener();
 	}
 
 	/**
 	 * Defines categories that can be used during prayer.
 	 */
 	public enum Menu
 	{
 		CONFIRM_CHARACTER(0, new ConfirmCharacter()), CREATE_CHARACTER(1, new CreateCharacter()), VIEW_CHARACTERS(2, new ViewCharacters()), VIEW_WARPS(3, new ViewWarps());
 
 		private Integer id;
 		private Category category;
 
 		private Menu(int id, Category category)
 		{
 			this.id = id;
 			this.category = category;
 		}
 
 		public Integer getId()
 		{
 			return this.id;
 		}
 
 		public Category getCategory()
 		{
 			return this.category;
 		}
 
 		public static Menu getFromId(int id)
 		{
 			for(Menu menu : Menu.values())
 			{
 				if(menu.getId().equals(id)) return menu;
 			}
 			return null;
 		}
 	}
 
 	public static org.bukkit.conversations.Conversation startPrayer(Player player)
 	{
 		// Grab the context Map
 		Map<Object, Object> conversationContext = Maps.newHashMap();
 		if(DataUtility.hasKeyTemp(player.getName(), "prayer_context")) conversationContext.putAll(((ConversationContext) DataUtility.getValueTemp(player.getName(), "prayer_context")).getAllSessionData());
 
 		// Build the conversation and begin
 		prayerConversation = Demigods.conversation.withEscapeSequence("/exit").withLocalEcho(false).withInitialSessionData(conversationContext).withFirstPrompt(new StartPrayer()).buildConversation(player);
 		prayerConversation.begin();
 
 		return prayerConversation;
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
 			MiscUtility.clearRawChat(player);
 
 			// Send Prayer menu
 			MiscUtility.clearRawChat(player);
 			player.sendRawMessage(ChatColor.AQUA + " -- Prayer Menu --------------------------------------");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + " While praying you are unable chat with players.");
 			player.sendRawMessage(ChatColor.GRAY + " You can return to the main menu at anytime by typing " + ChatColor.YELLOW + "menu" + ChatColor.GRAY + ".");
 			player.sendRawMessage(ChatColor.GRAY + " Walk away to stop praying.");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + " To begin, choose an option by entering its number in the chat:");
 			player.sendRawMessage(" ");
 
 			for(Menu menu : Menu.values())
 			{
 				if(menu.getCategory().canUse(context)) player.sendRawMessage(ChatColor.GRAY + "   [" + menu.getId() + ".] " + menu.getCategory().getChatName());
 			}
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			try
 			{
 				return Menu.getFromId(Integer.parseInt(message)) != null && Menu.getFromId(Integer.parseInt(message)).getCategory().canUse(context);
 			}
 			catch(Exception ignored)
 			{}
 			return false;
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			return Menu.getFromId(Integer.parseInt(message)).getCategory();
 		}
 	}
 
 	// Warps
 	static class ViewWarps extends ValidatingPrompt implements Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.LIGHT_PURPLE + "View Warps " + ChatColor.GRAY + "(& Invites)";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return PlayerWrapper.getPlayer((Player) context.getForWhom()).getCurrent() != null;
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			PlayerCharacter character = PlayerWrapper.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			MiscUtility.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + " " + UnicodeUtility.rightwardArrow() + " Viewing Warps & Invites -----------------------------");
 			player.sendRawMessage(" ");
 
 			if(character.hasWarps() || character.hasInvites())
 			{
 				player.sendRawMessage(ChatColor.LIGHT_PURPLE + "  Light purple" + ChatColor.GRAY + " represents the warp(s) at this location.");
 				player.sendRawMessage(" ");
 
 				for(Map.Entry<String, DemigodsLocation> entry : character.getWarps().entrySet())
 				{
 					player.sendRawMessage((player.getLocation().distance(entry.getValue().toLocation()) < 8 ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " (" + StringUtils.capitalize(entry.getValue().toLocation().getWorld().getName().toLowerCase()) + ": " + Math.round(entry.getValue().toLocation().getX()) + ", " + Math.round(entry.getValue().toLocation().getY()) + ", " + Math.round(entry.getValue().toLocation().getZ()) + ")");
 				}
 				for(Map.Entry<String, DemigodsLocation> entry : character.getInvites().entrySet())
 				{
 					player.sendRawMessage(ChatColor.GREEN + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " (" + StringUtils.capitalize(entry.getValue().toLocation().getWorld().getName().toLowerCase()) + ": " + Math.round(entry.getValue().toLocation().getX()) + ", " + Math.round(entry.getValue().toLocation().getY()) + ", " + Math.round(entry.getValue().toLocation().getZ()) + ") " + ChatColor.GREEN + "Invited by [ALLAN!!]"); // TODO: Invited by
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
 			if(context.getSessionData("warp_notifications") != null && !((List<TextUtility.Text>) context.getSessionData("warp_notifications")).isEmpty())
 			{
 				// Grab the notifications
 				List<TextUtility.Text> notifications = (List<TextUtility.Text>) context.getSessionData("warp_notifications");
 
 				player.sendRawMessage(" ");
 
 				// List them
 				for(TextUtility.Text notification : notifications)
 				{
 					player.sendRawMessage("  " + Demigods.text.getText(notification));
 					notifications.remove(notification);
 				}
 			}
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			// Define variables
 			PlayerCharacter character = PlayerWrapper.getPlayer((Player) context.getForWhom()).getCurrent();
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length >= 2 ? message.split(" ")[1] : null;
 			String arg2 = message.split(" ").length >= 3 ? message.split(" ")[2] : null;
 
			return message.equalsIgnoreCase("menu") || arg0.equalsIgnoreCase("new") && StringUtils.isAlphanumeric(arg1) && !character.getWarps().containsKey(arg1.toLowerCase()) || ((arg0.equalsIgnoreCase("warp") || arg0.equalsIgnoreCase("delete")) && (character.getWarps().containsKey(arg1.toLowerCase()) || character.getInvites().containsKey(arg1.toLowerCase())) || (arg0.equalsIgnoreCase("invite") && (PlayerCharacter.charExists(arg1) || PlayerWrapper.getPlayer(Bukkit.getOfflinePlayer(arg1)).getCurrent() != null) && character.getWarps().containsKey(arg2.toLowerCase())));
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			PlayerCharacter character = PlayerWrapper.getPlayer((Player) context.getForWhom()).getCurrent();
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length >= 2 ? message.split(" ")[1] : null;
 			String arg2 = message.split(" ").length >= 3 ? message.split(" ")[2] : null;
 
 			// Create and save the notification list
 			context.setSessionData("warp_notifications", Lists.newArrayList());
 			List<TextUtility.Text> notifications = (List<TextUtility.Text>) context.getSessionData("warp_notifications");
 
 			MiscUtility.clearRawChat(player);
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				// THEY WANT THE MENU!? SOCK IT TO 'EM!
 				return new StartPrayer();
 			}
 			if(arg0.equalsIgnoreCase("new"))
 			{
 				// Save notification
 				notifications.add(TextUtility.Text.NOTIFICATION_WARP_CREATED);
 
 				// Add the warp
 				character.addWarp(arg1, player.getLocation());
 
 				// Return to view warps
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("delete"))
 			{
 				// Save notification
 				notifications.add(TextUtility.Text.NOTIFICATION_WARP_DELETED);
 
 				// Remove the warp/invite
 				if(character.getWarps().containsKey(arg1.toLowerCase()))
 				{
 					character.removeWarp(arg1);
 				}
 				else if(character.getInvites().containsKey(arg1.toLowerCase()))
 				{
 					character.removeInvite(arg1);
 				}
 
 				// Return to view warps
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("invite"))
 			{
 				// Save notification
 				notifications.add(TextUtility.Text.NOTIFICATION_INVITE_SENT);
 
 				// Define variables
				PlayerCharacter invitee = PlayerCharacter.charExists(arg1) ? PlayerCharacter.getCharacterByName(arg1) : PlayerWrapper.getPlayer(Bukkit.getOfflinePlayer(arg1)).getCurrent();
 				Location warp = character.getWarps().get(arg2).toLocation();
 
 				// Add the invite
 				invitee.addInvite(character.getName(), warp);
 
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
 				PlayerWrapper.togglePrayingSilent(player, false);
 
 				// Teleport and message
 				if(character.getWarps().containsKey(arg1.toLowerCase()))
 				{
 					player.teleport(character.getWarps().get(arg1).toLocation());
 				}
 				else if(character.getInvites().containsKey(arg1.toLowerCase()))
 				{
 					player.teleport(character.getInvites().get(arg1.toLowerCase()).toLocation());
 					character.removeInvite(arg1.toLowerCase());
 				}
 				player.sendMessage(ChatColor.GRAY + "Teleported to " + ChatColor.LIGHT_PURPLE + StringUtils.capitalize(arg1.toLowerCase()) + ChatColor.GRAY + ".");
 			}
 			return null;
 		}
 	}
 
 	// Character viewing
 	static class ViewCharacters extends ValidatingPrompt implements Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.YELLOW + "View Characters";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return PlayerWrapper.getCharacters((Player) context.getForWhom()) != null && !PlayerWrapper.getCharacters((Player) context.getForWhom()).isEmpty();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			MiscUtility.clearRawChat(player);
 
 			player.sendRawMessage(ChatColor.YELLOW + " " + UnicodeUtility.rightwardArrow() + " Viewing Character ---------------------------------");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.LIGHT_PURPLE + "  Light purple" + ChatColor.GRAY + " represents your current character.");
 			player.sendRawMessage(" ");
 
 			for(PlayerCharacter character : PlayerWrapper.getCharacters(player))
 			{
 				if(!character.canUse()) continue;
 				player.sendRawMessage((character.isActive() ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + character.getName() + ChatColor.GRAY + " [" + character.getDeity().getInfo().getColor() + character.getDeity().getInfo().getName() + ChatColor.GRAY + " / Fav: " + MiscUtility.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") / Asc: " + ChatColor.GREEN + character.getMeta().getAscensions() + ChatColor.GRAY + "]");
 			}
 
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
 			return message.equalsIgnoreCase("menu") || PlayerWrapper.hasCharName((Player) context.getForWhom(), splitMsg[0]) && (splitMsg[1].equalsIgnoreCase("info") || splitMsg[1].equalsIgnoreCase("switch"));
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ")[1];
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				return new StartPrayer();
 			}
 			if(arg1.equalsIgnoreCase("info"))
 			{
 				context.setSessionData("viewing_character", arg0);
 				return new DetailedInfo();
 			}
 			else if(arg1.equalsIgnoreCase("switch"))
 			{
 				PlayerWrapper.getPlayer((Player) context.getForWhom()).switchCharacter(PlayerCharacter.getCharacterByName(arg0));
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
 				PlayerCharacter character = PlayerCharacter.getCharacterByName(context.getSessionData("viewing_character").toString());
 				String status = character.isActive() ? ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "(Current) " + ChatColor.RESET : ChatColor.RED + "" + ChatColor.ITALIC + "(Inactive) " + ChatColor.RESET;
 
 				// Clear chat
 				MiscUtility.clearRawChat(player);
 
 				// Send the player the info
 				player.sendRawMessage(ChatColor.YELLOW + " " + UnicodeUtility.rightwardArrow() + " Viewing Character ---------------------------------");
 				player.sendRawMessage(" ");
 				player.sendRawMessage("    " + status + ChatColor.YELLOW + character.getName() + ChatColor.GRAY + " > Allied to " + character.getDeity().getInfo().getColor() + character.getDeity() + ChatColor.GRAY + " of the " + ChatColor.GOLD + character.getAlliance() + "s");
 				player.sendRawMessage(ChatColor.GRAY + "  --------------------------------------------------");
 				player.sendRawMessage(ChatColor.GRAY + "    Health: " + ChatColor.WHITE + MiscUtility.getColor(character.getHealth(), 20) + character.getHealth() + ChatColor.GRAY + " (of " + ChatColor.GREEN + 20 + ChatColor.GRAY + ")" + ChatColor.GRAY + "  |  Hunger: " + ChatColor.WHITE + MiscUtility.getColor(character.getHunger(), 20) + character.getHunger() + ChatColor.GRAY + " (of " + ChatColor.GREEN + 20 + ChatColor.GRAY + ")" + ChatColor.GRAY + "  |  Exp: " + ChatColor.WHITE + (int) (Math.round(character.getExperience()))); // TODO: Exp isn't correct.
 				player.sendRawMessage(ChatColor.GRAY + "  --------------------------------------------------");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "    Favor: " + MiscUtility.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") " + ChatColor.YELLOW + "+5 every " + Demigods.config.getSettingInt("regeneration.favor") + " seconds"); // TODO: This should change with "perks" (assuming that we implement faster favor regeneration perks).
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "    Ascensions: " + ChatColor.GREEN + character.getMeta().getAscensions());
 				player.sendRawMessage(" ");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "back" + ChatColor.GRAY + " to return to your characters or type " + ChatColor.YELLOW + "switch");
 				player.sendRawMessage(ChatColor.GRAY + "  to change your current character to " + character.getDeity().getInfo().getColor() + character.getName() + ChatColor.GRAY + ".");
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.equalsIgnoreCase("back") || message.equalsIgnoreCase("switch");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.equalsIgnoreCase("back"))
 				{
 					return new ViewCharacters();
 				}
 				else if(message.equalsIgnoreCase("switch"))
 				{
 					PlayerWrapper.getPlayer((Player) context.getForWhom()).switchCharacter(PlayerCharacter.getCharacterByName(context.getSessionData("viewing_character").toString()));
 				}
 				return null;
 			}
 		}
 	}
 
 	// Character creation
 	static class CreateCharacter extends ValidatingPrompt implements Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.GREEN + "Create Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			// TODO: Add permissions support.
 			return true;
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			MiscUtility.clearRawChat((Player) context.getForWhom());
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
 				MiscUtility.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + " " + UnicodeUtility.rightwardArrow() + " Creating Character --------------------------------");
 				player.sendRawMessage(" ");
 
 				if(context.getSessionData("name_errors") == null)
 				{
 					// No errors, continue
 					player.sendRawMessage(ChatColor.AQUA + "  Enter a name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 				}
 				else
 				{
 					// Grab the errors
 					List<TextUtility.Text> errors = (List<TextUtility.Text>) context.getSessionData("name_errors");
 
 					// List the errors
 					for(TextUtility.Text error : errors)
 					{
 						player.sendRawMessage(ChatColor.RED + "  " + Demigods.text.getText(error));
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
 				Player player = (Player) context.getForWhom();
 
 				if(name.length() < 4 || name.length() > 14 || !StringUtils.isAlphanumeric(name) || MiscUtility.hasCapitalLetters(name, Demigods.config.getSettingInt("character.max_caps_in_name")) || PlayerWrapper.hasCharName(player, name))
 				{
 					// Create the list
 					List<TextUtility.Text> errors = Lists.newArrayList();
 
 					// Check the errors
 					if(name.length() < 4 || name.length() > 14)
 					{
 						errors.add(TextUtility.Text.ERROR_NAME_LENGTH);
 					}
 					if(!StringUtils.isAlphanumeric(name))
 					{
 						errors.add(TextUtility.Text.ERROR_ALPHA_NUMERIC);
 					}
 					if(MiscUtility.hasCapitalLetters(name, Demigods.config.getSettingInt("character.max_caps_in_name")))
 					{
 						errors.add(TextUtility.Text.ERROR_MAX_CAPS);
 					}
 					if(PlayerCharacter.charExists(name))
 					{
 						errors.add(TextUtility.Text.ERROR_CHAR_EXISTS);
 					}
 
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
 				MiscUtility.clearRawChat((Player) context.getForWhom());
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
 				if(message.contains("y")) return new ChooseDeity();
 				else
 				{
 					context.setSessionData("chosen_name", null);
 					return new ChooseName();
 				}
 			}
 		}
 
 		class ChooseDeity extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 
 				MiscUtility.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + " " + UnicodeUtility.rightwardArrow() + " Creating Character --------------------------------");
 				context.getForWhom().sendRawMessage(" ");
 
 				player.sendRawMessage(ChatColor.AQUA + "  Please choose a Deity: " + ChatColor.GRAY + "(Type in the name of the Deity)");
 
 				for(String alliance : Deity.getLoadedDeityAlliances())
 				{
 					for(Deity deity : Deity.getAllDeitiesInAlliance(alliance))
 					{
 						if(player.hasPermission(deity.getInfo().getPermission())) player.sendRawMessage(ChatColor.GRAY + "  " + UnicodeUtility.rightwardArrow() + " " + ChatColor.YELLOW + StringUtils.capitalize(deity.getInfo().getName()) + ChatColor.GRAY + " (" + alliance + ")");
 					}
 				}
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String deityName)
 			{
 				for(Deity deity : Demigods.getLoadedDeities())
 				{
 					if(deity.getInfo().getName().equalsIgnoreCase(deityName)) return true;
 				}
 				return false;
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
 				MiscUtility.clearRawChat((Player) context.getForWhom());
 				return ChatColor.GRAY + "Are you sure you want to use " + ChatColor.YELLOW + StringUtils.capitalize((String) context.getSessionData("chosen_deity")) + ChatColor.GRAY + "? (y/n)";
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
 					MiscUtility.clearRawChat(player);
 					player.sendRawMessage(ChatColor.AQUA + "  Before you can confirm your lineage with " + ChatColor.YELLOW + StringUtils.capitalize(chosenDeity) + ChatColor.AQUA + ",");
 					player.sendRawMessage(ChatColor.AQUA + "  you must first sacrifice the following items:");
 					player.sendRawMessage(" ");
 					for(Material item : Deity.getDeity(chosenDeity).getInfo().getClaimItems())
 					{
 						player.sendRawMessage(ChatColor.GRAY + "  " + UnicodeUtility.rightwardArrow() + " " + ChatColor.YELLOW + item.name());
 					}
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.GRAY + "  After you obtain these items, return to an Altar to");
 					player.sendRawMessage(ChatColor.GRAY + "  confirm your new character.");
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.AQUA + "  Your prayer has been disabled.");
 					player.sendRawMessage(" ");
 
 					// Save temporary data, end the conversation, and return
 					context.setSessionData("confirming_deity", true);
 					PlayerWrapper.togglePrayingSilent(player, false);
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
 
 	// Character confirmation
 	static class ConfirmCharacter extends ValidatingPrompt implements Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.YELLOW + "Confirm Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return context.getSessionData("confirming_deity") != null && Boolean.parseBoolean(context.getSessionData("confirming_deity").toString());
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			String chosenDeity = (String) context.getSessionData("chosen_deity");
 
 			// Clear chat
 			MiscUtility.clearRawChat(player);
 
 			// Ask them if they have the items
 			player.sendRawMessage(ChatColor.GREEN + " " + UnicodeUtility.rightwardArrow() + " Confirming Character -------------------------------");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.AQUA + "  Do you have the following items in your inventory?" + ChatColor.GRAY + " (y/n)");
 			player.sendRawMessage(" ");
 			for(Material item : Deity.getDeity(chosenDeity).getInfo().getClaimItems())
 			{
 				player.sendRawMessage(ChatColor.GRAY + "  " + UnicodeUtility.rightwardArrow() + " " + ChatColor.YELLOW + item.name());
 			}
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
 			Inventory inv = Bukkit.getServer().createInventory(player, 27, "Place Your Tributes Here");
 			player.openInventory(inv);
 
 			return null;
 		}
 	}
 }
 
 class PrayerListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGH)
 	public void prayerInteract(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 		// Define variables
 		Player player = event.getPlayer();
 
 		// First we check if the player is clicking a prayer block
 		if(StructureUtility.isCenterBlockWithFlag(event.getClickedBlock().getLocation(), StructureInfo.Flag.PRAYER_LOCATION))
 		{
 			// TODO: Update this stuff with the language system
 			if(!PlayerWrapper.isPraying(player))
 			{
 				if(Demigods.config.getSettingBoolean("zones.use_dynamic_pvp_zones") && ZoneUtility.canTarget(player))
 				{
 					player.sendMessage(ChatColor.GRAY + "You cannot pray when PvP is still possible.");
 					player.sendMessage(ChatColor.GRAY + "Wait a few moments and then try again when it's safe.");
 					event.setCancelled(true);
 					return;
 				}
 
 				// Toggle praying
 				PlayerWrapper.togglePraying(player, true);
 
 				// Tell nearby players that the user is praying
 				for(Entity entity : player.getNearbyEntities(20, 20, 20))
 				{
 					if(entity instanceof Player) ((Player) entity).sendMessage(ChatColor.AQUA + player.getName() + " has knelt to begin prayer.");
 				}
 			}
 			else if(PlayerWrapper.isPraying(player))
 			{
 				// Toggle prayer to false
 				PlayerWrapper.togglePraying(player, false);
 			}
 
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void createCharacter(InventoryCloseEvent event)
 	{
 		try
 		{
 			if(!(event.getPlayer() instanceof Player)) return;
 			Player player = (Player) event.getPlayer();
 
 			// If it isn't a confirmation chest then exit
 			if(!event.getInventory().getName().contains("Place Your Tributes Here")) return;
 
 			// Exit if this isn't for character creation
 			if(!PlayerWrapper.isPraying(player)) return;
 
 			// Define variables
 			ConversationContext prayerContext = PlayerWrapper.getPrayerContext(player);
 			String chosenName = (String) prayerContext.getSessionData("chosen_name");
 			String chosenDeity = (String) prayerContext.getSessionData("chosen_deity");
 			String deityAlliance = StringUtils.capitalize(Deity.getDeity(chosenDeity).getInfo().getAlliance());
 
 			// Check the chest items
 			int items = 0;
 			int neededItems = Deity.getDeity(chosenDeity).getInfo().getClaimItems().size();
 
 			for(ItemStack ii : event.getInventory().getContents())
 			{
 				if(ii != null)
 				{
 					for(Material item : Deity.getDeity(chosenDeity).getInfo().getClaimItems())
 					{
 						if(ii.getType().equals(item))
 						{
 							items++;
 						}
 					}
 				}
 			}
 
 			// Stop their praying
 			PlayerWrapper.togglePrayingSilent(player, false);
 
 			// Clear chat and send update
 			MiscUtility.clearChat(player);
 			player.sendMessage(ChatColor.YELLOW + "The " + deityAlliance + "s are pondering your offerings...");
 
 			if(neededItems == items)
 			{
 				// Accepted, finish everything up!
 				PlayerCharacter.create(player, chosenDeity, chosenName, true);
 
 				// Clear the prayer session
 				PlayerWrapper.clearPrayerSession(player);
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + "You have been denied entry into the lineage of " + chosenDeity.toUpperCase() + "!");
 			}
 
 			// Clear the confirmation case
 			event.getInventory().clear();
 		}
 		catch(Exception e)
 		{
 			// Print error for debugging
 			e.printStackTrace();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	private void onPlayerMove(PlayerMoveEvent event)
 	{
 		// Define variables
 		Player player = event.getPlayer();
 
 		if(!PlayerWrapper.isPraying(player)) return;
 
 		if(event.getTo().distance((Location) DataUtility.getValueTemp(player.getName(), "prayer_location")) >= Demigods.config.getSettingInt("zones.prayer_radius"))
 		{
 			PlayerWrapper.togglePraying(player, false);
 		}
 	}
 }
 
 // Can't touch this. Naaaaaa na-na-na.. Ba-dum, ba-dum.
 interface Category extends Prompt
 {
 	public String getChatName();
 
 	public boolean canUse(ConversationContext context);
 }
