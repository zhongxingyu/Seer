 package net.minecraft.src;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.src.forge.*;
 
 public class TileEntityShelfRenderer extends TileEntitySpecialRenderer
 {
     private RenderBlocks blockrender;
     private static Method render;
 
     public TileEntityShelfRenderer()
     {
         blockrender = new RenderBlocks();
     }
 
     public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTick)
     {
         a((TileEntityShelf)tileentity, x, y, z, partialTick);
     }
 
     public void a(TileEntityShelf tileentityshelf, double x, double y, double z, float partialTick)
     {
         float f1 = 0.0F;
         if (mod_Shelf.RotateItems)
         {
             f1 = tileentityshelf.worldObj.getWorldTime() % 360L;
         }
         GL11.glPushMatrix();
         GL11.glTranslatef((float)x + 0.5F, (float)y + 0.8F, (float)z + 0.5F);
         int i = tileentityshelf.getBlockMetadata() & 3;
         switch (i)
         {
             case 0:
                 GL11.glRotatef(270F, 0.0F, 1.0F, 0.0F);
                 break;
 
             case 1:
                 GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
                 break;
 
             case 2:
                 GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
                 break;
 
             case 3:
                 GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
                 break;
         }
         GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
 
         for (int j = 0; j < tileentityshelf.getSizeInventory(); j++)
         {
             if (j == 0)
             {
                 GL11.glTranslatef(-0.3333333F, 0.0F, -0.3333333F);
             }
             else if (j % 3 != 0)
             {
                 GL11.glTranslatef(0.3333333F, 0.0F, 0.0F);
             }
             else
             {
                 GL11.glTranslatef(-0.6666667F, -0.3333333F, 0.3333333F);
             }
             ItemStack itemstack = tileentityshelf.getStackInSlot(j);
             System.out.println("item "+j+" is "+itemstack);
             if (itemstack != null && Item.itemsList[itemstack.itemID] != null)
             {
                if (itemstack.itemID < Block.blocksList.length && RenderBlocks.renderItemIn3d(Block.blocksList[itemstack.itemID].getRenderType()))
                 {
                     Block block = Block.blocksList[itemstack.itemID];
                     if (block instanceof ITextureProvider)
                     {
                         // Forge infinite sprite sheets
                         // see http://minecraftforge.net/wiki/How_to_use_infinite_terrain_and_sprite_indexes
                         bindTextureByName(((ITextureProvider)block).getTextureFile());
                     } 
                     else
                     {
                         bindTextureByName("/terrain.png");
                     }
 
                     float f2 = 0.25F;
                     int i1 = block.getRenderType();
                     if (i1 == 1 || i1 == 19 || i1 == 12 || i1 == 2)
                     {
                         f2 = 0.5F;
                     }
                     GL11.glPushMatrix();
                     if (mod_Shelf.RotateItems)
                     {
                         GL11.glRotatef(f1, 0.0F, 1.0F, 0.0F);
                     }
                     GL11.glScalef(f2, f2, f2);
                     GL11.glTranslatef(0.0F, 0.35F, 0.0F);
                     float f3 = 1.0F;
                     blockrender.renderBlockAsItem(Block.blocksList[itemstack.itemID], itemstack.getItemDamage(), f3);
                     GL11.glPopMatrix();
                 }
                 else
                 {
                     GL11.glPushMatrix();
                     try
                     {
                         GL11.glScalef(0.3333333F, 0.3333333F, 0.3333333F);
                         if (mod_Shelf.RotateItems)
                         {
                             GL11.glRotatef(f1, 0.0F, 1.0F, 0.0F);
                         }
                         if (mod_Shelf.Render3DItems)
                         {
                             GL11.glTranslatef(-0.5F, 0.0F, 0.0F);
                         }
 
 
                         Item item = Item.itemsList[itemstack.itemID];
 
                         if (itemstack.getItem().func_46058_c())
                         {
                             if (item instanceof ITextureProvider) {
                                 bindTextureByName(((ITextureProvider)item).getTextureFile());
                             } 
                             else
                             {
                                 bindTextureByName("/gui/items.png");
                             }
                             for (int k = 0; k <= 1; k++)
                             {
                                 int j1 = itemstack.getItem().func_46057_a(itemstack.getItemDamage(), k);
                                 int color = itemstack.getItem().getColorFromDamage(itemstack.getItemDamage(), k);
                                 float red = (float)(color >> 16 & 0xff) / 255F;
                                 float green = (float)(color >> 8 & 0xff) / 255F;
                                 float blue = (float)(color & 0xff) / 255F;
                                 GL11.glColor4f(red, green, blue, 1.0F);
                                 drawItem(j1);
                             }
                         }
                         else
                         {
                             int l = itemstack.getIconIndex();
                             if (item instanceof ITextureProvider) 
                             {
                                 bindTextureByName(((ITextureProvider)item).getTextureFile());
                             } 
                             else 
                             {
                                if (itemstack.itemID < Block.blocksList.length)
                                 {
                                     bindTextureByName("/terrain.png");
                                 }
                                 else
                                 {
                                     bindTextureByName("/gui/items.png");
                                 }
                             }
                             int color = Item.itemsList[itemstack.itemID].getColorFromDamage(itemstack.getItemDamage(), 0);
                             float red = (float)(color >> 16 & 0xff) / 255F;
                             float green = (float)(color >> 8 & 0xff) / 255F;
                             float blue = (float)(color & 0xff) / 255F;
                             GL11.glColor4f(red, green, blue, 1.0F);
                             drawItem(l);
                         }
                     }
                     catch (Throwable throwable)
                     {
                         throw new RuntimeException(throwable);
                     }
                     GL11.glPopMatrix();
                 }
             }
         }
 
         GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
         GL11.glPopMatrix();
     }
 
     private void drawItem(int tileIndex)
     throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
     {
         Tessellator tessellator = Tessellator.instance;
         float f = (float)((tileIndex % 16) * 16 + 0) / 256F;
         float f1 = (float)((tileIndex % 16) * 16 + 16) / 256F;
         float f2 = (float)((tileIndex / 16) * 16 + 0) / 256F;
         float f3 = (float)((tileIndex / 16) * 16 + 16) / 256F;
         float f4 = 1.0F;
         float f5 = 0.5F;
         float f6 = 0.25F;
         if (mod_Shelf.Render3DItems)
         {
             render.invoke(RenderManager.instance.itemRenderer, new Object[]
                     {
                         tessellator, Float.valueOf(f1), Float.valueOf(f2), Float.valueOf(f), Float.valueOf(f3)
                     });
         }
         else
         {
             tessellator.startDrawingQuads();
             tessellator.setNormal(0.0F, 1.0F, 0.0F);
             tessellator.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, f, f3);
             tessellator.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, f1, f3);
             tessellator.addVertexWithUV(f4 - f5, 1.0F - f6, 0.0D, f1, f2);
             tessellator.addVertexWithUV(0.0F - f5, 1.0F - f6, 0.0D, f, f2);
             tessellator.addVertexWithUV(0.0F - f5, 1.0F - f6, 0.0D, f, f2);
             tessellator.addVertexWithUV(f4 - f5, 1.0F - f6, 0.0D, f1, f2);
             tessellator.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, f1, f3);
             tessellator.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, f, f3);
             tessellator.draw();
         }
     }
 
     static
     {
         render = null;
         if (mod_Shelf.Render3DItems)
         {
             try
             {
                 render = (net.minecraft.src.ItemRenderer.class).getDeclaredMethod("a", new Class[]
                         {
                             net.minecraft.src.Tessellator.class, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE
                         });
                 render.setAccessible(true);
             }
             catch (Throwable throwable)
             {
                 throwable.printStackTrace();
             }
         }
     }
 }
