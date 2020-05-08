 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package net.minecraft.src.WirelessRedstonePipes;
 
 import net.minecraft.src.ExtraBuildcraftPipes.TileExtractionPipe;
 import net.minecraft.src.*;
 
 public class TileWirelessExtractionPipe extends TileExtractionPipe implements IWirelessPipe
 {
 	public Object currentFreq;
 
     public TileWirelessExtractionPipe()
     {
     	super();
 		currentFreq = 0;
    	setPowerProvider(mod_WirelessRedstonePipes.wirelessPowerFramework.createPowerProvider());
     }
 
 	@Override
 	public String getInvName() {
 		return "Wireless Extraction Pipe";
 	}
 
 	@Override
 	public Object getFreq() {
 		return currentFreq;
 	}
 
 	@Override
 	public void setFreq(int frequency) {
 		if (frequency < 0)
 			frequency = 0;
 		if (frequency > 9999)
 			frequency = 9999;
 
 		currentFreq = frequency;
 		updateEntity();
 	}
 
 	@Override
 	public boolean isPowered() {
 		return RedstoneEther.getInstance().getFreqState(currentFreq);
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
 			return false;
 		}
 		return entityplayer.getDistanceSq((double) xCoord + 0.5D,
 				(double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64D;
 	}
 }
