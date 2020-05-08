 package entanglecraft.items;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import entanglecraft.blocks.EntangleCraftBlocks;
 
 public class EntangleCraftItems {
 	  public static final Item ItemNetherEssence = new ItemLambda(8600).setIconIndex(87).setItemName("ItemNetherEssence");
 	  public static final Item ItemDeviceG = new ItemDevice(8601).setIconIndex(90).setItemName("ItemDeviceG");
 	  public static final Item ItemDeviceR = new ItemDevice(8602).setIconIndex(91).setItemName("ItemDeviceR");
 	  public static final Item ItemDeviceY = new ItemDevice(8603).setIconIndex(92).setItemName("ItemDeviceY");
 	  public static final Item ItemDeviceB = new ItemDevice(8604).setIconIndex(93).setItemName("ItemDeviceB");
 	  public static final Item ItemNethermonicDiamond = new ItemLambda(8605).setIconIndex(88).setItemName("ItemNethermonicDiamond");
 	  public static final Item ItemLambdaCore = new ItemLambda(8606).setIconIndex(89).setItemName("ItemLambdaCore");
 	  public static final Item ItemTransformer = new ItemLambda(8607).setIconIndex(80).setItemName("ItemTransformer");
 	  public static final Item ItemReverseTransformer = new ItemLambda(8608).setIconIndex(81).setItemName("ItemReverseTransformer");
 	  public static final Item ItemTransmitter = new ItemLambda(8609).setIconIndex(82).setItemName("ItemTransmitter");
 	  public static final Item ItemFrShard = new ItemLambda(8610).setIconIndex(86).setItemName("ItemFrShard");
 	  public static final Item ItemBlueShard = new ItemShard(8611,0).setIconIndex(83).setItemName("ItemBlueShard").setMaxDamage(256);
 	  public static final Item ItemRedShard = new ItemShard(8612,1).setIconIndex(84).setItemName("ItemRedShard").setMaxDamage(256);
 	  public static final Item ItemYelShard = new ItemShard(8613,2).setIconIndex(85).setItemName("ItemYelShard").setMaxDamage(256);
 	  public static final Item ItemImbuedShard = new ItemShard(8614,4).setIconIndex(94).setItemName("ItemImbuedShard");
 	  public static final Item ItemInductionCircuit = new ItemLambda(8615).setIconIndex(95).setItemName("ItemInductionCircuit").setMaxStackSize(1);
 	  public static final Item ItemCircuit = new ItemLambda(8616).setIconIndex(96).setItemName("ItemCircuit");
 	  public static final Item ItemInclusiveFilter = new ItemLambda(8617).setIconIndex(97).setItemName("ItemInclusiveFilter").setMaxStackSize(1);
 	  public static final Item ItemExclusiveFilter = new ItemLambda(8618).setIconIndex(98).setItemName("ItemExclusiveFilter").setMaxStackSize(1);
 	  public static final Item ItemDestroyFilter = new ItemLambda(8625).setIconIndex(97+16).setItemName("ItemDestroyFilter").setMaxStackSize(1);
 	  public static final Item ItemDontDestroyFilter = new ItemLambda(8626).setIconIndex(97+17).setItemName("ItemDontDestroyFilter").setMaxStackSize(1);
 	  public static final Item ItemSuperInductionCircuit = new ItemLambda(8619).setIconIndex(99).setItemName("ItemSuperInductionCircuit").setMaxStackSize(1);
 	  public static final Item ItemTPScroll = new ItemShard(8620,3).setIconIndex(101).setItemName("ItemTPScroll").setMaxDamage(1);
 	  public static final Item ItemShardPickG = new ItemShardPick(8621).setIconIndex(102).setItemName("ItemShardPickG").setMaxDamage(0);
 	  public static final Item ItemShardPickR = new ItemShardPick(8622).setIconIndex(103).setItemName("ItemShardPickR").setMaxDamage(0);
 	  public static final Item ItemShardPickY = new ItemShardPick(8623).setIconIndex(104).setItemName("ItemShardPickY").setMaxDamage(0);
 	  public static final Item ItemShardPickB = new ItemShardPick(8624).setIconIndex(105).setItemName("ItemShardPickB").setMaxDamage(0);
 	  
 public static void addItems(){
 	itemInitializing();
 	
     LanguageRegistry.addName(ItemDeviceG, "Lambda Device : G");
     LanguageRegistry.addName(ItemDeviceR, "Lambda Device : R");
     LanguageRegistry.addName(ItemDeviceY, "Lambda Device : Y");
     LanguageRegistry.addName(ItemDeviceB, "Lambda Device : B");
     LanguageRegistry.addName(ItemLambdaCore, "Lambda Core");
     LanguageRegistry.addName(ItemNetherEssence, "Nethermonic Essence");
     LanguageRegistry.addName(ItemNethermonicDiamond, "Nethermonic Diamond");
     LanguageRegistry.addName(ItemTransformer, "Displacement Flat-rate Transformer");
     LanguageRegistry.addName(ItemReverseTransformer, "Entanglement Flat-rate Transformer");
     LanguageRegistry.addName(ItemTransmitter,"Transmitter");
     LanguageRegistry.addName(ItemInductionCircuit, "Induction Circuit");
     LanguageRegistry.addName(ItemSuperInductionCircuit, "Forerunner Induction Circuit");
     LanguageRegistry.addName(ItemFrShard, "Forerunner Obsidian Shard");
     LanguageRegistry.addName(ItemBlueShard, "Mysterious Blue Shard");
     LanguageRegistry.addName(ItemRedShard, "Mysterious Red Shard");
     LanguageRegistry.addName(ItemYelShard, "Mysterious Yellow Shard");
     LanguageRegistry.addName(ItemImbuedShard, "Legendary Tri-Shard");
     LanguageRegistry.addName(ItemCircuit,"Circuit");
     LanguageRegistry.addName(ItemInclusiveFilter,"'Mine only x' Filter Device");
     LanguageRegistry.addName(ItemExclusiveFilter, "Exclusive Filter Device");
     LanguageRegistry.addName(ItemDestroyFilter, "'Destroy x' Filter Device");
     LanguageRegistry.addName(ItemDontDestroyFilter, "'Do not destroy x' Filter Device");
     LanguageRegistry.addName(ItemTPScroll, "TP Scroll");
     LanguageRegistry.addName(ItemShardPickG, "Displacement Pick : G");
     LanguageRegistry.addName(ItemShardPickR, "Displacement Pick : R");
     LanguageRegistry.addName(ItemShardPickY, "Displacement Pick : Y");
     LanguageRegistry.addName(ItemShardPickB, "Displacement Pick : B");
     GameRegistry.addSmelting(Block.netherrack.blockID, new ItemStack(ItemNetherEssence, 1),1F);
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockRLD, 1), new Object[] { "NRN", "NGN", "NDN", Character.valueOf('D'), ItemNethermonicDiamond, Character.valueOf('N'), ItemNetherEssence, Character.valueOf('G'), EntangleCraftBlocks.BlockGLD, Character.valueOf('R'), Item.redstone });
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockYLD, 1), new Object[] { "NSN", "NGN", "NDN", Character.valueOf('D'), ItemNethermonicDiamond, Character.valueOf('N'), ItemNetherEssence, Character.valueOf('G'), EntangleCraftBlocks.BlockGLD, Character.valueOf('S'), Item.lightStoneDust });
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockBLD, 1), new Object[] { "NSN", "NGN", "NDN", Character.valueOf('D'), ItemNethermonicDiamond, Character.valueOf('N'), ItemNetherEssence, Character.valueOf('G'), EntangleCraftBlocks.BlockGLD, Character.valueOf('S'), new ItemStack(Item.dyePowder, 1, 4) });
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockFObsidian,8), new Object[] {"OOO","OSO","OOO", Character.valueOf('O'),Block.obsidian, Character.valueOf('S'),ItemFrShard});
     GameRegistry.addRecipe(new ItemStack(ItemDeviceG, 1), new Object[] { "SOO", "OLO", "OXO", Character.valueOf('O'), Block.obsidian, Character.valueOf('L'), ItemLambdaCore, Character.valueOf('S'), Item.lightStoneDust, Character.valueOf('X'), Item.map});
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockGLD, 1), new Object[] { "FFF", "FLF", "FFF", Character.valueOf('F'), Block.obsidian, Character.valueOf('L'), ItemLambdaCore });
     //GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockGenericDestination, 1), new Object[] { "  D","   ", "   ", Character.valueOf('D'), Block.dirt});
     GameRegistry.addRecipe(new ItemStack(ItemNethermonicDiamond, 1), new Object[] { "NNN", "NDN", "NNN", Character.valueOf('N'), ItemNetherEssence, Character.valueOf('D'), Item.diamond });
     GameRegistry.addRecipe(new ItemStack(ItemNethermonicDiamond,12), new Object[] {"DDD","DID","DDD", Character.valueOf('D'), ItemNethermonicDiamond, Character.valueOf('I'), ItemImbuedShard});
     GameRegistry.addRecipe(new ItemStack(ItemLambdaCore, 1), new Object[] { "FBF", "SNS", "FBF", Character.valueOf('S'), Item.lightStoneDust, Character.valueOf('F'), Block.obsidian, Character.valueOf('N'), ItemNethermonicDiamond, Character.valueOf('B'), new ItemStack(Item.dyePowder, 1, 4) });
     GameRegistry.addRecipe(new ItemStack(ItemTransformer, 1), new Object[] { "RNL", "NIN", "GNR", Character.valueOf('N'), ItemNetherEssence, Character.valueOf('R'), Item.redstone, Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('G'), Item.lightStoneDust, Character.valueOf('I'), Item.ingotIron });
     GameRegistry.addRecipe(new ItemStack(ItemReverseTransformer, 1), new Object[] { "NLN", "RIR", "NGN", Character.valueOf('N'), ItemNetherEssence, Character.valueOf('R'), Item.redstone, Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('G'), Item.lightStoneDust, Character.valueOf('I'), Item.ingotIron });
     GameRegistry.addRecipe(new ItemStack(ItemTransmitter,1), new Object[] {"IBI","ICI","INI", Character.valueOf('I'), Item.ingotIron,Character.valueOf('B'), new ItemStack(Item.dyePowder,1,4), Character.valueOf('N'),Item.compass, Character.valueOf('C'),ItemCircuit});
     GameRegistry.addRecipe(new ItemStack(ItemCircuit,1), new Object[] {"FIF","ISI","FIF", Character.valueOf('F'), ItemFrShard,Character.valueOf('I'), Item.ingotIron, Character.valueOf('S'), ItemImbuedShard});
     GameRegistry.addRecipe(new ItemStack(ItemInductionCircuit,1), new Object[] {"IRI","ISI"," C ", Character.valueOf('C'), ItemCircuit, Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone, Character.valueOf('S'),ItemFrShard});
     GameRegistry.addRecipe(new ItemStack(ItemSuperInductionCircuit,1), new Object[] {"FDF","FDF"," I ", Character.valueOf('F'), EntangleCraftBlocks.BlockFObsidian, Character.valueOf('D'), Item.diamond, Character.valueOf('I'), ItemInductionCircuit});
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockGLM,1), new Object[] {"FCF","TLT","FDF",Character.valueOf('D'), ItemShardPickG, Character.valueOf('F'), EntangleCraftBlocks.BlockFObsidian,Character.valueOf('L'), ItemLambdaCore, Character.valueOf('C'), ItemCircuit, Character.valueOf('T'), ItemTransmitter});
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockRLM,1), new Object[] {"FCF","TLT","FDF",Character.valueOf('D'), ItemShardPickR, Character.valueOf('F'), EntangleCraftBlocks.BlockFObsidian,Character.valueOf('L'), ItemLambdaCore, Character.valueOf('C'), ItemCircuit, Character.valueOf('T'), ItemTransmitter});
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockYLM,1), new Object[] {"FCF","TLT","FDF",Character.valueOf('D'), ItemShardPickY, Character.valueOf('F'), EntangleCraftBlocks.BlockFObsidian,Character.valueOf('L'), ItemLambdaCore, Character.valueOf('C'), ItemCircuit, Character.valueOf('T'), ItemTransmitter});
     GameRegistry.addRecipe(new ItemStack(EntangleCraftBlocks.BlockBLM,1), new Object[] {"FCF","TLT","FDF",Character.valueOf('D'), ItemShardPickB, Character.valueOf('F'), EntangleCraftBlocks.BlockFObsidian,Character.valueOf('L'), ItemLambdaCore, Character.valueOf('C'), ItemCircuit, Character.valueOf('T'), ItemTransmitter});
     GameRegistry.addRecipe(new ItemStack(ItemInclusiveFilter,1), new Object[] {"GTG","GCG","GTG", Character.valueOf('G'), Block.glass, Character.valueOf('C'),ItemCircuit,Character.valueOf('T'), ItemTransmitter});
     GameRegistry.addRecipe(new ItemStack(ItemExclusiveFilter,1), new Object[] {"GGG","TCT","GGG", Character.valueOf('G'), Block.glass, Character.valueOf('C'),ItemCircuit,Character.valueOf('T'), ItemTransmitter});
    GameRegistry.addRecipe(new ItemStack(ItemDestroyFilter,1), new Object[] {"GTG","GCG","GRG", Character.valueOf('G'), Block.glass, Character.valueOf('C'), ItemCircuit,Character.valueOf('T'), ItemTransmitter, Character.valueOf('R'), new ItemStack(Block.torchRedstoneActive,1)});
    GameRegistry.addRecipe(new ItemStack(ItemDontDestroyFilter,1), new Object[] {"GRG","GCG","GTG", Character.valueOf('G'), Block.glass, Character.valueOf('C'), ItemCircuit,Character.valueOf('T'), ItemTransmitter, Character.valueOf('R'), new ItemStack(Block.torchRedstoneActive,1)});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemFrShard,1), new Object[]{Block.obsidian}); // THIS RECIPE IS TEMPORARY, SHOULD BE BlockFObsidian AFTER WORLD GENEREATION RE-IMPLEMENTED AND SHOULD GRANT 8 INSTEAD OF 1
     GameRegistry.addShapelessRecipe(new ItemStack(ItemRedShard,1), new Object[]{ItemFrShard,Item.redstone});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemYelShard,1), new Object[]{ItemFrShard,Item.lightStoneDust});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemBlueShard,1), new Object[]{ItemFrShard,new ItemStack(Item.dyePowder,1,4)});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemImbuedShard), new Object[]{ItemYelShard,ItemBlueShard,ItemRedShard});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemTPScroll,8), new Object[]{Item.enderPearl,Item.spiderEye,Item.gunpowder,Item.bone,Item.paper});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemShardPickG), new Object[] {Item.pickaxeDiamond, ItemDeviceG});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemShardPickR), new Object[] {Item.pickaxeDiamond, ItemDeviceR});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemShardPickY), new Object[] {Item.pickaxeDiamond, ItemDeviceY});
     GameRegistry.addShapelessRecipe(new ItemStack(ItemShardPickB), new Object[] {Item.pickaxeDiamond, ItemDeviceB});
 }
 
 private static void itemInitializing()
 {
     
     Item[] availableChannelsDevices = new Item[] {
 			EntangleCraftItems.ItemDeviceG, 
 			EntangleCraftItems.ItemDeviceR, 
 			EntangleCraftItems.ItemDeviceY, 
 			EntangleCraftItems.ItemDeviceB};
     
     Item[] availableChannelsPicks = new Item[] {EntangleCraftItems.ItemShardPickG, 
     		EntangleCraftItems.ItemShardPickR, 
     		EntangleCraftItems.ItemShardPickY, 
     		EntangleCraftItems.ItemShardPickB};
     
     Item[][] classOfChannel = new Item[][] {availableChannelsDevices, availableChannelsPicks};
     
     for(Object obj : classOfChannel)
     {
     	Item[] thisClassOfChannels = (Item[])obj;
     	
 	    int i = 0;
 	    for(Object o : thisClassOfChannels)
 	    {
 	    	IChanneled thisItemChanneled = (IChanneled)o;
 	    	thisItemChanneled.setAvailableChannels(thisClassOfChannels);
 	    	thisItemChanneled.setChannel(i);
 	    	i += 1;
 	    }
     }
   
 
 }
 
 }
 
