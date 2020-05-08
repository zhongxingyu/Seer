 package com.censoredsoftware.demigods.ability.offense;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.player.Skill;
 import com.censoredsoftware.demigods.util.Zones;
 import com.google.common.collect.Lists;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.List;
 
 public class Blaze implements Ability
 {
 	public static Blaze ability;
 	private final static String name = "Blaze", command = "blaze";
 	private final static int cost = 400, delay = 15, repeat = 0;
 	private final static Skill.Type type = Skill.Type.OFFENSE;
 	private final static List<String> details = Lists.newArrayList("Ignite the ground at the target location.");
 	private String deity, permission;
 
 	public Blaze(String deity, String permission)
 	{
 		this.deity = deity;
 		this.permission = permission;
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
 	public Skill.Type getType()
 	{
 		return type;
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
 				if(Demigods.MiscUtil.isDisabledWorld(interactEvent.getPlayer().getWorld())) return;
 
 				if(!Ability.Util.isLeftClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 				if(!Deity.Util.canUseDeitySilent(character, deity)) return;
 
 				if(player.getItemInHand() != null && character.getMeta().checkBound(name, player.getItemInHand().getType()))
 				{
 					if(!DCharacter.Util.isCooledDown(character, name, false)) return;
 
 					Util.blaze(player);
 				}
 			}
 		};
 	}
 
 	@Override
 	public BukkitRunnable getRunnable()
 	{
 		return null;
 	}
 
 	public static class Util
 	{
 		// The actual ability command
 		public static void blaze(Player player)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			Location target;
 			LivingEntity entity = Ability.Util.autoTarget(player);
 			boolean notify;
 			if(entity != null)
 			{
 				target = Ability.Util.autoTarget(player).getLocation();
 				notify = true;
 				if(!Ability.Util.doAbilityPreProcess(player, entity, cost, type) || entity.getEntityId() == player.getEntityId()) return;
 			}
 			else
 			{
 				target = Ability.Util.directTarget(player);
 				notify = false;
 				if(!Ability.Util.doAbilityPreProcess(player, cost, type)) return;
 			}
 			int power = character.getMeta().getSkill(type).getLevel();
 			int diameter = (int) Math.ceil(1.43 * Math.pow(power, 0.1527));
 			if(diameter > 12) diameter = 12;
 
 			DCharacter.Util.setCoolDown(character, name, System.currentTimeMillis() + delay);
 			character.getMeta().subtractFavor(cost);
 
 			if(!Ability.Util.doTargeting(player, target, notify)) return;
 
 			for(int X = -diameter / 2; X <= diameter / 2; X++)
 			{
 				for(int Y = -diameter / 2; Y <= diameter / 2; Y++)
 				{
 					for(int Z = -diameter / 2; Z <= diameter / 2; Z++)
 					{
 						Block block = target.getWorld().getBlockAt(target.getBlockX() + X, target.getBlockY() + Y, target.getBlockZ() + Z);
						if((block.getType() == Material.AIR) || (((block.getType() == Material.SNOW)) && !Zones.inNoBuildZone(player, block.getLocation()))) block.setType(Material.FIRE);
 					}
 				}
 			}
 		}
 	}
 }
