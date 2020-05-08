 package com.Pabi;
 
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.java.games.input.Keyboard;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 
 public class SuperArmor extends ItemArmor{ //implements IArmorTextureProvider{
 
 	private Object iconIndex;
 
 	public SuperArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial,
 			int par3, int par4) {
 		super(par1, par2EnumArmorMaterial, par3, par4);
 	}
 
 	//private static boolean hasJetpack = false;
 	/**
 	 * 	To Add Armor Texture to Player when worn.
 	 */
 	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
 		{
 			if(itemID == pabimodbase.helmetSuper.itemID || itemID == pabimodbase.chestplateSuper.itemID || itemID == pabimodbase.bootsSuper.itemID)
 		{
 				return "pabimodbase:textures/items/super_1.png";
 		}
 			if(itemID == pabimodbase.legsSuper.itemID)
 		{
 				return "pabimodbase:textures/items/super_2.png";
 		}
 
 		
 		else return null;
 	}
 	/**
 	* To Add Armor Texture to Item Armor (when held or in inventory)
 	*/
     public void registerIcons(IconRegister iconReg) 
     {
 		if(itemID == pabimodbase.bootsSuper.itemID)
 		{
 			itemIcon = iconReg.registerIcon("PabiModBase:bootsSuper");
 		}
 		else if(itemID == pabimodbase.legsSuper.itemID)
 		{
 			itemIcon = iconReg.registerIcon("PabiModBase:leggingsSuper");
 		}
 		else if(itemID == pabimodbase.chestplateSuper.itemID)
 		{
 			itemIcon = iconReg.registerIcon("PabiModBase:chestplateSuper");
 		}
 		else if(itemID == pabimodbase.helmetSuper.itemID)
 		{
 			itemIcon = iconReg.registerIcon("PabiModBase:helmetSuper");
 		}
 	}
     
     /**
 	* To add armor effect to armor. Doesn't work yet.
 	*/
     public boolean onTickInGame(float f, Minecraft minecraft, EntityPlayer par2)
     {
     if (par2.inventory.armorItemInSlot(2) != null) //Change two for the desired armor placement (see above)
         {
             ItemStack itemstack = par2.inventory.armorItemInSlot(2);
             if (itemstack.itemID == pabimodbase.chestplateSuper.itemID && par2.motionY < 0.0D)
             {
                 par2.motionY /= 0.2000000238418579D;
                 par2.fallDistance = 0.0F;
             }
         }
         return true;
     }
     
     //FOR JETPACK//
     
     /*public boolean onTickInGame(float f, Minecraft minecraft)
     {
     if (minecraft.thePlayer.inventory.armorItemInSlot(2) != null)
         {
             ItemStack itemstack = minecraft.thePlayer.inventory.armorItemInSlot(2);
            if (itemstack.itemID == PabiModBase.chestplateSuper.itemID)
             {
                 if (minecraft.currentScreen == null && (Keyboard.isKeyDown(Keyboard.KEY_Z))//Keyboard.isKeyDown(minecraft.gameSettings.keyBindJump.keyCode))
                 {
                     if (minecraft.thePlayer.motionY > 0.0D)
                     {
                         minecraft.thePlayer.motionY += 0.084999999105930327D;
                     }
                     else
                     {
                         minecraft.thePlayer.motionY += 0.11699999910593033D;
                     }
 
                     minecraft.theWorld.spawnParticle("smoke", minecraft.thePlayer.posX, minecraft.thePlayer.posY - 1.0D, minecraft.thePlayer.posZ, 0.0D, 0.0D, 0.0D);
                 }
 
                 if (minecraft.thePlayer.motionY < 0.0D)
                 {
                     minecraft.thePlayer.motionY /= 1.1499999761581421D;
                 }
 
                 if (!minecraft.thePlayer.onGround)
                 {
                     
                         minecraft.thePlayer.motionX *= 1.0399999618530273D;
                         minecraft.thePlayer.motionZ *= 1.0399999618530273D;
                     
                 }
 
                 minecraft.thePlayer.fallDistance = 0.0F;
                 hasJetpack = true;
             }
             else
             {
                 hasJetpack = false;
             }
         }
         return true;
     }
     
    /*public boolean onTickInGame(float f, Minecraft minecraft)
     {
     if (minecraft.thePlayer.inventory.armorItemInSlot(0) != null)
         {
          ItemStack itemstack = minecraft.thePlayer.inventory.armorItemInSlot(0);
            if (itemstack.itemID == PabiModBase.bootsSuper.itemID)
             {
              minecraft.thePlayer.speedOnGround = 0.41999998688697815F;
             }
         }    
     return true;
     }*/
 }
