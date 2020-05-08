 package com.censoredsoftware.demigods.deity.titan;
 
 import com.censoredsoftware.demigods.Elements;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.util.Strings;
 import com.censoredsoftware.demigods.util.Unicodes;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Iapetus implements Deity
 {
 	private final static String name = "Iapetus", alliance = "Titan", permission = "demigods.titan.iapetus";
	private final static ChatColor color = ChatColor.RED;
 	private final static Map<Material, Integer> claimItems = Maps.newHashMap(ImmutableMap.of(Material.ARROW, 2, Material.WOOD_SWORD, 1));
 	private final static Map<Material, Integer> forsakeItems = Maps.newHashMap(ImmutableMap.of(Material.GOLD_SWORD, 4));
 	private final static List<String> lore = new ArrayList<String>(9 + claimItems.size())
 	{
 		{
 			add(" ");
 			add(ChatColor.RED + " Demigods > " + ChatColor.RESET + color + name);
 			add(ChatColor.RESET + "-----------------------------------------------------");
 			add(" ");
 			add(ChatColor.YELLOW + " Claim Items:");
 			add(" ");
 			for(Map.Entry<Material, Integer> entry : claimItems.entrySet())
 				add(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.WHITE + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 			add(" ");
 			add(ChatColor.YELLOW + " Abilities:");
 			add(" ");
 		}
 	};
 	private final static Type type = Type.TIER1;
 	private final static Set<Ability> abilities = Sets.newHashSet();
 
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 
 	@Override
 	public Elements.ListedDeity getListedDeity()
 	{
 		return Elements.Deities.IAPETUS;
 	}
 
 	@Override
 	public String getAlliance()
 	{
 		return alliance;
 	}
 
 	@Override
 	public String getPermission()
 	{
 		return permission;
 	}
 
 	@Override
 	public ChatColor getColor()
 	{
 		return color;
 	}
 
 	@Override
 	public Map<Material, Integer> getClaimItems()
 	{
 		return claimItems;
 	}
 
 	@Override
 	public Map<Material, Integer> getForsakeItems()
 	{
 		return forsakeItems;
 	}
 
 	@Override
 	public List<String> getLore()
 	{
 		return lore;
 	}
 
 	@Override
 	public Type getType()
 	{
 		return type;
 	}
 
 	@Override
 	public Set<Ability> getAbilities()
 	{
 		return abilities;
 	}
 
 	@Override
 	public String toString()
 	{
 		return getName();
 	}
 }
