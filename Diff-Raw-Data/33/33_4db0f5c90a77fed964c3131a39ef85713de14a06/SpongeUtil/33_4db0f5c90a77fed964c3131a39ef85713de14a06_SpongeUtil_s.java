 package me.MnC.MnC_SERVER_MOD.util;
 
 import java.util.List;
 
 import me.MnC.MnC_SERVER_MOD.MnC_SERVER_MOD;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 public class SpongeUtil 
 {
	private static final int CLEAR_WATER_RADIUS = 4;
 	private static final FixedMetadataValue USED_SPONGE_METADATA = new FixedMetadataValue(MnC_SERVER_MOD.getInstance(), true);
 	private static final String USED_SPONGE_METADATA_KEY = "mnc.plugin.sponge";
 	
 	public static void clearWater(World world, int x, int y, int z)
 	{
		for (int cx = -CLEAR_WATER_RADIUS; cx <= CLEAR_WATER_RADIUS; cx++) 
 		{
			for (int cy = -CLEAR_WATER_RADIUS; cy <=CLEAR_WATER_RADIUS; cy++) 
 			{
				for (int cz = -CLEAR_WATER_RADIUS; cz <= CLEAR_WATER_RADIUS; cz++) 
 				{
 					if (world.getBlockTypeIdAt(x + cx, y + cy, z + cz) == 8 || world.getBlockTypeIdAt(x + cx, y + cy, z + cz) == 9) //isWater?
 					{
 							world.getBlockAt(x + cx, y + cy, z + cz).setTypeId(0);
 					}
 				}
 			}
 		}
 	}
 	
 	public static void setUsedAsSponge(Block sponge)
 	{
 		sponge.setType(Material.SAND);
 		sponge.setMetadata(USED_SPONGE_METADATA_KEY, USED_SPONGE_METADATA);
 	}
 	
 	public static boolean wasUsedAsSponge(Block block)
 	{
 		List<MetadataValue> metaList = block.getMetadata(USED_SPONGE_METADATA_KEY);
 		if(metaList == null || metaList.isEmpty())
 		{
 			return false;
 		}
 		for(MetadataValue m : metaList)
 		{
 			if(m.equals(USED_SPONGE_METADATA) || m == USED_SPONGE_METADATA)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 }
