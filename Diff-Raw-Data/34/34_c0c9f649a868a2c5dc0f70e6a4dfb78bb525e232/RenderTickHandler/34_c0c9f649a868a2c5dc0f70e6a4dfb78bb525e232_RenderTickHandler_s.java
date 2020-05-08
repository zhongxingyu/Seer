 package silentAbyss.core.handlers.tick;
 
 import java.util.EnumSet;
 
 import silentAbyss.core.handlers.ChaosHandler;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.entity.player.EntityPlayer;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 
 public class RenderTickHandler implements ITickHandler {
     
     public void onRenderTick() {
         
         // This is temporary... might add an item for it later.
         drawHUD();
     }
     
     private void drawHUD() {
 
         // Shows the current chaos level/factor
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft().gameSettings,
                Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        FontRenderer fontRender = Minecraft.getMinecraft().fontRenderer;
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        
        StringBuilder s = new StringBuilder();
        s.append(ChaosHandler.getChaos());
        s.append(" (");
        s.append(String.format("%.3f", ChaosHandler.getChaosFactor()));
        s.append(")");
        
        fontRender.drawStringWithShadow(s.toString(), 100, 200, 0xFFFFFF);
     }
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {}
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 
         onRenderTick();
     }
 
     @Override
     public EnumSet<TickType> ticks() {
 
         return EnumSet.of(TickType.RENDER);
     }
 
     @Override
     public String getLabel() {
 
         return "Render";
     }
 
 }
