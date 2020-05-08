 package com.mrgreaper.twisted.items;
 
 import com.mrgreaper.twisted.TwistedMod;
 import com.mrgreaper.twisted.client.sounds.Sounds;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class itemBunnyD extends Item {
 
 	public itemBunnyD(int id) {
 		super(id);
 		setCreativeTab(TwistedMod.tabTwisted);
 		setMaxStackSize(1);
 		setUnlocalizedName(ItemInfo.BUNNYD_UNLOCALIZED_NAME);
 		
 		// TODO Auto-generated constructor stub
 	}
 	@Override
 	public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
		int playerX = (int) Minecraft.getMinecraft().thePlayer.lastTickPosX;
		int playerY = (int) Minecraft.getMinecraft().thePlayer.lastTickPosY;
		int playerZ = (int) Minecraft.getMinecraft().thePlayer.lastTickPosZ;
 		System.out.println(playerX + " " + playerY + " " + playerZ);
		if (!player.worldObj.isRemote){
 			Sounds.EVIL_LAUGH.play (playerX ,playerY, playerZ, 35, 1);
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	//here is where we add the textures etc, has to be client side ofcourse
 	public void registerIcons(IconRegister register) {
 		itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BUNNYD_ICON);
 	}
 	
 	
 	
 	
 
 }
