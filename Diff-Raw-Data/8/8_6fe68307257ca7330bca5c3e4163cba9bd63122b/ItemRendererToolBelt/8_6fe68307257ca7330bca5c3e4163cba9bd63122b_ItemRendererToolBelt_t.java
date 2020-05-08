 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.client.renderers;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.ItemRenderer;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.texture.TextureManager;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import slimevoid.tmf.core.TMFCore;
 import slimevoid.tmf.core.helpers.ItemHelper;
 import cpw.mods.fml.client.FMLClientHandler;
 
 public class ItemRendererToolBelt implements IItemRenderer {
 
    private Minecraft                     mc;
    private int                           zLevel         = 0;
    private final static ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
 
     public ItemRendererToolBelt(Minecraft client) {
         this.mc = client;
     }
 
     public static void init() {
         MinecraftForgeClient.registerItemRenderer(TMFCore.miningToolBelt.itemID,
                                                   new ItemRendererToolBelt(FMLClientHandler.instance().getClient()));
     }
 
     private IItemRenderer getRendererForTool(ItemStack itemstack, ItemRenderType type) {
         return MinecraftForgeClient.getItemRenderer(itemstack,
                                                     type);
     }
 
     @Override
     public boolean handleRenderType(ItemStack item, ItemRenderType type) {
         ItemStack tool = ItemHelper.getSelectedTool(item);
         if (tool != null) {
             IItemRenderer renderer = this.getRendererForTool(tool,
                                                              type);
             if (renderer != null) {
                 return renderer.handleRenderType(tool,
                                                  type);
             }
         }
         if (type.equals(ItemRenderType.EQUIPPED)
             || type.equals(ItemRenderType.INVENTORY)
             || type.equals(ItemRenderType.EQUIPPED_FIRST_PERSON)) {
             return true;
         }
         return false;
     }
 
     @Override
     public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
         ItemStack tool = ItemHelper.getSelectedTool(item);
         if (tool != null) {
             IItemRenderer renderer = this.getRendererForTool(tool,
                                                              type);
             if (renderer != null) {
                 return renderer.shouldUseRenderHelper(type,
                                                       tool,
                                                       helper);
             }
         }
         return false;
     }
 
     @Override
     public void renderItem(ItemRenderType type, ItemStack itemstack, Object... data) {
         if (!renderToolBeltItem(type,
                                 itemstack,
                                 data)) {
             this.renderDefaultItem(type,
                                    itemstack,
                                    data);
         }
     }
 
     private boolean renderToolBeltItem(ItemRenderType type, ItemStack itemstack, Object... data) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null) {
             IItemRenderer renderer = this.getRendererForTool(tool,
                                                              type);
             if (renderer != null) {
                 renderer.renderItem(type,
                                     tool,
                                     data);
                 return true;
             }
         }
         return false;
     }
 
     private void renderDefaultItem(ItemRenderType type, ItemStack itemstack, Object... data) {
         if (type.equals(ItemRenderType.INVENTORY)) {
             doRenderInventoryItem(itemstack,
                                   (RenderBlocks) data[0]);
         } else if (type.equals(ItemRenderType.EQUIPPED_FIRST_PERSON)) {
             this.doRenderEquippedFirstPerson(itemstack,
                                              (RenderBlocks) data[0],
                                              (EntityLivingBase) data[1]);
         } else {
             doRenderEquippedItem(itemstack,
                                  (RenderBlocks) data[0],
                                  (EntityLivingBase) data[1],
                                  type);
         }
     }
 
     private void doRenderInventoryItem(ItemStack toolBelt, RenderBlocks renderBlocks) {
         ItemStack itemstack = toolBelt;
         ItemStack tool = ItemHelper.getSelectedTool(toolBelt);
         if (tool != null) {
             itemstack = tool;
         }
         this.renderInventoryItem(itemstack,
                                  renderBlocks);
     }
 
     private void renderInventoryItem(ItemStack itemstack, RenderBlocks renderBlocks) {
         TextureManager textureManager = this.mc.getTextureManager();
         RenderItem itemRenderer = null;
         if (RenderManager.instance.entityRenderMap.containsKey(EntityItem.class)) {
             itemRenderer = (RenderItem) RenderManager.instance.entityRenderMap.get(EntityItem.class);
         }
         if (itemRenderer != null) {
             itemRenderer.renderItemIntoGUI(this.mc.fontRenderer,
                                            textureManager,
                                            itemstack,
                                            0,
                                            0);
         }
     }
 
     private void doRenderEquippedFirstPerson(ItemStack toolBelt, RenderBlocks renderBlocks, EntityLivingBase entityLivingBase) {
         ItemStack itemstack = toolBelt;
         ItemStack tool = ItemHelper.getSelectedTool(toolBelt);
         if (tool != null) {
             itemstack = tool;
         }
         this.doRenderEquippedItem(itemstack,
                                   renderBlocks,
                                   entityLivingBase);
     }
 
     private void doRenderEquippedItem(ItemStack toolBelt, RenderBlocks renderBlocks, EntityLivingBase entityliving, ItemRenderType type) {
         ItemStack itemstack = toolBelt;
         ItemStack tool = ItemHelper.getSelectedTool(toolBelt);
         if (tool != null) {
             itemstack = tool;
         }
         this.doRenderEquippedItem(itemstack,
                                   renderBlocks,
                                   entityliving);
     }
 
     private void doRenderEquippedItem(ItemStack itemstack, RenderBlocks renderBlocks, EntityLivingBase entityliving) {
         TextureManager texturemanager = this.mc.getTextureManager();
         if (itemstack.getItem().requiresMultipleRenderPasses()) {
             this.renderItem(entityliving,
                             itemstack,
                             0,
                             texturemanager);
             for (int x = 1; x < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); x++) {
                 int i1 = Item.itemsList[itemstack.itemID].getColorFromItemStack(itemstack,
                                                                                 x);
                 float f11 = (float) (i1 >> 16 & 255) / 255.0F;
                 float f13 = (float) (i1 >> 8 & 255) / 255.0F;
                 float f14 = (float) (i1 & 255) / 255.0F;
                 GL11.glColor4f(1.0F * f11,
                                1.0F * f13,
                                1.0F * f14,
                                1.0F);
                 this.renderItem(entityliving,
                                 itemstack,
                                 x,
                                 texturemanager);
             }
         } else {
             this.renderItem(entityliving,
                             itemstack,
                             0,
                             texturemanager);
         }
     }
 
     private void renderItem(EntityLivingBase entityliving, ItemStack itemstack, int index, TextureManager texturemanager) {
         GL11.glPushMatrix();
         Icon icon = entityliving.getItemIcon(itemstack,
                                              index);
 
         if (icon == null) {
             GL11.glPopMatrix();
             return;
         }
 
         texturemanager.bindTexture(texturemanager.getResourceLocation(itemstack.getItemSpriteNumber()));
         Tessellator tessellator = Tessellator.instance;
         float f = icon.getMinU();
         float f1 = icon.getMaxU();
         float f2 = icon.getMinV();
         float f3 = icon.getMaxV();
         float f4 = 0.0F;
         float f5 = 0.3F;
         GL11.glEnable(GL12.GL_RESCALE_NORMAL);
         /*
          * GL11.glTranslatef( -f4, -f5, 0.0F); float f6 = 1.5F; GL11.glScalef(
          * f6, f6, f6); GL11.glRotatef( 50.0F, 0.0F, 1.0F, 0.0F);
          * GL11.glRotatef( 335.0F, 0.0F, 0.0F, 1.0F); GL11.glTranslatef(
          * -0.9375F, -0.0625F, 0.0F);
          */
         ItemRenderer.renderItemIn2D(tessellator,
                                     f1,
                                     f2,
                                     f,
                                     f3,
                                     icon.getIconWidth(),
                                     icon.getIconHeight(),
                                     0.0625F);
 
         if (itemstack.hasEffect(index)) {
             GL11.glDepthFunc(GL11.GL_EQUAL);
             GL11.glDisable(GL11.GL_LIGHTING);
            texturemanager.bindTexture(RES_ITEM_GLINT);
             GL11.glEnable(GL11.GL_BLEND);
             GL11.glBlendFunc(GL11.GL_SRC_COLOR,
                              GL11.GL_ONE);
             float f7 = 0.76F;
             GL11.glColor4f(0.5F * f7,
                            0.25F * f7,
                            0.8F * f7,
                            1.0F);
             GL11.glMatrixMode(GL11.GL_TEXTURE);
             GL11.glPushMatrix();
             float f8 = 0.125F;
             GL11.glScalef(f8,
                           f8,
                           f8);
             float f9 = Minecraft.getSystemTime() % 3000L / 3000.0F * 8.0F;
             GL11.glTranslatef(f9,
                               0.0F,
                               0.0F);
             GL11.glRotatef(-50.0F,
                            0.0F,
                            0.0F,
                            1.0F);
             ItemRenderer.renderItemIn2D(tessellator,
                                         0.0F,
                                         0.0F,
                                         1.0F,
                                         1.0F,
                                         256,
                                         256,
                                         0.0625F);
             GL11.glPopMatrix();
             GL11.glPushMatrix();
             GL11.glScalef(f8,
                           f8,
                           f8);
             f9 = Minecraft.getSystemTime() % 4873L / 4873.0F * 8.0F;
             GL11.glTranslatef(-f9,
                               0.0F,
                               0.0F);
             GL11.glRotatef(10.0F,
                            0.0F,
                            0.0F,
                            1.0F);
             ItemRenderer.renderItemIn2D(tessellator,
                                         0.0F,
                                         0.0F,
                                         1.0F,
                                         1.0F,
                                         256,
                                         256,
                                         0.0625F);
             GL11.glPopMatrix();
             GL11.glMatrixMode(GL11.GL_MODELVIEW);
             GL11.glDisable(GL11.GL_BLEND);
             GL11.glEnable(GL11.GL_LIGHTING);
             GL11.glDepthFunc(GL11.GL_LEQUAL);
         }
         GL11.glPopMatrix();
     }
 
 }
