 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import java.util.EnumSet;
 
 public class TickHandlerServerPlayer implements ITickHandler {
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData){
 /*
 		EntityPlayer player = (EntityPlayer)tickData[0];
 		if(player != null){
 			ItemStack itemstack = player.getCurrentItemOrArmor(0);
 			Item item = (itemstack == null ? null : itemstack.getItem());
 			if(item instanceof ItemEnderXT){
 				
 			}
 		}
 */
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData){}
 
 	@Override
 	public EnumSet<TickType> ticks(){
 		return EnumSet.of(TickType.PLAYER);
 	}
 
 	@Override
 	public String getLabel(){
 		return "AUExtras: Server Player Tick";
 	}
 }
