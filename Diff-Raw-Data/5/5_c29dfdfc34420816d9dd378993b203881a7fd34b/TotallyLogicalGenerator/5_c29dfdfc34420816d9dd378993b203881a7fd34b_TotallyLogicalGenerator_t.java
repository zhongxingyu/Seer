 package com.stuzzhosting.totallylogical;
 
 import java.util.Random;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.noise.SimplexNoiseGenerator;
 
 class TotallyLogicalGenerator extends ChunkGenerator {
 	private static final byte BEDROCK = (byte) Material.BEDROCK.getId();
 	private static final byte STONE = (byte) Material.MONSTER_EGGS.getId();
 
 	@Override
 	public byte[][] generateBlockSections( World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes ) {
 		byte[][] result = new byte[world.getMaxHeight() >> 4][];
 
 		Random seed = new Random(world.getSeed());
 		SimplexNoiseGenerator exists = new SimplexNoiseGenerator( seed );
 		SimplexNoiseGenerator tall = new SimplexNoiseGenerator( seed );
 
 		for ( int _x = 0; _x < 16; _x++ ) {
 			for ( int _z = 0; _z < 16; _z++ ) {
 				biomes.setBiome( _x, _z, Biome.EXTREME_HILLS );
 
 				int x = _x + ( chunkX << 4 ), z = _z + ( chunkZ << 4 );
 
 				if ( exists.noise( x / 100.0, z / 100.0 ) < 0.2 ) {
 					boolean isTall = Math.abs( tall.noise( x / 20.0, z / 20.0 ) ) < 0.2;
 					for (int y = 0; y < world.getMaxHeight() / (isTall ? 1 : 4); y++) {
 						if (y <= random.nextInt(6)) {
 							setBlock( result, x, y, z, BEDROCK );
 							continue;
 						}
 
 						setBlock( result, x, y, z, STONE );
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	private static void setBlock( byte[][] result, int x, int y, int z, byte blkid ) {
 		if ( result[y >> 4] == null && blkid != 0 ) {
 			result[y >> 4] = new byte[16 * 16 * 16];
 		}
 		result[y >> 4][( ( y & 0xF ) << 8 ) | ( ( z & 0xF ) << 4 ) | ( x & 0xF )] = blkid;
 	}
 
 	private static byte getBlock( byte[][] result, int x, int y, int z ) {
 		if ( result[y >> 4] == null ) {
 			return (byte) 0;
 		}
 		return result[y >> 4][( ( y & 0xF ) << 8 ) | ( ( z & 0xF ) << 4 ) | ( x & 0xF )];
 	}
	@Override
	public boolean canSpawn( World world, int x, int z ) {
		return world.getHighestBlockYAt( x, z ) < 128;
	}
 }
