 package btwmods.world;
 
 import java.util.EventObject;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.Chunk;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 
 public abstract class BlockEventBase extends EventObject {
 
 	private World world;
 	
 	protected Chunk chunk = null;
 	protected Block block = null;
 	protected int metadata = -1;
 	
 	private int x = 0;
 	private int y = 0;
 	private int z = 0;
 	private boolean coordsSet = false;
 	
 	private boolean checkedTileEntity = false;
 	protected TileEntity tileEntity = null;
 	protected ItemStack[] contents = null;
 	
 	public World getWorld() {
 		return world;
 	}
 	
 	public Chunk getChunk() {
 		if (chunk == null && hasCoordinatesSet()) {
 			chunk = world.getChunkFromBlockCoords(x, z);
 		}
 		
 		return chunk;
 	}
 	
 	public Block getBlock() {
 		if (block == null && hasCoordinatesSet()) {
 			block = Block.blocksList[world.getBlockId(x, y, z)];
 		}
 		
 		return block;
 	}
 	
 	public int getMetadata() {
 		return metadata;
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	public int getZ() {
 		return z;
 	}
 	
 	protected void setCoordinates(int x, int y, int z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 		coordsSet = true;
 	}
 	
 	public boolean hasCoordinatesSet() {
 		return coordsSet;
 	}
 	
 	public TileEntity getTileEntity() {
 		if (tileEntity == null && !checkedTileEntity && hasCoordinatesSet()) {
 			tileEntity = world.getBlockTileEntity(x, y, z);
 			checkedTileEntity = true;
 		}
 		
 		return tileEntity;
 	}
 	
 	public boolean hasInventory() {
 		return getTileEntity() instanceof IInventory;
 	}
 	
 	public ItemStack[] getContents() {
 		if (contents == null && hasInventory()) {
 			IInventory inventory = (IInventory)getTileEntity();
 			
 			contents = new ItemStack[inventory.getSizeInventory()];
 			for (int i = 0; i < contents.length; i++) {
 				contents[i] = inventory.getStackInSlot(i);
 			}
 		}
 		
 		return contents;
 	}
 	
 	protected BlockEventBase(Object source, World world) {
 		super(source);
 		this.world = world;
 	}
 }
