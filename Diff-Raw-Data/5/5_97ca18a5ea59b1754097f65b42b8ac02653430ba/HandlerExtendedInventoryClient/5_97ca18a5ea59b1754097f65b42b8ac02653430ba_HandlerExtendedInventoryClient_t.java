 package loecraftpack.ponies.inventory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.gui.GuiIds;
 import loecraftpack.enums.Race;
 import loecraftpack.packet.PacketHelper;
 import loecraftpack.packet.PacketIds;
 import loecraftpack.ponies.stats.StatHandlerServer;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.inventory.GuiContainerCreative;
 import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 @SideOnly(Side.CLIENT)
 public class HandlerExtendedInventoryClient
 {
 	static Map<String, InventoryEquipment> playerSpecialInv = new HashMap<String, InventoryEquipment>();
 	static Map<String, InventoryEarth> playerEarthInv = new HashMap<String, InventoryEarth>();
 	
 	/**
 	 * Called by Common Class, gets the player's custom inventory
 	 */
 	public static InventoryCustom getInventory(EntityPlayer player, InventoryId id)
 	{
 		InventoryCustom result;
 		switch (id)
 		{
 		case EQUIPMENT:
 			result = playerSpecialInv.get(player.username);
 			if (result == null)
 			{
 				result = new InventoryEquipment();
 				playerSpecialInv.put(player.username, (InventoryEquipment)result);
 			}
 			return result;
 			
 		case EARTH_PONY:
 			result = playerEarthInv.get(player.username);
 			if (result == null)
 			{
 				result = new InventoryEarth();
 				playerEarthInv.put(player.username, (InventoryEarth)result);
 			}
 			return result;
 			
 		default:
 			return null;
 		}
 	}
 	
 	/**
 	 * this sets in motion, the events to cycle the player's inventory.
 	 */
 	public static void cycleInventory()
 	{
 		if (Minecraft.getMinecraft().currentScreen != null)
 		{
 			//current screen
 			Class gui = Minecraft.getMinecraft().currentScreen.getClass();
 			GuiIds currentId = null;
 			if (gui == GuiInventory.class)
 			{
 				currentId = GuiIds.MAIN_INV;
 			}
 			else if (gui == GuiContainerCreative.class)
 			{
				if (((GuiContainerCreative)Minecraft.getMinecraft().currentScreen).func_74230_h()!=CreativeTabs.tabAllSearch.getTabIndex())
					currentId = GuiIds.CREATIVE_INV;
 			}
 			else if (gui == GuiSpecialEquipment.class)
 			{
 				currentId = GuiIds.EQUIPMENT_INV;
 			}
 			else if (gui == GuiEarthPonyInventory.class)
 			{
 				currentId = GuiIds.EARTH_INV;
 			}
 			//Attempt to cycle forward; this can lag for every thing except creative Inv.
 			if (currentId != null)
 			{				
 				PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.subInventory, currentId.ordinal()));
 			}
 		}
 	}
 }
