 package com.censoredsoftware.Demigods.Demo.Deity.Titan;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.util.Vector;
 
 import com.censoredsoftware.Demigods.Engine.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Ability.AbilityInfo;
 import com.censoredsoftware.Demigods.Engine.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Deity.DeityInfo;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 import com.google.common.collect.Sets;
 
 public class Prometheus extends Deity
 {
 	private static String name = "Prometheus", alliance = "Titan";
 	private static ChatColor color = ChatColor.GOLD;
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
 				add(ChatColor.GRAY + " -> " + ChatColor.WHITE + item.name());
 			}
 			add(ChatColor.YELLOW + " Abilities:");
 		}
 	};
 	private static Type type = Type.DEMO;
 	private static Set<Ability> abilities = new HashSet<Ability>()
 	{
 		{
 			add(new ShootFireball());
 			add(new Blaze());
 			add(new Firestorm());
 		}
 	};
 
 	public Prometheus()
 	{
 		super(new DeityInfo(name, alliance, color, claimItems, lore, type), abilities);
 	}
 
 	public static void shootFireball(Location from, Location to, Player shooter)
 	{
 		shooter.getWorld().spawnEntity(from, EntityType.FIREBALL);
 		for(Entity entity : shooter.getNearbyEntities(2, 2, 2))
 		{
 			if(!(entity instanceof Fireball)) continue;
 
 			Fireball fireball = (Fireball) entity;
 			to.setX(to.getX() + .5);
 			to.setY(to.getY() + .5);
 			to.setZ(to.getZ() + .5);
 			Vector path = to.toVector().subtract(from.toVector());
 			Vector victor = from.toVector().add(from.getDirection().multiply(2));
 			fireball.teleport(new Location(shooter.getWorld(), victor.getX(), victor.getY(), victor.getZ()));
 			fireball.setDirection(path);
 			fireball.setShooter(shooter);
 		}
 	}
 }
 
 class ShootFireball extends Ability
 {
 	private static String deity = "Prometheus", name = "Fireball", command = "fireball", permission = "demigods.titan.protmetheus";
 	private static int cost = 100, delay = 5, cooldownMin = 0, cooldownMax = 0;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/fireball" + ChatColor.WHITE + " - Shoot a fireball at the cursor's location.");
 		}
 	};
 	private static Type type = Type.OFFENSE;
 
 	protected ShootFireball()
 	{
 		super(new AbilityInfo(deity, name, command, permission, cost, delay, cooldownMin, cooldownMax, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGH)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(!Ability.isClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isEnabledAbility(name) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == character.getMeta().getBind(name))))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, false)) return;
 
 					fireball(player);
 				}
 			}
 		});
 	}
 
 	// The actual ability command
 	public static void fireball(Player player)
 	{
 		// Define variables
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 		LivingEntity target = Ability.autoTarget(player);
 
 		if(!Ability.doAbilityPreProcess(player, target, "fireball", cost, type)) return;
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay);
 		character.getMeta().subtractFavor(cost);
 
 		if(!Ability.targeting(player, target)) return;
 
 		if(target.getEntityId() != player.getEntityId())
 		{
 			Prometheus.shootFireball(player.getEyeLocation(), target.getLocation(), player);
 		}
 	}
 }
 
 class Blaze extends Ability
 {
 	private static String deity = "Prometheus", name = "Blaze", command = "blaze", permission = "demigods.titan.protmetheus";
 	private static int cost = 400, delay = 15, cooldownMin = 0, cooldownMax = 0;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/blaze" + ChatColor.WHITE + " - Ignite the ground at the target location.");
 		}
 	};
 	private static Type type = Type.OFFENSE;
 
 	protected Blaze()
 	{
 		super(new AbilityInfo(deity, name, command, permission, cost, delay, cooldownMin, cooldownMax, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGH)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(!Ability.isClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isEnabledAbility(name) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == character.getMeta().getBind(name))))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, false)) return;
 
 					blaze(player);
 				}
 			}
 		});
 	}
 
 	// The actual ability command
 	public static void blaze(Player player)
 	{
 		// Define variables
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 		LivingEntity target = Ability.autoTarget(player);
 		int power = character.getMeta().getLevel("OFFENSE");
 		int diameter = (int) Math.ceil(1.43 * Math.pow(power, 0.1527));
 		if(diameter > 12) diameter = 12;
 
 		if(!Ability.doAbilityPreProcess(player, target, name, cost, type)) return;
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay);
 		character.getMeta().subtractFavor(cost);
 
 		if(!Ability.targeting(player, target)) return;
 
 		if(target.getEntityId() != player.getEntityId())
 		{
 			for(int X = -diameter / 2; X <= diameter / 2; X++)
 			{
 				for(int Y = -diameter / 2; Y <= diameter / 2; Y++)
 				{
 					for(int Z = -diameter / 2; Z <= diameter / 2; Z++)
 					{
 						Block block = target.getWorld().getBlockAt(target.getLocation().getBlockX() + X, target.getLocation().getBlockY() + Y, target.getLocation().getBlockZ() + Z);
 						if((block.getType() == Material.AIR) || (((block.getType() == Material.SNOW)) && !ZoneUtility.zoneNoBuild(player, block.getLocation()))) block.setType(Material.FIRE);
 					}
 				}
 			}
 		}
 	}
 }
 
 class Firestorm extends Ability
 {
 	private static String deity = "Prometheus", name = "Firestorm", command = "firestorm", permission = "demigods.titan.protmetheus.ultimate";
 	private static int cost = 5500, delay = 15, cooldownMin = 60, cooldownMax = 600;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/firestorm" + ChatColor.WHITE + " - Rain down fireballs from the sky.");
 		}
 	};
 	private static Type type = Type.OFFENSE;
 
 	protected Firestorm()
 	{
 		super(new AbilityInfo(deity, name, command, permission, cost, delay, cooldownMin, cooldownMax, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGH)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(!Ability.isClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isEnabledAbility(name) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == character.getMeta().getBind(name))))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, false)) return;
 
 					firestorm(player);
 				}
 			}
 		});
 	}
 
 	// The actual ability command
 	public static void firestorm(final Player player)
 	{
 		// Define variables
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 		int devotion = character.getMeta().getDevotion();
 		int total = 20 * (int) Math.round(2 * Math.pow(devotion, 0.15));
 		final Set<LivingEntity> entityList = Sets.newHashSet();
 		for(Entity entity : player.getNearbyEntities(50, 50, 50))
 		{
 			if(!(entity instanceof LivingEntity)) continue;
 			if(entity instanceof Player)
 			{
 				PlayerCharacter otherCharacter = TrackedPlayer.getTracked((Player) entity).getCurrent();
 				if(otherCharacter != null && PlayerCharacter.areAllied(character, otherCharacter)) continue;
 			}
 			if(!ZoneUtility.canTarget(entity)) continue;
 			entityList.add((LivingEntity) entity);
 		}
 		for(int i = 0; i <= total; i += 20)
 		{
 			for(final LivingEntity entity : entityList)
 			{
 				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 				{
 					@Override
 					public void run()
 					{
						Location up = entity.getLocation().getWorld().getHighestBlockAt(Location.locToBlock(entity.getLocation().getX()), Location.locToBlock(entity.getLocation().getZ())).getLocation();
						up.setY(up.getY() + 10.0);
 						Prometheus.shootFireball(up, entity.getLocation(), player);
 					}
 				}, i);
 			}
 		}
 	}
 
 }
