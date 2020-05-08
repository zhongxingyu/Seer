 package mods.minecraft.darth.dc.event;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.lwjgl.opengl.GL11;
 
 import mods.minecraft.darth.dc.lib.Textures;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 
 import net.minecraftforge.client.event.RenderGameOverlayEvent;
 import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
 import net.minecraftforge.event.EventPriority;
 import net.minecraftforge.event.ForgeSubscribe;
 
 //Code on the Forge Tutorials - Edited a bit by Darth
 public class GuiEffectBar extends Gui
 {
     private Minecraft mc;
 
     public GuiEffectBar(Minecraft mc)
     {
           super();
           
           // We need this to invoke the render engine.
           this.mc = mc;
     }
 
     private static final int BUFF_ICON_SIZE = 18;
     private static final int BUFF_ICON_SPACING = BUFF_ICON_SIZE + 2; // 2 pixels between buff icons
     
     //Constants for features of effect icon sheet
     private static final int BUFF_ICON_BASE_U_OFFSET = 0;
     private static final int BUFF_ICON_BASE_V_OFFSET = 198;
     private static final int BUFF_ICONS_PER_ROW = 8;
     
     
     // This event is called by GuiIngameForge during each frame by GuiIngameForge.pre() and GuiIngameForce.post().
     @SuppressWarnings("rawtypes")
     @ForgeSubscribe(priority = EventPriority.NORMAL)
     public void onRenderExperienceBar(RenderGameOverlayEvent event)
     {
           if(event.isCancelable() || event.type != ElementType.EXPERIENCE)
           {      
               return;
           }
     
           //Starting position for the buff bar - 2 pixels from the top left corner.
           int xPos = 2;
           int yPos = 2;
           
           Collection collection = this.mc.thePlayer.getActivePotionEffects();
           
           if (!collection.isEmpty())
           {
                 GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                 GL11.glDisable(GL11.GL_LIGHTING);
                 
                this.mc.func_110434_K().func_110577_a(Textures.VANILLA_HUD);  
         
                 for (Iterator iterator = this.mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext(); xPos += BUFF_ICON_SPACING)
                 {
                       PotionEffect potioneffect = (PotionEffect) iterator.next();
                       Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
             
                       if (potion.hasStatusIcon())
                       {
                             int iconIndex = potion.getStatusIconIndex();
                             this.drawTexturedModalRect(
                                 xPos,
                                 yPos, 
                                 BUFF_ICON_BASE_U_OFFSET + iconIndex % BUFF_ICONS_PER_ROW * BUFF_ICON_SIZE,
                                 BUFF_ICON_BASE_V_OFFSET + iconIndex / BUFF_ICONS_PER_ROW * BUFF_ICON_SIZE,
                                 BUFF_ICON_SIZE,
                                 BUFF_ICON_SIZE);
                       }
                 }
           }
     }
 }
