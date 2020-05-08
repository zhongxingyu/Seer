 package elcon.mods.soundcraft.blocks;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.Explosion;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import elcon.mods.soundcraft.SoundCableType;
 import elcon.mods.soundcraft.SoundCraftConfig;
 
 public class BlockSoundCable extends BlockContainer {
 
 	public BlockSoundCable(int i) {
 		super(i, Material.cloth);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return null;
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		SoundCableType.registerIcons(iconRegister);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getBlockTextureFromSideAndMetadata(int i, int j) {
 		return SoundCableType.soundCables[getTypeFromMetadata(j)].textures[getColorFromMetadata(j)];
 	}
 	
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 	
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 	
 	@Override
 	public int getRenderType() {
 		return SoundCraftConfig.soundCableRenderID;
 	}
 	
 	public void notifyCableNeighbors(World world, int x, int y, int z) {
 		if (world.getBlockId(x, y, z) == blockID) {
 			world.notifyBlocksOfNeighborChange(x, y, z, blockID);
 			world.notifyBlocksOfNeighborChange(x - 1, y, z, blockID);
 			world.notifyBlocksOfNeighborChange(x + 1, y, z, blockID);
 			world.notifyBlocksOfNeighborChange(x, y, z - 1, blockID);
 			world.notifyBlocksOfNeighborChange(x, y, z + 1, blockID);
 			world.notifyBlocksOfNeighborChange(x, y - 1, z, blockID);
 			world.notifyBlocksOfNeighborChange(x, y + 1, z, blockID);
 		}
 	}
 	
 	private static boolean charToBoolean(char c) {
 		if(c == '0') {
 			return false;
 		} else if(c == '1') {
 			return true;
 		}
 		return false;
 	}
 	
 	private static String decToBin(int data) {
 		String s = Integer.toString(data, 2);
		while(13 - s.length() > 0) {
			s += "0";
 		}
		return s;
 	}
 	
 	public static int getTypeFromMetadata(int meta) {
		return Integer.parseInt(decToBin(meta).substring(0, 3), 2);
 	}
 	
 	public static boolean[] getDirectionsFromMetadata(int meta) {
 		boolean[] directions = new boolean[6];
		String direction = decToBin(meta).substring(3, 9);
 		for(int i = 0; i<6; i++) {
 			directions[i] = charToBoolean(direction.charAt(i));
 		}
 		return directions;
 	}
 	
 	public static int getColorFromMetadata(int meta) {
		return Integer.parseInt(decToBin(meta).substring(9, 13), 2);
 	}
 	
 	public boolean isCableEqual(int data1, int data2) {
 		System.out.println(data1 + " - " + data2);
		if(decToBin(data1).substring(0, 3).equalsIgnoreCase(decToBin(data2).substring(0, 3)) && decToBin(data1).substring(9, 13).equalsIgnoreCase(decToBin(data2).substring(9, 13))) {
 			System.out.println("equal");
 			return true;
 		}
 		return false;
 	}
 	
 	public void updateCable(World world, int x, int y, int z) {
 		int data = world.getBlockMetadata(x, y, z);
 		
 		System.out.println("updating cable at: " + x + "," + y + "," + z);
 		
 		if(world.getBlockId(x - 1, y, z) == blockID && isCableEqual(data, world.getBlockMetadata(x - 1, y, z))) {
 			world.setBlockMetadataWithNotify(x, y, z, data | 8, 2);
 			data = world.getBlockMetadata(x, y, z);
 		} 
 		if(world.getBlockId(x + 1, y, z) == blockID && isCableEqual(data, world.getBlockMetadata(x + 1, y, z))) {
 			world.setBlockMetadataWithNotify(x, y, z, data | 16, 2);
 			data = world.getBlockMetadata(x, y, z);
 		}
 		if(world.getBlockId(x, y - 1, z) == blockID && isCableEqual(data, world.getBlockMetadata(x, y - 1, z))) {
 			world.setBlockMetadataWithNotify(x, y, z, data | 32, 2);
 			data = world.getBlockMetadata(x, y, z);
 		}
 		if(world.getBlockId(x, y + 1, z) == blockID && isCableEqual(data, world.getBlockMetadata(x, y + 1, z))) {
 			world.setBlockMetadataWithNotify(x, y, z, data | 64, 2);
 			data = world.getBlockMetadata(x, y, z);
 		}
 		if(world.getBlockId(x, y, z - 1) == blockID && isCableEqual(data, world.getBlockMetadata(x, y, z - 1))) {
 			world.setBlockMetadataWithNotify(x, y, z, data | 128, 2);
 			data = world.getBlockMetadata(x, y, z);
 		} 
 		if(world.getBlockId(x, y, z + 1) == blockID && isCableEqual(data, world.getBlockMetadata(x, y, z + 1))) {
 			world.setBlockMetadataWithNotify(x, y, z, data | 256, 2);
 			data = world.getBlockMetadata(x, y, z);
 		}
 		
 		System.out.println(world.getBlockMetadata(x, y, z));
 		
 		//notifyCableNeighbors(world, x, y, z);
 	}
 	
 	@Override
 	public void onBlockAdded(World world, int x, int y, int z) {
 		if(!world.isRemote) {
 			updateCable(world, x, y, z);
 		}
 	}
 	
 	@Override
 	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
 		if(!world.isRemote) {
 			updateCable(world, x, y, z);
 		}
 	}
 
 	@Override
 	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int par5) {
 		if(!world.isRemote) {
 			updateCable(world, x, y, z);
 		}
 	}
 	
 	@Override
 	public void onNeighborBlockChange(World world, int x, int y, int z, int par5) {
 		if(!world.isRemote) {
 			//updateCable(world, x, y, z);
 		}
 	}
 }
