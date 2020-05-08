 package testmod.client.render.tileentity;
 
 import org.lwjgl.opengl.GL11;
 
 import testmod.client.render.model.ModelTest;
 import testmod.lib.Textures;
 import cpw.mods.fml.client.FMLClientHandler;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 
 
 public class TileTestRenderer extends TileEntitySpecialRenderer {
 
 	private ModelTest model = new ModelTest();
 	
 	@Override
 	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float tick) {
 		
 		int scale = 1;
 		
 		GL11.glPushMatrix();
 		GL11.glDisable(GL11.GL_LIGHTING);
 
 		// Scale, Translate, Rotate
 		GL11.glScalef(scale, scale, scale);
 		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glRotatef(-90F, 0F, 0F, 0F);
 		
 		FMLClientHandler.instance().getClient().renderEngine.bindTexture(Textures.MODEL_BUSWAY);
 		
 		model.renderPart("test");
 		
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glPopMatrix();
 	}
 
 }
