 package tcc.MotherOfPearl.armor;
 
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
 import tcc.MotherOfPearl.items.ModItems;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import darkevilmac.MotherOfPearl.Config.MOPConfiguration;
 import darkevilmac.MotherOfPearl.lib.ItemStacks;
 
 public class ModArmor {
     public static Item helmetPearl;
     public static Item chestplatePearl;
     public static Item leggingsPearl;
     public static Item bootsPearl;
     public static Item helmetBlackPearl;
     public static Item chestplateBlackPearl;
     public static Item leggingsBlackPearl;
     public static Item bootsBlackPearl;
     public static Item crownShell;
     public static Item braShell;
     public static Item skirtGrass;
     public static Item hempSandals;
 
    public static EnumArmorMaterial pearlMaterial = EnumHelper.addArmorMaterial("PearlArmor", 21, new int[] { 3, 9, 6, 3 }, 25);
     public static EnumArmorMaterial blackPearlMaterial = EnumHelper.addArmorMaterial("PearlArmor", 21, new int[] { 3, 9, 6, 3 }, 25);
     public static EnumArmorMaterial shellMaterial = EnumHelper.addArmorMaterial("ShellArmor", 11, new int[] { 1, 5, 3, 2 }, 8);
 
     public static void init() {
         defArmor();
         initGameRegistry();
         initLanguageRegistry();
     }
 
     public static void defArmor() {
         helmetPearl = new ItemPearlHelmet(MOPConfiguration.helmetPearlID, pearlMaterial, 0, 0, null);
         chestplatePearl = new ItemPearlChestplate(MOPConfiguration.chestplatePearlID, pearlMaterial, 0, 1, null);
         leggingsPearl = new ItemPearlLeggings(MOPConfiguration.leggingsPearlID, pearlMaterial, 0, 2, null);
         bootsPearl = new ItemPearlBoots(MOPConfiguration.bootsPearlID, pearlMaterial, 0, 3, null);
         helmetBlackPearl = new ItemBlackPearlHelmet(MOPConfiguration.helmetBlackPearlID, blackPearlMaterial, 0, 0, null);
         chestplateBlackPearl = new ItemBlackPearlChestplate(MOPConfiguration.chestplateBlackPearlID, blackPearlMaterial, 0, 1, null);
         leggingsBlackPearl = new ItemBlackPearlLeggings(MOPConfiguration.leggingsBlackPearlID, blackPearlMaterial, 0, 2, null);
         bootsBlackPearl = new ItemBlackPearlBoots(MOPConfiguration.bootsBlackPearlID, blackPearlMaterial, 0, 3, null);
         crownShell = new ItemShellCrown(MOPConfiguration.crownShellID, shellMaterial, 0, 0, null);
         braShell = new ItemShellBra(MOPConfiguration.braShellID, shellMaterial, 0, 1, null);
         skirtGrass = new ItemGrassSkirt(MOPConfiguration.skirtGrassID, shellMaterial, 0, 2, null);
         hempSandals = new ItemHempSandals(MOPConfiguration.hempSandalsID, shellMaterial, 0, 3, null);
     }
 
     public static void initGameRegistry() {
 
     }
 
     public static void initCrafting() {
         GameRegistry.addRecipe(new ItemStack(braShell, 1), "yxy", 'x', ItemStacks.string, 'y', ModItems.shell);
         GameRegistry.addRecipe(new ItemStack(skirtGrass, 1), "xxx", "ypy", "ggg", 'x', ItemStacks.string, 'y', ModItems.shell, 'p', ModItems.pearl, 'g', ItemStacks.wheat);
         GameRegistry.addRecipe(new ItemStack(crownShell, 1), "yyy", "yxy", 'x', ItemStacks.string, 'y', ModItems.shell);
         GameRegistry.addRecipe(new ItemStack(hempSandals, 1), "x x", "g g", 'x', ItemStacks.string, 'g', ItemStacks.wheat);
         GameRegistry.addRecipe(new ItemStack(chestplatePearl, 1), "x x", "yxy", "yyy", 'x', ModItems.pearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(chestplateBlackPearl, 1), "x x", "yxy", "yyy", 'x', ModItems.blackPearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(helmetPearl, 1), "xyx", "y y", 'x', ModItems.pearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(helmetBlackPearl, 1), "xyx", "y y", 'x', ModItems.blackPearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(leggingsPearl, 1), "yxy", "y y", "x x", 'x', ModItems.pearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(leggingsBlackPearl, 1), "yxy", "y y", "x x", 'x', ModItems.blackPearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(bootsPearl, 1), "x x", "y y", 'x', ModItems.pearl, 'y', ItemStacks.ingotIron);
         GameRegistry.addRecipe(new ItemStack(bootsBlackPearl, 1), "x x", "y y", 'x', ModItems.blackPearl, 'y', ItemStacks.ingotIron);
     }
 
     public static void initLanguageRegistry() {
         LanguageRegistry.addName(helmetPearl, "Pearly Helmet");
         LanguageRegistry.addName(chestplatePearl, "Pearly Chestplate");
         LanguageRegistry.addName(leggingsPearl, "Pearly leggings");
         LanguageRegistry.addName(bootsPearl, "Pearly Boots");
         LanguageRegistry.addName(helmetBlackPearl, "Black Pearly Helmet");
         LanguageRegistry.addName(chestplateBlackPearl, "Black Pearly Chestplate");
         LanguageRegistry.addName(leggingsBlackPearl, "Black Pearly Leggings");
         LanguageRegistry.addName(bootsBlackPearl, "Black Pearly Boots");
         LanguageRegistry.addName(crownShell, "Crown of Shells");
         LanguageRegistry.addName(braShell, "Shell Bra");
         LanguageRegistry.addName(skirtGrass, "Grass Skirt");
         LanguageRegistry.addName(hempSandals, "Hemp Sandals");
     }
 
 }
