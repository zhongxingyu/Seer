/**
 * 
 */
 package fuj1n.globalChestMod.client.gui;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 
 import fuj1n.globalChestMod.GlobalChests;
 import fuj1n.globalChestMod.common.inventory.ContainerGlobalChest;
 import fuj1n.globalChestMod.common.inventory.InventoryGlobalChest;
 import fuj1n.globalChestMod.common.tileentity.TileEntityGlobalChest;
 
 /**
  * @author fuj1n
  */
 
 class GuiGlobalChest extends GuiContainer{
 
 	private static ContainerGlobalChest container;
 	private TileEntityGlobalChest tileEntity;
 	
 	//Tooltip data
 	private int[] tickNow = {0};
 	private int[] tickMax = {200};
 	private boolean isTickInRange[] = {false};
 	
 	private EntityPlayer player;
 
 	public GuiButton cheatButton;
 	
 	public GuiGlobalChest(EntityPlayer player, TileEntityGlobalChest tileEnity) {
 		super(container = new ContainerGlobalChest(player, tileEnity));
 		this.player = player;
 		this.tileEntity = tileEnity;
 	}
 
 	@Override
 	public void initGui(){
 		super.initGui();
 		cheatButton = new GuiButton(0, this.width / 2, this.height / 2, 60, 10, container.inventory.gamemode.getName());
 		cheatButton.drawButton = container.player.worldObj.getWorldInfo().areCommandsAllowed();
 		buttonList.add(cheatButton);
 		alignButtons();
 	}
 	
 	public void alignButtons(){
 		cheatButton.xPosition = this.width / 2 + 20;
 		cheatButton.yPosition = this.height / 2 - 73;
 	}
 	
 	@Override
 	public void updateScreen(){
 		super.updateScreen();
 		cheatButton.drawButton = container.player.worldObj.getWorldInfo().areCommandsAllowed();
 	}
 	
 	@Override
 	protected void drawGuiContainerForegroundLayer(int par1, int par2){
         fontRenderer.drawString("Global Chest", 8, 5, 4210752);
         fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 3, 4210752);
         fontRenderer.drawString("Chest Weight: " + (container.totalPrice < 0 ? 0 : container.totalPrice), 8, 14, 4210752);
         fontRenderer.drawString("Max Weight: " + GlobalChests.globalChestManager.maxWeight, 110 - Integer.toString(GlobalChests.globalChestManager.maxWeight).length() * 6, ySize - 96 + 3, 4210752);
 	}
 	
 	@Override
     public void onGuiClosed(){
         super.onGuiClosed();
         tileEntity.closeChest();
     }
 	
 	public void setTooltipTick(int par1TooltipID){
 		if(this.isTickInRange.length > par1TooltipID){
 			this.isTickInRange[par1TooltipID] = true;
 		}else{
 			throw new ArrayIndexOutOfBoundsException("Trying to activate a non-existing tooltip tick.");
 		}
 	}
 	
 	@Override
     protected void drawItemStackTooltip(ItemStack par1ItemStack, int par2, int par3)
     {
         List list = par1ItemStack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
         Slot slot = null;
         boolean flag0 = false;
         
         for(int i = 0; i < this.inventorySlots.inventorySlots.size(); i++){
         	Slot sl = (Slot) this.inventorySlots.inventorySlots.get(i);
         	try{
 	        	if(sl.inventory.getStackInSlot(i) == par1ItemStack){
 	        		flag0 = true;
 	        		slot = sl;
 	        		break;
 	        	}
         	}catch(ArrayIndexOutOfBoundsException e){}
         }
         
         if(!flag0){
 	        for(int i = 0; i < this.mc.thePlayer.inventoryContainer.inventorySlots.size(); i++){
 	        	Slot sl = (Slot) this.mc.thePlayer.inventoryContainer.inventorySlots.get(i);
 	        	try{
 		        	if(sl.inventory.getStackInSlot(i) == par1ItemStack){
 		        		slot = sl;
 		        		break;
 		        	}
	        	}catch(ArrayIndexOutOfBoundsException e){
	        		return;
	        	}
 	        }
         }
         
         boolean flag1 = false;
         if(slot != null){
         	flag1 = slot.inventory instanceof InventoryGlobalChest;
         }
         
         if(GlobalChests.globalChestManager.isItemBanned(par1ItemStack)){
         	list.add(EnumChatFormatting.GRAY + "This item cannot be transfered.");
         }else{
         	if(GlobalChests.globalChestManager.getItemPrice(par1ItemStack) < 0){
         		list.add(EnumChatFormatting.GRAY + "This item frees: " + -GlobalChests.globalChestManager.getItemPrice(new ItemStack(par1ItemStack.getItem(), 1)) + " Grams");
         	}else{
 		    	list.add(EnumChatFormatting.GRAY + "This item weights: " + GlobalChests.globalChestManager.getItemPrice(new ItemStack(par1ItemStack.getItem(), 1)) + " Grams");
 		    	list.add(EnumChatFormatting.GRAY + "This stack weights: " + GlobalChests.globalChestManager.getItemPrice(par1ItemStack) + " Grams");
         	}
         	if(GlobalChests.globalChestManager.isItemStackLimited(par1ItemStack)){
         		list.add(EnumChatFormatting.GRAY + "The amount of this item is limited to: " + GlobalChests.globalChestManager.getStackLimit(par1ItemStack));
         	}
 			if(container.totalPrice + GlobalChests.globalChestManager.getItemPrice(new ItemStack(par1ItemStack.getItem(), par1ItemStack.stackSize)) > GlobalChests.globalChestManager.maxWeight && !flag1){
 				list.add(EnumChatFormatting.GRAY + "Cannot fit this stack.");
 			}else if(container.totalPrice - GlobalChests.globalChestManager.getItemPrice(new ItemStack(par1ItemStack.getItem(), par1ItemStack.stackSize)) > GlobalChests.globalChestManager.maxWeight && flag1){
 				list.add(EnumChatFormatting.GRAY + "Cannot remove this stack.");
 			}
         }
         
         for (int k = 0; k < list.size(); ++k)
         {
             if (k == 0)
             {
                 list.set(k, "\u00a7" + Integer.toHexString(par1ItemStack.getRarity().rarityColor) + (String)list.get(k));
             }
             else
             {
                 list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
             }
         }
 
         this.func_102021_a(list, par2, par3);
     }
 	
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.renderEngine.bindTexture("/gui/globalChest.png");
         int x = (width - xSize) / 2;
         int y = (height - ySize) / 2;
         this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
 	}
 }
