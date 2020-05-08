 package com.censoredsoftware.demigods.greek.ability.ultimate;
 
 import com.censoredsoftware.demigods.engine.ability.Ability;
 import com.censoredsoftware.demigods.engine.battle.Battle;
 import com.censoredsoftware.demigods.engine.deity.Deity;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;
 
 import java.util.List;
 import java.util.Set;
 
 public class Storm implements Ability
 {
 	private final static String name = "Storm", command = "storm";
	private final static int cost = 3700, delay = 600 * 20, repeat = 0;
 	private final static List<String> details = Lists.newArrayList("Throw all of your enemies into the sky as lightning fills the heavens.");
 	private String deity, permission;
 
 	public Storm(String deity, String permission)
 	{
 		this.deity = deity;
 		this.permission = permission;
 	}
 
 	public static void storm(Player player)
 	{
 		// Define variables
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 		Set<Entity> entitySet = Sets.newHashSet();
 		Vector playerLocation = player.getLocation().toVector();
 
 		if(!Ability.Util.doAbilityPreProcess(player, cost)) return;
 
 		for(Entity anEntity : player.getWorld().getEntities())
 			if(anEntity.getLocation().toVector().isInSphere(playerLocation, 50.0)) entitySet.add(anEntity);
 
 		for(Entity entity : entitySet)
 		{
 			if(entity instanceof Player)
 			{
 				Player otherPlayer = (Player) entity;
 				DCharacter otherChar = DPlayer.Util.getPlayer(otherPlayer).getCurrent();
 				if(otherPlayer.equals(player)) continue;
 				if(otherChar != null && !DCharacter.Util.areAllied(character, otherChar) && !otherPlayer.equals(player))
 				{
 					Util.strikeLightning(player, otherPlayer);
 					Util.strikeLightning(player, otherPlayer);
 					continue;
 				}
 			}
 			if(entity instanceof LivingEntity)
 			{
 				LivingEntity livingEntity = (LivingEntity) entity;
 				Util.strikeLightning(player, livingEntity);
 				Util.strikeLightning(player, livingEntity);
 			}
 		}
 	}
 
 	@Override
 	public String getDeity()
 	{
 		return deity;
 	}
 
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 
 	@Override
 	public String getCommand()
 	{
 		return command;
 	}
 
 	@Override
 	public String getPermission()
 	{
 		return permission;
 	}
 
 	@Override
 	public int getCost()
 	{
 		return cost;
 	}
 
 	@Override
 	public int getDelay()
 	{
 		return delay;
 	}
 
 	@Override
 	public int getRepeat()
 	{
 		return repeat;
 	}
 
 	@Override
 	public List<String> getDetails()
 	{
 		return details;
 	}
 
 	@Override
 	public Material getWeapon()
 	{
 		return null;
 	}
 
 	@Override
 	public boolean hasWeapon()
 	{
 		return getWeapon() != null;
 	}
 
 	@Override
 	public Listener getListener()
 	{
 		return new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(Zones.inNoDemigodsZone(interactEvent.getPlayer().getLocation())) return;
 
 				if(!Ability.Util.isLeftClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 				if(!Deity.Util.canUseDeitySilent(character, deity)) return;
 
 				if(player.getItemInHand() != null && character.getMeta().checkBound(name, player.getItemInHand().getType()))
 				{
 					if(!DCharacter.Util.isCooledDown(character, name, true)) return;
 
 					storm(player);
 
 					int cooldownMultiplier = (int) (delay * ((double) character.getMeta().getAscensions() / 100));
 					DCharacter.Util.setCoolDown(character, name, System.currentTimeMillis() + cooldownMultiplier * 1000);
 				}
 			}
 		};
 	}
 
 	@Override
 	public BukkitRunnable getRunnable()
 	{
 		return null;
 	}
 
 	public static class Lightning implements Ability
 	{
 		private final static String name = "Lighting", command = "lightning";
 		private final static int cost = 140, delay = 1000, repeat = 0;
 		private final static List<String> details = Lists.newArrayList("Strike lightning upon your enemies.");
 		private String deity, permission;
 
 		public Lightning(String deity, String permission)
 		{
 			this.deity = deity;
 			this.permission = permission;
 		}
 
 		protected static void lightning(Player player)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			Location target;
 			LivingEntity entity = Util.autoTarget(player);
 			boolean notify;
 			if(entity != null)
 			{
 				target = entity.getLocation();
 				notify = true;
 				if(!Util.doAbilityPreProcess(player, entity, cost)) return;
 			}
 			else
 			{
 				target = Util.directTarget(player);
 				notify = false;
 				if(!Util.doAbilityPreProcess(player, cost)) return;
 			}
 
 			DCharacter.Util.setCoolDown(character, name, System.currentTimeMillis() + delay);
 			character.getMeta().subtractFavor(cost);
 
 			Storm.Util.strikeLightning(player, target, notify);
 		}
 
 		@Override
 		public String getDeity()
 		{
 			return deity;
 		}
 
 		@Override
 		public String getName()
 		{
 			return name;
 		}
 
 		@Override
 		public String getCommand()
 		{
 			return command;
 		}
 
 		@Override
 		public String getPermission()
 		{
 			return permission;
 		}
 
 		@Override
 		public int getCost()
 		{
 			return cost;
 		}
 
 		@Override
 		public int getDelay()
 		{
 			return delay;
 		}
 
 		@Override
 		public int getRepeat()
 		{
 			return repeat;
 		}
 
 		@Override
 		public List<String> getDetails()
 		{
 			return details;
 		}
 
 		@Override
 		public Material getWeapon()
 		{
 			return null;
 		}
 
 		@Override
 		public boolean hasWeapon()
 		{
 			return getWeapon() != null;
 		}
 
 		@Override
 		public Listener getListener()
 		{
 			return new Listener()
 			{
 				@EventHandler(priority = EventPriority.HIGH)
 				public void onPlayerInteract(PlayerInteractEvent interactEvent)
 				{
 					if(Zones.inNoDemigodsZone(interactEvent.getPlayer().getLocation())) return;
 
 					if(!Util.isLeftClick(interactEvent)) return;
 
 					// Set variables
 					Player player = interactEvent.getPlayer();
 					DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 					if(!Deity.Util.canUseDeitySilent(character, deity)) return;
 
 					if(player.getItemInHand() != null && character.getMeta().checkBound(name, player.getItemInHand().getType()))
 					{
 						if(!DCharacter.Util.isCooledDown(character, name, false)) return;
 
 						lightning(player);
 					}
 				}
 			};
 		}
 
 		@Override
 		public BukkitRunnable getRunnable()
 		{
 			return null;
 		}
 	}
 
 	public static class Util
 	{
 		public static boolean strikeLightning(Player player, LivingEntity target)
 		{
 			return Battle.Util.canTarget(target) && strikeLightning(player, target.getLocation(), true);
 		}
 
 		public static boolean strikeLightning(Player player, Location target, boolean notify)
 		{
 			// Set variables
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 			if(!player.getWorld().equals(target.getWorld())) return false;
 			Location toHit = Ability.Util.adjustedAimLocation(character, target);
 
 			player.getWorld().strikeLightningEffect(toHit);
 
 			for(Entity entity : toHit.getBlock().getChunk().getEntities())
 			{
 				if(entity instanceof LivingEntity)
 				{
 					if(!Battle.Util.canTarget(entity)) continue;
 					LivingEntity livingEntity = (LivingEntity) entity;
 					if(livingEntity.equals(player)) continue;
 					if((toHit.getBlock().getType().equals(Material.WATER) || toHit.getBlock().getType().equals(Material.STATIONARY_WATER)) && livingEntity.getLocation().distance(toHit) < 8) Ability.Util.dealDamage(player, livingEntity, character.getMeta().getAscensions() * 6, EntityDamageEvent.DamageCause.LIGHTNING);
 					else if(livingEntity.getLocation().distance(toHit) < 2) Ability.Util.dealDamage(player, livingEntity, character.getMeta().getAscensions() * 4, EntityDamageEvent.DamageCause.LIGHTNING);
 				}
 			}
 
 			if(!Ability.Util.isHit(target, toHit) && notify) player.sendMessage(ChatColor.RED + "Missed...");
 
 			return true;
 		}
 	}
 }
