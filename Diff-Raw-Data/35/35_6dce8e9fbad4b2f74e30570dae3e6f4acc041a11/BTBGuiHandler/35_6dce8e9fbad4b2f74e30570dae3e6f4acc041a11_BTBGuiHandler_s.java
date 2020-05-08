 package deathrat.mods.btbees.gui;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.IGuiHandler;
 import deathrat.mods.btbees.blocks.TileEntityWok;
 
 public class BTBGuiHandler implements IGuiHandler
 {
 
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityWok)
 		{
			return new ContainerWok(player.inventory, (TileEntityWok) tileEntity);
 		}
 		return null;
 	}
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityWok)
 		{
			return new GuiWok(player.inventory, (TileEntityWok) tileEntity);
 		}
 		return null;
 	}
 }
