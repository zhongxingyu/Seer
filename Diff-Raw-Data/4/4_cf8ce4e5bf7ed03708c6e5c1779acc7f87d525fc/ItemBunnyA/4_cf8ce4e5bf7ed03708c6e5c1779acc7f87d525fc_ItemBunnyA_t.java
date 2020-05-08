 package com.mrgreaper.twisted.items;
 
 import java.util.Random;
 
 import com.mrgreaper.twisted.TwistedMod;
 import com.mrgreaper.twisted.client.sounds.Sounds;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemBunnyA extends Item {
 
 	public ItemBunnyA(int id) {
 		super(id);
 		setCreativeTab(TwistedMod.tabTwisted);
 		setMaxStackSize(1);
 		setUnlocalizedName(ItemInfo.BUNNYA_UNLOCALIZED_NAME);
 	}
 	
 	@Override
 	public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
 		//int playerX = (int) Minecraft.getMinecraft().thePlayer.lastTickPosX;
 		//int playerY = (int) Minecraft.getMinecraft().thePlayer.lastTickPosY;
 		//int playerZ = (int) Minecraft.getMinecraft().thePlayer.lastTickPosZ;
 		int playerX = (int) player.prevPosX;
 		int playerY = (int) player.prevPosY;
 		int playerZ = (int) player.prevPosZ;
 		
 		System.out.println(playerX + " " + playerY + " " + playerZ);
		if (player.worldObj.isRemote){
 			Random randomGenerator = new Random(); //test of random
 			int randomInt = randomGenerator.nextInt(3);
 			System.out.println("the random number was "+randomInt + "the entity was " + player + " theplayer :" + Minecraft.getMinecraft().thePlayer);
 			if (randomInt == 0){
 			Sounds.CREATED_MONSTER.play (playerX ,playerY, playerZ, 150, 1);
 			}
 			if (randomInt == 1){
 				Sounds.BUNNY_RELEASE.play (playerX ,playerY, playerZ, 150, 1);
 				}
 			if (randomInt == 2){
 				Sounds.BUNNY_LITTLE.play (playerX ,playerY, playerZ, 150, 1);
 				}
 			Minecraft.getMinecraft().thePlayer.addChatMessage("Dont let The evil vile creature go free!");
 		}
 	}
 	
     public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
     {
     	Random randomGenerator = new Random();
     	int randomInt = randomGenerator.nextInt(30);
     	if (randomInt == 0){
     		itemstack.stackSize --;
     		entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.bunnye,1));
     	}else{
     		int randomIntB = randomGenerator.nextInt(9);
         	if (randomIntB == 0 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("You failed to shake the bunny enough to energize it");
         	}
         	if (randomIntB == 1 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("The bunny just stares back at you");
         	}	
         	if (randomIntB == 2 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("The bunny blows you a rasperry");
         	}
         	if (randomIntB == 3 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("You think thats enough to energize the bunny...shake failure");
         	}
         	if (randomIntB == 4 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("The bunny appears to be mocking you");
         	}
         	if (randomIntB == 5 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("The bunny does a little impression of you, shaking his paw then sniggers");
         	}
         	if (randomIntB == 6 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("The bunny looks puzzled for a second then throws up");
         	}
         	if (randomIntB == 7 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("The bunny seems to twitch its nose in threatening manner");
         	}
         	if (randomIntB == 8 && entityplayer.worldObj.isRemote){
         		Minecraft.getMinecraft().thePlayer.addChatMessage("the bunny keels over and dies");
         	}
         	if (randomIntB == 8){
         		itemstack.stackSize --;
         		entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.bunnyd,1));
         	}
     	}
 	 
     	return itemstack;
     }
 	@Override
 	@SideOnly(Side.CLIENT)
 	//here is where we add the textures etc, has to be client side ofcourse
 	public void registerIcons(IconRegister register) {
 		itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BUNNYA_ICON);
 		
 	}
 
 	
 	
 
 }
