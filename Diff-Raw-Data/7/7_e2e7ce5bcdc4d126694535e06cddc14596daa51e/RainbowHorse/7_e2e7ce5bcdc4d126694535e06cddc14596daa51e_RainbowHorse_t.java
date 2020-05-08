 package com.censoredsoftware.demigods.ability.passive;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.player.Pet;
 import com.censoredsoftware.demigods.util.Randoms;
 import com.google.common.collect.Lists;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.event.Listener;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.List;
 
 public class RainbowHorse implements Ability
 {
 	private final static String name = "Horse of a Different Color", command = null;
 	private final static int cost = 0, delay = 0, repeat = 100;
 	private final static Devotion.Type type = Devotion.Type.PASSIVE;
 	private final static List<String> details = Lists.newArrayList("All of you horse are belong to us.");
 	private String deity, permission;
 
 	public RainbowHorse(String deity, String permission)
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
 	public Devotion.Type getType()
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
 		return null;
 	}
 
 	@Override
 	public BukkitRunnable getRunnable()
 	{
 		return new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for(Pet horse : Pet.Util.findByType(EntityType.HORSE))
 				{
					if(horse.getCurrentLocation() == null || Demigods.isDisabledWorld(horse.getCurrentLocation().getWorld())) return;
					if(horse.getDeity().getName().equals("DrD1sco") && horse.getEntity() != null && !horse.getEntity().isDead()) ((Horse) horse.getEntity()).setColor(getRandomHorseColor());
 				}
 			}
 
 			private Horse.Color getRandomHorseColor()
 			{
 				return Lists.newArrayList(Horse.Color.values()).get(Randoms.generateIntRange(0, 5));
 			}
 		};
 	}
 }
