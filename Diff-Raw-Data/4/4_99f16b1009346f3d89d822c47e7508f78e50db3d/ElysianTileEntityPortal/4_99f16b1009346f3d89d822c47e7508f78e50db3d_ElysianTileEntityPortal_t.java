 package mods.elysium.dimension.portal;
 
 import java.util.Random;
 
 import mods.elysium.DefaultProps;
 import mods.elysium.Elysium;
 import net.minecraft.block.Block;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ChunkCoordinates;
 
 public class ElysianTileEntityPortal extends TileEntity
 {
 	public boolean canstay = true;
 	public ElysianPortalPosition coords;
 	byte tick;
 	public int timebeforetp = -1;
 	public boolean wasCollided = false;
 	public int ticksWithoutColliding = 0;
 	public float rotation = 0;
 	public float alpha;
 	public double radius;
 	
 	@Override
 	public void updateEntity()
 	{
 		rotation += 2.5F;
 		if(rotation >= 360) rotation -= 360;
 		
 		radius = Math.sin(Math.toRadians(rotation*4))/8 + 0.8;
 		alpha = (float) Math.sin(Math.toRadians(rotation*3))/4 + 0.5F;
 		
 		if(!wasCollided) ticksWithoutColliding++;
 		if(wasCollided) ticksWithoutColliding = 0;
 		if(ticksWithoutColliding > 5)
 		{
 			timebeforetp = -1;
 		}
 		if(timebeforetp > 0)
 		{
 			timebeforetp--;
 		}
 		wasCollided = false;
 		
 		if(coords == null)
 		{
 			coords = new ElysianPortalPosition(worldObj.provider.dimensionId, xCoord, yCoord, zCoord);
 		}
 		
 		tick++;
 		if(tick >= DefaultProps.ticksbeforeportalcheck)
 		{
 			tick = 0;
 			canstay = canStayPortal();
 		}
 		
 		if(canstay)
 		{
 			if(!ElysianTeleporter.portals.contains(coords))
 			{
 				ElysianTeleporter.portals.add(coords);
 			}
 		}
 		else
 		{
 			if(ElysianTeleporter.portals.contains(coords))
 			{
 				ElysianTeleporter.portals.remove(coords);
 			}
			if(!worldObj.isRemote)
				worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord, yCoord, zCoord, new ItemStack(Elysium.itemGracePrism)));
 			worldObj.setBlock(xCoord, yCoord, zCoord, Block.dragonEgg.blockID);
 		}
 	}
 	
 	public boolean canStayPortal()
 	{
 		boolean ret = true;
 		
 		for(int i=-2; i<=2; i++)
 		{
 			for(int j=-2; j<=2; j++)
 			{
 				if(worldObj.getBlockId(xCoord+i, yCoord-2, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-2, zCoord+j) != 0) ret = false;
 				if(worldObj.getBlockId(xCoord+i, yCoord-8, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-8, zCoord+j) != 0) ret = false;
 			}
 		}
 		for(int i=-1; i<=1; i++)
 		{
 			for(int j=-1; j<=1; j++)
 			{
 				if(worldObj.getBlockId(xCoord+i, yCoord-1, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-1, zCoord+j) != 1) ret = false;
 				
 				if(worldObj.getBlockId(xCoord+i, yCoord-3, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-3, zCoord+j) != 2) ret = false;
 				if(worldObj.getBlockId(xCoord+i, yCoord-4, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-4, zCoord+j) != 2) ret = false;
 				
 				if(worldObj.getBlockId(xCoord+i, yCoord-5, zCoord+j) != Block.blockGold.blockID) ret = false;
 				
 				if(worldObj.getBlockId(xCoord+i, yCoord-6, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-6, zCoord+j) != 2) ret = false;
 				if(worldObj.getBlockId(xCoord+i, yCoord-7, zCoord+j) != Block.blockNetherQuartz.blockID) ret = false;
 				if(worldObj.getBlockMetadata(xCoord+i, yCoord-7, zCoord+j) != 2) ret = false;
 			}
 		}
 		
 		return ret;
 	}
 }
