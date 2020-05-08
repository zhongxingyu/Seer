 package xelitez.frostcraft.world;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.init.Blocks;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.util.ChatComponentText;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraftforge.common.util.ForgeDirection;
 import xelitez.frostcraft.SaveHandler;
 import xelitez.frostcraft.registry.IdMap;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class WorldGenFrostWingTower implements IWorldGenerator
 {
 	public List<BiomeGenBase> biomeList = new ArrayList<BiomeGenBase>();
 	
 	private static WorldGenFrostWingTower instance = new WorldGenFrostWingTower();
 	
 	public static WorldGenFrostWingTower getInstance()
 	{
 		return instance;
 	}
 	
 	public WorldGenFrostWingTower()
 	{
 		biomeList.add(BiomeGenBase.iceMountains);
 		biomeList.add(BiomeGenBase.icePlains);
		biomeList.add(BiomeGenBase.coldTaiga);
		biomeList.add(BiomeGenBase.coldTaigaHills);
 	}
 	
 	public static List<Integer> getPossibleYPos(World world, int x, int z)
 	{
 		List<Integer> list = new ArrayList<Integer>();
 		for(int i = 50;i < world.getActualHeight() - 50;i++)
 		{
 			if(world.getBlock(x, i - 1, z) != null && (world.getBlock(x, i - 1, z).isSideSolid(world, x, i - 1, z, ForgeDirection.UP)) && world.getBlock(x, i, z).getMaterial() == Material.air)
 			{
 				list.add(i);
 			}
 		}
 		return list;
 	}
 	
 	private static void sendChatMsg(String Msg)
 	{
 		FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText("<Frostcraft Generator> " + Msg));
 	}
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world,
 			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
 	{
 		int centerX = chunkX * 16 + random.nextInt(16);
 		int centerZ = chunkZ * 16 + random.nextInt(16);
 		if(!biomeList.contains(world.getBiomeGenForCoords(centerX, centerZ)))
 		{
 			return;
 		}
 		NBTTagCompound nbt = SaveHandler.getTagCompound(world, false);
 		NBTTagList list = null;
 		if(nbt.hasKey("Castles"))
 		{
			list = nbt.getTagList("Castles", 10);
 		}
 		if(list == null)
 		{
 			list = new NBTTagList();
 		}
 		List<Integer[]> intlist = new ArrayList<Integer[]>();
 		if(list != null)
 		{
 			for(int var0 = 0;var0 < list.tagCount();var0++)
 			{
 				NBTTagCompound nbt1 = (NBTTagCompound)list.getCompoundTagAt(var0);
 				Integer[] coords = new Integer[] {nbt1.getInteger("xCoord"), nbt1.getInteger("yCoord"), nbt1.getInteger("zCoord")};
 				intlist.add(coords);
 			}
 		}
 		
 		int centerY = 0;
 		for(Integer int1 : getPossibleYPos(world, centerX, centerZ))
 		{
 			if(int1 > 50 && random.nextInt(6) == 2)
 			{
 				centerY = int1;
 			}
 		}
 
 		if(centerY != 0)
 		{
 			boolean canGenerate = true;
 			for(Integer[] ints : intlist)
 			{
 				int dx = ints[0] - centerX;
 				int dy = ints[1] - centerY;
 				int dz = ints[2] - centerZ;
 				if(Math.sqrt(dx * dx + dy * dy + dz * dz) < 750)
 				{
 					canGenerate = false;
 				}
 			}
 
 			if(canGenerate)
 			{
 				generateSimple(random, centerX, centerY, centerZ, world);
 
 				NBTTagCompound nbt2 = new NBTTagCompound();
 				nbt2.setInteger("xCoord", centerX);
 				nbt2.setInteger("yCoord", centerY);
 				nbt2.setInteger("zCoord", centerZ);
 				list.appendTag(nbt2);
 				nbt.setTag("Castles", list);
 			}
 		}
 	}
 	
 	private void generateSimple(Random random, int xCoord, int yCoord, int zCoord, World world)
 	{
 		for(int x = -7;x < 8;x++)
 		{
 			for(int z = -7;z < 8;z++)
 			{
 				for(int y = -1;y <= 20;y++)
 				{
 					if(Math.floor(Math.sqrt(x * x + z * z)) <= 7.0D)
 					{
 						world.setBlockToAir(xCoord + x, yCoord + y, zCoord + z);
 						if(y == -1)
 						{
 							this.generateFloor(world, xCoord + x, yCoord + y - 1, zCoord + z);
 						}
 					}
 					if(Math.floor(Math.sqrt(x * x + z * z)) == 7.0D)
 					{
 						if(!(Math.abs(x) < 2 && z < 0 && y < 3 && y >= 0) && !(x == 0 && y == 3 && z < 0) && y <= 18)
 						{					
 							world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 						}
 						if(y == 18)
 						{
 							if(Math.abs(x) == 1 || Math.abs(z) == 1 || Math.abs(x) == 3 || Math.abs(z) == 3)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostSingleSlabSet, 2, 2);
 							}
 							if(Math.abs(x) == 5 && Math.abs(z) == 5)
 							{
 								world.setBlockToAir(xCoord + x, yCoord + y, zCoord + z);
 							}
 						}
 					}
 					if(y == -1)
 					{
 						if(Math.floor(Math.sqrt(x * x + z * z)) <= 6.0D)
 						{
 							world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 							if(Math.floor(Math.sqrt(x * x + z * z)) == 6.0D)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 							}
 							if(Math.floor(Math.sqrt(x * x + z * z)) == 2.0D)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 							}
 							if(Math.abs(x) > 1 && z == 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 							}
 							if(Math.abs(z) > 1 && x == 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 							}
 						}
 					}
 					if(y == 17)
 					{
 						if(Math.floor(Math.sqrt(x * x + z * z)) > 2.0D)
 						{
 							if(Math.floor(Math.sqrt(x * x + z * z)) <= 6.0D)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 								if(Math.floor(Math.sqrt(x * x + z * z)) == 6.0D)
 								{
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 								}
 								if(Math.abs(x) > 1 && z == 0)
 								{
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 								}
 								if(Math.abs(z) > 1 && x == 0)
 								{
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 								}
 							}
 						}
 					}
 					if(x == 0 && z == 0 && y <= 17)
 					{
 						world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 					}
 					if(Math.floor(Math.sqrt(x * x + z * z)) <= 2.0D && y >= 0 && y <= 17)
 					{
 						if(y % 4 == 0)
 						{
 							if(x < 0 && z > 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 							}
 							if(z == 0 && x < 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 2, 2);
 							}
 							if(z > 0 && x == 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 5, 2);
 							}
 						}
 						if(y % 4 == 1)
 						{
 							if(x > 0 && z > 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 							}
 							if(z > 0 && x == 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 0, 2);
 							}
 							if(z == 0 && x > 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 6, 2);
 							}
 							if(y == 17 && Math.floor(Math.sqrt(x * x + z * z)) == 2.0D)
 							{
 								if(z > 0 && x > 0)
 								{
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 								}
 								if(z == 0 && x > 0)
 								{
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 6, 2);
 								}
 							}
 						}
 						if(y % 4 == 2)
 						{
 							if(x > 0 && z < 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 							}
 							if(z == 0 && x > 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 3, 2);
 							}
 							if(z < 0 && x == 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 4, 2);
 							}
 						}
 						if(y % 4 == 3)
 						{
 							if(x < 0 && z < 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 							}
 							if(z < 0 && x == 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 1, 2);
 							}
 							if(z == 0 && x < 0)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 7, 2);
 							}
 						}
 					}
 				}
 			}
 		}
 		this.generateCandle(world, xCoord + 6, yCoord, zCoord, 2);
 		this.generateCandle(world, xCoord, yCoord, zCoord + 6, 1);
 		this.generateCandle(world, xCoord - 6, yCoord, zCoord, 2);
 		this.generateCandle(world, xCoord - 2, yCoord, zCoord - 6, 0);
 		this.generateCandle(world, xCoord + 2, yCoord, zCoord - 6, 0);
 		this.generateCandle(world, xCoord, yCoord + 18, zCoord, 0);
 		this.generateCandle(world, xCoord + 6, yCoord + 18, zCoord, 0);
 		this.generateCandle(world, xCoord, yCoord + 18, zCoord + 6, 0);
 		this.generateCandle(world, xCoord - 6, yCoord + 18, zCoord, 0);
 		world.setBlock(xCoord, yCoord + 18, zCoord - 6, IdMap.blockStatue, 2, 2);
 		world.setBlock(xCoord, yCoord + 19, zCoord - 6, IdMap.blockStatue, 4, 2);
 		world.setBlock(xCoord, yCoord + 20, zCoord - 6, IdMap.blockStatue, 5, 2);
 	}
 	
 	private void generateFloor(World world, int x, int yStart, int z)
 	{
 		for(int y = yStart; y > 0; y--)
 		{
 			if(world.getBlock(x, y, z).getMaterial() == Material.air || world.getBlock(x, y, z).isReplaceable(world, x, y, z))
 			{
 				world.setBlock(x, y, z, Blocks.dirt, 0, 2);
 			}
 			else
 			{
 				break;
 			}
 		}
 	}
 	
 	private void generateCandle(World world, int x, int y, int z, int type)
 	{
 		switch(type)
 		{
 		case 0:
 			world.setBlock(x, y, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x, y + 1, z, Blocks.torch, 5, 2);
 			break;
 		case 1:
 			world.setBlock(x, y, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x, y + 1, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x + 1, y + 1, z , IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x - 1, y + 1, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x + 1, y + 2, z, Blocks.torch, 5, 2);
 			world.setBlock(x - 1, y + 2, z, Blocks.torch, 5, 2);
 			break;
 		case 2:
 			world.setBlock(x, y, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x, y + 1, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x, y + 1, z + 1, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x, y + 1, z - 1, IdMap.blockBlackFrostFenceSet, 1, 2);
 			world.setBlock(x, y + 2, z + 1, Blocks.torch, 5, 2);
 			world.setBlock(x, y + 2, z - 1, Blocks.torch, 5, 2);
 			break;
 		}
 	}
 	
 	public static void generateAt(Random random, int i, int j, int k, World world1) 
 	{
 		final World world  = world1;
 		final int xCoord = i;
 		final int yCoord = j;
 		final int zCoord = k;
 		new Thread()
 		{
 			public void run()
 			{
 				for(int x = -7;x < 8;x++)
 				{
 					for(int z = -7;z < 8;z++)
 					{
 						for(int y = -1;y <= 20;y++)
 						{
 							if(Math.floor(Math.sqrt(x * x + z * z)) <= 7.0D)
 							{
 								world.setBlockToAir(xCoord + x, yCoord + y, zCoord + z);
 								if(y == -1)
 								{
 									this.generateFloor(world, xCoord + x, yCoord + y - 1, zCoord + z);
 								}
 							}
 							if(Math.floor(Math.sqrt(x * x + z * z)) == 7.0D)
 							{
 								if(!(Math.abs(x) < 2 && z < 0 && y < 3 && y >= 0) && !(x == 0 && y == 3 && z < 0) && y <= 18)
 								{					
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 								}
 								if(y == 18)
 								{
 									if(Math.abs(x) == 1 || Math.abs(z) == 1 || Math.abs(x) == 3 || Math.abs(z) == 3)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostSingleSlabSet, 2, 2);
 									}
 									if(Math.abs(x) == 5 && Math.abs(z) == 5)
 									{
 										world.setBlockToAir(xCoord + x, yCoord + y, zCoord + z);
 									}
 								}
 							}
 							if(y == -1)
 							{
 								if(Math.floor(Math.sqrt(x * x + z * z)) <= 6.0D)
 								{
 									world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 									if(Math.floor(Math.sqrt(x * x + z * z)) == 6.0D)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 									}
 									if(Math.floor(Math.sqrt(x * x + z * z)) == 2.0D)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 									}
 									if(Math.abs(x) > 1 && z == 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 									}
 									if(Math.abs(z) > 1 && x == 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 									}
 								}
 							}
 							if(y == 17)
 							{
 								if(Math.floor(Math.sqrt(x * x + z * z)) > 2.0D)
 								{
 									if(Math.floor(Math.sqrt(x * x + z * z)) <= 6.0D)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 										if(Math.floor(Math.sqrt(x * x + z * z)) == 6.0D)
 										{
 											world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 										}
 										if(Math.abs(x) > 1 && z == 0)
 										{
 											world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 										}
 										if(Math.abs(z) > 1 && x == 0)
 										{
 											world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 										}
 									}
 								}
 							}
 							if(x == 0 && z == 0 && y <= 17)
 							{
 								world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 							}
 							if(Math.floor(Math.sqrt(x * x + z * z)) <= 2.0D && y >= 0 && y <= 17)
 							{
 								if(y % 4 == 0)
 								{
 									if(x < 0 && z > 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 									}
 									if(z == 0 && x < 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 2, 2);
 									}
 									if(z > 0 && x == 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 5, 2);
 									}
 								}
 								if(y % 4 == 1)
 								{
 									if(x > 0 && z > 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 									}
 									if(z > 0 && x == 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 0, 2);
 									}
 									if(z == 0 && x > 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 6, 2);
 									}
 									if(y == 17 && Math.floor(Math.sqrt(x * x + z * z)) == 2.0D)
 									{
 										if(z > 0 && x > 0)
 										{
 											world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 										}
 										if(z == 0 && x > 0)
 										{
 											world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 6, 2);
 										}
 									}
 								}
 								if(y % 4 == 2)
 								{
 									if(x > 0 && z < 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 									}
 									if(z == 0 && x > 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 3, 2);
 									}
 									if(z < 0 && x == 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 4, 2);
 									}
 								}
 								if(y % 4 == 3)
 								{
 									if(x < 0 && z < 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 2, 2);
 									}
 									if(z < 0 && x == 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 1, 2);
 									}
 									if(z == 0 && x < 0)
 									{
 										world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrostStairBrick, 7, 2);
 									}
 								}
 							}
 						}
 					}
 				}
 				this.generateCandle(world, xCoord + 6, yCoord, zCoord, 2);
 				this.generateCandle(world, xCoord, yCoord, zCoord + 6, 1);
 				this.generateCandle(world, xCoord - 6, yCoord, zCoord, 2);
 				this.generateCandle(world, xCoord - 2, yCoord, zCoord - 6, 0);
 				this.generateCandle(world, xCoord + 2, yCoord, zCoord - 6, 0);
 				this.generateCandle(world, xCoord, yCoord + 18, zCoord, 0);
 				this.generateCandle(world, xCoord + 6, yCoord + 18, zCoord, 0);
 				this.generateCandle(world, xCoord, yCoord + 18, zCoord + 6, 0);
 				this.generateCandle(world, xCoord - 6, yCoord + 18, zCoord, 0);
 				world.setBlock(xCoord, yCoord + 18, zCoord - 6, IdMap.blockStatue, 2, 2);
 				world.setBlock(xCoord, yCoord + 19, zCoord - 6, IdMap.blockStatue, 4, 2);
 				world.setBlock(xCoord, yCoord + 20, zCoord - 6, IdMap.blockStatue, 5, 2);
 			}
 			
 			private void generateFloor(World world, int x, int yStart, int z)
 			{
 				for(int y = yStart; y > 0; y--)
 				{
 					if(world.getBlock(x, y, z).getMaterial() == Material.air || world.getBlock(x, y, z).isReplaceable(world, x, y, z))
 					{
 						world.setBlock(x, y, z, Blocks.dirt, 0, 2);
 					}
 					else
 					{
 						break;
 					}
 				}
 			}
 			
 			private void generateCandle(World world, int x, int y, int z, int type)
 			{
 				switch(type)
 				{
 				case 0:
 					world.setBlock(x, y, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x, y + 1, z, Blocks.torch, 5, 2);
 					break;
 				case 1:
 					world.setBlock(x, y, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x, y + 1, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x + 1, y + 1, z , IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x - 1, y + 1, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x + 1, y + 2, z, Blocks.torch, 5, 2);
 					world.setBlock(x - 1, y + 2, z, Blocks.torch, 5, 2);
 					break;
 				case 2:
 					world.setBlock(x, y, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x, y + 1, z, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x, y + 1, z + 1, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x, y + 1, z - 1, IdMap.blockBlackFrostFenceSet, 1, 2);
 					world.setBlock(x, y + 2, z + 1, Blocks.torch, 5, 2);
 					world.setBlock(x, y + 2, z - 1, Blocks.torch, 5, 2);
 					break;
 				}
 			}
 		}.start();
 	}
 	
 	public static void generateFrostWingCylinder(World world, int xCoord, int zCoord)
 	{
 		sendChatMsg("Generating Arena... 0%");
 		int yCoord = world.getActualHeight() - 27;
 		for(int x = -25;x <= 25;x++)
 		{
 			for(int z = -25;z <= 25;z++)
 			{
 				for(int y = 0;y <= 25;y++)
 				{
 					if(x == -15 && y == 5 && z == -15)
 					{
 						sendChatMsg("Generating Arena... 20%");
 					}
 					if(x == -5 && y == 10 && z == -5)
 					{
 						sendChatMsg("Generating Arena... 40%");
 					}
 					if(x == 5 && y == 15 && z == 5)
 					{
 						sendChatMsg("Generating Arena... 60%");
 					}
 					if(x == 15 && y == 20 && z == 15)
 					{
 						sendChatMsg("Generating Arena... 80%");
 					}
 					if(x == 25 && y == 25 && z == 25)
 					{
 						sendChatMsg("Generating Arena... 100%");
 					}
 					if(y == 0 && Math.floor(Math.sqrt(x * x + z * z)) < 25.0D)
 					{
 						world.setBlock(xCoord + x, yCoord + y, zCoord + z, Blocks.snow, 0, 2);
 					}
 					if(Math.floor(Math.sqrt(x * x + z * z)) == 25.0D)
 					{
 						world.setBlock(xCoord + x, yCoord + y, zCoord + z, IdMap.blockBlackFrost, 0, 2);
 					}
 				}
 			}
 		}
 		sendChatMsg("Generation complete!");
 	}
 	
 	public static void removeCylinder(World world, int xCoord, int zCoord)
 	{
 		if(!world.isRemote)
 		{
 			sendChatMsg("Removing Arena... 0%");
 			int yCoord = world.getActualHeight() - 27;
 			for(int x = -25;x <= 25;x++)
 			{
 				for(int z = -25;z <= 25;z++)
 				{
 					for(int y = 0;y <= 25;y++)
 					{
 						if(x == -15 && y == 5 && z == -15)
 						{
 							sendChatMsg("Removing Arena... 20%");
 						}
 						if(x == -5 && y == 10 && z == -5)
 						{
 							sendChatMsg("Removing Arena... 40%");
 						}
 						if(x == 5 && y == 15 && z == 5)
 						{
 							sendChatMsg("Removing Arena... 60%");
 						}
 						if(x == 15 && y == 20 && z == 15)
 						{
 							sendChatMsg("Removing Arena... 80%");
 						}
 						if(x == 25 && y == 25 && z == 25)
 						{
 							sendChatMsg("Removing Arena... 100%");
 						}
 						if(y == 0 && Math.floor(Math.sqrt(x * x + z * z)) < 25.0D)
 						{
 							world.setBlockToAir(xCoord + x, yCoord + y, zCoord + z);
 						}
 						if(Math.floor(Math.sqrt(x * x + z * z)) == 25.0D)
 						{
 							world.setBlockToAir(xCoord + x, yCoord + y, zCoord + z);
 						}
 					}
 				}
 			}
 			sendChatMsg("Removal complete!");
 		}
 	}
 	
 }
