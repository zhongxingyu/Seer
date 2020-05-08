 /**
  * Majyyka
  *
 * ModelWand.java
  *
  * @author Myo-kun
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 package majyyka.client;
 
 import net.minecraft.client.model.ModelBase;
 import net.minecraftforge.client.model.AdvancedModelLoader;
 import net.minecraftforge.client.model.IModelCustom;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class ModelWand extends ModelBase {
     
     private IModelCustom modelWand;
     
     public ModelWand() {
     	modelWand = AdvancedModelLoader.loadModel("/assets/majyyka/models/wand.obj");
     }
     
     public void render() {
         modelWand.renderPart("Cube");
     }
     
 }
