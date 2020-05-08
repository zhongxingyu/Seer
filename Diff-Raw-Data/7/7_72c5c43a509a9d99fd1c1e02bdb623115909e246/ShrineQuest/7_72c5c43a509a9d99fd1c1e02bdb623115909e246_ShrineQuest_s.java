 package com.censoredsoftware.Demigods.Demo.Quest.Passive;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.censoredsoftware.Demigods.Engine.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Quest.Quest;
 import com.censoredsoftware.Demigods.Engine.Quest.Task;
 import com.censoredsoftware.Demigods.Engine.Quest.TaskInfo;
 import com.censoredsoftware.Demigods.Engine.Structure.Shrine;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.ValueUtility;
 
 public class ShrineQuest extends Quest
 {
 	private static String name = "Shrine", permission = "demigods.shrine";
 	private static Type type = Type.PASSIVE;
 
 	private static List<String> about = new ArrayList<String>()
 	{
 		{
 			add("Shrines are an integral part of Demigods.");
 			add("Right-click a gold block with a book to create one.");
 			add("With your Shrine you can tribute to your Deity!");
 		}
 	}, accepted = new ArrayList<String>()
 	{
 		{
 			add("Accepted.");
 		}
 	}, complete = new ArrayList<String>()
 	{
 		{
 			add("Complete.");
 		}
 	}, failed = new ArrayList<String>()
 	{
 		{
 			add("Failed.");
 		}
 	};
 
 	private static List<Task> tasks = new ArrayList<Task>()
 	{
 		{
 			add(new Tribute(name, permission, about, accepted, complete, failed, type));
 		}
 	};
 
 	public ShrineQuest()
 	{
 		super(name, permission, about, accepted, complete, failed, type, tasks);
 	}
 }
 
 class Tribute extends Task
 {
 	// Define required task variables
 	private static String name = "Tributing";
 	private static int order = 0;
 	private static double reward = 0.0, penalty = 0.0;
 
 	private static Listener listener = new Listener()
 	{
 		@EventHandler(priority = EventPriority.HIGHEST)
 		public void onShrineInteract(PlayerInteractEvent event)
 		{
 			// Return if the player is mortal
 			if(!TrackedPlayer.isImmortal(event.getPlayer())) return;
 			if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 			// Define variables
 			Location location = event.getClickedBlock().getLocation();
 			Player player = event.getPlayer();
 			PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 			if(Shrine.isShrine(location))
 			{
 				// Cancel the interaction
 				event.setCancelled(true);
 
 				// Define the shrine
 				Shrine shrine = Shrine.getShrine(location);
 
 				// Return if they aren't clicking the gold block
 				if(!event.getClickedBlock().getType().equals(Material.GOLD_BLOCK)) return;
 
 				// Return if the player is mortal
 				if(character == null || !character.isImmortal())
 				{
 					player.sendMessage(ChatColor.RED + "You must be immortal to use that!");
 					return;
 				}
 
 				if(character.getDeity().equals(shrine.getDeity()))
 				{
 					useShrine(character, shrine);
 				}
 				else
 				{
 					player.sendMessage(ChatColor.YELLOW + "You must be allied to " + shrine.getDeity().getInfo().getName() + " in order to tribute here.");
 				}
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void onPlayerTribute(InventoryCloseEvent event)
 		{
 			// Return if it's not a player
 			if(!(event.getPlayer() instanceof Player)) return;
 
 			// Define player and character
 			Player player = (Player) event.getPlayer();
 			PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 			// Make sure they have a character and are immortal
 			if(character == null || !character.isImmortal()) return;
 
 			// If it isn't a tribute chest then break the method
 			if(!event.getInventory().getName().contains("Shrine") || !Shrine.isShrine(player.getTargetBlock(null, 10).getLocation())) return;
 
 			// Get the creator of the shrine
 			PlayerCharacter shrineOwner = PlayerCharacter.getCharacterByName(DemigodsData.getValueTemp(player.getName(), character.getName()).toString());
 
 			// Calculate the tribute value
 			int tributeValue = 0, items = 0;
 			for(ItemStack item : event.getInventory().getContents())
 			{
 				if(item != null)
 				{
 					tributeValue += ValueUtility.getTributeValue(item);
 					items += item.getAmount();
 				}
 			}
 
 			// Return if it's empty
 			if(items == 0) return;
 
 			// Handle the multiplier
 			tributeValue *= Demigods.config.getSettingDouble("multipliers.favor");
 
 			// Get the current favor for comparison
 			int favorBefore = character.getMeta().getMaxFavor();
 
 			// Update the character's favor
 			character.getMeta().addFavor(tributeValue / 3);
 			character.getMeta().addMaxFavor(tributeValue);
 
 			// Handle messaging and Shrine owner updating
			if(character.getMeta().getMaxFavor() > favorBefore)
 			{
 				// Message the tributer
 				player.sendMessage(ChatColor.YELLOW + character.getDeity().getInfo().getName() + " is pleased!");
 				player.sendMessage(ChatColor.GRAY + "Your favor cap has increased to " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ".");
 
 				// Update the shrine owner's devotion and let them know
 				OfflinePlayer shrineOwnerPlayer = shrineOwner.getOfflinePlayer();
 
 				if(!player.getName().equals(shrineOwnerPlayer.getName()))
 				{
 					// Give them some of the blessings
 					shrineOwner.getMeta().addMaxFavor(tributeValue / 5);
 
 					// Message them
 					if(shrineOwnerPlayer.isOnline() && TrackedPlayer.getTracked(shrineOwner.getOfflinePlayer()).getCurrent().getId().equals(shrineOwner.getId()))
 					{
 						((Player) shrineOwnerPlayer).sendMessage(ChatColor.YELLOW + "Someone just tributed at your shrine!");
 						((Player) shrineOwnerPlayer).sendMessage(ChatColor.GRAY + "Your favor cap has increased to " + ChatColor.GREEN + shrineOwner.getMeta().getMaxFavor() + ChatColor.GRAY + "!");
 					}
 				}
 			}
 			else
 			{
 				// If they aren't good enough let them know
 				if(items > 0) player.sendMessage(ChatColor.RED + "Your tributes were insufficient for " + character.getDeity().getInfo().getName() + "'s blessings.");
 			}
 
 			// Clear the tribute case
 			event.getInventory().clear();
 		}
 	};
 
 	private static void useShrine(PlayerCharacter character, Shrine shrine)
 	{
 		Player player = character.getOfflinePlayer().getPlayer();
 		String shrineOwner = shrine.getCharacter().getName();
 		Deity shrineDeity = shrine.getDeity();
 
 		// Open the tribute inventory
 		Inventory ii = Bukkit.getServer().createInventory(player, 27, "Shrine of " + shrineDeity);
 		player.openInventory(ii);
 		DemigodsData.saveTemp(player.getName(), character.getName(), shrineOwner);
 	}
 
 	public Tribute(String quest, String permission, List<String> about, List<String> accepted, List<String> complete, List<String> failed, Quest.Type type)
 	{
 		super(new TaskInfo(name, quest, permission, order, reward, penalty, about, accepted, complete, failed, type, TaskInfo.Subtype.TECHNICAL), listener);
 	}
 }
