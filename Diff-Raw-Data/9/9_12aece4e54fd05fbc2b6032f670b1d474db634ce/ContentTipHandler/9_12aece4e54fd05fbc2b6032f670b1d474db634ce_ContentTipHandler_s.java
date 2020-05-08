 package ml.boxes.client;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import ml.boxes.Boxes;
 import ml.boxes.Lib;
 import ml.boxes.data.BoxData;
 import ml.boxes.data.ItemIBox;
 import ml.boxes.inventory.ContainerBox;
 import ml.boxes.item.ItemBox;
 import ml.boxes.network.packets.PacketTipClick;
 import ml.core.Geometry;
 import ml.core.Geometry.XYPair;
 import ml.core.Geometry.rectangle;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.client.gui.inventory.GuiContainerCreative;
 import net.minecraft.client.renderer.RenderEngine;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet250CustomPayload;
 
 import org.lwjgl.opengl.GL11;
 
 import codechicken.nei.ContainerCreativeInv;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class ContentTipHandler implements ITickHandler {
 
 	private static Slot hoverSlot;
 	private static long tickerTime = 0;
 	public static boolean showingTip = false;
 
 	//private static Slot boxSlot;
 	private static rectangle curBounds;
 	private static boolean renderContents = true;
 	private static rectangle gcBounds = new rectangle(0, 0, 0, 0);
 	private static boolean interacting = false;
 	private static XYPair gridDimensions;
 	private static List<ItemStack> contentStacks = new ArrayList<ItemStack>();
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		if (type.contains(TickType.RENDER)){
 			Minecraft mc = FMLClientHandler.instance().getClient();
 			if (mc.currentScreen instanceof GuiContainer){
 
 				Geometry.XYPair m = Geometry.getScaledMouse();
				if (!Boxes.neiInstalled) //NEI Provides a better place for doing this. Use it if we can
 					renderContentTip(mc, m.X, m.Y, (Float)tickData[0]);
 			}
 		}else if (type.contains(TickType.CLIENT)){
 			updateCurrentTip();
 		}
 	}
 
 	// Do calculations for the tip
 	private static void updateCurrentTip(){
 		Minecraft mc = FMLClientHandler.instance().getClient();
 		if (mc.currentScreen instanceof GuiContainer){
 			GuiContainer asGuiContainer = (GuiContainer)mc.currentScreen;
 
 			int guiXSize = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, (GuiContainer)mc.currentScreen, 1);
 			int guiYSize = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, (GuiContainer)mc.currentScreen, 2);
 
 			Geometry.XYPair m = Geometry.getScaledMouse();
 
 			gcBounds.width = asGuiContainer.width;
 			gcBounds.height = asGuiContainer.height;
 			
 			gcBounds.xCoord = (asGuiContainer.width - guiXSize) / 2;
 			gcBounds.yCoord = (asGuiContainer.height - guiYSize) / 2;
 			
 			if (!isTipValid(m.X, m.Y)){
 				showingTip = false;
 				curBounds = new rectangle(0, 0, 0, 0);
 				boolean thoverSlot = false;
 				for (Object objSlt : asGuiContainer.inventorySlots.inventorySlots){
 					Slot slt = (Slot)objSlt;
 					if (Geometry.pointInRect(m.X, m.Y, gcBounds.xCoord + slt.xDisplayPosition, gcBounds.yCoord + slt.yDisplayPosition, 16, 16)){
 						thoverSlot = true;
 						if (hoverSlot != slt)
 							tickerTime = mc.getSystemTime();
 						hoverSlot = slt;
 					}
 				}
 				if (!thoverSlot)
 					hoverSlot = null;
 			}
 			
 			if (hoverSlot != null &&
 					hoverSlot.getHasStack() &&
 					(hoverSlot.getStack().getItem() instanceof ItemBox) &&
 					(!Boxes.config.shiftForTip || asGuiContainer.isShiftKeyDown()) &&
 					(mc.getSystemTime() - tickerTime > Boxes.config.tipReactionTime || asGuiContainer.isShiftKeyDown()) &&
 					!(asGuiContainer instanceof GuiBox && ((ContainerBox)asGuiContainer.inventorySlots).box instanceof ItemIBox && mc.thePlayer.inventory.currentItem == hoverSlot.getSlotIndex()) //((ItemIBox)((ContainerBox)asGuiContainer.inventorySlots).box).stack == hoverSlot.getStack()
 					)
 			{
 				BoxData bd = getROBoxData();
 				
 				contentStacks.clear();
 				if (Boxes.neiInstalled && asGuiContainer.isShiftKeyDown() && hoverSlot.inventory instanceof InventoryPlayer){
 					interacting = true;
 					for (int i=0; i<bd.getSizeInventory(); i++){
 						contentStacks.add(bd.getStackInSlot(i));
 					}
 					gridDimensions = Geometry.determineSquarestGrid(bd.getSizeInventory());
 					showingTip = true;
 				} else {
 					interacting = false;
 					List<ItemStack> iss = bd.getContainedItemStacks();
 					for (ItemStack is : iss){
 						boolean matched = false;
 						for (ItemStack his : contentStacks){
 							if (his.isItemEqual(is) && ItemStack.areItemStackTagsEqual(his, is)){
 								his.stackSize += is.stackSize;
 								matched = true;
 								break;
 							}
 						}
 						if (!matched)
 							contentStacks.add(is.copy());
 					}
 					gridDimensions = Geometry.determineSquarestGrid(contentStacks.size());
 					showingTip = contentStacks.size() > 0;
 				}
 			}
 			
 			if (showingTip){
 				int tX = gridDimensions.X*18 +16;
 				int tY = gridDimensions.Y*18 +16;
 				renderContents = true;
 				if (tX != curBounds.width || tY != curBounds.height){
 					renderContents = false;
 					if (tX > curBounds.width){
 						curBounds.width += 16;
 						if (tX < curBounds.width)
 							curBounds.width = tX;
 					} else if (tX < curBounds.width) {
 						curBounds.width -= 16;
 						if (tX > curBounds.width)
 							curBounds.width = tX;
 					}
 					
 					if (tY > curBounds.height){
 						curBounds.height += 16;
 						if (tY < curBounds.height)
 							curBounds.height = tY;
 					} else if (tY < curBounds.height) {
 						curBounds.height -= 16;
 						if (tY > curBounds.height)
 							curBounds.height = tY;
 					}
 					
 					curBounds.xCoord = gcBounds.xCoord + hoverSlot.xDisplayPosition + (16-curBounds.width)/2;
 					curBounds.yCoord = gcBounds.yCoord + hoverSlot.yDisplayPosition - curBounds.height;
 				}
 			}
 		}
 	}
 	
 	public static boolean isTipValid(int mX, int mY){
 		return showingTip && (hoverSlot != null && (Geometry.pointInRect(mX, mY, gcBounds.xCoord + hoverSlot.xDisplayPosition, gcBounds.yCoord + hoverSlot.yDisplayPosition, 16, 16) || 
 				(interacting && Geometry.pointInRect(mX, mY, curBounds))) &&
 				hoverSlot.getHasStack() // TODO Add better checking to ensure that it is the same box.
 				);
 	}
 
 	public static boolean isPointInTip(int pX, int pY){
 		return Geometry.pointInRect(pX, pY, curBounds);
 	}
 	
 	public static int getSlotAtPosition(int pX, int pY){
 		if (interacting && renderContents && hoverSlot != null){
 			for (int i=0; i< getROBoxData().getSizeInventory(); i++){
 				int col = i%gridDimensions.X;
 				int row = i/gridDimensions.X;
 				
 				int slotX = 8+col*18 + curBounds.xCoord;
 				int slotY = 10+row*18 + curBounds.yCoord;
 				
 				if (Geometry.pointInRect(pX, pY, slotX, slotY, 16, 16)){
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 	
 	public static ItemStack getStackAtPosition(int pX, int pY){
 		int sltNum = getSlotAtPosition(pX, pY);
 		BoxData bd = getROBoxData();
 		if (sltNum >= 0 && sltNum < bd.getSizeInventory()){
 			return bd.getStackInSlot(sltNum);
 		}
 		return null;
 	}
 	
 	private static ItemIBox getItemIBox(){
 		return new ItemIBox(hoverSlot.getStack());
 	}
 	
 	private static BoxData getROBoxData(){
 		return getItemIBox().getBoxData();
 	}
 
 	//Render the tip
 	public static void renderContentTip(Minecraft mc, int mx, int my, float tickTime){
 		if (revalidateTip(mx, my)){
 			RenderEngine re = mc.renderEngine;
 			int tex = re.getTexture("/ml/boxes/res/contentTipGui2.png");
 			re.bindTexture(tex);
 			
 			GL11.glPushMatrix();
 			GL11.glTranslatef(curBounds.xCoord, curBounds.yCoord, 0F);
 			GL11.glDisable(GL11.GL_LIGHTING);
 			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 
 			RenderUtils.drawTexturedModalRect(0, 0, 0, 0, curBounds.width-9, curBounds.height-7);
 			RenderUtils.drawTexturedModalRect(7, 0, 178-(curBounds.width-7), 0, curBounds.width-7, curBounds.height-7);
 			RenderUtils.drawTexturedModalRect(7, 9, 178-(curBounds.width-7), 106-(curBounds.height-9), curBounds.width-7, curBounds.height-9);
 			RenderUtils.drawTexturedModalRect(0, 9, 0, 106-(curBounds.height-9), curBounds.width-9, curBounds.height-9);
 
 			if (renderContents){
 				for (int i=0; i<contentStacks.size(); i++){
 					int col = i%gridDimensions.X;
 					int row = i/gridDimensions.X;
 
 					int slotX = 8+col*18;
 					int slotY = 10+row*18;
 					
 					ItemStack is = contentStacks.get(i);
 					if (interacting){
 						re.bindTexture(tex);
 						RenderUtils.drawTexturedModalRect(slotX-1, slotY-1, 0, 106, 18, 18);
 
 						RenderUtils.drawStackAt(mc, slotX, slotY, is);
 
 						GL11.glDisable(GL11.GL_LIGHTING);
 						if (Geometry.pointInRect(mx - curBounds.xCoord, my - curBounds.yCoord, slotX, slotY, 16, 16)){
 							RenderUtils.drawGradientRect(slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433);
 						}
 					} else {
 						RenderUtils.drawSpecialStackAt(mc, slotX, slotY, is, is.stackSize> 1 ? Lib.toGroupedString(is.stackSize,1) : "");
 					}
 				}
 			}
 
 			GL11.glEnable(GL11.GL_LIGHTING);
 			GL11.glPopMatrix();
 		}
 	}
 
 	public static boolean revalidateTip(int mx, int my){
 		if (!isTipValid(mx, my)){
 			showingTip = false;
 		}
 		return showingTip;
 	}
 	
 	public static boolean handleClick(int mx, int my, int btn){
 		if (revalidateTip(mx, my) && isPointInTip(mx, my)){
 			int slNum = getSlotAtPosition(mx, my);
 			ItemIBox iib = getItemIBox();
 			Minecraft mc = FMLClientHandler.instance().getClient();
 			
 			if (slNum >= 0 && (mc.thePlayer.inventory.getItemStack() == null || iib.getBoxData().ISAllowedInBox(mc.thePlayer.inventory.getItemStack()))){
 				ItemStack isInBox = iib.getBoxData().getStackInSlot(slNum);
 				iib.getBoxData().setInventorySlotContents(slNum, mc.thePlayer.inventory.getItemStack());
 				iib.saveData();
 				mc.thePlayer.inventory.setItemStack(isInBox);
 				
 				if (mc.currentScreen instanceof GuiContainerCreative){ //Don't love this, but it's the only way I have found to get the ContentTips updating properly in CreativeInventory. PR anyone?
 					mc.thePlayer.inventoryContainer.detectAndSendChanges();
 				} else {
 					Packet250CustomPayload pkt = (new PacketTipClick((Player)mc.thePlayer, hoverSlot.getSlotIndex(), slNum)).convertToPkt250();
 					mc.thePlayer.sendQueue.addToSendQueue(pkt);
 				}
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.RENDER, TickType.CLIENT);
 	}
 
 	@Override
 	public String getLabel() {
 		return "Boxes";
 	}
 }
