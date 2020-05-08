 package com.censoredsoftware.demigods.util;
 
 import com.censoredsoftware.demigods.Elements;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.location.Region;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 @SuppressWarnings("unchecked")
 public class Structures
 {
 	public static Structure.Save getStructureRegional(Location location)
 	{
 		for(Structure.Save save : getStructuresInRegionalArea(location))
 		{
 			if(save.getLocations().contains(location)) return save;
 		}
 		return null;
 	}
 
 	public static Structure.Save getStructureSaveGlobal(Location location)
 	{
 		for(Structure.Save save : loadAll())
 		{
 			if(save.getLocations().contains(location)) return save;
 		}
 		return null;
 	}
 
 	public static Set<Structure.Save> getStructuresInRegionalArea(Location location)
 	{
 		final Region center = Region.Util.getRegion(location);
 		return new HashSet<Structure.Save>()
 		{
 			{
 				for(Region region : center.getSurroundingRegions())
 					addAll(getStructuresInSingleRegion(region));
 			}
 		};
 	}
 
 	public static Collection<Structure.Save> getStructuresInSingleRegion(final Region region)
 	{
 		return findAll(new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(@Nullable Structure.Save save)
 			{
 				return save.getRegion().equals(region.toString());
 			}
 		});
 	}
 
 	public static boolean partOfStructureWithType(Location location, final String type)
 	{
 		for(Structure.Save save : findRegional(location, new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(@Nullable Structure.Save save)
 			{
 				return save.getType().equals(type);
 			}
 		}))
 		{
 			if(save.getLocations().contains(location)) return true;
 		}
 		return false;
 	}
 
 	public static boolean partOfStructureWithFlag(Location location, Structure.Flag flag)
 	{
 		for(Structure.Save save : getStructuresSavesWithFlag(location, flag))
 			if(save.getLocations().contains(location)) return true;
 		return false;
 	}
 
 	public static boolean isClickableBlockWithFlag(Location location, Structure.Flag flag)
 	{
 		for(Structure.Save save : getStructuresSavesWithFlag(location, flag))
 		{
 			if(save.getClickableBlocks().contains(location)) return true;
 		}
 		return false;
 	}
 
 	public static boolean isInRadiusWithFlag(Location location, Structure.Flag flag)
 	{
 		return getInRadiusWithFlag(location, flag) != null;
 	}
 
 	public static Structure.Save getInRadiusWithFlag(Location location, Structure.Flag flag)
 	{
 		for(Structure.Save save : getStructuresSavesWithFlag(flag))
			if(save.getReferenceLocation().distance(location) <= save.getStructure().getRadius()) return save;
 		return null;
 	}
 
 	public static boolean isTrespassingInNoGriefingZone(Player player)
 	{
 		Location location = player.getLocation();
 		if(Zones.zoneNoBuild(player, location)) return true;
 		Structure.Save save = getInRadiusWithFlag(location, Structure.Flag.NO_GRIEFING);
 		if(save != null) return !(save.getOwner() != null && save.getOwner().getId().equals(DPlayer.Util.getPlayer(player).getCurrent().getId()));
 		return false;
 	}
 
 	public static void regenerateStructures()
 	{
 		for(Structure.Save save : loadAll())
 			save.generate(false);
 	}
 
 	public static Set<Structure> getStructuresWithFlag(final Structure.Flag flag)
 	{
 		return new HashSet<Structure>()
 		{
 			{
 				for(Elements.ListedStructure structure : Collections2.filter(Sets.newHashSet(Elements.Structures.values()), new Predicate<Elements.ListedStructure>()
 				{
 					@Override
 					public boolean apply(@Nullable Elements.ListedStructure lS)
 					{
 						return lS.getStructure().getFlags().contains(flag);
 					}
 				}))
 					add(structure.getStructure());
 			}
 		};
 	}
 
 	public static Set<Structure.Save> getStructuresSavesWithFlag(final Structure.Flag flag)
 	{
 		return new HashSet<Structure.Save>()
 		{
 			{
 				for(Structure.Save save : findAll(new Predicate<Structure.Save>()
 				{
 					@Override
 					public boolean apply(@Nullable Structure.Save save)
 					{
 						return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
 					}
 				}))
 					add(save);
 			}
 		};
 	}
 
 	public static Set<Structure.Save> getStructuresSavesWithFlag(final Location location, final Structure.Flag flag)
 	{
 		return new HashSet<Structure.Save>()
 		{
 			{
 				for(Structure.Save save : findRegional(location, new Predicate<Structure.Save>()
 				{
 					@Override
 					public boolean apply(@Nullable Structure.Save save)
 					{
 						return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
 					}
 				}))
 					add(save);
 			}
 		};
 	}
 
 	/**
 	 * Strictly checks the <code>reference</code> location to validate if the area is safe
 	 * for automated generation.
 	 * 
 	 * @param reference the location to be checked
 	 * @param area how big of an area (in blocks) to validate
 	 * @return Boolean
 	 */
 	public static boolean canGenerateStrict(Location reference, int area)
 	{
 		Location location = reference.clone();
 		location.subtract(0, 1, 0);
 		location.add((area / 3), 0, (area / 2));
 
 		// Check ground
 		for(int i = 0; i < area; i++)
 		{
 			if(!location.getBlock().getType().isSolid()) return false;
 			location.subtract(1, 0, 0);
 		}
 
 		// Check ground adjacent
 		for(int i = 0; i < area; i++)
 		{
 			if(!location.getBlock().getType().isSolid()) return false;
 			location.subtract(0, 0, 1);
 		}
 
 		// Check ground adjacent again
 		for(int i = 0; i < area; i++)
 		{
 			if(!location.getBlock().getType().isSolid()) return false;
 			location.add(1, 0, 0);
 		}
 
 		location.add(0, 1, 0);
 
 		// Check air diagonally
 		for(int i = 0; i < area + 1; i++)
 		{
 			if(!location.getBlock().getType().isTransparent()) return false;
 			location.add(0, 1, 1);
 			location.subtract(1, 0, 0);
 		}
 
 		return true;
 	}
 
 	public static void remove(UUID id)
 	{
 		DataManager.structures.remove(id);
 	}
 
 	public static Structure.Save load(UUID id)
 	{
 		return DataManager.structures.get(id);
 	}
 
 	public static Collection<Structure.Save> loadAll()
 	{
 		return DataManager.structures.values();
 	}
 
 	public static Collection<Structure.Save> findAll(Predicate<Structure.Save> predicate)
 	{
 		return Collections2.filter(DataManager.structures.values(), predicate);
 	}
 
 	public static Collection<Structure.Save> findRegional(Location location, Predicate<Structure.Save> predicate)
 	{
 		return Collections2.filter(getStructuresInRegionalArea(location), predicate);
 	}
 }
