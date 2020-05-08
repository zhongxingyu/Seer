 /**
  * Pony's Handy Dandy Rape Generator
  *
  * INSERT BSD HERE
  */
 package net.nexisonline.spade.chunkproviders;
 
 import java.util.Random;
 import java.util.logging.Logger;
 
 import libnoiseforjava.module.Perlin;
 import net.minecraft.server.BiomeBase;
 import net.minecraft.server.BlockSand;
 import net.minecraft.server.NoiseGeneratorOctaves;
 import net.minecraft.server.WorldGenCactus;
 import net.minecraft.server.WorldGenClay;
 import net.minecraft.server.WorldGenDungeons;
 import net.minecraft.server.WorldGenFlowers;
 import net.minecraft.server.WorldGenLakes;
 import net.minecraft.server.WorldGenLiquids;
 import net.minecraft.server.WorldGenMinable;
 import net.minecraft.server.WorldGenPumpkin;
 import net.minecraft.server.WorldGenReed;
 import net.minecraft.server.WorldGenerator;
 import net.nexisonline.spade.SpadeChunkProvider;
 import net.nexisonline.spade.SpadePlugin;
 
 import org.bukkit.Material;
 import org.bukkit.block.Biome;
 import org.bukkit.craftbukkit.util.BiomeUtils;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 /**
  * @author PrettyPonyyy
  *
  */
 public class ChunkProviderDoublePerlin extends SpadeChunkProvider
 {
 	private static final int WATER_HEIGHT = 32;
 	private double[] r = new double[256];
 	private double[] s = new double[256];
 	private double[] t = new double[256];
 	private NoiseGeneratorOctaves n;
 	private NoiseGeneratorOctaves o;
 	private Random j;
 	private NoiseGeneratorOctaves c;
 	net.minecraft.server.World p=null;
 	
 	private Perlin m_perlinGenerator;
 	private Perlin m_fractalGenerator;
 	private SpadePlugin plugin;
 	private int distanceSquared;
 	
 	public ChunkProviderDoublePerlin(SpadePlugin plugin) {
 		this.plugin=plugin;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.bukkit.ChunkProvider#onLoad(org.bukkit.World, long)
 	 */
 	@Override
 	public void onLoad(Object world, long seed)
 	{
 		this.setHasCustomTerrain(true);
 		this.setHasCustomSedimenter(true);
 		this.setHasCustomPopulator(true);
 
 		try {
 			this.p = (net.minecraft.server.World)world;
 		} catch(Throwable e) {}
 
 		try
 		{
 			m_perlinGenerator = new Perlin(); //new Perlin();
 			m_fractalGenerator = new Perlin(); //new Perlin();
 
 			m_perlinGenerator.setSeed((int)(seed*1024));
 			m_perlinGenerator.setOctaveCount(1);
 			m_perlinGenerator.setFrequency(1f);
 
 			m_fractalGenerator.setSeed((int)(seed*1024) + 55);
 			m_fractalGenerator.setOctaveCount(1);
 			m_fractalGenerator.setFrequency(3f);
 			
 			this.j = new Random(seed+77);
 			this.n = new NoiseGeneratorOctaves(this.j, 4);
 			this.o = new NoiseGeneratorOctaves(this.j, 4);
 			this.c = new NoiseGeneratorOctaves(this.j, 8);
 		}
 		catch (Exception e)
 		{
 		}
 	}
 
 	private static double lerp(double a, double b, double f)
 	{
 		return (a * (1 - f) + b * f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.bukkit.ChunkProvider#generateChunk(int, int, byte[],
 	 * org.bukkit.block.Biome[], double[])
 	 */
 	@Override
 	public void generateChunk(Object world, int X, int Z, byte[] blocks, Biome[] biomes, double[] temperature)
 	{
 		double dist = this.plugin.getChunkDistanceToSpawn(this.worldName,X,Z);
 		if(dist>this.distanceSquared) {
 			blocks=new byte[blocks.length];
			Logger.getLogger("Minecraft").info(String.format("[wat] SKIPPING Chunk (%d,%d) (%d>%d)",X,Z,(int)dist,(int)distanceSquared));
 			return;
 		}
 		double density[][][] = new double[16][128][16];
 
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y += 3)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					double posX = /*Math.abs*/(x + (X*16));
 					double posY = /*Math.abs*/(y - 96);
 					double posZ = /*Math.abs*/(z + (Z*16));
 
 					final double warp = 0.004;
 					double warpMod = m_fractalGenerator.getValue(posX * warp, posY * warp, posZ * warp) * 5;
 					double warpPosX = posX * warpMod;
 					double warpPosY = posY * warpMod;
 					double warpPosZ = posZ * warpMod;
 
 					double mod = m_perlinGenerator.getValue(warpPosX * 0.0005, warpPosY * 0.0005, warpPosZ * 0.005);
 
 					density[x][y][z] = -(y - 64);
 					density[x][y][z] += mod * 100;
 				}
 			}
 		}
 
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y += 3)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					if (y != 126)
 					{
 						density[x][y+1][z] = lerp(density[x][y][z], density[x][y+3][z], 0.2);
 						density[x][y+2][z] = lerp(density[x][y][z], density[x][y+3][z], 0.8);
 					}
 				}
 			}
 		}
 
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y++)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					if (x == 0 && z > 0)
 					{
 						density[x][y][z-1] = lerp(density[x][y][z], density[x][y][z-3], 0.25);
 						density[x][y][z-2] = lerp(density[x][y][z], density[x][y][z-3], 0.85);
 					}
 					else if (x > 0 && z > 0)
 					{
 						density[x-1][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.25);
 						density[x-2][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.85);
 
 						density[x][y][z-1] = lerp(density[x][y][z], density[x][y][z-3], 0.25);
 						density[x-1][y][z-1] = lerp(density[x][y][z], density[x-3][y][z-3], 0.25);
 						density[x-2][y][z-1] = lerp(density[x][y][z], density[x-3][y][z-3], 0.85);
 
 						density[x][y][z-2] = lerp(density[x][y][z], density[x][y][z-3], 0.25);
 						density[x-1][y][z-2] = lerp(density[x][y][z], density[x-3][y][z-3], 0.85);
 						density[x-2][y][z-2] = lerp(density[x][y][z], density[x-3][y][z-3], 0.85);
 					}
 					else if (x > 0 && z == 0)
 					{
 						density[x-1][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.25);
 						density[x-2][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.85);
 					}
 				}
 			}
 		}
 
 		for (int x = 0; x < 16; x++)
 		{
 			for (int y = 0; y < 128; y++)
 			{
 				for (int z = 0; z < 16; z++)
 				{
 					byte block = 0;
 					if (density[x][y][z] > 0)
 					{
 						block = 1;
 					}
 					else
 					{
 						block = (byte) ((y<WATER_HEIGHT) ? Material.STATIONARY_WATER.getId() : 0);
 					}
 					// Origin point + sand to prevent 5000 years of loading.
 					if ((x == 0) && (z == 0) && (X == x) && (Z == z) && (y <= 63)) {
 						block = (byte) ((y == 125) ? 12 : 7);
 					}
 					if(y==1)
 						block=7;
 					blocks[getBlockIndex(x,y,z)]=block;
 				}
 			}
 		}
 
		Logger.getLogger("Minecraft").info(String.format("[wat] Chunk (%d,%d) (%d<=%d)",X,Z,(int)dist,(int)distanceSquared));
 	}
 	
 	/**
 	 * Stolen standard terrain populator, screwed with to generate water at the desired height.
 	 */
 	@Override
 	public void generateSediment(Object world, int X, int Z, byte[] blocks, Biome[] biomes) {
 		if(this.plugin.getChunkDistanceToSpawn(this.worldName,X,Z)>this.distanceSquared) {
 			blocks=new byte[blocks.length];
 			return;
 		}		
 		double var6 = 0.03125D;
 		this.r = this.n.a(this.r, (double)(X * 16), (double)(Z * 16), 0.0D, 16, 16, 1, var6, var6, 1.0D);
 		this.s = this.n.a(this.s, (double)(X * 16), 109.0134D, (double)(Z * 16), 16, 1, 16, var6, 1.0D, var6);
 		this.t = this.o.a(this.t, (double)(X * 16), (double)(Z * 16), 0.0D, 16, 16, 1, var6 * 2.0D, var6 * 2.0D, var6 * 2.0D);
 
 		for(int x = 0; x < 16; ++x) {
 			for(int z = 0; z < 16; ++z) {
 
 				double columnDist=this.plugin.getBlockDistanceToSpawn(this.worldName,x+(X*16),0,z+(Z*16));
 				BiomeBase biome = BiomeUtils.biome2BiomeBase(biomes[x + z * 16]);
 				boolean var11 = this.r[x + z * 16] + this.j.nextDouble() * 0.2D > 0.0D;
 				boolean var12 = this.s[x + z * 16] + this.j.nextDouble() * 0.2D > 3.0D;
 				int var13 = (int)(this.t[x + z * 16] / 3.0D + 3.0D + this.j.nextDouble() * 0.25D);
 				int var14 = -1;
 				byte grass = biome.o;
 				byte soil = biome.p;
 
 				for(int y = 127; y >= 0; --y) {
 					int idx = (z * 16 + x) * 128 + y;
 					if(columnDist==(int)(this.distanceSquared-(8^2))) {
 						blocks[idx]=7; // Bedrock
 						continue;
 					}else if(columnDist>(int)(this.distanceSquared-(8^2))) {
 						blocks[idx]=0; // Air
 						continue;
 					}
 					if(y <= 0 + this.j.nextInt(5)) {
 						blocks[idx] = (byte)Material.BEDROCK.getId();
 					} else {
 						byte var19 = blocks[idx];
 						if(var19 == 0) {
 							var14 = -1;
 						} else if(var19 == Material.STONE.getId()) {
 							if(var14 == -1) {
 								if(var13 <= 0) {
 									grass = 0;
 									soil = (byte)Material.STONE.getId();
 								} else if(y >= WATER_HEIGHT - 4 && y <= WATER_HEIGHT + 1) {
 									grass = biome.o;
 									soil = biome.p;
 									if(var12) {
 										grass = 0;
 									}
 
 									if(var12) {
 										soil = (byte)Material.GRAVEL.getId();
 									}
 
 									if(var11) {
 										grass = (byte)Material.SAND.getId();
 									}
 
 									if(var11) {
 										soil = (byte)Material.SAND.getId();
 									}
 								}
 
 								if(y < WATER_HEIGHT && grass == 0) {
 									grass = (byte)Material.STATIONARY_WATER.getId();
 								}
 
 								var14 = var13;
 								if(y >= WATER_HEIGHT - 1) {
 									blocks[idx] = grass;
 								} else {
 									blocks[idx] = soil;
 								}
 							} else if(var14 > 0) {
 								--var14;
 								blocks[idx] = soil;
 								if(var14 == 0 && soil == Material.SAND.getId()) {
 									var14 = this.j.nextInt(4);
 									soil = (byte)Material.SANDSTONE.getId();
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 	}
 	
 	public void populateChunk(Object ch,int X, int Z) {
 		if(this.plugin.getChunkDistanceToSpawn(this.worldName,X,Z)>this.distanceSquared) {
 			return;
 		}
 		BlockSand.a = true;
 		int var4 = X * 16;
 		int var5 = Z * 16;
 		BiomeBase var6 = this.p.a().a(var4 + 16, var5 + 16);
 		this.j.setSeed(this.p.k());
 		long var7 = this.j.nextLong() / 2L * 2L + 1L;
 		long var9 = this.j.nextLong() / 2L * 2L + 1L;
 		this.j.setSeed((long)X * var7 + (long)Z * var9 ^ this.p.k());
 		double var11 = 0.25D;
 		int var13;
 		int x;
 		int y;
 		if(this.j.nextInt(4) == 0) {
 			var13 = var4 + this.j.nextInt(16) + 8;
 			x = this.j.nextInt(128);
 			y = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenLakes(Material.STATIONARY_WATER.getId())).a(this.p, this.j, var13, x, y);
 		}
 
 		if(this.j.nextInt(8) == 0) {
 			var13 = var4 + this.j.nextInt(16) + 8;
 			x = this.j.nextInt(this.j.nextInt(120) + 8);
 			y = var5 + this.j.nextInt(16) + 8;
 			if(x < 64 || this.j.nextInt(10) == 0) {
 				(new WorldGenLakes(Material.STATIONARY_LAVA.getId())).a(this.p, this.j, var13, x, y);
 			}
 		}
 
 		int z;
 		for(var13 = 0; var13 < 8; ++var13) {
 			x = var4 + this.j.nextInt(16) + 8;
 			y = this.j.nextInt(128);
 			z = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenDungeons()).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 10; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(128);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenClay(32)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 20; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(128);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.DIRT.getId(), 32)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 10; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(128);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.GRAVEL.getId(), 32)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 20; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(128);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.COAL_ORE.getId(), 16)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 20; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(64);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.IRON_ORE.getId(), 8)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 2; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(32);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.GOLD_ORE.getId(), 8)).a(this.p, this.j, x, y, z);
 		}
 
 		// ADDED GLOWSTONE, MAX OF 8 PER CHUNK
 		for(var13 = 0; var13 < 8; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(32);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.GLOWSTONE.getId(), 32)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 8; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(16);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.REDSTONE_ORE.getId(), 7)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 1; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(16);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.DIAMOND_ORE.getId(), 7)).a(this.p, this.j, x, y, z);
 		}
 
 		for(var13 = 0; var13 < 1; ++var13) {
 			x = var4 + this.j.nextInt(16);
 			y = this.j.nextInt(16) + this.j.nextInt(16);
 			z = var5 + this.j.nextInt(16);
 			(new WorldGenMinable(Material.LAPIS_ORE.getId(), 6)).a(this.p, this.j, x, y, z);
 		}
 
 		var11 = 0.5D;
 		var13 = (int)((this.c.a((double)var4 * var11, (double)var5 * var11) / 8.0D + this.j.nextDouble() * 4.0D + 4.0D) / 3.0D);
 		x = 0;
 		if(this.j.nextInt(10) == 0) {
 			++x;
 		}
 
 		if(var6 == BiomeBase.FOREST) {
 			x += var13 + 5;
 		}
 
 		if(var6 == BiomeBase.RAINFOREST) {
 			x += var13 + 5;
 		}
 
 		if(var6 == BiomeBase.SEASONAL_FOREST) {
 			x += var13 + 2;
 		}
 
 		if(var6 == BiomeBase.TAIGA) {
 			x += var13 + 5;
 		}
 
 		if(var6 == BiomeBase.DESERT) {
 			x -= 20;
 		}
 
 		if(var6 == BiomeBase.TUNDRA) {
 			x -= 20;
 		}
 
 		if(var6 == BiomeBase.PLAINS) {
 			x -= 20;
 		}
 
 		int var17;
 		for(y = 0; y < x; ++y) {
 			z = var4 + this.j.nextInt(16) + 8;
 			var17 = var5 + this.j.nextInt(16) + 8;
 			WorldGenerator var18 = var6.a(this.j);
 			var18.a(1.0D, 1.0D, 1.0D);
 			var18.a(this.p, this.j, z, this.p.d(z, var17), var17);
 		}
 
 		int var23;
 		for(y = 0; y < 2; ++y) {
 			z = var4 + this.j.nextInt(16) + 8;
 			var17 = this.j.nextInt(128);
 			var23 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenFlowers(Material.YELLOW_FLOWER.getId())).a(this.p, this.j, z, var17, var23);
 		}
 
 		if(this.j.nextInt(2) == 0) {
 			y = var4 + this.j.nextInt(16) + 8;
 			z = this.j.nextInt(128);
 			var17 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenFlowers(Material.RED_ROSE.getId())).a(this.p, this.j, y, z, var17);
 		}
 
 		if(this.j.nextInt(4) == 0) {
 			y = var4 + this.j.nextInt(16) + 8;
 			z = this.j.nextInt(128);
 			var17 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenFlowers(Material.BROWN_MUSHROOM.getId())).a(this.p, this.j, y, z, var17);
 		}
 
 		if(this.j.nextInt(8) == 0) {
 			y = var4 + this.j.nextInt(16) + 8;
 			z = this.j.nextInt(128);
 			var17 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenFlowers(Material.RED_MUSHROOM.getId())).a(this.p, this.j, y, z, var17);
 		}
 
 		for(y = 0; y < 10; ++y) {
 			z = var4 + this.j.nextInt(16) + 8;
 			var17 = this.j.nextInt(128);
 			var23 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenReed()).a(this.p, this.j, z, var17, var23);
 		}
 
 		if(this.j.nextInt(32) == 0) {
 			y = var4 + this.j.nextInt(16) + 8;
 			z = this.j.nextInt(128);
 			var17 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenPumpkin()).a(this.p, this.j, y, z, var17);
 		}
 
 		y = 0;
 		if(var6 == BiomeBase.DESERT) {
 			y += 10;
 		}
 
 		int var19;
 		for(z = 0; z < y; ++z) {
 			var17 = var4 + this.j.nextInt(16) + 8;
 			var23 = this.j.nextInt(128);
 			var19 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenCactus()).a(this.p, this.j, var17, var23, var19);
 		}
 
 		for(z = 0; z < 50; ++z) {
 			var17 = var4 + this.j.nextInt(16) + 8;
 			var23 = this.j.nextInt(this.j.nextInt(120) + 8);
 			var19 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenLiquids(Material.WATER.getId())).a(this.p, this.j, var17, var23, var19);
 		}
 
 		for(z = 0; z < 20; ++z) {
 			var17 = var4 + this.j.nextInt(16) + 8;
 			var23 = this.j.nextInt(this.j.nextInt(this.j.nextInt(112) + 8) + 8);
 			var19 = var5 + this.j.nextInt(16) + 8;
 			(new WorldGenLiquids(Material.LAVA.getId())).a(this.p, this.j, var17, var23, var19);
 		}
 
 		double[] w = this.p.a().a(new double[256], var4 + 8, var5 + 8, 16, 16);
 
 		for(z = var4 + 8; z < var4 + 8 + 16; ++z) {
 			for(var17 = var5 + 8; var17 < var5 + 8 + 16; ++var17) {
 				var23 = z - (var4 + 8);
 				var19 = var17 - (var5 + 8);
 				int var20 = this.p.e(z, var17);
 				double var21 = w[var23 * 16 + var19] - (double)(var20 - 64) / 64.0D * 0.3D;
 				if(var21 < 0.5D && var20 > 0 && var20 < 128 && this.p.isEmpty(z, var20, var17) && this.p.getMaterial(z, var20 - 1, var17).isSolid() && this.p.getMaterial(z, var20 - 1, var17) != net.minecraft.server.Material.ICE) {
 					this.p.e(z, var20, var17, Material.SNOW.getId());
 				}
 			}
 		}
 
 		/* FUCK locked chests
 		Calendar var24 = Calendar.getInstance();
 		var24.setTimeInMillis(System.currentTimeMillis());
 		if(var24.get(2) == 3 && var24.get(5) == 1) {
 			var17 = var4 + this.j.nextInt(16) + 8;
 			var23 = this.j.nextInt(128);
 			var19 = var5 + this.j.nextInt(16) + 8;
 			if(this.p.getTypeId(var17, var23, var19) == 0 && this.p.d(var17, var23 - 1, var19)) {
 				System.out.println("added a chest!!");
 				this.p.e(var17, var23, var19, Material.LOCKED_CHEST.getId());
 			}
 		}
 		*/
 		BlockSand.a = false;
 	}
 
 	@Override
 	public ConfigurationNode configure(ConfigurationNode node) {
 		if(node==null) {
 			node = Configuration.getEmptyNode();
 		}
 		distanceSquared=node.getInt("chunks-from-spawn",0);
 		if(distanceSquared>0)
 			distanceSquared=distanceSquared^2;
 		return node;
 	}
 }
