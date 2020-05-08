 package ccm.compresstion.client.renderer.block;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 import ccm.compresstion.block.CompressedType;
 import ccm.compresstion.tileentity.CompressedTile;
 import ccm.compresstion.utils.lib.Archive;
 import ccm.nucleum.omnium.utils.helper.NBTHelper;
 
 /**
  * CompressedBlockRenderer
  * <p>
 *
  * @author Resinresin
  */
 public class CompressedBlockRenderer implements ISimpleBlockRenderingHandler
 {
     public final static int id = RenderingRegistry.getNextAvailableRenderId();
 
     World worldr;
 
     @Override
     public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
     {
        renderer.setOverrideBlockTexture(renderer.getBlockIcon(block));
         renderer.clearOverrideBlockTexture();
 
         CompressedTile te = (CompressedTile) world.getBlockTileEntity(x, y, z);
         CompressedType type = CompressedType.values()[world.getBlockMetadata(x, y, z)];
 
         Icon original = Block.blocksList[te.getBlock().blockID].getBlockTexture(world, x, y, z, 0);
         Icon overlay = type.getOverlay();
         Tessellator.instance.setBrightness(0xf0);
         Tessellator.instance.setColorOpaque(100, 100, 100);
 
         renderer.renderFaceYNeg(block, x, y, z, original);
         renderer.renderFaceYNeg(block, x, y, z, overlay);
 
         renderer.renderFaceYPos(block, x, y, z, original);
         renderer.renderFaceYPos(block, x, y, z, overlay);
 
         renderer.renderFaceXNeg(block, x, y, z, original);
         renderer.renderFaceXNeg(block, x, y, z, overlay);
 
         renderer.renderFaceXPos(block, x, y, z, original);
         renderer.renderFaceXPos(block, x, y, z, overlay);
 
         renderer.renderFaceZNeg(block, x, y, z, original);
         renderer.renderFaceZNeg(block, x, y, z, overlay);
 
         renderer.renderFaceZPos(block, x, y, z, original);
         renderer.renderFaceZPos(block, x, y, z, overlay);
 
         worldr.markBlockForUpdate(x, y, z);
         Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(x, y, z);
 
         return false;
     }
 
     @Override
     public boolean shouldRender3DInInventory()
     {
         return true;
     }
 
     @Override
     public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
     {
         Tessellator tessellator = Tessellator.instance;
         GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, -1.0F, 0.0F);
         renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 1.0F, 0.0F);
         renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 0.0F, -1.0F);
         renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 0.0F, 1.0F);
         renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(-1.0F, 0.0F, 0.0F);
         renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(1.0F, 0.0F, 0.0F);
         renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
         tessellator.draw();
         GL11.glTranslatef(0.5F, 0.5F, 0.5F);
     }
 
     @Override
     public int getRenderId()
     {
         return id;
     }
 }
