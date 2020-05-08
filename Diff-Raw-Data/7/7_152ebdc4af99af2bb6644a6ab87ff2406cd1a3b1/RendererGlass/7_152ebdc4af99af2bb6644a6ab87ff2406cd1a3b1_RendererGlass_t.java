 package com.qzx.au.extras;
 
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.world.IBlockAccess;
 
 import org.lwjgl.opengl.GL11;
 
 import com.qzx.au.core.RenderUtils;
 
 @SideOnly(Side.CLIENT)
 public class RendererGlass implements ISimpleBlockRenderingHandler {
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer){
 		renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 
 		if(((BlockGlass)block).renderInPass1()){
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glEnable(GL11.GL_BLEND);
 			RenderUtils.renderInventoryBlock(block, renderer, block.getIcon(0, metadata));
 			GL11.glDisable(GL11.GL_BLEND);
 		}
 
 		// render untinted frame over the tinted glass
 		if(((BlockGlass)block).renderInPass0())
 			RenderUtils.renderInventoryBlock(block, renderer, AUExtras.blockGlass.getIcon(0, metadata));
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
 		if(ClientProxy.renderPass != block.getRenderBlockPass()) return false;
 
 		RenderUtils.initLighting(world, block, x, y, z);
 		if(RenderUtils.shouldSideBeRendered(block, world, x, y, z, 0))
 			RenderUtils.renderBottomFace(x, y, z, x+1.0F, y, z+1.0F, block.getBlockTexture(world, x, y, z, 0), 0.0F, 0.0F, 1.0F, 1.0F, false);
 		if(RenderUtils.shouldSideBeRendered(block, world, x, y, z, 1))
 			RenderUtils.renderTopFace(x, y+1.0F, z, x+1.0F, y+1.0F, z+1.0F, block.getBlockTexture(world, x, y, z, 1), 0.0F, 0.0F, 1.0F, 1.0F, false);
 		if(RenderUtils.shouldSideBeRendered(block, world, x, y, z, 2))
 			RenderUtils.renderSideFace(x+1.0F, y, z+0.0F, x+0.0F, y+1.0F, z+0.0F, block.getBlockTexture(world, x, y, z, 2), 0.0F, 0.0F, 1.0F, 1.0F);
 		if(RenderUtils.shouldSideBeRendered(block, world, x, y, z, 3))
 			RenderUtils.renderSideFace(x+0.0F, y, z+1.0F, x+1.0F, y+1.0F, z+1.0F, block.getBlockTexture(world, x, y, z, 3), 0.0F, 0.0F, 1.0F, 1.0F);
 		if(RenderUtils.shouldSideBeRendered(block, world, x, y, z, 4))
 			RenderUtils.renderSideFace(x+0.0F, y, z+0.0F, x+0.0F, y+1.0F, z+1.0F, block.getBlockTexture(world, x, y, z, 4), 0.0F, 0.0F, 1.0F, 1.0F);
 		if(RenderUtils.shouldSideBeRendered(block, world, x, y, z, 5))
 			RenderUtils.renderSideFace(x+1.0F, y, z+1.0F, x+1.0F, y+1.0F, z+0.0F, block.getBlockTexture(world, x, y, z, 5), 0.0F, 0.0F, 1.0F, 1.0F);
 
 		return true;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory(){
 		return true;
 	}
 
 	@Override
 	public int getRenderId(){
 		return ClientProxy.glassRenderType;
 	}
 }
