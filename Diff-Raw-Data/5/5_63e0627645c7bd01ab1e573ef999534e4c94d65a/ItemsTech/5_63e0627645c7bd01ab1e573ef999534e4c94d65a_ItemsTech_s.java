 package mcextreme.tech.item;
 
 import mcextreme.core.config.MCExtremeConfig;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 
 public class ItemsTech
 {
    public static Item itemIngot, itemScrewdriver, itemNanotube;
    public static Item itemIngot, itemScrewdriver, itemDebugScrewdriver;
     
     public static void initItems()
     {
         itemIngot = new ItemIngotsTech(MCExtremeConfig.itemTechIngotID, "INGOT").setUnlocalizedName("ingotMCExtreme");
         itemScrewdriver = new ItemScrewdriver(MCExtremeConfig.itemScrewdriverID).setUnlocalizedName("screwdriver");
         itemScrewdriver = new ItemScrewdriver(MCExtremeConfig.itemScrewdriverID, false).setUnlocalizedName("screwdriver");
         itemDebugScrewdriver = new ItemScrewdriver(MCExtremeConfig.itemDebugScrewdriverID, true).setUnlocalizedName("dbgscrewdriver");
     }
     
 }
