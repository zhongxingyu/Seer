 /**
  * Majyyka
  *
 * ItemWandRenderer.scala
  *
  * @author Myo-kun
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 package majyyka.client;
 
 import java.util.LinkedList;
 
 import majyyka.api.Wand;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.IItemRenderer;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.client.FMLClientHandler;
 
 public class ItemWandRenderer implements IItemRenderer {
     
     private ModelWand modelWand;
     public LinkedList<ResourceLocation> wandTextures = new LinkedList<ResourceLocation>();
     public LinkedList<String> wandNames = new LinkedList<String>(Wand.wands().keySet());
     
     public ItemWandRenderer() {
     	
     	modelWand = new ModelWand();
     	
     }
     
     public boolean handleRenderType(ItemStack stack, ItemRenderType renderType) {
         
         return true;
         
     }
     
     public boolean shouldUseRenderHelper(ItemRenderType renderType, ItemStack stack, ItemRendererHelper helper) {
         
         return true;
         
     }
     
     public void renderItem(ItemRenderType renderType, ItemStack stack, Object... data) {
         
         switch (renderType) {
             case ENTITY:
                 renderWand(0, 0, 0, 0.5F, stack);
                 return;
             case EQUIPPED:
                 renderWand(0, 1, 1, 0.5F, stack);
                 return;
             case INVENTORY:
                 renderWand(0, 0, 0, 0.5F, stack);
                 return;
             default:
             	renderWand(0, 0, 0, 0.5F, stack);
                 return;
         }
         
     }
     
     private void renderWand(float x, float y, float z, float scale, ItemStack stack) {
         
         GL11.glPushMatrix();
         
         GL11.glDisable(GL11.GL_LIGHTING);
         
         GL11.glTranslatef(x, y, z);
         GL11.glScalef(scale, scale, scale);
         GL11.glRotatef(180, 0, 1, 0);
         
         for (int i = 0; i < Wand.wands().size(); i++) {
             wandTextures.add(new ResourceLocation("majyyka", "textures/models/wands/" + wandNames.get(i)));
         }
         
         if (stack.hasTagCompound()) {
             
             FMLClientHandler.instance().getClient().renderEngine.bindTexture(wandTextures.get(stack.stackTagCompound.getInteger("WandTypeIndex")));
             
         }
         
         modelWand.render();
         
         GL11.glEnable(GL11.GL_LIGHTING);
         GL11.glPopMatrix();
         
     }
     
 }
