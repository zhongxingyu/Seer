 package com.madpcgaming.madtech.core.proxy;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 import com.madpcgaming.madtech.MadTech;
 import com.madpcgaming.madtech.client.gui.GuiIndustrialFurnace;
 import com.madpcgaming.madtech.client.gui.GuiSimpleEFurnace;
 import com.madpcgaming.madtech.helpers.LogHelper;
 import com.madpcgaming.madtech.lib.GuiIds;
 import com.madpcgaming.madtech.tileentitys.TileEntityIndustrialFurnaceCore;
 import com.madpcgaming.madtech.tileentitys.TileSimpleEFurnace;
 
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 
 public class CommonProxy implements IGuiHandler
 {
 	
 	public void registerRenderings()
 	{
 		NetworkRegistry.INSTANCE.registerGuiHandler(MadTech.instance, this);
 	}
 	
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		LogHelper.info("Getting Server GUI!");
 		TileEntity t = world.func_147438_o(x, y, z);
 		/*if (t instanceof TileElectrolyser)
 		{
 			return new ElectrolyserContainer(player.inventory, (TileElectrolyser) t);
 		}*/
 		if (ID == GuiIds.FURNACE_CORE)
 		{
 			TileEntityIndustrialFurnaceCore tileIndustrialFurnaceCore = (TileEntityIndustrialFurnaceCore) world.func_147438_o(x, y, z);
			return new GuiIndustrialFurnace(player.inventory, tileIndustrialFurnaceCore);
 		}
 		else if (ID == GuiIds.FURNACE_SIMPLE)
 		{
 			TileSimpleEFurnace t1 = (TileSimpleEFurnace) t;
			return new GuiSimpleEFurnace(player.inventory, t1);
 		}
 		return null;
 	}
 	
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		LogHelper.info("Getting CLient GUI!");
 		TileEntity t = world.func_147438_o(x, y, z);
 		/*if (t instanceof TileElectrolyser)
 		{
 			return new ElectrolyserGUI(new ElectrolyserContainer(player.inventory, (TileElectrolyser) t), (TileElectrolyser) t);
 		}*/
 		if (ID == GuiIds.FURNACE_CORE)
 		{
 			//WHY DO I DECLARE T IF YOU DONT USE IT?
 			TileEntityIndustrialFurnaceCore tileIndustrialFurnaceCore = (TileEntityIndustrialFurnaceCore) world.func_147438_o(x, y, z);
 			return new GuiIndustrialFurnace(player.inventory, tileIndustrialFurnaceCore);
 		}
 		else if (ID == GuiIds.FURNACE_SIMPLE)
 		{
 			TileSimpleEFurnace t1 = (TileSimpleEFurnace) t;
 			return new GuiSimpleEFurnace(player.inventory, t1);
 		}
 		return null;
 	}
 }
