 package xelitez.frostcraft.world;
 
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.EnumSkyBlock;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.Chunk;
 import xelitez.frostcraft.registry.IdMap;
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import cpw.mods.fml.common.gameevent.TickEvent;
 import cpw.mods.fml.common.gameevent.TickEvent.Phase;
 import cpw.mods.fml.relauncher.Side;
 
 public class WorldTicker
 {	
     protected int updateLCG = (new Random()).nextInt();
 
	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@SubscribeEvent
 	public void tickEnd(TickEvent.WorldTickEvent evt) 
 	{
 		if(evt.side == Side.SERVER && evt.phase == Phase.END)
 		{
 			World world = evt.world;
 			try
 			{
				Set set = new HashSet();
				set.addAll(world.getPersistentChunks().keySet());
		        for (int i = 0; i < world.playerEntities.size(); ++i)
		        {
		            EntityPlayer entityplayer = (EntityPlayer)world.playerEntities.get(i);
		            int j = MathHelper.floor_double(entityplayer.posX / 16.0D);
		            int k = MathHelper.floor_double(entityplayer.posZ / 16.0D);
		            byte b0 = 7;

		            for (int l = -b0; l <= b0; ++l)
		            {
		                for (int i1 = -b0; i1 <= b0; ++i1)
		                {
		                    set.add(new ChunkCoordIntPair(l + j, i1 + k));
		                }
		            }
		        }
				Iterator var3 = set.iterator();
 		        while (var3.hasNext())
 		        {
 		            ChunkCoordIntPair var4 = (ChunkCoordIntPair)var3.next();
 		            int var5 = var4.chunkXPos * 16;
 		            int var6 = var4.chunkZPos * 16;
 		            Chunk var7 = world.getChunkFromChunkCoords(var4.chunkXPos, var4.chunkZPos);
 		            if (world.provider.canDoRainSnowIce(var7) && world.rand.nextInt(48) == 0)
 		            {
 		                this.updateLCG = this.updateLCG * 3 + 1013904223;
 		                int var8 = this.updateLCG >> 2;
 		                int var9 = var8 & 15;
 		                int var10 = var8 >> 8 & 15;
 		                List<Integer> yPosses = WorldGenIcicles.getPossibleYPos(world, var9 + var5, var10 + var6);
 						if(yPosses.size() > 0)
 						{
 							int index = world.rand.nextInt(yPosses.size());
 							if(index >= yPosses.size())
 							{
 								index = yPosses.size() - 1;
 							}
 							int var11 = yPosses.get(index);
 							if (world.isRaining() && this.canSpawnIcicles(world, var9 + var5, var11, var10 + var6) && world.getSavedLightValue(EnumSkyBlock.Block, var9 + var5, var11, var10 + var6) < 11)
 							{
 								world.setBlock(var9 + var5, var11, var10 + var6, IdMap.blockIcicle);
 							}
 						}
 		            }
 		        }
 			}
 	        catch(Exception e)
 	        {
	        	e.printStackTrace();
 	        }
 		}
 	}
 
 	private boolean canSpawnIcicles(World world, int i, int j, int k) 
 	{
 		return IdMap.blockIcicle.canBlockStay(world, i, j, k) && WorldGenIcicles.getInstance().biomeList.contains(world.getBiomeGenForCoords(i, k));
 	}
 
 }
