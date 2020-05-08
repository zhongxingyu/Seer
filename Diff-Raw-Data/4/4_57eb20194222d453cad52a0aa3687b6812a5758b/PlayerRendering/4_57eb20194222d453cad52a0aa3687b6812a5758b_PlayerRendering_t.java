 /*
  * Copyright (c) 2013 monnef.
  */
 
 package monnef.dawn.client;
 
 import monnef.dawn.item.ArmorModelEnum;
 import monnef.dawn.item.IDawnArmor;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.RenderPlayerAPI;
 import net.minecraft.src.RenderPlayerBase;
 import org.lwjgl.opengl.GL11;
 
 public class PlayerRendering extends RenderPlayerBase {
     public static final float U = 1 / 16f;
     private final ModelHat hat;
 
     public PlayerRendering(RenderPlayerAPI var1) {
         super(var1);
         this.hat = new ModelHat();
     }
 
     public static float interpolateRotation(float par1, float par2, float par3) {
         float f3 = par2 - par1;
 
         while (f3 < -180.0F) f3 += 360.0F;
         while (f3 >= 180.0F) f3 -= 360.0F;
 
         return par1 + par3 * f3;
     }
 
     @Override
     public void renderPlayer(EntityPlayer var1, double x, double y, double z, float var8, float var9) {
         super.renderPlayer(var1, x, y, z, var8, var9);
 
         ItemStack headStack = var1.inventory.armorItemInSlot(3);
         if (headStack != null && headStack.getItem() instanceof IDawnArmor && ((IDawnArmor) headStack.getItem()).getArmorModel() == ArmorModelEnum.HAT) {
             float f2 = interpolateRotation(var1.prevRenderYawOffset, var1.renderYawOffset, U);
             float f3 = interpolateRotation(var1.prevRotationYawHead, var1.rotationYawHead, U);
 
             GL11.glPushMatrix();
             double yF = y - (double) var1.yOffset;
             GL11.glTranslatef((float) x, (float) yF, (float) z);
             GL11.glRotatef(180, 1, 0, 0);
             GL11.glTranslatef(0, -22 * U, 0);
 
            if (var1.isSneaking()) {
                GL11.glTranslatef(0, 1 * U, 0);
            }

             GL11.glRotatef(f3, 0, 1, 0); // ok
             GL11.glRotatef(var1.rotationPitch, 1, 0, 0);  // . . 1
             GL11.glTranslatef(0, -30 * U, 0); // 24 is exactly like the head
             this.renderPlayer.localLoadTexture("/hat01.png");
             hat.render(var1, 0, 0, 0, 0, 0, U);
 
             GL11.glPopMatrix();
         }
     }
 }
