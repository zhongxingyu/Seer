 package com.censoredsoftware.demigods.engine.structure;
 
 import com.censoredsoftware.demigods.engine.location.Region;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import org.bukkit.Location;
 import org.bukkit.event.Listener;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 public interface Structure
 {
 	public String getName();
 
 	public Design getDesign(final String name);
 
 	public Set<Flag> getFlags();
 
 	public Listener getUniqueListener();
 
 	public int getRadius();
 
 	public Collection<StructureData> getAll();
 
 	public StructureData createNew(Location reference, boolean generate);
 
 	public interface Design
 	{
 		public String getName();
 
 		public Set<Location> getClickableBlocks(Location reference);
 
 		public Schematic getSchematic();
 	}
 
 	public enum Flag
 	{
 		DELETE_WITH_OWNER, PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, TRIBUTE_LOCATION, NO_OVERLAP
 	}
 
 	public static class Util
 	{
 		public static StructureData getStructureRegional(final Location location)
 		{
 			try
 			{
 				return Iterables.find(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getLocations().contains(location);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static StructureData getStructureGlobal(final Location location)
 		{
 			try
 			{
 				return Iterables.find(StructureData.Util.loadAll(), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getLocations().contains(location);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Set<StructureData> getStructuresInRegionalArea(Location location)
 		{
 			final Region center = Region.Util.getRegion(location);
 			Set<StructureData> set = new HashSet<StructureData>();
 			for(Region region : center.getSurroundingRegions())
 				set.addAll(getStructuresInSingleRegion(region));
 			return set;
 		}
 
 		public static Collection<StructureData> getStructuresInSingleRegion(final Region region)
 		{
 			return StructureData.Util.findAll(new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRegion().equals(region.toString());
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithType(final Location location, final String type)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getTypeName().equals(type) && save.getLocations().contains(location);
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithFlag(final Location location, final Flag flag)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getLocations().contains(location);
 				}
 			});
 		}
 
 		public static boolean isClickableBlockWithFlag(final Location location, final Flag flag)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getClickableBlocks().contains(location);
 				}
 			});
 		}
 
 		public static boolean isInRadiusWithFlag(Location location, Flag flag)
 		{
 			return getInRadiusWithFlag(location, flag) != null;
 		}
 
 		public static StructureData getInRadiusWithFlag(final Location location, final Flag flag)
 		{
 			try
 			{
 				return Iterables.find(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getReferenceLocation().getWorld().equals(location.getWorld()) && save.getReferenceLocation().distance(location) <= save.getType().getRadius();
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Set<StructureData> getInRadiusWithFlag(final Location location, final Flag flag, final int radius)
 		{
 			return Sets.filter(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getReferenceLocation().getWorld().equals(location.getWorld()) && save.getReferenceLocation().distance(location) <= radius;
 				}
 			});
 		}
 
 		public static void regenerateStructures()
 		{
 			for(StructureData save : StructureData.Util.loadAll())
 				save.generate();
 		}
 
 		public static Collection<StructureData> getStructureWithFlag(final Flag flag)
 		{
 			return StructureData.Util.findAll(new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
 				}
 			});
 		}
 
 		public static boolean noOverlapStructureNearby(Location location)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags().contains(Flag.NO_OVERLAP.name());
 				}
 			});
 		}
 
 		public static Set<StructureData> getStructureWeb(final Location start, final Flag flag, final int radius)
 		{
 			return getStructureWebRecursion(getInRadiusWithFlag(start, flag, radius), flag, radius);
 		}
 
 		private static Set<StructureData> getStructureWebRecursion(Set<StructureData> structures, final Flag flag, final int radius)
 		{
 			int notDone = structures.size();
 			Set<StructureData> working = Sets.newHashSet();
 			for(StructureData structure : structures)
 			{
 				Set<StructureData> found = getInRadiusWithFlag(structure.getReferenceLocation(), flag, radius);
				working.addAll(found);
 				if(found.isEmpty()) notDone -= 1;
 			}
			if(notDone != 0) getStructureWebRecursion(working, flag, radius);
 			return working;
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
 				if(location.getBlock().getType().isSolid()) return false;
 				location.add(0, 1, 1);
 				location.subtract(1, 0, 0);
 			}
 
 			return true;
 		}
 	}
 }
