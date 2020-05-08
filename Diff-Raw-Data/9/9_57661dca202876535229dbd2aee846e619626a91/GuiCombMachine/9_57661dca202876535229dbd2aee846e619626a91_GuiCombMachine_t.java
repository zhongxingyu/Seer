 package Earth.Combiner.MachineUtilety;
 
 import Earth.Combiner.CombinerCore;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.client.resources.I18n;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.ResourceLocation;
 
import org.lwjgl.opengl.GL11;// Cant import somhow ...
 
 @SideOnly(Side.CLIENT)
 public class GuiCombMachine extends GuiContainer
 {
     private static final ResourceLocation field_110410_t = new ResourceLocation("combinercore", "/textures/gui/combheater.png");
     
     private TileEntityCombMachine commachineInventory;
 
     public GuiCombMachine(InventoryPlayer par1InventoryPlayer, TileEntityCombMachine par2TileEntityCombMachine)
     {
         super(new ContainerCombMachine(par1InventoryPlayer, par2TileEntityCombMachine));
         this.commachineInventory = par2TileEntityCombMachine;
     }
 
     /**
      * Draw the foreground layer for the GuiContainer (everything in front of the items)
      */
     protected void drawGuiContainerForegroundLayer(int par1, int par2)
     {
         String s = this.commachineInventory.isInvNameLocalized() ? this.commachineInventory.getInvName() : I18n.func_135053_a(this.commachineInventory.getInvName());
         this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
         this.fontRenderer.drawString(I18n.func_135053_a("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
     }
 
     /**
      * Draw the background layer for the GuiContainer (everything behind the items)
      */
     protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
     {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.func_110434_K().func_110577_a(field_110410_t);
         int k = (this.width - this.xSize) / 2;
         int l = (this.height - this.ySize) / 2;
         this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
         int i1;
 
         if (this.commachineInventory.isCombining())
         {
         	this.drawTexturedModalRect(k+31, l+38, 176, 32, 12, 12);
             i1 = this.commachineInventory.getCombTimeRemainingScaled(12);
             this.drawTexturedModalRect(k + 78, l + 54 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
         }
 
         i1 = this.commachineInventory.getCombDurProgressScaled(24);
         this.drawTexturedModalRect(k + 70, l + 34, 176, 14, i1 + 1, 16);
     }
 }
 
