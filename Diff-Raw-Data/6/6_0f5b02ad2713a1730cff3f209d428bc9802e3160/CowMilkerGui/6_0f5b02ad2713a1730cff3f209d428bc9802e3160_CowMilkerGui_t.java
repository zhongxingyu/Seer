 package biotech.client;
 
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.StatCollector;
 import org.lwjgl.opengl.GL11;
 import biotech.Biotech;
 import biotech.container.CowMilkerContainer;
 import biotech.tileentity.CowMilkerTileEntity;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class CowMilkerGui extends GuiContainer
 {
 	private CowMilkerTileEntity	tileEntity;
 	
 	private int					containerWidth;
 	private int					containerHeight;
 	
 	public CowMilkerGui(InventoryPlayer playerInventory, CowMilkerTileEntity tileEntity)
 	{
 		super(new CowMilkerContainer(playerInventory, tileEntity));
 		
 		this.tileEntity = tileEntity;
 	}
 	
 	/**
 	 * Draw the foreground layer for the GuiContainer (everything in front of
 	 * the items)
 	 */
 	@Override
 	protected void drawGuiContainerForegroundLayer(int i, int j)
 	{
 		this.fontRenderer.drawString(this.tileEntity.getInvName(), 57, 4, 4210752);
 		
 		String displayText = "";
 		
 		if (this.tileEntity.isDisabled())
 		{
 			displayText = "Disabled!";
 		}
 		else if (this.tileEntity.checkRedstone())
 		{
 			displayText = "Working";
 		}
 		else
 		{
 			displayText = "Idle";
 		}
 		
 		this.fontRenderer.drawString("Status: " + displayText, 28, 22, 0x00CD00);
 		this.fontRenderer.drawString("Milk: " + this.tileEntity.getMilkStored() + "/" + this.tileEntity.getMaxMilk(), 28, 32, 0x00CD00);
 		
 		this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
 	}
 	
 	/**
 	 * Draw the background layer for the GuiContainer (everything behind the
 	 * items)
 	 */
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
 	{
 		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		this.mc.renderEngine.bindTexture(this.getTexture());
 		
 		this.containerWidth = ((this.width - this.xSize) / 2);
 		this.containerHeight = ((this.height - this.ySize) / 2);
 		
 		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, xSize, ySize);
 		
 		int milkScale = (int) (((double) this.tileEntity.getMilkStored() / this.tileEntity.getMaxMilk()) * 100);
 		
 		this.drawTexturedModalRect(containerWidth + 108, containerHeight + 71 - milkScale, 176, 50 - milkScale, 8, milkScale);
 		
 		if (this.tileEntity.bucketTime > 0)
 		{
			int scale = (((this.tileEntity.bucketTime - this.tileEntity.bucketTimeMax) / this.tileEntity.bucketTimeMax) * 100);			
			//((this.tileEntity.bucketTime / this.tileEntity.bucketTimeMax) / 10);
 			
			this.drawTexturedModalRect(containerWidth + 122, containerHeight + 38, 176, 51, 12, 28 - scale);
 		}
 	}
 	
 	public static String getTexture()
 	{
 		return Biotech.GUI_PATH + "GUI_CowMilker.png";
 	}
 }
