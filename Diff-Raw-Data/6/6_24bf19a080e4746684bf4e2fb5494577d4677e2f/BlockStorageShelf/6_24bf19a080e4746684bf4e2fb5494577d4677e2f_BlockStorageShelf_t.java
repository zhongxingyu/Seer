 package enhancedbooks.common.blocks;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ari
  * Date: 02/01/13
  * Time: 3:47 AM
  */
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import enhancedbooks.client.renderers.StorageShelfRenderer;
 import enhancedbooks.common.core.BooksCore;
 import enhancedbooks.common.tileentities.TileEntityStorageShelf;
import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import java.util.Random;
 
 public class BlockStorageShelf extends BlockContainer {
 
     public BlockStorageShelf(int ID) {
         super(ID, Material.wood); // The ID and material (The material defines what tools are better on it)
         this.setUnlocalizedName("StorageShelf"); // The name of the block
         this.setHardness(1.5F); // How hard the block is to break
         this.setResistance(2.0F); // How well the block resists explosions
        this.setStepSound(Block.soundWoodFootstep); // The sound the block makes when you walk on it as well as place or break
         this.setCreativeTab(CreativeTabs.tabDecorations);
     }
 
     @Override
     public void registerIcons(IconRegister iconRegister) {
         this.blockIcon = iconRegister.registerIcon("enhancedbooks:StorageShelf");
     }
 
     @Override
     public int getEnchantPower(World world, int x, int y, int z) {
         int power = 0;
         boolean[] filledSlots = ((TileEntityStorageShelf)world.getBlockTileEntity(x, y, z)).getFilledSlots();
         for(int i = 0; i < 8; i++) {
             power += filledSlots[i] ? 1: 0;
         }
         return power / 3; //Divide by 3, because it's 3 books in one vanilla shelf for 1 vanilla shelf of power.
        //THIS SHOULD BE 3F BUT LEX FUCKED UP THE PULL D:
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving placer, ItemStack itemStack) {
         if (!world.isRemote) {
             int rot = MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
             world.setBlockMetadataWithNotify(x, y, z, rot, 3);
         }
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public int getRenderType() {
         return StorageShelfRenderer.storageShelfModelID;
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
         return true;
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int idk, float what, float these, float are) {
         TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 
         if (tileEntity == null || player.isSneaking()) {
             return false;
         }
         player.openGui(BooksCore.instance, 0, world, x, y, z);
         return true;
     }
 
     @Override
     public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
         dropItems(world, x, y, z);
         super.breakBlock(world, x, y, z, par5, par6);
     }
 
     private void dropItems(World world, int x, int y, int z) {
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
 
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileEntityStorageShelf();
     }
 }
