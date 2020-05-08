 package net.medsouz.omg.item;
 
 import net.medsouz.omg.api.Gun;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemGun extends Item{
 
 	public Gun gun;
 	
 	public ItemGun(int par1, Gun g) {
 		super(par1);
 		setFull3D();
 		setCreativeTab(CreativeTabs.tabCombat);
 		gun = g;
 	}
 	
 	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack){
 		return true;
 	}
 	
 	public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player){
 		return true;
 	}
 	
 	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){
 		return true;
 	}
 	
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
 		par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 		return par1ItemStack;
 	}
	
	public int getMaxItemUseDuration(ItemStack par1ItemStack){
		return 72000;
	}
 
 }
