 package infinitealloys.client.gui;
 
 import infinitealloys.block.Blocks;
 import infinitealloys.tile.TEMComputer;
 import infinitealloys.tile.TEMEnergyStorage;
 import infinitealloys.util.Consts;
 import infinitealloys.util.Funcs;
 import infinitealloys.util.MachineHelper;
 import infinitealloys.util.Point;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ResourceLocation;
 
 public class GuiInternetWand extends GuiScreen {
 
 	private final RenderItem itemRenderer = new RenderItem();
 	private final ResourceLocation background;
 	private final int xSize = 196;
 	private final int ySize = 240;
 
 	/** If the GUI was opened by clicking on a machine, this button adds the machine that was clicked to the wand */
 	private GuiButton addToWand;
 	/** If the GUI was opened by clicking on a machine, this button adds the selected machine to the machine that was clicked */
 	private GuiButton addSelected;
 
 	private MachineButton[] machineButtons = new MachineButton[Consts.WAND_MAX_COORDS];
 	private Point currentMachine;
 
 	/** Binary integer to represent the buttons that are selected. Right-most digit is the top of the list.
 	 * There is one bit for each button. 0 is not-selected. 1 is selected. */
 	private int selectedButtons;
 
 	public GuiInternetWand() {
 		background = Funcs.getGuiTexture("wand");
 	}
 
 	@Override
 	public void initGui() {
 		buttonList.add(new GuiButton(0, width - 20, 0, 20, 20, "?")); // The button to enable/disable help
 		buttonList.add(addToWand = new GuiButton(1, width / 2 - 91, height / 2 - 112, 80, 20, Funcs.getLoc("wand.addToWand")));
 		buttonList.add(addSelected = new GuiButton(2, width / 2 + 11, height / 2 - 112, 80, 20, Funcs.getLoc("wand.addSelected")));
 		setButtons();
 	}
 
 	@Override
 	public void drawScreen(int mouseX, int mouseY, float partialTick) {
 		Funcs.bindTexture(background);
 		drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
 		super.drawScreen(mouseX, mouseY, partialTick);
 		for(MachineButton button : machineButtons)
 			if(button != null)
 				button.drawButton();
 	}
 
 	private void setButtons() {
 		ItemStack wandItem = mc.thePlayer.getHeldItem();
 		if(wandItem.hasTagCompound()) {
 			NBTTagCompound tagCompound = wandItem.getTagCompound();
 
 			for(int i = 0; i < Consts.WAND_MAX_COORDS; i++) {
 				machineButtons[i] = null;
 				if(wandItem.getTagCompound().hasKey("Coords" + i)) {
 					int id = tagCompound.getIntArray("Coords" + i)[0];
 					int x = tagCompound.getIntArray("Coords" + i)[1];
 					int y = tagCompound.getIntArray("Coords" + i)[2];
 					int z = tagCompound.getIntArray("Coords" + i)[3];
 					if(MachineHelper.isElectric(mc.theWorld, x, y, z) || MachineHelper.isWireless(mc.theWorld, x, y, z))
 						machineButtons[i] = new MachineButton(i, 150, i * 20 + 100, id, x, y, z);
 				}
 			}
 
 			addToWand.enabled = false;
 			addSelected.enabled = true;
 			if(tagCompound.hasKey("CoordsCurrent")) {
 				int[] a = tagCompound.getIntArray("CoordsCurrent");
 				addToWand.enabled = !tagCompound.hasKey("Coords" + (Consts.WAND_MAX_COORDS - 1)) && MachineHelper.isClient(mc.theWorld, a[0], a[1], a[2]);
 
 				boolean noneSelected = true;
 				addSelected.enabled = true;
 				TileEntity te = mc.theWorld.getBlockTileEntity(a[0], a[1], a[2]);
 				for(MachineButton button : machineButtons) { // Go over each button
 					if((selectedButtons & 1 << button.buttonID) != 0) { // If this button is selected
 						noneSelected = false;
 						if(!(te instanceof TEMEnergyStorage && button.isElectric || te instanceof TEMComputer && button.isWireless))
 							addSelected.enabled = false;
 					}
 				}
 				if(noneSelected)
 					addSelected.enabled = false;
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
 		for(int i = 0; i < Consts.WAND_MAX_COORDS; i++) {
 			MachineButton button = machineButtons[i];
 			if(Funcs.mouseInZone(mouseX, mouseY, button.xPos, button.yPos, button.width, button.height)) { // If this button was clicked
 				selectedButtons ^= 1 << i; // Invert its selected state
 				if(isShiftKeyDown()) {
 					int firstOne = -1; // The index of the position of the rightmost 1 in the binary integer
 					int lastOne = -1; // The index of the position of the leftmost 1 in the binary integer
 					for(int j = 0; j < Consts.WAND_MAX_COORDS; j++) { // Go over each bit in the integer
 						if((selectedButtons & 1 << j) != 0) { // If the bit is 1
 							if(firstOne == -1) // If the leftmost 1 has not been found already
 								firstOne = j; // This is the leftmost 1
 							lastOne = j; // This is the rightmost 1 (so far)
 						}
 					}
 					if(firstOne != -1) // If there is a 1 in the integer
 						// Loop from the first 1 to the last 1
 						for(int j = firstOne; j <= lastOne; j++)
 							selectedButtons |= 1 << j; // Set each bit to 1
 				}
 				else if(!isCtrlKeyDown()) // If the CTRL or Shift key wasn't held, set all other buttons to 0
 					selectedButtons &= 1 << i;
 				break;
 			}
 		}
 		setButtons();
 	}
 
 	@Override
 	public boolean doesGuiPauseGame() {
 		return false;
 	}
 
 	/** A button that represents a machine with its texture and coordinates */
 	private class MachineButton {
 
 		final int width = 120;
 		final int height = 30;
 
 		int buttonID;
 		int xPos;
 		int yPos;
 
 		int machineID;
 		int machineX;
 		int machineY;
 		int machineZ;
 		boolean isElectric;
 		boolean isWireless;
 
 		MachineButton(int buttonID, int xPos, int yPos, int machineID, int machineX, int machineY, int machineZ) {
 			this.buttonID = buttonID;
 			this.xPos = xPos;
 			this.yPos = yPos;
 			this.machineID = machineID;
 			this.machineX = machineX;
 			this.machineY = machineY;
 			this.machineZ = machineZ;
 			isElectric = MachineHelper.isElectric(mc.theWorld, machineX, machineY, machineZ);
 			isWireless = MachineHelper.isWireless(mc.theWorld, machineX, machineY, machineZ);
 		}
 
 		void drawButton() {
 			Funcs.bindTexture(GuiMachine.extras);
 
			if((selectedButtons & 1 << buttonID) != 0) { // If this button is selected, draw a white box around it
				drawHorizontalLine(xPos - 1, yPos - 1, xPos + width + 1, 0xffffff);
				drawHorizontalLine(xPos - 1, yPos + width + 1, xPos + width + 1, 0xffffff);
				drawVerticalLine(xPos - 1, yPos - 1, yPos + height + 1, 0xffffff);
				drawVerticalLine(xPos + width + 1, yPos - 1, yPos + height + 1, 0xffffff);
			}
 
 			// Draw the string for the coordinates
 			String display = machineX + ", " + machineY + ", " + machineZ;
 			mc.fontRenderer.drawStringWithShadow(display, xPos + 90 - (mc.fontRenderer.getStringWidth(display) / 2), yPos + 5, 0xffffff);
 
 			itemRenderer.renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, new ItemStack(Blocks.machineID, 1, machineID), xPos + 18, yPos);
 		}
 	}
 }
