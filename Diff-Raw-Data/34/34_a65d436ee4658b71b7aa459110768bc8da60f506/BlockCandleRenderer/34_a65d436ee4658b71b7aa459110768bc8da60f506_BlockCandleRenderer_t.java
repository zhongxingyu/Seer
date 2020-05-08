 package mods.nordwest.client.renders;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import java.awt.Color;
 import java.util.Random;
 import org.lwjgl.opengl.GL11;
 import net.minecraft.world.IBlockAccess;
 
 public class BlockCandleRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
 	public static final int[] colors =
 	//{ 15790320, 15435844, 12801229, 6719955,  14602026, 4312372,  14188952, 4408131,  10526880, 2651799,  8073150,  2437522,  5320730,  3887386,  11743532, 1973019 };
 	{ 0xf0f0f0, 0xeb8844, 0xc354cd, 0x6689d3, 0xdecf2a, 0x41cd34, 0xd88198, 0x434343, 0xa0a0a0, 0x287697, 0x7b2fbe, 0x253192, 0x51301a, 0x3b511a, 0xb3312c, 0x1e1b1b };
 
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
 		Color color = new Color(colors[metadata]);
 		float r = color.getRed() / 255.0F;
 		float g = color.getGreen() / 255.0F;
 		float b = color.getBlue() / 255.0F;
 		GL11.glColor3f(r, g, b);
 		block.setBlockBounds(W6, 0.0F, W6, W10, 0.5F, W10);
 		renderer.setRenderBoundsFromBlock(block);
 		drawFaces(renderer, block, 67, true);
 		GL11.glColor3f(1.0F, 1.0F, 1.0F);
 
 		block.setBlockBounds(0.475F, 0.5F, 0.475F, 0.525F, W10, 0.525F);
 		renderer.setRenderBoundsFromBlock(block);
 		drawFaces(renderer, block, 66, true);
 	}
 
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
 		int type = 0;
 
 		block.setBlockBounds(W6, 0.0F, W6, W10, 0.5F, W10);
 		renderer.setRenderBoundsFromBlock(block);
 		renderer.renderStandardBlock(block, x, y, z);
 
 		Random rr = new Random(x + y * z);
 		int q = 1 + rr.nextInt(5);
 		for (int a = 0; a < q; a++) {
 			boolean side = rr.nextBoolean();
 			int loc = 2 + rr.nextInt(2);
 			if (a % 2 == 0) {
 				block.setBlockBounds(W5 + W1 * loc, 0.0F - W1 * rr.nextInt(2), side ? W5 : W10, W6 + W1 * loc, W1 * (1 + rr.nextInt(3)), side ? W6 : W11);
 				renderer.setRenderBoundsFromBlock(block);
 				renderer.renderStandardBlock(block, x, y, z);
 			} else {
 				block.setBlockBounds(side ? W5 : W10, 0.0F - W1 * rr.nextInt(2), W5 + W1 * loc, side ? W6 : W11, W1 * (1 + rr.nextInt(3)), W6 + W1 * loc);
 				renderer.setRenderBoundsFromBlock(block);
 				renderer.renderStandardBlock(block, x, y, z);
 			}
 		}
 
 		block.setBlockBounds(0.475F, 0.5F, 0.475F, 0.525F, W10, 0.525F);
 		renderer.setRenderBoundsFromBlock(block);
 		renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 0.8F, 0.8F, 0.8F);;
 
 	
 		
		block.setBlockBounds(W6, 0.0F, W6, W10, 0.5F, W10);
 		renderer.setRenderBounds(0.375F, 0.0F, 0.375F, 0.625F, 0.5F, 0.625F);
 		return true;
 	}
 
 	public boolean shouldRender3DInInventory() {
 		return true;
 	}
 
 	public int getRenderId() {
 		return mods.nordwest.common.Config.CandleRendererID;
 	}
 }
