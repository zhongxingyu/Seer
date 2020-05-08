 package com.isocraft.tileentity;
 
 import java.util.Map;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 import com.google.common.collect.ImmutableMap;
 import com.isocraft.block.BlockDataManager;
 import com.isocraft.lib.BlockInfo;
 import com.isocraft.lib.Strings;
 import com.isocraft.network.PacketTypeHandler;
 import com.isocraft.network.packets.PacketClientDisplay;
 import com.isocraft.thesis.ThesisSystem;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * ISOCraft
  * 
  * Tile Entity class for the Data Manager
  * 
  * @author Turnermator13
  */
 
 public class TileDataManager extends TileEntityISOCraftInventory {
 
     public static int slots = 3;
     public static final Map<Integer, AdvancedDataSlotInfo> advancedSlots = ImmutableMap.of(0, new AdvancedDataSlotInfo(0, "Slot for PDA or Disk"), 1, new AdvancedDataSlotInfo(1, "Slot for Translation Scripts (if Required)"), 2, new AdvancedDataSlotInfo(2, "Slot for Item to be Copied or Digitized"));
     
     public int state = 0;
     private boolean change = false;
     private int ticker = 0;
     private boolean guiOpen = false;
     
     @SideOnly(Side.CLIENT)
     public boolean guiChange;
     
     private int modeNo = 4;
     private boolean[] modeState  = new boolean[]{true, false, false, false};
     public int modeSelected = 0;
     public int modesAvalible = 1;
     
     public int overridePing = 0;
     public int overridePingTicker = 0;
     
 	public int CpyProgress = 0;
 	private ItemStack prevCpyStack = null;
 	private ItemStack CpyStack = null;
 
     public TileDataManager() {
         super(slots, BlockInfo.DataManager_tileentity, advancedSlots);
     }
 
     @Override
     public void updateEntity() {
         super.updateEntity();
        ++ticker;
               
         if(!worldObj.isRemote){
             if(ticker == 50 || ticker == 100 || guiOpen){
            	if(ticker == 120){
             		ticker = 0;
             		if(CpyProgress == 3000){
             			CpyProgress = 0;
             		}
             	}
             	
            	if(ticker == 60 && CpyProgress == 4000){
             		CpyProgress = 0;            		
             	}
             	
                 if(this.getStackInSlot(0) != null){
                     state = 2;
                     change = true;
                 } else {
                     state = 0;
                     change = true;
                 }
                 
                 if(CpyProgress == 1000 && ticker == 0){
                 	CpyProgress = 2000;
                 }
                 
                 if(CpyProgress == 2100){
             		if(CpyStack.equals(this.prevCpyStack)){
             			this.CpyProgress = 4000;
             		} 
                 }
                 
                 if(CpyProgress == 2200){
             		this.setInventorySlotContents(0, CpyStack);
             		CpyProgress = 3000;
                 }
                 
                 if(CpyProgress > 1990 && CpyProgress < 2200){
                 	CpyProgress = CpyProgress + 1;
                 }
             }
             
             if(change){
                 BlockDataManager.updateState(state, worldObj, xCoord, yCoord, zCoord);
                 change = false;
             }
             
             if(overridePing == 1){
                 if(overridePingTicker == 2){
                     overridePing = 0;
                     overridePingTicker = 0;
                 } else {
                     ++overridePingTicker;
                 }
             }
         }
     }
     
     public void cpy(String peram){
     	if(CpyProgress == 0){
 	    	if (peram.contains("-")) {
 	    		String[] parts = peram.split("-");
 	    		String thesisRef = parts[0];
 	    		String theoremRef = parts[1];
 	    		
 	    		ItemStack iStack = ThesisSystem.addTheoremToItem(this.getStackInSlot(0), ThesisSystem.getThesisFromReference(thesisRef), ThesisSystem.getThesisFromReference(thesisRef).getTheoremFromReference(theoremRef));
 	    		
 	    		this.prevCpyStack = this.getStackInSlot(0);
 	        	this.CpyStack = iStack;
 	        	
 	        	
 	    		this.CpyProgress = 1000;
 	        	
 	    	} else {
 	    	    throw new IllegalArgumentException("String " + peram + " does not contain -");
 	    	}
     	}
     }
     
     public void cycleMode(Player player){
        int nextMode = this.modeSelected + 1;
        boolean flag = false;
        
        while(!flag){
            if(nextMode > (this.modeNo - 1)){
                nextMode = 0;
            } else if(!this.modeState[nextMode]) {
                ++nextMode;
            } else {
                this.modeSelected = nextMode;
                flag = true;
            }
        }
        this.sendReleventData(player);
     }
     
     private boolean modeCheck(){
         boolean ret = false;
         
         if(!this.modeState[this.modeSelected]) {
             ret = false;
         } else {
             ret = true;
         }  
         return ret;
     }
     
     private int modesAvalible(){
         int ret = 0;
         for(int i = 0; i < this.modeNo; ++i){
             if(this.modeState[i]){
                 ++ret;
             }
         }
         return ret;
     }
     
     private void modeManager(){
         if(this.getStackInSlot(0) != null){ 
             this.modeState[1] = true;
         } else { 
             this.modeState[1] = false;
         }
         
         if(this.getStackInSlot(2) != null){
             this.modeState[2] = true;
         } else {
             this.modeState[2] = false;
         }
         
         if(this.getStackInSlot(0) != null && this.getStackInSlot(2) != null){
             this.modeState[3] = true;
         } else { 
             this.modeState[3] = false;
         }
     }
     
     private void sendReleventData(Player player){
         if(this.modeSelected != 0){
             if (!this.worldObj.isRemote) {
                 if(this.modeSelected == 1){
                     PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketClientDisplay(this.getStackInSlot(0).stackTagCompound.getCompoundTag(Strings.Thesis_name))), player);
                 } else if(this.modeSelected == 2){
                     PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketClientDisplay(this.getStackInSlot(2).stackTagCompound.getCompoundTag(Strings.Thesis_name))), player);
                 } else if(this.modeSelected == 3){
                     PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketClientDisplay(this.getStackInSlot(2).stackTagCompound.getCompoundTag(Strings.Thesis_name))), player);
                 }
             }
         } 
     }
     
     @Override
     public void onInventoryChanged(){      
        if(this.guiOpen){
            this.modeManager();
            this.modesAvalible = this.modesAvalible();
            if(!modeCheck()){
                this.overridePing = 1;
                this.modeSelected = 0;
            }
        }
     }
     
     @Override
     public void readFromNBT(NBTTagCompound nbtTagCompound) {
         super.readFromNBT(nbtTagCompound);
         this.state = nbtTagCompound.getInteger("state");
         this.modeSelected = nbtTagCompound.getInteger("mode");
         
         this.modeManager();
         change = true;
     }
     
     @Override
     public void writeToNBT(NBTTagCompound nbtTagCompound) {
         super.writeToNBT(nbtTagCompound);
         nbtTagCompound.setInteger("state", this.state);
         nbtTagCompound.setInteger("mode", this.modeSelected);
     }
     
     public void openChest(Player player) {
         this.openChest();
         this.overridePing = 1;
         this.overridePingTicker = 0;
         this.modesAvalible = this.modesAvalible();
         this.modeManager();
         this.sendReleventData(player);
     }
 
     @Override
     public void openChest() {
         this.guiOpen = true;
     }
     
     @Override
     public void closeChest() {
         this.guiOpen = false;
     }
 }
