 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.server.LongHashMap;
 
 public class BiomeCache {
     private final WorldChunkManager a;
     private long b = 0L;
 
     private LongHashMap c = new LongHashMap();
    @SuppressWarnings("rawtypes")
     private List d = new ArrayList();
 
     public BiomeCache(WorldChunkManager paramWorldChunkManager) {
         this.a = paramWorldChunkManager;
     }
 
    @SuppressWarnings("unchecked")
     public BiomeCacheBlock a(int paramInt1, int paramInt2) {
         paramInt1 >>= 4;
         paramInt2 >>= 4;
         long l = paramInt1 & 0xFFFFFFFF | (paramInt2 & 0xFFFFFFFF) << 32;
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
         long l1 = System.currentTimeMillis();
         long l2 = l1 - this.b;
         if ((l2 > 7500L) || (l2 < 0L)) {
             this.b = l1;
 
             for (int i = 0; i < this.d.size(); i++) {
                 BiomeCacheBlock localBiomeCacheBlock = (BiomeCacheBlock) this.d.get(i);
                 long l3 = l1 - localBiomeCacheBlock.f;
                 if ((l3 <= 30000L) && (l3 >= 0L))
                     continue;
                 this.d.remove(i--);
                 long l4 = localBiomeCacheBlock.d & 0xFFFFFFFF | (localBiomeCacheBlock.e & 0xFFFFFFFF) << 32;
                 this.c.remove(l4);
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
