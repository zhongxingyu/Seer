 package org.yogpstop.tof;
 
 
 
 import java.util.Random;
 import java.util.logging.Level;
 
 import net.minecraft.src.World;
 import net.minecraft.src.WorldGenMinable;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 
import org.yogpstop.tof.BaseModConnecter;
 import org.yogpstop.tof.GenEmerald;
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
@Mod(modid = "TimesOreForge", name = "TimesOreForge", version = "1.0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class mod_TimesOreForge {
 
 	public static int RmaxLumps;
 	public static int RmaxHeight;
 	public static int RmaxBlocks;
 
 	public static int LmaxLumps;
 	public static int LmaxHeight;
 	public static int LmaxBlocks;
 
 	public static int ImaxLumps;
 	public static int ImaxHeight;
 	public static int ImaxBlocks;
 
 	public static int GmaxLumps;
 	public static int GmaxHeight;
 	public static int GmaxBlocks;
 
 	public static int DmaxLumps;
 	public static int DmaxHeight;
 	public static int DmaxBlocks;
 
 	public static int CmaxLumps;
 	public static int CmaxHeight;
 	public static int CmaxBlocks;
 
 	public static int EmaxLumps;
 	public static int EmaxHeight;
 	public static int EmaxBlocks;
 
 	public static int[] anyotheroreLumps;
 	public static int[] anyotheroreHeight;
 	public static int[] anyotheroreBlocks;
 	public static int[] anyotheroreBlockID = new int[1];
 	public static int[] anyotheroreMeta;
 
 	@Mod.PreInit
 	public void preload(FMLPreInitializationEvent event) {
 		Configuration conf = new Configuration(
 				event.getSuggestedConfigurationFile());
 		try {
 			conf.load();
 
 			Property RL = conf.get("RedStone", "maxLumpsPerChunk", 0);
 			Property RH = conf.get("RedStone", "maxHeight", 16);
 			Property RB = conf.get("RedStone", "maxBlocksPerLump", 14);
 			Property LL = conf.get("LapisLazuli", "maxLumpsPerChunk", 0);
 			Property LH = conf.get("LapisLazuli", "maxHeight", 32);
 			Property LB = conf.get("LapisLazuli", "maxBlocksPerLump", 8);
 			Property IL = conf.get("Iron", "maxLumpsPerChunk", 0);
 			Property IH = conf.get("Iron", "maxHeight", 67);
 			Property IB = conf.get("Iron", "maxBlocksPerLump", 9);
 			Property GL = conf.get("Gold", "maxLumpsPerChunk", 0);
 			Property GH = conf.get("Gold", "maxHeight", 30);
 			Property GB = conf.get("Gold", "maxBlocksPerLump", 8);
 			Property DL = conf.get("Diamond", "maxLumpsPerChunk", 0);
 			Property DH = conf.get("Diamond", "maxHeight", 15);
 			Property DB = conf.get("Diamond", "maxBlocksPerLump", 10);
 			Property CL = conf.get("Coal", "maxLumpsPerChunk", 0);
 			Property CH = conf.get("Coal", "maxHeight", 255);
 			Property CB = conf.get("Coal", "maxBlocksPerLump", 50);
 			Property EL = conf.get("Emerald", "maxLumpsPerChunk", 0);
 			Property EH = conf.get("Emerald", "maxHeight", 31);
 			Property EB = conf.get("Emerald", "maxBlocksPerLump", 1);
 			Property OL = conf.get("other", "maxLumpsPerChunk", "0");
 			Property OH = conf.get("other", "maxHeight", "0");
 			Property OB = conf.get("other", "maxBlocksPerLump", "0");
 			Property OI = conf.get("other", "ListOfBlockID", "0");
 			Property OM = conf.get("other", "ListOfMetaData", "0");
 
 			RL.comment = "How many lumps is there generated in a chunk?";
 			RH.comment = "What position of Y axis is ores generated when the highest?";
 			RB.comment = "How many ores is there generated in a lump?";
 			OI.comment = "Enter any other ores's ItemID separated by comma.";
 			OM.comment = "Enter any other ores's MetaData (like number after semicolon) separated by comma.";
 
 			conf.addCustomCategoryComment("RedStone",
 					"Author write how to configure in here. Other ores is same too.");
 			conf.addCustomCategoryComment("other",
 					"This category is special. How to configure is written in readme. All value must be separated by comma.");
 
 			RmaxLumps = RB.getInt(0);
 			RmaxHeight = RH.getInt(16);
 			RmaxBlocks = RB.getInt(14);
 			LmaxLumps = LL.getInt(0);
 			LmaxHeight = LH.getInt(32);
 			LmaxBlocks = LB.getInt(8);
 			ImaxLumps = IL.getInt(0);
 			ImaxHeight = IH.getInt(67);
 			ImaxBlocks = IB.getInt(9);
 			GmaxLumps = GL.getInt(0);
 			GmaxHeight = GH.getInt(30);
 			GmaxBlocks = GB.getInt(8);
 			DmaxLumps = DL.getInt(0);
 			DmaxHeight = DH.getInt(15);
 			DmaxBlocks = DB.getInt(10);
 			CmaxLumps = CL.getInt(0);
 			CmaxHeight = CH.getInt(255);
 			CmaxBlocks = CB.getInt(50);
 			EmaxLumps = EL.getInt(0);
 			EmaxHeight = EH.getInt(31);
 			EmaxBlocks = EB.getInt(1);
 			anyotheroreLumps = parseComma(OL.value);
 			anyotheroreHeight = parseComma(OH.value);
 			anyotheroreBlocks = parseComma(OB.value);
 			anyotheroreBlockID = parseComma(OI.value);
 			anyotheroreMeta = parseComma(OM.value);
 			if (anyotheroreHeight.length != anyotheroreBlockID.length
 					|| anyotheroreBlocks.length != anyotheroreBlockID.length
 					|| anyotheroreMeta.length != anyotheroreBlockID.length
 					|| anyotheroreLumps.length != anyotheroreBlockID.length) {
 				FMLLog.log(Level.SEVERE, "Config file is wrongly!");
 				anyotheroreLumps = new int[1];
 				anyotheroreHeight = new int[1];
 				anyotheroreBlocks = new int[1];
 				anyotheroreBlockID = new int[1];
 				anyotheroreMeta = new int[1];
 			}
 		} finally {
 			conf.save();
 		}
 	}
 
 	@Mod.Init
 	public void load() {
 		WgenM = new WorldGenMinable[anyotheroreBlockID.length + 6];
 		set(net.minecraft.src.Block.oreCoal.blockID, 0, CmaxBlocks, 0);
 		set(net.minecraft.src.Block.oreIron.blockID, 0, ImaxBlocks, 1);
 		set(net.minecraft.src.Block.oreGold.blockID, 0, GmaxBlocks, 2);
 		set(net.minecraft.src.Block.oreDiamond.blockID, 0, DmaxBlocks, 3);
 		set(net.minecraft.src.Block.oreLapis.blockID, 0, LmaxBlocks, 4);
 		set(net.minecraft.src.Block.oreRedstone.blockID, 0, RmaxBlocks, 5);
 		GenEmerald.load(EmaxBlocks);
 	}
 	
 	public static void make(World world, Random random, int i, int j) {
 		generate(world, random, i, j, CmaxLumps, CmaxHeight, 0);
 		generate(world, random, i, j, ImaxLumps, ImaxHeight, 1);
 		generate(world, random, i, j, GmaxLumps, GmaxHeight, 2);
 		generate(world, random, i, j, DmaxLumps, DmaxHeight, 3);
 		generate(world, random, i, j, LmaxLumps, LmaxHeight, 4);
 		generate(world, random, i, j, RmaxLumps, RmaxHeight, 5);
 		parseGen(world, random, i, j);
 	}
 
	public static void parseSet() {
 		for (int c = 0; c < anyotheroreBlockID.length; c++) {
 			set(anyotheroreBlockID[c], anyotheroreMeta[c],
 					anyotheroreBlocks[c], c + 6);
 		}
 	}
 
	public static void set(int BlockID, int Meta, int maxBlocks, int count) {
 		WgenM[count] = new WorldGenMinable(BlockID, Meta, maxBlocks);
 	}
 
	public static void parseGen(World world, Random random, int i, int j) {
 		for (int c = 0; c < anyotheroreBlockID.length; c++) {
 			generate(world, random, i, j, anyotheroreLumps[c],
 					anyotheroreHeight[c], c + 6);
 		}
 	}
 
	public static void generate(World world, Random random, int i, int j,
 			int maxLumps, int maxHeight, int count) {
 		for (int l = 0; l < maxLumps; l++) {
 			int ia = (i - (i % 16)) + random.nextInt(16);
 			int ja = random.nextInt(maxHeight + 1);
 			int ka = (j - (j % 16)) + random.nextInt(16);
 			WgenM[count].generate(world, random, ia, ja, ka);
 		}
 	}
 
	public int[] parseComma(String source) {
 		String[] scache = source.split(",");
 		int[] cache = new int[scache.length];
 		for (int i = 0; i < scache.length; i++) {
 			scache[i] = scache[i].replaceAll(" ", "");
 			cache[i] = Integer.parseInt(scache[i]);
 		}
 		return cache;
 	}
 
 	static WorldGenMinable[] WgenM;
 
 }
