 package mods.natureoverhaul;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCactus;
 import net.minecraft.block.BlockCocoa;
 import net.minecraft.block.BlockCrops;
 import net.minecraft.block.BlockFlower;
 import net.minecraft.block.BlockGrass;
 import net.minecraft.block.BlockLeaves;
 import net.minecraft.block.BlockLog;
 import net.minecraft.block.BlockMushroom;
 import net.minecraft.block.BlockMushroomCap;
 import net.minecraft.block.BlockMycelium;
 import net.minecraft.block.BlockNetherStalk;
 import net.minecraft.block.BlockReed;
 import net.minecraft.block.BlockSapling;
 import net.minecraft.block.BlockStem;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 
 import com.google.common.primitives.Ints;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = "NatureOverhaul", name = "Nature Overhaul", version = "0.3",dependencies="after:mod_MOAPI")
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 /**
  * From Clinton Alexander idea.
  * @author Olivier
  *
  */
 public class NatureOverhaul implements ITickHandler{
 	@Instance ("NatureOverhaul")
 	public static NatureOverhaul instance;
 	private static boolean autoSapling=true,autoFarming=true,lumberjack=true,moddedBonemeal=true,
 			killLeaves=true,biomeModifiedRate=true,useStarvingSystem=true,decayLeaves=true,
 			mossCorruptStone=true,customDimension=true,wildAnimalsBreed=true;
 	private static int wildAnimalBreedRate=0,growthType=0;
 	private int updateLCG = (new Random()).nextInt();
 	
 	private static HashMap<String,Integer> valueToGrowthTypeMapping = new HashMap();
 	static{
 		valueToGrowthTypeMapping.put("Neither", 0);
 		valueToGrowthTypeMapping.put("Growth", 1);
 		valueToGrowthTypeMapping.put("Decay", 2);
 		valueToGrowthTypeMapping.put("Both", 3);
 	}
 	private static HashMap<Integer,NOType> IDToTypeMapping = new HashMap();
 	private static HashMap<Integer,Boolean> IDToGrowingMapping = new HashMap(),IDToDyingMapping = new HashMap();
 	private static HashMap<Integer,Float> IDToOptTempMapping = new HashMap(),IDToOptRainMapping = new HashMap();
 	private static HashMap<Integer,Integer> IDToGrowthRateMapping= new HashMap(),IDToDeathRateMapping= new HashMap(),
 			LogToLeafMapping=new HashMap(),IDToFireCatchMapping=new HashMap(),IDToFirePropagateMapping=new HashMap();
 	private static String[] names=new String[]
     	{
     	"Sapling","Tree","Plants","Netherwort","Grass","Reed","Cactus","Mushroom","Mushroom Tree","Leaf","Crops","Moss","Cocoa"
     	};
     private static boolean[] dieSets=new boolean[names.length],growSets=new boolean[names.length+1];
 	private static int[] deathRates=new int[names.length],growthRates=new int[names.length+1];
     private static String[] optionsCategory=new String[names.length+1];
     		static{
     		for(int i=0;i<names.length;i++)
     		{
     			optionsCategory[i]=names[i]+" Options";
     		}
     		optionsCategory[names.length]="Misc Options";
     		};
     private static int[] idLog=new int[]{17},idLeaf=new int[]{18};
     private Configuration config;
 	private boolean API;
 	private BonemealEventHandler bonemealEvent;
 	private AnimalEventHandler animalEvent;
 	private PlayerEventHandler lumberEvent;
 	private AutoSaplingEventHandler autoEvent;
 	private AutoFarmingEventHandler farmingEvent;
 	
     @PreInit
     public void preInit(FMLPreInitializationEvent event)
     {
         instance = this;
         config = new Configuration(event.getSuggestedConfigurationFile(),true);
         config.load();
         for(String name:optionsCategory)
         {
         	config.addCustomCategoryComment(name,"The lower the rate, the faster the changes happen.");
         }
     	config.addCustomCategoryComment(optionsCategory[2],"Plants are flower, deadbush, lilypad and tallgrass");
     	config.addCustomCategoryComment(optionsCategory[names.length],"Log ids and leaves ids should have the same length and include all compatible blocks");
     	
     	autoSapling=config.get(optionsCategory[0],"AutoSapling",true).getBoolean(true);
         for (int i=0;i<names.length;i++)
         {     
         	dieSets[i]=config.get(optionsCategory[i],names[i]+" Die",true).getBoolean(true);
         	growSets[i]=config.get(optionsCategory[i],names[i]+" Grow",true).getBoolean(true);
         	deathRates[i]=config.get(optionsCategory[i],names[i]+" Death Rate",1200).getInt(1200);
         	growthRates[i]=config.get(optionsCategory[i],names[i]+" Growth Rate",1200).getInt(1200);
         }
         //Toggle between alternative time of growth for sapling
         growthType=valueToGrowthTypeMapping.get(config.get(optionsCategory[0],"Sapling grow on","Both","WIP setting, possible values are Neither,Growth,Decay,Both").getString());
         //Toggle for lumberjack system on trees
         lumberjack=config.get(optionsCategory[1],"Enable lumberjack",true).getBoolean(true);
         killLeaves=config.get(optionsCategory[1],"Lumberjack kill leaves",true).getBoolean(true);
         //Apples don't have a dying system, because it is only an item
         growSets[names.length]=config.get(optionsCategory[9],"Apple Grows",true).getBoolean(true);
         growthRates[names.length]=config.get(optionsCategory[9],"Apple Growth Rate",3000).getInt(3000);            
         //Force remove leaves after killing a tree, instead of letting Minecraft doing it
         decayLeaves=config.get(optionsCategory[9],"Enable leaves decay on tree death",true).getBoolean(true);      
         //Toggle so Stone can turn into Mossy Cobblestone
         mossCorruptStone=config.get(optionsCategory[11],"Enable moss growing on stone",true).getBoolean(true);      
         //Misc options
         useStarvingSystem=config.get(optionsCategory[names.length],"Enable starving system",true).getBoolean(true);      
         biomeModifiedRate=config.get(optionsCategory[names.length],"Enable biome specific rates",true).getBoolean(true);
         moddedBonemeal=config.get(optionsCategory[names.length],"Enable modded Bonemeal",true).getBoolean(true);
         customDimension=config.get(optionsCategory[names.length],"Enable custom dimensions",true).getBoolean(true);
         wildAnimalsBreed=config.get(optionsCategory[names.length],"Enable wild animals Breed",true).getBoolean(true);
         wildAnimalBreedRate=config.get(optionsCategory[names.length],"Wild animals breed rate",16000).getInt(16000);
         autoFarming=config.get(optionsCategory[names.length], "Plant seeds on player drop", true).getBoolean(true);
     }
     @Init
     public void load(FMLInitializationEvent event)
     {	
     	TickRegistry.registerTickHandler(this, Side.SERVER);
     }
    
 	/**
 	* Called from the world tick {@link #tickStart(EnumSet, Object...)} with a {@link #isValid(int)} id.
 	* Checks with {@link #isGrowing(int)} or {@link #isMortal(int)} booleans, 
 	* and probabilities with {@link #getGrowthProb(World, int, int, int, int, NOType)} or 
 	* {@link #getDeathProb(World, int, int, int, int, NOType)}
 	* then call {@link #grow(World, int, int, int, int, NOType)} or {@link #death(World, int, int, int, int, NOType)}.
 	*/
 	private void onUpdateTick(World world, int i, int j, int k, int id)	
 	{	
 		NOType type=Utils.getType(id);
 		if( isGrowing(id) && world.rand.nextFloat() < getGrowthProb(world, i, j, k, id, type)) 
 		{
 			//System.out.println("Block "+id+" growing at "+i+","+j+","+k);
 			grow(world, i, j, k, id, type);				
 		}
 		if (isMortal(id) && (hasDied(world, i, j, k, id, type) || world.rand.nextFloat() < getDeathProb(world, i, j, k, id, type)))
 		{
 			//System.out.println("Block "+id+" dying at "+i+","+j+","+k);		
 			death(world, i, j, k, id, type);
 		}
 	}
 	/**
 	 * Called by {@link #onUpdateTick(World, int, int, int, int)}.
 	* Checks whether this block has died on this tick for any reason
 	* @param	world
 	* @param	i first coordinate
 	* @param	j second coordinate
 	* @param	k third coordinate
 	* @return	True if plant has died
 	*/
 	private boolean hasDied(World world, int i, int j, int k, int id, NOType type) {
 		switch(type){
 		case CUSTOM:
 			if (Block.blocksList[id] instanceof IBlockDeath)
 				return ((IBlockDeath)Block.blocksList[id]).hasDied(world,i,j,k);
 			return false;
 		case CACTUS:case REED:case GRASS:case PLANT:case MUSHROOM:case COCOA:
 			case FERTILIZED:case MOSS:case NETHERSTALK:case SAPLING:
 				return useStarvingSystem && hasStarved(world, i, j, k, type);
 		case LOG:case MUSHROOMCAP:case LEAVES://Trees and leaves are only randomly dying
 			return false;	
 		default:
 			return false;	
 		}	
 	}
 	/**
 	* Checks whether this block has starved on this tick
 	* by being surrounded by too many of it's kind (ie {@link NOType}).
 	* Unused if {@link #useStarvingSystem} is set to false.
 	* @param	world
 	* @param	i first coordinate
 	* @param	j second coordinate
 	* @param	k third coordinate
 	* @return	True if plant has starved
 	*/
 	private boolean hasStarved(World world, int i, int j, int k, NOType type) {
 		int radius = 1;
 		int maxNeighbours = 9;
 		int foundNeighbours = 0;
 		switch(type){
 		case CACTUS:case REED:
 			radius = 2;
 			break;
 		case MOSS:
 			maxNeighbours = 15;
 			break;
 		case NETHERSTALK:case COCOA:case GRASS:case MUSHROOM:case PLANT:
 			maxNeighbours = 5;
 			break;
 		case FERTILIZED:
 			maxNeighbours = 6;
 			break;
 		case SAPLING:
 			radius = 2;
 			maxNeighbours = 5;
 			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);		
 			if(biome.temperature > 1F) {
 				radius = 4;
 				maxNeighbours = 1;
 			}		
 			if(biome.rainfall < 0.5F) {
 				radius++;
 			}					
 			if(biome.temperature <= 0F) 
 				maxNeighbours = 1;			
 			maxNeighbours = maxNeighbours + (int) Math.ceil(biome.rainfall / 0.2) - 2;		
 			if(maxNeighbours < 0) 
 				maxNeighbours=0;		
 			break;
 		default://All cases have been set, this is for safety
 			return false;
 		}	
 		if((radius > 0) && (radius < 10)) {
 			for(int x = i - radius; x < i + radius; x++) {
 				for(int y = j - radius; y < j + radius; y++) {
 					for(int z = k - radius; z < k + radius; z++) {
 						if((i != x) || (j != y) || (k != z)) {
 							int blockID = world.getBlockId(x, y, z);
 							if(foundNeighbours <= maxNeighbours && isValid(blockID) && Utils.getType(blockID) == type) {
 								foundNeighbours++;
 							}
 						}
 					}
 				}
 			}
 		}	
 		return (foundNeighbours > maxNeighbours);
 	}
 	/**
 	 * The death general method.
 	* Called by {@link #onUpdateTick(World, int, int, int, int)}
 	* when conditions are fulfilled.
 	* @param	world
 	* @param	i first coordinate
 	* @param	j second coordinate
 	* @param	k third coordinate
 	**/
 	private void death(World world, int i, int j, int k, int id, NOType type) {
 		switch(type){
 		case CUSTOM:
 			if (Block.blocksList[id] instanceof IBlockDeath)
 				((IBlockDeath)Block.blocksList[id]).death(world, i, j, k);
 			return;
 		case CACTUS:case REED://Disappear completely from top to bottom
 			int y = j;
 			// Get to the top so to avoid any being dropped since this is death
 			while(world.getBlockId(i, y + 1, k) == id) {
 				y = y + 1;
 			}
 			// Scan back down and delete
 			while(world.getBlockId(i, y, k) == id) {
 				world.setBlockToAir(i, y, k);
 				y--;
 			}
 			return;		
 		case LOG:case MUSHROOMCAP:
 			if(TreeUtils.isTree(world, i, j, k, type, false))
 			{
 				TreeUtils.killTree(world, i, Utils.getLowestTypeJ(world, i, j, k , type), k, id, type==NOType.LOG?decayLeaves:false);
 			}
 			return;
 		case MOSS://Return to cobblestone
 			world.setBlock(i,j,k,Block.cobblestone.blockID);
 			return;
 		case LEAVES://Has a chance to emit a sapling if sets accordingly
 			if(growthType > 1 && world.rand.nextFloat() < getGrowthProb(world, i, j, k, Block.sapling.blockID, NOType.SAPLING))
 				Utils.emitItem(world, i, j, k, new ItemStack(Block.sapling, 1, 
 						 world.getBlockMetadata(i, j, k) % 4));
 			world.setBlockToAir(i, j, k);//Then disappear
 			return;
 		case NETHERSTALK:case PLANT:case MUSHROOM:case COCOA:case SAPLING:
 			world.setBlockToAir(i, j, k);//Disappear
 			return;
 		case GRASS: //Return to dirt
 			world.setBlock(i,j,k,Block.dirt.blockID);
 			return;
 		case FERTILIZED: //Ungrow, or turn to dirt if too low
 			int meta = world.getBlockMetadata(i, j, k);
 			if (meta>=1)
 				world.setBlockMetadataWithNotify(i, j, k, meta-1, 2);
 			else {
 				world.setBlockToAir(i,j,k);
 				world.setBlock(i,j-1,k,Block.dirt.blockID);
 			}	
 			return;
 		default:
 			return;		
 		}
 	}
 	/**
 	 * The general growing method.
 	* Called by {@link #onUpdateTick(World, int, int, int, int)}.
 	* when conditions are fulfilled.
 	* @param	world
 	* @param	i first coordinate
 	* @param	j second coordinate
 	* @param	k third coordinate
 	**/
 	public void grow(World world, int i, int j, int k, int id, NOType type) {
 		int scanSize;
 		int[] coord;
 		switch(type){
 		case CUSTOM:
 			if (Block.blocksList[id] instanceof IGrowable)
 				((IGrowable)Block.blocksList[id]).grow(world, i, j, k);
 			return;
 		case CACTUS:case REED://Grow on top if too low, or on a neighbor spot		
 			if(TreeUtils.getTreeHeight(world, i, j, k, id)>2)
 			{//Find a neighbor spot for new one
 				scanSize = 2;
 				for(int attempt=0;attempt<18;attempt++){
 					coord=Utils.findRandomNeighbour(i, j, k, scanSize);
 					if(Block.blocksList[id].canPlaceBlockAt(world, coord[0], coord[1], coord[2])){
 						world.setBlock(coord[0], coord[1], coord[2], id);
 						return;
 					}		
 				}
 			}
 			else{
 				int height = j;
 				// Get to the top
 				while(world.getBlockId(i, height + 1, k) == id) {
 					height = height + 1;
 				}
 				if (world.getBlockId(i, height + 1, k) == 0)
 					world.setBlock(i, height + 1, k, id);//Grow on top
 			}				
 			return;
 		case COCOA://Emit cocoa dye
 			if(world.getBlockId(i, j - 1, k) == 0 && cocoaCanGrow(world,i,k)) {
 				Utils.emitItem(world, i, j - 1, k, new ItemStack(Item.dyePowder, 1, 3));			
 			}
 			return;
 		case LEAVES://Emit apples and saplings (with odd growthType)
 			if (world.getBlockId(i, j - 1, k) == 0 && appleCanGrow(world, i, k) 
 			&& Math.random()<(double)getGrowthProb( world, i, j, k, id, NOType.APPLE))
 				Utils.emitItem(world, i, j - 1, k, new ItemStack(Item.appleRed));
 			if(growthType % 2 ==1 && world.getBlockId(i, j + 1, k) == 0) 
 				Utils.emitItem(world, i, j + 1, k, new ItemStack(Block.sapling, 1,
 										world.getBlockMetadata(i, j, k) % 4));
 			return;
 		case SAPLING://Use sapling vanilla method for growing a tree
 			((BlockSapling) Block.blocksList[id]).growTree(world, i, j, k, world.rand);
 			return;
 		case LOG://case MUSHROOMCAP:
 			if (TreeUtils.isTree(world, i, j, k, type, false))
 			{		
 				TreeUtils.growTree(world, i, j, k, id, type);
 			}		
 			return;
 		case GRASS://Replace surrounding dirt with grass
 			scanSize = 1;
 			for(int x = i - scanSize; x <= i + scanSize; x++) {
 				for(int y = j - scanSize; y <= j + scanSize; y++) {
 					for(int z = k - scanSize; z <= k + scanSize; z++) {
 						if(world.getBlockId(x, y, z) == Block.dirt.blockID){
 							world.setBlock(x, y, z, id);
 						}
 					}
 				}
 			}
 			return;
 		case PLANT:case NETHERSTALK:
 			scanSize = 2;
 			for(int attempt=0;attempt<18;attempt++){
 				coord=Utils.findRandomNeighbour(i, j, k, scanSize);
 				if( Block.blocksList[id].canPlaceBlockAt(world, coord[0], coord[1], coord[2]) && world.getBlockMaterial(coord[0], coord[1], coord[2])!=Material.water){
 					if (id!=Block.tallGrass.blockID)
 					{
 						world.setBlock(coord[0], coord[1], coord[2], id);							
 					}
 					else//Metadata sensitive for tall grass
 					{
 						world.setBlock(coord[0], coord[1], coord[2], id, world.getBlockMetadata(i, j, k), 3);
 					}
 					return;
 				}
 			}
 			return;
 		case MOSS://Moss grows on both stone (or only cobblestone), changing only one block
 			scanSize = 1;
 			int iD;
 			for(int attempt=0;attempt<15;attempt++){
 				coord=Utils.findRandomNeighbour(i, j, k, scanSize);
 				iD= world.getBlockId(coord[0], coord[1], coord[2]);
 				if((mossCorruptStone && iD == Block.stone.blockID) || iD == Block.cobblestone.blockID) {
 					world.setBlock(coord[0], coord[1], coord[2], id);
 					return;
 				}
 			}
 			return;
 		case MUSHROOM://Small chance of having a mushroom tree, grown using vanilla method
 			if (Math.random()<(double)getGrowthProb(world, i, j, k, id+60 ,NOType.MUSHROOMCAP))
 				((BlockMushroom) Block.blocksList[id]).fertilizeMushroom(world, i, j, k, world.rand);
 			else//Grow a similar mushroom nearby
 			{
 				scanSize = 3;
 				for(int attempt=0;attempt<15;attempt++){
 					coord=Utils.findRandomNeighbour(i, j, k, scanSize);
 					if(((BlockMushroom) Block.blocksList[id]).canPlaceBlockAt(world, coord[0], coord[1], coord[2])) {
 						world.setBlock(coord[0], coord[1], coord[2], id);
 						return;
 					}
 				}
 			}
 			return;
 		case FERTILIZED://Blocks that uses fertilize function for grow
 			if (Block.blocksList[id] instanceof BlockStem)
 				((BlockStem)Block.blocksList[id]).fertilizeStem(world, i, j, k);
 			else if (Block.blocksList[id] instanceof BlockCrops)
 				((BlockCrops)Block.blocksList[id]).fertilize(world, i, j, k);
 			return;
 		default:
 			return;
 		}
 	}
 	
 	/**
 	* Get the growth probability.
 	* Called by {@link #onUpdateTick(World, int, int, int, int)}.
 	* @param	world
 	* @param	i first coordinate
 	* @param	j second coordinate
 	* @param	k third coordinate
 	* @param 	type the NOType
 	* @return	Growth probability as a float
 	*/
 	private float getGrowthProb(World world, int i, int j, int k, int id, NOType type) {	
 		float freq = type!=NOType.APPLE?getGrowthRate(id):growSets[names.length]?growthRates[names.length]*1.5F:-1F;
 		if(biomeModifiedRate && freq>0 && type!=NOType.NETHERSTALK) {
 			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
 			if(type!=NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
 				return 0.01F;
 			} else if(type!=NOType.CUSTOM){
 			freq *= Utils.getOptValueMult(biome.rainfall, type!=NOType.APPLE?getOptRain(id):0.8F, type.getRainGrowth());
 			freq *= Utils.getOptValueMult(biome.temperature, type!=NOType.APPLE?getOptTemp(id):0.7F, type.getTempGrowth());
 			}
 		}
 		if(freq>0)
 			return 1F / freq;
 		else
 			return -1F;
 	}
 	/**
 	* Get the death probability.
 	* Called by {@link #onUpdateTick(World, int, int, int, int)}.
 	* @param	world
 	* @param	i
 	* @param	j
 	* @param	k
 	* @param type 
 	* @return	Death probability as a float
 	*/
 	private float getDeathProb(World world, int i, int j, int k, int id, NOType type) {			
 		float freq = getDeathRate(id);
 		if(biomeModifiedRate && freq>0 && type!=NOType.NETHERSTALK) {
 			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
 			if(type!=NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
 				return 1F;
 			} else if(type!=NOType.CUSTOM){
 			freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainDeath());
 			freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempDeath());		
 			}
 		}
 		if(freq>0)
 			return 1F / freq;
 		else
 			return -1F;
 	}
 	/**
 	* Checks if an apple can grow in this biome.
 	* Used in {@link #grow(World, int, int, int, int, NOType)}
 	* when type is leaf.
 	* 
 	* @return	True if it can grow in these coordinates
 	*/
 	private boolean appleCanGrow(World world, int i, int k) {
 		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);	
 		// Apples can grow in the named biomes
 		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.0F) 
 				&& (biome.rainfall > 0.4F));
 	}
 	/**
 	* Checks if an cocoa can grow in this biome.
 	* Used in {@link #grow(World, int, int, int, int, NOType)}
 	* when type is cocoa.
 	* @return	true if can grow in these coordinates
 	*/
 	private boolean cocoaCanGrow(World world, int i, int k) {
 		BiomeGenBase biome = world.getBiomeGenForCoords(i,k);	
 		// Cocoa can grow in the named biomes
 		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.5F) 
 				&& (biome.rainfall >= 0.8F));
 	}
 	////Helper methods to get parameters out of the mappings.////
 	private float getOptTemp(int id) {
 		return IDToOptTempMapping.get(Integer.valueOf(id));
 	}
 	private float getOptRain(int id) {
 		return IDToOptRainMapping.get(Integer.valueOf(id));
 	}
 	private int getGrowthRate(int id) {
 		return IDToGrowthRateMapping.get(Integer.valueOf(id));
 	}
 	private int getDeathRate(int id) {
 		return IDToDeathRateMapping.get(Integer.valueOf(id));
 	}
 	public boolean isGrowing(int id){
 		return IDToGrowingMapping.get(Integer.valueOf(id));	
 	}
 	public boolean isMortal(int id){
 		return IDToDyingMapping.get(Integer.valueOf(id));
 	}
 	public boolean isValid(int id){
 		return id>0 && id<4096 && Block.blocksList[id]!=null && IDToGrowingMapping.containsKey(Integer.valueOf(id));
 	}
 	public HashMap<Integer, NOType> getIDToTypeMapping(){
 		return IDToTypeMapping;
 	}
 	public HashMap<Integer, Integer> getLogToLeafMapping(){
 		return LogToLeafMapping;
 	}
 	////-----------------------------------------------------////
 	/**
 	 * Registers all mappings simultaneously.
 	 * @param id The id the block is registered with.
 	 * @param isGrowing Whether the block can call {@link #grow(World, int, int, int, int, NOType)} on tick.
 	 * @param growthRate How often the {@link #grow(World, int, int, int, int, NOType)} method will be called.
 	 * @param isMortal Whether the block can call {@link #death(World, int, int, int, int, NOType)} method on tick.
 	 * @param deathRate How often the {@link #death(World, int, int, int, int, NOType)} method will be called.
 	 * @param optTemp The optimal temperature parameter for the growth.
 	 * @param optRain The optimal humidity parameter for the growth.
 	 * @param type {@link NOType} Decides which growth and/or death to use, and tolerance to temperature and humidity.
 	 * @param fireCatch Related to 3rd parameter in {@link Block.setBurnProperties(int,int,int)}.
 	 * @param firePropagate Related to 2nd parameter in {@link Block.setBurnProperties(int,int,int)}.
 	 */
     private static void addMapping(int id, boolean isGrowing,int growthRate, boolean isMortal,int deathRate, float optTemp, float optRain, NOType type, int fireCatch,int firePropagate)
     {
     	IDToGrowingMapping.put(Integer.valueOf(id), isGrowing);
     	IDToGrowthRateMapping.put(Integer.valueOf(id),growthRate);
         IDToDyingMapping.put(Integer.valueOf(id), isMortal);
         IDToDeathRateMapping.put(Integer.valueOf(id),deathRate);
         IDToOptTempMapping.put(Integer.valueOf(id), optTemp);
         IDToOptRainMapping.put(Integer.valueOf(id), optRain);
         IDToTypeMapping.put(Integer.valueOf(id), type);
         IDToFireCatchMapping.put(Integer.valueOf(id),fireCatch);
         IDToFirePropagateMapping.put(Integer.valueOf(id),firePropagate);
     }
     private static void addMapping(int id,boolean isGrowing,int growthRate,boolean isMortal,int deathRate, float optTemp, float optRain, NOType type)
     {
     	addMapping(id, isGrowing, growthRate, isMortal, deathRate, optTemp, optRain, type, 0, 0);
     }
     @PostInit//Register blocks with config values and NOType, and log/leaf couples 
     public void modsLoaded(FMLPostInitializationEvent event){
     	if(Loader.isModLoaded("mod_MOAPI"))
     	{//We can use reflection to load options in MOAPI
 			try {
 				Class api = Class.forName("moapi.ModOptionsAPI");
 				Method addMod = api.getMethod("addMod",String.class);
 				//"addMod" is static, we don't need an instance
 				Object option =addMod.invoke(null,"Nature Overhaul");
 				Class optionClass= addMod.getReturnType();
 				//Set options as able to be used on a server,get the instance back
 				option=optionClass.getMethod("setServerMode").invoke(option);
 				//"addBooleanOption" and "addSliderOption" aren't static, we need options class and an instance
				Method addBoolean = optionClass.getMethod("addBooleanOption", new Class[]{String.class, boolean.class});
				Method addSlider = optionClass.getMethod("addSliderOption",new Class[]{String.class, int.class,int.class});
				Method addMap=optionClass.getMethod("addMappedOption", new Class[]{String.class,String[].class,int[].class});
 				//To create a submenu
 				Method addSubOption= optionClass.getMethod("addSubOption", String.class);
 				//Create "General" submenu and options
 				Object subOption= addSubOption.invoke(option, "General");
 				for(int i=0; i<names.length;i++)
 				{
 					addBoolean.invoke(subOption, names[i]+" grow", true);
 					addBoolean.invoke(subOption, names[i]+" die", true);
 					addSlider.invoke(subOption, names[i]+" growth rate",0,10000);
 					addSlider.invoke(subOption, names[i]+" death rate",0,10000);
 				}
 				addBoolean.invoke(subOption, "Apple grows", true);
 				addSlider.invoke(subOption, "Apple growth rate",0,10000);
 				//Create "LumberJack" submenu and options
 				Object lumberJackOption=addSubOption.invoke(option, "LumberJack");
 				addBoolean.invoke(lumberJackOption, "Enable", true);
 				addBoolean.invoke(lumberJackOption, "Kill leaves", true);
 				//Create "Misc" submenu and options
 				Object miscOption=addSubOption.invoke(option, "Misc");
 				addMap.invoke(miscOption, "Sapling grows on",new String[]{"Both","Decay","Growth","Neither"},new int[]{3,2,1,0});
 				addBoolean.invoke(miscOption, "AutoSapling", true);
 				addBoolean.invoke(miscOption, "Plant seeds on player drop", true);
 				addBoolean.invoke(miscOption, "Leaves decay on tree death", true);
 				addBoolean.invoke(miscOption, "Moss growing on stone", true);
 				addBoolean.invoke(miscOption, "Starving system", true);
 				addBoolean.invoke(miscOption, "Biome specific rates", true);
 				addBoolean.invoke(miscOption, "Modded Bonemeal", true);
 				addBoolean.invoke(miscOption, "Custom dimensions", true);
 				//Create "Animals" submenu and options
 				Object animalsOption=addSubOption.invoke(option, "Animals");
 				addBoolean.invoke(animalsOption, "Wild breed", true);
 				addSlider.invoke(animalsOption, "Breeding rate",0,10000);
 				//Create "Fire" submenu and options
 				Object fireOption=addSubOption.invoke(option, "Fire");
 				addSlider.invoke(fireOption, "WIP",0,100);
 				//Loads and saves values
 				option=optionClass.getMethod("loadValues").invoke(option);
 				option=optionClass.getMethod("saveValues").invoke(option);
 				//We have saved the values, we can start to get them back
 				Method getBoolean=optionClass.getMethod("getBooleanValue", String.class);
 				Method getSlider=optionClass.getMethod("getSliderValue", String.class);
 				Method getMap=optionClass.getMethod("getMappedValue", String.class);
 				for(int i=0; i<names.length;i++)
 				{
 					growSets[i]= Boolean.class.cast( getBoolean.invoke(subOption, names[i]+" grow")).booleanValue();
 					dieSets[i]=Boolean.class.cast( getBoolean.invoke(subOption, names[i]+" die")).booleanValue();
 					growthRates[i]=Integer.class.cast( getSlider.invoke(subOption, names[i]+" growth rate")).intValue();
 					deathRates[i]=Integer.class.cast( getSlider.invoke(subOption, names[i]+" death rate")).intValue();
 				}
 				growSets[names.length]=Boolean.class.cast( getBoolean.invoke(subOption, "Apple grows")).booleanValue();
 				growthRates[names.length]=Integer.class.cast( getSlider.invoke(subOption, "Apple growth rate")).intValue();
 		        lumberjack=Boolean.class.cast( getBoolean.invoke(lumberJackOption, "Enable")).booleanValue();
 		        killLeaves=Boolean.class.cast( getBoolean.invoke(lumberJackOption, "Kill leaves")).booleanValue();
 		        growthType=Integer.class.cast(getMap.invoke(miscOption, "Sapling grows on")).intValue();
 		        autoSapling=Boolean.class.cast( getBoolean.invoke(miscOption,"AutoSapling")).booleanValue();
 		        autoFarming=Boolean.class.cast( getBoolean.invoke(miscOption, "Plant seeds on player drop")).booleanValue();
 		        decayLeaves=Boolean.class.cast( getBoolean.invoke(miscOption, "Leaves decay on tree death")).booleanValue();
 		        mossCorruptStone=Boolean.class.cast( getBoolean.invoke(miscOption, "Moss growing on stone")).booleanValue();
 		        useStarvingSystem=Boolean.class.cast( getBoolean.invoke(miscOption, "Starving system")).booleanValue();
 		        biomeModifiedRate=Boolean.class.cast( getBoolean.invoke(miscOption, "Biome specific rates")).booleanValue();
 		        moddedBonemeal=Boolean.class.cast( getBoolean.invoke(miscOption, "Modded Bonemeal")).booleanValue();
 		        customDimension=Boolean.class.cast( getBoolean.invoke(miscOption, "Custom dimensions")).booleanValue();
 		        wildAnimalsBreed=Boolean.class.cast( getBoolean.invoke(animalsOption, "Wild breed")).booleanValue();
 		        wildAnimalBreedRate=Integer.class.cast( getSlider.invoke(animalsOption, "Breeding rate")).intValue();
 				API=true;
 			}catch (ClassNotFoundException c){
 				API=false;
 				System.out.println("Nature Overhaul couldn't find MOAPI, continuing with values in config file.");
 			}
 			catch(NoSuchMethodException n) {
     			API=false;
				System.err.println("Nature Overhaul couldn't find a MOAPI part, please report to NO author:");
 				n.printStackTrace();
     		}
     		catch( SecurityException s){
     			API=false;
     			System.err.println("Nature Overhaul encountered a security issue, please report to MOAPI author:");
 				s.printStackTrace();
     		}
     		catch( IllegalAccessException i){
     			API=false;
     			System.err.println("Nature Overhaul couldn't call a MOAPI function, please report to MOAPI author:");
 				i.printStackTrace();
     		}
     		catch( IllegalArgumentException i){
     			API=false;
     			System.err.println("Nature Overhaul didn't use a MOAPI function properly, please report to NO author:");
 				i.printStackTrace();
     		}
     		catch( InvocationTargetException i){
     			API=false;
     			
     		}//Even if it fails, we can still rely on settings stored in Forge recommended config file.
     	}
     	//Now we can register every available blocks at this point.
     	//If a block is registered after, it won't be accounted for.
     	ArrayList<Integer> logID=new ArrayList(),leafID=new ArrayList();
     	for (int i=1;i<Block.blocksList.length;i++)
     	{
     		if (Block.blocksList[i]!=null)
     		{
     			if (Block.blocksList[i] instanceof IGrowable && Block.blocksList[i] instanceof IBlockDeath)//Priority to Blocks using the API
     			{
     				addMapping(i,true,((IGrowable)Block.blocksList[i]).getGrowthRate(),true, ((IBlockDeath)Block.blocksList[i]).getDeathRate(),-1.0F,-1.0F,NOType.CUSTOM);
     			}
     			else if (Block.blocksList[i] instanceof IGrowable)
     			{
     				addMapping(i,true,((IGrowable)Block.blocksList[i]).getGrowthRate(),false,-1,-1.0F,-1.0F,NOType.CUSTOM);
     			}
     			else if (Block.blocksList[i] instanceof IBlockDeath)
     			{
     				addMapping(i, false, -1, true, ((IBlockDeath)Block.blocksList[i]).getDeathRate(), -1.0F, -1.0F,NOType.CUSTOM);
     			} 
     			else if(Block.blocksList[i] instanceof BlockSapling)
     			{
     				addMapping(i, growSets[0], 0, dieSets[0],deathRates[0], 0.8F, 0.8F,NOType.SAPLING);	
     			}
     			else if(Block.blocksList[i] instanceof BlockLog)
     			{
     				addMapping(i,growSets[1],growthRates[1],dieSets[1],deathRates[1], 1.0F, 1.0F,NOType.LOG,5,5);
     				if(!logID.contains(i))
     					logID.add(i);		
     			}	
     			else if(Block.blocksList[i] instanceof BlockNetherStalk)//In the Nether, we don't use biome dependent parameter
     			{
     				addMapping(i,growSets[3],growthRates[3],dieSets[3],deathRates[3],1.0F,1.0F,NOType.NETHERSTALK);
     			}			
     			else if (Block.blocksList[i] instanceof BlockGrass||Block.blocksList[i] instanceof BlockMycelium)
     			{
     				addMapping(i, growSets[4], growthRates[4], dieSets[4], deathRates[4], 0.7F, 0.5F,NOType.GRASS);		
     			}
     			else if(Block.blocksList[i] instanceof BlockReed)
     			{
     				addMapping(i,growSets[5],growthRates[5],dieSets[5],deathRates[5],0.8F,0.8F,NOType.REED);
     			}
     			else if(Block.blocksList[i] instanceof BlockCactus)
     			{
     				addMapping(i, growSets[6],growthRates[6], dieSets[6],deathRates[6], 1.5F, 0.2F,NOType.CACTUS);
     			}	
     			else if (Block.blocksList[i] instanceof BlockMushroom)
     			{
     				addMapping(i,growSets[7],growthRates[7],dieSets[7],deathRates[7],0.9F,1.0F,NOType.MUSHROOM);
     			}
     			else if(Block.blocksList[i] instanceof BlockMushroomCap)
     			{
     				addMapping(i,growSets[8],growthRates[8],dieSets[8],deathRates[8],0.9F,1.0F,NOType.MUSHROOMCAP);
     			}
     			else if(Block.blocksList[i] instanceof BlockLeaves)
     			{
     				addMapping(i, growSets[9], growthRates[9], dieSets[9],deathRates[9], 1.0F, 1.0F,NOType.LEAVES,60,30 );
     				if(!leafID.contains(i))
     					leafID.add(i);
     			}
     			else if(Block.blocksList[i] instanceof BlockCrops || Block.blocksList[i] instanceof BlockStem)
     			{
     				addMapping(i,growSets[10],growthRates[10],dieSets[10],deathRates[10],1.0F,1.0F,NOType.FERTILIZED);
     			}
     			else if(Block.blocksList[i] instanceof BlockFlower)//Flower ,deadbush, lilypad, tallgrass
     			{
     				addMapping(i,growSets[2],growthRates[2],dieSets[2],deathRates[2],0.6F,0.7F,NOType.PLANT,100,60);
     			}			
     			else if(i==Block.cobblestoneMossy.blockID)
     			{
     				addMapping(i,growSets[11],growthRates[11],dieSets[11],deathRates[11],0.7F,1.0F,NOType.MOSS);
     			}
     			else if(Block.blocksList[i] instanceof BlockCocoa)
     			{
     				addMapping(i,growSets[12],growthRates[12],dieSets[12],deathRates[12],1.0F,1.0F,NOType.COCOA);
     			}
     			IDToFirePropagateMapping.put(i,config.get("Fire",Block.blocksList[i].getUnlocalizedName().substring(5)+ " chance to encourage fire",IDToFirePropagateMapping.containsKey(i)?IDToFirePropagateMapping.get(i):0).getInt());
     			IDToFireCatchMapping.put(i,config.get("Fire",Block.blocksList[i].getUnlocalizedName().substring(5)+ " chance to catch fire",IDToFireCatchMapping.containsKey(i)?IDToFireCatchMapping.get(i):0).getInt());	
         		Block.setBurnProperties(i, IDToFirePropagateMapping.get(i), IDToFireCatchMapping.get(i));
     		}	
     	}
     	idLog=config.get(optionsCategory[names.length],"Log ids linked to Leaves",Ints.toArray(logID)).getIntList();
         idLeaf=config.get(optionsCategory[names.length],"Leaves ids linked to Logs",Ints.toArray(leafID)).getIntList();
     	for(int id=0;id<Math.min(idLog.length, idLeaf.length);id++)
 		{
 			LogToLeafMapping.put(Integer.valueOf(idLog[id]), Integer.valueOf(idLeaf[id]));
 		}
     	//Saving Forge recommended config file.
     	if (config.hasChanged())
         {        
       	  config.save();     	
         }
     	//Registering event listeners.
     	bonemealEvent=new BonemealEventHandler(moddedBonemeal);
 		MinecraftForge.EVENT_BUS.register(bonemealEvent);
 		animalEvent=new AnimalEventHandler(wildAnimalsBreed,wildAnimalBreedRate);
 		MinecraftForge.EVENT_BUS.register(animalEvent);
 		lumberEvent=new PlayerEventHandler(lumberjack,killLeaves);
 		MinecraftForge.EVENT_BUS.register(lumberEvent);
 		farmingEvent=new AutoFarmingEventHandler(autoFarming);
 		MinecraftForge.EVENT_BUS.register(farmingEvent);
 		autoEvent=new AutoSaplingEventHandler(autoSapling);
 		
     	if(growthType%2!=0)
     		MinecraftForge.EVENT_BUS.register(autoEvent);
     	if(growthType%2==0)
     		MinecraftForge.EVENT_BUS.register(new SaplingGrowEventHandler());
     }
     @Override
     /**
      * Core method. We make vanilla-like random ticks in loaded chunks.
      */
 	public void tickStart(EnumSet<TickType> type, Object... tickData) 
 	{	
     	if(API)
     	{
     		try{
 	    		Class api = Class.forName("moapi.ModOptionsAPI");
 				Method getMod = api.getMethod("getModOptions",String.class);
 				//"getMod" is static, we don't need an instance
 				Object option =getMod.invoke(null,"Nature Overhaul");
 				Class optionClass= getMod.getReturnType();
 				//To get a submenu
 				Method getSubOption= optionClass.getMethod("getOption", String.class);
 				Object subOption= getSubOption.invoke(option, "General");
 				//Get "LumberJack" submenu
 				Object lumberJackOption=getSubOption.invoke(option, "LumberJack");
 				//Get "Misc" submenu
 				Object miscOption=getSubOption.invoke(option, "Misc");
 				//Get "Animals" submenu
 				Object animalsOption=getSubOption.invoke(option, "Animals");
 				//We can start to get the values back
 				Method getBoolean=optionClass.getMethod("getBooleanValue", String.class);
 				Method getSlider=optionClass.getMethod("getSliderValue", String.class);
 				Method getMap=optionClass.getMethod("getMappedValue", String.class);
 				for(int i=0; i<names.length;i++)
 				{
 					growSets[i]= Boolean.class.cast( getBoolean.invoke(subOption, names[i]+" grow")).booleanValue();
 					dieSets[i]=Boolean.class.cast( getBoolean.invoke(subOption, names[i]+" die")).booleanValue();
 					growthRates[i]=Integer.class.cast( getSlider.invoke(subOption, names[i]+" growth rate")).intValue();
 					deathRates[i]=Integer.class.cast( getSlider.invoke(subOption, names[i]+" death rate")).intValue();
 				}
 				growSets[names.length]=Boolean.class.cast( getBoolean.invoke(subOption, "Apple grows")).booleanValue();
 				growthRates[names.length]=Integer.class.cast( getSlider.invoke(subOption, "Apple growth rate")).intValue();
 		        lumberjack=Boolean.class.cast( getBoolean.invoke(lumberJackOption, "Enable")).booleanValue();
 		        killLeaves=Boolean.class.cast( getBoolean.invoke(lumberJackOption, "Kill leaves")).booleanValue();
 		        growthType=Integer.class.cast(getMap.invoke(miscOption, "Sapling grows on")).intValue();
 		        autoSapling=Boolean.class.cast( getBoolean.invoke(miscOption,"AutoSapling")).booleanValue();
 		        autoFarming=Boolean.class.cast( getBoolean.invoke(miscOption, "Plant seeds on player drop")).booleanValue();
 		        decayLeaves=Boolean.class.cast( getBoolean.invoke(miscOption, "Leaves decay on tree death")).booleanValue();
 		        mossCorruptStone=Boolean.class.cast( getBoolean.invoke(miscOption, "Moss growing on stone")).booleanValue();
 		        useStarvingSystem=Boolean.class.cast( getBoolean.invoke(miscOption, "Starving system")).booleanValue();
 		        biomeModifiedRate=Boolean.class.cast( getBoolean.invoke(miscOption, "Biome specific rates")).booleanValue();
 		        moddedBonemeal=Boolean.class.cast( getBoolean.invoke(miscOption, "Modded Bonemeal")).booleanValue();
 		        customDimension=Boolean.class.cast( getBoolean.invoke(miscOption, "Custom dimensions")).booleanValue();
 		        wildAnimalsBreed=Boolean.class.cast( getBoolean.invoke(animalsOption, "Wild breed")).booleanValue();
 		        wildAnimalBreedRate=Integer.class.cast( getSlider.invoke(animalsOption, "Breeding rate")).intValue();
 				API=true;
     		}catch(NoSuchMethodException n) {
     			API=false;
     		}
     		catch( SecurityException s){
     			API=false;
     		}
     		catch( IllegalAccessException 	i){
     			API=false;
     		}
     		catch( IllegalArgumentException i){
     			API=false;
     		}
     		catch( InvocationTargetException i){
     			API=false;
     		}
     		catch( ClassNotFoundException n) {
     			API=false;
     		}
     		bonemealEvent.set(moddedBonemeal);
     		animalEvent.set(wildAnimalsBreed,wildAnimalBreedRate);
     		lumberEvent.set(lumberjack,killLeaves);
     		autoEvent.set(autoSapling);
     	}
     	if (tickData.length>0 && tickData[0] instanceof WorldServer)
     	{
     		WorldServer world = (WorldServer) tickData[0];
     		if((world.provider.dimensionId==0|| (customDimension && world.provider.dimensionId!=1)) && !world.activeChunkSet.isEmpty())
     		{//start
 				Iterator it=world.activeChunkSet.iterator();			
 				while (it.hasNext())
 				{
 					ChunkCoordIntPair chunkIntPair = (ChunkCoordIntPair) it.next();
 					int k = chunkIntPair.chunkXPos * 16;
 		            int l = chunkIntPair.chunkZPos * 16;
 					Chunk chunk=null;
 					if(world.getChunkProvider().chunkExists(chunkIntPair.chunkXPos,chunkIntPair.chunkZPos))
 					{
 						chunk=world.getChunkFromChunkCoords(chunkIntPair.chunkXPos,chunkIntPair.chunkZPos);
 					}
 					if (chunk!=null && chunk.isChunkLoaded && chunk.isTerrainPopulated)
 					{	//We could use reflection to get field_94579_S,
 						//but it is empty at tickstart (too early) and tickend (too late)
 						/*try
 						{
 							Field f = tickData[0].getClass().getDeclaredField("field_94579_S");
 							f.setAccessible(true);
 							ArrayList<NextTickListEntry> list =  new ArrayList();
 							while(list.isEmpty())
 							{
 								list =  (ArrayList<NextTickListEntry>) f.get(tickData[0]);
 							}
 							if (list!=null && !list.isEmpty())
 							{		
 								System.out.println("check 1");
 								Iterator itr=list.iterator();						
 								while(itr.hasNext())				
 								{			
 									NextTickListEntry nextTickEntry= (NextTickListEntry) itr.next();	
 									//itr.remove();
 
 					                if (world.getChunkProvider().chunkExists(nextTickEntry.xCoord, nextTickEntry.zCoord))
 					                {
 					                    int k = world.getBlockId(nextTickEntry.xCoord, nextTickEntry.yCoord, nextTickEntry.zCoord);
 
 					                    if (k > 0 && Block.isAssociatedBlockID(k, nextTickEntry.blockID) && isValid(nextTickEntry.blockID))						
 					                    {	
 										System.out.println("check 2");
 										onUpdateTick(world,nextTickEntry.xCoord,nextTickEntry.yCoord,nextTickEntry.zCoord,nextTickEntry.blockID);						
 					                    }						
 					                }	
 								}
 							}
 						}
 						catch(NoSuchFieldException | SecurityException| IllegalAccessException e)
 						{
 							System.err.println("NatureOverhaul encountered a problem while getting ticks");
 							e.printStackTrace();
 						}*/
 						int i2;//Vanilla like random ticks for blocks
 						for (ExtendedBlockStorage blockStorage:chunk.getBlockStorageArray())
 							if (blockStorage!=null && !blockStorage.isEmpty() && blockStorage.getNeedsRandomTick())
 								{
 								for (int j2 = 0; j2 < 3; ++j2)
 			                    	{
 			                        	this.updateLCG = this.updateLCG * 3 + 1013904223;
 			                        	i2 = this.updateLCG >> 2;
 			                        	int k2 = i2 & 15;
 			                        	int l2 = i2 >> 8 & 15;
 			                        	int i3 = i2 >> 16 & 15;
 			                        	int j3 = blockStorage.getExtBlockID(k2, i3, l2);
 			                        	Block block = Block.blocksList[j3];
 		
 			                        	if (isValid(j3))
 			                        	{
 			                        		onUpdateTick(world, k2 + k, i3 + blockStorage.getYLocation(), l2 + l, j3);
 			                        	}
 			                    	}				
 						}
 					}
 				}//end
     		}			
     	}
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {}
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.WORLD);//The only TickType we want to get the world ticks.
 	}
 	@Override
 	public String getLabel() {
 		return "Nature Overhaul Tick";
 	}
 }
