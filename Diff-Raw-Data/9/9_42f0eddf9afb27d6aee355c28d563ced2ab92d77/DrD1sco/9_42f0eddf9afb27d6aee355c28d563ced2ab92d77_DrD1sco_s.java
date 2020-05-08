 package com.censoredsoftware.Demigods.Episodes.Demo.Deity.Insignian;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.*;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.AbilityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Devotion;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.DeityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.Structure;
 import com.censoredsoftware.Demigods.Engine.Utility.*;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class DrD1sco extends Deity
 {
 	private static String name = "DrD1sco", alliance = "Insignian", permission = "demigods.insignian.disco";
 	private static ChatColor color = ChatColor.DARK_PURPLE;
 	private static Set<Material> claimItems = new HashSet<Material>()
 	{
 		{
 			add(Material.DIRT);
 		}
 	};
 	private static List<String> lore = new ArrayList<String>()
 	{
 		{
 			add(" ");
 			add(ChatColor.AQUA + " Demigods > " + ChatColor.RESET + color + name);
 			add(ChatColor.RESET + "-----------------------------------------------------");
 			add(ChatColor.YELLOW + " Claim Items:");
 			for(Material item : claimItems)
 			{
 				add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.WHITE + item.name());
 			}
 			add(ChatColor.YELLOW + " Abilities:");
 		}
 	};
 	private static Type type = Type.DEMO;
 	private static Set<Ability> abilities = new HashSet<Ability>()
 	{
 		{
 			add(new RainbowWalking());
 			add(new RainbowHorse());
			add(new Discoball());
 		}
 	};
 
 	public DrD1sco()
 	{
 		super(new DeityInfo(name, alliance, permission, color, claimItems, lore, type), abilities);
 	}
 
 	protected static void playRandomNote(Location location, float volume)
 	{
 		location.getWorld().playSound(location, Sound.NOTE_BASS_GUITAR, volume, (float) ((double) MiscUtility.generateIntRange(5, 10) / 10.0));
 	}
 }
 
 class RainbowWalking extends Ability
 {
 	private static String deity = "DrD1sco", name = "Rainbow Walking", command = null, permission = "demigods.insignian.disco";
 	private static int cost = 0, delay = 0, repeat = 5;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add("Spread the disco while sneaking.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.STEALTH;
 
 	protected RainbowWalking()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), null, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for(Player online : Bukkit.getOnlinePlayers())
 				{
 					if(Deity.canUseDeitySilent(online, "DrD1sco") && online.isSneaking() && !online.isFlying() && !ZoneUtility.zoneNoPVP(online.getLocation()) && !Structure.isTrespassingInNoGriefingZone(online)) doEffect(online, true);
 					else if(Deity.canUseDeitySilent(online, "DrD1sco")) doEffect(online, false);
 				}
 			}
 
 			private void doEffect(Player player, boolean effect)
 			{
 				for(Entity entity : player.getNearbyEntities(30, 30, 30))
 				{
 					if(!(entity instanceof Player)) continue;
 					Player viewing = (Player) entity;
 					if(effect)
 					{
 						viewing.hidePlayer(player);
 						rainbow(player, viewing);
 					}
 					else viewing.showPlayer(player);
 				}
 				if(effect)
 				{
 					rainbow(player, player);
 					DrD1sco.playRandomNote(player.getLocation(), 0.5F);
 				}
 			}
 		});
 	}
 
 	private static void rainbow(Player disco, Player player)
 	{
 		player.sendBlockChange(disco.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation(), Material.WOOL, (byte) MiscUtility.generateIntRange(0, 15));
 		if(Demigods.runningSpigot()) SpigotUtility.playParticle(disco.getLocation(), Effect.COLOURED_DUST, 1, 0, 1, 10F, 100, 30);
 	}
 }
 
 class RainbowHorse extends Ability
 {
 	private static String deity = "DrD1sco", name = "Horse of a Different Color", command = null, permission = "demigods.insignian.disco";
 	private static int cost = 0, delay = 0, repeat = 100;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add("All of you horse are belong to us.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.PASSIVE;
 
 	protected RainbowHorse()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), null, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for(TameableWrapper horse : TameableWrapper.findByType(EntityType.HORSE))
 				{
 					if(horse.getDeity().getInfo().getName().equals("DrD1sco") && horse.getEntity() != null && !horse.getEntity().isDead())
 					{
 						((Horse) horse.getEntity()).setColor(getRandomHorseColor());
 					}
 				}
 			}
 
 			private Horse.Color getRandomHorseColor()
 			{
 				return Lists.newArrayList(Horse.Color.values()).get(MiscUtility.generateIntRange(0, 5));
 			}
 		});
 	}
 }
 
 class Discoball extends Ability
 {
 	private static String deity = "DrD1sco", name = "Discoball of Doom", command = "discoball", permission = "demigods.insignian.disco";
 	private static int cost = 30, delay = 30, repeat = 4;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add("Spread the music while causing destruction.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.ULTIMATE;
 
 	private static Set<FallingBlock> discoBalls = Sets.newHashSet();
 
 	protected Discoball()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 				if(!Ability.isLeftClick(interactEvent)) return;
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isBound(name))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, true)) return;
 
 					discoBall(player);
 				}
 			}
 
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onBlockChange(EntityChangeBlockEvent changeEvent)
 			{
 				if(changeEvent.getEntityType() != EntityType.FALLING_BLOCK) return;
 				changeEvent.getBlock().setType(Material.AIR);
 				FallingBlock block = (FallingBlock) changeEvent.getEntity();
 				if(discoBalls.contains(block))
 				{
 					discoBalls.remove(block);
 					block.remove();
 				}
 			}
 		}, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for(FallingBlock block : discoBalls)
 				{
 					if(block != null)
 					{
 						Location location = block.getLocation();
 						DrD1sco.playRandomNote(location, 2F);
 						sparkleSparkle(location);
 						destoryNearby(location);
 					}
 				}
 			}
 		});
 	}
 
 	private static void discoBall(final Player player)
 	{
 		// Set variables
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 		if(!Ability.doAbilityPreProcess(player, name, cost, info)) return;
 		character.getMeta().subtractFavor(cost);
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay);
 
 		// Cooldown
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay * 1000);
 
 		balls(player);
 
 		player.sendMessage(ChatColor.YELLOW + "Dance!");
 
 		Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				player.sendMessage(ChatColor.RED + "B" + ChatColor.GOLD + "o" + ChatColor.YELLOW + "o" + ChatColor.GREEN + "g" + ChatColor.AQUA + "i" + ChatColor.LIGHT_PURPLE + "e" + ChatColor.DARK_PURPLE + " W" + ChatColor.BLUE + "o" + ChatColor.RED + "n" + ChatColor.GOLD + "d" + ChatColor.YELLOW + "e" + ChatColor.GREEN + "r" + ChatColor.AQUA + "l" + ChatColor.LIGHT_PURPLE + "a" + ChatColor.DARK_PURPLE + "n" + ChatColor.BLUE + "d" + ChatColor.RED + "!");
 			}
 		}, 40);
 	}
 
 	private static void balls(Player player)
 	{
 		for(Location location : LocationUtility.getCirclePoints(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() + 30 < 256 ? player.getLocation().getBlockY() + 30 : 256, player.getLocation().getBlockZ()), 3.0, 50))
 		{
 			spawnBall(location);
 		}
 
 	}
 
 	private static void spawnBall(Location location)
 	{
 		final FallingBlock discoBall = location.getWorld().spawnFallingBlock(location, Material.GLOWSTONE, (byte) 0);
 		discoBalls.add(discoBall);
 		Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				discoBalls.remove(discoBall);
 				discoBall.remove();
 			}
 		}, 600);
 	}
 
 	private static void sparkleSparkle(Location location)
 	{
 		if(Demigods.runningSpigot()) SpigotUtility.playParticle(location, Effect.CRIT, 1, 1, 1, 10F, 1000, 30);
 	}
 
 	private static void destoryNearby(Location location)
 	{
 		location.getWorld().createExplosion(location, 2F);
 	}
 }
