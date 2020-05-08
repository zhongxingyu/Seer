 /**
  * Copyright (c) Scott Killen, 2012
  * 
  * This mod is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license
  * located in /MMPL-1.0.txt
  */
 
 package bunyan.blocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.BlockLog;
 import net.minecraft.src.Entity;
 import net.minecraft.src.IBlockAccess;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.World;
 import net.minecraft.src.forge.ITextureProvider;
 
 public class WideLog extends BlockLog implements ITextureProvider {
 
 	// metadata values
 	public static final int		metaRedwood	= 0;
 	public static final int		metaFir		= 1;
 	public static final int		metaOak		= 2;
 
	@Override
	protected int damageDropped(int metadata) {
		return metadata & 3;
	}

 	public static int metadataWithDirection(int metadata, int direction)
 	{
 		direction -= 2;
 		return metadata | direction << 2;
 	}
 
 	public static void setDirection(World world, int x, int y, int z,
 			byte direction)
 	{
 		final int metadata = world.getBlockMetadata(x, y, z);
 		world.setBlockMetadataWithNotify(x, y, z,
 				metadataWithDirection(metadata, direction));
 	}
 
 	public WideLog(int id) {
 		super(id);
 		blockIndexInTexture = 0;
 		setStepSound(soundWoodFootstep);
 		setHardness(Block.wood.getHardness());
 		setResistance(Block.wood.getExplosionResistance(null) * 5.0F);
 		setRequiresSelfNotify();
 	}
 
 	@Override
 	public void addCreativeItems(ArrayList itemList) {
 		itemList.add(new ItemStack(blockID, 1, metaRedwood));
 		itemList.add(new ItemStack(blockID, 1, metaFir));
 		itemList.add(new ItemStack(blockID, 1, metaOak));
 	}
 
 	@Override
 	public float getExplosionResistance(Entity entity) {
 		return Block.wood.getExplosionResistance(entity);
 	}
 
 	@Override
 	public int getFireSpreadSpeed(World world, int x, int y, int z,
 			int metadata, int face) {
 		return Block.wood.getFireSpreadSpeed(world, x, y, z, metadata, face);
 	}
 
 	@Override
 	public int getFlammability(IBlockAccess world, int x, int y, int z,
 			int metadata, int face) {
 		return Block.wood.getFlammability(world, x, y, z, metadata, face);
 	}
 
 	@Override
 	public float getHardness() {
 		return Block.wood.getHardness();
 	}
 
 	@Override
 	public float getHardness(int meta) {
 		return Block.wood.getHardness(meta);
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int metadata)
 	{
 		final int direction = ((metadata & 3 << 2) >> 2) + 2;
 		metadata &= 3;
 		int row = 0;
 		int column = 0;
 
 		if (side == 0 || side == 1) { // bottom or top
 			if (direction == 2) {
 				row = 1;
 				column = 1;
 			} else if (direction == 3) {
 				row = 2;
 				column = 1;
 			} else if (direction == 4) {
 				row = 2;
 				column = 0;
 			} else if (direction == 5) {
 				row = 1;
 				column = 0;
 			}
 		} else if (side == 2) {
 			if (direction == 2) {
 				row = 0;
 				column = 0;
 			} else if (direction == 3) {
 				row = 3;
 				column = 0;
 			} else if (direction == 4) {
 				row = 3;
 				column = 1;
 			} else if (direction == 5) {
 				row = 0;
 				column = 1;
 			}
 		} else if (side == 3) {
 			if (direction == 2) {
 				row = 3;
 				column = 1;
 			} else if (direction == 3) {
 				row = 0;
 				column = 1;
 			} else if (direction == 4) {
 				row = 0;
 				column = 0;
 			} else if (direction == 5) {
 				row = 3;
 				column = 0;
 			}
 		} else if (side == 4) {
 			if (direction == 2) {
 				row = 3;
 				column = 0;
 			} else if (direction == 3) {
 				row = 3;
 				column = 1;
 			} else if (direction == 4) {
 				row = 0;
 				column = 1;
 			} else if (direction == 5) {
 				row = 0;
 				column = 0;
 			}
 		} else if (side == 5) if (direction == 2) {
 			row = 0;
 			column = 1;
 		} else if (direction == 3) {
 			row = 0;
 			column = 0;
 		} else if (direction == 4) {
 			row = 3;
 			column = 0;
 		} else if (direction == 5) {
 			row = 3;
 			column = 1;
 		}
 
 		return (row + 3) * 16 + column + metadata * 2;
 	}
 
 	@Override
 	public String getTextureFile() {
 		return "/bunyan/blocks/blocks.png";
 	}
 
 	@Override
 	public int idDropped(int metadata, Random random, int alwaysZero) {
 		return blockID;
 	}
 
 	@Override
 	public void onBlockAdded(World world, int x, int y, int z) {
 		super.onBlockAdded(world, x, y, z);
 		setDefaultDirection(world, x, y, z);
 	}
 
 	private void setDefaultDirection(World world, int x, int y, int z) {
 		if (!world.isRemote) {
 			final int eastBlock = world.getBlockId(x, y, z - 1);
 			final int westBlock = world.getBlockId(x, y, z + 1);
 			final int northBlock = world.getBlockId(x - 1, y, z);
 			final int southBlock = world.getBlockId(x + 1, y, z);
 			byte direction = 3;
 
 			if (eastBlock != blockID && westBlock != blockID
 					&& northBlock != blockID && southBlock != blockID)
 			{
 
 				if (Block.opaqueCubeLookup[eastBlock]
 						&& !Block.opaqueCubeLookup[westBlock])
 					direction = 3;
 
 				if (Block.opaqueCubeLookup[westBlock]
 						&& !Block.opaqueCubeLookup[eastBlock])
 					direction = 2;
 
 				if (Block.opaqueCubeLookup[northBlock]
 						&& !Block.opaqueCubeLookup[southBlock])
 					direction = 5;
 
 				if (Block.opaqueCubeLookup[southBlock]
 						&& !Block.opaqueCubeLookup[northBlock])
 					direction = 4;
 				setDirection(world, x, y, z, direction);
 			} else
 				setSmartDirection(world, x, y, z);
 		}
 	}
 
 	private void setSmartDirection(World world, int x, int y, int z) {
 		int block = world.getBlockId(x, y, z + 1);
 		int direction = 3;
 		if (block == blockID) {
 			final int blockMeta = world.getBlockMetadata(x, y, z + 1);
 			final int blockDirection = ((blockMeta & 3 << 2) >> 2) + 2;
 			direction = blockDirection == 3 ? 2
 					: blockDirection == 2 ? 3 : blockDirection == 4 ? 5
 							: 4;
 		}
 
 		block = world.getBlockId(x, y, z - 1);
 		if (block == blockID) {
 			final int blockMeta = world.getBlockMetadata(x, y, z - 1);
 			final int blockDirection = ((blockMeta & 3 << 2) >> 2) + 2;
 			direction = blockDirection == 3 ? 2
 					: blockDirection == 2 ? 3 : blockDirection == 4 ? 5
 							: 4;
 		}
 
 		block = world.getBlockId(x - 1, y, z);
 		if (block == blockID) {
 			final int blockMeta = world.getBlockMetadata(x - 1, y, z);
 			final int blockDirection = ((blockMeta & 3 << 2) >> 2) + 2;
 			direction = blockDirection == 3 ? 4
 					: blockDirection == 4 ? 3 : blockDirection == 2 ? 5
 							: 2;
 		}
 
 		block = world.getBlockId(x + 1, y, z);
 		if (block == blockID) {
 			final int blockMeta = world.getBlockMetadata(x + 1, y, z);
 			final int blockDirection = ((blockMeta & 3 << 2) >> 2) + 2;
 			direction = blockDirection == 3 ? 4
 					: blockDirection == 4 ? 3 : blockDirection == 2 ? 5
 							: 2;
 		}
 		setDirection(world, x, y, z, (byte) direction);
 	}
 
 }
