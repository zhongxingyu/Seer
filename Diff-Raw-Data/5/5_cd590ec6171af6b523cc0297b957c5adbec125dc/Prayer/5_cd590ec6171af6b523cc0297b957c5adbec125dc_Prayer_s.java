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
 		CONFIRM_DEITY(0, new ConfirmDeity()), CREATE_CHARACTER(1, new CreateCharacter()), VIEW_CHARACTERS(2, new ViewCharacters());
 
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
 
 	static class StartPrayer extends ValidatingPrompt
 	{
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			PlayerCharacter currentCharacter = PlayerWrapper.getPlayer(player).getCurrent();
 
 			// Clear chat
 			MiscUtility.clearRawChat(player);
 
 			// Send Prayer menu
 			MiscUtility.clearRawChat(player);
 			player.sendRawMessage(ChatColor.AQUA + " -- Prayer Menu --------------------------------------");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + " While praying you are unable chat with players.");
 			player.sendRawMessage(ChatColor.GRAY + " You can return to the main menu at anytime by typing \"menu\".");
 			player.sendRawMessage(ChatColor.GRAY + " Walk away to stop praying.");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + " To begin, choose an option by entering its number in the chat:");
 			player.sendRawMessage(" ");
 
 			for(Menu menu : Menu.values())
 			{
 				if(menu.getCategory().canUse(context, player)) player.sendRawMessage(ChatColor.GRAY + "   [" + menu.getId() + ".] " + menu.getCategory().getChatName());
 			}
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			try
 			{
 				return Menu.getFromId(Integer.parseInt(message)) != null;
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
 
 	// Character viewing
 	static class ViewCharacters extends ValidatingPrompt implements Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.YELLOW + "View Characters";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context, Player player)
 		{
 			return PlayerWrapper.getCharacters(player) != null;
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
 				ChatColor color = ChatColor.GRAY;
 				if(character.isActive()) color = ChatColor.LIGHT_PURPLE;
 				player.sendRawMessage(color + "  " + character.getName() + ChatColor.GRAY + " [" + character.getDeity().getInfo().getColor() + character.getDeity().getInfo().getName() + ChatColor.GRAY + " / Fav: " + MiscUtility.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") / Asc: " + ChatColor.GREEN + character.getMeta().getAscensions() + ChatColor.GRAY + "]");
 			}
 
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Type" + ChatColor.YELLOW + " <character name> info" + ChatColor.GRAY + " for detailed information."); // TODO
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Type" + ChatColor.YELLOW + " switch to <character name> " + ChatColor.GRAY + "to change your current");
 			player.sendRawMessage(ChatColor.GRAY + "  character.");
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return false;
 		}
 
 		@Override
 		protected ValidatingPrompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			return null;
 		}
 	}
 
 	// Character creation
 	static class CreateCharacter extends ValidatingPrompt implements Category
 	{
 		public enum Error
 		{
 			NAME_LENGTH("Your name should be between 4 and 14 characters."), ALPHA_NUMERIC("Only alpha-numeric characters are allowed."), MAX_CAPS("Please use no more than " + Demigods.config.getSettingInt("character.max_caps_in_name") + " capital letters."), CHAR_EXISTS("You already have a character with that name.");
 
 			private String message;
 
 			private Error(String message)
 			{
 				this.message = message;
 			}
 
 			public String getMessage()
 			{
 				return this.message;
 			}
 		}
 
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.GREEN + "Create Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context, Player player)
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
 					// Grab the errors.
 					List<Error> errors = (List<Error>) context.getSessionData("name_errors");
 
 					// List the errors
 					for(Error error : errors)
 					{
 						player.sendRawMessage(ChatColor.RED + "  " + error.getMessage());
 					}
 
 					// Ask for a new name
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
 					List<Error> errors = Lists.newArrayList();
 
 					// Check the errors
 					if(name.length() < 4 || name.length() > 14)
 					{
 						errors.add(Error.NAME_LENGTH);
 					}
 					if(!StringUtils.isAlphanumeric(name))
 					{
 						errors.add(Error.ALPHA_NUMERIC);
 					}
 					if(MiscUtility.hasCapitalLetters(name, Demigods.config.getSettingInt("character.max_caps_in_name")))
 					{
 						errors.add(Error.MAX_CAPS);
 					}
 					if(PlayerWrapper.hasCharName(player, name))
 					{
 						errors.add(Error.CHAR_EXISTS);
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
 						if(player.hasPermission(deity.getInfo().getPermission())) player.sendRawMessage(ChatColor.GRAY + "  " + UnicodeUtility.rightwardArrow() + " " + ChatColor.YELLOW + MiscUtility.capitalize(deity.getInfo().getName()) + ChatColor.GRAY + " (" + alliance + ")");
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
 				return ChatColor.GRAY + "Are you sure you want to use " + ChatColor.YELLOW + MiscUtility.capitalize((String) context.getSessionData("chosen_deity")) + ChatColor.GRAY + "? (y/n)";
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
 					player.sendRawMessage(ChatColor.AQUA + "  Before you can confirm your lineage with " + ChatColor.YELLOW + MiscUtility.capitalize(chosenDeity) + ChatColor.AQUA + ",");
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
 	static class ConfirmDeity extends ValidatingPrompt implements Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.YELLOW + "Confirm Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context, Player player)
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
 			String deityAlliance = MiscUtility.capitalize(Deity.getDeity(chosenDeity).getInfo().getAlliance());
 
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
				// They were accepted, finish everything up!
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
 
		if(event.getTo().distance((Location) DataUtility.getValueTemp(player.getName(), "praying_location")) >= Demigods.config.getSettingInt("zones.prayer_radius"))
 		{
 			PlayerWrapper.togglePraying(player, false);
 		}
 	}
 }
 
 // Can't touch this. Naaaaaa na-na-na.. Ba-dum, ba-dum.
 interface Category extends Prompt
 {
 	public String getChatName();
 
 	public boolean canUse(ConversationContext context, Player player);
 }
