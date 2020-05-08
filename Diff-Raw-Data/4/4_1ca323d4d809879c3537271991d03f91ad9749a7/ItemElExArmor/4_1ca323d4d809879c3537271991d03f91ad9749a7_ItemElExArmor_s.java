 package elex.item;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import elex.ElementalExperimentation;
 import elex.lib.ItemIds;
 import elex.lib.Reference;
 
 /**
  * Elemental Experimentation
  * 
  * ItemElExArmor
  * 
  * @author Myo-kun
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class ItemElExArmor extends ItemArmor {
     
     private EnumArmorMaterial material;
     private String armorType;
     private int armorSlot;
     
     @SideOnly(Side.CLIENT)
     private Icon[] icons;
     
    public ItemElExArmor(int id, EnumArmorMaterial material, int slot, int render, String type) {
        super(id - Reference.SHIFTED_ID_RANGE_CORRECTION, material, slot, render);
         setCreativeTab(ElementalExperimentation.elexTab);
         setMaxStackSize(1);
         
         this.material = material;
         this.armorType = type;
         this.armorSlot = slot;
     }
     
     @Override
     public String getUnlocalizedName(ItemStack stack) {
         return "item." + ItemIds.ELEX_ARMOR_REAL_UNLOCALIZED_NAMES[armorSlot];
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister register) {
         icons = new Icon[ItemIds.ELEX_ARMOR_REAL_UNLOCALIZED_NAMES.length];
         for (int i = 0; i < icons.length; i++) {
             icons[i] = register.registerIcon(Reference.MOD_ID + ":" + ItemIds.ELEX_ARMOR_REAL_UNLOCALIZED_NAMES[i]);
         }
     }
     
     @Override
     public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) {
         if (slot == 2) {
             return Reference.MOD_ID + ":" + armorType + "2";
         }
         return Reference.MOD_ID + ":" + armorType + "1";
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(ItemStack stack, int pass) {
         return icons[armorSlot];
     }
 
 }
