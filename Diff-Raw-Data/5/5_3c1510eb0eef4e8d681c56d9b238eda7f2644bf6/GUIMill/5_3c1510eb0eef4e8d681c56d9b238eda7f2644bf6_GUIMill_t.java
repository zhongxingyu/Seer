 package adanaran.mods.bfr.gui;
 
 import org.lwjgl.opengl.GL11;
 
 import adanaran.mods.bfr.entities.TileEntityMill;
 import adanaran.mods.bfr.entities.TileEntityStove;
 import adanaran.mods.bfr.inventory.ContainerMill;
 import adanaran.mods.bfr.inventory.ContainerStove;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.client.resources.I18n;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.world.World;
 
 /**
  * @author Demitreus
  *
  */
 public class GUIMill extends GuiContainer {
 
 
 	private TileEntityMill millInventory;
 	public static final ResourceLocation guiTexture = new ResourceLocation("bfr","gui/container/mill.png");
 
 	public GUIMill(InventoryPlayer invPlayer, TileEntityMill tileEntityMill, World world){
         super(new ContainerMill(invPlayer, tileEntityMill, world));
         this.millInventory = tileEntityMill;
 	}
 	
     /**
      * Draw the foreground layer for the GuiContainer (everything in front of the items)
      */
     protected void drawGuiContainerForegroundLayer(int par1, int par2)
     {
         String s = this.millInventory.isInvNameLocalized() ? this.millInventory.getInvName() : I18n.func_135053_a(this.millInventory.getInvName());
         this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
         this.fontRenderer.drawString(I18n.func_135053_a("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
     }
     
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.func_110434_K().func_110577_a(guiTexture);
         int k = (this.width - this.xSize) / 2;
         int l = (this.height - this.ySize) / 2;
         this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
         int i1;
         if(millInventory.isMilling()){
            this.drawTexturedModalRect(k+12, l+16, 176, 31, 33, 33);
         }
         //position of progressbar
         i1 = millInventory.getMillProgressScaled(24);
        this.drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16);
 		
 	}
 }
