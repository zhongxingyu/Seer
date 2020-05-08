 package com.qzx.au.extras;
 
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 
 import net.minecraftforge.common.ForgeDirection;
 
 import org.lwjgl.opengl.GL11;
 
 import com.qzx.au.core.BlockCoord;
 import com.qzx.au.core.RenderUtils;
 
 @SideOnly(Side.CLIENT)
 public class RendererLamp implements ISimpleBlockRenderingHandler {
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer){
 		// render lamp block, on or off
 		renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 		RenderUtils.renderInventoryBlock(block, renderer, ((BlockLamp)block).getIcon(0, metadata));
 
 		if(((BlockLamp)block).isOn()){
 			// lamp is on, render its halo
 			final float size0 = 0.0F - BlockCoord.ADD_1_64*3;
 			final float size1 = 1.0F + BlockCoord.ADD_1_64*3;
 			renderer.setRenderBounds(size0, size0, size0, size1, size1, size1);
 
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glEnable(GL11.GL_BLEND);
 			RenderUtils.renderInventoryBlock(block, renderer, ((BlockLamp)block).getGlowIcon(metadata));
 			GL11.glDisable(GL11.GL_BLEND);
 		}
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
 		int color = world.getBlockMetadata(x, y, z);
 
 		if(ClientProxy.renderPass == 1){
 			if(!((BlockLamp)block).isOn()) return false;
 
 			// lamp is on, render its halo
 			Tessellator tessellator = Tessellator.instance;
 			tessellator.setBrightness(0xf000f0);
 			tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
 
 			Icon icon = ((BlockLamp)block).getGlowIcon(color);
 			BlockCoord coord = new BlockCoord(world, x, y, z);
 			boolean renderYNeg = this.shouldExteriorSideBeRendered(coord, 0);
 			boolean renderYPos = this.shouldExteriorSideBeRendered(coord, 1);
 			boolean renderZNeg = this.shouldExteriorSideBeRendered(coord, 2);
 			boolean renderZPos = this.shouldExteriorSideBeRendered(coord, 3);
 			boolean renderXNeg = this.shouldExteriorSideBeRendered(coord, 4);
 			boolean renderXPos = this.shouldExteriorSideBeRendered(coord, 5);
 
 			final float size0 = 0.0F - BlockCoord.ADD_1_64*3;
 			final float size1 = 1.0F + BlockCoord.ADD_1_64*3;
 			renderer.setRenderBounds(renderXNeg?size0:0.0F, renderYNeg?size0:0.0F, renderZNeg?size0:0.0F, renderXPos?size1:1.0F, renderYPos?size1:1.0F, renderZPos?size1:1.0F);
 
 			if(renderYNeg) renderer.renderFaceYNeg(block, (double)x, (double)y, (double)z, icon);
 			if(renderYPos) renderer.renderFaceYPos(block, (double)x, (double)y, (double)z, icon);
 			if(renderZNeg) renderer.renderFaceZNeg(block, (double)x, (double)y, (double)z, icon);
 			if(renderZPos) renderer.renderFaceZPos(block, (double)x, (double)y, (double)z, icon);
 			if(renderXNeg) renderer.renderFaceXNeg(block, (double)x, (double)y, (double)z, icon);
 			if(renderXPos) renderer.renderFaceXPos(block, (double)x, (double)y, (double)z, icon);
 
 			return true;
 		}
 
 		renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 		if(((BlockLamp)block).isOn()){
 			// lamp is on, render as full bright block
 			Tessellator tessellator = Tessellator.instance;
 			tessellator.setBrightness(0xf000f0);
 			tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
 
 			Icon icon = block.getIcon(0, color);
 			BlockCoord coord = new BlockCoord(world, x, y, z);
 
 			if(this.shouldInteriorSideBeRendered(block, coord, 0)) renderer.renderFaceYNeg(block, (double)x, (double)y, (double)z, icon);
 			if(this.shouldInteriorSideBeRendered(block, coord, 1)) renderer.renderFaceYPos(block, (double)x, (double)y, (double)z, icon);
 			if(this.shouldInteriorSideBeRendered(block, coord, 2)) renderer.renderFaceZNeg(block, (double)x, (double)y, (double)z, icon);
 			if(this.shouldInteriorSideBeRendered(block, coord, 3)) renderer.renderFaceZPos(block, (double)x, (double)y, (double)z, icon);
 			if(this.shouldInteriorSideBeRendered(block, coord, 4)) renderer.renderFaceXNeg(block, (double)x, (double)y, (double)z, icon);
 			if(this.shouldInteriorSideBeRendered(block, coord, 5)) renderer.renderFaceXPos(block, (double)x, (double)y, (double)z, icon);
 		} else
 			// lamp is off, render as normal block
 			renderer.renderStandardBlock(block, x, y, z);
 
 		return true;
 	}
 
 	private boolean shouldInteriorSideBeRendered(Block block, BlockCoord coord, int side){
 		BlockCoord neighbor = (new BlockCoord(coord)).translateToSide(side);
 		return block.shouldSideBeRendered(coord.access, neighbor.x, neighbor.y, neighbor.z, ForgeDirection.OPPOSITES[side]);
 	}
 	private boolean shouldExteriorSideBeRendered(BlockCoord coord, int side){
 		// don't render halo on sides with other lit lamps
 		Block neighbor = (new BlockCoord(coord)).translateToSide(side).getBlock();
 		if(neighbor instanceof BlockLamp)
 			return !((BlockLamp)neighbor).isOn();
 		return true;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory(){
 		return true;
 	}
 
 	@Override
 	public int getRenderId(){
 		return ClientProxy.lampRenderType;
 	}
 }
