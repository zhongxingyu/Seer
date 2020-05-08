 package mrkirby153.MscHouses.block.GUI;
 
 import mrkirby153.MscHouses.block.Container.ContainerHouseGenerator;
 import mrkirby153.MscHouses.block.tileEntity.TileEntityHouseGen;
 import mrkirby153.MscHouses.lib.ResourceFile;
 import mrkirby153.MscHouses.lib.Strings;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 /**
  * 
  * Msc Houses
  *
  * GuiBlockBase
  *
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 @SideOnly(Side.CLIENT)
 public class GuiHouseGenerator extends GuiContainer{
 
 	private GuiButton generate;
 	public GuiHouseGenerator (InventoryPlayer inventoryPlayer,
 			TileEntityHouseGen tileEntity) {
 		//the container is instanciated and passed to the superclass for handling
 		super(new ContainerHouseGenerator(inventoryPlayer, tileEntity));
 		this.ySize = 176;
 		this.xSize = 176;
 	}
 	@Override
	public void initGui() {
	//	this.buttonList.add(generate = new GuiButton(0, this.width /2 + 20, this.height /2 - 65, 60, 20, "Generate"));
	}
	@Override
 	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
 		//draw text and stuff here
 		//the parameters for drawString are: string, x, y, color
 		fontRenderer.drawString(StatCollector.translateToLocal(Strings.RESOURCE_PREFIX+Strings.TILE_HOUSE_GEN), 8, 6, 4210752);
 		//draws "Inventory" or your regional equivalent
 		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);
 
 	}
 
 	@Override
 	protected void actionPerformed(GuiButton button) {
 		if(button.id==0){
 			this.mc.displayGuiScreen((GuiScreen)null);
 		}
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float par1, int par2,
 			int par3) {
 		//draw your Gui here, only thing you need to change is the path
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		this.mc.getTextureManager().bindTexture(ResourceFile.houseGen_Img);
 
 		int x = (width - xSize) / 2;
 		int y = (height - ySize) / 2;
 		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
 	}
 
 }
