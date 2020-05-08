 package com.shadows.Cooling.sided;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 
 import com.shadows.Cooling.TE.TECooler;
 import com.shadows.Cooling.inv.CoolerC;
 import com.shadows.Cooling.inv.GUICooler;
 import com.shadows.Cooling.utils.Registry;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ServerProxy {
 
 	public void initTE() {
 		GameRegistry.registerTileEntity(TECooler.class, Registry.CoolerTE);
 	}
 
 	public Object getServerGui(int ID, EntityPlayer player, World world, int x,
 			int y, int z) {
 
 		if (ID == Registry.CoolerID) {
 			TECooler CoolerTE = (TECooler) world.getBlockTileEntity(x, y, z);
			return new CoolerC(player.inventory, CoolerTE);
 		}
 
 		return null;
 	}
 
 	public Object getClientGui(int ID, EntityPlayer player, World world, int x,
 			int y, int z) {
 
 		if (ID == Registry.CoolerID) {
 			TECooler CoolerTE = (TECooler) world.getBlockTileEntity(x, y, z);
			return new GUICooler(player.inventory, CoolerTE);
 		}
 		return null;
 	}
 }
