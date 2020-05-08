 package mods.CompactStuff.compactor;
 
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import mods.CompactStuff.BlockCompressed;
 import mods.CompactStuff.Metas;
 import mods.CompactStuff.client.CompactPacket;
 import mods.CompactStuff.client.ImageFiles;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GL11;
 
 public class CompactorGUI extends GuiContainer {
 	TileEntityCompactor tec;
 	public CompactorGUI(IInventory play, TileEntityCompactor tile) {
 		super(new ContainerCompactor(play, tile));
 		this.ySize=222;
 		this.tec=tile;
 	}
 
 	@Override protected void drawGuiContainerForegroundLayer(int par1, int par2) {
         this.fontRenderer.drawString("Compactor", 8, 6, 4210752);
         this.mc.renderEngine.bindTexture(ImageFiles.COMPACTOR_GUI.path);
         int x0 = 133, y0 = 18;
         for(int r=0; r<3; r++) {
         	for(int c=0; c<2; c++) {
         		if(CompactorRecipes.isEnabled(tec.enabled(), tec.getStackInSlot(tec.COMFIRST+r*2+c))) {
         			drawTexturedModalRect(x0+c*18,y0+r*18,176,0,18,18);
         		}
         	}
         }
     }
 
 	@Override protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.renderEngine.bindTexture(ImageFiles.COMPACTOR_GUI.path);
         int x = (width - xSize) / 2;
         int y = (height - ySize) / 2;
         this.drawTexturedModalRect(x,y,0,0,xSize,ySize);
 	}
 	
 	private int getSlotNumberBetween(int x, int y, int first, int last) {
 		Slot s = getSlotAtPosition(x,y);
 		int slot=-1;
 		if(s!=null) slot = s.slotNumber;
 		if(s==null || slot<first || slot>last) return -1;
 		return slot;
 	}
 	
 	@Override protected void mouseClicked(int x, int y, int button) {
 		int slot = getSlotNumberBetween(x,y,TileEntityCompactor.CRAFTFIRST, TileEntityCompactor.CRAFTLAST);
 		if(slot==-1) slot = getSlotNumberBetween(x,y,TileEntityCompactor.OUTPUT, TileEntityCompactor.OUTPUT);
 		if(slot==-1) slot = getSlotNumberBetween(x,y,TileEntityCompactor.COMFIRST,TileEntityCompactor.COMLAST);
 		if(slot==-1) super.mouseClicked(x, y, button);
 		else clickPacket(slot, button);
 	}
 	
 	@Override public void handleMouseInput() {
 		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
 	    int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
 	    int slot = getSlotNumberBetween(i,j,TileEntityCompactor.CRAFTFIRST, TileEntityCompactor.CRAFTLAST);
 		if(slot>-1 && Mouse.getEventButton()==-1 && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1))) {
 			this.mouseMovedOrUp(i, j, -1);
 		} else super.handleMouseInput();
 	}
 	@Override protected void mouseMovedOrUp(int x, int y, int which) {
 		int slot = getSlotNumberBetween(x,y,TileEntityCompactor.CRAFTFIRST, TileEntityCompactor.CRAFTLAST);
 		if(slot==-1) super.mouseMovedOrUp(x, y, which);
 		else clickPacket(slot, 0);
 	}
 	
 	public void clickPacket(int slot, int button) {
 		TileEntityCompactor tile = tec;
 		String channel = (tile.CRAFTFIRST<=slot && slot<=tile.CRAFTLAST) ? Metas.CH_COMPCRAFT :
 						 (tile.OUTPUT==slot) ? Metas.CH_COMPOUT : Metas.CH_COMPMAKE;
 		CompactPacket packet = new CompactPacket(channel);
 		packet.writeInts(tile.xCoord, tile.yCoord, tile.zCoord, slot, button);
 		packet.send();
 	}
 	
 	private Slot getSlotAtPosition(int x, int y) {
         for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
             Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i);
             if (this.isMouseOverSlot(slot, x, y)) return slot;
         }
         return null;
     }
 	
 	private boolean isMouseOverSlot(Slot slot, int x, int y) {
         return this.isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y);
     }
 }
