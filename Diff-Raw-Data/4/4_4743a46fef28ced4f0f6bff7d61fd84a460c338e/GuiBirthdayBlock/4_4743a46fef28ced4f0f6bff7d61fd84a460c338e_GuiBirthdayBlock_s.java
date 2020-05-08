 package mtshaw113.juli.interfaces;
 
 import mtshaw113.juli.tileentity.TileEntityJuli;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class GuiBirthdayBlock extends GuiContainer {
 
     public GuiBirthdayBlock(InventoryPlayer invPlayer, TileEntityJuli juli) {
         super(new ContainerBirthdayBlock(invPlayer, juli));
         
        xSize = 1280;
        ySize = 720;
     }
     
     private static final ResourceLocation texture  = new ResourceLocation("example", "textures/gui/machine.jpg");
     
     @Override
     protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1, 1, 1, 1);
         
         Minecraft.getMinecraft().func_110434_K().func_110577_a(texture);
         drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
     }
     
 }
