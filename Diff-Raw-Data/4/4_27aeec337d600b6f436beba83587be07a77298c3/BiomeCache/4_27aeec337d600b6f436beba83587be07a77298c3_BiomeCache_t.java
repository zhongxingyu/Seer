 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.server.BiomeBase;
 import net.minecraft.server.BiomeCacheBlock;
 import net.minecraft.server.LongHashMap;
 
 public class BiomeCache extends net.minecraft.server.BiomeCache {
 
     private final WorldChunkManager a;
     private long b = 0L;
 
     private LongHashMap c = new LongHashMap();
     @SuppressWarnings("rawtypes")
     private List d = new ArrayList();
 
     public BiomeCache(WorldChunkManager paramWorldChunkManager) {
         super(paramWorldChunkManager);
         this.a = paramWorldChunkManager;
     }
 
     @SuppressWarnings("unchecked")
     public BiomeCacheBlock a(int paramInt1, int paramInt2) {
         paramInt1 >>= 4;
         paramInt2 >>= 4;
         long l = (long) paramInt1 & 0xffffffffL | ((long) paramInt2 & 0xffffffffL) << 32;
         BiomeCacheBlock localBiomeCacheBlock = (BiomeCacheBlock) this.c.getEntry(l);
         if (localBiomeCacheBlock == null) {
             localBiomeCacheBlock = new BiomeCacheBlock(this, paramInt1, paramInt2);
             this.c.put(l, localBiomeCacheBlock);
             this.d.add(localBiomeCacheBlock);
         }
         localBiomeCacheBlock.f = System.currentTimeMillis();
         return localBiomeCacheBlock;
     }
 
     public BiomeBase b(int paramInt1, int paramInt2) {
         return a(paramInt1, paramInt2).a(paramInt1, paramInt2);
     }
 
     public float c(int paramInt1, int paramInt2) {
         return a(paramInt1, paramInt2).b(paramInt1, paramInt2);
     }
 
     public void a() {
         long l = System.currentTimeMillis();
         long l1 = l - b;
         if (l1 > 7500L || l1 < 0L) {
             this.b = l;
 
             for (int i = 0; i < this.d.size(); i++) {
                 BiomeCacheBlock biomecacheblock = (BiomeCacheBlock) d.get(i);
                 long l2 = l - biomecacheblock.f;
                 if (l2 > 30000L || l2 < 0L) {
                     this.d.remove(i--);
                     long l3 = (long) biomecacheblock.d & 0xffffffffL | ((long) biomecacheblock.e & 0xffffffffL) << 32;
                     this.c.remove(l3);
                 }
             }
         }
     }
 
     public BiomeBase[] d(int paramInt1, int paramInt2) {
         return a(paramInt1, paramInt2).c;
     }
 
    public static WorldChunkManager a(BiomeCache paramBiomeCache) {
        return paramBiomeCache.a;
     }
 }
