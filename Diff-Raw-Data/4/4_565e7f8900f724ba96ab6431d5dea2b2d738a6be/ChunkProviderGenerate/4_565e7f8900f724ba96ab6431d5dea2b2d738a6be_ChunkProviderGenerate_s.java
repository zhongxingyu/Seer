 package com.djdch.bukkit.onehundredgenerator.generator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 import com.djdch.bukkit.onehundredgenerator.configuration.WorldConfiguration;
 import com.djdch.bukkit.onehundredgenerator.mc100.BiomeBase;
 import com.djdch.bukkit.onehundredgenerator.mc100.NoiseGeneratorOctaves;
 import com.djdch.bukkit.onehundredgenerator.mc100.WorldChunkManager;
 import com.djdch.bukkit.onehundredgenerator.mc100.WorldGenLakes;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.BlockSand;
 import net.minecraft.server.Chunk;
 import net.minecraft.server.ChunkCoordIntPair;
 import net.minecraft.server.ChunkPosition;
 import net.minecraft.server.EnumCreatureType;
 import net.minecraft.server.IChunkProvider;
 import net.minecraft.server.IProgressUpdate;
 import net.minecraft.server.MathHelper;
 import net.minecraft.server.SpawnerCreature;
 import net.minecraft.server.World;
 
 public class ChunkProviderGenerate extends ChunkGenerator implements IChunkProvider {
     private Random n;
     private NoiseGeneratorOctaves o;
     private NoiseGeneratorOctaves p;
     private NoiseGeneratorOctaves q;
     private NoiseGeneratorOctaves r;
     public NoiseGeneratorOctaves a;
     public NoiseGeneratorOctaves b;
     public NoiseGeneratorOctaves c;
     private World s;
     @SuppressWarnings("unused")
     private boolean t;
     private double[] u;
     private double[] v = new double[256];
 
 //    private WorldGenBase w = new WorldGenCaves();
 //    public WorldGenStronghold d = new WorldGenStronghold();
 //    public WorldGenVillage e = new WorldGenVillage();
 //    public WorldGenMineshaft f = new WorldGenMineshaft();
 //    private WorldGenBase x = new WorldGenCanyon();
 
     private BiomeBase[] y;
     double[] g;
     double[] h;
     double[] i;
     double[] j;
     double[] k;
     float[] l;
     int[][] m = new int[32][32];
     protected WorldConfiguration worldSettings;
     protected WorldChunkManager worldChunkManager;
     protected ArrayList<BlockPopulator> populatorList;
 
     public void Init(World paramWorld, WorldChunkManager paramWorldChunkManager, long paramLong, boolean paramBoolean) {
         this.s = paramWorld;
         this.t = paramBoolean;
         this.worldChunkManager = paramWorldChunkManager;
 
         this.n = new Random(paramLong);
         this.o = new NoiseGeneratorOctaves(this.n, 16);
         this.p = new NoiseGeneratorOctaves(this.n, 16);
         this.q = new NoiseGeneratorOctaves(this.n, 8);
         this.r = new NoiseGeneratorOctaves(this.n, 4);
 
         this.a = new NoiseGeneratorOctaves(this.n, 10);
         this.b = new NoiseGeneratorOctaves(this.n, 16);
 
         this.c = new NoiseGeneratorOctaves(this.n, 8);
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public ChunkProviderGenerate(WorldConfiguration worldSettings) {
         this.worldSettings = worldSettings;
         this.worldSettings.chunkProvider = this;
         this.populatorList = new ArrayList();
         this.populatorList.add(new ObjectSpawner(this));
     }
 
 //    public ChunkProviderGenerate(World paramWorld, long paramLong, boolean paramBoolean) {
 //        this.s = paramWorld;
 //        this.t = paramBoolean;
 //
 //        this.n = new Random(paramLong);
 //        this.o = new NoiseGeneratorOctaves(this.n, 16);
 //        this.p = new NoiseGeneratorOctaves(this.n, 16);
 //        this.q = new NoiseGeneratorOctaves(this.n, 8);
 //        this.r = new NoiseGeneratorOctaves(this.n, 4);
 //
 //        this.a = new NoiseGeneratorOctaves(this.n, 10);
 //        this.b = new NoiseGeneratorOctaves(this.n, 16);
 //
 //        this.c = new NoiseGeneratorOctaves(this.n, 8);
 //    }
 
     public void a(int paramInt1, int paramInt2, byte[] paramArrayOfByte) {
         int i1 = 4;
         int i2 = this.s.height / 8;
         int i3 = this.s.seaLevel;
 
         int i4 = i1 + 1;
         int i5 = this.s.height / 8 + 1;
         int i6 = i1 + 1;
 
         this.y = this.worldChunkManager.getBiomes(this.y, paramInt1 * 4 - 2, paramInt2 * 4 - 2, i4 + 5, i6 + 5);
         this.u = a(this.u, paramInt1 * i1, 0, paramInt2 * i1, i4, i5, i6);
 
         for (int i7 = 0; i7 < i1; i7++)
             for (int i8 = 0; i8 < i1; i8++)
                 for (int i9 = 0; i9 < i2; i9++) {
                     double d1 = 0.125D;
                     double d2 = this.u[(((i7 + 0) * i6 + (i8 + 0)) * i5 + (i9 + 0))];
                     double d3 = this.u[(((i7 + 0) * i6 + (i8 + 1)) * i5 + (i9 + 0))];
                     double d4 = this.u[(((i7 + 1) * i6 + (i8 + 0)) * i5 + (i9 + 0))];
                     double d5 = this.u[(((i7 + 1) * i6 + (i8 + 1)) * i5 + (i9 + 0))];
 
                     double d6 = (this.u[(((i7 + 0) * i6 + (i8 + 0)) * i5 + (i9 + 1))] - d2) * d1;
                     double d7 = (this.u[(((i7 + 0) * i6 + (i8 + 1)) * i5 + (i9 + 1))] - d3) * d1;
                     double d8 = (this.u[(((i7 + 1) * i6 + (i8 + 0)) * i5 + (i9 + 1))] - d4) * d1;
                     double d9 = (this.u[(((i7 + 1) * i6 + (i8 + 1)) * i5 + (i9 + 1))] - d5) * d1;
 
                     for (int i10 = 0; i10 < 8; i10++) {
                         double d10 = 0.25D;
 
                         double d11 = d2;
                         double d12 = d3;
                         double d13 = (d4 - d2) * d10;
                         double d14 = (d5 - d3) * d10;
 
                         for (int i11 = 0; i11 < 4; i11++) {
                             int i12 = i11 + i7 * 4 << this.s.heightBitsPlusFour | 0 + i8 * 4 << this.s.heightBits | i9 * 8 + i10;
                             int i13 = 1 << this.s.heightBits;
                             i12 -= i13;
                             double d15 = 0.25D;
 
                             double d16 = d11;
                             double d17 = (d12 - d11) * d15;
                             d16 -= d17;
                             int tmp586_585 = 0;
                             for (int i14 = 0; i14 < 4; i14++) {
                                 if ((d16 += d17) > 0.0D) {
                                     int tmp553_552 = (i12 + i13);
                                     i12 = tmp553_552;
                                     paramArrayOfByte[tmp553_552] = (byte) Block.STONE.id;
                                 } else if (i9 * 8 + i10 < i3) {
                                     tmp586_585 = (i12 + i13);
                                     i12 = tmp586_585;
                                     paramArrayOfByte[tmp586_585] = (byte) Block.STATIONARY_WATER.id;
                                 } else {
                                     int tmp606_605 = (i12 + i13);
                                     i12 = tmp606_605;
                                     paramArrayOfByte[tmp606_605] = 0;
                                 }
                             }
                             d11 += d13;
                             tmp586_585 += d14;
                         }
 
                         d2 += d6;
                         d3 += d7;
                         d4 += d8;
                         d5 += d9;
                     }
                 }
     }
 
     public void a(int paramInt1, int paramInt2, byte[] paramArrayOfByte, BiomeBase[] paramArrayOfBiomeBase) {
         int i1 = this.s.seaLevel;
 
         double d1 = 0.03125D;
         this.v = this.r.a(this.v, paramInt1 * 16, paramInt2 * 16, 0, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);
 
         float[] arrayOfFloat = this.s.getWorldChunkManager().a(paramInt1 * 16, paramInt2 * 16, 16, 16);
 
         for (int i2 = 0; i2 < 16; i2++)
             for (int i3 = 0; i3 < 16; i3++) {
                 float f1 = arrayOfFloat[(i3 + i2 * 16)];
 
                 BiomeBase localBiomeBase = paramArrayOfBiomeBase[(i3 + i2 * 16)];
                 int i4 = (int) (this.v[(i2 + i3 * 16)] / 3.0D + 3.0D + this.n.nextDouble() * 0.25D);
 
                 int i5 = -1;
 
                 int i6 = localBiomeBase.t;
                 int i7 = localBiomeBase.u;
 
                 for (int i8 = this.s.heightMinusOne; i8 >= 0; i8--) {
                     int i9 = (i3 * 16 + i2) * this.s.height + i8;
 
                     if (i8 <= 0 + this.n.nextInt(5)) {
                         paramArrayOfByte[i9] = (byte) Block.BEDROCK.id;
                     } else {
                         int i10 = paramArrayOfByte[i9];
 
                         if (i10 == 0)
                             i5 = -1;
                         else if (i10 == Block.STONE.id)
                             if (i5 == -1) {
                                 if (i4 <= 0) {
                                     i6 = 0;
                                     i7 = (byte) Block.STONE.id;
                                 } else if ((i8 >= i1 - 4) && (i8 <= i1 + 1)) {
                                     i6 = localBiomeBase.t;
                                     i7 = localBiomeBase.u;
                                 }
 
                                 if ((i8 < i1) && (i6 == 0)) {
                                     if (f1 < 0.15F)
                                         i6 = (byte) Block.ICE.id;
                                     else {
                                         i6 = (byte) Block.STATIONARY_WATER.id;
                                     }
 
                                 }
 
                                 i5 = i4;
                                 if (i8 >= i1 - 1)
                                     paramArrayOfByte[i9] = (byte) i6;
                                 else
                                     paramArrayOfByte[i9] = (byte) i7;
                             } else if (i5 > 0) {
                                 i5--;
                                 paramArrayOfByte[i9] = (byte) i7;
 
                                 if ((i5 == 0) && (i7 == Block.SAND.id)) {
                                     i5 = this.n.nextInt(4);
                                     i7 = (byte) Block.SANDSTONE.id;
                                 }
                             }
                     }
                 }
             }
     }
 
     private double[] a(double[] paramArrayOfDouble, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
         if (paramArrayOfDouble == null) {
             paramArrayOfDouble = new double[paramInt4 * paramInt5 * paramInt6];
         }
         if (this.l == null) {
             this.l = new float[25];
             for (int i1 = -2; i1 <= 2; i1++) {
                 for (int i2 = -2; i2 <= 2; i2++) {
                     float f1 = 10.0F / MathHelper.c(i1 * i1 + i2 * i2 + 0.2F);
                     this.l[(i1 + 2 + (i2 + 2) * 5)] = f1;
                 }
             }
         }
 
         double d1 = 684.41200000000003D;
         double d2 = 684.41200000000003D;
 
         this.j = this.a.a(this.j, paramInt1, paramInt3, paramInt4, paramInt6, 1.121D, 1.121D, 0.5D);
         this.k = this.b.a(this.k, paramInt1, paramInt3, paramInt4, paramInt6, 200.0D, 200.0D, 0.5D);
 
         this.g = this.q.a(this.g, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1 / 80.0D, d2 / 160.0D, d1 / 80.0D);
         this.h = this.o.a(this.h, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);
         this.i = this.p.a(this.i, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);
         paramInt1 = paramInt3 = 0;
 
         int i3 = 0;
         int i4 = 0;
 
         for (int i5 = 0; i5 < paramInt4; i5++) {
             for (int i6 = 0; i6 < paramInt6; i6++) {
                 float f2 = 0.0F;
                 float f3 = 0.0F;
                 float f4 = 0.0F;
 
                 int i7 = 2;
 
                 BiomeBase localBiomeBase1 = this.y[(i5 + 2 + (i6 + 2) * (paramInt4 + 5))];
                 for (int i8 = -i7; i8 <= i7; i8++) {
                     for (int i9 = -i7; i9 <= i7; i9++) {
                         BiomeBase localBiomeBase2 = this.y[(i5 + i8 + 2 + (i6 + i9 + 2) * (paramInt4 + 5))];
                         float f5 = this.l[(i8 + 2 + (i9 + 2) * 5)] / (localBiomeBase2.w + 2.0F);
                         if (localBiomeBase2.w > localBiomeBase1.w) {
                             f5 /= 2.0F;
                         }
                         f2 += localBiomeBase2.x * f5;
                         f3 += localBiomeBase2.w * f5;
                         f4 += f5;
                     }
                 }
                 f2 /= f4;
                 f3 /= f4;
 
                 f2 = f2 * 0.9F + 0.1F;
                 f3 = (f3 * 4.0F - 1.0F) / 8.0F;
 
                 double d3 = this.k[i4] / 8000.0D;
                 if (d3 < 0.0D)
                     d3 = -d3 * 0.3D;
                 d3 = d3 * 3.0D - 2.0D;
 
                 if (d3 < 0.0D) {
                     d3 /= 2.0D;
                     if (d3 < -1.0D)
                         d3 = -1.0D;
                     d3 /= 1.4D;
                     d3 /= 2.0D;
                 } else {
                     if (d3 > 1.0D)
                         d3 = 1.0D;
                     d3 /= 8.0D;
                 }
 
                 i4++;
 
                 for (int i10 = 0; i10 < paramInt5; i10++) {
                     double d4 = f3;
                     double d5 = f2;
 
                     d4 += d3 * 0.2D;
                     d4 = d4 * paramInt5 / 16.0D;
 
                     double d6 = paramInt5 / 2.0D + d4 * 4.0D;
 
                     double d7 = 0.0D;
 
                     double d8 = (i10 - d6) * 12.0D * 128.0D / this.s.height / d5;
 
                     if (d8 < 0.0D)
                         d8 *= 4.0D;
 
                     double d9 = this.h[i3] / 512.0D;
                     double d10 = this.i[i3] / 512.0D;
 
                     double d11 = (this.g[i3] / 10.0D + 1.0D) / 2.0D;
                     if (d11 < 0.0D)
                         d7 = d9;
                     else if (d11 > 1.0D)
                         d7 = d10;
                     else
                         d7 = d9 + (d10 - d9) * d11;
                     d7 -= d8;
 
                     if (i10 > paramInt5 - 4) {
                         double d12 = (i10 - (paramInt5 - 4)) / 3.0F;
                         d7 = d7 * (1.0D - d12) + -10.0D * d12;
                     }
 
                     paramArrayOfDouble[i3] = d7;
                     i3++;
                 }
             }
         }
         return paramArrayOfDouble;
     }
 
     @Override
     public boolean isChunkLoaded(int paramInt1, int paramInt2) {
         return true;
     }
 
     @Override
     public Chunk getOrCreateChunk(int paramInt1, int paramInt2) {
         this.n.setSeed(paramInt1 * 341873128712L + paramInt2 * 132897987541L);
 
         byte[] arrayOfByte = new byte[16 * this.s.height * 16];
         Chunk localChunk = new Chunk(this.s, arrayOfByte, paramInt1, paramInt2);
 
         a(paramInt1, paramInt2, arrayOfByte);
         this.y = this.worldChunkManager.a(this.y, paramInt1 * 16, paramInt2 * 16, 16, 16);
         a(paramInt1, paramInt2, arrayOfByte, this.y);
 
 //        this.w.a(this, this.s, paramInt1, paramInt2, arrayOfByte);
 //        this.x.a(this, this.s, paramInt1, paramInt2, arrayOfByte);
 //        if (this.t) {
 //            this.f.a(this, this.s, paramInt1, paramInt2, arrayOfByte);
 //            this.e.a(this, this.s, paramInt1, paramInt2, arrayOfByte);
 //            this.d.a(this, this.s, paramInt1, paramInt2, arrayOfByte);
 //        }
 
         localChunk.initLighting();
 
         return localChunk;
     }
 
     @Override
     public Chunk getChunkAt(int paramInt1, int paramInt2) {
         return getOrCreateChunk(paramInt1, paramInt2);
     }
 
     @Override
     public void getChunkAt(IChunkProvider paramIChunkProvider, int paramInt1, int paramInt2) {
         BlockSand.instaFall = true;
         int i1 = paramInt1 * 16;
         int i2 = paramInt2 * 16;
 
         BiomeBase localBiomeBase = this.worldChunkManager.getBiome(i1 + 16, i2 + 16);
 
         this.n.setSeed(this.s.getSeed());
         long l1 = this.n.nextLong() / 2L * 2L + 1L;
         long l2 = this.n.nextLong() / 2L * 2L + 1L;
         this.n.setSeed(paramInt1 * l1 + paramInt2 * l2 ^ this.s.getSeed());
 
         boolean bool = false;
 
 //        if (this.t) {
 //            this.f.a(this.s, this.n, paramInt1, paramInt2);
 //            bool = this.e.a(this.s, this.n, paramInt1, paramInt2);
 //            this.d.a(this.s, this.n, paramInt1, paramInt2);
 //        }
         int i4;
         int i5;
         int i3;
         if ((!bool) && (this.n.nextInt(4) == 0)) {
             i3 = i1 + this.n.nextInt(16) + 8;
             i4 = this.n.nextInt(this.s.height);
             i5 = i2 + this.n.nextInt(16) + 8;
             new WorldGenLakes(Block.STATIONARY_WATER.id, this.worldChunkManager).a(this.s, this.n, i3, i4, i5);
         }
 
         if ((!bool) && (this.n.nextInt(8) == 0)) {
             i3 = i1 + this.n.nextInt(16) + 8;
             i4 = this.n.nextInt(this.n.nextInt(this.s.height - 8) + 8);
             i5 = i2 + this.n.nextInt(16) + 8;
             if ((i4 < this.s.seaLevel) || (this.n.nextInt(10) == 0))
                 new WorldGenLakes(Block.STATIONARY_LAVA.id, this.worldChunkManager).a(this.s, this.n, i3, i4, i5);
         }
 
 //        for (i3 = 0; i3 < 8; i3++) {
 //            i4 = i1 + this.n.nextInt(16) + 8;
 //            i5 = this.n.nextInt(this.s.height);
 //            int i6 = i2 + this.n.nextInt(16) + 8;
 //            if (!new WorldGenDungeons().a(this.s, this.n, i4, i5, i6)) {
 //                continue;
 //            }
 //        }
         localBiomeBase.a(this.s, this.n, i1, i2);
 
         SpawnerCreature.a(this.s, localBiomeBase, i1 + 8, i2 + 8, 16, 16, this.n);
 
         i1 += 8;
         i2 += 8;
         for (i3 = 0; i3 < 16; i3++) {
             for (i4 = 0; i4 < 16; i4++) {
                 i5 = this.s.e(i1 + i3, i2 + i4);
 
                 if (this.s.p(i3 + i1, i5 - 1, i4 + i2)) {
                     this.s.setTypeId(i3 + i1, i5 - 1, i4 + i2, Block.ICE.id);
                 }
                 if (this.s.r(i3 + i1, i5, i4 + i2)) {
                     this.s.setTypeId(i3 + i1, i5, i4 + i2, Block.SNOW.id);
                 }
             }
 
         }
 
         BlockSand.instaFall = false;
     }
 
     @Override
     public boolean saveChunks(boolean paramBoolean, IProgressUpdate paramIProgressUpdate) {
         return true;
     }
 
     @Override
     public boolean unloadChunks() {
         return false;
     }
 
     @Override
     public boolean canSave() {
         return true;
     }
 
     @Override
     @SuppressWarnings("rawtypes")
     public List a(EnumCreatureType paramEnumCreatureType, int paramInt1, int paramInt2, int paramInt3) {
         WorldChunkManager localWorldChunkManager = this.worldChunkManager;
         if (localWorldChunkManager == null) {
             return null;
         }
         BiomeBase localBiomeBase = localWorldChunkManager.a(new ChunkCoordIntPair(paramInt1 >> 4, paramInt3 >> 4));
         if (localBiomeBase == null) {
             return null;
         }
         return localBiomeBase.a(paramEnumCreatureType);
     }
 
     @Override
     public ChunkPosition a(World paramWorld, String paramString, int paramInt1, int paramInt2, int paramInt3) {
 //        if (("Stronghold".equals(paramString)) && (this.d != null)) {
 //            return this.d.a(paramWorld, paramInt1, paramInt2, paramInt3);
 //        }
         return null;
     }
 
     @Override
    public byte[] generate(org.bukkit.World arg0, Random arg1, int arg2, int arg3) {
        return null;
     }
 
     @Override
     public boolean canSpawn(org.bukkit.World world, int x, int z) {
         this.worldSettings.plugin.WorldInit(world);
 
         int i = ((CraftWorld) world).getHandle().a(x, z);
         return (i != 0) && (Block.byId[i].material.isSolid());
     }
 
     @Override
     public List<BlockPopulator> getDefaultPopulators(org.bukkit.World world) {
         return this.populatorList;
     }
 }
