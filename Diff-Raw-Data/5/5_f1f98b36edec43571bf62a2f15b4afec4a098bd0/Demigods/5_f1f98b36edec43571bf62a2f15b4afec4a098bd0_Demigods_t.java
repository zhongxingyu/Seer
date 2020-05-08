 package com.censoredsoftware.Demigods.Engine;
 
 import java.util.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.plugin.Plugin;
 
 import com.bekvon.bukkit.residence.Residence;
 import com.censoredsoftware.Demigods.DemigodsPlugin;
 import com.censoredsoftware.Demigods.Engine.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Event.Character.CharacterBetrayCharacterEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Character.CharacterKillCharacterEvent;
 import com.censoredsoftware.Demigods.Engine.Language.Translation;
 import com.censoredsoftware.Demigods.Engine.Listener.*;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Quest.Quest;
 import com.censoredsoftware.Demigods.Engine.Quest.Task;
 import com.censoredsoftware.Demigods.Engine.Structure.Altar;
 import com.censoredsoftware.Demigods.Engine.Structure.StructureGenerator;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBlock;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.AdminUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.UnicodeUtility;
 import com.censoredsoftware.Modules.*;
 import com.massivecraft.factions.P;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class Demigods
 {
 	// Public Static Access
 	public static DemigodsPlugin plugin;
 
 	// Public Modules
 	public static ConfigModule config;
 	public static MessageModule message;
 	public static PermissionModule permission;
 
 	// Public Dependency Plugins
 	public static WorldGuardPlugin worldguard;
 	public static P factions;
 	public static Residence residence;
 
 	// Protected Modules
 	protected static BukkitUpdateModule update;
 	protected static LatestTweetModule notice;
 
 	// The Game Data
 	protected static Deque<Deity> deities;
 	protected static Deque<Quest> quests;
 
 	// The Engline Default Text
 	public static Translation text;
 
 	public interface ListedDeity
 	{
 		public Deity getDeity();
 	}
 
 	public interface ListedQuest
 	{
 		public Quest getQuest();
 	}
 
 	public Demigods(DemigodsPlugin instance, final ListedDeity[] deities, final ListedQuest[] quests)
 	{
 		// Allow static access.
 		plugin = instance;
 
 		// Setup public modules.
 		config = new ConfigModule(instance, true);
 		message = new MessageModule(instance, config.getSettingBoolean("misc.tag_messages"));
 		permission = new PermissionModule();
 
 		// Setup protected modules.
 		update = new BukkitUpdateModule(instance, "http://dev.bukkit.org/server-mods/demigods/files.rss", "/dg update", "demigods.update", config.getSettingBoolean("update.auto"), config.getSettingBoolean("update.notify"), 10);
 
 		// Define the game data.
 		Demigods.deities = new ArrayDeque<Deity>()
 		{
 			{
 				for(ListedDeity deity : deities)
 				{
 					add(deity.getDeity());
 				}
 			}
 		};
 		Demigods.quests = new ArrayDeque<Quest>()
 		{
 			{
 				for(ListedQuest quest : quests)
 				{
 					add(quest.getQuest());
 				}
 			}
 		};
 
 		Demigods.text = getTranslation();
 
 		// Initialize soft data.
 		new DemigodsData(instance);
 
 		// Setup protected modules that require data.
 		notice = LatestTweetModule.recreate(instance, "DemigodsRPG", "/dg twitter", "demigods.twitter", config.getSettingBoolean("twitter.notify"));
 
 		// Finish loading the demigods based on the game data.
 		loadDepends(instance);
 		loadListeners(instance);
 		loadCommands(instance);
 
 		// Finally, regenerate structures
 		TrackedBlock.regenerateStructures();
 	}
 
 	/**
 	 * Get the translation involved.
 	 * 
 	 * @return The translation.
 	 */
 	public Translation getTranslation()
 	{
 		// Default to EnglishCharNames
 		return new DemigodsText.Engrish();
 	}
 
 	protected static void loadListeners(DemigodsPlugin instance)
 	{
 		// Engine
 		instance.getServer().getPluginManager().registerEvents(new AbilityListener(), instance);
 		// TODO: instance.getServer().getPluginManager().registerEvents(new BattleListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new BlockListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new CharacterListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new ChatListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new ChunkListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new CommandListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new DebugListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new EntityListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new PlayerListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new EventFactory(), instance);
 
 		// Deities
 		for(Deity deity : getLoadedDeities())
 		{
 			if(deity.getAbilities() == null) continue;
 			for(Ability ability : deity.getAbilities())
 			{
 				instance.getServer().getPluginManager().registerEvents(ability.getListener(), instance);
 			}
 		}
 
 		// Quests
 		for(Quest quest : getLoadedQuests())
 		{
 			if(quest.getTasks() == null) continue;
 			for(Task task : quest.getTasks())
 			{
 				instance.getServer().getPluginManager().registerEvents(task.getListener(), instance);
 			}
 		}
 	}
 
 	protected static void loadCommands(DemigodsPlugin instance)
 	{
 		// Define Main CommandExecutor
 		Commands ce = new Commands();
 
 		// Define General Commands
 		instance.getCommand("dg").setExecutor(ce);
 		instance.getCommand("check").setExecutor(ce);
 		instance.getCommand("owner").setExecutor(ce);
 		instance.getCommand("removechar").setExecutor(ce);
 		instance.getCommand("viewmaps").setExecutor(ce);
 		instance.getCommand("test1").setExecutor(ce);
 	}
 
 	protected static void loadDepends(DemigodsPlugin instance)
 	{
 		// WorldGuard
 		Plugin depend = instance.getServer().getPluginManager().getPlugin("WorldGuard");
 		if(depend instanceof WorldGuardPlugin) worldguard = (WorldGuardPlugin) depend;
 
 		// Factions
 		depend = instance.getServer().getPluginManager().getPlugin("Factions");
 		if(depend instanceof P) factions = (P) depend;
 
 		// Residence
 		depend = instance.getServer().getPluginManager().getPlugin("Residence");
 		if(depend instanceof Residence) residence = (Residence) depend;
 	}
 
 	public static Deque<Deity> getLoadedDeities()
 	{
 		return Demigods.deities;
 	}
 
 	public static Deque<Quest> getLoadedQuests()
 	{
 		return Demigods.quests;
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 }
 
 class EventFactory implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onEntityDeath(EntityDeathEvent event)
 	{
 		Entity entity = event.getEntity();
 		if(entity instanceof Player)
 		{
 			Player player = (Player) entity;
 			PlayerCharacter playerChar = TrackedPlayer.getTracked(player).getCurrent();
 
 			// if(playerChar != null) // TODO Killstreak in a new way.
 			// {
 			// if(playerChar.getKillstreak() > 3) Demigods.message.broadcast(ChatColor.YELLOW + playerChar.getName() + ChatColor.GRAY + "'s killstreak has ended.");
 			// playerChar.setKillstreak(0);
 			// }
 
 			EntityDamageEvent damageEvent = player.getLastDamageCause();
 
 			if(damageEvent instanceof EntityDamageByEntityEvent)
 			{
 				EntityDamageByEntityEvent damageByEvent = (EntityDamageByEntityEvent) damageEvent;
 				Entity damager = damageByEvent.getDamager();
 
 				if(damager instanceof Player)
 				{
 					Player attacker = (Player) damager;
 					PlayerCharacter attackChar = TrackedPlayer.getTracked(attacker).getCurrent();
 					if(attackChar != null && playerChar != null && PlayerCharacter.areAllied(attackChar, playerChar)) Bukkit.getServer().getPluginManager().callEvent(new CharacterBetrayCharacterEvent(attackChar, playerChar, TrackedPlayer.getCurrentAlliance(player)));
 					else Bukkit.getServer().getPluginManager().callEvent(new CharacterKillCharacterEvent(attackChar, playerChar));
 
 					if(attackChar != null)
 					{
 						// Killstreak
 						// int killstreak = attackChar.getKillstreak();
 						// attackChar.setKillstreak(killstreak + 1);
 						// if(attackChar.getKillstreak() > 2)
 						// {
 						// Demigods.message.callEvent(new CharacterKillstreakEvent(attackChar, playerChar, killstreak + 1));
 						// }
 
 						// TODO Dominating
 					}
 				}
 			}
 		}
 	}
 }
 
 class Commands implements CommandExecutor
 {
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if(command.getName().equalsIgnoreCase("dg")) return dg(sender, args);
 		else if(command.getName().equalsIgnoreCase("check")) return check(sender, args);
 		else if(command.getName().equalsIgnoreCase("owner")) return owner(sender, args);
 
 		// TESTING ONLY
 		else if(command.getName().equalsIgnoreCase("removechar")) return removeChar(sender, args);
 		else if(command.getName().equalsIgnoreCase("test1")) return test1(sender, args);
 
 		// Debugging
 		else if(command.getName().equalsIgnoreCase("viewmaps")) return viewMaps(sender);
 
 		return false;
 	}
 
 	private static boolean test1(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		player.sendMessage("Setting blocks...");
 
 		// TEST STRUCTURE
 
 		Location target = player.getTargetBlock(null, 10).getLocation();
 		StructureGenerator.GeneratorSchematic sponge = new StructureGenerator.GeneratorSchematic(target, 0, 0, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SPONGE));
 			}
 		});
 		StructureGenerator.GeneratorSchematic bottomRight = new StructureGenerator.GeneratorSchematic(target, 0, 0, -1, 1, 3, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic bottomLeft = new StructureGenerator.GeneratorSchematic(target, 0, 0, 1, 1, 3, 2, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic bottomFront = new StructureGenerator.GeneratorSchematic(target, 1, 0, 0, 2, 3, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic bottomBack = new StructureGenerator.GeneratorSchematic(target, -1, 0, 0, 0, 3, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic piston = new StructureGenerator.GeneratorSchematic(target, 0, 4, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.PISTON_STICKY_BASE));
 			}
 		});
 		StructureGenerator.GeneratorSchematic redstoneBlock = new StructureGenerator.GeneratorSchematic(target, 0, 3, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.REDSTONE_BLOCK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic lanternRight = new StructureGenerator.GeneratorSchematic(target, 0, 3, -1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.REDSTONE_LAMP_OFF));
 			}
 		});
 		StructureGenerator.GeneratorSchematic lanternLeft = new StructureGenerator.GeneratorSchematic(target, 0, 3, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.REDSTONE_LAMP_OFF));
 			}
 		});
 		StructureGenerator.GeneratorSchematic lanternFront = new StructureGenerator.GeneratorSchematic(target, 1, 3, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.REDSTONE_LAMP_OFF));
 			}
 		});
 		StructureGenerator.GeneratorSchematic lanternBack = new StructureGenerator.GeneratorSchematic(target, -1, 3, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.REDSTONE_LAMP_OFF));
 			}
 		});
 		StructureGenerator.GeneratorSchematic topRight = new StructureGenerator.GeneratorSchematic(target, 0, 4, -1, 1, 6, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic topLeft = new StructureGenerator.GeneratorSchematic(target, 0, 4, 1, 1, 6, 2, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic topFront = new StructureGenerator.GeneratorSchematic(target, 1, 4, 0, 2, 6, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic topBack = new StructureGenerator.GeneratorSchematic(target, -1, 4, 0, 0, 6, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.SMOOTH_BRICK));
 			}
 		});
 		StructureGenerator.GeneratorSchematic lightSensor = new StructureGenerator.GeneratorSchematic(target, 0, 5, 0, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.DAYLIGHT_DETECTOR));
 			}
 		});
 		StructureGenerator.GeneratorSchematic vineRight = new StructureGenerator.GeneratorSchematic(target, -1, 5, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
				add(new StructureGenerator.BlockData(Material.VINE, (byte) 1, 100));
 				add(new StructureGenerator.BlockData(Material.AIR, (byte) 0, 0));
 			}
 		});
 		StructureGenerator.GeneratorSchematic vineLeft = new StructureGenerator.GeneratorSchematic(target, 1, 5, -1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
				add(new StructureGenerator.BlockData(Material.VINE, (byte) 4, 100));
 				add(new StructureGenerator.BlockData(Material.AIR, (byte) 0, 0));
 			}
 		});
 		StructureGenerator.GeneratorSchematic vineFront = new StructureGenerator.GeneratorSchematic(target, 1, 5, 1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.VINE, (byte) 1, 100));
 				add(new StructureGenerator.BlockData(Material.AIR, (byte) 0, 0));
 			}
 		});
 		StructureGenerator.GeneratorSchematic vineBack = new StructureGenerator.GeneratorSchematic(target, -1, 5, -1, new HashSet<StructureGenerator.BlockData>()
 		{
 			{
 				add(new StructureGenerator.BlockData(Material.VINE, (byte) 4, 100));
 				add(new StructureGenerator.BlockData(Material.AIR, (byte) 0, 0));
 			}
 		});
 
 		sponge.generate();
 		bottomRight.generate();
 		bottomLeft.generate();
 		bottomFront.generate();
 		bottomBack.generate();
 		piston.generate();
 		redstoneBlock.generate();
 		lanternRight.generate();
 		lanternLeft.generate();
 		lanternFront.generate();
 		lanternBack.generate();
 		topRight.generate();
 		topLeft.generate();
 		topFront.generate();
 		topBack.generate();
 		lightSensor.generate();
 		vineRight.generate();
 		vineLeft.generate();
 		vineFront.generate();
 		vineBack.generate();
 
 		// TEST STRUCTURE
 
 		player.sendMessage("Blocks set!");
 
 		return true;
 	}
 
 	/*
 	 * Command: "dg"
 	 */
 	private static boolean dg(CommandSender sender, String[] args)
 	{
 		if(args.length > 0)
 		{
 			dg_extended(sender, args);
 			return true;
 		}
 
 		// Define Player
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 
 		// Check Permissions
 		if(!Demigods.permission.hasPermissionOrOP(player, "demigods.basic")) return Demigods.message.noPermission(player);
 
 		Demigods.message.tagged(sender, "Documentation");
 		for(String alliance : Deity.getLoadedDeityAlliances())
 		{
 			sender.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase());
 		}
 		sender.sendMessage(ChatColor.GRAY + " /dg info");
 		sender.sendMessage(ChatColor.GRAY + " /dg commands");
 		if(Demigods.permission.hasPermissionOrOP(player, "demigods.admin")) sender.sendMessage(ChatColor.RED + " /dg admin");
 		sender.sendMessage(" ");
 		sender.sendMessage(ChatColor.WHITE + " Use " + ChatColor.YELLOW + "/check" + ChatColor.WHITE + " to see your player information.");
 		return true;
 	}
 
 	/*
 	 * Command: "dg_extended"
 	 */
 	@SuppressWarnings("unchecked")
 	private static boolean dg_extended(CommandSender sender, String[] args)
 	{
 		// Define Player
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 
 		// Define args
 		String category = args[0];
 		String option1 = null, option2 = null, option3 = null, option4 = null;
 		if(args.length >= 2) option1 = args[1];
 		if(args.length >= 3) option2 = args[2];
 		if(args.length >= 4) option3 = args[3];
 		if(args.length >= 5) option4 = args[4];
 
 		// Check Permissions
 		if(!Demigods.permission.hasPermissionOrOP(player, "demigods.basic")) return Demigods.message.noPermission(player);
 
 		if(category.equalsIgnoreCase("admin"))
 		{
 			dg_admin(sender, option1, option2, option3, option4);
 		}
 		else if(category.equalsIgnoreCase("commands"))
 		{
 			Demigods.message.tagged(sender, "Command Directory");
 			sender.sendMessage(ChatColor.GRAY + " There's nothing here...");
 		}
 		else if(category.equalsIgnoreCase("info"))
 		{
 			if(option1 == null)
 			{
 				Demigods.message.tagged(sender, "Information Directory");
 				sender.sendMessage(ChatColor.GRAY + " /dg info characters");
 				sender.sendMessage(ChatColor.GRAY + " /dg info shrines");
 				sender.sendMessage(ChatColor.GRAY + " /dg info tributes");
 				sender.sendMessage(ChatColor.GRAY + " /dg info players");
 				sender.sendMessage(ChatColor.GRAY + " /dg info pvp");
 				sender.sendMessage(ChatColor.GRAY + " /dg info stats");
 				sender.sendMessage(ChatColor.GRAY + " /dg info rankings");
 				sender.sendMessage(ChatColor.GRAY + " /dg info demigods");
 			}
 			else if(option1.equalsIgnoreCase("demigods"))
 			{
 				Demigods.message.tagged(sender, "About the Plugin");
 				sender.sendMessage(ChatColor.WHITE + " Not to be confused with other RPG plugins that focus on skills and classes alone, " + ChatColor.GREEN + "Demigods" + ChatColor.WHITE + " adds culture and conflict that will keep players coming back even after they've maxed out their levels and found all of the diamonds in a 50km radius.");
 				sender.sendMessage(" ");
 				sender.sendMessage(ChatColor.GREEN + " Demigods" + ChatColor.WHITE + " is unique in its system of rewarding players for both adventuring (tributes) and conquering (PvP) with a wide array of fun and usefull skills.");
 				sender.sendMessage(" ");
 				sender.sendMessage(ChatColor.WHITE + " Re-enact mythological battles and rise from a PlayerCharacter to a full-fledged Olympian as you form new Alliances with mythical groups and battle to the bitter end.");
 				sender.sendMessage(" ");
 				sender.sendMessage(ChatColor.GRAY + " Developed by: " + ChatColor.GREEN + "_Alex" + ChatColor.GRAY + " and " + ChatColor.GREEN + "HmmmQuestionMark");
 				sender.sendMessage(ChatColor.GRAY + " Website: " + ChatColor.YELLOW + "http://demigodsrpg.com/");
 				sender.sendMessage(ChatColor.GRAY + " Source: " + ChatColor.YELLOW + "https://github.com/Clashnia/Minecraft-Demigods");
 			}
 			else if(option1.equalsIgnoreCase("characters"))
 			{
 				Demigods.message.tagged(sender, "Characters");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Characters.");
 			}
 			else if(option1.equalsIgnoreCase("shrine"))
 			{
 				Demigods.message.tagged(sender, "Shrines");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Shrines.");
 			}
 			else if(option1.equalsIgnoreCase("tribute"))
 			{
 				Demigods.message.tagged(sender, "Tributes");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Tributes.");
 			}
 			else if(option1.equalsIgnoreCase("player"))
 			{
 				Demigods.message.tagged(sender, "Players");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about Players.");
 			}
 			else if(option1.equalsIgnoreCase("pvp"))
 			{
 				Demigods.message.tagged(sender, "PVP");
 				sender.sendMessage(ChatColor.GRAY + " This is some info about PVP.");
 			}
 			else if(option1.equalsIgnoreCase("stats"))
 			{
 				Demigods.message.tagged(sender, "Stats");
 				sender.sendMessage(ChatColor.GRAY + " Read some server-wide stats for Demigods.");
 			}
 			else if(option1.equalsIgnoreCase("rankings"))
 			{
 				Demigods.message.tagged(sender, "Rankings");
 				sender.sendMessage(ChatColor.GRAY + " This is some ranking info about Demigods.");
 			}
 		}
 
 		for(String alliance : Deity.getLoadedDeityAlliances())
 		{
 			if(category.equalsIgnoreCase(alliance))
 			{
 				if(args.length < 2)
 				{
 					Demigods.message.tagged(sender, alliance + " Directory");
 					for(Deity deity : Deity.getAllDeitiesInAlliance(alliance))
 						sender.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase() + " " + deity.getInfo().getName().toLowerCase());
 				}
 				else
 				{
 					for(final Deity deity : Deity.getAllDeitiesInAlliance(alliance))
 					{
 						assert option1 != null;
 						if(option1.equalsIgnoreCase(deity.getInfo().getName()))
 						{
 							try
 							{
 								for(String toPrint : new ArrayList<String>()
 								{
 									{
 										addAll(deity.getInfo().getLore());
 										for(Ability ability : deity.getAbilities())
 										{
 											addAll(ability.getInfo().getDetails());
 										}
 									}
 								})
 								{
 									sender.sendMessage(toPrint);
 								}
 
 								return true;
 							}
 							catch(Exception e)
 							{
 								sender.sendMessage(ChatColor.RED + "(ERR: 3001)  Please report this immediately.");
 								e.printStackTrace(); // DEBUG
 								return true;
 							}
 						}
 					}
 					sender.sendMessage(ChatColor.DARK_RED + " No such deity, please try again.");
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	// Admin Directory
 	private static boolean dg_admin(CommandSender sender, String option1, String option2, String option3, String option4)
 	{
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		Player toEdit;
 		PlayerCharacter character;
 		int amount;
 
 		if(!Demigods.permission.hasPermissionOrOP(player, "demigods.admin")) return Demigods.message.noPermission(player);
 
 		if(option1 == null)
 		{
 			Demigods.message.tagged(sender, "Admin Directory");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin wand");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin debug");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin check <p> <char>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin remove [player|character] <name>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin set [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin add [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin sub [maxfavor|favor|devotion|ascensions] <p> <amt>");
 		}
 
 		if(option1 != null)
 		{
 			if(option1.equalsIgnoreCase("wand"))
 			{
 				if(!AdminUtility.wandEnabled(player))
 				{
 					DemigodsData.saveTemp(player.getName(), "temp_admin_wand", true);
 					player.sendMessage(ChatColor.RED + "Your admin wand has been enabled for " + Material.getMaterial(Demigods.config.getSettingInt("admin.wand_tool")));
 				}
 				else if(AdminUtility.wandEnabled(player))
 				{
 					DemigodsData.removeTemp(player.getName(), "temp_admin_wand");
 					player.sendMessage(ChatColor.RED + "You have disabled your admin wand.");
 				}
 				return true;
 			}
 			else if(option1.equalsIgnoreCase("debug"))
 			{
 				if(!DemigodsData.hasKeyTemp(player.getName(), "temp_admin_debug") || !Boolean.parseBoolean(DemigodsData.getValueTemp(player.getName(), "temp_admin_debug").toString()))
 				{
 					DemigodsData.saveTemp(player.getName(), "temp_admin_debug", true);
 					player.sendMessage(ChatColor.RED + "You have enabled debugging.");
 				}
 				else if(DemigodsData.hasKeyTemp(player.getName(), "temp_admin_debug") && Boolean.parseBoolean(DemigodsData.getValueTemp(player.getName(), "temp_admin_debug").toString()))
 				{
 					DemigodsData.removeTemp(player.getName(), "temp_admin_debug");
 					player.sendMessage(ChatColor.RED + "You have disabled debugging.");
 				}
 			}
 			else if(option1.equalsIgnoreCase("check"))
 			{
 				if(option2 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to specify a player.");
 					sender.sendMessage("/dg admin check <p>");
 					return true;
 				}
 
 				// Define variables
 				Player toCheck = Bukkit.getPlayer(option2);
 
 				if(option3 == null)
 				{
 					Demigods.message.tagged(sender, ChatColor.RED + toCheck.getName() + " Player Check");
 					sender.sendMessage(" Characters:");
 
 					final List<PlayerCharacter> chars = TrackedPlayer.getChars(toCheck);
 
 					for(PlayerCharacter checkingChar : chars)
 					{
 						player.sendMessage(ChatColor.GRAY + "   (#: " + checkingChar.getId() + ") Name: " + checkingChar.getName() + " / Deity: " + checkingChar.getDeity());
 					}
 				}
 				else
 				{
 					// TODO: Display specific character information when called for.
 				}
 			}
 			else if(option1.equalsIgnoreCase("remove"))
 			{
 				if(option2 == null || option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to be more specific with what you want to remove.");
 					return true;
 				}
 				else
 				{
 					if(option2.equalsIgnoreCase("player"))
 					{
 						// TODO: Full player data removal
 					}
 					else if(option2.equalsIgnoreCase("character"))
 					{
 						PlayerCharacter removing = PlayerCharacter.getCharacterByName(option3);
 						String removingName = removing.getName();
 
 						// Remove the data
 						removing.remove();
 
 						sender.sendMessage(ChatColor.RED + "Character \"" + removingName + "\" removed.");
 					}
 				}
 			}
 			else if(option1.equalsIgnoreCase("set"))
 			{
 				if(option2 == null || option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to specify a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = TrackedPlayer.getTracked(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().setMaxFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "Max favor set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's max favor has been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().setFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "Favor set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's favor has been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().setAscensions(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "Ascensions set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's Ascensions have been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 			}
 			else if(option1.equalsIgnoreCase("add"))
 			{
 				if(option2 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to be more specific.");
 					return true;
 				}
 				else if(option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You must select a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = TrackedPlayer.getTracked(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().setMaxFavor(character.getMeta().getMaxFavor() + amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " added to " + toEdit.getName() + "'s current character's max favor.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's max favor has been increased by " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().addFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " favor added to " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character has been given " + amount + " favor.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().addAscensions(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " Ascension(s) added to " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character has been given " + amount + " Ascensions.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 			}
 			else if(option1.equalsIgnoreCase("sub"))
 			{
 				if(option2 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You need to be more specific.");
 					return true;
 				}
 				else if(option3 == null)
 				{
 					sender.sendMessage(ChatColor.RED + "You must select a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = TrackedPlayer.getTracked(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().subtractFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " removed from " + toEdit.getName() + "'s current character's max favor.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character's max favor has been reduced by " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().subtractFavor(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " favor removed from " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character has had " + amount + " favor removed.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().subtractAscensions(amount);
 
 					sender.sendMessage(ChatColor.GREEN + "" + amount + " Ascension(s) removed from " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character has had " + amount + " Ascension(s) removed.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 					return true;
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "Invalid category selected.");
 				sender.sendMessage("/dg admin [set|add|sub] [maxfavor|favor|devotion|ascensions] <p> <amt>");
 				return true;
 			}
 		}
 
 		return true;
 	}
 
 	/*
 	 * Command: "check"
 	 */
 	private static boolean check(CommandSender sender, String[] args)
 	{
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 		if(character == null || !character.isImmortal())
 		{
 			player.sendMessage(ChatColor.RED + "You cannot use that command, mortal.");
 			return true;
 		}
 
 		// Define variables
 		int kills = character.getKills();
 		int deaths = character.getDeaths();
 		// int killstreak = character.getKillstreak();
 		String charName = character.getName();
 		String deity = character.getDeity().getInfo().getName();
 		String alliance = character.getAlliance();
 		int favor = character.getMeta().getFavor();
 		int maxFavor = character.getMeta().getMaxFavor();
 		int ascensions = character.getMeta().getAscensions();
 		// int powerOffense = character.getPower(AbilityEvent.Type.OFFENSE);
 		// int powerDefense = character.getPower(AbilityEvent.Type.DEFENSE);
 		// int powerStealth = character.getPower(AbilityEvent.Type.STEALTH);
 		// int powerSupport = character.getPower(AbilityEvent.Type.SUPPORT);
 		// int powerPassive = character.getPower(AbilityEvent.Type.PASSIVE);
 		ChatColor deityColor = character.getDeity().getInfo().getColor();
 		ChatColor favorColor = MiscUtility.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor());
 
 		// if(args.length == 1 && (args[0].equalsIgnoreCase("level") || args[0].equalsIgnoreCase("levels")))
 		// {
 		// // Send the user their info via chat
 		// Demigods.message.tagged(sender, "Levels Check");
 		//
 		// sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Offense: " + ChatColor.GREEN + powerOffense);
 		// sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Defense: " + ChatColor.GREEN + powerDefense);
 		// sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Stealth: " + ChatColor.GREEN + powerStealth);
 		// sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Support: " + ChatColor.GREEN + powerSupport);
 		// sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Passive: " + ChatColor.GREEN + powerPassive);
 		//
 		// return true;
 		// }
 
 		// Send the user their info via chat
 		Demigods.message.tagged(sender, "Player Check");
 
 		sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Character: " + ChatColor.AQUA + charName);
 		sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Deity: " + deityColor + deity + ChatColor.WHITE + " of the " + ChatColor.GOLD + MiscUtility.capitalize(alliance) + "s");
 		sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Favor: " + favorColor + favor + ChatColor.GRAY + " (of " + ChatColor.GREEN + maxFavor + ChatColor.GRAY + ")");
 		sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Ascensions: " + ChatColor.GREEN + ascensions);
 		sender.sendMessage(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.RESET + "Kills: " + ChatColor.GREEN + kills + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + deaths + ChatColor.WHITE); // + " / Killstreak: " + ChatColor.RED + killstreak);
 
 		return true;
 	}
 
 	/*
 	 * Command: "owner"
 	 */
 	private static boolean owner(CommandSender sender, String[] args)
 	{
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 
 		if(args.length < 1)
 		{
 			player.sendMessage(ChatColor.RED + "You must select a character.");
 			player.sendMessage(ChatColor.RED + "/owner <character>");
 			return true;
 		}
 
 		PlayerCharacter charToCheck = PlayerCharacter.getCharByName(args[0]);
 
 		if(charToCheck.getName() == null)
 		{
 			player.sendMessage(ChatColor.RED + "That character doesn't exist.");
 			return true;
 		}
 		else
 		{
 			player.sendMessage(charToCheck.getDeity().getInfo().getColor() + charToCheck.getName() + ChatColor.YELLOW + " belongs to " + charToCheck.getOfflinePlayer().getName() + ".");
 			return true;
 		}
 	}
 
 	/*
 	 * Command: "viewMaps"
 	 */
 	private static boolean viewMaps(CommandSender sender)
 	{
 		try
 		{
 			if(!PlayerCharacter.getAllChars().isEmpty())
 			{
 				sender.sendMessage(" ");
 				sender.sendMessage("-- Characters ---------------");
 				sender.sendMessage(" ");
 
 				for(PlayerCharacter character : PlayerCharacter.getAllChars())
 				{
 					sender.sendMessage(character.getName() + ": " + character.getDeity().getInfo().getName());
 				}
 			}
 
 			if(TrackedBlock.getAllBlocks().isEmpty())
 			{
 				sender.sendMessage(" ");
 				sender.sendMessage("-- Blocks -------------------");
 				sender.sendMessage(" ");
 
 				sender.sendMessage("There are no blocks in the database.");
 			}
 
 			sender.sendMessage(" ");
 			sender.sendMessage("-- Altars -------------------");
 			sender.sendMessage(" ");
 
 			for(Altar altar : Altar.getAllAltars())
 			{
 				sender.sendMessage(altar.getId() + ":");
 				sender.sendMessage(" " + UnicodeUtility.rightwardArrow() + " Active: " + altar.isActive());
 				if(sender instanceof Player) sender.sendMessage(" " + UnicodeUtility.rightwardArrow() + " Distance: " + altar.getLocation().distance(((Player) sender).getLocation()));
 				sender.sendMessage(" " + UnicodeUtility.rightwardArrow() + " Has Blocks: " + (altar.getBlocks() != null && !altar.getBlocks().isEmpty()));
 			}
 		}
 		catch(NullPointerException e)
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "Data is missing.");
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 
 	/*
 	 * Command: "removeChar"
 	 */
 	private static boolean removeChar(CommandSender sender, String[] args)
 	{
 		if(args.length != 1) return false;
 
 		// Define args
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		String charName = args[0];
 
 		if(TrackedPlayer.hasCharName(player, charName))
 		{
 			PlayerCharacter character = PlayerCharacter.getCharacterByName(charName);
 			character.remove();
 
 			sender.sendMessage(ChatColor.RED + "Character removed!");
 		}
 		else sender.sendMessage(ChatColor.RED + "There was an error while removing your character.");
 
 		return true;
 	}
 }
