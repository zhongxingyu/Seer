 package testmod.client.render.item;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import testmod.client.render.model.ModelTest;
 import testmod.lib.Textures;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.client.IItemRenderer;
 
 
 public class ItemTileTestRenderer implements IItemRenderer{
 	
 	private ModelTest model;
 	
 	public ItemTileTestRenderer() {
 		model = new ModelTest();
 	}
 
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
 
 		return true;
 	}
 
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
 
 		return true;
 	}
 
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
 
 		switch (type) {
             case ENTITY: {
             	renderModel(-0.5F, 0.0F, 0.5F, 1.0F);
                 return;
             }
             case EQUIPPED: {
             	renderModel(0.0F, 0.0F, 1.0F, 1.0F);
                 return;
             }
             case EQUIPPED_FIRST_PERSON: {
             	renderModel(0.0F, 0.0F, 1.0F, 1.0F);
                 return;
             }
             case INVENTORY: {
             	renderModel(0.0F, 0F, 1.0F, 1.0F);
                 return;
             }
             default:
                 return;
         }
 		
 	}
 	
 	public void renderModel(float x, float y, float z, float scale) {
 		GL11.glPushMatrix();
         GL11.glDisable(GL11.GL_LIGHTING);
 
         // Scale, Translate, Rotate
         GL11.glScalef(scale, scale, scale);
         GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-90F, 1F, 0, 0);
 
         // Bind texture
         FMLClientHandler.instance().getClient().renderEngine.bindTexture(Textures.MODEL_BUSWAY);
         
         // Render
         model.renderPart("test");
 
         GL11.glEnable(GL11.GL_LIGHTING);
         GL11.glPopMatrix();
 	}
 
 }
