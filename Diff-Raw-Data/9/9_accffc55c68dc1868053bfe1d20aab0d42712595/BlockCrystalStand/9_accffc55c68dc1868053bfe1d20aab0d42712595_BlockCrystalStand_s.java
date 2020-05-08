 package Seremis.SoulCraft.block;
 
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import Seremis.SoulCraft.api.plasma.block.IPlasmaContainerItem;
 import Seremis.SoulCraft.core.lib.RenderIds;
 import Seremis.SoulCraft.core.proxy.CommonProxy;
 import Seremis.SoulCraft.tileentity.TileCrystalStand;
 
 public class BlockCrystalStand extends SCBlock {
 
 	public BlockCrystalStand(int ID, Material material) {
 		super(ID, material);
 		setUnlocalizedName("crystalStand");
 		setHardness(0.5F);
 	}
 	
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 	
 	@Override
 	public int getRenderType() {
 		return RenderIds.CrystalStandRenderID;
 	}
 	
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
     {
 	    TileCrystalStand tile = (TileCrystalStand)(world.getBlockTileEntity(x, y, z));
 	    ItemStack currPlayerItem = player.getCurrentEquippedItem();
 	    ItemStack currStack = tile.getStackInSlot(0);
     			
     	if(currPlayerItem != null && currPlayerItem.itemID == ModBlocks.Crystal.blockID) {
     		if(tile != null && currStack == null) {
     			tile.setInventorySlotContents(0, new ItemStack(ModBlocks.Crystal, 1));
     			player.getCurrentEquippedItem().stackSize--;
     			world.markBlockForRenderUpdate(x, y, z);
     		}
     	}
    	if(tile != null && currStack != null && !(currPlayerItem.getItem() instanceof IPlasmaContainerItem)) {
     			
     	    tile.setInventorySlotContents(0, null);
     				
     	    if(CommonProxy.proxy.isServerWorld(world)){
     	        world.spawnEntityInWorld(new EntityItem(world, x, y, z, currStack));
     	    }
     		world.markBlockForRenderUpdate(x, y, z);	
     	}
     	return true;
     }
 	
 	@Override
 	public TileEntity createTileEntity(World world, int metadata) {
 		return new TileCrystalStand();
 	}
 	
 	@Override
     public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
             dropItems(world, x, y, z);
             if(((TileCrystalStand)world.getBlockTileEntity(x, y, z)).getNetwork() != null) {
                 ((TileCrystalStand)world.getBlockTileEntity(x, y, z)).getNetwork().invalidate((TileCrystalStand)world.getBlockTileEntity(x, y, z));
             }
             super.breakBlock(world, x, y, z, par5, par6);
     }
 
     private void dropItems(World world, int x, int y, int z){
             Random rand = new Random();
 
             TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
             if (!(tileEntity instanceof IInventory)) {
                     return;
             }
             IInventory inventory = (IInventory) tileEntity;
 
             for (int i = 0; i < inventory.getSizeInventory(); i++) {
                     ItemStack item = inventory.getStackInSlot(i);
 
                     if (item != null && item.stackSize > 0) {
                             float rx = rand.nextFloat() * 0.8F + 0.1F;
                             float ry = rand.nextFloat() * 0.8F + 0.1F;
                             float rz = rand.nextFloat() * 0.8F + 0.1F;
 
                             EntityItem entityItem = new EntityItem(world,
                                             x + rx, y + ry, z + rz,
                                             new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 
                             if (item.hasTagCompound()) {
                                     entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                             }
 
                             float factor = 0.05F;
                             entityItem.motionX = rand.nextGaussian() * factor;
                             entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                             entityItem.motionZ = rand.nextGaussian() * factor;
                             world.spawnEntityInWorld(entityItem);
                             item.stackSize = 0;
                     }
             }
     }
 
 }
