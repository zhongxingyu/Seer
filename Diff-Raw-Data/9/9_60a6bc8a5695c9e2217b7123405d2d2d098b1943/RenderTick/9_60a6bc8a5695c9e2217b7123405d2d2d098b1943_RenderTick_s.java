 package net.medsouz.miney.updater;
 
 import java.util.EnumSet;
 
 import net.minecraft.client.Minecraft;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class RenderTick implements ITickHandler{
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 		
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		if(MineyUpdater.updater != null){
			Minecraft.getMinecraft().currentScreen.drawString(Minecraft.getMinecraft().fontRenderer, "Miney is updating!", 2, 2, 16777215);
 		}
 		if(MineyUpdater.updateFailed){
			Minecraft.getMinecraft().currentScreen.drawString(Minecraft.getMinecraft().fontRenderer, "Miney failed to update, please try again later!", 2, 2, 16777215);
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.RENDER);
 	}
 
 	@Override
 	public String getLabel() {
 		return "Miney Updater Render Tick";
 	}
 
 }
