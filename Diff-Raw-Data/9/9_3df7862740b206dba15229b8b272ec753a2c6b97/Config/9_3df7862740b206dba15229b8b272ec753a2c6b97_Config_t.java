 package NaturalBiomes2;
 
 import java.io.File;
 
 import NaturalBiomes2.Lib.BiomeAllow;
 import NaturalBiomes2.Lib.BiomeIds;
 import NaturalBiomes2.Lib.Strings;
 import NaturalBiomes2.Lib.VillageAllow;
 
 
 import net.minecraftforge.common.Configuration;
 
 public class Config
 {
 	public static Configuration config;
 	
 	public static void initialize(File file)
 	{
 		config = new Configuration(file);
 		
 		biomeIds();
 		allowBiomes();
 		allowVillages();	
 		
 		config.save();
 	}
 
 	public static void save()
 	{
 		config.save();
 	}
 	
 	public static void biomeIds()
 	{
 		BiomeIds.RAIN_FOREST= config.get(Strings.CATEGORY_BIOME, Strings.RAIN_FOREST_NAME, BiomeIds.RAIN_FOREST_DEFAULT).getInt(BiomeIds.RAIN_FOREST_DEFAULT);
         BiomeIds.BIRCH_JUNGLE = config.get(Strings.CATEGORY_BIOME, Strings.BIRCH_JUNGLE_NAME, BiomeIds.BIRCH_JUNGLE_DEFAULT).getInt(BiomeIds.BIRCH_JUNGLE_DEFAULT);
         BiomeIds.OAK_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.OAK_WOODS_NAME, BiomeIds.OAK_WOODS_DEFAULT).getInt(BiomeIds.OAK_WOODS_DEFAULT);
         BiomeIds.THICK_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.THICK_WOODS_NAME, BiomeIds.THICK_WOODS_DEFAULT).getInt(BiomeIds.THICK_WOODS_DEFAULT);
         BiomeIds.SPARSE_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.SPARSE_WOODS_NAME, BiomeIds.SPARSE_WOODS_DEFAULT).getInt(BiomeIds.SPARSE_WOODS_DEFAULT);
         BiomeIds.SNOWY_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.SNOWY_WOODS_NAME, BiomeIds.SNOWY_WOODS_DEFAULT).getInt(BiomeIds.SNOWY_WOODS_DEFAULT);
         BiomeIds.LUSH_PLAINS = config.get(Strings.CATEGORY_BIOME, Strings.LUSH_PLAINS_NAME, BiomeIds.LUSH_PLAINS_DEFAULT).getInt(BiomeIds.LUSH_PLAINS_DEFAULT);
         BiomeIds.LUSH_MOUNTAINS = config.get(Strings.CATEGORY_BIOME, Strings.LUSH_MOUNTAINS_NAME, BiomeIds.LUSH_MOUNTAINS_DEFAULT).getInt(BiomeIds.LUSH_MOUNTAINS_DEFAULT);
         BiomeIds.PINE_FOREST = config.get(Strings.CATEGORY_BIOME, Strings.PINE_FOREST_NAME, BiomeIds.PINE_FOREST_DEFAULT).getInt(BiomeIds.PINE_FOREST_DEFAULT);
         BiomeIds.MEADOWS = config.get(Strings.CATEGORY_BIOME, Strings.MEADOWS_NAME, BiomeIds.MEADOWS_DEFAULT).getInt(BiomeIds.MEADOWS_DEFAULT);
         BiomeIds.SHRUBLANDS = config.get(Strings.CATEGORY_BIOME, Strings.SHRUBLANDS_NAME, BiomeIds.SHRUBLANDS_DEFAULT).getInt(BiomeIds.SHRUBLANDS_DEFAULT);
         BiomeIds.SNOWY_SHRUBLANDS= config.get(Strings.CATEGORY_BIOME, Strings.SNOWY_SHRUBLANDS_NAME, BiomeIds.SNOWY_SHRUBLANDS_DEFAULT).getInt(BiomeIds.SNOWY_SHRUBLANDS_DEFAULT);
         BiomeIds.MARSH = config.get(Strings.CATEGORY_BIOME, Strings.MARSH_NAME, BiomeIds.MARSH_DEFAULT).getInt(BiomeIds.MARSH_DEFAULT);
         BiomeIds.SAVANNAH = config.get(Strings.CATEGORY_BIOME, Strings.SAVANNAH_NAME, BiomeIds.SAVANNAH_DEFAULT).getInt(BiomeIds.SAVANNAH_DEFAULT);
         BiomeIds.BIRCH_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.BIRCH_WOODS_NAME, BiomeIds.BIRCH_WOODS_DEFAULT).getInt(BiomeIds.BIRCH_WOODS_DEFAULT);
         BiomeIds.OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME, Strings.OAK_MOUNTAINS_NAME, BiomeIds.OAK_MOUNTAINS_DEFAULT).getInt(BiomeIds.OAK_MOUNTAINS_DEFAULT);
         BiomeIds.THICK_OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME, Strings.THICK_OAK_MOUNTAINS_NAME, BiomeIds.THICK_OAK_MOUNTAINS_DEFAULT).getInt(BiomeIds.THICK_OAK_MOUNTAINS_DEFAULT);
         BiomeIds.SPARSE_OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME, Strings.SPARSE_OAK_MOUNTAINS_NAME, BiomeIds.SPARSE_OAK_MOUNTAINS_DEFAULT).getInt(BiomeIds.SPARSE_OAK_MOUNTAINS_DEFAULT);
         BiomeIds.SNOWY_OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME, Strings.SNOWY_OAK_MOUNTAINS_NAME, BiomeIds.SNOWY_OAK_MOUNTAINS_DEFAULT).getInt(BiomeIds.SNOWY_OAK_MOUNTAINS_DEFAULT);
         BiomeIds.BIRCH_MOUNTAINS = config.get(Strings.CATEGORY_BIOME, Strings.BIRCH_MOUNTAINS_NAME, BiomeIds.BIRCH_MOUNTAINS_DEFAULT).getInt(BiomeIds.BIRCH_MOUNTAINS_DEFAULT);   
         BiomeIds.DUNES = config.get(Strings.CATEGORY_BIOME, Strings.DUNES_NAME, BiomeIds.DUNES_DEFAULT).getInt(BiomeIds.DUNES_DEFAULT);
         BiomeIds.THICK_SWAMP = config.get(Strings.CATEGORY_BIOME, Strings.THICK_SWAMP_NAME, BiomeIds.THICK_SWAMP_DEFAULT).getInt(BiomeIds.THICK_SWAMP_DEFAULT);
         BiomeIds.LUSH_FOREST = config.get(Strings.CATEGORY_BIOME, Strings.LUSH_FOREST_NAME, BiomeIds.LUSH_FOREST_DEFAULT).getInt(BiomeIds.LUSH_FOREST_DEFAULT);
         BiomeIds.LUSH_SWAMP = config.get(Strings.CATEGORY_BIOME, Strings.LUSH_SWAMP_NAME, BiomeIds.LUSH_SWAMP_DEFAULT).getInt(BiomeIds.LUSH_SWAMP_DEFAULT);
         BiomeIds.EXTREME_BIRCH_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_BIRCH_WOODS, BiomeIds.EXTREME_BIRCH_WOODS_DEFAULT).getInt(BiomeIds.EXTREME_BIRCH_WOODS_DEFAULT);
         BiomeIds.EXTREME_DESERT_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_DESERT_HILLS, BiomeIds.EXTREME_DESERT_HILLS_DEFAULT).getInt(BiomeIds.EXTREME_DESERT_HILLS_DEFAULT);
         BiomeIds.EXTREME_DUNES = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_DUNES, BiomeIds.EXTREME_DUNES_DEFAULT).getInt(BiomeIds.EXTREME_DUNES_DEFAULT);
         BiomeIds.EXTREME_FORESTED_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_FORESTED_HILLS, BiomeIds.EXTREME_FORESTED_HILLS_DEFAULT).getInt(BiomeIds.EXTREME_FORESTED_HILLS_DEFAULT);
         BiomeIds.EXTREME_GRASS_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_GRASS_HILLS, BiomeIds.EXTREME_GRASS_HILLS_DEFAULT).getInt(BiomeIds.EXTREME_GRASS_HILLS_DEFAULT);
         BiomeIds.EXTREME_PINE_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_PINE_HILLS, BiomeIds.EXTREME_PINE_HILLS_DEFAULT).getInt(BiomeIds.EXTREME_PINE_HILLS_DEFAULT);
         BiomeIds.EXTREME_SNOW_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_SNOW_HILLS, BiomeIds.EXTREME_SNOW_HILLS_DEFAULT).getInt(BiomeIds.EXTREME_SNOW_HILLS_DEFAULT);
         BiomeIds.EXTREME_SPARSE_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_SPARSE_WOODS, BiomeIds.EXTREME_SPARSE_WOODS_DEFAULT).getInt(BiomeIds.EXTREME_SPARSE_WOODS_DEFAULT);
         BiomeIds.MASSIVE_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.MASSIVE_HILLS, BiomeIds.MASSIVE_HILLS_DEFAULT).getInt(BiomeIds.MASSIVE_HILLS_DEFAULT);
         BiomeIds.EXTREME_WOODS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_WOODS, BiomeIds.EXTREME_WOODS_DEFAULT).getInt(BiomeIds.EXTREME_WOODS_DEFAULT);
        BiomeIds.EXTREME_MEADOW_HILLS = config.get(Strings.CATEGORY_BIOME, Strings.EXTREME_MEADOW_HILLS, BiomeIds.EXTREME_MEADOW_HILLS_DEFAULT).getInt(BiomeIds.EXTREME_MEADOW_HILLS_DEFAULT);
 	}
 	
 	public static void allowBiomes(){
 		BiomeAllow.RAIN_FOREST = config.get(Strings.CATEGORY_BIOME_ON, Strings.RAIN_FOREST_NAME, BiomeAllow.RAIN_FOREST_DEFAULT).getBoolean(BiomeAllow.RAIN_FOREST_DEFAULT);
 		BiomeAllow.BIRCH_JUNGLE = config.get(Strings.CATEGORY_BIOME_ON, Strings.BIRCH_JUNGLE_NAME, BiomeAllow.BIRCH_JUNGLE_DEFAULT).getBoolean(BiomeAllow.BIRCH_JUNGLE_DEFAULT);
 		BiomeAllow.OAK_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.OAK_WOODS_NAME, BiomeAllow.OAK_WOODS_DEFAULT).getBoolean(BiomeAllow.OAK_WOODS_DEFAULT);
 		BiomeAllow.THICK_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.THICK_WOODS_NAME, BiomeAllow.THICK_WOODS_DEFAULT).getBoolean(BiomeAllow.THICK_WOODS_DEFAULT);
 		BiomeAllow.SPARSE_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.SPARSE_WOODS_NAME, BiomeAllow.SPARSE_WOODS_DEFAULT).getBoolean(BiomeAllow.SPARSE_WOODS_DEFAULT);
 		BiomeAllow.SNOWY_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.SNOWY_WOODS_NAME, BiomeAllow.SNOWY_WOODS_DEFAULT).getBoolean(BiomeAllow.SNOWY_WOODS_DEFAULT);
 		BiomeAllow.LUSH_PLAINS = config.get(Strings.CATEGORY_BIOME_ON, Strings.LUSH_PLAINS_NAME, BiomeAllow.LUSH_PLAINS_DEFAULT).getBoolean(BiomeAllow.LUSH_PLAINS_DEFAULT);
 		BiomeAllow.LUSH_MOUNTAINS = config.get(Strings.CATEGORY_BIOME_ON, Strings.LUSH_MOUNTAINS_NAME, BiomeAllow.LUSH_MOUNTAINS_DEFAULT).getBoolean(BiomeAllow.LUSH_MOUNTAINS_DEFAULT);
 		BiomeAllow.PINE_FOREST = config.get(Strings.CATEGORY_BIOME_ON, Strings.PINE_FOREST_NAME, BiomeAllow.PINE_FOREST_DEFAULT).getBoolean(BiomeAllow.PINE_FOREST_DEFAULT);
 		BiomeAllow.MEADOWS = config.get(Strings.CATEGORY_BIOME_ON, Strings.MEADOWS_NAME, BiomeAllow.MEADOWS_DEFAULT).getBoolean(BiomeAllow.MEADOWS_DEFAULT);
 		BiomeAllow.SHRUBLANDS = config.get(Strings.CATEGORY_BIOME_ON, Strings.SHRUBLANDS_NAME, BiomeAllow.SHRUBLANDS_DEFAULT).getBoolean(BiomeAllow.SHRUBLANDS_DEFAULT);
 		BiomeAllow.SNOWY_SHRUBLANDS = config.get(Strings.CATEGORY_BIOME_ON, Strings.SNOWY_SHRUBLANDS_NAME, BiomeAllow.SNOWY_SHRUBLANDS_DEFAULT).getBoolean(BiomeAllow.SNOWY_SHRUBLANDS_DEFAULT);
 		BiomeAllow.MARSH = config.get(Strings.CATEGORY_BIOME_ON, Strings.MARSH_NAME, BiomeAllow.MARSH_DEFAULT).getBoolean(BiomeAllow.MARSH_DEFAULT);
 		BiomeAllow.SAVANNAH = config.get(Strings.CATEGORY_BIOME_ON, Strings.SAVANNAH_NAME, BiomeAllow.SAVANNAH_DEFAULT).getBoolean(BiomeAllow.SAVANNAH_DEFAULT);
 		BiomeAllow.BIRCH_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.BIRCH_WOODS_NAME, BiomeAllow.BIRCH_WOODS_DEFAULT).getBoolean(BiomeAllow.BIRCH_WOODS_DEFAULT);
 		BiomeAllow.OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME_ON, Strings.OAK_MOUNTAINS_NAME, BiomeAllow.OAK_MOUNTAINS_DEFAULT).getBoolean(BiomeAllow.OAK_MOUNTAINS_DEFAULT);
 		BiomeAllow.THICK_OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME_ON, Strings.THICK_OAK_MOUNTAINS_NAME, BiomeAllow.THICK_OAK_MOUNTAINS_DEFAULT).getBoolean(BiomeAllow.THICK_OAK_MOUNTAINS_DEFAULT);
 		BiomeAllow.SPARSE_OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME_ON, Strings.SPARSE_OAK_MOUNTAINS_NAME, BiomeAllow.SPARSE_OAK_MOUNTAINS_DEFAULT).getBoolean(BiomeAllow.SPARSE_OAK_MOUNTAINS_DEFAULT);
 		BiomeAllow.SNOWY_OAK_MOUNTAINS = config.get(Strings.CATEGORY_BIOME_ON, Strings.SNOWY_OAK_MOUNTAINS_NAME, BiomeAllow.SNOWY_OAK_MOUNTAINS_DEFAULT).getBoolean(BiomeAllow.SNOWY_OAK_MOUNTAINS_DEFAULT);
 		BiomeAllow.BIRCH_MOUNTAINS_DEFAULT = config.get(Strings.CATEGORY_BIOME_ON, Strings.BIRCH_MOUNTAINS_NAME, BiomeAllow.BIRCH_MOUNTAINS_DEFAULT).getBoolean(BiomeAllow.BIRCH_MOUNTAINS_DEFAULT);
 		BiomeAllow.DUNES = config.get(Strings.CATEGORY_BIOME_ON, Strings.DUNES_NAME, BiomeAllow.DUNES_DEFAULT).getBoolean(BiomeAllow.DUNES_DEFAULT);
 		BiomeAllow.THICK_SWAMP = config.get(Strings.CATEGORY_BIOME_ON, Strings.THICK_SWAMP_NAME, BiomeAllow.THICK_SWAMP_DEFAULT).getBoolean(BiomeAllow.THICK_SWAMP_DEFAULT);
 		BiomeAllow.LUSH_FOREST = config.get(Strings.CATEGORY_BIOME_ON, Strings.LUSH_FOREST_NAME, BiomeAllow.LUSH_FOREST_DEFAULT).getBoolean(BiomeAllow.LUSH_FOREST_DEFAULT);
 		BiomeAllow.LUSH_SWAMP = config.get(Strings.CATEGORY_BIOME_ON, Strings.LUSH_SWAMP_NAME, BiomeAllow.LUSH_SWAMP_DEFAULT).getBoolean(BiomeAllow.LUSH_SWAMP_DEFAULT);
 		BiomeAllow.EXTREME_BIRCH_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_BIRCH_WOODS, BiomeAllow.EXTREME_BIRCH_WOODS_DEFAULT).getBoolean(BiomeAllow.EXTREME_BIRCH_WOODS_DEFAULT);
         BiomeAllow.EXTREME_DESERT_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_DESERT_HILLS, BiomeAllow.EXTREME_DESERT_HILLS_DEFAULT).getBoolean(BiomeAllow.EXTREME_DESERT_HILLS_DEFAULT);
         BiomeAllow.EXTREME_DUNES = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_DUNES, BiomeAllow.EXTREME_DUNES_DEFAULT).getBoolean(BiomeAllow.EXTREME_DUNES_DEFAULT);
         BiomeAllow.EXTREME_FORESTED_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_FORESTED_HILLS, BiomeAllow.EXTREME_FORESTED_HILLS_DEFAULT).getBoolean(BiomeAllow.EXTREME_FORESTED_HILLS_DEFAULT);
         BiomeAllow.EXTREME_GRASS_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_GRASS_HILLS, BiomeAllow.EXTREME_GRASS_HILLS_DEFAULT).getBoolean(BiomeAllow.EXTREME_GRASS_HILLS_DEFAULT);
         BiomeAllow.EXTREME_PINE_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_PINE_HILLS, BiomeAllow.EXTREME_PINE_HILLS_DEFAULT).getBoolean(BiomeAllow.EXTREME_PINE_HILLS_DEFAULT);
         BiomeAllow.EXTREME_SNOW_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_SNOW_HILLS, BiomeAllow.EXTREME_SNOW_HILLS_DEFAULT).getBoolean(BiomeAllow.EXTREME_SNOW_HILLS_DEFAULT);
         BiomeAllow.EXTREME_SPARSE_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_SPARSE_WOODS, BiomeAllow.EXTREME_SPARSE_WOODS_DEFAULT).getBoolean(BiomeAllow.EXTREME_SPARSE_WOODS_DEFAULT);
        BiomeAllow.EXTREME_MEADOW_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_MEADOW_HILLS, BiomeAllow.EXTREME_MEADOW_HILLS_DEFAULT).getBoolean(BiomeAllow.EXTREME_MEADOW_HILLS_DEFAULT);
         BiomeAllow.MASSIVE_HILLS = config.get(Strings.CATEGORY_BIOME_ON, Strings.MASSIVE_HILLS, BiomeAllow.MASSIVE_HILLS_DEFAULT).getBoolean(BiomeAllow.MASSIVE_HILLS_DEFAULT);
         BiomeAllow.EXTREME_WOODS = config.get(Strings.CATEGORY_BIOME_ON, Strings.EXTREME_WOODS, BiomeAllow.EXTREME_WOODS_DEFAULT).getBoolean(BiomeAllow.EXTREME_WOODS_DEFAULT);
 	}
 	
 	public static void allowVillages(){
 		VillageAllow.RAIN_FOREST = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.RAIN_FOREST_NAME, VillageAllow.RAIN_FOREST_DEFAULT).getBoolean(VillageAllow.RAIN_FOREST_DEFAULT);
 		VillageAllow.BIRCH_JUNGLE = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.BIRCH_JUNGLE_NAME, VillageAllow.BIRCH_JUNGLE_DEFAULT).getBoolean(VillageAllow.BIRCH_JUNGLE_DEFAULT);
 		VillageAllow.OAK_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.OAK_WOODS_NAME, VillageAllow.OAK_WOODS_DEFAULT).getBoolean(VillageAllow.OAK_WOODS_DEFAULT);
 		VillageAllow.THICK_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.THICK_WOODS_NAME, VillageAllow.THICK_WOODS_DEFAULT).getBoolean(VillageAllow.THICK_WOODS_DEFAULT);
 		VillageAllow.SPARSE_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SPARSE_WOODS_NAME, VillageAllow.SPARSE_WOODS_DEFAULT).getBoolean(VillageAllow.SPARSE_WOODS_DEFAULT);
 		VillageAllow.SNOWY_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SNOWY_WOODS_NAME, VillageAllow.SNOWY_WOODS_DEFAULT).getBoolean(VillageAllow.SNOWY_WOODS_DEFAULT);
 		VillageAllow.LUSH_PLAINS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.LUSH_PLAINS_NAME, VillageAllow.LUSH_PLAINS_DEFAULT).getBoolean(VillageAllow.LUSH_PLAINS_DEFAULT);
 		VillageAllow.LUSH_MOUNTAINS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.LUSH_MOUNTAINS_NAME, VillageAllow.LUSH_MOUNTAINS_DEFAULT).getBoolean(VillageAllow.LUSH_MOUNTAINS_DEFAULT);
 		VillageAllow.PINE_FOREST = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.PINE_FOREST_NAME, VillageAllow.PINE_FOREST_DEFAULT).getBoolean(VillageAllow.PINE_FOREST_DEFAULT);
 		VillageAllow.MEADOWS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.MEADOWS_NAME, VillageAllow.MEADOWS_DEFAULT).getBoolean(VillageAllow.MEADOWS_DEFAULT);
 		VillageAllow.SHRUBLANDS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SHRUBLANDS_NAME, VillageAllow.SHRUBLANDS_DEFAULT).getBoolean(VillageAllow.SHRUBLANDS_DEFAULT);
 		VillageAllow.SNOWY_SHRUBLANDS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SNOWY_SHRUBLANDS_NAME, VillageAllow.SNOWY_SHRUBLANDS_DEFAULT).getBoolean(VillageAllow.SNOWY_SHRUBLANDS_DEFAULT);
 		VillageAllow.MARSH = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.MARSH_NAME, VillageAllow.MARSH_DEFAULT).getBoolean(VillageAllow.MARSH_DEFAULT);
 		VillageAllow.SAVANNAH = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SAVANNAH_NAME, VillageAllow.SAVANNAH_DEFAULT).getBoolean(VillageAllow.SAVANNAH_DEFAULT);
 		VillageAllow.BIRCH_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.BIRCH_WOODS_NAME, VillageAllow.BIRCH_WOODS_DEFAULT).getBoolean(VillageAllow.BIRCH_WOODS_DEFAULT);
 		VillageAllow.OAK_MOUNTAINS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.OAK_MOUNTAINS_NAME, VillageAllow.OAK_MOUNTAINS_DEFAULT).getBoolean(VillageAllow.OAK_MOUNTAINS_DEFAULT);
 		VillageAllow.THICK_OAK_MOUNTAINS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.THICK_OAK_MOUNTAINS_NAME, VillageAllow.THICK_OAK_MOUNTAINS_DEFAULT).getBoolean(VillageAllow.THICK_OAK_MOUNTAINS_DEFAULT);
 		VillageAllow.SPARSE_OAK_MOUNTAINS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SPARSE_OAK_MOUNTAINS_NAME, VillageAllow.SPARSE_OAK_MOUNTAINS_DEFAULT).getBoolean(VillageAllow.SPARSE_OAK_MOUNTAINS_DEFAULT);
 		VillageAllow.SNOWY_OAK_MOUNTAINS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.SNOWY_OAK_MOUNTAINS_NAME, VillageAllow.SNOWY_OAK_MOUNTAINS_DEFAULT).getBoolean(VillageAllow.SNOWY_OAK_MOUNTAINS_DEFAULT);
 		VillageAllow.BIRCH_MOUNTAINS_DEFAULT = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.BIRCH_MOUNTAINS_NAME, VillageAllow.BIRCH_MOUNTAINS_DEFAULT).getBoolean(VillageAllow.BIRCH_MOUNTAINS_DEFAULT);
 		VillageAllow.DUNES = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.DUNES_NAME, VillageAllow.DUNES_DEFAULT).getBoolean(VillageAllow.DUNES_DEFAULT);
 		VillageAllow.THICK_SWAMP = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.THICK_SWAMP_NAME, VillageAllow.THICK_SWAMP_DEFAULT).getBoolean(VillageAllow.THICK_SWAMP_DEFAULT);
 		VillageAllow.LUSH_FOREST = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.LUSH_FOREST_NAME, VillageAllow.LUSH_FOREST_DEFAULT).getBoolean(VillageAllow.LUSH_FOREST_DEFAULT);
 		VillageAllow.LUSH_SWAMP = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.LUSH_SWAMP_NAME, VillageAllow.LUSH_SWAMP_DEFAULT).getBoolean(VillageAllow.LUSH_SWAMP_DEFAULT);
 		VillageAllow.EXTREME_BIRCH_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_BIRCH_WOODS, VillageAllow.EXTREME_BIRCH_WOODS_DEFAULT).getBoolean(VillageAllow.EXTREME_BIRCH_WOODS_DEFAULT);
         VillageAllow.EXTREME_DESERT_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_DESERT_HILLS, VillageAllow.EXTREME_DESERT_HILLS_DEFAULT).getBoolean(VillageAllow.EXTREME_DESERT_HILLS_DEFAULT);
         VillageAllow.EXTREME_DUNES = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_DUNES, VillageAllow.EXTREME_DUNES_DEFAULT).getBoolean(VillageAllow.EXTREME_DUNES_DEFAULT);
         VillageAllow.EXTREME_FORESTED_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_FORESTED_HILLS, VillageAllow.EXTREME_FORESTED_HILLS_DEFAULT).getBoolean(VillageAllow.EXTREME_FORESTED_HILLS_DEFAULT);
         VillageAllow.EXTREME_GRASS_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_GRASS_HILLS, VillageAllow.EXTREME_GRASS_HILLS_DEFAULT).getBoolean(VillageAllow.EXTREME_GRASS_HILLS_DEFAULT);
         VillageAllow.EXTREME_PINE_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_PINE_HILLS, VillageAllow.EXTREME_PINE_HILLS_DEFAULT).getBoolean(VillageAllow.EXTREME_PINE_HILLS_DEFAULT);
         VillageAllow.EXTREME_SNOW_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_SNOW_HILLS, VillageAllow.EXTREME_SNOW_HILLS_DEFAULT).getBoolean(VillageAllow.EXTREME_SNOW_HILLS_DEFAULT);
         VillageAllow.EXTREME_SPARSE_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_SPARSE_WOODS, VillageAllow.EXTREME_SPARSE_WOODS_DEFAULT).getBoolean(VillageAllow.EXTREME_SPARSE_WOODS_DEFAULT);
        VillageAllow.EXTREME_MEADOW_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_MEADOW_HILLS, VillageAllow.EXTREME_MEADOW_HILLS_DEFAULT).getBoolean(VillageAllow.EXTREME_MEADOW_HILLS_DEFAULT);
         VillageAllow.MASSIVE_HILLS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.MASSIVE_HILLS, VillageAllow.MASSIVE_HILLS_DEFAULT).getBoolean(VillageAllow.MASSIVE_HILLS_DEFAULT);
         VillageAllow.EXTREME_WOODS = config.get(Strings.CATEGORY_VILLAGE_ON, Strings.EXTREME_WOODS, VillageAllow.EXTREME_WOODS_DEFAULT).getBoolean(VillageAllow.EXTREME_WOODS_DEFAULT);
 	}
 }
