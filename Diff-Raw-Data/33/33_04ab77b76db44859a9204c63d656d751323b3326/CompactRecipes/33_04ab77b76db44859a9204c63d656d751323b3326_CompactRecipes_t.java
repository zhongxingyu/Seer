 package mods.CompactStuff;
 
 import static mods.CompactStuff.CompactStuff.bagOfHolding;
 import static mods.CompactStuff.CompactStuff.carbon;
 import static mods.CompactStuff.CompactStuff.comBlock;
 import static mods.CompactStuff.CompactStuff.comGlass;
 import static mods.CompactStuff.CompactStuff.compactor;
 import static mods.CompactStuff.CompactStuff.furnace;
 import static mods.CompactStuff.CompactStuff.itemStuff;
 import static mods.CompactStuff.CompactStuff.plantBall;
 import static mods.CompactStuff.CompactStuff.smeltOnAStick;
 import static mods.CompactStuff.CompactStuff.transmog;
 import mods.CompactStuff.boh.ItemBagOfHolding;
 import mods.CompactStuff.compactor.TileEntityCompactor;
 import mods.CompactStuff.furnace.ItemCompactFurnace;
 import mods.CompactStuff.furnace.TileEntityCarbonFurnace;
 import mods.CompactStuff.furnace.TileEntityCobbleFurnace;
 import mods.CompactStuff.tmog.ItemBlockTmog;
 import mods.CompactStuff.tmog.TileEntityTransmog;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class CompactRecipes {
 
 	public static void setUpRecipes() {
 		setUpPlantBalls();
 		setUpComBlocks();
 		setUpSmeltStick();
 		setUpFurnaces();
 		setUpGlassAndStuff();
 		setUpCompactors();
 		setUpCarbon();
 		setUpBagOfHolding();
 		
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.coal),
 				new ItemStack(Item.coal,1,1), //charcoal
 				new ItemStack(Block.cobblestone));		
 		GameRegistry.addRecipe(new ItemStack(Block.beacon), "ccc","odo","obo",
 				'c', new ItemStack(comGlass),
 				'o', new ItemStack(Block.obsidian),
 				'd', new ItemStack(comBlock, 1, Metas.COMDIAMOND),
 				'b', new ItemStack(itemStuff, 1, ItemStuff.TMOG_CRYSTAL));
 		GameRegistry.addRecipe(new ItemStack(Block.stoneBrick,4,3),"ss","ss",'s',new ItemStack(Block.stoneBrick));
 		GameRegistry.addRecipe(new ItemStack(43,1,8), "s","s",'s',new ItemStack(Block.stoneSingleSlab));
 	}
 
 	private static void addHomogeneousRecipe(ItemStack output, ItemStack input, int n) {
 		Object[] inputs = new Object[n];
 		java.util.Arrays.fill(inputs, input);
 		GameRegistry.addShapelessRecipe(output, inputs);
 	}
 	private static void addReversableRecipe(ItemStack output, ItemStack input, int n) {
 		addHomogeneousRecipe(output, input, n);
 		ItemStack nout = input.copy();
 		nout.stackSize = n;
 		addHomogeneousRecipe(nout, output, 1);
 	}
 	private static void addFilledRecipe(ItemStack output, ItemStack ring, ItemStack center) {
 		if(center==null) GameRegistry.addRecipe(output, "rrr", "r r", "rrr", 'r', ring);
 		else GameRegistry.addRecipe(output, "rrr", "rir", "rrr", 'r', ring, 'i', center);
 	}
 	private static void addPlateRecipe(int plateMeta, int blockMeta, ItemStack ingot) {
 		ItemStack plate = new ItemStack(itemStuff, 6, plateMeta);
 		GameRegistry.addShapelessRecipe(plate, new ItemStack(comBlock, 1, blockMeta));
 		ItemStack ingots = ingot.copy(); ingots.stackSize=13; GameRegistry.addShapelessRecipe(ingots, plate);
 	}
 	private static void addBothAlternatingRecipes(ItemStack output, ItemStack a, ItemStack b, ItemStack c) {
 		GameRegistry.addRecipe(output, "aba","bcb","aba",'a',a,'b',b,'c',c);
 		GameRegistry.addRecipe(output, "bab","aca","bab",'a',a,'b',b,'c',c);
 	}
 	private static void addCrossRecipe(ItemStack output, ItemStack c, ItemStack v, ItemStack h, ItemStack m) {
 		GameRegistry.addRecipe(output, "cvc","hmh","cvc",'c',c,'v',v,'h',h,'m',m);
 	}
 	
 	public static void setUpBagOfHolding() {
 		ItemStack bag = new ItemStack(bagOfHolding,1,0);
 		LanguageRegistry.addName(bag, "Bag of Holding");
 		
 		GameRegistry.addRecipe(bag, "fef", "fnf", "fff",
 				'f', new ItemStack(carbon,1,6),
 				'e', ItemStuff.stack(ItemStuff.BLAZE_EMERALD),
 				'n', new ItemStack(Item.eyeOfEnder));
 		
 		for(int bagMeta = 0; bagMeta<16; bagMeta++) {
 			for(int dyeMeta = 0; dyeMeta<16; dyeMeta++) {
 				int newbag = ItemBagOfHolding.COLOR_CRAFTING[dyeMeta][bagMeta];
 				if(newbag!=-1) {
 					GameRegistry.addShapelessRecipe(new ItemStack(bagOfHolding,1,newbag),
 						new ItemStack(bagOfHolding,1,bagMeta),
 						new ItemStack(Item.dyePowder,1,dyeMeta));
 				}
 			}
 		}
 	}
 	public static void setUpCarbon() {
 		ItemStack wafe = new ItemStack(carbon,1,0),
 			heat = new ItemStack(carbon,1,4),
 			wovn = new ItemStack(carbon,1,6);
 		
 		for(int i=0; i<7; i++) LanguageRegistry.addName(new ItemStack(carbon,1,i), ItemCarbon.names[i]);
 		
 		//dense, compressed, metamorphic, fiber are Compactor-only, see CompactorRecipes
 		
 		addHomogeneousRecipe(wafe,new ItemStack(Item.coal),4);
 		FurnaceRecipes.smelting().addSmelting(wafe.itemID,0, ItemCarbon.stack(Metas.CB_PURE) ,3.0f);
 		
 		GameRegistry.addRecipe(wovn,"xx","xx",'x',ItemCarbon.stack(Metas.CB_FIBER));
 		FurnaceRecipes.smelting().addSmelting(heat.itemID,4,new ItemStack(Item.diamond),15.0f);
 	}
 	public static void setUpCompactors() {
 		GameRegistry.registerBlock(compactor, "Compactor");
 		GameRegistry.registerBlock(transmog, ItemBlockTmog.class, "Transmogrifier");
 		GameRegistry.registerTileEntity(TileEntityCompactor.class,	"compactstuff.compactor");
 		GameRegistry.registerTileEntity(TileEntityTransmog.class,	"compactstuff.transmog");
 		MinecraftForge.setBlockHarvestLevel(compactor, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(transmog, "pickaxe", 1);
 		
 		ItemStack comp = new ItemStack(compactor),
 				tmogCore = new ItemStack(transmog,1,0),
 				tmogShield = new ItemStack(transmog,1,1),
 				tmogFrame = new ItemStack(transmog,1,2);
 		
 		LanguageRegistry.addName(comp, "Compactor");
 		addCrossRecipe(comp,
 				ItemStuff.stack(ItemStuff.STEEL_INGOT), new ItemStack(Item.redstone), new ItemStack(Block.pistonBase),
 				new ItemStack(Item.enderPearl));
 				
 		LanguageRegistry.addName(tmogShield, "Transmogrifier Shield");
 		LanguageRegistry.addName(tmogFrame, "Transmogrifier Frame");
 		LanguageRegistry.addName(tmogCore, "Transmogrifier Core");
 		GameRegistry.addRecipe(new ShapedOreRecipe(tmogFrame, "ppp","csc","ppp",
 				'p', "plankWood",
 				'c', new ItemStack(carbon, 1, 6),
 				's', ItemStuff.stack(ItemStuff.STEEL_PLATE)));
 		GameRegistry.addRecipe(tmogCore, "udu", "ltl", "ece",
 				'u', new ItemStack(comBlock, 1, Metas.COMGOLD),
 				'd', new ItemStack(comBlock, 1, Metas.COMDIAMOND),
 				'l', new ItemStack(Block.blockLapis),
 				't', ItemStuff.stack(ItemStuff.TMOG_CRYSTAL),
 				'e', new ItemStack(comBlock, 1, Metas.COMSTEEL),
 				'c', new ItemStack(compactor));
 		//shield is Compactor-only, see CompactorRecipes.
 				
 	}
 	private static void setUpGlassAndStuff() {
 		ItemStack slag 	= ItemStuff.stack(ItemStuff.GLASS_SLAG), stack;
 		for(int i=0; i<ItemStuff.names.length; i++) {
 			if(ItemStuff.names[i]==null) continue;
 			stack = new ItemStack(itemStuff,1,i);
 			LanguageRegistry.addName(stack, stack.getItem().getUnlocalizedName(stack));
 		}			
 		
 		addBothAlternatingRecipes(ItemStuff.stack(ItemStuff.BLAZE_EMERALD),
 				new ItemStack(Item.ingotGold), new ItemStack(Item.blazePowder),
 				new ItemStack(Item.emerald));
 		GameRegistry.addRecipe(ItemStuff.stack(ItemStuff.GLASS_FIBER),"xxx",'x',slag);
 		//Transmogrifier Crystal, CA Plate, Gilded CA Plate are compactor-only, see CompactorRecipes in mods.CompactStuff.compactor
 		
 		addPlateRecipe(ItemStuff.DIAMOND_PLATE, Metas.COMDIAMOND, new ItemStack(Item.diamond));
 		addPlateRecipe(ItemStuff.IRON_PLATE, Metas.COMIRON, new ItemStack(Item.ingotIron));
 		addPlateRecipe(ItemStuff.STEEL_PLATE, Metas.COMSTEEL, ItemStuff.stack(ItemStuff.STEEL_INGOT));
 		addPlateRecipe(ItemStuff.GOLD_PLATE, Metas.COMGOLD, new ItemStack(Item.ingotGold));
 		
 		GameRegistry.addShapelessRecipe(ItemStuff.stack(ItemStuff.STEEL_INGOT,2),
				new ItemStack(Item.ingotIron),new ItemStack(Item.ingotIron),new ItemStack(Item.ingotIron),
				ItemCarbon.stack(Metas.CB_PURE),ItemCarbon.stack(Metas.CB_PURE),ItemCarbon.stack(Metas.CB_PURE));
 		
 		FurnaceRecipes.smelting().addSmelting(slag.itemID,ItemStuff.GLASS_SLAG,new ItemStack(Block.thinGlass),0f);
 	}
 	public static void setUpFurnaces() {
 		GameRegistry.registerBlock(furnace, ItemCompactFurnace.class, "Compact Furnace");
 		GameRegistry.registerTileEntity(TileEntityCobbleFurnace.class, "compactstuff.cobblefurnace");
 		GameRegistry.registerTileEntity(TileEntityCarbonFurnace.class, "compactstuff.carbonfurnace");
 		MinecraftForge.setBlockHarvestLevel(furnace,	"pickaxe", 0);
 		
 		ItemStack c = new ItemStack(comBlock,1,Metas.COMCOBBLE),
 				f	= new ItemStack(Block.furnaceIdle),
 				n	= new ItemStack(carbon,1,4),
 				comp= new ItemStack(furnace,1,2),
 				carb= new ItemStack(furnace,1,10);
 		
 		LanguageRegistry.addName(comp, "Compression Furnace");
 		LanguageRegistry.addName(carb, "Metamorphic Furnace");
 					
 		addFilledRecipe(comp, c, null);
 		addFilledRecipe(comp, f, f);
 		addFilledRecipe(carb, n, comp);
 	}
 	public static void setUpPlantBalls() {				
 		for(int i=0; i<4; i++) {
 			ItemStack pb = new ItemStack(plantBall, 1, i);
 			LanguageRegistry.addName(pb, pb.getItem().getUnlocalizedName(pb));
 			addReversableRecipe(pb, new ItemStack(Block.sapling, 1, i), 8);
 		}
 		LanguageRegistry.addName(new ItemStack(plantBall, 1, 4), "Seed ball");
 		addReversableRecipe(new ItemStack(plantBall, 1, 4), new ItemStack(Item.seeds), 8);	
 	}
 	public static void setUpComBlocks() {
 		GameRegistry.registerBlock(comBlock, ItemBlockCompressed.class, "Compressed Block");
 		GameRegistry.registerBlock(comGlass, "Compressed Glass");
 		
 		MinecraftForge.setBlockHarvestLevel(comBlock,  "pickaxe", 1);
 		
 		for(Integer i : BlockCompressed.names.keySet())
 			LanguageRegistry.addName(new ItemStack(comBlock,1,i),BlockCompressed.names.get(i));
 		LanguageRegistry.addName(comGlass, "Compressed Glass");
 		
 		ItemStack
 			blckSteel = new ItemStack(comBlock,1,Metas.STEELBLOCK),
 			comCobble = new ItemStack(comBlock,1,Metas.COMCOBBLE),
 			ingotIron = new ItemStack(Item.ingotIron),
 			compGlass = new ItemStack(comGlass,5),
 			vanlGlass = new ItemStack(Block.glass);
 		
 		//compressed iron, steel, diamond, gold, diorite creation all in CompactorRecipes
 		GameRegistry.addShapelessRecipe(BlockCompressed.stack(Metas.COMCOBBLE, 9), BlockCompressed.stack(Metas.DIORITE));
 		
 		addReversableRecipe(blckSteel, new ItemStack(itemStuff,1,ItemStuff.STEEL_INGOT), 9);
 		addReversableRecipe(new ItemStack(comBlock,1,Metas.COMCOAL), new ItemStack(Item.coal), 8);
 		addReversableRecipe(new ItemStack(comBlock,1,Metas.COMRACK), new ItemStack(Block.netherrack), 8);
 		addReversableRecipe(comCobble, new ItemStack(Block.cobblestone), 9);
 				
 		for(ItemStack a : fullHomogen.keySet()) addReversableRecipe(a, new ItemStack(fullHomogen.get(a)), 9);
 		
 		GameRegistry.addRecipe(compGlass,"gcg","igi","gcg",'g',vanlGlass,'c',comCobble,'i',ingotIron);
 		GameRegistry.addRecipe(compGlass,"gig","cgc","gig",'g',vanlGlass,'c',comCobble,'i',ingotIron);
 	}
 	private static void setUpSmeltStick() {
 		ItemStack stick = new ItemStack(smeltOnAStick);
 		LanguageRegistry.addName(stick, "Smelt-On-A-Stick");
 		GameRegistry.addRecipe(stick, "cdc", "sls", " t ",
 				'c', new ItemStack(Item.coal),
 				'd', new ItemStack(Item.diamond),
 				's', ItemStuff.stack(ItemStuff.STEEL_INGOT),
 				'l', new ItemStack(Item.bucketLava),
 				't', new ItemStack(Item.stick));
 	}
 	static java.util.HashMap<ItemStack, Block> fullHomogen = new java.util.HashMap<ItemStack, Block>();
 	static {
 		Integer[] a = {Metas.COMDIRT, Metas.COMSAND, Metas.COMGRAVEL};
 		Block[] b = {Block.dirt, Block.sand, Block.gravel};
 		for(int i=0; i<a.length; i++) fullHomogen.put(new ItemStack(comBlock, 1, a[i]), b[i]);
 	}
 }
