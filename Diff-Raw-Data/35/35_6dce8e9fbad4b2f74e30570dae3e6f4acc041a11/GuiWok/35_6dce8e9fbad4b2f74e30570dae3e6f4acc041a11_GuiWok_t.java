 package deathrat.mods.btbees.gui;
 
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 
 import org.lwjgl.opengl.GL11;
 
 import deathrat.mods.btbees.blocks.TileEntityWok;
 
 public class GuiWok extends GuiContainer
 {
 
 	public GuiWok(InventoryPlayer invPlayer, TileEntityWok tileEntity)
 	{
 		super(new ContainerWok(invPlayer, tileEntity));
 	}
 
 	@Override
 	protected void drawGuiContainerForegroundLayer(int par1, int par2)
 	{
 		super.drawGuiContainerForegroundLayer(par1, par2);

		fontRenderer.drawString("Wok", 8, 6, 4210752);
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
 	{
 		int texture = mc.renderEngine.getTexture("/deathrat/mods/btbees/gui_wok.png");
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		this.mc.renderEngine.bindTexture(texture);
 		int x = (width - xSize) / 2;
 		int y = (height - ySize) / 2;
 		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
 	}
 }
