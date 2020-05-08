 package kovu.teamstats;
 
 import java.util.EnumSet;
 
 import kovu.utils.gui.GuiUtils;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.src.ModLoader;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.network.Player;
 
 public class ClientTickHandler implements ITickHandler {
 	
 	private boolean guinotopen;
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 
         if (type.equals(EnumSet.of(TickType.RENDER))) {
             onRenderTick();
         } else if (type.equals(EnumSet.of(TickType.CLIENT))) {
             GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
             if (guiscreen != null) {
                 onTickInGUI(guiscreen);
             } else {
                 onTickInGame();
             }
 
         }
     }
 
     @Override
     public EnumSet<TickType> ticks() {
 
         return EnumSet.of(TickType.RENDER, TickType.CLIENT);
     }
 
     @Override
     public String getLabel() {
 
         return null;
     }
 
     public void onTickInGUI(GuiScreen guiscreen) {
     	
     	guinotopen = false;
     }
 
     public void onTickInGame() {
     	
     	guinotopen = true;
     }
 
     public void onRenderTick() 
     {   	
     	if(guinotopen == true)
     	{
     		GuiTeamInfoIngame.drawMain();
     	}
    	SkinHandler sk = new SkinHandler();
     }
 }
