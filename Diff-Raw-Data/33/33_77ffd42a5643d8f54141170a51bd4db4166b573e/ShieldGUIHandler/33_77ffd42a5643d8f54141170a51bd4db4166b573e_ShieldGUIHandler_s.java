 package BL2.client.render;
 
 import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
 import static org.lwjgl.opengl.GL11.GL_FLAT;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_SMOOTH;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glScalef;
 import static org.lwjgl.opengl.GL11.glShadeModel;
 
 import java.awt.Color;
 import java.util.EnumSet;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 
 import org.lwjgl.opengl.GL11;
 
 import BL2.BL2Core;
 import BL2.common.Reference;
 import BL2.common.item.ItemArmorShield;
 import BL2.common.item.ItemArmorShield.ShieldAtributes;
 import BL2.common.item.ItemGun;
 import BL2.common.item.ItemGun.GunAtributes;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class ShieldGUIHandler implements ITickHandler {
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
         // TODO Auto-generated method stub
         Minecraft minecraft = FMLClientHandler.instance().getClient();
         EntityPlayer player = minecraft.thePlayer;
         if (type.contains(TickType.RENDER)) {
             if (player != null) {
                 //
                 if (minecraft.inGameHasFocus && hasShield(player)) {
                     // System.out.println("works");
                     renderStoneHUD();
                 }
                 if (minecraft.inGameHasFocus && gunEquipped(player)) {
                     // System.out.println("works");
                     renderAmmoHUD();
                 }
                 if (player.isDead == false) {
                     // if (player.getCurrentEquippedItem().itemID ==
                     // BL2Core.guns.shiftedIndex ||
                     // player.getCurrentEquippedItem().itemID ==
                     // BL2Core.grenade.shiftedIndex){
 
                     // MinecraftForgeClient.registerItemRenderer(BL2Core.guns.itemID,
                     // new RenderGunInHand());
                     // MinecraftForgeClient.registerItemRenderer(BL2Core.grenade.itemID,
                     // new RenderGrenadeInHand());
 
                     // }
                 }
             }
         }
     }
 
     @Override
     public EnumSet<TickType> ticks() {
         // TODO Auto-generated method stub
         return EnumSet.of(TickType.CLIENT, TickType.RENDER);
     }
 
     @Override
     public String getLabel() {
         // TODO Auto-generated method stub
         return Reference.MOD_NAME + ": " + this.getClass().getSimpleName();
     }
 
     public boolean hasShield(EntityPlayer player) {
         ItemStack stack = null;
 
         for (int i = 0; i < 4; i++) {
             stack = player.inventory.armorItemInSlot(i);
 
             if (stack != null && stack.itemID == BL2Core.shield.itemID)
                 return true;
         }
         return false;
     }
     
     public boolean gunEquipped(EntityPlayer player) {
         ItemStack stack = null;
         stack = player.inventory.getCurrentItem();
     
         if (stack != null && stack.itemID == BL2Core.guns.itemID)
             return true;
         return false;
     }
 
     public static int calcShield(EntityPlayer player) {
         ItemStack stack = null;
 
         for (int i = 0; i < 4; i++) {
             stack = player.inventory.armorItemInSlot(i);
 
             if (stack != null) {
                 if (stack.itemID == BL2Core.shield.itemID) {
                     ItemArmorShield.ShieldAtributes str = new ItemArmorShield.ShieldAtributes(
                             stack);
                     return (int) ((float) str.charge / str.maxcharge * 80);
                 }
             }
         }
 
         return 0;
     }
     
     public static int calcAmmo(EntityPlayer player) {
         ItemStack stack = null;
         
         stack = player.inventory.getCurrentItem();
 
         if (stack != null) {
             if (stack.itemID == BL2Core.guns.itemID) {
                 ItemGun.GunAtributes atr = new GunAtributes(
                         stack);
                 if (atr.bulletsleft <= 1 && atr.reloadticker == 0)
                     return 1;
                 else if (atr.reloadticker > 0)
                     return (int) ((float)((atr.reloadtime - atr.reloadticker) / (float) atr.reloadtime)*80);
                 else if (atr.bulletsleft > 1)
                     return (int) ((float)(atr.bulletsleft - 1) / (float)(atr.clipsize - 1)*80);
                 return 0;
             }
         }
         return 0;
     }
 
     public static ShieldAtributes getStr(EntityPlayer player) {
         ItemStack stack = null;
 
         for (int i = 0; i < 4; i++) {
             stack = player.inventory.armorItemInSlot(i);
 
             if (stack != null) {
                 if (stack.itemID == BL2Core.shield.itemID) {
                     ItemArmorShield.ShieldAtributes str = new ItemArmorShield.ShieldAtributes(
                             stack);
                     return str;
                 }
             }
         }
         return null;
     }
     
    public static String AmmoString(GunAtributes atr){
         if(atr.reloadticker > 0){
             float currentReload = (float) atr.reloadticker/20;
             float roundedReload = (float) ((double) Math.round(currentReload * 100) / 100);
             String reload = ""+roundedReload;
             if(reload.length() == 3)
                 reload+="0";
            return reload;
         }
         return (atr.bulletsleft-1) + "/" + (atr.clipsize-1);
     }
     
     private static void renderAmmoHUD() {
         Minecraft mc = Minecraft.getMinecraft();
         ScaledResolution res = new ScaledResolution(mc.gameSettings,
                 mc.displayWidth, mc.displayHeight);
         int height = res.getScaledHeight();
         int width = res.getScaledWidth();
         mc.thePlayer.getHealth();
         float ammo = calcAmmo(mc.thePlayer);
         FontRenderer fr = mc.fontRenderer;
         mc.playerController.shouldDrawHUD();
         GunAtributes atr = new ItemGun.GunAtributes(mc.thePlayer.inventory.getCurrentItem());
         Minecraft minecraft = FMLClientHandler.instance().getClient();
         drawOutlinedBox(width / 2, height - 42, 80, 10, 0x6E7B8B);
         drawSolidGradientRectAmmo(width / 2 - 90, height - 42, (int) ammo, 10,
                 Color.ORANGE, Color.ORANGE);
         if (minecraft.inGameHasFocus) {
            drawAmmoText(AmmoString(atr), height, width, fr);
             // fr.drawStringWithShadow(ShieldString, (width / 2 -
             // fr.getStringWidth(ShieldString) / 2) - 40, height - 60,
             // 0xFFFFFF);
         }
     }
 
     private static void renderStoneHUD() {
         Minecraft mc = Minecraft.getMinecraft();
         ScaledResolution res = new ScaledResolution(mc.gameSettings,
                 mc.displayWidth, mc.displayHeight);
         int height = res.getScaledHeight();
         int width = res.getScaledWidth();
         mc.thePlayer.getHealth();
         float shield = calcShield(mc.thePlayer);
         FontRenderer fr = mc.fontRenderer;
         mc.playerController.shouldDrawHUD();
         ShieldAtributes str = getStr(mc.thePlayer);
         String ShieldString = str.charge + "/" + str.maxcharge;
         Minecraft minecraft = FMLClientHandler.instance().getClient();
         drawOutlinedBox(width / 2 - 90, height - 42, 80, 10, 0x6E7B8B);
         drawSolidGradientRect(width / 2 - 90, height - 42, (int) shield, 10,
                 Color.CYAN, Color.CYAN);
         if (minecraft.inGameHasFocus) {
             drawShieldText(ShieldString, height, width, fr);
             // fr.drawStringWithShadow(ShieldString, (width / 2 -
             // fr.getStringWidth(ShieldString) / 2) - 40, height - 60,
             // 0xFFFFFF);
         }
     }
 
     static float zLevel = -90.0F;
 
     public static void drawOutlinedBox(int x, int y, int width, int height,
             int outlineColorHex) {
         Color outlineColor = new Color(outlineColorHex);
         glPushMatrix();
         y -= 19;
         glScalef(0.5F, 0.5F, 0.5F);
         drawLeftSide(x * 2 - 1 - 2, y * 2 - 2, x * 2 + 2, (y + height) * 2 + 2,
                 outlineColor);
         drawSolidRect(x * 2 + 40, y * 2 - 2, (x + width) * 2 + 40, y * 2 + 2,
                 outlineColor);
         drawRightSide((x + width + 1) * 2 - 2, y * 2 - 1,
                 (x + width + 1) * 2 + 2, (y + height) * 2 + 2, outlineColor);
         drawSolidRect(x * 2 - 2, (y + height) * 2 - 2, (x + width) * 2 + 2,
                 (y + height) * 2 + 2, outlineColor);
         glPopMatrix();
     }
 
     public static void drawOutlinedBox2(int x, int y, int width, int height,
             Color color) {
         y -= 19;
         glPushMatrix();
         glScalef(0.5F, 0.5F, 0.5F);
         drawSolidRect(x * 2 - 2, y * 2 - 2, (x + width) * 2 + 2,
                 (y + height) * 2 + 2, color);
         drawTransparentRect(x * 2 - 1, y * 2 - 1, (x + width) * 2 + 1,
                 (y + height) * 2 + 1, color);
         glPopMatrix();
     }
 
     public static void drawSolidRect(int vertex1, int vertex2, int vertex3,
             int vertex4, Color color1) {
         glPushMatrix();
         Tessellator tess = Tessellator.instance;
         glDisable(GL_TEXTURE_2D);
         tess.startDrawingQuads();
         tess.setColorOpaque(color1.getRed(), color1.getGreen(),
                 color1.getBlue());
         tess.addVertex(vertex1, vertex4, zLevel);
         tess.addVertex(vertex3, vertex4, zLevel);
         tess.addVertex(vertex3, vertex2, zLevel);
         tess.addVertex(vertex1, vertex2, zLevel);
         tess.draw();
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
 
     public static void drawTransparentRect(int vertex1, int vertex2,
             int vertex3, int vertex4, Color color1) {
         glPushMatrix();
         Tessellator tess = Tessellator.instance;
         glDisable(GL_TEXTURE_2D);
         GL11.glEnable(GL11.GL_BLEND);
         color1 = Color.GREEN;
         tess.startDrawingQuads();
         tess.setColorOpaque(color1.getRed(), color1.getGreen(),
                 color1.getBlue());
         tess.addVertex(vertex1, vertex4, zLevel);
         tess.addVertex(vertex3, vertex4, zLevel);
         tess.addVertex(vertex3, vertex2, zLevel);
         tess.addVertex(vertex1, vertex2, zLevel);
         tess.draw();
         glEnable(GL_TEXTURE_2D);
         GL11.glEnable(GL11.GL_BLEND);
         glPopMatrix();
     }
 
     public static void drawLeftSide(int vertex1, int vertex2, int vertex3,
             int vertex4, Color color1) {
         glPushMatrix();
         Tessellator tess = Tessellator.instance;
         glDisable(GL_TEXTURE_2D);
         tess.startDrawingQuads();
         tess.setColorOpaque(color1.getRed(), color1.getGreen(),
                 color1.getBlue());
         // Bottom Left
         tess.addVertex(vertex1 - 5, vertex4, zLevel);
         // Bottom Right
         tess.addVertex(vertex3 - 2, vertex4, zLevel);
         // Top Right
         tess.addVertex(vertex3 + 40, vertex2 + 3, zLevel);
         // Top Left
         tess.addVertex(vertex1 + 43, vertex2, zLevel);
         tess.draw();
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
 
     public static void drawRightSide(int vertex1, int vertex2, int vertex3,
             int vertex4, Color color1) {
         glPushMatrix();
         Tessellator tess = Tessellator.instance;
         glDisable(GL_TEXTURE_2D);
         tess.startDrawingQuads();
         tess.setColorOpaque(color1.getRed(), color1.getGreen(),
                 color1.getBlue());
         // Bottom Left
         tess.addVertex(vertex1 - 5, vertex4, zLevel);
         // Bottom Right
         tess.addVertex(vertex3 - 2, vertex4, zLevel);
         // Top Right
         tess.addVertex(vertex3 + 44, vertex2 - 1, zLevel);
         // Top Left
         tess.addVertex(vertex1 + 41, vertex2 - 1, zLevel);
         tess.draw();
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
 
     public static void drawSolidGradientRectAmmo(int x, int y, int width,
             int height, Color color1Color, Color color2Color) {
         y -= 19;
         drawSolidGradientRectAmmo0(x * 2, y * 2, (x + width) * 2, (y + height) * 2,
                 color1Color, color2Color);
     }
 
     public static void drawSolidGradientRectAmmo0(int vertex1, int vertex2,
             int vertex3, int vertex4, Color color1Color, Color color2Color) {
         glPushMatrix();
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
         Color color3Color = new Color(0xffa900);
         Color color4Color = new Color(0xf6bb48);
         glScalef(0.5F, 0.5F, 0.5F);
         glDisable(GL_TEXTURE_2D);
         glDisable(GL_ALPHA_TEST);
         glShadeModel(GL_SMOOTH);
         Tessellator tess = Tessellator.instance;
         tess.startDrawingQuads();
         tess.setColorOpaque(color4Color.getRed(), color4Color.getGreen(),
                 color4Color.getBlue());
         tess.addVertex(vertex1 + 180, vertex4, zLevel);
         tess.addVertex(vertex3 + 180, vertex4, zLevel);
         tess.setColorOpaque(color3Color.getRed(), color3Color.getGreen(),
                 color3Color.getBlue());
         tess.addVertex(vertex3 + 220, vertex2, zLevel);
         tess.addVertex(vertex1 + 220, vertex2, zLevel);
         tess.draw();
         glShadeModel(GL_FLAT);
         glEnable(GL_ALPHA_TEST);
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
     
     public static void drawSolidGradientRect(int x, int y, int width,
             int height, Color color1Color, Color color2Color) {
         y -= 19;
         drawSolidGradientRect0(x * 2, y * 2, (x + width) * 2, (y + height) * 2,
                 color1Color, color2Color);
     }
 
     public static void drawSolidGradientRect0(int vertex1, int vertex2,
             int vertex3, int vertex4, Color color1Color, Color color2Color) {
         glPushMatrix();
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
         Color color3Color = new Color(0x00FFFF);
         Color color4Color = new Color(0x009ACD);
         glScalef(0.5F, 0.5F, 0.5F);
         glDisable(GL_TEXTURE_2D);
         glDisable(GL_ALPHA_TEST);
         glShadeModel(GL_SMOOTH);
         Tessellator tess = Tessellator.instance;
         tess.startDrawingQuads();
         tess.setColorOpaque(color4Color.getRed(), color4Color.getGreen(),
                 color4Color.getBlue());
         tess.addVertex(vertex1, vertex4, zLevel);
         tess.addVertex(vertex3, vertex4, zLevel);
         tess.setColorOpaque(color3Color.getRed(), color3Color.getGreen(),
                 color3Color.getBlue());
         tess.addVertex(vertex3 + 40, vertex2, zLevel);
         tess.addVertex(vertex1 + 40, vertex2, zLevel);
         tess.draw();
         glShadeModel(GL_FLAT);
         glEnable(GL_ALPHA_TEST);
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
 
     public static void drawShieldText(String ShieldString, int height,
             int width, FontRenderer fr) {
         glPushMatrix();
         fr.drawStringWithShadow(ShieldString,
                 width / 2 - fr.getStringWidth(ShieldString) / 2 - 40,
                 height - 60, 0xFFFFFF);
         glPopMatrix();
     }
     
     public static void drawAmmoText(String ShieldString, int height,
             int width, FontRenderer fr) {
         glPushMatrix();
         fr.drawStringWithShadow(ShieldString,
                 width / 2 - fr.getStringWidth(ShieldString) / 2 + 50,
                 height - 60, 0xFFFFFF);
         glPopMatrix();
     }
 }
