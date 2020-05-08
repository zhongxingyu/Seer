 package Oskar13.TheCharacters;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.nbt.NBTTagCompound;
 import Oskar13.OskarStart;
 
 public class NBTSave {

 	public NBTSave(String nick)  {
 		
 		
 		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(nick);
 		NBTTagCompound oldNBT = player.getEntityData();
 		NBTTagCompound nbt = oldNBT.getCompoundTag("Characters");
 		
 		if(!oldNBT.hasKey("Characters")) {
 			oldNBT.setCompoundTag("Characters", nbt);
 		}
 		
 		
 	Stats stat  = 	Characters.getPlayer(nick).getStats();
 		
 		nbt.setInteger("str", stat.str);
 		nbt.setInteger("dex", stat.dex);
 		nbt.setInteger("def", stat.def);
 		nbt.setInteger("hp", stat.hp);
 		nbt.setInteger("mp", stat.mp);
 	    nbt.setString("modelName", stat.modelName);
 
 	}
 }
