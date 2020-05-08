 package jmm.block;
 
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
 import java.util.Random;
 
 import jmm.JurassicMinecraftMod;
 import jmm.lib.Reference;
 import jmm.tileentity.TileTestingBlock;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class BlockTestingBlock extends BlockContainer {
 
     public BlockTestingBlock(int ID, Material blockMaterial) {
         super(ID, blockMaterial);
         setHardness(2.0F);
         setResistance(5.0F);
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileTestingBlock();
     } 
     
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z,
                     EntityPlayer player, int idk, float what, float these, float are) {
             TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
             if (tileEntity == null || player.isSneaking()) {
                     return false;
             }
           player.openGui(JurassicMinecraftMod.instance, 0, world, x, y, z);
           return true;
     }
 
     @Override
     public void registerIcons(IconRegister iconRegister)
     {
              blockIcon = iconRegister.registerIcon(Reference.MOD_ID + ":TestingBlock");
     }
     
     @Override
     public boolean renderAsNormalBlock()
     {
         return true;
     }
     
     @Override
     public boolean isOpaqueCube() {
         return true;
     }
     @Override
     public int getRenderType()
     {
         return 0;
     }
     
     @Override
     public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
             dropItems(world, x, y, z);
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
