 package newdungeons;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.EnumMobType;
 import net.minecraft.block.material.Material;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.NetServerHandler;
 import net.minecraft.src.BaseMod;
 import net.minecraft.src.MLProp;
 import net.minecraft.src.ModLoader;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.tileentity.TileEntityDispenser;
 import net.minecraft.tileentity.TileEntityMobSpawner;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.biome.BiomeGenDesert;
 import net.minecraft.world.biome.BiomeGenEnd;
 import net.minecraft.world.biome.BiomeGenForest;
 import net.minecraft.world.biome.BiomeGenHell;
 import net.minecraft.world.biome.BiomeGenJungle;
 import net.minecraft.world.biome.BiomeGenOcean;
 import net.minecraft.world.biome.BiomeGenPlains;
 import net.minecraft.world.biome.BiomeGenSwamp;
 import net.minecraft.world.biome.BiomeGenTaiga;
 
 public final class mod_NewDungeons extends BaseMod
 {
     int xxx,zzz,yyy;
     static boolean doing = false;
     boolean changeingX;
     int change;
     public static BiomeGenBase biome;
     public static boolean vines;
     public static Block pressurePlatetest,stairsTest,modDimTorch,modBlockTest,modTripWire;
     public static ModBlockTripWireSource modTripWireSource;
     static ArrayList loot = new ArrayList(),potLoot = new ArrayList(),disLoot = new ArrayList();
     @MLProp(
             name = "OmaxSize",
             info = "Maximum size.",
             min = 10.0D,
             max = 200.0D
     )
     public static int OmaxSize = 110;
     @MLProp(
             name = "OminSize",
             info = "Minimum size.",
             min = 10.0D,
             max = 200.0D
     )
     public static int OminSize = 10;
     @MLProp(
             name = "OminRoomSize",
             info = "Minimum room size.",
             min = 1.0D,
             max = 50.0D
     )
     public static int OminRoomSize = 2;
     @MLProp(
             name = "Orareity",
             info = "Dungeon rarity (higher = more rare).",
             min = 10.0D,
             max = 100000.0D
     )
     public static int Orareity = 500;
     @MLProp(
             name = "OMinSquareBlocks",
             info = "Minimum size in square blocks.",
             min = 100.0D,
             max = 10000.0D
     )
     public static int OMinSquareBlocks = 500;
     @MLProp(
             name = "OadditionalItems",
             info = "itemId rarity value maxStack minStack minDanger enchProb maxEnchant damigeVal"
     )
     public static String OadditionalItems = "";
     @MLProp(
     		name = "pressurePlateID",
     		info = "Block ID for custom pressure plate"		
 	)
     public static int pressurePlateID = 252;
     @MLProp(
     		name = "dimTorchID",
     		info = "Block ID for custom torch"		
 	)
     public static int dimTorchID = 253;
     @MLProp(
     		name = "tripWireID",
     		info = "BlockID for custom tripwire"		
 	)
     public static int tripWireID = 254;
     @MLProp(
     		name = "tripWireSrcID",
     		info = "BlockID for custom hook"		
 	)
     public static int tripWireSrcID = 255;
     public int OsizeRedFac;
 	public static int Oheight;
     public int Ochest,Omine,Ospider,Ospawner,Opillar;
     public static boolean Odebug,OidCompatibility;
 
     /*private void checkVersion()
     {
         String var1 = "";
         URL var3 = null;
         URLConnection var4 = null;
         InputStreamReader var5 = null;
         BufferedReader var6 = null;
 
         try
         {
             var3 = new URL("http://dl.dropbox.com/u/719738/newDun%20version.txt");
             var4 = var3.openConnection();
             var5 = new InputStreamReader(var4.getInputStream());
             var6 = new BufferedReader(var5);
 
             while (true)
             {
                 String var2 = var6.readLine();
 
                 if (var2 == null)
                 {
                     break;
                 }
 
                 var1 = var1 + var2 + " ";
             }
         }
         catch (MalformedURLException var8)
         {
             System.out.println("Please check the URL:" + var8.toString());
         }
         catch (IOException var9)
         {
             ;
         }
 
         String[] var7 = var1.split(" ");
 
         if (var7.length > 1 && !var7[1].equals(this.getVersion()))
         {
             ;
         }
     }*/
     @Override
     public void load()
     {
         this.OsizeRedFac = 2;
         Oheight = 5;
         ++OminRoomSize;
         Odebug = false;
         this.Ochest = 1;
         this.Omine = 1;
         this.Ospider = 1;
         this.Ospawner = 1;
         this.Opillar = 1;
         OidCompatibility = false;
         this.addLoot();
         int var1;
 
         if (!OidCompatibility)
         {    
             pressurePlatetest = (new ModPressurePlate(pressurePlateID, "stone", EnumMobType.players, Material.rock)).setHardness(0.5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("pressurePlate").setTickRandomly(true);        
             ModLoader.registerBlock(pressurePlatetest);
         }
        modDimTorch = (new ModBlockTorch(dimTorchID)).setHardness(0.0F).setLightValue(0.5F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Dim Torch").setTextureName("torch_on").setTickRandomly(true);
        modTripWire = (new ModBlockTripWire(tripWireID)).setUnlocalizedName("modtripWire").setTextureName("trip_wire").setTickRandomly(true);
        modTripWireSource = (ModBlockTripWireSource)(new ModBlockTripWireSource(tripWireSrcID)).setUnlocalizedName("modtripWireSource").setTextureName("trip_wire_source").setTickRandomly(true);
 
         ModLoader.registerBlock(modDimTorch);
         ModLoader.addName(modDimTorch, "Dim Torch");
         ModLoader.addShapelessRecipe(new ItemStack(Block.torchWood, 6), new Object[] {Item.coal, modDimTorch});
         ModLoader.addShapelessRecipe(new ItemStack(Block.torchWood, 6), new Object[] {Item.flint, modDimTorch});
         ModLoader.addShapelessRecipe(new ItemStack(Block.torchWood, 8), new Object[] {Item.coal, Item.stick, modDimTorch});
     }
     @Override
     public void serverChat(NetServerHandler var1, String var2)
     {
         //System.out.println(var2);
         String[] var3 = var2.split(" ");
         //System.out.println(var3.length);
         //System.out.println(var3[0]);
 
         if (var3[0].equals("/makeDun") && var3.length == 3)
         {
             World var4 = var1.getPlayer().worldObj;
             Random var5 = new Random();
             int var6 = Integer.parseInt(var3[1]);
             int var7 = Integer.parseInt(var3[2]);
             double var8 = var1.getPlayer().lastTickPosX - (double)(var6 / 2);
             double var10 = var1.getPlayer().lastTickPosZ - (double)(var7 / 2);
             //System.out.println(var8 + " " + var10);
             byte var12 = 5;
             int var13 = 100;
             int var14;
 
             if (!Odebug)
             {
                 var13 = var5.nextInt(45) + 10;
 
                 for (var14 = 0; var14 < 10; ++var14)
                 {
                     try
                     {
                         var13 = var4.getTopSolidOrLiquidBlock((int)var8, (int)var10) - (var5.nextInt(30) + 20);
                     }
                     catch (Exception var16)
                     {
                         return;
                     }
 
                     if (var13 > 10 && var13 < 55)
                     {
                         break;
                     }
                 }
             }
 
             var14 = var6 * var7 / (var5.nextInt(50) + 50);
 
             if (makeDun1(var4, var5, var6, var7, var12, (int)var8, var13, (int)var10, var14, biome))
             {
                 var1.getPlayer().addChatMessage("successfully made a dungeon! look around.");
             }
         }
     }
 
     private void addLoot()
     {
         int var1 = 1;
         int var2 = 1;
         int var3 = 1;
         int var4 = 1;
         int var5 = 30;
         int var6 = 0;
         int var7 = 1;
         double var8 = 1.0D;
         double var10 = 1.0D;
 
         if (OadditionalItems != "")
         {
             String[] var12 = OadditionalItems.split("_");
 
             for (int var13 = 0; var13 < var12.length; ++var13)
             {
                 String[] var14 = var12[var13].split("-");
 
                 try
                 {
                     switch (var14.length)
                     {
                         case 1:case 2:
                             System.err.println("cannot add id" + Integer.parseInt(var14[0]) + "not enough arguments!");
                             break;
 
                         case 3:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             break;
 
                         case 4:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             var2 = Integer.parseInt(var14[3]);
                             break;
 
                         case 5:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             var2 = Integer.parseInt(var14[3]);
                             var3 = Integer.parseInt(var14[4]);
                             break;
 
                         case 6:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             var2 = Integer.parseInt(var14[3]);
                             var3 = Integer.parseInt(var14[4]);
                             var4 = Integer.parseInt(var14[5]);
                             break;
 
                         case 7:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             var2 = Integer.parseInt(var14[3]);
                             var3 = Integer.parseInt(var14[4]);
                             var4 = Integer.parseInt(var14[5]);
                             var6 = Integer.parseInt(var14[6]);
                             break;
 
                         case 8:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             var2 = Integer.parseInt(var14[3]);
                             var3 = Integer.parseInt(var14[4]);
                             var4 = Integer.parseInt(var14[5]);
                             var6 = Integer.parseInt(var14[6]);
                             var5 = Integer.parseInt(var14[7]);
                             break;
 
                         case 9:
                             var1 = Integer.parseInt(var14[0]);
                             var8 = Double.parseDouble(var14[1]);
                             var10 = Double.parseDouble(var14[2]);
                             var2 = Integer.parseInt(var14[3]);
                             var3 = Integer.parseInt(var14[4]);
                             var4 = Integer.parseInt(var14[5]);
                             var6 = Integer.parseInt(var14[6]);
                             var5 = Integer.parseInt(var14[7]);
                             var7 = Integer.parseInt(var14[8]);
                     }
                 }
                 catch (NumberFormatException var18)
                 {
                     System.err.println("mod_GenTest error: " + var18.toString());
                 }
                 Block var15 = Block.blocksList[var1];
                 loot.add(new DunLootItem(var15, var2, var3, var8, var10, var4, var5, var6, var7, var7));
                 
                 Item var20 = Item.itemsList[var1];
                 loot.add(new DunLootItem(var20, var2, var3, var8, var10, var4, var5, var6, var7, var7));
                 
             }
         }
 
         loot.add(new DunLootItem(Item.diamond, 6, 1, 20.0D, 15.0D, 300));
         loot.add(new DunLootItem(Block.torchWood, 15, 1, 3.0D, 3.0D, 30));
         loot.add(new DunLootItem(Block.dirt, 64, 15, 13.0D, 1.0D, -1));
         loot.add(new DunLootItem(Item.appleRed, 3, 1, 5.0D, 5.0D, 50));
         loot.add(new DunLootItem(Item.bone, 5, 1, 7.0D, 2.0D, 0));
         loot.add(new DunLootItem(Item.arrow, 16, 1, 10.0D, 7.0D, 50));
         loot.add(new DunLootItem(Item.bow, 1, 1, 20.0D, 15.0D, 100, 20, 20));
         loot.add(new DunLootItem(Item.painting, 2, 1, 25.0D, 3.0D, 30));
         loot.add(new DunLootItem(Block.planks, 5, 1, 4.0D, 10.0D, 40, 0, 0, 0, 3));
         loot.add(new DunLootItem(Block.wood, 5, 1, 16.0D, 15.0D, 60, 0, 0, 0, 3));
         loot.add(new DunLootItem(Item.leather, 5, 1, 10.0D, 8.0D, 20));
         loot.add(new DunLootItem(Item.legsLeather, 1, 1, 20.0D, 15.0D, 50));
         loot.add(new DunLootItem(Item.helmetLeather, 1, 1, 20.0D, 15.0D, 50));
         loot.add(new DunLootItem(Item.bootsLeather, 1, 1, 20.0D, 15.0D, 50));
         loot.add(new DunLootItem(Item.plateLeather, 1, 1, 20.0D, 15.0D, 50));
         loot.add(new DunLootItem(Item.legsIron, 1, 1, 35.0D, 30.0D, 250, 15, 15));
         loot.add(new DunLootItem(Item.helmetIron, 1, 1, 35.0D, 30.0D, 250, 15, 15));
         loot.add(new DunLootItem(Item.bootsIron, 1, 1, 35.0D, 30.0D, 250, 15, 15));
         loot.add(new DunLootItem(Item.plateIron, 1, 1, 35.0D, 30.0D, 250, 15, 15));
         loot.add(new DunLootItem(Item.bread, 5, 1, 7.0D, 5.0D, 50));
         loot.add(new DunLootItem(Item.bucketEmpty, 1, 1, 10.0D, 2.0D, 20));
         loot.add(new DunLootItem(Item.ingotIron, 10, 1, 20.0D, 7.0D, 150));
         loot.add(new DunLootItem(Block.oreIron, 10, 1, 10.0D, 5.0D, 100));
         loot.add(new DunLootItem(Item.cake, 1, 1, 40.0D, 20.0D, 60));
         loot.add(new DunLootItem(Item.beefRaw, 1, 1, 5.0D, 4.0D, 5));
         loot.add(new DunLootItem(Item.beefCooked, 1, 1, 6.0D, 8.0D, 5));
         loot.add(new DunLootItem(Block.melon, 3, 1, 17.0D, 10.0D, 30));
         loot.add(new DunLootItem(Item.melonSeeds, 5, 1, 8.0D, 7.0D, 30));
         loot.add(new DunLootItem(Block.pumpkin, 3, 1, 13.0D, 10.0D, 30));
         loot.add(new DunLootItem(Item.pumpkinSeeds, 5, 1, 8.0D, 7.0D, 30));
         loot.add(new DunLootItem(Block.cobblestone, 64, 15, 14.0D, 2.0D, 0));
         loot.add(new DunLootItem(Item.legsGold, 1, 1, 80.0D, 25.0D, 350, 17, 6));
         loot.add(new DunLootItem(Item.helmetGold, 1, 1, 80.0D, 25.0D, 350, 17, 6));
         loot.add(new DunLootItem(Item.bootsGold, 1, 1, 80.0D, 25.0D, 350, 17, 6));
         loot.add(new DunLootItem(Item.plateGold, 1, 1, 80.0D, 25.0D, 350, 17, 6));
         loot.add(new DunLootItem(Item.pickaxeWood, 1, 1, 20.0D, 2.0D, 70));
         loot.add(new DunLootItem(Item.pickaxeStone, 1, 1, 20.0D, 5.0D, 70));
         loot.add(new DunLootItem(Item.pickaxeIron, 1, 1, 50.0D, 15.0D, 100, 30, 20));
         loot.add(new DunLootItem(Item.pickaxeDiamond, 1, 1, 70.0D, 60.0D, 400, 50, 25));
         loot.add(new DunLootItem(Item.swordWood, 1, 1, 20.0D, 3.0D, 70));
         loot.add(new DunLootItem(Item.swordStone, 1, 1, 20.0D, 6.0D, 70));
         loot.add(new DunLootItem(Item.swordIron, 1, 1, 50.0D, 17.0D, 100, 30, 20));
         loot.add(new DunLootItem(Item.swordDiamond, 1, 1, 70.0D, 61.0D, 400, 50, 25));
         loot.add(new DunLootItem(Item.appleGold, 1, 1, 1000.0D, 100.0D, 200));
         loot.add(new DunLootItem(Item.enderPearl, 6, 1, 46.0D, 80.0D, 200));
         loot.add(new DunLootItem(Item.eyeOfEnder, 3, 1, 57.0D, 130.0D, 300));
         loot.add(new DunLootItem(Item.redstone, 64, 32, 60.0D, 3.0D, 15));
         loot.add(new DunLootItem(Block.tnt, 3, 1, 30.0D, 36.0D, 300));
         loot.add(new DunLootItem(Item.saddle, 1, 1, 10.0D, 60.0D, 30));
         loot.add(new DunLootItem(Item.rottenFlesh, 4, 1, 2.0D, 10.0D, 0));
         loot.add(new DunLootItem(Item.coal, 7, 1, 15.0D, 18.0D, 20));
         loot.add(new DunLootItem(Item.stick, 7, 1, 3.0D, 2.0D, 0));
         loot.add(new DunLootItem(Item.flintAndSteel, 1, 1, 20.0D, 100.0D, 200));
         loot.add(new DunLootItem(Item.flint, 7, 1, 15.0D, 5.0D, 13));
         loot.add(new DunLootItem(Block.obsidian, 3, 1, 34.0D, 40.0D, 200));
         loot.add(new DunLootItem(Item.emerald, 3, 1, 40.0D, 50.0D, 200));
 
         for (int var19 = 0; var19 <= 10; ++var19)
         {
             loot.add(new DunLootItem(Item.itemsList[Item.record13.itemID + var19], 1, 1, (double)(1000 + var19 * 2), 50.0D, 0));
         }
 
         loot.add(new DunLootItem(Item.itemsList[Item.potion.itemID], 1, 1, 27.0D, 36.0D, 100, 0, 99999999));
         potLoot.add(new DunLootItem(Item.glowstone, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.redstone, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.netherStalkSeeds, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.sugar, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.gunpowder, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.spiderEye, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.fermentedSpiderEye, 4, 1, 5.0D, 4.0D, 0));
         potLoot.add(new DunLootItem(Item.goldNugget, 4, 1, 1.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.melon, 4, 1, 1.0D, 1.0D, 0, 0, 0, 0, 1));
         potLoot.add(new DunLootItem(Item.ghastTear, 4, 1, 7.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.magmaCream, 4, 1, 3.0D, 1.0D, 0));
         potLoot.add(new DunLootItem(Item.glassBottle, 10, 1, 1.0D, 1.0D, 0));
         disLoot.add(new DunLootItem(Item.arrow, 5, 1, 1.0D, 1.0D, 10));
         disLoot.add(new DunLootItem(Item.snowball, 5, 1, 10.0D, 15.0D, 10));
         disLoot.add(new DunLootPotion(Item.itemsList[Item.potion.itemID], 6, 2, 1.0D, 0.0D, 0, 16420));
         disLoot.add(new DunLootPotion(Item.itemsList[Item.potion.itemID], 4, 2, 1.0D, 0.0D, 0, 16396));
         disLoot.add(new DunLootPotion(Item.itemsList[Item.potion.itemID], 5, 2, 1.0D, 0.0D, 0, 16428));
         disLoot.add(new DunLootPotion(Item.itemsList[Item.potion.itemID], 3, 2, 1.0D, 0.0D, 0, 16456));
         disLoot.add(new DunLootPotion(Item.itemsList[Item.potion.itemID], 3, 2, 1.0D, 0.0D, 0, 16458));
     }
     @Override
     public void generateSurface(World var1, Random var2, int var3, int var4)
     {
         biome = var1.getWorldChunkManager().getBiomeGenAt(var3, var4);
 
         if (var1.getWorldInfo().isMapFeaturesEnabled() && !(biome instanceof BiomeGenHell) && !(biome instanceof BiomeGenEnd))
         {
             this.genDun1(var1, var2, var3, var4);
             this.genDun5(var1, var2, var3, var4);
         }
     }
     /**
      * 
      * @deprecated Basically unused for now ?
      */
 @Deprecated
     private void genDun8(World var1, Random var2, int var3, int var4)
     {
         if (var2.nextInt(30) == 0)
         {
             int var5 = var3;
             int var6 = var1.getTopSolidOrLiquidBlock(var3, var4);
             int var7 = var4;
             boolean var8 = true;
             byte var9 = 7;
             byte var10 = 4;
             byte var11 = 5;
             int var12;
             int var13;
             int var14;
 
             for (var12 = 0; var12 < var9; ++var12)
             {
                 for (var13 = 0; var13 < var9; ++var13)
                 {
                     var14 = var1.getTopSolidOrLiquidBlock(var5 + var12, var7 + var13);
 
                     if (var14 > var6)
                     {
                         var6 = var14;
                     }
                 }
             }
 
             ++var6;
 
             for (var12 = 0; var12 < var9; ++var12)
             {
                 for (var13 = 0; var13 < var9; ++var13)
                 {
                     for (var14 = var6; var14 > 0 && var1.getBlockMaterial(var12 + var5, var14, var13 + var7) != Material.rock; --var14)
                     {
                         var1.setBlock(var5 + var12, var14, var7 + var13, Block.cobblestone.blockID);
                     }
                 }
             }
 
             for (var12 = 0; var12 < var10; ++var12)
             {
                 for (var13 = 0; var13 < var9; ++var13)
                 {
                     for (var14 = 0; var14 < var9; ++var14)
                     {
                         for (int var15 = 0; var15 < var11; ++var15)
                         {
                             if (var13 == 0 || var13 == var9 - 1 || var14 == 0 || var14 == var9 - 1 || var15 == var11 - 1)
                             {
                                 var1.setBlock(var5 + var13, var6 + var15 + var12 * var11, var7 + var14, Block.netherBrick.blockID);
 
                                 if (var2.nextInt(10) == 0)
                                 {
                                     var1.setBlock(var5 + var13, var6 + var15 + var12 * var11, var7 + var14, Block.gravel.blockID);
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void genDun7(World var1, Random var2, int var3, int var4)
     {
         if (this.chance(var2, 0.3D))
         {
             if (Odebug)
             {
                 for (int var5 = 128; var5 > 0 && var1.getBlockId(var3, var5, var4) == 0; --var5)
                 {
                     var1.setBlock(var3, var5, var4, Block.glass.blockID);
                 }
             }
 
             this.genDun7Helper(var1, var2, var3, MathHelper.getRandomIntegerInRange(var2, 30, 40), var4, 100.0D);
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void genDun7Helper(World var1, Random var2, int var3, int var4, int var5, double var6)
     {
         if (var4 > 7 && this.chance(var2, var6))
         {
             int var9;
             int var10;
 
             for (int var8 = -1; var8 <= 1; ++var8)
             {
                 for (var9 = -1; var9 <= 1; ++var9)
                 {
                     for (var10 = -1; var10 <= 1; ++var10)
                     {
                         if (var6 > 75.0D && Math.abs(var8) + Math.abs(var9) + Math.abs(var10) == 1 && (!(var1.getBlockId(var3 + var8 * 1, var4 + var9 * 1, var5 + var10 * 1) == 0) || var1.getBlockId(var3 + var8 * 1, var4 + var9 * 1, var5 + var10 * 1) == Block.vine.blockID))
                         {
                             this.genDun7Helper(var1, var2, var3 + var8 * 1, var4 + var9 * 1, var5 + var10 * 1, var6 - 0.5D);
                         }
                     }
                 }
             }
 
             byte var13 = 1;
 
             for (var9 = -var13; var9 <= var13; ++var9)
             {
                 for (var10 = -var13; var10 <= var13; ++var10)
                 {
                     for (int var11 = -var13; var11 <= var13; ++var11)
                     {
                         int var12 = var1.getBlockId(var3 + var9, var4 + var10, var5 + var11);
 
                         if (var12 != Block.bedrock.blockID && var12 != Block.planks.blockID && var12 != Block.fence.blockID && var12 != Block.rail.blockID && var12 != Block.torchWood.blockID)
                         {
                             var1.setBlockToAir(var3 + var9, var4 + var10, var5 + var11);
                         }
 
                         if (this.chance(var2, 5.0D))
                         {
                             ;
                         }
                     }
                 }
             }
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private double distance(int var1, int var2, int var3, int var4, int var5, int var6)
     {
         return (double)MathHelper.sqrt_double((double)((var1 - var2 ^ 2) + (var3 - var4 ^ 2) + (var5 - var6 ^ 2)));
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void smooth(World var1, int var2, int var3, int var4)
     {
         ArrayList var5 = new ArrayList();
         boolean var6 = false;
         int var7;
         int var8;
         int var9;
 
         for (var7 = -1; var7 <= 1; ++var7)
         {
             for (var8 = -1; var8 <= 1; ++var8)
             {
                 var9 = -1;
 
                 while (var9 <= 1)
                 {
                     int var12 = var1.getBlockId(var2 + var7, var3 + var8, var4 + var9);
                     boolean var10 = false;
                     int var11 = 0;
 
                     while (true)
                     {
                         if (var11 < var5.size())
                         {
                             if (((Integer)((ArrayList)var5.get(var11)).get(0)).intValue() != var12)
                             {
                                 ++var11;
                                 continue;
                             }
 
                             ((ArrayList)var5.get(var11)).set(1, Integer.valueOf(((Integer)((ArrayList)var5.get(var11)).get(1)).intValue() + 1));
                             var10 = true;
                         }
 
                         if (!var10)
                         {
                             ArrayList var14 = new ArrayList();
                             var14.add(Integer.valueOf(var12));
                             var14.add(Integer.valueOf(1));
                             var5.add(var14);
                         }
 
                         ++var9;
                         break;
                     }
                 }
             }
         }
 
         var7 = 0;
         var8 = 0;
         var9 = 0;
 
         for (int var13 = 0; var13 < var5.size(); ++var13)
         {
             if (((Integer)((ArrayList)var5.get(var13)).get(1)).intValue() > var7)
             {
                 var8 = var7;
                 var7 = ((Integer)((ArrayList)var5.get(var13)).get(1)).intValue();
                 var9 = var13;
             }
         }
 
         if (var8 * 10 > var7 || ((Integer)((ArrayList)var5.get(var9)).get(0)).intValue() == 0)
         {
             var1.setBlock(var2, var3, var4, ((Integer)((ArrayList)var5.get(var9)).get(0)).intValue());
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private boolean chance(Random var1, double var2)
     {
         double var4 = (double)var1.nextInt(10000) / 100.0D;
         return var2 > var4;
     }
 
     private void genDun5(World var1, Random var2, int var3, int var4)
     {
         for (int var5 = 0; var5 < 10; ++var5)
         {
             if (var2.nextInt(2) == 0)
             {
                 int var6 = var2.nextInt(16) + 1 + var3;
                 int var7 = var2.nextInt(16) + 1 + var4;
                 int var8;
 
                 do
                 {
                     do
                     {
                         var8 = var2.nextInt(128);
 
                         try
                         {
                             var1.getTopSolidOrLiquidBlock(var6, var7);
                         }
                         catch (Exception var12)
                         {
                             return;
                         }
                     }
                     while (var1.getTopSolidOrLiquidBlock(var6, var7) == var8);
                 }
                 while (var1.getBlockId(var6, var8 - 1, var7) == Block.waterStill.blockID);
 
                 if (var1.getBlockId(var6, var8, var7) == 0 && var1.getBlockId(var6, var8 - 1, var7) != 0)
                 {
                     var1.setBlock(var6, var8, var7, Block.chest.blockID);
                     ArrayList var9 = new ArrayList();
                     var9.add(new DunChest(var6, var8, var7));
                     int var10 = 60 - var8;
                     int var11 = var10 * 15;
                     var10 *= 13;
                     putLoot(var1, var2, var9, var10, var11);
                 }
             }
         }
     }
     /**
      * 
      * @deprecated Basically unused for now ?
      */
 @Deprecated
     private void genDun4(World var1, Random var2, int var3, int var4)
     {
         biome = var1.getWorldChunkManager().getBiomeGenAt(var3, var4);
 
         if (var2.nextInt(30) == 0)
         {
             boolean var5 = true;
             boolean var6 = false;
             byte var7 = 20;
             byte var8 = 20;
             byte var9 = 3;
             boolean var10 = true;
             boolean var11 = true;
             int var12 = var3;
             int var13 = var4;
             int var14 = var1.getTopSolidOrLiquidBlock(var3, var4);
 
             for (int var17 = 0; var17 < var7 * 2; var17 += 2)
             {
                 for (int var18 = 0; var18 < var8 * 2; var18 += 2)
                 {
                     int var15 = var2.nextInt(3);
                     int var16 = var2.nextInt(3);
 
                     for (int var19 = 0; var19 < var9; ++var19)
                     {
                         var1.setBlock(var12 + var17, var14 + var19, var13 + var18, 0);
                         var1.setBlock(var12 + var17 + 1, var14 + var19, var13 + var18 + 1, Block.stoneBrick.blockID);
 
                         if (var15 == 0)
                         {
                             var1.setBlock(var12 + var17 + 1, var14 + var19, var13 + var18, Block.stoneBrick.blockID);
                         }
                         else
                         {
                             var1.setBlock(var12 + var17 + 1, var14 + var19, var13 + var18, 0);
                         }
 
                         if (var16 == 0)
                         {
                             var1.setBlock(var12 + var17, var14 + var19, var13 + var18 + 1, Block.stoneBrick.blockID);
                         }
                         else
                         {
                             var1.setBlock(var12 + var17, var14 + var19, var13 + var18 + 1, 0);
                         }
                     }
 
                     if (var2.nextInt(10) == 0)
                     {
                         var1.setBlock(var12 + var17, var14, var13 + var18, Block.mobSpawner.blockID);
                         TileEntityMobSpawner var34 = null;
 
                         try
                         {
                             var34 = (TileEntityMobSpawner)var1.getBlockTileEntity(var12 + var17, var14, var13 + var18);
                         }
                         catch (Exception var29)
                         {
                             var29.printStackTrace();
                             var1.setBlockToAir(var12 + var17, var14, var13 + var18);
                         }
 
                         if (var34 != null)
                         {
                             String var20 = pickMobSpawner(var2);
                             var34.getSpawnerLogic().setMobID(var20);
                         }
                         else
                         {
                             System.err.println("Failed to fetch mob spawner entity at (" + (var12 + var17) + ", " + var14 + ", " + (var13 + var18) + ")");
                         }
                     }
                     else if (var2.nextInt(10) == 0)
                     {
                         if (!OidCompatibility)
                         {
                             var1.setBlock(var12 + var17, var14, var13 + var18, pressurePlatetest.blockID);
                         }
                         else
                         {
                             var1.setBlock(var12 + var17, var14, var13 + var18, Block.pressurePlateStone.blockID, 3, 3);
                         }
 
                         var1.setBlock(var12 + var17, var14 - 2, var13 + var18, Block.tnt.blockID);
                         var1.setBlock(var12 + var17, var14 - 3, var13 + var18, Block.stone.blockID);
                     }
                 }
             }
 
             boolean var30 = false;
             boolean var31 = false;
             boolean var32 = false;
             int var21;
             int var22;
             int var33;
 
             for (var33 = -1; var33 < var7 * 2 + 1; ++var33)
             {
                 for (var21 = -1; var21 < var8 * 2 + 1; ++var21)
                 {
                     if (var33 == -1 || var33 == var7 * 2 || var21 == -1 || var21 == var8 * 2)
                     {
                         for (var22 = 0; var22 < var9; ++var22)
                         {
                             var1.setBlock(var12 + var33, var14 + var22, var13 + var21, Block.stoneBrick.blockID);
                         }
 
                         var1.setBlock(var12 + var33, var14 + var9 + 1, var13 + var21, Block.fenceIron.blockID);
                     }
 
                     var1.setBlock(var12 + var33, var14 - 1, var13 + var21, Block.stoneBrick.blockID);
                     var1.setBlock(var12 + var33, var14 + var9, var13 + var21, Block.stoneBrick.blockID);
                 }
             }
 
             for (var33 = 0; var33 < 100; ++var33)
             {
                 var21 = var2.nextInt(var7);
                 var22 = var2.nextInt(var8);
                 int var23 = Math.abs(var21 - var7 / 2);
                 int var24 = Math.abs(var22 - var8 / 2);
 
                 if (var23 > var24)
                 {
                     if (var21 > var7 / 2)
                     {
                         var21 = var7 + 1;
                     }
                     else
                     {
                         var21 = -1;
                     }
                 }
                 else if (var22 > var8 / 2)
                 {
                     var22 = var8 + 1;
                 }
                 else
                 {
                     var22 = -1;
                 }
 
                 var1.setBlock(var12 + var21, var14 + var9 + 5, var13 + var22, 1);
                 boolean var25 = var1.getBlockId(var12 + var21 - 1, var14 + 1, var13 + var22) == 0;
                 boolean var26 = var1.getBlockId(var12 + var21 + 1, var14 + 1, var13 + var22) == 0;
                 boolean var27 = var1.getBlockId(var12 + var21, var14 + 1, var13 + var22 - 1) == 0;
                 boolean var28 = var1.getBlockId(var12 + var21, var14 + 1, var13 + var22 + 1) == 0;
 
                 if (var25 && var26 || var27 && var28)
                 {
                     for (var24 = 1; var24 < 5; ++var24)
                     {
                         var1.setBlock(var12 + var21, var14 + var24, var13 + var22, 0);
                     }
 
                     return;
                 }
             }
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void genDun3(World var1, Random var2, int var3, int var4)
     {
         if (var2.nextInt(50) == 1)
         {
             Dun3Node var5 = new Dun3Node(var3, var2.nextInt(30) + 10, var4, 5);
             var5.makeNodes(var2);
             var5.genTunels(var1);
             var1.setBlock(var3, var1.getTopSolidOrLiquidBlock(var3, var4) + 20, var4, 1);
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void genDun2(World var1, Random var2, int var3, int var4)
     {
         if (var2.nextInt(50) == 1)
         {
             biome = var1.getWorldChunkManager().getBiomeGenAt(var3, var4);
 
             if (!(biome instanceof BiomeGenPlains) && !(biome instanceof BiomeGenDesert))
             {
                 ;
             }
 
             int var5 = var3;
             int var6 = var4;
             int var7 = var1.getTopSolidOrLiquidBlock(var3, var4) + 10;
             int var8 = var2.nextInt(10) + 20;
             int var9 = var2.nextInt(10) + 20;
             byte var10 = 6;
             int var11 = var1.getTopSolidOrLiquidBlock(var3, var4);
             int var12 = var1.getTopSolidOrLiquidBlock(var3, var4 + var9);
             int var13 = var1.getTopSolidOrLiquidBlock(var3 + var8, var4);
             int var14 = var1.getTopSolidOrLiquidBlock(var3 + var8, var4 + var9);
             int var15;
 
             for (var15 = var11; var15 < var7; ++var15)
             {
                 var1.setBlock(var5, var15, var6, Block.netherBrick.blockID);
             }
 
             for (var15 = var12; var15 < var7; ++var15)
             {
                 var1.setBlock(var5, var15, var6 + var9, Block.netherBrick.blockID);
             }
 
             for (var15 = var13; var15 < var7; ++var15)
             {
                 var1.setBlock(var5 + var8, var15, var6, Block.netherBrick.blockID);
             }
 
             for (var15 = var14; var15 < var7; ++var15)
             {
                 var1.setBlock(var5 + var8, var15, var6 + var9, Block.netherBrick.blockID);
             }
 
             byte var19 = 7;
             int var17;
             int var16;
 
             for (var16 = var19; var16 > 0; --var16)
             {
                 for (var17 = 1; var17 <= var16; ++var17)
                 {
                     var1.setBlock(var5 + var17, var7 - var19 + var16, var6, Block.netherBrick.blockID);
                     var1.setBlock(var5 + var17, var7 - var19 + var16, var6 + var9, Block.netherBrick.blockID);
                 }
             }
 
             for (var16 = var19; var16 > 0; --var16)
             {
                 for (var17 = 1; var17 <= var16; ++var17)
                 {
                     var1.setBlock(var5 + var8 - var17, var7 - var19 + var16, var6, Block.netherBrick.blockID);
                     var1.setBlock(var5 + var8 - var17, var7 - var19 + var16, var6 + var9, Block.netherBrick.blockID);
                 }
             }
 
             for (var16 = var19; var16 > 0; --var16)
             {
                 for (var17 = 1; var17 <= var16; ++var17)
                 {
                     var1.setBlock(var5, var7 - var19 + var16, var6 + var17, Block.netherBrick.blockID);
                     var1.setBlock(var5 + var8, var7 - var19 + var16, var6 + var17, Block.netherBrick.blockID);
                 }
             }
 
             for (var16 = var19; var16 > 0; --var16)
             {
                 for (var17 = 1; var17 <= var16; ++var17)
                 {
                     var1.setBlock(var5, var7 - var19 + var16, var6 - var17 + var9, Block.netherBrick.blockID);
                     var1.setBlock(var5 + var8, var7 - var19 + var16, var6 - var17 + var9, Block.netherBrick.blockID);
                 }
             }
 
             for (var16 = 0; var16 <= var8 - 2 * var19; ++var16)
             {
                 var1.setBlock(var5 + var19 + var16, var7 - 1, var6, Block.netherFence.blockID);
                 var1.setBlock(var5 + var19 + var16, var7 - 1, var6 + var9, Block.netherFence.blockID);
             }
 
             for (var16 = 0; var16 <= var9 - 2 * var19; ++var16)
             {
                 var1.setBlock(var5, var7 - 1, var6 + var19 + var16, Block.netherFence.blockID);
                 var1.setBlock(var5 + var8, var7 - 1, var6 + var19 + var16, Block.netherFence.blockID);
             }
 
             for (var16 = 0; var16 <= var8; ++var16)
             {
                 for (var17 = 0; var17 <= var9; ++var17)
                 {
                     var1.setBlock(var5 + var16, var7, var6 + var17, Block.netherBrick.blockID);
                     var1.setBlock(var5 + var16, var7 + var10 - 1, var6 + var17, Block.netherBrick.blockID);
 
                     if (var16 == 0 || var16 == var8 || var17 == 0 || var17 == var9)
                     {
                         for (int var18 = 0; var18 < var10; ++var18)
                         {
                             var1.setBlock(var5 + var16, var7 + var18, var6 + var17, Block.netherBrick.blockID);
                         }
                     }
                 }
             }
 
             for (var16 = 0; (float)var16 < MathHelper.sqrt_double((double)(var8 * var9)) * 2.0F; ++var16)
             {
                 this.breakMe(var1, var2, var5 + var2.nextInt(var8 - 5) + 5, var7, var6 + var2.nextInt(var9 - 5) + 5, var2.nextInt(3));
             }
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void breakMe(World var1, Random var2, int var3, int var4, int var5, int var6)
     {
         if (var6 > 0 && var1.getBlockId(var3, var4, var5) == Block.netherBrick.blockID && var1.getBlockId(var3, var4 + 1, var5) == 0)
         {
             if (var2.nextInt(2) == 0)
             {
                 var1.setBlock(var3, var4, var5, 0);
             }
             else if (var2.nextInt(2) == 0)
             {
                 var1.setBlock(var3, var4, var5, Block.stairsNetherBrick.blockID, var2.nextInt(4), 3);
             }
 
             for (int var7 = -1; var7 < 2; ++var7)
             {
                 for (int var8 = -1; var8 < 2; ++var8)
                 {
                     if (Math.abs(var7) != Math.abs(var8))
                     {
                         if (var2.nextInt(2) == 0)
                         {
                             var1.setBlock(var3 + var7, var4, var5 + var8, Block.stairsNetherBrick.blockID, var2.nextInt(4), 3);
                         }
                         else
                         {
                             this.breakMe(var1, var2, var3 + var7, var4, var5 + var8, var6 - 1);
                         }
                     }
                 }
             }
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private boolean canGenHere(World var1, int var2, int var3, int var4, int var5, int var6, int var7)
     {
         return true;
     }
     
     private static boolean makeDun1(World var0, Random var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, BiomeGenBase var9)
     {
         int var10 = 0;
         int var11 = 0;
         int var12 = 0;
         int var13 = 0;
         int var14;
         int var15;
         int var16;
 
         for (var14 = 0; var14 < var2 + 1; ++var14)
         {
             for (var15 = 0; var15 < var3 + 1; ++var15)
             {
                 for (var16 = 1; var16 < var4 - 1; ++var16)
                 {
                     if (var0.getBlockId(var5 + var14, var6 + var16, var7 + var15) != Block.chest.blockID)
                     {
                         var0.setBlock(var5 + var14, var6 + var16, var7 + var15, 0);
                     }
                 }
             }
         }
 
         for (var14 = 0; var14 < var2 + 1; ++var14)
         {
             for (var15 = 0; var15 < var3 + 1; ++var15)
             {
                 genStone(var0, var1, var5 + var14, var6, var7 + var15);
 
                 if (!Odebug)
                 {
                     genStone(var0, var1, var5 + var14, var6 + var4 - 1, var7 + var15, true);
                 }
 
                 if (var14 == 0 || var14 == var2 || var15 == 0 || var15 == var3)
                 {
                     for (var16 = 1; var16 < var4 - 1; ++var16)
                     {
                         genStone(var0, var1, var5 + var14, var6 + var16, var7 + var15);
                     }
                 }
             }
         }
 
         ArrayList var37 = new ArrayList();
         var15 = 0;
         int var17;
         int var18;
         int var21;
         boolean var20;
         int var23;
         int var22;
         int var25;
         int var24;
         int var26;
 
         while (var15 < var8 + var1.nextInt(var8 * 5) + var1.nextInt(var8) * -1)
         {
             var16 = var1.nextInt(var2 - 2) + 1;
             var17 = var1.nextInt(var3 - 2) + 1;
             var18 = var1.nextInt(10 - OminRoomSize + 1) + OminRoomSize;
             var18 = var1.nextInt(var18 - OminRoomSize + 1) + OminRoomSize;
             boolean var19 = true;
             var20 = true;
             var21 = 0;
 
             while (true)
             {
                 if (var21 < var18)
                 {
                     if (var0.getBlockMaterial(var5 + var16 + var21, var6 + 1, var7 + var17) != Material.rock && var0.getBlockMaterial(var5 + var16 - var21, var6 + 1, var7 + var17) != Material.rock)
                     {
                         ++var21;
                         continue;
                     }
 
                     var20 = false;
                 }
 
                 for (var21 = 0; var21 < var18; ++var21)
                 {
                     if (var0.getBlockMaterial(var5 + var16, var6 + 1, var7 + var17 + var21) == Material.rock || var0.getBlockMaterial(var5 + var16, var6 + 1, var7 + var17 - var21) == Material.rock)
                     {
                         var19 = false;
                         break;
                     }
                 }
 
                 if (var19 || var20)
                 {
                     for (var21 = 1; var21 < var4 - 1; ++var21)
                     {
                         genStone(var0, var1, var5 + var16, var6 + var21, var7 + var17);
                     }
                 }
 
                 var21 = 1;
                 var22 = 1;
                 var23 = 1;
                 var24 = 1;
 
                 if (var19)
                 {
                     for (var25 = 1; var25 < var2 && var0.getBlockMaterial(var5 + var16 + var25, var6 + 1, var7 + var17) != Material.rock; ++var25)
                     {
                         ++var21;
 
                         for (var26 = 1; var26 < var4 - 1; ++var26)
                         {
                             genStone(var0, var1, var5 + var16 + var25, var6 + var26, var7 + var17);
                         }
                     }
 
                     for (var25 = 1; var25 < var2 && var0.getBlockMaterial(var5 + var16 - var25, var6 + 1, var7 + var17) != Material.rock; ++var25)
                     {
                         ++var22;
 
                         for (var26 = 1; var26 < var4 - 1; ++var26)
                         {
                             genStone(var0, var1, var5 + var16 - var25, var6 + var26, var7 + var17);
                         }
                     }
                 }
 
                 if (var20)
                 {
                     for (var25 = 1; var25 < var3 && var0.getBlockMaterial(var5 + var16, var6 + 1, var7 + var17 + var25) != Material.rock; ++var25)
                     {
                         ++var23;
 
                         for (var26 = 1; var26 < var4 - 1; ++var26)
                         {
                             genStone(var0, var1, var5 + var16, var6 + var26, var7 + var17 + var25);
                         }
                     }
 
                     for (var25 = 1; var25 < var3 && var0.getBlockMaterial(var5 + var16, var6 + 1, var7 + var17 - var25) != Material.rock; ++var25)
                     {
                         ++var24;
 
                         for (var26 = 1; var26 < var4 - 1; ++var26)
                         {
                             genStone(var0, var1, var5 + var16, var6 + var26, var7 + var17 - var25);
                         }
                     }
                 }
 
                 if (var21 > 1 || var22 > 1 || var23 > 1 || var24 > 1)
                 {
                     var37.add(new Dun1Node(var21, var22, var23, var24, var5 + var16, var6 + 1, var7 + var17));
                 }
 
                 ++var15;
                 break;
             }
         }
 
         var15 = var1.nextInt(var1.nextInt(2) + 1) + 1;
         int var27;
         int var28;
         int var40;
         int var41;
 
         for (var16 = 0; var16 < var37.size() - 1; ++var16)
         {
             var17 = ((Dun1Node)var37.get(var16)).wp;
             var18 = ((Dun1Node)var37.get(var16)).wn;
             var40 = ((Dun1Node)var37.get(var16)).lp;
             var41 = ((Dun1Node)var37.get(var16)).ln;
             var21 = ((Dun1Node)var37.get(var16)).x;
             var22 = ((Dun1Node)var37.get(var16)).z;
             var23 = ((Dun1Node)var37.get(var16)).y;
             boolean var29;
             boolean var31;
             boolean var30;
             boolean var32;
             int var33;
 
             for (var24 = var1.nextInt(var15) + 1; var24 > 0; --var24)
             {
                 var25 = var1.nextInt(var17);
                 var1.nextInt(var18);
                 var1.nextInt(var40);
                 var1.nextInt(var41);
 
                 if (var17 > 2)
                 {
                     var29 = var0.getBlockMaterial(var21 + var25 + 1, var23, var22) == Material.rock;
                     var30 = var0.getBlockMaterial(var21 + var25 - 1, var23, var22) == Material.rock;
                     var31 = var0.getBlockMaterial(var21 + var25, var23, var22 + 1) == Material.rock;
                     var32 = var0.getBlockMaterial(var21 + var25, var23, var22 - 1) == Material.rock;
                     var33 = 0;
 
                     if (var29)
                     {
                         ++var33;
                     }
 
                     if (var30)
                     {
                         ++var33;
                     }
 
                     if (var31)
                     {
                         ++var33;
                     }
 
                     if (var32)
                     {
                         ++var33;
                     }
 
                     if ((var29 && var30) ^ (var31 && var32) && var33 == 2)
                     {
                         var0.setBlock(var21 + var25, var23, var22, 0);
                         var0.setBlock(var21 + var25, var23 + 1, var22, 0);
                     }
                 }
             }
 
             for (var24 = var1.nextInt(var15) + 1; var24 > 0; --var24)
             {
                 var1.nextInt(var17);
                 var26 = var1.nextInt(var18);
                 var1.nextInt(var40);
                 var1.nextInt(var41);
 
                 if (var18 > 2)
                 {
                     var29 = var0.getBlockMaterial(var21 - var26 + 1, var23, var22) == Material.rock;
                     var30 = var0.getBlockMaterial(var21 - var26 - 1, var23, var22) == Material.rock;
                     var31 = var0.getBlockMaterial(var21 - var26, var23, var22 + 1) == Material.rock;
                     var32 = var0.getBlockMaterial(var21 - var26, var23, var22 - 1) == Material.rock;
                     var33 = 0;
 
                     if (var29)
                     {
                         ++var33;
                     }
 
                     if (var30)
                     {
                         ++var33;
                     }
 
                     if (var31)
                     {
                         ++var33;
                     }
 
                     if (var32)
                     {
                         ++var33;
                     }
 
                     if ((var29 && var30) ^ (var31 && var32) && var33 == 2)
                     {
                         var0.setBlock(var21 - var26, var23, var22, 0);
                         var0.setBlock(var21 - var26, var23 + 1, var22, 0);
                     }
                 }
             }
 
             for (var24 = var1.nextInt(var15) + 1; var24 > 0; --var24)
             {
                 var1.nextInt(var17);
                 var1.nextInt(var18);
                 var27 = var1.nextInt(var40);
                 var1.nextInt(var41);
 
                 if (var40 > 2)
                 {
                     var29 = var0.getBlockMaterial(var21 + 1, var23, var22 + var27) == Material.rock;
                     var30 = var0.getBlockMaterial(var21 - 1, var23, var22 + var27) == Material.rock;
                     var31 = var0.getBlockMaterial(var21, var23, var22 + 1 + var27) == Material.rock;
                     var32 = var0.getBlockMaterial(var21, var23, var22 - 1 + var27) == Material.rock;
                     var33 = 0;
 
                     if (var29)
                     {
                         ++var33;
                     }
 
                     if (var30)
                     {
                         ++var33;
                     }
 
                     if (var31)
                     {
                         ++var33;
                     }
 
                     if (var32)
                     {
                         ++var33;
                     }
 
                     if ((var29 && var30) ^ (var31 && var32) && var33 == 2)
                     {
                         var0.setBlock(var21, var23, var22 + var27, 0);
                         var0.setBlock(var21, var23 + 1, var22 + var27, 0);
                     }
                 }
             }
 
             for (var24 = var1.nextInt(var15) + 1; var24 > 0; --var24)
             {
                 var1.nextInt(var17);
                 var1.nextInt(var18);
                 var1.nextInt(var40);
                 var28 = var1.nextInt(var41);
 
                 if (var41 > 2)
                 {
                     var29 = var0.getBlockMaterial(var21 + 1, var23, var22 - var28) == Material.rock;
                     var30 = var0.getBlockMaterial(var21 - 1, var23, var22 - var28) == Material.rock;
                     var31 = var0.getBlockMaterial(var21, var23, var22 + 1 - var28) == Material.rock;
                     var32 = var0.getBlockMaterial(var21, var23, var22 - 1 - var28) == Material.rock;
                     var33 = 0;
 
                     if (var29)
                     {
                         ++var33;
                     }
 
                     if (var30)
                     {
                         ++var33;
                     }
 
                     if (var31)
                     {
                         ++var33;
                     }
 
                     if (var32)
                     {
                         ++var33;
                     }
 
                     if ((var29 && var30) ^ (var31 && var32) && var33 == 2)
                     {
                         var0.setBlock(var21, var23, var22 - var28, 0);
                         var0.setBlock(var21, var23 + 1, var22 - var28, 0);
                     }
                 }
             }
         }
 
         boolean var39 = false;
         boolean var51;
         int var49;
         int var48;
         int var58;
 
         for (var17 = 0; var17 < 100; ++var17)
         {
             var18 = var1.nextInt(var2);
             var40 = var1.nextInt(var3);
             var20 = false;
 
             for (var21 = -1; var21 < 2; ++var21)
             {
                 for (var22 = -1; var22 < 2; ++var22)
                 {
                     if (var0.getBlockMaterial(var5 + var18 + var21, var6 + 1, var7 + var40 + var22) != Material.air)
                     {
                         var20 = true;
                         break;
                     }
                 }
 
                 if (var20)
                 {
                     break;
                 }
             }
 
             if (!var20)
             {
                 var21 = var0.getTopSolidOrLiquidBlock(var5 + var18, var7 + var40);
                 var12 = var21;
 
                 if (var21 >= 60)
                 {
                     byte var43 = 5;
                     var23 = -1;
                     var24 = -1;
                     var25 = 0;
                     var51 = true;
                     var27 = 1;
 
                     for (var28 = var6 + 4; var28 < var21; ++var28)
                     {
                         for (var48 = -1; var48 < 2; ++var48)
                         {
                             for (var49 = -1; var49 < 2; ++var49)
                             {
                                 var0.setBlock(var5 + var18 + var48, var28, var7 + var40 + var49, 0);
                             }
                         }
                     }
 
                     boolean var52 = false;
 
                     if (var1.nextInt(3) == 0)
                     {
                         var52 = true;
                     }
 
                     if (!var52 && var1.nextInt(2) == 0)
                     {
                         var0.setBlock(var5 + var18, var6, var7 + var40, Block.waterStill.blockID);
 
                         if (var1.nextInt(5) != 0)
                         {
                             var0.setBlockToAir(var5 + var18, var6 - 1, var7 + var40);
                         }
                     }
 
                     for (var48 = 0; var48 < var21 * 4; ++var48)
                     {
                         var0.setBlock(var5 + var18 + var23, var6 + 1 + var25, var7 + var40 + var24, BlockHalfSlab.stoneSingleSlab.blockID, var43, 3);
 
                         if (var52)
                         {
                             genStone(var0, var1, var5 + var18, var6 + 1 + var25, var7 + var40);
                         }
 
                         if (var51)
                         {
                             var23 += var27;
 
                             if (Math.abs(var23) == 1)
                             {
                                 var51 = false;
                             }
                         }
                         else
                         {
                             var24 += var27;
 
                             if (Math.abs(var24) == 1)
                             {
                                 var51 = true;
                                 var27 *= -1;
                             }
                         }
 
                         if (var43 == 5)
                         {
                             var43 = 13;
                         }
                         else
                         {
                             var43 = 5;
                             ++var25;
                         }
 
                         if (var6 + var25 > var21 - 2)
                         {
                             break;
                         }
                     }
 
                     var10 = var18;
                     var11 = var40;
                     var48 = var1.nextInt(9) - 4;
                     var49 = var1.nextInt(9) - 4;
                     var58 = var0.getTopSolidOrLiquidBlock(var5 + var18 + var48, var7 + var40 + var49);
                     var0.setBlock(var5 + var18 + var48, var58, var7 + var40 + var49, Block.mobSpawner.blockID);
                     TileEntityMobSpawner var57 = (TileEntityMobSpawner)var0.getBlockTileEntity(var5 + var18 + var48, var58, var7 + var40 + var49);
 
                     if (var57 != null)
                     {
                         var57.getSpawnerLogic().setMobID(pickMobSpawner(var1));
                     }
                     else
                     {
                         System.err.println("Failed to fetch mob spawner entity at (" + (var5 + var18 + var48) + ", " + var58 + ", " + (var7 + var40 + var49) + ")");
                     }
                 }
 
                 break;
             }
         }
 
         ArrayList var38 = new ArrayList();
         int var10000 = var3 * var2;
         boolean var46;
         boolean var44;
         boolean var50;
 
         for (var40 = 0; var40 < var8 * 2; ++var40)
         {
             if (var38.size() < 0)
             {
                 var40 = 999999999;
             }
 
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = false;
 
             if (var0.getBlockMaterial(var5 + var41, var6 + 1, var7 + var21) == Material.air)
             {
                 for (var23 = -1; var23 <= 1; ++var23)
                 {
                     for (var24 = -1; var24 <= 1; ++var24)
                     {
                         if (var0.getBlockMaterial(var5 + var41 + var23, var6 + 1, var7 + var21 + var24) == Material.rock && Math.abs(var23) == 1 ^ Math.abs(var24) == 1)
                         {
                             var44 = true;
                             break;
                         }
                     }
 
                     if (var44)
                     {
                         break;
                     }
                 }
             }
 
             boolean var42 = var0.getBlockMaterial(var5 + var41 + 1, var6 + 1, var7 + var21) == Material.rock;
             var46 = var0.getBlockMaterial(var5 + var41 - 1, var6 + 1, var7 + var21) == Material.rock;
             var50 = var0.getBlockMaterial(var5 + var41, var6 + 1, var7 + var21 + 1) == Material.rock;
             var51 = var0.getBlockMaterial(var5 + var41, var6 + 1, var7 + var21 - 1) == Material.rock;
 
             if ((var42 && var46) ^ (var50 && var51))
             {
                 var44 = false;
             }
 
             if (var44)
             {
                 var0.setBlock(var5 + var41, var6 + 1, var7 + var21, Block.chest.blockID);
                 var38.add(new DunChest(var5 + var41, var6 + 1, var7 + var21));
             }
         }
 
         for (var40 = 0; (double)var40 < (double)var8 / 1.1D; ++var40)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = false;
 
             if (var0.getBlockMaterial(var5 + var41, var6 + 1, var7 + var21) == Material.air)
             {
                 for (var23 = -1; var23 <= 1; ++var23)
                 {
                     for (var24 = -1; var24 <= 1; ++var24)
                     {
                         if (var0.getBlockMaterial(var5 + var41 + var23, var6 + 2, var7 + var21 + var24) == Material.rock && Math.abs(var23) == 1 ^ Math.abs(var24) == 1)
                         {
                             var44 = true;
                             break;
                         }
                     }
 
                     if (var44)
                     {
                         break;
                     }
                 }
             }
 
             if (var44)
             {
                 --var13;
                 var0.setBlock(var5 + var41, var6 + 2, var7 + var21, modDimTorch.blockID);
             }
         }
 
         for (var40 = 0; var40 < var8 * 6; ++var40)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = false;
 
             if (var0.getBlockMaterial(var5 + var41, var6 + 2, var7 + var21) == Material.rock)
             {
                 var44 = true;
             }
 
             if (var44)
             {
                 var0.setBlock(var5 + var41, var6 + 2, var7 + var21, Block.fenceIron.blockID);
             }
         }
 
         label1349:
 
         for (var40 = 0; (double)var40 < (double)var8 * 0.2D; ++var40)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = true;
 
             for (var23 = -2; var23 < 3; ++var23)
             {
                 for (var24 = -2; var24 < 3; ++var24)
                 {
                     if (var0.getBlockId(var5 + var41 + var23, var6 + 1, var7 + var21 + var24) != 0)
                     {
                         var44 = false;
                         break;
                     }
                 }
 
                 if (!var44)
                 {
                     break;
                 }
             }
 
             if (var44)
             {
                 for (var23 = -1; var23 < 2; ++var23)
                 {
                     for (var24 = -1; var24 < 2; ++var24)
                     {
                         var0.setBlock(var5 + var41 + var23, var6, var7 + var21 + var24, Block.grass.blockID);
                     }
                 }
 
                 ArrayList var45 = new ArrayList();
                 var24 = var1.nextInt(5) + 3;
 
                 for (var25 = 0; var25 < var24; ++var25)
                 {
                     var26 = var1.nextInt(6);
 
                     if (var26 < 3)
                     {
                         var45.add(Integer.valueOf(Block.brewingStand.blockID));
                     }
                     else if (var26 < 5)
                     {
                         var45.add(Integer.valueOf(Block.cauldron.blockID));
                     }
                     else if (var26 == 5)
                     {
                         var45.add(Integer.valueOf(Block.chest.blockID));
                     }
                 }
 
                 ArrayList var53 = new ArrayList();
 
                 for (var26 = 0; var26 < var45.size(); ++var26)
                 {
                     var27 = ((Integer)var45.get(var26)).intValue();
                     var28 = var1.nextInt(3) - 1;
                     var48 = var1.nextInt(3) - 1;
 
                     if (var0.getBlockId(var5 + var41 + var28, var6 + 1, var7 + var21 + var48) == 0)
                     {
                         var0.setBlock(var5 + var41 + var28, var6 + 1, var7 + var21 + var48, var27);
 
                         if (var27 == Block.chest.blockID)
                         {
                             var53.add(new DunChest(var5 + var41 + var28, var6 + 1, var7 + var21 + var48));
                         }
 
                         if (var27 == Block.cauldron.blockID)
                         {
                             var0.setBlockMetadataWithNotify(var5 + var41 + var28, var6 + 1, var7 + var21 + var48, var1.nextInt(3) + 1, 3);
                         }
                     }
                 }
 
                 var26 = 0;
 
                 while (true)
                 {
                     if (var26 >= var53.size())
                     {
                         break label1349;
                     }
 
                     var27 = var1.nextInt(10) + 5;
 
                     for (var28 = 0; var28 < var27; ++var28)
                     {
                         var48 = var1.nextInt(potLoot.size());
                         var49 = ((DunChest)var53.get(var26)).x;
                         var58 = ((DunChest)var53.get(var26)).y;
                         int var55 = ((DunChest)var53.get(var26)).z;
                         TileEntityChest var63 = null;
 
                         if (var0.getBlockId(var49, var58, var55) == Block.chest.blockID)
                         {
                             var63 = (TileEntityChest)var0.getBlockTileEntity(var49, var58, var55);
                         }
 
                         if (var63 == null)
                         {
                             break;
                         }
 
                         for (int var34 = 0; var34 < 1; ++var34)
                         {
                             ItemStack var35 = ((DunLootItem)potLoot.get(var48)).getItemStack(var1);
 
                             if (var35 != null)
                             {
                                 var63.setInventorySlotContents(var1.nextInt(var63.getSizeInventory()), var35);
                             }
                         }
                     }
 
                     var28 = ((DunChest)var53.get(var26)).x;
                     var48 = ((DunChest)var53.get(var26)).y;
                     var49 = ((DunChest)var53.get(var26)).z;
                     TileEntityChest var56 = null;
 
                     if (var0.getBlockId(var28, var48, var49) == Block.chest.blockID)
                     {
                         var56 = (TileEntityChest)var0.getBlockTileEntity(var28, var48, var49);
                     }
 
                     if (var56 != null)
                     {
                         var56.setInventorySlotContents(var1.nextInt(var56.getSizeInventory()), new ItemStack(Item.glassBottle, var1.nextInt(7) + 2));
                     }
 
                     ++var26;
                 }
             }
         }
 
         for (var40 = 0; var40 < var8 / 5; ++var40)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = true;
             var23 = var1.nextInt(2);
             var24 = 0;
 
             for (var25 = -1; var25 < 3; ++var25)
             {
                 for (var26 = 1; var26 < 4; ++var26)
                 {
                     if (var23 == 1)
                     {
                         if (!isEmpty(var0, var5 + var41 + var25, var6 + var26, var7 + var21))
                         {
                             var44 = false;
                         }
                     }
                     else if (!isEmpty(var0, var5 + var41, var6 + var26, var7 + var21 + var25))
                     {
                         var44 = false;
                     }
 
                     if (!var44)
                     {
                         break;
                     }
                 }
 
                 if (!var44)
                 {
                     break;
                 }
             }
 
             if (var44)
             {
                 for (var25 = -1; var25 < 3; ++var25)
                 {
                     for (var26 = 0; var26 < 5; ++var26)
                     {
                         if ((var25 == -1 || var25 == 2 || var26 == 0 || var26 == 4) && var1.nextInt(6) != 1)
                         {
                             ++var24;
 
                             if (var23 == 1)
                             {
                                 var0.setBlock(var5 + var41 + var25, var6 + var26, var7 + var21, Block.obsidian.blockID);
                             }
                             else
                             {
                                 var0.setBlock(var5 + var41, var6 + var26, var7 + var21 + var25, Block.obsidian.blockID);
                             }
                         }
                     }
                 }
 
                 if (var24 == 14)
                 {
                     for (var25 = 0; var25 < 2; ++var25)
                     {
                         for (var26 = 1; var26 < 4; ++var26)
                         {
                             if (var23 == 1)
                             {
                                 var0.setBlock(var5 + var41 + var25, var6 + var26, var7 + var21, Block.portal.blockID);
                             }
                             else
                             {
                                 var0.setBlock(var5 + var41, var6 + var26, var7 + var21 + var25, Block.portal.blockID);
                             }
                         }
                     }
                 }
 
                 break;
             }
         }
 
         for (var40 = 0; var40 < var8; ++var40)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = true;
 
             for (var23 = -3; var23 < 4; ++var23)
             {
                 for (var24 = -3; var24 < 4; ++var24)
                 {
                     if (var0.getBlockId(var5 + var41 + var23, var6 + 1, var7 + var21 + var24) != 0)
                     {
                         var44 = false;
                         break;
                     }
                 }
 
                 if (!var44)
                 {
                     break;
                 }
             }
 
             if (var44)
             {
                 for (var23 = -2; var23 < 3; ++var23)
                 {
                     for (var24 = -2; var24 < 3; ++var24)
                     {
                         var0.setBlock(var5 + var41 + var23, var6, var7 + var21 + var24, Block.planks.blockID);
 
                         if ((Math.abs(var23) == 2 || Math.abs(var24) == 2) && var1.nextBoolean())
                         {
                             var25 = var1.nextInt(3);
 
                             for (var26 = 0; var26 < var25; ++var26)
                             {
                                 var0.setBlock(var5 + var41 + var23, var6 + 1 + var26, var7 + var21 + var24, Block.bookShelf.blockID);
                                 genVine(var0, var1, var5 + var41 + var23, var6 + 1 + var26, var7 + var21 + var24, var9);
                             }
                         }
                     }
                 }
 
                 var0.setBlock(var5 + var41, var6 + 1, var7 + var21, Block.enchantmentTable.blockID);
                 break;
             }
         }
 
         var40 = 0;
 
         while ((double)var40 < (double)var8 * 1.5D)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = true;
             var23 = -2;
 
             while (true)
             {
                 if (var23 < 3)
                 {
                     for (var24 = -2; var24 < 3; ++var24)
                     {
                         if (var0.getBlockMaterial(var5 + var41 + var23, var6 + 1, var7 + var21 + var24) == Material.rock)
                         {
                             var44 = false;
                             break;
                         }
                     }
 
                     if (var44)
                     {
                         ++var23;
                         continue;
                     }
                 }
 
                 if (var44)
                 {
                     for (var23 = 1; var23 < var4 - 1; ++var23)
                     {
                         genStone(var0, var1, var5 + var41, var6 + var23, var7 + var21);
                     }
 
                     for (var23 = -1; var23 < 2; ++var23)
                     {
                         for (var24 = -1; var24 < 2; ++var24)
                         {
                             if (var23 == 0 ^ var24 == 0 && var1.nextInt(4) == 0)
                             {
                                 var0.setBlock(var5 + var41 + var23, var6 + 2, var7 + var21 + var24, modDimTorch.blockID);
                             }
                         }
                     }
                 }
 
                 ++var40;
                 break;
             }
         }
 
         var40 = 0;
 
         while (var40 < var8)
         {
             var41 = var1.nextInt(var2 - 2) + 1;
             var21 = var1.nextInt(var3 - 2) + 1;
             var44 = true;
             var23 = -1;
 
             while (true)
             {
                 if (var23 <= 1)
                 {
                     for (var24 = -1; var24 <= 1; ++var24)
                     {
                         if (var0.getBlockMaterial(var5 + var41 + var23, var6 + 1, var7 + var21 + var24) == Material.rock)
                         {
                             var44 = false;
                             break;
                         }
                     }
 
                     if (var44)
                     {
                         ++var23;
                         continue;
                     }
                 }
 
                 if (var44)
                 {
                     var13 += 10;
                     var0.setBlock(var5 + var41, var6 + 1, var7 + var21, Block.mobSpawner.blockID);
                     TileEntityMobSpawner var59 = (TileEntityMobSpawner)var0.getBlockTileEntity(var5 + var41, var6 + 1, var7 + var21);              
 
                     if (var59 != null)
                     {
                         String var60 = pickMobSpawner(var1);
                         var59.getSpawnerLogic().setMobID(var60);
 
                         if (var60 == "Creeper")
                         {
                             var13 += 10;
                         }
                     }
                     else
                     {
                         System.err.println("Failed to fetch mob spawner entity at (" + (var5 + var41) + ", " + (var6 + 1) + ", " + (var7 + var21) + ")");
                     }
                 }
 
                 ++var40;
                 break;
             }
         }
 
         byte var47 = 2;
 
         if (var9 instanceof BiomeGenDesert)
         {
             var47 = 8;
         }
 
         if (var9 instanceof BiomeGenSwamp)
         {
             var47 = 1;
         }
 
         if (var9 instanceof BiomeGenOcean)
         {
             var47 = 4;
         }
 
         if (var9 instanceof BiomeGenTaiga)
         {
             var47 = 6;
         }
 
         if (var9 instanceof BiomeGenJungle)
         {
             var47 = 0;
         }
 
         var41 = var8 / 2;
 
         for (var21 = 0; var21 < var41 * var47; ++var21)
         {
             var22 = var1.nextInt(var2 - 2) + 1;
             var23 = var1.nextInt(var3 - 2) + 1;
             var24 = var1.nextInt(var4 - 2) + var6 + 1;
             var25 = 0;
 
             for (var26 = -1; var26 <= 2; ++var26)
             {
                 for (var27 = -1; var27 <= 2; ++var27)
                 {
                     for (var28 = -1; var28 <= 2; ++var28)
                     {
                         if (var0.getBlockMaterial(var5 + var22 + var26, var24 + var28, var7 + var23 + var27) == Material.rock)
                         {
                             ++var25;
                         }
                     }
                 }
             }
 
             if (var25 > 1 && var0.getBlockId(var5 + var22, var24, var7 + var23) == 0)
             {
                 var13 += 2;
                 var0.setBlock(var5 + var22, var24, var7 + var23, Block.web.blockID);
             }
         }
 
         var21 = 0;
 
         while (var21 < var8)
         {
             var22 = var1.nextInt(var2 - 2) + 1;
             var23 = var1.nextInt(var3 - 2) + 1;
             var46 = true;
             var25 = -7;
 
             while (true)
             {
                 if (var25 <= 8)
                 {
                     for (var26 = -5; var26 <= 6; ++var26)
                     {
                         if (var0.getBlockId(var5 + var22 + var25, var6 + 1, var7 + var23 + var26) == Block.mobSpawner.blockID)
                         {
                             var46 = false;
                             break;
                         }
                     }
 
                     if (var46)
                     {
                         ++var25;
                         continue;
                     }
                 }
 
                 if (var0.getBlockMaterial(var5 + var22, var6 + 2, var7 + var23) == Material.rock)
                 {
                     var46 = false;
                 }
 
                 if (var46)
                 {
                     var13 += 4;
 
                     if (!OidCompatibility)
                     {
                         var0.setBlock(var5 + var22, var6 + 1, var7 + var23, pressurePlatetest.blockID);
                     }
                     else
                     {
                         var0.setBlock(var5 + var22, var6 + 1, var7 + var23, Block.pressurePlateStone.blockID, -1, 3);
                     }
 
                     if (var1.nextInt(3) != 1)
                     {
                         var0.setBlock(var5 + var22, var6, var7 + var23, Block.gravel.blockID);
                     }
 
                     var0.setBlock(var5 + var22, var6 - 1, var7 + var23, Block.tnt.blockID);
                     var0.setBlock(var5 + var22, var6 - 2, var7 + var23, Block.stone.blockID);
                     var50 = true;
 
                     for (var26 = -1; var26 < 2; ++var26)
                     {
                         for (var27 = -1; var27 < 2; ++var27)
                         {
                             if (var0.getBlockMaterial(var5 + var22 + var26, var6 - 3, var7 + var23 + var27) == Material.air)
                             {
                                 var50 = false;
                             }
                         }
                     }
 
                     for (var26 = -2; var26 < 3; ++var26)
                     {
                         for (var27 = -2; var27 < 3; ++var27)
                         {
                             if ((var26 == -2 || var26 == 2 || var27 == -2 || var27 == 2) && var0.getBlockMaterial(var5 + var22 + var26, var6 - 2, var7 + var23 + var27) == Material.air)
                             {
                                 var50 = false;
                             }
                         }
                     }
 
                     if (var1.nextInt(5) == 1 && var50)
                     {
                         for (var26 = -1; var26 < 2; ++var26)
                         {
                             for (var27 = -1; var27 < 2; ++var27)
                             {
                                 var0.setBlock(var5 + var22 + var26, var6 - 3, var7 + var23 + var27, Block.lavaStill.blockID);
                             }
                         }
                     }
                 }
 
                 ++var21;
                 break;
             }
         }
 
         for (var21 = 0; var21 < 20; ++var21)
         {
             var22 = var1.nextInt(var2);
             var23 = var1.nextInt(var3);
             TileEntityDispenser var62;
 
             if (var1.nextBoolean() && var0.getBlockId(var5 + var22, var6 + 1, var7 + var23) == 0)
             {
                 var46 = false;
                 var50 = false;
                 var26 = 0;
                 var27 = 0;
 
                 for (var28 = 0; var28 < 200; ++var28)
                 {
                     if (var0.getBlockId(var5 + var22, var6 + 1, var7 + var23 + var28) != 0)
                     {
                         if (var0.getBlockMaterial(var5 + var22, var6 + 1, var7 + var23 + var28) == Material.rock)
                         {
                             var46 = true;
                             var26 = var28 - 1;
                         }
 
                         break;
                     }
                 }
 
                 if (var46)
                 {
                     for (var28 = 0; var28 < 200; ++var28)
                     {
                         if (var0.getBlockId(var5 + var22, var6 + 1, var7 + var23 - var28) != 0)
                         {
                             if (var0.getBlockMaterial(var5 + var22, var6 + 1, var7 + var23 - var28) == Material.rock)
                             {
                                 var50 = true;
                                 var27 = var28 - 1;
                             }
 
                             break;
                         }
                     }
                 }
 
                 //System.out.println(var26);
                 //System.out.println(var27);
 
                 if (var50 && var26 + var27 > 3)
                 {
                     for (var28 = 0; var28 < var26; ++var28)
                     {
                         var0.setBlock(var5 + var22, var6 + 1, var7 + var23 + var28, modTripWire.blockID);
                     }
 
                     for (var28 = 0; var28 < var27; ++var28)
                     {
                         var0.setBlock(var5 + var22, var6 + 1, var7 + var23 - var28, modTripWire.blockID);
                     }
 
                     var0.setBlock(var5 + var22, var6 + 1, var7 + var23 + var26, modTripWireSource.blockID, 2, 3);
                     var0.setBlock(var5 + var22, var6 + 1, var7 + var23 - var27, modTripWireSource.blockID);
 
                     if (var1.nextBoolean())
                     {
                         var0.setBlock(var5 + var22, var6 + 2, var7 + var23 - var27 - 1, Block.dispenser.blockID);
                         var0.setBlockMetadataWithNotify(var5 + var22, var6 + 2, var7 + var23 - var27 - 1, 3, 3);
                         var62 = null;
 
                         if (var0.getBlockId(var5 + var22, var6 + 2, var7 + var23 - var27 - 1) == Block.dispenser.blockID)
                         {
                             var62 = (TileEntityDispenser)var0.getBlockTileEntity(var5 + var22, var6 + 2, var7 + var23 - var27 - 1);
                         }
 
                         if (var62 == null)
                         {
                             break;
                         }
 
                         var48 = var1.nextInt(5) + 1;
 
                         for (var49 = 0; var49 < var48; ++var49)
                         {
                             var58 = var1.nextInt(disLoot.size());
 
                             if (var1.nextInt((int)((DunLootItem)disLoot.get(var58)).rareity) == 0)
                             {
                                 var62.setInventorySlotContents(var1.nextInt(var62.getSizeInventory()), ((DunLootItem)disLoot.get(var58)).getItemStack(var1));
                             }
                             else
                             {
                                 --var49;
                             }
                         }
                     }
                     else
                     {
                         var0.setBlock(var5 + var22, var6 + 2, var7 + var23 + var26 + 1, Block.dispenser.blockID);
                         var0.setBlockMetadataWithNotify(var5 + var22, var6 + 2, var7 + var23 + var26 + 1, 2, 3);
                         var62 = null;
 
                         if (var0.getBlockId(var5 + var22, var6 + 2, var7 + var23 + var26 + 1) == Block.dispenser.blockID)
                         {
                             var62 = (TileEntityDispenser)var0.getBlockTileEntity(var5 + var22, var6 + 2, var7 + var23 + var26 + 1);
                         }
 
                         if (var62 == null)
                         {
                             break;
                         }
 
                         var48 = var1.nextInt(5) + 1;
 
                         for (var49 = 0; var49 < var48; ++var49)
                         {
                             var58 = var1.nextInt(disLoot.size());
 
                             if (var1.nextInt((int)((DunLootItem)disLoot.get(var58)).rareity) == 0)
                             {
                                 var62.setInventorySlotContents(var1.nextInt(var62.getSizeInventory()), ((DunLootItem)disLoot.get(var58)).getItemStack(var1));
                             }
                             else
                             {
                                 --var49;
                             }
                         }
                     }
                 }
             }
             else if (var0.getBlockId(var5 + var22, var6 + 1, var7 + var23) == 0)
             {
                 var46 = false;
                 var50 = false;
                 var26 = 0;
                 var27 = 0;
 
                 for (var28 = 0; var28 < 200; ++var28)
                 {
                     if (var0.getBlockId(var5 + var22 + var28, var6 + 1, var7 + var23) != 0)
                     {
                         if (var0.getBlockMaterial(var5 + var22 + var28, var6 + 1, var7 + var23) == Material.rock)
                         {
                             var46 = true;
                             var26 = var28 - 1;
                         }
 
                         break;
                     }
                 }
 
                 if (var46)
                 {
                     for (var28 = 0; var28 < 200; ++var28)
                     {
                         if (var0.getBlockId(var5 + var22 - var28, var6 + 1, var7 + var23) != 0)
                         {
                             if (var0.getBlockMaterial(var5 + var22 - var28, var6 + 1, var7 + var23) == Material.rock)
                             {
                                 var50 = true;
                                 var27 = var28 - 1;
                             }
 
                             break;
                         }
                     }
                 }
 
                 //System.out.println(var26);
                 //System.out.println(var27);
 
                 if (var50 && var26 + var27 > 3)
                 {
                     for (var28 = 0; var28 < var26; ++var28)
                     {
                         var0.setBlock(var5 + var22 + var28, var6 + 1, var7 + var23, modTripWire.blockID);
                     }
 
                     for (var28 = 0; var28 < var27; ++var28)
                     {
                         var0.setBlock(var5 + var22 - var28, var6 + 1, var7 + var23, modTripWire.blockID);
                     }
 
                     var0.setBlock(var5 + var22 + var26, var6 + 1, var7 + var23, modTripWireSource.blockID, 1, 3);
                     var0.setBlock(var5 + var22 - var27, var6 + 1, var7 + var23, modTripWireSource.blockID, 3, 3);
 
                     if (var1.nextBoolean())
                     {
                         var0.setBlock(var5 + var22 - var27 - 1, var6 + 2, var7 + var23, Block.dispenser.blockID);
                         var0.setBlockMetadataWithNotify(var5 + var22 - var27 - 1, var6 + 2, var7 + var23, 5, 3);
                         var62 = null;
 
                         if (var0.getBlockId(var5 + var22 - var27 - 1, var6 + 2, var7 + var23) == Block.dispenser.blockID)
                         {
                             var62 = (TileEntityDispenser)var0.getBlockTileEntity(var5 + var22 - var27 - 1, var6 + 2, var7 + var23);
                         }
 
                         if (var62 == null)
                         {
                             break;
                         }
 
                         var48 = var1.nextInt(5) + 1;
 
                         for (var49 = 0; var49 < var48; ++var49)
                         {
                             var58 = var1.nextInt(disLoot.size());
 
                             if (var1.nextInt((int)((DunLootItem)disLoot.get(var58)).rareity) == 0)
                             {
                                 var62.setInventorySlotContents(var1.nextInt(var62.getSizeInventory()), ((DunLootItem)disLoot.get(var58)).getItemStack(var1));
                             }
                             else
                             {
                                 --var49;
                             }
                         }
                     }
                     else
                     {
                         var0.setBlock(var5 + var22 + var26 + 1, var6 + 2, var7 + var23, Block.dispenser.blockID);
                         var0.setBlockMetadataWithNotify(var5 + var22 + var26 + 1, var6 + 2, var7 + var23, 4, 3);
                         var62 = null;
 
                         if (var0.getBlockId(var5 + var22 + var26 + 1, var6 + 2, var7 + var23) == Block.dispenser.blockID)
                         {
                             var62 = (TileEntityDispenser)var0.getBlockTileEntity(var5 + var22 + var26 + 1, var6 + 2, var7 + var23);
                         }
 
                         if (var62 == null)
                         {
                             break;
                         }
 
                         var48 = var1.nextInt(5) + 1;
 
                         for (var49 = 0; var49 < var48; ++var49)
                         {
                             var58 = var1.nextInt(disLoot.size());
 
                             if (var1.nextInt((int)((DunLootItem)disLoot.get(var58)).rareity) == 0)
                             {
                                 var62.setInventorySlotContents(var1.nextInt(var62.getSizeInventory()), ((DunLootItem)disLoot.get(var58)).getItemStack(var1));
                             }
                             else
                             {
                                 --var49;
                             }
                         }
                     }
                 }
             }
         }
 
         double var54 = (double)var13;
         double var61 = var54 / 300.0D * 7.0D;
         var25 = (int)(var61 + 2.0D);
 
         if (var12 >= 60)
         {
             for (var26 = -5; var26 < 6; ++var26)
             {
                 for (var27 = -5; var27 < 6; ++var27)
                 {
                     if (var26 == -5 || var26 == 5 || var27 == -5 || var27 == 5)
                     {
                         for (var28 = var1.nextInt(var25) * -1; var28 < 100; ++var28)
                         {
                             if (var0.getBlockMaterial(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27) != Material.air && var0.getBlockMaterial(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27) != Material.water && var0.getBlockMaterial(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27) != Material.leaves && var0.getBlockMaterial(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27) != Material.wood && var0.getBlockId(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27) != Block.tallGrass.blockID && var0.getBlockMaterial(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27) != Material.snow)
                             {
                                 if (var28 < 0)
                                 {
                                     var0.setBlock(var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27, 0);
                                 }
 
                                 if (var28 == 0)
                                 {
                                     ;
                                 }
                             }
                             else
                             {
                                 genStone(var0, var1, var5 + var10 + var26, var12 + 1 - var28, var7 + var11 + var27);
                             }
 
                             if (var12 + 1 - var28 < var6 + var4)
                             {
                                 break;
                             }
                         }
                     }
                 }
             }
         }
 
         var26 = var13 * 2 * (var38.size() / 2);
         putLoot(var0, var1, var38, var13, var26);
         return true;
     }
 
     public static void genDun1(World var0, Random var1, int var2, int var3)
     {
         if (var1.nextInt(Orareity) == 0)
         {
             biome = var0.getWorldChunkManager().getBiomeGenAt(var2, var3);
             int var4 = var2;
             int var5 = var3;
             int var6 = var1.nextInt(45) + 10;
             int var7;
 
             for (var7 = 0; var7 < 10; ++var7)
             {
                 var6 = var0.getTopSolidOrLiquidBlock(var4, var5) - (var1.nextInt(30) + 20);             
 
                 if (var6 > 10 && var6 < 55)
                 {
                     break;
                 }
             }
 
             var7 = 1;
             int var8 = 1;
             int var9;
             int var10;
             int var11;
 
             for (var9 = 0; var9 < 100 && (var7 * var8 < OMinSquareBlocks || var7 < 10 || var8 < 10); ++var9)
             {
                 var10 = var1.nextInt(OmaxSize - OminSize) + OminSize + 1;
                 var11 = var1.nextInt(OmaxSize - OminSize) + OminSize + 1;
                 var7 = var1.nextInt(var10 - OminSize) + OminSize;
                 var8 = var1.nextInt(var11 - OminSize) + OminSize;
             }
 
             var10 = Oheight;
             var11 = var7 * var8 / (var1.nextInt(50) + 50);
 
             if (Odebug)
             {
                 var6 = 100;
             }
 
             vines = !(biome instanceof BiomeGenDesert);
 
             if (var9 < 100)
             {
                 doing = false;
                 makeDun1(var0, var1, var7, var8, var10, var4, var6, var5, var11, biome);
             }
         }
     }
     /**
      * 
      * @deprecated Basically unused for now ?
      */
 @Deprecated
     private static void genDun6(World var0, Random var1, int var2, int var3, BiomeGenBase var4)
     {
         if (var1.nextInt(100) == 0)
         {
             ;
         }
     }
 
     private static void putLoot(World var0, Random var1, ArrayList var2, int var3, int var4)
     {
         int var5 = 0;
         boolean var6 = false;
         boolean var7 = false;
         int var19;
 
         for (int var8 = 0; var8 < 1000 && var4 > 10; ++var8)
         {
             for (int var9 = 0; var9 < 1000 && var4 > 10 && var2.size() > 0; ++var9)
             {
                 if (var7)
                 {
                     ++var5;
                 }
 
                 var7 = false;
 
                 if (var5 >= var2.size())
                 {
                     var5 = 0;
                 }
 
                 int var10 = var1.nextInt(loot.size());
                 DunLootItem var11 = (DunLootItem)loot.get(var10);
                 int var12 = 1;
 
                 if (var11.maxStack - var11.minStack > 1)
                 {
                     var12 = var1.nextInt(var11.maxStack - var11.minStack) + var11.minStack;
                 }
 
                 for (var19 = 0; var19 < 100; ++var19)
                 {
                     var10 = var1.nextInt(loot.size());
                     var11 = (DunLootItem)loot.get(var10);
                     var12 = 1;
 
                     if (var11.maxStack - var11.minStack > 1)
                     {
                         var12 = var1.nextInt(var11.maxStack - var11.minStack) + var11.minStack;
                     }
 
                     if (var1.nextInt((int)((DunLootItem)loot.get(var10)).rareity) == 0)
                     {
                         break;
                     }
                 }
 
                 if (var19 != 100 && (double)var4 > ((DunLootItem)loot.get(var10)).value * (double)var12 && ((DunLootItem)loot.get(var10)).minDanger <= var3 && var2.size() > 0)
                 {
                     var4 = (int)((double)var4 - ((DunLootItem)loot.get(var10)).value * (double)var12);
                     var7 = true;
                     int var13 = ((DunChest)var2.get(var5)).x;
                     int var14 = ((DunChest)var2.get(var5)).y;
                     int var15 = ((DunChest)var2.get(var5)).z;
                     ((DunChest)var2.get(var5)).looted = true;
                     TileEntityChest var16 = null;
 
                     if (var0.getBlockId(var13, var14, var15) == Block.chest.blockID)
                     {
                         var16 = (TileEntityChest)var0.getBlockTileEntity(var13, var14, var15);
                     }
 
                     if (var16 == null)
                     {
                         break;
                     }
 
                     for (int var17 = 0; var17 < 1; ++var17)
                     {
                         ItemStack var18 = ((DunLootItem)loot.get(var10)).getItemStack(var1);
 
                         if (((DunLootItem)loot.get(var10)).enchatProb > 0 && var1.nextInt(((DunLootItem)loot.get(var10)).enchatProb) == 0)
                         {
                             EnchantmentHelper.addRandomEnchantment(var1, var18, ((DunLootItem)loot.get(var10)).maxEnchatLev);
                         }
 
                         if (var18 != null)
                         {
                             var16.setInventorySlotContents(var1.nextInt(var16.getSizeInventory()), var18);
                         }
                     }
                 }
             }
         }
 
         for (var19 = 0; var19 < var2.size(); ++var19)
         {
             if (!((DunChest)var2.get(var19)).looted)
             {
                 var0.setBlock(((DunChest)var2.get(var19)).x, ((DunChest)var2.get(var19)).y, ((DunChest)var2.get(var19)).z, 0);
             }
         }
     }
 
     private static void genStone(World var0, Random var1, int var2, int var3, int var4)
     {
         genStone(var0, var1, var2, var3, var4, false);
     }
 
     private static void genStone(World var0, Random var1, int var2, int var3, int var4, boolean var5)
     {
         int var8 = Block.stoneBrick.blockID;
         byte var9 = 0;
         boolean var10 = false;
 
         if (var1.nextInt(10) == 0 && var5)
         {
             var0.setBlock(var2, var3, var4, 0);
             genStone(var0, var1, var2, var3 + 1, var4);
         }
         else
         {
             while (!var10)
             {
                 int var6 = var1.nextInt(7);
                 int var7 = var1.nextInt(100);
 
                 if (var6 == 0 && var7 < 50)
                 {
                     var8 = Block.stoneBrick.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 1 && var7 < 25)
                 {
                     var8 = Block.stoneBrick.blockID;
                     var9 = 1;
                     var10 = true;
                 }
 
                 if (var6 == 2 && var7 < 25)
                 {
                     var8 = Block.stoneBrick.blockID;
                     var9 = 2;
                     var10 = true;
                 }
 
                 if (var6 == 3 && var7 < 10)
                 {
                     var8 = Block.cobblestoneMossy.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 4 && var7 < 6)
                 {
                     var8 = Block.cobblestone.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 5 && var7 < 3)
                 {
                     var8 = Block.stone.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 6 && var7 < 7)
                 {
                     var8 = Block.stoneBrick.blockID;
                     var9 = 3;
                     var10 = true;
                 }
             }
 
             var0.setBlock(var2, var3, var4, var8, var9, 3);
         }
 
         genVine(var0, var1, var2, var3, var4, biome);
     }
     /**
      * 
      * @deprecated Basically unused for now ?
      */
 @Deprecated
     private static void genStone2(World var0, Random var1, int var2, int var3, int var4, boolean var5)
     {
         int var8 = Block.stoneBrick.blockID;
         boolean var9 = false;
         boolean var10 = false;
 
         if (var1.nextInt(10) == 0 && var5)
         {
             var0.setBlock(var2, var3, var4, 0);
         }
         else
         {
             while (!var10)
             {
                 int var6 = var1.nextInt(5);
                 int var7 = var1.nextInt(100);
 
                 if (var6 == 0 && var7 < 100)
                 {
                     var8 = Block.netherBrick.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 1 && var7 < 50)
                 {
                     var8 = Block.netherFence.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 2 && var7 < 25)
                 {
                     var8 = Block.gravel.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 3 && var7 < 5)
                 {
                     var8 = Block.netherrack.blockID;
                     var10 = true;
                 }
 
                 if (var6 == 4 && var7 < 1)
                 {
                     var8 = 0;
                     var10 = true;
                 }
             }
 
             var0.setBlock(var2, var3, var4, var8);
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     private void genDun9(World var1, Random var2, int var3, int var4)
     {
         if (var2.nextInt(5) == 1)
         {
             int var5 = var3 + var2.nextInt(16);
             int var6 = var4 + var2.nextInt(16);
             int var7 = var2.nextInt(10) + 5;
             int var8 = this.getTopBlock(var1, var5, var6);
             int var9 = var2.nextInt(4) + 3;
             int var10 = var2.nextInt(7) + 3;
             int var11;
             int var12;
             int var13;
 
             for (var11 = 0; var11 < var7; ++var11)
             {
                 for (var12 = 0; var12 < var7; ++var12)
                 {
                     var13 = var1.getBlockId(var5 + var11, this.getTopBlock(var1, var5 + var11, var6 + var12), var6 + var12);
 
                     if (var13 == Block.waterStill.blockID)
                     {
                         System.out.println("NOT spawning tower at x:" + var5 + "z: " + var6 + "NOO!");
                         return;
                     }
 
                     if (this.getTopBlock(var1, var5 + var11, var6 + var12) > var8)
                     {
                         var8 = this.getTopBlock(var1, var5 + var11, var6 + var12);
                     }
                 }
             }
 
             System.out.println("spawning tower at x:" + var5 + "z: " + var6 + "WOO!");
             var8 += var2.nextInt(5);
 
             for (var11 = 0; var11 < var7; ++var11)
             {
                 for (var12 = 0; var12 < var7; ++var12)
                 {
                     for (var13 = var1.getTopSolidOrLiquidBlock(var5 + var11, var6 + var12); var13 < var8; ++var13)
                     {
                         var1.setBlock(var5 + var11, var13, var6 + var12, Block.cobblestoneMossy.blockID);
                     }
                 }
             }
 
             for (var11 = 0; var11 < var10; ++var11)
             {
                 for (var12 = 0; var12 < var9; ++var12)
                 {
                     for (var13 = 0; var13 < var7; ++var13)
                     {
                         for (int var14 = 0; var14 < var7; ++var14)
                         {
                             if (var13 == 0 || var13 == var7 - 1 || var14 == 0 || var14 == var7 - 1 || var12 == var9 - 1)
                             {
                                 var1.setBlock(var5 + var13, var8 + var11 * var9 + var12, var6 + var14, Block.stoneBrick.blockID);
                             }
                         }
                     }
                 }
 
                 if (var2.nextInt(1) == 0)
                 {
                     var12 = var2.nextInt(var7 - 3) + 2;
                     var13 = var2.nextInt(var7 - 3) + 2;
                     boolean var18 = false;
                     byte var17;
 
                     if (var2.nextInt(2) == 0)
                     {
                         if (var2.nextInt(2) == 0)
                         {
                             var12 = 1;
                             var17 = 5;
                         }
                         else
                         {
                             var12 = var7 - 2;
                             var17 = 4;
                         }
                     }
                     else if (var2.nextInt(2) == 0)
                     {
                         var13 = 1;
                         var17 = 3;
                     }
                     else
                     {
                         var13 = var7 - 2;
                         var17 = 2;
                     }
 
                     boolean var15 = var2.nextBoolean();
 
                     for (int var16 = 0; var16 < var9; ++var16)
                     {
                         if (var15 && var2.nextInt(2) != 1)
                         {
                             if (var15)
                             {
                                 var1.setBlock(var5 + var12, var8 + var11 * var9 + var16, var6 + var13, 0);
                             }
                         }
                         else
                         {
                             var1.setBlock(var5 + var12, var8 + var11 * var9 + var16, var6 + var13, Block.ladder.blockID, var17, 3);
                         }
                     }
                 }
             }
         }
     }
 /**
  * 
  * @deprecated Basically unused for now ?
  */
 @Deprecated
     public int getTopBlock(World var1, int var2, int var3)
     {
         ArrayList var4 = new ArrayList();
         var4.add(Integer.valueOf(0));
         var4.add(Integer.valueOf(Block.leaves.blockID));
         var4.add(Integer.valueOf(Block.wood.blockID));
         var4.add(Integer.valueOf(Block.tallGrass.blockID));
         var4.add(Integer.valueOf(Block.plantYellow.blockID));
         var4.add(Integer.valueOf(Block.plantRed.blockID));
         var4.add(Integer.valueOf(Block.snow.blockID));
         var4.add(Integer.valueOf(Block.stoneBrick.blockID));
 
         for (int var5 = 256; var5 > 0; --var5)
         {
             int var6 = 0;
 
             for (int var7 = 0; var7 < var4.size(); ++var7)
             {
                 if (var1.getBlockId(var2, var5, var3) == ((Integer)var4.get(var7)).intValue())
                 {
                     ++var6;
                 }
             }
 
             if (var6 == 0)
             {
                 return var5;
             }
         }
 
         return ((Integer)null).intValue();
     }
 
     public static void genVine(World var0, Random var1, int var2, int var3, int var4, BiomeGenBase var5)
     {
         byte var6 = 15;
 
         if (var5 instanceof BiomeGenDesert)
         {
             var6 = 50;
         }
 
         if (var5 instanceof BiomeGenSwamp)
         {
             var6 = 2;
         }
 
         if (var5 instanceof BiomeGenOcean)
         {
             var6 = 5;
         }
 
         if (var5 instanceof BiomeGenForest)
         {
             var6 = 10;
         }
 
         if (var5 instanceof BiomeGenJungle)
         {
             var6 = 2;
         }
 
         if (var1.nextInt(var6) == 0 && var0.getBlockId(var2 - 1, var3, var4) == 0)
         {
             func_35265_a(var0, var2 - 1, var3, var4, 8);
         }
 
         if (var1.nextInt(var6) == 0 && var0.getBlockId(var2 + 1, var3, var4) == 0)
         {
             func_35265_a(var0, var2 + 1, var3, var4, 2);
         }
 
         if (var1.nextInt(var6) == 0 && var0.getBlockId(var2, var3, var4 - 1) == 0)
         {
             func_35265_a(var0, var2, var3, var4 - 1, 1);
         }
 
         if (var1.nextInt(var6) == 0 && var0.getBlockId(var2, var3, var4 + 1) == 0)
         {
             func_35265_a(var0, var2, var3, var4 + 1, 4);
         }
 
         if (var1.nextInt(var6) == 0 && var0.getBlockId(var2, var3 - 1, var4) == 0)
         {
             func_35265_a(var0, var2, var3 - 1, var4 + 1);
         }
     }
 //Why the awful name ?
     private static void func_35265_a(World var0, int var1, int var2, int var3, int var4)
     {
         var0.setBlock(var1, var2, var3, Block.vine.blockID, var4, 3);
         Random var5 = new Random();
         int var6 = var5.nextInt(5) + 1;
 
         while (true)
         {
             --var2;
 
             if (var0.getBlockId(var1, var2, var3) != 0 || var6 <= 0)
             {
                 return;
             }
 
             var0.setBlock(var1, var2, var3, Block.vine.blockID, var4, 3);
             --var6;
         }
     }
   //Why the awful name ?
     private static void func_35265_a(World var0, int var1, int var2, int var3)
     {
         var0.setBlock(var1, var2, var3, Block.vine.blockID);
         Random var4 = new Random();
         int var5 = var4.nextInt(5) + 1;
 
         while (true)
         {
             --var2;
 
             if (var0.getBlockId(var1, var2, var3) != 0 || var5 <= 0)
             {
                 return;
             }
 
             var0.setBlock(var1, var2, var3, Block.vine.blockID);
             --var5;
         }
     }
     @Override
     public String getName()
     {
         return "NewDungeons";
     }
     @Override
     public String getVersion()
     {
     	return "1.6.2";
     }
 
     private static String pickMobSpawner(Random var0)
     {
         for (int var1 = 0; var1 < 5; ++var1)
         {
             int var2 = var0.nextInt(6);
 
             if (var2 == 0)
             {
                 return "Skeleton";
             }
 
             if (var2 == 1)
             {
                 return "Zombie";
             }
 
             if (var2 == 2)
             {
                 return "Zombie";
             }
 
             if (var2 == 3)
             {
                 return "Spider";
             }
 
             if (var2 == 5 && var0.nextInt(25) == 1)
             {
                 return "Creeper";
             }
         }
 
         return "";
     }
 
     public static boolean isEmpty(World var0, int var1, int var2, int var3)
     {
         int var4 = var0.getBlockId(var1, var2, var3);
         return var4 == 0 || var4 == Block.vine.blockID || var4 == Block.web.blockID || var4 == modDimTorch.blockID;
     }
 }
