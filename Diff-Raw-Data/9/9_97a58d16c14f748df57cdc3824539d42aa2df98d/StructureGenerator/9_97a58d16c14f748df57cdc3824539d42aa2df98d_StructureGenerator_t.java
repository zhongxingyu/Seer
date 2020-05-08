 package com.censoredsoftware.Demigods.Engine.Structure;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.google.common.collect.Lists;
 
 public interface StructureGenerator
 {
 	public Set<GeneratorSchematic> getGenerateBlockAreas();
 
 	public static class GeneratorSchematic
 	{
 		private World world;
 		private Location reference;
 		private int X, Y, Z, XX, YY, ZZ;
 		private boolean cuboid;
 		private Set<BlockData> blockData;
 
 		/**
 		 * Constructor for a GeneratorSchematic (non-cuboid).
 		 * 
 		 * @param reference The reference location that all the data is based off of.
 		 * @param X The relative X coordinate of the schematic from the reference location.
 		 * @param Y The relative Y coordinate of the schematic from the reference location.
 		 * @param Z The relative Z coordinate of the schematic from the reference location.
 		 * @param blockData The BlockData objects of this schematic.
 		 */
 		public GeneratorSchematic(Location reference, int X, int Y, int Z, Set<BlockData> blockData)
 		{
 			this.world = reference.getWorld();
 			this.reference = reference;
 			this.X = this.XX = X;
 			this.Y = this.YY = Y;
 			this.Z = this.ZZ = Z;
 			this.cuboid = false;
 			this.blockData = blockData;
 		}
 
 		/**
 		 * Constructor for a GeneratorSchematic (cuboid).
 		 * 
 		 * @param reference The reference location that all the data is based off of.
 		 * @param X The relative X coordinate of the schematic from the reference location.
 		 * @param Y The relative Y coordinate of the schematic from the reference location.
 		 * @param Z The relative Z coordinate of the schematic from the reference location.
 		 * @param XX The second relative X coordinate of the schematic from the reference location, creating a cuboid.
 		 * @param YY The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
 		 * @param ZZ The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
 		 * @param blockData The BlockData objects of this schematic.
 		 */
 		public GeneratorSchematic(Location reference, int X, int Y, int Z, int XX, int YY, int ZZ, Set<BlockData> blockData)
 		{
 			this.world = reference.getWorld();
 			this.reference = reference;
 			this.X = X;
 			this.Y = Y;
 			this.Z = Z;
 			this.XX = XX;
 			this.YY = YY;
 			this.ZZ = ZZ;
 			this.cuboid = true;
 			this.blockData = blockData;
 		}
 
 		/**
 		 * Get the material of the object (a random material is chosen based on the configured odds).
 		 * 
 		 * TODO This method needs work, I'm not sure this is the more efficient way to do what we want.
 		 * 
 		 * @return A material.
 		 */
 		public BlockData getBlockData()
 		{
 			List<BlockData> blockData = Lists.newArrayList();
 			for(BlockData block : this.blockData)
 			{
 				if(block.getOdds() == 0) continue;
 				if(block.getOdds() == 100) return block;
 				for(int i = 0; i < block.getOdds(); i++)
 				{
 					blockData.add(block);
 				}
 			}
 			return blockData.get(MiscUtility.generateIntRange(0, blockData.size()));
 		}
 
 		/**
 		 * Get a relative location, based on the <code>X</code>, <code>Y</code>, <code>Z</code> coordinates relative to the object's central location.
 		 * 
 		 * @param X Relative X coordinate.
 		 * @param Y Relative Y coordinate.
 		 * @param Z Relative Z coordinate.
 		 * @return New relative location.
 		 */
 		public Location getLocation(int X, int Y, int Z)
 		{
			return this.reference.clone().add(X, Y, Z);
 		}
 
 		/**
 		 * Get the block locations in this object.
 		 * 
 		 * @return A set of locations.
 		 */
 		public Set<Location> getBlockLocations()
 		{
 			if(cuboid)
 			{
				final int X = this.X < this.XX ? this.X : this.XX, XX = this.X > this.XX ? this.X : this.XX;
				final int Y = this.Y < this.YY ? this.Y : this.YY, YY = this.Y > this.YY ? this.Y : this.YY;
				final int Z = this.Z < this.ZZ ? this.Z : this.ZZ, ZZ = this.Z > this.ZZ ? this.Z : this.ZZ;
 
 				return new HashSet<Location>()
 				{
 					{
 						for(int i = X; i < XX; i++)
 						{
 							for(int o = Y; o < YY; o++)
 							{
 								for(int p = Z; p < ZZ; p++)
 								{
 									add(getLocation(i, o, p));
 								}
 							}
 						}
 					}
 				};
 			}
 			else
 			{
 				final int X = this.X, Y = this.Y, Z = this.Z;
 				return new HashSet<Location>()
 				{
 					{
 						add(getLocation(X, Y, Z));
 					}
 				};
 			}
 		}
 
 		public void generate()
 		{
 			for(Location location : getBlockLocations())
 			{
 				BlockData data = getBlockData();
 				location.getBlock().setTypeIdAndData(data.getMaterial().getId(), data.getData(), false);
 			}
 		}
 	}
 
 	public static class BlockData
 	{
 		private Material material;
 		private byte data;
 		private int odds;
 
 		/**
 		 * Constructor for BlockData with only Material given.
 		 * 
 		 * @param material Material of the block.
 		 */
 		public BlockData(Material material)
 		{
 			this.material = material;
 			this.data = 0;
 			this.odds = 100;
 		}
 
 		/**
 		 * Constructor for BlockData with only Material and byte data given.
 		 * 
 		 * @param material Material of the block.
 		 * @param data Byte data of the block.
 		 */
 		public BlockData(Material material, byte data)
 		{
 			this.material = material;
 			this.data = data;
 			this.odds = 100;
 		}
 
 		/**
 		 * Constructor for BlockData with Material, byte data, and odds given.
 		 * 
 		 * @param material Material of the block.
 		 * @param data Byte data of the block.
 		 * @param odds The odds of this object being generated.
 		 */
 		public BlockData(Material material, byte data, int odds)
 		{
 			this.material = material;
 			this.data = data;
 			this.odds = odds;
 		}
 
 		/**
 		 * Get the Material of this object.
 		 * 
 		 * @return A Material.
 		 */
 		public Material getMaterial()
 		{
 			return this.material;
 		}
 
 		/**
 		 * Get the byte data of this object.
 		 * 
 		 * @return Byte data.
 		 */
 		public byte getData()
 		{
 			return this.data;
 		}
 
 		/**
 		 * Get the odds of this object generating.
 		 * 
 		 * @return Odds (as an integer, out of 100).
 		 */
 		public int getOdds()
 		{
 			return this.odds;
 		}
 	}
 }
