 /**
  * Pony's Handy Dandy Rape Generator 3.0
  *
  * INSERT BSD HERE
  */
 package net.nexisonline.spade.chunkproviders;
 
 import java.util.Map;
 import java.util.Random;
 
 import net.nexisonline.spade.InterpolatedDensityMap;
 import net.nexisonline.spade.SpadeChunkProvider;
 import net.nexisonline.spade.SpadePlugin;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 
 import toxi.math.noise.SimplexNoise;
 
 /**
  * 
  * @author PrettyPonyyy
  * 
  * 
  */
 public class ChunkProviderGemini extends SpadeChunkProvider {
     private int WATER_HEIGHT = 32;
     private int OCEAN_FLOOR=16;
 
     SimplexNoise m_primaryNoise;
     SimplexNoise m_xTurbulence;
     SimplexNoise m_yTurbulence;
     SimplexNoise m_zTurbulence;
     SimplexNoise m_heightOffset;
 
     private InterpolatedDensityMap density;
 
     public ChunkProviderGemini(SpadePlugin plugin) 
     {
         super(plugin);
         density = new InterpolatedDensityMap();
     }
     
     @Override
     public void onLoad(String worldName, long worldSeed, Map<String, Object> map) 
     {
         super.onLoad(worldName,worldSeed,map);
         
         /** Configure **/
        WATER_HEIGHT=(Integer)map.get("water-level");
        OCEAN_FLOOR=(Integer)map.get("min-ocean-floor-height");
         
         try 
         {
             m_primaryNoise = new SimplexNoise(((int)worldSeed * 1024));
             m_primaryNoise.setFrequency(0.01f);
 
             m_xTurbulence = new SimplexNoise(((int)worldSeed * 1024) + 1);
             m_xTurbulence.setAmplitude(35);
             m_xTurbulence.setFrequency(0.01f);
 
             m_yTurbulence = new SimplexNoise(((int)worldSeed * 1024) + 2);
             m_yTurbulence.setAmplitude(35);
             m_yTurbulence.setFrequency(0.01f);  
 
             m_zTurbulence = new SimplexNoise(((int)worldSeed * 1024) + 3);
             m_zTurbulence.setAmplitude(35);
             m_zTurbulence.setFrequency(0.01f);
 
             m_heightOffset = new SimplexNoise(((int)worldSeed * 1024) + 4);
             m_heightOffset.setAmplitude(32);
             m_heightOffset.setFrequency(0.001f);            
         }
         catch (Exception e) 
         {
         }
     }
 
     @Override
     public byte[] generate(World world, Random rand, int X, int Z) 
     {
         byte[] blocks = new byte[16*128*16];
         // Shift the height up by quite a bit. It doesn't like to be very high.
         double heightOffset = m_heightOffset.sample(X, 0, Z) + 42;
         for (int x = 0; x < 16; x+=5) 
         {
             for (int y = 0; y <= 128; y += 16) 
             {
                 for (int z = 0; z < 16; z+=5) 
                 {
                     double posX = (x + (X * 16));
                     double posY = (y - 64);
                     double posZ = (z + (Z * 16));
 
                     float warpX = (float) (posX + m_xTurbulence.sample(posX, posY, posZ));
                     float warpY = (float) (posY + m_yTurbulence.sample(posX, posY, posZ));
                     float warpZ = (float) (posZ + m_zTurbulence.sample(posX, posY, posZ));
 
                     float fdensity = (float) m_primaryNoise.sample(warpX, warpY, warpZ);
                     fdensity -= (warpY - heightOffset) / 32;
 
                     density.setDensity(x, y, z, (double)fdensity);
                 }
             }
         }
 
         density.interpolate();
         double minDensity = Double.MAX_VALUE;
         double maxDensity = Double.MIN_VALUE;
 
         for (int x = 0; x < 16; ++x) 
         {
             for (int y = 0; y < 128; ++y) 
             {
                 for (int z = 0; z < 16; ++z) 
                 {
                     byte block = 0;
                     double d = density.getDensity(x, y, z);
                     maxDensity = Math.max(maxDensity, d);
                     minDensity = Math.min(minDensity, d);
 
                     if ((int) d > 0) 
                     {
                         block = 1;
                     } 
                     else 
                     {
                         block = (byte) ((y < WATER_HEIGHT) ? Material.STATIONARY_WATER.getId() : 0);
                     }
 
                     if (y == 1)
                         block = 7;
 
                     setBlockByte(blocks,x,y,z,block);
                 }
             }
         }
 
         return blocks;
     }
     
     @Override
     public Map<String,Object> getConfig() {
         Map<String,Object> cfg = super.getConfig();
         cfg.put("water-level", WATER_HEIGHT);
         cfg.put("min-ocean-floor-height", OCEAN_FLOOR);
         return cfg;
     }
 }
