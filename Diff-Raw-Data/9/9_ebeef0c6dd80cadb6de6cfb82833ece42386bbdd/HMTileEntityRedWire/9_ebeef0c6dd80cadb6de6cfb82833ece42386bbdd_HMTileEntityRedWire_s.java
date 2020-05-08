 
 package hawksmachinery.redstone.common.tileentity;
 
 import hawksmachinery.core.common.api.HMVector;
 import hawksmachinery.core.common.tileentity.HMTileEntity;
 import java.awt.Color;
 import net.minecraft.block.Block;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMTileEntityRedWire extends HMTileEntity
 {
 	public Color wireColor;
 	
 	public int redstoneStrength;
 	
 	public boolean[] connectedSides = new boolean[6];
 	
 	@Override
 	public void updateEntity()
 	{
 		for (int counter = 0; counter < 6; ++counter)
 		{
 			ForgeDirection dir = ForgeDirection.getOrientation(counter);
 			Block block = this.selfVec.getBlockWithDir(dir);
 			TileEntity tileEntity = this.selfVec.getTileEntityWithDir(dir);
 			
 			if (tileEntity != null)
 			{
 				if (tileEntity instanceof HMTileEntityRedWire)
 				{
 					if (((HMTileEntityRedWire)tileEntity).isColored() && this.isColored())
 					{
 						this.connectedSides[counter] = ((HMTileEntityRedWire)tileEntity).wireColor.equals(this.wireColor);
 						
 					}
 					
					this.connectedSides[counter] = true;
					this.redstoneStrength = Math.max(this.redstoneStrength, ((HMTileEntityRedWire)tileEntity).redstoneStrength - 1);
 					continue;
 				}
 				
 			}
 			
 			if (block != null)
 			{
 				if (block.canProvidePower())
 				{
 					this.connectedSides[counter] = true;
 					continue;
 				}
 				else
 				{
 					if (counter > 0)
 					{
 						if (block.canConnectRedstone(this.worldObj, this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ, counter - 2))
 						{
 							this.connectedSides[counter] = true;
 							continue;
 						}
 						
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	public boolean isColored()
 	{
 		return this.wireColor != null;
 	}
 	
 }
