 
 package me.heldplayer.api.Smartestone.micro.rendering;
 
 import me.heldplayer.api.Smartestone.micro.MicroBlockInfo;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraftforge.client.event.DrawBlockHighlightEvent;
 import net.minecraftforge.common.ForgeDirection;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public final class MicroBlockRenderHelper {
 
     public static void renderMicroBlock(MicroBlockInfo info, DrawBlockHighlightEvent event) {
         GL11.glPushMatrix();
 
         int side = event.target.sideHit;
 
         ForgeDirection dir = ForgeDirection.getOrientation(side);
 
         GL11.glTranslatef(dir.offsetX, dir.offsetY, dir.offsetZ);
 
         RenderBlocks renderer = event.context.globalRenderBlocks;
 
         double x = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * (double) event.partialTicks;
         double y = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * (double) event.partialTicks;
         double z = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * (double) event.partialTicks;
 
        AxisAlignedBB aabb = info.getType().getBoundsInBlock(info).expand(-0.001D, -0.001D, -0.001D);
 
         GL11.glTranslated(-x, -y, -z);
 
         renderer.renderMinX = aabb.minX;
         renderer.renderMaxX = aabb.maxX;
         renderer.renderMinY = aabb.minY;
         renderer.renderMaxY = aabb.maxY;
         renderer.renderMinZ = aabb.minZ;
         renderer.renderMaxZ = aabb.maxZ;
 
         Tessellator tes = Tessellator.instance;
 
         tes.startDrawingQuads();
         tes.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
         renderer.renderFaceYNeg(null, event.target.blockX, event.target.blockY, event.target.blockZ, info.getMaterial().getIcon(0));
 
         tes.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
         renderer.renderFaceYPos(null, event.target.blockX, event.target.blockY, event.target.blockZ, info.getMaterial().getIcon(1));
 
         tes.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
         renderer.renderFaceZNeg(null, event.target.blockX, event.target.blockY, event.target.blockZ, info.getMaterial().getIcon(2));
 
         tes.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
         renderer.renderFaceZPos(null, event.target.blockX, event.target.blockY, event.target.blockZ, info.getMaterial().getIcon(3));
 
         tes.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
         renderer.renderFaceXNeg(null, event.target.blockX, event.target.blockY, event.target.blockZ, info.getMaterial().getIcon(4));
 
         tes.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
         renderer.renderFaceXPos(null, event.target.blockX, event.target.blockY, event.target.blockZ, info.getMaterial().getIcon(5));
         tes.draw();
 
         GL11.glPopMatrix();
     }
 
 }
