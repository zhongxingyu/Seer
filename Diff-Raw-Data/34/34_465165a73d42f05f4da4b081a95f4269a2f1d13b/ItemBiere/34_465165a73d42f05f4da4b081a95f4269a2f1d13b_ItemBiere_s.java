 package net.minecraft.server;
 
 import net.minecraft.server.CreativeModeTab;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.Item;
 import net.minecraft.server.ItemFood;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.MobEffectList;
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.World;
 
 public class ItemBiere extends Item
 {
    public ItemBiere(int var1)
     {
        super(var1);
        this.maxStackSize = 1;
         this.a(CreativeModeTab.h);
     }
 
    public ItemStack onEaten(ItemStack itemstack, World world, EntityHuman entityhuman)
     {
        if (!entityhuman.abilities.canFly)
         {
            --itemstack.b;
         }
 
         if (!world.isStatic)
         {
          entityhuman.b();
         	entityhuman.addEffect(new MobEffect(MobEffectList.BLINDNESS.id, 13, 0));
         	entityhuman.addEffect(new MobEffect(MobEffectList.CONFUSION.id, 500, 2));
         	entityhuman.addEffect(new MobEffect(MobEffectList.SLOWER_MOVEMENT.id, 300, 0));
         	entityhuman.addEffect(new MobEffect(MobEffectList.WITHER.id, 50, 0));
         }
 
        return --itemstack.count <= 0 ? new ItemStack(Item.biereVide) : itemstack;
     }
 
     /**
      * How long it takes to use or consume an item
      */
    public int getMaxItemUseDuration(ItemStack var1)
     {
         return 32;
     }
 
     /**
      * returns the action that specifies what animation to play when the items is being used
      */
    public EnumAnimation getItemUseAction(ItemStack var1)
     {
         return EnumAnimation.DRINK;
     }
 
     /**
      * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
      */
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityHuman entityhuman)
     {
        entityhuman.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
         return itemstack;
     }
 
 }
