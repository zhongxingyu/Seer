 package fuj1n.globalChestMod.common.tileentity;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
 
 public class TileEntitySatLink extends TileEntity{
 
 	public float topBitRotationAngle = 0F;
 	
 	@Override
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound){
         super.readFromNBT(par1NBTTagCompound);
     }
 
     @Override
     public void writeToNBT(NBTTagCompound par1NBTTagCompound){
     	super.writeToNBT(par1NBTTagCompound);
     }
 	
 	@Override
     public AxisAlignedBB getRenderBoundingBox(){
 		AxisAlignedBB bb = AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
         return bb;
     }
 }
