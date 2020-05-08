 package immibis.core.covers;
 
 import forge.ITextureProvider;
 import immibis.core.NonSharedProxy;
 import net.minecraft.server.Item;
 import net.minecraft.server.ItemStack;
 
 public class ItemSaw extends Item implements ITextureProvider
 {
     protected ItemSaw(int var1)
     {
         super(var1);
         this.textureId = 0;
         this.maxStackSize = 1;
         this.a("immibis.core.saw");
         NonSharedProxy.AddLocalization("item.immibis.core.saw.name", "Hacksaw");
     }
 
     public String getTextureFile()
     {
         return "/immibis/core/items.png";
     }
 
     /**
      * True if this Item has a container item (a.k.a. crafting result)
      */
     public boolean k()
     {
         return true;
     }
 
     public Item j()
     {
         return this;
     }
 
     public boolean func_46059_i(ItemStack var1)
     {
         return false;
     }
 
     /**
      * If this returns true, after a recipe involving this item is crafted the container item will be added to the
      * player's inventory instead of remaining in the crafting grid.
      */
     public boolean e(ItemStack var1)
     {
         return false;
     }
 }
