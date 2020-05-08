 package nuclearcontrol;
 
 import forge.ITextureProvider;
 import ic2.api.ElectricItem;
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.Item;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.World;
 import net.minecraft.server.mod_IC2NuclearControl;
 
 public class ItemToolThermometer extends Item implements ITextureProvider
 {
     public ThermometerVersion thermometerVersion;
 
     public ItemToolThermometer(int var1, int var2, ThermometerVersion var3)
     {
         super(var1);
         this.d(var2);
         this.setMaxDurability(102);
         this.e(1);
         this.thermometerVersion = var3;
     }
 
     public boolean canTakeDamage(ItemStack var1, int var2)
     {
         return true;
     }
 
     public String getTextureFile()
     {
         return "/img/texture_thermo.png";
     }
 
     public boolean onItemUseFirst(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7)
     {
         if (!this.canTakeDamage(var1, 2))
         {
             return false;
         }
         else
         {
             TileEntity var8 = NuclearHelper.getReactorAt(var3, var4, var5, var6);
 
             if (var8 == null)
             {
                 TileEntity var9 = NuclearHelper.getReactorChamberAt(var3, var4, var5, var6);
 
                 if (var9 != null)
                 {
                     var8 = NuclearHelper.getReactorAroundCoord(var3, var4, var5, var6);
                 }
             }
 
             if (var8 != null)
             {
                 if (!var3.isStatic)
                 {
                     this.messagePlayer(var2, var8);
                     this.damage(var1, 1, var2);
                 }
 
                 return true;
             }
             else
             {
                 return false;
             }
         }
     }
 
     public void messagePlayer(EntityHuman var1, TileEntity var2)
     {
         int var3 = NuclearHelper.getReactorHeat(var2);
 
        switch (this.thermometerVersion.ordinal())
         {
             case 1:
                 mod_IC2NuclearControl.chatMessage(var1, "Hull heat: " + var3);
                 break;
 
             case 2:
                 int var4 = NuclearHelper.getMaxHeat(var2);
                 mod_IC2NuclearControl.chatMessage(var1, "Hull heat: " + var3 + " (Water evaporate: " + var4 * 50 / 100 + " / melting: " + var4 * 85 / 100 + ")");
         }
     }
 
     public void damage(ItemStack var1, int var2, EntityHuman var3)
     {
        switch (this.thermometerVersion.ordinal())
         {
             case 1:
                 var1.damage(10, var3);
                 break;
 
             case 2:
                 ElectricItem.use(var1, 50 * var2, var3);
         }
     }
 }
