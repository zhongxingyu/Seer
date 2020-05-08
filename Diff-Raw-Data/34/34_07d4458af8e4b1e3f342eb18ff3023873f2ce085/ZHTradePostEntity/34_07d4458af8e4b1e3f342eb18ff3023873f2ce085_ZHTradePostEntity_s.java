 package zh.usefulthings.tileentities;
 
 
 
 import java.util.HashMap;
 import java.util.List;
 
 import zh.usefulthings.UsefulThings;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.village.MerchantRecipeList;
 
 public class ZHTradePostEntity extends TileEntity 
 {
 	private static int range = UsefulThings.tradePostRange.getInt();
 	private static int maxRecipes = UsefulThings.tradePostMaxTrades.getInt();
 	public int[] villagerIDs;
 	public MerchantRecipeList[] trades;
 	
 	public Packet getTradePacket(EntityPlayer player)
 	{
 		NBTTagCompound packet = new NBTTagCompound();
 		List villagers = worldObj.getEntitiesWithinAABB(EntityVillager.class, AxisAlignedBB.getAABBPool()
 				.getAABB(xCoord - range, 0.0d, zCoord - range, xCoord + range, 255.0d, zCoord + range)); //minX,minY,minZ,maxX,maxY,maxZ
 		
 		if(villagers == null || villagers.size() == 0)
 			return null;
 		
 		int numRecipes = 0;
 		packet.setInteger("pid", player.entityId);
 		
 		//Loop until we look at all villagers or until the recipe list fills up
 		for(int i = 0; i < villagers.size() && numRecipes <= maxRecipes; i++)
 		{
 			EntityVillager villager = (EntityVillager)villagers.get(i);
 			
 			if(!villager.isChild())
 			{
 				packet.setInteger("v" + i, villager.entityId);
 				packet.setCompoundTag("r" + i, villager.getRecipes(player).getRecipiesAsTags());
 				
 				numRecipes++;
 			}
 		}
 		
 		if(numRecipes == 0)
 			return null;
 		
 		packet.setInteger("num", numRecipes);
 		
 		return new Packet132TileEntityData(xCoord,yCoord,zCoord,4,packet);
 
 	}
 	
 	public void onDataPacket(INetworkManager network, Packet132TileEntityData packet)
 	{
 		if (worldObj.isRemote)
 			if(packet.customParam1.hasKey("pid"))
 				if(packet.customParam1.hasKey("num"))
 				{
 					int numTrades = packet.customParam1.getInteger("num");
 					villagerIDs = new int[numTrades];
 					trades = new MerchantRecipeList[numTrades];
 					
 					for (int i = 0; i< numTrades; i++)
 					{
 						villagerIDs[i] = packet.customParam1.getInteger("v" + i);
 						trades[i] = new MerchantRecipeList(packet.customParam1.getCompoundTag("r" + i));
 						
 						
 						
 					}
 					
 					Entity player = worldObj.getEntityByID(packet.customParam1.getInteger("pid"));
 					
 					if (player instanceof EntityPlayer)
 						((EntityPlayer)player).openGui(UsefulThings.instance, 0, worldObj, xCoord, yCoord, zCoord);
 				}
 					
 		
 		
 	}
 }
