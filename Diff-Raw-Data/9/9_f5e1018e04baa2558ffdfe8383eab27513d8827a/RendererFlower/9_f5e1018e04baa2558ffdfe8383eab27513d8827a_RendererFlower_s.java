 package com.qzx.au.extras;
 
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.EntityRenderer;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.src.FMLRenderAccessLibrary;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 
 import com.qzx.au.core.RenderUtils;
 
 @SideOnly(Side.CLIENT)
 public class RendererFlower implements ISimpleBlockRenderingHandler {
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer){
 		// not used
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
 		Tessellator tessellator = Tessellator.instance;
 
 		tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
 
 		int colorMultiplier = block.colorMultiplier(world, x, y, z);
 		float r = (float)(colorMultiplier >> 16 & 255) / 255.0F;
 		float g = (float)(colorMultiplier >> 8 & 255) / 255.0F;
 		float b = (float)(colorMultiplier & 255) / 255.0F;
 		if(EntityRenderer.anaglyphEnable){
 			float rr = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
 			float gg = (r * 30.0F + g * 70.0F) / 100.0F;
 			float bb = (r * 30.0F + b * 70.0F) / 100.0F;
 			r = rr;
 			g = gg;
 			b = bb;
 		}
 		tessellator.setColorOpaque_F(1.0F * r, 1.0F * g, 1.0F * b);
 
 		Icon icon = block.getIcon(0, world.getBlockMetadata(x, y, z));
 		double minU = icon.getMinU();
 		double minV = icon.getMinV();
 		double maxU = icon.getMaxU();
 		double maxV = icon.getMaxV();
 
 		double x1 = (double)x + 0.05F;
 		double x2 = (double)x + 0.95F;
 		double y1 = (double)y + 0.0F;
 		double y2 = (double)y + 1.0F;
 		double z1 = (double)z + 0.05F;
 		double z2 = (double)z + 0.95F;
 
 		int below = world.getBlockId(x, y - 1, z);
		if(below == Block.slowSand.blockID || below == Block.tilledField.blockID){
 			y1 -= BlockFlowerSeed.y_offset;
 			y2 -= BlockFlowerSeed.y_offset;
 		}
 
 		// SW sides
 		tessellator.addVertexWithUV(x1, y2, z1, minU, minV);
 		tessellator.addVertexWithUV(x1, y1, z1, minU, maxV);
 		tessellator.addVertexWithUV(x2, y1, z2, maxU, maxV);
 		tessellator.addVertexWithUV(x2, y2, z2, maxU, minV);
 		// NE sides
 		tessellator.addVertexWithUV(x2, y2, z2, minU, minV);
 		tessellator.addVertexWithUV(x2, y1, z2, minU, maxV);
 		tessellator.addVertexWithUV(x1, y1, z1, maxU, maxV);
 		tessellator.addVertexWithUV(x1, y2, z1, maxU, minV);
 		// SE sides
 		tessellator.addVertexWithUV(x1, y2, z2, minU, minV);
 		tessellator.addVertexWithUV(x1, y1, z2, minU, maxV);
 		tessellator.addVertexWithUV(x2, y1, z1, maxU, maxV);
 		tessellator.addVertexWithUV(x2, y2, z1, maxU, minV);
 		// NW sides
 		tessellator.addVertexWithUV(x2, y2, z1, minU, minV);
 		tessellator.addVertexWithUV(x2, y1, z1, minU, maxV);
 		tessellator.addVertexWithUV(x1, y1, z2, maxU, maxV);
 		tessellator.addVertexWithUV(x1, y2, z2, maxU, minV);
 
 		return true;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory(){
 		return false;
 	}
 
 	@Override
 	public int getRenderId(){
 		return ClientProxy.flowerRenderType;
 	}
 }
