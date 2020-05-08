 package Oskar13.TheCharacters;
 
 import Oskar13.OskarStart;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.nbt.NBTTagCompound;
 import cpw.mods.fml.common.FMLCommonHandler;
 
 public class NBTRead {
//1-18
 	public NBTRead(String nick) { 
 		
 		
 		OskarStart.debug("Poprawne uruchomienie NBTREAD");
 		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(nick);
 		NBTTagCompound oldnbt = player.getEntityData();
 		NBTTagCompound nbt = oldnbt.getCompoundTag("Characters");
 		
 		
 		if(nbt.hasKey("hp")) {
 			
 			
 			Characters.getPlayer(nick).getStats().hp = nbt.getInteger("hp");
 			Characters.getPlayer(nick).getStats().mp = nbt.getInteger("mp");
 			Characters.getPlayer(nick).getStats().def = nbt.getInteger("def");
 			Characters.getPlayer(nick).getStats().str = nbt.getInteger("str");
 			Characters.getPlayer(nick).getStats().dex = nbt.getInteger("dex");
 			Characters.getPlayer(nick).getStats().modelName = nbt.getString("modelName");
 			
 		} else { 
 			
 			new Stats();
 			OskarStart.sendStats(nick, Characters.getPlayer(nick).getStats());
 			
 		}
 		
 		
 		
 	}
 	
 }
