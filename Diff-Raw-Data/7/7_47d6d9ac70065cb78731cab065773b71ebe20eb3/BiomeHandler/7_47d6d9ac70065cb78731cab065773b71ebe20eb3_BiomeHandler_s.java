 package NaturalBiomes2.Biomes;
 
 import NaturalBiomes2.Lib.BiomeAllow;
 import NaturalBiomes2.Lib.BiomeIds;
 import NaturalBiomes2.Lib.VillageAllow;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.biome.BiomeGenDesert;
 import net.minecraft.world.biome.BiomeGenSnow;
 import net.minecraftforge.common.BiomeDictionary;
 import net.minecraftforge.common.BiomeManager;
 
 public class BiomeHandler {
 	
 	public static BiomeGenBase birchJungle;
 	public static BiomeGenBase birchMountains;
 	public static BiomeGenBase birchWoods;
 	public static BiomeGenBase dunes;
 	public static BiomeGenBase extremeBirchWoods;
 	public static BiomeGenBase extremeDesertHills;
 	public static BiomeGenBase extremeDunes;
 	public static BiomeGenBase extremeForestedHills;
 	public static BiomeGenBase extremeGrassHills;
 	public static BiomeGenBase extremeMeadowHills;
 	public static BiomeGenBase extremeWoods;
 	public static BiomeGenBase extremePineHills;
 	public static BiomeGenBase extremeSnowHills;
 	public static BiomeGenBase extremeSparseWoods;
 	public static BiomeGenBase forest;
 	public static BiomeGenBase oakMountains;
 	public static BiomeGenBase oakWoods;
 	public static BiomeGenBase lushForest;
 	public static BiomeGenBase lushMountains;
 	public static BiomeGenBase lushPlains; 
 	public static BiomeGenBase lushSwamp;
 	public static BiomeGenBase massiveHills;
 	public static BiomeGenBase marsh;
 	public static BiomeGenBase meadows;
 	public static BiomeGenBase pineForest;
 	public static BiomeGenBase rainForest;
 	public static BiomeGenBase savannah;
 	public static BiomeGenBase shrublands;
 	public static BiomeGenBase snowyForest;
 	public static BiomeGenBase snowyOakMountains; 
 	public static BiomeGenBase snowyShrublands;  
 	public static BiomeGenBase snowyWoods; 
 	public static BiomeGenBase sparseOakMountains; 
 	public static BiomeGenBase sparseWoods; 
 	public static BiomeGenBase thickOakMountains; 
 	public static BiomeGenBase thickSwamp;
 	public static BiomeGenBase thickWoods;
     
     	//new ones that need to be added
     	public static BiomeGenBase birchForest;
     	public static BiomeGenBase blackMarsh;
     	public static BiomeGenBase desertMountains;
     	public static BiomeGenBase forestMountains;
     	public static BiomeGenBase plains; //no tall grass (uses extreme hill stuff)
     	public static BiomeGenBase taigaMountains;
     	public static BiomeGenBase swampyHills; //same as mountains; spawn lakes as some trees
     	public static BiomeGenBase snowyMountains; //uses ice plains
 	
 	public static void init()
 	{
         	initBiomes();
         
         	addToBiomeDictionary();
 
         	addSpawnBiomes();
         
         	addVillageBiomes();
         
         	addStrongholdBiomes();
 
         	registerBiomes();
 	}
 	
 	public static void initBiomes()
 	{
 		birchJungle = new BiomeGenBirchJungle(BiomeIds.BIRCH_JUNGLE).setColor(353825).setBiomeName("Birch Jungle (NB2)").setTemperatureRainfall(0.4F, 0.9F).setMinMaxHeight(0.4F, 0.7F);
         birchMountains = new BiomeGenBirchWoods(BiomeIds.BIRCH_MOUNTAINS, 3, 3).setColor(353825).setBiomeName("Birch Mountains (NB2)").setTemperatureRainfall(0.4F, 0.9F).setMinMaxHeight(0.5F, 0.9F);
         birchWoods = new BiomeGenBirchWoods(BiomeIds.BIRCH_WOODS, 3, 3).setColor(353825).setBiomeName("Birch Woods (NB2)").setTemperatureRainfall(0.4F, 0.9F).setMinMaxHeight(0.2F, 0.3F);
        dunes = new BiomeGenDunes(BiomeIds.DUNES).setColor(16421912).setBiomeName("Dunes (NB2)").setDisableRain().setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.5F, 0.9F);	
         extremeBirchWoods = new BiomeGenBirchWoods(BiomeIds.EXTREME_BIRCH_WOODS, 3, 3).setColor(6316128).setBiomeName("Extreme Birch Woods (NB2)").setTemperatureRainfall(0.4F, 0.9F).setMinMaxHeight(0.3F, 1.5F);
    	extremeDesertHills = new BiomeGenDesert(BiomeIds.EXTREME_DESERT_HILLS).setColor(6316128).setBiomeName("Extreme Desert (NB2)").setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.3F, 1.5F);
    	extremeDunes = new BiomeGenDunes(BiomeIds.EXTREME_DUNES).setColor(6316128).setBiomeName("Extreme Dunes (NB2)").setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.3F, 1.5F);
     	extremeForestedHills = new BiomeGenForest(BiomeIds.EXTREME_FORESTED_HILLS, 3).setColor(6316128).setBiomeName("Extreme Forested Hills (NB2)").setTemperatureRainfall(0.7F, 0.8F).setMinMaxHeight(0.3F, 1.5F);
     	extremeGrassHills = new BiomeGenGrass(BiomeIds.EXTREME_GRASS_HILLS).setColor(6316128).setBiomeName("Extreme Grass Hills (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(0.3F, 1.5F);
     	extremeMeadowHills = new BiomeGenMeadows(BiomeIds.EXTREME_MEADOW_HILLS).setColor(6316128).setBiomeName("Extreme Meadow Hills (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(0.3F, 1.5F);
     	extremeWoods = new BiomeGenWoods(BiomeIds.EXTREME_WOODS, 3, 3).setColor(6316128).setBiomeName("Extreme Woods (NB2)").setTemperatureRainfall(0.6F, 0.4F).setMinMaxHeight(0.3F, 1.5F);
     	extremePineHills = new BiomeGenPineForest(BiomeIds.EXTREME_PINE_HILLS).setColor(6316128).setBiomeName("Extreme Pines (NB2)").setTemperatureRainfall(0.3F, 0.6F).setMinMaxHeight(0.3F, 1.5F);
     	extremeSnowHills = new BiomeGenSnow(BiomeIds.EXTREME_SNOW_HILLS).setColor(6316128).setBiomeName("Extreme Snow Hills (NB2)").setTemperatureRainfall(0.0F, 0.05F).setMinMaxHeight(0.3F, 1.5F);
     	extremeSparseWoods = new BiomeGenWoods(BiomeIds.EXTREME_SPARSE_WOODS, 1, 3).setColor(6316128).setBiomeName("Extreme Sparse Woods (NB2)").setTemperatureRainfall(1.5F, 0.15F).setMinMaxHeight(0.3F, 1.5F);
     	forest = new BiomeGenForest(BiomeIds.FOREST, 5).setColor(353825).setBiomeName("Forest (NB2)").setTemperatureRainfall(0.7F, 0.8F).setMinMaxHeight(0.2F, 0.3F);
     	oakMountains = new BiomeGenWoods(BiomeIds.OAK_MOUNTAINS, 3, 3).setColor(353825).setBiomeName("Oak Mountains (NB2)").setTemperatureRainfall(0.6F, 0.4F).setMinMaxHeight(0.5F, 0.9F);
 		oakWoods = new BiomeGenWoods(BiomeIds.OAK_WOODS, 3, 3).setColor(353825).setBiomeName("Woods (NB2)").setTemperatureRainfall(0.6F, 0.4F).setMinMaxHeight(0.2F, 0.3F);
 		lushForest = new BiomeGenForest(BiomeIds.LUSH_FOREST, 30).setColor(9286496).setBiomeName("Lush Forest (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(0.2F, 0.3F);
         lushMountains = new BiomeGenGrass(BiomeIds.LUSH_MOUNTAINS).setColor(9286496).setBiomeName("Lush Mountains (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(0.5F, 0.9F);
         lushPlains = new BiomeGenGrass(BiomeIds.LUSH_PLAINS).setColor(9286496).setBiomeName("Lush Plains (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(0.1F, 0.2F);
         lushSwamp = new BiomeGenLushSwamp(BiomeIds.LUSH_SWAMP, 2).setColor(9286496).setBiomeName("Lush Swamps (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(-0.2F, 0.1F);
         massiveHills = new BiomeGenMassiveHills(BiomeIds.MASSIVE_HILLS).setColor(9286496).setBiomeName("Massive Hills (NB2)").setMinMaxHeight(1.5F, 2.0F).setTemperatureRainfall(0.2F, 0.3F);
         marsh = new BiomeGenMarsh(BiomeIds.MARSH).setColor(353825).setBiomeName("Marsh (NB2)").setTemperatureRainfall(0.7F, 0.8F).setMinMaxHeight(0.1F, 0.2F);
         meadows = new BiomeGenMeadows(BiomeIds.MEADOWS).setColor(9286496).setBiomeName("Meadows (NB2)").setTemperatureRainfall(1.2F, 1.0F).setMinMaxHeight(0.1F, 0.2F);
         pineForest = new BiomeGenPineForest(BiomeIds.PINE_FOREST).setColor(747097).setBiomeName("Pine Forest (NB2)").setTemperatureRainfall(0.3F, 0.6F).setMinMaxHeight(0.1F, 0.3F);
         rainForest = new BiomeGenRainForest(BiomeIds.RAIN_FOREST).setColor(5470985).setBiomeName("Rainforest (NB2)").setTemperatureRainfall(1.2F, 0.9F).setMinMaxHeight(0.4F, 0.8f);
         savannah = new BiomeGenGrass(BiomeIds.SAVANNAH).setColor(353825).setBiomeName("Savannah (NB2)").setTemperatureRainfall(1.9F, 0.15F).setDisableRain().setMinMaxHeight(0.1F, 0.2F);
         shrublands = new BiomeGenShrublands(BiomeIds.SHRUBLANDS).setColor(353825).setBiomeName("Shrublands (NB2)").setTemperatureRainfall(1.5F, 0.15F).setMinMaxHeight(0.1F, 0.5F);       
         snowyForest = new BiomeGenForest(BiomeIds.SNOWY_FOREST, 3).setColor(747097).setBiomeName("Snowy Forest (NB2)").setTemperatureRainfall(0.05F, 0.6F).setEnableSnow().setMinMaxHeight(0.1F, 0.3F);
         snowyOakMountains = new BiomeGenWoods(BiomeIds.SNOWY_OAK_MOUNTAINS, 3, 3).setColor(747097).setBiomeName("Snowy Oak Mountains (NB2)").setTemperatureRainfall(0.05F, 0.6F).setMinMaxHeight(0.5F, 0.9F);
         snowyShrublands = new BiomeGenShrublands(BiomeIds.SNOWY_SHRUBLANDS).setColor(9286496).setBiomeName("Snowy Shrublands (NB2)").setTemperatureRainfall(0.05F, 0.6F).setEnableSnow().setMinMaxHeight(0.1F, 0.5F);            
         snowyWoods = new BiomeGenWoods(BiomeIds.SNOWY_WOODS, 3, 3).setColor(747097).setBiomeName("Snowy Woods (NB2)").setTemperatureRainfall(0.05F, 0.6F).setEnableSnow().setMinMaxHeight(0.1F, 0.3F);
         sparseOakMountains = new BiomeGenWoods(BiomeIds.SPARSE_OAK_MOUNTAINS, 1, 3).setColor(353825).setBiomeName("Sparse Oak Mountains (NB2)").setTemperatureRainfall(1.5F, 0.15F).setMinMaxHeight(0.5F, 0.9F);
         sparseWoods = new BiomeGenWoods(BiomeIds.SPARSE_WOODS, 1, 3).setColor(353825).setBiomeName("Sparse Woods (NB2)").setTemperatureRainfall(1.5F, 0.15F).setMinMaxHeight(0.1F, 0.3F);
         thickOakMountains = new BiomeGenWoods(BiomeIds.THICK_OAK_MOUNTAINS, 12, 3).setColor(353825).setBiomeName("Thick Oak Mountains (NB2)").setTemperatureRainfall(0.6F, 0.4F).setMinMaxHeight(0.5F, 0.9F);
         thickSwamp = new BiomeGenSwamp(BiomeIds.THICK_SWAMP, 15).setColor(522674).setBiomeName("Thick Swampland (NB2)").func_76733_a(9154376).setMinMaxHeight(-0.2F, 0.1F).setTemperatureRainfall(0.8F, 0.9F);    
         thickWoods = new BiomeGenWoods(BiomeIds.THICK_WOODS, 12, 5).setColor(353825).setBiomeName("Thick Woods (NB2)").setTemperatureRainfall(0.8F, 0.9F).setMinMaxHeight(0.1F, 0.3F);
 	}
 	
 	public static void addToBiomeDictionary()
 	{
 		BiomeDictionary.registerBiomeType(birchJungle, new BiomeDictionary.Type[] {BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.HILLS});
 		BiomeDictionary.registerBiomeType(birchMountains, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(birchWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(dunes, new BiomeDictionary.Type[] {BiomeDictionary.Type.DESERT, BiomeDictionary.Type.MOUNTAIN});
 		BiomeDictionary.registerBiomeType(forest, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(extremeBirchWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN,BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(extremeDesertHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.DESERT});
 		BiomeDictionary.registerBiomeType(extremeDunes, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.DESERT});
 		BiomeDictionary.registerBiomeType(extremeForestedHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(extremeGrassHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN});
 		BiomeDictionary.registerBiomeType(extremeMeadowHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN});
 		BiomeDictionary.registerBiomeType(extremePineHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(extremeSnowHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FROZEN});
 		BiomeDictionary.registerBiomeType(extremeSparseWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(extremeWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(lushForest, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(lushMountains, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN});
 		BiomeDictionary.registerBiomeType(lushPlains, new BiomeDictionary.Type[] {BiomeDictionary.Type.PLAINS});
 		BiomeDictionary.registerBiomeType(lushSwamp, new BiomeDictionary.Type[] {BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(massiveHills, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN});
 		BiomeDictionary.registerBiomeType(marsh, new BiomeDictionary.Type[] {BiomeDictionary.Type.SWAMP});
 		BiomeDictionary.registerBiomeType(meadows, new BiomeDictionary.Type[] {BiomeDictionary.Type.PLAINS});
 		BiomeDictionary.registerBiomeType(oakMountains, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(oakWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(pineForest, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(rainForest, new BiomeDictionary.Type[] {BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.HILLS});
 		BiomeDictionary.registerBiomeType(savannah, new BiomeDictionary.Type[] {BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.DESERT});
 		BiomeDictionary.registerBiomeType(shrublands, new BiomeDictionary.Type[] {BiomeDictionary.Type.PLAINS});
 		BiomeDictionary.registerBiomeType(snowyForest, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST, BiomeDictionary.Type.FROZEN});
 		BiomeDictionary.registerBiomeType(snowyOakMountains, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.FROZEN});
 		BiomeDictionary.registerBiomeType(snowyShrublands, new BiomeDictionary.Type[] {BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.FROZEN});
 		BiomeDictionary.registerBiomeType(snowyWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST, BiomeDictionary.Type.FROZEN});
 		BiomeDictionary.registerBiomeType(sparseOakMountains, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(sparseWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(thickOakMountains, new BiomeDictionary.Type[] {BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(thickSwamp, new BiomeDictionary.Type[] {BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.FOREST});
 		BiomeDictionary.registerBiomeType(thickWoods, new BiomeDictionary.Type[] {BiomeDictionary.Type.FOREST});
 	}
 	
 	public static void addSpawnBiomes()
 	{
 		BiomeManager.addSpawnBiome(birchJungle);
 		BiomeManager.addSpawnBiome(birchMountains);
 		BiomeManager.addSpawnBiome(birchWoods);
 		BiomeManager.addSpawnBiome(dunes);
 		BiomeManager.addSpawnBiome(forest);
 		BiomeManager.addSpawnBiome(extremeBirchWoods);
 		BiomeManager.addSpawnBiome(extremeDesertHills);
 		BiomeManager.addSpawnBiome(extremeDunes);
 		BiomeManager.addSpawnBiome(extremeForestedHills);
 		BiomeManager.addSpawnBiome(extremeGrassHills);
 		BiomeManager.addSpawnBiome(extremeMeadowHills);
 		BiomeManager.addSpawnBiome(extremeWoods);
 		BiomeManager.addSpawnBiome(extremePineHills);
 		BiomeManager.addSpawnBiome(extremeSnowHills);
 		BiomeManager.addSpawnBiome(extremeSparseWoods);
 		BiomeManager.addSpawnBiome(lushForest);
 		BiomeManager.addSpawnBiome(lushMountains);
 		BiomeManager.addSpawnBiome(lushPlains);
 		BiomeManager.addSpawnBiome(lushSwamp);
 		BiomeManager.addSpawnBiome(massiveHills);
 		BiomeManager.addSpawnBiome(marsh);
 		BiomeManager.addSpawnBiome(meadows);
 		BiomeManager.addSpawnBiome(oakMountains);
 		BiomeManager.addSpawnBiome(oakWoods);
 		BiomeManager.addSpawnBiome(pineForest);
 		BiomeManager.addSpawnBiome(rainForest);
 		BiomeManager.addSpawnBiome(savannah);
 		BiomeManager.addSpawnBiome(shrublands);
 		BiomeManager.addSpawnBiome(snowyForest);
 		BiomeManager.addSpawnBiome(snowyOakMountains);
 		BiomeManager.addSpawnBiome(snowyShrublands);
 		BiomeManager.addSpawnBiome(snowyWoods);
 		BiomeManager.addSpawnBiome(sparseOakMountains);
 		BiomeManager.addSpawnBiome(sparseWoods);
 		BiomeManager.addSpawnBiome(thickOakMountains);
 		BiomeManager.addSpawnBiome(thickSwamp);
 		BiomeManager.addSpawnBiome(thickWoods);
 	}
 	
 	public static void addVillageBiomes()
 	{
 		BiomeManager.addVillageBiome(birchJungle, VillageAllow.BIRCH_JUNGLE);
 		BiomeManager.addVillageBiome(birchMountains, VillageAllow.BIRCH_MOUNTAINS);
 		BiomeManager.addVillageBiome(birchWoods, VillageAllow.BIRCH_WOODS);
 		BiomeManager.addVillageBiome(dunes, VillageAllow.DUNES);
 		BiomeManager.addVillageBiome(forest, VillageAllow.FOREST);
 		BiomeManager.addVillageBiome(extremeBirchWoods, VillageAllow.EXTREME_BIRCH_WOODS);
 		BiomeManager.addVillageBiome(extremeDesertHills, VillageAllow.EXTREME_DESERT_HILLS);
 		BiomeManager.addVillageBiome(extremeDunes, VillageAllow.EXTREME_DUNES);
 		BiomeManager.addVillageBiome(extremeForestedHills, VillageAllow.EXTREME_FORESTED_HILLS);
 		BiomeManager.addVillageBiome(extremeGrassHills, VillageAllow.EXTREME_GRASS_HILLS);
 		BiomeManager.addVillageBiome(extremeMeadowHills, VillageAllow.EXTREME_MEADOW_HILLS);
 		BiomeManager.addVillageBiome(extremeWoods, VillageAllow.EXTREME_WOODS);
 		BiomeManager.addVillageBiome(extremePineHills, VillageAllow.EXTREME_PINE_HILLS);
 		BiomeManager.addVillageBiome(extremeSnowHills, VillageAllow.EXTREME_SNOW_HILLS);
 		BiomeManager.addVillageBiome(extremeSparseWoods, VillageAllow.EXTREME_SPARSE_WOODS);
 		BiomeManager.addVillageBiome(lushForest, VillageAllow.LUSH_FOREST);
 		BiomeManager.addVillageBiome(lushMountains, VillageAllow.LUSH_MOUNTAINS);
 		BiomeManager.addVillageBiome(lushPlains, VillageAllow.LUSH_PLAINS);
 		BiomeManager.addVillageBiome(lushSwamp, VillageAllow.LUSH_SWAMP);
 		BiomeManager.addVillageBiome(massiveHills, VillageAllow.MASSIVE_HILLS);
 		BiomeManager.addVillageBiome(marsh, VillageAllow.MARSH);
 		BiomeManager.addVillageBiome(meadows, VillageAllow.MEADOWS);
 		BiomeManager.addVillageBiome(oakMountains, VillageAllow.OAK_MOUNTAINS);
 		BiomeManager.addVillageBiome(oakWoods, VillageAllow.OAK_WOODS);
 		BiomeManager.addVillageBiome(pineForest, VillageAllow.PINE_FOREST);
 		BiomeManager.addVillageBiome(rainForest, VillageAllow.RAIN_FOREST);
 		BiomeManager.addVillageBiome(savannah, VillageAllow.SAVANNAH);
 		BiomeManager.addVillageBiome(shrublands, VillageAllow.SHRUBLANDS);
 		BiomeManager.addVillageBiome(snowyForest, VillageAllow.SNOWY_FOREST);
 		BiomeManager.addVillageBiome(snowyOakMountains, VillageAllow.SNOWY_OAK_MOUNTAINS);
 		BiomeManager.addVillageBiome(snowyShrublands, VillageAllow.SNOWY_SHRUBLANDS);
 		BiomeManager.addVillageBiome(snowyWoods, VillageAllow.SNOWY_WOODS);
 		BiomeManager.addVillageBiome(sparseOakMountains, VillageAllow.SPARSE_OAK_MOUNTAINS);
 		BiomeManager.addVillageBiome(sparseWoods, VillageAllow.SPARSE_WOODS);
 		BiomeManager.addVillageBiome(thickOakMountains, VillageAllow.THICK_OAK_MOUNTAINS);
 		BiomeManager.addVillageBiome(thickSwamp, VillageAllow.THICK_SWAMP);
 		BiomeManager.addVillageBiome(thickWoods, VillageAllow.THICK_WOODS);
 	}
 	
 	public static void addStrongholdBiomes(){
 		BiomeManager.addStrongholdBiome(birchJungle);
 		BiomeManager.addStrongholdBiome(birchMountains);
 		BiomeManager.addStrongholdBiome(birchWoods);
 		BiomeManager.addStrongholdBiome(dunes);
 		BiomeManager.addStrongholdBiome(forest);
 		BiomeManager.addStrongholdBiome(extremeBirchWoods);
 		BiomeManager.addStrongholdBiome(extremeDesertHills);
 		BiomeManager.addStrongholdBiome(extremeDunes);
 		BiomeManager.addStrongholdBiome(extremeForestedHills);
 		BiomeManager.addStrongholdBiome(extremeGrassHills);
 		BiomeManager.addStrongholdBiome(extremeMeadowHills);
 		BiomeManager.addStrongholdBiome(extremeWoods);
 		BiomeManager.addStrongholdBiome(extremePineHills);
 		BiomeManager.addStrongholdBiome(extremeSnowHills);
 		BiomeManager.addStrongholdBiome(extremeSparseWoods);
 		BiomeManager.addStrongholdBiome(lushForest);
 		BiomeManager.addStrongholdBiome(lushMountains);
 		BiomeManager.addStrongholdBiome(lushPlains);
 		BiomeManager.addStrongholdBiome(lushSwamp);
 		BiomeManager.addStrongholdBiome(massiveHills);
 		BiomeManager.addStrongholdBiome(marsh);
 		BiomeManager.addStrongholdBiome(meadows);
 		BiomeManager.addStrongholdBiome(oakMountains);
 		BiomeManager.addStrongholdBiome(oakWoods);
 		BiomeManager.addStrongholdBiome(pineForest);
 		BiomeManager.addStrongholdBiome(rainForest);
 		BiomeManager.addStrongholdBiome(savannah);
 		BiomeManager.addStrongholdBiome(shrublands);
 		BiomeManager.addStrongholdBiome(snowyForest);
 		BiomeManager.addStrongholdBiome(snowyOakMountains);
 		BiomeManager.addStrongholdBiome(snowyShrublands);
 		BiomeManager.addStrongholdBiome(snowyWoods);
 		BiomeManager.addStrongholdBiome(sparseOakMountains);
 		BiomeManager.addStrongholdBiome(sparseWoods);
 		BiomeManager.addStrongholdBiome(thickOakMountains);
 		BiomeManager.addStrongholdBiome(thickSwamp);
 		BiomeManager.addStrongholdBiome(thickWoods);
 	}
 	
 	public static void registerBiomes()
 	{
 		if (BiomeAllow.BIRCH_JUNGLE)
 			GameRegistry.addBiome(birchJungle);
 		
 		if (BiomeAllow.BIRCH_MOUNTAINS)
 			GameRegistry.addBiome(birchMountains);
 			
 		if (BiomeAllow.BIRCH_WOODS)
 			GameRegistry.addBiome(birchWoods);
 			
 		if (BiomeAllow.DUNES)
 			GameRegistry.addBiome(dunes);
 		
 		if (BiomeAllow.FOREST)
 			GameRegistry.addBiome(forest);
 		
 		if (BiomeAllow.EXTREME_BIRCH_WOODS)
 			GameRegistry.addBiome(extremeBirchWoods);
 		
 		if (BiomeAllow.EXTREME_DESERT_HILLS)
 			GameRegistry.addBiome(extremeDesertHills);
 		
 		if (BiomeAllow.EXTREME_DUNES)
 			GameRegistry.addBiome(extremeDunes);
 		
 		if (BiomeAllow.EXTREME_FORESTED_HILLS)
 			GameRegistry.addBiome(extremeForestedHills);
 		
 		if (BiomeAllow.EXTREME_GRASS_HILLS)
 			GameRegistry.addBiome(extremeGrassHills);
 		
 		if (BiomeAllow.EXTREME_MEADOW_HILLS)
 			GameRegistry.addBiome(extremeMeadowHills);
 		
 		if (BiomeAllow.EXTREME_WOODS)
 			GameRegistry.addBiome(extremeWoods);
 		
 		if (BiomeAllow.EXTREME_PINE_HILLS)
 			GameRegistry.addBiome(extremePineHills);
 		
 		if (BiomeAllow.EXTREME_SNOW_HILLS)
 			GameRegistry.addBiome(extremeSnowHills);
 		
 		if (BiomeAllow.EXTREME_SPARSE_WOODS)
 			GameRegistry.addBiome(extremeSparseWoods);
 			
 		if (BiomeAllow.LUSH_FOREST)
 			GameRegistry.addBiome(lushForest);
 		
 		if (BiomeAllow.LUSH_MOUNTAINS)
 			GameRegistry.addBiome(lushMountains);
 			
 		if (BiomeAllow.LUSH_PLAINS)
 			GameRegistry.addBiome(lushPlains);
 			
 		if (BiomeAllow.LUSH_SWAMP)
 			GameRegistry.addBiome(lushSwamp);
 			
 		if (BiomeAllow.MASSIVE_HILLS)
 			GameRegistry.addBiome(massiveHills);
 		
 		if (BiomeAllow.MARSH)
 			GameRegistry.addBiome(marsh);
 			
 		if (BiomeAllow.MEADOWS)
 			GameRegistry.addBiome(meadows);
 			
 		if (BiomeAllow.OAK_MOUNTAINS)
 			GameRegistry.addBiome(oakMountains);
 			
 		if (BiomeAllow.OAK_WOODS)
 			GameRegistry.addBiome(oakWoods);
 			
 		if (BiomeAllow.PINE_FOREST)
 			GameRegistry.addBiome(pineForest);
 			
 		if (BiomeAllow.RAIN_FOREST)
 			GameRegistry.addBiome(rainForest);
 			
 		if (BiomeAllow.SAVANNAH)
 			GameRegistry.addBiome(savannah);
 			
 		if (BiomeAllow.SHRUBLANDS)
 			GameRegistry.addBiome(shrublands);
 			
 		if (BiomeAllow.SNOWY_FOREST)
 			GameRegistry.addBiome(snowyForest);
 		
 		if (BiomeAllow.SNOWY_OAK_MOUNTAINS)
 			GameRegistry.addBiome(snowyOakMountains);
 			
 		if (BiomeAllow.SNOWY_SHRUBLANDS)
 			GameRegistry.addBiome(snowyShrublands);
 			
 		if (BiomeAllow.SNOWY_WOODS)
 			GameRegistry.addBiome(snowyWoods);
 			
 		if (BiomeAllow.SPARSE_OAK_MOUNTAINS)
 			GameRegistry.addBiome(sparseOakMountains);
 			
 		if (BiomeAllow.SPARSE_WOODS)
 			GameRegistry.addBiome(sparseWoods);
 			
 		if (BiomeAllow.THICK_OAK_MOUNTAINS)
 			GameRegistry.addBiome(thickOakMountains);
 			
 		if (BiomeAllow.THICK_SWAMP)
 			GameRegistry.addBiome(thickSwamp);
 			
 		if (BiomeAllow.THICK_WOODS)
 			GameRegistry.addBiome(thickWoods);
 	}
 	
 }
