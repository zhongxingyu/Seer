 package steamcraft.items;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import steamcraft.Steamcraft;
 
 public class ItemTeacup extends ItemFood
 {
     public ItemTeacup(int i, int j)
     {
     	super(i,j,0.0F,false);
     	setMaxStackSize(1);
     }
     
     @Override
    protected void onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
     {
        super.onFoodEaten(par1ItemStack, par2World, par3EntityPlayer);
        par1ItemStack = new ItemStack(Steamcraft.emptyTeacup, 1);
     }
 }
