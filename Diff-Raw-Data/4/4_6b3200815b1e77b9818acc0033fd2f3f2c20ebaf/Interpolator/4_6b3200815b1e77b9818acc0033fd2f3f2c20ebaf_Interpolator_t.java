 package net.nexisonline.spade;
 
 public class Interpolator {
 	private static int arrayindex(int x, int y, int z) {
 		return x << 11 | z << 7 | y;
 	}
 	/**
 	 * Linear Interpolator
 	 * @author PrettyPony <prettypony@7chan.org>
 	 */
 	public static byte[] LinearExpand(byte[] abyte) {
 		// Generate the xy and yz planes of blocks by interpolation
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y += 3)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					if (y != 15)
 					{
						abyte[arrayindex(x , y + 1, z)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x, y + 3, z)], 0.02f);
						abyte[arrayindex(x, y + 2, z)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x, y + 3, z)], 0.98f);
 					}
 				}
 			}
 		}
 
 		// Generate the xz plane of blocks by interpolation
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y++)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					if (x == 0 && z > 0)
 					{
 						abyte[arrayindex(x, y, z - 1)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x, y, z - 3)], 0.25f);
 						abyte[arrayindex(x, y, z - 2)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x, y, z - 3)], 0.85f);
 					}
 					else if (x > 0 && z > 0)
 					{
 						abyte[arrayindex(x - 1, y, z)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z)], 0.25f);
 						abyte[arrayindex(x - 2, y, z)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z)], 0.85f);
 
 						abyte[arrayindex(x, y, z - 1)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x, y, z - 3)], 0.25f);
 						abyte[arrayindex(x - 1, y, z - 1)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z - 3)], 0.25f);
 						abyte[arrayindex(x - 2, y, z - 1)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z - 3)], 0.85f);
 
 						abyte[arrayindex(x, y, z - 2)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x, y, z - 3)], 0.85f);
 						abyte[arrayindex(x - 1, y, z - 2)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z - 3)], 0.25f);
 						abyte[arrayindex(x - 2, y, z - 2)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z - 3)], 0.85f);
 					}
 					else if (x > 0 && z == 0)
 					{
 						abyte[arrayindex(x - 1, y, z)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z)], 0.25f);
 						abyte[arrayindex(x - 2, y, z)] = lerp(abyte[arrayindex(x, y, z)], abyte[arrayindex(x - 3, y, z)], 0.85f);
 					}
 				}
 			}
 		}
 		return abyte;
 	}
 	private static byte lerp(byte a, byte b, float f) {
 		return (byte) (a+(b-a)*f);
 	}
 }
