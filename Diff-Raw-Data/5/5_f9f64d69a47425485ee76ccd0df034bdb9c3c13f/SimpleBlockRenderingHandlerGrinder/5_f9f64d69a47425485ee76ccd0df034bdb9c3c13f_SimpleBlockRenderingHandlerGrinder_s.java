 package slimevoid.tmf.client.renderers;
 
 import slimevoid.tmf.machines.tileentities.TileEntityGrinder;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class SimpleBlockRenderingHandlerGrinder implements ISimpleBlockRenderingHandler{
 	public static int id = RenderingRegistry.getNextAvailableRenderId();
 	
 	
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
 		TileEntityGrinder tile = new TileEntityGrinder();
 		tile.blockType = block;
 		TileEntityRenderer.instance.renderTileEntityAt(tile, 0.0D, 0.0D, 0.0D, 0.0F);
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
 		return false;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return true;
 	}
 
 	@Override
 	public int getRenderId() {
 		return id;
 	}
 
 }
